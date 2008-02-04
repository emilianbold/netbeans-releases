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
package org.netbeans.modules.ruby.platform.gems;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.api.ruby.platform.RubyPlatformManager;
import org.netbeans.modules.gsfret.source.usages.ClassIndexManager;
import org.netbeans.modules.ruby.platform.DebuggerPreferences;
import org.netbeans.modules.ruby.platform.Util;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.Utilities;

/**
 * Class which handles gem interactions - executing gem, installing, uninstalling, etc.
 *
 * @todo Use the new ExecutionService to do process management.
 *
 * @author Tor Norbye
 */
public final class GemManager {

    private static final Logger LOGGER = Logger.getLogger(GemManager.class.getName());
    
    /** Top level directories inside the Gem repository. */
    private static final String[] TOP_LEVEL_REPO_DIRS = { "cache", "specifications", "gems", "doc" }; // NOI18N
    
    /** Directory inside the GEM_HOME directory. */
    private static final String SPECIFICATIONS = "specifications"; // NOI18N
    
    /**
     * Regexp for matching version number in gem packages:  name-x.y.z (we need
     * to pull out x,y,z such that we can do numeric comparisons on them)
     */
    private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)(-\\S+)?"); // NOI18N
    
    private static final boolean PREINDEXING = Boolean.getBoolean("gsf.preindexing");

    private static boolean SKIP_INDEX_LIBS = System.getProperty("ruby.index.nolibs") != null; // NOI18N
    private static boolean SKIP_INDEX_GEMS = System.getProperty("ruby.index.nogems") != null; // NOI18N

    /**
     * Extension of files containing gems specification residing in {@link
     * #SPECIFICATIONS}.
     */
    private static final String DOT_GEM_SPEC = ".gemspec"; // NOI18N

    /** Map&lt;gemName, Map&lt;version, specFile&gt;&gt; */
    private Map<String, Map<String, File>> gemFiles;
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
    
    public GemManager(final RubyPlatform platform) {
        assert platform.hasRubyGemsInstalled() : "called when RubyGems installed";
        this.platform = platform;
    }

    private String getGemMissingMessage() {
        if (Utilities.isMac() && "/usr/bin/ruby".equals(platform.getInterpreter())) { // NOI18N
            String version = System.getProperty("os.version"); // NOI18N
            if (version == null || version.startsWith("10.4")) { // Only a problem on Tiger // NOI18N
                return NbBundle.getMessage(GemAction.class, "GemMissingMac");
            }
        }
        return NbBundle.getMessage(GemAction.class, "GemMissing");
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
            return NbBundle.getMessage(GemAction.class, "CannotFindGemRepository");
        }

        File gemHome = new File(gemHomePath);
        
        if (!gemHome.exists()) {
            // Is this possible? (Installing gems, but no gems installed yet
            return null;
        }
        
        if (!gemHome.canWrite()) {
            return NbBundle.getMessage(GemAction.class, "GemNotWritable");
        }
        
        return null;
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

    /** Returns paths to all Gem repositories. */
    public List<String> getRepositories() {
        List<String> repos = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(platform.getInfo().getGemPath(), File.pathSeparator);
        while (st.hasMoreTokens()) {
            repos.add(st.nextToken());
        }
        return repos;
    }
    
    public void addRepository(final String path) {
        List<String> gemPath = getRepositories();
        gemPath.add(path);
        storeRepositories(gemPath);
        DebuggerPreferences prefs = DebuggerPreferences.getInstance();
        if (!platform.isJRuby() && platform.hasFastDebuggerInstalled()) {
            prefs.setUseClassicDebugger(platform, false);
        }
    }

    public void removeRepository(final String path) {
        List<String> gemPath = getRepositories();
        gemPath.remove(path);
        storeRepositories(gemPath);
    }

    private void storeRepositories(final List<String> gemPath) {
        StringBuilder pathSB = new StringBuilder();
        for (String token : gemPath) {
            if (pathSB.length() != 0) {
                pathSB.append(File.pathSeparator);
            }
            pathSB.append(token);
        }
        platform.getInfo().setGemPath(pathSB.toString());
        try {
            RubyPlatformManager.storePlatform(platform);
        } catch (IOException ioe) {
            LOGGER.log(Level.SEVERE, ioe.getLocalizedMessage(), ioe);
        }
        resetLocal();
    }
    
    /** Return > 0 if version1 is greater than version 2, 0 if equal and -1 otherwise */
    public static int compareGemVersions(String version1, String version2) {
        if (version1.equals(version2)) {
            return 0;
        }

        Matcher matcher1 = VERSION_PATTERN.matcher(version1);

        if (matcher1.matches()) {
            int major1 = Integer.parseInt(matcher1.group(1));
            int minor1 = Integer.parseInt(matcher1.group(2));
            int micro1 = Integer.parseInt(matcher1.group(3));

            Matcher matcher2 = VERSION_PATTERN.matcher(version2);

            if (matcher2.matches()) {
                int major2 = Integer.parseInt(matcher2.group(1));
                int minor2 = Integer.parseInt(matcher2.group(2));
                int micro2 = Integer.parseInt(matcher2.group(3));

                if (major1 != major2) {
                    return major1 - major2;
                }

                if (minor1 != minor2) {
                    return minor1 - minor2;
                }

                if (micro1 != micro2) {
                    return micro1 - micro2;
                }
            } else {
                // TODO uh oh
                //assert false : "no version match on " + version2;
            }
        } else {
            // TODO assert false : "no version match on " + version1;
        }

        // Just do silly alphabetical comparison
        return version1.compareTo(version2);
    }

    /**
     * Checks whether a gem with the given name is installed in the gem
     * repository used by the currently set Ruby interpreter.
     *
     * @param gemName name of a gem to be checked
     * @return <tt>true</tt> if installed; <tt>false</tt> otherwise
     */
    public boolean isGemInstalled(final String gemName) {
        return getVersion(gemName) != null;
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
        String currVersion = getVersion(gemName);
        return currVersion != null && GemManager.compareGemVersions(version, currVersion) <= 0;
    }

    public boolean isGemInstalledForPlatform(final String gemName, final String version) {
        String currVersion = getVersionForPlatform(gemName);
        return currVersion != null && GemManager.compareGemVersions(version, currVersion) <= 0;
        
    }
    
    public String getVersion(String gemName) {
        // TODO - use gemVersions map instead!
        initGemList();

        if (gemFiles == null) {
            return null;
        }

        Map<String, File> highestVersion = gemFiles.get(gemName);

        if ((highestVersion == null) || (highestVersion.size() == 0)) {
            return null;
        }

        return highestVersion.keySet().iterator().next();
    }

    public String getVersionForPlatform(String gemName) {
        // TODO - use gemVersions map instead!
        initGemList();

        if (gemFiles == null) {
            return null;
        }

        Map<String, File> versionsToSpecs = gemFiles.get(gemName);

        if ((versionsToSpecs == null) || (versionsToSpecs.size() == 0)) {
            return null;
        }

        // filtering
        for (Map.Entry<String, File> versionToSpec : versionsToSpecs.entrySet()) {
            String specName = versionToSpec.getValue().getName();
            // filter out all java gems for non-java platforms
            // hack until we support proper .gemspec parsing
            if (!platform.isJRuby() && specName.endsWith("-java.gemspec")) { // NOI18N
                continue;
            }

            // special hack for fast debugger
            if (specName.startsWith("ruby-debug-base-")) {
                boolean forJavaPlaf = specName.endsWith("-java.gemspec");
                if (platform.isJRuby() && !forJavaPlaf) {
                    continue;
                }
                if (!platform.isJRuby() && forJavaPlaf) {
                    continue;
                }
            }
            return versionToSpec.getKey();
        }

        return null;
    }

    /**
     * Logs the installed gems using the given logging level.
     */ 
    private void logGems(Level level) {
        if (!LOGGER.isLoggable(level)) {
            return;
        }
        
        if (gemFiles == null) {
            LOGGER.log(level, "No gems found, gemFiles is null");
            return;
        }
        
        LOGGER.log(level, "Found " + gemFiles.size() + " gems.");
        for (String key : gemFiles.keySet()) {
            Map<String, File> value = gemFiles.get(key);
            LOGGER.log(level, key + " has " + (value == null ? "null" : "" + value.size()) + " version(s):");
            for (String version : value.keySet()) {
                LOGGER.log(level, version + " at " + value.get(version));
            }
        }
    }
    
    private void initGemList() {
        if (gemFiles == null) {
            // Initialize lazily
            assert platform.hasRubyGemsInstalled() : "asking for gems only when RubyGems are installed";
            List<String> repos = getRepositories();
            repos.add(0, getGemHome()); // XXX is not a GEM_HOME always part of GEM_PATH
            gemFiles = new HashMap<String, Map<String, File>>();
            for (String gemDir : repos) {
                File specDir = new File(gemDir, SPECIFICATIONS);
                if (specDir.exists()) {
                    LOGGER.finest("Initializing \"" + gemDir + "\" repository");
                    // Add each of */lib/
                    File[] specFiles = specDir.listFiles();
                    if (specFiles != null) {
                        chooseGems(specFiles, gemFiles);
                    }
                } else {
                    LOGGER.finest("Cannot find Gems repository. \"" + gemDir + "\" does not exist or is not a directory."); // NOI18N
                }
                logGems(Level.FINEST);
            }
        }
    }

    /** 
     * Given a list of files that may represent gems, choose the most recent
     * version of each.
     */
    private static File[] chooseGems(final  File[] specFiles, final Map<String, Map<String, File>> gemFiles) {
        GemFilesParser gemFilesParser = new GemFilesParser(specFiles);
        gemFilesParser.chooseGems();
        gemFiles.putAll(gemFilesParser.getGemMap());
        return gemFilesParser.getFiles();
    }

    public Set<String> getInstalledGemsFiles() {
        initGemList();

        if (gemFiles == null) {
            return Collections.emptySet();
        }

        return gemFiles.keySet();
    }

    public void reset() {
        resetRemote();
        resetLocal();
        gemHomeUrl = null;
    }
    
    public void resetRemote() {
        remote = null;
    }
    
    public void resetLocal() {
        installed = null;
        gemFiles = null;
        platform.fireGemsChanged();
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
     * @return list of the installed gems. Returns an empty list if they could not
     * be read, never null. 
     */
    public List<Gem> getInstalledGems(List<String> errors) {
        reloadIfNeeded(errors);
        return installed != null ? installed : Collections.<Gem>emptyList();
    }
    
    /**
     * Gets the available remote gems. <em>WARNING:</em> Slow! Synchronous gem execution.
     * 
     * @param errors list to which the errors, which happen during gems
     *        reload, will be accumulated 
     * @return list of the available remote gems. Returns an empty list if they could not
     * be read, never null. 
     */
    public List<Gem> getRemoteGems(List<String> errors) {
        reloadIfNeeded(errors);
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
            DialogDisplayer.getDefault().notify(nd);
            return false;
        }
        return true;
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
        if (!checkGemProblem()) {
            return;
        }
        
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
     * Install the latest version of the given gem with dependencies and refresh
     * IDE caches accordingly after the gem is installed.
     *
     * @param gem gem to install
     * @param rdoc if true, generate rdoc as part of the installation
     * @param ri if true, generate ri data as part of the installation
     */
    public void installGem(final String gem, final boolean rdoc, final boolean ri) {
        final Gem[] gems = new Gem[] {
            new Gem(gem, null, null)
        };
        Runnable installationComplete = new Runnable() {
            public void run() {
                platform.recomputeRoots();
            }
        };
        install(gems, null, rdoc, ri, null, true, true, installationComplete);
    }

    /**
     * Install the given gems.
     *
     * @param gem Gem description for the gem to be installed. Only the name is relevant.
     * @param parent For asynchronous tasks, provide a parent Component that will have progress dialogs added,
     *   a possible cursor change, etc.
     * @param progressHandle If the task is not asynchronous, use the given handle for progress notification.
     * @param asynchronous If true, run the gem task asynchronously - returning immediately and running the gem task
     *    in a background thread. A progress bar and message will be displayed (along with the option to view the
     *    gem output). If the exit code is normal, the completion task will be run at the end.
     * @param asyncCompletionTask If asynchronous is true and the gem task completes normally, this task will be run at the end.
     * @param rdoc If true, generate rdoc as part of the installation
     * @param ri If true, generate ri data as part of the installation
     * @param version If non null, install the specified version rather than the latest remote version
     */
    public boolean install(Gem[] gems, Component parent, boolean rdoc, boolean ri,
            String version, boolean includeDeps, boolean asynchronous,
            final Runnable asyncCompletionTask) {
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
     * Uninstall the given gem.
     *
     * @param gem Gem description for the gem to be uninstalled. Only the name is relevant.
     * @param parent For asynchronous tasks, provide a parent Component that will have progress dialogs added,
     *   a possible cursor change, etc.
     * @param progressHandle If the task is not asynchronous, use the given handle for progress notification.
     * @param asynchronous If true, run the gem task asynchronously - returning immediately and running the gem task
     *    in a background thread. A progress bar and message will be displayed (along with the option to view the
     *    gem output). If the exit code is normal, the completion task will be run at the end.
     * @param asyncCompletionTask If asynchronous is true and the gem task completes normally, this task will be run at the end.
     */
    public boolean uninstall(Gem[] gems, Component parent, boolean asynchronous, final Runnable asyncCompletionTask) {
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
     * Update the given gem, or all gems if gem == null
     *
     * @param gem Gem description for the gem to be uninstalled. Only the name is relevant. If null, all installed gems
     *    will be updated.
     * @param parent For asynchronous tasks, provide a parent Component that will have progress dialogs added,
     *   a possible cursor change, etc.
     * @param progressHandle If the task is not asynchronous, use the given handle for progress notification.
     * @param asynchronous If true, run the gem task asynchronously - returning immediately and running the gem task
     *    in a background thread. A progress bar and message will be displayed (along with the option to view the
     *    gem output). If the exit code is normal, the completion task will be run at the end.
     * @param asyncCompletionTask If asynchronous is true and the gem task completes normally, this task will be run at the end.
     */
    public boolean update(Gem[] gems, Component parent, boolean rdoc,
            boolean ri, boolean asynchronous, Runnable asyncCompletionTask) {
        List<String> gemNames = gems == null ? null : mapToGemNames(gems);
        GemRunner gemRunner = new GemRunner(platform);
        if (asynchronous) {
            gemRunner.updateAsynchronously(gemNames, rdoc, ri, resetCompletionTask(asyncCompletionTask), parent);
            return false;
        } else {
            boolean ok = gemRunner.update(gemNames, rdoc, ri);
            resetLocal();
            return ok;
        }
    }

    public String getAutoTest() {
        return platform.findExecutable("autotest"); // NOI18N
    }

    public boolean isValidAutoTest(boolean warn) {
        String autoTest = getAutoTest();
        boolean valid = (autoTest != null) && new File(autoTest).exists();

        if (warn && !valid) {
            String msg = NbBundle.getMessage(GemManager.class, "GemManager.NotInstalledCmd", "autotest");
            NotifyDescriptor nd =
                    new NotifyDescriptor.Message(msg, NotifyDescriptor.Message.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }

        return valid;
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

            if (rake != null && !(new File(rake).exists()) && getVersion("rake") != null) { // NOI18N
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

        if (warn && !valid) {
            String msg = NbBundle.getMessage(GemManager.class, "GemManager.NotInstalledCmd", "rake");
            NotifyDescriptor nd =
                    new NotifyDescriptor.Message(msg, NotifyDescriptor.Message.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }

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

        if (warn && !valid) {
            String msg = NbBundle.getMessage(GemManager.class, "GemManager.NotInstalledCmd", "rails");
            NotifyDescriptor nd =
                    new NotifyDescriptor.Message(msg, NotifyDescriptor.Message.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }

        return valid;
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

            FileObject rubyStubs = platform.getRubyStubs();

            if (rubyStubs != null) {
                try {
                    nonGemUrls.add(rubyStubs.getURL());
                } catch (FileStateInvalidException fsie) {
                    Exceptions.printStackTrace(fsie);
                }
            }

            // Install standard libraries
            // lib/ruby/1.8/ 
            if (!SKIP_INDEX_LIBS) {
                String rubyLibDir = platform.getLibDir();
                if (rubyLibDir != null) {
                    File libs = new File(rubyLibDir);
                    assert libs.exists() && libs.isDirectory();
                    nonGemUrls.add(libs.toURI().toURL());
                }
            }

            // Install gems.
            if (!SKIP_INDEX_GEMS) {
                initGemList();
                if (PREINDEXING) {
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
                } else if (gemFiles != null) {
                    Set<String> gems = gemFiles.keySet();
                    for (String name : gems) {
                        Map<String, File> m = gemFiles.get(name);
                        assert m.keySet().size() == 1;
                        File f = m.values().iterator().next();
                        // Points to the specification file
                        assert f.getName().endsWith(DOT_GEM_SPEC);
                        String filename = f.getName().substring(0,
                                f.getName().length() - DOT_GEM_SPEC.length());
                        File lib = new File(f.getParentFile().getParentFile(), "gems" + // NOI18N
                                File.separator + filename + File.separator + "lib"); // NOI18N

                        if (lib.exists() && lib.isDirectory()) {
                            URL url = lib.toURI().toURL();
                            gemUrls.put(name, url);
                            String version = m.keySet().iterator().next();
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

            // Register boot roots. This is a bit of a hack.
            // I need to find a better way to distinguish source directories
            // from boot (library, gems, etc.) directories at the scanning and indexing end.
            ClassIndexManager mgr = ClassIndexManager.getDefault();
            List<URL> roots = new ArrayList<URL>(gemUrls.size() + nonGemUrls.size());
            roots.addAll(gemUrls.values());
            roots.addAll(nonGemUrls);
            mgr.setBootRoots(roots);
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
        Parameters.notNull("gemHomeF", gemHomeF);
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
        String gemHome = adjustGemPath(platform.getGemManager().getGemHome());
        String gemPath = adjustGemPath(platform.getInfo().getGemPath());
        env.put("GEM_HOME", gemHome); // NOI18N
        env.put("GEM_PATH", gemPath); // NOI18N
    }

    private static String adjustGemPath(final String origPath) {
        // it's needed on Windows, otherwise gem tool fails
        return Utilities.isWindows() ? origPath.replace('\\', '/') : origPath;
    }

}
