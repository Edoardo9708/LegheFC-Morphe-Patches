'use strict';

import Java from 'frida-java-bridge';

/**
 * F6 - EXPERIMENTAL in-process certificate substitution.
 * Caller supplies an X.509 PUBLIC certificate (DER/Base64) using Morphe option.
 * Only hooks getPackageInfo(String,int) for this app package, retaining the
 * original method call and its normal exception behavior.
 *
 * This cannot change the installed APK signature, Android PackageManager
 * service, Binder responses to other processes, or Google backend identity.
 * Never install alongside the read-only F5 tracer.
 */
const TAG = 'PB-CustomSig';
const MARKER = 'PB-FRIDA-20261030-F6';
const TARGET = 'org.vernazza.androidfuel';
const OPTION_NAME = 'lab.prezzibenzina.pbdiag.CUSTOM_CERT_DER';
const GET_SIGNING_CERTIFICATES = 0x08000000;
const GET_SIGNATURES = 0x00000040;
let initialized = false;
let logged = 0;
let nativeWrite = null;

try {
  nativeWrite = new NativeFunction(
    Module.getGlobalExportByName('__android_log_write'),
    'int', ['int', 'pointer', 'pointer']
  );
} catch (_) {}

function info(message) {
  const line = MARKER + ' ' + String(message);
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

function setup() {
  if (initialized) return;
  initialized = true;

  try {
    if (!Java.available) {
      info('ERROR Java unavailable');
      return;
    }

    Java.perform(function () {
      try {
        const ActivityThread = Java.use('android.app.ActivityThread');
        const Base64 = Java.use('android.util.Base64');
        const PM = Java.use('android.app.ApplicationPackageManager');
        const Signature = Java.use('android.content.pm.Signature');
        const SigningInfo = Java.use('android.content.pm.SigningInfo');
        const CertificateFactory = Java.use('java.security.cert.CertificateFactory');
        const ByteArrayInputStream = Java.use('java.io.ByteArrayInputStream');
        const MessageDigest = Java.use('java.security.MessageDigest');
        const ArrayList = Java.use('java.util.ArrayList');

        const application = ActivityThread.currentApplication();
        if (application === null) {
          info('ERROR no currentApplication; no hooks installed');
          return;
        }

        const appInfo = application.getPackageManager()
          .getApplicationInfo(TARGET, 0x80);
        const metadata = appInfo.metaData.value;
        const encoded = metadata === null ? null : metadata.getString(OPTION_NAME);
        if (encoded === null || String(encoded).length === 0) {
          info('ERROR custom certificate missing; no hooks installed');
          return;
        }
        const bytes = Base64.decode.overload('java.lang.String', 'int')
          .call(Base64, encoded, 0);
        const certificate = CertificateFactory.getInstance('X.509')
          .generateCertificate(ByteArrayInputStream.$new(bytes));
        const replacementSignature = Signature.$new(bytes);
        const signers = ArrayList.$new();
        signers.add(replacementSignature);
        const keys = ArrayList.$new();
        keys.add(certificate.getPublicKey());

        // Compute the selected certificate digest once, not on every call.
        const sha1 = MessageDigest.getInstance('SHA-1').digest(bytes);
        const parts = [];
        for (let i = 0; i < sha1.length; i++) {
          parts.push((sha1[i] & 255).toString(16).padStart(2, '0'));
        }
        info('CERT READY SHA1=' + parts.join(':').toUpperCase());

        const original = PM.getPackageInfo.overload('java.lang.String', 'int');

        original.implementation = function (packageName, flags) {
          // One unmodified PackageManager call, including exception semantics.
          const result = original.call(this, packageName, flags);
          if (String(packageName) !== TARGET || result === null) return result;

          const f = Number(flags) >>> 0;
          if ((f & (GET_SIGNING_CERTIFICATES | GET_SIGNATURES)) === 0) {
            return result;
          }

          try {
            if ((f & GET_SIGNING_CERTIFICATES) !== 0) {
              const current = result.signingInfo.value;
              let scheme = 3;
              if (current !== null) {
                scheme = Number(current.getSchemeVersion());
              }
              // Android API >= 35 supports this public SigningInfo constructor.
              const customInfo = SigningInfo.$new.overload(
                'int',
                'java.util.Collection',
                'java.util.Collection',
                'java.util.Collection'
              ).call(SigningInfo, scheme, signers, keys, signers);
              result.signingInfo.value = customInfo;
            }

            if ((f & GET_SIGNATURES) !== 0) {
              result.signatures.value = Java.array(
                'android.content.pm.Signature', [replacementSignature]
              );
            }

            if (logged < 8) {
              logged++;
              info('LOCAL RESULT CHANGED hit=' + logged +
                ' flags=0x' + f.toString(16).toUpperCase());
              if (logged === 8) info('Further messages suppressed');
            }
          } catch (error) {
            // Fail open: preserve original result if replacement fails.
            info('REPLACEMENT ERROR ' + String(error));
          }
          return result;
        };

        info('HOOKED getPackageInfo(String,int); return override is local only');
      } catch (error) {
        info('INSTALL ERROR ' + String(error));
      }
    });
  } catch (error) {
    info('JAVA ERROR ' + String(error));
  }
}

rpc.exports = {
  init(stage, parameters) {
    info('GADGET READY stage=' + String(stage));
    // Do not instrument ART while Application.onCreate is still initializing.
    setTimeout(setup, 1500);
  }
};
