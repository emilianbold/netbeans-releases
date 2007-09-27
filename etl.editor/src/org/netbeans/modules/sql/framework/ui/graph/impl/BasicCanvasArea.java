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

package org.netbeans.modules.sql.framework.ui.graph.impl;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Iterator;
import java.util.List;

import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.netbeans.modules.sql.framework.model.GUIInfo;
import org.netbeans.modules.sql.framework.model.SQLCanvasObject;
import org.netbeans.modules.sql.framework.ui.graph.IGraphNode;
import org.netbeans.modules.sql.framework.ui.graph.IGraphPort;
import org.netbeans.modules.sql.framework.ui.graph.IGraphView;

import com.nwoods.jgo.JGoView;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class BasicCanvasArea extends CanvasArea implements IGraphNode {

    protected TitleArea titleArea;
    protected Dimension expandedSize;
    protected Object dataObject = null;
    protected IGraphView view;
    protected JPopupMenu popUpMenu;

    private boolean updateGuiInfo = true;

    /** Creates a new instance of BasicCanvasArea */
    public BasicCanvasArea() {
        super();
        expandedSize = new Dimension(-1, -1);
        titleArea = new TitleArea("dummy");
    }

    /**
     * Expand this graph node
     * 
     * @param expand whether to expand or collapse this node
     */
    public void expand(boolean expand) {
        this.setExpandedState(expand);
    }

    /**
     * set the expansion state of the graph node
     * 
     * @param expand expansion state
     */
    public void setExpandedState(boolean expand) {
        if (expand) {
            titleArea.setState(TitleArea.EXPANDED);
        } else {
            titleArea.setState(TitleArea.COLLAPSED);
        }
    }

    /**
     * get the expanded state
     * 
     * @return expanded state
     */
    public boolean isExpandedState() {
        if (titleArea.getState() == TitleArea.EXPANDED) {
            return true;
        }

        return false;
    }

    /**
     * get a list of all input and output links
     * 
     * @return list of input links
     */
    public List getAllLinks() {
        return null;
    }

    /**
     * get the child graphNode
     * 
     * @param obj child data object
     * @return graph node
     */
    public IGraphNode getChildNode(Object obj) {
        return null;
    }

    /**
     * get the data object associated with graph node
     * 
     * @return data object
     */
    public Object getDataObject() {
        return dataObject;
    }

    /**
     * get field name given a port
     * 
     * @param graphPort graph port
     * @return field name
     */
    public String getFieldName(IGraphPort graphPort) {
        return null;
    }

    /**
     * get input graph port , given a field name
     * 
     * @param fieldName field name
     * @return graph port
     */
    public IGraphPort getInputGraphPort(String fieldName) {
        return null;
    }

    /**
     * get output graph port , given a field name
     * 
     * @param fieldName field name
     * @return graph port
     */
    public IGraphPort getOutputGraphPort(String fieldName) {
        return null;
    }

    /**
     * Get the parent node
     * 
     * @return parent
     */
    public IGraphNode getParentGraphNode() {
        return null;
    }

    /**
     * Remove a child object
     * 
     * @param child child object
     */
    public void removeChildNode(IGraphNode childNode) throws Exception {
    }

    /**
     * set data object in this graph node
     * 
     * @param obj data object
     */
    public void setDataObject(Object obj) {
        this.dataObject = obj;
    }

    /**
     * Sets whether this area is expanded. called by update method may be overriden by
     * subclass
     * 
     * @param isExpanded whether area is expanded
     */
    protected void setExpanded(boolean isExpanded) {
        this.setResizable(isExpanded);

        if (isExpanded) {
            this.setSize(expandedSize);
        } else {
            expandedSize = this.getSize();
            this.setSize(getMinimumWidth(), getMinimumHeight());
        }

        SQLCanvasObject canvasObject = (SQLCanvasObject) this.getDataObject();
        if (canvasObject != null && updateGuiInfo) {
            GUIInfo guiInfo = canvasObject.getGUIInfo();
            if (guiInfo != null) {
                guiInfo.setExpanded(this.isExpandedState());
            }
        }
    }

    /**
     * handle geometry change
     * 
     * @param prevRect previous bounds rectangle
     */
    protected void geometryChange(Rectangle prevRect) {
        super.geometryChange(prevRect);
        Rectangle rect = this.getBoundingRect();
        SQLCanvasObject canvasObject = (SQLCanvasObject) this.getDataObject();
        if (canvasObject != null && updateGuiInfo) {
            GUIInfo guiInfo = canvasObject.getGUIInfo();
            if (guiInfo != null && guiInfo.isVisible()) {
                guiInfo.setX(rect.x);
                guiInfo.setY(rect.y);
                if (this.isExpandedState()) {
                    guiInfo.setWidth(rect.width);
                    guiInfo.setHeight(rect.height);
                }
            }
        }
    }

    /**
     * Updates contents based on notification from the TableTitleArea when its
     * expand/collapse control is clicked.
     * 
     * @param hint event hint
     * @param prevInt previous integer value
     * @param prevVal previous object val
     */
    public void update(int hint, int prevInt, Object prevVal) {
        if (hint == TitleArea.EXPANSION_STATE_CHANGED) {
            // optimization: assume area doesn't change when scrolling items
            if (titleArea.getState() == TitleArea.EXPANDED) {
                setExpanded(true);
            } else {
                setExpanded(false);
            }
        } else {
            super.update(hint, prevInt, prevVal);
        }
    }

    /**
     * set the graph view whixh hold this node
     * 
     * @param view graph view
     */
    public void setGraphView(IGraphView view) {
        this.view = view;
    }

    /**
     * get the graphview which holds this view
     * 
     * @return graph view
     */
    public IGraphView getGraphView() {
        return view;
    }

    /**
     * update this node with changes in data object
     */
    public void updateUI() {
    }

    /**
     * remove the child data object
     * 
     * @param obj child data object
     */
    public void removeChildObject(Object obj) {
    }

    /**
     * add child data object
     * 
     * @param obj child data object
     */
    public void addChildObject(Object obj) {
    }

    public boolean doMouseClick(int modifiers, Point dc, Point vc, JGoView view1) {

        int onmask = java.awt.event.InputEvent.BUTTON3_MASK;

        if ((modifiers & onmask) != 0 && popUpMenu != null) {
            popUpMenu.show(view1, vc.x, vc.y);
            return true;
        }

        return false;
    }

    /**
     * set the actions on a node
     * 
     * @param actions
     */
    public void initializeActions(List actions) {
        popUpMenu = new JPopupMenu();
        Iterator it = actions.iterator();

        while (it.hasNext()) {
            Action action = (Action) it.next();
            JMenuItem item = new JMenuItem(action);
            popUpMenu.addSeparator();
            popUpMenu.add(item);
        }
    }

    public void setUpdateGuiInfo(boolean update) {
        this.updateGuiInfo = update;
    }

    /**
     * is this node can be deleted
     * 
     * @return true if node can be deleted
     */
    public boolean isDeleteAllowed() {
        return true;
    }
}

