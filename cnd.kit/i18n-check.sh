#!/bin/sh
perl ../nbbuild/misc/i18ncheck.pl ../cnd.* ../asm* | grep -v "/test/" | grep -v "cnd.antlr/" | grep -v "generated/"
rc=$?
echo ""
if [ ${rc} -eq 0 ]; then
    echo "I18n check FAILED"
    #exit 4
else
    echo "I18n check SUCCEEDED - no warnings"
    #exit 0
fi
