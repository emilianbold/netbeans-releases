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


package org.netbeans.test.uml.pref;

import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;


import org.netbeans.junit.NbTestSuite;
import org.netbeans.test.umllib.DiagramElementOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.ElementTypes;
import org.netbeans.test.umllib.testcases.UMLTestCase;
import org.netbeans.test.umllib.util.OptionsOperator;
//import qa.uml.UMLClassOperator;




/**
 *
 * @author psb
 * @spec UML/
 */
public class UMLItems extends UMLTestCase {
    
    
    //some system properties
    private static String lastTestCase=null;
    private static boolean initialized=false;
    private static String[][] items={{"UML","Automatically Hide Modeling Window","Delete File when Deleting Artifacts","Show Aliases","Prompt to Save Diagram","Prompt to Save Project","Don't Show Filter Warning Dialog"}};
    private static String[][] defaultValues={{null,"Yes","Ask Me","No","Yes","Yes","Ask Me"}};
    private static String workdir=System.getProperty("nbjunit.workdir");
    private static String prName="UMLItems";
     private static String project=prName+"_uml";
     //
     private static int testCounter=0;
     //
     DiagramOperator dgr;
     //store
     ProjectsTabOperator pto = null;
     Node lastDiagramNode=null;
    
    /** Need to be defined because of JUnit */
    public UMLItems(String name) {
        super(name);
    }
     public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(org.netbeans.test.uml.pref.UMLItems.class);
        return suite;
    }
    
    
     public void setUp() {
        System.out.println("########  "+getName()+"  #######");
        if(!initialized)
        {
            org.netbeans.test.uml.pref.utils.Utils.commonSetup(workdir, prName);
            initialized=true;
        }
        pto=ProjectsTabOperator.invoke();
        new EventTool().waitNoEvent(1000);
    }
   
    public void testDontShowSaveDiagram() {
        lastTestCase=getCurrentTestMethodName();
        testCounter++;
            OptionsOperator op=OptionsOperator.invoke();
            op=op.invokeAdvanced();
            op.setAdvancedValues(items,defaultValues);
            op.close();
            //check behavour
            dgr=createDiagram(project,"pkg"+testCounter,"clD"+testCounter);
            //
            dgr.putElementOnDiagram("test1",ElementTypes.CLASS);
            //
            new Thread(new Runnable() {
                public void run() {
                    //
                    dgr.closeWindow();
                    dgr.waitComponentShowing(false);
                 }
            }).start();
            try{Thread.sleep(100);}catch(Exception e){}
           //
            JDialogOperator saveDlg=null;
            try
            {
                saveDlg=new JDialogOperator("Save Diagram");
            }
            catch(Exception ex)
            {
                fail("No save dilog with default option to save");
            }
            //
            new JButtonOperator(saveDlg,"Save Always").push();
            //reopen diagram
            dgr=createDiagram(project,"pkg"+testCounter,"clD"+testCounter);
            new DiagramElementOperator(dgr,"test1");
            dgr.putElementOnDiagram("test2",ElementTypes.CLASS);
            dgr.closeWindow();
            dgr.waitComponentShowing(false);
            dgr=createDiagram(project,"pkg"+testCounter,"clD"+testCounter);
            new DiagramElementOperator(dgr,"test2");
            org.netbeans.test.umllib.util.Utils.makeScreenShotCustom(lastTestCase,"save_1_");
            //
            op=OptionsOperator.invoke();
            op=op.invokeAdvanced();
            assertTrue("Option isn't set to No/ current: "+op.getAdvancedValue(items[0][0],items[0][4]),op.getAdvancedValue(items[0][0],items[0][4]).equals("No"));
            op.setAdvancedValue(items[0][0],items[0][4],"Yes");
            op.close();
            dgr=createDiagram(project,"pkg"+testCounter,"clD"+testCounter);
            new DiagramElementOperator(dgr,"test1");
            new DiagramElementOperator(dgr,"test2");
            dgr.putElementOnDiagram("test3",ElementTypes.CLASS);
            new Thread(new Runnable() {
                public void run() {
                    //
                    dgr.closeWindow();
                    dgr.waitComponentShowing(false);
                 }
            }).start();
            try{Thread.sleep(100);}catch(Exception e){}
            saveDlg=new JDialogOperator("Save Diagram");
            //
            new JButtonOperator(saveDlg,"No").push();
            saveDlg.waitClosed();
            org.netbeans.test.umllib.util.Utils.makeScreenShotCustom(lastTestCase,"save_2_");
            op=OptionsOperator.invoke();
            op=op.invokeAdvanced();
            assertTrue("Option isn't set to Ask Me/ current: "+op.getAdvancedValue(items[0][0],items[0][4]),op.getAdvancedValue(items[0][0],items[0][4]).equals("Yes"));
            op.setAdvancedValue(items[0][0],items[0][4],"No");
            op.close();
            dgr=createDiagram(project,"pkg"+testCounter,"clD"+testCounter);
            new DiagramElementOperator(dgr,"test1");
            new DiagramElementOperator(dgr,"test2");
            Object el3=null;
            try
            {
                el3=DiagramElementOperator.findGraphObject(dgr,new DiagramElementOperator.ElementByVNChooser("test3",ElementTypes.CLASS),0);
                fail("3rd element is  on diagram after No to save");
            }
            catch(org.netbeans.test.umllib.exceptions.NotFoundException ex)
            {
                //good
            }
            assertTrue("3rd element is  on diagram after No to save",el3==null);
            dgr.putElementOnDiagram("test4",ElementTypes.CLASS);
            dgr.closeWindow();
            dgr.waitComponentShowing(false);
            dgr=createDiagram(project,"pkg"+testCounter,"clD"+testCounter);
            new DiagramElementOperator(dgr,"test4");
    }

    public void testDontFilterWarning() {
        lastTestCase=getCurrentTestMethodName();
        testCounter++;
            OptionsOperator op=OptionsOperator.invoke();
            op=op.invokeAdvanced();
            op.setAdvancedValues(items,defaultValues);
            op.close();
            //check behavour
            Node prNode=new Node(pto.tree(),prName+"|Model");
            prNode.performPopupActionNoBlock("Filter...");
           //
            JDialogOperator filterDlg=null;
            try
            {
                filterDlg=new JDialogOperator("Filter Collapse Nodes Warning");
            }
            catch(Exception ex)
            {
                fail("No filter dilog with default option ask");
            }
            //
            JCheckBoxOperator chk=new JCheckBoxOperator(filterDlg);
            if(chk.isSelected())fail("CheckBox selected");
            chk.clickMouse();
            chk.waitSelected(true);
            //
            new JButtonOperator(filterDlg,"Yes").pushNoBlock();
            filterDlg.waitClosed();
            JDialogOperator filterDlg2=new JDialogOperator("Filter");
            new JButtonOperator(filterDlg2,"OK").pushNoBlock();
            filterDlg2.waitClosed();
            //reopen
            prNode.performPopupActionNoBlock("Filter...");
            filterDlg2=new JDialogOperator("Filter");
            new JButtonOperator(filterDlg2,"OK").pushNoBlock();
            filterDlg2.waitClosed();
            //
            op=OptionsOperator.invoke();
            op=op.invokeAdvanced();
            assertTrue("Option isn't set to Always/ current: "+op.getAdvancedValue(items[0][0],items[0][6]),op.getAdvancedValue(items[0][0],items[0][6]).equals("Always"));
            op.setAdvancedValue(items[0][0],items[0][6],"Ask Me");
            op.close();
            prNode=new Node(pto.tree(),prName+"|Model");
            prNode.performPopupActionNoBlock("Filter...");
            filterDlg=new JDialogOperator("Filter Collapse Nodes Warning");
            filterDlg.close();
            filterDlg.waitClosed();
            op=OptionsOperator.invoke();
            op=op.invokeAdvanced();
            op.setAdvancedValue(items[0][0],items[0][6],"Always");
            op.close();
            prNode.performPopupActionNoBlock("Filter...");
            filterDlg2=new JDialogOperator("Filter");
            new JButtonOperator(filterDlg2,"OK").pushNoBlock();
            filterDlg2.waitClosed();
    }

    public void testShowAliases() {
        lastTestCase=getCurrentTestMethodName();
        testCounter++;
            OptionsOperator op=OptionsOperator.invoke();
            op=op.invokeAdvanced();
            op.setAdvancedValues(items,defaultValues);
            op.close();
            dgr=createDiagram(project,"pkg"+testCounter,"clD"+testCounter);
            //
            dgr.putElementOnDiagram("test1",ElementTypes.CLASS);
            //
            fail(0,"under construction");
    } 

    //
     private DiagramOperator createDiagram(String project,String workPkg, String diagram){
        org.netbeans.test.umllib.Utils.RetAll rt=org.netbeans.test.umllib.Utils.createOrOpenDiagram(project,workPkg,diagram,org.netbeans.test.umllib.NewDiagramWizardOperator.CLASS_DIAGRAM);
        pto = rt.pto;
        lastDiagramNode=rt.lastDiagramNode;
        return rt.dOp;
    }
   //
    public void tearDown() {
         org.netbeans.test.umllib.util.Utils.makeScreenShot(lastTestCase);
         closeAllModal();
         if(OptionsOperator.findJDialog(new OptionsOperator.ChooseOptionDialog())!=null)
         {
             new OptionsOperator().close();
         }
         if(dgr!=null && dgr.isShowing())
         {
             new Thread(new Runnable() {
                public void run() {
                    //
                    dgr.closeWindow();
                    dgr.waitComponentShowing(false);
                 }
            }).start();
            try{Thread.sleep(100);}catch(Exception e){}
            new Thread(new Runnable() {
                public void run() {
                    //
                    JDialogOperator saveDlg=new JDialogOperator("Save Diagram");
                    new JButtonOperator(saveDlg,"No").push();
                 }
            }).start();
            try{Thread.sleep(1000);}catch(Exception e){}
         }
         closeAllModal();
         org.netbeans.test.umllib.util.Utils.tearDown();
   }
    
}
