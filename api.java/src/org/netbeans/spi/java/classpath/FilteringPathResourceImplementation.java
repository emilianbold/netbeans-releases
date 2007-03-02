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

import java.net.URL;

/**
 * SPI interface for a classpath entry which can include or exclude particular files.
 * @author Jesse Glick
 * @see "issue #49026"
 * @since org.netbeans.api.java/1 1.13
 */
public interface FilteringPathResourceImplementation extends PathResourceImplementation {

    /**
     * Property name to fire in case {@link #includes} would change.
     * (The old and new value should be left null.)
     * <p>
     * <strong>Special usage note:</strong>
     * If multiple {@link FilteringPathResourceImplementation}s inside a single
     * {@link ClassPathImplementation} fire changes in this pseudo-property in
     * succession, all using the same non-null {@link java.beans.PropertyChangeEvent#setPropagationId},
     * {@link org.netbeans.api.java.classpath.ClassPath#PROP_INCLUDES} will be fired just once. This can be used
     * to prevent "event storms" from triggering excessive Java source root rescanning.
     */
    String PROP_INCLUDES = "includes"; // NOI18N

    /**
     * Determines whether a given resource is included in the classpath or not.
     * @param root one of the roots given by {@link #getRoots} (else may throw {@link IllegalArgumentException})
     * @param resource a relative resource path within that root; may refer to a file or slash-terminated folder; the empty string refers to the root itself
     * @return true if included (or, in the case of a folder, at least partially included); false if excluded
     */
    boolean includes(URL root, String resource);

}
