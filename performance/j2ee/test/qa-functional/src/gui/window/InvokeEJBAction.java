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

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyProperties;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;

/**
 * Test of dialogs from EJB source editor.
 *
 * @author  lmartinek@netbeans.org
 */
public class InvokeEJBAction extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private static EditorOperator editor;
    
    private String popupMenu = null;
    private String dialogTitle = null;
    
    /**
     * Creates a new instance of InvokeEJBAction 
     */
    public InvokeEJBAction(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 1000;
    }
    
    /**
     * Creates a new instance of InvokeEJBAction 
     */
    public InvokeEJBAction(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 1000;
    }
    
    public void testAddBusinessMethodDialog(){
        popupMenu = "EJB Methods|Add Business Method";
        dialogTitle = "Add Business Method";
        doMeasurement();
    }

    public void testCallEJBDialog(){
        popupMenu = "Enterprise Resources|" + 
                org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entres.Bundle", "LBL_CallEjbAction");
        dialogTitle = "Call Enterprise Bean";
        doMeasurement();
    }
    
    public void initialize() {
        
        // open a java file in the editor
        Node openFile = new Node(new ProjectsTabOperator().getProjectRootNode("TestApplication-EJBModule"),"Enterprise Beans|TestSessionSB");
        new OpenAction().performAPI(openFile);
        editor = new EditorWindowOperator().getEditor("TestSessionBean.java");
        new org.netbeans.jemmy.EventTool().waitNoEvent(5000);
        editor.select(11);
        JemmyProperties.setCurrentDispatchingModel(JemmyProperties.ROBOT_MODEL_MASK); 
    }
    
    public void prepare() {
        // do nothing
   }
    
    public ComponentOperator open(){
        new ActionNoBlock(null,popupMenu).perform(editor);
        return new NbDialogOperator(dialogTitle);
    }

    public void shutdown(){
        editor.closeDiscard();
    }
    
}
