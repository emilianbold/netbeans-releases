#/bin/sh -x
echo "### absolute"
cat `pwd`/test/file_1
cat `pwd`/test/subdir/sub_1
echo "### relative"
cat test/file_1
cat test/subdir/sub_1
echo "### cd"
cd test
cat file_1
cat subdir/sub_1
echo "### more cd"
cd subdir
cat sub_1
cd ../..
tmpfile="/tmp/${USER}-rfs-test"
echo "tmp" > ${tmpfile}
cat ${tmpfile}


