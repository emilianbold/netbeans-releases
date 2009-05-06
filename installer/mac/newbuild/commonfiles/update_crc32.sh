#!/bin/sh -x

#set  -x

if [ -z "$1" ] || [ -z "$2" ] ; then
    echo "usage: $0 jarsDir nbDir"
    echo "jarsDir is the dir with unpacked .jar files"
    echo "nbDir is the dir with NetBeans sources containing update_tracking dirs to be processed"
    exit 1;
fi

jarsDir="$1"
nbDir="$2"

cd "$jarsDir"

for jar in $(find . -name "*.jar")
do
    echo JAR FILE = "$jar"
    #get first number from cksum output
    crc32=`cksum -o 3 "$jar" | sed 's/\([0-9]*\).*/\1/'`
    #get jar's relative path without 'cluster' folder (the same as used in xml)
    jar_subpath=`echo $jar | sed 's/^.\/[a-z0-9\.]*\///'`
    jar_name=`basename $jar 2>&1`
    #find xml file (within update_tracking dirs) in which this jar is mentioned
    update_tracking_xml_file=`ls "$nbDir"/*/update_tracking/*.xml | xargs grep -l "$jar_subpath"` 

    #if xml file is found replace the crc32
    if [ ! -z "$update_tracking_xml_file" ] && [ -f "$update_tracking_xml_file" ] ; then        
        cp "$update_tracking_xml_file" "$update_tracking_xml_file".back

        jar_subpath_for_sed=`echo $jar_subpath | sed "s/\\\//\\\\\\\\\//g"`
        sed '/'$jar_subpath_for_sed'/s/crc=\"[0-9]*\"/crc=\"'$crc32'\"/' < "$update_tracking_xml_file".back > "$update_tracking_xml_file"

        rm -rf "$update_tracking_xml_file".back
        echo "File '$update_tracking_xml_file' is processed"
       # echo "jar file = $jar"
       # echo "new crc32 = $crc32"
       # echo ""
    fi
done
