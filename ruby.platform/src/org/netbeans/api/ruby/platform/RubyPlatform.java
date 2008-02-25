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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

import java.awt.Dialog;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import org.netbeans.modules.gsf.api.annotations.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.ruby.platform.Util;
import org.netbeans.modules.ruby.platform.gems.GemManager;
import org.netbeans.napi.gsfret.source.Source;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Represents one Ruby platform, i.e. installation of a Ruby interpreter.
 */
public final class RubyPlatform {

    private static final Logger LOGGER = Logger.getLogger(RubyPlatform.class.getName());
    
    public static final String DEFAULT_RUBY_RELEASE = "1.8"; // NOI18N

    /** Version number of the rubystubs */
    private static final String RUBYSTUBS_VERSION = "1.8.6-p110"; // NOI18N

    /** Name of the Ruby Debug IDE gem. */
    static final String RUBY_DEBUG_IDE_NAME = "ruby-debug-ide"; // NOI18N
    static final String RUBY_DEBUG_BASE_NAME = "ruby-debug-base"; // NOI18N
    
    /** Required version of ruby-debug-ide gem. */
    static final String RDEBUG_IDE_VERSION;
    static final String RDEBUG_BASE_VERSION;
    
    static {
        if (Utilities.isWindows()) {
            RDEBUG_IDE_VERSION = "0.1.9"; // NOI18N
            RDEBUG_BASE_VERSION = "0.9.3"; // NOI18N
        } else {
            RDEBUG_IDE_VERSION = "0.1.10"; // NOI18N
            RDEBUG_BASE_VERSION = "0.10.0"; // NOI18N
        }
    }

    private Info info;
    
    private final String id;
    private final String interpreter;
    private File home;
    private String homeUrl;
    private String rubylib;
    private FileObject libFO;
    private GemManager gemManager;
    private FileObject stubsFO;
    private boolean indexInitialized;
    
    private String rdoc;

    private PropertyChangeSupport pcs;

    RubyPlatform(String id, String interpreterPath, Info info) {
        this.id = id;
        this.interpreter = interpreterPath;
        this.info = info;
    }

    /**
     * Tries to find a {@link RubyPlatform platform} for a given project. Might
     * return <tt>null</tt>.
     */
    @CheckForNull
    public static RubyPlatform platformFor(final Project project) {
        RubyPlatformProvider rpp = project.getLookup().lookup(RubyPlatformProvider.class);
        return rpp == null ? null : rpp.getPlatform();
    }

    /** 
     * Tries to find a {@link GemManager gem manager} for a given project, or
     * strictly speaking, for its {@link RubyPlatform platform}. Might return
     * <tt>null</tt>. 
     */
    @CheckForNull
    public static GemManager gemManagerFor(final Project project) {
        RubyPlatform platform = RubyPlatform.platformFor(project);
        return platform == null ? null : platform.getGemManager();
    }

    @CheckForNull
    public static String platformDescriptionFor(Project project) {
        RubyPlatform platform = platformFor(project);
        return platform == null ? null : platform.getInfo().getLongDescription();
    }
    
    public Info getInfo() {
        return info;
    }

    /**
     * Checks whether project has a valid platform and in turn whether the
     * platform has a valid Rake installed.
     *
     * @param warn whether to show warning message to the user if ther is no
     *        valid Rake installed
     */
    public static boolean hasValidRake(final Project project, final boolean warn) {
        RubyPlatform platform = RubyPlatform.platformFor(project);
        if (platform == null) {
            if (warn) {
                showWarning(project);
            }
            return false;
        }
        return platform.isValidRuby(warn) && platform.hasRubyGemsInstalled() && platform.getGemManager().isValidRake(warn);
    }
    
    public String getID() {
        return id;
    }

    public String getInterpreter(boolean cannonical) {
        String result = interpreter;
        if (cannonical) {
            try {
                result = new File(interpreter).getCanonicalFile().getAbsolutePath();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Cannot get canonical path", e);
            }
        }
        
        updateIndexRoots();
        
        return result;
    }

    public String getInterpreter() {
        updateIndexRoots();
        
        return interpreter;
    }

    public File getInterpreterFile() {
        updateIndexRoots();
        
        return new File(interpreter);
    }

    public File getHome() {
        return getHome(true);
    }

    public File getHome(boolean canonical) {
        if (home == null) {
            try {
                String rp = getInterpreter(canonical);
                if (rp == null) {
                    return null;
                }
                File r = new File(rp);

                // Handle bogus paths like "/" which cannot possibly point to a valid ruby installation
                File p = r.getParentFile();
                if (p == null) {
                    return null;
                }

                p = p.getParentFile();
                if (p == null) {
                    return null;
                }

                home = p.getCanonicalFile();
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
                return null;
            }
        }
        return home;
    }

    public String getHomeUrl() {
        if (homeUrl == null) {
            try {
                File r = getHome();
                if (r != null) {
                    homeUrl = r.toURI().toURL().toExternalForm();
                }
            } catch (MalformedURLException mue) {
                Exceptions.printStackTrace(mue);
            }
        }

        return homeUrl;
    }

    /** Return the lib directory for this interprerter. */
    public String getLib() {
        File home = getHome();
        if (home == null) {
            return null;
        }
        File lib = new File(home, "lib"); // NOI18N

        if (lib.exists() && new File(lib, "ruby").exists()) { // NOI18N
            try {
                return lib.getCanonicalPath();
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        throw new AssertionError("'lib/ruby' cannot be resolved for '" + interpreter + "' interpreter");
    }

    public FileObject getLibFO() {
        if (libFO == null) {
            String lib = getLib();

            if (lib != null) {
                libFO = FileUtil.toFileObject(new File(lib));
            }
        }

        return libFO;
    }

    /**
     * Find the Ruby-specific library directory for the chosen Ruby. This is
     * usually something like /foo/bar/lib/1.8/ (where Ruby was
     * /foo/bar/bin/ruby) but it tries to work with other versions of Ruby as
     * well (such as 1.9).
     */
    public String getLibDir() {
        if (rubylib == null) {
            File home = getHome();
            assert home != null : "home not null";

            File lib = new File(home, "lib" + File.separator + "ruby"); // NOI18N

            if (!lib.exists()) {
                return null;
            }

            File f = new File(lib, DEFAULT_RUBY_RELEASE); // NOI18N

            if (f.exists()) {
                rubylib = f.getAbsolutePath();
            } else {
                // Search for a numbered directory
                File[] children = lib.listFiles();

                for (File c : children) {
                    if (!c.isDirectory()) {
                        continue;
                    }

                    String name = c.getName();

                    if (name.matches("\\d+\\.\\d+")) { // NOI18N
                        rubylib = c.getAbsolutePath();

                        break;
                    }
                }
            }

            assert rubylib != null : "rubylib not null";
        }

        return rubylib;
    }

    /** Return the site_ruby directory for the current ruby installation. Not cached. */
    public String getRubyLibSiteDir() {
        String sitedir = null;
        File home = getHome();
        assert home != null : "home not null";

        File lib =
                new File(home, "lib" + File.separator + "ruby" + File.separator + "site_ruby"); // NOI18N

        if (!lib.exists()) {
            return null;
        }

        File f = new File(lib, DEFAULT_RUBY_RELEASE); // NOI18N

        if (f.exists()) {
            sitedir = f.getAbsolutePath();
        } else {
            // Search for a numbered directory
            File[] children = lib.listFiles();

            for (File c : children) {
                if (!c.isDirectory()) {
                    continue;
                }

                String name = c.getName();

                if (name.matches("\\d+\\.\\d+")) { // NOI18N
                    sitedir = c.getAbsolutePath();

                    break;
                }
            }

            if ((sitedir == null) && (children.length > 0)) {
                sitedir = children[0].getAbsolutePath();
            }
        }

        return sitedir;
    }

    public boolean isValidRuby(boolean warn) {
        String rp = getBinDir();
        boolean valid = false;
        if (rp != null) {
            File file = new File(rp);
            valid = file.exists() && getHome() != null;
        }

        if (warn && !valid) {
            showWarning(this);
        }

        return valid;
    }

    private static void showWarning(final RubyPlatform platform) {
        String msg = NbBundle.getMessage(RubyInstallation.class, "InvalidRubyPlatform", platform.getLabel());
        JButton closeButton = getCloseButton();

        Object[] options = new Object[]{closeButton};
        showDialog(msg, options);
    }
    
    private static void showWarning(final Project project) {
        String msg =
                NbBundle.getMessage(RubyInstallation.class, "InvalidRubyPlatformForProject",
                ProjectUtils.getInformation(project).getDisplayName());
        JButton closeButton = getCloseButton();

        CustomizerProvider customizer = project.getLookup().lookup(CustomizerProvider.class);
        Object[] options;
        JButton propertiesButton =
                new JButton(NbBundle.getMessage(RubyInstallation.class, "Properties"));
        if (customizer != null) {
            options = new Object[]{propertiesButton, closeButton};
        } else {
            options = new Object[]{closeButton};
            
        }
        if (showDialog(msg, options) == propertiesButton) {
            customizer.showCustomizer();
        }
    }

    private static Object showDialog(String msg, Object[] options) {
        DialogDescriptor descriptor =
                new DialogDescriptor(msg,
                NbBundle.getMessage(RubyInstallation.class, "MissingRuby"), true, options,
                options[0],
                DialogDescriptor.DEFAULT_ALIGN, new HelpCtx(RubyInstallation.class), null);
        descriptor.setMessageType(NotifyDescriptor.Message.ERROR_MESSAGE);
        descriptor.setModal(true);
        Dialog dlg = null;
        try {
            dlg = DialogDisplayer.getDefault().createDialog(descriptor);
            dlg.setVisible(true);
        } finally {
            if (dlg != null) {
                dlg.dispose();
            }
        }
        return descriptor.getValue();
    }

    private static JButton getCloseButton() {
        JButton closeButton =
                new JButton(NbBundle.getMessage(RubyInstallation.class, "CTL_Close"));
        closeButton.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(RubyInstallation.class, "AD_Close"));
        return closeButton;
    }

    public String getVersion() {
        return info.getVersion();
    }

    public String getLabel() {
        return info.getLabel(isDefault());
    }

    public boolean isDefault() {
        return interpreter.equals(RubyPlatformManager.getDefaultPlatform().getInterpreter());
    }

    public boolean isJRuby() {
        return info.isJRuby();
    }

    public boolean isValid() {
        return new File(interpreter).isFile();
    }

    /**
     * See also {@link #hasRubyGemsInstalled}.
     *
     * @return either an instance of {@link GemManager} or <tt>null</tt>.
     */
    @CheckForNull
    public GemManager getGemManager() {
        if (gemManager == null && hasRubyGemsInstalled()) {
            gemManager = new GemManager(this);
        }
        return gemManager;
    }

    public String getBinDir() {
        return getBinDir(true);
    }

    public String getBinDir(boolean canonical) {
        String rubybin = null;
        String r = getInterpreter(canonical);

        if (r != null) {
            rubybin = new File(r).getParent();
        }
        return rubybin;
    }

    /**
     * Try to find a path to the <tt>toFind</tt> executable in the "Ruby
     * specific" manner.
     *
     * @param toFind executable to be find, e.g. rails, rake, ...
     * @return path to the found executable; might be <tt>null</tt> if not
     *         found.
     */
    public String findExecutable(final String toFind) {
        String exec = null;
        boolean canonical = true; // default
        do {
            String binDir = getBinDir();
            if (binDir != null) {
                LOGGER.finest("Looking for '" + toFind + "' executable; used intepreter: '" + getInterpreter() + "'"); // NOI18N
                exec = RubyPlatform.findExecutable(binDir, toFind);
            } else {
                LOGGER.warning("Could not find Ruby interpreter executable when searching for '" + toFind + "'"); // NOI18N
            }
            if (exec == null && hasRubyGemsInstalled()) {
                for (File repo : gemManager.getRepositories()) {
                    String libGemBinDir = repo.getAbsolutePath() + File.separator + "bin"; // NOI18N
                    exec = RubyPlatform.findExecutable(libGemBinDir, toFind);
                    if (exec != null) {
                        break;
                    }
                }
            }
            canonical ^= true;
        } while (!canonical && exec == null);
        // try to find a gem on system path - see issue 116219
        if (exec == null) {
            exec = Util.findOnPath(toFind);
        }
        // try *.bat commands on Windows
        if (exec == null && !toFind.endsWith(".bat") && Utilities.isWindows()) { // NOI18N
            exec = findExecutable(toFind + ".bat"); // NOI18N
        }
        if (exec != null) {
            LOGGER.finest("Found '" + toFind + "': '" + exec + "'");
        }
        return exec;
    }

    private static String findExecutable(final String dir, final String toFind) {
        String exec = dir + File.separator + toFind;
        if (!new File(exec).isFile()) {
            LOGGER.finest("'" + exec + "' is not a file."); // NOI18N
            exec = null;
        }
        return exec;
    }

    public String getRDoc() {
        if (rdoc == null) {
            rdoc = findExecutable("rdoc"); // NOI18N
            if (rdoc == null && !isJRuby()) {
                String name = new File(getInterpreter(true)).getName();
                if (name.startsWith("ruby")) { // NOI18N
                    String suffix = name.substring(4);
                    // Try to find with suffix (#120441)
                    rdoc = findExecutable("rdoc" + suffix); // NOI18N
                }
            }
        }
        return rdoc;
    }

    public FileObject getRubyStubs() {
        if (stubsFO == null) {
            // Core classes: Stubs generated for the "builtin" Ruby libraries.
            File clusterFile = InstalledFileLocator.getDefault().locate(
                    "modules/org-netbeans-modules-ruby-project.jar", null, false); // NOI18N

            if (clusterFile != null) {
                File rubyStubs =
                    new File(clusterFile.getParentFile().getParentFile().getAbsoluteFile(),
                        // JRUBY_RELEASEDIR + File.separator + 
                    "rubystubs" + File.separator + RUBYSTUBS_VERSION); // NOI18N
                assert rubyStubs.exists() && rubyStubs.isDirectory();
                stubsFO = FileUtil.toFileObject(rubyStubs);
            } else {
                // During test?
                String r = getInterpreter();
                if (r != null) {
                    FileObject fo = FileUtil.toFileObject(new File(r));
                    if (fo != null) {
                        stubsFO = fo.getParent().getParent().getParent().getFileObject("rubystubs/" + RUBYSTUBS_VERSION); // NOI18N
                    }
                }
            }
        }

        return stubsFO;
    }

    /**
     * @return whether everything needed for fast debugging is installed
     */
    public boolean hasFastDebuggerInstalled() {
        return gemManager != null && getFastDebuggerProblemsInHTML() == null;
    }

    /**
     * @return null if everthing is OK or errors in String
     */
    public String getFastDebuggerProblemsInHTML() {
        assert gemManager != null : "has gemManager when asking whether Fast Debugger is installed";
        StringBuilder errors = new StringBuilder();
        checkAndReport(RUBY_DEBUG_IDE_NAME, RDEBUG_IDE_VERSION, errors);
        checkAndReport(RUBY_DEBUG_BASE_NAME, RDEBUG_BASE_VERSION, errors);
        return errors.length() == 0 ? null : errors.toString();
    }

    private void checkAndReport(final String gemName, final String gemVersion, final StringBuilder errors) {
        if (!gemManager.isGemInstalledForPlatform(gemName, gemVersion)) {
            errors.append(NbBundle.getMessage(RubyPlatform.class, "RubyPlatform.GemInVersionMissing", gemName, gemVersion));
            errors.append("<br>"); // NOI18N
        }
    }

    public boolean installFastDebugger() {
        assert gemManager != null : "has gemManager when trying to install fast debugger";
        gemManager.installGem(RUBY_DEBUG_IDE_NAME, false, false, RDEBUG_IDE_VERSION);
        return hasFastDebuggerInstalled();
    }

    /*
     * Return non-null iff the given file is a "system" file - e.g. it's part of
     * the Ruby 1.8 libraries, or the Java support libraries, or the user's
     * rubygems, or something under $GEM_HOME...
     *
     * @return The actual root the in the system the file is under.
     */
    public FileObject getSystemRoot(FileObject file) {
        // See if the file is under the Ruby libraries
        FileObject rubyLibFo = getLibFO();
        FileObject rubyStubs = getRubyStubs();
        FileObject gemHome = gemManager != null ? gemManager.getGemHomeFO() : null;

        //        FileObject jar = FileUtil.getArchiveFile(file);
        //        if (jar != null) {
        //            file = jar;
        //        }

        while (file != null) {
            if (file == rubyLibFo || file == rubyStubs || file == gemHome) {
                return file;
            }

            file = file.getParent();
        }

        return null;
    }

    private void updateIndexRoots() {
        if (!indexInitialized) {
            indexInitialized = true;
            // HACK, fix soon
            // Let RepositoryUpdater and friends know where they can root preindexing
            // This should be done in a cleaner way.
            //org.netbeans.modules.gsfret.source.usages.Index.setPreindexRootUrl(getHomeUrl());

            org.netbeans.modules.gsfret.source.usages.Index.addPreindexRoot(FileUtil.toFileObject(getHome(true)));

            if (hasRubyGemsInstalled()) {
                FileObject gemFo = getGemManager().getGemHomeFO();
                org.netbeans.modules.gsfret.source.usages.Index.addPreindexRoot(gemFo);
            }

        }
    }
    
    /**
     * The gems installed have changed, or the installed ruby has changed etc. --
     * force a recomputation of the installed classpath roots.
     */
    public void recomputeRoots() {
        updateIndexRoots();

        // Ensure that source cache is wiped and classpaths recomputed for existing files
        Source.clearSourceCache();
        
        if (pcs != null) {
            pcs.firePropertyChange("roots", null, null); // NOI18N
        }

        //        // Force ClassIndex registration
        //        // Dummy class path provider just to trigger recomputation
        //        ClassPathProviderImpl cpProvider = new ClassPathProviderImpl(null, null, null, null);
        //        ClassPath[] cps = cpProvider.getProjectClassPaths(ClassPath.BOOT);
        //        GlobalPathRegistry.getDefault().register(ClassPath.BOOT, cps);
        //        GlobalPathRegistry.getDefault().unregister(ClassPath.BOOT, cps);

        // Possibly clean up index from old ruby root as well?
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (pcs == null) {
            pcs = new PropertyChangeSupport(this);
        }

        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        if (pcs != null) {
            pcs.removePropertyChangeListener(listener);
        }
    }

    public void setGemHome(File gemHome) {
        assert hasRubyGemsInstalled() : "has RubyGems installed";
        info.setGemHome(gemHome.getAbsolutePath());
        gemManager.reset();
    }

    /**
     * @return whether the RubyGems are installed for this platform.
     */
    public boolean hasRubyGemsInstalled() {
        return info.getGemHome() != null;
    }

    /**
     * Notifies the registered listeners that are changes in this platform's gems,
     * i.e. a gem was removed or a new gem was installed.
     */
    public void fireGemsChanged() {
        if (pcs != null) {
            pcs.firePropertyChange("gems", null, null); //NOI18N
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RubyPlatform other = (RubyPlatform) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

    public @Override String toString() {
        return "RubyPlatform[id:" + getID() + ", label:" + getLabel() + ", " + getInterpreter() + "]"; // NOI18N
    }

    public static class Info {

        static final String RUBY_KIND = "ruby_kind"; // NOI18N
        static final String RUBY_VERSION = "ruby_version"; // NOI18N
        static final String JRUBY_VERSION = "jruby_version"; // NOI18N
        static final String RUBY_PATCHLEVEL = "ruby_patchlevel"; // NOI18N
        static final String RUBY_RELEASE_DATE = "ruby_release_date"; // NOI18N
        static final String RUBY_EXECUTABLE = "ruby_executable"; // NOI18N
        static final String RUBY_PLATFORM = "ruby_platform"; // NOI18N
        static final String GEM_HOME = "gem_home"; // NOI18N
        static final String GEM_PATH = "gem_path"; // NOI18N
        static final String GEM_VERSION = "gem_version"; // NOI18N

        private String kind;
        private String version;
        private String jversion;
        private String patchlevel;
        private String releaseDate;
        private String executable;
        private String platform;
        private String gemHome;
        private String gemPath;
        private String gemVersion;
        
        Info(final Properties props) {
            this.kind = props.getProperty(RUBY_KIND);
            this.version = props.getProperty(RUBY_VERSION);
            this.jversion = props.getProperty(JRUBY_VERSION);
            this.patchlevel = props.getProperty(RUBY_PATCHLEVEL);
            this.releaseDate = props.getProperty(RUBY_RELEASE_DATE);
            this.executable = props.getProperty(RUBY_EXECUTABLE);
            this.platform = props.getProperty(RUBY_PLATFORM);
            this.gemHome = props.getProperty(GEM_HOME);
            this.gemPath = props.getProperty(GEM_PATH);
            this.gemVersion = props.getProperty(GEM_VERSION);
        }

        Info(String kind, String version) {
            this.kind = kind;
            this.version = version;
        }
        
        static Info forDefaultPlatform() {
            // NbBundle.getMessage(RubyPlatformManager.class, "CTL_BundledJRubyLabel")
            Info info = new Info("JRuby", "1.8.6"); // NOI18N
            info.jversion = "1.1RC1"; // NOI18N
            info.patchlevel = "5512"; // NOI18N
            info.releaseDate = "2008-01-12"; // NOI18N
            info.executable = null;
            info.platform = "java"; // NOI18N
            File jrubyHome = InstalledFileLocator.getDefault().locate(
                    "jruby-1.1RC1", "org.netbeans.modules.ruby.platform", false);  // NOI18N
            // XXX handle valid case when it is not available, see #124534
            assert (jrubyHome != null && jrubyHome.isDirectory()) : "Default platform available";
            info.gemHome = FileUtil.toFile(FileUtil.toFileObject(jrubyHome).getFileObject("/lib/ruby/gems/1.8")).getAbsolutePath(); // NOI18N
            info.gemPath = info.gemHome;
            info.gemVersion = "1.0.1 (1.0.1)"; // NOI18N
            return info;
        }
        
        public String getLabel(final boolean isDefault) {
            String ver = isJRuby() ? jversion
                    : version + (patchlevel != null ? "-p" + patchlevel : ""); // NOI18N
            return (isDefault ? NbBundle.getMessage(RubyPlatform.class, "RubyPlatformManager.CTL_BundledJRubyLabel") : kind)
                    + " (" + ver + ')'; // NOI18N
        }
        
        public String getLongDescription() {
            StringBuilder sb = new StringBuilder(kind + ' ' + version + ' ' + '(' + releaseDate);
            if (patchlevel != null) {
                sb.append(" patchlevel ").append(patchlevel); // NOI18N
            }
            sb.append(") [").append(platform).append(']'); // NOI18N
            return sb.toString();
        }

        public boolean isJRuby() {
            return "JRuby".equals(kind); // NOI18N
        }

//        public String getExecutable() {
//            return executable;
//        }

        public void setGemHome(String gemHome) {
            this.gemHome = gemHome;
        }
        
        public String getGemHome() {
            return gemHome;
        }

        public void setGemPath(String gemPath) {
            this.gemPath = gemPath;
        }
        
        public String getGemPath() {
            return gemPath;
        }

        public String getGemVersion() {
            return gemVersion;
        }

        public String getKind() {
            return kind;
        }

        public String getPatchlevel() {
            return patchlevel;
        }

        public String getPlatform() {
            return platform;
        }

        public String getReleaseDate() {
            return releaseDate;
        }

        public String getJVersion() {
            return jversion;
        }
        
        public String getVersion() {
            return version;
        }

    }
}
