/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.jellytools;

import java.awt.Component;
import java.awt.Container;
import javax.swing.JComponent;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.OutputWindowViewAction;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.ComponentSearcher;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;

/**
 * Provides access to the Output window and it's subcomponents.
 * Output window might contain one or more output tabs.
 * It is better to use {@link OutputTabOperator} if you want to work
 * with a particular output tab.
 * <p>
 * Usage:<br>
 * <pre>
 *      OutputOperator oo = new OutputOperator();
 *      System.out.println("TEXT from active output tab="+oo.getText().substring(0, 10));
 *      // get TermOperator instance
 *      OutputTabOperator oto = oo.getOutputTab("myoutput");
 *      // or
 *      // OutputTabOperator oto = new OutputTabOperator("myoutput");
 *      // call an action from context menu
 *      oo.find();
 *      // close output
 *      oo.close();
 * </pre>
 *
 * @author Jiri.Skrivanek@sun.com
 * @see OutputTabOperator
 */
public class OutputOperator extends TopComponentOperator {
    
    private static final Action invokeAction = new OutputWindowViewAction();
    
    /** Waits for output window top component and creates a new operator for it. */
    public OutputOperator() {
        /* In IDE OutputWindow top component is singleton but in sense of
         jellytools, it is not singleton. It can be closed/hidden and
         again opened/shown, so it make sense to wait for OutputWindow
         top component again.
         */
        super(waitTopComponent(null, null, 0, outputSubchooser));
    }
    
    /** Opens Output from main menu Window|Output and returns OutputOperator.
     * @return instance of OutputOperator
     */
    public static OutputOperator invoke() {
        invokeAction.perform();
        return new OutputOperator();
    }
    
    /**
     * Waits a tabbed pane inside the Output.
     * @return instance of JTabbedPaneOperator
     * @deprecated Use {@link OutputTabOperator} or {@link #getOutputTab} to locate
     * appropriate output tab.
     */
    public JTabbedPaneOperator tbpOutputTabbedPane() {
        // everytime return a new instance because a new JTabbedPane is created
        // when all tabs are closed and then one or more newly opened
        return new JTabbedPaneOperator(this);
    }
    
    /**
     * Returns active TermOperator instance regardless it is the only one in
     * output or it is in tabbed pane.
     * @return active TermOperator instance
     * @deprecated Use {@link OutputTabOperator} or {@link #getOutputTab} to locate
     * appropriate output tab.
     */
    public TermOperator getActiveTerm() {
        return new TermOperator((JComponent)getActiveOutputTab().getSource());
    }
    
    /** Returns active OutputTabOperator instance regardless it is the only one in
     * output or it is in tabbed pane.
     * @return active OutputTabOperator instance
     */
    private OutputTabOperator getActiveOutputTab() {
        OutputTabOperator outputTabOper;
        if(null != JTabbedPaneOperator.findJTabbedPane((Container)getSource(), ComponentSearcher.getTrueChooser(""))) {
            outputTabOper = new OutputTabOperator(((JComponent)tbpOutputTabbedPane().getSelectedComponent()));
            outputTabOper.copyEnvironment(this);
        } else {
            outputTabOper = new OutputTabOperator("");
        }
        return outputTabOper;
    }

    /**
     * Returns selected page title, <code>null</code> if window does not contain
     * a tabbed pane.
     * @return selected page title, <code>null</code> if window does not contain
     * a tabbed pane.
     */
    public String getActivePageName() {
        try {
            JTabbedPaneOperator _tabbed = tbpOutputTabbedPane();
            return _tabbed.getTitleAt(_tabbed.getSelectedIndex());
        } catch(TimeoutExpiredException e) {
            return null;
        }
    }
    
    /**
     * Selects page by title.
     * @param pageTitle name of page
     * @return selected component
     * @deprecated Use {@link #getOutputTab(String)} if you need {@link OutputTabOperator}
     * instance of specified name. If there is the only output tab there is no
     * tabbed pane and method selectPage can fail.
     */
    public Component selectPage(String pageTitle) {
        return tbpOutputTabbedPane().selectPage(pageTitle);
    }
    
    /**
     * Returns instance of TermOperator of given name.
     * It is activated by default.
     * @param termName name of term to be selected
     * @return instance of TermOperator
     * @deprecated Use {@link #getOutputTab(String)} instead
     */
    public TermOperator getTerm(String termName) {
        return new TermOperator(termName);
    }

    /**
     * Returns instance of OutputTabOperator of given name.
     * It is activated by default.
     * @param tabName name of tab to be selected
     * @return instance of OutputTabOperator
     */
    public OutputTabOperator getOutputTab(String tabName) {
        return new OutputTabOperator(tabName);
    }
    
    /**
     * Returns text from the active tab.
     * @return text from the active tab
     */
    public String getText() {
        return getActiveOutputTab().getText();
    }
    
    /********************************** Actions ****************************/
    
    /** Performs copy action on active tab. */
    public void copy() {
        getActiveOutputTab().copy();
    }
    
    /** Performs find action on active tab. */
    public void find() {
        getActiveOutputTab().find();
    }
    
    /** Performs find next action on active tab. */
    public void findNext() {
        getActiveOutputTab().findNext();
    }
    
    /** Performs select all action on active tab. */
    public void selectAll() {
        getActiveOutputTab().selectAll();
    }    
    
    /** Performs next error action on active tab. */
    public void nextError() {
        getActiveOutputTab().nextError();
    }
    
    /** Performs next error action on active tab. */
    public void previousError() {
        getActiveOutputTab().previousError();
    }

    /** Performs wrap text action on active tab. */
    public void wrapText() {
        getActiveOutputTab().wrapText();
    }
    
    /** Performs clear action on active tab. */
    public void clear() {
        getActiveOutputTab().clear();
    }

    /** Performs save as action on active tab. */
    public void saveAs() {
        getActiveOutputTab().saveAs();
    }
    
    /** Performs verification by accessing all sub-components. */
    public void verify() {
        // do nothing because output top component can be empty
    }
    
    /** SubChooser to determine OutputWindow TopComponent
     * Used in constructor.
     */
    private static final ComponentChooser outputSubchooser = new ComponentChooser() {
        public boolean checkComponent(Component comp) {
            return comp.getClass().getName().endsWith("OutputWindow"); //NOI18N
        }
        
        public String getDescription() {
            return "component instanceof org.netbeans.core.output2.OutputWindow";// NOI18N
        }
    };
}
