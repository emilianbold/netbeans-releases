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
import java.io.File;
import java.net.URL;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.Specification;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileObject;

/**
 * Implementation of the JavaPlatform API class, which serves proper
 * bootstrap classpath information.
 */
public class J2SEPlatformImpl extends JavaPlatform {

    public static final String PROP_DISPLAY_NAME = "displayName";           //NOI18N
    public static final String PROP_SOURCE_FOLDER = "sourceFolders";         //NOI18N
    public static final String PROP_JAVADOC_FOLDER ="javadocFolders";        //NOI18N
    public static final String PROP_ANT_NAME = "antName";                   //NOI18N
    public static final String PLATFORM_J2SE = "j2se";                      //NOI18N

    protected static final String PLAT_PROP_ANT_NAME="platform.ant.name";             //NOI18N
    protected static final String PLAT_PROP_PLATFORM_HOME    = "platform.home";       //NOI18N   java.home can not be used on MacOS X
    protected static final String PLAT_PROP_PLATFORM_SOURCES = "platform.src";        //NOI18N
    protected static final String PLAT_PROP_PLATFORM_JAVADOC = "platform.javadoc";    //NOI18N
    protected static final String SYSPROP_BOOT_CLASSPATH = "sun.boot.class.path";     // NOI18N
    protected static final String SYSPROP_JAVA_CLASS_PATH = "java.class.path";        // NOI18N

    /**
     * Holds the display name of the platform
     */
    private String displayName;
    /**
     * Holds the properties of the platform
     */
    private Map properties;
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

    J2SEPlatformImpl (String dispName, Map initialProperties, Map sysProperties) {
        super();
        this.displayName = dispName;
        this.properties = initialProperties;
        setSystemProperties(sysProperties);
    }

    protected J2SEPlatformImpl (String dispName, String antName, Map initialProperties, Map sysProperties) {
        this (dispName,  initialProperties, sysProperties);
        this.properties.put (PLAT_PROP_ANT_NAME,antName);
    }

    /**
     * @return  a descriptive, human-readable name of the platform
     */
    public final String getDisplayName() {
        return displayName;
    }

    /**
     * Alters the human-readable name of the platform
     * @param name the new display name
     */
    public final void setDisplayName(String name) {
        this.displayName = name;
        firePropertyChange(PROP_DISPLAY_NAME, null, null); // NOI18N
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
        String home = (String) this.properties.get (PLAT_PROP_PLATFORM_HOME);
        Collection result = new HashSet ();
        if (home != null) {
            StringTokenizer tk = new StringTokenizer (home, File.pathSeparator);
            while (tk.hasMoreTokens()) {
                String path = tk.nextToken();
                File f = new File (path);
                FileObject[] fos = FileUtil.fromFile (f);
                if (fos.length > 0)
                    result.add (fos[0]);
            }
        }
        return result;
    }


    /**
     * Returns the location of the source of platform
     * or null when the location is not set or is invalid
     * @return List<FileObject>
     */
    public final List getSourceFolders () {
        String src = (String) this.properties.get (PLAT_PROP_PLATFORM_SOURCES);
        List result = new ArrayList ();
        if (src != null) {
            StringTokenizer tk = new StringTokenizer (src, File.pathSeparator);
            while (tk.hasMoreTokens()) {
                String path = tk.nextToken ();
                File f = new File (path);
                URL url = Util.getRootURL(f);
                if (url != null) {
                    FileObject fo = FileUtil.findFileObject (url);
                    if (fo!=null)
                        result.add (fo);
                }
            }
        }
        return result;
    }

    public final void setSourceFolders (List c) {
        assert c != null;
        StringBuffer propValue = new StringBuffer ();
        boolean first = true;
        for (Iterator it = c.iterator(); it.hasNext();) {
            FileObject fo = (FileObject) it.next ();
            if (!fo.isFolder())
                throw new IllegalArgumentException ("SourceFolder must be a folder.");
            FileObject tmpFo = FileUtil.getArchiveFile(fo);
            if (tmpFo != null) {
                fo = tmpFo;
            }
            File file = FileUtil.toFile(fo);
            assert file != null : "Invalid FileObject, the FileObject: "+fo.getPath()+" can't be converted into the java.io.File";
            String path = file.getAbsolutePath();
            if (!first) {
                propValue.append (File.pathSeparator);
            }
            else {
                first = false;
            }
            propValue.append (path);
        }
        if (first) {
            this.properties.remove (PLAT_PROP_PLATFORM_SOURCES);
        }
        else {
            this.properties.put (PLAT_PROP_PLATFORM_SOURCES, propValue.toString());
        }
        this.firePropertyChange(PROP_SOURCE_FOLDER, null, null);       
    }

        /**
     * Returns the location of the Javadoc for this platform
     * or null if the location is not set or invalid
     * @return FileObject
     */
    public final List getJavadocFolders () {
        String jdoc = (String) this.properties.get (PLAT_PROP_PLATFORM_JAVADOC);
        List result = new ArrayList ();
        if (jdoc != null) {
            StringTokenizer tk = new StringTokenizer (jdoc, File.pathSeparator);
            while (tk.hasMoreTokens()) {
                String path = tk.nextToken ();
                File f = new File (path);
                URL url = Util.getRootURL(f);
                if (url != null) {
                    FileObject fo = FileUtil.findFileObject (url);
                    if (fo != null)
                        result.add (fo);
                }
            }                                   
        }
        return result;
    }

    public final void setJavadocFolders (List c) {
        assert c != null;
        StringBuffer propValue = new StringBuffer ();
        boolean first  = true;
        for (Iterator it = c.iterator(); it.hasNext();) {
            FileObject fo = (FileObject) it.next ();
            if (!fo.isFolder())
                throw new IllegalArgumentException ("JavadocFolder must be a folder.");
            FileObject tmpFo = FileUtil.getArchiveFile(fo);
            if (tmpFo!=null) {
                fo = tmpFo;
            }
            File file = FileUtil.toFile(fo);
            assert file != null : "Invalid FileObject, the FileObject: "+fo.getPath()+" can't be converted into the java.io.File";
            String path = file.getAbsolutePath();
            if (!first) {
                propValue.append (File.pathSeparator);
            }
            else {
                first = false;
            }
            propValue.append (path);
        }
        if (first) {
            this.properties.remove (PLAT_PROP_PLATFORM_JAVADOC);
        }
        else {
            this.properties.put (PLAT_PROP_PLATFORM_JAVADOC, propValue.toString());
        }
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
}
