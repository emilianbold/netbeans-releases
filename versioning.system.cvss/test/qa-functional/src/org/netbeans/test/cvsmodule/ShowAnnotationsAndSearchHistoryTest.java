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
 * ShowAnnotationsAndSearchHistoryTest.java
 *7
 * Created on 18. prosinec 2006, 9:47
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.test.cvsmodule;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ListModel;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.modules.javacvs.CVSRootStepOperator;
import org.netbeans.jellytools.modules.javacvs.CheckoutWizardOperator;
import org.netbeans.jellytools.modules.javacvs.ModuleToCheckoutStepOperator;
import org.netbeans.jellytools.modules.javacvs.SearchHistoryOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.ide.ProjectSupport;

/**
 *
 * @author novakm
 */
public class ShowAnnotationsAndSearchHistoryTest extends JellyTestCase {
    static String os_name;
    static String sessionCVSroot;
    final String projectName = "ForImport";
    final String pathToMain = "forimport|Main.java";
    final String PROTOCOL_FOLDER = "protocol";
    static File cacheFolder;
    Operator.DefaultStringComparator comOperator;
    Operator.DefaultStringComparator oldOperator;
    static Logger log;
    /** Creates a new instance of ShowAnnotationsAndSearchHistoryTest */
    public ShowAnnotationsAndSearchHistoryTest(String name) {
        super(name);
        if (os_name == null) {
            os_name = System.getProperty("os.name");
        }
        try {
            TestKit.extractProtocol(getDataDir());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    protected void setUp() throws Exception {
        System.out.println("### " + getName() + " ###");

        if (log == null) {
            log = Logger.getLogger("org.netbeans.modules.versioning.system.cvss.t9y");
            log.setLevel(Level.ALL);
            TestKit.removeHandlers(log);
        } else {
            TestKit.removeHandlers(log);
        }
    }
    
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(ShowAnnotationsAndSearchHistoryTest.class).addTest(
                     "testCheckOutProject",
                     //"testSearchHistory",
                     "testShowAnnotations",
                     "removeAllData"
                )
                .enableModules(".*")
                .clusters(".*")
        );
     }
    
    public void testCheckOutProject() throws Exception {
        MessageHandler mh = new MessageHandler("Checking out");
        log.addHandler(mh);

        TestKit.closeProject(projectName);
        TestKit.showStatusLabels();
//
        if ((os_name !=null) && (os_name.indexOf("Mac") > -1))
            NewProjectWizardOperator.invoke().close();

        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(comOperator);
        CheckoutWizardOperator cwo = CheckoutWizardOperator.invoke();
        Operator.setDefaultStringComparator(oldOperator);
        CVSRootStepOperator crso = new CVSRootStepOperator();
        crso.setCVSRoot(":pserver:anoncvs@localhost:/cvs");
        //prepare stream for successful authentification and run PseudoCVSServer
        InputStream in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "authorized.in");
        PseudoCvsServer cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        cvss.ignoreProbe();
        String CVSroot = cvss.getCvsRoot();
        sessionCVSroot = CVSroot;
        crso.setCVSRoot(CVSroot);
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        crso.next();
        
        //2nd step of CheckOutWizard
        File tmp = new File("/tmp"); // NOI18N
        File work = new File(tmp, "" + File.separator + System.currentTimeMillis());
        cacheFolder = new File(work, projectName + File.separator + "src" + File.separator + "forimport" + File.separator + "CVS" + File.separator + "RevisionCache");
        tmp.mkdirs();
        work.mkdirs();
        tmp.deleteOnExit();
        ModuleToCheckoutStepOperator moduleCheck = new ModuleToCheckoutStepOperator();
        cvss.stop();
        try {
            new EventTool().waitNoEvent(1000);
            in.close();
        } catch (IOException e) {
            //
        }
        moduleCheck.setModule("ForImport");
        moduleCheck.setLocalFolder(work.getAbsolutePath()); // NOI18N
        //Pseudo CVS server for finishing check out wizard
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "checkout_finish_2.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        CVSroot = cvss.getCvsRoot();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        cwo.finish();
        TestKit.waitText(mh);
        cvss.stop();
        
        NbDialogOperator nbdialog = new NbDialogOperator("Checkout Completed");
        JButtonOperator open = new JButtonOperator(nbdialog, "Open Project");
        open.push();
        
        ProjectSupport.waitScanFinished();
//        TestKit.waitForQueueEmpty();
//        ProjectSupport.waitScanFinished();
        
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }
            
    public void testShowAnnotations() throws Exception {
        PseudoCvsServer cvss, cvss2;
        InputStream in, in2;
        org.openide.nodes.Node nodeIDE;
        String color, cvsRoot;
               
        TestKit.deleteRecursively(cacheFolder);
        
        MessageHandler mh = new MessageHandler("Loading Annotation");
        log.addHandler(mh);
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "show_annotation.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        
        in2 = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "show_annotation_1.in");
        cvss2 = new PseudoCvsServer(in2);
        new Thread(cvss2).start();
        
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", cvss.getCvsRoot() + "," + cvss2.getCvsRoot());
        Node nodeMain = new Node(new SourcePackagesNode("ForImport"), "forimport|Main.java");
        
        nodeMain.performPopupAction("CVS|Show Annotations");
        new EventTool().waitNoEvent(1000);
        TestKit.waitText(mh);
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
        cvss.stop();
        cvss2.stop();
    }
    
    public void testSearchHistory() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 18000);
        PseudoCvsServer cvss, cvss2;
        InputStream in, in2;
        
        org.openide.nodes.Node nodeIDE;
        String color, cvsRoot;
        TestKit.deleteRecursively(cacheFolder);

        MessageHandler mh = new MessageHandler("Searching History");
        log.addHandler(mh);

        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "search_history.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", cvss.getCvsRoot());
        Node nodeMain = new Node(new SourcePackagesNode("ForImport"), "forimport|Main.java");
        //nodeMain.performPopupAction("CVS|Search History...");
        SearchHistoryOperator sho = SearchHistoryOperator.invoke(nodeMain);
        TestKit.waitText(mh);
        new EventTool().waitNoEvent(1000);
        cvss.stop();
        
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "search_history_1.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", cvss.getCvsRoot());
        sho.setUsername("test");
        sho.setFrom("1.1");
        sho.setTo("1.1");
        sho.btSearch().push();
        mh = new MessageHandler("Searching History");
        TestKit.removeHandlers(log);
        log.addHandler(mh);

        new EventTool().waitNoEvent(1000);
        cvss.stop();
        JListOperator list = sho.lstHistory();
        ListModel model = list.getModel();
        assertEquals("Wrong result count", 2, model.getSize());
        assertTrue("Revision \"1.1\" is missing", model.getElementAt(1).toString().indexOf("1.1") > 0);
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }
    
    public void removeAllData() throws Exception {
        TestKit.closeProject(projectName);
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }
}
