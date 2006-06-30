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
package org.netbeans.spi.java.classpath;

import java.util.List;
import java.beans.PropertyChangeListener;

/**
 * SPI interface for ClassPath.
 * @see ClassPathFactory
 * @since org.netbeans.api.java/1 1.4
 */
public interface ClassPathImplementation {

    public static final String PROP_RESOURCES = "resources";    //NOI18N

    /**
     * Returns list of entries, the list is unmodifiable.
     * @return List of PathResourceImplementation, never returns null
     * it may return an empty List
     */
    public List<? extends PathResourceImplementation> getResources();

    /**
     * Adds property change listener.
     * The listener is notified when the set of entries has changed.
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Removes property change listener
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener);
}
