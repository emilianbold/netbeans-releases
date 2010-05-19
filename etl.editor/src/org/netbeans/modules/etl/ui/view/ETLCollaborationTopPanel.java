/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.etl.ui.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.event.KeyEvent;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import org.netbeans.modules.etl.model.impl.ETLDefinitionImpl;
import org.netbeans.modules.etl.ui.DataObjectProvider;
import org.netbeans.modules.etl.ui.ETLDataObject;
import org.netbeans.modules.etl.ui.model.impl.ETLCollaborationModel;
import org.netbeans.modules.etl.ui.view.cookies.ExecuteTestCookie;
import org.netbeans.modules.etl.ui.view.cookies.SelectTablesCookie;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.ui.SwingWorker;
import org.netbeans.modules.sql.framework.ui.editor.property.IPropertySheet;
import org.netbeans.modules.sql.framework.ui.graph.IGraphNode;
import org.netbeans.modules.sql.framework.ui.graph.IGraphView;
import org.netbeans.modules.sql.framework.ui.graph.actions.PrintAction;
import org.netbeans.modules.sql.framework.ui.graph.actions.RedoAction;
import org.netbeans.modules.sql.framework.ui.graph.actions.RunAction;
import org.netbeans.modules.sql.framework.ui.graph.actions.UndoAction;
import org.netbeans.modules.sql.framework.ui.output.SQLLogView;
import org.netbeans.modules.sql.framework.ui.view.validation.SQLValidationView;
import org.netbeans.modules.sql.framework.ui.zoom.ZoomSupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.actions.SaveAction;
import org.openide.nodes.Node;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.actions.SystemAction;
import com.sun.etl.exception.BaseException;
import net.java.hulp.i18n.Logger;
import java.io.Externalizable;
import javax.swing.JToolBar;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.sql.framework.ui.graph.view.impl.SQLToolBar;
import org.netbeans.modules.sql.framework.ui.view.graph.SQLCollaborationView;
import org.openide.awt.StatusDisplayer;

/**
 * An openable window available to the IDE's window manager.
 *
 * @author Ritesh Adval
 * @version $Revision$
 */
public class ETLCollaborationTopPanel extends JPanel implements ZoomSupport, Externalizable {

    private static transient final Logger mLogger = Logger.getLogger(ETLCollaborationTopPanel.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    class ValidationThread extends SwingWorker {

        private SQLDefinition execModel;
        private List list;

        public ValidationThread(SQLDefinition execModel) {
            this.execModel = execModel;
        }

        /**
         * Compute the value to be returned by the <code>get</code> method.
         *
         * @return object
         */
        public Object construct() {
            list = execModel.validate();

            return "";
        }

        //Runs on the event-dispatching thread.
        @Override
        public void finished() {
            if (execModel.getAllObjects().size() == 0) {
                String nbBundle1 = mLoc.t("BUND158: \nNo items to validate in this collaboration.");
                String msg = nbBundle1.substring(15);
                StatusDisplayer.getDefault().setStatusText("\n" + msg);
            } else if (execModel.getTargetTables().size() == 0) {
                String nbBundle2 = mLoc.t("BUND159: \nNo target table defined.");
                String msg = nbBundle2.substring(15);
                StatusDisplayer.getDefault().setStatusText("\n" + msg);
            } else if (list.size() == 0) {
                StatusDisplayer.getDefault().setStatusText("\nCollaboration is valid.");
            }

            if (list.size() > 0) {
                validationView.setValidationInfos(list);
                showSplitPaneView(validationView);
            }
        }
    }
    /**
     * Constant representing default name of operator folder from which operator
     * definitions are retrieved.
     */
    public static final String DEFAULT_OPERATOR_FOLDER = "ETLOperators";
    private static final String GRAPHPANEL_NAME = "Graph Panel";
    private CardLayout cLayout;
    private EditDBModelPanel editPanel;
    private static ETLDataObject dObj;
    private ETLEditorTopView etlTopView;
    private SQLLogView logView;
    private SQLValidationView validationView;
    private JToolBar toolBar;

    // REMEMBER: You should have a public default constructor!
    // This is for externalization. If you have a non-default
    // constructor for normal creation of the component, leave
    // in a default constructor that will put the component into
    // a consistent but unconfigured state, and make sure readExternal
    // initializes it properly. Or, be creative with writeReplace().
    /**
     * Constructs a new default instance of ETLCollaborationTopPanel.
     */
    public ETLCollaborationTopPanel() {
        initComponents();

        //do not show tab view if there is only one tab
        putClientProperty("TabPolicy", "HideWhenAlone"); //NOI18N

        putClientProperty("PersistenceType", "Never"); //NOI18N

        this.setFont(new Font("Dialog", Font.PLAIN, 12)); //NOI18N

        registerActions();
    // Use the Component Inspector to set tool-tip text. This will be saved
    // automatically. Other JComponent properties you may need to save yuorself.
    // At any time you can affect the node selection:
    // setActivatedNodes(new Node[] { ... } );
    }

    /**
     * Constructs new instance of ETLCollaborationTopPanel, using the given
     * data object to populate its contents.
     *
     * @param eTL data object to be rendered
     * @throws Exception if error occurs during instantiation
     */
    public ETLCollaborationTopPanel(ETLDataObject mObj) throws Exception {
        this();
        dObj = mObj;
        //DataObjectProvider provider = DataObjectProvider.getProvider();
        String collaborationName = dObj.getName();

        cLayout = new CardLayout();
        this.setLayout(cLayout);
        ETLCollaborationModel collabModel = dObj.getModel();
        etlTopView = new ETLEditorTopView(collabModel, this);
        etlTopView.setName(collaborationName);
        this.add(etlTopView, GRAPHPANEL_NAME);

        cLayout.first(this);

        String nbBundle3 = mLoc.t("BUND383: Validation");
        validationView = new SQLValidationView(this.getGraphView());
        String validationLabel = nbBundle3.substring(15);
        validationView.setName(validationLabel);

        logView = new SQLLogView();
        String nbBundle4 = mLoc.t("BUND160: Execution Log");
        String logLabel = nbBundle4.substring(15);
        logView.setName(logLabel);
    }

    /**
     * Adds input table
     *
     * @param type - type
     */
    public void addRuntime(int type) {
        TablePanel tPanel = new TablePanel(type);
        tPanel.showTablePanel();
        if (dObj.getModel().isDirty()) {
            dObj.getETLEditorSupport().synchDocument();
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    // Printing, saving, compiling, etc.: use cookies on some appropriate node and
    // use this node as the node selection.

    /**
     * Is editable
     *
     * @return boolean - true/false
     */
    public boolean canEdit() {
        if (isEditable()) {
            return true;
        }

        String nbBundle5 = mLoc.t("BUND161: Please check out {0} before modifying it.", DataObjectProvider.getProvider().getActiveDataObject().getName());
        try {
            String msg = nbBundle5.substring(15);
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(msg, NotifyDescriptor.INFORMATION_MESSAGE));
        } catch (Exception ex) {
            mLogger.errorNoloc(mLoc.t("EDIT044: Can't get name of data object{0}", DataObjectProvider.getProvider().getActiveDataObject()), ex);
        }

        return false;
    }

    /**
     * Validates the collaboration.
     */
    public void doValidation() {
        try {
            ETLCollaborationModel collabModel = DataObjectProvider.getProvider().getActiveDataObject().getModel();

            if (collabModel != null) {
                validationView.clearView();
                ETLDefinitionImpl def = collabModel.getETLDefinition();
                ValidationThread vThread = new ValidationThread(def.getSQLDefinition());
                vThread.start();
            }
        } catch (Exception ex) {
            mLogger.errorNoloc(mLoc.t("EDIT045: \nError occurred during validation:{0}", ex.getMessage()), ex);
            String nbBundle6 = mLoc.t("BUND162: \nError occurred during validation: {0}", ex.getMessage());
            validationView.appendToView(nbBundle6.substring(15));
        }
    }

    /**
     * Displays dialog box to edit database properties.
     */
    public void editDBModel() {
        String nbBundle7 = mLoc.t("BUND163: Modify design-time database properties for this session.");
        JLabel panelTitle = new JLabel(nbBundle7.substring(15));
        panelTitle.getAccessibleContext().setAccessibleName(nbBundle7.substring(15));
        panelTitle.setDisplayedMnemonic(nbBundle7.substring(15).charAt(0));
        panelTitle.setFont(panelTitle.getFont().deriveFont(Font.BOLD));
        panelTitle.setFocusable(false);
        panelTitle.setHorizontalAlignment(SwingConstants.LEADING);
        dObj = DataObjectProvider.getProvider().getActiveDataObject();
        editPanel = new EditDBModelPanel(DataObjectProvider.getProvider().getActiveDataObject());

        JPanel contentPane = new JPanel();
        contentPane.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        contentPane.setLayout(new BorderLayout());
        contentPane.add(panelTitle, BorderLayout.NORTH);
        contentPane.add(editPanel, BorderLayout.CENTER);

        String nbBundle8 = mLoc.t("BUND164: Edit Database Properties");
        DialogDescriptor dd = new DialogDescriptor(contentPane, nbBundle8.substring(15));
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
        dlg.getAccessibleContext().setAccessibleDescription("This is the dialog to edit database properties");
        dlg.setSize(new Dimension(600, 450));
        dlg.setVisible(true);
        if (NotifyDescriptor.OK_OPTION.equals(dd.getValue())) {
            DBModelTreeView dbModelTreeView = editPanel.getDBModelTreeView();
            if (dbModelTreeView != null) {
                IPropertySheet propSheet = dbModelTreeView.getPropSheet();
                if (propSheet != null) {
                    propSheet.commitChanges();
                    ETLCollaborationModel collabModel = dObj.getModel();
                    collabModel.setDirty(true);
                    ETLCollaborationTopPanel.dObj.getETLEditorSupport().synchDocument();
                }
            }
        } else {
            ETLCollaborationModel collabModel = dObj.getModel();
            collabModel.setDirty(false);
        }
    }

    /**
     * Gets IGraphNode, if any, associated with the given Object.
     *
     * @param dataObj - data object
     * @return IGraphNode, if any, associated with <code>dataObj</code>
     */
    public IGraphNode findGraphNode(Object dataObj) {
        return etlTopView.findGraphNode(dataObj);
    }

    /**
     * Gets current IGraphView instance.
     *
     * @return current IGraphView instance
     */
    public IGraphView getGraphView() {
        return this.etlTopView.getGraphView();
    }

    public JToolBar createToolbar() {
        if (toolBar == null) {
            IGraphView graphView = getGraphView();
            etlTopView.enableToolBarActions(true);
            SQLCollaborationView collabView = etlTopView.getCollaborationView();
            SQLToolBar sqltoolBar = new SQLToolBar(collabView.getIOperatorManager());
            sqltoolBar.setGraphView(graphView);
            sqltoolBar.setActions(etlTopView.getToolBarActions());
            sqltoolBar.initializeToolBar();
            toolBar = (JToolBar) sqltoolBar;
        }
        return toolBar;
    }

    /**
     * Gets name of operator folder.
     *
     * @return name of operator folder
     */
    public String getOperatorFolder() {
        return DEFAULT_OPERATOR_FOLDER;
    }

    /**
     * Gets the zoom factor for this TopComponent.
     *
     * @return zoom factor
     */
    public double getZoomFactor() {
        return etlTopView.getZoomFactor();
    }

    /**
     * Is editable
     *
     * @return boolean - true/false
     */
    public boolean isEditable() {
        return true;
    }

    public void reload() {
        try {
            ETLCollaborationModel collabModel = DataObjectProvider.getProvider().getActiveDataObject().getModel();
            collabModel.getUndoManager().discardAllEdits();
            // is below required?
            collabModel.setReloaded(true);
            populateCanvas(collabModel);
        } catch (Exception ex) {
            logReloadException(ex);
        } finally {
            resetEditorInEventDispatchThread();
        }
    }

    /**
     * Reset this view
     */
    public void reset() {
        etlTopView.setModifiable(isEditable());
    }

    /**
     * Executes test run of this ETL Collaboration.
     */
    public void run() {
        Node node = DataObjectProvider.getProvider().getActiveDataObject().getNodeDelegate();
        final ExecuteTestCookie testCookie = node.getCookie(ExecuteTestCookie.class);

        if (testCookie != null) {
            Runnable run = new Runnable() {

                public void run() {
                    testCookie.start();
                }
            };

            SwingUtilities.invokeLater(run);
        }
    }

    /**
     * Displays dialog to select source and target tables.
     */
    public void selectTables() {
        Node node = DataObjectProvider.getProvider().getActiveDataObject().getNodeDelegate();
        final SelectTablesCookie selTablesCookie = node.getCookie(SelectTablesCookie.class);

        if (selTablesCookie != null) {
            Runnable run = new Runnable() {

                public void run() {
                    selTablesCookie.showDialog();
                }
            };

            SwingUtilities.invokeLater(run);
        }
    }

    /**
     * Set editable
     *
     * @param edit - true/false
     */
    public void setEditable(boolean edit) {
        //collabView.setEditable(edit);
    }

    public void setModifiable(boolean b) {
        this.etlTopView.setModifiable(b);
    }

    /**
     * Sets the zoom factor
     *
     * @param factor new zoom factor
     */
    public void setZoomFactor(double factor) {
        etlTopView.setZoomFactor(factor);
    }

    /**
     * Show log
     *
     * @return - ETLLogView
     */
    public SQLLogView showLog() {
        logView.clearView();
        showSplitPaneView(logView);
        return logView;
    }

    /**
     * Toggle the output view
     */
    public void toggleOutputView() {
        ETLOutputWindowTopComponent topComp = ETLOutputWindowTopComponent.findInstance();
        if (topComp.isOpened()) {
            topComp.close();
        } else {
            topComp.open();
            topComp.requestVisible();
            topComp.setVisible(true);
        }
    }

    /**
     * Shows output view in bottom portion of a split pane.
     *
     * @param c - component
     */
    public void showSplitPaneView(Component c) {
        etlTopView.showSplitPaneView(c);
    }

    // APPEARANCE
    /**
     * This method is called from within the constructor to initialize the form. WARNING:
     * Do NOT modify this code. The content of this method is always regenerated by the
     * FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents

    private void logReloadException(Exception e) {
        String nbBundle9 = mLoc.t("BUND165: Error encountered while loading eTL collaboration {0}", getName());
        mLogger.errorNoloc(mLoc.t("EDIT046: Error in executing reload {0}", ETLCollaborationTopPanel.class.getName()), e);
        NotifyDescriptor d = new NotifyDescriptor.Message(nbBundle9.substring(15), NotifyDescriptor.WARNING_MESSAGE);
        DialogDisplayer.getDefault().notify(d);
    }

    /**
     * Populates eTL editor canvas using information from given ETLCollaborationModel.
     *
     * @param collabModel
     * @param disableMods
     * @throws BaseException
     */
    private void populateCanvas(ETLCollaborationModel collabModel) throws BaseException {
        setModifiable(false);

        this.getGraphView().clearAll();
        collabModel.restoreUIState();
        setModifiable(true);
    }

    /**
     * Invokes editor reset in AWT event dispatch thread to ensure Swing-related updates
     * are correctly handled.
     */
    private void resetEditorInEventDispatchThread() {
        Runnable resetEditor = new Runnable() {

            public void run() {
                reset();
            }
        };
        SwingUtilities.invokeLater(resetEditor);
    }

    /**
     * @see reload()
     */
    public void refresh() {
        mLogger.infoNoloc(mLoc.t("EDIT047: Refresh called{0}in {1}", new java.util.Date(), ETLCollaborationTopPanel.class.getName()));
        this.reload();
    }

    private void registerActions() {
        InputMap im1 = getInputMap(WHEN_FOCUSED);
        InputMap im2 = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap am = getActionMap();
        CallableSystemAction saveAction = (CallableSystemAction) SystemAction.get(SaveAction.class);

        im1.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), "Save Collaboration"); // NOI18N

        im1.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK), "undo-something"); // NOI18N

        im1.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK), "redo-something"); // NOI18N

        im1.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK), "Print Collaboration"); // NOI18N

        im1.put(KeyStroke.getKeyStroke(KeyEvent.VK_T, KeyEvent.CTRL_DOWN_MASK), "run-something"); // NOI18N

        im2.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.CTRL_DOWN_MASK), "Save Collaboration"); // NOI18N

        im2.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, KeyEvent.CTRL_DOWN_MASK), "undo-something"); // NOI18N

        im2.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, KeyEvent.CTRL_DOWN_MASK), "redo-something"); // NOI18N

        im2.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK), "Print Collaboration"); // NOI18N

        im2.put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, KeyEvent.SHIFT_DOWN_MASK), "Run Collaboration"); // NOI18N

        am.put("Save Collaboration", saveAction); // NOI18N

        am.put("undo-something", new UndoAction());
        am.put("redo-something", new RedoAction());
        am.put("Print Collaboration", new PrintAction());
        am.put("Run Collaboration", new RunAction());
    }

    //For Navigator
    /*public JComponent getSatelliteView() {
    GraphView graphView = (GraphView) getGraphView();
    BirdsEyeView satelliteView = graphView.getSatelliteView();
    return satelliteView;
    }*/
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(dObj);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        dObj = (ETLDataObject) in.readObject();
    }
}
