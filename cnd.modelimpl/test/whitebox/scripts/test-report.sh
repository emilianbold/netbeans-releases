#!/bin/bash

#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
#
# Oracle and Java are registered trademarks of Oracle and/or its affiliates.
# Other names may be trademarks of their respective owners.
#
# The contents of this file are subject to the terms of either the GNU
# General Public License Version 2 only ("GPL") or the Common
# Development and Distribution License("CDDL") (collectively, the
# "License"). You may not use this file except in compliance with the
# License. You can obtain a copy of the License at
# http://www.netbeans.org/cddl-gplv2.html
# or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
# specific language governing permissions and limitations under the
# License.  When distributing the software, include this License Header
# Notice in each file and include the License file at
# nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
# particular file as subject to the "Classpath" exception as provided
# by Oracle in the GPL Version 2 section of the License file that
# accompanied this code. If applicable, add the following below the
# License Header, with the fields enclosed by brackets [] replaced by
# your own identifying information:
# "Portions Copyrighted [year] [name of copyright owner]"
#
# Contributor(s):
#
# The Original Software is NetBeans. The Initial Developer of the Original
# Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
# Microsystems, Inc. All Rights Reserved.
#
# If you wish your version of this file to be governed by only the CDDL
# or only the GPL Version 2, indicate your decision by adding
# "[Contributor] elects to include this software in this distribution
# under the [CDDL or GPL Version 2] license." If you do not indicate a
# single choice of license, a recipient has the option to distribute
# your version of this file under either the CDDL, the GPL Version 2 or
# to extend the choice of license to its licensees as provided above.
# However, if you add GPL Version 2 code and therefore, elected the GPL
# Version 2 license, then the option applies only if the new code is
# made subject to such option by the copyright holder.
#

report() {
DATE=`date +%m_%d_%y`
#mkdir -p ${DWARF_TEST_TEMP}/${PROJECT_NAME}/${DATE}
    DWARF_HTML_REPORT="${DWARF_TEST_TEMP}/_dwarf_test_report.html"
    DWARF_TEXT_REPORT="${DWARF_TEST_TEMP}/_dwarf_test_report.txt"
    TITLE="Project: `basename ${DWARF_TEST_PROJECT_DIR}`    Date: ${DATE}"

    rm ${DWARF_HTML_REPORT} 2>/dev/null
    echo "<html><head></head><body>" >> ${DWARF_HTML_REPORT}
    echo "<h3>${TITLE}</h3>" >> ${DWARF_HTML_REPORT}
    echo "<table border=1>" >> ${DWARF_HTML_REPORT}
    echo "<tr><td>File</td><td>Dwarf</td><td>Model</td><td>Accuracy, %</td></tr>" >> ${DWARF_HTML_REPORT}
    echo "" >> ${DWARF_HTML_REPORT}
    cat ${DWARF_LOG} | grep "DumpDiff" | sort | awk \
    'BEGIN { \
	cnt=0; sum_total_jdd=0; sum_found_in_csm=0; \
    } \
    { \
	total_jdd=$6; found_in_csm=$7; \
	sum_total_jdd+=total_jdd; \
	sum_found_in_csm+=found_in_csm; \
	if( total_jdd != 0 )
	    perc=(found_in_csm*100)/total_jdd; \
	else \
	    perc=100;\
	print "<tr><td>", $3, "</td><td>" total_jdd, "</td><td>" found_in_csm, "</td><td>" perc "%</td></tr>"; \
	cnt++; \
    } \
    END { \
if (sum_total_jdd != 0 ) {\
	avg=(sum_found_in_csm*100)/sum_total_jdd;
}\
	print "<tr><td><b>AVERAGE</td> <td>" sum_total_jdd "</td> <td>" sum_found_in_csm "</td> <td><b>", avg "%</td></tr>"; \
	print "</table>" ; \
    }' >> ${DWARF_HTML_REPORT}

    echo "</body></html>" | tee -a ${DWARF_HTML_REPORT}
    echo ${TITLE} > ${DWARF_TEXT_REPORT}
    cat ${DWARF_LOG} | grep "DumpDiff" | sort | awk \
    'BEGIN { \
	cnt=0; sum_total_jdd=0; sum_found_in_csm=0; \
    } \
    { \
	total_jdd=$6; found_in_csm=$7; \
	sum_total_jdd+=total_jdd; \
	sum_found_in_csm+=found_in_csm; \
	if( total_jdd != 0 )
	    perc=(found_in_csm*100)/total_jdd; \
	else \
	    perc=100;\
	print $3, "\t\tDwarf:\t" total_jdd, "\t\tModel:\t" found_in_csm, perc "%"; \
	cnt++; \
    } \
    END { \
if (sum_total_jdd != 0 ) {\
	avg=(sum_found_in_csm*100)/sum_total_jdd;
}\
	print "Average:", avg, "%" > ".subject" ; \
	print "Average:", avg, "%", "(jdd: ", sum_total_jdd, "csm: ", sum_found_in_csm, ")" ; \
    }' | tee -a ${DWARF_TEXT_REPORT} 

    echo "See formatted results in ${DWARF_TEXT_REPORT}"

# move results
  # send e-mail if needs
sys=`/bin/uname -s`
if [ "$MAIL_LIST" != "" ]
then
echo send me email
echo `cat .subject` 
    if [ "$sys" = "Linux" ]
    then
        cat ${DWARF_TEXT_REPORT} | mail -s "${TITLE}" `cat .subject` $MAIL_LIST
    else
        echo "Subject: " "${TITLE}" `cat .subject` >> .mail
        echo >> .mail
        cat ${DWARF_TEXT_REPORT} >> .mail
        echo "" >> .mail
        echo "PATH to results:" ${DWARF_TEXT_REPORT} >> .mail   
        echo "PATH to results in HTML:" ${DWARF_HTML_REPORT} >> .mail   
        cat .mail | mail $MAIL_LIST
        rm -rf .mail
    fi
fi
rm .subject
}

report
