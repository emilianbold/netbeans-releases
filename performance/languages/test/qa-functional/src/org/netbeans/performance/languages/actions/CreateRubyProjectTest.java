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

package org.netbeans.performance.languages.actions;

import org.netbeans.modules.performance.utilities.PerformanceTestCase;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;
import org.netbeans.modules.performance.guitracker.ActionTracker;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.performance.languages.setup.ScriptingSetup;

import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.LogRecord;
import java.util.logging.Level;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NewRubyProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class CreateRubyProjectTest extends PerformanceTestCase {

    private NewRubyProjectNameLocationStepOperator wizard_location;
    
    public String category, project, project_name, project_type;
    
    public CreateRubyProjectTest(String testName) {
        super(testName);        
        expectedTime = 10000;
        WAIT_AFTER_OPEN=20000;
    }
    
    public CreateRubyProjectTest(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=20000;
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(ScriptingSetup.class)
             .addTest(CreateRubyProjectTest.class)
             .enableModules(".*").clusters(".*")));
        return suite;
    }

        class PhaseHandler extends Handler {

            public boolean published = false;

            public void publish(LogRecord record) {
                if (record.getMessage().equals("Open Editor, phase 1, AWT [ms]"))
                     ActionTracker.getInstance().stopRecording();
            }

            public void flush() {
            }

            public void close() throws SecurityException {
            }
        }

    PhaseHandler phaseHandler=new PhaseHandler();


    @Override
    public void initialize(){
       closeAllModal();
    }

    @Override
    public void prepare(){
        Logger.getLogger("TIMER").setLevel(Level.FINE);
        Logger.getLogger("TIMER").addHandler(phaseHandler);

 /*       repaintManager().addRegionFilter(new LoggingRepaintManager.RegionFilter() {

            public boolean accept(JComponent c) {
                String cn = c.getClass().getName();
                if ("javax.swing.JRootPane".equals(cn)
                        && "org.netbeans.core.windows.view.ui.MainWindow".equals(
                        c.getParent().getClass().getName())) {
                    return false;
                } else if ("null.nbGlassPane".equals(c.getName())) {
                    return false;
                } else {
                    return true;
                }
            }

            public String getFilterName() {
                return "Ignores JRootPane under MainWindow, and nbGlassPane";
            }
        });
*/
        repaintManager().addRegionFilter(LoggingRepaintManager.IGNORE_STATUS_LINE_FILTER);
        repaintManager().addRegionFilter(LoggingRepaintManager.IGNORE_DIFF_SIDEBAR_FILTER);
        repaintManager().addRegionFilter(LoggingRepaintManager.EDITOR_FILTER);

        NewProjectWizardOperator wizard = NewProjectWizardOperator.invoke();
        wizard.selectCategory(category);
        wizard.selectProject(project);
        wizard.next();
        wizard_location = new NewRubyProjectNameLocationStepOperator();
        String directory = CommonUtilities.getTempDir() + "createdProjects";
        wizard_location.txtProjectLocation().setText("");
        wizard_location.txtProjectLocation().setText(directory);
        project_name = project_type + "_" + System.currentTimeMillis();
        wizard_location.txtProjectName().setText("");
        wizard_location.txtProjectName().typeText(project_name);
    }

    public ComponentOperator open() {
        wizard_location.finish();
        return null;
    }
    
    @Override
    public void close() {
        repaintManager().resetRegionFilters();
        EditorOperator.closeDiscardAll();
        Logger.getLogger("TIMER").removeHandler(phaseHandler);
    }    
    
    public void testCreateRubyProject() {
        category = "Ruby";
        project = Bundle.getString("org.netbeans.modules.ruby.rubyproject.ui.wizards.Bundle",
                "Templates/Project/Ruby/emptyRuby.xml");
        project_type = "RubyProject";
        doMeasurement();
    }
    
    public void testCreateRubyOnRailsProject() {
        category = "Ruby";
        project = "Ruby on Rails Application";
        project_type = "RailsProject";
        doMeasurement();
    }

}
