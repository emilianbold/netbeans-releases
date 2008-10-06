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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Collection;
import java.util.HashMap;
import org.netbeans.api.visual.action.ResizeProvider;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.SeparatorWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.diagrams.actions.CompositeWidgetSelectProvider;
import org.netbeans.modules.uml.drawingarea.LabelManager;
import org.netbeans.modules.uml.drawingarea.actions.ResizeStrategyProvider;
import org.netbeans.modules.uml.drawingarea.actions.WindowStyleResizeProvider;
import org.netbeans.modules.uml.drawingarea.palette.context.DefaultContextPaletteModel;
import org.netbeans.modules.uml.drawingarea.persistence.NodeWriter;
import org.netbeans.modules.uml.drawingarea.util.Util;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.drawingarea.view.UMLEdgeWidget;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;
import org.netbeans.modules.uml.drawingarea.widgets.ContainerWithCompartments;
import org.openide.util.Lookup;

/**
 *
 * @author Sheryl Su
 */
public abstract class CompositeNodeWidget extends UMLNodeWidget implements ContainerWithCompartments
{
    private ResizeStrategyProvider resizeProvider = null;
    
    public CompositeNodeWidget(Scene scene)
    {
        super(scene, true);
        addToLookup(initializeContextPalette());
        addToLookup(new CompositeWidgetSelectProvider(this));
    }

    private DefaultContextPaletteModel initializeContextPalette()
    {
        DefaultContextPaletteModel paletteModel = new DefaultContextPaletteModel(this);
        paletteModel.initialize(getContextPalettePath());
        return paletteModel;
    }


    public void addChildrenInBounds()
    {
        for (CompartmentWidget w : getCompartmentWidgets())
        {
            w.getContainerWidget().calculateChildren(false);//only add, do not check removal
        }
    }
 
    
    @Override
    protected void notifyFontChanged(Font font)
    {
        if (font == null || getNameWidget() == null)
        {
            return;
        }
        getNameWidget().setNameFont(font);
        for (CompartmentWidget w : getCompartmentWidgets())
        {
            w.setFont(font);            
        }
        revalidate();
    }

    @Override
    public void duplicate(boolean setBounds, Widget target)
    {
        assert target instanceof CompositeNodeWidget;

        DesignerScene targetScene = (DesignerScene) target.getScene();
        DesignerScene sourceScene = (DesignerScene) getScene();
        
        
        CompositeNodeWidget cloned = (CompositeNodeWidget) target;
        cloned.clear();
        cloned.setOrientation(getOrientation());
        for (CompartmentWidget w : getCompartmentWidgets())
        {
            CompartmentWidget rw = cloned.addCompartment(w.getElement());

            Rectangle rec = w.getBounds();
            rw.setPreferredSize(new Dimension(rec.width, rec.height));
            w.getContainerWidget().duplicate(setBounds, rw.getContainerWidget());
            rw.revalidate();
        }

        targetScene.validate();
        for (ConnectionWidget w : Util.getAllContainedEdges(target))
        {
            targetScene.removeEdge((IPresentationElement) targetScene.findObject(w));
        }

        for (ConnectionWidget cw : Util.getAllContainedEdges(this))
        {
            if (cw instanceof UMLEdgeWidget)
            {
                UMLEdgeWidget originalCW = (UMLEdgeWidget) cw;
                IPresentationElement sourcePE = sourceScene.getEdgeSource(originalCW.getObject());
                IPresentationElement targetPE = sourceScene.getEdgeTarget(originalCW.getObject());

                IPresentationElement newSourcePE = null;
                IPresentationElement newTargetPE = null;

                for (Object obj : Util.getAllNodeChildren(target))
                {
                    if (((IPresentationElement) obj).getFirstSubject().getXMIID().equals(sourcePE.getFirstSubject().getXMIID()))
                    {
                        newSourcePE = (IPresentationElement) obj;
                        break;
                    }
                }
                for (Object obj : Util.getAllNodeChildren(target))
                {
                    if (((IPresentationElement) obj).getFirstSubject().getXMIID().equals(targetPE.getFirstSubject().getXMIID()))
                    {
                        newTargetPE = (IPresentationElement) obj;
                        break;
                    }
                }

                IPresentationElement clonedEdgePE = Util.createNodePresentationElement();
                // Workaround for nested link. Unlike other relationships, it does not
                // have its own designated IElement, the IPresentationElement.getFirstSubject
                // returns an element at one end. Use this mechanism (multiple subjects) for 
                // DefaultDiagramEngine.createConnectionWidget() to identify the connector type
                if (((UMLEdgeWidget) cw).getWidgetID().
                        equals(UMLWidgetIDString.NESTEDLINKCONNECTIONWIDGET.toString()))
                {
                    clonedEdgePE.addSubject(sourcePE.getFirstSubject());
                    clonedEdgePE.addSubject(targetPE.getFirstSubject());
                } else
                {
                    clonedEdgePE.addSubject(originalCW.getObject().getFirstSubject());
                }

                Widget clonedEdge = targetScene.addEdge(clonedEdgePE);

                targetScene.setEdgeSource(clonedEdgePE, newSourcePE);
                targetScene.setEdgeTarget(clonedEdgePE, newTargetPE);
                Lookup lookup = clonedEdge.getLookup();
                if (lookup != null)
                {
                    LabelManager manager = lookup.lookup(LabelManager.class);
                    if (manager != null)
                    {
                        manager.createInitialLabels();
                    }
                }
                ((UMLEdgeWidget) originalCW).duplicate(clonedEdge);
            }
        }
        super.duplicate(setBounds, target);
        target.revalidate();
    }
    
    
    @Override
    public ResizeStrategyProvider getResizeStrategyProvider()
    {
        if (resizeProvider == null)
        {
            resizeProvider = new CompositeNodeResizeProvider(getResizeControlPoints());
        }
        return resizeProvider;
    }
    
    protected Rectangle calculateMinimumBounds()
    {
        Rectangle clientArea = new Rectangle(getBounds().x, getBounds().y);
        clientArea.add(getNameWidget().getPreferredBounds());
        
        for (CompartmentWidget w : getCompartmentWidgets())
        {
            Rectangle bounds = w.calculateMinimumBounds();
            Point location = w.getLocation();
            bounds.translate(location.x, location.y);
            bounds.translate(w.getParentWidget().getLocation().x, w.getParentWidget().getLocation().y);
            clientArea.add(bounds);
        }
        
        Insets insets = getBorder().getInsets();
        clientArea.x -= insets.left;
        clientArea.y -= insets.top;
        clientArea.width += insets.left + insets.right;
        clientArea.height += insets.top + insets.bottom;


        return clientArea;
    }
    
    public CompartmentWidget findCompartmentWidget(IElement element)
    {
        if (element != null)
        {
            for (CompartmentWidget compartmentWidget : getCompartmentWidgets())
            {
                if (compartmentWidget.getElement().equals(element))
                {
                    return compartmentWidget;
                }
            }
        }
        return null;
    }
    
    @Override
    public void save(NodeWriter nodeWriter)
    {
        HashMap map = nodeWriter.getProperties();
        map.put("Orientation", getOrientation().toString());
        nodeWriter.setProperties(map);
        super.save(nodeWriter);
    }
    
    public void clear()
    {
        for (ConnectionWidget connection: Util.getAllContainedEdges(this))
        {
            connection.removeFromParent();
        }
        for (CompartmentWidget compartment: getCompartmentWidgets())
        {
            compartment.removeFromParent();
        }
        getCompartmentWidgets().clear();      
    }
       
    
    public abstract String getContextPalettePath();
    public abstract UMLNameWidget getNameWidget();
    public abstract void setOrientation(SeparatorWidget.Orientation orientation);
    public abstract SeparatorWidget.Orientation getOrientation();
    public abstract CompartmentWidget addCompartment(IElement element);
    public abstract Collection<CompartmentWidget> getCompartmentWidgets();    
    public abstract void removeCompartment(CompartmentWidget widget);
    
  
    public class CompositeNodeResizeProvider extends WindowStyleResizeProvider
    {
        Rectangle minimumBounds;
        
        public CompositeNodeResizeProvider(ResizeProvider.ControlPoint points[])
        {
            super(points);
        }

        @Override
        public void resizingStarted(Widget widget)
        {
            super.resizingStarted(widget);
            minimumBounds = calculateMinimumBounds(); 
            for (CompartmentWidget w : getCompartmentWidgets())
            {
                
                Object constraint = w.getParentWidget().getChildConstraint(w);
                if (constraint instanceof Number)
                {
                    if (((Number) constraint).intValue() != 1)
                    {
                        int width = w.getBounds().width;
                        int height = w.getBounds().height;
                        if (getOrientation() == SeparatorWidget.Orientation.HORIZONTAL)
                        {
                            height = 0;
                        } else
                        {
                            width = 0;
                        }
                        w.setMinimumSize(new Dimension(width, height));
                    }
                    else
                        w.setPreferredSize(null);
                    w.setPreferredBounds(null);
                }
            }       
        }
        
        @Override
        public Rectangle boundsSuggested(Widget widget, Rectangle originalBounds, Rectangle suggestedBounds, ControlPoint controlPoint)
        {
            Rectangle suggested = suggestedBounds;

            if (minimumBounds.width < suggestedBounds.width)
            {
                if (minimumBounds.height < suggestedBounds.height)
                {
                    suggested = suggestedBounds;
                } else
                {
                    suggested = new Rectangle(suggestedBounds.x, widget.getBounds().y,
                            Math.max(minimumBounds.width, suggestedBounds.width),
                            Math.max(minimumBounds.height, suggestedBounds.height));
                }

            } else
            {
                if (minimumBounds.height < suggestedBounds.height)
                {
                    suggested = new Rectangle(widget.getBounds().x, suggestedBounds.y,
                            Math.max(minimumBounds.width, suggestedBounds.width),
                            Math.max(minimumBounds.height, suggestedBounds.height));
                } else
                {
                    suggested = new Rectangle(widget.getBounds().x, widget.getBounds().y,
                            Math.max(minimumBounds.width, suggestedBounds.width),
                            Math.max(minimumBounds.height, suggestedBounds.height));;
                }
            }
            return suggested;
        }
        
        public void resizingFinished(Widget widget)
        {
            super.resizingFinished(widget);
            for (CompartmentWidget w : getCompartmentWidgets())
            {
                Object constraint = w.getParentWidget().getChildConstraint(w);
                if (constraint instanceof Number)
                {
                    if (((Number) constraint).intValue() != 1)
                    {
                        w.setPreferredBounds(w.getBounds());
                    }
                }
                w.setMinimumSize(null);
                w.revalidate();
            }
        }
    }
}
