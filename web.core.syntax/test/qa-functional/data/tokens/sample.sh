#!/bin/bash
while [ $# -gt 0 ]; do    # Until you run out of parameters . . .
  case "$1" in
    -d|--debug)
              # "-d" or "--debug" parameter?
              DEBUG=1
              ;;
    -c|--conf)
              CONFFILE="$2"
              shift
              if [ ! -f $CONFFILE ]; then
                echo "Error: Supplied file doesn't exist!"
                exit 1     # File not found error.
              fi
              ;;
  esac
  shift       # Check next set of parameters.
done

func1()
{ # This is simple function :)
  echo "This is simple function."
} # Function declaration must precede call.

function func2()
{
  echo "This is another simple function."
}


func1
func2

(( 5 > 4 ))                                      # true
echo "Exit status of \"(( 5 > 4 ))\" is $?."     # 0

MAX=10000
for ((nr=1; nr<$MAX; nr++))
do

    let "t1 = nr % 5"
    if [ "$t1" -ne 3 ];then
      continue
    fi

    let "t2 = nr % 7"
    if [ "$t2" -ne 4 ]
    then
      continue
    fi

    let "t3 = nr % 9"
    if [ "$t3" -ne 5 ]
    then
      continue
    fi

done
echo "Number = $nr"


exit 0
