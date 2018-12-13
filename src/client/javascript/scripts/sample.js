const expect = chai.expect;
const socket = io('https://localhost:8080/?taskId=1f26d0b1-a76b-448c-82e0-ca45fffed938&role=role2&type=JavaScript');

describe('Interactivity tests', () => {
  document.getElementById('state').innerText = 'Preparing';
  before(() => {
    return new Promise((resolve) => {
      socket.on('iatf-control', (data) => {
        switch (data) {
          case 'start':
            document.getElementById('state').innerText = 'Started';
            resolve();
            break;
          default:
            console.log('Received unknown message: ' + JSON.stringify(data));
        }
      })
    });
  });
  describe('Basic connection tests', () => {
    it('Each endpoint sends a message should success.', () => {
      socket.emit('test');
    });
  });

  after(()=>{
    document.getElementById('state').innerText = 'Finished';
  });
});

mocha.checkLeaks();
mocha.run();