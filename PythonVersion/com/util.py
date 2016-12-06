#!/usr/bin/python
import subprocess
def getcomputerIp(ipaddress):
	   ip=subprocess.Popen('ifconfig',stdout=subprocess.PIPE,shell=True)
	   ip.wait()
	   out,err = ip.communicate()
	   if ipaddress in out:
	   		return True
	   else:
	   		return False

def runcmd(cmd):
	   pid=subprocess.Popen(cmd,shell=True)
	   pid.wait()

# if __name__ == '__main__':
# 	getcomputerIp("10.239.44.86")
