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

package org.netbeans.modules.visualweb.gravy;

import java.awt.Component;
import java.awt.Container;
import java.io.PrintStream;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;

import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.DialogOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComponentOperator.JComponentByTipFinder;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.openide.awt.Toolbar;

import org.openide.awt.StatusDisplayer;

/**
 *
 * @author  shura
 */
public class MainWindowOperator extends JFrameOperator {

    JButtonOperator deployButton;
    
    /** Creates a new instance of MainWindowOperator */
    public MainWindowOperator() {
            super((JFrame) RaveWindowOperator.getDefaultRave().getSource());
            copyEnvironment(RaveWindowOperator.getDefaultRave());
    }

    /** Pushes Save All Button in Main Toolbar 
     */
    public void saveAll() {
        new JButtonOperator(this, new JComponentByTipFinder("Save All")).push();
    }
    
    /** Pushes Deploy button from Main Toolbar and waits some timeout 
     */
    public DeploymentDialogOperator deploy() {
        btDeploy().push();
	DeploymentDialogOperator dlg = null;
	long oldWaitTime = Util.getMainWindow().getTimeouts().getTimeout("DialogWaiter.WaitDialogTimeout");
	try {
            Util.getMainWindow().getTimeouts().setTimeout("DialogWaiter.WaitDialogTimeout", 100000);
	    dlg = new DeploymentDialogOperator();
	} catch(Exception e) {
	    e.printStackTrace();
            Util.getMainWindow().getTimeouts().setTimeout("DialogWaiter.WaitDialogTimeout", oldWaitTime);
        }
	return(dlg);
    }
    
    /** Deploy project and waits until deployment completed */
    public void deployProject() {
        DeploymentDialogOperator dlg=deploy();
        dlg.setAutoHide(false);
        dlg.waitCompleted();
        Util.wait(1000);
        dlg.close();
    }

    /** Pushes deploy button from Main Toolbar 
     */
    public JButtonOperator btDeploy() {
        if(deployButton == null) {
            deployButton = new JButtonOperator(this, new JComponentByTipFinder(
                    Bundle.getStringTrimmed("org.netbeans.modules.project.ui.actions.Bundle",
                    "LBL_RunMainProjectAction_Name")));
            deployButton.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 60000);
        }
        return(deployButton);
    }
    private static StatusTextTracer statusTextTracer = null;

    /** Returns text from status bar.
     * @return  currently displayed text
     */
    public String getStatusText() {
        return org.openide.awt.StatusDisplayer.getDefault().getStatusText();
    }


    /** Sets given text to main window's status bar.
     * @param newStatusText string to be displayed in status bar
     */
    public void setStatusText(String newStatusText) {
        org.openide.awt.StatusDisplayer.getDefault().setStatusText(newStatusText);
    }

    /** Returns singleton instance of StatusTextTracer.
     * @return singleton instance of StatusTextTracer
     */
    public synchronized StatusTextTracer getStatusTextTracer() {
        if(statusTextTracer == null) {
            statusTextTracer = new StatusTextTracer();
        }
        return statusTextTracer;
    }
    
    /** Waits until given text appears in the main window status bar.
     * If you want to trace status messages during an operation is proceed,
     * use {@link StatusTextTracer}.
     * @param text  a text to wait for
     */
    public void waitStatusText(final String text) {
        getStatusTextTracer().start();
        try {
            // not wait in case status text was already printed out
            if(!getComparator().equals(getStatusText(), text)) {
                getStatusTextTracer().waitText(text);
            }
        } finally {
            getStatusTextTracer().stop();
        }
    }


    /** Class to trace messages printed to status bar of the Main Window.
     * <p>
     * Usage:<br>
     * <pre>
     *      MainWindowOperator.StatusTextTracer stt = MainWindowOperator.getDefault().getStatusTextTracer();
     *      // start tracing
     *      stt.start();
     *      // compile action will produce at least two messages: "Compiling ...",
     *      // "Finished ..."
     *      new CompileAction().performAPI();
     *
     *      // waits for "Compiling" status text
     *      stt.waitText("Compiling");
     *      // waits for "Finished" status text
     *      stt.waitText("Finished");
     *
     *      // order is not significant => following works as well
     *      stt.waitText("Finished");
     *      stt.waitText("Compiling");
     *
     *      // to be order significant, set removedCompared parameter to true
     *      stt.waitText("Compiling", true);
     *      stt.waitText("Finished", true);
     *
     *      // history was removed by above methods => need to produce a new messages
     *      new CompileAction().performAPI();
     *
     *      // order is significant if removedCompared parameter is true =>
     *      // => following fails because Finished is shown as second
     *      stt.waitText("Finished", true);
     *      stt.waitText("Compiling", true);
     *
     *      // stop tracing
     *      stt.stop();
     * </pre>
     */
    public class StatusTextTracer implements ChangeListener {
        /** List of all messages */
        private ArrayList statusTextHistory;
        
        /** Creates new instance. */
        public StatusTextTracer() {
            this.statusTextHistory = new ArrayList();
        }
        
        /** Starts to register all status messages into history array.
         * Exactly, it adds the listener to org.openide.awt.StatusDisplayer.
         * It clears possible previously filled history array before.
         */
        public void start() {
            stop();
            clear();
            StatusDisplayer.getDefault().addChangeListener(this);
        }
        
        /** Stops registering of status messages. Exactly, it removes the
         * listener from org.openide.awt.StatusDisplayer.
         */
        public void stop() {
            StatusDisplayer.getDefault().removeChangeListener(this);
        }
        
        /** Called when status text was changed. It adds status text to history
         * array.
         * @param evt change event - not used
         */
        public void stateChanged(ChangeEvent evt) {
            synchronized (this) {
                statusTextHistory.add(StatusDisplayer.getDefault().getStatusText());
                // print message to jemmy output stream
                JemmyProperties.getCurrentOutput().printTrace("Status text changed to: \""+
                                           StatusDisplayer.getDefault().getStatusText()+"\"");
            }
        }
        
        /** Clears status text history array. */
        public void clear() {
            synchronized (this) {
                statusTextHistory.clear();
            }
        }
        
        /** Checks whether given text equals to any of messages in the history
         * array. Comparator of this MainWindowOperator instance is used.
         * If <tt>removeCompared</tt> parameter is set to <tt>true</tt>,
         * messages already compared are removed from history array. Otherwise
         * messages are not removed until {@link #clear} or {@link #start} are
         * called.
         * @param text a text to be compared
         * @param removeCompared whether to remove already compared messages from
         * history array
         * @return true if text matches any of messages in the history array;
         * false otherwise
         */
        public boolean contains(String text, boolean removeCompared) {
            StringComparator comparator = getComparator();
            synchronized (this) {
                if(removeCompared) {
                    while(!statusTextHistory.isEmpty()) {
                        String status = (String)statusTextHistory.remove(0);
                        if(comparator.equals(status, text)) {
                            return true;
                        }
                    }
                } else {
                    for (int i = 0; i < statusTextHistory.size(); i ++) {
                        if(comparator.equals((String)statusTextHistory.get(i), text)) {
                            return true;
                        }
                    }
                }
                return false;
            }
        }
        
        /** Waits for text to be shown in the Main Window status bar not 
         * removing any message from history.
         * Comparator of this MainWindowOperator instance is used.
         * It throws TimeoutExpiredException if timeout expires.
         * @param text a text to wait for
         */
        public void waitText(final String text) {
            waitText(text, false);
        }
        
        /** Waits for text to be shown in the Main Window status bar.
         * Comparator of this MainWindowOperator instance is used.
         * If <tt>removeCompared</tt> parameter is set to <tt>true</tt>,
         * messages already compared are removed from history array. It satisfies
         * that order of messages is significant when this method is called
         * more than once.
         * If <tt>removeCompared</tt> parameter is set to <tt>false</tt>,
         * messages are not removed until {@link #clear} or {@link #start} are
         * called and its order is not taken into account.
         * @param text a text to wait for
         * @param removeCompared whether to remove already compared messages from
         * history array
         */
        public void waitText(final String text, final boolean removeCompared) {
            try {
                new Waiter(new Waitable() {
                    public Object actionProduced(Object anObject) {
                        return contains(text, removeCompared) ? Boolean.TRUE : null;
                    }
                    public String getDescription() {
                        return("Wait status text equals to "+text);
                    }
                }).waitAction(null);
            } catch (InterruptedException e) {
                throw new JemmyException("Interrupted.", e);
            }
        }
        
        /** Calls {@link #stop} at the end of life cycle of this class. */
        public void finalize() {
            stop();
        }

        /** Returns list of elements collected from the moment method 
         * {@link #start} was called. Remember, if <tt>removeCompared</tt> 
         * parameter is set to <tt>true</tt> in some of methods,
         * messages already compared are removed from history array.
         * @return ArrayList of elements representing status text messages
         */
        public ArrayList getStatusTextHistory() {
            return statusTextHistory;
        }

        /** Prints list of elements collected from the moment method 
         * {@link #start} was called. Remember, if <tt>removeCompared</tt> 
         * parameter is set to <tt>true</tt> in some of methods,
         * messages already compared are removed from history array.
         * @param outputPrintStream stream to print output in
         */
        public void printStatusTextHistory(PrintStream outputPrintStream) {
            for (int i = 0; i < statusTextHistory.size(); i ++) {
                outputPrintStream.println(statusTextHistory.get(i).toString());
            }
        }
    }


    /***************** methods for toolbars manipulation *******************/
    
    /** Returns ContainerOperator representing index-th floating toolbar in
     * IDE main window. Toolbars are NOT indexed from left to right.
     * @param index index of toolbar to find
     * @return ContainerOperator instance representing a toolbar
     */
    public ContainerOperator getToolbar(int index) {
        ComponentChooser chooser = new ToolbarChooser();
        return new ContainerOperator((Container)waitComponent((Container)getSource(),
                                     chooser, index));
    }
    
    /** Returns ContainerOperator representing floating toolbar with given name.
     * @param toolbarName toolbar's display name. It is shown in its tooltip.
     * @return  ContainerOperator instance representing a toolbar
     */
    public ContainerOperator getToolbar(String toolbarName) {
        ComponentChooser chooser = new ToolbarChooser(toolbarName, getComparator());
        return new ContainerOperator((Container)waitComponent((Container)getSource(), chooser));
    }
    
    /** Returns number of toolbars currently shown in IDE.
     * @return number of toolbars
     */
    public int getToolbarCount() {
        ToolbarChooser chooser = new ToolbarChooser("Non sense name - @#$%^&*", //NOI18N
                                                    getComparator());
        findComponent((Container)getSource(), chooser);
        return chooser.getCount();
    }
    
    /** Returns display name of toolbar with given index. Toolbars are NOT
     * indexed from left to right.
     * @param index index of toolbar
     * @return display name of toolbar
     */
    public String getToolbarName(int index) {
        return ((Toolbar)getToolbar(index).getSource()).getDisplayName();
    }
    
    /** Return JButtonOperator representing a toolbar button found by given
     * tooltip within given toolbar operator.
     * @param toolbarOper ContainerOperator of a toolbar.
     *          Use {@link #getToolbar(String)} or {@link #getToolbar(int)}
     *          to obtain an operator.
     * @param buttonTooltip tooltip of toolbar button
     * @return JButtonOperator instance of found toolbar button
     */
    public JButtonOperator getToolbarButton(ContainerOperator toolbarOper, String buttonTooltip) {
        ToolbarButtonChooser chooser = new ToolbarButtonChooser(buttonTooltip, getComparator());
        return new JButtonOperator(JButtonOperator.waitJButton(
        (Container)toolbarOper.getSource(), chooser));
    }
    
    /** Return JButtonOperator representing index-th toolbar button within given
     * toolbar operator.
     * @param toolbarOper ContainerOperator of a toolbar.
     *          Use {@link #getToolbar(String)} or {@link #getToolbar(int)}
     *          to obtain an operator.
     * @param index index of toolbar button to find
     * @return JButtonOperator instance of found toolbar button
     */
    public JButtonOperator getToolbarButton(ContainerOperator toolbarOper, int index) {
        return new JButtonOperator(toolbarOper, index);
    }
    
    /** Pushes popup menu on toolbars. It doesn't matter on which toolbar it is
     * invoked, everytime it is the same. That's why popup menu is invoked on
     * the right side of toolbar with index 0.
     * @param popupPath path to menu item (e.g. "Edit")
     */
    public void pushToolbarPopupMenu(String popupPath) {
        ContainerOperator contOper = getToolbar(0);
        contOper.clickForPopup(contOper.getWidth()-1, contOper.getHeight()/2);
        new JPopupMenuOperator().pushMenu(popupPath, "|");
    }
    
    /** Pushes popup menu on toolbars - no block further execution.
     * It doesn't matter on which toolbar it is
     * invoked, everytime it is the same. That's why popup menu is invoked on
     * the right side of toolbar with index 0.
     * @param popupPath path to menu item (e.g. "Save Configuration...")
     */
    public void pushToolbarPopupMenuNoBlock(String popupPath) {
        ContainerOperator contOper = getToolbar(0);
        contOper.clickForPopup(contOper.getWidth()-1, contOper.getHeight()/2);
        new JPopupMenuOperator().pushMenuNoBlock(popupPath, "|");
    }
    
    /** Drags a toolbar to a new position determined by [x, y] relatively.
     * @param toolbarOper ContainerOperator of a toolbar.
     *          Use {@link #getToolbar(String)} or {@link #getToolbar(int)}
     *          to obtain an operator.
     * @param x relative move along x direction
     * @param y relative move along y direction
     */
    public void dragNDropToolbar(ContainerOperator toolbarOper, int x, int y) {
        ComponentChooser chooser = new ComponentChooser() {
            public boolean checkComponent(Component comp) {
                if(comp instanceof JPanel) {
                    String className = comp.getClass().getName();
                    return className.equals("org.openide.awt.Toolbar$ToolbarBump") ||
                           // used in Windows L&F
                           className.equals("org.openide.awt.Toolbar$ToolbarGrip");
                }
                return false;
            }
            public String getDescription() {
                return "org.openide.awt.Toolbar$ToolbarBump or org.openide.awt.Toolbar$ToolbarGrip";
            }
        };
        Component comp = findComponent((Container)toolbarOper.getSource(), chooser);
        new ComponentOperator(comp).dragNDrop(comp.getWidth()/2, comp.getHeight()/2, x, y);
    }
    
    
    /** Chooser which can be used to find a org.openide.awt.Toolbar component or
     * count a number of such components in given container.
     */
    private static class ToolbarChooser implements ComponentChooser {
        private String toolbarName;
        private StringComparator comparator;
        private int count = 0;
        
        /** Use this to find org.openide.awt.Toolbar component with given name. */
        public ToolbarChooser(String toolbarName, StringComparator comparator) {
            this.toolbarName = toolbarName;
            this.comparator = comparator;
        }
        
        /** Use this to count org.openide.awt.Toolbar components in given container. */
        public ToolbarChooser() {
            this.comparator = null;
        }
        
        public boolean checkComponent(Component comp) {
            if(comp instanceof org.openide.awt.Toolbar) {
                count++;
                if(comparator != null) {
                    return comparator.equals(((Toolbar)comp).getDisplayName(), toolbarName);
                } else {
                    return true;
                }
            }
            return false;
        }
        
        public String getDescription() {
            return "org.openide.awt.Toolbar";
        }
        
        public int getCount() {
            return count;
        }
    }
    
    /** Chooser which can be used to find a component with given tooltip,
     * for example a toolbar button.
     */
    private static class ToolbarButtonChooser implements ComponentChooser {
        private String buttonTooltip;
        private StringComparator comparator;
        
        public ToolbarButtonChooser(String buttonTooltip, StringComparator comparator) {
            this.buttonTooltip = buttonTooltip;
            this.comparator = comparator;
        }
        
        public boolean checkComponent(Component comp) {
            return comparator.equals(((JComponent)comp).getToolTipText(), buttonTooltip);
        }
        
        public String getDescription() {
            return "Toolbar button with tooltip \""+buttonTooltip+"\".";
        }
    }

}
