/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss;

import org.netbeans.modules.versioning.util.ListenersSupport;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.admin.AdminHandler;
import org.netbeans.lib.cvsclient.event.CVSListener;
import org.netbeans.lib.cvsclient.command.*;
import org.netbeans.lib.cvsclient.command.importcmd.ImportCommand;
import org.netbeans.lib.cvsclient.command.checkout.CheckoutCommand;
import org.netbeans.lib.cvsclient.command.add.AddCommand;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.modules.versioning.spi.VersioningListener;
import org.netbeans.modules.versioning.system.cvss.util.Utils;
import org.netbeans.modules.versioning.system.cvss.settings.MetadataAttic;
import org.netbeans.modules.versioning.system.cvss.settings.CvsModuleConfig;
import org.netbeans.modules.masterfs.providers.InterceptionListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.core.modules.ModuleManager;
import org.netbeans.core.modules.Module;
import org.netbeans.core.NbTopManager;
import org.openide.ErrorManager;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.filesystems.*;

import java.io.*;
import java.util.*;
import java.util.zip.InflaterInputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.regex.Pattern;

/**
 * A singleton CVS manager class, center of CVS module. Use {@link #getInstance()} to get access
 * to CVS module functionality.
 * 
 * @author Maros Sandor
 */
public class CvsVersioningSystem {

    private static CvsVersioningSystem instance;
    
    public static final String FILENAME_CVSIGNORE = ".cvsignore";

    public static final Object EVENT_PARAM_CHANGED = new Object();
    public static final Object PARAM_BATCH_REFRESH_RUNNING = new Object();

    private static final String FILENAME_CVS = "CVS";
    
    private final Map clientsCache = new HashMap();
    private final Map params = new HashMap();

    private GlobalOptions defaultGlobalOptions;
    private FileStatusCache fileStatusCache;
    private RefreshManager  refreshManager;

    private CvsLiteAdminHandler sah;
    private FilesystemHandler filesystemHandler;

    private Annotator annotator;

    private final Set   userIgnorePatterns = new HashSet();
    private boolean     userIgnorePatternsReset;
    private long        userIgnorePatternsTimestamp;
    
    public static synchronized CvsVersioningSystem getInstance() {
        if (instance == null) {
            instance = new CvsVersioningSystem();
            instance.init();
        }
        return instance;
    }

    private void init() {
        disableCurrentModules();
        defaultGlobalOptions = new GlobalOptions();
        sah = new CvsLiteAdminHandler();
        loadCache();
        filesystemHandler  = new FilesystemHandler(this);
        refreshManager = new RefreshManager(this);
        annotator = new Annotator(this);
        Project[] projects = OpenProjects.getDefault().getOpenProjects();
        updateManagedRoots(projects);
        MetadataAttic.cleanUp();
    }

    /**
     * Disables old VCS modules. 
     */ 
    private void disableCurrentModules() {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                final ModuleManager mgr = NbTopManager.get().getModuleSystem().getManager();
                mgr.mutex().writeAccess(new Runnable() {
                    public void run() {
                        Module m;
                        Set modules = new HashSet();
                        m = mgr.get("org.netbeans.modules.vcs.profiles.cvsprofiles");
                        if (m != null && m.isEnabled()) modules.add(m);
                        m = mgr.get("org.netbeans.modules.vcs.advanced");
                        if (m != null && m.isEnabled()) modules.add(m);
                        m = mgr.get("org.netbeans.modules.vcs.profiles.vss");
                        if (m != null && m.isEnabled()) modules.add(m);
                        m = mgr.get("org.netbeans.modules.vcs.profiles.pvcs");
                        if (m != null && m.isEnabled()) modules.add(m);
                        if (modules.size() > 0) {
                            mgr.disable(modules);
                        }
                    }
                });
            }
        });
    }
    
    /**
     * Simple cache deserialization.
     */ 
    private void loadCache() {
        HashMap kes = null;
        ObjectInputStream ois = null;
        FileObject cache = null;
        try {
            cache = Repository.getDefault().getDefaultFileSystem().getRoot().
                    getFileObject("Versioning/org-netbeans-modules-versioning-simplecache.bin");
            if (cache != null) {
                ois = new ObjectInputStream(new InflaterInputStream(cache.getInputStream()));
                kes = (HashMap) ois.readObject();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (cache != null) try { cache.delete(); } catch (IOException ex) {}
        } finally {
            if (ois != null) try { ois.close(); } catch (IOException e) {}
        }
        fileStatusCache = new FileStatusCache(this, kes);
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                fileStatusCache.refreshLocallyModified();
            }
        });
    }

    /**
     * Simple cache serialization.
     * 
     * @return true
     */ 
    boolean closing() {
        ObjectOutputStream oos = null;
        try {
            FileObject cache = Repository.getDefault().getDefaultFileSystem().getRoot().
                    getFileObject("Versioning/org-netbeans-modules-versioning-simplecache.bin");
            if (cache == null) {
                FileObject fo = Repository.getDefault().getDefaultFileSystem().getRoot();
                cache = fo.getFileObject("Versioning");
                if (cache == null) cache = fo.createFolder("Versioning");
                fo = cache.getFileObject("org-netbeans-modules-versioning-simplecache.bin");
                if (fo == null) fo = cache.createData("org-netbeans-modules-versioning-simplecache.bin");
                cache = fo;
            }
            FileLock lock = cache.lock();
            oos = new ObjectOutputStream(new DeflaterOutputStream(cache.getOutputStream(lock)));
            fileStatusCache.writeCache(oos);
            lock.releaseLock();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (oos != null) try { oos.close(); } catch (IOException e) {}
        }
        return true;
    }
    
    private CvsVersioningSystem() {
    }

    public CvsFileTableModel getFileTableModel(File [] roots, int displayStatuses) {
        return CvsFileTableModel.getModel(roots, displayStatuses);
    }
    
    /**
     * Determines correct CVS client from the given cvs root.
     * 
     * @param cvsRoot root never <code>null</code>
     * @return
     */ 
    private ClientRuntime getClientRuntime(String cvsRoot) {
 
            cvsRoot.length();  // rise NPE

            ClientRuntime clientRuntime;
            synchronized(clientsCache) {
                clientRuntime = (ClientRuntime) clientsCache.get(cvsRoot);
                if (clientRuntime == null) {
                    clientRuntime = new ClientRuntime(CVSRoot.parse(cvsRoot));
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
            throw new NotVersionedException("Cannot determine CVSRoot for command: " + cmd);
        }

        File oneFile = files[0];
        try {
            String cvsRoot = Utils.getCVSRootFor(oneFile);
            return cvsRoot;
        } catch (IOException e) {
            throw new NotVersionedException("Cannot determine CVSRoot for: " + oneFile);
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
    public RequestProcessor.Task post(Command cmd, CVSListener mgr) throws CommandException,
            AuthenticationException, NotVersionedException, IllegalCommandException,
            IOException {
        return post(cmd, defaultGlobalOptions, mgr);
    }

    /**
     *
     * @param cmd
     * @param options Global options to use, may be set to null to use default options
     * @param mgr
     * @throws CommandException
     * @throws AuthenticationException
     * @throws NotVersionedException
     */ 
    public RequestProcessor.Task post(Command cmd, GlobalOptions options, CVSListener mgr) throws CommandException,
            AuthenticationException, NotVersionedException, IllegalCommandException,
            IOException {
        ClientRuntime clientRuntime = getClientRuntime(cmd, options);
        RequestProcessor.Task task = clientRuntime.createTask(cmd, options != null ? options : defaultGlobalOptions, mgr);
        task.schedule(0);
        return task;
    }

    /**
     * Gets client runtime (a repository session).
     * XXX it can split into multiple 
     */
    public ClientRuntime getClientRuntime(Command cmd, GlobalOptions options) {
        String root;
        if ( cmd instanceof CheckoutCommand || cmd instanceof ImportCommand) {
            root = options.getCVSRoot();
        } else {
            try {
                root = detectCvsRoot(cmd);
            } catch (NotVersionedException e) {
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
    
    public RefreshManager getRefreshManager() {
        return refreshManager;
    }

    /**
     * Checks if the file is be ignored by CVS module. This method assumes that the file is managed so
     * if you do not know this beforehand, you have to call isManaged() first.
     *
     * @param file file to be tested
     * @return true, if the file is ignored by CVS, false otherwise.
     */
    public boolean isIgnored(File file) {
        if (SharabilityQuery.getSharability(file) == SharabilityQuery.NOT_SHARABLE) return true;

        String name = file.getName();
        if (file.isDirectory()) {
            if (name.equals(FILENAME_CVS)) return true;
        }
        Set patterns = new HashSet(Arrays.asList(CvsModuleConfig.getDefault().getIgnoredFilePatterns()));
        addUserPatterns(patterns);

        for (Iterator i = patterns.iterator(); i.hasNext();) {
            Pattern pattern = (Pattern) i.next();
            if (pattern.matcher(name).matches()) return true;
        }

        return isInCvsIgnore(file);
    }
    
    private void addUserPatterns(Set patterns) {
        File userIgnores = new File(System.getProperty("user.home"), FILENAME_CVSIGNORE);
        if (userIgnores.lastModified() > userIgnorePatternsTimestamp || userIgnores.lastModified() == 0 && userIgnorePatternsTimestamp > 0) {
            userIgnorePatternsTimestamp = userIgnores.lastModified();
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
                if ("!".equals(s)) {
                    userIgnorePatternsReset = true;
                    userIgnorePatterns.clear();
                } else {
                    // TODO: implement SH->REGEX convertor
                    s = s.replaceAll("\\.", "\\\\.");
                    s = s.replaceAll("\\*", ".*");
                    userIgnorePatterns.add(Pattern.compile(s));
                }
            }
        } catch (IOException e) {
            // user has invalid ignore list, ignore it
        } finally {
            if (r != null) try { r.close(); } catch (IOException e) {};
        }
    }

    /**
     * Tests whether the file/directory is managed by this module.
     * 
     * @param file a file or directory
     * @return true if the file is under this module management, false otherwise
     */ 
    public boolean isManaged(File file) {
        String path = file.getAbsolutePath();
        // TODO: more elegant testing ?
        Set managedRoots = CvsModuleConfig.getDefault().getManagedRoots();
        if (Utilities.isWindows()) {
            for (Iterator i = managedRoots.iterator(); i.hasNext();) {
                CvsModuleConfig.ManagedRoot root = (CvsModuleConfig.ManagedRoot) i.next();
                String setupPath = root.getPath();
                if (path.length() >= setupPath.length() && setupPath.equalsIgnoreCase(path.substring(0, setupPath.length()))) {
                    return true;
                }
            }
        } else {
            for (Iterator i = managedRoots.iterator(); i.hasNext();) {
                CvsModuleConfig.ManagedRoot root = (CvsModuleConfig.ManagedRoot) i.next();
                if (path.startsWith(root.getPath())) {
                    return true;
                }
            }
        }
/*
        // automatic adding of versioned roots, disabled now
        if (file.isFile()) file = file.getParentFile();
        if (file == null) return false;
        File entries = new File(file, FILENAME_CVS + "/Entries");
        if (entries.canRead()) {
            for (;;) {
                if (file.getParentFile() == null) break;
                entries = new File(file.getParentFile(), FILENAME_CVS + "/Entries");
                if (!entries.canRead()) {
                    addManagedRoot(file);
                    break;
                }
                file = file.getParentFile();
            }
            return true;
        }
*/
        return false;
    }

/*
        // automatic adding of versioned roots, disabled now
    private void addManagedRoot(File file) {
        Set newRoots = new HashSet(CvsModuleConfig.getDefault().getManagedRoots());
        for (Iterator i = newRoots.iterator(); i.hasNext();) {
            CvsModuleConfig.ManagedRoot root = (CvsModuleConfig.ManagedRoot) i.next();
            File currentRoot = new File(root.getPath());
            if (Utils.isParentOrEqual(currentRoot, file)) return;
            if (Utils.isParentOrEqual(file, currentRoot)) {
                i.remove();
            }
        }
        newRoots.add(new CvsModuleConfig.ManagedRoot(FileUtil.normalizeFile(file).getAbsolutePath()));
        CvsModuleConfig.getDefault().setManagedRoots(newRoots);
    }
*/

    public boolean isRoot(File file) {
        String path = file.getAbsolutePath();
        for (Iterator i = CvsModuleConfig.getDefault().getManagedRoots().iterator(); i.hasNext();) {
            CvsModuleConfig.ManagedRoot root = (CvsModuleConfig.ManagedRoot) i.next();
            if (path.equals(root.getPath())) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isInCvsIgnore(File file) {
        try {
            return readCvsIgnoreEntries(file.getParentFile()).contains(file.getName());
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
        return isText(file) ? KeywordSubstitutionOptions.DEFAULT : KeywordSubstitutionOptions.BINARY;    
    }

    public boolean isText(File file) {
        // TODO: Let user configure defaults
        if (FILENAME_CVSIGNORE.equals(file.getName())) {
            return true;            
        }
        FileObject fo = FileUtil.toFileObject(file);
        if (fo == null) return false;
        if (fo.getMIMEType().startsWith("text")) {
            return true;            
        }
        // TODO: HACKS begin
        if ("mf".equalsIgnoreCase(fo.getExt())) {
            return true;            
        }
        if ("form".equalsIgnoreCase(fo.getExt())) {
            return true;
        }        
        // TODO: HACKS end
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
                if (FILENAME_CVSIGNORE.equals(files[i].getName())) continue;
                addToCvsIgnore(files[i]);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
    }

    public void setNotignored(File[] files) {
        for (int i = 0; i < files.length; i++) {
            try {
                removeFromCvsIgnore(files[i]);
            } catch (IOException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
    }
        
    private void addToCvsIgnore(File file) throws IOException {
        
        Set entries = readCvsIgnoreEntries(file.getParentFile());
        if (entries.add(file.getName())) {
            writeCvsIgnoreEntries(file.getParentFile(), entries);
            fileStatusCache.refreshCached(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
        }
    }
    
    private void removeFromCvsIgnore(File file) throws IOException {
        Set entries = readCvsIgnoreEntries(file.getParentFile());
        if (entries.remove(file.getName())) {
            writeCvsIgnoreEntries(file.getParentFile(), entries);
            fileStatusCache.refreshCached(file, FileStatusCache.REPOSITORY_STATUS_UNKNOWN);
        }
    }
    
    private Set readCvsIgnoreEntries(File directory) throws IOException {
        File cvsIgnore = new File(directory, FILENAME_CVSIGNORE);
        
        Set entries = new HashSet(5);
        if (!cvsIgnore.canRead()) return entries;
        
        String s;
        BufferedReader r = null;
        try {
            r = new BufferedReader(new FileReader(cvsIgnore));
            while ((s = r.readLine()) != null) {
                entries.add(s);
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

    /**
     * Marks all passed projects as versioning roots if CVS subfolder found.
     * Updates internal structures used by {@link #isManaged}.
     * Adds all files marked as NOT_SHARABLE by {@link org.netbeans.api.queries.SharabilityQuery} into .cvsignore lists
     * to be consistent with other tools.
     */
    public void updateManagedRoots(Project[] projects) {
        Set roots = new HashSet();
        for (int i = 0; i < projects.length; i++) {
            Project project = projects[i];
            Sources projectSources = ProjectUtils.getSources(project);
            SourceGroup groups[] = projectSources.getSourceGroups(Sources.TYPE_GENERIC);
            for (int j = 0; j < groups.length; j++) {
                SourceGroup group = groups[j];
                FileObject root = group.getRootFolder();
                File rootFile = FileUtil.toFile(root);
                File testCVS = new File(rootFile, FILENAME_CVS);  // NOI18N
                if (testCVS.isDirectory()) {
                    roots.add(new CvsModuleConfig.ManagedRoot(rootFile.getAbsolutePath()));
                }
            }
        }
        CvsModuleConfig.getDefault().setManagedRoots(roots);
    }
    
    /**
     * Hook to obtain CVS system interception listener.
     * 
     * @return InterceptionListener returns file system handler instance
     */ 
    InterceptionListener getFileSystemHandler() {
        return filesystemHandler;
    }
}