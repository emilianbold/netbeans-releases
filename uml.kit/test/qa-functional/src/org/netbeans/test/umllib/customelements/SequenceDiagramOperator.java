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



package org.netbeans.test.umllib.customelements;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Timeout;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.support.umlsupport.ETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.applicationmanager.NodePresentation;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.netbeans.test.umllib.CompartmentTypes;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.LinkOperator;
import org.netbeans.test.umllib.LinkTypes;
import org.netbeans.test.umllib.SetName;
import org.netbeans.test.umllib.exceptions.NotFoundException;
import org.netbeans.test.umllib.util.LibProperties;

public class SequenceDiagramOperator extends DiagramOperator {
    private int logicalTop = 50;
    public SequenceDiagramOperator(String name){
        super(name);
        IDrawingAreaControl daControl = getDrawingArea().getArea().getDrawingArea();
        logicalTop = daControl.deviceToLogicalPoint(10, 50).getY();
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
        IDrawingAreaControl daControl = getDrawingArea().getArea().getDrawingArea();        
        
        
        ETList<IETGraphObject> allGraphs = daControl.getAllItems6();
        Iterator<IETGraphObject> tsIt = allGraphs.iterator();
        ArrayList<IETRect> elementBounds = new ArrayList<IETRect>();
        while(tsIt.hasNext()) {
            IETGraphObject graphObject = tsIt.next();            
            IPresentationElement presElement = graphObject.getPresentationElement();
            if (presElement == null) {
                continue;
            }
            if(!(presElement instanceof NodePresentation)) { //We are looking only for nodes here
                continue;
            }
            
            IETRect rect = graphObject.getEngine().getLogicalBoundingRect(true);
            rect.inflate(10+span);
            elementBounds.add(rect);
        }
        
        IETPoint p = null;
        int h = daControl.logicalToDevicePoint(new ETPoint(0,logicalTop)).getY();
        for(int w=50;w<width-50;w+=10) {
            p =  daControl.deviceToLogicalPoint(w, h);            
            boolean pointIsFree = true;
            for (int i=0; i<elementBounds.size(); i++){
                if (elementBounds.get(i).getRight()>p.getX()){
                    pointIsFree = false;
                    break;
                }
            }
            if (pointIsFree){
                return daControl.logicalToDevicePoint(p).asPoint();
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
        int logicalSrcXPos = fromElement.getGraphObject().getEngine().getLogicalBoundingRect(true).getCenterPoint().x;
        int logicalDestYPos = destCompartment.getLogicalRectangle().y;
        int logicalDestXPos = toElement.getGraphObject().getEngine().getLogicalBoundingRect(true).getCenterPoint().x;
                
        int lowest = (logicalSrcYPos<logicalDestYPos?logicalSrcYPos:logicalDestYPos);
        
        while(it.hasNext()){
            LinkOperator lnk = it.next();
            int bottom = lnk.getSource().getEngine().getLogicalBoundingRect(true).getBottom();
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
        
        //creating message
        paletter().selectToolByType(linkElementType);
        fromElement.center();
        new Timeout("",500).sleep();
        Point devSrcPoint = this.getDrawingAreaControl().logicalToDevicePoint(new ETPoint(xSrc, ySrc)).asPoint();
        fromElement.clickOn(devSrcPoint, 1, InputEvent.BUTTON1_MASK, 0);
        new Timeout("",500).sleep();
        
        toElement.center();
        new Timeout("",500).sleep();
        Point devDestPoint = this.getDrawingAreaControl().logicalToDevicePoint(new ETPoint(xDest, yDest)).asPoint();        
        toElement.clickOn(devDestPoint, 1, InputEvent.BUTTON1_MASK, 0);
        new Timeout("",500).sleep();
        paletter().selectToolByType(linkElementType);
        toolbar().selectDefault();        
    }
    
    
    
}
