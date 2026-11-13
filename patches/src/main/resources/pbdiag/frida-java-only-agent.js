'use strict';

import Java from 'frida-java-bridge';

/**
 * F8 - experimental Java bridge isolation test for Android 17.
 * Same Gadget and launch path as F7/F6, but invokes only Java.perform.
 * No Java.use, no hooks, no PackageManager calls, no certificate changes.
 */
const TAG = 'PB-JavaOnly';
const MARKER = 'PB-FRIDA-20261030-F8';
let nativeWrite = null;
let started = false;

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
      info('ERROR Java unavailable; no hooks installed');
      return;
    }
    info('JAVA PERFORM REQUESTED');
    Java.perform(function () {
      info('JAVA PERFORM OK; no hooks installed');
    });
  } catch (e) {
    info('JAVA ERROR ' + String(e));
  }
}

rpc.exports = {
  init(stage) {
    info('GADGET READY stage=' + String(stage));
    info('JAVA BRIDGE ONLY; no interception or certificate changes');
    // Match the 1500 ms delay in F6 to isolate the Java bridge specifically.
    setTimeout(setup, 1500);
    setTimeout(function () { info('ALIVE after 5s'); }, 5000);
    setTimeout(function () { info('ALIVE after 30s'); }, 30000);
  }
};
