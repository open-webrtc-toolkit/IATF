import {
  EventDispatcher,
  ControlEvent,
  WorkflowEvent
} from './event-dispatcher.js';

const CaseState = Object.freeze({
  unknown: 0,
  ready: 1,
  testing: 2,
  ended: 3
});

export class Task extends EventDispatcher {
  constructor(config) {
    super();
    this._started = false;
    this._caseSequenceNumber = 0;
    this._resolveStartCase;
    this._caseState = CaseState.unknown;
    this._eventMsg = new Map();
    Object.defineProperty(this, 'configuration', {
      configurable: false,
      writable: false,
      value: config
    });
  };

  // Start task and wait for all other participants are ready.
  start() {
    return new Promise((resolve, reject) => {
      this._socket = io(this.configuration.socketIoUrl, {
        query: 'taskId=' + this.configuration.taskId + '&role=' + this.configuration.role + '&type=JavaScript'
      });
      this._socket.on('iatf-control', (message) => {
        console.log('Received control message: ' + JSON.stringify(message));
        switch (message.type) {
          case 'task-start':
            if (this._started) {
              console.warn('Duplicated start event.');
              return;
            }
            this._started = true;
            console.log('Start resolved.');
            resolve();
            break;
          case 'task-end':
            if (!this._started) {
              console.warn('Task is not started.');
              return;
            }
            this._started = false;
            this.dispatchEvent(new ControlEvent('end'));
            break;
          case 'case-start':
            if (this._resolveStartCase) {
              this._caseState = CaseState.testing;
              this._resolveStartCase();
            } else {
              console.warn('Receive case-start event in invalid state.')
            }
            break;
          default:
            console.warn('Unrecognized control message: ' + JSON.stringify(message));
        }
      });
      this._socket.on('iatf-workflow', (message) => {
        console.log('Recieved workflow message: ' + message);
        this.dispatchEvent(new WorkflowEvent(message.type, {
          sender: message.sender,
          message: message.data
        }));
      });
    });
  };

  startCase() {
    this._caseSequenceNumber++;
    this._caseState = CaseState.ready;
    this._socket.emit('iatf-control', {
      type: 'case-ready',
      message: {
        sequenceNumber: this._caseSequenceNumber
      }
    });
    // Wait for 'case-start' event from server.
    return new Promise((resolve) => {
      this._resolveStartCase = resolve;
    });
  }

  stopCase() {
    this._caseState = CaseState.ended;
    this._socket.emit('iatf-control', {
      type: 'case-end',
      message: {
        sequenceNumber: this._caseSequenceNumber
      }
    });
    // Return immediately, so app has more time to prepare for the next case. But we can wait for server's confirmation without changing API.
    return Promise.resolve();
  }

  bindListener(eventType) {
    this.addEventListener(eventType, (event) => {
      console.log(eventType)
      console.log(event)
      if (!this._eventMsg.has(eventType)) {
        this._eventMsg.set(eventType, event);
      }else{
        this._eventMsg.get(eventType).sender = event.sender;
        this._eventMsg.get(eventType).message = event.data;
      }
    })
  }

  waitWorkflowLock(eventType, eventData) {
    return new Promise((resolve, reject) => {
      let interval = setInterval(() => {
        if (this._eventMsg.has(eventType) && this._eventMsg.get(eventType).message == eventData){
          clearInterval(interval)
          clearTimeout(timeout)
          resolve()
        }
      }, 500)
      let timeout = setTimeout(() => {
         clearInterval(interval)
         reject("can not get message:" + eventData)
      }, 10000)
    })
  }

  notifyWorkflowLock(type, data) {
    this._socket.emit('iatf-workflow', {
      type: type,
      data: data
    });
  }

  run(func){
    func()
    return Promise.resolve();
  }
}