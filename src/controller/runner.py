# Copyright (C) <2019> Intel Corporation
#
# SPDX-License-Identifier: Apache-2.0

'''IATF Test Runner base class

It's the base class for all test runners on different platforms.

'''

from threading import Thread

def create_runner(type, context):
    if type.lower() != 'javascript':
        raise TypeError('Only JavaScript runner is supported.')
    from runners import javascriptrunner
    return javascriptrunner.JavaScriptRunner(context)


class Runner(object):
    def __init__(self):
        raise Exception(
            'Initiazing Runner is not allowed. Please use a specific Runner instead.')

    def setup(self):
        '''Setup testing environment.'''
        pass

    def run(self):
        '''Start to run tests.'''
        pass

    def teardown(self):
        '''Completing testing. Free all resources.'''
        pass
