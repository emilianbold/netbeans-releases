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

import org.netbeans.modules.iep.editor.model.NameGenerator;
import org.netbeans.modules.iep.editor.tcg.ps.TcgComponentNodeProperty;
import org.netbeans.modules.iep.editor.tcg.ps.TcgComponentNodePropertyCustomizerState;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import org.netbeans.modules.iep.editor.tcg.dialog.NotifyHelper;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.Property;
import org.netbeans.modules.iep.model.lib.TcgProperty;
import org.netbeans.modules.iep.model.lib.TcgPropertyType;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.NbBundle;

/**
 * MinusCustomEditor.java
 *
 * Created on Febuary 06, 2007, 10:23 AM
 *
 * @author Bing Lu
 */
public class MinusCustomEditor extends DefaultCustomEditor {
    private static final Logger mLog = Logger.getLogger(TimeBasedWindowCustomEditor.class.getName());
    
    /** Creates a new instance of MinusCustomEditor */
    public MinusCustomEditor() {
        super();
    }
    
    public Component getCustomEditor() {
        if (mEnv != null) {
            return new MyCustomizer(getPropertyType(), getOperatorComponent(), mEnv);
        }
        return new MyCustomizer(getPropertyType(), getOperatorComponent(), mCustomizerState);
    }
    
    private static class MyCustomizer extends DefaultCustomizer {
        protected JComboBox mSubtractCbb;
        protected JTextField mExpTf;
        protected LinkedList mInputIdList;
        protected LinkedList mInputNameList;
        protected String mSubtractFromName;
        
        public MyCustomizer(TcgPropertyType propertyType, OperatorComponent component, PropertyEnv env) {
            super(propertyType, component, env);
        }
        
        public MyCustomizer(TcgPropertyType propertyType, OperatorComponent component, TcgComponentNodePropertyCustomizerState customizerState) {
            super(propertyType, component, customizerState);
        }
        
        protected JPanel createPropertyPanel() throws Exception {
            JPanel pane = new JPanel();
            String msg = NbBundle.getMessage(DefaultCustomEditor.class, "CustomEditor.DETAILS");
            pane.setBorder(new CompoundBorder(
                    new TitledBorder(LineBorder.createGrayLineBorder(), msg, TitledBorder.LEFT, TitledBorder.TOP),
                    BorderFactory.createEmptyBorder(3, 3, 3, 3)));
            pane.setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(3, 3, 3, 3);
            
            // name
            Property nameProp = mComponent.getProperty(NAME_KEY);
            String nameStr = NbBundle.getMessage(DefaultCustomEditor.class, "CustomEditor.NAME");
            mNamePanel = PropertyPanel.createSingleLineTextPanel(nameStr, nameProp, false);
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mNamePanel.component[0], gbc);
            
            gbc.gridx = 1;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mNamePanel.component[1], gbc);
            
            // output schema
            Property outputSchemaNameProp = mComponent.getProperty(OUTPUT_SCHEMA_ID_KEY);
            String outputSchemaNameStr = NbBundle.getMessage(DefaultCustomEditor.class, "CustomEditor.OUTPUT_SCHEMA_NAME");
            mOutputSchemaNamePanel = PropertyPanel.createSingleLineTextPanel(outputSchemaNameStr, outputSchemaNameProp, false);
            if (mIsSchemaOwner) {
                if (mOutputSchemaNamePanel.getStringValue() == null || mOutputSchemaNamePanel.getStringValue().trim().equals("")) {
                    IEPModel model = mComponent.getModel();
                    String schemaName = NameGenerator.generateSchemaName(model.getPlanComponent().getSchemaComponentContainer());
                    mOutputSchemaNamePanel.setStringValue(schemaName);
                }
            }else {
                ((JTextField)mOutputSchemaNamePanel.input[0]).setEditable(false);
            }
            gbc.gridx = 0;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mOutputSchemaNamePanel.component[0], gbc);
            
            gbc.gridx = 1;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mOutputSchemaNamePanel.component[1], gbc);
            
            // struct
            gbc.gridx = 2;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(Box.createHorizontalStrut(20), gbc);
            
            //Subtract from
            String subtractStr = NbBundle.getMessage(TimeBasedWindowCustomEditor.class, "MinusCustomEditor.SUBTRACT_FROM");
            JLabel subtractLabel = new JLabel(subtractStr);
            gbc.gridx = 3;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(subtractLabel, gbc);
            
            
            List<OperatorComponent> inputs = mComponent.getInputOperatorList();
            mInputIdList = new LinkedList();
            mInputNameList = new LinkedList();
            String[] inputNames = new String[inputs.size()];
            int j = 0;
            
            Iterator<OperatorComponent> it = inputs.iterator();
            while(it.hasNext()) {
                OperatorComponent input = it.next();
                String id = input.getId();
                String inputName = input.getDisplayName();
                mInputIdList.add(id);
                mInputNameList.add(inputName);
                inputNames[j] = inputName;
                j++;
            }
            Arrays.sort(inputNames); // display a sorted list in the combobox
            
            String subtractFromId = mComponent.getProperty(SUBTRACT_FROM_KEY).getValue();
            if (subtractFromId != null) {
                subtractFromId = subtractFromId.trim();
            }
            int idx = mInputIdList.indexOf(subtractFromId);
            if (idx < 0) {
                if (mInputNameList.size() > 0) { // choose the first one by default
                    mSubtractFromName = (String)mInputNameList.get(0);
                } else {
                    mSubtractFromName = null; // subtractFromId is either invalid or undefined
                }
            } else {
                mSubtractFromName = (String)mInputNameList.get(idx);
            }
            
            mSubtractCbb = new JComboBox(inputNames);
            // PreferredSize must be set o.w. failed validation will resize this field.
            mSubtractCbb.setPreferredSize(new Dimension(160, 20));
            mSubtractCbb.setEditable(false);
            if (mSubtractFromName != null) {
                mSubtractCbb.setSelectedItem(mSubtractFromName);
            }
            mSubtractCbb.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    mSubtractFromName = (String)mSubtractCbb.getSelectedItem();
                    StringBuffer sb = new StringBuffer();
                    sb.append(mSubtractFromName);
                    for (int i = 0; i < mInputNameList.size(); i++) {
                        String inputName = (String)mInputNameList.get(i);
                        if (!inputName.equals(mSubtractFromName)) {
                            sb.append(" - ");
                            sb.append(inputName);
                        }
                    }
                    String exp = sb.toString();
                    mExpTf.setText(exp);
                    mExpTf.setCaretPosition(0);
                }
            });
            gbc.gridx = 4;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mSubtractCbb, gbc);
            
            gbc.gridx = 5;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 1.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            pane.add(Box.createHorizontalGlue(), gbc);
            
            // Expression
            String expStr = NbBundle.getMessage(TimeBasedWindowCustomEditor.class, "MinusCustomEditor.EXPRESSION");
            JLabel expLabel = new JLabel(expStr);
            gbc.gridx = 3;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(expLabel, gbc);
            
            mExpTf = new JTextField();
            mExpTf.setPreferredSize(new Dimension(160, 20));
            mExpTf.setEditable(false);
            if (mSubtractFromName != null) {
                StringBuffer sb = new StringBuffer();
                sb.append(mSubtractFromName);
                for (int i = 0; i < mInputNameList.size(); i++) {
                    String inputName = (String)mInputNameList.get(i);
                    if (!inputName.equals(mSubtractFromName)) {
                        sb.append(" - ");
                        sb.append(inputName);
                    }
                }
                String exp = sb.toString();
                mExpTf.setText(exp);
                mExpTf.setCaretPosition(0);
            }
            gbc.gridx = 4;
            gbc.gridy = 1;
            gbc.gridwidth = 2;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 1.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            pane.add(mExpTf, gbc);

            return pane;
        }
        
        public void validateContent(PropertyChangeEvent evt) throws PropertyVetoException {
            super.validateContent(evt);
            if (mInputNameList.size() < 2) {
                return;
            }
            if (mSubtractFromName == null) {
                String msg = NbBundle.getMessage(MinusCustomEditor.class,
                        "MinusCustomEditor.SUBTRACT_FROM_PROPERTY_MUST_BE_DEFINED");
                throw new PropertyVetoException(msg, evt);
            }
            int idx = mInputNameList.indexOf(mSubtractFromName);
            if (idx < 0) {
                String msg = NbBundle.getMessage(MinusCustomEditor.class,
                        "MinusCustomEditor.SUBTRACT_FROM_MUST_TAKE_ONE_OF_INPUT_NAMES");
                throw new PropertyVetoException(msg, evt);
            }
        }
        
        public void setValue() {
            super.setValue();
            try {
                if (mInputNameList.size() < 2) {
                    return;
                }
                int idx = mInputNameList.indexOf(mSubtractFromName);
                String subtractFrom = (String)mInputIdList.get(idx);
                mComponent.getModel().startTransaction();
                mComponent.getProperty(SUBTRACT_FROM_KEY).setValue(subtractFrom);
                mComponent.getModel().endTransaction();
            } catch (Exception e) {
                e.printStackTrace();
                NotifyHelper.reportError(e.getMessage());
            }
        }
        
    }
}
