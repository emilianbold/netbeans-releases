/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
package org.netbeans.modules.javacard.platform;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.Profile;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.modules.javacard.api.JavacardPlatform;
import org.netbeans.modules.propdos.ObservableProperties;
import org.netbeans.modules.javacard.api.ProjectKind;
import static org.netbeans.modules.javacard.constants.JCConstants.JAVACARD_DEVICE_FILE_EXTENSION;
import org.netbeans.modules.javacard.constants.JavacardPlatformKeyNames;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import org.netbeans.modules.javacard.Utils;
import org.netbeans.modules.javacard.constants.JCConstants;
import org.openide.util.NbCollections;

/**
 * Implementation of JavaPlatform for Java Card&trade;.  Most everything
 * delegates to a properties file the vendor is expected to provide.
 * See PropertyNames.PLATFORM_* for how this works.
 *
 * @author Tim Boudreau
 */
public final class JavacardPlatformImpl extends JavacardPlatform {

    private final Properties props;
    private final PropertyChangeListener pcl;
    public JavacardPlatformImpl(Properties props) {
        super (props.getProperty(JavacardPlatformKeyNames.PLATFORM_ID));
        this.props = props;
        if (props instanceof ObservableProperties) {
            ObservableProperties o = (ObservableProperties) props;
            pcl = new PCL();
            o.addPropertyChangeListener(WeakListeners.propertyChange(pcl, o));
        } else {
            pcl = null;
        }
    }

    JavacardPlatformImpl(File root, String name, PlatformInfo info) {
        //This constructor is only used transiently by JavacardPlatformWizardIterator
        //to briefly create an in-memory platform from info gathered in the
        //wizard.  This info is then written to disk and an instance
        //created from the written file which will be able to track changes
        //in the platform properties
        this (toProperties(root, name, info));
    }

    private static Properties toProperties (File root, String name, PlatformInfo info) {
        Properties result = new Properties();
        info.writeTo(result);
        result.setProperty(JavacardPlatformKeyNames.PLATFORM_DEVICE_FILE_EXTENSION, JAVACARD_DEVICE_FILE_EXTENSION);
        result.setProperty(JavacardPlatformKeyNames.PLATFORM_DISPLAYNAME, name);
        result.setProperty(JavacardPlatformKeyNames.PLATFORM_HOME, root.getAbsolutePath());
        return result;
    }

    public File getHome() {
        String home = props.getProperty(JavacardPlatformKeyNames.PLATFORM_HOME);
        return home == null ? null : new File (home);
    }

    @Override
    public boolean isValid() {
        File home = getHome();
        return home != null && home.exists() && home.isDirectory();
    }

    @Override
    public boolean isRI() {
        String pKind = props.getProperty(JavacardPlatformKeyNames.PLATFORM_KIND);
        return pKind == null ? true : JavacardPlatformKeyNames.PLATFORM_KIND_RI.equals(pKind);
    }

    @Override
    public Properties toProperties() {
        return props;
    }

    public void write(FileObject to, Properties settings) throws IOException {
        FileLock lock = to.lock();
        OutputStream out = to.getOutputStream(lock);
        try {
            settings.putAll(toProperties());
            settings.store(out, "Javacard Platform Properties"); //NOI18N
        } finally {
            lock.releaseLock();
            out.close();
        }
    }

    @Override
    public String getDisplayName() {
        return props.getProperty(JavacardPlatformKeyNames.PLATFORM_DISPLAYNAME, "[missing name]");
    }

    @Override
    public String getVendor() {
        String result = props.getProperty(JavacardPlatformKeyNames.PLATFORM_VENDOR);
        if (result == null) {
            return NbBundle.getMessage(JavacardPlatformImpl.class,
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
            //for(Object key : this.props.keySet()) {
            //    result.put(key.toString(), this.props.getProperty(key.toString()));
            //}
        return result;
    }

    private Map <Boolean, ClassPath> bootCps = Collections.synchronizedMap(
            new HashMap<Boolean, ClassPath>());
    public ClassPath getBootstrapLibraries(ProjectKind kind) {
        ClassPath result = bootCps.get(kind.isClassic());
        if (result == null) {
            String bootClasspath;
            if (kind.isClassic()) {
                bootClasspath = props.getProperty(JavacardPlatformKeyNames.PLATFORM_CLASSIC_BOOT_CLASSPATH);
                if (bootClasspath == null) {
                    bootClasspath = props.getProperty(JavacardPlatformKeyNames.PLATFORM_BOOT_CLASSPATH);
                }
            } else {
                bootClasspath = props.getProperty(JavacardPlatformKeyNames.PLATFORM_BOOT_CLASSPATH);
            }
            if (bootClasspath == null || "".equals(bootClasspath.trim())) { //NOI18N
                result = ClassPathSupport.createClassPath(""); //NOI18N
                bootCps.put (kind.isClassic(), result);
            } else {
                result = createClasspath(bootClasspath);
                bootCps.put (kind.isClassic(), result);
            }
        }
        return result;
    }

    @Override
    public boolean equals (Object o) {
        boolean result = o != null && o.getClass() == JavacardPlatformImpl.class;
        if (result) {
            JavacardPlatformImpl other = (JavacardPlatformImpl) o;
            result = (getHome() == null && other.getHome() == null) ||
                    (getHome() != null && other.getHome() != null);
            if (result) {
                result = other.getSystemName().equals(getSystemName());
            }
        }
        return result;
    }

    @Override
    public int hashCode() {
        String uid = props.getProperty(JavacardPlatformKeyNames.PLATFORM_ID);
        return (JavacardPlatformImpl.class.hashCode() * 37) +
            ((getHome() == null ? 0 : getHome().hashCode()) * 11) +
            (getSystemName().hashCode() * 7) + (uid == null ? 0 : uid.hashCode());
    }

    @Override
    public String toString() {
        return super.toString() + '[' +
                Utils.sfsFolderForRegisteredJavaPlatforms() + "/" + //NOI18N
                getSystemName() + "." +
                JCConstants.JAVACARD_PLATFORM_FILE_EXTENSION + //NOI18N
                " (" + propsAsString() + ")]";
    }

    private String propsAsString() {
        //for toString() diagnostics
        StringBuilder sb = new StringBuilder("\n");
        for (String s : NbCollections.checkedSetByFilter(toProperties().keySet(), String.class, false)) {
            sb.append (s);
            sb.append ("=");
            sb.append (props.getProperty(s));
        }
        return sb.toString();
    }

    private ClassPath createClasspathFromProperty (String key) {
        String value = props.getProperty(key);
        if (value == null) {
            return ClassPathSupport.createClassPath("");
        }
        return createClasspath(value);
    }

    private ClassPath createClasspath (String relativePaths) {
        return ClassPathSupport.createClassPath(getURLs(relativePaths));
    }

    private URL[] getURLs(String absolutePaths) {
        String[] entries = absolutePaths.split(File.pathSeparator);
        List<URL> urls = new ArrayList<URL>(entries.length);
        for (String s : entries) {
            File f = new File (s);
            try {
                URL url = f.toURI().toURL();
                if (f.getName().endsWith(".jar")) { //NOI18N
                    String jarURL = "jar:" + url + "!/"; //NOI18N
                    url = new URL (jarURL);
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
        return createClasspathFromProperty(JavacardPlatformKeyNames.PLATFORM_CLASSPATH);
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
    public FileObject findTool(String arg0) {
        return null;
    }

    @Override
    public ClassPath getSourceFolders() {
        return createClasspathFromProperty(JavacardPlatformKeyNames.PLATFORM_SRC_PATH);
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
    public String getPlatformKind() {
        return props.getProperty(JavacardPlatformKeyNames.PLATFORM_KIND);
    }

    private final class PCL implements PropertyChangeListener {

        public void propertyChange(PropertyChangeEvent evt) {
            if (JavacardPlatformKeyNames.PLATFORM_BOOT_CLASSPATH.equals(evt.getPropertyName())) {
                bootCps.clear();
            }
        }
    }

    private final Map<String, String> FAKE_SYSPROPS = new HashMap<String, String>();
    {
        FAKE_SYSPROPS.put("java.specification.vendor", "Sun Microsystems Inc."); //NOI18N
        FAKE_SYSPROPS.put("java.specification.version", "1.6"); //NOI18N
        FAKE_SYSPROPS.put("java.vm.specification.vendor", "Sun Microsystems Inc."); //NOI18N
        FAKE_SYSPROPS.put("os.arch", "unknown"); //NOI18N
        FAKE_SYSPROPS.put("java.vm.specification.name", "Java Card Virtual Machine Specification"); //NOI18N
        FAKE_SYSPROPS.put("java.vm.vendor", "Sun Microsystems Inc."); //NOI18N
        FAKE_SYSPROPS.put("java.runtime.name", "Java Card(TM) Runtime Environment"); //NOI18N
        FAKE_SYSPROPS.put("java.endorsed.dirs", ""); //NOI18N
        FAKE_SYSPROPS.put("java.class.version", "50.0"); //NOI18N
        FAKE_SYSPROPS.put("java.ext.dirs", ""); //NOI18N
        FAKE_SYSPROPS.put("java.version", "1.6.0_10"); //NOI18N
    }
}
