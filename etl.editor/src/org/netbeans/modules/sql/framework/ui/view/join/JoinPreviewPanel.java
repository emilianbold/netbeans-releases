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
package org.netbeans.modules.sql.framework.ui.view.join;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.SQLJoinOperator;
import org.netbeans.modules.sql.framework.model.SQLJoinTable;
import org.netbeans.modules.sql.framework.model.SQLJoinView;
import org.netbeans.modules.sql.framework.model.SQLModelObjectFactory;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.ui.graph.IGraphView;
import org.netbeans.modules.sql.framework.ui.model.CollabSQLUIModel;
import org.netbeans.modules.sql.framework.ui.model.JoinBuilderSQLUIModel;
import org.netbeans.modules.sql.framework.ui.model.impl.JoinBuilderSQLUIModelImpl;
import org.netbeans.modules.sql.framework.ui.view.IGraphViewContainer;
import org.netbeans.modules.sql.framework.ui.view.graph.SQLGraphView;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import com.sun.sql.framework.exception.BaseException;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;


/**
 * This class provides preview functionality for join defined by user. This view is
 * contained in JoinMainPanel.
 *
 * @author radval
 */
public class JoinPreviewPanel extends JPanel implements IGraphViewContainer {

    private static final String LOG_CATEGORY = JoinPreviewPanel.class.getName();
    private SQLGraphView graphView;
    private JoinBuilderSQLUIModel model;
    // private JoinMainPanel mainPanel;
    private IGraphView mainGraphView;
    private static transient final Logger mLogger = Logger.getLogger(JoinPreviewPanel.class.getName());
    private static transient final Localizer mLoc = Localizer.get();

    /** Creates a new instance of JoinPreviewPanel */
    public JoinPreviewPanel(JoinMainPanel mPanel) {
        // this.mainPanel = mPanel;
        initGUI();
    }

    private void initGUI() {
        this.setLayout(new BorderLayout());
    }

    /**
     * Is editable
     *
     * @return boolean - true/false
     */
    public boolean canEdit() {
        return true;
    }

    /**
     * Execute a command
     *
     * @param command - command
     * @param args - arguments
     */
    public Object[] execute(String command, Object[] args) {
        return null;
    }

    public IGraphView getGraphView() {
        return this.graphView;
    }

    public String getOperatorFolder() {
        return ((IGraphViewContainer) mainGraphView.getGraphViewContainer()).getOperatorFolder();
    }

    public void reset(IGraphView mainView) {
        this.mainGraphView = mainView;

        // first remove all existing objects in model
        if (model != null) {
            model.removeSQLDataListener(this.graphView);
        }

        // remove old graph view
        if (graphView != null) {
            this.remove(graphView);
        }

        SQLDefinition def = null;

        if (mainGraphView != null) {
            def = ((CollabSQLUIModel) mainGraphView.getGraphModel()).getSQLDefinition();
        }

        model = new JoinBuilderSQLUIModelImpl(def);
        JoinPreviewViewFactory viewFactory = new JoinPreviewViewFactory(model, this, mainGraphView);

        graphView = (SQLGraphView) viewFactory.getGraphView();
        graphView.setKeyEnabled(false);
        this.add(graphView, BorderLayout.CENTER);
    }

    /**
     * This method refreshs the preview panel with table and join nodes.
     *
     * @paran joinSources list of table/join from destination transfer list which needs to
     *        be joined.
     */
    public void refresh(List joinSources) {
        try {
            // first remove all existing objects in model
            model.removeAll();
            this.graphView.clearAll();

            // convert tables to joinTables
            ArrayList<SQLJoinTable> joinTables = new ArrayList<SQLJoinTable>();

            Iterator tIt = joinSources.iterator();
            while (tIt.hasNext()) {
                SourceTable sTable = (SourceTable) tIt.next();
                SQLJoinTable jTable = SQLModelObjectFactory.getInstance().createSQLJoinTable(sTable);
                joinTables.add(jTable);
            }

            // reset and set join source which will be used to number table graph node
            ((JoinPreviewGraphFactory) graphView.getGraphFactory()).setJoinSources(null);
            ((JoinPreviewGraphFactory) graphView.getGraphFactory()).setJoinSources(joinTables);

            // create auto join based on the order of tables in destination list
            JoinUtility.handleAutoJoins(joinTables, model);

            // now call restore ui state to show the model
            model.restoreUIState();
        } catch (BaseException ex) {
            mLogger.errorNoloc(mLoc.t("EDIT181: Error caught while refreshing preview{0}", LOG_CATEGORY), ex);
            return;
        }

        // do auto layout
        Runnable layout = new Runnable() {

            public void run() {
                graphView.autoLayout();
            }
        };
        SwingUtilities.invokeLater(layout);
    }

    /**
     * This method refreshs the preview panel with table and join nodes.
     *
     * @paran joinSources list of table/join from destination transfer list which needs to
     *        be joined.
     */
    public void refresh(SourceTable sTable) {
        try {
            this.graphView.clearAll();

            SQLJoinView jView = model.getSQLJoinView();
            // convert tables to joinTables
            ArrayList<SQLJoinTable> joinTables = new ArrayList<SQLJoinTable>(jView.getSQLJoinTables());

            SQLJoinTable jTable = SQLModelObjectFactory.getInstance().createSQLJoinTable(sTable);
            joinTables.add(jTable);

            // reset and set join source which will be used to number table graph node
            ((JoinPreviewGraphFactory) graphView.getGraphFactory()).setJoinSources(null);
            ((JoinPreviewGraphFactory) graphView.getGraphFactory()).setJoinSources(joinTables);

            // create auto join based on the order of tables in destination list
            JoinUtility.handleAutoJoins(jTable, true, model);

            // now call restore ui state to show the model
            model.restoreUIState();
        } catch (BaseException ex) {
            mLogger.errorNoloc(mLoc.t("EDIT181: Error caught while refreshing preview{0}", LOG_CATEGORY), ex);
            return;
        }

        // do auto layout
        Runnable layout = new Runnable() {

            public void run() {
                graphView.autoLayout();
            }
        };

        SwingUtilities.invokeLater(layout);
    }

    public void removeTable(SourceTable sTable) {
        try {
            this.graphView.clearAll();

            SQLJoinView jView = model.getSQLJoinView();
            SQLJoinTable jTable = jView.getJoinTable(sTable);
            if (jTable != null) {
                model.removeObject(jTable);
            }

            // convert tables to joinTables
            ArrayList<SQLJoinTable> joinTables = new ArrayList<SQLJoinTable>(jView.getSQLJoinTables());

            ((JoinPreviewGraphFactory) graphView.getGraphFactory()).setJoinSources(null);
            ((JoinPreviewGraphFactory) graphView.getGraphFactory()).setJoinSources(joinTables);

            // now call restore ui state to show the model
            model.restoreUIState();
        } catch (BaseException ex) {
            mLogger.errorNoloc(mLoc.t("EDIT183: Error caught while removing Table in preview{0}", LOG_CATEGORY), ex);
            return;
        }

        // do auto layout
        Runnable layout = new Runnable() {

            public void run() {
                graphView.autoLayout();
            }
        };

        SwingUtilities.invokeLater(layout);
    }

    public void createJoin(ArrayList remaingTables) {
        try {
            this.graphView.clearAll();
            SQLJoinView jView = model.getSQLJoinView();

            Iterator rit = remaingTables.iterator();
            while (rit.hasNext()) {
                SourceTable sTable = (SourceTable) rit.next();
                SQLJoinTable joinTable = jView.getJoinTable(sTable);
                if (joinTable != null) {
                    JoinUtility.handleAutoJoins(joinTable, false, model);
                }
            }

            // convert tables to joinTables
            ArrayList<SQLJoinTable> joinTables = new ArrayList<SQLJoinTable>(jView.getSQLJoinTables());
            ((JoinPreviewGraphFactory) graphView.getGraphFactory()).setJoinSources(null);
            ((JoinPreviewGraphFactory) graphView.getGraphFactory()).setJoinSources(joinTables);

            // now call restore ui state to show the model
            model.restoreUIState();
        } catch (BaseException ex) {
            mLogger.errorNoloc(mLoc.t("EDIT184: Error caught while creating a join again to relink all tables in preview{0}", LOG_CATEGORY), ex);
            return;
        }

        // do auto layout
        Runnable layout = new Runnable() {

            public void run() {
                graphView.autoLayout();
            }
        };

        SwingUtilities.invokeLater(layout);
    }

    public boolean checkForUserDefinedCondition() {
        SQLJoinView jView = model.getSQLJoinView();
        Collection joins = jView.getObjectsOfType(SQLConstants.JOIN);
        Iterator it = joins.iterator();

        while (it.hasNext()) {
            SQLJoinOperator join = (SQLJoinOperator) it.next();
            if (join.getJoinConditionType() == SQLJoinOperator.USER_DEFINED_CONDITION) {
                Object response = DialogDisplayer.getDefault().notify(new NotifyDescriptor.Confirmation("User modified condition in some joins will be lost, Do you want to continue?", NotifyDescriptor.WARNING_MESSAGE));

                if (response == NotifyDescriptor.CANCEL_OPTION) {
                    return false;
                }
            }
        }
        return true;
    }

    public SQLJoinView getSQLJoinView() {
        return model.getSQLJoinView();
    }

    public void setSQLJoinView(SQLJoinView joinView) {
        // model = new JoinBuilderSQLUIModelImpl(joinView);
        this.graphView.clearAll();
        model.removeAll();
        model.setSQLJoinView(joinView);
        // reset and set join source which will be used to number table graph node
        ((JoinPreviewGraphFactory) graphView.getGraphFactory()).setJoinSources(null);
        ((JoinPreviewGraphFactory) graphView.getGraphFactory()).setJoinSources(new ArrayList<SQLJoinTable>(joinView.getSQLJoinTables()));

        try {
            // now call restore ui state to show the model
            model.restoreUIState();

            // do auto layout
            Runnable layout = new Runnable() {

                public void run() {
                    graphView.autoLayout();
                }
            };

            SwingUtilities.invokeLater(layout);
        } catch (BaseException ex) {
            mLogger.errorNoloc(mLoc.t("EDIT158: error in doing reload{0}", LOG_CATEGORY), ex);
            //Logger.printThrowable(Logger.ERROR, JoinPreviewPanel.class.getName(), "setSQLJoinView", "error in doing reload", ex);
            NotifyDescriptor d = new NotifyDescriptor.Message(ex.toString(), NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }
    }

    public void setModifiable(boolean b) {
        this.graphView.setModifiable(b);
    }
}
