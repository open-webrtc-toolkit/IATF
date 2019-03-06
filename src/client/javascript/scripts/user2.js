import {
  Task
} from "../../sdk/javascript/task.js";

const expect = chai.expect;
const url = window.location.href;
const task = new Task({
  taskId: (new URL(document.location)).searchParams.get("taskId"),
  role: (new URL(document.location)).searchParams.get('role'),
  socketIoUrl: 'https://' + document.domain + ':8080/'
});

describe('Interactivity tests', function() {
  this.timeout(50000);  // Set a larger timeout value because it needs to wait for another endpoint's response.
  document.getElementById('iatf-state').innerText = 'Preparing';
  before(() => {
    // Start a new task.
    return task.start();
  });
  describe('Basic connection tests', () => {
    beforeEach(() => {
      // Start a new test case.
      task.bindListener('user1')
      return task.startCase();
    });
    afterEach(() => {
      // Stop a test case.
      return task.stopCase();
    });
    it('testConnect should success.', (done) => {
      task.waitWorkflowLock('user1', 'connect').then(() => {
        console.log("user1 connect")
      }).then(() => {
        task.run(() => {
           task.notifyWorkflowLock('user2', 'connect');
        })
        done()
      }).catch(err => {
        done(err)
      })
    });
  });

  after(() => {
    document.getElementById('iatf-state').innerText = 'Finished';
  });
}).timeout(50000);

mocha.checkLeaks();
mocha.run();