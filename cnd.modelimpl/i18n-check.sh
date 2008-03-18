#!/bin/sh 
perl ../nbbuild/misc/i18ncheck.pl ../cnd.* ../asm* | grep -v "/test/" | grep -v "cnd.antlr/" | grep -v "generated/"

