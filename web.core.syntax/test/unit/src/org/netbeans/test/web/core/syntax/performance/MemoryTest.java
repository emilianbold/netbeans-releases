/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.test.web.core.syntax.performance;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.StyledDocument;
import junit.framework.Test;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.EditorCookie.Observable;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author Jindrich Sedek
 */
public class MemoryTest extends NbTestCase {

    public MemoryTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Log.enableInstances(Logger.getLogger("TIMER.j2ee.parser"), null, Level.FINEST);
    }

    public static Test suite() {
        return NbModuleSuite.allModules(MemoryTest.class);
    }

    @Override
    protected void tearDown() throws Exception {
        Log.assertInstances("Leaking instances");
        super.tearDown();
    }

    public void testHTML() throws Exception {
        PerformanceTest.openNavigator();
        processTest("performance.html", "<table></table>");
    }

    public void testJSP() throws Exception {
        PerformanceTest.openNavigator();
        processTest("performance.jsp", "${\"hello\"}");
    }

    public void testCSS() throws Exception {
        PerformanceTest.openNavigator();
        processTest("performance.css", "selector{color:green}");
    }

    private void processTest(String testName, String insertionText) throws Exception {
        File testFile = new File(getDataDir(), testName);
        FileObject testObject = FileUtil.createData(testFile);
        DataObject dataObj = DataObject.find(testObject);
        EditorCookie.Observable ed = dataObj.getCookie(Observable.class);
        StyledDocument doc = ed.openDocument();
        ed.open();
        Thread.sleep(10000);
        doc.insertString(0, insertionText, null);
        Thread.sleep(10000);
        ed.saveDocument();
        ed.close();
        Thread.sleep(10000);
    }
}
