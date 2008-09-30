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
package org.netbeans.modules.uml.diagrams.edges;

import java.awt.Point;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.anchor.AnchorShapeLocationResolver;
import org.netbeans.modules.uml.diagrams.anchors.DiamondAnchorShape;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.anchor.AnchorShapeFactory;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.model.ObjectScene;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAggregation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociation;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IAssociationEnd;
import org.netbeans.modules.uml.core.metamodel.infrastructure.coreinfrastructure.IClassifier;
import org.netbeans.modules.uml.drawingarea.LabelManager;
import org.netbeans.modules.uml.drawingarea.ModelElementChangedKind;
import org.netbeans.modules.uml.drawingarea.actions.WidgetContext;
import org.netbeans.modules.uml.drawingarea.actions.WidgetContextFactory;
import org.netbeans.modules.uml.drawingarea.persistence.EdgeWriter;
import org.netbeans.modules.uml.drawingarea.persistence.PersistenceUtil;
import org.netbeans.modules.uml.drawingarea.persistence.data.EdgeInfo;
import org.netbeans.modules.uml.drawingarea.view.UMLEdgeWidget;
import org.openide.util.Lookup;

/**
 *
 * @author treyspiva
 */
public class AssociationConnector extends AbstractUMLConnectionWidget
{

//    private final static AnchorShape NAVIGABLE_END = AnchorShapeFactory.createArrowAnchorShape(50, 10);
    private final static AnchorShape NAVIGABLE_END = UMLEdgeWidget.ARROW_END;
    private QualifierLabelWidget sourceQualifier = null;
    private QualifierLabelWidget targetQualifier = null;
    
    private AnchorShapeLocationResolver sourceLocationResolver = null;
    private AnchorShapeLocationResolver targetLocationResolver = null;
    
    private static final String NAME = "Name"; //NOI18N
    private static final String END_NAME = "End Name"; //NOI18N    
    private static final String MULTIPLICITY = "Multiplicity"; //NOI18N
    private static final String STEREOTYPE = "Stereotype"; //NOI18N
    

    public AssociationConnector(Scene scene)
    {
        super(scene);
        
        setForeground(Color.BLACK);
        
        setSourceAnchorShape(AnchorShape.NONE);
        setTargetAnchorShape(AnchorShape.NONE);
        setControlPointShape(PointShape.SQUARE_FILLED_BIG);
        setEndPointShape(PointShape.SQUARE_FILLED_BIG);
        
        addToLookup(new AssociationWidgetContext());

    }

    @Override
    public void initialize(IPresentationElement element)
    {
        if (element != null)
        {
            IAssociation assoc = (IAssociation) element.getFirstSubject();
            updateEnds(assoc);
        }
    }

    @Override
    public Lookup getLookup()
    {
        return super.getLookup();
    }

    @Override
    protected LabelManager createLabelManager()
    {
        return new AssociationLabelManager(this);
    }

    protected void updateEnds(IAssociation element)
    {
        if (element instanceof IAggregation)
        {
            updateAggregationEnds((IAggregation) element);
        }
        else
        {
            updateAssociationEnds(element);
        }

        setControlPointShape(PointShape.SQUARE_FILLED_BIG);
    }

    public static boolean isSourceEnd(ConnectionWidget widget,
                                        IAssociationEnd end)
    {
        boolean retVal = false;

        if ((widget == null) || (end == null))
        {
            throw new NullPointerException();
        }
        Widget w=null;
        if (widget.getSourceAnchor() != null)
        {
            w = widget.getSourceAnchor().getRelatedWidget();
        }
        if (w != null && (w.getScene() instanceof ObjectScene))
        {
            ObjectScene scene = (ObjectScene) w.getScene();
            IPresentationElement element = (IPresentationElement) scene.findObject(w);
            retVal = end.isSameParticipant(element.getFirstSubject());
        }
        else if(w==null)
        {
            //do not return always false, but try to assume from end position in ends list
            IAssociation assoc=end.getAssociation();
            if(assoc.getEndIndex(end)==0)retVal=true;
        }

        return retVal;
    }

        public static boolean isTargetEnd(ConnectionWidget widget,
                                        IAssociationEnd end)
    {
        boolean retVal = false;

        if ((widget == null) || (end == null))
        {
            throw new NullPointerException();
        }
        Widget w=null;
        if (widget.getTargetAnchor() != null)
        {
            w = widget.getTargetAnchor().getRelatedWidget();
        }
        if (w != null && (w.getScene() instanceof ObjectScene))
        {
            ObjectScene scene = (ObjectScene) w.getScene();
            IPresentationElement element = (IPresentationElement) scene.findObject(w);
            retVal = end.isSameParticipant(element.getFirstSubject());
        }
        else if(w==null)
        {
            //do not return always false, but try to assume from end position in ends list
            IAssociation assoc=end.getAssociation();
            if(assoc.getEndIndex(end)==1)retVal=true;
        }


        return retVal;
    }
        
    @Override
    public void propertyChange(PropertyChangeEvent evt)
    {
        String propName = evt.getPropertyName();
        if (propName.equals(ModelElementChangedKind.ASSOCIATION_END_TRANDFORMED.toString()))
        {
            updateEnds((IAssociation) evt.getSource());
        }
        else if (propName.equals(ModelElementChangedKind.RELATION_CREATED.toString()) == true)
        {
            updateEnds((IAssociation) evt.getSource());
        }
        else if (propName.equals(ModelElementChangedKind.QUALIFIER_ADDED.toString()) == true)
        {
            updateQualifier(evt);
        }
        else
        {
            super.propertyChange(evt);
        }
    }

    private void updateQualifier(PropertyChangeEvent evt)
    {
        GraphScene scene = (GraphScene)getScene();

        QualifierLabelWidget qualifier = null;
        
        
        if(isSourceEnd(this, (IAssociationEnd) evt.getSource()) == true)
        {
            
            if(sourceQualifier == null)
            {
                sourceQualifier = new QualifierLabelWidget(scene);
                
                addChild(sourceQualifier);
                
                setConstraint(sourceQualifier, 
                              LayoutFactory.ConnectionWidgetLayoutAlignment.CENTER_SOURCE,
                              0);

                sourceLocationResolver = new DirectRoutingAnchorResolver(sourceQualifier, true);

                AnchorShape shape = 
                        AnchorShapeFactory.createAdjustableAnchorShape(getSourceAnchorShape(), 
                                                                       sourceLocationResolver);
                setSourceAnchorShape(shape);
            }
            qualifier = sourceQualifier;
        }
        else
        {
            if(targetQualifier == null)
            {
                targetQualifier = new QualifierLabelWidget(scene);

                addChild(targetQualifier);
                
                setConstraint(targetQualifier, 
                              LayoutFactory.ConnectionWidgetLayoutAlignment.CENTER_TARGET,
                              -1);
                
                targetLocationResolver = new DirectRoutingAnchorResolver(targetQualifier, false);
                AnchorShape shape = 
                        AnchorShapeFactory.createAdjustableAnchorShape(getTargetAnchorShape(), 
                                                                       targetLocationResolver);
                setTargetAnchorShape(shape);
            }
            qualifier = targetQualifier;
        }
        
        qualifier.propertyChange(evt);
    }
    
    private void updateAssociationEnds(IAssociation element)
    {
        for (IAssociationEnd curEnd : element.getEnds())
        {
            if (isSourceEnd(this, curEnd) == true)
            {
                if (curEnd.getIsNavigable() == true)
                {
                    setSourceAnchorShape(NAVIGABLE_END);
                }
                else
                {
                    setSourceAnchorShape(AnchorShape.NONE);
                }
            }
            else if (isTargetEnd(this, curEnd) == true)
            {
                if (curEnd.getIsNavigable() == true)
                {
                    setTargetAnchorShape(NAVIGABLE_END);
                }
                else
                {
                    setTargetAnchorShape(AnchorShape.NONE);
                }
            }
        }
    }

    private void updateAggregationEnds(IAggregation element)
    {
        boolean filled = false;
        if (element.getIsComposite() == true)
        {
            filled = true;
        }

        for (IAssociationEnd curEnd : element.getEnds())
        {
            if (isSourceEnd(this, curEnd) == true)
            {
                if (element.isAggregateEnd(curEnd) == true)
                {
                    setSourceAnchorShape(new DiamondAnchorShape(12, filled, 12));
                }
                else if (curEnd.getIsNavigable() == true)
                {
                    setSourceAnchorShape(NAVIGABLE_END);
                }
                else
                {
                    setSourceAnchorShape(AnchorShape.NONE);
                }
            }
            else
            {
                if (element.isAggregateEnd(curEnd) == true)
                {
                    setTargetAnchorShape(new DiamondAnchorShape(12, filled, 12));
                }
                else if (curEnd.getIsNavigable() == true)
                {
                    setTargetAnchorShape(NAVIGABLE_END);
                }
                else
                {
                    setTargetAnchorShape(AnchorShape.NONE);
                }
            }
        }
    }

    protected IAssociationEnd getSourceEnd()
    {
        IAssociationEnd retVal = null;

        IAssociation assoc = getAssociation();
        if (assoc != null)
        {
            retVal = assoc.getEndAtIndex(0);
        }

        return retVal;
    }

    protected IAssociationEnd getTargetEnd()
    {
        IAssociationEnd retVal = null;

        IAssociation assoc = getAssociation();
        if (assoc != null)
        {
            retVal = assoc.getEndAtIndex(1);
        }

        return retVal;
    }

    protected IAssociation getAssociation()
    {
        ObjectScene scene = (ObjectScene) getScene();
        IPresentationElement presentation = (IPresentationElement) scene.findObject(this);
        return (IAssociation) presentation.getFirstSubject();
    }

    @Override  // we need to handle associations differently
    public void save(EdgeWriter edgeWriter)
    {
        edgeWriter.setLocation(this.getLocation());
        List<Point> controlPts = ((ConnectionWidget) this).getControlPoints();
        edgeWriter.setWayPoints(controlPts);
        edgeWriter.setPEID(PersistenceUtil.getPEID(this));
        edgeWriter.setMEID(PersistenceUtil.getMEID(this));
        edgeWriter.setElementType(PersistenceUtil.getModelElement(this).getElementType());

        edgeWriter.setSrcAnchorID(PersistenceUtil.findAnchor(this.getSourceAnchor()));
        edgeWriter.setTargetAnchorID(PersistenceUtil.findAnchor(this.getTargetAnchor()));

        //begin the association edge
        edgeWriter.beginGraphEdge();
        // now get all the children of the widget
        edgeWriter.beginContained();
        //now get all the associationEnds of this association
        IAssociation association = getAssociation();
        if (association != null)
        {
            LabelManager manager = getLabelManager();
            if (manager != null)
            {
                HashMap<String, String> map = new HashMap();
                //write sourceEnd info
                IAssociationEnd sourceEnd = getSourceEnd();         
                //create a hashmap with labelKeys, and typeInfos
                map.clear();
                map.put("End Name_SOURCE", NAME);
                map.put("Multiplicity_SOURCE", MULTIPLICITY);
                writeAssociationEnd(edgeWriter, manager, sourceEnd,map);
                
                //write targetEnd Info
                IAssociationEnd targetEnd = getTargetEnd();         
                //create a hashmap with labelKeys, and typeInfos
                map.clear();
                map.put("End Name_TARGET", NAME);
                map.put("Multiplicity_TARGET", MULTIPLICITY);
                writeAssociationEnd(edgeWriter, manager, targetEnd,map);
                
                //now write the association name
                writeAssociationChild(edgeWriter, manager, "Name_EDGE", NAME);
                //write stereotypes if any
                writeAssociationChild(edgeWriter, manager, "Stereotype_EDGE", STEREOTYPE);
                //write taggedvalues if any
                //TODO tagged values
            }
        }

        edgeWriter.endContained();
        //write anchor
        edgeWriter.writeEdgeAnchors();

        edgeWriter.endGraphEdge();
    }

    private void writeAssociationEnd(EdgeWriter edgeWriter, LabelManager manager, IAssociationEnd aEnd, HashMap<String, String> keyMap)
    {
        // set values and begin associationEnd graph node
        setValues(edgeWriter, this.getLocation(),
                this.getBounds().getSize(), "AssociationEndPEID",
                aEnd.getXMIID(), aEnd.getElementType(), null);
        edgeWriter.beginGraphNodeWithModelBridge();
        //begin contained
        edgeWriter.beginContained();
        //now write children if any
        if (keyMap != null && !keyMap.isEmpty())
        {
            for (String key : keyMap.keySet())
            {
                if (manager.getLabelMap().containsKey(key))
                {
                    writeAssociationChild(edgeWriter, manager, key, keyMap.get(key));
                }
            }
        }        
        //end contained
        edgeWriter.endContained();
        edgeWriter.endGraphNode(); //end the AssociationEnd graph node
    }
    
    private void writeAssociationChild(EdgeWriter edgeWriter, LabelManager manager, String key, String typeInfo)
    {
        Widget child = manager.getLabelMap().get(key);
        if (child != null)
        {
            setValues(edgeWriter,
                    child.getPreferredLocation(), child.getPreferredSize(),
                    key + "_PEID", null, null, typeInfo);
            edgeWriter.beginGraphNode();
            edgeWriter.endGraphNode();
        }
    }

    private void setValues(EdgeWriter edgeWriter, Point location,
                            Dimension size, String PEID, String MEID, String elementType, String typeInfo)
    {
        if (edgeWriter != null)
        {
            edgeWriter.setLocation(location);
            edgeWriter.setSize(size);
            edgeWriter.setPEID(PEID);
            edgeWriter.setMEID(MEID);
            edgeWriter.setElementType(elementType);
            edgeWriter.setTypeInfo(typeInfo);
            edgeWriter.setHasPositionSize(true);
        }
    }    

    @Override
    public void load(EdgeInfo edgeReader)
    {
        super.load(edgeReader);

        LabelManager manager = getLabelManager();
        if (manager != null)
        {
            List<EdgeInfo.EndDetails> ends = edgeReader.getEnds();
            for (Iterator<EdgeInfo.EndDetails> iter = ends.iterator(); iter.hasNext();)
            {
                LabelManager.LabelType labelType = LabelManager.LabelType.EDGE; //default is EDGE
                EdgeInfo.EndDetails endDetails = iter.next();
                if (endDetails.getID() != null)
                {
                    if (endDetails.getID().equalsIgnoreCase(getSourceEnd().getXMIID()))
                    {
                        //this is source end
                        labelType = LabelManager.LabelType.SOURCE;
                    }
                    if (endDetails.getID().equalsIgnoreCase(getTargetEnd().getXMIID()))
                    {
                        //this is target end
                        labelType = LabelManager.LabelType.TARGET;
                    }
                    List<EdgeInfo.EdgeLabel> endLabels = endDetails.getEndEdgeLabels();
                    for (Iterator<EdgeInfo.EdgeLabel> endLabIter = endLabels.iterator(); endLabIter.hasNext();)
                    {
                        EdgeInfo.EdgeLabel edgeLabel = endLabIter.next();
                        if (edgeLabel.getLabel().equalsIgnoreCase(NAME))
                        {
                            manager.showLabel(END_NAME, labelType, edgeLabel.getPosition());
                        }
                        else if (edgeLabel.getLabel().equalsIgnoreCase(MULTIPLICITY))
                        {
                            manager.showLabel(MULTIPLICITY, labelType, edgeLabel.getPosition());
                        }
                    }

                }
            }
        }
    }

    private class AssociationWidgetContext implements WidgetContextFactory
    {

        public WidgetContext findWidgetContext(Point localLocation)
        {
            WidgetContext retVal = null;

            // (LLS) Adding the buildContext logic to support A11Y issues.  The
            // user should be able to use the CTRL-F10 keystroke to activate
            // the context menu.  In the case of the keystroke the location
            // will not be valid.  Therefore, we have to just check if the
            // compartment is selected.
            if (localLocation != null)
            {
                if (isPointNearTheMiddle(localLocation) == true)
                {
                    retVal = new AssociationEdgeContext(LabelManager.LabelType.EDGE, null);
                }
            }
            else
            {
                // TODO: A11Y, do Something!!!!
            }

            if (retVal == null)
            {
                retVal = determineTheClosestEnd(localLocation);
            }


            return retVal;
        }

        protected boolean isPointNearTheMiddle(final Point pt)
        {
            Rectangle boundingRect = getBoundingArea();

            int regionWidth = boundingRect.width / 4;
            int regionHeight = boundingRect.height / 4;

            boundingRect.x += regionWidth;
            boundingRect.y += regionHeight;
            boundingRect.height = regionHeight * 2;
            boundingRect.width = regionWidth * 2;

            // We scale the bounding rect by 1/2.  If the point is inside the scaled rect
            // then we consider that we're too close to the middle to determine the edge so
            // we keep the flag indicating we're in the middle
            double nWidth = boundingRect.getWidth();
            double nHeight = boundingRect.getHeight();

            // Use the width and height to determine if we've got a close-to horizontal or
            // close to vertical edge
            boolean bWeAreNearMiddle = true;
            if (nWidth < 3)
            {
                int top = boundingRect.y;
                int bottom = top + boundingRect.height;
                if (!(top >= pt.getY() && bottom <= pt.getY()))
                {
                    bWeAreNearMiddle = false;
                }
            }
            else if (nHeight < 3)
            {
                double left = boundingRect.getX();
                double right = left + boundingRect.getWidth();
                if (!(left <= pt.getX() && right >= pt.getX()))
                {
                    bWeAreNearMiddle = false;
                }
            }
            else if (boundingRect.contains(pt) == false)
            {
                bWeAreNearMiddle = false;
            }
            return bWeAreNearMiddle;
        }
        
        private Rectangle getBoundingArea()
        {
            Rectangle retVal = calculateClientArea();
            
            if(sourceLocationResolver != null)
            {
                int delta = sourceLocationResolver.getEndLocation();
                retVal.x += delta;
                retVal.width -= delta;
            }
            
            if(targetLocationResolver != null)
            {
                int delta = targetLocationResolver.getEndLocation();
                retVal.width -= delta;
            }
            
            return retVal;
        }
        
        private WidgetContext determineTheClosestEnd(final Point pt)
        {
            // We're on the border of the edge.  Find what side.
            Anchor sourceAnchor = getSourceAnchor();
            Anchor targetAnchor = getTargetAnchor();

            Anchor.Result sourceResult = sourceAnchor.compute(getSourceAnchorEntry());
            Anchor.Result targetResult = targetAnchor.compute(getTargetAnchorEntry());

            Point fromPoint = sourceResult.getAnchorSceneLocation();
            Point toPoint = targetResult.getAnchorSceneLocation();

            Widget sourceWidget = getSourceAnchor().getRelatedWidget();
            Widget targetWidget = getTargetAnchor().getRelatedWidget();

            LabelManager.LabelType endName = LabelManager.LabelType.EDGE;

            if (fromPoint != null && toPoint != null)
            {
                Widget closesNodeWidget = null;

                double fromPointDistance = fromPoint.distanceSq(pt);
                double toPointDistance = toPoint.distanceSq(pt);

                if (fromPointDistance < toPointDistance)
                {
                    endName = LabelManager.LabelType.SOURCE;
                    closesNodeWidget = sourceWidget;
                }
                else
                {
                    endName = LabelManager.LabelType.TARGET;
                    closesNodeWidget = targetWidget;
                }

                if (closesNodeWidget != null && (sourceWidget != targetWidget))
                {
                    ObjectScene scene = (ObjectScene) closesNodeWidget.getScene();
                    IElement pElementNearPoint = (IElement) scene.findObject(closesNodeWidget);

                    if (pElementNearPoint != null)
                    {
                        IClassifier closesClassifier = pElementNearPoint instanceof IClassifier ? (IClassifier) pElementNearPoint : null;
                        if (closesClassifier != null)
                        {
                            endName = LabelManager.LabelType.TARGET;
                            IAssociationEnd sourceEnd = getSourceEnd();
                            if (sourceEnd != null)
                            {
                                boolean bIsSame = sourceEnd.isSameParticipant(closesClassifier);

                                if (bIsSame)
                                {
                                    endName = LabelManager.LabelType.SOURCE;
                                }
                            }
                        }
                    }
                }
            }

            IAssociationEnd end = null;
            if (endName == LabelManager.LabelType.SOURCE)
            {
                end = getSourceEnd();
            }
            else if (endName == LabelManager.LabelType.TARGET)
            {
                end = getTargetEnd();
            }
            return new AssociationEdgeContext(endName, end);
        }
    }

    private class AssociationEdgeContext implements WidgetContext
    {

        private LabelManager.LabelType contextName = LabelManager.LabelType.EDGE;
        private IAssociationEnd contextEnd = null;

        public AssociationEdgeContext(LabelManager.LabelType context,
                                        IAssociationEnd end)
        {
            contextName = context;
            contextEnd = end;
        }

        public String getContextName()
        {
            return contextName.toString();
        }

        public Object[] getContextItems()
        {
            Object[] retVal = null;

            if (contextEnd != null)
            {
                retVal = new Object[]{contextEnd};
            }
            else
            {
                retVal = new Object[0];
            }

            return retVal;
        }
    }

    public String getWidgetID()
    {
        return UMLWidgetIDString.ASSOCIATIONCONNECTORWIDGET.toString();
    }

    /**
     * The DirectRoutingAnchorResolver is used to determine the cut off distance
     * where the anchor shape should be placed.  The distance is calucated from
     * where the edge enters the related widget to the intersection point where
     * the edge will exist the AnchorShape.
     *
     * This resolver assumes a direct routing.
     */
    private class DirectRoutingAnchorResolver implements AnchorShapeLocationResolver
    {
        private boolean source = true;
        private Widget decoratorWidget;

        public DirectRoutingAnchorResolver(Widget widget, boolean source)
        {
            this.source = source;
            this.decoratorWidget = widget;
        }

        public int getEndLocation()
        {
            Point start = getRelatedControlPoint();
            Point end = getNextControlPoint();

            Point intersection = null;
            for (Line2D curLine : findPossibleLines())
            {
                if (Line2D.linesIntersect(start.x, start.y,
                                          end.x, end.y,
                                          curLine.getX1(), curLine.getY1(),
                                          curLine.getX2(), curLine.getY2()) == true)
                {
                    intersection = calculateIntersect(start.x, start.y,
                                                      end.x, end.y,
                                                      curLine.getX1(), curLine.getY1(),
                                                      curLine.getX2(), curLine.getY2());

                    break;
                }
            }
            if (intersection == null)
            {
                intersection = start;
            }
            return (int) Point.distance(start.x, start.y, intersection.x, intersection.y);
        }

        /**
         * Find the possible boundaries that the edge can cross.  Basically the
         * edge can not cross the boundary that is connected to the related
         * widget.
         *
         * @return An array of lines that are used to calculate the intersection
         *         point.
         */
        protected Line2D[] findPossibleLines()
        {
                Rectangle bounds = decoratorWidget.convertLocalToScene(decoratorWidget.getBounds());
            
            Widget relatedWidget = getSourceAnchor().getRelatedWidget();
            if(source == false)
            {
                relatedWidget = getTargetAnchor().getRelatedWidget();
            }

            Rectangle relatedBounds = relatedWidget.convertLocalToScene(relatedWidget.getBounds());
            Line2D[] retVal = new Line2D[3];
            int right = bounds.x + bounds.width;
            int bottom = bounds.y + bounds.height;
            if (bounds.x < relatedBounds.x)
            {
                // LEFT
                retVal[0] = new Line2D.Float(bounds.x, bounds.y, right, bounds.y);
                retVal[1] = new Line2D.Float(bounds.x, bounds.y, bounds.x, bottom);
                retVal[2] = new Line2D.Float(bounds.x, bottom, right, bottom);
            }
            else if (bounds.x > (relatedBounds.x + relatedBounds.width - (bounds.width / 3)))
            {
                // RIGHT
                retVal[0] = new Line2D.Float(bounds.x, bounds.y, right, bounds.y);
                retVal[1] = new Line2D.Float(right, bounds.y, right, bottom);
                retVal[2] = new Line2D.Float(bounds.x, bottom, right, bottom);
            }
            else if (bounds.y >= (relatedBounds.y + relatedBounds.height - (bounds.height / 3)))
            {
                // BOTTOM
                retVal[0] = new Line2D.Float(bounds.x, bottom, right, bottom);
                retVal[1] = new Line2D.Float(bounds.x, bounds.y, bounds.x, bottom);
                retVal[2] = new Line2D.Float(right, bounds.y, right, bottom);
            }
            else if (bounds.y < relatedBounds.y)
            {
                // TOP
                retVal[0] = new Line2D.Float(bounds.x, bounds.y, right, bounds.y);
                retVal[1] = new Line2D.Float(bounds.x, bounds.y, bounds.x, bottom);
                retVal[2] = new Line2D.Float(right, bounds.y, right, bottom);
            }
            return retVal;
        }

        /**
         * Calculates the point where the edge will intersect with a line.
         *
         * @param x1 Edge start x
         * @param y1 Edge start Y
         * @param x2 Edge end x
         * @param y2 Edge end y
         * @param x3 Line start x
         * @param y3 Line start y
         * @param x4 Line end x
         * @param y4 Line end y
         * @return
         */
        public Point calculateIntersect(double x1, double y1,
                                        double x2, double y2,
                                        double x3, double y3,
                                        double x4, double y4)
        {
            double line1Slope = calculateSlope(x1, y1, x2, y2);
            double line2Slope = calculateSlope(x3, y3, x4, y4);
            double line1c = calculateCoeffient(x1, y1, line1Slope);
            double line2c = calculateCoeffient(x3, y3, line2Slope);
            double x = (line2c - line1c) / (line1Slope - line2Slope);

            // Check if one of the lines is vertical.  If a line is
            // vertical then the intersection will of occur on the
            // horizonal X position.
            if (Double.isInfinite(line1c) == true)
            {
                x = (int) x1;
            } else if (Double.isInfinite(line2c) == true)
            {
                x = (int) x3;
            }
            double y = line1Slope * x + line1c;
            Point retVal = new Point((int) x, (int) y);
            return retVal;
        }

        private double calculateSlope(double x1, double y1, double x2, double y2)
        {
            double retVal = 0;
            
//            if((x2 - x1) != 0)
            {
                retVal = (y2 - y1) / (x2 - x1);
            }
            return retVal;
        }

        private double calculateCoeffient(double x, double y, double slope)
        {
            return y - (x * slope);
        }

        private Point getNextControlPoint()
        {
            Point retVal = getControlPoint(1);

            if(source == false)
            {
                retVal = getControlPoint(getControlPoints().size() - 2);
            }

            return retVal;
        }

        private Point getRelatedControlPoint()
        {
            Point retVal = getFirstControlPoint();

            if(source == false)
            {
                retVal = getLastControlPoint();
            }

            return retVal;
        }
    }
}
