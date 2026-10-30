'use strict';

import Java from 'frida-java-bridge';

const TAG = 'PB-F9';
const MARKER = 'PB-FRIDA-20261030-F9';
let started = false;
let count = 0;
let logFunction = null;

try {
  logFunction = new NativeFunction(
    Module.getGlobalExportByName('__android_log_write'),
    'int', ['int', 'pointer', 'pointer']
  );
} catch (_) {}

function info(message) {
  const line = MARKER + ' ' + message;
  if (logFunction !== null) {
    try {
      logFunction(4, Memory.allocUtf8String(TAG), Memory.allocUtf8String(line));
      return;
    } catch (_) {}
  }
  console.log(line);
}

function setup() {
  if (started) return;
  started = true;
  if (!Java.available) {
    info('JAVA NOT AVAILABLE');
    return;
  }

  Java.perform(function () {
    try {
      const manager = Java.use('android.app.ApplicationPackageManager');
      const method = manager.getPackageInfo.overload('java.lang.String', 'int');

      method.implementation = function (name, options) {
        const result = method.call(this, name, options);
        count++;
        if (count <= 8) info('PASS THROUGH count=' + count);
        return result;
      };

      info('HOOK READY; ORIGINAL RESULT UNCHANGED');
    } catch (error) {
      info('SETUP ERROR ' + String(error));
    }
  });
}

rpc.exports = {
  init(stage) {
    info('GADGET READY stage=' + String(stage));
    setTimeout(setup, 1500);
    setTimeout(function () { info('ALIVE after 5s'); }, 5000);
    setTimeout(function () { info('ALIVE after 30s'); }, 30000);
  }
};
