#! /bin/sh

# The contents of this file are subject to the terms of the Common Development
# and Distribution License (the License). You may not use this file except in
# compliance with the License.
#
# You can obtain a copy of the License at http://www.netbeans.org/cddl.html
# or http://www.netbeans.org/cddl.txt.
#
# When distributing Covered Code, include this CDDL Header Notice in each file
# and include the License file at http://www.netbeans.org/cddl.txt.
# If applicable, add the following below the CDDL Header, with the fields
# enclosed by brackets [] replaced by your own identifying information:
# "Portions Copyrighted [year] [name of copyright owner]"
#
# The Original Software is NetBeans. The Initial Developer of the Original
# Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
# Microsystems, Inc. All Rights Reserved.

if [ -x /usr/ucb/echo ]
then
    # Solaris' echo doesn't support the -n option, so use an alaternate echo
    ECHO=/usr/ucb/echo
else
    ECHO=echo
fi

prompt="$1"
pgm="$2"
shift 2

# Ensure we work for users who don't have "." in their path.
case "$pgm" in
/*|./*|[a-zA-Z]:/*) ;;
*)  pgm="./$pgm" ;;
esac

"$pgm" "$@"
rc=$?

if [ -n "$NBCND_RC" ]
then
    echo $rc > "$NBCND_RC"
fi

$ECHO -n "$prompt"
read a
exit $rc
