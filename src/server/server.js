const app = require('express');
const fs = require('fs');
const path = require('path');
const commander = require('commander');

// `commander`'s properties use undersource-case because arguments are usersource-cased.
commander.description('IATF server.').option('--key_file <key_file>',
  'Key for Socket.IO server.').option(
  '--certificate_file <certificate_file>',
  'Certificate for Socket.IO server.').parse(process.argv);

const httpsOptions = {
  key: fs.readFileSync(commander.key_file).toString(),
  cert: fs.readFileSync(commander.certificate_file).toString()
};

const socketio = require('socket.io').listen(require('https').createServer(
  httpsOptions, require('express')()).listen(9000));
