#!/bin/bash

ant -Dismp.home=$IS_HOME -Dcluster.dir=$SONYERICSSON_CDC_PATH -Dis.debug=1 build-unix $*
