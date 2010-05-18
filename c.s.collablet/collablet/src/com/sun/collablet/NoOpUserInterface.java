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

import javax.swing.*;


/**
 *
 * @author  todd
 */
public final class NoOpUserInterface extends UserInterface {
    /**
     *
     *
     */
    public void initialize(CollabManager manager) {
        // Do nothing
    }

    /**
     *
     *
     */
    public void deinitialize() {
        // Do nothing
    }

    /**
     *
     *
     */
    public void login() {
        // Do nothing
    }

    /**
     *
     *
     */
    public void login(Account account, String password, Runnable successTask, Runnable failureTask) {
        if (failureTask != null) {
            SwingUtilities.invokeLater(failureTask);
        }
    }

    /**
     *
     *
     */
    public boolean acceptConversation(
        CollabSession session, CollabPrincipal originator, String conversationName, String message
    ) {
        // Do nothing
        return false;
    }

    /**
     *
     *
     */
    public void registerConversationUI(Conversation conversation, Object ui) {
        // Do nothing
    }

    /**
     *
     *
     */
    public boolean showConversation(Conversation conversation) {
        // Do nothing
        return false;
    }

    /**
     *
     *
     */
    public boolean approvePresenceSubscription(CollabSession session, CollabPrincipal subscriber) {
        // Do nothing
        return true;
    }

    /**
     *
     *
     */
    public Account createNewAccount(Account account, String msg) {
        // Do nothing
        return null;
    }

    /**
     *
     *
     */
    public Account getDefaultAccount() {
        return null;
    }

    /**
     *
     *
     */
    public void setDefaultAccount(Account value) {
        // Do nothing
    }

    /**
     *
     *
     */
    public boolean isAutoLoginAccount(Account account) {
        return false;
    }

    /**
     *
     *
     */
    public void setAutoLoginAccount(Account account, boolean value) {
        // Do nothing
    }

    /**
     *
     *
     */
    public void notifySessionError(CollabSession session, String errorMessage) {
        // Do nothing
    }

    /**
     *
     *
     */
    public void notifyAccountRegistrationSucceeded(Account account) {
        // Do nothing
    }

    /**
     *
     *
     */
    public void notifyAccountPasswordChangeSucceeded(Account account) {
        // Do nothing
    }

    /**
     *
     *
     */
    public void notifyAccountUnRegistrationSucceeded(Account account) {
        // Do nothing
    }

    /**
     *
     *
     */
    public void notifyAccountRegistrationFailed(Account account, String reason) {
        // Do nothing
    }

    /**
     *
     *
     */
    public void notifyAccountUnRegistrationFailed(Account account, String reason) {
        // Do nothing
    }

    /**
     *
     *
     */
    public void notifyAccountPasswordChangeFailed(Account account, String reason) {
        // Do nothing
    }

    /**
     *
     *
     */
    public void notifySubscriptionDenied(CollabPrincipal principal) {
        // Do nothing
    }

    /**
     *
     *
     */
    public void notifyConversationEvent(Conversation conversation, int notificationType) {
        // Do nothing
    }

    /**
     *
     *
     */
    public void notifyStatusChange(CollabSession session, CollabPrincipal principal, int notificationType) {
        // Do nothing
    }

    /**
     *
     *
     */
    public void notifyChannelEvent(Conversation conversation, Collablet channel, int notificationType) {
        // Do nothing
    }

    /**
     *
     *
     */
    public boolean createPublicConversation(CollabSession session) {
        return false;
    }

    /**
     *
     *
     */
    public void subscribePublicConversation(CollabSession session) {
        // Do nothing
    }

    /**
     *
     *
     */
    public void notifyInvitationDeclined(String destination, String msg) {
        // Do nothing
    }

    /**
     *
     *
     */
    public void managePublicConversation(CollabSession session, String conversationName) {
        // Do nothing
    }

    /**
     *
     *
     */
    public void inviteUsers(Conversation conversation) {
        // Do nothing
    }

    /**
     *
     *
     */
    public void notifyPublicConversationDeleted(String name) {
        // Do nothing
    }

    /**
     *
     *
     */
    public String getEncryptedLicenseKey() {
        // Do nothing
        return null;
    }

    /**
     *
     *
     */
    public void manageAccounts(Account selectedAccount) {
        // Do nothing
    }

    /**
     *
     *
     */
    public void changeUI(int change) {
        // Do nothing
    }
    
}
