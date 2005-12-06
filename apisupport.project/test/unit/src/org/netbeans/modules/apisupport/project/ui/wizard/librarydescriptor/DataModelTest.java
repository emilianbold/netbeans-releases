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

package org.netbeans.modules.apisupport.project.ui.wizard.librarydescriptor;

import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.layers.LayerTestBase;
import org.netbeans.modules.apisupport.project.layers.LayerUtils;
import org.netbeans.modules.project.uiapi.ProjectChooserFactory;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;

/**
 * Tests {@link DataModel}.
 *
 * @author Radek Matous
 */
public class DataModelTest extends LayerTestBase {
    public DataModelTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        TestBase.initializeBuildProperties(getWorkDir());
        LibraryManager lbm = LibraryManager.getDefault();
    }
    
    public void testValidityOfDataModel() throws Exception {
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        WizardDescriptor wd = new WizardDescriptor(new Panel[] {});
        wd.putProperty(ProjectChooserFactory.WIZARD_KEY_PROJECT, project);
        NewLibraryDescriptor.DataModel data = new NewLibraryDescriptor.DataModel(wd);
        
        assertEquals(project, data.getProject());
        
        assertFalse(data.isValidLibraryDisplayName());
        assertFalse(data.isValidLibraryName());
        
        data.setLibraryName("");
        assertFalse(data.isValidLibraryName());

        data.setLibraryDisplayName("");
        assertFalse(data.isValidLibraryDisplayName());
        
        data.setLibraryName("mylibrary");
        assertTrue(data.isValidLibraryName());

        data.setLibraryDisplayName("mylibrary is great");
        assertTrue(data.isValidLibraryDisplayName());
        
        
        assertFalse(data.libraryAlreadyExists());
        LayerUtils.LayerHandle h = LayerUtils.layerForProject(data.getProject());
        FileSystem fs = h.layer(true);
        FileObject fo = FileUtil.createData(fs.getRoot(),CreatedModifiedFilesProvider.getLibraryDescriptorEntryPath(data.getLibraryName()));
        assertNotNull(fo);
        assertTrue(data.libraryAlreadyExists());
    }    
}

