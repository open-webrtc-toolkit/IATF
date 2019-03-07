import requests
from requests.packages import urllib3
requests.packages.urllib3.disable_warnings()
import json
import os
import subprocess
import argparse

THIS_PATH = os.path.abspath(os.path.dirname(__file__))
TASK = json.loads(
    open(os.path.join(THIS_PATH, 'task.json'), 'r').read())


def do_request(url, method='GET', data=None, headers=None):
    response = None
    if method == 'GET':
        response = requests.get(url, verify=False)
    elif method == 'POST':
        response = requests.post(url, data=data, headers=headers, verify=False)
    elif method == 'PUT':
        response = requests.put(url, data=data, headers=headers, verify=False)
    if response.status_code != 200:
        print(response.status_code)
        print("error", response.text)
    else:
        return response.text


def start(id, server, verify):
    cmd = ['python3.7', os.path.abspath(os.path.join(THIS_PATH, 'controller/controller.py')), '--server', server,
           '--task', id]
    if not verify:
        cmd = cmd + ['--no_ssl_verification']
    print(cmd)
    subprocess.Popen(cmd).communicate()


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='IATF controller.')
    parser.add_argument('--no_ssl_verification', help="Verify server certificate.",
                        default=True, action='store_false', dest='verify')
    required_arguments = parser.add_argument_group('required arguments')
    required_arguments.add_argument(
        '--server', help='IATF server address.', required=True)
    required_arguments.add_argument('--task', help='Task ID.', default=None)
    opts = parser.parse_args()
    task_id = opts.task

    if task_id is None:
        headers = {
            "Content-Type": "application/json"
        }
        task_id = do_request(opts.server + '/rest/v1/tasks', method="PUT", data=json.dumps(TASK),
                             headers=headers)
    print(task_id)
    if task_id is not None:
        start(id=task_id, server=opts.server, verify=opts.verify)
