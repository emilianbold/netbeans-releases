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
package org.netbeans.jellytools.actions;

import java.awt.Container;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import javax.swing.KeyStroke;
import javax.swing.tree.TreePath;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.nodes.Node;
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
    
    /** creates new ActionNoBlock instance without API_MODE support
     * @param shortcuts array of Shortcut instances (use null value if shortcut mode is not supported)
     * @param menuPath action path in main menu (use null value if menu mode is not supported)
     * @param popupPath action path in popup menu (use null value if popup mode shell is not supported)
     * @deprecated Use {@link ActionNoBlock#ActionNoBlock(String menuPath, String popupPath, KeyStroke[] keystrokes)} instead.
    */
    public ActionNoBlock(String menuPath, String popupPath, Shortcut[] shortcuts) {
        super(menuPath, popupPath, shortcuts);
    }

    /** creates new ActionNoBlock instance without API_MODE support
     * @param shortcut Shortcut (use null value if shortcut mode is not supported)
     * @param menuPath action path in main menu (use null value if menu mode is not supported)
     * @param popupPath action path in popup menu (use null value if popup mode shell is not supported)
     * @deprecated Use {@link ActionNoBlock#ActionNoBlock(String menuPath, String popupPath, KeyStroke keystroke)} instead.
    */
    public ActionNoBlock(String menuPath, String popupPath, Shortcut shortcut) {
        super(menuPath, popupPath, shortcut);
    }
    
    /** creates new ActionNoBlock instance
     * @param shortcuts array of Shortcut instances (use null value if shortcut mode is not supported)
     * @param menuPath action path in main menu (use null value if menu mode is not supported)
     * @param popupPath action path in popup menu (use null value if popup mode is not supported)
     * @param systemActionClass String class name of SystemAction (use null value if API mode is not supported)
     * @deprecated Use {@link ActionNoBlock#ActionNoBlock(String menuPath, String popupPath, String systemActionClass, KeyStroke[] keystrokes)} instead.
    */
    public ActionNoBlock(String menuPath, String popupPath, String systemActionClass, Shortcut[] shortcuts) {
        super(menuPath, popupPath, systemActionClass, shortcuts);
    }

    /** creates new ActionNoBlock instance
     * @param shortcut Shortcut String (use null value if menu mode is not supported)
     * @param menuPath action path in main menu (use null value if menu mode is not supported)
     * @param popupPath action path in popup menu (use null value if popup mode is not supported)
     * @param systemActionClass String class name of SystemAction (use null value if API mode is not supported)
     * @deprecated Use {@link ActionNoBlock#ActionNoBlock(String menuPath, String popupPath, String systemActionClass, KeyStroke keystroke)} instead.
     */
    public ActionNoBlock(String menuPath, String popupPath, String systemActionClass, Shortcut shortcut) {
        super(menuPath, popupPath, systemActionClass, shortcut);
    }
    
    /** Creates new ActionNoBlock instance without API_MODE support.
     * @param keystroke KeyStroke instance (use null value if shorcut mode is not supported)
     * @param menuPath action path in main menu (use null value if menu mode is not supported)
     * @param popupPath action path in popup menu (use null value if popup mode shell is not supported)
    */
    public ActionNoBlock(String menuPath, String popupPath, KeyStroke keystroke) {
        super(menuPath, popupPath, keystroke);
    }

    /** Creates new ActionNoBlock instance without API_MODE support.
     * @param keystrokes array of KeyStroke instances (use null value if shorcut mode is not supported)
     * @param menuPath action path in main menu (use null value if menu mode is not supported)
     * @param popupPath action path in popup menu (use null value if popup mode shell is not supported)
    */
    public ActionNoBlock(String menuPath, String popupPath, KeyStroke[] keystrokes) {
        super(menuPath, popupPath, keystrokes);
    }
    
    /** Creates new ActionNoBlock instance.
     * @param keystroke KeyStroke instance (use null value if shorcut mode is not supported)
     * @param menuPath action path in main menu (use null value if menu mode is not supported)
     * @param popupPath action path in popup menu (use null value if popup mode is not supported)
     * @param systemActionClass String class name of SystemAction (use null value if API mode is not supported) 
    */
    public ActionNoBlock(String menuPath, String popupPath, String systemActionClass, KeyStroke keystroke) {
        super(menuPath, popupPath, systemActionClass, new KeyStroke[] {keystroke});
    }
    
    /** Creates new ActionNoBlock instance.
     * @param keystrokes array of KeyStroke instances (use null value if shorcut mode is not supported)
     * @param menuPath action path in main menu (use null value if menu mode is not supported)
     * @param popupPath action path in popup menu (use null value if popup mode is not supported)
     * @param systemActionClass String class name of SystemAction (use null value if API mode is not supported) 
    */
    public ActionNoBlock(String menuPath, String popupPath, String systemActionClass, KeyStroke[] keystrokes) {
        super(menuPath, popupPath, systemActionClass, keystrokes);
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
        MainWindowOperator.getDefault().menuBar().pushMenuNoBlock(menuPath, "|");
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
        callPopup(nodes).pushMenuNoBlock(popupPath, "|");
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
    @SuppressWarnings("unchecked")
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
                            if(SystemAction.class.isAssignableFrom(systemActionClass)) {
                                // SystemAction used in IDE
                                SystemAction.get(systemActionClass).actionPerformed(
                                                        new ActionEvent(new Container(), 0, null));
                            } else {
                                // action implements javax.swing.Action
                                try {
                                    ((javax.swing.Action)systemActionClass.newInstance()).actionPerformed(null);
                                } catch (Exception e) {
                                    throw new JemmyException("Exception when trying to create instance of action \""+systemActionClass.getName()+"\".", e);
                                }
                            }
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
        final KeyStroke[] strokes = getKeyStrokes();
        if (strokes == null) {
            throw new UnsupportedOperationException(getClass().toString()+" does not define shortcut");
        }
        new Thread(new Runnable() {
            public void run() {
                for(int i=0; i<strokes.length; i++) {
                    new KeyRobotDriver(null).pushKey(null, strokes[i].getKeyCode(), strokes[i].getModifiers(), JemmyProperties.getCurrentTimeouts().create("ComponentOperator.PushKeyTimeout"));
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
