'''IATF Test Runner base class

It's the base class for all test runners on different platforms.

'''

from threading import Thread

class Runner:
    def __init__(self):
        raise Exception(
            'Initiazing Runner is not allowed. Please use a specific Runner instead.')

    def setup(self):
        '''Setup testing environment.'''
        pass

    async def run(self):
        '''Start to run tests.'''
        pass

    def teardown(self):
        '''Completing testing. Free all resources.'''
        pass
