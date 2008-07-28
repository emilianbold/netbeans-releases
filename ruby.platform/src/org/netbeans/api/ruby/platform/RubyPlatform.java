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

import java.awt.Dialog;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.JButton;
import org.netbeans.modules.gsf.api.annotations.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.ruby.platform.Util;
import org.netbeans.modules.ruby.platform.gems.GemInfo;
import org.netbeans.modules.ruby.platform.gems.GemManager;
import org.netbeans.modules.ruby.platform.gems.GemManager.VersionPredicate;
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

    private final Info info;

    private final String id;
    private final String interpreter;
    private File home;
    private String homeUrl;
    private FileObject libDirFO;
    private GemManager gemManager;
    private static FileObject stubsFO;
    private boolean indexInitialized;

    private String rdoc;
    private String irb;

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
        return platform.isValidRuby(warn) && platform.hasRubyGemsInstalled(warn) && platform.getGemManager().isValidRake(warn);
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

    /**
     * Return the lib directory for this interprerter. Usually parent of {@link
     * #getVersionLibDir()}.
     */
    public String getLibDir() {
        if (isRubinius()) {
            return getRubiniusLibDir();
        }
        String lib = info.getLibDir();
        if (lib == null) {
            LOGGER.warning("rubylibdir not found for " + interpreter + ", was: " + lib);
            return null;
        }
        File libDir = new File(lib);
        if (!libDir.isDirectory()) {
            LOGGER.warning("rubylibdir not found for " + interpreter + ", was: " + lib);
            return null;
        }
        // info.getVersionLibDir() return e.g. .../lib/ruby/1.8
        libDir = libDir.getParentFile();
        if (libDir == null) {
            return null;
        }
        libDir = libDir.getParentFile();
        if (libDir == null) {
            return null;
        }
        return libDir.getAbsolutePath();
    }

    private String getRubiniusLibDir() {
        File lib = new File(getHome(), "lib"); // NOI18N
        return lib.isDirectory() ? lib.getAbsolutePath() : null; // NOI18N
    }

    /** Utility method. See {@link #getLibDir()}. */
    public FileObject getLibDirFO() {
        if (libDirFO == null) {
            String lib = getLibDir();
            if (lib != null) {
                libDirFO = FileUtil.toFileObject(new File(lib));
            }
        }
        return libDirFO;
    }

    /**
     * Delegates to {@link Info#getLibDir()}.
     */
    public String getVersionLibDir() {
        return info.getLibDir();
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
        String msg = NbBundle.getMessage(RubyPlatform.class, "InvalidRubyPlatform", platform.getLabel());
        JButton closeButton = getCloseButton();

        Object[] options = new Object[]{closeButton};
        showDialog(msg, options);
    }

    private static void showWarning(final Project project) {
        String msg =
                NbBundle.getMessage(RubyPlatform.class, "InvalidRubyPlatformForProject",
                ProjectUtils.getInformation(project).getDisplayName());
        JButton closeButton = getCloseButton();

        CustomizerProvider customizer = project.getLookup().lookup(CustomizerProvider.class);
        Object[] options;
        JButton propertiesButton =
                new JButton(NbBundle.getMessage(RubyPlatform.class, "Properties"));
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
                NbBundle.getMessage(RubyPlatform.class, "MissingRuby"), true, options,
                options[0],
                DialogDescriptor.DEFAULT_ALIGN, new HelpCtx(RubyPlatform.class), null);
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
                new JButton(NbBundle.getMessage(RubyPlatform.class, "CTL_Close"));
        closeButton.getAccessibleContext().setAccessibleDescription(
                NbBundle.getMessage(RubyPlatform.class, "AD_Close"));
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

    public boolean isRubinius() {
        return info.isRubinius();
    }

    public boolean isValid() {
        return new File(interpreter).isFile() && getLibDir() != null;
    }

    /**
     * If the platform is in invalid state, shows general message to the user.
     *
     * @return whether the platform is valid
     */
    public boolean showWarningIfInvalid() {
        boolean valid = isValid();
        if (!valid) {
            Util.notifyLocalized(RubyPlatform.class, "RubyPlatform.InvalidInterpreter", // NOI18N
                    NotifyDescriptor.WARNING_MESSAGE, getInterpreter());
        }
        return valid;
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
     * @param toFind executable to be find, e.g. rails, rake, rdoc, irb ...
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

    /**
     * The same as {@link #findExecutable(String)}, but if fails and withSuffix
     * is set to true, it tries to find also executable with the suffix with
     * which was compiled the interpreter. E.g. for <em>ruby1.8.6-p111</em>
     * tries to find <em>irb1.8.6-p111</em>.
     *
     * @param toFind see {@link #findExecutable(String)}
     * @param withSuffix whether to try also suffix version when non-suffix is not found
     * @return see {@link #findExecutable(String)}
     */
    public String findExecutable(final String toFind, final boolean withSuffix) {
        String exec = findExecutable(toFind);
        if (exec == null && withSuffix && !isJRuby()) { // JRuby is not compiled with custom suffix
            String name = new File(getInterpreter(true)).getName();
            if (name.startsWith("ruby")) { // NOI18N
                String suffix = name.substring(4);
                // Try to find with suffix (#120441)
                exec = findExecutable(toFind + suffix);
            }
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
            rdoc = findExecutable("rdoc", true); // NOI18N
        }
        return rdoc;
    }

    public String getIRB() {
        if (irb == null) {
            irb = findExecutable(isJRuby() ? "jirb" : "irb", true); // NOI18N
        }
        return irb;
    }

    public static FileObject getRubyStubs() {
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
                String r = RubyPlatformManager.getDefaultPlatform().getInterpreter();
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
        // no usable version of Fast Debugger for Rubinius is available yet
        return gemManager != null && !isRubinius() && getFastDebuggerProblemsInHTML() == null;
    }

    /**
     * @return null if everthing is OK or errors in String
     */
    public String getFastDebuggerProblemsInHTML() {
        assert gemManager != null : "has gemManager when asking whether Fast Debugger is installed";
        StringBuilder errors = new StringBuilder();
        checkAndReport(RUBY_DEBUG_IDE_NAME, getRequiredRDebugIDEVersionPattern(), errors);
        return errors.length() == 0 ? null : errors.toString();
    }

    /**
     * Platform requires version of <i>ruby-debug-ide</i> which matches pattern
     * returned by this function.
     */
    private Pattern getRequiredRDebugIDEVersionPattern() {
        return Pattern.compile("0\\.2\\..*"); // NOI18N
    }

    private void checkAndReport(final String gemName, final Pattern gemVersion, final StringBuilder errors) {
        VersionPredicate predicate = new VersionPredicate() {
            public boolean isRight(final String version) {
                return gemVersion.matcher(version).matches();
            }
        };
        if (!gemManager.isGemInstalledForPlatform(gemName, predicate)) {
            errors.append(NbBundle.getMessage(RubyPlatform.class, "RubyPlatform.GemInVersionMissing", gemName, gemVersion.toString()));
            errors.append("<br>"); // NOI18N
        }
    }

    /**
     * Returns latest available, but valid version of rdebug-ide gem for this
     * platform. So if e.g. 0.1.10, 0.2.0 and 0.3.0 versions are available, but
     * this platform can work only with 0.1.10 and 0.2.0, version 0.2.0 is
     * returned.
     *
     * @return latest available valid version: <tt>null</tt> if none suitable
     *         version is found
     */
    public String getLatestAvailableValidRDebugIDEVersions() {
        List<GemInfo> versions = gemManager.getVersions(RUBY_DEBUG_IDE_NAME);
        for (GemInfo getInfo : versions) {
            String version = getInfo.getVersion();
            if (getRequiredRDebugIDEVersionPattern().matcher(version).matches()) {
                return version;
            }
        }
        return null;
    }

    String getLatestRequiredRDebugIDEVersion() {
        return "< 0.3"; // NOI18N
    }

    /**
     * Tries to install fast Ruby debugger for the platform. That is an
     * appropriate version of <em>ruby-debug-ide</em> gem.
     *
     * @return <tt>true</tt> whether the installation succeed; <tt>false</tt> otherwise
     */
    public boolean installFastDebugger() {
        assert gemManager != null : "has gemManager when trying to install fast debugger";
        gemManager.installGem(RUBY_DEBUG_IDE_NAME, false, false, getLatestRequiredRDebugIDEVersion());
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
        FileObject rubyLibFo = isRubinius() ? null : getLibDirFO();
        FileObject rubyStubs = getRubyStubs();
        FileObject gemHome = gemManager != null ? gemManager.getGemHomeFO() : null;

        //        FileObject jar = FileUtil.getArchiveFile(file);
        //        if (jar != null) {
        //            file = jar;
        //        }

        while (file != null) {
            if (file.equals(rubyLibFo) || file.equals(rubyStubs) || file.equals(gemHome)) {
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
        try {
            RubyPlatformManager.storePlatform(this);
        } catch (IOException ioe) {
            LOGGER.log(Level.SEVERE, ioe.getLocalizedMessage(), ioe);
        }
        gemManager.reset();
    }

    /**
     * Calls {@link #hasRubyGemsInstalled(boolean)} with <tt>false</tt> for warn
     * parameter.
     */
    public boolean hasRubyGemsInstalled() {
        return hasRubyGemsInstalled(false);
    }

    /**
     * Check for RubyGems installation for this platform.
     *
     * @param warn whether to show warning if RubyGems are not installed
     * @return whether the RubyGems are installed for this platform.
     */
    public boolean hasRubyGemsInstalled(boolean warn) {
        boolean hasRubyGems = info.getGemHome() != null;
        if (!hasRubyGems && warn) {
            Util.notifyLocalized(RubyPlatform.class, "RubyPlatform.DoesNotHaveRubyGems", // NOI18N
                    NotifyDescriptor.WARNING_MESSAGE, this.getLabel());
        }
        return hasRubyGems;
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
        return "RubyPlatform[id:" + getID() + ", label:" + getLabel() + ", " + getInterpreter() + ", info: " + info + "]"; // NOI18N
    }

    public static class Info {

        static final String RUBY_KIND = "ruby_kind"; // NOI18N
        static final String RUBY_VERSION = "ruby_version"; // NOI18N
        static final String JRUBY_VERSION = "jruby_version"; // NOI18N
        static final String RUBY_PATCHLEVEL = "ruby_patchlevel"; // NOI18N
        static final String RUBY_RELEASE_DATE = "ruby_release_date"; // NOI18N
        static final String RUBY_EXECUTABLE = "ruby_executable"; // NOI18N
        static final String RUBY_PLATFORM = "ruby_platform"; // NOI18N
        static final String RUBY_LIB_DIR = "ruby_lib_dir"; // NOI18N
        static final String GEM_HOME = "gem_home"; // NOI18N
        static final String GEM_PATH = "gem_path"; // NOI18N
        static final String GEM_VERSION = "gem_version"; // NOI18N

        private final String kind;
        private final String version;
        private String jversion;
        private String patchlevel;
        private String releaseDate;
        private String platform;
        private String gemHome;
        private String gemPath;
        private String gemVersion;
        private String libDir;

        Info(final Properties props) {
            this.kind = props.getProperty(RUBY_KIND);
            this.version = props.getProperty(RUBY_VERSION);
            this.jversion = props.getProperty(JRUBY_VERSION);
            this.patchlevel = props.getProperty(RUBY_PATCHLEVEL);
            this.releaseDate = props.getProperty(RUBY_RELEASE_DATE);
            this.platform = props.getProperty(RUBY_PLATFORM);
            this.libDir = props.getProperty(RUBY_LIB_DIR);
            setGemHome(props.getProperty(GEM_HOME));
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
            info.jversion = "1.1.3"; // NOI18N
            info.patchlevel = "114"; // NOI18N
            info.releaseDate = "2008-07-19"; // NOI18N
            info.platform = "java"; // NOI18N
            File jrubyHome = InstalledFileLocator.getDefault().locate(
                    "jruby-1.1.3", "org.netbeans.modules.ruby.platform", false);  // NOI18N
            // XXX handle valid case when it is not available, see #124534
            assert (jrubyHome != null && jrubyHome.isDirectory()) : "Default platform available";
            FileObject libDirFO = FileUtil.toFileObject(jrubyHome).getFileObject("/lib/ruby"); // NOI18N
            info.libDir = FileUtil.toFile(libDirFO.getFileObject("/1.8")).getAbsolutePath(); // NOI18N
            info.gemHome = FileUtil.toFile(libDirFO.getFileObject("/gems/1.8")).getAbsolutePath(); // NOI18N
            info.gemPath = info.gemHome;
            info.gemVersion = "1.0.1 (1.0.1)"; // NOI18N
            return info;
        }

        public String getLabel(final boolean isDefault) {
            String ver = isJRuby() ? jversion
                    : version + (patchlevel != null ? "-p" + patchlevel : ""); // NOI18N
            return (isDefault ? NbBundle.getMessage(RubyPlatform.class, "RubyPlatformManager.CTL_BundledJRubyLabel") : kind)
                    + ' ' + ver;
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

        public boolean isRubinius() {
            return "Rubinius".equals(kind); // NOI18N
        }

        public final void setGemHome(String gemHome) {
            this.gemHome = gemHome == null ? null : new File(gemHome).getAbsolutePath();
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

        /** Returns content of <code>RbConfig::CONFIG['rubylibdir']</code>. */
        public String getLibDir() {
            return libDir;
        }

        public @Override String toString() {
            return "RubyPlatform$Info[GEM_HOME:" + getGemHome() + ", GEM_PATH: " + getGemPath() // NOI18N
                    + ", gemVersion: " + getGemVersion() + ", lib: " + getLibDir() + "]"; // NOI18N
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Info other = (Info) obj;
            if (this.kind != other.kind && (this.kind == null || !this.kind.equals(other.kind))) {
                return false;
            }
            if (this.version != other.version && (this.version == null || !this.version.equals(other.version))) {
                return false;
            }
            if (this.patchlevel != other.patchlevel && (this.patchlevel == null || !this.patchlevel.equals(other.patchlevel))) {
                return false;
            }
            if (this.platform != other.platform && (this.platform == null || !this.platform.equals(other.platform))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 83 * hash + (this.kind != null ? this.kind.hashCode() : 0);
            hash = 83 * hash + (this.version != null ? this.version.hashCode() : 0);
            hash = 83 * hash + (this.patchlevel != null ? this.patchlevel.hashCode() : 0);
            hash = 83 * hash + (this.platform != null ? this.platform.hashCode() : 0);
            return hash;
        }
    }
}
