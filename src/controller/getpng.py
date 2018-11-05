#!/usr/bin/python
# -*- coding: utf-8 -*-

import sys, getopt, time
import numpy as np
import matplotlib.pyplot as pl
from matplotlib.ticker import MultipleLocator, FormatStrFormatter

ax1 =''
ax2 =''
parmater = ''
parmater_list =[]
mode=''
devices= ''
devices_list =[]
color=['r','b','g','y','k','m']
try:
   opts, args = getopt.getopt(sys.argv[1:],"h:p:m:d:",["help", "parmater=","mode=",'devices='])
except getopt.GetoptError:
   print 'error : run.py -p <parmater> -m <mode> -d <devices>'
   sys.exit(2)
for opt, arg in opts:
   if opt in ("-h", "--help"):
      print 'run.py -p <parmater> -m <mode> -d <devices>\n '
      sys.exit()
   elif opt == "-p":
      parmater = arg
      print parmater
   elif opt == '-m':
      mode = arg
      print mode
   elif opt == '-d':
      devices = arg
      print devices
   else:
      assert False, "unhandled option"
if parmater == ''or mode == ''or devices == '':
   print 'Error !!! \n Please use: run.py -p <parmater> -m <mode> \n '
   sys.exit()

def paservalue():
	global parmater_list
	global devices_list
	parmater_list = parmater.split('/')
	devices_list = devices.split('/')
	if len(parmater_list) != len(devices_list): 
		print 'parmater_list len is not equal to devices_list len'
		sys.exit()



def choosepl(pl,ax,title):
	pl.sca(ax)
	ax = pl.axes()
	pl.title(title)
	pl.xlabel('run time')
	pl.ylabel('value')
	pl.xlim(0, 30.0)
	pl.ylim(-1000, 1000)
	ymajor = MultipleLocator(100)
	xmajor = MultipleLocator(5)
	ymajorformatter = 	FormatStrFormatter('%3.1f')
	ax.yaxis.set_major_locator(ymajor)
	ax.xaxis.set_major_locator(xmajor)
	ax.yaxis.set_major_formatter(ymajorformatter)
	ax.spines['top'].set_color('none')
	ax.spines['right'].set_color('none')
	ax.xaxis.set_ticks_position('bottom')
	ax.spines['bottom'].set_position(('data',0))
	ax.yaxis.set_ticks_position('left')
	ax.spines['left'].set_position(('data',0))
	return pl

def setvalue(pl,listx,listy,color):
	plot, = pl.plot(listx,listy,color)
	return plot

def createplot(title,xmax,ymax,xmajorvalue,ymajorvalue):
	pl.figure(figsize=(100,100)) # 创建图表2
	ax = pl.axes()
	pl.title(title)
	pl.xlabel('run time')
	pl.ylabel('value')
	pl.xlim(0, xmax)
	pl.ylim(0, ymax)
	ymajor = MultipleLocator(ymajorvalue)
	xmajor = MultipleLocator(xmajorvalue)
	ymajorformatter = 	FormatStrFormatter('%3.1f')
	ax.yaxis.set_major_locator(ymajor)
	ax.xaxis.set_major_locator(xmajor)
	ax.yaxis.set_major_formatter(ymajorformatter)
	# ax.spines['top'].set_color('none')
	# ax.spines['right'].set_color('none')
	# ax.xaxis.set_ticks_position('bottom')
	# ax.spines['bottom'].set_position(('data',0))
	# ax.yaxis.set_ticks_position('left')
	# ax.spines['left'].set_position(('data',0))
	# global ax1
	# global ax2
	# ax1 = pl.subplot(221) # 在图表2中创建子图1
	# ax2 = pl.subplot(222) # 在图表2中创建子图2
	return pl

def save(pl,name):
	fig = pl.gcf()
	fig.set_size_inches(10,10)
	fig.savefig('report/'+name,dpi=100)

if __name__ == '__main__':
	ymax = 0
	ymajorvalue = 0
	maxparma_list = []
	maxlen = []
	paservalue()
	for parma in parmater_list:
		parma_list = parma.split(',')
		parma_arr = np.array(parma_list)
		parma_arr[parma_arr<0] = 0
		parma_arr = parma_arr.astype(np.int32)
		parma_list = list(parma_arr)
		print parma_list
		print max(parma_list)
		maxparma_list.append(max(parma_list))
		maxlen.append(len(parma_list))
	print maxparma_list
	ymax = int(int(max(maxparma_list))*1.2)
	ymax_str = str(ymax)
	print ymax_str
	ymax = int((int(ymax_str[0])+1)*pow(10,len(ymax_str)-1))
	ymajorvalue = int(ymax/10)
	print ymax
	print "maxlen:"+str(maxlen)
	print max(maxlen)
	# if mode == 'video_fps':
	# 	ymax = 40
	# 	ymajorvalue = 10
	# if mode == 'video_delay':
	# 	ymax = 400
	# 	ymajorvalue = 40
	# if mode == 'googFramerateSent':
	# 	ymax = 400
	# 	ymajorvalue = 40
	# if mode == 'googFrameWidthSent':
	# 	ymax = 400
	# 	ymajorvalue = 40
	# if mode == 'googFrameHeightSent':
	# 	ymax = 400
	# 	ymajorvalue = 40
	# if mode == 'googFramerateOutput':
	# 	ymax = 400
	# 	ymajorvalue = 40
	if mode == 'CPUusage':
	 	ymax = 100
	 	ymajorvalue = 10
	pl = createplot(mode,max(maxlen),ymax,10,ymajorvalue)
	plot=[]
	color_index = 0
	for value in parmater_list:
		value_list = value.split(',')
		len_value = len(value_list)
		plot.append(setvalue(pl,range(len_value),value_list,color[color_index]))
		color_index = color_index+1

	#print devices_list
	#print parmater_list
	pl.legend(plot,devices_list)
	# pl2 = choosepl(pl,ax2,'tile1')
	# plot3 = setvalue(pl2,range(20),y1,'r')
	# pl2.legend([plot3],['aaaaaa'])
	save(pl,mode)
	#pl.show()