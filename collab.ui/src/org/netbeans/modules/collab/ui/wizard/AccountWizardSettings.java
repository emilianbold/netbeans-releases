/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.collab.ui.wizard;

import com.sun.collablet.Account;

import org.openide.*;

import java.awt.Dimension;

import org.netbeans.modules.collab.*;


/**
 *
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class AccountWizardSettings {
    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private WizardDescriptor descriptor;
    private Account account;
    private boolean isNew = true;
    private Dimension preferredSize;
    private String message;

    /**
     *
     *
     */
    public AccountWizardSettings() {
        super();
    }

    /**
     *
     *
     */
    public AccountWizardSettings(Account account) {
        this();
        this.account = account;
    }

    /**
     *
     *
     */
    public Dimension getPreferredPanelSize() {
        return preferredSize;
    }

    /**
     *
     *
     */
    protected void setPreferredPanelSize(Dimension value) {
        preferredSize = value;
    }

    /**
     *
     *
     */
    public WizardDescriptor getWizardDescriptor() {
        return descriptor;
    }

    /**
     *
     *
     */
    public void setWizardDescriptor(WizardDescriptor value) {
        descriptor = value;
    }

    /**
     *
     *
     */
    public Account getAccount() {
        return account;
    }

    /**
     *
     *
     */
    public void setAccount(Account value) {
        account = value;
    }

    /**
     *
     *
     */
    public boolean isNewAccount() {
        return isNew;
    }

    /**
     *
     *
     */
    public void setNewAccount(boolean value) {
        isNew = value;
    }

    /**
     *
     *
     */
    public static AccountWizardSettings narrow(Object object) {
        return (AccountWizardSettings) object;
    }

    /**
     *
     *
     */
    public String getMessage() {
        return message;
    }

    /**
     *
     *
     */
    public void setMessage(String value) {
        message = value;
    }
}
