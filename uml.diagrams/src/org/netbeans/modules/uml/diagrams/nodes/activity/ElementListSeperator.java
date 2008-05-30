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
package org.netbeans.modules.uml.diagrams.nodes;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.HashSet;
import java.util.Set;
import javax.swing.BorderFactory;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.action.WidgetAction.State;
import org.netbeans.api.visual.action.WidgetAction.WidgetMouseEvent;
import org.netbeans.api.visual.animator.AnimatorEvent;
import org.netbeans.api.visual.animator.AnimatorListener;
import org.netbeans.api.visual.animator.SceneAnimator;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.SeparatorWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.drawingarea.view.DesignerTools;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget.RESIZEMODE;

/**
 *
 * @author treyspiva
 */
public class ElementListSeperator extends SeparatorWidget
{
    private Widget targetWidget = null;

    public ElementListSeperator(Scene scene,  Widget target)
    {
        this(scene, SeparatorWidget.Orientation.HORIZONTAL, target);
    }
    
    public ElementListSeperator(Scene scene, 
                                SeparatorWidget.Orientation orientation,
                                Widget target)
    {
        super(scene, orientation);
        
        this.targetWidget = target;
        createActions(DesignerTools.SELECT).addAction(new CollapseControllerAction());
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }

    public class CollapseButton extends Widget
    {
        public CollapseButton(Scene scene)
        {
            super(scene);
            setOpaque(true);
            setPreferredSize(new Dimension(10, 10));
            setBorder(BorderFactory.createLineBorder(Color.BLACK));
            setBackground(null);
        }
        
        @Override
        protected void paintBackground()
        {
//            super.paintBackground();

            Paint bg = getBackground();

            // TODO: Need to test if gradient paint preference is set.
            if (bg instanceof Color)
            {
                Rectangle bounds = getClientArea();
                float midX = bounds.width / 2;

                Color bgColor = (Color) bg;
                GradientPaint paint = new GradientPaint(midX, 0, Color.WHITE,
                        midX, getBounds().height,
                        bgColor);
                Graphics2D g = getGraphics();
                g.setPaint(paint);
                g.fillRect(0, 0, bounds.width, bounds.height);
            }
        }

        @Override
        protected void paintWidget()
        {
            super.paintWidget();
        }
    }
    
    public class CollapseControllerAction extends WidgetAction.Adapter
    {
        private Border border = null; 
        private boolean collapsed = false;

        @Override
        public State mouseClicked(Widget widget, WidgetMouseEvent event)
        {
            State retVal = State.REJECTED;
            if((event.getClickCount() == 2) && (event.getButton() == MouseEvent.BUTTON1))
            {
                // First make sure that the nodes minumum size is not set.
                System.out.println("I am here");
                Widget parent = getParentWidget();
                UMLNodeWidget node = null;
                while(parent != null)
                {
                    if (parent instanceof UMLNodeWidget)
                    {
                        node = (UMLNodeWidget) parent;
                        
                        // I would perfer this to be the name comparment min size.  
                        // Maybe the size of the children above.
                        node.setMinimumSize(new Dimension(1,1));
                        break;
                    }
                    parent = parent.getParentWidget();
                }
                
                
                Rectangle bounds = targetWidget.getBounds();
                final SceneAnimator animator = getScene().getSceneAnimator();
                final Rectangle newBounds = collapsed == false ? new Rectangle() : null;
                
                for(Widget child : targetWidget.getChildren())
                {
                    animator.animatePreferredBounds(child, newBounds);
                }
                
                if(collapsed == true)
                {
                    if(border != null)
                    {
                        targetWidget.setBorder(border);
                    }
                    collapsed = false;
                }
                else
                {
                    border = targetWidget.getBorder();
                    targetWidget.setBorder(BorderFactory.createEmptyBorder(1,0,1,0));
//                    targetWidget.setBorder(BorderFactory.createEmptyBorder());
                    collapsed = true;
                }
            }
            
            return retVal;
        }
        
    }
}
