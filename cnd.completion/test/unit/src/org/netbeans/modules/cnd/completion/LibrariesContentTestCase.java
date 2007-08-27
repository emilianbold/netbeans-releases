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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.completion;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.cnd.completion.cplusplus.ext.CompletionBaseTestCase;

/**
 *
 * @author vv159170
 */
public class LibrariesContentTestCase extends CompletionBaseTestCase {
    
    public LibrariesContentTestCase(String testName) {
        super(testName, true);
    }

    @Override
    protected File beforeParsingProjectInDir(File projectDir) {
        // we have following structure for this test
        // test-folder
        //  --src\
        //        main.cc
        //  --sys_include1\ 
        //        include1.h
        //  --sys_include2\
        //        include2.h
        //
        // so, adjust used folders
        
        File srcDir = new File(projectDir, "src");
        File incl1 = new File(projectDir, "sys_include");
        File incl2 = new File(projectDir, "sys_include2");
        checkDir(srcDir);
        checkDir(incl1);
        checkDir(incl2);
        List<String> sysIncludes = Arrays.asList(incl1.getAbsolutePath(), incl2.getAbsolutePath());
        super.setSysIncludes(sysIncludes);
        return srcDir;
    }
    
    public void testGlobalFunsWithPrefix() throws Exception {
        super.performTest("src/main.cc", 6, 5, "f");
    }

    private void checkDir(File srcDir) {
        assertTrue("Not existing directory" + srcDir, srcDir.exists());
        assertTrue("Not directory" + srcDir, srcDir.isDirectory());
    }
    
}
