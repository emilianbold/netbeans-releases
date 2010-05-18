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




package org.netbeans.test.umllib;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Timeout;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.drivers.input.MouseRobotDriver;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.drawingarea.view.DesignerScene;
import org.netbeans.modules.uml.drawingarea.view.UMLLabelWidget;
import org.netbeans.test.umllib.actions.Actionable;
import org.netbeans.test.umllib.exceptions.NotFoundException;
import org.netbeans.test.umllib.util.JPopupByPointChooser;

/**
 * This is operator for labels.
 */
public class DiagramLabelOperator implements Actionable {
    
    /**
     * Default delay for Compartment Waiter. Real value can be changed
     * as for the all other operators(For example via JemmyProperties)
     */
    public static final long WAIT_LABEL_TIMEOUT =  5000;
    
    static {
        Timeouts.initDefault("LabelOperator.WaitLabelTime", WAIT_LABEL_TIMEOUT);
    }
    

    protected Widget sourceLabel = null;
    private DiagramOperator diagramOperator = null;
    
    /**
     * 
     * @return 
     */
     protected Widget getSource(){
        return sourceLabel;
    }
    
    /**
     * Constructs label by text
     * @param dia Instance of DiagramOperator
     * @param text text
     * @throws qa.uml.exceptions.NotFoundException when no suitable label found
     */
    public DiagramLabelOperator(DiagramOperator dia, String text) throws NotFoundException{
        this(dia, waitForLabel(dia, new LabelByTextChooser(text), 0));
    }
    
    /**
     * Costruct label by text and index
     * @param dia - current diagram operator
     * @param text - label's text
     * @param index - index of the label
     * @throws qa.uml.exceptions.NotFoundException when no suitable label found     
     */
    public DiagramLabelOperator(DiagramOperator dia, String text, int index) throws NotFoundException{
        this(dia, waitForLabel(dia, new LabelByTextChooser(text), index));
    }
    
    /**
     * Costruct label by chooser and index
     * @param dia - current diagram operator
     * @param chooser - label's chooser
     * @param index - index of the label
     * @throws qa.uml.exceptions.NotFoundException when no suitable label found     
     */
    public DiagramLabelOperator(DiagramOperator dia, LabelChooser chooser, int index) throws NotFoundException{
        this(dia, waitForLabel(dia, chooser, index));
    }
    
    /**
     * Construct label by label class
     * @param dia 
     * @param label 
     */
    public DiagramLabelOperator(DiagramOperator dia, Widget label){
        sourceLabel = label;
        diagramOperator = dia;
    }
    
    
    /**
     *method return name of comparment from compartment source
     *@return compartmant name
     */
    public String getText(){
       return  ((UMLLabelWidget) sourceLabel).getLabel();
    }
    
    
    /**
     * Search for suitable graph object
     * @param diagramOperator Diagram to look for label
     * @param labelChooser custom chooser
     * @param index index
     * @throws qa.uml.exceptions.NotFoundException when nothing suitable found
     * @return UMLLabelWidget if found
     */
    public static Widget findLabel(DiagramOperator diagramOperator, LabelChooser labelChooser, int index) throws NotFoundException{
        ArrayList<Widget> elementsFound = new ArrayList<Widget>();
        DesignerScene scene = diagramOperator.getDesignerScene();
        Collection<IPresentationElement> children =scene.getNodes();
        for (IPresentationElement presElement : children)
        {
            Widget widget = scene.findWidget(presElement);  
 
            if (!(widget instanceof UMLLabelWidget)){
                continue;
            }
 
            if (labelChooser.checkLabel(widget)){
                elementsFound.add(widget);
            }
        }
        
        //sorting found elements
        LabelComparator<Widget> c = new LabelComparator<Widget>();
        Widget[] arr = (Widget[])elementsFound.toArray(new Widget[0]);
        Arrays.sort(arr,c);
        if (arr.length>index){
            return arr[index];
        }else{
            throw new NotFoundException("Element matching the criteria not found on diagram " + diagramOperator.getDiagramName());
        }
    }
    
    
    
    public static class LabelComparator<C extends Widget> implements Comparator<C>{
        /**
         *
         * @param o1
         * @param o2
         * @return
         */
        public int compare(C o1, C o2){
             Point o1Center = (new UMLWidgetOperator(o1)).getCenterPoint();
             Point o2Center = (new UMLWidgetOperator(o1)).getCenterPoint(); 
        
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
    
    /**
     * Wait for suitable graph object
     * @return Element's GraphObject if found
     * @param chooser 
     * @param diagramOperator Diagram to look for element
     * @param index index
     */
    public static Widget waitForLabel(final DiagramOperator diagramOperator, final LabelChooser chooser, final int index) {
    
        try{
            Waiter w = new Waiter(new Waitable() {
                public Object actionProduced(Object obj) {
                    try {
                        Widget go = findLabel(diagramOperator, chooser, index);
                        return go;
                    }catch(NotFoundException e) {
                        return null;
                    }
                }
                public String getDescription() {
                    return("Wait for " + chooser.getDescription());
                }
            });
            Timeouts t = JemmyProperties.getCurrentTimeouts();
            t.setTimeout("Waiter.WaitingTime", t.getTimeout("LabelOperator.WaitLabelTime"));
            return (Widget)w.waitAction(null);
            
        }catch(InterruptedException ie) {
            return null;
        }
    }
     
        
    
    //Methods from Actionable interface
    /**
     * 
     * @return 
     */
    public JPopupMenuOperator getPopup() {
        //workarround for Issue 79519
        if(System.getProperty("os.name").toLowerCase().indexOf("windows")==-1)
        {
            clickOnCenter();
            try{Thread.sleep(100);}catch(Exception ex){}
        }
        clickOnCenter(1, InputEvent.BUTTON3_MASK, 0);
        JPopupMenuOperator ret=new JPopupMenuOperator(JPopupMenuOperator.waitJPopupMenu((java.awt.Container)(MainWindowOperator.getDefault().getSource()),new JPopupByPointChooser(getCenterPoint(),diagramOperator.getDrawingArea().getSource(),0)));
        return ret;
    }
    
    
    /**
     * Select label by click on center
     */
    public void select() {
        clickOnCenter();
        new Timeout("",50).sleep();
    }
    
    public void addToSelection() {
        new Timeout("",10).sleep();
        clickOnCenter(1, InputEvent.BUTTON1_MASK, KeyEvent.CTRL_MASK);
        new Timeout("",50).sleep();
    }
    
    /**
     *
     * @param clickCount
     * @param mouseButton
     * @param modifiers
     */
    public void clickOnCenter(int clickCount, int mouseButton, int modifiers) {
        clickOn(getCenterPoint(), clickCount, mouseButton, modifiers);
    }
    
    
    /**
     *
     * @param clickCount
     * @param mouseButton
     */
    public void clickOnCenter(int clickCount, int mouseButton) {
        clickOn(getCenterPoint(), clickCount, mouseButton, 0);
    }
    public void clickOnCenter() {
        clickOn(getCenterPoint(), 1, InputEvent.BUTTON1_MASK, 0);
    }
    
    
    /**
     *
     * @param p
     * @param clickCount
     * @param mouseButton
     * @param modifiers
     */
    public void clickOn(Point p, int clickCount, int mouseButton, int modifiers) {
        p = makeVisible(p);
        diagramOperator.getDrawingArea().clickMouse(p.x,p.y, clickCount, mouseButton, modifiers);
    }
    
    public void clickForPopup() {
        clickOn(getCenterPoint(), 1, InputEvent.BUTTON3_MASK, 0);
    }
    
    private void dummy() {
        
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
        //wait for Trey's center apis
//6.0        ADGraphWindow area = diagramOperator.getDrawingArea().getArea();
//        area.getDrawingArea().centerPresentationElement(sourceLabel.getPresentationElement(), selectIt, deselectOthers);
    }
    
    /**
     * @return bounding rect for label
     */
    public Rectangle getBoundingRect()
    {
        return (new UMLWidgetOperator(sourceLabel)).getRectangle();
         
    }
    
    /**
     * Returns center point of this compartment
     * @return center point of this compartment
     */
    public Point getCenterPoint() {
         return (new UMLWidgetOperator(sourceLabel)).getCenterPoint();
    }
    
   /**
     * 
     * @return true if it's name label
     */
    public boolean isLinkName()
    {
     //TODO: getLabelKind()  
// 6.0       int kind=sourceLabel.getLabelKind();
//        //
//        boolean isName=kind==TSLabelKind.TSLK_ACTIVITYEDGE_NAME;
//        isName=isName || kind==TSLabelKind.TSLK_ASSOCIATION_NAME;
// 6.0       isName=isName || kind==TSLabelKind.TSLK_NAME;
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
//        return isName;
        return false;
    }
    
    /**
     * gets point inside a component and, if component is not visible centers window in this component.
     * The updated device point is returned
     * @param point 
     * @return 
     */
    public Point makeVisible(Point point){
        //TODO: Wait for Trey's api
//6.0        ADGraphWindow area = diagramOperator.getDrawingArea().getArea();
//        IDrawingAreaControl daControl = area.getDrawingArea();
//        
//        IETPoint etPoint = daControl.deviceToLogicalPoint(point.x,point.y);
//        
//        IETRect eDeviceAreaRect = new ETRect(area.getVisibleRect());
//        IETRect eVisibleAreaRect = daControl.deviceToLogicalRect(eDeviceAreaRect);
//        IETRect eElementRect = sourceLabel.getEngine().getLogicalBoundingRect(true);
//        
//        if (!eVisibleAreaRect.contains(eElementRect)){
//            center();
//            new Timeout("", 500);
//        }
//6.0        return daControl.logicalToDevicePoint(etPoint).asPoint();
   return point;
    }
        
    /**
     * Change element position with usage of mouse robot driver
     * @param x 
     * @param y 
     */
    public void moveTo(int x,int y)
    {
        Point parB=getCenterPoint();
        DrawingAreaOperator drA=diagramOperator.getDrawingArea();
        MouseRobotDriver driver = new MouseRobotDriver(new Timeout("",250));
        driver.moveMouse(drA, parB.x, parB.y);
        driver.pressMouse(drA, parB.x, parB.y, InputEvent.BUTTON1_MASK, 0);                
        new Timeout("",500).sleep();
        driver.moveMouse(drA, x, y);
        new Timeout("",500).sleep();
        driver.releaseMouse(drA, x, y, InputEvent.BUTTON1_MASK, 0);
        new Timeout("",500).sleep();
    }
    /**
     * 
     * @param x 
     * @param y 
     */
    public void moveTo(double x,double y)
    {
        moveTo((int)x,(int)y);
    }
    
    /**
     * Change element position with usage of mouse robot driver
     * @param dx 
     * @param dy 
     */
    public void shift(int dx,int dy)
    {
        Point parB=getCenterPoint();
        DrawingAreaOperator drA=diagramOperator.getDrawingArea();
        MouseRobotDriver driver = new MouseRobotDriver(new Timeout("",250));
        driver.moveMouse(drA, parB.x, parB.y);
        driver.pressMouse(drA, parB.x, parB.y, InputEvent.BUTTON1_MASK, 0);                
        new Timeout("",500).sleep();
        driver.moveMouse(drA, parB.x+dx, parB.y+dy);
        new Timeout("",500).sleep();
        driver.releaseMouse(drA, parB.x+dx, parB.y+dy, InputEvent.BUTTON1_MASK, 0);
        new Timeout("",500).sleep();
    }
    /**
     * 
     * @param dx 
     * @param dy 
     */
    public void shift(double dx,double dy)
    {
        shift((int)dx,(int)dy);
    }
    
    
    /**
     * Select compartmnet by name
     */
    public static class LabelByTextChooser implements LabelChooser {
        
        private String text = null;
        
        /**
         * Select compartment by type
         * @param text 
         */
        public LabelByTextChooser(String text){
            this.text = text;
        }
        
        /**
         * Check param by it's type
         * @return true if suitable and false otherwise
         * @param label 
         */
        public boolean checkLabel(Widget label) {
            return text.equals(((UMLLabelWidget)label).getLabel());
           
        }
        
        /**
         * Returns description of this chooser
         * @return description of this chooser
         */
        public String getDescription( ) {
            return "Chooser that accepts label with specific text";
        }  
    }
    
    
    /**
     * Any compartment chooser. Used for looking by index.
     */
    public static class AnyLabelChooser implements LabelChooser {
        
        /**
         * Any Compartment chooser. Selects any compartments
         */
        public AnyLabelChooser(){
        }
        
        /**
         * Check label
         * @param lab label to check
         * @return Always true
         */
        public boolean checkLabel(Widget lab) {
            return true;
        }
        
        /**
         * Returns short description
         * @return Description
         */
        public String getDescription( ) {
            return "Chooser that accepts any label ";
        }        
    }  
}
