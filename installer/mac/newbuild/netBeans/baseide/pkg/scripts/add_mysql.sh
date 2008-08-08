#!/bin/sh -x
nb_dir=$1
mysql_dir=$2

echo Changing netbeans.conf in $nb_dir
echo MySQL is in $mysql_dir

if [ "$nb_dir" = "" ] || [ "$mysql_dir" = "" ]
then
  exit
fi
if [ -d "$nb_dir" ] && [ -d "$mysql_dir" ]
then
  cd "$nb_dir" 
  cd Contents/Resources/NetBeans*/etc
  if [ -f netbeans.conf ]
  then
    echo netbeans.conf found: `pwd`/netbeans.conf
    cp netbeans.conf netbeans.conf_orig_mysql
    admin_file=$mysql_dir/support-files/mysql-admin.server
    cat netbeans.conf_orig_mysql  | sed -e 's|netbeans_default_options=\"|netbeans_default_options=\"-J-Dcom.sun.mysql.startcommand='$admin_file' -J-Dcom.sun.mysql.stopcommand='$admin_file' -J-Dcom.sun.mysql.startargs=start -J-Dcom.sun.mysql.stopargs=stop -J-Dcom.sun.mysql.port=3306 |' > netbeans.conf
  else
    echo No netbeans.conf in: `pwd`
  fi
fi

