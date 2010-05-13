/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.web.core;

import java.io.File;
import java.io.IOException;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.modules.j2ee.core.api.support.java.SourceUtils;
import static org.netbeans.api.java.source.JavaSource.Phase;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.netbeans.junit.NbTestCase;

import org.netbeans.modules.web.core.test.TestUtil;

/**
 *
 * @author Radko Najman
 */
public class WebInjectionTargetQueryImplementationTest extends NbTestCase {
    
    private String serverID;
    private FileObject ordinaryClass;
    private FileObject fileSubclass;
    private FileObject directServletSubclass;
    private FileObject secondLevelServletSubclass;

    public WebInjectionTargetQueryImplementationTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        
        TestUtil.makeScratchDir(this);
        serverID = TestUtil.registerSunAppServer(this);
    }

    protected void tearDown() throws Exception {
        serverID = null;
        ordinaryClass = null;
        fileSubclass = null;
        directServletSubclass = null;
        secondLevelServletSubclass = null;
        
        super.tearDown();
    }

    /**
     * Test of isInjectionTarget method, of class org.netbeans.modules.web.core.WebInjectionTargetQueryImplementation.
     */
    public void testIsInjectionTarget() {
        System.out.println("isInjectionTarget");
        
        final boolean[] result = {false};        
        final WebInjectionTargetQueryImplementation instance = new WebInjectionTargetQueryImplementation();

        //J2EE 1.4 project
        File f = new File(getDataDir().getAbsolutePath(), "projects/WebApplication_j2ee14");
        FileObject projdir = FileUtil.toFileObject(f);

        ordinaryClass = projdir.getFileObject("src/java/org/test/NewClass.java");
        fileSubclass = projdir.getFileObject("src/java/org/test/FileSubclass.java");
        directServletSubclass = projdir.getFileObject("src/java/org/test/NewServlet.java");
        secondLevelServletSubclass = projdir.getFileObject("src/java/org/test/NewServletSubclass.java");
        
        CancellableTask task = new CancellableTask<CompilationController>() {
                public void run(CompilationController controller) throws IOException {
                    controller.toPhase(Phase.ELEMENTS_RESOLVED);
                    TypeElement thisTypeEl = SourceUtils.getPublicTopLevelElement(controller);
                    result[0] = instance.isInjectionTarget(controller, thisTypeEl);
                }
                public void cancel() {}
        };
        try {
            JavaSource javaSrc = JavaSource.forFileObject(ordinaryClass);
            javaSrc.runUserActionTask(task, true);
            assertEquals(false, result[0]);
            javaSrc = JavaSource.forFileObject(fileSubclass);
            javaSrc.runUserActionTask(task, true);
            assertEquals(false, result[0]);
            javaSrc = JavaSource.forFileObject(directServletSubclass);
            javaSrc.runUserActionTask(task, true);
            assertEquals(false, result[0]);
            javaSrc = JavaSource.forFileObject(secondLevelServletSubclass);
            javaSrc.runUserActionTask(task, true);
            assertEquals(false, result[0]);

            //Java EE 5 project
            f = new File(getDataDir().getAbsolutePath(), "projects/WebApplication_jee5");
            projdir = FileUtil.toFileObject(f);

            ordinaryClass = projdir.getFileObject("src/java/org/test/NewClass.java");
            fileSubclass = projdir.getFileObject("src/java/org/test/FileSubclass.java");
            directServletSubclass = projdir.getFileObject("src/java/org/test/NewServlet.java");
            secondLevelServletSubclass = projdir.getFileObject("src/java/org/test/NewServletSubclass.java");

            javaSrc = JavaSource.forFileObject(ordinaryClass);
            javaSrc.runUserActionTask(task, true);
            assertEquals(false, result[0]);
            javaSrc = JavaSource.forFileObject(fileSubclass);
            javaSrc.runUserActionTask(task, true);
            assertEquals(false, result[0]);
            javaSrc = JavaSource.forFileObject(directServletSubclass);
            javaSrc.runUserActionTask(task, true);
            assertEquals(true, result[0]);
            javaSrc = JavaSource.forFileObject(secondLevelServletSubclass);
            javaSrc.runUserActionTask(task, true);
            assertEquals(true, result[0]);
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("ex="+ex);
            throw new AssertionError(ex);
        }
    } 
    
}
