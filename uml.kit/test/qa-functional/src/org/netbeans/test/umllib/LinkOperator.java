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


 /*
 * LinkOperator.java
 *
 * Created on February 15, 2005, 6:42 PM
 */

package org.netbeans.test.umllib;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import java.util.ArrayList;
import java.util.Comparator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import java.util.TreeSet;
import org.netbeans.api.visual.graph.GraphScene;
import org.netbeans.api.visual.widget.ConnectionWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jemmy.EventTool;

import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Timeout;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.drawingarea.view.UMLLabelWidget;
import org.netbeans.test.umllib.actions.Actionable;
import org.netbeans.test.umllib.exceptions.NotFoundException;

/**
 * This is operator for a link beween diagram elements.
 * Using it you can perform various operations on link.
 * @author Alexei Mokeev, Sherry Zhou
 */
public class LinkOperator implements Actionable{
    private Widget edge = null;
    private Widget to = null;
    private Widget from = null;
    private DiagramOperator diagramOperator = null;
    private UMLWidgetOperator edgeOpr = null; 
    //absolute correction for x and y for click/get point (should be removed if the nature of shift will be discovered)
    //for now it seems like different approximation to integer in different parts of link creation
    //for now shift to right bottom works with 100%,200%,400% zoom and horizontal/vertical link orientation
    //do not works with 50%,25% zooms but there is a problem with popup in these zooms in manual steps
    private int pnt_shift_x=2;
    private int pnt_shift_y=2;
    
    //private IDrawingAreaControl drawingAreaControl = null;
    
    public static final long WAIT_LINK_TIMEOUT = 30000;
    
    static {
        Timeouts.initDefault("LinkOperator.WaitLinkTime", WAIT_LINK_TIMEOUT);
    }
    
    /**
     * 
     * @param diagram 
     * @param edge 
     */
     public LinkOperator(DiagramOperator diagram, Widget edge) {
        this.edge = edge;
        to = getToNode(edge);//We should get class cast in case of an error
        from = getFromNode(edge);
        diagramOperator = diagram;
        edgeOpr = new UMLWidgetOperator(edge);
    }
    
   
    /**
     * Creates a new instance of LinkOperator
     * @param source 
     * @param destination 
     * @param linkType 
     * @param index 
     * @throws qa.uml.exceptions.NotFoundException 
     */
    public LinkOperator(DiagramElementOperator source, DiagramElementOperator destination, LinkTypes linkType, int index) throws NotFoundException{
        this(source.getDiagram(), waitForLink(source, destination, linkType, index).getSource());
        
    }
    
    /**
     * 
     * @param source 
     * @param destination 
     * @param linkType 
     * @throws qa.uml.exceptions.NotFoundException 
     */
    public LinkOperator(DiagramElementOperator source, DiagramElementOperator destination, LinkTypes linkType)  throws NotFoundException {
        this(source, destination, linkType, 0);
    }
    
    /**
     * 
     * @param source 
     * @param destination 
     * @throws qa.uml.exceptions.NotFoundException 
     */
    public LinkOperator(DiagramElementOperator source, DiagramElementOperator destination)  throws NotFoundException{
        this(source, destination, LinkTypes.ANY, 0);
    }
    
    
    /**
     * 
     * @return 
     */
    public DiagramOperator getDiagramOperator(){
        return diagramOperator;
    }
    
   
    //Methods from Actionable interface
    /**
     * 
     * @return 
     */
    public JPopupMenuOperator getPopup() {
        try{Thread.sleep(100);}catch(Exception ex){}
        Point p = getPointForClick();
        //workarround for Issue 79519
        if(System.getProperty("os.name").toLowerCase().indexOf("windows")==-1)
        {
            diagramOperator.getDrawingArea().clickMouse(p.x,p.y,1);
            try{Thread.sleep(100);}catch(Exception ex){}
        }
        diagramOperator.getDrawingArea().clickForPopup(p.x, p.y);
        
        
        JPopupMenuOperator ret=new JPopupMenuOperator(JPopupMenuOperator.waitJPopupMenu((Container)(MainWindowOperator.getDefault().getSource()),new org.netbeans.test.umllib.util.JPopupByPointChooser(diagramOperator.getDrawingArea().relativeToScreenPoint(p))));

        return ret;
    }
    
    
    
    
    /**
     * gets point that belongs to a link and centers on it.
     * The updated device point is returned
     * @param point 
     * @return 
     */
    public Point makeVisible(Point point){
        //TODO: wait for trey's center api
// 6.0       ADGraphWindow area = diagramOperator.getDrawingArea().getArea();
//        IDrawingAreaControl daControl = area.getDrawingArea();
//        
//        IETPoint etPoint = daControl.deviceToLogicalPoint(point.x,point.y);
//        
//        IETRect eDeviceAreaRect = new ETRect(area.getVisibleRect());
//        IETRect eVisibleAreaRect = daControl.deviceToLogicalRect(eDeviceAreaRect);
//        IETRect eElementRect = edge.getEngine().getLogicalBoundingRect(true);
//        if (!eVisibleAreaRect.contains(etPoint)){
//            diagramOperator.getDrawingArea().centerAtPoint(point);            
//        }
//6.0        return daControl.logicalToDevicePoint(etPoint).asPoint();
        return point;
    }
    
    
    
    public void center() {
        center(false, false);
    }
    
    
    /**
     *
     * @param selectIt
     * @param deselectOthers
     */
    public void center(boolean selectIt, boolean deselectOthers) {
        // TODO: Wait for Trey's api
//6.0        ADGraphWindow area = diagramOperator.getDrawingArea().getArea();
//6.0        area.getDrawingArea().centerPresentationElement(edge.getPresentationElement(), selectIt, deselectOthers);
    }
    
    
    
    public void select() {
        Point p = getPointForClick();

        diagramOperator.getDrawingArea().moveMouse(p.x, p.y);
        new Timeout("", 10).sleep();
        diagramOperator.getDrawingArea().clickMouse(p.x, p.y, 1);
        new Timeout("", 50).sleep();

        //somehow the above doesn't always work so we add a little hack     
        if (!edgeOpr.isSelected()) {
            //dragndrop doesn't work too :(
            int x1 = p.x;
            int y1 = p.y;
            for (int i = 1; i < 10; i++) {
                x1 = p.x + i;
                diagramOperator.getDrawingArea().clickMouse(x1, y1, 1);
                new EventTool().waitNoEvent(500);
                if (edgeOpr.isSelected()) {
                    break;
                }
                x1 = p.x - i;
                diagramOperator.getDrawingArea().clickMouse(x1, y1, 1);
                new EventTool().waitNoEvent(500);
                if (edgeOpr.isSelected()) {
                    break;
                }
                y1 = p.y + i;
                diagramOperator.getDrawingArea().clickMouse(x1, y1, 1);
                new EventTool().waitNoEvent(500);
                if (edgeOpr.isSelected()) {
                    break;
                }
                y1 = p.y - i;
                diagramOperator.getDrawingArea().clickMouse(x1, y1, 1);
                new EventTool().waitNoEvent(500);
                if (edgeOpr.isSelected()) {
                    break;
                }
            }
            diagramOperator.getDrawingArea().moveMouse(p.x, p.y);
        }
    }
    
    public void addToSelection() {
        Point p = getPointForClick();
        new Timeout("",10).sleep();
        diagramOperator.getDrawingArea().clickMouse(p.x, p.y,1, InputEvent.BUTTON1_MASK, KeyEvent.CTRL_DOWN_MASK);
        new Timeout("",50).sleep();
    }
    
    /**
     * 
     * @return 
     */
    public Widget getSource() {
        return edge;
    }
    
     /*
     *  Find edge's source widget
     */
    public static Widget getToNode(Widget edge) {
        DesignerScene scene =(DesignerScene)edge.getScene();
        IPresentationElement edgePre = (IPresentationElement)scene.findObject(edge);
        IPresentationElement sourcePre =scene.getEdgeSource(edgePre);
        return scene.findWidget(sourcePre);
    }
    
    /*
     *  Find edge's target nodet
     */
    public static Widget getFromNode(Widget edge) {
        DesignerScene scene =(DesignerScene)edge.getScene();
        IPresentationElement edgePre = (IPresentationElement)scene.findObject(edge);
        IPresentationElement targetPre =scene.getEdgeTarget(edgePre);
        return scene.findWidget(targetPre);
    }
    
       
    
    /**
     * 
     * @return current edge's target node
     */
    public Widget getToNode() {
        return to;
    }
    
    /**
     * 
     * @return current edge's source node
     */
    public Widget getFromNode() {
        return from;
    }
    
    /**
     * 
     * @return 
     */
    public boolean hasBends() {
       if (getBends().size()>=1)
           return true;
       else
           return false;
    }
    
    /**
     * 
     * @return 
     */
    public List getBends() {
        return ((ConnectionWidget)edge).getControlPoints();
         
    }
    
    /**
     * 
     * @return 
     */
    public String getType() {
        UMLWidgetOperator edgeOpr = new UMLWidgetOperator(edge);
         return edgeOpr.getElementType();
        }
    
    /**
     * 
     * @return 
     */
    public ArrayList<DiagramLabelOperator> getLabels() {
        List<Widget> list = edge.getChildren();
        ArrayList<DiagramLabelOperator> res = new ArrayList<DiagramLabelOperator>();
        for (Widget child : list) {
            if (child instanceof UMLLabelWidget) {

                res.add(new DiagramLabelOperator(diagramOperator, child));
            }
        }
        return res;
    }
    
        
    /**
     *
     *@return 
     */
    public String[] getLabelsTexts() {
        List<Widget> list = edge.getChildren();
        String[] res=new String[list.size()];
        int cnt=0;
        for (Widget child : list) {
            if ( child instanceof UMLLabelWidget){
               res[cnt++]=((UMLLabelWidget)child).getLabel(); 
            }
        }
        return res;
 
    }
    
    
    /**
     * 
     * @param text 
     * @param index 
     * @param exact 
     * @param caseSensitive 
     * @return 
     */
   public DiagramLabelOperator getLabel(String text,int index, boolean exact, boolean caseSensitive){
       ArrayList<DiagramLabelOperator> labels = getLabels();
       int ind_count=0;
       Operator.DefaultStringComparator comp = new Operator.DefaultStringComparator(exact, caseSensitive);
       for(int i=0; i<labels.size(); i++){
           if (comp.equals(labels.get(i).getText(),text)){
               if(index<=ind_count)return labels.get(i);
               else ind_count++;
           }
       }
       return null;
   }
    /**
     * 
     * @param text 
     * @param index 
     * @return 
     */
    public DiagramLabelOperator getLabel(String text,int index){
        return getLabel(text,index,false,false);
    }
    /**
     * 
     * @param text 
     * @return 
     */
    public DiagramLabelOperator getLabel(String text){
        return getLabel(text,0);
    }
    
    /**
     * 
     * @return 
     */
    private Point getPointForClick() {
        return getPointForClick(true);
    }
    /**
     * find point and avoid click on labels
     * @return 
     */
    private Point getPointForClick(boolean findEmptyPoint) {
         //TODO: Wait for Trey on getSourceClippingPoint and getTargetClippingPoint()
        //6.0
//        TSConstPoint fromPoint =((ETEdge)edge).getSourceClippingPoint();
//        TSConstPoint toPoint =((ETEdge)edge).getTargetClippingPoint();
//        List bendPoints = ((ETEdge)edge).bendPoints();
//        if((bendPoints!=null)&&(bendPoints.size() >=1)) {
//            fromPoint = (TSConstPoint)bendPoints.get(bendPoints.size()-1);
//        }
//        double y = (fromPoint.getY()+toPoint.getY())*0.5;
//        double x = (fromPoint.getX()+toPoint.getX())*0.5;        
//        Point tmp=diagramOperator.getDrawingAreaControl().logicalToDevicePoint(PointConversions.newETPoint(new TSConstPoint(x,y))).asPoint();        
//        tmp.translate(pnt_shift_x, pnt_shift_y);
//        tmp = makeVisible(tmp);
//        //check also +-2 area
//        /*Point a=new Point(tmp);a.translate(2,2);
//        Point b=new Point(tmp);b.translate(-2,2);
//        Point c=new Point(tmp);c.translate(-2,-2);
//        Point d=new Point(tmp);d.translate(2,-2);*/
//        boolean notFreeArea=!diagramOperator.getDrawingArea().isFreePoint(tmp);// || !diagramOperator.getDrawingArea().isFreePoint(a) || !diagramOperator.getDrawingArea().isFreePoint(b) ||!diagramOperator.getDrawingArea().isFreePoint(c) || !diagramOperator.getDrawingArea().isFreePoint(d);
//        long counter=0;//avoid infinit search
//        while(notFreeArea && findEmptyPoint && counter<2000){
//            y = (y+toPoint.getY())*0.5;
//            x = (x+toPoint.getX())*0.5;        
//            tmp=diagramOperator.getDrawingAreaControl().logicalToDevicePoint(PointConversions.newETPoint(new TSConstPoint(x,y))).asPoint();                    
//            tmp.translate(pnt_shift_x, pnt_shift_y);
//            tmp = makeVisible(tmp);
//            notFreeArea=!diagramOperator.getDrawingArea().isFreePoint(tmp);// || !diagramOperator.getDrawingArea().isFreePoint(a) || !diagramOperator.getDrawingArea().isFreePoint(b) ||!diagramOperator.getDrawingArea().isFreePoint(c) || !diagramOperator.getDrawingArea().isFreePoint(d);
//            counter++;
//        }
//        return tmp;  
        //6.0
        return null;
    }
    
    /**
     * 
     * @return Point close to link center of close to last section center
     */
    public Point getNearCenterPoint()
    {
        return getPointForClick();
    }
    /**
     * 
     * @return Point close to link center of close to last section center
     */
    public Point getNearCenterPointWithoutOverlayCheck()
    {
        return getPointForClick(false);
    }
    
    /**
     *
     * @return Point close to Target clipping point (with about 5points shift from the edge)
     */
   public Point getNearTargetPoint()
    {
       return getNearTargetPoint(5);
    }
    /**
     * @param edgeShift - shift in local point from edge
     * @return Point close to Target clipping point (with about edgeShift points shift from the edge)
     */
    public Point getNearTargetPoint(int edgeShift)
    {
     
         //TODO: Wait for Trey on getSourceClippingPoint and getTargetClippingPoint()
        //TSConstPoint fromPoint =((ETEdge)edge).getSourceClippingPoint();
//        TSConstPoint toPoint =((ETEdge)edge).getTargetClippingPoint();
//        List bendPoints = ((ETEdge)edge).bendPoints();
//        if((bendPoints!=null)&&(bendPoints.size() >=1)) {
//            fromPoint = (TSConstPoint)bendPoints.get(bendPoints.size()-1);
//        }
//        double toX=toPoint.getX();
//        double toY=toPoint.getY();
//        double frX=fromPoint.getX();
//        double frY=fromPoint.getY();
//         double len=Math.sqrt((frY-toY)*(frY-toY)+(frX-toX)*(frX-toX));
//       double y = toY+edgeShift*(frY-toY)/len;
//        double x = toX+edgeShift*(frX-toX)/len;
//        
//        Point tmp=diagramOperator.getDrawingAreaControl().logicalToDevicePoint(PointConversions.newETPoint(new TSConstPoint(x,y))).asPoint();
//         tmp.translate(pnt_shift_x,pnt_shift_y);
//6.0       return tmp; 
         return new Point(10,10);
    }
    /**
     *
     * @return Point close to Source clipping point (with about 5points shift from the edge)
     */
    public Point getNearSourcePoint()
    {
        return getNearSourcePoint(5);
    }
     /**
     * @param edgeShift - shift in local point from edge
     * @return Point close to Target clipping point (with about edgeShift points shift from the edge)
     */
   public Point getNearSourcePoint(int edgeShift)
    {
       //TODO: Wait for Trey on getSourceClippingPoint and getTargetClippingPoint()
//        TSConstPoint fromPoint =((ETEdge)edge).getSourceClippingPoint();
//        TSConstPoint toPoint =((ETEdge)edge).getTargetClippingPoint();
//        List bendPoints = ((ETEdge)edge).bendPoints();
//        if((bendPoints!=null)&&(bendPoints.size() >=1)) {
//            toPoint = (TSConstPoint)bendPoints.get(0);
//        }
//        double toX=toPoint.getX();
//        double toY=toPoint.getY();
//        double frX=fromPoint.getX();
//        double frY=fromPoint.getY();
//        double len=Math.sqrt((frY-toY)*(frY-toY)+(frX-toX)*(frX-toX));
//        double y = frY-edgeShift*(frY-toY)/len;
//        double x = frX-edgeShift*(frX-toX)/len;
//        
//        Point tmp=diagramOperator.getDrawingAreaControl().logicalToDevicePoint(PointConversions.newETPoint(new TSConstPoint(x,y))).asPoint();
//        tmp.translate(pnt_shift_x,pnt_shift_y);
//        return tmp;   
       return null;
    }
    
    /**
     * 
     * @param obj 
     * @return 
     */
    public boolean equals(Object obj) {
        //was compare with equals
        //now with references
        if((obj instanceof LinkOperator)&&(((LinkOperator)obj).getSource()==(edge))) {
            return true;
        }else {
            return false;
        }
    }
    /**
     * 
     * @param source 
     * @param destination 
     * @throws qa.uml.exceptions.NotFoundException 
     * @return 
     */
    public static LinkOperator waitForLink(final DiagramElementOperator source, final DiagramElementOperator destination)  throws NotFoundException {
        return waitForLink(source, destination, new LinkByTypeChooser(LinkTypes.ANY), 0);
    }
    
    
    /**
     * 
     * @return 
     * @param linkType 
     * @param source 
     * @param destination 
     * @throws qa.uml.exceptions.NotFoundException 
     */
    public static LinkOperator waitForUndirectedLink(final DiagramElementOperator source, final DiagramElementOperator destination, final LinkTypes linkType)  throws NotFoundException {
        Waiter w = new Waiter(new Waitable() {
            public Object actionProduced(Object obj) {
                LinkOperator lnk = findLink(source, destination,  new LinkByTypeChooser(linkType), 0);
                if (lnk == null){
                    return findLink(destination, source, new LinkByTypeChooser(linkType), 0);
                } else{
                    return lnk;
                }
            }
            public String getDescription() {
                return("Wait for undirected link " + linkType);
            }
        });
        
        return waitForLink(source, destination, new LinkByTypeChooser(linkType), 0, w);
    }
    
    /**
     * 
     * @param source 
     * @param destination 
     * @param linkType 
     * @throws qa.uml.exceptions.NotFoundException 
     * @return 
     */
    public static LinkOperator waitForLink(final DiagramElementOperator source, final DiagramElementOperator destination, LinkTypes linkType)  throws NotFoundException {
        return waitForLink(source, destination, new LinkByTypeChooser(linkType), 0);
    }
    
    /**
     * 
     * @param source 
     * @param destination 
     * @param linkType 
     * @param index 
     * @throws qa.uml.exceptions.NotFoundException 
     * @return 
     */
    public static LinkOperator waitForLink(final DiagramElementOperator source, final DiagramElementOperator destination, LinkTypes linkType, int index)  throws NotFoundException {
        return waitForLink(source, destination, new LinkByTypeChooser(linkType), index);
    }
    /**
     * 
     * @param source 
     * @param destination 
     * @param linkChooser 
     * @throws qa.uml.exceptions.NotFoundException 
     * @return 
     */
    public static LinkOperator waitForLink(final DiagramElementOperator source, final DiagramElementOperator destination, final LinkChooser linkChooser)  throws NotFoundException {
        return waitForLink(source, destination, linkChooser, 0);
    }
    
    /**
     * 
     * @param source 
     * @param destination 
     * @param linkChooser 
     * @param index 
     * @throws qa.uml.exceptions.NotFoundException 
     * @return 
     */
    public static LinkOperator waitForLink(final DiagramElementOperator source, final DiagramElementOperator destination, final LinkChooser linkChooser, final int index) throws NotFoundException {        
        Waiter w = new Waiter(new Waitable() {
            public Object actionProduced(Object obj) {
                return findLink(source, destination, linkChooser, index);
            }
            public String getDescription() {
                //
                String all="\nfrom source: ";
                Object tmp[];
                tmp=source.getLinks().toArray();
                for(int i=0;i<tmp.length;i++)all+=((LinkOperator)tmp[i]).getName()+"|"+((LinkOperator)tmp[i]).getType()+"|"+((LinkOperator)tmp[i]).getFromNode()+"|"+((LinkOperator)tmp[i]).getToNode()+";";
                tmp=destination.getLinks().toArray();
                all+="\nfrom destination: ";
                for(int i=0;i<tmp.length;i++)all+=((LinkOperator)tmp[i]).getName()+"|"+((LinkOperator)tmp[i]).getType()+"|"+((LinkOperator)tmp[i]).getFromNode()+"|"+((LinkOperator)tmp[i]).getToNode()+";";
                //
                return("Wait with link chooser: " + linkChooser.getDescription()+" between "+source.getName()+"/"+source.getType()+" and "+destination.getName()+"/"+destination.getType()+"///all:"+all+"///findResult: "+findLink(source, destination, linkChooser, index));
            }
        });
        return waitForLink(source, destination, linkChooser, index, w);          
    }
    
    
    /**
     * 
     * @return 
     */
    public boolean isSelected(){
        return edgeOpr.isSelected();
    }
        
    /**
     * 
     * @return 
     */
    public String getName()
    {
        return edgeOpr.getName();
    
        /*String ret=null;
        ETList<IETLabel> lbls= edge.getLabels();
        for(int i=0;lbls!=null && i<lbls.size();i++)
        {
            if(isNameLabel(lbls.get(i)))
            {
                ret=lbls.get(i).getText();
            }
        }
        return ret;*/
    }
    
//    private boolean isNameLabel(IETLabel sourceLabel)
    private boolean isNameLabel(String sourceLabel)
    {
        //TODO: need getLableKind();
//6.0        int kind=sourceLabel.getLabelKind();
//        //
//        boolean isName=kind==TSLabelKind.TSLK_ACTIVITYEDGE_NAME;
//        isName=isName || kind==TSLabelKind.TSLK_ASSOCIATION_NAME;
//6.0        isName=isName || kind==TSLabelKind.TSLK_NAME;
        /*isName=isName || kind==TSLabelKind.;
        isName=isName || kind==TSLabelKind.;
        isName=isName || kind==TSLabelKind.;
        isName=isName || kind==TSLabelKind.;
        isName=isName || kind==TSLabelKind.;
        isName=isName || kind==TSLabelKind.;
        isName=isName || kind==TSLabelKind.;
        isName=isName || kind==TSLabelKind.;
        isName=isName || kind==TSLabelKind.;
        isName=isName || kind==TSLabelKind.;*/
        return false;

    }
    
    /**
     * 
     * @return 
     * @param w 
     * @param source 
     * @param destination 
     * @param linkChooser 
     * @param index 
     * @throws qa.uml.exceptions.NotFoundException 
     */
    public static LinkOperator waitForLink(final DiagramElementOperator source, final DiagramElementOperator destination, final LinkChooser linkChooser, final int index, final Waiter w) throws NotFoundException {
        try{            
            Timeouts t = JemmyProperties.getCurrentTimeouts();
            t.setTimeout("Waiter.WaitingTime", t.getTimeout("LinkOperator.WaitLinkTime"));
            LinkOperator l = (LinkOperator)w.waitAction(null);
            if(l!=null) {
                return l;
            }
            throw new NotFoundException("Matching link was not found.");
        }catch(InterruptedException ie) {
            throw new NotFoundException("Link was not found due to runtime error.");
        }
    }
    
    
    
    
    /**
     * find link with certain order (i.e. from source to destination)
     * @param source 
     * @param destination 
     * @param linkChooser 
     * @param index 
     * @return 
     */
    public static LinkOperator findLink(DiagramElementOperator source, DiagramElementOperator destination, LinkChooser linkChooser, int index) {
        TreeSet<LinkOperator> linksFound = new TreeSet<LinkOperator>(new LinkComparator<LinkOperator>());
        if (( source == null ) || ( destination == null) || (linkChooser == null)) {
            return null;
        }
  
        //It is not the best way, but retainAll doesn't work :(
        HashSet<LinkOperator> outLinks = source.getOutLinks();
        HashSet<LinkOperator> inLinks = destination.getInLinks();
        HashSet<LinkOperator> intersection = new HashSet<LinkOperator>();
        Iterator<LinkOperator> inIt = inLinks.iterator();
        Iterator<LinkOperator> outIt = outLinks.iterator();
        
        while(inIt.hasNext()) {
            LinkOperator in = inIt.next();
            outIt = outLinks.iterator();
            while(outIt.hasNext()) {
                LinkOperator out = outIt.next();
                //should be compare references here?
               // if(out.getSource()==in.getSource()) {
                //or objects
                 if(out.getSource().equals(in.getSource())) {
                    intersection.add(in);
                }
            }
        }
        
        
        Iterator<LinkOperator> it = intersection.iterator();
        
        
        while( it.hasNext() ){
            LinkOperator edge =  it.next();
            if (linkChooser.checkLink(edge.getSource())){
                linksFound.add(edge);
          }
        }
        
        if(linksFound.size()>index) {
            return linksFound.toArray(new LinkOperator[1])[index];
        }
        return null; //Nothing suitable found
        
    }
    
    public String toString()
    {
        return super.toString()+" //name: "+getName()+"//type: "+getType()+"//";
    }
    
    public static class LinkByElementsChooser implements LinkChooser {
        
        private Widget from = null;
        private Widget to = null;
        private String linkType = null;
        
        /**
         * 
         * @param from 
         * @param to 
         */
        public LinkByElementsChooser(Widget from, Widget to){
            this(from,to,LinkTypes.ANY);
        }
        
        /**
         * 
         * @param from 
         * @param to 
         * @param linkType 
         */
        public LinkByElementsChooser(Widget from, Widget to, LinkTypes linkType){
            this.from = from;
            this.to = to;
            this.linkType = linkType.toString();
        }
        
        
        /**
         * 
         * @param edge 
         * @return 
         */
        public boolean checkLink(Widget edge) {
            Widget fromNode  = getFromNode(edge);
            Widget toNode = getToNode(edge);

            if (from.equals(fromNode) && to.equals(toNode)) {
                GraphScene scene = (GraphScene) edge.getScene();
                IPresentationElement presentation = (IPresentationElement) scene.findObject(edge);


                if (presentation == null) {
                    return false;
                }
                
                ETList<IElement> subjects = presentation.getSubjects();
                Iterator<IElement> itSubj = subjects.iterator();
                while (itSubj.hasNext()) {
                    IElement sbj = (IElement) itSubj.next();
                        if (sbj.getElementType().equals(linkType)) {
                            return true;
                        }
                    }
                }
            return false;
            }
      
        
        /**
         * 
         * @return 
         */
        public String getDescription( ) {
            return "Chooser for " + linkType + " link from:" + from + " to " + to;
        }
        
        
    }
    
    
   
    
    
   
    
    
    
    
    public static class LinkByTypeChooser implements LinkChooser {
        private String linkType = null;
        
        
        /**
         * 
         * @param linkType 
         */
        public LinkByTypeChooser(LinkTypes linkType){
            this.linkType = linkType.toString();
        }
        
        
        /**
         * 
         * @param edge 
         * @return 
         */
        public boolean checkLink(Widget edge) {
            GraphScene scene = (GraphScene) edge.getScene();
            IPresentationElement presentation = (IPresentationElement) scene.findObject(edge);

 
            if  (presentation == null ){
                return false;
            }
            if(LinkTypes.ANY.toString().equals(linkType)) {
                return true;
            }

            ETList subjects = presentation.getSubjects();
            Iterator itSubj = subjects.iterator();
            while (itSubj.hasNext()) {
                IElement sbj = (IElement) itSubj.next();
            if ( sbj.getElementType().equals(linkType) ){
                    return true;
                }
            }
          return false;   
        }
        
        
        /**
         * 
         * @return 
         */
        public String getDescription( ) {
            return "Chooser for link of type " + linkType;
        }
        
        
    }
    
    
    public static class LinkComparator<C extends LinkOperator> implements Comparator<C>{
        /**
         * 
         * @param o1 
         * @param o2 
         * @return 
         */
        public int compare(C o1, C o2){
             Widget o1s=o1.getSource();
             Widget o2s=o2.getSource();
//            if(o1s instanceof UMLEdgeWidget && o2s instanceof UMLEdgeWidget)
//            {
//                //if both are UMLEdgeWidget it's possible to compare by id which seems to be unique instead of borders center
//                if((((UMLEdgeWidget)o1s).getUMLWidgetID()()-((UMLEdgeWidget)o2s).UMLEdgeWidget())<0) return -1;
//                else if ((((ETEdge)o1s).getID()-((ETEdge)o2s).getID())>0)return 1;
//                else return 0;
//            }
//            else
//            {
               UMLWidgetOperator o1sOpr = new  UMLWidgetOperator(o1s);
               UMLWidgetOperator o2sOpr = new  UMLWidgetOperator(o2s);
               
                Point o1Center = o1sOpr.getCenterPoint();
                Point o2Center = o2sOpr.getCenterPoint();
                if (o1Center.y>o2Center.y){
                    return 1;
                }else if (o1Center.y == o2Center.y){
                    if(o1Center.x > o2Center.y){
                        return -1;
                    } else{
                        return 1;
                    }
                }else{
                    return -1;
                }
            }
             
        }
      

}
