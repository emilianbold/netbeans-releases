/*
 * JoinPreviewPanel.java
 *
 * Created on January 13, 2004, 11:38 AM
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
import com.sun.sql.framework.utils.Logger;

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
            ArrayList joinTables = new ArrayList();

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
            Logger.printThrowable(Logger.ERROR, LOG_CATEGORY, "refresh", "Error caught while refreshing preview", ex);
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
            ArrayList joinTables = new ArrayList(jView.getSQLJoinTables());

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
            Logger.printThrowable(Logger.ERROR, LOG_CATEGORY, "refresh", "Error caught while refreshing preview", ex);
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
            ArrayList joinTables = new ArrayList(jView.getSQLJoinTables());

            ((JoinPreviewGraphFactory) graphView.getGraphFactory()).setJoinSources(null);
            ((JoinPreviewGraphFactory) graphView.getGraphFactory()).setJoinSources(joinTables);

            // now call restore ui state to show the model
            model.restoreUIState();
        } catch (BaseException ex) {
            Logger.printThrowable(Logger.ERROR, LOG_CATEGORY, "removeTable", "Error caught while removing Table in preview", ex);
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
            ArrayList joinTables = new ArrayList(jView.getSQLJoinTables());
            ((JoinPreviewGraphFactory) graphView.getGraphFactory()).setJoinSources(null);
            ((JoinPreviewGraphFactory) graphView.getGraphFactory()).setJoinSources(joinTables);

            // now call restore ui state to show the model
            model.restoreUIState();
        } catch (BaseException ex) {
            Logger.printThrowable(Logger.ERROR, LOG_CATEGORY, "createJoin",
                "Error caught while creating a join again to relink all tables in preview", ex);
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
                Object response = DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Confirmation("User modified condition in some joins will be lost, Do you want to continue?",
                        NotifyDescriptor.WARNING_MESSAGE));

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
        ((JoinPreviewGraphFactory) graphView.getGraphFactory()).setJoinSources(new ArrayList(joinView.getSQLJoinTables()));

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
            Logger.printThrowable(Logger.ERROR, JoinPreviewPanel.class.getName(), "setSQLJoinView", "error in doing reload", ex);
            NotifyDescriptor d = new NotifyDescriptor.Message(ex.toString(), NotifyDescriptor.WARNING_MESSAGE);
            DialogDisplayer.getDefault().notify(d);
        }
    }

    public void setModifiable(boolean b) {
        this.graphView.setModifiable(b);
    }

}

