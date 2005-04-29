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

package org.openide.filesystems;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author  rm111737
 */
public class TestUtilHid {

    private static int cnt = 0;
    private static NumberFormat cntFormat = new DecimalFormat("000");
    public  static final File locationOfTempFolder (String name) throws IOException {
        name += cntFormat.format(cnt++);
        String property = System.getProperty("workdir");

        File workdir = (property == null) ? null: new File (property);
        File tmpdir = (workdir != null) ? workdir : new File(System.getProperty("java.io.tmpdir"), "fstests");
        tmpdir.mkdirs();
        if (!tmpdir.isDirectory()) throw new IOException("Could not make: " + tmpdir);
        File tmp = File.createTempFile(name,null, tmpdir);
        tmp.delete();
        tmp = new File(tmp.getParent(),name);
        return tmp;
    }

    /** @return  URL to folder where should be placed tested data */
    public final static URL getResourceContext () {
        //System.out.println("getResourceContext: " + FileSystemFactoryHid.class.getResource("../data"));
        return FileSystemFactoryHid.class.getResource("../data/");
    }
    
    /** It may be helpful to delete resursively Files */
    public final static boolean  deleteFolder (File file)  throws IOException{
        boolean ret = file.delete();
        
        if (ret) {
            return true;
        }
        
        if (! file.exists()) {
            return false;
        }
        
        if (file.isDirectory()) {
            // first of all delete whole content
            File[] arr = file.listFiles();
            for (int i = 0; i < arr.length; i++) {
                if (deleteFolder (arr[i]) != true) {
                    throw new IOException ("Cannot delete: "+ arr[i]);
                    //return false;
                }
            }
        }
        
        return (file.delete() ? true : false);
    }    
    
    /**
     * XXX this method should be package-private: non-FS-testing code should instead use
     * {@link NbTestCase#getWorkDir} and call either
     * {@link #createLocalFileSystem(File,String[])}
     * or use FileUtil.toFileObject with masterfs in CP
     */
    public final static FileSystem createLocalFileSystem(String testName, String[] resources) throws IOException {
        File mountPoint = locationOfLFSTempFolder(testName);
        return createLocalFileSystem(mountPoint, resources);

    }

    public static FileSystem createLocalFileSystem(File mountPoint, String[] resources) throws IOException {
        mountPoint.mkdir();
        
        for (int i = 0; i < resources.length; i++) {                        
            File f = new File (mountPoint,resources[i]);
            if (f.isDirectory() || resources[i].endsWith("/")) {
                f.mkdirs();
            }
            else {
                f.getParentFile().mkdirs();
                try {
                    f.createNewFile();
                } catch (IOException iex) {
                    throw new IOException ("While creating " + resources[i] + " in " + mountPoint.getAbsolutePath() + ": " + iex.toString() + ": " + f.getAbsolutePath() + " with resource list: " + Arrays.asList(resources));
                }
            }
        }
        
        LocalFileSystem lfs = new StatusFileSystem();
        try {
        lfs.setRootDirectory(mountPoint);
        } catch (Exception ex) {}
        
        return lfs;
    }

    public static File locationOfLFSTempFolder(String testName) throws IOException {        
        File mountPoint = TestUtilHid.locationOfTempFolder("lfstest");
        return mountPoint;
    }

    public final static  void destroyLocalFileSystem (String testName) throws IOException {            
        File mountPoint = TestUtilHid.locationOfTempFolder("lfstest");
        
        if (mountPoint.exists()) {
                if (TestUtilHid.deleteFolder(mountPoint) == false)
                    throw new IOException("Cannot delete test folder: " + mountPoint.toString());
        }
        
    }

    public final static void destroyXMLFileSystem(String testName) throws IOException {    
        File tempFile = TestUtilHid.locationOfTempFolder("xfstest");
        File xmlFile = new File (tempFile,"xfstest.xml");
        if (xmlFile.exists()) 
            xmlFile.delete();                            
    }
    
    
    public final static FileSystem createXMLFileSystem(String testName, String[] resources) throws IOException{
        File xmlFile = createXMLLayer(testName, resources);

        XMLFileSystem xfs = new XMLFileSystem  ();
        try {
            xfs.setXmlUrl(xmlFile.toURI().toURL());
        } catch (Exception ex) {}
        
        return xfs;
    }

    public static File createXMLLayer(String testName, String[] resources) throws IOException {
        File tempFile = TestUtilHid.locationOfTempFolder("xfstest");
        tempFile.mkdir();
        
        File xmlFile = new File (tempFile,"xfstest.xml");
        if (!xmlFile.exists()) {
            xmlFile.getParentFile().mkdirs();
            xmlFile.createNewFile();
        } 
        FileOutputStream xos = new FileOutputStream (xmlFile);        
        ResourceElement root =  new ResourceElement ("");        
        
        for (int i = 0; i < resources.length; i++)                         
            root.add (resources[i]);
       
        PrintWriter pw = new PrintWriter (xos); 
        pw.println("<filesystem>");
        testStructure (pw,root.getChildren () ,"  ");
        pw.println("</filesystem>");       
        pw.close();
        return xmlFile;
    }

    private  static void testStructure (PrintWriter pw,ResourceElement[] childern,String tab) {
        for (int i = 0; i < childern.length;i++) {
            ResourceElement[] sub = childern[i].getChildren ();
            if (sub.length != 0)
                pw.println(tab+"<folder name=\""+childern[i].getName ()+"\">" );            
            else
                pw.println(tab+"<file name=\""+childern[i].getName ()+"\">" );                            
            
            testStructure (pw,sub, tab+"  ");            
            
            if (sub.length != 0)
                pw.println(tab+"</folder>" );            
            else
                pw.println(tab+"</file>" );                            
        }
    }
    
    private  static class ResourceElement {
        String element;
        ResourceElement (String element) {
            //System.out.println(element);
            this.element = element;
        }
        Map children = new HashMap ();
        void add (String resource) {
            add (new StringTokenizer (resource,"/"));
        }
        private void add (Enumeration en) {
            //StringTokenizer tokens = StringTokenizer (resource);
            if (en.hasMoreElements()) {
                String chldElem = (String)en.nextElement();
                ResourceElement child = (ResourceElement)children.get(chldElem);
                if (child == null)
                    child = new ResourceElement(chldElem);
                children.put (chldElem,child);
                child.add (en);                
            }
        }
        ResourceElement[] getChildren () {
            int i = 0;
            ResourceElement[] retVal =  new ResourceElement[children.entrySet().size()];
            Iterator it = children.entrySet().iterator();
            while (it.hasNext()) {
                retVal[i++] = (ResourceElement)((Map.Entry)it.next()).getValue();
            }
                        
            return retVal;
        }
        
        String getName () {
            return element;
        }
    }    

    static class StatusFileSystem extends LocalFileSystem {
        Status status = new Status () {
            public String annotateName (String name, java.util.Set files) {
                return name;
            }

            public java.awt.Image annotateIcon (java.awt.Image icon, int iconType, java.util.Set files) {
                return icon;
            }
        };        
        
        public org.openide.filesystems.FileSystem.Status getStatus() {
            return status;
        }
        
    }
}
