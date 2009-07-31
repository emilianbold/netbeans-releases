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

package org.netbeans.modules.versioning.system.cvss;

import org.netbeans.modules.versioning.util.ListenersSupport;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.netbeans.lib.cvsclient.admin.AdminHandler;
import org.netbeans.lib.cvsclient.admin.Entry;
import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.command.add.AddCommand;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.lib.cvsclient.file.FileHandler;
import org.netbeans.lib.cvsclient.file.FileUtils;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.util.Context;
import org.netbeans.modules.versioning.system.cvss.ui.syncview.CvsSynchronizeTopComponent;
import org.netbeans.modules.versioning.spi.VCSAnnotator;
import org.netbeans.modules.versioning.spi.VCSInterceptor;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.netbeans.api.queries.SharabilityQuery;
import org.openide.ErrorManager;
import org.openide.util.RequestProcessor;
import org.openide.filesystems.*;

import javax.swing.*;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * A singleton CVS manager class, center of CVS module. Use {@link #getInstance()} to get access
 * to CVS module functionality.
 * 
 * @author Maros Sandor
 */
public class CvsVersioningSystem {

    private static CvsVersioningSystem instance;
    
    public static final String FILENAME_CVSIGNORE = ".cvsignore"; // NOI18N
    public static final String FILENAME_CVS = "CVS"; // NOI18N

    public static final Object EVENT_PARAM_CHANGED = new Object();
    public static final Object PARAM_BATCH_REFRESH_RUNNING = new Object();
    public static final Object EVENT_VERSIONED_FILES_CHANGED = new Object();

    public static final Object EVENT_REFRESH_ANNOTATIONS = new Object();    

    public static final String FILENAME_CVS_REPOSITORY = FILENAME_CVS + "/Repository"; // NOI18N
    public static final String FILENAME_CVS_ENTRIES = FILENAME_CVS + "/Entries"; // NOI18N

    /**
     * Extensions to be treated as text although MIME type may suggest otherwise.
     */ 
    private static final Set textExtensions = new HashSet(Arrays.asList(new String [] { "txt", "xml", "html", "properties", "mf", "jhm", "hs", "form" })); // NOI18N
    public static Logger LOG = Logger.getLogger(CvsVersioningSystem.class.getName());
    private final Map<String, ClientRuntime> clientsCache = new HashMap<String, ClientRuntime>();
    private final Map params = new HashMap();

    private GlobalOptions defaultGlobalOptions;
    private FileStatusCache fileStatusCache;

    private CvsLiteAdminHandler sah;
    private CvsLiteFileHandler  workdirFileHandler;
    private CvsLiteGzippedFileHandler workdirGzippedFileHandler;
    private FilesystemHandler filesystemHandler;
    private VCSAnnotator fileStatusProvider;

    private Annotator annotator;

    private final Set<Pattern>   userIgnorePatterns = new HashSet<Pattern>();
    private boolean     userIgnorePatternsReset;
    private long        userIgnorePatternsTimestamp;

    private final Set<File> unignoreOverride = new HashSet<File>(1);

    private final Set<File> alreadyGeneratedFiles = new HashSet<File>(5);

    private final Set<File> unversionedParents = Collections.synchronizedSet(new HashSet<File>(20));
    
    public static synchronized CvsVersioningSystem getInstance() {
        if (instance == null) {
            instance = new CvsVersioningSystem();
            instance.init();
        }
        return instance;
    }

    private void init() {
        defaultGlobalOptions = CvsVersioningSystem.createGlobalOptions();
        sah = new CvsLiteAdminHandler();
        workdirFileHandler = new CvsLiteFileHandler();
        FileUtils.setFileReadOnlyHandler(workdirFileHandler);
        workdirGzippedFileHandler = new CvsLiteGzippedFileHandler();
        fileStatusCache = new FileStatusCache(this);
        filesystemHandler  = new FilesystemHandler(this);
        annotator = new Annotator(this);
        fileStatusProvider = new FileStatusProvider();
    }

    void shutdown() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    CvsSynchronizeTopComponent.getInstance().close();
                } catch (Throwable e) {
                    // ignore, this component is already invalid
                }
            }
        });
    }
    
    private CvsVersioningSystem() {
    }

    public CvsFileTableModel getFileTableModel(Context context, int displayStatuses) {
        return new CvsFileTableModel(context, displayStatuses);
    }
    
    /**
     * Determines correct CVS client from the given cvs root.
     * 
     * @param cvsRoot root never <code>null</code>
     * @return
     */ 
    public ClientRuntime getClientRuntime(String cvsRoot) {
 
            cvsRoot.length();  // rise NPE

            ClientRuntime clientRuntime;
            synchronized(clientsCache) {
                clientRuntime = clientsCache.get(cvsRoot);
                if (clientRuntime == null) {
                    clientRuntime = new ClientRuntime(cvsRoot);
                    clientsCache.put(cvsRoot, clientRuntime);
                }
            }
            return clientRuntime;
    }

    /**
     * Determines CVS root for the given command.
     * 
     * @param cmd a CVS command
     * @return CVSRoot the command will execute in
     * @throws NotVersionedException if the root cannot be determined (no CVS/Root file or unsupported command)
     */ 
    String detectCvsRoot(Command cmd) throws NotVersionedException {
        File [] files;

        if (cmd instanceof AddCommand) {
            AddCommand c = (AddCommand) cmd;
            files = c.getFiles();
        } else if (cmd instanceof BasicCommand) {
            BasicCommand c = (BasicCommand) cmd;
            files = c.getFiles();
        } else {
            throw new NotVersionedException("Cannot determine CVSRoot for command: " + cmd); // NOI18N
        }

        File oneFile = files[0];
        try {
            return Utils.getCVSRootFor(oneFile);
        } catch (IOException e) {
            throw new NotVersionedException("Cannot determine CVSRoot for: " + oneFile); // NOI18N
        }

    }

    /**
     * Executes this command asynchronously, in a separate thread, and returns immediately. The command may
     * or may not execute immediately, depending on previous commands sent to the CVS client that may be
     * still waiting for execution.
     *  
     * @param cmd command to execute
     * @param mgr listener for events the command produces
     * @throws CommandException
     * @throws AuthenticationException
     */
    public RequestProcessor.Task post(Command cmd, ExecutorSupport mgr) throws CommandException,
            AuthenticationException, NotVersionedException, IllegalCommandException,
            IOException {
        return post(cmd, defaultGlobalOptions, mgr);
    }

    /**
     * Schedules given command for execution.
     * @param cmd
     * @param options Global options to use, may be set to null to use default options
     * @param mgr
     * @return already scheduled task
     * @throws IllegalCommandException if the command is not valid, e.g. it contains files that cannot be
     * processed by a single command (they do not have a common filesystem root OR their CVS Roots differ)
     */
    public RequestProcessor.Task post(Command cmd, GlobalOptions options, ExecutorSupport mgr) throws IllegalCommandException {
        ClientRuntime clientRuntime = getClientRuntime(cmd, options);
        RequestProcessor.Task task = clientRuntime.createTask(cmd, options != null ? options : defaultGlobalOptions, mgr);
        task.schedule(0);
        return task;
    }

    /**
     * Gets client runtime (a repository session).
     *
     * @return runtime never <code>null</code>
     */
    public ClientRuntime getClientRuntime(Command cmd, GlobalOptions options) {
        String root;
        if (options != null && options.getCVSRoot() != null) {
            root = options.getCVSRoot();
        } else {
            try {
                root = detectCvsRoot(cmd);
            } catch (NotVersionedException e) {
                if (options == null) return null;
                root = options.getCVSRoot();
            }
        }
        return getClientRuntime(root);
    }

    public FileStatusCache getStatusCache() {
        return fileStatusCache;
    }
    
    ListenersSupport listenerSupport = new ListenersSupport(this);
    public void addVersioningListener(VersioningListener listener) {
        listenerSupport.addListener(listener);
    }

    public void removeVersioningListener(VersioningListener listener) {
        listenerSupport.removeListener(listener);
    }

    /**
     * Returns true for files that were originally ignored by default (.exe files for example) but the user 
     * explicitly invoked CVS/Unignore on them.
     * 
     * @param file a file to test
     * @return true if the file was explicitly Unignored by user
     */
    boolean isUnignored(final File file) {
        return unignoreOverride.contains(file);
    }
    
    /**
     * Checks if the file is ignored by CVS module. This method assumes that the file is managed so
     * if you do not know this beforehand, you have to call isManaged() first.
     *
     * @param file file to be tested
     * @return true, if the file is ignored by CVS, false otherwise.
     */
    boolean isIgnored(final File file) {
        if (file.isDirectory()) {
            File cvsRepository = new File(file, FILENAME_CVS_REPOSITORY);
            if (cvsRepository.canRead()) return false;
        }
        String name = file.getName();

        // backward compatability #68124
        if (".nbintdb".equals(name)) {  // NOI18N
            return true;
        }
        
        if (isUnignored(file)) return false;

        Set<Pattern> patterns = new HashSet<Pattern>(Arrays.asList(CvsModuleConfig.getDefault().getIgnoredFilePatterns()));
        addUserPatterns(patterns);
        addCvsIgnorePatterns(patterns, file.getParentFile());

        for (Iterator i = patterns.iterator(); i.hasNext();) {
            Pattern pattern = (Pattern) i.next();
            if (pattern.matcher(name).matches()) return true;
        }

        // #67900 global sharability query will report .cvsignore as not sharable
        if (FILENAME_CVSIGNORE.equals(name)) return false;
        
        int sharability = SharabilityQuery.getSharability(file);
        if (sharability == SharabilityQuery.NOT_SHARABLE) {
            // BEWARE: In NetBeans VISIBILTY == SHARABILITY ... and we hide Locally Removed folders => we must not Ignore them by mistake
            if (CvsVisibilityQuery.isHiddenFolder(file)) {
                return false;
            }
            // #90564: make sure that we only try to generate the .cvsignore file ONCE and if the user deletes it
            // the file will NOT be re-generated. Do this only for auto-generated .cvsignore files.
            File cvsIgnoreFile = new File(file.getParentFile(), FILENAME_CVSIGNORE);
            if (file.exists() && !cvsIgnoreFile.exists()) {
                if (!alreadyGeneratedFiles.add(cvsIgnoreFile)) return true;
            }
            try {
                setIgnored(file);
            } catch (IOException e) {
                // strange, but does no harm
            }
            return true;
        } else {
            return false;
        }
    }
    
    private void addUserPatterns(Set<Pattern> patterns) {
        File userIgnores = new File(System.getProperty("user.home"), FILENAME_CVSIGNORE); // NOI18N
        long lm = userIgnores.lastModified();
        if (lm > userIgnorePatternsTimestamp || lm == 0 && userIgnorePatternsTimestamp > 0) {
            userIgnorePatternsTimestamp = lm;
            parseUserPatterns(userIgnores);
        }
        if (userIgnorePatternsReset) {
            patterns.clear();
        }
        patterns.addAll(userIgnorePatterns);
    }

    private void parseUserPatterns(File userIgnores) {
        userIgnorePatternsReset = false;
        userIgnorePatterns.clear();
        BufferedReader r = null;
        try {
            r = new BufferedReader(new FileReader(userIgnores));
            String s;
            while ((s = r.readLine()) != null) {
                if ("!".equals(s)) { // NOI18N
                    userIgnorePatternsReset = true;
                    userIgnorePatterns.clear();
                } else {
                    try {
                        userIgnorePatterns.add(sh2regex(s));
                    } catch (IOException e) {
                        // unsupported pattern
                    }
                }
            }
        } catch (IOException e) {
            // user has invalid ignore list, ignore it
        } finally {
            if (r != null) try { r.close(); } catch (IOException e) {}
        }
    }

    /**
     * Converts shell file pattern to regex pattern.
     * 
     * @param s unix shell pattern
     * @return regex patterm
     * @throws IOException if this shell pattern is not supported
     */ 
    private static Pattern sh2regex(String s) throws IOException {
        // TODO: implement full SH->REGEX convertor
        s = s.replaceAll("\\.", "\\\\."); // NOI18N
        s = s.replaceAll("\\*", ".*"); // NOI18N
        s = s.replaceAll("\\?", "."); // NOI18N
        try {
            return Pattern.compile(s);
        } catch (PatternSyntaxException e) {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Tests whether a file or directory should receive the STATUS_NOTVERSIONED_NOTMANAGED status. 
     * All files and folders that have a parent with CVS/Repository file are considered versioned.
     * 
     * @param file a file or directory
     * @return false if the file should receive the STATUS_NOTVERSIONED_NOTMANAGED status, true otherwise
     */ 
    public static boolean isManaged(File file) {
        return VersioningSupport.getOwner(file) instanceof CVS && !Utils.isPartOfCVSMetadata(file);
    }

    public void versionedFilesChanged() {
        unversionedParents.clear();        
        listenerSupport.fireVersioningEvent(EVENT_VERSIONED_FILES_CHANGED);
    }

    /**
     * Tests whether the file is managed by this versioning system. If it is, the method should return the topmost 
     * parent of the file that is still versioned.
     *  
     * @param file a file
     * @return File the file itself or one of its parents or null if the supplied file is NOT managed by this versioning system
     */
    File getTopmostManagedParent(File file) {
        long t = System.currentTimeMillis();
        LOG.log(Level.FINE, "getTopmostManagedParent {0}", new Object[] { file });
        if(unversionedParents.contains(file)) {
            LOG.fine(" cached as unversioned");
            return null;
        }
        if (Utils.isPartOfCVSMetadata(file)) {
            LOG.fine(" part of metaddata");
            for (;file != null; file = file.getParentFile()) {
                if (file.getName().equals(FILENAME_CVS) && (file.isDirectory() || !file.exists())) {
                    file = file.getParentFile();
                    LOG.log(Level.FINE, " will use parent {0}", new Object[] { file });
                    break;
                }
            }
        }

        Set<File> done = new HashSet<File>();
        File topmost = null;
        for (; file != null; file = file.getParentFile()) {
            if(unversionedParents.contains(file)) {
                LOG.log(Level.FINE, " already known as unversioned {0}", new Object[] { file });
                break;
            }
            if (org.netbeans.modules.versioning.util.Utils.isScanForbidden(file)) break;
            if (Utils.containsMetadata(file)) {
                LOG.log(Level.FINE, " found managed parent {0}", new Object[] { file });
                topmost = file;
                done.clear();   // all folders added before must be removed, they ARE in fact managed by CVS
            } else {
                LOG.log(Level.FINE, " found unversioned {0}", new Object[] { file });
                if(file.exists()) { // could be created later ...
                    done.add(file);
                }
            }
        }
        if(done.size() > 0) {
            LOG.log(Level.FINE, " storing unversioned");
            unversionedParents.addAll(done);
        }
        if(LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, " getTopmostManagedParent returns {0} after {1} millis", new Object[] { topmost, System.currentTimeMillis() - t });
        }
        return topmost;
    }

    private void addCvsIgnorePatterns(Set<Pattern> patterns, File file) {
        Set<String> shPatterns;
        try {
            shPatterns = readCvsIgnoreEntries(file);
        } catch (IOException e) {
            // ignore invalid entries
            return;
        }
        for (Iterator i = shPatterns.iterator(); i.hasNext();) {
            String shPattern = (String) i.next();
            if ("!".equals(shPattern)) { // NOI18N
                patterns.clear();
            } else {
                try {
                    patterns.add(sh2regex(shPattern));
                } catch (IOException e) {
                    // unsupported pattern
                }
            }
        }
    }
    
     private boolean isInCvsIgnore(File file) {
        try {
            String patternToIgnore = computePatternToIgnore(file.getName());
            return readCvsIgnoreEntries(file.getParentFile()).contains(patternToIgnore);
        } catch (IOException e) {
            ErrorManager.getDefault().notify(e);
            return false;
        }
    }
    
    public boolean isIgnoredFilename(File file) {
        if (FILENAME_CVS.equals(file.getName())) return true;
        return false;
    }
    

    public AdminHandler getAdminHandler() {
        return sah;
    }

    public FileHandler getFileHandler() {
        return workdirFileHandler;
    }

    public FileHandler getGzippedFileHandler() {
        return workdirGzippedFileHandler;
    }

    public Annotator getAnnotator() {
        return annotator;
    }
    
    public Object getParameter(Object key) {
        synchronized(params) {
            return params.get(key);
        }
    }

    public KeywordSubstitutionOptions getDefaultKeywordSubstitution(File file) {
        // TODO: Let user configure defaults
        return isText(file) || isBinary(file) == false ?
                KeywordSubstitutionOptions.DEFAULT : 
                KeywordSubstitutionOptions.BINARY;
    }

    /**
     * @return true if the file is almost certainly textual.
     */
    public boolean isText(File file) {
        if (FILENAME_CVSIGNORE.equals(file.getName())) {
            return true;            
        }
        // honor Entries, only if this fails use MIME type, etc.
        try {
            Entry entry = sah.getEntry(file);
            if (entry != null) {
                return !entry.isBinary();
            }
        } catch (IOException e) {
            // ignore, probably new or nonexistent file
        }
        if(org.netbeans.modules.versioning.util.Utils.isFileContentText(file)) {
            return true;
        }

        // TODO: HACKS begin, still needed?
        int idx = file.getName().lastIndexOf('.');
        return idx != -1 && textExtensions.contains(file.getName().substring(idx + 1));
    }

    /**
     * Uses first 1024 bytes test. A control byte means binary.
     * @return true if the file is almost certainly binary.
     */
    public boolean isBinary(File file) {
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            in = new BufferedInputStream(in);
            for (int i = 0; i<1024; i++) {
                int ch = in.read();
                if (ch == -1) break;
                if (ch < 32 && ch != '\t' && ch != '\n' && ch != '\r') {
                    return true;
                }
            }
        } catch (IOException e) {
            ErrorManager err = ErrorManager.getDefault();
            err.notify(ErrorManager.INFORMATIONAL, e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException alreadyClosed) {
                    // ignore
                }
            }
        }
        return false;
    }

    public void setParameter(Object key, Object value) {
        Object old; 
        synchronized(params) {
            old = params.put(key, value);
        }
        if (old != value) listenerSupport.fireVersioningEvent(EVENT_PARAM_CHANGED, key);
    }

    /**
     * Adds all supplied files to 'cvsignore' file. They need not reside in the same folder.
     * 
     * @param files files to ignore
     */ 
    public void setIgnored(File[] files) {
        for (int i = 0; i < files.length; i++) {
            try {
                setIgnored(files[i]);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
    }

    /**
     * Adds supplied file to 'cvsignore' file.
     * 
     * @param file file to ignore
     */ 
    private void setIgnored(File file) throws IOException {
        if (file.exists()) {
            addToCvsIgnore(file);
        }
    }

    void setNotUnignored(File file) {
        unignoreOverride.remove(file);
    }
    
    public void setNotignored(File[] files) {
        for (File file : files) {
            if (isInCvsIgnore(file)) {
                try {
                    removeFromCvsIgnore(file);
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(e);
                }
            } else {
                unignoreOverride.add(file);
                fileStatusCache.refresh(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
            }
        }
    }
        
    private void addToCvsIgnore(File file) throws IOException {
        
        Set<String> entries = readCvsIgnoreEntries(file.getParentFile());
        String patternToIgnore = computePatternToIgnore(file.getName());
        if (entries.add(patternToIgnore)) {
            writeCvsIgnoreEntries(file.getParentFile(), entries);
        }
    }
    
    private String computePatternToIgnore(String name) {
        return name.replace(' ', '?');
    }

    private void removeFromCvsIgnore(File file) throws IOException {
        Set entries = readCvsIgnoreEntries(file.getParentFile());
        String patternToIgnore = computePatternToIgnore(file.getName());        
        if (entries.remove(patternToIgnore)) {
            writeCvsIgnoreEntries(file.getParentFile(), entries);
        }
    }
    
    private Set<String> readCvsIgnoreEntries(File directory) throws IOException {
        File cvsIgnore = new File(directory, FILENAME_CVSIGNORE);
        
        Set<String> entries = new HashSet<String>(5);
        if (!cvsIgnore.canRead()) return entries;
        
        String s;
        BufferedReader r = null;
        try {
            r = new BufferedReader(new FileReader(cvsIgnore));
            while ((s = r.readLine()) != null) {
                entries.addAll(Arrays.asList(s.trim().split(" ")));
            }
        } finally {
            if (r != null) try { r.close(); } catch (IOException e) {}
        }
        return entries;
    }
    
    private void writeCvsIgnoreEntries(File directory, Set entries) throws IOException {
        File cvsIgnore = new File(directory, FILENAME_CVSIGNORE);
        FileObject fo = FileUtil.toFileObject(cvsIgnore);

        if (entries.size() == 0) {
            if (fo != null) fo.delete();
            return;
        }
        
        if (fo == null || !fo.isValid()) {
            fo = FileUtil.toFileObject(directory);
            fo = fo.createData(FILENAME_CVSIGNORE);
        }
        FileLock lock = fo.lock();
        PrintWriter w = null;
        try {
            w = new PrintWriter(fo.getOutputStream(lock));
            for (Iterator i = entries.iterator(); i.hasNext();) {
                w.println(i.next());
            }
        } finally {
            lock.releaseLock();
            if (w != null) w.close();
        }
    }

    /** @see FilesystemHandler#ignoreEvents */
    public static void ignoreFilesystemEvents(boolean ignore) {
        FilesystemHandler.ignoreEvents(ignore);
    }

    /**
     * Creates new GlobalOptions prefilled with default options:
     * <ul>
     *   <li>compression level 3 if not enabled logging
     * </ul>
     */
    public static GlobalOptions createGlobalOptions() {
        GlobalOptions globalOptions = new GlobalOptions();
        if (System.getProperty("cvsClientLog") == null) {    // NOI18N
            int gzipLevel = 4;
            String level = System.getProperty("netbeans.experimental.cvs.io.compressionLevel"); // NOI18N
            if (level != null) {
                try {
                    int candidate = Integer.parseInt(level);
                    if (0 <= candidate && candidate < 10) {
                        gzipLevel = candidate;
                    }
                } catch (NumberFormatException ex) {
                    // default level
                }
            }
            if (gzipLevel > 0) {
                globalOptions.setCompressionLevel(gzipLevel);
            }
        }
        return globalOptions;
    }

    public VCSAnnotator getVCSAnnotator() {
        return fileStatusProvider;
    }

    public VCSInterceptor getVCSInterceptor() {
        return filesystemHandler;
    }

    private static final int STATUS_DIFFABLE = 
            FileInformation.STATUS_VERSIONED_UPTODATE | 
            FileInformation.STATUS_VERSIONED_MODIFIEDLOCALLY |
            FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY |
            FileInformation.STATUS_VERSIONED_CONFLICT |
            FileInformation.STATUS_VERSIONED_MERGE |
            FileInformation.STATUS_VERSIONED_REMOVEDINREPOSITORY |
            FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY |
            FileInformation.STATUS_VERSIONED_MODIFIEDINREPOSITORY;
    
    
    public void getOriginalFile(File workingCopy, File originalFile) {
        FileInformation info = fileStatusCache.getStatus(workingCopy);
        if ((info.getStatus() & STATUS_DIFFABLE) == 0) return;

        // TODO: it is not easy to tell whether the file is not yet versioned OR some real error occurred   
        try {
            if (CvsVersioningSystem.getInstance().getAdminHandler().getEntry(workingCopy) == null) {
                // VC would throw IAE in this case, so silently return here
                // TODO: this can happen because of implicit logic in the cache that treats not-present files as uptodate, thus failing the STATUS_DIFFABLE test above 
                return;
            }
            File original = VersionsCache.getInstance().getRemoteFile(workingCopy, VersionsCache.REVISION_BASE, null, true);
            if (original == null) throw new IOException("Unable to get BASE revision of " + workingCopy);
            org.netbeans.modules.versioning.util.Utils.copyStreamsCloseAll(new FileOutputStream(originalFile), new FileInputStream(original));
        } catch (Exception e) {
            LOG.log(Level.INFO, "Unable to get original file", e);
        }
    }

    public void refreshAllAnnotations() {
        listenerSupport.fireVersioningEvent(EVENT_REFRESH_ANNOTATIONS);
    }
}