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

package org.netbeans.modules.editor;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.font.FontRenderContext;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.prefs.Preferences;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.KeyStroke;
import javax.swing.text.Document;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;
import javax.swing.text.Keymap;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.settings.SimpleValueNames;
import org.netbeans.editor.ActionFactory;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.SideBarFactory;
import org.netbeans.editor.ext.ExtKit;
import org.netbeans.modules.editor.impl.CustomizableSideBar.SideBarPosition;
import org.openide.awt.DynamicMenuContent;
import org.openide.loaders.DataFolder;
import org.openide.util.Exceptions;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.Presenter;
import org.openide.actions.UndoAction;
import org.openide.actions.RedoAction;
import org.openide.windows.TopComponent;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.MacroDialogSupport;
import org.netbeans.editor.MimeTypeInitializer;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;
import org.netbeans.modules.editor.impl.ActionsList;
import org.netbeans.modules.editor.impl.CustomizableSideBar;
import org.netbeans.modules.editor.impl.EditorActionsProvider;
import org.netbeans.modules.editor.impl.SearchBar;
import org.netbeans.modules.editor.impl.PopupMenuActionsProvider;
import org.netbeans.modules.editor.impl.ToolbarActionsProvider;
import org.netbeans.modules.editor.impl.actions.NavigationHistoryBackAction;
import org.netbeans.modules.editor.impl.actions.NavigationHistoryForwardAction;
import org.netbeans.modules.editor.lib2.EditorPreferencesDefaults;
import org.netbeans.modules.editor.lib.ColoringMap;
import org.netbeans.modules.editor.options.AnnotationTypesFolder;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
* Java editor kit with appropriate document
*
* @author Miloslav Metelka
* @version 1.00
*/

public class NbEditorKit extends ExtKit implements Callable {

    private static final Logger LOG = Logger.getLogger(NbEditorKit.class.getName());
    
    /** Action property that stores the name of the corresponding nb-system-action */
    public static final String SYSTEM_ACTION_CLASS_NAME_PROPERTY = "systemActionClassName"; // NOI18N

    static final long serialVersionUID =4482122073483644089L;
    
    private static final Map<String, String> contentTypeTable;

    /** Name of the action for generating of Go To popup menu*/
    public static final String generateGoToPopupAction = "generate-goto-popup"; // NOI18N

    /** Name of the action for generating of code folding popup menu*/
    public static final String generateFoldPopupAction = "generate-fold-popup"; // NOI18N

    private static final NbUndoAction nbUndoActionDef = new NbUndoAction();
    private static final NbRedoAction nbRedoActionDef = new NbRedoAction();
    
    private Map systemAction2editorAction = new HashMap();
    
    static {
        contentTypeTable = new HashMap<String, String>();
        contentTypeTable.put("org.netbeans.modules.properties.syntax.PropertiesKit", "text/x-properties"); // NOI18N
        contentTypeTable.put("org.netbeans.modules.web.core.syntax.JSPKit", "text/x-jsp"); // NOI18N
        contentTypeTable.put("org.netbeans.modules.css.text.syntax.CSSEditorKit", "text/css"); // new  - open source package // NOI18N
        contentTypeTable.put("org.netbeans.modules.xml.css.editor.CSSEditorKit", "text/css"); // old  - close source package // NOI18N
        contentTypeTable.put("org.netbeans.modules.xml.text.syntax.DTDKit", "text/x-dtd"); // NOI18N
        contentTypeTable.put("org.netbeans.modules.xml.text.syntax.XMLKit", "text/xml"); // NOI18N
        contentTypeTable.put("org.netbeans.modules.corba.idl.editor.coloring.IDLKit", "text/x-idl"); // NOI18N
    }
    
    public NbEditorKit() {
        super();
//        new Throwable("NbEditorKit: " + getClass()).printStackTrace();
    }

    
    // XXX: should idealy be final, but can't. Document in javadoc what needs
    // to be done when overriding this method.
    public @Override Document createDefaultDocument() {
        return new NbEditorDocument(getContentType());
    }

    /**
     * Do any locking necessary prior evaluation of tooltip annotations.
     * <br>
     * This method will always be followed by {@link #toolTipAnnotationsUnlock(Document)}
     * by using <code>try ... finally</code>.
     * <br>
     * This method is called prior read locking of the document.
     */
    protected void toolTipAnnotationsLock(Document doc) {
    }

    /**
     * Release any locking requested previously by {@link #toolTipAnnotationsLock(Document)}.
     * <br>
     * This method is called after read unlocking of the document.
     */
    protected void toolTipAnnotationsUnlock(Document doc) {
    }

    protected @Override EditorUI createEditorUI() {
        return new NbEditorUI();
    }

    protected @Override Action[] createActions() {
        Action[] nbEditorActions = new Action[] {
                                       nbUndoActionDef,
                                       nbRedoActionDef,
                                       new GenerateFoldPopupAction(),
                                       new NavigationHistoryBackAction(),
                                       new NavigationHistoryForwardAction(),
                                       new SearchBar.IncrementalSearchForwardAction(),
                                       new SearchBar.IncrementalSearchBackwardAction(),
//                                       new ToggleToolbarAction(),
//                                       new NbToggleLineNumbersAction(),
                                       new NbGenerateGoToPopupAction(),
                                   };
        return TextAction.augmentList(super.createActions(), nbEditorActions);
    }

    @Override
    protected Action[] getDeclaredActions() {
        List<Action> declaredActionList = EditorActionsProvider.getEditorActions(getContentType());
        Action[] declaredActions = new Action[declaredActionList.size()];
        declaredActionList.toArray(declaredActions);
        return declaredActions;
    }


    protected void addSystemActionMapping(String editorActionName, Class systemActionClass) {
        Action a = getActionByName(editorActionName);
        if (a != null) {
            a.putValue(SYSTEM_ACTION_CLASS_NAME_PROPERTY, systemActionClass.getName());
        }
        systemAction2editorAction.put(systemActionClass.getName(), editorActionName);
    }
    
    protected @Override void updateActions() {
        addSystemActionMapping(cutAction, org.openide.actions.CutAction.class);
        addSystemActionMapping(copyAction, org.openide.actions.CopyAction.class);
        addSystemActionMapping(pasteAction, org.openide.actions.PasteAction.class);
        // #69077 - DeleteAction now delegates to deleteNextCharAction
        addSystemActionMapping(deleteNextCharAction, org.openide.actions.DeleteAction.class);
        addSystemActionMapping(showPopupMenuAction, org.openide.actions.PopupAction.class);

        addSystemActionMapping(SearchBar.INCREMENTAL_SEARCH_FORWARD, org.openide.actions.FindAction.class);
        addSystemActionMapping(replaceAction, org.openide.actions.ReplaceAction.class);
        addSystemActionMapping(gotoAction, org.openide.actions.GotoAction.class);

        addSystemActionMapping(undoAction, org.openide.actions.UndoAction.class);
        addSystemActionMapping(redoAction, org.openide.actions.RedoAction.class);
    }

    private boolean isInheritorOfNbEditorKit(){
        Class clz = this.getClass();
        while(clz.getSuperclass() != null){
            clz = clz.getSuperclass();
            if (NbEditorKit.class == clz) return true;
        }
        return false;
    }
    
    public @Override String getContentType() {
        if (isInheritorOfNbEditorKit()){
            Logger.getLogger("global").log(Level.WARNING,
                "Warning: KitClass "+this.getClass().getName()+" doesn't override the method getContentType."); //NOI18N
        }
        return (contentTypeTable.containsKey(this.getClass().getName())) ? 
            (String)contentTypeTable.get(this.getClass().getName()) : super.getContentType(); //NOI18N
    }

    private static ResourceBundle getBundleFromName (String name) {
        ResourceBundle bundle = null;
        if (name != null) {
            try {
                bundle = NbBundle.getBundle (name);
            } catch (MissingResourceException mre) {
                //ErrorManager.getDefault ().notify (mre);
            }
        }
        return bundle;
    }

    /**
     * @deprecated this action is no longer used. It is reimplemented in editor.actions module.
     */
    //@EditorActionRegistration(name = toggleToolbarAction)
    // Registration in createActions() due to getPopupMenuItem()
    public static class ToggleToolbarAction extends BaseAction {

        public ToggleToolbarAction() {
            super(toggleToolbarAction); // Due to creation from MainMenuAction
            putValue ("helpID", ToggleToolbarAction.class.getName ()); // NOI18N
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
            Preferences prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
            boolean toolbarVisible = prefs.getBoolean(SimpleValueNames.TOOLBAR_VISIBLE_PROP, EditorPreferencesDefaults.defaultToolbarVisible);
            prefs.putBoolean(SimpleValueNames.TOOLBAR_VISIBLE_PROP, !toolbarVisible);
        }
        
        public @Override JMenuItem getPopupMenuItem(JTextComponent target) {
            Preferences prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
            boolean toolbarVisible = prefs.getBoolean(SimpleValueNames.TOOLBAR_VISIBLE_PROP, EditorPreferencesDefaults.defaultToolbarVisible);
            
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(
                NbBundle.getBundle(ToggleToolbarAction.class).getString("PROP_base_toolbarVisible"), //NOI18N
                toolbarVisible);
            
            item.addItemListener( new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    actionPerformed(null,null);
                }
            });
            
            return item;
        }
        
        protected @Override Class getShortDescriptionBundleClass() {
            return BaseKit.class;
        }
    }

    private static Lookup getContextLookup(java.awt.Component component){
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

    private static Action translateContextLookupAction(Lookup contextLookup, Action action) {
        if (action instanceof ContextAwareAction && contextLookup != null){
            action = ((org.openide.util.ContextAwareAction)action)
            .createContextAwareInstance(contextLookup); // May return null
        }
        return action;
    }

    private static JMenuItem createLocalizedMenuItem(Action action) {
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
    
    private static void assignAccelerator(Keymap km, Action action, JMenuItem item) {
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

    @EditorActionRegistration(name = buildPopupMenuAction)
    public static class NbBuildPopupMenuAction extends BuildPopupMenuAction {

        static final long serialVersionUID =-8623762627678464181L;

        public NbBuildPopupMenuAction() {
        }

        protected @Override JPopupMenu createPopupMenu(JTextComponent component) {
            // to make keyboard navigation (Up/Down keys) inside popup work, we
            // must use JPopupMenuPlus instead of JPopupMenu
            return new org.openide.awt.JPopupMenuPlus();
        }

        protected @Override JPopupMenu buildPopupMenu(JTextComponent component) {        
            EditorUI ui = Utilities.getEditorUI(component);
            if (!ui.hasExtComponent()) {
                return null;
            }
            
            JPopupMenu pm = createPopupMenu(component);
            
            String mimeType = NbEditorUtilities.getMimeType(component);
            List l = PopupMenuActionsProvider.getPopupMenuItems(mimeType);
            
            if (l.isEmpty()){
                String settingName = ui == null || ui.hasExtComponent()
                        ? "popup-menu-action-name-list" //NOI18N
                        : "dialog-popup-menu-action-name-list"; //NOI18N

                Preferences prefs = MimeLookup.getLookup(mimeType).lookup(Preferences.class);
                String actionNames = prefs.get(settingName, null);

                if (actionNames != null) {
                    for(StringTokenizer t = new StringTokenizer(actionNames, ","); t.hasMoreTokens(); ) { //NOI18N
                        String action = t.nextToken().trim();
                        l.add(action);
                    }
                }
            }
            
            if (l != null) {
                for (Iterator i = l.iterator(); i.hasNext(); ) {
                    Object obj = i.next();
                    
                    if (obj == null || obj instanceof javax.swing.JSeparator) {
                        addAction(component, pm, (String)null);
                    } else if (obj instanceof String) {
                        addAction(component, pm, (String)obj);
                    } else if (obj instanceof Action) {
                        addAction(component, pm, (Action)obj);
                    } else if (obj instanceof DataFolder) {
                        pm.add(new LayerSubFolderMenu(component, ((DataFolder) obj).getPrimaryFile()));
                    }
                }
            }
            
            return pm;
        }

        protected void addAction(JTextComponent component, JPopupMenu popupMenu, Action action) {
            Lookup contextLookup = getContextLookup(component);
            
            // issue #69688
            EditorKit kit = component.getUI().getEditorKit(component);
            if (contextLookup == null && (kit instanceof NbEditorKit) &&
                    ((NbEditorKit)kit).systemAction2editorAction.containsKey(action.getClass().getName())){
                addAction(component, popupMenu, (String) ((NbEditorKit)kit).systemAction2editorAction.get(action.getClass().getName()));
                return;
            }
            
            action = translateContextLookupAction(contextLookup, action);

            if (action != null) {
                JMenuItem item = createLocalizedMenuItem(action);
                if (item instanceof DynamicMenuContent) {
                    Component[] cmps = ((DynamicMenuContent)item).getMenuPresenters();
                    for (int i = 0; i < cmps.length; i++) {
                        popupMenu.add(cmps[i]);
                    }
                } else {
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
        }
        
        private void addTopComponentActions(JTextComponent component, JPopupMenu popupMenu) {
            Lookup contextLookup = getContextLookup(component);
            // Get the cloneable-editor instance
            TopComponent tc = NbEditorUtilities.getOuterTopComponent(component);
            if (tc != null) {
                // Add all the actions
                Action[] actions = tc.getActions();
                Component[] comps = org.openide.util.Utilities.actionsToPopup(actions, contextLookup).getComponents();
                for (int i = 0; i < comps.length; i++) {
                    popupMenu.add(comps[i]);
                }
            }
        }

        protected @Override void addAction(JTextComponent component, JPopupMenu popupMenu, String actionName) {
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
                        JMenuItem item = (action != null) ? createLocalizedMenuItem(action) : null;
                        if (item != null) {
                            if (item instanceof DynamicMenuContent) {
                                Component[] cmps = ((DynamicMenuContent)item).getMenuPresenters();
                                for (int i = 0; i < cmps.length; i++) {
                                    popupMenu.add(cmps[i]);
                                }
                            } else {
                                if (!(item instanceof JMenu)) {
                                    assignAccelerator(
                                         (Keymap)Lookup.getDefault().lookup(Keymap.class),
                                         action,
                                         item
                                    );
                                }
                                debugPopupMenuItem(item, action);
                                popupMenu.add(item);
                            }
                        }

                        return;
                    }
                }

            }

            super.addAction(component, popupMenu, actionName);

        }


    }

    /**
     * @deprecated Without any replacement. This class is no longer functional.
     */
    public class NbStopMacroRecordingAction extends ActionFactory.StopMacroRecordingAction {
        protected @Override MacroDialogSupport getMacroDialogSupport(Class kitClass){
            return super.getMacroDialogSupport(kitClass);
        }
    } // End of NbStopMacroRecordingAction class
        
    public static class NbUndoAction extends ActionFactory.UndoAction {

        public @Override void actionPerformed(ActionEvent evt, JTextComponent target) {
            Document doc = target.getDocument();
            if (doc.getProperty(BaseDocument.UNDO_MANAGER_PROP) != null) { // Basic way of undo
                super.actionPerformed(evt, target);
            } else { // Deleagte to system undo action
                // Delegate to system undo action
                UndoAction ua = (UndoAction)SystemAction.get(UndoAction.class);
                if (ua != null && ua.isEnabled()) {
                    ua.actionPerformed(evt);
                }
            }
        }

    }

    public static class NbRedoAction extends ActionFactory.RedoAction {

        public @Override void actionPerformed(ActionEvent evt, JTextComponent target) {
            Document doc = target.getDocument();
            if (doc.getProperty(BaseDocument.UNDO_MANAGER_PROP) != null) { // Basic way of undo
                super.actionPerformed(evt, target);
            } else { // Deleagte to system undo action
                // Delegate to system redo action
                RedoAction ra = (RedoAction)SystemAction.get(RedoAction.class);
                if (ra != null && ra.isEnabled()) {
                    ra.actionPerformed(evt);
                }
            }
        }

    }

    /**
     * Switch visibility of line numbers in editor.
     * @deprecated this action is no longer used. It is reimplemented in editor.actions module.
     */
    //@EditorActionRegistration(name = BaseKit.toggleLineNumbersAction)
    // Registration in createActions() due to getPopupMenuItem() in predecessor
    public static final class NbToggleLineNumbersAction extends ActionFactory.ToggleLineNumbersAction {

        public NbToggleLineNumbersAction() {
        }
        
        protected @Override boolean isLineNumbersVisible() {
            Preferences prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
            return prefs.getBoolean(SimpleValueNames.LINE_NUMBER_VISIBLE, EditorPreferencesDefaults.defaultLineNumberVisible);
        }
        
        protected @Override void toggleLineNumbers() {
            Preferences prefs = MimeLookup.getLookup(MimePath.EMPTY).lookup(Preferences.class);
            boolean visible = prefs.getBoolean(SimpleValueNames.LINE_NUMBER_VISIBLE, EditorPreferencesDefaults.defaultLineNumberVisible);
            prefs.putBoolean(SimpleValueNames.LINE_NUMBER_VISIBLE, !visible);
        }
    }

    // Annotation registration disabled due to overriding in GSF kit
//    @EditorActionRegistration(name = generateGoToPopupAction)
    public static class NbGenerateGoToPopupAction extends BaseAction {

        public NbGenerateGoToPopupAction() {
            super(generateGoToPopupAction); // Because of action in Gsf
            putValue(BaseAction.NO_KEYBINDING, Boolean.TRUE);
        }

        public void actionPerformed(ActionEvent evt, JTextComponent target) {
        }

        protected @Override Class getShortDescriptionBundleClass() {
            return NbEditorKit.class;
        }
        
    }


    @EditorActionRegistration(name = buildToolTipAction)
    public static class NbBuildToolTipAction extends BuildToolTipAction {

        public @Override void actionPerformed(ActionEvent evt, JTextComponent target) {
            if (target != null) {
                NbToolTip.buildToolTip(target);
            }
        }

    }

// No registration - NO_KEYBINDING property in constructor
//    @EditorActionRegistration(name = generateFoldPopupAction)
    public static class GenerateFoldPopupAction extends BaseAction {

        private boolean addSeparatorBeforeNextAction;

        public GenerateFoldPopupAction() {
            super(generateFoldPopupAction);
            putValue(BaseAction.NO_KEYBINDING, Boolean.TRUE);
        }

        protected @Override Class getShortDescriptionBundleClass() {
            return NbEditorKit.class;
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
                itemText = (String) a.getValue("popupText");
                if (itemText == null) {
                    itemText = (String) a.getValue("menuText");
                    if (itemText == null) {
                        // Do not try to get Action.SHORT_DESCRIPTION property
                        // since for system actions it would contain action's description
                        // inappropriate for the popup menu (actionName is a localized text
                        // suitable for popup menu for system actions).
                        itemText = actionName;
                    }
                }
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
            
            boolean foldingEnabled = false;
            if (target != null) {
                Preferences prefs = MimeLookup.getLookup(DocumentUtilities.getMimeType(target)).lookup(Preferences.class);
                foldingEnabled = prefs.getBoolean(SimpleValueNames.CODE_FOLDING_ENABLE, EditorPreferencesDefaults.defaultCodeFoldingEnable);
            }
            
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
        
        public @Override JMenuItem getPopupMenuItem(JTextComponent target) {
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

    
    private static final class LayerSubFolderMenu extends JMenu {

        private static String getLocalizedName(FileObject f) {
            Object displayName = f.getAttribute("displayName"); //NOI18N
            if (displayName instanceof String) {
                return (String) displayName;
            } else {
                try {
                    return f.getFileSystem().getStatus().annotateName(
                        f.getNameExt(),
                        Collections.singleton(f));
                } catch (FileStateInvalidException e) {
                    return f.getNameExt();
                }
            }
        }
        
        public LayerSubFolderMenu(JTextComponent target, FileObject folder) {
            this(target, getLocalizedName(folder), ActionsList.convert(sort(folder.getChildren()), false));
        }
        
        private static List<FileObject> sort( FileObject[] children ) {
            List<FileObject> fos = Arrays.asList(children);
            fos = FileUtil.getOrder(fos, true);
            return fos;
        }
        
        private LayerSubFolderMenu(JTextComponent target, String text, List items) {
            super();
            Mnemonics.setLocalizedText(this, text);
            
            for (Iterator i = items.iterator(); i.hasNext(); ) {
                Object obj = i.next();
                
                if (obj == null || obj instanceof javax.swing.JSeparator) {
                    addSeparator();
                } else if (obj instanceof String) {
                    addAction(target, this, (String)obj);
                } else if (obj instanceof Action) {
                    addAction(target, this, (Action)obj);
                } else if (obj instanceof DataFolder) {
                    this.add(new LayerSubFolderMenu(target, ((DataFolder) obj).getPrimaryFile()));
                }
            }
        }

        private static void addAcceleretors(Action a, JMenuItem item, JTextComponent target) {
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
                    if (added == false) {
                        item.setAccelerator(keys[0]);
                    }
                }else if (a!=null){
                    KeyStroke ks = (KeyStroke)a.getValue(Action.ACCELERATOR_KEY);
                    if (ks!=null) {
                        item.setAccelerator(ks);
                    }
                }
            }
        }

        private static String getItemText(JTextComponent target, String actionName, Action a) {
            String itemText;
            if (a instanceof BaseAction) {
                itemText = ((BaseAction)a).getPopupMenuText(target);
            } else {
                itemText = (String) a.getValue("popupText");
                if (itemText == null) {
                    itemText = (String) a.getValue("menuText");
                    if (itemText == null) {
                        itemText = actionName;
                    }
                }
            }
            return itemText;
        }

        private static void addAction(JTextComponent target, JMenu menu, String actionName) {
            assert target != null : "The parameter target must not be null"; //NOI18N
            assert menu != null : "The parameter menu must not be null"; //NOI18N
            assert actionName != null : "The parameter actionName must not be null";//NOI18N
            
            BaseKit kit = Utilities.getKit(target);
            if (kit == null) return;
            Action a = kit.getActionByName(actionName);
            if (a != null) {
                addAction(target, menu, a);
            }
        }        
        
        
        private static void addAction(JTextComponent target, JMenu menu, Action action) {
            assert target != null : "The parameter target must not be null"; //NOI18N
            assert menu != null : "The parameter menu must not be null"; //NOI18N
            assert action != null : "The parameter action must not be null"; //NOI18N
            
            JMenuItem item = null;
            if (action instanceof BaseAction) {
                item = ((BaseAction)action).getPopupMenuItem(target);
            }

            if (item == null) {
                Lookup contextLookup = getContextLookup(target);
                Action nonContextAction = action;
                action = translateContextLookupAction(contextLookup, action);
                if (action != null) {
                    item = createLocalizedMenuItem(action);
                    String actionName = (String) action.getValue(Action.NAME);
                    String itemText = getItemText(target, actionName, action);
                    if (itemText != null) {
                        item.setText(itemText);
                        Mnemonics.setLocalizedText(item, itemText);
                    }
                    // Search for shortcut by using original non-context action.
                    // Since JTextComponent.DefaultKeymap.getKeyStrokesForAction(Action a)
                    // uses == for comparison when searching for shortcut then
                    // there would be no match for ContextAwareAction that would produce
                    // a fresh action's instance. Thus it's better to use original action
                    // when searching for an accelerator.
                    addAcceleretors(nonContextAction, item, target);
                    item.setEnabled(action.isEnabled());
                    Object helpID = action.getValue ("helpID"); // NOI18N
                    if (helpID != null && (helpID instanceof String)) {
                        item.putClientProperty ("HelpID", helpID); // NOI18N
                    }
                }
            }

            if (item != null) {
                menu.add(item);
            }
        }        
    }

    public Object call() {
        Map<SideBarPosition, List<SideBarFactory>> factoriesMap = CustomizableSideBar.getFactoriesMap(getContentType());
        //initialize all factories
        for (Entry<SideBarPosition, List<SideBarFactory>> e : factoriesMap.entrySet()) {
            for (SideBarFactory f : e.getValue()) {
                if (f instanceof MimeTypeInitializer) { //TODO: SideBarFactory should probably implement MimeTypeInitializer
                    try {
                        ((MimeTypeInitializer) f).init(getContentType());
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }

        NbEditorToolBar.initKeyBindingList(getContentType());
        ToolbarActionsProvider.getToolbarItems(getContentType());
        ToolbarActionsProvider.getToolbarItems("text/base"); //NOI18N
        //#159661: Pre-initialize annotationTypesFolder here
        AnnotationTypesFolder.getAnnotationTypesFolder();

        // initialize coloring map (#69232)
        ColoringMap cmap = ColoringMap.get(getContentType());

        // initialize fonts (#170423)
        Coloring defaultColoring = cmap.getMap().get("default"); //NOI18N
        if (defaultColoring != null) {
            defaultColoring.getFont().getMaxCharBounds(new FontRenderContext(null, true, true));
        }

        return null;
    }
}
