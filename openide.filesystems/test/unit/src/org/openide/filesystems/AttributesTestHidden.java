/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.filesystems;

import junit.framework.*;
//import org.openide.filesystems.hidden.*;

import java.util.*;
import java.io.*;
import java.net.URL;

/**
 *
 * @author  vs124454
 * @version
 *
 */
public class AttributesTestHidden extends TestBaseHid {
    static final String TEST_ERR = "Not the tested code, but this test contains errors";
    static final String COMMON_ATTR_STRING = "testAttribute";

    private static String[] resources = new String [] {
        "/compatibility/filesystem.attributes.backup",//0
        "/compatibility/Attr.test",//1
    };
    
    //private boolean logIt = false;

    protected String[] getResources (String testName) {
        return resources;
    }
    
    /** Creates new FileObjectTestHidden */
    public AttributesTestHidden(String name) {
        super(name);
    }

    /**
     *  setAttribute did not write all changes to .nbattrs and after gc() WeakReference
     *  was lost and from .nbattrs was read some old value.
     *  Problem better described in IssueZilla.
     *  BugFix: #10637 
     *  Probably problem:      
     */
    @SuppressWarnings("unchecked")
    public void testAttribute01() throws IOException {
        assertTrue (TEST_ERR,testedFS != null);
        if (testedFS.isReadOnly ()) return;                
        
        FileObject foTested = testedFS.getRoot ();
        List<String> list  = new LinkedList<String> ();
        
        
        int i = 0;
        for (i = 0; i < 20; i++) {
            list.add(COMMON_ATTR_STRING);        
            foTested.setAttribute (COMMON_ATTR_STRING, list);
        }
        
        System.gc ();System.gc ();
        try {
            Thread.sleep(300);
        } catch (InterruptedException iex){}
        list = (List)foTested.getAttribute (COMMON_ATTR_STRING);
        assertTrue ("Caching problem in attributes: "+list.size(),list.size() == i);
    }
    
    /** NOI18N specific test. BugFix: #11076, #11127
     *  Probably problem:      
     */ 
    public void testAttribute02 () throws IOException {
        assertTrue (TEST_ERR,testedFS != null);
        if (testedFS.isReadOnly ()) return;        
        
        Locale locale = Locale.getDefault();// returns current value

        if (locale != Locale.JAPAN  || locale != Locale.JAPANESE) 
            return;                
        
        /** I`m not sure how japan unicode chars looks like. For present time
         * I choose some chars > 0xFF */
        String japanFileName = new String (new char[] {'\u025A','\u012F','\u025A','\u012F'});
        FileObject foTested = testedFS.getRoot ();        
        FileObject folder = foTested.getFileObject (japanFileName,null);
        
        if (folder == null) {            
            try {
                folder = foTested.createFolder (japanFileName);
                if (folder == null) return;
            } catch (IOException iox) {
                return;
            }            
        }
        
        folder.setAttribute (COMMON_ATTR_STRING,COMMON_ATTR_STRING);
        String result = (String)folder.getAttribute (COMMON_ATTR_STRING);
        assertTrue ("Coding problem by writing and reading attributes - .nbattrs",result != null);
    }

    /** In XML not permited chars should be properly encoded and decode
     *  BugFix: #? 
     *  Probably problem:      
     */     
    public void testAttribute03 () throws IOException {
        assertTrue (TEST_ERR,testedFS != null);
        if (testedFS.isReadOnly ()) return;                

        /** Probably later extend set of chars that should be tested*/
        String testedStr = new String (new char[] {'&','<','>','\"','\''});
        String result;
        
        result = basicAttributeTest (testedStr,COMMON_ATTR_STRING);
        assertTrue ("XML not allowed chars are bad handled - key problem",
        result != null && result.equals (COMMON_ATTR_STRING));                
        
        result = basicAttributeTest (COMMON_ATTR_STRING,testedStr);                
        assertTrue ("XML not allowed chars are bad handled - value problem",
        result != null && result.equals (testedStr));                        
    }

    /** 
     *  Back compatibility. Transforming from serialized file: filesystem.attributes
     *  BugFix: #?
     *  Probably problem:      
     */     
    public void KtestAttribute04 () throws IOException {
        assertTrue (TEST_ERR,testedFS != null);
        if (testedFS.isReadOnly ()) return;                
        
        /** This test expects that gets testedFS where exists next resorce*/
        FileObject backup = testedFS.findResource ("/compatibility/filesystem.attributes.backup");
        assertTrue (TEST_ERR,backup != null);
        FileLock lock = backup.lock();        
        backup.rename(lock,"filesystem","attributes");
        lock.releaseLock();
        FileObject testedFo = testedFS.findResource ("/compatibility/Attr.test");
        assertTrue (TEST_ERR,testedFo != null);
        /** Name of attribute testAttr must be kept. Becuase this name is saved in old format in 
         * filesystem.attributes*/
        assertTrue ("Back compatibility problem",testedFo.getAttribute ("testAttr") != null);                 
    }

    /** 
     *  filsystem.attributes files should be hidden
     *  BugFix: #?
     *  Probably problem:      
     */     
    public void testAttribute05 () throws IOException {
        assertTrue (TEST_ERR,testedFS != null);
        if (testedFS.isReadOnly ()) return;                
        
        /** This test expects that gets testedFS where exists next resorce*/
        FileObject testedFo = testedFS.findResource ("/compatibility");
        assertTrue (TEST_ERR,testedFo != null);
                
        assertTrue ("filesystem.attributes must stay hidden for filesystems",testedFo.getFileObject ("filesystem","attributes") == null);                 
    }

    
    /** 
     *  .nbattrs files should be hidden
     *  BugFix: #?
     *  Probably problem:      
     */     
    public void testAttribute06 () throws IOException {
        assertTrue (TEST_ERR,testedFS != null);
        if (testedFS.isReadOnly ()) return;                
        
        basicAttributeTest (COMMON_ATTR_STRING,COMMON_ATTR_STRING);
        assertTrue (".nbattrs must stay hidden for filesystems",testedFS.getRoot ().getFileObject (".nbattrs") == null);                         
    }
    
    /** 
     *  Null as vaule should delete record for key from attributes
     *  BugFix: #?
     *  Probably problem:      
     */     
    public void testAttribute07 () throws IOException {
        assertTrue (TEST_ERR,testedFS != null);
        if (testedFS.isReadOnly ()) return;                
        /**This test can be done only on LocalFileSystem - but it is not important 
         * to run this test on other FileSystems
         */
        if (!(testedFS instanceof LocalFileSystem)) return;
        
        String testAttr = COMMON_ATTR_STRING+"Null";
        
        basicAttributeTest (testAttr,testAttr);
        basicAttributeTest (COMMON_ATTR_STRING,COMMON_ATTR_STRING);        
        
        String result = basicAttributeTest (testAttr,null);        
        assertTrue ("After setAttribute (,null) - getAttribute must return null",result == null);                         
        
        String content = testAttr;
        FileInputStream fis = null;
        try {
            File parent = FileUtil.toFile(testedFS.getRoot());
            fis = new FileInputStream(new File(parent,".nbattrs"));
            byte[] bytes = new byte [fis.available()];
            fis.read(bytes);
            content = new String (bytes);            
        } finally {
            if (fis != null) fis.close();
        } 
        assertTrue ("After setAttribute (,null) appropriate key cannot be in .nbattrs",content.indexOf("name=\""+testAttr) == -1 );
        assertTrue ("There was not found expected key: " + COMMON_ATTR_STRING +" in .nbattrs",content.indexOf("name=\""+COMMON_ATTR_STRING) != -1 );//        
    }
    
    
    public void testAttribute08 () throws Exception {
      URL fsURLDef = this.getClass().getResource ("data/Attributes.xml");
      assertTrue ("Cannot create XML FS for testing purposes", fsURLDef != null);  
      FileSystem fs = new XMLFileSystem (fsURLDef);
      FileObject fo = fs.findResource("testMethodValue");
      assertTrue ("Cannot acces  FileObject named testMethodValue", fo != null);  

      String testName = "test1";
      Object obj = fo.getAttribute(testName);
      assertTrue ("methodValue failed", obj != null);          
      assertTrue ("methodValue doesn't keep order ", 
      obj.equals(getObjectViaMethodValue1 (fo, testName)));                
      
      testName = "test2";
      obj = fo.getAttribute(testName);
      assertTrue ("methodValue failed", obj != null);          
      assertTrue ("methodValue doesn't keep order ", 
      obj.equals(getObjectViaMethodValue2 (testName, fo)));                

      testName = "test3";
      obj = fo.getAttribute(testName);
      assertTrue ("methodValue failed", obj != null);          
      assertTrue ("methodValue doesn't keep order ", 
      obj.equals(getObjectViaMethodValue3 (fo)));                

      testName = "test4";
      obj = fo.getAttribute(testName);
      assertTrue ("methodValue failed", obj != null);          
      assertTrue ("methodValue doesn't keep order ", 
      obj.equals(getObjectViaMethodValue4 (testName)));                

      testName = "test5";
      obj = fo.getAttribute(testName);
      assertTrue ("methodValue failed", obj != null);          
      assertTrue ("methodValue doesn't keep order ", 
      obj.equals(getObjectViaMethodValue5 ()));                     

      testName = "test6";
      obj = fo.getAttribute(testName);
      assertTrue ("methodValue failed", obj != null);          
      assertEquals("even works for java.util.Map", "Ahoj1", obj);

      testName = "test7";
      obj = fo.getAttribute(testName);
      assertTrue ("methodValue failed", obj != null);          
      assertEquals("works for map and string", "Ahoj1test7", obj);  
    }

    public void testAttribute09 () throws IOException {
        assertTrue (TEST_ERR,testedFS != null);
        if (testedFS.isReadOnly ()) return;                
        /**This test can be done only on LocalFileSystem - but it is not important 
         * to run this test on other FileSystems
         */
        if (!(testedFS instanceof LocalFileSystem)) return;
        FileObject foTested = testedFS.getRoot ();
        
        char[] testChars  = new char [] {'A',0x0000,0x008F,'B'}; 
        String[] atts  = new String [] {"c:\\builds\\u3\\AboutExamples",
            "c:\\builds\\u3A\\AboutExamples",
            "c:\\builds\\u33\\AboutExamples",
            "c:\\builds\\u345\\AboutExamples",
            "c:\\builds\\u3456\\AboutExamples",            
            "c:\\builds\\u3G85\\AboutExamples",
            "c:\\builds\\au3456\\AboutExamples",
            String.valueOf(testChars)            
        };

        for (int i = 0; i < atts.length; i++) {
            String keyAndValue = atts[i];
            foTested.setAttribute(keyAndValue, keyAndValue);
            Object testIt = foTested.getAttribute(keyAndValue);
            assertEquals(keyAndValue, testIt);            
        }                        
    }
    
    private static String getObjectViaMethodValue1 (FileObject fo, String testName) {
        return fo.getPath()+testName;
    }

    private static String getObjectViaMethodValue1 (String testName, FileObject fo) {
        return testName+fo.getPath();
    }

    private static String getObjectViaMethodValue1 (FileObject fo) {
        return fo.getPath();        
    }
    
    private static String getObjectViaMethodValue1 (String testName) {
        return testName;
    }

    private static String getObjectViaMethodValue1 () {
        return "";        
    }
///
    private static String getObjectViaMethodValue2 (String testName, FileObject fo) {
        return testName+fo.getPath();
    }

    private static String getObjectViaMethodValue2 (FileObject fo) {
        return fo.getPath();        
    }
    
    private static String getObjectViaMethodValue2 (String testName) {
        return testName;
    }

    private static String getObjectViaMethodValue2 () {
        return "";        
    }
///
    private static String getObjectViaMethodValue3 (FileObject fo) {
        return fo.getPath();        
    }

    private static String getObjectViaMethodValue3 (String testName) {
        return testName;
    }

    private static String getObjectViaMethodValue3 () {
        return "";        
    }
///
    private static String getObjectViaMethodValue4 (String testName) {
        return testName;
    }

    private static String getObjectViaMethodValue4 () {
        return "";        
    }
///
    private static String getObjectViaMethodValue5 () {
        return "";        
    }
            
    private static Object getObjectViaMethodValue6 (Map attrs) {
        try {
            attrs.keySet().iterator().remove();
            return "UnsupportedOperationException";
        } catch (UnsupportedOperationException ex) {
            // ok
        }
        try {
            attrs.put("value1", "nothing");
            return "UnsupportedOperationException";
        } catch (UnsupportedOperationException ex) {
            // ok
        }
        try {
            attrs.remove("value1");
            return "UnsupportedOperationException";
        } catch (UnsupportedOperationException ex) {
            // ok
        }
        
        
        return attrs.get("value1");
    }
    private static Object getObjectViaMethodValue7 (Map<String,Object> attrs, String attrName) {
        assertEquals(8, attrs.keySet().size());
        try {
            attrs.entrySet().remove(null);
            return "UnsupportedOperationException";
        } catch (UnsupportedOperationException ex) {
            // ok
        }
        
        
        return attrs.get("value1") + attrName;
    }
            
    private String basicAttributeTest (String attrName, String attrValue) throws IOException {
        FileObject foTested = testedFS.getRoot ();        
        foTested.setAttribute (attrName, attrValue);                
        return (String)foTested.getAttribute (attrName);
    }
    /*public void testFindResource () throws IOException {
        assertTrue (TEST_ERR,testedFS != null);                    
    }*/

}



