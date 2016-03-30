#!/bin/bash
SCRIPT_DIR=`pwd`

#PATHS
DOXY_DIR=$SCRIPT_DIR/doxygen

#CLEAN
#rm -rf $SCRIPT_DIR/html

#GENERATE
cd $DOXY_DIR
doxygen doxygen_tf.conf

#COPY PIC
cd $SCRIPT_DIR
cp -r ./pic/ html/
