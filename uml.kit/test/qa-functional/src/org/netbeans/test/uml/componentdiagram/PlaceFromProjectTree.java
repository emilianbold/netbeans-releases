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
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTreeOperator;


import org.netbeans.junit.NbTestSuite;
//import qa.uml.UMLClassOperator;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.DrawingAreaOperator;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.UMLPaletteOperator;
import org.netbeans.test.umllib.exceptions.NotFoundException;
import org.netbeans.test.umllib.testcases.UMLTestCase;


/**
 *
 * @author psb
 * @spec UML/ComponentDiagram.xml
 */
public class PlaceFromProjectTree extends UMLTestCase {
    
    //some system properties
    private static String contextPropItemName="Properties";
    private static String umlPropertyWindowTitle="Project Properties";
    private static String umlSourcePackagesLabel="Source Packages";
    private static String umlSourcePackagesColumn="Folder Label";
    private static String umlSourceUsageColumn="Model?";
    private static String mainTreeTabName="Projects";
    //common test properties
    private static String prName= "ComponentDiagramProjectFT";
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
    private JTreeOperator prTree=null;
    private static String workdir=System.getProperty("nbjunit.workdir");
    private String lastTestCase=null;
    //--
    private static String diagramName1 = "cpD1";
    private static String workPkg1 = "pkg1";
    private static String element1="Component";
    private static String elementName1=defaultNewElementName;
    private static ElementTypes elementType1=ElementTypes.COMPONENT;
    //--
    private static String diagramName2 = "cpD2";
    private static String workPkg2 = "pkg2";
    private static String element2="Class";
    private static String elementName2=defaultNewElementName;
    private static ElementTypes elementType2=ElementTypes.CLASS;
    //--
    private static String diagramName3 = "cpD3";
    private static String workPkg3 = "pkg3";
    private static String element3="Interface";
    private static String elementName3=defaultNewElementName;
    private static ElementTypes elementType3=ElementTypes.INTERFACE;
    //--
    private static String diagramName4 = "cpD4";
    private static String workPkg4 = "pkg4";
    private static String element4="Package";
    private static String elementName4=defaultNewElementName;
    private static ElementTypes elementType4=ElementTypes.PACKAGE;
    //--
    private static String diagramName5 = "cpD5";
    private static String workPkg5 = "pkg5";
    private static String element5="Artifact";
    private static String elementName5=defaultNewElementName;
    private static ElementTypes elementType5=ElementTypes.ARTIFACT;
    //--
    private static String diagramName6 = "cpD6";
    private static String workPkg6 = "pkg6";
    private static String element6="Assembly Connector";
    private static String elementName6=defaultNewElementName;
    private static ElementTypes elementType6=ElementTypes.INTERFACE;
    //--
    private static String diagramName7 = "cpD7";
    private static String workPkg7 = "pkg7";
    private static String element7="Template Class";
    private static String elementName7=defaultNewElementName;
    private static ElementTypes elementType7=ElementTypes.TEMPLATE_CLASS;
    //--
    private static String diagramName8 = "cpD8";
    private static String workPkg8 = "pkg8";
    private static String element8="Derivation Classifier";
    private static String elementName8=defaultNewElementName;
    private static ElementTypes elementType8=ElementTypes.DERIVATION_CLASSIFIER;
    //--
    private static String diagramName9 = "cpD9";
    private static String workPkg9 = "pkg9";
    private static String element9="Design Pattern";
    private static String elementName9=defaultNewElementName;
    private static ElementTypes elementType9=ElementTypes.DESIGN_PATTERN;
    //--
    private static String diagramName10 = "cpD10";
    private static String workPkg10 = "pkg10";
    private static String element10="Role";
    private static String elementName10=defaultNewElementName;
    private static ElementTypes elementType10=ElementTypes.ROLE;
    //--
    private static String diagramName11 = "cpD11";
    private static String workPkg11 = "pkg11";
    private static String element11="Interface Role";
    private static String elementName11=defaultNewElementName;
    private static ElementTypes elementType11=ElementTypes.INTERFACE_ROLE;
    //--
    private static String diagramName12 = "cpD12";
    private static String workPkg12 = "pkg12";
    private static String element12="Actor Role";
    private static String elementName12=defaultNewElementName;
    private static ElementTypes elementType12=ElementTypes.ACTOR_ROLE;
    //--
    private static String diagramName13 = "cpD13";
    private static String workPkg13 = "pkg13";
    private static String element13="Class Role";
    private static String elementName13=defaultNewElementName;
    private static ElementTypes elementType13=ElementTypes.CLASS_ROLE;
    //--
    private static String diagramName14 = "cpD14";
    private static String workPkg14 = "pkg14";
    private static String element14="Use Case Role";
    private static String elementName14=defaultNewElementName;
    private static ElementTypes elementType14=ElementTypes.USE_CASE_ROLE;
    //--
    private static String diagramName15 = "cpD15";
    private static String workPkg15 = "pkg15";
    private static String element15="Role Binding";
    private static String elementName15=defaultNewElementName;
    private static ElementTypes elementType15=ElementTypes.ROLE_BINDING;
    //--
    private static String diagramName16 = "cpD16";
    private static String workPkg16 = "pkg16";
    private static String element16="Comment";
    private static String elementName16="";
    private static ElementTypes elementType16=ElementTypes.COMMENT;
    //--
    private static String diagramName17 = "cpD17";
    private static String workPkg17 = "pkg17";
    private static String element17="Link Comment";
    private static String elementName17="";
    private static ElementTypes elementType17=ElementTypes.LINK_COMMENT;
    //--
   /* private static String diagramName15 = "cpD15";
    private static String workPkg15 = "pkg15";
    private static String element15="Activity Edge";
    private static String treeNode15=element15;
    private static String elementName15="";
    private static LinkTypes elementType15=LinkTypes.ACTIVITY_EDGE;
    private static boolean eIO15=true;
    //--
    private static String diagramName16 = "cpD16";
    private static String workPkg16 = "pkg16";
    private static String element16="Dependency";
    private static String treeNode16=element16;
    private static String elementName16="";
    private static LinkTypes elementType16=LinkTypes.DEPENDENCY;
    private static boolean eIO16=false;
    //--
    private static String diagramName17 = "cpD17";
    private static String workPkg17 = "pkg17";
    private static String element17="Realize";
    private static String treeNode17="Realization";
    private static String elementName17="";
    private static LinkTypes elementType17=LinkTypes.REALIZE;
    private static boolean eIO17=false;
    //--
    private static String diagramName18 = "cpD18";
    private static String workPkg18 = "pkg18";
    private static String element18="Usage";
    private static String treeNode18=element18;
    private static String elementName18="";
    private static LinkTypes elementType18=LinkTypes.USAGE;
    private static boolean eIO18=false;
    //--
    private static String diagramName19 = "cpD19";
    private static String workPkg19 = "pkg19";
    private static String element19="Permission";
    private static String treeNode19=element19;
    private static String elementName19="";
    private static LinkTypes elementType19=LinkTypes.PERMISSION;
    private static boolean eIO19=false;
    //--
    private static String diagramName20 = "cpD20";
    private static String workPkg20 = "pkg20";
    private static String element20="Abstraction";
    private static String treeNode20=element20;
    private static String elementName20="";
    private static LinkTypes elementType20=LinkTypes.ABSTRACTION;
    private static boolean eIO20=false;*/

    
    /** Need to be defined because of JUnit */
    public PlaceFromProjectTree(String name) {
        super(name);
    }
     public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(org.netbeans.test.uml.componentdiagram.PlaceFromProjectTree.class);
        return suite;
    }
    
    private DiagramOperator createOrOpenDiagram(String project,String workPkg, String diagram) {
        org.netbeans.test.umllib.Utils.RetAll rt=org.netbeans.test.umllib.Utils.createOrOpenDiagram(project,workPkg,diagram,org.netbeans.test.umllib.NewDiagramWizardOperator.COMPONENT_DIAGRAM);
        pto = rt.pto;
        prTree=new JTreeOperator(pto);
        lastDiagramNode=rt.lastDiagramNode;
        return rt.dOp;
    }
    
    

    public void testPlaceFromTree1() {
        lastTestCase=getCurrentTestMethodName();
        testElementPlaceFromTree(workPkg1,diagramName1,element1,elementName1,elementType1);
    }
    public void testPlaceFromTree2() {
        lastTestCase=getCurrentTestMethodName();;
        testElementPlaceFromTree(workPkg2,diagramName2,element2,elementName2,elementType2);
    }
    public void testPlaceFromTree3() {
         lastTestCase=getCurrentTestMethodName();;
       testElementPlaceFromTree(workPkg3,diagramName3,element3,elementName3,elementType3);
    }
    public void testPlaceFromTree4() {
        lastTestCase=getCurrentTestMethodName();;
        testElementPlaceFromTree(workPkg4,diagramName4,element4,elementName4,elementType4);
    }
    public void testPlaceFromTree5() {
        lastTestCase=getCurrentTestMethodName();;
        testElementPlaceFromTree(workPkg5,diagramName5,element5,elementName5,elementType5);
    }
    public void testPlaceFromTree6() {
        lastTestCase=getCurrentTestMethodName();
        testElementPlaceFromTree(workPkg6,diagramName6,element6,elementName6,elementType6);
    }
    public void testPlaceFromTree7() {
        lastTestCase=getCurrentTestMethodName();
        testElementPlaceFromTree(workPkg7,diagramName7,element7,elementName7,elementType7);
    }
    public void testPlaceFromTree8() {
        lastTestCase=getCurrentTestMethodName();
        testElementPlaceFromTree(workPkg8,diagramName8,element8,elementName8,elementType8);
    }
    public void testPlaceFromTree9() {
        lastTestCase=getCurrentTestMethodName();
        testElementPlaceFromTree(workPkg9,diagramName9,element9,elementName9,elementType9);
    }
    public void testPlaceFromTree10() {
        lastTestCase=getCurrentTestMethodName();
        testElementPlaceFromTree(workPkg10,diagramName10,element10,elementName10,elementType10);
    }
   public void testPlaceFromTree11() {
        lastTestCase=getCurrentTestMethodName();
        testElementPlaceFromTree(workPkg11,diagramName11,element11,elementName11,elementType11);
    }
   public void testPlaceFromTree12() {
        lastTestCase=getCurrentTestMethodName();
        testElementPlaceFromTree(workPkg12,diagramName12,element12,elementName12,elementType12);
    }
   public void testPlaceFromTree13() {
        lastTestCase=getCurrentTestMethodName();
        testElementPlaceFromTree(workPkg13,diagramName13,element13,elementName13,elementType13);
    }
   public void testPlaceFromTree14() {
        lastTestCase=getCurrentTestMethodName();
        testElementPlaceFromTree(workPkg14,diagramName14,element14,elementName14,elementType14);
    }
   public void testPlaceFromTree15() {
        lastTestCase=getCurrentTestMethodName();
        testElementPlaceFromTree(workPkg15,diagramName15,element15,elementName15,elementType15);
    }
   public void testPlaceFromTree16() {
        lastTestCase=getCurrentTestMethodName();
        testElementPlaceFromTree(workPkg16,diagramName16,element16,elementName16,elementType16);
    }
   public void testPlaceFromTree17() {
        lastTestCase=getCurrentTestMethodName();
        testElementPlaceFromTree(workPkg17,diagramName17,element17,elementName17,elementType17);
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
    
   private void testElementPlaceAndName(String workPkg,String diagramName,String element,String elementName,ElementTypes elementType,String newName)
   {
        DiagramOperator d = createOrOpenDiagram(project,workPkg, diagramName);
        //
        int numChild=new Node(prTree,lastDiagramNode.getParentPath()).getChildren().length;
        //
        UMLPaletteOperator pl=new UMLPaletteOperator();
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
        new EventTool().waitNoEvent(500);
        //
        try
        {
            DiagramElementOperator dEl=new DiagramElementOperator(d,elementName,elementType,0);
        }
        catch(Exception ex)
        {
            try
            {
                fail(element+" wasn't added to diagram, but object with type:"+new DiagramElementOperator(d,elementName).getType()+": and element type :"+new DiagramElementOperator(d,elementName).getElementType()+": was added whyle type should be :"+elementType+": was added");
            }
            catch(Exception ex2)
            {
                
            }
            fail(element+" wasn't added to iagram.");
        }
       //
        new EventTool().waitNoEvent(500);
        PropertySheetOperator ps=new PropertySheetOperator();
           if(elementName.equals(""))
        {
            assertTrue("Property sheet isn't for "+elementType+", but for "+ps.getName(),(elementType+" - Properties").equals(ps.getName()));
        }
        else
        {
            assertTrue("Property sheet isn't for "+elementType+" with "+elementName+" name, but for "+ps.getName(),(elementName+" - Properties").equals(ps.getName()));
        }
        //
        int numChild2=new Node(prTree,lastDiagramNode.getParentPath()).getChildren().length;
        
        org.netbeans.test.uml.componentdiagram.utils.Utils.setTextProperty("Name",newName);
        //
        new EventTool().waitNoEvent(500);
        //
        try
        {
            DiagramElementOperator dEl=new DiagramElementOperator(d,newName,elementType,0);
        }
        catch(Exception ex)
        {
             fail("Can't find "+element+" with name \""+newName+"\" on diagram");
        }
        //
        assertTrue("No name propagated to node within diagram (in project tree)",new Node(prTree,lastDiagramNode.getParentPath()).isChildPresent(newName));
   }
   private void testElementPlaceAndName(String workPkg,String diagramName,String element,String elementName,ElementTypes elementType)
   {
       testElementPlaceAndName(workPkg,diagramName,element,elementName,elementType,"ElName");
   }
   //
   private void testElementPlaceFromTree(String workPkg,String diagramName,String element,String elementName,ElementTypes elementType,String newName)
   {
       testElementPlaceAndName(workPkg,diagramName+"_first",element,elementName,elementType,newName);
       Node de=new Node(prTree,lastDiagramNode.getParentPath()+"|"+newName);
        try{
            DiagramOperator dClose=new DiagramOperator("cpD");
            DrawingAreaOperator drAr=dClose.getDrawingArea();
            drAr.pushKey(KeyEvent.VK_ESCAPE);
            java.awt.Point a=drAr.getFreePoint();
            drAr.clickMouse(a.x,a.y,1,InputEvent.BUTTON3_MASK);
            drAr.pushKey(KeyEvent.VK_ESCAPE);
            new Thread(new Runnable() {
                public void run() {
                    new JButtonOperator(new JDialogOperator("Save"),"Save").push();
                }
            }).start();
           dClose.closeAllDocuments();
        }catch(Exception ex){};
        DiagramOperator d = createOrOpenDiagram(project,workPkg, diagramName+"_second");
        //
        int numChild=new Node(prTree,lastDiagramNode.getParentPath()).getChildren().length;
        d.createGenericElementOnDiagram(de);
        new EventTool().waitNoEvent(500);
        //
        try
        {
            DiagramElementOperator dEl=new DiagramElementOperator(d,newName,elementType,0);
        }
        catch(Exception ex)
        {
            try
            {
                fail(element+" wasn't added to diagram, but object with type:"+new DiagramElementOperator(d,newName).getType()+": and element type :"+new DiagramElementOperator(d,newName).getElementType()+": was added whyle type should be :"+elementType+": was added");
            }
            catch(Exception ex2)
            {
                
            }
            fail(element+" wasn't added to iagram.");
        }
       //
        new EventTool().waitNoEvent(500);
        //
        int numChild2=new Node(prTree,lastDiagramNode.getParentPath()).getChildren().length;
        assertTrue("No new nodes should be added to package",(numChild2-numChild)==0);
    }
   private void testElementPlaceFromTree(String workPkg,String diagramName,String element,String elementName,ElementTypes elementType)
   {
       testElementPlaceFromTree(workPkg,diagramName,element,elementName,elementType,"nameEl");
   }
}
