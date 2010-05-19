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
