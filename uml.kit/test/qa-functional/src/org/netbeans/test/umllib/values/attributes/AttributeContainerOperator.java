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
 * made subject to such option by the copyright hdefaultElementer.
 */
/*
 * AttributeContainerOperator.java
 *
 * Created on January 27, 2006, 1:31 PM
 *
 */
package org.netbeans.test.umllib.values.attributes;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.modules.uml.diagrams.nodes.AttributeWidget;
import org.netbeans.modules.uml.diagrams.nodes.FeatureWidget;
import org.netbeans.test.umllib.CompartmentOperator;
import org.netbeans.test.umllib.CompartmentTypes;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.EditControlOperator;
import org.netbeans.test.umllib.UMLWidgetOperator;
import org.netbeans.test.umllib.Utils;
import org.netbeans.test.umllib.util.LabelsAndTitles;
import org.netbeans.test.umllib.values.ValueOperator;

/**
 *
 * @author Alexandr Scherbatiy
 */
public class AttributeContainerOperator extends ValueOperator {

    private DiagramElementOperator  diagramElementOperator;
    private CompartmentOperator attributeCompartment;
    
    /** Creates a new instance of AttributeContainerOperator */
    public AttributeContainerOperator(DiagramElementOperator elementOperator) {
        this.diagramElementOperator = elementOperator;
        this.attributeCompartment = new CompartmentOperator(elementOperator, CompartmentTypes.ATTRIBUTE_LIST_COMPARTMENT, "Attributes");
    }
 
    
    //  ======================   Getting  Action  ================================
    public AttributeElement getAttribute(String name) {
        Utils.log("getAttribute(String name) called");
        if (name == null) {
            return null;
        }
  
        AttributeElement[] elems = getAttributes();

        for (AttributeElement elem : elems) {
            Utils.log("attrib="+elem.getName());
            if (name.equals(elem.getName())) {
                return elem;
            }
        }
        
        return null;
    }

    public AttributeElement getAttribute(int index) {
        AttributeElement[] elems = getAttributes();
        return elems[index];
    }

    public AttributeElement[] getAttributes() {

        List<Widget> list = attributeCompartment.getSource().getChildren();
        ArrayList<AttributeElement> attributeList = new ArrayList<AttributeElement>();

        for (Widget attrib : list) {
            if (attrib instanceof AttributeWidget) {
                Utils.log("attrib=" + attrib.toString());
                String parse = ((FeatureWidget) attrib).getText();
                Utils.log("parse=" + parse);
                //System.out.println("--- get operation: \"" +  parse+ "\"");
                AttributeElement elem = AttributeElement.parseOperationElement(parse);
                attributeList.add(elem);

            }
        }
        return attributeList.toArray(new AttributeElement[]{});
    }

    //  ======================= Add / Rename / Remove Actions ===================
    public void addAttribute(AttributeElement attributeElement) {
        addOperationByContextMenu(attributeElement);
    }

    private void addOperationByContextMenu(AttributeElement attributeElement) {

        System.out.println("***************************************************");
        AttributeElement defaultElement = new AttributeElement();
        System.out.println("***  element  = " + attributeElement);
        System.out.println("***  defaultElement      = " + defaultElement);
        System.out.println("***  text     = \"" + attributeElement.getText() + "\"");
        System.out.println("***  defaultElement text = \"" + defaultElement.getText() + "\"");
        Utils.log("looking for menu: "+ LabelsAndTitles.POPUP_ADD_ATTRIBUTE);
        attributeCompartment.getPopup().pushMenu(LabelsAndTitles.POPUP_ADD_ATTRIBUTE);
        
        
        // Enter Edit control mode
        Widget source=attributeCompartment.getSource();
        List<Widget> _attributes = source.getChildren();
        if (_attributes != null) {
            for (Widget child : _attributes) {
                Utils.log("AttributeContainterOperator(): child=" + child.toString());
                if (child instanceof AttributeWidget) {
                    String parse = ((FeatureWidget) child).getText(); 
                    AttributeElement elem = AttributeElement.parseOperationElement(parse);
                    if (elem.getName().equals(defaultElement.getName())) {
                    Utils.log("AttributeContainterOperator(): atrributeWidget found");
                    Utils.log("AttributeContainterOperator(): attribute = " + ((FeatureWidget) child).getText());                    
                    UMLWidgetOperator wo = new UMLWidgetOperator(child);
                    Point p = wo.getCenterPoint();
                    wo.clickOn(p, 2);
                    try {
                        Thread.sleep(1000);
                    } catch (Exception ex) {
                    }
                    wo.clickOn(p, 2);                  
                }
                }
            }
        } else
            Utils.log("AttributeContainterOperator(): _attribute=null");
       
        try {
            Thread.sleep(1000);
        } catch (Exception ex) {
        }
           
        
        EditControlOperator ec = new EditControlOperator(MainWindowOperator.getDefault());
        JTextFieldOperator textFieldOperator = ec.getTextFieldOperator();
          
        //tf.setText(operationElement.getText());
         textFieldOperator.setCaretPosition(0);
 
        changeText(attributeElement.getVisibility().getValue(), defaultElement.getVisibility().getValue(), textFieldOperator);
        changeText(attributeElement.getType().getValue(), defaultElement.getType().getValue(), textFieldOperator);
        changeText(attributeElement.getName(), defaultElement.getName(), textFieldOperator);

        //textFieldOperator.pushKey(KeyEvent.VK_ENTER);
        try {
            Thread.sleep(1000);
        } catch (Exception ex) {
        }
        
       DiagramOperator.getDrawingArea().clickMouse();
       
        try {
            Thread.sleep(100);
        } catch (Exception ex) {
        }
    }
    public void clickOnLeftCenterPoint(Widget w) {
        Point p = getLeftCenterPoint(w, 10);
    }
    
    public Point getLeftCenterPoint(Widget w, int shift) {
        Rectangle rect = getRectangle(w);
        return new Point(rect.x+shift, (int)rect.getCenterY());
    }
    
     public Rectangle getRectangle(Widget w) {
         Rectangle localRect = w.getBounds();
         Point scenePoint = w.convertLocalToScene(new Point(localRect.x, localRect.y));
         Rectangle sceneRect = new Rectangle(scenePoint.x, scenePoint.y, localRect.width, localRect.height);
         return sceneRect;
     }
     
      public Point getCenterPoint(Widget w) {         
        Point centerPoint = new Point((int)getRectangle(w).getCenterX(),(int)getRectangle(w).getCenterY());
        return centerPoint;
    }
}
