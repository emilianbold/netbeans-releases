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

import java.awt.*;
import java.util.*;
import java.net.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import org.openide.nodes.Node;
import org.netbeans.modules.xml.multiview.cookies.LinkCookie;
import org.netbeans.modules.xml.multiview.cookies.ErrorComponentContainer;

/**
 *
 * @author  mkuchtiak
 */
public class SectionPanel extends javax.swing.JPanel implements NodeSectionPanel, ErrorComponentContainer {
    
    private Node activeNode=null;
    private SectionView sectionView;
    private String title;
    private Node node;
    private boolean active;
    private CustomPanel customPanel;
    private Object key;
    private CustomPanelFactory panelFactory;
    
    /** Creates new form SectionContainer */

    public SectionPanel(SectionView sectionView, Node explorerNode, Object key, CustomPanelFactory panelFactory) {
        this(sectionView, explorerNode, explorerNode.getDisplayName(), key, panelFactory);
    }
    
    public SectionPanel(SectionView sectionView, Node node, String title, Object key, CustomPanelFactory panelFactory) {
        this.sectionView = sectionView;
        this.title=title;
        this.node=node;
        this.key=key;
        this.panelFactory = panelFactory;

        initComponents();
        filler.setBackground(SectionVisualTheme.getFillerColor());
        filler.setVisible(false);
        setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        titleButton.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        titleButton.setText(title);
        java.awt.Image image = node.getIcon(java.beans.BeanInfo.ICON_COLOR_16x16);
        if (image!=null) {
            titleButton.setIcon(new javax.swing.ImageIcon(image));
        }
        
        titleButton.addMouseListener(new org.openide.awt.MouseUtils.PopupMouseAdapter() {
            protected void showPopup(java.awt.event.MouseEvent e) {
                if (!SectionPanel.this.isActive()) {
                    SectionPanel.this.setActive(true);
                }
                JPopupMenu popup = getNode().getContextMenu();
                popup.show(foldButton,e.getX(), e.getY());
            }
        });
    }
    
    private void openCustomPanel() {     
        if (this.customPanel!=null) remove(customPanel);
        customPanel = panelFactory.createCustomPanel(key);
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        filler.setVisible(true);
        customPanel.addMouseListener(new org.openide.awt.MouseUtils.PopupMouseAdapter() {
            protected void showPopup(java.awt.event.MouseEvent e) {
                if (!SectionPanel.this.isActive()) {
                    SectionPanel.this.setActive(true);
                }
                JPopupMenu popup = getNode().getContextMenu();
                popup.show(foldButton,e.getX(), e.getY());
            }
        });
        add(customPanel, gridBagConstraints);
    }
    
    private void closeCustomPanel() {     
        if (this.customPanel!=null) {
            remove(customPanel);
            customPanel=null;
        }
        filler.setVisible(false);
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title=title;
    }
    
    /** Method from NodeSectionPanel interface */
    public Node getNode() {
        return node; 
    }
    
    /** Method from NodeSectionPanel interface */
    public void open(){
        foldButton.setSelected(false);
        //contentPanel.setVisible(true);
        openCustomPanel();
        filler.setVisible(true);
    }
    
    /** Method from NodeSectionPanel interface */
    public void scroll() {
        Point location = SwingUtilities.convertPoint(this, getLocation(),sectionView.scrollPane);
        location.x=0;
        sectionView.scrollPane.getViewport().setViewPosition(location);
    }
    
    /** Method from NodeSectionPanel interface */
    public void setActive(boolean active) {
        //System.out.println("setActive = "+active +":"+node.getDisplayName());
        titleButton.setBackground(active?SectionVisualTheme.getSectionHeaderActiveColor():SectionVisualTheme.getSectionHeaderColor());
        if (active && !this.equals(sectionView.getActivePanel())) {
            sectionView.sectionSelected(true);
            sectionView.setActivePanel(this);
            sectionView.selectNode(node);
        }
        this.active=active;
    }
    
    /** Method from NodeSectionPanel interface */
    public boolean isActive() {
        return active;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        foldButton = new javax.swing.JToggleButton();
        titleButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        filler = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        foldButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/xml/multiview/resources/arrowbottom.gif")));
        foldButton.setSelected(true);
        foldButton.setBorder(null);
        foldButton.setBorderPainted(false);
        foldButton.setContentAreaFilled(false);
        foldButton.setFocusPainted(false);
        foldButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/xml/multiview/resources/arrowright.gif")));
        foldButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                foldButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 0, 6);
        add(foldButton, gridBagConstraints);

        titleButton.setFont(new java.awt.Font("Dialog", 1, 14));
        titleButton.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(1, 1, 1, 1)));
        titleButton.setBorderPainted(false);
        titleButton.setFocusPainted(false);
        titleButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        titleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                titleButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(titleButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        add(jSeparator1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 0, 6);
        add(filler, gridBagConstraints);

    }//GEN-END:initComponents

    private void titleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_titleButtonActionPerformed
        // TODO add your handling code here:
        setActive(true);
    }//GEN-LAST:event_titleButtonActionPerformed

    private void foldButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_foldButtonActionPerformed
        // TODO add your handling code here:
        if (!foldButton.isSelected()) openCustomPanel();
        else closeCustomPanel();
        //contentPanel.setVisible(!foldButton.isSelected());
        //filler.setVisible(!foldButton.isSelected());
    }//GEN-LAST:event_foldButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel filler;
    private javax.swing.JToggleButton foldButton;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton titleButton;
    // End of variables declaration//GEN-END:variables
    
    public static abstract class CustomPanel extends javax.swing.JPanel implements LinkCookie, ErrorComponentContainer {

        public void add (java.awt.Component comp, Object constraints) {
            super.add(comp, constraints);           
            if (comp instanceof javax.swing.text.JTextComponent) {
                ((javax.swing.text.JTextComponent)comp).addFocusListener(new java.awt.event.FocusAdapter() {
                    public void focusGained(java.awt.event.FocusEvent evt) {
                        java.awt.Component parent = CustomPanel.this.getParent();
                        if (parent instanceof  SectionPanel) {
                            ((SectionPanel)parent).setActive(true);
                        }
                    }
                });
            }
        }
        
        public abstract javax.swing.JComponent getErrorComponent(String errorId);
    }

    public void setKey (Object key) {
        this.key=key;
    }
    
    public Object getKey() {
        return key;
    }

    public JComponent getErrorComponent(String errorId) {
        if (customPanel!=null) return customPanel.getErrorComponent(errorId);
        return null;
    }
    
    CustomPanel getCustomPanel() {
        return customPanel;
    }
}
