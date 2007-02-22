#!/bin/bash

function main {

	script_dir="../scripts"
	
	s_projects="Freeway ddd-3.3.11 mico-2.3.10 mysql-5.0.18 Python-2.4 clucene-0.9.10"
	# + boost_1_33_1
	
	i=0
	for P in ${s_projects}; do
		i=`expr $i + 1`
		projects[$i]=$P
	done
	
	cnt=${#projects[*]}
	
	root=${TESTCODE}
	
	if [ ! -d ${root} ]; then
		echo "Directory ${root} does not exist"
		return
	fi
	
	i=0;
	while [ $i -lt ${cnt} ]; do
		i=`expr $i + 1`
		dir="${root}/${projects[i]}"
		echo "==================== Configuring ${dir} ===================="
		(
			cd ${dir}
			#if [ "${projects[i]}" == "mysql-5.0.18" ]; then
			#	./configure --with-ndbcluster CFLAGS="-g3 -gdwarf-2" CXXFLAGS="-g3 -gdwarf-2"
			#else
			if [ "${projects[i]}" == "perl-5.8.5" ]; then
				./configure.gnu
			else
				./configure
			fi
			#fi
		)
	done

}

main $@

