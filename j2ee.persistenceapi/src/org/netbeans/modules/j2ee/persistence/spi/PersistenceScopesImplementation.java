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

package org.netbeans.modules.j2ee.persistence.spi;

import java.beans.PropertyChangeListener;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;

/**
 * The SPI for {@link org.netbeans.modules.j2ee.persistence.api.PersistenceScopes}.
 *
 * @author Andrei Badea
 *
 * @see org.netbeans.modules.j2ee.persistence.api.PersistenceScopes
 * @see PersistenceScopesFactory
 */
public interface PersistenceScopesImplementation {

    /**
     * Returns the persistence scopes contained in this instance.
     *
     * @return an array of <code>PersistenceScope</code> instances; never null.
     */
    PersistenceScope[] getPersistenceScopes();

    /**
     * Adds a property change listener, allowing to listen on properties, e.g.
     * {@link org.netbeans.modules.j2ee.persistence.api.PersistenceScopes#PROP_PERSISTENCE_SCOPES}.
     *
     * @param  listener the listener to add; can be null.
     */
    void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Removes a property change listener.
     *
     * @param  listener the listener to remove; can be null.
     */
    void removePropertyChangeListener(PropertyChangeListener listener);
}
