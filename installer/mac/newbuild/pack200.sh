#!/bin/sh -x

verifyClassName=
verifyClassPath=
doVerify=0

if [ -n "$2" ] && [ -n "$3" ] ; then
   verifyClassName="$2"
   verifyClassPath="$3"
   doVerify=1
fi

javaPath="/System/Library/Frameworks/JavaVM.framework/Versions/1.6/Home"
unpackCommand="$javaPath/bin/unpack200"
javaCommand="$javaPath/bin/java"
packCommand="$javaPath/bin/pack200"

verify(){
  filenamePacked="$1"
  filenameSource="$2"
  tmpFile="$2.tmp"
  $unpackCommand "$1" "$tmpFile"
  result=1
  if [ 0 -eq $? ] ; then
	$javaCommand -cp "$verifyClassPath" "$verifyClassName" "$tmpFile" >/dev/null
	result=$?
  fi

  if [ -f "$tmpFile" ] ; then
      rm "$tmpFile"
  fi

  return $result
}

for f in `find $1 -name "*.jar"`
do
  bn=`basename $f`
  if  [ "$bn" != "jhall.jar" ] && [ "$bn" != "derby.jar" ] && [ "$bn" != "derbyclient.jar" ]
  then
    if [ -f "$f.pack" ] || [ -f "$f.pack.gz" ] ; then 
        echo "Packed file $f.pack(.gz) exists, skipping packing of the original file $f"
        continue
    fi
    if [ -f `echo $f | sed 's/.jar/.jad/'` ] ; then
        echo "Jar Descriptor (.jad) exists, skipping packing of the original file $f"
        continue
    fi
    if [ 2 -eq `unzip -l "$f" 2>/dev/null | grep "META-INF/" | sed "s/.*META-INF\///g" | grep "\.SF\|\.RSA\|\.DSA"| wc -l` ] ; then
        echo "Jar file $f is signed, skipping packing"
        continue
    fi

    echo Packing $f
    $packCommand -J-Xmx256m -g $f.pack $f
    if [ 0 -eq $? ] ; then
        res=0
        if [ 1 -eq $doVerify ] ; then
	    verify $f.pack $f
            res=$?
	fi

        if [ 0 -eq $res ] ; then
            chmod `stat -f %Lp $f` $f.pack && touch -r $f $f.pack
            rm $f
        else
            echo Error verification packed jar : $f
	    rm $f.pack
        fi
    else
	if [ -f $f.pack ] ; then
	    echo Error packing jar : $f
	    rm $f.pack
	fi
    fi
  fi
done

