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
package org.netbeans.modules.sql.framework.ui.graph.view.impl;

import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.Action;
import javax.swing.JToolBar;

import org.netbeans.modules.sql.framework.ui.graph.IGraphView;
import org.netbeans.modules.sql.framework.ui.graph.IToolBar;
import org.netbeans.modules.sql.framework.ui.graph.actions.GraphActionDelegator;
import org.netbeans.modules.sql.framework.ui.graph.actions.ZoomAction;


/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class BasicToolBar extends JToolBar implements IToolBar {
    private Object tController;
    private IGraphView graphView;
    private List customActions;
    private List graphActionDelegators = new ArrayList();

    /**
     * get the toolbar actions that need to be shown
     * 
     * @return a list of GraphAction, null in list represents a seperator
     */
    public List getActions() {
        return customActions;
    }

    /**
     * set toolbar actions on this view
     * 
     * @param actions list of GraphAction
     */
    public void setActions(List actions) {
        this.customActions = actions;
    }

    /**
     * get a action given its class
     * 
     * @param actionClass
     * @return action
     */
    public Action getAction(Class actionClass) {
        List actions = this.getActions();
        if (actions == null) {
            return null;
        }

        Iterator it = actions.iterator();

        while (it.hasNext()) {
            Action act = (Action) it.next();
            if (act != null && act.getClass().getName().equals(actionClass.getName())) {
                return act;
            }
        }

        return null;
    }

    /**
     * set the graph view for this tool bar
     * 
     * @param gView graph view
     */
    public void setGraphView(IGraphView gView) {
        this.graphView = gView;
    }

    /**
     * get the graph view for this tool bar
     * 
     * @return graph view
     */
    public IGraphView getGraphView() {
        return this.graphView;
    }

    /**
     * Sets tool bar controller
     * 
     * @param controller tool bar controller
     */
    public void setToolBarController(Object controller) {
        tController = controller;
    }

    /**
     * Gets tool bar controller
     * 
     * @return tool bar controller
     */
    public Object getToolBarController() {
        return tController;
    }

    /**
     * call this to add items in tool bar based on actions set in this toolbar
     */
    public void initializeToolBar() {
        List actions = this.getActions();
        if (actions == null) {
            return;
        }

        Iterator it = actions.iterator();

        while (it.hasNext()) {
            Action action = (Action) it.next();
            GraphActionDelegator gaDelegator = new GraphActionDelegator(this.getGraphView(), action);
            if (action != null) {
                if (action instanceof ZoomAction) {
                    Component comp = ((ZoomAction) action).getComponent();
                    if (comp != null) {
                        this.add(comp);
                    }
                } else {
                    this.add(gaDelegator);
                    graphActionDelegators.add(gaDelegator);
                }
            } else {
                this.addSeparator(new Dimension(1, 0));
                this.addSeparator(new Dimension(2, 30));
                this.addSeparator(new Dimension(1, 0));
            }
        }
        this.setRollover(true);        
    }

    public void enableToolBar(boolean enable) {
        this.setEnabled(enable);

        Iterator it = this.graphActionDelegators.iterator();

        while (it.hasNext()) {
            Action action = (Action) it.next();
            if (action != null) {
                action.setEnabled(enable);
            }
        }
    }
}

