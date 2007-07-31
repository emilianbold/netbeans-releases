/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.versioning.util;

import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.DataShadow;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.awt.Actions;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport;
import org.openide.windows.CloneableOpenSupport;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.spi.VersioningSupport;

import javax.swing.text.Document;
import javax.swing.*;
import java.io.*;
import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.awt.Rectangle;
import java.awt.Point;
import java.text.MessageFormat;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.nio.charset.Charset;
import org.netbeans.api.queries.FileEncodingQuery;
import org.openide.ErrorManager;

/**
 * Utilities class.
 * 
 * @author Maros Sandor
 */
public final class Utils {
    
    /**
     * Request processor for quick tasks.
     */
    private static final RequestProcessor vcsRequestProcessor = new RequestProcessor("Versioning", 1);

    /**
     * Request processor for long running tasks.
     */
    private static final RequestProcessor vcsBlockingRequestProcessor = new RequestProcessor("Versioning long tasks", 1);

    private static /*final*/ File [] unversionedFolders;
    
    static {
        try {
            String uf = VersioningSupport.getPreferences().get("unversionedFolders", null);
            if (uf == null || uf.length() == 0) {
                unversionedFolders = new File[0];
            } else {
                String [] paths = uf.split("\\;");
                unversionedFolders = new File[paths.length];
                int idx = 0;
                for (String path : paths) {
                    unversionedFolders[idx++] = new File(path);
                }
            }
        } catch (Exception e) {
            unversionedFolders = new File[0];
            Logger.getLogger(Utils.class.getName()).log(Level.WARNING, e.getMessage(), e);
        }
    }
    
    private Utils() {
    }

    /**
     * Creates a task that will run in the Versioning RequestProcessor (with has throughput of 1). The runnable may take long
     * to execute (connet through network, etc).
     * 
     * @param runnable Runnable to run
     * @return RequestProcessor.Task created task
     */
    public static RequestProcessor.Task createTask(Runnable runnable) {
        return vcsBlockingRequestProcessor.create(runnable);
    }

    /**
     * Runs the runnable in the Versioning RequestProcessor (with has throughput of 1). The runnable must not take long
     * to execute (connet through network, etc).
     * 
     * @param runnable Runnable to run
     */
    public static void post(Runnable runnable) {
        vcsRequestProcessor.post(runnable);
    }
    
    /**
     * Tests for ancestor/child file relationsip.
     * 
     * @param ancestor supposed ancestor of the file
     * @param file a file
     * @return true if ancestor is an ancestor folder of file OR both parameters are equal, false otherwise
     */
    public static boolean isAncestorOrEqual(File ancestor, File file) {
        if (VersioningSupport.isFlat(ancestor)) {
            return ancestor.equals(file) || ancestor.equals(file.getParentFile()) && !file.isDirectory();
        }
        for (; file != null; file = file.getParentFile()) {
            if (file.equals(ancestor)) return true;
        }
        return false;
    }

    /**
     * Tests whether all files belong to the same data object.
     * 
     * @param files array of Files
     * @return true if all files share common DataObject (even null), false otherwise
     */
    public static boolean shareCommonDataObject(File[] files) {
        if (files == null || files.length < 2) return true;
        DataObject common = findDataObject(files[0]);
        for (int i = 1; i < files.length; i++) {
            DataObject dao = findDataObject(files[i]);
            if (dao != common && (dao == null || !dao.equals(common))) return false;
        }  
        return true;
    }

    /**
     * @param file
     * @return Set<File> all files that belong to the same DataObject as the argument
     */
    public static Set<File> getAllDataObjectFiles(File file) {
        Set<File> filesToCheckout = new HashSet<File>(2);
        filesToCheckout.add(file);
        FileObject fo = FileUtil.toFileObject(file);
        if (fo != null) {
            try {
                DataObject dao = DataObject.find(fo);
                Set<FileObject> fileObjects = dao.files();
                for (FileObject fileObject : fileObjects) {
                    filesToCheckout.add(FileUtil.toFile(fileObject));
                }
            } catch (DataObjectNotFoundException e) {
                // no dataobject, never mind
            }
        }
        return filesToCheckout;
    }
    
    private static DataObject findDataObject(File file) {
        FileObject fo = FileUtil.toFileObject(file);
        if (fo != null) {
            try {
                return DataObject.find(fo);
            } catch (DataObjectNotFoundException e) {
                // ignore
            }
        }
        return null;
    }
    
    /**
     * Copies all content from the supplied reader to the supplies writer and closes both streams when finished.
     * 
     * @param writer where to write
     * @param reader what to read
     * @throws IOException if any I/O operation fails 
     */
    public static void copyStreamsCloseAll(OutputStream writer, InputStream reader) throws IOException {
        byte [] buffer = new byte[4096];
        int n;
        while ((n = reader.read(buffer)) != -1) {
            writer.write(buffer, 0, n);
        }
        writer.close();
        reader.close();
    }

    /**
     * Copies all content from the supplied reader to the supplies writer and closes both streams when finished.
     * 
     * @param writer where to write
     * @param reader what to read
     * @throws IOException if any I/O operation fails 
     */
    public static void copyStreamsCloseAll(Writer writer, Reader reader) throws IOException {
        char [] buffer = new char[4096];
        int n;
        while ((n = reader.read(buffer)) != -1) {
            writer.write(buffer, 0, n);
        }
        writer.close();
        reader.close();
    }
    
    /**
     * Helper method to get an array of Strings from preferences.
     *  
     * @param prefs storage
     * @param key key of the String array
     * @return List<String> stored List of String or an empty List if the key was not found (order is preserved)
     */
    public static List<String> getStringList(Preferences prefs, String key) {
        List<String> retval = new ArrayList<String>();
        try {
            String[] keys = prefs.keys();            
            for (int i = 0; i < keys.length; i++) {
                String k = keys[i];
                if (k != null && k.startsWith(key)) {
                    int idx = Integer.parseInt(k.substring(k.lastIndexOf('.') + 1));
                    retval.add(idx + "." + prefs.get(k, null));
                }
            }
            List<String> rv = new ArrayList<String>(retval.size());
            rv.addAll(retval);
            for (String s : retval) {
                int pos = s.indexOf('.');
                int index = Integer.parseInt(s.substring(0, pos));
                rv.set(index, s.substring(pos + 1));
            }
            return rv;
        } catch (Exception ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.INFO, null, ex);
            return new ArrayList<String>(0);
        }
    }

    /**
     * Stores a List of Strings into Preferences node under the given key.
     * 
     * @param prefs storage
     * @param key key of the String array
     * @param value List of Strings to write (order will be preserved)
     */
    public static void put(Preferences prefs, String key, List<String> value) {
        try {
            String[] keys = prefs.keys();            
            for (int i = 0; i < keys.length; i++) {
                String k = keys[i];
                if (k != null && k.startsWith(key + ".")) {
                    prefs.remove(k);
                }
            }
            int idx = 0;
            for (String s : value) {
                prefs.put(key + "." + idx++, s);
            }
        } catch (BackingStoreException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.INFO, null, ex);
        }        
    }

    /**
     * Convenience method for storing array of Strings with a maximum length with LRU policy. Supplied value is
     * stored at index 0 and all items beyond (maxLength - 1) index are discarded. <br>
     * If the value is already stored then it will be first removed from its old position.
     * 
     * @param prefs storage
     * @param key key for the array
     * @param value String to store
     * @param maxLength maximum length of the stored array. won't be considered if &lt; 0
     */
    public static void insert(Preferences prefs, String key, String value, int maxLength) {
        List<String> newValues = getStringList(prefs, key);        
        if(newValues.contains(value)) {
            newValues.remove(value);
        }    
        newValues.add(0, value);   
        if (maxLength > -1 && newValues.size() > maxLength) {
            newValues.subList(maxLength, newValues.size()).clear();
        }
        put(prefs, key, newValues);        
    }
    
    /**
     * Convenience method to remove a array of values from a in preferences stored array of Strings 
     * 
     * @param prefs storage
     * @param key key for the array
     * @param values Strings to remove     
     */
    public static void removeFromArray(Preferences prefs, String key, List<String> values) {
        List<String> newValues = getStringList(prefs, key);
        newValues.removeAll(values);                            
        put(prefs, key, newValues);
    }

    /**
     * Convenience method to remove a value from a in preferences stored array of Strings 
     * 
     * @param prefs storage
     * @param key key for the array
     * @param value String to remove     
     */
    public static void removeFromArray(Preferences prefs, String key, String value) {
        List<String> newValues = getStringList(prefs, key);
        newValues.remove(value);                            
        put(prefs, key, newValues);
    }
    
    /**
     * Splits files/folders into 2 groups: flat folders and other files
     * 
     * @param files array of files to split
     * @return File[][] the first array File[0] contains flat folders (@see #flatten for their direct descendants),
     * File[1] contains all other files
     */ 
    public static File[][] splitFlatOthers(File [] files) {
        Set<File> flat = new HashSet<File>(1);
        for (int i = 0; i < files.length; i++) {
            if (VersioningSupport.isFlat(files[i])) {
                flat.add(files[i]);
            }
        }
        if (flat.size() == 0) {
            return new File[][] { new File[0], files };
        } else {
            Set<File> allFiles = new HashSet<File>(Arrays.asList(files));
            allFiles.removeAll(flat);
            return new File[][] {
                flat.toArray(new File[flat.size()]),
                allFiles.toArray(new File[allFiles.size()])
            };
        }
    }

    /**
     * Recursively deletes the file or directory.
     *
     * @param file file/directory to delete
     */
    public static void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File [] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                deleteRecursively(files[i]);
            }
        }
        file.delete();
    }

    /**
     * Searches for common filesystem parent folder for given files.
     * 
     * @param a first file
     * @param b second file
     * @return File common parent for both input files with the longest filesystem path or null of these files
     * have not a common parent
     */ 
    public static File getCommonParent(File a, File b) {
        for (;;) {
            if (a.equals(b)) {
                return a;
            } else if (a.getAbsolutePath().length() > b.getAbsolutePath().length()) {
                a = a.getParentFile();
                if (a == null) return null;
            } else {
                b = b.getParentFile();
                if (b == null) return null;
            }
        }
    }

    public static String getStackTrace() {
        Exception e = new Exception();
        e.fillInStackTrace();
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    /**
     * Copied from org.netbeans.api.xml.parsers.DocumentInputSource to save whole module dependency.
     *  
     * @param doc a Document to read
     * @return Reader a reader that reads document's text
     */
    public static Reader getDocumentReader(final Document doc) {
        final String[] str = new String[1];
        Runnable run = new Runnable() {
            public void run () {
                try {
                    str[0] = doc.getText(0, doc.getLength());
                } catch (javax.swing.text.BadLocationException e) {
                    // impossible
                    e.printStackTrace();
                }
            }
        };
        doc.render(run);
        return new StringReader(str[0]);
    }

    /**
     * For popups invoked by keyboard determines best location for it. 
     * 
     * @param table source of popup event
     * @return Point best location for menu popup
     */
    public static Point getPositionForPopup(JTable table) {
        int idx = table.getSelectedRow();
        if (idx == -1) idx = 0;
        Rectangle rect = table.getCellRect(idx, 1, true);
        return rect.getLocation();
    }

    /**
     * Creates a menu item from an action.
     * 
     * @param action an action
     * @return JMenuItem
     */
    public static JMenuItem toMenuItem(Action action) {
        JMenuItem item;
        if (action instanceof Presenter.Menu) {
            item = ((Presenter.Menu) action).getMenuPresenter();
        } else {
            item = new JMenuItem();
            Actions.connect(item, action, false);
        }
        return item;
    }

    public static File getTempFolder() {
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));   // NOI18N
        for (;;) {
            File dir = new File(tmpDir, "vcs-" + Long.toString(System.currentTimeMillis())); // NOI18N
            if (!dir.exists() && dir.mkdirs()) {
                dir.deleteOnExit();
                return FileUtil.normalizeFile(dir);
            }
        }
    }

    /**
     * Utility method to word-wrap a String.
     * 
     * @param s String to wrap 
     * @param maxLineLength maximum length of one line. If less than 1 no wrapping will occurr
     * @return String wrapped string 
     */
    public static String wordWrap(String s, int maxLineLength) {
        int n = s.length() - 1;
        if (maxLineLength < 1 || n < maxLineLength) return s;
        StringBuilder sb = new StringBuilder();

        int currentWrap = 0;
        for (;;) {
            int nextWrap = currentWrap + maxLineLength - 1;
            if (nextWrap >= n) {
                sb.append(s.substring(currentWrap));
                break;
            }
            int idx = s.lastIndexOf(' ', nextWrap + 1);
            if (idx > currentWrap) {
                sb.append(s.substring(currentWrap, idx).trim());
                currentWrap = idx + 1;
            } else {
                sb.append(s.substring(currentWrap, nextWrap + 1));
                currentWrap = nextWrap + 1;
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    /**
     * Computes display name of an action based on its context.
     *  
     * @param clazz caller class for bundle location
     * @param baseName base bundle name
     * @param ctx action's context
     * @return String full name of the action, eg. Show "File.java" Annotations 
     */
    public static String getActionName(Class clazz, String baseName, VCSContext ctx) {
        Set<File> nodes = ctx.getRootFiles();
        int objectCount = nodes.size();
        // if all nodes represent project node the use plain name
        // It avoids "Show changes 2 files" on project node
        // caused by fact that project contains two source groups.

        Node[] activatedNodes = ctx.getElements().lookupAll(Node.class).toArray(new Node[0]);
        boolean projectsOnly = true;
        for (int i = 0; i < activatedNodes.length; i++) {
            Node activatedNode = activatedNodes[i];
            Project project =  (Project) activatedNode.getLookup().lookup(Project.class);
            if (project == null) {
                projectsOnly = false;
                break;
            }
        }
        if (projectsOnly) objectCount = activatedNodes.length; 

        if (objectCount == 0) {
            return NbBundle.getBundle(clazz).getString(baseName);
        } else if (objectCount == 1) {
            if (projectsOnly) {
                String dispName = ProjectUtils.getInformation((Project) activatedNodes[0].getLookup().lookup(Project.class)).getDisplayName();
                return NbBundle.getMessage(clazz, baseName + "_Context",  // NOI18N
                                                dispName);
            }
            String name;
            FileObject fo = (FileObject) activatedNodes[0].getLookup().lookup(FileObject.class);
            if (fo != null) {
                name = fo.getNameExt();
            } else {
                DataObject dao = (DataObject) activatedNodes[0].getLookup().lookup(DataObject.class);
                if (dao instanceof DataShadow) {
                    dao = ((DataShadow) dao).getOriginal();
                }
                if (dao != null) {
                    name = dao.getPrimaryFile().getNameExt();
                } else {
                    name = activatedNodes[0].getDisplayName();
                }
            }
            return MessageFormat.format(NbBundle.getBundle(clazz).getString(baseName + "_Context"), name); // NOI18N
        } else {
            if (projectsOnly) {
                try {
                    return MessageFormat.format(NbBundle.getBundle(clazz).getString(baseName + "_Projects"), objectCount); // NOI18N
                } catch (MissingResourceException ex) {
                    // ignore use files alternative bellow
                }
            }
            return MessageFormat.format(NbBundle.getBundle(clazz).getString(baseName + "_Context_Multiple"), objectCount); // NOI18N
        }
    }

    /**
     * Computes display name of a context.
     *  
     * @param ctx a context
     * @return String short display name of the context, eg. File.java, 3 Files, 2 Projects, etc. 
     */
    public static String getContextDisplayName(VCSContext ctx) {
        // TODO: reuse this code in getActionName() 
        Set<File> nodes = ctx.getRootFiles();
        int objectCount = nodes.size();
        // if all nodes represent project node the use plain name
        // It avoids "Show changes 2 files" on project node
        // caused by fact that project contains two source groups.

        Node[] activatedNodes = ctx.getElements().lookupAll(Node.class).toArray(new Node[0]);
        boolean projectsOnly = true;
        for (int i = 0; i < activatedNodes.length; i++) {
            Node activatedNode = activatedNodes[i];
            Project project =  (Project) activatedNode.getLookup().lookup(Project.class);
            if (project == null) {
                projectsOnly = false;
                break;
            }
        }
        if (projectsOnly) objectCount = activatedNodes.length; 

        if (objectCount == 0) {
            return null;
        } else if (objectCount == 1) {
            if (projectsOnly) {
                return ProjectUtils.getInformation((Project) activatedNodes[0].getLookup().lookup(Project.class)).getDisplayName();
            }
            FileObject fo = (FileObject) activatedNodes[0].getLookup().lookup(FileObject.class);
            if (fo != null) {
                return fo.getNameExt();
            } else {
                DataObject dao = (DataObject) activatedNodes[0].getLookup().lookup(DataObject.class);
                if (dao instanceof DataShadow) {
                    dao = ((DataShadow) dao).getOriginal();
                }
                if (dao != null) {
                    return dao.getPrimaryFile().getNameExt();
                } else {
                    return activatedNodes[0].getDisplayName();
                }
            }
        } else {
            if (projectsOnly) {
                try {
                    return MessageFormat.format(NbBundle.getBundle(Utils.class).getString("MSG_ActionContext_MultipleProjects"), objectCount);  // NOI18N
                } catch (MissingResourceException ex) {
                    // ignore use files alternative bellow
                }
            }
            return MessageFormat.format(NbBundle.getBundle(Utils.class).getString("MSG_ActionContext_MultipleFiles"), objectCount);  // NOI18N
        }
    }

    /**
     * Open a read-only view of the file in editor area.
     * 
     * @param fo a file to open
     * @param revision revision of the file
     */
    public static void openFile(FileObject fo, String revision) {
        ViewEnv env = new ViewEnv(fo);
        CloneableEditorSupport ces = new ViewCES(env, fo.getNameExt() + " @ " + revision); // NOI18N
        ces.view();
    }

    /**
     * Asks for permission to scan a given folder for versioning metadata. Misconfigured automount daemons may
     * try to look for a "CVS" server if asked for "/net/CVS/Entries" file for example causing hangs and full load.
     * Versioning systems must NOT scan a folder if this method returns true and should consider it as unversioned. 
     * 
     * @param folder a folder to query
     * @link http://www.netbeans.org/issues/show_bug.cgi?id=105161
     * @return true if scanning for versioning system metadata is forbidden in the given folder, false otherwise
     */
    public static boolean isScanForbidden(File folder) {
        for (File unversionedFolder : unversionedFolders) {
            if (isAncestorOrEqual(unversionedFolder, folder)) {
                return true;
            }
        }
        return false;
    }
    
    private static Map<File, Charset> fileToCharset;
    
    /**
     * Retrieves the Charset for the referenceFile and associates it weakly with
     * the given file. A following getAssociatedEncoding() call for 
     * the file will then return the referenceFile-s Charset.      
     * 
     * @param referenceFile the file which charset has to be used when encoding file
     * @param file file to be encoded with the referenceFile-s charset 
     * 
     */
    public static void associateEncoding(File referenceFile, File file) {
        FileObject fo = FileUtil.toFileObject(referenceFile);
        if(fo == null || fo.isFolder()) {
            return;
        }
        Charset c = FileEncodingQuery.getEncoding(fo);        
        if(c == null) {
            return;
        }
        if(fileToCharset == null) {
            fileToCharset = new WeakHashMap<File, Charset>();
        }        
        synchronized(fileToCharset) {
            fileToCharset.put(file, c);
        }
    }   
    
    /**
     * Returns a charset for the given file if it was previously registered via associateEncoding()
     * 
     * @param fo file for which the encoding has to be retrieved
     * @return the charset the given file has to be encoded with
     */ 
    public static Charset getAssociatedEncoding(FileObject fo) {
        try {
            if(fileToCharset == null || fileToCharset.isEmpty() || fo == null || fo.isFolder()) {
                return null;
            }       
            File file = FileUtil.toFile(fo);            
            if(file == null) {
                return null;
            }
            synchronized(fileToCharset) {
                return fileToCharset.get(file);
            }
        } catch (Throwable t) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, t);
            return  null;
        }        
    }
    
    public static Reader createReader(File file) throws FileNotFoundException {
        FileObject fo = FileUtil.toFileObject(file);
        if (fo == null) {
            return new FileReader(file);
        } else {
            return createReader(fo);
        }
    }

    public static Reader createReader(FileObject file) throws FileNotFoundException {
        return new InputStreamReader(file.getInputStream(), FileEncodingQuery.getEncoding(file));
    }
    
    private static class ViewEnv implements CloneableEditorSupport.Env {
        
        private final FileObject    file;

        public ViewEnv(FileObject file) {
            this.file = file;
        }

        public InputStream inputStream() throws IOException {
            return file.getInputStream();
        }

        public OutputStream outputStream() throws IOException {
            throw new IOException();
        }

        public Date getTime() {
            return file.lastModified();
        }

        public String getMimeType() {
            return file.getMIMEType();
        }

        public void addPropertyChangeListener(PropertyChangeListener l) {
        }

        public void removePropertyChangeListener(PropertyChangeListener l) {
        }

        public void addVetoableChangeListener(VetoableChangeListener l) {
        }

        public void removeVetoableChangeListener(VetoableChangeListener l) {
        }

        public boolean isValid() {
            return file.isValid();
        }

        public boolean isModified() {
            return false;
        }

        public void markModified() throws IOException {
            throw new IOException();
        }

        public void unmarkModified() {
        }

        public CloneableOpenSupport findCloneableOpenSupport() {
            return null;
        }
    }
    
    private static class ViewCES extends CloneableEditorSupport {
        
        private final String name;

        public ViewCES(Env env, String name) {
            super(env);
            this.name = name;
        }

        protected String messageSave() {
            return name;
        }

        protected String messageName() {
            return name;
        }

        protected String messageToolTip() {
            return name;
        }

        protected String messageOpening() {
            return name;
        }

        protected String messageOpened() {
            return name;
        }
    }    
}
