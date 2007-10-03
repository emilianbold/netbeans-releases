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
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.actions.SaveAllAction;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.performance.test.utilities.PerformanceTestCase;

/**
 * Test of finishing dialogs from WS source editor.
 *
 * @author  lmartinek@netbeans.org
 */
public class MeasureWebServiceAction extends PerformanceTestCase {
    
    private static EditorOperator editor;
    private static NbDialogOperator dialog;
    private static Node openFile;
    
    private String popup_menu;
    private String title;
    private String name;
    
    /**
     * Creates a new instance of MeasureWebServiceAction 
     */
    public MeasureWebServiceAction(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
    }
    
    /**
     * Creates a new instance of MeasureEntityBeanAction 
     */
    public MeasureWebServiceAction(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
    }
    
     public void testAddOperation(){
        WAIT_AFTER_OPEN = 5000;
        popup_menu = Bundle.getString(
                "org.netbeans.modules.websvc.core.webservices.action.Bundle",
                "LBL_OperationAction");
        title = Bundle.getString(
                "org.netbeans.modules.websvc.core.webservices.action.Bundle",
                "TTL_AddOperation");
        name = "testOperation";
        doMeasurement();
    }
     
    public void initialize() {
        // open a java file in the editor
        openFile = new Node(new ProjectsTabOperator().getProjectRootNode(
                "TestApplication-WebModule"),"Web Services|TestWebService");
        new OpenAction().performAPI(openFile);
        editor = new EditorWindowOperator().getEditor("TestWebServiceImpl.java");
        new org.netbeans.jemmy.EventTool().waitNoEvent(5000);
        editor.select(11);
    }
    
    public void prepare() {
        //new ActionNoBlock(null,popup_menu).perform(editor);
        openFile.performPopupActionNoBlock(popup_menu);
        dialog = new NbDialogOperator(title);
        new JTextFieldOperator(dialog).setText(name+Utils.getTimeIndex());
        new org.netbeans.jemmy.EventTool().waitNoEvent(2000);
   }
    
    public ComponentOperator open(){
        dialog.ok();
        return null;
    }

    public void shutdown(){
        new SaveAllAction().performAPI();
        editor.closeDiscard();
    }
    
}
