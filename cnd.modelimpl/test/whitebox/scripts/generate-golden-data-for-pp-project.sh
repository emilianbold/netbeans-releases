#!/bin/bash

#
# Copyright (c) 2009, 2012, Oracle and/or its affiliates. All rights reserved.
#

function main() {

    files="`find $1 -name '*.cc' -o -name '*.cpp' -o -name '*.c++' -o -name '*.cxx' -o -name '*.c' -o -name '*.C' -o -name '*.mm'`"

    echo "Golden data generation for:"
    echo "$files"

    echo "$@"

    bash `dirname $0`/generate-golden-data-for-pp-files.sh "$@ $files"
}

main $@
