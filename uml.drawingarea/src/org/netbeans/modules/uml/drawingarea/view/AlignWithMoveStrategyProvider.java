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
package org.netbeans.modules.uml.drawingarea.view;

import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import org.netbeans.api.visual.action.*;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;

import java.util.ArrayList;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.drawingarea.UMLDiagramTopComponent;
import org.netbeans.modules.uml.drawingarea.palette.context.ContextPaletteManager;
import org.netbeans.modules.uml.drawingarea.util.Util;
import org.netbeans.modules.uml.drawingarea.widgets.ContainerWidget;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 * @author David Kaspar
 */
public class AlignWithMoveStrategyProvider extends AlignWithSupport implements MoveStrategy, MoveProvider {

    private boolean outerBounds;
    private static int eventID = 0;
    private LayerWidget interactionLayer = null;
    private LayerWidget mainLayer = null;
    private ArrayList < MovingWidgetDetails > movingWidgets = null;
    private Point original;
    private boolean moveWidgetInitialized;
    
    public AlignWithMoveStrategyProvider (AlignWithWidgetCollector collector, 
                                          LayerWidget interractionLayer, 
                                          LayerWidget widgetLayer,
                                          AlignWithMoveDecorator decorator, 
                                          boolean outerBounds) {
        super (collector, interractionLayer, decorator);
        this.outerBounds = outerBounds;
        this.interactionLayer = interractionLayer;
        this.mainLayer = widgetLayer;
        moveWidgetInitialized=false;
    }

    public Point locationSuggested (Widget widget, Point originalLocation, Point suggestedLocation) {
        
        
        if(movingWidgets == null)
        {
            if(originalLocation.equals(suggestedLocation))return suggestedLocation;//do not move if no real movement started
            initializeMovingWidgets(widget.getScene(), widget);
        }
        
        if(movingWidgets.size() > 1)
        {
            adjustControlPoints(originalLocation, suggestedLocation);
            return suggestedLocation;
        }
        
        Point widgetLocation = widget.getLocation ();
        Rectangle widgetBounds = outerBounds ? widget.getBounds () : widget.getClientArea ();
        Rectangle bounds = widget.convertLocalToScene (widgetBounds);
        bounds.translate (suggestedLocation.x - widgetLocation.x, suggestedLocation.y - widgetLocation.y);
        Insets insets = widget.getBorder ().getInsets ();
        if (! outerBounds) 
        {
            suggestedLocation.x += insets.left;
            suggestedLocation.y += insets.top;
        }
        
        Widget parent = widget.getParentWidget();
        
        Point scenePoint = parent.convertLocalToScene(suggestedLocation);
        Point point = super.locationSuggested (widget, bounds, scenePoint, true, true, true, true);
        if (! outerBounds) {
            point.x -= insets.left;
            point.y -= insets.top;
        }
        
        Point localPt = parent.convertSceneToLocal (point);
        adjustControlPoints(originalLocation, localPt);
        return localPt;
    }

    public void movementStarted (Widget widget) {
        show ();
        
        Scene scene = widget.getScene();
        ContextPaletteManager manager = scene.getLookup().lookup(ContextPaletteManager.class);
        if(manager != null)
        {
            manager.cancelPalette();
        }
        moveWidgetInitialized=false;
//        initializeMovingWidgets(scene, widget);
    }

    public void movementFinished (Widget widget) {
        hide ();
        
        Scene scene = widget.getScene();
        if(movingWidgets != null)
        {
            for(MovingWidgetDetails details : movingWidgets)
            {
                Widget curWidget = details.getWidget();
                Point location = curWidget.getLocation();

                MoveDropTargetDropEvent dropEvent = new MoveDropTargetDropEvent(curWidget, location);
                WidgetAction.WidgetDropTargetDropEvent event = new WidgetAction.WidgetDropTargetDropEvent (++ eventID, dropEvent);

                if(processLocationOperator(scene, event, location) == false)
                {
                    finishedOverScene(details, scene);
                }

                if (details.getOwner() instanceof ContainerWidget)
                {
                    ContainerWidget container = (ContainerWidget) details.getOwner();
                    container.firePropertyChange(ContainerWidget.CHILDREN_CHANGED, null, null);

                }
                
                // If a widgets new owner is the same as its original owner
                // then make sure that the widget has the same index as it 
                // had before the move began.
                for(MovingWidgetDetails curDetail : movingWidgets)
                {
                    curDetail.updateIndexIfRequired();
                }
            }
            
            movingWidgets.clear();
            movingWidgets = null;
            if (scene instanceof DesignerScene) 
            {
                TopComponent topComp = ((DesignerScene) scene).getTopComponent();
                if (topComp instanceof UMLDiagramTopComponent) 
                {
                    ((UMLDiagramTopComponent) topComp).setDiagramDirty(true);
                }
            }
        }
        
        ContextPaletteManager manager = scene.getLookup().lookup(ContextPaletteManager.class);
        if(manager != null)
        {
            manager.selectionChanged(null);
        }
        
        original = null;
    }

    public Point getOriginalLocation (Widget widget) {
        
        original = widget.getPreferredLocation();
        return original;
    }

    protected Point convertLocationToScene(Widget widget)
    {
        Point retVal = widget.getLocation();
        
        Widget curWidget = widget.getParentWidget();
        while(curWidget != null)
        {
            retVal.x += curWidget.getLocation().x;
            retVal.y += curWidget.getLocation().y;
            
            curWidget = curWidget.getParentWidget();
        }
        
        return retVal;
    }
    
    public void setNewLocation (Widget widget, Point location) {
        
        if(location != null)
        {
            int dx = location.x - original.x;
            int dy = location.y - original.y;
            if(dx!=0 || dy!=0)
            {
                if(movingWidgets == null)
                {
                    //in case if this class is used only as provider and strategy isn't used
                    initializeMovingWidgets(widget.getScene(), widget);
                }
                
                for(MovingWidgetDetails details : movingWidgets)
                {
                    Point point = details.getOriginalLocation();
                    Point newPt = new Point(point.x + dx, point.y + dy);
                    if (details.getWidget() instanceof ConnectionWidget)
                    {
                        ConnectionWidget connection = (ConnectionWidget) details.getWidget();
                        List<Point> list = new ArrayList<Point>();

                        ArrayList<Point> oldList = new ArrayList<Point>(connection.getControlPoints());
                        oldList.remove(connection.getFirstControlPoint());
                        oldList.remove(connection.getLastControlPoint());
                        Anchor sourceAnchor = connection.getSourceAnchor();
                        Anchor targetAnchor = connection.getTargetAnchor();
                        if (sourceAnchor == null || targetAnchor == null)
                        {
                            continue;
                        }
                        Point sourceP = sourceAnchor.compute(connection.getSourceAnchorEntry()).getAnchorSceneLocation();
                        list.add(sourceP);

                        for (Point p : oldList)
                        {
                            int ddx = p.x - connection.getFirstControlPoint().x;
                            int ddy = p.y - connection.getFirstControlPoint().y;
                            Point np = new Point(details.getOriginalLocation().x + dx + ddx, details.getOriginalLocation().y + dy + ddy);
                            list.add(np);
                        }
                        list.add(targetAnchor.compute(connection.getTargetAnchorEntry()).getAnchorSceneLocation());

                        connection.setControlPoints(list, true);
                    }
                    else
                        details.getWidget().setPreferredLocation(newPt);
                }
            }
        }
    }
    
    protected ArrayList<MovingWidgetDetails> getMovingDetails()
    {
        return movingWidgets;
    }

    private void adjustControlPoints(Point originalLocation, Point suggestedLocation)
    {
        // Nodes are only put onto this list of widgets.  Therefore I need 
        // to check there associated connection widgets to see if any are selected.
        for(MovingWidgetDetails details : movingWidgets)
        {
            Widget widget = details.getWidget();
            GraphScene scene = (GraphScene)widget.getScene();
            Object data = scene.findObject(widget);
            
            Point location = widget.getPreferredLocation();
            int dx = suggestedLocation.x - location.x;
            int dy = suggestedLocation.y - location.y;
        
            for (Object connectionObj : scene.findNodeEdges(data, true, true))
            {
                ConnectionWidget connection = (ConnectionWidget) scene.findWidget(connectionObj);
                if(connection.getState().isSelected() == true)
                {
                    for(Point pt : connection.getControlPoints())
                    {
                        pt.x += dx;
                        pt.y += dy;
                    }
                }
            }
        }
    }
    
    private boolean checkIfAccepted(Widget widget,
                                    WidgetAction.WidgetDropTargetDropEvent event,
                                    Point pt)
    {
        boolean retVal = false;
        
        if(widget != null)
        {
            for(Widget child : widget.getChildren())
            {
                Rectangle bounds = child.getBounds();
                if (bounds.contains(pt))
                {

                    List<Widget> children = child.getChildren();
                    for (Widget curChild : children)
                    {
                        if (curChild.isVisible() == true)
                        {
                            Point childLoc = curChild.getLocation();
                            Point testPoint = new Point(pt.x - childLoc.x,
                                                        pt.y - childLoc.y);
                            retVal = checkIfAccepted(curChild, event, testPoint);
                            
                            if(retVal == true)
                            {
                                break;
                            }
                        }
                    }
                    
                    if(retVal == false)
                    {
                        retVal = sendEvents(child, event, pt);
                    }
                }
            }
        }
        
        return retVal;
    }

    private void finishedOverScene(MovingWidgetDetails details, Scene scene)
    {
        Widget widget = details.getWidget();
        List <Widget>  children = interactionLayer.getChildren();
        if (children != null && children.contains(widget))
        {
            interactionLayer.removeChild(widget);
            mainLayer.addChild(widget);
        }

        Lookup lookup = scene.getLookup();
        if (lookup != null)
        {
            // Find out who currently owns the widgets metamodel element
            // If a new namespace is about to own the metamodel element, then
            // remove the model element from the curent namespace.
            INamedElement element = null;
            if (scene instanceof ObjectScene)
            {
                ObjectScene objScene = (ObjectScene) scene;
                
                Object data = objScene.findObject(widget);
                if (data instanceof IPresentationElement)
                {
                    IPresentationElement presentation = (IPresentationElement) data;
                    element = (INamedElement) presentation.getFirstSubject();
                }

                // Handle the namespace change.  The container widget will
                // handle adding a child to the containers namespace.
                //
                // We have to have to handle the case of when the widget
                // is moved from a container to the scene.
                if (details.getOwner() instanceof ContainerWidget)
                {
                    IDiagram diagram = lookup.lookup(IDiagram.class);
                    if (diagram != null)
                    {
                        INamespace space = diagram.getNamespace();
                        INamespace curSpace = element.getOwningPackage();

                        if (space.equals(curSpace) == false)
                        {
                            curSpace.removeOwnedElement(element);
                            space.addOwnedElement(element);
                        }
                    }
                }
            }
        }
    }

    private void initializeMovingWidgets(Scene scene, Widget widget)
    {
        if(movingWidgets != null)
        {
            // We should never be here;
            movingWidgets.clear();
            movingWidgets = null;
        }
        movingWidgets = new ArrayList < MovingWidgetDetails >();
                
        if (scene instanceof GraphScene)
        {
            GraphScene gscene = (GraphScene) scene;
            Object object = gscene.findObject(widget);
            if (gscene.isNode(object))
            {
                Set < ? > selected = gscene.getSelectedObjects();
                for (Object o : selected)
                {
                    if ((gscene.isNode(o)) && (isOwnerSelected(o, selected, gscene) == false))
                    {
                        Widget w = gscene.findWidget(o);
                        if (w != null)
                        {
                            Point pt = w.getPreferredLocation();
                            Widget owner = w.getParentWidget();
                            if (owner != null)
                            {
                                pt = owner.convertLocalToScene(pt);
                                w.setPreferredLocation(pt);
                            }

                            MovingWidgetDetails details = new MovingWidgetDetails(w, owner, pt);
                            movingWidgets.add(details);
                            
                            for (ConnectionWidget c: Util.getAllContainedEdges(w))
                            {
                                movingWidgets.add(new MovingWidgetDetails(c, 
                                        c.getParentWidget(), c.getParentWidget().convertLocalToScene(c.getFirstControlPoint())));
                            }

                            if (details.getOwner() != null)
                            {
                                details.getOwner().removeChild(w);
                            }
                            interactionLayer.addChild(w);
                        }
                    }
                }
                //need to sort in order to return back properly without indexOutOfBounds
                Collections.sort(movingWidgets, new Comparator<MovingWidgetDetails>()
                {
                    public int compare(AlignWithMoveStrategyProvider.MovingWidgetDetails o1, AlignWithMoveStrategyProvider.MovingWidgetDetails o2) {
                        return o1.getOriginalIndex()-o2.getOriginalIndex();
                    }
                });
            }
            else
            {
                MovingWidgetDetails details = new MovingWidgetDetails(widget, widget.getParentWidget(), widget.getPreferredLocation());

                movingWidgets.add(details);
            }
        }
        moveWidgetInitialized=true;
    }
    
    public boolean isMovementInitialized()
    {
        return moveWidgetInitialized;
    }

    private boolean isOwnerSelected(Object o, 
                                    Set<?> selected,
                                    GraphScene gscene)
    {
        boolean retVal = false;
        
        Widget widget = gscene.findWidget(o);
        if(widget != null)
        {
            Widget parent = widget.getParentWidget();
            Object parentObj = gscene.findObject(parent);
            
            if(parentObj != null)
            {
                if(selected.contains(parentObj) == true)
                {
                    retVal = true;
                }
                else
                {
                    retVal = isOwnerSelected(parentObj, selected, gscene);
                }
            }
        }
        
        return retVal;
    }
    
    private boolean processLocationOperator(Widget widget,
                                         WidgetAction.WidgetDropTargetDropEvent event,
                                         Point cursorSceneLocation)
    {
        Scene scene = widget.getScene();
        Point location = scene.getLocation();
        return processLocationOperator2(scene, event, new Point(cursorSceneLocation.x + location.x, cursorSceneLocation.y + location.y));
    }

    private boolean processLocationOperator2(Widget widget,
                                            WidgetAction.WidgetDropTargetDropEvent event, 
                                            Point point)
    {
        boolean retVal = false;
        
        if (!widget.isVisible())
        {
            return false;
        }

        Point location = widget.getLocation();
        point.translate(-location.x, -location.y);

        Rectangle bounds = widget.getBounds();
        if (bounds.contains(point))
        {
            List<Widget> children = widget.getChildren();
            Widget[] childrenArray = children.toArray(new Widget[children.size()]);

            for (int i = childrenArray.length - 1; i >= 0; i--)
            {
                if(processLocationOperator2(childrenArray[i], event, point) == true)
                {
                    retVal = true;
                    break;
                }
            }

            if ((retVal == false) && (widget.isHitAt(point) == true))
            {
                retVal = sendEvents(widget, event, point);
            }
        }

        point.translate(location.x, location.y);
        return retVal;
    }


    private boolean sendEvents(Widget target,
                               WidgetAction.WidgetDropTargetDropEvent event,
                               Point pt)
    {
        boolean retVal = false;
        
        if(target != null)
        {
            if(sendEvents(target.getActions(), target, event) == false)
            {
                String tool = target.getScene().getActiveTool();
                retVal = sendEvents(target.getActions(tool), target, event);
            }
            else
            {
                retVal = true;
            }
        }
        
        return retVal;
    }
    
    private boolean sendEvents(WidgetAction.Chain actions,
                                    Widget target,
                                    WidgetAction.WidgetDropTargetDropEvent event)
    {
        boolean retVal = false;
        
        if(actions != null)
        {
            for(WidgetAction action :actions.getActions())
            {
                if(action.drop(target, event) == WidgetAction.State.CONSUMED)
                {
                    retVal = true;
                    break;
                }
            }
        }
        
        return retVal;
    }
    
    protected class MovingWidgetDetails
    {
        private Widget widget = null;
        private Widget owner = null;
        private Point originalLocation = null;
        private int originalIndex = -1;
        
        public MovingWidgetDetails(Widget widget, 
                                   Widget owner,
                                   Point  location)
        {
            this.widget = widget;
            this.owner = owner;
            this.originalLocation = location;
            
            if(owner != null)
            {
                originalIndex = owner.getChildren().indexOf(widget);
            }
        }

        public Point getOriginalLocation()
        {
            return originalLocation;
        }

        public Widget getOwner()
        {
            return owner;
        }

        public Widget getWidget()
        {
            return widget;
        }
        
        public int getOriginalIndex()
        {
            return originalIndex;
        }
        
        public void updateIndexIfRequired()
        {
            if(owner.equals(widget.getParentWidget()) == true)
            {
                owner.removeChild(widget);
                owner.addChild(originalIndex, widget);
            }
        }
    }

}
