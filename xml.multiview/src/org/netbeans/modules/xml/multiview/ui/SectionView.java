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
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import org.openide.nodes.Node;
import org.netbeans.modules.xml.multiview.cookies.SectionFocusCookie;
/**
 *
 * @author mkuchtiak
 */
public class SectionView extends PanelView implements SectionFocusCookie {
    private JPanel scrollPanel, filler;
    javax.swing.JScrollPane scrollPane;
    private java.util.Hashtable map;
    private int sectionCount=0;
    private NodeSectionPanel activePanel;
    boolean sectionSelected; 
    
    public void initComponents() {
        super.initComponents();
        map = new java.util.Hashtable();
        setLayout(new java.awt.BorderLayout());
        scrollPanel = new JPanel();
        scrollPanel.setLayout(new java.awt.GridBagLayout());
        scrollPane = new javax.swing.JScrollPane();
        scrollPane.setViewportView(scrollPanel);
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

    private void openSection(Node node){
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
    
    public void mapSection(NodeSectionPanel panel){
        mapSection(panel.getNode(),panel);
    }
    
    public void mapSection(Node key, NodeSectionPanel panel){
        map.put(key,panel);
    }
    
    public NodeSectionPanel getSection(Node key){
        return (NodeSectionPanel)map.get(key);
    }    
    
    public void addSection(NodeSectionPanel section){
        scrollPanel.remove(filler);
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = sectionCount;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        //gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 6);
        scrollPanel.add((JPanel)section,gridBagConstraints);  
        
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = sectionCount+1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 2.0;
        scrollPanel.add(filler,gridBagConstraints);  
        
        mapSection(section.getNode(), section);
        sectionCount++;
    }

    public void setActivePanel(NodeSectionPanel activePanel) {
        if (this.activePanel!=null && this.activePanel!=activePanel) {
            this.activePanel.setActive(false);
        }
        this.activePanel = activePanel;
    }
    
    public NodeSectionPanel getActivePanel() {
        return activePanel;
    }
    
    public void selectNode(Node node) {
        setManagerSelection(new Node[]{node});
    }
    
    public void showSelection(org.openide.nodes.Node[] nodes) {
        //System.out.println("showSelection "+nodes[0]+":"+sectionSelected);
        if (sectionSelected) {
            sectionSelected=false;
            return;
        }
        if (nodes!=null && nodes.length>0) {
         final Node n = nodes[0];
         javax.swing.SwingUtilities.invokeLater(new Runnable () {
             public void run() {
                 openSection(n);
             }
         });
        }
    }
    
    void sectionSelected(boolean sectionSelected) {
        this.sectionSelected=sectionSelected;
    }
    
    public org.netbeans.modules.xml.multiview.Error validatePanel() {
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
    
}
