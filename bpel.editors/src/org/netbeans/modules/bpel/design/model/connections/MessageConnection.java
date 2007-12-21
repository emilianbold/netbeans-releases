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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.util.HashSet;
import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.geometry.FPoint;
import org.netbeans.modules.bpel.design.geometry.FStroke;
import org.netbeans.modules.bpel.design.model.DiagramModel;
import org.netbeans.modules.bpel.design.model.PartnerRole;

import org.netbeans.modules.bpel.design.model.patterns.Pattern;
import org.netbeans.modules.bpel.design.selection.EntitySelectionModel;
import org.netbeans.modules.bpel.design.DiagramView;
import org.netbeans.modules.bpel.design.model.patterns.PartnerlinkPattern;


/**
 *
 * @author aa160298
 */
public class MessageConnection extends Connection {
    private static HashSet<Integer> usedSlots = new HashSet<Integer>() ;
    /** 
     * Creates a new instance of MessageConnection.
     */
    public MessageConnection(Pattern p) {
        super(p);
        setPaintCircle(true);
        setPaintDashed(true);
    }

    private boolean isPatternSelected() {
        Pattern p = getPattern();
        if (p == null) {
            return false;
        }

        DiagramModel model = p.getModel();
        if (model == null) {
            return false;
        }

        DesignView view = model.getView();
        if (view == null) {
            return false;
        }

        EntitySelectionModel selection = view.getSelectionModel();
        if (selection == null) {
            return false;
        }

        return p == selection.getSelectedPattern();
    }
    public static void resetRoutingInfo(){
        usedSlots.clear();
    }
    public void paintPL(Graphics2D g2) {
        boolean isOutcoming = (getTarget().getPattern() != getPattern());

        FPoint pl_point = isOutcoming ? getEndPoint() : getStartPoint();
        Direction dir = isOutcoming ? getTargetDirection() : getSourceDirection();

        GeneralPath path = new GeneralPath();
        
        path.moveTo(pl_point.x, pl_point.y);
        
        path.lineTo(pl_point.x + ((dir == Direction.RIGHT) ? 100 : -100), pl_point.y);
                
        paintCurvedConnection(g2, path, 1, COLOR);

    }
    /*
     * @paintOnProcess flag identify which view requested this connection 
     * to be rendered:
     * true: processView
     * false: partnerLinkView
     * */

    @Override
    public void paint(Graphics2D g2) {


        FPoint pt_pl = getPLSidePoint();

        boolean isOutcoming = (getTarget().getPattern() != getPattern());

        Shape path = (isOutcoming) ? 
            getPath(getStartPoint(), pt_pl) : 
            getPath(pt_pl, getEndPoint());

        paintCurvedConnection(g2, path, 1, COLOR);

    }

    private FPoint getPLSidePoint() {

        boolean isOutcoming = (getTarget().getPattern() != getPattern());

        FPoint pl_point = isOutcoming ? getEndPoint() : getStartPoint();


        Pattern pl_pattern = (isOutcoming) ? getTarget().getPattern() : getSource().getPattern();

        if (pl_pattern == null) {
            return null;
        }


        DiagramModel model = getPattern().getModel();

        if (model == null) {
            return null;
        }

        

        DesignView view = model.getView();

        DiagramView pl_view = pl_pattern.getView();


        DiagramView process_view = view.getProcessView();



        
        pl_point = process_view.convertPointFromParent(pl_view.convertPointToParent(pl_point));


        Point tmp = process_view.convertDiagramToScreen(pl_point);
        
        Rectangle r = process_view.getVisibleRect();
        
        tmp.x = (((PartnerlinkPattern) pl_pattern).getType() == PartnerRole.PROVIDER) ? (r.x + r.width) : r.x;



        return process_view.convertScreenToDiagram(tmp);




    }
    private static int STEP = 8;
    private static int SLOT_STEP = 4;

    public GeneralPath getPath(FPoint start, FPoint end) {
        float x1, x2, x6, x5;

        float direction = (start.x < end.x) ? 1 : -1;
        x1 = start.x;
        x2 = x1 + STEP * direction;

        x6 = (int) end.x;

        x5 = x6 - STEP * direction;
        
        while (usedSlots.contains(new Integer((int)(x5 / SLOT_STEP)))){
            x5 -= SLOT_STEP * direction;
        }
        //usedSlots.add(new Integer((int)(x5 / SLOT_STEP)));

        float d = Math.max((x5 - x2) / 4, STEP * 2) * direction;

        float x3 = x2 + d;
        float x4 = x5 - d;

        float y1 = start.y;
        float y2 = end.y;

        float cx = (x3 + x4) / 2f;
        float cy = (y1 + y2) / 2f;

        


        GeneralPath path = new GeneralPath();
        path.moveTo(x1, y1);
        path.lineTo(x2, y1);
        path.quadTo(x3, y1, cx, cy);
        path.quadTo(x4, y2, x5, y2);
//        path.lineTo(x5, y1);
//        path.lineTo(x5, y2);
        
        path.lineTo(x6, y2);
        return path;


    }

    public static void paintCurvedConnection(Graphics2D g2, Shape path, double width,
            Color color) {
        if (path == null) {
            return;
        }


        if (color == null) {
            color = COLOR;
        }

        g2.setRenderingHint(
                RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_NORMALIZE);

        g2.setPaint(color);
        g2.setStroke(new FStroke(width, 3).createStroke(g2));


        g2.draw(path);


    /*        
    if (paintDashed) {
    g2.setStroke(new FStroke(width).createStroke(g2));
    }
    /*
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
    }*/
    }

    public void paintTail(Graphics2D g2) {
    /*
    Pattern p = getPattern();
    if (isPatternSelected()) {
    Connection.paintConnection(g2, path, isPaintDashed(), 
    isPaintArrow(), isPaintSlash(), isPaintCircle(), 2, COLOR);
    } else {
    Connection.paintConnection(g2, path, isPaintDashed(), 
    isPaintArrow(), isPaintSlash(), isPaintCircle(), null);
    }*/
    }
    private static final Color COLOR_SELECTED = new Color(0x5D985C);
}
