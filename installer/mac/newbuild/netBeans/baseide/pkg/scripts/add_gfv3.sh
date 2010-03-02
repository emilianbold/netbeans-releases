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
  dirname=`dirname "$0"`
  jdk_home=`"$dirname"/get_current_jdk.sh`
  "$jdk_home"/bin/java -cp \
                           platform/core/core.jar:\
                           platform/lib/boot.jar:\
			   platform/lib/org-openide-modules.jar:\
			   platform/core/org-openide-filesystems.jar:\
                           platform/lib/org-openide-util.jar:\
                           platform/lib/org-openide-util-lookup.jar:\
                           ide/modules/org-netbeans-modules-glassfish-common.jar \
                           \
                           org.netbeans.modules.glassfish.common.registration.AutomaticRegistration \
                           \
                           "$nb_dir/nb" \
                           "$gf_dir/glassfish"
  val=$?

  if [ $val -eq 0 ] ; then
     echo "GlassFish V3 installed at $gf_dir integrated with NetBeans installed at $nb_dir"
  else
     echo "GlassFish V3 installed at $gf_dir was not integrated with NetBeans installed at $nb_dir, error code is $val"
  fi
fi

