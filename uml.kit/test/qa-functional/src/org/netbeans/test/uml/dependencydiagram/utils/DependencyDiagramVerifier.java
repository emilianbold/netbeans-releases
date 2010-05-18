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



package org.netbeans.test.uml.dependencydiagram.utils;

import java.awt.Point;
import java.awt.event.InputEvent;
import java.io.PrintStream;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import javax.swing.JMenuItem;
import javax.swing.MenuElement;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.LinkOperator;
import org.netbeans.test.umllib.LinkTypes;
import org.netbeans.test.umllib.customelements.ClassOperator;
import org.netbeans.test.umllib.exceptions.ElementVerificationException;
import org.netbeans.test.umllib.exceptions.NotFoundException;
import org.netbeans.test.umllib.exceptions.NotFoundOnDiagramException;
import org.netbeans.test.umllib.exceptions.UnexpectedMenuItemStatusException;
import org.netbeans.test.umllib.util.JPopupByPointChooser;
import org.netbeans.test.umllib.util.PopupConstants;
import org.netbeans.test.umllib.vrf.GenericVerifier;


public class DependencyDiagramVerifier extends GenericVerifier{

    public static int COUNT = 1;
    
    public static final String DEP_DIAGRAM_FOOTER = "Dependencies";
    
    private EventTool eventTool = new EventTool();
    
    public DependencyDiagramVerifier(DiagramOperator dia, PrintStream log){
        super(dia, log);
    }
    
    
    public DiagramElementOperator createElement(String name, ElementTypes elementType) throws NotFoundException{
        Point p = dia.getDrawingArea().getFreePoint(100);
        return createElement(name, elementType, p.x, p.y);
    }

    
    /**
     * testcase: Generated dependency diagram of class A w/ two operations: public X op1() and public void op2(Y y)
     * <br/>
     * RESULT: Classes A, X, and Y created 
     */
    public void testAssociatedDatatypes(){
        String elementNameClass = "ClassEl";
        String elementNameDatatype1 = "Dtt1";
        String elementNameDatatype2 = "Dtt2";

        ElementTypes elementTypeClass = ElementTypes.CLASS;
        ElementTypes elementTypeDatatype1 = ElementTypes.DATATYPE;
        ElementTypes elementTypeDatatype2 = ElementTypes.DATATYPE;
        // create elements on src diagram
        DiagramOperator srcDiagram  = new DiagramOperator(dia.getName());
        createElement(elementNameClass, elementTypeClass);
        ClassOperator elementClass = new ClassOperator(dia, elementNameClass);
        elementClass.insertOperation(null, elementNameDatatype1, "oper1", null, true);
        elementClass.insertOperation(null, null, "oper2", elementNameDatatype2+" param", true);

        // start dependency creation
        checkIfDependencyGenerationAvailable(elementClass);
        // create dependency diagram
        createDependencyOnDiagramElement(elementClass);
        
        DiagramOperator depDiagram  = new DiagramOperator(elementNameClass+DEP_DIAGRAM_FOOTER);
        if (depDiagram == null)
            throw new NotFoundException(elementNameClass+DEP_DIAGRAM_FOOTER+" dependency diagram wasn't found for "+elementNameClass);
        
        // find expected elements on diagram
        DiagramElementOperator depElementClass = new DiagramElementOperator(depDiagram, elementNameClass, elementTypeClass);
        DiagramElementOperator depElementDatatype1 = new DiagramElementOperator(depDiagram, elementNameDatatype1, elementTypeDatatype1);
        DiagramElementOperator depElementDatatype2 = new DiagramElementOperator(depDiagram, elementNameDatatype2, elementTypeDatatype2);
        checkDiagramHasExactElements(new DiagramElementOperator[]{depElementClass, depElementDatatype1, depElementDatatype2}, depDiagram);
    }

    
    /**
     * Drop and name a new class on the dependency diagram. Add attribute and operation to it.
     * <br/>
     * RESULT: Class should be created in correct namespace
     */
    public void testAddNewClassOnDependencyDiagram(String projectName){
        String elementNameSrc = "ClassElSrc2";
        String elementNameDep = "ClassElDep2";

        ElementTypes elementTypeSrc = ElementTypes.CLASS;
        ElementTypes elementTypeDep = ElementTypes.CLASS;
        
        testAddNewElementOnDependencyDiagram(elementNameSrc, elementTypeSrc, elementNameDep, elementTypeDep);
        
        // check if element created on dependency diagram is nested for original element
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        ProjectRootNode root = new ProjectRootNode(pto.tree(),projectName);
        Node nodeClassSrc = new Node(root,"Model|"+elementNameSrc);
        if (!nodeClassSrc.isChildPresent(elementNameDep))
            throw new NotFoundException(elementNameDep+" created on "+elementNameSrc+DEP_DIAGRAM_FOOTER+" dependency diagram wasn't created as nested node of "+elementNameSrc);
    }

    public void testAddNewElementOnDependencyDiagram(String elementNameSrc, ElementTypes elementTypeSrc, String elementNameDep, ElementTypes elementTypeDep){

        DiagramOperator srcDiagram  = new DiagramOperator(dia.getName());
        DiagramElementOperator elementSrc = createElement(elementNameSrc, elementTypeSrc);

        // do not spent time on this?
        checkIfDependencyGenerationAvailable(elementSrc);
        // create dependency diagram
        createDependencyOnDiagramElement(elementSrc);

        DiagramOperator depDiagram  = new DiagramOperator(elementNameSrc+DEP_DIAGRAM_FOOTER);
        if (depDiagram == null)
            throw new NotFoundException(elementNameSrc+DEP_DIAGRAM_FOOTER+" dependency diagram wasn't found for "+elementNameSrc);
        
        // check elements on diagram
        DiagramElementOperator depElementSrc = new DiagramElementOperator(depDiagram, elementNameSrc, elementTypeSrc);
        checkDiagramHasExactElements(new DiagramElementOperator[]{depElementSrc}, depDiagram);

        // Add new element on dependency diagram
        Point p = depDiagram.getDrawingArea().getFreePoint(100);
        DiagramElementOperator depElementDep = depDiagram.putElementOnDiagram(elementNameDep, elementTypeDep, p.x, p.y);
        
        checkDiagramHasExactElements(new DiagramElementOperator[]{depElementSrc, depElementDep}, depDiagram);
    }

    /**
     * Make changes to diagram by dropping new elements onto diagram, then save
     * <br/>
     * All changes to diagram should be saved
     */
    public void testModifiedDependencySaving(String projectName){
        String elementNameSrc = "ClassElSrc3";
        String elementNameDep = "ClassElDep3";

        ElementTypes elementTypeSrc = ElementTypes.CLASS;
        ElementTypes elementTypeDep = ElementTypes.CLASS;

        testAddNewElementOnDependencyDiagram(elementNameSrc, elementTypeSrc, elementNameDep, elementTypeDep);

        // find dependency diagram
        String depDiagramName = elementNameSrc+DEP_DIAGRAM_FOOTER;
        DiagramOperator depDiagram  = new DiagramOperator(depDiagramName);
        if (depDiagram == null){
            throw new NotFoundException(elementNameSrc+DEP_DIAGRAM_FOOTER+" dependency diagram wasn't found for "+elementNameSrc);
        }

        // save and close modified diagram
        depDiagram.saveDocument();
        depDiagram.closeWindow();

        // find in tree and reopen dependency diagram
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        ProjectRootNode root = new ProjectRootNode(pto.tree(),projectName);
        Node nodeDiagrams = new Node(root,"Diagrams");
        Node nodeDiagram = new Node(nodeDiagrams, depDiagramName);
        pto.tree().clickOnPath(nodeDiagram.getTreePath(), 2);
        new EventTool().waitNoEvent(1000);        

        DiagramOperator reopenDepDiagram = new DiagramOperator(depDiagramName);
        if (reopenDepDiagram == null){
            throw new NotFoundException(depDiagramName+" dependency diagram wasn't reopened");
        }

        // check elements on diagram
        DiagramElementOperator depElementSrc = new DiagramElementOperator(reopenDepDiagram, elementNameSrc, elementTypeSrc);
        DiagramElementOperator depElementDep = new DiagramElementOperator(reopenDepDiagram, elementNameDep, elementTypeDep);
        checkDiagramHasExactElements(new DiagramElementOperator[]{depElementSrc, depElementDep}, reopenDepDiagram);
        
    }
    
    public void testSrcOf2LinkedElements(ElementTypes elementTypeSrc, ElementTypes elementTypeDst, LinkTypes linkType){
    	COUNT++;
        String elementNameSrc = "DepElSrc"+COUNT;
        String elementNameDst = "DepElDst"+COUNT;
        
        // special processing for nested link
        if(linkType.equals(LinkTypes.NESTED_LINK))
            linkType=linkType.NESTED_LINK(elementTypeDst.toString());
        
        // draw elements and links on diagram
        DiagramOperator srcDiagram  = new DiagramOperator(dia.getName());
        DiagramElementOperator elementSrc = createElement(elementNameSrc, elementTypeSrc);
        DiagramElementOperator elementDst = createElement(elementNameDst, elementTypeDst);
        eventTool.waitNoEvent(500);
        LinkOperator link = srcDiagram.createLinkOnDiagram(linkType, elementSrc, elementDst);
        
        // do not spent time on this?
        checkIfDependencyGenerationAvailable(elementSrc);
        // create dependency diagram
        createDependencyOnDiagramElement(elementSrc);
        
        DiagramOperator depDiagram  = new DiagramOperator(elementNameSrc+DEP_DIAGRAM_FOOTER);
        if (depDiagram == null)
            throw new NotFoundException(elementNameSrc+DEP_DIAGRAM_FOOTER+" dependency diagram wasn't found for "+elementNameSrc);
        
        // check elements on diagram
        DiagramElementOperator depElementSrc = new DiagramElementOperator(depDiagram, elementNameSrc, elementTypeSrc);
        
        // if link should be drawn on dependency diagram or not
        if (isNotDependencyRelation(linkType)) {
            checkDiagramHasExactElements(new DiagramElementOperator[]{depElementSrc}, depDiagram);
            
            if (depElementSrc.getLinks().size()>0){
                throw new ElementVerificationException("Unnecessary links are created on Diagram", null);
            }
            
        } else {
            DiagramElementOperator depElementDst = new DiagramElementOperator(depDiagram, elementNameDst, elementTypeDst);
            checkDiagramHasExactElements(new DiagramElementOperator[]{depElementSrc, depElementDst}, depDiagram);
            LinkTypes lType=linkType;
            if (linkType.equals(LinkTypes.COMPOSITION)||linkType.equals(LinkTypes.NAVIGABLE_COMPOSITION)|| linkType.equals(LinkTypes.NAVIGABLE_AGGREGATION))
               lType =  LinkTypes.AGGREGATION;
            else if (linkType.equals(LinkTypes.NAVIGABLE_ASSOCIATION))
               lType = LinkTypes.ASSOCIATION;  
            checkDiagramHasLink(depElementSrc, depElementDst, lType);
            
            if (depElementSrc.getLinks().size()>1){
                throw new ElementVerificationException("Unnecessary links are created on Diagram", null);
            }
        }
    }
    
    
    public void testSrcOf4LinkedElements(
            ElementTypes elementTypeSrc,
            ElementTypes elementTypeDst1, LinkTypes linkType1,
            ElementTypes elementTypeDst2, LinkTypes linkType2,
            ElementTypes elementTypeDst3, LinkTypes linkType3){
        
        COUNT++;
        // diagram elements names
        String elementNameSrc = "DepElSrc"+COUNT;
        String elementNameDst1 = "DepElDst1"+COUNT;
        String elementNameDst2 = "DepElDst2"+COUNT;
        String elementNameDst3 = "DepElDst3"+COUNT;
        String realLinkType1 = null;
        String realLinkType2 = null;
        String realLinkType3 = null;
        
        // process Nested links in special way
        if(linkType1.equals(LinkTypes.NESTED_LINK)){
            realLinkType1 = elementTypeDst1.toString();
            linkType1=LinkTypes.NESTED_LINK.NESTED_LINK(elementTypeDst1.toString());
        }
        if(linkType2.equals(LinkTypes.NESTED_LINK)){
            realLinkType2 = elementTypeDst2.toString();
            linkType2=LinkTypes.NESTED_LINK.NESTED_LINK(elementTypeDst2.toString());
        }
        if(linkType3.equals(LinkTypes.NESTED_LINK)){
            realLinkType3 = elementTypeDst3.toString();
            linkType3=LinkTypes.NESTED_LINK.NESTED_LINK(elementTypeDst3.toString());
        }
        // create elements on original diagram.
        DiagramOperator srcDiagram  = new DiagramOperator(dia.getName());
        DiagramElementOperator elementSrc = createElement(elementNameSrc, elementTypeSrc);
        
        DiagramElementOperator elementDst1 = createElement(elementNameDst1, elementTypeDst1);
        if (realLinkType1!= null) linkType1=linkType1.NESTED_LINK(realLinkType1);
        eventTool.waitNoEvent(500);
        LinkOperator link1 = srcDiagram.createLinkOnDiagram(linkType1, elementSrc, elementDst1);
        
        DiagramElementOperator elementDst2 = createElement(elementNameDst2, elementTypeDst2);
        if (realLinkType2!= null) linkType2=linkType2.NESTED_LINK(realLinkType2);
        eventTool.waitNoEvent(500);
        LinkOperator link2 = srcDiagram.createLinkOnDiagram(linkType2, elementSrc, elementDst2);
        
        DiagramElementOperator elementDst3 = createElement(elementNameDst3, elementTypeDst3);
        if (realLinkType3!= null) linkType3=linkType3.NESTED_LINK(realLinkType3);
        eventTool.waitNoEvent(500);
        LinkOperator link3 = srcDiagram.createLinkOnDiagram(linkType3, elementSrc, elementDst3);
        
        // do not spent time on this?
        checkIfDependencyGenerationAvailable(elementSrc);
        // generate dependency diagram
        createDependencyOnDiagramElement(elementSrc);
        
        // check elements and links on dependency diagram
        DiagramOperator depDiagram  = new DiagramOperator(elementNameSrc+DEP_DIAGRAM_FOOTER);
        if (depDiagram == null)
            throw new NotFoundException(elementNameSrc+DEP_DIAGRAM_FOOTER+" dependency diagram wasn't found for "+elementNameSrc);
        // this array will contain elements with relation that should be drawn on dependency diagram
        ArrayList<String> depElementNames = new ArrayList();
        if (!isNotDependencyRelation(linkType1)){
            depElementNames.add(elementNameDst1);
            if (realLinkType1!= null) linkType1=linkType1.NESTED_LINK(realLinkType1);
             
            if (linkType1.equals(LinkTypes.COMPOSITION)||linkType1.equals(LinkTypes.NAVIGABLE_COMPOSITION)|| linkType1.equals(LinkTypes.NAVIGABLE_AGGREGATION))
               linkType1 =  LinkTypes.AGGREGATION;
            else if (linkType1.equals(LinkTypes.NAVIGABLE_ASSOCIATION))
               linkType1 = LinkTypes.ASSOCIATION;            
            checkDiagramHasLink(elementNameSrc, elementNameDst1, linkType1, depDiagram);
        }
        if (!isNotDependencyRelation(linkType2)){
            depElementNames.add(elementNameDst2);
            if (realLinkType2!= null) linkType2=linkType2.NESTED_LINK(realLinkType2);
            if (linkType2.equals(LinkTypes.COMPOSITION)||linkType2.equals(LinkTypes.NAVIGABLE_COMPOSITION)|| linkType2.equals(LinkTypes.NAVIGABLE_AGGREGATION))
               linkType2 =  LinkTypes.AGGREGATION;
            else if (linkType2.equals(LinkTypes.NAVIGABLE_ASSOCIATION))
               linkType2 = LinkTypes.ASSOCIATION; 
            checkDiagramHasLink(elementNameSrc, elementNameDst2, linkType2, depDiagram);
        }
        if (!isNotDependencyRelation(linkType3)){
            depElementNames.add(elementNameDst3);
            if (realLinkType3!= null) linkType3=linkType3.NESTED_LINK(realLinkType3);
             if (linkType3.equals(LinkTypes.COMPOSITION)||linkType3.equals(LinkTypes.NAVIGABLE_COMPOSITION)|| linkType3.equals(LinkTypes.NAVIGABLE_AGGREGATION))
               linkType3 =  LinkTypes.AGGREGATION;
            else if (linkType3.equals(LinkTypes.NAVIGABLE_ASSOCIATION))
               linkType3 = LinkTypes.ASSOCIATION; 
            checkDiagramHasLink(elementNameSrc, elementNameDst3, linkType3, depDiagram);
        }
        DiagramElementOperator depElementSrc = new DiagramElementOperator(depDiagram, elementNameSrc, elementTypeSrc);
        
        // if there were link that should be drawn on dependency diagram or not
        if (depElementNames.size() == 0) {
            if (depElementSrc.getLinks().size()>0){
                throw new ElementVerificationException("Unnecessary links are created on Diagram", null);
            }
            checkDiagramHasExactElements(new String[]{elementNameSrc}, depDiagram);
        } else {
            if (depElementSrc.getLinks().size()>depElementNames.size()){
                throw new ElementVerificationException("Unnecessary links are created on Diagram", null);
            }
            // to check that src also exists on diagram
            depElementNames.add(elementNameSrc);
            checkDiagramHasExactElements(depElementNames.toArray(new String[0]), depDiagram);
        }
    }
    
    
    public void testDependencyGenerationAvailability(ElementTypes elementTypeSrc){
        String elementNameSrc = "DepElSrc";
        
        // draw elements and links on diagram
        DiagramOperator srcDiagram  = new DiagramOperator(dia.getName());
        DiagramElementOperator elementSrc = createElement(elementNameSrc, elementTypeSrc);

        checkIfDependencyGenerationAvailable(elementSrc);
    }
    
    public void testDependencyGenerationAbsence(ElementTypes elementTypeSrc){
        String elementNameSrc = "DepElSrc";
        
        // draw elements and links on diagram
        DiagramOperator srcDiagram  = new DiagramOperator(dia.getName());
        DiagramElementOperator elementSrc = createElement(elementNameSrc, elementTypeSrc);

        checkIfDependencyGenerationAbsent(elementSrc);
    }

    /**
     * checks if link shouldn't be generated on dependency diagram
     * 
     * @param linkType link that should be checked
     * @return true if link shouldn't be generated on dependency diagram, otherwise false.
     */
    private boolean isNotDependencyRelation(LinkTypes linkType){
        return linkType.equals(LinkTypes.NESTED_LINK) ||
                linkType.equals(LinkTypes.DELEGATE) ||
                linkType.equals(LinkTypes.INCLUDE) ||
                linkType.equals(LinkTypes.EXTEND) ||
                linkType.equals(LinkTypes.COMPOSITION) ||
                linkType.equals(LinkTypes.AGGREGATION) ||
                linkType.equals(LinkTypes.ASSOCIATION);
    }
    
    public void createDependencyOnNode(Node node){
        new ActionNoBlock(null, PopupConstants.GENERATE_DEPENDENCY_DIAGRAM).performPopup(node);
        eventTool.waitNoEvent(2000);
        
    }
    
    public void checkIfDependencyGenerationAvailable(DiagramElementOperator element){
        if (!isDependencyGenerationMenuItemExists(element))
            throw new UnexpectedMenuItemStatusException(
                    PopupConstants.GENERATE_DEPENDENCY_DIAGRAM+" menu item is absent on "+element.getType()+" element named "+element.getName(),
                    UnexpectedMenuItemStatusException.Status.ABSENT,
                    UnexpectedMenuItemStatusException.MenuType.DIAGRAMELEMENT_POPUP
                    );
    }
    
    public void checkIfDependencyGenerationAbsent(DiagramElementOperator element){
        if (isDependencyGenerationMenuItemExists(element))
            throw new UnexpectedMenuItemStatusException(
                    PopupConstants.GENERATE_DEPENDENCY_DIAGRAM+" menu item is unexpectedly exists on "+element.getType()+" element named "+element.getName(),
                    UnexpectedMenuItemStatusException.Status.EXIST,
                    UnexpectedMenuItemStatusException.MenuType.DIAGRAMELEMENT_POPUP
                    );
    }
    
    public boolean isDependencyGenerationMenuItemExists(DiagramElementOperator element){
        boolean isAvailable = false;
        
        //JPopupMenuOperator popup = element.getPopup();
        JPopupMenuOperator popup =element.getGeneralPopup();
        MenuElement [] elements = popup.getSubElements();
        for (int i=0; i<elements.length; i++){
            if (elements[i] instanceof JMenuItem){
                if ( ((JMenuItem)elements[i]).getText().equalsIgnoreCase(PopupConstants.GENERATE_DEPENDENCY_DIAGRAM) ){
                    isAvailable = true;
                    break;
                }
            }
        }
        // close popup menu
        popup.pushKey(KeyEvent.VK_ESCAPE);
        return isAvailable;
    }
    
    public void createDependencyOnDiagramElement(DiagramElementOperator element){
        JPopupMenuOperator popup = element.getGeneralPopup();
         
        boolean isBug = false;
        try{   // known bug on Unix OSs
            popup.pushMenu(PopupConstants.GENERATE_DEPENDENCY_DIAGRAM);
        }catch(TimeoutExpiredException e){
            isBug = true;
        }
        if(isBug){  // try to open popup menu again for Unix
            element.select();
            popup = element.getPopup();
            popup.pushMenu(PopupConstants.GENERATE_DEPENDENCY_DIAGRAM);
        }
        
        eventTool.waitNoEvent(2000);
    }
    
    public void checkDiagramHasExactElements(String[] elementNames, DiagramOperator dia){
        DiagramElementOperator[] els = new DiagramElementOperator[elementNames.length];
        for (int i=0;i<elementNames.length; i++){
            els[i] = new DiagramElementOperator(dia, elementNames[i]);
        }
        checkDiagramHasExactElements(els, dia);
    }
    
    public void checkDiagramHasExactElements(DiagramElementOperator[] elements, DiagramOperator dia){
        ArrayList<DiagramElementOperator> expectedElmts = new ArrayList<DiagramElementOperator>();
        for(int i=0;i<elements.length;i++){
            expectedElmts.add(elements[i]);
        }

        ArrayList<DiagramElementOperator> diaElmts = dia.getDiagramElements();
        for(int i=0;i<expectedElmts.size();i++){
            int index = diaElmts.indexOf(expectedElmts.get(i));
            if (index<0){
                throw new NotFoundOnDiagramException(
                        ElementTypes.valueOf(expectedElmts.get(i).getElementType()),
                        NotFoundOnDiagramException.ActionTypes.DEPENDENCY);
            }
            diaElmts.remove(index);
        }
        
        if (diaElmts.size()>0){
            throw new ElementVerificationException("Unnecessary "+diaElmts.size()+" elements left on Diagram", null);
        }
    }
    
    public void checkDiagramHasLink(DiagramElementOperator source, DiagramElementOperator destination, LinkTypes linkType){
        LinkOperator link = new LinkOperator(source, destination, linkType);
    }
    
    public void checkDiagramHasLink(String sourceName, String destinationName, LinkTypes linkType, DiagramOperator dia){
        DiagramElementOperator source = new DiagramElementOperator(dia, sourceName);
        DiagramElementOperator destination = new DiagramElementOperator(dia, destinationName);
        checkDiagramHasLink(source, destination, linkType);
    }
    
    
    ///////////////////////
    public Node getNode(String projectName, String nodePath){
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        ProjectRootNode root = new ProjectRootNode(pto.tree(),projectName);
        
        Node node = new Node(root, nodePath);
        return node;
    }
    
    public void selectNode(String projectName, String nodePath){
        Node node = getNode(projectName, nodePath);
        node.select();
    }
    
    
    public boolean nodeExists(String projectName, String path){
        long waitNodeTime = JemmyProperties.getCurrentTimeouts().getTimeout("JTreeOperator.WaitNextNodeTimeout");
        JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", 2000);
        try{
            Node node = getNode(projectName, path);
            node.select();
            return true;
        }catch(Exception e){
            return false;
        }finally{
            JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", waitNodeTime);
        }
    }
    
    
    public boolean allNodesExist(String projectName, String parentPath, String[] nodeLabels){
        for(int i=0; i<nodeLabels.length; i++){
            if (!nodeExists(projectName, parentPath+"|"+nodeLabels[i])){
                return false;
            }
        }
        return true;
    }

}
