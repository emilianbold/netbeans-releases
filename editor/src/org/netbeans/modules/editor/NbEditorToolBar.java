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

import java.awt.Component;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractButton;
import javax.swing.Action;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.ToolBarUI;
import javax.swing.text.EditorKit;
import org.netbeans.editor.BaseAction;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.BaseTextUI;
import org.netbeans.editor.LocaleSupport;
import org.netbeans.editor.MultiKeyBinding;
import org.netbeans.editor.Settings;
import org.netbeans.editor.SettingsChangeEvent;
import org.netbeans.editor.SettingsChangeListener;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.editor.options.AllOptionsFolder;
import org.netbeans.modules.editor.options.BaseOptions;
import org.openide.ErrorManager;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.TopologicalSortException;
import org.openide.util.actions.Presenter;

/**
 * Editor toolbar component.
 * <br>
 * Toolbar contents are obtained by merging of
 * Editors/mime-type/Toolbars/Default
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

final class NbEditorToolBar extends JToolBar implements SettingsChangeListener {
    
    /** Flag for testing the support by debugging messages. */
    private static final boolean debug
        = Boolean.getBoolean("netbeans.debug.editor.toolbar"); // NOI18N
    
    /** Flag for testing the sorting support by debugging messages. */
    private static final boolean debugSort
        = Boolean.getBoolean("netbeans.debug.editor.toolbar.sort"); // NOI18N

    /** Name of the main folder where the particular toolbars' folders
     * are located.
     */
    private static final String TOOLBARS_FOLDER_NAME = "Toolbars"; // NOI18N
    
    /** Name of the folder for the default toolbar. */
    private static final String DEFAULT_TOOLBAR_NAME = "Default"; // NOI18N
    
    static final String BASE_MIME_TYPE = "text/base"; // NOI18N
    
    private static final Insets BUTTON_INSETS = new Insets(2, 1, 0, 1);
    
    FileChangeListener moduleRegListener;

    /** Runnable for returning the focus back to the last active text component. */
    private static final Runnable returnFocusRunnable
        = new Runnable() {
            public void run() {
                Component c = Utilities.getLastActiveComponent();
                if (c != null) {
                    c.requestFocus();
                }
            }
        };
       
    private static final ActionListener sharedActionListener
        = new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                SwingUtilities.invokeLater(returnFocusRunnable);
            }
        };

    /** Shared mouse listener used for setting the border painting property
     * of the toolbar buttons and for invoking the popup menu.
     */
    private static final MouseListener sharedMouseListener
        = new org.openide.awt.MouseUtils.PopupMouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                if (evt.getSource() instanceof JButton) {
                    JButton button = (JButton)evt.getSource();
                    if (button.isEnabled()){
                        button.setContentAreaFilled(true);
                        button.setBorderPainted(true);
                    }
                }
            }
            
            public void mouseExited(MouseEvent evt) {
                if (evt.getSource() instanceof JButton) {
                    JButton button = (JButton)evt.getSource();
                    button.setContentAreaFilled(false);
                    button.setBorderPainted(false);
                }
            }
            
            protected void showPopup(MouseEvent evt) {
            }
        };
       

    
    /** Editor kit to which this support belongs */
    private NbEditorUI editorUI;
    
    private boolean presentersAdded;

    private boolean addListener = true;
    
   
    NbEditorToolBar(NbEditorUI editorUI) {
        this.editorUI = editorUI;
        
        setFloatable(false);
        //mkleint - instead of here, assign the border in CloneableEditor and MultiView module.
//        // special border installed by core or no border if not available
//        Border b = (Border)UIManager.get("Nb.Editor.Toolbar.border"); //NOI18N
//        setBorder(b);
        addMouseListener(sharedMouseListener);
        Settings.addSettingsChangeListener(this);
        settingsChange(null);

        installModulesInstallationListener();
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
        Runnable r = new Runnable() {
                public void run() {
                    if (visible) {
                        checkPresentersAdded();
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
            DataFolder baseFolder = getToolBarFolder(BASE_MIME_TYPE, false);
            DataFolder mimeFolder = getToolBarFolder(getMimeType(), false);
            
            addPresenters(baseFolder, mimeFolder);
        }
    }
    
    private void checkPresentersRemoved() {
        presentersAdded = false;        
        removeAll();
    }    

    /** Get the target toolbar folder for the given mimetype.
     * @param type mime type of the requested toolbar folder
     * or "text/base" for the global folder.
     */
    private static DataFolder getToolBarFolder(String type, boolean forceCreate) {
        String toolbarFolderPath = "Editors/" + type + "/" // NOI18N
            + TOOLBARS_FOLDER_NAME + "/" + DEFAULT_TOOLBAR_NAME; // NOI18N

        DataFolder toolbarFolder = null;
        FileObject f = Repository.getDefault().
            getDefaultFileSystem().findResource(toolbarFolderPath);

        if (f != null) {
            try {
                DataObject dob = DataObject.find(f);
                toolbarFolder = (DataFolder)dob.getCookie(DataFolder.class);
            } catch (DataObjectNotFoundException e) {
                // DataObject for the toolbar folder not found
            }
            
        } else if (forceCreate) { // does not exist yet
            try {
                FileUtil.createFolder(
                    Repository.getDefault().getDefaultFileSystem().getRoot(),
                    toolbarFolderPath
                );
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.EXCEPTION, e);
            }
                
        }
        
        return toolbarFolder;
    }
    
    private static boolean isToolBarVisible() {
        return AllOptionsFolder.getDefault().isToolbarVisible();
    }
    
    private String getMimeType() {
        EditorKit kit = Utilities.getKit(editorUI.getComponent());
        String mimeType = (kit != null) ? kit.getContentType() : null;
        return mimeType;
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
    
    /** Add the presenters (usually buttons) for the contents of the toolbar
     * contained in the base and mime folders.
     * @param baseFolder folder that corresponds to "text/base"
     * @param mimeFolder target mime type folder.
     * @param toolbar toolbar being constructed.
     */
    private void addPresenters(DataFolder baseFolder, DataFolder mimeFolder) {
        List keyBindingsList = new ArrayList();
        
        AllOptionsFolder aof = AllOptionsFolder.getDefault();
        if (aof != null) {
            List gkbl = aof.getKeyBindingList();
            if (gkbl != null) {
                keyBindingsList.addAll(gkbl);
            }
        }

        EditorKit kit = Utilities.getKit(editorUI.getComponent());
        if (kit instanceof BaseKit) {
            BaseKit baseKit = (BaseKit)kit;
            BaseOptions options = BaseOptions.getOptions(kit.getClass());
            if (options != null) {
                List kbl = options.getKeyBindingList();
                if (kbl != null) {
                    keyBindingsList.addAll(kbl);
                }
            }

            for (Iterator it = getToolbarObjects(baseFolder, mimeFolder).iterator();
                it.hasNext();
            ) {

                DataObject dob = (DataObject)it.next();
                InstanceCookie ic = (InstanceCookie)dob.getCookie(InstanceCookie.class);
                if (ic != null){
                    try {
                        if(JSeparator.class.isAssignableFrom(ic.instanceClass())){
                            addSeparator();

                        } else { // attempt to instantiate the cookie
                            Object obj = ic.instanceCreate();
                            if (obj instanceof Presenter.Toolbar) {
                                Component tbp = ((Presenter.Toolbar)obj).getToolbarPresenter();
                                add(tbp);
                                if (tbp instanceof AbstractButton) {
                                    processButton((AbstractButton)tbp);
                                }

                            } else if (obj instanceof Component) {
                                add((Component)obj);
                                if (obj instanceof AbstractButton) {
                                    processButton((AbstractButton)obj);
                                }
                            }
                        }

                    } catch (IOException e) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                    } catch (ClassNotFoundException e) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                    }

                } else { // no instance cookie present
                    // Attempt to find action with the name of dob
                    String actionName = dob.getName();
                    Action a = baseKit.getActionByName(actionName);
                    if (a != null) {
                        // Try to find an icon if not present
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
                        
                        JButton button = add(a);
                        
                        // Possibly find mnemonic
                        for (Iterator kbIt = keyBindingsList.iterator(); kbIt.hasNext();) {
                            Object o = kbIt.next();
                            if (o instanceof MultiKeyBinding) {
                                MultiKeyBinding binding = (MultiKeyBinding)o;
                                if (actionName.equals(binding.actionName)) {
                                    button.setToolTipText(button.getToolTipText()
                                        + " (" + getMnemonic(binding) + ")"); // NOI18N
                                    break; // multiple shortcuts ?
                                }
                            }
                        }
                        
                        processButton(button);
                    }
                }
            }
        }
    }

    private void processButton(AbstractButton button) {
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.addActionListener(sharedActionListener);
        button.setMargin(BUTTON_INSETS);
        if (button instanceof JButton) {
            button.addMouseListener(sharedMouseListener);
        }
    }

    /** Merge together the base and mime folders.
     * @param baseFolder folder that corresponds to "text/base"
     * @param mimeFolder target mime type folder.
     * @return list of data objects resulted from merging.
     */
    static List getToolbarObjects(DataFolder baseFolder, DataFolder mimeFolder) {
        Map name2dob = new HashMap();
        Map edges = new HashMap();

        if (baseFolder != null) {
            addDataObjects(name2dob, baseFolder.getChildren());
        }
        if (mimeFolder != null) {
            addDataObjects(name2dob, mimeFolder.getChildren());
        }
        
        if (baseFolder != null) {
            addEdges(edges, name2dob, baseFolder);
        }
        if (mimeFolder != null) {
            addEdges(edges, name2dob, mimeFolder);
        }

        try {
            return org.openide.util.Utilities.topologicalSort(name2dob.values(), edges);
        } catch (TopologicalSortException ex) {
            ErrorManager.getDefault().notify(ex);
            return ex.partialSort();
        }
    }
    
    /** Append array of dataobjects to the existing list of dataobjects. Also
     * append the names of added dataobjects so that the both dobs and names
     * list are in the same order.
     * @param dobs valid existing list of dataobjects
     * @param names valid existing list of names of the dataobjects from dobs list.
     * @param addDobs dataobjects to be added.
     */
    private static void addDataObjects(Map name2dob, DataObject[] addDobs) {
        int addDobsLength = addDobs.length;
        for (int i = 0; i < addDobsLength; i++) {
            DataObject dob = addDobs[i];
            String dobName = dob.getPrimaryFile().getNameExt();
            name2dob.put(dobName, dob);
        }
    }

    /** Append the pairs - first dob name then second dob name to the list
     * of sort pairs for the dataobjects from the given folder.
     */
    private static void addEdges(Map edges, Map name2dob, DataFolder toolbarFolder) {
        FileObject primaryFile = toolbarFolder.getPrimaryFile();
        for (Enumeration e = primaryFile.getAttributes();
            e.hasMoreElements();
        ) {
            String name = (String)e.nextElement();
            int slashIndex = name.indexOf("/"); // NOI18N
            if (slashIndex != -1) { //NOI18N
                Object value = primaryFile.getAttribute(name);
                if ((value instanceof Boolean) && ((Boolean) value).booleanValue()){
                    String name1 = name.substring(0, slashIndex);
                    String name2 = name.substring(slashIndex + 1);
                    if (debugSort) {
                        System.err.println("SORT-PAIR: [" + name1 + ", " + name2 + "]"); // NOI18N
                    }
                    
                    DataObject dob = (DataObject)name2dob.get(name1);
                    DataObject target = (DataObject)name2dob.get(name2);
                    if (dob != null && target != null) {
                        Collection targetVertices = (Collection)edges.get(dob);
                        if (targetVertices == null) { // none target vertices yet
                            // Use just singleton list to save space
                            targetVertices = Collections.singletonList(target);
                            edges.put(dob, targetVertices);

                        } else if (targetVertices.size() == 1) { // singleton list
                            targetVertices = new HashSet(targetVertices);
                            targetVertices.add(target);
                            edges.put(dob, targetVertices);

                        } else {
                            targetVertices.add(target);
                        }
                    }
                }
            }
        }
    }
    
}
