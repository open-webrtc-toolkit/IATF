const app = require('express');
const fs = require('fs');
const path = require('path');
const commander = require('commander');

commander.option('--key_file', 'Key for Socket.IO server.').option(
  '--certificate_file', 'Certificate for Socket.IO server.').parse(process.argv);

const httpsOptions = {
  key: fs.readFileSync(path.resolve(__dirname, 'certs', 'privatekey.key'),
    'utf8'),
  cert: fs.readFileSync(path.resolve(__dirname, 'certs', 'certificate.crt'),
    'utf8')
};

const socketio = require('socket.io').listen(require('https').createServer(
  httpsOptions, require('express')()).listen(9000));
