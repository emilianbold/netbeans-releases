/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.KeyStroke;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import javax.swing.text.Keymap;
import org.netbeans.editor.ActionFactory;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.ext.ExtKit;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.Presenter;
import org.openide.actions.UndoAction;
import org.openide.actions.RedoAction;
import org.openide.windows.TopComponent;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.LocaleSupport;
import org.netbeans.editor.MacroDialogSupport;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.ext.ExtSettingsNames;
import org.netbeans.modules.editor.options.BaseOptions;
import org.netbeans.modules.editor.options.OptionUtilities;
import org.netbeans.modules.editor.options.AllOptionsFolder;
import org.netbeans.modules.editor.options.MacrosEditorPanel;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
* Java editor kit with appropriate document
*
* @author Miloslav Metelka
* @version 1.00
*/

public class NbEditorKit extends ExtKit {

    /** Action property that stores the name of the corresponding nb-system-action */
    public static final String SYSTEM_ACTION_CLASS_NAME_PROPERTY = "systemActionClassName"; // NOI18N

    static final long serialVersionUID =4482122073483644089L;
    
    private static final Map contentTypeTable;

    /** Name of the action for generating of Go To popup menu*/
    public static final String generateGoToPopupAction = "generate-goto-popup"; // NOI18N

    /** Name of the action for generating of code folding popup menu*/
    public static final String generateFoldPopupAction = "generate-fold-popup"; // NOI18N

    static {
        contentTypeTable = new HashMap();
        contentTypeTable.put("org.netbeans.modules.properties.syntax.PropertiesKit", "text/x-properties"); // NOI18N
        contentTypeTable.put("org.netbeans.modules.web.core.syntax.JSPKit", "text/x-jsp"); // NOI18N
        contentTypeTable.put("org.netbeans.modules.css.text.syntax.CSSEditorKit", "text/css"); // new  - open source package // NOI18N
        contentTypeTable.put("org.netbeans.modules.xml.css.editor.CSSEditorKit", "text/css"); // old  - close source package // NOI18N
        contentTypeTable.put("org.netbeans.modules.xml.text.syntax.DTDKit", "text/x-dtd"); // NOI18N
        contentTypeTable.put("org.netbeans.modules.xml.text.syntax.XMLKit", "text/xml"); // NOI18N
        contentTypeTable.put("org.netbeans.modules.corba.idl.editor.coloring.IDLKit", "text/x-idl"); // NOI18N
    }
    
    public NbEditorKit(){
        super();
        // lazy init of MIME options
        EditorModule.init();
        AllOptionsFolder aof = AllOptionsFolder.getDefault();
        if (aof != null) {
            aof.loadMIMEOption(this.getClass());
        }
    }

    public Document createDefaultDocument() {
        return new NbEditorDocument(this.getClass());
    }

    /**
     * Do any locking necessary prior evaluation of tooltip annotations.
     * <br>
     * This method will always be followed by {@link #toolTipAnnotationsUnlock()}
     * by using <code>try ... finally</code>.
     * <br>
     * This method is called prior read locking of the document.
     */
    protected void toolTipAnnotationsLock(Document doc) {
    }

    /**
     * Release any locking requested previously by {@link #toolTipAnnotationsLock()}.
     * <br>
     * This method is called after read unlocking of the document.
     */
    protected void toolTipAnnotationsUnlock(Document doc) {
    }

    protected EditorUI createEditorUI() {
        return new NbEditorUI();
    }

    protected Action[] createActions() {
        Action[] nbEditorActions = new Action[] {
                                       new NbBuildPopupMenuAction(),
                                       new NbStopMacroRecordingAction(),
                                       new NbUndoAction(),
                                       new NbRedoAction(),
                                       new NbBuildToolTipAction(),
                                       new NbToggleLineNumbersAction(),
                                       new ToggleToolbarAction(),
                                       new NbGenerateGoToPopupAction(),
                                       new GenerateFoldPopupAction()
                                   };
        return TextAction.augmentList(super.createActions(), nbEditorActions);
    }


    protected void addSystemActionMapping(String editorActionName, Class systemActionClass) {
        Action a = getActionByName(editorActionName);
        if (a != null) {
            a.putValue(SYSTEM_ACTION_CLASS_NAME_PROPERTY, systemActionClass.getName());
        }
    }
    
    protected void updateActions() {
        addSystemActionMapping(cutAction, org.openide.actions.CutAction.class);
        addSystemActionMapping(copyAction, org.openide.actions.CopyAction.class);
        addSystemActionMapping(pasteAction, org.openide.actions.PasteAction.class);
        addSystemActionMapping(removeSelectionAction, org.openide.actions.DeleteAction.class);

        addSystemActionMapping(findAction, org.openide.actions.FindAction.class);
        addSystemActionMapping(replaceAction, org.openide.actions.ReplaceAction.class);
        addSystemActionMapping(gotoAction, org.openide.actions.GotoAction.class);
    }

    private boolean isInheritorOfNbEditorKit(){
        Class clz = this.getClass();
        while(clz.getSuperclass() != null){
            clz = clz.getSuperclass();
            if (NbEditorKit.class == clz) return true;
        }
        return false;
    }
    
    public String getContentType() {
        if (isInheritorOfNbEditorKit()){
            ErrorManager.getDefault().log(ErrorManager.WARNING, 
                "Warning: KitClass "+this.getClass().getName()+" doesn't override the method getContentType."); //NOI18N
        }
        return (contentTypeTable.containsKey(this.getClass().getName())) ? 
            (String)contentTypeTable.get(this.getClass().getName()) : "text/"+this.getClass().getName().replace('.','_'); //NOI18N
    }

    
    
    public static class ToggleToolbarAction extends BaseAction {

        public ToggleToolbarAction() {
            super(ExtKit.toggleToolbarAction);
            putValue ("helpID", ToggleToolbarAction.class.getName ()); // NOI18N
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            boolean toolbarVisible = AllOptionsFolder.getDefault().isToolbarVisible();
            AllOptionsFolder.getDefault().setToolbarVisible(!toolbarVisible);
        }
        
        public JMenuItem getPopupMenuItem(JTextComponent target) {
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(LocaleSupport.getString("PROP_base_toolbarVisible"), AllOptionsFolder.getDefault().isToolbarVisible());
            item.addItemListener( new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    actionPerformed(null,null);
                }
            });
            return item;
        }
        
    }
    
    
    public class NbBuildPopupMenuAction extends BuildPopupMenuAction {

        static final long serialVersionUID =-8623762627678464181L;

        protected JPopupMenu createPopupMenu(JTextComponent component) {
            // to make keyboard navigation (Up/Down keys) inside popup work, we
            // must use JPopupMenuPlus instead of JPopupMenu
            return new org.openide.awt.JPopupMenuPlus();
        }

        protected JPopupMenu buildPopupMenu(JTextComponent component) {        
            JPopupMenu pm = createPopupMenu(component);
            List l;
            EditorUI ui = Utilities.getEditorUI(component);            
            BaseOptions bo = BaseOptions.getOptions(NbEditorKit.this.getClass());
            if (bo != null){
                l = OptionUtilities.getPopupStrings(bo.getOrderedMultiPropertyFolderFiles("Popup"), true); // NOI18N
            }else{
                l = (List)Settings.getValue(Utilities.getKitClass(component),
                    (ui == null || ui.hasExtComponent())
                        ? ExtSettingsNames.POPUP_MENU_ACTION_NAME_LIST
                        : ExtSettingsNames.DIALOG_POPUP_MENU_ACTION_NAME_LIST
                );
            }
            
            if (l != null) {
                Iterator i = l.iterator();
                while (i.hasNext()) {
                    Object obj = i.next();
                    if (obj == null) continue;
                    if (obj instanceof Action){
                        addAction(component, pm, (Action)obj);
                    }else if (obj instanceof javax.swing.JSeparator){
                        addAction(component, pm, (String)null);
                    }else{
                        String actionName = (String)obj;
                        addAction(component, pm, actionName);
                    }
                }
            }
            
            return pm;
        }

        private Lookup getContextLookup(java.awt.Component component){
            Lookup lookup = null;
            for (java.awt.Component c = component; c != null; c = c.getParent()) {
                if (c instanceof Lookup.Provider) {
                    lookup = ((Lookup.Provider)c).getLookup ();
                    if (lookup != null) {
                        break;
                    }
                }
            }
            return lookup;
        }
        
        private Action translateContextLookupAction(Lookup contextLookup, Action action) {
            if (action instanceof ContextAwareAction && contextLookup != null){
                action = ((org.openide.util.ContextAwareAction)action)
                .createContextAwareInstance(contextLookup);
            }
            return action;
        }
        
        private JMenuItem createLocalizedMenuItem(Action action) {
            JMenuItem item;
            if (action instanceof Presenter.Popup) {
                item = ((Presenter.Popup)action).getPopupPresenter();
            } else {
                item = new JMenuItem(action);
                Mnemonics.setLocalizedText(item, item.getText());
                if (item.getIcon() != null) item.setIcon(null); //filter out icons
            }
            return item;
        }
        
        private void assignAccelerator(Keymap km, Action action, JMenuItem item) {
            if (item.getAccelerator() == null){
                KeyStroke ks = (KeyStroke)action.getValue(Action.ACCELERATOR_KEY);
                if (ks!=null) {
                    item.setAccelerator(ks);
                } else {
                    // Try to get the accelerator from keymap
                    if (km != null) {
                        KeyStroke[] keys = km.getKeyStrokesForAction(action);
                        if (keys != null && keys.length > 0) {
                            item.setAccelerator(keys[0]);
                        }
                    }
                }
            }
        }
        
        protected void addAction(JTextComponent component, JPopupMenu popupMenu, Action action) {
            Lookup contextLookup = getContextLookup(component);
            action = translateContextLookupAction(contextLookup, action);

            if (action != null) {
                JMenuItem item = createLocalizedMenuItem(action);
                item.setEnabled(action.isEnabled());
                Object helpID = action.getValue ("helpID"); // NOI18N
                if (helpID != null && (helpID instanceof String)) {
                    item.putClientProperty ("HelpID", helpID); // NOI18N
                }
                assignAccelerator(component.getKeymap(), action, item);
                debugPopupMenuItem(item, action);
                popupMenu.add(item);
            }
        }
        
        private void addTopComponentActions(JTextComponent component, JPopupMenu popupMenu) {
            Lookup contextLookup = getContextLookup(component);
            // Get the cloneable-editor instance
            TopComponent tc = NbEditorUtilities.getOuterTopComponent(component);
            if (tc != null) {
                // Add all the actions
                Action[] actions = tc.getActions();
                for (int i = 0; i < actions.length; i++) {
                    Action action = actions[i];
                    action = translateContextLookupAction(contextLookup, action);
                    
                    if (action != null){
                        JMenuItem item = createLocalizedMenuItem(action);
                        assignAccelerator(
                            (Keymap)Lookup.getDefault().lookup(Keymap.class),
                            action,
                            item
                        );

                        debugPopupMenuItem(item, action);
                        popupMenu.add(item);
                    }
                }
            }
        }

        protected void addAction(JTextComponent component, JPopupMenu popupMenu,
        String actionName) {
            if (actionName != null) { // try if it's an action class name
                // Check for the TopComponent actions
                if (TopComponent.class.getName().equals(actionName)) {
                    addTopComponentActions(component, popupMenu);
                    return;

                } else { // not cloneable-editor actions

                    // Try to load the action class
                    Class saClass = null;
                    try {
                        ClassLoader loader = (ClassLoader)Lookup.getDefault().lookup(ClassLoader.class);
                        saClass = Class.forName(actionName, false, loader);
                    } catch (Throwable t) {
                    }
                    
                    if (saClass != null && SystemAction.class.isAssignableFrom(saClass)) {
                        Lookup contextLookup = getContextLookup(component);
                        Action action = SystemAction.get(saClass);
                        action = translateContextLookupAction(contextLookup, action);
                        
                        JMenuItem item = createLocalizedMenuItem(action);
                        if (item != null) { // && !(item instanceof JMenu))
                            assignAccelerator(
                                 (Keymap)Lookup.getDefault().lookup(Keymap.class),
                                 action,
                                 item
                            );
                            debugPopupMenuItem(item, action);
                            popupMenu.add(item);
                        }

                        return;
                    }
                }

            }

            super.addAction(component, popupMenu, actionName);

        }


    }

    public class NbStopMacroRecordingAction extends ActionFactory.StopMacroRecordingAction {
        
        private BaseOptions bo;
        
        private Map getKBMap(){
            Map ret;
            List list = bo.getKeyBindingList();
            if( list.size() > 0 &&
            ( list.get( 0 ) instanceof Class || list.get( 0 ) instanceof String )
            ) {
                list.remove( 0 ); //remove kit class name
            }
            ret = OptionUtilities.makeKeyBindingsMap(list);
            return ret;
        }
        
        protected MacroDialogSupport getMacroDialogSupport(Class kitClass){
            return new NbMacroDialogSupport(kitClass);
        }
        
        
        private class NbMacroDialogSupport extends MacroDialogSupport{
            
            public NbMacroDialogSupport( Class kitClass ) {
                super(kitClass);
            }
            
            public void actionPerformed(ActionEvent evt) {
                bo = BaseOptions.getOptions(NbEditorKit.this.getClass());
                Map oldMacroMap = null;
                Map oldKBMap = null;
                if (bo != null){
                    oldMacroMap = bo.getMacroMap();
                    oldKBMap = getKBMap();
                }

                super.actionPerformed(evt);

                if (bo != null){
                    Map newMacroMap = bo.getMacroMap();
                    bo.setMacroDiffMap(OptionUtilities.getMapDiff(oldMacroMap, newMacroMap, true));
                    bo.setKeyBindingsDiffMap(OptionUtilities.getMapDiff(oldKBMap, getKBMap(), true));
                    bo.setMacroMap(newMacroMap,false);
                    bo.setKeyBindingList(bo.getKeyBindingList(), false);
                }
            }
            
            protected int showConfirmDialog(String macroName){
                NotifyDescriptor confirm = new NotifyDescriptor.Confirmation(
                MessageFormat.format(
                NbBundle.getMessage(MacrosEditorPanel.class,"MEP_Overwrite"), //NOI18N
                new Object[] {macroName}),
                NotifyDescriptor.YES_NO_CANCEL_OPTION,
                NotifyDescriptor.WARNING_MESSAGE
                );
                org.openide.DialogDisplayer.getDefault().notify(confirm);
                return ((Integer)confirm.getValue()).intValue();
            }
            
        }
        
    }
        
    public static class NbUndoAction extends ActionFactory.UndoAction {

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            // Delegate to system undo action
            UndoAction ua = (UndoAction)SystemAction.get(UndoAction.class);
            if (ua != null && ua.isEnabled()) {
                ua.actionPerformed(evt);
            }
        }

    }

    public static class NbRedoAction extends ActionFactory.RedoAction {

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            // Delegate to system redo action
            RedoAction ra = (RedoAction)SystemAction.get(RedoAction.class);
            if (ra != null && ra.isEnabled()) {
                ra.actionPerformed(evt);
            }
        }

    }

    /** Switch visibility of line numbers in editor */
    public class NbToggleLineNumbersAction extends ActionFactory.ToggleLineNumbersAction {

        private BaseOptions bo;
        
        // no options for the kit, bugfix of #27568
        private boolean lineNumbersVisible = false;
        
        public NbToggleLineNumbersAction() {
            bo = BaseOptions.getOptions(NbEditorKit.this.getClass());
        }
        
        protected boolean isLineNumbersVisible() {
            return (bo != null) ? bo.getLineNumberVisible() : lineNumbersVisible;
        }
        
        protected void toggleLineNumbers() {
            if (bo != null){
                bo.setLineNumberVisible(!isLineNumbersVisible());
            }else{
                lineNumbersVisible = !lineNumbersVisible;
            }
        }
        
    }

    public static class NbGenerateGoToPopupAction extends BaseAction {

        public String getShortDescription() {
            return org.openide.util.NbBundle.getBundle (NbEditorKit.class).getString(generateGoToPopupAction); // NOI18N
        }
        
        public NbGenerateGoToPopupAction() {
            super(generateGoToPopupAction);
            String desc = getShortDescription();
            if (desc != null) {
                putValue(SHORT_DESCRIPTION, desc);
            }
            putValue(BaseAction.NO_KEYBINDING, Boolean.TRUE);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
        }

    }


    public static class NbBuildToolTipAction extends BuildToolTipAction {

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                NbToolTip.buildToolTip(target);
            }
        }

    }
    
    public static class GenerateFoldPopupAction extends BaseAction {

        private boolean addSeparatorBeforeNextAction;

        public GenerateFoldPopupAction() {
            super(generateFoldPopupAction);
            String desc = getShortDescription();
            if (desc != null) {
                putValue(SHORT_DESCRIPTION, desc);
            }
            putValue(BaseAction.NO_KEYBINDING, Boolean.TRUE);
        }

        public String getShortDescription() {
            return org.openide.util.NbBundle.getBundle (NbEditorKit.class).getString(generateFoldPopupAction); // NOI18N
        }
        
        public void actionPerformed(ActionEvent evt, JTextComponent target) {
        }

        private void addAcceleretors(Action a, JMenuItem item, JTextComponent target){
            // Try to get the accelerator
            Keymap km = (target == null) ? BaseKit.getKit(BaseKit.class).getKeymap() :
                    target.getKeymap();
            if (km != null) {
                KeyStroke[] keys = km.getKeyStrokesForAction(a);
                if (keys != null && keys.length > 0) {
                    boolean added = false;
                    for (int i = 0; i<keys.length; i++){
                        if ((keys[i].getKeyCode() == KeyEvent.VK_MULTIPLY) ||
                            keys[i].getKeyCode() == KeyEvent.VK_ADD){
                            item.setAccelerator(keys[i]);
                            added = true;
                            break;
                        }
                    }
                    if (added == false) item.setAccelerator(keys[0]);
                }
            }
        }

        protected String getItemText(JTextComponent target, String actionName, Action a) {
            String itemText;
            if (a instanceof BaseAction) {
                itemText = ((BaseAction)a).getPopupMenuText(target);
            } else {
                itemText = actionName;
            }
            return itemText;
        }
        
        
        protected void addAction(JTextComponent target, JMenu menu,
        String actionName) {
            if (addSeparatorBeforeNextAction) {
                addSeparatorBeforeNextAction = false;
                menu.addSeparator();
            }

            BaseKit kit = (target == null) ? BaseKit.getKit(BaseKit.class) : Utilities.getKit(target);
            if (!(kit instanceof BaseKit)) { //bugfix of #45101
                kit = BaseKit.getKit(BaseKit.class);
                target = null;
            }
            if (kit == null) return;
            boolean foldingEnabled = (target == null) ? false :
                ((Boolean)Settings.getValue(Utilities.getKitClass(target), SettingsNames.CODE_FOLDING_ENABLE)).booleanValue();
            Action a = kit.getActionByName(actionName);
            if (a != null) {
                JMenuItem item = null;
                if (a instanceof BaseAction) {
                    item = ((BaseAction)a).getPopupMenuItem(target);
                }
                if (item == null) {
                    String itemText = getItemText(target, actionName, a);
                    if (itemText != null) {
                        item = new JMenuItem(itemText);
                        item.addActionListener(a);
                        Mnemonics.setLocalizedText(item, itemText);
                        addAcceleretors(a, item, target);
                        item.setEnabled(a.isEnabled() && foldingEnabled);
                        Object helpID = a.getValue ("helpID"); // NOI18N
                        if (helpID != null && (helpID instanceof String))
                            item.putClientProperty ("HelpID", helpID); // NOI18N
                    }
                }

                if (item != null) {
                    menu.add(item);
                }

            } else { // action-name is null, add the separator
                menu.addSeparator();
            }
        }        
        
        protected void setAddSeparatorBeforeNextAction(boolean addSeparator) {
            this.addSeparatorBeforeNextAction = addSeparator;
        }
        
        protected void addAdditionalItems(JTextComponent target, JMenu menu){
            setAddSeparatorBeforeNextAction(false);
        }
        
        public JMenuItem getPopupMenuItem(JTextComponent target) {
            String menuText = org.openide.util.NbBundle.getBundle (NbEditorKit.class).
                getString("Menu/View/CodeFolds");
            JMenu menu = new JMenu(menuText);
            Mnemonics.setLocalizedText(menu, menuText);
            setAddSeparatorBeforeNextAction(false);
            addAction(target, menu, BaseKit.collapseFoldAction);
            addAction(target, menu, BaseKit.expandFoldAction);
            setAddSeparatorBeforeNextAction(true);
            addAction(target, menu, BaseKit.collapseAllFoldsAction);
            addAction(target, menu, BaseKit.expandAllFoldsAction);
            // By default add separator before next actions (can be overriden if unwanted)
            setAddSeparatorBeforeNextAction(true);
            if (target != null) addAdditionalItems(target, menu);
            return menu;
        }
    
    }
    

}
