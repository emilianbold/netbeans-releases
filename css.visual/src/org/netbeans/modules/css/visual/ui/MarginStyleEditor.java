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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

/*
 * BorderStyleEditor.java
 *
 * Created on October 13, 2004, 12:23 PM
 */

package org.netbeans.modules.css.visual.ui;

import org.netbeans.modules.css.visual.model.CssProperties;
import org.netbeans.modules.css.model.CssRuleContent;
import java.awt.BorderLayout;
import java.awt.FontMetrics;
import org.netbeans.modules.css.model.CssRuleContent;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.awt.event.*;
import javax.swing.table.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.*;
import org.openide.util.NbBundle;

/**
 * Margin Style editor.
 * @author  Winston Prakash
 * @version 1.0
 */
public class MarginStyleEditor extends StyleEditor {

    MarginDataTable marginDataTable = new MarginDataTable();

    /** Creates new form FontStyleEditor */
    public MarginStyleEditor() {
        setName("marginStyleEditor"); //NOI18N
        setDisplayName(NbBundle.getMessage(MarginStyleEditor.class, "MARGIN_EDITOR_DISPNAME"));
        initComponents();
        marginPanel.add(marginDataTable, BorderLayout.CENTER);
    }

    /**
     * Set the CSS Properties Values from the CssStyleData data structure
     * to the GUI components.
     */
    protected void setCssPropertyValues(CssRuleContent cssStyleData) {
        removeCssPropertyChangeListener();
        marginDataTable.setCssPropertyValues(cssStyleData);
        setCssPropertyChangeListener(cssStyleData);
        marginDataTable.validate();
        marginDataTable.repaint();
    }

    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        marginPanel = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout(0, 5));

        marginPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5), javax.swing.BorderFactory.createEtchedBorder()));
        marginPanel.setLayout(new java.awt.BorderLayout());
        add(marginPanel, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

/**
     * Margin Data Table that holds the margin style info
     * @author  Winston Prakash
     */
    public class MarginDataTable extends JPanel {

        /**
         * Holds value of property value.
         */
        private String value;
        MarginWidthField allMarginField;
        PaddingWidthField allPaddingField;
        MarginWidthField topMarginField;
        PaddingWidthField topPaddingField;
        MarginWidthField bottomMarginField;
        PaddingWidthField leftPaddingField;
        MarginWidthField rightMarginField;
        PaddingWidthField rightPaddingField;
        PaddingWidthField bottomPaddingField;
        MarginWidthField leftMarginField;

        public MarginDataTable() {
            setLayout(new FlexibleGridLayout(6, 3, 5, 5));
            FontMetrics fontMetrics = getFontMetrics(getFont());
            int aheight = (fontMetrics.getHeight() + 10) > 25 ? (fontMetrics.getHeight() + 10) : 25;
            setBackground(new JPanel().getBackground());
            initCells(aheight);
            getAccessibleContext().setAccessibleName(NbBundle.getMessage(MarginDataTable.class, "MARGIN_STYLE_TABLE_ACCESS_NAME"));
            getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MarginDataTable.class, "MARGIN_STYLE_TABLE_ACCESS_DESC"));
        }

        protected void setCssPropertyValues(CssRuleContent cssStyleData) {

            // Set the value for the Margin
            String topMargin = cssStyleData.getProperty(CssProperties.MARGIN_TOP);
            String bottomMargin = cssStyleData.getProperty(CssProperties.MARGIN_BOTTOM);
            String leftMargin = cssStyleData.getProperty(CssProperties.MARGIN_LEFT);
            String rightMargin = cssStyleData.getProperty(CssProperties.MARGIN_RIGHT);

            if ((topMargin != null) && (bottomMargin != null) && (leftMargin != null) && (rightMargin != null) && (topMargin.equals(bottomMargin)) && (topMargin.equals(leftMargin)) && (topMargin.equals(rightMargin))) {
                allMarginField.setMarginString(topMargin);
            } else {
                allMarginField.setMarginString(null);
                topMarginField.setMarginString(topMargin);
                bottomMarginField.setMarginString(bottomMargin);
                leftMarginField.setMarginString(leftMargin);
                rightMarginField.setMarginString(rightMargin);
            }

            // Set the value for the Padding
            String topPadding = cssStyleData.getProperty(CssProperties.PADDING_TOP);
            String bottomPadding = cssStyleData.getProperty(CssProperties.PADDING_BOTTOM);
            String leftPadding = cssStyleData.getProperty(CssProperties.PADDING_LEFT);
            String rightPadding = cssStyleData.getProperty(CssProperties.PADDING_RIGHT);

            if ((topPadding != null) && (bottomPadding != null) && (leftPadding != null) && (rightPadding != null) && (topPadding.equals(bottomPadding)) && (topPadding.equals(leftPadding)) && (topPadding.equals(rightPadding))) {
                allPaddingField.setPaddingString(topPadding);
            } else {
                allPaddingField.setPaddingString(null);
                topPaddingField.setPaddingString(topPadding);
                bottomPaddingField.setPaddingString(bottomPadding);
                leftPaddingField.setPaddingString(leftPadding);
                rightPaddingField.setPaddingString(rightPadding);
            }
        }

        public void initCells(int aheight) {
            JPanel colHeader1 = new JPanel();

            JPanel colHeader2 = new JPanel();
            colHeader2.add(new JLabel(org.openide.util.NbBundle.getMessage(MarginDataTable.class, "MARGIN_ALL")));

            JPanel colHeader3 = new JPanel();
            colHeader3.add(new JLabel(org.openide.util.NbBundle.getMessage(MarginDataTable.class, "PADDING")));

            JPanel rowHeader1 = new JPanel();
            rowHeader1.add(new JLabel(org.openide.util.NbBundle.getMessage(MarginDataTable.class, "MARGIN_PADDING_ALL")));

            JPanel rowHeader2 = new JPanel();
            rowHeader2.add(new JLabel(org.openide.util.NbBundle.getMessage(MarginDataTable.class, "MARGIN_PADDING_TOP")));

            JPanel rowHeader3 = new JPanel();
            rowHeader3.add(new JLabel(org.openide.util.NbBundle.getMessage(MarginDataTable.class, "MARGIN_PADDING_BOTTOM")));

            JPanel rowHeader4 = new JPanel();
            rowHeader4.add(new JLabel(org.openide.util.NbBundle.getMessage(MarginDataTable.class, "MARGIN_PADDING_LEFT")));

            JPanel rowHeader5 = new JPanel();
            rowHeader5.add(new JLabel(org.openide.util.NbBundle.getMessage(MarginDataTable.class, "MARGIN_PADDING_RIGHT")));

            // All Side Margin
            allMarginField = new MarginWidthField();
            allMarginField.setAccessibleName(NbBundle.getMessage(MarginDataTable.class, "ALL_SIDE_MARGIN_WIDTH_ACCESS_NAME"), NbBundle.getMessage(MarginDataTable.class, "ALL_SIDE_MARGIN_WIDTH_UNIT_ACCESS_NAME"));
            allMarginField.setAccessibleDescription(NbBundle.getMessage(MarginDataTable.class, "ALL_SIDE_MARGIN_WIDTH_ACCESS_DESC"), NbBundle.getMessage(MarginDataTable.class, "ALL_SIDE_MARGIN_WIDTH_UNIT_ACCESS_DESC"));
            allMarginField.addPropertyChangeListener("margin-width", new PropertyChangeListenerImpl() {

                //NOI18N
                public void propertyChange(PropertyChangeEvent evt) {
                    //cssPropertyChangeSupport().firePropertyChange(CssProperties.MARGIN, null, evt.getNewValue().toString());
                    topMarginField.setMarginString(evt.getNewValue().toString());
                    bottomMarginField.setMarginString(evt.getNewValue().toString());
                    leftMarginField.setMarginString(evt.getNewValue().toString());
                    rightMarginField.setMarginString(evt.getNewValue().toString());
                }
            });



            // All Side Margin
            allPaddingField = new PaddingWidthField();
            allPaddingField.setAccessibleName(NbBundle.getMessage(MarginDataTable.class, "ALL_SIDE_PADDING_WIDTH_ACCESS_NAME"), NbBundle.getMessage(MarginDataTable.class, "ALL_SIDE_PADDING_WIDTH_UNIT_ACCESS_NAME"));
            allPaddingField.setAccessibleDescription(NbBundle.getMessage(MarginDataTable.class, "ALL_SIDE_PADDING_WIDTH_ACCESS_DESC"), NbBundle.getMessage(MarginDataTable.class, "ALL_SIDE_PADDING_WIDTH_UNIT_ACCESS_DESC"));
            allPaddingField.addPropertyChangeListener("margin-width", new PropertyChangeListenerImpl() {

                //NOI18N
                public void propertyChange(PropertyChangeEvent evt) {
                    //cssPropertyChangeSupport().firePropertyChange(CssProperties.PADDING, null, evt.getNewValue().toString());
                    topPaddingField.setPaddingString(evt.getNewValue().toString());
                    bottomPaddingField.setPaddingString(evt.getNewValue().toString());
                    leftPaddingField.setPaddingString(evt.getNewValue().toString());
                    rightPaddingField.setPaddingString(evt.getNewValue().toString());
                }
            });


            // Top Side Margin
            topMarginField = new MarginWidthField();
            topMarginField.setAccessibleName(NbBundle.getMessage(MarginDataTable.class, "TOP_SIDE_MARGIN_WIDTH_ACCESS_NAME"), NbBundle.getMessage(MarginDataTable.class, "TOP_SIDE_MARGIN_WIDTH_UNIT_ACCESS_NAME"));
            topMarginField.setAccessibleDescription(NbBundle.getMessage(MarginDataTable.class, "TOP_SIDE_MARGIN_WIDTH_ACCESS_DESC"), NbBundle.getMessage(MarginDataTable.class, "TOP_SIDE_MARGIN_WIDTH_UNIT_ACCESS_DESC"));
            topMarginField.addPropertyChangeListener("margin-width", new PropertyChangeListenerImpl() {

                //NOI18N
                public void propertyChange(PropertyChangeEvent evt) {
                    cssPropertyChangeSupport().firePropertyChange(CssProperties.MARGIN_TOP, null, evt.getNewValue().toString());
                }
            });

            // Top Side Margin
            topPaddingField = new PaddingWidthField();
            topPaddingField.setAccessibleName(NbBundle.getMessage(MarginDataTable.class, "TOP_SIDE_PADDING_WIDTH_ACCESS_NAME"), NbBundle.getMessage(MarginDataTable.class, "TOP_SIDE_PADDING_WIDTH_UNIT_ACCESS_NAME"));
            topPaddingField.setAccessibleDescription(NbBundle.getMessage(MarginDataTable.class, "TOP_SIDE_PADDING_WIDTH_ACCESS_DESC"), NbBundle.getMessage(MarginDataTable.class, "TOP_SIDE_PADDING_WIDTH_UNIT_ACCESS_DESC"));
            topPaddingField.addPropertyChangeListener("margin-width", new PropertyChangeListenerImpl() {

                //NOI18N
                public void propertyChange(PropertyChangeEvent evt) {
                    cssPropertyChangeSupport().firePropertyChange(CssProperties.PADDING_TOP, null, evt.getNewValue().toString());
                }
            });

            // Bottom Side Margin
            bottomMarginField = new MarginWidthField();
            bottomMarginField.setAccessibleName(NbBundle.getMessage(MarginDataTable.class, "BOTTOM_SIDE_MARGIN_WIDTH_ACCESS_NAME"), NbBundle.getMessage(MarginDataTable.class, "BOTTOM_SIDE_MARGIN_WIDTH_UNIT_ACCESS_NAME"));
            bottomMarginField.setAccessibleDescription(NbBundle.getMessage(MarginDataTable.class, "BOTTOM_SIDE_MARGIN_WIDTH_ACCESS_DESC"), NbBundle.getMessage(MarginDataTable.class, "BOTTOM_SIDE_MARGIN_WIDTH_UNIT_ACCESS_DESC"));
            bottomMarginField.addPropertyChangeListener("margin-width", new PropertyChangeListenerImpl() {

                //NOI18N
                public void propertyChange(PropertyChangeEvent evt) {
                    cssPropertyChangeSupport().firePropertyChange(CssProperties.MARGIN_BOTTOM, null, evt.getNewValue().toString());
                }
            });

            // Bottom Side Margin
            bottomPaddingField = new PaddingWidthField();
            bottomPaddingField.setAccessibleName(NbBundle.getMessage(MarginDataTable.class, "BOTTOM_SIDE_PADDING_WIDTH_ACCESS_NAME"), NbBundle.getMessage(MarginDataTable.class, "BOTTOM_SIDE_PADDING_WIDTH_UNIT_ACCESS_NAME"));
            bottomPaddingField.setAccessibleDescription(NbBundle.getMessage(MarginDataTable.class, "BOTTOM_SIDE_PADDING_WIDTH_ACCESS_DESC"), NbBundle.getMessage(MarginDataTable.class, "BOTTOM_SIDE_PADDING_WIDTH_UNIT_ACCESS_DESC"));
            bottomPaddingField.addPropertyChangeListener("margin-width", new PropertyChangeListenerImpl() {

                //NOI18N
                public void propertyChange(PropertyChangeEvent evt) {
                    cssPropertyChangeSupport().firePropertyChange(CssProperties.PADDING_BOTTOM, null, evt.getNewValue().toString());
                }
            });

            // Left Side Margin
            leftMarginField = new MarginWidthField();
            leftMarginField.setAccessibleName(NbBundle.getMessage(MarginDataTable.class, "LEFT_SIDE_MARGIN_WIDTH_ACCESS_NAME"), NbBundle.getMessage(MarginDataTable.class, "LEFT_SIDE_MARGIN_WIDTH_UNIT_ACCESS_NAME"));
            leftMarginField.setAccessibleDescription(NbBundle.getMessage(MarginDataTable.class, "LEFT_SIDE_MARGIN_WIDTH_ACCESS_DESC"), NbBundle.getMessage(MarginDataTable.class, "LEFT_SIDE_MARGIN_WIDTH_UNIT_ACCESS_DESC"));
            leftMarginField.addPropertyChangeListener("margin-width", new PropertyChangeListenerImpl() {

                //NOI18N
                public void propertyChange(PropertyChangeEvent evt) {
                    cssPropertyChangeSupport().firePropertyChange(CssProperties.MARGIN_LEFT, null, evt.getNewValue().toString());
                }
            });

            // Left Side Margin
            leftPaddingField = new PaddingWidthField();
            leftPaddingField.setAccessibleName(NbBundle.getMessage(MarginDataTable.class, "LEFT_SIDE_PADDING_WIDTH_ACCESS_NAME"), NbBundle.getMessage(MarginDataTable.class, "LEFT_SIDE_PADDING_WIDTH_UNIT_ACCESS_NAME"));
            leftPaddingField.setAccessibleDescription(NbBundle.getMessage(MarginDataTable.class, "LEFT_SIDE_PADDING_WIDTH_ACCESS_DESC"), NbBundle.getMessage(MarginDataTable.class, "LEFT_SIDE_PADDING_WIDTH_UNIT_ACCESS_DESC"));
            leftPaddingField.addPropertyChangeListener("margin-width", new PropertyChangeListenerImpl() {

                //NOI18N
                public void propertyChange(PropertyChangeEvent evt) {
                    cssPropertyChangeSupport().firePropertyChange(CssProperties.PADDING_LEFT, null, evt.getNewValue().toString());
                }
            });

            // Left Side Margin
            rightMarginField = new MarginWidthField();
            rightMarginField.setAccessibleName(NbBundle.getMessage(MarginDataTable.class, "RIGHT_SIDE_MARGIN_WIDTH_ACCESS_NAME"), NbBundle.getMessage(MarginDataTable.class, "RIGHT_SIDE_MARGIN_WIDTH_UNIT_ACCESS_NAME"));
            rightMarginField.setAccessibleDescription(NbBundle.getMessage(MarginDataTable.class, "RIGHT_SIDE_MARGIN_WIDTH_ACCESS_DESC"), NbBundle.getMessage(MarginDataTable.class, "RIGHT_SIDE_MARGIN_WIDTH_UNIT_ACCESS_DESC"));
            rightMarginField.addPropertyChangeListener("margin-width", new PropertyChangeListenerImpl() {

                //NOI18N
                public void propertyChange(PropertyChangeEvent evt) {
                    cssPropertyChangeSupport().firePropertyChange(CssProperties.MARGIN_RIGHT, null, evt.getNewValue().toString());
                }
            });

            // Left Side Margin
            rightPaddingField = new PaddingWidthField();
            rightPaddingField.setAccessibleName(NbBundle.getMessage(MarginDataTable.class, "RIGHT_SIDE_PADDING_WIDTH_ACCESS_NAME"), NbBundle.getMessage(MarginDataTable.class, "RIGHT_SIDE_PADDING_WIDTH_UNIT_ACCESS_NAME"));
            rightPaddingField.setAccessibleDescription(NbBundle.getMessage(MarginDataTable.class, "RIGHT_SIDE_PADDING_WIDTH_ACCESS_DESC"), NbBundle.getMessage(MarginDataTable.class, "RIGHT_SIDE_PADDING_WIDTH_UNIT_ACCESS_DESC"));
            rightPaddingField.addPropertyChangeListener("margin-width", new PropertyChangeListenerImpl() {

                //NOI18N
                public void propertyChange(PropertyChangeEvent evt) {
                    cssPropertyChangeSupport().firePropertyChange(CssProperties.PADDING_RIGHT, null, evt.getNewValue().toString());
                }
            });

            add(colHeader1);
            add(colHeader2);
            add(colHeader3);
            add(rowHeader1);
            add(allMarginField);
            add(allPaddingField);
            add(rowHeader2);
            add(topMarginField);
            add(topPaddingField);
            add(rowHeader3);
            add(bottomMarginField);
            add(bottomPaddingField);
            add(rowHeader4);
            add(leftMarginField);
            add(leftPaddingField);
            add(rowHeader5);
            add(rightMarginField);
            add(rightPaddingField);
            int cnt = getComponentCount();
            int[] widths = {30, 100, 100};
            for (int i = 0; i < cnt; i++) {
                int awidth = widths[i % 3];
                getComponent(i).setPreferredSize(new Dimension(awidth, aheight));
                getComponent(i).setMinimumSize(new Dimension(awidth, aheight));
                getComponent(i).setMaximumSize(new Dimension(awidth, aheight));
            }
        }

        class PropertyChangeListenerImpl implements PropertyChangeListener {

            public void propertyChange(PropertyChangeEvent evt) {
            }
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel marginPanel;
    // End of variables declaration//GEN-END:variables
}