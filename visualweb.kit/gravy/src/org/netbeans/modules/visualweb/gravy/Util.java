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

package org.netbeans.modules.visualweb.gravy;

import java.util.*;
import java.awt.*;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.util.DefaultVisualizer;
import org.netbeans.jemmy.util.NameComponentChooser;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.QueueTool;
import java.awt.event.*;
import org.netbeans.junit.NbTestCase;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jemmy.EventDispatcher;

/**
 * Class with different ancillary utils.
 */
public class Util {
    public static final String
        WINDOWS_OS_FAMILY_NAME = "Windows",
        LINUX_OS_FAMILY_NAME   = "Linux",
        SUN_OS_FAMILY_NAME     = "SunOS",
        MAC_OS_FAMILY_NAME     = "Mac";

    static MainWindowOperator mainWindow;
    static JMenuBarOperator mainMenu;

    /**
     * Get delay scale for tests.
     * @return double Value of delay scale.
     */
    public static double getDelayScale(){
        String s=System.getProperty("Env-RaveTestsDelayScale","1");
        try{
            return Double.parseDouble(s);
        }catch(NumberFormatException e){
            return 1;
        }
    }

    /**
     * Get operation system's name.
     * @return String Name of operation system.
     */
    public static String getOperatingSystemName() {
        return System.getProperty("os.name");
    }

    /**
     * @return True if operation system is from Windows family.
     */
    public static boolean isWindowsOS() {
        String osName = getOperatingSystemName();
        return (osName.toUpperCase().indexOf(
                WINDOWS_OS_FAMILY_NAME.toUpperCase()) > -1);
    }

    /**
     * @return True if operation system is from Sun (Solaris) family.
     */
    public static boolean isSunOS() {
        String osName = getOperatingSystemName();
        return (osName.toUpperCase().indexOf(
                SUN_OS_FAMILY_NAME.toUpperCase()) > -1);
    }

    /**
     * @return True if operation system is from MAC OS X family.
     */
    public static boolean isMacOS() {
        String osName = getOperatingSystemName();
        return (osName.toUpperCase().indexOf(
                MAC_OS_FAMILY_NAME.toUpperCase()) > -1);
    }

    /**
     * Get main window.
     * @return MainWindowOperator.
     */
    public static MainWindowOperator getMainWindow() {
        if(mainWindow == null) {
            Operator.setDefaultComponentVisualizer(new DefaultVisualizer());
            mainWindow = new MainWindowOperator();
        }
        return(mainWindow);
    }

    /**
     * Get main menu.
     * @return JMenuBarOperator Main menu.
     */
    public static JMenuBarOperator getMainMenu() {
        if(mainMenu == null) {
            mainMenu = new JMenuBarOperator(getMainWindow());
        }
        return(mainMenu);
    }

    /**
     * Get main tab.
     * @return JTabbedPaneOperator Main tab.
     */
    public static JTabbedPaneOperator getMainTab() {
        return(new JTabbedPaneOperator(new ContainerOperator(getMainWindow(),
                                       new NameComponentChooser("mainTab"))));
    }

    /**
     * Select tab with specified name.
     * @param tabName Name of the tab.
     */
    public static void selectTab(String tabName){
        new org.netbeans.jellytools.TopComponentOperator(tabName);     
    }

    /**
     * Generate time stamp.
     * @return String Time stamp.
     */
    public static String generateTimeStamp() {
        Calendar calendar = Calendar.getInstance();
        return("" +
               calendar.get(Calendar.YEAR) +
               calendar.get(Calendar.MONTH) +
               calendar.get(Calendar.DAY_OF_MONTH) +
               calendar.get(Calendar.HOUR_OF_DAY) +
               calendar.get(Calendar.MINUTE) +
               calendar.get(Calendar.SECOND));
    }

    /**
     * Close main window.
     */
    public static void closeWindow(){
        (new org.netbeans.jellytools.actions.Action(
            Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/Window").substring(1)/*Menu/Window=&Window*/
            + "|" +
            Bundle.getStringTrimmed("org.netbeans.core.windows.actions.Bundle", "LBL_CloseWindowAction"), /*Close Window*/
            null)).performMenu();
    }

    /**
     * Save all that is opened and changed with main menu.
     */
    public static void saveAll(){
        // find item "Save all"
        JMenuItemOperator saveAll= Util.getMainMenu().showMenuItem(new String[]{"File","Save All"});
        if (saveAll.isEnabled()){
            Util.getMainMenu().pushMenu(Bundle.getStringTrimmed("org.netbeans.core.Bundle", "Menu/File")+"|"+Bundle.getStringTrimmed("com.sun.rave.gravy.project.actions.Bundle", "LBL_SaveAllAction"));
        }
    }

    /**
     * Save all that is opened and changed with API function.
     */
    public static void saveAllAPICall() {
        ((org.openide.actions.SaveAllAction)org.openide.actions.SaveAllAction.
         findObject(org.openide.actions.SaveAllAction.class, true)).performAction();
    }

    /**
     * Get time of execution.
     * @return long Time in format long.
     */
    public static long getExecutionTime() {
        String execTime = System.getProperty("xtest.ide.execution.time");
        if(execTime != null) {
            System.out.println("Property = " + execTime);
            int comps[] = new int[6];
            StringTokenizer token = new StringTokenizer(execTime, ":");
            for(int i = 0; i<6; i++) {
                comps[i] = Integer.parseInt(token.nextToken());
            }
            Calendar calend = Calendar.getInstance();
            calend.set(comps[0], comps[1] - 1, comps[2], comps[3], comps[4], comps[5]);
            long result = calend.getTimeInMillis();
            System.out.print("Execution time: " + calend.toString());
            System.out.println(" = " + result);
            return(result);
        } else {
            throw(new RuntimeException("xtest.ide.execution.time property is empty"));
        }
    }

    /**
     * Get current time .
     * @return long Time in format long.
     */
    public static long getCurrentTime() {
        Calendar calend = Calendar.getInstance();
        long result = calend.getTimeInMillis();
        System.out.print("Current time: " + calend.toString());
        System.out.println(" = " + result);
        return(result);
    }

    /**
     * Save performance data.
     */
    public static void savePerformanceData(NbTestCase test, String id, long startTime, long endTime) {
        System.out.println("Time diff is " + (endTime - startTime));
        System.out.println("Saving it into " + test.getName() + ".perfdata");
        test.getLog(test.getName() + ".perfdata").print(test.getName() + "." + id + " = ");
        test.getLog(test.getName() + ".perfdata").println(endTime - startTime);
    }

    /**
     * Save performance data.
     */
    public static void savePerformanceData(NbTestCase test, String id, long startTime) {
        savePerformanceData(test, id, startTime, getCurrentTime());
    }

    /**
     * Wait for specified time.
     * @param millisec Amount of milliseconds.
     */
    public static void wait(int millisec){
        millisec = new Double(millisec*getDelayScale()).intValue();
        System.out.println("Delay scale() = " + getDelayScale());
        System.out.println("Sleep to "+millisec/1000+" seconds");
        EventDispatcher.waitQueueEmpty();
        try { Thread.sleep(millisec); } catch(Exception e) {}
        EventDispatcher.waitQueueEmpty();
    }

    /**
     * Wait for any keyboard event.
     */
    public static void waitKeyboardEvent() {
        waitKeyboardEvent(1200000);
    }

    /**
     * Wait for any keyboard event with specified delay.
     * @param delay Time for delay in long format
     */
    public static void waitKeyboardEvent(long delay) {
        String interactiveTestMode = System.getProperty("interactive.test.mode");
        if ((interactiveTestMode == null) || (!interactiveTestMode.equalsIgnoreCase("Yes"))) {
            return;
        }
        Waiter kbdWaiter = getKeyboardEventWaiter();       
        kbdWaiter.getTimeouts().setTimeout("Waiter.WaitingTime", delay);
        try {
            kbdWaiter.waitAction(null);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    private static Waiter getKeyboardEventWaiter() {
        final String msgKeyWasPressed = "Key was pressed";
        Waiter kbdWaiter = new Waiter(new Waitable() {
            public Object actionProduced(Object obj) {
                AWTEvent awtEvent = null;
                try {
                    awtEvent = EventTool.getLastEvent();
                } catch(Exception e) {
                    TestUtils.outMsg("+++ Exception was thrown = " + e.getMessage());
                    e.printStackTrace();
                    return e.getMessage();
                }
                Util.wait(1000);
                new QueueTool().waitEmpty();
                if (awtEvent.getID() == KeyEvent.KEY_RELEASED) {
                    TestUtils.outMsg("+++ " + msgKeyWasPressed);
                    return msgKeyWasPressed;
                }
                return null;
            }
            public String getDescription() {
                return "Waiting for key press...";
            }
        });       
        return kbdWaiter;
    }
}
