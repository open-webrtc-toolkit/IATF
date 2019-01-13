const uuid = require('uuid/v4');
const Role = require('./role.js').Role;
const RoleState = require('./role.js').State;

// A testing task runs specific cases with specific devices.
module.exports.Task = class Task {
  // roleInits - List of roleInit defined in role.js.
  constructor(id, roleInits) {
    Object.defineProperty(this, 'id', {
      configurable: false,
      writable: false,
      value: id || uuid()
    });
    // this._roles is a map for internal use, this.role is an array.
    this._roles = new Map(roleInits.map(roleInit => [roleInit.name, new Role(
      roleInit)]));
    for (const role of this._roles.values()) {
      role.onCaseStart = () => {
        console.log(role.name + ' reports case started.');
        if (this._isCaseReady()) {
          this._sendControlData({
            type: 'case-start'
          });
        }
      };
      role.onWorkflowData = (data) => {
        data.sender = role.name;
        for (const r of this.roles) {
          if (r.name !== role.name) {
            r.sendWorkflowData(data);
          }
        }
      }
      role.onCaseEnd = undefined;
      role.onConnect = undefined;
      role.onDisconnect = undefined;
    }
    Object.defineProperty(this, 'roles', {
      configurable: false,
      writable: false,
      value: Array.from(this._roles.values())
    });
  }

  _sendControlData(message) {
    for (const role of this._roles.values()) {
      role.sendControlData(message);
    }
  }

  // Are all roles ready for start this task.
  _isReady() {
    for (const role of this._roles.values()) {
      if (!role.isConnected()) {
        return false;
      }
    }
    return true;
  }

  // Are all roles ready for a specific case.
  _isCaseReady() {
    let caseNumber;
    for (const role of this._roles.values()) {
      caseNumber = caseNumber || role.currentCase;
      if (role.state !== RoleState.ready || caseNumber !== role.currentCase) {
        return false;
      }
    }
    return true;
  }

  // A new client is connected to this task.
  setConnection(roleName, connection) {
    if (!this._roles.has(roleName)) {
      return;
    }
    this._roles.get(roleName).setConnection(connection);
    if (this._isReady()) {
      this._sendControlData({
        type: 'task-start'
      });
      console.log('Task is started.');
    }
  }
};
