// Copyright (C) <2019> Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0
'use strict';

const chai = require('chai');
const sinon = require('sinon');
const Role = require('../role.js').Role;
const expect = chai.expect;

const iatfControl = 'iatf-control';
const iatfWorkflow = 'iatf-workflow';

describe('Test role module.', () => {
  let role;
  let fakeConnection;
  const testMessages = ['control-event', {
    p1: 'v1',
    p2: 'v2'
  }, 777];
  beforeEach(() => {
    role = new Role({
      name: 'role1',
      type: 'javascript'
    });
    fakeConnection = Object.create({});
    fakeConnection.send = sinon.spy();
  });
  it('Role constroctor.', () => {
    expect(role).to.be.instanceof(Role);
  });

  it('isConnected returns true after connection is set.', () => {
    expect(role.isConnected()).to.be.false;
    role.setConnection(fakeConnection);
    expect(role.isConnected()).to.be.true;
  });

  it('Sending data.', () => {
    role.setConnection(fakeConnection);
    for (const message of testMessages) {
      role.sendControlData(message);
      role.sendWorkflowData(message);
      expect(fakeConnection.send.withArgs(iatfControl, message).calledOnce)
        .to.be.true;
      expect(fakeConnection.send.withArgs(iatfWorkflow, message).calledOnce)
        .to.be.true;
    }
  });

  it(
    'onWorkflowData should be called when a new workflow data is received.',
    () => {
      role.setConnection(fakeConnection);
      role.onWorkflowData = sinon.spy();
      for (const message of testMessages) {
        fakeConnection.onData(iatfWorkflow, message);
        expect(role.onWorkflowData.calledWith(message)).to.be.true;
      }
    });

  it('Callbacks should be called when control data is received.', () => {
    role.setConnection(fakeConnection);
    role.onCaseStart = sinon.spy();
    role.onCaseEnd = sinon.spy();
    fakeConnection.onData(iatfControl, {
      type: 'case-ready'
    });
    expect(role.onCaseStart.calledOnce).to.be.true;
    fakeConnection.onData(iatfControl, {
      type: 'case-end'
    });
    expect(role.onCaseEnd.calledOnce).to.be.true;
  })
});
