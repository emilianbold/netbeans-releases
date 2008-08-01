/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.uml.diagrams.edges;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author treyspiva
 */
public class ConnectionAnchor extends Anchor
{

    public ConnectionAnchor(ConnectionWidget connection)
    {
        super(connection);
    }

    /**
     * Returns a scene location of a related widget.
     * @return the scene location; null if no related widget is assigned
     */
    public Point getRelatedSceneLocation () 
    {
        return getLocationPoint(0.5);
    }
    
    @Override
    public Anchor.Result compute(Entry entry)
    {
        
        Point center = getLocationPoint(0.5);
        return new Anchor.Result(center, Anchor.DIRECTION_ANY);
    }

    protected Point getLocationPoint(double location)
    {
        ConnectionWidget connectionWidget = (ConnectionWidget) getRelatedWidget();
        
        java.util.List<Point> controlPoints = connectionWidget.getControlPoints();
        boolean empty = controlPoints == null || controlPoints.size() <= 0;

        double totalDistance = 0.0;
        double[] distances = new double[empty ? 0 : controlPoints.size() - 1];
        for (int i = 0; i < distances.length; i++)
        {
            distances[i] = totalDistance += GeomUtil.distanceSq(controlPoints.get(i), controlPoints.get(i + 1));
        }

        // For now I always want the center of the connectoin widget.
        double lineDistance = totalDistance * location;

        return getLinePointDistance(distances, (int)lineDistance, controlPoints);
    }
    
    private Point getLinePointDistance(double[] distances, 
                                       int lineDistance, 
                                       java.util.List<Point> controlPoints)
    {
        Point retVal = new Point();
        
        if(distances.length > 0)
        {
            int index = distances.length - 1;
            for (int i = 0; i < distances.length; i++)
            {
                if (lineDistance < distances[i])
                {
                    index = i;
                    break;
                }
            }

            double segmentStartDistance = index > 0 ? distances[index - 1] : 0;
            double segmentLength = distances[index] - segmentStartDistance;
            double segmentDistance = lineDistance - segmentStartDistance;

            if (segmentLength == 0.0)
            {
                retVal = controlPoints.get(index);
            }
            else
            {
                Point p1 = controlPoints.get(index);
                Point p2 = controlPoints.get(index + 1);

                double segmentFactor = segmentDistance / segmentLength;

                retVal = new Point((int) (p1.x + (p2.x - p1.x) * segmentFactor), 
                                   (int) (p1.y + (p2.y - p1.y) * segmentFactor));
            }
        }
        
        return retVal;
    }
}
