##########
#getDevices function is use to read current connected android devices, only return authenticated devices.
##########################
#author: yanbin

import os
import time
import datetime
import sys
import getopt
import subprocess
import psutil

number=1
p=psutil.Process(30000);  
try: 
	print p.status()
except Exception, e:
	print e
