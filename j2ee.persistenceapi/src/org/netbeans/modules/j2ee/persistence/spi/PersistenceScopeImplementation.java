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

import org.netbeans.api.java.classpath.ClassPath;
import org.openide.filesystems.FileObject;

/**
 * The SPI for {@link org.netbeans.modules.j2ee.persistence.api.PersistenceScope}.
 *
 * @author Andrei Badea
 *
 * @see org.netbeans.modules.j2ee.persistence.api.PersistenceScope
 * @see PersistenceScopeFactory
 */
public interface PersistenceScopeImplementation {

    /**
     * Returns the persistence.xml file of this persistence scope.
     *
     * @return the persistence.xml file or null if it the persistence.xml file does
     * not exist.
     */
    FileObject getPersistenceXml();

    /**
     * Provides the classpath of this persistence scope, which covers the sources 
     * of the entity classes referenced by the persistence.xml file, as well
     * as the referenced JAR files.
     *
     * @return the persistence scope classpath; never null.
     */
    ClassPath getClassPath();
}
