if [ -z "$XTEST_SERVER_HOME" ] ; then
   XTEST_SERVER_HOME=`dirname $0`/..
   oldpwd=`pwd` ; cd ${XTEST_SERVER_HOME}; XTEST_SERVER_HOME=`pwd`; cd $oldpwd; unset oldpwd
   case "`uname`" in
     CYGWIN*) XTEST_SERVER_HOME=`cygpath -w "${XTEST_SERVER_HOME}" | tr '\\\' '/'` ;;
   esac
   echo "XTEST_SERVER_HOME not set. Guessing it is $XTEST_SERVER_HOME"
   export XTEST_SERVER_HOME
fi

if [ ! -d "$XTEST_SERVER_HOME" ]; then
    echo "XTEST_SERVER_HOME directory '$XTEST_SERVER_HOME' doesn't exist."
    exit 1
fi
if [ ! -d "${XTEST_SERVER_HOME}/bin" ]; then
    echo Directory ${XTEST_SERVER_HOME}/bin not found.
    exit 1
fi
if [ ! -d "${XTEST_SERVER_HOME}/conf" ]; then
    echo Directory ${XTEST_SERVER_HOME}/conf not found.
    exit 1
fi