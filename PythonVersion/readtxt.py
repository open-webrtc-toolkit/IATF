#!/usr/bin/python
# -*- coding: utf-8 -*-
import os,re
import subprocess
import sys, getopt, time
mode = ''
devices =''
try:
   opts, args = getopt.getopt(sys.argv[1:],"h:m:d:",["help","mode=",'devices='])
except getopt.GetoptError:
   print 'error : run.py -p <parmater> -m <mode> -d <devices>'
   sys.exit(2)
for opt, arg in opts:
   if opt in ("-h", "--help"):
      print 'run.py -p <parmater> -m <mode> -d <devices>\n '
      sys.exit()
   elif opt == '-m':
      mode = arg
      print mode
   elif opt == '-d':
      devices = arg
      print devices
   else:
      assert False, "unhandled option"
if mode == ''or devices == '':
   print 'Error !!! \n Please use: run.py -d <devices> -m <mode> \n '
   sys.exit()



def ReadJsTxt(file):
	dict_js={}
	js_fps = []
	js_video_delay =[]
	if os.path.exists(file):
		for line in open(file).readlines():
			if 'video_fps' in line:
				js_fps.append(line.split(":")[1].strip('\n'))
			if 'video_delay' in line:
				js_video_delay.append(line.split(":")[1].strip('\n'))
	if len(js_fps) != 0:
		dict_js['video_fps'] = js_fps
	if len(js_video_delay) != 0:
		dict_js['video_delay'] = js_video_delay
	return dict_js
def ReadAndroidTxt(file):
	dict_android={}
	android_fps = []
	android_video_delay =[]
	if os.path.exists(file):
		for line in open(file).readlines():
			if 'video_fps' in line:
				android_fps.append(line.split(":")[1].strip('\n'))
			if 'video_delay' in line:
				android_video_delay.append(line.split(":")[1].strip('\n'))	
	if len(android_fps) != 0:
		dict_android['video_fps'] = android_fps
	if len(android_video_delay) != 0:
		dict_android['video_delay'] = android_video_delay
	return dict_android


def ReadTxt(device_detial,mode_list):
	basefilepath = ''
	dict_devices={}
	fps = []
	video_delay =[]
	googFramerateSent = []
	googFrameWidthSent = []
	googFrameHeightSent = []
	googFramerateOutput = []
	CPUusage= []
	Memoryusage = []
	isrecord = {'video_fps': False,'video_delay':False,'googFramerateSent':False,'googFrameWidthSent':False,'googFrameHeightSent':False,
					'googFramerateOutput':False,'CPUusage':False,'Memoryusage':False
				}
	for mode in mode_list:
		isrecord[mode] = True
	
	if os.path.exists(r'report/'+device_detial+'.txt'):
		for line in open(r'report/'+device_detial+'.txt').readlines():
			if isrecord['video_fps'] and 'video_fps' in line:
				fps.append(line.split(":")[1].strip('\n'))
			if isrecord['video_delay'] and 'video_delay' in line:
				video_delay.append(line.split(":")[1].strip('\n'))
			if isrecord['googFramerateSent'] and'googFramerateSent' in line:
				googFramerateSent.append(line.split(":")[1].strip('\n'))
			if isrecord['video_delay'] and 'googFrameWidthSent' in line:
				googFrameWidthSent.append(line.split(":")[1].strip('\n'))
			if isrecord['googFrameHeightSent'] and 'googFrameHeightSent' in line:
				googFrameHeightSent.append(line.split(":")[1].strip('\n'))
			if isrecord['googFramerateOutput'] and 'googFramerateOutput' in line:
				googFramerateOutput.append(line.split(":")[1].strip('\n'))

	if isrecord['CPUusage']:
		CPUusage = ReadCPU(r'report/'+device_detial+'_CPU.txt')
	if isrecord['Memoryusage']:
		Memoryusage = ReadCPU(r'report/'+device_detial+'_CPU.txt')
	if len(fps) != 0:
		dict_devices['video_fps'] = fps
	if len(video_delay) != 0:
		dict_devices['video_delay'] = video_delay
	if len(googFramerateSent) != 0:
		dict_devices['googFramerateSent'] = googFramerateSent
	if len(googFrameWidthSent) != 0:
		dict_devices['googFrameWidthSent'] = googFrameWidthSent
	if len(googFrameHeightSent) != 0:
		dict_devices['googFrameHeightSent'] = googFrameHeightSent
	if len(googFramerateOutput) != 0:
		dict_devices['googFramerateOutput'] = googFramerateOutput
	if len(CPUusage) != 0:
		dict_devices['CPUusage'] = CPUusage
	if len(Memoryusage) != 0:
		dict_devices['Memoryusage'] = Memoryusage
	return dict_devices


def ReadCPU(file):
	cpu =[]
	if os.path.exists(file):
		for line in open(file).readlines():
			lines = re.split('\\s+',line)
			print lines
			if lines[0] == '':
				cpu.append(lines[3].strip('%'))
			else:
				cpu.append(lines[2].strip('%'))
	print '------------------------------'
	print cpu
	return cpu


def ReadMemory(file):
	memory = []
	if os.path.exists(file):
		for line in open(file).readlines():
			lines = re.split('\\s+',line)
			print lines
			if lines[0] == '':
				memory.append(lines[7])
			else:
				memory.append(lines[6])
	return memory

if __name__ =='__main__':
	parmater = ''
	devices_result = {}
	mode_list = mode.split("/")
	devices_list = devices.split("/")
	for device_detial in devices_list:
			devices_result[device_detial]= ReadTxt(device_detial, mode_list)
	print devices_result
	for mode_value in mode_list:
		try:
			parmater =''
			device=''
			for devices_key in devices_result:
				devices_value = devices_result[devices_key]
				if mode_value in devices_value:
					parmater = parmater + ",".join(devices_value[mode_value])+"/"
					device = device + devices_key+ "/"
			parmater = parmater[0:len(parmater)-1]
			device = device[0:len(device)-1]
			print parmater
			print device
			if parmater != '' and device != '':
				pid = subprocess.Popen('python getpng.py -m '+mode_value+ ' -d '+ device+ ' -p '+parmater , shell=True)
				pid.wait()
		except Exception,e:  
			print Exception,":",e
