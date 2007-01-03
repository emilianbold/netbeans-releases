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
package org.netbeans.modules.collab.ui;

import java.awt.event.*;
import java.beans.*;
import java.util.*;
import javax.swing.SwingUtilities;
import org.openide.awt.StatusDisplayer;
import org.openide.modules.ModuleInstall;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;
import com.sun.collablet.*;
import java.awt.EventQueue;
import org.netbeans.modules.collab.core.Debug;
import org.netbeans.modules.collab.ui.options.CollabSettings;

/**
 *
 * @author        Todd Fast, todd.fast@sun.com
 */
public class Install extends ModuleInstall {

    public Install() {
        super();
    }

    public void uninstalled() {
        try {
            if (CollabManager.getDefault() != null) {
                UserInterface ui = CollabManager.getDefault().getUserInterface();

                if (ui == DefaultUserInterface.getDefault()) {
                    CollabManager.getDefault().setUserInterface(null);
                }
            }
        } catch (Exception e) {
            Debug.debugNotify(e);
        }
    }

    public void restored() {
        // Add a shutdown hook to log off all sessions
        //		shutdownHook=new ShutdownTask();
        //		Runtime.getRuntime().addShutdownHook(shutdownHook);
        // Auto-login when the main IDE window shows
        WindowManager.getDefault().invokeWhenUIReady(new AutoLoginJob());
    }

    public boolean closing() {
        final CollabManager manager = CollabManager.getDefault();

        if (manager != null) {
            manager.invalidate();
        }

        return true;
    }

    public void close() {
        // No need to run hook if we're shutting down gracefully
        //		if (shutdownHook!=null)
        //			Runtime.getRuntime().removeShutdownHook(shutdownHook);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Utility methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    private static Account[] getAutoLoginAccounts() {
        java.util.List result = new ArrayList();

        final CollabManager manager = CollabManager.getDefault();

        if (manager != null) {
            UserInterface ui = manager.getUserInterface();

            Account[] accounts = AccountManager.getDefault().getAccounts();

            for (int i = 0; i < accounts.length; i++) {
                Account account = accounts[i];

                if (ui.isAutoLoginAccount(account) && (manager.getSession(account) == null)) {
                    result.add(account);
                }
            }
        }

        return (Account[]) result.toArray(new Account[result.size()]);
    }

    /**
     */
    protected static class AutoLoginJob implements Runnable {
        
        public void run() {
            // Note, we make the check for auto-login here because we 
            // effectively assured the settings objects will have been 
            // deserialized by this point
            if (
                (CollabSettings.getDefault().getAutoLogin() != null) &&
                    CollabSettings.getDefault().getAutoLogin().booleanValue()
            ) {
                Account[] accounts = getAutoLoginAccounts();

                if (accounts.length > 0) {
                    // Lock up front so the user doesn't try to login.  We
                    // undo this lock in the LoginTask.
                    CollabExplorerPanel.getInstance().getLoginAccountPanel().lock(
                        NbBundle.getMessage(Install.class, "MSG_Install_AutoLoginStatus")
                    ); // NOI18N

                    // Let the system settle down a little before 
                    // logging in
                    javax.swing.Timer timer = new javax.swing.Timer(
                            5000,
                            new ActionListener() {
                                public void actionPerformed(ActionEvent event) {
                                    SwingUtilities.invokeLater(new LoginTask());
                                }
                            }
                        );
                    timer.setRepeats(false);
                    timer.start();
                }
            }

        }
    }

    /**
     * Runs in AWT thread
     *
     */
    protected static class LoginTask extends Object implements Runnable {
        /**
         *
         *
         */
        public void run() {
            StatusDisplayer.getDefault().setStatusText(
                NbBundle.getMessage(Install.class, "MSG_Install_AutoLoginStatus")
            ); // NOI18N

            final CollabManager manager = CollabManager.getDefault();

            if (manager != null) {
                UserInterface ui = manager.getUserInterface();

                Account[] accounts = getAutoLoginAccounts();
                boolean shownExplorer = false;

                for (int i = 0; i < accounts.length; i++) {
                    if (!shownExplorer) {
                        // If we don't do this here, then the session nodes 
                        // will not be expanded once we log in
                        CollabExplorerPanel.getInstance().open();

                        // Unlock (briefly) to balance the immediate lock
                        // we placed when the IDE started
                        CollabExplorerPanel.getInstance().getLoginAccountPanel().unlock();

                        shownExplorer = true;
                    }

                    // Lock once for each account
                    CollabExplorerPanel.getInstance().getLoginAccountPanel().lock(
                        NbBundle.getMessage(Install.class, "MSG_Install_AutoLoginStatus")
                    ); // NOI18N

                    // Use the stored password to login.  Note, the 
                    // following call is asynchronous.
                    ui.login(accounts[i], accounts[i].getPassword(), new PostLoginTask(true), new PostLoginTask(false));
                }
            }
        }
    }

    /** Updates status and shows collab UI if logged in.
     */
    protected static class PostLoginTask extends Object implements Runnable {
        
        private boolean logged;
        
        PostLoginTask(boolean status) {
            logged= status;
        }
        
        public void run() {
            assert EventQueue.isDispatchThread();
            
            CollabExplorerPanel.getInstance().getLoginAccountPanel().unlock();

            if (logged) {
                StatusDisplayer.getDefault().setStatusText(
                    NbBundle.getMessage(Install.class, "MSG_Install_AutoLoginSuccess")
                ); // NOI18N

                // Show the collab explorer
                CollabExplorerPanel.getInstance().showComponent(CollabExplorerPanel.COMPONENT_EXPLORER);
            }
            else {
                StatusDisplayer.getDefault().setStatusText(
                        NbBundle.getMessage(Install.class, "MSG_Install_AutoLoginFailure")
                        ); // NOI18N
            }
        }
    }
}
