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

package gui.action;

import gui.window.WebFormDesignerOperator;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.performance.test.guitracker.LoggingRepaintManager;

/**
 *
 * @author mkhramov@netbeans.org
 */
public class ViewSwitchTest extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private String targetProject = "UltraLargeWA";
    private ProjectsTabOperator pto;
    private String pageToOpen = "Page1_2";
    private WebFormDesignerOperator testPage;
    private EditorOperator editorOperator;
    private int caretBlinkRate;   
    
    public ViewSwitchTest(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=4000;             
    }
    public ViewSwitchTest(String testName, String perfomanceDataName) {
        super(testName, perfomanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN=4000;             
    }
    
    
    @Override
    public void initialize() {
        log("::initialize");  
        EditorOperator.closeDiscardAll();
        pto = ProjectsTabOperator.invoke();  
    }
    
    public void prepare() {
        repaintManager().addRegionFilter(LoggingRepaintManager.EDITOR_FILTER);         
        OpenTestPage();          
        testPage.switchToJSPView();
        editorOperator = EditorWindowOperator.getEditor(pageToOpen); 
        
        caretBlinkRate =  editorOperator.txtEditorPane().getCaret().getBlinkRate();
        editorOperator.txtEditorPane().getCaret().setBlinkRate(0);
        
        editorOperator.makeComponentVisible();
        editorOperator.typeKey('z');        
    }

    
    public ComponentOperator open() {
        testPage.switchToDesignView();
        return null;
    }
    
    
    
    @Override
    public void close() {
        log("::close");
        editorOperator.txtEditorPane().getCaret().setBlinkRate(caretBlinkRate);
        repaintManager().resetRegionFilters();
        testPage.closeDiscard();
        EditorOperator.closeDiscardAll();          
    }

    private void OpenTestPage() {
        long oldTimeout = JemmyProperties.getCurrentTimeout("ComponentOperator.WaitStateTimeout");
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitStateTimeout", 120000);

        Node docNode = new Node(pto.getProjectRootNode(targetProject), gui.VWPUtilities.WEB_PAGES + "|" + pageToOpen + ".jsp");
        new OpenAction().performAPI(docNode);
        testPage = WebFormDesignerOperator.findWebFormDesignerOperator(pageToOpen);

        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitStateTimeout", oldTimeout);
    }
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new ViewSwitchTest("doMeasurement","Test view switch time"));
        return suite;
    }    
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());        
    }

}
