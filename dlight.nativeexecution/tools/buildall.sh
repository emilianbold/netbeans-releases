#/bin/bash

if [ "$1" = "--solaris" ]; then
	SOLARIS=1
fi

MAKE=`which gmake || which make`

if [ "x$SOLARIS" != "x" ]; then
	DISCOVER=/path/to/discover
	args='CC=/path/to/cc NOSTRIP=1 CFLAGS_EXTRA=-O3 64BITS=64 CF_COMMON=""'
else
	CC=gcc
	args='64BITS=64'
fi

sh build.sh clean

cd pty
rm -rf dist
cd ..

cd killall
rm -rf dist
cd ..

cd unbuffer
rm -rf dist
cd ..


sh build.sh $args

cd pty
${MAKE} $args
cd ..

cd killall
${MAKE} $args
cd ..

cd unbuffer
${MAKE} $args
cd ..

BUILD_ALL=buildall

mkdir -p $BUILD_ALL
cd $BUILD_ALL
rm -f *
cd ..

find "../release/bin/nativeexecution/" "unbuffer/dist/" "pty/dist" "killall/dist" -not -name "*.sh" -type f -exec cp {} $BUILD_ALL \;

if [ "x$SOLARIS" != "x" ]; then
	find "$BUILD_ALL" -type f -exec sh -c "$DISCOVER"' -v -w $0.%p.txt $0' {} \;
fi

find $BUILD_ALL -type f | xargs file
