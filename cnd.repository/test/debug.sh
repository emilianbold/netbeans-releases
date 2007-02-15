export JAR_PATH=../../suite/build/cluster/modules/org-netbeans-modules-cnd-repository.jar
export DEBUG="-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5858"
java $DEBUG -cp $JAR_PATH org.netbeans.modules.cnd.repository.testbench.TestBench -p 1 -i 2 -l 0.05 -t 0 -m 0 -x 500 -lf 0.5