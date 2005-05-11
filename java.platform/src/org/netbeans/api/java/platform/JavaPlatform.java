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

package org.netbeans.api.java.platform;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.openide.filesystems.FileObject;

/**
 * JavaPlatform describes a java platform in a way that the IDE tools may utilize. It may serve as
 * description of the platform a java project targets, or it may provide access to tools from the
 * particular SDK installation. It also provides information about individual platforms, for example
 * the Java platform version implemented, vendor name or implementation version. It is also possible
 * to enumerate services that the IDE supports, which are implemented as a part of the Platform.
 *
 * @author Radko Najman, Svata Dedic, Tomas Zezula
 */
public abstract class JavaPlatform {

    //List of names of mutable properties
    /**
     * Property name for displayName
     */
    public static final String PROP_DISPLAY_NAME = "displayName";           //NOI18N
    
    /**
     * Property name for sourceFolders
     */
    public static final String PROP_SOURCE_FOLDER = "sourceFolders";         //NOI18N
    
    /**
     * Property name for javadocFolders
     */
    public static final String PROP_JAVADOC_FOLDER ="javadocFolders";        //NOI18N
    
    /**
     * Property name for systemProperties
     */
    public static final String PROP_SYSTEM_PROPERTIES = "systemProperties";  //NOI18N

    private Map sysproperties = Collections.EMPTY_MAP;
    private PropertyChangeSupport supp;
    
    /** Creates a new instance of JavaPlatform */
    protected JavaPlatform() {
    }

    /**
     * @return  a descriptive, human-readable name of the platform
     */
    public abstract String getDisplayName();

    /**
     * Registers a listener to be notified when some of the platform's properties
     * change
     */
    public final void addPropertyChangeListener(PropertyChangeListener l) {
        synchronized (this) {
            if (supp == null)
                supp = new PropertyChangeSupport(this);
        }
        supp.addPropertyChangeListener(l);
    }
    
    /**
     * Removes a listener registered previously
     */
    public final void removePropertyChangeListener(PropertyChangeListener l) {
        if (supp != null)
            supp.removePropertyChangeListener(l);
    }
    
    /** Gets the properties defined for java platform.
     * @return the java platform properties
     */
    public abstract Map getProperties();
    
    /** Gets the java platform system properties.
     * @return the java platform system properties
     */
    public final Map getSystemProperties() {
        return sysproperties;
    }

    /**
     * Returns a ClassPath, which represents bootstrap libraries for the
     * runtime environment. The Bootstrap libraries include libraries in 
     * JRE's extension directory, if there are any.
     * @return ClassPath representing the bootstrap libs
     */
    public abstract ClassPath getBootstrapLibraries();
    
    /**
     * Returns libraries recognized by default by the platform. Usually
     * it corresponds to contents of CLASSPATH environment variable.
     */
    public abstract ClassPath getStandardLibraries();

    /**
     * Returns the vendor of the Java SDK
     * @return String
     */
    public abstract String getVendor ();

    /**
     * Returns specification of the Java SDK
     * @return Specification
     */
    public abstract Specification getSpecification ();

    /**
     * Retrieves a collection of {@link FileObject}s of one or more folders
     * where the Platform is installed. Typically it returns one folder, but
     * in some cases there can be more of them.
     */
    public abstract Collection getInstallFolders();

    /**
     * Gets the platform tool executable.
     * @param toolName the tool platform independent tool name.
     * @return file representing the tool executable, or null if the tool can not be found
     */
    public abstract FileObject findTool (String toolName);


    /**
     * Returns the locations of the source of platform
     * or empty collection when the location is not set or is invalid
     * @return ClassPath never returns null
     */
    public abstract ClassPath getSourceFolders ();

    /**
     * Returns the locations of the Javadoc for this platform
     * or empty collection if the location is not set or invalid
     * @return List&lt;URL&gt never returns null
     */
    public abstract List getJavadocFolders ();


    /**
     * Get the "default platform", meaning the JDK on which NetBeans itself is running.
     * @return the default platform, if it can be found, or null
     * @see JavaPlatformManager#getDefaultPlatform
     */
    public static JavaPlatform getDefault() {
        return JavaPlatformManager.getDefault().getDefaultPlatform();
    }


    //SPI methods

    /** Fires PropertyChange to all registered PropertyChangeListeners
     * @param propName
     * @param oldValue
     * @param newValue
     */
    protected final void firePropertyChange(String propName, Object oldValue, Object newValue) {
        if (supp != null)
            supp.firePropertyChange(propName, oldValue, newValue);
    }

    /** Sets the system properties of java platform.
     * @param sysproperties the java platform system properties
     */
    protected final void setSystemProperties(Map sysproperties) {
        this.sysproperties = Collections.unmodifiableMap(sysproperties);
        firePropertyChange(PROP_SYSTEM_PROPERTIES, null, null); // NOI18N
    }

 }
