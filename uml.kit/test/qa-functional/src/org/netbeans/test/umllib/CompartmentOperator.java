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
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.uml.diagrams.nodes.ElementListWidget;
import org.netbeans.modules.uml.diagrams.nodes.FeatureWidget;
import org.netbeans.test.umllib.actions.Actionable;
import org.netbeans.test.umllib.exceptions.NotFoundException;
import org.netbeans.test.umllib.util.JPopupByPointChooser;

/**
 * This is operator for "compartment". Compartment in
 * ETI terms means subsection of some diagram element. For example:
 * <B>Attributes compartment</B>
 * <B>Operations compartment</B>
 * <B>Name compartment</B>
 */
public class CompartmentOperator implements Actionable {
    
    /**
     * Default delay for Compartment Waiter. Real value can be changed
     * as for the all other operators(For example via JemmyProperties)
     */
    public static final long WAIT_COMPARTMENT_TIMEOUT =  5000;
    
    static {
        Timeouts.initDefault("CompartmentOperator.WaitCompartmentTime", WAIT_COMPARTMENT_TIMEOUT);
    }
    
    protected DiagramElementOperator sourceElement = null;
    protected Widget sourceCompartment = null;
    protected DrawingAreaOperator drawingArea = null;
    
    /**
     * 
     * @return 
     */
    public Widget getSource() {
        return sourceCompartment;
    }
    
    /**
     * 
     * @return 
     */
    public DiagramElementOperator getElement()
    {
        return sourceElement;
    }
    
    /**
     * Construct compartment by index
     * @param el Instance of DiagramElementOperator to look for compartment
     * @param index index
     * @throws qa.uml.exceptions.NotFoundException when no suitable compartment found
     */
    public CompartmentOperator(DiagramElementOperator el, int index) throws NotFoundException{
        this(el, waitForCompartment(el, new AnyCompartmentChooser(),index));
    }
    
    /**
     * Costruct compartment by type
     * @param el Instance of DiagramElementOperator to look for compartment
     * @param type Compartemnt type.
     * @throws qa.uml.exceptions.NotFoundException when no suitable compartment found
     * @see qa.uml.CompartmentTypes
     */
    public CompartmentOperator(DiagramElementOperator el, CompartmentTypes type) throws NotFoundException{
        this(el, waitForCompartment(el, new CompartmentByTypeChooser(type), 0));
    }
    
      public CompartmentOperator(DiagramElementOperator el, CompartmentTypes type, String typeInfo) throws NotFoundException{
        
        this(el, waitForCompartment(el, new CompartmentByTypeChooser(type), 0, typeInfo));
    }
    /**
     * Construct compartment by compartment class
     * @param el Instance of DiagramElementOperator
     * @param co Instance of Widget
     */
    public CompartmentOperator(DiagramElementOperator el, Widget co){
        sourceElement = el;
        sourceCompartment = co; 
        drawingArea = DiagramOperator.getDrawingArea();
    }
    
     
    
    /**
     * method return name of comparment from compartment source
     * @return compartmant name
     */
    public String getName(){
       return ((FeatureWidget)sourceCompartment).getText();
       
     
    }
    
    public static Widget waitForCompartment(final DiagramElementOperator source, final CompartmentChooser chooser, final int index) throws NotFoundException {
         return waitForCompartment(source, chooser, index, "");
    }
    /**
     * Wait for suitable compartment
     * @param source DiagramElementOperator to look in
     * @param chooser Custom chooser
     * @param index index
     * @throws qa.uml.exceptions.NotFoundException When now suitable compartment found
     * @return CompartmentOperator if found
     */
      
     public static Widget waitForCompartment(final DiagramElementOperator source, final CompartmentChooser chooser, final int index, final String type) throws NotFoundException { 
         try{
            Waiter w = new Waiter(new Waitable() {
                public Object actionProduced(Object obj) {
                   return findCompartment(source, chooser, index, type);
                }
                public String getDescription() {
                    return("Wait for " + chooser.getDescription());
                }
            });
            
            Timeouts t = JemmyProperties.getCurrentTimeouts();
            t.setTimeout("Waiter.WaitingTime", t.getTimeout("CompartmentOperator.WaitCompartmentTime"));
            
            Widget c = (Widget)w.waitAction(null);
            if(c!=null) {
                return c;
            }
            
            throw new NotFoundException("Matching compartment was not found.");
        }catch(InterruptedException ie) {
            throw new NotFoundException("Compartment was not found due to runtime error.");
        }
    }
    
    
    /**
     * Search for suitable compartment
     * @param source DiagramElementOperator to look in
     * @param chooser Custom chooser
     * @param index index
     * @return CompartmentOperator if found
     */
        
    public static Widget findCompartment(DiagramElementOperator source, CompartmentChooser chooser, int index, String type) {
        if (( source == null ) || (chooser == null)) {
            return null;
        }
        
        ArrayList<Widget> compartmentsFound = new ArrayList<Widget>();
        
        List<Widget> children = source.getGraphObject().getChildren();
        Utils.log("findCompartment(): children = "+ children);
        if (children != null & children.size() > 0) {
            for (Widget child : children) {
                if (chooser.checkCompartment(child, type)) {
                    compartmentsFound.add(child);
                }
                //Find subcompartment
                compartmentsFound.addAll(getCompartments(chooser, child, type));
            }

            if (compartmentsFound.size() > index) {
                Utils.log( "debug: return widget =" +compartmentsFound.toArray(new Widget[1])[index]);
                return compartmentsFound.toArray(new Widget[1])[index];
            }
            return null; //Nothing suitable found

        }
         return null;
    }
    
    /**
     * Return list of all sub compartmens
     * @return list of sub compartmens
     */
    public ArrayList<CompartmentOperator> getCompartments(){
        return getCompartments(new AnyCompartmentChooser());
    }
    
    
     
    /**
     * Returns all sub compartments accepted by the specified chooser
     * @param chooser to be used for compartment selection
     * @return list of sub compartments
     */
    public ArrayList<CompartmentOperator> getCompartments(CompartmentChooser chooser){
       return  getCompartments(chooser, "");
    }
    /**
     * Returns all sub compartments accepted by the specified chooser
     * @param chooser to be used for compartment selection
     * @return list of sub compartments
     */
    public ArrayList<CompartmentOperator> getCompartments(CompartmentChooser chooser, String type){      
        ArrayList<Widget> iComps = CompartmentOperator.getCompartments(chooser, sourceCompartment, type);
        ArrayList<CompartmentOperator> comps = new ArrayList<CompartmentOperator>();
        for (int i=0; i<iComps.size(); i++){
            comps.add(new CompartmentOperator(sourceElement, iComps.get(i)));
        }
        return comps;
    }
    
    
    /**
     * Returns list of sub compartmens
     * @return list of sub compartmens
     * @param chooser Custom chooser
     * @param parentCompartment Parent Compartment
     */
     private static ArrayList<Widget> getCompartments(CompartmentChooser chooser, Widget parentCompartment){
          return getCompartments(chooser, parentCompartment, "");
     }
    
    
    
    /**
     * Returns list of sub compartmens
     * @return list of sub compartmens
     * @param chooser Custom chooser
     * @param parentCompartment Parent Compartment
     */
     private static ArrayList<Widget> getCompartments(CompartmentChooser chooser, Widget parentCompartment, String type){  
        ArrayList<Widget> compartmentsFound = new ArrayList<Widget>();
        if (parentCompartment instanceof Widget){
            List<Widget> compartments = ((Widget)parentCompartment).getChildren();
            if(compartments == null){
                return compartmentsFound;
            }
            for (Widget child : compartments) { 
                if (chooser.checkCompartment(child, type)){
                    compartmentsFound.add(child);
                }
                compartmentsFound.addAll(getCompartments(chooser, child, type));
            }
            return compartmentsFound;
        } else {
            return compartmentsFound;
        }
         
     }   
        
    
    //Methods from Actionable interface
    /**
     * Invokes popup menu and returns JPopupMenuOperator
     * @return JPopupMenuOperator
     */
    public JPopupMenuOperator getPopup() {
        //workarround for Issue 79519
//        System.out.println("os.name="+ System.getProperty("os.name"));
//        if(System.getProperty("os.name").toLowerCase().indexOf("windows")==-1)
//        {
//            clickOnCenter();
//            try{Thread.sleep(100);}catch(Exception ex){}
//        }
        clickOnCenter();
        clickForPopup();
        
        JPopupMenuOperator ret=new JPopupMenuOperator(JPopupMenuOperator.waitJPopupMenu((java.awt.Container)(MainWindowOperator.getDefault().getSource()),new JPopupByPointChooser(getCenterPoint(),drawingArea.getSource(),0)));
        return ret;
    }
    
    /**
     * Click on this compartment
     */
    public void select() {
    }
    
    /**
     * Add this compartment to selection by clicking on it with
     * CTRL pressed
     */
    public void addToSelection() {
    }
    
    
    
    
    /**
     * Returns center point of this compartment
     * @return center point of this compartment
     */
    public Point getCenterPoint() {         
        Point centerPoint = new Point((int)getRectangle().getCenterX(),(int)getRectangle().getCenterY());
        return centerPoint;
    }
    
    /**
     * Returns left horizontal, center vertical point of this compartment
     * @param shift - shift from left border
     * @return left horizontal, center vertical point of this compartment
     */
    public Point getLeftCenterPoint(int shift) {
        Rectangle rect = getRectangle();
        return new Point(rect.x+shift, (int)rect.getCenterY());
    }
    
    
    /**
     * Returns left horizontal, center vertical point of this compartment
     * with default shift from left border
     * @return left horizontal, center vertical point of this compartment
     */
    public Point getLeftCenterPoint() {
        return getLeftCenterPoint(10);
    }
    
    /**
     * 
     * @return bounding rectangle for compartment
     */
    public Rectangle getRectangle() {
         Rectangle localRect = sourceCompartment.getBounds();
         Point scenePoint = sourceCompartment.convertLocalToScene(new Point(localRect.x, localRect.y));
         Rectangle sceneRect = new Rectangle(scenePoint.x, scenePoint.y, localRect.width, localRect.height);
         return sceneRect;        
    }
    
    
    
    /**
     * Click on this compartment
     * @param clickCount count
     * @param mouseButton mouse button. Use constants from InputEvent
     * @param modifiers Key modifiers. Use contants from KeyEvent
     */
    public void clickOnCenter(int clickCount, int mouseButton, int modifiers) {
        sourceElement.clickOn(getCenterPoint(), clickCount, mouseButton, modifiers);
    }
    
    
    /**
     * Click on this compartment
     * @param clickCount click count: 1,2 ..
     * @param mouseButton Mouse button to click. Use constants from InputEvent
     */
    public void clickOnCenter(int clickCount, int mouseButton) {
        clickOnCenter(clickCount,mouseButton,0);
    }
    
    /**
     * Click on this compartmnet by left mouse button
     */
    public void clickOnCenter() {
        clickOnCenter(1,InputEvent.BUTTON1_MASK);
    }
    
    /**
     * Click on this compartment by right mouse button
     */
    public void clickForPopup() {
        clickOnCenter(1,InputEvent.BUTTON3_MASK);
    }
    /**
     * Click on this compartment
     * @param clickCount count
     * @param mouseButton mouse button. Use constants from InputEvent
     * @param modifiers Key modifiers. Use contants from KeyEvent
     */
    public void clickOnLeftCenter(int clickCount, int mouseButton, int modifiers) {
        sourceElement.clickOn(getLeftCenterPoint(), clickCount, mouseButton, modifiers);
    }
    
    /**
     * Click on this compartment
     * @param clickCount click count: 1,2 ..
     * @param mouseButton Mouse button to click. Use constants from InputEvent
     */
    public void clickOnLeftCenter(int clickCount, int mouseButton) {
        clickOnLeftCenter(clickCount,mouseButton,0);
    }
    
    /**
     * Click on this compartmnet by left mouse button
     */
    public void clickOnLeftCenter() {
        clickOnLeftCenter(1,InputEvent.BUTTON1_MASK);
    }
    
    /**
     * Click on this compartment by right mouse button
     */
    public void clickleftForPopup() {
        clickOnLeftCenter(1,InputEvent.BUTTON3_MASK);
    }
    
    
    /**
     * Returns Font of text in this compartment
     * @return Font of text in this compartment
     */
    public Font getFont(){
        //TODO: 
        //6.0 return sourceCompartment.getCompartmentFont(sourceElement.getDiagram().getDrawingAreaControl().getCurrentZoom());
        return sourceCompartment.getFont();
        // return null;
    }
    
    
    /**
     * Returns Color of text in this compartment
     * @return Color of text in this compartment
     */
    public Color getFontColor(){        
        return null;
    }
    
    
    /**
     * 
     * @return 
     */
    public Color getBorderColor(){
        //TODO: Wait for Trey's API
//6.0        try{
//            INodeDrawEngine engine =  (INodeDrawEngine)sourceCompartment.getEngine();
//            return engine.getBorderColor();
//        }catch(Exception e){
//            return null;
//6.0        }
        return null;
    }
    

    /**
     * Returns background color using standard method getFillColor provided by interface IDrawEngine
     * <br> Problem is that this method returns FillColor variable for all elements.
     * But 'backgroung color' changed by user is saved to different variables for different elements
     * <br> look also at getFillColor() and getLightGradientFillColor()
     *
     * @return Color background color
     */
    public Color getBackgroundColor(){
        //TODO: Wait for Trey's API
//6.0        try{
//            INodeDrawEngine engine =  (INodeDrawEngine)sourceCompartment.getEngine();
//            return engine.getFillColor();
//        }catch(Exception e){
//            return null;
//6.0        }
        return null;
    }
    
    /**
     * Returns Fill color using method getFillColor provided by ETNodeDrawEngine.
     *
     * @return Color background fill color
     */
    public Color getFillColor(){
         //TODO: Wait for Trey's API
//        try{
//            ETNodeDrawEngine engine =  (ETNodeDrawEngine)sourceCompartment.getEngine();
//            return engine.getFillColor();
//        }catch(Exception e){
//            return null;
//6.0        }
        return null;
    }
    
    /**
     * Returns Fill color using method getLightGradientFillColor provided by ETNodeDrawEngine
     *
     * @return Color light gradient background fill color
     */
    public Color getLightGradientFillColor(){
        //TODO: Wait for Trey's API
//6.0        try{
//            ETNodeDrawEngine engine =  (ETNodeDrawEngine)sourceCompartment.getEngine();
//            return engine.getLightGradientFillColor();
//        }catch(Exception e){
//            return null;
//6.0        }
        return null;
    }
    
    
    
    /**
     * Select compartmnet by type
     */
    public static class CompartmentByTypeChooser implements CompartmentChooser {
        
        private Class clazz = null;
         
        
        /**
         * Select compartment by type
         * @param compType compartment type
         */
        public CompartmentByTypeChooser(CompartmentTypes compType){      
            try{
               Utils.log("Looking for compartment " + compType.toString());
               this.clazz = Class.forName(compType.toString());  
            }catch(Exception e){
                clazz = null;
            }
        }
        
        
        public boolean checkCompartment(Widget co) {
            return this.checkCompartment(co, "");
        }
          
         
        /**
         * Check param by it's type
         * @param co Compartment to check
         * @return true if suitable and false otherwise
         */

         public boolean checkCompartment(Widget co, String typeInfo) {
            Utils.log("CompartmentByTypeChooser:checkCompartment() typeInfo=" + typeInfo);
//            if (clazz != null && clazz.isInstance(co) && typeInfo != null) {
//                return ((ElementListWidget) co).getLabel().equals(typeInfo);
//            } else if (typeInfo == null) {
//                Utils.log("CompartmentByTypeChooser:checkCompartment() return"+ (clazz != null && clazz.isInstance(co)));
//                Utils.log("clazz="+ clazz.toString());
//                Utils.log("clazz.isInstance("+co.toString()+") = " + clazz.isInstance(co));
//                return (clazz != null && clazz.isInstance(co)) ;
//            }
//            Utils.log("CompartmentByTypeChooser:checkCompartment() return false at end");
//            return false;
            if (clazz != null && clazz.isInstance(co)) {
                Utils.log("CompartmentOperator:CompartmentByTypeChooser:checkCompartment():clazz != null && clazz.isInstance(co)=true ");
                if (typeInfo.equals("Attributes") || typeInfo.equals("Operations")) {
                    // It is ElementListWidget compartment
                    // TODO implement real behaviour
                    return false;
//                     return ((ElementListWidget) co).getLabel().equals(typeInfo);
                }    
            }
            return (clazz != null && clazz.isInstance(co)) ; 
             
         }
        
        
        /**
         * Returns description of this chooser
         * @return description of this chooser
         */
        public String getDescription( ) {
            return "Chooser that accepts compartments of specific type ";
        }
        
    }
    
    
    
    /**
     * Select compartmnet by name
     */
    public static class CompartmentByNameChooser implements CompartmentChooser {
        
        private String name = null;
        
        /**
         * Select compartment by name
         * @param compname 
         */
        public CompartmentByNameChooser(String compname){
            this.name = compname;
        }
        
         public boolean checkCompartment(Widget co) {
            return this.checkCompartment(co, "");
        }
        
        /**
         * Check param by it's name
         * @param co Compartment to check
         * @return true if suitable and false otherwise
         */
       //6.0 public boolean checkCompartment(Widget co) {
        public boolean checkCompartment(Widget co, String name) {
            if (!name.equals("") )
               return name.equals(co.getClass().getName());
            else
               return true;
        }
        
        /**
         * Returns description of this chooser
         * @return description of this chooser
         */
        public String getDescription( ) {
            return "Chooser that accepts compartments with specific name ";
        }
        
        
    }
   
    
    /**
     * Any compartment chooser. Used for looking by index.
     */
    public static class AnyCompartmentChooser implements CompartmentChooser {
        
        /**
         * Any Compartment chooser. Selects any compartments
         */
        public AnyCompartmentChooser(){
        }
        
        public boolean checkCompartment(Widget co) {
            return  checkCompartment(co, "");
        }
        /**
         * Check compartment
         * @param co Compartment to check
         * @return Always true
         */
 
         public boolean checkCompartment(Widget co, String name) {
            return true;
        }
        
        /**
         * Returns short description
         * @return Description
         */
        public String getDescription( ) {
            return "Chooser that accepts any compartment ";
        }  
    }    
}
