
SET BUILD_JDK=C:\PROGRA~1\java\jdk1.5.0_08
SET LIB_NAME=lib.jnikill.amd64.x86

cl /I%BUILD_JDK%\include /I%BUILD_JDK%\include\win32 ^
org_netbeans_xtest_util_JNIKill.c ^
/D WIN32 /MD /Ox /c

link /DLL /MAP:%LIB_NAME%.map /OUT:%LIB_NAME%.dll org_netbeans_xtest_util_JNIKill.obj bufferoverflowU.lib

del vc60.pdb
del *.obj
del *.exp
del *.lib

