#!/bin/sh

if [ ! -z "$XTEST_MAIL_FROM" -a ! -z "$XTEST_MAIL_TO" -a ! -z "$XTEST_MAILHOST" ] ; then
  ant -buildfile mail.xml -Dxtest.mail.from="$XTEST_MAIL_FROM" -Dxtest.mail.to="$XTEST_MAIL_TO" \
      -Dxtest.mail.mailhost="$XTEST_MAILHOST" -Dxtest.mail.subject="$1" -Dxtest.mail.message="$2"
fi