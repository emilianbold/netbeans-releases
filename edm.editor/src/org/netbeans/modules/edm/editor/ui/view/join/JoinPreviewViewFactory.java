/*
 * JoinPreviewViewFactory.java
 *
 * Created on January 16, 2004, 2:12 PM
 */

package org.netbeans.modules.edm.editor.ui.view.join;

import java.util.List;
import org.netbeans.modules.edm.editor.graph.jgo.IGraphController;
import org.netbeans.modules.edm.editor.graph.jgo.IGraphView;
import org.netbeans.modules.edm.editor.graph.jgo.IOperatorXmlInfoModel;
import org.netbeans.modules.edm.editor.graph.jgo.IToolBar;
import org.netbeans.modules.edm.editor.ui.model.SQLUIModel;
import org.netbeans.modules.edm.editor.ui.view.IGraphViewContainer;
import org.netbeans.modules.edm.editor.graph.AbstractSQLViewFactory;
import org.netbeans.modules.edm.editor.graph.SQLGraphView;


/**
 * @author Ritesh Adval
 */
public class JoinPreviewViewFactory extends AbstractSQLViewFactory {

    protected SQLUIModel sqlModel;
    protected IGraphViewContainer gViewContainer;
    protected IGraphView gView;
    protected IToolBar toolBar;
    private IGraphView mainSQLGraphView;

    public JoinPreviewViewFactory(SQLUIModel model, IGraphViewContainer gContainer, IGraphView mainSQLGraphView) {
        this.sqlModel = model;
        this.gViewContainer = gContainer;
        this.mainSQLGraphView = mainSQLGraphView;
        super.setToolBar();
    }

    /**
     * create a graph view
     *
     * @return graph view
     */
    public IGraphView createGraphView() {
        SQLGraphView graphView = new SQLGraphView();
        this.gView = graphView;
        JoinPreviewGraphFactory graphFactory = new JoinPreviewGraphFactory(mainSQLGraphView);
        graphView.setGraphFactory(graphFactory);
        //set up model
        sqlModel.addSQLDataListener(graphView);
        return graphView;
    }

    /**
     * create a tool bar
     *
     * @return tool bar
     */
    public IToolBar createToolBar() {
        return null;
    }

    /**
     * create a graph controller
     *
     * @return
     */
    public IGraphController createGraphController() {
        IGraphController graphController = new JoinGraphController();
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
        return null;
    }

    /**
     * return toolbar actions
     *
     * @return toolbar actions
     */
    public List getToolBarActions() {
        return null;
    }

    /**
     * get operator xml info model which is defined in netbeans layer.xml file
     *
     * @return operator xml info model
     */
    public IOperatorXmlInfoModel getOperatorXmlInfoModel() {
        return null;
    }

    //to be removed
    /**
     * Gets operator model currently associated with this manager.
     *
     * @return current instance of IOperatorXmlInfoModel
     */
    public IOperatorXmlInfoModel getModel() {
        return null;
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
        return this.gView;
    }
}