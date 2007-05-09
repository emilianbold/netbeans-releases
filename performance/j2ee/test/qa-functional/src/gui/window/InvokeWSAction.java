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

package gui.window;

import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.performance.test.utilities.PerformanceTestCase;

/**
 * Test of dialogs from WS source editor.
 *
 * @author  lmartinek@netbeans.org
 */
public class InvokeWSAction extends PerformanceTestCase {
    
    private static EditorOperator editor;
    private static Node node;
    
    private String popupMenu = null;
    private String dialogTitle = null;
    
    /**
     * Creates a new instance of InvokeWSAction 
     */
    public InvokeWSAction(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 1000;
    }
    
    /**
     * Creates a new instance of InvokeWSAction 
     */
    public InvokeWSAction(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 1000;
    }
    
    public void testAddOperationDialog(){
        popupMenu = Bundle.getString(
                "org.netbeans.modules.websvc.core.webservices.action.Bundle",
                "LBL_OperationAction");
        dialogTitle = Bundle.getString(
                "org.netbeans.modules.websvc.core.webservices.action.Bundle",
                "TTL_AddOperation");
        doMeasurement();
    }

    public void initialize() {
        //MENU = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.core.Bundle","Menu/Edit") + "|" + org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.search.project.Bundle","LBL_SearchProjects");
        //TITLE = org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.editor.Bundle", "find");
        
        // open a java file in the editor
        node = new Node(new ProjectsTabOperator().getProjectRootNode("TestApplication-WebModule"),"Web Services|TestWebService");
        new OpenAction().performPopup(node);
        editor = new EditorWindowOperator().getEditor("TestWebServiceImpl.java");
        new org.netbeans.jemmy.EventTool().waitNoEvent(5000);
        //editor.select(11);
    }
    
    public void prepare() {
        // do nothing
   }
    
    public ComponentOperator open(){
        node.performPopupActionNoBlock(popupMenu);
        return new NbDialogOperator(dialogTitle);
    }

    public void shutdown(){
        editor.closeDiscard();
    }
    
}
