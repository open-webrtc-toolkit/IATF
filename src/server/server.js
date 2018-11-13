// Starts a HTTP server for dashboard and a Socket.IO server for testing.

const app = require('express');
const fs = require('fs');
const http2 = require('http2');
const path = require('path');
const commander = require('commander');

// Map{testId, {roles: map{role, {socket: socket, type: string}}}}.
const tests = new Map();

// `commander`'s properties use undersource-case because arguments are usersource-cased.
commander.description('IATF server.').option('--key_file <key_file>',
  'Key for Socket.IO server.').option(
  '--certificate_file <certificate_file>',
  'Certificate for Socket.IO server.').parse(process.argv);

const httpsOptions = {
  key: fs.readFileSync(commander.key_file).toString(),
  cert: fs.readFileSync(commander.certificate_file).toString()
};

const httpServer = http2.createSecureServer(httpsOptions);
httpServer.listen(8080);

const io = require('socket.io').listen(require('https').createServer(
  httpsOptions, require('express')()).listen(9000));

// Initialize a test.
const initTest = function(id, roles) {
  if (tests.has(id)) {
    console.warn('Test ID exists.');
    return false;
  }
  const info = {
    roles: new Map()
  };
  for (const role of roles) {
    info.roles.set(role, {});
  }
  tests.set(id, info);
  return true;
}

// Emit to message to all other clients in current test.
const notifyOtherRoles = function(currentRole, roleInfoList, message) {

}

io.on('connection', socket => {
  // `testId` is a unique string indicates a round of testing.
  const testId = socket.handshake.query.testId;
  // Current client's role in this testing.
  const role = socket.handshake.query.role;
  // Client type. e.g. JavaScript, iOS, Android.
  const type = socket.handshake.query.type;

  if (!tests.get(testId)) {
    console.warn('Invalid test ID.');
    socket.disconnect(true);
  }
  if (!tests.get(testId).roles.has(role)) {
    console.warn('Invalid role.');
    socket.disconnect(true);
  }
  tests.get(testId).roles.get(role).socket = socket;

  console.log('A new client connected. Test ID: ' + testId + ', role: ' +
    role + ', type: ' + type);
});
