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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.edm.editor.graph;

import java.awt.Image;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Point;
import java.util.WeakHashMap;
import java.util.LinkedHashMap;
import java.awt.Dialog;

import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.widget.Widget;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.windows.WindowManager;
import org.netbeans.api.visual.action.HoverProvider;

import org.netbeans.modules.edm.editor.dataobject.MashupDataObject;
import org.netbeans.modules.edm.editor.graph.actions.RuntimeModelPopupProvider;
import org.netbeans.modules.edm.editor.graph.actions.TablePopupProvider;
import org.netbeans.modules.edm.editor.widgets.EDMNodeWidget;
import org.netbeans.modules.edm.editor.widgets.EDMPinWidget;
import org.netbeans.modules.edm.editor.widgets.EDMGraphScene;
import org.netbeans.modules.edm.editor.graph.actions.SceneAcceptProvider;
import org.netbeans.modules.edm.editor.graph.actions.ScenePopupProvider;
import org.netbeans.modules.edm.editor.utils.MashupGraphUtil;
import org.netbeans.modules.edm.editor.graph.actions.JoinPopupProvider;

import org.netbeans.modules.edm.model.SQLConstants;
import org.netbeans.modules.edm.model.SQLDBColumn;
import org.netbeans.modules.edm.model.SQLDBTable;
import org.netbeans.modules.edm.model.SQLDefinition;
import org.netbeans.modules.edm.model.SQLInputObject;
import org.netbeans.modules.edm.model.SQLJoinOperator;
import org.netbeans.modules.edm.model.SQLJoinTable;
import org.netbeans.modules.edm.model.SQLJoinView;
import org.netbeans.modules.edm.model.SQLObject;
import org.netbeans.modules.edm.model.RuntimeDatabaseModel;
import org.netbeans.modules.edm.model.RuntimeInput;
import org.netbeans.modules.edm.editor.utils.RuntimeAttribute;
import java.util.List;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.Anchor.Entry;
import org.netbeans.modules.edm.editor.graph.actions.EditJoinAction;
import org.netbeans.modules.edm.editor.graph.actions.EditJoinConditionAction;
import org.netbeans.modules.edm.editor.graph.actions.GroupByPopupProvider;
import org.netbeans.modules.edm.editor.graph.components.EDMDataOutputPanel;
import org.netbeans.modules.edm.editor.graph.components.EDMNavigatorComponent;
import org.netbeans.modules.edm.editor.graph.components.EDMOutputTopComponent;
import org.netbeans.modules.edm.editor.graph.components.EDMSQLStatementPanel;
import org.netbeans.modules.edm.editor.graph.components.TableChooserPanel;
import org.netbeans.modules.edm.editor.utils.ImageConstants;
import org.netbeans.modules.edm.editor.widgets.property.GroupByNode;
import org.netbeans.modules.edm.editor.widgets.property.JoinNode;
import org.netbeans.modules.edm.editor.widgets.property.TableNode;
import org.netbeans.modules.edm.editor.widgets.property.editor.ColumnSelectionEditor;
import org.netbeans.modules.edm.model.SQLCondition;
import org.netbeans.modules.edm.model.SQLGroupBy;
import org.netbeans.modules.edm.model.SourceTable;
import org.netbeans.modules.edm.model.impl.SQLGroupByImpl;
import org.netbeans.modules.edm.editor.utils.UIUtil;
import org.netbeans.modules.edm.model.DBColumn;
import org.netbeans.modules.edm.model.ForeignKey;
import org.netbeans.modules.edm.model.EDMException;
import org.netbeans.modules.edm.editor.widgets.EDMConnectionWidget;
import org.netbeans.modules.edm.editor.widgets.EDMNodeAnchor;
import org.netbeans.modules.edm.model.DBConnectionDefinition;
import org.netbeans.modules.edm.model.DBTable;
import org.netbeans.modules.edm.model.GUIInfo;
import org.netbeans.modules.edm.model.SQLCanvasObject;
import org.netbeans.modules.edm.model.SourceColumn;
import org.netbeans.modules.edm.model.impl.SQLConditionImpl;
import org.netbeans.modules.edm.model.impl.SQLJoinOperatorImpl;
import org.netbeans.modules.edm.model.impl.SQLJoinTableImpl;
import org.netbeans.modules.edm.model.impl.SourceTableImpl;
import org.netbeans.modules.edm.editor.utils.StringUtil;
import org.netbeans.modules.edm.editor.utils.XmlUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author karthikeyan s
 */
public class MashupGraphManager {

    private MashupDataObject mObj;
    private EDMGraphScene scene;
    private JScrollPane pane;
    private JComponent satelliteView;
    private long edgeCounter = 1;
    private long nodeCounter = 1;
    private long pinCounter = 1;
    private Map<SQLObject, Widget> sqltoWidgetMap = new HashMap<SQLObject, Widget>();
    private WeakHashMap<Widget, SQLObject> widgetToObjectMap =
            new WeakHashMap<Widget, SQLObject>();
    private Map<String, String> edgeMap = new HashMap<String, String>();
    private List<Widget> widgets = new ArrayList<Widget>();
    private WidgetAction columnSelectionEditor;
    private HashMap<String, EDMDataOutputPanel> outputDataViewMap = new HashMap<String, EDMDataOutputPanel>();
    private HashMap<String, EDMSQLStatementPanel> sqlViewMap = new HashMap<String, EDMSQLStatementPanel>();

    public MashupGraphManager() {
        scene = new EDMGraphScene();
        pane = new JScrollPane();

        JComponent view = scene.createView();
        pane.setViewportView(view);
        satelliteView = scene.createSatelliteView();

        scene.getActions().addAction(ActionFactory.createZoomAction());
        scene.getActions().addAction(ActionFactory.createPanAction());
        scene.getActions().addAction(ActionFactory.createMoveAction());

        // vlv: print
        view.putClientProperty(java.awt.print.Printable.class, ""); // NOI18N
    }

    /**
     * Creates a Mashup graph scene.
     * @param dObj MashupDataObject
     */
    public MashupGraphManager(MashupDataObject dObj) {
        this();
        mObj = dObj;
        scene.getActions().addAction(ActionFactory.createAcceptAction(
                new SceneAcceptProvider(mObj, this)));
        scene.getActions().addAction(ActionFactory.createPopupMenuAction(
                new ScenePopupProvider(mObj, this)));
        columnSelectionEditor = ActionFactory.createInplaceEditorAction(
                new ColumnSelectionEditor(mObj));
    }

    public EDMGraphScene getScene() {
        return scene;
    }

    public void layoutGraph() {
        if (this.mObj.getModel().getSQLDefinition().getObjectsOfType(SQLConstants.JOIN_VIEW).size() == 0) {
            scene.layoutScene(true);
        } else {
            scene.layoutScene(false);
        }
        if (null != EDMNavigatorComponent.getInstance()) {
            EDMNavigatorComponent.getInstance().setNewContent(mObj);
        }
    }

    public void unJoin() {
        try {
            Iterator iter = scene.getNodes().iterator();
            while (iter.hasNext()) {
                EDMNodeWidget widget = (EDMNodeWidget) scene.findWidget(iter.next());
                if ((widget.getNodeName().trim().equalsIgnoreCase("ROOT JOIN")) || (widget.getNodeName().trim().equalsIgnoreCase("JOIN")) || (widget.getNodeName().trim().equalsIgnoreCase("GROUP BY"))) {
                    widget.removeFromParent();
                    scene.validate();
                }
            }
            Collection<String> edgesStr = scene.getEdges();
            for (String str : edgesStr) {
                Collection<String> c = scene.findEdgesBetween(scene.getEdgeSource(str), scene.getEdgeTarget(str));
                for (String collStr : c) {
                    EDMConnectionWidget connWd = (EDMConnectionWidget) scene.findWidget(collStr);
                    connWd.removeFromParent();
                    scene.validate();
                }
            }
        } catch (Exception e) {
            e.getMessage();
        }
        persistGuiForAllObjects();
    }

    public void persistGuiForAllObjects() {
        for (Widget w : scene.getNodesInScene()) {
            mObj.persistGUIInfo(w.getPreferredLocation(), (EDMNodeWidget) w, w.getBounds());
        }
    }

    public void removeObject(SQLObject obj) {
        try {
            Iterator iter = scene.getNodes().iterator();
            while (iter.hasNext()) {
                EDMNodeWidget widget = (EDMNodeWidget) scene.findWidget(iter.next());
                if (widget.getNodeName().trim().equalsIgnoreCase(obj.getDisplayName().trim())) {
                    widget.removeFromParent();
                    scene.validate();
                }
                try {
                    HashMap<EDMNodeWidget, Anchor> edgesMap = scene.getEdgesMap();
                    if (edgesMap != null) {
                        EDMNodeAnchor anchor = (EDMNodeAnchor) edgesMap.get(widget);
                        EDMNodeWidget edmWidget = (EDMNodeWidget) anchor.getRelatedWidget();
                        if (edmWidget.getNodeName().trim().equals("Group By")) {
                            List<Anchor.Entry> entries = anchor.getEntries();
                            for (Entry entry : entries) {
                                EDMConnectionWidget connWd = (EDMConnectionWidget) entry.getAttachedConnectionWidget();
                                connWd.removeFromParent();
                            }
                            edmWidget.removeFromParent();
                            scene.validate();
                        }
                    }
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
        }
    }

    public void removeRuntimeArgs(SourceColumn obj, SQLObject sqlObj) {
        try {
            for (Widget w : scene.getNodesInScene()) {
                EDMNodeWidget edmWidget = (EDMNodeWidget) w;
                if (edmWidget.getNodeName().trim().equalsIgnoreCase("Runtime Input")) {
                    List<Widget> list = edmWidget.getChildren();
                    Iterator iter = list.iterator();
                    while (iter.hasNext()) {
                        Widget w1 = (Widget) iter.next();
                        if (w1 instanceof EDMPinWidget) {
                            EDMPinWidget pinWidget = (EDMPinWidget) w1;
                            if (obj.getName().contains(pinWidget.getPinName())) {
                                pinWidget.removeFromParent();
                                scene.validate();
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
        }
    }

    public void refreshGraph() {
        try {
            this.mObj.establishRuntimeInputs(this.mObj.getModel(), this.mObj.getModel().getSQLDefinition().getSourceTables());
        } catch (EDMException ex) {
            Exceptions.printStackTrace(ex);
        }
        generateGraph(this.mObj.getModel().getSQLDefinition());
        scene.validate();
    }

    public JComponent getSatelliteView() throws Exception {
        return satelliteView;
    }

    public void fitToPage() {
        Rectangle rectangle = new Rectangle(0, 0, 1, 1);
        for (Widget widget : scene.getChildren()) {
            rectangle = rectangle.union(widget.convertLocalToScene(widget.getBounds()));
        }
        Dimension dim = rectangle.getSize();
        Dimension viewDim = pane.getViewportBorderBounds().getSize();
        scene.getSceneAnimator().animateZoomFactor(
                Math.min((float) viewDim.width / dim.width,
                (float) viewDim.height / dim.height));
        scene.validate();
    }

    public void fitToWidth() {
        Rectangle rectangle = new Rectangle(0, 0, 1, 1);
        for (Widget widget : scene.getChildren()) {
            rectangle = rectangle.union(widget.convertLocalToScene(widget.getBounds()));
        }
        Dimension dim = rectangle.getSize();
        Dimension viewDim = pane.getViewportBorderBounds().getSize();
        scene.getSceneAnimator().animateZoomFactor(
                (float) viewDim.width / dim.width);
        scene.validate();
    }

    public void fitToHeight() {
        Rectangle rectangle = new Rectangle(0, 0, 1, 1);
        for (Widget widget : scene.getChildren()) {
            rectangle = rectangle.union(widget.convertLocalToScene(widget.getBounds()));
        }
        Dimension dim = rectangle.getSize();
        Dimension viewDim = pane.getViewportBorderBounds().getSize();
        scene.getSceneAnimator().animateZoomFactor(
                (float) viewDim.height / dim.height);
        scene.validate();
    }

    public void expandAll() {
        Iterator<Widget> it = widgets.iterator();
        while (it.hasNext()) {
            Widget wd = it.next();
            if (wd instanceof EDMNodeWidget) {
                ((EDMNodeWidget) wd).expandWidget();
                wd.revalidate();
            }
        }
    }

    public void collapseAll() {
        Iterator<Widget> it = widgets.iterator();
        while (it.hasNext()) {
            Widget wd = it.next();
            if (wd instanceof EDMNodeWidget) {
                ((EDMNodeWidget) wd).collapseWidget();
                wd.revalidate();
            }
        }
    }

    public void zoomGraph(double zoomFactor) {
        scene.getSceneAnimator().animateZoomFactor(zoomFactor);
        scene.validate();
    }

    public void zoomIn() {
        if (scene.getZoomFactor() * 1.1 < 2.0) {
            scene.getSceneAnimator().animateZoomFactor(scene.getZoomFactor() * 1.1);
            scene.validate();
        }
    }

    public void zoomOut() {
        if (scene.getZoomFactor() * 0.9 > 0.33) {
            scene.getSceneAnimator().animateZoomFactor(scene.getZoomFactor() * 0.9);
            scene.validate();
        }
    }

    public boolean addGroupby(Point point) {
        boolean status = false;
        try {
            SQLGroupBy groupby = new SQLGroupByImpl();
            SQLJoinView[] joinViews = getJoinViews();
            if (joinViews.length != 0) {
                List<DBColumn> columns = new ArrayList<DBColumn>();
                SQLDBTable[] tables = (SQLDBTable[]) joinViews[0].getSourceTables().
                        toArray(new SQLDBTable[0]);
                for (SQLDBTable table : tables) {
                    columns.addAll(table.getColumnList());
                }
                groupby.setColumns(columns);
                joinViews[0].setSQLGroupBy(groupby);
                groupby.setParentObject(joinViews[0]);
                status = true;
            } else {
                DialogDescriptor dlgDesc = null;
                List<SQLDBTable> srcTbls = new ArrayList<SQLDBTable>();
                for (DBTable tbl : mObj.getModel().getSQLDefinition().getSourceTables()) {
                    srcTbls.add((SQLDBTable) tbl);
                }
                TableChooserPanel panel = new TableChooserPanel(srcTbls);
                dlgDesc = new DialogDescriptor(panel, NbBundle.getMessage(MashupGraphManager.class, "LBL_Select_Table"), true,
                        NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.OK_OPTION,
                        DialogDescriptor.DEFAULT_ALIGN, null, null);
                Dialog dlg = DialogDisplayer.getDefault().createDialog(dlgDesc);
                dlg.setVisible(true);
                if (NotifyDescriptor.OK_OPTION == dlgDesc.getValue()) {
                    SQLDBTable table = panel.getSelectedTable();
                    if (table == null) {
                        NotifyDescriptor d =
                                new NotifyDescriptor.Message(NbBundle.getMessage(MashupGraphManager.class, "MSG_Group_by_discarded"),
                                NotifyDescriptor.WARNING_MESSAGE);
                        DialogDisplayer.getDefault().notify(d);
                    } else {
                        groupby.setColumns(table.getColumnList());
                        groupby.setParentObject(table);
                        ((SourceTable) table).setSQLGroupBy(groupby);
                        status = true;
                        addTable(table, point);
                    }
                }
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return status;
    }

    public void generateGraph(SQLDefinition sqlDefinition) {
        UIUtil.startProgressDialog(mObj.getName(), NbBundle.getMessage(MashupGraphManager.class, "MSG_Generating_graph"));
        removeAllChildren();
        try {
            SQLJoinView[] joinViews = getJoinViews();
            if (joinViews != null && joinViews.length != 0) {
                addJoinsAndTables(sqlDefinition, joinViews[0]);
            } else {
                addTablesOnly(sqlDefinition);
            }

            // Add runtime models
            addRuntimeModel(sqlDefinition);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        UIUtil.stopProgressDialog();
    }

    public void setDataObject(MashupDataObject mashupDataObject) {
        this.mObj = mashupDataObject;
    }

    public JScrollPane getPanel() {
        if (pane == null) {
            pane = new JScrollPane();
            pane.setViewportView(scene.createView());
        }
        return pane;
    }

    public void setLog(String text) {
        EDMOutputTopComponent win = EDMOutputTopComponent.findInstance();
        win.setLog(text);
    }

    public void showOutput(SQLObject object, SQLDefinition sqlDef) {
        String id = object.getId();
        if (object instanceof SQLJoinOperator) {
            id = ((SQLJoinView) object.getParentObject()).getId();
        }
        EDMDataOutputPanel dataPanel = outputDataViewMap.get(id);
        if (dataPanel == null) {
            dataPanel = new EDMDataOutputPanel(object);
            outputDataViewMap.put(id, dataPanel);
        }
        dataPanel.removeAll();
        dataPanel.generateOutput(object, sqlDef, mObj.getName());
        dataPanel.revalidate();
        dataPanel.setVisible(true);
    }

    public void showSql(SQLObject object, SQLDefinition sqlDef) {
        String id = object.getId();
        if (object instanceof SQLJoinOperator) {
            id = ((SQLJoinView) object.getParentObject()).getId();
        }
        EDMSQLStatementPanel sqlPanel = sqlViewMap.get(id);
        if (sqlPanel == null) {
            sqlPanel = new EDMSQLStatementPanel(object);
            sqlViewMap.put(id, sqlPanel);
        }
        sqlPanel.showSql(object, sqlDef);
        sqlPanel.revalidate();
        sqlPanel.setVisible(true);
    }

    public void updateColumnSelection(SQLDBTable table) {
        EDMNodeWidget widget = (EDMNodeWidget) sqltoWidgetMap.get(table);
        List<Widget> usedCol = new LinkedList<Widget>();
        List<Widget> unusedCol = new LinkedList<Widget>();
        HashMap<String, List<Widget>> categories = new LinkedHashMap<String, List<Widget>>();
        SQLDBColumn[] columns = (SQLDBColumn[]) table.getColumnList().
                toArray(new SQLDBColumn[0]);
        for (SQLDBColumn column : columns) {
            Widget[] children = widget.getChildren().toArray(new Widget[0]);
            EDMPinWidget pin = null;
            for (Widget child : children) {
                if (child instanceof EDMPinWidget &&
                        ((EDMPinWidget) child).getPinName().equals(column.getDisplayName())) {
                    pin = (EDMPinWidget) child;
                    break;
                }
            }
            if (column.isVisible()) {
                usedCol.add(pin);
            } else {
                unusedCol.add(pin);
            }
        }
        if (usedCol.size() != 0) {
            categories.put(NbBundle.getMessage(MashupGraphManager.class, "TITLE_Used_Columns"), usedCol);
        }
        if (unusedCol.size() != 0) {
            categories.put(NbBundle.getMessage(MashupGraphManager.class, "TITLE_Unused_Columns"), unusedCol);
        }
        widget.sortPins(categories);
        widget.revalidate();
    }

    public void setSelectedNode(Widget wd) {
        SQLObject obj = widgetToObjectMap.get(wd);
        if (obj != null) {
            if (obj instanceof SQLJoinOperator) {
                WindowManager.getDefault().getRegistry().getActivated().
                        setActivatedNodes(new Node[]{new JoinNode((SQLJoinOperator) obj, mObj)});
            } else if (obj instanceof SQLDBTable) {
                WindowManager.getDefault().getRegistry().getActivated().
                        setActivatedNodes(new Node[]{new TableNode((SourceTable) obj)});
            } else if (obj instanceof SQLJoinTable) {
                SQLJoinTable joinTbl = (SQLJoinTable) obj;
                WindowManager.getDefault().getRegistry().getActivated().
                        setActivatedNodes(new Node[]{new TableNode(joinTbl.getSourceTable())});
            } else if (obj instanceof SQLGroupByImpl) {
                SQLGroupByImpl grpby = (SQLGroupByImpl) obj;
                WindowManager.getDefault().getRegistry().getActivated().
                        setActivatedNodes(new Node[]{new GroupByNode(grpby, mObj)});
            }
        }
    }

    public SQLObject mapWidgetToObject(Widget widget) {
        return widgetToObjectMap.get(widget);
    }

    public Widget mapsqltoWidget(SQLObject sqlObj) {
        return sqltoWidgetMap.get(sqlObj);
    }

    public void validateScene() {
        scene.validate();
    }

    private void createGraphEdge(String sourcePinID, String targetNodeID) {
        String edgeID = "edge" + this.edgeCounter++;
        Widget widget = scene.addEdge(edgeID);
        widgets.add(widget);
        scene.setEdgeSource(edgeID, sourcePinID + EDMGraphScene.PIN_ID_DEFAULT_SUFFIX);
        scene.setEdgeTarget(edgeID, targetNodeID + EDMGraphScene.PIN_ID_DEFAULT_SUFFIX);
        edgeMap.put(edgeID, sourcePinID + "#" + targetNodeID);
        scene.validate();
    }

    private String createGraphNode(SQLObject model) {
        String nodeID = model.getId() + "#" + this.nodeCounter++;
        EDMNodeWidget widget = (EDMNodeWidget) scene.addNode(nodeID);
        widgets.add(widget);
        widgetToObjectMap.put(widget, model);
        scene.validate();
        widget.setNodeImage(MashupGraphUtil.getImageForObject(model.getObjectType()));
        scene.validate();
        if (model instanceof SQLJoinOperator) {
            addJoinOperatorNode((SQLJoinOperator) model, widget, nodeID);
        } else if (model instanceof RuntimeInput) {
            addRuntimeNode((RuntimeInput) model, widget, nodeID);
        } else if (model instanceof SQLDBTable) {
            addTableNode((SQLDBTable) model, widget, nodeID);
        } else if (model instanceof SQLJoinTable) {
            addTableNode((SQLDBTable) ((SQLJoinTable) model).getSourceTable(),
                    widget, nodeID);
        } else if (model instanceof SQLGroupByImpl) {
            addGroupbyNode((SQLGroupByImpl) model, widget, nodeID);
            widget.setNodeImage(MashupGraphUtil.getImage(ImageConstants.GROUPBY));
            scene.validate();
        }
        widget.getActions().addAction(scene.createWidgetHoverAction());
        widget.getActions().addAction(ActionFactory.createHoverAction(new MashupHoverProvider()));
        scene.addPin(nodeID, nodeID + EDMGraphScene.PIN_ID_DEFAULT_SUFFIX);
        scene.validate();
        edmNodeWidget = widget;
        return nodeID;
    }
    private EDMNodeWidget edmNodeWidget;

    private void recursivelyAddNodes(SQLJoinOperator rootJoin, String join) {
        SQLInputObject leftIn = rootJoin.getInput(SQLJoinOperator.LEFT);
        SQLInputObject rightIn = rootJoin.getInput(SQLJoinOperator.RIGHT);

        // left side traversal
        while (true) {
            String left = createGraphNode(leftIn.getSQLObject());

            // check for groupby operator.
            if (leftIn.getSQLObject().getObjectType() == SQLConstants.JOIN_TABLE) {
                SQLGroupBy groupby = ((SQLJoinTable) leftIn.getSQLObject()).getSourceTable().getSQLGroupBy();
                if (groupby != null) {
                    String grpbyNode = createGraphNode((SQLGroupByImpl) groupby);
                    createGraphEdge(grpbyNode, left);
                    left = grpbyNode;
                }
            }

            createGraphEdge(join, left);
            if (leftIn.getSQLObject().getObjectType() == SQLConstants.JOIN_TABLE) {
                break;
            }
            recursivelyAddNodes((SQLJoinOperator) leftIn.getSQLObject(), left);
            break;
        }

        // right side traversal
        while (true) {
            String right = createGraphNode(rightIn.getSQLObject());

            // check for groupby operator.
            if (rightIn.getSQLObject().getObjectType() == SQLConstants.JOIN_TABLE) {
                SQLGroupBy groupby = ((SQLJoinTable) rightIn.getSQLObject()).getSourceTable().getSQLGroupBy();
                if (groupby != null) {
                    String grpbyNode = createGraphNode((SQLGroupByImpl) groupby);
                    createGraphEdge(grpbyNode, right);
                    right = grpbyNode;
                }
            }
            createGraphEdge(join, right);
            if (rightIn.getSQLObject().getObjectType() == SQLConstants.JOIN_TABLE) {
                break;
            }
            recursivelyAddNodes((SQLJoinOperator) rightIn.getSQLObject(), right);
            break;
        }
    }

    private void addJoinOperatorNode(SQLJoinOperator joinOp, EDMNodeWidget widget, String nodeID) {
        sqltoWidgetMap.put(joinOp, widget);
        String nodeName = "";
        if (joinOp.isRoot()) {
            nodeName = NbBundle.getMessage(MashupGraphManager.class, "TITLE_ROOT_JOIN");
        } else {
            nodeName = NbBundle.getMessage(MashupGraphManager.class, "TITLE_JOIN");
        }
        widget.setNodeName(nodeName);
        widget.getActions().addAction(new ConditionAction(mObj, joinOp));

        EDMPinWidget joinTypePin = ((EDMPinWidget) scene.addPin(
                nodeID, "nodeID" + "#pin" + pinCounter++));
        scene.validate();
        joinTypePin.setPinName(NbBundle.getMessage(MashupGraphManager.class, "TITLE_JOIN_TYPE"));
        scene.validate();
        List<Image> typeImage = new ArrayList<Image>();
        typeImage.add(MashupGraphUtil.getImage(ImageConstants.PROPERTIES));
        joinTypePin.setGlyphs(typeImage);
        scene.validate();
        widgets.add(joinTypePin);
        EDMPinWidget conditionPin = ((EDMPinWidget) scene.addPin(
                nodeID, "nodeID" + "#pin" + pinCounter++));
        scene.validate();
        conditionPin.setPinName(NbBundle.getMessage(MashupGraphManager.class, "TITLE_CONDITION"));
        conditionPin.getActions().addAction(new ConditionAction(mObj, joinOp));
        conditionPin.getActions().addAction(ActionFactory.createHoverAction(new MashupHoverProvider()));
        List<Image> image = new ArrayList<Image>();
        image.add(MashupGraphUtil.getImage(ImageConstants.CONDITION));
        conditionPin.setGlyphs(image);
        scene.validate();
        widgets.add(conditionPin);

        // add popup for join widget.
        widget.getActions().addAction(
                ActionFactory.createPopupMenuAction(new JoinPopupProvider(
                joinOp, mObj)));
        scene.validate();
    }

    private void addRuntimeNode(RuntimeInput rtInput, EDMNodeWidget widget,
            String nodeID) {
        sqltoWidgetMap.put(rtInput, widget);
        widget.setNodeName(NbBundle.getMessage(MashupGraphManager.class, "TITLE_Runtime_Input"));
        Iterator it = rtInput.getRuntimeAttributeMap().keySet().iterator();
        while (it.hasNext()) {
            String name = (String) it.next();
            EDMPinWidget columnPin = ((EDMPinWidget) scene.addPin(nodeID, "nodeID" + "#pin" + pinCounter++));
            scene.validate();
            columnPin.setPinName(name);
            List<Image> image = new ArrayList<Image>();
            image.add(MashupGraphUtil.getImage(ImageConstants.RUNTIMEATTR));
            columnPin.setGlyphs(image);
            scene.validate();
            RuntimeAttribute rtAttr = (RuntimeAttribute) rtInput.getRuntimeAttributeMap().get(name);
            columnPin.setToolTipText("<html><table border=0 cellspacing=0 cellpadding=0 >" +
                    "<tr><td>&nbsp; Value</td><td>&nbsp; : &nbsp; <b>" + rtAttr.getAttributeValue() +
                    "</b></td></tr></table></html>");
            widgets.add(columnPin);
        }

        // add popup for runtime inputs.
        widget.getActions().addAction(
                ActionFactory.createPopupMenuAction(
                new RuntimeModelPopupProvider(rtInput, mObj)));
        runtimeWidget = widget;
        scene.validate();
    }
    EDMNodeWidget runtimeWidget;

    private void addTableNode(SQLDBTable tbl, EDMNodeWidget widget, String nodeID) {
        sqltoWidgetMap.put(tbl, widget);
        widget.setNodeName(tbl.getDisplayName());
        scene.validate();
        widget.getActions().addAction(ActionFactory.createPopupMenuAction(new TablePopupProvider(tbl, mObj)));
        String condition = ((SourceTable) tbl).getFilterCondition().getConditionText();
        if (condition != null && !condition.equals("")) {
            List<Image> image = new ArrayList<Image>();
            image.add(MashupGraphUtil.getImage(ImageConstants.FILTER));
            widget.setGlyphs(image);
            scene.validate();

        }

        // now add columns.
        SQLDBColumn[] columns = (SQLDBColumn[]) tbl.getColumnList().
                toArray(new SQLDBColumn[0]);
        List<Widget> usedCol = new ArrayList<Widget>();
        List<Widget> unusedCol = new ArrayList<Widget>();
        Map<String, List<Widget>> categories = new LinkedHashMap<String, List<Widget>>();
        for (SQLDBColumn column : columns) {
            String pinTooltip = UIUtil.getColumnToolTip(column);
            EDMPinWidget columnPin = ((EDMPinWidget) scene.addPin(nodeID, "nodeID" + "#pin" + pinCounter++));
            scene.validate();
            columnPin.setPinName(column.getDisplayName());
            columnPin.getActions().addAction(columnSelectionEditor);
            scene.validate();
            List<Image> image = new ArrayList<Image>();
            if (column.isVisible()) {
                if (column.isPrimaryKey()) {
                    image.add(MashupGraphUtil.getImage(ImageConstants.PRIMARYKEYCOL));
                    pinTooltip = pinTooltip + "<tr> <td>&nbsp; PRIMARY KEY </td> <td> &nbsp; : &nbsp; <b> Yes </b> </td> </tr>";//<tr><td colspan=2><b>PRIMARY KEY</b></td></tr>";
                } else if (column.isForeignKey()) {
                    image.add(MashupGraphUtil.getImage(ImageConstants.FOREIGNKEYCOL));
                    image.add(MashupGraphUtil.getImage(ImageConstants.FOREIGNKEY));
                    pinTooltip = pinTooltip + "<tr> <td>&nbsp; FOREIGN KEY </td> <td> &nbsp; : &nbsp;<b>" + getForeignKeyString(column) + "</b></td></tr>";//<tr><td colspan=2><b>FOREIGN KEY</b></td></tr>";
                } else {
                    image.add(MashupGraphUtil.getImage(ImageConstants.COLUMN));
                }
                usedCol.add(columnPin);

            } else {
                image.add(MashupGraphUtil.getImage(ImageConstants.COLUMN));
                unusedCol.add(columnPin);

            }
            pinTooltip = pinTooltip + "</table></html>";
            columnPin.setGlyphs(image);
            scene.validate();
            columnPin.setToolTipText(pinTooltip);
            scene.validate();
            widgets.add(columnPin);
            sqltoWidgetMap.put(column, columnPin);
            widgetToObjectMap.put(columnPin, column);
        }
        if (usedCol.size() != 0) {
            categories.put(NbBundle.getMessage(MashupGraphManager.class, "TITLE_Used_Columns"), usedCol);
        }
        if (unusedCol.size() != 0) {
            categories.put(NbBundle.getMessage(MashupGraphManager.class, "TITLE_Unused_Columns"), unusedCol);
        }
        widget.sortPins(categories);
    }

    private void addGroupbyNode(SQLGroupByImpl groupby,
            EDMNodeWidget widget, String nodeID) {
        sqltoWidgetMap.put(groupby, widget);
        widget.setNodeName(NbBundle.getMessage(MashupGraphManager.class, "TITLE_Group_By"));
        SQLCondition condition = groupby.getHavingCondition();
        String conditionText = "<NO CONDITION>";
        if (condition != null) {
            conditionText = condition.getConditionText();
        }
        EDMPinWidget havingPin = ((EDMPinWidget) scene.addPin(nodeID, "nodeID" + "#pin" + pinCounter++));
        List<Image> image = new ArrayList<Image>();
        image.add(MashupGraphUtil.getImage(ImageConstants.FILTER));
        havingPin.setGlyphs(image);
        havingPin.setPinName(NbBundle.getMessage(MashupGraphManager.class, "TITLE_HAVING_CLAUSE"));
        havingPin.setToolTipText(conditionText);
        scene.validate();
        widget.getActions().addAction(
                ActionFactory.createPopupMenuAction(new GroupByPopupProvider(groupby, mObj)));
        widgets.add(havingPin);
    }

    private void addJoinsAndTables(SQLDefinition sqlDefinition, SQLJoinView joinView) {
        SQLJoinOperator joinOperator = joinView.getRootJoin();
        String join = createGraphNode(joinOperator);
        recursivelyAddNodes(joinOperator, join);

        // Add tables which are not a part of the join.
        SQLDBTable[] dbTables = (SQLDBTable[]) sqlDefinition.getJoinSources().toArray(new SQLDBTable[0]);
        for (SQLDBTable dbTable : dbTables) {
            createGraphNode(dbTable);
        }

        // Add groupby operator.
        SQLGroupBy grpBy = joinView.getSQLGroupBy();
        if (grpBy != null) {
            String grpbyId = createGraphNode((SQLGroupByImpl) grpBy);
            createGraphEdge(grpbyId, join);
        }
    }

    public void addTable(SQLDBTable table, Point p) {
        createGraphNode(table);
        edmNodeWidget.setPreferredLocation(p);
        try {
            mObj.persistGUIInfo(p, (EDMNodeWidget) edmNodeWidget, edmNodeWidget.getBounds());
        } catch (Exception e) {
        }
        scene.validate();

        // Remove the old runtime widget
        Point rtArgLoc = null;
        try {
            for (Widget w : scene.getNodesInScene()) {
                EDMNodeWidget edmWidget = (EDMNodeWidget) w;
                if (edmWidget.getNodeName().trim().equalsIgnoreCase("Runtime Input")) {
                    rtArgLoc = edmWidget.getLocation();
                    edmWidget.removeFromParent();
                    scene.validate();
                }
            }
        } catch (Exception e) {
        }

        // Add the runtime widget with old location
        try {
            this.mObj.establishRuntimeInputs(this.mObj.getModel(), this.mObj.getModel().getSQLDefinition().getSourceTables());
            addRuntimeModel(this.mObj.getModel().getSQLDefinition());
            if (rtArgLoc != null) {
                runtimeWidget.setPreferredLocation(rtArgLoc);
            } else {
                runtimeWidget.setPreferredLocation(new Point(p.x + edmNodeWidget.getBounds().width + 10, p.y));
            }

            scene.validate();
        } catch (Exception ex) {
        }
    }

    private Point avoidOverlap(Rectangle r) {
        // Rectangle r = w.getBounds();
        int x = 10;
        int y = 5;
        Point point = new Point(x * 2, y * 2);
        HashSet<Widget> unresolvedNodesSet = new HashSet<Widget>(scene.getNodesInScene());
        for (Widget edmWidget : unresolvedNodesSet) {
            Rectangle sceneRect = edmWidget.convertLocalToScene(edmWidget.getBounds());
            if (r.intersects(sceneRect)) {
                point = new Point(x * 2, y * 2);
                avoidOverlap(new Rectangle(point));
            } else {
                return point;
            }
        }
        return point;
    }

    private void addTablesOnly(SQLDefinition sqlDefinition) {
        SQLDBTable[] dbTables = (SQLDBTable[]) sqlDefinition.getSourceTables().
                toArray(new SQLDBTable[0]);
        for (SQLDBTable dbTable : dbTables) {
            String nodeId = createGraphNode(dbTable);
            SQLGroupBy groupBy = ((SourceTable) dbTable).getSQLGroupBy();
            if (groupBy != null) {
                String grpbyId = createGraphNode((SQLGroupByImpl) groupBy);
                createGraphEdge(grpbyId, nodeId);
            }
        }
    }

    private void addRuntimeModel(SQLDefinition sqlDefinition) {
        RuntimeDatabaseModel rtModel = sqlDefinition.getRuntimeDbModel();
        if (rtModel != null) {
            RuntimeInput rtInput = rtModel.getRuntimeInput();
            if (rtInput != null) {
                if (rtInput.getRuntimeAttributeMap().size() != 0) {
                    createGraphNode(rtInput);
                }
            }
        }
    }

    private SQLJoinView[] getJoinViews() {
        SQLJoinView[] joinViews = (SQLJoinView[]) mObj.getModel().getSQLDefinition().getObjectsOfType(SQLConstants.JOIN_VIEW).toArray(new SQLJoinView[0]);
        return joinViews;
    }

    private void removeAllChildren() {
        Iterator it = widgets.iterator();
        while (it.hasNext()) {
            Widget wd = (Widget) it.next();
            wd.removeFromParent();
            scene.validate();
        }

        // clear all data structures.
        sqltoWidgetMap.clear();
        edgeMap.clear();
        widgets.clear();
        widgetToObjectMap.clear();
    }

    public GUIInfo getGUIInfo(Widget w) {
        SQLObject obj = mapWidgetToObject(w);
        if (obj instanceof SQLJoinOperatorImpl) {
            SQLJoinOperatorImpl join = (SQLJoinOperatorImpl) obj;
            SQLConditionImpl tbl = (SQLConditionImpl) join.getJoinCondition();
            return tbl.getGUIInfo();
        } else if (obj instanceof SQLCanvasObject) {
            SQLCanvasObject tbl = (SQLCanvasObject) obj;
            return tbl.getGUIInfo();
        } else if (obj instanceof SQLGroupByImpl) {
            SQLGroupByImpl grpBy = (SQLGroupByImpl) obj;
            SQLConditionImpl tbl = (SQLConditionImpl) grpBy.getHavingCondition();
            return tbl.getGUIInfo();
        }
        return null;
    }

    public void getGuiInfo() {
        try {
            if (!mObj.newFile) {
                List<Widget> w = scene.getNodesInScene();
                int x = 10;
                int y = 10;
                for (Widget wd : w) {
                    SQLObject obj = mapWidgetToObject(wd);
                    if (obj instanceof SQLJoinOperatorImpl) {
                        SQLJoinOperatorImpl join = (SQLJoinOperatorImpl) obj;
                        SQLConditionImpl tbl = (SQLConditionImpl) join.getJoinCondition();
                        x = tbl.getGUIInfo().getX();
                        y = tbl.getGUIInfo().getY();
                    } else if (obj instanceof SQLCanvasObject) {
                        SQLCanvasObject tbl = (SQLCanvasObject) obj;
                        x = tbl.getGUIInfo().getX();
                        y = tbl.getGUIInfo().getY();
                    } else if (obj instanceof SQLGroupByImpl) {
                        SQLGroupByImpl grpBy = (SQLGroupByImpl) obj;
                        SQLConditionImpl tbl = (SQLConditionImpl) grpBy.getHavingCondition();
                        x = tbl.getGUIInfo().getX();
                        y = tbl.getGUIInfo().getY();
                    }
                    if (x < 0 || y < 0) {
                        layoutGraph();
                    } else {
                        wd.setPreferredLocation(new Point(x, y));
                    }
                }
                scene.validate();
                scene.createSatelliteView();
            } else {
                // TODO: Find empty location and drop the object
                layoutGraph();
            }
        } catch (Exception e) {
        }
    }

    public static String getForeignKeyString(DBColumn column) {
        String refString = column.getName() + " --> ";
        StringBuilder str = new StringBuilder(refString);
        DBTable table = column.getParent();
        List list = table.getForeignKeys();

        Iterator it = list.iterator();
        while (it.hasNext()) {
            ForeignKey fk = (ForeignKey) it.next();
            if (fk.contains(column)) {
                List pkColumnList = fk.getPKColumnNames();
                Iterator it1 = pkColumnList.iterator();
                while (it1.hasNext()) {
                    String pkColName = (String) it1.next();
                    str.append(pkColName);
                    if (it1.hasNext()) {
                        str.append(", ");
                    }
                }
            }
        }
        return str.toString();
    }

    private class MashupHoverProvider implements HoverProvider {

        public void widgetHovered(Widget widget) {
            EDMNodeWidget edmWd = null;
            if (widget != null) {
                if (widget instanceof EDMNodeWidget) {
                    edmWd = (EDMNodeWidget) widget;
                }
                if (widget instanceof EDMPinWidget) {
                    EDMPinWidget pin = (EDMPinWidget) widget;
                    if (pin.getPinName().equalsIgnoreCase("Condition")) {
                        EDMNodeWidget e = (EDMNodeWidget) widget.getParentWidget();
                        SQLObject sqlObj = widgetToObjectMap.get(e);
                        SQLJoinOperator joinOp = (SQLJoinOperator) sqlObj;
                        SQLCondition cond = joinOp.getJoinCondition();
                        String condition = "";
                        if (cond != null) {
                            condition = cond.getConditionText();
                            if (condition == null) {
                                condition = "";
                            }
                        }
                        condition = condition.equals("") ? "<NO CONDITION DEFINED>" : condition;
                        pin.setToolTipText("<html> <table border=0 cellspacing=0 cellpadding=0>" +
                                "<tr><td>&nbsp; Join Condition</td><td> &nbsp; : &nbsp; <b>" +
                                XmlUtil.escapeHTML(condition) + "</b></td></tr></table></html>");
                    }
                }
                SQLObject sqlObj = widgetToObjectMap.get(edmWd);
                if (sqlObj instanceof SourceTableImpl) {
                    String tooltip = getSourceTableToolTip(sqlObj);
                    edmWd.setToolTipText(tooltip);
                } else if (sqlObj instanceof SQLJoinOperatorImpl) {
                    SQLJoinOperator joinOp = (SQLJoinOperator) sqlObj;
                    String joinType = "<html><table border=0 cellspacing=0 cellpadding=0>" + "<tr><td>&nbsp; Join Type</td><td> &nbsp; : &nbsp; <b>";
                    switch (joinOp.getJoinType()) {
                        case SQLConstants.INNER_JOIN:
                            joinType += "INNER JOIN";
                            break;
                        case SQLConstants.RIGHT_OUTER_JOIN:
                            joinType += "RIGHT OUTER JOIN";
                            break;
                        case SQLConstants.LEFT_OUTER_JOIN:
                            joinType += "LEFT OUTER JOIN";
                            break;
                        case SQLConstants.FULL_OUTER_JOIN:
                            joinType += "FULL OUTER JOIN";
                    }
                    joinType += "</b></td></tr></table></html>";
                    edmWd.setToolTipText(joinType);
                } else if (sqlObj instanceof SQLJoinTableImpl) {
                    SQLJoinTableImpl sqlimpl = (SQLJoinTableImpl) sqlObj;
                    String str = getSourceTableToolTip(sqlimpl.getSourceTable());
                    widget.setToolTipText(str);
                }
            }
        }
    }

    private String getSourceTableToolTip(SQLObject sqlObj) {
        SQLDBTable tbl = (SQLDBTable) sqlObj;
        StringBuilder tooltip = new StringBuilder("<html> <table border=0 cellspacing=0 cellpadding=0 >");
        boolean isUserDefinedTableName = !StringUtil.isNullString(tbl.getUserDefinedTableName());
        tooltip.append("<tr> <td>&nbsp; Table </td> <td> &nbsp; : &nbsp; <b>");
        if (isUserDefinedTableName) {
            tooltip.append("<i>").append(UIUtil.getResolvedTableName(tbl)).append("</i>");
        } else {
            tooltip.append(UIUtil.getResolvedTableName(tbl));
        }
        tooltip.append("</b> </td> </tr>");
        if (tbl.getAliasName() != null && !tbl.getAliasName().trim().equals("")) {
            tooltip.append("<tr> <td>&nbsp; Alias  </td> <td> &nbsp; : &nbsp; <b>");
            tooltip.append(tbl.getAliasName()).append("</b> </td> </tr>");
        }
        String schema = tbl.getUserDefinedSchemaName();
        final boolean isUserDefinedSchema = !StringUtil.isNullString(schema);
        if (!isUserDefinedSchema) {
            schema = tbl.getSchema();
        }
        if (!StringUtil.isNullString(schema)) {
            tooltip.append("<tr> <td>&nbsp; Schema  </td> <td> &nbsp; : &nbsp; <b>");
            if (isUserDefinedSchema) {
                tooltip.append("<i>").append(schema.trim()).append("</i>");
            } else {
                tooltip.append(schema.trim());
            }
            tooltip.append("</b> </td> </tr>");
        }
        DBConnectionDefinition conDef = tbl.getParent().getConnectionDefinition();

        String name = conDef.getName();
        if (!StringUtil.isNullString(name)) {
            tooltip.append("<tr> <td>&nbsp; ConnectionName </td> <td> &nbsp; : &nbsp; <b>");
            if (name.length() > 40) {
                tooltip.append("...").append(name.substring(name.length() - 40));
            } else {
                tooltip.append(name).append("</b> </td> </tr>");
            }
        }

        String URL = conDef.getConnectionURL();
        if (!StringUtil.isNullString(URL)) {
            tooltip.append("<tr> <td>&nbsp; ConnectionURL </td> <td> &nbsp; : &nbsp; <b>");
            if (URL.length() > 40) {
                tooltip.append("...").append(URL.substring(URL.length() - 40));
            } else {
                tooltip.append(URL).append("</b> </td> </tr>");
            }
        }
        String dbType = conDef.getDBType();
        if (!StringUtil.isNullString(dbType)) {
            tooltip.append("<tr> <td>&nbsp; DB Type </td> <td> &nbsp; : &nbsp; <b>");
            tooltip.append(conDef.getDBType()).append("</b> </td> </tr>");
        }
        String condition = ((SourceTable) tbl).getFilterCondition().getConditionText();
        if (condition != null && !condition.equals("")) {
            tooltip.append("<tr><td>&nbsp; Filter Condition</td><td>&nbsp; : &nbsp; <b>" + condition + "</b></td></tr></table></html>");
        }
        return tooltip.toString();
    }

    private final class ConditionAction extends WidgetAction.Adapter {

        private MashupDataObject mObj;
        private SQLJoinOperator joinOp;

        private ConditionAction(MashupDataObject mObj, SQLJoinOperator joinOp) {
            this.mObj = mObj;
            this.joinOp = joinOp;
        }

        @Override
        public State mouseClicked(Widget widget, WidgetMouseEvent event) {
            if (event.getClickCount() == 2) {
                if (event.getButton() == MouseEvent.BUTTON1 || event.getButton() == MouseEvent.BUTTON2) {
                    if (widget instanceof EDMPinWidget) {
                        EDMPinWidget wd = (EDMPinWidget) widget;
                        if (wd.getPinName().equals("CONDITION")) {
                            new EditJoinConditionAction(mObj, joinOp).actionPerformed(null);
                        }
                    }
                    if (widget instanceof EDMNodeWidget) {
                        EDMNodeWidget wd = (EDMNodeWidget) widget;
                        if (wd.getNodeName().equals("ROOT JOIN")) {
                            new EditJoinAction(mObj, joinOp).actionPerformed(null);
                        }
                    }
                    return State.CONSUMED;
                }
            }
            return State.REJECTED;
        }
    }
}
