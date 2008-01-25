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
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.modules.uml.core.support.umlsupport.ETDeviceRect;
import org.netbeans.modules.uml.core.support.umlsupport.ETRect;
import org.netbeans.modules.uml.core.support.umlsupport.IETRect;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.products.ad.drawengines.ETNodeDrawEngine;
import org.netbeans.modules.uml.ui.products.ad.drawengines.INodeDrawEngine;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ETRectEx;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.ICompartment;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IListCompartment;
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
    protected ICompartment sourceCompartment = null;
    
    /**
     * 
     * @return 
     */
    public ICompartment getSource(){
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
    /**
     * Construct compartment by compartment class
     * @param el Instance of DiagramElementOperator
     * @param co Instance of ICompartment
     */
    public CompartmentOperator(DiagramElementOperator el, ICompartment co){
        sourceElement = el;
        sourceCompartment = co;
    }
    
    
    /**
     *method return name of comparment from compartment source
     *@return compartmant name
     */
    public String getName(){
        return sourceCompartment.getName();
    }
    
    
    /**
     * Wait for suitable compartment
     * @param source DiagramElementOperator to look in
     * @param chooser Custom chooser
     * @param index index
     * @throws qa.uml.exceptions.NotFoundException When now suitable compartment found
     * @return CompartmentOperator if found
     */
    public static ICompartment waitForCompartment(final DiagramElementOperator source, final CompartmentChooser chooser, final int index) throws NotFoundException {
        try{
            Waiter w = new Waiter(new Waitable() {
                public Object actionProduced(Object obj) {
                    return findCompartment(source, chooser, index);
                }
                public String getDescription() {
                    return("Wait for " + chooser.getDescription());
                }
            });
            
            Timeouts t = JemmyProperties.getCurrentTimeouts();
            t.setTimeout("Waiter.WaitingTime", t.getTimeout("CompartmentOperator.WaitCompartmentTime"));
            
            ICompartment c = (ICompartment)w.waitAction(null);
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
    public static ICompartment findCompartment(DiagramElementOperator source, CompartmentChooser chooser, int index) {
        if (( source == null ) || (chooser == null)) {
            return null;
        }
        
        ETList<ICompartment> compartments = source.getGraphObject().getEngine().getCompartments();
        if(compartments == null){
            return null;
        }
        
        Iterator<ICompartment> it = compartments.iterator();
        ArrayList<ICompartment> compartmentsFound = new ArrayList<ICompartment>();
        while(it.hasNext()) {
            ICompartment co = it.next();
            if (chooser.checkCompartment(co)){
                compartmentsFound.add(co);
            }
            compartmentsFound.addAll(getCompartments(chooser, co));
        }
        
        
        if(compartmentsFound.size()>index) {
            return compartmentsFound.toArray(new ICompartment[1])[index];
        }
        return null; //Nothing suitable found
        
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
        ArrayList<ICompartment> iComps = CompartmentOperator.getCompartments(chooser, sourceCompartment);
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
    private static ArrayList<ICompartment> getCompartments(CompartmentChooser chooser, ICompartment parentCompartment){
        ArrayList<ICompartment> compartmentsFound = new ArrayList<ICompartment>();
        if (parentCompartment instanceof IListCompartment){
            ETList<ICompartment> compartments = ((IListCompartment)parentCompartment).getCompartments();
            if(compartments == null){
                return compartmentsFound;
            }
            Iterator<ICompartment> it = compartments.iterator();
            while(it.hasNext()) {
                ICompartment co = it.next();
                if (chooser.checkCompartment(co)){
                    compartmentsFound.add(co);
                }
                compartmentsFound.addAll(getCompartments(chooser, co));
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
        if(System.getProperty("os.name").toLowerCase().indexOf("windows")==-1)
        {
            clickOnCenter();
            try{Thread.sleep(100);}catch(Exception ex){}
        }
        clickForPopup();
        
        JPopupMenuOperator ret=new JPopupMenuOperator(JPopupMenuOperator.waitJPopupMenu((java.awt.Container)(MainWindowOperator.getDefault().getSource()),new JPopupByPointChooser(getCenterPoint(),sourceElement.getDiagram().getDrawingArea().getSource(),0)));
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
        //return sourceElement.getDiagram().getDrawingAreaControl().logicalToDeviceRect(sourceCompartment.getEngine().getLogicalBoundingRect(true)).getRectangle();
        
        IETRect tmpRect=sourceCompartment.getBoundingRect();
        ETDeviceRect tmpDevRect=null;
        int x,y;
        //transform to device coordinates
        if (tmpRect instanceof ETRect || tmpRect instanceof ETRectEx) {
            // This special case is for all the code that depends
            // on the bounding rectangle in device coordinates.
            tmpDevRect=((ETRect) tmpRect).getAsDeviceRect();
        } else if (tmpRect instanceof ETDeviceRect) {
            tmpDevRect=((ETDeviceRect) tmpRect);
        }
        return tmpDevRect.getBounds();        
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
        return sourceCompartment.getCompartmentFont(sourceElement.getDiagram().getDrawingAreaControl().getCurrentZoom());
    }
    
    
    /**
     * Returns Color of text in this compartment
     * @return Color of text in this compartment
     */
    public Color getFontColor(){        
        return sourceCompartment.getCompartmentFontColor();
    }
    
    
    /**
     * 
     * @return 
     */
    public Color getBorderColor(){
        try{
            INodeDrawEngine engine =  (INodeDrawEngine)sourceCompartment.getEngine();
            return engine.getBorderColor();
        }catch(Exception e){
            return null;
        }
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
        try{
            INodeDrawEngine engine =  (INodeDrawEngine)sourceCompartment.getEngine();
            return engine.getFillColor();
        }catch(Exception e){
            return null;
        }
    }
    
    /**
     * Returns Fill color using method getFillColor provided by ETNodeDrawEngine.
     *
     * @return Color background fill color
     */
    public Color getFillColor(){
        try{
            ETNodeDrawEngine engine =  (ETNodeDrawEngine)sourceCompartment.getEngine();
            return engine.getFillColor();
        }catch(Exception e){
            return null;
        }
    }
    
    /**
     * Returns Fill color using method getLightGradientFillColor provided by ETNodeDrawEngine
     *
     * @return Color light gradient background fill color
     */
    public Color getLightGradientFillColor(){
        try{
            ETNodeDrawEngine engine =  (ETNodeDrawEngine)sourceCompartment.getEngine();
            return engine.getLightGradientFillColor();
        }catch(Exception e){
            return null;
        }
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
                this.clazz = Class.forName(compType.toString());
            }catch(Exception e){
                clazz = null;
            }
        }
        
        /**
         * Check param by it's type
         * @param co Compartment to check
         * @return true if suitable and false otherwise
         */
        public boolean checkCompartment(ICompartment co) {
            return (clazz!=null && clazz.isInstance(co));
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
        
        /**
         * Check param by it's name
         * @param co Compartment to check
         * @return true if suitable and false otherwise
         */
        public boolean checkCompartment(ICompartment co) {
            return name.equals(co.getName());
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
        
        /**
         * Check compartment
         * @param co Compartment to check
         * @return Always true
         */
        public boolean checkCompartment(ICompartment co) {
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
