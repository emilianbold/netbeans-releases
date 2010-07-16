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
import org.netbeans.modules.iep.editor.ps.InvokeStreamCustomEditor.MyTableModel;
import org.netbeans.modules.tbls.editor.ps.TcgComponentNodePropertyCustomizerState;
import org.netbeans.modules.tbls.editor.table.DefaultMoveableRowTableModel;
import org.netbeans.modules.tbls.editor.table.ReadOnlyNoExpressionDefaultMoveableRowTableModel;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.Property;
import org.netbeans.modules.tbls.model.TcgPropertyType;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.NbBundle;

/**
 * DynamicOutputCustomEditor.java
 *
 * Created on November 10, 2006, 10:23 AM
 *
 * @author Bing Lu
 */
public class DynamicOutputCustomEditor extends DefaultCustomEditor {

    private static final Logger mLog = Logger.getLogger(DynamicOutputCustomEditor.class.getName());

    /** Creates a new instance of StaticIOCustomEditor */
    public DynamicOutputCustomEditor() {
        super();
    }

    public Component getCustomEditor() {
        if (mEnv != null) {
            return new MyCustomizer(getPropertyType(), getOperatorComponent(), mEnv);
        }
        return new MyCustomizer(getPropertyType(), getOperatorComponent(), mCustomizerState);
    }

    private static class MyCustomizer extends DefaultCustomizer {

        protected PropertyPanel mIncludeTimestampPanel;

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
            Property nameProp = mComponent.getProperty(PROP_NAME);
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
            Property outputSchemaNameProp = mComponent.getProperty(PROP_OUTPUT_SCHEMA_ID);
            String outputSchemaNameStr = NbBundle.getMessage(DefaultCustomEditor.class, "CustomEditor.OUTPUT_SCHEMA_NAME");
            mOutputSchemaNamePanel = PropertyPanel.createSingleLineTextPanel(outputSchemaNameStr, outputSchemaNameProp, false);
            if (mIsSchemaOwner) {
                if (mOutputSchemaNamePanel.getStringValue() == null || mOutputSchemaNamePanel.getStringValue().trim().equals("")) {
                    IEPModel model = mComponent.getModel();
                    String schemaName = NameGenerator.generateSchemaName(model.getPlanComponent().getSchemaComponentContainer());
                    mOutputSchemaNamePanel.setStringValue(schemaName);
                }
            } else {
                ((JTextField) mOutputSchemaNamePanel.input[0]).setEditable(false);
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

            // multiple events
            Property includeTimestampProp = mComponent.getProperty(PROP_INCLUDE_TIMESTAMP);
            String includeTimestampStr = NbBundle.getMessage(DynamicOutputCustomEditor.class, "CustomEditor.INCLUDE_TIMESTAMP");
            mIncludeTimestampPanel = PropertyPanel.createCheckBoxPanel(includeTimestampStr, includeTimestampProp);
            gbc.gridx = 3;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mIncludeTimestampPanel.panel, gbc);
            //accessibility
            mIncludeTimestampPanel.input[0].getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DynamicOutputCustomEditor.class, "ACSD_CustomEditor.INCLUDE_TIMESTAMP"));
            ((JCheckBox) mIncludeTimestampPanel.input[0]).setToolTipText(NbBundle.getMessage(DynamicOutputCustomEditor.class, "TOOLTIP_CustomEditor.INCLUDE_TIMESTAMP"));



            // glue
            gbc.gridx = 4;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 1.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            pane.add(Box.createHorizontalGlue(), gbc);

            return pane;
        }

        @Override
        protected SelectPanel createSelectPanel(OperatorComponent component) {
            return new MySelectPanel(component);
        }

        public void validateContent(PropertyChangeEvent evt) throws PropertyVetoException {
            super.validateContent(evt);
        }

        public void setValue() {
            super.setValue();
            mIncludeTimestampPanel.store();
        }
    }

    static class MySelectPanel extends SelectPanel {

        public MySelectPanel(OperatorComponent component) {
            super(component);
        }

        @Override
        protected DefaultMoveableRowTableModel createTableModel() {
            return new ReadOnlyNoExpressionDefaultMoveableRowTableModel();
        }
    }
}
