#!/bin/bash

#
# The contents of this file are subject to the terms of the Common Development
# and Distribution License (the License). You may not use this file except in
# compliance with the License.
#
# You can obtain a copy of the License at http://www.netbeans.org/cddl.html
# or http://www.netbeans.org/cddl.txt.
#
# When distributing Covered Code, include this CDDL Header Notice in each file
# and include the License file at http://www.netbeans.org/cddl.txt.
# If applicable, add the following below the CDDL Header, with the fields
# enclosed by brackets [] replaced by your own identifying information:
# "Portions Copyrighted [year] [name of copyright owner]"
#
# The Original Software is NetBeans. The Initial Developer of the Original
# Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
# Microsystems, Inc. All Rights Reserved.
#

run() {
	echo "Root directory is `pwd`"
	
	#count files
	printf "\nFiles count:\n"
	for D in ${dirs}; do cnt=`ls $D/*.diff | wc -l`; printf "\t%s\t%5d\n" $D ${cnt}; done;
	
	#merge and sort all diffs
	all_diffs="_all_diffs_.txt"
	printf "\nMerging and sorting diffs to ${all_diffs} in each directory...\n"
	for D in ${dirs}; do (cd $D; printf " processing $D ... "; cat *.diff | egrep "(DIFFER)|(DWARF)" | sort > ${all_diffs}); done	
	printf "\n\n"
	
	all_results="_all_results_.txt"
	
	#create totals
	printf "Totals:\n"
	for D in ${dirs}; do 
		(cd $D; 
			for F in `ls *.diff`; do tail -1 $F; done > ${all_results} 
			printf "\t%s" $D
			awk '{ matched+=$4; total+=$6; delta+=$8; err+=$10} END {printf "\t%6d  of %6d  %2.2f%%      delta: %6d    parser errors: %5d\n", matched, total, (100*matched/total), delta, err}' ${all_results}
		); 
	done
	
	#unresolved counters
	printf "\nUnresolved:\n"
	for D in ${dirs}; do 
		(cd $D; 
			diffs=`grep "unresolved" *.diff | wc -l`; 
			printf "\t%s\t%6d\n" $D ${diffs}; 
		); 
	done
	
	#types differ vs not found
	printf "\nDiffer vs not found:\n\n"
	printf "\t%s\t%8s %8s    %8s\n"   "Project" "Differs" "N/F"    "N/F"
	printf "\t%s\t%8s %8s    %8s\n\n" ""        ""        "weight" "count"
	for D in ${dirs}; do 
		(cd $D; 
			diff=`grep "DIFFER|" *.diff | wc -l`;
			cnt=`grep "In DWARF only" *.diff | wc -l`; 
			weight=`grep "In DWARF only" *.diff | awk '{cnt+=$NF} END {print cnt}'`;
			printf "\t%s\t%8d %8d    %8d\n" $D ${diff} ${weight} ${cnt}
		); 
	done
	
	#const differences
	printf "\nDifferences in const modifier:\n\n"
	printf "\t%s\t%8s %8s\n\n" "Project" "Left" "Right"
	for D in ${dirs}; do 
		(cd $D; 
			cnt_L=`cat *.diff | grep "types differ" | grep -v "unresolved" | awk -F\| '{ v=sprintf(" const%s", $3); if( v == $5 ) print $0   }' | wc -l`;
			cnt_R=`cat *.diff | grep "types differ" | grep -v "unresolved" | awk -F\| '{ v=sprintf("%sconst ", $3); if( v == $5 ) print $0   }' | wc -l`;
			printf "\t%s\t%8d %8d\n" $D ${cnt_L} ${cnt_R}
		); 
	done
	

	
}

params() {
    while [ -n "$1" ]
    do
	case "$1" in
	    -r|--res)
		shift
		root=$1
		;;
	    *)
		dirs="${dirs} $1"
		;;
	esac
	shift
    done
}

main() {
	#setting defaults
	TESTCODE=${TESTCODE-${HOME}/_testcode}
	root=${TESTCODE}/_res
	
	#processing parameters
	params $@
	if [ -z "${dirs}" ]; then
		dirs="ddd mico mysql python clucene boost"
	fi
	
	#checking parameters
	if [ ! -d ${root} ]; then
		echo "Directory ${root} does not exist"
		return
	fi
	for D in ${dirs}; do
		if [ ! -d ${root}/${D} ]; then
			echo "Directory ${root}/${D} does not exist"
			return
		fi
	done

	(cd ${root}; run)
}

main $@
