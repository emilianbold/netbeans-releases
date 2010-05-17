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
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableColumnModel;

import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.NbBundle;

/**
 * GapWindowCustomEditor.java
 *
 * Created on November 10, 2006, 10:23 AM
 *
 * @author Bing Lu
 */
public class GapWindowCustomEditor extends TcgComponentNodePropertyEditor implements SharedConstants {
    private static final Logger mLog = Logger.getLogger(GapWindowCustomEditor.class.getName());
    
    private static Set<String> INTEGER_TYPES = new HashSet<String>();
    static {
//        INTEGER_TYPES.add(SQL_TYPE_TINYINT);
//        INTEGER_TYPES.add(SQL_TYPE_SMALLINT);
        INTEGER_TYPES.add(SQL_TYPE_INTEGER);
        INTEGER_TYPES.add(SQL_TYPE_BIGINT);
    }
    
    
    /** Creates a new instance of GapWindowCustomEditor */
    public GapWindowCustomEditor() {
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
        private PropertyPanel mStartPanel;
        private PropertyPanel mAttributePanel;
        private PartitionKeySelectionPanel mPartitionKeyPanel;
        private DefaultMoveableRowTableModel mTableModel;
        private MoveableRowTable mTable;
        private Vector<String> mColTitle;
//        private JLabel mStatusLbl;
        
        public MyCustomizer(TcgPropertyType propertyType, OperatorComponent component, PropertyEnv env) {
            super(propertyType,component, env);
        }
        
        public MyCustomizer(TcgPropertyType propertyType, OperatorComponent component, TcgComponentNodePropertyCustomizerState customizerState) {
            super(propertyType, component, customizerState);
        }
        
        protected void initialize() {
            try {
                mComponent = getOperatorComponent();
                getContentPane().setLayout(new GridBagLayout());
                GridBagConstraints gbc = new GridBagConstraints();
                gbc.insets = new Insets(3, 3, 3, 3);
                int gGridy = 0;
                
                // create input selection panel first to parse schema information
                // which is used by properties in property panel created
                // by createPropertyPanel()
                
                // create partition key panel first to parse schema information
                // which is used by properties in property panel created
                // by createPropertyPanel()
                IEPModel model = mComponent.getModel();
                mPartitionKeyPanel = new PartitionKeySelectionPanel(model, mComponent);

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
                mPartitionKeyPanel.setPreferredSize(new Dimension(150, 300));
                ((JSplitPane)attributePane).setLeftComponent(mPartitionKeyPanel);
                mPartitionKeyPanel.addItemListener(new ItemListener() {
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
                String indexAttribute = mAttributePanel.getStringValue();
                boolean indexSelected = indexAttribute != null && !indexAttribute.trim().equals("");
                List<SchemaAttribute> attributeList = mPartitionKeyPanel.getSelectedAttributeList();
                Vector<String> r;
                for (int i = 0, I = attributeList.size(); i < I; i++) {
                    SchemaAttribute attr = attributeList.get(i);
                    r = new Vector<String>();
                    r.add(attr.getAttributeName());
                    r.add(attr.getAttributeType());
                    r.add(attr.getAttributeSize());
                    r.add(attr.getAttributeScale());
                    data.add(r);
                }
                if (indexSelected) {
                    SchemaAttribute attr = mPartitionKeyPanel.getAttribute(indexAttribute);
                    r = new Vector<String>();
                    r.add(attr.getAttributeName());
                    r.add(attr.getAttributeType());
                    r.add(attr.getAttributeSize());
                    r.add(attr.getAttributeScale());
                    data.add(r);
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
                        NbBundle.getMessage(GapWindowCustomEditor.class, "CustomEditor.FAILED_UPDATE_TABLE"),
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

            // start
            Property startProp = mComponent.getProperty(PROP_START);
            String startStr = NbBundle.getMessage(GapWindowCustomEditor.class, "CustomEditor.START");
            mStartPanel = PropertyPanel.createIntNumberPanel(startStr, startProp, false);
            gbc.gridx = 3;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mStartPanel.component[0], gbc);
            
            gbc.gridx = 4;
            gbc.gridy = 0;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mStartPanel.component[1], gbc);

            // attribute
            Property attributeProp = mComponent.getProperty(PROP_ATTRIBUTE);
            String attributeStr = NbBundle.getMessage(TupleBasedAggregatorCustomEditor.class, "CustomEditor.SORT_BY");
            List<String> attributeList = mPartitionKeyPanel.getAttributeNameList(INTEGER_TYPES);
            attributeList.add(0, "");
            mAttributePanel = PropertyPanel.createComboBoxPanel(attributeStr, attributeProp, (String[])attributeList.toArray(new String[0]), false);
            gbc.gridx = 3;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mAttributePanel.component[0], gbc);
            
            gbc.gridx = 4;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(mAttributePanel.component[1], gbc);
            
            ((JComboBox)mAttributePanel.input[0]).addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    updateTable();
                }
            });
            
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
                
                SchemaComponentContainer scContainer = mModel.getPlanComponent().getSchemaComponentContainer();
                
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
                
                // start
                mStartPanel.validateContent(evt);
                
                // attribute index
                mAttributePanel.validateContent(evt);
                
                // attribute for index cannot be part of partition key
                String indexName = mAttributePanel.getStringValue();
                List partitionKey = mPartitionKeyPanel.getSelectedAttributeNameList();
                for (int i = 0, I = partitionKey.size(); i < I; i++) {
                    if (indexName.equals(partitionKey.get(i))) {
                        String msg = NbBundle.getMessage(DefaultCustomEditor.class,
                                "CustomEditor.SORT_BY_ATTRIBUTE_MUST_NOT_BE_PART_OF_PARTITION_KEY");
                        throw new PropertyVetoException(msg, evt);
                        
                    }
                }
            } catch (Exception e) {
                String msg = e.getMessage();
                mStatusLbl.setText(msg);
                mStatusLbl.setIcon(GuiConstants.ERROR_ICON);
                throw new PropertyVetoException(msg, evt);
            }
        }
        
        
        public List<SchemaAttribute> getAttributes() {
            List<SchemaAttribute> attributeList = new ArrayList<SchemaAttribute>();
            Vector r = mTableModel.getDataVector();
            for (int i = 0, I = r.size(); i < I; i++) {
                Vector c = (Vector) r.elementAt(i);
                if (!(c.elementAt(0) == null) && !(c.elementAt(0).equals(""))) {
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
                    
                    
                    attributeList.add(sa);
                    
                }
            }
            return attributeList;
        }
        
        
        
        public void setValue() {
            
            IEPModel model = mComponent.getModel();
            SchemaComponentContainer scContainer = model.getPlanComponent().getSchemaComponentContainer();
            mNamePanel.store();
            mStartPanel.store();
            mAttributePanel.store();
            mPartitionKeyPanel.store();
            try {
                String newSchemaName = mOutputSchemaNamePanel.getStringValue();
                SchemaComponent outputSchema = mComponent.getOutputSchema();
                String schemaName = null;
                
                if(outputSchema != null) {
                    schemaName = outputSchema.getName();
                }
                
                boolean schemaExist = schemaName != null && !schemaName.trim().equals("") && outputSchema != null;
                List<SchemaAttribute> attrs = getAttributes();
                if (schemaExist) {
                    if (!newSchemaName.equals(schemaName)) {
                        model.startTransaction();
                        
//                        String sName = NameGenerator.generateSchemaName(scContainer);
                        SchemaComponent scComp = model.getFactory().createSchema(model);
                        scComp.setName(newSchemaName);
                        scComp.setTitle(newSchemaName);
                        scComp.setSchemaAttributes(attrs);
                        
                        
                        scContainer.addSchemaComponent(scComp);
                        scContainer.removeSchemaComponent(outputSchema);
                        model.endTransaction();
                        
                        mOutputSchemaNamePanel.store();
                        
                        //newSchema = ModelManager.createSchema(newSchemaName);
                        //newSchema.setAttributeMetadataAsList(attributes);
                        //plan.addSchema(newSchema);
                        //plan.removeSchema(schemaName);
                        
                        //ritmProperty.getNode().getView().updateTcgComponentNodeView();
                        //ritplan.getPropertyChangeSupport().firePropertyChange("Schema Name",
                        //        schemaName, newSchemaName);
                        
                    } else {
                        model.startTransaction();
                        outputSchema.setSchemaAttributes(attrs);
                        model.endTransaction();
                        
                        //rit fix this
                        /*
                        if (!schema.hasSameAttributeMetadata(attributes)) {
                            schema.setAttributeMetadataAsList(attributes);
                            plan.getPropertyChangeSupport().firePropertyChange("Schema Column Metadata",
                                    "old", "new");
                        }*/
                    }
                } else {
                    model.startTransaction();
                    SchemaComponent scComp = model.getFactory().createSchema(model);
                    scComp.setName(newSchemaName);
                    scComp.setTitle(newSchemaName);
                    scComp.setSchemaAttributes(attrs);
                    scContainer.addSchemaComponent(scComp);
                    model.endTransaction();
                    mOutputSchemaNamePanel.store();
                    //mProperty.getNode().getView().updateTcgComponentNodeView();
                    //ritplan.getPropertyChangeSupport().firePropertyChange("Schema",
                    //        null, scComp);
                }
                

                //set documentation
                super.setDocumentation();
                
            } catch (Exception e) {
                e.printStackTrace();
                NotifyHelper.reportError(e.getMessage());
            }
        }
    }
    
   
}
