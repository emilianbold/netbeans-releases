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

import java.awt.BorderLayout;
import java.awt.FontMetrics;
import org.netbeans.modules.css.visual.model.BorderModel;
import org.netbeans.modules.css.visual.model.CssProperties;
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
 * Borders Style editor.
 * @author  Winston Prakash
 * @version 1.0
 */
public class BorderStyleEditor extends StyleEditor {

    BorderDataTable borderDataTable = new BorderDataTable();

    /** Creates new form FontStyleEditor */
    public BorderStyleEditor() {
        setName("borderStyleEditor"); //NOI18N
        setDisplayName(NbBundle.getMessage(BorderStyleEditor.class, "BORDER_EDITOR_DISPNAME"));
        initComponents();
        borderPanel.add(borderDataTable, BorderLayout.CENTER);
    }

    /**
     * Set the CSS Properties Values from the CssStyleData data structure
     * to the GUI components.
     */
    protected void setCssPropertyValues(CssRuleContent cssStyleData) {
        removeCssPropertyChangeListener();
        borderDataTable.setCssPropertyValues(cssStyleData);
        setCssPropertyChangeListener(cssStyleData);
    }

    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        borderPanel = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout(0, 5));

        borderPanel.setLayout(new java.awt.BorderLayout());

        borderPanel.setBorder(new javax.swing.border.CompoundBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 5)), new javax.swing.border.EtchedBorder()));
        add(borderPanel, java.awt.BorderLayout.NORTH);

    }
    // </editor-fold>//GEN-END:initComponents

    class BorderDataTable extends JPanel {

        CssRuleContent cssStyleData;
        /**
         * Holds value of property value.
         */
        private String value;
        BorderModel borderModel = new BorderModel();
        JComboBox allStyleCombo;
        BorderWidthField allWidthField;
        ColorSelectionField allColorField;
        JComboBox topStyleCombo;
        BorderWidthField topWidthField;
        ColorSelectionField topColorField;
        JComboBox bottomStyleCombo;
        BorderWidthField bottomWidthField;
        ColorSelectionField bottomColorField;
        JComboBox leftStyleCombo;
        BorderWidthField leftWidthField;
        ColorSelectionField leftColorField;
        JComboBox rightStyleCombo;
        BorderWidthField rightWidthField;
        ColorSelectionField rightColorField;

        public BorderDataTable() {
            setLayout(new FlexibleGridLayout(6, 4, 5, 5));
            FontMetrics fontMetrics = getFontMetrics(getFont());
            int aheight = (fontMetrics.getHeight() + 10) > 25 ? (fontMetrics.getHeight() + 10) : 25;
            initCells(aheight);
            getAccessibleContext().setAccessibleName(NbBundle.getMessage(BorderDataTable.class, "BORDER_STYLE_TABLE_ACCESS_NAME"));
            getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(BorderDataTable.class, "BORDER_STYLE_TABLE_ACCESS_DESC"));
        }

        protected void setCssPropertyValues(CssRuleContent cssStyleData) {
            removeCssPropertyChangeListener();


            // Set the values for the Border Style
            String topStyle = cssStyleData.getProperty(CssProperties.BORDER_TOP_STYLE);
            String bottomStyle = cssStyleData.getProperty(CssProperties.BORDER_BOTTOM_STYLE);
            String leftStyle = cssStyleData.getProperty(CssProperties.BORDER_LEFT_STYLE);
            String rightStyle = cssStyleData.getProperty(CssProperties.BORDER_RIGHT_STYLE);


            if ((topStyle != null) && (bottomStyle != null) && (leftStyle != null) && (rightStyle != null) && (topStyle.equals(bottomStyle)) && (topStyle.equals(leftStyle)) && (topStyle.equals(rightStyle))) {
                allStyleCombo.setSelectedItem(topStyle);
            } else {
                allStyleCombo.setSelectedIndex(0);
                if (topStyle != null) {
                    topStyleCombo.setSelectedItem(topStyle);
                } else {
                    topStyleCombo.setSelectedIndex(0);
                }
                if (bottomStyle != null) {
                    bottomStyleCombo.setSelectedItem(bottomStyle);
                } else {
                    bottomStyleCombo.setSelectedIndex(0);
                }
                if (leftStyle != null) {
                    leftStyleCombo.setSelectedItem(leftStyle);
                } else {
                    leftStyleCombo.setSelectedIndex(0);
                }
                if (rightStyle != null) {
                    rightStyleCombo.setSelectedItem(rightStyle);
                } else {
                    rightStyleCombo.setSelectedIndex(0);
                }
            }

            // Set the value for the Border Width
            String topWidth = cssStyleData.getProperty(CssProperties.BORDER_TOP_WIDTH);
            String bottomWidth = cssStyleData.getProperty(CssProperties.BORDER_BOTTOM_WIDTH);
            String leftWidth = cssStyleData.getProperty(CssProperties.BORDER_LEFT_WIDTH);
            String rightWidth = cssStyleData.getProperty(CssProperties.BORDER_RIGHT_WIDTH);

            if ((topWidth != null) && (bottomWidth != null) && (leftWidth != null) && (rightWidth != null) && (topWidth.equals(bottomWidth)) && (topWidth.equals(leftWidth)) && (topWidth.equals(rightWidth))) {
                allWidthField.setWidthString(topWidth);
            } else {
                allWidthField.setWidthString(null);
                topWidthField.setWidthString(topWidth);
                bottomWidthField.setWidthString(bottomWidth);
                leftWidthField.setWidthString(leftWidth);
                rightWidthField.setWidthString(rightWidth);
            }

            // Set the value for the Border Width
            String topColor = cssStyleData.getProperty(CssProperties.BORDER_TOP_COLOR);
            String bottomColor = cssStyleData.getProperty(CssProperties.BORDER_BOTTOM_COLOR);
            String leftColor = cssStyleData.getProperty(CssProperties.BORDER_LEFT_COLOR);
            String rightColor = cssStyleData.getProperty(CssProperties.BORDER_RIGHT_COLOR);

            if ((topColor != null) && (bottomColor != null) && (leftColor != null) && (rightColor != null) && (topColor.equals(bottomColor)) && (topColor.equals(leftColor)) && (topColor.equals(rightColor))) {
                allColorField.setColorString(topColor);
            } else {
                allColorField.setColorString(null);
                topColorField.setColorString(topColor);
                bottomColorField.setColorString(bottomColor);
                leftColorField.setColorString(leftColor);
                rightColorField.setColorString(rightColor);
            }

            borderDataTable.validate();
            borderDataTable.repaint();
            setCssPropertyChangeListener(cssStyleData);
        }

        public void initCells(int aheight) {
            JLabel colHeader1 = new JLabel("");
            //colHeader1.add(new JLabel(NbBundle.getMessage(BorderDataTable.class, "BORDER_SIDE")));
            //setValueAt(colHeader1, 0, 0 );
            JPanel colHeader2 = new JPanel();
            colHeader2.add(new JLabel(NbBundle.getMessage(BorderDataTable.class, "BORDER_STYLE")));

            JPanel colHeader3 = new JPanel();
            colHeader3.add(new JLabel(NbBundle.getMessage(BorderDataTable.class, "BORDER_WIDTH")));

            JPanel colHeader6 = new JPanel();
            colHeader6.add(new JLabel(NbBundle.getMessage(BorderDataTable.class, "BORDER_COLOR")));


            JPanel rowHeader1 = new JPanel();
            rowHeader1.add(new JLabel(NbBundle.getMessage(BorderDataTable.class, "BORDER_ALL")));

            JPanel rowHeader2 = new JPanel();
            rowHeader2.add(new JLabel(NbBundle.getMessage(BorderDataTable.class, "BORDER_TOP")));

            JPanel rowHeader3 = new JPanel();
            rowHeader3.add(new JLabel(NbBundle.getMessage(BorderDataTable.class, "BORDER_BOTTOM")));

            JPanel rowHeader4 = new JPanel();
            rowHeader4.add(new JLabel(NbBundle.getMessage(BorderDataTable.class, "BORDER_LEFT")));

            JPanel rowHeader5 = new JPanel();
            rowHeader5.add(new JLabel(NbBundle.getMessage(BorderDataTable.class, "BORDER_RIGHT")));


            // All Side Style
            allStyleCombo = new JComboBox();
            allStyleCombo.getAccessibleContext().setAccessibleName(NbBundle.getMessage(BorderDataTable.class, "ALL_SIDE_BORDER_STYLE_ACCESS_NAME"));
            allStyleCombo.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(BorderDataTable.class, "ALL_SIDE_BORDER_STYLE_ACCESS_DESC"));
            allStyleCombo.setModel(borderModel.getStyleList());
            allStyleCombo.addItemListener(new ItemListener() {

                public void itemStateChanged(ItemEvent evt) {
                    //cssPropertyChangeSupport().firePropertyChange(CssStyleData.BORDER_STYLE, null, allStyleCombo.getSelectedItem().toString());
                    topStyleCombo.setSelectedIndex(allStyleCombo.getSelectedIndex());
                    bottomStyleCombo.setSelectedIndex(allStyleCombo.getSelectedIndex());
                    leftStyleCombo.setSelectedIndex(allStyleCombo.getSelectedIndex());
                    rightStyleCombo.setSelectedIndex(allStyleCombo.getSelectedIndex());
                }
            });


            // All Side Width
            allWidthField = new BorderWidthField();
            allWidthField.setAccessibleName(NbBundle.getMessage(BorderDataTable.class, "ALL_SIDE_BORDER_WIDTH_ACCESS_NAME"), NbBundle.getMessage(BorderDataTable.class, "ALL_SIDE_BORDER_WIDTH_UNIT_ACCESS_NAME"));
            allWidthField.setAccessibleDescription(NbBundle.getMessage(BorderDataTable.class, "ALL_SIDE_BORDER_WIDTH_ACCESS_DESC"), NbBundle.getMessage(BorderDataTable.class, "ALL_SIDE_BORDER_WIDTH_UNIT_ACCESS_DESC"));
            allWidthField.addPropertyChangeListener("border-width", new PropertyChangeListenerImpl() {

                //NOI18N
                public void propertyChange(PropertyChangeEvent evt) {
                    //cssPropertyChangeSupport().firePropertyChange(CssStyleData.BORDER_WIDTH, null, evt.getNewValue().toString());
                    topWidthField.setWidthString(evt.getNewValue().toString());
                    bottomWidthField.setWidthString(evt.getNewValue().toString());
                    leftWidthField.setWidthString(evt.getNewValue().toString());
                    rightWidthField.setWidthString(evt.getNewValue().toString());
                }
            });


            // All Side Width
            allColorField = new ColorSelectionField();
            allColorField.addPropertyChangeListener("color", new PropertyChangeListenerImpl() {

                //NOI18N
                public void propertyChange(PropertyChangeEvent evt) {
                    //cssPropertyChangeSupport().firePropertyChange(CssStyleData.BORDER_COLOR, null, allColorField.getColorString());
                    topColorField.setColorString(allColorField.getColorString());
                    bottomColorField.setColorString(allColorField.getColorString());
                    leftColorField.setColorString(allColorField.getColorString());
                    rightColorField.setColorString(allColorField.getColorString());
                }
            });


            // Top Side Style
            topStyleCombo = new JComboBox();
            topStyleCombo.getAccessibleContext().setAccessibleName(NbBundle.getMessage(BorderDataTable.class, "TOP_SIDE_BORDER_STYLE_ACCESS_NAME"));
            topStyleCombo.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(BorderDataTable.class, "TOP_SIDE_BORDER_STYLE_ACCESS_DESC"));
            topStyleCombo.setModel(borderModel.getStyleList());
            topStyleCombo.addItemListener(new ItemListener() {

                public void itemStateChanged(ItemEvent evt) {
                    cssPropertyChangeSupport().firePropertyChange(CssProperties.BORDER_TOP_STYLE, null, topStyleCombo.getSelectedItem().toString());
                }
            });


            // Top Side Width
            topWidthField = new BorderWidthField();
            topWidthField.setAccessibleName(NbBundle.getMessage(BorderDataTable.class, "TOP_SIDE_BORDER_WIDTH_ACCESS_NAME"), NbBundle.getMessage(BorderDataTable.class, "TOP_SIDE_BORDER_WIDTH_UNIT_ACCESS_NAME"));
            topWidthField.setAccessibleDescription(NbBundle.getMessage(BorderDataTable.class, "TOP_SIDE_BORDER_WIDTH_ACCESS_DESC"), NbBundle.getMessage(BorderDataTable.class, "TOP_SIDE_BORDER_WIDTH_UNIT_ACCESS_DESC"));
            topWidthField.addPropertyChangeListener("border-width", new PropertyChangeListenerImpl() {

                //NOI18N
                public void propertyChange(PropertyChangeEvent evt) {
                    cssPropertyChangeSupport().firePropertyChange(CssProperties.BORDER_TOP_WIDTH, null, evt.getNewValue().toString());
                }
            });


            // Top Side Color
            topColorField = new ColorSelectionField();
            topColorField.addPropertyChangeListener("color", new PropertyChangeListenerImpl() {

                //NOI18N
                public void propertyChange(PropertyChangeEvent evt) {
                    cssPropertyChangeSupport().firePropertyChange(CssProperties.BORDER_TOP_COLOR, null, topColorField.getColorString());
                }
            });



            // Bottom Side Style
            bottomStyleCombo = new JComboBox();
            bottomStyleCombo.getAccessibleContext().setAccessibleName(NbBundle.getMessage(BorderDataTable.class, "BOTTOM_SIDE_BORDER_STYLE_ACCESS_NAME"));
            bottomStyleCombo.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(BorderDataTable.class, "BOTTOM_SIDE_BORDER_STYLE_ACCESS_DESC"));
            bottomStyleCombo.setModel(borderModel.getStyleList());
            bottomStyleCombo.addItemListener(new ItemListener() {

                public void itemStateChanged(ItemEvent evt) {
                    cssPropertyChangeSupport().firePropertyChange(CssProperties.BORDER_BOTTOM_STYLE, null, bottomStyleCombo.getSelectedItem().toString());
                }
            });


            // Bottom Side Width
            bottomWidthField = new BorderWidthField();
            bottomWidthField.setAccessibleName(NbBundle.getMessage(BorderDataTable.class, "BOTTOM_SIDE_BORDER_WIDTH_ACCESS_NAME"), NbBundle.getMessage(BorderDataTable.class, "BOTTOM_SIDE_BORDER_WIDTH_UNIT_ACCESS_NAME"));
            bottomWidthField.setAccessibleDescription(NbBundle.getMessage(BorderDataTable.class, "BOTTOM_SIDE_BORDER_WIDTH_ACCESS_DESC"), NbBundle.getMessage(BorderDataTable.class, "BOTTOM_SIDE_BORDER_WIDTH_UNIT_ACCESS_DESC"));
            bottomWidthField.addPropertyChangeListener("border-width", new PropertyChangeListenerImpl() {

                //NOI18N
                public void propertyChange(PropertyChangeEvent evt) {
                    cssPropertyChangeSupport().firePropertyChange(CssProperties.BORDER_BOTTOM_WIDTH, null, evt.getNewValue().toString());
                }
            });


            // Bottom Side Width
            bottomColorField = new ColorSelectionField();
            bottomColorField.addPropertyChangeListener("color", new PropertyChangeListenerImpl() {

                //NOI18N
                public void propertyChange(PropertyChangeEvent evt) {
                    cssPropertyChangeSupport().firePropertyChange(CssProperties.BORDER_BOTTOM_COLOR, null, bottomColorField.getColorString());
                }
            });





            // Left Side Style
            leftStyleCombo = new JComboBox(new String[]{});
            leftStyleCombo.getAccessibleContext().setAccessibleName(NbBundle.getMessage(BorderDataTable.class, "LEFT_SIDE_BORDER_STYLE_ACCESS_NAME"));
            leftStyleCombo.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(BorderDataTable.class, "LEFT_SIDE_BORDER_STYLE_ACCESS_DESC"));
            leftStyleCombo.setModel(borderModel.getStyleList());
            leftStyleCombo.addItemListener(new ItemListener() {

                public void itemStateChanged(ItemEvent evt) {
                    cssPropertyChangeSupport().firePropertyChange(CssProperties.BORDER_LEFT_STYLE, null, leftStyleCombo.getSelectedItem().toString());
                }
            });


            // Left Side Width
            leftWidthField = new BorderWidthField();
            leftWidthField.setAccessibleName(NbBundle.getMessage(BorderDataTable.class, "LEFT_SIDE_BORDER_WIDTH_ACCESS_NAME"), NbBundle.getMessage(BorderDataTable.class, "LEFT_SIDE_BORDER_WIDTH_UNIT_ACCESS_NAME"));
            leftWidthField.setAccessibleDescription(NbBundle.getMessage(BorderDataTable.class, "LEFT_SIDE_BORDER_WIDTH_ACCESS_DESC"), NbBundle.getMessage(BorderDataTable.class, "LEFT_SIDE_BORDER_WIDTH_UNIT_ACCESS_DESC"));
            // Set the Left Side data
            leftWidthField.addPropertyChangeListener("border-width", new PropertyChangeListenerImpl() {

                //NOI18N
                public void propertyChange(PropertyChangeEvent evt) {
                    cssPropertyChangeSupport().firePropertyChange(CssProperties.BORDER_LEFT_WIDTH, null, evt.getNewValue().toString());
                }
            });


            // Left Side Width
            leftColorField = new ColorSelectionField();
            leftColorField.addPropertyChangeListener("color", new PropertyChangeListenerImpl() {

                //NOI18N
                public void propertyChange(PropertyChangeEvent evt) {
                    cssPropertyChangeSupport().firePropertyChange(CssProperties.BORDER_LEFT_COLOR, null, leftColorField.getColorString());
                }
            });



            // Right Side Style
            rightStyleCombo = new JComboBox();
            rightStyleCombo.getAccessibleContext().setAccessibleName(NbBundle.getMessage(BorderDataTable.class, "RIGHT_SIDE_BORDER_STYLE_ACCESS_NAME"));
            rightStyleCombo.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(BorderDataTable.class, "RIGHT_SIDE_BORDER_STYLE_ACCESS_DESC"));
            rightStyleCombo.setModel(borderModel.getStyleList());
            rightStyleCombo.addItemListener(new ItemListener() {

                public void itemStateChanged(ItemEvent evt) {
                    cssPropertyChangeSupport().firePropertyChange(CssProperties.BORDER_RIGHT_STYLE, null, rightStyleCombo.getSelectedItem().toString());
                }
            });



            // Right Side Width
            rightWidthField = new BorderWidthField();
            rightWidthField.setAccessibleName(NbBundle.getMessage(BorderDataTable.class, "RIGHT_SIDE_BORDER_WIDTH_ACCESS_NAME"), NbBundle.getMessage(BorderDataTable.class, "RIGHT_SIDE_BORDER_WIDTH_UNIT_ACCESS_NAME"));
            rightWidthField.setAccessibleDescription(NbBundle.getMessage(BorderDataTable.class, "RIGHT_SIDE_BORDER_WIDTH_ACCESS_DESC"), NbBundle.getMessage(BorderDataTable.class, "RIGHT_SIDE_BORDER_WIDTH_UNIT_ACCESS_DESC"));
            rightWidthField.addPropertyChangeListener("border-width", new PropertyChangeListenerImpl() {

                //NOI18N
                public void propertyChange(PropertyChangeEvent evt) {
                    cssPropertyChangeSupport().firePropertyChange(CssProperties.BORDER_RIGHT_WIDTH, null, evt.getNewValue().toString());
                }
            });


            // Right Side Width
            rightColorField = new ColorSelectionField();
            rightColorField.addPropertyChangeListener("color", new PropertyChangeListenerImpl() {

                //NOI18N
                public void propertyChange(PropertyChangeEvent evt) {
                    cssPropertyChangeSupport().firePropertyChange(CssProperties.BORDER_RIGHT_COLOR, null, rightColorField.getColorString());
                }
            });


            add(colHeader1);
            add(colHeader2);
            add(colHeader3);
            add(colHeader6);
            add(rowHeader1);
            add(allStyleCombo);
            add(allWidthField);
            add(allColorField);
            add(rowHeader2);
            add(topStyleCombo);
            add(topWidthField);
            add(topColorField);
            add(rowHeader3);
            add(bottomStyleCombo);
            add(bottomWidthField);
            add(bottomColorField);
            add(rowHeader4);
            add(leftStyleCombo);
            add(leftWidthField);
            add(leftColorField);
            add(rowHeader5);
            add(rightStyleCombo);
            add(rightWidthField);
            add(rightColorField);
            int cnt = getComponentCount();
            int[] widths = {50, 75, 125, 125};
            for (int i = 0; i < cnt; i++) {
                int awidth = widths[i % 4];
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
    private javax.swing.JPanel borderPanel;
    // End of variables declaration//GEN-END:variables
}