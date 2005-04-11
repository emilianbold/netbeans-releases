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

package org.netbeans.modules.editor;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Registry;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.modules.editor.options.BaseOptions;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

/**
 * Main menu action, like Edit/Go to Source, Edit/Go to Line..., 
 * View/Show Editor Toolbar, View/Show Line Numbers
 * This is the action implements Presenter.Menu and delegates on specific actions like 
 * ExtKit.toggleToolbarAction or ExtKit.gotoSuperImplementationAction
 *
 * @author  Martin Roskanin
 */
public abstract class MainMenuAction extends GlobalContextAction implements Presenter.Menu, ChangeListener {

    public static final Icon BLANK_ICON = new ImageIcon(org.openide.util.Utilities.loadImage("org/netbeans/modules/editor/resources/empty.gif"));
    public boolean menuInitialized = false;
    
    /** Creates a new instance of ShowLineNumbersAction */
    public MainMenuAction() {
        // needs to listen on Registry - resultChanged event is fired before 
        // TopComponent is really focused - this causes problems in getComponent method 
        Registry.addChangeListener(this);
    }
    
    public void resultChanged(org.openide.util.LookupEvent ev){
        setMenu();
    }
    
    public void stateChanged(ChangeEvent e)    {
        setMenu();
    }
    
    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;        
    }

    public String getName() {
        return getMenuItemText();
    }

    /** Returns focused editor component */
    private static JTextComponent getComponent(){
        return Utilities.getFocusedComponent();
    }

    /** Returns the action by given name */
    private static Action getActionByName(String actionName){
        BaseKit bKit = getKit();
        if (bKit!=null){
            Action action = bKit.getActionByName(actionName);
            return action;
        }
        return null;
    }
    
    /** Adds accelerators to given JMenuItem taken from the action */
    protected static void addAccelerators(Action a, JMenuItem item, JTextComponent target){
        if (target == null || a==null || item==null) return;
        
        // get accelerators from kitAction
        Action kitAction = getActionByName((String)a.getValue(Action.NAME));
        if (kitAction!=null) a = kitAction;
        // Try to get the accelerator, TopComponent action could be obsoleted
        Keymap km = target.getKeymap();

        if (km != null) {
            KeyStroke[] keys = km.getKeyStrokesForAction(a);
            KeyStroke itemAccelerator = item.getAccelerator();
            
            if (keys != null && keys.length > 0) {
                if (itemAccelerator==null || !itemAccelerator.equals(keys[0])){
                    item.setAccelerator(keys[0]);
                }
            }else{
                if (itemAccelerator!=null && kitAction!=null){
                    item.setAccelerator(null);
                }
            }
        }
    }
    
    /** Gets the editor kit */
    private static BaseKit getKit(){
        JTextComponent component = getComponent();
        return (component == null) ? null : Utilities.getKit(component);
    }
    
    public boolean isEnabled() {
        return false;
    }
    
    private static Object getSettingValue(BaseKit kit, String settingName) {
        return Settings.getValue(kit.getClass(), settingName);
    }

    /** Get the value of the boolean setting from the <code>Settings</code>
     * @param settingName name of the setting to get.
     */
    private static boolean getSettingBoolean(BaseKit kit, String settingName) {
        Boolean val = (Boolean)getSettingValue(kit, settingName);
        return (val != null) ? val.booleanValue() : false;
    }

    /** Sets the state of JMenuItem*/
    protected void setMenu(){
        ActionMap am = getContextActionMap();
        Action action = null;
        JMenuItem presenter = getMenuPresenter();

        if (am!=null){
            action = am.get(getActionName());
            Action presenterAction = presenter.getAction();
            if (presenterAction == null){
                if (action != null){
                    presenter.setAction(action);
                    menuInitialized = false;
                }
            }else{
                if ((action!=null && !action.equals(presenterAction))){
                    presenter.setAction(action);
                    menuInitialized = false;
                }else if (action == null){
                    presenter.setEnabled(false);
                }
            }
        }

        if (!menuInitialized){
            Mnemonics.setLocalizedText(presenter, getMenuItemText());
            menuInitialized = true;
        }
        
        presenter.setEnabled(action != null);
        JTextComponent comp = Utilities.getFocusedComponent();
        if (comp != null){
            addAccelerators(action, presenter, comp);
        } else {
            presenter.setAccelerator(getDefaultAccelerator());
        }
    }
    
    /** Get the text of the menu item */
    protected abstract String getMenuItemText();
    
    /** Get the action name */
    protected abstract String getActionName();
    
    /** Get default accelerator */
    protected KeyStroke getDefaultAccelerator(){
        return null;
    }
    
    
    public static class ShowToolBarAction extends MainMenuAction{

        private static JCheckBoxMenuItem SHOW_TOOLBAR_MENU;
            
        public ShowToolBarAction(){
            super();
            SHOW_TOOLBAR_MENU = new JCheckBoxMenuItem(getMenuItemText());
            setMenu();
        }
        
        protected void setMenu(){
            super.setMenu();
            SHOW_TOOLBAR_MENU.setState(isToolbarVisible());
        }
        
        public JMenuItem getMenuPresenter() {
            return SHOW_TOOLBAR_MENU;
        }

        private static boolean isToolbarVisible(){
            BaseKit kit = getKit();
            if (kit==null) return false;
            return getSettingBoolean(kit, BaseOptions.TOOLBAR_VISIBLE_PROP);
        }
        
        protected String getMenuItemText(){
            return NbBundle.getBundle(MainMenuAction.class).getString(
                "show_editor_toolbar_main_menu_view_item"); //NOI18N
        }
        
        protected String getActionName() {
            return ExtKit.toggleToolbarAction;
        }        
        
    }
    
    
    public static class ShowLineNumbersAction extends MainMenuAction{

        private JCheckBoxMenuItem SHOW_LINE_MENU;
        
        public ShowLineNumbersAction(){
            super();
            SHOW_LINE_MENU  = new JCheckBoxMenuItem(getMenuItemText());
            setMenu();
        }
        
        protected void setMenu(){
            super.setMenu();
            SHOW_LINE_MENU.setState(isLineNumbersVisible());
        }
        
        protected String getMenuItemText(){
            return NbBundle.getBundle(MainMenuAction.class).getString(
                "show_line_numbers_main_menu_view_item"); //NOI18N
        }
        
        public String getName() {
            return getMenuItemText();
        }   
        
        public javax.swing.JMenuItem getMenuPresenter() {
            return SHOW_LINE_MENU;
        }
        
        private static boolean isLineNumbersVisible(){
            BaseKit kit = getKit();
            if (kit==null) return false;
            return getSettingBoolean(kit, SettingsNames.LINE_NUMBER_VISIBLE);
        }
        
        protected String getActionName() {
            return ExtKit.toggleLineNumbersAction;
        }
        
    }
    
    
    public static class GoToSourceAction extends MainMenuAction{
        
        private JMenuItem GOTO_SOURCE_MENU;

        public GoToSourceAction(){
            super();
            GOTO_SOURCE_MENU = new JMenuItem(getMenuItemText());
            setMenu();
        }
        
        /** Overriden to keep BLANK_ICON, which is reseted by superclass
         * setMenu impl
         */
        protected void setMenu () {
            super.setMenu();
            if (!BLANK_ICON.equals(GOTO_SOURCE_MENU.getIcon())) {
                GOTO_SOURCE_MENU.setIcon(BLANK_ICON);
            }
        }
        
        protected String getMenuItemText(){
            return NbBundle.getBundle(MainMenuAction.class).getString(
                "goto_source_main_menu_edit_item"); //NOI18N
        }
        
        public JMenuItem getMenuPresenter() {
            return GOTO_SOURCE_MENU;
        }

        protected String getActionName() {
            return ExtKit.gotoSourceAction;
        }
        
        protected KeyStroke getDefaultAccelerator(){
            return KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.ALT_MASK);
        }
        
    }

    
    public static class GoToSuperAction extends MainMenuAction{
        
        private  JMenuItem GOTO_SUPER_MENU;// = new JMenuItem(getMenuItemText(), BLANK_ICON);        

        public GoToSuperAction(){
            super();
            GOTO_SUPER_MENU = new JMenuItem(getMenuItemText());
            setMenu();
        }
        
        /** Overriden to keep BLANK_ICON, which is reseted by superclass
         * setMenu impl
         */
        protected void setMenu () {
            super.setMenu();
            if (!BLANK_ICON.equals(GOTO_SUPER_MENU.getIcon())) {
                GOTO_SUPER_MENU.setIcon(BLANK_ICON);
            }
        }
        
        protected String getMenuItemText(){
            return NbBundle.getBundle(MainMenuAction.class).getString(
                "goto_super_implementation_main_menu_edit_item"); //NOI18N
        }
        
        public JMenuItem getMenuPresenter() {
            return GOTO_SUPER_MENU;
        }

        protected String getActionName() {
            return ExtKit.gotoSuperImplementationAction;
        }
        
        protected KeyStroke getDefaultAccelerator(){
            return KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_MASK);
        }
        
    }

    public static class GoToDeclarationAction extends MainMenuAction{
        
        private  JMenuItem GOTO_DECL_MENU;

        public GoToDeclarationAction(){
            super();
            GOTO_DECL_MENU = new JMenuItem(getMenuItemText());
            setMenu();
        }
        
        /** Overriden to keep BLANK_ICON, which is reseted by superclass
         * setMenu impl
         */
        protected void setMenu () {
            super.setMenu();
            if (!BLANK_ICON.equals(GOTO_DECL_MENU.getIcon())) {
                GOTO_DECL_MENU.setIcon(BLANK_ICON);
            }
        }
        
        protected String getMenuItemText(){
            return NbBundle.getBundle(MainMenuAction.class).getString(
                "goto_declaration_main_menu_edit_item"); //NOI18N
        }

        public JMenuItem getMenuPresenter() {
            return GOTO_DECL_MENU;
        }

        protected String getActionName() {
            return ExtKit.gotoDeclarationAction;
        }
        
        protected KeyStroke getDefaultAccelerator(){
            return KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.ALT_MASK);
        }
        
        
    }
    
}
    