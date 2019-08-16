import subprocess
import os
import sys
from runner import Runner


class AndroidRuner(Runner):
    def __init__(self, context):
        self.source_path = context.config.get('sourcePath', None)
        self.test_class = context.config.get('testClass', None)
        self.device = context.config.get('device', None)
        self.task_id = context.task_id
        self.role = context.role
        self.package = context.config.get('package', None)
        self.test_runner = context.config.get('testRunner', None)

    def setup(self):
        pass

    def teardown(self):
        pass

    def run(self):
        if not os.path.exists(self.source_path):
            print('Error android source path.')
            return

        t_class = self.package + '.' + self.test_class

        self.test_runner = 'android.support.test.runner.AndroidJUnitRunner' if self.test_runner is None else self.test_runner

        cmd = ['adb', '-s', self.device, 'shell', 'am', 'instrument', '-w', '-r', '-e', 'taskId', self.task_id, '-e',
               'class', t_class, self.package + '.test/' + self.test_runner]

        with open(os.path.join(self.source_path, self.device+".log"), 'w') as f:
            proc = subprocess.Popen(cmd, cwd=self.source_path, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
            for line in proc.stdout:
                line = line.decode("utf-8")
                sys.stdout.write(line)
                f.write(line)
            proc.communicate()
