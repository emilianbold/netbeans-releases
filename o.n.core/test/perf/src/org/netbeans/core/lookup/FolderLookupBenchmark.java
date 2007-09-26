/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */


package org.netbeans.core.lookup;


import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

//import org.openide.debugger.Debugger;
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
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;

import org.netbeans.core.NbTopManager;
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

        System.err.println("TopManager="+NbTopManager.get()); // TEMP
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
            // XXX
//            URL url = TopManager.getDefault().currentClassLoader().getResource(layerResources[i]);
            ClassLoader cl = (ClassLoader)Lookup.getDefault().lookup(ClassLoader.class);
            URL url = cl.getResource(layerResources[i]);
            
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
