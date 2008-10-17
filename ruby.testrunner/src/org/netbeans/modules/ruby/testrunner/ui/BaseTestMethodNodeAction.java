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

package org.netbeans.modules.ruby.testrunner.ui;

import java.util.Collection;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.platform.execution.FileLocator;
import org.netbeans.modules.ruby.platform.execution.OutputRecognizer.FileLocation;
import org.netbeans.modules.ruby.rubyproject.RubyBaseProject;
import org.netbeans.modules.ruby.rubyproject.spi.TestRunner;
import org.netbeans.modules.ruby.testrunner.ui.Report.Testcase;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Base class for actions associated with a test method node.
 *
 * @author Erno Mononen
 */
abstract class BaseTestMethodNodeAction extends AbstractAction {

    private static final Logger LOGGER = Logger.getLogger(BaseTestMethodNodeAction.class.getName());

    protected final Testcase testcase;
    protected final Project project;
    protected final String name;

    public BaseTestMethodNodeAction(Testcase testcase, Project project, String name) {
        this.testcase = testcase;
        this.project = project;
        this.name = name;
    }

    @Override
    public Object getValue(String key) {
        if (NAME.equals(key)) {
            return name;
        }
        return super.getValue(key);
    }

    protected String getTestMethod() {
        return testcase.className + "/" + testcase.name; //NOI18N
    }

    protected FileObject getTestSourceRoot() {
        RubyBaseProject baseProject = project.getLookup().lookup(RubyBaseProject.class);
        // need to use test source roots, not source roots -- see the comments in #135680
        FileObject[] testRoots = baseProject.getTestSourceRootFiles();
        // if there are not test roots, return the project root -- works in rails projects
        return 0 == testRoots.length ? project.getProjectDirectory() : testRoots[0];
    }

    protected TestRunner getTestRunner(TestRunner.TestType testType) {
        Collection<? extends TestRunner> testRunners = Lookup.getDefault().lookupAll(TestRunner.class);
        for (TestRunner each : testRunners) {
            if (each.supports(testType)) {
                return each;
            }
        }
        return null;
    }
    
    protected void doRspecRun(FileObject testFile, FileLocation location){
    }

    protected final void runRspec() {
        if (testcase.getLocation() == null) {
            return;
        }
        FileLocation location = OutputUtils.getFileLocation(testcase.getLocation());
        if (location == null) {
            return;
        }
        FileObject testFile = OutputUtils.findFile(location.file, project.getLookup().lookup(FileLocator.class));
        if (testFile == null) {
            return;
        }
        RubyPlatform platform = RubyPlatform.platformFor(project);
        if (platform == null || platform.isJRuby()) {
            //XXX: does not work with JRuby, more info in issue #135680
            LOGGER.info("Rerunning an rspec test case on JRuby is currently not working");
            return;
        }
        Project owner = FileOwnerQuery.getOwner(testFile);
        assert project.equals(owner) : "Resolving FileObject for " + getTestMethod() + "/" + testFile + " failed." + "Got " + owner + ", expected " + project;
        doRspecRun(testFile, location);
    }

}
