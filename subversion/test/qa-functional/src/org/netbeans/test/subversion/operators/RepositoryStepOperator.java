/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
/*
 * CheckoutWizardOperator.java
 *
 * Created on 19/04/06 13:24
 */
package org.netbeans.test.subversion.operators;

import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jemmy.operators.*;

/**
 * Class implementing all necessary methods for handling "CheckoutWizardOperator" NbDialog.
 *
 *
 * @author peter
 * @version 1.0
 */
public class RepositoryStepOperator extends WizardOperator {

    public static final String ITEM_FILE = "file:///";
    public static final String ITEM_HTTP = "http://";
    public static final String ITEM_HTTPS = "https://";
    public static final String ITEM_SVN = "svn://";
    public static final String ITEM_SVNSSH = "svn+ssh://";
    
    /**
     * Creates new CheckoutWizardOperator that can handle it.
     */
    public RepositoryStepOperator() {
        super(""); //NO I18N
        stepsWaitSelectedValue("Repository");
    }

    private JLabelOperator _lblSteps;
    private JListOperator _lstSteps;
    private JLabelOperator _lblRepository;
    private JButtonOperator _btProxyConfiguration;
    private JLabelOperator _lblUseExternal;
    private JLabelOperator _lblTunnelCommand;
    private JTextFieldOperator _txtTunnelCommand;
    private JLabelOperator _lblPassword;
    private JLabelOperator _lblUser;
    private JLabelOperator _lblRepositoryURL;
    private JTextFieldOperator _txtUser;
    private JPasswordFieldOperator _txtPassword;
    private JLabelOperator _lblLeaveBlankForAnonymousAccess;
    private JComboBoxOperator _cboRepositoryURL;
    private JLabelOperator _lblSpecifySubversionRepositoryLocation;
    private JLabelOperator _lblWizardDescriptor$FixedHeightLabel;
    private JButtonOperator _btStop;
    private JButtonOperator _btBack;
    private JButtonOperator _btNext;
    private JButtonOperator _btFinish;
    private JButtonOperator _btCancel;
    private JButtonOperator _btHelp;
    private JLabelOperator _lblWarning;
    
    
    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "Steps" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSteps() {
        if (_lblSteps==null) {
            _lblSteps = new JLabelOperator(this, "Steps");
        }
        return _lblSteps;
    }

    /** Tries to find null JList in this dialog.
     * @return JListOperator
     */
    public JListOperator lstSteps() {
        if (_lstSteps==null) {
            _lstSteps = new JListOperator(this);
        }
        return _lstSteps;
    }

    /** Tries to find "Repository" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblRepository() {
        if (_lblRepository==null) {
            _lblRepository = new JLabelOperator(this, "Repository");
        }
        return _lblRepository;
    }

    /** Tries to find "Proxy Configuration..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btProxyConfiguration() {
        if (_btProxyConfiguration==null) {
            _btProxyConfiguration = new JButtonOperator(this, "Proxy Configuration...");
        }
        return _btProxyConfiguration;
    }
    
    /** Tries to find "Use External Tunnel" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblUseExternal() {
        if (_lblUseExternal==null) {
            _lblUseExternal = new JLabelOperator(this, "Use External");
        }
        return _lblUseExternal;
    }
    
    /** Tries to find "Tunnel Command" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblTunnelCommand() {
        if (_lblTunnelCommand==null) {
            _lblTunnelCommand = new JLabelOperator(this, "Tunnel Command");
        }
        return _lblTunnelCommand;
    }

    /** Tries to find "Password:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblPassword() {
        if (_lblPassword==null) {
            _lblPassword = new JLabelOperator(this, "Password:");
        }
        return _lblPassword;
    }

    /** Tries to find "User:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblUser() {
        if (_lblUser==null) {
            _lblUser = new JLabelOperator(this, "User:");
        }
        return _lblUser;
    }

    /** Tries to find "Repository URL:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblRepositoryURL() {
        if (_lblRepositoryURL==null) {
            _lblRepositoryURL = new JLabelOperator(this, "Repository URL:");
        }
        return _lblRepositoryURL;
    }

    /** Tries to find "Tunnel Command" JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtTunnelCommand() {
        if (_txtTunnelCommand == null)
            _txtTunnelCommand = new JTextFieldOperator(this, 1);
        return _txtTunnelCommand;
    }
    
    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtUser() {
        if (_txtUser == null)
            _txtUser = new JTextFieldOperator(this);
        return _txtUser;
    }

    /** Tries to find null JPasswordField in this dialog.
     * @return JPasswordFieldOperator
     */
    public JPasswordFieldOperator txtPassword() {
        if (_txtPassword == null)
            _txtPassword = new JPasswordFieldOperator(this);
        return _txtPassword;
    }

    /** Tries to find "(leave blank for anonymous access)" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblLeaveBlankForAnonymousAccess() {
        if (_lblLeaveBlankForAnonymousAccess==null) {
            _lblLeaveBlankForAnonymousAccess = new JLabelOperator(this, "(leave blank for anonymous access)");
        }
        return _lblLeaveBlankForAnonymousAccess;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboRepositoryURL() {
        if (_cboRepositoryURL==null) {
            _cboRepositoryURL = new JComboBoxOperator(this);
        }
        return _cboRepositoryURL;
    }

    /** Tries to find "Specify Subversion repository location:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSpecifySubversionRepositoryLocation() {
        if (_lblSpecifySubversionRepositoryLocation==null) {
            _lblSpecifySubversionRepositoryLocation = new JLabelOperator(this, "Specify Subversion repository location:");
        }
        return _lblSpecifySubversionRepositoryLocation;
    }

    /** Tries to find null WizardDescriptor$FixedHeightLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblWizardDescriptor$FixedHeightLabel() {
        if (_lblWizardDescriptor$FixedHeightLabel==null) {
            _lblWizardDescriptor$FixedHeightLabel = new JLabelOperator(this, 7);
        }
        return _lblWizardDescriptor$FixedHeightLabel;
    }
    
    public JLabelOperator lblWarning() {
        if (_lblWarning == null) {
            _lblWarning = new JLabelOperator(this, 5);
        }
        return _lblWarning;
    }

    /** Tries to find "Stop" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btStop() {
        if (_btStop==null) {
            _btStop = new JButtonOperator(this, "Stop");
        }
        return _btStop;
    }
    
    /** Tries to find "< Back" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btBack() {
        if (_btBack==null) {
            _btBack = new JButtonOperator(this, "< Back");
        }
        return _btBack;
    }

    /** Tries to find "Next >" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btNext() {
        if (_btNext==null) {
            _btNext = new JButtonOperator(this, "Next >");
        }
        return _btNext;
    }

    /** Tries to find "Finish" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btFinish() {
        if (_btFinish==null) {
            _btFinish = new JButtonOperator(this, "Finish");
        }
        return _btFinish;
    }

    /** Tries to find "Cancel" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btCancel() {
        if (_btCancel==null) {
            _btCancel = new JButtonOperator(this, "Cancel");
        }
        return _btCancel;
    }

    /** Tries to find "Help" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btHelp() {
        if (_btHelp==null) {
            _btHelp = new JButtonOperator(this, "Help");
        }
        return _btHelp;
    }

    public ProxyConfigurationOperator invokeProxy() {
        btProxyConfiguration().pushNoBlock();
        return new ProxyConfigurationOperator();
    }
    
    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** clicks on "Proxy Configuration..." JButton
     */
    public void proxyConfiguration() {
        btProxyConfiguration().pushNoBlock();
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
        txtUser().setText(text);
    }

    /** types text for txtUser
     * @param text String text
     */
    public void typeUser(String text) {
        txtUser().typeText(text);
    }

    /** sets text for txtPassword
     * @param text String text
     */
    public void setPassword(String text) {
        txtPassword().setText(text);
    }

    /** types text for txtPassword
     * @param text String text
     */
    public void typePassword(String text) {
        txtPassword().typeText(text);
    }

    /** returns selected item for cboRepositoryURL
     * @return String item
     */
    public String getSelectedRepositoryURL() {
        return cboRepositoryURL().getSelectedItem().toString();
    }

    /** selects item for cboRepositoryURL
     * @param item String item
     */
    public void selectRepositoryURL(String item) {
        cboRepositoryURL().selectItem(item);
    }
    
    public void setRepositoryURL(String url) {
        cboRepositoryURL().clearText();
        cboRepositoryURL().typeText(url);
    }

    /** types text for cboRepositoryURL
     * @param text String text
     */
    public void typeRepositoryURL(String text) {
        cboRepositoryURL().typeText(text);
    }

    /** clicks on "< Back" JButton
     */
    public void back() {
        btBack().push();
    }

    /** clicks on "Next >" JButton
     */
    public void next() {
        btNext().push();
    }

    /** clicks on "Finish" JButton
     */
    public void finish() {
        btFinish().push();
    }

    /** clicks on "Cancel" JButton
     */
    public void cancel() {
        btCancel().push();
    }

    /** clicks on "Help" JButton
     */
    public void help() {
        btHelp().push();
    }


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /**
     * Performs verification of CheckoutWizardOperator by accessing all its components.
     */
    public void verify() {
        lblSteps();
        lstSteps();
        lblRepository();
        btProxyConfiguration();
        lblPassword();
        lblUser();
        lblRepositoryURL();
        txtUser();
        txtPassword();
        lblLeaveBlankForAnonymousAccess();
        cboRepositoryURL();
        lblSpecifySubversionRepositoryLocation();
        lblWizardDescriptor$FixedHeightLabel();
        btBack();
        btNext();
        btFinish();
        btCancel();
        btHelp();
    }
}

