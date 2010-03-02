/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.test.php;

import org.netbeans.jemmy.JemmyException;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import java.awt.event.KeyEvent;
import javax.swing.JEditorPane;
import java.awt.Rectangle;
import javax.swing.text.BadLocationException;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.JTextComponentOperator;
import org.netbeans.jemmy.operators.Operator;
import java.io.File;
import org.netbeans.jemmy.Timeouts;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jellytools.modules.editor.CompletionJListOperator;
import java.util.List;

/**
 *
 * @author michaelnazarov@netbeans.org
 */
public class GeneralPHP extends JellyTestCase {

    // Okey, this is hack and should be removed later
    protected boolean bRandomCheck = false;
    static final String PHP_CATEGORY_NAME = "PHP";
    static final String PHP_PROJECT_NAME = "PHP Application";
    static final String SAMPLES = "Samples";
    static final String PROJECT_JOBEET = "Jobeet";
    static final String PROJECT_AirAliance = "Air Alliance Sample Application";
    static protected final int COMPLETION_LIST_THRESHOLD = 5000;
    protected static final String PHP_EXTENSION = ".php";

    public class CFulltextStringComparator implements Operator.StringComparator {

        public boolean equals(java.lang.String caption, java.lang.String match) {
            return caption.equals(match);
        }
    }

    public class CStartsStringComparator implements Operator.StringComparator {

        public boolean equals(java.lang.String caption, java.lang.String match) {
            return caption.startsWith(match);
        }
    }

    protected class CompletionInfo {

        public CompletionJListOperator listItself;
        public List listItems;

        public int size() {
            return listItems.size();
        }

        public void hideAll() {
            listItself.hideAll();
        }
    }

    public GeneralPHP(String arg0) {
        super(arg0);
    }

    public void Dummy() {
        startTest();
        System.out.println("=== DUMMY ===");
        endTest();
    }

    protected String GetWorkDir() {
        return getDataDir().getPath() + File.separator;
    }

    protected void Sleep(int iTime) {
        try {
            Thread.sleep(iTime);
        } catch (InterruptedException ex) {
            System.out.println("=== Interrupted sleep ===");
        }
    }

    protected String CreateSamplePHPApplication(String type) {
        NewProjectWizardOperator.invoke().cancel();

        NewProjectWizardOperator opNewProjectWizard = NewProjectWizardOperator.invoke();

        opNewProjectWizard.selectCategory(SAMPLES + "|" + PHP_CATEGORY_NAME);
//        opNewProjectWizard.selectCategory(PHP_CATEGORY_NAME);

        String typeName = "";

        if (type == "Jobeet")
            opNewProjectWizard.selectProject(PROJECT_JOBEET);
         else if (type == "AirAlliance")
             opNewProjectWizard.selectProject(PROJECT_AirAliance);

        opNewProjectWizard.next();

        JDialogOperator jdNew = new JDialogOperator("New PHP Sample Project");
        JTextComponentOperator jtName = new JTextComponentOperator(jdNew, 0);

        String sResult = jtName.getText();

        opNewProjectWizard.finish();
        return sResult; 

    }

    // All defaults including name
    protected String CreatePHPApplicationInternal(int iPort) {
        // Create PHP application

        // Workaround for MacOS platform
        // TODO : check platform
        // TODO : remove after normal issue fix
        NewProjectWizardOperator.invoke().cancel();

        NewProjectWizardOperator opNewProjectWizard = NewProjectWizardOperator.invoke();
        opNewProjectWizard.selectCategory(PHP_CATEGORY_NAME);
        opNewProjectWizard.selectProject(PHP_PROJECT_NAME);

        opNewProjectWizard.next();

        JDialogOperator jdNew = new JDialogOperator("New PHP Project");

        JTextComponentOperator jtName = new JTextComponentOperator(jdNew, 0);

        String sResult = jtName.getText();

        String sProjectPath = GetWorkDir() + File.separator + sResult;

        /*
        JComboBoxOperator jcPath = new JComboBoxOperator( jdNew, 0 );

        Timeouts t =  jcPath.getTimeouts( );
        long lBack = t.getTimeout( "JTextComponentOperator.TypeTextTimeout" );
        t.setTimeout( "JTextComponentOperator.TypeTextTimeout", 30000 );
        jcPath.setTimeouts( t );

        jcPath.enterText( sProjectPath );

        t.setTimeout( "JTextComponentOperator.TypeTextTimeout", lBack );
        jcPath.setTimeouts( t );
         */

        //NewProjectNameLocationStepOperator opNewProjectNameLocationStep = new NewProjectNameLocationStepOperator( );
        //opNewProjectNameLocationStep.txtProjectLocation( ).setText( GetWorkDir( ) );

        opNewProjectWizard.next();

        //opNewProjectNameLocationStep.txtProjectName( ).setText( sName );

        if (-1 != iPort) {
            //opNewProjectWizard.next( );

            // Set new port based URL here
            jdNew = new JDialogOperator("New PHP Project");
            JTextComponentOperator jtUrl = new JTextComponentOperator(jdNew, 0);
            String sUrl = jtUrl.getText();
            System.out.println("== Original: " + sUrl);
            sUrl = sUrl.replace("localhost", "localhost:" + iPort);
            System.out.println("== Fixed: " + sUrl);
            jtUrl.setText(sUrl);
        }

        opNewProjectWizard.finish();

        // Wait for warnings
        Sleep(5000);
        try {
            JDialogOperator jdWarning = new JDialogOperator("Warning");
            JButtonOperator jbCancel = new JButtonOperator(jdWarning, "Cancel");
            jbCancel.push();
            jdWarning.waitClosed();
        } catch (JemmyException ex) {
            // No warning? Nice to know.
        }

        return sResult;
    }

    protected String CreatePHPApplicationInternal() {
        return CreatePHPApplicationInternal(-1);
    }

    // All defaults including name
    protected void CreatePHPApplicationInternal(String sProjectName, int iPort) {
        // Create PHP application

        // Workaround for MacOS platform
        // TODO : check platform
        // TODO : remove after normal issue fix
        NewProjectWizardOperator.invoke().cancel();
        Sleep(1000);

        NewProjectWizardOperator opNewProjectWizard = NewProjectWizardOperator.invoke();
        opNewProjectWizard.selectCategory(PHP_CATEGORY_NAME);
        opNewProjectWizard.selectProject(PHP_PROJECT_NAME);

        opNewProjectWizard.next();

        JDialogOperator jdNew = new JDialogOperator("New PHP Project");

        JTextComponentOperator jtName = new JTextComponentOperator(jdNew, 0);

        if (null != sProjectName) {
            int iSleeps = 0;
            while (!jtName.isEnabled()) {
                if (60 <= ++iSleeps) {
                    fail("Project name disabled during too long time.");
                }
                Sleep(1000);
            }
            jtName.setText(sProjectName);
        }

        String sProjectPath = GetWorkDir() + File.separator + jtName.getText();

        JComboBoxOperator jcPath = new JComboBoxOperator(jdNew, 1);

        int iSleeps = 0;
        while (!jcPath.isEnabled()) {
            if (60 <= ++iSleeps) {
                fail("Project path disabled during too long time.");
            }
            Sleep(1000);
        }

        Timeouts t = jcPath.getTimeouts();
        long lBack = t.getTimeout("JTextComponentOperator.TypeTextTimeout");
        t.setTimeout("JTextComponentOperator.TypeTextTimeout", 30000);
        jcPath.setTimeouts(t);

        jcPath.enterText(sProjectPath);

        t.setTimeout("JTextComponentOperator.TypeTextTimeout", lBack);
        jcPath.setTimeouts(t);

        //NewProjectNameLocationStepOperator opNewProjectNameLocationStep = new NewProjectNameLocationStepOperator( );
        //opNewProjectNameLocationStep.txtProjectLocation( ).setText( GetWorkDir( ) );

        if (-1 != iPort) {
            //opNewProjectWizard.next( );

            // Set new port based URL here
            jdNew = new JDialogOperator("New PHP Project");
            JTextComponentOperator jtUrl = new JTextComponentOperator(jdNew, 1);
            String sUrl = jtUrl.getText();
            System.out.println("== Original: " + sUrl);
            sUrl = sUrl.replace("localhost", "localhost:" + iPort);
            System.out.println("== Fixed: " + sUrl);
            jtUrl.setText(sUrl);
        }

        opNewProjectWizard.finish();

        // Wait for warnings
        Sleep(5000);
        try {
            JDialogOperator jdWarning = new JDialogOperator("Warning");
            JButtonOperator jbCancel = new JButtonOperator(jdWarning, "Cancel");
            jbCancel.push();
            jdWarning.waitClosed();
        } catch (JemmyException ex) {
            // No warning? Nice to know.
        }

    }

    protected void CreatePHPApplicationInternal(String sProjectName) {
        CreatePHPApplicationInternal(sProjectName, -1);
    }

    protected void TypeCode(EditorOperator edit, String code) {
        int iLimit = code.length();
        for (int i = 0; i < iLimit; i++) {
            edit.typeKey(code.charAt(i));
            Sleep(100);
        }
    }

    protected void CheckResult(
            EditorOperator eoPHP,
            String sCheck) {
        CheckResult(eoPHP, sCheck, 0);
    }

    protected void CheckResult(
            EditorOperator eoPHP,
            String sCheck,
            int iOffset) {
        String sText = eoPHP.getText(eoPHP.getLineNumber() + iOffset);

        // Check code completion list
        if (-1 == sText.indexOf(sCheck)) {
            if (bRandomCheck) {
                fail("Invalid completion, looks like issue #153062 still here: \"" + sText + "\", should be: \"" + sCheck + "\"");
            } else {
                fail("Invalid completion: \"" + sText + "\", should be: \"" + sCheck + "\"");
            }
        }
    }

    protected void CheckResultRegex(
            EditorOperator eoPHP,
            String sCheck) {
        String sText = eoPHP.getText(eoPHP.getLineNumber());

        // Check code completion list
        if (!sText.matches(sCheck)) {
            fail("Invalid completion: \"" + sText + "\", should be: \"" + sCheck + "\"");
        }
    }

    protected void TypeCodeCheckResult(
            EditorOperator eoPHP,
            String sType,
            String sCheck) {
        TypeCodeCheckResult(eoPHP, sType, sCheck, 0);
    }

    protected void TypeCodeCheckResult(
            EditorOperator eoPHP,
            String sType,
            String sCheck,
            int iOffset) {
        TypeCode(eoPHP, sType);
        CheckResult(eoPHP, sCheck, iOffset);
    }

    protected void TypeCodeCheckResultRegex(
            EditorOperator eoPHP,
            String sType,
            String sCheck) {
        TypeCode(eoPHP, sType);
        CheckResultRegex(eoPHP, sCheck);
    }

    protected void CheckResult(EditorOperator eoCode, String[] asCode, int iOffset) {
        for (int i = 0; i < asCode.length; i++) {
            CheckResult(eoCode, asCode[i], iOffset + i);
        }
    }

    private class dummyClick implements Runnable {

        private JListOperator list;
        private int index, count;

        public dummyClick(JListOperator l, int i, int j) {
            list = l;
            index = i;
            count = j;
        }

        public void run() {
            list.clickOnItem(index, count);
        }
    }

    protected void ClickListItemNoBlock(
            JListOperator jlList,
            int iIndex,
            int iCount) {
        (new Thread(new dummyClick(jlList, iIndex, iCount))).start();
    }

    protected void ClickForTextPopup(EditorOperator eo, String menu) {
        JEditorPaneOperator txt = eo.txtEditorPane();
        JEditorPane epane = (JEditorPane) txt.getSource();
        try {
            Rectangle rct = epane.modelToView(epane.getCaretPosition());
            txt.clickForPopup(rct.x, rct.y);
            JPopupMenuOperator popup = new JPopupMenuOperator();
            popup.pushMenu(menu);
        } catch (BadLocationException ex) {
            System.out.println("=== Bad location");
        }

        return;
    }

    private void SetTagsSupport(String sTag, String sProject, boolean b) {
        // Open project properties
        ProjectsTabOperator pto = new ProjectsTabOperator();
        ProjectRootNode prn = pto.getProjectRootNode(sProject);
        prn.select();
        prn.callPopup();
        JPopupMenuOperator popup = new JPopupMenuOperator();
        popup.pushMenuNoBlock("Properties");
        JDialogOperator jdProperties = new JDialogOperator("Project Properties - ");
        // Set support
        JCheckBoxOperator box = new JCheckBoxOperator(jdProperties, sTag);
        if (box.isSelected() ^ b) {
            box.clickMouse();
        }
        //Sleep( 10000 );
        // Close dialog
        JButtonOperator bOk = new JButtonOperator(jdProperties, "OK");
        bOk.push();
        jdProperties.waitClosed();
    }

    protected void SetShortTags(String sProject, boolean b) {
        SetTagsSupport("Allow short tags", sProject, b);
    }

    protected void SetAspTags(String sProject, boolean b) {
        SetTagsSupport("Allow ASP tags", sProject, b);
    }

    protected void CreatePHPFile(
            String sProject,
            String sItem,
            String sName) {
        ProjectsTabOperator pto = new ProjectsTabOperator();
        ProjectRootNode prn = pto.getProjectRootNode(sProject);
        prn.select();

        // Workaround for MacOS platform
        NewFileWizardOperator.invoke().cancel();

        NewFileWizardOperator opNewFileWizard = NewFileWizardOperator.invoke();
        opNewFileWizard.selectCategory("PHP");
        opNewFileWizard.selectFileType(sItem);
        opNewFileWizard.next();

        JDialogOperator jdNew = new JDialogOperator("New " + sItem);
        JTextComponentOperator jt = new JTextComponentOperator(jdNew, 0);
        if (null != sName) {
            jt.setText(sName);
        } else {
            sName = jt.getText();
        }

        opNewFileWizard.finish();

        // Check created in project tree
        String sPath = sProject + "|Source Files|" + sName;
        prn = pto.getProjectRootNode(sPath);
        prn.select();

        // Check created in editor
        new EditorOperator(sName);
    }

    protected CompletionInfo GetCompletion() {
        CompletionInfo result = new CompletionInfo();
        result.listItself = null;
        int iRedo = 10;
        while (true) {
            try {
                result.listItself = new CompletionJListOperator();
                try {
                    result.listItems = result.listItself.getCompletionItems();
                    Object o = result.listItems.get(0);
                    if ( //!o.toString( ).contains( "No suggestions" )
                            //&&
                            !o.toString().contains("Scanning in progress...")) {
                        return result;
                    }
                    Sleep(1000);
                } catch (java.lang.Exception ex) {
                    return null;
                }
            } catch (JemmyException ex) {
                System.out.println("Wait completion timeout.");
                if (0 == --iRedo) {
                    return null;
                }
            }
            Sleep(100);
        }
    }

    protected void CheckCompletionItems(
            CompletionJListOperator jlist,
            String[] asIdeal) {
        for (String sCode : asIdeal) {
            int iIndex = jlist.findItemIndex(sCode, new CFulltextStringComparator());
            if (-1 == iIndex) {
                try {
                    List list = jlist.getCompletionItems();
                    for (int i = 0; i < list.size(); i++) {
                        System.out.println("******" + list.get(i));
                    }
                } catch (java.lang.Exception ex) {
                    System.out.println("#" + ex.getMessage());
                }
                fail("Unable to find " + sCode + " completion.");
            }
        }
    }

    protected void CheckCompletionItems(
            CompletionInfo jlist,
            String[] asIdeal) {
        CheckCompletionItems(jlist.listItself, asIdeal);
    }

    protected void Backit(EditorOperator eoPHP, int iCount) {
        for (int i = 0; i < iCount; i++) {
            eoPHP.pressKey(KeyEvent.VK_BACK_SPACE);
        }
    }

    private String Suppress(String sFrom) {
        String sResult = sFrom.replaceAll("[\t\r\n]+", " ");
        sResult = sResult.replaceAll(" +", " ");
        sResult = sResult.replaceAll("^ *", "");
        sResult = sResult.replaceAll(" *$", "");
        sResult = sResult.replaceAll(" *[{] *", "{");
        sResult = sResult.replaceAll(" *[(] *", "(");
        sResult = sResult.replaceAll(" *[}] *", "}");
        sResult = sResult.replaceAll(" *[)] *", ")");
        sResult = sResult.replaceAll(" *= *", "=");
        sResult = sResult.replaceAll(" *, *", ",");
        sResult = sResult.replaceAll(" *; *", ";");

        return sResult;
    }

    protected void CheckFlex(
            EditorOperator eoCode,
            String sIdealCode,
            boolean bDeleteAfter) {
        //System.out.println( "===sIdealCode===" + sIdealCode + "===" );
        // Move up line by line till ideal code starts with
        int iWalkUpLine = eoCode.getLineNumber();
        String sLine;
        while (true) {
            sLine = Suppress(eoCode.getText(iWalkUpLine));
            if (!sLine.equals("")) {
                //System.out.println( "===startwith===" + sLine + "===" );
                if (sIdealCode.startsWith(sLine)) {
                    break;
                }
            }
            iWalkUpLine--;
            /*
            if( !--iWalkUpLine )
            {
            fail( "Unable to find start of flex result." );
            }
             */
        }

        // Move down line by line till whole ideal code found
        int iWalkDownLine = iWalkUpLine + 1;
        while (true) {
            String sNext = eoCode.getText(iWalkDownLine);
            sLine = Suppress(sLine + sNext);
            //System.out.println( "===" + sLine + "===" );
            if (sIdealCode.equals(sLine)) {
                break;
            }

            iWalkDownLine++;
            /*
            if( == ++iWalkDownLine )
            {
            fail( "End of file reached before ideal code found." );
            }
             */
        }

        if (bDeleteAfter) {
            for (int i = 0; i < iWalkDownLine - iWalkUpLine + 1; i++) {
                eoCode.deleteLine(iWalkUpLine);
            }
        }
    }
}
