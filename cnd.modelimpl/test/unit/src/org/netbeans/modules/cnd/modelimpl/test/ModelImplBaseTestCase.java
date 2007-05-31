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

package org.netbeans.modules.cnd.modelimpl.test;

import java.io.File;
import java.io.IOException;
import org.netbeans.junit.Manager;
import org.netbeans.modules.cnd.test.BaseTestCase;

/**
 * IMPORTANT NOTE:
 * If This class is not compiled with the notification about not resolved
 * BaseTestCase class => cnd/core tests are not compiled
 * 
 * To solve this problem compile or run tests for cnd/core
 *
 * If problems with NB JUnit see comment to @see BaseTestCase
 */

/**
 * base class for modelimpl module tests
 * @author Vladimir Voskresensky
 */
public abstract class ModelImplBaseTestCase extends BaseTestCase {
    
    /**
     * Creates a new instance of ModelImplBaseTestCase
     */
    public ModelImplBaseTestCase(String testName) {
        super(testName);
    }
    
    @Override 
    public String getWorkDirPath() {
        String workDirPath = System.getProperty("cnd.modelimpl.unit.workdir"); // NOI18N
        if (workDirPath == null || workDirPath.length() == 0) {
            return super.getWorkDirPath();
        } else {
            return workDirPath;
        }
    }
    
    @Override 
    public File getGoldenFile(String filename) {
        String goldenDirPath = System.getProperty("cnd.modelimpl.unit.golden"); // NOI18N
        if (goldenDirPath == null || goldenDirPath.length() == 0) {
            return super.getGoldenFile(filename);
        } else {
            return Manager.normalizeFile(new File(goldenDirPath, filename));
        }        
    }   

    @Override
    protected File getDataFile(String filename) {
        String dataDirPath = System.getProperty("cnd.modelimpl.unit.data"); // NOI18N
        if (dataDirPath == null || dataDirPath.length() == 0) {
            return super.getDataFile(filename);
        } else {
            return Manager.normalizeFile(new File(dataDirPath, filename));
        }
    }   
}
