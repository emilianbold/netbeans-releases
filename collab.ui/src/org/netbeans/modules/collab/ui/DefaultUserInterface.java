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
package org.netbeans.modules.collab.ui;

import java.applet.*;
import java.awt.Dialog;
import java.awt.Toolkit;
import java.awt.event.*;
import java.beans.*;
import java.io.IOException;
import java.lang.ref.*;
import java.net.*;
import java.text.MessageFormat;
import java.util.*;
import javax.swing.*;

import org.openide.*;
import org.openide.awt.Mnemonics;
import org.openide.cookies.*;
import org.openide.nodes.*;
import org.openide.util.*;

import com.sun.collablet.*;
import org.netbeans.modules.collab.core.Debug;
import org.netbeans.modules.collab.ui.options.CollabSettings;
import org.netbeans.modules.collab.ui.options.HiddenCollabSettings;
import org.netbeans.modules.collab.ui.options.NotificationSettings;
import org.netbeans.modules.collab.ui.wizard.AccountWizardIterator;
import org.netbeans.modules.collab.ui.wizard.AccountWizardSettings;

/**
 * Default IDE user interface implementation
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public class DefaultUserInterface extends UserInterface {
    ////////////////////////////////////////////////////////////////////////////
    // Class fields
    ////////////////////////////////////////////////////////////////////////////
    public static final String ATTRIBUTE_AUTO_LOGIN = "org.netbeans.modules.collab.ui.autoLogin";

    ////////////////////////////////////////////////////////////////////////////
    // Instance fields
    ////////////////////////////////////////////////////////////////////////////
    private Map conversationNodes = new IdentityHashMap();

    /**
     *
     *
     */
    public DefaultUserInterface() {
        super();
    }

    /**
     *
     *
     */
    public void initialize(CollabManager manager) throws CollabException {
        IdleDetectionListener.attach();
        NotificationBar.install();
    }

    /**
     *
     *
     */
    public void deinitialize() {
        conversationNodes.clear();

        NotificationBar.uninstall();
        IdleDetectionListener.detatch();
        NotificationRegistry.deinitialize();
    }

    /**
     *
     *
     */
    public void login() {
        CollabExplorerPanel.getInstance().open();
        CollabExplorerPanel.getInstance().showComponent(CollabExplorerPanel.COMPONENT_LOGIN);
        CollabExplorerPanel.getInstance().requestActive();
    }

    /**
     *
     *
     */
    public void login(Account account, String password, Runnable successTask, Runnable failureTask) {
        // Perform the login in a different thread
        new Thread(
            new LoginTask(account, password, successTask, failureTask),
            "Collaboration Login: " + account.getDisplayName()
        ).start(); // NOI18M
    }

    /**
     *
     *
     */
    public boolean acceptConversation(
        CollabSession session, CollabPrincipal originator, String conversationName, String message
    ) {
        assert originator != null : "originator was null";

        if (CollabSettings.getDefault().getAutoAcceptConversation().booleanValue() == true) {
            return true;
        }

        String name = NbBundle.getMessage(
                DefaultUserInterface.class, "MSG_DefaultUserInterface_InvitationOriginator",
                new Object[] { originator.getDisplayName(), originator.getIdentifier() }
            );

        if ((message == null) || (message.trim().length() == 0)) {
            message = NbBundle.getMessage(
                    DefaultUserInterface.class, "MSG_DefaultUserInterface_EmptyInvitationMessage"
                );
        }

        JOptionPane messageText = new JOptionPane(
            NbBundle.getMessage(DefaultUserInterface.class, "MSG_DefaultUserInterface_AcceptConference", new Object[] { name, message }),
            JOptionPane.PLAIN_MESSAGE,
            0, // options type
            null, // icon
            new Object[0], // options
            null // value
        ) {
            public int getMaxCharactersPerLineCount() {
                return 100;
            }
        };
        messageText.getAccessibleContext().setAccessibleName(NbBundle.getMessage(DefaultUserInterface.class,
            "ACSD_NAME_MSG_DefaultUserInterface_AcceptConference"));
        messageText.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DefaultUserInterface.class,
            "ACSD_DESC_MSG_DefaultUserInterface_AcceptConference"));

        JButton acceptOption = new JButton();
        Mnemonics.setLocalizedText (acceptOption, NbBundle.getMessage(DefaultUserInterface.class,
            "OPT_DefaultUserInterface_AcceptConference"));
        acceptOption.getAccessibleContext().setAccessibleName(NbBundle.getMessage(DefaultUserInterface.class,
            "ACSD_NAME_DefaultUserInterface_AcceptConference"));
        acceptOption.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DefaultUserInterface.class,
            "ACSD_DESC_DefaultUserInterface_AcceptConference"));

        JButton declineOption = new JButton();
        Mnemonics.setLocalizedText (declineOption, NbBundle.getMessage(DefaultUserInterface.class,
            "OPT_DefaultUserInterface_DeclineConference"));
        declineOption.getAccessibleContext().setAccessibleName(NbBundle.getMessage(DefaultUserInterface.class,
            "ACSD_NAME_DefaultUserInterface_DeclineConference"));
        declineOption.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DefaultUserInterface.class,
            "ACSD_DESC_DefaultUserInterface_DeclineConference"));

        // Prompt to accept conference
        Object[] options=new Object[] {acceptOption,declineOption};
        NotifyDescriptor descriptor=new NotifyDescriptor(
                (Object)NbBundle.getMessage(DefaultUserInterface.class, "MSG_DefaultUserInterface_AcceptConference", new Object[] { name, message }),
                NbBundle.getMessage (NotifyDescriptor.class, "NTF_QuestionTitle"),
                NotifyDescriptor.YES_NO_OPTION,
                NotifyDescriptor.QUESTION_MESSAGE,
                options,
                options[0]
        );
        return DialogDisplayer.getDefault().notify(descriptor) == acceptOption;
    }

    /**
     *
     *
     */
    public void registerConversationUI(Conversation conversation, Object ui) {
        // Note, we use a weak reference to the UI object (node) to make sure
        // we don't keep around references to it even if it's destroyed. This
        // is a general requirement when dealing with nodes.
        conversationNodes.put(conversation, new WeakReference(ui));

        // Add a listener to unregister the conversation when the conversation
        // becomes invalid
        conversation.addPropertyChangeListener(
            new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent event) {
                    if (event.getPropertyName().equals(Conversation.PROP_VALID)) {
                        DefaultUserInterface.this.conversationNodes.remove(event.getSource());
                    }
                }
            }
        );
    }

    /**
     *
     *
     */
    protected Object getConversationUI(Conversation conversation) {
        Reference nodeRef = (Reference) conversationNodes.get(conversation);

        if (nodeRef == null) {
            return null;
        }

        return nodeRef.get();
    }

    /**
     *
     *
     */
    public boolean showConversation(Conversation conversation) {
        // Get the node from the reference.  Note, we know this is a Node, as
        // this is our assumption in this class implementation
        Node node = (Node) getConversationUI(conversation);

        if (node == null) {
            return false;
        }

        OpenCookie cookie = (OpenCookie) node.getCookie(OpenCookie.class);
        assert cookie != null : "OpenCookie was not available from the conversation node";

        //		// Select the conversation in the collab explorer
        //		CollabExplorerPanel.getInstance().setActivatedNodes(new Node[] {node});
        // Open the conversation view
        cookie.open();

        return true;
    }

    /**
     *
     *
     */
    public boolean approvePresenceSubscription(CollabSession session, CollabPrincipal subscriber) {
        if (CollabSettings.getDefault().getAutoApprove().booleanValue() == true) {
            boolean isContact = false;

            ContactGroup[] groups = session.getContactGroups();

            for (int i = 0; i < groups.length; i++) {
                CollabPrincipal[] contacts = groups[i].getContacts();

                for (int j = 0; j < contacts.length; j++) {
                    if (contacts[j] == subscriber) {
                        isContact = true;

                        break;
                    }
                }
            }

            if (isContact) {
                return true;
            }
        }

        AuthSubscriptionForm form = new AuthSubscriptionForm(session, subscriber);

        return form.approve();
    }

    /**
     *
     *
     */
    public synchronized Account createNewAccount(Account anAccount, String msg) {
        Account account = null;

        // Construct the wizard
        AccountWizardSettings settings = createWizardSettings(anAccount);
        settings.setMessage(msg);

        AccountWizardIterator wizardIterator = new AccountWizardIterator(settings);
        WizardDescriptor wizardDesc = new WizardDescriptor(wizardIterator, settings);
        settings.setWizardDescriptor(wizardDesc);
        initWizard(wizardDesc);

        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDesc);

        try {
            // Display the dialog
            dialog.setVisible(true);

            if (wizardDesc.getValue() == WizardDescriptor.OK_OPTION) {
                // Retrieve the new account
                account = settings.getAccount();

                // If the account is new, try to register it
                if (account.getAccountType() == Account.NEW_ACCOUNT) {
                    RequestProcessor.postRequest(new AsyncAccountRegistration(account));
                } else {
                    try {
                        // Simply record the account in the list
                        AccountManager.getDefault().addAccount(account);
                    } catch (IOException e) {
                        // Shouldn't happen
                        Debug.errorManager.notify(e);
                    }
                }
            }
        } finally {
            dialog.dispose();
        }

        return account;
    }

    /**
     *
     *
     */
    private AccountWizardSettings createWizardSettings(Account account) {
        if (account == null) {
            account = new Account();
        }

        return new AccountWizardSettings(account);
    }

    /**
         * Initializes wizard descriptor
         *
         */
    private void initWizard(WizardDescriptor wizardDesc) {
        wizardDesc.putProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, // NOI18N
            Boolean.TRUE
        );
        wizardDesc.putProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, // NOI18N
            Boolean.TRUE
        );
        wizardDesc.putProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, // NOI18N
            Boolean.TRUE
        );

        wizardDesc.setTitle(
            NbBundle.getMessage(DefaultUserInterface.class, "LBL_DefaultUserInterface_AccountWizardTitle")
        ); // NOI18N
        wizardDesc.setTitleFormat(new MessageFormat("{0} ({1})")); // NOI18N

        wizardDesc.setModal(true);
    }

    /**
     * Returns the default login information.  Note, this method should
     * not prompt the user for this information, and instead should return
     * the login information that was last used, set as default by the user,
     * etc.
     *
     */
    public Account getDefaultAccount() {
        Account[] accounts = AccountManager.getDefault().getAccounts();

        if (accounts == null) {
            return null;
        }

        String id = HiddenCollabSettings.getDefault().getDefaultAccountID();

        for (int i = 0; i < accounts.length; i++) {
            if (accounts[i].getInstanceID().equals(id)) {
                return accounts[i];
            }
        }

        return null;
    }

    /**
     * Sets the default login information.  Modules are cautioned not to use
     * this method to frivolously change this information.  Instead, this
     * method exists to provide cooperating modules with a standard mechanism
     * for setting the default information once it has been gathered in a
     * module-specific way.
     *
     */
    public void setDefaultAccount(Account value) {
        String id = null;

        if (value != null) {
            id = value.getInstanceID();
        }

        HiddenCollabSettings.getDefault().setDefaultAccountID(id);
    }

    /**
     *
     *
     */
    public boolean isAutoLoginAccount(Account account) {
        //		try
        //		{
        //			DataObject dataObject=
        //				AccountManager.getDefault().findAccountDataObject(account);
        //			if (dataObject!=null)
        //			{
        //				Boolean result=(Boolean)dataObject.getPrimaryFile().getAttribute(
        //					ATTRIBUTE_AUTO_LOGIN);
        //				return result!=null ? result.booleanValue() : false;
        //			}
        //		}
        //		catch (IOException e)
        //		{
        //			Debug.debugNotify(e);
        //		}
        //
        //		return false;
        return (account != null) && account.getAutoLogin();
    }

    /**
     *
     *
     */
    public void setAutoLoginAccount(Account account, boolean value) {
        //		try
        //		{
        //			DataObject dataObject=
        //				AccountManager.getDefault().findAccountDataObject(account);
        //			if (dataObject!=null)
        //			{
        //				// Set the attribute to true, or remove it if false
        //				dataObject.getPrimaryFile().setAttribute(ATTRIBUTE_AUTO_LOGIN,
        //					value ? Boolean.TRUE : null);
        //			}
        account.setAutoLogin(value);

        //		}
        //		catch (IOException e)
        //		{
        //			Debug.debugNotify(e);
        //		}
    }

    /**
     *
     *
     */
    public void notifyAccountRegistrationSucceeded(Account account) {
        // Show a confirmation message
        final String message = NbBundle.getMessage(
                DefaultUserInterface.class, "MSG_DefaultUserInterface_AccountRegistrationSuccess", // NOI18N
                account.getDisplayName()
            );

        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message));
                }
            }
        );
    }

    /**
     *
     *
     */
    public void notifyAccountPasswordChangeSucceeded(Account account) {
        // Show a confirmation message
        final String message = NbBundle.getMessage(
                DefaultUserInterface.class, "MSG_DefaultUserInterface_AccountPasswordChangeSuccess", // NOI18N
                account.getDisplayName()
            );
        Debug.out.println("message: " + message);
        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message));
                }
            }
        );
    }

    /**
     *
     *
     */
    public void notifyAccountUnRegistrationSucceeded(Account account) {
        // Show a confirmation message
        final String message = NbBundle.getMessage(
                DefaultUserInterface.class, "MSG_DefaultUserInterface_AccountUnRegistrationSuccess", // NOI18N
                account.getDisplayName()
            );

        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message));
                }
            }
        );
    }

    /**
     *
     *
     */
    public void notifyAccountRegistrationFailed(Account account, String failureMessage) {
        // Show a friendly error message
        final String message = NbBundle.getMessage(
                DefaultUserInterface.class, "MSG_DefaultUserInterface_AccountRegistrationError", // NOI18N
                failureMessage
            );

        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE)
                    );
                }
            }
        );
    }

    /**
     *
     *
     */
    public void notifyAccountUnRegistrationFailed(Account account, String reason) {
        // Show a friendly error message
        final String message = NbBundle.getMessage(
                DefaultUserInterface.class, "MSG_DefaultUserInterface_AccountUnregistrationError", // NOI18N
                reason
            );

        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE)
                    );
                }
            }
        );
    }

    /**
     *
     *
     */
    public void notifyAccountPasswordChangeFailed(Account account, String reason) {
        // Show a friendly error message
        final String message = NbBundle.getMessage(
                DefaultUserInterface.class, "MSG_DefaultUserInterface_AccountPasswordChangeError", // NOI18N
                reason
            );

        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    DialogDisplayer.getDefault().notify(
                        new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE)
                    );
                }
            }
        );
    }

    /**
     *
     *
     */
    public void notifySessionError(CollabSession session, String errorMessage) {
        final String message = NbBundle.getMessage(
                DefaultUserInterface.class, "MSG_DefaultUserInterface_SessionError", // NOI18N
                session.getUserPrincipal().getDisplayName(), errorMessage
            );

        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    NotifyDescriptor.Message descriptor = new NotifyDescriptor.Message(
                            message, NotifyDescriptor.ERROR_MESSAGE
                        );
                    DialogDisplayer.getDefault().notify(descriptor);
                }
            }
        );
    }

    /**
     * To notify user that subscription was denied, and as a consequence,
     * this contact will be removed from contact list
     */
    public void notifySubscriptionDenied(CollabPrincipal principal) {
        final String message = NbBundle.getMessage(
                DefaultUserInterface.class, "MSG_DefaultUserInterface_SubscriptionDenied", principal.getDisplayName(),
                principal.getDisplayName()
            );

        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    NotifyDescriptor.Message descriptor = new NotifyDescriptor.Message(
                            message, NotifyDescriptor.INFORMATION_MESSAGE
                        );
                    DialogDisplayer.getDefault().notify(descriptor);
                }
            }
        );
    }

    /*
     *
     *
     */
    public void notifyConversationEvent(Conversation conversation, int notificationType) {
        play(notificationType);
    }

    /*
     *
     *
     */
    public void notifyStatusChange(CollabSession session, CollabPrincipal principal, int notificationType) {
        play(notificationType);

        // Determine if visual notification should be used
        if (NotificationSettings.getDefault().getShowPresenceNotifications().booleanValue()) {
            // Show a notification
            CollabExplorerPanel.getInstance().showContactNotification(principal, notificationType);
        }
    }

    /*
     *
     *
     */
    public void notifyChannelEvent(Conversation conversation, Collablet channel, int notificationType) {
        // Do nothing currently
    }

    /**
     *
     *
     */
    public void notifyPublicConversationCreationSucceeded(Conversation conv) {
        // Show a confirmation message 
        final String message = NbBundle.getMessage(
                DefaultUserInterface.class, "MSG_DefaultUserInterface_PublicConversationCreationSuccess", // NOI18N 
                conv.getDisplayName()
            );

        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message));
                }
            }
        );
    }

    /**
     *
     *
     */
    public static void play(int type) {
        // Determine if audio notification should be used
        if (!NotificationSettings.getDefault().getPlayAudioNotifications().booleanValue()) {
            return;
        }

        URL url;

        switch (type) {
        case NOTIFY_CHAT_MESSAGE_SENT:
            url = DefaultUserInterface.class.getResource("/org/netbeans/modules/collab/ui/sound/send.wav");

            break;

        case NOTIFY_CHAT_MESSAGE_RECEIVED:
            url = DefaultUserInterface.class.getResource("/org/netbeans/modules/collab/ui/sound/receive.wav");

            break;

        case NOTIFY_USER_STATUS_AWAY:
            url = DefaultUserInterface.class.getResource("/org/netbeans/modules/collab/ui/sound/pop2.wav");

            break;

        case NOTIFY_USER_STATUS_BUSY:
            url = DefaultUserInterface.class.getResource("/org/netbeans/modules/collab/ui/sound/pop2.wav");

            break;

        case NOTIFY_USER_STATUS_IDLE:
            url = DefaultUserInterface.class.getResource("/org/netbeans/modules/collab/ui/sound/idle.wav");

            break;

        case NOTIFY_USER_STATUS_ONLINE:
            url = DefaultUserInterface.class.getResource("/org/netbeans/modules/collab/ui/sound/soundon.wav");

            break;

        case NOTIFY_USER_STATUS_OFFLINE:
            url = DefaultUserInterface.class.getResource("/org/netbeans/modules/collab/ui/sound/soundoff.wav");

            break;

        case NOTIFY_PARTICIPANT_JOINED:
            url = DefaultUserInterface.class.getResource("/org/netbeans/modules/collab/ui/sound/join.wav");

            break;

        case NOTIFY_PARTICIPANT_LEFT:
            url = DefaultUserInterface.class.getResource("/org/netbeans/modules/collab/ui/sound/left.wav");

            break;

        default:
            url = null;
        }

        if (url == null) {
            return;
        }

        AudioClip clip = Applet.newAudioClip(url);

        if (clip != null) {
            clip.play();
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    /**
     *
     *
     */
    public boolean createPublicConversation(CollabSession session) {
        boolean addedNewConversation = false;

        NotifyDescriptor descriptor = new NotifyDescriptor.InputLine(
                NbBundle.getMessage(DefaultUserInterface.class, "MSG_DefaultUserInterface_ConversationName"),
                NbBundle.getMessage(DefaultUserInterface.class, "MSG_DefaultUserInterface_CreateConversationTitle"),
                NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.PLAIN_MESSAGE
            );

        if (DialogDisplayer.getDefault().notify(descriptor) == NotifyDescriptor.OK_OPTION) {
            String conversationName = ((NotifyDescriptor.InputLine) descriptor).getInputText();

            // Check for null input
            if ((conversationName == null) || (conversationName.trim().length() == 0)) {
                final NotifyDescriptor descriptor2 = new NotifyDescriptor.Message(
                        NbBundle.getMessage(
                            DefaultUserInterface.class, "MSG_DefaultUserInterface_InvalidConversationName"
                        ), // NOI18N
                        NotifyDescriptor.WARNING_MESSAGE
                    );

                SwingUtilities.invokeLater(
                    new Runnable() {
                        public void run() {
                            DialogDisplayer.getDefault().notify(descriptor2);
                        }
                    }
                );

                return false;
            }

            // TEMP, check ascii value to prevent multibyte characters and special
            // characters in public conversation name
            //			if (!isValidJID(conversationName))
            //			{
            //				final NotifyDescriptor descriptor2=
            //					new NotifyDescriptor.Message(
            //						NbBundle.getMessage(DefaultUserInterface.class,
            //						"MSG_DefaultUserInterface_InvalidConversationName"), // NOI18N
            //						NotifyDescriptor.WARNING_MESSAGE);
            //
            //				SwingUtilities.invokeLater(
            //					new Runnable()
            //					{
            //						public void run()
            //						{
            //							DialogDisplayer.getDefault().notify(
            //								descriptor2);
            //						}
            //					});
            //
            //				return false;				
            //			}
            try {
                String[] conversations = session.findPublicConversations(CollabSession.SEARCHTYPE_STARTSWITH, conversationName);

                /* doesn't seem to be necessary anymore
                // workaround for #6180229
                if (session.findPrincipals(CollabSession.SEARCHTYPE_EQUALS, conversationName).length > 0) {
                    final NotifyDescriptor descriptor2 = new NotifyDescriptor.Message(
                            NbBundle.getMessage(
                                DefaultUserInterface.class, "MSG_DefaultUserInterface_" + "ConversationNameExists", // NOI18N
                                conversationName
                            ), NotifyDescriptor.WARNING_MESSAGE
                        );

                    SwingUtilities.invokeLater(
                        new Runnable() {
                            public void run() {
                                DialogDisplayer.getDefault().notify(descriptor2);
                            }
                        }
                    );

                    return false;
                }
                */

                if ((conversations != null) && (conversations.length > 0)) {
                    for (int i = 0; i < conversations.length; i++) {
                        String name = conversations[i].substring(0, conversations[i].indexOf("@"));

                        if (name.equals(conversationName)) {
                            final NotifyDescriptor descriptor2 = new NotifyDescriptor.Message(
                                    NbBundle.getMessage(
                                        DefaultUserInterface.class,
                                        "MSG_DefaultUserInterface_" + "ConversationNameExists", // NOI18N
                                        conversationName
                                    ), NotifyDescriptor.WARNING_MESSAGE
                                );

                            SwingUtilities.invokeLater(
                                new Runnable() {
                                    public void run() {
                                        DialogDisplayer.getDefault().notify(descriptor2);
                                    }
                                }
                            );

                            return false;
                        }
                    }
                }

                Conversation conv = session.createPublicConversation(conversationName);

                if (conv == null) {
                    final NotifyDescriptor descriptor2 = new NotifyDescriptor.Message(
                            NbBundle.getMessage(
                                DefaultUserInterface.class, "MSG_DefaultUserInterface_" + "ConversationNameExists", // NOI18N
                                conversationName
                            ), NotifyDescriptor.WARNING_MESSAGE
                        );

                    SwingUtilities.invokeLater(
                        new Runnable() {
                            public void run() {
                                DialogDisplayer.getDefault().notify(descriptor2);
                            }
                        }
                    );

                    return false;
                }

                //Fix for bug # 6280758                                
                ManageThread thread = new ManageThread(session, conversationName, conv);
                thread.start();

                //If there are errors in managing conversation, we still
                //open the conversation
                //				managePublicConversation(session, conversationName);
                //				if(conv!=null) 
                //				{ 
                //					conv.leave();
                //					conv=session.createPublicConversation(conversationName);
                //				}
                //				CollabManager.getDefault().getUserInterface()
                //					.showConversation(conv);
                addedNewConversation = true;
            } catch (CollabException e) {
                Debug.errorManager.notify(e);
            }
        }

        return addedNewConversation;
    }

    /**
     *
     *
     */
    public void subscribePublicConversation(CollabSession session) {
        AddConversationForm form = new AddConversationForm(session);
        form.addConversation();
    }

    /**
     *
     *
     */
    public void notifyInvitationDeclined(String destination, String msg) {
        String message = NbBundle.getMessage(
                DefaultUserInterface.class, "MSG_DefaultUserInterface_InvitationDeclined", // NOI18N
                destination
            );

        final NotifyDescriptor.Message descriptor = new NotifyDescriptor.Message(
                message, NotifyDescriptor.INFORMATION_MESSAGE
            );

        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    DialogDisplayer.getDefault().notify(descriptor);
                }
            }
        );
    }

    /**
     *
     *
     */
    public void managePublicConversation(CollabSession session, final String conversationName) {
        try {
            //Fix for bug#6239787, show warning if #of participants>0  
            Conversation[] conversations = session.getConversations();
            Conversation conversation = null;

            Collection convUsers = session.getParticipantsFromPublicConference(conversationName); 
            if ((convUsers != null && convUsers.size() > 0)) { //conv users are enough 
                NotifyDescriptor descriptor=new NotifyDescriptor.Confirmation(  
                    NbBundle.getMessage(DefaultUserInterface.class,  
                        "MSG_DefaultUserInterface_ManagePublicConversation_Warning",   
                        conversationName, new Integer(convUsers.size()).toString()),  
                    NotifyDescriptor.OK_CANCEL_OPTION);
                if (DialogDisplayer.getDefault().notify(descriptor) != NotifyDescriptor.OK_OPTION) {
                    return;
                }
            }

            // Get the list of privileges
            ConversationPrivilege[] privileges = session.getPublicConversationPrivileges(conversationName);

            // Create and show the management form
            ManagePublicConversationForm form = new ManagePublicConversationForm(session, conversationName, privileges);
            DialogDescriptor descriptor = new DialogDescriptor(
                    form,
                    NbBundle.getMessage(
                        DefaultUserInterface.class, "TITLE_ManagePublicConversationForm", conversationName
                    )
                );
            Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);

            try {
                dialog.setVisible(true);
            } finally {
                dialog.dispose();
            }

            // Set the privileges on the conversation
            if (descriptor.getValue() == DialogDescriptor.OK_OPTION) {
                session.setPublicConversationDefaultPrivilege(conversationName, form.getDefaultPrivilege());
                session.setPublicConversationPrivileges(conversationName, form.getPrivileges());
            }

            //hack for bug#6239787, join and leave conv after manage 
            //			Conversation conv = 
            //				session.createPublicConversation(conversationName); 
            //			if(conv!=null) 
            //			{ 
            //				conv.leave(); 
            //			}			
        } catch (final CollabException e) {
            SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        DialogDisplayer.getDefault().notify(
                            new NotifyDescriptor.Message(
                                NbBundle.getMessage(
                                    DefaultUserInterface.class, "MSG_DefaultUserInterface_" +
                                    "ManageConversationError", // NOI18N
                                    conversationName, e.getMessage()
                                ), NotifyDescriptor.ERROR_MESSAGE
                            )
                        );
                    }
                }
            );

            Debug.logDebugException("Exception managing public conversation", e, true); // NOI18N
        }
    }

    /**
     *
     *
     */
    public void inviteUsers(Conversation conversation) {
        ParticipantSearchForm form = new ParticipantSearchForm(conversation);
        form.inviteToConversation();
    }

    /**
     *
     *
     */
    public void notifyPublicConversationDeleted(String name) {
        String message = NbBundle.getMessage(
                DefaultUserInterface.class, "MSG_DefaultUserInterface_PublicConversationDeleted", // NOI18N
                name
            );

        final NotifyDescriptor.Message descriptor = new NotifyDescriptor.Message(
                message, NotifyDescriptor.INFORMATION_MESSAGE
            );

        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    DialogDisplayer.getDefault().notify(descriptor);
                }
            }
        );
    }

    ////////////////////////////////////////////////////////////////////////////
    // Helper methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    public static boolean isWindowsLF() {
        String id = UIManager.getLookAndFeel().getID();

        return id.equals("Windows"); // NOI18N
    }

    /**
     *
     *
     */
    public static boolean isXPLF() {
        if (!isWindowsLF()) {
            return false;
        }

        Boolean isXP = (Boolean) Toolkit.getDefaultToolkit().getDesktopProperty("win.xpstyle.themeActive"); // NOI18N

        return (isXP == null) ? false : isXP.booleanValue();
    }

    /**
     *
     *
     */
    public static boolean isValidJID(String name) {
        byte[] b = name.getBytes();

        for (int i = 0; i < b.length; i++) {
            if (
                ((48 <= b[i]) && (b[i] <= 57)) || ((65 <= b[i]) && (b[i] <= 90)) || ((97 <= b[i]) && (b[i] <= 122)) ||
                    (b[i] == '_') || (b[i] == '-') || (b[i] == '.')
            ) {
                continue;
            } else {
                return false;
            }
        }

        return true;
    }

    //	/**
    //	 *
    //	 *
    //	 */
    //	public String getEncryptedLicenseKey() 
    //	{
    //		String productToken = LMUtil.getSerialNumber(LMConstants.LICENSE_TOKEN);	
    //		String encodeStr="";
    //		
    //		if(productToken!=null && !productToken.trim().equals(""))
    //		{
    ////			SerialNumber sn = new SerialNumber(productToken);
    ////			if (sn.isTrial())
    ////				productToken = LMConstants.LICENSE_TOKEN;
    ////			
    ////			Debug.out.println("productToken: "+productToken);
    //			
    //			try
    //			{
    //				encodeStr=CryptoUtil.encrypt(productToken);
    ////				Debug.out.println("Base64 encoded productToken: "+encodeStr);
    //			}
    //			catch(Exception e)
    //			{
    //				Debug.out.println(e.getMessage());
    //			}		
    //		}
    //		return encodeStr;
    //	}	

    private String replaceAll(String origStr, String targetStr, String replacedStr) {
        StringBuffer sb = new StringBuffer(origStr);
        int idx;
        while ((idx = sb.indexOf(targetStr)) != -1) {
            sb.replace(idx, idx+targetStr.length(), replacedStr);
        }
        return sb.toString();
    }


    /**
     *
     *
     */
    public void manageAccounts(Account selectedAccount) {
        final String addOption = NbBundle.getMessage(DefaultUserInterface.class, "LBL_DefaultUserInterface_addOption"); // NOI18N
        final String deleteOption = NbBundle.getMessage(
                DefaultUserInterface.class, "LBL_DefaultUserInterface_deleteOption"
            ); // NOI18N
        final String setAsDefaultOption = NbBundle.getMessage(
                DefaultUserInterface.class, "LBL_DefaultUserInterface_setAsDefaultOption"
            ); // NOI18N
        String closeOption = NbBundle.getMessage(DefaultUserInterface.class, "LBL_DefaultUserInterface_closeOption"); // NOI18N

        final AccountManagementPanel panel = new AccountManagementPanel(selectedAccount);

        final DialogDescriptor descriptor = new DialogDescriptor(
                panel, NbBundle.getMessage(DefaultUserInterface.class, "LBL_DefaultUserInterface_AccountMangement"), // NOI18N
                true,
                new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        String add = replaceAll(addOption, "&", "");
                        String delete = replaceAll(deleteOption, "&", "");
                        String setAsDefault = replaceAll(setAsDefaultOption, "&", "");

                        if (event.getActionCommand().equals(add)) {
                            createNewAccount(null, null);
                        } else if (event.getActionCommand().equals(delete)) {
                            Account[] accounts = panel.getSelectedAccounts();

                            if (accounts.length == 0) {
                                return;
                            }

                            DeleteAccountConfirmationPanel confirmPanel = new DeleteAccountConfirmationPanel(
                                    accounts[0]
                                );

                            DialogDescriptor descriptor = new DialogDescriptor(
                                    confirmPanel,
                                    NbBundle.getMessage(
                                        DefaultUserInterface.class, "MSG_DefaultUserInterface_ConfirmDeleteAccounts", // NOI18N 
                                        accounts[0].getDisplayName()
                                    ), true, NotifyDescriptor.YES_NO_OPTION, null, null
                                );
                            descriptor.setMessageType(DialogDescriptor.QUESTION_MESSAGE);
                            descriptor.setOptionsAlign(DialogDescriptor.BOTTOM_ALIGN);

                            Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
                            dialog.setVisible(true);

                            if (descriptor.getValue() == DialogDescriptor.YES_OPTION) {
                                for (int i = 0; i < accounts.length; i++) {
                                    deleteAccount(accounts[i], confirmPanel.deleteServerAccount());
                                }
                            }
                        } else if (event.getActionCommand().equals(setAsDefault)) {
                            Account[] accounts = panel.getSelectedAccounts();

                            if (accounts.length == 0) {
                                return;
                            }

                            CollabManager.getDefault().getUserInterface().setDefaultAccount(accounts[0]);
                        }
                    }
                }
            );

        panel.addPropertyChangeListener(
            new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent event) {
                    if (event.getPropertyName().equals("validity")) {
                        descriptor.setValid(((Boolean) (event.getNewValue())).booleanValue());
                    }
                }
            }
        );

        descriptor.setOptions(new Object[] { addOption, deleteOption, setAsDefaultOption, closeOption });
        descriptor.setClosingOptions(new Object[] { closeOption });

        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);

        dialog.setVisible(true);
    }

    /**
     *
     *
     */
    private void deleteAccount(Account account, boolean deleteFromServer) {
        try {
            if (!deleteFromServer) {
                AccountManager.getDefault().removeAccount(account);

                return;
            }

            CollabManager manager = CollabManager.getDefault();
            assert manager != null : "Could not find default CollabManager"; // NOI18N

            try {
                manager.unregisterUser(account);
            } catch (IOException e) {
                e.printStackTrace();

                //				Debug.debugNotify(e);
                // Show the user a friendly error
                String message = NbBundle.getMessage(
                        LoginAccountPanel.class, "MSG_LoginAccountPanel_CannotConnect", // NOI18N
                        account.getServer(), e.toString()
                    );
                final NotifyDescriptor.Message descriptor = new NotifyDescriptor.Message(
                        message, NotifyDescriptor.WARNING_MESSAGE
                    );

                SwingUtilities.invokeLater(
                    new Runnable() {
                        public void run() {
                            DialogDisplayer.getDefault().notify(descriptor);
                        }
                    }
                );
            } catch (SecurityException e) {
                e.printStackTrace();

                //				Debug.debugNotify(e);
                // Show the user a friendly error
                String message = NbBundle.getMessage(
                        LoginAccountPanel.class, "MSG_LoginAccountPanel_AuthFailed", // NOI18N
                        account.toString()
                    );
                final NotifyDescriptor.Message descriptor = new NotifyDescriptor.Message(
                        message, NotifyDescriptor.WARNING_MESSAGE
                    );

                SwingUtilities.invokeLater(
                    new Runnable() {
                        public void run() {
                            DialogDisplayer.getDefault().notify(descriptor);
                        }
                    }
                );
            } catch (CollabException e) {
                e.printStackTrace();

                Throwable exception = e;

                if (exception.getCause() instanceof java.net.ConnectException) {
                    exception = exception.getCause();
                }

                //				Debug.debugNotify(e);
                //				if (e!=exception)
                //					Debug.debugNotify(exception);
                // Show the user a friendly error
                String message = NbBundle.getMessage(
                        LoginAccountPanel.class, "MSG_LoginAccountPanel_UnknownError", // NOI18N
                        exception.getMessage()
                    );
                final NotifyDescriptor.Message descriptor = new NotifyDescriptor.Message(
                        message, NotifyDescriptor.ERROR_MESSAGE
                    );

                SwingUtilities.invokeLater(
                    new Runnable() {
                        public void run() {
                            DialogDisplayer.getDefault().notify(descriptor);
                        }
                    }
                );
            }
        } catch (Exception e) {
            String message = NbBundle.getMessage(
                    LoginAccountPanel.class, "MSG_DefaultUserInterface_ErrorDeleteAccount", // NOI18N
                    account, e.getMessage()
                );
            final NotifyDescriptor.Message descriptor = new NotifyDescriptor.Message(
                    message, NotifyDescriptor.WARNING_MESSAGE
                );

            SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        DialogDisplayer.getDefault().notify(descriptor);
                    }
                }
            );
        }
    }

    /**
     *
     *
     */
    public void changeUI(int change) {
        switch (change) {
            case SHOW_COLLAB_SESSION_PANEL:
                CollabExplorerPanel.getInstance().showComponent(CollabExplorerPanel.COMPONENT_EXPLORER);
                break;
            default:
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    protected class AsyncAccountRegistration extends Object implements Runnable {
        private Account account;

        /**
         *
         *
         */
        public AsyncAccountRegistration(Account account) {
            super();
            this.account = account;
        }

        /**
         *
         *
         */
        public void run() {
            try {
                CollabManager.getDefault().registerUser(account);

                /*
                try
                {
                        // Simply record the account in the list
                        AccountManager.getDefault().addAccount(account);
                }
                catch (IOException e)
                {
                        // Shouldn't happen
                        Debug.errorManager.notify(e);
                }
                 */
            } catch (UnknownHostException e) {
                // Could not resolve server
                e.printStackTrace();
                e.printStackTrace(Debug.out);

                //				notifyAccountRegistrationFailed(account,
                //					NbBundle.getMessage(DefaultUserInterface.class,
                //						"MSG_DefaultUserInterface_"+ // NOI18N
                //							"InvalidRegistrationServer", // NOI18N
                //						account.getServer()));
                String failureMessage = NbBundle.getMessage(
                        DefaultUserInterface.class, "MSG_DefaultUserInterface_" + // NOI18N
                        "InvalidRegistrationServer", // NOI18N
                        account.getServer()
                    );
                final String message = NbBundle.getMessage(
                        DefaultUserInterface.class, "MSG_DefaultUserInterface_AccountRegistrationError", // NOI18N
                        failureMessage
                    );
                Debug.out.println(" error message : " + message);
                createNewAccount(account, message);
            } catch (IOException e) {
                // Could not contact server
                e.printStackTrace();
                e.printStackTrace(Debug.out);

                //				notifyAccountRegistrationFailed(account,
                //					NbBundle.getMessage(DefaultUserInterface.class,
                //						"MSG_DefaultUserInterface_"+ // NOI18N
                //							"RegistrationServerUnreachable", // NOI18N
                //						account.getServer()));
                String failureMessage = NbBundle.getMessage(
                        DefaultUserInterface.class,
                        "MSG_DefaultUserInterface_" + // NOI18N
                        "RegistrationServerUnreachable", // NOI18N
                        account.getServer()
                    );
                final String message = NbBundle.getMessage(
                        DefaultUserInterface.class, "MSG_DefaultUserInterface_AccountRegistrationError", // NOI18N
                        failureMessage
                    );
                createNewAccount(account, message);
            } catch (Exception e) {
                e.printStackTrace();
                e.printStackTrace(Debug.out);

                //				notifyAccountRegistrationFailed(account,e.getMessage());
                final String message = NbBundle.getMessage(
                        DefaultUserInterface.class, "MSG_DefaultUserInterface_AccountRegistrationError", // NOI18N
                        e.getMessage()
                    );
                createNewAccount(account, message);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Runs in non-AWT thread
     *
     */
    protected class LoginTask extends Object implements Runnable {
        private Account account;
        private String password;
        private Runnable successTask;
        private Runnable failureTask;

        /**
         *
         *
         */
        public LoginTask(Account account, String password, Runnable successTask, Runnable failureTask) {
            super();
            this.account = account;
            this.password = password;
            this.successTask = successTask;
            this.failureTask = failureTask;
        }

        /**
         *
         *
         */
        public void run() {
            try {
                CollabManager manager = CollabManager.getDefault();
                assert manager != null : "Could not find default CollabManager"; // NOI18N

                // TAF: 5-24-2005
                // Added the following statements to eliminate the "No
                // active sessions" bug.  Although I can't reproduce the
                // problem under normal conditions, I can force it to occur
                // by reinstalling the JIM module and forcing a new 
                // CollabManager to be created.  When that occurs, there is
                // no notification sent to objects that are listening to the
                // existing CollabManager, like RootNode and its Children
                // object.  Therefore, those objects remain unable to receive
                // events from the new CollabManager and do not respond to
                // login/session changes.  This forces the listener 
                // relationship to be reestablished before trying to login
                // in case something has happened to the CollabManager since 
                // this object was created.  I don't know if this will help
                // with the problem "in the wild", but it at least addresses
                // the issue when reinstalling the JIM module, which exhibits
                // the same behavior.
                RootNode rootNode = CollabExplorerPanel.getInstance().getRootNode();

                if (rootNode != null) {
                    rootNode.bug_5071137_workaround();
                }

                // Create the session
                manager.createSession(account, password);

                if (successTask != null) {
                    SwingUtilities.invokeLater(successTask);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Debug.debugNotify(e);

                // Show the user a friendly error
                String message = NbBundle.getMessage(
                        LoginAccountPanel.class, "MSG_LoginAccountPanel_CannotConnect", // NOI18N
                        account.getServer(), e.toString()
                    );
                final NotifyDescriptor.Message descriptor = new NotifyDescriptor.Message(
                        message, NotifyDescriptor.WARNING_MESSAGE
                    );

                SwingUtilities.invokeLater(
                    new Runnable() {
                        public void run() {
                            DialogDisplayer.getDefault().notify(descriptor);
                        }
                    }
                );

                if (failureTask != null) {
                    SwingUtilities.invokeLater(failureTask);
                }
            } catch (SecurityException e) {
                e.printStackTrace();
                Debug.debugNotify(e);

                // Show the user a friendly error
                String message = NbBundle.getMessage(
                        LoginAccountPanel.class, "MSG_LoginAccountPanel_AuthFailed", // NOI18N
                        account.toString()
                    );
                final NotifyDescriptor.Message descriptor = new NotifyDescriptor.Message(
                        message, NotifyDescriptor.WARNING_MESSAGE
                    );

                SwingUtilities.invokeLater(
                    new Runnable() {
                        public void run() {
                            DialogDisplayer.getDefault().notify(descriptor);
                        }
                    }
                );

                if (failureTask != null) {
                    SwingUtilities.invokeLater(failureTask);
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
                Debug.debugNotify(e);

                // Show the user a friendly error
                String message = NbBundle.getMessage(
                        LoginAccountPanel.class, "MSG_LoginAccountPanel_AlreadyLoggedIn", // NOI18N
                        account.getUserName()
                    );
                final NotifyDescriptor.Message descriptor = new NotifyDescriptor.Message(
                        message, NotifyDescriptor.ERROR_MESSAGE
                    );

                SwingUtilities.invokeLater(
                    new Runnable() {
                        public void run() {
                            DialogDisplayer.getDefault().notify(descriptor);
                        }
                    }
                );

                if (failureTask != null) {
                    SwingUtilities.invokeLater(failureTask);
                }
            } catch (CollabException e) {
                e.printStackTrace();

                Throwable exception = e;

                if (exception.getCause() instanceof java.net.ConnectException) {
                    exception = exception.getCause();
                }

                Debug.debugNotify(e);

                if (e != exception) {
                    Debug.debugNotify(exception);
                }

                // Show the user a friendly error
                String message = NbBundle.getMessage(
                        LoginAccountPanel.class, "MSG_LoginAccountPanel_UnknownError", // NOI18N
                        exception.getMessage()
                    );
                final NotifyDescriptor.Message descriptor = new NotifyDescriptor.Message(
                        message, NotifyDescriptor.ERROR_MESSAGE
                    );

                SwingUtilities.invokeLater(
                    new Runnable() {
                        public void run() {
                            DialogDisplayer.getDefault().notify(descriptor);
                        }
                    }
                );

                if (failureTask != null) {
                    SwingUtilities.invokeLater(failureTask);
                }
            }
        }
    }

    /* author Smitha Krishna Nagesh
     * Fix for bug# 6280758
     */
    protected class ManageThread extends Thread {
        CollabSession session;
        String conversationName;
        Conversation conv;

        public ManageThread(CollabSession session, final String conversationName, Conversation conv) {
            this.session = session;
            this.conversationName = conversationName;
            this.conv = conv;
        }

        public void run() {
            try {
                sleep(2000);
                conv.leave();
                managePublicConversation(session, conv.getIdentifier());
                conv = session.createPublicConversation(conversationName);

                CollabManager.getDefault().getUserInterface().showConversation(conv);
            } catch (Exception e) {
                ErrorManager.getDefault().notify(e);
            }
        }
    }
}
