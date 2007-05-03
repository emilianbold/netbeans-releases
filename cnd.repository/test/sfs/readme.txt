#
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
# Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
# Microsystems, Inc. All Rights Reserved.
#

Repository storage tests
------------------------

run-correctness.sh	tests repository storage correctness
run-threading.sh	tests repository storage threading

To run tests, one should set NBDIST environment variable first.
It should point to NetBeans version 5.5 or 5.5.1 installation.

Syntax:

run-correctness.sh  <directory>
run-threading.sh <directory>

where <directory> is a directory that is used to fill test data.
Directory is recursed, and a test data element is created for each file.


