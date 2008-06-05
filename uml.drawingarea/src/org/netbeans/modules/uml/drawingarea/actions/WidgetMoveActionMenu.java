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

package org.netbeans.modules.uml.drawingarea.actions;

import java.awt.event.ActionEvent;
import java.util.List;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.drawingarea.view.LabelNode;
import org.netbeans.modules.uml.drawingarea.view.UMLEdgeWidget;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;
import org.openide.util.NbBundle;

/**
 *
 * @author Sheryl Su
 */
public class WidgetMoveActionMenu
{

    public static final class MoveForward extends AbstractWidgetMoveAction
    {

        public MoveForward()
        {
            super(NbBundle.getMessage(WidgetMoveActionMenu.class, "LBL_MoveForward"));
        }

        protected String iconResource()
        {
            return "org/netbeans/modules/uml/resources/images/move-forward.png";
        }

        public void actionPerformed(ActionEvent ev)
        {
            Widget widget = getWidget();
            if (widget != null)
            {
                Widget parentWidget = widget.getParentWidget();
                if (parentWidget == null)
                {
                    return;
                }
                List<Widget> children = parentWidget.getChildren();
                int i = children.indexOf(widget);
                if (i < 0 || i == children.size() - 1)
                {
                    return;
                }

                int target = children.size();
                for (Widget w : children)
                {
                    if (w == widget || (!(w instanceof UMLNodeWidget) && !(w instanceof UMLEdgeWidget)))
                        continue;
                    boolean intersect = w.convertLocalToScene(w.getBounds()).intersects(
                            widget.convertLocalToScene(widget.getBounds()));
                    if (intersect && children.indexOf(w) > i)
                    {
                        target = Math.min(target, children.indexOf(w));
                    }
                }
                if (target == children.size())
                    return;
                Widget swap = children.get(target);
                
                parentWidget.removeChild(widget);
                parentWidget.removeChild(swap);
                parentWidget.addChild(i, swap);
                if (swap instanceof LabelNode)
                    target += 1;
                if (widget instanceof LabelNode)
                    target -= 1;
                parentWidget.addChild(target, widget);
                widget.getScene().validate();
            }
        }
    }

    public static final class MoveBackward extends AbstractWidgetMoveAction
    {

        public MoveBackward()
        {
            super(NbBundle.getMessage(WidgetMoveActionMenu.class, "LBL_MoveBackward"));
        }

        public void actionPerformed(ActionEvent ev)
        {
            Widget widget = getWidget();
            if (widget != null)
            {
                Widget parentWidget = widget.getParentWidget();
                if (parentWidget == null)
                {
                    return;
                }
                List<Widget> children = parentWidget.getChildren();
                int i = children.indexOf(widget);
                if (i < 1)
                {
                    return;
                }

                int target = 0;
                for (Widget w : children)
                {
                    // filter out non object widget, e.g. floating movable label widget
                    if (w == widget || (!(w instanceof UMLNodeWidget) && !(w instanceof UMLEdgeWidget)))
                        continue;
                    boolean intersect = w.convertLocalToScene(w.getBounds()).intersects(
                            widget.convertLocalToScene(widget.getBounds()));
                    if ( intersect && children.indexOf(w) < i)
                    {
                        target = Math.max(target, children.indexOf(w));
                    }
                }

                Widget swap = children.get(target);
                
                parentWidget.removeChild(swap);
                parentWidget.removeChild(widget);
                parentWidget.addChild(target, widget);
                if (widget instanceof LabelNode)
                    i += 1;
                if (swap instanceof LabelNode)
                    i -= 1;
                parentWidget.addChild(i, swap);
                widget.getScene().validate();
            }
        }

        protected String iconResource()
        {
            return "org/netbeans/modules/uml/resources/images/move-backward.png";
        }
    }

    public static final class MoveToFront extends AbstractWidgetMoveAction
    {

        public MoveToFront()
        {
            super(NbBundle.getMessage(WidgetMoveActionMenu.class, "LBL_MoveFront"));
        }

        public void actionPerformed(ActionEvent ev)
        {
            Widget widget = getWidget();
            if (widget != null)
            {
                Widget parent = widget.getParentWidget();
                parent.removeChild(widget);
                parent.addChild(widget);
                widget.getScene().validate();
            }
        }

        protected String iconResource()
        {
            return "org/netbeans/modules/uml/resources/images/move-front.png";
        }
    }

    public static final class MoveToBack extends AbstractWidgetMoveAction
    {

        public MoveToBack()
        {
            super(NbBundle.getMessage(WidgetMoveActionMenu.class, "LBL_MoveBack"));
        }

        public void actionPerformed(ActionEvent ev)
        {
            Widget widget = getWidget();
            if (widget != null)
            {
                Widget parent = widget.getParentWidget();
                parent.removeChild(widget);
                parent.addChild(0, widget);
                widget.getScene().validate();
            }
        }

        protected String iconResource()
        {
            return "org/netbeans/modules/uml/resources/images/move-to-back.png";
        }
    }
}