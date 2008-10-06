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


package org.netbeans.test.uml.activitydiagram;
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
import org.netbeans.jemmy.operators.JPopupMenuOperator;


import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.DrawingAreaOperator;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.LinkOperator;
import org.netbeans.test.umllib.LinkTypes;
import org.netbeans.test.umllib.NewDiagramWizardOperator;
import org.netbeans.test.umllib.UMLPaletteOperator;
import org.netbeans.test.umllib.exceptions.NotFoundException;
import org.netbeans.test.umllib.testcases.UMLTestCase;



/**
 *
 * @author psb
 * @spec uml/UML-EditControl.xml
 */
public class ActivityDiagramElementsContextMenu extends UMLTestCase {
    
    //some system properties
    private static String contextPropItemName="Properties";
    private static String umlPropertyWindowTitle="Project Properties";
    private static String umlSourcePackagesLabel="Source Packages";
    private static String umlSourcePackagesColumn="Folder Label";
    private static String umlSourceUsageColumn="Model?";
    private static String mainTreeTabName="Projects";
    //common test properties
    private static String prName= "ActivityDiagramProjectECM";
    private static String project = prName+"|Model";
    private static String sourceProject = "source";
    private static boolean codeSync=false;
    private static String defaultNewElementName=org.netbeans.test.uml.activitydiagram.utils.Utils.defaultNewElementName;
    private static String defaultReturnType=org.netbeans.test.uml.activitydiagram.utils.Utils.defaultReturnType;
    private static String defaultAttributeType=org.netbeans.test.uml.activitydiagram.utils.Utils.defaultAttributeType;
    private static String defaultAttributeVisibility=org.netbeans.test.uml.activitydiagram.utils.Utils.defaultAttributeVisibility;
    private static String defaultOperationVisibility=org.netbeans.test.uml.activitydiagram.utils.Utils.defaultOperationVisibility;
    private ProjectsTabOperator pto=null;
    private Node lastDiagramNode=null;
    private String lastTestCase=null;
    private static String workdir=System.getProperty("nbjunit.workdir");
    //
    private static String activityDiagramName0 = "acD";
    private static String workPkg0 = "pkg";
    private static long counter=0;
    //--
    private static String element1="Invocation";
    private static String elementName1="";
    private static ElementTypes elementType1=ElementTypes.INVOCATION;
    private static String menuItems1[]={"Edit|Lock Edit","Resize Element to Contents","Reset Edges",elementType1.toString()+"|Font",elementType1.toString()+"|Font Color",elementType1.toString()+"|Background Color",elementType1.toString()+"|Border Color"};
    //--
    private static String element2="Activity Group";
    private static String elementName2=defaultNewElementName;
    private static ElementTypes elementType2=ElementTypes.ACTIVITY_GROUP;
    private static String menuItems2[]={"Edit|Lock Edit","Resize Element to Contents","Reset Edges",elementType2.toString()+"|Font",elementType2.toString()+"|Font Color",elementType2.toString()+"|Background Color",elementType2.toString()+"|Border Color"};
    //--
    private static String element3="Initial Node";
    private static String elementName3="";
    private static ElementTypes elementType3=ElementTypes.INITIAL_NODE;
    private static String menuItems3[]={"Labels|Name","Edit|Lock Edit","Resize Element to Contents","Reset Edges",elementType3.toString()+"|Font",elementType3.toString()+"|Font Color",elementType3.toString()+"|Background Color",elementType3.toString()+"|Border Color"};
    //--
    private static String element4="Activity Final Node";
    private static String elementName4="";
    private static ElementTypes elementType4=ElementTypes.ACTIVITY_FINAL_NODE;
    private static String menuItems4[]={"Labels|Name","Edit|Lock Edit","Resize Element to Contents","Reset Edges",elementType4.toString()+"|Font",elementType4.toString()+"|Font Color",elementType4.toString()+"|Background Color",elementType4.toString()+"|Border Color"};
    //--
    private static String element5="Flow Final";
    private static String elementName5="";
    private static ElementTypes elementType5=ElementTypes.FLOW_FINAL;
    private static String menuItems5[]={"Labels|Name","Edit|Lock Edit","Resize Element to Contents","Reset Edges",elementType5.toString()+"|Font",elementType5.toString()+"|Font Color",elementType5.toString()+"|Background Color",elementType5.toString()+"|Border Color"};
    //--
    private static String element6="Decision";
    private static String elementName6="";
    private static ElementTypes elementType6=ElementTypes.DECISION;
    private static String menuItems6[]={"Labels|Name","Edit|Lock Edit","Resize Element to Contents","Reset Edges",elementType6.toString()+"|Font",elementType6.toString()+"|Font Color",elementType6.toString()+"|Background Color",elementType6.toString()+"|Border Color"};
    //--
    private static String element7="Vertical Fork";
    private static String elementName7="";
    private static ElementTypes elementType7=ElementTypes.VERTICAL_FORK;
    private static String menuItems7[]={"Labels|Name","Edit|Lock Edit","Resize Element to Contents","Reset Edges",elementType7.toString()+"|Font",elementType7.toString()+"|Font Color",elementType7.toString()+"|Background Color",elementType7.toString()+"|Border Color"};
    //--
    private static String element8="Horizontal Fork";
    private static String elementName8="";
    private static ElementTypes elementType8=ElementTypes.HORIZONTAL_FORK;
    private static String menuItems8[]={"Labels|Name","Edit|Lock Edit","Resize Element to Contents","Reset Edges",elementType8.toString()+"|Font",elementType8.toString()+"|Font Color",elementType8.toString()+"|Background Color",elementType8.toString()+"|Border Color"};
    //--
    private static String element9="Parameter Usage";
    private static String elementName9="";
    private static ElementTypes elementType9=ElementTypes.PARAMETER_USAGE;
    private static String menuItems9[]={"Edit|Lock Edit","Resize Element to Contents","Reset Edges",elementType9.toString()+"|Font",elementType9.toString()+"|Font Color",elementType9.toString()+"|Background Color",elementType9.toString()+"|Border Color"};
    //--
    private static String element10="Data Store";
    private static String elementName10="";
    private static ElementTypes elementType10=ElementTypes.DATA_STORE;
    private static String menuItems10[]={"Edit|Lock Edit","Resize Element to Contents","Reset Edges",elementType10.toString()+"|Font",elementType10.toString()+"|Font Color",elementType10.toString()+"|Background Color",elementType10.toString()+"|Border Color"};
    //--
    private static String element11="Signal";
    private static String elementName11="";
    private static ElementTypes elementType11=ElementTypes.SIGNAL;
    private static String menuItems11[]={"Edit|Lock Edit","Resize Element to Contents","Reset Edges",elementType11.toString()+"|Font",elementType11.toString()+"|Font Color",elementType11.toString()+"|Background Color",elementType11.toString()+"|Border Color"};
    //--
    private static String element12="Partition";
    private static String elementName12=defaultNewElementName;
    private static ElementTypes elementType12=ElementTypes.PARTITION;
    private static String menuItems12[]={"Partitions|Add Partition Column to the Right","Partitions|Add Partition Row to the Bottom","Edit|Lock Edit","Resize Element to Contents","Reset Edges",elementType12.toString()+"|Font",elementType12.toString()+"|Font Color",elementType12.toString()+"|Background Color",elementType12.toString()+"|Border Color"};
    //--
    private static String element13="Comment";
    private static String elementName13="";
    private static ElementTypes elementType13=ElementTypes.COMMENT;
    private static String menuItems13[]={"Compartment|Font","Compartment|Font Color","Edit|Lock Edit","Resize Element to Contents","Reset Edges",elementType13.toString()+"|Font",elementType13.toString()+"|Font Color",elementType13.toString()+"|Background Color",elementType13.toString()+"|Border Color"};
    //--
    private static String element14="Link Comment";
    private static String elementName14="";
    private static ElementTypes elementType14=ElementTypes.LINK_COMMENT;
    private static String menuItems14[]={"Compartment|Font","Compartment|Font Color","Edit|Lock Edit","Resize Element to Contents","Reset Edges",elementType14.toString()+"|Font",elementType14.toString()+"|Font Color",elementType14.toString()+"|Background Color",elementType14.toString()+"|Border Color"};
    //--
    private static String element15="Activity Edge";
    private static String elementName15="";
    private static LinkTypes elementType15=LinkTypes.ACTIVITY_EDGE;
    private static String menuItems15[]={"Labels|Show Guard Condition","Labels|Show Name","Labels|Reset Labels","Find|Source Element","Find|Target Element",elementType15.toString()+"|Border Color"};
    //--
    private static String element16="Dependency";
    private static String elementName16="";
    private static LinkTypes elementType16=LinkTypes.DEPENDENCY;
    private static String menuItems16[]={"Labels|Name","Labels|Reset Labels","Find|Source Element","Find|Target Element",elementType16.toString()+"|Border Color"};
    //--
    private static String element17="Realize";
    private static String elementName17="";
    private static LinkTypes elementType17=LinkTypes.REALIZE;
    private static String menuItems17[]={"Labels|Name","Labels|Stereotype","Labels|Reset Labels","Find|Source Element","Find|Target Element",elementType17.toString()+"|Border Color"};
    //--
    private static String element18="Usage";
    private static String elementName18="";
    private static LinkTypes elementType18=LinkTypes.USAGE;
    private static String menuItems18[]={"Labels|Name","Labels|Stereotype","Labels|Reset Labels","Find|Source Element","Find|Target Element",elementType18.toString()+"|Border Color"};
    //--
    private static String element19="Permission";
    private static String elementName19="";
    private static LinkTypes elementType19=LinkTypes.PERMISSION;
    private static String menuItems19[]={"Labels|Name","Labels|Stereotype","Labels|Reset Labels","Find|Source Element","Find|Target Element",elementType19.toString()+"|Border Color"};
    //--
    private static String element20="Abstraction";
    private static String elementName20="";
    private static LinkTypes elementType20=LinkTypes.ABSTRACTION;
    private static String menuItems20[]={"Labels|Name","Labels|Stereotype","Labels|Reset Labels","Find|Source Element","Find|Target Element",elementType20.toString()+"|Border Color"};
    //
    private static String element21="Link Comment";
    private static String elementName21="";
    private static LinkTypes elementType21=LinkTypes.COMMENT;
    private static String menuItems21[]={"Find|Source Element","Find|Target Element",elementType21.toString()+"|Border Color"};
    //common
    private static String commonMenuItems[]={"Edit|Copy","Edit|Cut","Edit|Delete","Edit|Paste","Edit|Select All","Edit|Invert Selection","Edit|Select All Similar Elements","Synchronize Element with Data","Select in Model","Properties","Associate With...","Apply Design Pattern..."};
    
    
    
    /** Need to be defined because of JUnit */
    public ActivityDiagramElementsContextMenu(String name) {
        super(name);
    }
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(org.netbeans.test.uml.activitydiagram.ActivityDiagramElementsContextMenu.class);
        return suite;
    }
    
    private DiagramOperator createDiagram(String project,String workPkg, String diagram){
        org.netbeans.test.umllib.Utils.RetAll rt=org.netbeans.test.umllib.Utils.createDiagram(project,workPkg,diagram,NewDiagramWizardOperator.ACTIVITY_DIAGRAM);
        pto = rt.pto;
        lastDiagramNode=rt.lastDiagramNode;
        return rt.dOp;
    }
    
    
    
    public void testContextMenu1() {
        testElementContext(element1,elementName1,elementType1,menuItems1);
    }
    public void testContextMenu2() {
        testElementContext(element2,elementName2,elementType2, menuItems2);
    }
    public void testContextMenu3() {
        testElementContext(element3,elementName3,elementType3, menuItems3);
    }
    public void testContextMenu4() {
        testElementContext(element4,elementName4,elementType4, menuItems4);
    }
    public void testContextMenu5() {
        testElementContext(element5,elementName5,elementType5, menuItems5);
    }
    public void testContextMenu6() {
        testElementContext(element6,elementName6,elementType6, menuItems6);
    }
    public void testContextMenu7() {
        testElementContext(element7,elementName7,elementType7, menuItems7);
    }
    public void testContextMenu8() {
        testElementContext(element8,elementName8,elementType8, menuItems8);
    }
    public void testContextMenu9() {
        testElementContext(element9,elementName9,elementType9, menuItems9);
    }
    public void testContextMenu10() {
        testElementContext(element10,elementName10,elementType10, menuItems10);
    }
    public void testContextMenu11() {
        testElementContext(element11,elementName11,elementType11, menuItems11);
    }
    public void testContextMenu12() {
        testElementContext(element12,elementName12,elementType12, menuItems12);
    }
    public void testContextMenu13() {
        testElementContext(element13,elementName13,elementType13, menuItems13);
    }
    public void testContextMenu14() {
        testElementContext(element14,elementName14,elementType14, menuItems14);
    }
    public void testContextMenu15() {
        testLinkContext(element15,elementName15,elementType15,menuItems15);
    }
    public void testContextMenu16() {
        testLinkContext(element16,elementName16,elementType16,menuItems16);
    }
    public void testContextMenu17() {
        testLinkContext(element17,elementName17,elementType17,menuItems17);
    }
    public void testContextMenu18() {
        testLinkContext(element18,elementName18,elementType18,menuItems18);
    }
    public void testContextMenu19() {
        testLinkContext(element19,elementName19,elementType19,menuItems19);
    }
    public void testContextMenu20() {
        testLinkContext(element20,elementName20,elementType20,menuItems20);
    }
    
    public void testLinkComment() {
        lastTestCase=getCurrentTestMethodName();
        log("LAST: "+lastTestCase);
        String [] customMenuItems=menuItems21;
        String element=element21;
        LinkTypes elementType=elementType21;
        //
        String workPkg=workPkg0+counter;
        String diagramName=activityDiagramName0+counter;
        counter++;
        String localElName1="El1";
        //
        DiagramOperator d = createDiagram(project,workPkg, diagramName);
        //
        UMLPaletteOperator pl=new UMLPaletteOperator();
        DrawingAreaOperator drAr=d.getDrawingArea();
        //
        java.awt.Point a=drAr.getFreePoint();
        DiagramElementOperator dE1=null,dE2=null;
        dE1=d.putElementOnDiagram(localElName1,elementType1,a.x,a.y);
        //
        pl.selectTool(element);
        //
        drAr.clickMouse(dE1.getCenterPoint().x,dE1.getCenterPoint().y,1);
        a=drAr.getFreePoint(150);
        drAr.clickMouse(a.x,a.y,1);
        dE2=new DiagramElementOperator(d,"",ElementTypes.LINK_COMMENT);
        //
        //drAr.pushKey(KeyEvent.VK_ESCAPE);
        d.toolbar().selectDefault();
        pl.waitSelection(element,false);
        new EventTool().waitNoEvent(500);
        //
        try{Thread.sleep(500);}catch(Exception ex){}
        a=drAr.getFreePoint(100);
        drAr.clickMouse(a.x,a.y,1,InputEvent.BUTTON3_MASK);
        drAr.pushKey(KeyEvent.VK_ESCAPE);
        //
        LinkOperator testedlink=LinkOperator.findLink(dE1, dE2,new LinkOperator.LinkByTypeChooser(elementType),0);
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
        assertTrue("Can't find "+elementType+" link between elements",testedlink!=null);
        drAr.clickMouse(testedlink.getNearCenterPoint().x,testedlink.getNearCenterPoint().y,1,InputEvent.BUTTON3_MASK);
        verify(customMenuItems);
    }
    
    
    
    
    public void setUp() {
        System.out.println("########  "+getName()+"  #######");
        pto = ProjectsTabOperator.invoke();
        if(!codeSync) {
            org.netbeans.test.uml.activitydiagram.utils.Utils.commonActivityDiagramSetup(workdir, prName);
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
            DiagramOperator d=new DiagramOperator("acD");
            d.closeAllDocuments();
            d.waitClosed();
            new EventTool().waitNoEvent(1000);
        }catch(Exception ex){};
        closeAllModal();
        //save
        org.netbeans.test.umllib.util.Utils.tearDown();
   }
    
    private void testElementContext(String element,String elementName,ElementTypes elementType,String[] customMenuItems) {
        lastTestCase=getCurrentTestNamesWithCheck()[1];
        log("LAST: "+lastTestCase);
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
        d.toolbar().selectDefault();
        //drAr.pushKey(KeyEvent.VK_ESCAPE);
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
        } catch(RuntimeException ex) {
            try {
                fail(element+" wasn't added to diagram, but object with type:"+new DiagramElementOperator(d,elementName).getType()+": and element type :"+new DiagramElementOperator(d,elementName).getElementType()+": was added whyle type should be :"+elementType+": was added");
            } catch(Exception ex2) {
                
            }
            throw ex;
        }
        dEl.select();
        //call popup
        drAr.clickMouse(dEl.getCenterPoint().x,dEl.getCenterPoint().y,1,InputEvent.BUTTON3_MASK);
        verify(customMenuItems);
    }
    private void testLinkContext(String element,String elementName,LinkTypes elementType,String [] customMenuItems) {
        lastTestCase=getCurrentTestNamesWithCheck()[1];
        log("LAST: "+lastTestCase);
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
        try {
            dE1=d.putElementOnDiagram(localElName1,elementType1,a.x,a.y);
            a=drAr.getFreePoint(150);
            dE2=d.putElementOnDiagram(localElName2,elementType1,a.x,a.y);
        } catch(Exception ex) {
            
        }
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
        //drAr.pushKey(KeyEvent.VK_ESCAPE);
        d.toolbar().selectDefault();
        pl.waitSelection(element,false);
        new EventTool().waitNoEvent(500);
        //
        try{Thread.sleep(500);}catch(Exception ex){}
        a=drAr.getFreePoint(100);
        drAr.clickMouse(a.x,a.y,1,InputEvent.BUTTON3_MASK);
        drAr.pushKey(KeyEvent.VK_ESCAPE);
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
            try {
                if(pop.showMenuItem(commonMenuItems[i])==null) {
                    fails+="Null item "+commonMenuItems[i]+";\n";
                    org.netbeans.test.umllib.util.Utils.makeScreenShot();
                }
            } catch(Exception ex) {
                fails+="Timeout on selection of "+commonMenuItems[i]+";\n";
                org.netbeans.test.umllib.util.Utils.makeScreenShot();
            }
            //returns back from inner popup
            pop.pushKey(KeyEvent.VK_LEFT);
        }
        if(add!=null)for(int i=0;i<add.length;i++) {
            try {
                if(pop.showMenuItem(add[i])==null) {
                    fails+="Null item "+add[i]+";\n";
                    org.netbeans.test.umllib.util.Utils.makeScreenShot(lastTestCase);
                }
            } catch(Exception ex) {
                fails+="Timeout on selection of "+add[i]+";\n";
                org.netbeans.test.umllib.util.Utils.makeScreenShot(lastTestCase);
            }
            //returns back from inner popup
            pop.pushKey(KeyEvent.VK_LEFT);
        }
        //
        assertTrue("There are some problems with context menu: "+fails,fails.length()==0);
        
    }
    
}
