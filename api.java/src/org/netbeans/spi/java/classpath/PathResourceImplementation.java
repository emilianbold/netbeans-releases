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


import java.beans.PropertyChangeListener;
import java.net.URL;

/**
 * SPI interface for one classpath entry.
 * @see ClassPathImplementation
 * @since org.netbeans.api.java/1 1.4
 */
public interface PathResourceImplementation {

    public static final String PROP_ROOTS = "roots";    //NOI18N

    /** Roots of the class path entry.
     *  In the case of simple resource it returns array containing just one URL.
     *  In the case of composite resource it returns array containing one or more URL.
     * @return array of URL, never returns null.
     */
    public URL[] getRoots();

    /**
     * Returns ClassPathImplementation representing the content of the PathResourceImplementation.
     * If the PathResourceImplementation represents leaf resource, it returns null.
     * The ClassPathImplementation is live and can be used for path resource content
     * modification.
     * @return ClassPath in case of composite resource null for leaf resource
     */
    public ClassPathImplementation getContent();

    /**
     * Adds property change listener.
     * The listener is notified when the roots of the entry are changed.
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Removes property change listener.
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener);

}
