#!/bin/bash

function scp_from_one_host() {
  HOST=$1
  SRC=$2
  DST=$3
  OPTIONS=$4
  if [ -z ${HOST} ]; then 
    echo "host is not set"
    return
  fi
  if [ -z ${DST} ]; then 
    echo "destination is not set"
    return
  fi
  if [ -z ${SRC} ]; then 
    echo "source is not set"
    return
  fi
  
  CMD="scp -r ${OPTIONS} ${HOST}:${SRC} ${DST}/"
  echo $CMD
  eval $CMD
  if [ $? -eq 0 ]; then
    echo "OK"
  else
    echo "Error"
  fi
}

if [ -z "$NB" ]; then
  echo "NB environmane variable is not set"
  exit 2
fi
BASE=$NB/cnd.remote
if [ ! -d "${BASE}" ]; then
  echo "Directory ${BASE} does not exist"
  exit 2
fi

DAT=${NB}/cnd.remote/tools/nbproject/private/download-binaries.dat
if [ ! -r "${DAT}" ]; then
  echo "Can not read ${DAT}"
  exit 2
fi

DST=${BASE}/release/bin
echo "Going to copy rfs_* binaries to ${DST}"
echo "  using hosts and directories specified in"
echo "  ${DAT}"
echo "Press enter to continue or ^C to cancel"
read t

echo
echo "Copying rfs_* binaries to ${DST} ..."
mkdir -p ${DST}

while read -r L
do
  if [ ! -z "$L" ]; then
    if [ ! "`expr substr "$L" 1 1`" = "#" ]; then    
      HOST=`echo $L | awk '{ print $1 }'`
      SRC=`echo $L | awk '{ print $2 }'`
      OPT=`echo $L | awk '{ print $3 $4 $5 $6 }'`
      echo
      echo "Copying from ${HOST} ..."
      scp_from_one_host ${HOST} ${SRC} ${DST} ${OPT}
    fi
  fi  
done < ${DAT}
