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

import java.awt.Component;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JEditorPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Registry;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsChangeListener;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.modules.editor.java.JavaKit;
import org.netbeans.modules.editor.options.BaseOptions;
import org.openide.cookies.EditorCookie;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author  Martin Roskanin
 */
public abstract class MainMenuAction extends SystemAction implements Presenter.Menu, ChangeListener{

    public static final Icon BLANK_ICON = new ImageIcon(org.openide.util.Utilities.loadImage("org/netbeans/modules/editor/resources/empty.gif"));
    
    
    /** Creates a new instance of ShowLineNumbersAction */
    public MainMenuAction() {
        Registry.addChangeListener(this);
    }

    
    
    public void stateChanged(ChangeEvent evt) {
        setMenu();
    }
    
    public void actionPerformed(java.awt.event.ActionEvent e) {
    }
    
    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;        
    }

    public String getName() {
        return "";
    }

    private static boolean isOpen(Document doc){
        if (doc==null) return false;
        DataObject dobj = NbEditorUtilities.getDataObject(doc);
        if (dobj==null) return false;
        EditorCookie ec = (EditorCookie)dobj.getCookie(EditorCookie.class);
        if (ec==null) return false;
        JEditorPane jep[] = ec.getOpenedPanes();
        return (jep!=null && jep.length>0);
    }

    private static JTextComponent getComponent(){
        return Utilities.getFocusedComponent();
    }

    
    private static Action getActionByName(String actionName){
        BaseKit bKit = getKit();
        if (bKit==null) bKit = BaseKit.getKit(NbEditorKit.class);
        if (bKit!=null){
            Action action = bKit.getActionByName(actionName);
            return action;
        }
        return null;
    }
    
    protected static void addAccelerators(Action a, JMenuItem item, JTextComponent target){
        if (target == null || a==null || item==null) return;
        // Try to get the accelerator
        Keymap km = target.getKeymap();
        if (km != null) {
            KeyStroke[] keys = km.getKeyStrokesForAction(a);
            if (keys != null && keys.length > 0) {
                item.setAccelerator(keys[0]);
            }
        }
    }
    
    
    private static BaseKit getKit(){
        JTextComponent component = getComponent();
        return (component == null) ? BaseKit.getKit(NbEditorKit.class) : Utilities.getKit(component);
    }

    protected static boolean isMainMenuActionEnabled(){
        JTextComponent component = Utilities.getFocusedComponent();
        if (component!=null){
            Document doc = component.getDocument();
            return isOpen(doc);
        }

        return false;
    }
    
    public boolean isEnabled() {
        return isMainMenuActionEnabled();
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

    protected abstract void setMenu();
    
    
    public static class ShowToolBarAction extends MainMenuAction{

        private static JCheckBoxMenuItem SHOW_TOOLBAR_MENU;// = new JCheckBoxMenuItem(getMenuItemText(), BLANK_ICON);
            
        public ShowToolBarAction(){
            super();
            SHOW_TOOLBAR_MENU = new CheckBoxItem(this, getMenuItemText(), BLANK_ICON);
            setMenu();
        }
        
        
        protected void setMenu(){
            Action action = getActionByName(ExtKit.toggleToolbarAction);
            SHOW_TOOLBAR_MENU.setAction(action);
            SHOW_TOOLBAR_MENU.setState(isToolbarVisible());
            SHOW_TOOLBAR_MENU.setText(getMenuItemText());
            SHOW_TOOLBAR_MENU.setIcon(BLANK_ICON);
            SHOW_TOOLBAR_MENU.setEnabled(ShowToolBarAction.isMainMenuActionEnabled());
            addAccelerators(action, SHOW_TOOLBAR_MENU, Utilities.getFocusedComponent());
        }
        
        public JMenuItem getMenuPresenter() {
            return SHOW_TOOLBAR_MENU;
        }

        private static boolean isToolbarVisible(){
            BaseKit kit = getKit();
            if (kit==null) return false;
            return getSettingBoolean(kit, BaseOptions.TOOLBAR_VISIBLE_PROP);
        }
        
        protected static String getMenuItemText(){
            return NbBundle.getBundle(MainMenuAction.class).getString(
                "show_editor_toolbar_main_menu_view_item"); //NOI18N
        }
        
    }
    
    
    // temporary solution
    private static class CheckBoxItem extends JCheckBoxMenuItem{
        private Action a;
        public CheckBoxItem(Action a, String text, Icon icon){
            super(text, icon);
            this.a = a;
        }
    }
    
    public static class ShowLineNumbersAction extends MainMenuAction{

        private static JCheckBoxMenuItem SHOW_LINE_MENU;
        
        public ShowLineNumbersAction(){
            super();
            SHOW_LINE_MENU  = new CheckBoxItem(this, getMenuItemText(), BLANK_ICON);        
            setMenu();
        }

        protected void setMenu(){
            Action action = getActionByName(BaseKit.toggleLineNumbersAction);
            SHOW_LINE_MENU.setAction(action);
            SHOW_LINE_MENU.setState(isLineNumbersVisible());
            SHOW_LINE_MENU.setText(getMenuItemText());
            SHOW_LINE_MENU.setIcon(BLANK_ICON);
            SHOW_LINE_MENU.setEnabled(ShowLineNumbersAction.isMainMenuActionEnabled());
            addAccelerators(action, SHOW_LINE_MENU, Utilities.getFocusedComponent());
        }
        
        protected static String getMenuItemText(){
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
        
    }
    
    
    private static class MyMenuItem extends JMenuItem{
        Action a;
        public MyMenuItem(Action a, String text, Icon icon){
            super(text, icon);
            this.a = a;
        }
    }
    
    public static class GoToSourceAction extends MainMenuAction{
        
        private JMenuItem GOTO_SOURCE_MENU;// = new MyMenuItem(getMenuItemText(), BLANK_ICON);        

        public GoToSourceAction(){
            super();
            GOTO_SOURCE_MENU = new MyMenuItem(this, getMenuItemText(), BLANK_ICON);        
            setMenu();
        }
        
        private static String getMenuItemText(){
            return NbBundle.getBundle(MainMenuAction.class).getString(
                "goto_source_main_menu_edit_item"); //NOI18N
        }
        
        public JMenuItem getMenuPresenter() {
            return GOTO_SOURCE_MENU;
        }

        protected void setMenu() {

            BaseKit kit = getKit();
            String txt = getMenuItemText();
            if (MainMenuAction.isMainMenuActionEnabled() || kit == null){
                GOTO_SOURCE_MENU.setEnabled(false);                    
                GOTO_SOURCE_MENU.setText(txt);
                GOTO_SOURCE_MENU.setIcon(BLANK_ICON);
            }

            Action action = getActionByName(ExtKit.gotoSourceAction);
            if (action instanceof BaseAction && kit instanceof JavaKit){
                GOTO_SOURCE_MENU.setEnabled(true);
                GOTO_SOURCE_MENU.setAction(action);
                GOTO_SOURCE_MENU.setText(txt);
                GOTO_SOURCE_MENU.setIcon(BLANK_ICON);
                addAccelerators(action, GOTO_SOURCE_MENU, Utilities.getFocusedComponent());
            }else{
                GOTO_SOURCE_MENU.setEnabled(false);
                GOTO_SOURCE_MENU.setText(txt);
                GOTO_SOURCE_MENU.setIcon(BLANK_ICON);
            }            
        }
        
    }

    
    public static class GoToSuperAction extends MainMenuAction{
        
        private  JMenuItem GOTO_SUPER_MENU;// = new JMenuItem(getMenuItemText(), BLANK_ICON);        

        public GoToSuperAction(){
            super();
            GOTO_SUPER_MENU = new MyMenuItem(this, getMenuItemText(), BLANK_ICON);        
            setMenu();
        }
        
        private String getMenuItemText(){
            return NbBundle.getBundle(MainMenuAction.class).getString(
                "goto_super_implementation_main_menu_edit_item"); //NOI18N
        }
        
        public JMenuItem getMenuPresenter() {
            return GOTO_SUPER_MENU;
        }

        protected void setMenu() {
            BaseKit kit = getKit();
            String txt = getMenuItemText();
            if (MainMenuAction.isMainMenuActionEnabled() || kit == null){
                GOTO_SUPER_MENU.setEnabled(false);                    
                GOTO_SUPER_MENU.setText(txt);
                GOTO_SUPER_MENU.setIcon(BLANK_ICON);
            }

            Action action = getActionByName(JavaKit.gotoSuperImplementationAction);
            if (action instanceof BaseAction && kit instanceof JavaKit){
                GOTO_SUPER_MENU.setEnabled(true);
                GOTO_SUPER_MENU.setAction(action);
                GOTO_SUPER_MENU.setText(txt);
                GOTO_SUPER_MENU.setIcon(BLANK_ICON);
                addAccelerators(action, GOTO_SUPER_MENU, Utilities.getFocusedComponent());                    
            }else{
                GOTO_SUPER_MENU.setEnabled(false);
                GOTO_SUPER_MENU.setText(txt);
                GOTO_SUPER_MENU.setIcon(BLANK_ICON);
            }            
        }
    }

    public static class GoToDeclarationAction extends MainMenuAction{
        
        private  JMenuItem GOTO_DECL_MENU;// = new JMenuItem(getMenuItemText(), BLANK_ICON);        

        public GoToDeclarationAction(){
            super();
            GOTO_DECL_MENU = new MyMenuItem(this, getMenuItemText(), BLANK_ICON);        
            setMenu();
        }
        
        private String getMenuItemText(){
            return NbBundle.getBundle(MainMenuAction.class).getString(
                "goto_declaration_main_menu_edit_item"); //NOI18N
        }
        
        public JMenuItem getMenuPresenter() {
            return GOTO_DECL_MENU;
        }

        protected void setMenu() {
            BaseKit kit = getKit();
            String txt = getMenuItemText();
            if (MainMenuAction.isMainMenuActionEnabled() || kit == null){
                GOTO_DECL_MENU.setEnabled(false);                    
                GOTO_DECL_MENU.setText(txt);
                GOTO_DECL_MENU.setIcon(BLANK_ICON);
            }

            Action action = getActionByName(ExtKit.gotoDeclarationAction);
            if (action instanceof BaseAction && kit instanceof JavaKit){
                GOTO_DECL_MENU.setEnabled(true);
                GOTO_DECL_MENU.setAction(action);
                GOTO_DECL_MENU.setText(txt);
                GOTO_DECL_MENU.setIcon(BLANK_ICON);
                addAccelerators(action, GOTO_DECL_MENU, Utilities.getFocusedComponent());                    
            }else{
                GOTO_DECL_MENU.setEnabled(false);
                GOTO_DECL_MENU.setText(txt);
                GOTO_DECL_MENU.setIcon(BLANK_ICON);
            }            
        }
        
        
    }
    
}
    