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

import org.netbeans.modules.xml.multiview.cookies.ErrorLocator;
import org.openide.nodes.Node;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * @author mkuchtiak
 */
public class SectionPanel extends javax.swing.JPanel implements NodeSectionPanel, ErrorLocator {

    private SectionView sectionView;
    private String title;
    private Node node;
    private boolean active;
    private SectionInnerPanel innerPanel;
    private Object key;
    private int index;

    private FocusListener sectionFocusListener = new FocusAdapter() {
        public void focusGained(FocusEvent e) {
            setActive(true);
        }
    };

    /**
     * Creates new form SectionPanel
     */
    public SectionPanel(SectionView sectionView, Node explorerNode, Object key) {
        this(sectionView, explorerNode, key, false);
    }

    public SectionPanel(SectionView sectionView, Node explorerNode, Object key, boolean autoExpand) {
        this(sectionView, explorerNode, explorerNode.getDisplayName(), key, autoExpand);
    }

    public SectionPanel(SectionView sectionView, Node node, String title, Object key) {
        this(sectionView, node, title, key, false);
    }

    public SectionPanel(SectionView sectionView, Node node, String title, Object key, boolean autoExpand) {
        this.sectionView = sectionView;
        this.title = title;
        this.node = node;
        this.key = key;

        initComponents();
        filler.setBackground(SectionVisualTheme.getFillerColor());
        filler.setVisible(false);
        setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        titleButton.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        actionPanel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        titleButton.setText(title);
        java.awt.Image image = node == null ? null : node.getIcon(java.beans.BeanInfo.ICON_COLOR_16x16);
        if (image != null) {
            titleButton.setIcon(new javax.swing.ImageIcon(image));
        }

        titleButton.addMouseListener(new org.openide.awt.MouseUtils.PopupMouseAdapter() {
            protected void showPopup(java.awt.event.MouseEvent e) {
                if (!SectionPanel.this.isActive()) {
                    SectionPanel.this.setActive(true);
                }
                JPopupMenu popup = getNode().getContextMenu();
                popup.show(foldButton, e.getX(), e.getY());
            }
        });
        if(autoExpand) {
            open();
        }
    }

    protected void openInnerPanel() {
        if (innerPanel != null) {
            return;
        }
        innerPanel = createInnerpanel();
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        filler.setVisible(true);
        innerPanel.addFocusListener(sectionFocusListener);
        add(innerPanel, gridBagConstraints);
    }

    protected SectionInnerPanel createInnerpanel() {
        return sectionView.getInnerPanelFactory().createInnerPanel(key);
    }

    private void closeInnerPanel() {
        if (innerPanel != null) {
            innerPanel.removeFocusListener(sectionFocusListener);
            remove(innerPanel);
            innerPanel = null;
        }
        filler.setVisible(false);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        titleButton.setText(title);
        this.title = title;
    }

    /**
     * Method from NodeSectionPanel interface
     */
    public Node getNode() {
        return node;
    }

    /**
     * Method from NodeSectionPanel interface
     */
    public void open() {
        foldButton.setSelected(true);
        openInnerPanel();
    }

    /**
     * Method from NodeSectionPanel interface
     */
    public void scroll() {
        Point location = SwingUtilities.convertPoint(this, getLocation(), sectionView.scrollPane);
        location.x = 0;
        sectionView.scrollPane.getViewport().setViewPosition(location);
    }

    /**
     * Method from NodeSectionPanel interface
     */
    public void setActive(boolean active) {
        //System.out.println("setActive = "+active +":"+node.getDisplayName());
        titleButton.setBackground(
                active ? SectionVisualTheme.getSectionHeaderActiveColor() : SectionVisualTheme.getSectionHeaderColor());
        if (headerButtons!=null) {
            for (int i=0;i<headerButtons.length;i++) headerButtons[i].setEnabled(active);
        }
        if (active && !this.equals(sectionView.getActivePanel())) {
            sectionView.sectionSelected(true);
            sectionView.setActivePanel(this);
            sectionView.selectNode(node);
        }
        this.active = active;
    }

    /**
     * Method from NodeSectionPanel interface
     */
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
        actionPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        foldButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/xml/multiview/resources/arrowright.gif")));
        foldButton.setBorder(null);
        foldButton.setBorderPainted(false);
        foldButton.setContentAreaFilled(false);
        foldButton.setFocusPainted(false);
        foldButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/xml/multiview/resources/arrowbottom.gif")));
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
        gridBagConstraints.gridwidth = 2;
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

        actionPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 2, 0));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(actionPanel, gridBagConstraints);

    }//GEN-END:initComponents

    private void titleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_titleButtonActionPerformed
        if (!foldButton.isSelected()) {
            openInnerPanel();
            foldButton.setSelected(true);
        } else {
            if (isActive()) {
                closeInnerPanel();
                foldButton.setSelected(false);
            }
        }
        if (!isActive()) setActive(true);
        
    }//GEN-LAST:event_titleButtonActionPerformed

    private void foldButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_foldButtonActionPerformed
        if (foldButton.isSelected()) {
            openInnerPanel();
            //setActive(true);
        } else {
            closeInnerPanel();
        }
        //contentPanel.setVisible(!foldButton.isSelected());
        //filler.setVisible(!foldButton.isSelected());
    }//GEN-LAST:event_foldButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel actionPanel;
    private javax.swing.JPanel filler;
    private javax.swing.JToggleButton foldButton;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton titleButton;
    // End of variables declaration//GEN-END:variables

    public void setKey(Object key) {
        this.key = key;
    }

    public Object getKey() {
        return key;
    }

    public JComponent getErrorComponent(String errorId) {
        if (innerPanel != null) {
            return innerPanel.getErrorComponent(errorId);
        }
        return null;
    }

    SectionInnerPanel getInnerPanel() {
        return innerPanel;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
    
    private HeaderButton[] headerButtons;
    
    public void setHeaderActions(Action[] actions) {
        headerButtons = new HeaderButton[actions.length];
        for (int i=0;i<actions.length;i++) {
            headerButtons[i] = new HeaderButton(this,actions[i]);
            actionPanel.add(headerButtons[i]);
        }
    }
    
    public HeaderButton[] getHeaderButtons(){
        return headerButtons;
    }

    public JPanel getFiller() {
        return filler;
    }

    public JToggleButton getFoldButton() {
        return foldButton;
    }

    public JSeparator getSeparator() {
        return jSeparator1;
    }

    public JButton getTitleButton() {
        return titleButton;
    }

    public static class HeaderButton extends javax.swing.JButton {
        private SectionPanel panel;
        public HeaderButton(SectionPanel panel, Action action) {
            super(action);
            this.panel=panel;
            setEnabled(false);
        }     
        public SectionPanel getSectionPanel() {
            return panel;
        }
    }
    
}
