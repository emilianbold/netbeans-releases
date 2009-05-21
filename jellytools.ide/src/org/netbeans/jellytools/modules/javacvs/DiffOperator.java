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
package org.netbeans.jellytools.modules.javacvs;

import java.awt.Component;
import javax.swing.JComponent;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JToggleButtonOperator;
import org.netbeans.jemmy.operators.Operator;

/**
 * Class implementing all necessary methods for handling "Diff" view.
 * <br>
 * Usage:<br>
 * <pre>
 *      DiffOperator diffOper = new DiffOperator("MyClass.java");
 *      diffOper.next();
 *      diffOper.previous();
 *      diffOper.checkRemote(true);
 *      diffOper.checkLocal(true);
 *      diffOper.checkRemoteLocal(true);
 *      diffOper.refresh();
 *      diffOper.update();
 *      CommitOperator co = diffOper.commit();
 *      diffOper.selectFile("form");
 * </pre>
 * 
 * @author Jiri.Skrivanek@sun.com
 * @see CommitOperator
 * @see org.netbeans.jellytools.modules.javacvs.actions.DiffAction
 */
public class DiffOperator extends TopComponentOperator {
    
    /** Waits for Diff TopComponent with specified file name.
     * @param name name of 'diffed' file
     */
    public DiffOperator(String name) {
        super(waitTopComponent(null, name, 0, new DiffSubchooser()));
    }
    
    /** Waits for first open Diff TopComponent. */
    public DiffOperator() {
        this(null);
    }
    
    private JButtonOperator _btNext;
    private JButtonOperator _btPrevious;
    private JToggleButtonOperator _tbRemoveLocal;
    private JToggleButtonOperator _tbLocal;
    private JToggleButtonOperator _tbRemote;
    private JButtonOperator _btRefresh;
    private JButtonOperator _btUpdate;
    private JButtonOperator _btCommit;
    private JComboBoxOperator _cbFiles;
    
    //******************************
    // Subcomponents definition part
    //******************************
    
    /** Tries to find Go to Next Difference JButton in diff view.
     * @return JButtonOperator
     */
    public JButtonOperator btNext() {
        if (_btNext==null) {
            String tooltip = Bundle.getString(
                    "org.netbeans.modules.versioning.system.cvss.ui.actions.diff.Bundle",
                    "CTL_DiffPanel_Next_Tooltip");
            _btNext = new JButtonOperator(this, new TooltipChooser(tooltip, getComparator()));
        }
        return _btNext;
    }
    
    /** Tries to find Go to Previous Difference JButton in diff view.
     * @return JButtonOperator
     */
    public JButtonOperator btPrevious() {
        if (_btPrevious==null) {
            String tooltip = Bundle.getString(
                    "org.netbeans.modules.versioning.system.cvss.ui.actions.diff.Bundle",
                    "CTL_DiffPanel_Prev_Tooltip");
            _btPrevious = new JButtonOperator(this, new TooltipChooser(tooltip, getComparator()));
        }
        return _btPrevious;
    }
    
    /** Tries to find "Remove vs Local" JToggleButton in diff view.
     * @return JToggleButtonOperator
     */
    public JToggleButtonOperator tbRemoteLocal() {
        if (_tbRemoveLocal==null) {
            _tbRemoveLocal = new JToggleButtonOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.actions.diff.Bundle",
                    "CTL_DiffPanel_All"));
        }
        return _tbRemoveLocal;
    }
    
    /** Tries to find "Local" JToggleButton in diff view.
     * @return JToggleButtonOperator
     */
    public JToggleButtonOperator tbLocal() {
        if (_tbLocal==null) {
            Operator.StringComparator oldComparator = Operator.getDefaultStringComparator();
            Operator.DefaultStringComparator comparator = new Operator.DefaultStringComparator(true, true);
            try {
                this.setComparator(comparator);
                _tbLocal = new JToggleButtonOperator(this, Bundle.getStringTrimmed(
                        "org.netbeans.modules.versioning.system.cvss.ui.actions.diff.Bundle",
                        "CTL_DiffPanel_Local"));
            } finally {
                this.setComparator(oldComparator);
            }
        }
        return _tbLocal;
    }
    
    /** Tries to find "Remote" JToggleButton in diff view.
     * @return JToggleButtonOperator
     */
    public JToggleButtonOperator tbRemote() {
        if (_tbRemote==null) {
            Operator.StringComparator oldComparator = Operator.getDefaultStringComparator();
            Operator.DefaultStringComparator comparator = new Operator.DefaultStringComparator(true, true);
            try {
                this.setComparator(comparator);
                _tbRemote = new JToggleButtonOperator(this, Bundle.getStringTrimmed(
                        "org.netbeans.modules.versioning.system.cvss.ui.actions.diff.Bundle",
                        "CTL_DiffPanel_Remote"));
            } finally {
                this.setComparator(oldComparator);
            }
        }
        return _tbRemote;
    }
    
    /** Tries to find Refresh Diff JButton in diff view.
     * @return JButtonOperator
     */
    public JButtonOperator btRefresh() {
        if (_btRefresh==null) {
            _btRefresh = new JButtonOperator(this, new TooltipChooser(
                    Bundle.getString(
                    "org.netbeans.modules.versioning.system.cvss.ui.actions.diff.Bundle",
                    "MSG_RefreshDiff_Tooltip"),
                    this.getComparator()));
        }
        return _btRefresh;
    }
    
    /** Tries to find Update JButton in diff view.
     * @return JButtonOperator
     */
    public JButtonOperator btUpdate() {
        if (_btUpdate==null) {
            _btUpdate = new JButtonOperator(this, new TooltipChooser(
                    Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.actions.diff.Bundle",
                    "MSG_UpdateDiff_Tooltip"),
                    this.getComparator()));
        }
        return _btUpdate;
    }
    
    /** Tries to find Commit JButton in diff view.
     * @return JButtonOperator
     */
    public JButtonOperator btCommit() {
        if (_btCommit==null) {
            _btCommit = new JButtonOperator(this, new TooltipChooser(
                    Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.actions.diff.Bundle",
                    "MSG_CommitDiff_Tooltip"),
                    this.getComparator()));
        }
        return _btCommit;
    }
    
    /** Tries to find files JComboBoxOperator in diff view.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cbFiles() {
        if (_cbFiles == null) {
            _cbFiles = new JComboBoxOperator(this);
        }
        return _cbFiles;
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
    
    /** checks or unchecks given JToggleButton
     * @param state boolean requested state
     */
    public void checkRemoteLocal(boolean state) {
        if (tbRemoteLocal().isSelected()!=state) {
            tbRemoteLocal().push();
        }
    }
    
    /** checks or unchecks given JToggleButton
     * @param state boolean requested state
     */
    public void checkLocal(boolean state) {
        if (tbLocal().isSelected()!=state) {
            tbLocal().push();
        }
    }
    
    /** checks or unchecks given JToggleButton
     * @param state boolean requested state
     */
    public void checkRemote(boolean state) {
        if (tbRemote().isSelected()!=state) {
            tbRemote().push();
        }
    }
    
    /** clicks on Refresh Diff JButton
     */
    public void refresh() {
        btRefresh().push();
    }
    
    /** clicks on Update JButton
     */
    public void update() {
        btUpdate().push();
    }
    
    /** clicks on Commit JButton and returns CommitOperator.
     * @return CommitOperator instance
     */
    public CommitOperator commit() {
        btCommit().pushNoBlock();
        return new CommitOperator();
    }
    
    /** Selects specified file in combo box.
     * @param name file name to be selected
     */
    public void selectFile(String name) {
        cbFiles().selectItem(name);
    }
    
    /** Selects index-th file in combo box.
     * @param index index of file to be selected
     */
    public void selectFile(int index) {
        cbFiles().selectItem(index);
    }
    
    //*****************************************
    // High-level functionality definition part
    //*****************************************
    
    /** Performs verification of VersioningOperator by accessing all its components.
     */
    public void verify() {
        btNext();
        btPrevious();
        tbRemoteLocal();
        tbLocal();
        tbRemote();
        btRefresh();
        btUpdate();
        btCommit();
        cbFiles();
    }
    
    /** SubChooser to determine TopComponent is instance of
     * org.netbeans.modules.versioning.system.cvss.ui.actions.diff.DiffExecutor$DiffTopComponent
     * Used in constructor.
     */
    private static final class DiffSubchooser implements ComponentChooser {
        public boolean checkComponent(Component comp) {
            return comp.getClass().getName().endsWith("DiffTopComponent"); // NOI18N
        }
        
        public String getDescription() {
            return "org.netbeans.modules.versioning.system.cvss.ui.actions.diff.DiffExecutor$DiffTopComponent"; // NOI18N
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

