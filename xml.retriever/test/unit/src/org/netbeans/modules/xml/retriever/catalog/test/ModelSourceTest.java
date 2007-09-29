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

/*
 * ModelSourceTest.java
 * JUnit based test
 *
 * Created on January 22, 2007, 6:38 PM
 */

package org.netbeans.modules.xml.retriever.catalog.test;

import java.io.File;
import java.io.IOException;
import junit.framework.*;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModelException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author girix
 */
public class ModelSourceTest extends TestCase {
    
    public ModelSourceTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
    }
    
    public void testModelSource(){
        
                /*
                 * Step1: Copy
                 * <nb_src>/xml/retriever/test/unit/src/org/netbeans/modules/xml/retriever/catalog/test/TestCatalogModel.java
                 * to your unit test area.
                 */
        
        
        
        
        
        
        
        
        
        
        /*
         *Step 2: IMPORTANT NOTE: also make sure that all the required jars are actually set in the unit test class path.
         * This is done by placing or appending to the property (for more accurate list, copy and use from:
         <nb_src>/xml/retriever/nbproject/project.properties)
         
         test.unit.cp.extra=\
            ${netbeans.dest.dir}/ide7/modules/org-netbeans-modules-xml-retriever.jar:\
            ${netbeans.dest.dir}/ide7/modules/org-netbeans-modules-xml-xdm.jar:\
            ${netbeans.dest.dir}/ide7/modules/org-netbeans-modules-xml-xam.jar:\
            ${netbeans.dest.dir}/ide7/modules/org-apache-xml-resolver.jar:\
            ${netbeans.dest.dir}/ide7/modules/org-netbeans-modules-editor.jar:\
            ${netbeans.dest.dir}/platform6/lib/org-openide-modules.jar:\
            ${netbeans.dest.dir}/ide7/modules/org-netbeans-modules-editor-util.jar:\
            ${netbeans.dest.dir}/ide7/modules/org-netbeans-modules-xml-text.jar:\
            ${netbeans.dest.dir}/ide7/modules/org-netbeans-modules-xml-core.jar:\
            ${netbeans.dest.dir}/ide7/modules/org-netbeans-modules-editor-lib.jar:\
            ${netbeans.dest.dir}/ide7/modules/org-netbeans-modules-projectapi.jar:\
            ${netbeans.dest.dir}/platform6/modules/org-netbeans-modules-masterfs.jar:\
            ${netbeans.dest.dir}/platform6/modules/org-openide-windows.jar:\
            ${netbeans.dest.dir}/platform6/modules/org-openide-dialogs.jar:\
            ${netbeans.dest.dir}/platform6/modules/org-openide-awt.jar:\
            ${netbeans.dest.dir}/platform6/modules/org-openide-options.jar:\
            ${netbeans.dest.dir}/platform6/modules/org-openide-loaders.jar:\
            ${netbeans.dest.dir}/platform6/core/org-openide-filesystems.jar:\
            ${netbeans.dest.dir}/platform6/modules/org-openide-nodes.jar:\
            ${netbeans.dest.dir}/platform6/modules/org-openide-text.jar:\
            ${netbeans.dest.dir}/platform6/lib/org-openide-util.jar
         */
        
        
        //To create a model source use this code
        
        //ModelSource ms = TestCatalogModel.getDefault().createTestModelSource(FileObject, editable);
        
        /*Sample code*/
        File file = null;
        try {
            file = File.createTempFile("modelsource", "deleteme");
            file.deleteOnExit();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        //create ur own file object here
        FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(file));
        ModelSource ms = null;
        try {
            ms = TestCatalogModel.getDefault().createTestModelSource(fo, true);
        } catch (CatalogModelException ex) {
            ex.printStackTrace();
        }
        
        System.out.println(ms.getLookup().lookup(FileObject.class));
        
    }
    
}
