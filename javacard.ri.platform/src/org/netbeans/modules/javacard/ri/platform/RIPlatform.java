/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javacard.ri.platform;

import org.netbeans.modules.javacard.spi.Cards;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.Profile;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.javacard.common.JCConstants;
import org.netbeans.modules.javacard.common.ListenerProxy;
import org.netbeans.modules.javacard.common.Utils;
import org.netbeans.modules.javacard.ri.platform.installer.PlatformInfo;
import org.netbeans.modules.javacard.ri.spi.CardsFactory;
import org.netbeans.modules.javacard.spi.JavacardDeviceKeyNames;
import org.netbeans.modules.javacard.spi.JavacardPlatform;
import org.netbeans.modules.javacard.spi.JavacardPlatformKeyNames;
import org.netbeans.modules.javacard.spi.ProjectKind;
import org.netbeans.modules.propdos.ObservableProperties;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Provider;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.Utilities;
/**
 * Implementation of JavacardPlatform for the Java Card Reference Implementation
 * @author Tim Boudreau
 */
public class RIPlatform extends JavacardPlatform {
    private final Properties props;
    private final Map<Boolean, ClassPath> processorCps = new HashMap<Boolean, ClassPath>();
    private final Map <Boolean, ClassPath> bootCps = new HashMap<Boolean, ClassPath>();
    private final Object cpLock = new Object();
    private ClassPath stdLibs;
    private ClassPath src;
    private final Set<ProjectKind> suppKinds = new HashSet<ProjectKind>(5);
    static boolean inFindDefaultPlatform;

    static {
        //XXX necessary?  Ensures that the global build.properties always
        //has a valid value...
        getDefault();
    }
    private Cards cards = new CardsImpl();
    public RIPlatform(Properties props) {
        this.props = props;
    }

    public RIPlatform(File root, String name, PlatformInfo info) {
        //This constructor is only used transiently by JavacardPlatformWizardIterator
        //to briefly create an in-memory platform from info gathered in the
        //wizard.  This info is then written to disk and an instance
        //created from the written file which will be able to track changes
        //in the platform properties
        this (props(root, name, info));
    }

    @Override
    public void onDelete() throws IOException {
        FileObject fo = Utils.sfsFolderForDeviceConfigsForPlatformNamed(getSystemName(), false);
        if (fo != null) {
            fo.delete();
        }
        fo = Utils.sfsFolderForDeviceEepromsForPlatformNamed(getSystemName(), false);
        if (fo != null) {
            fo.delete();
        }
    }

    @Override
    public Cards getCards() {
        CardsFactory f = CardsFactory.find(getPlatformKind());
        if (f != null) {
            Cards result = f.getCards(Utils.findPlatformDataObjectNamed(
                    getSystemName()).getPrimaryFile());
            if (result != null) {
                return result;
            }
        }
        return cards;
    }

    private static Properties props (File root, String name, PlatformInfo info) {
        Properties p = new Properties();
        p.putAll(toProperties(root, name, info));
        return p;
    }

    public Set<ProjectKind> supportedProjectKinds() {
        if (suppKinds.isEmpty()) {
            String prop = props.getProperty(
                    JavacardPlatformKeyNames.PLATFORM_SUPPORTED_PROJECT_KINDS);
            suppKinds.addAll(ProjectKind.kindsFor(prop, true));
        }
        return suppKinds;
    }

    private static EditableProperties toProperties (File root, String name, PlatformInfo info) {
        EditableProperties result = new EditableProperties(true);
        info.writeTo(result);

        result.setProperty(JavacardPlatformKeyNames.PLATFORM_DEVICE_FILE_EXTENSION, "jcard"); //NOI18N
        result.setProperty(JavacardPlatformKeyNames.PLATFORM_DISPLAYNAME, name);
        result.setProperty(JavacardPlatformKeyNames.PLATFORM_HOME, root.getAbsolutePath());
        return result;
    }

    public boolean isReferenceImplementation() {
        return JavacardPlatformKeyNames.PLATFORM_KIND_RI.equals(props.getProperty(
                JavacardPlatformKeyNames.PLATFORM_KIND));
    }

    public static DataObject findDefaultPlatform(DataObject caller) throws IOException {
        //Pending - always use whatever is the default, or somehow make
        //it explicit what platform is delegated to
        FileObject res = null;
        for (FileObject fo : Utils.findAllRegisteredJavacardPlatformFiles()) {
            if (JCConstants.DEFAULT_JAVACARD_PLATFORM_FILE_NAME.equals(fo.getName())) {
                res = fo;
                break;
            } else {
                DataObject ob = DataObject.find(fo);
                if (caller == ob) {
                    continue;
                }
                JavacardPlatform p = ob.getNodeDelegate().getLookup().lookup(RIPlatform.class);
                if (p != null && JavacardPlatformKeyNames.PLATFORM_KIND_RI.equals(p.getPlatformKind())) {
                    res = fo;
                }
            }
        }
        return res == null ? null : DataObject.find(res);

    }


    /**
     * Finds the default instance of JavacardPlatform.  This is defined as
     * the value set in the global (userdir) build.properties for the key
     * JCConstants.GLOBAL_BUILD_PROPERTIES_RI_PROPERTIES_PATH_KEY.
     * If not set, this method will cause the property to become set,
     * and set it to the highest javacard spec version platform installed
     * which identifies itself as the reference implementation.
     * @return A JavacardPlatform.  If no JavacardPlatform is actually installed,
     * returns a dummy invalid instance.  Never returns null.
     */
    public static JavacardPlatform getDefault() {
        File riProps = null;
        try {
            riProps = ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<File>() {

                public File run() throws Exception {
                    EditableProperties globals = PropertyUtils.getGlobalProperties();
                    String path = globals.getProperty(JCConstants.GLOBAL_BUILD_PROPERTIES_RI_PROPERTIES_PATH_KEY);
                    File result = path == null ? null : new File(path);
                    if (result != null && (!result.exists() || !result.isFile())) {
                        result = null;
                    }
                    if (result == null) { //last known platform doesn't exist or was deleted
                        SpecificationVersion last = new SpecificationVersion("0.0"); //NOI18N
                        FileObject target = null;
                        FileObject platformFolder = Utils.sfsFolderForRegisteredJavaPlatforms();
                        //Find the highest numbered RI platform in the case of multiple
                        for (FileObject child : platformFolder.getChildren()) {
                            DataObject dob = DataObject.find(child);
                            RIPlatform ri = dob.getLookup().lookup(RIPlatform.class);
                            if (ri != null && ri.isReferenceImplementation()) {
                                SpecificationVersion ver = ri.getJavacardVersion();
                                if (ver.compareTo(last) >= 0) {
                                    target = child;
                                }
                            }
                        }
                        if (target != null) {
                            result = FileUtil.toFile(target);
                            globals.setProperty(
                                    JCConstants.GLOBAL_BUILD_PROPERTIES_RI_PROPERTIES_PATH_KEY,
                                    result.getAbsolutePath());
                            PropertyUtils.putGlobalProperties(globals);
                        }
                    }
                    return result;
                }
            });
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (riProps == null) return brokenPlatform(); //NOI18N
        FileObject fo = FileUtil.toFileObject(riProps);
        if (fo == null) return brokenPlatform(); //NOI18N
        try {
            DataObject dob = DataObject.find(fo);
            JavacardPlatform result = dob.getNodeDelegate().getLookup().lookup(JavacardPlatform.class);
            return result;
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        return brokenPlatform(); //NOI18N
    }

    private static JavacardPlatform brokenPlatform() {
        return JavacardPlatform.createBrokenJavacardPlatform("none"); //NOI18N
    }

    private List<? extends Provider> getCardProviders() {
        String nm = getSystemName();
        FileObject fld  = Utils.sfsFolderForDeviceConfigsForPlatformNamed(nm, true);
        DataFolder df = DataFolder.findFolder(fld);
        String ext = props.getProperty(JavacardPlatformKeyNames.PLATFORM_DEVICE_FILE_EXTENSION);
        ext = ext == null ? "jcard" : ext; //NOI18N
        DataObject[] kids = df.getChildren();
        List <Provider> result = new ArrayList<Provider>(kids.length);
        for (DataObject dob : kids) {
            FileObject fo = dob.getPrimaryFile();
            if (fo.isData() && ext.equalsIgnoreCase(fo.getExt())) {
                result.add(dob);
            }
        }
        return result;
    }

    @Override
    public Properties toProperties() {
        return props;
    }

    @Override
    public String getPlatformKind() {
        return props.getProperty(JavacardPlatformKeyNames.PLATFORM_KIND, "RI");
    }

    public File getHome() {
        String home = props.getProperty(JavacardPlatformKeyNames.PLATFORM_HOME);
        return home == null ? null : new File (home);
    }

    private static ClassPath createClasspathFromProperty (String key, Properties props) {
        return ClassPathFactory.createClassPath(new PropertyClassPathImpl(key, props));
    }
    
    private static URL[] getURLs(String absolutePaths) {
        String[] entries = absolutePaths.split(File.pathSeparator);
        List<URL> urls = new ArrayList<URL>(entries.length);
        for (String s : entries) {
            File f = new File (s);
            try {
                URL url = f.toURI().toURL();
                if (f.getName().endsWith(".jar")) { //NOI18N
                    String jarURL = "jar:" + url + "!/"; //NOI18N
                    url = new URL (jarURL);
                } else if (!url.toString().endsWith("/")) {
                    //path to src/ subdir in some distros will not exist
                    //Manually append a / so SimplePathResourceImplementation
                    //does not throw an exception
                    url = new URL (url.toString() + "/");
                }
                urls.add (url);
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        URL[] results = urls.toArray(new URL[urls.size()]);
        return results;
    }

    @Override
    public ClassPath getBootstrapLibraries() {
        return getBootstrapLibraries(ProjectKind.EXTENDED_APPLET);
    }

    @Override
    public ClassPath getStandardLibraries() {
        synchronized (cpLock) {
            if (stdLibs == null) {
                stdLibs = createClasspathFromProperty(JavacardPlatformKeyNames.PLATFORM_CLASSPATH, props);
            }
            return stdLibs;
        }
    }

    @Override
    public ClassPath getProcessorClasspath(ProjectKind kind) {
        synchronized (cpLock) {
            ClassPath result = processorCps.get(kind.isClassic());
            if (result == null) {
                result = createClasspathFromProperty(kind.isClassic() ?
                    JavacardPlatformKeyNames.PLATFORM_PROCESSOR_CLASSIC_CLASSPATH :
                    JavacardPlatformKeyNames.PLATFORM_PROCESSOR_EXT_CLASSPATH, props);
                processorCps.put(kind.isClassic(), result);
            }
        return result;
        }
    }

    @Override
    public ClassPath getBootstrapLibraries(ProjectKind kind) {
        synchronized (cpLock) {
            ClassPath result = bootCps.get(kind.isClassic());
            if (result == null) {
                String prop = kind.isClassic() ?
                    JavacardPlatformKeyNames.PLATFORM_CLASSIC_BOOT_CLASSPATH :
                    JavacardPlatformKeyNames.PLATFORM_BOOT_CLASSPATH;
                result = createClasspathFromProperty(prop, props);
                bootCps.put (kind.isClassic(), result);
            }
            return result;
        }
    }

    @Override
    public Collection<FileObject> getInstallFolders() {
        if (isValid()) {
            return Collections.singleton(FileUtil.toFileObject(FileUtil.normalizeFile(getHome())));
        } else {
            return Collections.emptySet();
        }
    }

    @Override
    public FileObject findTool(String tool) {
        File home = getHome();
        if (TOOL_EMULATOR.equals(tool)) {
            String path = props.getProperty(JavacardPlatformKeyNames.PLATFORM_DEBUG_PROXY); //NOI18N
            if (path == null) {
                File f = new File (new File(home, "bin"), Utilities.isWindows() ? "debugproxy.bat" : "debugproxy.sh"); //NOI18N
                return FileUtil.toFileObject(FileUtil.normalizeFile(f));
            }
            return FileUtil.toFileObject(FileUtil.normalizeFile(new File (path)));
        } else if (TOOL_DEBUG_PROXY.equals(tool)) {
            String path = props.getProperty(
                JavacardPlatformKeyNames.PLATFORM_EMULATOR_PATH);
            if (path == null) {
                File f = new File (new File(home, "bin"), Utilities.isWindows() ? "cjcre.exe" : "cjcre"); //NOI18N
                return FileUtil.toFileObject(FileUtil.normalizeFile(f));
            }
        }
        return null;
    }

    @Override
    public ClassPath getSourceFolders() {
        synchronized (cpLock) {
            if (src == null) {
                src = createClasspathFromProperty(JavacardPlatformKeyNames.PLATFORM_SRC_PATH, props);
            }
            return src;
        }
    }

    @Override
    public List<URL> getJavadocFolders() {
        String docFolders = props.getProperty(JavacardPlatformKeyNames.PLATFORM_JAVADOC_PATH);
        if (docFolders == null || "".equals(docFolders)) { //NOI18N
            return Collections.emptyList();
        }
        return Arrays.asList(getURLs(docFolders));
    }

    @Override
    public boolean isValid() {
        File home = getHome();
        boolean result = home != null && home.exists() && home.isDirectory();
        return result;
    }

    @Override
    public String getDisplayName() {
        return props.getProperty(JavacardPlatformKeyNames.PLATFORM_DISPLAYNAME, "[missing name]");
    }

    @Override
    public String getSystemName() {
        return props.getProperty(JavacardPlatformKeyNames.PLATFORM_ID);
    }

    @Override
    public SpecificationVersion getJavacardVersion() {
        String ver = props.getProperty(JavacardDeviceKeyNames.DEVICE_JAVACARD_VERSION);
        return ver == null ? new SpecificationVersion("3.0") : new SpecificationVersion(ver);
    }

    @Override
    public boolean isVersionSupported(SpecificationVersion javacardVersion) {
        return javacardVersion.compareTo(3) >= 0;
    }

    @Override
    public String getVendor() {
        String result = props.getProperty(JavacardPlatformKeyNames.PLATFORM_VENDOR);
        if (result == null) {
            return NbBundle.getMessage(JavacardPlatform.class,
                "UNKNOWN_VENDOR"); //NOI18N
        } else {
            return result;
        }
    }

    @Override
    public Specification getSpecification() {
        String profileName = props.getProperty(JavacardPlatformKeyNames.PLATFORM_PROFILE);
        String profileVersion = props.getProperty(JavacardPlatformKeyNames.PLATFORM_PROFILE_MAJOR_VERSION, "1") + '.' + //NOI18N
                props.getProperty(JavacardPlatformKeyNames.PLATFORM_PROFILE_MINOR_VERSION, "0"); //NOI18N
        Profile profile = profileName == null ? null : new Profile (profileName,
                new SpecificationVersion(profileVersion));
        String platformVersion = props.getProperty(JavacardPlatformKeyNames.PLATFORM_MAJORVERSION, "1") + "." + //NOI18N
                props.getProperty(JavacardPlatformKeyNames.PLATFORM_MINORVERSION, "0"); //NOI18N
        SpecificationVersion specVer = new SpecificationVersion (platformVersion);
        Specification result =  profile == null ? new Specification (JavacardPlatformKeyNames.PLATFORM_SPECIFICATION_NAME, specVer) :
            new Specification (JavacardPlatformKeyNames.PLATFORM_SPECIFICATION_NAME, specVer, new Profile[] { profile });
        return result;
    }

    @Override
    public Map<String, String> getProperties() {
        Map<String, String> result = new HashMap<String, String>(FAKE_SYSPROPS);
        result.put("bootclasspath", //NOI18N
            this.props.getProperty(JavacardPlatformKeyNames.PLATFORM_BOOT_CLASSPATH));
        return result;
    }

    private final Map<String, String> FAKE_SYSPROPS = new HashMap<String, String>();
    {
        FAKE_SYSPROPS.put("java.specification.vendor", "Sun Microsystems Inc."); //NOI18N
        FAKE_SYSPROPS.put("java.specification.version", "1.6"); //NOI18N
        FAKE_SYSPROPS.put("java.vm.specification.vendor", "Sun Microsystems Inc."); //NOI18N
        FAKE_SYSPROPS.put("os.arch", "unknown"); //NOI18N
        FAKE_SYSPROPS.put("java.vm.specification.name", "Java Card Virtual Machine Specification"); //NOI18N
        FAKE_SYSPROPS.put("java.vm.vendor", "Sun Microsystems Inc."); //NOI18N
        FAKE_SYSPROPS.put("java.runtime.name", "Java Card Runtime Environment"); //NOI18N
        FAKE_SYSPROPS.put("java.endorsed.dirs", ""); //NOI18N
        FAKE_SYSPROPS.put("java.class.version", "50.0"); //NOI18N
        FAKE_SYSPROPS.put("java.ext.dirs", ""); //NOI18N
        FAKE_SYSPROPS.put("java.version", "1.6.0_10"); //NOI18N
    }

    private class CardsImpl extends Cards implements FileChangeListener {

        @Override
        protected void addNotify() {
            super.addNotify();
            FileObject fld = getFolder();
            if (fld != null) {
                fld.addFileChangeListener(this);
            }
        }

        @Override
        protected void removeNotify() {
            FileObject fld = getFolder();
            if (fld != null) {
                fld.removeFileChangeListener(this);
            }
            super.removeNotify();
        }

        private FileObject getFolder() {
            String nm = getSystemName();
            FileObject fld  = Utils.sfsFolderForDeviceConfigsForPlatformNamed(nm, true);
            if (fld != null && fld.isValid()) {
                return fld;
            }
            return null;
        }

        @Override
        public List<? extends Lookup.Provider> getCardSources() {
            return getCardProviders();
        }

        public void fileFolderCreated(FileEvent fe) {
            //do nothing
        }

        public void fileDataCreated(FileEvent fe) {
            fireChange();
        }

        public void fileChanged(FileEvent fe) {
            fireChange();
        }

        public void fileDeleted(FileEvent fe) {
            fireChange();
        }

        public void fileRenamed(FileRenameEvent fe) {
            fireChange();
        }

        public void fileAttributeChanged(FileAttributeEvent fe) {
            //do nothing
        }
    }

    public static class PropertyClassPathImpl extends ListenerProxy<Properties> implements ClassPathImplementation {
        private final String property;
        private final List<PathResourceImplementation> resources = new ArrayList<PathResourceImplementation>();
        private volatile boolean attached;
        PropertyClassPathImpl (String property, Properties props) {
            super (props);
            this.property = property;
        }

        @Override
        public List<? extends PathResourceImplementation> getResources() {
            synchronized (this) {
                if (resources.isEmpty()) {
                    resources.addAll(refresh());
                }
                return new LinkedList<PathResourceImplementation>(resources);
            }
        }

        @Override
        protected void attach(Properties props, PropertyChangeListener precreatedListener) {
            if (props instanceof ObservableProperties) {
                ObservableProperties ops = (ObservableProperties) props;
                ops.addPropertyChangeListener(precreatedListener);
            }
            attached = true;
        }

        @Override
        protected void detach(Properties props, PropertyChangeListener precreatedListener) {
            if (props instanceof ObservableProperties) {
                ObservableProperties ops = (ObservableProperties) props;
                ops.removePropertyChangeListener(precreatedListener);
            }
            attached = false;
        }

        @Override
        protected void onChange(String prop, Object old, Object nue) {
            if (property.equals(prop)) {
                List<PathResourceImplementation> oldRes = new ArrayList<PathResourceImplementation>();
                List<PathResourceImplementation> curr;
                boolean fire;
                synchronized(this) {
                    oldRes.addAll(resources);
                    resources.clear();
                    resources.addAll(refresh());
                    fire = !oldRes.equals(resources);
                    curr = !fire ? null : new ArrayList<PathResourceImplementation>(resources);
                }
                if (fire) {
                    fire(PROP_RESOURCES, oldRes, curr);
                }
            }
        }

        private List<PathResourceImplementation> refresh() {
            List<PathResourceImplementation> result = new ArrayList<PathResourceImplementation>();
            String value = get().getProperty(property);
            if (value == null) {
                return result;
            } else {
                URL[] urls = getURLs(value);
                for (URL u : urls) {
                    PRI pri = new PRI(u);
                    result.add (pri);
                }
            }
            return result;
        }

        private final class PRI implements PathResourceImplementation {
            private final URL url;
            PRI(URL url) {
                this.url = url;
                Parameters.notNull("url", url);
            }

            @Override
            public URL[] getRoots() {
                return new URL[] { url };
            }

            @Override
            public ClassPathImplementation getContent() {
                return PropertyClassPathImpl.this;
            }

            @Override
            public void addPropertyChangeListener(PropertyChangeListener listener) {
                //do nothing
            }

            @Override
            public void removePropertyChangeListener(PropertyChangeListener listener) {
                //do nothing
            }

            @Override
            public boolean equals(Object obj) {
                if (obj == null) {
                    return false;
                }
                if (getClass() != obj.getClass()) {
                    return false;
                }
                final PRI other = (PRI) obj;
                if (this.url != other.url && (this.url == null || !this.url.toString().equals(other.url.toString()))) {
                    return false;
                }
                return true;
            }

            @Override
            public int hashCode() {
                return url.toString().hashCode();
            }
        }
    }
}
