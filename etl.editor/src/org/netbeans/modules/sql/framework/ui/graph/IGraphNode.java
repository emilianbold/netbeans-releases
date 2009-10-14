/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.sql.framework.ui.graph;

import java.util.List;

/**
 * @author radval
 */
public interface IGraphNode extends IDataNode {
    /**
     * get field name given a port
     * 
     * @param graphPort graph port
     * @return field name
     */
    String getFieldName(IGraphPort graphPort);

    /**
     * get output graph port , given a field name
     * 
     * @param fieldName field name
     * @return graph port
     */
    IGraphPort getOutputGraphPort(String fieldName);

    /**
     * get input graph port , given a field name
     * 
     * @param fieldName field name
     * @return graph port
     */
    IGraphPort getInputGraphPort(String fieldName);

    /**
     * get a list of all input and output links
     * 
     * @return list of input links
     */
    List getAllLinks();

    /**
     * Expand this graph node
     * 
     * @param expand whether to expand or collapse this node
     */
    public void expand(boolean expand);

    /**
     * get the child graphNode
     * 
     * @param obj child data object
     * @return graph node
     */
    public IGraphNode getChildNode(Object obj);

    /**
     * Remove a child object
     * 
     * @param child child object
     */
    public void removeChildNode(IGraphNode childNode) throws Exception;

    /**
     * Get the parent node
     * 
     * @return parent
     */
    public IGraphNode getParentGraphNode();

    /**
     * set the expansion state of the graph node
     * 
     * @param expand expansion state
     */
    public void setExpandedState(boolean expand);

    /**
     * get the expanded state
     * 
     * @return expanded state
     */
    public boolean isExpandedState();

    /**
     * set the graph view whixh hold this node
     * 
     * @param view graph view
     */
    public void setGraphView(IGraphView view);

    /**
     * get the graphview which holds this view
     * 
     * @return graph view
     */
    public IGraphView getGraphView();

    /**
     * update this node with changes in data object
     */
    public void updateUI();

    /**
     * remove the child data object
     * 
     * @param obj child data object
     */
    public void removeChildObject(Object obj);

    /**
     * add child data object
     * 
     * @param obj child data object
     */
    public void addChildObject(Object obj);

    /**
     * set the actions on a node
     * 
     * @param actions
     */
    public void initializeActions(List actions);

    /**
     * is this node can be deleted
     * 
     * @return true if node can be deleted
     */
    public boolean isDeleteAllowed();

}

