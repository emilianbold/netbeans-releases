/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.sql.framework.ui.view.graph;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingResourceException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.netbeans.modules.sql.framework.common.utils.TagParserUtility;
import org.netbeans.modules.sql.framework.model.GUIInfo;
import org.netbeans.modules.sql.framework.model.SQLCanvasObject;
import org.netbeans.modules.sql.framework.model.SQLCastOperator;
import org.netbeans.modules.sql.framework.model.SQLConnectableObject;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLInputObject;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SQLOperator;
import org.netbeans.modules.sql.framework.model.SQLOperatorArg;
import org.netbeans.modules.sql.framework.model.SQLPredicate;
import org.netbeans.modules.sql.framework.model.TargetColumn;
import org.netbeans.modules.sql.framework.model.VisibleSQLLiteral;
import org.netbeans.modules.sql.framework.model.impl.SQLCustomOperatorImpl;
import org.netbeans.modules.sql.framework.ui.graph.IGraphController;
import org.netbeans.modules.sql.framework.ui.graph.IGraphLink;
import org.netbeans.modules.sql.framework.ui.graph.IGraphNode;
import org.netbeans.modules.sql.framework.ui.graph.IGraphPort;
import org.netbeans.modules.sql.framework.ui.graph.IGraphView;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfo;
import org.netbeans.modules.sql.framework.ui.graph.impl.CustomOperatorNode;
import org.netbeans.modules.sql.framework.ui.model.SQLUIModel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.windows.WindowManager;
import net.java.hulp.i18n.Logger;
import com.sun.sql.framework.exception.BaseException;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.sql.framework.model.DBTable;
import org.netbeans.modules.sql.framework.model.DBTableCookie;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class SQLGraphController implements IGraphController {

    private static final String NETBEANS_NODE_MIMETYPE = "application/x-java-openide-nodednd; class=org.openide.nodes.Node";
    private static final String LOG_CATEGORY = SQLGraphController.class.getName();
    private static transient final Logger mLogger = Logger.getLogger(SQLGraphController.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
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

                        String dlgTitle = null;
                        String nbBundle1 = mLoc.t("BUND390: Add a table");
                        try {
                            dlgTitle = nbBundle1.substring(15);
                        } catch (MissingResourceException mre) {
                            dlgTitle = "Add a table";
                        }

                        // Recall and use most recently selected table type.
                        TypeSelectorPanel selectorPnl = new TypeSelectorPanel(tableTypeSelected);
                        DialogDescriptor dlgDesc = new DialogDescriptor(selectorPnl, dlgTitle, true, NotifyDescriptor.OK_CANCEL_OPTION,
                                NotifyDescriptor.OK_OPTION, DialogDescriptor.DEFAULT_ALIGN, null, null);
                        Dialog dlg = DialogDisplayer.getDefault().createDialog(dlgDesc);
                        dlg.getAccessibleContext().setAccessibleDescription("This dialog helps user to add a table");
                        dlg.setVisible(true);

                        if (NotifyDescriptor.OK_OPTION == dlgDesc.getValue()) {
                            tableTypeSelected = selectorPnl.getSelectedType();
                            if (SQLConstants.SOURCE_TABLE == tableTypeSelected) {
                                collabModel.addSourceTable(nodeTable, loc);
                            } else {
                                collabModel.addTargetTable(nodeTable, loc);
                            }

                            e.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                        } else {
                            e.rejectDrop();
                        }
                    }
                }
            } catch (IOException ex) {
                mLogger.errorNoloc(mLoc.t("EDIT150: Caught IOException while handling DnD{0}", LOG_CATEGORY), ex);

                e.rejectDrop();
            } catch (UnsupportedFlavorException ex) {
                mLogger.errorNoloc(mLoc.t("EDIT151: Caught UnsupportedFlavorException while handling DnD{0}", LOG_CATEGORY), ex);

                e.rejectDrop();
            } catch (BaseException ex) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(ex.getLocalizedMessage(), NotifyDescriptor.WARNING_MESSAGE));
                mLogger.errorNoloc(mLoc.t("EDIT152: Caught BaseException while handling DnD{0}", LOG_CATEGORY), ex);

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
        if (existing instanceof TargetColumn) {
            existing = ((TargetColumn) existing).getValue();
        }

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

    private boolean doTypeChecking(SQLCanvasObject srcObj, SQLConnectableObject destObj, String srcParam1, String destParam1) throws BaseException {
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
                    String nbBundle2 = mLoc.t("BUND396: Cannot connect {0} to {1}-{2} at input {3}.",srcObjType,
                        destObjType, destObj.getDisplayName(), destParam1);
                    msg = nbBundle2.substring(15);
                } else {
                    String nbBundle3 = mLoc.t("BUND397: Cannot connect {0} to {1}",srcObjType, destObjType);
                    msg = nbBundle3.substring(15);
                }
            } catch (Exception e) {
                mLogger.errorNoloc(mLoc.t("EDIT153: Caught Exception while resolving error message{0}", LOG_CATEGORY), e);

                msg = "Cannot link these objects together.";
            }

            NotifyDescriptor.Message m = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);

            DialogDisplayer.getDefault().notify(m);
            return false;
        }

        switch (destObj.isInputCompatible(destParam1, input)) {
            case SQLConstants.TYPE_CHECK_INCOMPATIBLE:
                try {
                    String nbBundle3 = mLoc.t("BUND398: Incompatible source and target datatypes.");
                    msg = nbBundle3.substring(15);
                } catch (MissingResourceException e) {
                    msg = "Incompatible source and target datatypes.";
                }

                NotifyDescriptor.Message m = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);

                DialogDisplayer.getDefault().notify(m);
                return false;

            case SQLConstants.TYPE_CHECK_DOWNCAST_WARNING:
                try {
                    String nbBundle4 = mLoc.t("BUND399: Connecting these types may result in a loss of precision or data truncation.  Continue?");
                    msg = nbBundle4.substring(15);
                } catch (MissingResourceException e) {
                    msg = "Connecting these datatypes may result in a loss of " + "precision or data truncation in the target.  Continue?";
                }

                String title = null;
                try {
                    String nbBundle5 = mLoc.t("BUND400: Datatype conversion");
                    title = nbBundle5.substring(15);
                } catch (MissingResourceException e) {
                    title = "Datatype conversion";
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

            String nbBundle6 = mLoc.t("BUND401: New Cast-As Operator");
            //do special processing for following objects
            switch (sqlObj.getObjectType()) {
                case SQLConstants.CAST_OPERATOR:
                    CastAsDialog castDlg = new CastAsDialog(WindowManager.getDefault().getMainWindow(),
                            nbBundle6.substring(15), true);
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
                    String nbBundle7 = mLoc.t("BUND402: User Function");
                    String title = nbBundle7.substring(15);
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
                    String nbBundle8 = mLoc.t("BUND403: New Literal Object");
                    LiteralDialog dlg = new LiteralDialog(WindowManager.getDefault().getMainWindow(),
                            nbBundle8.substring(15), true);
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
        } catch (BaseException e) {
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

            mLogger.errorNoloc(mLoc.t("EDIT166: Caught exception while removing object{0}", LOG_CATEGORY), e);
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

    class TypeSelectorPanel extends JPanel {

        private ButtonGroup bg;
        private JRadioButton source;
        private JRadioButton target;

        public TypeSelectorPanel() {
            this(SQLConstants.SOURCE_TABLE);
        }

        public TypeSelectorPanel(int newType) {
            super();
            setLayout(new BorderLayout());

            JPanel insetPanel = new JPanel();
            insetPanel.setLayout(new BoxLayout(insetPanel, BoxLayout.PAGE_AXIS));
            insetPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 0, 15));

            String title = "";
            try {
                String nbBundle9 = mLoc.t("BUND404: Select table type:");
                title = nbBundle9.substring(15);
            } catch (MissingResourceException mre) {
                title = "Specify table type:";
            }

            insetPanel.add(new JLabel(title));

            String sourceLabel = "";
            try {
                String nbBundle10 = mLoc.t("BUND405: Source table");
                sourceLabel = nbBundle10.substring(15);
            } catch (MissingResourceException mre) {
                sourceLabel = "Source table";
            }

            String targetLabel = "";
            try {
                 String nbBundle11 = mLoc.t("BUND406: Target table");
                targetLabel = nbBundle11.substring(15);
            } catch (MissingResourceException mre) {
                targetLabel = "Target table";
            }

            source = new JRadioButton(sourceLabel);
            target = new JRadioButton(targetLabel);

            insetPanel.add(source);
            insetPanel.add(target);
            add(insetPanel, BorderLayout.CENTER);

            bg = new ButtonGroup();
            bg.add(source);
            bg.add(target);

            setSelectedType(newType);
        }

        public void setSelectedType(int type) {
            switch (type) {
                case SQLConstants.TARGET_TABLE:
                    bg.setSelected(target.getModel(), true);
                    break;

                case SQLConstants.SOURCE_TABLE:
                default:
                    bg.setSelected(source.getModel(), true);
            }
        }

        public int getSelectedType() {
            return target.isSelected() ? SQLConstants.TARGET_TABLE : SQLConstants.SOURCE_TABLE;
        }

        @Override
        public void addNotify() {
            super.addNotify();

            switch (getSelectedType()) {
                case SQLConstants.TARGET_TABLE:
                    target.requestFocusInWindow();
                    break;

                case SQLConstants.SOURCE_TABLE:
                default:
                    source.requestFocusInWindow();
                    break;
            }
        }
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

