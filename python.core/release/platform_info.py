#! /usr/bin/python

import sys
import os

command = sys.executable
version = sys.version.split()[0]
isJava = sys.platform.count("java")
if isJava :
    print("platform.name="+ "Jython " + version)
else:
    print("platform.name="+ "Python " + version)
if command != None :    
    print("python.command="+ command.replace("\\", "\\\\"))
path = ""
for pathItem in sys.path:
    path += pathItem + os.pathsep
print("python.path="+path.replace("\\", "\\\\"))

if isJava  :  
    from java.lang import System
    classpath = System.getProperty('java.class.path')
    print(classpath)

