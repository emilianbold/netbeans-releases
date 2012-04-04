#!/bin/sh -x
nb_dir=$1
jdk_home=$2

if [ -z "$1" ] || [ -z "$2" ]; then
    echo "usage: $0 nb_dir jdk_home"
    exit
fi

echo Changing 'netbeans_jdkhome' netbeans.conf in $nb_dir
echo 'jdk_home' is $jdk_home

if [ -d "$nb_dir" ] && [ -d "$jdk_home" ]
then
  cd "$nb_dir" 
  cd Contents/Resources/NetBeans*/etc
  if [ -f netbeans.conf ]
  then
    echo netbeans.conf found: `pwd`/netbeans.conf
    if  grep -q "^netbeans_jdkhome" netbeans.conf
    then
       echo 'netbeans_jdkhome' has been already set to $jdk_home
    else
        if  grep -q "^#netbeans_jdkhome=\"/path/to/jdk\"" netbeans.conf
        then
            echo Setting 'netbeans_jdkhome' to "$jdk_home"...
            cp netbeans.conf netbeans.conf_orig_jdk
            cat netbeans.conf_orig_jdk  | sed -e 's|#netbeans_jdkhome=\"/path/to/jdk\"|netbeans_jdkhome=\"'$jdk_home'\"|' > netbeans.conf
        fi
    fi
  else
    echo No netbeans.conf in: `pwd`
  fi
fi

