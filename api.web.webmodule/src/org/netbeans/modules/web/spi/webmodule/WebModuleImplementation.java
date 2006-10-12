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
package org.netbeans.modules.web.spi.webmodule;

import org.openide.filesystems.FileObject;

/**
 * SPI interface for {@link org.netbeans.modules.web.api.webmodule.WebModule}.
 * @see WebModuleFactory
 */
public interface WebModuleImplementation {

    /** Folder that contains sources of the static documents for
     * the web module (html, JSPs, etc.).
     */
    FileObject getDocumentBase ();

    /** Context path of the web module.
     */
    String getContextPath ();

    /** J2EE platform version - one of the constants
     * {@link org.netbeans.modules.web.api.webmodule.WebModule#J2EE_13_LEVEL}, 
     * {@link org.netbeans.modules.web.api.webmodule.WebModule#J2EE_14_LEVEL}.
     * @return J2EE platform version
     */
    String getJ2eePlatformVersion ();
    
    /**
     * WEB-INF folder for the web module.
     * <div class="nonnormative">
     * The WEB-INF folder would typically be a child of the folder returned 
     * by {@link #getDocumentBase} but does not need to be.
     * </div>
     *
     * @return the {@link FileObject}; might be <code>null</code>
     */
    FileObject getWebInf ();

    /**
     * Deployment descriptor (web.xml file) of the web module.
     * <div class="nonnormative">
     * The web.xml file would typically be a child of the folder returned 
     * by {@link #getWebInf} but does not need to be.
     * </div>
     *
     * @return the {@link FileObject}; might be <code>null</code>
     */
    FileObject getDeploymentDescriptor ();
    
    /** Source roots associated with the web module.
     * <div class="nonnormative">
     * Note that not all the java source roots in the project (e.g. in a freeform project)
     * belong to the web module.
     * </div>
     */
    FileObject[] getJavaSources();
    
}
