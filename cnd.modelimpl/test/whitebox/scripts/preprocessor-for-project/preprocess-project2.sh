#!/bin/bash

function main() {

    projName="`basename $1`"
    currentDir="`pwd`"

    rm -r $projName/pptemp
    mkdir -p $projName/pptemp

    export MY_PP_TEMP=${currentDir}/$projName/pptemp
    export MY_PP_TARGET=$1

	echo "Adding ${TEST_PATH} to PATH"
    TEST_PATH="$currentDir"

	export PATH=${TEST_PATH}:${PATH}

    echo "$PATH"

    echo "Running gmake"
    echo ""

    cd $1

    make clean
    make   
}

main $@
