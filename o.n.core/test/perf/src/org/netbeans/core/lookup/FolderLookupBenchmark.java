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


package org.netbeans.core.lookup;


import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.openide.debugger.Debugger;
import org.openide.execution.Executor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.XMLFileSystem;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.FolderLookup;
import org.openide.ServiceType;
import org.openide.TopManager;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;

import org.netbeans.core.xml.FileEntityResolver;
import org.netbeans.performance.Benchmark;

import org.xml.sax.SAXException;

import junit.framework.TestSuite;
import junit.textui.TestRunner;


public class FolderLookupBenchmark extends Benchmark {

    private static final String[] layerResources = new String[] {
        "/org/netbeans/core/resources/ant.xml", // NOI18N
        "/org/netbeans/core/resources/applet.xml", // NOI18N
        "/org/netbeans/core/resources/autoupdate.xml", // NOI18N
        "/org/netbeans/core/resources/beans.xml", // NOI18N
        "/org/netbeans/core/resources/core.xml", // NOI18N
        "/org/netbeans/core/resources/debuggercore.xml", // NOI18N
        "/org/netbeans/core/resources/debuggerjpda.xml", // NOI18N
        "/org/netbeans/core/resources/debuggertools.xml", // NOI18N
        "/org/netbeans/core/resources/editor.xml", // NOI18N
        "/org/netbeans/core/resources/extbrowser.xml", // NOI18N
        "/org/netbeans/core/resources/form.xml", // NOI18N
        "/org/netbeans/core/resources/html.xml", // NOI18N
        "/org/netbeans/core/resources/httpserver.xml", // NOI18N
        "/org/netbeans/core/resources/i18n.xml", // NOI18N
        "/org/netbeans/core/resources/icebrowser.xml", // NOI18N
        "/org/netbeans/core/resources/image.xml", // NOI18N
        "/org/netbeans/core/resources/j2ee.xml", // NOI18N
        "/org/netbeans/core/resources/jarpackager.xml", // NOI18N
        "/org/netbeans/core/resources/javacvs.xml", // NOI18N
        "/org/netbeans/core/resources/javadoc.xml", // NOI18N
        "/org/netbeans/core/resources/java.xml", // NOI18N
        "/org/netbeans/core/resources/jndi.xml", // NOI18N
        "/org/netbeans/core/resources/objectbrowser.xml", // NOI18N
        "/org/netbeans/core/resources/projects.xml", // NOI18N
        "/org/netbeans/core/resources/properties.xml", // NOI18N
        "/org/netbeans/core/resources/rmi.xml", // NOI18N
        "/org/netbeans/core/resources/scripting.xml", // NOI18N
        "/org/netbeans/core/resources/text.xml", // NOI18N
        "/org/netbeans/core/resources/usersguide.xml", // NOI18N
        "/org/netbeans/core/resources/utilities.xml", // NOI18N
        "/org/netbeans/core/resources/vcscore.xml", // NOI18N
        "/org/netbeans/core/resources/vcsgeneric.xml", // NOI18N
        "/org/netbeans/core/resources/web-core.xml", // NOI18N
        "/org/netbeans/core/resources/web-templates.xml", // NOI18N
        "/org/netbeans/core/resources/web-tomcat.xml" // NOI18N
    };
    
    /** Instance of folder lookup */
    private FolderLookup fl;

    /** Data folder on which to provide lookup. */
    private DataFolder df;
    
    /** Number of found instances. */
//    private int result = -1;

    
    public FolderLookupBenchmark(java.lang.String testName) {
        super(testName);

        System.err.println("TopManager="+TopManager.getDefault()); // TEMP
        System.err.println("Lookup="+Lookup.getDefault()); // TEMP
    }
    
    
    /** Runs the test suite. */
    public static void main(String[] args) {
        TestRunner.run(new TestSuite(FolderLookupBenchmark.class));
    }
    

    /** Creates XML file system (from core mf-layer.xml) on which to provide lookup. */
    protected void setUp () 
    throws java.net.MalformedURLException, 
            SAXException, 
            DataObjectNotFoundException {

        List systems = new ArrayList(layerResources.length);
        
        for(int i = 0; i < layerResources.length; i++) {
            URL url = TopManager.getDefault().currentClassLoader().getResource(layerResources[i]);
            
            systems.add(new XMLFileSystem(url));
        }
        
        FileObject services = new MultiFileSystem((FileSystem[])systems.toArray(new FileSystem[0])).getRoot().getFileObject("Services");
        
        df = (DataFolder)DataObject.find(services);
    }
    
    /** Clears the lookup. */
    protected void tearDown () {
        fl = null;
        df = null;
//        result = -1;
    }

/*    private void findTemplate(Class clazz) {
        fl = new FolderLookup(df);

        result = fl.getLookup().lookup(new Lookup.Template(clazz)).allInstances().size();
    }
 */
    
    /** Test to find the first registered object. */
    public void testCreateFolderLookup() {
        fl = new FolderLookup(df);

        fl.getLookup();
        fl.instanceFinished();
    }
    
/*    public void testFindSerializable() {
        findTemplate(Serializable.class);
    }

    public void testFindService() {
        findTemplate(ServiceType.class);
    }

    public void testFindCompiler() {
        findTemplate(Compiler.class);
    }

    public void testFindExecutor() {
        findTemplate(Executor.class);
    }

    public void testFindDebugger() {
        findTemplate(Debugger.class);
    }
 */

}
