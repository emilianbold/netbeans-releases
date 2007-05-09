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

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.actions.SaveAllAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyProperties;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;

/**
 * Test of finishing dialogs from WS source editor.
 *
 * @author  lmartinek@netbeans.org
 */
public class MeasureCallEjbAction extends org.netbeans.performance.test.utilities.PerformanceTestCase {
    
    private static EditorOperator editor;
    private static NbDialogOperator dialog;
    
    private int index;
    
    /**
     * Creates a new instance of MeasureWebServiceAction 
     */
    public MeasureCallEjbAction(String testName) {
        super(testName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 5000;
    }
    
    /**
     * Creates a new instance of MeasureEntityBeanAction 
     */
    public MeasureCallEjbAction(String testName, String performanceDataName) {
        super(testName, performanceDataName);
        expectedTime = WINDOW_OPEN;
        WAIT_AFTER_OPEN = 5000;
    }
     
    public void initialize() {
    	index = 1;
        // open a java file in the editor
        Node openFile = new Node(new ProjectsTabOperator().getProjectRootNode("TestApplication-EJBModule"),"Enterprise Beans|TestSessionSB");
        new OpenAction().performAPI(openFile);
        editor = new EditorWindowOperator().getEditor("TestSessionBean.java");
        new org.netbeans.jemmy.EventTool().waitNoEvent(5000);
        editor.select(11);
        JemmyProperties.setCurrentDispatchingModel(JemmyProperties.ROBOT_MODEL_MASK); 
    }
    
    public void prepare() {
        new ActionNoBlock(null,"Enterprise Resources|" + 
                org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entres.Bundle", "LBL_CallEjbAction")).perform(editor);
        dialog = new NbDialogOperator("Call Enterprise Bean");
        new Node(new JTreeOperator(dialog),"TestApplication-EJBModule|ExpandTest00" + (index++) + "SB").select();
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
