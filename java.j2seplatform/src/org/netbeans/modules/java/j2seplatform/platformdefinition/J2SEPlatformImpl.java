/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.java.j2seplatform.platformdefinition;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.ErrorManager;

/**
 * Implementation of the JavaPlatform API class, which serves proper
 * bootstrap classpath information.
 */
public class J2SEPlatformImpl extends JavaPlatform {
    
    public static final String PROP_ANT_NAME = "antName";                   //NOI18N
    public static final String PLATFORM_J2SE = "j2se";                      //NOI18N

    protected static final String PLAT_PROP_ANT_NAME="platform.ant.name";             //NOI18N
    protected static final String SYSPROP_BOOT_CLASSPATH = "sun.boot.class.path";     // NOI18N
    protected static final String SYSPROP_JAVA_CLASS_PATH = "java.class.path";        // NOI18N
    protected static final String SYSPROP_JAVA_EXT_PATH = "java.ext.dirs";            //NOI18N

    /**
     * Holds the display name of the platform
     */
    private String displayName;
    /**
     * Holds the properties of the platform
     */
    private Map properties;

    /**
     * List&lt;URL&gt;
     */
    private ClassPath sources;

    /**
     * List&lt;URL&gt;
     */
    private List javadoc;

    /**
     * List&lt;FileObject&gt;
     */
    private List installFolders;

    /**
     * Holds bootstrap libraries for the platform
     */
    Reference       bootstrap = new WeakReference(null);
    /**
     * Holds standard libraries of the platform
     */
    Reference       standardLibs = new WeakReference(null);

    /**
     * Holds the specification of the platform
     */
    private Specification spec;

    J2SEPlatformImpl (String dispName, List installFolders, Map initialProperties, Map sysProperties, List sources, List javadoc) {
        super();
        this.displayName = dispName;
        if (installFolders != null) {
            this.installFolders = installFolders;       //No copy needed, called from this module => safe
        }
        else {
            //Old version, repair
            String home = (String) initialProperties.remove ("platform.home");        //NOI18N
            if (home != null) {
                this.installFolders = new ArrayList ();
                StringTokenizer tk = new StringTokenizer (home, File.pathSeparator);
                while (tk.hasMoreTokens()) {
                    File f = new File (tk.nextToken());
                    try {
                        this.installFolders.add (f.toURI().toURL());
                    } catch (MalformedURLException mue) {
                        ErrorManager.getDefault().notify (mue);
                    }
                }
            }
            else {
                throw new IllegalArgumentException ("Invalid platform, platform must have install folder.");    //NOI18N
            }
        }
        this.properties = initialProperties;
        this.sources = createClassPath(sources);
        if (javadoc != null) {
            this.javadoc = Collections.unmodifiableList(javadoc);   //No copy needed, called from this module => safe
        }
        else {
            this.javadoc = Collections.EMPTY_LIST;
        }
        setSystemProperties(sysProperties);
    }

    protected J2SEPlatformImpl (String dispName, String antName, List installFolders, Map initialProperties, Map sysProperties, List sources, List javadoc) {
        this (dispName,  installFolders, initialProperties, sysProperties,sources, javadoc);
        this.properties.put (PLAT_PROP_ANT_NAME,antName);
    }

    /**
     * @return  a descriptive, human-readable name of the platform
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Alters the human-readable name of the platform
     * @param name the new display name
     */
    public void setDisplayName(String name) {
        this.displayName = name;
        firePropertyChange(PROP_DISPLAY_NAME, null, null); // NOI18N
    }
    
    /**
     * Alters the human-readable name of the platform without firing
     * events. This method is an internal contract to allow lazy creation
     * of display name
     * @param name the new display name
     */
    final protected void internalSetDisplayName (String name) {
        this.displayName = name;
    }


    public String getAntName () {
        return (String) this.properties.get (PLAT_PROP_ANT_NAME);
    }

    public void setAntName (String antName) {
        if (antName == null || antName.length()==0) {
            throw new IllegalArgumentException ();
        }
        this.properties.put(PLAT_PROP_ANT_NAME, antName);
        this.firePropertyChange (PROP_ANT_NAME,null,null);
    }


    public ClassPath getBootstrapLibraries() {
        synchronized (this) {
            ClassPath cp = (ClassPath) (bootstrap == null ? null : bootstrap.get());
            if (cp != null)
                return cp;
            String pathSpec = (String)getSystemProperties().get(SYSPROP_BOOT_CLASSPATH);
            String extPathSpec = Util.getExtensions((String)getSystemProperties().get(SYSPROP_JAVA_EXT_PATH));
            if (extPathSpec != null) {
                pathSpec = pathSpec + File.pathSeparator + extPathSpec;
            }
            cp = Util.createClassPath (pathSpec);
            bootstrap = new WeakReference(cp);
            return cp;
        }
    }

    /**
     * This implementation simply reads and parses `java.class.path' property and creates a ClassPath
     * out of it.
     * @return  ClassPath that represents contents of system property java.class.path.
     */
    public ClassPath getStandardLibraries() {
        synchronized (this) {
            ClassPath cp = (ClassPath) (standardLibs == null ? null : standardLibs.get());
            if (cp != null)
                return cp;
            String pathSpec = (String)getSystemProperties().get(SYSPROP_JAVA_CLASS_PATH);
            cp = Util.createClassPath (pathSpec);
            standardLibs = new WeakReference(cp);
            return cp;
        }
    }

    /**
     * Retrieves a collection of {@link org.openide.filesystems.FileObject}s of one or more folders
     * where the Platform is installed. Typically it returns one folder, but
     * in some cases there can be more of them.
     */
    public final Collection getInstallFolders() {
        Collection result = new ArrayList ();
        for (Iterator it = this.installFolders.iterator(); it.hasNext();) {
            URL url = (URL) it.next ();
            FileObject root = URLMapper.findFileObject(url);
            if (root != null) {
                result.add (root); 
            }
        }
        return result;
    }


    public final FileObject findTool(final String toolName) {
        return Util.findTool (toolName, this.getInstallFolders());
    }


    /**
     * Returns the location of the source of platform
     * @return List&lt;URL&gt;
     */
    public final ClassPath getSourceFolders () {
        return this.sources;
    }

    public final void setSourceFolders (ClassPath c) {
        assert c != null;
        this.sources = c;
        this.firePropertyChange(PROP_SOURCE_FOLDER, null, null);
    }

        /**
     * Returns the location of the Javadoc for this platform
     * @return FileObject
     */
    public final List getJavadocFolders () {
        return this.javadoc;
    }

    public final void setJavadocFolders (List c) {
        assert c != null;
        List safeCopy = Collections.unmodifiableList (new ArrayList (c));
        for (Iterator it = safeCopy.iterator(); it.hasNext();) {
            URL url = (URL) it.next ();
            if (!"jar".equals (url.getProtocol()) && FileUtil.isArchiveFile(url)) {
                throw new IllegalArgumentException ("JavadocFolder must be a folder.");
            }
        }
        this.javadoc = safeCopy;
        this.firePropertyChange(PROP_JAVADOC_FOLDER, null, null);
    }

    public String getVendor() {
        String s = (String)getSystemProperties().get("java.vm.vendor"); // NOI18N
        return s == null ? "" : s; // NOI18N
    }

    public Specification getSpecification() {
        if (spec == null) {
            spec = new Specification (PLATFORM_J2SE, Util.getSpecificationVersion(this)); //NOI18N
        }
        return spec;
    }

    public Map getProperties() {
        return Collections.unmodifiableMap (this.properties);
    }
    
    Collection getInstallFolderURLs () {
        return Collections.unmodifiableList(this.installFolders);
    }


    private static ClassPath createClassPath (List urls) {
        List resources = new ArrayList ();
        if (urls != null) {
            for (Iterator it = urls.iterator(); it.hasNext();) {
                URL url = (URL) it.next();
                resources.add (ClassPathSupport.createResource (url));
            }
        }
        return ClassPathSupport.createClassPath (resources);
    }
}
