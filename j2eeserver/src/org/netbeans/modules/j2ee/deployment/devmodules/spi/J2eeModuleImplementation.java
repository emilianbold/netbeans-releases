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

package org.netbeans.modules.j2ee.deployment.devmodules.spi;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Iterator;
import org.netbeans.modules.j2ee.dd.api.common.RootInterface;
import org.openide.filesystems.FileObject;

/**
 * Base SPI interface for {@link J2eeModule}. Implementation of this interface 
 * is used to create {@link J2eeModule} instance using the {@link J2eeModuleFactory}.
 * 
 * @author sherold
 * 
 * @since 1.23
 */
public interface J2eeModuleImplementation {
    
    /** 
     * Returns module specification version.
     *
     * @return module specification version.
     */
    String getModuleVersion();
    
    /** 
     * Returns module type.
     * 
     * @return module type.
     */
    Object getModuleType();
    
    /** 
     * Returns the location of the module within the application archive. 
     * 
     * TODO: this does not belong here.. it has to be moved to J2eeApplication
     */
    abstract String getUrl ();
    
    /** Returns the archive file for the module of null if the archive file 
     * does not exist (for example, has not been compiled yet). 
     */
    FileObject getArchive () throws java.io.IOException;
    
    /** Returns the contents of the archive, in copyable form.
     *  Used for incremental deployment.
     *  Currently uses its own {@link RootedEntry} interface.
     *  If the J2eeModule instance describes a
     *  j2ee application, the result should not contain module archives.
     *  @return Iterator through {@link RootedEntry}s
     */
    Iterator getArchiveContents() throws java.io.IOException;

    /** This call is used in in-place deployment. 
     *  Returns the directory staging the contents of the archive
     *  This directory is the one from which the content entries returned
     *  by {@link #getArchiveContents} came from.
     *  @return FileObject for the content directory, return null if the 
     *     module doesn't have a build directory, like an binary archive project
     */
    FileObject getContentDirectory() throws java.io.IOException;
    
    /** Returns a live bean representing the final deployment descriptor
     * that will be used for deploment of the module. This can be
     * taken from sources, constructed on fly or a combination of these
     * but it needs to be available even if the module has not been built yet.
     *
     * @param location Parameterized by location because of possibility of multiple 
     * deployment descriptors for a single module (jsp.xml, webservices.xml, etc).
     * Location must be prefixed by /META-INF or /WEB-INF as appropriate.
     * @return a live bean representing the final DD
     */
    RootInterface getDeploymentDescriptor(String location);
    
    /**
     * Returns the module resource directory, or null if the module has no resource
     * directory.
     * 
     * @return the module resource directory, or null if the module has no resource
     *         directory.
     */
    File getResourceDirectory();
    
    /**
     * Returns source deployment configuration file path for the given deployment 
     * configuration file name.
     *
     * @param name file name of the deployment configuration file, WEB-INF/sun-web.xml
     *        for example.
     * 
     * @return absolute path to the deployment configuration file, or null if the
     *         specified file name is not known to this J2eeModule.
     */
    File getDeploymentConfigurationFile(String name);
    
    /**
     * Add a PropertyChangeListener to the listener list.
     * 
     * @param listener PropertyChangeListener
     */
    void addPropertyChangeListener(PropertyChangeListener listener);
    
    /**
     * Remove a PropertyChangeListener from the listener list.
     * 
     * @param listener PropertyChangeListener
     */
    void removePropertyChangeListener(PropertyChangeListener listener);
    
}
