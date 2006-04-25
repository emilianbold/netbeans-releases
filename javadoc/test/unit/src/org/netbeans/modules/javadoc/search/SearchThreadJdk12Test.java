/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.javadoc.search;

import java.io.File;
import java.util.List;
import java.util.LinkedList;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Jan Pokorsky
 */
public final class SearchThreadJdk12Test extends NbTestCase {

    private LocalFileSystem fs;
    private static final String JDK14_INDEX_PATH = "docs_jdk14/api/index-files";
    private static final String JDK15_INDEX_PATH = "docs_jdk15/api/index-files";

    public SearchThreadJdk12Test(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        File dataFile = getDataDir();
        assertNotNull("missing data file", dataFile);
        fs = new LocalFileSystem();
        fs.setRootDirectory(dataFile);
    }

    public void testSearchInJDK14_Class() throws Exception {
        FileObject idxFolder = fs.findResource(JDK14_INDEX_PATH);
        assertNotNull(idxFolder);
        
        String toFind = "DataFlavor";
        MyDocIndexItemConsumer diiConsumer = new MyDocIndexItemConsumer();
        SearchThreadJdk12 search = new SearchThreadJdk12(toFind, idxFolder, diiConsumer, true);
        search.run(); // not go() since we do not want to post the task to another thread
        assertTrue("not finished", diiConsumer.isFinished);
        assertEquals("search result", 6, diiConsumer.l.size());
        
        // class DataFlavor
        DocIndexItem dii = (DocIndexItem) diiConsumer.l.get(0);
        assertEquals("field", "DataFlavor", dii.getField());
        assertEquals("declaring class", "DataFlavor", dii.getDeclaringClass());
        assertEquals("remark", " - class java.awt.datatransfer.DataFlavor.", dii.getRemark());
        assertEquals("package", "java.awt.datatransfer.", dii.getPackage());
        assertTrue("url", dii.getURL().toString().endsWith("api/java/awt/datatransfer/DataFlavor.html"));
        // constructor
        dii = (DocIndexItem) diiConsumer.l.get(1);
        assertEquals("field", "DataFlavor()", dii.getField());
        assertEquals("declaring class", "DataFlavor", dii.getDeclaringClass());
        assertEquals("remark", " - Constructor for class java.awt.datatransfer.DataFlavor", dii.getRemark());
        assertEquals("package", "java.awt.datatransfer.", dii.getPackage());
        assertTrue("url", dii.getURL().toString().endsWith("api/java/awt/datatransfer/DataFlavor.html#DataFlavor()"));
    }
    
    public void testSearchInJDK14_Interface() throws Exception {
        FileObject idxFolder = fs.findResource(JDK14_INDEX_PATH);
        assertNotNull(idxFolder);
        
        String toFind = "DatabaseMetaData";
        MyDocIndexItemConsumer diiConsumer = new MyDocIndexItemConsumer();
        SearchThreadJdk12 search = new SearchThreadJdk12(toFind, idxFolder, diiConsumer, true);
        search.run(); // not go() since we do not want to post the task to another thread
        assertTrue("not finished", diiConsumer.isFinished);
        assertEquals("search result", 1, diiConsumer.l.size());
        
        // DatabaseMetaData
        DocIndexItem dii = (DocIndexItem) diiConsumer.l.get(0);
        assertEquals("field", "DatabaseMetaData", dii.getField());
        assertEquals("declaring class", "DatabaseMetaData", dii.getDeclaringClass());
        assertEquals("remark", " - interface java.sql.DatabaseMetaData.", dii.getRemark());
        assertEquals("package", "java.sql.", dii.getPackage());
        assertTrue("url", dii.getURL().toString().endsWith("api/java/sql/DatabaseMetaData.html"));
    }

    public void testSearchInJDK14_Exception() throws Exception {
        FileObject idxFolder = fs.findResource(JDK14_INDEX_PATH);
        assertNotNull(idxFolder);
        
        String toFind = "DataFormatException";
        MyDocIndexItemConsumer diiConsumer = new MyDocIndexItemConsumer();
        SearchThreadJdk12 search = new SearchThreadJdk12(toFind, idxFolder, diiConsumer, true);
        search.run(); // not go() since we do not want to post the task to another thread
        assertTrue("not finished", diiConsumer.isFinished);
        assertEquals("search result", 3, diiConsumer.l.size());
        
        // DataFormatException
        DocIndexItem dii = (DocIndexItem) diiConsumer.l.get(0);
        assertEquals("field", "DataFormatException", dii.getField());
        assertEquals("declaring class", "DataFormatException", dii.getDeclaringClass());
        assertEquals("remark", " - exception java.util.zip.DataFormatException.", dii.getRemark());
        assertEquals("package", "java.util.zip.", dii.getPackage());
        assertTrue("url", dii.getURL().toString().endsWith("api/java/util/zip/DataFormatException.html"));
        
        // DataFormatException(String) - constructor
        dii = (DocIndexItem) diiConsumer.l.get(2);
        assertEquals("field", "DataFormatException(String)", dii.getField());
        assertEquals("declaring class", "DataFormatException", dii.getDeclaringClass());
        assertEquals("remark", " - Constructor for class java.util.zip.DataFormatException", dii.getRemark());
        assertEquals("package", "java.util.zip.", dii.getPackage());
        assertTrue("url", dii.getURL().toString().endsWith("api/java/util/zip/DataFormatException.html#DataFormatException(java.lang.String)"));
    }

    public void testSearchInJDK14_Method() throws Exception {
        FileObject idxFolder = fs.findResource(JDK14_INDEX_PATH);
        assertNotNull(idxFolder);
        
        String toFind = "damageLineRange";
        MyDocIndexItemConsumer diiConsumer = new MyDocIndexItemConsumer();
        SearchThreadJdk12 search = new SearchThreadJdk12(toFind, idxFolder, diiConsumer, true);
        search.run(); // not go() since we do not want to post the task to another thread
        assertTrue("not finished", diiConsumer.isFinished);
        assertEquals("search result", 1, diiConsumer.l.size());
        
        // damageLineRange(int, int, Shape, Component)
        DocIndexItem dii = (DocIndexItem) diiConsumer.l.get(0);
        assertEquals("field", "damageLineRange(int, int, Shape, Component)", dii.getField());
        assertEquals("declaring class", "PlainView", dii.getDeclaringClass());
        assertEquals("remark", " - Method in class javax.swing.text.PlainView", dii.getRemark());
        assertEquals("package", "javax.swing.text.", dii.getPackage());
        assertTrue("url", dii.getURL().toString().endsWith("api/javax/swing/text/PlainView.html#damageLineRange(int, int, java.awt.Shape, java.awt.Component)"));
    }

    public void testSearchInJDK14_Variables() throws Exception {
        FileObject idxFolder = fs.findResource(JDK14_INDEX_PATH);
        assertNotNull(idxFolder);
        
        String toFind = "darkShadow";
        MyDocIndexItemConsumer diiConsumer = new MyDocIndexItemConsumer();
        SearchThreadJdk12 search = new SearchThreadJdk12(toFind, idxFolder, diiConsumer, true);
        search.run(); // not go() since we do not want to post the task to another thread
        assertTrue("not finished", diiConsumer.isFinished);
        assertEquals("search result", 4, diiConsumer.l.size());
        
        // darkShadow
        DocIndexItem dii = (DocIndexItem) diiConsumer.l.get(0);
        assertEquals("field", "darkShadow", dii.getField());
        assertEquals("declaring class", "BasicBorders.ButtonBorder", dii.getDeclaringClass());
        assertEquals("remark", " - Variable in class javax.swing.plaf.basic.BasicBorders.ButtonBorder", dii.getRemark());
        assertEquals("package", "javax.swing.plaf.basic.", dii.getPackage());
        assertTrue("url", dii.getURL().toString().endsWith("api/javax/swing/plaf/basic/BasicBorders.ButtonBorder.html#darkShadow"));
        
        // darkShadowColor - static variable
        dii = (DocIndexItem) diiConsumer.l.get(3);
        assertEquals("field", "darkShadowColor", dii.getField());
        assertEquals("declaring class", "MetalSliderUI", dii.getDeclaringClass());
        assertEquals("remark", " - Static variable in class javax.swing.plaf.metal.MetalSliderUI", dii.getRemark());
        assertEquals("package", "javax.swing.plaf.metal.", dii.getPackage());
        assertTrue("url", dii.getURL().toString().endsWith("api/javax/swing/plaf/metal/MetalSliderUI.html#darkShadowColor"));
    }

    public void testSearchInJDK15_Class() throws Exception {
        FileObject idxFolder = fs.findResource(JDK15_INDEX_PATH);
        assertNotNull(idxFolder);
        
        String toFind = "DataFlavor";
        MyDocIndexItemConsumer diiConsumer = new MyDocIndexItemConsumer();
        SearchThreadJdk12 search = new SearchThreadJdk12(toFind, idxFolder, diiConsumer, true);
        search.run(); // not go() since we do not want to post the task to another thread
        assertTrue("not finished", diiConsumer.isFinished);
        assertEquals("search result", 6, diiConsumer.l.size());
        
        // class DataFlavor
        DocIndexItem dii = (DocIndexItem) diiConsumer.l.get(0);
        assertEquals("field", "DataFlavor", dii.getField());
        assertEquals("declaring class", "java.awt.datatransfer", dii.getDeclaringClass());
        assertEquals("remark", " - Class in java.awt.datatransfer", dii.getRemark());
        assertEquals("package", "java.awt.datatransfer.", dii.getPackage());
        assertTrue("url", dii.getURL().toString().endsWith("api/java/awt/datatransfer/DataFlavor.html"));
        // constructor
        dii = (DocIndexItem) diiConsumer.l.get(1);
        assertEquals("field", "DataFlavor()", dii.getField());
        assertEquals("declaring class", "DataFlavor", dii.getDeclaringClass());
        assertEquals("remark", " - Constructor for class java.awt.datatransfer.DataFlavor", dii.getRemark());
        assertEquals("package", "java.awt.datatransfer.", dii.getPackage());
        assertTrue("url", dii.getURL().toString().endsWith("api/java/awt/datatransfer/DataFlavor.html#DataFlavor()"));
    }

    public void testSearchInJDK15_GenericClass_54244() throws Exception {
        // see issue #54244
        FileObject idxFolder = fs.findResource(JDK15_INDEX_PATH);
        assertNotNull(idxFolder);
        
        String toFind = "DemoHashMap";
        MyDocIndexItemConsumer diiConsumer = new MyDocIndexItemConsumer();
        SearchThreadJdk12 search = new SearchThreadJdk12(toFind, idxFolder, diiConsumer, true);
        search.run(); // not go() since we do not want to post the task to another thread
        assertTrue("not finished", diiConsumer.isFinished);
        assertEquals("search result", 2, diiConsumer.l.size());
        
        // class DemoHashMap<K,V>
        DocIndexItem dii = (DocIndexItem) diiConsumer.l.get(0);
        assertEquals("field", "DemoHashMap", dii.getField());
        assertEquals("declaring class", "java.util", dii.getDeclaringClass());
        assertEquals("remark", " - Class in java.util", dii.getRemark());
        assertEquals("package", "java.util.", dii.getPackage());
        assertTrue("url", dii.getURL().toString().endsWith("api/java/util/DemoHashMap.html"));
        // generic constructor DemoHashMap(Map<? extends K, ? extends V>)
        dii = (DocIndexItem) diiConsumer.l.get(1);
        assertEquals("field", "DemoHashMap(Map<? extends K, ? extends V>)", dii.getField());
        assertEquals("declaring class", "DemoHashMap", dii.getDeclaringClass());
        assertEquals("remark", " - Constructor for class java.util.DemoHashMap", dii.getRemark());
        assertEquals("package", "java.util.", dii.getPackage());
        assertTrue("url", dii.getURL().toString().endsWith("api/java/util/DemoHashMap.html#DemoHashMap(java.util.Map)"));
    }

    public void testSearchInJDK15_Method() throws Exception {
        FileObject idxFolder = fs.findResource(JDK15_INDEX_PATH);
        assertNotNull(idxFolder);
        
        String toFind = "damageLineRange";
        MyDocIndexItemConsumer diiConsumer = new MyDocIndexItemConsumer();
        SearchThreadJdk12 search = new SearchThreadJdk12(toFind, idxFolder, diiConsumer, true);
        search.run(); // not go() since we do not want to post the task to another thread
        assertTrue("not finished", diiConsumer.isFinished);
        assertEquals("search result", 1, diiConsumer.l.size());
        
        // damageLineRange(int, int, Shape, Component)
        DocIndexItem dii = (DocIndexItem) diiConsumer.l.get(0);
        assertEquals("field", "damageLineRange(int, int, Shape, Component)", dii.getField());
        assertEquals("declaring class", "PlainView", dii.getDeclaringClass());
        assertEquals("remark", " - Method in class javax.swing.text.PlainView", dii.getRemark());
        assertEquals("package", "javax.swing.text.", dii.getPackage());
        assertTrue("url", dii.getURL().toString().endsWith("api/javax/swing/text/PlainView.html#damageLineRange(int, int, java.awt.Shape, java.awt.Component)"));
    }

    public void testSearchInJDK15_Variables() throws Exception {
        FileObject idxFolder = fs.findResource(JDK15_INDEX_PATH);
        assertNotNull(idxFolder);
        
        String toFind = "darkShadow";
        MyDocIndexItemConsumer diiConsumer = new MyDocIndexItemConsumer();
        SearchThreadJdk12 search = new SearchThreadJdk12(toFind, idxFolder, diiConsumer, true);
        search.run(); // not go() since we do not want to post the task to another thread
        assertTrue("not finished", diiConsumer.isFinished);
        assertEquals("search result", 4, diiConsumer.l.size());
        
        // darkShadow
        DocIndexItem dii = (DocIndexItem) diiConsumer.l.get(0);
        assertEquals("field", "darkShadow", dii.getField());
        assertEquals("declaring class", "BasicBorders.ButtonBorder", dii.getDeclaringClass());
        assertEquals("remark", " - Variable in class javax.swing.plaf.basic.BasicBorders.ButtonBorder", dii.getRemark());
        assertEquals("package", "javax.swing.plaf.basic.", dii.getPackage());
        assertTrue("url", dii.getURL().toString().endsWith("api/javax/swing/plaf/basic/BasicBorders.ButtonBorder.html#darkShadow"));
        
        // darkShadowColor - static variable
        dii = (DocIndexItem) diiConsumer.l.get(3);
        assertEquals("field", "darkShadowColor", dii.getField());
        assertEquals("declaring class", "MetalSliderUI", dii.getDeclaringClass());
        assertEquals("remark", " - Static variable in class javax.swing.plaf.metal.MetalSliderUI", dii.getRemark());
        assertEquals("package", "javax.swing.plaf.metal.", dii.getPackage());
        assertTrue("url", dii.getURL().toString().endsWith("api/javax/swing/plaf/metal/MetalSliderUI.html#darkShadowColor"));
    }

    public void testSearchInJDK15_Exception() throws Exception {
        FileObject idxFolder = fs.findResource(JDK15_INDEX_PATH);
        assertNotNull(idxFolder);
        
        String toFind = "DataFormatException";
        MyDocIndexItemConsumer diiConsumer = new MyDocIndexItemConsumer();
        SearchThreadJdk12 search = new SearchThreadJdk12(toFind, idxFolder, diiConsumer, true);
        search.run(); // not go() since we do not want to post the task to another thread
        assertTrue("not finished", diiConsumer.isFinished);
        assertEquals("search result", 3, diiConsumer.l.size());
        
        // DataFormatException
        DocIndexItem dii = (DocIndexItem) diiConsumer.l.get(0);
        assertEquals("field", "DataFormatException", dii.getField());
        assertEquals("declaring class", "java.util.zip", dii.getDeclaringClass());
        assertEquals("remark", " - Exception in java.util.zip", dii.getRemark());
        assertEquals("package", "java.util.zip.", dii.getPackage());
        assertTrue("url", dii.getURL().toString().endsWith("api/java/util/zip/DataFormatException.html"));
        
        // DataFormatException(String) - constructor
        dii = (DocIndexItem) diiConsumer.l.get(2);
        assertEquals("field", "DataFormatException(String)", dii.getField());
        assertEquals("declaring class", "DataFormatException", dii.getDeclaringClass());
        assertEquals("remark", " - Constructor for exception java.util.zip.DataFormatException", dii.getRemark());
        assertEquals("package", "java.util.zip.", dii.getPackage());
        assertTrue("url", dii.getURL().toString().endsWith("api/java/util/zip/DataFormatException.html#DataFormatException(java.lang.String)"));
    }
    
    public void testSearchInJDK15_Interface() throws Exception {
        FileObject idxFolder = fs.findResource(JDK15_INDEX_PATH);
        assertNotNull(idxFolder);
        
        String toFind = "DatabaseMetaData";
        MyDocIndexItemConsumer diiConsumer = new MyDocIndexItemConsumer();
        SearchThreadJdk12 search = new SearchThreadJdk12(toFind, idxFolder, diiConsumer, true);
        search.run(); // not go() since we do not want to post the task to another thread
        assertTrue("not finished", diiConsumer.isFinished);
        assertEquals("search result", 1, diiConsumer.l.size());
        
        // DatabaseMetaData
        DocIndexItem dii = (DocIndexItem) diiConsumer.l.get(0);
        assertEquals("field", "DatabaseMetaData", dii.getField());
        assertEquals("declaring class", "java.sql", dii.getDeclaringClass());
        assertEquals("remark", " - Interface in java.sql", dii.getRemark());
        assertEquals("package", "java.sql.", dii.getPackage());
        assertTrue("url", dii.getURL().toString().endsWith("api/java/sql/DatabaseMetaData.html"));
    }
    
    public void testSearchInJDK15_Enum() throws Exception {
        FileObject idxFolder = fs.findResource(JDK15_INDEX_PATH);
        assertNotNull(idxFolder);
        
        String toFind = "DemoMemoryType";
        MyDocIndexItemConsumer diiConsumer = new MyDocIndexItemConsumer();
        SearchThreadJdk12 search = new SearchThreadJdk12(toFind, idxFolder, diiConsumer, true);
        search.run(); // not go() since we do not want to post the task to another thread
        assertTrue("not finished", diiConsumer.isFinished);
        assertEquals("search result", 1, diiConsumer.l.size());
        
        // DemoMemoryType
        DocIndexItem dii = (DocIndexItem) diiConsumer.l.get(0);
        assertEquals("field", "DemoMemoryType", dii.getField());
        assertEquals("declaring class", "java.lang.management", dii.getDeclaringClass());
        assertEquals("remark", " - Enum in java.lang.management", dii.getRemark());
        assertEquals("package", "java.lang.management.", dii.getPackage());
        assertTrue("url", dii.getURL().toString().endsWith("api/java/lang/management/DemoMemoryType.html"));
    }
    
    public void testSearchInJDK15_AnnotationType() throws Exception {
        FileObject idxFolder = fs.findResource(JDK15_INDEX_PATH);
        assertNotNull(idxFolder);
        
        String toFind = "Deprecated";
        MyDocIndexItemConsumer diiConsumer = new MyDocIndexItemConsumer();
        SearchThreadJdk12 search = new SearchThreadJdk12(toFind, idxFolder, diiConsumer, true);
        search.run(); // not go() since we do not want to post the task to another thread
        assertTrue("not finished", diiConsumer.isFinished);
        assertEquals("search result", 1, diiConsumer.l.size());
        
        // Deprecated
        DocIndexItem dii = (DocIndexItem) diiConsumer.l.get(0);
        assertEquals("field", "Deprecated", dii.getField());
        assertEquals("declaring class", "java.lang", dii.getDeclaringClass());
        assertEquals("remark", " - Annotation Type in java.lang", dii.getRemark());
        assertEquals("package", "java.lang.", dii.getPackage());
        assertTrue("url", dii.getURL().toString().endsWith("api/java/lang/Deprecated.html"));
    }
    
    private static final class MyDocIndexItemConsumer implements IndexSearchThread.DocIndexItemConsumer {
        boolean isFinished = false;
        List l = new LinkedList();
            
        public void addDocIndexItem(DocIndexItem dii) {
//            try {
//            System.out.println("dc: " + dii.getDeclaringClass() + ", field: " + dii.getField() +
//                    ", pkg: " + dii.getPackage() + ", remark: " + dii.getRemark() + ", url: " + dii.getURL().toString());
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            l.add(dii);
        }

        public void indexSearchThreadFinished(IndexSearchThread ist) {
//            System.out.println("-------------------------------");
            isFinished = true;
        }
    }
}
