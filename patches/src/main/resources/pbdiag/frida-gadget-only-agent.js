'use strict';

/**
 * F7 diagnostic: Frida Gadget core-only.
 *
 * Intentionally NO frida-java-bridge import, Java.perform, interception,
 * PackageManager access, certificate manipulation, or network requests.
 * This isolates Gadget runtime/script startup from F6's Java instrumentation.
 */
const TAG = 'PB-GadgetOnly';
const MARKER = 'PB-FRIDA-20261030-F7';
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

rpc.exports = {
  init(stage) {
    info('GADGET READY stage=' + String(stage));
    info('CORE ONLY; no Java bridge and no hooks');
    setTimeout(function () { info('ALIVE after 5s'); }, 5000);
    setTimeout(function () { info('ALIVE after 30s'); }, 30000);
  }
};
