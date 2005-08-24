/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
