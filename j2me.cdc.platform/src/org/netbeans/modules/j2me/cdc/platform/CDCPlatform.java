/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.j2me.cdc.platform;

import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.modules.j2me.cdc.platform.platformdefinition.Util;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.filesystems.URLMapper;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Lookup;


/**
 * Implementation of the JavaPlatform API class, which serves proper
 * bootstrap classpath information.
 */
public class CDCPlatform extends JavaPlatform {

    public static final String PROP_ANT_NAME = "antName";                   //NOI18N
    public static final String PLATFORM_CDC = "cdc";                      //NOI18N

    protected static final String PLAT_PROP_ANT_NAME="platform.ant.name";             //NOI18N
    public static final String PLATFORM_STRING_PREFIX = "${platform.home}/"; // NOI18N

//    protected static final String SYSPROP_BOOT_CLASSPATH = "sun.boot.class.path";     // NOI18N
//    protected static final String SYSPROP_JAVA_CLASS_PATH = "java.class.path";        // NOI18N
//    protected static final String SYSPROP_JAVA_EXT_PATH = "java.ext.dirs";            //NOI18N

    public static String PROP_EXEC_MAIN   = "main";
    public static String PROP_EXEC_XLET   = "xlet";
    public static String PROP_EXEC_APPLET = "applet";

    /**
     * Holds the properties of the platform
     */
    private Properties properties;

    /**
     * Holds the display name of the platform
     */
    private String displayName;

    /**
     * String type of VM
     */
    private String type;

    /**
     * String supported class version
     */
    private String classVersion;

    /**
     * List&lt;URL&gt;
     */
    private ClassPath sources;

    /**
     * List&lt;URL&gt;
     */
    private List<URL> javadoc;

    /**
     * List&lt;FileObject&gt;
     */
    private List<URL> installFolders;

    /**
     * List of supported devices
     */
    private CDCDevice[] devices;

    private boolean fatJar;

    /**
     * Holds bootstrap libraries for the platform
     */
    Reference<ClassPath> bootstrap = new WeakReference<ClassPath>(null);
    /**
     * Holds standard libraries of the platform
     */
    Reference<ClassPath> standardLibs = new WeakReference<ClassPath>(null);

    /**
     * Holds the specification of the platform
     */
    private Specification spec;

    /**
     * @param dispName display name for platfrom
     * @param antName ant name for platform
     * @param type type of platform (semc, nokiaS80, savaje, ricoh)
     * @param classVersion maximum class version supported
     * @param installFolders where the platfrom is installed
     * @param sources list of FileObject with source folders for platform
     * @param javadoc list of FileObject with javadoc folders for platform (note, top level folders, where index.html is located or entry in zip file)
     * @param devices array of CDCDevices
     */
    public CDCPlatform (String dispName, String antName, String type, String classVersion, List<URL> installFolders, List<URL> sources, List<URL> javadoc, CDCDevice[] devices, boolean fatJar) {
        super();
        assert classVersion != null;
        this.displayName = dispName;
        this.type = type;
        this.classVersion = classVersion;
        this.devices = devices;
        this.fatJar = fatJar;

        if (installFolders != null) {
            this.installFolders = installFolders;       //No copy needed, called from this module => safe
        }
        this.sources = createClassPath(sources);
        if (javadoc != null) {
            this.javadoc = Collections.unmodifiableList(javadoc);   //No copy needed, called from this module => safe
        }
        else {
            this.javadoc = Collections.EMPTY_LIST;
        }
        properties = new Properties();
        properties.put (PLAT_PROP_ANT_NAME, antName);
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

    /**
     * @return ant name
     */
    public String getAntName () {
        return (String) properties.get (PLAT_PROP_ANT_NAME);
    }

    /**
     * @param antName name of platfrom
     */
    public void setAntName (String antName) {
        if (antName == null || antName.length()==0) {
            throw new IllegalArgumentException ();
        }
        this.properties.put(PLAT_PROP_ANT_NAME, antName);
        this.firePropertyChange (PROP_ANT_NAME,null,null);
    }

    /**
     * Set platform bootpath to specified values
     */
    public void setBootstrapLibraries(List<URL> bs)
    {
        assert bs != null;
        synchronized (this) {
            //find differences
            List<URL> newPath=new ArrayList<URL>();
            newPath.addAll(bs);
            ClassPath cp=ClassPathSupport.createClassPath(getAllLibraries());
            List<ClassPath.Entry> entries=cp.entries();
            List<FileObject> removed=new ArrayList<FileObject>();
            for (ClassPath.Entry entry : entries) {
                URL url=entry.getURL();
                if (!newPath.remove(url))
                    removed.add(entry.getRoot());
            }

            boolean changing = false;
            bootstrap = new WeakReference<ClassPath>(ClassPathSupport.createClassPath(bs.toArray(new URL[bs.size()])));
            for (CDCDevice device : devices ) {
                CDCDevice.CDCProfile[] profiles = device.getProfiles();
                for ( CDCDevice.CDCProfile profile : profiles)
                {
                    String bootp=profile.getBootClassPath();

                    //Remove removed jars
                    List<FileObject> fobs=resolveRelativePathToFileObjects(bootp);
                    for (FileObject fo:removed)
                    {
                        fobs.remove(fo);
                    }

                    //Construct new bootstrap classpath
                    StringBuffer newBS=new StringBuffer();
                    for (URL url : bs)
                    {
                        FileObject fo=URLMapper.findFileObject(url);
                        String name=null;
                        if (fo.isRoot())
                            try
                            {
                                name=fo.getFileSystem().getDisplayName();
                            } catch (FileStateInvalidException ex)
                            {}
                        else
                            name=fo.getPath();

                        if (newPath.contains(url))
                        {
                            newBS.append(name);
                            newBS.append(';');
                        }

                        if (fobs.contains(fo))
                        {
                            newBS.append(name);
                            newBS.append(';');
                        }
                    }

                    changing = true;
                    profile.setBootClassPath(newBS.toString());
                    profile.setRunClassPath(newBS.toString());
                }
            }
            if (changing)
                firePropertyChange("classpath", null, null);
        }
    }

    /**
     * @return bootstarap libraries. If there are more platfrom with different classpath supported, return all classpath entries
     */
    public ClassPath getBootstrapLibraries() {
        synchronized (this) {
            ClassPath cp = bootstrap == null ? null : bootstrap.get();
            if (cp != null)
                return cp;
            cp = ClassPathSupport.createClassPath(getAllLibraries());
            bootstrap = new WeakReference<ClassPath>(cp);
            return cp;
        }
    }

    public ClassPath getBootstrapLibrariesForProfile(String activeDevice, String activeProfile){
        if (activeDevice == null || activeProfile == null){ //in such case return all
            return getBootstrapLibraries();
        }
        CDCDevice[] devices = this.getDevices();
        CDCDevice.CDCProfile active = null;
        for (int i = 0; devices != null && i < devices.length; i++) {
            if (devices[i].getName().equals(activeDevice)){
                CDCDevice.CDCProfile[] profiles = devices[i].getProfiles();
                for (int j = 0; profiles != null && j < profiles.length; j++) {
                    if (profiles[j].getName().equals(activeProfile)){
                        active = profiles[j];
                        break;
                    }
                }
                break;
            }
        }
        if (active != null){
            List<FileObject> l = resolveRelativePathToFileObjects(active.getBootClassPath());
            return ClassPathSupport.createClassPath(l.toArray(new FileObject[l.size()]));
        }
        return null;
    }
    /**
     * @return type of platfrom. Note: type must be unique identifier
     */
    public String getType() {
        return type;
    }

    /**
     * @param type of platfrom
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return maximum class version supported by platform
     */
    public String getClassVersion() {
        return classVersion;
    }

    /**
     * @param classVersion maximum class version for platform
     */
    public void setClassVersion(String classVersion) {
        this.classVersion = classVersion;
    }

    public CDCDevice[] getDevices() {
        return devices;
    }

    public void setDevices(CDCDevice[] devices) {
        this.devices = devices;
    }

    /**
     * This implementation simply reads and parses `java.class.path' property and creates a ClassPath
     * out of it.
     * @return  ClassPath that represents contents of system property java.class.path.
     */
    public ClassPath getStandardLibraries() {
        synchronized (this) {
            ClassPath cp = standardLibs == null ? null : standardLibs.get();
            if (cp != null)
                return cp;
            cp = ClassPathSupport.createClassPath(new ArrayList()); //empty for CDC
            standardLibs = new WeakReference<ClassPath>(cp);
            return cp;
        }
    }

    public String getHomePath() {
        Iterator it = getInstallFolders().iterator();
        if (it.hasNext()){
            FileObject fo = (FileObject) it.next();
            return FileUtil.toFile(fo).getAbsolutePath();
        }
        return null;
    }

    /**
     * Retrieves a collection of {@link org.openide.filesystems.FileObject}s of one or more folders
     * where the Platform is installed. Typically it returns one folder, but
     * in some cases there can be more of them.
     */
    public final Collection<FileObject> getInstallFolders() {
        Collection<FileObject> result = new ArrayList<FileObject> ();
        for (URL url : installFolders ) {
            FileObject root = URLMapper.findFileObject(url);
            if (root != null) {
                result.add (root);
            }
        }
        return result;
    }


    public final FileObject findTool(final String toolName) {
        //throw new IllegalArgumentException("Not to be used here " + toolName);
        //return Util.findTool ("",toolName, this.getInstallFolders());
        System.out.println("findTool: " + toolName);
        return null;
    }

    public final FileObject findTool(final String folder, final String toolName) {
        return Util.findTool (folder, toolName, this.getInstallFolders());
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
    public final List<URL> getJavadocFolders () {
        return this.javadoc;
    }

    public final void setJavadocFolders (List<URL> c) {
        assert c != null;
        List<URL> safeCopy = Collections.unmodifiableList (new ArrayList<URL> (c));
        for (URL url : safeCopy ) {
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
            spec = new Specification (PLATFORM_CDC, new SpecificationVersion(getClassVersion())); //NOI18N
        }
        return spec;
    }

    public Map getProperties() {
        return properties;
    }

    public Collection getInstallFolderURLs () {
        return Collections.unmodifiableList(this.installFolders);
    }

    private static ClassPath createClassPath (List<URL> urls) {
        List<PathResourceImplementation> resources = new ArrayList<PathResourceImplementation> ();
        if (urls != null) {
            for (URL url : urls ) {
                resources.add (ClassPathSupport.createResource (url));
            }
        }
        return ClassPathSupport.createClassPath (resources);
    }

    private FileObject[] getAllLibraries() {
        ArrayList<FileObject> fobs = new ArrayList<FileObject>();
        for (CDCDevice device : devices ) {
            for (CDCDevice.CDCProfile profile : device.getProfiles()) {
                List<FileObject> list=resolveRelativePathToFileObjects(profile.getBootClassPath());
                int lastIndex = -1;
                for (FileObject fo : list)
                {
                    if (!fobs.contains(fo))
                        fobs.add(lastIndex+1,fo);
                    lastIndex = fobs.indexOf(fo);
                }
            }
        }
        return fobs.toArray(new FileObject[fobs.size()]);
    }

    private List<FileObject> resolveRelativePathToFileObjects(String path) {
        ArrayList<FileObject> res = new ArrayList<FileObject>();
        if (path == null)
            return res;
        String paths[] = PropertyUtils.tokenizePath(path);
        for (String pth : paths ) {
            FileObject fo = resolveRelativePathToFileObject(pth.trim());
            if (fo != null)
                res.add(fo);
        }
        return res;
    }

    public FileObject resolveRelativePathToFileObject(String path) {
        if (path == null  ||  path.length () <= 0) return null;
        File f;
        if (path.startsWith (PLATFORM_STRING_PREFIX)){
            FileObject fo = URLMapper.findFileObject(installFolders.iterator().next());
            if (fo == null) return null;
            f = new File (FileUtil.toFile(fo), path.substring (PLATFORM_STRING_PREFIX.length ()));
        } else {
            f = new File (path);
            if (!f.exists()) return null;
        }
        f = FileUtil.normalizeFile (f);
        FileObject fo = FileUtil.toFileObject(f);
        if (fo == null || !FileUtil.isArchiveFile(fo)) return fo;
        return FileUtil.getArchiveRoot(fo);
    }

    public boolean isFatJar() {
        return fatJar;
    }
}
