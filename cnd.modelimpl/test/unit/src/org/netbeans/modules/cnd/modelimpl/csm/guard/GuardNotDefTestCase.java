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

package org.netbeans.modules.cnd.modelimpl.csm.guard;

import java.io.File;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelTestBase;

/**
 * base class for guard block tests
 *
 * @author Alexander Simon
 */
public class GuardNotDefTestCase extends TraceModelTestBase {
    
    public GuardNotDefTestCase(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testGuard() throws Exception {
        performTest("argc.cc"); // NOI18N
    }
    
    @Override
    protected void performTest(String source) throws Exception {
        File testFile = getDataFile(source);
        assertTrue("File not found "+testFile.getAbsolutePath(),testFile.exists()); // NOI18N
        performModelTest(testFile, System.out, System.err);
        boolean checked = false;
        for(FileImpl file : getProject().getAllFileImpls()){
            if ("cstdlib.h".equals(file.getName())){ // NOI18N
                assertTrue("Guard guard block defined", file.getMacros().size()==2); // NOI18N
                String guard = file.testGetGuardState().testGetGuardName();
                assertTrue("Guard guard block name not _STDLIB_H", "_STDLIB_H".equals(guard)); // NOI18N
                checked = true;
            } else if ("iostream.h".equals(file.getName())){ // NOI18N
                String guard = file.testGetGuardState().testGetGuardName();
                assertTrue("Guard guard block found", guard == null); // NOI18N
            } else if ("argc.cc".equals(file.getName())){ // NOI18N
                String guard = file.testGetGuardState().testGetGuardName();
                assertTrue("Guard guard block name not MAIN", "MAIN".equals(guard)); // NOI18N
            }
        }
        assertTrue("Not found FileImpl for cstdlib.h", checked); // NOI18N
    }
    
    private String getClassName(Class cls){
        String s = cls.getName();
        return s.substring(s.lastIndexOf('.')+1);
    }
}
