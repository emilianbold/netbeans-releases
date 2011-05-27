#!bash -x

new=`less "$WORKSPACE"/../../../../../build.number`
old=`less "$WORKSPACE"/../../../../../previous.build.number`

less "$WORKSPACE"/../../../../../build.number > "$WORKSPACE"/../../../../../previous.build.number

#if [ "$new" == "$old" ]; then echo "Results for this build already exist!"; exit 1; fi