/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.api.ruby.platform;

import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.beans.VetoableChangeSupport;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.ruby.platform.RubyPlatform.Info;
import org.netbeans.modules.ruby.platform.RubyExecution;
import org.netbeans.modules.ruby.platform.Util;
import org.netbeans.modules.ruby.platform.execution.ExecutionService;
import org.netbeans.modules.ruby.spi.project.support.rake.EditableProperties;
import org.netbeans.modules.ruby.spi.project.support.rake.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.Utilities;
import org.openide.util.io.ReaderInputStream;

/**
 * Represents one Ruby platform, i.e. installation of a Ruby interpreter.
 */
public final class RubyPlatformManager {
    
    public static final boolean PREINDEXING = Boolean.getBoolean("gsf.preindexing");
    
    private static final String[] RUBY_EXECUTABLE_NAMES = { "ruby", "jruby", "rubinius" }; // NOI18N
    
    /** For unit tests. */
    static Properties TEST_RUBY_PROPS;

    private static final String PLATFORM_PREFIX = "rubyplatform."; // NOI18N
    private static final String PLATFORM_INTEPRETER = ".interpreter"; // NOI18N
    private static final String PLATFORM_ID_DEFAULT = "default"; // NOI18N

    private static final Logger LOGGER = Logger.getLogger(RubyPlatformManager.class.getName());
    
    private static Set<RubyPlatform> platforms;
    /**
     * Change support for notifying of platform changes, using vetoable for 
     * making it possible to prevent removing of a used platform.
     */
    private static final VetoableChangeSupport vetoableChangeSupport = new VetoableChangeSupport(RubyPlatformManager.class);

    private RubyPlatformManager() {
        // static methods only
    }

    /**
     * So far, for unit tests only.
     * <p>
     * Resets platforms cache.
     */
    static void resetPlatforms() {
        platforms = null;
        firePlatformsChanged();
    }
    /**
     * Get a set of all registered platforms.
     */
    public static synchronized Set<RubyPlatform> getPlatforms() {
        return new HashSet<RubyPlatform>(getPlatformsInternal());
    }

    public static void performPlatformDetection() {
        if (PREINDEXING) {
            return;
        }
        // Check the path to see if we find any other Ruby installations

        final Set<File> rubies = new LinkedHashSet<File>();
        for (String dir : Util.dirsOnPath()) {
            for (String ruby : RUBY_EXECUTABLE_NAMES) {
                File f = findPlatform(dir, ruby);
                if (f != null) {
                    rubies.add(f);
                }
            }
        }

        for (File ruby : rubies) {
            try {
                if (getPlatformByFile(ruby) == null) {
                    addPlatform(ruby);
                }
            } catch (IOException e) {
                // tell the user that something goes wrong
                LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
            }
        }
        Util.setFirstPlatformTouch(false);
        
    }

    private static void firePlatformsChanged() {
        try {
            vetoableChangeSupport.fireVetoableChange("platforms", null, null); //NOI18N
        } catch (PropertyVetoException ex) {
            // do nothing, vetoing not implemented yet
        }
    }
    private static File findPlatform(final String dir, final String ruby) {
        File f = null;
        if (Utilities.isWindows()) {
            f = new File(dir, ruby + ".exe"); // NOI18N
        } else {
            f = new File(dir, ruby); // NOI18N
            // Don't include /usr/bin/ruby on the Mac - it's no good
            // Source: http://developer.apple.com/tools/rubyonrails.html
            //   "The version of Ruby that shipped on Mac OS X Tiger prior to 
            //    v10.4.6 did not work well with Rails.   If you're running 
            //    an earlier version of Tiger, you'll need to either upgrade 
            //    to 10.4.6 or upgrade your copy of Ruby to version 1.8.4 or 
            //    later using the open source distribution."
            if (ruby.equals("ruby") && Utilities.isMac() && "/usr/bin/ruby".equals(f.getPath())) { // NOI18N
                String version = System.getProperty("os.version"); // NOI18N
                if (version == null || version.startsWith("10.4")) { // Only a problem on Tiger // NOI18N
                    return null;
                }
            }
        }
        if (f.isFile()) {
            return f;
        }
        return null;
    }

    private static synchronized Set<RubyPlatform> getPlatformsInternal() {
        if (platforms == null) {
            platforms = new HashSet<RubyPlatform>();

            // Test and preindexing hook
            String hardcodedRuby = System.getProperty("ruby.interpreter");
            if (hardcodedRuby != null) {
                Info info = new Info("User-specified Ruby", "0.1"); // NOI18N

                FileObject gems = FileUtil.toFileObject(new File(hardcodedRuby)).getParent().getParent().getFileObject("lib/ruby/gems/1.8"); // NOI18N
                if (gems != null) {
                    Properties props = new Properties();
                    props.setProperty(Info.RUBY_KIND, "User-specified Ruby"); // NOI18N
                    props.setProperty(Info.RUBY_VERSION, "0.1"); // NOI18N
                    String gemHome = FileUtil.toFile(gems).getAbsolutePath();
                    props.setProperty(Info.GEM_HOME, gemHome);
                    props.setProperty(Info.GEM_PATH, gemHome);
                    props.setProperty(Info.GEM_VERSION, "1.0.1 (1.0.1)"); // NOI18N
                    info = new Info(props);
                }

                platforms.add(new RubyPlatform(PLATFORM_ID_DEFAULT, hardcodedRuby, info));
                return platforms;
            }
            
            Map<String, String> p = PropertyUtils.sequentialPropertyEvaluator(null,
                    PropertyUtils.globalPropertyProvider()).getProperties();
            if (p == null) { // #115909
                p = Collections.emptyMap();
            }
            boolean foundDefault = false;
            for (Map.Entry<String, String> entry : p.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith(PLATFORM_PREFIX) && key.endsWith(PLATFORM_INTEPRETER)) {
                    String id = key.substring(PLATFORM_PREFIX.length(),
                            key.length() - PLATFORM_INTEPRETER.length());
                    String idDot = id + '.';
                    Properties props = new Properties();
                    String kind = p.get(PLATFORM_PREFIX + idDot + Info.RUBY_KIND);
                    if (kind == null) { // not supporting old 6.0 platform, skip
                        continue;
                    }
                    props.put(Info.RUBY_KIND, kind);
                    props.put(Info.RUBY_VERSION, p.get(PLATFORM_PREFIX + idDot + Info.RUBY_VERSION));
                    String jrubyVersion = p.get(PLATFORM_PREFIX + idDot + Info.JRUBY_VERSION);
                    if (jrubyVersion != null) {
                        props.put(Info.JRUBY_VERSION, jrubyVersion);
                    }
                    String patchLevel = p.get(PLATFORM_PREFIX + idDot + Info.RUBY_PATCHLEVEL);
                    if (patchLevel != null){
                        props.put(Info.RUBY_PATCHLEVEL, patchLevel);
                    }
                    props.put(Info.RUBY_RELEASE_DATE, p.get(PLATFORM_PREFIX + idDot + Info.RUBY_RELEASE_DATE));
//                    props.put(Info.RUBY_EXECUTABLE, p.get(PLATFORM_PREFIX + idDot + Info.RUBY_EXECUTABLE));
                    props.put(Info.RUBY_PLATFORM, p.get(PLATFORM_PREFIX + idDot + Info.RUBY_PLATFORM));
                    String libDir = p.get(PLATFORM_PREFIX + idDot + Info.RUBY_LIB_DIR);
                    if (libDir != null) {
                        props.put(Info.RUBY_LIB_DIR, libDir);
                    }
                    String gemHome = p.get(PLATFORM_PREFIX + idDot + Info.GEM_HOME);
                    if (gemHome != null) {
                        props.put(Info.GEM_HOME, gemHome);
                        props.put(Info.GEM_PATH, p.get(PLATFORM_PREFIX + idDot + Info.GEM_PATH));
                        props.put(Info.GEM_VERSION, p.get(PLATFORM_PREFIX + idDot + Info.GEM_VERSION));
                    }
                    String interpreterPath = entry.getValue();
                    if (libDir == null) {
                        LOGGER.warning("no libDir for platform: " + interpreterPath); // NOI18N
                    }
                    Info info = new Info(props);
                    platforms.add(new RubyPlatform(id, interpreterPath, info));
                    foundDefault |= id.equals(PLATFORM_ID_DEFAULT);
                }
            }
            if (!foundDefault) {
                String loc = RubyInstallation.getInstance().getJRuby();
                if (loc != null) {
                    platforms.add(new RubyPlatform(PLATFORM_ID_DEFAULT, loc, Info.forDefaultPlatform()));
                }
            }
            LOGGER.fine("RubyPlatform initial list: " + platforms);
        }

        return platforms;
    }

    /** Typically bundled JRuby. */
    public static RubyPlatform getDefaultPlatform() {
        RubyPlatform defaultPlatform = RubyPlatformManager.getPlatformByID(PLATFORM_ID_DEFAULT);
        assert defaultPlatform != null : "Cannot find default platform";
        return defaultPlatform;
    }

    /**
     * Find a platform by its ID.
     * @param id an ID (as in {@link #getID})
     * @return the platform with that ID, or null
     */
    public static synchronized RubyPlatform getPlatformByID(String id) {
        for (RubyPlatform p : getPlatformsInternal()) {
            if (p.getID().equals(id)) {
                return p;
            }
        }
        return null;
    }

    public static synchronized RubyPlatform getPlatformByFile(File interpreter) {
        for (RubyPlatform p : getPlatformsInternal()) {
            try {
                File current = new File(p.getInterpreter()).getCanonicalFile();
                File toFind = interpreter.getCanonicalFile();
                if (current.equals(toFind)) {
                    return p;
                }
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
            }
        }
        return null;
    }
    
    static synchronized RubyPlatform getPlatformByPath(String path) {
        return getPlatformByFile(new File(path));
    }

    /**
     * Adds platform to the current platform list. Checks whether such platform
     * is already present.
     * 
     * @param interpreter interpreter to be added
     * @return <tt>null</tt>, if the given <tt>interpreter</tt> is not valid
     *         Ruby interpreter. If the platform is already present, returns it.
     *         Otherwise new platform instance is returned.
     * @throws java.io.IOException
     */
    public static RubyPlatform addPlatform(final File interpreter) throws IOException {
        RubyPlatform plaf = getPlatformByFile(interpreter);
        if (plaf != null) {
            return plaf;
        }
        final Info info = computeInfo(interpreter);
        if (info == null) {
            return null;
        }
        if (info.getKind() == null) { // # see #128354
            LOGGER.warning("Getting platform information for " + interpreter + " failed.");
            return null;
        }

        final String id = computeID(info.getKind());
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                public Void run() throws IOException {
                    if (getPlatformByID(id) != null) {
                        throw new IOException("ID " + id + " already taken"); // NOI18N
                    }
                    EditableProperties props = PropertyUtils.getGlobalProperties();
                    putPlatformProperties(id, interpreter, info, props);
                    PropertyUtils.putGlobalProperties(props);
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (IOException) e.getException();
        }
        plaf = new RubyPlatform(id, interpreter.getAbsolutePath(), info);
        synchronized (RubyPlatform.class) {
            getPlatformsInternal().add(plaf);
        }
        firePlatformsChanged();
        LOGGER.fine("RubyPlatform added: " + plaf);
        return plaf;
    }

    public static void removePlatform(final RubyPlatform plaf) throws IOException {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                public Void run() throws IOException {
                    EditableProperties props = PropertyUtils.getGlobalProperties();
                    clearProperties(plaf, props);
                    PropertyUtils.putGlobalProperties(props);
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (IOException) e.getException();
        }
        synchronized (RubyPlatform.class) {
            getPlatformsInternal().remove(plaf);
        }
        firePlatformsChanged();
        LOGGER.fine("RubyPlatform removed: " + plaf);
    }

    public static void storePlatform(final RubyPlatform plaf) throws IOException {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                public Void run() throws IOException {
                    EditableProperties props = PropertyUtils.getGlobalProperties();
                    clearProperties(plaf, props);
                    putPlatformProperties(plaf.getID(), plaf.getInterpreterFile(), plaf.getInfo(), props);
                    PropertyUtils.putGlobalProperties(props);
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (IOException) e.getException();
        }
        LOGGER.fine("RubyPlatform stored: " + plaf);
    }

    private static void clearProperties(RubyPlatform plaf, EditableProperties props) {
        String id = PLATFORM_PREFIX + plaf.getID();
        props.remove(id + PLATFORM_INTEPRETER);
        String idDot = id + '.';
        props.remove(PLATFORM_PREFIX + idDot + Info.RUBY_KIND);
        props.remove(PLATFORM_PREFIX + idDot + Info.RUBY_VERSION);
        props.remove(PLATFORM_PREFIX + idDot + Info.JRUBY_VERSION);
        props.remove(PLATFORM_PREFIX + idDot + Info.RUBY_PATCHLEVEL);
        props.remove(PLATFORM_PREFIX + idDot + Info.RUBY_RELEASE_DATE);
//                    props.remove(PLATFORM_PREFIX + idDot + Info.RUBY_EXECUTABLE);
        props.remove(PLATFORM_PREFIX + idDot + Info.RUBY_PLATFORM);
        props.remove(PLATFORM_PREFIX + idDot + Info.RUBY_LIB_DIR);
        props.remove(PLATFORM_PREFIX + idDot + Info.GEM_HOME);
        props.remove(PLATFORM_PREFIX + idDot + Info.GEM_PATH);
        props.remove(PLATFORM_PREFIX + idDot + Info.GEM_VERSION);
    }
    
    private static void putPlatformProperties(final String id, final File interpreter,
            final Info info, final EditableProperties props) throws FileNotFoundException {
        String interpreterKey = PLATFORM_PREFIX + id + PLATFORM_INTEPRETER;
        props.setProperty(interpreterKey, interpreter.getAbsolutePath());
        if (!interpreter.isFile()) {
            throw new FileNotFoundException(interpreter.getAbsolutePath());
        }
        String idDot = id + '.';
        props.setProperty(PLATFORM_PREFIX + idDot + Info.RUBY_KIND, info.getKind());
        props.setProperty(PLATFORM_PREFIX + idDot + Info.RUBY_VERSION, info.getVersion());
        if (info.getJVersion() != null) {
            props.setProperty(PLATFORM_PREFIX + idDot + Info.JRUBY_VERSION, info.getJVersion());
        }
        if (info.getPatchlevel() != null) {
            props.setProperty(PLATFORM_PREFIX + idDot + Info.RUBY_PATCHLEVEL, info.getPatchlevel());
        }
        props.setProperty(PLATFORM_PREFIX + idDot + Info.RUBY_RELEASE_DATE, info.getReleaseDate());
//                    props.setProperty(PLATFORM_PREFIX + idDot + Info.RUBY_EXECUTABLE, info.getExecutable());
        props.setProperty(PLATFORM_PREFIX + idDot + Info.RUBY_PLATFORM, info.getPlatform());
        if (!info.isRubinius()) {
            props.setProperty(PLATFORM_PREFIX + idDot + Info.RUBY_LIB_DIR, info.getLibDir());
        }
        if (info.getGemHome() != null) {
            props.setProperty(PLATFORM_PREFIX + idDot + Info.GEM_HOME, info.getGemHome());
            props.setProperty(PLATFORM_PREFIX + idDot + Info.GEM_PATH, info.getGemPath());
            props.setProperty(PLATFORM_PREFIX + idDot + Info.GEM_VERSION, info.getGemVersion());
        }
    }

    private static String computeID(final String kind) {
        String id = kind;
        for (int i = 0; getPlatformByID(id) != null; i++) {
            id = kind + '_' + i;
        }
        return id;
    }

    public static Iterator<RubyPlatform> platformIterator() {
        return getPlatformsInternal().iterator();
    }

    static Info computeInfo(final File interpreter) {
        if (TEST_RUBY_PROPS != null && !RubyPlatformManager.getDefaultPlatform().getInterpreterFile().equals(interpreter)) { // tests
            return new Info(TEST_RUBY_PROPS);
        }
        Info info = null;
        try {
            File platformInfoScript = InstalledFileLocator.getDefault().locate(
                    "platform_info.rb", "org.netbeans.modules.ruby.platform", false);  // NOI18N
            if (platformInfoScript == null) {
                throw new IllegalStateException("Cannot locate platform_info.rb script"); // NOI18N
            }
            ProcessBuilder pb = new ProcessBuilder(interpreter.getAbsolutePath(), platformInfoScript.getAbsolutePath()); // NOI18N
            // be sure that JRUBY_HOME is not set during configuration
            // autodetection, otherwise interpreter under JRUBY_HOME would be
            // effectively used
            pb.environment().remove("JRUBY_HOME"); // NOI18N
            pb.environment().put("JAVA_HOME", RubyExecution.getJavaHome()); // NOI18N
            ExecutionService.logProcess(pb);
            Process proc = pb.start();
            // FIXME: set timeout
            proc.waitFor();
            if (proc.exitValue() == 0) {
                Properties props = new Properties();
                if (LOGGER.isLoggable(Level.FINEST)) {
                    String stdout = Util.readAsString(proc.getInputStream());
                    String stderr = Util.readAsString(proc.getErrorStream());
                    LOGGER.finest("stdout:\n" + stdout);
                    LOGGER.finest("stderr:\n " + stderr);
                    props.load(new ReaderInputStream(new StringReader(stdout)));
                } else {
                    props.load(proc.getInputStream());
                }
                info = new Info(props);
            } else {
                LOGGER.severe(interpreter.getAbsolutePath() + " does not seems to be a valid interpreter"); // TODO localize me
                BufferedReader errors = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
                String line;
                while ((line = errors.readLine()) != null) {
                    LOGGER.severe(line);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.INFO, "Not a ruby platform: " + interpreter.getAbsolutePath()); // NOI18N
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
        }
        return info;
    }
    
    public static void addVetoableChangeListener(VetoableChangeListener listener) {
        vetoableChangeSupport.addVetoableChangeListener(listener);
    }
    
    public static void removeVetoableChangeListener(VetoableChangeListener listener) {
        vetoableChangeSupport.removeVetoableChangeListener(listener);
    }

}
