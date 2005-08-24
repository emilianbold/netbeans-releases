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

import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.web.api.webmodule.WebModule;

/**
 * SPI interface for {@link org.netbeans.modules.j2ee.api.ejbjar.Ear}.
 * @see EjbJarFactory
 */
public interface EarImplementation {
    
    /** J2EE platform version - one of the constants 
     * defined in {@link org.netbeans.modules.j2ee.api.common.EjbProjectConstants}.
     * @return J2EE platform version
     */
    String getJ2eePlatformVersion ();
    
    /** META-INF folder for the Ear.
     */
    FileObject getMetaInf ();

    /** Deployment descriptor (application.xml file) of the ejb module.
     */
    FileObject getDeploymentDescriptor ();
    
    /** Add j2ee webmodule into application.
     * @param module the module to be added
     */
    void addWebModule (WebModule module);
    
    /** Add j2ee ejbjar module into application.
     * @param module the module to be added
     */
    void addEjbJarModule (EjbJar module);
    
}
