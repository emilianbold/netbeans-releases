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

package org.netbeans.modules.editor;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JEditorPane;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.KeyBindingSettings;
import org.netbeans.api.editor.settings.MultiKeyBinding;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Registry;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.modules.editor.options.AllOptionsFolder;
import org.netbeans.modules.editor.options.BaseOptions;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
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
    /** icon of the action, null means no icon */
    private final Icon forcedIcon;
    /** true when icon of original action should be ignored */
    private boolean forceIcon;
    
    /** Creates a new instance of ShowLineNumbersAction */
    public MainMenuAction() {
        // force no icon
        this(true, null);
    }
    
    public MainMenuAction (boolean forceIcon, Icon forcedIcon) {
        // needs to listen on Registry - resultChanged event is fired before 
        // TopComponent is really focused - this causes problems in getComponent method 
        Registry.addChangeListener(this);
        this.forceIcon = forceIcon;
        this.forcedIcon = forcedIcon;
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

    /** If there is no kit sensitive action, some global kit action can be returned
     * by subclasses. Returning null by default */
    protected Action getGlobalKitAction(){
        return null;
    }
    
    
    /** Sets the state of JMenuItem*/
    protected void setMenu(){
        ActionMap am = getContextActionMap();
        Action action = null;
        if (am != null) {
            action = am.get(getActionName());
        }

        if (action == null){
            action = getGlobalKitAction();
        }
        
        JMenuItem presenter = getMenuPresenter();
        assert presenter != null : "Got null return from getMenuPresenter in " + this;
        Action presenterAction = presenter.getAction();
        if (presenterAction == null){
            if (action != null){
                presenter.setAction(action);
                presenter.setToolTipText(null); /* bugfix #62872 */ 
                menuInitialized = false;
            }
        }else{
            if ((action!=null && !action.equals(presenterAction))){
                presenter.setAction(action);
                presenter.setToolTipText(null); /* bugfix #62872 */
                menuInitialized = false;
            }
        }
        
        if (!menuInitialized){
            Mnemonics.setLocalizedText(presenter, getMenuItemText());
            menuInitialized = true;
        }
        
        presenter.setEnabled(action != null);
        JTextComponent comp = Utilities.getFocusedComponent();
        if (comp != null && comp instanceof JEditorPane){
            addAccelerators(action, presenter, comp);
        } else {
            presenter.setAccelerator(getDefaultAccelerator());
        }
        
        if (forceIcon) {
            presenter.setIcon(forcedIcon);
        }
    }
    
    /** Get the text of the menu item */
    protected abstract String getMenuItemText();
    
    /** Get the action name */
    protected abstract String getActionName();
    
    /** Get default accelerator */
    protected KeyStroke getDefaultAccelerator(){
        Lookup ml = MimeLookup.getLookup(MimePath.get("text/x-java")); //NOI18N
        KeyBindingSettings kbs = (KeyBindingSettings) ml.lookup(KeyBindingSettings.class);
        if (kbs != null){
            List lst = kbs.getKeyBindings();
            if (lst != null){
                for (int i=0; i<lst.size(); i++){
                    MultiKeyBinding mkb = (MultiKeyBinding)lst.get(i);
                    String an = mkb.getActionName();
                    if (an != null && an.equals(getActionName())){
                        if (mkb.getKeyStrokeCount() == 1){// we do not support multi KB in mnemonics
                            return mkb.getKeyStroke(0);
                        }
                    }
                }
            }
        }
        return null;
    }
    
    public static class ShowToolBarAction extends MainMenuAction{

        private static JCheckBoxMenuItem SHOW_TOOLBAR_MENU;
        private Action delegate = null;
        
        public ShowToolBarAction(){
            super(false, null);
            SHOW_TOOLBAR_MENU = new JCheckBoxMenuItem(getMenuItemText());
            setMenu();
        }

        protected void setMenu(){
            super.setMenu();
            boolean visible = AllOptionsFolder.getDefault().isToolbarVisible();
            SHOW_TOOLBAR_MENU.setState(visible);
        }
        
        public JMenuItem getMenuPresenter() {
            return SHOW_TOOLBAR_MENU;
        }

        protected String getMenuItemText(){
            return NbBundle.getBundle(MainMenuAction.class).getString(
                "show_editor_toolbar_main_menu_view_item"); //NOI18N
        }
        
        protected String getActionName() {
            return ExtKit.toggleToolbarAction;
        }        
        
        protected Action getGlobalKitAction() {
            if (delegate == null) {
                delegate = new NbEditorKit.ToggleToolbarAction();
            }
            return delegate;
        }
    }
    
    
    public static class ShowLineNumbersAction extends MainMenuAction{

        private JCheckBoxMenuItem SHOW_LINE_MENU;
        private Action delegate = null;
        
        public ShowLineNumbersAction(){
            super(false, null);
            SHOW_LINE_MENU  = new JCheckBoxMenuItem(getMenuItemText());
            setMenu();
        }
        
        protected void setMenu(){
            super.setMenu();
            boolean visible = AllOptionsFolder.getDefault().getLineNumberVisible();
            SHOW_LINE_MENU.setState(visible);
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
        
        protected String getActionName() {
            return ExtKit.toggleLineNumbersAction;
        }
        
        protected Action getGlobalKitAction() {
            if (delegate == null) {
                delegate = new NbEditorKit.NbToggleLineNumbersAction();
            }
            return delegate;
        }
    }
    
    
    public static class GoToSourceAction extends MainMenuAction{
        
        private JMenuItem GOTO_SOURCE_MENU;

        public GoToSourceAction(){
            super();
            GOTO_SOURCE_MENU = new JMenuItem(getMenuItemText());
            setMenu();
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
        
        private  JMenuItem GOTO_SUPER_MENU;        

        public GoToSuperAction(){
            super();
            GOTO_SUPER_MENU = new JMenuItem(getMenuItemText());
            setMenu();
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

    /** Back action in Go To main menu, wrapper for BaseKit.jumpListPrevAction
     */ 
    public static final class JumpBackAction extends MainMenuAction {
        
        private JMenuItem jumpBackMenuItem;

        public JumpBackAction () {
            super();
            jumpBackMenuItem = new JMenuItem(getMenuItemText());
            setMenu();
        }
        
        protected String getMenuItemText () {
            return NbBundle.getBundle(JumpBackAction.class).getString(
                "jump_back_main_menu_item"); //NOI18N
        }

        public JMenuItem getMenuPresenter () {
            return jumpBackMenuItem;
        }

        protected String getActionName () {
            return BaseKit.jumpListPrevAction;
        }
        
        protected KeyStroke getDefaultAccelerator () {
            return KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.ALT_MASK);
        }
        
    } // end of JumpBackAction
    
    /** Forward action in Go To main menu, wrapper for BaseKit.jumpListNextAction
     */ 
    public static final class JumpForwardAction extends MainMenuAction {
        
        private JMenuItem jumpForwardMenuItem;

        public JumpForwardAction () {
            super();
            jumpForwardMenuItem = new JMenuItem(getMenuItemText());
            setMenu();
        }
        
        protected String getMenuItemText () {
            return NbBundle.getBundle(JumpForwardAction.class).getString(
                "jump_forward_main_menu_item"); //NOI18N
        }

        public JMenuItem getMenuPresenter () {
            return jumpForwardMenuItem;
        }

        protected String getActionName () {
            return BaseKit.jumpListNextAction;
        }
        
        protected KeyStroke getDefaultAccelerator () {
            return KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.ALT_MASK);
        }
        
    } // end of JumpForwardAction

    /** Reformat Code action in Source main menu, wrapper for BaseKit.formatAction
     */ 
    public static final class FormatAction extends MainMenuAction {
        
        private JMenuItem formatMenuItem;

        public FormatAction () {
            super();
            formatMenuItem = new JMenuItem(getMenuItemText());
            setMenu();
        }
        
        protected String getMenuItemText () {
            return NbBundle.getBundle(FormatAction.class).getString(
                "format_main_menu_item"); //NOI18N
        }

        public JMenuItem getMenuPresenter () {
            return formatMenuItem;
        }

        protected String getActionName () {
            return BaseKit.formatAction;
        }
        
    } // end of FormatAction
    
    /** Shift Left action in Source main menu, wrapper for BaseKit.shiftLineLeftAction
     */ 
    public static final class ShiftLineLeftAction extends MainMenuAction {
        
        private JMenuItem menuItem;

        public ShiftLineLeftAction () {
            super();
            menuItem = new JMenuItem(getMenuItemText());
            setMenu();
        }
        
        protected String getMenuItemText () {
            return NbBundle.getBundle(ShiftLineLeftAction.class).getString(
                "shift_line_left_main_menu_item"); //NOI18N
        }

        public JMenuItem getMenuPresenter () {
            return menuItem;
        }

        protected String getActionName () {
            return BaseKit.shiftLineLeftAction;
        }
        
    } // end of ShiftLineLeftAction
    
    /** Shift Right action in Source main menu, wrapper for BaseKit.shiftLineRightAction
     */ 
    public static final class ShiftLineRightAction extends MainMenuAction {
        
        private JMenuItem menuItem;

        public ShiftLineRightAction () {
            super();
            menuItem = new JMenuItem(getMenuItemText());
            setMenu();
        }
        
        protected String getMenuItemText () {
            return NbBundle.getBundle(ShiftLineRightAction.class).getString(
                "shift_line_right_main_menu_item"); //NOI18N
        }

        public JMenuItem getMenuPresenter () {
            return menuItem;
        }

        protected String getActionName () {
            return BaseKit.shiftLineRightAction;
        }
        
    } // end of ShiftLineRightAction
    
    /** Comment action in Source main menu, wrapper for ExtKit.commentAction
     */ 
    public static final class CommentAction extends MainMenuAction {
        
        private JMenuItem menuItem;

        public CommentAction () {
            super();
            menuItem = new JMenuItem(getMenuItemText());
            setMenu();
        }
        
        protected String getMenuItemText () {
            return NbBundle.getBundle(CommentAction.class).getString(
                "comment_main_menu_item"); //NOI18N
        }

        public JMenuItem getMenuPresenter () {
            return menuItem;
        }

        protected String getActionName () {
            return ExtKit.commentAction;
        }
        
    } // end of CommentAction
    
    /** Uncomment action in Source main menu, wrapper for ExtKit.uncommentAction
     */ 
    public static final class UncommentAction extends MainMenuAction {
        
        private JMenuItem menuItem;

        public UncommentAction () {
            super();
            menuItem = new JMenuItem(getMenuItemText());
            setMenu();
        }
        
        protected String getMenuItemText () {
            return NbBundle.getBundle(UncommentAction.class).getString(
                "uncomment_main_menu_item"); //NOI18N
        }

        public JMenuItem getMenuPresenter () {
            return menuItem;
        }

        protected String getActionName () {
            return ExtKit.uncommentAction;
        }
        
    } // end of UncommentAction
    
    /** Uncomment action in Source main menu, wrapper for ExtKit.uncommentAction
     */ 
    public static final class ToggleCommentAction extends MainMenuAction {
        
        private JMenuItem menuItem;

        public ToggleCommentAction () {
            super();
            menuItem = new JMenuItem(getMenuItemText());
            setMenu();
        }
        
        protected String getMenuItemText () {
            return NbBundle.getBundle(UncommentAction.class).getString(
                "toggle_comment_main_menu_item"); //NOI18N
        }

        public JMenuItem getMenuPresenter () {
            return menuItem;
        }

        protected String getActionName () {
            return ExtKit.toggleCommentAction;
        }
        
    } // end of UncommentAction
    
    /** Insert Next Matching Word action in Source main menu, wrapper for BaseKit.wordMatchNextAction
     */ 
    public static final class WordMatchNextAction extends MainMenuAction {
        
        private JMenuItem menuItem;

        public WordMatchNextAction () {
            super();
            menuItem = new JMenuItem(getMenuItemText());
            setMenu();
        }
        
        protected String getMenuItemText () {
            return NbBundle.getBundle(WordMatchNextAction.class).getString(
                "word_match_next_main_menu_item"); //NOI18N
        }

        public JMenuItem getMenuPresenter () {
            return menuItem;
        }

        protected String getActionName () {
            return BaseKit.wordMatchNextAction;
        }
        
    } // end of WordMatchNextAction

    /** Insert Previous Matching Word action in Source main menu, wrapper for BaseKit.wordMatchPrevAction
     */ 
    public static final class WordMatchPrevAction extends MainMenuAction {
        
        private JMenuItem menuItem;

        public WordMatchPrevAction () {
            super();
            menuItem = new JMenuItem(getMenuItemText());
            setMenu();
        }
        
        protected String getMenuItemText () {
            return NbBundle.getBundle(WordMatchPrevAction.class).getString(
                "word_match_previous_main_menu_item"); //NOI18N
        }

        public JMenuItem getMenuPresenter () {
            return menuItem;
        }

        protected String getActionName () {
            return BaseKit.wordMatchPrevAction;
        }
        
    } // end of WordMatchPrevAction

    /** Find Next action in Edit main menu, wrapper for BaseKit.findNextAction
     */ 
    public static final class FindNextAction extends MainMenuAction {
        
        private JMenuItem menuItem;

        public FindNextAction () {
            super(true, BLANK_ICON);
            menuItem = new JMenuItem(getMenuItemText());
            setMenu();
        }
        
        protected String getMenuItemText () {
            return NbBundle.getBundle(FindNextAction.class).getString(
                "find_next_main_menu_item"); //NOI18N
        }

        public JMenuItem getMenuPresenter () {
            return menuItem;
        }

        protected String getActionName () {
            return BaseKit.findNextAction;
        }
        
    } // end of FindNextAction
    
    
    /** Find Previous action in Edit main menu, wrapper for BaseKit.findPreviousAction
     */ 
    public static final class FindPreviousAction extends MainMenuAction {
        
        private JMenuItem menuItem;

        public FindPreviousAction () {
            super(true, BLANK_ICON);
            menuItem = new JMenuItem(getMenuItemText());
            setMenu();
        }
        
        protected String getMenuItemText () {
            return NbBundle.getBundle(FindNextAction.class).getString(
                "find_previous_main_menu_item"); //NOI18N
        }

        public JMenuItem getMenuPresenter () {
            return menuItem;
        }

        protected String getActionName () {
            return BaseKit.findPreviousAction;
        }
        
    } // end of FindPreviousAction

    /** Find Selection action in Edit main menu, wrapper for BaseKit.findSelectionAction
     */ 
    public static final class FindSelectionAction extends MainMenuAction {
        
        private JMenuItem menuItem;

        public FindSelectionAction () {
            super(true, BLANK_ICON);
            menuItem = new JMenuItem(getMenuItemText());
            setMenu();
        }
        
        protected String getMenuItemText () {
            return NbBundle.getBundle(FindNextAction.class).getString(
                "find_selection_main_menu_item"); //NOI18N
        }

        public JMenuItem getMenuPresenter () {
            return menuItem;
        }

        protected String getActionName () {
            return BaseKit.findSelectionAction;
        }
        
    } // end of FindSelectionAction

    /** Start Macro Recording action in View main menu, wrapper for BaseKit.startMacroRecordingAction
     */ 
    public static final class StartMacroRecordingAction extends MainMenuAction {
        
        private JMenuItem menuItem;

        public StartMacroRecordingAction () {
            super(true, BLANK_ICON);
            menuItem = new JMenuItem(getMenuItemText());
            setMenu();
        }
        
        protected String getMenuItemText () {
            return NbBundle.getBundle(StartMacroRecordingAction.class).getString(
                "start_macro_recording_main_menu_item"); //NOI18N
        }

        public JMenuItem getMenuPresenter () {
            return menuItem;
        }

        protected String getActionName () {
            return BaseKit.startMacroRecordingAction;
        }
        
    } // end of StartMacroRecordingAction

    /** Stop Macro Recording action in View main menu, wrapper for BaseKit.stopMacroRecordingAction
     */ 
    public static final class StopMacroRecordingAction extends MainMenuAction {
        
        private JMenuItem menuItem;

        public StopMacroRecordingAction () {
            super(true, BLANK_ICON);
            menuItem = new JMenuItem(getMenuItemText());
            setMenu();
        }
        
        protected String getMenuItemText () {
            return NbBundle.getBundle(StopMacroRecordingAction.class).getString(
                "stop_macro_recording_main_menu_item"); //NOI18N
        }

        public JMenuItem getMenuPresenter () {
            return menuItem;
        }

        protected String getActionName () {
            return BaseKit.stopMacroRecordingAction;
        }
        
    } // end of StopMacroRecordingAction
            
    /** Remove Trailing Spaces action in View main menu, wrapper for BaseKit.removeTrailingSpaces
     */ 
    public static final class RemoveTrailingSpacesAction extends MainMenuAction {

        private JMenuItem menuItem;

        public RemoveTrailingSpacesAction() {
            super(true, null);
            menuItem = new JMenuItem(getMenuItemText());
            setMenu();
        }

        protected String getMenuItemText () {
            return NbBundle.getBundle(RemoveTrailingSpacesAction.class).getString(
                "remove_trailing_spaces_main_menu_item"); //NOI18N
        }

        public JMenuItem getMenuPresenter () {
            return menuItem;
        }

        protected String getActionName () {
            return BaseKit.removeTrailingSpacesAction;
        }

    } // end of RemoveTrailingSpacesAction

    /** Paste Formatted action in Edit main menu, wrapper for BaseKit.pasteFormattedAction
     */ 
    public static final class PasteFormattedAction extends MainMenuAction {

        private JMenuItem menuItem;

        public PasteFormattedAction() {
            super(true, BLANK_ICON);
            menuItem = new JMenuItem(getMenuItemText());
            setMenu();
        }

        protected String getMenuItemText () {
            return NbBundle.getBundle(PasteFormattedAction.class).getString(
                "paste_formatted_main_menu_item"); //NOI18N
        }

        public JMenuItem getMenuPresenter () {
            return menuItem;
        }

        protected String getActionName () {
            return BaseKit.pasteFormatedAction;
        }

    } // end of PasteFormattedAction
    
}
    