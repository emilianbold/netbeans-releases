/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
package org.netbeans.test.localhistory.operators;

import java.awt.Component;
import java.awt.Point;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.TreeTableOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.test.localhistory.actions.ShowLocalHistoryAction;

/** Class implementing all necessary methods for handling "Local History" view.
 */
public class ShowLocalHistoryOperator extends TopComponentOperator {
    
    /** "Local History" */
    static final String LOCAL_HISTORY_TITLE = "Local History";
    
    /** Waits for Local History TopComponent within whole IDE. */
    public ShowLocalHistoryOperator() {
        super(waitTopComponent(null, LOCAL_HISTORY_TITLE, 0, new LocalHistorySubchooser()));
    }
    
    /** Selects nodes and call Local History action on them.
     * @param nodes an array of nodes
     * @return SearchHistoryOperator instance
     */
    public static ShowLocalHistoryOperator invoke(Node[] nodes) {
        new ShowLocalHistoryAction().perform(nodes);
        return new ShowLocalHistoryOperator();
    }
    
    /** Selects node and call Local History action on it.
     * @param node node to be selected
     * @return SearchHistoryOperator instance
     */
    public static ShowLocalHistoryOperator invoke(Node node) {
        return invoke(new Node[] {node});
    }
    
    private JButtonOperator _btNext;
    private JButtonOperator _btPrevious;
    private TreeTableOperator _treeTable;
    
    //******************************
    // Subcomponents definition part
    //******************************
    
    
    
    /** Tries to find Go to Next Difference JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btNext() {
        if (_btNext==null) {
            String tooltip = "Go to next difference";
            _btNext = new JButtonOperator(this, new TooltipChooser(tooltip, getComparator()));
        }
        return _btNext;
    }
    
    /** Tries to find Go to Previous Difference JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btPrevious() {
        if (_btPrevious==null) {
            String tooltip = "Go to previous difference";
            _btPrevious = new JButtonOperator(this, new TooltipChooser(tooltip, getComparator()));
        }
        return _btPrevious;
    }
    
    /** Tries to find History JList in this dialog.
     * @return JListOperator
     */
    public TreeTableOperator treeTableHistory() {
        if (_treeTable == null) {
            _treeTable = new TreeTableOperator(this);
        }
        return _treeTable;
    }
    
    
    //****************************************
    // Low-level functionality definition part
    //****************************************
    
    /** clicks on Go to Next Difference JButton
     */
    public void next() {
        btNext().push();
    }
    
    /** clicks on Go to Previous Difference JButton
     */
    public void previous() {
        btPrevious().push();
    }
    
    /** Selects a folder denoted by path.
     * @param path path to folder without root (e.g. "folder|subfolder")
     */
    public void selectFolder(String path) {
        new Node(treeTableHistory().tree(), path).select();
    }
    
    public void performPopupAction(int rowIndex, String path) {
        JPopupMenu popup = treeTableHistory().callPopupOnCell(rowIndex, 0);
        JPopupMenuOperator popupOperator = new JPopupMenuOperator(popup);
        popupOperator.pushMenu(path);
    }
    
    //*****************************************
    // High-level functionality definition part
    //*****************************************
    
    /** Performs verification of SearchHistoryOperator by accessing all its components.
     */
    public void verify() {
        btNext();
        btPrevious();
        treeTableHistory();
    }
    
    /** SubChooser to determine TopComponent is instance of
     *  org.netbeans.modules.versioning.system.cvss.ui.history.SearchHistoryTopComponent
     * Used in constructor.
     */
    private static final class LocalHistorySubchooser implements ComponentChooser {
        public boolean checkComponent(Component comp) {
            return comp.getClass().getName().endsWith("LocalHistoryTopComponent");
        }
        
        public String getDescription() {
            return "org.netbeans.modules.localhistory.ui.view.LocalHistoryTopComponent";
        }
    }
    
    /** Chooser which can be used to find a component with given tooltip,
     * for example a button.
     */
    private static class TooltipChooser implements ComponentChooser {
        private String buttonTooltip;
        private StringComparator comparator;
        
        public TooltipChooser(String buttonTooltip, StringComparator comparator) {
            this.buttonTooltip = buttonTooltip;
            this.comparator = comparator;
        }
        
        public boolean checkComponent(Component comp) {
            return comparator.equals(((JComponent)comp).getToolTipText(), buttonTooltip);
        }
        
        public String getDescription() {
            return "Button with tooltip \""+buttonTooltip+"\".";
        }
    }
}
