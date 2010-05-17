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
package org.netbeans.modules.collab.ui.options;

import org.openide.options.SystemOption;
import org.openide.util.*;


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
