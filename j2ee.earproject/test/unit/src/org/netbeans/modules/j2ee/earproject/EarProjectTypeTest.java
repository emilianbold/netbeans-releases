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

package org.netbeans.modules.j2ee.earproject;

import java.io.File;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author vkraemer
 */
public final class EarProjectTypeTest extends NbTestCase {
    
    private EarProjectType prjType;
    
    public EarProjectTypeTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();
        prjType = new EarProjectType();
    }
    
    public void testCreateProject() throws Exception {
        File prjDirF = new File(getWorkDir(), "EarProjectTypeTest.testCreatProject");
        prjDirF.mkdirs();
        FileObject prjDirFO = FileUtil.toFileObject(prjDirF);
        AntProjectHelper tmp = ProjectGenerator.createProject(prjDirFO, prjType.getType());
        prjType.createProject(tmp);
    }
    
    public void testCreateProjectNullArg() throws Exception {
        try {
            prjType.createProject(null);
            fail("null is an invalid argument");
        } catch (IllegalArgumentException ex) {
            // OK we should get here
        }
    }
    
    public void testGetType() {
        assertEquals(prjType.getType(), "org.netbeans.modules.j2ee.earproject");
    }
    
}
