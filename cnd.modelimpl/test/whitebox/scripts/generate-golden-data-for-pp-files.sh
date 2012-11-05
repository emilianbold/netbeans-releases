#!/bin/bash

#
# Copyright (c) 2009, 2012, Oracle and/or its affiliates. All rights reserved.
#

function main() {

    java_home="/usr/bin"
    elsa_home="/export/home/nk220367/elsa/elsa"
    gcc_home="/usr/sfw/bin"
    elsa_result_analyzer_home="/export/home/nk220367/main/cnd.modelimpl/test/whitebox/elsa-result-analyser"

    ppPath="$1"

    shift

    projName="`basename $1`"
    mainPath="$1"
    params="$1"
    relPath=""
    import=""

	shift

    rm -r elsatemp
    rm -r $projName

    mkdir elsatemp
    mkdir -p $projName/variables
    mkdir -p $projName/functions
    mkdir -p $projName/index

    while [ -n "$1" ]
    do
    	case "$1" in
    	    -J*)
            shift
     		java_home="$1"
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
        relPath=${1#$ppPath/}
        fileDir="`dirname $relPath`"

        mkdir -p elsatemp/$fileDir

        if [[ $relPath =~ ".*\.cpp|.*\.cc|.*\.c\+\+|.*\.cxx|.*\.mm|.*\.C" ]]; then
            echo "$relPath c++"
            ${elsa_home}/ccparse -tr printTypedAST $1 > elsatemp/$relPath
        else
            echo "$relPath c"
            ${elsa_home}/ccparse -tr c_lang,printTypedAST $1 > elsatemp/$relPath
        fi

#        echo ${relPath}

		params="${params} elsatemp/${relPath}"
    	shift
    done

    ${java_home}/java -jar "${elsa_result_analyzer_home}/dist/elsa-result-analyser.jar" ${params}
}

main $@
