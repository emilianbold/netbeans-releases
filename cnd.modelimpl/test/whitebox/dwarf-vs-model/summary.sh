#!/bin/bash

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
			awk '{ matched+=$4; total+=$6; delta+=$8; err+=$10} END {printf "\t%6d  of %6d  %2.2f%%      delta: %6d    parser errors: %5d\n", matched, total, (matched/total), delta, err}' ${all_results}
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
	    -r)
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
	root=${HOME}/_testcode/_res
	
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
