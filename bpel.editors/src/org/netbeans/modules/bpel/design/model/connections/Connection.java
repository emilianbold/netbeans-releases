/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.bpel.design.model.connections;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.bpel.design.geometry.FCoords;
import org.netbeans.modules.bpel.design.geometry.FPath;
import org.netbeans.modules.bpel.design.geometry.FPoint;
import org.netbeans.modules.bpel.design.geometry.FShape;
import org.netbeans.modules.bpel.design.geometry.FStroke;
import org.netbeans.modules.bpel.design.geometry.Triangle;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.design.model.patterns.CompositePattern;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;

public class Connection {
    
    
    private VisualElement source;
    private VisualElement target;

    /** 
     * directions can be changed in derived classes
     */
    private Direction sourceDirection;
    private Direction targetDirection;
    
    private Pattern pattern;
    
    private boolean paintArrow = true;
    private boolean paintSlash = false;
    private boolean paintDashed = false;
    private boolean paintCircle = false;
    protected FPoint endPoint;
    protected FPoint startPoint;
    
    private float x1 = 0, y1 = 0;
    private float dx, dy;
    

    private final int uid;
    
    private boolean needsRedraw = true;
    
    private FPath path;
    
    
    public Connection(Pattern pattern) {
        this.pattern = pattern;
        uid = uidCounter++;
        pattern.addConnection(this);
    }
    
    
    public void setPaintArrow(boolean b) { paintArrow = b; }
    public void setPaintCircle(boolean b) { paintCircle = b; }
    public void setPaintSlash(boolean b) { paintSlash = b; }
    public void setPaintDashed(boolean b) { paintDashed = b; }
    
    
    public boolean isPaintArrow() { return paintArrow; }
    public boolean isPaintCircle() { return paintCircle; }
    public boolean isPaintSlash() { return paintSlash; }
    public boolean isPaintDashed() { return paintDashed; }
    public FPoint getStartPoint() {return startPoint;}
    public FPoint getEndPoint() {return endPoint;}
    
    
    public void setStartAndEndPoints(FPoint startPoint, FPoint endPoint) {
        
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        
        float newX1 = startPoint.x;
        float newY1 = startPoint.y;
        float newX2 = endPoint.x;
        float newY2 = endPoint.y;
        
        float newDX = newX2 - newX1;
        float newDY = newY2 - newY1;
        
        float oldX1 = this.x1;
        float oldY1 = this.y1;
        
        float oldDX = this.dx;
        float oldDY = this.dy;
        
        final float epsilon = 0.33f;
        
        if (needsRedraw || newX1 != oldX1 || newY1 != oldY1 
                || newDX != oldDX || newDY != oldDY)
        {
            this.x1 = newX1;
            this.y1 = newY1;
            this.dx = newDX;
            this.dy = newDY;
            update();
        }
    }
    
    
    protected double findXStep(double x1, double dx) {
        return x1 + dx / 2;
    }
    
    
    protected double findYStep(double y1, double dy) {
        return y1 + dy / 2.0;
    }
    
    
    protected void update() {
        double dx = this.dx;
        double dy = this.dy;
        
        double cx = findXStep(x1, dx) - x1;
        double cy = findYStep(y1, dy) - y1;

        FPoint[] points = null;
        
        if (sourceDirection == targetDirection) {
            double t;
            switch (sourceDirection) {
                case TOP:
                    t = Math.min(0, dy) - BRACKET_SIZE;
                    points = new FPoint[] {
                        new FPoint(0, 0),
                        new FPoint(0, t),
                        new FPoint(dx, t),
                        new FPoint(dx, dy)
                    };
                    break;
                case BOTTOM:
                    t = Math.max(0, dy) + BRACKET_SIZE;
                    points = new FPoint[] {
                        new FPoint(0, 0),
                        new FPoint(0, t),
                        new FPoint(dx, t),
                        new FPoint(dx, dy)
                    };
                    break;
                case LEFT:
                    t = Math.min(0, dx) - BRACKET_SIZE;
                    points = new FPoint[] {
                        new FPoint(0, 0),
                        new FPoint(t, 0),
                        new FPoint(t, dy),
                        new FPoint(dx, dy)
                    };
                    break;
                case RIGHT:
                    t = Math.max(0, dx) + BRACKET_SIZE;
                    points = new FPoint[] {
                        new FPoint(0, 0),
                        new FPoint(t, 0),
                        new FPoint(t, dy),
                        new FPoint(dx, dy)
                    };
                    break;
            }
        } else if (sourceDirection.isVertical()) {
            if (targetDirection.isVertical()) {
                // source direction - vertical
                // target direction - vertical
                if (dy != 0) {
                    points = new FPoint[] {
                        new FPoint( 0,  0), 
                        new FPoint( 0, cy),
                        new FPoint(dx, cy),
                        new FPoint(dx, dy)
                    };
                }
            } else { 
                // source direction - vertical
                // target direction - horizontal 
                points = new FPoint[] {
                    new FPoint( 0,  0), 
                    new FPoint( 0, dy),
                    new FPoint(dx, dy)
                };
            }
        } else { 
            if (targetDirection.isVertical()) {
                // source direction - horizontal
                // target direction - vertical
                points = new FPoint[] {
                    new FPoint( 0,  0), 
                    new FPoint(dx,  0),
                    new FPoint(dx, dy)
                };
            } else {
                // source direction - horizontal
                // target direction - horizontal
                if (dx != 0) {
                    points = new FPoint[] {
                        new FPoint( 0,  0), 
                        new FPoint(cx,  0),
                        new FPoint(cx, dy),
                        new FPoint(dx, dy)
                    };
                }
            }
        }
        
        
        if (points != null) { 
            path = new FPath(points).round(2).translate(x1, y1);
        } else {
            path = null;
        }
        
        needsRedraw = false;        
    }
    
    
    public void connect(VisualElement source, Direction sourceDirection,
            VisualElement target, Direction targetDirection) 
    {
        setSource(source, sourceDirection);
        setTarget(target, targetDirection);
    }

    
    public void setSource(VisualElement newSource,  
            Direction newSourceDirection) 
    {
        assert newSource != null;
        
        VisualElement oldSource = this.source;
        Direction oldSourceDirection = this.sourceDirection;
        
        if (newSource != oldSource) {
            if (oldSource != null) {
                oldSource.removeOutputConnection(this);
            }
            newSource.addOutputConnection(this);
            this.source = newSource;
            needsRedraw = true;
        }
        
        if (newSourceDirection != oldSourceDirection) {
            this.sourceDirection = newSourceDirection;
            needsRedraw = true;
        }
    }
    
    
    public void setTarget(VisualElement newTarget,  
            Direction newTargetDirection) 
    {
        assert newTarget != null;
        
        VisualElement oldTarget = this.target;
        Direction oldTargetDirection = this.targetDirection;
        
        if (newTarget != oldTarget) {
            if (oldTarget != null) {
                oldTarget.removeInputConnection(this);
            }
            newTarget.addInputConnection(this);
            this.target = newTarget;
            needsRedraw = true;
        }
        
        if (oldTargetDirection != newTargetDirection) {
            this.targetDirection = newTargetDirection;
            needsRedraw = true;
        }
    }    
    
    
    public Direction getSourceDirection() { 
        return sourceDirection; 
    }
    
    
    public Direction getTargetDirection() {
        return targetDirection; 
    }
    
    
    public VisualElement getTarget() {
        return target; 
    }
    
    
    public VisualElement getSource() { 
        return source; 
    }
    
    
    public Pattern getPattern() {
        return pattern;
    }


    public void remove() {
        if (source != null) {
            source.removeOutputConnection(this);
            source = null;
        }
        
        if (target != null) {
            target.removeInputConnection(this);
            target = null;
        }
        
        pattern.removeConnection(this);
    }
    
    
    public String toString() {
        return "Connection: " + getClass().getName() + 
                ", belongs to " + pattern + 
                ", from "+ getSource() + 
                ", to " + getTarget();
    }
    


    public FPath getPath() {
        return path;
    }

    
    public FPath getSegmentsForPattern(CompositePattern pattern) {
        
        Pattern sp = source.getPattern();
        Pattern tp = target.getPattern();
        
        boolean substructFromTarget = false;
        boolean substructAll = false;
        
        List<Pattern> sParents = new ArrayList<Pattern>();
        for (Pattern p = sp; p != null; p = p.getParent()) {
            sParents.add(p);
        }
        
        Pattern cp = null; // common pattern
        for (Pattern p = tp; p != null; p = p.getParent()) {
            if (p == pattern) substructFromTarget = true;
            
            if (sParents.contains(p)) {
                cp = p;
                break;
            }
        }
        
        if (cp == pattern) {
            substructAll = true;
        }


        FPath result = path.intersect(pattern.getBorder().getShape());
        
        FShape targetBorder = null;
        for (Pattern p = tp; (p != cp) && (p != pattern); p = p.getParent()) {
            if (!(p instanceof CompositePattern)) continue;
            if (((CompositePattern) p).getBorder() == null) continue;
            targetBorder = ((CompositePattern) p).getBorder().getShape();
        }
        
        FShape sourceBorder = null;
        for (Pattern p = sp; (p != cp) && (p != pattern); p = p.getParent()) {
            if (!(p instanceof CompositePattern)) continue;
            if (((CompositePattern) p).getBorder() == null) continue;
            sourceBorder = ((CompositePattern) p).getBorder().getShape();
        }
        
        
        if (substructAll) {
            if (sourceBorder != null) {
                result = result.subtract(sourceBorder);
            }
            if (targetBorder != null) {
                result = result.subtract(targetBorder);
            }
        } else if (substructFromTarget) {
            if (targetBorder != null) {
                result = result.subtract(targetBorder);
            }
        } else {
            if (sourceBorder != null) {
                result = result.subtract(sourceBorder);
            }
        }
        
        return result;
    }
    
    

    
    public int getUID() {
        return uid;
    }


    private static int uidCounter = 0;
    
    
    
    public void paint(Graphics2D g2) {
        assert (path != null): "Invalid connection(path is null) found on diagram: " + this;
        paintConnection(g2, path, isPaintDashed(), isPaintArrow(), 
                isPaintSlash(), isPaintCircle(), null);
    }
    
    
    public void paintThumbnail(Graphics2D g2) {
        assert (path != null): "Invalid connection(path is null) found on diagram: " + this;
        paintConnection(g2, path, false, false, false, false, null);
    }
    
    
    // Rendering constants
    public static final Color COLOR = new Color(0xE68B2C);
    public static final Color CIRCLE_FILL = new Color(0xFFFFFF);

    
    
    public static void paintConnection(Graphics2D g2, FPath path,
            boolean paintDashed, 
            boolean paintArrow, 
            boolean paintSlash, 
            boolean paintCircle,
            Color color) 
    {
        paintConnection(g2, path, paintDashed, paintArrow, paintSlash, 
            paintCircle, 1, color);
    }
    
    public static void paintConnection(Graphics2D g2, FPath path,
            boolean paintDashed, 
            boolean paintArrow, 
            boolean paintSlash, 
            boolean paintCircle,
            double width,
            Color color) 
    {
        if (path == null) return;
        if (path.length() <= 0.0f) return;
        
        if (color == null) {
            color = COLOR;
        }
        
        g2.setRenderingHint(
                RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_NORMALIZE);
        
        g2.setPaint(color);
        if (paintDashed) {
            g2.setStroke(new FStroke(width, 3).createStroke(g2));
        } else {
            g2.setStroke(new FStroke(width).createStroke(g2));
        }
        
        g2.draw(path);
        
        if (paintDashed) {
            g2.setStroke(new FStroke(width).createStroke(g2));
        }

        if (paintArrow) {
            FCoords coords = path.coords(1.0);
            FPoint p1 = coords.getPoint(-4.0, 2.0);
            FPoint p2 = coords.getPoint(-4.0, -2.0);
            
            Shape arrowShape = new Triangle(coords.x, coords.y, p1.x, p1.y, 
                    p2.x, p2.y);
            
            g2.fill(arrowShape);
            g2.draw(arrowShape);
        }
        
        if (paintSlash) {
            FCoords coords = path.coords(0.0);
            FPoint p1 = coords.getPoint(5.0, -4.0);
            FPoint p2 = coords.getPoint(11.0, 4.0);
            g2.draw(new Line2D.Float(p1.x, p1.y, p2.x, p2.y));
        }
        
        
        if (paintCircle) {
            FPoint center = path.coords(0.0).getPoint(2.0, 0);
            Shape s = new Ellipse2D.Double(center.x - 2, center.y - 2, 4, 4);
            g2.setPaint(CIRCLE_FILL);
            g2.fill(s);
            g2.setPaint(color);
            g2.draw(s);
        }
    }
    
    
    public static final float BRACKET_SIZE = 16;
    
    private static FStroke SOLID_STROKE = new FStroke(1);
    private static FStroke DASHED_STROKE = new FStroke(1, 3);
}
