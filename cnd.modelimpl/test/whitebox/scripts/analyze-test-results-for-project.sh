#!/bin/bash

#
# Copyright (c) 2009, 2012, Oracle and/or its affiliates. All rights reserved.
#

function main() {

    java_home="/usr/bin"
    test_result_analyzer_home="/export/home/nk220367/projects/code-completion-test-result-analyzer"

    files="`find $1 -name '*.xml'`"

    project_dir="$2"
    index_dir="$3"


    while [ -n "$1" ]
    do
    	case "$1" in
    	    -J*)
            shift
     		java_home="$1"
     		shift
            continue
    		;;

    	    -A*)
            shift
     		test_result_analyzer_home="$1"
     		shift
            continue
    		;;
    	esac

        shift
    done


#    echo "Analyzing:"
#    echo "$files"

    java -jar "${test_result_analyzer_home}/dist/code-completion-test-result-analyzer.jar" $project_dir $index_dir $files
}

main $@
