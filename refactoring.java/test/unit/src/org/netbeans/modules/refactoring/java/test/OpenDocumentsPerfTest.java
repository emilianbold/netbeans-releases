/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.refactoring.java.test;

import java.io.IOException;
import java.util.Enumeration;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import junit.framework.Assert;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.junit.Log;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import static org.netbeans.modules.refactoring.java.test.Utilities.*;

/**
 * Test that all java-source instances are disposed at the end.
 *
 * @author Pavel Flaska
 */
public class OpenDocumentsPerfTest extends RefPerfTestCase {

    static {
        OpenDocumentsPerfTest.class.getClassLoader().setDefaultAssertionStatus(true);
        System.setProperty("org.openide.util.Lookup", TestLkp.class.getName());
        Assert.assertEquals(TestLkp.class, Lookup.getDefault().getClass());
    }

    public OpenDocumentsPerfTest(String name) {
        super(name);
    }

    public void testOpenDocuments()
            throws IOException, InterruptedException, ExecutionException {
        // logging is used to obtain data about consumed time
        Logger timer = Logger.getLogger("TIMER.RefactoringSession");
        timer.setLevel(Level.FINE);
        timer.addHandler(handler);

        timer = Logger.getLogger("TIMER.RefactoringPrepare");
        timer.setLevel(Level.FINE);
        timer.addHandler(handler);

        Log.enableInstances(Logger.getLogger("TIMER"), "JavacParser", Level.FINEST);

        ClasspathInfo cpi = ClasspathInfo.create(getBoot(), getCompile(), getSource());
        
        Enumeration<? extends FileObject> files = getProjectDir().getData(true);
        JavaSource src = null;
        while (files.hasMoreElements()) {
            FileObject testFile = files.nextElement();
            src = JavaSource.create(cpi, testFile);
            src.runUserActionTask(new Task<CompilationController>() {

                public void run(CompilationController controller) throws Exception {
                    controller.toPhase(Phase.RESOLVED);
                    Document d = controller.getDocument();
                }
            }, true);

        }


        System.gc(); System.gc();
        src = null;
        Log.assertInstances("Some instances of parser were not GCed");
    }

}