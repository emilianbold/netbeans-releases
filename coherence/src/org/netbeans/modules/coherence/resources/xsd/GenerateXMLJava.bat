SETLOCAL
set JAXB_HOME=C:\Software\Java\jaxb-ri
set JAXB_CLASSPATH=%JAXB_HOME%\lib\jsr173_1.0_api.jar;%JAXB_HOME%\lib\jaxb-api.jar;%JAXB_HOME%\lib\jaxb-impl.jar;%JAXB_HOME%\lib\activation.jar
set PATH=%PATH%;%JAXB_HOME%\bin;%JAVA_HOME%\bin
set CLASSPATH=%CLASSPATH%;%JAXB_CLASSPATH%
rem call %JAXB_HOME%\bin\xjc BPEL.xsd -d ..\..\java -p com.tox.core.bpel.xml
call %JAXB_HOME%\bin\xjc -dtd cache-config.dtd -d .. -p com.oracle.ateam.coherence.xml.cache
call %JAXB_HOME%\bin\xjc -dtd coherence.dtd -d .. -p com.oracle.ateam.coherence.xml.coherence
call %JAXB_HOME%\bin\xjc -dtd pof-config.dtd -d .. -p com.oracle.ateam.coherence.xml.pof
pause