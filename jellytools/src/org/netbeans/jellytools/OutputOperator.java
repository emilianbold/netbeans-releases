/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
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
 * Output window might contain one or more tabs with terms
 * displayed in them. It is better to use {@link TermOperator} if you want to work
 * with a particular output term.
 * <p>
 * Usage:<br>
 * <pre>
 *      OutputOperator oo = new OutputOperator();
 *      System.out.println("TEXT from active term="+oo.getText().substring(0, 10));
 *      System.out.println("Compiler messages="+oo.getCompilerTerm().getText()+"\n");
 *      // get TermOperator instance
 *      TermOperator to1 = oo.getActiveTerm();
 *      TermOperator to2 = oo.getTerm("MyTerm");
 *      // call an action from context menu
 *      oo.startRedirection();
 *      // close active term by menu
 *      oo.discard();
 *      // close all terms by menu
 *      oo.discardAll();
 *      // close by API
 *      oo.close();
 * </pre>
 *
 * @author Jiri.Skrivanek@sun.com
 * @see TermOperator
 */
public class OutputOperator extends TopComponentOperator {
    
    private static final Action invokeAction = new OutputWindowViewAction();
    
    /** Waits for output window top component and creates a new operator for it. */
    public OutputOperator() {
        /* In IDE OutputView top component is singleton but in sense of
         jellytools, it is not singleton. It can be closed/hidden and
         again opened/shown, so it make sense to wait for OutputView
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
     * @deprecated Use {@link TermOperator} or {@link #getTerm} to locate
     * appropriate output pane.
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
     */
    public TermOperator getActiveTerm() {
        TermOperator term;
        if(null != JTabbedPaneOperator.findJTabbedPane((Container)getSource(), ComponentSearcher.getTrueChooser(""))) {
            term = new TermOperator(((JComponent)tbpOutputTabbedPane().getSelectedComponent()));
            term.copyEnvironment(this);
        } else {
            term = new TermOperator(this);
        }
        return term;
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
     * @deprecated Use {@link #getTerm(String)} if you need {@link TermOperator}
     * instance of specified name. If there is the only output term there is no
     * tabbed pane and method selectPage can fail.
     */
    public Component selectPage(String pageTitle) {
        return tbpOutputTabbedPane().selectPage(pageTitle);
    }
    
    /**
     * Selects "Compiler" page.
     *
     * @deprecated Use {@link #getCompilerTerm} if you need compiler
     * {@link TermOperator}. If there is the only output term there is no
     * tabbed pane and method selectCompilerPage can fail.
     */
    public void selectCompilerPage()  {
        selectPage(Bundle.getString("org.netbeans.core.compiler.Bundle", "CTL_CompileTab"));
    }
    
    /**
     * Returns instance of TermOperator of given name.
     * It is activated by default.
     * @param termName name of term to be selected
     * @return instance of TermOperator
     */
    public TermOperator getTerm(String termName) {
        return new TermOperator(termName);
    }
    
    /** Returns instance of Compiler TermOperator.
     * It is activated by default.
     * @return instance of TermOperator
     */
    public TermOperator getCompilerTerm() {
        return getTerm(Bundle.getString("org.netbeans.core.compiler.Bundle", "CTL_CompileTab"));
    }
    
    /**
     * Returns text from the active term.
     * @return text from the active term
     */
    public String getText() {
        return getActiveTerm().getText();
    }
    
    /********************************** Actions ****************************/
    
    /** Performs copy action on active term. */
    public void copy() {
        getActiveTerm().copy();
    }
    
    /** Performs find action on active term. */
    public void find() {
        getActiveTerm().find();
    }
    
    /** Performs find next action on active term. */
    public void findNext() {
        getActiveTerm().findNext();
    }
    
    /** Performs select all action on active term. */
    public void selectAll() {
        getActiveTerm().selectAll();
    }
    
    /** Performs clear output action on active term. */
    public void clearOutput() {
        getActiveTerm().clearOutput();
    }
    
    /** Performs start redirection action on active term. */
    public void startRedirection() {
        getActiveTerm().startRedirection();
    }
    
    /** Performs stop redirection action on active term. */
    public void stopRedirection() {
        getActiveTerm().stopRedirection();
    }
    
    /** Performs discard action on active term. */
    public void discard() {
        getActiveTerm().discard();
    }
    
    /** Performs discard all action on active term. */
    public void discardAll() {
        getActiveTerm().discardAll();
    }
    
    /** Performs verification by accessing all sub-components. */
    public void verify() {
        // do nothing because output top component can be empty
    }
    
    /** SubChooser to determine OutputView TopComponent
     * Used in constructor.
     */
    private static final ComponentChooser outputSubchooser = new ComponentChooser() {
        public boolean checkComponent(Component comp) {
            return comp.getClass().getName().endsWith("OutputView"); //NOI18N
        }
        
        public String getDescription() {
            return "component instanceof org.netbeans.core.output.OutputView";// NOI18N
        }
    };
}
