'use strict';

import Java from 'frida-java-bridge';

/**
 * PB F4: one Java hook, read-only.
 * Target: ApplicationPackageManager.getPackageInfo(String, int).
 * Scope: this APK process only, and only calls asking for Prezzi Benzina.
 * No certificate edits, request replacements, or changes to return values.
 */
const TAG = 'PB-Signature';
const MARKER = 'PB-FRIDA-20261030-F4';
const TARGET = 'org.vernazza.androidfuel';
const MAX_HITS = 30;
let ready = false;
let hits = 0;

let nativeWrite = null;
try {
  const addr = Module.getGlobalExportByName('__android_log_write');
  nativeWrite = new NativeFunction(addr, 'int', ['int', 'pointer', 'pointer']);
} catch (_) {}

function info(message) {
  const line = MARKER + ' ' + message;
  try {
    if (nativeWrite !== null) {
      nativeWrite(4, Memory.allocUtf8String(TAG), Memory.allocUtf8String(line));
    } else {
      console.log(line);
    }
  } catch (_) { console.log(line); }
}

function installSingleHook() {
  if (ready) return;
  ready = true;

  try {
    info('JAVA available=' + String(Java.available));
    if (!Java.available) {
      info('JAVA NOT AVAILABLE: no hooks installed');
      return;
    }

    Java.perform(function () {
      try {
        const PM = Java.use('android.app.ApplicationPackageManager');
        const target = PM.getPackageInfo.overload('java.lang.String', 'int');
        const Log = Java.use('android.util.Log');
        const Throwable = Java.use('java.lang.Throwable');

        target.implementation = function (packageName, flags) {
          // Everything outside Prezzi Benzina is passed through uninspected.
          if (String(packageName) === TARGET && hits < MAX_HITS) {
            hits += 1;
            try {
              info('HIT ' + hits + ' getPackageInfo(package=' +
                String(packageName) + ', flags=' + String(flags) + ')');
              const trace = String(Log.getStackTraceString(Throwable.$new()))
                .split('\n').slice(2, 15).join(' | ').substring(0, 2400);
              info('STACK ' + hits + ' ' + trace);
              if (hits === MAX_HITS) info('HIT LIMIT reached');
            } catch (logError) {
              info('STACK-ERROR ' + String(logError));
            }
          }
          // Executes the original Android implementation unchanged.
          return target.call(this, packageName, flags);
        };

        info('HOOKED ApplicationPackageManager.getPackageInfo(String,int)');
        info('READY: now open the Maps tab');
      } catch (error) {
        info('INSTALL-ERROR ' + String(error));
      }
    });
  } catch (error) {
    info('JAVA-INIT-ERROR ' + String(error));
  }
}

rpc.exports = {
  init(stage, parameters) {
    info('GADGET READY, stage=' + String(stage) + ', scheduling exactly one hook');
    // The previous F1 installed several Java hooks during early app startup;
    // let Application.onCreate return before touching ART.
    setTimeout(installSingleHook, 1500);
  }
};
