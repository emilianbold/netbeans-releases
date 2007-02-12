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
package org.netbeans.modules.vmd.api.model;

import java.io.IOException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.netbeans.modules.vmd.api.model.descriptors.FirstCD;
import org.netbeans.modules.vmd.api.model.descriptors.SecondCD;
import org.netbeans.modules.vmd.api.model.utils.ModelTestUtil;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Karol Harezlak
 */
public class PropertyValueArrayTest extends TestCase {

    private FileObject firstCD = ModelTestUtil.getTestFileObject(ModelTestUtil.getTestFolder(ModelTestUtil.PROJECT_TYPE+ModelTestUtil.FOLDER_PATH_COMPONENTS),
            ModelTestUtil.FIRST_FILE_NAME_CD);
    private FileObject secondCD = ModelTestUtil.getTestFileObject(ModelTestUtil.getTestFolder(ModelTestUtil.PROJECT_TYPE+ModelTestUtil.FOLDER_PATH_COMPONENTS),
            ModelTestUtil.SECOND_FILE_NAME_CD);
    private DesignDocument document;
    //private DocumentInterface project = ModelTestUtil.createTestDocumentInterface(ModelTestUtil.PROJECT_ID);
    
    public PropertyValueArrayTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        document = ModelTestUtil.createTestDesignDocument();
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(GlobalDescriptorRegistryTest.class);
        return suite;
    }
    
    /**
     *  Checking if descriptor has been properly registered
     */
    public void testGlobalDecriptorRegistering(){
        
        document.getTransactionManager().readAccess(new Runnable() {
            public void run() {
                ComponentDescriptor firstCD = document.getDescriptorRegistry().getComponentDescriptor(FirstCD.TYPEID_CLASS);
                ComponentDescriptor secondCD = document.getDescriptorRegistry().getComponentDescriptor(SecondCD.TYPEID_CLASS);
                
                assertNotNull(firstCD);
                assertNotNull(secondCD);
            }
        });
    }
    
    
}
