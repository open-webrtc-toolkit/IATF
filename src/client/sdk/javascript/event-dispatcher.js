// Copyright (C) <2019> Intel Corporation
//
// SPDX-License-Identifier: Apache-2.0
'use strict';

/**
 * @class EventDispatcher
 * @classDesc A shim for EventTarget. Might be changed to EventTarget later.
 * @hideconstructor
 */
export class EventDispatcher {
  constructor() {
    this._eventListeners = new Map();
  }

  /**
   * @function addEventListener
   * @desc This function registers a callback function as a handler for the corresponding event.
   * @instance
   * @param {string} eventType Event string.
   * @param {function} listener Callback function.
   */
  addEventListener(eventType, listener) {
    if (!this._eventListeners.has(eventType)) {
      this._eventListeners.set(eventType, []);
    }
    for (const l of this._eventListeners.get(eventType)) {
      if (l === listener) {
        console.log('Duplicated listener. Ignored.');
        return;
      }
    }
    this._eventListeners.get(eventType).push(listener);
  }

  /**
   * @function removeEventListener
   * @desc This function removes a registered event listener.
   * @instance
   * @param {string} eventType Event string.
   * @param {function} listener Callback function.
   */
  removeEventListener(eventType, listener) {
    if (!this._eventListeners.has(eventType)) {
      console.log('No specified event type.');
      return;
    }
    this._eventListeners.get(eventType).splice(this._eventListeners.indexOf(listener), 1);
  }

  dispatchEvent(event) {
    if (!this._eventListeners.has(event.type)) {
      return;
    }
    for (const listener of this._eventListeners.get(event.type)) {
      listener(event);
    }
  }
}

/**
 * @class IatfEvent
 * @classDesc Class IatfEvent represents a generic Event in the library.
 * @hideconstructor
 */
export class IatfEvent {
  constructor(type) {
    this.type = type;
  }
}

/**
 * @class WorkflowEvent
 * @classDesc Class WorkflowEvent represents a workflow event sent from other endpoints.
 * @hideconstructor
 */
export class WorkflowEvent extends IatfEvent {
  constructor(type, init) {
    super(type);
    /**
     * @member {string} sender
     * @instance
     * @desc role of the remote endpoint who sent this event.
     */
    this.sender = init.sender;
    /**
     * @member {string} message
     * @instance
     */
    this.message = init.message;
  }
}

/**
 * @class ControlEvent
 * @classDesc Class ControlEvent represents a control event sent from server.
 * @hideconstructor
 */
export class ControlEvent extends IatfEvent {
  constructor(type, init) {
    super(type);
    /**
     * @member {string} message
     * @instance
     */
    this.message = init.message;
  }
}