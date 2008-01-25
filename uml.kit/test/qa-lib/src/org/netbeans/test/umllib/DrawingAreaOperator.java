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



package org.netbeans.test.umllib;
import com.tomsawyer.drawing.geometry.TSConstPoint;//look in tsalleditor601dev.jar
import java.awt.AWTException;
import java.awt.Color;
//import com.tomsawyer.util.TSConstPoint; changed with buzz3
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.Timeout;
import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.input.KeyRobotDriver;
import org.netbeans.jemmy.drivers.input.MouseRobotDriver;
import org.netbeans.jemmy.operators.ComponentOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETPoint;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.applicationmanager.LabelPresentation;
import org.netbeans.modules.uml.ui.support.applicationmanager.NodePresentation;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.PointConversions;
import org.netbeans.modules.uml.ui.swing.drawingarea.ADGraphWindow;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.netbeans.test.umllib.actions.Actionable;
import org.netbeans.test.umllib.exceptions.UMLCommonException;
import org.netbeans.test.umllib.util.JPopupByPointChooser;


/**
 * This is operator for diagram drawing area.
 * (Drawing area is the area under toolbar where 
 * diagram elements and links are located ).
 * This operator extends ComponentOperator 
 * so look carefully on inherited methods
 */
public class DrawingAreaOperator extends ComponentOperator implements Actionable{   
    private final int WIDTH_BORDER = 100;
    private final int HEIGHT_BORDER = 100;
       
    static{
        DriverManager.setDriver(DriverManager.MOUSE_DRIVER_ID, new MouseRobotDriver(new Timeout("",10)), DrawingAreaOperator.class);        
        DriverManager.setKeyDriver(new KeyRobotDriver(new Timeout("autoDelay",50)));     
    }
    
    /**
     * Constructs new DrawingAreaOperator using instance of
     * ADGraphWindow.
     * @param area instance of ADGraphWindow.
     */
    public DrawingAreaOperator(ADGraphWindow area) {
        super(area);        
    }
    
        
    /**
     * Returns underlying ADGraphWindow
     * @return underlying ADGraphWindow
     */
    public ADGraphWindow getArea(){
        return (ADGraphWindow)getSource();
    } 
    
    /**
     * Returns underlying ADGraphWindow ZoomLevel
     * @return double 
     */
    public double getZoomLevel()
    {
       return  getArea().getZoomLevel();
    }
    
    /**
     * Return free point. You may use such point 
     * to place an element or invoke popup menu
     * @return free point
     */
    public Point getFreePoint(){
        return getFreePoint(10);
    }
    
    /**
     * Return free point. the nearest element would be not closer than 
     * <CODE>span</CODE> points.
     * You may use such point to place an element or invoke popup menu
     * @param span Minimal distance to the nearest element
     * @return free point
     */
    public Point getFreePoint(int span) {
        //
        try{Thread.sleep(100);}catch(Exception ex){}
        //
        int width = getSource().getWidth();
        int height = getSource().getHeight();
        IDrawingAreaControl daControl = getArea().getDrawingArea();        
        //searching for elements matching elemenFinder criteria
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
        for(int w=WIDTH_BORDER;w<width-WIDTH_BORDER;w+=10) {
            for(int h=HEIGHT_BORDER;h<height-HEIGHT_BORDER;h+=10 ) {
                p =  daControl.deviceToLogicalPoint(w, h);
                boolean pointIsFree = true;
                for (int i=0; i<elementBounds.size(); i++){
                    if (elementBounds.get(i).contains(p)){
                        pointIsFree = false;
                        break;
                    }
                }
                if (pointIsFree){
                    //add small links check
                     ETList<IPresentationElement> tmp1=daControl.getAllEdgesViaRect(new ETRect(p.getX(),p.getY(),0,0),true);
                     ETList<IPresentationElement> tmp2=daControl.getAllEdgesViaRect(new ETRect(p.getX(),p.getY(),1,1),true);
                     ETList<IPresentationElement> tmp3=daControl.getAllEdgesViaRect(new ETRect(p.getX()-1,p.getY()-1,2,2),true);
                     ETList<IPresentationElement> tmp4=daControl.getAllEdgesViaRect(new ETRect(p.getX()-2,p.getY()-2,3,3),true);
                     ETList<IPresentationElement> tmp5=daControl.getAllEdgesViaRect(new ETRect(p.getX()-2,p.getY()-2,4,4),true);
                     if(tmp1==null && tmp2==null && tmp3==null && tmp4==null && tmp5==null)
                         return daControl.logicalToDevicePoint(p).asPoint();
                }
            }
        }
        
        return centerAtPoint(new Point(width+50,height/2));
    }
    
    
    /**
     * 
     * @param point 
     * @return 
     */
    boolean isFreePoint(Point point){
        IDrawingAreaControl daControl = getArea().getDrawingArea();        
        
        //searching for elements 
        ETList<IETGraphObject> allGraphs = daControl.getAllItems6();
        Iterator<IETGraphObject> tsIt = allGraphs.iterator();
        ArrayList<IETRect> elementBounds = new ArrayList<IETRect>();
        while(tsIt.hasNext()) {
            IETGraphObject graphObject = tsIt.next();
            IPresentationElement presElement = graphObject.getPresentationElement();
            if (presElement == null) {
                continue;
            }
            if(!(presElement instanceof NodePresentation || presElement instanceof LabelPresentation)) { //We are looking only for nodes here
                continue;
            }
            IETRect rect = graphObject.getEngine().getLogicalBoundingRect(true);
            rect.inflate(5);
            elementBounds.add(rect);
        }
        
        IETPoint p = null;
        p =  daControl.deviceToLogicalPoint(point.x, point.y);
        for (int i=0; i<elementBounds.size(); i++){
            if (elementBounds.get(i).contains(p)){
                return false;
            }
        }
        return true;
    }
    
    
    /**
     * centers at the device point specified and returns the new device coordinates of the point specified
     * @param point device point to center at
     * @return the new device coordinates of the point specified
     */
    public Point centerAtPoint(Point point) {        
        IDrawingAreaControl daControl = getArea().getDrawingArea();
        IETPoint etPoint = daControl.deviceToLogicalPoint(point.x,point.y);
        TSConstPoint tsPoint = PointConversions.ETPointToTSPoint(etPoint);
        getArea().centerPointInWindow(tsPoint, true);
        new EventTool().waitNoEvent(500);
        return daControl.logicalToDevicePoint(PointConversions.newETPoint(tsPoint)).asPoint();
    }


    
    
    public void select(){
        getOutput().printTrace("Diagram is not a selectable object.");
    }
    
    public void addToSelection(){
        getOutput().printTrace("Diagram is not a selectable object.");
    }
    
    
    /**
     * Invokes popup menu
     * @return  popup menu operator
     */
    public JPopupMenuOperator getPopup(){
        Point clickPoint = getFreePoint(20);        
        //workarround for Issue 79519
        if(System.getProperty("os.name").toLowerCase().indexOf("windows")==-1)
        {
            clickMouse(clickPoint.x,clickPoint.y,1);
            try{Thread.sleep(100);}catch(Exception ex){}
        }
        clickForPopup(clickPoint.x, clickPoint.y);
                JPopupMenuOperator ret=new JPopupMenuOperator(JPopupMenuOperator.waitJPopupMenu((java.awt.Container)(MainWindowOperator.getDefault().getSource()),new JPopupByPointChooser(clickPoint,getSource(),0)));
        //
        return ret;         
    }    
    
    /**
     * click in empty space somewhere
     **/
    public void clickMouse() {
        Point fr=getFreePoint(25);
        super.clickMouse(fr.x,fr.y,1);
    }
    //
    /**
     * 
     * @param x 
     * @param y 
     * @param count 
     * @param movetovisible 
     */
    public void clickMouse(int x,int y,int count,boolean movetovisible)
    {
        ComponentOperator o=(ComponentOperator)this;
        int oldCX=o.getX();
        int oldCY=o.getY();
        java.awt.Rectangle rel_bnds=o.getBounds();
        if(!rel_bnds.contains(x,y) && movetovisible)
        {
            Point tmp=centerAtPoint(new Point(x,y));
            x=tmp.x;
            y=tmp.y;
        }
        super.clickMouse(x,y,count);
    }

    /**
     * 
     * @param p 
     * @return 
     */
    public Point relativeToScreenPoint(Point p)
    {
        return new Point(p.x+getLocationOnScreen().x,p.y+getLocationOnScreen().y);
    }
    
    //native - not api functions
    /**
     * helps to find lines/points on diagram differs from surrounding area
     * works only for visible area
     * @param startPoint 
     * @param step_x 
     * @param step_y 
     * @param edgeClr should be null for auto
     * @return 
     */
    public Point getSolidEdge(Point startPoint,int step_x,int step_y,Color edgeClr)
    {
        Point ret=null;
        //
        ComponentOperator o=(ComponentOperator)this;
        //colors usually have no gradients on x axe
        int big_color_shift=Math.min(2*Math.abs(step_x)+10*Math.abs(step_y),127);        
        //
        int oldX=startPoint.x;
        int oldY=startPoint.y;
        java.awt.Robot rbt=null;
        try {
            rbt=new java.awt.Robot();
        } catch (AWTException ex) {
            ex.printStackTrace();
        }
        Color oldClr=rbt.getPixelColor(oldX+getLocationOnScreen().x,oldY+getLocationOnScreen().y);
        Color prevClr=oldClr;
        int oldR=oldClr.getRed();
        int oldG=oldClr.getGreen();
        int oldB=oldClr.getBlue();
        int prevR=prevClr.getRed();
        int prevG=prevClr.getGreen();
        int prevB=prevClr.getBlue();
        for(int x=oldX,y=oldY;x<(getBounds().x+getBounds().width) && x>getBounds().x && y<(getBounds().y+getBounds().height) && y>getBounds().y;x+=step_x,y+=step_y)
        {
            Color clr=rbt.getPixelColor(x+getLocationOnScreen().x,y+getLocationOnScreen().y);
            rbt.mouseMove(x+getLocationOnScreen().x,y+getLocationOnScreen().y);
            //new Timeout("",50).sleep();
            if(edgeClr!=null)
            {
                if(clr.equals(edgeClr))return new Point(x,y);
            }
            else
            {
                int r=clr.getRed();
                int g=clr.getGreen();
                int b=clr.getBlue();
                //check difference, try to take into account gradients
                if(Math.abs(prevR-r)>big_color_shift || Math.abs(prevG-g)>big_color_shift || Math.abs(prevB-b)>big_color_shift)
                {
                    //if(true)throw new UMLCommonException(Math.abs(prevR-r)+":"+Math.abs(prevG-g)+":"+Math.abs(prevB-b));
                    return new Point(x,y);
                }
                //
            }
            prevClr=clr;
            prevR=prevClr.getRed();
            prevG=prevClr.getGreen();
            prevB=prevClr.getBlue();
        }
        return ret;
    }

}
