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
import org.netbeans.modules.xml.multiview.Utils;
import org.openide.nodes.Node;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.VetoableChangeListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;

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
    private ToolBarDesignEditor toolBarDesignEditor;

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
        headerSeparator.setForeground(SectionVisualTheme.getSectionHeaderLineColor());
        fillerLine.setForeground(SectionVisualTheme.getFoldLineColor());
        fillerEnd.setForeground(SectionVisualTheme.getFoldLineColor());
        fillerLine.setVisible(false);
        fillerEnd.setVisible(false);
        setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        titlePanel.setBackground(SectionVisualTheme.getSectionHeaderColor());
        actionPanel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        titleButton.setText(title);
        /*
        java.awt.Image image = node == null ? null : node.getIcon(java.beans.BeanInfo.ICON_COLOR_16x16);
        if (image != null) {
            titleButton.setIcon(new javax.swing.ImageIcon(image));
        }
        */
        titleButton.addMouseListener(new org.openide.awt.MouseUtils.PopupMouseAdapter() {
            protected void showPopup(java.awt.event.MouseEvent e) {
                if (!SectionPanel.this.isActive()) {
                    SectionPanel.this.setActive(true);
                }
                JPopupMenu popup = getNode().getContextMenu();
                popup.show(titleButton, e.getX(), e.getY());
            }
        });
        if(autoExpand) {
            open();
        }
        titleButton.addFocusListener(sectionFocusListener);
    }
    
    public SectionView getSectionView() {
        return sectionView;
    }

    protected void openInnerPanel() {
        if (toolBarDesignEditor == null) {
            toolBarDesignEditor = sectionView.getToolBarDesignEditor();
            if (toolBarDesignEditor != null) {
                toolBarDesignEditor.addVetoableChangeListener(new VetoableChangeListener() {
                    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
                        if (ToolBarDesignEditor.PROPERTY_FLUSH_DATA.equals(evt.getPropertyName()) &&
                                evt.getNewValue() == null) {
                            if (innerPanel != null && !innerPanel.canClose()) {
                                throw new PropertyVetoException("", evt);
                            }
                        }
                    }
                });
            }
        }
        if (innerPanel != null) {
            return;
        }
        innerPanel = createInnerpanel();
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        fillerLine.setVisible(true);
        fillerEnd.setVisible(true);
        innerPanel.addFocusListener(sectionFocusListener);
        add(innerPanel, gridBagConstraints);
        Utils.scrollToVisible(this);
        innerPanel.setBackground(
            active ? SectionVisualTheme.getSectionActiveBackgroundColor() : SectionVisualTheme.getDocumentBackgroundColor());
    }

    protected SectionInnerPanel createInnerpanel() {
        return sectionView.getInnerPanelFactory().createInnerPanel(key);
    }

    protected void closeInnerPanel() {
        if (innerPanel != null) {
            innerPanel.removeFocusListener(sectionFocusListener);
            remove(innerPanel);
            innerPanel = null;
        }
        fillerLine.setVisible(false);
        fillerEnd.setVisible(false);
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
        Utils.scrollToVisible(this);
    }

    /**
     * Method from NodeSectionPanel interface
     */
    public void setActive(boolean active) {
        //System.out.println("setActive = "+active +":"+node.getDisplayName());
        titlePanel.setBackground(
            active ? SectionVisualTheme.getSectionHeaderActiveColor() : SectionVisualTheme.getSectionHeaderColor());
        //headerSeparator.setVisible(!active);
        if (innerPanel!=null) innerPanel.setBackground(
            active ? SectionVisualTheme.getSectionActiveBackgroundColor() : SectionVisualTheme.getDocumentBackgroundColor());
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
        headerSeparator = new javax.swing.JSeparator();
        actionPanel = new javax.swing.JPanel();
        fillerLine = new javax.swing.JSeparator();
        fillerEnd = new javax.swing.JSeparator();
        titlePanel = new javax.swing.JPanel();
        titleButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        foldButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/xml/multiview/resources/plus.gif")));
        foldButton.setBorder(null);
        foldButton.setBorderPainted(false);
        foldButton.setContentAreaFilled(false);
        foldButton.setFocusPainted(false);
        foldButton.setFocusable(false);
        foldButton.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/xml/multiview/resources/minus.gif")));
        foldButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                foldButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 2, 0, 2);
        add(foldButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 0, 0, 0);
        add(headerSeparator, gridBagConstraints);

        actionPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 2, 0));

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(actionPanel, gridBagConstraints);

        fillerLine.setOrientation(javax.swing.SwingConstants.VERTICAL);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        add(fillerLine, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 2);
        add(fillerEnd, gridBagConstraints);

        titlePanel.setLayout(new java.awt.BorderLayout());

        titleButton.setFont(new java.awt.Font("Dialog", 1, 14));
        titleButton.setBorderPainted(false);
        titleButton.setContentAreaFilled(false);
        titleButton.setFocusPainted(false);
        titleButton.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        titleButton.setMargin(new java.awt.Insets(0, 4, 0, 4));
        titleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                titleButtonActionPerformed(evt);
            }
        });

        titlePanel.add(titleButton, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        add(titlePanel, gridBagConstraints);

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
        } else {
            closeInnerPanel();
        }
    }//GEN-LAST:event_foldButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel actionPanel;
    private javax.swing.JSeparator fillerEnd;
    private javax.swing.JSeparator fillerLine;
    private javax.swing.JToggleButton foldButton;
    private javax.swing.JSeparator headerSeparator;
    private javax.swing.JButton titleButton;
    private javax.swing.JPanel titlePanel;
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

    public SectionInnerPanel getInnerPanel() {
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
    
    protected JComponent getFillerLine() {
        return fillerLine;
    }
    
    protected JComponent getFillerEnd() {
        return fillerEnd;
    }
    
    protected JToggleButton getFoldButton() {
        return foldButton;
    }

    protected JSeparator getHeaderSeparator() {
        return headerSeparator;
    }

    protected JButton getTitleButton() {
        return titleButton;
    }

    public static class HeaderButton extends javax.swing.JButton {
        private SectionPanel panel;
        public HeaderButton(SectionPanel panel, Action action) {
            super(action);
            this.panel=panel;
            setMargin(new java.awt.Insets(0,14,0,14));
            setEnabled(false);
        }     
        public SectionPanel getSectionPanel() {
            return panel;
        }
    }
    
}
