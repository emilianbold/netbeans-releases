#!/bin/bash
unzip $1 -d tmp
find tmp/netbeans -name "*.jar.pack.gz" | xargs -I [] unpack200 -r [] [].jar
find tmp/netbeans -name "*.pack.gz.jar" | grep .pack.gz.jar | sed 's/\(.*\).pack.gz.jar/mv & \1/' | sh
ant -Dnbm.filename=$1 -f $BASE_DIR/main/nbbuild/build.xml refresh-update_tracking-ml
rm -rf tmp
