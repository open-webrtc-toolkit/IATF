#!/usr/bin/python
'''
Created on Jan,16 2016

@author: Yanbin

this file was not used for current version, result anlysis is done by android scripts
'''
import sys, getopt, time
from socketIO_client import SocketIO, LoggingNamespace
#from socketConnect.socketioconnect import socketioconnect as a
from subprocess import call
print 'Number of arguments:', len(sys.argv), 'arguments.'
print 'Argument List:', str(sys.argv)


def ReadFile(inputfile):

   print 'Input file is "', inputfile
   read_caselist(inputfile)
def read_caselist(filename):
    lines = [line.rstrip('\n') for line in open(filename)]
    print 'line[0] is', lines[0]
    casename, caseresult = lines[0].split(" ");
    print "casename is", casename;
    print "caseresult is", caseresult;


