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

package org.netbeans.modules.j2ee.persistence.api;

import java.beans.PropertyChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceScopesImplementation;
import org.netbeans.modules.j2ee.persistence.spi.PersistenceScopesProvider;
import org.netbeans.modules.j2ee.persistenceapi.PersistenceScopesAccessor;

/**
 * Describes a list of persistence scopes and allows listening on this list.
 *
 * @author Andrei Badea
 */
public final class PersistenceScopes {

    /**
     * The property corresponding to {@link #getPersistenceScopes}.
     */
    public static final String PROP_PERSISTENCE_SCOPES = "persistenceScopes"; // NOI18N

    private final PersistenceScopesImplementation impl;

    static {
        PersistenceScopesAccessor.DEFAULT = new PersistenceScopesAccessor() {
            public PersistenceScopes createPersistenceScopes(PersistenceScopesImplementation impl) {
                return new PersistenceScopes(impl);
            }
        };
    }

    /**
     * Returns an instance of <code>PersistenceScopes</code> for the given
     * project.
     *
     * @return an instance of <code>PersistenceScopes</code> or null if the
     *         project doesn't provide a list of persistence scopes.
     * @throws NullPointerException if <code>project</code> was null.
     */
    public static PersistenceScopes getPersistenceScopes(Project project) {
        if (project == null) {
            throw new NullPointerException("Passed null to PersistenceScopes.getPersistenceScopes(Project)"); // NOI18N
        };
        PersistenceScopesProvider provider = (PersistenceScopesProvider)project.getLookup().lookup(PersistenceScopesProvider.class);
        if (provider != null) {
            return provider.getPersistenceScopes();
        }
        return null;
    }

    private PersistenceScopes(PersistenceScopesImplementation impl) {
        this.impl = impl;
    }

    /**
     * Returns the persistence scopes contained in this instance.
     *
     * @return an array of <code>PersistenceScope</code> instances; never null.
     */
    public PersistenceScope[] getPersistenceScopes() {
        return impl.getPersistenceScopes();
    }

    /**
     * Adds a property change listener, allowing to listen on properties, e.g.
     * {@link #PROP_PERSISTENCE_SCOPES}.
     *
     * @param  listener the listener to add; can be null.
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        impl.addPropertyChangeListener(listener);
    }

    /**
     * Removes a property change listener.
     *
     * @param  listener the listener to remove; can be null.
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        impl.removePropertyChangeListener(listener);
    }
}
