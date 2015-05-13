#!/bin/sh -x
nb_dir=$1
jre_home="$nb_dir/Contents/Resources/NetBeans/bin/jre"

if [ -z "$1" ]; then
    echo "usage: $0 nb_dir"
    exit
fi

echo Changing 'netbeans_jdkhome' netbeans.conf in $nb_dir
echo 'jre_home' is $jre_home

if [ -d "$nb_dir" ] && [ -d "$jre_home" ]
then
  cd "$nb_dir" 
  cd Contents/Resources/NetBeans*/etc
  if [ -f netbeans.conf ]
  then
    echo netbeans.conf found: `pwd`/netbeans.conf
    if  grep -q "^netbeans_jdkhome" netbeans.conf
    then
       echo 'netbeans_jdkhome' has been already set to $jre_home
    else
        if  grep -q "^#netbeans_jdkhome=\"/path/to/jdk\"" netbeans.conf
        then
            echo Setting 'netbeans_jdkhome' to "$jre_home"...
            cp netbeans.conf netbeans.conf_orig_jdk
            cat netbeans.conf_orig_jdk  | sed -e "s|#netbeans_jdkhome=\"/path/to/jdk\"|netbeans_jdkhome=\"$jre_home\"|" > netbeans.conf
        fi
    fi
  else
    echo No netbeans.conf in: `pwd`
  fi
fi

