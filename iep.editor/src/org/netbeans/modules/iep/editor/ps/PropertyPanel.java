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


package org.netbeans.modules.iep.editor.ps;

import org.netbeans.modules.iep.editor.designer.JTextFieldFilter;
import org.netbeans.modules.iep.editor.share.SharedConstants;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.text.ParseException;
import java.util.Calendar;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import org.netbeans.modules.iep.model.Property;
import org.netbeans.modules.iep.model.lib.TcgProperty;
import org.netbeans.modules.iep.model.lib.TcgPropertyType;
import org.netbeans.modules.iep.model.lib.TcgType;
import org.openide.util.NbBundle;

/**
 * PropertyPanel.java
 *
 * Created on November 1, 2006, 1:52 PM
 *
 * @author Bing Lu
 */
public class PropertyPanel {
    protected String mLabel;
    protected Property mProperty;
    protected Property mProperty2;
    protected Object mTemp;
    protected Object mTemp2;
    protected JPanel panel;
    public JComponent component[];
    public JComponent input[];
    
//    public PropertyPanel(String label, TcgProperty prop) {
//        mLabel = label;
//        mProperty = prop;
//    }
    
//    public PropertyPanel(String label, TcgProperty prop, TcgProperty prop2) {
//        mLabel = label;
//        mProperty = prop;
//        mProperty2 = prop2;
//    }
//    
    public PropertyPanel(String label, Property prop) {
        mLabel = label;
        mProperty = prop;
    }
    
    public PropertyPanel(String label, Property prop, Property prop2) {
        mLabel = label;
        mProperty = prop;
        mProperty2 = prop2;
    }
    
    public JPanel getPanel() {
        return panel;
    }
    
    public String getStringValue() {
        return null;
    }
    
    public void setStringValue(String value) {
        return;
    }
    
    public boolean getBooleanValue() {
        return false;
    }
    
    public int getIntValue() {
        return 0;
    }
    
    public void store() {
        return;
    }
    
    public void validateContent(PropertyChangeEvent evt) throws PropertyVetoException {
        TcgPropertyType pt = mProperty.getPropertyType();
        String value =  getStringValue();
        // if value is required, it must be specified
        if (pt.isRequired()) {
            if ((value == null) || value.trim().equals("")) {
                String msg = NbBundle.getMessage(PropertyPanel.class,
                        "PropertyPanel.PROPERTY_CANNOT_BE_EMPTY",
                        mLabel);
                throw new PropertyVetoException(msg, evt);
            }
        }
        if ((value == null) || value.trim().equals("")) {
            return;
        }
        // if value is specified, then it must be valid
        if (pt.getType() == TcgType.INTEGER) {
            try {
                Integer.parseInt(value);
            } catch (NumberFormatException e) {
                String msg = NbBundle.getMessage(PropertyPanel.class,
                        "PropertyPanel.PROPERTY_IS_NOT_A_VALID_INTEGER",
                        mLabel);
                throw new PropertyVetoException(msg, evt);
            }
        }
        if (pt.getType() == TcgType.LONG) {
            try {
                Long.parseLong(value);
            } catch (NumberFormatException e) {
                String msg = NbBundle.getMessage(PropertyPanel.class,
                        "PropertyPanel.PROPERTY_IS_NOT_A_VALID_LONG_INTEGER",
                        mLabel);
                throw new PropertyVetoException(msg, evt);
            }
        }
        if (pt.getType() == TcgType.INTEGER) {
            try {
                Double.parseDouble(value);
            } catch (NumberFormatException e) {
                String msg = NbBundle.getMessage(PropertyPanel.class,
                        "PropertyPanel.PROPERTY_IS_NOT_A_VALID_NUMBER",
                        mLabel);
                throw new PropertyVetoException(msg, evt);
            }
        }
        if (pt.getType() == TcgType.DATE) {
            try {
                value = value + mTemp.toString();
                SharedConstants.DATE_FORMAT.parse(value);
            } catch (ParseException e) {
                String msg = NbBundle.getMessage(PropertyPanel.class,
                        "PropertyPanel.PROPERTY_IS_NOT_A_VALID_TIME",
                        mLabel);
                throw new PropertyVetoException(msg, evt);
            }
        }
    }
    
//    public static PropertyPanel createSingleLineTextPanel(String label, TcgProperty prop, JTextFieldFilter tff, boolean createPanel) {
//        PropertyPanel panel = new PropertyPanel(label, prop) {
//            public String getStringValue() {
//                return ((JTextField)input[0]).getText();
//            }
//            public void setStringValue(String value) {
//                ((JTextField)input[0]).setText(value);
//            }
//            public int getIntValue() {
//                try {
//                    return Integer.parseInt(getStringValue());
//                } catch (NumberFormatException e) {
//                    return 0;
//                }
//            }
//            public void store() {
//                String value = ((JTextField)input[0]).getText();
//                if (!mProperty.getStringValue().equals(value)) {
//                    mProperty.setStringValue(value);
//                }
//            }
//        };
//        JLabel nameLbl = new JLabel(label);
//        JTextField tf = new JTextField();
//        tf.setPreferredSize(new Dimension(140, 20));
//        tf.setMinimumSize(new Dimension(50, 20));
//        tf.setDocument(tff);
//        tf.setText(prop.getStringValue());
//        if (createPanel) {
//            panel.panel = new JPanel();
//            panel.panel.setLayout(new GridBagLayout());
//            GridBagConstraints gbc = new GridBagConstraints();
//            gbc.insets = new Insets(4, 3, 4, 3);
//            
//            gbc.gridx = 0;
//            gbc.gridy = 0;
//            gbc.gridwidth = 1;
//            gbc.gridheight = 1;
//            gbc.anchor = GridBagConstraints.WEST;
//            gbc.weightx = 0.0D;
//            gbc.weighty = 0.0D;
//            gbc.fill = GridBagConstraints.NONE;
//            panel.panel.add(nameLbl, gbc);
//            
//            gbc.gridx = 1;
//            gbc.gridy = 0;
//            gbc.gridwidth = 2;
//            gbc.gridheight = 1;
//            gbc.weightx = 1.0D;
//            gbc.weighty = 0.0D;
//            //gbc.fill = GridBagConstraints.NONE;
//            gbc.anchor = GridBagConstraints.WEST;
//            gbc.fill = GridBagConstraints.HORIZONTAL;
//            panel.panel.add(tf, gbc);
//            /*
//            gbc.gridx = 2;
//            gbc.gridy = 0;
//            gbc.gridwidth = 1;
//            gbc.gridheight = 1;
//            gbc.weightx = 0.0D;
//            gbc.weighty = 0.0D;
//            gbc.fill = GridBagConstraints.HORIZONTAL;
//            java.awt.Component glue = Box.createHorizontalGlue();
//        panel.panel.add(glue, gbc);
//             */
//            
//            gbc.gridx = 2;
//            gbc.gridy = 0;
//            gbc.gridwidth = 1;
//            gbc.gridheight = 1;
//            gbc.anchor = GridBagConstraints.WEST;
//            gbc.weightx = 1.0D;
//            gbc.weighty = 0.0D;
//            gbc.fill = GridBagConstraints.HORIZONTAL;
//            java.awt.Component struct = Box.createHorizontalStrut(10);
//            panel.panel.add(struct, gbc);
//           
//            
//        }
//        panel.component = new JComponent[2];
//        panel.component[0] = nameLbl;
//        panel.component[1] = tf;
//        
//        panel.input = new JComponent[1];
//        panel.input[0] = tf;
//        
//        return panel;
//    }
    
    public static PropertyPanel createSingleLineTextPanel(String label, Property prop, Document tff, boolean createPanel) {
        PropertyPanel panel = new PropertyPanel(label, prop) {
            public String getStringValue() {
                return ((JTextField)input[0]).getText();
            }
            public void setStringValue(String value) {
                ((JTextField)input[0]).setText(value);
            }
            public int getIntValue() {
                try {
                    return Integer.parseInt(getStringValue());
                } catch (NumberFormatException e) {
                    return 0;
                }
            }
            public void store() {
                String value = ((JTextField)input[0]).getText();
                if (!mProperty.getValue().equals(value)) {
                    mProperty.getModel().startTransaction();
                    mProperty.setValue(value);
                    mProperty.getModel().endTransaction();
                }
            }
        };
        JLabel nameLbl = new JLabel(label);
        JTextField tf = new JTextField();
        tf.setPreferredSize(new Dimension(140, 20));
        tf.setMinimumSize(new Dimension(50, 20));
        tf.setDocument(tff);
        tf.setText(prop.getValue());
        if (createPanel) {
            panel.panel = new JPanel();
            panel.panel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(4, 3, 4, 3);
            
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            panel.panel.add(nameLbl, gbc);
            
            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            gbc.gridheight = 1;
            gbc.weightx = 1.0D;
            gbc.weighty = 0.0D;
            //gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            panel.panel.add(tf, gbc);
            /*
            gbc.gridx = 2;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            java.awt.Component glue = Box.createHorizontalGlue();
        panel.panel.add(glue, gbc);
             */
            
            gbc.gridx = 2;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 1.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            java.awt.Component struct = Box.createHorizontalStrut(10);
            panel.panel.add(struct, gbc);
           
            
        }
        panel.component = new JComponent[2];
        panel.component[0] = nameLbl;
        panel.component[1] = tf;
        
        panel.input = new JComponent[1];
        panel.input[0] = tf;
        
        return panel;
    }
    
//    public static PropertyPanel createSingleLineTextPanel(String label, TcgProperty prop, boolean createPanel) {
//        return createSingleLineTextPanel(label, prop, JTextFieldFilter.newAlphaNumericUnderscore(), createPanel);
//    }
    
    public static PropertyPanel createSingleLineTextPanel(String label, Property prop, boolean createPanel) {
        return createSingleLineTextPanel(label, prop, JTextFieldFilter.newAlphaNumericUnderscore(), createPanel);
    }
    
    public static PropertyPanel createSingleLineTextPanelWithoutFilter(String label, Property prop, boolean createPanel ) {
        return createSingleLineTextPanel(label, prop, new PlainDocument(), createPanel);
    }
    
//    public static PropertyPanel createFloatNumberPanel(String label, TcgProperty prop, boolean createPanel) {
//        return createSingleLineTextPanel(label, prop, JTextFieldFilter.newFloat(), createPanel);
//    }
    
    public static PropertyPanel createFloatNumberPanel(String label, Property prop, boolean createPanel) {
        return createSingleLineTextPanel(label, prop, JTextFieldFilter.newFloat(), createPanel);
    }
    
//    public static PropertyPanel createIntNumberPanel(String label, TcgProperty prop, boolean createPanel) {
//        return createSingleLineTextPanel(label, prop, JTextFieldFilter.newNumeric(), createPanel);
//    }
    
    public static PropertyPanel createIntNumberPanel(String label, Property prop, boolean createPanel) {
        return createSingleLineTextPanel(label, prop, JTextFieldFilter.newNumeric(), createPanel);
    }
    
//    public static PropertyPanel createSmartSingleLineTextPanel(String label, TcgProperty prop, boolean truncateColumn, boolean createPanel) {
//        PropertyPanel panel = new PropertyPanel(label, prop) {
//            public String getStringValue() {
//                return ((JTextField)input[0]).getText();
//            }
//            public void setStringValue(String value) {
//                ((JTextField)input[0]).setText(value);
//            }
//            public void store() {
//                String value = ((JTextField)input[0]).getText();
//                if (mProperty.getType().isMultiple()) {
//                    value = value.replace(',', '\\');
//                }
//                if (!mProperty.getStringValue().equals(value)) {
//                    mProperty.setStringValue(value);
//                }
//            }
//        };
//        JLabel nameLbl = new JLabel(label);
//        SmartTextField tf = new SmartTextField(truncateColumn);
//        tf.setPreferredSize(new Dimension(200, 20));
//        String value = prop.getStringValue();
//        if (prop.getType().isMultiple()) {
//            value = value.replace('\\', ',');
//        }
//        tf.setText(value);
//        if (createPanel) {
//            panel.panel = new JPanel();
//            panel.panel.setLayout(new GridBagLayout());
//            GridBagConstraints gbc = new GridBagConstraints();
//            gbc.insets = new Insets(4, 3, 4, 3);
//            
//            gbc.gridx = 0;
//            gbc.gridy = 0;
//            gbc.gridwidth = 1;
//            gbc.gridheight = 1;
//            gbc.anchor = GridBagConstraints.WEST;
//            gbc.weightx = 0.0D;
//            gbc.weighty = 0.0D;
//            gbc.fill = GridBagConstraints.NONE;
//            panel.panel.add(nameLbl, gbc);
//            
//            gbc.gridx = 1;
//            gbc.gridy = 0;
//            gbc.gridwidth = 1;
//            gbc.gridheight = 1;
//            gbc.weightx = 1.0D;
//            gbc.weighty = 0.0D;
//            gbc.fill = GridBagConstraints.HORIZONTAL;
//            panel.panel.add(tf, gbc);
//            
//            gbc.gridx = 2;
//            gbc.gridy = 0;
//            gbc.gridwidth = 1;
//            gbc.gridheight = 1;
//            gbc.anchor = GridBagConstraints.WEST;
//            gbc.weightx = 0.0D;
//            gbc.weighty = 0.0D;
//            gbc.fill = GridBagConstraints.NONE;
//            java.awt.Component struct = Box.createHorizontalStrut(10);
//            panel.panel.add(struct, gbc);
//        }
//        panel.component = new JComponent[2];
//        panel.component[0] = nameLbl;
//        panel.component[1] = tf;
//        
//        panel.input = new JComponent[1];
//        panel.input[0] = tf;
//        
//        return panel;
//    }
    
    public static PropertyPanel createSmartSingleLineTextPanel(String label, Property prop, boolean truncateColumn, boolean createPanel) {
        PropertyPanel panel = new PropertyPanel(label, prop) {
            public String getStringValue() {
                return ((JTextField)input[0]).getText();
            }
            public void setStringValue(String value) {
                ((JTextField)input[0]).setText(value);
            }
            public void store() {
                String value = ((JTextField)input[0]).getText();
                if (mProperty.getPropertyType().isMultiple()) {
                    value = value.replace(',', '\\');
                }
                if (!mProperty.getValue().equals(value)) {
                    mProperty.getModel().startTransaction();
                    mProperty.setValue(value);
                    mProperty.getModel().endTransaction();
                }
            }
        };
        JLabel nameLbl = new JLabel(label);
        SmartTextField tf = new SmartTextField(truncateColumn);
        tf.setPreferredSize(new Dimension(200, 20));
        String value = prop.getValue();
        if (prop.getPropertyType().isMultiple()) {
            value = value.replace('\\', ',');
        }
        tf.setText(value);
        if (createPanel) {
            panel.panel = new JPanel();
            panel.panel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(4, 3, 4, 3);
            
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            panel.panel.add(nameLbl, gbc);
            
            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 1.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            panel.panel.add(tf, gbc);
            
            gbc.gridx = 2;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            java.awt.Component struct = Box.createHorizontalStrut(10);
            panel.panel.add(struct, gbc);
        }
        panel.component = new JComponent[2];
        panel.component[0] = nameLbl;
        panel.component[1] = tf;
        
        panel.input = new JComponent[1];
        panel.input[0] = tf;
        
        return panel;
    }
    
//    public static PropertyPanel createSmartMultiLineTextPanel(String label, TcgProperty prop) {
//        PropertyPanel panel = new PropertyPanel(label, prop) {
//            public String getStringValue() {
//                return ((JTextArea)input[0]).getText();
//            }
//            public void setStringValue(String value) {
//                ((JTextArea)input[0]).setText(value);
//            }
//            public void store() {
//                String value = ((JTextArea)input[0]).getText();
//                if (!mProperty.getStringValue().equals(value)) {
//                    mProperty.setStringValue(value);
//                }
//            }
//        };
//        panel.panel = new JPanel();
//        panel.panel.setLayout(new GridBagLayout());
//        GridBagConstraints gbc = new GridBagConstraints();
//        gbc.insets = new Insets(4, 3, 4, 3);
//        
//        gbc.gridx = 0;
//        gbc.gridy = 0;
//        gbc.gridwidth = 1;
//        gbc.gridheight = 1;
//        gbc.anchor = GridBagConstraints.WEST;
//        gbc.weightx = 0.0D;
//        gbc.weighty = 0.0D;
//        gbc.fill = GridBagConstraints.NONE;
//        JLabel nameLbl = new JLabel(label);
//        panel.panel.add(nameLbl, gbc);
//        
//        gbc.gridx = 0;
//        gbc.gridy = 1;
//        gbc.gridwidth = 1;
//        gbc.gridheight = 1;
//        gbc.anchor = GridBagConstraints.WEST;
//        gbc.weightx = 1.0D;
//        gbc.weighty = 1.0D;
//        gbc.fill = GridBagConstraints.BOTH;
//        JScrollPane scrollPane = new JScrollPane();
//        panel.panel.add(scrollPane, gbc);
//        SmartTextArea ta = new SmartTextArea(5, 40);
//        ta.setBorder(new EtchedBorder());
//        ta.setMargin(new Insets(3, 3, 3, 3));
//        if (prop.getType().isMultiple()) {
//            // Note that prop.getStringValue() returns
//            // a "\" separated single line text. But we want
//            // a newline separated multi-line text.
//            String s = prop.getStringValue();
//            s = s.replace("\\", "\n");
//            ta.setText(s);
//        } else {
//            ta.setText(prop.getStringValue());
//        }
//        scrollPane.getViewport().add(ta);
//        
//        gbc.gridx = 1;
//        gbc.gridy = 1;
//        gbc.gridwidth = 1;
//        gbc.gridheight = 1;
//        gbc.anchor = GridBagConstraints.WEST;
//        gbc.weightx = 0.0D;
//        gbc.weighty = 0.0D;
//        gbc.fill = GridBagConstraints.NONE;
//        java.awt.Component struct = Box.createHorizontalStrut(10);
//        panel.panel.add(struct, gbc);
//        
//        panel.input = new JComponent[1];
//        panel.input[0] = ta;
//        
//        return panel;
//    }
    
    public static PropertyPanel createSmartMultiLineTextPanel(String label, Property prop) {
        PropertyPanel panel = new PropertyPanel(label, prop) {
            public String getStringValue() {
                return ((JTextArea)input[0]).getText();
            }
            public void setStringValue(String value) {
                ((JTextArea)input[0]).setText(value);
            }
            public void store() {
                String value = ((JTextArea)input[0]).getText();
                if (!mProperty.getValue().equals(value)) {
                    mProperty.getModel().startTransaction();
                    mProperty.setValue(value);
                    mProperty.getModel().endTransaction();
                }
            }
        };
        panel.panel = new JPanel();
        panel.panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 3, 4, 3);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0.0D;
        gbc.weighty = 0.0D;
        gbc.fill = GridBagConstraints.NONE;
        JLabel nameLbl = new JLabel(label);
        panel.panel.add(nameLbl, gbc);
        
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0D;
        gbc.weighty = 1.0D;
        gbc.fill = GridBagConstraints.BOTH;
        JScrollPane scrollPane = new JScrollPane();
        panel.panel.add(scrollPane, gbc);
        SmartTextArea ta = new SmartTextArea(5, 40);
        ta.setBorder(new EtchedBorder());
        ta.setMargin(new Insets(3, 3, 3, 3));
        if (prop.getPropertyType().isMultiple()) {
            // Note that prop.getStringValue() returns
            // a "\" separated single line text. But we want
            // a newline separated multi-line text.
            String s = prop.getValue();
            s = s.replace("\\", "\n");
            ta.setText(s);
        } else {
            ta.setText(prop.getValue());
        }
        scrollPane.getViewport().add(ta);
        
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0.0D;
        gbc.weighty = 0.0D;
        gbc.fill = GridBagConstraints.NONE;
        java.awt.Component struct = Box.createHorizontalStrut(10);
        panel.panel.add(struct, gbc);
        
        panel.input = new JComponent[1];
        panel.input[0] = ta;
        
        return panel;
    }
    
//    public static PropertyPanel createCheckBoxPanel(String label, TcgProperty prop) {
//        PropertyPanel panel = new PropertyPanel(label, prop) {
//            public boolean getBooleanValue() {
//                return ((JCheckBox)input[0]).isSelected();
//            }
//            public void store() {
//                boolean value = ((JCheckBox)input[0]).isSelected();
//                if (!mProperty.getBoolValue() == value) {
//                    mProperty.setValue(value? Boolean.TRUE : Boolean.FALSE);
//                }
//            }
//        };
//        panel.panel = new JPanel();
//        panel.panel.setLayout(new GridBagLayout());
//        GridBagConstraints gbc = new GridBagConstraints();
//        gbc.insets = new Insets(4, 3, 4, 3);
//        
//        gbc.gridx = 0;
//        gbc.gridy = 0;
//        gbc.gridwidth = 1;
//        gbc.gridheight = 1;
//        gbc.anchor = GridBagConstraints.WEST;
//        gbc.weightx = 0.0D;
//        gbc.weighty = 0.0D;
//        gbc.fill = GridBagConstraints.NONE;
//        JCheckBox cb = new JCheckBox(label, prop.getBoolValue());
//        panel.panel.add(cb, gbc);
//        
//        gbc.gridx = 1;
//        gbc.gridy = 0;
//        gbc.gridwidth = 1;
//        gbc.gridheight = 1;
//        gbc.weightx = 1.0D;
//        gbc.weighty = 0.0D;
//        gbc.fill = GridBagConstraints.HORIZONTAL;
//        java.awt.Component glue = Box.createHorizontalGlue();
//        panel.panel.add(glue, gbc);
//        
//        panel.input = new JComponent[1];
//        panel.input[0] = cb;
//        
//        return panel;
//    }
    

    public static PropertyPanel createCheckBoxPanel(String label, 
                                                    Property prop) {
        PropertyPanel panel = new PropertyPanel(label, prop) {
            public boolean getBooleanValue() {
                return ((JCheckBox)input[0]).isSelected();
            }
            
            @Override
            public void setStringValue(String value) {
                boolean val = Boolean.parseBoolean(value);
                ((JCheckBox)input[0]).setSelected(val);
            }
            
            public void store() {
                boolean value = ((JCheckBox)input[0]).isSelected();
                if (!mProperty.getValue().equals(Boolean.valueOf(value).toString())) {
                    mProperty.getModel().startTransaction();
                    mProperty.setValue(value? Boolean.TRUE.toString() : Boolean.FALSE.toString());
                    mProperty.getModel().endTransaction();
                }
            }
        };
        panel.panel = new JPanel();
        panel.panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 3, 4, 3);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0.0D;
        gbc.weighty = 0.0D;
        gbc.fill = GridBagConstraints.NONE;
        JCheckBox cb = new JCheckBox(label, Boolean.valueOf(prop.getValue()).booleanValue());
        panel.panel.add(cb, gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 1.0D;
        gbc.weighty = 0.0D;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        java.awt.Component glue = Box.createHorizontalGlue();
        panel.panel.add(glue, gbc);
        
        panel.input = new JComponent[1];
        panel.input[0] = cb;
        
        return panel;
    }
    
    
//    public static PropertyPanel createComboBoxPanel(String label, TcgProperty prop, String[] values, boolean createPanel) {
//        PropertyPanel panel = new PropertyPanel(label, prop) {
//            public String getStringValue() {
//                return (String)((JComboBox)input[0]).getSelectedItem();
//            }
//            public void store() {
//                String value = getStringValue();
//                if (!mProperty.getStringValue().equals(value)) {
//                    mProperty.setStringValue(value);
//                }
//            }
//        };
//        JLabel nameLabel = new JLabel(label);
//        JComboBox cbb = new JComboBox(values);
//        // PreferredSize must be set o.w. failed validation will resize this field.
//        cbb.setPreferredSize(new Dimension(140, 20));
//        cbb.setMinimumSize(new Dimension(50, 20));
//
//        String value = prop.getStringValue();
//        for (int i = 0; i < values.length; i++) {
//            if (values[i].equals(value)) {
//                cbb.setSelectedItem(value);
//                break;
//            }
//        }
//        if (createPanel) {
//            panel.panel = new JPanel();
//            panel.panel.setLayout(new GridBagLayout());
//            GridBagConstraints gbc = new GridBagConstraints();
//            gbc.insets = new Insets(4, 3, 4, 3);
//            
//            gbc.gridx = 0;
//            gbc.gridy = 0;
//            gbc.gridwidth = 1;
//            gbc.gridheight = 1;
//            gbc.anchor = GridBagConstraints.WEST;
//            gbc.weightx = 0.0D;
//            gbc.weighty = 0.0D;
//            gbc.fill = GridBagConstraints.NONE;
//            panel.panel.add(nameLabel, gbc);
//            
//            gbc.gridx = 1;
//            gbc.gridy = 0;
//            gbc.gridwidth = 1;
//            gbc.gridheight = 1;
//            gbc.weightx = 0.0D;
//            gbc.weighty = 0.0D;
//            gbc.fill = GridBagConstraints.HORIZONTAL;
//            panel.panel.add(cbb, gbc);
//            
//            gbc.gridx = 2;
//            gbc.gridy = 0;
//            gbc.gridwidth = 1;
//            gbc.gridheight = 1;
//            gbc.weightx = 1.0D;
//            gbc.weighty = 0.0D;
//            gbc.fill = GridBagConstraints.HORIZONTAL;
//            java.awt.Component glue = Box.createHorizontalGlue();
//            panel.panel.add(glue, gbc);
//        }
//        panel.component = new JComponent[2];
//        panel.component[0] = nameLabel;
//        panel.component[1] = cbb;
//        
//        panel.input = new JComponent[1];
//        panel.input[0] = cbb;
//        
//        return panel;
//    }

    public static PropertyPanel createComboBoxPanel(String label, Property prop, String[] values, boolean createPanel) {
        //this api assumes that value and display names are same
        return createComboBoxPanel(label, prop, values, values, createPanel);
    }
    
    public static PropertyPanel createComboBoxPanel(String label, Property prop, String[] displayNames, String[] values, boolean createPanel) {
        return new ComboBoxPropertyPanel(label, prop, displayNames, values, createPanel);
    }
    
    private static Calendar CALENDAR = Calendar.getInstance();
//    public static PropertyPanel createDatePanel(String label, TcgProperty prop, boolean createPanel) {
//        PropertyPanel panel = new PropertyPanel(label, prop) {
//            public String getStringValue() {
//                return (String)((JTextField)input[0]).getText();
//            }
//            public void setStringValue(String value) {
//                ((JTextField)input[0]).setText(value);
//            }
//            public void store() {
//                String value = getStringValue() + mTemp.toString();
//                if (!mProperty.getStringValue().equals(value)) {
//                    mProperty.setStringValue(value);
//                }
//            }
//        };
//        JLabel nameLabel = new JLabel(label);
//        JTextField tf = new JTextField();
//        String value = prop.getStringValue();
//        panel.mTemp = value.substring(value.length()-5);
//        tf.setText(value.substring(0, value.length()-5));
//        tf.setPreferredSize(new Dimension(140, 20));
//        tf.setMinimumSize(new Dimension(50, 20));
//        String timeZone = CALENDAR.getTimeZone().getDisplayName();
//        JLabel zoneLbl = new JLabel(timeZone);
//        if (createPanel) {
//            panel.panel = new JPanel();
//            panel.panel.setLayout(new GridBagLayout());
//            GridBagConstraints gbc = new GridBagConstraints();
//            gbc.insets = new Insets(4, 3, 4, 3);
//            
//            gbc.gridx = 0;
//            gbc.gridy = 0;
//            gbc.gridwidth = 1;
//            gbc.gridheight = 1;
//            gbc.anchor = GridBagConstraints.WEST;
//            gbc.weightx = 0.0D;
//            gbc.weighty = 0.0D;
//            gbc.fill = GridBagConstraints.NONE;
//            panel.panel.add(nameLabel, gbc);
//            
//            gbc.gridx = 1;
//            gbc.gridy = 0;
//            gbc.gridwidth = 1;
//            gbc.gridheight = 1;
//            gbc.weightx = 0.0D;
//            gbc.weighty = 0.0D;
//            gbc.fill = GridBagConstraints.NONE;
//            panel.panel.add(tf, gbc);
//            
//            gbc.gridx = 2;
//            gbc.gridy = 0;
//            gbc.gridwidth = 1;
//            gbc.gridheight = 1;
//            gbc.weightx = 0.0D;
//            gbc.weighty = 0.0D;
//            gbc.fill = GridBagConstraints.NONE;
//            panel.panel.add(zoneLbl, gbc);
//            
//            gbc.gridx = 3;
//            gbc.gridy = 0;
//            gbc.gridwidth = 1;
//            gbc.gridheight = 1;
//            gbc.weightx = 1.0D;
//            gbc.weighty = 0.0D;
//            gbc.fill = GridBagConstraints.HORIZONTAL;
//            java.awt.Component glue = Box.createHorizontalGlue();
//            panel.panel.add(glue, gbc);
//        }
//        panel.component = new JComponent[3];
//        panel.component[0] = nameLabel;
//        panel.component[1] = tf;
//        panel.component[2] = zoneLbl;
//        panel.input = new JComponent[1];
//        panel.input[0] = tf;
//        
//        return panel;
//    }
    
    public static PropertyPanel createDatePanel(String label, Property prop, boolean createPanel) {
        PropertyPanel panel = new PropertyPanel(label, prop) {
            public String getStringValue() {
                return (String)((JTextField)input[0]).getText();
            }
            public void setStringValue(String value) {
                ((JTextField)input[0]).setText(value);
            }
            public void store() {
                String value = getStringValue() + mTemp.toString();
                if (!mProperty.getValue().equals(value)) {
                    mProperty.getModel().startTransaction();
                    mProperty.setValue(value);
                    mProperty.getModel().endTransaction();
                }
            }
        };
        JLabel nameLabel = new JLabel(label);
        JTextField tf = new JTextField();
        String value = prop.getValue();
        panel.mTemp = value.substring(value.length()-5);
        tf.setText(value.substring(0, value.length()-5));
        tf.setPreferredSize(new Dimension(140, 20));
        tf.setMinimumSize(new Dimension(50, 20));
        String timeZone = CALENDAR.getTimeZone().getDisplayName();
        JLabel zoneLbl = new JLabel(timeZone);
        if (createPanel) {
            panel.panel = new JPanel();
            panel.panel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(4, 3, 4, 3);
            
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            panel.panel.add(nameLabel, gbc);
            
            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            panel.panel.add(tf, gbc);
            
            gbc.gridx = 2;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            panel.panel.add(zoneLbl, gbc);
            
            gbc.gridx = 3;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.weightx = 1.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            java.awt.Component glue = Box.createHorizontalGlue();
            panel.panel.add(glue, gbc);
        }
        panel.component = new JComponent[3];
        panel.component[0] = nameLabel;
        panel.component[1] = tf;
        panel.component[2] = zoneLbl;
        panel.input = new JComponent[1];
        panel.input[0] = tf;
        
        return panel;
    }
    
//    public static PropertyPanel createDurationPanel(String label, TcgProperty size, TcgProperty unit, boolean createPanel) {
//        PropertyPanel panel = new PropertyPanel(label, size, unit) {
//            public String getStringValue() {
//                return ((JTextField)input[0]).getText();
//            }
//            public void setStringValue(String value) {
//                ((JTextField)input[0]).setText(value);
//            }
//            public void store() {
//                String size = getStringValue();
//                if (!mProperty.getStringValue().equals(size)) {
//                    mProperty.setStringValue(size);
//                }
//                String unit = (String)((JComboBox)input[1]).getSelectedItem();
//                if (!mProperty2.getStringValue().equals(unit)) {
//                    mProperty2.setStringValue(unit);
//                }
//            }
//        };
//        JLabel nameLabel = new JLabel(label);
//        JTextField tf = new JTextField();
//        tf.setPreferredSize(new Dimension(140, 20));
//        tf.setMinimumSize(new Dimension(50, 20));
//        //tf.setDocument(JTextFieldFilter.newFloat());
//        tf.setDocument(JTextFieldFilter.newFloatExp());
//        
//        tf.setText(size.getStringValue());
//        String[] values = new String[] {
//            SharedConstants.TIME_UNIT_SECOND,
//            SharedConstants.TIME_UNIT_MINUTE,
//            SharedConstants.TIME_UNIT_HOUR,
//            SharedConstants.TIME_UNIT_DAY,
//            SharedConstants.TIME_UNIT_WEEK,
//        };
//        JComboBox cbb = new JComboBox(values);
//        // PreferredSize must be set o.w. failed validation will resize this field.
//        cbb.setPreferredSize(new Dimension(80, 20));
//        cbb.setMinimumSize(new Dimension(30, 20));
//        
//        String value = unit.getStringValue();
//        for (int i = 0; i < values.length; i++) {
//            if (values[i].equals(value)) {
//                cbb.setSelectedItem(value);
//                break;
//            }
//        }
//        if (createPanel) {
//            panel.panel = new JPanel();
//            panel.panel.setLayout(new GridBagLayout());
//            GridBagConstraints gbc = new GridBagConstraints();
//            gbc.insets = new Insets(4, 3, 4, 3);
//            
//            gbc.gridx = 0;
//            gbc.gridy = 0;
//            gbc.gridwidth = 1;
//            gbc.gridheight = 1;
//            gbc.anchor = GridBagConstraints.WEST;
//            gbc.weightx = 0.0D;
//            gbc.weighty = 0.0D;
//            gbc.fill = GridBagConstraints.NONE;
//            panel.panel.add(nameLabel, gbc);
//            
//            gbc.gridx = 1;
//            gbc.gridy = 0;
//            gbc.gridwidth = 1;
//            gbc.gridheight = 1;
//            gbc.anchor = GridBagConstraints.WEST;
//            gbc.weightx = 0.0D;
//            gbc.weighty = 0.0D;
//            gbc.fill = GridBagConstraints.NONE;
//            panel.panel.add(tf, gbc);
//            
//            gbc.gridx = 2;
//            gbc.gridy = 0;
//            gbc.gridwidth = 1;
//            gbc.gridheight = 1;
//            gbc.anchor = GridBagConstraints.WEST;
//            gbc.weightx = 0.0D;
//            gbc.weighty = 0.0D;
//            gbc.fill = GridBagConstraints.NONE;
//            panel.panel.add(cbb, gbc);
//            
//            gbc.gridx = 3;
//            gbc.gridy = 0;
//            gbc.gridwidth = 1;
//            gbc.gridheight = 1;
//            gbc.anchor = GridBagConstraints.WEST;
//            gbc.weightx = 1.0D;
//            gbc.weighty = 0.0D;
//            gbc.fill = GridBagConstraints.HORIZONTAL;
//            java.awt.Component glue = Box.createHorizontalGlue();
//            panel.panel.add(glue, gbc);
//        }
//        panel.component = new JComponent[3];
//        panel.component[0] = nameLabel;
//        panel.component[1] = tf;
//        panel.component[2] = cbb;
//
//        panel.input = new JComponent[2];
//        panel.input[0] = tf;
//        panel.input[1] = cbb;
//        return panel;
//    }
//    
    public static PropertyPanel createDurationPanel(String label, Property size, Property unit, boolean createPanel) {
        PropertyPanel panel = new PropertyPanel(label, size, unit) {
            public String getStringValue() {
                return ((JTextField)input[0]).getText();
            }
            public void setStringValue(String value) {
                ((JTextField)input[0]).setText(value);
            }
            public void store() {
                String size = getStringValue();
                if (!mProperty.getValue().equals(size)) {
                    mProperty.getModel().startTransaction();
                    mProperty.setValue(size);
                    mProperty.getModel().endTransaction();
                }
                String unit = (String)((JComboBox)input[1]).getSelectedItem();
                if (!mProperty2.getValue().equals(unit)) {
                    mProperty.getModel().startTransaction();
                    mProperty2.setValue(unit);
                    mProperty.getModel().endTransaction();
                }
            }
        };
        JLabel nameLabel = new JLabel(label);
        JTextField tf = new JTextField();
        tf.setPreferredSize(new Dimension(140, 20));
        tf.setMinimumSize(new Dimension(50, 20));
        //tf.setDocument(JTextFieldFilter.newFloat());
        tf.setDocument(JTextFieldFilter.newFloatExp());
        
        tf.setText(size.getValue());
        String[] values = new String[] {
            SharedConstants.TIME_UNIT_SECOND,
            SharedConstants.TIME_UNIT_MINUTE,
            SharedConstants.TIME_UNIT_HOUR,
            SharedConstants.TIME_UNIT_DAY,
            SharedConstants.TIME_UNIT_WEEK,
        };
        JComboBox cbb = new JComboBox(values);
        // PreferredSize must be set o.w. failed validation will resize this field.
        cbb.setPreferredSize(new Dimension(80, 20));
        cbb.setMinimumSize(new Dimension(30, 20));
        
        String value = unit.getValue();
        for (int i = 0; i < values.length; i++) {
            if (values[i].equals(value)) {
                cbb.setSelectedItem(value);
                break;
            }
        }
        if (createPanel) {
            panel.panel = new JPanel();
            panel.panel.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(4, 3, 4, 3);
            
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            panel.panel.add(nameLabel, gbc);
            
            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            panel.panel.add(tf, gbc);
            
            gbc.gridx = 2;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            panel.panel.add(cbb, gbc);
            
            gbc.gridx = 3;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 1.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            java.awt.Component glue = Box.createHorizontalGlue();
            panel.panel.add(glue, gbc);
        }
        panel.component = new JComponent[3];
        panel.component[0] = nameLabel;
        panel.component[1] = tf;
        panel.component[2] = cbb;

        panel.input = new JComponent[2];
        panel.input[0] = tf;
        panel.input[1] = cbb;
        return panel;
    }
    
    static class ComboBoxPropertyPanel extends PropertyPanel {
            JComboBox mComboBox;
            
            private String[] mValues;
            private String[] mDisplayNames;
            private boolean mCreatePanel;
            
            
            ComboBoxPropertyPanel(String label, Property prop, String[] displayNames, String[] values, boolean createPanel) {
                super(label, prop);
                this.mDisplayNames = displayNames;
                this.mValues = values;
                this.mCreatePanel = createPanel;
                init();
            }
            
            private void init() {
                JLabel nameLabel = new JLabel(mLabel);
                mComboBox = new JComboBox(mDisplayNames);
                // PreferredSize must be set o.w. failed validation will resize this field.
                mComboBox.setPreferredSize(new Dimension(140, 20));
                mComboBox.setMinimumSize(new Dimension(50, 20));

                String value = mProperty.getValue();
                for (int i = 0; i < mValues.length; i++) {
                    if (mValues[i].equals(value)) {
                        mComboBox.setSelectedItem(mDisplayNames[i]);
                        break;
                    }
                }
                if (mCreatePanel) {
                    panel = new JPanel();
                    panel.setLayout(new GridBagLayout());
                    GridBagConstraints gbc = new GridBagConstraints();
                    gbc.insets = new Insets(4, 3, 4, 3);

                    gbc.gridx = 0;
                    gbc.gridy = 0;
                    gbc.gridwidth = 1;
                    gbc.gridheight = 1;
                    gbc.anchor = GridBagConstraints.WEST;
                    gbc.weightx = 0.0D;
                    gbc.weighty = 0.0D;
                    gbc.fill = GridBagConstraints.NONE;
                    panel.add(nameLabel, gbc);

                    gbc.gridx = 1;
                    gbc.gridy = 0;
                    gbc.gridwidth = 1;
                    gbc.gridheight = 1;
                    gbc.weightx = 0.0D;
                    gbc.weighty = 0.0D;
                    gbc.fill = GridBagConstraints.HORIZONTAL;
                    panel.add(mComboBox, gbc);

                    gbc.gridx = 2;
                    gbc.gridy = 0;
                    gbc.gridwidth = 1;
                    gbc.gridheight = 1;
                    gbc.weightx = 1.0D;
                    gbc.weighty = 0.0D;
                    gbc.fill = GridBagConstraints.HORIZONTAL;
                    java.awt.Component glue = Box.createHorizontalGlue();
                    panel.add(glue, gbc);
                }
                component = new JComponent[2];
                component[0] = nameLabel;
                component[1] = mComboBox;

                input = new JComponent[1];
                input[0] = mComboBox;


            }
            
            public String getStringValue() {
                String val = null;
                String displayName = (String)((JComboBox)input[0]).getSelectedItem();
                for (int i = 0; i < mDisplayNames.length; i++) {
                    String dName = mDisplayNames[i];
                    if(dName.equals(displayName)) {
                        val = mValues[i];
                        break;
                    }
                }
                
                return val;
            }

            @Override
            public void setStringValue(String value) {
                String displayName = null;
                for (int i = 0; i < mValues.length; i++) {
                    String val = mValues[i];
                    if(val.equals(value)) {
                        displayName = mDisplayNames[i];
                        break;
                    }
                }
                
                if(displayName != null) {
                    mComboBox.setSelectedItem(displayName);
                }
            }
            
            
            public void store() {
                String value = getStringValue();
                if (!mProperty.getValue().equals(value)) {
                    mProperty.getModel().startTransaction();
                    mProperty.setValue(value);
                    mProperty.getModel().endTransaction();
                }
            }
        }
    
}
