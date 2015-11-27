#!/bin/bash 

function increment_idx() {
    req_idx=`expr $req_idx + 1`
}

#
# Parameters:
# 1) directory to iterate
# 2) output file
# 3) command
function make_requests() {

    # directory to iterate recursively
    R=$1
    if [ ! -d $R ]; then
      echo $R is not a directory
      exit 2
    fi
    R=`(cd $R; pwd)`

    # output file
    O=$2

    # fs_server command
    C=$3

    for D in `find $R -type d`; do increment_idx; echo "${C} $req_idx ${#D} $D"  >> $O; done
}

# Possible types of requests:
# l - FS_REQ_LS
# r - FS_REQ_RECURSIVE_LS
# S - FS_REQ_STAT
# s - FS_REQ_LSTAT
# C - FS_REQ_COPY
# m - FS_REQ_MOVE
# q - FS_REQ_QUIT
# P - FS_REQ_SLEEP
# W - FS_REQ_ADD_WATCH
# w - FS_REQ_REMOVE_WATCH
# R - FS_REQ_REFRESH
# d - FS_REQ_DELETE
# i - FS_REQ_SERVER_INFO
# o - FS_REQ_OPTION
# ? - FS_REQ_HELP

basedir=`dirname $0`

echo "changing current directory to ${basedir}"
cd ${basedir}

req_file="/tmp/dynamic_fs_server_test.req"
echo "preparing requests and writing them into ${req_file}"
rm -rf ${req_file}
req_idx=0

make_requests ../../.. ${req_file} l

for x in 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16; do
    increment_idx
    echo "P ${req_idx} 5" >> ${req_file}
done

make_requests ../.. ${req_file} r
make_requests ../.. ${req_file} s
make_requests ../.. ${req_file} S

#
# now make requests for copy, move and delete
#

tmpdir=`mktemp -d`
echo "temporary dir: ${tmpdir}"
src=${tmpdir}/src
copy_dst=${tmpdir}/copy_dst
move_dst=${tmpdir}/move_dst
mkdir ${src}
cp -r ../../* ${src}/

increment_idx
echo "C ${req_idx} 0 ${src} 0 ${copy_dst}" >> ${req_file}

increment_idx
echo "m ${req_idx} 0 ${src} 0 ${move_dst}" >> ${req_file}

# the previous one was erroneous since the fs_server can only move plain files, not directories
# let's now add correct one
increment_idx
plain_file=${tmpdir}/plain_file
echo "123" > ${plain_file}
echo "m ${req_idx} 0 ${plain_file} 0 ${move_dst}" >> ${req_file}

increment_idx
echo "d ${req_idx} 0 ${copy_dst}" >> ${req_file}

increment_idx
echo "d ${req_idx} 0 ${src}" >> ${req_file}

increment_idx
echo "d ${req_idx} 0 ${tmpdir}" >> ${req_file}

increment_idx
make_requests ../../.. ${req_file} r

echo "i 0" >> ${req_file}
echo "o 0 0 access=fast" >> ${req_file}
echo "o 0 0 access=full" >> ${req_file}

echo "q" >> ${req_file}

#echo "printing ${req_file}"
#cat ${req_file}

arc=`arch`
if [ "${arc}" = "sun4" ]; then
    arc="sparc"
else
    if [ "${arc}" = "x86_64" ]; then
        arc="x86"
    else
        echo "Architecture ${arc} is not supported for this test"
        exit 4
    fi
fi

os=`uname -s`

echo "launching fs_server and feeding it with commands from ${req_file}"

cat ${req_file} | ../../../release/bin/${os}-${arc}/fs_server -t 16 -p -l -s -d /tmp/fs_server_test_cache_${USER} > /dev/null

#just in case remove temp directory
if [ -d ${tmpdir} ]; then
    echo "Something went wrong: fs_server should have already removed ${tmpdir}"
    echo "Don'w worry, we'll now remove it."
    rm -rf ${tmpdir}
    echo "Removed"
fi
