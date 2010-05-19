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

import java.awt.BorderLayout;
import org.netbeans.modules.iep.editor.designer.GuiConstants;
import org.netbeans.modules.tbls.editor.dialog.NotifyHelper;
import org.netbeans.modules.iep.model.share.SharedConstants;
import org.netbeans.modules.tbls.editor.ps.TcgComponentNodePropertyCustomizerState;
import org.netbeans.modules.tbls.editor.ps.TcgComponentNodePropertyCustomizer;
import org.netbeans.modules.tbls.editor.ps.TcgComponentNodePropertyEditor;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.OperatorComponentContainer;
import org.netbeans.modules.iep.model.Property;
import org.netbeans.modules.tbls.model.TcgPropertyType;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.netbeans.modules.iep.editor.model.NameGenerator;
import org.netbeans.modules.iep.model.SchemaAttribute;
import org.netbeans.modules.iep.model.SchemaComponent;
import org.netbeans.modules.iep.model.SchemaComponentContainer;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.util.NbBundle;

/**
 * InvokeServiceCustomEditor.java
 *
 * Created on September 22, 2008, 10:23 AM
 *
 * @author Bing Lu
 */
public class InvokeServiceCustomEditor extends TcgComponentNodePropertyEditor {

    private static final Logger mLog = Logger.getLogger(InvokeServiceCustomEditor.class.getName());

    /** Creates a new instance of InvokeServiceCustomEditor */
    public InvokeServiceCustomEditor() {
    }

    @Override
    public boolean supportsCustomEditor() {
        return true;
    }

    @Override
    public Component getCustomEditor() {
        if (mEnv != null) {
            return new MyCustomizer(getPropertyType(), getOperatorComponent(), mEnv);
        }
        return new MyCustomizer(getPropertyType(), getOperatorComponent(), mCustomizerState);
    }

    private static class MyCustomizer extends TcgComponentNodePropertyCustomizer implements SharedConstants {

        protected PropertyPanel mNamePanel;
        protected PropertyPanel mOutputSchemaNamePanel;
        protected InvocationRequestPanel mRequestPanel;
        protected InvocationResponsePanel mResponsePanel;
        protected InvocationAttributePanel mAttributePanel;

        public MyCustomizer(TcgPropertyType propertyType, OperatorComponent component, PropertyEnv env) {
            super(propertyType, component, env);
        }

        public MyCustomizer(TcgPropertyType propertyType, OperatorComponent component, TcgComponentNodePropertyCustomizerState customizerState) {
            super(propertyType, component, customizerState);
        }

        protected void initialize() {
            try {
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
                JPanel detailsPanel = createPropertyPanel();
                getContentPane().add(detailsPanel, gbc);

                // Web service invocation
                gbc.gridx = 0;
                gbc.gridy = gGridy++;
                gbc.gridwidth = 1;
                gbc.gridheight = 1;
                gbc.anchor = GridBagConstraints.WEST;
                gbc.weightx = 1.0D;
                gbc.weighty = 0.5D;
                gbc.fill = GridBagConstraints.BOTH;
                JPanel servicePane = new JPanel();
                getContentPane().add(servicePane, gbc);
                String title = NbBundle.getMessage(DefaultCustomEditor.class, "InvokeServiceCustomEditor.Web_Service_Invocation");
                servicePane.setBorder(new TitledBorder(LineBorder.createGrayLineBorder(), title, TitledBorder.LEFT, TitledBorder.TOP));
                servicePane.setLayout(new BorderLayout());
                JTabbedPane tabPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
                servicePane.add(tabPane, BorderLayout.CENTER);
                // Requst
                mRequestPanel = new InvocationRequestPanel(mComponent);
                title = NbBundle.getMessage(InvocationRequestPanel.class, "InvocationRequestPanel.Request");
                tabPane.add(mRequestPanel, title);
                // Reply
                mResponsePanel = new InvocationResponsePanel(mComponent);
                title = NbBundle.getMessage(InvocationResponsePanel.class, "InvocationResponsePanel.Response");
                tabPane.add(mResponsePanel, title);

                //Attributes
                gbc.gridx = 0;
                gbc.gridy = gGridy++;
                gbc.gridwidth = 1;
                gbc.gridheight = 1;
                gbc.anchor = GridBagConstraints.WEST;
                gbc.weightx = 1.0D;
                gbc.weighty = 0.5D;
                gbc.fill = GridBagConstraints.BOTH;
                JPanel attributePane = new JPanel();
                getContentPane().add(attributePane, gbc);
                title = NbBundle.getMessage(InvocationAttributePanel.class, "InvocationAttributePanel.Attributes");
                attributePane.setBorder(new TitledBorder(LineBorder.createGrayLineBorder(), title, TitledBorder.LEFT, TitledBorder.TOP));
                mAttributePanel = new InvocationAttributePanel(mComponent);
                attributePane.setLayout(new BorderLayout(5, 5));
                attributePane.add(mAttributePanel, BorderLayout.CENTER);

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
                
                TableModelListener tmListener = new TableModelListener() {
                    public void tableChanged(TableModelEvent e) {
                        Vector<Vector<String>> newDv = new Vector<Vector<String>>();
                        Vector<Vector<Object>> requestDv = mRequestPanel.getTableModel().getDataVector();
                        Vector<Vector<Object>> responseDv = mResponsePanel.getTableModel().getDataVector();
                        for (int i = 0; i < requestDv.size(); i++) {
                            Vector<Object> row = requestDv.elementAt(i);
                            Boolean selected = (Boolean)row.elementAt(0);
                            if (!selected) {
                                continue;
                            }
                            Vector<String> newRow = new Vector<String>();
                            for (int j = 1; j < row.size(); j++) {
                                newRow.add((String)row.elementAt(j));
                            }
                            newDv.add(newRow);
                        }
                        for (int i = 0; i < responseDv.size(); i++) {
                            Vector<Object> row = responseDv.elementAt(i);
                            Vector<String> newRow = new Vector<String>();
                            for (int j = 0; j < row.size(); j++) {
                                newRow.add((String)row.elementAt(j));
                            }
                            newDv.add(newRow);
                        }
                        mAttributePanel.setDataVector(newDv);
                    }                      
                };
                mRequestPanel.getTableModel().addTableModelListener(tmListener);
                mResponsePanel.getTableModel().addTableModelListener(tmListener);
            } catch (Exception e) {
                mLog.log(Level.SEVERE,
                        NbBundle.getMessage(DefaultCustomEditor.class, "CustomEditor.FAILED_TO_LAYOUT"),
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

            // glue
            gbc.gridx = 2;
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
                if (outputSchema != null) {
                    schemaName = outputSchema.getName();
                }
                if (!newSchemaName.equals(schemaName) && scContainer.findSchema(newSchemaName) != null) {
                    String msg = NbBundle.getMessage(DefaultCustomEditor.class,
                            "CustomEditor.OUTPUT_SCHEMA_NAME_IS_ALREADY_TAKENBY_ANOTHER_SCHEMA",
                            newSchemaName);
                    throw new PropertyVetoException(msg, evt);
                }

                // reponse
                mResponsePanel.validateContent(evt);

                // attribute
                mAttributePanel.validateContent(evt);
            } catch (Exception e) {
                String msg = e.getMessage();
                mStatusLbl.setText(msg);
                mStatusLbl.setIcon(GuiConstants.ERROR_ICON);
                throw new PropertyVetoException(msg, evt);
            }
        }

        public void setValue() {
            try {
                SchemaComponentContainer scContainer = mModel.getPlanComponent().getSchemaComponentContainer();

                mNamePanel.store();

                String newSchemaName = mOutputSchemaNamePanel.getStringValue();
                SchemaComponent outputSchema = mComponent.getOutputSchema();
                String schemaName = null;
                if (outputSchema != null) {
                    schemaName = outputSchema.getName();
                }

                boolean schemaExist = schemaName != null && !schemaName.trim().equals("") && outputSchema != null;
                //ritList attributes = mSelectPanel.getAttributeMetadataAsList();
                List<SchemaAttribute> attrs = mAttributePanel.getAttributes();
                if (schemaExist) {
                    if (!newSchemaName.equals(schemaName)) {
                        mModel.startTransaction();
                        SchemaComponent sc = mModel.getFactory().createSchema(mModel);
                        sc.setName(newSchemaName);
                        sc.setTitle(newSchemaName);
                        sc.setSchemaAttributes(attrs);

                        scContainer.addSchemaComponent(sc);
                        scContainer.removeSchemaComponent(outputSchema);
                        mModel.endTransaction();

                        mOutputSchemaNamePanel.store();
                    } else {
                        mModel.startTransaction();
                        outputSchema.setSchemaAttributes(attrs);
                        mModel.endTransaction();
                    }
                } else {
                    mModel.startTransaction();
                    SchemaComponent sc = mModel.getFactory().createSchema(mModel);
                    sc.setName(newSchemaName);
                    sc.setTitle(newSchemaName);
                    sc.setSchemaAttributes(attrs);

                    scContainer.addSchemaComponent(sc);
                    mModel.endTransaction();

                    mOutputSchemaNamePanel.store();
                }


                // request
                mRequestPanel.store();
                
                // reponse
                mResponsePanel.store();

                //set documentation
                super.setDocumentation();
            } catch (Exception e) {
                e.printStackTrace();
                NotifyHelper.reportError(e.getMessage());
            }
        }
    }
    
}
