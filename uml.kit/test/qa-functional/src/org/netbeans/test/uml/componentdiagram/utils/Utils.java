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


/*
 * Utils.java
 *
 * Created on 31 ���� 2005 �., 19:11
 * @author psb
 */

package org.netbeans.test.uml.componentdiagram.utils;

import java.awt.event.KeyEvent;
import javax.swing.JTextField;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.TreeTableOperator;
import org.netbeans.jellytools.actions.NewProjectAction;
import org.netbeans.jellytools.properties.Property;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.Timeout;
import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.input.KeyRobotDriver;
import org.netbeans.jemmy.drivers.input.MouseRobotDriver;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.test.umllib.CompartmentOperator;
import org.netbeans.test.umllib.util.OptionsOperator;

/**
 *
 * @author psb
 */
public class Utils {
     //
    private static boolean innerCall=false;
    //
    public static String defaultNewElementName="Unnamed";
    public static String defaultReturnType="void";
    public static String defaultAttributeType="int";
    public static String defaultAttributeVisibility="private";
    public static String defaultAttributeValue="";
    public static String defaultOperationVisibility="public";
    //
    private static int minWait=50;
    private static int longWait=500;
    
    {
        DriverManager.setMouseDriver(new MouseRobotDriver(new Timeout("",50)));
        DriverManager.setKeyDriver(new KeyRobotDriver(new Timeout("autoDelay",50)));
    }
    
   //=========================================================================================================
   static public CompartmentOperator getNotConstructorFinalizerOperationCmp(CompartmentOperator opComp,String className,int index)
   {
       int count_index=0;
        CompartmentOperator oprCmp=null;
        for(int i=0;i<opComp.getCompartments().size();i++) {
            String tmp=opComp.getCompartments().get(i).getName();
            //
            if(tmp.indexOf("public "+className+"(")==-1 && tmp.indexOf("void finalize(")==-1) {
                if(count_index>=index)
                {
                    oprCmp=opComp.getCompartments().get(i);
                    break;
                }
                else
                {
                    count_index++;
                }
            }
        }
        return oprCmp;
    }
   static public CompartmentOperator getNotConstructorFinalizerOperationCmp(CompartmentOperator opComp,String className)
   {
       return getNotConstructorFinalizerOperationCmp(opComp,className,0);
   }
   //
   static public String getNotConstructorFinalizerOperationStr(CompartmentOperator opComp,String className,int index)
   {
         return getNotConstructorFinalizerOperationCmp(opComp,className,index).getName();
   }
   static public String getNotConstructorFinalizerOperationStr(CompartmentOperator opComp,String className)
   {
       return getNotConstructorFinalizerOperationStr(opComp,className,0);
   }

   public static void setTextProperty(String pName,String pValue)
   {
        PropertySheetOperator ps=new PropertySheetOperator();
        Property nmProp=new Property(ps,pName);
        double nmPntX=ps.tblSheet().getCellRect(nmProp.getRow(),1,false).getCenterX();
        double nmPntY=ps.tblSheet().getCellRect(nmProp.getRow(),1,false).getCenterY();
        ps.clickMouse((int)nmPntX,(int)nmPntY,1);
        for(int i=0;i<pValue.length();i++)ps.typeKey(pValue.charAt(i));
        ps.pushKey(KeyEvent.VK_ENTER);
    }

   //
    public static void setDefaultPreferences()
   {
        OptionsOperator op=OptionsOperator.invoke();
        op=op.invokeAdvanced();
        TreeTableOperator tr=op.treeTable();
        tr.tree().selectPath(tr.tree().findPath("UML|New Project"));
        tr.tree().waitSelected(tr.tree().findPath("UML|New Project"));
        new EventTool().waitNoEvent(1000);
        PropertySheetOperator ps=new PropertySheetOperator(op);
        Property pr=new Property(ps,"Create New Diagram");
        pr.setValue(1);
        if(pr.getValue().equalsIgnoreCase("yes"))pr.setValue(0);
        //autosave diagrams
        tr.tree().selectPath(tr.tree().findPath("UML"));
        tr.tree().waitSelected(tr.tree().findPath("UML"));
        pr=new Property(ps,"Prompt to Save Diagram");
        pr.setValue(1);
        if(!pr.getValue().equalsIgnoreCase("No"))pr.setValue(0);
        //
        new JButtonOperator(op,"Close").push();
   }
   public static void commonComponentDiagramSetup(String workdir,String prName)
   {
            //setDefaultPreferences();
            new NewProjectAction().performMenu();
            NewProjectWizardOperator newWizardOper=new NewProjectWizardOperator();
            new EventTool().waitNoEvent(500);
            try{Thread.sleep(2000);}catch(Exception ex){}
            //newWizardOper.selectCategory(qa.uml.util.LabelsAndTitles.UML_PROJECTS_CATEGORY);
            JTreeOperator catTree=new JTreeOperator(newWizardOper);
            java.awt.Rectangle pth=catTree.getPathBounds(catTree.findPath(org.netbeans.test.umllib.util.LabelsAndTitles.UML_PROJECTS_CATEGORY));
            catTree.moveMouse(pth.x+pth.width/3, pth.y+pth.height/2);
            catTree.selectPath(catTree.findPath(org.netbeans.test.umllib.util.LabelsAndTitles.UML_PROJECTS_CATEGORY));
            catTree.waitSelected(catTree.findPath(org.netbeans.test.umllib.util.LabelsAndTitles.UML_PROJECTS_CATEGORY));
            new EventTool().waitNoEvent(500);
            newWizardOper.selectProject(org.netbeans.test.umllib.util.LabelsAndTitles.ANALYSIS_UML_PROJECT_LABEL);
            newWizardOper.next();
            JLabelOperator ploL=new JLabelOperator(newWizardOper,"Project Location:");
            JTextFieldOperator ploT=new JTextFieldOperator((JTextField)(ploL.getLabelFor()));
            ploT.clearText();
            ploT.typeText(workdir);
            JLabelOperator pnmL=new JLabelOperator(newWizardOper,"Project Name:");
            JTextFieldOperator pnmT=new JTextFieldOperator((JTextField)(pnmL.getLabelFor()));
            pnmT.clearText();
            pnmT.typeText(prName);
            // newWizardOper.finish();
            new JButtonOperator(newWizardOper, "Finish").push();
            new JButtonOperator(new JDialogOperator("Create New Diagram"), "Cancel").push();
            //properties
            new JMenuBarOperator(MainWindowOperator.getDefault()).pushMenu("Window|Properties");
            new PropertySheetOperator();
            new EventTool().waitNoEvent(500);
       
   }
}
