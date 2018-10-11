#!/usr/bin/python
import os
import sys
import subprocess
import com.util as util
def createEnv():
	if not os.path.exists(sys.argv[1]):
		os.mkdir(sys.argv[1])
	else:
		pass
		#util.runcmd("rm ")

def kill_node():
    kill_node=subprocess.Popen('ps aux | grep \'node.py\' | grep -v \'grep\' | awk \'{print $2}\'|xargs kill -9 >/dev/null 2>&1', shell=True)
    kill_node.wait()
    print "clean success"

createEnv()
kill_node()