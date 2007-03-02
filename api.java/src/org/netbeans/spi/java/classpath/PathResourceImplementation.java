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
     * <p><strong>Semi-deprecated.</strong> There was never a real reason for this method to exist.
     * If implementing <code>PathResourceImplementation</code> you can simply return null;
     * it is unlikely anyone will call this method anyway.
     * @return classpath handle in case of composite resource; null for leaf resource
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
