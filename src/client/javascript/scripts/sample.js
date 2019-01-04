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
      return task.startCase();
    });
    afterEach(() => {
      // Stop a test case.
      return task.stopCase();
    });
    it('Each endpoint sends a message should success.', (done) => {
      task.addEventListener('test1', (event) => {
        if (event.sender != task.role) {
          // Execute done when received message from another endpoint.
          done();
        }
      })
      task.send('test1', 'Something useful.');
    });
    it('Each endpoint sends a message again should success.', (done) => {
      task.addEventListener('test2', (event) => {
        if (event.sender != task.role) {
          done();
        }
      })
      task.send('test2', 'Something more useful.');
    });
  });

  after(() => {
    document.getElementById('iatf-state').innerText = 'Finished';
  });
}).timeout(50000);

mocha.checkLeaks();
mocha.run();