/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.ruby.rubyproject;

import java.io.File;
import java.util.Collection;
import org.netbeans.api.project.Project;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.platform.RubyExecution;
import org.netbeans.modules.ruby.platform.execution.ExecutionDescriptor;
import org.netbeans.modules.ruby.platform.gems.GemManager;
import org.netbeans.modules.ruby.rubyproject.spi.TestRunner;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Various methods for supporting AutoTest execution
 *
 * @author Tor Norbye
 */
public class AutoTestSupport {
    
    private Project project;
    private Lookup context;
    private String charsetName;
    private String classPath;

    public AutoTestSupport(Lookup context, Project project, String charsetName) {
        this.context = context;
        this.project = project;
        this.charsetName = charsetName;
    }

    /** Extra class path to be used in case the execution process is a VM */
    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }
    
    public static boolean isInstalled(final Project project) {
        GemManager gemManager = RubyPlatform.gemManagerFor(project);
        return gemManager == null ? false : gemManager.isValidAutoTest(false);
    }

    public void start() {

        // use the ui test runner if available
        TestRunner autotestRunner = getTestRunner(TestRunner.TestType.AUTOTEST);
        if (autotestRunner != null) {
            autotestRunner.runAllTests(project, false);
            return;
        }

        RubyPlatform platform = RubyPlatform.platformFor(project);
        GemManager gemManager = platform.getGemManager();
        if (!gemManager.isValidAutoTest(true)) {
            return;
        }

        // TODO - the output here should emit into the Tasklist!
        // TODO - if you select this a second time, I've gotta front an existing
        // one if it's already running
        // I can store the existing autotest input window from execution service's
        // getInputOutput method, and reopen it. Maybe I could just call isClosed
        // on it first to see if it's still running, or if not, I could add a
        // task listener and stash these references per project.
        File pwd = FileUtil.toFile(project.getProjectDirectory());

        RubyFileLocator fileLocator = new RubyFileLocator(context, project);
        String displayName = NbBundle.getMessage(AutoTestSupport.class, "AutoTest");
        ExecutionDescriptor desc = new ExecutionDescriptor(platform, displayName, pwd, gemManager.getAutoTest());
        desc.additionalArgs("-v"); // NOI18N
        desc.fileLocator(fileLocator);
        desc.classPath(classPath); // Applies only to JRuby
        desc.showProgress(false);
        desc.addOutputRecognizer(new TestNotifier(false, false));
        desc.addStandardRecognizers();
        new RubyExecution(desc, charsetName).run();
    }
    
    private TestRunner getTestRunner(TestRunner.TestType testType) {
        Collection<? extends TestRunner> testRunners = Lookup.getDefault().lookupAll(TestRunner.class);
        for (TestRunner each : testRunners) {
            if (each.supports(testType)) {
                return each;
            }
        }
        return null;
    }

}
