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

package org.netbeans.core.windows.view.ui.toolbars;

import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.loaders.DataFolder;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.DefaultEditorKit;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import org.openide.actions.ToolsAction;
import org.openide.util.actions.SystemAction;


/**
 * Dialog for displaying the configuration of toolbars.
 * @author Milos Kleint
 */
public class ConfigureToolbarPanel extends JPanel implements ExplorerManager.Provider, Lookup.Provider
{
    static final long serialVersionUID =8538698225954965242L;
    
    private ExplorerManager manager;
    private Lookup lookup;
    
    /**
     * shows the Configure Toolbars dialog (nonmodal)
     */
    static void showConfigureDialog(final Node rootNode) {
        final ConfigureToolbarPanel panel = new ConfigureToolbarPanel();
        DialogDescriptor desc = new DialogDescriptor(panel, NbBundle.getBundle(ConfigureToolbarPanel.class).getString("TITLE_configureToolbarPanel"));
        desc.setModal(false);
        JButton clButton = new JButton(NbBundle.getBundle(ConfigureToolbarPanel.class).getString("ConfigureToolbarPanel.closeButton.text"));
        clButton.setMnemonic(NbBundle.getBundle(ConfigureToolbarPanel.class).getString("ConfigureToolbarPanel.closeButton.mnemonic").charAt(0));
        Object[] options = new Object[] { clButton };
        desc.setOptions(options);
        desc.setValue(options[0]);
        desc.setClosingOptions(options);
        final Dialog dial = DialogDisplayer.getDefault().createDialog(desc);
        Mutex.EVENT.readAccess(new Runnable()
        {
            public void run() {
                // filter layer to strip the standard.xml and other config files..
                panel.getExplorerManager().setRootContext(new FilterNode(rootNode, new ToolbarsMainFilterChildren(rootNode)));
                dial.show();
            }
        });
    }
    
    
    /**
     * Create new ConfigureToolbarPanel
     */
    private ConfigureToolbarPanel() {
        manager = new ExplorerManager();
        
        //TODO adding actions however it doesn't seem to work in a dialog. Dunno why. (mkleint)
        ActionMap map = getActionMap();
        map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(manager));
        map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(manager));
        map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(manager));
        map.put("delete", ExplorerUtils.actionDelete(manager, true)); // or false

        InputMap keys = getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        keys.put(KeyStroke.getKeyStroke("control c"), DefaultEditorKit.copyAction); //NOI18N
        keys.put(KeyStroke.getKeyStroke("control x"), DefaultEditorKit.cutAction); //NOI18N
        keys.put(KeyStroke.getKeyStroke("control v"), DefaultEditorKit.pasteAction); //NOI18N
        keys.put(KeyStroke.getKeyStroke("DELETE"), "delete"); //NOI18N

        // ...and initialization of lookup variable
        lookup = ExplorerUtils.createLookup (manager, map);
        
        getAccessibleContext().setAccessibleName(NbBundle.getBundle(ConfigureToolbarPanel.class).getString("ACSN_configureToolbarPanel"));
        getAccessibleContext().setAccessibleDescription(NbBundle.getBundle(ConfigureToolbarPanel.class).getString("ACSD_configureToolbarPanel"));
        
        initialize();
        
    }
    
    private void initialize()
    {
        setLayout(new BorderLayout());
        BeanTreeView tv = new BeanTreeView();
       // install proper border for treeview
        tv.setBorder((Border)UIManager.get("Nb.ScrollPane.border")); // NOI18N
        add(tv, BorderLayout.CENTER);
        
    }
    
    // ...method as before and getLookup
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    public Lookup getLookup() {
        return lookup;
    }
    // ...methods as before, but replace componentActivated and
    // componentDeactivated with e.g.:
    public void addNotify() {
        super.addNotify();
        ExplorerUtils.activateActions(manager, true);
    }
    public void removeNotify() {
        ExplorerUtils.activateActions(manager, false);
        super.removeNotify();
    }
    
   
    /**
     * Filters out everything that is not a folder from the main folder. 
     * Eg. the Standard.xml file and other config files.
     */
    
    private static class ToolbarsMainFilterChildren extends FilterNode.Children {
        
        public ToolbarsMainFilterChildren(Node originalNode) {
            super(originalNode);
        }
        /**
         * Create nodes for a given key.
         * @param key the key
         * @return child nodes for this key or null if there should be no
         *    nodes for this key
         */
        protected Node[] createNodes(Object key) {
            Node[] retValue;
            if (key instanceof Node) {
                Node nd = (Node)key;
                DataFolder folder = (DataFolder)nd.getCookie(DataFolder.class);
                // if it's not a folder, ignore..
                retValue = folder == null ? new Node[0] : new Node[] { new CategoryNode(nd) };
            } else {
                retValue = super.createNodes(key);
            }
            return retValue;
        }
        
    }
    
    private static class CategoryNode extends FilterNode {
        public CategoryNode(Node original) {
            super(original);
        }
// filter out the Tools popup item.. no meaning here.
        public SystemAction[] getActions() {
            SystemAction[] retValue;
            retValue = super.getActions();
            boolean hasTools = false;
            ArrayList lst = new ArrayList(retValue.length + 5);
            for (int i = 0; i < retValue.length; i++) {
                if (retValue[i] != null && retValue[i] instanceof ToolsAction) {
                    hasTools = true;
                } else {
                    lst.add(retValue[i]);
                }
            }
            if (hasTools) {
                retValue = new SystemAction[retValue.length - 1];
                Iterator it = lst.iterator();
                int index = 0;
                while (it.hasNext()) {
                    retValue[index] = (SystemAction)it.next();
                    index = index + 1;
                }
            }
            return retValue;
        }
        
        
    }
    
}

