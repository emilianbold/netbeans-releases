/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

/**
 *
 * @author Sherry
 */
package org.netbeans.test.umllib;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.util.List;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.widget.Widget; 
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;

 

public class UMLWidgetOperator {

    private Widget widget;
    private DrawingAreaOperator drawingArea;

    public UMLWidgetOperator(UMLWidgetOperator wo) {
        this(wo.getSource());
    }
    
    public UMLWidgetOperator(Widget widget) {
        this.widget = widget;
        this.drawingArea = DiagramOperator.getDrawingArea();
    }
    
    public Widget getSource() {
        return widget;
    }

    public void clickOn(Point p, int count) {
       DiagramOperator.getDrawingArea().clickMouse(p.x, p.y, count); 
    }
    
    public void clickOnLeftCenterPoint() {
        Point p = this.getLeftCenterPoint(10);
        DiagramOperator.getDrawingArea().clickMouse(p.x, p.y, 1);
    }
    
    public void clickOnCenterPoint() {
        Point p = this.getCenterPoint();
        DiagramOperator.getDrawingArea().clickMouse(p.x, p.y, 1, InputEvent.BUTTON1_MASK, 0);
    }


    public Point getLeftCenterPoint() {
        return this.getLeftCenterPoint(10);
    }

    public Point getLeftCenterPoint(int shift) {
        Rectangle rect = this.getRectangle();
        return new Point(rect.x + shift, (int) rect.getCenterY());
    }

    public Point getCenterPoint() {
        Point centerPoint = new Point((int) this.getRectangle().getCenterX(), (int) this.getRectangle().getCenterY());
        return centerPoint;
    }

    public Rectangle getRectangle() {
        Rectangle localRect = widget.getBounds();
        Point scenePoint = widget.convertLocalToScene(new Point(localRect.x, localRect.y));
        Rectangle sceneRect = new Rectangle(scenePoint.x, scenePoint.y, localRect.width, localRect.height);
        return sceneRect;
    }
    
    public int getBottom() {
       Rectangle rect = getRectangle();
       return (int)rect.getY() - (int)rect.getHeight(); 
    }
    
    public int getRight() {
        Rectangle rect = getRectangle();
        return (int)rect.getX() + (int)rect.getWidth();
    }
    
    public int getLeft() {
        Rectangle rect = getRectangle();
        return (int)rect.getX();
    }
    
    public Point getBottomRight() {
        return new Point(this.getRight(),this.getBottom());
    }
    
    public Point getBottomLeft() {
        return new Point(this.getLeft(), this.getBottom());
    }
    public Rectangle inflateRect(int size) {
         
        Rectangle rect = getRectangle();
        int x = (int) rect.getX();
        int y = (int) rect.getY();
        int width = (int) rect.getWidth();
        int height = (int) rect.getHeight();
        Utils.log("inflateRect()");
        Utils.log("size="+size);
        Utils.log("x= "+ x);
        Utils.log("y= "+ y);
        Utils.log("y= "+ y);
        Utils.log("width= "+ width);
        Utils.log("height= "+ height);
         
        x += size;
        y += size;
        width += (size * 2);
        height += (size * 2);
        Utils.log("x= "+ x);
        Utils.log("y= "+ y);
        Utils.log("y= "+ y);
        Utils.log("width= "+ width);
        Utils.log("height= "+ height);
        return new Rectangle(new Point(x, y), new Dimension(width, height));   
    }
    
    public boolean isSelected() {
        return widget.getState().isSelected();
    }
    
    public String getElementType() {
        if (getPresentationElement().getSubjectCount() >= 1) {
            return getPresentationElement().getFirstSubject().getElementType();
        } else {
            return null;
        }
    }

    public IPresentationElement getPresentationElement() {
        GraphScene scene = (GraphScene) widget.getScene();
        return (IPresentationElement) scene.findObject(widget);
    }

    
    public String getName() {
        if (getPresentationElement().getSubjectCount() >= 1) {
            return getPresentationElement().getFirstSubject().toString();
        } else {
            return null;
        }
    }
   
    public GraphScene getScene() {
        return (GraphScene) widget.getScene();
    }
    
    public boolean isNode() {
        return getScene().isNode(getPresentationElement());
    }
    
    public boolean isEdge() {
        return getScene().isEdge(getPresentationElement());
    }
    
    // Workaround. Use UMLNodeWidget.getCurrentView() once it is changed to public
    public Widget getCurrentView() {
        Widget retVal = null;
         
        if(this.widget.getChildren().size() > 0)
        {
            retVal = this.widget.getChildren().get(0).getChildren().get(0);
            Utils.log("UMLWidgetOperator: getCurrentView = " + retVal);
        }
        listWidgetChildren(retVal);
        return retVal;
    }
    
    public static void listWidgetChildren(Widget w) {
        List<Widget> list = w.getChildren();
        if (list.size() > 0) {  
            Utils.log("listWidgetChildren(): parent = " + w.toString() + " has "+list.size()+" children");  
            int count = 1;
            for (Widget child : list) {
                Utils.log("listWidgetChildren(): parent = " + w.toString() + " child = " + child.toString());
                listWidgetChildren(child);  
                count++;
                }  
        } else
             Utils.log("listWidgetChildren(): widget = " + w.toString() +" has no chidlren");
    }
    
    Widget retVal = null;

    public Widget findWidgetByType(String type) throws ClassNotFoundException {
        return findWidgetByType(this.widget, type);
    }

    public Widget findWidgetByType(Widget w, String type) throws ClassNotFoundException {
        Class clazz = Class.forName(type);
        List<Widget> list = w.getChildren();
        if (list.size() > 0) {
            for (Widget child : list) {
                if (clazz.isInstance(child)) {
                    retVal = child;
                    Utils.log("UMLWidgetOperator:findWidgetByType: EditableWidget found");
                    break;
                } else {
                    retVal = findWidgetByType(child, type);
                }
            }
        }
        return retVal;
    }

    public UMLWidgetOperator findEditableCompartmentWidget() throws ClassNotFoundException  {
        return findEditableCompartmentWidget(this.widget);
    }
    
    public UMLWidgetOperator findEditableCompartmentWidget(Widget w) throws ClassNotFoundException  {
        Widget ew = findWidgetByType(w, "org.netbeans.modules.uml.diagrams.nodes.EditableCompartmentWidget");
        return (new UMLWidgetOperator(ew));
    }    
}
