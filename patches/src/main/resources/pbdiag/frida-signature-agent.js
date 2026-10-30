'use strict';

import Java from 'frida-java-bridge';

/*
 * Prezzi Benzina F2 / Android 17 signature lookup trace.
 * Built with frida-compile 19.1.0 + frida-java-bridge 7.0.13.
 * Read-only: all original Android method implementations are executed unchanged.
 */
const TAG = 'PB-Signature';
const MARKER = 'PB-FRIDA-20261030-F2';
const TARGET = 'org.vernazza.androidfuel';
const MAX_HITS = 50;
let started = false;

function report(message) {
  console.log(MARKER + ' ' + message);
}

function install() {
  if (started) return;
  started = true;

  if (!Java.available) {
    report('Java VM unavailable');
    return;
  }

  Java.perform(function () {
    const PM = Java.use('android.app.ApplicationPackageManager');
    const Log = Java.use('android.util.Log');
    const Throwable = Java.use('java.lang.Throwable');
    let hits = 0;

    function info(message) {
      try { Log.i(TAG, MARKER + ' ' + message); }
      catch (err) { report('Log.i unavailable: ' + err); }
    }

    function stack() {
      try {
        return String(Log.getStackTraceString(Throwable.$new()))
          .split('\n').slice(2, 14).join(' | ').substring(0, 1600);
      } catch (err) {
        return 'stack unavailable ' + err;
      }
    }

    function hook(name) {
      if (!PM[name]) {
        info('UNAVAILABLE ' + name);
        return;
      }

      const overloads = PM[name].overloads;
      for (let i = 0; i < overloads.length; i++) {
        (function (overload) {
          overload.implementation = function () {
            const args = Array.prototype.slice.call(arguments);
            const a = args.length ? String(args[0]) : '';
            const b = args.length > 1 ? String(args[1]) : '';
            const relevant = a.includes(TARGET) ||
              (name === 'checkSignatures' && b.includes(TARGET));

            if (relevant && hits < MAX_HITS) {
              hits++;
              const detail = args.map(x => {
                if (x === null || x === undefined) return 'null';
                return String(x).substring(0, 90);
              }).join(', ');
              info('HIT ' + hits + ' ' + name + '(' + detail + ')');
              info('STACK ' + hits + ' ' + stack());
              if (hits === MAX_HITS) info('HIT LIMIT reached');
            }
            // Never modify return values or arguments.
            return overload.apply(this, args);
          };
        })(overloads[i]);
      }
      info('HOOKED ' + name + ' overloads=' + overloads.length);
    }

    ['getPackageInfo', 'getPackageInfoAsUser', 'getApplicationInfo',
     'hasSigningCertificate', 'checkSignatures'].forEach(hook);

    info('READY; open the Maps tab');
  });
}

rpc.exports = {
  init(stage, parameters) {
    report('INIT stage=' + stage + ' Java.available=' + Java.available);
    try { install(); } catch (err) { report('ERROR ' + String(err)); }
  }
};
