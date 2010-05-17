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
package org.netbeans.modules.edm.editor.widgets.property;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.edm.editor.property.impl.PropertyNode;
import org.openide.ErrorManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;

import org.netbeans.modules.edm.model.SourceTable;
import org.netbeans.modules.edm.model.impl.SourceTableImpl;
import java.util.logging.Logger;
import org.netbeans.modules.edm.editor.ui.view.MashupDataObjectProvider;
import org.netbeans.modules.edm.editor.ui.view.MashupEditorTopView;

import org.netbeans.modules.edm.model.EDMException;
import org.netbeans.modules.edm.model.DBMetaDataFactory;
import org.netbeans.modules.edm.model.SQLConstants;
import org.netbeans.modules.edm.model.SQLDBTable;
import org.netbeans.modules.edm.model.SQLDefinition;
import org.netbeans.modules.edm.model.SQLObject;
import org.netbeans.modules.edm.model.impl.RuntimeInputImpl;
import org.netbeans.modules.edm.editor.utils.SQLObjectUtil;
import org.netbeans.modules.edm.editor.property.impl.TemplateFactory;
import org.netbeans.modules.edm.editor.ui.view.property.FFSourceTableProperties;
import org.netbeans.modules.edm.editor.ui.view.property.RuntimeInputProperties;
import org.netbeans.modules.edm.editor.ui.view.property.SQLCollaborationProperties;
import org.netbeans.modules.edm.editor.ui.view.property.SourceTableProperties;
import org.netbeans.modules.edm.editor.utils.Attribute;
import org.openide.awt.StatusDisplayer;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.windows.WindowManager;

/**
 *
 * @author karthikeyan s
 */
public class TableNode extends AbstractNode {
    
    private SourceTableImpl dbTable;
    
    public TableNode(SourceTable obj) {
        super(Children.LEAF);
        dbTable = (SourceTableImpl) obj;
    }
    
    @Override
    public boolean canCopy() {
        return false;
    }
    
    @Override
    public boolean canRename() {
        return false;
    }
    
    @Override
    public boolean canCut() {
        return false;
    }
    
    @Override
    public boolean canDestroy() {
        return true;
    }
    
    /** Creates a property sheet. */
    @Override
    protected Sheet createSheet() {
        Sheet sheet = Sheet.createDefault();
        Sheet.Set set = Sheet.createPropertiesSet();
        try {
            
            SQLDefinition def = SQLObjectUtil.getAncestralSQLDefinition(this.dbTable);
            MashupEditorTopView edmView = MashupDataObjectProvider.getProvider().getActiveDataObject().getEditorView();
            String template = "Collaboration";
            Object pBean = new SQLCollaborationProperties(def);
            
            if (this.dbTable.getObjectType() == SQLConstants.SOURCE_TABLE) {
                SourceTableProperties srcTableBaen = new SourceTableProperties(dbTable, edmView.getCollabSQLUIModel());
                if ((dbTable.getParent().getConnectionDefinition().getDBType().equals(DBMetaDataFactory.AXION) ||
                        dbTable.getParent().getConnectionDefinition().getDBType().equalsIgnoreCase("Internal"))) {
                    template = getTemplateName(dbTable);
                    pBean = new FFSourceTableProperties(srcTableBaen);
                } else {
                    template = "SourceTable";
                    pBean = srcTableBaen;
                }
            } else if (dbTable.getObjectType() == SQLConstants.RUNTIME_INPUT) {
                pBean = new RuntimeInputProperties((RuntimeInputImpl) dbTable, def);
                template = "RuntimeInput";
            }
             
            PropertyNode pNode = MashupDataObjectProvider.getProvider().getActiveDataObject().getPropertyViewManager().getPropertyNodeForTemplateName(template, null, pBean);
            final Object pb = pBean;
            pNode.addPropertyChangeSupport(new PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent evt) {
                    // if value is differnt then only set it
                    if (evt.getOldValue() != null && !evt.getOldValue().equals(evt.getNewValue())) {
                        try {
                            TemplateFactory.invokeSetter(pb, evt.getPropertyName(), evt.getNewValue());
                            MashupDataObjectProvider.getProvider().getActiveDataObject().getMashupDataEditorSupport().synchDocument();
                            MashupDataObjectProvider.getProvider().getActiveDataObject().setModified(true);
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            });
        WindowManager.getDefault().getRegistry().getActivated().setActivatedNodes(new Node[]{pNode});              
        if (!WindowManager.getDefault().findTopComponent("properties").isShowing()) {
            WindowManager.getDefault().findTopComponent("properties").open();
        }
        } catch (Exception ex) {
            ErrorManager.getDefault().notify(ex);
        }
        sheet.put(set);
        return sheet;
    }
    
    private String getTemplateName(SQLObject bean) {
        String template = null;
        Attribute attr = bean.getAttribute("ORGPROP_LOADTYPE");
        try {
            SQLObjectUtil.setOrgProperties((SQLDBTable) bean);
            attr = bean.getAttribute("ORGPROP_LOADTYPE");
        } catch (EDMException ex) {
            StatusDisplayer.getDefault().setStatusText(ex.getMessage());
            if (bean.getObjectType() == SQLConstants.SOURCE_TABLE) {
                template = "FFSourceTable";
            }
            return template;
        }

        if (bean.getObjectType() == SQLConstants.SOURCE_TABLE) {
            if (attr == null) {
                template = "FFSourceTable";
                return template;
            }
            if (((String) attr.getAttributeValue()).equals("RSS")) {
                template = "RSSSourceTable";
            } else if (((String) attr.getAttributeValue()).equalsIgnoreCase("SPREADSHEET")) {
                template = "SpreadsheetSourceTable";
             } else if (((String) attr.getAttributeValue()).equalsIgnoreCase("WEB")) {
                template = "WebSourceTable";
            } else if (((String) attr.getAttributeValue()).equalsIgnoreCase("WEBROWSET")) {
                template = "WebrowsetSourceTable";
            } else if (((String) attr.getAttributeValue()).equalsIgnoreCase("DELIMITED") ||
                    ((String) attr.getAttributeValue()).equalsIgnoreCase("FIXEDWIDTH")) {
                template = "FFSourceTable";
            } else {
                template = "FFSourceTable";
            }
        }
        return template;
    }
}