/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.jellytools.actions;

import java.lang.IllegalArgumentException;
import java.awt.Component;
import java.awt.event.KeyEvent;
import javax.swing.tree.TreePath;

import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.drivers.input.KeyRobotDriver;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.Operator;

import org.openide.util.actions.SystemAction;


/** Ancestor class for all blocking actions.<p>
 * It handles performing action through main menu (MENU_MODE), popup menu
 * (POPUP_MODE), IDE SystemAction API call (API_MODE) or through keyboard shortcut
 * (SHORTCUT_MODE).<p>
 * Action can be performed in exact mode by calling performMenu(...),
 * performPopup(...), performAPI(...) or performShortcut(...).<p>
 * If exact mode is not supported by the action it throws
 * UnsupportedOperationException.<p>
 * Current implementation supports MENU_MODE when menuPath is defined, POPUP_MODE
 * when popupPath is defined, API_MODE when systemActionClass is defined and
 * SHORTCUT_MODE when shorcut is defined (see Action constructors).<p>
 * Action also can be performed using runtime default mode by calling perform(...).<p>
 * When default mode is not support by the action other modes are tried till
 * supported mode found and action is performed.
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a> */
public class Action {
    
    /** through menu action performing mode */    
    public static final int MENU_MODE = 0;
    /** through popup menu action performing mode */    
    public static final int POPUP_MODE = 1;
    /** through API action performing mode */    
    public static final int API_MODE = 2;
    /** through shortcut action performing mode */    
    public static final int SHORTCUT_MODE = 3;
    
    /** sleep time between nodes selection and action execution */    
    protected static final long SELECTION_WAIT_TIME = 300;
    /** sleep time after action execution */    
    protected static final long AFTER_ACTION_WAIT_TIME = 0;
    
    private static final int sequence[][] = {{MENU_MODE, POPUP_MODE, SHORTCUT_MODE, API_MODE}, 
                                             {POPUP_MODE, MENU_MODE, SHORTCUT_MODE, API_MODE}, 
                                             {API_MODE, POPUP_MODE, MENU_MODE, SHORTCUT_MODE},
                                             {SHORTCUT_MODE, POPUP_MODE, MENU_MODE, API_MODE}};

                                             /** menu path of current action or null when MENU_MODE is not supported */                                             
    protected String menuPath;
    /** popup menu path of current action or null when POPUP_MODE is not supported */    
    protected String popupPath;
    /** SystemAction class of current action or null when API_MODE is not supported */    
    protected Class systemActionClass;
    /** shortcut of current action or null when SHORTCUT_MODE is not supported */    
    protected Shortcut shortcut;
    
    /** creates new Action instance without API_MODE and SHORTCUT_MODE support
     * @param menuPath action path in main menu (use null value if menu mode is not supported)
     * @param popupPath action path in popup menu (use null value if popup mode shell is not supported) */    
    public Action(String menuPath, String popupPath) {
        this(menuPath, popupPath, null, null);
    }
    
    /** creates new Action instance without SHORTCUT_MODE support
     * @param menuPath action path in main menu (use null value if menu mode is not supported)
     * @param popupPath action path in popup menu (use null value if popup mode is not supported)
     * @param systemActionClass String class name of SystemAction (use null value if API mode is not supported) */    
    public Action(String menuPath, String popupPath, String systemActionClass) {
        this(menuPath, popupPath, systemActionClass, null);
    }
    
    /** creates new Action instance without API_MODE support
     * @param shortcut Shortcut (use null value if menu mode is not supported)
     * @param menuPath action path in main menu (use null value if menu mode is not supported)
     * @param popupPath action path in popup menu (use null value if popup mode shell is not supported) */    
    public Action(String menuPath, String popupPath, Shortcut shortcut) {
        this(menuPath, popupPath, null, shortcut);
    }
    
    /** creates new Action instance
     * @param shortcut Shortcut String (use null value if menu mode is not supported)
     * @param menuPath action path in main menu (use null value if menu mode is not supported)
     * @param popupPath action path in popup menu (use null value if popup mode is not supported)
     * @param systemActionClass String class name of SystemAction (use null value if API mode is not supported) */    
    public Action(String menuPath, String popupPath, String systemActionClass, Shortcut shortcut) {
        this.menuPath = menuPath;
        this.popupPath = popupPath;
        if (systemActionClass==null) {
            this.systemActionClass = null;
        } else try {
            this.systemActionClass = Class.forName(systemActionClass);
        } catch (ClassNotFoundException e) {
            this.systemActionClass = null;
        }            
        this.shortcut = shortcut;
    }
    
    static {
        if (JemmyProperties.getCurrentProperty("Action.DefaultMode")==null)
            JemmyProperties.setCurrentProperty("Action.DefaultMode", new Integer(POPUP_MODE));
    }
    
    private void perform(int mode) {
        switch (mode) {
            case POPUP_MODE: performPopup(); break;
            case MENU_MODE: performMenu(); break;
            case API_MODE: performAPI(); break;
            case SHORTCUT_MODE: performShortcut(); break;
            default: throw new IllegalArgumentException("Wrong Action.MODE");
        }
    }
            
    private void perform(Node node, int mode) {
        switch (mode) {
            case POPUP_MODE: performPopup(node); break;
            case MENU_MODE: performMenu(node); break;
            case API_MODE: performAPI(node); break;
            case SHORTCUT_MODE: performShortcut(node); break;
            default: throw new IllegalArgumentException("Wrong Action.MODE");
        }
    }
            
    private void perform(Node[] nodes, int mode) {
        switch (mode) {
            case POPUP_MODE: performPopup(nodes); break;
            case MENU_MODE: performMenu(nodes); break;
            case API_MODE: performAPI(nodes); break;
            case SHORTCUT_MODE: performShortcut(nodes); break;
            default: throw new IllegalArgumentException("Wrong Action.MODE");
        }
    }
            
    private void perform(ComponentOperator component, int mode) {
        switch (mode) {
            case POPUP_MODE: performPopup(component); break;
            case MENU_MODE: performMenu(component); break;
            case API_MODE: performAPI(component); break;
            case SHORTCUT_MODE: performShortcut(component); break;
            default: throw new IllegalArgumentException("Wrong Action.MODE");
        }
    }
            
    
    /** performs action depending on default mode,<br>
     * calls performPopup(), performMenu() or performAPI(),<br>
     * when default mode is not supported, others are tried */    
    public void perform() {
        int modes[] = sequence[getDefaultMode()];
        for (int i=0; i<modes.length; i++) try {
            perform(modes[i]);
            return;
        } catch (UnsupportedOperationException e) {}
    }
    
    /** performs action depending on default mode,<br>
     * calls performPopup(), performMenu() or performAPI(),<br>
     * when default mode is not supported, others are tried
     * @param node node to be action performed on */    
    public void perform(Node node) {
        int modes[] = sequence[getDefaultMode()];
        for (int i=0; i<modes.length; i++) try {
            perform(node, modes[i]);
            return;
        } catch (UnsupportedOperationException e) {}
    }
    
    /** performs action depending on default mode,<br>
     * calls performPopup(), performMenu() or performAPI(),<br>
     * when default mode is not supported, others are tried
     * @param nodes nodes to be action performed on */    
    public void perform(Node[] nodes) {
        int modes[] = sequence[getDefaultMode()];
        for (int i=0; i<modes.length; i++) try {
            perform(nodes, modes[i]);
            return;
        } catch (UnsupportedOperationException e) {}
    }

    /** performs action depending on default mode,<br>
     * calls performPopup(), performMenu() or performAPI(),<br>
     * when default mode is not supported, others are tried
     * @param component component to be action performed on */    
    public void perform(ComponentOperator component) {
        int modes[] = sequence[getDefaultMode()];
        for (int i=0; i<modes.length; i++) try {
            perform(component, modes[i]);
            return;
        } catch (UnsupportedOperationException e) {}
    }
    
    /** performs action through main menu
     * @throws UnsupportedOperationException when action does not support menu mode */    
    public void performMenu() {
        if (menuPath==null)
            throw new UnsupportedOperationException(getClass().toString()+" does not define menu path");
        MainWindowOperator.getDefault().menuBar().pushMenu(menuPath, "|");
        try {
            Thread.sleep(AFTER_ACTION_WAIT_TIME);
        } catch (Exception e) {
            throw new JemmyException("Sleeping interrupted", e);
        }
    }    

    /** performs action through main menu
     * @param node node to be action performed on 
     * @throws UnsupportedOperationException when action does not support menu mode */    
    public void performMenu(Node node) {
        performMenu(new Node[]{node});
    }
    
    /** performs action through main menu
     * @param nodes nodes to be action performed on
     * @throws UnsupportedOperationException when action does not support menu mode */    
    public void performMenu(Node[] nodes) {
        if (menuPath==null)
            throw new UnsupportedOperationException(getClass().toString()+" does not define menu path");
        testNodes(nodes);
        nodes[0].select();
        for (int i=1; i<nodes.length; i++)
            nodes[i].addSelectionPath();
        try {
            Thread.sleep(SELECTION_WAIT_TIME);
        } catch (Exception e) {
            throw new JemmyException("Sleeping interrupted", e);
        }
        performMenu();
    }
    
    /** performs action through main menu
     * @param component component to be action performed on
     * @throws UnsupportedOperationException when action does not support menu mode */    
    public void performMenu(ComponentOperator component) {
        component.getFocus();
        try {
            Thread.sleep(SELECTION_WAIT_TIME);
        } catch (Exception e) {
            throw new JemmyException("Sleeping interrupted", e);
        }
        performMenu();
    }
    
    /** performs action through popup menu
     * @throws UnsupportedOperationException */    
    public void performPopup() {
        throw new UnsupportedOperationException(getClass().toString()+" does not implement performPopup()");
    }

    /** performs action through popup menu
     * @param node node to be action performed on 
     * @throws UnsupportedOperationException when action does not support popup mode */    
    public void performPopup(Node node) {
        performPopup(new Node[]{node});
    }
    
    /** performs action through popup menu
     * @param nodes nodes to be action performed on  
     * @throws UnsupportedOperationException when action does not support popup mode */    
    public void performPopup(Node[] nodes) {
        if (popupPath==null)
            throw new UnsupportedOperationException(getClass().toString()+" does not define popup path");
        testNodes(nodes);
        TreePath paths[]=new TreePath[nodes.length];
        for (int i=0; i<nodes.length; i++)
            paths[i]=nodes[i].getTreePath();
        nodes[0].tree().selectPaths(paths);
        try {
            Thread.sleep(SELECTION_WAIT_TIME);
        } catch (Exception e) {
            throw new JemmyException("Sleeping interrupted", e);
        }
        JPopupMenuOperator popup=new JPopupMenuOperator(nodes[0].tree().callPopupOnPaths(paths));
        popup.setComparator(new Operator.DefaultStringComparator(false, true));
        popup.pushMenu(popupPath, "|");
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
        component.clickForPopup();
        JPopupMenuOperator popup=new JPopupMenuOperator(component);
        popup.setComparator(new Operator.DefaultStringComparator(false, true));
        popup.pushMenu(popupPath, "|");
        try {
            Thread.sleep(AFTER_ACTION_WAIT_TIME);
        } catch (Exception e) {
            throw new JemmyException("Sleeping interrupted", e);
        }
    }
    
    /** performs action through API  
     * @throws UnsupportedOperationException when action does not support API mode */    
    public void performAPI() {
        if (systemActionClass==null)
            throw new UnsupportedOperationException(getClass().toString()+" does not define SystemAction");
        SystemAction.get(systemActionClass).actionPerformed(null);    
        try {
            Thread.sleep(AFTER_ACTION_WAIT_TIME);
        } catch (Exception e) {
            throw new JemmyException("Sleeping interrupted", e);
        }
    }
    
    /** performs action through API
     * @param node node to be action performed on 
     * @throws UnsupportedOperationException when action does not support API mode */    
    public void performAPI(Node node) {
        performAPI(new Node[]{node});
    }
    
    /** performs action through API
     * @param nodes nodes to be action performed on 
     * @throws UnsupportedOperationException when action does not support API mode */    
    public void performAPI(Node[] nodes) {
        if (systemActionClass==null)
            throw new UnsupportedOperationException(getClass().toString()+" does not define SystemAction");
        testNodes(nodes);
        nodes[0].select();
        for (int i=1; i<nodes.length; i++)
            nodes[i].addSelectionPath();
        try {
            Thread.sleep(SELECTION_WAIT_TIME);
        } catch (Exception e) {
            throw new JemmyException("Sleeping interrupted", e);
        }
        performAPI();
    }
    
    /** performs action through API
     * @param component component to be action performed on
     * @throws UnsupportedOperationException when action does not support API mode */    
    public void performAPI(ComponentOperator component) {
        component.getFocus();
        try {
            Thread.sleep(SELECTION_WAIT_TIME);
        } catch (Exception e) {
            throw new JemmyException("Sleeping interrupted", e);
        }
        performAPI();
    }
    
    /** performs action through shortcut  
     * @throws UnsupportedOperationException */    
    public void performShortcut() {
        if (shortcut==null)
            throw new UnsupportedOperationException(getClass().toString()+" does not define shortcut");
        new KeyRobotDriver(null).pushKey(null, shortcut.getKeyCode(), shortcut.getKeyModifiers(), JemmyProperties.getCurrentTimeouts().create("Timeouts.DeltaTimeout"));
        try {
            Thread.sleep(AFTER_ACTION_WAIT_TIME);
        } catch (Exception e) {
            throw new JemmyException("Sleeping interrupted", e);
        }
    }
    
    /** performs action through shortcut
     * @param node node to be action performed on 
     * @throws UnsupportedOperationException when action does not support shortcut mode */    
    public void performShortcut(Node node) {
        performShortcut(new Node[]{node});
    }
    
    /** performs action through shortcut
     * @param nodes nodes to be action performed on 
     * @throws UnsupportedOperationException when action does not support shortcut mode */    
    public void performShortcut(Node[] nodes) {
        if (shortcut==null)
            throw new UnsupportedOperationException(getClass().toString()+" does not define shortcut");
        testNodes(nodes);
        nodes[0].select();
        for (int i=1; i<nodes.length; i++)
            nodes[i].addSelectionPath();
        try {
            Thread.sleep(SELECTION_WAIT_TIME);
        } catch (Exception e) {
            throw new JemmyException("Sleeping interrupted", e);
        }
        performShortcut();
    }
    
    /** performs action through shortcut
     * @param component component to be action performed on
     * @throws UnsupportedOperationException when action does not support shortcut mode */    
    public void performShortcut(ComponentOperator component) {
        component.getFocus();
        try {
            Thread.sleep(SELECTION_WAIT_TIME);
        } catch (Exception e) {
            throw new JemmyException("Sleeping interrupted", e);
        }
        performShortcut();
    }
    
    /** tests if nodes are all from the same tree
     * @param nodes nodes
     * @throws IllegalArgumentException when given nodes does not pass */    
    protected void testNodes(Node[] nodes) {
        if ((nodes==null)||(nodes.length==0))
            throw new IllegalArgumentException("argument nodes is null or empty");
        Class myClass = getClass();
        Component nodesTree=nodes[0].tree().getSource();
        for (int i=0; i<nodes.length; i++) {
//            if (!nodes[i].hasAction(myClass))
//                throw new IllegalArgumentException(this.toString()+" could not be performed on "+nodes[i].toString());
            if (nodes[i]==null)
                throw new IllegalArgumentException("argument nodes contains null value");
            if (!nodesTree.equals(nodes[i].tree().getSource()))
                throw new IllegalArgumentException(nodes[i].toString()+" is from different tree");
        }
    }
    
    private int getDefaultMode() {
        int mode=(((Integer)JemmyProperties.getCurrentProperty("Action.DefaultMode")).intValue());
        if (mode<0 || mode>3)
            return POPUP_MODE;
        return mode;
    }
    
    /** This class defines keyboard shortcut for action execution */    
    public static class Shortcut extends Object {
        /** key code of shortcut (see KeyEvent) */        
        protected int keyCode;
        /** key modifiers of shortcut (see KeyEvent) */        
        protected int keyModifiers;
        
        /** creates new shortcut
         * @param keyCode int key code (see KeyEvent) */        
        public Shortcut(int keyCode) {
            this(keyCode, 0);
        }

        /** creates new shortcut
         * @param keyCode int key code (see KeyEvent)
         * @param keyModifiers int key modifiers (see KeyEvent) */        
        public Shortcut(int keyCode, int keyModifiers) {
            this.keyCode=keyCode;
            this.keyModifiers=keyModifiers;
        }
        
        /** getter for key code
         * @return int key code */        
        public int getKeyCode() {
            return keyCode;
        }
        
        /** getter for key modifiers
         * @return int key modifiers */        
        public int getKeyModifiers() {
            return keyModifiers;
        }
            
        /** returns String representation of shortcut
         * @return String representation of shortcut */        
        public String toString() {
            return KeyEvent.getKeyModifiersText(getKeyModifiers())+"-"+KeyEvent.getKeyText(getKeyCode());
        }
    }
}
