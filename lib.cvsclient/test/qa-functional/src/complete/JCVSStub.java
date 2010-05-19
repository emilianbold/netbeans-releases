
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

package complete;

import complete.GenericStub.GenericNode;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import junit.framework.AssertionFailedError;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewWizardOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.CustomizeAction;
import org.netbeans.jellytools.modules.javacvs.CVSClientCustomizerDialogOperator;
import org.netbeans.jellytools.modules.javacvs.actions.JCVSMountAction;
import org.netbeans.jellytools.modules.javacvs.wizard.*;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.properties.ComboBoxProperty;
import org.netbeans.jellytools.properties.PropertySheetOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.test.oo.gui.jelly.javacvs.AddDialog;
import org.netbeans.test.oo.gui.jelly.javacvs.CheckoutDialog;
import org.netbeans.test.oo.gui.jelly.javacvs.CommitDialog;
import org.netbeans.test.oo.gui.jelly.javacvs.TagDialog;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.Repository;
import util.History;

public abstract class JCVSStub extends GenericStub {
    
    public JCVSStub(String testName) {
        super(testName);
        nRootPrefix = "CVS ";
    }
    
    static FileSystem sfs = null;
    
    protected FileSystem getFileSystem () {
        return sfs;
    }
    
    protected void setFileSystem (FileSystem fs) {
        sfs = fs;
    }
  
    protected void prepareServer (String dir) {
    }
    
    protected static String roothistoryshort = null;
    
    protected void setUp() throws Exception {
        super.setUp ();
        if (!"configure".equals (getName ())) {
            history.switchToJCVS();
            history.setTimeout(30);
            root.setHistoryShort(roothistoryshort);
        }
    }
    
    protected void findFS () {
        if (!GenericStub.DEBUG) {
            super.findFS ();
            return;
        }
        String normalizedClient = (nRootPrefix + clientDirectory.replace ('\\', '/')).toLowerCase ();
        Enumeration e = Repository.getDefault().getFileSystems();
        while (e.hasMoreElements()) {
            FileSystem f = (FileSystem) e.nextElement();
            info.println("Is it?: " + f.getDisplayName());
            String path = f.getDisplayName ().replace ('\\', '/').toLowerCase();
            if (path.indexOf (normalizedClient) >= 0) {
                info.println("Yes");
                setFileSystem(f);
                roothistoryshort = f.getDisplayName ().substring(f.getDisplayName ().lastIndexOf ('/') + 1);
                return;
            }
        }
        new AssertionFailedError ("Filesystem not found: Filesystem: " + normalizedClient);
    }
    
    protected void mountVCSFileSystem () {
        new Action ("Versioning|Mount Version Control", null).performMenu (); // workaround for issue #31026
        new Action ("Tools", null).performMenu (); // workaround for issue #31026
        sleep (10000);
        new Action ("Versioning|Mount Version Control", null).performMenu (); // workaround for issue #31026
        new Action ("Tools", null).performMenu (); // workaround for issue #31026
        new JCVSMountAction().perform();
        
        WorkingDirectoryStepOperator wdso = new WorkingDirectoryStepOperator ();
        wdso.verify ();
        txtSetTypeText (wdso.cboWorkingDirectory().getTextField(), clientDirectory);
        wdso.next ();
        
        CVSConnectionMethodStepOperator ccmsto = new CVSConnectionMethodStepOperator ();
        ccmsto.verify ();
        ccmsto.passwordServer();
//        ccmsto.setServerName("qa-w2k-s1");
//        ccmsto.setUserName("test");
//        txtSetTypeText (ccmsto.txtRepository(), "d:\\apps\\vcs\\cvs\\repository");
//        ccmsto.txtCVSROOT().waitText(":pserver:test@qa-w2k-s1:d:\\apps\\vcs\\cvs\\repository");
        ccmsto.setServerName("mercury");
        ccmsto.setUserName("test");
        txtSetTypeText (ccmsto.txtRepository(), "/cvsroot");
        ccmsto.txtCVSROOT().waitText(":pserver:test@mercury:/cvsroot");
        ccmsto.next ();
        
        CVSClientStepOperator ccso = new CVSClientStepOperator ();
        ccso.verify ();
        ccso.useBuiltInCVSClient();
        ccso.next ();
        
        CVSLoginStepOperator clso = new CVSLoginStepOperator ();
        clso.verify ();
        clso.txtPassword().clearText ();
        clso.txtPassword().typeText ("test");
        clso.login();
        clso.txtLogin().waitText ("Login successful.");
        clso.next ();
        
        InitialCheckoutStepOperator icso = new InitialCheckoutStepOperator ();
        icso.checkCheckOutACopyOfTheRepositoryFilesToYourWorkingDirectory (false);
        icso.finish ();
    }
        
    protected void prepareClient () {
        history = new History (getFileSystem(), info);
        history.switchToJCVS();
        history.setTimeout(30);
        root.setHistoryShort ("client");

        closeAllVCSWindows();
        closeAllProperties();

        root.jcvsNode().properties();
        PropertySheetOperator pso = new PropertySheetOperator (root.node ());
        ComboBoxProperty cbp = new ComboBoxProperty (pso, "User Interface Mode");
        cbp.setValue("GUI Style");
        sleep (1000);
        info.println ("User Interface Mode property value 1: " + cbp.getValue ());
        closeAllProperties();

        // stabilization
        root.jcvsNode().properties();
        pso = new PropertySheetOperator (root.node ());
        cbp = new ComboBoxProperty (pso, "User Interface Mode");
        info.println ("User Interface Mode property value 2: " + cbp.getValue ());
        cbp.setValue("GUI Style");
        sleep (1000);
        info.println ("User Interface Mode property value 3: " + cbp.getValue ());
        closeAllProperties();

        history.setTimeout(120);
        checkout(root, "test/jcvs");
        history.setTimeout(30);
        GenericNode test = new GenericNode (root, "test");
        GenericNode jcvs = new GenericNode (test, "jcvs");
        jcvs.waitStatus (null);
        jcvs.jcvsNode().select ();
        
        roothistoryshort = new SimpleDateFormat ("yyMMddHHmmssSS").format (new Date (System.currentTimeMillis()));
        GenericNode cur = new GenericNode (jcvs, roothistoryshort);
        cur.mkdirs();
        refresh (jcvs);
        cur.waitStatus("Local");
        addDirectory (cur);
        
        new CustomizeAction ().performPopup(root.jcvsNode());
        CVSClientCustomizerDialogOperator cus = new CVSClientCustomizerDialogOperator ();
        new Node (cus.treeSelectRelativeMountPoint(), "test|jcvs|" + roothistoryshort).select ();
        cus.txtRelativeMountPoint().waitText ("test" + File.separator + "jcvs" + File.separator + roothistoryshort);
        cus.ok ();
        cus.waitClosed();
        closeAllVCSWindows();
        for (int a = 0; a < 30; a ++) {
            if (getFileSystem().getDisplayName().endsWith(roothistoryshort)) {
                root = new GenericNode (null, getFileSystem ().getDisplayName ().substring(nRootPrefix.length()));
                root.setHistoryShort(roothistoryshort);
                root.jcvsNode ().select ();
                return;
            }
            sleep (1000);
        }
        throw new AssertionFailedError ("New root not found: Root: " + roothistoryshort);
    }
    
    protected void refresh (GenericNode node) {
        node.jcvsNode ().expand (); // workaround for issue #29598
        node.jcvsNode().jCVSRefresh();
        node.waitHistoryShort ("status -l");
    }

    protected void refreshRecursively (GenericNode node) {
        node.jcvsNode ().expand (); // workaround for issue #29598
        node.jcvsNode ().jCVSRefreshRecursively();
        node.waitHistoryShort ("status");
    }

    protected void addDirectory (GenericNode node) {
        node.jcvsNode ().expand (); // workaround for issue #29598
        node.jcvsNode ().jCVSAdd();
        AddDialog add = new AddDialog ();
        add.runCommand ();
        add.waitClosed();
        node.waitHistoryShort ("add");
        node.waitStatus (null);
    }
    
    protected void checkout (GenericNode node, String what) {
        node.jcvsNode ().expand (); // workaround for issue #29598
        node.jcvsNode ().jCVSCheckOut();
        CheckoutDialog cod = new CheckoutDialog ();
        cod.txtCheckout().clearText();
        cod.txtCheckout().typeText(what);
        cod.runCommand();
        cod.waitClosed();
        node.waitHistoryShort ("checkout " + what);
    }

    protected void addFile (GenericNode node, String desc) {
        node.cvsNode ().cVSAdd();
        AddDialog add = new AddDialog ();
        if (desc != null) {
            add.txtMessage().setCaretPosition(0);
            add.txtMessage().typeText(desc);
        }
        add.runCommand ();
        add.waitClosed();
        if (desc != null) {
            if (desc.length () > 9)
                desc = desc.substring(0, 5) + "...";
            node.waitHistoryShort("add -m \"" + desc + "\"");
        } else
            node.waitHistoryShort("add");
        node.waitStatus ("LAdded; New");
    }

    protected void commitFile (GenericNode node, String branch, String desc) {
        node.cvsNode ().cVSCommit();
        CommitDialog com = new CommitDialog ();
        if (branch != null) {
            checkCheckBox (com.cbRevisionBranch());
            com.txtRevisionBranch().clearText();
            com.txtRevisionBranch().typeText(branch);
        }
        if (desc != null) {
            com.txtMessage().setCaretPosition(0);
            com.txtMessage().typeText(desc);
        }
        com.runCommand ();
        com.waitClosed();
        String cmd = "commit";
        if (branch != null)
            cmd += " -r " + branch;
        if (desc != null) {
            if (desc.length () > 9)
                desc = desc.substring (0, 5) + "...";
            cmd += " -m \"" + desc + "\"";
        } else
            cmd += " -m \"\"";
        node.waitHistoryShort (cmd);
        node.jcvsNode().jCVSRefresh(); // workaround for unreported issue - sometimes file annotation is not updated after commit
        node.waitHistoryShort ("status -l"); // workaround for unreported issue - sometimes file annotation is not updated after commit
        node.waitStatus ("Up-to-date", false);
    }
    
    public void createAddCommitFile (GenericNode node, String template) {
        NewWizardOperator.create(template, node.parent ().node (), node.name ());
        node.waitStatus ("Local");
        addFile (node, "Initial");
        commitFile (node, null, "Initial state");
        node.waitStatus ("Up-to-date; 1.1");
    }
    
    public void dumpTable (JTableOperator table) {
        out.println ("==== Table Dump ====");
        int rows = table.getRowCount();
        out.println ("Rows: " + rows);
        int columns = table.getColumnCount();
        out.println ("Columns: " + columns);
        for (int a = 0; a < rows; a ++) {
            out.print ("" + (a + 1) + ".");
            for (int b = 0; b < columns; b ++)
                out.print (" - " + table.getValueAt(a, b));
            out.println ();
        }
    }
    
    protected void addTagFile (GenericNode node, String name, boolean branch) {
        node.jcvsNode().jCVSTag();
        TagDialog tag = new TagDialog ();
        checkCheckBox (tag.cbBranch(), branch);
        tag.txtTag().clearText ();
        tag.txtTag().typeText (name);
        tag.runCommand ();
        tag.waitClosed ();
        if (branch)
            node.waitHistoryShort("tag -b " + name);
        else
            node.waitHistoryShort("tag " + name);
    }

    protected void checkCheckBox (JCheckBoxOperator cb) {
        checkCheckBox (cb, true);
    }
    
    protected void checkCheckBox (JCheckBoxOperator cb, boolean state) {
        if (cb.isSelected() != state)
            cb.push ();
    }

    protected void assertCVSErrorDialog () {
        NbDialogOperator dia = new NbDialogOperator ("CVS Error");
        dia.close();
        dia.waitClosed ();
    }
}
