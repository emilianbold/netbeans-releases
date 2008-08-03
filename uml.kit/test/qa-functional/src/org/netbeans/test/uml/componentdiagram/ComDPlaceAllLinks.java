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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTreeOperator;


import org.netbeans.junit.NbTestSuite;

import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.DrawingAreaOperator;
import org.netbeans.test.umllib.ExpandedElementTypes;
import org.netbeans.test.umllib.LinkOperator;
import org.netbeans.test.umllib.LinkTypes;
import org.netbeans.test.umllib.UMLPaletteOperator;
import org.netbeans.test.umllib.exceptions.NotFoundException;
import org.netbeans.test.umllib.testcases.UMLMultiTestCase;
import org.netbeans.test.umllib.testcases.UMLMultiTestSuite;
import org.netbeans.test.umllib.testcases.UMLTestCase;
import org.netbeans.test.umllib.util.LibProperties;




/**
 *
 * @author psb
 */
public class ComDPlaceAllLinks extends UMLMultiTestSuite {
    
    private static String[] Test_Cases={"PlaceLinks"};
    
    private static boolean makeScreen=false;
    
    protected UMLMultiTestCase[] cases(){
        return new UMLMultiTestCase[]{new PlaceLinks()};
    } 
    
    //common test properties
    private static String prName= "componentDiagramProjectLinks";
    private static String lastPrName=null;
    private static String project = prName+"|Model";
    private static String lastProject = lastPrName+"|Model";
    private static String sourceProject = "source";
    private static boolean codeSync=false;
    private ProjectsTabOperator pto=null;
    private Node lastDiagramNode=null;
    private JTreeOperator prTree=null;
    private static String workdir=System.getProperty("nbjunit.workdir");
    private String lastTestCase=null;
    //
    private static PlaceLinks testClass=null;
    //--
    private static long testCounter=0;
    private static String componentDiagramName="cpD";
    private static String workPkg = "pkg";
    private static LinkTypes[] allLinkTypes={
                                                LinkTypes.GENERALIZATION,
                                                LinkTypes.IMPLEMENTATION,
                                                LinkTypes.NESTED_LINK,
                                                LinkTypes.DELEGATE,
                                                LinkTypes.USAGE,
                                                LinkTypes.DERIVATION_EDGE,
                                                LinkTypes.ASSOCIATION,        
                                                LinkTypes.COMPOSITION,        
                                                LinkTypes.NAVIGABLE_COMPOSITION,        
                                                LinkTypes.AGGREGATION,        
                                                LinkTypes.NAVIGABLE_AGGREGATION,        
                                                LinkTypes.NAVIGABLE_ASSOCIATION,        
                                                LinkTypes.ASSOCIATION_CLASS,
                                                LinkTypes.ROLE_BINDING,
                                                LinkTypes.COMMENT
                                            };
    private static ExpandedElementTypes[] allElementTypes={
                                                        ExpandedElementTypes.COMPONENT,
                                                        ExpandedElementTypes.CLASS,
                                                        ExpandedElementTypes.INTERFACE,        
                                                        ExpandedElementTypes.PACKAGE,        
                                                        ExpandedElementTypes.ARTIFACT,        
                                                        ExpandedElementTypes.ASSEMBLY_CONNECTOR,        
                                                        ExpandedElementTypes.TEMPLATE_CLASS,       
                                                        ExpandedElementTypes.DERIVATION_CLASSIFIER,        
                                                        ExpandedElementTypes.DESIGN_PATTERN,        
                                                        ExpandedElementTypes.ROLE,        
                                                        ExpandedElementTypes.INTERFACE_ROLE,        
                                                        ExpandedElementTypes.ACTOR_ROLE,        
                                                        ExpandedElementTypes.CLASS_ROLE,        
                                                        ExpandedElementTypes.USE_CASE_ROLE,        
                                                        ExpandedElementTypes.ROLE_BINDING,        
                                                        ExpandedElementTypes.COMMENT,        
                                                        ExpandedElementTypes.LINK_COMMENT        
                                                  };
    
     

     /** Need to be defined because of JUnit */
    public ComDPlaceAllLinks(String name) {
        super(name);
    }
     public static NbTestSuite suite() {
        return new ComDPlaceAllLinks("PlaceLinks");
    }
    
     ////////////////////////
     public void setUp0() {
            makeScreen=true;
            //
            File store=new File(workdir+"/user/org.netbeans.test.uml.componentdiagram.ComDPlaceAllLinks$PlaceLinks/store.txt");
            if(store.exists())
            {
                try {
                    FileReader read=new FileReader(store);
                } catch (FileNotFoundException ex) {
                    ex.printStackTrace();
                }
            }
            else
            {
                try {
                    store.createNewFile();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            //
            int testNum=l+t*allLinkTypes.length+f*allLinkTypes.length*allElementTypes.length;
            testCounter=testNum;
            if(((testCounter-1)%15)==0)
            {
                lastPrName=prName+testNum;
                lastProject = lastPrName+"|Model";
                org.netbeans.test.uml.componentdiagram.utils.Utils.commonComponentDiagramSetup(workdir,lastPrName);
            }
        }
        public void tearDown0(PlaceLinks tc) 
        {
            if(makeScreen)org.netbeans.test.umllib.util.Utils.makeScreenShot("org.netbeans.test.uml.componentdiagram.ComDPlaceAllLinks$PlaceLinks",this.getName());
            //tc.closeAllModal();
            long tmp=JemmyProperties.getCurrentTimeout("DialogWaiter.WaitDialogTimeout");
            long tmp2=JemmyProperties.getCurrentTimeout("ComponentOperator.WaitComponentTimeout");
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 1000); 
            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 1000);
            org.netbeans.test.umllib.util.Utils.saveAll();
            try{
                DiagramOperator d=new DiagramOperator(componentDiagramName);
                DrawingAreaOperator drAr=d.getDrawingArea();
                drAr.pushKey(KeyEvent.VK_ESCAPE);
                java.awt.Point a=drAr.getFreePoint();
                drAr.clickMouse(a.x,a.y,1,InputEvent.BUTTON3_MASK);
                drAr.pushKey(KeyEvent.VK_ESCAPE);
               new Thread(new Runnable() {
                    public void run() {
                        try{Thread.sleep(1000);}catch(Exception ex){}
                        new JButtonOperator(new JDialogOperator("Save Diagram"),"Discard").push();
                    }
                }).start();
                d.closeAllDocuments();
            }catch(Exception ex){};
            //closeAllModal();
            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", tmp2);
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", tmp);
            //
            if(lastDiagramNode!=null)
            {
                Node pkgN=new Node(prTree,lastDiagramNode.getParentPath());
                pkgN.performPopupActionNoBlock("Delete");
                JDialogOperator dlg=new JDialogOperator("Confirm Object Deletion");
                new JButtonOperator(dlg,"Yes").pushNoBlock();
                dlg.waitClosed();
                //dlg=new JDialogOperator("Deleting a Package");
                //new JButtonOperator(dlg,"Yes").pushNoBlock();
                //dlg.waitClosed();
                pkgN.waitNotPresent();
            }
            //
            if((testCounter%15)==0)
            {
                Node root=new Node(prTree,lastPrName);
                root.performPopupAction("Close");
                try{Thread.sleep(1000);}catch(Exception ex){}
                System.gc();
                try{Thread.sleep(15000);}catch(Exception ex){}
            }
            else
            {
                try{Thread.sleep(1000);}catch(Exception ex){}
                System.gc();
                try{Thread.sleep(1000);}catch(Exception ex){}
            }
            //
            //tc.closeAllModal();
        }

     ////////////////////////
    //
    private int f=0,t=0,l=0;
    public class PlaceLinks extends UMLMultiTestCase
    {
        ExpandedElementTypes elFrom;
        ExpandedElementTypes elTo;
        LinkTypes lnk;
        PlaceLinks()
        {
            
        }
        private void setParams(ExpandedElementTypes elFrom,ExpandedElementTypes elTo,LinkTypes lnk)
        {
            this.elFrom=elFrom;this.elTo=elTo;this.lnk=lnk;
        }
        public void prepare()
        {
            makeScreen=true;
            testClass=new PlaceLinks();
            //org.netbeans.test.uml.componentdiagram.utils.Utils.commonComponentDiagramSetup(workdir,prName);
        }
        //
        public void setUp() {
            setUp0();
        }
        public UMLMultiTestCase create()
        {
            if(f<allElementTypes.length && t<allElementTypes.length && l<allLinkTypes.length)
            {
                testClass.setParams(allElementTypes[f],allElementTypes[t], allLinkTypes[l]);
                testClass.setName("test"+allLinkTypes[l]+"LinkOn"+allElementTypes[f]+"To"+allElementTypes[t]);
            }
            else testClass=null;
            l++;
            if(l>=allLinkTypes.length){l=0;t++;}
            if(t>=allElementTypes.length){t=0;f++;}
            //
            return testClass;
        }
        //
        public void execute()
        {
            int testNum=l+t*allLinkTypes.length+f*allLinkTypes.length*allElementTypes.length;
            testLinkPlace(this, workPkg+testNum, componentDiagramName+testNum, elFrom,elTo,lnk, lnk.toString(), false);
            makeScreen=false;
        }
        //
        public void tearDown() 
        {
            tearDown0(this);
        }
        //
        public void cleanup()
        {
              org.netbeans.test.umllib.util.Utils.tearDown();
        }
         
     }
    
     
    private DiagramOperator createOrOpenDiagram(UMLTestCase tc,String project,String workPkg, String diagram) 
    {
        org.netbeans.test.umllib.Utils.RetAll rt=org.netbeans.test.umllib.Utils.createOrOpenDiagram(project,workPkg,diagram,org.netbeans.test.umllib.NewDiagramWizardOperator.COMPONENT_DIAGRAM);
        pto = rt.pto;
        prTree=new JTreeOperator(pto);
        lastDiagramNode=rt.lastDiagramNode;
        return rt.dOp;
    }


    
    

    
   private void testLinkPlace(UMLTestCase tc,String workPkg,String diagramName,ExpandedElementTypes fromType,ExpandedElementTypes toType,LinkTypes lnkType,String treeNode,boolean useIO)
   {
        String localElName1="El1";
        String localElName2="El2";
        String newName="LinkName";
        //workaround for nested linkd
        if(lnkType.equals(LinkTypes.NESTED_LINK))lnkType=LinkTypes.NESTED_LINK.NESTED_LINK(toType.toString());
        //
        String linkInPalette=LibProperties.getCurrentToolName(lnkType);
        String defaultLinkName= LibProperties.getCurrentDefaultName(lnkType);
        //
        DiagramOperator d = createOrOpenDiagram(tc,lastProject,workPkg, diagramName);
        //
        Node pkgNode=new Node(prTree,lastDiagramNode.getParentPath());
        int numChild=pkgNode.getChildren().length;
        //
        UMLPaletteOperator pl=new UMLPaletteOperator();
        DrawingAreaOperator drAr=d.getDrawingArea();
        //
        java.awt.Point a=drAr.getFreePoint();
        DiagramElementOperator dE1=null,dE2=null;
        try
        {
            dE1=d.putElementOnDiagram(localElName1,fromType,a.x,a.y);
            a=drAr.getFreePoint(200);
            dE2=d.putElementOnDiagram(localElName2,toType,a.x,a.y);
        }
        catch(Exception ex)
        {
            tc.fail("Element placement error: "+ex);
        }
        //
        try {
            pl.selectTool(linkInPalette);
        } catch(NotFoundException ex) {
            tc.fail("BLOCKING: Can't find '"+linkInPalette+"' in paletter");
        }
        //
        drAr.clickMouse(dE1.getCenterPoint().x,dE1.getCenterPoint().y,1);
        drAr.clickMouse(dE2.getCenterPoint().x,dE2.getCenterPoint().y,1);
        //
        new EventTool().waitNoEvent(2000);
        //
        LinkOperator testedlink=null;
        LinkOperator altLink=null;
        Timeouts t = JemmyProperties.getCurrentTimeouts();
        long waitLinkOld=t.getTimeout("LinkOperator.WaitLinkTime");
        t.setTimeout("LinkOperator.WaitLinkTime",5000);
        try
        {
            testedlink=LinkOperator.waitForLink(dE1, dE2,new LinkOperator.LinkByTypeChooser(lnkType),0);
        }
        catch(Exception ex)
        {
            //tc.fail(linkInPalette+" of type "+lnkType+" wasn't added to diagram:"+ex);
        }
        if(testedlink==null)
        {
            try
            {
                altLink=LinkOperator.waitForLink(dE1, dE2,new LinkOperator.LinkByTypeChooser(LinkTypes.ANY),0);
            }
            catch(Exception ex)
            {
                //tc.fail("any link find failed.");
            }
        }
        t.setTimeout("LinkOperator.WaitLinkTime",waitLinkOld);
        boolean shouldBe=true;
        //***
        if(lnkType.equals(LinkTypes.DERIVATION_EDGE) && !(fromType.equals(ExpandedElementTypes.DERIVATION_CLASSIFIER) && toType.equals(ExpandedElementTypes.COMPONENT)))shouldBe=false;
        else if(lnkType.equals(LinkTypes.ROLE_BINDING) && (!(fromType.equals(ExpandedElementTypes.DESIGN_PATTERN) && toType.name().indexOf("ROLE")>-1) || !(toType.equals(ExpandedElementTypes.DESIGN_PATTERN) && fromType.name().indexOf("ROLE")>-1)))shouldBe=false;
        //###
        //next steps are appropriate only if link should exists
        if(!shouldBe)
        {
            if(testedlink!=null)tc.fail("There is "+lnkType+" link between elements.");
            else if(altLink!=null)tc.fail("There is "+altLink+" link between elements.");
        }
        else
        {
            if(testedlink==null && altLink!=null)tc.fail("Can't find "+lnkType+" link between elements, but the is "+altLink.getType()+" link.");
            else if(testedlink==null)tc.fail("There is no link between elements.");
            PropertySheetOperator ps=null;
            String propTitlePrefix=lnkType.toString();
            //***
            if(lnkType.equals(LinkTypes.NESTED_LINK))propTitlePrefix=localElName2;
            //###
            try
            {
                ps=new PropertySheetOperator(propTitlePrefix+" - Properties");
            }
            catch(org.netbeans.jemmy.TimeoutExpiredException ex)
            {
                ps=new PropertySheetOperator();
                throw ex;
            }
            org.netbeans.test.uml.componentdiagram.utils.Utils.setTextProperty("Name", newName);
            new EventTool().waitNoEvent(500);
            //check link after renaming
            testedlink=null;altLink=null;
            try
            {
                testedlink=LinkOperator.findLink(dE1, dE2,new LinkOperator.LinkByTypeChooser(lnkType),0);
            }
            catch(Exception ex)
            {
                //tc.fail(linkInPalette+" of type "+lnkType+" wasn't added to diagram:"+ex);
            }
            if(testedlink==null)
            {
                try
                {
                    altLink=LinkOperator.findLink(dE1, dE2,new LinkOperator.LinkByTypeChooser(LinkTypes.ANY),0);
                }
                catch(Exception ex)
                {
                    //tc.fail("any link find failed.");
                }
            }
            if(testedlink==null && altLink!=null)tc.fail("Can't find "+lnkType+" link between elements after renaming, but the is "+altLink.getType()+" link.");
            else if(testedlink==null)tc.fail("There is no link between elements after renaming.");
            //***
            if(!LinkTypes.DERIVATION_EDGE.equals(lnkType)) tc.assertTrue("Link name isn't correct: "+testedlink.getName()+", should be: "+newName,newName.equals(testedlink.getName()));
            //###
            //***
            if(lnkType.equals(LinkTypes.NESTED_LINK))
            {
                Node n2=new Node(pkgNode,localElName2);
                Node n1=new Node(n2,localElName1);
           }
            else if(lnkType.equals(LinkTypes.ROLE_BINDING))
            {
                
            }
            else
            {
                Node n1=new Node(pkgNode,localElName1);
                Node n2=new Node(pkgNode,localElName2);
                Node rel1=new Node(n1,"Relationships");
                Node rel2=new Node(n2,"Relationships");
                new EventTool().waitNoEvent(500);
                if(useIO)
                {
                    Node nI=new Node(rel1,"Outgoing Edges");
                    Node nO=new Node(rel2,"Incoming Edges");
                    tc.assertTrue("There should be only one node within Outgoing Edges node",nO.getChildren().length==1);
                    tc.assertTrue("There should be only one node within Incoming Edges node",nI.getChildren().length==1);
                    tc.assertTrue("There should be "+newName+" node within Outgoing Edges node",nO.getChildren()[0].equals(newName));
                    tc.assertTrue("There should be "+newName+" node within Incoming Edges node",nI.getChildren()[0].equals(newName));
                    Node nMO=new Node(nO,newName.toString());
                    Node nMI=new Node(nI,newName.toString());
                    new EventTool().waitNoEvent(500);
                    tc.assertTrue("There should be only one node within "+newName+" node within Outgoing Edges node",nMO.getChildren().length==1);
                    tc.assertTrue("There should be only one node within "+newName+" node within Incoming Edges node",nMI.getChildren().length==1);
                    tc.assertTrue("There should be "+localElName2+" node within flow node within Outgoing Edges node",nMO.isChildPresent(localElName2));
                    tc.assertTrue("There should be "+localElName1+" node within flow node within Incoming Edges node",nMI.isChildPresent(localElName1));
                }
                else
                {
                    tc.assertTrue("There should be "+newName+" node within Relationships node of "+localElName1,rel1.getChildren()[0].equals(newName));
                    tc.assertTrue("There should be "+newName+" node within Relationships node of "+localElName2,rel2.getChildren()[0].equals(newName));
                    Node rn1=new Node(rel1,newName);
                    Node rn2=new Node(rel2,newName);
                    new EventTool().waitNoEvent(500);
                    if(LinkTypes.DELEGATE.equals(lnkType))
                    {
                        tc.assertTrue(86374,"there is no nodes within delegate node", rn1.getChildren().length>0);
                        tc.assertTrue(86374,"there is no nodes within delegate node", rn2.getChildren().length>0);
                    }
                    tc.assertTrue("There should be only one node within "+newName+" node, now: "+rn1.getChildren().length,rn1.getChildren().length==1);
                    tc.assertTrue("There should be only one node within "+newName+" node, now: "+rn2.getChildren().length,rn2.getChildren().length==1);
                    Node de1=new Node(rn1,localElName2);
                    Node de2=new Node(rn2,localElName1);
                }
            }
            //###
        }
   }
}
