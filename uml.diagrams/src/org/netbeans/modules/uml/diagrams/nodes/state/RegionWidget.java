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

import java.awt.BasicStroke;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ResizeControlPointResolver;
import org.netbeans.api.visual.action.ResizeProvider;
import org.netbeans.api.visual.action.ResizeStrategy;
import org.netbeans.api.visual.action.SelectProvider;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.SeparatorWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.IRegion;
import org.netbeans.modules.uml.core.metamodel.common.commonstatemachines.ITransition;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.diagrams.UMLRelationshipDiscovery;
import org.netbeans.modules.uml.diagrams.nodes.UMLNameWidget;
import org.netbeans.modules.uml.drawingarea.ModelElementChangedKind;
import org.netbeans.modules.uml.drawingarea.actions.DiagramPopupMenuProvider;
import org.netbeans.modules.uml.drawingarea.util.Util;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.drawingarea.view.DesignerTools;
import org.netbeans.modules.uml.drawingarea.view.UMLWidget;

/**
 *
 * @author Sheryl Su
 */
public class RegionWidget extends Widget implements PropertyChangeListener
{

    private Scene scene;
    private IRegion region;
    private Widget stateContainerWidget;
    private CompositeStateWidget compositeStateWidget;
    private CompartmentSeparatorWidget separatorWidget;
    private UMLNameWidget nameWidget;
    private static final int STROKE_THICKNESS = 1;
    private BasicStroke stroke =
            new BasicStroke(STROKE_THICKNESS, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 1.0f, new float[]{5, 5}, 0);

    private Rectangle mininumBounds;
    private int BORDER_THICKNESS = 1;
    private ResizeStrategy RESIZE_STRATEGY;
    private static SelectProvider provider = new RegionSelectProvider();
    private static RegionWidgetSelectAction regionSelectAction = new RegionWidgetSelectAction(provider, true);
    
    
    public RegionWidget(Scene scene, IRegion region, CompositeStateWidget compositeStateWidget)
    {
        super(scene);
        setForeground(null);
        setBackground(null);
        this.scene = scene;
        this.region = region;
        this.compositeStateWidget = compositeStateWidget;
        init(region);

        DiagramPopupMenuProvider menuProvider = new DiagramPopupMenuProvider();
        WidgetAction.Chain selectTool = createActions(DesignerTools.SELECT);
        selectTool.addAction(ActionFactory.createResizeAction(RESIZE_STRATEGY, new RegionResizeControlPointResolver(), RESIZE_PROVIDER));               
        
        selectTool.addAction(regionSelectAction);
        selectTool.addAction(ActionFactory.createPopupMenuAction(menuProvider));
        
    }

    private void init(IRegion region)
    {
        Widget layer = new Widget(scene);
        layer.setForeground(null);
        layer.setBackground(null);
        
        layer.setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY, 0));
        nameWidget = new UMLNameWidget(scene, false, getWidgetID());
        nameWidget.initialize(region);

        layer.addChild(nameWidget, 0);
        stateContainerWidget = new RegionContainerWidget(scene);
        
        layer.addChild(stateContainerWidget, 1);

        boolean horizontal = getCompositeStateWidget().isHorizontalLayout();
        if (horizontal)
        {
            setLayout(LayoutFactory.createHorizontalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY, 0));
        } else
        {
            setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY, 0));
        }
        separatorWidget = new CompartmentSeparatorWidget(scene,
                horizontal ? SeparatorWidget.Orientation.VERTICAL : SeparatorWidget.Orientation.HORIZONTAL, stroke, BORDER_THICKNESS);
        addChild(layer, 1);
        addChild(separatorWidget, 0);

        
        initContainedElements();

        RESIZE_STRATEGY = new ResizeStrategy()
        {
            public Rectangle boundsSuggested(Widget widget, Rectangle originalBounds, Rectangle suggestedBounds, ResizeProvider.ControlPoint controlPoint)
            {
                int width = Math.max(mininumBounds.width + separatorWidget.calculateClientArea().width, suggestedBounds.width + 1);
                int height = Math.max(mininumBounds.height + separatorWidget.calculateClientArea().height + 1, suggestedBounds.height);
                
                if (getCompositeStateWidget().isHorizontalLayout())
                {
                    return new Rectangle(suggestedBounds.x, suggestedBounds.y, width, suggestedBounds.height);
                }
                return new Rectangle(suggestedBounds.x, suggestedBounds.y, suggestedBounds.width, height);
            }
        };
        
    }

    
    public void updateOrientation(boolean horizontal)
    {
        if (separatorWidget != null)
            separatorWidget.removeFromParent();
        
        if (getCompositeStateWidget().isHorizontalLayout())
        {
            setLayout(LayoutFactory.createHorizontalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY, 0));
        } else
        {
            setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.JUSTIFY, 0));
        }
        separatorWidget = new CompartmentSeparatorWidget(scene,
                horizontal ? SeparatorWidget.Orientation.VERTICAL : SeparatorWidget.Orientation.HORIZONTAL, stroke, BORDER_THICKNESS);
        addChild(separatorWidget, 0);
        scene.validate();
    }
    
    
    public IRegion getElement()
    {
        return region;
    }

    private String getWidgetID()
    {
        return UMLWidget.UMLWidgetIDString.STATEWIDGET.toString();
    }

    public CompositeStateWidget getCompositeStateWidget()
    {
        return compositeStateWidget;
    }

    public void showSeparator(boolean show)
    {
        if (separatorWidget != null)
        {
            separatorWidget.setVisible(show);
        }
    }

    public void propertyChange(PropertyChangeEvent evt)
    {
        if (evt.getPropertyName().equals(ModelElementChangedKind.ELEMENTMODIFIED.toString()))
        {
            nameWidget.propertyChange(evt);
        } 
        else if (evt.getPropertyName().equals(ModelElementChangedKind.DELETE.toString()) ||
                 evt.getPropertyName().equals(ModelElementChangedKind.PRE_DELETE.toString()))
        {
            compositeStateWidget.removeRegion(this);
        }
    }

    private class RegionResizeControlPointResolver implements ResizeControlPointResolver
    {
        public ResizeProvider.ControlPoint resolveControlPoint(Widget widget, Point point)
        {
            Rectangle bounds = widget.getBounds();
          
            if (point.y >= bounds.y + bounds.height - BORDER_THICKNESS - STROKE_THICKNESS && point.y < bounds.y + bounds.height + 1)
            {
                if (point.x >= bounds.x  && point.x < bounds.x + bounds.width)
                {               
                    return ResizeProvider.ControlPoint.BOTTOM_CENTER;
                }
            }
            else if (point.x >= bounds.x + bounds.width - BORDER_THICKNESS - STROKE_THICKNESS && point.x < bounds.x + bounds.width + 1)
            {
                if (point.y >= bounds.y  && point.y < bounds.y + bounds.height)
                {               
                    return ResizeProvider.ControlPoint.CENTER_RIGHT;
                }
            }
            return null;
        }
    }
    

    
    private Rectangle calculateMinimumBounds()
    {
        Insets insets = getBorder().getInsets();
        Rectangle clientArea = new Rectangle();
        
        clientArea.add(nameWidget.getPreferredBounds());
        clientArea.add(calculateMinimumBounds(stateContainerWidget));
        
        clientArea.x -= insets.left;
        clientArea.y -= insets.top;
        clientArea.width += insets.left + insets.right;
        clientArea.height += insets.top + insets.bottom;
        
        return clientArea;   
    }
    
    
    
    private Rectangle calculateMinimumBounds(Widget widget)
    {
        Insets insets = getBorder().getInsets();
        Rectangle clientArea = new Rectangle();
        for (Widget child : widget.getChildren())
        {
            if (!child.isVisible())
            {
                continue;
            }
            Point location = child.getLocation();
            Rectangle bounds = child.getBounds();
            bounds.translate(location.x, location.y);
            clientArea.add(bounds);
        }
        clientArea.translate(widget.getLocation().x, widget.getLocation().y);
        clientArea.x -= insets.left;
        clientArea.y -= insets.top;
        clientArea.width += insets.left + insets.right;
        clientArea.height += insets.top + insets.bottom;
        return clientArea;
    }
    

    protected void notifyStateChanged(ObjectState previousState, ObjectState state)
    {
        if (state.isSelected())
            setBorder(BorderFactory.createLineBorder(0, UMLWidget.BORDER_HILIGHTED_COLOR));
        else
            setBorder(BorderFactory.createEmptyBorder());
    }
    
    protected UMLNameWidget getNameWidget()
    {
        return nameWidget;
    }
    
    private final ResizeProvider RESIZE_PROVIDER = new ResizeProvider() {
        public void resizingStarted (Widget widget) {
            mininumBounds = calculateMinimumBounds();
        }
        public void resizingFinished (Widget widget) {
            compositeStateWidget.revalidate();
        }
    };
    
    private void initContainedElements()
    {
        if (!(scene instanceof GraphScene))
        {
            return;
        }
     
        Point point = new Point(10,10);
        for (IElement element : region.getElements())
        {
            if (element instanceof ITransition)
                continue;
            IPresentationElement presentation = Util.createNodePresentationElement();
            presentation.addSubject(element);

            Widget w = ((DesignerScene) getScene()).addNode(presentation);
            if (w != null)
            {
                w.removeFromParent();
                stateContainerWidget.addChild(w);
                w.setPreferredLocation(point);
                point = new Point(point.x + 50, point.y + 50);
            }
        }
    }
    
    
    // todo: need to find a better way to address region selection and move behavior
    private static class RegionWidgetSelectAction extends WidgetAction.LockedAdapter {

    private boolean aiming = false;
    private Widget aimedWidget = null;
    private boolean invertSelection;
    private SelectProvider provider;
    private boolean trapRightClick = false ;

    public RegionWidgetSelectAction (SelectProvider provider, boolean trapRightClick) {
        this.provider = provider ;
        this.trapRightClick = trapRightClick ;
    }
    
    public RegionWidgetSelectAction (SelectProvider provider) {
        this.provider = provider;
    }

    protected boolean isLocked () {
        return aiming;
    }

    public State mousePressed(Widget widget, WidgetMouseEvent event) {
        
        if (isLocked()) {
            return State.createLocked(widget, this);
        }
        
        Point localLocation = event.getPoint();
        
        if (event.getButton() == MouseEvent.BUTTON1 || event.getButton() == MouseEvent.BUTTON2) {
            invertSelection = (event.getModifiersEx() & MouseEvent.CTRL_DOWN_MASK) != 0;
            
            if (!invertSelection && widget.getState().isSelected())
                return State.REJECTED;
                
            if (provider.isSelectionAllowed(widget, localLocation, invertSelection)) {
                aiming = provider.isAimingAllowed(widget, localLocation, invertSelection);
                if (aiming) {
                    updateState(widget, localLocation);
                    return State.createLocked(widget, this);
                } else {
                    provider.select(widget, localLocation, invertSelection);
                    return State.CHAIN_ONLY;
                }
            }
        } else if (trapRightClick && event.getButton() == MouseEvent.BUTTON3) {
            provider.select(widget, localLocation, invertSelection);
            return State.CHAIN_ONLY;
        }
        
        return State.REJECTED;
    }

    public State mouseReleased (Widget widget, WidgetMouseEvent event) {
        if (aiming) {
            Point point = event.getPoint ();
            updateState (widget, point);
            if (aimedWidget != null)
                provider.select (widget, point, invertSelection);
            updateState (null, null);
            aiming = false;
            return State.CONSUMED;
        }
        return super.mouseReleased (widget, event);
    }

    private void updateState (Widget widget, Point localLocation) {
        if (widget != null  &&  ! widget.isHitAt (localLocation))
            widget = null;
        if (widget == aimedWidget)
            return;
        if (aimedWidget != null)
            aimedWidget.setState (aimedWidget.getState ().deriveWidgetAimed (false));
        aimedWidget = widget;
        if (aimedWidget != null)
            aimedWidget.setState (aimedWidget.getState ().deriveWidgetAimed (true));
    }

    public State keyTyped (Widget widget, WidgetKeyEvent event) {
        if (! aiming  &&  event.getKeyChar () == KeyEvent.VK_SPACE) {
            provider.select (widget, null, (event.getModifiersEx () & MouseEvent.CTRL_DOWN_MASK) != 0);
            return State.CONSUMED;
        }
        return State.REJECTED;
    }
    }
    
    private static class RegionSelectProvider implements SelectProvider {

        public boolean isAimingAllowed (Widget widget, Point localLocation, boolean invertSelection) {
            return false;
        }

        public boolean isSelectionAllowed (Widget widget, Point localLocation, boolean invertSelection) {
            return true;
        }

        public void select (Widget widget, Point localLocation, boolean invertSelection) {
            ObjectScene objectScene = null;
            
            Scene scene = widget.getScene();
            if (scene instanceof ObjectScene)
                objectScene = (ObjectScene)scene;
            
            if (objectScene == null)
                return;
            Object object = objectScene.findObject (widget);

            objectScene.setFocusedObject (object);
            if (object != null) {
                if (! invertSelection  &&  objectScene.getSelectedObjects ().contains (object))
                    return;
                objectScene.userSelectionSuggested (Collections.singleton (object), invertSelection);
            } else
                objectScene.userSelectionSuggested (Collections.emptySet (), invertSelection);
        }
    }
}
