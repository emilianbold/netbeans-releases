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

package org.netbeans.test.j2ee.lib;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.swing.JComboBox;
import junit.framework.AssertionFailedError;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.RuntimeTabOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.ide.ProjectSupport;

/**
 *
 * @author lm97939
 */
public class Utils {
    
    private NbTestCase nbtestcase;
    
    public Utils(NbTestCase nbtestcase) {
        this.nbtestcase = nbtestcase;
    }
    
    public static String getTimeIndex() {
        return new SimpleDateFormat("HHmmssS",Locale.US).format(new Date());
    }
    
    /**
     * Starts or Stops AppServer
     * @param start if true, starts appserver, if false stops appserver.
     */
    public static void startStopServer(boolean start) {
        RuntimeTabOperator runtimeTab = RuntimeTabOperator.invoke();
        Node serverNode = new Node(runtimeTab.getRootNode(), Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.Bundle", "SERVER_REGISTRY_NODE")
        +"|Application Server");
        new org.netbeans.jemmy.EventTool().waitNoEvent(10000);
        if (start)
            serverNode.performPopupAction("Start");
        else
            serverNode.performPopupAction("Stop");
        new org.netbeans.jemmy.EventTool().waitNoEvent(5000);
        ProgressSupport.waitFinished((start?"Starting":"Stopping") + " Sun Java System Application Server", 500000);
        new org.netbeans.jemmy.EventTool().waitNoEvent(2000);
    }
    
    public static void prepareDatabase() {
        		
        new Action("Tools|"+Bundle.getStringTrimmed("org.netbeans.modules.derby.Bundle", "LBL_DerbyDatabase")+
                "|"+Bundle.getStringTrimmed("org.netbeans.modules.derby.Bundle", "LBL_StartAction"), null).performMenu();
        OutputTabOperator outputOper = new OutputTabOperator(Bundle.getStringTrimmed("org.netbeans.modules.derby.Bundle", "LBL_outputtab"));
        outputOper.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 120000);
        outputOper.waitText("Server is ready to accept connections on port 1527.");
        new Node(new RuntimeTabOperator().getRootNode(), "Databases|/sample").performPopupActionNoBlock("Connect");
        try {
            NbDialogOperator dialog = new NbDialogOperator("Connect");
            new JTextFieldOperator(dialog,0).typeText("app");
            dialog.ok();
        } catch (TimeoutExpiredException e) {}
    }
    
    public void assertFiles(File dir, String fileNames[], String goldenFilePrefix) throws IOException {
        AssertionFailedError firstExc = null;
        for (int i=0; i<fileNames.length; i++) {
            File file = new File(dir, fileNames[i]);            
            try {
                File goldenFile = nbtestcase.getGoldenFile(goldenFilePrefix + fileNames[i]);                
                nbtestcase.assertFile("File "+file.getAbsolutePath()+" is different than golden file "+goldenFile.getAbsolutePath()+".",
                        file,
                        goldenFile,
                        new File(nbtestcase.getWorkDir(), fileNames[i]+".diff"),
                        new TrimmingLineDiff()); 
            } catch (AssertionFailedError e) {
                if (firstExc == null){
                    firstExc = e;
                } 
                File copy = new File(nbtestcase.getWorkDirPath(), goldenFilePrefix+fileNames[i]);
                copyFile(file,copy);
            }
        }
        if (firstExc != null)
            throw firstExc;
    }
    
    /**
     * Copy file in to out
     * @param in File
     * @param out File
     * @throws Exception
     */
    public static void copyFile(File in, File out) {
        try {
            out.createNewFile();
            FileChannel srcChannel = new FileInputStream(in).getChannel();
            FileChannel dstChannel = new FileOutputStream(out).getChannel();
            dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
            srcChannel.close();
            dstChannel.close();
        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
        }
    }
    
    /**
     * Loads page specified by URL
     * @param url_string URL
     * @throws java.io.IOException
     * @return downloaded page
     */
    public static String loadFromURL(String url_string) throws IOException {
        URL url = new URL(url_string);
        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
        StringBuffer sb = new StringBuffer();
        String line;
        while ((line = reader.readLine()) != null) {
            if (sb.length() > 0)
                sb.append("\n");
            sb.append(line);
        }
        reader.close();
        return sb.toString();
    }
    
    /**
     * Deploys Application
     *
     * @return downloaded page, null if url parameter was null
     * @param projectName Name of Project to deploy
     * @param url URL of page that should be downloaded, can be null.
     * @throws java.io.IOException
     */
    public static String deploy(String projectName, String url, boolean projectNameInStatus) throws IOException {
        JTreeOperator tree = ProjectsTabOperator.invoke().tree();
        tree.setComparator(new Operator.DefaultStringComparator(true, true));
        Node node = new ProjectRootNode(tree, projectName);
        node.performPopupAction(Bundle.getStringTrimmed("org.netbeans.modules.j2ee.earproject.ui.Bundle", "LBL_DeployAction_Name"));
        MainWindowOperator.getDefault().getTimeouts().setTimeout("Waiter.WaitingTime", 600000);
        MainWindowOperator.getDefault().waitStatusText(Bundle.getString("org.apache.tools.ant.module.run.Bundle", "FMT_finished_target_status", new String[] {(projectNameInStatus?projectName:"build.xml")+" (run-deploy)."}));
        if (url != null)
            return Utils.loadFromURL(url);
        return null;
    }
    
    public static String deploy(String projectnName, String url)  throws IOException {
        return deploy(projectnName, url, false);
    }
    
    /** Undeploys Application. Verifies that application node in runtime disappears.
     * @param app Name of application to undeploy
     */
    
    public static void undeploy(String category, String app) {
        RuntimeTabOperator runtimeTab = RuntimeTabOperator.invoke();
        Node rootNode = new Node(runtimeTab.getRootNode(), Bundle.getStringTrimmed("org.netbeans.modules.j2ee.deployment.impl.ui.Bundle", "SERVER_REGISTRY_NODE")
        +"|Application Server|"
                + Bundle.getStringTrimmed("org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.nodes.Bundle", "LBL_Applications") + "|"
                + category);
        rootNode.performPopupAction("Refresh");
        Node node = new Node(rootNode, app);
        node.performPopupAction(Bundle.getStringTrimmed("org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.nodes.Bundle", "LBL_Undeploy"));
        node.waitNotPresent();
    }
    
    public static void undeploy(String app) {
        undeploy(Bundle.getStringTrimmed("org.netbeans.modules.j2ee.sun.ide.j2ee.runtime.nodes.Bundle", "LBL_AppModules"), app);
    }
    
    /** Checks whether firstText in on firstLine and secondText in on secondLine. If deleteLine is true,
     *  deletes insertLine and inserts there insertText.
     */
    public void checkAndModify(String file, int firstLine, String firstText,
            int secondLine, String secondText, int insertLine, boolean deleteLine, String insertText) {
        EditorOperator editor = EditorWindowOperator.getEditor(file);
        if (firstText != null) {
            if (!(editor.getText(firstLine).indexOf(firstText)>=0))
                NbTestCase.fail("I expect text '"+firstText+"' on line "+firstLine+" in "+file+"."+
                        "There is text: '"+editor.getText(firstLine)+"'.");
        }
        if (secondText != null) {
            if (!(editor.getText(secondLine).indexOf(secondText)>=0))
                NbTestCase.fail("I expect text '"+secondText+"' on line "+secondLine+" in "+file+"."+
                        "There is text: '"+editor.getText(secondLine)+"'.");
        }
        if (deleteLine)
            editor.deleteLine(insertLine);
        if (insertText != null)
            editor.insert(insertText, insertLine, 1);
        editor.save();
    }
    
    public static void buildProject(String projectName) {
        ProjectsTabOperator pto = ProjectsTabOperator.invoke();
        Node node = pto.getProjectRootNode(projectName);
//        node.performPopupAction(Bundle.getStringTrimmed(
//                "org.netbeans.modules.j2ee.earproject.ui.Bundle", "LBL_RebuildAction_Name"));
        node.performPopupAction("Clean and Build");
        MainWindowOperator.getDefault().getTimeouts().setTimeout("Waiter.WaitingTime", 300000);
        MainWindowOperator.getDefault().waitStatusText(Bundle.getString(
                "org.apache.tools.ant.module.run.Bundle", "FMT_finished_target_status",
                new String[] {projectName.replace(' ', '_') + " (clean,dist)"}));
        new EventTool().waitNoEvent(2500);
    }
    
    public static void cleanProject(String projectName) {
        Action cleanAction = new Action(null, Bundle.getStringTrimmed(
                "org.netbeans.modules.j2ee.earproject.ui.Bundle", "LBL_RebuildAction_Name"));
        cleanAction.setComparator(new Operator.DefaultStringComparator(true, true));
        cleanAction.perform(new ProjectsTabOperator().getProjectRootNode(projectName));
        MainWindowOperator.getDefault().getTimeouts().setTimeout("Waiter.WaitingTime", 300000);
        MainWindowOperator.getDefault().waitStatusText(Bundle.getString(
                "org.apache.tools.ant.module.run.Bundle", "FMT_finished_target_status",
                new String[] {projectName.replace(' ', '_') + " (clean,dist)"}));
        new EventTool().waitNoEvent(2500);
    }
    
    public static void createLibrary(String name, String[] jars, String[] srcs, String[] javadocs) {
        if ((name == null) || (name.indexOf(" ") > -1)) {
            throw new IllegalArgumentException("Name cannot be null nor contain spaces");
        }
        if (jars == null) {
            jars = new String[0];
        }
        if (srcs == null) {
            srcs = new String[0];
        }
        if (javadocs == null) {
            javadocs = new String[0];
        }
        new ActionNoBlock("Tools|Libraries", null).performMenu();
        NbDialogOperator ndo = new NbDialogOperator(
                Bundle.getString("org.netbeans.api.project.libraries.Bundle", "TXT_LibrariesManager"));
        new JButtonOperator(ndo, Bundle.getStringTrimmed(
                "org.netbeans.modules.project.libraries.ui.Bundle", "CTL_NewLibrary")).push();
        NbDialogOperator ndo2 = new NbDialogOperator(
                Bundle.getString("org.netbeans.modules.project.libraries.ui.Bundle", "CTL_CreateLibrary"));
        JTextFieldOperator jtfo = new JTextFieldOperator(ndo2, 0);
        jtfo.clearText();
        jtfo.typeText(name);
        ndo2.ok();
        JTabbedPaneOperator jtpo = new JTabbedPaneOperator(ndo, "Classpath");
        for (int i = 0; i < jars.length; i++) {
            new JButtonOperator(jtpo, "Add JAR/Folder...").push();
            ndo2 = new NbDialogOperator("Browse JAR/Folder");
            jtfo = new JTextFieldOperator(ndo2, 0);
            jtfo.clearText();
            jtfo.typeText(jars[i]);
            new JButtonOperator(ndo2, "Add JAR/Folder").push();
        }
        jtpo.selectPage("Sources");
        for (int i = 0; i < srcs.length; i++) {
            new JButtonOperator(jtpo, "Add JAR/Folder...").push();
            ndo2 = new NbDialogOperator("Browse JAR/Folder");
            jtfo = new JTextFieldOperator(ndo2, 0);
            jtfo.clearText();
            jtfo.typeText(srcs[i]);
            new JButtonOperator(ndo2, "Add JAR/Folder").push();
        }
        jtpo.selectPage("Javadoc");
        for (int i = 0; i < javadocs.length; i++) {
            new JButtonOperator(jtpo, "Add ZIP/Folder...").push();
            ndo2 = new NbDialogOperator("Browse ZIP/Folder");
            jtfo = new JTextFieldOperator(ndo2, 0);
            jtfo.clearText();
            jtfo.typeText(javadocs[i]);
            new JButtonOperator(ndo2, "Add ZIP/Folder").push();
        }
        ndo.ok();
    }
    
    public static void openOutputTab() {
        new ActionNoBlock("Window|Output", null).performMenu();
    }
    
    public static boolean checkMissingServer(String projectName) {
        // check missing target server dialog is shown    
        // "Open Project"
        String openProjectTitle = Bundle.getString("org.netbeans.modules.j2ee.common.ui.Bundle", "MSG_Broken_Server_Title");
        boolean needToSetServer = false;
        if(JDialogOperator.findJDialog(openProjectTitle, true, true) != null) {
            new NbDialogOperator(openProjectTitle).close();
            needToSetServer = true;
        }
        // open project properties
        ProjectsTabOperator.invoke().getProjectRootNode(projectName).properties();
        // "Project Properties"
        String projectPropertiesTitle = Bundle.getStringTrimmed("org.netbeans.modules.web.project.ui.customizer.Bundle", "LBL_Customizer_Title");
        NbDialogOperator propertiesDialogOper = new NbDialogOperator(projectPropertiesTitle);
        // select "Run" category
        new Node(new JTreeOperator(propertiesDialogOper), "Run").select();
        if(needToSetServer) {
            // set default server
            JComboBox comboBox = (JComboBox) new JLabelOperator(propertiesDialogOper, "Server").getLabelFor();
            new JComboBoxOperator(comboBox).setSelectedIndex(0);
        }
        // confirm properties dialog
        propertiesDialogOper.ok();
        // if setting default server, it scans server jars; otherwise it continues immediatelly
        ProjectSupport.waitScanFinished();
        return needToSetServer;
    }
}
