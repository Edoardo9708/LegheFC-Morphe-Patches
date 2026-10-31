'use strict';

import Java from 'frida-java-bridge';

const TAG = 'PB-F10';
const MARKER = 'PB-FRIDA-20261030-F10';
let started = false;
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

      // Resolve the Java class and overload. Do NOT install an implementation.
      // This isolates Java.use()/overload() from actual ART method interception.
      void method;
      info('METHOD LOOKUP OK; NO HOOK INSTALLED');
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
