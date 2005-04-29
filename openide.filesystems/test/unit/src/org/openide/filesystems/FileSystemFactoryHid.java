/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.filesystems;

import junit.framework.*;
import org.netbeans.junit.*;

import java.io.*;
import java.util.*;

import org.openide.filesystems.*;


/**
 *
 * @author  vs124454, rm111737 
 * @version 
 */
public abstract class FileSystemFactoryHid extends NbTestSetup {
    private static Map map = new HashMap ();
    private static String className;
    

    /** Creates new FileSystemFactory
     * @param test  */
    public FileSystemFactoryHid(Test test) {
        super(test);        
        /**Adds */
        registerMap (test);
    }

    /**
     * Intended to allow prepare tested environment for each individual test.
     * Although this method is static, all subclasses must modify its behaviour by means of
     * createTestedFS.
     * @param testName name of test 
     * @return  array of FileSystems that should be tested in test named: "testName"*/    
    final static FileSystem[] createFileSystem (String testName,String[] resources, Test test) throws IOException {
         return getInstance (test,true).createFileSystem(testName, resources);
    }
      
    final static void destroyFileSystem (String testName, Test test)  throws IOException  {
        getInstance (test,false).destroyFileSystem(testName);
    }    
    
    final static String getResourcePrefix (String testName, Test test, String[] resources) {
        return getInstance (test,false).getResourcePrefix(testName, resources);
    }    

    
    
    private final static  FileSystemFactoryHid getInstance (Test test, boolean delete) {
            FileSystemFactoryHid factory = getFromMap (test,delete);
            if (factory != null)
                className  =  factory.getClass().getName();
        return factory;
    }
    
    final static  String getTestClassName () {
        return (className != null) ? className : "Unknown TestSetup";
    }
    
    /**
     * @param resources that are required to run given test
     * @return  array of FileSystems that should be tested in test named: "testName"*/    
    protected abstract FileSystem[] createFileSystem(String testName, String[] resources) throws IOException;        
    
    protected abstract void destroyFileSystem(String testName) throws IOException;

    protected String getResourcePrefix (String testName, String[] resources) {
        return "";
    }
    

    private void registerMap (Test test) {
        if (test instanceof TestSuite) {
            Enumeration en = ((TestSuite)test).tests ();
            while (en.hasMoreElements()) {                
                Test tst = (Test)en.nextElement();
                if (tst instanceof TestSuite) 
                    registerMap (tst);
                else {
                    addToMap (tst);
                }
            }
        } else {
            addToMap (test);                
        }
    }
    
    private   void addToMap (Test test) {    
        List s = (List)map.get (test);
        if (s == null) {
            s = new LinkedList();
        } 
        s.add(this);
        map.put(test ,s );                                                        
        
    }
        

    private static FileSystemFactoryHid getFromMap (Test test, boolean delete) {    
        LinkedList s = (LinkedList)map.get (test);
        FileSystemFactoryHid  retVal;
        try {
            retVal = (FileSystemFactoryHid)s.getLast();
        } catch (NoSuchElementException x ) {
            System.out.println("exc: "+ test + " : " );
            throw x;
        }
        if (delete) {
            s.remove(retVal);
        }
        return retVal;         
    }            
}
