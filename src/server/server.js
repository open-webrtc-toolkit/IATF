// Starts a HTTP server for dashboard and a Socket.IO server for testing.

const express = require('express');
const fs = require('fs');
const spdy = require('spdy');
const path = require('path');
const commander = require('commander');
const uuid = require('uuid/v4');

const webapp = express();
webapp.use(express.json()); // to support JSON-encoded bodies
webapp.use(express.urlencoded()); // to support URL-encoded bodies

// Map{taskId, {roles: map{roleName, {name: string, socket: socket, type: string}}}}.
const tasks = new Map();

// `commander`'s properties use undersource-case because arguments are usersource-cased.
commander.description('IATF server.').option('--key_file <key_file>',
  'Key for Socket.IO server.').option(
  '--certificate_file <certificate_file>',
  'Certificate for Socket.IO server.').parse(process.argv);

const httpsOptions = {
  key: fs.readFileSync(commander.key_file).toString(),
  cert: fs.readFileSync(commander.certificate_file).toString()
};

const httpServer = spdy.createServer(httpsOptions, webapp);
httpServer.listen(8080);

const io = require('socket.io').listen(httpServer);

// Initialize a test.
const addTask = function(roles) {
  let id = uuid();
  while (tasks.has(id)) {
    id = uuid();
  }
  const task = {
    roles: new Map()
  };
  for (const role of roles) {
    task.roles.set(role.name, {
      name: role.name,
      type: role.type
    });
  }
  tasks.set(id, task);
  return id;
}

// Emit a message to all other clients in current test. Message type is 'iatf-workflow'.
const sendUserMessage = function(currentRoleName, roleInfoList, message) {
  for (const role of roleInfoList) {
    if (role.name !== currentRoleName) {
      role.socket.emit(message);
    }
  }
}

// Emit a message to all clients in current test. Message type is 'iatf-control'.
const sendSystemMessage = function(test, message) {
  for (const role of test.roles.values()) {
    role.socket.emit(message);
  }
}

const areAllRolesReady = function(test) {
  for (const role of test.roles.values()) {
    if (!role.socket) {
      return false;
    }
  }
  return true;
}

io.on('connection', socket => {
  // `taskId` is a unique string indicates a round of testing.
  const taskId = socket.handshake.query.taskId;
  // Current client's role in this testing.
  const role = socket.handshake.query.role;
  // Client type. e.g. JavaScript, iOS, Android.
  const type = socket.handshake.query.type;

  if (!tasks.get(taskId)) {
    console.warn('Invalid test ID.');
    socket.disconnect(true);
  }
  if (!tasks.get(taskId).roles.has(role)) {
    console.warn('Invalid role.');
    socket.disconnect(true);
  }
  const test = tasks.get(taskId);
  const roleInfo = tasks.get(role);
  roleInfo.socket = socket;
  roleInfo.type = type;

  console.log('A new client connected. Test ID: ' + taskId + ', role: ' +
    role + ', type: ' + type);

  if (areAllRolesReady(tasks.get(taskId))) {
    sendSystemMessage(test, 'start');
    // Record start time.
  }
});

// Create a new test.
webapp.put('/rest/tasks', (req, res) => {
  // req.body is expected to be [{name: string for role name, type: string for device type}].
  console.log(req);
  const taskId = addTask(req.body.roles);
  if (taskId) {
    return res.send(200, taskId);
  }
});

const rolesMapToResponse = function(roles) {
  const response = {
    roles: []
  };
  for (const [roleId, roleInfo] of roles.roles) {
    response.roles.push({
      name: roleInfo.name,
      type: roleInfo.type
    });
  }
  return response;
}

const tasksMapToResponse = function() {
  const response = [];
  for (const [id, roles] of tasks) {
    response.push({
      id: id,
      roles: rolesMapToResponse(roles)
    });
  }
  return response;
}

webapp.get('/rest/tasks', (req, res) => {
  return res.send(tasksMapToResponse())
});

webapp.get('/rest/tasks/:task', (req, res) => {
  const id = req.params.task;
  if (!tasks.has(id)) {
    res.status(404);
  }
  return res.send(rolesMapToResponse(tasks.get(id)));
})

webapp.use('/web', express.static('LockServerHtmlClient'));

webapp.get('*', (req, res) => {
  res.send(404, 'Not found');
});
