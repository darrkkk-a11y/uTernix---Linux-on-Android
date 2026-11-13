const path = require('node:path');
const moduleDir = process.argv[2];
if (!moduleDir) throw new Error('Expected ARM64 node-pty module directory');
const pty = require(path.resolve(moduleDir, 'lib/index.js'));
let output = '';
const child = pty.spawn('/bin/sh', ['-c', 'printf node-pty-ok'], {
  name: 'xterm-color', cols: 80, rows: 24, cwd: process.cwd(), env: process.env,
});
child.onData(data => { output += data; });
child.onExit(({ exitCode }) => {
  if (exitCode !== 0 || !output.includes('node-pty-ok')) {
    console.error('PTY smoke test failed', exitCode, output);
    process.exitCode = 1;
  } else console.log('PTY smoke test passed');
});
setTimeout(() => { console.error('PTY smoke test timed out'); process.exit(1); }, 10000).unref();
