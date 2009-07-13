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

package org.netbeans.performance.scanning;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import junit.framework.Test;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbPerformanceTest.PerformanceData;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.ui.ProjectTab;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Pavel Flaška
 */
public class JavaNavigatorPerfTest extends NbTestCase {

    private final List<PerformanceData> data;

    public JavaNavigatorPerfTest(String name) {
        super(name);
        data = new ArrayList<PerformanceData>();
    }

    /**
     * Set-up the services and project
     */
    @Override
    protected void setUp() throws IOException, InterruptedException {
        clearWorkDir();
        System.setProperty("netbeans.user", getWorkDirPath());
    }

    public void testEditorPaneSwitch() throws IOException, ExecutionException, InterruptedException, InvocationTargetException {
        String zipPath = Utilities.projectOpen("http://spbweb.russia.sun.com/~ok153203/jEdit41.zip", "jEdit41.zip");
        File zipFile = FileUtil.normalizeFile(new File(zipPath));
        Utilities.unzip(zipFile, getWorkDirPath());
        final FileObject projectDir = Utilities.openProject("jEdit41", getWorkDir());

        Logger navigatorUpdater = Logger.getLogger("org.netbeans.modules.java.navigation.ClassMemberPanelUI.perf");
        navigatorUpdater.setLevel(Level.FINE);
        navigatorUpdater.addHandler(new NavigatorHandler());
        
        JavaSource src = JavaSource.create(ClasspathInfo.create(projectDir));

        src.runWhenScanFinished(new Task<CompilationController>() {

            @Override()
            public void run(CompilationController controller) throws Exception {
                controller.toPhase(JavaSource.Phase.RESOLVED);
            }
        }, false).get();
        SwingUtilities.invokeAndWait(new Runnable() {

            public void run() {
                ProjectTab pt = ProjectTab.findDefault(ProjectTab.ID_LOGICAL);
                pt.requestActive();
                FileObject testFile = projectDir.getFileObject("/src/bsh/This.java");
                pt.selectNode(testFile);
            }
        });
        Thread.sleep(500);
        SwingUtilities.invokeAndWait(new Runnable() {

            public void run() {
                ProjectTab pt = ProjectTab.findDefault(ProjectTab.ID_LOGICAL);
                pt.requestActive();
                FileObject testFile = projectDir.getFileObject("/src/org/gjt/sp/jedit/jEdit.java");
                pt.selectNode(testFile);
            }
        });
        Thread.sleep(5000);
        SwingUtilities.invokeAndWait(new Runnable() {

            public void run() {
                Logger.getAnonymousLogger().log(Level.INFO, "Test finished execution.");
            }
        });
    }

    @Override
    protected void tearDown() throws Exception {
        Logger.getAnonymousLogger().log(Level.INFO, "Processing results.");
        super.tearDown();
        for (PerformanceData rec : getPerformanceData()) {
            Utilities.processUnitTestsResults(JavaNavigatorPerfTest.class.getCanonicalName(), rec);
        }
        data.clear();
    }


    public static Test suite() throws InterruptedException {
        return NbModuleSuite.create(NbModuleSuite.emptyConfiguration().
            addTest(JavaNavigatorPerfTest.class).gui(false));
    }

    public PerformanceData[] getPerformanceData() {
        return data.toArray(new PerformanceData[0]);
    }

    private class NavigatorHandler extends Handler {

        @Override
        public void publish(LogRecord record) {
            PerformanceData perfRec = new PerformanceData();
            perfRec.name = (String) record.getParameters()[0];
            perfRec.value = (Long) record.getParameters()[1];
            perfRec.unit = "ms";
            perfRec.runOrder = 0;
            perfRec.threshold = 5000;
            System.err.println(perfRec.name);
            data.add(perfRec);
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }
}
