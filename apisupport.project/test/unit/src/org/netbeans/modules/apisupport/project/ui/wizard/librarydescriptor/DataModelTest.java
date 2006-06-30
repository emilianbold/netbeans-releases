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

