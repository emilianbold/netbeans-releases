/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.cnd.repository.impl;

import java.io.File;
import org.netbeans.modules.cnd.api.model.CsmModel;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.modelimpl.csm.core.ModelImpl;
import org.netbeans.modules.cnd.modelimpl.trace.FileModelTest;

/**
 * Test for Key uniquity 
 *
 * @author Sergey Grinev
 */
public class KeyValidatorRepositoryTest extends FileModelTest {
    
    public KeyValidatorRepositoryTest(String testName) {
        super(testName);
    }
    
    public void testModelProvider() {
        CsmModel csmModel = CsmModelAccessor.getModel();
        assertNotNull("Null model", csmModel); //NOI18N
        assertTrue("Unknown model provider " + csmModel.getClass().getName(), csmModel instanceof ModelImpl); //NOI18N
    }

    @Override
    protected void postSetUp() {
        String dataPath = getDataDir().getAbsolutePath().replaceAll("repository", "modelimpl"); //NOI18N
        String superClassNameAsPath = getClass().getSuperclass().getName().replace('.', File.separatorChar);
        System.setProperty("cnd.modelimpl.unit.data", dataPath + File.separator + superClassNameAsPath); //NOI18N
        System.setProperty("cnd.modelimpl.unit.golden", dataPath + File.separator + "goldenfiles" + File.separator + superClassNameAsPath); //NOI18N
        super.postSetUp();
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("cnd.repository.validate.keys", Boolean.TRUE.toString()); //NOI18N
        super.setUp();
    }
}