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
package org.netbeans.modules.sql.framework.ui.view.conditionbuilder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

import org.netbeans.modules.sql.framework.codegen.SQLOperatorFactory;
import org.netbeans.modules.sql.framework.model.SQLConnectableObject;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLDBColumn;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.SQLModelObjectFactory;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SQLOperatorDefinition;
import org.netbeans.modules.sql.framework.model.SQLPredicate;
import org.netbeans.modules.sql.framework.model.ValidationInfo;
import org.netbeans.modules.sql.framework.model.utils.ConditionUtil;
import org.netbeans.modules.sql.framework.model.visitors.SQLValidationVisitor;
import org.netbeans.modules.sql.framework.parser.conditionparser.ParseException;
import org.netbeans.modules.sql.framework.ui.graph.IGraphView;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfo;
import org.netbeans.modules.sql.framework.ui.graph.actions.GraphAction;
import org.netbeans.modules.sql.framework.ui.graph.view.impl.BasicToolBar;
import org.netbeans.modules.sql.framework.ui.model.CollabSQLUIModel;
import org.netbeans.modules.sql.framework.ui.view.IGraphViewContainer;
import org.netbeans.modules.sql.framework.ui.output.IMessageView;
import org.netbeans.modules.sql.framework.ui.output.SQLLogView;
import org.netbeans.modules.sql.framework.ui.view.conditionbuilder.actions.ValidateSQLAction;
import org.openide.windows.TopComponent;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.sql.framework.ui.output.SQLEditorPanel;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class ConditionBuilderExpRightPanel extends TopComponent {

    private static transient final Logger mLogger = Logger.getLogger(ConditionBuilderExpRightPanel.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    private class ConditionDocumentListener implements DocumentListener {

        /**
         * Gives notification that an attribute or set of attributes changed.
         * 
         * @param e the document event
         */
        public void changedUpdate(DocumentEvent e) {
        }

        /**
         * Gives notification that there was an insert into the document. The range given
         * by the DocumentEvent bounds the freshly inserted region.
         * 
         * @param e the document event
         */
        public void insertUpdate(DocumentEvent e) {
            setDirty(true);
        }

        /**
         * Gives notification that a portion of the document has been removed. The range
         * is given in terms of what the view last saw (that is, before updating sticky
         * positions).
         * 
         * @param e the document event
         */
        public void removeUpdate(DocumentEvent e) {
            setDirty(true);
        }
    }

    private class TextDropTargetListener implements DropTargetListener {

        /**
         * Called while a drag operation is ongoing, when the mouse pointer enters the
         * operable part of the drop site for the <code>DropTarget</code> registered
         * with this listener.
         * 
         * @param dtde the <code>DropTargetDragEvent</code>
         */
        public void dragEnter(DropTargetDragEvent dtde) {
        }

        /**
         * Called while a drag operation is ongoing, when the mouse pointer has exited the
         * operable part of the drop site for the <code>DropTarget</code> registered
         * with this listener.
         * 
         * @param dte the <code>DropTargetEvent</code>
         */
        public void dragExit(DropTargetEvent dte) {
        }

        /**
         * Called when a drag operation is ongoing, while the mouse pointer is still over
         * the operable part of the drop site for the <code>DropTarget</code> registered
         * with this listener.
         * 
         * @param dtde the <code>DropTargetDragEvent</code>
         */
        public void dragOver(DropTargetDragEvent dtde) {
        }

        /**
         * Called when the drag operation has terminated with a drop on the operable part
         * of the drop site for the <code>DropTarget</code> registered with this
         * listener.
         * <p>
         * This method is responsible for undertaking the transfer of the data associated
         * with the gesture. The <code>DropTargetDropEvent</code> provides a means to
         * obtain a <code>Transferable</code> object that represents the data object(s)
         * to be transfered.
         * <P>
         * From this method, the <code>DropTargetListener</code> shall accept or reject
         * the drop via the acceptDrop(int dropAction) or rejectDrop() methods of the
         * <code>DropTargetDropEvent</code> parameter.
         * <P>
         * Subsequent to acceptDrop(), but not before, <code>DropTargetDropEvent</code>
         * 's getTransferable() method may be invoked, and data transfer may be performed
         * via the returned <code>Transferable</code>'s getTransferData() method.
         * <P>
         * At the completion of a drop, an implementation of this method is required to
         * signal the success/failure of the drop by passing an appropriate
         * <code>boolean</code> to the <code>DropTargetDropEvent</code>'s
         * dropComplete(boolean success) method.
         * <P>
         * Note: The data transfer should be completed before the call to the
         * <code>DropTargetDropEvent</code>'s dropComplete(boolean success) method.
         * After that, a call to the getTransferData() method of the
         * <code>Transferable</code> returned by
         * <code>DropTargetDropEvent.getTransferable()</code> is guaranteed to succeed
         * only if the data transfer is local; that is, only if
         * <code>DropTargetDropEvent.isLocalTransfer()</code> returns <code>true</code>.
         * Otherwise, the behavior of the call is implementation-dependent.
         * <P>
         * 
         * @param dtde the <code>DropTargetDropEvent</code>
         */
        public void drop(DropTargetDropEvent dtde) {
            boolean dropSucceeded = false;

            try {
                if (dtde.isDataFlavorSupported(mDataFlavorArray[0])) {
                    dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                    Transferable tr = dtde.getTransferable();
                    Object data = tr.getTransferData(mDataFlavorArray[0]);

                    boolean selectionExists = (pane.getSelectedText() != null);
                    String textToDrop = null;

                    if (data instanceof SQLObject) {
                        SQLObject sqlObj = (SQLObject) data;

                        if (sqlObj instanceof SQLDBColumn) {
                            SQLDBColumn column = (SQLDBColumn) sqlObj;
                            SQLDBTable table = (SQLDBTable) column.getParentObject();

                            // Handle runtime input argument, we need to append $ sign
                            if (table.getObjectType() == SQLConstants.RUNTIME_INPUT) {
                                textToDrop = "$" + column.getName();
                            } else {
                                textToDrop = sqlObj.toString();
                            }
                        } else {
                            textToDrop = sqlObj.toString();
                        }

                        dropSucceeded = true;
                    } else if (data instanceof IOperatorXmlInfo) {
                        IOperatorXmlInfo opInfo = (IOperatorXmlInfo) data;
                        try {
                            SQLOperatorDefinition opDef = SQLOperatorFactory.getDefault().getSQLOperatorDefinition(opInfo.getName());
                            if (opDef != null) {
                                textToDrop = opDef.getGuiName();
                            } else {
                                textToDrop = opInfo.getName();
                            }

                            dropSucceeded = true;
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }

                    if (textToDrop != null && dropSucceeded) {
                        // Add trailing space to dropped string.
                        textToDrop += " ";
                        if (selectionExists) {
                            pane.replaceSelection(textToDrop);
                        } else {
                            pane.getDocument().insertString(pane.getCaretPosition(), textToDrop, null);
                        }
                    }
                } else {
                    dtde.rejectDrop();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                dtde.dropComplete(dropSucceeded);
                if (dropSucceeded) {
                    pane.requestFocusInWindow();
                }
            }
        }

        /**
         * Called if the user has modified the current drop gesture.
         * <P>
         * 
         * @param dtde the <code>DropTargetDragEvent</code>
         */
        public void dropActionChanged(DropTargetDragEvent dtde) {
        }
    }

    private class ValidationListener implements ActionListener {

        /**
         * Invoked when an action occurs.
         */
        public void actionPerformed(ActionEvent e) {
            try {
                IGraphView view = viewContainer.getGraphView();
                if (view != null) {
                    CollabSQLUIModel model = (CollabSQLUIModel) view.getGraphModel();
                    if (model != null) {
                        SQLObject obj = ConditionUtil.parseCondition(getCondition(), model.getSQLDefinition());
                        valArea.setText(obj.toString());
                        if (gPanel != null) {
                            gPanel.refresh(obj);
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                valArea.setText(ex.getMessage());
            }
        }
    }
    protected static DataFlavor[] mDataFlavorArray = new DataFlavor[1];
    private static final double PREFERRED_DIVIDER_RATIO = 3.0 / 5.0;
    private static final double SPLITPANE_RESIZE_WEIGHT = PREFERRED_DIVIDER_RATIO;
    private static final String LOG_CATEGORY = ConditionBuilderExpRightPanel.class.getName();
    private boolean dirty;
    private ConditionBuilderRightPanel gPanel;
    private JSplitPane hSplitPane;
    private SQLLogView logView;
    //private SQLOutputConditionView outputView;
    public SQLEditorPanel pane;
    private JSplitPane splitPane;
    private TableTreeView tableTreeView;
    private JTextArea valArea;
    private IGraphViewContainer viewContainer;
    

    static {
        try {
            mDataFlavorArray[0] = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /** Creates a new instance of ConditionBuilderExpRightPane */
    public ConditionBuilderExpRightPanel() {
        initGui(new ArrayList());
    }

    public ConditionBuilderExpRightPanel(String condition, IGraphViewContainer viewC, List tables, int toolBarType) {
        initGui(tables);
        if (condition != null) {
            pane.setText(condition);
        }

        this.viewContainer = viewC;

        ConditionBuilderTextViewFactory viewFactory = new ConditionBuilderTextViewFactory(viewC, toolBarType);

        BasicToolBar tlBar = (BasicToolBar) viewFactory.getOperatorView();
        this.add(tlBar, BorderLayout.NORTH);

    }

    /**
     * Is editable.
     * 
     * @return boolean - true/false
     */
    public boolean canEdit() {
        return viewContainer.canEdit();
    }

    /**
     * Validates the collaboration.
     */
    public void doValidation() {
        String condition = getCondition();
        logView.clearView();

        if (condition != null && condition.trim().equals("")) {
            logView.refreshView("Condition is not defined.");
            showSplitPaneView(logView);
            return;
        }

        try {
            List errorList = new ArrayList();
            SQLValidationVisitor visitor = new SQLValidationVisitor();

            SQLObject obj = getConditionRootPredicate();
            if (obj instanceof SQLPredicate) {
                SQLPredicate predicate = (SQLPredicate) obj;
                visitor.visit(predicate);

                errorList.addAll(ConditionBuilderUtil.filterValidations(visitor.getValidationInfoList()));
                showSplitPaneView(logView);
            } else {
                String nbBundle1 = mLoc.t("BUND378: Condition expression does not contain a valid predicate.");
                String error = nbBundle1.substring(15);
                ValidationInfo info = SQLModelObjectFactory.getInstance().createValidationInfo(null, error, ValidationInfo.VALIDATION_ERROR);
                errorList.add(info);

                // If root object is an instance of SQLConnectableObject, visit it to
                // validate for errors and warnings.
                if (obj instanceof SQLConnectableObject) {
                    ((SQLConnectableObject) obj).visit(visitor);
                    errorList.addAll(visitor.getValidationInfoList());
                }
            }

            if (errorList.size() == 0) {
                String nbBundle2 = mLoc.t("BUND379: Condition is valid.");
                logView.refreshView(nbBundle2.substring(15));
            } else {
                if (visitor.hasErrors(errorList)) {
                    String nbBundle3 = mLoc.t("BUND302: Condition is not valid.");
                    logView.refreshView(nbBundle3.substring(15));
                } else {
                    String nbBundle4 = mLoc.t("BUND381: Condition is valid, but has warnings.");
                    logView.refreshView(nbBundle4.substring(15));
                }
                logView.appendToView("\n");

                Iterator iter = errorList.iterator();
                while (iter.hasNext()) {
                    ValidationInfo info = (ValidationInfo) iter.next();
                    logView.appendToView(info.getDescription());
                    logView.appendToView("\n");
                }
            }
            showSplitPaneView(logView);
        } catch (Exception ex) {
            mLogger.errorNoloc(mLoc.t("EDIT173: Error({0})", LOG_CATEGORY), ex);

            if (ex instanceof ParseException) {
                logView.refreshView("Condition is invalid - cannot parse text.");
            } else {
                String nbBundle5 = mLoc.t("BUND302: Condition is not valid.");
                logView.refreshView(nbBundle5.substring(15));
                logView.appendToView("\n");
                if (ex.getMessage() != null) {
                    logView.appendToView(ex.getMessage());
                }
            }
            showSplitPaneView(logView);
        }
    }

    public String getCondition() {
        return pane.getText();
    }

    public SQLObject getConditionRootPredicate() throws Exception {
        SQLObject obj = null;
        IGraphView view = viewContainer.getGraphView();
        if (view != null) {
            CollabSQLUIModel model = (CollabSQLUIModel) view.getGraphModel();
            if (model != null) {
                obj = ConditionUtil.parseCondition(getCondition(), model.getSQLDefinition());
                // Check if it is not a expression object if not we should return null
                // since we expect a valid expression object. It doesn't have to be a
                // SQLPredicate since user could be in the middle of building a predicate
                // expression using sql operators. Calling classes should test whether
                // the returned SQLObject is a SQLPredicate before assuming that the
                // expression is valid.
                if (!(obj instanceof SQLConnectableObject)) {
                    obj = null;
                }
            }
        }

        return obj;
    }

    public SQLDefinition getSQLDefinition() throws Exception {
        IGraphView view = viewContainer.getGraphView();
        if (view != null) {
            CollabSQLUIModel model = (CollabSQLUIModel) view.getGraphModel();
            if (model != null) {
                return model.getSQLDefinition();
            }
        }

        return null;
    }

    /**
     * highlight invalid objects
     */
    public void highlightInvalidNode(List list, IMessageView c) {
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setCondition(String text) {
        pane.setText(text);
    }

    public void setConditionRightGraphView(ConditionBuilderRightPanel panel) {
        this.gPanel = panel;
    }

    public void setDirty(boolean d) {
        this.dirty = d;
    }

    public void setModifiable(boolean edit) {
        pane.setEditable(edit);
        if (!edit) {
            pane.setBackground(new Color(0xDD, 0xDD, 0xDD));
        } else {
            pane.setBackground(Color.white);
        }

        Runnable layout = new Runnable() {

            public void run() {
                pane.repaint();
            }
        };

        SwingUtilities.invokeLater(layout);

        GraphAction action = GraphAction.getAction(ValidateSQLAction.class);
        if (action != null) {
            action.setEnabled(edit);
        }
    }

    /**
     * Shows output view in bottom portion of a split pane.
     * 
     * @param c - component
     */
    public void showSplitPaneView(Component c) {
        splitPane.setBottomComponent(c);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(PREFERRED_DIVIDER_RATIO);
    //outputView.addComponent(c);
    }

    /**
     * Shows the condition SQL
     */
    public void showSql() {
    }

    public void showTableTree() {
        if (hSplitPane.getLeftComponent() == null) {
            hSplitPane.setLeftComponent(tableTreeView);
            hSplitPane.setOneTouchExpandable(true);
        } else {
            hSplitPane.setOneTouchExpandable(false);
            hSplitPane.setLeftComponent(null);
        }
    }

    private void initGui(List tables) {
        this.setLayout(new BorderLayout());

        // Create table tree view
        tableTreeView = new TableTreeView(tables);
        tableTreeView.setMinimumSize(new Dimension(200, 100));

        // Create a horizontal split pane which has left and right side
        // Left side holds tree tabbed view
        hSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        hSplitPane.setOneTouchExpandable(true);
        hSplitPane.setDividerLocation(200);

        // Set the tree tabbed pane as left component
        hSplitPane.setLeftComponent(tableTreeView);

        pane = new SQLEditorPanel();
        pane.setTables(tables);

        final JComponent c = new JScrollPane(pane);

        Document doc = pane.getDocument();
        // Listen for when document is changed
        doc.addDocumentListener(new ConditionDocumentListener());

        DropTarget dTarget = new DropTarget(pane, DnDConstants.ACTION_COPY_OR_MOVE, new TextDropTargetListener());
        pane.setDropTarget(dTarget);

        JPanel validationPanel = new JPanel();
        validationPanel.setLayout(new BorderLayout());

        JScrollPane valSPane = new JScrollPane();
        validationPanel.add(valSPane, BorderLayout.CENTER);

        valArea = new JTextArea();
        valSPane.setViewportView(valArea);

        String nbBundle60 = mLoc.t("BUND039: Validate");
        JButton validationButton = new JButton(nbBundle60.substring(15));
        validationButton.getAccessibleContext().setAccessibleName(nbBundle60.substring(15));
        validationButton.addActionListener(new ValidationListener());
        validationPanel.add(validationButton, BorderLayout.EAST);
        splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(SPLITPANE_RESIZE_WEIGHT);
        //outputView = new SQLOutputConditionView(this);
        logView = new SQLLogView();
        String nbBundle61 = mLoc.t("BUND383: Validation");
        logView.setName(nbBundle61.substring(15));
        logView.getAccessibleContext().setAccessibleName(nbBundle61.substring(15));
        splitPane.setTopComponent(c);
        hSplitPane.setRightComponent(splitPane);
        this.add(hSplitPane, BorderLayout.CENTER);
    }
}

