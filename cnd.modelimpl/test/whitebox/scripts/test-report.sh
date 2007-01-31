#!/bin/bash 

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
