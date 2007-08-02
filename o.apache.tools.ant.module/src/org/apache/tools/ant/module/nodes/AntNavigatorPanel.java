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

package org.apache.tools.ant.module.nodes;

import java.awt.BorderLayout;
import java.beans.PropertyVetoException;
import java.util.Collection;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.ListView;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/**
 * Displays Ant targets in the Navigator.
 * @author Jesse Glick
 */
public final class AntNavigatorPanel implements NavigatorPanel {
    
    private Lookup.Result<DataObject> selection;
    private final LookupListener selectionListener = new LookupListener() {
        public void resultChanged(LookupEvent ev) {
            Mutex.EVENT.readAccess(new Runnable() { // #69355: safest to run in EQ
                public void run() {
                    if (selection != null) { // #111772
                        display(selection.allInstances());
                    }
                }
            });
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
                @Override
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
        selection = context.lookupResult(DataObject.class);
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
    
    private void display(Collection<? extends DataObject> selectedFiles) {
        // Show list of targets for selected file:
        if (selectedFiles.size() == 1) {
            DataObject d = selectedFiles.iterator().next();
            manager.setRootContext(d.getNodeDelegate());
            return;
        }
        // Fallback:
        manager.setRootContext(Node.EMPTY);
    }
    
}
