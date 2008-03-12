#!/bin/sh 
perl ../nbbuild/misc/i18ncheck.pl ../cnd.* | grep -v "/test/" | grep -v "cnd.antlr/" | grep -v "generated/"

