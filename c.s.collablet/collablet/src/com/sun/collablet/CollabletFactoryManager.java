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

import java.util.*;


/**
 *
 * @author  Todd Fast, todd.fast@sun.com
 */
public abstract class CollabletFactoryManager extends Object {
    ////////////////////////////////////////////////////////////////////////////
    // Class variables
    ////////////////////////////////////////////////////////////////////////////
    private static CollabletFactoryManager DEFAULT_INSTANCE;
    private static Locator LOCATOR;
    public static final String PROP_COLLABLET_FACTORIES = "collabletFactories";

    ////////////////////////////////////////////////////////////////////////////
    // Instance variables
    ////////////////////////////////////////////////////////////////////////////
    private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

    /**
     *
     *
     */
    protected CollabletFactoryManager() {
        super();
    }

    /**
     *
     *
     */
    public abstract CollabletFactory[] getCollabletFactories();

    /**
     * Automatically configures channels from all providers on the provider
     * conversation object
     *
     */
    public synchronized void configureChannels(Conversation conversation) {
        // Create a channel from each provider and set it on the conversation
        CollabletFactory[] providers = getCollabletFactories();

        for (int i = 0; i < providers.length; i++) {
            Collablet channel = providers[i].createInstance(conversation);
            conversation.addChannel(channel);
        }
    }

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
    // Instance management methods
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Returns the current <code>CollabletFactoryManager</code> instance.  If no
     * <code>Locator</code> has been set by a call to <code>setLocator</code>,
     * this method returns null.
     *
     * @return        The current <code>CollabletFactoryManager</code> instance or
     *                        null if no instance is available or a <code>Locator</code>
     *                        has not been set.
     */
    public static synchronized CollabletFactoryManager getDefault() {
        if (LOCATOR == null) {
            if (DEFAULT_INSTANCE == null) {
                DEFAULT_INSTANCE = new CollabletFactoryManager() {
                            public CollabletFactory[] getCollabletFactories() {
                                final CollabletFactory[] FACTORIES = new CollabletFactory[0];

                                return FACTORIES;
                            }
                        };
            }

            return DEFAULT_INSTANCE;
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
         * Returns the <code>CollabletFactoryManager</code> instance, if any.
         * This method will be called many times, so it should be as
         * lightweight as possible.
         *
         * @return        A valid instance or null if no instance is available
         */
        public CollabletFactoryManager getInstance();
    }
}
