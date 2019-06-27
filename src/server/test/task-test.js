// Copyright (C) <2019> Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0
'use strict';

const chai = require('chai');
const sinon = require('sinon');
const Task = require('../task.js').Task;
const expect = chai.expect;

chai.use(require('chai-as-promised'));

describe('Test task module.', () => {
  let task;
  beforeEach(() => {
    task = new Task(undefined, [{
        name: 'role1',
        type: 'JavaScript',
        config: {
          url: 'http://localhost:8081/javascript/test.html'
        }
      },
      {
        name: 'role2',
        type: 'JavaScript',
        config: {
          url: 'http://localhost:8081/javascript/test.html'
        }
      }
    ])
  });

  it('Create a task.', () => {
    expect(task).to.be.instanceof(Task);
  });

  it('A task is ready when all roles are connected.', ()=>{
    expect(task._isReady()).to.be.false;
    const fakeConnection = Object.create({});
    fakeConnection.send = sinon.spy();
    task.setConnection('role1', fakeConnection);
    expect(task._isReady()).to.be.false;
    task.setConnection('role2', fakeConnection);
    expect(task._isReady()).to.be.true;
  });
});
