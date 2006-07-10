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
package org.netbeans.modules.collab.ui.options;

import org.openide.options.SystemOption;
import org.openide.util.*;


/**
 * This class is for storage of user-visible properties only.  All properties
 * must have a descriptor defined in the BeanInfo if they are to be persisted.
 *
 * @author        Todd Fast, todd.fast@sun.com
 */
public class CollabSettings extends SystemOption {
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////
    private static final long serialVersionUID = 1L; // DO NOT CHANGE!
    public static final String PROP_TEST = "test"; // NOI18N
    public static final String PROP_IDLE_TIMEOUT = "idleTimeout";
    public static final String PROP_AUTO_APPROVE = "autoApprove";
    public static final String PROP_AUTO_LOGIN = "autoLogin";
    public static final String PROP_AUTO_ACCEPT_CONVERSATION = "autoAcceptConversation";

    /**
     *
     *
     */
    public CollabSettings() {
        super();
    }

    /**
     *
     *
     */
    protected void initialize() {
        super.initialize();

        // If you have more complex default values which might require
        // other parts of the module to already be installed, do not
        // put them here; e.g. make the getter return them as a
        // default if getProperty returns null. (The class might be
        // initialized partway through module installation.)
        setTest(Boolean.FALSE);
        setIdleTimeout(new Integer(5)); // 5 minutes
        setAutoApprove(Boolean.FALSE);
        setAutoLogin(Boolean.TRUE);
        setAutoAcceptConversation(Boolean.FALSE);
    }

    /**
     *
     *
     */
    protected boolean clearSharedData() {
        super.clearSharedData();

        return false;
    }

    /**
     *
     *
     */
    public String displayName() {
        return NbBundle.getMessage(CollabSettings.class, "LBL_CollabSettings_DisplayName"); // NOI18N
    }

    /**
     *
     *
     */
    public HelpCtx getHelpCtx() {
        // If you provide context help then use:
        // return new HelpCtx(CollabSettings.class);
        return HelpCtx.DEFAULT_HELP;
    }

    /**
     * Default instance of this system option, for the convenience of
     * associated classes.
     *
     */
    public static CollabSettings getDefault() {
        CollabSettings result = (CollabSettings) findObject(CollabSettings.class, true);
        assert result != null : "Default CollabSettings object was null";

        return result;
    }

    /**
     * Default instance of this system option, for the convenience of
     * associated classes.
     *
     */
    public static CollabSettings getDefault(boolean value) {
        return (CollabSettings) findObject(CollabSettings.class, value);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Option property methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public Boolean getTest() {
        return (Boolean) getProperty(PROP_TEST);
    }

    /**
     *
     *
     */
    public void setTest(Boolean value) {
        // true = automatically fire property changes if needed
        putProperty(PROP_TEST, value, true);
    }

    /**
     *
     *
     */
    public Integer getIdleTimeout() {
        return (Integer) getProperty(PROP_IDLE_TIMEOUT);
    }

    /**
     *
     * @param        value
     *                        Value is in seconds
     */
    public void setIdleTimeout(Integer value) {
        // true = automatically fire property changes if needed
        putProperty(PROP_IDLE_TIMEOUT, value, true);
    }

    /**
     *
     *
     */
    public Boolean getAutoApprove() {
        return (Boolean) getProperty(PROP_AUTO_APPROVE);
    }

    /**
     *
     *
     */
    public void setAutoApprove(Boolean value) {
        // true = automatically fire property changes if needed
        putProperty(PROP_AUTO_APPROVE, value, true);
    }

    /**
     *
     *
     */
    public Boolean getAutoLogin() {
        return (Boolean) getProperty(PROP_AUTO_LOGIN);
    }

    /**
     *
     *
     */
    public void setAutoLogin(Boolean value) {
        // true = automatically fire property changes if needed
        putProperty(PROP_AUTO_LOGIN, value, true);
    }

    /**
     *
     *
     */
    public Boolean getAutoAcceptConversation() {
        return (Boolean) getProperty(PROP_AUTO_ACCEPT_CONVERSATION);
    }

    /**
     *
     *
     */
    public void setAutoAcceptConversation(Boolean value) {
        // true = automatically fire property changes if needed
        putProperty(PROP_AUTO_ACCEPT_CONVERSATION, value, true);
    }
}
