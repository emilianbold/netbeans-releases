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

package org.netbeans.modules.navigator;

import java.io.File;
import java.net.URL;
import java.util.List;
import javax.swing.JComponent;
import junit.framework.*;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;
import org.openide.util.Lookup;


/**
 *
 * @author Dafe Simonek
 */
public class NavigatorControllerTest extends TestCase {
    
    private static final String JAVA_DATA_TYPE = "text/marvelous/data_type";
    
    
    public NavigatorControllerTest(String testName) {
        super(testName);
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(NavigatorControllerTest.class);
        return suite;
    }

    public void testObtainProviders() throws Exception {
        System.out.println("Testing NavigatorController.obtainProviders");
        UnitTestUtils.prepareTest(new String [] { "/org/netbeans/modules/navigator/resources/NavigatorControllerTestProvider.xml" });
        URL url = NavigatorControllerTest.class.getResource("resources/sample_folder/subfolder1/subfolder2");
        assertNotNull("url not found.", url);

        FileUtil.setMIMEType("my_extension", "NavigatorControllerTest/TestMimeType");
        FileObject fo = URLMapper.findFileObject(url);
        FileObject[] fos = fo.getChildren();
        fo = fo.getFileObject("Nic.my_extension");
        assertNotNull("fo not found.", fo);
        
        FileObject foSubFolder1 = fo.getParent().getParent();
        FileObject foSampleFolder = foSubFolder1.getParent();
        DataObject dObj = DataObject.find(fo);
        DataFolder subFolder1 = (DataFolder)DataObject.find(foSubFolder1);
        DataFolder sampleFolder = (DataFolder)DataObject.find(foSampleFolder);
        DataShadow shadow1 = DataShadow.create(subFolder1, dObj);
        DataShadow shadow2 = DataShadow.create(sampleFolder, shadow1);
        
        System.out.println("Testing DataShadow resolvement...");
        // not really valid, uses impl fact that during obtainProviders,
        // NavigatorTC parameter will not be needed.
        NavigatorController nc = new NavigatorController(null);
        List result = nc.obtainProviders(shadow1.getNodeDelegate());
        assertNotNull("provider not found", result);
        assertEquals(1, result.size());
        assertTrue(result.get(0) instanceof TestJavaNavigatorPanel);
        
        result = nc.obtainProviders(shadow2.getNodeDelegate());
        assertNotNull("provider not found", result);
        assertEquals(1, result.size());
        assertTrue(result.get(0) instanceof TestJavaNavigatorPanel);
    }

    /** Dummy navigator panel provider, just for testing
     */ 
    public static final class TestJavaNavigatorPanel implements NavigatorPanel {
        
        public String getDisplayName () {
            return JAVA_DATA_TYPE;
        }
    
        public String getDisplayHint () {
            return null;
        }

        public JComponent getComponent () {
            return null;
        }

        public void panelActivated (Lookup context) {
        }

        public void panelDeactivated () {
        }
        
        public Lookup getLookup () {
            return null;
        }
        
    }
    
            
            
    
}
