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
package org.netbeans.modules.collab.ui.wizard;

import java.awt.Dimension;

import org.openide.WizardDescriptor;

import com.sun.collablet.Account;

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
