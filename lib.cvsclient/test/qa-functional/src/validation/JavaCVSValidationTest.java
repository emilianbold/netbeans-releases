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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
 * JavaCVSValidationTest.java
 *
 * Created on December 12, 2001, 11:09 AM
 * Last Update: $Date$
 */

package validation;

import java.awt.Color;
import java.awt.Container;
import java.lang.reflect.Method;
import java.io.*;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyleConstants.ColorConstants;
import javax.swing.tree.TreePath;
import junit.framework.*;

//import org.netbeans.jemmy.*;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TestOut;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.util.PNGEncoder;
import org.netbeans.junit.*;

// New Jelly2 lib
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.actions.*;
import org.netbeans.jellytools.nodes.*;
import org.netbeans.jellytools.properties.*;
import org.netbeans.jellytools.modules.javacvs.actions.*;
import org.netbeans.jellytools.modules.javacvs.output.JCVSDiffOutputOperator;
import org.netbeans.jellytools.modules.javacvs.output.LogOutputOperator;
import org.netbeans.jellytools.modules.javacvs.output.StatusOutputOperator;
import org.netbeans.jellytools.modules.vcscore.actions.*;
import org.netbeans.jellytools.modules.vcscore.VCSGroupsFrameOperator;
import org.netbeans.jellytools.modules.vcscore.VersioningFrameOperator;
import org.netbeans.jellytools.modules.vcscore.GroupVerificationOperator;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;

//import org.netbeans.jellytools.properties.editors.*;
//import org.netbeans.jellytools.modules.javacvs.*;

// Old Jelly lib
//import org.netbeans.test.oo.gui.jam.*;
//import org.netbeans.test.oo.gui.jello.*;
//import org.netbeans.test.oo.gui.jelly.*;
import org.netbeans.test.oo.gui.jelly.vcscore.*;
//import org.netbeans.test.oo.gui.jelly.vcscore.wizard.*;
import org.netbeans.test.oo.gui.jelly.javacvs.*;

import org.openide.options.SystemOption;
import org.netbeans.modules.cvsclient.JavaCvsSettings;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;

/** XTest / JUnit test class performing validation of JavaCVS module
 *
 * @author <a href="mailto:adam.sotona@sun.com">Adam Sotona</a>
 * @see #suite JavaCVS validation testsuite definition
 * @version 1.0
 */
public class JavaCVSValidationTest extends JellyTestCase {
    
    static util.History his;
    static String path;
    static String repositoryprefix;
    static String CVSlbl = Bundle.getStringTrimmed("org.netbeans.modules.javacvs.Bundle", "JavaCvsFileSystem.validFilesystemLabel");
    static String filesystem;
    static FileSystem testedJavaCVSFS;

    /* Site specific properties */
    final String SERVER = System.getProperty("SERVER");
    final String REPOSITORY = System.getProperty("REPOSITORY");
    final String SERVERTYPE = System.getProperty("SERVERTYPE");
    final String USER = System.getProperty("USER");
    final String PASSWORD = System.getProperty("PASSWORD");
    final String PORT = System.getProperty("PORT");
    final String MODULE = System.getProperty("MODULE");
    
    static final String WORKDIR = "JavaCVSwork";
    static final String TESTDIR = "testdir";
    static final String TESTFILE = "testfile";
    static final String TESTFILEPROPERTY = "javacvs.validation.test.file";

    static final String VERSIONINGMENU = Bundle.getStringTrimmed("org.netbeans.modules.vcscore.actions.Bundle", "Menu/Versioning");
    static final String MOUNTVCS = Bundle.getStringTrimmed("org.netbeans.modules.vcscore.actions.Bundle", "CTL_MountActionName");
    static String CVS = Bundle.getStringTrimmed("org.netbeans.modules.vcscore.wizard.mountcvs.Bundle", "Templates/Mount/VCS/org-netbeans-modules-vcscore-wizard-mountcvs-CvsMountFS.settings");
    static String strCmdAction;
    static File testFile = null;
    static MainWindowOperator.StatusTextTracer sbt = MainWindowOperator.getDefault().getStatusTextTracer();    

    static final boolean capture = true;
    PrintStream psRef;
    PrintStream ps;
//    PrintStream pstreams [] = {ps , psRef};       it's not neccessary anymore. See tearDown()
    
    APIController api;
    
    /** store previously used comparator
     */
    Operator.StringComparator oldComparator = Operator.getDefaultStringComparator();
    
    /** constructor required by JUnit
     * @param testName method name to be used as testcase
     */
    public JavaCVSValidationTest(String testName) {
        super(testName);
    }
    
    /** method used for explicit testsuite definition<br>
     * JavaCVS validation suite is order-sensitive <br>
     * Contains also <i>commented</i> TRY-CATCH BLOCK
     * with set longer timeouts. This should be uncommented
     * on slower machines.
     *
     *
     * @return JavaCVS validation testsuite
     * @see #testConnectionManager 1.testConnectionManager
     * @see #testMount <br>2.testMount
     * @see #testHistory <br>3.testHistory
     * @see #testCreateFile <br>4.testCreateFile
     * @see #testAdd <br>5.testAdd
     * @see #testCommit <br>6.testCommit
     * @see #testDiff <br>7.testDiff
     * @see #testVCSGroups <br>8.testVCSGroups
     * @see #testVersioningExplorer <br>9.testVersioningExplorer
     * @see #testBranch <br>10.testBranch
     * @see #testUpdateBranch <br>11.testUpdateBranch
     * @see #testCommitToBranch <br>12.testCommitToBranch
     * @see #testMerge <br>13.testMerge
     * @see #testStatus <br>14.testStatus
     * @see #testLog <br>15.testLog
     * @see #testAnnotate <br>16.testAnnotate
     * @see #testUnmount <br>17.testUnmount
     * @see #testUndock <br>18.testUndock
     */
    public static junit.framework.Test suite() {
        TestSuite suite = new NbTestSuite();
        suite.addTest(new JavaCVSValidationTest("testConnectionManager"));
        suite.addTest(new JavaCVSValidationTest("testMount"));
        suite.addTest(new JavaCVSValidationTest("testHistory"));
        suite.addTest(new JavaCVSValidationTest("testCreateFile"));
        suite.addTest(new JavaCVSValidationTest("testAdd"));
        suite.addTest(new JavaCVSValidationTest("testCommit"));
        suite.addTest(new JavaCVSValidationTest("testDiff"));
        suite.addTest(new JavaCVSValidationTest("testVCSGroups"));
        suite.addTest(new JavaCVSValidationTest("testVersioningExplorer"));
        suite.addTest(new JavaCVSValidationTest("testBranch"));
        suite.addTest(new JavaCVSValidationTest("testUpdateBranch"));
        suite.addTest(new JavaCVSValidationTest("testCommitToBranch"));
        suite.addTest(new JavaCVSValidationTest("testMerge"));
        suite.addTest(new JavaCVSValidationTest("testStatus"));
        suite.addTest(new JavaCVSValidationTest("testLog"));
        suite.addTest(new JavaCVSValidationTest("testAnnotate"));
        suite.addTest(new JavaCVSValidationTest("testUnmount"));
        suite.addTest(new JavaCVSValidationTest("testUndock"));
        return suite;
    }
    
    /** Use for internal test execution inside IDE
     * @param args command line arguments
     */
    public static void main(java.lang.String[] args) throws IOException {
        /* Execution Xtestu using ANT
        ant -Dxtest.userdata.propertyfile=/home.local/danielm/cvs/ide/javacvs/test/qa-functional/src/site-specific/JavaCVSValidationTest_home.properties
         */

        /* Preparing xtest.userdate properties for internal usage
         */
        InputStream inStream = JavaCVSValidationTest.class.getResourceAsStream("/site-specific/JavaCVSValidationTest_home.properties");
        Properties pro = System.getProperties();
        pro.load(inStream);
        Set set = pro.keySet();
        String[] str = (String[]) set.toArray(new String[set.size ()]);

        if (str != null)
            for (int i = 0; i < str.length; i ++)
                if (str[i].startsWith ("xtest.userdata"))
                    System.setProperty(str[i].substring (str[i].indexOf ('|') + 1), System.getProperty (str[i]));
        pro.list(System.out);
        
        junit.textui.TestRunner.run(suite());
    }

    /** method called before each testcase<br>
     * sets long Jemmy WaitComponentTimeout and switches JavaCVS UI mode
     */
    protected void setUp() {
        new EventTool().waitNoEvent(2000);
        System.out.println(getName());
        ((JavaCvsSettings)SystemOption.findObject(JavaCvsSettings.class, true)).setUiMode(1);
//        JellyProperties.setJemmyDebugTimeouts();
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout",20000);
	api = new APIController();
        api.setOut (new PrintWriter (System.out, true));
        //api.setOut (new PrintWriter (getLog(), true));
        ps = getLog ("jemmy.log");
        psRef = getRef();
        
//        JellyProperties.setJemmyOutput(new PrintWriter(ps, true), new PrintWriter(ps, true));
        JemmyProperties.getCurrentOutput().setAutoFlushMode(true);
        
        /* Seems that Robot dispatching is not safe on my linux (RedHat7.1, KDE2.1.1)
         * Test usualy fails at testMount() method when pushMenuNoBlock on Verisoning menu
         * and if it goes through the all test. My WindowMAnager is confused. I have to restart X-server :-(
         */
        //JemmyProperties.setCurrentDispatchingModel(JemmyProperties.ROBOT_MODEL_MASK);
//      JemmyProperties.setCurrentDispatchingModel(JemmyProperties.QUEUE_MODEL_MASK);
        log("setUp(DispatchModel)="+JemmyProperties.getCurrentDispatchingModel());
        
        /* Jemmy & Gnome workarround:
         * Usually happens on Linux and Gnome that just activated window doesn't have focus.
         * Calling this method :
         * ComponentOperator.setDefaultComponentVisualizer(new MouseVisualizer(MouseVisualizer.TOP, 0.5, 10, false);
         * or setting the following JellyProperty should help it:
         */
        //JellyProperties.setLinuxWindowActivation(true);
        if (testedJavaCVSFS != null )
            initHistory(testedJavaCVSFS);

    }
    
    /** method called after each testcase<br>
     * resets Jemmy WaitComponentTimeout
     */
    protected void tearDown() {
        JemmyProperties.setCurrentOutput(TestOut.getNullOutput());
/*        it is automaticaly flushed after returning into NbTestCase
        pstreams[0] = ps;
        pstreams[1] = psRef;

        for (int i=0 ; i < pstreams.length; i++) {
            pstreams[i].flush();
            pstreams[i].close ();
        }
*/
//        if ( ps !=   null ) ps.close();
//        if ( psRef != null) ps.close();
    }
    
    void fail(Exception e) {
        e.printStackTrace(getLog());
        if (capture) {
            try {
                PNGEncoder.captureScreen(getWorkDirPath()+"/screen.png");
            } catch (IOException ioe) {}
        }
        throw new AssertionFailedErrorException(e);
    }

    /** Method called in case of fail or error just after screen shot and XML dumps. <br>
     * Override this method when you need to be notified about test failures or errors 
     * but avoid any exception to be throwed from this method.<br>
     * super.failNotify() does not need to be called because it is empty.
     * @param reason Throwable reason of current fail */
    protected void failNotify(Throwable reason) {
        System.out.println("jsi v failNotify()");
        sbt.printStatusTextHistory(ps);
        System.out.println(sbt.getStatusTextHistory().toString());
        if (his != null)
            his.print ();
    }
    
    protected void dumpDiffGraphical (TopComponentOperator tco) {
        JEditorPaneOperator p1 = new JEditorPaneOperator (tco, 0);
        JEditorPaneOperator p2 = new JEditorPaneOperator (tco, 1);
        psRef.println ("==== Text - Panel 1 ====");
        psRef.println (p1.getText ());
        psRef.println ("==== Text - Panel 2 ====");
        psRef.println (p2.getText ());
        StyledDocument sd1 = (StyledDocument) p1.getDocument();
        StyledDocument sd2 = (StyledDocument) p2.getDocument();
        psRef.println ("==== Colors - Panel 1 ====");
        dumpColors(sd1);
        psRef.println ("==== Colors - Panel 2 ====");
        dumpColors(sd2);
    }

    protected static final Color annoWhite = new Color (254, 254, 254);
    protected static final Color annoGreen = new Color (180, 255, 180);
    protected static final Color annoBlue = new Color (160, 200, 255);
    protected static final Color annoRed = new Color (255, 160, 180);
    
    protected void dumpColors (StyledDocument sd) {
        int b = sd.getLength();// * 2;
//        psRef.println ("Len: " + b);
        for (int a = 0; a < b; a ++) {
            Style st = sd.getLogicalStyle(a);
            if (st == null)
                continue;
            Color col = (Color) st.getAttribute(ColorConstants.Background);
            String str;
            if (annoWhite.equals (col))
                str = "White";
            else if (annoGreen.equals (col))
                str = "Green";
            else if (annoBlue.equals (col))
                str = "Blue";
            else if (annoRed.equals (col))
                str = "Red";
            else
                str = col.toString ();
            psRef.println ("Pos: " + a + " ---- " + str);
            a ++;
        }
    }

    File getJavaCVSWork() throws IOException {
        return new File(getWorkDir().getParentFile(), WORKDIR);
    }
    
    File getTestFile() throws IOException {
        return (testFile==null)?new File(System.getProperty(TESTFILEPROPERTY)):testFile;
    }
    
    void setTestFile(File f) {
        testFile=f;
        System.setProperty(TESTFILEPROPERTY, f.getAbsolutePath());
    }
    
    void setClassVariable() {
        try {
            path = getJavaCVSWork().getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace(); 
        }
        filesystem = CVSlbl + path;
    }
    
    FileSystem getTestedJavaCVSFS(String filesystem) {
//        setClassVariable();
        Enumeration enFS = Repository.getDefault().getFileSystems();
        FileSystem tmpFS;
        while(enFS.hasMoreElements()) {
            tmpFS = (FileSystem) enFS.nextElement();
            if (tmpFS.getDisplayName().equals(filesystem)) {
                testedJavaCVSFS = tmpFS;
                return tmpFS;
            }
        }
        return null;
    }
    
    /** Inicializing of History object
     */
    void initHistory(FileSystem testedJavaCVSFS) {
        his = new util.History(testedJavaCVSFS, getLog ());
        his.setBreakpoint (null);
        his.switchToJCVS();
    }
    
    /** invokes Connection Manager window from Versioning menu, adds new record and performs login
     * @throws Exception any unexpected exception thrown during test
     * @see #testMount next: testMount
     */
    public void testConnectionManager() {
        this.clearTestStatus();
        this.startTest();
        /* Please, don't change the following line !!!
         * necessary for treating result of the test
         */
        log("witchBRANCH", "This test suit was checkouted as the *release34*");
        try {
            log("Trying to push Main Menu:\n"+VERSIONINGMENU+"|"+Bundle.getStringTrimmed("org.netbeans.modules.cvsclient.login.Bundle", "LoginManagerAction.dialogTitle")+"\n");
            new JCVSConnectionManagerAction().perform();
            log("Trying to invoke CVS Connection Manager dialog\n");
            CVSConnectionDialog conn = new CVSConnectionDialog();
            
            conn.activate();
            log("Trying to find and click 'Add' button to open Login Manager Dialog\n");
            CVSConnectionDialog.AddNewRootDialog add = conn.add();
            log("Trying to fill-in the dialog:\n"+SERVERTYPE+","+ SERVER+","+ USER+","+ PORT+","+ REPOSITORY+"\n");
            add.setAddNewRootOptions(SERVERTYPE, SERVER, USER, PORT, REPOSITORY);
            add.compareAddNewRootOptions(SERVERTYPE, SERVER, USER, PORT, REPOSITORY);
            add.verify();
            log("Trying to find and click 'OK' button\n");
            add.ok();
            CVSConnectionDialog.LoginToPserverDialog login = new CVSConnectionDialog.LoginToPserverDialog();
            log("Trying to write PASSWD in to the textfield: "+PASSWORD+"\n");
            login.setJPasswordField(PASSWORD);
            login.verify();
            log("Trying to find and click 'Login' button\n");
            login.login();
            int i=0;
            while ((i++<30)&&(login.isVisible())) {
                Thread.sleep(1000);
            }
            log("Trying to verify the login dialog\n");
            conn.verify();
            endTest();
            log("Trying to find 'Close' button\n");
            conn.close();
        } catch (Exception e) {
            fail(e);
        }
    }
    
    /** invokes CVS Wizard from Versioning menu, sets workdir, connection settings and performs initial checkout of module
     * @throws Exception any unexpected exception thrown during test
     * @see #testConnectionManager depends on: testConnectionManager
     * @see #testHistory <br>next: testHistory
     */
    public void testMount() {
        startTest();
        try {
            log("JavaCVSWork dir is:\n"+getJavaCVSWork()+"\n");
            File f = getJavaCVSWork();
            assertTrue(f.mkdirs());

//            log("Nasty workaround for bug #31026");
            log("Trying to push MainMenu:\n"+VERSIONINGMENU+"|"+MOUNTVCS+"|"+CVS+"\n");
            new JCVSMountAction().perform();

/*            //---Hacking bug #31026---
    //Checking if it was fixed or not
    new Action ("Versioning|Mount Version Control", null).performMenu ();       //necessery for converting JmenuItem to Jmenu
    JMenuBarOperator jmbo = MainWindowOperator.getDefault().menuBar();
    javax.swing.JMenu jm = ((javax.swing.JMenuBar)jmbo.getSource()).getMenu(6); //Should be Versioning
    log("MainMenu je ... |"+ jm.getText()+"|");

    JMenuOperator jmo =  new JMenuOperator(jm);
    log("jmo ma MenuItem? "+ jmo.getItemCount());
    
    int ii = 0;
    for(int i = 0; i < jmo.getItemCount(); i++) {
       if(((javax.swing.JMenu)jmo.getSource()).getItem(i) != null) {
           log( i + " |"+ ((javax.swing.JMenu)jmo.getSource()).getItem(i).getText() +"|");
           if ( ((javax.swing.JMenu)jmo.getSource()).getItem(i).getText().equals("Mount Version Control") ) ii = i;
       }
    }
    
    jmo = new JMenuOperator((javax.swing.JMenu) jmo.getItem(ii));
    log("Menu je ("+ii+") ... |"+ jmo.getText()+"|");
    log("jmo ma Item? "+ jmo.getItemCount());
    for(int i = 0 ; i < jmo.getItemCount(); i++) {
        log(" ... " + i + "|" + jmo.getItem(i).getText());
    }
    //End of check
*/            
            
/*    log("Trying to push MainMenu:\n"+VERSIONINGMENU+"|"+MOUNTVCS+"\n");
    final String VERSIONING_MENU1 = Bundle.getStringTrimmed("org.netbeans.modules.vcscore.actions.Bundle", "Menu/Versioning");
    final String MOUNT_VERSIONING_CONTROL1 = Bundle.getStringTrimmed("org.netbeans.modules.vcscore.actions.Bundle", "CTL_MountActionName");
    final String CVS_TYPE1 = Bundle.getStringTrimmed("org.netbeans.modules.vcscore.wizard.mountcvs.Bundle", "Templates/Mount/VCS/org-netbeans-modules-vcscore-wizard-mountcvs-CvsMountFS.settings");

    new ActionNoBlock(VERSIONING_MENU1+"|"+MOUNT_VERSIONING_CONTROL1, null).perform();
    new ActionNoBlock("Tools", null).perform();
    
    org.netbeans.jemmy.Timeout tm = new org.netbeans.jemmy.Timeout("tmpTM", 10000);
    tm.sleep();

    new JCVSMountAction().perform();
            //---end of Hack---
*/    
            
            
            NewWizardCVSDialog2 wizardPage1 = new NewWizardCVSDialog2();
            log("Trying to fill-in the first Panel of Wizard\n");
            wizardPage1.setWorkingDirectoryTxt(f.getAbsolutePath());
            wizardPage1.verify();
            wizardPage1.next();
            log("Trying to fill-in the second Panel of Wizard\n");
            NewWizardCVSDialog4 wizardPage2 = new NewWizardCVSDialog4();
            wizardPage2.setServerName(SERVER);
            wizardPage2.setUserName(USER);
            wizardPage2.setRepository(REPOSITORY);
            wizardPage2.verify();
            wizardPage2.next();
            log("Trying to fill-in the third Panel of Wizard\n");
            NewWizardCVSDialog5 wizardPage3 = new NewWizardCVSDialog5();
            wizardPage3.verify();
            wizardPage3.next();
            log("Trying to fill-in the fourth Panel of Wizard\n");
            NewWizardCVSDialog6 wizardPage4 = new NewWizardCVSDialog6(SERVER);
            wizardPage4.verify();
            wizardPage4.next();
            log("Trying to fill-in the fifth Panel of Wizard\n");
            NewWizardCVSDialog6a wizardPage5 = new NewWizardCVSDialog6a();
            log("Checking up Initial checkout!!!\n");
            wizardPage5.checkCheckOutACopyOfTheRepositoryFilesToYourWorkingDirectory(true);
            wizardPage5.verify();
            wizardPage5.finish();

            log("now the Checkout dilaog should comes up...\n");
            CheckoutDialog checkout=new CheckoutDialog();

            log("DEADLOCK when invoking 'Select Module' dialog is fixed now,\n"+
                "see issue #29464\nhttp://www.netbeans.org/issues/show_bug.cgi?id=29464 ,\n"+
                "please if during automat test DEADLOCK occures again, please REOPEN this bug!");
            
            log("Trying to open ModuleSelector dialog\n");
            CheckoutDialog.ModuleChooserDialog chooser=checkout.selectModule();
            log("Trying to find desired module: "+MODULE);
            int i=chooser.tabModulesTable().findCellRow(MODULE, new Operator.DefaultStringComparator (true, true));
            log("Desired module on line "+i+" has been found...selecting it\n");
            chooser.tabModulesTable().setRowSelectionInterval(i,i);
            chooser.verify();
            chooser.ok();

            // instead of it is used typing module into a text field
            /*
            checkout.txtCheckout().clearText();
            checkout.txtCheckout().typeText(MODULE);
            */
            
            checkout.verify();
            
            log("The outpuWindow should open now...\n");
            
            /* This piece of code is a workaround for problems with losted HierarchyEvent
             * There is an jemmy's issue http://www.netbeans.org/issues/show_bug.cgi?id=32466
             * but later appeared as a JDK bug: http://developer.java.sun.com/developer/bugParade/bugs/4924516.html
             */
            TabbedOutputOfVCSCommandsFrame out;
            try {
                EventTool.removeListeners();
                log("Trying to find and click 'Run command' button\n");
                checkout.runCommand();
                out = new TabbedOutputOfVCSCommandsFrame();
            } finally {
                EventTool.addListeners();
            }
            // end of workaround issue #32466
            
            
            endTest();
            setClassVariable();
            initHistory(getTestedJavaCVSFS(filesystem)); // just only for this TC. For the rest it is initialized in setUp() method

            out.verify();
//            endTest();
            
            log("Trying to find 'Close' button");
            out.close();
            log("Closed...\n");
        } catch (Exception e) {
            fail(e);
        }
    }
    
    /** looks for CVS command checkout history record under Explorer's Runtime tab, opens record and tests if "Finished" info is present
     * @throws Exception any unexpected exception thrown during test
     * @see #testMount depends on: testMount
     * @see #testCreateFile <br>next: testCreateFile
     */
    public void testHistory() {
        startTest();
        String path;
        String filesystem;
        CVS = Bundle.getStringTrimmed("org.netbeans.modules.javacvs.Bundle", "JavaCvsFileSystem.validFilesystemLabel");
        String cmdStatus = Bundle.getStringTrimmed("org.netbeans.modules.cvsclient.commands.Bundle", "CTL_Status_Done");
        boolean isCommandSuccessed;
        String status;
        try {
            path = getJavaCVSWork().getAbsolutePath();
            filesystem = CVS + path;
            /*Ask Milos How it's construct the command string which is in RunTime TAB
             *                       # RuntimeCommandNode:
             *                       # {0} .. Command's display name
             *The ResourceBundle is: org.netbeans.modules.vcscore.runtime.Bundle
             *The key is:            RuntimeCommandNode.Description=VCS Command ''{0}''
             *on the line no:        70
             *
             *Property-Command name: Checkout
             *The ResourceBundle is: org.netbeans.modules.javacvs.commands.Bundle
             *The key is:            CvsCheckout.name=Checkout
             *on the line no:        50
             */
            String command = "checkout " + MODULE;

            log("Trying to switch to RunTime TAB via API\n");
            APICommandsHistory history = new APICommandsHistory(api);
            
            log("Creating RunTime TAB Jemmy handler\n");
            CommandsHistory commandsHistory = new CommandsHistory();
            
            log("Verifying if command succeded...");
            isCommandSuccessed = history.isCommandSuccessed(filesystem, command, cmdStatus);
            
            log("Checkin' if command:\n  |"+command+"|\non this filesystem:\n  "+filesystem+
                "\nsuccessed: "+isCommandSuccessed+"\n");
            Thread.sleep(500L);
            assertTrue("\nError: cvs command:\n  |"+command+
                "|\nnode hasn't been found in RunTime TAB under the specified FS:\n  "
                +filesystem+"|\n", isCommandSuccessed);
            
            log("Comparin' command status:\n  |" +command+ "|\n  |"+ cmdStatus +"|\non this filesystem:\n  "+filesystem+"\n");
            Thread.sleep(500L);
            assertTrue("\nError: Wrong status of\n|" + command + "|\ncommand.",
                commandsHistory.compareStatus(cmdStatus, filesystem, command));
            
            log("Comparin' command name:\n  |" +command+ "|\non this filesystem:\n  "+filesystem+"\n");
            Thread.sleep(500L);
            assertTrue("\nError: command name of\n|" + command + "|\ncommand.",
                commandsHistory.compareCommandName("Checkout", filesystem, command));
            

            /* This piece of code is a workaround for problems with losted HierarchyEvent
             * There is an jemmy's issue http://www.netbeans.org/issues/show_bug.cgi?id=32466
             * but later appeared as a JDK bug: http://developer.java.sun.com/developer/bugParade/bugs/4924516.html
             */
            TabbedOutputOfVCSCommandsFrame out;
            try {
                EventTool.removeListeners();
                log("Trying to open output of command...");
                out = commandsHistory.viewOutput(filesystem, command);
            } finally {
                EventTool.addListeners();
            }
            // end of workaround issue #32466
            
            log("succeeded, OPENED :)\n");
            
            status = Bundle.getString("org.netbeans.modules.vcscore.commands.Bundle", "CommandExitStatus.finished");
            log("Comparin' statuses in the textfiled:\n  |" +status+ "|\n");
            Thread.sleep(500L);
            assertTrue("\nError: command status is different from expected value:\n  "
                +status+"|\n",  out.compareStatusTextFld(status));
            out.verify();
            
            log("Trying to find 'Close' button\n");
            out.close();
            endTest();
        } catch (Exception e) {
            fail(e);
        }
    }
    
    String getTestFileObjectName() throws IOException {
        final String file=getTestFile().getName();
        return file.substring(0,file.lastIndexOf("."));
    }
    
    String getTestDirNodePath() throws IOException {
        return "CVS "+getJavaCVSWork().getAbsolutePath()+"|"+MODULE+"|"+getTestFile().getParentFile().getName();
    }

    String getTestDirObjectName() throws IOException {
        return getTestFile().getParentFile().getName();
    }
    
    /** creates new unique testdirectory under workdir, creates new testfile indside and verifies that both are present under Explorer's Filesystems tab with attribute [Local]
     * @throws Exception any unexpected exception thrown during test
     * @see #testHistory depends on: testHistory
     * @see #testAdd <br>next: testAdd
     */
    public void testCreateFile() {
        clearTestStatus();
        startTest();
        try {
//            MainWindowOperator.StatusTextTracer sbt = MainWindowOperator.getDefault().getStatusTextTracer();
            
            log("Trying to find(create) a unique dir/file on a disk: \n"+TESTDIR+", "+getJavaCVSWork()+", "+MODULE+"\n");
            File f=File.createTempFile(TESTDIR, "", new File(getJavaCVSWork(), MODULE));
            assertTrue(f.delete());
            
            repositoryprefix = "/" + MODULE + "/" + f.getName();
            log("Trying to make directory: \n"+f.toString()+"\n");
            assertTrue(f.mkdir());
            
            log("Trying to create testing file: "+f+" ,"+TESTFILE+".java\n");
            setTestFile(new File(f, TESTFILE+".java"));
            FileOutputStream out = new FileOutputStream(getTestFile());
            out.write(("class "+TESTFILE+" {}\n").getBytes());
            
            log("Trying to close the FileOutputStream\n");
            out.close();

            log("Trying to open new Explorer and imediately locate and switch to FS TAB...\n");
            Node rootNode = ExplorerOperator.invoke().repositoryTab().getRootNode();//selectPageFilesystems();
            
            log("Creating a node on this filesystem:\n" + "CVS "+getJavaCVSWork().getAbsolutePath()+"\n");
            Node node = new Node(rootNode, "CVS "+getJavaCVSWork().getAbsolutePath());
            new JCVSRefreshAction().perform(node);
            
            log("Creating a node on this filesystem:\n" + "CVS "+getJavaCVSWork().getAbsolutePath()+"|"+MODULE+"\n");
            node = new Node(rootNode, "CVS "+getJavaCVSWork().getAbsolutePath()+"|"+MODULE);
            log("The node is:\n  "+ node.getParentPath()+"\n  "+node.getPath()+"\n  "+node.getText()+"\n  "+node.toString()+"\n");
            log("Trying to go to PopupMenu:\n"+"CVS|Refresh\non this filesystem:\n  " + node.getPath() +"\n");
/*            
            Operator.DefaultStringComparator comparator = new Operator.DefaultStringComparator(true, true);
            JMenuBarOperator
//            MainWindowOperator.getDefault().menuBar ().setDef
//            Operator.setDefaultStringComparator(comparator);
            Action ac = new Action(VERSIONINGMENU+"|"+CVS+"|Refresh", "CVS|Refresh");
            ac.setComparator(comparator);
            ac.performMenu(node);
            Operator.setDefaultStringComparator(oldComparator);
*/            

/*            Operator.DefaultStringComparator comparator = new
            Operator.DefaultStringComparator(true, true);
            Action ac = new Action("Versioning|CVS|Refresh", null);
            ac.setComparator(comparator);
            ac.performMenu(node);            
 */
            sbt.start();
            System.out.println("===BACHA!!! REFRESHIM ===");
            new JCVSRefreshAction().perform(node);
            sbt.waitText("CVS Client Running Command: Refresh", true);
            sbt.waitText("Finished: Refresh", true);
            
            
//            exp.tree().callPopupOnPath("CVS|Refresh", "CVS "+getJavaCVSWork().getAbsolutePath()+"|"+MODULE);
//            exp.pushPopupMenu("CVS|Refresh", "CVS "+getJavaCVSWork().getAbsolutePath()+"|"+MODULE);
            
            log("Trying to select a node:\n  "+rootNode.getPath() +"\n  "+
                getTestDirNodePath()+" [Local]|"+getTestFileObjectName()+" [Local]\n");
            node = new Node(rootNode, getTestDirNodePath()+" [Local]|"+getTestFileObjectName()+" [Local]");
            node.select();
            
//            exp.selectNode(getTestDirNodePath()+" [Local]|"+getTestFileObjectName()+" [Local]");
            endTest();
        } catch (Exception e) {
            fail(e);
        }
    }
    
    /** invokes Add dialog from testdir's popup menu, runs add command recursively and verifies that testfile has now attributes [LAdded; New] under Explorer
     * @throws Exception any unexpected exception thrown during test
     * @see #testCreateFile depends on: testCreateFile
     * @see #testCommit <br>next: testCommit
     */
    public void testAdd() {
        startTest();
        try {
//            Explorer exp=new Explorer();
            log("Explorer should be opened..\n switching to FS TAB\n");
//            exp.switchToFilesystemsTab();
            Node node = new Node(ExplorerOperator.invoke().repositoryTab().getRootNode(),
                getTestDirNodePath()+" [Local]");
            
            log("Trying to push popup menu 'CVS|Add...' on the node: "+getTestDirNodePath()+" [Local]\n");
            new JCVSAddAction().perform(node);
            
            
            log("Trying to open Add dialog\n");
            AddDialog add=new AddDialog();
            
            log("Checking up checkBox: 'Recursively'\n");
            add.check(add.cbRecursively(), true);
            
            log("Verifiing the dialog\n");
            add.verify();
            
            log("Trying to push the 'Run Command=OK' buttonn\n");
            add.runCommand();
            
            OutputOfVCSCommandsFrame out=new OutputOfVCSCommandsFrame();
            log("Verifying the OutputOfCommand dialog\n");
            out.verify();
            
            log("Trying to push the 'Close' button\n");
            out.close();
            
            log("TCs which are founding nodes using fileStatuses will probably fails\n"+
                "BECAUSE OF P1 #25987 BUG IN OPENIDE, see http://www.netbeans.org/issues/show_bug.cgi?id=25987");
            log("Trying to select a file node:\n "+getTestDirNodePath()+"|"+getTestFileObjectName()+" [LAdded; New]");
            Thread.sleep(1000L);    //Rather wait sometime to give ide  a possibility to refresh its nodes...
            new Node((new RepositoryTabOperator()).getRootNode(),
                getTestDirNodePath()+"|"+getTestFileObjectName()+" [LAdded; New]").select();
//            exp.selectNode(getTestDirNodePath()+"|"+getTestFileObjectName()+" [LAdded; New]");
            log("Selected... :-)\n");
            endTest();
        } catch (Exception e) {
            fail(e);
        }
    }
    
    /** invokes Commit dialog from testfile's popup menu, runs commit command and verifies that testfile has now attributes [Up-to-date; 1.1] under Explorer
     * @throws Exception any unexpected exception thrown during test
     * @see #testAdd depends on: testAdd
     * @see #testDiff <br>next: testDiff
     */
    public void testCommit() {
        startTest();
        try {
//            Explorer exp=new Explorer();
            log("Trying to switch to FS TAB\n");
            Node node = new Node(ExplorerOperator.invoke().repositoryTab().getRootNode(),
                getTestDirNodePath()+"|"+getTestFileObjectName()+" [LAdded; New]");
//            exp.switchToFilesystemsTab();
            
            log("Trying to push popup menu 'CVS|Commit...' on the node: "+getTestDirNodePath()+"|"+getTestFileObjectName()+" [LAdded; New]\n");
            new JCVSCommitAction().perform(node);
            
            CommitDialog commit=new CommitDialog();
            log("verifiing Commit dialog\n");
            commit.verify();
            
            log("Trying to push 'Run Command' button\n");
            commit.runCommand();
            
            OutputOfVCSCommandsFrame out=new OutputOfVCSCommandsFrame();
            log("verifiing the OutputOfCVSCommand dialog\n");
            out.verify();
            
            log("Trying to push 'Close' button\n");
            out.close();
            
            log("Trying to select a node:\n"+getTestDirNodePath()+"|"+getTestFileObjectName()+" [Up-to-date; 1.1]");
            new Node((new RepositoryTabOperator()).getRootNode(),
                getTestDirNodePath()+"|"+getTestFileObjectName()+" [Up-to-date; 1.1]").select();
//            exp.selectNode(getTestDirNodePath()+"|"+getTestFileObjectName()+" [Up-to-date; 1.1]");
            log("Selected... :-)\n");
            endTest();
        } catch (Exception e) {
            fail(e);
        }
    }
    
    /** changes testfile contents, invokes Refresh command, verifies that testfile has now attributes [LMod; 1.1] under Explorer, invokes diff command from testfile's popup menu and waits for Diff Output frame
     * @throws Exception any unexpected exception thrown during test
     * @see #testCommit depends on: testCommit
     * @see #testVCSGroups <br>next: testVCSGroups
     */
    public void testDiff() {
        startTest();
        String path;
        CVS = Bundle.getStringTrimmed("org.netbeans.modules.javacvs.Bundle", "JavaCvsFileSystem.validFilesystemLabel");
        String filesystem;
        try {
             path = getJavaCVSWork().getAbsolutePath();
             filesystem = CVS + path;
//            Explorer exp=new Explorer();
            log("Trying to switch to FS TAB\n");
            Node rootNode = ExplorerOperator.invoke().repositoryTab().getRootNode();
            Node node = new Node(rootNode, getTestDirNodePath()+"|"+getTestFileObjectName()+" [Up-to-date; 1.1]");
//            exp.switchToFilesystemsTab();
            log("Trying to select a node:\n"+getTestDirNodePath()+"|"+getTestFileObjectName()+" [Up-to-date; 1.1]\n");
//            exp.selectNode(getTestDirNodePath()+"|"+getTestFileObjectName()+" [Up-to-date; 1.1]");
            node.select();

            log("Openning an FileOutputStream using this file:\n"+getTestFile()+"\n");
            FileOutputStream file = new FileOutputStream(getTestFile());
            log("Writing a text into it.\n");
            file.write(("//comment\nclass "+TESTFILE+" {}\n").getBytes());
            file.close();
            
            log("Trying to open PopupMenu:\n'CVS|Refresh' on a node:\n" + getTestDirNodePath()+"\n");
            node = new Node(rootNode, getTestDirNodePath());
//            exp.pushPopupMenu("CVS|Refresh", getTestDirNodePath());
            new JCVSRefreshAction().perform(node);

            MainWindowOperator.getDefault().waitStatusText("Finished: Refresh");
            
//            Node rootNode1 = ExplorerOperator.invoke().runtimeTab().getRootNode();
//            rootNode1.select();
            /* The folowing block of code is there because I need wait for a node ...
             * It needs establish after refresh. And it's good for testing stability
             */
            rootNode = ExplorerOperator.invoke().repositoryTab().getRootNode();
            node = new Node(rootNode, getTestDirNodePath()+"|"+getTestFileObjectName());            
            node.select();

//            log("Trying to open PopupMenu:\n'CVS|Diff...', "+ getTestDirNodePath()+"|"+getTestFileObjectName()+" [LMod; 1.1]\n");
//            node = new Node(rootNode, getTestDirNodePath()+"|"+getTestFileObjectName()+" [LMod; 1.1]");
//            node = new Node(node, " [LMod; 1.1]");
            
//            exp.pushPopupMenu("CVS|Diff...", getTestDirNodePath()+"|"+getTestFileObjectName()+" [LMod; 1.1]");
            log("node je...:\n"+node.getPath());
            node.select();
            new JCVSDiffAction().perform(node);
            
            log("Trying to open dialog for Diff command...\n");
            DiffDialog diff=new DiffDialog();
            diff.verify();
            log("Trying to click on 'Run Command' button...\n");
            diff.runCommand();

            log("TCs which are test 'Output Of VCS command [' dialogs will probably fails\n"+
                "BECAUSE OF P3 #26811 BUG IN CORE, see http://www.netbeans.org/issues/show_bug.cgi?id=26811");
            log("Opening Diff output window...\n");
            
            TopComponentOperator tco = new TopComponentOperator(new EditorWindowOperator (), "Diff testfile.java");
            dumpDiffGraphical(tco);
            compareReferenceFiles();
            
            tco.undockView();
            JCVSDiffOutputOperator out = new JCVSDiffOutputOperator("Diff testfile.java", "1.1", "Working File");
            log("Verifiing if everything is allright on the diff output\n");
            out.verify();
            
            log("Trying to push 'Close' button\n");
            out.close();
            endTest();
        } catch (Exception e) {
            fail(e);
        }
    }
    
    void switchToTab(ExplorerOperator expO, String tab) throws Exception {
        expO.invoke().selectPage(tab);
    }
    
    /** adds testfile to default VCS group using popup menu, invokes VCS Groups frame from Versioning menu, docks it into Explorer, performs Verify, Commit and Delete commands on default group and verifies that testfile has now attributes [Up-to-date; 1.2] under Explorer
     * @throws Exception any unexpected exception thrown during test
     * @see #testDiff depends on: testDiff
     * @see #testVersioningExplorer <br>next: testVersioningExplorer
     */
    public void testVCSGroups() {
        clearTestStatus();
        startTest();
        CVS = Bundle.getStringTrimmed("org.netbeans.modules.vcscore.wizard.mountcvs.Bundle", "Templates/Mount/VCS/org-netbeans-modules-vcscore-wizard-mountcvs-CvsMountFS.settings");
        String strCmdAction = Bundle.getStringTrimmed("org.netbeans.modules.cvsclient.actions.Bundle", "LBL_JCommitAction") +
                              Bundle.getStringTrimmed("org.netbeans.modules.vcscore.actions.Bundle", "ClusteringAction.DialogDots");
        final String DEFAULTGROUP = Bundle.getStringTrimmed("org.netbeans.modules.vcscore.grouping.Bundle", "LBL_DefaultGroupName");
        final String vcsgrpT = Bundle.getStringTrimmed("org.netbeans.modules.vcscore.grouping.Bundle", "LBL_MODE.title");
        final String vcsgrpM = Bundle.getStringTrimmed("org.netbeans.modules.vcscore.grouping.Bundle", "LBL_VcsGroupMenuAction");
        final String addTovcsGrp = Bundle.getStringTrimmed("org.netbeans.modules.vcscore.actions.Bundle", "LBL_AddToGroupAction");
        final String verify     = Bundle.getStringTrimmed("org.netbeans.modules.vcscore.grouping.Bundle", "LBL_VerifyGroupAction");
        final String dialogTitle= Bundle.getStringTrimmed("org.openide.explorer.Bundle", "MSG_ConfirmDeleteObjectTitle");
        try {
            log("Trying to get an instance of MainFrame\n");
//            MainFrame main=MainFrame.getMainFrame();
            MainWindowOperator main = MainWindowOperator.getDefault();
            ExplorerOperator exp = new ExplorerOperator();
            Node rootNode = exp.invoke().repositoryTab().getRootNode();
            JTreeOperator jtro = null;
            
            log("Trying to invoke PopupMenu:\n" + addTovcsGrp +"|"+ DEFAULTGROUP +", on\n"+ getTestDirNodePath()+"|"+getTestFileObjectName()+" [LMod; 1.1]\n");
//            exp.pushPopupMenu(addTovcsGrp+"|"+DEFAULTGROUP, getTestDirNodePath()+"|"+getTestFileObjectName()+" [LMod; 1.1]");
            Node node = new Node(rootNode, getTestDirNodePath()+"|"+getTestFileObjectName()+" [LMod; 1.1]");
            node.performPopupAction(addTovcsGrp +"|"+ DEFAULTGROUP);
            
            log("Trying to push MainMenu:\n"+VERSIONINGMENU+"|" +vcsgrpM+ "\n");
            
            new VCSGroupsAction().perform();
            try { // try is here because VCS Groups frame can be already docked under Explorer
                log("Realy trying to  open VCSGroup Frame\n");
                VCSGroupsFrameOperator groups = new VCSGroupsFrameOperator();
//                VCSGroupsFrame groups = new VCSGroupsFrame();
                log("Verifying the VCSGroup dialog...\n");
                groups.makeComponentVisible();
                groups.verify();
                Thread.sleep(500);
                
                log("Trying to invoke MainMenu:\n'Window|Dock View Into|Explorer|Center'\n");
                new DockingAction("Explorer|"+DockingAction.CENTER).perform();
//                main.pushMenuNoExact("Window|Dock View Into|Explorer|Center");
            } catch (JemmyException ex) {log("Seems that VCSGroup's been already docked in Exp");}
            
            log("Trying to switch to VCS Groups TAB\n");
            //exp=new Explorer();
            //This probably isn't needed and the construction below seems good enought
/*            for(int i = 0; i < exp.tbpExplorerTabPane().getTabCount(); i ++){
                String sTitle = exp.tbpExplorerTabPane().getTitleAt(i);
                if(sTitle.indexOf(vcsgrpT) >= 0) {
                    exp.selectPage(vcsgrpT);
                    jtro = new JTreeOperator(JTreeOperator.waitJTree( (Container)
                        exp.tbpExplorerTabPane().getComponent(i), null, false, false, 0));
                }
            }
*/                      
            exp.selectPage(vcsgrpT);
            jtro = new JTreeOperator(JTreeOperator.waitJTree( (Container)
                    exp.tbpExplorerTabPane().getSource (), vcsgrpT, true, true, 0));
            
            log("Trying to invoke PopupMenu:\n'Verify', "+"  on the\n" + DEFAULTGROUP +"\n");
            new VerifyGroupAction().perform(new Node(jtro, DEFAULTGROUP));
            
            log("Trying to open GroupVerification Dialog....\n");
            GroupVerificationOperator out = new GroupVerificationOperator();
            
            log("Trying to verify the Group dialog...\n");
            out.verify(false, false, false, false);
            log("Trying to click on 'Close' button\n");
            out.close();
            
            log("Invoking PopupMenu:\n'"+CVS+ "|" +strCmdAction+"', on "+DEFAULTGROUP+"\n");
            new JCVSCommitAction().perform(new Node(jtro, DEFAULTGROUP));
//            exp.pushPopupMenu("CVS|Commit...", DEFAULTGROUP);
            
            log("Commit dialog should appeare\n");
// No jelly2 CommitDialogOperator
            CommitDialog commit=new CommitDialog();
            
            log("Verifying the Commit dialog...\n");
            commit.verify();
            log("Trying to click on 'Run Command' button\n");
            commit.runCommand();
            
            log("...an CommandOutputWindow should open NOW...\n");
// No jelly2 CommanOutputWondowOperator
            OutputOfVCSCommandsFrame out2=new OutputOfVCSCommandsFrame();
            
            log("Verifying the Output dialog....\n");
            out2.verify();
            
            log("Trying to click on 'Close' button\n");
            out2.close();
            
            log("Trying to invoke PopupMenu:\n'Delete', "+ DEFAULTGROUP);
            strCmdAction = Bundle.getStringTrimmed("org.openide.actions.Bundle", "Delete");
            new DeleteAction().perform(new Node(jtro, DEFAULTGROUP));
//            exp.pushPopupMenu("Delete", DEFAULTGROUP);
            
            log("Answering YES! to the YesNoDialog 'Confirm Object Deletion'...\n");
            new NbDialogOperator(dialogTitle).yes();
//            new JelloYesNoDialog("Confirm Object Deletion").yes();
            
            //exp=new Explorer();
            log("...sleeping 1s...\n");
            Thread.sleep(1000);
            
            log("Trying to switch to FS's TAB\n");
            rootNode = exp.invoke().repositoryTab().getRootNode();//selectPageFilesystems();

            log("Trying to select a node:\n"+getTestDirNodePath()+"|"+getTestFileObjectName()+" [Up-to-date; 1.2]\n");
//            exp.selectNode(getTestDirNodePath()+"|"+getTestFileObjectName()+" [Up-to-date; 1.2]");
            node = new Node(rootNode, getTestDirNodePath()+"|"+getTestFileObjectName()+" [Up-to-date; 1.2]");
            node.select();
            endTest();
        } catch (Exception e) {
            fail(e);
        }
    }
    
    /** invokes Versioning Explorer from testdir's popup menu, docks it into Explorer and looks for "1.1  no message" and "1.2  <Default Group>" nodes under testfile node inside Versioning Explorer
     * @throws Exception any unexpected exception thrown during test
     * @see #testVCSGroups depends on: testVCSGroups
     * @see #testBranch <br>next: testBranch
     */
    public void testVersioningExplorer() {
        startTest();
        String versioning = Bundle.getStringTrimmed("org.netbeans.modules.vcscore.versioning.impl.Bundle", "versioningSystemName");
        final String verExplorer= Bundle.getStringTrimmed("org.netbeans.modules.vcscore.actions.Bundle", "LBL_VersioningExplorer");
        try {
            log("Trying to get MainFrame...\n");
            MainWindowOperator main = MainWindowOperator.getDefault();
            ExplorerOperator exp = new ExplorerOperator();
            Node rootNode = exp.invoke().repositoryTab().getRootNode();
            JTreeOperator jtro = null;

            log("Trying to switch to FS's TAB\n");
            exp.selectPageFilesystems();
            
            log("Invoking popup menu 'Versioning Explorer' on a node:\n"+getTestDirNodePath()+"\n");
            new VersioningExplorerAction().perform(new Node(rootNode, getTestDirNodePath()));
            
            try { // try is here because Versioning Explorer can be already docked under Explorer
                new VersioningFrameOperator().verify();
                log("Trying to dock the Explorer window into Explorer... pushing a MainMenu:\n'Window|Dock View Into|Explorer|Center'\n");
                new DockingAction("Explorer|"+DockingAction.CENTER).perform();
            } catch (JemmyException ex) {log("Seems that Versioning Expl.'s been already docked in Exp");}
            
            Thread.sleep(1000);
            log("Trying to switch to Versioning TAB\n");
            switchToTab(exp, versioning);
            jtro = new JTreeOperator(JTreeOperator.waitJTree( (Container)
                    exp.tbpExplorerTabPane().getSource (), versioning, true, true, 0));

            log("Trying to select a node:\n" + getTestDirNodePath()+"\n");
            Node node = new Node(jtro, getTestDirNodePath());
            node.select();
//            exp.selectNode(getTestDirNodePath());
            
            log("Trying to select a node:\n" + getTestDirNodePath()+"|"+getTestFileObjectName()+".java [Up-to-date; 1.2]\n");
//            exp.selectNode(getTestDirNodePath()+"|"+getTestFileObjectName()+".java [Up-to-date; 1.2]");
            node = new Node(node, getTestFileObjectName()+".java [Up-to-date; 1.2]");
            node.select();
            node.expand();
            
            /* Checking if there isn't unluckyly "Please wait..." node
             */
            boolean isPlease = true;
            TreePath tp = jtro.findPath(getTestDirNodePath()+"|"+getTestFileObjectName()+".java", "|");
            jtro.expandPath(tp);
            log("This is TreePath on VE node:\n"+tp+"\n");
            while (isPlease) {
                for(int i = 0; i < jtro.getChildCount(tp); i++) {
                    String please = jtro.getChildPath(tp, i).getLastPathComponent().toString().trim();
                    log("This is String node 'Please wait':\n"+please);
                    if(please.equals("Please wait...") || please.indexOf("Please wait...")>-1){
                        log("There is still 'Please wait...' node.\nWe're  waiting another second for refreshing nodes");
                        Thread.sleep(1000);
                    } else {
                        isPlease = false;
                    }
                }
            }
            
            log("Trying to select a node:\n" + getTestDirNodePath()+"|"+getTestFileObjectName()+".java [Up-to-date; 1.2]|1.1  no message \n");
//            exp.selectNode(getTestDirNodePath()+"|"+getTestFileObjectName()+".java [Up-to-date; 1.2]|1.1  no message ");
            Node tmpNode = node;
            node = new Node(node, "1.1  no message ");
            node.select();
//            node.expand();

            
            log("Trying to select a node:\n" + getTestDirNodePath()+"|"+getTestFileObjectName()+".java [Up-to-date; 1.2]|1.2  <Default Group> \n");
//            exp.selectNode(getTestDirNodePath()+"|"+getTestFileObjectName()+".java [Up-to-date; 1.2]|1.2  <Default Group> ");
            node = new Node(tmpNode, "1.2  <Default Group> ");
            node.select();
            endTest();
            
            log("Switching into FS TAB\n");
//            exp.switchToFilesystemsTab();
            exp.selectPageFilesystems();
        } catch (Exception e) {
            fail(e);
        }
    }
    
    /** invokes Tag dialog from testfile's popup menu, creates new branch "vetev2", invokes Refresh Revisions command under Versioning Explorer and looks for "1.2.2 (vetev2)" node inside Versioning Explorer
     * @throws Exception any unexpected exception thrown during test
     * @see #testVersioningExplorer depends on: testVersioningExplorer
     * @see #testUpdateBranch <br>next: testUpdateBranch
     */
    public void testBranch() {
        startTest();
        strCmdAction = Bundle.getStringTrimmed("org.netbeans.modules.cvsclient.actions.Bundle", "LBL_JTagAction") +"...";
        String versioning = Bundle.getStringTrimmed("org.netbeans.modules.vcscore.versioning.impl.Bundle", "versioningSystemName");
        String refrRev = Bundle.getStringTrimmed("org.netbeans.modules.vcscore.versioning.Bundle", "RefreshRevisionsAction_Name");
        CVS = Bundle.getStringTrimmed("org.netbeans.modules.vcscore.wizard.mountcvs.Bundle", "Templates/Mount/VCS/org-netbeans-modules-vcscore-wizard-mountcvs-CvsMountFS.settings");
        try {
            
//            MainWindowOperator.StatusTextTracer sbt = MainWindowOperator.getDefault().getStatusTextTracer();
//            MainWindowOperator main = MainWindowOperator.getDefault();
            ExplorerOperator exp = new ExplorerOperator();
            Node rootNode = exp.invoke().repositoryTab().getRootNode();
            JTreeOperator jtro = null;
            log("Trying to switch to FS TAB in Explorer\n");
            exp.selectPageFilesystems();
            
            log("Trying to push PopupMe4nu:\n" + CVS +"|"+ strCmdAction +"\non this node:\n"+
                getTestDirNodePath()+"|"+getTestFileObjectName()+" [Up-to-date; 1.2]\n");
            new JCVSTagAction(true).perform(new Node(rootNode, 
                                getTestDirNodePath()+"|"+ getTestFileObjectName()+" [Up-to-date; 1.2]"));
            
            log("Trying to open TAG dialog\n");
// No jelly2 TagDialogOperator
            TagDialog tag=new TagDialog();
            
            log("Writing text 'vetev2' into tag textfield...\n");
            tag.txtTag().setText("vetev2");
            
            log("Checking up check box for branch tag...\n");
            tag.check(tag.cbBranch(), true);
            
            /* Because of cvsrc file, there could be predefined various command switches
             * which we don't know. So we traying to find as much known substring as it is possible
             */
            log("Trying to find known tag switches: '-b vetev2'\n");
            assertTrue("Switches are different!", tag.compareSwitches("tag -"));
            assertTrue("Switches are different!", tag.compareSwitches(" -b vetev2"));
            
            log("Verification TAG dialog....\n");
            tag.verify();
            
            
            /* This component isn't used anymore now
             * Instead of it is used this one:
             * TabbedOutputOfVCSCommandsFrame 
             */
//            OutputOfVCSCommandsFrame out=new OutputOfVCSCommandsFrame();

            /* This piece of code is a workaround for problems with losted HierarchyEvent
             * There is an jemmy's issue http://www.netbeans.org/issues/show_bug.cgi?id=32466
             * but later appeared as a JDK bug: http://developer.java.sun.com/developer/bugParade/bugs/4924516.html
             */
            TabbedOutputOfVCSCommandsFrame out;
            try {
                EventTool.removeListeners();
                log("Trying ot execute command => clickin' on OK button\n");
                tag.runCommand();
                log("The Output Of VCS Command window should be opened now...\n");
                out = new TabbedOutputOfVCSCommandsFrame();
            } finally {
                EventTool.addListeners();
            }
            // end of workaround issue #32466
            
            
            log("Trying to verify the dialog\n");
            //Nefunguje komponenta
            // pouzivej TabbedOutputOfVCSCommandsFrame out=new TabbedOutputOfVCSCommandsFrame();
            out.verify();
            
            log("Trying to close the dialog\n");           
            out.close();
            
            //exp=new Explorer();
            log("Trying to switch to 'Versioning' TAB in the Explorer\n");
            switchToTab(exp, versioning);

            jtro = new JTreeOperator(JTreeOperator.waitJTree( (Container)
                    exp.tbpExplorerTabPane().getSource (), versioning, true, true, 0));
            
            //Starting listening to MainWindow's status line
            sbt.start();
            log("Trying to push PopupMenu:\n"+"Refresh Revisions\non the node:\n"+
                getTestDirNodePath()+"|"+getTestFileObjectName()+".java [Up-to-date; 1.2]\n");
            
            new RefreshRevisionsAction().perform(new Node(jtro,
                    getTestDirNodePath()+"|"+getTestFileObjectName()+".java [Up-to-date; 1.2]"));

            log("Trying to find this String 'Finished: Log' in the status line of the MainFrame...\n");
            // Will expaire after 60s and throw an Exception if no matching String will be found.
            sbt.contains("Finished: Log", true);
            sbt.stop();
            
            new EventTool().waitNoEvent(2000);
            log("Trying to select node in the Explorer:\n"+getTestDirNodePath()+"|"+getTestFileObjectName()+
                ".java [Up-to-date; 1.2]|1.2  <Default Group> |1.2.2 (vetev2)\n");
            (new Node(jtro, getTestDirNodePath()+"|"+getTestFileObjectName()+
                ".java [Up-to-date; 1.2]|1.2  <Default Group> |1.2.2 (vetev2)")).select();
            
            endTest();
            log("Trying to switch to FS TAB\n");
            exp.selectPageFilesystems();
        } catch (Exception e) {
            fail(e);
        }
    }
    
    /** invokes Update dialog from testfile's popup menu, performs update using Tag "vetev2" and verifies that testfile has now attributes [Up-to-date; 1.2] (vetev2) under Filesystems tab
     * @throws Exception any unexpected exception thrown during test
     * @see #testBranch depends on: testBranch
     * @see #testCommitToBranch <br>next: testCommitToBranch
     */
    public void testUpdateBranch() {
        startTest();
        strCmdAction = Bundle.getStringTrimmed("org.netbeans.modules.cvsclient.actions.Bundle", "LBL_JUpdateAction") +"...";
        CVS = Bundle.getStringTrimmed("org.netbeans.modules.vcscore.wizard.mountcvs.Bundle", "Templates/Mount/VCS/org-netbeans-modules-vcscore-wizard-mountcvs-CvsMountFS.settings");
        try {
            ExplorerOperator exp = new ExplorerOperator();
            Node rootNode = exp.invoke().repositoryTab().getRootNode();
//            JTreeOperator jtro = null;
            
            exp.selectPageFilesystems();
            new Action(null, CVS +"|"+ strCmdAction).perform(
                    new Node(rootNode,getTestDirNodePath()+"|"+getTestFileObjectName()+" [Up-to-date; 1.2]"));
            
// No jelly2 UpdateDialogOperator
            UpdateDialog update=new UpdateDialog();
            update.check(update.cbRevisionTag(), true);
            update.txtRevisionTag().setText("vetev2");
            update.check(update.cbRevisionTag(), false);
            update.check(update.cbRevisionTag(), true);
            /* Because of cvsrc file, there could be predefined various command switches
             * which we don't know. So we traying to find as much known substring as it is possible
             */
            assertTrue("Switches are different!", update.compareSwitches("update -")); assertTrue("Switches are different!", update.compareSwitches(" -r vetev2"));
            
            update.verify();
            update.runCommand();
// No jelly2 OutputOfVCSCommandsFrameOperator
            OutputOfVCSCommandsFrame out=new OutputOfVCSCommandsFrame();
            out.verify();
            out.close();
            new Node(rootNode, getTestDirNodePath()+"|"+getTestFileObjectName()+" [Up-to-date; 1.2] (vetev2)").select();
            endTest();
        } catch (Exception e) {
            fail(e);
        }
    }
    
    /** changes testfile contents again, invokes Refresh command, verifies that testfile has now attributes [LMod; 1.2] (vetev2), invokes Commit command from testfile's popup menu, verifies that testfile has now attributes [Up-to-date; 1.2.2.1] (vetev2), performs Refresh Revissions command under Revision Expplorer and looks for "1.2.2.1  no message" node inside
     * @throws Exception any unexpected exception thrown during test
     * @see #testUpdateBranch depends on: testUpdateBranch
     * @see #testMerge <br>next: testMerge
     */
    public void testCommitToBranch() {
        startTest();
        strCmdAction = Bundle.getStringTrimmed("org.netbeans.modules.cvsclient.actions.Bundle", "LBL_JRefreshAction");
        String commitcmd= Bundle.getStringTrimmed("org.netbeans.modules.cvsclient.actions.Bundle", "LBL_JCommitAction") + "...";
        String versioning = Bundle.getStringTrimmed("org.netbeans.modules.vcscore.versioning.impl.Bundle", "versioningSystemName");
        String refrRev = Bundle.getStringTrimmed("org.netbeans.modules.vcscore.versioning.Bundle", "RefreshRevisionsAction_Name");
        CVS = Bundle.getStringTrimmed("org.netbeans.modules.vcscore.wizard.mountcvs.Bundle", "Templates/Mount/VCS/org-netbeans-modules-vcscore-wizard-mountcvs-CvsMountFS.settings");
        try {
            ExplorerOperator exp = new ExplorerOperator();
            Node rootNode = exp.invoke().repositoryTab().getRootNode();
//            MainWindowOperator.StatusTextTracer sbt = MainWindowOperator.getDefault().getStatusTextTracer();
            Node node = null;
            JTreeOperator jtro = null;

            log("Trying to switch to FS Tab\n");
            exp.selectPageFilesystems();
            
            log("Trying to select this node:\n"+
                getTestDirNodePath()+"|"+getTestFileObjectName()+" [Up-to-date; 1.2] (vetev2)");
            new Node(rootNode, getTestDirNodePath()+"|"+getTestFileObjectName()+" [Up-to-date; 1.2] (vetev2)").select();


            FileOutputStream file = new FileOutputStream(getTestFile());
            file.write(("//comment\nclass "+TESTFILE+" {\n//branch vetev2\n}\n").getBytes());
            file.close();
            
            log("Trying to push PopupMenu:\n"+ CVS +"|"+ strCmdAction +"\non the node:\n"+ getTestDirNodePath()+"\n");
            new JCVSRefreshAction().perform(new Node(rootNode, getTestDirNodePath()));
            
            log("Trying to push PopupMenu:\n"+ CVS +"|"+ commitcmd +"\non the node:\n"+
                getTestDirNodePath()+"|"+getTestFileObjectName()+" [LMod; 1.2] (vetev2)\n");
            new JCVSCommitAction().perform(
                new Node(rootNode, getTestDirNodePath()+"|"+getTestFileObjectName()+" [LMod; 1.2] (vetev2)"));
            
            log("Opening the Commit dialog...\n");
// No jelly2 CommitDialogOperator
            CommitDialog commit=new CommitDialog();
            
            log("Trying to verify the dialog...\b");
            commit.verify();
            
            log("Executing the command...\n");
            commit.runCommand();
            
            log("Opening  the 'Output of VCS Commands [' dialog...\n");
            OutputOfVCSCommandsFrame out=new OutputOfVCSCommandsFrame();
            
            log("Verifying the dialog...\n");
            out.verify();
            
            log("Closing t dialog...\n");
            out.close();
            
            log("Trying to select a node:\n"+
                getTestDirNodePath()+"|"+getTestFileObjectName()+" [Up-to-date; 1.2.2.1] (vetev2)\n");
            new Node(rootNode, getTestDirNodePath()+"|"+getTestFileObjectName()+" [Up-to-date; 1.2.2.1] (vetev2)").select();
            
            log("Trying to switch to Verifying TAB\n");
            switchToTab(exp, versioning);
            jtro = new JTreeOperator(JTreeOperator.waitJTree( (Container)
                exp.tbpExplorerTabPane().getSource (), versioning, true, true, 0));
            
            log("Selecting a node:\n" +getTestDirNodePath());
            node = new Node(jtro, getTestDirNodePath());
            node.select();
            
            sbt.start();
            log("Trying to push PopupMenu:\n" + refrRev + "\non the node:\n"+
                getTestDirNodePath()+"|"+getTestFileObjectName()+".java [Up-to-date; 1.2.2.1] (vetev2)+\n");
            
            node = new Node(node, getTestFileObjectName()+".java [Up-to-date; 1.2.2.1] (vetev2)");
            
            new RefreshRevisionsAction().perform(node);
            
            log("Verifying status line in MainFrame...'Finished: Log'\n");
            sbt.waitText("Finished: Log");
            sbt.stop();

            /* Waiting for creating node after LOG command in VE */
            try {
                new Waiter(new Waitable() {
                    public Object actionProduced(Object parent) {
                        return new Node((Node)parent, "1.2|1.2.2 (vetev2)").isChildPresent("1.2.2.1") ? Boolean.TRUE: null;
                    }
                    public String getDescription() {
                        return("Child '1.2.2 (vetev2)' present under parent '1.2 <Default Group> '");
                    }
                }).waitAction(node);
            } catch (InterruptedException e) {
                throw new JemmyException("Interrupted.", e);
            }

            
            log("Selecting node:\n"+ getTestDirNodePath()+"|"+getTestFileObjectName()+
                ".java [Up-to-date; 1.2.2.1] (vetev2)|1.2 <Default Group> |1.2.2 (vetev2)|1.2.2.1 no message \n");
            
            
            node = new Node(jtro, getTestDirNodePath()+"|"+getTestFileObjectName()+
                ".java [Up-to-date; 1.2.2.1] (vetev2)|1.2  <Default Group> |1.2.2 (vetev2)|1.2.2.1  no message ");
            log("Selected node should be:\n"+node.getPath());
            
            
            node.select();
            endTest();
            
            log("Trying to switch to FS TAB\n");
            exp.selectPageFilesystems();
        } catch (Exception e) {
            fail(e);
        }
    }
    
    /** performs update of testfile with Reset Sticky option, verifies that testfile has now [Up-to-date; 1.2] attributes, swithes to Revision Explorer, performs Merge With Revision command on "1.2.2 (vetev2)" node, verifies that testfile has now [LMod; 1.2] attributes, performs Commit command on testfile's node and verifies that testfile has now [Up-to-date; 1.3] attributes
     * @throws Exception any unexpected exception thrown during test
     * @see #testCommitToBranch depends on: testCommitToBranch
     * @see #testStatus <br>next: testStatus
     */
    public void testMerge() {
        startTest();
        strCmdAction = Bundle.getStringTrimmed("org.netbeans.modules.cvsclient.actions.Bundle", "LBL_JUpdateAction") +"...";
        String versioning = Bundle.getStringTrimmed("org.netbeans.modules.vcscore.versioning.impl.Bundle", "versioningSystemName");
        String merge = Bundle.getStringTrimmed("org.netbeans.modules.cvsclient.versioning.Bundle", "JavaCvsVersioningAction.merge");
        String commitCmd = Bundle.getStringTrimmed("org.netbeans.modules.cvsclient.actions.Bundle", "LBL_JCommitAction") + "...";
        CVS = Bundle.getStringTrimmed("org.netbeans.modules.vcscore.wizard.mountcvs.Bundle", "Templates/Mount/VCS/org-netbeans-modules-vcscore-wizard-mountcvs-CvsMountFS.settings");
        try {
            ExplorerOperator exp = new ExplorerOperator();
            Node rootNode = exp.invoke().repositoryTab().getRootNode();
//            MainWindowOperator.StatusTextTracer sbt = MainWindowOperator.getDefault().getStatusTextTracer();
            JTreeOperator jtro = null;
            
            log("Trying to switch to FS Tab\n");
            exp.selectPageFilesystems();
            
            log("Trying to push PopupMenu:\n" +CVS+ "|" +strCmdAction+ "\non the node:\n"+
                getTestDirNodePath()+"|"+getTestFileObjectName()+" [Up-to-date; 1.2.2.1] (vetev2)\n");
            new JCVSUpdateAction().perform(
                new Node(rootNode, getTestDirNodePath()+"|"+getTestFileObjectName()+" [Up-to-date; 1.2.2.1] (vetev2)"));
            
            log("Opening Update dialog...\n");
// No jelly2 UpdateDialogOperator
            UpdateDialog update=new UpdateDialog();
            
            log("Trying to 'reset sticky tag'...\n");
            update.check(update.cbResetSticky(), true);
            
            log("Trying to verify the dialog...\n");
            update.verify();
            
            log("Executing command....\n");
            update.runCommand();
            
            log("The 'Output of VCS Commands [' should be opened...\n");
// No jelly2 UpdateDialogOperator
            OutputOfVCSCommandsFrame out=new OutputOfVCSCommandsFrame();
            log("Trying to verify the dialog\n");
            out.verify();
            log("Trying to close the dialog using 'Close' button\n");
            out.close();
            
            log("Trying to select node in the explorer:\n"+
                getTestDirNodePath()+"|"+getTestFileObjectName()+" [Up-to-date; 1.2]");
            new Node(rootNode, getTestDirNodePath()+"|"+getTestFileObjectName()+" [Up-to-date; 1.2]").select();

            //exp=new Explorer();
            log("Trying to switch to Versioning TAB\n");
            switchToTab(exp, versioning);
            jtro = new JTreeOperator(JTreeOperator.waitJTree( (Container)
                  exp.tbpExplorerTabPane().getSource(), versioning, true, true, 0));

            sbt.start();
            log("Trying to push PopupMenu:\n" +merge+ "\non the node in VE:\n"+ getTestDirNodePath()+"|"+
                getTestFileObjectName()+".java [Up-to-date; 1.2]|1.2  <Default Group> |1.2.2 (vetev2)");
            new JCVSMergeWithRevisionAction().perform(new Node
                (jtro, getTestDirNodePath()+"|"+getTestFileObjectName()+ ".java [Up-to-date; 1.2]|1.2  <Default Group> |1.2.2 (vetev2)"));

//Please correct text which is compaered in statusline after merge!!!!
            log("WARNING TO DEVELOPERS:\nPlease correct 'mergre' text in status line o MainWindow!!!");
            System.out.println("Jaky je text v Main Window status Line?");
            System.out.println(sbt.getStatusTextHistory().toString());
            sbt.contains("Merge:finished...", true);
            sbt.stop();
            
            log("Merge With Revision seems succeeded...\n");

            log("Trying to push PopupMenu:\n"+ CVS +"|"+ commitCmd +"\non the node:\n"+
                getTestDirNodePath()+"|"+getTestFileObjectName()+".java [LMod; 1.2]");
            new JCVSCommitAction().perform(new Node
                (jtro, getTestDirNodePath()+"|"+getTestFileObjectName()+".java [LMod; 1.2]"));
            
            log("Invoking Commit dialog");
// No jelly2 CommitDialogOperator
            CommitDialog commit=new CommitDialog();
            
            log("Typing a message:\nMerged with branch.\n");
            commit.txtMessage().setText("Merged with branch.");
            
            log("Trying to verify the dialog\n");
            commit.verify();
            
            log("Trying to execute the command\n");
            commit.runCommand();
            
            log("Invoking the 'Ouptut of VCS Commands [' dialog\n");
            out=new OutputOfVCSCommandsFrame();
            
            log("Trying to verify the dialog\n");
            out.verify();
            
            log("Closing the dialog\n");
            out.close();
            
            log("Selecting a node:\n"+
                getTestDirNodePath()+"|"+getTestFileObjectName()+".java [Up-to-date; 1.3]\n");
            new Node(jtro, getTestDirNodePath()+"|"+getTestFileObjectName()+".java [Up-to-date; 1.3]").select();
            endTest();
            
            log("Trying to switch to FS Tab\n");
            exp.selectPageFilesystems();
        } catch (Exception e) {
            fail(e);
        }
    }
    
    /** performs Status command on testfile and looks for "Up-to-date" and "1.3" labels inside output frame
     * @throws Exception any unexpected exception thrown during test
     * @see #testMerge depends on: testMerge
     * @see #testLog <br>next: testLog
     */
    public void testStatus() {
        startTest();
        strCmdAction = Bundle.getStringTrimmed("org.netbeans.modules.cvsclient.actions.Bundle", "LBL_JStatusAction") +"...";
        String versioning = Bundle.getStringTrimmed("org.netbeans.modules.vcscore.versioning.impl.Bundle", "versioningSystemName");
        CVS = Bundle.getStringTrimmed("org.netbeans.modules.vcscore.wizard.mountcvs.Bundle", "Templates/Mount/VCS/org-netbeans-modules-vcscore-wizard-mountcvs-CvsMountFS.settings");
        try {
            ExplorerOperator exp=new ExplorerOperator();
            Node rootNode = exp.invoke().repositoryTab().getRootNode();
//            MainWindowOperator.StatusTextTracer sbt = MainWindowOperator.getDefault().getStatusTextTracer();
            Node node = null;
            JTreeOperator jtro = null;

            log("Trying to switch to Versioning TAB\n");
            switchToTab(exp, versioning);
            
            log("Trying to push PopupMenu:\n"+ CVS +"|"+ strCmdAction +"\non the node:\n"+
                getTestDirNodePath()+"|"+getTestFileObjectName()+".java [Up-to-date; 1.3]\n");
            jtro = new JTreeOperator(JTreeOperator.waitJTree( (Container)
                  exp.tbpExplorerTabPane().getSource(), versioning, true, true, 0));
            new JCVSStatusAction().perform(new Node
                (jtro, getTestDirNodePath()+"|"+getTestFileObjectName()+".java [Up-to-date; 1.3]"));
            
            log("Invoking command dialog...\n");
// No jelly2 StatusDialogOperator
            StatusDialog dlg=new StatusDialog();
            
            log("Verifying the dialog...\n");
            dlg.verify();
            
            log("Executing the command...\n");
            dlg.runCommand();
            
            log("Looking for 'CVS Output [Status - testfile.java]' dialog....\nbut sometimes it might happen"+
                "that command name and file is ommited:-((((...In this case...\n test would fail\nsee P2 #26842 bug at: "+
                "http://www.netbeans.org/issues/show_bug.cgi?id=26842\nand write your experiences in to it, please:\n");
            StatusOutputOperator out=new StatusOutputOperator("testfile.java");
            
            log("Status output dialog contains:");
            log(out.getStatus());
            log(out.getFilename());
            log(out.getRepositoryFile());
            log(out.getWorkingRevision());
            log(out.getRepositoryRevision());
            log(out.getStickyTag());
            log(out.getStickyOptions()+"\n");
            
            log("psRef="+psRef.toString());
            log("repositoryprefix="+repositoryprefix);
            out.dumpFile(psRef, repositoryprefix);
            out.close();
            compareReferenceFiles();
            
            log("Trying to switch to FS Tab\n");
            exp.selectPageFilesystems();
            endTest();
        } catch (Exception e) {
            fail(e);
        }
    }
    
    /** performs Log command on testfile and looks for "1.3" label inside output frame
     * @throws Exception any unexpected exception thrown during test
     * @see #testStatus depends on: testStatus
     * @see #testAnnotate <br>next: testAnnotate
     */
    public void testLog() {
        clearTestStatus();
        startTest();
        strCmdAction = Bundle.getStringTrimmed("org.netbeans.modules.cvsclient.actions.Bundle", "LBL_JLogAction") +"...";
        String versioning = Bundle.getStringTrimmed("org.netbeans.modules.vcscore.versioning.impl.Bundle", "versioningSystemName");
        CVS = Bundle.getStringTrimmed("org.netbeans.modules.vcscore.wizard.mountcvs.Bundle", "Templates/Mount/VCS/org-netbeans-modules-vcscore-wizard-mountcvs-CvsMountFS.settings");
        try {
            ExplorerOperator exp=new ExplorerOperator();
            Node rootNode = exp.invoke().repositoryTab().getRootNode();
            Node node = null;
            JTreeOperator jtro = null;
            
            log("Trying to switch to Verifying TAB\n");
            switchToTab(exp, versioning);
            
            jtro = new JTreeOperator(JTreeOperator.waitJTree( (Container)
                    exp.tbpExplorerTabPane().getSource (), versioning, true, true, 0));
            node = new Node(jtro, getTestDirNodePath()+"|"+getTestFileObjectName()+".java [Up-to-date; 1.3]");

            log("Trying to push PopupMenu:\n"+ CVS +"|"+ strCmdAction +"\non the node:\n" + node.getPath());
            new JCVSLogAction().perform(node);
            
            log("Opening the input dialog for log command...\n");
// No jelly2 LogDialogOperator
            LogDialog dlg=new LogDialog();
            
            log("Verifying the dialog...\n");
            dlg.verify();
            
            log("Exception the command...\n");
            dlg.runCommand();

            log("Looking for 'CVS Output [Log - testfile.java]' dialog....\nbut sometimes it might happen"+
                "that command name and file is ommited:-((((...In this case...\n test would fail\nsee P2 bug at: "+
                "http://www.netbeans.org/issues/show_bug.cgi?id=26842\nand write your experiences in to it, please:\n");
            LogOutputOperator out=new LogOutputOperator("testfile.java", false, false);
            
            log("Log output dialog contains:");
            log(out.getFilename());
            log(out.getLocks());
            log(out.getRepositoryFile());
            log(out.getHeadRevision());
            log(out.getSelectedRevisions());
            log(out.getOutOf());
            log(out.getLogMessage()+"\n");

            log("psRef="+psRef.toString());
            log("repositoryprefix="+repositoryprefix);
            out.dumpFile(psRef, repositoryprefix);
            compareReferenceFiles();
            
            log("Closing the dialog...\n");
            out.close();
            endTest();
            
            log("Trying to switch to FS TAB\n");
            exp.selectPageFilesystems();
        } catch (Exception e) {
            fail(e);
        }
    }
    
    /** performs Annotate command on testfile and looks for output
     * @throws Exception any unexpected exception thrown during test
     * @see #testLog depends on: testLog
     * @see #testUnmount <br>next: testUnmount
     */
        public void testAnnotate() {
        clearTestStatus();
        startTest();
        strCmdAction = Bundle.getStringTrimmed("org.netbeans.modules.cvsclient.actions.Bundle", "LBL_JAnnotateAction") +"...";
        String versioning = Bundle.getStringTrimmed("org.netbeans.modules.vcscore.versioning.impl.Bundle", "versioningSystemName");
        CVS = Bundle.getStringTrimmed("org.netbeans.modules.vcscore.wizard.mountcvs.Bundle", "Templates/Mount/VCS/org-netbeans-modules-vcscore-wizard-mountcvs-CvsMountFS.settings");
        try {
            ExplorerOperator exp=new ExplorerOperator();
            Node rootNode = exp.invoke().repositoryTab().getRootNode();
            Node node = null;
            JTreeOperator jtro = null;

            log("Trying to switch to Versioning TAB\n");
            switchToTab(exp, versioning);
            
            log("Trying to push PopupMenu:\n"+ CVS +"|"+ strCmdAction +"\non the node:\n"+
                getTestDirNodePath()+"|"+getTestFileObjectName()+".java [Up-to-date; 1.3]");

            jtro = new JTreeOperator(JTreeOperator.waitJTree( (Container)
                    exp.tbpExplorerTabPane().getSource (), versioning, true, true, 0));
            new JCVSAnnotateAction().perform(
                new Node(jtro, getTestDirNodePath()+"|"+getTestFileObjectName()+".java [Up-to-date; 1.3]"));
            
            log("Opening the input command dialog...\n");
// No jelly2 AnnotateDialogOperator
            AnnotateDialog dlg=new AnnotateDialog();
            
            log("Trying to verify the dialog...\n");
            dlg.verify();
            
            log("Explorer the log command...\n");
            dlg.runCommand();
            
            log("Looking for 'CVS Output [Annotate - testfile.java]' dialog.... and traying it to close,\nbut sometimes it might happen"+
                "that command name and file is ommited:-((((...In this case...\n test would fail\nsee P2 bug at: "+
                "http://www.netbeans.org/issues/show_bug.cgi?id=26842\nand write your experiences in to it, please:\n");
            new FrameOperator("CVS Output [Annotate testfile.java]").close();
            endTest();
            
            log("Trying to switch to FS TAB\n");
            exp.selectPageFilesystems();
        } catch (Exception e) {
            fail(e);
        }
    }
    
    /** unmounts CVS filesystem using its popup menu
     * @throws Exception any unexpected exception thrown during test
     * @see #testAnnotate depends on: testAnnotate
     * @see #testUndock <br>next: testUndock
     */
    public void testUnmount() {
        clearTestStatus();
        startTest();
        CVS = Bundle.getStringTrimmed("org.netbeans.modules.javacvs.Bundle", "JavaCvsFileSystem.validFilesystemLabel");
        String umountFS = Bundle.getStringTrimmed("org.netbeans.core.actions.Bundle", "UnmountFS");
        try {
            ExplorerOperator exp=new ExplorerOperator();
            Node rootNode = exp.invoke().repositoryTab().getRootNode();
            Node node = null;
//            JTreeOperator jtro = null;

            log("Trying to get focus to Explorer\n");
            exp.getFocus();
            
//            log("Trying to switch to FileSystems TAB");
//            exp.selectPageFilesystems();
            
            node = new Node(rootNode, CVS + getJavaCVSWork().getAbsolutePath());
            log("Trying to push PopupMenu:\n"+umountFS+"\non the node:\n"+node+"\n");
            new UnmountFSAction().perform(node);
            log("DONE:)");
            endTest();
        } catch (Exception e) {
            fail(e);
        }
    }
    
    /** undocks VCS Groups and Versioning frames from Explorer and closes them
     * @throws Exception any unexpected exception thrown during test
     * @see #testUnmount depends on: testUnmount
     */
    public void testUndock() {
        clearTestStatus();
        startTest();
        String vcsGrpTAB = Bundle.getStringTrimmed("org.netbeans.modules.vcscore.grouping.Bundle", "LBL_MODE.title");
        String versTAB   = Bundle.getStringTrimmed("org.netbeans.modules.vcscore.versioning.impl.Bundle", "versioningSystemName");
        try {
            ExplorerOperator exp=new ExplorerOperator();
            Node rootNode = exp.invoke().repositoryTab().getRootNode();
            Node node = null;
//            JTreeOperator jtro = null;
            JFrameOperator jfo = null;

            log("Trying to activate the Explorer...\n");
            exp.getFocus();
            //Excersising with the Explorer to correctly undock VCS Groups TAB
            Thread.sleep(2500);
            
            log("Trying to switch to FS TAB\n");
            exp.selectPageFilesystems();
            //Excersising with the Explorer to correctly undock VCS Groups TAB
            Thread.sleep(2500);
            
            log("Trying to switch to '" +vcsGrpTAB+ "' TAB\n");
            switchToTab(exp, vcsGrpTAB);
            
            log("Trying to push MainMenu:\"+ 'Window||Undock View'\n");
//            MainWindowOperator.getDefault();
            new UndockAction().perform();
            
            log("...now the VCSGroupFrame should be undock....and we'return closing it...\n");
            new VCSGroupsFrameOperator().close();
            
            //exp=new Explorer();
            log("Trying to activate the Explorer...\n");
//            new JFrameOperator(exp.getJFrame()).activate();
            exp.getFocus();
            
            log("Trying to switch to '" +versTAB+ "' TAB\n");
            switchToTab(exp, versTAB);
//            MainFrame.getMainFrame().pushMenu("Window|Undock View");
            new UndockAction().perform();
            new VersioningFrameOperator().close();
            endTest();
        } catch (Exception e) {
            fail(e);
        }
    }
}
