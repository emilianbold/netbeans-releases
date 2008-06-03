/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.uml.diagrams.actions.activity;

import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.event.ChangeListener;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.diagrams.nodes.activity.ActivityPartitionWidget;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.openide.awt.Actions;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

public final class PartitionActions extends NodeAction
{
    private static final long serialVersionUID = 1L;

    @Override
    public JMenuItem getPopupPresenter()
    {
        JMenuItem item = new Actions.SubMenu(this,
                new PartitionSubMenuModel(), true);
        return item;
    }

    public String getName()
    {
        return NbBundle.getMessage(PartitionActions.class,
                                   "CTL_AddPartitionAction");
    }

    public HelpCtx getHelpCtx()
    {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous()
    {
        return false;
    }

    @Override
    protected boolean enable(Node[] activatedNodes)
    {
        boolean retval = false;
        if (activatedNodes.length == 1)
        {
            ActivityPartitionWidget widget = getPartitionWidget(activatedNodes[0]);
            if (widget != null)
            {
                retval = true;
            }
        }
        return retval;
    }

    protected void performAction(Node[] activatedNodes)
    {
    }

    protected ActivityPartitionWidget getPartitionWidget(Node node)
    {
        IPresentationElement pe = node.getLookup().lookup(IPresentationElement.class);
        DesignerScene scene = node.getLookup().lookup(DesignerScene.class);

        if (scene != null && pe != null)
        {
            Widget widget = scene.findWidget(pe);
            if (widget instanceof ActivityPartitionWidget)
            {
                return (ActivityPartitionWidget) widget;
            }
        }
        return null;
    }

    public Action[] getPartitionActions()
    {
        Action[] actionArray = new Action[2];
        //Node[] nodes = WindowManager.getDefault().getRegistry().getCurrentNodes(); 
        Node[] activatedNodes = this.getActivatedNodes();
        ActivityPartitionWidget widget = getPartitionWidget(activatedNodes[0]);

        if (widget != null)
        {
            if (widget.getSubPartitionCount() == 0)
            {
                actionArray[0] = new AddPartitionColumnAction(widget);
                actionArray[1] = new AddPartitionRowAction(widget);
            } else if (widget.hasRowPartition())
            {
                actionArray[0] = new AddPartitionRowAction(widget);
                actionArray[1] = new DeletePartitionAction(widget);
            } else // column partition
            {
                actionArray[0] = new AddPartitionColumnAction(widget);
                actionArray[1] = new DeletePartitionAction(widget);
            }
        }

        return actionArray;
    }

    private class PartitionSubMenuModel implements Actions.SubMenuModel
    {

        private Action[] actions = null;

        public int getCount()
        {
            int retVal = 0;
            Action[] actionList = getActions();
            if (actionList != null)
            {
                retVal = actionList.length;
            }
            return retVal;
        }

        public String getLabel(int index)
        {
            String retVal = null;

            Action[] actionList = getActions();
            if (actionList != null && actionList.length > index)
            {
                if (actionList[index] instanceof IPartitionAction)
                {
                    retVal = ((IPartitionAction) actionList[index]).getName();
                }
            }
            return retVal;
        }

        public HelpCtx getHelpCtx(int index)
        {
            HelpCtx helpCtx = null;
            Action[] actionList = getActions();
            if (actionList != null && actionList.length > index)
            {
                if (actionList[index] instanceof IPartitionAction)
                {
                    helpCtx = ((IPartitionAction) actionList[index]).getHelpCtx();
                }
            }
            return helpCtx;
        }

        public void performActionAt(int index)
        {
            Action[] actionList = getActions();
            if (actionList != null && actionList.length > index)
            {
                Action targetAction = actionList[index];
                if (targetAction instanceof IPartitionAction)
                {
                    ((IPartitionAction) targetAction).handlePartitionAction();
                }
            }
        }

        public void addChangeListener(ChangeListener l)
        {
        //throw new UnsupportedOperationException("Not supported yet.");
        }

        public void removeChangeListener(ChangeListener l)
        {
        //throw new UnsupportedOperationException("Not supported yet.");
        }

        protected Action[] getActions()
        {
            if (actions == null)
            {
                actions = getPartitionActions();
            }
            return actions;
        }
    }  // End PartitionSubMenuModel
}

