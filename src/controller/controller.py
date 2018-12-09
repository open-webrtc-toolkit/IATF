'''IATF controller

It fetches IATF task from server and start specified test clients.

'''

import argparse
import sys
import requests
import runner
import asyncio
from runners import javascriptrunner


def _requestConfig(server, task_id, verify):
    '''Request configuration from IATF server.'''
    response = requests.get(server+'/rest/tasks/'+task_id, verify=verify)
    print(response.json())

async def _startRunner(type, config):
    js_runner=javascriptrunner.JavaScriptRunner(None)
    js_runner.run()

async def _startRunners():
    await asyncio.gather(_startRunner(None, None), _startRunner(None, None))

def main():
    parser = argparse.ArgumentParser(description='IATF controller.')
    parser.add_argument('--no_ssl_verification', help="Verify server certificate.",
                        default=True, action='store_false', dest='verify')
    required_arguments = parser.add_argument_group('required arguments')
    required_arguments.add_argument(
        '--server', help='IATF server address.', required=True)
    required_arguments.add_argument('--task', help='Task ID.', required=True)
    opts = parser.parse_args()
    #requestConfig(opts.server, opts.task, opts.verify)
    js_runner1 = javascriptrunner.JavaScriptRunner(None)
    js_runner2 = javascriptrunner.JavaScriptRunner(None)
    asyncio.run(_startRunners())

if __name__ == '__main__':
    sys.exit(main())