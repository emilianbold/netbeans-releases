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
