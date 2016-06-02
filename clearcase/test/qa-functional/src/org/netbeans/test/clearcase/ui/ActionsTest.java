/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2008, 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 */
package org.netbeans.test.clearcase.ui;

import java.io.File;
import java.io.PrintStream;
import javax.swing.table.TableModel;
import junit.framework.Test;
import junit.textui.TestRunner;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.clearcase.operators.AddToControlOperator;
import org.netbeans.test.clearcase.operators.CheckinOperator;
import org.netbeans.test.clearcase.operators.CheckoutOperator;
import org.netbeans.test.clearcase.operators.VersioningOperator;
import org.netbeans.test.clearcase.utils.TestKit;

/**
 *
 * @author pvcs
 */
public class ActionsTest extends JellyTestCase {

    public static final String WORK_PATH = "work";
    public static String PROJECT_NAME = "JavaApp";
    public static final String TMP_PATH = "/tmp";
    public static String VIEW_LOCATION = "M:/view_mine/vobisko/peter/test";
    public File projectPath;
    public PrintStream stream;
    String os_name;
    Operator.DefaultStringComparator comOperator;
    Operator.DefaultStringComparator oldOperator;

    /** Creates a new instance of AnnotationsTest */
    public ActionsTest(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        os_name = System.getProperty("os.name");
        //System.out.println(os_name);
        System.out.println("### " + getName() + " ###");

    }

    protected boolean isUnix() {
        boolean unix = false;
        if (os_name.indexOf("Windows") == -1) {
            unix = true;
        }
        return unix;
    }

    public static void main(String[] args) {
        // TODO code application logic here
        TestRunner.run(suite());
    }

    public static Test suite() {
        return NbModuleSuite.create(NbModuleSuite.createConfiguration(ActionsTest.class).addTest(
                "testIgnore",
                "testAddToSourceControl",
                "testCheckin",
                "testCheckout",
                "finalCleanUp").enableModules(".*").clusters(".*"));
    }

    public void testCheckout() throws Exception {
        String color;
        String status;
        Node node;
        org.openide.nodes.Node openIdeNode;
        PROJECT_NAME = "JavaApp";
        
        try {
            VersioningOperator vo = VersioningOperator.invoke();
            OutputOperator oo = new OutputOperator();
            OutputTabOperator oto;
            CheckinOperator ci;
            CheckoutOperator co;
            TableModel tm;
            NbDialogOperator dialog;
            JButtonOperator btnUncheck;
////            projectPath = TestKit.prepareProject("Java", "Java Application", PROJECT_NAME);
//
            comOperator = new Operator.DefaultStringComparator(false, true);
            oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
            Operator.setDefaultStringComparator(comOperator);
            oto = oo.getOutputTab("Clearcase");
            oto.setDefaultStringComparator(oldOperator);
            oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 10000);           
//
            node = new Node(new ProjectsTabOperator().tree(), PROJECT_NAME);
            node.performPopupAction("Clearcase|Show Changes");
            oto.waitText("Refreshing status... finished.");
            //oto.clear();
            
            //Checkout - Project root node
            node = new Node(new ProjectsTabOperator().tree(), PROJECT_NAME);
            co = CheckoutOperator.invoke(node);
            co.typeCheckoutMessage("Get it!!!");
            co.checkReservedCheckout(true);
            co.checkout();        
            oto.waitText("Checking out... finished.");
            Thread.sleep(5000);
            node = new Node(new ProjectsTabOperator().tree(), PROJECT_NAME);
            openIdeNode = (org.openide.nodes.Node) node.getOpenideNode();
            color = TestKit.getColor(openIdeNode.getHtmlDisplayName());
            status = TestKit.getStatus(openIdeNode.getHtmlDisplayName());        
            assertEquals("1. Wrong color!!!", "#0000FF", color);
            assertEquals("1. Wrong status!!!", "[Reserved; \\main\\1]", status);
            assertEquals("1. Versioning tab should contain JavaApp node!!!", "JavaApp", vo.tabFiles().getValueAt(0, 0).toString());
            //Uncheckout - Project root node
            node = new Node(new ProjectsTabOperator().tree(), PROJECT_NAME);
            node.performPopupActionNoBlock("Clearcase|Uncheckout");
            dialog = new NbDialogOperator("Uncheckout");
            btnUncheck = new JButtonOperator(dialog, "Uncheckout");
            btnUncheck.push();
            oto.waitText("Undoing Checkout... finished.");
            Thread.sleep(5000);
            node = new Node(new ProjectsTabOperator().tree(), PROJECT_NAME);
            openIdeNode = (org.openide.nodes.Node) node.getOpenideNode();
            status = TestKit.getStatus(openIdeNode.getHtmlDisplayName());        
            assertEquals("2. Wrong status!!!", "", status);
            assertEquals("2. Versioning tab should be empty!!!", 0, vo.tabFiles().getRowCount());
//            
            //package node
            node = new Node(new SourcePackagesNode(PROJECT_NAME), PROJECT_NAME.toLowerCase());
            co = CheckoutOperator.invoke(node);
            co.typeCheckoutMessage("Get it!!!");
            co.checkReservedCheckout(true);
            co.checkout();        
            oto.waitText("Checking out... finished.");
            Thread.sleep(5000);
            node = new Node(new SourcePackagesNode(PROJECT_NAME), PROJECT_NAME.toLowerCase());
            openIdeNode = (org.openide.nodes.Node) node.getOpenideNode();
            color = TestKit.getColor(openIdeNode.getHtmlDisplayName());
            status = TestKit.getStatus(openIdeNode.getHtmlDisplayName());        
            assertEquals("3. Wrong color!!!", "#0000FF", color);
            assertEquals("3. Wrong status!!!", "[Reserved; \\main\\1]", status);
            assertEquals("3. Versioning tab should contain JavaApp node!!!", "javaapp", vo.tabFiles().getValueAt(0, 0).toString());
            //Uncheckout - Project root node
            node = new Node(new SourcePackagesNode(PROJECT_NAME), PROJECT_NAME.toLowerCase());
            node.performPopupActionNoBlock("Clearcase|Uncheckout");
            dialog = new NbDialogOperator("Uncheckout");
            btnUncheck = new JButtonOperator(dialog, "Uncheckout");
            btnUncheck.push();
            oto.waitText("Undoing Checkout... finished.");
            Thread.sleep(5000);
            node = new Node(new SourcePackagesNode(PROJECT_NAME), PROJECT_NAME.toLowerCase());
            openIdeNode = (org.openide.nodes.Node) node.getOpenideNode();
            status = TestKit.getStatus(openIdeNode.getHtmlDisplayName());        
            assertEquals("4. Wrong status!!!", "", status);
            assertEquals("4. Versioning tab should be empty!!!", 0, vo.tabFiles().getRowCount());
////            
            //file node
            node = new Node(new SourcePackagesNode(PROJECT_NAME), PROJECT_NAME.toLowerCase() + "|Main.java");
            co = CheckoutOperator.invoke(node);
            co.typeCheckoutMessage("Get it!!!");
            co.checkReservedCheckout(true);
            co.checkout();        
            oto.waitText("Checking out... finished.");
            Thread.sleep(5000);
            node = new Node(new SourcePackagesNode(PROJECT_NAME), PROJECT_NAME.toLowerCase() + "|Main.java");
            openIdeNode = (org.openide.nodes.Node) node.getOpenideNode();
            color = TestKit.getColor(openIdeNode.getHtmlDisplayName());
            status = TestKit.getStatus(openIdeNode.getHtmlDisplayName());        
            assertEquals("5. Wrong color!!!", "#0000FF", color);
            assertEquals("5. Wrong status!!!", "[Reserved; \\main\\1]", status);
            assertEquals("5. Versioning tab should contain JavaApp node!!!", "Main.java", vo.tabFiles().getValueAt(0, 0).toString());
            //Uncheckout - Project root node
            node = new Node(new SourcePackagesNode(PROJECT_NAME), PROJECT_NAME.toLowerCase() + "|Main.java");
            node.performPopupActionNoBlock("Clearcase|Uncheckout");
            dialog = new NbDialogOperator("Uncheckout");
            btnUncheck = new JButtonOperator(dialog, "Uncheckout");
            btnUncheck.push();
            oto.waitText("Undoing Checkout... finished.");
            Thread.sleep(5000);
            node = new Node(new SourcePackagesNode(PROJECT_NAME), PROJECT_NAME.toLowerCase() + "|Main.java");
            openIdeNode = (org.openide.nodes.Node) node.getOpenideNode();
            status = TestKit.getStatus(openIdeNode.getHtmlDisplayName());        
            assertEquals("6. Wrong status!!!", "", status);
            assertEquals("6. Versioning tab should be empty!!!", 0, vo.tabFiles().getRowCount());
                     
        } catch (Throwable e) {
            throw new Exception(e);
        } finally {
            
        }
    }

    public void testActions() throws Exception {
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 30000);
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 30000);
        //System.out.println("Test!!!");
        try {
            TestKit.closeProject(PROJECT_NAME);
            projectPath = TestKit.prepareProject("Java", "Java Application", PROJECT_NAME);
            Node node = new Node(new ProjectsTabOperator().tree(), PROJECT_NAME);
            AddToControlOperator atco = AddToControlOperator.invoke(node);
            atco.verify();
            atco.cancel();
            CheckinOperator ci = CheckinOperator.invoke(node);
            ci.verify();
            ci.cancel();
        //CheckoutOperator co = CheckoutOperator.invoke(node);
        //co.verify();
        //co.cancel();
        } catch (Throwable e) {
            throw new Exception("Test failed: " + e);
        } finally {
            TestKit.closeProject(PROJECT_NAME);
        }
    }

    public void testIgnore() throws Exception {
        PROJECT_NAME = "JavaApp";
        String[] filesToAdd = new String[] {
            PROJECT_NAME, PROJECT_NAME.toLowerCase(), "build.xml", "manifest.mf", "nbproject", "build-impl.xml", 
            "genfiles.properties", "project.properties", "project.xml", "src", "test", "Main.java"
        };
        String color;
        String status;
        Node node;
        TimeoutExpiredException tee;
        OutputTabOperator oto;
        org.openide.nodes.Node openIdeNode;
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 10000);
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 10000);
        
        //System.out.println("Test!!!");
        try {
            VersioningOperator vo = VersioningOperator.invoke();
            OutputOperator oo = new OutputOperator();
            projectPath = TestKit.prepareProject("Java", "Java Application", PROJECT_NAME);

            comOperator = new Operator.DefaultStringComparator(false, true);
            oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
            Operator.setDefaultStringComparator(comOperator);
            oto = oo.getOutputTab("Clearcase");
            oto.setDefaultStringComparator(oldOperator);
            oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 10000);           

            //Ignore/Unignore project root node
            node = new Node(new ProjectsTabOperator().tree(), PROJECT_NAME);
            node.performPopupAction("Clearcase|Show Changes");
            oto.waitText("Refreshing status... finished.");
            openIdeNode = (org.openide.nodes.Node) node.getOpenideNode();
            color = TestKit.getColor(openIdeNode.getHtmlDisplayName());
            status = TestKit.getStatus(openIdeNode.getHtmlDisplayName());
            assertEquals("1. Color should be green!!!", TestKit.NEW_COLOR, color);
            assertEquals("1. Status should be [New; ]!!!", TestKit.NEW_STATUS, status);
            //Verify Clearcase tab
            TableModel tm = vo.tabFiles().getModel();
            assertEquals(12, tm.getRowCount());
            String[] actual = new String[tm.getRowCount()];
            for (int i = 0; i < tm.getRowCount(); i++) {
                actual[i] = tm.getValueAt(i, 0).toString();
            }
            int result = TestKit.compareThem(filesToAdd, actual, false);
            assertEquals("2. ", filesToAdd.length, result);
           
//            Node node = new Node(new SourcePackagesNode(PROJECT_NAME), PROJECT_NAME.toLowerCase() + "|NewClass");
            node.performPopupAction("Clearcase|Ignore");
            oto = oo.getOutputTab("Clearcase");
            oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
            oto.waitText("Ignoring... finished.");
            Thread.sleep(5000);
            node = new Node(new ProjectsTabOperator().tree(), PROJECT_NAME);
            openIdeNode = (org.openide.nodes.Node) node.getOpenideNode();
            color = TestKit.getColor(openIdeNode.getHtmlDisplayName());
            status = TestKit.getStatus(openIdeNode.getHtmlDisplayName());
            assertEquals("3. Color should be grey!!!", TestKit.IGNORED_COLOR, color);
            assertEquals("3. Status should be [Ignored]!!!", TestKit.IGNORED_STATUS, status);
            
            oto.clear();
//            tee = null;
//            try {
//                node.performPopupAction("Clearcase|Ignore");
//            } catch (Exception e) {
//                tee = (TimeoutExpiredException) e;
//            }
//            assertNotNull(tee);
            node.performPopupAction("Clearcase|Unignore");
            oto.waitText("Unignoring... finished.");
            Thread.sleep(5000);
            
            //Ignore/Unignore package node
            oto.clear();
            node = new Node(new SourcePackagesNode(PROJECT_NAME), PROJECT_NAME.toLowerCase());
            openIdeNode = (org.openide.nodes.Node) node.getOpenideNode();
            color = TestKit.getColor(openIdeNode.getHtmlDisplayName());
            status = TestKit.getStatus(openIdeNode.getHtmlDisplayName());
            assertEquals("4. Color should be green!!!", TestKit.NEW_COLOR, color);
            assertEquals("4. Status should be [New]!!!", TestKit.NEW_STATUS, status);
            node.performPopupAction("Clearcase|Ignore");
            oto.waitText("Ignoring... finished.");
            Thread.sleep(5000);
            node = new Node(new SourcePackagesNode(PROJECT_NAME), PROJECT_NAME.toLowerCase());
            openIdeNode = (org.openide.nodes.Node) node.getOpenideNode();
            color = TestKit.getColor(openIdeNode.getHtmlDisplayName());
            status = TestKit.getStatus(openIdeNode.getHtmlDisplayName());
            assertEquals("5. Color should be grey!!!", TestKit.IGNORED_COLOR, color);
            assertEquals("5. Status should be [Ignored]!!!", TestKit.IGNORED_STATUS, status);
            tee = null;
//            try {
//                node.performPopupAction("Clearcase|Ignore");
//            } catch (Exception e) {
//                tee = (TimeoutExpiredException) e;
//            }
//            assertNotNull(tee);
            oto.clear();
            node.performPopupAction("Clearcase|Unignore");
            oto.waitText("Unignoring... finished.");
            Thread.sleep(5000);
            
            node = new Node(new SourcePackagesNode(PROJECT_NAME), PROJECT_NAME.toLowerCase());
            openIdeNode = (org.openide.nodes.Node) node.getOpenideNode();
            color = TestKit.getColor(openIdeNode.getHtmlDisplayName());
            status = TestKit.getStatus(openIdeNode.getHtmlDisplayName());
            assertEquals("6. Color should be grey!!!", TestKit.NEW_COLOR, color);
            assertEquals("6. Status should be [Ignored]!!!", TestKit.NEW_STATUS, status);

            //Ignore/Unignore file
            oto.clear();
            node = new Node(new SourcePackagesNode(PROJECT_NAME), PROJECT_NAME.toLowerCase() + "|Main.java");
            openIdeNode = (org.openide.nodes.Node) node.getOpenideNode();
            color = TestKit.getColor(openIdeNode.getHtmlDisplayName());
            status = TestKit.getStatus(openIdeNode.getHtmlDisplayName());
            assertEquals("7. Color should be green!!!", TestKit.NEW_COLOR, color);
            assertEquals("7. Status should be [New]!!!", TestKit.NEW_STATUS, status);
            node.performPopupAction("Clearcase|Ignore");
            oto.waitText("Ignoring... finished.");
            Thread.sleep(5000);
            node = new Node(new SourcePackagesNode(PROJECT_NAME), PROJECT_NAME.toLowerCase() + "|Main.java");
            openIdeNode = (org.openide.nodes.Node) node.getOpenideNode();
            color = TestKit.getColor(openIdeNode.getHtmlDisplayName());
            status = TestKit.getStatus(openIdeNode.getHtmlDisplayName());
            assertEquals("8. Color should be grey!!!", TestKit.IGNORED_COLOR, color);
            assertEquals("8. Status should be [Ignored]!!!", TestKit.IGNORED_STATUS, status);
//            tee = null;
//            try {
//                node.performPopupAction("Clearcase|Ignore");
//            } catch (Exception e) {
//                tee = (TimeoutExpiredException) e;
//            }
//            assertNotNull(tee);
            oto.clear();
            node.performPopupAction("Clearcase|Unignore");
            oto.waitText("Unignoring... finished.");
            Thread.sleep(5000);
            node = new Node(new SourcePackagesNode(PROJECT_NAME), PROJECT_NAME.toLowerCase() + "|Main.java");
            openIdeNode = (org.openide.nodes.Node) node.getOpenideNode();
            color = TestKit.getColor(openIdeNode.getHtmlDisplayName());
            status = TestKit.getStatus(openIdeNode.getHtmlDisplayName());
            assertEquals("9. Color should be grey!!!", TestKit.NEW_COLOR, color);
            assertEquals("9. Status should be [Ignored]!!!", TestKit.NEW_STATUS, status);

        } catch (Throwable e) {
            throw new Exception("Test failed: " + e);
        } finally {
//            TestKit.closeProject(PROJECT_NAME);
        }
    }
    
    public void testCheckin() throws Exception {
        String color;
        String status;
        Node node;
        org.openide.nodes.Node openIdeNode;
        PROJECT_NAME = "JavaApp";
        
        try {
            VersioningOperator vo = VersioningOperator.invoke();
            OutputOperator oo = new OutputOperator();
            OutputTabOperator oto;
            CheckinOperator ci;
            TableModel tm;
//            projectPath = TestKit.prepareProject("Java", "Java Application", PROJECT_NAME);
//
            comOperator = new Operator.DefaultStringComparator(false, true);
            oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
            Operator.setDefaultStringComparator(comOperator);
            oto = oo.getOutputTab("Clearcase");
            oto.setDefaultStringComparator(oldOperator);
            oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 10000);           
//
//            node = new Node(new ProjectsTabOperator().tree(), PROJECT_NAME);
//            node.performPopupAction("Clearcase|Show Changes");
//            oto.waitText("Refreshing status... finished.");
//        
//            //Project root node
            node = new Node(new ProjectsTabOperator().tree(), PROJECT_NAME);
            openIdeNode = (org.openide.nodes.Node) node.getOpenideNode();
            color = TestKit.getColor(openIdeNode.getHtmlDisplayName());
            status = TestKit.getStatus(openIdeNode.getHtmlDisplayName());
            assertEquals("1. Wrong color!!!", "#0000FF", color);
            assertEquals("2. Wrong status!!!", "[Reserved; \\main\\0]", status);
//            
//            //package node
            node = new Node(new SourcePackagesNode(PROJECT_NAME), PROJECT_NAME.toLowerCase());
            openIdeNode = (org.openide.nodes.Node) node.getOpenideNode();
            color = TestKit.getColor(openIdeNode.getHtmlDisplayName());
            status = TestKit.getStatus(openIdeNode.getHtmlDisplayName());
            assertEquals("1. Wrong color!!!", TestKit.MODIFIED_COLOR, color);
            assertEquals("2. Wrong status!!!", "[Reserved; \\main\\0]", status);
//            
//            //file node
            node = new Node(new SourcePackagesNode(PROJECT_NAME), PROJECT_NAME.toLowerCase()+ "|Main.java");
            openIdeNode = (org.openide.nodes.Node) node.getOpenideNode();
            color = TestKit.getColor(openIdeNode.getHtmlDisplayName());
            status = TestKit.getStatus(openIdeNode.getHtmlDisplayName());
            assertEquals("1. Wrong color!!!", TestKit.MODIFIED_COLOR, color);
            assertEquals("2. Wrong status!!!", "[Reserved; \\main\\0]", status);
            
//            //checkin file
            node = new Node(new SourcePackagesNode(PROJECT_NAME), PROJECT_NAME.toLowerCase()+ "|Main.java");
            openIdeNode = (org.openide.nodes.Node) node.getOpenideNode();
            ci = CheckinOperator.invoke(node);
            oto.waitText("Preparing Checkin... finished.");
            tm = ci.tabFilesToCheckin().getModel();
            assertEquals("1. Wrong count of records in Checkin table!!!", 1, tm.getRowCount());
            assertEquals("1. There should be Main.java node!!!", "Main.java", tm.getValueAt(0, 0).toString().trim());
            ci.checkin();
            oto.waitText("Checking in... finished.");
            Thread.sleep(5000);
            node = new Node(new SourcePackagesNode(PROJECT_NAME), PROJECT_NAME.toLowerCase()+ "|Main.java");
            openIdeNode = (org.openide.nodes.Node) node.getOpenideNode();
            color = TestKit.getColor(openIdeNode.getHtmlDisplayName());
            status = TestKit.getStatus(openIdeNode.getHtmlDisplayName());
            assertEquals("2. Wrong status!!!", "", status);
            
            //checkin package
            node = new Node(new SourcePackagesNode(PROJECT_NAME), PROJECT_NAME.toLowerCase());
            openIdeNode = (org.openide.nodes.Node) node.getOpenideNode();
            ci = CheckinOperator.invoke(node);
            oto.waitText("Preparing Checkin... finished.");
            tm = ci.tabFilesToCheckin().getModel();
            assertEquals("3. Wrong count of records in Checkin table!!!", 1, tm.getRowCount());
            assertEquals("3. There should be javaapp node!!!", "javaapp", tm.getValueAt(0, 0).toString().trim());
            ci.checkin();
            oto.waitText("Checking in... finished.");
            Thread.sleep(5000);
            node = new Node(new SourcePackagesNode(PROJECT_NAME), PROJECT_NAME.toLowerCase());
            openIdeNode = (org.openide.nodes.Node) node.getOpenideNode();
            status = TestKit.getStatus(openIdeNode.getHtmlDisplayName());
            assertEquals("4. Wrong status!!!", "", status);
            
            //checkin Projecr
            oto.clear();
            node = new Node(new ProjectsTabOperator().tree(), PROJECT_NAME);
            openIdeNode = (org.openide.nodes.Node) node.getOpenideNode();
            ci = CheckinOperator.invoke(node);
            oto.waitText("Preparing Checkin... finished.");
            tm = ci.tabFilesToCheckin().getModel();
            assertEquals("5. Wrong count of records in Checkin table!!!", 10, tm.getRowCount());
//            assertEquals("5. There should be javaapp node!!!", "javaapp", tm.getValueAt(0, 0).toString().trim());
            ci.checkin();
            oto.waitText("Checking in... finished.");
            Thread.sleep(5000);
            node = new Node(new ProjectsTabOperator().tree(), PROJECT_NAME);
            openIdeNode = (org.openide.nodes.Node) node.getOpenideNode();
            status = TestKit.getStatus(openIdeNode.getHtmlDisplayName());
            assertEquals("6. Wrong status!!!", "", status);
                     
        } catch (Throwable e) {
            throw new Exception(e);
        } finally {
            
        }
    }

    public void testAddToSourceControl() throws Exception {
        long unique = System.currentTimeMillis();
        PROJECT_NAME = "JavaApp";
        
        String[] output = new String[] {
            "Created directory element \"JavaApp" + unique + "\".",
            "Created element \"JavaApp" + unique + "\" (type \"directory\").",
            "Checked out \"JavaApp" + unique + "\" from version \"\\main\\0\".",
            "Created element \"JavaApp" + unique + "\\build.xml\" (type \"xml\").",
            "Checked out \"JavaApp" + unique + "\\build.xml\" from version \"\\main\\0\".",
            "Created element \"JavaApp" + unique + "\\manifest.mf\" (type \"text_file\").",
            "Checked out \"JavaApp" + unique + "\\manifest.mf\" from version \"\\main\\0\".",
            "Created directory element \"JavaApp" + unique + "\\nbproject\".",
            "Created element \"JavaApp" + unique + "\\nbproject\" (type \"directory\").",
            "Checked out \"JavaApp" + unique + "\\nbproject\" from version \"\\main\\0\".",
            "Created element \"JavaApp" + unique + "\\nbproject\\build-impl.xml\" (type \"xml\").",
            "Checked out \"JavaApp" + unique + "\\nbproject\\build-impl.xml\" from version \"\\main\\0\".",
            "Created element \"JavaApp" + unique + "\\nbproject\\genfiles.properties\" (type \"text_file\").",
            "Checked out \"JavaApp" + unique + "\\nbproject\\genfiles.properties\" from version \"\\main\\0\".",
            "Created element \"JavaApp" + unique + "\\nbproject\\project.properties\" (type \"text_file\").",
            "Checked out \"JavaApp" + unique + "\\nbproject\\project.properties\" from version \"\\main\\0\".",
            "Created element \"JavaApp" + unique + "\\nbproject\\project.xml\" (type \"xml\").",
            "Checked out \"JavaApp" + unique + "\\nbproject\\project.xml\" from version \"\\main\\0\".",
            "Created directory element \"JavaApp" + unique + "\\src\".",
            "Created element \"JavaApp" + unique + "\\src\" (type \"directory\").",
            "Checked out \"JavaApp" + unique + "\\src\" from version \"\\main\\0\".",
            "Created directory element \"JavaApp" + unique + "\\src\\javaapp" + unique + "\".",
            "Created element \"JavaApp" + unique + "\\src\\JavaApp" + unique + "\" (type \"directory\").",
            "Checked out \"JavaApp" + unique + "\\src\\JavaApp" + unique + "\" from version \"\\main\\0\".",
            "Created element \"JavaApp" + unique + "\\src\\javaapp" + unique + "\\Main.java\" (type \"text_file\").",
            "Checked out \"JavaApp" + unique + "\\src\\javaapp" + unique + "\\Main.java\" from version \"\\main\\0\".",
            "Created directory element \"JavaApp" + unique + "\\test\".",
            "Created element \"JavaApp" + unique + "\\test\" (type \"directory\").",
            "Checked out \"JavaApp" + unique + "\\test\" from version \"\\main\\0\"."
        };
        
        String[] filesToAdd = new String[] {
            "JavaApp", "javaapp", "build.xml", "manifest.mf", "nbproject", "build-impl.xml", 
            "genfiles.properties", "project.properties", "project.xml", "src", "test", "Main.java"
        };

        try {
            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 30000);
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 30000);
//            TestKit.closeProject(PROJECT_NAME);
            
            OutputOperator oo = OutputOperator.invoke();
//            projectPath = TestKit.prepareProject("Java", "Java Application", PROJECT_NAME);

            comOperator = new Operator.DefaultStringComparator(false, true);
            oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
            Operator.setDefaultStringComparator(comOperator);
            OutputTabOperator oto = oo.getOutputTab("Clearcase");
            oto.setDefaultStringComparator(oldOperator);
            
            oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);

            oto.clear();
            
            Thread.sleep(3000);
            new ProjectsTabOperator().tree().clearSelection();
            Node node = new Node(new ProjectsTabOperator().tree(), PROJECT_NAME);
            AddToControlOperator atco = AddToControlOperator.invoke(node);
            atco.verify();
            atco.typeJTextArea("initial message");
            oto.waitText("Preparing Add To... finished");
            
            Thread.sleep(3000);
            
            TableModel tm = atco.tabFilesToAdd().getModel();
            assertEquals(12, tm.getRowCount());
            String[] actual = new String[tm.getRowCount()];
            for (int i = 0; i < tm.getRowCount(); i++) {
                actual[i] = (String) tm.getValueAt(i, 0);
            }
            int result = TestKit.compareThem(filesToAdd, actual, false);
            assertEquals(filesToAdd.length, result);
            
            atco.add();
//            for (String line : output) {
//                oto.waitText(line);
//            }

            oto.waitText("Adding... finished.");
        } catch (Throwable e) {
            throw new Exception("Test failed: " + e);
        } finally {
//            
        }
    }
    
    public void finalCleanUp() throws Exception {
        TestKit.closeProject(PROJECT_NAME);
        String[] cmds = {"cleartool", "co", "-c", "delete", "."};
        TestKit.execute(cmds, null, new File(TestKit.VIEW_LOCATION));
        cmds = new String[]{"cleartool", "unco", PROJECT_NAME};
        TestKit.execute(cmds, null, new File(TestKit.VIEW_LOCATION));
        cmds = new String[]{"cleartool", "rmelem", "-force", PROJECT_NAME};
        TestKit.execute(cmds, null, new File(TestKit.VIEW_LOCATION));
        cmds = new String[]{"cleartool", "ci", "-c", "checkin", "."};
        TestKit.execute(cmds, null, new File(TestKit.VIEW_LOCATION));
        TestKit.deleteFolder(projectPath);
    }
}
