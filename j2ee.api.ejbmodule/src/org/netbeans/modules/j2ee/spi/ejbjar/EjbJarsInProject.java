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

package org.netbeans.modules.j2ee.spi.ejbjar;

import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.openide.filesystems.FileObject;

/**
 * Put an implementation of this interface into lookup of an ejb project.
 * This is a complementary interface to EjbJarProvider to allow clients find all
 * ejb modules within a project w/o specifying a concrete file.
 * @see EjbJar#getEjbJar
 * @author Pavel Buzek
 */
public interface EjbJarsInProject {

    /**
     * Get EjbJar for all ejb modules in a given project.
     *
     * @return an array of EjbJar for all modules in a project
     * @see EjbJarFactory
     */
    EjbJar[] getEjbJars();
    
}
