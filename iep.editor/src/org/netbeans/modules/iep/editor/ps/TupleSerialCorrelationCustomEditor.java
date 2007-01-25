/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.iep.editor.ps;

import org.netbeans.modules.iep.editor.designer.GuiConstants;
import org.netbeans.modules.iep.editor.model.AttributeMetadata;
import org.netbeans.modules.iep.editor.model.ModelManager;
import org.netbeans.modules.iep.editor.model.Plan;
import org.netbeans.modules.iep.editor.model.Schema;
import org.netbeans.modules.iep.editor.tcg.dialog.NotifyHelper;
import org.netbeans.modules.iep.editor.share.SharedConstants;
import org.netbeans.modules.iep.editor.tcg.table.DefaultMoveableRowTableModel;
import org.netbeans.modules.iep.editor.tcg.table.MoveableRowTable;
import org.netbeans.modules.iep.editor.tcg.model.TcgComponent;
import org.netbeans.modules.iep.editor.tcg.model.TcgProperty;
import org.netbeans.modules.iep.editor.tcg.ps.TcgComponentNodeProperty;
import org.netbeans.modules.iep.editor.tcg.ps.TcgComponentNodePropertyCustomizerState;
import org.netbeans.modules.iep.editor.tcg.ps.TcgComponentNodePropertyCustomizer;
import org.netbeans.modules.iep.editor.tcg.ps.TcgComponentNodePropertyEditor;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.dnd.DragGestureEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.NbBundle;

/**
 * TupleSerialCorrelationCustomEditor.java
 *
 * Created on November 10, 2006, 10:23 AM
 *
 * @author Bing Lu
 */
public class TupleSerialCorrelationCustomEditor extends TcgComponentNodePropertyEditor {
    private static final Logger mLog = Logger.getLogger(TupleSerialCorrelationCustomEditor.class.getName());
    
    /** Creates a new instance of TupleSerialCorrelationCustomEditor */
    public TupleSerialCorrelationCustomEditor() {
    }
    
    public boolean supportsCustomEditor() {
        return true;
    }
    
    public Component getCustomEditor() {
        if (mEnv != null) {
            return new MyCustomizer(mProperty, mEnv);
        }
        return new MyCustomizer(mProperty, mCustomizerState);
    }
    
    private static class MyCustomizer extends TcgComponentNodePropertyCustomizer implements SharedConstants {
        private TcgComponent mComponent;
        private PropertyPanel mNamePanel;
        private PropertyPanel mOutputSchemaNamePanel;
        private PropertyPanel mIncrementPanel;
        private PropertyPanel mSizePanel;
        private InputSchemaSelectionPanel mSelectionPanel;
        private DefaultMoveableRowTableModel mTableModel;
        private MoveableRowTable mTable;
        private Vector mColTitle;
        private JLabel mStatusLbl;
        
        public MyCustomizer(TcgComponentNodeProperty prop, PropertyEnv env) {
            super(prop, env);
        }
        
        public MyCustomizer(TcgComponentNodeProperty prop, TcgComponentNodePropertyCustomizerState customizerState) {
            super(prop, customizerState);
        }
        
        protected void initialize() {
            try {
                mComponent = mProperty.getProperty().getParentComponent();
                setLayout(new GridBagLayout());
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(3, 3, 3, 3);
                int gGridy = 0;
                
                // property pane
                gbc.gridx = 0;
                gbc.gridy = gGridy++;
                gbc.gridwidth = 1;
                gbc.gridheight = 1;
                gbc.anchor = GridBagConstraints.WEST;
                gbc.weightx = 1.0D;
                gbc.weighty = 0.0D;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                JPanel topPanel = createPropertyPanel();
                add(topPanel, gbc);
                
                // attribute pane
                gbc.gridx = 0;
                gbc.gridy = gGridy++;
                gbc.gridwidth = 1;
                gbc.gridheight = 1;
                gbc.anchor = GridBagConstraints.WEST;
                gbc.weightx = 1.0D;
                gbc.weighty = 1.0D;
                gbc.fill = GridBagConstraints.BOTH;
                JSplitPane attributePane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
                String msg = NbBundle.getMessage(DefaultCustomEditor.class, "CustomEditor.ATTRIBUTES");
                attributePane.setBorder(new TitledBorder(LineBorder.createGrayLineBorder(), msg, TitledBorder.LEFT, TitledBorder.TOP));
                add(attributePane, gbc);
                
                // left attribute pane
                ((JSplitPane)attributePane).setOneTouchExpandable(true);
                mSelectionPanel = new InputSchemaSelectionPanel((Plan)mProperty.getNode().getDoc(), mComponent);
                mSelectionPanel.setPreferredSize(new Dimension(150, 300));
                ((JSplitPane)attributePane).setLeftComponent(mSelectionPanel);
                mSelectionPanel.addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        MyCustomizer.this.updateTable();
                    }
                });
                
                // right attribute pane
                JScrollPane rightPane = new JScrollPane();
                rightPane.setPreferredSize(new Dimension(450, 300));
                attributePane.setRightComponent(rightPane);
                
                mColTitle = new Vector();
                mColTitle.add(NbBundle.getMessage(SelectPanel.class, "SelectPanel.ATTRIBUTE_NAME"));
                mColTitle.add(NbBundle.getMessage(SelectPanel.class, "SelectPanel.DATA_TYPE"));
                mColTitle.add(NbBundle.getMessage(SelectPanel.class, "SelectPanel.SIZE"));
                mColTitle.add(NbBundle.getMessage(SelectPanel.class, "SelectPanel.SCALE"));
                mTableModel = new DefaultMoveableRowTableModel();
                updateTable();
                mTable = new MoveableRowTable(mTableModel) {
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                    public void dragGestureRecognized(DragGestureEvent dge) {
                        return;
                    }
                };
                rightPane.getViewport().add(mTable);
                
                // status bar
                gbc.gridx = 0;
                gbc.gridy = gGridy++;
                gbc.gridwidth = 1;
                gbc.gridheight = 1;
                gbc.anchor = GridBagConstraints.WEST;
                gbc.weightx = 1.0D;
                gbc.weighty = 0.0D;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                mStatusLbl = new JLabel();
                mStatusLbl.setForeground(Color.RED);
                add(mStatusLbl, gbc);
            } catch(Exception e) {
                mLog.log(Level.SEVERE,
                        NbBundle.getMessage(DefaultCustomEditor.class, "CustomEditor.FAILED_TO_LAYOUT"),
                        e);
            }
        }
        
        private void updateTable() {
            try {
                Vector data = new Vector();
                int size = mSizePanel.getIntValue();
                List attributeList = mSelectionPanel.getSelectedAttributeList();
                Vector r;
                for (int i = 0; i < size; i++) {
                    for (int j = 0, J = attributeList.size(); j < J; j++) {
                        r = new Vector();
                        AttributeMetadata attr = (AttributeMetadata)attributeList.get(j);
                        r.add(attr.getAttributeName() + "_" + i);
                        r.add(attr.getAttributeType());
                        r.add(attr.getAttributeSize());
                        r.add(attr.getAttributeScale());
                        data.add(r);
                    }
                }
                mTableModel.setDataVector(data, mColTitle);
            } catch (Exception e) {
                mLog.log(Level.SEVERE,
                        NbBundle.getMessage(TupleSerialCorrelationCustomEditor.class, "CustomEditor.FAILED_UPDATE_TABLE"),
                        e);
            }
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
            TcgProperty nameProp = mComponent.getProperty(NAME_KEY);
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
            TcgProperty outputSchemaNameProp = mComponent.getProperty(OUTPUT_SCHEMA_ID_KEY);
            String outputSchemaNameStr = NbBundle.getMessage(DefaultCustomEditor.class, "CustomEditor.OUTPUT_SCHEMA_NAME");
            mOutputSchemaNamePanel = PropertyPanel.createSingleLineTextPanel(outputSchemaNameStr, outputSchemaNameProp, false);
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

            // increment
            TcgProperty incrementProp = mComponent.getProperty(INCREMENT_KEY);
            String incrementStr = NbBundle.getMessage(TupleSerialCorrelationCustomEditor.class, "CustomEditor.INCREMENT");
            mIncrementPanel = PropertyPanel.createIntNumberPanel(incrementStr, incrementProp, false);
            gbc.gridx = 3;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mIncrementPanel.component[0], gbc);
            
            gbc.gridx = 4;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mIncrementPanel.component[1], gbc);

            // size
            TcgProperty sizeProp = mComponent.getProperty(SIZE_KEY);
            String sizeStr = NbBundle.getMessage(TupleSerialCorrelationCustomEditor.class, "CustomEditor.SIZE");
            mSizePanel = PropertyPanel.createIntNumberPanel(sizeStr, sizeProp, false);
            ((JTextField)mSizePanel.input[0]).addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    MyCustomizer.this.updateTable();
                }
            });
            gbc.gridx = 3;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mSizePanel.component[0], gbc);
            
            gbc.gridx = 4;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mSizePanel.component[1], gbc);

            // glue
            gbc.gridx = 5;
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
        
        public void validateContent(PropertyChangeEvent evt) throws PropertyVetoException {
            try {
                Plan plan = (Plan)mProperty.getNode().getDoc();
                
                // name
                mNamePanel.validateContent(evt);
                String newName = mNamePanel.getStringValue();
                String name = mComponent.getProperty(NAME_KEY).getStringValue();
                if (!newName.equals(name) && plan.hasOperator(newName)) {
                    String msg = NbBundle.getMessage(DefaultCustomEditor.class,
                            "CustomEditor.NAME_IS_ALREADY_TAKEN_BY_ANOTHER_OPERATOR",
                            newName);
                    throw new PropertyVetoException(msg, evt);
                }
                
                // output schema name
                mOutputSchemaNamePanel.validateContent(evt);
                String newSchemaName = mOutputSchemaNamePanel.getStringValue();
                String schemaName = mComponent.getProperty(OUTPUT_SCHEMA_ID_KEY).getStringValue();
                if (!newSchemaName.equals(schemaName) && plan.hasSchema(newSchemaName)) {
                    String msg = NbBundle.getMessage(DefaultCustomEditor.class,
                            "CustomEditor.OUTPUT_SCHEMA_NAME_IS_ALREADY_TAKENBY_ANOTHER_SCHEMA",
                            newSchemaName);
                    throw new PropertyVetoException(msg, evt);
                }
                
                // increment
                mIncrementPanel.validateContent(evt);
                
                // size
                mSizePanel.validateContent(evt);
                if (mSizePanel.getIntValue() < 1) {
                    String msg = NbBundle.getMessage(DefaultCustomEditor.class,
                            "CustomEditor.SIZE_MUST_BE_GREATER_THAN_ZERO",
                            newSchemaName);
                    throw new PropertyVetoException(msg, evt);
                }
                
                // attributes selection
                if (mSelectionPanel.getSelectedAttributeList().size() == 0) {
                    String msg = NbBundle.getMessage(DefaultCustomEditor.class,
                            "CustomEditor.AT_LEAST_ONE_ATTRIBUTE_MUST_BE_SELECTED");
                    throw new PropertyVetoException(msg, evt);
                }
            } catch (Exception e) {
                String msg = e.getMessage();
                mStatusLbl.setText(msg);
                mStatusLbl.setIcon(GuiConstants.ERROR_ICON);
                throw new PropertyVetoException(msg, evt);
            }
        }
        
        private List getAttributeMetadataAsList() {
            List attributeMetadataList = new ArrayList();
            Vector r = mTableModel.getDataVector();
            for (int i = 0, I = r.size(); i < I; i++) {
                Vector c = (Vector) r.elementAt(i);
                attributeMetadataList.add(c.elementAt(0));
                attributeMetadataList.add(c.elementAt(1));
                attributeMetadataList.add(c.elementAt(2));
                attributeMetadataList.add(c.elementAt(3));
                attributeMetadataList.add("");
            }
            return attributeMetadataList;
        }
        
        public void setValue() {
            Plan plan = (Plan)mProperty.getNode().getDoc();
            Schema newSchema = null;
            mNamePanel.store();
            mIncrementPanel.store();
            mSizePanel.store();
            try {
                String newSchemaName = mOutputSchemaNamePanel.getStringValue();
                String schemaName = mComponent.getProperty(OUTPUT_SCHEMA_ID_KEY).getStringValue();
                boolean schemaExist = schemaName != null && !schemaName.trim().equals("");
                List attributes = getAttributeMetadataAsList();
                if (schemaExist) {
                    Schema schema = plan.getSchema(schemaName);
                    if (!newSchemaName.equals(schemaName)) {
                        newSchema = ModelManager.createSchema(newSchemaName);
                        newSchema.setAttributeMetadataAsList(attributes);
                        plan.addSchema(newSchema);
                        plan.removeSchema(schemaName);
                        mOutputSchemaNamePanel.store();
                        plan.getPropertyChangeSupport().firePropertyChange("Schema Name",
                                schemaName, newSchemaName);
                        
                    } else {
                        if (!schema.hasSameAttributeMetadata(attributes)) {
                            schema.setAttributeMetadataAsList(attributes);
                            plan.getPropertyChangeSupport().firePropertyChange("Schema Column Metadata",
                                    "old", "new");
                        }
                    }
                } else {
                    newSchema = ModelManager.createSchema(schemaName);
                    plan.addSchema(newSchema);
                    newSchema.setAttributeMetadataAsList(attributes);
                    TcgProperty prop = mComponent.getProperty(OUTPUT_SCHEMA_ID_KEY); 
                    if (prop != null) {
                        prop.setValue(newSchemaName);
                        mProperty.getNode().getView().updateTcgComponentNodeView();
                    }
                    plan.getPropertyChangeSupport().firePropertyChange("Schema",
                            null, newSchema);
                }
                List selectedAttrNameList = mSelectionPanel.getSelectedAttributeNameList();
                mComponent.getProperty(FROM_COLUMN_LIST_KEY).setValue(selectedAttrNameList);
            } catch (Exception e) {
                e.printStackTrace();
                NotifyHelper.reportError(e.getMessage());
            }
        }
    }
}
