'use strict';

import Java from 'frida-java-bridge';

/**
 * F9: isolate the ApplicationPackageManager.getPackageInfo(String,int)
 * interception from F6's certificate/SigningInfo replacement.
 *
 * Same Gadget, Java bridge and 1500 ms setup delay used by F6/F8.
 * Exactly one Java hook, always returns the untouched original result.
 * Never changes PackageInfo/signatures/certificates or calls network APIs.
 */
const TAG = 'PB-NoopHook';
const MARKER = 'PB-FRIDA-20261030-F9';
const TARGET = 'org.vernazza.androidfuel';
let started = false;
let seen = 0;
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
      return;
    }
  } catch (_) {}
  console.log(line);
}

function setup() {
  if (started) return;
  started = true;

  try {
    if (!Java.available) {
      info('ERROR Java unavailable; no hook installed');
      return;
    }
    info('JAVA PERFORM REQUESTED');
    Java.perform(function () {
      try {
        const PM = Java.use('android.app.ApplicationPackageManager');
        const original = PM.getPackageInfo.overload('java.lang.String', 'int');
        original.implementation = function (packageName, flags) {
          // Preserve return object identity and Android's exception behavior.
          const result = original.call(this, packageName, flags);
          if (String(packageName) === TARGET && seen < 8) {
            seen++;
            const f = Number(flags) >>> 0;
            info('PASS THROUGH hit=' + seen +
              ' flags=0x' + f.toString(16).toUpperCase());
            if (seen === 8) info('Further hits suppressed');
          }
          return result;
        };
        info('HOOK INSTALLED getPackageInfo(String,int); return unchanged');
      } catch (e) {
        info('INSTALL ERROR ' + String(e));
      }
    });
  } catch (e) {
    info('JAVA ERROR ' + String(e));
  }
}

rpc.exports = {
  init(stage) {
    info('GADGET READY stage=' + String(stage));
    info('NOOP HOOK ONLY; never modifies PackageInfo');
    setTimeout(setup, 1500);
    setTimeout(function () { info('ALIVE after 5s'); }, 5000);
    setTimeout(function () { info('ALIVE after 30s'); }, 30000);
  }
};
