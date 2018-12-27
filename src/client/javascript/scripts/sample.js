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

describe('Interactivity tests', () => {
  document.getElementById('iatf-state').innerText = 'Preparing';
  before(() => {
    return task.start();
  });
  describe('Basic connection tests', () => {
    beforeEach(() => {
      return task.startCase();
    });
    afterEach(() => {
      return task.stopCase();
    });
    it('Each endpoint sends a message should success.', (done) => {
      task.addEventListener('test1', (event) => {
        if (event.sender != role) {
          done();
        }
      })
      task.send('test1', 'Something useful.');
    });
    it('Each endpoint sends a message again should success.', (done) => {
      task.addEventListener('test2', () => {
        if (event.sender != role) {
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