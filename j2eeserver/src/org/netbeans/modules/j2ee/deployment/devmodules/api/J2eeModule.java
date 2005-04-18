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

package org.netbeans.modules.j2ee.deployment.devmodules.api;

import org.netbeans.modules.schema2beans.BaseBean;
import javax.enterprise.deploy.shared.ModuleType;
import org.openide.filesystems.FileObject;
import java.util.Iterator;

/** Abstraction of J2EE module. Provides access to basic properties
 * of the modules: J2EE version, module type, deployment descriptor.
 *
 * @author  Pavel Buzek
 */
public interface J2eeModule {

    /** MIME type for ContentDescriptor of build targets that have J2eeModule in lookup.
     * This can be used to search implementations of targets providing J2eeModule 
     * in project's ContainersList.
     */
    public static final String MIME_J2EE_MODULE_TARGET = "MIME-org-nb-j2eeserver-J2eeModule-BuildTarget"; //NOI18N
    
    /** The module is an EAR archive. */
    public static final Object EAR = ModuleType.EAR;
    /** The module is an Web Application archive. */
    public static final Object WAR = ModuleType.WAR;
    /** The module is an Enterprise Java Bean archive. */
    public static final Object EJB = ModuleType.EJB;
    /** The module is an Connector archive. */
    public static final Object CONN = ModuleType.RAR;
    /** The module is an Client Application archive. */
    public static final Object CLIENT = ModuleType.CAR;
    
    /** 
     * J2EE specification version 1.3 
     * @since 1.5
     */
    public static final String J2EE_13 = "1.3"; //NOI18N
    /** 
     * J2EE specification version 1.4 
     * @since 1.5
     */
    public static final String J2EE_14 = "1.4"; //NOI18N
    /** 
     * J2EE specification version 1.5
     * @since 1.6
     */
    public static final String J2EE_15 = "1.5"; //NOI18N
    
    public static final String APP_XML = "META-INF/application.xml";
    public static final String WEB_XML = "WEB-INF/web.xml";
    public static final String WEBSERVICES_XML = "WEB-INF/webservices.xml";
    public static final String EJBJAR_XML = "META-INF/ejb-jar.xml";
    public static final String EJBSERVICES_XML = "META-INF/webservices.xml";
    public static final String CONNECTOR_XML = "META-INF/ra.xml";
    public static final String CLIENT_XML = "META-INF/application-client.xml";
    
    /** Returns module specification version */
    public String getModuleVersion();
    
    /** Returns module type */
    public Object getModuleType();
    
    /** Returns the location of the module within the application archive. */
    public abstract String getUrl ();
    
    /** Sets the location of the modules within the application archive.
     * For example, a web module could be at "/wbmodule1.war" within the ear
     * file. For standalone module the URL cannot be set to a different value
     * then "/"
     */
    public void setUrl (String url);
    
    /** Returns the archive file for the module of null if the archive file 
     * does not exist (for example, has not been compiled yet). 
     */
    public FileObject getArchive () throws java.io.IOException;
    
    /** Returns the contents of the archive, in copyable form.
     *  Used for incremental deployment.
     *  Currently uses its own {@link RootedEntry} interface.
     *  If the J2eeModule instance describes a
     *  j2ee application, the result should not contain module archives.
     *  @return Iterator through {@link RootedEntry}s
     */
    public Iterator getArchiveContents() throws java.io.IOException;

    /** This call is used in in-place deployment. 
     *  Returns the directory staging the contents of the archive
     *  This directory is the one from which the content entries returned
     *  by {@link #getArchiveContents} came from.
     *  @return FileObject for the content directory
     */
    public FileObject getContentDirectory() throws java.io.IOException;
    
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
    public BaseBean getDeploymentDescriptor(String location);

    public interface RootedEntry {
        FileObject getFileObject ();
        String getRelativePath ();
    }
    
    /** Add module change listener.
     * @param listener on version change
     */
    public void addVersionListener(VersionListener listener);
    
    /** Remove module version change listener.
     * @param listener on version change
     */
    public void removeVersionListener(VersionListener listener);
    
    public interface VersionListener {
        void versionChanged(String oldVersion, String newVersion);
    }
}
