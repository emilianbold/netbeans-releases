/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
