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
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/** Class implementing all necessary methods for handling "Edit CVS Root" dialog.
 * It is open from Checkout Wizard.
 * <br>
 * Usage:<br>
 * <pre>
 *      CVSRootStepOperator cvsRootOper = new CVSRootStepOperator();
 *      EditCVSRootOperator editOper = cvsRootOper.edit();
 *      editOper.selectAccessMethod(editOper.ITEM_PSERVER);
 *      editOper.setUser("user");// NOI18N
 *      editOper.setHost("host");// NOI18N
 *      editOper.setRepositoryPath("repository");// NOI18N
 *      editOper.setPort("8080");
 *      editOper.ok();
 *</pre>
 *
 * @see CheckoutWizardOperator
 * @see CVSRootStepOperator
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class EditCVSRootOperator extends NbDialogOperator {
    
    /** Waits for "Edit CVS Root" dialog. */
    public EditCVSRootOperator() {
        super(Bundle.getStringTrimmed(
                "org.netbeans.modules.versioning.system.cvss.ui.wizards.Bundle", "BK2024"));
    }
    
    private JLabelOperator _lblAccessMethod;
    private JComboBoxOperator _cboAccessMethod;
    /** pserver item. */
    public static final String ITEM_PSERVER = "pserver"; //NOI18N
    /** ext item. */
    public static final String ITEM_EXT = "ext"; //NOI18N
    /** local item. */
    public static final String ITEM_LOCAL = "local"; //NOI18N
    /** fork item. */
    public static final String ITEM_FORK = "fork"; //NOI18N
    private JLabelOperator _lblUser;
    private JTextFieldOperator _txtUser;
    private JLabelOperator _lblHost;
    private JTextFieldOperator _txtHost;
    private JLabelOperator _lblPort;
    private JTextFieldOperator _txtPort;
    private JLabelOperator _lblRepositoryPath;
    private JTextFieldOperator _txtRepositoryPath;
    
    //******************************
    // Subcomponents definition part
    //******************************
    
    /** Tries to find "Access Method:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblAccessMethod() {
        if (_lblAccessMethod==null) {
            _lblAccessMethod = new JLabelOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.wizards.Bundle", "BK1006"));
        }
        return _lblAccessMethod;
    }
    
    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboAccessMethod() {
        if (_cboAccessMethod==null) {
            _cboAccessMethod = new JComboBoxOperator(this);
        }
        return _cboAccessMethod;
    }
    
    /** Tries to find "User:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblUser() {
        if (_lblUser==null) {
            _lblUser = new JLabelOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.wizards.Bundle", "BK1007"));
        }
        return _lblUser;
    }
    
    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtUser() {
        if (_txtUser==null) {
            _txtUser = new JTextFieldOperator(
                    (JTextField)lblUser().getLabelFor());
        }
        return _txtUser;
    }
    
    /** Tries to find "Host:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblHost() {
        if (_lblHost==null) {
            _lblHost = new JLabelOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.wizards.Bundle", "BK1008"));
        }
        return _lblHost;
    }
    
    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtHost() {
        if (_txtHost==null) {
            _txtHost = new JTextFieldOperator(
                    (JTextField)lblHost().getLabelFor());
        }
        return _txtHost;
    }
    
    /** Tries to find "Port:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblPort() {
        if (_lblPort==null) {
            _lblPort = new JLabelOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.wizards.Bundle", "BK1009"));
        }
        return _lblPort;
    }
    
    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtPort() {
        if (_txtPort==null) {
            _txtPort = new JTextFieldOperator(
                    (JTextField)lblPort().getLabelFor());
        }
        return _txtPort;
    }
    
    /** Tries to find "Repository Path:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblRepositoryPath() {
        if (_lblRepositoryPath==null) {
            _lblRepositoryPath = new JLabelOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.versioning.system.cvss.ui.wizards.Bundle", "BK1005"));
        }
        return _lblRepositoryPath;
    }
    
    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtRepositoryPath() {
        if (_txtRepositoryPath==null) {
            _txtRepositoryPath = new JTextFieldOperator(
                    (JTextField)lblRepositoryPath().getLabelFor());
        }
        return _txtRepositoryPath;
    }
    
    //****************************************
    // Low-level functionality definition part
    //****************************************
    
    /** returns selected item for cboAccessMethod
     * @return String item
     */
    public String getAccessMethod() {
        return cboAccessMethod().getSelectedItem().toString();
    }
    
    /** selects item for cboAccessMethod
     * @param item String item
     */
    public void selectAccessMethod(String item) {
        cboAccessMethod().selectItem(item);
    }
    
    /** gets text for txtUser
     * @return String text
     */
    public String getUser() {
        return txtUser().getText();
    }
    
    /** sets text for txtUser
     * @param text String text
     */
    public void setUser(String text) {
        txtUser().clearText();
        txtUser().typeText(text);
    }
    
    /** gets text from Host text field.
     * @return text from Host text field.
     */
    public String getHost() {
        return txtHost().getText();
    }
    
    /** sets text for txtHost
     * @param text String text
     */
    public void setHost(String text) {
        txtHost().clearText();
        txtHost().typeText(text);
    }
    
    /** gets text for txtPort
     * @return String text
     */
    public String getPort() {
        return txtPort().getText();
    }
    
    /** sets text for txtPort
     * @param text String text
     */
    public void setPort(String text) {
        txtPort().clearText();
        txtPort().typeText(text);
    }
    
    /** gets text for txtRepositoryPath
     * @return String text
     */
    public String getRepositoryPath() {
        return txtRepositoryPath().getText();
    }
    
    /** sets text for txtRepositoryPath
     * @param text String text
     */
    public void setRepositoryPath(String text) {
        txtRepositoryPath().clearText();
        txtRepositoryPath().typeText(text);
    }
    
    //*****************************************
    // High-level functionality definition part
    //*****************************************
    
    /**
     * Performs verification of EditCVSRootOperator by accessing all its components.
     */
    public void verify() {
        lblAccessMethod();
        cboAccessMethod();
        lblUser();
        txtUser();
        lblHost();
        txtHost();
        lblPort();
        txtPort();
        lblRepositoryPath();
        txtRepositoryPath();
    }
}

