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
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
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
     * Returns a Java EE module specification version, version of a web application 
     * for example.
     * <p>
     * Do not confuse with the Java EE platform specification version.
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
    
    /**
     * Returns a metadata model of a deployment descriptor specified by the 
     * <code>type</code> parameter.
     * 
     * <p>
     * As an example, passing <code>org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata.class</code>
     * as a type parameter will return a metadata model of the web module deployment 
     * descriptor - web.xml.
     * </p>
     * 
     * @param type metadata model type class for which a <code>MetadataModel</code>
     *        instance will be returned.
     * 
     * @return metadata model of a deployment descriptor specified by the <code>type</code>
     *         parameter.
     */
    <T> MetadataModel<T> getMetadataModel(Class<T> type);
    
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
