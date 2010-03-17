#!/bin/sh -x
nb_dir=$1
javadb_dir=$2

echo NetBeans in question is $nb_dir
echo JavaDB is $javadb_dir

if [ "$nb_dir" = "" ] || [ "$javadb_dir" = "" ]
then
  exit
fi
if [ -d "$nb_dir" ] && [ -d "$javadb_dir" ]
then
  cd "$nb_dir" 
  cd Contents/Resources/NetBeans*/
  curdir=`pwd`
  dirname=`dirname "$0"`
  jdk_home=`"$dirname"/get_current_jdk.sh`
  "$jdk_home"/bin/java -cp \
                           platform/core/core.jar:platform/lib/boot.jar:platform/lib/org-openide-modules.jar:platform/core/org-openide-filesystems.jar:platform/lib/org-openide-util.jar:platform/lib/org-openide-util-lookup.jar:ide/modules/org-netbeans-modules-derby.jar \
                           \
                           org.netbeans.modules.derby.DerbyRegistration \
                           \
                           "$curdir/nb" \
                           "$javadb_dir"
  val=$?

  if [ $val -eq 0 ] ; then
     echo "JavaDB available at $javadb_dir integrated with NetBeans installed at $nb_dir"
  else
     echo "JavaDB available at $javadb_dir was not integrated with NetBeans installed at $nb_dir, error code is $val"
  fi
fi

