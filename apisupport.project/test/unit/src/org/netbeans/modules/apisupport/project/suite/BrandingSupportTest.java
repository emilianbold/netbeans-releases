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

package org.netbeans.modules.apisupport.project.suite;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.ui.customizer.SuitePropertiesTest;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Radek Matous
 */
public class BrandingSupportTest extends TestBase {
    private BrandingSupport instance = null;
    
    public BrandingSupportTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        File suiteDir  = new File(getWorkDir(), "testSuite");
        SuiteProjectGenerator.createSuiteProject(suiteDir, NbPlatform.PLATFORM_ID_DEFAULT);
        FileObject fo = FileUtil.toFileObject(suiteDir);
        SuiteProject suitePrj = (SuiteProject) ProjectManager.getDefault().findProject(fo);
        assertNotNull(suitePrj);
        
        instance = BrandingSupport.getInstance(SuitePropertiesTest.getSuiteProperties(suitePrj));
        
    }
    
    public void testBranding1() throws IOException {
        assertFalse(instance.getBrandingRoot().exists());
        Set keys = new HashSet(Arrays.asList(new String[]{"CTL_About_Title"}));
        implOfBundleKeyTest("org.netbeans.core.startup",
                "org/netbeans/core/startup/Bundle.properties",keys, "About");
    }
    
    public void testBranding2() throws IOException {
        assertFalse(instance.getBrandingRoot().exists());
        Set keys = new HashSet(Arrays.asList(new String[]{"CTL_About_Title"}));
        implOfBundleKeyTest("org.netbeans.core.startup", null,keys, "About");
    }
    
    
    public void testBranding3() throws IOException {
        assertFalse(instance.getBrandingRoot().exists());
        Set keys = new HashSet(Arrays.asList(new String[]{"LBL_ProductInformation"}));
        implOfBundleKeyTest("org.netbeans.core",
                "org/netbeans/core/ui/Bundle.properties", keys, "NetBeans Product Information");
    }

    public void testBranding4() throws IOException {
        assertFalse(instance.getBrandingRoot().exists());
        Set keys = new HashSet(Arrays.asList(new String[]{"CTL_MainWindow_Title"}));
        implOfBundleKeyTest("org.netbeans.core.windows",
                "org/netbeans/core/windows/view/ui/Bundle.properties", keys, "NetBeans Platform {0}");
    }
    
    public void testBrandingFile() throws IOException {
        assertFalse(instance.getBrandingRoot().exists());
        assertNotNull(instance.getBrandedFiles());
        assertEquals(0,instance.getBrandedFiles().size());
        BrandingSupport.BrandedFile bFile =
                instance.getBrandedFile("org.netbeans.core.startup","org/netbeans/core/startup/splash.gif");
        
        BrandingSupport.BrandedFile bFile2 =
                instance.getBrandedFile("org.netbeans.core.startup","org/netbeans/core/startup/splash.gif");
        
        assertEquals(bFile2, bFile);
        assertEquals(bFile2.getBrandingSource(), bFile.getBrandingSource());
        assertFalse(bFile.isModified());        
        
        assertNotNull(bFile);
        assertEquals(0,instance.getBrandedFiles().size());
        assertFalse(instance.isBranded(bFile));
        instance.brandFile(bFile);
        assertFalse(bFile.isModified());        
        
        assertFalse(instance.isBranded(bFile));
        assertEquals(0,instance.getBrandedFiles().size());
        
        File newSource = createNewSource(bFile);
        assertEquals(0,instance.getBrandedFiles().size());
        
        bFile.setBrandingSource(newSource);
        assertTrue(bFile.isModified());        
        
        assertEquals(0,instance.getBrandedFiles().size());
        instance.brandFile(bFile);
        assertFalse(bFile.isModified());        
        
        
        assertEquals(1,instance.getBrandedFiles().size());
        assertTrue(instance.isBranded(bFile));
        assertEquals(bFile2, bFile);
        assertFalse(bFile2.getBrandingSource().equals(bFile.getBrandingSource()));

        
        
    }
    
    private File createNewSource(final BrandingSupport.BrandedFile bFile) throws MalformedURLException, FileNotFoundException, IOException {
        OutputStream os = null;
        InputStream is = null;
        File newSource = new File(getWorkDir(),"newSource.gif");
        
        try {
            
            os = new FileOutputStream(newSource);
            is = bFile.getBrandingSource().openStream();
            FileUtil.copy(is,os);
        } finally  {
            if (is != null) {
                is.close();
            }
            if (os != null) {
                os.close();
            }
        }
        return newSource;
    }
    
    
    private void implOfBundleKeyTest(final String moduleCodeNameBase, final String bundleEntry, final Set keys, String expectedValue) throws IOException {
        Set bKeys;
        if (bundleEntry != null) {
            bKeys= instance.getBundleKeys(moduleCodeNameBase,bundleEntry,keys);
        } else {
            bKeys= instance.getLocalizingBundleKeys(moduleCodeNameBase,keys);
        }
        
        assertNotNull(bKeys);
        assertEquals(1, bKeys.size());
        
        BrandingSupport.BundleKey bKey = (BrandingSupport.BundleKey) bKeys.toArray()[0];
        assertFalse(instance.isBranded(bKey));
        assertFalse(instance.isBranded(bKey.getModuleEntry()));
        assertFalse(instance.getBrandingRoot().exists());
        assertFalse(instance.getModuleEntryDirectory(bKey.getModuleEntry()).exists());
        assertNotNull(instance.getBrandedBundleKeys());
        assertFalse(instance.getBrandedBundleKeys().contains(bKey));
        assertEquals(expectedValue, bKey.getValue());
        
        instance.brandBundleKeys(bKeys);
        assertFalse(instance.isBranded(bKey));
        assertFalse(instance.isBranded(bKey.getModuleEntry()));
        assertFalse(instance.getBrandingRoot().exists());
        assertFalse(instance.getModuleEntryDirectory(bKey.getModuleEntry()).exists());
        assertNotNull(instance.getBrandedBundleKeys());
        assertFalse(instance.getBrandedBundleKeys().contains(bKey));
        assertEquals(expectedValue, bKey.getValue());
        assertFalse(bKey.isModified());        
        
        bKey.setValue("brandedValue");
        assertTrue(bKey.isModified());                
        instance.brandBundleKeys(bKeys);
        assertFalse(bKey.isModified());        
        
        assertTrue(instance.isBranded(bKey));
        assertTrue(instance.isBranded(bKey.getModuleEntry()));
        assertTrue(instance.getBrandingRoot().exists());
        assertTrue(instance.getModuleEntryDirectory(bKey.getModuleEntry()).exists());
        assertNotNull(instance.getBrandedBundleKeys());
        assertTrue(instance.getBrandedBundleKeys().contains(bKey));
        assertEquals("brandedValue", bKey.getValue());
        
    }
    
}
