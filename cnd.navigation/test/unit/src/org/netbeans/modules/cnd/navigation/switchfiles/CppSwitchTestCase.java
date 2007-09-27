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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.navigation.switchfiles;

import java.io.File;
import java.io.PrintStream;
import java.util.Collection;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelTestBase;

/**
 *
 * @author Sergey Grinev
 */
public class CppSwitchTestCase extends TraceModelTestBase {

    public CppSwitchTestCase(String testName) {
        super(testName);
    }

    public void testTwoNamesakes() throws Exception {
        String source = "welcome.cc"; // NOI18N
        performTest("", source + ".dat", source + ".err"); // NOI18N
    }

    protected void performTest() throws Exception {
        CsmProject project = getCsmProject();
        Collection<CsmFile> files = project.getAllFiles();
        for (CsmFile csmFile : files) {
            if (csmFile.getAbsolutePath().indexOf("welcome.cc")!=-1) { //NOI18N
                CsmFile f = CppSwitchAction.findHeader(csmFile);
                assert f!=null && f.getAbsolutePath().indexOf("dir1/welcome.h")!=-1; //NOI18N
            }
            if (csmFile.getAbsolutePath().indexOf("welcome.h")!=-1) { //NOI18N
                CsmFile f = CppSwitchAction.findSource(csmFile);
                assert f!=null && f.getAbsolutePath().indexOf("welcome.cc")!=-1; //NOI18N
            }
        }
    }

    @Override
    protected void doTest(File testFile, PrintStream streamOut, PrintStream streamErr, Object... params) throws Exception {
        PrintStream oldOut = System.out;
        PrintStream oldErr = System.err;
        try {
            // redirect output and err
            System.setOut(streamOut);
            System.setErr(streamErr);
            performModelTest(testFile, streamOut, streamErr);
            performTest();
        } finally {
            // restore err and out
            System.setOut(oldOut);
            System.setErr(oldErr);
        }
    }
}