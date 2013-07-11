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

package org.openide.filesystems;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SyncFailedException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.WeakHashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;
import org.netbeans.modules.openide.filesystems.declmime.MIMEResolverImpl;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.implspi.NamedServicesProvider;

/** Common utilities for handling files.
 * This is a dummy class; all methods are static.
 */
public final class FileUtil extends Object {

    private static final RequestProcessor REFRESH_RP = new RequestProcessor("FileUtil-Refresh-All");//NOI18N
    private static RequestProcessor.Task refreshTask = null;

    private static final Logger LOG = Logger.getLogger(FileUtil.class.getName());

    /** Normal header for ZIP files. */
    private static byte[] ZIP_HEADER_1 = {0x50, 0x4b, 0x03, 0x04};
    /** Also seems to be used at least in apisupport/project/test/unit/data/example-external-projects/suite3/nbplatform/random/modules/ext/stuff.jar; not known why */
    private static byte[] ZIP_HEADER_2 = {0x50, 0x4b, 0x05, 0x06};
    
    /** transient attributes which should not be copied
    * of type Set<String>
    */
    static final Set<String> transientAttributes = new HashSet<String>();

    static {
        transientAttributes.add("templateWizardURL"); // NOI18N
        transientAttributes.add("templateWizardIterator"); // NOI18N
        transientAttributes.add("templateWizardDescResource"); // NOI18N
        transientAttributes.add("templateCategory"); // NOI18N
        transientAttributes.add("instantiatingIterator"); // NOI18N
        transientAttributes.add("instantiatingWizardURL"); // NOI18N
        transientAttributes.add("SystemFileSystem.localizingBundle"); // NOI18N
        transientAttributes.add("SystemFileSystem.icon"); // NOI18N
        transientAttributes.add("SystemFileSystem.icon32"); // NOI18N
        transientAttributes.add("displayName"); // NOI18N
        transientAttributes.add("iconBase"); // NOI18N
        transientAttributes.add("position"); // NOI18N
        transientAttributes.add(MultiFileObject.WEIGHT_ATTRIBUTE); // NOI18N
    }

    /** Cache for {@link #isArchiveFile(FileObject)}. */
    private static final Map<FileObject, Boolean> archiveFileCache = new WeakHashMap<FileObject,Boolean>();
    private static FileSystem diskFileSystem;

    static String toDebugString(File file) {
        if (file == null) {
            return "NULL-ref"; // NOI18N
        } else {
            return file.getPath() + "(" + file.getClass() + ")"; // NOI18N
        }
    }
    
    static boolean assertNormalized(File path) {
        if (path != null) {
            File np;
            assert path.getClass().getName().startsWith("sun.awt.shell") ||
                path.equals(np = FileUtil.normalizeFileCached(path)) : 
                "Need to normalize " + toDebugString(path) + " was " + toDebugString(np);  //NOI18N
        }
        return true;
    }

    private static FileSystem getDiskFileSystemFor(File... files) {
        FileSystem fs = getDiskFileSystem();
        if (fs == null) {
            for (File file : files) {
                FileObject fo = toFileObject(file);
                fs = getDiskFileSystem();
                if (fs != null) {
                    break;
                }
            }
        }
        return fs;
    }

    private FileUtil() {
    }
    
    /**
     * Refreshes all necessary filesystems. Not all instances of <code>FileObject</code> are refreshed
     * but just those that represent passed <code>files</code> and their children recursively.
     * @param files
     * @since 7.6
     */
    public static void refreshFor(File... files) {
        FileSystem fs = getDiskFileSystemFor(files);
        if (fs != null) {
            try {
                fs.getRoot().setAttribute("request_for_refreshing_files_be_aware_this_is_not_public_api", files);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } 
    }         

    /**
     * Refreshes all <code>FileObject</code> that represent files <code>File.listRoots()</code> 
     * and their children recursively.
     * @since 7.7
     */
    public static void refreshAll() {
        // run just one refreshTask in time and always wait for finish
        final RequestProcessor.Task taskToWaitFor;  // prevent possible NPE with only refreshTask.waitFinished
        synchronized (REFRESH_RP) {
            if (refreshTask != null) {
                refreshTask.cancel();
            } else {
                refreshTask = REFRESH_RP.create(new Runnable() {

                    public void run() {
                        LOG.fine("refreshAll - started");  //NOI18N
                        refreshFor(File.listRoots());
                        try {
                            getConfigRoot().getFileSystem().refresh(true);
                        } catch (FileStateInvalidException ex) {
                            Exceptions.printStackTrace(ex);
                        } finally {
                            LOG.fine("refreshAll - finished");  //NOI18N
                            synchronized (REFRESH_RP) {
                                refreshTask = null;
                            }
                        }
                    }
                });
            }
            taskToWaitFor = refreshTask;
            refreshTask.schedule(0);
            LOG.fine("refreshAll - scheduled");  //NOI18N
        }
        taskToWaitFor.waitFinished();
        LOG.fine("refreshAll - finished");  //NOI18N
    }

    /**
     * Registers <code>listener</code> so that it will receive
     * <code>FileEvent</code>s from <code>FileSystem</code>s providing instances
     * of <code>FileObject</code> convertible to <code>java.io.File</code>. 
     * @param fcl
     * @see #toFileObject
     * @since 7.7
     */
    public static void addFileChangeListener(FileChangeListener fcl) {
        FileSystem fs = getDiskFileSystem();
        if (fs == null) {fs = getDiskFileSystemFor(File.listRoots());}
        if (fs != null) {
            fs.addFileChangeListener(fcl);
        }
    }
    
    /**
     * Unregisters <code>listener</code> so that it will no longer receive
     * <code>FileEvent</code>s from <code>FileSystem</code>s providing instances
     * of <code>FileObject</code> convertible to <code>java.io.File</code>      
     * @param fcl
     * @see #toFileObject
     * @since 7.7
     */
    public static void removeFileChangeListener(FileChangeListener fcl) {
        FileSystem fs = getDiskFileSystem();
        if (fs == null) {fs = getDiskFileSystemFor(File.listRoots());}
        if (fs != null) {
            fs.removeFileChangeListener(fcl);
        }
    }

    /**
     * Adds a listener to changes in a given path. It permits you to listen to a file
     * which does not yet exist, or continue listening to it after it is deleted and recreated, etc.
     * <br/>
     * When given path represents a file ({@code path.isDirectory() == false})
     * <ul>
     * <li>fileDataCreated event is fired when the file is created</li>
     * <li>fileDeleted event is fired when the file is deleted</li>
     * <li>fileChanged event is fired when the file is modified</li>
     * <li>fileRenamed event is fired when the file is renamed</li>
     * <li>fileAttributeChanged is fired when FileObject's attribute is changed</li>
     * </ul>
     * When given path represents a folder ({@code path.isDirectory() == true})
     * <ul>
     * <li>fileFolderCreated event is fired when the folder is created or a child folder created</li>
     * <li>fileDataCreated event is fired when a child file is created</li>
     * <li>fileDeleted event is fired when the folder is deleted or a child file/folder removed</li>
     * <li>fileChanged event is fired when a child file is modified</li>
     * <li>fileRenamed event is fired when the folder is renamed or a child file/folder is renamed</li>
     * <li>fileAttributeChanged is fired when FileObject's attribute is changed</li>
     *</ul>
     * Can only add a given [listener, path] pair once. However a listener can 
     * listen to any number of paths. Note that listeners are always held weakly
     * - if the listener is collected, it is quietly removed.
     *
     * @param listener FileChangeListener to listen to changes in path
     * @param path File path to listen to (even not existing)
     *
     * @see FileObject#addFileChangeListener
     * @since org.openide.filesystems 7.20
     */
    public static void addFileChangeListener(FileChangeListener listener, File path) {
        FileChangeImpl.addFileChangeListenerImpl(LOG, listener, path);
    }

    /**
     * Removes a listener to changes in a given path.
     * @param listener FileChangeListener to be removed
     * @param path File path in which listener was listening
     * @throws IllegalArgumentException if listener was not listening to given path
     *
     * @see FileObject#removeFileChangeListener
     * @since org.openide.filesystems 7.20
     */
    public static void removeFileChangeListener(FileChangeListener listener, File path) {
        FileChangeImpl.removeFileChangeListenerImpl(LOG, listener, path);
    }
    /**
     * Works like {@link #addRecursiveListener(org.openide.filesystems.FileChangeListener, java.io.File, java.io.FileFilter, java.util.concurrent.Callable) 
     * addRecursiveListener(listener, path, null, null)}.
     *
     * @param listener FileChangeListener to listen to changes in path
     * @param path File path to listen to (even not existing)
     *
     * @since org.openide.filesystems 7.28
     */
    public static void addRecursiveListener(FileChangeListener listener, File path) {
        addRecursiveListener(listener, path, null, null);
    }

    /** Works like {@link #addRecursiveListener(org.openide.filesystems.FileChangeListener, java.io.File, java.io.FileFilter, java.util.concurrent.Callable) 
     * addRecursiveListener(listener, path, null, stop)}.
     *
     * @param listener FileChangeListener to listen to changes in path
     * @param path File path to listen to (even not existing)
     * @param stop an interface to interrupt the process of registering
     *    the listener. If the <code>call</code> returns true, the process
     *    of registering the listener is immediately interrupted
     *
     * @see FileObject#addRecursiveListener
     * @since org.openide.filesystems 7.37
     */
    public static void addRecursiveListener(FileChangeListener listener, File path, Callable<Boolean> stop) {
        addRecursiveListener(listener, path, null, stop);
    }

    /** 
     * Adds a listener to changes under given path. It permits you to listen to a file
     * which does not yet exist, or continue listening to it after it is deleted and recreated, etc.
     * <br/>
     * When given path represents a file ({@code path.isDirectory() == false}), this
     * code behaves exactly like {@link #addFileChangeListener(org.openide.filesystems.FileChangeListener, java.io.File)}.
     * Usually the path shall represent a folder ({@code path.isDirectory() == true})
     * <ul>
     * <li>fileFolderCreated event is fired when the folder is created or a child folder created</li>
     * <li>fileDataCreated event is fired when a child file is created</li>
     * <li>fileDeleted event is fired when the folder is deleted or a child file/folder removed</li>
     * <li>fileChanged event is fired when a child file is modified</li>
     * <li>fileRenamed event is fired when the folder is renamed or a child file/folder is renamed</li>
     * <li>fileAttributeChanged is fired when FileObject's attribute is changed</li>
     *</ul>
     * The above events are delivered for changes in all subdirectories (recursively).
     * It is guaranteed that with each change at least one event is generated.
     * For example adding a folder does not notify about content of the folder,
     * hence one event is delivered.
     *
     * Can only add a given [listener, path] pair once. However a listener can
     * listen to any number of paths. Note that listeners are always held weakly
     * - if the listener is collected, it is quietly removed.
     *
     * <div class="nonnormative">
     * As registering of the listener can take a long time, especially on deep
     * hierarchies, it is possible provide a callback <code>stop</code>.
     * This stop object is guaranteed to be called once per every folder on the
     * default (when masterfs module is included) implemention. If the call
     * to <code>stop.call()</code> returns true, then the registration of
     * next recursive items is interrupted. The listener may or may not get
     * some events from already registered folders.
     * </div>
     * 
     * Those who provide {@link FileFilter recurseInto} callback can prevent
     * the system to enter, and register operating system level listeners 
     * to certain subtrees under the provided <code>path</code>. This does
     * not prevent delivery of changes, if they are made via the filesystem API.
     * External changes however will not be detected.
     * 
     * @param listener FileChangeListener to listen to changes in path
     * @param path File path to listen to (even not existing)
     * @param stop an interface to interrupt the process of registering
     *    the listener. If the <code>call</code> returns true, the process
     *    of registering the listener is immediately interrupted. <code>null</code>
     *    value disables this kind of callback.
     * @param recurseInto a file filter that may return <code>false</code> when
     *   a folder should not be traversed into and external changes in it ignored.
     *   <code>null</code> recurses into all subfolders
     * @since 7.61
     */
    public static void addRecursiveListener(FileChangeListener listener, File path, FileFilter recurseInto, Callable<Boolean> stop) {
        FileChangeImpl.addRecursiveListener(listener, path, recurseInto, stop);
    }

    /**
     * Removes a listener to changes under given path.
     * @param listener FileChangeListener to be removed
     * @param path File path in which listener was listening
     * @throws IllegalArgumentException if listener was not listening to given path
     *
     * @see FileObject#removeRecursiveListener
     * @since org.openide.filesystems 7.28
     */
    public static void removeRecursiveListener(FileChangeListener listener, File path) {
        FileChangeImpl.removeRecursiveListener(listener, path);
    }

    /**
     * Executes atomic action. For more info see {@link FileSystem#runAtomicAction}. 
     * <p>
     * All events about filesystem changes (related to events on all affected instances of <code>FileSystem</code>)
     * are postponed after the whole <code>atomicCode</code> 
     * is executed.
     * </p>
     * @param atomicCode code that is supposed to be run as atomic action. See {@link FileSystem#runAtomicAction}
     * @throws java.io.IOException
     * @since 7.5
     */
    @SuppressWarnings("deprecation")
    public static final void runAtomicAction(final AtomicAction atomicCode) throws IOException {
        Repository.getDefault().getDefaultFileSystem().runAtomicAction(atomicCode);
    }

    /**
     * Executes atomic action. For more info see {@link FileSystem#runAtomicAction}. 
     * <p>
     * All events about filesystem changes (related to events on all affected instances of <code>FileSystem</code>)
     * are postponed after the whole <code>atomicCode</code> 
     * is executed.
     * </p>
     * @param atomicCode code that is supposed to be run as atomic action. See {@link FileSystem#runAtomicAction}
     * @since 7.5
     */
    public static final void runAtomicAction(final Runnable atomicCode) {
        final AtomicAction action = new FileSystem.AtomicAction() {
            public void run() throws IOException {
                atomicCode.run();
            }
        };
        try {
            FileUtil.runAtomicAction(action);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }        
    /**
     * Returns FileObject for a folder.
     * If such a folder does not exist then it is created, including any necessary but nonexistent parent 
     * folders. Note that if this operation fails it may have succeeded in creating some of the necessary
     * parent folders.
     * @param folder folder to be created
     * @return FileObject for a folder
     * @throws java.io.IOException if the creation fails
     * @since 7.0
     */
    public static FileObject createFolder (final File folder) throws IOException {
        File existingFolder = folder;
        while(existingFolder != null && !existingFolder.isDirectory()) {
            existingFolder = existingFolder.getParentFile();
        }
        if (existingFolder == null) {
            throw new IOException(folder.getAbsolutePath());
        }        
                      
        FileObject retval = null;
        FileObject folderFo = FileUtil.toFileObject(existingFolder);        
        assert folderFo != null : existingFolder.getAbsolutePath();
        final String relativePath = getRelativePath(existingFolder, folder);
        try {
            retval = FileUtil.createFolder(folderFo,relativePath);        
        } catch (IOException ex) {
            //thus retval = null;
        }
        //if refresh needed because of external changes        
        if (retval == null || !retval.isValid()) {
            folderFo.getFileSystem().refresh(false);
            retval = FileUtil.createFolder(folderFo,relativePath);
        }        
        assert retval != null;        
        return retval;        
    } 
    
    /**Returns FileObject for a data file.
     * If such a data file does not exist then it is created, including any necessary but nonexistent parent
     * folders. Note that if this operation fails it may have succeeded in creating some of the necessary
     * parent folders.
     * @param data data file to be created
     * @return FileObject for a data file
     * @throws java.io.IOException if the creation fails
     * @since 7.0
     */
    public static FileObject createData (final File data) throws IOException {        
        File folder = data;
        while(folder != null && !folder.isDirectory()) {
            folder = folder.getParentFile();
        }
        if (folder == null) {
            throw new IOException(data.getAbsolutePath());
        }        
                      
        FileObject retval = null;
        FileObject folderFo = FileUtil.toFileObject(folder);
        assert folderFo != null : folder.getAbsolutePath();
        final String relativePath = getRelativePath(folder, data);
        try {
            retval = FileUtil.createData(folderFo,relativePath);        
        } catch (IOException ex) {
            //thus retval = null;
        }
        //if refresh needed because of external changes        
        if (retval == null || !retval.isValid()) {
            folderFo.getFileSystem().refresh(false);
            retval = FileUtil.createData(folderFo,relativePath);
        }        
        assert retval != null;        
        return retval;
    } 
        
    private static String getRelativePath(final File dir, final File file) {
        Stack<String> stack = new Stack<String>();
        File tempFile = file;
        while(tempFile != null && !tempFile.equals(dir)) {
            stack.push (tempFile.getName());
            tempFile = tempFile.getParentFile();
        }
        assert tempFile != null : file.getAbsolutePath() + "not found in " + dir.getAbsolutePath();//NOI18N
        StringBuilder retval = new StringBuilder();
        while (!stack.isEmpty()) {
            retval.append(stack.pop());
            if (!stack.isEmpty()) {
                retval.append('/');//NOI18N
            }
        }                        
        return retval.toString();
    }
    
    /** Copies stream of files.
    * <P>
    * Please be aware, that this method doesn't close any of passed streams.
    * @param is input stream
    * @param os output stream
    */
    public static void copy(InputStream is, OutputStream os)
    throws IOException {
        final byte[] BUFFER = new byte[65536];
        int len;

        for (;;) {
            len = is.read(BUFFER);

            if (len == -1) {
                return;
            }

            os.write(BUFFER, 0, len);
        }
    }

    /** Copies file to the selected folder.
     * This implementation simply copies the file by stream content.
    * @param source source file object
    * @param destFolder destination folder
    * @param newName file name (without extension) of destination file
    * @param newExt extension of destination file
    * @return the created file object in the destination folder
    * @exception IOException if <code>destFolder</code> is not a folder or does not exist; the destination file already exists; or
    *      another critical error occurs during copying
    */
    static FileObject copyFileImpl(FileObject source, FileObject destFolder, String newName, String newExt)
    throws IOException {
        FileObject dest = destFolder.createData(newName, newExt);

        FileLock lock = null;
        InputStream bufIn = null;
        OutputStream bufOut = null;

        try {
            lock = dest.lock();
            bufIn = source.getInputStream();

            if (dest instanceof AbstractFileObject) {
                /** prevents from firing fileChange*/
                bufOut = ((AbstractFileObject) dest).getOutputStream(lock, false);
            } else {
                bufOut = dest.getOutputStream(lock);
            }

            copy(bufIn, bufOut);
            copyAttributes(source, dest);
        } finally {
            if (bufIn != null) {
                bufIn.close();
            }

            if (bufOut != null) {
                bufOut.close();
            }

            if (lock != null) {
                lock.releaseLock();
            }
        }

        return dest;
    }

    //
    // public methods
    //

    /** Factory method that creates an empty implementation of a filesystem that
     * completely resides in a memory.
     * <p>To specify the MIME type of a data file without using a MIME resolver,
     * set the {@code mimeType} file attribute.
     * <p>Since 7.42, a {@link URLMapper} is available for files (and folders)
     * in memory filesystems. These URLs are valid only so long as the filesystem
     * has not been garbage-collected, so hold the filesystem (or a file in it)
     * strongly for as long as you expect the URLs to be in play.
     * @return a blank writable filesystem
     * @since 4.43
     */
    public static FileSystem createMemoryFileSystem() {
        return new MemoryFileSystem();
    }

    /** Copies file to the selected folder.
    * This implementation simply copies the file by stream content.
    * @param source source file object
    * @param destFolder destination folder
    * @param newName file name (without extension) of destination file
    * @param newExt extension of destination file
    * @return the created file object in the destination folder
    * @exception IOException if <code>destFolder</code> is not a folder or does not exist; the destination file already exists; or
    *      another critical error occurs during copying
    */
    public static FileObject copyFile(FileObject source, FileObject destFolder, String newName, String newExt)
    throws IOException {
        return source.copy(destFolder, newName, newExt);
    }

    /** Copies file to the selected folder.
    * This implementation simply copies the file by stream content.
    * Uses the extension of the source file.
    * @param source source file object
    * @param destFolder destination folder
    * @param newName file name (without extension) of destination file
    * @return the created file object in the destination folder
    * @exception IOException if <code>destFolder</code> is not a folder or does not exist; the destination file already exists; or
    *      another critical error occurs during copying
    */
    public static FileObject copyFile(FileObject source, FileObject destFolder, String newName)
    throws IOException {
        return copyFile(source, destFolder, newName, source.getExt());
    }

    /** Moves file to the selected folder.
     * This implementation uses a copy-and-delete mechanism, and automatically uses the necessary lock.
    * @param source source file object
    * @param destFolder destination folder
    * @param newName file name (without extension) of destination file
    * @return new file object
    * @exception IOException if either the {@link #copyFile copy} or {@link FileObject#delete delete} failed
    */
    public static FileObject moveFile(FileObject source, FileObject destFolder, String newName)
    throws IOException {
        FileLock lock = null;

        try {
            lock = source.lock();

            return source.move(lock, destFolder, newName, source.getExt());
        } finally {
            if (lock != null) {
                lock.releaseLock();
            }
        }
    }

    /** Returns a folder on given filesystem if such a folder exists. 
     * If not then a folder is created, including any necessary but nonexistent parent 
     * folders. Note that if this operation fails it may have succeeded in creating some of the necessary
     * parent folders. 
     * The name of the new folder can be
     * specified as a multi-component pathname whose components are separated
     * by File.separatorChar or &quot;/&quot; (forward slash).
     *
     * @param folder where the new folder will be placed in
     * @param name name of the new folder
     * @return the new folder
     * @exception IOException if the creation fails
     */
    public static FileObject createFolder(FileObject folder, String name)
    throws IOException {
        String separators;

        if (File.separatorChar != '/') {
            separators = "/" + File.separatorChar; // NOI18N
        } else {
            separators = "/"; // NOI18N
        }

        StringTokenizer st = new StringTokenizer(name, separators);

        while (st.hasMoreElements()) {
            name = st.nextToken();

            if (name.length() > 0) {
                FileObject f = folder.getFileObject(name);

                if (f == null) {
                    try {
                        LOG.finest("createFolder - before create folder if not exists.");
                        f = folder.createFolder(name);
                    } catch (IOException ex) {  // SyncFailedException or IOException when folder already exists
                        // there might be unconsistency between the cache
                        // and the disk, that is why
                        folder.refresh();

                        // and try again
                        f = folder.getFileObject(name);

                        if (f == null) {
                            // if still not found than we have to report the
                            // exception
                            throw ex;
                        }
                    }
                }

                folder = f;
            }
        }

        return folder;
    }

    /** Returns a data file on given filesystem if such a data file exists. 
    * If not then a data file is created, including any necessary but nonexistent parent 
    * folders. Note that if this operation fails it may have succeeded in creating some of the necessary
    * parent folders. The name of
    * data file can be composed as resource name (e. g. org/netbeans/myfolder/mydata ).
    *
    * @param folder to begin with creation at
    * @param name name of data file as a resource
    * @return the data file for given name
    * @exception IOException if the creation fails
    */
    public static FileObject createData(FileObject folder, String name) throws IOException {
        Parameters.notNull("folder", folder);  //NOI18N
        Parameters.notNull("name", name);  //NOI18N

        String foldername;
        String dataname;
        String fname;
        String ext;
        int index = name.lastIndexOf('/');
        FileObject data;

        // names with '/' on the end are not valid
        if (index >= name.length()) {
            throw new IOException("Wrong file name."); // NOI18N
        }

        // if name contains '/', create necessary folder first
        if (index != -1) {
            foldername = name.substring(0, index);
            dataname = name.substring(index + 1);
            folder = createFolder(folder, foldername);
            assert folder != null;
        } else {
            dataname = name;
        }

        // create data
        index = dataname.lastIndexOf('.');

        if (index != -1) {
            fname = dataname.substring(0, index);
            ext = dataname.substring(index + 1);
        } else {
            fname = dataname;
            ext = ""; // NOI18N
        }

        data = folder.getFileObject(fname, ext);

        if (data == null) {
            try {
                data = folder.createData(fname, ext);
                assert data != null : "FileObject.createData cannot return null; called on " + folder + " + " + fname +
                " + " + ext; // #50802
            } catch (SyncFailedException ex) {
                // there might be unconsistency between the cache
                // and the disk, that is why
                folder.refresh();

                // and try again
                data = folder.getFileObject(fname, ext);

                if (data == null) {
                    // if still not found than we have to report the
                    // exception
                    throw ex;
                }
            }
        }

        return data;
    }

    /** Finds appropriate java.io.File to FileObject if possible.
     * If not possible then null is returned.
     * This is the inverse operation of {@link #toFileObject}.
     * @param fo FileObject whose corresponding File will be looked for
     * @return java.io.File or null if no corresponding File exists.
     * @since 1.29
     */
    public static File toFile(FileObject fo) {
        File retVal = (File) fo.getAttribute("java.io.File"); // NOI18N;        

        if (retVal == null) {
            URL fileURL = URLMapper.findURL(fo, URLMapper.INTERNAL);
            if (fileURL == null || !"file".equals(fileURL.getProtocol())) {  //NOI18N
                fileURL = URLMapper.findURL(fo, URLMapper.EXTERNAL);
            }

            if ((fileURL != null) && "file".equals(fileURL.getProtocol())) {
                retVal = Utilities.toFile(URI.create(fileURL.toExternalForm()));
            }
        }
        assert assertNormalized(retVal);
        return retVal;
    }

    /**
     * Converts a disk file to a matching file object.
     * This is the inverse operation of {@link #toFile}.
     * <p class="nonnormative">
     * If you are running with {@code org.netbeans.modules.masterfs} enabled,
     * this method should never return null for a file which exists on disk.
     * For example, to make this method work in unit tests in an Ant-based module project,
     * right-click Unit Test Libraries, Add Unit Test Dependency, check Show Non-API Modules, select Master Filesystem.
     * (Also right-click the new Master Filesystem node, Edit, uncheck Include in Compile Classpath.)
     * To ensure masterfs (or some other module that can handle the conversion)
     * is present put following line into your module manifest:
     * </p>
     * <pre>
     * OpenIDE-Module-Needs: org.openide.filesystems.FileUtil.toFileObject
     * </pre>
     * 
     * @param file a disk file (may or may not exist). This file
     * must be {@linkplain #normalizeFile normalized}.
     * @return a corresponding file object, or null if the file does not exist
     *         or there is no {@link URLMapper} available to convert it
     * @since 4.29
     */
    public static FileObject toFileObject(File file) {
        Parameters.notNull("file", file);  //NOI18N
        // return null for UNC root
        if(file.getPath().equals("\\\\")) {
            return null;
        }
        boolean asserts = false;
        assert asserts = true;
        if (asserts) {
            File normFile = normalizeFile(file);
            if (!file.equals(normFile)) {
                final String msg = "Parameter file was not " + // NOI18N   
                    "normalized. Was " + toDebugString(file) + " instead of " + toDebugString(normFile); // NOI18N
                LOG.log(Level.WARNING, msg);
                LOG.log(Level.INFO, msg, new IllegalArgumentException(msg));
            }
            file = normFile;
        }

        FileObject retVal = null;
        try {
            URL url = Utilities.toURI(file).toURL();
            retVal = URLMapper.findFileObject(url);

            /*probably temporary piece of code to catch the cause of #46630*/
        } catch (MalformedURLException e) {
            retVal = null;
        }

        if (retVal != null) {
            if (getDiskFileSystem() == null) {
                try {
                    FileSystem fs = retVal.getFileSystem();
                    setDiskFileSystem(fs);
                } catch (FileStateInvalidException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return retVal;
    }
        
    /** Finds appropriate FileObjects to java.io.File if possible.
     * If not possible then empty array is returned. More FileObjects may
     * correspond to one java.io.File that`s why array is returned.
     * @param file File whose corresponding FileObjects will be looked for.
     * The file has to be "normalized" otherwise IllegalArgumentException is thrown.
     * See {@link #normalizeFile} for how to do that.
     * @return corresponding FileObjects or empty array  if no
     * corresponding FileObject exists.
     * @since 1.29
     * @deprecated Use {@link #toFileObject} instead.
     */
    @Deprecated
    public static FileObject[] fromFile(File file) {
        FileObject[] retVal;

        if (!file.equals(normalizeFile(file))) {
            throw new IllegalArgumentException(
                "Parameter file was not " + // NOI18N
                "normalized. Was " + toDebugString(file) + " instead of " + toDebugString(normalizeFile(file)));  // NOI18N
        }

        try {
            URL url = (Utilities.toURI(file).toURL());
            retVal = URLMapper.findFileObjects(url);
        } catch (MalformedURLException e) {
            retVal = null;
        }

        return retVal;
    }

    /** Copies attributes from one file to another.
    * Note: several special attributes will not be copied, as they should
    * semantically be transient. These include attributes used by the
    * template wizard (but not the template attribute itself).
    * @param source source file object
    * @param dest destination file object
    * @exception IOException if the copying failed
    */
    public static void copyAttributes(FileObject source, FileObject dest)
    throws IOException {
        Enumeration<String> attrKeys = source.getAttributes();

        while (attrKeys.hasMoreElements()) {
            String key = attrKeys.nextElement();

            if (transientAttributes.contains(key)) {
                continue;
            }

            if (isTransient(source, key)) {
                continue;
            }

            AtomicBoolean isRawValue = new AtomicBoolean();
            Object value = XMLMapAttr.getRawAttribute(source, key, isRawValue);

            // #132801 and #16761 - don't set attributes where value is 
            // instance of VoidValue because these attributes were previously written 
            // by mistake in code. So it should happen only if you import some
            // settings from old version.
            if (value != null && !(value instanceof MultiFileObject.VoidValue)) {
                if (isRawValue.get() && value instanceof Method) {
                    dest.setAttribute("methodvalue:" + key, value); // NOI18N
                } else if (isRawValue.get() && value instanceof Class) {
                    dest.setAttribute("newvalue:" + key, value); // NOI18N
                } else {
                    dest.setAttribute(key, value);
                }
            }
        }
    }

    static boolean isTransient(FileObject fo, String attrName) {
        return XMLMapAttr.ModifiedAttribute.isTransient(fo, attrName);
    }

    /** Extract jar file into folder represented by file object. If the JAR contains
    * files with name filesystem.attributes, it is assumed that these files
    * has been created by DefaultAttributes implementation and the content
    * of these files is treated as attributes and added to extracted files.
    * <p><code>META-INF/</code> directories are skipped over.
    *
    * @param fo file object of destination folder
    * @param is input stream of jar file
    * @exception IOException if the extraction fails
    * @deprecated Use of XML filesystem layers generally obsoletes this method.
    *             For tests, use {@link org.openide.util.test.TestFileUtils#unpackZipFile}.
    */
    @Deprecated
    public static void extractJar(final FileObject fo, final InputStream is)
    throws IOException {
        FileSystem fs = fo.getFileSystem();

        fs.runAtomicAction(
            new FileSystem.AtomicAction() {
                public void run() throws IOException {
                    extractJarImpl(fo, is);
                }
            }
        );
    }

    /** Does the actual extraction of the Jar file.
     */
    private static void extractJarImpl(FileObject fo, InputStream is)
    throws IOException {
        JarInputStream jis;
        JarEntry je;

        // files with extended attributes (name, DefaultAttributes.Table)
        HashMap<String, DefaultAttributes.Table> attributes =
                new HashMap<String, DefaultAttributes.Table>(7);

        jis = new JarInputStream(is);

        while ((je = jis.getNextJarEntry()) != null) {
            String name = je.getName();

            if (name.toLowerCase().startsWith("meta-inf/")) {
                continue; // NOI18N
            }

            if (je.isDirectory()) {
                createFolder(fo, name);

                continue;
            }

            if (DefaultAttributes.acceptName(name)) {
                // file with extended attributes
                DefaultAttributes.Table table = DefaultAttributes.loadTable(jis, name);
                attributes.put(name, table);
            } else {
                // copy the file
                FileObject fd = createData(fo, name);
                FileLock lock = fd.lock();

                try {
                    OutputStream os = fd.getOutputStream(lock);

                    try {
                        copy(jis, os);
                    } finally {
                        os.close();
                    }
                } finally {
                    lock.releaseLock();
                }
            }
        }

        //
        // apply all extended attributes
        //
        Iterator it = attributes.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();

            String fileName = (String) entry.getKey();
            int last = fileName.lastIndexOf('/');
            String dirName;

            if (last != -1) {
                dirName = fileName.substring(0, last + 1);
            } else {
                dirName = ""; // NOI18N
            }

            String prefix = fo.isRoot() ? dirName : (fo.getPath() + '/' + dirName);

            DefaultAttributes.Table t = (DefaultAttributes.Table) entry.getValue();
            Iterator files = t.keySet().iterator();

            while (files.hasNext()) {
                String orig = (String) files.next();
                String fn = prefix + orig;
                FileObject obj = fo.getFileSystem().findResource(fn);

                if (obj == null) {
                    continue;
                }

                Enumeration<String> attrEnum = t.attrs(orig);

                while (attrEnum.hasMoreElements()) {
                    // iterate thru all arguments
                    String attrName = attrEnum.nextElement();

                    // Note: even transient attributes set here!
                    Object value = t.getAttr(orig, attrName);

                    if (value != null) {
                        obj.setAttribute(attrName, value);
                    }
                }
            }
        }
    }
     // extractJar

    /** Gets the extension of a specified file name. The extension is
    * everything after the last dot.
    *
    * @param fileName name of the file
    * @return extension of the file (or <code>""</code> if it had none)
    */
    public static String getExtension(String fileName) {
        int index = fileName.lastIndexOf("."); // NOI18N

        if (index == -1) {
            return ""; // NOI18N
        } else {
            return fileName.substring(index + 1);
        }
    }

    /** Finds an unused file name similar to that requested in the same folder.
     * The specified file name is used if that does not yet exist or is
     * {@link FileObject#isVirtual isVirtual}.
     * Otherwise, the first available name of the form <code>basename_nnn.ext</code> (counting from one) is used.
     *
     * <p><em>Caution:</em> this method does not lock the parent folder
     * to prevent race conditions: i.e. it is possible (though unlikely)
     * that the resulting name will have been created by another thread
     * just as you were about to create the file yourself (if you are,
     * in fact, intending to create it just after this call). Since you
     * cannot currently lock a folder against child creation actions,
     * the safe approach is to use a loop in which a free name is
     * retrieved; an attempt is made to {@link FileObject#createData create}
     * that file; and upon an <code>IOException</code> during
     * creation, retry the loop up to a few times before giving up.
     *
    * @param folder parent folder
    * @param name preferred base name of file
    * @param ext extension to use (or null)
    * @return a free file name <strong>(without the extension)</strong>
     */
    public static String findFreeFileName(FileObject folder, String name, String ext) {
        if (checkFreeName(folder, name, ext)) {
            return name;
        }

        for (int i = 1;; i++) {
            String destName = name + "_" + i; // NOI18N

            if (checkFreeName(folder, destName, ext)) {
                return destName;
            }
        }
    }

    /** Finds an unused folder name similar to that requested in the same parent folder.
     * <p>See caveat for <code>findFreeFileName</code>.
     * @see #findFreeFileName findFreeFileName
    * @param folder parent folder
    * @param name preferred folder name
    * @return a free folder name
    */
    public static String findFreeFolderName(FileObject folder, String name) {
        if (checkFreeName(folder, name, null)) {
            return name;
        }

        for (int i = 1;; i++) {
            String destName = name + "_" + i; // NOI18N

            if (checkFreeName(folder, destName, null)) {
                return destName;
            }
        }
    }

    /**
     * Gets a relative resource path between folder and fo.
     * @param folder root of filesystem or any other folder in folders hierarchy
     * @param fo arbitrary FileObject in folder's tree (including folder itself)
     * @return relative path between folder and fo. The returned path never
     * starts with a '/'. It never ends with a '/'. Specifically, if
     * folder==fo, returns "". Returns <code>null</code> if fo is not in
     * folder's tree.
     * @see #isParentOf
     * @since 4.16
     */
    public static String getRelativePath(FileObject folder, FileObject fo) {
        if (!isParentOf(folder, fo) && (folder != fo)) {
            return null;
        }

        String result = fo.getPath().substring(folder.getPath().length());

        if (result.startsWith("/") && !result.startsWith("//")) {
            result = result.substring(1);
        }

        return result;
    }

    /** Test if given name is free in given folder.
     * @param fo folder to check in
     * @param name name of the file or folder to check
     * @param ext extension of the file (null for folders)
     * @return true, if such name does not exists
     */
    private static boolean checkFreeName(FileObject fo, String name, String ext) {
        if ((Utilities.isWindows() || (Utilities.getOperatingSystem() == Utilities.OS_OS2)) || Utilities.isMac()) {
            // case-insensitive, do some special check
            Enumeration<? extends FileObject> en = fo.getChildren(false);

            while (en.hasMoreElements()) {
                fo = en.nextElement();

                String n = fo.getName();
                String e = fo.getExt();

                // different names => check others
                if (!n.equalsIgnoreCase(name)) {
                    continue;
                }

                // same name + without extension => no
                if (((ext == null) || (ext.trim().length() == 0)) && ((e == null) || (e.trim().length() == 0))) {
                    return fo.isVirtual();
                }

                // one of there is witout extension => check next
                if ((ext == null) || (e == null)) {
                    continue;
                }

                if (ext.equalsIgnoreCase(e)) {
                    // same name + same extension => no
                    return fo.isVirtual();
                }
            }

            // no of the files has similar name and extension
            return true;
        } else {
            if (ext == null) {
                fo = fo.getFileObject(name);

                if (fo == null) {
                    return true;
                }

                return fo.isVirtual();
            } else {
                fo = fo.getFileObject(name, ext);

                if (fo == null) {
                    return true;
                }

                return fo.isVirtual();
            }
        }
    }

    // note: "sister" is preferred in English, please don't ask me why --jglick // NOI18N

    /** Finds brother file with same base name but different extension.
    * @param fo the file to find the brother for or <CODE>null</CODE>
    * @param ext extension for the brother file
    * @return a brother file (with the requested extension and the same parent folder as the original) or
    *   <CODE>null</CODE> if the brother file does not exist or the original file was <CODE>null</CODE>
    */
    public static FileObject findBrother(FileObject fo, String ext) {
        if (fo == null) {
            return null;
        }

        FileObject parent = fo.getParent();

        if (parent == null) {
            return null;
        }

        return parent.getFileObject(fo.getName(), ext);
    }

    /** Obtain MIME type for a well-known extension.
    * If there is a case-sensitive match, that is used, else will fall back
    * to a case-insensitive match.
    * @param ext the extension: <code>"jar"</code>, <code>"zip"</code>, etc.
    * @return the MIME type for the extension, or <code>null</code> if the extension is unrecognized
    * @deprecated use {@link #getMIMEType(FileObject)} or {@link #getMIMEType(FileObject, String[])}
    * as MIME cannot be generally detected by file object extension.
    */
    @Deprecated
    public static String getMIMEType(String ext) {
        assert false : "FileUtil.getMIMEType(String extension) is deprecated. Please, use FileUtil.getMIMEType(FileObject).";  //NOI18N
        if (ext.toLowerCase().equals("xml")) {  //NOI18N
            return "text/xml"; // NOI18N
        }
        return null;
    }

    /** Resolves MIME type. Registered resolvers are invoked and used to achieve this goal.
    * Resolvers must subclass MIMEResolver.
    * @param fo whose MIME type should be recognized
    * @return the MIME type for the FileObject, or {@code null} if the FileObject is unrecognized.
     * It may return {@code content/unknown} instead of {@code null}.
    */
    public static String getMIMEType(FileObject fo) {
        return MIMESupport.findMIMEType(fo);
    }

    /** Resolves MIME type. Registered resolvers are invoked and used to achieve this goal.
     * Resolvers must subclass MIMEResolver.
     * @param fo whose MIME type should be recognized
     * @param withinMIMETypes an array of MIME types. Only resolvers whose
     * {@link MIMEResolver#getMIMETypes} contain one or more of the requested
     * MIME types will be asked if they recognize the file. It is possible for
     * the resulting MIME type to not be a member of this list.
     * @return the MIME type for the FileObject, or <code>null</code> if 
     * the FileObject is unrecognized. It may return {@code content/unknown} instead of {@code null}.
     * It is possible for the resulting MIME type to not be a member of given list.
     * @since 7.13
     */
    public static String getMIMEType(FileObject fo, String... withinMIMETypes) {
        Parameters.notNull("withinMIMETypes", withinMIMETypes);  //NOI18N
        return MIMESupport.findMIMEType(fo, withinMIMETypes);
    }
    
    /** Registers specified extension to be recognized as specified MIME type.
     * If MIME type parameter is null, it cancels previous registration.
     * Note that you may register a case-sensitive extension if that is
     * relevant (for example {@literal *.C} for C++) but if you register
     * a lowercase extension it will by default apply to uppercase extensions
     * too on Windows.
     * @param extension the file extension to be registered
     * @param mimeType the MIME type to be registered for the extension or {@code null} to deregister
     * @see #getMIMEType(FileObject)
     * @see #getMIMETypeExtensions(String)
     */
    public static void setMIMEType(String extension, String mimeType) {
        Parameters.notEmpty("extension", extension);  //NOI18N
        final Map<String, Set<String>> mimeToExtensions = new HashMap<String, Set<String>>();
        FileObject userDefinedResolverFO = MIMEResolverImpl.getUserDefinedResolver();
        if (userDefinedResolverFO != null) {
            // add all previous content
            mimeToExtensions.putAll(MIMEResolverImpl.getMIMEToExtensions(userDefinedResolverFO));
            // exclude extension possibly registered for other MIME types
            for (Set<String> extensions : mimeToExtensions.values()) {
                extensions.remove(extension);
            }
        }
        if (mimeType != null) {
            // add specified extension to our structure
            Set<String> previousExtensions = mimeToExtensions.get(mimeType);
            if (previousExtensions != null) {
                previousExtensions.add(extension);
            } else {
                mimeToExtensions.put(mimeType, Collections.singleton(extension));
            }
        }
        if (MIMEResolverImpl.storeUserDefinedResolver(mimeToExtensions)) {
            MIMESupport.resetCache();
        }
        MIMESupport.freeCaches();
    }

    /** Returns list of file extensions associated with specified MIME type. In
     * other words files with those extensions are recognized as specified MIME type
     * in NetBeans' filesystem. It never returns {@code null}.
     * @param mimeType the MIME type (e.g. image/gif)
     * @return list of file extensions associated with specified MIME type, never {@code null}
     * @see #setMIMEType(String, String)
     * @since org.openide.filesystems 7.18
     */
    public static List<String> getMIMETypeExtensions(String mimeType) {
        Parameters.notEmpty("mimeType", mimeType);  //NOI18N
        HashMap<String, String> extensionToMime = new HashMap<String, String>();
        for (FileObject mimeResolverFO : MIMEResolverImpl.getOrderedResolvers()) {
            Map<String, Set<String>> mimeToExtensions = MIMEResolverImpl.getMIMEToExtensions(mimeResolverFO);
            for (Map.Entry<String, Set<String>> entry : mimeToExtensions.entrySet()) {
                String mimeKey = entry.getKey();
                Set<String> extensions = entry.getValue();
                for (String extension : extensions) {
                    extensionToMime.put(extension, mimeKey);
                }
            }
        }
        List<String> registeredExtensions = new ArrayList<String>();
        for (Map.Entry<String, String> entry : extensionToMime.entrySet()) {
            if (entry.getValue().equals(mimeType)) {
                registeredExtensions.add(entry.getKey());
            }
        }
        return registeredExtensions;
    }

    /**
     * @deprecated No longer used.
     */
    @Deprecated
    public static URLStreamHandler nbfsURLStreamHandler() {
        return new FileURL.Handler();
    }

    /** Recursively checks whether the file is underneath the folder. It checks whether
     * the file and folder are located on the same filesystem, in such case it checks the
     * parent <code>FileObject</code> of the file recursively until the folder is found
     * or the root of the filesystem is reached.
     * <p><strong>Warning:</strong> this method will return false in the case that
     * <code>folder == fo</code>.
     * @param folder the root of folders hierarchy to search in
     * @param fo the file to search for
     * @return <code>true</code>, if <code>fo</code> lies somewhere underneath the <code>folder</code>,
     * <code>false</code> otherwise
     * @since 3.16
     */
    public static boolean isParentOf(FileObject folder, FileObject fo) {
        Parameters.notNull("folder", folder);  //NOI18N
        Parameters.notNull("fo", fo);  //NOI18N

        if (folder.isData()) {
            return false;
        }

        try {
            if (folder.getFileSystem() != fo.getFileSystem()) {
                return false;
            }
        } catch (FileStateInvalidException e) {
            return false;
        }

        FileObject parent = fo.getParent();

        while (parent != null) {
            if (parent.equals(folder)) {
                return true;
            }

            parent = parent.getParent();
        }

        return false;
    }

    /** Creates a weak implementation of FileChangeListener.
     *
     * @param l the listener to delegate to
     * @param source the source that the listener should detach from when
     *     listener <CODE>l</CODE> is freed, can be <CODE>null</CODE>
     * @return a FileChangeListener delegating to <CODE>l</CODE>.
     * @since 4.10
     */
    public static FileChangeListener weakFileChangeListener(FileChangeListener l, Object source) {
        return WeakListeners.create(FileChangeListener.class, l, source);
    }

    /** Creates a weak implementation of FileStatusListener.
     *
     * @param l the listener to delegate to
     * @param source the source that the listener should detach from when
     *     listener <CODE>l</CODE> is freed, can be <CODE>null</CODE>
     * @return a FileChangeListener delegating to <CODE>l</CODE>.
     * @since 4.10
     */
    public static FileStatusListener weakFileStatusListener(FileStatusListener l, Object source) {
        return WeakListeners.create(FileStatusListener.class, l, source);
    }

    /**
     * Get an appropriate display name for a file object.
     * If the file corresponds to a path on disk, this will be the disk path.
     * Otherwise the name will mention the filesystem name or archive name in case
     * the file comes from archive and relative path. Relative path will be mentioned
     * just in case that passed <code>FileObject</code> isn't root {@link FileObject#isRoot}.
     *
     * @param fo a file object
     * @return a display name indicating where the file is
     * @since 4.39
     */
    public static String getFileDisplayName(FileObject fo) {
        String displayName = null;
        File f = FileUtil.toFile(fo);

        if (f != null) {
            displayName = f.getAbsolutePath();
        } else {
            FileObject archiveFile = FileUtil.getArchiveFile(fo);

            if (archiveFile != null) {
                displayName = getArchiveDisplayName(fo, archiveFile);
            }
        }

        if (displayName == null) {
            try {
                if (fo.isRoot()) {
                    displayName = fo.getFileSystem().getDisplayName();
                } else {
                    displayName = NbBundle.getMessage(
                            FileUtil.class, "LBL_file_in_filesystem", fo.getPath(), fo.getFileSystem().getDisplayName()
                        );
                }
            } catch (FileStateInvalidException e) {
                // Not relevant now, just use the simple path.
                displayName = fo.getPath();
            }
        }

        return displayName;
    }

    private static String getArchiveDisplayName(FileObject fo, FileObject archiveFile) {
        String displayName = null;

        File f = FileUtil.toFile(archiveFile);

        if (f != null) {
            String archivDisplayName = f.getAbsolutePath();

            if (fo.isRoot()) {
                displayName = archivDisplayName;
            } else {
                String entryPath = fo.getPath();
                displayName = NbBundle.getMessage(
                        FileUtil.class, "LBL_file_in_filesystem", entryPath, archivDisplayName
                    );
            }
        }

        return displayName;
    }

    /**
     * See {@link #normalizeFile} for details
     * @param path file path to normalize
     * @return normalized file path
     * @since 7.42
     */
    public static String normalizePath(final String path) {
        Map<String, String> normalizedPaths = getNormalizedFilesMap();
        String normalized = normalizedPaths.get(path);
        if (normalized == null) {
            File ret = normalizeFileImpl(new File(path));
            normalized = ret.getPath();
            normalizedPaths.put(path, normalized);
        }
        return normalized;
    }
    
    /**
     * Normalize a file path to a clean form.
     * This method may for example make sure that the returned file uses
     * the natural case on Windows; that old Windows 8.3 filenames are changed to the long form;
     * that relative paths are changed to be
     * absolute; that <code>.</code> and <code>..</code> sequences are removed; etc.
     * Unlike {@link File#getCanonicalFile} this method will not traverse symbolic links on Unix.
     * <p>This method involves some overhead and should not be called frivolously.
     * Generally it should be called on <em>incoming</em> pathnames that are gotten from user input
     * (including filechoosers), configuration files, Ant properties, etc. <em>Internal</em>
     * calculations should not need to renormalize paths since {@link File#listFiles},
     * {@link File#getParentFile}, etc. will not produce abnormal variants.
     * @param file file to normalize
     * @return normalized file
     * @since 4.48
     */
    public static File normalizeFile(final File file) {
        File ret = normalizeFileCached(file);
        assert assertNormalized(ret);
        return ret;
    }
    
    private static File normalizeFileCached(final File file) {
        Map<String, String> normalizedPaths = getNormalizedFilesMap();
        String unnormalized = file.getPath();
        String normalized = normalizedPaths.get(unnormalized);
        if (
            normalized != null && 
                normalized.equalsIgnoreCase(unnormalized) && 
                !normalized.equals(unnormalized)
        ) {
            normalized = null;
        }
        File ret;
        if (normalized == null) {
            ret = normalizeFileImpl(file);
            assert !ret.getName().equals(".") : "Original file " + file + " normalized: " + ret;
            normalizedPaths.put(unnormalized, ret.getPath());
        } else if (normalized.equals(unnormalized)) {
            ret = file;
        } else {
            ret = new File(normalized);
        }
        return ret;
    }

    private static File normalizeFileImpl(File file) {
        // XXX should use NIO in JDK 7; see #6358641
        Parameters.notNull("file", file);  //NOI18N
        File retFile;
        LOG.log(Level.FINE, "FileUtil.normalizeFile for {0}", file); // NOI18N

        long now = System.currentTimeMillis();
        if ((Utilities.isWindows() || (Utilities.getOperatingSystem() == Utilities.OS_OS2))) {
            retFile = normalizeFileOnWindows(file);
        } else if (Utilities.isMac()) {
            retFile = normalizeFileOnMac(file);
        } else {
            retFile = normalizeFileOnUnixAlike(file);
        }
        File ret = (file.getPath().equals(retFile.getPath())) ? file : retFile;
        long took = System.currentTimeMillis() - now;
        if (took > 500) {
            LOG.log(Level.WARNING, "FileUtil.normalizeFile({0}) took {1} ms. Result is {2}", new Object[]{file, took, ret});
        }
        return ret;
    }

    private static File normalizeFileOnUnixAlike(File file) {
        // On Unix, do not want to traverse symlinks.
        // URI.normalize removes ../ and ./ sequences nicely.
        file = Utilities.toFile(Utilities.toURI(file).normalize()).getAbsoluteFile();
        while (file.getAbsolutePath().startsWith("/../")) { // NOI18N
            file = new File(file.getAbsolutePath().substring(3));
        }
        if (file.getAbsolutePath().equals("/..")) { // NOI18N
            // Special treatment.
            file = new File("/"); // NOI18N
        }
        return file;
    }

    private static File normalizeFileOnMac(final File file) {
        File retVal = file;

        try {
            // URI.normalize removes ../ and ./ sequences nicely.            
            File absoluteFile = Utilities.toFile(Utilities.toURI(file).normalize());
            File canonicalFile = file.getCanonicalFile();
            String absolutePath = absoluteFile.getAbsolutePath();
            if (absolutePath.equals("/..")) { // NOI18N
                // Special treatment.
                absoluteFile = new File(absolutePath = "/"); // NOI18N
            }
            boolean isSymLink = !canonicalFile.getAbsolutePath().equalsIgnoreCase(absolutePath);

            if (isSymLink) {
                retVal = normalizeSymLinkOnMac(absoluteFile);
            } else {
                retVal = canonicalFile;
            }
        } catch (IOException ioe) {
            LOG.log(Level.FINE, "Normalization failed on file " + file, ioe);

            // OK, so at least try to absolutize the path
            retVal = file.getAbsoluteFile();
        }

        return retVal;
    }

    /**
     * @param file is expected to be already absolute with removed ../ and ./
     */
    private static File normalizeSymLinkOnMac(final File file)
    throws IOException {
        File retVal = File.listRoots()[0];
        File pureCanonicalFile = retVal;

        final String pattern = File.separator + ".." + File.separator; //NOI18N                
        final String fileName;

        { // strips insufficient non-<tt>".."</tt> segments preceding them

            String tmpFileName = file.getAbsolutePath();
            int index = tmpFileName.lastIndexOf(pattern);

            if (index > -1) {
                tmpFileName = tmpFileName.substring(index + pattern.length()); //Remove starting {/../}*
            }

            fileName = tmpFileName;
        }

        /*normalized step after step*/
        StringTokenizer fileSegments = new StringTokenizer(fileName, File.separator);

        while (fileSegments.hasMoreTokens()) {
            File absolutelyEndingFile = new File(pureCanonicalFile, fileSegments.nextToken());
            pureCanonicalFile = absolutelyEndingFile.getCanonicalFile();

            boolean isSymLink = !pureCanonicalFile.getAbsolutePath().equalsIgnoreCase(
                    absolutelyEndingFile.getAbsolutePath()
                );

            if (isSymLink) {
                retVal = new File(retVal, absolutelyEndingFile.getName());
            } else {
                retVal = new File(retVal, pureCanonicalFile.getName());
            }
        }

        return retVal;
    }

    private static File normalizeFileOnWindows(final File file) {
        File retVal = null;
        
        if (file.getClass().getName().startsWith("sun.awt.shell")) { // NOI18N
            return file;
        }

        try {
            retVal = file.getCanonicalFile();
            if (retVal.getName().equals(".")) { // NOI18Ny
                // try one more time
                retVal = retVal.getCanonicalFile();
            }
        } catch (IOException e) {
            String path = file.getPath();
            // report only other than UNC path \\ or \\computerName because these cannot be canonicalized
            if (!path.equals("\\\\") && !("\\\\".equals(file.getParent()))) {  //NOI18N
                LOG.log(Level.FINE, path, e);
            }
        }
        // #135547 - on Windows Vista map "Documents and Settings\<username>\My Documents" to "Users\<username>\Documents"
        if((Utilities.getOperatingSystem() & Utilities.OS_WINVISTA) != 0) {
            if(retVal == null) {
                retVal = file.getAbsoluteFile();
            }
            String absolutePath = retVal.getAbsolutePath();
            if(absolutePath.contains(":\\Documents and Settings")) {  //NOI18N
                absolutePath = absolutePath.replaceFirst("Documents and Settings", "Users");  //NOI18N
                absolutePath = absolutePath.replaceFirst("My Documents", "Documents");  //NOI18N
                absolutePath = absolutePath.replaceFirst("My Pictures", "Pictures");  //NOI18N
                absolutePath = absolutePath.replaceFirst("My Music", "Music");  //NOI18N
                retVal = new File(absolutePath);
            }
        }

        return (retVal != null) ? retVal : file.getAbsoluteFile();
    }

    private static Reference<Map<String, String>> normalizedRef = new SoftReference<Map<String, String>>(new ConcurrentHashMap<String, String>());
    private static Map<String, String> getNormalizedFilesMap() {
        Map<String, String> map = normalizedRef.get();
        if (map == null) {
            synchronized (FileUtil.class) {
                map = normalizedRef.get();
                if (map == null) {
                    map = new ConcurrentHashMap<String, String>();
                    normalizedRef = new SoftReference<Map<String, String>>(map);
                }
            }
        }
        return map;
    }
    static void freeCaches() {
        normalizedRef.clear();
    }

    /**
     * Returns a FileObject representing the root folder of an archive.
     * Clients may need to first call {@link #isArchiveFile(FileObject)} to determine
     * if the file object refers to an archive file.
     * @param fo a ZIP- (or JAR-) format archive file
     * @return a virtual archive root folder, or null if the file is not actually an archive
     * @since 4.48
     */
    public static FileObject getArchiveRoot(FileObject fo) {
        URL archiveURL = URLMapper.findURL(fo, URLMapper.EXTERNAL);

        if (archiveURL == null) {
            return null;
        }

        return URLMapper.findFileObject(getArchiveRoot(archiveURL));
    }

    /**
     * Returns a URL representing the root of an archive.
     * Clients may need to first call {@link #isArchiveFile(URL)} to determine if the URL
     * refers to an archive file.
     * @param url of a ZIP- (or JAR-) format archive file
     * @return the <code>jar</code>-protocol URL of the root of the archive
     * @since 4.48
     */
    public static URL getArchiveRoot(URL url) {
        try {
            // XXX TBD whether the url should ever be escaped...
            return new URL("jar:" + url + "!/"); // NOI18N
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Returns a FileObject representing an archive file containing the
     * FileObject given by the parameter.
     * <strong>Remember</strong> that any path within the archive is discarded
     * so you may need to check for non-root entries.
     * @param fo a file in a JAR filesystem
     * @return the file corresponding to the archive itself,
     *         or null if <code>fo</code> is not an archive entry
     * @since 4.48
     */
    public static FileObject getArchiveFile(FileObject fo) {
        Parameters.notNull("fo", fo);   //NOI18N
        try {
            FileSystem fs = fo.getFileSystem();

            if (fs instanceof JarFileSystem) {
                File jarFile = ((JarFileSystem) fs).getJarFile();

                return toFileObject(jarFile);
            }
        } catch (FileStateInvalidException e) {
            Exceptions.printStackTrace(e);
        }

        return null;
    }

    /**
     * Returns the URL of the archive file containing the file
     * referred to by a <code>jar</code>-protocol URL.
     * <strong>Remember</strong> that any path within the archive is discarded
     * so you may need to check for non-root entries.
     * @param url a URL
     * @return the embedded archive URL, or null if the URL is not a
     *         <code>jar</code>-protocol URL containing <code>!/</code>
     * @since 4.48
     */
    public static URL getArchiveFile(URL url) {
        String protocol = url.getProtocol();

        if ("jar".equals(protocol)) { //NOI18N

            String path = url.getPath();
            int index = path.indexOf("!/"); //NOI18N

            if (index >= 0) {
                String jarPath = null;
                try {
                    jarPath = path.substring(0, index);
                    if (jarPath.indexOf("file://") > -1 && jarPath.indexOf("file:////") == -1) {  //NOI18N
                        /* Replace because JDK application classloader wrongly recognizes UNC paths. */
                        jarPath = jarPath.replaceFirst("file://", "file:////");  //NOI18N
                    }
                    return new URL(jarPath);

                } catch (MalformedURLException mue) {                    
                    LOG.log(
                        Level.WARNING,
                        "Invalid URL ({0}): {1}, jarPath: {2}", //NOI18N
                        new Object[] {
                            mue.getMessage(),
                            url.toExternalForm(),
                            jarPath
                        });
                }
            }
        }

        return null;
    }

    /**
     * Tests if a file represents a JAR or ZIP archive.
     * @param fo the file to be tested
     * @return true if the file looks like a ZIP-format archive
     * @since 4.48
     */
    public static boolean isArchiveFile(FileObject fo) {
        Parameters.notNull("fileObject", fo);  //NOI18N

        if (!fo.isValid()) {
            return isArchiveFile(fo.getPath());
        }
        // XXX Special handling of virtual file objects: try to determine it using its name, but don't cache the
        // result; when the file is checked out the more correct method can be used
        if (fo.isVirtual()) {
            return isArchiveFile(fo.getPath());
        }

        if (fo.isFolder()) {
            return false;
        }

        // First check the cache.
        Boolean b = archiveFileCache.get(fo);

        if (b == null) {
            // Need to check it.
            try {
                InputStream in = fo.getInputStream();

                try {
                    byte[] buffer = new byte[4];
                    int len = in.read(buffer, 0, 4);

                    if (len == 4) {
                        // Got a header, see if it is a ZIP file.
                        b = Boolean.valueOf(Arrays.equals(ZIP_HEADER_1, buffer) || Arrays.equals(ZIP_HEADER_2, buffer));
                    } else {
                        //If the length is less than 4, it can be either
                        //broken (empty) archive file or other empty file.
                        //Return false and don't cache it, when the archive
                        //file will be written and closed its length will change
                        return false;
                    }
                } finally {
                    in.close();
                }
            } catch (IOException ioe) {
                // #160507 - ignore exception (e.g. permission denied)
                LOG.log(Level.FINE, null, ioe);
            }

            if (b == null) {
                b = isArchiveFile(fo.getPath());
            }

            archiveFileCache.put(fo, b);
        }

        return b.booleanValue();
    }

    /**
     * Tests if a URL represents a JAR or ZIP archive.
     * If there is no such file object, the test is done by heuristic: any URL with an extension is
     * treated as an archive.
     * @param url a URL to a file
     * @return true if the URL seems to represent a ZIP-format archive
     * @since 4.48
     */
    public static boolean isArchiveFile(URL url) {
        Parameters.notNull("url", url);  //NOI18N

        if ("jar".equals(url.getProtocol())) { //NOI18N

            //Already inside archive, return false
            return false;
        }

        FileObject fo = URLMapper.findFileObject(url);

        if ((fo != null) && !fo.isVirtual()) {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.log(Level.FINEST, "isArchiveFile_FILE_RESOLVED", fo); //NOI18N, used by FileUtilTest.testIsArchiveFileRace
            }
            return isArchiveFile(fo);
        } else {
            return isArchiveFile(url.getPath());
        }
    }

    /**
     * Convert a file such as would be shown in a classpath entry into a proper folder URL.
     * If the file looks to represent a directory, a <code>file</code> URL will be created.
     * If it looks to represent a ZIP archive, a <code>jar</code> URL will be created.
     * @param entry a file or directory name
     * @return an appropriate classpath URL which will always end in a slash (<samp>/</samp>),
     *         or null for an existing file which does not look like a valid archive
     * @since org.openide.filesystems 7.8
     */
    public static URL urlForArchiveOrDir(File entry) {
        try {
            boolean wasDir;
            boolean isDir;
            URL u;            
            do {
                wasDir = entry.isDirectory();
                LOG.finest("urlForArchiveOrDir:toURI:entry");   //NOI18N
                u = Utilities.toURI(entry).toURL();
                isDir = entry.isDirectory();
            } while (wasDir ^ isDir);
            if (isArchiveFile(u) || entry.isFile() && entry.length() < 4) {
                return getArchiveRoot(u);
            } else if (isDir) {
                assert u.toExternalForm().endsWith("/");    //NOI18N
                return u;
            } else if (!entry.exists()) {
                if (!u.toString().endsWith("/")) {  //NOI18N
                    u = new URL(u + "/"); // NOI18N
                }
                return u;
            } else {
                return null;
            }
        } catch (MalformedURLException x) {
            assert false : x;
            return null;
        }
    }

    /**
     * Convert a classpath-type URL to a corresponding file.
     * If it is a <code>jar</code> URL representing the root folder of a local disk archive,
     * that archive file will be returned.
     * If it is a <code>file</code> URL representing a local disk folder,
     * that folder will be returned.
     * @param entry a classpath entry or similar URL
     * @return a corresponding file, or null for e.g. a network URL or non-root JAR folder entry
     * @since org.openide.filesystems 7.8
     */
    public static File archiveOrDirForURL(URL entry) {
        String u = entry.toString();
        if (u.startsWith("jar:file:") && u.endsWith("!/")) { // NOI18N
            return Utilities.toFile(URI.create(u.substring(4, u.length() - 2)));
        } else if (u.startsWith("file:")) { // NOI18N
            return Utilities.toFile(URI.create(u));
        } else {
            return null;
        }
    }

    /**
     * Make sure that a JFileChooser does not traverse symlinks on Unix.
     * @param chooser a file chooser
     * @param currentDirectory if not null, a file to set as the current directory
     *                         using {@link JFileChooser#setCurrentDirectory} without canonicalizing
     * @see <a href="http://www.netbeans.org/issues/show_bug.cgi?id=46459">Issue #46459</a>
     * @see <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4906607">JRE bug #4906607</a>
     * @since org.openide/1 4.42
     * @deprecated Just use {@link JFileChooser#setCurrentDirectory}. JDK 6 does not have this bug.
     */
    @Deprecated
    public static void preventFileChooserSymlinkTraversal(JFileChooser chooser, File currentDirectory) {
        chooser.setCurrentDirectory(currentDirectory);
    }

    /**
     * Sorts some sibling file objects.
     * <p>Normally this is done by looking for numeric file attributes named <code>position</code>
     * on the children; children with a lower position number are placed first.
     * Now-deprecated relative ordering attributes of the form <code>earlier/later</code> may
     * also be used; if the above attribute has a boolean value of <code>true</code>,
     * then the file named <code>earlier</code> will be sorted somewhere (not necessarily directly)
     * before the file named <code>later</code>. Numeric and relative attributes may also be mixed.</p>
     * <p>The sort is <em>stable</em> at least to the extent that if there is no ordering information
     * whatsoever, the returned list will be in the same order as the incoming collection.</p>
     * @param children zero or more files (or folders); must all have the same {@link FileObject#getParent}
     * @param logWarnings true to log warnings about relative ordering attributes or other semantic problems, false to keep quiet
     * @return a sorted list of the same children
     * @throws IllegalArgumentException in case there are duplicates, or nulls, or the files do not have a common parent
     * @since org.openide.filesystems 7.2
     * @see #setOrder
     * @see <a href="http://wiki.netbeans.org/wiki/view/FolderOrdering103187">Specification</a>
     */
    public static List<FileObject> getOrder(Collection<FileObject> children, boolean logWarnings) throws IllegalArgumentException {
        return Ordering.getOrder(children, logWarnings);
    }

    /**
     * Imposes an order on some sibling file objects.
     * After this call, if no other changes have intervened,
     * {@link #getOrder} on these files should return a list in the same order.
     * Beyond the fact that this call may manipulate the <code>position</code> attributes
     * of files in the folder, and may delete deprecated relative ordering attributes on the folder,
     * the exact means of setting the order is unspecified.
     * @param children a list of zero or more files (or folders); must all have the same {@link FileObject#getParent}
     * @throws IllegalArgumentException in case there are duplicates, or nulls, or the files do not have a common parent
     * @throws IOException if new file attributes to order the children cannot be written out
     * @since org.openide.filesystems 7.2
     */
    public static void setOrder(List<FileObject> children) throws IllegalArgumentException, IOException {
        Ordering.setOrder(children);
    }

    /**
     * Checks whether a change in a given file attribute would affect the result of {@link #getOrder}.
     * @param event an attribute change event
     * @return true if the attribute in question might affect the order of some folder
     * @since org.openide.filesystems 7.2
     */
    public static boolean affectsOrder(FileAttributeEvent event) {
        return Ordering.affectsOrder(event);
    }

    /**
     * Returns {@code FileObject} from the NetBeans default (system, configuration)
     * filesystem or {@code null} if does not exist.
     * If you wish to create the file/folder when it does not already exist,
     * start with {@link #getConfigRoot} and use {@link #createData(FileObject, String)}
     * or {@link #createFolder(FileObject, String)} methods.
     * @param path the path from the root of the NetBeans default (system, configuration)
     * filesystem delimited by '/' or empty string to get root folder.
     * @throws NullPointerException if the path is {@code null}
     * @return a {@code FileObject} for given path in the NetBeans default (system, configuration)
     * filesystem or {@code null} if does not exist
     * @since org.openide.filesystems 7.19
     */
    @SuppressWarnings("deprecation")
    public static FileObject getConfigFile(String path) {
        Parameters.notNull("path", path);  //NOI18N
        return Repository.getDefault().getDefaultFileSystem().findResource(path);
    }
    
    /** Finds a config object under given path. The path contains the extension
     * of a file e.g.:
     * <pre>
     * Actions/Edit/org-openide-actions-CopyAction.instance
     * Services/Browsers/swing-browser.settings
     * </pre>
     * @param filePath path to .instance or .settings file
     * @param type the requested type for given object
     * @return either null or instance of requrested type
     * @since 7.49 
     */
    public static <T> T getConfigObject(String path, Class<T> type) {
        FileObject fo = getConfigFile(path);
        if (fo == null || fo.isFolder()) {
            return null;
        }
        return NamedServicesProvider.getConfigObject(path, type);
    }

    /**
     * Returns the root of the NetBeans default (system, configuration)
     * filesystem.
     * @return a {@code FileObject} for the root of the NetBeans default (system, configuration)
     * filesystem
     * @since org.openide.filesystems 7.19
     */
    public static FileObject getConfigRoot() {
        return getConfigFile("");  //NOI18N
    }

    private static File wrapFileNoCanonicalize(File f) {
        if (f instanceof NonCanonicalizingFile) {
            return f;
        } else if (f != null) {
            return new NonCanonicalizingFile(f);
        } else {
            return null;
        }
    }

    private static File[] wrapFilesNoCanonicalize(File[] fs) {
        if (fs != null) {
            for (int i = 0; i < fs.length; i++) {
                fs[i] = wrapFileNoCanonicalize(fs[i]);
            }
        }

        return fs;
    }

    private static final class NonCanonicalizingFile extends File {
        public NonCanonicalizingFile(File orig) {
            this(orig.getPath());
        }

        private NonCanonicalizingFile(String path) {
            super(path);
        }

        private NonCanonicalizingFile(URI uri) {
            super(uri);
        }

        @Override
        public File getCanonicalFile() throws IOException {
            return wrapFileNoCanonicalize(normalizeFile(super.getAbsoluteFile()));
        }

        @Override
        public String getCanonicalPath() throws IOException {
            return normalizeFile(super.getAbsoluteFile()).getAbsolutePath();
        }

        @Override
        public File getParentFile() {
            return wrapFileNoCanonicalize(super.getParentFile());
        }

        @Override
        public File getAbsoluteFile() {
            return wrapFileNoCanonicalize(super.getAbsoluteFile());
        }

        @Override
        public File[] listFiles() {
            return wrapFilesNoCanonicalize(super.listFiles());
        }

        @Override
        public File[] listFiles(FileFilter filter) {
            return wrapFilesNoCanonicalize(super.listFiles(filter));
        }

        @Override
        public File[] listFiles(FilenameFilter filter) {
            return wrapFilesNoCanonicalize(super.listFiles(filter));
        }
    }

    private static final class NonCanonicalizingFileSystemView extends FileSystemView {
        private final FileSystemView delegate = FileSystemView.getFileSystemView();

        public NonCanonicalizingFileSystemView() {
        }

        @Override
        public boolean isFloppyDrive(File dir) {
            return delegate.isFloppyDrive(dir);
        }

        @Override
        public boolean isComputerNode(File dir) {
            return delegate.isComputerNode(dir);
        }

        public File createNewFolder(File containingDir)
        throws IOException {
            return wrapFileNoCanonicalize(delegate.createNewFolder(containingDir));
        }

        @Override
        public boolean isDrive(File dir) {
            return delegate.isDrive(dir);
        }

        @Override
        public boolean isFileSystemRoot(File dir) {
            return delegate.isFileSystemRoot(dir);
        }

        @Override
        public File getHomeDirectory() {
            return wrapFileNoCanonicalize(delegate.getHomeDirectory());
        }

        @Override
        public File createFileObject(File dir, String filename) {
            return wrapFileNoCanonicalize(delegate.createFileObject(dir, filename));
        }

        @Override
        public Boolean isTraversable(File f) {
            return delegate.isTraversable(f);
        }

        @Override
        public boolean isFileSystem(File f) {
            return delegate.isFileSystem(f);
        }

        /*
        protected File createFileSystemRoot(File f) {
            return translate(delegate.createFileSystemRoot(f));
        }
         */
        @Override
        public File getChild(File parent, String fileName) {
            return wrapFileNoCanonicalize(delegate.getChild(parent, fileName));
        }

        @Override
        public File getParentDirectory(File dir) {
            return wrapFileNoCanonicalize(delegate.getParentDirectory(dir));
        }

        @Override
        public Icon getSystemIcon(File f) {
            return delegate.getSystemIcon(f);
        }

        @Override
        public boolean isParent(File folder, File file) {
            return delegate.isParent(folder, file);
        }

        @Override
        public String getSystemTypeDescription(File f) {
            return delegate.getSystemTypeDescription(f);
        }

        @Override
        public File getDefaultDirectory() {
            return wrapFileNoCanonicalize(delegate.getDefaultDirectory());
        }

        @Override
        public String getSystemDisplayName(File f) {
            return delegate.getSystemDisplayName(f);
        }

        @Override
        public File[] getRoots() {
            return wrapFilesNoCanonicalize(delegate.getRoots());
        }

        @Override
        public boolean isHiddenFile(File f) {
            return delegate.isHiddenFile(f);
        }

        @Override
        public File[] getFiles(File dir, boolean useFileHiding) {
            return wrapFilesNoCanonicalize(delegate.getFiles(dir, useFileHiding));
        }

        @Override
        public boolean isRoot(File f) {
            return delegate.isRoot(f);
        }

        @Override
        public File createFileObject(String path) {
            return wrapFileNoCanonicalize(delegate.createFileObject(path));
        }
    }
    
    private static FileSystem getDiskFileSystem() {
        synchronized (FileUtil.class) {
            return diskFileSystem;
        }
    }

    private static void setDiskFileSystem(FileSystem fs) {
        Object o = fs.getRoot().getAttribute("SupportsRefreshForNoPublicAPI");
        if (o instanceof Boolean && ((Boolean) o).booleanValue()) {
            synchronized (FileUtil.class) {
                diskFileSystem = fs;
            }
        }
    }

    /**
     * Tests if a non existent path represents a file.
     * @param path to be tested, separated by '/'.
     * @return true if the file has '.' after last '/'.
     */
    private static boolean isArchiveFile (final String path) {
        int index = path.lastIndexOf('.');  //NOI18N
        return (index != -1) && (index > path.lastIndexOf('/') + 1);    //NOI18N
    }
}
