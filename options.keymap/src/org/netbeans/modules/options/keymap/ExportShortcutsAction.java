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


package org.netbeans.modules.options.keymap;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import org.netbeans.core.options.keymap.api.ShortcutAction;
import org.netbeans.core.options.keymap.spi.KeymapManager;
import org.netbeans.modules.options.keymap.KeymapModel;
import org.netbeans.modules.options.keymap.XMLStorage.Attribs;
import org.openide.ErrorManager;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

public class ExportShortcutsAction {
    
    private static Action exportIDEActionsAction = new AbstractAction () {
        {putValue (Action.NAME, loc ("CTL_Export_IDE_Actions_Action"));}
        
        public void actionPerformed (ActionEvent e) {

            // 1) load all keymaps to allKeyMaps
            LayersBridge layersBridge = new LayersBridge ();
            Map categoryToActions = layersBridge.getActions ();
            Map m = resolveNames (categoryToActions);

            generateLayersXML (layersBridge, m);
        }
    };
    
    public static Action getExportIDEActionsAction () {
        return exportIDEActionsAction;
    }
    
    private static Action exportIDEShortcutsAction = new AbstractAction () {
        {putValue (Action.NAME, loc ("CTL_Export_IDE_Shortcuts_Action"));}
        
        public void actionPerformed (ActionEvent e) {

            // 1) load all keymaps to allKeyMaps
            Map allKeyMaps = new HashMap ();
            LayersBridge layersBridge = new LayersBridge ();
            layersBridge.getActions ();
            List keyMaps = layersBridge.getProfiles ();
            Iterator it3 = keyMaps.iterator ();
            while (it3.hasNext ()) {
                String keyMapName = (String) it3.next ();
                Map actionToShortcuts = layersBridge.getKeymap (keyMapName);
                Map shortcutToAction = LayersBridge.shortcutToAction (actionToShortcuts);
                allKeyMaps.put (keyMapName, shortcutToAction);
            }

            generateLayersXML (layersBridge, allKeyMaps);
        }
    };
    
    public static Action getExportIDEShortcutsAction () {
        return exportIDEShortcutsAction;
    }
    
    private static Action exportEditorShortcutsAction = new AbstractAction () {
        {putValue (Action.NAME, loc ("CTL_Export_Editor_Shortcuts_Action"));}
        
        public void actionPerformed (ActionEvent e) {
            KeymapManager editorBridge = null;
            for (KeymapManager km : KeymapModel.getKeymapManagerInstances()) {
                if ("EditorBridge".equals(km.getName())) {
                    editorBridge = km;
                    break;
                }
            }
            if (editorBridge != null) {
                Map actionToShortcuts = editorBridge.getKeymap(editorBridge.getCurrentProfile ());
                generateEditorXML (actionToShortcuts);
            }
        }
    };
    
    public static Action getExportEditorShortcutsAction () {
        return exportEditorShortcutsAction;
    }
    
    private static Action exportShortcutsToHTMLAction = new AbstractAction () {
        {putValue (Action.NAME, loc ("CTL_Export_Shortcuts_to_HTML_Action"));}
        
        public void actionPerformed (ActionEvent e) {
            exportShortcutsToHTML ();
        }
    };
    
    public static Action getExportShortcutsToHTMLAction () {
        return exportShortcutsToHTMLAction;
    }

    
    // helper methods ..........................................................
    
    private static void exportShortcutsToHTML () {
        // read all shortcuts to keymaps
        KeymapModel keymapModel = new KeymapModel ();
        Map keymaps = new TreeMap ();
        Iterator it = keymapModel.getProfiles ().iterator ();
        while (it.hasNext ()) {
            String profile = (String) it.next ();
            keymaps.put (
                profile,
                keymapModel.getKeymap (profile)
            );
        }
        
        try {
            StringBuffer sb = new StringBuffer ();

            Attribs attribs = new Attribs (true);
            XMLStorage.generateFolderStart (sb, "html", attribs, "");
            XMLStorage.generateFolderStart (sb, "body", attribs, "  ");
            attribs.add ("border", "1");
            attribs.add ("cellpadding", "1");
            attribs.add ("cellspacing", "0");
            XMLStorage.generateFolderStart (sb, "table", attribs, "    ");
            attribs = new Attribs (true);

            // print header of table
            XMLStorage.generateFolderStart (sb, "tr", attribs, "      ");
            XMLStorage.generateFolderStart (sb, "td", attribs, "        ");
            XMLStorage.generateFolderStart (sb, "h2", attribs, "        ");
            sb.append ("Action Name");
            XMLStorage.generateFolderEnd (sb, "h2", "        ");
            XMLStorage.generateFolderEnd (sb, "td", "        ");
            it = keymaps.keySet ().iterator ();
            while (it.hasNext ()) {
                String profile = (String) it.next ();
                XMLStorage.generateFolderStart (sb, "td", attribs, "        ");
                XMLStorage.generateFolderStart (sb, "h2", attribs, "        ");
                sb.append (profile);
                XMLStorage.generateFolderEnd (sb, "h2", "        ");
                XMLStorage.generateFolderEnd (sb, "td", "        ");
            }
            
            // print body of table
            exportShortcutsToHTML2 (keymapModel, sb, keymaps);
            
            XMLStorage.generateFolderEnd (sb, "table", "    ");
            XMLStorage.generateFolderEnd (sb, "body", "  ");
            XMLStorage.generateFolderEnd (sb, "html", "");
            
            FileObject fo = FileUtil.createData (
                Repository.getDefault ().getDefaultFileSystem ().getRoot (),
                "shortcuts.html"
            );
            FileLock fileLock = fo.lock ();
            try {
                OutputStream outputStream = fo.getOutputStream (fileLock);
                OutputStreamWriter writer = new OutputStreamWriter (outputStream);
                writer.write (sb.toString ());
                writer.close ();
            } catch (IOException ex) {
                ErrorManager.getDefault ().notify (ex);
            } finally {
                fileLock.releaseLock ();
            }
        } catch (IOException ex) {
            ErrorManager.getDefault ().notify (ex);
        }
    }

    /**
     * Writes body of shortcuts table to given StringBuffer.
     */
    private static void exportShortcutsToHTML2 (
        KeymapModel keymapModel, 
        StringBuffer sb,
        Map keymaps
    ) {
        List categories = new ArrayList (keymapModel.getActionCategories ());
        Collections.sort (categories);
        Attribs attribs = new Attribs (true);
        Iterator it = categories.iterator ();
        while (it.hasNext ()) {
            String category = (String) it.next ();
            
            // print category title
            XMLStorage.generateFolderStart (sb, "tr", attribs, "      ");
            attribs.add ("colspan", Integer.toString (keymaps.size () + 1));
            attribs.add ("rowspan", "1");
            XMLStorage.generateFolderStart (sb, "td", attribs, "        ");
            attribs = new Attribs (true);
            XMLStorage.generateFolderStart (sb, "h3", attribs, "        ");
            sb.append (category);
            XMLStorage.generateFolderEnd (sb, "h3", "        ");
            XMLStorage.generateFolderEnd (sb, "td", "        ");
            XMLStorage.generateFolderEnd (sb, "tr", "      ");
            
            // print body of one category
            exportShortcutsToHTML3 (sb, keymapModel, category, keymaps);
        }
    }

    /**
     * Writes body of given category.
     */
    private static void exportShortcutsToHTML3 (
        StringBuffer sb, 
        KeymapModel keymapModel, 
        String category,
        Map keymaps
    ) {
        Set actions = keymapModel.getActions (category);

        // sort actions
        Map sortedActions = new TreeMap ();
        Iterator it = actions.iterator ();
        while (it.hasNext ()) {
            ShortcutAction action = (ShortcutAction) it.next ();
            sortedActions.put (
                action.getDisplayName (), 
                action
            );
        }

        // print actions
        Attribs attribs = new Attribs (true);
        it = sortedActions.keySet ().iterator ();
        while (it.hasNext ()) {
            String actionName = (String) it.next ();
            ShortcutAction action = (ShortcutAction) sortedActions.get (actionName);

            // print action name to the first column
            XMLStorage.generateFolderStart (sb, "tr", attribs, "      ");
            XMLStorage.generateFolderStart (sb, "td", attribs, "        ");
            sb.append (actionName);
            XMLStorage.generateFolderEnd (sb, "td", "        ");
            
            Iterator it2 = keymaps.keySet ().iterator ();
            while (it2.hasNext ()) {
                String profile = (String) it2.next ();
                Map keymap = (Map) keymaps.get (profile);
                Set shortcuts = (Set) keymap.get (action);

                XMLStorage.generateFolderStart (sb, "td", attribs, "        ");
                printShortcuts (shortcuts, sb);
                XMLStorage.generateFolderEnd (sb, "td", "        ");
            }
            
            XMLStorage.generateFolderEnd (sb, "tr", "      ");
        }
    }
    
    private static void printShortcuts (Set shortcuts, StringBuffer sb) {
        if (shortcuts == null) {
            sb.append ('-');
            return;
        }
        Iterator it = shortcuts.iterator ();
        while (it.hasNext ()) {
            String shortcut = (String) it.next ();
            sb.append (shortcut);
            if (it.hasNext ()) sb.append (", ");
        }
    }
    
    private static void generateLayersXML (
        LayersBridge layersBridge, 
        Map categoryToActions
    ) {
        Writer fw = null;
        try {
            fw = openWriter ();
            if (fw == null) return;

            StringBuffer sb = XMLStorage.generateHeader ();
            Attribs attribs = new Attribs (true);
            XMLStorage.generateFolderStart (sb, "filesystem", attribs, "");
            attribs.add ("name", "Keymaps");
                XMLStorage.generateFolderStart (sb, "folder", attribs, "    ");
                    generateShadowsToXML (layersBridge, sb, categoryToActions, "        ");
                XMLStorage.generateFolderEnd (sb, "folder", "    ");
            XMLStorage.generateFolderEnd (sb, "filesystem", "");
            System.out.println(sb.toString ());
            fw.write (sb.toString ());
        } catch (IOException e) {
            ErrorManager.getDefault ().notify (e);
        } finally {
            try {
                if (fw != null) {
                    fw.flush ();
                    fw.close ();
                }
            } catch (IOException e) {}
        }
    }
    
    private static void generateEditorXML (
        Map actionToShortcuts
    ) {
        Writer fw = null;
        try {
            fw = openWriter ();
            if (fw == null) return;

            StringBuffer sb = XMLStorage.generateHeader ();
            Attribs attribs = new Attribs (true);
            XMLStorage.generateFolderStart (sb, "bindings", attribs, "");
            
            Map sortedMap = new TreeMap ();
            Iterator it = actionToShortcuts.keySet ().iterator ();
            while (it.hasNext ()) {
                ShortcutAction action = (ShortcutAction) it.next ();
                sortedMap.put (
                    action.getDisplayName (), 
                    actionToShortcuts.get (action)
                );
            }
            it = sortedMap.keySet ().iterator ();
            while (it.hasNext ()) {
                String actionName = (String) it.next ();
                Set shortcuts = (Set) sortedMap.get (actionName);
                Iterator it2 = shortcuts.iterator ();
                while (it2.hasNext ()) {
                    String shortcut = (String) it2.next ();
                    attribs = new Attribs (true);
                    attribs.add ("actionName", actionName);
                    attribs.add ("key", shortcut);
                    XMLStorage.generateLeaf (sb, "bind", attribs, "  ");
                }
            }
            
            XMLStorage.generateFolderEnd (sb, "bindings", "");
            System.out.println(sb.toString ());
            fw.write (sb.toString ());
        } catch (IOException e) {
            ErrorManager.getDefault ().notify (e);
        } finally {
            try {
                if (fw != null) {
                    fw.flush ();
                    fw.close ();
                }
            } catch (IOException e) {}
        }
    }
    
    private static Map resolveNames (Map categoryToActions) {
        Map result = new HashMap ();
        Iterator it = categoryToActions.keySet ().iterator ();
        while (it.hasNext ()) {
            String category = (String) it.next ();
            Set actions = (Set) categoryToActions.get (category);
            Map actionsMap = new HashMap ();
            Iterator it1 = actions.iterator ();
            while (it1.hasNext ()) {
                ShortcutAction action = (ShortcutAction) it1.next ();
                actionsMap.put (action.getDisplayName (), action);
            }
            result.put (category, actionsMap);
        }
        return result;
    }
    
    /**
     * Converts:
     * Map (String (profile | category) > Map (String (category)) |
     *                                    ShortcutAction)
     * to xml. 
     *   (String > Map) is represented by folder and
     *   (String > DataObject) by ShadowDO
     */
    private static void generateShadowsToXML (
        LayersBridge        layersBridge,
        StringBuffer        sb,
        Map                 shortcutToAction,
        String              indentation
    ) {
        Iterator it = shortcutToAction.keySet ().iterator ();
        while (it.hasNext ()) {
            String key = (String) it.next ();
            Object value = shortcutToAction.get (key);
            if (value instanceof Map) {
                Attribs attribs = new Attribs (true);
                attribs.add ("name", key);
                XMLStorage.generateFolderStart (sb, "folder", attribs, indentation);
                generateShadowsToXML (
                    layersBridge,
                    sb, 
                    (Map) value, 
                    "    " + indentation
                );
                XMLStorage.generateFolderEnd (sb, "folder", indentation);
            } else {
                DataObject dob = layersBridge.getDataObject (value);
                if (dob == null) {
                    System.out.println("no Dataobject " + value);
                    continue;
                }
                FileObject fo = dob.getPrimaryFile ();
                Attribs attribs = new Attribs (true);
                attribs.add ("name", (String) key + ".shadow");
                XMLStorage.generateFolderStart (sb, "file", attribs, indentation);
                    Attribs attribs2 = new Attribs (true);
                    attribs2.add ("name", "originalFile");
                    attribs2.add ("stringvalue", fo.getPath ());
                    XMLStorage.generateLeaf (sb, "attr", attribs2, indentation + "    ");
                XMLStorage.generateFolderEnd (sb, "file", indentation);
            }
        }
    }
    
    private static Writer openWriter () throws IOException {
        JFileChooser fileChooser = new JFileChooser ();
        int result = fileChooser.showSaveDialog 
            (WindowManager.getDefault ().getMainWindow ());
        if (result != JFileChooser.APPROVE_OPTION) return null;
        File f = fileChooser.getSelectedFile ();
        return new FileWriter (f);
    }
    
    private static String loc (String key) {
        return NbBundle.getMessage (ExportShortcutsAction.class, key);
    }
}

