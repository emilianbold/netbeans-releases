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
import java.io.IOException;
import junit.framework.*;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
import org.netbeans.junit.*;

/**
 * It tests ValidatePath ant tasks
 * @author pzajac
 */
public class ValidatePathTest extends NbTestCase {
    
    public ValidatePathTest(java.lang.String testName) {
        super(testName);
    }
    
    public void testValidatePath() throws IOException {
        Project prj = new Project();
        prj.setBaseDir(getWorkDir());
        Path path = new Path(prj); 
        ValidatePath vp = new ValidatePath();
        vp.setPath(path);
        
        // empty path
        vp.execute();
        // a valid file
        File f = new File(getWorkDir(),"file1");
        assertTrue("Cannot create temporary file",f.createNewFile());
        path.setPath(f.getAbsolutePath());
        vp.setPath(path);
        vp.execute();
        
        // a valid  + a invalid file 
        File f2 = new File(getWorkDir(),"file2");
        path.setPath(f.getAbsolutePath() + ":" + f2.getAbsolutePath());
        vp.setPath(path);
        try {
            vp.execute();
            fail("File " + f2.getPath() + " doesn't exist but task passed");
        } catch (BuildException be) {
            // ok
        }
    }
    
}
