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
package org.netbeans.modules.uml.diagrams.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 *
 * @author Sheryl Su
 */
public class DeleteCompartmentWidgetAction extends AbstractAction
{

    private Widget widget;
    private GraphScene scene;

    public DeleteCompartmentWidgetAction(Widget widget, String name)
    {
        super(name);
        this.widget = widget;
        if (widget.getScene() instanceof GraphScene)
        {
            scene = (GraphScene)widget.getScene();
        }
    }

    public void actionPerformed(ActionEvent e)
    {
        if (scene == null)
            return;
        
        Object obj = ((ObjectScene) scene).findObject(widget);
        if (obj instanceof IPresentationElement)
        {
            IElement element = ((IPresentationElement) obj).getFirstSubject();
            String subComponentName = "";
            if (element instanceof INamedElement)
            {
                subComponentName = ((INamedElement) element).getNameWithAlias();
            }
            NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(
                    NbBundle.getMessage(DeleteCompartmentWidgetAction.class,
                    "LBL_DeleteCompartment", subComponentName),
                    NbBundle.getMessage(DeleteCompartmentWidgetAction.class, "LBL_DeleteCompartmentTitle"),
                    NotifyDescriptor.YES_NO_OPTION);

            if (DialogDisplayer.getDefault().notify(descriptor) == NotifyDescriptor.YES_OPTION)
            {
                for (Object o : getAllChildren(new ArrayList<Object>(), widget))
                {
                    if (scene.isNode(o))
                    {
                        scene.removeNodeWithEdges(o);
                    }
                }
                element.delete();
            }
        }
    }

    private List<Object> getAllChildren(List<Object> list, Widget widget)
    {
        for (Widget child : widget.getChildren())
        {
            Object pe = scene.findObject(widget);
            if (scene.isNode(pe))
            {
                list.add(pe);
            }
            list = getAllChildren(list, child);
        }
        return list;
    }
}
