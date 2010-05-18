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

import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewWizardOperator;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.javacvs.output.AnnotateOutputOperator;
import org.netbeans.jellytools.modules.javacvs.output.LogOutputOperator;
import org.netbeans.jellytools.modules.javacvs.output.StatusOutputOperator;
import org.netbeans.jellytools.modules.vcscore.VCSCommandsOutputOperator;
import org.netbeans.jellytools.nodes.FilesystemNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.util.StringFilter;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.test.oo.gui.jelly.javacvs.AddDialog;
import org.netbeans.test.oo.gui.jelly.javacvs.AnnotateDialog;
import org.netbeans.test.oo.gui.jelly.javacvs.CommitDialog;
import org.netbeans.test.oo.gui.jelly.javacvs.DiffDialog;
import org.netbeans.test.oo.gui.jelly.javacvs.LogDialog;
import org.netbeans.test.oo.gui.jelly.javacvs.RemoveDialog;
import org.netbeans.test.oo.gui.jelly.javacvs.StatusDialog;
import org.netbeans.test.oo.gui.jelly.javacvs.TagDialog;
import org.netbeans.test.oo.gui.jelly.vcscore.OutputOfVCSCommandsFrame;

public class BasicCommands extends JCVSStub {
    
    public BasicCommands(String testName) {
        super(testName);
    }
    
    public static junit.framework.Test suite() {
//        DEBUG = true;
        junit.framework.TestSuite suite = new org.netbeans.junit.NbTestSuite();
        suite.addTest(new BasicCommands("configure"));
        suite.addTest(new BasicCommands("testAddDirectoryRecursive"));
        suite.addTest(new BasicCommands("testAddNoKeywordSubstitution"));
        suite.addTest(new BasicCommands("testAddKeywordSubstitution"));
        suite.addTest(new BasicCommands("testTagInit"));
        suite.addTest(new BasicCommands("testTagDirectory"));
        suite.addTest(new BasicCommands("testTagAdd"));
        suite.addTest(new BasicCommands("testTagModified"));
        suite.addTest(new BasicCommands("testTagByRevision"));
        suite.addTest(new BasicCommands("testTagForce"));
        suite.addTest(new BasicCommands("testTagDelete"));
        suite.addTest(new BasicCommands("testUnmount"));
        return suite;
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    GenericNode pac1, subpac1;
    GenericNode add1, add2, add3;
    
    GenericNode pac2, subpac2, subpac22;
    GenericNode tag1, tag2, tag3, tag4, tag51, tag52, tag6;
    
    protected void createStructure() {
        pac1 = new GenericNode (root, "pac1");
        subpac1 = new GenericNode (pac1, "subpac1");
        add1 = new GenericNode (subpac1, "add1");
        add2 = new GenericNode (pac1, "add2");
        add3 = new GenericNode (pac1, "add3");

        pac2 = new GenericNode (root, "pac2");
        subpac2 = new GenericNode (pac2, "subpac2");
        tag1 = new GenericNode (subpac2, "tag1", ".java");
        tag2 = new GenericNode (pac2, "tag2", ".java");
        tag3 = new GenericNode (pac2, "tag3", ".java");
        tag4 = new GenericNode (pac2, "tag4", ".java");

        subpac22 = new GenericNode (pac2, "subpa22");
        tag51 = new GenericNode (subpac22, "tag51");
        tag52 = new GenericNode (subpac22, "tag52");
        
        tag6 = new GenericNode (pac2, "tag6",".java");
    }
    
    public void configure () {
        super.configure ();
    }
    
    public void dumpStatus (GenericNode node, boolean directory) {
        closeAllVCSWindows ();
        node.jcvsNode ().jCVSStatus ();
        StatusDialog stat = new StatusDialog ();
        checkCheckBox (stat.cbIncludeTagInfo());
        stat.runCommand();
        stat.waitClosed ();
        node.waitHistoryShort ("status -v");
        StatusOutputOperator soo = new StatusOutputOperator (directory);
        if (directory)
            soo.dumpAll(out, "/cvsroot/test/jcvs/" + roothistoryshort);
        else
            soo.dumpFile(out, "/cvsroot/test/jcvs/" + roothistoryshort);
    }
    
    public void testAddDirectoryRecursive () {
        subpac1.mkdirs ();
        add1.save ("Adding 1");
        refresh(root);
        
        pac1.jcvsNode ().jCVSAdd ();
        AddDialog add = new AddDialog ();
        add.txtMessage().setText ("Adding Desc 1");
        checkCheckBox(add.cbRecursively());
        add.runCommand();
        add.waitClosed ();
        pac1.waitHistoryShort("add -m \"Addin...\"");
        
        add1.waitStatus ("LAdded; New", true);
        dumpStatus (add1, false);
        
        compareReferenceFiles ();
    }
    
    public void assertKeywordSubstitution (AddDialog add, String value, String switches) {
        switches = "add " + ((switches != null) ? (switches + " ") : "");
        add.cboSubstitution().selectItem (value);
        add.compareSwitches(switches);
        JTextFieldOperator tfo = new JTextFieldOperator (add, switches);
        assertEquals ("Keyword Substitution assertion failed", switches, tfo.getText ());
    }
    
    public void testAddNoKeywordSubstitution () {
        add2.save ("$Revision$\n");
        refresh (pac1);

        add2.jcvsNode ().jCVSAdd ();
        AddDialog add = new AddDialog ();
        assertKeywordSubstitution (add, "Reuse Old Value (-ko)", "-ko");
        add.txtMessage().setText ("Adding Desc 2");
        add.runCommand();
        add.waitClosed ();
        add2.waitHistoryShort("add -m \"Addin...\" -ko");
        add2.waitStatus ("LAdded; New", true);
        
        commitFile (add2, null, null);
        add2.waitStatus ("Up-to-date; 1.1", true);
        out.println ("Corrected file:");
        out.println (loadFile(add2.file ()));
        
        compareReferenceFiles ();
    }
    
    public void testAddKeywordSubstitution () {
        add3.save ("$Revision$\n");
        refresh (pac1);

        add3.jcvsNode ().jCVSAdd ();
        AddDialog add = new AddDialog ();
        assertKeywordSubstitution (add, "<NONE>", null);
        assertKeywordSubstitution (add, "Binary (-kb)", "-kb");
        assertKeywordSubstitution (add, "Keyword and Value (-kkv)", "-kkv");
        assertKeywordSubstitution (add, "Keyword, Value, Locker (-kkvl)", "-kkvl");
        assertKeywordSubstitution (add, "Keyword Only (-kk)", "-kk");
        assertKeywordSubstitution (add, "Value Only (-kv)", "-kv");
        assertKeywordSubstitution (add, "Reuse Old Value (-ko)", "-ko");

        assertKeywordSubstitution (add, "Keyword and Value (-kkv)", "-kkv");
        add.txtMessage().setText ("Adding Desc 3");
        add.runCommand();
        add.waitClosed ();
        add3.waitHistoryShort("add -m \"Addin...\" -kkv");
        add3.waitStatus ("LAdded; New", true);
        
        commitFile (add3, null, null);
        add3.waitStatus ("Up-to-date; 1.1", true);
        out.println ("Corrected file:");
        out.println (loadFile(add3.file ()));
        
        compareReferenceFiles ();
    }
    
    public void testTagInit () {
        subpac2.mkdirs ();
        refresh (root);
        addDirectory (pac2);
        refresh (pac2);
        addDirectory (subpac2);
        createAddCommitFile (tag1, "Java Classes|Empty");
        createAddCommitFile (tag2, "Java Classes|Empty");
    }
    
    public void testTagDirectory () {
        TagDialog tag;
        
        pac2.jcvsNode ().jCVSTag ();
        tag = new TagDialog ();
        tag.txtTag().typeText ("DirectoryRecursiveTag");
        checkCheckBox (tag.cbBranch());
        tag.runCommand();
        tag.waitClosed ();
        pac2.waitHistoryShort("tag -b DirectoryRecursiveTag");
        
        pac2.jcvsNode ().jCVSTag ();
        tag = new TagDialog ();
        tag.txtTag().typeText ("DirectoryTag");
        checkCheckBox (tag.cbNotRecursively());
        tag.runCommand();
        tag.waitClosed ();
        pac2.waitHistoryShort("tag -l DirectoryTag");
        
        dumpStatus (pac2, true);
        
        compareReferenceFiles ();
    }
    
    public void testTagAdd () {
        createAddCommitFile(tag3, "Java Classes|Empty");
        TagDialog tag;
        
        tag3.jcvsNode ().jCVSTag ();
        tag = new TagDialog ();
        tag.txtTag().typeText ("AddTagName");
        tag.runCommand ();
        tag.waitClosed ();
        tag3.waitHistoryShort ("tag AddTagName");
        
        tag3.jcvsNode ().jCVSTag ();
        tag = new TagDialog ();
        tag.txtTag().typeText ("AddBranchTagName");
        checkCheckBox (tag.cbBranch());
        tag.runCommand ();
        tag.waitClosed ();
        tag3.waitHistoryShort ("tag -b AddBranchTagName");
        
        dumpStatus (tag3, false);
        
        compareReferenceFiles();
    }
    
    public void testTagModified () {
        createAddCommitFile(tag4, "Java Classes|Empty");
        tag4.save ("");
        refresh (tag4.parent ());
        
        closeAllVCSWindows ();
        tag4.jcvsNode ().jCVSTag ();
        TagDialog tag = new TagDialog ();
        tag.txtTag().typeText ("ModifiedAddTagName");
        checkCheckBox (tag.cbCheckModifiedFiles());
        tag.runCommand ();
        tag.waitClosed ();
        assertCVSErrorDialog ();
    }
    
    public void testTagByRevision () {
        commitFile (tag4, null, "Desc 1.2");
        tag4.waitStatus ("Up-to-date; 1.2");
        
        closeAllVCSWindows ();
        tag4.jcvsNode ().jCVSTag ();
        TagDialog tag = new TagDialog ();
        tag.txtTag().typeText ("AddTagToRevisionName");
        checkCheckBox (tag.cbByRevision());
        tag.txtByRevision().typeText ("1.1");
        tag.runCommand ();
        tag.waitClosed ();

        dumpStatus (tag4, false);
        
        compareReferenceFiles();
    }
    
    public void testTagForce () {
        subpac22.mkdirs ();
        refresh (subpac22.parent ());
        addDirectory(subpac22);
        
        tag51.save ("Tag51");
        tag52.save ("Tag52");
        refresh (subpac22);
        addFile(tag51, null);
        commitFile(tag51, null, null);
        tag51.waitStatus ("Up-to-date; 1.1");
        addTagFile(tag51, "NoForceTag", false);
        tag51.save ("Tag51a");
        commitFile(tag51, null, null);
        tag51.waitStatus ("Up-to-date; 1.2");
        addFile(tag52, null);
        commitFile(tag52, null, null);
        tag52.waitStatus ("Up-to-date; 1.1");
        addTagFile(tag52, "ForceTag", false);
        tag52.save ("Tag52a");
        commitFile(tag52, null, null);
        tag52.waitStatus ("Up-to-date; 1.2");

        dumpStatus(subpac22, true);

        subpac22.jcvsNode ().jCVSTag();
        TagDialog tag = new TagDialog ();
        tag.txtTag().typeText ("ForceTag");
        checkCheckBox (tag.cbForceReassignement());
        tag.runCommand();
        tag.waitClosed ();
        subpac22.waitHistoryShort("tag -F ForceTag");

        dumpStatus(subpac22, true);

        compareReferenceFiles ();
    }
    
    public void testTagDelete () {
        createAddCommitFile(tag6, "Java Classes|Empty");
        addTagFile(tag6, "TagToDelete", false);

        dumpStatus (tag6, false);

        tag6.jcvsNode ().jCVSTag ();
        TagDialog tag = new TagDialog ();
        tag.txtTag().typeText ("TagToDelete");
        checkCheckBox(tag.cbDelete());
        tag.runCommand ();
        tag.waitClosed ();
        tag6.waitHistoryShort("tag -d TagToDelete");
        
        dumpStatus(tag6, false);

        compareReferenceFiles ();
    }
    
    public void testUnmount() {
        new FilesystemNode(exp.repositoryTab().tree(), root.node ()).unmount();
        new Node (exp.repositoryTab ().tree (), "").waitChildNotPresent(root.node ());
    }
    
}
