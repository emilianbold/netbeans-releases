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
import java.awt.Component;
import java.awt.Dimension;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.netbeans.modules.sql.framework.model.SQLCondition;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SQLPredicate;
import org.netbeans.modules.sql.framework.model.impl.SQLCustomOperatorImpl;
import org.netbeans.modules.sql.framework.model.visitors.SQLValidationVisitor;
import org.netbeans.modules.sql.framework.ui.view.IGraphViewContainer;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.explorer.propertysheet.editors.EnhancedCustomPropertyEditor;
import net.java.hulp.i18n.Logger;
import com.sun.sql.framework.exception.BaseException;
import org.netbeans.modules.etl.logger.Localizer;


/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class ConditionBuilderView extends JPanel implements EnhancedCustomPropertyEditor {

    private static transient final Logger mLogger = Logger.getLogger(ConditionBuilderView.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    private class TabChangeAdapter implements ChangeListener {

        private boolean trySync = true;

        /**
         * Invoked when the target of the listener has changed its state.
         * 
         * @param e a ChangeEvent object
         */
        public void stateChanged(ChangeEvent e) {
            if (reLoad) {
                return;
            }

            Component selComp = rightPanel.getSelectedComponent();
            String nbBundle1 = mLoc.t("BUND387: Failed to synchronize graph with SQL code - check if condition is valid in text mode.");
            if (selComp instanceof ConditionBuilderRightPanel) {
                if (trySync && !ConditionBuilderView.this.synchronizeGraphView()) {
                    DialogDisplayer.getDefault().notify(
                            new NotifyDescriptor.Message(nbBundle1.substring(15),
                            NotifyDescriptor.INFORMATION_MESSAGE));
                    trySync = false;
                    rightPanel.setSelectedComponent(rightTextPanel);
                    trySync = true;
                    return;
                }

                ConditionBuilderRightPanel rightGraphPanel1 = (ConditionBuilderRightPanel) selComp;
                rightGraphPanel1.setModifiable(!rightTextPanel.isDirty());
                ConditionBuilderView.this.condContainerObj.setGuiMode(SQLCondition.GUIMODE_GRAPHICAL);
            } else if (selComp instanceof ConditionBuilderExpRightPanel) {
                String nbBundle2 = mLoc.t("BUND388: Failed to synchronize SQL code with graphical mode - check if condition is valid in graphical mode.");
                if (trySync && !ConditionBuilderView.this.synchronizeSQLCodeView()) {
                    DialogDisplayer.getDefault().notify(
                            new NotifyDescriptor.Message(
                           nbBundle2.substring(15),
                            NotifyDescriptor.INFORMATION_MESSAGE));
                    trySync = false;
                    rightPanel.setSelectedComponent(rightGraphPanel);
                    trySync = true;
                    return;
                }

                ConditionBuilderExpRightPanel rightExpPanel = (ConditionBuilderExpRightPanel) selComp;
                rightExpPanel.setModifiable(!rightGraphPanel.isDirty());
                ConditionBuilderView.this.condContainerObj.setGuiMode(SQLCondition.GUIMODE_SQLCODE);
            }
        }
    }
    private static final String LOG_CATEGORY = ConditionBuilderView.class.getName();
    private SQLCondition condContainerObj;
    private IGraphViewContainer editor;
    private boolean reLoad = false;
    private ConditionBuilderRightPanel rightGraphPanel;
    private JTabbedPane rightPanel = new JTabbedPane();
    private ConditionBuilderExpRightPanel rightTextPanel;
    private boolean showError = true;

    /**
     * Creates a new instance of ConditionBuilderView
     */
    public ConditionBuilderView(IGraphViewContainer editor, List tables, SQLCondition cond, int toolbarType) {
        this(cond);
        this.editor = editor;
        initGui(tables, toolbarType);
    }

    private ConditionBuilderView() {
        ConditionViewManager.getDefault().setCurrentConditionBuilderView(this);
    }

    /** Creates a new instance of ConditionBuilderView */
    private ConditionBuilderView(SQLCondition cond) {
        this();
        if (cond == null) {
            throw new IllegalArgumentException("Must supply non null SQLCondition.");
        }
        try {
            condContainerObj = (SQLCondition) cond.cloneSQLObject();
        } catch (CloneNotSupportedException ex) {
            mLogger.errorNoloc(mLoc.t("EDIT514: error cloning the condition{0}", LOG_CATEGORY), ex);
            return;
        }
    }

    public void doGraphValidation() {
        rightGraphPanel.doValidation();
    }

    public void doSQLCodeValidation() {
        rightTextPanel.doValidation();
    }

    public void doValidation() {
        if (condContainerObj.getGuiMode() == SQLCondition.GUIMODE_GRAPHICAL) {
            this.doGraphValidation();
        } else {
            this.doSQLCodeValidation();
        }
    }

    /**
     * Get the customized property value.
     * 
     * @return the property value
     * @exception IllegalStateException when the custom property editor does not contain a
     *            valid property value (and thus it should not be set)
     */
    public Object getPropertyValue() throws IllegalStateException {
        // When user exit find out the currently selected mode
        // then try to synchronize other mode
        boolean result = setGuiMode();
        String nbBundle3 = mLoc.t("BUND389: The condition is not valid. Make sure you correct it.");
        if (showError && !result) {
            DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Message(nbBundle3.substring(15),
                    NotifyDescriptor.WARNING_MESSAGE));
        }

        return condContainerObj;
    }

    public SQLCondition getSQLCondition() {
        return this.condContainerObj;
    }

    public boolean isConditionValid() {
        return (this.rightTextPanel.isDirty()) ? synchronizeGraphView() : synchronizeSQLCodeView();
    }

    public boolean isDirty() {
        return this.rightTextPanel.isDirty() || this.rightGraphPanel.isDirty();
    }

    public boolean isShowErrorMsg() {
        return this.showError;
    }

    public void setShowErrorMsg(boolean show) {
        this.showError = show;
    }

    public void showTableTree() {
        Component c = this.rightPanel.getSelectedComponent();
        if (c instanceof ConditionBuilderExpRightPanel) {
            rightTextPanel.showTableTree();
        }
        if (c instanceof ConditionBuilderRightPanel) {
            rightGraphPanel.showTableTree();
        }
    }

    public boolean synchronizeGraphView() {
        String cond = rightTextPanel.getCondition();
        boolean customOperator = false;

        SQLObject obj = null;
        try {
            obj = rightTextPanel.getConditionRootPredicate();

        } catch (Exception ex) {
            if (ex instanceof BaseException) {
                if (((BaseException) ex).getErrorCode() == BaseException.OPERATOR_NOT_DEFINED) {
                    customOperator = true;
                }
            } else {
                // Ignore this safely
                mLogger.errorNoloc(mLoc.t("EDIT515: Exception occurred while parsing condition{0}", LOG_CATEGORY), ex);
            }
        }

        // For custom operator, disable any further validation 
        // as it can be properly validated only at query time using
        // test colloboration
        if (customOperator) {
            rightGraphPanel.setModifiable(true);
            rightGraphPanel.setDirty(false);
            rightTextPanel.setDirty(false);
            return true;
        }
        boolean graphChanged = false;
        // First check if user has removed the condition
        if (cond != null && cond.trim().equals("")) {
            condContainerObj.setConditionText("");
            rightGraphPanel.clearView();
            graphChanged = true;
        // Then check if there is a valid root predicate
        } else if (obj instanceof SQLPredicate) {
            SQLValidationVisitor visitor = new SQLValidationVisitor();
            SQLPredicate predicate = (SQLPredicate) obj;
            predicate.visit(visitor);

            // Proceed only if predicate is valid.
            if (!visitor.hasErrors(ConditionBuilderUtil.filterValidations(visitor.getValidationInfoList()))) {
                rightGraphPanel.getModel().clearJavaOperators();
                rightGraphPanel.refresh(obj);
                graphChanged = true;
            }
        }

        if (graphChanged) {
            rightGraphPanel.setModifiable(true);
            rightGraphPanel.setDirty(false);
            rightTextPanel.setDirty(false);
            return true;
        }

        return false;
    }

    public boolean synchronizeSQLCodeView() {
        SQLObject obj = null;
        boolean synchronizedSqlText = true;
        boolean textChanged = false;
        obj = rightGraphPanel.getConditionRootPredicate();

        // First check if there is any graph object available
        // if not then make text empty
        Collection objC = condContainerObj.getAllObjects();
        if ((objC == null || objC.size() == 0) && (obj == null)) {
            rightTextPanel.setCondition("");
            condContainerObj.setConditionText("");
            textChanged = true;
        } else if (obj != null) {
            rightTextPanel.setCondition(obj.toString());
            condContainerObj.setConditionText(rightTextPanel.getCondition());
            textChanged = true;
        } else if ((objC != null) && (objC.size() > 0)) {
            // There are objects but failed to get Predicate.
            Iterator iter = objC.iterator();
            boolean customOperatorCheck = false;
            while (iter.hasNext()) {
                Object localObj = iter.next();
                if (localObj instanceof SQLCustomOperatorImpl) {
                    customOperatorCheck = true;
                }
            }
            //disabling custom operator validation as 
            //custom operator validation is done only at Test colloboration level
            if (!customOperatorCheck) {
                synchronizedSqlText = false;
            }
        }

        if (textChanged) {
            rightTextPanel.setModifiable(true);
            rightTextPanel.setDirty(false);
            rightGraphPanel.setDirty(false);
        }

        return synchronizedSqlText;
    }

    private void initGui(List tables, int toolbarType) {
        this.setLayout(new BorderLayout());

        // Create ConditionBuilderRightPanel
        rightTextPanel = new ConditionBuilderExpRightPanel(condContainerObj.getConditionText(), editor, tables, toolbarType);
        rightTextPanel.setMinimumSize(new Dimension(150, 400));
        rightPanel.add(rightTextPanel, "SQL Code");

        rightGraphPanel = new ConditionBuilderRightPanel(editor, condContainerObj, tables, toolbarType);
        rightGraphPanel.setMinimumSize(new Dimension(150, 400));
        rightPanel.add(rightGraphPanel, "Graphical");

        // Add listener after adding all the tabs
        rightPanel.addChangeListener(new TabChangeAdapter());

        rightTextPanel.setConditionRightGraphView(rightGraphPanel);

        // set both panel to not dirty initially
        rightGraphPanel.setDirty(false);
        rightTextPanel.setDirty(false);

        this.add(BorderLayout.CENTER, rightPanel);

        this.setPreferredSize(new Dimension(800, 500));

        // Now set the default mode which was used last time
        setGuiState();
    }

    private boolean setGuiMode() {
        boolean result = true;
        if (this.rightTextPanel.isDirty() || condContainerObj.getGuiMode() == SQLCondition.GUIMODE_SQLCODE) {
            // If user modifies text view the always try to sync graph view with text view
            // before exiting from condition builder
            result = synchronizeGraphView();
            if (!result) {
                rightGraphPanel.clearView();
            }
            condContainerObj.setGuiMode(SQLCondition.GUIMODE_SQLCODE);
            condContainerObj.setConditionText(rightTextPanel.getCondition());
        } else {
            condContainerObj.setGuiMode(SQLCondition.GUIMODE_GRAPHICAL);

            // Check if text does not have a valid condition if so then
            // try to set condition from graph to text
            result = synchronizeSQLCodeView();
            if (!result) {
                rightTextPanel.setCondition("");
                condContainerObj.setConditionText("");
            }
        }

        return result;
    }

    private void setGuiState() {
        // We are in reload mode so set flag to true
        reLoad = true;
        int mode = condContainerObj.getGuiMode();

        switch (mode) {
            case SQLCondition.GUIMODE_SQLCODE:
                rightTextPanel.setDirty(true);
                rightPanel.setSelectedComponent(rightTextPanel);
                rightGraphPanel.setModifiable(false);
                break;
            case SQLCondition.GUIMODE_GRAPHICAL:
                rightGraphPanel.setDirty(true);
                rightPanel.setSelectedComponent(rightGraphPanel);
                rightTextPanel.setModifiable(false);
                break;

        }

        reLoad = false;
    }
}
