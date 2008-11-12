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

package org.netbeans.test.cvsmodule;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OutputOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.modules.javacvs.CVSRootStepOperator;
import org.netbeans.jellytools.modules.javacvs.CheckoutWizardOperator;
import org.netbeans.jellytools.modules.javacvs.ModuleToCheckoutStepOperator;
import org.netbeans.jellytools.modules.javacvs.TagOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.ide.ProjectSupport;
/**
 *
 * @author peter
 */
public class TagTest extends JellyTestCase {
    
    String os_name;
    static String sessionCVSroot;
    boolean unix = false;
    final String projectName = "ForImport";
    final String pathToMain = "forimport|Main.java";
    final String PROTOCOL_FOLDER = "protocol";
    Operator.DefaultStringComparator comOperator; 
    Operator.DefaultStringComparator oldOperator;
    
    /** Creates a new instance of TagTest */
    public TagTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        os_name = System.getProperty("os.name");
        //System.out.println(os_name);
        System.out.println("### "+getName()+" ###");
        try {
            TestKit.extractProtocol(getDataDir());
        } catch (Exception e ) {
            e.printStackTrace();
        } 
    }
    
    protected boolean isUnix() {
        boolean _unix = false;
        if (os_name.indexOf("Windows") == -1) {
            _unix = true;
        }
        return _unix;
    }
    
    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(TagTest.class).addTest(
                      "testCheckOutProject", "testTagDialogUI", "testCreateNewTag",
                      "testCreateTagOnModified", "testOnNonVersioned", "removeAllData"
                )
                .enableModules(".*")
                .clusters(".*")
        );
     }
    
    public void testCheckOutProject() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 36000);
        //JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 36000);
        TestKit.closeProject(projectName);
        new ProjectsTabOperator().tree().clearSelection();
        OutputOperator oo = OutputOperator.invoke();
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(comOperator);
        CheckoutWizardOperator cwo = CheckoutWizardOperator.invoke();
        Operator.setDefaultStringComparator(oldOperator);
        CVSRootStepOperator crso = new CVSRootStepOperator();
        //JComboBoxOperator combo = new JComboBoxOperator(crso, 0);
        crso.setCVSRoot(":pserver:anoncvs@localhost:/cvs");
        //crso.setPassword("");
        //crso.setPassword("test");
        
        //prepare stream for successful authentification and run PseudoCVSServer
        //InputStream in = getClass().getResourceAsStream("authorized.in");   
        InputStream in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "authorized.in");   
        PseudoCvsServer cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        cvss.ignoreProbe();
        String CVSroot = cvss.getCvsRoot();
        sessionCVSroot = CVSroot;
        crso.setCVSRoot(CVSroot);
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        crso.next();
              
        //second step of checkoutwizard
        //2nd step of CheckOutWizard
        
        File tmp = new File("/tmp"); // NOI18N
        File work = new File(tmp, "" + File.separator + System.currentTimeMillis());
        tmp.mkdirs();
        work.mkdirs();
        tmp.deleteOnExit();
        ModuleToCheckoutStepOperator moduleCheck = new ModuleToCheckoutStepOperator();
        cvss.stop();
        try {
            Thread.sleep(1000);
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
        //cvss.ignoreProbe();
        
        //crso.setCVSRoot(CVSroot);
        //combo.setSelectedItem(CVSroot);
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", CVSroot);
        cwo.finish();
        Thread.sleep(3000);
        
        
        //System.out.println(CVSroot);
        //sessionCVSroot = CVSroot;
        OutputTabOperator oto = new OutputTabOperator(sessionCVSroot); 
        oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
        oto.waitText("Checking out finished");
        cvss.stop();
        try {
            Thread.sleep(1000);
            in.close();
        } catch (IOException e) {
            //
        }
        NbDialogOperator nbdialog = new NbDialogOperator("Checkout Completed");
        JButtonOperator open = new JButtonOperator(nbdialog, "Open Project");
        open.push();
        
        //ProjectSupport.waitScanFinished();
        //TestKit.waitForQueueEmpty()
        ProjectSupport.waitScanFinished();
        
        //create new elements for testing
        TestKit.createNewElements(projectName);
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }
    
    public void testTagDialogUI() throws Exception {
        Node node = new Node(new SourcePackagesNode(projectName), pathToMain);
        TagOperator to = TagOperator.invoke(node);
        to.setTagName("TagTest");
        
        //System.out.println("Error in dialog buttons - OK -> Tag, Help -> missing!!!");
        new JButtonOperator(to, "Tag");
        new JButtonOperator(to, "Help");
        new JButtonOperator(to, "Cancel");
        
        to.checkAvoidTaggingLocallyModifiedFiles(false);
        //
        assertFalse(to.cbAvoidTaggingLocallyModifiedFiles().isSelected());
        //
        to.checkAvoidTaggingLocallyModifiedFiles(true);
        assertTrue(to.cbAvoidTaggingLocallyModifiedFiles().isSelected());
        //
        to.checkMoveExistingTag(false);
        assertFalse(to.cbMoveExistingTag().isSelected());
        //
        to.checkMoveExistingTag(true);
        assertTrue(to.cbMoveExistingTag().isSelected());
        to.cancel();
    }
    
    public void testCreateNewTag() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 3000);
        //JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 3000);
        PseudoCvsServer cvss;
        InputStream in;
        
        Node node = new Node(new SourcePackagesNode(projectName), pathToMain);
        TagOperator to = TagOperator.invoke(node);
        to.setTagName("MyNewTag");
        
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "create_new_tag.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", cvss.getCvsRoot());
        JButtonOperator btnTag = new JButtonOperator(to, "Tag");
        btnTag.push();
        
        //OutputOperator oo = OutputOperator.invoke();
        OutputTabOperator oto = new OutputTabOperator(sessionCVSroot); 
        oto.waitText("Tagging \"Main.java\"... finished");
        cvss.stop();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }
    
    public void testCreateTagOnModified() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 3000);
        //JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 3000);
        PseudoCvsServer cvss;
        InputStream in;
        
        Node node = new Node(new SourcePackagesNode(projectName), pathToMain);
        node.performPopupAction("Open");
        EditorOperator eo = new EditorOperator("Main.java");
        eo.insert("//Comment\n");
        eo.save();
        Thread.sleep(1000);
        
        TagOperator to = TagOperator.invoke(node);
        to.setTagName("MyNewTag");
        to.checkAvoidTaggingLocallyModifiedFiles(false);
        
        in = TestKit.getStream(getDataDir().getCanonicalFile().toString() + File.separator + PROTOCOL_FOLDER, "create_new_tag_on_modified.in");
        cvss = new PseudoCvsServer(in);
        new Thread(cvss).start();
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", cvss.getCvsRoot());
        JButtonOperator btnTag = new JButtonOperator(to, "Tag");
        btnTag.push();
        
        //OutputOperator oo = OutputOperator.invoke();
        OutputTabOperator oto = new OutputTabOperator(sessionCVSroot); 
        oto.waitText("cvs server: Main.java is locally modified");
        oto.waitText("correct the above errors first!");
        cvss.stop();
        
        NbDialogOperator nbd = new NbDialogOperator("Command Failed");
        JButtonOperator btnOk = new JButtonOperator(nbd, "OK");
        btnOk.push();
        oto.waitText("Tagging \"Main.java\"... finished");
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }
    
    public void testOnNonVersioned() throws Exception{
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 3000);
        //JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 3000);
        //delete fake versioning of file
        //TestKit.unversionProject(file, projNonName);
        
        TimeoutExpiredException tee = null;
        try {
            Node node = new Node(new SourcePackagesNode(projectName), "xx|NewClass.java");
            TagOperator bo = TagOperator.invoke(node);
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull(tee);
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }
    
    public void removeAllData() throws Exception {
        TestKit.closeProject(projectName);
        System.setProperty("netbeans.t9y.cvs.connection.CVSROOT", "");
    }
}
