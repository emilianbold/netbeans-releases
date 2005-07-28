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
package org.netbeans.modules.collab.ui.options;

import org.openide.options.*;
import org.openide.util.*;

import java.io.*;

import org.netbeans.modules.collab.*;


/**
 * This class exists because it is not possible to keep any values in the
 * CollabSettings objects that are not present in the BeanInfo.  Because
 * we want these settings to be hidden from the user, we define this class
 * explicitly without a BeanInfo as the central place to store all hidden
 * information.
 *
 * @author        Todd Fast, todd.fast@sun.com
 */
public class HiddenCollabSettings extends SystemOption {
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////
    private static final long serialVersionUID = 1L; // DO NOT CHANGE!
    public static final String PROP_TEST = "test"; // NOI18N
    public static final String PROP_LAST_OPENED_MODE = "lastOpenedMode"; // NOI18N
    public static final String PROP_DEFAULT_ACCOUNT_ID = "defaultAccountID"; // NOI18N
    public static final String PROP_MAIN_SPLIT = "conversationMainSplit"; // NOI18N
    public static final String PROP_CHAT_CHANNEL_SPLIT = "conversationChatChannelSplit"; // NOI18N

    /**
     *
     *
     */
    public HiddenCollabSettings() {
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
    }

    /**
     *
     *
     */
    public String displayName() {
        return "Collaboration Hidden Settings"; // NOI18N
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
    public static HiddenCollabSettings getDefault() {
        HiddenCollabSettings result = (HiddenCollabSettings) findObject(HiddenCollabSettings.class, true);
        assert result != null : "Default HiddenCollabSettings object was null";

        return result;
    }

    /**
     * Default instance of this system option, for the convenience of
     * associated classes.
     *
     */
    public static HiddenCollabSettings getDefault(boolean value) {
        return (HiddenCollabSettings) findObject(HiddenCollabSettings.class, value);
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
     * This is an invisible setting and should not be shown to the user
     *
     */
    public String getLastOpenedMode() {
        return (String) getProperty(PROP_LAST_OPENED_MODE);
    }

    /**
     * This is an invisible setting and should not be shown to the user
     *
     * @param        value
     *                        Value is in seconds
     */
    public void setLastOpenedMode(String value) {
        // true = automatically fire property changes if needed
        putProperty(PROP_LAST_OPENED_MODE, value, true);
    }

    /**
     * This is an invisible setting and should not be shown to the user
     *
     */
    public String getDefaultAccountID() {
        String result = (String) getProperty(PROP_DEFAULT_ACCOUNT_ID);

        return result;
    }

    /**
     * This is an invisible setting and should not be shown to the user
     *
     */
    public void setDefaultAccountID(String value) {
        // true = automatically fire property changes if needed
        putProperty(PROP_DEFAULT_ACCOUNT_ID, value, true);
    }

    /**
     *
     *
     */
    public int getLastConversationMainSplit() {
        Integer result = (Integer) getProperty(PROP_MAIN_SPLIT);

        return (result != null) ? result.intValue() : (-1);
    }

    /**
     *
     *
     */
    public void setLastConversationMainSplit(int value) {
        putProperty(PROP_MAIN_SPLIT, new Integer(value));
    }

    /**
     *
     *
     */
    public int getLastConversationChatChannelSplit() {
        Integer result = (Integer) getProperty(PROP_CHAT_CHANNEL_SPLIT);

        return (result != null) ? result.intValue() : (-1);
    }

    /**
     *
     *
     */
    public void setLastConversationChatChannelSplit(int value) {
        putProperty(PROP_CHAT_CHANNEL_SPLIT, new Integer(value));
    }
}
