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
import java.io.IOException;
import java.util.ArrayList;
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
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
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

    /** Runnable for returning the focus back to the last active text component. */
    private static final Runnable returnFocusRunnable
        = new Runnable() {
            public void run() {
                Component c = BaseTextUI.getFocusedComponent();
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
        List names = new ArrayList(); // list of its names
        List sortPairs = new ArrayList(); // each member of every pair added to the list sequentially

        if (mimeFolder != null) {
            addDataObjects(name2dob, names, mimeFolder.getChildren());
        }
        if (baseFolder != null) {
            addDataObjects(name2dob, names, baseFolder.getChildren());
        }
        
        if (mimeFolder != null) {
            addSortPairs(mimeFolder, sortPairs);
        }
        if (baseFolder != null) {
            addSortPairs(baseFolder, sortPairs);
        }

        SortSupport sortSupport = new SortSupport();
        for (Iterator it = sortPairs.iterator(); it.hasNext();) {
            Object firstName = it.next();
            Object secondName = it.next(); // would throw exc. which is OK - shouldn't happen
            if (name2dob.containsKey(firstName) && name2dob.containsKey(secondName)) {
                sortSupport.addEdge(firstName, secondName);
            } else { // edge not added
                if (debugSort) {
                    System.out.println("Edge not added: " // NOI18N
                        + firstName + " -> " + name2dob.containsKey(firstName) // NOI18N
                        + ", " + secondName + " -> " + name2dob.containsKey(secondName) // NOI18N
                    );
                }
            }
        }

        if (debugSort) {
            System.out.println("Names: " + names + "\nsortSupport:" + sortSupport); // NOI18N
        }

        List standalones = sortSupport.eliminateMultipleStarts(names, true);
        if (debugSort) {
            System.out.println("Eliminated multiple starts: " + sortSupport // NOI18N
                + "\nStandalones: " + standalones); // NOI18N
        }

        List sortedNameList = sortSupport.createSortedList();
        if (debugSort) {
            System.out.println("Sorted Name List: " + sortedNameList); // NOI18N
        }

        sortedNameList.addAll(standalones);

        int sortedNameListSize = sortedNameList.size();
        List dobList = new ArrayList(sortedNameListSize);
        for (int i = 0; i < sortedNameListSize; i++) {
            dobList.add(name2dob.get(sortedNameList.get(i)));
        }
        
        if (dobList.indexOf(null) != -1) {
            throw new IllegalStateException();
        }
        
        return dobList;
    }
    
    /** Append array of dataobjects to the existing list of dataobjects. Also
     * append the names of added dataobjects so that the both dobs and names
     * list are in the same order.
     * @param dobs valid existing list of dataobjects
     * @param names valid existing list of names of the dataobjects from dobs list.
     * @param addDobs dataobjects to be added.
     */
    private static void addDataObjects(Map name2dob, List names, DataObject[] addDobs) {
        int addDobsLength = addDobs.length;
        List addNamesList = new ArrayList(addDobsLength);

        for (int i = 0; i < addDobsLength; i++) {
            DataObject dob = addDobs[i];
            String dobName = dob.getPrimaryFile().getNameExt();
            if (names.indexOf(dobName) == -1 && addNamesList.indexOf(dobName) == -1) {
                name2dob.put(dobName, dob);
                addNamesList.add(dobName);
            }
        }

        // Insert at the begining of existing names
        names.addAll(0, addNamesList);
    }

    /** Append the pairs - first dob name then second dob name to the list
     * of sort pairs for the dataobjects from the given folder.
     */
    private static void addSortPairs(DataFolder toolbarFolder, List sortPairs) {
        FileObject primaryFile = toolbarFolder.getPrimaryFile();
        for (Enumeration e = primaryFile.getAttributes();
            e.hasMoreElements();
        ) {
            String name = (String)e.nextElement();
            int slashIndex = name.indexOf("/"); // NOI18N
            if (slashIndex != -1) { //NOI18N
                Object value = primaryFile.getAttribute(name);
                if ((value instanceof Boolean) && ((Boolean) value).booleanValue()){
                    sortPairs.add(name.substring(0, slashIndex));
                    sortPairs.add(name.substring(slashIndex + 1));
                }
            }
        }
    }
    
    /** Support for topological sort of the oriented acyclic graph.
     * No synchronization.
     */
    private static final class SortSupport {
       
        /** Map of [vertex, edge-end] pairs. */
        private Map vert2edges;
        
        /** Set of ends of edges. Vertices that are not members
         * of this set are not ends of any edge. They
         * can still have one or more edges starting from them.
         */
        private Set edgesEnds;
        
        /** Visited vertices in the modified DFS algorithm. */
        private Set mdfsVisited;
        
        /** Finished vertices in the modified DFS algorithm. */
        private List mdfsFinished;
        
        SortSupport() {
            vert2edges = new HashMap();
            edgesEnds = new HashSet();
            mdfsVisited = new HashSet();
            mdfsFinished = new ArrayList();
        }
        
        /** Add a new edge from the start vertex into end vertex.
         */
        void addEdge(Object start, Object end) {
            if (start == null || end == null) {
                throw new IllegalStateException();
            }
            
            if (start.equals(end)) {
                return; // edge to itself - do not add
            }
            
            edgesEnds.add(end);
            List verts = (List)vert2edges.get(start);
            boolean addedNewList = false;
            if (verts == null) {
                verts = new ArrayList(2);
                vert2edges.put(start, verts);
                addedNewList = true;
            }
            if (verts.indexOf(end) == -1) { // not yet added
                verts.add(end);
                if (debugSort) {
                    System.out.println("added edge " + start + " -> " + end); // NOI18N
                }
                if (mdfs(start)) { // cycle created by added edge
                    verts.remove(verts.size() - 1);
                    if (addedNewList) {
                        vert2edges.remove(start);
                    }
                    if (debugSort) {
                        System.out.println("REMOVED edge " + start + " -> " + end); // NOI18N
                    }
                }
            }
        }
        
        /** Eliminate multiple starts (vertices not present in edgesEnds set)
         * by walking through the given list and deriving the order
         * of the vertices from the given list. The missing edges are added
         * so that there is just one starting vertex after this method
         * completes.
         * @param vertList complete list of vertices.
         * @param omitStandalones If true the standalone vertices that do not
         *  participate in any edge will be returned in the list. If false
         *  there will be an artificial edge made like for other start vertices.
         * @return valid list of the standalone vertices or null
            if the omitStandalones parameter was false.
         */
        List eliminateMultipleStarts(List vertList, boolean omitStandalones) {
            Object first = null;
            Object lastStart = null;
            List standalones = omitStandalones ? new ArrayList() : null;
            int vertListSize = vertList.size();
            for (int i = 0; i < vertListSize; i++) {
                Object start = vertList.get(i);
                if (!edgesEnds.contains(start)) { // not end of any edge
                    if (omitStandalones && !vert2edges.containsKey(start)) {
                        standalones.add(start);
                    } else { // not standalone or standalones not maintained
                        if (first == null) {
                            first = start;
                            lastStart = start;

                        } else { // some vert already first
                            addEdge(lastStart, start); // add extra edge
                        }
                    }
                }
            }
            
            return standalones;
        }
                        
        /** Create list (sorted topologically) derived from the information
         * about the edges. It's necessary to have just one starting vertex
         * so it may be necessary to call eliminateMultipleStarts() first.
         */
        List createSortedList() {
            Set allVerts = new HashSet(vert2edges.keySet());
            allVerts.removeAll(edgesEnds);
            Iterator it = allVerts.iterator();
            if (!it.hasNext()) {
                throw new IllegalStateException("No first item"); // NOI18N
            }
            Object first = it.next();
            if (it.hasNext()) { // [PENDING] 
                throw new IllegalStateException("More than one start item"); // NOI18N
            }
            
            mdfs(first);
            
            
            Collections.reverse(mdfsFinished);

            return mdfsFinished;
        }

        private boolean mdfs(Object v) {
            //mdfsOpCount = 0;
            mdfsVisited.clear();
            mdfsFinished.clear();
            return mdfsRec(v);
            //boolean ret = mdfsRec(v); System.out.println("MDFSOpCount=" + mdfsOpCount); return ret;
        }

        //private int mdfsOpCount; // counter used for debugging the mdfs op count
        
        /** Modified Depth First Search algorithm.
         * @param v vertex to be processed by the algorithm.
         */
        private boolean mdfsRec(Object v) {
            //mdfsOpCount++;
            if (mdfsVisited.contains(v)) {
                return true; // cycle found
            }
            
            mdfsVisited.add(v);
            List verts = (List)vert2edges.get(v);
            if (verts != null) {
                int vertsSize = verts.size();
                for (int i = 0; i < vertsSize; i++) {
                    if (mdfsRec(verts.get(i))) {
                        return true; // stop due to cycle found
                    }
                }
                
            }
            mdfsFinished.add(v);
            return false; // not yet found
        }

        public String toString() {
            return "edgesEnds=" + edgesEnds // NOI18N
                + "\n\n vert2edges=" + vert2edges; // NOI18N
        }

    }

}
