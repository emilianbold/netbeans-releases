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


package org.netbeans.test.umllib.tests;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxMenuItemOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
//6.0import org.netbeans.modules.uml.ui.products.ad.graphobjects.ETEdge;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.DiagramToolbarOperator;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.LinkOperator;
import org.netbeans.test.umllib.LinkTypes;
import org.netbeans.test.umllib.exceptions.NotFoundException;
import org.netbeans.test.umllib.exceptions.UMLCommonException;
import org.netbeans.test.umllib.testcases.UMLTestCase;
import org.netbeans.test.umllib.util.JPopupByPointChooser;
//6.0 import com.tomsawyer.drawing.geometry.TSConstPoint;

/**
 *
 * @author psb
 * @spec UML/ComponentDiagram.xml
 */
public abstract class DiagramLayout extends UMLTestCase {
    
    //some system properties
    private static String contextPropItemName="Properties";
    private static String umlPropertyWindowTitle="Project Properties";
    private static String umlSourcePackagesLabel="Source Packages";
    private static String umlSourcePackagesColumn="Folder Label";
    private static String umlSourceUsageColumn="Model?";
    private static String mainTreeTabName="Projects";
    public static String diagramType=null;//NewDiagramWizardOperator.ACTIVITY_DIAGRAM;
    public static String childClassName=null;
    //common test properties
    public static String prName=null;// "ActivityDiagramProjectADC";
    public static String project = null;//prName+"|Model";
    private static String sourceProject = "source";
    private static boolean codeSync=false;
    private ProjectsTabOperator pto=null;
    private Node lastDiagramNode=null;
    private String lastTestCase=null;
    private JTreeOperator prTree=null;
    private static String workdir=System.getProperty("nbjunit.workdir");
    private static long elCount=0;

    private static ElementTypes centerElement=null;
    private static ElementTypes outerElement=null;
    private static LinkTypes link=null;

    private boolean fromouter;

    public static final int NOMENUITEM=1;
    
    /**  */
    public DiagramLayout(String name,String type,String prNm,String childClName,ElementTypes center,ElementTypes outer,LinkTypes lnk,boolean fromouter) {
        super(name);
        diagramType=type;
        prName=prNm;
        project = prName+"|Model";
        childClassName=childClName;
        centerElement=center;
        outerElement=outer;
        link=lnk;
        this.fromouter=fromouter;
    }

    
     public void setUp() {
        System.out.println("########  "+getName()+"  #######");
        pto = ProjectsTabOperator.invoke();
        if(!codeSync)
        {
            if(prName==null)throw new UMLCommonException("Project is null");
            org.netbeans.test.umllib.tests.utils.Utils.commonTestsSetup(workdir, prName,"Yes");
            //
            codeSync=true;
        }
    }
   

 
    public void testHierarchicalLayoutViaContext()
    {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String diagramName="testD"+elCount;
        String workPkg="pkg"+elCount;
        //
        createContent();
        //
        DiagramOperator dgr=new DiagramOperator(diagramName);
        //
        JMenuItemOperator mi=null;
        try
        {
            mi=dgr.getDrawingArea().getPopup().showMenuItem("Layout|Hierarchical");
        }
        catch(TimeoutExpiredException ex)
        {
            failInPlace(NOMENUITEM,ex);
        }
        if(mi==null)failInPlace(NOMENUITEM,new NotFoundException("Can't find Layout|Hierarchical menu item"));
        mi.pushNoBlock();
        verifyHierarchical(diagramName);
    }
    public void testHierarchicalLayoutViaToolbar()
    {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String diagramName="testD"+elCount;
        String workPkg="pkg"+elCount;
        //
        createContent();
        //
        DiagramOperator dgr=new DiagramOperator(diagramName);
        //
        dgr.toolbar().selectToolNoBlock(DiagramToolbarOperator.HIERARCHICAL_LAYOUT_TOOL);
        verifyHierarchical(diagramName);
    }
    public void testHierarchicalLayoutViaShortcut()
    {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String diagramName="testD"+elCount;
        String workPkg="pkg"+elCount;
        //
        createContent();
        //
        DiagramOperator dgr=new DiagramOperator(diagramName);
        //
        dgr.pushKey(KeyEvent.VK_K,KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK);
        verifyHierarchical(diagramName);
    }
    
    public void testOrthogonalLayoutViaContext()
    {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String diagramName="testD"+elCount;
        String workPkg="pkg"+elCount;
        //
        createContent();
        //
        DiagramOperator dgr=new DiagramOperator(diagramName);
        //
        JMenuItemOperator mi=null;
        try
        {
            mi=dgr.getDrawingArea().getPopup().showMenuItem("Layout|Orthogonal");
        }
        catch(TimeoutExpiredException ex)
        {
            failInPlace(NOMENUITEM,ex);
        }
        if(mi==null)failInPlace(NOMENUITEM,new NotFoundException("Can't find Layout|Hierarchical menu item"));
        mi.pushNoBlock();
        //
        verifyOrthogonal(diagramName);
    }
    public void testOrthogonalLayoutViaToolbar()
    {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String diagramName="testD"+elCount;
        String workPkg="pkg"+elCount;
        //
        createContent();
        //
        DiagramOperator dgr=new DiagramOperator(diagramName);
        //
        dgr.toolbar().selectToolNoBlock(DiagramToolbarOperator.ORTHOGONAL_LAYOUT_TOOL);
        //
        verifyOrthogonal(diagramName);
    }
    public void testOrthogonalLayoutViaShortcut()
    {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String diagramName="testD"+elCount;
        String workPkg="pkg"+elCount;
        //
        createContent();
        //
        DiagramOperator dgr=new DiagramOperator(diagramName);
        //
        dgr.pushKey(KeyEvent.VK_B,KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK);
       //
        verifyOrthogonal(diagramName);
    }
    
    public void testSymmetricLayoutViaContext()
    {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String diagramName="testD"+elCount;
        String workPkg="pkg"+elCount;
        //
        createContent();
        //
        DiagramOperator dgr=new DiagramOperator(diagramName);
        //
        JMenuItemOperator mi=null;
        try
        {
            mi=dgr.getDrawingArea().getPopup().showMenuItem("Layout|Symmetric");
        }
        catch(TimeoutExpiredException ex)
        {
            failInPlace(NOMENUITEM,ex);
        }
        if(mi==null)failInPlace(NOMENUITEM,new NotFoundException("Can't find Layout|Hierarchical menu item"));
        mi.pushNoBlock();
        //
        verifySymmetric(diagramName);
     }
    public void testSymmetricLayoutViaToolbar()
    {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String diagramName="testD"+elCount;
        String workPkg="pkg"+elCount;
        //
        createContent();
        //
        DiagramOperator dgr=new DiagramOperator(diagramName);
        //
        dgr.toolbar().selectToolNoBlock(DiagramToolbarOperator.SYMMETRIC_LAYOUT_TOOL);
        //
        verifySymmetric(diagramName);
    }
    
    public void testSymmetricLayoutViaShortcut()
    {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String diagramName="testD"+elCount;
        String workPkg="pkg"+elCount;
        //
        createContent();
        //
        DiagramOperator dgr=new DiagramOperator(diagramName);
        //
        dgr.pushKey(KeyEvent.VK_Y,KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK);
        //
        verifySymmetric(diagramName);
    }
    
    public void testIncrementalLayoutViaContext()
    {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String diagramName="testD"+elCount;
        String workPkg="pkg"+elCount;
        //
        createContent();
        //
        DiagramOperator dgr=new DiagramOperator(diagramName);
        //
        JMenuItemOperator mi=null;
        try
        {
            mi=dgr.getDrawingArea().getPopup().showMenuItem("Layout|Orthogonal");
        }
        catch(TimeoutExpiredException ex)
        {
            failInPlace(NOMENUITEM,ex);
        }
        if(mi==null)failInPlace(NOMENUITEM,new NotFoundException("Can't find Layout|Hierarchical menu item"));
        mi.pushNoBlock();
        JDialogOperator lt=new JDialogOperator("Layout");
        new JButtonOperator(lt,"Yes").push();
        lt.waitClosed();
        try{Thread.sleep(1000);}catch(Exception ex){}
        addContent(diagramName);
        mi=null;
        try
        {
            mi=dgr.getDrawingArea().getPopup().showMenuItem("Layout|Incremental");
        }
        catch(TimeoutExpiredException ex)
        {
            failInPlace(NOMENUITEM,ex);
        }
        if(mi==null)failInPlace(NOMENUITEM,new NotFoundException("Can't find Layout|Hierarchical menu item"));
        mi.pushNoBlock();
        //
        verifyIncremental(diagramName);
     }
    public void testIncrementalLayoutViaToolbar()
    {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String diagramName="testD"+elCount;
        String workPkg="pkg"+elCount;
        //
        createContent();
        //
        DiagramOperator dgr=new DiagramOperator(diagramName);
        //
        dgr.toolbar().selectToolNoBlock(DiagramToolbarOperator.ORTHOGONAL_LAYOUT_TOOL);
        JDialogOperator lt=new JDialogOperator("Layout");
        new JButtonOperator(lt,"Yes").push();
        lt.waitClosed();
        try{Thread.sleep(1000);}catch(Exception ex){}
        addContent(diagramName);
        //
        dgr.toolbar().selectToolNoBlock(DiagramToolbarOperator.INCREMENTAL_LAYOUT_TOOL);
        verifyIncremental(diagramName);
    }
    
    public void testIncrementalLayoutViaShortcut()
    {
        lastTestCase=getCurrentTestMethodName();
        elCount++;
        String diagramName="testD"+elCount;
        String workPkg="pkg"+elCount;
        //
        createContent();
        //
        DiagramOperator dgr=new DiagramOperator(diagramName);
        //
        dgr.pushKey(KeyEvent.VK_B,KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK);
        JDialogOperator lt=new JDialogOperator("Layout");
        new JButtonOperator(lt,"Yes").push();
        lt.waitClosed();
        try{Thread.sleep(1000);}catch(Exception ex){}
        addContent(diagramName);
        dgr.pushKey(KeyEvent.VK_I,KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK);
        //
        verifyIncremental(diagramName);
    }
    
    
    
    private void createContent()
    {
        String diagramName="testD"+elCount;
        String workPkg="pkg"+elCount;
        //
        org.netbeans.test.umllib.Utils.RetAll rt=org.netbeans.test.umllib.Utils.createDiagram(project,workPkg,diagramName,diagramType);
        lastDiagramNode=rt.lastDiagramNode;
        Point a=rt.dOp.getDrawingArea().getFreePoint(120);
        DiagramElementOperator cntr=rt.dOp.putElementOnDiagram("CENTER",centerElement,a.x,a.y);
        a=rt.dOp.getDrawingArea().getFreePoint(30);
        DiagramElementOperator o1=rt.dOp.putElementOnDiagram("OUT1",outerElement,a.x,a.y);
        a=rt.dOp.getDrawingArea().getFreePoint(30);
        DiagramElementOperator o2=rt.dOp.putElementOnDiagram("OUT2",outerElement,a.x,a.y);
        a=rt.dOp.getDrawingArea().getFreePoint(30);
        DiagramElementOperator o3=rt.dOp.putElementOnDiagram("OUT3",outerElement,a.x,a.y);
        //
        if(fromouter)
        {
            rt.dOp.createLinkOnDiagram(link,o1,cntr);
            rt.dOp.createLinkOnDiagram(link,o2,cntr);
            rt.dOp.createLinkOnDiagram(link,o3,cntr);
        }
        else
        {
            rt.dOp.createLinkOnDiagram(link,cntr,o1);
            rt.dOp.createLinkOnDiagram(link,cntr,o2);
            rt.dOp.createLinkOnDiagram(link,cntr,o3);
        }
    }
    private void addContent(String diagramName)
    {
        //
        DiagramOperator dOp=new DiagramOperator(diagramName);
        Point a=a=dOp.getDrawingArea().getFreePoint(30);
        DiagramElementOperator cntr=new DiagramElementOperator(dOp,"CENTER",centerElement);
        DiagramElementOperator o4=dOp.putElementOnDiagram("OUT4",outerElement,a.x,a.y);
        //
        if(fromouter)
        {
            dOp.createLinkOnDiagram(link,o4,cntr);
        }
        else
        {
            dOp.createLinkOnDiagram(link,cntr,o4);
        }
    }
    
    private void verifyHierarchical(String diagramName)
    {
        //
        JDialogOperator lt=new JDialogOperator("Layout");
        new JButtonOperator(lt,"Yes").push();
        lt.waitClosed();
        try{Thread.sleep(1000);}catch(Exception ex){}
        DiagramOperator dgr=new DiagramOperator(diagramName);
        DiagramElementOperator cntr=new DiagramElementOperator(dgr,"CENTER",centerElement);
        DiagramElementOperator o1=new DiagramElementOperator(dgr,"OUT1",outerElement);
        DiagramElementOperator o2=new DiagramElementOperator(dgr,"OUT2",outerElement);
        DiagramElementOperator o3=new DiagramElementOperator(dgr,"OUT3",outerElement);
        //
        assertTrue("All \"outer\" elements should be on the same level, now: "+o1.getCenterPoint().y+";"+o2.getCenterPoint().y+";"+o3.getCenterPoint().y,o1.getCenterPoint().y==o2.getCenterPoint().y && o3.getCenterPoint().y==o2.getCenterPoint().y);
        int maxX=Math.max(Math.max(o1.getCenterPoint().x,o2.getCenterPoint().x),o3.getCenterPoint().x);
        int minX=Math.min(Math.min(o1.getCenterPoint().x,o2.getCenterPoint().x),o3.getCenterPoint().x);
        assertTrue("Center element should be between \"outer\" elements on x axe, now: "+cntr.getCenterPoint().x+" vs "+o1.getCenterPoint().x+";"+o2.getCenterPoint().x+";"+o3.getCenterPoint().x,maxX>cntr.getCenterPoint().x && minX<cntr.getCenterPoint().x);
        if(!fromouter)
        {
            assertTrue("Element level do not match relation after layout",o1.getCenterPoint().y<cntr.getCenterPoint().y);
        }
        else
        {
            assertTrue("Element level do not match relation after layout",o1.getCenterPoint().y>cntr.getCenterPoint().y);
        }
        //verify buttons, properties etc
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        dgr.getDrawingArea().clickMouse();
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        dgr.getDrawingArea().clickMouse();
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        //properties
        PropertySheetOperator ps=null;
        try{
            ps=new PropertySheetOperator(diagramName+" - Properties");
        }
        catch(Exception ex)
        {
            ps=new PropertySheetOperator();
            fail("Property sheet isn't for "+diagramName+", but for "+ps.getName());
        }
        Property pr=new Property(ps,"Layout Style");
        assertTrue("Layout style isn't hierarchical in properties","hierarchical".equals(pr.getValue()));
        //context menu
        JMenuItemOperator mi=null;
        JPopupMenuOperator popm=dgr.getDrawingArea().getPopup();
        try
        {
            mi=popm.showMenuItem("Layout");
        }
        catch(TimeoutExpiredException e)
        {
            //do nothing, null check is below
        }
        //do not check if there is no item, fail should be in another place of test
        if(mi!=null)
        {
            mi.pushNoBlock();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            //fail(mi.getHeight()/2+":"+mi.getWidth()+":"+mi.getLocationOnScreen());
            JPopupMenuOperator pmo=new JPopupMenuOperator(MainWindowOperator.getDefault(),new JPopupByPointChooser(mi.getWidth()+30,mi.getHeight()/2,mi.getSource(),0));
            JCheckBoxMenuItemOperator cmi=new JCheckBoxMenuItemOperator(pmo,"Hierarchical");
            assertTrue("Layout|Hierarchical isn't checked",cmi.isSelected());
        }
        //toolbar
        assertTrue(100638,"Hierarchical on toolbar isn't selected",dgr.toolbar().getToggleButtonByTooltip(DiagramToolbarOperator.HIERARCHICAL_LAYOUT_TOOL).isSelected());
    }
    
    private void verifyOrthogonal(String diagramName)
    {
        //
        JDialogOperator lt=new JDialogOperator("Layout");
        new JButtonOperator(lt,"Yes").push();
        lt.waitClosed();
        try{Thread.sleep(1000);}catch(Exception ex){}
        DiagramOperator dgr=new DiagramOperator(diagramName);
        DiagramElementOperator cntr=new DiagramElementOperator(dgr,"CENTER",centerElement);
        DiagramElementOperator o1=new DiagramElementOperator(dgr,"OUT1",outerElement);
        DiagramElementOperator o2=new DiagramElementOperator(dgr,"OUT2",outerElement);
        DiagramElementOperator o3=new DiagramElementOperator(dgr,"OUT3",outerElement);
        //
        int maxX=Math.max(Math.max(o1.getCenterPoint().x,o2.getCenterPoint().x),o3.getCenterPoint().x);
        int minX=Math.min(Math.min(o1.getCenterPoint().x,o2.getCenterPoint().x),o3.getCenterPoint().x);
        int maxY=Math.max(Math.max(o1.getCenterPoint().y,o2.getCenterPoint().y),o3.getCenterPoint().y);
        int minY=Math.min(Math.min(o1.getCenterPoint().y,o2.getCenterPoint().y),o3.getCenterPoint().y);
        assertTrue("Center element should be between \"outer\" elements on x or y axe, now: "+cntr.getCenterPoint().x+" vs "+o1.getCenterPoint().x+";"+o2.getCenterPoint().x+";"+o3.getCenterPoint().x,(maxX>cntr.getCenterPoint().x && minX<cntr.getCenterPoint().x) || (maxY>cntr.getCenterPoint().y && minY<cntr.getCenterPoint().y));
        //verify links are orthogonal
        
        LinkOperator lnks[]=null;
        if(fromouter) lnks=new LinkOperator[]{new LinkOperator(o1,cntr),new LinkOperator(o2,cntr),new LinkOperator(o3,cntr)};
        else lnks=new LinkOperator[]{new LinkOperator(cntr,o1),new LinkOperator(cntr,o2),new LinkOperator(cntr,o3)};
        //
//6.0        for(int i=0;i<lnks.length;i++)
//        {
//            ArrayList<TSConstPoint> pnts=new ArrayList<TSConstPoint>();
//            pnts.add(((ETEdge)(lnks[i].getSource())).getSourceClippingPoint());
//            for(int j=0;j<lnks[i].getBends().size();j++)
//            {
//                pnts.add((TSConstPoint)(lnks[i].getBends().get(j)));
//            }
//            pnts.add(((ETEdge)(lnks[i].getSource())).getTargetClippingPoint());
//            //
//            for(int j=1;j<pnts.size();j++)
//            {
//                assertTrue("Pairs of bends/target/source points on link do not on same axe",pnts.get(j).getX()==pnts.get(j-1).getX() || pnts.get(j).getY()==pnts.get(j-1).getY());
//            }
//6.0        }
        //
        int top=0;
        int left=0;
        int right=0;
        int bottom=0;
        //
        if(o1.getCenterPoint().x<(cntr.getCenterPoint().x-cntr.getBoundingRect().width/2-o1.getBoundingRect().width/2))
        {
            //it's at left side
            left++;
            assertTrue("OUT1 shifts greatly from horizontal axe",o1.getCenterPoint().y>(cntr.getCenterPoint().y-10) && o1.getCenterPoint().y<(cntr.getCenterPoint().y+10));
        }
        else if(o1.getCenterPoint().x>(cntr.getCenterPoint().x+cntr.getBoundingRect().width/2+o1.getBoundingRect().width/2))
        {
            //right side
            right++;
            assertTrue("OUT1 shifts greatly from horizontal axe",o1.getCenterPoint().y>(cntr.getCenterPoint().y-10) && o1.getCenterPoint().y<(cntr.getCenterPoint().y+10));
        }
        else if(o1.getCenterPoint().y<(cntr.getCenterPoint().y-cntr.getBoundingRect().height/2-o1.getBoundingRect().height/2))
        {
            //top
            top++;
            assertTrue("OUT1 shifts greatly from vertical axe",o1.getCenterPoint().x>(cntr.getCenterPoint().x-10) && o1.getCenterPoint().x<(cntr.getCenterPoint().x+10));
        }
        else if(o1.getCenterPoint().y>(cntr.getCenterPoint().y+cntr.getBoundingRect().height/2+o1.getBoundingRect().height/2))
        {
            bottom++;
            assertTrue("OUT1 shifts greatly from vertical axe",o1.getCenterPoint().x>(cntr.getCenterPoint().x-10) && o1.getCenterPoint().x<(cntr.getCenterPoint().x+10));
        }
        //--
        if(o2.getCenterPoint().x<(cntr.getCenterPoint().x-cntr.getBoundingRect().width/2-o2.getBoundingRect().width/2))
        {
            //it's at left side
            left++;
            assertTrue("OUT2 shifts greatly from horizontal axe",o2.getCenterPoint().y>(cntr.getCenterPoint().y-10) && o2.getCenterPoint().y<(cntr.getCenterPoint().y+10));
        }
        else if(o2.getCenterPoint().x>(cntr.getCenterPoint().x+cntr.getBoundingRect().width/2+o2.getBoundingRect().width/2))
        {
            //right side
            right++;
            assertTrue("OUT2 shifts greatly from horizontal axe",o2.getCenterPoint().y>(cntr.getCenterPoint().y-10) && o2.getCenterPoint().y<(cntr.getCenterPoint().y+10));
        }
        else if(o2.getCenterPoint().y<(cntr.getCenterPoint().y-cntr.getBoundingRect().height/2-o2.getBoundingRect().height/2))
        {
            //top
            top++;
            assertTrue("OUT2 shifts greatly from vertical axe",o2.getCenterPoint().x>(cntr.getCenterPoint().x-10) && o2.getCenterPoint().x<(cntr.getCenterPoint().x+10));
        }
        else if(o2.getCenterPoint().y>(cntr.getCenterPoint().y+cntr.getBoundingRect().height/2+o2.getBoundingRect().height/2))
        {
            bottom++;
            assertTrue("OUT2 shifts greatly from vertical axe",o2.getCenterPoint().x>(cntr.getCenterPoint().x-10) && o2.getCenterPoint().x<(cntr.getCenterPoint().x+10));
        }
        //--
        if(o3.getCenterPoint().x<(cntr.getCenterPoint().x-cntr.getBoundingRect().width/2-o3.getBoundingRect().width/2))
        {
            //it's at left side
            left++;
            assertTrue("OUT3 shifts greatly from horizontal axe",o3.getCenterPoint().y>(cntr.getCenterPoint().y-10) && o3.getCenterPoint().y<(cntr.getCenterPoint().y+10));
        }
        else if(o3.getCenterPoint().x>(cntr.getCenterPoint().x+cntr.getBoundingRect().width/2+o3.getBoundingRect().width/2))
        {
            //right side
            right++;
            assertTrue("OUT3 shifts greatly from horizontal axe",o3.getCenterPoint().y>(cntr.getCenterPoint().y-10) && o3.getCenterPoint().y<(cntr.getCenterPoint().y+10));
        }
        else if(o3.getCenterPoint().y<(cntr.getCenterPoint().y-cntr.getBoundingRect().height/2-o3.getBoundingRect().height/2))
        {
            //top
            top++;
            assertTrue("OUT3 shifts greatly from vertical axe",o3.getCenterPoint().x>(cntr.getCenterPoint().x-10) && o3.getCenterPoint().x<(cntr.getCenterPoint().x+10));
        }
        else if(o3.getCenterPoint().y>(cntr.getCenterPoint().y+cntr.getBoundingRect().height/2+o3.getBoundingRect().height/2))
        {
            bottom++;
            assertTrue("OUT3 shifts greatly from vertical axe",o3.getCenterPoint().x>(cntr.getCenterPoint().x-10) && o3.getCenterPoint().x<(cntr.getCenterPoint().x+10));
        }
        assertTrue("Diagram do not match 3 sided scenaruio.",left<2 && right<2 && top<2 && bottom<2 && (left+top+bottom+right)==3);    
        //verify buttons, properties etc
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        dgr.getDrawingArea().clickMouse();
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        //properties
        PropertySheetOperator ps=new PropertySheetOperator();
        assertTrue("Property sheet isn't for "+diagramName+", but for "+ps.getName(),(diagramName+" - Properties").equals(ps.getName()));
        Property pr=new Property(ps,"Layout Style");
        assertTrue("Layout style isn't orthogonal in properties","orthogonal".equals(pr.getValue()));
        //context menu
        JMenuItemOperator mi=null;
        JPopupMenuOperator popm=dgr.getDrawingArea().getPopup();
        try
        {
            mi=popm.showMenuItem("Layout");
        }
        catch(TimeoutExpiredException e)
        {
            //do nothing, null check is below
        }
        //do not check if there is no item, fail should be in another place of test
        if(mi!=null)
        {
            mi.pushNoBlock();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            JPopupMenuOperator pmo=new JPopupMenuOperator(MainWindowOperator.getDefault(),new JPopupByPointChooser(mi.getWidth()+30,mi.getHeight()/2,mi.getSource(),0));
            JCheckBoxMenuItemOperator cmi=new JCheckBoxMenuItemOperator(pmo,"Orthogonal");
            assertTrue("Layout|Orthogonal isn't checked",cmi.isSelected());
        }
        //toolbar
        assertTrue(100638,"Orthogonal on toolbar isn't selected",dgr.toolbar().getToggleButtonByTooltip(DiagramToolbarOperator.ORTHOGONAL_LAYOUT_TOOL).isSelected());
    }
    
    private void verifySymmetric(String diagramName)
    {
       //
        JDialogOperator lt=new JDialogOperator("Layout");
        new JButtonOperator(lt,"Yes").push();
        lt.waitClosed();
        try{Thread.sleep(1000);}catch(Exception ex){}
        DiagramOperator dgr=new DiagramOperator(diagramName);
        DiagramElementOperator cntr=new DiagramElementOperator(dgr,"CENTER",centerElement);
        DiagramElementOperator o1=new DiagramElementOperator(dgr,"OUT1",outerElement);
        DiagramElementOperator o2=new DiagramElementOperator(dgr,"OUT2",outerElement);
        DiagramElementOperator o3=new DiagramElementOperator(dgr,"OUT3",outerElement);
        //
        Point cp=cntr.getCenterPoint();
        Point p1=o1.getCenterPoint();
        Point p2=o2.getCenterPoint();
        Point p3=o3.getCenterPoint();
        //
        int maxX=Math.max(Math.max(p1.x,p2.x),p3.x);
        int minX=Math.min(Math.min(p1.x,p2.x),p3.x);
        int maxY=Math.max(Math.max(p1.y,p2.y),p3.y);
        int minY=Math.min(Math.min(p1.y,p2.y),p3.y);
        assertTrue("Center element should be between \"outer\" elements on x and y axe, now: "+cp.x+" vs "+p1.x+";"+p2.x+";"+p3.x,maxX>cp.x && minX<cp.x  && minY<cp.y && maxY>cp.y);
        //find angles 1-2, 1-3
        double dx1=p1.x-cp.x;
        double dy1=p1.y-cp.y;
        double len1=Math.sqrt(dx1*dx1+dy1*dy1);
        dx1=dx1/len1;
        dy1=dy1/len1;

        double dx2=p2.x-cp.x;
        double dy2=p2.y-cp.y;
        double len2=Math.sqrt(dx2*dx2+dy2*dy2);
        dx2=dx2/len2;
        dy2=dy2/len2;

        double dx3=p3.x-cp.x;
        double dy3=p3.y-cp.y;
        double len3=Math.sqrt(dx3*dx3+dy3*dy3);
        dx3=dx3/len3;
        dy3=dy3/len3;
        
        double cos12=dx1*dx2+dy1*dy2;
        double cos13=dx1*dx3+dy1*dy3;
        double cos23=dx2*dx3+dy2*dy3;

        double angle12=Math.acos(cos12)*180/Math.PI;
        double angle13=Math.acos(cos13)*180/Math.PI;
        double angle23=Math.acos(cos23)*180/Math.PI;
        
        assertTrue("Angles should be about 120 grad: "+angle12+":"+angle13+":"+angle23,Math.abs(angle12-120)<12 && Math.abs(angle13-120)<12 && Math.abs(angle23-120)<12);
        
        //verify buttons, properties etc
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        dgr.getDrawingArea().clickMouse();
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        //properties
        PropertySheetOperator ps=new PropertySheetOperator();
        assertTrue("Property sheet isn't for "+diagramName+", but for "+ps.getName(),(diagramName+" - Properties").equals(ps.getName()));
        Property pr=new Property(ps,"Layout Style");
        assertTrue("Layout style isn't symmetric in properties","symmetric".equals(pr.getValue()));
        //context menu
        JMenuItemOperator mi=null;
        JPopupMenuOperator popm=dgr.getDrawingArea().getPopup();
        try
        {
            mi=popm.showMenuItem("Layout");
        }
        catch(TimeoutExpiredException e)
        {
            //do nothing, null check is below
        }
        //do not check if there is no item, fail should be in another place of test
        if(mi!=null)
        {
            mi.pushNoBlock();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            JPopupMenuOperator pmo=new JPopupMenuOperator(MainWindowOperator.getDefault(),new JPopupByPointChooser(mi.getWidth()+30,mi.getHeight()/2,mi.getSource(),0));
            JCheckBoxMenuItemOperator cmi=new JCheckBoxMenuItemOperator(pmo,"Symmetric");
            assertTrue("Layout|Orthogonal isn't checked",cmi.isSelected());
        }
        //toolbar
        assertTrue(100638,"Orthogonal on toolbar isn't selected",dgr.toolbar().getToggleButtonByTooltip(DiagramToolbarOperator.SYMMETRIC_LAYOUT_TOOL).isSelected());
    }
    
    private void verifyIncremental(String diagramName)
    {
        //
//6.0        JDialogOperator lt=new JDialogOperator("Layout");
//        new JButtonOperator(lt,"Yes").push();
//        lt.waitClosed();
//        try{Thread.sleep(1000);}catch(Exception ex){}
//        DiagramOperator dgr=new DiagramOperator(diagramName);
//        DiagramElementOperator cntr=new DiagramElementOperator(dgr,"CENTER",centerElement);
//        DiagramElementOperator o1=new DiagramElementOperator(dgr,"OUT1",outerElement);
//        DiagramElementOperator o2=new DiagramElementOperator(dgr,"OUT2",outerElement);
//        DiagramElementOperator o3=new DiagramElementOperator(dgr,"OUT3",outerElement);
//        DiagramElementOperator o4=new DiagramElementOperator(dgr,"OUT4",outerElement);
//        //
//        int maxX=Math.max(Math.max(Math.max(o1.getCenterPoint().x,o2.getCenterPoint().x),o3.getCenterPoint().x),o4.getCenterPoint().x);
//        int minX=Math.min(Math.min(Math.min(o1.getCenterPoint().x,o2.getCenterPoint().x),o3.getCenterPoint().x),o4.getCenterPoint().x);
//        int maxY=Math.max(Math.max(Math.max(o1.getCenterPoint().y,o2.getCenterPoint().y),o3.getCenterPoint().y),o4.getCenterPoint().y);
//        int minY=Math.min(Math.min(Math.min(o1.getCenterPoint().y,o2.getCenterPoint().y),o3.getCenterPoint().y),o4.getCenterPoint().y);
//        assertTrue("Center element should be between \"outer\" elements on x or y axe, now: "+cntr.getCenterPoint().x+" vs "+o1.getCenterPoint().x+";"+o2.getCenterPoint().x+";"+o3.getCenterPoint().x,(maxX>cntr.getCenterPoint().x && minX<cntr.getCenterPoint().x) || (maxY>cntr.getCenterPoint().y && minY<cntr.getCenterPoint().y));
//        //verify links are orthogonal
//        LinkOperator lnks[]=null;
//        if(fromouter) lnks=new LinkOperator[]{new LinkOperator(o1,cntr),new LinkOperator(o2,cntr),new LinkOperator(o3,cntr),new LinkOperator(o4,cntr)};
//        else lnks=new LinkOperator[]{new LinkOperator(cntr,o1),new LinkOperator(cntr,o2),new LinkOperator(cntr,o3),new LinkOperator(cntr,o4)};
//        //
//        for(int i=0;i<lnks.length;i++)
//        {
//            ArrayList<TSConstPoint> pnts=new ArrayList<TSConstPoint>();
//            pnts.add(((ETEdge)(lnks[i].getSource())).getSourceClippingPoint());
//            for(int j=0;j<lnks[i].getBends().size();j++)
//            {
//                pnts.add((TSConstPoint)(lnks[i].getBends().get(j)));
//            }
//            pnts.add(((ETEdge)(lnks[i].getSource())).getTargetClippingPoint());
//            //
//            for(int j=1;j<pnts.size();j++)
//            {
//                assertTrue("Pairs of bends/target/source points on link do not on same axe",pnts.get(j).getX()==pnts.get(j-1).getX() || pnts.get(j).getY()==pnts.get(j-1).getY());
//            }
//        }
//        //verify buttons, properties etc
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException ex) {
//            ex.printStackTrace();
//        }
//        dgr.getDrawingArea().clickMouse();
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException ex) {
//            ex.printStackTrace();
//        }
//        //properties
//        PropertySheetOperator ps=new PropertySheetOperator();
//        assertTrue("Property sheet isn't for "+diagramName+", but for "+ps.getName(),(diagramName+" - Properties").equals(ps.getName()));
//        Property pr=new Property(ps,"Layout Style");
//        assertTrue("Layout style isn't orthogonal in properties","orthogonal".equals(pr.getValue()));
//        //context menu
//        JMenuItemOperator mi=null;
//        JPopupMenuOperator popm=dgr.getDrawingArea().getPopup();
//        try
//        {
//            mi=popm.showMenuItem("Layout");
//        }
//        catch(TimeoutExpiredException e)
//        {
//            //do nothing, null check is below
//        }
//        //do not check if there is no item, fail should be in another place of test
//        if(mi!=null)
//        {
//            mi.pushNoBlock();
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException ex) {
//                ex.printStackTrace();
//            }
//            JPopupMenuOperator pmo=new JPopupMenuOperator(MainWindowOperator.getDefault(),new JPopupByPointChooser(mi.getWidth()+30,mi.getHeight()/2,mi.getSource(),0));
//            JCheckBoxMenuItemOperator cmi=new JCheckBoxMenuItemOperator(pmo,"Orthogonal");
//            assertTrue("Layout|Orthogonal isn't checked",cmi.isSelected());
//        }
//        //toolbar
//6.0        assertTrue(100638,"Orthogonal on toolbar isn't selected",dgr.toolbar().getToggleButtonByTooltip(DiagramToolbarOperator.ORTHOGONAL_LAYOUT_TOOL).isSelected());
    }
    
    public void tearDown() {
        org.netbeans.test.umllib.util.Utils.makeScreenShot(childClassName,lastTestCase);
        //popup protection
        MainWindowOperator.getDefault().pushKey(KeyEvent.VK_ESCAPE);
        new EventTool().waitNoEvent(1000);
        //
        closeAllModal();
        if(lastDiagramNode!=null)
        {
            lastDiagramNode.collapse();
            new Node(lastDiagramNode.tree(),lastDiagramNode.getParentPath()).collapse();
        }
        //save all
        ContainerOperator tlb=MainWindowOperator.getDefault().getToolbar("File");
        JButtonOperator sa=MainWindowOperator.getDefault().getToolbarButton(tlb,"Save All");
        if(sa.isEnabled())
        {
            sa.push();
            sa.waitState(new ChooseEnabledState(false));
            try{Thread.sleep(100);}catch(Exception ex){}
        }
        //
        DiagramOperator d=null;
        try{
            d=new DiagramOperator("testD");
        }
        catch(Exception e)
        {
        }
        //
        if(d!=null)
        {
            try{
                final DiagramOperator d2=d;
                new Thread()
                {
                    public void run()
                    {
                         d2.closeAllDocuments();
                    }
                }.start();

                d.waitClosed();
               new EventTool().waitNoEvent(1000);
            }catch(Exception ex){};
        }
        closeAllModal();
        //save
        org.netbeans.test.umllib.util.Utils.tearDown();
   }
    
    abstract public void failInPlace(int failId,RuntimeException ex);//most fail will be handled in child class (with specific bugs)
    
    public class ChooseEnabledState implements ComponentChooser
    {
        private boolean enabled=false;
        
        ChooseEnabledState(boolean enabled)
        {
            this.enabled=enabled;
        }
        
        public boolean checkComponent(Component component) {
            return (component.isEnabled() && enabled) || (!enabled && !component.isEnabled());
        }

        public String getDescription() {
            return "choose component if it's enabled: "+enabled;
        }
        
    }
}
