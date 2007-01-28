#!/bin/sh
#
#  This deploys the sample ejbs
#  It's called from the installer in the post-install step.
#

PRG="$0"

SCRIPT_HOME=`dirname "$PRG"`
# echo $SCRIPT_HOME

$SCRIPT_HOME/../@CLUSTER_DIR@/startup/bin/deploy-ejbs.sh "$@"
