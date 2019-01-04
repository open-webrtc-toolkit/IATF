'''IATF controller

It fetches IATF task from server and start specified test clients.

'''

import argparse
import sys
import requests
import runner
import asyncio
import json
import asyncio
import concurrent.futures


class Context(object):
    def __init__(self, task_id, role, config):
        self.task_id = task_id
        self.role = role
        self.config = config


def _request_task_info(server, task_id, verify):
    '''Request configuration from IATF server.'''
    response = requests.get(server+'/rest/tasks/'+task_id, verify=verify)
    return response.json()


async def _start_runners(runners):
    loop = asyncio.get_running_loop()
    futures = [loop.run_in_executor(None, runner.run) for runner in runners]
    await asyncio.gather(*futures)


def main():
    parser = argparse.ArgumentParser(description='IATF controller.')
    parser.add_argument('--no_ssl_verification', help="Verify server certificate.",
                        default=True, action='store_false', dest='verify')
    required_arguments = parser.add_argument_group('required arguments')
    required_arguments.add_argument(
        '--server', help='IATF server address.', required=True)
    required_arguments.add_argument('--task', help='Task ID.', required=True)
    opts = parser.parse_args()
    task_info = _request_task_info(opts.server, opts.task, opts.verify)
    runners = (runner.create_runner(role['type'], Context(
        opts.task, role['name'], role['config'])) for role in task_info['roles'])
    asyncio.run(_start_runners(runners))


if __name__ == '__main__':
    sys.exit(main())
