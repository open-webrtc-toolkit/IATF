// Starts a HTTP server for dashboard and a Socket.IO server for testing.

const express = require('express');
const fs = require('fs');
const spdy = require('spdy');
const path = require('path');
const commander = require('commander');
const uuid = require('uuid/v4');
const Task = require('./task.js').Task;

const webapp = express();
webapp.use(express.json()); // to support JSON-encoded bodies
webapp.use(express.urlencoded()); // to support URL-encoded bodies

const RoleState = Object.freeze({
  unknown: 0,
  ready: 1,
  testing: 2,
  ended: 3
});

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

// Initialize a task.
const addTask = function(rolesInfo) {
  console.log(rolesInfo);
  let id = uuid();
  while (tasks.has(id)) {
    id = uuid();
  }
  tasks.set(id, new Task(id, rolesInfo));
  return id;
}

io.on('connection', socket => {
  // `taskId` is a unique string indicates a round of testing.
  const taskId = socket.handshake.query.taskId;
  // Current client's role in this testing.
  const role = socket.handshake.query.role;
  // Client type. e.g. JavaScript, iOS, Android.
  const type = socket.handshake.query.type;

  if (!tasks.has(taskId)) {
    console.warn('Invalid task ID.');
    socket.disconnect(true);
    return;
  }
  const task = tasks.get(taskId);
  console.log(task.roles);
  if (!tasks.get(taskId).roles.find(r => {
      return r.name === role;
    })) {
    console.warn('Invalid role.');
    socket.disconnect(true);
    return;
  }
  // The `connection` for task and role has a `send` message and a `onData` event handle.
  const connection = Object.create({});
  connection.send = (type, message) => {
    console.log('Sending '+type+': '+message);
    socket.emit(type, message);
  };
  for (const type of ['iatf-control', 'iatf-workflow']) {
    socket.on(type, (data)=>{
      if (connection.onData) {
        connection.onData(type, data);
      }
    })
  }
  task.setConnection(role, connection);

  console.log('A new client connected. Task ID: ' + taskId + ', role: ' +
    role + ', type: ' + type);
});

// Create a new test.
// Example: {"roles":[{"name": "r1","type":"javascript"},{"name": "r2","type":"javascript"}]}
webapp.put('/rest/v1/tasks', (req, res) => {
  // req.body is expected to be [{name: string for role name, type: string for device type}].
  const taskId = addTask(req.body.roles);
  if (taskId) {
    return res.status(200).send(taskId);
  }
});

const rolesMapToResponse = function(task) {
  const response = {
    roles: []
  };
  for (const role of task.roles) {
    response.roles.push({
      name: role.name,
      type: role.type,
      config: role.config
    });
  }
  return response;
}

const tasksMapToResponse = function() {
  const response = [];
  for (const task of tasks) {
    response.push({
      id: task.id,
      roles: rolesMapToResponse(task)
    });
  }
  return response;
}

webapp.get('/rest/v1/tasks', (req, res) => {
  return res.send(tasksMapToResponse())
});

webapp.get('/rest/v1/tasks/:task', (req, res) => {
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
