package org.netbeans.modules.uml.drawingarea.view;

import java.awt.Point;
import java.awt.Rectangle;
import org.netbeans.api.visual.widget.Widget;

public class CircleShape implements WidgetShape
{

    private Widget sourceWidget = null;
    private int radius = 10;
    private int xOffset = 0;
    private int yOffset = 0;

    public CircleShape(Widget source, int r)
    {
        super();
        sourceWidget = source;
        radius = r;
    }
    
    public CircleShape(Widget source, int r, int xoffset, int yoffset)
    {
        super();
        sourceWidget = source;
        radius = r;
        xOffset = xoffset;
        yOffset = yoffset;
    }

    public Rectangle getBounds()
    {
        Rectangle retVal = null;
        Rectangle bounds = sourceWidget.getBounds();

//        int centerX = bounds.x + (bounds.width / 2);
//        int centerY = bounds.y + (bounds.height / 2);
        Point c = getCenter();
        retVal = new Rectangle(c.x - radius, c.y - radius, radius * 2, radius * 2);

        return sourceWidget.convertLocalToScene(retVal);
    }

    protected Point getCenter()
    {   
        Rectangle bounds = sourceWidget.getBounds();

        int centerX = xOffset + bounds.x + (bounds.width / 2);
        int centerY = yOffset + bounds.y + (bounds.height / 2);
        return new Point(centerX, centerY);
    }
    
    public Point getIntersection(Point p1, Point p2)
    {
            Rectangle bounds = sourceWidget.getBounds();
            bounds = sourceWidget.convertLocalToScene(bounds);

//            int centerX = bounds.x + (bounds.width / 2);
//            int centerY = bounds.y + (bounds.height / 2);
//            Point c = new Point(centerX, centerY);

            Point c = sourceWidget.convertLocalToScene(getCenter());
            Point retVal = getIntersection(p1, p2, c, radius);
            if(retVal == null)
            {
                retVal = p2;
            }
            return retVal;
//        return p2;
    }
    
    private boolean endPointOnCircle(Point p2, Point c, int r)
    {
        double epsilon = 0.001;

        double v = (p2.x - c.x) * (p2.x - c.x) + (p2.y - c.y) * (p2.y - c.y) - r * r;
        if (Math.abs(v) < epsilon)
        {
            return true;
        }

        return false;

    }

    private Point findPointsDirectly(Point c, int r, Point p2)
    {
        Point p;
        double d = Math.sqrt( (r * r) - (p2.x - c.x) * (p2.x - c.x));
        
        int y1 = (int) (c.y + d);
        int y2 = (int) (c.y - d);

        int y = Math.abs(p2.y - y1) < Math.abs(p2.y - y2) ? y1 : y2;

        p = new Point(p2.x, y);
        return p;
    }

    private Point getIntersection(Point p1, Point p2, Point c, int r)
    {
        double epsilon = 0.0001;

        Point p;

        double R = r * r;

        double rise = p2.y - p1.y;
        double run = p2.x - p1.x;
        if (Math.abs(run) < epsilon)
        {
            return findPointsDirectly(c, r, p2);
        }

        if (endPointOnCircle(p2, c, r))
        {
            p = p2;
            return p;
        }

        double m = rise / run;
        double M = m * m;

        double b = -m * p1.x + p1.y;
        double b_hat = b - c.y;
        double B_hat = b_hat * b_hat;

        // quad form [-B +- sqrt (B^2 - 4AC)]/2A
        double A = M + 1;
        double B = 2.0 * (b_hat * m - c.x);
        double C = c.x * c.x + B_hat - R;

        double RCND = (B * B) - (4 * A * C);

        //there is no intersection
        if (RCND < 0)
        {
            return null;
        }

        int x1 = (int) ((-B + Math.sqrt(RCND)) / (2.0 * A));
        int x2 = (int) ((-B - Math.sqrt(RCND)) / (2.0 * A));

        int x = Math.abs(p2.x - x1) < Math.abs(p2.x - x2) ? x1 : x2;

        int y = getYOnCircle(c, r, x, p2.y);

        p = new Point(x, y);

        return p;
    }

    private int getYOnCircle(Point c, int r, int x, int endPtY)
    {

        int y1 = (int) (c.y + Math.sqrt(r * r - (x - c.x) * (x - c.x)));
        int y2 = (int) (c.y - Math.sqrt(r * r - (x - c.x) * (x - c.x)));

        return Math.abs(endPtY - y1) < Math.abs(endPtY - y2) ? y1 : y2;
    }
}
