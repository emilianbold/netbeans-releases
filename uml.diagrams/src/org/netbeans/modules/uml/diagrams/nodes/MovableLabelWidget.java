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
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.UIManager;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.MoveProvider;
import org.netbeans.api.visual.action.MoveStrategy;
import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.FactoryRetriever;
import org.netbeans.modules.uml.core.metamodel.core.foundation.ICreationFactory;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.diagrams.actions.NodeLabelIteratorAction;
import org.netbeans.modules.uml.drawingarea.actions.MoveNodeKeyAction;
import org.netbeans.modules.uml.drawingarea.actions.ObjectSelectable;
import org.netbeans.modules.uml.drawingarea.engines.DiagramEngine;
import org.netbeans.modules.uml.drawingarea.persistence.NodeWriter;
import org.netbeans.modules.uml.drawingarea.persistence.PersistenceUtil;
import org.netbeans.modules.uml.drawingarea.persistence.api.DiagramNodeWriter;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.drawingarea.view.DesignerTools;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Sheryl Su
 */
public class MovableLabelWidget extends EditableCompartmentWidget implements Widget.Dependency, DiagramNodeWriter
{

    private Widget nodeWidget;
    private double dx;
    private double dy;
    private boolean updateLocation = false;
    private Color initialBeforeSelectionFG;
    private Integer x0;
    private Integer y0;
    private boolean diagramLoading = false;
    private int diffX;
    private int diffY;
    //we need the following 3 vars for combined fragments
    private boolean grandParentLocationExists = false;
    private Point grandParentLoc = null;
    private UMLNodeWidget grandParent;
    private Point origLoc;
    private InstanceContent lookupContent = new InstanceContent();
    private Lookup lookup = new AbstractLookup(lookupContent);

    public MovableLabelWidget(Scene scene, Widget nodeWidget, IElement element, String widgetID, String displayName)
    {
        this(scene, nodeWidget, element, widgetID, displayName, null, null);
    }

    /**
     * 
     * @param scene
     * @param parent
     * @param spec
     * @param toString
     * @param string
     * @param x0 - initial shift from center
     * @param y0 - initial shift from center
     */
    public MovableLabelWidget(Scene scene, Widget nodeWidget, IElement element, String widgetID, String displayName, Integer x0, Integer y0) {
        super(scene, element, widgetID, displayName);
        this.nodeWidget = nodeWidget;
        this.x0=x0;
        this.y0=y0;
        nodeWidget.addDependency(this);
        if (scene instanceof DesignerScene)
        {
            DesignerScene ds = (DesignerScene) scene;
            LabelMoveSupport labelMoveSupport = new LabelMoveSupport(ds.getInterractionLayer(), nodeWidget);
            WidgetAction.Chain chain = createActions(DesignerTools.SELECT);
            chain.addAction(ds.createSelectAction());
            chain.addAction(ActionFactory.createMoveAction(labelMoveSupport, labelMoveSupport));
            chain.addAction(new MoveNodeKeyAction(labelMoveSupport, labelMoveSupport));
            chain.addAction(new NodeLabelIteratorAction());
        }
        if (element instanceof INamedElement)
        {
            setLabel(((INamedElement)element).getNameWithAlias());
        }
        addPresentation(element);
        lookupContent.add(new ObjectSelectable());
    }

    public Widget getAttachedNodeWidget()
    {
        return nodeWidget;
    }
    
    @Override
    public void setLabel(String label)
    {
        super.setLabel(label);
        updateLocation = true;
    }

    @Override
    public Lookup getLookup()
    {
        return lookup;
    }

    @Override
    public void refresh(boolean resizetocontent)
    {
        //this is called ONLY from diagram loading logic..
        updateLocation = true;
        diagramLoading = true;
        HashMap map = getPersistenceProperties();
        if (map != null)
        {
            if (map.containsKey(UMLNodeWidget.LOCATION))
            {
                Point pt = (Point) map.get(UMLNodeWidget.LOCATION);
                this.setPreferredLocation(pt);
            }
            if (map.containsKey(UMLNodeWidget.GRANDPARENTLOCATION)) //we need this only in case of combined fragments
            {
                grandParentLocationExists = true;
                Object obj = map.get(UMLNodeWidget.GRANDPARENTLOCATION);
                if (obj instanceof Point)
                {
                    grandParentLoc = (Point)obj;
                }      
            }
            updateDiff();
        }          
    }

    private void updateDiff()
    {
        Point nodeLoc = nodeWidget.getPreferredLocation();
        Point labLoc = this.getPreferredLocation();
        if (nodeLoc != null && labLoc != null && !grandParentLocationExists)
        {
            diffX = nodeLoc.x - labLoc.x;
            diffY = nodeLoc.y - labLoc.y;
        }
        if (nodeLoc != null && labLoc != null && grandParentLocationExists && grandParentLoc != null)
        {
            grandParent = PersistenceUtil.getParentUMLNodeWidget(nodeWidget);
            Point cfLoc = grandParent.getPreferredLocation();
            diffX = cfLoc.x - labLoc.x;
            diffY = cfLoc.y - labLoc.y;
        }
    }
    
    @Override
    protected void paintWidget()
    {
        super.paintWidget();
        if (updateLocation)
        {
            updateLabelLocation();
        }
        updateLocation = false;
    }

    public void revalidateDependency()
    {
        if (nodeWidget == null || nodeWidget.getPreferredLocation() == null || getParentWidget() == null)
        {
            return;
        }
        updateLabelLocation();
    }

    private void updateLabelLocation()
    {
        Point point = new Point();
        Insets insets = nodeWidget.getBorder().getInsets();
        Rectangle labelBnd=getBounds();
        if(labelBnd==null)labelBnd=getPreferredBounds();
        Rectangle nodeBnd=nodeWidget.getBounds();
        //if(nodeBnd==null && )nodeBnd=nodeWidget.getPreferredBounds();
        if(nodeBnd==null)return;
        nodeBnd=nodeWidget.convertLocalToScene(nodeBnd);
        nodeBnd=getParentWidget().convertSceneToLocal(nodeBnd);//in parent of label coordinates
        if (getPreferredLocation() == null)
        {
            if(x0!=null)
            {
                dx=x0;
            }
            else dx = 0;
            if(y0==null)
            {
                dy = -labelBnd.height / 2 - nodeBnd.height / 2;
            }
            else dy=y0;

        }        
        double nodeCenterX = nodeBnd.x + insets.left + (nodeBnd.width - insets.left - insets.right) / 2;
        double nodeCenterY = nodeBnd.y + insets.bottom + (nodeBnd.height - insets.top - insets.bottom) / 2;
        
        if (getPreferredLocation() != null && diagramLoading && !grandParentLocationExists)
        {
            point = new Point((int) (nodeWidget.getPreferredLocation().x - diffX),
                    (int) (nodeWidget.getPreferredLocation().y - diffY));
        }
        else if (getPreferredLocation() != null 
                && diagramLoading 
                && grandParentLocationExists 
                && grandParent != null 
                && grandParent.getPreferredLocation() != null 
                && grandParentLoc != null)
        {
            //for now.. this is only for combinedfragments
            Point loc = grandParent.getPreferredLocation();
            int x = loc.x - diffX;
            int y = loc.y - diffY;
            point = new Point(x,y);
        }        
        else
        {
            point = new Point((int) (nodeCenterX + dx - labelBnd.width / 2),
                    (int) (nodeCenterY + dy - labelBnd.height / 2));
        }
        setPreferredLocation(point);
        getScene().revalidate();
    }

    private void updateDistance()
    {
        Insets insets = nodeWidget.getBorder().getInsets();
        Rectangle nodeBnd=nodeWidget.getBounds();
        if(nodeBnd==null)nodeBnd=nodeWidget.getPreferredBounds();
        nodeBnd=nodeWidget.convertLocalToScene(nodeBnd);
        nodeBnd=getParentWidget().convertSceneToLocal(nodeBnd);//in parent of label coordinates

        double nodeCenterX = nodeBnd.x+ insets.left + (nodeBnd.width - insets.left - insets.right) / 2;

        double nodeCenterY = nodeBnd.y+ insets.bottom + (nodeBnd.height - insets.top - insets.bottom) / 2;

        dx = -nodeCenterX + getLocation().x + getPreferredBounds().width / 2;
        dy = -nodeCenterY + getLocation().y + getPreferredBounds().height / 2;
    }
    
    protected void updateDistance(double dx, double dy)
    {
        this.dx = dx;
        this.dy = dy;
    }
    
    public double getCenterDx()
    {
        return dx;
    }
    
    public double getCenterDy()
    {
        return dy;
    }
    
    @Override
    protected void notifyStateChanged(ObjectState previousState, ObjectState state)
    {
        if((previousState.isSelected() == false) && (state.isSelected() == true))
        {
            // Going from not selected to selected.
            // Need to remove the background and changed the font back to the 
            // standard color.
            setOpaque(true);
            
            setBackground(UIManager.getColor("List.selectionBackground"));
            initialBeforeSelectionFG=getForeground();
            setForeground(UIManager.getColor("List.selectionForeground"));
             
            setBorder(BorderFactory.createLineBorder(1, BORDER_HILIGHTED_COLOR));
        }
        else if((previousState.isSelected() == true) && (state.isSelected() == false))
        {
            // Going from selected to not selected
            setOpaque(false);
            setForeground(initialBeforeSelectionFG);
            
            
            setBorder(BorderFactory.createEmptyBorder(1));
        }
    }
    private void addPresentation(IElement element)
    {
        Scene scene = getScene();
        if (scene instanceof ObjectScene)
        {
            IPresentationElement presentation = createPresentationElement();
            presentation.addSubject(element);
            
            ObjectScene objectScene = (ObjectScene)scene;
            objectScene.addObject(presentation, this);
        }

    }
    private IPresentationElement createPresentationElement()
    {
        IPresentationElement retVal = null;
        
        ICreationFactory factory = FactoryRetriever.instance().getCreationFactory();
        if(factory != null)
        {
           Object presentationObj = factory.retrieveMetaType("NodePresentation", null);
           if (presentationObj instanceof IPresentationElement)
           {
                  retVal = (IPresentationElement)presentationObj;    
           }
        }
        
        return retVal;
    }

    public void save(NodeWriter nodeWriter)
    {
        if (!(this.isVisible()))
            return;
        
        nodeWriter = PersistenceUtil.populateNodeWriter(nodeWriter, this);
        nodeWriter.setTypeInfo("MovableLabel");
        nodeWriter.setHasPositionSize(true);
        PersistenceUtil.populateProperties(nodeWriter, this);
        nodeWriter.beginGraphNode();
        nodeWriter.endGraphNode();
    }
    
    public void saveChildren(Widget widget, NodeWriter nodeWriter) {
        //not applicable
    }

    private class LabelMoveSupport implements MoveStrategy, MoveProvider
    {

        private LayerWidget interractionLayer;
        private ConnectionWidget lineWidget;
        private Widget nodeWidget;

        public LabelMoveSupport(LayerWidget interractionLayer, Widget nodeWidget)
        {
            this.interractionLayer = interractionLayer;
            this.nodeWidget = nodeWidget;
        }

        public void movementStarted(Widget widget)
        {
            show();
        }

        public void movementFinished(Widget widget)
        {
            hide();
            updateDistance();
            updateDiff();
            
            if (origLoc != null)
            {
                Point finalLoc = widget.getPreferredLocation();
                if (finalLoc != null)
                {
                    if ( (Math.abs(finalLoc.x - origLoc.x) > 5) || (Math.abs(finalLoc.y - origLoc.y) > 5))
                    {                        
                        ((DesignerScene)nodeWidget.getScene()).getEngine().getTopComponent().setDiagramDirty(true);
                    }
                }
            }
        }

        public Point getOriginalLocation(Widget widget)
        {
            origLoc =  widget.getPreferredLocation();
            return origLoc;
        }

        public void setNewLocation(Widget widget, Point location)
        {
            widget.setPreferredLocation(location);
        }

        public Point locationSuggested(Widget widget, Point originalLocation, Point suggestedLocation)
        {
            Point labelLocation = widget.getLocation();
            Rectangle widgetBounds = widget.getBounds();
            Rectangle labelBounds = widget.convertLocalToScene(widgetBounds);

            Rectangle nodeBounds = nodeWidget.getBounds();
            nodeBounds = nodeWidget.convertLocalToScene(nodeBounds);
            nodeBounds.getCenterX();
            labelBounds.translate(suggestedLocation.x - labelLocation.x, suggestedLocation.y - labelLocation.y);

            ArrayList<Point> controlPoints = new ArrayList<Point>();
            controlPoints.add(new Point((int) nodeBounds.getCenterX(), (int) nodeBounds.getCenterY()));
            controlPoints.add(new Point((int) labelBounds.getCenterX(), (int) labelBounds.getCenterY()));
            lineWidget.setControlPoints(controlPoints, true);
            return suggestedLocation;
        }

        public void show()
        {
            if (interractionLayer != null)
            {
                if (lineWidget == null)
                {
                    lineWidget = createLineWidget(interractionLayer.getScene());
                }
                interractionLayer.addChild(lineWidget);
            }
        }

        public void hide()
        {
            if (interractionLayer != null)
            {
                lineWidget.removeFromParent();
            }
        }

        private ConnectionWidget createLineWidget(Scene scene)
        {
            ConnectionWidget widget = new ConnectionWidget(scene);
            widget.setStroke(DiagramEngine.ALIGN_STROKE);
            widget.setForeground(Color.GRAY);
            return widget;
        }
    }
}
