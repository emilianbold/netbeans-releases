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


/*
 * UMLDiagramOperator.java
 *
 * Created on 4 Feb 2005, 17:18
 */

package org.netbeans.test.umllib;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JComponent;
import org.netbeans.core.windows.view.ui.tabcontrol.TabbedAdapter;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Timeout;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.drivers.MouseDriver;
import org.netbeans.jemmy.drivers.input.MouseRobotDriver;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.modules.uml.drawingarea.DiagramTopComponent;
import org.netbeans.modules.uml.ui.swing.drawingarea.ADDrawingAreaControl;
import org.netbeans.test.umllib.customelements.LifelineOperator;
import org.netbeans.test.umllib.exceptions.NotFoundException;
import org.netbeans.test.umllib.exceptions.NotFoundOnDiagramException;
import org.netbeans.test.umllib.exceptions.UMLCommonException;
import org.netbeans.test.umllib.util.LibProperties;

import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPresentationElement;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.applicationmanager.NodePresentation;
import org.netbeans.modules.uml.ui.support.viewfactorysupport.IETGraphObject;
import org.netbeans.modules.uml.ui.swing.drawingarea.ADGraphWindow;
import org.netbeans.modules.uml.ui.swing.drawingarea.IDrawingAreaControl;
import org.openide.windows.TopComponent;


/**
 * This common diagram operator
 * @author Alexei Mokeev
 */
public class DiagramOperator extends TopComponentOperator {
    private Component tcoSource = null;
    private IDrawingAreaControl drawingAreaControl = null;
    private DrawingAreaOperator drawingArea = null;
    private String diagramName = null;
    private DiagramToolbarOperator mToolbar = null;
    private UMLPaletteOperator mPaletter = null;
    
    public static final long WAIT_DIAGRAM_TIMEOUT =  60000;
    
    static {
        Timeouts.initDefault("DiagramOperator.WaitDiagramOperator", WAIT_DIAGRAM_TIMEOUT);
    }
    
    /**
     * Creates a new instance of DiagramOperator
     * @param diagramName Diagram name
     */
    public DiagramOperator(String diagramName){
        super((JComponent)waitForDiagram(diagramName,getDefaultStringComparator()));
        this.diagramName = diagramName;
        tcoSource = getSource();
        drawingAreaControl = (IDrawingAreaControl)findSubComponent((ComponentChooser)new DrawingAreaControlChooser());
        Component areaComp = findSubComponent((ComponentChooser)new DrawingAreaChooser());
        drawingArea = new DrawingAreaOperator((ADGraphWindow)areaComp);
    }
    
    
    /**
     * Returns instance of IDrawingAreaControl for this diagram
     * @return instance of IDrawingAreaControl for this diagram
     */
    public IDrawingAreaControl getDrawingAreaControl() {
        return drawingAreaControl;
    }
    
    /**
     * Returns DrawingAreaOperator for this diagram
     * @return DrawingAreaOperator for this diagram
     */
    public DrawingAreaOperator getDrawingArea() {
        return drawingArea;
    }
    
    
    /**
     * Returns diagram name
     * @return diagram name
     */
    public String getDiagramName() {
        return diagramName;
    }
    
    
    /**
     * Return toolbar for this diagram
     * @return Toolbar operator for this diagram
     */
    public DiagramToolbarOperator toolbar() {
        if(mToolbar == null){
            mToolbar = new DiagramToolbarOperator(this);
        }
        return mToolbar;
    }
    /**
     * Return paletter for this diagram, activate palette
     * @return Paletter operator for this diagram
     */
    public UMLPaletteOperator paletter() {
        if(mPaletter == null || !mPaletter.isShowing()){
            mPaletter = new UMLPaletteOperator();
        }
        mPaletter.makeComponentVisible();
        mPaletter.waitComponentVisible(true);
        return mPaletter;
    }
    
    /**
     * Put element on diagram by pressing paletter butoon and clicking on
     * the diagram
     * @param name Name for new element
     * @param elementType Type of new element
     * @throws qa.uml.exceptions.NotFoundException when Button for the element is not found on paletter
     * @return Operator for created element
     */
    public DiagramElementOperator putElementOnDiagram(String name, ElementTypes elementType) throws NotFoundException{
        Point p = getDrawingArea().getFreePoint();
        return putElementOnDiagram(name, elementType, p.x, p.y);
    }
    /**
     * Put element on diagram by pressing paletter butoon and clicking on
     * the diagram
     * @param name Name for new element
     * @param elementType Expanded Type of new element
     * @throws qa.uml.exceptions.NotFoundException when Button for the element is not found on paletter
     * @return Operator for created element
     */
    public DiagramElementOperator putElementOnDiagram(String name, ExpandedElementTypes elementType) throws NotFoundException{
        Point p = getDrawingArea().getFreePoint();
        return putElementOnDiagram(name, elementType, p.x, p.y);
    }
    
    
    /**
     * create an element on diagram by pressing paletter butoon and clicking on
     * the diagram
     * @param name Name for new element
     * @param elementType Type of new element
     * @throws qa.uml.exceptions.NotFoundException
     */
    public void createGenericElementOnDiagram(String name, ElementTypes elementType) throws NotFoundException{
        Point p = getDrawingArea().getFreePoint();
        createGenericElementOnDiagram(name, elementType, p.x, p.y, LibProperties.getCurrentNamer(elementType));
    }
    
    
    /**
     * Put element on diagram by pressing paletter butoon and clicking on
     * the diagram
     * @param name Name for new element
     * @param elementType Type of new element
     * @param x X coordinate
     * @param y Y coordinate
     * @throws qa.uml.exceptions.NotFoundException when Button for the element is not found on paletter
     * @return Operator for created element
     */
    public DiagramElementOperator putElementOnDiagram(String name, ElementTypes elementType, int x, int y) throws NotFoundException{
        return putElementOnDiagram(name,elementType,x,y, LibProperties.getCurrentNamer(elementType));
    }
    /**
     * Put element on diagram by pressing paletter butoon and clicking on
     * the diagram
     * @param name Name for new element
     * @param elementType Expanded Type of new element
     * @param x X coordinate
     * @param y Y coordinate
     * @throws qa.uml.exceptions.NotFoundException when Button for the element is not found on paletter
     * @return Operator for created element
     */
    public DiagramElementOperator putElementOnDiagram(String name, ExpandedElementTypes elementType, int x, int y) throws NotFoundException{
        return putElementOnDiagram(name,elementType,x,y, LibProperties.getCurrentNamer(elementType));
    }
    
    
    
    /**
     * Put element on diagram by pressing paletter butoon and clicking on
     * the diagram
     * @param name Name for new element
     * @param elementType Type of new element
     * @param x X coordinate
     * @param y Y coordinate
     * @throws qa.uml.exceptions.NotFoundException when Button for the element is not found on paletter
     */
    public void createGenericElementOnDiagram(String name, ElementTypes elementType, int x, int y) throws NotFoundException{
        createGenericElementOnDiagram(name, elementType, x, y, LibProperties.getCurrentNamer(elementType));
    }
    /**
     * Put element on diagram by pressing paletter butoon and clicking on
     * the diagram
     * @param name Name for new element
     * @param elementType Expanded Type of new element
     * @param x X coordinate
     * @param y Y coordinate
     * @throws qa.uml.exceptions.NotFoundException when Button for the element is not found on paletter
     */
    public void createGenericElementOnDiagram(String name, ExpandedElementTypes elementType, int x, int y) throws NotFoundException{
        createGenericElementOnDiagram(name, elementType, x, y, LibProperties.getCurrentNamer(elementType));
    }
    
    
    /**
     * Put element on diagram by pressing paletter butoon and clicking on
     * the diagram
     * @param name Name for new element
     * @param elementType Type of new element
     * @param x X cooordinate of point where element will be created
     * @param y Y cooordinate of point where element will be created
     * @param namer Namer for this element. Certain elements
     * should be named in very special way
     * @throws qa.uml.exceptions.NotFoundException when Button for the element is not found on paletter
     * @return Operator for created element
     */
    public DiagramElementOperator putElementOnDiagram(String name, ElementTypes elementType, int x, int y, SetName namer) throws NotFoundException{
        createGenericElementOnDiagram(name, elementType, x, y, namer);
        new EventTool().waitNoEvent(500);
        try{Thread.sleep(100);}catch(Exception ex){}
        if (elementType.equals(ElementTypes.LIFELINE) || elementType.equals(ElementTypes.ACTOR_LIFELINE)){
            int semicolonPos = name.indexOf(':');
            String lineName = name.substring(0,semicolonPos);
            String classifierName = name.substring(semicolonPos+1);
            return new LifelineOperator(this, lineName, classifierName);
        } else  return new DiagramElementOperator(this, name, elementType, 0);
    }
    
    /**
     * Put element on diagram by pressing paletter butoon and clicking on
     * the diagram
     * @param name Name for new element
     * @param elementType Expanded Type of new element
     * @param x X cooordinate of point where element will be created
     * @param y Y cooordinate of point where element will be created
     * @param namer Namer for this element. Certain elements
     * should be named in very special way
     * @throws qa.uml.exceptions.NotFoundException when Button for the element is not found on paletter
     * @return Operator for created element
     */
    public DiagramElementOperator putElementOnDiagram(String name, ExpandedElementTypes elementType, int x, int y, SetName namer) throws NotFoundException{
        createGenericElementOnDiagram(name, elementType, x, y, namer);
        new EventTool().waitNoEvent(500);
        if (elementType.equals(ExpandedElementTypes.LIFELINE) || elementType.equals(ExpandedElementTypes.ACTOR_LIFELINE)){
            int semicolonPos = name.indexOf(':');
            String lineName = name.substring(0,semicolonPos);
            String classifierName = name.substring(semicolonPos+1);
            return new LifelineOperator(this, lineName, classifierName);
        } else return new DiagramElementOperator(this, name, elementType, 0);
    }
    
    
    
    /**
     * Put element on diagram by pressing paletter butoon and clicking on
     * the diagram
     * @param name Name for new element
     * @param elementType Type of new element
     * @param x X cooordinate of point where element will be created
     * @param y Y cooordinate of point where element will be created
     * @param namer Namer for this element. Certain elements
     * should be named in very special way
     * @throws qa.uml.exceptions.NotFoundException
     */
    public void createGenericElementOnDiagram(String name, ElementTypes elementType, int x, int y, SetName namer) throws NotFoundException{
        int old=getAllDiagramElements().size();
        paletter().selectToolByType(elementType);
        getDrawingArea().clickMouse(x,y,1);
        //check if any element was added
        try{Thread.sleep(100);}catch(Exception ex){}
        for(int i=0;i<50 && getAllDiagramElements().size()==old;i++)
        {
            try{Thread.sleep(100);}catch(Exception ex){}
        }
        if(old==getAllDiagramElements().size())throw new UMLCommonException("No new elements appear on diagram in 5 seconds (new "+elementType+" was expected)");
        //
        toolbar().selectDefault();
        try{Thread.sleep(100);}catch(Exception ex){}
        if (name!=null){
            namer.setName(getDrawingArea(), x, y, name);
        }
        try{Thread.sleep(100);}catch(Exception ex){}
    }
    
    /**
     * Put element on diagram by pressing paletter butoon and clicking on
     * the diagram
     * @param name Name for new element
     * @param elementType Expanded Type of new element
     * @param x X cooordinate of point where element will be created
     * @param y Y cooordinate of point where element will be created
     * @param namer Namer for this element. Certain elements
     * should be named in very special way
     * @throws qa.uml.exceptions.NotFoundException
     */
    public void createGenericElementOnDiagram(String name, ExpandedElementTypes elementType, int x, int y, SetName namer) throws NotFoundException{
        int old=getAllDiagramElements().size();
        paletter().selectToolByType(elementType);
        getDrawingArea().clickMouse(x,y,1);
        //check if any element was added
        try{Thread.sleep(100);}catch(Exception ex){}
        for(int i=0;i<50 && getAllDiagramElements().size()==old;i++)
        {
            try{Thread.sleep(100);}catch(Exception ex){}
        }
        if(old==getAllDiagramElements().size())throw new UMLCommonException("No new elements appear on diagram in 5 seconds(new "+elementType+" was expected)");
        //
        toolbar().selectDefault();
        if (name!=null){
            namer.setName(getDrawingArea(), x, y, name);
        }
    }
    
    
    /**
     *
     * @param node
     */
    public void createGenericElementOnDiagram(Node node){
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        Point startPoint = pto.tree().getPointToClick(node.getTreePath());
        Point endPoint = this.getDrawingArea().getFreePoint(200);
        //MouseEventDriver evDriver = new MouseEventDriver();
        MouseDriver driver = new MouseRobotDriver(new Timeout("autoDelay",250));
        //
        driver.moveMouse(pto.tree(), startPoint.x, startPoint.y);
        new Timeout("",500).sleep();
        driver.pressMouse(pto.tree(), startPoint.x, startPoint.y, InputEvent.BUTTON1_MASK, 0);
        new Timeout("",500).sleep();
        driver.moveMouse(this.getDrawingArea(), endPoint.x, endPoint.y);
        for(int x=startPoint.x;x<endPoint.x;x+=10)driver.moveMouse(this.getDrawingArea(), x, startPoint.y+(endPoint.y-startPoint.y)*(x-startPoint.x)/(endPoint.x-startPoint.x));
        driver.moveMouse(this.getDrawingArea(), endPoint.x, endPoint.y);
        new Timeout("",500).sleep();
        driver.releaseMouse(this.getDrawingArea(), endPoint.x, endPoint.y, InputEvent.BUTTON1_MASK, 0);
        new Timeout("",500).sleep();
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
        if (linkElementType.equals(LinkTypes.COMPOSITION)||linkElementType.equals(LinkTypes.NAVIGABLE_COMPOSITION)|| linkElementType.equals(LinkTypes.NAVIGABLE_AGGREGATION))
               linkElementType =  LinkTypes.AGGREGATION;
        else if (linkElementType.equals(LinkTypes.NAVIGABLE_ASSOCIATION))
               linkElementType = LinkTypes.ASSOCIATION;  
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
        paletter().selectToolByType(linkElementType);
        paletter().waitSelection(linkElementType,true);
        try{Thread.sleep(100);}catch(Exception ex){}
        fromElement.clickOnCenter();
        new Timeout("",500).sleep();
        try{Thread.sleep(100);}catch(Exception ex){}
        toElement.clickOnCenter();
        new Timeout("",500).sleep();
        try{Thread.sleep(100);}catch(Exception ex){}
        toolbar().selectDefault();
        //toElement.clickOnCenter(1,InputEvent.BUTTON3_MASK);
        //paletter().selectToolByType(linkElementType);
    }
    
    
    /**
     * Returns all element from this diagram exclude NodePresentation elements
     * @return all elements from this diagram exclude NodePresentation elements
     */
    public  ArrayList<DiagramElementOperator> getDiagramElements(){
        ArrayList<DiagramElementOperator> elements = new ArrayList<DiagramElementOperator>();
        
        //searching for elements matching elemenFinder criteria
        ETList<IETGraphObject> allGraphs = getDrawingAreaControl().getAllItems6();
        Iterator<IETGraphObject> tsIt = allGraphs.iterator();
        while(tsIt.hasNext()) {
            IETGraphObject graphObject = tsIt.next();
            IPresentationElement presElement = graphObject.getPresentationElement();
            if (presElement == null) {
                continue;
            }
            if(!(presElement instanceof NodePresentation)) { //We are looking only for nodes here
                continue;
            }
            
            
            elements.add(new DiagramElementOperator(this,graphObject));
            
        }
        return elements;
        
    }
    
    
    /**
     * Returns all element from this diagram
     * @return all elements from this diagram
     */
    public  ArrayList<DiagramElementOperator> getAllDiagramElements(){
        ArrayList<DiagramElementOperator> elements = new ArrayList<DiagramElementOperator>();
        
        //searching for elements matching elemenFinder criteria
        ETList<IETGraphObject> allGraphs = getDrawingAreaControl().getAllItems6();
        Iterator<IETGraphObject> tsIt = allGraphs.iterator();
        while(tsIt.hasNext()) {
            IETGraphObject graphObject = tsIt.next();
            IPresentationElement presElement = graphObject.getPresentationElement();
            if (presElement == null) {
                continue;
            }
            
            elements.add(new DiagramElementOperator(this,graphObject));
            
        }
        return elements;
        
    }
    
    /**
     * Returns if there any selected objects on diagram
     * @return if there any selected objects on diagram
     */
    public boolean getHasSelectedElements(){
        
        return  getDrawingAreaControl().getHasSelected(true);
    }
    
    /**
     * Returns if there any selected links on diagram
     * @return if there any selected links on diagram
     */
    public boolean getHasSelectedLinks(){
        
        return  getDrawingAreaControl().getHasSelectedEdges(true);
    }
    
    /**
     *
     * @param name
     * @return
     */
    public  DiagramElementOperator getDiagramElement(String name){
        
        
        for (DiagramElementOperator elem : getDiagramElements()){
            if( name.equals(elem.getSubjectVNs().get(0)) ){
                return elem;
            }
        }
        
        return null;
    }
    
    
    /**
     *
     * @param diagramName
     * @param diagramType
     * @param projectNode
     * @return
     */
    public static DiagramOperator createDiagram(String diagramName, DiagramTypes diagramType, Node projectNode ){
        
        projectNode.callPopup().pushMenuNoBlock("New|Diagram");
        
        NewDiagramWizardOperator wiz = new NewDiagramWizardOperator();
        wiz.setDiagramType(diagramType.toString());
        wiz.setDiagramName(diagramName);
        wiz.clickFinish();
        
        return new DiagramOperator(diagramName);
        
    }
    
    /**
     * zooms diagram in using 'Zoom In' context menu
     */
    public void zoomIn(){
        this.getDrawingArea().getPopup().pushMenu("Zoom In");
    }
    
    /**
     * zooms diagram in using 'Zoom Out' context menu
     */
    public void zoomOut(){
        this.getDrawingArea().getPopup().pushMenu("Zoom Out");
    }
    
    /**
     * Selects diagram zoom percentage using UI.
     * Invokes 'zoom...' diagram context menu and types specified percent text into opened 'zoom' dialog.
     * @param ZoomCustomLevel string value to select in Percent ComboBox
     */
    public void selectZoomCustom(ZoomCustomLevel percent){
        this.getDrawingArea().getPopup().pushMenuNoBlock("Zoom...");
        JDialogOperator zmdlg=new JDialogOperator("Zoom");
        JComboBoxOperator percentDlg=new JComboBoxOperator(zmdlg);
        percentDlg.selectItem(percent.getValue());
        percentDlg.waitItemSelected(percent.getValue());
        new JButtonOperator(zmdlg,"OK").push();
        zmdlg.waitClosed();
    }
       
    public enum ZoomCustomLevel{
        PERCENT_25("25"),
        PERCENT_50("50"),
        PERCENT_100("100"),
        PERCENT_200("200"),
        PERCENT_400("400");
        
        private String value = "";
        private ZoomCustomLevel(String value){
            this.value = value;
        }
        public String getValue(){
            return this.value;
        }
    }
    
    public void waitClosed()
    {
        long timeout=5000;
        if(getTimeouts().contains("DiagramOperator.WaitClosed"))timeout=getTimeouts().getTimeout("DiagramOperator.WaitClosed");
        for(long i=0;i<timeout;i+=100)
        {
            if(drawingArea.getSource().isVisible())
            {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
            else return;
        }
        throw new UMLCommonException("Diagram/area is still visible after "+timeout+" ms.");
    }
    
    /** Returns TabbedAdapter component from parents hierarchy.
     */
    TabbedAdapter findTabbedAdapter() {
        Component parent = getSource().getParent();
        while(parent != null) {
            if(parent instanceof TabbedAdapter) {
                return (TabbedAdapter)parent;
            } else {
                parent = parent.getParent();
            }
        }
        return null;
    }    

    /** call menu on its tab.
     */
    public JPopupMenuOperator callMenuOnTab() {
        if(isOpened()) {
            this.makeComponentVisible();
            TabbedAdapter ta = findTabbedAdapter();
            int index = ta.indexOf((TopComponent)getSource());

            Rectangle r = new Rectangle();
            ta.getTabRect(index, r);
            Point p = new Point (r.x + (r.width / 2), r.y + (r.height / 2));
            Component tabsComp = ta.getComponentAt(p);
            return new JPopupMenuOperator(JPopupMenuOperator.callPopup(tabsComp, p.x, p.y));
        } else {
            throw new UMLCommonException("Attempt to call tab menu on closed diagram.");
        }
    }
    
    /**
     * Wait for diagram
     * @return CompartmentOperator if found
     * @param comp
     * @param name diagramName
     */
    public static Component waitForDiagram(final String name,final Operator.StringComparator comp) {
        try{
            Waiter w = new Waiter(new Waitable() {
                public Object actionProduced(Object obj) {
                    return findDiagram(name,comp);
                }
                public String getDescription() {
                    return("Wait for a diagram");
                }
            });
            
            Timeouts t = JemmyProperties.getCurrentTimeouts();
            t.setTimeout("Waiter.WaitingTime", t.getTimeout("DiagramOperator.WaitDiagramOperator"));
            
            return (Component)w.waitAction(null);
        }catch(InterruptedException ie) {
            return null;
        }
    }
    
    /**
     *
     * @param name
     * @param comp
     * @return
     */
    public static Component findDiagram(String name,Operator.StringComparator comp){
        //EditorWindowOperator mw = EditorWindowOperator.getEditor();
        return  MainWindowOperator.getDefault().findSubComponent(new DiagramComponentChooser(name,comp));
    }
    
    static class  DiagramComponentChooser implements ComponentChooser {
        String nm=null;
        Operator.StringComparator cmp=null;
        /**
         *
         * @param name
         * @param comp
         */
        DiagramComponentChooser(String name,Operator.StringComparator comp) {
            nm=name;
            cmp=comp;
        }
        
        /**
         *
         * @param arg0
         * @return
         */
        public boolean checkComponent( java.awt.Component arg0 ) {
            if(arg0 instanceof DiagramTopComponent) {
                return cmp.equals(arg0.getName(),nm);
            }
            return false;
        }
        /**
         *
         * @return
         */
        public String getDescription( ) {
            return "Chooser for UML Diagram";
        }
    }
    
    static class DrawingAreaControlChooser implements ComponentChooser {
        
        DrawingAreaControlChooser() {
            
        }
        
        /*
         * (non-Javadoc)
         *
         * @see org.netbeans.jemmy.ComponentChooser#checkComponent(java.awt.Component)
         */
        /**
         *
         * @param arg0
         * @return
         */
        public boolean checkComponent( java.awt.Component arg0 ) {
            if(arg0 instanceof ADDrawingAreaControl) {
                return true;
            }
            return false;
        }
        
        /*
         * (non-Javadoc)
         *
         * @see org.netbeans.jemmy.ComponentChooser#getDescription()
         */
        /**
         *
         * @return
         */
        public String getDescription( ) {
            return "Chooser for UML DrawingArea control";
        }
        
        
    }
    
    class DrawingAreaChooser implements ComponentChooser {
        
        DrawingAreaChooser() {
            
        }
        
        /*
         * (non-Javadoc)
         *
         * @see org.netbeans.jemmy.ComponentChooser#checkComponent(java.awt.Component)
         */
        /**
         *
         * @param arg0
         * @return
         */
        public boolean checkComponent( java.awt.Component arg0 ) {
            if(arg0 instanceof ADGraphWindow) {
                return true;
            }
            return false;
        }
        
        /*
         * (non-Javadoc)
         *
         * @see org.netbeans.jemmy.ComponentChooser#getDescription()
         */
        /**
         *
         * @return
         */
        public String getDescription( ) {
            return "Chooser for UML diagram pane";
        }
        
        
    }
}
