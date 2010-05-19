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
package org.netbeans.modules.edm.editor.graph;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.swing.JOptionPane;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;

import com.nwoods.jgo.JGoObject;
import org.netbeans.modules.edm.model.EDMException;
import org.netbeans.modules.edm.model.GUIInfo;
import org.netbeans.modules.edm.model.SQLCanvasObject;
import org.netbeans.modules.edm.model.SQLConnectableObject;
import org.netbeans.modules.edm.model.SQLDBTable;
import org.netbeans.modules.edm.model.SQLDefinition;
import org.netbeans.modules.edm.model.SQLObject;
import org.netbeans.modules.edm.model.SQLOperator;
import org.netbeans.modules.edm.editor.ui.event.SQLDataEvent;
import org.netbeans.modules.edm.editor.ui.event.SQLDataListener;
import org.netbeans.modules.edm.editor.ui.event.SQLLinkEvent;
import org.netbeans.modules.edm.editor.graph.jgo.IGraphLink;
import org.netbeans.modules.edm.editor.graph.jgo.IGraphNode;
import org.netbeans.modules.edm.editor.graph.jgo.IGraphPort;
import org.netbeans.modules.edm.editor.graph.jgo.IOperatorXmlInfo;
import org.netbeans.modules.edm.editor.graph.jgo.GraphView;
import org.netbeans.modules.edm.editor.ui.model.CollabSQLUIModel;
import org.netbeans.modules.edm.editor.ui.model.SQLUIModel;
import org.netbeans.modules.edm.editor.ui.view.IGraphViewContainer;
import org.netbeans.modules.edm.editor.ui.view.join.JoinPreviewGraphNode;
import org.openide.util.NbBundle;


/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class SQLGraphView extends GraphView implements SQLDataListener, UndoableEditListener {

    private static ClipBoard clipBoard = new ClipBoard();
    private static final String LOG_CATEGORY = SQLGraphView.class.getName();
    private static transient final Logger mLogger = Logger.getLogger(SQLGraphView.class.getName());
    Color pColor;
    Color sColor;

    /** Creates a new instance of SQLGraphView */
    public SQLGraphView() {
        super();
        setDropEnabled(true);
    }

    /**
     * perform the layout
     */
    public void performLayout() {
    }

    /**
     * find the link selected by the user
     *
     * @param srcObj link which is connected to a node which hold srcObj as data object
     * @param destObj link which is connected to a node which hold destObj as data object
     * @return -
     */
    public IGraphLink findLink(Object srcObj, String srcParam, Object destObj, String destParam) {

        Collection linkCol = getAllGraphLinks();
        Iterator it = linkCol.iterator();

        while (it.hasNext()) {
            IGraphLink link = (IGraphLink) it.next();

            IGraphPort from = link.getFromGraphPort();
            IGraphPort to = link.getToGraphPort();

            IGraphNode srcGraphNode = from.getDataNode();
            IGraphNode destGraphNode = to.getDataNode();

            IGraphPort linkSrcPort = srcGraphNode.getOutputGraphPort(srcParam);
            IGraphPort linkDestPort = destGraphNode.getInputGraphPort(destParam);

            Object linkSrcObj = srcGraphNode.getDataObject();
            Object linkDestObj = destGraphNode.getDataObject();

            if (linkSrcObj == srcObj && linkDestObj == destObj && linkSrcPort == from && linkDestPort == to) {
                return link;
            }
        }

        return null;
    }

    protected void createGraphNode(SQLDataEvent event) throws EDMException {
        SQLCanvasObject canvasObj = event.getCanvasObject();

        GUIInfo gInfo = canvasObj.getGUIInfo();
        if (gInfo != null && !gInfo.isVisible()) {
            return;
        }

        IGraphNode canvasNode = this.findGraphNode(canvasObj);
        //if graph node already exists then simply return
        if (canvasNode != null) {
            return;
        }

        AbstractGraphFactory factory = (AbstractGraphFactory) this.getGraphFactory();
        if (factory == null) {
            return;
        }

        canvasNode = factory.createGraphNode(canvasObj);

        if (canvasNode != null) {
            this.addNode(canvasNode);
            //Per QAI 94710 - disable join type combo box
            //There should be a better way to do it, such as adding a method to
            //IGraphNode interface.
            if (canvasNode instanceof JoinPreviewGraphNode) {
                ((JoinPreviewGraphNode) canvasNode).setModifiable(this.getDocument().isModifiable());
            }
        }

        //      now check if we have a java operator
        if (canvasObj instanceof SQLOperator) {
            SQLOperator operator = (SQLOperator) canvasObj;
            IOperatorXmlInfo opXmlInfo = operator.getOperatorXmlInfo();
            if (opXmlInfo == null) {
                throw new IllegalArgumentException(NbBundle.getMessage(SQLGraphView.class, "ERROR_xml_info_is_null."));
            }

            if (opXmlInfo.isJavaOperator()) {
                SQLUIModel model = (SQLUIModel) this.getGraphModel();
                model.addJavaOperator(operator);
            }
        }
    }

    private void deleteGraphNode(SQLDataEvent event) {
        SQLCanvasObject canvasObj = event.getCanvasObject();

        IGraphNode canvasNode = this.findGraphNode(canvasObj);
        if (canvasNode != null) {
            removeNode(canvasNode);
        }

        //      now check if we have a java operator
        if (canvasObj instanceof SQLOperator) {
            SQLOperator operator = (SQLOperator) canvasObj;
            IOperatorXmlInfo opXmlInfo = operator.getOperatorXmlInfo();
            if (opXmlInfo == null) {
                throw new IllegalArgumentException(NbBundle.getMessage(SQLGraphView.class, "ERROR_xml_info_is_null."));
            }

            if (opXmlInfo.isJavaOperator()) {
                SQLUIModel model = (SQLUIModel) this.getGraphModel();
                model.removeJavaOperator(operator);
            }
        }
    }

    public void objectCreated(SQLDataEvent evt) throws EDMException {
        createGraphNode(evt);
    }

    public void objectDeleted(SQLDataEvent evt) {
        deleteGraphNode(evt);
    }

    public void linkCreated(SQLLinkEvent evt) {
        SQLCanvasObject srcObj = evt.getSourceCanvasObject();
        SQLConnectableObject expObj = evt.getTargetCanvasObject();
        String srcParam = evt.getSourceFieldName();
        String destParam = evt.getDestinationFieldName();

        createLink(srcObj, expObj, srcParam, destParam);
    }

    public void createLink(SQLCanvasObject srcObj, SQLConnectableObject expObj, String srcParam, String destParam) {

        //if link already exist then simply return
        IGraphLink link = findLink(srcObj, srcParam, expObj, destParam);
        if (link != null) {
            return;
        }

        SQLCanvasObject destObj = getTopSQLCanvasObject(expObj);

        IGraphNode srcNode = findGraphNode(srcObj);
        IGraphNode destNode = findGraphNode(destObj);

        //if top object is differnt than expObj then we need to get child GraphNode
        if (destObj != expObj) {
            destNode = destNode.getChildNode(expObj);
        }

        if (srcNode == null || destNode == null) {
            return;
        }

        IGraphPort srcPort = srcNode.getOutputGraphPort(srcParam);

        IGraphPort destPort = destNode.getInputGraphPort(destParam);

        if (srcPort == null || destPort == null) {
            return;
        }

        link = new SQLGraphLink(srcPort, destPort);

        //add link
        this.getDocument().addObjectAtTail((JGoObject) link);
        performLayout();
    }

    public void linkDeleted(SQLLinkEvent evt) {
        SQLCanvasObject srcObj = evt.getSourceCanvasObject();
        SQLConnectableObject destObj = evt.getTargetCanvasObject();
        String srcParam = evt.getSourceFieldName();
        String destParam = evt.getDestinationFieldName();

        IGraphLink link = findLink(srcObj, srcParam, destObj, destParam);
        if (link != null) {
            this.getDocument().removeObject((JGoObject) link);
            performLayout();
        }
    }

    private SQLCanvasObject getTopSQLCanvasObject(SQLObject sqlObj) {
        if (sqlObj instanceof SQLCanvasObject) {
            return (SQLCanvasObject) sqlObj;
        }

        Object parentObj = sqlObj.getParentObject();
        while (parentObj != null && parentObj instanceof SQLObject && !(parentObj instanceof SQLCanvasObject)) {
            parentObj = ((SQLObject) parentObj).getParentObject();
        }

        if (parentObj instanceof SQLCanvasObject) {
            return (SQLCanvasObject) parentObj;
        }

        return null;
    }

    public boolean doMouseUp(int modifiers, java.awt.Point dc, java.awt.Point vc) {

        boolean mClick = super.doMouseUp(modifiers, dc, vc);

        if (this.pickDocObject(dc, false) != null) {
            return mClick;
        }

        int onmask = java.awt.event.InputEvent.BUTTON3_MASK;

        if ((modifiers & onmask) != 0 && popUpMenu != null) {
            //if element is not checked out then ask user to check it out before
            // modifiying it

            if (!((IGraphViewContainer) this.getGraphViewContainer()).canEdit()) {
                return false;
            }

            if (popUpMenu != null) {
                popUpMenu.show(this, vc.x, vc.y);
                return true;
            }
        }

        return false;
    }

    public void reset() {
        resetSelectionColors();
    }

    public void childObjectCreated(SQLDataEvent evt) {
        SQLCanvasObject canvasObj = evt.getCanvasObject();
        IGraphNode node = this.findGraphNode(canvasObj);
        SQLObject chldObj = evt.getChildObject();
        if (node != null && chldObj != null) {
            node.removeChildObject(chldObj);
        }
    }

    public void childObjectDeleted(SQLDataEvent evt) {
        SQLCanvasObject canvasObj = evt.getCanvasObject();
        IGraphNode node = this.findGraphNode(canvasObj);
        SQLObject chldObj = evt.getChildObject();
        if (node != null && chldObj != null) {
            node.addChildObject(chldObj);
        }
    }

    public void objectUpdated(SQLDataEvent evt) {
        SQLCanvasObject canvasObj = evt.getCanvasObject();
        IGraphNode node = this.findGraphNode(canvasObj);

        if (node != null) {
            node.updateUI();
        }
    }

    /**
     * Handles key event
     *
     * @param evt Description of the Parameter
     */
    public void onKeyEvent(KeyEvent evt) {
        int t = evt.getKeyCode();

        if (t == KeyEvent.VK_P && evt.isControlDown()) {
            printDoc();
        } else if (t == KeyEvent.VK_C && evt.isControlDown()) {
            copyToClipboard();
        } else if (t == KeyEvent.VK_V && evt.isControlDown()) {
            pasteFromClipboard();
        } else {
            super.onKeyEvent(evt);
        }
    }

    private void printDoc() {
    }

    private void copyToClipboard() {
        clipBoard.setNodes(this.getSelectedNodes());
    }

    private void pasteFromClipboard() {
        Collection nodes = clipBoard.getNodes();
        if (nodes != null) {
            Map nodeMap = pasteNodes(nodes);
            pasteLinks(nodeMap);
        }
    }

    private Map pasteNodes(Collection nodes) {
        Rectangle bounds = computeBounds(nodes);
        Map nodeMap = new HashMap();
        Iterator it = nodes.iterator();
        while (it.hasNext()) {
            IGraphNode node = (IGraphNode) it.next();
            Object dataObj = node.getDataObject();
            if (dataObj instanceof SQLCanvasObject) {
                try {
                    SQLCanvasObject obj = (SQLCanvasObject) dataObj;
                    SQLCanvasObject clonedObj = (SQLCanvasObject) obj.cloneSQLObject();
                    clonedObj.reset();
                    GUIInfo guiInfo = clonedObj.getGUIInfo();
                    if (mousePoint != null) {
                        int x = guiInfo.getX();
                        int y = guiInfo.getY();
                        guiInfo.setX(mousePoint.x + x - (int) bounds.getX());
                        guiInfo.setY(mousePoint.y + y - (int) bounds.getY());
                    }

                    SQLUIModel sqlModel = (SQLUIModel) this.getGraphController().getDataModel();
                    //sqlModel.addObject(clonedObj);

                    if (clonedObj instanceof SQLDBTable) {
                        SQLDefinition sqlDef = ((CollabSQLUIModel) sqlModel).getSQLDefinition();
                        SQLDBTable dbTable = (SQLDBTable) clonedObj;
                        Object existingObj = sqlDef.isTableExists(dbTable);
                        if (existingObj != null) {
                            int retVal = JOptionPane.showConfirmDialog(this, dbTable.getDisplayName() + NbBundle.getMessage(SQLGraphView.class, "MSG_already_exists"),
                                    "Confirm", JOptionPane.INFORMATION_MESSAGE);
                            if (retVal == JOptionPane.YES_OPTION) {
                                sqlModel.removeObject((SQLObject) existingObj);
                                sqlModel.addObject(clonedObj);
                            } else {
                                continue;
                            }
                        } else {
                            sqlModel.addObject(clonedObj);
                        }
                    } else {
                        sqlModel.addObject(clonedObj);
                    }
                    nodeMap.put(node, clonedObj);
                } catch (Exception ex) {
                    mLogger.log(Level.INFO,"Exception"+ex.getMessage(),ex);
                }
            }
        }
        return nodeMap;
    }

    private void pasteLinks(Map nodeMap) {
        Collection srcNodes = nodeMap.keySet();
        //ArrayList relatedLinks = new ArrayList();
        Iterator it = srcNodes.iterator();
        while (it.hasNext()) {
            IGraphNode node = (IGraphNode) it.next();
            Collection linksOfNode = node.getAllLinks();
            Iterator it2 = linksOfNode.iterator();
            while (it2.hasNext()) {
                IGraphLink link = (IGraphLink) it2.next();
                IGraphPort toPort = link.getToGraphPort();
                IGraphNode toNode = toPort.getDataNode();
                if (toNode != node && srcNodes.contains(toNode)) {
                    //copy the link
                    mLogger.log(Level.INFO,NbBundle.getMessage(SQLGraphView.class, "LOG.INFO_Find_a_link",new Object[] {node, toNode}));
                    Object clonedSrcObj = nodeMap.get(node);
                    Object clonedDestObj = nodeMap.get(toNode);
                    if (clonedSrcObj != null && clonedDestObj != null) {
                        try {
                            String srcFieldName = node.getFieldName(link.getFromGraphPort());
                            String destFieldName = toNode.getFieldName(toPort);
                            SQLUIModel sqlModel = (SQLUIModel) this.getGraphController().getDataModel();
                            SQLConnectableObject exprObj = (SQLConnectableObject) clonedDestObj;
                            sqlModel.createLink((SQLCanvasObject) clonedSrcObj, srcFieldName, exprObj, destFieldName);
                        } catch (Exception ex) {
                            mLogger.log(Level.INFO,"Exception"+ex.getMessage(),ex);
                        }
                    }
                }
            }
        }
    }

    private Rectangle computeBounds(Collection nodes) {
        int minx = Integer.MAX_VALUE;
        int miny = Integer.MAX_VALUE;
        int maxx = 0;
        int maxy = 0;
        Iterator it = nodes.iterator();
        while (it.hasNext()) {
            IGraphNode node = (IGraphNode) it.next();
            Object dataObj = node.getDataObject();
            if (dataObj instanceof SQLCanvasObject) {
                SQLCanvasObject obj = (SQLCanvasObject) dataObj;
                GUIInfo guiInfo = obj.getGUIInfo();
                int x = guiInfo.getX();
                int y = guiInfo.getY();
                minx = Math.min(x, minx);
                miny = Math.min(y, miny);
                maxx = Math.max(x, maxx);
                maxy = Math.max(y, maxy);
            }
        }
        return new Rectangle(minx, miny, (maxx - minx), (maxy - miny));
    }

    private static class ClipBoard {

        private Collection nodes;
        private Collection links;

        /**
         * Copy nodes to this clipboard
         *
         * @param nodes - nodes
         */
        public void setNodes(Collection nodes) {
            this.nodes = nodes;
        }

        /**
         * Copy links to this clipboard
         *
         * @param links - links
         */
        public void setLinks(Collection links) {
            this.links = links;
        }

        /**
         * Return nodes in this clipboard
         *
         * @return nodes
         */
        public Collection getNodes() {
            return nodes;
        }

        /**
         * Return links in this clipboard
         *
         * @return links
         */
        public Collection getLinks() {
            return links;
        }
    }

    /**
     * can this graph be edited
     *
     * @return true if graph is edited
     */
    public boolean canEdit() {
        return ((IGraphViewContainer) this.getGraphViewContainer()).canEdit();
    }

    public void undoableEditHappened(UndoableEditEvent e) {
    }

    /**
     * Execute a command
     *
     * @param command - command
     * @param args - arguments
     */
    public void execute(String command, Object[] args) {
        if (command == null) {
            return;
        }
        IGraphViewContainer viewContainer = (IGraphViewContainer) this.getGraphViewContainer();
        viewContainer.execute(command, args);
    }
}

