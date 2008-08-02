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


package org.netbeans.test.uml.componentdiagram;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTreeOperator;


import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.DrawingAreaOperator;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.ExpandedElementTypes;
import org.netbeans.test.umllib.LinkOperator;
import org.netbeans.test.umllib.LinkTypes;
import org.netbeans.test.umllib.NewDiagramWizardOperator;
import org.netbeans.test.umllib.UMLPaletteOperator;
import org.netbeans.test.umllib.exceptions.NotFoundException;
import org.netbeans.test.umllib.testcases.UMLTestCase;
import org.netbeans.test.umllib.util.LabelsAndTitles;
import org.netbeans.test.umllib.util.LibProperties;



/**
 * 
 * @author psb
 * @spec UML/ComponentDiagram.xml
 */
public class ComponentDiagramElementsContextMenu extends UMLTestCase {
    
    //some system properties
    private static String contextPropItemName="Properties";
    private static String umlPropertyWindowTitle="Project Properties";
    private static String umlSourcePackagesLabel="Source Packages";
    private static String umlSourcePackagesColumn="Folder Label";
    private static String umlSourceUsageColumn="Model?";
    private static String mainTreeTabName="Projects";
    //common test properties
    private static String prName= "ComponentDiagramProjectECM";
    private static String project = prName+"|Model";
    private static String sourceProject = "source";
    private static boolean codeSync=false;
    private static String defaultNewElementName=org.netbeans.test.uml.componentdiagram.utils.Utils.defaultNewElementName;
    private static String defaultReturnType=org.netbeans.test.uml.componentdiagram.utils.Utils.defaultReturnType;
    private static String defaultAttributeType=org.netbeans.test.uml.componentdiagram.utils.Utils.defaultAttributeType;
    private static String defaultAttributeVisibility=org.netbeans.test.uml.componentdiagram.utils.Utils.defaultAttributeVisibility;
    private static String defaultOperationVisibility=org.netbeans.test.uml.componentdiagram.utils.Utils.defaultOperationVisibility;
    private ProjectsTabOperator pto=null;
    private Node lastDiagramNode=null;
    private String lastTestCase=null;
    private static String workdir=System.getProperty("nbjunit.workdir");
    //
    private static String activityDiagramName0 = "cpD";
    private static String workPkg0 = "pkg";
    private static long counter=0;
    //--
    private static ExpandedElementTypes elementType1=ExpandedElementTypes.COMPONENT;
    private static String menuItems1[]={"Edit|Lock Edit","Resize Element to Contents","Reset Edges","Hide|Parents|One Level","Hide|Parents|All Levels","Hide|Children|One Level","Hide|Children|All Levels","Show|Parents|One Level","Show|Parents|All Levels","Show|Children|One Level","Show|Children|All Levels","Generate Dependency Diagram",elementType1.toString()+"|Font",elementType1.toString()+"|Font Color",elementType1.toString()+"|Background Color",elementType1.toString()+"|Border Color"};
    //--
    private static ExpandedElementTypes elementType2=ExpandedElementTypes.CLASS;
    private static String menuItems2[]={"Insert Attribute","Delete Attribute","Edit|Lock Edit","Compartment|Customize...","Resize Element to Contents","Transform|To Actor","Transform|To Class","Transform|To Interface","Transform|To DataType","Transform|To Enumeration","Reset Edges","Hide|Parents|One Level","Hide|Parents|All Levels","Hide|Children|One Level","Hide|Children|All Levels","Show|Parents|One Level","Show|Parents|All Levels","Show|Children|One Level","Show|Children|All Levels","Redefine Operations","Generate Dependency Diagram",elementType2.toString()+"|Font",elementType2.toString()+"|Font Color",elementType2.toString()+"|Background Color",elementType2.toString()+"|Border Color"};
    //--
    private static ExpandedElementTypes elementType3=ExpandedElementTypes.INTERFACE;
    private static String menuItems3[]={"Edit|Lock Edit","Compartment|Customize...","Resize Element to Contents","Transform|To Actor","Transform|To Class","Transform|To Interface","Transform|To DataType","Transform|To Enumeration","Reset Edges","Hide|Parents|One Level","Hide|Parents|All Levels","Hide|Children|One Level","Hide|Children|All Levels","Show|As Icon","Show|Parents|One Level","Show|Parents|All Levels","Show|Children|One Level","Show|Children|All Levels","Redefine Operations","Generate Dependency Diagram",elementType3.toString()+"|Font",elementType3.toString()+"|Font Color",elementType3.toString()+"|Background Color",elementType3.toString()+"|Border Color"};
    //--
    private static ExpandedElementTypes elementType4=ExpandedElementTypes.PACKAGE;
    private static String menuItems4[]={"Edit|Lock Edit","Resize Element to Contents","Reset Edges","Hide|Parents|One Level","Hide|Parents|All Levels","Hide|Children|One Level","Hide|Children|All Levels","Show|Name in Tab","Show|Parents|One Level","Show|Parents|All Levels","Show|Children|One Level","Show|Children|All Levels",elementType4.toString()+"|Font",elementType4.toString()+"|Font Color",elementType4.toString()+"|Background Color",elementType4.toString()+"|Border Color"};
    //--
    private static ExpandedElementTypes elementType5=ExpandedElementTypes.ARTIFACT;
    private static String menuItems5[]={"Edit|Lock Edit","Resize Element to Contents","Reset Edges","Hide|Parents|One Level","Hide|Parents|All Levels","Hide|Children|One Level","Hide|Children|All Levels","Show|Parents|One Level","Show|Parents|All Levels","Show|Children|One Level","Show|Children|All Levels","Generate Dependency Diagram",elementType5.toString()+"|Font",elementType5.toString()+"|Font Color",elementType5.toString()+"|Background Color",elementType5.toString()+"|Border Color"};
    //--
    private static ExpandedElementTypes elementType6=ExpandedElementTypes.TEMPLATE_CLASS;
    private static String menuItems6[]={"Insert Attribute","Delete Attribute","Edit|Lock Edit","Compartment|Customize...","Resize Element to Contents","Transform|To Actor","Transform|To Class","Transform|To Interface","Transform|To DataType","Transform|To Enumeration","Reset Edges","Hide|Parents|One Level","Hide|Parents|All Levels","Hide|Children|One Level","Hide|Children|All Levels","Show|Parents|One Level","Show|Parents|All Levels","Show|Children|One Level","Show|Children|All Levels","Redefine Operations","Generate Dependency Diagram",elementType6.toString()+"|Font",elementType6.toString()+"|Font Color",elementType6.toString()+"|Background Color",elementType6.toString()+"|Border Color"};
    //--
    private static ExpandedElementTypes elementType7=ExpandedElementTypes.DERIVATION_CLASSIFIER;
    private static String menuItems7[]={"Edit|Lock Edit","Resize Element to Contents","Reset Edges","Hide|Parents|One Level","Hide|Parents|All Levels","Hide|Children|One Level","Hide|Children|All Levels","Show|Parents|One Level","Show|Parents|All Levels","Show|Children|One Level","Show|Children|All Levels","Generate Dependency Diagram",elementType7.toString()+"|Font",elementType7.toString()+"|Font Color",elementType7.toString()+"|Background Color",elementType7.toString()+"|Border Color"};
    //--
    private static ExpandedElementTypes elementType8=ExpandedElementTypes.DESIGN_PATTERN;
    private static String menuItems8[]={"Edit|Lock Edit","Resize Element to Contents","Reset Edges","Hide|Parents|One Level","Hide|Parents|All Levels","Hide|Children|One Level","Hide|Children|All Levels","Show|Parents|One Level","Show|Parents|All Levels","Show|Children|One Level","Show|Children|All Levels","Generate Dependency Diagram",elementType8.toString()+"|Font",elementType8.toString()+"|Font Color",elementType8.toString()+"|Background Color",elementType8.toString()+"|Border Color","Promote Design Pattern..."};
    //--
    private static ExpandedElementTypes elementType9=ExpandedElementTypes.ROLE;
    private static String menuItems9[]={"Edit|Lock Edit","Compartment|Customize...","Resize Element to Contents","Reset Edges","Hide|Parents|One Level","Hide|Parents|All Levels","Hide|Children|One Level","Hide|Children|All Levels","Show|As Icon","Show|Parents|One Level","Show|Parents|All Levels","Show|Children|One Level","Show|Children|All Levels","Generate Dependency Diagram",LibProperties.getCurrentToolName(elementType9)+"|Font",LibProperties.getCurrentToolName(elementType9)+"|Font Color",LibProperties.getCurrentToolName(elementType9)+"|Background Color",LibProperties.getCurrentToolName(elementType9)+"|Border Color"};
    //--
    private static ExpandedElementTypes elementType10=ExpandedElementTypes.CLASS_ROLE;
    private static String menuItems10[]={"Edit|Lock Edit","Compartment|Customize...","Resize Element to Contents","Reset Edges","Hide|Parents|One Level","Hide|Parents|All Levels","Hide|Children|One Level","Hide|Children|All Levels","Show|As Icon","Show|Parents|One Level","Show|Parents|All Levels","Show|Children|One Level","Show|Children|All Levels","Generate Dependency Diagram",LibProperties.getCurrentToolName(elementType10)+"|Font",LibProperties.getCurrentToolName(elementType10)+"|Font Color",LibProperties.getCurrentToolName(elementType10)+"|Background Color",LibProperties.getCurrentToolName(elementType10)+"|Border Color"};
    //--
    private static ExpandedElementTypes elementType11=ExpandedElementTypes.ACTOR_ROLE;
    private static String menuItems11[]={"Edit|Lock Edit","Resize Element to Contents","Reset Edges","Hide|Parents|One Level","Hide|Parents|All Levels","Hide|Children|One Level","Hide|Children|All Levels","Show|Parents|One Level","Show|Parents|All Levels","Show|Children|One Level","Show|Children|All Levels","Generate Dependency Diagram",LibProperties.getCurrentToolName(elementType11)+"|Font",LibProperties.getCurrentToolName(elementType11)+"|Font Color",LibProperties.getCurrentToolName(elementType11)+"|Background Color",LibProperties.getCurrentToolName(elementType11)+"|Border Color"};
    //--
    private static ExpandedElementTypes elementType12=ExpandedElementTypes.INTERFACE_ROLE;
    private static String menuItems12[]={"Edit|Lock Edit","Compartment|Customize...","Resize Element to Contents","Reset Edges","Hide|Parents|One Level","Hide|Parents|All Levels","Hide|Children|One Level","Hide|Children|All Levels","Show|As Icon","Show|Parents|One Level","Show|Parents|All Levels","Show|Children|One Level","Show|Children|All Levels","Generate Dependency Diagram",LibProperties.getCurrentToolName(elementType12)+"|Font",LibProperties.getCurrentToolName(elementType12)+"|Font Color",LibProperties.getCurrentToolName(elementType12)+"|Background Color",LibProperties.getCurrentToolName(elementType12)+"|Border Color"};
    //--
    private static ExpandedElementTypes elementType13=ExpandedElementTypes.USE_CASE_ROLE;
    private static String menuItems13[]={"Edit|Lock Edit","Resize Element to Contents","Insert Extension Point","Reset Edges","Hide|Parents|One Level","Hide|Parents|All Levels","Hide|Children|One Level","Hide|Children|All Levels","Show|Parents|One Level","Show|Parents|All Levels","Show|Children|One Level","Show|Children|All Levels","Generate Dependency Diagram",LibProperties.getCurrentToolName(elementType13)+"|Font",LibProperties.getCurrentToolName(elementType13)+"|Font Color",LibProperties.getCurrentToolName(elementType13)+"|Background Color",LibProperties.getCurrentToolName(elementType13)+"|Border Color"};
    //--
    private static ExpandedElementTypes elementType14=ExpandedElementTypes.COMMENT;
    private static String menuItems14[]={"Compartment|Font","Compartment|Font Color","Edit|Lock Edit","Resize Element to Contents","Reset Edges",elementType14.toString()+"|Font",elementType14.toString()+"|Font Color",elementType14.toString()+"|Background Color",elementType14.toString()+"|Border Color"};
    //--
    private static ExpandedElementTypes elementType14_2=ExpandedElementTypes.LINK_COMMENT;
    private static String menuItems14_2[]={"Compartment|Font","Compartment|Font Color","Edit|Lock Edit","Resize Element to Contents","Reset Edges",elementType14_2.toString()+"|Font",elementType14_2.toString()+"|Font Color",elementType14_2.toString()+"|Background Color",elementType14_2.toString()+"|Border Color"};
    //--
    private static LinkTypes elementType15=LinkTypes.GENERALIZATION;
    private static ExpandedElementTypes[] elements15={ExpandedElementTypes.CLASS,ExpandedElementTypes.CLASS};
    private static String menuItems15[]={"Labels|Name","Labels|Reset Labels","Find|Source Element","Find|Target Element",elementType15.toString()+"|Border Color"};
    //--
    private static LinkTypes elementType16=LinkTypes.IMPLEMENTATION;
    private static ExpandedElementTypes[] elements16={ExpandedElementTypes.CLASS,ExpandedElementTypes.INTERFACE};
    private static String menuItems16[]={"Labels|Name","Labels|Reset Labels","Find|Source Element","Find|Target Element",elementType16.toString()+"|Border Color"};
    //common
    private static String commonMenuItems[]={"Edit|Copy","Edit|Cut","Edit|Delete","Edit|Paste","Edit|Select All","Edit|Invert Selection","Edit|Select All Similar Elements","Synchronize Element with Data","Select in Model","Properties","Associate With...","Apply Design Pattern..."};
    //
    JTreeOperator prTree;
    
    
    /** Need to be defined because of JUnit */
    public ComponentDiagramElementsContextMenu(String name) {
        super(name);
    }
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(org.netbeans.test.uml.componentdiagram.ComponentDiagramElementsContextMenu.class);
        return suite;
    }
    
    private DiagramOperator createDiagram(String project,String workPkg, String diagram){
        //
        org.netbeans.test.umllib.Utils.RetAll rt=org.netbeans.test.umllib.Utils.createDiagram(project,workPkg,diagram,NewDiagramWizardOperator.COMPONENT_DIAGRAM);
        pto = rt.pto;
        prTree=new JTreeOperator(pto);
        lastDiagramNode=rt.lastDiagramNode;
        return rt.dOp;
   }
    
    
    
    public void testCreate1() {
        testElementContext(elementType1,menuItems1);
    }
    public void testCreate2() {
        testElementContext(elementType2, menuItems2);
    }
    public void testCreate3() {
        testElementContext(elementType3, menuItems3);
    }
    public void testCreate4() {
        testElementContext(elementType4, menuItems4);
    }
    public void testCreate5() {
        testElementContext(elementType5, menuItems5);
    }
    public void testCreate6() {
        testElementContext(elementType6, menuItems6);
    }
    public void testCreate7() {
        testElementContext(elementType7, menuItems7);
    }
    public void testCreate8() {
        testElementContext(elementType8, menuItems8);
    }
    public void testCreate9() {
        testElementContext(elementType9, menuItems9);
    }
    public void testCreate10() {
        testElementContext(elementType10, menuItems10);
    }
    public void testCreate11() {
        testElementContext(elementType11, menuItems11);
    }
    public void testCreate12() {
        testElementContext(elementType12, menuItems12);
    }
    public void testCreate13() {
        testElementContext(elementType13, menuItems13);
    }
    public void testCreate14() {
        testElementContext(elementType14, menuItems14);
    }
    public void testCreate14_2() {
        testElementContext(elementType14_2, menuItems14_2);
    }
    
    public void testCreate15() {
        testLinkContext(elementType15,elements15,menuItems15);
    }
    public void testCreate16() {
        testLinkContext(elementType16,elements16,menuItems16);
    }

    
    
    
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
        pto = ProjectsTabOperator.invoke();
        if(!codeSync)
        {
            org.netbeans.test.uml.componentdiagram.utils.Utils.commonComponentDiagramSetup(workdir,prName);
            //
            codeSync=true;
        }
    }
    
    public void tearDown() {
        org.netbeans.test.umllib.util.Utils.makeScreenShot(lastTestCase);
        //popup protection
        MainWindowOperator.getDefault().pushKey(KeyEvent.VK_ESCAPE);
        new EventTool().waitNoEvent(1000);
        //
        closeAllModal();
        org.netbeans.test.umllib.util.Utils.saveAll();
        if(lastDiagramNode!=null)
        {
            lastDiagramNode.collapse();
            new Node(lastDiagramNode.tree(),lastDiagramNode.getParentPath()).collapse();
        }
        try{
            DiagramOperator d=new DiagramOperator("cpD");
            d.closeAllDocuments();
            d.waitClosed();
            new EventTool().waitNoEvent(1000);
        }catch(Exception ex){};
        closeAllModal();
        //save
        org.netbeans.test.umllib.util.Utils.tearDown();
   }
    
    private void testElementContext(ExpandedElementTypes elementType,String[] customMenuItems) {
        lastTestCase=getCurrentTestNamesWithCheck()[1];
        String elementName=LibProperties.getCurrentDefaultName(elementType);
        String element=LibProperties.getProperties().getCurrentToolName(elementType);
        //
        String workPkg=workPkg0+counter;
        String diagramName=activityDiagramName0+counter;
        counter++;
        //
        DiagramOperator d = createDiagram(project,workPkg, diagramName);
        //
        int numChild=lastDiagramNode.getChildren().length;
        //
        UMLPaletteOperator pl=new UMLPaletteOperator();
        pl.waitComponentShowing(true);
        pl.waitComponentVisible(true);
        //
        try {
            pl.selectTool(element);
        } catch(NotFoundException ex) {
            fail("BLOCKING: Can't find '"+element+"' in paletter");
        }
        //
        DrawingAreaOperator drAr=d.getDrawingArea();
        java.awt.Point a=drAr.getFreePoint();
        drAr.clickMouse(a.x,a.y,1);
        drAr.pushKey(KeyEvent.VK_ESCAPE);
        pl.waitSelection(element,false);
        new EventTool().waitNoEvent(500);
        //
        try{Thread.sleep(500);}catch(Exception ex){}
        a=drAr.getFreePoint(100);
        drAr.clickMouse(a.x,a.y,1,InputEvent.BUTTON3_MASK);
        drAr.pushKey(KeyEvent.VK_ESCAPE);
        //
        DiagramElementOperator dEl=null;
        try {
            dEl=new DiagramElementOperator(d,elementName,elementType,0);
        } catch(Exception ex) {
            try {
                fail(element+" wasn't added to diagram, but object with type:"+new DiagramElementOperator(d,elementName).getType()+": and element type :"+new DiagramElementOperator(d,elementName).getElementType()+": was added whyle type should be :"+elementType+": was added");
            } catch(Exception ex2) {
                
            }
            fail(element+" wasn't added to diagram.");
        }
        dEl.select();
        //call popup
        drAr.clickMouse(dEl.getCenterPoint().x,dEl.getCenterPoint().y,1,InputEvent.BUTTON3_MASK);
        verify(customMenuItems);
    }
    private void testLinkContext(LinkTypes elementType,ExpandedElementTypes[] elements,String [] customMenuItems) {
        lastTestCase=getCurrentTestNamesWithCheck()[1];
        String elementName=LibProperties.getCurrentDefaultName(elementType);
        String element=LibProperties.getCurrentToolName(elementType);
        //
        String workPkg=workPkg0+counter;
        String diagramName=activityDiagramName0+counter;
        counter++;
        String localElName1="El1";
        String localElName2="El2";
        //
        DiagramOperator d = createDiagram(project,workPkg, diagramName);
        //
        UMLPaletteOperator pl=new UMLPaletteOperator();
        DrawingAreaOperator drAr=d.getDrawingArea();
        //
        java.awt.Point a=drAr.getFreePoint();
        DiagramElementOperator dE1=null,dE2=null;
            dE1=d.putElementOnDiagram(localElName1,elements[0],a.x,a.y);
            a=drAr.getFreePoint(150);
            dE2=d.putElementOnDiagram(localElName2,elements[1],a.x,a.y);
        //
        try {
            pl.selectTool(element);
        } catch(NotFoundException ex) {
            fail("BLOCKING: Can't find '"+element+"' in paletter");
        }
        //
        drAr.clickMouse(dE1.getCenterPoint().x,dE1.getCenterPoint().y,1);
        drAr.clickMouse(dE2.getCenterPoint().x,dE2.getCenterPoint().y,1);
        //
        drAr.pushKey(KeyEvent.VK_ESCAPE);
        pl.waitSelection(element,false);
        new EventTool().waitNoEvent(500);
        //
        try{Thread.sleep(500);}catch(Exception ex){}
        a=drAr.getFreePoint(100);
        drAr.clickMouse(a.x,a.y,1,InputEvent.BUTTON3_MASK);
        drAr.pushKey(KeyEvent.VK_ESCAPE);
        //"workaround for assembly connector"
        if(elementType.equals(LinkTypes.ASSEMBLY))
        {
            dE1=new DiagramElementOperator(d,"",ExpandedElementTypes.PORT,0);
            elementType=LinkTypes.USAGE;
        }
        //
        LinkOperator testedlink=null;
        try {
            testedlink=LinkOperator.findLink(dE1, dE2,new LinkOperator.LinkByTypeChooser(elementType),0);
        } catch(Exception ex) {
            fail(element+" of type "+elementType+" wasn't added to diagram.");
        }
        if(testedlink==null) {
            LinkOperator altLink=null;
            try {
                altLink=LinkOperator.findLink(dE1, dE2,new LinkOperator.LinkByTypeChooser(LinkTypes.ANY),0);
            } catch(Exception ex) {
                fail("any link find failed.");
            }
            if(altLink!=null)fail("Can't find "+elementType+" link between elements, but the is "+altLink.getType()+" link.");
        }
        //
        if(testedlink==null && elementType.equals(LinkTypes.ACTIVITY_EDGE)) {
            //fail("Can't find Activity Edge/MultiFlow link between elemens, may be test library limitation, please recheck manually.");
        }
        else 
        {
            assertTrue("Can't find "+elementType+" link between elements",testedlink!=null);
        }
        //
        if(elementType.equals(LinkTypes.ACTIVITY_EDGE))
        {
            //tried to get popup for activity edge
            drAr.clickMouse((dE1.getCenterPoint().x+dE2.getCenterPoint().x)/2,(dE1.getCenterPoint().y+dE2.getCenterPoint().y)/2,1,InputEvent.BUTTON3_MASK);
        }
        else 
        {
            drAr.clickMouse(testedlink.getNearCenterPoint().x,testedlink.getNearCenterPoint().y,1,InputEvent.BUTTON3_MASK);
        }
        verify(customMenuItems);
    }
    
    private void verify(String[] add)
    {
       JPopupMenuOperator pop=new JPopupMenuOperator();
        pop.waitComponentShowing(true);
        try{Thread.sleep(500);}catch(Exception ex){}
        //workaround for 78301
        pop.pushKey(KeyEvent.VK_LEFT);
        //
        try{Thread.sleep(500);}catch(Exception ex){}
        //
        pop=new JPopupMenuOperator();
        //
        String fails="";
        for(int i=0;i<commonMenuItems.length;i++) {
            JMenuItemOperator it=null;
            try {
                it=pop.showMenuItem(commonMenuItems[i]);
                if(it==null) {
                    fails+="Null item "+commonMenuItems[i]+";\n";
                    org.netbeans.test.umllib.util.Utils.makeScreenShot();
                }
            } catch(Exception ex) {
                fails+="Timeout on selection of "+commonMenuItems[i]+";\n";
                org.netbeans.test.umllib.util.Utils.makeScreenShot();
            }
            //returns back from inner popup
            if(commonMenuItems[i].indexOf("|")>-1)
            {
                new EventTool().waitNoEvent(500);
                pop.pushKey(KeyEvent.VK_LEFT);
                new EventTool().waitNoEvent(500);
                pop.pushKey(KeyEvent.VK_LEFT);
            }
        }
        if(add!=null)for(int i=0;i<add.length;i++) {
            JMenuItemOperator it=null;
            try {
                it=pop.showMenuItem(add[i]);
                if(it==null) {
                    fails+="Null item "+add[i]+";\n";
                    org.netbeans.test.umllib.util.Utils.makeScreenShot(lastTestCase);
                }
            } catch(Exception ex) {
                fails+="Timeout on selection of "+add[i]+";\n";
                org.netbeans.test.umllib.util.Utils.makeScreenShot(lastTestCase);
            }
            //returns back from inner popup
            if(add[i].indexOf("|")>-1)
            {
                new EventTool().waitNoEvent(500);
                pop.pushKey(KeyEvent.VK_LEFT);
                new EventTool().waitNoEvent(500);
                pop.pushKey(KeyEvent.VK_LEFT);
            }
        }
        //
        assertTrue("There are some problems with context menu: "+fails,fails.length()==0);
        
    }
    
}
