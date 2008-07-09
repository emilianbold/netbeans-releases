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

package org.netbeans.modules.visualweb.gravy.actions;

import java.awt.Container;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import javax.swing.tree.TreePath;
// mdk import org.netbeans.modules.visualweb.gravy.MainWindowOperator;
import org.netbeans.modules.visualweb.gravy.Util;
import org.netbeans.modules.visualweb.gravy.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.drivers.input.KeyRobotDriver;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.util.EmptyVisualizer;
import org.openide.util.actions.SystemAction;

/** Ancestor class for all non-blocking actions.<p>
 * This class re-implements all blocking calls from parent Action class to
 * non-blocking call.<p>
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @see Action */
public class ActionNoBlock extends Action {
    
    /** creates new non-blocking Action instance without API_MODE support
     * @param menuPath action path in main menu (use null value if menu mode is not supported)
     * @param popupPath action path in popup menu (use null value if popup mode shell is not supported) */    
    public ActionNoBlock(String menuPath, String popupPath) {
        super(menuPath, popupPath);
    }
    
    /** creates new non-blocking Action instance
     * @param menuPath action path in main menu (use null value if menu mode is not supported)
     * @param popupPath action path in popup menu (use null value if popup mode is not supported)
     * @param systemActionClass String class name of SystemAction (use null value if API mode is not supported) */    
    public ActionNoBlock(String menuPath, String popupPath, String systemActionClass) {
        super(menuPath, popupPath, systemActionClass);
    }
    
    /** creates new Action instance without API_MODE support
     * @param shortcuts array of Shortcut instances (use null value if shortcut mode is not supported)
     * @param menuPath action path in main menu (use null value if menu mode is not supported)
     * @param popupPath action path in popup menu (use null value if popup mode shell is not supported) */    
    public ActionNoBlock(String menuPath, String popupPath, Shortcut[] shortcuts) {
        super(menuPath, popupPath, shortcuts);
    }

    /** creates new Action instance without API_MODE support
     * @param shortcut Shortcut (use null value if shortcut mode is not supported)
     * @param menuPath action path in main menu (use null value if menu mode is not supported)
     * @param popupPath action path in popup menu (use null value if popup mode shell is not supported) */    
    public ActionNoBlock(String menuPath, String popupPath, Shortcut shortcut) {
        super(menuPath, popupPath, shortcut);
    }
    
    /** creates new Action instance
     * @param shortcuts array of Shortcut instances (use null value if shortcut mode is not supported)
     * @param menuPath action path in main menu (use null value if menu mode is not supported)
     * @param popupPath action path in popup menu (use null value if popup mode is not supported)
     * @param systemActionClass String class name of SystemAction (use null value if API mode is not supported) */    
    public ActionNoBlock(String menuPath, String popupPath, String systemActionClass, Shortcut[] shortcuts) {
        super(menuPath, popupPath, systemActionClass, shortcuts);
    }

    /** creates new Action instance
     * @param shortcut Shortcut String (use null value if menu mode is not supported)
     * @param menuPath action path in main menu (use null value if menu mode is not supported)
     * @param popupPath action path in popup menu (use null value if popup mode is not supported)
     * @param systemActionClass String class name of SystemAction (use null value if API mode is not supported) */    
    public ActionNoBlock(String menuPath, String popupPath, String systemActionClass, Shortcut shortcut) {
        super(menuPath, popupPath, systemActionClass, shortcut);
    }
    
    /** performs action through main menu
     * @throws UnsupportedOperationException when action does not support menu mode */    
    public void performMenu() {
        if (menuPath==null) {
            throw new UnsupportedOperationException(getClass().toString()+" does not define menu path");
        }
        // Need to wait here to be more reliable.
        // TBD - It can be removed after issue 23663 is solved.
        new EventTool().waitNoEvent(500);
// mdk        MainWindowOperator.getDefault().menuBar().pushMenuNoBlock(menuPath, "|");
        Util.getMainMenu().pushMenuNoBlock(menuPath, "|");
        try {
            Thread.sleep(AFTER_ACTION_WAIT_TIME);
        } catch (Exception e) {
            throw new JemmyException("Sleeping interrupted", e);
        }
    }    
    
    /** performs action through popup menu
     * @param nodes nodes to be action performed on  
     * @throws UnsupportedOperationException when action does not support popup mode */    
    public void performPopup(Node[] nodes) {
        if (popupPath==null)
            throw new UnsupportedOperationException(getClass().toString()+" does not define popup path");
        testNodes(nodes);
        TreePath paths[]=new TreePath[nodes.length];
        for (int i=0; i<nodes.length; i++) {
            paths[i]=nodes[i].getTreePath();
        }
        Operator.ComponentVisualizer treeVisualizer = nodes[0].tree().getVisualizer();
        Operator.ComponentVisualizer oldVisualizer = null;
        // If visualizer of JTreeOperator is EmptyVisualizer, we need
        // to avoid making tree component visible in callPopup method.
        // So far only known case is tree from TreeTableOperator.
        if(treeVisualizer instanceof EmptyVisualizer) {
            oldVisualizer = Operator.getDefaultComponentVisualizer();
            Operator.setDefaultComponentVisualizer(treeVisualizer);
        }
        // Need to wait here to be more reliable.
        // TBD - It can be removed after issue 23663 is solved.
        new EventTool().waitNoEvent(500);
        JPopupMenuOperator popup = new JPopupMenuOperator(nodes[0].tree().callPopupOnPaths(paths));
        // restore previously used default visualizer
        if(oldVisualizer != null) {
            Operator.setDefaultComponentVisualizer(oldVisualizer);
        }
        popup.setComparator(getComparator());
        popup.pushMenuNoBlock(popupPath, "|");
        try {
            Thread.sleep(AFTER_ACTION_WAIT_TIME);
        } catch (Exception e) {
            throw new JemmyException("Sleeping interrupted", e);
        }
    }
    
    /** performs action through popup menu
     * @param component component to be action performed on
     * @throws UnsupportedOperationException when action does not support popup mode */    
    public void performPopup(ComponentOperator component) {
        if (popupPath==null)
            throw new UnsupportedOperationException(getClass().toString()+" does not define popup path");
        // Need to wait here to be more reliable.
        // TBD - It can be removed after issue 23663 is solved.
        new EventTool().waitNoEvent(500);
        component.clickForPopup();
        JPopupMenuOperator popup=new JPopupMenuOperator(component);
        popup.setComparator(getComparator());
        popup.pushMenuNoBlock(popupPath, "|");
        try {
            Thread.sleep(AFTER_ACTION_WAIT_TIME);
        } catch (Exception e) {
            throw new JemmyException("Sleeping interrupted", e);
        }
    }

    /** performs action through API  
     * @throws UnsupportedOperationException when action does not support API mode */    
    public void performAPI() {
        if (systemActionClass==null) {
            throw new UnsupportedOperationException(getClass().toString()+" does not define SystemAction");
        }
        new Thread(new Runnable() {
            public void run() {
                try {
                    // Actions have to be invoked in dispatch thread 
                    // (see http://www.netbeans.org/issues/show_bug.cgi?id=35755)
                    EventQueue.invokeAndWait(new Runnable() {
                        public void run() {
                            SystemAction.get(systemActionClass).actionPerformed(
                                                        new ActionEvent(new Container(), 0, null));
                        }
                    });
                } catch (Exception e) {
                    throw new JemmyException("Exception while performing action in dispatch thread", e);
                }
            }
        }, "thread performing action through API").start();
        try {
            Thread.sleep(AFTER_ACTION_WAIT_TIME);
        } catch (Exception e) {
            throw new JemmyException("Interrupted", e);
        }
    }

    /** performs action through shortcut
     * @throws UnsupportedOperationException when action does not support shortcut mode */    
    public void performShortcut() {
        if (shortcuts == null) {
            throw new UnsupportedOperationException(getClass().toString()+" does not define shortcut");
        }
        new Thread(new Runnable() {
            public void run() {
                for(int i=0; i<shortcuts.length; i++) {
                    new KeyRobotDriver(null).pushKey(null, shortcuts[i].getKeyCode(), shortcuts[i].getKeyModifiers(), JemmyProperties.getCurrentTimeouts().create("Timeouts.DeltaTimeout"));
                    JemmyProperties.getProperties().getTimeouts().sleep("Action.WaitAfterShortcutTimeout");
                }
            }
        }, "thread performing action through shortcut").start();
        try {
            Thread.sleep(AFTER_ACTION_WAIT_TIME);
        } catch (Exception e) {
            throw new JemmyException("Sleeping interrupted", e);
        }
    }
}
