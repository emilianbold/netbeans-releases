/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.vmd.inspector;



import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Collection;
import java.util.WeakHashMap;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.openide.util.Lookup;

import javax.swing.*;
import org.netbeans.modules.vmd.api.io.IOUtils;
import org.netbeans.modules.vmd.api.model.common.ActiveDocumentSupport;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * @author Karol Harezlak
 */

public final class InspectorPanel implements NavigatorPanel, ActiveDocumentSupport.Listener {
    
    private static final JLabel emptyPanel = new JLabel(NbBundle.getMessage(InspectorPanel.class, "LBL_emptyPanel"), JLabel.CENTER); //NOI18N
    
    private static InspectorPanel INSTANCE;
    private Node[] nodesToRemove;
    private JPanel panel;
    //TODO Memory leak !!
    private WeakHashMap<DesignDocument, InspectorUI> uiMap;
    
    public static InspectorPanel getInstance() {
        synchronized(InspectorPanel.class) {
            if (INSTANCE == null) {
                INSTANCE = new InspectorPanel();
                ActiveDocumentSupport.getDefault().addActiveDocumentListener(INSTANCE);
            }
            return INSTANCE;
        }
    }
    
    //private InspectorUI ui;
    private Lookup lookup;
    /** Dynamic Lookup content */
    private final InstanceContent ic;
    
    private InspectorPanel() {
        this.ic = new InstanceContent();
        this.lookup = new AbstractLookup(ic);
        this.panel = new JPanel(new BorderLayout());
        this.panel.setBackground(Color.WHITE);
        this.uiMap = new WeakHashMap<DesignDocument, InspectorUI>();
    }
    
    synchronized InspectorUI getUI(DesignDocument document) {
        InspectorUI ui = uiMap.get(document);
        if (ui == null)
            uiMap.put(document , new InspectorUI(document));
        return uiMap.get(document);
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(InspectorPanel.class, "LBL_InspectorPanelDisplayName"); //NOI18N
    }
    
    public String getDisplayHint() {
        return NbBundle.getMessage(InspectorPanel.class, "LBL_InspectorPanelHint"); //NOI18N
    }
    
    public synchronized JComponent getComponent() {
        return panel;
    }
    
    public void panelActivated(Lookup lookup) {
    }
    
    public void panelDeactivated() {
    }
    
    public Lookup getLookup() {
        return lookup;
    }
     
    synchronized void selectionChanged(final Node[] nodes) {
        IOUtils.runInAWTNoBlocking(new Runnable() {
            public void run() {
                if(nodesToRemove != null) {
                    for (Node node : nodesToRemove) {
                        ic.remove(node);
                    }
                }
                for (Node node : nodes) {
                    ic.add(node);
                }
                nodesToRemove = nodes;
            }
        });
    }
    
    public synchronized void activeDocumentChanged(DesignDocument deactivatedDocument,final DesignDocument activatedDocument) {
        if (activatedDocument == null) {
            IOUtils.runInAWTNoBlocking(new Runnable() {
                public void run() {
                    panel.removeAll();
                    panel.add(emptyPanel, BorderLayout.CENTER);
                    panel.revalidate();
                    panel.repaint();
                }
            });
            return;
        }
        
        IOUtils.runInAWTNoBlocking(new Runnable() {
            public void run() {
                panel.removeAll();
                InspectorUI ui = uiMap.get(activatedDocument);
                if (ui != null)
                    panel.add(ui, BorderLayout.CENTER);
                panel.revalidate();
                panel.repaint();
            }
        });
    }
    
    public void activeComponentsChanged(Collection<DesignComponent> activeComponents) {
    }
}
