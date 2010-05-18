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

import java.beans.*;

import java.io.*;


/**
 *
 *
 * @author  Todd Fast <todd.fast@sun.com>
 */
public abstract class CollabManager extends Object {
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////
    private static Locator LOCATOR;
    public static final String PROP_SESSIONS = "sessions";
    public static final String PROP_USER_INTERFACE = "userInterface";

    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private UserInterface userInterface;
    private boolean installedDefaultUI;
    protected final Object USER_INTERFACE_LOCK = new Object();
    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    /**
     * Default constructor
     *
     */
    protected CollabManager() {
        super();
    }

    /**
     * Returns the user interface currently being used to interact with
     * the user
     *
     */
    public UserInterface getUserInterface() {
        synchronized (USER_INTERFACE_LOCK) {
            // If we previously installed a default UI and the default UI has 
            // since changed, clear the current UI and install the current one
            if (
                (userInterface != null) && installedDefaultUI && (UserInterface.getDefault() != null) &&
                    (UserInterface.getDefault() != userInterface)
            ) {
                userInterface = null;
            }

            if (userInterface == null) {
                // Install the default user interface
                userInterface = createDefaultUserInterface();
                installedDefaultUI = true;
                getChangeSupport().firePropertyChange(PROP_USER_INTERFACE, null, userInterface);
            }

            return userInterface;
        }
    }

    /**
     * Returns a default user interface
     *
     * @return        Must not be null
     */
    protected UserInterface createDefaultUserInterface() {
        // Install the default user interface
        UserInterface result = UserInterface.getDefault();

        if (result != null) {
            // Initialize the UI object
            try {
                result.initialize(this);
            } catch (Exception e) {
                Throwable ex = new CollabException(e, "Could not initialize user interface");
                ex.printStackTrace();
            }
        }

        // If there was a problem, or a UI wasn't found, install a no-op 
        // user interface
        if (result == null) {
            final String message = "WARNING: Installing no-op " + // NOI18N
                "collaboration user interface"; // NOI18N
            System.err.println(message);

            result = new NoOpUserInterface();
        }

        return result;
    }

    /**
     * Sets the user interface to use to interact with the user
     *
     */
    public void setUserInterface(UserInterface value) {
        synchronized (USER_INTERFACE_LOCK) {
            if (userInterface == value) {
                return;
            }

            // De-initialize the current user interface
            if (userInterface != null) {
                userInterface.deinitialize();
            }

            UserInterface oldValue = userInterface;
            userInterface = value;
            getChangeSupport().firePropertyChange(PROP_USER_INTERFACE, oldValue, userInterface);
        }
    }

    /**
     * Register a new user
     *
     */
    public abstract void registerUser(Account account)
    throws IOException, CollabException;

    /**
     *
     *
     */
    public abstract void unregisterUser(Account account)
    throws IOException, SecurityException, CollabException;

    ////////////////////////////////////////////////////////////////////////////
    // Session management methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the list of all open collaboration sessions
     *
     */
    public abstract CollabSession[] getSessions();

    /**
     * Returns an existing session for the the provided login information
     *
     * @returns        The corresponding session, or null if one does not exist.
     */
    public abstract CollabSession getSession(Account account);

    /**
     * Creates a new session using the provided login information
     *
     */
    public abstract CollabSession createSession(Account account, String password)
    throws IOException, SecurityException, CollabException;

    /**
     * TODO: Better method name here?
     *
     */
    public synchronized void invalidate() {
        // Deinitialize the UI
        try {
            setUserInterface(null);
        } catch (Exception e) {
            // Ignore
        }

        // Do the manager cleanup
        cleanup();
    }

    /**
     * Cleanup all resources
     *
     */
    protected abstract void cleanup();

    ////////////////////////////////////////////////////////////////////////////
    // Property change methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     *
     *
     */
    protected PropertyChangeSupport getChangeSupport() {
        return changeSupport;
    }

    /**
     *
     *
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        getChangeSupport().addPropertyChangeListener(listener);
    }

    /**
     *
     *
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        getChangeSupport().removePropertyChangeListener(listener);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Lookup methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the current <code>CollabManager</code> instance.  If no
     * <code>Locator</code> has been set by a call to <code>setLocator</code>,
     * this method returns null.
     *
     * @return        The current <code>CollabManager</code> instance or null if
     *                        no instance is available or a <code>Locator</code> hasn't
     *                        been set.
     */
    public static synchronized CollabManager getDefault() {
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
         * Returns the <code>CollabManager</code> instance, if any.  This
         * method will be called many times, so it should be as lightweight
         * as possible.
         *
         * @return        A valid instance or null if no instance is available
         */
        public CollabManager getInstance();
    }
}
