/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.edm.editor.graph.actions;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import org.netbeans.modules.edm.editor.utils.ImageConstants;
import org.netbeans.modules.edm.editor.utils.MashupGraphUtil;
import org.netbeans.modules.edm.editor.dataobject.MashupDataObject;
import org.netbeans.modules.edm.model.EDMException;
import org.netbeans.modules.edm.model.DBMetaDataFactory;
import org.netbeans.modules.edm.model.SQLConstants;
import org.netbeans.modules.edm.model.SQLDBTable;
import org.netbeans.modules.edm.model.SQLObject;
import org.netbeans.modules.edm.model.SourceTable;
import org.netbeans.modules.edm.model.impl.RuntimeInputImpl;
import org.netbeans.modules.edm.editor.utils.SQLObjectUtil;
import org.netbeans.modules.edm.editor.property.impl.PropertyNode;
import org.netbeans.modules.edm.editor.property.impl.TemplateFactory;
import org.netbeans.modules.edm.editor.ui.view.property.FFSourceTableProperties;
import org.netbeans.modules.edm.editor.ui.view.property.RuntimeInputProperties;
import org.netbeans.modules.edm.editor.ui.view.property.SQLCollaborationProperties;
import org.netbeans.modules.edm.editor.ui.view.property.SourceTableProperties;
import org.netbeans.modules.edm.editor.utils.Attribute;
import org.openide.awt.StatusDisplayer;
import org.openide.windows.WindowManager;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;

/**
 *
 * @author RamaChandraiah
 */
public class PropertiesAction extends AbstractAction {

    private MashupDataObject mObj;
    private SQLObject mSqlObj;

    public PropertiesAction() {
        super("", new ImageIcon(MashupGraphUtil.getImage(ImageConstants.PROPERTIES)));
    }

    public PropertiesAction(MashupDataObject dObj, String name, SQLObject sqlObj) {
        super(name, new ImageIcon(MashupGraphUtil.getImage(ImageConstants.PROPERTIES)));
        this.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('7', InputEvent.CTRL_DOWN_MASK + InputEvent.SHIFT_MASK));
        mObj = dObj;
        mSqlObj = sqlObj;
    }

    /**
     * called when this action is performed in the ui
     *
     * @param ev event
     */
    public void actionPerformed(ActionEvent ev) {
        String template = "Collaboration";

        Object pBean = new SQLCollaborationProperties(mObj.getModel().getSQLDefinition());
        if (mSqlObj != null) {
            if (mSqlObj.getObjectType() == SQLConstants.SOURCE_TABLE) {
                SourceTableProperties srcTableBaen = new SourceTableProperties((SourceTable) mSqlObj, mObj.getEditorView().getCollabSQLUIModel());
                if (((SourceTable) mSqlObj).getParent().getConnectionDefinition().getDBType().equals(DBMetaDataFactory.AXION) ||
                        ((SourceTable) mSqlObj).getParent().getConnectionDefinition().getDBType().equalsIgnoreCase("Internal")) {
                    template = getTemplateName(mSqlObj);
                    pBean = new FFSourceTableProperties(srcTableBaen);
                } else {
                    template = "SourceTable";
                    pBean = srcTableBaen;
                }
            } else if (mSqlObj.getObjectType() == SQLConstants.RUNTIME_INPUT) {
                pBean = new RuntimeInputProperties((RuntimeInputImpl) mSqlObj, mObj.getModel().getSQLDefinition());
                template = "RuntimeInput";
            }
        }

        PropertyNode pNode = mObj.getPropertyViewManager().getPropertyNodeForTemplateName(template, null, pBean);
        final Object pb = pBean;
        pNode.addPropertyChangeSupport(new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                // if value is differnt then only set it
                if (evt.getOldValue() != null && !evt.getOldValue().equals(evt.getNewValue())) {
                    try {
                        TemplateFactory.invokeSetter(pb, evt.getPropertyName(), evt.getNewValue());
                        //mObj.getMashupDataEditorSupport().synchDocument();
                        mObj.getModel().setDirty(true);
                        mObj.setModified(true);
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
