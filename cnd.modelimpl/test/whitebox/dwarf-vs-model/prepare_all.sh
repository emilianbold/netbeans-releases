#!/bin/sh

function main {

	script_dir="../scripts"
	
	s_dirs="freeway ddd mico mysql python clucene"
	# + boost
	
	s_projects="Freeway ddd-3.3.11 mico-2.3.10 mysql-5.0.18 Python-2.4 clucene-0.9.10"
	# + boost_1_33_1
	
	i=0
	for D in ${s_dirs}; do
		i=`expr $i + 1`
		dirs[$i]=$D
	done
	
	i=0
	for P in ${s_projects}; do
		i=`expr $i + 1`
		projects[$i]=$P
	done
	
	if [ ${#dirs[*]} != ${#projects[*]} ]; then
		echo "Error: there are ${#dirs[*]} elements in dirs and ${#projects[*]} in projects!"
		return
	fi
	#cnt=${#dirs[*]}
	cnt=1
	
	root=${TESTCODE}
	initdata=${root}/_initdata
	
	if [ ! -d ${initdata} ]; then
		echo "Directory ${initdata} does not exist"
		return
	fi
	
	i=0;
	while [ $i -lt ${cnt} ]; do
		i=`expr $i + 1`
		init="${initdata}/${dirs[i]}"
		dir="${root}/${projects[i]}"
		echo ${init} ${dir}
		ls -ld ${dir}
		(
			cd ${script_dir}
			set -x
			./test.sh --init ${init} -d ${dir} 
			cd ${init}
			rc=$?
			if [ ${rc} != 0 ]; then
				return
			fi
			rm all.gcc
			ln -s ${projects[i]}.gcc all.gcc
			set +x
		)
	done

}

main $@

