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

package org.netbeans.test.subversion.operators;

import java.awt.Component;
import java.awt.Container;
import javax.swing.JComponent;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.ComponentSearcher;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.test.subversion.operators.actions.RefactoringAction;

public class RefactoringOperator extends TopComponentOperator {
    
    private static final Action invokeAction = new RefactoringAction();
    private JButtonOperator _btDoRefactoring;
    private JButtonOperator _btCancel;
    
    public JButtonOperator btDoRefactoring() {
        if (_btDoRefactoring == null) {
            _btDoRefactoring = new JButtonOperator(this, "Refactor");
        }
        return _btDoRefactoring;
    }
    
    public JButtonOperator btCancel() {
        if (_btCancel == null) {
            _btCancel = new JButtonOperator(this, "Cancel");
        }
        return _btCancel;
    }
    
    public void doRefactoring() {
        btDoRefactoring().pushNoBlock();
    }
    
    public void cancel() {
        btCancel().pushNoBlock();
    }
    
    /** Waits for refactoring window top component and creates a new operator for it. */
    public RefactoringOperator() {
        super(waitTopComponent(null, null, 0, renameChooser));
    }
    
    /**
     * Opens Output from main menu Window|Output and returns RefactoringOperator.
     * 
     * @return instance of RefactoringOperator
     */
    public static RefactoringOperator invoke() {
        invokeAction.perform();
        return new RefactoringOperator();
    }
    
    /** Returns active OutputTabOperator instance regardless it is the only one in
     * output or it is in tabbed pane.
     * @return active OutputTabOperator instance
     *
    private OutputTabOperator getActiveOutputTab() {
        OutputTabOperator outputTabOper;
        if(null != JTabbedPaneOperator.findJTabbedPane((Container)getSource(), ComponentSearcher.getTrueChooser(""))) {
            outputTabOper = new OutputTabOperator(((JComponent)new JTabbedPaneOperator(this).getSelectedComponent()));
            outputTabOper.copyEnvironment(this);
        } else {
            outputTabOper = new OutputTabOperator("");
        }
        return outputTabOper;
    }*/

    /**
     * Returns instance of OutputTabOperator of given name.
     * It is activated by default.
     * @param tabName name of tab to be selected
     * @return instance of OutputTabOperator
     *
    public OutputTabOperator getOutputTab(String tabName) {
        return new OutputTabOperator(tabName);
    }*/
    
    /**
     * Returns text from the active tab.
     * @return text from the active tab
     *
    public String getText() {
        return getActiveOutputTab().getText();
    }*/
    
    /********************************** Actions ****************************/
    
    /** Performs copy action on active tab. 
    public void copy() {
        getActiveOutputTab().copy();
    }*/
    
    /** Performs find action on active tab. 
    public void find() {
        getActiveOutputTab().find();
    }*/
    
    /** Performs find next action on active tab. 
    public void findNext() {
        getActiveOutputTab().findNext();
    }*/
    
    /** Performs select all action on active tab. 
    public void selectAll() {
        getActiveOutputTab().selectAll();
    }*/    
    
    /** Performs next error action on active tab. 
    public void nextError() {
        getActiveOutputTab().nextError();
    }*/
    
    /** Performs next error action on active tab. 
    public void previousError() {
        getActiveOutputTab().previousError();
    }*/

    /** Performs wrap text action on active tab. 
    public void wrapText() {
        getActiveOutputTab().wrapText();
    }*/ 
    
    /** Performs clear action on active tab. 
    public void clear() {
        getActiveOutputTab().clear();
    }*/

    /** Performs save as action on active tab. 
    public void saveAs() {
        getActiveOutputTab().saveAs();
    }*/
    
    /** Performs verification by accessing all sub-components. 
    public void verify() {
        // do nothing because output top component can be empty
    }*/
    
    /** SubChooser to determine OutputWindow TopComponent
     * Used in constructor.
     */
    private static final ComponentChooser renameChooser = new ComponentChooser() {
        public boolean checkComponent(Component comp) {
            return comp.getClass().getName().endsWith("RefactoringPanelContainer"); //NOI18N
        }
        
        public String getDescription() {
            return "component instanceof org.netbeans.refactoring.ui.RefactoringPanelContainer";// NOI18N
        }
    };
}
