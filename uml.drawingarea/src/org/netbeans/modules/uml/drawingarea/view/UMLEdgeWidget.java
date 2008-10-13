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

import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import javax.accessibility.AccessibleRole;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.anchor.AnchorShapeFactory;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.drawingarea.LabelManager;
import org.netbeans.modules.uml.drawingarea.persistence.api.DiagramEdgeReader;
import org.netbeans.modules.uml.drawingarea.persistence.api.DiagramEdgeWriter;
import org.netbeans.modules.uml.drawingarea.persistence.data.EdgeInfo;
import org.netbeans.modules.uml.drawingarea.persistence.EdgeWriter;
import org.netbeans.modules.uml.drawingarea.persistence.PersistenceUtil;
import org.netbeans.modules.uml.drawingarea.ui.addins.diagramcreator.SQDDiagramEngineExtension;
import org.openide.util.Lookup;

/**
 *
 * @author Jyothi
 */
public abstract class UMLEdgeWidget extends ConnectionWidget implements DiagramEdgeWriter, DiagramEdgeReader, UMLWidget
{

    DesignerScene scene;
    protected static final String STEREOTYPE = "Stereotype"; //NOI18N
    protected static final String NAME = "Name"; //NOI18N
    protected static final String TAGGEDVALUE = "TaggedValue"; //NOI18N
    protected static final String OPERATION = "Operation"; //NOI18N
    public final static AnchorShape ARROW_END = AnchorShapeFactory.createArrowAnchorShape(50, 10);
    public static final String LABEL_TYPE = "LABEL_TYPE"; //NOI18N
    
    //key to specify that a proxy pres elt should be created while loading the diagram. Eg: Nested Link
    public static final String PROXY_PRESENTATION_ELEMENT = "PROXY_PRESENTATION_ELEMENT";  //NOI18N

    public UMLEdgeWidget(Scene scene)
    {
        super(scene);
        this.scene = (DesignerScene) scene;
        setAccessibleContext(new UMLEdgeWidgetAccessibleContext(this));
    }

    protected LabelManager getLabelManager()
    {
        LabelManager manager = null;
        Lookup lookup = this.getLookup();
        if (lookup != null)
        {
            manager = lookup.lookup(LabelManager.class);
            if (manager != null)
            {
                return manager;
            }
        }
        return manager;
    }

    @Override
    public void setControlPoints(Collection<Point> controlPoints, boolean sceneLocations) {
        if (scene != null && scene.getEngine() instanceof SQDDiagramEngineExtension)
        {
            super.setControlPoints(controlPoints, sceneLocations);
        }
        else 
        {
            int ctrlPtCount = this.getControlPoints().size();
            super.setControlPoints(controlPoints, sceneLocations);
            //Mark the diagram dirty after setting control points
            if (scene != null && ctrlPtCount != this.getControlPoints().size()) 
            {
                ((DesignerScene) scene).getDiagram().setDirty(true);
            }
        }
    }

    public void save(EdgeWriter edgeWriter)
    {
        IElement modElt = PersistenceUtil.getModelElement(this);
        if (modElt == null)
            return;
        else if (modElt != null)
            edgeWriter.setElementType(modElt.getElementType());
        IPresentationElement pElt = PersistenceUtil.getPresentationElement(this);
        if (pElt != null)
            edgeWriter.setPresentation(pElt.getFirstSubjectsType());
        
        edgeWriter.setLocation(this.getLocation());
        List<Point> controlPts = ((ConnectionWidget) this).getControlPoints();
        edgeWriter.setWayPoints(controlPts);
        edgeWriter.setPEID(PersistenceUtil.getPEID(this));
        edgeWriter.setMEID(PersistenceUtil.getMEID(this));
        
        edgeWriter.setSrcAnchorID(PersistenceUtil.findAnchor(this.getSourceAnchor()));
        edgeWriter.setTargetAnchorID(PersistenceUtil.findAnchor(this.getTargetAnchor()));
        
        edgeWriter.beginGraphEdge();
        LabelManager manager = getLabelManager();
        if (manager != null)
        {
            HashMap<String, Widget> labMap = manager.getLabelMap();
            edgeWriter.beginContained();
            for (String child : labMap.keySet())
            {
                Widget childWidget = labMap.get(child);
                String childType = child.substring(0, child.indexOf('_'));
                if (childWidget instanceof DiagramEdgeWriter)
                {
                    //begin contained  
                    edgeWriter.setTypeInfo(childType);

                    HashMap map = edgeWriter.getProperties();
                    map.put(LABEL_TYPE, child);
                    edgeWriter.setProperties(map);

                    ((DiagramEdgeWriter) childWidget).save(edgeWriter);
                }
                else
                {
//                    System.out.println(" not a label... ");
                }
            }
            edgeWriter.endContained();
        }
        //write anchor
        edgeWriter.writeEdgeAnchors();

        edgeWriter.endGraphEdge();
    }

    public void load(EdgeInfo edgeReader)
    {
        //scene.setEdgeSource(pE, srcTargetPEs.get("Source"));
        if (scene != null)
        {
            Collection<IPresentationElement> nodesList = scene.getNodes();
            if ( nodesList != null )
            {
                if (!(nodesList.contains(edgeReader.getSourcePE())) 
                        || !(nodesList.contains(edgeReader.getTargetPE())))
                {
//                    System.out.println(" invalid edge...");
                    return;
                }
            }
            scene.setEdgeSource(PersistenceUtil.getPresentationElement(this), edgeReader.getSourcePE());
            scene.setEdgeTarget(PersistenceUtil.getPresentationElement(this), edgeReader.getTargetPE());
            setControlPoints(edgeReader.getWayPoints(), true);
        }
        LabelManager manager = getLabelManager();
        if (manager != null)
        {
            List<EdgeInfo.EdgeLabel> edgeLabels = edgeReader.getLabels();
            
            for (Iterator<EdgeInfo.EdgeLabel> it = edgeLabels.iterator(); it.hasNext();)
            {
                EdgeInfo.EdgeLabel edgeLabel = it.next();
                String labelTypeStr = edgeLabel.getLabelProperties().get(LABEL_TYPE); 
                LabelManager.LabelType labelType = null;
                if (labelTypeStr != null && labelTypeStr.trim().length() > 0)
                {                    
                    if (labelTypeStr.endsWith(LabelManager.LabelType.SOURCE.toString()))
                        labelType = LabelManager.LabelType.SOURCE;
                    else if (labelTypeStr.endsWith(LabelManager.LabelType.TARGET.toString()))
                        labelType = LabelManager.LabelType.TARGET;
                    else if (labelTypeStr.endsWith(LabelManager.LabelType.EDGE.toString()))
                        labelType = LabelManager.LabelType.EDGE;
                }
                    manager.showLabel(edgeLabel.getLabel(), labelType, edgeLabel.getPosition());
            }
        }
    }

    public void duplicate(Widget copy)
    {
        assert copy instanceof UMLEdgeWidget;
        
        //((UMLEdgeWidget)copy).initialize(((UMLEdgeWidget)copy).getObject());
        ((ConnectionWidget) copy).setControlPointsCursor(this.getControlPointsCursor());
        ConnectionWidget dup = (ConnectionWidget) copy;

        Anchor sourceAnchor = dup.getSourceAnchor();
        Anchor targetAnchor = dup.getTargetAnchor();
        if (sourceAnchor == null || targetAnchor == null)
        {
            return;
        }

        List<Point> list = new ArrayList<Point>();

        ArrayList<Point> oldList = new ArrayList<Point>(getControlPoints());
        oldList.remove(getFirstControlPoint());
        oldList.remove(getLastControlPoint());

        Point sourceP = sourceAnchor.compute(dup.getSourceAnchorEntry()).getAnchorSceneLocation();
        list.add(sourceP);

        int dx = sourceAnchor.getRelatedSceneLocation().x - getSourceAnchor().getRelatedSceneLocation().x;
        int dy = sourceAnchor.getRelatedSceneLocation().y - getSourceAnchor().getRelatedSceneLocation().y;

        for (Point p: oldList)
        {
            Point np = new Point(p.x + dx, p.y + dy);
            list.add(np);
        }
        list.add(targetAnchor.compute(dup.getTargetAnchorEntry()).getAnchorSceneLocation());

        dup.setControlPoints(list, true);        
    }

    public IPresentationElement getObject()
    {
        return (IPresentationElement) scene.findObject(this);
    }

    public void remove()
    {
        Widget sourceWidget = getSourceAnchor().getRelatedWidget();
        Widget targetWidget = getTargetAnchor().getRelatedWidget();
        scene.removeEdge(getObject());
        
        // Fixed iz #139489 and 148365
        // removed entries on source and target widget that are attatched to this edge.
        if (sourceWidget != null && sourceWidget instanceof UMLNodeWidget)
        {
            ((UMLNodeWidget)sourceWidget).removeAttachedEntries(this);
        }
         if (targetWidget != null && targetWidget instanceof UMLNodeWidget)
        {
            ((UMLNodeWidget)targetWidget).removeAttachedEntries(this);
        }
    }

    public void refresh(boolean resizetocontent)
    {
        IPresentationElement pe = getObject();
        if (pe == null || pe.getFirstSubject() == null)
        {
            remove();
            scene.validate();
        }
    }
    abstract public void initialize(IPresentationElement element);

    /**
     * Fix for 148994 - workaround for 6755974. Override of ConnectionWidget.paintWidget() 
     * to paint in "scaled out" mode.
     * Paints the connection widget (the path, the anchor shapes, the control points, the end points).
     */
    protected void paintWidget () {
        Graphics2D gr = getGraphics ();
        gr.setColor (getForeground ());
        GeneralPath path = null;

        Point firstControlPoint = getFirstControlPoint ();
        Point lastControlPoint = getLastControlPoint ();
                
        //checking to see if we should draw line through the AnchorShape. If the 
        //AnchorShape is hollow, the cutdistance will be true.
        boolean isSourceCutDistance = getSourceAnchorShape().getCutDistance () != 0.0;
        boolean isTargetCutDistance = getTargetAnchorShape().getCutDistance () != 0.0;
        
        double firstControlPointRotation = 
                firstControlPoint != null  &&  (getSourceAnchorShape().isLineOriented ()  
                ||  isSourceCutDistance) ? 
                    getSourceAnchorShapeRotation () : 0.0;
        double lastControlPointRotation = 
                lastControlPoint != null  
                &&  (getTargetAnchorShape().isLineOriented () || isTargetCutDistance) ? 
                    getTargetAnchorShapeRotation () : 0.0;
        
        List<Point> points;
        if ((isSourceCutDistance  ||  isTargetCutDistance)  &&  getControlPoints().size () >= 2) {
            points = new ArrayList<Point> (getControlPoints());
            points.set (0, new Point (
                firstControlPoint.x + (int) (getSourceAnchorShape().getCutDistance () * Math.cos (firstControlPointRotation)),
                firstControlPoint.y + (int) (getSourceAnchorShape().getCutDistance () * Math.sin (firstControlPointRotation))
            ));
            points.set (getControlPoints().size () - 1, new Point (
                lastControlPoint.x + (int) (getTargetAnchorShape().getCutDistance () * Math.cos (lastControlPointRotation)),
                lastControlPoint.y + (int) (getTargetAnchorShape().getCutDistance () * Math.sin (lastControlPointRotation))
            ));
        } else {
            points = getControlPoints();
        }

        if (getControlPointCutDistance() > 0) {
            for (int a = 0; a < points.size () - 1; a ++) {
                Point p1 = points.get (a);
                Point p2 = points.get (a + 1);
                double len = p1.distance (p2);

                if (a > 0) {
                    Point p0 = points.get (a - 1);
                    double ll = p0.distance (p1);
                    if (len < ll)
                        ll = len;
                    ll /= 2;
                    double cll = getControlPointCutDistance();
                    if (cll > ll)
                        cll = ll;
                    double direction = Math.atan2 (p2.y - p1.y, p2.x - p1.x);
                    if (!Double.isNaN (direction)) {
                        path = addToPath (path,
                                p1.x + (int) (cll * Math.cos (direction)),
                                p1.y + (int) (cll * Math.sin (direction))
                        );
                    }
                } else {
                    path = addToPath (path, p1.x, p1.y);
                }

                if (a < points.size () - 2) {
                    Point p3 = points.get (a + 2);
                    double ll = p2.distance (p3);
                    if (len < ll)
                        ll = len;
                    ll /= 2;
                    double cll = getControlPointCutDistance();
                    if (cll > ll)
                        cll = ll;
                    double direction = Math.atan2 (p2.y - p1.y, p2.x - p1.x);
                    if (!Double.isNaN (direction)) {
                        path = addToPath (path,
                                p2.x - (int) (cll * Math.cos (direction)),
                                p2.y - (int) (cll * Math.sin (direction))
                        );
                    }
                } else {
                    path = addToPath (path, p2.x, p2.y);
                }
            }
        } else {
            for (Point point : points)
                path = addToPath (path, point.x, point.y);
        }
        if (path != null) {

            double zoomFactor = getScene().getZoomFactor ();
            AffineTransform oldTransform = null;
            if (zoomFactor <= 0.25 ) 
            {
                oldTransform = gr.getTransform ();
                gr.scale (1/zoomFactor, 1/zoomFactor );
                path.transform(new AffineTransform(zoomFactor, 0, 0, zoomFactor, 0, 0)); 
            }

            Stroke previousStroke = gr.getStroke ();
            gr.setPaint (getForeground ());
            gr.setStroke (getStroke ());
            gr.draw (path);
            gr.setStroke (previousStroke);

            if (oldTransform != null) {
                gr.setTransform (oldTransform);
            }

        }


        AffineTransform previousTransform;

        if (firstControlPoint != null) {
            previousTransform = gr.getTransform ();
            gr.translate (firstControlPoint.x, firstControlPoint.y);
            if (getSourceAnchorShape().isLineOriented ())
                gr.rotate (firstControlPointRotation);
            getSourceAnchorShape().paint (gr, true);
            gr.setTransform (previousTransform);
        }

        if (lastControlPoint != null) {
            previousTransform = gr.getTransform ();
            gr.translate (lastControlPoint.x, lastControlPoint.y);
            if (getTargetAnchorShape().isLineOriented ())
                gr.rotate (lastControlPointRotation);
            getTargetAnchorShape().paint (gr, false);
            gr.setTransform (previousTransform);
        }

        if (isPaintControlPoints()) {
            int last = getControlPoints().size () - 1;
            for (int index = 0; index <= last; index ++) {
                Point point = getControlPoints().get (index);
                previousTransform = gr.getTransform ();
                gr.translate (point.x, point.y);
                if (index == 0  ||  index == last)
                    getEndPointShape().paint (gr);
                else
                    getControlPointShape().paint (gr);
                gr.setTransform (previousTransform);
            }
        }
    }

    private GeneralPath addToPath (GeneralPath path, int x, int y) {
        if (path == null) {
            path = new GeneralPath ();
            path.moveTo (x, y);
        } else {
            path.lineTo (x, y);
        }
        return path;
    }


    /**
     * Returns the rotation of the source anchor shape.
     * @return the source anchor shape rotation
     */
    private double getSourceAnchorShapeRotation () {
        if (getControlPoints().size () <= 1)
            return 0.0;
        Point point1 = getControlPoints().get (0);
        Point point2 = getControlPoints().get (1);
        return Math.atan2 (point2.y - point1.y, point2.x - point1.x);
    }

    /**
     * Returns the rotation of the target anchor shape.
     * @return the target anchor shape rotation
     */
    public double getTargetAnchorShapeRotation () {
        int size = getControlPoints().size ();
        if (size <= 1)
            return 0.0;
        Point point1 = getControlPoints().get (size - 1);
        Point point2 = getControlPoints().get (size - 2);
        return Math.atan2 (point2.y - point1.y, point2.x - point1.x);
    }


    ///////////// 
    // Accessible
    /////////////
   

    public class UMLEdgeWidgetAccessibleContext extends UMLWidgetAccessibleContext
    {
        public UMLEdgeWidgetAccessibleContext(Widget w) 
        {
            super(w);
        }

        public AccessibleRole getAccessibleRole () {
            return UMLAccessibleRole.GRAPH_EDGE;
        }
        
    }
    
}
