/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
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
    public List /*<PathResourceImplementation>*/ getResources();

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
