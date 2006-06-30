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

package org.netbeans.modules.xml.api.model;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import junit.framework.*;
import org.netbeans.ProxyClassLoader;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import org.openide.filesystems.XMLFileSystem;
import org.openide.util.Lookup;
import org.xml.sax.InputSource;

/**
 * The test always fails because you cannot register
 * test class in lookup using layer. I would like to
 * see FolderLookup tests for system FS.
 * <p>
 * Last test version passes but kills system class loader.
 *
 * @author Petr Kuzel
 */
public class GrammarQueryManagerTest extends TestCase {
    
    public GrammarQueryManagerTest(java.lang.String testName) {
        super(testName);
    }
    
    /** Test of getDefault method, of class org.netbeans.modules.xml.text.completion.api.GrammarQueryManager. */
    public void testGetDefault() throws Exception {
        System.out.println("testGetDefault");

        FileSystem def = Repository.getDefault().getDefaultFileSystem();
        org.netbeans.core.projects.SystemFileSystem system =
            (org.netbeans.core.projects.SystemFileSystem) def;
        FileSystem[] original = system.getLayers();
        ProxyClassLoader testLoader = new ProxyClassLoader(
            new ClassLoader[] {SampleGrammarQueryManager.class.getClassLoader()}
        );
                
        try {
            
            // modify default FS content
            
            URL source = getClass().getResource("data/filesystem.xml");
            XMLFileSystem xmlfs = new XMLFileSystem(source);        
            List layers = new ArrayList(Arrays.asList(original));            
            layers.add(xmlfs);
            FileSystem[] fss = (FileSystem[]) layers.toArray(new FileSystem[0]);
            system.setLayers(fss);
            
            // adjust system classloader
            
            ClassLoader loader = (ClassLoader) Lookup.getDefault().lookup(ClassLoader.class);
            ProxyClassLoader systemLoader = (ProxyClassLoader) loader;
            systemLoader.append(new ClassLoader[] {testLoader});
            
            // test
            
            GrammarQueryManager manager = GrammarQueryManager.getDefault();
            GrammarEnvironment env = new GrammarEnvironment(org.openide.util.Enumerations.empty(), new InputSource(), null);
            Enumeration trigger = manager.enabled(env);
            assertTrue("No grammar found!", trigger!=null);
        } finally {
            
            // rollback
            
            system.setLayers(original);
            
            testLoader.destroy();
        }        
    }
    
}
