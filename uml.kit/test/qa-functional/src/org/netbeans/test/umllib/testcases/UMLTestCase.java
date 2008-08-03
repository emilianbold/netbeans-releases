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
 * UMLTestCase.java
 *
 * Created on March 16, 2006, 12:17 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.test.umllib.testcases;

import java.awt.Component;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.StringComparator;
import org.netbeans.jemmy.operators.WindowOperator;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author sp153251
 */
public class UMLTestCase extends JellyTestCase {
 
    
    /**
     *   XTEST_WORK_DIR - xtest work dir
     */
    public static String XTEST_WORK_DIR = System.getProperty("nbjunit.workdir");
    public static String XTEST_PROJECT_DIR = XTEST_WORK_DIR ;//+ "/sys/data/data";

    
    
    private static Thread save;
    private static Thread exit;
    //srore already reported issues
    private static int[] reportedIssues=new int[1024];
    
    /** Creates a new instance of UMLTestCase */
    /**
     * Need to be defined because of JUnit
     * @param name 
     */
    public UMLTestCase(String name) {
        super(name);
        //maximize if it's possible with api
        JFrame mw=(JFrame)(MainWindowOperator.getDefault().getSource());
        if(mw.getToolkit().isFrameStateSupported(JFrame.MAXIMIZED_BOTH))mw.setExtendedState(JFrame.MAXIMIZED_BOTH);
        else MainWindowOperator.getDefault().maximize();
        //start dialog checkers/ avoid multiple threads creation
        if(exit==null || !exit.isAlive())
        {
            exit=new Thread(new TerminationDialogHandler(TerminationDialogHandler.NON_TERMINATED_PROCESSES_DIALOG,TerminationDialogHandler.NON_TERMINATED_PROCESSES_BUTTON));
            exit.start();
        }
        if(save==null || !save.isAlive())
        {
            save=new Thread(new TerminationDialogHandler(TerminationDialogHandler.SAVE_UNSAVED_DATA_DIALOG,TerminationDialogHandler.SAVE_UNSAVED_DATA_BUTTON));
            save.start();
        }
        JComponent nav=TopComponentOperator.findTopComponent("Navigator",0);
        if(nav!=null)new TopComponentOperator(nav).close();
    }
    
    /**
     * find name for current (executing now) test method
     * should be called directly from method body
     * @return returns current test method name using stacktrace
     */
    protected String getCurrentTestMethodName()
    {
        return getCurrentTestMethodName(0);
    }
    
    /**
     * find name for current (executing now) test method
     * can be called directly from method body or from any method called from test method with appropriate stack shift parameter
     * @param addShift adds shift in stack to get correct method
     * @return returns current test method name using stacktrace
     */
    protected String getCurrentTestMethodName(int addShift)
    {
        int shift=java.lang.System.getProperty("java.version").startsWith("1.5")?1:0;
        return Thread.currentThread().getStackTrace()[3+shift+addShift].getMethodName();
    }
    
    /**
     * find name for current (executing now) test method
     * can be called from any level of stack, methods will find first class folowing test class requirenments
     * test should starts with test, be public, has no argument, return void and should be in class hierarchy with org.netbeans.test.umllib.testcases.UMLTestCase
     * @return returns current class name[0] and test method name[1] using stacktrace
     */
    static public String[] getCurrentTestNamesWithCheck()
    {
        String [] ret=new String[2];
        //
        StackTraceElement[] els=Thread.currentThread().getStackTrace();
        String className=null;
        String methodName=null;
        
        for(int i=0;i<els.length;i++)
        {
            String tmpClassName=els[i].getClassName();
            String tmpMethodName=els[i].getMethodName();
            //first level check - test method should starts with "test"
            if(tmpMethodName.startsWith("test"))
            {
                Class testClass=null;
                Method[] ms=null;
                Method m=null;
                //
                try {
                    testClass=Class.forName(tmpClassName);
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
                if(testClass!=null)
                {
                    //check if class
                    boolean isGoodClass=false;
                    for(Class par=testClass.getSuperclass();par!=null;par=par.getSuperclass())
                    {
                        if(par.getName().equals(UMLTestCase.class.getName()))
                        {
                            isGoodClass=true;
                            break;
                        }
                    }
                    //
                    if(!isGoodClass)continue;
                    //
                    ms=testClass.getMethods();
                    for(int j=0;j<ms.length;j++)
                    {
                        //check only methods with name from stack
                        if(tmpMethodName.equals(ms[j].getName()))
                        {
                            int md=ms[j].getModifiers();
                            //if name and visibility and return type and parameters are correct we 
                            //test methods can be only public
                            //no parameters,
                            if(Modifier.isPublic(md) && ms[j].getParameterAnnotations().length==0 && ms[j].getReturnType().getName().indexOf("void")>-1)
                            {
                                //we got it
                                className=tmpClassName;
                                methodName=tmpMethodName;
                                break;
                            }
                        }
                    }
                    if(className!=null)break;
                }
            }
        }
        ret[0]=className;
        ret[1]=methodName;
        return ret;
    }
    /**
     * find name for currently executing test class, can be called if test class in current stack
     * can be called from any level of stack, methods will find first class folowing test class requirenments
     * public, has no argument, return void and should be in class hierarchy with org.netbeans.test.umllib.testcases.UMLTestCase
     * @param testName - name of test method (if null class with any test method compartible method will be returned)
     * @return returns current class name[0] and test method name[1] using stacktrace
     */
    static public String getCurrentClassNameWithCheck(String testName)
    {
        //
        StackTraceElement[] els=Thread.currentThread().getStackTrace();
        String className=null;
        
        for(int i=0;i<els.length;i++)
        {
            String tmpClassName=els[i].getClassName();
            //first level check
                Class testClass=null;
                Method[] ms=null;
                Method m=null;
                //
                try {
                    testClass=Class.forName(tmpClassName);
                } catch (Exception ex) {
                    //can't find and nothing to do
                }
                catch(NoClassDefFoundError ex)
                {
                   //can't find and nothing to do 
                }
                if(testClass!=null && !Modifier.isAbstract(testClass.getModifiers()))
                {
                    //check if class
                    boolean isGoodClass=false;
                    for(Class par=testClass.getSuperclass();par!=null;par=par.getSuperclass())
                    {
                        
                        if(par.getName().equals(UMLTestCase.class.getName()))
                        {
                            isGoodClass=true;
                            break;
                        }
                    }
                    //
                    if(!isGoodClass)continue;
                    //
                    ms=testClass.getMethods();
                    for(int j=0;j<ms.length;j++)
                    {
                        //check only methods with name from stack or if testcase unknown, check if test method only
                        if(testName==null || testName.equals(ms[j].getName()))
                        {
                            int md=ms[j].getModifiers();
                            //if name and visibility and return type and parameters are correct we 
                            //test methods can be only public
                            //no parameters,
                            if(Modifier.isPublic(md) && ms[j].getParameterAnnotations().length==0 && ms[j].getReturnType().getName().indexOf("void")>-1)
                            {
                                //we got it
                                className=tmpClassName;
                                break;
                            }
                        }
                    }
                    if(className!=null)break;
            }
        }
        return className;
    }
    
    /**
     * class works until MainWindow is showing and wait for different times of dialogs
     * if it find dialog it waits some time and close if it's unhandled by tests after some timeout
     */
    class TerminationDialogHandler implements Runnable
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
        final static public String NON_TERMINATED_PROCESSES_DIALOG="Exit IDE";
        final static public String NON_TERMINATED_PROCESSES_BUTTON="Exit IDE";
        //
        final static public String SAVE_UNSAVED_DATA_DIALOG="Save";
        final static public String SAVE_UNSAVED_DATA_BUTTON="Save All";
        
        /**
         * 
         * @param dlg 
         * @param btn 
         */
        TerminationDialogHandler(String dlg,String btn)
        {
            String tmp[]={btn};
            init(dlg,tmp,DEFAULT_TIMEOUT);
        }
        /**
         * 
         * @param dlg 
         * @param btn 
         * @param waitUntilSave 
         */
        TerminationDialogHandler(String dlg,String btn,long waitUntilSave)
        {
            String tmp[]={btn};
            init(dlg,tmp,waitUntilSave);
        }
        /*
         * if press on i-throws button do not close the window, next time tries to press i+1-th button
         */
        /**
         * 
         * @param dlg 
         * @param btn 
         */
        TerminationDialogHandler(String dlg,String btn[])
        {
            init(dlg,btn,DEFAULT_TIMEOUT);
        }
        /**
         * 
         * @param dlg 
         * @param btn 
         * @param waitUntilSave 
         */
        TerminationDialogHandler(String dlg,String btn[],long waitUntilSave)
        {
            init(dlg,btn,waitUntilSave);
        }
        
        private void init(String dlg,String btn[],long waitUntilSave)
        {
            dialogTitle=dlg;
            buttonTitle=btn;
            timeout=waitUntilSave;
            startTime=new Date().getTime();
        }
        
        public void run() {
            MainWindowOperator mw=null;
            while(true)
            {
                try
                {
                //try dialog every 30 second
                try{Thread.sleep(30*1000);}catch(Exception ex){};
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
                if(tDlg!=null && tDlg.isShowing())
                {
                    //we are going to try another button
                    lastBtnIndex++;
                    if(lastBtnIndex>=buttonTitle.length)lastBtnIndex=0;
                    //
                }
                else
                {
                    tDlg=null;
                    //try to find dialog
                    try
                    {
                        tDlg=new JDialogOperator(new ChooseDialogByTitleAndButton(dialogTitle, buttonTitle));
                    }
                    catch(Exception ex)
                    {
                        //no dialog
                    }
                }
                JButtonOperator tBtn=null;
                try
                {
                    if(tDlg!=null)
                    {
                        JButton tmp=JButtonOperator.findJButton((java.awt.Dialog)(tDlg.getSource()),buttonTitle[lastBtnIndex],true,true);
                        if(tmp!=null)tBtn=new JButtonOperator(tmp);
                    }
                }
                catch(Exception ex)
                {
                    //no save button
                }
                //if there save dialog 
                if(tBtn!=null)
                {
                    //wait timeout seconds
                    try{Thread.sleep(timeout);}catch(Exception ex){ex.printStackTrace();}
                    //if dialog still here save all
                    if(tDlg.isShowing() && tDlg.isDisplayable())
                    {
                        tBtn.push();
                    }
                }
                }
                catch(Exception ex)
                {
                }
            }
        }
     }
   
    /**
     * choose JDialog with appropriate exaqct title and appropriate exact button within
     */
    class ChooseDialogByTitleAndButton implements ComponentChooser
    {
        private String title;
        private String btn[];
        
        /**
         * 
         * @param ttl 
         * @param bt 
         */
        ChooseDialogByTitleAndButton(String ttl,String bt[])
        {
            title=ttl;
            btn=bt;
        }
        
        /**
         * 
         * @return 
         */
        public String getDescription()
        {
            return "find dialog with "+title+" title and buttons "+btn;
        }
        
        /**
         * 
         * @param component 
         * @return 
         */
        public boolean checkComponent(Component component)
        {
            if(component instanceof java.awt.Dialog)
            {
                if(((java.awt.Dialog)component).getTitle().equals(title))
                {
                    boolean all_btns=true;
                    for(int i=0;i<btn.length && all_btns;i++)
                    {
                        all_btns&=JButtonOperator.findJButton((java.awt.Dialog)component,btn[i],true,true)!=null;
                    }
                    return all_btns;
                }
            }
            return false;
        }
    }
    
    /**
     * Backward  compartibility section
     */
    /**
     * @deprecated use failByBug method directly
     */
    public void assertTrue(int bugId,String message,boolean condition)
    {
        if(!condition)failByBug(bugId,message);
    }
    /**
     * @deprecated use failByBug method directly
     */
    public void assertFalse(int bugId,String message,boolean condition)
    {
        if(condition)failByBug(bugId,message);
    }
     /**
     * @deprecated use failByBug method directly
     */
    public void fail(int bugId,String message)
    {
        failByBug(bugId,message);
    }
     /**
     * @deprecated use failByBug method directly
     */
    public void fail(int bugId)
    {
        failByBug(bugId);
    }


}

