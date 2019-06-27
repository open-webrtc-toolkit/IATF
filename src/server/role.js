// Copyright (C) <2019> Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0
'use strict';

const State = Object.freeze({
  unknown: 0,
  ready: 1, // Waiting for next case.
  testing: 2, // Running a case.
  ended: 3 // All case are finished.
});

module.exports.State = State;

// A role is a participant in a specific testing task. It will be associated with a device when running cases.
module.exports.Role = class Role {
  constructor(roleInit) {
    Object.defineProperty(this, 'name', {
      configurable: false,
      writable: false,
      value: roleInit.name
    });
    Object.defineProperty(this, 'type', {
      configurable: false,
      writable: false,
      value: roleInit.type
    });
    Object.defineProperty(this, 'config', {
      configurable: false,
      writable: false,
      value: roleInit.config
    });
    Object.defineProperty(this, 'state', {
      configurable: false,
      writable: true,
      value: State.unknown
    });
    // Current case number. Starts from 1.
    Object.defineProperty(this, 'currentCase', {
      configurable: false,
      writable: true,
      value: 0
    });
    this._connection = null;
    // Event handlers.
    this.onCaseStart = undefined;
    this.onCaseEnd = undefined;
    this.onConnect = undefined;
    this.onDisconnect = undefined;
    this.onWorkflowData = undefined; // Receive workflow message from client side.
  }

  sendControlData(message) {
    if (this._connection) {
      console.log('Role sending control message: ' + message);
      this._connection.send('iatf-control', message);
    }
  }

  sendWorkflowData(message) {
    if (this._connection) {
      this._connection.send('iatf-workflow', message);
    }
  }

  isConnected() {
    return !!this._connection;
  }

  setConnection(connection) {
    this._connection = connection;
    connection.onData = (type, data) => {
      if (type === 'iatf-control') {
        this._onControlData(data);
      } else if (type === 'iatf-workflow') {
        this._onWorkflowData(data);
      } else {
        console.warn('Unrecognized message type ' + type);
        return;
      }
    };
  }

  _onControlData(data) {
    switch (data.type) {
      case 'case-ready':
        this.currentCase++;
        this.state = State.ready;
        if (this.onCaseStart) {
          this.onCaseStart();
        }
        break;
      case 'case-end':
        if (this.onCaseEnd) {
          this.onCaseEnd();
        }
        break;
      default:
        console.warn('Unrecognized control message from ' + role + '.');
    }
  }

  _onWorkflowData(data) {
    if (this.onWorkflowData) {
      this.onWorkflowData(data);
    }
  }
};
