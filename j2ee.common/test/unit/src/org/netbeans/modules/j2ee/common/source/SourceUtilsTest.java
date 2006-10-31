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

package org.netbeans.modules.j2ee.common.source;

import java.io.IOException;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.j2ee.common.AbstractTask;
import org.netbeans.modules.j2ee.common.source.RepositoryImpl;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author Andrei Badea
 */
public class SourceUtilsTest extends NbTestCase {

    private FileObject workDir;

    public SourceUtilsTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        MockServices.setServices(FakeJavaMIMEResolver.class, FakeJavaDataLoaderPool.class, RepositoryImpl.class);
        clearWorkDir();
        workDir = FileUtil.toFileObject(getWorkDir());
    }

    public void testMainElement() throws Exception {
        FileObject javaClass = URLMapper.findFileObject(getClass().getResource("SomeClass.javax"));
        JavaSource javaSource = JavaSource.forFileObject(javaClass);
        javaSource.runUserActionTask(new AbstractTask<CompilationController>() {
            public void run(CompilationController controller) throws IOException {
                SourceUtils srcUtils = new SourceUtils(controller);
            }
        }, true);

        // now trying a class which does not have a main type element
        // should throw an IllegalStateException
        javaClass = URLMapper.findFileObject(getClass().getResource("EmptyFile.javax"));
        javaSource = JavaSource.forFileObject(javaClass);
        try {
            javaSource.runUserActionTask(new AbstractTask<CompilationController>() {
                public void run(CompilationController controller) throws IOException {
                    SourceUtils srcUtils = new SourceUtils(controller);
                }
            }, true);
            fail();
        } catch (IOException e) {
            assertTrue(e.getCause() instanceof IllegalStateException);
        }
    }
}
