package org.netbeans.modules.sql.framework.ui.view.graph;

import java.util.ArrayList;
import java.util.List;

import javax.swing.undo.UndoManager;

import org.netbeans.modules.sql.framework.ui.graph.IGraphController;
import org.netbeans.modules.sql.framework.ui.graph.IGraphView;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfoModel;
import org.netbeans.modules.sql.framework.ui.graph.IToolBar;
import org.netbeans.modules.sql.framework.ui.graph.actions.AutoLayoutAction;
import org.netbeans.modules.sql.framework.ui.graph.actions.CollapseAllAction;
import org.netbeans.modules.sql.framework.ui.graph.actions.ExpandAllAction;
import org.netbeans.modules.sql.framework.ui.graph.actions.GraphAction;
import org.netbeans.modules.sql.framework.ui.graph.actions.ZoomAction;
import org.netbeans.modules.sql.framework.ui.graph.impl.OperatorXmlInfoModel;
import org.netbeans.modules.sql.framework.ui.graph.view.impl.SQLToolBar;
import org.netbeans.modules.sql.framework.ui.model.SQLUIModel;
import org.netbeans.modules.sql.framework.ui.undo.SQLUndoManager;
import org.netbeans.modules.sql.framework.ui.view.IGraphViewContainer;

public final class BasicSQLViewFactory extends AbstractSQLViewFactory {
    private IOperatorXmlInfoModel operatorModel;
    private SQLUIModel sqlModel;
    private IGraphViewContainer gViewContainer;
    private IGraphView gView;
    private IToolBar toolBar;
    private List graphActions;
    private List toolBarActions;

    public BasicSQLViewFactory(SQLUIModel model, IGraphViewContainer gContainer, List gActions, List tActions) {
        this.sqlModel = model;
        this.gViewContainer = gContainer;
        this.graphActions = gActions;
        this.toolBarActions = tActions;

        // This has to be created only once
        operatorModel = OperatorXmlInfoModel.getInstance(this.gViewContainer.getOperatorFolder());
        this.toolbarType = IOperatorXmlInfoModel.CATEGORY_TRANSFORM;
        super.setUp();
    }

    /**
     * create a graph view
     * 
     * @return graph view
     */
    public IGraphView createGraphView() {
        SQLGraphView gv = new SQLGraphView();
        this.gView = gv;
        BasicGraphFactory graphFactory = new BasicGraphFactory(this.gViewContainer.getOperatorFolder());
        gv.setGraphFactory(graphFactory);
        //set up model
        sqlModel.addSQLDataListener(gv);
        
        UndoManager undoManager = sqlModel.getUndoManager();
        if (undoManager instanceof SQLUndoManager) {
            ((SQLUndoManager) undoManager).addUndoableEditListener(gv);
        }

        return gv;
    }

    /**
     * create a tool bar
     * 
     * @return tool bar
     */
    // XXX: Already created in Editor Multiview Element 
    public IToolBar createToolBar() {
        if(toolBar == null){
            toolBar = new SQLToolBar(this);
        }
        return toolBar;
    }

    /**
     * create a graph controller
     * 
     * @return
     */
    public IGraphController createGraphController() {
        IGraphController graphController = new BasicSQLGraphController();
        return graphController;
    }

    /**
     * get sql model
     * 
     * @return sql model
     */
    public SQLUIModel getSQLModel() {
        return this.sqlModel;
    }

    /**
     * get graph view container
     * 
     * @return graph view container
     */
    public Object getGraphViewContainer() {
        return this.gViewContainer;
    }

    /**
     * get graph view pop up actions
     * 
     * @return actions
     */
    public List getGraphActions() {
        if (graphActions != null) {
            return graphActions;
        }
        ArrayList actions = new ArrayList();
        //While right clicking
        /*actions.add(SystemAction.get(UndoAction.class));
        actions.add(SystemAction.get(RedoAction.class));*/
        //actions.add(GraphAction.getAction(PrintAction.class));
        return actions;
    }

    /**
     * return toolbar actions
     * 
     * @return toolbar actions
     */
    public List getToolBarActions() {
        if (toolBarActions != null) {
            return toolBarActions;
        }

        ArrayList actions = new ArrayList();
       
        //Commented the snippet for undo,redo fix
        
         //undo action are not static (because they are used in condition builder also)
        // so we create it using constructor
        /* UndoAction undoAction = new UndoAction();
        RedoAction redoAction = new RedoAction();

        //undo redo action should not about each other so they can enable/disable each
        // other
        undoAction.setRedoAction(redoAction);
        redoAction.setUndoAction(undoAction);
        actions.add(undoAction);
        actions.add(redoAction);
        actions.add(null);*/         
        actions.add(GraphAction.getAction(ExpandAllAction.class));
        actions.add(GraphAction.getAction(CollapseAllAction.class));
        actions.add(GraphAction.getAction(AutoLayoutAction.class));
        //actions.add(GraphAction.getAction(PrintAction.class));
        actions.add(GraphAction.getAction(ZoomAction.class));

        return actions;
    }

    /**
     * get operator xml info model which is defined in netbeans layer.xml file
     * 
     * @return operator xml info model
     */
    public IOperatorXmlInfoModel getOperatorXmlInfoModel() {
        return operatorModel;
    }

    /**
     * Gets operator view (toolbar) currently associated with this manager.
     * 
     * @return current instance of IToolBar
     */
    public IToolBar getOperatorView() {
        return toolBar;
    }

    /**
     * Gets graph view currently associated with this manager.
     * 
     * @return current instance of IGraphView
     */
    public IGraphView getGraphView() {
        return gView;
    }
}

