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
 * SPI interface for {@link org.netbeans.modules.web.api.webmodule.WebModule}.
 * @see WebModuleFactory
 */
public interface EjbJarImplementation {
    
    /** J2EE platform version - one of the constants 
     * {@link org.netbeans.modules.web.api.webmodule.WebModule#J2EE_13_LEVEL}, 
     * {@link org.netbeans.modules.web.api.webmodule.WebModule#J2EE_14_LEVEL}.
     * @return J2EE platform version
     */
    String getJ2eePlatformVersion ();
    
    /** WEB-INF folder for the web module.
     * <div class="nonnormative">
     * The WEB-INF folder would typically be a child of the folder returned 
     * by {@link #getDocumentBase} but does not need to be.
     * </div>
     */
    FileObject getMetaInf ();

    /** Deployment descriptor (web.xml file) of the web module.
     * <div class="nonnormative">
     * The web.xml file would typically be a child of the folder returned 
     * by {@link #getWebInf} but does not need to be.
     * </div>
     */
    FileObject getDeploymentDescriptor ();
}
