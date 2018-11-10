const expect = chai.expect;
const socket = io('https://localhost:9000');

describe('Interactivity tests', () => {
  before(() => {
  });
  describe('Basic connection tests', () => {
    it('Each endpoint sends a message should success.', () => {
      socket.emit('test');
    });
  });
});

mocha.checkLeaks();
mocha.run();