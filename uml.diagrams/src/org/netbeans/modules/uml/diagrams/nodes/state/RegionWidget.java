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
package org.netbeans.modules.uml.diagrams.nodes.state;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IRegion;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.diagrams.nodes.CompartmentWidget;
import org.netbeans.modules.uml.drawingarea.util.Util;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;
import org.netbeans.modules.uml.drawingarea.view.UMLWidget;
import org.netbeans.modules.uml.drawingarea.widgets.ContainerWidget;

/**
 *
 * @author Sheryl Su
 */
public class RegionWidget extends CompartmentWidget
{
    private ContainerWidget stateContainerWidget;
 
    
    public RegionWidget(Scene scene, IRegion region, CompositeStateWidget compositeStateWidget)
    {
        super(scene, region, compositeStateWidget);       
    }

    public String getWidgetID()
    {
        return UMLWidget.UMLWidgetIDString.STATEWIDGET.toString();
    }

    
    public void initContainedElements()
    {
        if (!(getScene() instanceof GraphScene))
        {
            return;
        }
 
        Point point = new Point(10,10);
        for (IElement element : getElement().getElements())
        {
            if (element instanceof ITransition)
                continue;
            
            boolean found = false;
            List<Widget> list = getContainerWidget().getChildren();
            List<Widget> children = new ArrayList<Widget>(list);
            for (Widget child: children)
            {
                Object object = ((DesignerScene)getScene()).findObject(child);
                assert object instanceof IPresentationElement;
                if (((IPresentationElement)object).getFirstSubject() == element)
                {
                    ((UMLNodeWidget)child).initializeNode((IPresentationElement)object);
                    found = true;
                    break;
                }
            }
            if (!found)
            {
                IPresentationElement presentation = Util.createNodePresentationElement();
                presentation.addSubject(element);

                Widget w = ((DesignerScene) getScene()).addNode(presentation);
                if (w != null)
                {
                    w.removeFromParent();
                    getContainerWidget().addChild(w);
                    w.setPreferredLocation(point);
                    point = new Point(point.x + 50, point.y + 50);
                }
            }
        }
    }

    @Override
    public ContainerWidget getContainerWidget()
    {
        if (stateContainerWidget == null)
        {
            stateContainerWidget = new RegionContainerWidget(getScene());
        }
        return stateContainerWidget;
    }
}
