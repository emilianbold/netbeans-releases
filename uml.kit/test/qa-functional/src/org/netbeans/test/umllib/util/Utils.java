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
 * Utils.java
 *
 * Created on October 24, 2005, 2:37 PM
 *
 * This Utils used for helper functions aren't directly related with uml (screenshots, logs etc)
 *
 */

package org.netbeans.test.umllib.util;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Date;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JMenuBarOperator;
import org.netbeans.jemmy.operators.JMenuItemOperator;
import org.netbeans.jemmy.operators.JProgressBarOperator;
import org.netbeans.test.umllib.exceptions.UMLCommonException;
import org.netbeans.test.umllib.testcases.UMLTestCase;

public class Utils {
    
    public static final long MAX_WAIT_TIME = 300000;
    public static String WORK_DIR = System.getProperty("nbjunit.workdir");
    
    private static String DEFAULT_SCREENSHOT_PREFIX="beforeTearDown";
    
    /**
     * screenshot is created in directory accessible from xtest result
     * call the method from your test  methods or teardown/setup (i.e/ from your test class)
     * method do nothing if there is problems with java.awt.Robot initialization or with file writing
     * method with default prefix for screenshot file
     * @param testClassName 
     * @param lastTestCase - last testase method name (if you want to place scrrenshot to methods's reprort directory)
     */
    public static void makeScreenShot(String testClassName, String lastTestCase){
        makeScreenShotCustom(testClassName,lastTestCase,DEFAULT_SCREENSHOT_PREFIX);
    }
    
    /**
     * screenshot is created in directory accessible from xtest result
     * call the method from your test  methods or teardown/setup (i.e/ from your test class)
     * method do nothing if there is problems with java.awt.Robot initialization or with file writing
     * by default only ide window is catched
     * @param testClassName 
     * @param lastTestCase - last testase method name (if you want to place scrrenshot to methods's reprort directory)
     * @param customPrefix part of screnshot file name
     */
    
    public static void makeScreenShotCustom(String testClassName, String lastTestCase,String customPrefix){
        makeScreenShotCustom(testClassName,lastTestCase,customPrefix,false);
    }
     /**
     * screenshot is created in directory accessible from xtest result
     * call the method from your test  methods or teardown/setup (i.e/ from your test class)
     * method do nothing if there is problems with java.awt.Robot initialization or with file writing
     * @param testClassName 
     * @param lastTestCase - last testase method name (if you want to place scrrenshot to methods's reprort directory)
     * @param customPrefix part of screnshot file name
     */
    
    public static void makeScreenShotCustom(String testClassName, String lastTestCase,String customPrefix,boolean fullscreen){
        //
        String workdir=System.getProperty("nbjunit.workdir");
        String path=workdir+"/user/" + testClassName+"/"+lastTestCase+"/"+customPrefix+new Date().getTime()+".png";
         //initially limited 1.5 support (workaround for calls from 1.6 java)
            //but start check if test name valid in all cases
            Class testClass=null;
            Method[] ms=null;
            Method m=null;
            //most simple, should start with "test"
            if(lastTestCase!=null && lastTestCase.startsWith("test"))
            {
                try {
                    testClass=Class.forName(testClassName);
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
                if(testClass!=null)
                {
                    ms=testClass.getMethods();
                    for(int i=0;i<ms.length;i++)
                    {
                        int md=ms[i].getModifiers();
                        //test methods can be only public
                        //no parameters,
                        if(Modifier.isPublic(md) && ms[i].getParameterAnnotations().length==0 && ms[i].getReturnType().getName().indexOf("void")>-1)
                        {
                            //we got it
                            m=ms[i];
                            break;
                        }
                    }
                }
            }
            //path correction if didn't find such test method'
            if(m==null)path=workdir+"/user/" + testClassName+"/"+customPrefix+new Date().getTime()+".png";
        java.awt.Robot rbt=null;
        try {
            rbt=new java.awt.Robot();
        } catch (AWTException ex) {
            ex.printStackTrace();
        }
        if(rbt!=null)
        {
            Rectangle bounds=null;
            if(!fullscreen)
            {
                bounds=MainWindowOperator.getDefault().getBounds();
            }
            else
            {
                Dimension size=Toolkit.getDefaultToolkit().getScreenSize();
                bounds=new Rectangle(size);
            }
            BufferedImage img= rbt.createScreenCapture(bounds);
            File saveTo=new File(path);
            try {
                javax.imageio.ImageIO.write(img,"png",saveTo);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        
    }
    
    /**
     * make scrrenshot and stores according to last test case name
     * should be called from test class
     * method with default prefix for screenshot file
     * @param lastTestCase 
     */
    public static void makeScreenShot(String lastTestCase)
    {
        makeScreenShot(lastTestCase,false);
    }
    /**
     * make scrrenshot and stores according to last test case name
     * should be called from test class
     * method with default prefix for screenshot file
     * @param lastTestCase 
     */
    public static void makeScreenShot(String lastTestCase,boolean fullscreen)
    {
        makeScreenShotCustom(lastTestCase,DEFAULT_SCREENSHOT_PREFIX,fullscreen);
    }
    
    /**
     * make scrrenshot and stores according to last test case name
     * should be called from test class
     * @param lastTestCase 
     * @param customPrefix part of screnshot file name
     */
    public static void makeScreenShotCustom(String lastTestCase,String customPrefix){
        makeScreenShotCustom(lastTestCase,customPrefix,false);
    }
    /**
     * make scrrenshot and stores according to last test case name
     * should be called from test class
     * @param lastTestCase 
     * @param customPrefix part of screnshot file name
     */
    public static void makeScreenShotCustom(String lastTestCase,String customPrefix,boolean fullscreen){
        String clName= UMLTestCase.getCurrentClassNameWithCheck(lastTestCase);
        if(clName!=null)makeScreenShotCustom(clName, lastTestCase,customPrefix);
    }
    
    /**
     * screenshot is created in directory accessible from xtest result
     * call the method from any method but test method should be in stack
     * screenshot is paced in report directory fro last executed test* method
     * method do nothing if there is problems with java.awt.Robot initialization or there is no 'test*' method in trace or with file writing
     * method with default prefix for screenshot file
     */
    public static void makeScreenShot()
    {
        makeScreenShot(false);
        makeScreenShotCustom(DEFAULT_SCREENSHOT_PREFIX);
    }
    /**
     * screenshot is created in directory accessible from xtest result
     * call the method from any method but test method should be in stack
     * screenshot is paced in report directory fro last executed test* method
     * method do nothing if there is problems with java.awt.Robot initialization or there is no 'test*' method in trace or with file writing
     * method with default prefix for screenshot file
     */
    public static void makeScreenShot(boolean fullscreen)
    {
        makeScreenShotCustom(DEFAULT_SCREENSHOT_PREFIX,fullscreen);
    }
    
    /**
     * screenshot is created in directory accessible from xtest result
     * call the method from any method but test method should be in stack
     * screenshot is paced in report directory fro last executed test* method
     * method do nothing if there is problems with java.awt.Robot initialization or there is no 'test*' method in trace or with file writing
     * @param customPrefix part of screnshot file name
     */
    public static void makeScreenShotCustom(String customPrefix){
        makeScreenShotCustom(customPrefix,false);
    }
    /**
     * screenshot is created in directory accessible from xtest result
     * call the method from any method but test method should be in stack
     * screenshot is paced in report directory fro last executed test* method
     * method do nothing if there is problems with java.awt.Robot initialization or there is no 'test*' method in trace or with file writing
     * @param customPrefix part of screnshot file name
     */
    public static void makeScreenShotCustom(String customPrefix,boolean fullscreen){
        String workdir=System.getProperty("nbjunit.workdir");
        StackTraceElement[] els=Thread.currentThread().getStackTrace();
        String [] ret=UMLTestCase.getCurrentTestNamesWithCheck();

        if(ret==null || ret[1]==null)
        {
            return;
        }
        
        makeScreenShotCustom(ret[0], ret[1],customPrefix,fullscreen);
    }
    
    
    public static void waitScanningClassPath(){
        
        try{Thread.sleep( 3000 ); }catch(Exception e) {}
        
        long waitTime = 50;
        long waitCount = MAX_WAIT_TIME / waitTime;
        
        for(long time=0; time < waitCount; time++){
            try{Thread.sleep( waitTime ); }catch(Exception e) {}
            
            Object scanning = JProgressBarOperator.findJProgressBar((Container)MainWindowOperator.getDefault().getSource());
            if(scanning == null) { return; }
        }
        throw new UMLCommonException("Scaning isn't finished in "+MAX_WAIT_TIME+" ms");
    }
    
    
    public static void saveAll(){
        try{
            //================  Save All Action  =================
            //SaveAllAction saveAllAction = new SaveAllAction();
            //if(saveAllAction.isEnabled()){
            //    saveAllAction.performMenu();
            //}
            JMenuBarOperator mm=new JMenuBarOperator(MainWindowOperator.getDefault());
            JMenuItemOperator it=mm.showMenuItem("File|Save All");
            if(it.isEnabled())it.pushNoBlock();
            else mm.pushKey(KeyEvent.VK_ESCAPE);
            
        }catch(Exception e){
            System.out.println("Exception in tearDown mwthod: " + e.getMessage());
        }
    }
    
    /**
     * 
     * @param dialogName 
     * @param dialogButton 
     */
    public static void closeDialog(String dialogName, String dialogButton){
        final String name = dialogName;
        final String button = dialogButton;
        new Thread(new Runnable() {
        //java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                long timeoutValDlg = JemmyProperties.getCurrentTimeout("DialogWaiter.WaitDialogTimeout");
                try{
                    Thread.sleep(3000);
                    JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 10000);                        
                    JDialogOperator dialog = new JDialogOperator(name);
                    new JButtonOperator(dialog, button).pushNoBlock();                    
                }catch(Exception e){}
                finally{
                    JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", timeoutValDlg);            
                }
            }
        //});
        }).start();
    }
    
    /**
     * 
     * @param dialogName 
     * @param dialogButton 
     */
    public static void closeTwoDialogs(String dialogName, String dialogButton){
        final String name = dialogName;
        final String button = dialogButton;
        new Thread(new Runnable() {
        //java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                long timeoutValDlg = JemmyProperties.getCurrentTimeout("DialogWaiter.WaitDialogTimeout");
                try{
                    Thread.sleep(3000);
                    JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 10000);                        
                    JDialogOperator dialog = new JDialogOperator(name);
                    new JButtonOperator(dialog, button).pushNoBlock(); 
                    closeDialog(name,button);
                }catch(Exception e){}
                finally{
                    JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", timeoutValDlg);            
                }
            }
        //});
        }).start();
    }
    
    public static void closeSaveDialog(){
        closeTwoDialogs(LabelsAndTitles.DIALOG_TITLE_SAVE, LabelsAndTitles.DIALOG_BUTTON_SAVE_ALL);
    }
    
    public static void closeExitDialog(){
        closeDialog(LabelsAndTitles.DIALOG_TITLE_EXIT_IDE, LabelsAndTitles.DIALOG_BUTTON_EXIT_IDE);
    }
    
    public static void tearDown() {
        releaseModificators();
        saveAll();
        closeSaveDialog();
        closeExitDialog();
    }
    
    private static void releaseModificators() {
        java.awt.Robot rbt=null;
        try {
            rbt=new java.awt.Robot();
        } catch (AWTException ex) {
            ex.printStackTrace();
        }
        if(rbt!=null)
        {
            rbt.keyRelease(KeyEvent.VK_SHIFT);
            rbt.keyRelease(KeyEvent.VK_CONTROL);
            rbt.keyRelease(KeyEvent.VK_ALT);
        }
    }
 
    
    public static void showIDE(){
        showComponents(MainWindowOperator.getDefault().getSource());
    }
    
    
    /**
     * 
     * @param comp 
     */
    public static void showComponents(Component comp){
        showComponents("", comp);
    }

    /**
     * 
     * @param blank 
     * @param comp 
     */
    public static void showComponents(String blank, Component comp){
        showClassHierarchy(blank, comp);
        
        if(comp instanceof Container){
            Container cont = (Container) comp;
            Component[] comps = cont.getComponents();
            
            for(Component c : comps){
                showComponents(blank + " ", c);
            }
            
        }
        
    }

    
    /**
     * 
     * @param obj 
     */
    public static void showClassHierarchy(Object obj){
        showClassHierarchy("", obj);
    }

    /**
     * 
     * @param blank 
     * @param obj 
     */
    public static void showClassHierarchy(String blank, Object obj){
        showClassHierarchy(blank + " ", obj.getClass());
        System.out.println(blank + obj);
    }
    
    
    /**
     * 
     * @param cls 
     */
    protected static void showClassHierarchy( Class cls){
        showClassHierarchy("", cls);
    }
    
    /**
     * 
     * @param blank 
     * @param cls 
     */
    protected static void showClassHierarchy(String blank, Class cls){


        Class superClass = cls.getSuperclass();
        if (superClass!= null){
                showClassHierarchy(blank + "  ", superClass);
        }
        
        Class[] interfaces = cls.getInterfaces();
        if (interfaces!= null){
            for( Class i: interfaces){
                showClassHierarchy(blank + "  ", i);
            }
        }

        System.out.println(blank + "\"" + cls.getName() + "\"");
        
    }

    
    
    
}
