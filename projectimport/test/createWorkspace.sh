#!/bin/bash
#
# Script: createWorkspace.sh
#
# Convenient script for generating workspace with metadat we needs for tests.
#
# cDate: 2004/12/14
# mDate: 2004/12/14
#
# Author: martin.krauskopf (martin.krauskopf at sun.com)
# Editor: VIM - Vi IMproved 6.3 (2004 June 7, compiled Jun 26 2004 15:03:59)

# =================== You can customize variables ==================== #

# workspace
WORKSPACE="$HOME/workspace-3.0.1"
# space separated project list
PROJECTS="projectInWorkspace projectInWorkspaceWeDependOn"


# ==================================================================== #
# =================== Don't need to tuch following =================== #
# ==================================================================== #

function fail {
  echo "SCRIPT_FAILED: $1"
  exit 1
}

[ -d "$WORKSPACE" ] || fail "$WORKSPACE must exist"
WORKSPACE_PLUGINS=".metadata/.plugins"
[ -d "$WORKSPACE/$WORKSPACE_PLUGINS" ] || fail "$WORKSPACE/$WORKSPACE_PLUGINS must exist"

WORKSPACE_DUMP="$PWD/`dirname $0`/`basename $WORKSPACE`"
mkdir "$WORKSPACE_DUMP" || fail "$WORKSPACE_DUMP cannot be created"

cd "$WORKSPACE"
cp -a --parent "$WORKSPACE_PLUGINS/org.eclipse.core.runtime/.settings/org.eclipse.jdt.launching.prefs" \
    "$WORKSPACE_PLUGINS/org.eclipse.core.runtime/.settings/org.eclipse.jdt.core.prefs" \
    "$WORKSPACE_PLUGINS/org.eclipse.core.resources/.projects" \
    $PROJECTS \
    "$WORKSPACE_DUMP" || fail "Copying failed"

echo "INFO: \"$WORKSPACE_DUMP\" was created on the base of \"$WORKSPACE\""

