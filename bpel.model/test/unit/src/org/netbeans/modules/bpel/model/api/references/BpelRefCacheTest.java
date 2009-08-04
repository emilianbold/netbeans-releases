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

package org.netbeans.modules.bpel.model.api.references;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import org.junit.After;
import org.netbeans.modules.bpel.model.TestCatalogModel;
import org.netbeans.modules.bpel.model.TestUtils;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.impl.BpelModelImpl;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.bpel.model.api.Process;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.ModelSource;
import org.netbeans.modules.xml.xam.locator.CatalogModel;

/**
 * The JUnit based test is intended to check the class RefCacheSupport.java
 *
 * @author Nikita Krjukov
 */
public class BpelRefCacheTest {
    
    private static String TEST_BPEL_PROJECT_ZIP = "resources/BpelRefCacheProject.zip"; // NOI18N

    private static String TEST_BPEL = "BpelRefCacheProject/src/Referencing.bpel"; // NOI18N
    private static String TEST_BAD_BPEL = "BpelRefCacheProject/src/Referencing_invalid.bpel"; // NOI18N

    private static String TEST_WSDL_IMPORT_DELETED = "BpelRefCacheProject/src/Referencing_wsdl_import_deleted.bpel"; // NOI18N
    private static String TEST_SCHEMA_IMPORT_DELETED = "BpelRefCacheProject/src/Referencing_schema_import_deleted.bpel"; // NOI18N

    private static String TEST_WSDL_IMPORT_ATTR_CHANGED = "BpelRefCacheProject/src/Referencing_wsdl_import_attr_changed.bpel"; // NOI18N
    private static String TEST_SCHEMA_IMPORT_ATTR_CHANGED = "BpelRefCacheProject/src/Referencing_schema_import_attr_changed.bpel"; // NOI18N

    private static String TEST_WSDL_IMPORT_ATTR_DELETED = "BpelRefCacheProject/src/Referencing_wsdl_import_attr_deleted.bpel"; // NOI18N
    private static String TEST_SCHEMA_IMPORT_ATTR_DELETED = "BpelRefCacheProject/src/Referencing_schema_import_attr_deleted.bpel"; // NOI18N

    private static String TEST_IMPORTED_WSDL_INVALID = "BpelRefCacheProject/src/Imported_Wsdl_invalid.wsdl"; // NOI18N
    private static String TEST_IMPORTED_SCHEMA_INVALID = "BpelRefCacheProject/src/Imported_Schema_invalid.xsd"; // NOI18N

    private static String TEST_IMPORTED_WSDL_TNS_CHANGED = "BpelRefCacheProject/src/Imported_Wsdl_tns_changed.wsdl"; // NOI18N
    private static String TEST_IMPORTED_SCHEMA_TNS_CHANGED = "BpelRefCacheProject/src/Imported_Schema_tns_changed.xsd"; // NOI18N

    private static String TEST_IMPORTED_WSDL_TNS_DELETED = "BpelRefCacheProject/src/Imported_Wsdl_tns_deleted.wsdl"; // NOI18N
    private static String TEST_IMPORTED_SCHEMA_TNS_DELETED = "BpelRefCacheProject/src/Imported_Schema_tns_deleted.xsd"; // NOI18N


    private static BpelModelImpl mBpelModel;

    public BpelRefCacheTest() {
    }

    /**
     * Removes models from memory.
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
        if (mBpelModel != null) {
            ModelSource mSource = mBpelModel.getModelSource();
            CatalogModel catalogModel = mSource.getLookup().lookup(CatalogModel.class);
            assert catalogModel instanceof TestCatalogModel;
            TestCatalogModel.class.cast(catalogModel).clearDocumentPool();
            //
            System.gc(); // it is required to clear schema models cache.
        }
    }

    /**
     * All tests uses the same BPEL project. It is reloaded for each test.
     */
    @Before
    public void init() {
        try {
            // mBpelModel = TestUtils.loadXamModel(TEST_BPEL, BpelModelImpl.class);
            mBpelModel = TestUtils.loadXamModel(TEST_BPEL_PROJECT_ZIP,
                    TEST_BPEL, BpelModelImpl.class);
        } catch (Exception ex) {
            assertTrue("Exception while loading BPEL", false);
        }
        assertNotNull(mBpelModel);
    }

    /**
     * Loads 2 referenced (Schema + WSDL).
     * RefCache shoul have 2 items after method is finished.
     *
     * @param sm
     * @return
     */
    private RefCacheSupport loadReferencedModels(BpelModel bModel) {
        assertNotNull(bModel);
        assert bModel instanceof BpelModelImpl;
        BpelModelImpl bmImpl = BpelModelImpl.class.cast(bModel);
        RefCacheSupport cache = bmImpl.getRefCacheSupport();
        //
        TestUtils.checkImports(bModel);
        //
        return cache;
    }

    /**
     * Checks if the cache is discarded after the owning BPEL model
     * becomes invalid.
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void testReferencingInvalid() throws Exception {
//        mBpelModel = TestUtils.loadXamModel(TEST_BPEL_PROJECT_ZIP,
//                TEST_BPEL, BpelModelImpl.class);
        RefCacheSupport cache = loadReferencedModels(mBpelModel);
        //
        assertEquals(2, cache.getCachedModelsSize());
        //
        TestUtils.setDocumentContentTo(mBpelModel,
                TEST_BPEL_PROJECT_ZIP, TEST_BAD_BPEL);
        try {
            mBpelModel.sync();
            assertFalse("Did not get IOException", true); // NOI18N
        } catch(IOException ioe) {
            assertEquals(mBpelModel.getState(), Model.State.NOT_WELL_FORMED);
        }
        //
        // Cache has to be empty after it was discarded
        assertEquals(0, cache.getCachedModelsSize());
    }

    /**
     * Checks if the imported wsdl model is deleted from the cache after
     * the corresponding import declaration is deleted.
     */
    @Test
    public void testDeleteWsdlImport() throws Exception {
//        mBpelModel = TestUtils.loadXamModel(TEST_BPEL_PROJECT_ZIP,
//                TEST_BPEL, BpelModelImpl.class);
        RefCacheSupport cache = loadReferencedModels(mBpelModel);
        //
        Process process = mBpelModel.getProcess();
        assertNotNull(process);
        //
        Import[] importsArr = process.getImports();
        assertEquals(2, importsArr.length);
        Import imp = importsArr[0];
        assertNotNull(imp);
        assertEquals(imp.getImportType(), Import.WSDL_IMPORT_TYPE);
        WSDLModel importedWsdl = cache.optimizedWsdlResolve(imp);
        assertNotNull(importedWsdl);
        //
        TestUtils.setDocumentContentTo(mBpelModel,
                TEST_BPEL_PROJECT_ZIP, TEST_WSDL_IMPORT_DELETED);
        try {
            mBpelModel.sync();
            assertEquals(mBpelModel.getState(), Model.State.VALID);
        } catch(IOException ioe) {
            assertFalse("IOException", false);  // NOI18N
        }
        //
        // Check the model's cache is updated
        assertFalse(cache.contains(importedWsdl));
    }

    /**
     * Checks if the imported schema model is deleted from the cache after
     * the corresponding import declaration is deleted.
     */
    @Test
    public void testDeleteSchemaImport() throws Exception {
//        mBpelModel = TestUtils.loadXamModel(TEST_BPEL_PROJECT_ZIP,
//                TEST_BPEL, BpelModelImpl.class);
        RefCacheSupport cache = loadReferencedModels(mBpelModel);
        //
        Process process = mBpelModel.getProcess();
        assertNotNull(process);
        //
        Import[] importsArr = process.getImports();
        assertEquals(2, importsArr.length);
        Import imp = importsArr[1];
        assertNotNull(imp);
        assertEquals(imp.getImportType(), Import.SCHEMA_IMPORT_TYPE);
        SchemaModel importedSchema = cache.optimizedSchemaResolve(imp);
        assertNotNull(importedSchema);
        //
        TestUtils.setDocumentContentTo(mBpelModel,
                TEST_BPEL_PROJECT_ZIP, TEST_SCHEMA_IMPORT_DELETED);
        try {
            mBpelModel.sync();
            assertEquals(mBpelModel.getState(), Model.State.VALID);
        } catch(IOException ioe) {
            assertFalse("IOException", false);  // NOI18N
        }
        //
        // Check the model's cache is updated
        assertFalse(cache.contains(importedSchema));
    }

    /**
     * Checks if the imported schema model is deleted from the cache after
     * an attribute is deleted from the corresponding import declaration.
     */
    @Test
    public void testSchemaImportAttrDelete() throws Exception {
//        mBpelModel = TestUtils.loadXamModel(TEST_BPEL_PROJECT_ZIP,
//                TEST_BPEL, BpelModelImpl.class);
        RefCacheSupport cache = loadReferencedModels(mBpelModel);
        //
        Process process = mBpelModel.getProcess();
        assertNotNull(process);
        //
        Import[] importsArr = process.getImports();
        assertEquals(2, importsArr.length);
        Import imp = importsArr[1];
        assertNotNull(imp);
        assertEquals(imp.getImportType(), Import.SCHEMA_IMPORT_TYPE);
        SchemaModel importedSchema = cache.optimizedSchemaResolve(imp);
        assertNotNull(importedSchema);
        //
        TestUtils.setDocumentContentTo(mBpelModel,
                TEST_BPEL_PROJECT_ZIP, TEST_SCHEMA_IMPORT_ATTR_DELETED);
        try {
            mBpelModel.sync();
            assertEquals(mBpelModel.getState(), Model.State.VALID);
        } catch(IOException ioe) {
            assertFalse("IOException", false);  // NOI18N
        }
        //
        // Check the model's cache is updated
        assertFalse(cache.contains(importedSchema));
    }

    /**
     * Checks if the imported schema model is deleted from the cache after
     * an attribute is changed at the corresponding import declaration.
     */
    @Test
    public void testSchemaImportAttrChanged() throws Exception {
//        mBpelModel = TestUtils.loadXamModel(TEST_BPEL_PROJECT_ZIP,
//                TEST_BPEL, BpelModelImpl.class);
        RefCacheSupport cache = loadReferencedModels(mBpelModel);
        //
        Process process = mBpelModel.getProcess();
        assertNotNull(process);
        //
        Import[] importsArr = process.getImports();
        assertEquals(2, importsArr.length);
        Import imp = importsArr[1];
        assertNotNull(imp);
        assertEquals(imp.getImportType(), Import.SCHEMA_IMPORT_TYPE);
        SchemaModel importedSchema = cache.optimizedSchemaResolve(imp);
        assertNotNull(importedSchema);
        //
        TestUtils.setDocumentContentTo(mBpelModel,
                TEST_BPEL_PROJECT_ZIP, TEST_SCHEMA_IMPORT_ATTR_CHANGED);
        try {
            mBpelModel.sync();
            assertEquals(mBpelModel.getState(), Model.State.VALID);
        } catch(IOException ioe) {
            assertFalse("IOException", false);  // NOI18N
        }
        //
        // Check the model's cache is updated
        assertFalse(cache.contains(importedSchema));
    }

    /**
     * Checks if the imported wsdl model is deleted from the cache after
     * an attribute is deleted from the corresponding import declaration.
     */
    @Test
    public void testWsdlImportAttrDelete() throws Exception {
//        mBpelModel = TestUtils.loadXamModel(TEST_BPEL_PROJECT_ZIP,
//                TEST_BPEL, BpelModelImpl.class);
        RefCacheSupport cache = loadReferencedModels(mBpelModel);
        //
        Process process = mBpelModel.getProcess();
        assertNotNull(process);
        //
        Import[] importsArr = process.getImports();
        assertEquals(2, importsArr.length);
        Import imp = importsArr[0];
        assertNotNull(imp);
        assertEquals(imp.getImportType(), Import.WSDL_IMPORT_TYPE);
        WSDLModel importedWsdl = cache.optimizedWsdlResolve(imp);
        assertNotNull(importedWsdl);
        //
        TestUtils.setDocumentContentTo(mBpelModel,
                TEST_BPEL_PROJECT_ZIP, TEST_WSDL_IMPORT_ATTR_DELETED);
        try {
            mBpelModel.sync();
            assertEquals(mBpelModel.getState(), Model.State.VALID);
        } catch(IOException ioe) {
            assertFalse("IOException", false);  // NOI18N
        }
        //
        // Check the model's cache is updated
        assertFalse(cache.contains(importedWsdl));
    }

    /**
     * Checks if the imported wsdl model is deleted from the cache after
     * an attribute is changed at the corresponding import declaration.
     */
    @Test
    public void testWsdlImportAttrChanged() throws Exception {
//        mBpelModel = TestUtils.loadXamModel(TEST_BPEL_PROJECT_ZIP,
//                TEST_BPEL, BpelModelImpl.class);
        RefCacheSupport cache = loadReferencedModels(mBpelModel);
        //
        Process process = mBpelModel.getProcess();
        assertNotNull(process);
        //
        Import[] importsArr = process.getImports();
        assertEquals(2, importsArr.length);
        Import imp = importsArr[0];
        assertNotNull(imp);
        assertEquals(imp.getImportType(), Import.WSDL_IMPORT_TYPE);
        WSDLModel importedWsdl = cache.optimizedWsdlResolve(imp);
        assertNotNull(importedWsdl);
        //
        TestUtils.setDocumentContentTo(mBpelModel,
                TEST_BPEL_PROJECT_ZIP, TEST_WSDL_IMPORT_ATTR_CHANGED);
        try {
            mBpelModel.sync();
            assertEquals(mBpelModel.getState(), Model.State.VALID);
        } catch(IOException ioe) {
            assertFalse("IOException", false);  // NOI18N
        }
        //
        // Check the model's cache is updated
        assertFalse(cache.contains(importedWsdl));
    }

    /**
     * Checks if the imported wsdl model is removed from the cache after
     * it becomes invalid.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testImportedWsdlInvalid() throws Exception {
//        mBpelModel = TestUtils.loadXamModel(TEST_BPEL_PROJECT_ZIP,
//                TEST_BPEL, BpelModelImpl.class);
        RefCacheSupport cache = loadReferencedModels(mBpelModel);
        //
        Process process = mBpelModel.getProcess();
        assertNotNull(process);
        //
        Import[] importsArr = process.getImports();
        assertEquals(2, importsArr.length);
        Import imp = importsArr[0];
        assertNotNull(imp);
        assertEquals(imp.getImportType(), Import.WSDL_IMPORT_TYPE);
        WSDLModel importedWsdl = cache.optimizedWsdlResolve(imp);
        assertNotNull(importedWsdl);
        //
        TestUtils.setDocumentContentTo(importedWsdl,
                TEST_BPEL_PROJECT_ZIP, TEST_IMPORTED_WSDL_INVALID);
        try {
            importedWsdl.sync();
            assertFalse("Did not get IOException", true); // NOI18N
        } catch(IOException ioe) {
            assertEquals(importedWsdl.getState(), Model.State.NOT_WELL_FORMED);
        }
        //
        // Check the model's cache is updated
        assertFalse(cache.contains(importedWsdl));
    }

    /**
     * Checks if the imported schema model is removed from the cache after
     * it becomes invalid.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testImportedSchemaInvalid() throws Exception {
//        mBpelModel = TestUtils.loadXamModel(TEST_BPEL_PROJECT_ZIP,
//                TEST_BPEL, BpelModelImpl.class);
        RefCacheSupport cache = loadReferencedModels(mBpelModel);
        //
        Process process = mBpelModel.getProcess();
        assertNotNull(process);
        //
        Import[] importsArr = process.getImports();
        assertEquals(2, importsArr.length);
        Import imp = importsArr[1];
        assertNotNull(imp);
        assertEquals(imp.getImportType(), Import.SCHEMA_IMPORT_TYPE);
        SchemaModel importedSchema = cache.optimizedSchemaResolve(imp);
        assertNotNull(importedSchema);
        //
        TestUtils.setDocumentContentTo(importedSchema,
                TEST_BPEL_PROJECT_ZIP, TEST_IMPORTED_SCHEMA_INVALID);
        try {
            importedSchema.sync();
            assertFalse("Did not get IOException", true); // NOI18N
        } catch(IOException ioe) {
            assertEquals(importedSchema.getState(), Model.State.NOT_WELL_FORMED);
        }
        //
        // Check the model's cache is updated
        assertFalse(cache.contains(importedSchema));
    }

    /**
     * Checks if the imported wsdl model is removed from the cache after
     * its targetNamespace has changed.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testImportedWsdlTargetNsChanged() throws Exception {
//        mBpelModel = TestUtils.loadXamModel(TEST_BPEL_PROJECT_ZIP,
//                TEST_BPEL, BpelModelImpl.class);
        RefCacheSupport cache = loadReferencedModels(mBpelModel);
        //
        Process process = mBpelModel.getProcess();
        assertNotNull(process);
        //
        Import[] importsArr = process.getImports();
        assertEquals(2, importsArr.length);
        Import imp = importsArr[0];
        assertNotNull(imp);
        assertEquals(imp.getImportType(), Import.WSDL_IMPORT_TYPE);
        WSDLModel importedWsdl = cache.optimizedWsdlResolve(imp);
        assertNotNull(importedWsdl);
        //
        TestUtils.setDocumentContentTo(importedWsdl,
                TEST_BPEL_PROJECT_ZIP, TEST_IMPORTED_WSDL_TNS_CHANGED);
        importedWsdl.sync();
        //
        // Check the model's cache is updated
        assertFalse(cache.contains(importedWsdl));
    }

    /**
     * Checks if the imported schema model is removed from the cache after
     * its targetNamespace has changed.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testImportedSchemaTargetNsChanged() throws Exception {
//        mBpelModel = TestUtils.loadXamModel(TEST_BPEL_PROJECT_ZIP,
//                TEST_BPEL, BpelModelImpl.class);
        RefCacheSupport cache = loadReferencedModels(mBpelModel);
        //
        Process process = mBpelModel.getProcess();
        assertNotNull(process);
        //
        Import[] importsArr = process.getImports();
        assertEquals(2, importsArr.length);
        Import imp = importsArr[1];
        assertNotNull(imp);
        assertEquals(imp.getImportType(), Import.SCHEMA_IMPORT_TYPE);
        SchemaModel importedSchema = cache.optimizedSchemaResolve(imp);
        assertNotNull(importedSchema);
        //
        TestUtils.setDocumentContentTo(importedSchema,
                TEST_BPEL_PROJECT_ZIP, TEST_IMPORTED_SCHEMA_TNS_CHANGED);
        importedSchema.sync();
        //
        // Check the model's cache is updated
        assertFalse(cache.contains(importedSchema));
    }

    /**
     * Checks if the imported wsdl model is removed from the cache after
     * its targetNamespace has deleted.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testImportedWsdlTargetNsDeleted() throws Exception {
//        mBpelModel = TestUtils.loadXamModel(TEST_BPEL_PROJECT_ZIP,
//                TEST_BPEL, BpelModelImpl.class);
        RefCacheSupport cache = loadReferencedModels(mBpelModel);
        //
        Process process = mBpelModel.getProcess();
        assertNotNull(process);
        //
        Import[] importsArr = process.getImports();
        assertEquals(2, importsArr.length);
        Import imp = importsArr[0];
        assertNotNull(imp);
        assertEquals(imp.getImportType(), Import.WSDL_IMPORT_TYPE);
        WSDLModel importedWsdl = cache.optimizedWsdlResolve(imp);
        assertNotNull(importedWsdl);
        //
        TestUtils.setDocumentContentTo(importedWsdl,
                TEST_BPEL_PROJECT_ZIP, TEST_IMPORTED_WSDL_TNS_DELETED);
        importedWsdl.sync();
        //
        // Check the model's cache is updated
        assertFalse(cache.contains(importedWsdl));
    }

    /**
     * Checks if the imported schema model is removed from the cache after
     * its targetNamespace has deleted.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testImportedSchemaTargetNsDeleted() throws Exception {
//        mBpelModel = TestUtils.loadXamModel(TEST_BPEL_PROJECT_ZIP,
//                TEST_BPEL, BpelModelImpl.class);
        RefCacheSupport cache = loadReferencedModels(mBpelModel);
        //
        Process process = mBpelModel.getProcess();
        assertNotNull(process);
        //
        Import[] importsArr = process.getImports();
        assertEquals(2, importsArr.length);
        Import imp = importsArr[1];
        assertNotNull(imp);
        assertEquals(imp.getImportType(), Import.SCHEMA_IMPORT_TYPE);
        SchemaModel importedSchema = cache.optimizedSchemaResolve(imp);
        assertNotNull(importedSchema);
        //
        TestUtils.setDocumentContentTo(importedSchema,
                TEST_BPEL_PROJECT_ZIP, TEST_IMPORTED_SCHEMA_TNS_DELETED);
        importedSchema.sync();
        //
        // Check the model's cache is updated
        assertFalse(cache.contains(importedSchema));
    }

    /**
     * Checks that a model caches only relevant schema model references.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testCacheContainsRelevantComponents() throws Exception {
        String TEST_BPEL_PROJECT_ZIP_2 = "resources/BpelRefCacheProject2.zip"; // NOI18N
        String TEST_BPEL_2 = "BpelRefCacheProject2/src/Referencing.bpel"; // NOI18N
        //
        mBpelModel = TestUtils.loadXamModel(TEST_BPEL_PROJECT_ZIP_2,
                TEST_BPEL_2, BpelModelImpl.class);
        RefCacheSupport cache = loadReferencedModels(mBpelModel);
        //
        // Cache has to contain 2 items
        assertEquals(2, cache.getCachedModelsSize());
        assertEquals(0, cache.checkKeys());
    }

}
