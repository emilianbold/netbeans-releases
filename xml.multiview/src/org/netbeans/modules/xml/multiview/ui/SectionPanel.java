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

import org.netbeans.modules.xml.multiview.Error;
import org.netbeans.modules.xml.multiview.cookies.ErrorComponentContainer;
import org.netbeans.modules.xml.multiview.cookies.LinkCookie;
import org.openide.nodes.Node;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

/**
 * @author mkuchtiak
 */
public class SectionPanel extends javax.swing.JPanel implements NodeSectionPanel, ErrorComponentContainer {

    private SectionView sectionView;
    private String title;
    private Node node;
    private boolean active;
    private CustomPanel customPanel;
    private Object key;
    private int index;

    private FocusListener sectionFocusListener = new FocusAdapter() {
        public void focusGained(FocusEvent e) {
            setActive(true);
        }
    };

    /**
     * Creates new form SectionContainer
     */

    public SectionPanel(SectionView sectionView, Node explorerNode, Object key) {
        this(sectionView, explorerNode, explorerNode.getDisplayName(), key);
    }

    public SectionPanel(SectionView sectionView, Node node, String title, Object key) {
        this.sectionView = sectionView;
        this.title = title;
        this.node = node;
        this.key = key;

        initComponents();
        filler.setBackground(SectionVisualTheme.getFillerColor());
        filler.setVisible(false);
        setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        titleButton.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
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
    }

    private void openCustomPanel() {
        closeCustomPanel(); // close previous custom panel if exists
        customPanel = sectionView.getCustomPanelFactory().createCustomPanel(key);
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
                popup.show(foldButton, e.getX(), e.getY());
            }
        });
        customPanel.addFocusListener(sectionFocusListener);
        add(customPanel, gridBagConstraints);
    }

    private void closeCustomPanel() {
        if (customPanel != null) {
            customPanel.removeFocusListener(sectionFocusListener);
            remove(customPanel);
            customPanel = null;
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
        foldButton.setSelected(false);
        //contentPanel.setVisible(true);
        openCustomPanel();
        filler.setVisible(true);
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
        setActive(true);
    }//GEN-LAST:event_titleButtonActionPerformed

    private void foldButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_foldButtonActionPerformed
        if (!foldButton.isSelected()) {
            openCustomPanel();
        } else {
            closeCustomPanel();
        }
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
        private SectionView sectionView;

        private boolean localFocusListenerInitialized = false;
        private FocusListener localFocusListener = new FocusListener() {
            public void focusGained(FocusEvent evt) {
                final FocusListener[] focusListeners = getFocusListeners();
                for (int i = 0; i < focusListeners.length; i++) {
                    focusListeners[i].focusGained(evt);
                }
            }

            public void focusLost(FocusEvent evt) {
                processFocusEvent(evt);
            }
        };

        public CustomPanel(SectionView sectionView) {
            this.sectionView = sectionView;
        }

        public synchronized void addFocusListener(FocusListener l) {
            super.addFocusListener(l);
            if (!localFocusListenerInitialized) {
                localFocusListenerInitialized = true;
                final Component[] components = getComponents();
                for (int i = 0; i < components.length; i++) {
                    Component component = components[i];
                    if (component.isFocusable() && !(component instanceof JLabel)) {
                        component.removeFocusListener(localFocusListener);
                        component.addFocusListener(localFocusListener);
                    }
                }
            }
        }

        public abstract javax.swing.JComponent getErrorComponent(String errorId);

        public abstract void setValue(javax.swing.JComponent source, Object value);

        public void documentChanged(javax.swing.text.JTextComponent source, String value) {}

        public void rollbackValue(javax.swing.text.JTextComponent source) {}

        public void addModifier(final javax.swing.JTextField tf) {
            tf.addFocusListener(new java.awt.event.FocusAdapter() {
                private String orgValue;

                public void focusGained(java.awt.event.FocusEvent evt) {
                    orgValue = tf.getText();
                }

                public void focusLost(java.awt.event.FocusEvent evt) {
                    if (!tf.getText().equals(orgValue)) {
                        setValue(tf, tf.getText());
                    }
                }
            });
        }

        public void addValidatee(final javax.swing.JTextField tf) {
            tf.getDocument().addDocumentListener(new TextListener(tf));
            tf.addFocusListener(new java.awt.event.FocusAdapter() {
                private String orgValue;

                public void focusGained(java.awt.event.FocusEvent evt) {
                    orgValue = tf.getText();
                }

                public void focusLost(java.awt.event.FocusEvent evt) {
                    Error error = sectionView.getErrorPanel().getError();
                    if (error != null && error.isEditError() && tf == error.getFocusableComponent()) {
                        if (Error.TYPE_WARNING == error.getSeverityLevel()) {
                            org.openide.DialogDescriptor desc = new RefreshSaveDialog(sectionView.getErrorPanel());
                            java.awt.Dialog dialog = org.openide.DialogDisplayer.getDefault().createDialog(desc);
                            dialog.show();
                            Integer opt = (Integer) desc.getValue();
                            if (opt.equals(RefreshSaveDialog.OPTION_FIX)) {
                                tf.requestFocus();
                            } else if (opt.equals(RefreshSaveDialog.OPTION_REFRESH)) {
                                rollbackValue(tf);
                                sectionView.validateView();
                            } else {
                                setValue(tf, tf.getText());
                                sectionView.validateView();
                            }
                        } else {
                            org.openide.DialogDescriptor desc = new RefreshDialog(sectionView.getErrorPanel());
                            java.awt.Dialog dialog = org.openide.DialogDisplayer.getDefault().createDialog(desc);
                            dialog.show();
                            Integer opt = (Integer) desc.getValue();
                            if (opt.equals(RefreshDialog.OPTION_FIX)) {
                                tf.requestFocus();
                            } else if (opt.equals(RefreshDialog.OPTION_REFRESH)) {
                                rollbackValue(tf);
                                sectionView.validateView();
                            }
                        }
                    } else {
                        if (!tf.getText().equals(orgValue)) {
                            setValue(tf, tf.getText());
                            sectionView.validateView();
                        }
                    }
                }
            });
        }

        private class TextListener implements javax.swing.event.DocumentListener {
            private javax.swing.JTextField tf;

            TextListener(javax.swing.JTextField tf) {
                this.tf = tf;
            }

            /**
             * Method from DocumentListener
             */
            public void changedUpdate(javax.swing.event.DocumentEvent evt) {
                update(evt);
            }

            /**
             * Method from DocumentListener
             */
            public void insertUpdate(javax.swing.event.DocumentEvent evt) {
                update(evt);
            }

            /**
             * Method from DocumentListener
             */
            public void removeUpdate(javax.swing.event.DocumentEvent evt) {
                update(evt);
            }

            private void update(javax.swing.event.DocumentEvent evt) {
                try {
                    String text = evt.getDocument().getText(0, evt.getDocument().getLength());
                    documentChanged(tf, text);
                } catch (javax.swing.text.BadLocationException ex) {
                }
            }
        }

    }

    public void setKey(Object key) {
        this.key = key;
    }

    public Object getKey() {
        return key;
    }

    public JComponent getErrorComponent(String errorId) {
        if (customPanel != null) {
            return customPanel.getErrorComponent(errorId);
        }
        return null;
    }

    CustomPanel getCustomPanel() {
        return customPanel;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }
}
