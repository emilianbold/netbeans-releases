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


