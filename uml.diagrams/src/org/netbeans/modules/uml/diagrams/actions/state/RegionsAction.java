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
package org.netbeans.modules.uml.diagrams.actions.state;

import java.awt.event.ActionEvent;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.api.visual.widget.SeparatorWidget.Orientation;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IRegion;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IState;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.State;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
import org.netbeans.modules.uml.diagrams.actions.DeleteCompartmentWidgetAction;
import org.netbeans.modules.uml.diagrams.nodes.CompartmentWidget;
import org.netbeans.modules.uml.drawingarea.actions.SceneNodeAction;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.diagrams.nodes.state.CompositeStateWidget;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Sheryl Su
 */
public class RegionsAction extends SceneNodeAction implements ContextAwareAction
{

    private DesignerScene scene;
    private IPresentationElement pe;
    private IState state;

    public Action createContextAwareInstance(Lookup actionContext)
    {
        scene = actionContext.lookup(DesignerScene.class);
        pe = actionContext.lookup(IPresentationElement.class);
        IElement e = pe.getFirstSubject();
        if (e instanceof IState)
        {
            state = (IState) e;
            if (state.getIsComposite())
            {
                return this;
            }
        }
        return null;
    }

    @Override
    protected void performAction(Node[] activatedNodes)
    {
        
    }

    @Override
    public String getName()
    {
        return loc("CTL_Regions");
    }

    @Override
    public HelpCtx getHelpCtx()
    {
        return HelpCtx.DEFAULT_HELP;
    }

    private String loc(String name)
    {
        return NbBundle.getMessage(RegionsAction.class, name);
    }

    @Override
    public JMenuItem getPopupPresenter()
    {
        JMenu popupMenu = new JMenu(loc("CTL_Regions")); // NOI18N
        popupMenu.setEnabled(scene.isReadOnly() == false);

        CompositeStateWidget widget = getCompositeStateWidget();
        if ((widget.getCompartmentWidgets().size() == 1))
        {
            popupMenu.add(new AddColumnAction(loc("CTL_AddRegionColumn")));
            popupMenu.add(new AddRowAction(loc("CTL_AddRegionRow")));
        }
        else 
            popupMenu.add(widget.getOrientation() == Orientation.HORIZONTAL ? 
                new AddColumnAction(loc("CTL_AddRegionColumn")) : new AddRowAction(loc("CTL_AddRegionRow")));
        
        for (CompartmentWidget w : widget.getCompartmentWidgets())
        {
            if (w.isSelected())
            {
                String name = widget.getOrientation() == Orientation.HORIZONTAL? 
                    loc("CTL_DeleteRegionColumn") : loc("CTL_DeleteRegionRow");
                popupMenu.add(new DeleteCompartmentWidgetAction(w, name));
                break;
            }
        }
        return popupMenu;
    }

    private CompositeStateWidget getCompositeStateWidget()
    {
        Set selected = scene.getSelectedObjects();
        if (selected.isEmpty())
        {
            return null;
        }
        Object[] objs = new Object[selected.size()];
        selected.toArray(objs);
        Widget widget = scene.findWidget(objs[0]);
        if (widget instanceof CompositeStateWidget)
        {
            return (CompositeStateWidget) widget;
        }
        return null;
    }

    private class AddColumnAction extends AbstractAction
    {

        public AddColumnAction(String name)
        {
            super(name);
        }

        public void actionPerformed(ActionEvent e)
        {
            CompositeStateWidget w = getCompositeStateWidget();
            if (w != null)
            {
                IRegion region = new TypedFactoryRetriever<IRegion>().createType("Region");
                State state = w.getElement();
                state.addContent(region);
                setLayout(w);
                w.addCompartment(region);
            }
        }

        protected void setLayout(CompositeStateWidget w)
        {
            w.setOrientation(Orientation.HORIZONTAL);
        }
    }

    private class AddRowAction extends AddColumnAction
    {

        public AddRowAction(String name)
        {
            super(name);
        }

        protected void setLayout(CompositeStateWidget w)
        {
            w.setOrientation(Orientation.VERTICAL);
        }
    }
}

