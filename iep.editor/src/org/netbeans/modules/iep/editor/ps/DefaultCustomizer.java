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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.iep.editor.designer.GuiConstants;
import org.netbeans.modules.iep.editor.model.NameGenerator;
import org.netbeans.modules.iep.editor.xsd.nodes.SchemaComponentIEPTypeFinderVisitor;
import org.netbeans.modules.iep.model.IEPModel;
import org.netbeans.modules.iep.model.OperatorComponent;
import org.netbeans.modules.iep.model.OperatorComponentContainer;
import org.netbeans.modules.iep.model.Property;
import org.netbeans.modules.iep.model.SchemaAttribute;
import org.netbeans.modules.iep.model.SchemaComponent;
import org.netbeans.modules.iep.model.SchemaComponentContainer;
import org.netbeans.modules.iep.model.WSType;
import org.netbeans.modules.iep.model.WsOperatorComponent;
import org.netbeans.modules.iep.model.share.SharedConstants;
import org.netbeans.modules.iep.model.util.StringUtil;
import org.netbeans.modules.tbls.editor.dialog.NotifyHelper;
import org.netbeans.modules.tbls.editor.ps.TcgComponentNodePropertyCustomizer;
import org.netbeans.modules.tbls.editor.ps.TcgComponentNodePropertyCustomizerState;
import org.netbeans.modules.tbls.editor.table.DefaultMoveableRowTableModel;
import org.netbeans.modules.tbls.editor.table.MoveableRowTable;
import org.netbeans.modules.tbls.model.TcgPropertyType;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.AXIType;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * DefaultCustomizer.java
 *
 * Created on November 10, 2006, 9:49 AM
 *
 * @author Bing Lu
 */
public class DefaultCustomizer extends TcgComponentNodePropertyCustomizer implements SharedConstants {

    protected static final Logger mLog = Logger.getLogger(DefaultCustomizer.class.getName());
    protected PropertyPanel mNamePanel;
    protected PropertyPanel mOutputSchemaNamePanel;
    protected InputSchemaTreePanel mInputPanel;
    protected SelectPanel mSelectPanel;
    protected PropertyPanel mFromPanel;
    protected PropertyPanel mWherePanel;
    protected PropertyPanel mGroupByPanel;
//    protected JLabel mStatusLbl;
    protected boolean mIsSchemaOwner;
    protected boolean mHasExpressionColumn;
    protected boolean mHasFromClause;
    protected boolean mHasWhereClause;
    protected boolean mHasGroupBy;

    public DefaultCustomizer(TcgPropertyType propertyType, OperatorComponent component, PropertyEnv env) {
        super(propertyType, component, env);
    }

    public DefaultCustomizer(TcgPropertyType propertyType, OperatorComponent component, TcgComponentNodePropertyCustomizerState customizerState) {
        super(propertyType, component, customizerState);
    }

    protected void initialize() {
        try {
            mIsSchemaOwner = mComponent.getBoolean(PROP_IS_SCHEMA_OWNER);
            String inputType = mComponent.getInputType().getType();
            mHasExpressionColumn = mIsSchemaOwner && !inputType.equals(IO_TYPE_NONE);
            mHasFromClause = mComponent.hasPropertyDefined(PROP_FROM_CLAUSE);
            mHasWhereClause = mComponent.hasPropertyDefined(PROP_WHERE_CLAUSE);
            mHasGroupBy = mComponent.hasPropertyDefined(PROP_GROUP_BY_COLUMN_LIST);

            getContentPane().setLayout(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(3, 3, 3, 3);
            int gGridy = 0;

            // create selection panel first to parse schema information
            // which is used by properties in property panel created
            // by createPropertyPanel()
            IEPModel model = getOperatorComponent().getModel();

            mSelectPanel = createSelectPanel(mComponent);

            UpdateFromClauseExpressionAttributeDropNotificationListener listener = new UpdateFromClauseExpressionAttributeDropNotificationListener();
            mSelectPanel.addAttributeDropNotificationListener(listener);

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
            JComponent attributePane;
            if (mHasExpressionColumn) {
                attributePane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
            } else {
                attributePane = new JPanel();
                attributePane.setLayout(new BorderLayout());
            }
            String msg = NbBundle.getMessage(DefaultCustomEditor.class, "CustomEditor.ATTRIBUTES");
            attributePane.setBorder(new TitledBorder(LineBorder.createGrayLineBorder(), msg, TitledBorder.LEFT, TitledBorder.TOP));
            getContentPane().add(attributePane, gbc);

            // left attribute pane
            if (mHasExpressionColumn) {
                ((JSplitPane) attributePane).setOneTouchExpandable(true);
                mInputPanel = createInputSchemaTreePanel(model, mComponent);
                if (mInputPanel != null) {
                    mInputPanel.setPreferredSize(new Dimension(200, 400));
                    ((JSplitPane) attributePane).setLeftComponent(mInputPanel);
                }
            }

            // right attribute pane
            JPanel rightPane = new JPanel();
            rightPane.setPreferredSize(new Dimension(560, 300));
            if (mHasExpressionColumn) {
                ((JSplitPane) attributePane).setRightComponent(rightPane);
            } else {
                attributePane.add(rightPane, BorderLayout.CENTER);
                if (mComponent.getOutputSchema() != null) {
                    attributePane.setToolTipText(NbBundle.getMessage(DefaultCustomizer.class,
                            "InputSchemaTreePanel_Tooltip.inputoperator_connected"));
                } else {
                    attributePane.setToolTipText(NbBundle.getMessage(DefaultCustomizer.class,
                            "InputSchemaTreePanel_Tooltip.inputoperator_not_connected"));
                }

            }
            rightPane.setLayout(new GridBagLayout());
            int rightPaneGridY = 0;
            gbc.gridx = 0;
            gbc.gridy = rightPaneGridY++;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 1.0D;
            gbc.weighty = 0.5D;
            gbc.fill = GridBagConstraints.BOTH;
            rightPane.add(mSelectPanel, gbc);

            if (mHasExpressionColumn) {
                if (mHasFromClause && isShowFromClause()) {
                    gbc.gridx = 0;
                    gbc.gridy = rightPaneGridY++;
                    gbc.gridwidth = 1;
                    gbc.gridheight = 1;
                    gbc.anchor = GridBagConstraints.WEST;
                    gbc.weightx = 1.0D;
                    gbc.weighty = 0.0D;
                    gbc.fill = GridBagConstraints.HORIZONTAL;
                    Property fromProp = mComponent.getProperty(PROP_FROM_CLAUSE);
                    boolean truncateColumn = true;
                    String label = NbBundle.getMessage(DefaultCustomEditor.class, "DefaultCustomEditor.FROM");
                    mFromPanel = PropertyPanel.createSmartSingleLineTextPanel(label, fromProp, truncateColumn, true);
                    rightPane.add(mFromPanel.panel, gbc);
                }
                if (mHasWhereClause) {
                    gbc.gridx = 0;
                    gbc.gridy = rightPaneGridY++;
                    gbc.gridwidth = 1;
                    gbc.gridheight = 1;
                    gbc.anchor = GridBagConstraints.WEST;
                    gbc.weightx = 1.0D;
                    gbc.weighty = 0.5D;
                    gbc.fill = GridBagConstraints.BOTH;
                    Property whereProp = mComponent.getProperty(PROP_WHERE_CLAUSE);
                    String label = NbBundle.getMessage(DefaultCustomEditor.class, "DefaultCustomEditor.WHERE");
                    mWherePanel = PropertyPanel.createSmartMultiLineTextPanel(label, whereProp);
                    rightPane.add(mWherePanel.panel, gbc);
                }
                if (mHasGroupBy) {
                    gbc.gridx = 0;
                    gbc.gridy = rightPaneGridY++;
                    gbc.gridwidth = 1;
                    gbc.gridheight = 1;
                    gbc.anchor = GridBagConstraints.WEST;
                    gbc.weightx = 1.0D;
                    gbc.weighty = 0.0D;
                    gbc.fill = GridBagConstraints.BOTH;
                    Property groupByProp = mComponent.getProperty(PROP_GROUP_BY_COLUMN_LIST);
                    boolean truncateColumn = false;
                    String label = NbBundle.getMessage(DefaultCustomEditor.class, "DefaultCustomEditor.GROUPBY");
                    mGroupByPanel = PropertyPanel.createSmartSingleLineTextPanel(label, groupByProp, truncateColumn, true);
                    rightPane.add(mGroupByPanel.panel, gbc);
                }
            }

//            // status bar
//            gbc.gridx = 0;
//            gbc.gridy = gGridy++;
//            gbc.gridwidth = 1;
//            gbc.gridheight = 1;
//            gbc.anchor = GridBagConstraints.WEST;
//            gbc.weightx = 1.0D;
//            gbc.weighty = 0.0D;
//            gbc.fill = GridBagConstraints.HORIZONTAL;
//            mStatusLbl = new JLabel();
//            mStatusLbl.setForeground(Color.RED);
//            mStatusLbl.setPreferredSize(new Dimension(160, 20));
//            add(mStatusLbl, gbc);
        } catch (Exception e) {
            mLog.log(Level.SEVERE,
                    NbBundle.getMessage(DefaultCustomEditor.class, "CustomEditor.FAILED_TO_LAYOUT"),
                    e);
        }
    }

    protected SelectPanel createSelectPanel(OperatorComponent component) {
        return new SelectPanel(component);
    }

    protected InputSchemaTreePanel createInputSchemaTreePanel(IEPModel model, OperatorComponent component) {
        mInputPanel = new InputSchemaTreePanel(model, mComponent);
        return mInputPanel;
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

        /*gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0D;
        gbc.weighty = 0.0D;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        pane.add(Box.createHorizontalGlue(), gbc);
         */
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

        // create the select attribute button for the InputOperatorComponent, like InputStream
        if ((mComponent instanceof WsOperatorComponent) && mComponent.getWsType().equals(WSType.IN_ONLY)) {
            String labelTitle = NbBundle.getMessage(DefaultCustomizer.class, "CustomEdiotr.SELECT_ATTRIBUTE");
            JLabel label = new JLabel(labelTitle);
            JButton button = new JButton(".."); // NONI18N
            JPanel panel = new JPanel();
            panel.add(label);
            panel.add(button);

            final FileObject fileObj = mModel.getModelSource().getLookup().lookup(FileObject.class);
            button.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    // popup the SchemaSelectionDialog
                    if (fileObj != null) {
                        Project project = FileOwnerQuery.getOwner(fileObj);
                        List<SchemaAttribute> attributeList = mSelectPanel.getAttributes();
                        List<String> artifactNamesList = mSelectPanel.getAttributeNameList();

                        SelectionArtifacts artifacts = SchemaSelectionDialog.showDialog(project, artifactNamesList);
                        List<AXIComponent> addedArtifacts = artifacts.getSelectedList();
                        List<AXIComponent> removedArtifacts = artifacts.getRemovedList();

                        // calculate the removed artifacts.
                        List<SchemaAttribute> toRemoveSchemaAttribute = new ArrayList<SchemaAttribute>();
                        for (AXIComponent comp : removedArtifacts) {
                            String name = ((AXIType) comp).getName();
                            for (SchemaAttribute attribute : attributeList) {
                                String attributeName = attribute.getAttributeName();
                                if (attributeName.equals(name)) {
                                    toRemoveSchemaAttribute.add(attribute);
                                }
                            }
                        }

                        // remove the attributes that was removed and hence obtain the list 
                        // of attributes to retain in the table.
                        for (SchemaAttribute attr : toRemoveSchemaAttribute) {
                            attributeList.remove(attr);
                        }

                        // clear the table of present rows
                        mSelectPanel.clearTable();

                        DefaultMoveableRowTableModel model = mSelectPanel.getTableModel();
                        final MoveableRowTable table = mSelectPanel.getTable();
                        // add the new selections to the table model
                        for (AXIComponent comp : addedArtifacts) {
                            String name = ((AXIType) comp).getName();
                            org.netbeans.modules.xml.schema.model.SchemaComponent sc = comp.getPeer();
                            SchemaComponentIEPTypeFinderVisitor visitor = new SchemaComponentIEPTypeFinderVisitor();
                            sc.accept(visitor);
                            String iepType = visitor.getIEPType();
                            String size = "";
                            String scale = "";
                            String comment = "";
                            if (SharedConstants.SQL_TYPE_VARCHAR.equals(iepType)) {
                                size = "50"; //by default use 50 for size. size is required for VARCHAR
                            }
                            model.addRow(new Object[]{name, iepType, size, scale, comment});
                            int rcount = table.getRowCount();
                            table.setRowSelectionInterval(rcount - 1, rcount - 1);

                        }

                        // add the attributes that should be retained.
                        for (SchemaAttribute attr : attributeList) {
                            String name = attr.getAttributeName();
                            String type = attr.getAttributeType();
                            String size = attr.getAttributeSize();
                            String scale = attr.getAttributeScale();
                            String comment = attr.getAttributeComment();

                            model.addRow(new Object[]{name, type, size, scale, comment});
                            int rcount = table.getRowCount();
                            table.setRowSelectionInterval(rcount - 1, rcount - 1);
                        }
                        // Table request's focus, so the selection is visible.
                        SwingUtilities.invokeLater(new Runnable() {

                            public void run() {
                                table.requestFocus();
                            }
                        });
                    }
                }
            });

            // struct
            gbc.gridx = 2;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(Box.createHorizontalStrut(20), gbc);

            gbc.gridx = 3;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(label, gbc);

            gbc.gridx = 4;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 0.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.NONE;
            pane.add(button, gbc);

            // glue
            gbc.gridx = 5;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 1.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            pane.add(Box.createHorizontalGlue(), gbc);

        } else {
            // glue
            gbc.gridx = 2;
            gbc.gridy = 1;
            gbc.gridwidth = 1;
            gbc.gridheight = 1;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.weightx = 1.0D;
            gbc.weighty = 0.0D;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            pane.add(Box.createHorizontalGlue(), gbc);
        }
        return pane;
    }

    public void validateContent(PropertyChangeEvent evt) throws PropertyVetoException {
        try {
            IEPModel model = mComponent.getModel();

            OperatorComponentContainer ocContainer = model.getPlanComponent().getOperatorComponentContainer();
            SchemaComponentContainer scContainer = model.getPlanComponent().getSchemaComponentContainer();

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

            if (mIsSchemaOwner) {
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

                // schema
                mSelectPanel.validateContent(evt);

                if (mHasExpressionColumn) {
                    if (mHasFromClause && isShowFromClause()) {
                        // from clause
                        mFromPanel.validateContent(evt);
                        String from = mFromPanel.getStringValue();
                        List actualInpuNameList = StringUtil.getTokenList(from.trim(), ",");
                        for (int i = 0; i < actualInpuNameList.size(); i++) {
                            // from could be: T t, S s
                            String s = ((String) actualInpuNameList.get(i)).trim();
                            int idx = s.indexOf(" ");
                            if (0 < idx) {
                                s = s.substring(0, idx);
                            }
                            if (!mSelectPanel.hasInput(s)) {
                                String msg = NbBundle.getMessage(DefaultCustomEditor.class,
                                        "CustomEditor.INPUT_NAME_CANNOT_BE_FOUND_FROM_THE_INPUTS",
                                        s);
                                throw new PropertyVetoException(msg, evt);
                            }
                        }
                    }
                    if (mHasWhereClause) {
                        // where clause
                        mWherePanel.validateContent(evt);
                    }
                    // group by
                    if (mHasGroupBy) {
                        mGroupByPanel.validateContent(evt);
                        String value = mGroupByPanel.getStringValue();
                        List attributeList = StringUtil.getTokenList(value, ",");
                        // group-by attribute name must be found from input tree
                        for (int i = 0, I = attributeList.size(); i < I; i++) {
                            String attributeName = (String) attributeList.get(i);
                            if (mSelectPanel.getAttribute(attributeName) == null) {
                                String msg = NbBundle.getMessage(DefaultCustomEditor.class,
                                        "CustomEditor.GROUP_BY_ATTRIBUTE_NAME_CANNOT_FOUND_FROM_THE_INPUT_ATTRIBUTES",
                                        attributeName);
                                throw new PropertyVetoException(msg, evt);
                            }
                        }
                        // no duplicate group-by attribute names
                        Set<String> nameSet = new HashSet<String>();
                        for (int i = 0, I = attributeList.size(); i < I; i++) {
                            String attributeName = (String) attributeList.get(i);
                            if (nameSet.contains(attributeName)) {
                                String msg = NbBundle.getMessage(DefaultCustomEditor.class,
                                        "CustomEditor.DUPLICATE_GROUP_BY_ATTRIBUTE_NAME_IS_NOT_ALLOWED",
                                        attributeName);
                                throw new PropertyVetoException(msg, evt);
                            }
                            nameSet.add(attributeName);
                        }

                        // single-attribute expression in expression list must show in group by list
                        List<String> expList = mSelectPanel.getExpressionList();
                        for (int i = 0, I = expList.size(); i < I; i++) {
                            String exp = expList.get(i);
                            exp = exp.trim();
                            if (mSelectPanel.hasInputAttribute(exp) && !nameSet.contains(exp)) {
                                String msg = NbBundle.getMessage(DefaultCustomEditor.class,
                                        "CustomEditor.SINGLE_ATTRIBUTE_EXPRESSION_CANNOT_BE_FOUND_FROM_GROUP_BY_ATTRIBUTES",
                                        exp);
                                throw new PropertyVetoException(msg, evt);
                            }
                        }
                    }
                }
            }
        // Nothing to check for now
        } catch (Exception e) {
            String msg = e.getMessage();
            mStatusLbl.setText(msg);
            mStatusLbl.setIcon(GuiConstants.ERROR_ICON);
            throw new PropertyVetoException(msg, evt);
        }
    // everything looks good, window close
    }

    public void setValue() {
        IEPModel model = mComponent.getModel();
        SchemaComponentContainer scContainer = model.getPlanComponent().getSchemaComponentContainer();

        try {
            // name
            mNamePanel.store();

            // schema
            if (mIsSchemaOwner) {
                String newSchemaName = mOutputSchemaNamePanel.getStringValue();
                SchemaComponent outputSchema = mComponent.getOutputSchema();
                String schemaName = null;
                if (outputSchema != null) {
                    schemaName = outputSchema.getName();
                }

                boolean schemaExist = schemaName != null && !schemaName.trim().equals("") && outputSchema != null;
                //ritList attributes = mSelectPanel.getAttributeMetadataAsList();
                List<SchemaAttribute> attrs = mSelectPanel.getAttributes();
                if (schemaExist) {
                    if (!newSchemaName.equals(schemaName)) {
                        model.startTransaction();
                        SchemaComponent sc = model.getFactory().createSchema(model);
                        sc.setName(newSchemaName);
                        sc.setTitle(newSchemaName);
                        sc.setSchemaAttributes(attrs);
                        scContainer.addSchemaComponent(sc);
                        scContainer.removeSchemaComponent(outputSchema);
                        model.endTransaction();

                        mOutputSchemaNamePanel.store();
                    //ritmProperty.getNode().getView().updateTcgComponentNodeView();
                    //ritplan.getPropertyChangeSupport().firePropertyChange("Schema Name",
                    //        schemaName, newSchemaName);

                    } else {
                        mModel.startTransaction();
                        outputSchema.setSchemaAttributes(attrs);
                        mModel.endTransaction();
                    }
                } else {
                    model.startTransaction();
                    SchemaComponent sc = model.getFactory().createSchema(model);
                    sc.setName(newSchemaName);
                    sc.setTitle(newSchemaName);
                    sc.setSchemaAttributes(attrs);

                    scContainer.addSchemaComponent(sc);
                    model.endTransaction();

                    mOutputSchemaNamePanel.store();
                }

                if (mHasExpressionColumn) {
                    mModel.startTransaction();
                    // expression
                    List<String> expList = mSelectPanel.getExpressionList();
                    mComponent.setStringList(PROP_FROM_COLUMN_LIST, expList);

                    // to column list
                    List<String> toList = mSelectPanel.getToColumnList();
                    mComponent.setStringList(PROP_TO_COLUMN_LIST, toList);
                    mModel.endTransaction();
                    //ritmComponent.getProperty(PROP_TO_COLUMN_LIST).setValue(toList);

                    if (mHasFromClause && isShowFromClause()) {
                        // from clause
                        mFromPanel.store();
                    }

                    if (mHasWhereClause) {
                        // where clause
                        mWherePanel.store();
                    }

                    if (mHasGroupBy) {
                        // groupby column list
                        mGroupByPanel.store();
                    }
                }
            }

            //set documentation
            super.setDocumentation();

        } catch (Exception e) {
            e.printStackTrace();
            NotifyHelper.reportError(e.getMessage());
        }
    }

    protected boolean isShowFromClause() {
        return true;
    }

    public String generateUniqueAttributeName(String baseName) {
        if (mSelectPanel != null) {
            return mSelectPanel.generateUniqueAttributeName(baseName);
        }

        return null;
    }

    private void updateFromClause(AttributeInfo info) {
        //generate form clause automatically
        String from = mFromPanel.getStringValue();
        String entityName = info.getEntityName();

        if (from == null || from.trim().equals("")) {
            mFromPanel.setStringValue(entityName);
        } else {
            if (!(from != null && from.contains(entityName))) {
                StringBuffer strBuf = new StringBuffer();
                strBuf.append(from);
                strBuf.append(",");
                strBuf.append(entityName);
                mFromPanel.setStringValue(strBuf.toString());
            }
        }

    }

    class UpdateFromClauseExpressionAttributeDropNotificationListener implements AttributeDropNotificationListener {

        public void onDropComplete(AttributeDropNotificationEvent evt) {
            if (mFromPanel == null) {
                return;
            }

            //generate form clause automatically
            AttributeInfo info = evt.getAttributeInfo();
            updateFromClause(info);

        }
    }
}
