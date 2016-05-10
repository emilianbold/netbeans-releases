#!/bin/bash

function usage() {
    short_name=$1
    echo "$short_name -"
    echo "    prints messages to stderr and stdout in cycle"
    echo "Usage:"
    echo "    $short_name -c <cycles> -e <err_msg_len> -o <out_msag_len>"
    exit 1
}

function message() {
    i=$1
    len=`expr $2 - 1` # reserve 1 position for '\n'
    err_or_out=$3
    format="%-${len}d\n"
    if [ "$err_or_out" = "out" ]; then
        printf "$format" $i | tr ' ' '#'
    else
        if [ "$err_or_out" = "err" ]; then
            printf "$format" $i | tr ' ' '#' >&2
        else
            exit 8
        fi
    fi
}


my_short_name=`basename $0`

#usage $my_short_name

cnt=0
err_len=0
out_len=0

while [ -n "$1" ]
do
    case "$1" in
        -c)
            cnt=$2
            shift
            ;;
        -e)
            err_len=$2
            shift
            ;;
        -o)
            out_len=$2
            shift
            ;;
        *)
            usage $my_short_name
            ;;
    esac
    shift
done

if [ $cnt = 0 ]; then
    echo cycles can not be zero
    usage $my_short_name
fi
if [ $err_len = 0 -a  $out_len = 0 ]; then
    echo both error and out messages length can not be zero
    usage $my_short_name
fi

i=0
while [ $i -lt $cnt ]; do
    #echo $i
    i=`expr $i + 1`
    if [ $out_len != 0 ]; then
	message $i $out_len "out"
    fi
    if [ $err_len != 0 ]; then
	message $i $err_len "err"
    fi
done
