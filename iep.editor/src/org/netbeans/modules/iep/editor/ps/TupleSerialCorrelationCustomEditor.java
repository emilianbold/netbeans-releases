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

import org.netbeans.modules.iep.editor.designer.GuiConstants;
import org.netbeans.modules.iep.editor.model.NameGenerator;
import org.netbeans.modules.tbls.editor.dialog.NotifyHelper;
import org.netbeans.modules.iep.model.share.SharedConstants;
import org.netbeans.modules.tbls.editor.table.DefaultMoveableRowTableModel;
import org.netbeans.modules.tbls.editor.table.MoveableRowTable;
import org.netbeans.modules.tbls.editor.table.ReadOnlyNoExpressionDefaultMoveableRowTableModel;
import org.netbeans.modules.tbls.editor.ps.TcgComponentNodePropertyCustomizerState;
import org.netbeans.modules.tbls.editor.ps.TcgComponentNodePropertyCustomizer;
import org.netbeans.modules.tbls.editor.ps.TcgComponentNodePropertyEditor;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.OperatorComponentContainer;
import org.netbeans.modules.iep.model.Property;
import org.netbeans.modules.iep.model.SchemaAttribute;
import org.netbeans.modules.iep.model.SchemaComponent;
import org.netbeans.modules.iep.model.SchemaComponentContainer;
import org.netbeans.modules.tbls.model.TcgPropertyType;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.dnd.DragGestureEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableColumnModel;

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
            return new MyCustomizer(getPropertyType(), getOperatorComponent(), mEnv);
        }
        return new MyCustomizer(getPropertyType(), getOperatorComponent(), mCustomizerState);
    }
    
    private static class MyCustomizer extends TcgComponentNodePropertyCustomizer implements SharedConstants {
        private OperatorComponent mComponent;
        private PropertyPanel mNamePanel;
        private PropertyPanel mOutputSchemaNamePanel;
        private PropertyPanel mIncrementPanel;
        private PropertyPanel mSizePanel;
        private InputSchemaSelectionPanel mSelectionPanel;
        private DefaultMoveableRowTableModel mTableModel;
        private MoveableRowTable mTable;
        private Vector<String> mColTitle;
//        private JLabel mStatusLbl;
        
        public MyCustomizer(TcgPropertyType propertyType, OperatorComponent component, PropertyEnv env) {
            super(propertyType, component, env);
        }
        
        public MyCustomizer(TcgPropertyType propertyType, OperatorComponent component, TcgComponentNodePropertyCustomizerState customizerState) {
            super(propertyType, component, customizerState);
        }
        
        protected void initialize() {
            try {
                mComponent = getOperatorComponent();
                IEPModel model = mComponent.getModel();
                getContentPane().setLayout(new GridBagLayout());
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
                getContentPane().add(topPanel, gbc);
                
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
                getContentPane().add(attributePane, gbc);
                
                // left attribute pane
                ((JSplitPane)attributePane).setOneTouchExpandable(true);
                mSelectionPanel = new InputSchemaSelectionPanel(model, mComponent);
                mSelectionPanel.setPreferredSize(new Dimension(150, 300));
                ((JSplitPane)attributePane).setLeftComponent(mSelectionPanel);
                mSelectionPanel.addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        MyCustomizer.this.updateTable();
                    }
                });
                
                // right attribute pane
                JScrollPane rightPane = new JScrollPane();
                rightPane.setPreferredSize(new Dimension(500, 300));
                attributePane.setRightComponent(rightPane);
                
                mColTitle = new Vector<String>();
                mColTitle.add(NbBundle.getMessage(SelectPanel.class, "SelectPanel.ATTRIBUTE_NAME"));
                mColTitle.add(NbBundle.getMessage(SelectPanel.class, "SelectPanel.DATA_TYPE"));
                mColTitle.add(NbBundle.getMessage(SelectPanel.class, "SelectPanel.SIZE"));
                mColTitle.add(NbBundle.getMessage(SelectPanel.class, "SelectPanel.SCALE"));
                mTableModel = new ReadOnlyNoExpressionDefaultMoveableRowTableModel();
                
                mTable = new MoveableRowTable(mTableModel) {
                    
                    public void dragGestureRecognized(DragGestureEvent dge) {
                        return;
                    }
                };
                
                updateTable();
                
                
                
                rightPane.getViewport().add(mTable);
                
//                // status bar
//                gbc.gridx = 0;
//                gbc.gridy = gGridy++;
//                gbc.gridwidth = 1;
//                gbc.gridheight = 1;
//                gbc.anchor = GridBagConstraints.WEST;
//                gbc.weightx = 1.0D;
//                gbc.weighty = 0.0D;
//                gbc.fill = GridBagConstraints.HORIZONTAL;
//                mStatusLbl = new JLabel();
//                mStatusLbl.setForeground(Color.RED);
//                add(mStatusLbl, gbc);
            } catch(Exception e) {
                mLog.log(Level.SEVERE,
                        NbBundle.getMessage(DefaultCustomEditor.class, "CustomEditor.FAILED_TO_LAYOUT"),
                        e);
            }
        }
        
      
        
        private void updateTable() {
            try {
                Vector<Vector<String>> data = new Vector<Vector<String>>();
                int size = mSizePanel.getIntValue();
                List<SchemaAttribute> attributeList = mSelectionPanel.getSelectedAttributeList();
                Vector<String> r;
                for (int i = 0; i < size; i++) {
                    for (int j = 0, J = attributeList.size(); j < J; j++) {
                        r = new Vector<String>();
                        SchemaAttribute attr = attributeList.get(j);
                        r.add(attr.getAttributeName() + "_" + i);
                        r.add(attr.getAttributeType());
                        r.add(attr.getAttributeSize());
                        r.add(attr.getAttributeScale());
                        data.add(r);
                    }
                }
                mTableModel.setDataVector(data, mColTitle);
                
                TableColumnModel tcm = mTable.getColumnModel();
                SelectPanelTableCellRenderer spTCRenderer = new SelectPanelTableCellRenderer();
//              setting up renderer
                int mNameCol = 0;
                tcm.getColumn(mNameCol).setCellRenderer(spTCRenderer);
                tcm.getColumn(mNameCol + 1).setCellRenderer(spTCRenderer);
                tcm.getColumn(mNameCol + 2).setCellRenderer(spTCRenderer);
                tcm.getColumn(mNameCol + 3).setCellRenderer(spTCRenderer);
                
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
            if (mOutputSchemaNamePanel.getStringValue() == null || mOutputSchemaNamePanel.getStringValue().trim().equals("")) {
                IEPModel model = mComponent.getModel();
                String schemaName = NameGenerator.generateSchemaName(model.getPlanComponent().getSchemaComponentContainer());
                mOutputSchemaNamePanel.setStringValue(schemaName);
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

            // increment
            Property incrementProp = mComponent.getProperty(PROP_INCREMENT);
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
            Property sizeProp = mComponent.getProperty(PROP_SIZE);
            String sizeStr = NbBundle.getMessage(TupleSerialCorrelationCustomEditor.class, "CustomEditor.SIZE");
            mSizePanel = PropertyPanel.createIntNumberPanel(sizeStr, sizeProp, false);
            ((JTextField)mSizePanel.input[0]).addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent evt) {
                    MyCustomizer.this.updateTable();
                }
            });
            
            ((JTextField)mSizePanel.input[0]).addFocusListener(new FocusListener() {
                public void focusLost(FocusEvent e) {
                    MyCustomizer.this.updateTable();
                }
                
                public void focusGained(FocusEvent e) {
                    // TODO Auto-generated method stub
                    
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
                OperatorComponentContainer ocContainer = mModel.getPlanComponent().getOperatorComponentContainer();
                SchemaComponentContainer scContainer = mModel.getPlanComponent().getSchemaComponentContainer();
                
                // name
                mNamePanel.validateContent(evt);
                String newName = mNamePanel.getStringValue();
                String name = mComponent.getString(PROP_NAME);
                if (!newName.equals(name) && ocContainer.findOperator(newName) != null) {
                    String msg = NbBundle.getMessage(DefaultCustomEditor.class,
                            "CustomEditor.NAME_IS_ALREADY_TAKEN_BY_ANOTHER_OPERATOR",
                            newName);
                    throw new PropertyVetoException(msg, evt);
                }
                
                // output schema name
                mOutputSchemaNamePanel.validateContent(evt);
                String newSchemaName = mOutputSchemaNamePanel.getStringValue();
                SchemaComponent outputSchema = mComponent.getOutputSchema();
                String schemaName = null;
                if(outputSchema != null) {
                    schemaName = outputSchema.getName();
                }
                
                if (!newSchemaName.equals(schemaName) && scContainer.findSchema(newSchemaName) != null) {
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
        
        
        private List<SchemaAttribute> getAttributes() {
            List<SchemaAttribute> attributeList = new ArrayList<SchemaAttribute>();
            Vector r = mTableModel.getDataVector();
            for (int i = 0, I = r.size(); i < I; i++) {
                Vector c = (Vector) r.elementAt(i);
                SchemaAttribute sa = mModel.getFactory().createSchemaAttribute(mModel);
                String name = (String) c.elementAt(0);
                sa.setName(name);
                sa.setTitle(name);
                
                //name
                sa.setAttributeName(name);
                
                //type
                sa.setAttributeType((String) c.elementAt(1));
                //size
                sa.setAttributeSize((String) c.elementAt(2));
                //scale
                sa.setAttributeScale((String) c.elementAt(3));
                //comment
                sa.setAttributeComment("");
                
                attributeList.add(sa);
            }
            return attributeList;
        }
        
        public void setValue() {
            SchemaComponentContainer scContainer = mModel.getPlanComponent().getSchemaComponentContainer();
            
            mNamePanel.store();
            mIncrementPanel.store();
            mSizePanel.store();
            try {
                String newSchemaName = mOutputSchemaNamePanel.getStringValue();
                
                SchemaComponent outputSchema = mComponent.getOutputSchema();
                String schemaName = null;
                if(outputSchema != null) {
                    schemaName = outputSchema.getName();
                }
                
                boolean schemaExist = schemaName != null && !schemaName.trim().equals("") && outputSchema != null;
                List<SchemaAttribute> attrs = getAttributes();
                
                //rit fix below
                if (schemaExist) {
                    if (!newSchemaName.equals(schemaName)) {
                        mModel.startTransaction();
                        SchemaComponent scComp = mModel.getFactory().createSchema(mModel);
                        scComp.setName(newSchemaName);
                        scComp.setType(newSchemaName);
                        scComp.setSchemaAttributes(attrs);
                        
                        
                        scContainer.addSchemaComponent(scComp);
                        scContainer.removeSchemaComponent(outputSchema);
                        mModel.endTransaction();
                        
                        mOutputSchemaNamePanel.store();
                        //ritmProperty.getNode().getView().updateTcgComponentNodeView();
                        //ritplan.getPropertyChangeSupport().firePropertyChange("Schema Name",
                        //rit        schemaName, newSchemaName);
                        
                    } else {
                        mModel.startTransaction();
                        outputSchema.setSchemaAttributes(attrs);
                        mModel.endTransaction();
                        
                        //rit need to fix this
                        /*if (!schema.hasSameAttributeMetadata(attributes)) {
                            schema.setAttributeMetadataAsList(attributes);
                            plan.getPropertyChangeSupport().firePropertyChange("Schema Column Metadata",
                                    "old", "new");
                        }*/
                    }
                } else {
                    mModel.startTransaction();
                    
                    SchemaComponent scComp = mModel.getFactory().createSchema(mModel);
                    scComp.setName(newSchemaName);
                    scComp.setTitle(newSchemaName);
                    scComp.setSchemaAttributes(attrs);
                    
                    scContainer.addSchemaComponent(scComp);
                    mModel.endTransaction();
                    mOutputSchemaNamePanel.store();
                    
//                    newSchema = ModelManager.createSchema(schemaName);
//                    plan.addSchema(newSchema);
//                    newSchema.setAttributeMetadataAsList(attributes);
//                    mOutputSchemaNamePanel.store();
//                    //ritmProperty.getNode().getView().updateTcgComponentNodeView();
//                    plan.getPropertyChangeSupport().firePropertyChange("Schema",
//                            null, newSchema);
                }
                List<String> selectedAttrNameList = mSelectionPanel.getSelectedAttributeNameList();
                mModel.startTransaction();
                mComponent.setStringList(PROP_FROM_COLUMN_LIST, selectedAttrNameList);
                mModel.endTransaction();
                

                //set documentation
                super.setDocumentation();
                
            } catch (Exception e) {
                e.printStackTrace();
                NotifyHelper.reportError(e.getMessage());
            }
        }
    }
    
    
}
