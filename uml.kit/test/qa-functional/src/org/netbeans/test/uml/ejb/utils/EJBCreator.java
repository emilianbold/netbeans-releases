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



package org.netbeans.test.uml.ejb.utils;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JTextArea;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.Timeout;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.drivers.DriverManager;
import org.netbeans.jemmy.drivers.input.KeyEventDriver;
import org.netbeans.jemmy.drivers.input.KeyRobotDriver;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextAreaOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;
import org.netbeans.test.umllib.DiagramOperator;
import org.netbeans.test.umllib.exceptions.KnownBugException;
import org.netbeans.test.umllib.exceptions.UMLCommonException;


public class EJBCreator {
    
    public final static String EJB_1 = "EJB1.1";
    public final static String EJB_2 = "EJB2.0";
    
    public final static String BMP = "BeanManaged";
    public final static String CMP = "ContainerManaged";
    public final static String MDB = "MessageDriven";
    public final static String SESSION_STATELESS = "StatelessSession";
    public final static String SESSION_STATEFUL = "StatefulSession";
    
    public final String APPLY_PATTERN_MENU = "Apply Design Pattern...";
    public final String PATTERN_WIZARD_FIRST_STEP_TTL = "Design Pattern Apply Wizard";
    public final String PATTERN_WIZARD_TTL = "Design Pattern Wizard";
    
    private final String NEXT_BTN = "Next";
    private final String FINISH_BTN = "Finish";
    
    
    private ArrayList<Pair> pairs = null;    
    private EventTool eventTool = new EventTool();
    private String projectName = null;
    
    private static boolean reported_85870=false;
    
    /** Creates a new instance of BasicEJBPatternVerifier */
    public EJBCreator(String project) {
        this.projectName = project;
    }
    
    public DiagramOperator create(String invocationPath, String ejbType, String ejbName, String namespace, boolean createDiagram, String diagramName){
        eventTool.waitNoEvent(1000);
        openDesignPatternWizard(invocationPath);
        
        eventTool.waitNoEvent(1000);
        handleStartPage();
        
        eventTool.waitNoEvent(1000);
        handleSelectPattern(ejbType, ejbName);
        
        eventTool.waitNoEvent(1000);
        handleSelectTargetScope(namespace);
        
        eventTool.waitNoEvent(1000);
        handleChooseParticipants(pairs);
        
        eventTool.waitNoEvent(1000);
        handleSetOptions(createDiagram, diagramName);
        
        eventTool.waitNoEvent(1000);
        finishWizard();   
        //
        eventTool.waitNoEvent(3000);
        //
        DiagramOperator dia = null;
        try
        {
            dia=new DiagramOperator(diagramName);
        }
        catch(TimeoutExpiredException ex)
        {
            //1st dialog
            JDialog dlg=JDialogOperator.findJDialog("Invalid Property",true,true);
            if(dlg!=null)
            {
                JTextArea lbl=JTextAreaOperator.findJTextArea(dlg,"The operations of an interface must be abstract. The Abstract property for this operation will be set back to True.",false,false);
                if(lbl!=null)
                {
                    if(!reported_85870)
                    {
                        reported_85870=true;
                        new Thread(new TerminationDialogHandler()).start();
                        throw new KnownBugException(85870,"Unexpected \"Invalid Property\" dialogs on pattern application");
                    }
                    else throw new KnownBugException(85870,"Too long application and a lot of Unexpected \"Invalid Property\" dialogs on pattern application");
                }
                else
                {
                    throw new UMLCommonException("Unknown Invalid property dialog.");
                }
            }
            else
            {
                throw ex;
            }
        }
        return dia;
    }
    
    
    
    
    protected void openDesignPatternWizard(String pathToInvokationPoint){
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        ProjectRootNode root = new ProjectRootNode(pto.tree(),projectName);
        
        Node node = new Node(root, pathToInvokationPoint);        
        node.performPopupActionNoBlock(APPLY_PATTERN_MENU);
        
        new JDialogOperator(PATTERN_WIZARD_FIRST_STEP_TTL);
    }
    
    protected void handleStartPage(){        
        JDialogOperator dlg = new JDialogOperator(PATTERN_WIZARD_FIRST_STEP_TTL);
        new JButtonOperator(dlg, NEXT_BTN).push();
    }
    
    protected void handleSelectPattern(String ejbType, String ejbName){
        JDialogOperator dlg = new JDialogOperator(PATTERN_WIZARD_TTL);
        JComboBoxOperator cbProject = new JComboBoxOperator(dlg, 0);
        cbProject.selectItem(ejbType);
        eventTool.waitNoEvent(1000);
        JComboBoxOperator cbEJB = new JComboBoxOperator(dlg, 1);
        cbEJB.selectItem(ejbName);
        eventTool.waitNoEvent(1000);
        
        new JButtonOperator(dlg, NEXT_BTN).push();        
    }
    
    
    protected void handleSelectTargetScope(String namespace){
        JDialogOperator dlg = new JDialogOperator(PATTERN_WIZARD_TTL);
        if (namespace!=null){
            //JComboBoxOperator cbNamespace = new JComboBoxOperator(dlg, 1);
            JLabelOperator nspLbl=new JLabelOperator(dlg,"Namespace:");
            JComboBoxOperator cbNamespace = new JComboBoxOperator((JComboBox)(nspLbl.getLabelFor()));
            cbNamespace.selectItem(namespace);
            cbNamespace.waitItemSelected(namespace);
            eventTool.waitNoEvent(1000);            
        }       
        new JButtonOperator(dlg, NEXT_BTN).push();        
    }
    
    
    protected void handleChooseParticipants(ArrayList<Pair> pairs){
        if (pairs!=null){
            //to be implemented in the descendants or later
        }
        JDialogOperator dlg = new JDialogOperator(PATTERN_WIZARD_TTL);
        new JButtonOperator(dlg, NEXT_BTN).push();                
    }
    
    protected void handleSetOptions(boolean createDiagram, String diagramName){
        JDialogOperator dlg = new JDialogOperator(PATTERN_WIZARD_TTL);
        
        new JCheckBoxOperator(dlg).clickMouse();
        new JCheckBoxOperator(dlg).waitSelected(true);
        eventTool.waitNoEvent(1000);
        DriverManager.setKeyDriver(new KeyEventDriver());
        new JTextComponentOperator(dlg, 1).clearText();
        new JTextComponentOperator(dlg, 1).waitText("");
        new JTextComponentOperator(dlg, 1).typeText(diagramName);
        eventTool.waitNoEvent(1000);
        DriverManager.setKeyDriver(new KeyRobotDriver(new Timeout("autoDelay",50)));
        new JButtonOperator(dlg, NEXT_BTN).push();   
        JComboBox g;
    }
    
    protected void finishWizard(){
        JDialogOperator dlg = new JDialogOperator(PATTERN_WIZARD_FIRST_STEP_TTL);
        new JButtonOperator(dlg, FINISH_BTN).push();      
        dlg.waitClosed();
    }
    
    //***************************************************************
    //WORKAROUND FOR 85870
    //***************************************************************
    /**
     * class works until MainWindow is showing and wait for different times of dialogs
     */
    static private class TerminationDialogHandler implements Runnable
    {
        private long startTime;
        private long timeout;
        private long prevTime;
        //
        private int lastBtnIndex=0;
        protected JDialogOperator tDlg=null;
        //
        private String dialogTitle;
        private String buttonTitle[];
        //
        final private long EXEC_TIMEOUT=60*60*6*1000;//6 hours to exit thread
        final private long DEFAULT_TIMEOUT=60*1000;//wait 60 seconds and close unused dialog
        //
        final static public String DIALOG_TITLE_85870="Invalid Property";
        final static public String BUTTON_TITLE_85870="OK";
        final static public String TEXT_85870="The operations of an interface must be abstract. The Abstract property for this operation will be set back to True.";
        
        TerminationDialogHandler()
        {
            startTime=new Date().getTime();
        }
        
        public void run() {
            MainWindowOperator mw=null;
            while(true)
            {
                //try dialog every 2 second
                try{Thread.sleep(2000);}catch(Exception ex){};
                mw=MainWindowOperator.getDefault();
                if(!(mw!=null && mw.isShowing() && mw.isVisible()))
                {
                    break;
                }
                if((new Date().getTime()-startTime)>EXEC_TIMEOUT)
                {
                    break;
                }
                //check if dialog was found before and still exists
                    tDlg=null;
                    //try to find dialog
                    try
                    {
                        tDlg=new JDialogOperator(new ChooseDialogByTitleAndButtonAndText(DIALOG_TITLE_85870, BUTTON_TITLE_85870,TEXT_85870));
                        new JButtonOperator(tDlg,BUTTON_TITLE_85870).push();
                        tDlg.waitClosed();
                    }
                    catch(Exception ex)
                    {
                        //no dialog
                    }
            }
        }
     }
   
    /**
     * choose JDialog with appropriate exaqct title and appropriate exact button within
     */
    static private class ChooseDialogByTitleAndButtonAndText implements ComponentChooser
    {
        private String title;
        private String btn;
        private String txt;
        
        ChooseDialogByTitleAndButtonAndText(String ttl,String bt,String text)
        {
            title=ttl;
            btn=bt;
            txt=text;
        }
        
        public String getDescription()
        {
            return "find dialog with "+title+" title";
        }
        
        public boolean checkComponent(Component component)
        {
            if(component instanceof java.awt.Dialog)
            {
                if(((java.awt.Dialog)component).getTitle().equals(title))
                {
                    return JButtonOperator.findJButton((java.awt.Dialog)component,btn,true,true)!=null && JTextAreaOperator.findJTextArea((java.awt.Dialog)component,txt,true,true)!=null;
                }
            }
            return false;
        }
    }

}
