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

package org.netbeans.modules.xml.multiview.ui;

import javax.swing.JPanel;
import java.awt.*;

import org.openide.nodes.Node;
import org.netbeans.modules.xml.multiview.cookies.SectionFocusCookie;
import org.netbeans.modules.xml.multiview.Utils;
/**
 *
 * @author mkuchtiak
 */
public class SectionView extends PanelView implements SectionFocusCookie, ContainerPanel {
    private JPanel scrollPanel, filler;
    javax.swing.JScrollPane scrollPane;
    private java.util.Hashtable map;
    private int sectionCount=0;
    private NodeSectionPanel activePanel;
    private InnerPanelFactory factory = null;
    boolean sectionSelected;


    public SectionView(InnerPanelFactory factory) {
        super();
        this.factory=factory;
    }
    
    public SectionView() {
        super();
    }
    
    public void initComponents() {
        super.initComponents();
        map = new java.util.Hashtable();
        setLayout(new java.awt.BorderLayout());
        scrollPanel = new JPanel();
        scrollPanel.setLayout(new java.awt.GridBagLayout());
        scrollPane = new javax.swing.JScrollPane();
        scrollPane.setViewportView(scrollPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(15);
        filler = new JPanel();
        filler.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        add (scrollPane, BorderLayout.CENTER);
    }
    
    public boolean focusSection(NodeSectionPanel panel) {
        panel.open();
        openParents((JPanel)panel);
        panel.scroll();
        setActivePanel(panel);
        panel.setActive(true);
        return true;
    }

    protected void openSection(Node node){
        NodeSectionPanel panel = (NodeSectionPanel) map.get(node);
        if (panel != null) {
            focusSection(panel);
        }
    }
    
    private void openParents(JPanel panel){
        javax.swing.JScrollPane scrollP = null;
        NodeSectionPanel parentSection=null;
        java.awt.Container ancestor = panel.getParent();
        while (ancestor !=null && scrollP == null){
            if (ancestor instanceof javax.swing.JScrollPane){
                scrollP = (javax.swing.JScrollPane) ancestor;
            }
            if (ancestor instanceof NodeSectionPanel){
                parentSection = (NodeSectionPanel) ancestor;
                parentSection.open();
            }
            ancestor = ancestor.getParent();
        }
    }
    
    void mapSection(Node key, NodeSectionPanel panel){
        map.put(key,panel);
    }
    
    void deleteSection(Node key){
        map.remove(key);
    }
    
    public NodeSectionPanel getSection(Node key){
        return (NodeSectionPanel)map.get(key);
    }
    
    public void addSection(NodeSectionPanel section, boolean open) {
        addSection(section);
        if (open) {
            section.open();
            section.scroll();
            section.setActive(true);
        }
    }
    
    public void addSection(NodeSectionPanel section) {
        scrollPanel.remove(filler);
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = sectionCount;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        //gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 6);
        scrollPanel.add((JPanel)section,gridBagConstraints);
        section.setIndex(sectionCount);
        
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = sectionCount+1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 2.0;
        //gridBagConstraints.insets = new java.awt.Insets(6, 2, 0, 6);
        scrollPanel.add(filler,gridBagConstraints);
        
        mapSection(section.getNode(), section);
        sectionCount++;
    }
    /** Removing section and its corresponding node
     */
    public void removeSection(Node node) {
        NodeSectionPanel section = getSection(node);
        if (section!=null) {
            // looking for enclosing container
            java.awt.Container cont = ((java.awt.Component)section).getParent();
            while (cont!=null && !(cont instanceof ContainerPanel)) {
                cont = cont.getParent();
            }
            if ( cont!= null) {
                // removing last active component
                ContainerPanel contPanel = (ContainerPanel)cont;
                if (section instanceof SectionPanel) {
                    Object key = ((SectionPanel)section).getKey();
                    if (key!=null && key==getLastActive()) {
                        setLastActive(null);
                    }
                }
                // removing section
                contPanel.removeSection(section);
                // removing node
                contPanel.getRoot().getChildren().remove(new Node[]{node});
            }
        }
    }
    
    public void removeSection(NodeSectionPanel panel){
        int panelIndex = panel.getIndex();
        scrollPanel.remove((JPanel)panel);
        
        // the rest components have to be moved up
        java.awt.Component[] components = scrollPanel.getComponents();
        java.util.AbstractList removedPanels = new java.util.ArrayList(); 
        for (int i=0;i<components.length;i++) {
            if (components[i] instanceof NodeSectionPanel) {
                NodeSectionPanel pan = (NodeSectionPanel)components[i];
                int index = pan.getIndex();
                if (index>panelIndex) {
                    scrollPanel.remove((JPanel)pan);
                    pan.setIndex(index-1);
                    removedPanels.add(pan);
                }
            }
        }
        for (int i=0;i<removedPanels.size();i++) {
            NodeSectionPanel pan = (NodeSectionPanel)removedPanels.get(i);
            java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = pan.getIndex();
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints.weightx = 1.0;
            //gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 6);
            scrollPanel.add((JPanel)pan,gridBagConstraints);
        }
        deleteSection(panel.getNode());
        sectionCount--;
    }

    public void setActivePanel(NodeSectionPanel activePanel) {
        if (this.activePanel!=null && this.activePanel!=activePanel) {
            this.activePanel.setActive(false);
        }
        this.activePanel = activePanel;
        if (activePanel instanceof SectionPanel) {
            setLastActive(((SectionPanel)activePanel).getKey());
        }
    }
    
    public NodeSectionPanel getActivePanel() {
        return activePanel;
    }
    
    public void selectNode(Node node) {
        setManagerSelection(new Node[]{node});
    }
    
    public void showSelection(org.openide.nodes.Node[] nodes) {
        if (sectionSelected) {
            sectionSelected=false;
            return;
        }
        if (nodes!=null && nodes.length>0) {
         final Node n = nodes[0];
         Utils.runInAwtDispatchThread(new Runnable () {
             public void run() {
                 openSection(n);
             }
         });
        }
    }
    
    void sectionSelected(boolean sectionSelected) {
        this.sectionSelected=sectionSelected;
    }
    
    protected org.netbeans.modules.xml.multiview.Error validateView() {
        return null;
    }
    
    public SectionPanel findSectionPanel(Object key) {
        java.util.Enumeration en = map.keys();
        while (en.hasMoreElements()) {
            NodeSectionPanel pan = (NodeSectionPanel)map.get(en.nextElement());
            if (pan instanceof SectionPanel) {
                SectionPanel p = (SectionPanel)pan;
                if (key==p.getKey()) {
                    return p;
                }
            }
        }
        return null;
    }
    
    InnerPanelFactory getInnerPanelFactory() {
        return factory;
    }
    
    public void setInnerPanelFactory(InnerPanelFactory factory) {
        this.factory=factory;
    }
    
    public void openPanel(Object key) {
        if (key!=null) {
            SectionPanel panel = findSectionPanel(key);
            if (panel!=null) {
                if (panel.getInnerPanel()==null) panel.open();
                openParents((JPanel)panel);
                panel.scroll();
                panel.setActive(true);
            }
        }
    }
    
    private Object getLastActive() {
        ToolBarDesignEditor toolBarDesignEditor = getToolBarDesignEditor();
        return toolBarDesignEditor == null ? null : toolBarDesignEditor.getLastActive();
    }

    private void setLastActive(Object key) {
        ToolBarDesignEditor toolBarDesignEditor = getToolBarDesignEditor();
        if(toolBarDesignEditor != null) {
            toolBarDesignEditor.setLastActive(key);
        }
    }

    protected ToolBarDesignEditor getToolBarDesignEditor() {
        Container parent = getParent();
        return parent == null ? null : (ToolBarDesignEditor) parent.getParent();
    }
}
