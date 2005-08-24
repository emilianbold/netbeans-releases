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
package org.netbeans.modules.j2ee.spi.ejbjar;

import org.openide.filesystems.FileObject;

/**
 * SPI interface for {@link org.netbeans.modules.j2ee.api.ejbjar.EjbJar}.
 * @see EjbJarFactory
 */
public interface EjbJarImplementation {
    
    /** J2EE platform version - one of the constants 
     * defined in {@link org.netbeans.modules.j2ee.api.common.EjbProjectConstants}.
     * @return J2EE platform version
     */
    String getJ2eePlatformVersion ();
    
    /** META-INF folder for the web module.
     */
    FileObject getMetaInf ();

    /** Deployment descriptor (ejb-jar.xml file) of the ejb module.
     */
    FileObject getDeploymentDescriptor ();

    /** Source roots associated with the EJB module.
     * <div class="nonnormative">
     * Note that not all the java source roots in the project (e.g. in a freeform project)
     * belong to the EJB module.
     * </div>
     */
    FileObject[] getJavaSources();
}
