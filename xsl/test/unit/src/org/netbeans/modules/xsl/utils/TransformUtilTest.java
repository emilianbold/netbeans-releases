/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.xsl.utils;

import java.io.*;
import java.net.*;
import java.beans.PropertyVetoException;

import junit.framework.*;
import org.netbeans.junit.*;

import org.openide.filesystems.*;
import org.openide.loaders.*;

/**
 *
 * @author Libor Kramolis
 */
public class TransformUtilTest extends NbTestCase {
    
    public TransformUtilTest(java.lang.String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new NbTestSuite(TransformUtilTest.class);
        return suite;
    }
    
    
    public void testIsXSLTransformation () throws Exception {
        System.out.println("testIsXSLTransformation");
  
        assertTrue (".xml document must NOT pass!", false==TransformUtil.isXSLTransformation (getDataObject ("doc.xml")));
        assertTrue (".xsl document MUST pass!",            TransformUtil.isXSLTransformation (getDataObject ("doc2xhtml.xsl")));
    }
    
    public void testGetURLName () throws Exception {
        System.out.println("testGetURLName");
        
        FileObject docXML = getFileObject("doc.xml");
        String docXMLName = TransformUtil.getURLName(docXML);
        System.out.println("    docXML: " + docXML + " => '" + docXMLName + "'");
        assertTrue ("URL should not contain nbsf://!",-1==docXMLName.indexOf("nbfs"));
    }

    public void testCreateURL () throws Exception {
        System.out.println("testCreateURL");
        
        URL dataURL = getClass().getResource("data/");
        URL docXMLURL = getClass().getResource("data/doc.xml");
        URL docDTDURL = getClass().getResource("data/doc.dtd");
        
        assertTrue ("Both URLs must be same!",            docXMLURL.sameFile (TransformUtil.createURL (dataURL, "doc.xml")));
        assertTrue ("Both URLs must be same!",            docXMLURL.sameFile (TransformUtil.createURL (docDTDURL, "doc.xml")));
        assertTrue ("Both URLs must be same!",            docXMLURL.sameFile (TransformUtil.createURL (docDTDURL, "../data/doc.xml")));
        assertTrue ("Both URLs must NOT be same!", false==docXMLURL.sameFile (TransformUtil.createURL (docDTDURL, "data/doc.xml")));
        assertTrue ("Both URLs must be same!",     false==docXMLURL.sameFile (TransformUtil.createURL (docDTDURL, docDTDURL.toExternalForm())));
    }

    public void testGetAssociatedStylesheet () throws Exception {
        System.out.println("testGetAssociatedStylesheet -- TBD");
    }
    
    public void testGuessOutputExt () throws Exception {
        System.out.println("testGuessOutputExt -- TBD");
    }
    
    public void testTransform () throws Exception {
        System.out.println("testTransform -- TBD");
    }
    
    //
    // utils
    //
    
    private FileObject getFileObject (String name) throws PropertyVetoException, IOException {
        URL url = getClass().getResource("data/" + name);
/*        FileSystem FS = getDataFileSystem();
        FileObject FO = FS.findResource (name);        
        return FO;*/
        
        FileObject[] fos = URLMapper.findFileObjects (url);
        return fos[0];
    }
    
    private DataObject getDataObject (String name) throws PropertyVetoException, IOException, DataObjectNotFoundException {
        FileObject FO = getFileObject (name);
        DataObject DO = DataObject.find (FO);
        
        return DO;
    }
   
/*    private FileSystem getDataFileSystem () throws PropertyVetoException, IOException {
        URL dataURL = getClass().getResource("data");
        String dataSysName = dataURL.toExternalForm();
        Repository repository = Repository.getDefault();
        FileSystem dataFS = repository.findFileSystem (dataSysName);
        
        if ( dataFS == null ) {
            LocalFileSystem locFS = new LocalFileSystem();
            locFS.setRootDirectory (new File (dataSysName));
            dataFS = locFS;
        }
        
        return dataFS;
    }*/
    
}
