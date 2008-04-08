#!/bin/sh -x
nb_dir=$1
gf_dir=$2

echo Changing netbeans.conf in $nb_dir
echo GlassFish is in $gf_dir

if [ "$nb_dir" = "" ] || [ "$gf_dir" = "" ]
then
  exit
fi
if [ -d "$nb_dir" ] && [ -d "$gf_dir" ]
then
  cd "$nb_dir" 
  cd Contents/Resources/NetBeans*/etc
  if [ -f netbeans.conf ]
  then
    echo netbeans.conf found: `pwd`/netbeans.conf
    cp netbeans.conf netbeans.conf_orig_gf
    cat netbeans.conf_orig_gf  | sed -e 's|netbeans_default_options=\"|netbeans_default_options=\"-J-Dcom.sun.aas.installRoot='$gf_dir' |' > netbeans.conf
  else
    echo No netbeans.conf in: `pwd`
  fi
fi

