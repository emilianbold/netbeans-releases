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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.visualweb.gravy.debugger;

import org.netbeans.modules.visualweb.gravy.TopComponentOperator;
import org.netbeans.modules.visualweb.gravy.Util;

import java.awt.Component;

import javax.swing.JDesktopPane;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.JemmyException;

import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JInternalFrameOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JToggleButtonOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;
import org.netbeans.jemmy.operators.Operator;

/** 
 * DebuggerWindowOperator class 
 */
public class DebuggerWindowOperator extends TopComponentOperator{
    ComponentOperator desktop = null;
    JTextComponentOperator query = null;

    private JToggleButtonOperator _tbSessions;
    private JToggleButtonOperator _tbThreads;
    private JToggleButtonOperator _tbCallStack;
    private JToggleButtonOperator _tbLocalVariables;
    private JToggleButtonOperator _tbAllInOne;
    private JToggleButtonOperator _tbWatches;
    private JToggleButtonOperator _tbClasses;
    private JToggleButtonOperator _tbBreakpoints;
    private JToggleButtonOperator _tbProperties;
    private JToggleButtonOperator _tbPublic;
    private JToggleButtonOperator _tbProtected;
    private JToggleButtonOperator _tbPrivate;
    private JToggleButtonOperator _tbPackagePrivate;
    private JToggleButtonOperator _tbStatic;
    private JToggleButtonOperator _tbInherited;

    private JTableOperator _tabTreeTable;

    private String [] views =  {"Sessions", "Threads", "CallStack", "LocalVariables", "AllInOne", "Watches", "Classes", "Breakpoints", "Properties"}; 

    public DebuggerWindowOperator(ContainerOperator parent) {
        super(parent, new DebuggerWindowChooser("Debugger"));
    }

    public DebuggerWindowOperator() {
        this(Util.getMainWindow());
    }

    /** Show debugger window
     */
    public DebuggerWindowOperator show() {
        this.setComparator(new Operator.DefaultStringComparator(false, true));
        Util.getMainTab().setSelectedIndex(Util.getMainTab().findPage("Debugger"));
        return(new DebuggerWindowOperator(Util.getMainWindow()));
    }


    /** Tries to find "" ToolbarToggleButton in this dialog.
     * @return JToggleButtonOperator
     */
    public JToggleButtonOperator tbSessions() {
            _tbSessions = new JToggleButtonOperator(this, "", 0);
        return _tbSessions;
    }

    /** Tries to find "" ToolbarToggleButton in this dialog.
     * @return JToggleButtonOperator
     */
    public JToggleButtonOperator tbThreads() {
            _tbThreads = new JToggleButtonOperator(this, "", 1);
        return _tbThreads;
    }

    /** Tries to find "" ToolbarToggleButton in this dialog.
     * @return JToggleButtonOperator
     */
    public JToggleButtonOperator tbCallStack() {
            _tbCallStack = new JToggleButtonOperator(this, "", 2);
        return _tbCallStack;
    }

    /** Tries to find "" ToolbarToggleButton in this dialog.
     * @return JToggleButtonOperator
     */
    public JToggleButtonOperator tbLocalVariables() {
            _tbLocalVariables = new JToggleButtonOperator(this, "", 3);
        return _tbLocalVariables;
    }

    /** Tries to find "" ToolbarToggleButton in this dialog.
     * @return JToggleButtonOperator
     */
    public JToggleButtonOperator tbAllInOne() {
            _tbAllInOne = new JToggleButtonOperator(this, "", 4);
        return _tbAllInOne;
    }

    /** Tries to find "" ToolbarToggleButton in this dialog.
     * @return JToggleButtonOperator
     */
    public JToggleButtonOperator tbWatches() {
            _tbWatches = new JToggleButtonOperator(this, "", 5);
        return _tbWatches;
    }

    /** Tries to find "" ToolbarToggleButton in this dialog.
     * @return JToggleButtonOperator
     */
    public JToggleButtonOperator tbClasses() {
            _tbClasses = new JToggleButtonOperator(this, "", 6);
        return _tbClasses;
    }

    /** Tries to find "" ToolbarToggleButton in this dialog.
     * @return JToggleButtonOperator
     */
    public JToggleButtonOperator tbBreakpoints() {
            _tbBreakpoints = new JToggleButtonOperator(this, "", 7);
        return _tbBreakpoints;
    }

    /** Tries to find "" ToolbarToggleButton in this dialog.
     * @return JToggleButtonOperator
     */
    public JToggleButtonOperator tbProperties() {
            _tbProperties = new JToggleButtonOperator(this, "", 8);
        return _tbProperties;
    }

    /** Tries to find "" ToolbarToggleButton in this dialog.
     * @return JToggleButtonOperator
     */
    public JToggleButtonOperator tbPublic() {
            _tbPublic = new JToggleButtonOperator(this, "", 9);
        return _tbPublic;
    }

    /** Tries to find "" ToolbarToggleButton in this dialog.
     * @return JToggleButtonOperator
     */
    public JToggleButtonOperator tbProtected() {
            _tbProtected = new JToggleButtonOperator(this, "", 10);
        return _tbProtected;
    }

    /** Tries to find "" ToolbarToggleButton in this dialog.
     * @return JToggleButtonOperator
     */
    public JToggleButtonOperator tbPrivate() {
            _tbPrivate = new JToggleButtonOperator(this, "", 11);
        return _tbPrivate;
    }

    /** Tries to find "" ToolbarToggleButton in this dialog.
     * @return JToggleButtonOperator
     */
    public JToggleButtonOperator tbPackagePrivate() {
            _tbPackagePrivate = new JToggleButtonOperator(this, "", 12);
        return _tbPackagePrivate;
    }

    /** Tries to find "" ToolbarToggleButton in this dialog.
     * @return JToggleButtonOperator
     */
    public JToggleButtonOperator tbStatic() {
            _tbStatic = new JToggleButtonOperator(this, "", 13);
        return _tbStatic;
    }

    /** Tries to find "" ToolbarToggleButton in this dialog.
     * @return JToggleButtonOperator
     */
    public JToggleButtonOperator tbInherited() {
            _tbInherited = new JToggleButtonOperator(this, "", 14);
        return _tbInherited;
    }


    /** checks or unchecks given ToolbarToggleButton
     * @param state boolean requested state
     */
    public void checkBreakpoints(boolean state) {
        if (tbBreakpoints().isSelected()!=state) {
            tbBreakpoints().push();
        }
    }

    /** Get state of the given ToolbarToggleButton
     */
    public boolean getBreakpointsState() {
        return tbBreakpoints().isSelected();
    }


    /** checks or unchecks given ToolbarToggleButton
     * @param state boolean requested state
     */
    public void checkSessions(boolean state) {
        if (tbSessions().isSelected()!=state) {
            tbSessions().push();
        }
    }

    /** Get state of the given ToolbarToggleButton
     */
    public boolean getSessionsState() {
        return tbSessions().isSelected();
    }

    /** checks or unchecks given ToolbarToggleButton
     * @param state boolean requested state
     */
    public void checkThreads(boolean state) {
        if (tbThreads().isSelected()!=state) {
            tbThreads().push();
        }
    }

    /** Get state of the given ToolbarToggleButton
     */
    public boolean getThreadsState() {
        return tbThreads().isSelected();
    }

    /** checks or unchecks given ToolbarToggleButton
     * @param state boolean requested state
     */
    public void checkCallStack(boolean state) {
        if (tbCallStack().isSelected()!=state) {
            tbCallStack().push();
        }
    }

    /** Get state of the given ToolbarToggleButton
     */
    public boolean getCallStackState() {
        return tbCallStack().isSelected();
    }

    /** checks or unchecks given ToolbarToggleButton
     * @param state boolean requested state
     */
    public void checkLocalVariables(boolean state) {
        if (tbLocalVariables().isSelected()!=state) {
            tbLocalVariables().push();
        }
    }

    /** Get state of the given ToolbarToggleButton
     */
    public boolean getLocalVariablesState() {
        return tbLocalVariables().isSelected();
    }

    /** checks or unchecks given ToolbarToggleButton
     * @param state boolean requested state
     */
    public void checkAllInOne(boolean state) {
        if (tbAllInOne().isSelected()!=state) {
            tbAllInOne().push();
        }
    }

    /** Get state of the given ToolbarToggleButton
     */
    public boolean getAllInOneState() {
        return tbAllInOne().isSelected();
    }

    /** checks or unchecks given ToolbarToggleButton
     * @param state boolean requested state
     */
    public void checkWatches(boolean state) {
        if (tbWatches().isSelected()!=state) {
            tbWatches().push();
        }
    }

    /** Get state of the given ToolbarToggleButton
     */
    public boolean getWatchesState() {
        return tbWatches().isSelected();
    }

    /** checks or unchecks given ToolbarToggleButton
     * @param state boolean requested state
     */
    public void checkClasses(boolean state) {
        if (tbClasses().isSelected()!=state) {
            tbClasses().push();
        }
    }

    /** Get state of the given ToolbarToggleButton
     */
    public boolean getClassesState() {
        return tbClasses().isSelected();
    }

    /** checks or unchecks given ToolbarToggleButton
     * @param state boolean requested state
     */
    public void checkProperties(boolean state) {
        if (tbProperties().isSelected()!=state) {
            tbProperties().push();
        }
    }

    /** Get state of the given ToolbarToggleButton
     */
    public boolean getPropertiesState() {
        return tbProperties().isSelected();
    }


    /** Tries to find null TreeTable in this dialog.
     * @return JTableOperator
     */
    public JTableOperator getJTableOperator(String view) {
        int viewNumber = -1;
        int displayedViews = -1;
        do {
            viewNumber++;
            switch (viewNumber) {
                case 0: {
                    if (views[viewNumber].equals(view)) {
                        checkSessions(true);
                        displayedViews++;
                    } else if (getSessionsState()) {
                        displayedViews++;
                    }                           
                    break;
                }
                case 1: {
                    if (views[viewNumber].equals(view)) {
                        checkThreads(true);
                        displayedViews++;
                    } else if (getThreadsState()) {
                        displayedViews++;
                    }                           
                    break;
                }
                case 2: {
                    if (views[viewNumber].equals(view)) {
                        checkCallStack(true);
                        displayedViews++;                        
                    } else if (getCallStackState()) {
                       displayedViews++;
                    }                           
                    break;
                }
                case 3: {
                    if (views[viewNumber].equals(view)) {
                        checkLocalVariables(true);
                        displayedViews++;
                    } else if (getLocalVariablesState()) {
                        displayedViews++;
                    }                           
                    break;
                }
                case 4: {
                    if (views[viewNumber].equals(view)) {
                        checkAllInOne(true);
                        displayedViews++;
                    } else if (getAllInOneState()) {
                        displayedViews++;
                    }                           
                    break;
                }
                case 5: {
                    if (views[viewNumber].equals(view)) {
                        checkWatches(true);
                        displayedViews++;
                    } else if (getWatchesState()) {
                        displayedViews++;
                    }                           
                    break;
                }
                case 6: {
                    if (views[viewNumber].equals(view)) {
                        checkClasses(true);
                        displayedViews++;
                    } else if (getClassesState()) {
                        displayedViews++;
                    }                           
                    break;
                }
                case 7: {
                    if (views[viewNumber].equals(view)) {
                        checkBreakpoints(true);
                        displayedViews++;
                    } else if (getBreakpointsState()) {
                        displayedViews++;
                    }                           
                    break;
                }
                case 8: {
                    if (views[viewNumber].equals(view)) {
                        checkProperties(true);
                        displayedViews++;
                    } else if (getPropertiesState()) {
                        displayedViews++;
                    }                           
                    break;
                }                                
            }
            
        } while (!views[viewNumber].equals(view));        
        _tabTreeTable = new JTableOperator(this, displayedViews);
        return _tabTreeTable;
    }

    public static class DebuggerWindowChooser implements ComponentChooser {
        String ID;
        private Operator.StringComparator comparator;
        public DebuggerWindowChooser(String ID) {
            this(ID, new Operator.DefaultStringComparator(false, false));
        }
        public DebuggerWindowChooser(String ID, Operator.StringComparator comparator){
            this.ID = ID;
            this.comparator = comparator;
        }
        public boolean checkComponent(Component comp) {
            if(comp !=null && ((Component)comp).getName()!=null)
             return(comp instanceof Component && ((Component)comp).getName().equals(ID) );
            else return false;
        }
        public String getDescription() {
            return("A Component with \"" + ID + "\" ID");
        }
    }


}
