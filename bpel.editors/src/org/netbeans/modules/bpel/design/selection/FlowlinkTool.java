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
package org.netbeans.modules.bpel.design.selection;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import org.netbeans.modules.bpel.design.geometry.FPath;
import org.netbeans.modules.bpel.design.geometry.FPoint;
import org.netbeans.modules.bpel.design.model.connections.Connection;
import org.netbeans.modules.bpel.design.model.elements.InvokeOperationElement;
import org.netbeans.modules.bpel.design.model.elements.OperationElement;
import org.netbeans.modules.bpel.design.model.elements.ReceiveOperationElement;

import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.Invoke;
import org.netbeans.modules.bpel.model.api.OperationReference;
import org.netbeans.modules.bpel.model.api.PartnerLink;
import org.netbeans.modules.bpel.model.api.PartnerLinkReference;
import org.netbeans.modules.bpel.model.api.PortTypeReference;
import org.netbeans.modules.bpel.model.api.Reply;
import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.DiagramView;
import org.netbeans.modules.bpel.design.GUtils;
import org.netbeans.modules.bpel.design.decoration.components.LinkToolButton;
import org.netbeans.modules.bpel.design.model.connections.Direction;
import org.netbeans.modules.bpel.design.model.elements.VisualElement;
import org.netbeans.modules.bpel.design.model.patterns.PartnerlinkPattern;
import org.netbeans.modules.bpel.model.api.OnEvent;
import org.netbeans.modules.bpel.model.api.OnMessage;
import org.netbeans.modules.bpel.model.api.Receive;
import org.netbeans.modules.bpel.model.api.references.BpelReference;
import org.netbeans.modules.bpel.model.api.references.ReferenceCollection;
import org.netbeans.modules.bpel.model.api.references.WSDLReference;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.PortType;

public class FlowlinkTool implements DnDTool {

    private VisualElement startElement;
    private VisualElement endElement;
    private static final int INPUT = 1;
    private static final int OUTPUT = 2;
    private static final int INPUT_OUTOUT = INPUT | OUTPUT;
    private DesignView designView;
    private int currentX;
    private int currentY;
    private DiagramView currentView;
    private LinkToolButton button;

    public FlowlinkTool(DesignView designView) {
        this.designView = designView;
    }

    public DesignView getDesignView() {
        return designView;
    }

    public void init(LinkToolButton btn) {
        this.button = btn;

        btn.setVisible(false);

        VisualElement e = btn.getPattern().getFirstElement();

        // DND can start only from operation element
        // or fromdiagram element involved into messageflow
        if ((e == null) || (!isOperation(e) && !isTask(e))) {
            return;
        }

        startElement = e;

       // getDesignView().getDecorationManager().decorationChanged();
    }

    public void move(FPoint fp) {
        if (!isActive()) {
            return;
        }

        Point p = getDesignView().getOverlayView().getMousePosition();
        if (p != null) {
            currentView = getDesignView().getView(p);
            currentX = p.x;
            currentY = p.y;


            FPoint viewPt = currentView.convertPointFromParent(p);
            VisualElement e = currentView.findElement(viewPt.x, viewPt.y);

            endElement = (e != null && isValidLinkTo(e)) ? e : null;
           
            getDesignView().repaint();
        }
    }

    public boolean isValidLocation() {
        return endElement != null;
    }



    public void drop(FPoint p) {
        drop(p.x, p.y);
    }

    public void drop(double x, double y) {
        if (!isActive()) {
            return;
        }

       move(null);

        if (endElement != null && (isValidLinkTo(endElement))) {
            VisualElement operation = isOperation(startElement)
                    ? startElement : endElement;
            if (operation != null) {
                VisualElement task = isOperation(startElement) ? endElement : startElement;

                PartnerlinkPattern pattern = (PartnerlinkPattern) operation.getPattern();

                BpelEntity activity = (BpelEntity) task.getPattern().getOMReference();

                BpelReference<PartnerLink> pl_ref = ((ReferenceCollection) activity).createReference((PartnerLink) pattern.getOMReference(), PartnerLink.class);

                if (pl_ref != null) {
                    ((PartnerLinkReference) activity).setPartnerLink(pl_ref);
                }


                Operation op = pattern.getOperation(operation);

                if (op != null) {
                    WSDLReference<Operation> op_ref = ((ReferenceCollection) activity).createWSDLReference(op, Operation.class);

                    if (op_ref != null) {
                        ((OperationReference) activity).setOperation(op_ref);
                    }
                    PortType pt = (PortType) op.getParent();
                    if (pt != null) {
                        WSDLReference<PortType> pt_ref = ((ReferenceCollection) activity).createWSDLReference(pt, PortType.class);

                        if (pt_ref != null) {
                            ((PortTypeReference) activity).setPortType(pt_ref);
                        } else {
                            ((PortTypeReference) activity).removePortType();
                        }


                    }
                }
            }
        }
        clear();
    }

    public void clear() {
        startElement = null;
        endElement = null;

        if (button != null) {
            button.setVisible(true);
            button = null;

        }

        //FIXME designView.getDecorationManager().repositionComponentsRecursive();
        getDesignView().repaint();
    }

    public boolean isActive() {
        return (startElement != null);
    }

    private boolean isValidLinkTo(VisualElement to) {
        if (to == null) {
            return false;
        } else if (to == startElement) {
            return false;
        }
        if ((isOperation(to) && isTask(startElement)) ||
                (isOperation(startElement) && isTask(to))) {
            return ((getDirections(to) | getDirections(startElement)) == (INPUT | OUTPUT));
        }
        return false;
    }

    private int getDirections(VisualElement e) {
        BpelEntity ref = (BpelEntity) e.getPattern().getOMReference();
        if (e instanceof ReceiveOperationElement) {
            return INPUT;
        } else if (e instanceof InvokeOperationElement) {
            return INPUT | OUTPUT;
        } else if (isTask(ref)) {
            if (ref instanceof Reply) {
                return OUTPUT;
            } else if (ref instanceof Invoke) {
                return OUTPUT;
            } else {
                return INPUT;
            }
        }

        return 0;
    }

    private boolean isOperation(VisualElement e) {
        return e instanceof OperationElement;
    }

    private boolean isSendTask(VisualElement e) {
        Object o = e.getPattern().getOMReference();
        return (o instanceof Invoke) || (o instanceof Reply);
    }

    private boolean isTask(VisualElement e) {
        return isTask((BpelEntity) e.getPattern().getOMReference());
    }

    private boolean isTask(BpelEntity o) {
        return (o instanceof OperationReference) && (o instanceof PartnerLinkReference) && (o instanceof PortTypeReference);
    }

    public void paint(Graphics2D g2) {
        if (!isActive()) {
            return;
        }
//        if (getDesignView().getModel().isReadOnly()) return; //127263


        if (currentView != null) {
            
            Point pt1 = getDesignView().getProcessView().convertPointToParent(
                    new FPoint(startElement.getCenterX(), startElement.getCenterY()));
           
            Point pt2 = new Point(currentX, currentY);
            
            if (isValidLocation()) {
                pt2 = currentView.convertPointToParent(
                        new FPoint(endElement.getCenterX(), endElement.getCenterY()));
            } 


            FPath path = createPath(pt1.x, pt1.y, pt2.x, pt2.y, !isForwardDirection());
   
            if (path != null) {
//            GUtils.setSolidStroke(g2, 8);
//            GUtils.setPaint(g2, new Color(0x88FFFFFF, true));
//            GUtils.draw(g2, GUtils.convert(path), true);
                Connection.paintConnection(g2, path, true, true, false, true,
                        (isValidLocation()) ? null : Color.RED);
            }

        }
        Graphics2D buttonGraphics = (Graphics2D) g2.create();

        buttonGraphics.translate(currentX, currentY);
        //buttonGraphics.scale( 1 / scale, 1 / scale);
        buttonGraphics.translate(-button.getWidth() / 2, -button.getHeight() / 2);
        button.paint(buttonGraphics);
        buttonGraphics.dispose();



    }

    private boolean isForwardDirection() {
        BpelEntity omRef = startElement.getPattern().getOMReference();

        if (omRef == null) {
            return true;
        }

        if (omRef instanceof OnMessage ||
                omRef instanceof OnEvent ||
                omRef instanceof Receive) {

            return false;
        } else if (omRef instanceof Invoke || omRef instanceof Reply) {
            return true;
        }

        return true;
    }

    private FPath createPath(double x1, double y1, double x2, double y2,
            boolean invert) {

        if (invert) {
            double t = x1;
            x1 = x2;
            x2 = t;

            t = y1;
            y1 = y2;
            y2 = t;
        }

        boolean nonZeroDX = x1 != x2;
        boolean nonZeroDY = y1 != y2;

        if (nonZeroDX && nonZeroDY) {
            double cx = (x1 + x2) / 2;
            return new FPath(x1, y1, cx, y1, cx, y2, x2, y2).round(2);
        } else if (nonZeroDX || nonZeroDY) {
            return new FPath(x1, y1, x2, y2);
        }

        return null;
    }

    public Point getPosition() {
        return designView.convertDiagramToScreen(new FPoint(currentX, currentY));
    }
    // Rendering constants
    private static final Shape END_MARKER_SHAPE = new Ellipse2D.Float(-2, -2, 4, 4);
    private static final Color ACCEPT_FILL_COLOR = new Color(0x00A000);
    private static final Color ACCEPT_STROKE_COLOR = new Color(0x20A020);
    private static final Color DECLINE_FILL_COLOR = new Color(0xA00000);
    private static final Color DECLINE_STROKE_COLOR = new Color(0xA02020);
    private static final Color COLOR = new Color(0xE68B2C);
}
