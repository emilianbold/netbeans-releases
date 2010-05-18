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



package org.netbeans.test.umllib.customelements;
import java.awt.Point;
import java.awt.Point;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Timeout;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComponentOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
//6.0import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
//6.0import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.drawingarea.view.UMLNodeWidget;
import org.netbeans.test.umllib.CompartmentTypes;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.LinkOperator;
import org.netbeans.test.umllib.LinkPaletteOperator;
import org.netbeans.test.umllib.LinkTypes;
import org.netbeans.test.umllib.SetName;
import org.netbeans.test.umllib.UMLWidgetOperator;
import org.netbeans.test.umllib.Utils;
import org.netbeans.test.umllib.exceptions.NotFoundException;
import org.netbeans.test.umllib.util.LibProperties;

public class SequenceDiagramOperator extends DiagramOperator {
    private int logicalTop = 50;
    private Scene scene = null;
    
    public SequenceDiagramOperator(String name){
        super(name);
        scene = getDesignerScene();
        logicalTop = (int) scene.convertSceneToView(new Point(10, 50)).getY();
    }
    
    /**
     * Returns free point for a new lifeline to be created at. The nearest element would be not closer than 
     * <CODE>span</CODE> points in horizontal direction.
     * You may use such point to place an element or invoke popup menu
     * @param span Minimal distance to the nearest element
     * @return free point
     */
    public Point getPointForLifeline(int span) {
        int width = getSource().getWidth();
        int height = getSource().getHeight();
        DesignerScene scene = getDesignerScene();
        Collection<IPresentationElement> children = scene.getNodes();
        ArrayList<Rectangle> elementBounds = new ArrayList<Rectangle>();
        for (IPresentationElement presentation : children) {
            Widget widget = (Widget) scene.findWidget(presentation);
            if (presentation == null) {
                continue;
            } 
            
          if(!(widget instanceof  UMLNodeWidget)) { //We are looking only for nodes here
                continue;
            }
            Rectangle rect = (new UMLWidgetOperator(widget)).getRectangle(); 
            Utils.log("Before inflate" + rect.toString());
            //TODO: need to understand what inflateRect()(orignal name is inflate() does
            //rect = umlWidgetOperator.inflateRect(span+10);
            Utils.log("After inflate" + rect.toString());
            elementBounds.add(rect);

        }
         
 
         Point p = null;
         int h = (int) scene.convertViewToScene(new Point(0,logicalTop)).getY();
         for(int w=50;w<width-50;w+=10) {
             p =  scene.convertSceneToView(new Point(w, h));            
             boolean pointIsFree = true;
              for (int i=0; i<elementBounds.size(); i++){
                  // find rect's right x 
                  int x = (int) (elementBounds.get(i).getX() +  elementBounds.get(i).getWidth());
                  if (x > p.getX()){
                    pointIsFree = false;
                    break;
                }
            }
            if (pointIsFree){
                 return scene.convertViewToScene(p);
            }            
        }
        
      return getDrawingArea().centerAtPoint(new Point(width+50, h));
    }
    
    
    
    /**
     * Put element on diagram by pressing toolbar butoon and clicking on 
     * the diagram
     * @param name Name for new element
     * @param elementType Type of new element
     * @throws qa.uml.exceptions.NotFoundException when Button for the element is not found on toolbar
     * @return Operator for created element
     */
    public DiagramElementOperator putElementOnDiagram(String name, ElementTypes elementType) throws NotFoundException{
        Point p = null;
        if (elementType.equals(ElementTypes.LIFELINE) || elementType.equals(ElementTypes.ACTOR_LIFELINE)){
            p = getPointForLifeline(100);
        }else{
            p = getDrawingArea().getFreePoint();
        }        
        return putElementOnDiagram(name, elementType, p.x, p.y);
    }
    
    
    /**
     * create an element on diagram by pressing toolbar butoon and clicking on 
     * the diagram
     * @param name Name for new element
     * @param elementType Type of new element
     */
    public void createGenericElementOnDiagram(String name, ElementTypes elementType) throws NotFoundException{
        Point p = null;
        if (elementType.equals(ElementTypes.LIFELINE) || elementType.equals(ElementTypes.ACTOR_LIFELINE)){
            p = getPointForLifeline(100);
        }else{
            p = getDrawingArea().getFreePoint();
        }  
        createGenericElementOnDiagram(name, elementType, p.x, p.y, LibProperties.getCurrentNamer(elementType));
    }
    
    
    
    /**
     * Put element on diagram by pressing toolbar butoon and clicking on 
     * the diagram
     * @param name Name for new element
     * @param elementType Type of new element
     * @param x X cooordinate of point where element will be created
     * @param y Y cooordinate of point where element will be created
     * @param namer Namer for this element. Certain elements
     * should be named in very special way
     * @throws qa.uml.exceptions.NotFoundException when Button for the element is not found on toolbar
     * @return Operator for created element
     */
    public DiagramElementOperator putElementOnDiagram(String name, ElementTypes elementType, int x, int y, SetName namer) throws NotFoundException{        
        createGenericElementOnDiagram(name, elementType, x, y, namer);
        
        //TODO: this should be an option, not a rule
        long timeoutValDlg = JemmyProperties.getCurrentTimeout("DialogWaiter.WaitDialogTimeout");
        try{
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 2000);
            JDialogOperator classifierCreated = new JDialogOperator("Classifier not found");
            new JButtonOperator(classifierCreated, "Yes").push();
        }catch(Exception excp){
        }finally{
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", timeoutValDlg);
        }
        
        if (elementType.equals(ElementTypes.LIFELINE) || elementType.equals(ElementTypes.ACTOR_LIFELINE)){
            int semicolonPos = name.indexOf(':');        
            String lineName = name.substring(0,semicolonPos);
            String classifierName = name.substring(semicolonPos+1);
            return new LifelineOperator(this, lineName, classifierName);
        }else{
            return new DiagramElementOperator(this, name);
        }
    }
 
    
    
    /**
     * Draw link on diagram
     * @param linkElementType Link type
     * @param fromElement Source element
     * @param toElement target element
     * @throws qa.uml.exceptions.NotFoundException when source or target is not found
     * @return Operator for created link
     */
    public LinkOperator createLinkOnDiagram(LinkTypes linkElementType, DiagramElementOperator fromElement, DiagramElementOperator toElement) throws NotFoundException{                
        createGenericRelationshipOnDiagram(linkElementType, fromElement, toElement);
        if (linkElementType.equals(LinkTypes.SYNC_MESSAGE) || linkElementType.equals(LinkTypes.ASYNC_MESSAGE) || linkElementType.equals(LinkTypes.MESSAGE_TO_SELF) || linkElementType.equals(LinkTypes.CREATE_MESSAGE)){
            linkElementType = LinkTypes.MESSAGE;
        }
        return new LinkOperator(fromElement, toElement, linkElementType);
    }
    
    
    /**
     * Create generic relationship on diagram
     * @param linkElementType Link type
     * @param fromElement Source element
     * @param toElement target element
     * @throws qa.uml.exceptions.NotFoundException when source or target is not found
     */
    public void createGenericRelationshipOnDiagram(LinkTypes linkElementType, DiagramElementOperator fromElement, DiagramElementOperator toElement) throws NotFoundException{
        org.netbeans.test.umllib.Utils.log("SequenceDiagramOperator:createDeneraticRelationShipOnDiagram(): fromElmentType=" + fromElement.getElementType());  
        if (!fromElement.getElementType().equals(ElementTypes.LIFELINE.toString())){
            
            super.createGenericRelationshipOnDiagram(linkElementType, fromElement, toElement);
            return;
        }
        HashSet<LinkOperator> links = fromElement.getLinks();
        Iterator<LinkOperator> it = links.iterator();        
        
        //calculating start and destination points
        SequenceLifelineCompartment sourceCompartment = new SequenceLifelineCompartment(fromElement, CompartmentTypes.SEQUENCE_LIFELINE_COMPARTMENT);
        SequenceLifelineCompartment destCompartment = new SequenceLifelineCompartment(toElement, CompartmentTypes.SEQUENCE_LIFELINE_COMPARTMENT);
        int logicalSrcYPos = sourceCompartment.getLogicalRectangle().y;
        int logicalSrcXPos = (new UMLWidgetOperator(fromElement.getGraphObject())).getCenterPoint().x;
        int logicalDestYPos = destCompartment.getLogicalRectangle().y;
        int logicalDestXPos = (new UMLWidgetOperator(toElement.getGraphObject())).getCenterPoint().x;
                
        int lowest = (logicalSrcYPos<logicalDestYPos?logicalSrcYPos:logicalDestYPos);
        
        while(it.hasNext()){
            LinkOperator lnk = it.next();
            int bottom = (new UMLWidgetOperator(lnk.getSource())).getBottom();
            if (lowest<bottom){
                lowest = bottom;
            }
        }
        
        int ySrc = lowest-50;
        int xSrc = logicalSrcXPos;
        
        int yDest = ySrc;
        int xDest = logicalDestXPos;
        //resizeLifeLine if needed
        //TODO: add resize function
        
        //Select source element       
        fromElement.select();
        (new EventTool()).waitNoEvent(500);
        
        LinkPaletteOperator lpo = new LinkPaletteOperator(fromElement);
        
        Utils.log("SequenceDiagramOperator:createDeneraticRelationShipOnDiagram(): linkElementType=" +linkElementType );
        //creating message
        JComponentOperator linkButton = lpo.getLinkButtonByIndex(linkElementType);
        Point buttonClickPoint = lpo.getClickPoint(linkButton);
        Utils.log("palette button click point = "+ buttonClickPoint.x + ", "+ buttonClickPoint.y);
//        Point elementClickPoint = toElement.getCenterPoint();
//        Utils.log("element click point = "+ elementClickPoint.x + ", "+ elementClickPoint.y);
        JemmyProperties.setCurrentDispatchingModel(JemmyProperties.ROBOT_MODEL_MASK);
        linkButton.clickMouse();
          
          
//        fromElement.center();
//        new Timeout("",500).sleep();
         Point devSrcPoint = scene.convertViewToScene(new Point(xSrc, ySrc));
         fromElement.clickOn(devSrcPoint, 1, InputEvent.BUTTON1_MASK, 0);
         new Timeout("",500).sleep();
         
         toElement.center();
         new Timeout("",500).sleep();
         Point devDestPoint = scene.convertViewToScene(new Point(xDest, yDest));        
         toElement.clickOn(devDestPoint, 1, InputEvent.BUTTON1_MASK, 0);
//        new Timeout("",500).sleep();
//        paletter().selectToolByType(linkElementType);
//        toolbar().selectDefault();        
    }
}
