#!/bin/bash
src_host=10.239.44.8
src_pwd=intel123
src_path=/home/webrtc/a.txt
src_user=webrtc
expect -c "
spawn scp $1@$2:$3 $4
expect \"password:\"
send \"$5\r\"
expect eof
"
