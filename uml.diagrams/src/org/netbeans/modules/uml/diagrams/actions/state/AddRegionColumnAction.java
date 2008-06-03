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

import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IRegion;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.State;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.TypedFactoryRetriever;
import org.netbeans.modules.uml.diagrams.nodes.state.CompositeStateWidget;
import org.netbeans.modules.uml.diagrams.nodes.state.RegionWidget;
import org.netbeans.modules.uml.diagrams.nodes.state.StateWidget;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Sheryl Su
 */
public class AddRegionColumnAction extends NodeAction
{

    @Override
    protected void performAction(Node[] activatedNodes)
    {
        CompositeStateWidget w = getCompositeStateWidget(activatedNodes[0]);
        if (w != null)
        {
            IRegion region = new TypedFactoryRetriever<IRegion>().createType("Region");
            State state = w.getElement();
            state.addContent(region);
            setLayout(w);
            w.addRegion(region);
        }
    }

    protected void setLayout(CompositeStateWidget w)
    {
        w.setHorizontalLayout(true);
    }
    
    
    @Override
    protected boolean enable(Node[] activatedNodes)
    {
        if (activatedNodes.length == 1)
        {
            CompositeStateWidget w = getCompositeStateWidget(activatedNodes[0]);

            if (w != null)
            {               
                return ((CompositeStateWidget)w).getRegionWidgets().size() == 1 || 
                        ((CompositeStateWidget)w).isHorizontalLayout();
            }
        }
        return false;
    }

    protected CompositeStateWidget getCompositeStateWidget(Node node)
    {
        IPresentationElement pe = node.getLookup().lookup(IPresentationElement.class);
        DesignerScene scene = node.getLookup().lookup(DesignerScene.class);
        if (scene == null || pe == null)
            return null;
        
        Widget w = scene.findWidget(pe);
        if (w instanceof RegionWidget)
        {
            return ((RegionWidget) w).getCompositeStateWidget();
        } else if (w instanceof StateWidget)
        {
            return ((StateWidget) w).getCompositeStateWidget();
        }
        return null;
    }

    @Override
    public String getName()
    {
        return loc("CTL_AddRegionColumn");
    }

    @Override
    public HelpCtx getHelpCtx()
    {
        return HelpCtx.DEFAULT_HELP;
    }

    protected String loc(String key)
    {
        return NbBundle.getMessage(AddRegionColumnAction.class, key);
    }
}
