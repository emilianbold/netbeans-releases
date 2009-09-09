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
import java.awt.EventQueue;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.JButton;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.ruby.platform.Util;
import org.netbeans.modules.ruby.platform.gems.GemInfo;
import org.netbeans.modules.ruby.platform.gems.GemManager.VersionPredicate;
import org.netbeans.modules.ruby.platform.gems.GemManager;
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
public final class RubyPlatform implements Comparable<RubyPlatform> {

    private static final Logger LOGGER = Logger.getLogger(RubyPlatform.class.getName());

    public static final String DEFAULT_RUBY_RELEASE = "1.8"; // NOI18N

    /** Version number of the rubystubs */
    public static final String RUBYSTUBS_VERSION = "1.8.7-p72"; // NOI18N

    /**
     * The name of the rubystubs folder.
     */
    public static final String RUBYSTUBS = "rubystubs"; //NOI18N

    /** Name of the Ruby Debug IDE gem. */
    static final String RUBY_DEBUG_IDE_NAME;
    static {
        // Allow to pass different gem name for ruby-debug-ide gem. It allows
        // testing of forks of official ruby-debug-ide gem. Cf. issue #157870.
        String prop = System.getProperty("rubyDebugIDEName"); // NOI18N
        if (prop != null) {
            RUBY_DEBUG_IDE_NAME = prop;
        } else {
            RUBY_DEBUG_IDE_NAME = "ruby-debug-ide"; // NOI18N
        }
    }

    private final Info info;
    private final RubyPlatformValidator validator;
    private final String id;
    private final String interpreter;
    private File home;
    private String homeUrl;
    private FileObject libDirFO;
    private GemManager gemManager;
    private static FileObject stubsFO;

    // XXX - see updateIndexRoots below
//    private boolean indexInitialized;

    // Platform tools
    private String gemTool;
    private String rdoc;
    private String irb;

    private PropertyChangeSupport pcs;

    /** 'rake' executable for this platform. */
    private String rake;

    /** 'rails' executable for this platform. */
    private String rails;

    /** 'autotest' executable for this platform. */
    private String autotest;

    /** 'autospec' executable for this platform. */
    private String autospec;

    RubyPlatform(String id, String interpreterPath, Info info) {
        this.id = id;
        this.interpreter = interpreterPath;
        this.info = info;
        this.validator = new RubyPlatformValidator(this);
    }

    /**
     * Tries to find a {@link RubyPlatform platform} for a given project. Might
     * return <tt>null</tt>.
     */
    @CheckForNull
    public static RubyPlatform platformFor(final Project project) {
        RubyPlatformProvider rpp = project.getLookup().lookup(RubyPlatformProvider.class);
        RubyPlatform result = rpp == null ? null : rpp.getPlatform();
        if (result == null && LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Could not resolve a platform for " + project + ". " +
                    "Platform provider: " + rpp);
        }
        return result;
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
     * @return whether this platform represents Ruby 1.9 platform.
     */
    public boolean is19() {
        return getVersion() != null && getVersion().startsWith("1.9");
    }

    /**
     * Checks whether the platform has a valid Rake installed.
     *
     * @param warn whether to show warning message to the user if ther is no
     *        valid Rake installed
     */
    public boolean hasValidRake(boolean warn) {
        boolean valid = isValid(warn) && hasRubyGemsInstalled(warn);
        String rakePath = getRake();
        valid = (rakePath != null) && new File(rakePath).exists();
        possiblyNotifyUser(warn, valid, "rake"); // NOI18N
        return valid;
    }

    /**
     * Checks whether the platform has a valid Rails installed.
     *
     * @param warn whether to show warning message to the user if ther is no
     *        valid Rails installed
     */
    public boolean hasValidRails(boolean warn) {
        String railsPath = getRails();
        boolean valid = (railsPath != null) && new File(railsPath).exists();
        possiblyNotifyUser(warn, valid, "rails"); // NOI18N
        return valid;
    }

    /**
     * Checks whether the platform has a valid autotest installed.
     *
     * @param warn whether to show warning message to the user if ther is no
     *        valid autotest installed
     */
    public boolean hasValidAutoTest(boolean warn) {
        boolean valid = isValidFile(getAutoTest());
        possiblyNotifyUser(warn, valid, "autotest"); // NOI18N
        return valid;
    }

    /**
     * Checks whether the platform has a valid autotest installed.
     *
     * @param warn whether to show warning message to the user if ther is no
     *        valid autotest installed
     */
    public boolean hasValidAutoSpec(boolean warn) {
        boolean valid = isValidFile(getAutoSpec());
        possiblyNotifyUser(warn, valid, "autospec"); // NOI18N
        return valid;
    }

    private boolean isValidFile(String path) {
        return path != null && new File(path).exists();
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
        return platform.hasValidRake(warn);
    }

    public String getRake() {
        if (rake == null) {
            rake = findExecutable("rake"); // NOI18N

            if (rake != null && !(new File(rake).exists()) && getGemManager().getLatestVersion("rake") != null) { // NOI18N
                // On Windows, rake does funny things - you may only get a rake.bat
                InstalledFileLocator locator = InstalledFileLocator.getDefault();
                File f = locator.locate("modules/org-netbeans-modules-ruby-project.jar", // NOI18N
                        null, false);

                if (f == null) {
                    throw new RuntimeException("Can't find cluster"); // NOI18N
                }

                f = new File(f.getParentFile().getParentFile().getAbsolutePath() + File.separator + "rake"); // NOI18N

                try {
                    rake = f.getCanonicalPath();
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }
        }

        return rake;
    }

    public String getRails() {
        if (rails == null) {
            rails = findExecutable("rails"); // NOI18N
        }
        return rails;
    }

    public String getAutoTest() {
        if (autotest == null) {
            autotest = findExecutable("autotest"); // NOI18N
        }
        return autotest;
    }

    public String getAutoSpec() {
        if (autospec == null) {
            autospec = findExecutable("autospec"); // NOI18N
        }
        return autospec;
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
            LOGGER.log(Level.WARNING, "rubylibdir not found for {0}, was: {1}", new String[]{interpreter, lib});
            return null;
        }
        File libDir = new File(lib);
        if (!libDir.isDirectory()) {
            LOGGER.log(Level.WARNING, "rubylibdir not found for {0}, was: {1}", new String[]{interpreter, lib});
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
        File _home = getHome();
        assert _home != null : "home not null";

        File lib =
                new File(_home, "lib" + File.separator + "ruby" + File.separator + "site_ruby"); // NOI18N

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

    /**
     * Calls {@link #isValid(boolean)} with <code>false</code>.
     */
    public boolean isValid() {
        return isValid(false);
    }

    /**
     * Test whether the platform is valid, i.e. has appropriate interpreter,
     * <em>lib</em> and <em>bin</em> directories.
     *
     * @param warn whether to show the dialog to the user if platform is invalid
     * @return whether the platform is valid
     */
    public boolean isValid(final boolean warn) {
        boolean valid = new File(interpreter).isFile() && getLibDir() != null;
        if (valid) {
            String binDir = getBinDir();
            if (binDir != null) {
                valid = new File(binDir).isDirectory();
            }
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
        RubyPlatform defaultPlatform = RubyPlatformManager.getDefaultPlatform();
        return defaultPlatform != null && interpreter.equals(defaultPlatform.getInterpreter());
    }

    public boolean isJRuby() {
        return info.isJRuby();
    }

    public boolean isRubinius() {
        return info.isRubinius();
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

    public String findExecutable(final String toFind) {
        return findExecutable(toFind, true);
    }

    /**
     * Try to find a path to the <tt>toFind</tt> executable in the "Ruby
     * specific" manner.
     *
     * @param toFind executable to be find, e.g. rails, rake, rdoc, irb ...
     * @return path to the found executable; might be <tt>null</tt> if not
     *         found.
     */
    private String findExecutable(final String toFind, final boolean searchInRubyGems) {
        String exec = null;
        boolean canonical = true; // default
        do {
            String binDir = getBinDir();
            if (binDir != null) {
                LOGGER.log(Level.FINER, "Looking for '{0}' executable; used intepreter: '{1}'", new String[]{toFind, getInterpreter()}); // NOI18N
                exec = RubyPlatform.findExecutable(binDir, toFind);
            } else {
                LOGGER.log(Level.WARNING, "Could not find Ruby interpreter executable when searching for '{0}'", toFind); // NOI18N
            }
            if (exec == null && searchInRubyGems && hasRubyGemsInstalled()) {
                for (File repo : getGemManager().getRepositories()) {
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
            exec = findExecutable(toFind + ".bat", searchInRubyGems); // NOI18N
        }
        if (exec != null) {
            LOGGER.log(Level.FINER, "Found '{0}': '{1}'", new String[]{toFind, exec});
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
    private String findExecutable(final String toFind, final boolean searchInRubyGems, final boolean withSuffix) {
        String exec = findExecutable(toFind, searchInRubyGems);
        if (exec == null && withSuffix && !isJRuby()) { // JRuby is not compiled with custom suffix
            String name = new File(getInterpreter(true)).getName();
            if (name.startsWith("ruby")) { // NOI18N
                String suffix = name.substring(4);
                // Try to find with suffix (#120441)
                exec = findExecutable(toFind + suffix, searchInRubyGems);
            }
        }
        return exec;
    }

    private static String findExecutable(final String dir, final String toFind) {
        String exec = dir + File.separator + toFind;
        if (!new File(exec).isFile()) {
            LOGGER.log(Level.FINER, "'{0}' is not a file.", exec); // NOI18N
            exec = null;
        }
        return exec;
    }

    /**
     * Return path to the <em>gem</em> tool if it does exist.
     *
     * @return path to the <em>gem</em> tool; might be <tt>null</tt> if not
     *         found.
     */
    public String getGemTool() {
        if (gemTool == null) {
            gemTool = findExecutable("gem", false, true); // NOI18N
        }
        return gemTool;
    }

    public String getRDoc() {
        if (rdoc == null) {
            rdoc = findExecutable("rdoc", false, true); // NOI18N
        }
        return rdoc;
    }

    public String getIRB() {
        if (irb == null) {
            irb = findExecutable(isJRuby() ? "jirb" : "irb", false, true); // NOI18N
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
                // Language registry polls us for some reason (see #153595 stacktrace)
                RubyPlatform platform = RubyPlatformManager.getDefaultPlatform();
                if (platform != null) { // there does not need to be default platform
                    String interpreter = platform.getInterpreter();
                    if (interpreter != null) {
                        FileObject fo = FileUtil.toFileObject(new File(interpreter));
                        if (fo != null) {
                            stubsFO = fo.getParent().getParent().getParent().getFileObject("rubystubs/" + RUBYSTUBS_VERSION); // NOI18N
                        }
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
        return getGemManager() != null && !isRubinius() && getFastDebuggerProblemsInHTML() == null;
    }

    /**
     * @return null if everything is OK or errors in String
     */
    public String getFastDebuggerProblemsInHTML() {
        assert getGemManager() != null : "has gemManager when asking whether Fast Debugger is installed";
        getGemManager().resetLocal();
        StringBuilder errors = new StringBuilder();

        // Special case for ruby-debug-ide19, likely temporary until
        // ruby-debug-ide support both, 1.8 and 1.9.
        // If ruby-debug-ide19 is available, use it.
        if (is19() && RUBY_DEBUG_IDE_NAME.equals("ruby-debug-ide") && // NOI18N
                checkGem(RUBY_DEBUG_IDE_NAME + "19", getRequiredRDebugIDEVersionPattern())) { // NOI18N
            return null;
        }

        checkGemAndReport(RUBY_DEBUG_IDE_NAME, getRequiredRDebugIDEVersionPattern(), errors);
        return errors.length() == 0 ? null : errors.toString();
    }

    /**
     * Platform requires version of <i>ruby-debug-ide</i> which matches pattern
     * returned by this function.
     */
    private Pattern getRequiredRDebugIDEVersionPattern() {
        return Pattern.compile("0\\.4\\..*"); // NOI18N
    }

    private boolean checkGem(final String gemName, final Pattern gemVersion) {
        VersionPredicate predicate = new VersionPredicate() {
            public boolean isRight(final String version) {
                return gemVersion.matcher(version).matches();
            }
        };
        return getGemManager().isGemInstalledForPlatform(gemName, predicate);
    }

    private void checkGemAndReport(final String gemName, final Pattern gemVersion, final StringBuilder errors) {
        if (!checkGem(gemName, gemVersion)) {
            errors.append(NbBundle.getMessage(RubyPlatform.class, "RubyPlatform.GemInVersionMissing", gemName, gemVersion.toString()));
            errors.append("<br>"); // NOI18N
        }
    }

    public void reportRubyGemsProblem() {
        validator.reportRubyGemsProblem();
    }

    /** Returns false if check fails. True in success case. */
    public boolean checkAndReportRubyGemsProblems() {
        return validator.checkAndReportRubyGemsProblems();
    }

    /**
     * Return <tt>null</tt> if there are no problems running gem. Otherwise
     * return an error message which describes the problem.
     */
    public String getRubyGemsProblems() {
        return validator.getRubyGemsProblems();
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
        // Special case for ruby-debug-ide19, likely temporary until
        // ruby-debug-ide support both, 1.8 and 1.9.
        // If ruby-debug-ide19 is available, use it.
        if (is19() && RUBY_DEBUG_IDE_NAME.equals("ruby-debug-ide")) { // NOI18N
            String version = getLatestAvailableValidRDebugIDEVersions(RUBY_DEBUG_IDE_NAME + "19"); // NOI18N
            if (version != null) {
                return version;
            }
        }
        return getLatestAvailableValidRDebugIDEVersions(RUBY_DEBUG_IDE_NAME);
    }

    private String getLatestAvailableValidRDebugIDEVersions(final String gemName) {
        List<GemInfo> versions = getGemManager().getVersions(gemName);
        for (GemInfo getInfo : versions) {
            String version = getInfo.getVersion();
            if (getRequiredRDebugIDEVersionPattern().matcher(version).matches()) {
                return version;
            }
        }
        return null;
    }

    /**
     * Tries to install fast Ruby debugger for the platform. That is an
     * appropriate version of <em>ruby-debug-ide</em> gem.
     *
     * @return <tt>true</tt> whether the installation succeed; <tt>false</tt> otherwise
     */
    public boolean installFastDebugger() {
        assert getGemManager() != null : "has gemManager when trying to install fast debugger";
        Runnable installer = new Runnable() {
            public void run() {
                // TODO: ideally this would be e.g. '< 0.3' but then running external
                // process has problems with the '<'. See issue 142240.
                getGemManager().installGem(RUBY_DEBUG_IDE_NAME, false, false, "0.4.9"); // NOI18N
            }
        };
        if (!EventQueue.isDispatchThread()) {
            try {
                EventQueue.invokeAndWait(installer);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            installer.run();
        }
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
        FileObject gemHome = getGemHome();

        while (file != null) {
            if (file.equals(rubyLibFo) || file.equals(rubyStubs) || isGemRoot(file) || file.equals(gemHome)) {
                return file;
            }

            file = file.getParent();
        }

        return null;
    }

    private FileObject getGemHome() {
        return getGemManager() != null ? getGemManager().getGemHomeFO() : null;
    }

    /**
     * @return true if the given file represents the lib dir of a gem, e.g.
     * <code>$GEM_HOME/mygem-0.1/lib</code>, false otherwise. This is useful
     * for {@link #getSystemRoot(libDirFO)} to be able to return an indexed root
     * (the lib dirs of gems are indexed roots). See also IZ 167814.
     */
    private boolean isGemRoot(FileObject file) {
        for (int i = 0; i < 3 && file != null; i++) {
            file = file.getParent();
        }
        return file != null && file.isFolder() && file.equals(getGemHome());
    }

    private void updateIndexRoots() {
        // XXX - Parsing API
//        if (!indexInitialized) {
//            indexInitialized = true;
//            // HACK, fix soon
//            // Let RepositoryUpdater and friends know where they can root preindexing
//            // This should be done in a cleaner way.
//            //org.netbeans.modules.gsfret.source.usages.Index.setPreindexRootUrl(getHomeUrl());
//
//            org.netbeans.modules.gsfret.source.usages.Index.addPreindexRoot(FileUtil.toFileObject(getHome(true)));
//
//            if (hasRubyGemsInstalled()) {
//                FileObject gemFo = getGemManager().getGemHomeFO();
//                org.netbeans.modules.gsfret.source.usages.Index.addPreindexRoot(gemFo);
//            }
//
//        }
    }

    /**
     * The gems installed have changed, or the installed ruby has changed etc. --
     * force a recomputation of the installed classpath roots.
     */
    public void recomputeRoots() {
        updateIndexRoots();

        // Ensure that source cache is wiped and classpaths recomputed for existing files
        // XXX - Parsing API
//        Source.clearSourceCache();

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
        getGemManager().reset();
    }

    /**
     * Calls {@link #hasRubyGemsInstalled(boolean)} with <tt>false</tt> for warn
     * parameter.
     */
    public boolean hasRubyGemsInstalled() {
        return hasRubyGemsInstalled(false);
    }

    /**
     * Check whether RubyGems are correctly installed for this platform.
     *
     * @param warn whether to show warning if RubyGems are not installed or
     *        installation is broken
     * @return whether the RubyGems are correctly installed for this platform
     */
    public boolean hasRubyGemsInstalled(boolean warn) {
        return validator.hasRubyGemsInstalled(warn);
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
        if ((this.interpreter == null) ? (other.interpreter != null) : !this.interpreter.equals(other.interpreter)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + (this.interpreter != null ? this.interpreter.hashCode() : 0);
        return hash;
    }

    public int compareTo(final RubyPlatform other) {
        return getInterpreter().compareTo(other.getInterpreter());
    }

    public @Override String toString() {
        return "RubyPlatform[id:" + getID() + ", label:" + getLabel() + ", " + getInterpreter() + ", info: " + info + "]"; // NOI18N
    }

    private void possiblyNotifyUser(boolean warn, boolean valid, String cmd) {
        if (warn && !valid) {
            String msg = NbBundle.getMessage(RubyPlatform.class, "RubyPlatform.NotInstalledCmd", cmd, getLabel());
            NotifyDescriptor nd =
                    new NotifyDescriptor.Message(msg, NotifyDescriptor.Message.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }
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
            info.jversion = "1.3.1"; // NOI18N
            info.patchlevel = "287"; // NOI18N
            // XXX this is dynamically generated during JRuby build, should be
            // fixed by not hardcoding the default platform info, but rather
            // computing as for other platforms
            info.releaseDate = "2009-06-15"; // NOI18N
            info.platform = "java"; // NOI18N
            File jrubyHome = InstalledFileLocator.getDefault().locate(
                    "jruby-1.3.1", "org.netbeans.modules.ruby.platform", false);  // NOI18N
            // XXX handle valid case when it is not available, see #124534
            assert (jrubyHome != null && jrubyHome.isDirectory()) : "Default platform available";
            FileObject libDirFO = FileUtil.toFileObject(jrubyHome).getFileObject("/lib/ruby"); // NOI18N
            info.libDir = FileUtil.toFile(libDirFO.getFileObject("/1.8")).getAbsolutePath(); // NOI18N
            info.gemHome = FileUtil.toFile(libDirFO.getFileObject("/gems/1.8")).getAbsolutePath(); // NOI18N
            info.gemPath = info.gemHome;
            info.gemVersion = "1.3.3"; // NOI18N
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

        public void setGemVersion(String gemVersion) {
            this.gemVersion = gemVersion;
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

        /**
         * Returns JRuby version as specified by <code>JRUBY_VERSION</code>
         * JRuby constant. Supported by JRuby only.
         *
         * @return JRuby version
         */
        public String getJVersion() {
            return jversion;
        }

        /**
         * Returns Ruby version as specified by <code>RUBY_VERSION</code>
         * constant. Supported by all interpreters.
         *
         * @return Ruby version
         */
        public String getVersion() {
            return version;
        }

        /**
         * Get version specific for the platform. E.g. in the case of JRuby it
         * returns e.g. 1.1.4 instead of 1.8.6.
         *
         * @return platform specific version
         */
        public String getPlatformVersion() {
            return isJRuby() ? getJVersion() : getVersion();
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
