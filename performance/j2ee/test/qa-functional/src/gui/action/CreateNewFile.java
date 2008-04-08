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

package gui.action;

import gui.Utils;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.NewFileAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;


/**
 * Test of Open File Dialog
 *
 * @author  lmartinek@netbeans.org
 */
public class CreateNewFile extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private NewFileWizardOperator wizard;
    
    private String project;
    private String category;
    private String fileType;
    private String fileName;
    private String packageName;
    private boolean isEntity = false;
    /**
     * Creates a new instance of CreateNewFile 
     */
    public CreateNewFile(String testName) {
        super(testName);
        expectedTime = 5000;
    }
    
    /**
     * Creates a new instance of CreateNewFile 
     */
    public CreateNewFile(String testName, String performanceDataName) {
        super(testName,performanceDataName);
        expectedTime = 5000;
    }
    
    public void testCreateNewSessionBean() {
        WAIT_AFTER_OPEN = 10000;
        project = "TestApplication-ejb";
        category = "Enterprise";
        fileType = "Session Bean";
        fileName = "NewTestSession";
        packageName = "test.newfiles";
        doMeasurement();
    }
    
    public void testCreateNewEntityBean() {
        WAIT_AFTER_OPEN = 10000;
        project = "TestApplication-ejb";
        category = "Enterprise";
        fileType = "Entity Bean";
        fileName = "NewTestEntity";
        packageName = "test.newfiles";
        isEntity = true;
        doMeasurement();
    }
    
    public void testCreateNewWebService() {
        WAIT_AFTER_OPEN = 10000;
        project = "TestApplication-ejb";
        category = "Web Services";
        fileType = "Web Service";
        fileName = "NewWebService";
        packageName = "test.newfiles";
        doMeasurement();
    }
     
    public void initialize() {
        new OpenAction().performAPI(new Node(new ProjectsTabOperator().getProjectRootNode("TestApplication-EJBModule"), "Source Packages|test|TestSessionRemote.java"));
    }
    
    public void shutdown() {
        new EditorOperator("TestSessionRemote.java").closeDiscard();
    }
    
    public void prepare() {
        new NewFileAction().performMenu();
        wizard = new NewFileWizardOperator();
        wizard.selectProject(project);
        wizard.selectCategory(category);
        wizard.selectFileType(fileType);
        wizard.next();
        JTextFieldOperator eBname;
        if(isEntity==true)
             eBname = new JTextFieldOperator(wizard,1);
        else
             eBname = new JTextFieldOperator(wizard);
        eBname.setText(fileName+Utils.getTimeIndex());
        new JComboBoxOperator(wizard,1).enterText(packageName);
    }

    public ComponentOperator open() {
        repaintManager().addRegionFilter(repaintManager().EDITOR_FILTER);
        if (System.getProperty("os.name").indexOf("Windows")!=-1) wizard.finish();
        return new EditorOperator(fileName);
    }
    
    public void close() {
        repaintManager().resetRegionFilters();
        if (testedComponentOperator != null){
            ((EditorOperator)testedComponentOperator).save();
            ((EditorOperator)testedComponentOperator).close();
        }    
    }
    
}
