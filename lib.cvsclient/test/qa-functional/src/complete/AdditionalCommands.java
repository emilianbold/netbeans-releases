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
import org.netbeans.test.oo.gui.jelly.javacvs.AnnotateDialog;
import org.netbeans.test.oo.gui.jelly.javacvs.CommitDialog;
import org.netbeans.test.oo.gui.jelly.javacvs.DiffDialog;
import org.netbeans.test.oo.gui.jelly.javacvs.LogDialog;
import org.netbeans.test.oo.gui.jelly.javacvs.RemoveDialog;
import org.netbeans.test.oo.gui.jelly.javacvs.StatusDialog;
import org.netbeans.test.oo.gui.jelly.vcscore.OutputOfVCSCommandsFrame;

public class AdditionalCommands extends JCVSStub {
    
    public AdditionalCommands(String testName) {
        super(testName);
    }
    
    public static junit.framework.Test suite() {
//        DEBUG = true;
        junit.framework.TestSuite suite = new org.netbeans.junit.NbTestSuite();
        suite.addTest(new AdditionalCommands("configure"));
        suite.addTest(new AdditionalCommands("testStatusRefresh"));
        suite.addTest(new AdditionalCommands("testRemove"));
//        suite.addTest(new AdditionalCommands("testRemoveRecursively")); // !!! do it
        suite.addTest(new AdditionalCommands("testLogInit"));
        suite.addTest(new AdditionalCommands("testLogFileFull"));
        suite.addTest(new AdditionalCommands("testLogFileStrict"));
        suite.addTest(new AdditionalCommands("testLogDirectory"));
        suite.addTest(new AdditionalCommands("testLogDirectoryRecursive"));
        suite.addTest(new AdditionalCommands("testLogRestrictions"));
        suite.addTest(new AdditionalCommands("testStatusInit"));
        suite.addTest(new AdditionalCommands("testStatusFile"));
        suite.addTest(new AdditionalCommands("testStatusFileIncludeTags"));
        suite.addTest(new AdditionalCommands("testStatusDirectory"));
        suite.addTest(new AdditionalCommands("testStatusDirectoryRecursive"));
        suite.addTest(new AdditionalCommands("testAnnotateInit"));
        suite.addTest(new AdditionalCommands("testAnnotateFileFull"));
        suite.addTest(new AdditionalCommands("testAnnotateFileStrict"));
        suite.addTest(new AdditionalCommands("testAnnotateDirectory"));
        suite.addTest(new AdditionalCommands("testAnnotateDirectoryRecursive"));
        suite.addTest(new AdditionalCommands("testAnnotateInvalid"));
        suite.addTest(new AdditionalCommands("testAnnotateForce"));
        suite.addTest(new AdditionalCommands("testDiffInit"));
        suite.addTest(new AdditionalCommands("testDiffFile1Revision"));
        suite.addTest(new AdditionalCommands("testDiffFile2Revisions"));
        suite.addTest(new AdditionalCommands("testDiffFileIgnore"));
        suite.addTest(new AdditionalCommands("testDiffFileFormat"));
//        suite.addTest(new AdditionalCommands("testDiffDirectory")); // graphical diff not work - used textual diff - but textual is broken too - always fail due to bug #29769
        suite.addTest(new AdditionalCommands("testUnmount"));
        return suite;
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    GenericNode pac1, subpac1;
    GenericNode aa1;
    GenericNode log1, log2;

    GenericNode pac2, subpac2;
    GenericNode status1, status2;
    
    GenericNode pac3, subpac3;
    GenericNode anno1, anno2;
    
    GenericNode pac4, subpac4;
    GenericNode diff1, diff2;
    
    protected void createStructure() {
        pac1 = new GenericNode (root, "pac1");
        subpac1 = new GenericNode (pac1, "subpac1");
        
        aa1 = new GenericNode (subpac1, "Aa1", ".java");

        log1 = new GenericNode (subpac1, "log1", ".java");
        log2 = new GenericNode (subpac1, "log2", ".java");
        
        pac2 = new GenericNode (root, "pac2");
        subpac2 = new GenericNode (pac2, "subpac2");

        status1 = new GenericNode (subpac2, "status1", ".java");
        status2 = new GenericNode (subpac2, "status2", ".java");
        
        pac3 = new GenericNode (root, "pac3");
        subpac3 = new GenericNode (pac3, "subpac3");

        anno1 = new GenericNode (pac3, "anno1");
        anno2 = new GenericNode (subpac3, "anno2");
        
        pac4 = new GenericNode (root, "pac4");
        subpac4 = new GenericNode (pac4, "subpac4");

        diff1 = new GenericNode (subpac4, "diff1");
        diff2 = new GenericNode (subpac4, "diff2");
    }
    
    public void configure () {
        super.configure ();
        if (!DEBUG) {
            createStructure ();

            subpac1.mkdirs ();
            refresh (root);
            pac1.waitStatus ("Local");
            addDirectory(pac1);
            refresh (pac1);
            subpac1.waitStatus ("Local");
            addDirectory(subpac1);

            subpac2.mkdirs ();
            refresh (root);
            pac2.waitStatus ("Local");
            addDirectory(pac2);
            refresh (pac2);
            subpac2.waitStatus ("Local");
            addDirectory(subpac2);

            createAddCommitFile (aa1, "Java Classes|Main");
        }
    }
    
    public void testStatusRefresh () {
        aa1.waitStatus ("Up-to-date; 1.1");
        String file = loadFile(aa1.file ());
        try {
            new OpenAction ().perform(aa1.jcvsNode ());
            EditorOperator eo = new EditorOperator (aa1.name () + " [Up-to-date; 1.1]");

            eo.txtEditorPane().typeText("// inserted line\n", 0);
            eo.waitModified(true);
            eo.save ();
            eo.waitModified (false);
            aa1.waitStatus ("LMod; 1.1");
            eo.deleteLine(1);
            eo.waitModified (true);
            eo.save ();
            eo.waitModified (false);
//            eo.close(); // workaround for unreported bug

            refresh (pac1);
            aa1.waitStatus ("LMod; 1.1");
            refresh (subpac1);
            aa1.waitStatus ("Up-to-date; 1.1");
        } finally {
            saveToFile(getWorkFilePath() + "/modfile1", loadFile (aa1.file ()));
            aa1.save (file);
        }
        
        try {
            aa1.save ("Modify");
            refresh (aa1);
            aa1.waitStatus ("LMod; 1.1");
        } finally {
            saveToFile(getWorkFilePath() + "/modfile2", loadFile (aa1.file ()));
            aa1.save (file);
        }
        
        refresh (pac1);
        aa1.waitStatus ("LMod; 1.1");
        refresh (subpac1);
        aa1.waitStatus ("Up-to-date; 1.1");
        
        try {
            aa1.save ("Modify");
            refresh (aa1);
            aa1.waitStatus ("LMod; 1.1");
        } finally {
            saveToFile(getWorkFilePath() + "/modfile3", loadFile (aa1.file ()));
            aa1.save (file);
        }
        
        refreshRecursively(pac1);
        aa1.waitStatus ("Up-to-date; 1.1");
    }
    
    public void testRemove () {
        GenericNode remfile = new GenericNode (subpac1, "remfile", ".java");
        createAddCommitFile (remfile, "Java Classes|Main");

        closeAllVCSWindows();
        remfile.jcvsNode ().jCVSRemove ();
        RemoveDialog rem = new RemoveDialog ();
        rem.runCommand ();
        rem.waitClosed ();
        
        OutputOfVCSCommandsFrame output = new OutputOfVCSCommandsFrame ();
        dumpTable (output.tabJTable());
        remfile.waitStatus ("Up-to-date; 1.1");
        
        closeAllVCSWindows();
        remfile.jcvsNode ().jCVSRemove ();
        rem = new RemoveDialog ();
        checkCheckBox (rem.cbDeleteBeforeRemove());
        rem.runCommand ();
        rem.waitClosed ();
        
        output = new OutputOfVCSCommandsFrame ();
        dumpTable (output.tabJTable());
        remfile.waitStatus ("LRemoved", false);

        remfile.jcvsNode().jCVSCommit ();
        CommitDialog com = new CommitDialog ();
        com.runCommand();
        com.waitClosed ();
        remfile.waitHistoryShort ("commit -m \"\"");
        remfile.parent ().jcvsNode ().waitChildNotPresent(remfile.name ());
    }
    
    public void testRemoveRecursively () {
        // !!! do it
    }
    
    public void testLogInit () {
        createAddCommitFile(log1, "Java Classes|Empty");
        log1.waitStatus ("Up-to-date; 1.1");
        createAddCommitFile(log2, "Java Classes|Empty");
        log2.waitStatus ("Up-to-date; 1.1");
        
        log1.save ("modify - log 1 - 1.2");
        refresh (log1);
        log1.waitStatus ("LMod; 1.1");
        commitFile(log1, null, "Desc - log 1 - 1.2");
        log1.waitStatus ("Up-to-date; 1.2");
        
        log2.save ("modify - log 2 - 1.2");
        refresh (log2);
        log2.waitStatus ("LMod; 1.1");
        commitFile(log2, null, "Desc - log 2 - 1.2");
        log2.waitStatus ("Up-to-date; 1.2");
        
        addTagFile (log1, "branchtag", true);
        log1.save ("modify - log 1 - branch - 1.2.1.1");
        refresh (log1);
        log1.waitStatus ("LMod; 1.2");
        commitFile (log1, "branchtag", "Desc - log 1 - branchtag - 1.2.2.1");
        log1.waitStatus ("Up-to-date; 1.2.2.1");
        log1.waitVersion("branchtag");
    }
    
    public void testLogFileFull () {
        closeAllVCSWindows();
        log1.jcvsNode ().jCVSLog ();
        LogDialog dia = new LogDialog ();
        dia.runCommand();
        dia.waitClosed();
        log1.waitHistoryShort ("log");
        
        LogOutputOperator log = new LogOutputOperator (false, false, false);
        log.dumpFile(out, "/cvsroot/test/jcvs/" + roothistoryshort);
        
        compareReferenceFiles();
    }
    
    public void testLogFileStrict () {
        closeAllVCSWindows();
        log1.jcvsNode ().jCVSLog ();
        LogDialog dia = new LogDialog ();
        checkCheckBox (dia.cbNotRecursively());
        checkCheckBox (dia.cbDefaultBranchOnly());
        checkCheckBox (dia.cbNoTags());
        checkCheckBox (dia.cbHeaderAndDescriptionOnly());
        checkCheckBox (dia.cbHeaderOnly());
        dia.runCommand();
        dia.waitClosed();
        log1.waitHistoryShort ("log -b -t -h -N -l");
        
        LogOutputOperator log = new LogOutputOperator (false, true, true);
        log.dumpFile(out, "/cvsroot/test/jcvs/" + roothistoryshort);
        
        compareReferenceFiles();
    }
    
    public void testLogDirectory () {
        closeAllVCSWindows();
        pac1.jcvsNode ().jCVSLog ();
        LogDialog dia = new LogDialog ();
        checkCheckBox (dia.cbNotRecursively());
        dia.runCommand();
        dia.waitClosed();
        pac1.waitHistoryShort ("log -l");
        
        LogOutputOperator log = new LogOutputOperator (true, false, false);
        log.txtContains().clearText ();
        log.txtContains().enterText("Desc - log ");
        log.dumpAll(out, "/cvsroot/test/jcvs/" + roothistoryshort);
        
        compareReferenceFiles();
    }
    
    public void testLogDirectoryRecursive () {
        closeAllVCSWindows();
        pac1.jcvsNode ().jCVSLog ();
        LogDialog dia = new LogDialog ();
        dia.runCommand();
        dia.waitClosed();
        pac1.waitHistoryShort ("log");
        
        LogOutputOperator log = new LogOutputOperator (true, false, false);
        log.txtContains().clearText ();
        log.txtContains().enterText("Desc - log ");
        log.dumpAll(out, "/cvsroot/test/jcvs/" + roothistoryshort);
        
        compareReferenceFiles();
    }
    
    public void testLogRestrictions () {
        pac1.jcvsNode ().jCVSLog ();
        LogDialog dia = new LogDialog ();
        checkCheckBox (dia.cbNotRecursively());
        checkCheckBox (dia.cbDefaultBranchOnly());
        checkCheckBox (dia.cbNoTags());
        checkCheckBox (dia.cbHeaderAndDescriptionOnly());
        checkCheckBox (dia.cbHeaderOnly());
        checkCheckBox (dia.cbState());
        dia.txtState().clearText ();
        dia.txtState().typeText ("STATE");
        checkCheckBox (dia.cbRevision());
        dia.txtRevision().clearText ();
        dia.txtRevision().typeText ("REVISION");
        checkCheckBox (dia.cbDate());
        dia.txtDate().clearText ();
        dia.txtDate().typeText ("DATE");
        checkCheckBox (dia.cbUser());
        dia.txtUser().clearText ();
        dia.txtUser().typeText ("USER");
        
        JTextFieldOperator cvsSwitches = new JTextFieldOperator (dia, "log ");
        assertEquals ("CVS Command String", "log -b -t -h -N -l -wUSER -rREVISION -sSTATE -dDATE ", cvsSwitches.getText ());
        dia.cancel();
        dia.waitClosed();
    }
    
    public void testStatusInit () {
        createAddCommitFile(status1, "Java Classes|Empty");
        status1.waitStatus ("Up-to-date; 1.1");
        createAddCommitFile(status2, "Java Classes|Empty");
        status2.waitStatus ("Up-to-date; 1.1");
        
        status1.save ("modify - status 1 - 1.2");
        refresh (status1);
        status1.waitStatus ("LMod; 1.1");
        commitFile(status1, null, "Desc - status 1 - 1.2");
        status1.waitStatus ("Up-to-date; 1.2");
        
        status2.save ("modify - status 2 - 1.2");
        refresh (status2);
        status2.waitStatus ("LMod; 1.1");
        commitFile(status2, null, "Desc - status 2 - 1.2");
        status2.waitStatus ("Up-to-date; 1.2");
        
        addTagFile (status1, "branchtag", true);
        status1.save ("modify - status 1 - branch - 1.2.1.1");
        refresh (status1);
        status1.waitStatus ("LMod; 1.2");
        commitFile (status1, "branchtag", "Desc - status 1 - branchtag - 1.2.2.1");
        status1.waitStatus ("Up-to-date; 1.2.2.1");
        status1.waitVersion("branchtag");
    }
    
    public void testStatusFile () {
        closeAllVCSWindows();
        status1.jcvsNode ().jCVSStatus ();
        StatusDialog dia = new StatusDialog ();
        dia.runCommand();
        dia.waitClosed();
        status1.waitHistoryShort ("status");
        
        StatusOutputOperator status = new StatusOutputOperator (false);
        status.dumpFile(out, "/cvsroot/test/jcvs/" + roothistoryshort);
        status.btGetTags().push ();
        waitNoEmpty (status.tabExistingTags());
        status.dumpExistingTags(out);
        
        compareReferenceFiles();
    }
    
    public void testStatusFileIncludeTags () {
        closeAllVCSWindows();
        status1.jcvsNode ().jCVSStatus ();
        StatusDialog dia = new StatusDialog ();
        checkCheckBox (dia.cbIncludeTagInfo());
        dia.runCommand();
        dia.waitClosed();
        status1.waitHistoryShort ("status -v");
        
        StatusOutputOperator status = new StatusOutputOperator (false);
        status.dumpFile(out, "/cvsroot/test/jcvs/" + roothistoryshort);
        
        compareReferenceFiles();
    }
    
    public void testStatusDirectory () {
        closeAllVCSWindows();
        pac2.jcvsNode ().jCVSStatus ();
        StatusDialog dia = new StatusDialog ();
        checkCheckBox (dia.cbIncludeTagInfo());
        checkCheckBox (dia.cbNotRecursively());
        dia.runCommand();
        dia.waitClosed();
        pac2.waitHistoryShort ("status -v -l");
        
        StatusOutputOperator status = new StatusOutputOperator (true);
        status.dumpAll(out, "/cvsroot/test/jcvs/" + roothistoryshort);
        
        compareReferenceFiles();
    }
    
    public void testStatusDirectoryRecursive () {
        closeAllVCSWindows();
        pac2.jcvsNode ().jCVSStatus ();
        StatusDialog dia = new StatusDialog ();
        checkCheckBox (dia.cbIncludeTagInfo());
        dia.runCommand();
        dia.waitClosed();
        pac2.waitHistoryShort ("status -v");
        
        StatusOutputOperator status = new StatusOutputOperator (true);
        status.dumpAll(out, "/cvsroot/test/jcvs/" + roothistoryshort);
        
        compareReferenceFiles();
    }
    
    public void testAnnotateInit () {
        subpac3.mkdirs ();
        refresh (root);
        pac3.waitStatus ("Local");
        addDirectory(pac3);
        refresh (pac3);
        subpac3.waitStatus ("Local");
        addDirectory(subpac3);

        anno1.save ("Commit-1.1");
        refresh (anno1.parent ());
        anno1.jcvsNode();
        addFile(anno1, "Add-Desc-1.1");
        commitFile(anno1, null, "Commit-Desc-1.1");
        anno1.waitStatus ("Up-to-date; 1.1");
        addTagFile(anno1, "TagName", true);
        anno1.save ("Commit-TagName");
        refresh (anno1.parent ());
        anno1.waitStatus ("LMod; 1.1");
        commitFile(anno1, "TagName", "Commit-Desc-TagName");
        anno1.waitStatus ("Up-to-date; 1.1.2.1");
        anno1.waitVersion ("TagName");

        anno2.save ("Commit-1.1 - Line1\nCommit-1.1 - Line2\nCommit-1.1 - Line4\nCommit-1.1 - Line5\n");
        refresh(anno2.parent ());
        anno2.jcvsNode();
        addFile (anno2, "InitialState");
        commitFile (anno2, null, "Commit_1.1");
        anno2.waitStatus ("Up-to-date; 1.1");
        anno2.save ("Commit-1.1 - Line1\nCommit-1.1 - Line2 - Modified-1.2\nCommit-1.2 - Line3 - Added-1.2\nCommit-1.1 - Line4\nCommit-1.1 - Line5 - Modified-1.2\n");
        commitFile (anno2, null, "Commit_1.2");
        anno2.waitStatus ("Up-to-date; 1.2");
        anno2.save ("Commit-1.3 - Line0 - Added-1.3\nCommit-1.1 - Line1\nCommit-1.1 - Line2 - Modified-1.2 - Modified-1.3\nCommit-1.2 - Line3 - Added-1.2\nCommit-1.1 - Line4\nCommit-1.1 - Line5 - Modified-1.2\n");
        commitFile (anno2, null, "Commit_1.3");
        anno2.waitStatus ("Up-to-date; 1.3");
    }
    
    public void testAnnotateFileFull () {
        closeAllVCSWindows ();
        anno2.jcvsNode().jCVSAnnotate();
        AnnotateDialog dia = new AnnotateDialog ();
        dia.runCommand();
        dia.waitClosed();
        anno2.waitHistoryShort ("annotate");
        
        AnnotateOutputOperator aoo = new AnnotateOutputOperator (false);
        aoo.dumpFile (out, roothistoryshort);
        
        compareReferenceFiles();
    }
    
    public void testAnnotateFileStrict () {
        AnnotateDialog dia;
        AnnotateOutputOperator aoo;

        closeAllVCSWindows ();
        anno1.jcvsNode().jCVSAnnotate();
        dia = new AnnotateDialog ();
        checkCheckBox(dia.cbDate());
        checkCheckBox(dia.cbRevisionTag());
        dia.txtRevisionTag().typeText ("TagName");
        checkCheckBox(dia.cbForceHead());
        dia.runCommand();
        dia.waitClosed();
        dia = null;
        anno1.waitHistoryShort ("annotate -r TagName -D  -f");
        
        aoo = new AnnotateOutputOperator (false);
        aoo.dumpFile (out, roothistoryshort);
        aoo = null;
        
        compareReferenceFiles();
    }
    
    public void testAnnotateDirectory () {
        closeAllVCSWindows ();
        pac3.jcvsNode().jCVSAnnotate();
        AnnotateDialog dia = new AnnotateDialog ();
        checkCheckBox(dia.cbNotRecursively());
        dia.runCommand();
        dia.waitClosed();
        pac3.waitHistoryShort ("annotate -l");
        
        AnnotateOutputOperator aoo = new AnnotateOutputOperator (true);
        aoo.dumpAll (out, roothistoryshort);
        
        compareReferenceFiles();
    }
    
    public void testAnnotateDirectoryRecursive () {
        closeAllVCSWindows ();
        pac3.jcvsNode().jCVSAnnotate();
        AnnotateDialog dia = new AnnotateDialog ();
        dia.runCommand();
        dia.waitClosed();
        pac3.waitHistoryShort ("annotate");
        
        AnnotateOutputOperator aoo = new AnnotateOutputOperator (true);
        aoo.dumpAll (out, roothistoryshort);
        
        compareReferenceFiles();
    }
    
    public void testAnnotateInvalid () {
        closeAllVCSWindows ();
        anno1.jcvsNode().jCVSAnnotate();
        AnnotateDialog dia = new AnnotateDialog ();
        checkCheckBox(dia.cbDate());
        checkCheckBox(dia.cbRevisionTag());
        dia.txtRevisionTag().typeText ("InvalidTagName");
        checkCheckBox(dia.cbForceHead());
        dia.runCommand();
        dia.waitClosed();
        
        NbDialogOperator d = new NbDialogOperator ("CVS Error");
        d.close();
        d.waitClosed ();
    }
    
    public void testAnnotateForce () {
        AnnotateDialog dia;
        AnnotateOutputOperator aoo;

        addTagFile (pac3, "NewTag", true);
        GenericNode anno3 = new GenericNode (pac3, "anno3", ".java");
        createAddCommitFile(anno3, "Java Classes|Empty");
        
        out.println ("+=== No Force ===+");
        closeAllVCSWindows ();
        pac3.jcvsNode().jCVSAnnotate();
        dia = new AnnotateDialog ();
        checkCheckBox(dia.cbRevisionTag());
        dia.txtRevisionTag().typeText ("NewTag");
        checkCheckBox(dia.cbForceHead());
        dia.runCommand();
        dia.waitClosed();
        pac3.waitHistoryShort ("annotate -r NewTag -f");
        
        aoo = new AnnotateOutputOperator (true);
        aoo.dumpAll (out, roothistoryshort);
        
        out.println ("+=== Force HEAD ===+");
        closeAllVCSWindows ();
        pac3.jcvsNode().jCVSAnnotate();
        dia = new AnnotateDialog ();
        checkCheckBox(dia.cbRevisionTag());
        dia.txtRevisionTag().typeText ("NewTag");
        dia.runCommand();
        dia.waitClosed();
        pac3.waitHistoryShort ("annotate -r NewTag");
        
        aoo = new AnnotateOutputOperator (true);
        aoo.dumpAll (out, roothistoryshort);
        
        compareReferenceFiles();
    }
    
    public void testDiffInit () {
        subpac4.mkdirs ();
        refresh (root);
        pac4.waitStatus ("Local");
        addDirectory(pac4);
        refresh (pac4);
        subpac4.waitStatus ("Local");
        addDirectory(subpac4);

        diff1.save ("Commit-1.1\nWhitespace Check\nSpace Changes Check\nCase-sensitive Check\nNewLines Check\nEnd of NewLines Check\n");
        refresh (diff1.parent ());
        diff1.jcvsNode();
        addFile(diff1, "Add-Desc-1.1");
        commitFile(diff1, null, "Commit-Desc-1.1");
        diff1.waitStatus ("Up-to-date; 1.1");
        diff1.save ("Commit-1.1\n    Whitespace    Check    \nSpace   Changes   Check\ncASE-SENsitive Check\nNewLines Check\n\n\n\nEnd of NewLines Check\n");
        refresh (diff1.parent ());
        diff1.waitStatus ("LMod; 1.1");

        diff2.save ("Commit-1.1 - Line1\nCommit-1.1 - Line2\nCommit-1.1 - Line4\nCommit-1.1 - Line5\n");
        refresh(diff2.parent ());
        diff2.jcvsNode();
        addFile (diff2, "InitialState");
        commitFile (diff2, null, "Commit_1.1");
        diff2.waitStatus ("Up-to-date; 1.1");
        diff2.save ("Commit-1.1 - Line1\nCommit-1.1 - Line2 - Modified-1.2\nCommit-1.2 - Line3 - Added-1.2\nCommit-1.1 - Line4\nCommit-1.1 - Line5 - Modified-1.2\n");
        commitFile (diff2, null, "Commit_1.2");
        diff2.waitStatus ("Up-to-date; 1.2");
        diff2.save ("Commit-1.3 - Line0 - Added-1.3\nCommit-1.1 - Line1\nCommit-1.1 - Line2 - Modified-1.2 - Modified-1.3\nCommit-1.2 - Line3 - Added-1.2\nCommit-1.1 - Line4\nCommit-1.1 - Line5 - Modified-1.2\n");
        commitFile (diff2, null, "Commit_1.3");
        diff2.waitStatus ("Up-to-date; 1.3");
    }

    public void dumpDiffGraphical (GenericNode node) {
        TopComponentOperator tco = new TopComponentOperator ("Diff " + node.filename(0));
        out.println ("==== Diff: " + node.filename(0));
        try {
            dumpDiffGraphical (tco);
        } finally {
            tco.close();
            waitIsShowing(tco.getSource());
        }
    }
    
    public void dumpDiffTextual (GenericNode node, String command) {
        VCSCommandsOutputOperator coo = new VCSCommandsOutputOperator ("Diff");
        out.println ("==== Diff: " + node.filename(0));
        waitNoEmpty(coo.txtStandardOutput());
        String txt = coo.txtStandardOutput ().getText ();
        info.println ("==== Diff: " + node.filename(0));
        info.println (txt);
        StringFilter sf = new StringFilter ();
        sf.addReplaceAllFilter("/cvsroot/test/jcvs/" + roothistoryshort, "<REPOSITORY_PREFIX>");
        sf.addReplaceFilter("*** " + node.history (), "", "*** " + node.history () + "<FILTERED>");
        sf.addReplaceFilter("--- " + node.history (), "", "--- " + node.history () + "<FILTERED>");
        sf.addReplaceFilter("+++ " + node.history (), "", "+++ " + node.history () + "<FILTERED>");
        printFiltered(txt, sf);
    }
    
    public void testDiffFile1Revision () {
        DiffDialog dia;
        
        diff2.jcvsNode ().jCVSDiff();
        dia = new DiffDialog ();
        dia.txtRevision1().typeText ("1.1");
        dia.runCommand(); dia.waitClosed (); dia = null;
        diff2.waitHistoryShort ("diff -r 1.1");
        out.println ("==== 1 revision ====");
        dumpDiffGraphical (diff2);
        
        compareReferenceFiles();
    }
    
    public void testDiffFile2Revisions () {
        DiffDialog dia;
        
        diff2.jcvsNode ().jCVSDiff();
        dia = new DiffDialog ();
        dia.txtRevision1().typeText ("1.1");
        dia.txtRevision2().typeText ("1.2");
        dia.runCommand(); dia.waitClosed (); dia = null;
        diff2.waitHistoryShort ("diff -r 1.1 -r 1.2");
        out.println ("==== 2 revisions ====");
        dumpDiffGraphical (diff2);
        
        compareReferenceFiles();
    }
    
    public void testDiffFileIgnore () {
        DiffDialog dia;
        
        diff1.jcvsNode ().jCVSDiff();
        dia = new DiffDialog ();
        checkCheckBox (dia.cbIgnoreAllWhitespace());
        dia.runCommand(); dia.waitClosed (); dia = null;
        diff1.waitHistoryShort ("diff -w");
        out.println ("==== cbIgnoneAllWhitespace ====");
        dumpDiffGraphical (diff1);
        
        diff1.jcvsNode ().jCVSDiff();
        dia = new DiffDialog ();
        checkCheckBox (dia.cbIgnoreBlankLines());
        dia.runCommand(); dia.waitClosed (); dia = null;
        diff1.waitHistoryShort ("diff -B");
        out.println ("==== cbIgnoreBlankLines ====");
        dumpDiffGraphical (diff1);
        
        diff1.jcvsNode ().jCVSDiff();
        dia = new DiffDialog ();
        checkCheckBox (dia.cbIgnoreCase());
        dia.runCommand(); dia.waitClosed (); dia = null;
        diff1.waitHistoryShort ("diff -i");
        out.println ("==== cbIgnoreCase ====");
        dumpDiffGraphical (diff1);
        
        diff1.jcvsNode ().jCVSDiff();
        dia = new DiffDialog ();
        checkCheckBox (dia.cbIgnoreSpaceChanges());
        dia.runCommand(); dia.waitClosed (); dia = null;
        diff1.waitHistoryShort ("diff -b");
        out.println ("==== cbIgnoreSpaceChanges ====");
        dumpDiffGraphical (diff1);
        
        diff1.jcvsNode ().jCVSDiff();
        dia = new DiffDialog ();
        checkCheckBox (dia.cbIgnoreAllWhitespace());
        checkCheckBox (dia.cbIgnoreBlankLines());
        checkCheckBox (dia.cbIgnoreCase());
        checkCheckBox (dia.cbIgnoreSpaceChanges());
        dia.runCommand(); dia.waitClosed (); dia = null;
        out.println ("==== Ignore All ====");
        assertInformationDialog("Diff showed no difference on the following file:");
        diff1.waitHistoryShort ("diff -w -B -i -b");
        
        compareReferenceFiles();
    }
    
    public void testDiffFileFormat () {
        DiffDialog dia;
        
        closeAllVCSWindows();
        diff1.jcvsNode ().jCVSDiff();
        dia = new DiffDialog ();
        checkCheckBox (dia.cbContextOutput());
        dia.runCommand(); dia.waitClosed (); dia = null;
        out.println ("==== cbContextOutput ====");
        dumpDiffTextual (diff1, "diff -c");
        
        closeAllVCSWindows();
        diff1.jcvsNode ().jCVSDiff();
        dia = new DiffDialog ();
        checkCheckBox (dia.cbUnifiedOutput());
        dia.runCommand(); dia.waitClosed (); dia = null;
        out.println ("==== cbUnifiedOutput ====");
        dumpDiffTextual (diff1, "diff -u");
        
        compareReferenceFiles();
    }
    
    public void testDiffDirectory () {
        DiffDialog dia;
        
        closeAllVCSWindows();
        pac4.jcvsNode ().jCVSDiff();
        dia = new DiffDialog ();
        dia.runCommand(); dia.waitClosed (); dia = null;
        out.println ("==== Recursively ====");
        dumpDiffTextual (pac4, "diff");
        
        closeAllVCSWindows();
        pac4.jcvsNode ().jCVSDiff();
        dia = new DiffDialog ();
        checkCheckBox (dia.cbNotRecursively());
        dia.runCommand(); dia.waitClosed (); dia = null;
        out.println ("==== NOT Recursively ====");
        dumpDiffTextual (pac4, "diff -l");
        
        compareReferenceFiles();
    }
    
    public void testUnmount() {
        new FilesystemNode(exp.repositoryTab().tree(), root.node ()).unmount();
        new Node (exp.repositoryTab ().tree (), "").waitChildNotPresent(root.node ());
    }
    
}
