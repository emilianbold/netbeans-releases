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

import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.openide.filesystems.FileObject;

/**
 * SPI for {@link org.netbeans.modules.web.api.webmodule.WebModule}.
 *
 * @see WebModuleFactory
 */
public interface WebModuleImplementation {

    /**
     * Returns the folder that contains sources of the static documents for
     * the web module (html, JSPs, etc.).
     *
     * @return the static documents folder; can be null.
     */
    FileObject getDocumentBase ();

    /**
     * Returns the context path of the web module.
     *
     * @return the context path; can be null.
     */
    String getContextPath ();

    /**
     * Returns the J2EE platform version of this module. The returned value is
     * one of the constants {@link org.netbeans.modules.web.api.webmodule.WebModule#J2EE_13_LEVEL},
     * {@link org.netbeans.modules.web.api.webmodule.WebModule#J2EE_14_LEVEL} or
     * {@link org.netbeans.modules.web.api.webmodule.WebModule#JAVA_EE_5_LEVEL}.
     *
     * @return J2EE platform version; never null.
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
     * Returns the deployment descriptor (<code>web.xml</code> file) of the web module.
     * <div class="nonnormative">
     * The web.xml file would typically be a child of the folder returned
     * by {@link #getWebInf} but does not need to be.
     * </div>
     *
     * @return the <code>web.xml</code> file; can be null.
     */
    FileObject getDeploymentDescriptor ();

    /**
     * Returns the Java source roots associated with the web module.
     * <div class="nonnormative">
     * <p>Note that not all the java source roots in the project (e.g. in a freeform project)
     * belong to the web module.</p>
     * </div>
     *
     * @return this web module's Java source roots; never null.
     * 
     * @deprecated This method is deprecated, because its return values does
     * not contain enough information about the source roots. Source roots
     * are usually implemented by a <code>org.netbeans.api.project.SourceGroup</code>,
     * which is more than just a container for a {@link org.openide.filesystems.FileObject}.
     */
    @Deprecated
    FileObject[] getJavaSources();

    /**
     * Returns a model describing the metadata of this web module (servlets,
     * resources, etc.).
     *
     * @return this web module's metadata model; never null.
     */
    MetadataModel<WebAppMetadata> getMetadataModel();
}
