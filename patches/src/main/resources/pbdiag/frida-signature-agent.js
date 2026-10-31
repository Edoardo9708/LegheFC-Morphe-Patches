'use strict';

import Java from 'frida-java-bridge';

/**
 * Prezzi Benzina F5 - read-only signature result diagnostics.
 *
 * One hook only: ApplicationPackageManager.getPackageInfo(String, int).
 * The original implementation is invoked exactly once with unchanged args.
 * We inspect the actual PackageInfo RETURNED by Android; no new PM requests.
 * Logs SHA-1 and SHA-256 digests only (never raw certificate bytes).
 * No modification of signatures, API keys, return values, or Play Services.
 */
const TAG = 'PB-Signature';
const MARKER = 'PB-FRIDA-20261030-F5';
const TARGET = 'org.vernazza.androidfuel';
const MAX_HITS = 30;
const GET_SIGNING_CERTIFICATES = 0x08000000;
const GET_SIGNATURES = 0x00000040;
let installed = false;
let hits = 0;

let nativeWrite = null;
try {
  nativeWrite = new NativeFunction(
    Module.getGlobalExportByName('__android_log_write'),
    'int',
    ['int', 'pointer', 'pointer']
  );
} catch (_) {}

function info(message) {
  const line = MARKER + ' ' + message;
  try {
    if (nativeWrite) {
      nativeWrite(4, Memory.allocUtf8String(TAG), Memory.allocUtf8String(line));
    } else {
      console.log(line);
    }
  } catch (_) {
    console.log(line);
  }
}

function installSingleHook() {
  if (installed) return;
  installed = true;

  try {
    info('JAVA available=' + String(Java.available));
    if (!Java.available) {
      info('NO JAVA VM: cannot trace');
      return;
    }

    Java.perform(function () {
      try {
        const PM = Java.use('android.app.ApplicationPackageManager');
        const target = PM.getPackageInfo.overload('java.lang.String', 'int');
        const MD = Java.use('java.security.MessageDigest');
        const Log = Java.use('android.util.Log');
        const Throwable = Java.use('java.lang.Throwable');

        function hexDigest(data, algorithm) {
          const bytes = MD.getInstance(algorithm).digest(data);
          const hex = [];
          for (let j = 0; j < bytes.length; j++) {
            hex.push((bytes[j] & 0xff).toString(16).padStart(2, '0'));
          }
          return hex.join(':').toUpperCase();
        }

        function describeSigners(label, signers, n) {
          if (signers === null || signers === undefined) {
            info('CERT HIT ' + n + ' ' + label + '=null');
            return;
          }
          info('CERT HIT ' + n + ' ' + label + '.count=' + signers.length);
          for (let i = 0; i < signers.length && i < 8; i++) {
            try {
              const der = signers[i].toByteArray();
              info('CERT HIT ' + n + ' ' + label + '[' + i + ']' +
                ' SHA1=' + hexDigest(der, 'SHA-1') +
                ' SHA256=' + hexDigest(der, 'SHA-256'));
            } catch (digestError) {
              info('CERT-ERROR HIT ' + n + ' ' + label +
                '[' + i + ']: ' + String(digestError));
            }
          }
        }

        function readReturnedCertificate(result, requestFlags, n) {
          // Do not query PackageManager again: report only what was returned.
          if (result === null) {
            info('CERT HIT ' + n + ' PackageInfo=null');
            return;
          }
          if ((requestFlags & GET_SIGNING_CERTIFICATES) !== 0) {
            try {
              const signingInfo = result.signingInfo.value;
              if (signingInfo === null) {
                info('CERT HIT ' + n + ' signingInfo=null');
                return;
              }
              const multiple = !!signingInfo.hasMultipleSigners();
              info('CERT HIT ' + n + ' signingInfo.present=true multiple=' +
                String(multiple));
              describeSigners('apkContentsSigners',
                signingInfo.getApkContentsSigners(), n);
              if (!multiple) {
                describeSigners('signingCertificateHistory',
                  signingInfo.getSigningCertificateHistory(), n);
              }
            } catch (certError) {
              info('CERT-ERROR HIT ' + n + ' signingInfo: ' +
                String(certError));
            }
          } else if ((requestFlags & GET_SIGNATURES) !== 0) {
            try {
              describeSigners('deprecatedSignatures', result.signatures.value, n);
            } catch (certError) {
              info('CERT-ERROR HIT ' + n + ' signatures: ' +
                String(certError));
            }
          } else {
            info('CERT HIT ' + n + ' not requested (flags lack signature bits)');
          }
        }

        target.implementation = function (packageName, flags) {
          // Do not touch calls involving any other package.
          if (String(packageName) !== TARGET) {
            return target.call(this, packageName, flags);
          }

          // Keep Android's behavior intact, including thrown exceptions.
          const returned = target.call(this, packageName, flags);
          if (hits >= MAX_HITS) return returned;
          const n = ++hits;

          try {
            const requestFlags = (Number(flags) >>> 0);
            info('HIT ' + n + ' getPackageInfo(' + String(packageName) +
              ', flags=' + String(flags) + ', hex=0x' +
              requestFlags.toString(16).toUpperCase() + ')');
            const trace = String(Log.getStackTraceString(Throwable.$new()))
              .split('\n').slice(2, 16).join(' | ').substring(0, 2400);
            info('STACK ' + n + ' ' + trace);
            readReturnedCertificate(returned, requestFlags, n);
            if (n === MAX_HITS) info('HIT LIMIT reached');
          } catch (logError) {
            info('INSPECT-ERROR HIT ' + n + ' ' + String(logError));
          }

          return returned;
        };

        info('HOOKED ApplicationPackageManager.getPackageInfo(String,int)');
        info('READY: inspect existing returns, open Maps tab');
      } catch (installError) {
        info('INSTALL-ERROR ' + String(installError));
      }
    });
  } catch (javaError) {
    info('JAVA-INIT-ERROR ' + String(javaError));
  }
}

rpc.exports = {
  init(stage, parameters) {
    info('GADGET READY, stage=' + String(stage) + ', scheduling one hook');
    setTimeout(installSingleHook, 1500);
  }
};
