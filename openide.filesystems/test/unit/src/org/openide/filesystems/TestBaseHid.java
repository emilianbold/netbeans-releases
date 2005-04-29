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
import java.net.URL;
import java.net.URLStreamHandlerFactory;
import java.util.*;

/**
 *
 * @author  vs124454, rm111737
 * @version 
 */
public abstract class TestBaseHid extends MultiThreadedTestCaseHid {
    /** Support for events*/
    protected  List fileChangedL = new ArrayList ();
    protected List fileDCreatedL  = new ArrayList ();
    protected  List fileFCreatedL  = new ArrayList ();
    protected  List fileDeletedL  = new ArrayList ();
    protected  List fileRenamedL  = new ArrayList ();   
    protected  List fileAttrChangedL  = new ArrayList ();       

    private FileChangeListener defListener;
    private String resourcePrefix = "";
    
    static {        
        URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory() {
            public java.net.URLStreamHandler createURLStreamHandler(String protocol) {
                if (protocol.equals("nbfs")) { // NOI18N
                    return FileUtil.nbfsURLStreamHandler();
                } 
                return null;
        }});
    }    
    
    
    /** Creates new FSTest */    
    public TestBaseHid(String name) {
        super(name);
    }
    
    /** first filesystem allTestedFS[0]*/
    protected FileSystem  testedFS;
    
    /** array of filesystems that can be used for tests. All filesystems should
     * satisfy requirements for resources @see getResources () */
    protected FileSystem  allTestedFS[];
    
    protected void setUp() throws Exception {                
        System.setProperty("workdir", getWorkDirPath());
        defListener = createFileChangeListener ();

        //FileSystemFactoryHid.destroyFileSystem (this.getName(),this);
        clearWorkDir();
        String[] resources = getResources (getName());        
        resourcePrefix = FileSystemFactoryHid.getResourcePrefix(this.getName(),this, resources);
        allTestedFS = FileSystemFactoryHid.createFileSystem(getName(),resources,this);        
        if (allTestedFS != null) testedFS = allTestedFS[0];
    }

    protected void tearDown() throws Exception {
        if (testedFS instanceof JarFileSystem) {
            testedFS.removeNotify();    
        }
        testedFS = null;
        allTestedFS = null;
    }

    protected final void registerDefaultListener (FileObject fo) {
        fo.addFileChangeListener(defListener);
    }
    
    protected final void registerDefaultListener (FileSystem fs) {
        fs.addFileChangeListener(defListener);
    }

    protected final void registerDefaultListener (Repository rep) {
        rep.addFileChangeListener(defListener);
    }

//
    private void reinitDefListener() {
        fileChangedL = new ArrayList ();
        fileDCreatedL  = new ArrayList ();
        fileFCreatedL  = new ArrayList ();
        fileDeletedL  = new ArrayList ();
        fileRenamedL  = new ArrayList ();   
        fileAttrChangedL  = new ArrayList ();
    }

    protected final void deregisterDefaultListener (FileObject fo) {
        reinitDefListener();
        fo.removeFileChangeListener(defListener);
    }

    protected final void deregisterDefaultListener (FileSystem fs) {
        reinitDefListener();        
        fs.removeFileChangeListener(defListener);
    }

    protected final void deregisterDefaultListener (Repository rep) {
        reinitDefListener();        
        rep.removeFileChangeListener(defListener);
    }
    
    
    /** Test can require some resources to be part of filesystem that will be tested
     * @return array of resources
     */
    protected abstract String[] getResources (String testName);// {return new String[] {};}
    
       
    public  final void fsTestFrameworkErrorAssert  (String message, boolean condition) {        
        fsAssert  ("Tests did not fail, but test framework contains errors: " + message,condition);
    }
    
    public  final void fsFail  (String message) {
        fail (message + " ["+ FileSystemFactoryHid.getTestClassName () + "]");
    }

    
    public  final void fsAssert  (String message, boolean condition) {
        assertTrue (message + " ["+ FileSystemFactoryHid.getTestClassName () + "]", condition);
    }
    
    public  final void fileChangedAssert  (String message, int expectedCount) {
        fileEventAssert (fileChangedL, message, expectedCount);        
    }

    public  final void fileDataCreatedAssert  (String message, int expectedCount) {
        fileEventAssert (fileDCreatedL, message, expectedCount);
    }

    public  final void fileFolderCreatedAssert  (String message, int expectedCount) {
        fileEventAssert (fileFCreatedL, message, expectedCount);
    }

    public  final void fileDeletedAssert  (String message, int expectedCount) {
        fileEventAssert (fileDeletedL, message, expectedCount);
    }
    
    public  final void fileRenamedAssert  (String message, int expectedCount) {
        fileEventAssert (fileRenamedL , message, expectedCount);
    }    
    
    public  final void fileAttributeChangedAssert (String message, int expectedCount) {
        fileEventAssert (fileAttrChangedL , message, expectedCount);        
    }
     
    private void fileEventAssert  (List list, String message, int expectedCount) {
        fsAssert (message+" Fired : " +list.size () + " ,but expected: " + expectedCount,expectedCount == list.size ()); 
    }
    
    protected FileChangeListener createFileChangeListener () {
     return new FileChangeAdapter () {
            public void fileChanged (FileEvent fe) {
                fileChangedL.add (fe);
            }
            public void fileDeleted (FileEvent fe) {
                fileDeletedL.add (fe);
            }
            public void fileFolderCreated (FileEvent fe) {
                fsAssert("Unexpected data file", fe.getFile().isFolder());
                fileFCreatedL.add (fe);
            }
            public void fileDataCreated (FileEvent fe) {
                fsAssert("Unexpected folder", fe.getFile().isData());                
                fileDCreatedL.add (fe);
            }
            public void fileRenamed (FileRenameEvent fe) {
                fileRenamedL.add (fe);
            }            
            
            public void fileAttributeChanged (FileAttributeEvent fe) {
                fileAttrChangedL.add (fe);
            }
        };   
    }

    protected String getResourcePrefix() {
        return resourcePrefix;
    }
}
