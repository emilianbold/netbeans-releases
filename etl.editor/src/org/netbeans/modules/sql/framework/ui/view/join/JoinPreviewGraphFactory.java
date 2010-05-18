/*
 * JoinPreviewGraphFactory.java
 *
 * Created on January 16, 2004, 11:57 AM
 */

package org.netbeans.modules.sql.framework.ui.view.join;

import java.awt.Point;
import java.util.List;
import org.netbeans.modules.sql.framework.model.SQLCanvasObject;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLJoinOperator;
import org.netbeans.modules.sql.framework.model.SQLJoinTable;
import org.netbeans.modules.sql.framework.ui.graph.IGraphNode;
import org.netbeans.modules.sql.framework.ui.graph.IGraphView;
import org.netbeans.modules.sql.framework.ui.view.graph.AbstractGraphFactory;


/**
 * This is the graph factory for join preview graph panel
 *
 * @author radval
 */
public class JoinPreviewGraphFactory extends AbstractGraphFactory {

    private Point sourceTableLoc = new Point(50, 50);
    private List jSources;
    private IGraphView mainSQLGraphView;

    /** Creates a new instance of JoinPreviewGraphFactory */
    public JoinPreviewGraphFactory(IGraphView gView) {
        this.mainSQLGraphView = gView;
    }

    /**
     * factory method for creating instance of IGraphNode given an SQLObject
     *
     * @param canvasObj sql object to be represented in the graph
     * @return an instance of IGraphNode
     */
    public IGraphNode createGraphNode(SQLCanvasObject canvasObj) {
        int objectType = canvasObj.getObjectType();
        IGraphNode graphNode = null;

        switch (objectType) {
            case SQLConstants.JOIN_TABLE:
                SQLJoinTable joinTable = (SQLJoinTable) canvasObj;
                TableGraphNode tNode = new TableGraphNode(joinTable);
                graphNode = tNode;
                tNode.setLocation(sourceTableLoc);
                if (this.jSources != null && this.jSources.indexOf(joinTable) != -1) {
                    int cnt = this.jSources.indexOf(joinTable);
                    tNode.setNumber("" + (++cnt));
                }

                break;
            case SQLConstants.JOIN:
                JoinPreviewGraphNode joinNode = new JoinPreviewGraphNode();
                joinNode.addJoinTypeComboBox();
                joinNode.setMainSQLGraphView(this.mainSQLGraphView);

                graphNode = joinNode;
                SQLJoinOperator join = (SQLJoinOperator) canvasObj;
                if (!join.isRoot()) {
                    joinNode.showOutputPort(true);
                }
        }

        //if graph node is not null set canvas object as data object
        //also set location and add this graph node
        if (graphNode != null) {
            graphNode.setDataObject(canvasObj);
        }

        return graphNode;
    }

    public void setJoinSources(List<SQLJoinTable> joinSources) {
        this.jSources = joinSources;
    }
}