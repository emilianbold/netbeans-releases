#!/bin/bash

#
# Copyright (c) 2009, 2012, Oracle and/or its affiliates. All rights reserved.
#

function main() {

    java_home="/usr/bin"
    elsa_home="/export/home/nk220367/elsa/elsa"
    gcc_home="/usr/sfw/bin"
    elsa_result_analyzer_home="/export/home/nk220367/main/cnd.modelimpl/test/whitebox/elsa-result-analyser"

    projName="`basename $1`"
    mainPath="$1"
    params="$1"
    relPath=""
    import=""

	shift

    rm -r pptemp
    rm -r elsatemp
    rm -r $projName

    mkdir pptemp
    mkdir elsatemp
    mkdir -p $projName/variables
    mkdir -p $projName/functions
    mkdir -p $projName/index

    while [ -n "$1" ]
    do
    	case "$1" in
    	    -I*)
     		import="${import} $1"
     		shift
            continue
    		;;

    	    -J*)
            shift
     		java_home="$1"
     		shift
            continue
    		;;

    	    -G*)
            shift
     		gcc_home="$1"
     		shift
            continue
    		;;

    	    -E*)
            shift
     		elsa_home="$1"
     		shift
            continue
    		;;

    	    -A*)
            shift
     		elsa_result_analyzer_home="$1"
     		shift
            continue
    		;;
    	esac

        echo "pp and ccparse $1"
        fileName="`basename $1`"
        relPath=${1#$mainPath/}
        fileDir="`dirname $relPath`"

        mkdir -p pptemp/$fileDir
        mkdir -p elsatemp/$fileDir

        ${gcc_home}/gcc  -Wno-deprecated -E $import $1 > pptemp/$relPath

        if [[ $relPath =~ ".*\.cpp|.*\.cc|.*\.c\+\+|.*\.cxx|.*\.mm|.*\.C" ]]; then
            ${elsa_home}/ccparse -tr printTypedAST pptemp/$relPath > elsatemp/$relPath
        else
            ${elsa_home}/ccparse -tr c_lang,printTypedAST pptemp/$relPath > elsatemp/$relPath
        fi


		params="${params} elsatemp/${relPath}"
    	shift
    done

    ${java_home}/java -jar "${elsa_result_analyzer_home}/dist/elsa-result-analyser.jar" ${params}
}

main $@
