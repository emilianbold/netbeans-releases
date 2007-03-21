@echo off
rem pushd .
set JAVA_HOME=C:\Alaska\root\build-tools\jdk1.5.0_09
set MODULE=C:\nb_all\nbbuild\netbeans\soa1\modules
set MODULE_EXT=%MODULE%\ext
rem
set CLASSPATH=%CLASSPATH%;%MODULE_EXT%\iep\editor\JGo5.1.jar
set CLASSPATH=%CLASSPATH%;%MODULE%\org-netbeans-modules-iep-editor.jar

if "%1" == "-Xdebug" GOTO Xdebug
%JAVA_HOME%\bin\java com.sun.jbi.ui.devtool.tcg.nb.palette.NbPaletteCodeGen
goto end

:Xdebug
%JAVA_HOME%\bin\java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8000 com.sun.jbi.ui.devtool.tcg.nb.palette.NbPaletteCodeGen

goto end

:end
rem popd
