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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.editor;

import java.awt.Component;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.plaf.TextUI;
import javax.swing.plaf.ToolBarUI;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.MultiKeyBinding;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsChangeEvent;
import org.netbeans.editor.SettingsChangeListener;
import org.netbeans.editor.SettingsNames;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.impl.ToolbarActionsProvider;
import org.netbeans.modules.editor.options.AllOptionsFolder;
import org.netbeans.modules.editor.options.BaseOptions;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Editor toolbar component.
 * <br>
 * Toolbar contents are obtained by merging of
 * Editors/mime-type/Toolbars/Default
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

/* package */ final class NbEditorToolBar extends JToolBar implements SettingsChangeListener {
    
    /** Flag for testing the sorting support by debugging messages. */
    private static final boolean debugSort
        = Boolean.getBoolean("netbeans.debug.editor.toolbar.sort"); // NOI18N

    private static final Insets BUTTON_INSETS = new Insets(2, 1, 0, 1);
    
    // An empty lookup. Can't use Lookup.EMPTY as this can be returned by clients.
    private static final Lookup NO_ACTION_CONTEXT = Lookups.fixed();
    
    private FileChangeListener moduleRegListener;

    /** Shared mouse listener used for setting the border painting property
     * of the toolbar buttons and for invoking the popup menu.
     */
    private static final MouseListener sharedMouseListener
        = new org.openide.awt.MouseUtils.PopupMouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                Object src = evt.getSource();
                
                if (src instanceof AbstractButton) {
                    AbstractButton button = (AbstractButton)evt.getSource();
                    if (button.isEnabled()) {
                        button.setContentAreaFilled(true);
                        button.setBorderPainted(true);
                    }
                }
            }
            
            public void mouseExited(MouseEvent evt) {
                Object src = evt.getSource();
                if (src instanceof AbstractButton)
                {
                    AbstractButton button = (AbstractButton)evt.getSource();
                    button.setContentAreaFilled(false);
                    button.setBorderPainted(false);
                }
            }
            
            protected void showPopup(MouseEvent evt) {
            }
        };
       

    
    /** Text component for which the toolbar gets constructed. */
    private Reference componentRef;
    
    private boolean presentersAdded;

    private boolean addListener = true;
    
    private static final String NOOP_ACTION_KEY = "noop-action-key"; //NOI18N
    private static final Action NOOP_ACTION = new NoOpAction();
    
   
    public NbEditorToolBar(JTextComponent component) {
        this.componentRef = new WeakReference(component);
        
        setFloatable(false);
        //mkleint - instead of here, assign the border in CloneableEditor and MultiView module.
//        // special border installed by core or no border if not available
//        Border b = (Border)UIManager.get("Nb.Editor.Toolbar.border"); //NOI18N
//        setBorder(b);
        addMouseListener(sharedMouseListener);
        Settings.addSettingsChangeListener(this);
        settingsChange(null);

        installModulesInstallationListener();
        installNoOpActionMappings();
    }

    // issue #69642
    private void installNoOpActionMappings(){
        InputMap im = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        // cut
        KeyStroke[] keys = findEditorKeys(DefaultEditorKit.cutAction, KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK));
        for (int i = 0; i < keys.length; i++) {
            im.put(keys[i], NOOP_ACTION_KEY);
        }
        // copy
        keys = findEditorKeys(DefaultEditorKit.copyAction, KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
        for (int i = 0; i < keys.length; i++) {
            im.put(keys[i], NOOP_ACTION_KEY);
        }
        // delete
        keys = findEditorKeys(DefaultEditorKit.deleteNextCharAction, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0)); //NOI18N
        for (int i = 0; i < keys.length; i++) {
            im.put(keys[i], NOOP_ACTION_KEY);
        }
        // paste
        keys = findEditorKeys(DefaultEditorKit.pasteAction, KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK));
        for (int i = 0; i < keys.length; i++) {
            im.put(keys[i], NOOP_ACTION_KEY);
        }
        
        getActionMap().put(NOOP_ACTION_KEY, NOOP_ACTION);
    }
    
    /** See issue #57773 for details. Toolbar should be updated with possible changes after
       module install/uninstall */
    private void installModulesInstallationListener(){
        moduleRegListener = new FileChangeAdapter() {
            public void fileChanged(FileEvent fe){
                //some module installed/uninstalled. Refresh toolbar content
                Runnable r = new Runnable() {
                        public void run() {
                            if (AllOptionsFolder.getDefault().isToolbarVisible()){
                                checkPresentersRemoved();
                                checkPresentersAdded();                                
                            }
                        }
                     };
                Utilities.runInEventDispatchThread(r);
            }
        };

        FileObject moduleRegistry = Repository.getDefault().getDefaultFileSystem().findResource("Modules"); //NOI18N

        if (moduleRegistry !=null){
            moduleRegistry.addFileChangeListener(
                FileUtil.weakFileChangeListener(moduleRegListener, moduleRegistry));
        }
    }
    
    public String getUIClassID() {
        //For GTK and Aqua look and feels, we provide a custom toolbar UI -
        //but we cannot override this globally or it will cause problems for
        //the form editor & other things
        if (UIManager.get("Nb.Toolbar.ui") != null) { //NOI18N
            return "Nb.Toolbar.ui"; //NOI18N
        } else {
            return super.getUIClassID();
        }
    }
    
    public String getName() {
        //Required for Aqua L&F toolbar UI
        return "editorToolbar"; // NOI18N
    }
    
    public void setUI(ToolBarUI ui){
        addListener = false;
        super.setUI(ui);
        addListener = true;
    }
    
    public synchronized void addMouseListener(MouseListener l){
        if (addListener){
            super.addMouseListener(l);
        }
    }
    
    public synchronized void addMouseMotionListener(MouseMotionListener l){
        if (addListener){
            super.addMouseMotionListener(l);
        }
    }
    
    public void settingsChange(SettingsChangeEvent evt) {
        final boolean visible = isToolBarVisible();
	final JTextComponent c = getComponent();
        final boolean keyBindingsChanged = 
                evt!=null && 
                SettingsNames.KEY_BINDING_LIST.equals(evt.getSettingName()) &&
                c != null
		&& evt.getKitClass() == Utilities.getKitClass(c);
        Runnable r = new Runnable() {
                public void run() {
                    if (visible) {
                        checkPresentersAdded();
                        if (keyBindingsChanged){ //#62487
                            installNoOpActionMappings();
                            int componentCount = getComponentCount();
                            String mimeType = NbEditorUtilities.getMimeType(c);
                            Map keybsMap = getKeyBindingMap(mimeType);
                            Component comps[] = getComponents();
                            for (int i=0; i<comps.length; i++){
                                Component comp = comps[i];
                                if (comp instanceof JButton){
                                    JButton button = (JButton)comp;
                                    Action action = button.getAction();
                                    if (action == null){
                                        continue;
                                    }
                                    String actionName = (String) action.getValue(Action.NAME);
                                    if (actionName == null){
                                        continue;
                                    }
                                    
                                    String tooltipText = button.getToolTipText();
                                    if (tooltipText!=null){
                                        int index = tooltipText.indexOf("("); //NOI18N
                                        if (index > 0){
                                            tooltipText = tooltipText.substring(0, index-1);
                                        }
                                    }
                                    
                                    MultiKeyBinding mkb = (MultiKeyBinding)keybsMap.get(actionName);
                                    if (mkb != null){
                                        button.setToolTipText(tooltipText
                                            + " (" + getMnemonic(mkb) + ")"); // NOI18N
                                    } else {
                                        button.setToolTipText(tooltipText);
                                    }
                                }
                            }
                        }
                    } else {
                        checkPresentersRemoved();
                    }
                    setVisible(visible);
                }
             };
        Utilities.runInEventDispatchThread(r);
    }
    
    private void checkPresentersAdded() {
        if (!presentersAdded) {
            presentersAdded = true;
            addPresenters();
        }
    }
    
    private void checkPresentersRemoved() {
        presentersAdded = false;        
        removeAll();
    }    

    private static boolean isToolBarVisible() {
        return AllOptionsFolder.getDefault().isToolbarVisible();
    }
    
    /** Utility method for getting the mnemonic of the multi key binding.
     * @param binding multi key binding
     * @return mnemonic for the binding.
     */
    private static String getMnemonic(MultiKeyBinding binding) {
        StringBuffer sb = new StringBuffer();
        if (binding.keys != null) { // multiple keys
            for (int i = 0; i < binding.keys.length; i++) {
                if (i > 0) {
                    sb.append(' ');
                }
                sb.append(getKeyMnemonic(binding.keys[i]));
            }

        } else { // multiple keys
            sb.append(getKeyMnemonic(binding.key));
        }
        return sb.toString();
    }

    /**
     * Get the mnemonic for a keystroke.
     * @param key a keystroke
     * @return mnemonic of tghe keystroke.
     */
    private static String getKeyMnemonic(KeyStroke key) {
        String sk = org.openide.util.Utilities.keyToString(key);
        StringBuffer sb = new StringBuffer();
        int mods = key.getModifiers();
        if ((mods & KeyEvent.CTRL_MASK) != 0) {
            sb.append("Ctrl+"); // NOI18N
        }
        if ((mods & KeyEvent.ALT_MASK) != 0) {
            sb.append("Alt+"); // NOI18N
        }
        if ((mods & KeyEvent.SHIFT_MASK) != 0) {
            sb.append("Shift+"); // NOI18N
        }
        if ((mods & KeyEvent.META_MASK) != 0) {
            sb.append("Meta+"); // NOI18N
        }
        
        int i = sk.indexOf('-');
        if (i != -1) {
            sk = sk.substring(i + 1);
        }
        sb.append(sk);
        
        return sb.toString();
    }

    private static Map/*<String, MultiKeyBinding>*/ getKeyBindingMap(String mimeType) {
        Map retMap = new HashMap();
        List keybList = getKeyBindingList(mimeType);
        Iterator it = keybList.iterator();
        while(it.hasNext()){
            Object obj = it.next();
            if (obj instanceof MultiKeyBinding){
                MultiKeyBinding keyb = (MultiKeyBinding)obj;
                retMap.put(keyb.actionName, keyb);
            }
        }
        return retMap;
    }
    
    private static List getKeyBindingList(String mimeType) {
        List keyBindingsList = new ArrayList();

        AllOptionsFolder aof = AllOptionsFolder.getDefault();
        if (aof != null) {
            List gkbl = aof.getKeyBindingList();
            if (gkbl != null) {
                keyBindingsList.addAll(gkbl);
            }
        }

        if (mimeType != null) {
            BaseOptions options = (BaseOptions) MimeLookup.getLookup(MimePath.parse(mimeType)).lookup(BaseOptions.class);
            if (options != null) {
                List kbl = options.getKeyBindingList();
                if (kbl != null) {
                    keyBindingsList.addAll(kbl);
                }
            }
        }
        
        return keyBindingsList;
        
    }
    
    private JTextComponent getComponent() {
	return (JTextComponent)componentRef.get();
    }
    
    /** Add the presenters (usually buttons) for the contents of the toolbar
     * contained in the base and mime folders.
     * @param baseFolder folder that corresponds to "text/base"
     * @param mimeFolder target mime type folder.
     * @param toolbar toolbar being constructed.
     */
    private void addPresenters() {
        JTextComponent c = getComponent();
        String mimeType = c == null ? null : NbEditorUtilities.getMimeType(c);
        
        if (mimeType == null) {
            return; // Probably no component or it's not loaded properly
        }

        List keybindings = null;
        Lookup actionContext = null;
        List items = ToolbarActionsProvider.getToolbarItems(mimeType);
        
        // COMPAT: The ToolbarsActionsProvider treats 'text/base' in a special way. It
        // will list only items registered for this particular mime type, but won't
        // inherit anything else. The 'text/base' is normally empty, but could be
        // used by some legacy code.
        List oldTextBaseItems = ToolbarActionsProvider.getToolbarItems("text/base"); //NOI18N
        if (oldTextBaseItems.size() > 0) {
            items = new ArrayList(items);
            items.add(new JSeparator());
            items.addAll(oldTextBaseItems);
        }
        
        for(Object item : items) {
            if (item instanceof JSeparator) {
                addSeparator();
                continue;
            }
            
            if (item instanceof String) {
                EditorKit kit = c.getUI().getEditorKit(c);
                if (kit instanceof BaseKit) {
                    Action a = ((BaseKit) kit).getActionByName((String) item);
                    if (a != null) {
                        item = a;
                    } else {
                        // unknown action
                        continue;
                    }
                }
            }
            
            if (item instanceof ContextAwareAction) {
                if (actionContext == null) {
                    Lookup context = createActionContext(c);
                    actionContext = context == null ? NO_ACTION_CONTEXT : context;
                }
                
                if (actionContext != NO_ACTION_CONTEXT) {
                    Action caa = ((ContextAwareAction) item).createContextAwareInstance(actionContext);
                    
                    // use the context aware instance only if it implements Presenter.Toolbar
                    // or is a Component else fall back to the original object
                    if (caa instanceof Presenter.Toolbar || caa instanceof Component) {
                        item = caa;
                    }
                }
            }
            
            if (item instanceof Presenter.Toolbar) {
                Component presenter = ((Presenter.Toolbar) item).getToolbarPresenter();
                if (presenter != null) {
                    item = presenter;
                }
            }
            
            if (item instanceof Component) {
                add((Component)item);
            } else if (item instanceof Action) {
                // Wrap action to execute on the proper text component
                // because the default fallback in TextAction.getTextComponent()
                // might not work properly if the focus was switched
                // to e.g. a JTextField and then toolbar was clicked.
                Action a = new WrapperAction(componentRef, (Action) item);
                
                // Try to find an icon if not present
                updateIcon(a);

                // Add the action and let the JToolbar to creat a presenter for it
                item = add(a);
            } else {
                // Some sort of crappy item -> ignore
                continue;
            }

            if (item instanceof AbstractButton) {
                AbstractButton button = (AbstractButton)item;
                processButton(button);
                
                if (keybindings == null) {
                    List l = getKeyBindingList(mimeType);
                    keybindings = l == null ? Collections.emptyList() : l;
                }
                updateTooltip(button, keybindings);
            }
        }
    }
    
    // XXX: this is actually wierd, because it changes the action's properties
    // perhaps we should just update the presenter, but should not touch the
    // action itself
    private static void updateIcon(Action a) {
        Object icon = a.getValue(Action.SMALL_ICON);
        if (icon == null) {
            String resourceId = (String)a.getValue(BaseAction.ICON_RESOURCE_PROPERTY);
            if (resourceId == null) { // use default icon
                resourceId = "org/netbeans/modules/editor/resources/default.gif"; // NOI18N
            }
            Image img = org.openide.util.Utilities.loadImage(resourceId);
            if (img != null) {
                a.putValue(Action.SMALL_ICON, new ImageIcon(img));
            }
        }
    }

    private static void updateTooltip(AbstractButton b, List keybindings) {
        Action a = b.getAction();
        String actionName = a == null ? null : (String) a.getValue(Action.NAME);
        
        if (actionName == null) {
            // perhaps no action at all
            return;
        }
        
        for (Iterator kbIt = keybindings.iterator(); kbIt.hasNext();) {
            Object o = kbIt.next();
            if (o instanceof MultiKeyBinding) {
                MultiKeyBinding binding = (MultiKeyBinding)o;
                if (actionName.equals(binding.actionName)) {
                    b.setToolTipText(b.getToolTipText() + " (" + getMnemonic(binding) + ")"); // NOI18N
                    break; // multiple shortcuts ?
                }
            }
        }
    }
    
    /**
     * Not private because of the tests.
     */
    static Lookup createActionContext(JTextComponent c) {
        Lookup nodeLookup = null;
        DataObject dobj = (c != null) ? NbEditorUtilities.getDataObject(c.getDocument()) : null;
        if (dobj != null && dobj.isValid()) {
            nodeLookup = dobj.getNodeDelegate().getLookup();
        }

        Lookup ancestorLookup = null;
        for (java.awt.Component comp = c; comp != null; comp = comp.getParent()) {
            if (comp instanceof Lookup.Provider) {
                Lookup lookup = ((Lookup.Provider)comp).getLookup ();
                if (lookup != null) {
                    ancestorLookup = lookup;
                    break;
                }
            }
        }

        if (nodeLookup == null) {
            return ancestorLookup;
        } else if (ancestorLookup == null) {
            return nodeLookup;
        }
        assert nodeLookup != null && ancestorLookup != null;

        Node node = (Node)nodeLookup.lookup(Node.class);
        boolean ancestorLookupContainsNode = ancestorLookup.lookup(
                new Lookup.Template(Node.class)).allInstances().contains(node);

        if (ancestorLookupContainsNode) {
            return ancestorLookup;
        } else {
            return new ProxyLookup(new Lookup[] { nodeLookup, ancestorLookup });
        }
    }

    private void processButton(AbstractButton button) {
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setMargin(BUTTON_INSETS);
        if (button instanceof AbstractButton) {
            button.addMouseListener(sharedMouseListener);
        }
        //fix of issue #69642. Focus shouldn't stay in toolbar
        button.setFocusable(false);
    }

    /** Attempt to find the editor keystroke for the given action. */
    private KeyStroke[] findEditorKeys(String editorActionName, KeyStroke defaultKey) {
        KeyStroke[] ret = new KeyStroke[] { defaultKey };
        JTextComponent comp = getComponent();
        if (editorActionName != null && comp != null) {
            TextUI ui = comp.getUI();
            Keymap km = comp.getKeymap();
            if (ui != null && km != null) {
                EditorKit kit = ui.getEditorKit(comp);
                if (kit instanceof BaseKit) {
                    Action a = ((BaseKit)kit).getActionByName(editorActionName);
                    if (a != null) {
                        KeyStroke[] keys = km.getKeyStrokesForAction(a);
                        if (keys != null && keys.length > 0) {
                            ret = keys;
                        } else {
                            // try kit's keymap
                            Keymap km2 = ((BaseKit)kit).getKeymap();
                            KeyStroke[] keys2 = km2.getKeyStrokesForAction(a);
                            if (keys2 != null && keys2.length > 0) {
                                ret = keys2;
                            }                            
                        }
                    }
                }
            }
        }
        return ret;
    }
    
    /** No operation action  - do nothing when invoked 
     *  issue #69642
     */
    private static final class NoOpAction extends AbstractAction{
        public NoOpAction(){
        }
        public void actionPerformed(ActionEvent e) {
        }
    } // End of NoOpAction class
    
    private static final class WrapperAction implements Action {
        
        private final Reference componentRef;
        
        private final Action delegate;
        
        WrapperAction(Reference componentRef, Action delegate) {
            this.componentRef = componentRef;
            assert (delegate != null);
            this.delegate = delegate;
        }
        
        public Object getValue(String key) {
            return delegate.getValue(key);
        }

        public void putValue(String key, Object value) {
            delegate.putValue(key, value);
        }

        public void setEnabled(boolean b) {
            delegate.setEnabled(b);
        }

        public boolean isEnabled() {
            return delegate.isEnabled();
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            delegate.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            delegate.removePropertyChangeListener(listener);
        }

        public void actionPerformed(ActionEvent e) {
            JTextComponent c = (JTextComponent)componentRef.get();
            if (c != null) { // Override action event to text component
                e = new ActionEvent(c, e.getID(), e.getActionCommand());
            }
            delegate.actionPerformed(e);
        }
    } // End of WrapperAction class
}
