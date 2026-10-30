'use strict';

/**
 * Prezzi Benzina / F3 — native-only Frida Gadget startup test.
 *
 * DIAGNOSTIC ONLY. No Java bridge, no method hooks, no replacements,
 * no signature spoofing, no network changes.
 *
 * The F1 crash had a SIGSEGV with an anonymous executable frame; this test
 * separates loading the Gadget from Java method instrumentation.
 */
const MARKER = 'PB-FRIDA-20261030-F3';
const TAG = 'PB-Signature';

function log(message) {
  const line = MARKER + ' ' + message;
  try {
    // Use Android's native logging directly; avoid Java/ART instrumentation.
    const address = Module.getGlobalExportByName('__android_log_write');
    const writeLog = new NativeFunction(address, 'int', ['int', 'pointer', 'pointer']);
    writeLog(4, Memory.allocUtf8String(TAG), Memory.allocUtf8String(line));
  } catch (error) {
    console.log(line + ' (Android log error: ' + String(error) + ')');
  }
}

rpc.exports = {
  init(stage, parameters) {
    log('READY, native-only startup; stage=' + String(stage));
    log('NO JAVA HOOKS; open the Maps tab and check whether the app stays alive');
  }
};
