#!/bin/sh

if [ ! -z "$XTEST_MAIL_FROM" -a ! -z "$XTEST_MAIL_TO" -a ! -z "$XTEST_MAILHOST" ] ; then
  OLDPATH=$PATH
  case "`uname`" in
     CYGWIN*) PATH=`cygpath -u "${JDK13_HOME}"`/bin:`cygpath -u "${ANT_HOME}"`/bin:$PATH ;;
     *)       PATH=${JDK13_HOME}/bin:${ANT_HOME}/bin:$PATH ;;
  esac
  JAVA_HOME=$JDK13_HOME
  export JAVA_HOME
  ant -buildfile ${XTEST_SERVER_HOME}/bin/mail.xml -Dxtest.mail.from="$XTEST_MAIL_FROM" -Dxtest.mail.to="$XTEST_MAIL_TO" \
      -Dxtest.mail.mailhost="$XTEST_MAILHOST" -Dxtest.mail.subject="$1" -Dxtest.mail.message="$2" \
      -Dxtest.mail.subject.prefix="$XTEST_MAIL_SUBJECT_PREFIX"
  PATH=$OLDPATH
fi