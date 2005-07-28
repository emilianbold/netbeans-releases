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
package org.netbeans.modules.collab.ui;

import com.sun.collablet.Account;
import com.sun.collablet.AccountManager;
import com.sun.collablet.CollabManager;
import com.sun.collablet.UserInterface;

import org.openide.*;
import org.openide.awt.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.modules.*;
import org.openide.util.*;
import org.openide.util.actions.*;
import org.openide.windows.*;

import java.awt.*;
import java.awt.event.*;

import java.beans.*;

import java.io.*;

import java.util.*;

import javax.swing.*;

import org.netbeans.modules.collab.*;
import org.netbeans.modules.collab.core.Debug;
import org.netbeans.modules.collab.ui.options.CollabSettings;


/**
 *
 *
 * @author        Todd Fast, todd.fast@sun.com
 */
public class Install extends ModuleInstall {
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////
    private static final long serialVersionUID = 1L; // DO NOT CHANGE!

    /**
     *
     *
     */
    public Install() {
        super();
    }

    /**
     *
     *
     */
    public void validate() {
        // Do nothing
    }

    /**
     *
     *
     */
    public void installed() {
        Introspector.flushCaches();
    }

    /**
     *
     *
     */
    public void updated(int release, String specVersion) {
        // Do nothing
        Introspector.flushCaches();
    }

    /**
     *
     *
     */
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

    /**
     *
     *
     */
    public void restored() {
        // Add a shutdown hook to log off all sessions
        //		shutdownHook=new ShutdownTask();
        //		Runtime.getRuntime().addShutdownHook(shutdownHook);
        // Auto-login when the main IDE window shows
        SwingUtilities.invokeLater(
            new Runnable() {
                public void run() {
                    Frame frame = WindowManager.getDefault().getMainWindow();

                    if (frame != null) {
                        frame.addWindowListener(new AutoLoginWindowListener());
                    }
                }
            }
        );
    }

    /**
     *
     *
     */
    public boolean closing() {
        final CollabManager manager = CollabManager.getDefault();

        if (manager != null) {
            //			if (CollabSettings.getDefault().getAutoLogin()!=null &&
            //				CollabSettings.getDefault().getAutoLogin().booleanValue())
            //			{
            //				// Reset all accounts
            //				Account[] autoLoginAccounts=getAutoLoginAccounts();
            //				for (int i=0; i<autoLoginAccounts.length; i++)
            //				{
            //					manager.getUserInterface().setAutoLoginAccount(
            //						autoLoginAccounts[i],false);
            //				}
            //
            //				// Remember the logged in accounts and make then auto-login
            //				CollabSession[] sessions=manager.getSessions();
            //				for (int i=0; i<sessions.length; i++)
            //				{
            //					Account account=sessions[i].getAccount();
            //					if (account.isValid())
            //					{
            //						manager.getUserInterface().setAutoLoginAccount(
            //							account,true);
            //					}
            //				}
            //			}
            // Log off all sessions.  If we don't do this, then the server will
            // show people as still logged on due to a bug in IM server.
            manager.invalidate();
        }

        return true;
    }

    /**
     *
     *
     */
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

    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    protected static class ShutdownTask extends Thread implements Runnable {
        /**
         *
         *
         */
        public void run() {
            try {
                System.out.println("Running collaboration " + // NOI18N
                    "shutdown hook on abnormal module shutdown"
                ); // NOI18N

                // Log off all sessions.  If we don't do this, then the 
                // server will show people as still logged on due to a 
                // bug in IM server.
                final CollabManager manager = CollabManager.getDefault();

                if (manager != null) {
                    System.out.println("Invalidating collaboration manager..."); // NOI18N

                    manager.invalidate();

                    System.out.println("Collaboration manager " + // NOI18N
                        "invalidated successfully"
                    ); // NOI18N
                } else {
                    System.out.println("No collaboration manager found"); // NOI18N
                }
            } catch (Throwable e) {
                System.out.println("Exception invalidating collaboration manager"); // NOI18N
                e.printStackTrace(System.out);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    protected static class AutoLoginWindowListener extends WindowAdapter {
        /**
         *
         *
         */
        public void windowOpened(WindowEvent event) {
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

            WindowManager.getDefault().getMainWindow().removeWindowListener(this);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////

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
                    ui.login(accounts[i], accounts[i].getPassword(), new LoginSuccessTask(), new LoginFailureTask());
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Runs in AWT thread
     *
     */
    protected static class LoginSuccessTask extends Object implements Runnable {
        /**
         *
         *
         */
        public void run() {
            CollabExplorerPanel.getInstance().getLoginAccountPanel().unlock();

            StatusDisplayer.getDefault().setStatusText(
                NbBundle.getMessage(Install.class, "MSG_Install_AutoLoginSuccess")
            ); // NOI18N

            // Show the collab explorer
            CollabExplorerPanel.getInstance().showComponent(CollabExplorerPanel.COMPONENT_EXPLORER);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Inner class
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Runs in AWT thread
     *
     */
    protected static class LoginFailureTask extends Object implements Runnable {
        /**
         *
         *
         */
        public void run() {
            CollabExplorerPanel.getInstance().getLoginAccountPanel().unlock();

            StatusDisplayer.getDefault().setStatusText(
                NbBundle.getMessage(Install.class, "MSG_Install_AutoLoginFailure")
            ); // NOI18N
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    //	private Thread shutdownHook;
}
