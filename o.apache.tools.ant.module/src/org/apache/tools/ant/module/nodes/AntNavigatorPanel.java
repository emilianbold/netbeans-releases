/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.apache.tools.ant.module.nodes;

import java.awt.BorderLayout;
import java.beans.PropertyVetoException;
import java.util.Collection;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import org.apache.tools.ant.module.api.AntProjectCookie;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.ListView;
import org.openide.loaders.DataObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;

/**
 * Displays Ant targets in the Navigator.
 * @author Jesse Glick
 */
public final class AntNavigatorPanel implements NavigatorPanel {
    
    private Lookup.Result selection;
    private final LookupListener selectionListener = new LookupListener() {
        public void resultChanged(LookupEvent ev) {
            display(selection.allInstances());
        }
    };
    private JComponent panel;
    private final ExplorerManager manager = new ExplorerManager();
    
    /**
     * Default constructor for layer instance.
     */
    public AntNavigatorPanel() {}
    
    public String getDisplayName() {
        return NbBundle.getMessage(AntNavigatorPanel.class, "ANP_label");
    }
    
    public String getDisplayHint() {
        return NbBundle.getMessage(AntNavigatorPanel.class, "ANP_hint");
    }
    
    public JComponent getComponent() {
        if (panel == null) {
            final ListView view = new ListView();
            view.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            class Panel extends JPanel implements ExplorerManager.Provider, Lookup.Provider {
                // Make sure action context works correctly:
                private final Lookup lookup = ExplorerUtils.createLookup(manager, new ActionMap());
                {
                    setLayout(new BorderLayout());
                    add(view, BorderLayout.CENTER);
                }
                public ExplorerManager getExplorerManager() {
                    return manager;
                }
                // Make sure list gets focus, with first node initially selected:
                public boolean requestFocusInWindow() {
                    boolean b = view.requestFocusInWindow();
                    if (manager.getSelectedNodes().length == 0) {
                        Node[] children = manager.getRootContext().getChildren().getNodes(true);
                        if (children.length > 0) {
                            try {
                                manager.setSelectedNodes(new Node[] {children[0]});
                            } catch (PropertyVetoException e) {
                                assert false : e;
                            }
                        }
                    }
                    return b;
                }
                public Lookup getLookup() {
                    return lookup;
                }
            }
            panel = new Panel();
        }
        return panel;
    }
    
    public void panelActivated(Lookup context) {
        selection = context.lookup(new Lookup.Template(DataObject.class));
        selection.addLookupListener(selectionListener);
        selectionListener.resultChanged(null);
    }
    
    public void panelDeactivated() {
        selection.removeLookupListener(selectionListener);
        selection = null;
    }
    
    public Lookup getLookup() {
        return null;
    }
    
    private void display(Collection/*<DataObject>*/ selectedFiles) {
        // Show list of targets for selected file:
        if (selectedFiles.size() == 1) {
            DataObject d = (DataObject) selectedFiles.iterator().next();
            AntProjectCookie cookie = (AntProjectCookie) d.getCookie(AntProjectCookie.class);
            if (cookie != null) {
                manager.setRootContext(new AbstractNode(new AntProjectChildren(cookie)));
                return;
            }
        }
        // Fallback:
        manager.setRootContext(Node.EMPTY);
    }
    
}
