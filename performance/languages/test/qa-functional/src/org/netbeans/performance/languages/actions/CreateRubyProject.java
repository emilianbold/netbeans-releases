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


import javax.swing.JComponent;
import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NewProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.performance.guitracker.LoggingRepaintManager;
import org.netbeans.modules.performance.utilities.CommonUtilities;
import org.netbeans.modules.project.ui.test.ProjectSupport;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class CreateRubyProject  extends org.netbeans.modules.performance.utilities.PerformanceTestCase {
    public static final String suiteName="Scripting UI Responsiveness Actions suite";
    private NewProjectNameLocationStepOperator wizard_location;
    
    public String category, project, project_name, project_type,  editor_name;
    
    public CreateRubyProject(String testName) {
        super(testName);        
        expectedTime = 10000;
        WAIT_AFTER_OPEN=20000;
    }
    
    public CreateRubyProject(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = 10000;
        WAIT_AFTER_OPEN=20000;
    }

    @Override
    public void initialize(){
        log("::initialize::");

        repaintManager().addRegionFilter(new LoggingRepaintManager.RegionFilter() {

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

        repaintManager().addRegionFilter(LoggingRepaintManager.IGNORE_STATUS_LINE_FILTER);
        repaintManager().addRegionFilter(LoggingRepaintManager.IGNORE_EXPLORER_TREE_FILTER);
        repaintManager().addRegionFilter(LoggingRepaintManager.IGNORE_DIFF_SIDEBAR_FILTER);

        closeAllModal();
    }

    @Override
    public void prepare(){
        log("::prepare");
        createProject();
    }
    
    private void createProject() {
        NewProjectWizardOperator wizard = NewProjectWizardOperator.invoke();
        wizard.selectCategory(category);
        wizard.selectProject(project);
        wizard.next();
        wizard_location = new NewProjectNameLocationStepOperator();
        
        String directory = CommonUtilities.getTempDir() + "createdProjects";
       
        wizard_location.txtProjectLocation().setText("");
        waitNoEvent(1000);
        wizard_location.txtProjectLocation().setText(directory);
        
        project_name = project_type + "_" + System.currentTimeMillis();
        wizard_location.txtProjectName().setText("");
        waitNoEvent(1000);
        wizard_location.txtProjectName().typeText(project_name);
    }

    public ComponentOperator open() {
        log("::open");    
        wizard_location.finish();
        long oldTimeout = JemmyProperties.getCurrentTimeouts().getTimeout("ComponentOperator.WaitStateTimeout");
        JemmyProperties.getCurrentTimeouts().setTimeout("ComponentOperator.WaitStateTimeout",120000);
        wizard_location.waitClosed();

        JemmyProperties.getCurrentTimeouts().setTimeout("ComponentOperator.WaitStateTimeout",oldTimeout);        

        TopComponentOperator.findTopComponent(editor_name, 0);
        return null;
    }
    
    @Override
    public void close() {
        log("::close");

        ProjectSupport.closeProject(project_name);
    }    
    
    public void testCreateRubyProject() {
        category = "Ruby";
        project = Bundle.getString("org.netbeans.modules.ruby.rubyproject.ui.wizards.Bundle",
                "Templates/Project/Ruby/emptyRuby.xml");
        project_type = "RubyProject";
        editor_name = "main.rb";
        doMeasurement();
    }
    
    public void testCreateRubyOnRailsProject() {
        category = "Ruby";
        project = "Ruby on Rails Application";
        project_type = "RailsProject";
        editor_name = "database.yml";
        doMeasurement();
    }

    public static Test suite() {
        prepareForMeasurements();

        return NbModuleSuite.create(
            NbModuleSuite.createConfiguration(CreateRubyProject.class)
            .enableModules(".*")
            .clusters(".*")
            .reuseUserDir(true)
        );    
    }
}
