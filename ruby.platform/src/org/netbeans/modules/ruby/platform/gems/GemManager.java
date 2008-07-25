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
 * Contributor(s):
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
package org.netbeans.modules.ruby.platform.gems;

import java.awt.Component;
import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.api.ruby.platform.RubyPlatformManager;
import org.netbeans.modules.ruby.platform.Util;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.Utilities;

/**
 * Provides access to RubyGems environment, like RubyGems repositories, provides
 * operations for getting information about installed gems, for fetching
 * remotely available gems, etc.
 * <p>
 * Every instance of {@link GemManager} <em>belongs</em> to particular {@link
 * RubyPlatform}.
 */
public final class GemManager {

    private static final Logger LOGGER = Logger.getLogger(GemManager.class.getName());

    /** Top level directories inside the Gem repository. */
    private static final String[] TOP_LEVEL_REPO_DIRS = { "cache", "specifications", "gems", "doc" }; // NOI18N

    /** Directory inside the GEM_HOME directory. */
    private static final String SPECIFICATIONS = "specifications"; // NOI18N

    private static final boolean SKIP_INDEX_LIBS = System.getProperty("ruby.index.nolibs") != null; // NOI18N
    private static final boolean SKIP_INDEX_GEMS = System.getProperty("ruby.index.nogems") != null; // NOI18N

    /**
     * Extension of files containing gems specification residing in {@link
     * #SPECIFICATIONS}.
     */
    private static final String DOT_GEM_SPEC = ".gemspec"; // NOI18N

    /**
     * Contains the locally installed gems. All installed versions are included.
     * <p>
     * Maps <i>gem name</i> to <i>sorted-by-version list of {@link GemInfo}s</i>
     * </p>
     */
    private Map<String, List<GemInfo>> localGems;

    private Map<String, String> gemVersions;
    private Map<String, URL> gemUrls;
    private Set<URL> nonGemUrls;

    /**
     * Used by tests.
     * <p>
     * <em>FIXME</em>: get rid of this
     */
    public static String TEST_GEM_HOME;

    /** Share over invocations of the dialog since these are slow to compute */
    private List<Gem> installed;

    /** Share over invocations of the dialog since these are ESPECIALLY slow to compute */
    private List<Gem> remote;

    private String gemTool;
    private String gemHomeUrl;
    private String rake;
    private String rails;

    private final RubyPlatform platform;

    private final Lock runnerLock;

    public GemManager(final RubyPlatform platform) {
        assert platform.hasRubyGemsInstalled() : "called when RubyGems installed";
        this.platform = platform;
        this.runnerLock = new ReentrantLock(true);
    }

    private String getGemMissingMessage() {
        if (Utilities.isMac() && "/usr/bin/ruby".equals(platform.getInterpreter())) { // NOI18N
            String version = System.getProperty("os.version"); // NOI18N
            if (version == null || version.startsWith("10.4")) { // Only a problem on Tiger // NOI18N
                return NbBundle.getMessage(GemManager.class, "GemMissingMac");
            }
        }
        return NbBundle.getMessage(GemManager.class, "GemMissing");
    }

    /**
     * Return null if there are no problems running gem. Otherwise return
     * an error message which describes the problem.
     */
    public String getGemProblem() {
        String gem = getGemTool();

        if (gem == null) {
            return getGemMissingMessage();
        }

        String gemHomePath = getGemHome();
        if (gemHomePath == null) {
            // edge case, misconfiguration? gem tool is installed but repository is not found
            return NbBundle.getMessage(GemManager.class, "CannotFindGemRepository");
        }

        File gemHome = new File(gemHomePath);

        if (!gemHome.exists()) {
            // Is this possible? (Installing gems, but no gems installed yet
            return null;
        }

        return null;
    }

    boolean hasObsoleteRubyGemsVersion() {
        String gemVersion = platform.getInfo().getGemVersion();
        return Util.compareVersions("1.0", gemVersion) > 0; // NOI18N
    }

    boolean isGemHomeWritable() {
        return getGemHomeF().canWrite();
    }

    private boolean checkGemHomePermissions() {
        if (!isGemHomeWritable()) {
            String gksu = Util.findOnPath("gksu"); // NOI18N
            if (gksu == null) {
                NotifyDescriptor nd = new NotifyDescriptor.Message(
                        NbBundle.getMessage(GemManager.class, "GemNotWritable", getGemHome()),
                        NotifyDescriptor.Message.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notifyLater(nd);
                return false;
            }
        }
        return true;
    }

    /** Initialize/creates empty Gem Repository. */
    public static void initializeRepository(File gemRepo) throws IOException {
        if (!gemRepo.exists()) {
            gemRepo.mkdirs();
        }
        initializeRepository(FileUtil.toFileObject(gemRepo));
    }
    /** Initialize/creates empty Gem Repository. */
    public static void initializeRepository(FileObject gemRepo) throws IOException {
        for (String dir : TOP_LEVEL_REPO_DIRS) {
            gemRepo.createFolder(dir);
        }
    }

    /** Returns main Gem repository path. */
    public String getGemHome() {
        return platform.getInfo().getGemHome();
    }

    public File getGemHomeF() {
        return FileUtil.normalizeFile(new File(platform.getInfo().getGemHome()));
    }

    public FileObject getGemHomeFO() {
        return FileUtil.toFileObject(getGemHomeF());
    }

    public String getGemHomeUrl() {
        if (gemHomeUrl == null) {
            String gemHome = getGemHome();
            if (gemHome != null) {
                try {
                    File r = new File(gemHome);
                    if (r != null) {
                        gemHomeUrl = r.toURI().toURL().toExternalForm();
                    }
                } catch (MalformedURLException mue) {
                    Exceptions.printStackTrace(mue);
                }
            }
        }

        return gemHomeUrl;
    }

    public Set<? extends File> getRepositories() {
        Set<File> repos = getGemPath();
        repos.add(getGemHomeF());
        return repos;
    }

    /** Returns paths to all Gem repositories. */
    public Set<File> getGemPath() {
        Set<File> repos = new LinkedHashSet<File>();
        StringTokenizer st = new StringTokenizer(platform.getInfo().getGemPath(), File.pathSeparator);
        while (st.hasMoreTokens()) {
            repos.add(new File(st.nextToken()));
        }
        return repos;
    }

    public boolean addGemPath(final File path) {
        Set<File> gemPath = getGemPath();
        boolean result;
        try {
            result = gemPath.add(path.getCanonicalFile());
        } catch (IOException ioe) {
            LOGGER.log(Level.SEVERE, ioe.getLocalizedMessage(), ioe);
            result = false;
        }
        if (result) {
            storeGemPath(gemPath);
        }
        return result;
    }

    public void removeGemPath(final File path) {
        Set<File> gemPath = getGemPath();
        gemPath.remove(path);
        storeGemPath(gemPath);
    }

    private void storeGemPath(final Set<File> gemPath) {
        StringBuilder pathSB = new StringBuilder();
        for (File token : gemPath) {
            if (pathSB.length() != 0) {
                pathSB.append(File.pathSeparator);
            }
            pathSB.append(token.getAbsolutePath());
        }
        platform.getInfo().setGemPath(pathSB.toString());
        try {
            RubyPlatformManager.storePlatform(platform);
        } catch (IOException ioe) {
            LOGGER.log(Level.SEVERE, ioe.getLocalizedMessage(), ioe);
        }
        resetLocal();
    }

    /**
     * Checks whether a gem with the given name is installed in the gem
     * repository used by the currently set Ruby interpreter.
     *
     * @param gemName name of a gem to be checked
     * @return <tt>true</tt> if installed; <tt>false</tt> otherwise
     */
    public boolean isGemInstalled(final String gemName) {
        return !getVersions(gemName).isEmpty();
    }

    public boolean isGemInstalledForPlatform(final String gemName, final VersionPredicate predicate) {
        for (GemInfo gemInfo : getVersions(gemName)) {
            // TODO: the platform info should rather be encapsulated in GemInfo
            String specName = gemInfo.getSpecFile().getName();
            // filter out all java gems for non-java platforms
            // hack until we support proper .gemspec parsing
            if (!platform.isJRuby() && specName.endsWith("-java.gemspec")) { // NOI18N
                continue;
            }

            // special hack for fast debugger
            if (specName.startsWith("ruby-debug-base-")) { // NOI18N
                boolean forJavaPlaf = specName.endsWith("-java.gemspec"); // NOI18N
                if (platform.isJRuby() && !forJavaPlaf) {
                    continue;
                }
                if (!platform.isJRuby() && forJavaPlaf) {
                    continue;
                }
            }
            if (predicate.isRight(gemInfo.getVersion())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks whether a gem with the given name and the given version is
     * installed in the gem repository used by the currently set Ruby
     * interpreter.
     *
     * @param gemName name of a gem to be checked
     * @param version version of the gem to be checked
     * @return <tt>true</tt> if installed; <tt>false</tt> otherwise
     */
    public boolean isGemInstalled(final String gemName, final String version) {
        String currVersion = getLatestVersion(gemName);
        return isRightVersion(currVersion, version, false);
    }

    /**
     * Checks whether the installed version matches.
     *
     * @param gemName cf. {@link #isGemInstalled(String, String)}
     * @param version cf. {@link #isGemInstalled(String, String)}
     * @param exact whether exact match should be performed. If <tt>false</tt>,
     * equal or greater matches. If <tt>true</tt>, only exact version matches.
     * @return whether the installed version matches
     */
    public boolean isGemInstalledForPlatform(final String gemName, final String expectedVersion, final boolean exact) {
        VersionPredicate predicate = new VersionPredicate() {
            public boolean isRight(String version) {
                return isRightVersion(version, expectedVersion, exact);
            }
        };
        return isGemInstalledForPlatform(gemName, predicate);
    }

    private boolean isRightVersion(final String currVersion, final String version, final boolean exact) {
        boolean isInstalled = false;
        if (currVersion != null) {
            int result = Util.compareVersions(version, currVersion);
            isInstalled = exact ? result == 0 : result <= 0;
        }
        return isInstalled;
    }

    public boolean isGemInstalledForPlatform(final String gemName, final String version) {
        return isGemInstalledForPlatform(gemName, version, false);
    }

    /**
     * Gets the newest locally installed version of the given <code>gemName</code>.
     *
     * @param gemName the name of the gem to check.
     * @return the version number of the newest version or <code>null</code> if
     * no version of the given gem was installed.
     */
    public String getLatestVersion(String gemName) {
        initGemList();
        List<GemInfo> versions = getVersions(gemName);
        return versions.isEmpty() ? null : versions.get(0).getVersion();
    }


    /**
     * Gets all locally installed versions of the given <code>gemName</code>
     * sorted by version in descending order.
     * 
     * @param gemName
     * @return the versions, an empty list if there are no versions
     *         of the given <code>gemName</code>.
     */
    public List<GemInfo> getVersions(String gemName) {
        initGemList();

        List<GemInfo> versions = localGems.get(gemName);
        if (versions == null || versions.isEmpty()) {
            return Collections.<GemInfo>emptyList();
        }
        return versions;
    }

    /**
     * Logs the installed gems using the given logging level.
     */
    private void logGems(Level level) {
        if (!LOGGER.isLoggable(level)) {
            return;
        }

        if (localGems == null) {
            LOGGER.log(level, "No gems found, gemFiles is null"); // NOI18N
            return;
        }

        LOGGER.log(level, "Found " + localGems.size() + " gems."); // NOI18N
        for (String key : localGems.keySet()) {
            List<GemInfo> versions = getVersions(key);
            LOGGER.log(level, key + " has " + versions.size() + " version(s):"); // NOI18N
            for (GemInfo version : versions) {
                LOGGER.log(level, version + " at " + version.getSpecFile()); // NOI18N
            }
        }
    }

    private void initGemList() {
        if (localGems == null) {
            // Initialize lazily
            assert platform.hasRubyGemsInstalled() : "asking for gems only when RubyGems are installed";
            localGems = new HashMap<String, List<GemInfo>>();
            for (File gemDir : getRepositories()) {
                File specDir = new File(gemDir, SPECIFICATIONS);
                if (specDir.exists()) {
                    LOGGER.finest("Initializing \"" + gemDir + "\" repository");
                    // Add each of */lib/
                    File[] specFiles = specDir.listFiles();
                    if (specFiles != null) {
                        localGems.putAll(GemFilesParser.getGemInfos(specFiles));
                    }
                } else {
                    LOGGER.finest("Cannot find Gems repository. \"" + gemDir + "\" does not exist or is not a directory."); // NOI18N
                }
                logGems(Level.FINEST);
            }
        }
    }

    public Set<String> getInstalledGemsFiles() {
        initGemList();
        return localGems.keySet();
    }

    public void reset() {
        resetRemote();
        resetLocal();
        gemHomeUrl = null;
    }

    /**
     * Tries to reset <em>remote</em> gems. Request might be ignored if the
     * update is just in progress.
     */
    public void resetRemote() {
        if (runnerLock.tryLock()) {
            try {
                remote = null;
            } finally {
                runnerLock.unlock();
            }
        } else {
            LOGGER.finest("resetRemote() ignored");
        }
    }

    /**
     * Tries to reset <em>local</em> and <em>installed</em> gems. Request might
     * be ignored if the update is just in progress.
     */
    public void resetLocal() {
        if (runnerLock.tryLock()) {
            try {
                installed = null;
                localGems = null;
                platform.fireGemsChanged();
            } finally {
                runnerLock.unlock();
            }
        } else {
            LOGGER.finest("resetLocal() ignored");
        }
    }

    /**
     * <em>WARNING:</em> Slow! Synchronous gem execution.
     *
     * @param errors list to which the errors, which happen during gems
     *        reload, will be accumulated
     */
    public void getAllGems(List<String> errors) {
        reloadIfNeeded(errors);
    }

    /**
     * <em>WARNING:</em> Slow! Synchronous gem execution.
     *
     * @param errors list to which the errors, which happen during gems
     *        reload, will be accumulated
     * @return list of the installed gems. Returns an empty list if they could
     *         not be read, never null.
     */
    public List<Gem> getInstalledGems(List<String> errors) {
        reloadIfNeeded(errors);
        return getInstalledGems();
    }

    /**
     * Gets the available remote gems. <em>WARNING:</em> Slow! Synchronous gem execution.
     *
     * @param errors list to which the errors, which happen during gems
     *        reload, will be accumulated
     * @return list of the available remote gems. Returns an empty list if they could not
     *         be read, never null.
     */
    public List<Gem> getRemoteGems(List<String> errors) {
        reloadIfNeeded(errors);
        return getRemoteGems();
    }

    /**
     * Gets the available <b>cached</b> installed gems. Clients must be sure
     * reload was triggered.
     *
     * @return list of the available installed gems. Returns an empty list if
     *         they could not be read, never null.
     */
    public List<Gem> getInstalledGems() {
        return installed != null ? installed : Collections.<Gem>emptyList();
    }

    /**
     * Gets the available <b>cached</b> remote gems. Clients must be sure reload
     * was triggered.
     *
     * @return list of the available remote gems. Returns an empty list if they
     *         could not be read, never null.
     */
    public List<Gem> getRemoteGems() {
        return remote != null ? remote : Collections.<Gem>emptyList();
    }

    public boolean haveGemTool() {
        return getGemTool() != null;
    }

    /** Returns false if check fails. True in success case. */
    private boolean checkGemProblem() {
        String gemProblem = getGemProblem();
        if (gemProblem != null) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(gemProblem,
                    NotifyDescriptor.Message.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(nd);
            return false;
        }
        return true;
    }

    boolean needsReload() {
        return installed == null || remote == null;
    }

    /**
     * This method is called automatically every time when installed or remote
     * gems are looked for. The method reloads only needed gems. Remote, local
     * or both. You might want to call this method explicitly if you know you
     * will be getting all (remote and locals) gems subsequently.
     *
     * @param errors list into which the errors, which happen during gems
     *        reload, will be accumulated
     */
    public void reloadIfNeeded(final List<String> errors) {
        assert !EventQueue.isDispatchThread() : "do not call from EDT!";
        if (!checkGemProblem()) {
            return;
        }

        runnerLock.lock();
        try {
            GemRunner gemRunner = new GemRunner(platform);
            boolean ok;
            if (installed == null && remote == null) {
                ok = gemRunner.fetchBoth();
                installed = new ArrayList<Gem>();
                remote = new ArrayList<Gem>();
            } else if (installed == null) {
                ok = gemRunner.fetchLocal();
                installed = new ArrayList<Gem>();
            } else if (remote == null) {
                ok = gemRunner.fetchRemote();
                remote = new ArrayList<Gem>();
            } else {
                return; // no reload needed
            }

            if (ok) {
                parseGemList(gemRunner.getOutput(), installed, remote);

                // Sort the lists
                if (installed != null) {
                    Collections.sort(installed);
                }
                if (remote != null) {
                    Collections.sort(remote);
                }
            } else {
                // Produce the error list
                boolean inErrors = false;
                for (String line : gemRunner.getOutput()) {
                    if (inErrors) {
                        errors.add(line);
                    } else if (line.startsWith("***") || line.startsWith(" ") || line.trim().length() == 0) { // NOI18N
                        continue;
                    } else if (!line.matches("[a-zA-Z\\-]+ \\(([0-9., ])+\\)\\s?")) { // NOI18N
                        errors.add(line);
                        inErrors = true;
                    }
                }
            }
        } finally {
            runnerLock.unlock();
        }
    }

    private static void parseGemList(List<String> lines, List<Gem> localList, List<Gem> remoteList) {
        Gem gem = null;
        boolean listStarted = false;
        boolean inLocal = false;
        boolean inRemote = false;

        for (String line : lines) {
            if (line.length() == 0) {
                gem = null;

                continue;
            }

            if (line.startsWith("*** REMOTE GEMS")) { // NOI18N
                inRemote = true;
                inLocal = false;
                listStarted = true;
                gem = null;
                continue;
            } else if (line.startsWith("*** LOCAL GEMS")) { // NOI18N
                inRemote = false;
                inLocal = true;
                listStarted = true;
                gem = null;
                continue;
            }

            if (!listStarted) {
                // Skip status messages etc.
                continue;
            }

            if (Character.isWhitespace(line.charAt(0))) {
                if (gem != null) {
                    String description = line.trim();

                    if (gem.getDescription() == null) {
                        gem.setDescription(description);
                    } else {
                        gem.setDescription(gem.getDescription() + " " + description); // NOI18N
                    }
                }
            } else {
                if (line.charAt(0) == '.') {
                    continue;
                }

                // Should be a gem - but could be an error message!
                int versionIndex = line.indexOf('(');

                if (versionIndex != -1) {
                    String name = line.substring(0, versionIndex).trim();
                    int endIndex = line.indexOf(')');
                    String versions;

                    if (endIndex != -1) {
                        versions = line.substring(versionIndex + 1, endIndex);
                    } else {
                        versions = line.substring(versionIndex);
                    }

                    gem = new Gem(name, inLocal ? versions : null, inLocal ? null : versions);
                    if (inLocal) {
                        localList.add(gem);
                    } else {
                        assert inRemote;
                        remoteList.add(gem);
                    }
                } else {
                    gem = null;
                }
            }
        }
    }

    /**
     * Install the given version of the given gem with dependencies and refresh
     * IDE caches accordingly after the gem is installed.
     *
     * @param gem gem to install
     * @param rdoc if true, generate RDoc as part of the installation
     * @param ri if true, generate RI data as part of the installation
     * @param version If non null, install the specified version rather than the
     *        latest remote version
     */
    public void installGem(final String gem, final boolean rdoc, final boolean ri, final String version) {
        if (!checkGemHomePermissions()) {
            return;
        }
        final Gem[] gems = new Gem[] {
            new Gem(gem, null, null)
        };
        Runnable installationComplete = new Runnable() {
            public void run() {
                platform.recomputeRoots();
            }
        };
        install(gems, null, rdoc, ri, version, true, true, installationComplete);
    }

    /**
     * Install the latest version of the given gem with dependencies and refresh
     * IDE caches accordingly after the gem is installed.
     *
     * @param gem gem to install
     * @param rdoc if true, generate RDoc as part of the installation
     * @param ri if true, generate RI data as part of the installation
     */
    public void installGem(final String gem, final boolean rdoc, final boolean ri) {
        installGem(gem, rdoc, ri, null);
    }

    /**
     * Install the given gems.
     *
     * @param gem Gem description for the gem to be installed. Only the name is relevant.
     * @param parent For asynchronous tasks, provide a parent Component that will have progress dialogs added,
     *   a possible cursor change, etc.
     * @param rdoc If true, generate RDoc as part of the installation
     * @param ri If true, generate RI data as part of the installation
     * @param version If non null, install the specified version rather than the latest remote version
     * @param asynchronous If true, run the gem task asynchronously - returning immediately and running the gem task
     *    in a background thread. A progress bar and message will be displayed (along with the option to view the
     *    gem output). If the exit code is normal, the completion task will be run at the end.
     * @param asyncCompletionTask If asynchronous is true and the gem task completes normally, this task will be run at the end.
     */
    public boolean install(Gem[] gems, Component parent, boolean rdoc, boolean ri,
            String version, boolean includeDeps, boolean asynchronous,
            final Runnable asyncCompletionTask) {
        if (!checkGemHomePermissions()) {
            return false;
        }
        List<String> gemNames = mapToGemNames(gems);
        GemRunner gemRunner = new GemRunner(platform);
        if (asynchronous) {
            gemRunner.installAsynchronously(gemNames, rdoc, ri, includeDeps, version, resetCompletionTask(asyncCompletionTask), parent);
            return false;
        } else {
            boolean ok = gemRunner.install(gemNames, rdoc, ri, includeDeps, version);
            resetLocal();
            return ok;
        }
    }

    /**
     * Install the given gem.
     *
     * @param gem gem file to be installed (e.g. /path/to/rake-0.8.1.gem)
     * @param parent For asynchronous tasks, provide a parent Component that will have progress dialogs added,
     *   a possible cursor change, etc.
     * @param rdoc If true, generate RDoc as part of the installation
     * @param ri If true, generate RI data as part of the installation
     * @param asynchronous If true, run the gem task asynchronously - returning immediately and running the gem task
     *    in a background thread. A progress bar and message will be displayed (along with the option to view the
     *    gem output). If the exit code is normal, the completion task will be run at the end.
     * @param asyncCompletionTask If asynchronous is true and the gem task completes normally, this task will be run at the end.
     */
    boolean installLocal(File gem, GemPanel parent, boolean rdoc, boolean ri, boolean asynchronous, Runnable asyncCompletionTask) {
        if (!checkGemHomePermissions()) {
            return false;
        }
        GemRunner gemRunner = new GemRunner(platform);
        if (asynchronous) {
            gemRunner.installLocalAsynchronously(gem, rdoc, ri, resetCompletionTask(asyncCompletionTask), parent);
            return false;
        } else {
            boolean ok = gemRunner.installLocal(gem, rdoc, ri);
            resetLocal();
            return ok;
        }
    }

    /**
     * Uninstall the given gem.
     *
     * @param gem Gem description for the gem to be uninstalled. Only the name is relevant.
     * @param parent For asynchronous tasks, provide a parent Component that will have progress dialogs added,
     *   a possible cursor change, etc.
     * @param asynchronous If true, run the gem task asynchronously - returning immediately and running the gem task
     *    in a background thread. A progress bar and message will be displayed (along with the option to view the
     *    gem output). If the exit code is normal, the completion task will be run at the end.
     * @param asyncCompletionTask If asynchronous is true and the gem task completes normally, this task will be run at the end.
     */
    public boolean uninstall(Gem[] gems, Component parent, boolean asynchronous, final Runnable asyncCompletionTask) {
        if (!checkGemHomePermissions()) {
            return false;
        }
        List<String> gemNames = mapToGemNames(gems);
        GemRunner gemRunner = new GemRunner(platform);
        if (asynchronous) {
            gemRunner.uninstallAsynchronously(gemNames, resetCompletionTask(asyncCompletionTask), parent);
            return false;
        } else {
            boolean ok = gemRunner.uninstall(gemNames);
            resetLocal();
            return ok;
        }
    }

    /**
     * Updates the given gems, or all gems if <code>gems == null</code>.
     *
     * @param gems the Gem descriptions for the gems to be updated. Only the names are relevant.
     * If <code>null</code>, all installed gems will be updated.
     * @param rdoc specifies whether RDoc documentation should be generated.
     * @param ri specifies whether RI documentation should be generated.
     * @param includeDependencies specifies whether the required dependent gems should be updated.
     * @param parent For asynchronous tasks, provide a parent Component that will have progress dialogs added,
     *   a possible cursor change, etc.
     * @param asynchronous If true, run the gem task asynchronously - returning immediately and running the gem task
     *    in a background thread. A progress bar and message will be displayed (along with the option to view the
     *    gem output). If the exit code is normal, the completion task will be run at the end.
     * @param asyncCompletionTask If asynchronous is true and the gem task completes normally, this task will be run at the end.
     * @return true if the update was performed synchronously and was successful, false otherwise.
     */
    public boolean update(Gem[] gems, Component parent, boolean rdoc,
            boolean ri, boolean includeDependencies, boolean asynchronous,
            Runnable asyncCompletionTask) {

        if (!checkGemHomePermissions()) {
            return false;
        }
        List<String> gemNames = gems == null ? null : mapToGemNames(gems);
        GemRunner gemRunner = new GemRunner(platform);
        if (asynchronous) {
            gemRunner.updateAsynchronously(gemNames, rdoc, ri, includeDependencies,
                    resetCompletionTask(asyncCompletionTask), parent);
            return false;
        } else {
            boolean ok = gemRunner.update(gemNames, rdoc, ri, includeDependencies);
            resetLocal();
            return ok;
        }
    }

    /**
     * Return path to the <em>gem</em> tool if it does exist.
     *
     * @return path to the <em>gem</em> tool; might be <tt>null</tt> if not
     *         found.
     */
    public String getGemTool() {
        if (!platform.hasRubyGemsInstalled()) {
            return null;
        }
        if (gemTool == null) {
            String bin = platform.getBinDir();
            if (bin != null) {
                gemTool = bin + File.separator + "gem"; // NOI18N
                if (!new File(gemTool).isFile()) {
                    gemTool = null;
                }
            }
        }
        if (gemTool == null) {
            gemTool = Util.findOnPath("gem"); // NOI18N
        }
        return gemTool;
    }

    public String getRake() {
        if (rake == null) {
            rake = platform.findExecutable("rake"); // NOI18N

            if (rake != null && !(new File(rake).exists()) && getLatestVersion("rake") != null) { // NOI18N
                // On Windows, rake does funny things - you may only get a rake.bat
                InstalledFileLocator locator = InstalledFileLocator.getDefault();
                File f =
                        locator.locate("modules/org-netbeans-modules-ruby-project.jar", // NOI18N
                        null, false); // NOI18N

                if (f == null) {
                    throw new RuntimeException("Can't find cluster"); // NOI18N
                }

                f = new File(f.getParentFile().getParentFile().getAbsolutePath() + File.separator +
                        "rake"); // NOI18N

                try {
                    rake = f.getCanonicalPath();
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }
        }

        return rake;
    }

    public boolean isValidRake(boolean warn) {
        String rakePath = getRake();
        boolean valid = (rakePath != null) && new File(rakePath).exists();
        possiblyNotifyUser(warn, valid, "rake"); // NOI18N
        return valid;
    }

    public String getRails() {
        if (rails == null) {
            rails = platform.findExecutable("rails"); // NOI18N
        }
        return rails;
    }

    public boolean isValidRails(boolean warn) {
        String railsPath = getRails();
        boolean valid = (railsPath != null) && new File(railsPath).exists();
        possiblyNotifyUser(warn, valid, "rails"); // NOI18N
        return valid;
    }

    public String getAutoTest() {
        return platform.findExecutable("autotest"); // NOI18N
    }

    public boolean isValidAutoTest(boolean warn) {
        String autoTest = getAutoTest();
        boolean valid = (autoTest != null) && new File(autoTest).exists();
        possiblyNotifyUser(warn, valid, "autotest"); // NOI18N
        return valid;
    }

    private void possiblyNotifyUser(boolean warn, boolean valid, String cmd) {
        if (warn && !valid) {
            String msg = NbBundle.getMessage(GemManager.class, "GemManager.NotInstalledCmd", cmd, platform.getLabel());
            NotifyDescriptor nd =
                    new NotifyDescriptor.Message(msg, NotifyDescriptor.Message.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }
    }

    /** Return other load path URLs (than the gem ones returned by {@link #getGemUrls} to add for the platform
     * such as the basic ruby 1.8 libraries, the site_ruby libraries, and the stub libraries for
     * the core/builtin classes.
     *
     * @return a set of URLs
     */
    public Set<URL> getNonGemLoadPath() {
        if (nonGemUrls == null) {
            initializeUrlMaps();
        }

        return nonGemUrls;
    }

    /**
     * Return a map from gem name to the version string, which is of the form
     * {@code <major>.<minor>.<tiny>[-<platform>]}, such as 1.2.3 and 1.13.5-ruby
     */
    public Map<String, String> getGemVersions() {
        if (gemVersions == null) {
            initializeUrlMaps();
        }

        return gemVersions;
    }

    /**
     * Return a map from gem name to the URL for the lib root of the current gems
     */
    public Map<String, URL> getGemUrls() {
        if (gemUrls == null) {
            initializeUrlMaps();
        }

        return gemUrls;
    }

    private void initializeUrlMaps() {
        File rubyHome = platform.getHome();

        if (rubyHome == null || !rubyHome.exists()) {
            gemVersions = Collections.emptyMap();
            gemUrls = Collections.emptyMap();
            nonGemUrls = Collections.emptySet();
            return;
        }
        try {
            gemUrls = new HashMap<String, URL>(60);
            gemVersions = new HashMap<String, String>(60);
            nonGemUrls = new HashSet<URL>(12);

            // Now registered via the standard library mechanism in RubyLanguage instead
            //FileObject rubyStubs = RubyPlatform.getRubyStubs();
            //
            //if (rubyStubs != null) {
            //    try {
            //        nonGemUrls.add(rubyStubs.getURL());
            //    } catch (FileStateInvalidException fsie) {
            //        Exceptions.printStackTrace(fsie);
            //    }
            //}

            // Install standard libraries
            // lib/ruby/1.8/
            if (!SKIP_INDEX_LIBS) {
                String rubyLibDir = platform.getVersionLibDir();
                if (rubyLibDir != null) {
                    File libs = new File(rubyLibDir);
                    assert libs.exists() && libs.isDirectory();
                    nonGemUrls.add(libs.toURI().toURL());
                }
            }

            // Install gems.
            if (!SKIP_INDEX_GEMS) {
                initGemList();
                if (RubyPlatformManager.PREINDEXING) {
                    String gemDir = getGemHome();
                    File specDir = new File(gemDir, "gems"); // NOI18N

                    if (specDir.exists()) {
                        File[] gems = specDir.listFiles();
                        for (File f : gems) {
                            if (f.getName().indexOf('-') != -1) {
                                File lib = new File(f, "lib"); // NOI18N

                                if (lib.exists() && lib.isDirectory()) {
                                    URL url = lib.toURI().toURL();
                                    nonGemUrls.add(url);
                                }
                            }
                        }
                    }
                } else if (localGems != null) {
                    Set<String> gems = localGems.keySet();
                    for (String name : gems) {
                        List<GemInfo> versions = localGems.get(name);
//                        Map<String, File> m = gemFiles.get(name);
                        assert !versions.isEmpty();
                        GemInfo newestVersion = versions.get(0);
                        File f = newestVersion.getSpecFile();
                        // Points to the specification file
                        assert f.getName().endsWith(DOT_GEM_SPEC);
                        String filename = f.getName().substring(0,
                                f.getName().length() - DOT_GEM_SPEC.length());
                        File lib = new File(f.getParentFile().getParentFile(), "gems" + // NOI18N
                                File.separator + filename + File.separator + "lib"); // NOI18N

                        if (lib.exists() && lib.isDirectory()) {
                            URL url = lib.toURI().toURL();
                            gemUrls.put(name, url);
                            String version = newestVersion.getVersion();
                            gemVersions.put(name, version);
                        }
                    }
                }
            }

            // Install site ruby - this is where rubygems lives for example
            if (!SKIP_INDEX_LIBS) {
                String rubyLibSiteDir = platform.getRubyLibSiteDir();

                if (rubyLibSiteDir != null) {
                    File siteruby = new File(rubyLibSiteDir);

                    if (siteruby.exists() && siteruby.isDirectory()) {
                        nonGemUrls.add(siteruby.toURI().toURL());
                    }
                }
            }

            // During development only:
            gemUrls = Collections.unmodifiableMap(gemUrls);
            gemVersions = Collections.unmodifiableMap(gemVersions);
            nonGemUrls = Collections.unmodifiableSet(nonGemUrls);

//            // Register boot roots. This is a bit of a hack.
//            // I need to find a better way to distinguish source directories
//            // from boot (library, gems, etc.) directories at the scanning and indexing end.
//            Language language = LanguageRegistry.getInstance().getLanguageByMimeType(RubyInstallation.RUBY_MIME_TYPE);
//            ClassIndexManager mgr = ClassIndexManager.get(language);
//            List<URL> roots = new ArrayList<URL>(gemUrls.size() + nonGemUrls.size());
//            roots.addAll(gemUrls.values());
//            roots.addAll(nonGemUrls);
//            mgr.setBootRoots(roots);
        } catch (MalformedURLException mue) {
            Exceptions.printStackTrace(mue);
        }
    }

    private List<String> mapToGemNames(Gem[] gems) {
        List<String> gemNames = new ArrayList<String>();
        for (Gem gem : gems) {
            gemNames.add(gem.getName());
        }
        return gemNames;
    }

    private Runnable resetCompletionTask(final Runnable origTask) {
        return new Runnable() {
            public void run() {
                resetLocal();
                origTask.run();
            }
        };
    }

    public static String getNotInstalledMessage() {
        return NbBundle.getMessage(GemManager.class, "GemManager.rubyGemsNotInstalled");
    }

    static boolean isValidGemHome(final File gemHomeF) {
        Parameters.notNull("gemHomeF", gemHomeF); // NOI18N
        boolean valid = gemHomeF.isDirectory();
        for (int i = 0; valid && i < TOP_LEVEL_REPO_DIRS.length; i++) {
            String dir = TOP_LEVEL_REPO_DIRS[i];
            File dirF = new File(gemHomeF, dir);
            LOGGER.finest("Checking: " + dirF);
            valid &= dirF.isDirectory();
            LOGGER.finest("valid: " + valid);
        }
        return valid;
    }

    public static void adjustEnvironment(final RubyPlatform platform, final Map<String, String> env) {
        if (platform.hasRubyGemsInstalled()) {
            String gemHome = adjustGemPath(platform.getGemManager().getGemHome());
            String gemPath = adjustGemPath(platform.getInfo().getGemPath());
            env.put("GEM_HOME", gemHome); // NOI18N
            env.put("GEM_PATH", gemPath); // NOI18N
        }
    }

    private static String adjustGemPath(final String origPath) {
        // it's needed on Windows, otherwise gem tool fails
        return Utilities.isWindows() ? origPath.replace('\\', '/') : origPath;
    }

    public interface VersionPredicate {

        /**
         * Returns true if given version is suitable.
         * @param version version to be checked
         * @return true if given version is suitable
         */
        boolean isRight(String version);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GemManager other = (GemManager) obj;
        if (this.platform != other.platform && (this.platform == null || !this.platform.equals(other.platform))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + (this.platform != null ? this.platform.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "RubyPlatform[platform:" + platform + "]"; // NOI18N
    }

}
