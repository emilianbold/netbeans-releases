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
package org.netbeans.jellytools.modules.javacvs;

import javax.swing.JTextField;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JFileChooserOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextAreaOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/** Class implementing all necessary methods for handling "Folder to Import" panel of
 * Import wizard.
 * <br>
 * Usage:<br>
 * <pre>
 *      ImportWizardOperator.invoke();
 *      CVSRootStepOperator cvsRootOper = new CVSRootStepOperator();
 *      cvsRootOper.setPassword("password");
 *      cvsRootOper.setCVSRoot(":pserver:user@host:repository");
 *      cvsRootOper.next();
 *      FolderToImportStepOperator folderToImportOper = new FolderToImportStepOperator();
 *      folderToImportOper.setFolderToImport("/tmp/myLocalfolder");
 *      folderToImportOper.setImportMessage("Import message");
 *      folderToImportOper.setRepositoryFolder("folder");
 *      folderToImportOper.finish();
 * </pre>
 * @see ImportWizardOperator
 * @see BrowseRepositoryFolderOperator
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class FolderToImportStepOperator extends ImportWizardOperator {

    /** Waits for Folder to Import panel. */
    public FolderToImportStepOperator() {
        super();
        stepsWaitSelectedValue(Bundle.getString(
                "org.netbeans.modules.versioning.system.cvss.ui.actions.project.Bundle",
                "BK0014"));
    }

    private JLabelOperator _lblFolderToImport;
    private JTextFieldOperator _txtFolderToImport;
    private JButtonOperator _btBrowse;
    private JLabelOperator _lblImportMessage;
    private JTextAreaOperator _txtImportMessage;
    private JLabelOperator _lblRepositoryFolder;
    private JTextFieldOperator _txtRepositoryFolder;
    private JButtonOperator _btBrowse2;
    private JCheckBoxOperator _cbCheckoutAfterImport;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "Folder to Import:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblFolderToImport() {
        if (_lblFolderToImport==null) {
            _lblFolderToImport = new JLabelOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.actions.project.Bundle", "BK1104"));
        }
        return _lblFolderToImport;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtFolderToImport() {
        if (_txtFolderToImport==null) {
            _txtFolderToImport = new JTextFieldOperator(
                    (JTextField)lblFolderToImport().getLabelFor());
        }
        return _txtFolderToImport;
    }

    /** Tries to find "Browse..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btBrowseFolderToImport() {
        if (_btBrowse==null) {
            _btBrowse = new JButtonOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.wizards.Bundle", "BK2003"));
        }
        return _btBrowse;
    }

    /** Tries to find "Import Message:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblImportMessage() {
        if (_lblImportMessage==null) {
            _lblImportMessage = new JLabelOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.actions.project.Bundle", "BK0001"));
        }
        return _lblImportMessage;
    }

    /** Tries to find null KTextArea in this dialog.
     * @return JTextAreaOperator
     */
    public JTextAreaOperator txtImportMessage() {
        if (_txtImportMessage==null) {
            _txtImportMessage = new JTextAreaOperator(this);
        }
        return _txtImportMessage;
    }

    /** Tries to find "Repository Folder:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblRepositoryFolder() {
        if (_lblRepositoryFolder==null) {
            _lblRepositoryFolder = new JLabelOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.actions.project.Bundle", "Bk0002"));
        }
        return _lblRepositoryFolder;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtRepositoryFolder() {
        if (_txtRepositoryFolder==null) {
            _txtRepositoryFolder = new JTextFieldOperator(
                    (JTextField)lblRepositoryFolder().getLabelFor());
        }
        return _txtRepositoryFolder;
    }

    /** Tries to find "Browse..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btBrowseRepositoryFolder() {
        if (_btBrowse2==null) {
            _btBrowse2 = new JButtonOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.wizards.Bundle", "BK2003"),
                    1);
        }
        return _btBrowse2;
    }

    /** Tries to find "Checkout After Import" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbCheckoutAfterImport() {
        if (_cbCheckoutAfterImport==null) {
            _cbCheckoutAfterImport = new JCheckBoxOperator(this,
                    Bundle.getStringTrimmed(
                        "org.netbeans.modules.versioning.system.cvss.ui.actions.project.Bundle", "BK0011"));
        }
        return _cbCheckoutAfterImport;
    }

    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** gets text for txtFolderToImport
     * @return String text
     */
    public String getFolderToImport() {
        return txtFolderToImport().getText();
    }

    /** sets text for txtFolderToImport
     * @param text String text
     */
    public void setFolderToImport(String text) {
        txtFolderToImport().clearText();
        txtFolderToImport().typeText(text);
    }

    /** clicks on "Browse..." button to browse for folder to import and returns
     * JFileChooserOperator. 
     * @return JFileChooserOperator instance
     */
    public JFileChooserOperator browseFolderToImport() {
        btBrowseFolderToImport().pushNoBlock();
        return new JFileChooserOperator();
    }

    /** gets text for txtImportMessage
     * @return String text
     */
    public String getImportMessage() {
        return txtImportMessage().getText();
    }

    /** sets text for txtImportMessage
     * @param text String text
     */
    public void setImportMessage(String text) {
        txtImportMessage().clearText();
        txtImportMessage().typeText(text);
    }

    /** gets text for txtRepositoryFolder
     * @return String text
     */
    public String getRepositoryFolder() {
        return txtRepositoryFolder().getText();
    }

    /** sets text for txtRepositoryFolder
     * @param text String text
     */
    public void setRepositoryFolder(String text) {
        txtRepositoryFolder().clearText();
        txtRepositoryFolder().typeText(text);
    }

    /** Clicks on "Browse..." button to browse for repository folder and returns
     * BrowseRepositoryFolderOperator.
     * @return BrowseRepositoryFolderOperator instance
     */
    public BrowseRepositoryFolderOperator browseRepositoryFolder() {
        btBrowseRepositoryFolder().pushNoBlock();
        return new BrowseRepositoryFolderOperator();
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkCheckoutAfterImport(boolean state) {
        if (cbCheckoutAfterImport().isSelected()!=state) {
            cbCheckoutAfterImport().push();
        }
    }


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /**
     * Performs verification of FolderToImportStepOperator by accessing all its components.
     */
    public void verify() {
        lblFolderToImport();
        txtFolderToImport();
        btBrowseFolderToImport();
        lblImportMessage();
        txtImportMessage();
        lblRepositoryFolder();
        txtRepositoryFolder();
        btBrowseRepositoryFolder();
        cbCheckoutAfterImport();
    }
}

