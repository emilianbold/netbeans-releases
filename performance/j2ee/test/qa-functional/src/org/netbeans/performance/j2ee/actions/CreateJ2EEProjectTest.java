/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.performance.j2ee.actions;

import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.LogRecord;
import java.util.logging.Level;
import junit.framework.Test;
import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.modules.performance.guitracker.ActionTracker;
import org.netbeans.jellytools.NewWebProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.performance.j2ee.setup.J2EEBaseSetup;

/**
 * Test create projects
 *
 * @author lmartinek@netbeans.org
 */
public class CreateJ2EEProjectTest extends PerformanceTestCase {

    private NewWebProjectNameLocationStepOperator wizard_location;

    private String category, project, project_name;
    private boolean createSubProjects = false;
    private Logger TIMER = null;
    public static final String WEB_PROJECT_NAME = "WebApp";

    /**
     * Creates a new instance of CreateJ2EEProjectTest
     *
     * @param testName the name of the test
     */
    public CreateJ2EEProjectTest(String testName) {
        super(testName);
        expectedTime = 20000;
        WAIT_AFTER_OPEN = 5000;
    }

    /**
     * Creates a new instance of CreateJ2EEProjectTest
     *
     * @param testName the name of the test
     * @param performanceDataName measured values will be saved under this name
     */
    public CreateJ2EEProjectTest(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = 20000;
        WAIT_AFTER_OPEN = 5000;
    }

    public static Test suite() {
        return emptyConfiguration().addTest(J2EEBaseSetup.class).addTest(CreateJ2EEProjectTest.class, "testCreateWebProject").suite();
    }

    class PhaseHandler extends Handler {

        public boolean published = false;

        public void publish(LogRecord record) {
            if (record.getMessage().equals("Open Editor, phase 1, AWT [ms]")) {
                ActionTracker.getInstance().stopRecording();
            }
        }

        public void flush() {
        }

        public void close() throws SecurityException {
        }

    }

    PhaseHandler phaseHandler = new PhaseHandler();

    public void testCreateWebProject() {
        category = "Java Web";
        project = "Web Application";
        project_name = "WebApp";
        doMeasurement();
    }

    public void testCreateEnterpriseApplicationProject() {
        category = "Java EE";
        project = "Enterprise Application";
        project_name = WEB_PROJECT_NAME;
        createSubProjects = true;
        doMeasurement();
    }

    public void testCreateStandaloneEnterpriseApplicationProject() {
        category = "Java EE";
        project = "Enterprise Application";
        project_name = "MyStandaloneApp";
        createSubProjects = false;
        doMeasurement();
    }

    public void testCreateEJBModuleProject() {
        category = "Java EE";
        project = "EJB Module";
        project_name = "MyEJBModule";
        doMeasurement();
    }

    public void testCreateEnterpriseApplicationClient() {
        category = "Java EE";
        project = "Enterprise Application Client";
        project_name = "MyEntAppClient";
        doMeasurement();
    }

    @Override
    public void initialize() {
    }

    public void prepare() {
        TIMER = Logger.getLogger("TIMER");
        TIMER.setLevel(Level.FINE);
        TIMER.addHandler(phaseHandler);

        NewProjectWizardOperator wizard = NewProjectWizardOperator.invoke();
        wizard.selectCategory(category);
        wizard.selectProject(project);
        wizard.next();
        wizard_location = new NewWebProjectNameLocationStepOperator();
        wizard_location.txtProjectLocation().setText(System.getProperty("nbjunit.workdir") + java.io.File.separator + "tmpdir");
        project_name += CommonUtilities.getTimeIndex();
        wizard_location.txtProjectName().setText(project_name);
        wizard_location.next();
        if (project.equals("Enterprise Application")) {
            JCheckBoxOperator createEjb = new JCheckBoxOperator(wizard_location, "Ejb");
            JCheckBoxOperator createWeb = new JCheckBoxOperator(wizard_location, "Web");
            createEjb.setSelected(createSubProjects);
            createWeb.setSelected(createSubProjects);
        }
    }

    public ComponentOperator open() {
        wizard_location.finish();
        wizard_location.waitClosed();
        new ProjectsTabOperator().getProjectRootNode(project_name);
        return null;
    }

    @Override
    public void close() {
    }

    @Override
    public void shutdown() {
        TIMER.removeHandler(phaseHandler);
    }
}
