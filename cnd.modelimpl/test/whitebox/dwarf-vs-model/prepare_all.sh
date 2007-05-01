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
	cnt=${#dirs[*]}
	
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
			./test.sh --init ${init} -d ${dir} -t /tmp/${USER}/prepare-whitebox
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

