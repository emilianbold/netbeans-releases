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
package com.sun.collablet;


/**
 * Abstraction of the core module's UI requirements.  Note, not all UI
 * interaction need go through this class.  This class only formalizes the
 * required points of interaction according to this module's needs.  A UI
 * module would potentially have many other ways of initiating UI interactions.
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public abstract class UserInterface {
    ////////////////////////////////////////////////////////////////////////////
    // Class members
    ////////////////////////////////////////////////////////////////////////////
    private static Locator LOCATOR;
    public static final int NOTIFY_USER_STATUS_ONLINE = 1;
    public static final int NOTIFY_USER_STATUS_AWAY = 2;
    public static final int NOTIFY_USER_STATUS_IDLE = 3;
    public static final int NOTIFY_USER_STATUS_OFFLINE = 4;
    public static final int NOTIFY_USER_STATUS_WATCHED = 5;
    public static final int NOTIFY_USER_STATUS_PENDING = 6;
    public static final int NOTIFY_USER_STATUS_CHAT = 7;
    public static final int NOTIFY_USER_STATUS_BUSY = 8;
    public static final int NOTIFY_CHAT_MESSAGE_SENT = 9;
    public static final int NOTIFY_CHAT_MESSAGE_RECEIVED = 10;
    public static final int NOTIFY_PARTICIPANT_JOINED = 11;
    public static final int NOTIFY_PARTICIPANT_LEFT = 12;
    public static final int NOTIFY_CHANNEL_MODIFIED = 13;

    public static final int SHOW_COLLAB_SESSION_PANEL = 1;

    /**
     * Called when the user interface is installed in the CollabManager
     *
     */
    public abstract void initialize(CollabManager manager)
    throws CollabException;

    /**
     * Called when the user interface is uninstalled from the CollabManager
     *
     */
    public abstract void deinitialize();

    /**
     * Prompts the user to login
     *
     */
    public abstract void login();

    /**
     * Performs a login without prompting user.  Note that the login occurs
     * asynchronously.
     *
     */
    public abstract void login(Account account, String password, Runnable successTask, Runnable failureTask);

    /**
     * TODO: Need better parameters, like Conversation and CollabMessage;
     * not doing this now as shortcut.
     *
     */
    public abstract boolean acceptConversation(
        CollabSession session, CollabPrincipal originator, String conversationName, String message
    );

    /**
     *
     *
     */
    public abstract void registerConversationUI(Conversation conversation, Object ui);

    /**
     *
     *
     */
    public abstract boolean showConversation(Conversation conversation);

    /**
     *
     *
     */
    public abstract boolean approvePresenceSubscription(CollabSession session, CollabPrincipal subscriber);

    /**
     *
     *
     */
    public abstract Account createNewAccount(Account accountDefaults, String msg);

    /**
     * Returns the default login information.  Note, this method should
     * not prompt the user for this information, and instead should return
     * the login information that was last used, set as default by the user,
     * etc.
     *
     */
    public abstract Account getDefaultAccount();

    /**
     * Sets the default login information.  Modules are cautioned not to use
     * this method to frivolously change this information.  Instead, this
     * method exists to provide cooperating modules with a standard mechanism
     * for setting the default information once it has been gathered in a
     * module-specific way.
     *
     */
    public abstract void setDefaultAccount(Account value);

    /**
     *
     *
     */
    public abstract boolean isAutoLoginAccount(Account account);

    /**
     *
     *
     */
    public abstract void setAutoLoginAccount(Account account, boolean value);

    /**
     *
     *
     */
    public abstract boolean createPublicConversation(CollabSession session);

    /**
     *
     *
     */
    public abstract void managePublicConversation(CollabSession session, String conversationName);

    /**
     *
     *
     */
    public abstract void subscribePublicConversation(CollabSession session);

    /**
     *
     *
     */
    public abstract void inviteUsers(Conversation conversation);

    /**
     *
     *
     */
    public abstract void notifyAccountRegistrationSucceeded(Account account);

    /**
     *
     *
     */
    public abstract void notifyAccountPasswordChangeSucceeded(Account account);

    /**
     *
     *
     */
    public abstract void notifyAccountUnRegistrationSucceeded(Account account);

    /**
     *
     *
     */
    public abstract void notifyAccountRegistrationFailed(Account account, String reason);

    /**
     *
     *
     */
    public abstract void notifyAccountUnRegistrationFailed(Account account, String reason);

    /**
     *
     *
     */
    public abstract void notifyAccountPasswordChangeFailed(Account account, String reason);

    /**
     *
     *
     */
    public abstract void notifySessionError(CollabSession session, String errorMessage);

    /**
     *
     *
     */
    public abstract void notifySubscriptionDenied(CollabPrincipal principal);

    /**
     *
     *
     */
    public abstract void notifyStatusChange(CollabSession session, CollabPrincipal principal, int notificationType);

    /**
     *
     *
     */
    public abstract void notifyConversationEvent(Conversation conversation, int notificationType);

    /**
     *
     *
     */
    public abstract void notifyChannelEvent(Conversation conversation, Collablet channel, int notificationType);

    /**
     *
     *
     */
    public abstract void notifyInvitationDeclined(String destination, String msg);

    /**
     *
     *
     */
    public abstract void notifyPublicConversationDeleted(String name);

    /**
     *
     *
     */
    public abstract void manageAccounts(Account selectedAccount);

    /** 
     *
     *
     */
    public abstract void changeUI(int change);

    ////////////////////////////////////////////////////////////////////////////
    // Lookup methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the current <code>UserInterface</code> instance.  If no
     * <code>Locator</code> has been set by a call to <code>setLocator</code>,
     * this method returns null.
     *
     * @return        The current <code>UserInterface</code> instance or null if
     *                        no instance is available or a <code>Locator</code> hasn't
     *                        been set.
     */
    public static synchronized UserInterface getDefault() {
        if (LOCATOR == null) {
            return null;
        }

        return LOCATOR.getInstance();
    }

    /**
     *
     *
     */
    public static synchronized Locator getLocator() {
        return LOCATOR;
    }

    /**
     * Sets the <code>Locator</code> instance used to find the manager.
     *
     */
    public static synchronized void setLocator(Locator locator) {
        if (LOCATOR != null) {
            throw new IllegalArgumentException("The locator instance has already been set and may " + "not be changed");
        }

        LOCATOR = locator;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////

    /**
     * A simple class used to locate the currently usable instance of
     * <code>CollabManager</code>.
     *
     */
    public static interface Locator {
        /**
         * Returns the <code>UserInterface</code> instance, if any.  This
         * method will be called many times, so it should be as lightweight
         * as possible.
         *
         * @return        A valid instance or null if no instance is available
         */
        public UserInterface getInstance();
    }
}
