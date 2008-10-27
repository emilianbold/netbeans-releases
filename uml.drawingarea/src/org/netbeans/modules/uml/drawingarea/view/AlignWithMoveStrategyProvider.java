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
import java.util.Collection;
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
import org.netbeans.modules.uml.drawingarea.widgets.ContainerWithCompartments;
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
        
        
//        if(movingWidgets == null)
//        {
//            if(originalLocation.equals(suggestedLocation))return suggestedLocation;//do not move if no real movement started
//            initializeMovingWidgets(widget.getScene(), widget);
//        }
        
        if(movingWidgets.size() > 1)
        {
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
        
        Point point = super.locationSuggested (widget, bounds, suggestedLocation, true, true, true, true);
        if (! outerBounds) {
            point.x -= insets.left;
            point.y -= insets.top;
        }
        
        Point localPt = parent.convertSceneToLocal (point);
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
        initializeMovingWidgets(widget.getScene(), widget);
        
        lastPoint = original;
        if (lastPoint != null)
        {
            original = widget.getParentWidget().convertLocalToScene(lastPoint);
        }
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

    Point lastPoint = null;
    public void setNewLocation (Widget widget, Point location) {
        
        if(location != null && original != null)
        {
            //int dx = location.x - original.x;
            //int dy = location.y - original.y;

            // Determine if the new location of the widget has actually moved.
            //
            // Originally we used the dx and dy variables to determine if the
            // node moved.  However, the dx and dy values are based off the
            // original position of the widget.  In this case the "original"
            // position of the widget is defined as the location before the move
            // started.  Therefore if the widget is moved back to the exact
            // coordinate as the origional location the widget will not be
            // moved.  This is not that big of a deal when using the mouse to
            // move because it is not very likely that the exact coordinate will
            // occur in a single move.  However when using the keyboard to move
            // nodes this is very likey to happen, especially on the SQD where
            // the lifeline node can only be moved left and right.

            int dx = location.x - widget.getPreferredLocation().x;
            int dy = location.y - widget.getPreferredLocation().y;
            if(dx != 0 || dy != 0)
            {
                if(movingWidgets == null)
                {
                    //in case if this class is used only as provider and strategy isn't used
                    initializeMovingWidgets(widget.getScene(), widget);
                }

                // The dx is calcuated using a start location of when the move
                // first started.  However for connection points we do not have
                // the connection point values from before the move started.
                //
                // Therefore use the reference widget to determine the dx.

                lastPoint = location;
                adjustControlPoints(getMovingWidgetList(), dx, dy);
                for(MovingWidgetDetails details : movingWidgets)
                {
                    Point point = details.getWidget().getPreferredLocation();
                    if(point == null)
                    {
                        point = details.getWidget().getLocation();
                    }

                    Point newPt = new Point(point.x + dx, point.y + dy);
                    if (details.getWidget() instanceof ConnectionWidget)
                    {
                        // Do nothing because the adjustControlPoints will
                        // take care of this situation.
                    }
                    else
                    {
                        // I do not understand what is different between the
                        // composite container and the other nodes.  However
                        // the position is always relative to the contianers
                        // original owner.
                        //
                        // Since we are at the end of the release cycle I am
                        // not wanting to do anything big here.  So special
                        // case the composite nodes.
                        if(details.getWidget() instanceof ContainerWithCompartments)
                        {
                            newPt = details.getOwner().convertLocalToScene(newPt);
                        }
                        details.getWidget().setPreferredLocation(newPt);
                    }
                }

                //Now mark the diagram dirty.. since we really moved
                Scene scene = widget.getScene();
                if (scene != null && scene instanceof DesignerScene)
                {
                    TopComponent topComp = ((DesignerScene) scene).getTopComponent();
                    if (topComp instanceof UMLDiagramTopComponent)
                    {
                        ((UMLDiagramTopComponent) topComp).setDiagramDirty(true);
                    }
                }
            }
        }
    }
    
    protected ArrayList<MovingWidgetDetails> getMovingDetails()
    {
        return movingWidgets;
    }

    protected List < Widget > getMovingWidgetList()
    {
        ArrayList < Widget > retVal = new ArrayList < Widget >();
        
        for(MovingWidgetDetails details : movingWidgets)
        {
            retVal.add(details.getWidget());
        }
        
        return retVal;
    }

    /**
     * Adjust the control points of all selected connection widgets attached
     * to a node.
     *
     * @param widgets The list of widgets to update.
     * @param dx The distance in the x direction.
     * @param dy The distance in the y direction.
     */
    public static void adjustControlPoints(List < Widget> widgets, 
                                           int dx, int dy)
    {
        // Since child nodes are not part of the widgets (since they are moved
        // when the parent widget is moved), we need to first make sure
        // that we get not only the selected set of widgets, but also the
        // edges attached to all child nodes.  This is mostly for containers.
        List < ConnectionWidget > connections = includeAllConnections(widgets);
        
        ArrayList < Object > alreadyProcessed = new ArrayList < Object >();

        for(ConnectionWidget connection : connections)
        {
            GraphScene scene = (GraphScene)connection.getScene();
            Object data = scene.findObject(connection);
            
            if(alreadyProcessed.contains(data) == false)
            {
                if ((connection.getState().isSelected() == true) || 
                    (widgets.contains(connection) == true))
                {
                    List<Point> points = connection.getControlPoints();
                    for (int index = 1; index < points.size() - 1; index++) 
                    {
                        Point pt = points.get(index);
                        pt.x += dx;
                        pt.y += dy;
                    }
                }
                
                // Each node also needs to be revalidated so that the anchor 
                // gets a chance to update the end point
                Anchor sourceAnchor = connection.getSourceAnchor();
                if(sourceAnchor != null)
                {
                    sourceAnchor.getRelatedWidget().revalidate();
                }
                
                Anchor targetAnchor = connection.getTargetAnchor();
                if(targetAnchor != null)
                {
                    targetAnchor.getRelatedWidget().revalidate();
                }
                
                alreadyProcessed.add(data);
            }
        }
    }
    
    private static List<ConnectionWidget> includeAllConnections(List<Widget> widgets) 
    {
        ArrayList < ConnectionWidget > retVal = new ArrayList <ConnectionWidget>();
        
        for(Widget widget : widgets)
        {
           GraphScene scene = (GraphScene)widget.getScene();
           List < ConnectionWidget > connections = buildListOfConnections(scene, widget);
           if((connections != null) && (connections.size() > 0))
           {
               retVal.addAll(connections);
           }
        }
         
        return retVal;
    }
    
    private static List<ConnectionWidget> buildListOfConnections(GraphScene scene,
                                                                 Widget widget)
    {
        ArrayList < ConnectionWidget > retVal = new ArrayList <ConnectionWidget>();
        
        // First get the edges for the passed in widget.  If the data object
        // does not represent a node the method findNodeEdges will throw an
        // assertion.  Therefore check if it is a node first.
        Object data = scene.findObject(widget);
        if((data != null) && (scene.isNode(data) == true))
        {
            // If you ask for the object of a widget it will get the object
            // of the first parent that has an associated object.  Therefore
            // we need to first make sure that the object is associated with the
            // widget in question.  Otherwise we will end up with a lot of
            // duplicates.
            if(widget.equals(scene.findWidget(data)) == true)
            {
                Collection edges = scene.findNodeEdges(data, true, true);
                if((edges != null) && (edges.size() > 0))
                {
                    for(Object curEdge : edges)
                    {
                        ConnectionWidget connection = (ConnectionWidget)scene.findWidget(curEdge);
                        retVal.add(connection);
                    }
                }
            }
        }
        
        // Second get the edges for all of the children.
        for(Widget child : widget.getChildren())
        {
            
            List<ConnectionWidget> childConns = buildListOfConnections(scene, child);
            if((childConns != null) && (childConns.size() > 0))
            {
                retVal.addAll(childConns);
            }
        }
        return retVal;
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
