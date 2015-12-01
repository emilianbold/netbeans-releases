64BITS=64

sh build.sh COMPILER=~/demo/sources/solarisstudiodev/bin/cc CFLAGS_EXTRA="-xinstrument=datarace" CF_COMMON=""

cd pty
gmake CC=~/demo/sources/solarisstudiodev/bin/cc CFLAGS_EXTRA="-xinstrument=datarace" CF_COMMON="" 64BITS="64"
cd ..

cd killall
gmake CC=~/demo/sources/solarisstudiodev/bin/cc CFLAGS_EXTRA="-xinstrument=datarace" CF_COMMON="" 64BITS="64"
cd ..

cd unbuffer
gmake CC=~/demo/sources/solarisstudiodev/bin/cc CFLAGS_EXTRA="-xinstrument=datarace" CF_COMMON="" 64BITS="64"
cd ..

BUILD_ALL=buildall/

mkdir -p $BUILD_ALL
find ../release/bin/nativeexecution/ unbuffer/dist/ pty/dist killall/dist -type f -exec cp {} $BUILD_ALL \;