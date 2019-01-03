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
  let id = uuid();
  while (tasks.has(id)) {
    id = uuid();
  }
  const task = {
    roles: new Map()
  };
  task.roles = new Map();
  for (const role of rolesInfo) {
    task.roles.set(role.name, {
      name: role.name,
      type: role.type,
      config: role.config,
      currentCase: 0
    });
  }
  tasks.set(id, task);
  return id;
}

// Emit a message to all other clients in current test. Message type is 'iatf-workflow'.
const sendUserMessage = function(currentRoleName, roleInfoList, message) {
  console.log('Send user message: ' + JSON.stringify(message));
  for (const role of roleInfoList) {
    if (role.name !== currentRoleName) {
      if (!role.socket) {
        return;
      }
      role.socket.emit('iatf-workflow', message);
    }
  }
}

// Emit a message to all clients in current test. Message type is 'iatf-control'.
const sendSystemMessage = function(task, message) {
  console.log('Send system message: ' + JSON.stringify(message));
  for (const role of task.roles.values()) {
    if (!role.socket) {
      return;
    }
    role.socket.emit('iatf-control', message);
  }
}

// Are all roles ready for start a task.
const isTaskReady = function(task) {
  for (const role of task.roles.values()) {
    if (!role.socket) {
      return false;
    }
  }
  return true;
}

// Are all roles ready for a specific case.
const isCaseReady = function(roleInfoList) {
  let caseNumber;
  for (const role of roleInfoList.values()) {
    caseNumber = caseNumber || role.currentCase;
    console.log('Case number: ' + caseNumber);
    if (role.state !== RoleState.ready || caseNumber !== role.currentCase) {
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

  if (!tasks.has(taskId)) {
    console.warn('Invalid task ID.');
    socket.disconnect(true);
    return;
  }
  if (!tasks.get(taskId).roles.has(role)) {
    console.warn('Invalid role.');
    socket.disconnect(true);
    return;
  }
  const task = tasks.get(taskId);
  const roleInfo = task.roles.get(role);
  roleInfo.socket = socket;
  roleInfo.type = type;
  roleInfo.currentCase = 0;

  console.log('A new client connected. Task ID: ' + taskId + ', role: ' +
    role + ', type: ' + type);

  socket.on('iatf-workflow', (message) => {
    // Add sender info and forward this message to all other participants.
    message.sender = role;
    sendUserMessage(role, task.roles.values(), message);
  });

  socket.on('iatf-control', (message) => {
    switch (message.type) {
      case 'case-ready':
        roleInfo.currentCase++;
        roleInfo.state = RoleState.ready;
        console.log(task);
        if (isCaseReady(task.roles)) {
          sendSystemMessage(task, {
            type: 'case-start',
            data: {
              caseNumber: roleInfo.currentCase
            }
          });
        }
        break;
      case 'case-end':
        break;
      default:
        console.warn('Unrecognized control message from ' + role + '.');
    }
  });

  if (isTaskReady(task)) {
    sendSystemMessage(task, {
      type: 'task-start'
    });
    console.log('Task is started.');
  }
});

// Create a new test.
// Example: {"roles":[{"name": "r1","type":"javascript"},{"name": "r2","type":"javascript"}]}
webapp.put('/rest/tasks', (req, res) => {
  // req.body is expected to be [{name: string for role name, type: string for device type}].
  console.log(req);
  const taskId = addTask(req.body.roles);
  if (taskId) {
    return res.status(200).send(taskId);
  }
});

const rolesMapToResponse = function(roles) {
  const response = {
    roles: []
  };
  for (const [roleId, roleInfo] of roles.roles) {
    response.roles.push({
      name: roleId,
      type: roleInfo.type,
      config: roleInfo.config,
      state: RoleState.unknown,
      currentCase: 0
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
