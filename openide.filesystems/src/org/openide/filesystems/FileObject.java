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
import java.io.FileNotFoundException;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import org.openide.util.Enumerations;
import org.openide.util.NbBundle;
import org.openide.util.UserQuestionException;

/** This is the base for all implementations of file objects on a filesystem.
* Provides basic information about the object (its name, parent,
* whether it exists, etc.) and operations on it (move, delete, etc.).
*
* @author Jaroslav Tulach, Petr Hamernik, Ian Formanek
*/
public abstract class FileObject extends Object implements Serializable {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 85305031923497718L;

    /** Get the name without extension of this file or folder.
    * Period at first position is not considered as extension-separator
    * For the root folder of a filesystem, this will be the empty
    * string (the extension will also be the empty string, and the
    * fully qualified name with any delimiters will also be the
    * empty string).
    * @return name of the file or folder(in its enclosing folder)
    */
    public abstract String getName();

    /** Get the extension of this file or folder.
    * Period at first position is not considered as extension-separator
    * This is the string after the last dot of the full name, if any.
    *
    * @return extension of the file or folder (if any) or empty string if there is none
    */
    public abstract String getExt();

    /** Renames this file (or folder).
    * Both the new basename and new extension should be specified.
    * <p>
    * Note that using this call, it is currently only possible to rename <em>within</em>
    * a parent folder, and not to do moves <em>across</em> folders.
    * Conversely, implementing filesystems need only implement "simple" renames.
    * If you wish to move a file across folders, you should call {@link FileUtil#moveFile}.
    * @param lock File must be locked before renaming.
    * @param name new basename of file
    * @param ext new extension of file (ignored for folders)
    */
    public abstract void rename(FileLock lock, String name, String ext)
    throws IOException;

    /** Copies this file. This allows the filesystem to perform any additional
    * operation associated with the copy. But the default implementation is simple
    * copy of the file and its attributes
    *
    * @param target target folder to move this file to
    * @param name new basename of file
    * @param ext new extension of file (ignored for folders)
    * @return the newly created file object representing the moved file
    */
    public FileObject copy(FileObject target, String name, String ext)
    throws IOException {
        if (isFolder()) {
            FileObject peer = target.createFolder(name);
            FileUtil.copyAttributes(this, peer);
            for (FileObject fo : getChildren()) {
                fo.copy(peer, fo.getName(), fo.getExt());
            }
            return peer;
        }

        FileObject dest = FileUtil.copyFileImpl(this, target, name, ext);

        return dest;
    }

    /** Moves this file. This allows the filesystem to perform any additional
    * operation associated with the move. But the default implementation is encapsulated
    * as copy and delete.
    *
    * @param lock File must be locked before renaming.
    * @param target target folder to move this file to
    * @param name new basename of file
    * @param ext new extension of file (ignored for folders)
    * @return the newly created file object representing the moved file
    */
    public FileObject move(FileLock lock, FileObject target, String name, String ext)
    throws IOException {
        if (getParent().equals(target)) {
            // it is possible to do only rename
            rename(lock, name, ext);

            return this;
        } else {
            // have to do copy
            FileObject dest = copy(target, name, ext);
            delete(lock);

            return dest;
        }
    }

    /**
     * Gets a textual represtentation of this <code>FileObject</code>.
     * The precise format is not defined. In particular it is probably
     * <em>not</em> a resource path.
     * For that purpose use {@link #getPath} directly.
     * <p>Typically it is useful for debugging purposes. Example of correct usage:
     * <pre>
     * <font class="type">FileObject</font> <font class="variable-name">fo</font> = getSomeFileObject();
     * ErrorManager.getDefault().log(<font class="string">"Got a change from "</font> + fo);
     * </pre>
     * @return some representation of this file object
     */
    @Override
    public String toString() {
        String cname = getClass().getName();
        String cnameShort = cname.substring(cname.lastIndexOf('.') + 1);
        
        try {
            return cnameShort + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' + (isRoot() ? "root of " + getFileSystem() : getPath()) + ']'; // NOI18N
        } catch (FileStateInvalidException x) {
            return cnameShort + '@' + Integer.toHexString(System.identityHashCode(this)) + "[???]"; // NOI18N
        }
    }

    /** Get the full resource path of this file object starting from the filesystem root.
     * Folders are separated with forward slashes. File extensions, if present,
     * are included. The root folder's path is the empty string. The path of a folder
     * never ends with a slash.
     * <p>Subclasses are strongly encouraged to override this method.
     * <p>Never use this to get a display name for the file! Use {@link FileUtil#getFileDisplayName}.
     * <p>Do not use this method to find a file path on disk! Use {@link FileUtil#toFile}.
     * @return the path, for example <samp>path/from/root.ext</samp>
     * @see FileSystem#findResource
     * @since 3.7
     */
    public String getPath() {
        StringBuilder[] buf = { null };
        constructName(buf, '/', 0);
        return buf[0].toString();
    }

    /** Get fully-qualified filename. Does so by walking through all folders
    * to the root of the filesystem. Separates files with provided <code>separatorChar</code>.
    * The extension, if present, is separated from the basename with <code>extSepChar</code>.
    * <p><strong>Note:</strong> <samp>fo.getPath()</samp> will have the
    * same effect as using this method with <samp>/</samp> and <samp>.</samp> (the standard
    * path and extension delimiters).
    * @param separatorChar char to separate folders and files
    * @param extSepChar char to separate extension
    * @return the fully-qualified filename
    * @deprecated Please use the <a href="@org-netbeans-api-java-classpath@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
    */
    @Deprecated
    public String getPackageNameExt(char separatorChar, char extSepChar) {
        assert false : "Deprecated.";

        if (isRoot() || getParent().isRoot()) {
            return getNameExt();
        }

        StringBuilder[] arr = new StringBuilder[1];
        getParent().constructName(arr, separatorChar, 50);

        String ext = getExt();

        if ((ext == null) || ext.equals("")) { // NOI18N
            arr[0].append(separatorChar).append(getNameExt());
        } else {
            arr[0].append(separatorChar).append(getName()).append(extSepChar).append(getExt());
        }

        return arr[0].toString();
    }

    /** Get fully-qualified filename, but without extension.
    * Like {@link #getPackageNameExt} but omits the extension.
    * @param separatorChar char to separate folders and files
    * @return the fully-qualified filename
    * @deprecated Please use the <a href="@org-netbeans-api-java-classpath@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead.
    */
    @Deprecated
    public String getPackageName(char separatorChar) {
        assert false : "Deprecated.";

        if (isRoot() || getParent().isRoot()) {
            return (isFolder()) ? getNameExt() : getName();
        }

        StringBuilder[] arr = new StringBuilder[1];
        String name = getName();

        getParent().constructName(arr, separatorChar, name.length());
        arr[0].append(separatorChar).append(name);
        return arr[0].toString();
    }

    /** Getter for name and extension of a file object. Dot is used
     * as separator between name and ext.
     * @return string name of the file in the folder (with extension)
     */
    public String getNameExt() {
        String n = getName();
        String e = getExt();

        return ((e == null) || (e.length() == 0)) ? n : (n + '.' + e);
    }

    /** Constructs path of file.
    * @param arr to place the string buffer
    * @param sepChar separator character
    */
    private void constructName(StringBuilder[] arr, char sepChar, int lengthSoFar) {
        String myName = getNameExt();
        int myLen = lengthSoFar + myName.length();

        FileObject parent = getParent();

        if ((parent != null) && !parent.isRoot()) {
            parent.constructName(arr, sepChar, myLen + 1);
            arr[0].append(sepChar);
        } else {
            assert arr[0] == null;
            arr[0] = new StringBuilder(myLen);
        }
        arr[0].append(getNameExt());
    }

    /** Get the filesystem containing this file.
    * <p>
    * Note that it may be possible for a stale file object to exist which refers to a now-defunct filesystem.
    * If this is the case, this method will throw an exception.
    * @return the filesystem
    * @exception FileStateInvalidException if the reference to the file
    *   system has been lost (e.g., if the filesystem was deleted)
    */
    public abstract FileSystem getFileSystem() throws FileStateInvalidException;

    /** Get parent folder.
    * The returned object will satisfy {@link #isFolder}.
    *
    * @return the parent folder or <code>null</code> if this object {@link #isRoot}.
    */
    public abstract FileObject getParent();

    /** Test whether this object is a folder.
    * @return true if the file object is a folder (i.e., can have children)
    */
    public abstract boolean isFolder();

    /**
    * Get last modification time.
    * @return the date
    */
    public abstract java.util.Date lastModified();

    /** Test whether this object is the root folder.
    * The root should always be a folder.
    * @return true if the object is the root of a filesystem
    */
    public abstract boolean isRoot();

    /** Test whether this object is a data object.
    * This is exclusive with {@link #isFolder}.
    * @return true if the file object represents data (i.e., can be read and written)
    */
    public abstract boolean isData();

    /** Test whether the file is valid. The file can be invalid if it has been deserialized
    * and the file no longer exists on disk; or if the file has been deleted.
    *
    * @return true if the file object is valid
    */
    public abstract boolean isValid();

    /** Test whether there is a file with the same basename and only a changed extension in the same folder.
    * The default implementation asks this file's parent using {@link #getFileObject(String name, String ext)}.
    *
    * @param ext the alternate extension
    * @return true if there is such a file
    */
    public boolean existsExt(String ext) {
        FileObject parent = getParent();

        return (parent != null) && (parent.getFileObject(getName(), ext) != null);
    }

    /** Delete this file. If the file is a folder and it is not empty then
    * all of its contents are also recursively deleted.
    *
    * @param lock the lock obtained by a call to {@link #lock}
    * @exception IOException if the file could not be deleted
    */
    public abstract void delete(FileLock lock) throws IOException;

    /** Delete this file. If the file is a folder and it is not empty then
    * all of its contents are also recursively deleted. FileObject is locked
    * before delete and finally is this lock released.
    *
    * @exception IOException if the file could not be deleted or
    * FileAlreadyLockedException if the file is already locked {@link #lock}
    * @since 1.15
    */
    public final void delete() throws IOException {
        FileLock lock = lock();

        try {
            delete(lock);
        } finally {
            lock.releaseLock();
        }
    }

    /** Get the file attribute with the specified name.
    * @param attrName name of the attribute
    * @return appropriate (serializable) value or <CODE>null</CODE> if the attribute is unset (or could not be properly restored for some reason)
    */
    abstract public Object getAttribute(String attrName);

    /** Set the file attribute with the specified name.
    * @param attrName name of the attribute
    * @param value new value or <code>null</code> to clear the attribute. Must be serializable, although particular filesystems may or may not use serialization to store attribute values.
    * @exception IOException if the attribute cannot be set. If serialization is used to store it, this may in fact be a subclass such as {@link java.io.NotSerializableException}.
    */
    abstract public void setAttribute(String attrName, Object value)
    throws IOException;

    /** Get all file attribute names for this file.
    * @return enumeration of keys (as strings)
    */
    abstract public Enumeration<String> getAttributes();

    /** Test whether this file has the specified extension.
    * @param ext the extension the file should have
    * @return true if the text after the last period (<code>.</code>) is equal to the given extension
    */
    public final boolean hasExt(String ext) {
        if (isHasExtOverride()) {
            return hasExtOverride(ext);
        }

        return getExt().equals(ext);
    }

    /** Overriden in AbstractFolder */
    boolean isHasExtOverride() {
        return false;
    }

    /** Overridden in AbstractFolder */
    boolean hasExtOverride(String ext) {
        return false;
    }

    /** Add new listener to this object.
    * @param fcl the listener
    */
    public abstract void addFileChangeListener(FileChangeListener fcl);

    /** Remove listener from this object.
    * @param fcl the listener
    */
    public abstract void removeFileChangeListener(FileChangeListener fcl);


    /** Adds a listener to this {@link FileObject} and all its children and
     * children or its children.
     * It is guaranteed that whenever a change
     * is made via the FileSystem API itself under this {@link FileObject}
     * that it is notified to the <code>fcl</code> listener. Whether external
     * changes (if they make sense) are detected and
     * notified depends on actual implementation. As some implementations may
     * need to perform non-trivial amount of work during initialization of
     * listeners, this methods can take long time. Usage of this method may
     * consume a lot of system resources and as such it shall be used with care.
     * Traditional {@link #addFileChangeListener(org.openide.filesystems.FileChangeListener)}
     * is definitely preferred variant.
     * <p class="nonnormative">
     * If you are running with the MasterFS module enabled, it guarantees
     * that for files backed with real {@link File}, the system initializes
     * itself to detect external changes on the whole subtree.
     * This requires non-trivial amount of work and especially on slow
     * disks (aka networks ones) may take a long time to add the listener
     * and also refresh the system when {@link FileObject#refresh()}
     * and especially {@link FileUtil#refreshAll()} is requested.
     * </p>
     *
     * @param fcl the listener to register
     * @since 7.28
     */
    public void addRecursiveListener(FileChangeListener fcl) {
        if (!isFolder()) {
            addFileChangeListener(fcl);
            return;
        }
        try {
            boolean allowsExternalChanges = getFileSystem() instanceof LocalFileSystem;
            getFileSystem().addFileChangeListener(new RecursiveListener(this, fcl, allowsExternalChanges));
        } catch (FileStateInvalidException ex) {
            ExternalUtil.LOG.log(Level.FINE, "Cannot remove listener from " + this, ex);
        }
    }

    /** Removes listener previously added by {@link #addRecursiveListener(org.openide.filesystems.FileChangeListener)}
     *
     * @param fcl the listener to remove
     * @since 7.28
     */
    public void removeRecursiveListener(FileChangeListener fcl) {
        if (!isFolder()) {
            removeFileChangeListener(fcl);
            return;
        }
        try {
            getFileSystem().removeFileChangeListener(new RecursiveListener(this, fcl, false));
        } catch (FileStateInvalidException ex) {
            ExternalUtil.LOG.log(Level.FINE, "Cannot remove listener from " + this, ex);
        }
    }

    /** Fire data creation event.
    * @param en listeners that should receive the event
    * @param fe the event to fire in this object
    */
    protected void fireFileDataCreatedEvent(Enumeration<FileChangeListener> en, FileEvent fe) {
        dispatchEvent(FCLSupport.Op.DATA_CREATED, en, fe);
    }

    /** Fire folder creation event.
    * @param en listeners that should receive the event
    * @param fe the event to fire in this object
    */
    protected void fireFileFolderCreatedEvent(Enumeration<FileChangeListener> en, FileEvent fe) {
        dispatchEvent(FCLSupport.Op.FOLDER_CREATED, en, fe);
    }

    /** Fire file change event.
    * @param en listeners that should receive the event
    * @param fe the event to fire in this object
    */
    protected void fireFileChangedEvent(Enumeration<FileChangeListener> en, FileEvent fe) {
        dispatchEvent(FCLSupport.Op.FILE_CHANGED, en, fe);
    }

    /** Fire file deletion event.
    * @param en listeners that should receive the event
    * @param fe the event to fire in this object
    */
    protected void fireFileDeletedEvent(Enumeration<FileChangeListener> en, FileEvent fe) {
        dispatchEvent(FCLSupport.Op.FILE_DELETED, en, fe);
    }

    /** Fire file attribute change event.
    * @param en listeners that should receive the event
    * @param fe the event to fire in this object
    */
    protected void fireFileAttributeChangedEvent(Enumeration<FileChangeListener> en, FileAttributeEvent fe) {
        dispatchEvent(FCLSupport.Op.ATTR_CHANGED, en, fe);
    }

    /** Fire file rename event.
    * @param en listeners that should receive the event
    * @param fe the event to fire in this object
    */
    protected void fireFileRenamedEvent(Enumeration<FileChangeListener> en, FileRenameEvent fe) {
        dispatchEvent(FCLSupport.Op.FILE_RENAMED, en, fe);
    }

    /** Puts the dispatch event into the filesystem.
    */
    private final void dispatchEvent(FCLSupport.Op op, Enumeration<FileChangeListener> en, FileEvent fe) {
        try {
            FileSystem fs = getFileSystem();
            fs.dispatchEvent(new ED(op, en, fe));
        } catch (FileStateInvalidException ex) {
            // no filesystem, no notification
        }
    }

    final void dispatchEvent(Enumeration<FileChangeListener> en, FileEvent fe) {
        try {
            getFileSystem().dispatchEvent(new ED(en, fe));
        } catch (FileStateInvalidException ex) {
            // no filesystem, no notification
        }
    }

    /** Get the MIME type of this file.
    * The MIME type identifies the type of the file's contents and should be used in the same way as in the <B>Java
    * Activation Framework</B> or in the {@link java.awt.datatransfer} package.
    * <P>
    * The default implementation calls {@link FileUtil#getMIMEType}.
    * (As a fallback return value, <code>content/unknown</code> is used.)
    * @return the MIME type textual representation, e.g. <code>"text/plain"</code>; never <code>null</code>
    */
    public String getMIMEType() {
        String mimeType = FileUtil.getMIMEType(this);
        return  mimeType == null ? "content/unknown" : mimeType;  //NOI18N
    }

    /** Get the size of the file.
    * @return the size of the file in bytes or zero if the file does not contain data (does not
    *  exist or is a folder).
    */
    public abstract long getSize();

    /** Get input stream.
    * @return an input stream to read the contents of this file
    * @exception FileNotFoundException if the file does not exists, is a folder
    * rather than a regular file  or is invalid
    */
    public abstract InputStream getInputStream() throws FileNotFoundException;

    /** Reads the full content of the file object and returns it as array of
     * bytes.
     * @return array of bytes
     * @exception IOException in case the content cannot be fully read
     * @since 7.21
     */
    public byte[] asBytes() throws IOException {
        long len = getSize();
        if (len > Integer.MAX_VALUE) {
            throw new IOException("Too big file " + getPath()); // NOI18N
        }
        InputStream is = getInputStream();
        try {
            byte[] arr = new byte[(int)len];
            int pos = 0;
            while (pos < arr.length) {
                int read = is.read(arr, pos, arr.length - pos);
                if (read == -1) {
                    break;
                }
                pos += read;
            }
            if (pos != arr.length) {
                throw new IOException("Just " + pos + " bytes read from " + getPath()); // NOI18N
            }
            return arr;
        } finally {
            is.close();
        }
    }

    /** Reads the full content of the file object and returns it as string.
     * @param encoding the encoding to use
     * @return string representing the content of the file
     * @exception IOException in case the content cannot be fully read
     * @since 7.21
     */
    public String asText(String encoding) throws IOException {
        return new String(asBytes(), encoding);
    }

    /** Reads the full content of the file object and returns it as string.
     * This is similar to calling {@link #asText(java.lang.String)} with
     * default system encoding.
     * 
     * @return string representing the content of the file
     * @exception IOException in case the content cannot be fully read
     * @since 7.21
     */
    public String asText() throws IOException {
        return asText(Charset.defaultCharset().name());
    }

    /** Reads the full content of the file line by line with default
     * system encoding. Typical usage is
     * in <code>for</code> loops:
     * <pre>
     * for (String line : fo.asLines()) {
     *   // do something
     * }
     * </pre>
     * <p>
     * The list is optimized for iterating line by line, other operations,
     * like accessing all the lines or counting the number of its lines may
     * be suboptimal.
     *
     * @return list of strings representing the content of the file
     * @exception IOException in case the content cannot be fully read
     * @since 7.21
     */
    public List<String> asLines() throws IOException {
        return asLines(Charset.defaultCharset().name());
    }
    
    /** Reads the full content of the file line by line. Typical usage is
     * in <code>for</code> loops:
     * <pre>
     * for (String line : fo.asLines("UTF-8")) {
     *   // do something
     * }
     * </pre>
     * <p>
     * The list is optimized for iterating line by line, other operations,
     * like accessing all the lines or counting the number of its lines may
     * be suboptimal.
     *
     * @param encoding the encoding to use
     * @return list of strings representing the content of the file
     * @exception IOException in case the content cannot be fully read
     * @since 7.21
     */
    public List<String> asLines(final String encoding) throws IOException {
        return new FileObjectLines(encoding, this);
    }

    /** Get output stream.
    * @param lock the lock that belongs to this file (obtained by a call to
    *   {@link #lock})
    * @return output stream to overwrite the contents of this file
    * @exception IOException if an error occures (the file is invalid, etc.)
    */
    public abstract OutputStream getOutputStream(FileLock lock)
    throws IOException;

    /** Get output stream.
     * @return output stream to overwrite the contents of this file
     * @throws IOException if an error occurs (the file is invalid, etc.)
     * @throws FileAlreadyLockedException if the file is already locked
     * @since 6.6
     */
    public final OutputStream getOutputStream() throws FileAlreadyLockedException, IOException  {
        final FileLock lock = lock();
        final OutputStream os;
        try {
            os = getOutputStream(lock);
            return new FilterOutputStream(os) {
                @Override
                public void write(byte b[], int off, int len) throws IOException {
                    // Delegate to real stream because it is more efficient if it is FileOutputStream.
                    // Otherwise it is copied byte by byte.
                    os.write(b, off, len);
                }

                @Override
                public void close() throws IOException {
                    try {
                        super.close();
                        lock.releaseLock();
                    } catch(IOException iex) {
                        if (lock.isValid()) {
                            lock.releaseLock();
                        }
                        throw iex;
                    }
                }
            };
        } catch(IOException iex) {
            if (lock.isValid()) {
                lock.releaseLock();
            }
            throw iex;
        }
    }

    
    /** Lock this file.
    * @return lock that can be used to perform various modifications on the file
    * @throws FileAlreadyLockedException if the file is already locked
    * @throws UserQuestionException in case when the lock cannot be obtained now,
    *    but the underlaying implementation is able to do it after some
    *    complex/dangerous/long-lasting operation and request confirmation
    *    from the user
    *
    */
    public abstract FileLock lock() throws IOException;

    /**
     * Test if file is locked
     * @return true if file is locked
     * @since 7.3
     */
    public boolean isLocked() {
        FileLock fLock = null;
        try {
            fLock = lock();
        } catch (FileAlreadyLockedException fax) {
            return true;
        } catch (IOException ex) {
            return false;
        } finally {
            if (fLock != null) {
                fLock.releaseLock();
            }
        }
        return fLock == null;
    }

    /** Indicate whether this file is important from a user perspective.
    * This method allows a filesystem to distingush between important and
    * unimportant files when this distinction is possible.
    * <P>
    * <em>For example:</em> Java sources have important <code>.java</code> files and
    * unimportant <code>.class</code> files. If the filesystem provides
    * an "archive" feature it should archive only <code>.java</code> files.
    * @param b true if the file should be considered important
    * @deprecated No longer used. Instead use
    * <a href="@org-netbeans-modules-queries@/org/netbeans/api/queries/SharabilityQuery.html"><code>SharabilityQuery</code></a>.
    */
    @Deprecated
    public abstract void setImportant(boolean b);

    /** Get all children of this folder (files and subfolders). If the file does not have children
    * (does not exist or is not a folder) then an empty array should be returned. No particular order is assumed.
    *
    * @return array of direct children
    * @see #getChildren(boolean)
    * @see #getFolders
    * @see #getData
    */
    public abstract FileObject[] getChildren();

    /** Enumerate all children of this folder. If the children should be enumerated
    * recursively, first all direct children are listed; then children of direct subfolders; and so on.
    *
    * @param rec whether to enumerate recursively
    * @return enumeration of type <code>FileObject</code>
    */
    public Enumeration<? extends FileObject> getChildren(final boolean rec) {
        class WithChildren implements Enumerations.Processor<FileObject, FileObject> {
            public FileObject process(FileObject fo, Collection<FileObject> toAdd) {
                if (rec && fo.isFolder()) {
                    toAdd.addAll(Arrays.asList(fo.getChildren()));
                }

                return fo;
            }
        }

        return Enumerations.queue(Enumerations.array(getChildren()), new WithChildren());
    }

    /** Enumerate the subfolders of this folder.
    * @param rec whether to recursively list subfolders
    * @return enumeration of type <code>FileObject</code> (satisfying {@link #isFolder})
    */
    public Enumeration<? extends FileObject> getFolders(boolean rec) {
        return Enumerations.filter(getChildren(rec), new OnlyFolders(true));
    }

    /** Enumerate all data files in this folder.
    * @param rec whether to recursively search subfolders
    * @return enumeration of type <code>FileObject</code> (satisfying {@link #isData})
    */
    public Enumeration<? extends FileObject> getData(boolean rec) {
        return Enumerations.filter(getChildren(rec), new OnlyFolders(false));
    }

    /** Retrieve file or folder contained in this folder by name.
    * <em>Note</em> that neither file nor folder is created on disk.
    * @param name basename of the file or folder (in this folder)
    * @param ext extension of the file; <CODE>null</CODE> or <code>""</code>
    *    if the file should have no extension or if folder is requested
    * @return the object representing this file or <CODE>null</CODE> if the file
    *   or folder does not exist
    * @exception IllegalArgumentException if <code>this</code> is not a folder
    */
    public abstract FileObject getFileObject(String name, String ext);

    /** Retrieve file or folder relative to a current folder, with a given relative path.
    * <em>Note</em> that neither file nor folder is created on disk. This method isn't final since revision 1.93.
    * @param relativePath is just basename of the file or (since 4.16) the relative path delimited by '/'
    * @return the object representing this file or <CODE>null</CODE> if the file
    *   or folder does not exist
    * @exception IllegalArgumentException if <code>this</code> is not a folder
    */
    public FileObject getFileObject(String relativePath) {
        if (relativePath.startsWith("/") && !relativePath.startsWith("//")) {
            relativePath = relativePath.substring(1);
        }

        FileObject myObj = this;
        StringTokenizer st = new StringTokenizer(relativePath, "/");
        
        if(relativePath.startsWith("//")) {
            // if it is UNC absolute path, start with //ComputerName/sharedFolder
            myObj = myObj.getFileObject("//"+st.nextToken()+"/"+st.nextToken(), null);
        }
        while ((myObj != null) && st.hasMoreTokens()) {
            String nameExt = st.nextToken();
            myObj = myObj.getFileObject(nameExt, null);
        }

        return myObj;
    }

    /**
     * Create a new folder below this one with the specified name.
     * Fires {@link FileChangeListener#fileFolderCreated}.
     *
     * @param name the name of folder to create. Periods in name are allowed (but no slashes).
     * @return the new folder
     * @exception IOException if the folder cannot be created (e.g. already exists), or if <code>this</code> is not a folder
     * @see FileUtil#createFolder
     */
    public abstract FileObject createFolder(String name)
    throws IOException;

    /**
     * Create new data file in this folder with the specified name.
     * Fires {@link FileChangeListener#fileDataCreated}.
     *
     * @param name the name of data object to create (can contain a period, but no slashes)
     * @param ext the extension of the file (or <code>null</code> or <code>""</code>)
     * @return the new data file object
     * @exception IOException if the file cannot be created (e.g. already exists), or if <code>this</code> is not a folder
     * @see FileUtil#createData
     */
    public abstract FileObject createData(String name, String ext)
    throws IOException;

    /**
     * Create new data file in this folder with the specified name.
     * Fires {@link FileChangeListener#fileDataCreated}.
     *
     * @param name the name of data object to create (can contain a period, but no slashes)
     * @return the new data file object
     * @exception IOException if the file cannot be created (e.g. already exists), or if <code>this</code> is not a folder
     * @since 1.17
     * @see FileUtil#createData
     */
    public FileObject createData(String name) throws IOException {
        return createData(name, ""); // NOI18N        
    }

    /** Test whether this file can be written to or not.
     * <P>
     * The value returned from this method should indicate the capabilities of the
     * file from the point of view of users of the FileObject's API, the actual
     * state of the file on a disk does not matter if the implementation of the
     * filesystem can change it when requested.
     * <P>
     * The result returned from this method should be tight together with
     * the expected behaviour of <code>getOutputStream</code>. If it is
     * likely that the method successfully returns a stream that can be
     * written to, let the <code>isReadOnly</code> return <code>false</code>.
     * <P>
     * Also other fileobject methods like <code>delete</code>
     * are suggested to be connected to result of this method. If not
     * read only, then it can be deleted, etc.
     * <p>
     * It is a good idea to call this method before attempting to perform any
     * operation on the FileObject that might throw an IOException simply
     * because it is read-only. If isReadOnly returns true, the operation may
     * be skipped, or the user notified that it cannot be done.
     * <em>However</em> it is often desirable for the user to be able to
     * continue the operation in case the filesystem supports making a file
     * writable. In this case calling code should:
     * <ol>
     * <li>Call {@link #lock} and catch any exception thrown.
     * <li>Then:
     * <ul>
     * <li>If no exception is thrown, proceed with the operation.
     * <li>If a {@link UserQuestionException} is thrown,
     * call {@link UserQuestionException#confirmed} on it
     * (asynchronously - do not block any important threads). If <code>true</code>,
     * proceed with the operation. If <code>false</code>, exit.
     * If an <code>IOException</code> is thrown, notify it and exit.
     * <li>If another <code>IOException</code> is thrown, call {@link #isReadOnly}.
     * If <code>true</code>, ignore the exception (it is expected).
     * If <code>false</code>, notify it.
     * </ul>
     * In either case, exit.
     * </ol>
     * <p>
     *
     * @return <CODE>true</CODE> if file is read-only
     * @deprecated Please use the {@link #canWrite}.
     */
    @Deprecated
    public abstract boolean isReadOnly();

    /**
     * Tests if this file can be written to.
     * <P>
     * The default implementation simply uses <code> java.io.File.canWrite </code>
     * if there exists conversion to <code> java.io.File</code> (see {@link FileUtil#toFile}).
     * If conversion is not possible, then deprecated method {@link #isReadOnly} is used.
     * @return true if this file can be written, false if not.
     * @since 3.31
     */
    public boolean canWrite() {
        File f = FileUtil.toFile(this);

        if (f != null) {
            return f.canWrite();
        }

        return !isReadOnly();
    }

    /**
     * Tests if this file can be read.
     * <P>
     * The default implementation simply uses <code> java.io.File.canRead </code>
     * if there exists conversion to <code> java.io.File</code> (see {@link FileUtil#toFile}).
     * If conversion is not possible, then <code>true </code> is returned.
     * @return true if this file can be read, false if not.
     * @since 3.31
     */
    public boolean canRead() {
        File f = FileUtil.toFile(this);

        if (f != null) {
            return f.canRead();
        }

        return true;
    }

    /** Should check for external modifications. For folders it should reread
    * the content of disk, for data file it should check for the last
    * time the file has been modified.
    *
    * @param expected should the file events be marked as expected change or not?
    * @see FileEvent#isExpected
    */
    public void refresh(boolean expected) {
    }

    /** Should check for external modifications. For folders it should reread
    * the content of disk, for data file it should check for the last
    * time the file has been modified.
    * <P>
    * The file events are marked as unexpected.
    */
    public void refresh() {
        refresh(false);
    }

    /** Get URL that can be used to access this file.
     * If the file object does not correspond to a disk file or JAR entry,
     * the URL will only be usable within NetBeans as it uses a special protocol handler.
     * Otherwise an attempt is made to produce an external URL.
    * @return URL of this file object
    * @exception FileStateInvalidException if the file is not valid
     * @see URLMapper#findURL
     * @see URLMapper#INTERNAL
    */
    public final URL getURL() throws FileStateInvalidException {
        // XXX why does this still throw FSIE? need not
        return URLMapper.findURL(this, URLMapper.INTERNAL);
    }

    /**
     * Tests if file really exists or is missing. Some operation on it may be restricted.
     * @return true indicates that the file is missing.
     * @since 1.9
     */
    public boolean isVirtual() {
        return false;
    }

    /** Listeners registered from MultiFileObject are considered as priority
     *  listeners.
     */
    static boolean isPriorityListener(FileChangeListener fcl) {
        if (fcl instanceof PriorityFileChangeListener) {
            return true;
        } else {
            return false;
        }
    }

    interface PriorityFileChangeListener extends FileChangeListener {}

    private static class ED extends FileSystem.EventDispatcher {
        private FCLSupport.Op op;
        private Enumeration<FileChangeListener> en;
        final private List<FileChangeListener> fsList;
        final private List<FileChangeListener> repList;
        
        
        private FileEvent fe;

        public ED(FCLSupport.Op op, Enumeration<FileChangeListener> en, FileEvent fe) {
            this.op = op;
            this.en = en;
            this.fe = fe;
            FileSystem fs = null;
            try {
                fs = this.fe.getFile().getFileSystem();
            } catch (FileStateInvalidException ex) {
                ExternalUtil.exception(ex);
            }
            ListenerList<FileChangeListener> fsll = (fs != null) ? fs.getFCLSupport().listeners : null;
            ListenerList<FileChangeListener> repll = (fs != null && fs.getRepository() != null) ? fs.getRepository().getFCLSupport().listeners : null;
            fsList = (fsll != null) ? new ArrayList<FileChangeListener>(fsll.getAllListeners()) : Collections.<FileChangeListener>emptyList();
            repList = (repll != null) ? new ArrayList<FileChangeListener>(repll.getAllListeners()) : Collections.<FileChangeListener>emptyList();
        }

        public ED(Enumeration<FileChangeListener> en, FileEvent fe) {
            this(null, en, fe);
        }

        /** @param onlyPriority if true then invokes only priority listeners
         *  else all listeners are invoked.
         */
        protected void dispatch(boolean onlyPriority, Collection<Runnable> postNotify) {
            if (this.op == null) {
                this.op = fe.getFile().isFolder() ? FCLSupport.Op.FOLDER_CREATED : FCLSupport.Op.DATA_CREATED;
            }

            LinkedList<FileChangeListener> newEnum = new LinkedList<FileChangeListener>(); // later lazy                

            while (en.hasMoreElements()) {
                FileChangeListener fcl = en.nextElement();

                if (onlyPriority && !isPriorityListener(fcl)) {
                    newEnum.add(fcl);

                    continue;
                }
                FCLSupport.dispatchEvent(fcl, fe, op, postNotify);
            }

            if (onlyPriority) {
                this.en = Collections.enumeration(newEnum);
            }

            /** FileEvents are forked in may cases. But FileEvents fired from
             * FileSystem and from Repository mustn`t be forked.
             */
            FileObject fo = fe.getFile();
            boolean transmit = false;            
            if (fo != null) {
                switch (op) {
                    case FILE_CHANGED:
                        transmit = fo.equals(fe.getSource());
                        break;
                    default:
                        transmit = !fo.equals(fe.getSource());
                        if (!transmit && fe instanceof Enumeration && !((Enumeration) fe).hasMoreElements()) {
                            transmit = true;
                        } 
                }
                
            }                

            if (!en.hasMoreElements() && transmit && !onlyPriority) {
                FileSystem fs = null;
                Repository rep = null;

                try {
                    fs = fe.getFile().getFileSystem();
                    rep = fs.getRepository();
                } catch (FileStateInvalidException fsix) {
                    return;
                }
                if (fs != null && fsList != null) {
                    for (FileChangeListener fcl : fsList) {
                        FCLSupport.dispatchEvent(fcl, fe, op, postNotify);
                    }  
                }


                if (rep != null && repList != null) {
                    for (FileChangeListener fcl : repList) {
                        FCLSupport.dispatchEvent(fcl, fe, op, postNotify);
                    }                      
                }
            }
        }

        protected void setAtomicActionLink(EventControl.AtomicActionLink propID) {
            fe.setAtomicActionLink(propID);
        }
    }

    /** Filters folders or data files.
     */
    private static final class OnlyFolders implements Enumerations.Processor<FileObject, FileObject> {
        private boolean folders;

        public OnlyFolders(boolean folders) {
            this.folders = folders;
        }

        public FileObject process(FileObject obj, Collection<FileObject> coll) {
            FileObject fo = obj;

            if (folders) {
                return fo.isFolder() ? fo : null;
            } else {
                return fo.isData() ? fo : null;
            }
        }
    }
     // end of OnlyFolders
}
