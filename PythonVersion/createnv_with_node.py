#!/usr/bin/python
import os
import sys
import com.util as util
def createEnv():
	print sys.argv[1]
	if not os.path.exists(sys.argv[1]):
		os.mkdir(sys.argv[1])
	else:
		util.runcmd('rm '+sys.argv[1]+'/*')

createEnv()