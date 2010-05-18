/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.edm.editor.graph;

import java.awt.Dialog;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.MissingResourceException;


import org.netbeans.modules.edm.editor.utils.TagParserUtility;
import org.netbeans.modules.edm.model.GUIInfo;
import org.netbeans.modules.edm.model.SQLCanvasObject;
import org.netbeans.modules.edm.model.SQLCastOperator;
import org.netbeans.modules.edm.model.SQLConnectableObject;
import org.netbeans.modules.edm.model.SQLConstants;
import org.netbeans.modules.edm.model.SQLInputObject;
import org.netbeans.modules.edm.model.SQLObject;
import org.netbeans.modules.edm.model.SQLOperator;
import org.netbeans.modules.edm.model.SQLOperatorArg;
import org.netbeans.modules.edm.model.SQLPredicate;
import org.netbeans.modules.edm.model.VisibleSQLLiteral;
import org.netbeans.modules.edm.model.impl.SQLCustomOperatorImpl;
import org.netbeans.modules.edm.editor.graph.jgo.IGraphController;
import org.netbeans.modules.edm.editor.graph.jgo.IGraphLink;
import org.netbeans.modules.edm.editor.graph.jgo.IGraphNode;
import org.netbeans.modules.edm.editor.graph.jgo.IGraphPort;
import org.netbeans.modules.edm.editor.graph.jgo.IGraphView;
import org.netbeans.modules.edm.editor.graph.jgo.IOperatorXmlInfo;
import org.netbeans.modules.edm.editor.graph.jgo.CustomOperatorNode;
import org.netbeans.modules.edm.editor.ui.model.SQLUIModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.windows.WindowManager;
import org.netbeans.modules.edm.model.EDMException;
import org.netbeans.modules.edm.model.DBTable;
import org.netbeans.modules.edm.model.DBTableCookie;
import org.openide.util.NbBundle;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class SQLGraphController implements IGraphController {

    private static final String NETBEANS_NODE_MIMETYPE = "application/x-java-openide-nodednd; class=org.openide.nodes.Node";
    private static final String LOG_CATEGORY = SQLGraphController.class.getName();
    private static transient final Logger mLogger = Logger.getLogger(SQLGraphController.class.getName());
    private static DataFlavor[] mDataFlavorArray = new DataFlavor[1];
    

    static {
        try {
            mDataFlavorArray[0] = new DataFlavor(NETBEANS_NODE_MIMETYPE);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }
    protected SQLUIModel collabModel;
    protected IGraphView viewC;
    private String srcParam = null;
    private String destParam = null;
    private transient int tableTypeSelected = SQLConstants.SOURCE_TABLE;

    /** Creates a new instance of SQLGraphController */
    public SQLGraphController() {
    }

    /**
     * Handle drop.
     * 
     * @param e DropTargetDropEvent
     */
    public void handleDrop(java.awt.dnd.DropTargetDropEvent e) {
        if (!isEditAllowed()) {
            return;
        }

        Point loc = e.getLocation();
        if (e.isDataFlavorSupported(mDataFlavorArray[0])) {
            try {
                Transferable t = e.getTransferable();
                Object o = t.getTransferData(mDataFlavorArray[0]);
                if (o instanceof Node) {
                    Node.Cookie tableCookie = ((Node) o).getCookie(DBTableCookie.class);
                    if (tableCookie != null) {
                        DBTable nodeTable = ((DBTableCookie) tableCookie).getDBTable();
                        // Recall and use most recently selected table type.
                        if (SQLConstants.SOURCE_TABLE == tableTypeSelected) {
                            collabModel.addSourceTable(nodeTable, loc);
                        }

                        e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                    }
                }
            } catch (IOException ex) {
                mLogger.log(Level.INFO,NbBundle.getMessage(SQLGraphController.class, "LOG.INFO_Caught_IOException",new Object[] {LOG_CATEGORY}),ex);

                e.rejectDrop();
            } catch (UnsupportedFlavorException ex) {
                mLogger.log(Level.INFO,NbBundle.getMessage(SQLGraphController.class, "LOG.INFO_Caught_UnsupportedFlavorException",new Object[] {LOG_CATEGORY}),ex);

                e.rejectDrop();
            } catch (EDMException ex) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(ex.getLocalizedMessage(), NotifyDescriptor.WARNING_MESSAGE));
                mLogger.log(Level.INFO,NbBundle.getMessage(SQLGraphController.class, "LOG.INFO_Caught_BaseException",new Object[] {LOG_CATEGORY}),ex);

                e.rejectDrop();
            }
        } else {
            e.rejectDrop();
        }
    }

    /**
     * Handle drop of arbitrary object.
     * 
     * @param obj Object dropped onto canvas
     */
    public void handleObjectDrop(Object obj) {
        if (!isEditAllowed()) {
            return;
        }
    }

    /**
     * handle new link
     * 
     * @param from IGraphPort
     * @param to IGraphPort
     */
    public void handleLinkAdded(IGraphPort from, IGraphPort to) {
        if (!isEditAllowed()) {
            return;
        }

        IGraphNode srcGraphNode = null;
        IGraphNode destGraphNode = null;

        srcGraphNode = from.getDataNode();
        destGraphNode = to.getDataNode();

        if (srcGraphNode != null && destGraphNode != null && srcGraphNode.equals(destGraphNode)) {
            return;
        }

        setParameters(from, to, srcGraphNode, destGraphNode);

        SQLCanvasObject srcObj = (SQLCanvasObject) srcGraphNode.getDataObject();
        SQLConnectableObject destObj = (SQLConnectableObject) destGraphNode.getDataObject();

        if (srcObj == null && destObj == null) {
            return;
        }

        SQLInputObject inputObj = destObj.getInput(destParam);
        SQLObject existing = (inputObj != null) ? inputObj.getSQLObject() : null;
        if (existing != null) {
            return;
        }

        try {
            boolean userResponse = doTypeChecking(srcObj, destObj, srcParam, destParam);

            if (srcObj != null && destObj != null && userResponse) {
                collabModel.createLink(srcObj, srcParam, destObj, destParam);
            }
        } catch (Exception sqle) {
            NotifyDescriptor d = new NotifyDescriptor.Message(sqle.toString(), NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }
    }

    private boolean doTypeChecking(SQLCanvasObject srcObj, SQLConnectableObject destObj, String srcParam1, String destParam1) throws EDMException {
        // Ignore join for type checking purposes.
        //        if (srcObj.getObjectType() == SQLConstants.JOIN
        //                || destObj instanceof SQLJoinOperator) {
        //            return true;
        //        }

        String msg = null;
        SQLObject input = srcObj;

        //get the specific sub object from srcObj which we are trying to link
        input = srcObj.getOutput(srcParam1);
        // Obtain SourceColumn (an SQLObject) if srcObj is a source table.
        //        if (srcObj.getObjectType() == SQLConstants.SOURCE_TABLE) {
        //            DBColumn srcColumn = ((SourceTable) srcObj).getColumn(srcParam1);
        //            if (srcColumn instanceof SourceColumn) {
        //                input = (SourceColumn) srcColumn;
        //            }
        //        }

        if (!destObj.isInputValid(destParam1, input)) {
            try {
                String srcObjType = TagParserUtility.getDisplayStringFor(input.getObjectType());
                String destObjType = TagParserUtility.getDisplayStringFor(destObj.getObjectType());
                String srcName = destObj.getDisplayName();

                if (srcName != null && destParam1 != null) {
                    msg = NbBundle.getMessage(SQLGraphController.class, "MSG_Cannot_connect",new Object[] {srcObjType, destObjType, destObj.getDisplayName(), destParam1});
                } else {
                    msg =NbBundle.getMessage(SQLGraphController.class, "ERROR_Cannot_connect",new Object[] {srcObjType, destObjType});
                }
            } catch (Exception e) {
                mLogger.log(Level.INFO,NbBundle.getMessage(SQLGraphController.class, "MSG_Caught_Exception_while_resolving_error_message",new Object[] {LOG_CATEGORY}),e);
                msg = NbBundle.getMessage(SQLGraphController.class, "MSG_Cannot_link_objects");
            }

            NotifyDescriptor.Message m = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);

            DialogDisplayer.getDefault().notify(m);
            return false;
        }

        switch (destObj.isInputCompatible(destParam1, input)) {
            case SQLConstants.TYPE_CHECK_INCOMPATIBLE:
                try {
                    msg = NbBundle.getMessage(SQLGraphController.class, "ERR_type_check_incompatible");
                } catch (MissingResourceException e) {
                    msg = NbBundle.getMessage(SQLGraphController.class, "ERR_type_check_incompatible");
                }

                NotifyDescriptor.Message m = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);

                DialogDisplayer.getDefault().notify(m);
                return false;

            case SQLConstants.TYPE_CHECK_DOWNCAST_WARNING:
                try {
                    msg = NbBundle.getMessage(SQLGraphController.class, "LBL_data_truncation");
                } catch (MissingResourceException e) {
                    msg = NbBundle.getMessage(SQLGraphController.class, "LBL_data_truncation");
                }

                String title = null;
                try {
                    title = NbBundle.getMessage(SQLGraphController.class, "TITLE_Datatype_conversion");
                } catch (MissingResourceException e) {
                    title = NbBundle.getMessage(SQLGraphController.class, "TITLE_Datatype_conversion");
                }

                NotifyDescriptor.Confirmation d = new NotifyDescriptor.Confirmation(msg, title, NotifyDescriptor.OK_CANCEL_OPTION,
                        NotifyDescriptor.QUESTION_MESSAGE);

                return (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION);

            case SQLConstants.TYPE_CHECK_COMPATIBLE:
            default:
                return true;
        }
    }

    private void setParameters(IGraphPort from, IGraphPort to, IGraphNode srcGraphNode, IGraphNode destGraphNode) {
        if (srcGraphNode != null && destGraphNode != null) {
            srcParam = srcGraphNode.getFieldName(from);
            destParam = destGraphNode.getFieldName(to);
        }
    }

    /**
     * handle link deletion
     * 
     * @param link IGraphLink
     */
    public void handleLinkDeleted(IGraphLink link) {
        if (!isEditAllowed()) {
            return;
        }

        IGraphPort from = link.getFromGraphPort();
        IGraphPort to = link.getToGraphPort();
        IGraphNode srcGraphNode = from.getDataNode();
        IGraphNode destGraphNode = to.getDataNode();

        setParameters(from, to, srcGraphNode, destGraphNode);

        //source is always canvas object and destination is always expression object
        SQLCanvasObject srcObj = (SQLCanvasObject) srcGraphNode.getDataObject();
        SQLConnectableObject destObj = (SQLConnectableObject) destGraphNode.getDataObject();

        if (srcObj == null && destObj == null) {
            return;
        }

        try {
            collabModel.removeLink(srcObj, srcParam, destObj, destParam);
        } catch (Exception e) {
            NotifyDescriptor d = new NotifyDescriptor.Message(e.toString(), NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }
    }

    /**
     * handle node add
     * 
     * @param xmlInfo IOperatorXmlInfo
     * @param dropLocation dropLocation
     */
    @SuppressWarnings("fallthrough")
    public void handleNodeAdded(IOperatorXmlInfo xmlInfo, Point dropLocation) {
        if (!isEditAllowed()) {
            return;
        }

        //what object type is dropped
        String className = xmlInfo.getObjectClassName();

        try {
            //create object
            SQLCanvasObject sqlObj = collabModel.createObject(className);
            sqlObj.setDisplayName(xmlInfo.getName());

            GUIInfo guiInfo = sqlObj.getGUIInfo();
            guiInfo.setX(dropLocation.x);
            guiInfo.setY(dropLocation.y);

            //do special processing for following objects
            switch (sqlObj.getObjectType()) {
                case SQLConstants.CAST_OPERATOR:
                    CastAsDialog castDlg = new CastAsDialog(WindowManager.getDefault().getMainWindow(),
                            "New Cast-As Operator", true);
                    castDlg.show();
                    if (castDlg.isCanceled()) {
                        return;
                    }

                    SQLCastOperator castOp = (SQLCastOperator) sqlObj;
                    castOp.setOperatorXmlInfo(xmlInfo);

                    castOp.setJdbcType(castDlg.getJdbcType());

                    int precision = castDlg.getPrecision();
                    castOp.setPrecision(precision);

                    int scale = castDlg.getScale();
                    castOp.setScale(scale);

                    break;

                case SQLConstants.CUSTOM_OPERATOR:
                    CustomOperatorPane customOptPane = new CustomOperatorPane(new ArrayList());
                    String title = "User Function";
                    DialogDescriptor dlgDesc = new DialogDescriptor(customOptPane, title, true, NotifyDescriptor.OK_CANCEL_OPTION,
                            NotifyDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN, null, null);
                    Dialog customOptDialog = DialogDisplayer.getDefault().createDialog(dlgDesc);
                    customOptDialog.getAccessibleContext().setAccessibleDescription("This dialog hepls user to configure user-defined functions");
                    customOptDialog.setVisible(true);
                    if (NotifyDescriptor.CANCEL_OPTION == dlgDesc.getValue()) {
                        return;
                    }
                    List inputArgs = customOptPane.getArgList();
                    SQLOperatorArg retType = customOptPane.getReturnType();
                    CustomOperatorNode customOptNode = new CustomOperatorNode(xmlInfo, inputArgs, retType);
                    SQLCustomOperatorImpl custOp = (SQLCustomOperatorImpl) sqlObj;
                    custOp.setOperatorXmlInfo(customOptNode);
                    custOp.setCustomOperatorName(customOptPane.getFunctionName());
                    custOp.getOperatorDefinition().setArgList(inputArgs);
                    custOp.initializeInputs(inputArgs.size());
                    /**
                    CustomOperatorDialog custOprDlg = new CustomOperatorDialog(WindowManager.getDefault().getMainWindow(), NbBundle.getMessage(
                    BasicSQLGraphController.class, "TITLE_user_function"), true);
                    custOprDlg.show();
                    if (custOprDlg.isCanceled()) {
                    return;
                    }
                    
                    SQLCustomOperatorImpl custOp = (SQLCustomOperatorImpl) sqlObj;
                    custOp.setOperatorXmlInfo(xmlInfo);
                    custOp.setCustomOperatorName(custOprDlg.getFunctionName());
                    custOp.initializeInputs(custOprDlg.getNumberOfArguments());
                     */
                    break;

                case SQLConstants.VISIBLE_PREDICATE:
                    ((SQLPredicate) sqlObj).setOperatorXmlInfo(xmlInfo);
                // fall through to set XML info (using common SQLOperator interface)

                case SQLConstants.GENERIC_OPERATOR:
                case SQLConstants.DATE_ARITHMETIC_OPERATOR:
                    //for operator we need to set the type of operator
                    // ((SQLGenericOperator) sqlObj).setOperatorType(xmlInfo.getName());
                    ((SQLOperator) sqlObj).setOperatorXmlInfo(xmlInfo);
                    sqlObj.setDisplayName(xmlInfo.getDisplayName());
                    break;
                    
                   
                case SQLConstants.VISIBLE_LITERAL:
                    LiteralDialog dlg = new LiteralDialog(WindowManager.getDefault().getMainWindow(),
                            "New Literal Object", true);
                    dlg.show();

                    // OK button is not pressed so return
                    if (dlg.isCanceled()) {
                        return;
                    }

                    String value = dlg.getLiteral();
                    VisibleSQLLiteral lit = (VisibleSQLLiteral) sqlObj;
                    lit.setJdbcType(dlg.getType());
                    lit.setValue(value);
                    lit.setDisplayName(xmlInfo.getDisplayName());

                    break;
            }

            //now add the object
            collabModel.addObject(sqlObj);
        //also flag if java operators are to be used
        } catch (EDMException e) {
            NotifyDescriptor d = new NotifyDescriptor.Message(e.toString(), NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }
    }

    /**
     * handle node deletion
     * 
     * @param node IGraphNode
     */
    public void handleNodeRemoved(IGraphNode node) {
        if (!isEditAllowed()) {
            return;
        }

        try {
            IGraphNode pNode = node.getParentGraphNode();
            //if node has a parent then we should delete it from parent and return
            //we do not need to go to collaboration as node is contained within
            //its parent and deleting it from its parent should remove it
            if (pNode != null) {
                pNode.removeChildNode(node);
                return;
            }

            SQLCanvasObject sqlObj = (SQLCanvasObject) node.getDataObject();
            if (sqlObj != null) {
                collabModel.removeObject(sqlObj);
            }
        } catch (Exception e) {

            mLogger.log(Level.INFO,NbBundle.getMessage(SQLGraphController.class, "LOG.INFO_Caught_exception",new Object[] {LOG_CATEGORY}),e);
            NotifyDescriptor d = new NotifyDescriptor.Message(e.toString(), NotifyDescriptor.INFORMATION_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }
    }

    /**
     * Sets the data model which this controller modifies
     * 
     * @param newModel new data model
     */
    public void setDataModel(Object newModel) {
        collabModel = (SQLUIModel) newModel;

    }

    public Object getDataModel() {
        return collabModel;
    }


    protected boolean isEditAllowed() {
        if (viewC != null) {
            return viewC.canEdit();
        }

        return true;
    }

    /**
     * set the view from which this controller interacts
     * 
     * @param view view
     */
    public void setView(Object view) {
        viewC = (IGraphView) view;
    }
}

