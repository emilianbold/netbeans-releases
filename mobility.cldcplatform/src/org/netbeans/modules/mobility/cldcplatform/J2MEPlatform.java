/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.mobility.cldcplatform;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.Profile;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Implementation of the JavaPlatform API class, which serves proper
 * bootstrap classpath information.
 */
public final class J2MEPlatform extends JavaPlatform {
    
    public static final String SPECIFICATION_NAME = "j2me"; // NOI18N
    private static final String URL_SEPARATOR = ","; // NOI18N
    private static final String PATH_SEPARATOR = ":"; // NOI18N
    public static final String PLATFORM_STRING_PREFIX = "${platform.home}/"; // NOI18N
    
    private ClassPath bootstrapLibs, standardLibs;
    private Specification spec;
    
    private String name;
    private final String type;
    private String preverifyCmd;
    private String runCmd;
    private String debugCmd;
    private Device[] devices;
    private final File homeFile;
    private FileObject home;
    private String displayName;
    private List<URL> javadocs;
    private ClassPath sources;
    
    public J2MEPlatform(String name, String home, String type, String displayName, String srcPath, String docPath, String preverifyCmd, String runCmd, String debugCmd, Device[] devices) {
        assert name != null;
        this.name = name;
        assert home != null;
        this.homeFile = FileUtil.normalizeFile(new File(home));
        this.home = FileUtil.toFileObject(homeFile);
        assert type != null;
        this.type = type;
        this.displayName = displayName == null ? name : displayName;
        this.preverifyCmd = preverifyCmd;
        this.runCmd = runCmd;
        this.debugCmd = debugCmd;
        assert devices != null;
        assert devices.length > 0;
        this.devices = devices;
        this.sources = ClassPathSupport.createClassPath(resolveRelativePathToFileObjects(srcPath).toArray(new FileObject[0]));
        this.javadocs = resolveRelativePathToURLs(docPath);
    }
    
    public Device[] getDevices() {
        return devices == null ? null : devices.clone();
    }
    
    public synchronized void setDevices(final Device[] devices) {
        this.devices = devices;
        spec = null;
        bootstrapLibs = ClassPathSupport.createClassPath(getAllLibraries());
        firePropertyChange("devices", null, null); //NOI18N
        firePropertyChange("bootstrapLibraries", null, null); //NOI18N
    }
    
    private List<URL> resolveRelativePathToURLs(final String path) {
        final List<URL> array = new ArrayList<URL>();
        if (path == null)
            return array;
        final StringTokenizer stk = new StringTokenizer(path, URL_SEPARATOR);
        while (stk.hasMoreTokens()) {
            final URL url = resolveRelativePathToURL(stk.nextToken().trim());
            if (url != null)
                array.add(url);
        }
        return array;
    }
    
    private URL getURL(final String fragment) throws MalformedURLException {
        try {
            final URL url = new URL(fragment);
            if (fragment.equals(url.toExternalForm())) {
                return url;
            }
        } catch (MalformedURLException e) {}
        return (fragment.startsWith(PLATFORM_STRING_PREFIX) ? new File(homeFile, fragment.substring(PLATFORM_STRING_PREFIX.length())) : new File(fragment)).toURI().toURL();
    }
    
    public URL resolveRelativePathToURL(final String path) {
        if (path == null  ||  path.length() <= 0) return null;
        try {
            final URL url = getURL(path);
            if (!FileUtil.isArchiveFile(url)) {
                final String s = url.toExternalForm();
                if (s.endsWith("/")) return url; //NOI18N
                return new URL(s + '/');
            }
            return FileUtil.getArchiveRoot(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static URL localfilepath2url(final String path) {
        try {
            return new File(path).toURI().toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static URL localfilepath2url(final File file) {
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private List<FileObject> resolveRelativePathToFileObjects(final String path) {
        final ArrayList<FileObject> res = new ArrayList<FileObject>();
        if (path == null)
            return new ArrayList<FileObject>();
        final String paths[] = PropertyUtils.tokenizePath(path);
        for (int a = 0; a < paths.length; a ++) {
            final FileObject fo = resolveRelativePathToFileObject(paths[a].trim());
            if (fo != null)
                res.add(fo);
        }
        return res;
    }
    
    public FileObject resolveRelativePathToFileObject(final String path) {
        if (path == null  ||  path.length() <= 0) return null;
        File f;
        if (path.startsWith(PLATFORM_STRING_PREFIX))
            f = new File(homeFile, path.substring(PLATFORM_STRING_PREFIX.length()));
        else
            f = new File(path);
        f = FileUtil.normalizeFile(f);
        final FileObject fo = FileUtil.toFileObject(f);
        if (fo == null || !FileUtil.isArchiveFile(fo)) return fo;
        return FileUtil.getArchiveRoot(fo);
    }
    
    public static String getFilePath(final FileObject fo) {
        if (fo == null)
            return null;
        FileObject ff = FileUtil.getArchiveFile(fo);
        if (ff == null)
            ff = fo;
        final File file = FileUtil.toFile(ff);
        return (file != null) ? file.getAbsolutePath() : null;
    }
    
    public String getAllLibrariesString() {
        final FileObject[] files = getAllLibraries();
        if (files == null  ||  files.length <= 0)
            return ""; //NOI18N
        final StringBuffer sb = new StringBuffer();
        boolean first = true;
        for (int a = 0; a < files.length; a ++) {
            final String value = getFilePath(files[a]);
            if (value == null) {
                continue;
            }
            if (first)
                first = false;
            else
                sb.append(':'); //NOI18N
            sb.append(value);
        }
        return sb.toString();
    }
    
    /**
     * implements JavaPlatform
     * @return empty ClassPath
     */
    public ClassPath getStandardLibraries() {
        if (standardLibs == null) {
            standardLibs = ClassPathSupport.createClassPath(new ArrayList<PathResourceImplementation>());
        }
        return standardLibs;
    }
    
    /**
     * implements JavaPlatform
     * @return complete ClassPath over all APIs and all devices
     */
    public ClassPath getBootstrapLibraries() {
        if (bootstrapLibs == null) {
            bootstrapLibs = ClassPathSupport.createClassPath(getAllLibraries());
        }
        return bootstrapLibs;
    }
    
    private FileObject[] getAllLibraries() {
        final HashSet<FileObject> fobs = new HashSet<FileObject>();
        for (int i=0; i<devices.length; i++) {
            final J2MEProfile[] profiles = devices[i].getProfiles();
            for (int j=0; j<profiles.length; j++) {
                fobs.addAll(resolveRelativePathToFileObjects(profiles[j].getClassPath()));
            }
        }
        return fobs.toArray(new FileObject[fobs.size()]);
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }
    
    public void setName(final String name) {
        this.name = name;
    }
    
    public String toString() {
        return getDisplayName();
    }
    
    public String getVendor() {
        return displayName;
    }
    
    public String getName() {
        return name;
    }
    
    public String getType() {
        return type;
    }
    
    /**
     * Getter for property preverifyCmd.
     *
     * @return Value of property preverifyCmd.
     */
    public String getPreverifyCmd() {
        return this.preverifyCmd;
    }
    
    /**
     * Getter for property runCmd.
     *
     * @return Value of property runCmd.
     */
    public String getRunCmd() {
        return this.runCmd;
    }
    
    /**
     * Getter for property debugCmd.
     *
     * @return Value of property debugCmd.
     */
    public String getDebugCmd() {
        return this.debugCmd;
    }
    
    public String getHomePath() {
        return homeFile.getAbsolutePath();
    }

    private boolean hasEssentialTools(Collection<FileObject> folders) {
        return findTool("emulator", folders)!=null && findTool("preverify", folders)!=null; //NOI18N
    }
    
    public Collection<FileObject> getInstallFolders() {
        if (home == null) home = FileUtil.toFileObject(homeFile);
        Collection<FileObject> folders;
        if(home != null && home.isValid() && home.isFolder()) {
            folders = Collections.singleton(home);
            return hasEssentialTools(folders) ? folders : Collections.EMPTY_SET;
        }
        return Collections.EMPTY_SET;
    }
    
    public List<URL> getJavadocFolders() {
        return Collections.unmodifiableList(javadocs);
    }
    
    public void setJavadocFolders(final List<URL> javadocs) {
        this.javadocs = new ArrayList<URL>(javadocs);
        firePropertyChange("javadocFolders", null, null); //NOI18N
    }
    
    private String toRelativePaths(final List<? extends Object> path) {
        final StringBuffer sb = new StringBuffer();
        String homePath = homeFile.getAbsolutePath();
        if (Utilities.isWindows())
            homePath = "/" + homePath.toLowerCase().replace('\\', '/'); //NOI18N
        for ( final Object o : path ) {
            if (o instanceof FileObject) {
                FileObject fo = FileUtil.getArchiveFile((FileObject) o);
                if (fo == null) fo = (FileObject) o;
                if (home != null  &&  FileUtil.isParentOf(home, fo)) {
                    String tmp = FileUtil.getRelativePath(home, fo);
                    if (tmp != null) {
                        if (tmp.startsWith("\\")  ||  tmp.startsWith("/")) //NOI18N
                            tmp = tmp.substring(1);
                        sb.append(PLATFORM_STRING_PREFIX).append(tmp).append(PATH_SEPARATOR);
                    }
                } else {
                    final File f = FileUtil.toFile(fo);
                    if (f != null) {
                        sb.append(f.getAbsolutePath()).append(PATH_SEPARATOR);
                    }
                }
            } else if (o instanceof URL) {
                final URL url = (URL) o;
                try {
                    FileObject fo = URLMapper.findFileObject(url);
                    if (fo != null  &&  home != null  &&  FileUtil.isParentOf(home, fo)) {
                        String tmp = FileUtil.getRelativePath(home, fo);
                        if (tmp != null) {
                            if (tmp.startsWith("\\")  ||  tmp.startsWith("/")) //NOI18N
                                tmp = tmp.substring(1);
                            sb.append(PLATFORM_STRING_PREFIX).append(tmp).append(URL_SEPARATOR);
                        }
                    } else
                        sb.append(url.toString()).append(URL_SEPARATOR);
                } catch (Exception e) {}
            }
        }
        return sb.toString();
    }
    
    public String getJavadocPath() {
        return toRelativePaths(javadocs);
    }
    
    public ClassPath getSourceFolders() {
        return sources;
    }
    
    public void setSourceFolders(final List<FileObject> sources) {
        this.sources = ClassPathSupport.createClassPath(sources.toArray(new FileObject[sources.size()]));
        firePropertyChange("sourceFolders", null, null); //NOI18N
    }
    
    public String getSourcePath() {
        return toRelativePaths(Arrays.asList(sources.getRoots()));
    }
    
    public Map<String,String> getProperties() {
        return Collections.singletonMap("platform.ant.name", getName()); //NOI18N
    }
    
    public synchronized Specification getSpecification() {
        if (spec == null) {
            final HashSet<J2MEProfile> profs = new HashSet<J2MEProfile>();
            for (int i=0; i<devices.length; i++) {
                final J2MEProfile[] profiles = devices[i].getProfiles();
                for (int j=0; j<profiles.length; j++)
                    profs.add(profiles[j]);
            }
            spec = new Specification(SPECIFICATION_NAME, null, NbBundle.getMessage(J2MEPlatform.class, "TXT_J2MEDisplayName"), profs.toArray(new Profile[profs.size()]));
        }
        return spec;
    }
    
    public boolean isValid() {
        if (getInstallFolders().isEmpty() || devices == null) return false;
        for (int i=0; i<devices.length; i++) if (devices[i].isValid()) return true;
        return false;
    }
    
    public static final class Device {
        
        private final String name;
        private final String description;
        private final String[] securityDomains;
        private final J2MEProfile[] profiles;
        private final Screen screen;
        private final String[] pathOrder;
        
        public Device(String name, String description, String[] securityDomains, J2MEProfile[] profiles, Screen screen) {
            this.screen = screen;
            assert name != null;
            this.name = name;
            this.description = description == null ? name : description;
            this.securityDomains = securityDomains;
            assert profiles != null;
            this.profiles = profiles;
            
            //following algorithm tries to determine if the profile has a unique jar on classpath that makes it realy optional (un-selectable)
            //and to determine physical path order to keep higher version of API first
            Set<String> paths[] = new Set[profiles.length];
            HashMap<String,HashMap<String,SpecificationVersion>> allPaths = new HashMap<String,HashMap<String,SpecificationVersion>>();
            HashSet<String> doubledPaths = new HashSet<String>();
            for (int i=0; i<paths.length; i++) {
                paths[i] = new HashSet<String>();
                String s[] = PropertyUtils.tokenizePath(profiles[i].getClassPath());
                for (int j=0; j<s.length; j++) {
                    paths[i].add(s[j]);
                    HashMap<String,SpecificationVersion> apis = allPaths.get(s[j]);
                    if (apis == null) {
                        apis = new HashMap<String,SpecificationVersion>();
                        allPaths.put(s[j], apis);
                    } else {
                        doubledPaths.add(s[j]);
                    }
                    apis.put(profiles[i].getName(), profiles[i].getVersion());
                }
            }
            for (int i=0; i<paths.length; i++) {
                profiles[i].realyOptional = paths[i].retainAll(doubledPaths);
            }
            this.pathOrder = new String[allPaths.size()];
            for (int i=0; i<pathOrder.length; i++) {
                String first = findFirst(allPaths);
                pathOrder[i] = first;
                allPaths.remove(first);
            }
        }
        
        public String sortClasspath(final String classpath) {
            final HashSet<String> path = new HashSet<String>(Arrays.asList(PropertyUtils.tokenizePath(classpath)));
            final StringBuffer newPath = new StringBuffer();
            for (int i=0; i<pathOrder.length; i++) {
                if (path.remove(pathOrder[i])) {
                    if (newPath.length() > 0) newPath.append(':');
                    newPath.append(pathOrder[i]);
                }
            }
            for (String str : path) {
                if (newPath.length() > 0) newPath.append(':');
                newPath.append(str);
            }
            return newPath.toString();
        }
        
        /**
         * Getter for property name.
         * @return Value of property name.
         */
        public String getName() {
            return this.name;
        }
        
        public String toString() {
            return getName();
        }
        
        /**
         * Getter for property description.
         * @return Value of property description.
         */
        public String getDescription() {
            return this.description;
        }
        
        /**
         * Getter for property securitydomains.
         * @return Value of property securitydomains.
         */
        public String[] getSecurityDomains() {
            return securityDomains == null ? null : (String[])this.securityDomains.clone();
        }
        
        /**
         * Getter for property profiles.
         * @return Value of property profiles.
         */
        public J2MEProfile[] getProfiles() {
            return profiles == null ? null : (J2MEProfile[])this.profiles.clone();
        }
        
        public Screen getScreen() {
            return screen;
        }
        
        public boolean isValid() {
            if (profiles == null) return false;
            boolean cfg = false, prof = false;
            for (int i=0; i<profiles.length; i++) {
                cfg = cfg || profiles[i].getType().equals(J2MEProfile.TYPE_CONFIGURATION);
                prof = prof || profiles[i].getType().equals(J2MEProfile.TYPE_PROFILE);
            }
            return cfg && prof;
        }
        
        //finds the API that can be ordered as the first of all the given with preference to put CLDC and MIDP at the end
        private String findFirst(final HashMap<String,HashMap<String,SpecificationVersion>> allPaths) {
            Map.Entry<String,HashMap<String,SpecificationVersion>> last=null;
            for (final Map.Entry<String,HashMap<String,SpecificationVersion>> me : allPaths.entrySet() ) {
                final Map<String,SpecificationVersion> apis = me.getValue();
                if (!apis.containsKey("CLDC") && !apis.containsKey("MIDP") && canBeFirst(apis, allPaths)) return me.getKey(); //NOI18N
                last = me;
            }
            
            for ( final Map.Entry<String,HashMap<String,SpecificationVersion>> me : allPaths.entrySet() ) {
                if (canBeFirst(me.getValue(), allPaths)) return me.getKey();
                last=me;
            }
            return last.getKey(); //error case - return anything
        }
        
        //tests if given map of APIs can be ordered prior all the other APIs
        private boolean canBeFirst(final Map<String,SpecificationVersion> map, final HashMap<String,HashMap<String,SpecificationVersion>> allPaths) {
            for (HashMap<String,SpecificationVersion> svmap : allPaths.values()) {
                if (!isValidOrder(map, svmap)) return false;
            }
            return true;
        }
        
        //this method should return false only when there is possible ordering and it is opposite then given arguments
        //all the other cases return true (equal versions of the same API, cross-dependencies, valid order, no common APIs,...)
        private boolean isValidOrder(final Map<String,SpecificationVersion> firstAPImap, final HashMap<String,SpecificationVersion> secondAPImap) {
            final HashSet<String> hs = new HashSet<String>(firstAPImap.keySet());
            hs.retainAll(secondAPImap.keySet());
            if (hs.isEmpty()) return true;
            for ( final String api : hs ) {
                if (firstAPImap.get(api).compareTo(secondAPImap.get(api)) >= 0) return true;
            }
            return false;
        }
    }
    
    public static final class Screen {
        
        private final Integer width;
        private final Integer height;
        private final Integer bitDepth;
        private final Boolean color;
        private final Boolean touch;
        
        public Screen(String width, String height, String bitDepth, String color, String touch) {
            Object o;
            try {
                o = new Integer(width);
            } catch (NumberFormatException e) {
                o = null;
            }
            this.width = (Integer) o;
            
            try {
                o = new Integer(height);
            } catch (NumberFormatException e) {
                o = null;
            }
            this.height = (Integer) o;
            
            try {
                o = new Integer(bitDepth);
            } catch (NumberFormatException e) {
                o = null;
            }
            this.bitDepth = (Integer) o;
            
            try {
                o = new Boolean(color);
            } catch (NumberFormatException e) {
                o = null;
            }
            this.color = (Boolean) o;
            
            try {
                o = new Boolean(touch);
            } catch (NumberFormatException e) {
                o = null;
            }
            this.touch = (Boolean) o;
        }
        
        public Integer getBitDepth() {
            return bitDepth;
        }
        
        public Boolean getColor() {
            return color;
        }
        
        public Integer getHeight() {
            return height;
        }
        
        public Boolean getTouch() {
            return touch;
        }
        
        public Integer getWidth() {
            return width;
        }
        
        public boolean equals(Object obj) {
            if (obj instanceof Screen) {
                Integer width = ((Screen) obj).getWidth();
                Integer height = ((Screen) obj).getHeight();
                Integer bitDepth = ((Screen) obj).getBitDepth();
                Boolean color = ((Screen) obj).getColor();
                Boolean touch = ((Screen) obj).getTouch();
                boolean ret = true;
                
                ret &= width != null ? width.equals(this.width) : false;
                ret &= height != null ? height.equals(this.height) : false;
                ret &= bitDepth != null ? bitDepth.equals(this.bitDepth) : false;
                ret &= color != null ? color.equals(this.color) : false;
                ret &= touch != null ? touch.equals(this.touch) : false;
                return ret;
            }
            return false;
        }
    }
    
    public static final class J2MEProfile extends Profile implements Comparable {
        
        public static final String TYPE_CONFIGURATION = "configuration"; //NOI18N
        public static final String TYPE_PROFILE = "profile"; //NOI18N
        public static final String TYPE_OPTIONAL = "optional"; //NOI18N
        
        private static final MessageFormat DISPLAY_NAME_WITH_VERSION_FORMAT = new MessageFormat(NbBundle.getMessage(J2MEPlatform.class, "FMT_J2MEPlatform_J2MEProfile_DisplayNameWithVersion")); //NOI18N
        
        private final String displayName;
        private final String displayNameWithVersion;
        private final String type;
        private final String dependencies;
        private final String classPath;
        private final boolean def;
        boolean realyOptional;
        
        public J2MEProfile(String name, String version, String displayName, String type, String dependencies, String classPath, boolean def) {
            this(name, new SpecificationVersion(version), displayName, type, dependencies, classPath, def);
        }
        
        J2MEProfile(String name, SpecificationVersion version, String displayName, String type, String dependencies, String classPath, boolean def) {
            super(name, version);
            assert name != null;
            assert version != null;
            this.displayName = displayName == null ? name : displayName;
            //this is an ugly workaround for IMP-NG profile where the NG is the version number so no other version number should be appended
            if ("IMP-NG".equalsIgnoreCase(getName())) this.displayNameWithVersion = getDisplayName(); //NOI18N
            else this.displayNameWithVersion = DISPLAY_NAME_WITH_VERSION_FORMAT.format(new Object[] {getDisplayName(), getVersion().toString()}, new StringBuffer(), null).toString();
            assert type != null;
            this.type = type;
            this.dependencies = dependencies != null ? dependencies : ""; //NOI18N
            this.classPath = classPath == null ? "" : classPath; // NOI18N
            this.def = def;
        }
        
        public String getDisplayName() {
            return this.displayName;
        }
        
        public String getDisplayNameWithVersion() {
            return this.displayNameWithVersion;
        }
        
        public String getType() {
            return this.type;
        }
        
        public String getDependencies() {
            return this.dependencies;
        }
        
        public String getClassPath() {
            assert this.classPath != null;
            return this.classPath;
        }
        
        public boolean isDefault() {
            return this.def;
        }
        
        public boolean isRealyOptional() {
            return this.realyOptional;
        }
        
        public boolean isNameIsJarFileName() {
            return classPath.equals(PLATFORM_STRING_PREFIX + displayName);
        }
        
        public String toString() {
            return isNameIsJarFileName() || "IMP-NG".equalsIgnoreCase(getName()) ? getName() : getName() + '-' + getVersion().toString(); //NOI18N
        }
        
        public int compareTo(final Object o) {
            final Profile p = (Profile)o;
            final int r = getName().compareTo(p.getName());
            if (r != 0) return r;
            if (getVersion() == null) return p.getVersion() == null ? 0 : -1;
            return p.getVersion() == null ? 1 : getVersion().compareTo(p.getVersion());
        }
        
    }
    
    public static FileObject getSubFileObject(final FileObject folder, String name) {
        if (folder == null  ||  name == null)
            return null;
        if (Utilities.isWindows()) {
            name = name + ".exe"; //NOI18N
            final FileObject[] fos = folder.getChildren();
            if (fos != null) for (int a = 0; a < fos.length; a ++) {
                if (name.equalsIgnoreCase(fos[a].getNameExt()))
                    return fos[a];
            }
            return null;
        }
        return folder.getFileObject(name);
    }
    
    public static FileObject findTool(final String toolName, final Collection<FileObject> installFolders) {
        assert toolName != null;
        for ( final FileObject root : installFolders ) {
            final FileObject bin = root.getFileObject("bin");             //NOI18N
            if (bin == null) {
                continue;
            }
            final FileObject tool = getSubFileObject(bin, toolName);
            if (tool != null) {
                return tool;
            }
        }
        return null;
    }
    
    public FileObject findTool(final String toolName) {
        return findTool(toolName, this.getInstallFolders());
    }
    
    public static DataObject createPlatform(final String platformPath) throws IOException {
        final J2MEPlatform p = new UEIEmulatorConfiguratorImpl(platformPath).getPlatform();
        return p == null ? null : createPlatform(p);
    }
    
    public static DataObject createPlatform(final J2MEPlatform platform) throws IOException {
        final String name = platform.getName();
        final FileObject platformsFolder = FileUtil.getConfigFile("Services/Platforms/org-netbeans-api-java-Platform"); //NOI18N
        if (platformsFolder.getFileObject(name, "xml") != null) //NOI18N
            return null;
        return PlatformConvertor.create(platform, DataFolder.findFolder(platformsFolder), name);
    }
    
    public static String computeUniqueName(final String platformName) {
        final StringBuffer antname = new StringBuffer();
        for (int a = 0; a < platformName.length(); a++) {
            final char c = platformName.charAt(a);
            if (Character.isJavaIdentifierPart(c))
                antname.append(c);
            else
                antname.append('_');
        }
        
        return antname.toString(); // + "@" + Long.toHexString (((long) hashCode.toString ().hashCode ()) & 0xFFFFFFFFl).toUpperCase (); //NOI18N
    }
    
    /**
     * Setter for property runCmd.
     * @param runCmd New value of property runCmd.
     */
    public void setRunCmd(final String runCmd) {
        final String old = this.runCmd;
        this.runCmd = runCmd;
        firePropertyChange("runCmd", old, runCmd); //NOI18N
    }
    
    /**
     * Setter for property preverifyCmd.
     * @param preverifyCmd New value of property preverifyCmd.
     */
    public void setPreverifyCmd(final String preverifyCmd) {
        final String old = this.preverifyCmd;
        this.preverifyCmd = preverifyCmd;
        firePropertyChange("preverifyCmd", old, preverifyCmd); //NOI18N
    }
    
    /**
     * Setter for property debugCmd.
     * @param debugCmd New value of property debugCmd.
     */
    public void setDebugCmd(final String debugCmd) {
        final String old = this.debugCmd;
        this.debugCmd = debugCmd;
        firePropertyChange("debugCmd", old, debugCmd); //NOI18N
    }
    
}
