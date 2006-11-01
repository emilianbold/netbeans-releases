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

package org.netbeans.nbbuild;

import java.io.File;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
import org.netbeans.junit.NbTestCase;

/**
 * It tests ValidatePath ant tasks
 * @author pzajac, Jesse Glick
 */
public class ValidatePathTest extends NbTestCase {

    public ValidatePathTest(String name) {
        super(name);
    }

    private ValidatePath vp;
    private Path path;

    protected void setUp() throws Exception {
        super.setUp();
        Project prj = new Project();
        prj.setBaseDir(getWorkDir());
        path = new Path(prj);
        vp = new ValidatePath();
        vp.setPath(path);
    }

    public void testEmptyPath() throws Exception {
        vp.execute();
    }

    public void testValidFile() throws Exception {
        File f = new File(getWorkDir(),"file1");
        assertTrue("Cannot create temporary file",f.createNewFile());
        path.setPath(f.getAbsolutePath());
        vp.execute();
    }

    public void testValidPlusInvalidFile() throws Exception {
        File f = new File(getWorkDir(),"file1");
        assertTrue("Cannot create temporary file",f.createNewFile());
        File f2 = new File(getWorkDir(),"file2");
        path.setPath(f.getAbsolutePath() + ":" + f2.getAbsolutePath());
        try {
            vp.execute();
            fail("File " + f2.getPath() + " doesn't exist but task passed");
        } catch (BuildException be) {
            // ok
        }
    }

}
