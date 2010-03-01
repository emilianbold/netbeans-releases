#!/bin/sh

PROG=`basename "$0"`
USAGE="usage: ${PROG} [-h host] [-b bitness] [options to gmake]"

BITNESS_PARAM=no

while getopts h:b: opt; do
  case $opt in
    h) RHOST=$OPTARG
       ;;
    b) BITNESS_PARAM=$OPTARG
       ;;
  esac
done

shift `expr $OPTIND - 1`

if [ "$RHOST" = "" ]; then
   PREFIX=""
else 
   PREFIX="ssh $RHOST"
fi

LHOST=`uname -n`
PWD=`pwd`

$PREFIX sh -s << EOF
OS=\`uname -s\`
OSFAMILY=
CPUTYPE=\`uname -p\`
BITNESS=32

if [ "\${CPUTYPE}" = "unknown" ]; then
   CPUTYPE=\`uname -m\`
fi

if [ "\${OS}" = "SunOS" ]; then
   BITNESS=\`isainfo -b\`
   OSFAMILY="SunOS"
else
   uname -a | egrep "x86_64|WOW64" >/dev/null
   if [ \$? -eq 0 ]; then
      BITNESS=64
   fi
fi

OSFAMILY=\${OSFAMILY:-\`echo \${OS} | grep _NT- >/dev/null && echo Windows\`}
OSFAMILY=\${OSFAMILY:-\`test "\$OS" = "Darwin" && echo MacOSX\`}
OSFAMILY=\${OSFAMILY:-\`test "\$OS" = "Linux" && echo Linux\`}
OSFAMILY=\${OSFAMILY:-\${OS}}

CPUFAMILY=\`(echo \${CPUTYPE} | egrep "^i|x86_64|athlon|Intel" >/dev/null && echo x86) || echo \${CPUTYPE}\`

if [ "$BITNESS_PARAM" != "no" ]; then
   BITNESS="$BITNESS_PARAM"
fi

PLATFORM=\${OSFAMILY}-\${CPUFAMILY}

if [ "\${BITNESS}" = "64" ]; then
   PLATFORM=\${PLATFORM}_64
fi

echo Platform: \${PLATFORM}
uname -a

MAKE=\`which gmake || which make\`
cd /net/$LHOST/$PWD
\$MAKE PLATFORM_DIR=\${PLATFORM} $@

EOF

