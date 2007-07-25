/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.filesystems;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import org.openide.util.Enumerations;
import org.openide.util.NbCollections;
import org.openide.util.actions.SystemAction;

/**
 * General base class for filesystems which proxy to others.
 *
 * <p>This filesystem has no form of storage in and of itself. Rather, it composes and proxies one or more
 * "delegate" filesystems. The result is that of a "layered" sandwich of filesystems, each able to provide files
 * to appear in the merged result. The layers are ordered so that a filesystem in "front" can override one in
 * "back". Often the frontmost layer will be writable, and all changes to the filesystem are sent to this layer,
 * but that behavior is configurable.
 *
 * <p>Creating a new <code>MultiFileSystem</code> is easy in the simplest cases: just call {@link
 * #MultiFileSystem(FileSystem[])} and pass a list of delegates. If you pass it only read-only delegates, the
 * composite will also be read-only. Or you may pass it one or more writable filesystems (make sure the first
 * file system is one of them); then it will be able to act as a writable file system, by default storing all
 * actual changes on the first file system.
 *
 * <p>This class is intended to be subclassed more than to be used as is, since typically a user of it will want
 * finer-grain control over several aspects of its operation. When subclassed, delegates may be {@link
 * #setDelegates changed dynamically} and the "contents" of the composite filesystem will
 * automatically be updated.
 *
 * <p>One of the more common methods to override is {@link #createWritableOn}, which should provide the delegate
 * filesystem which should be used to store changes to the indicated file (represented as a resource path).
 * Normally, this is hardcoded to provide the first filesystem (assuming it is writable); subclasses may store
 * changes on another filesystem, or they may select a filesystem to store changes on according to (for example)
 * file extension. This could be used to separate source from compiled/deployable files, for instance. Or some
 * filesystems may prefer to attempt to make changes in whatever filesystem provided the original file, assuming
 * it is writable; this is also possible, getting information about the file's origin from {@link #findSystem}.
 *
 * <p>Another likely candidate for overriding is {@link #findResourceOn}. Normally this method just looks up
 * resources by the normal means on delegate filesystems and provides the resultant file objects. However it
 * could be customized to "warp" the delegates somehow; for example, selecting a different virtual root for one
 * of them.
 *
 * <p>{@link #notifyMigration} provides a means for subclasses to update some state (for example, visual
 * annotations) when the filesystem used to produce a given file changes dynamically. This most often happens
 * when a file provided by a back delegate is written to, and the result stored on the foremost delegate, but it
 * could occur in other situations too (e.g. change of delegates, removal of overriding file on underlying front
 * delegate, etc.).
 *
 * <p>When new files are added to the <code>MultiFileSystem</code>, normally they will be physically added to
 * the foremost delegate, although as previously mentioned they can be customized. When their contents (or
 * attributes) are changed, by default these changes are again made in the foremost delegate. But what if a file
 * is deleted? There must be a way to indicate on the foremost delegate that it should not appear in the
 * composite (even while it remains in one of the delegates). For this reason, <code>MultiFileSystem</code> uses
 * "masks" which are simply empty files named according to the file they should hide, but with the suffix
 * <samp>_hidden</samp>. Thus, for example, if there is a file <samp>subdir/readme.txt_hidden</samp> in a front
 * delegate it will hide any files named <samp>subdir/readme.txt</samp> in delegates further back. These masks
 * are automatically created as needed when files are "deleted" from <code>MultiFileSystem</code>; or delegate
 * filesystems may explicitly provide them. Normally the mask files are not themselves visible as {@link
 * FileObject}s, since they are an artifact of the deletion logic. However, when nesting
 * <code>MultiFileSystem</code>s inside other <code>MultiFileSystem</code>s, it can be useful to have delegates
 * be able to mask files not provided by their immediate siblings, but by cousins. For this reason, nested
 * subclasses may call {@link #setPropagateMasks} to make the mask files propagate up one or more nesting levels
 * and thus remain potent against cousin delegates.
 */
public class MultiFileSystem extends FileSystem {
    static final long serialVersionUID = -767493828111559560L;

    /** what extension to add to file that mask another ones */
    static final String MASK = "_hidden"; // NOI18N

    /** index of the filesystem with write access */
    private static final int WRITE_SYSTEM_INDEX = 0;

    /** array of fs. the filesystem at position 0 can be null, because
    * it is writable filesystem. Others are only for read access
    */
    private FileSystem[] systems;

    /** @see #getPropagateMasks */
    private boolean propagateMasks = false;

    /** root */
    private transient MultiFileObject root;

    /** Creates new empty MultiFileSystem. Useful only for
    * subclasses.
    */
    protected MultiFileSystem() {
        this(new FileSystem[0]);
    }

    /** Creates new MultiFileSystem.
    * @param fileSystems array of filesystems (can contain nulls)
    */
    public MultiFileSystem(FileSystem[] fileSystems) {
        this.systems = fileSystems.clone();
    }

    /**
     * Actually implements contract of FileSystem.refresh().
     */
    public @Override void refresh(boolean expected) {
        Enumeration<AbstractFolder> en = getMultiRoot().existingSubFiles(true);

        while (en.hasMoreElements()) {
            FileObject fo = en.nextElement();
            fo.refresh(expected);
        }
    }

    /** Changes the filesystems that this system delegates to
    *
    * @param fileSystems array of filesystems
    */
    protected final void setDelegates(FileSystem... fileSystems) {
        if (Arrays.equals(fileSystems, systems)) {
            return;
        }
        MultiFileObject.freeAllAttribCaches();
        // save for notification
        FileSystem[] oldSystems = systems;

        // set them
        this.systems = fileSystems;

        getMultiRoot().updateAllAfterSetDelegates(oldSystems);

        List<FileSystem> oldList = Arrays.asList(oldSystems);
        List<FileSystem> newList = Arrays.asList(systems);

        // notify removed filesystems
        Set<FileSystem> toRemove = new HashSet<FileSystem>(oldList);
        toRemove.removeAll(newList);

        for (FileSystem fs : toRemove) {
            if (fs != null) {
                fs.removeNotify();
            }
        }

        // notify added filesystems
        Set<FileSystem> toAdd = new HashSet<FileSystem>(newList);
        toAdd.removeAll(oldList);

        for (FileSystem fs : toAdd) {
            if (fs != null) {
                fs.addNotify();
            }
        }
    }

    /** All filesystem that this system delegates to.
    * @return the array of delegates
    */
    protected final FileSystem[] getDelegates() {
        return systems;
    }

    /** Will mask files that are not used be listed as children?
     * @return <code>true</code> if so
     */
    public final boolean getPropagateMasks() {
        return propagateMasks;
    }

    /** Set whether unused mask files should be listed as children.
     * @param pm <code>true</code> if so
     */
    protected final void setPropagateMasks(boolean pm) {
        propagateMasks = pm;
    }

    /** This filesystem is readonly if it has not writable system.
    */
    public boolean isReadOnly() {
        return WRITE_SYSTEM_INDEX >= systems.length || systems[WRITE_SYSTEM_INDEX] == null || systems[WRITE_SYSTEM_INDEX].isReadOnly();
    }

    /** The name of the filesystem.
    */
    public String getDisplayName() {
        return getString("CTL_MultiFileSystem");
    }

    /** Root of the filesystem.
    */
    public FileObject getRoot() {
        return getMultiRoot();
    }

    /**
     * @return the root of the filesystem
     */
    private MultiFileObject getMultiRoot() {
        MultiFileObject retval = null;
        synchronized (MultiFileSystem.class) {
            if (root != null) {
                retval = root;
            }            
        }
        
        if (retval == null) {
            retval = new MultiFileObject(this);
        }
        
        synchronized (MultiFileSystem.class) {
            if (root == null) {
                root = retval;
            }            
        }
                
        return root;
    }

    /** Merge actions from all delegates.
    */
    public @Override SystemAction[] getActions() {
        List<SystemAction> al = new ArrayList<SystemAction>(101); // randomly choosen constant
        Set<SystemAction> uniq = new HashSet<SystemAction>(101); // not that randommly choosen

        FileSystem[] del = this.getDelegates();

        for (int i = 0; i < del.length; i++) {
            if (del[i] == null) {
                continue;
            }

            SystemAction[] acts = del[i].getActions();

            for (int j = 0; j < acts.length; j++) {
                if (uniq.add(acts[j])) {
                    al.add(acts[j]);
                }
            }
        }

        return al.toArray(new SystemAction[al.size()]);
    }

    public @Override SystemAction[] getActions(final Set<FileObject> foSet) {
        List<SystemAction> al = new ArrayList<SystemAction>(101); // randomly choosen constant
        Set<SystemAction> uniq = new HashSet<SystemAction>(101); // not that randommly choosen

        final FileSystem[] del = this.getDelegates();

        for (int i = 0; i < del.length; i++) {
            if (del[i] == null) {
                continue;
            }

            final SystemAction[] acts = del[i].getActions(foSet);

            for (int j = 0; j < acts.length; j++) {
                if (uniq.add(acts[j])) {
                    al.add(acts[j]);
                }
            }
        }

        return al.toArray(new SystemAction[al.size()]);
    }

    @Deprecated // have to override for compat
    public @Override FileObject find(String aPackage, String name, String ext) {
        // create enumeration of name to look for
        Enumeration<String> st = NbCollections.checkedEnumerationByFilter(new StringTokenizer(aPackage, "."), String.class, true); // NOI18N
        Enumeration<String> en;

        if ((name == null) || (ext == null)) {
            en = st;
        } else {
            en = Enumerations.concat(st, Enumerations.singleton(name + '.' + ext));
        }

        // tries to find it (can return null)
        return getMultiRoot().find(en);
    }

    /* Finds file when its resource name is given.
    * The name has the usual format for the {@link ClassLoader#getResource(String)}
    * method. So it may consist of "package1/package2/filename.ext".
    * If there is no package, it may consist only of "filename.ext".
    *
    * @param name resource name
    *
    * @return FileObject that represents file with given name or
    *   <CODE>null</CODE> if the file does not exist
    */
    public FileObject findResource(String name) {
        if (name.length() == 0) {
            return getMultiRoot();
        } else {
            Enumeration<String> tok = NbCollections.checkedEnumerationByFilter(new StringTokenizer(name, "/"), String.class, true); // NOI18N
            return getMultiRoot().find(tok);
        }
    }

    //
    // Helper methods for subclasses
    //

    /** For given file object finds the filesystem that the object is placed on.
    * The object must be created by this filesystem orherwise IllegalArgumentException
    * is thrown.
    *
    * @param fo file object
    * @return the filesystem (from the list we delegate to) the object has file on
    * @exception IllegalArgumentException if the file object is not represented in this filesystem
    */
    protected final FileSystem findSystem(FileObject fo)
    throws IllegalArgumentException {
        try {
            if (fo instanceof MultiFileObject) {
                MultiFileObject mfo = (MultiFileObject) fo;

                return mfo.getLeaderFileSystem();
            }
        } catch (FileStateInvalidException ex) {
            // can happen if there is no delegate, I do not know what to return
            // better, but we should not throw the exception
            return this;
        }

        throw new IllegalArgumentException(fo.getPath());
    }

    /** Marks a resource as hidden. It will not be listed in the list of files.
    * Uses createMaskOn method to determine on which filesystem to mark the file.
    *
    * @param res resource name of file to hide or show
    * @param hide true if we should hide the file/false otherwise
    * @exception IOException if it is not possible
    */
    protected final void hideResource(String res, boolean hide)
    throws IOException {
        if (hide) {
            // mask file
            maskFile(createWritableOn(res), res);
        } else {
            unmaskFile(createWritableOn(res), res);
        }
    }

    /** Finds all hidden files on given filesystem. The methods scans all files for
    * ones with hidden extension and returns enumeration of names of files
    * that are hidden.
    *
    * @param folder folder to start at
    * @param rec proceed recursivelly
    * @return enumeration of String with names of hidden files
    */
    protected static Enumeration<String> hiddenFiles(FileObject folder, boolean rec) {
        Enumeration<? extends FileObject> allFiles = folder.getChildren(rec);

        class OnlyHidden implements Enumerations.Processor<FileObject, String> {
            public String process(FileObject obj, Collection<FileObject> ignore) {
                String sf = obj.getPath();

                if (sf.endsWith(MASK)) {
                    return sf.substring(0, sf.length() - MASK.length());
                } else {
                    return null;
                }
            }
        }

        return Enumerations.filter(allFiles, new OnlyHidden());
    }

    //
    // methods for subclass customization
    //

    /** Finds a resource on given filesystem. The default
    * implementation simply uses FileSystem.findResource, but
    * subclasses may override this method to hide/show some
    * resources.
    *
    * @param fs the filesystem to scan on
    * @param res the resource name to look for
    * @return the file object or null
    */
    protected FileObject findResourceOn(FileSystem fs, String res) {
        return fs.findResource(res);
    }

    /** Finds the system to create writable version of the file on.
    *
    * @param name name of the file (full)
    * @return the first one
    * @exception IOException if the filesystem is readonly
    */
    protected FileSystem createWritableOn(String name)
    throws IOException {
        if (isReadOnly()) {
            FSException.io("EXC_FSisRO", getDisplayName()); // NOI18N
        }

        return systems[WRITE_SYSTEM_INDEX];
    }

    /** Special case of createWritableOn (@see #createWritableOn).
    *
    * @param oldName original name of the file (full)
    * @param newName name new of the file (full)
    * @return the first one
    * @exception IOException if the filesystem is readonly
    * @since 1.34
    */
    protected FileSystem createWritableOnForRename(String oldName, String newName)
    throws IOException {
        return createWritableOn(newName);
    }

    /** When a file is about to be locked this method is consulted to
    * choose which delegates should be locked. By default this method
    * returns only one filesystem; the same returned by createWritableOn.
    * <P>
    * If an delegate resides on a filesystem returned in the resulting
    * set, it will be locked. All others will remain unlocked.
    *
    * @param name the resource name to lock
    * @return set of filesystems
    * @exception IOException if the resource cannot be locked
    */
    protected Set createLocksOn(String name) throws IOException {
        FileSystem writable = createWritableOn(name);

        return Collections.singleton(writable);
    }

    /** Notification that a file has migrated from one filesystem
    * to another. Usually when somebody writes to file on readonly file
    * system and the file has to be copied to write one.
    * <P>
    * This method allows subclasses to fire for example FileSystem.PROP_STATUS
    * change to notify that annotation of this file should change.
    *
    * @param fo file object that change its actual filesystem
    */
    protected void notifyMigration(FileObject fo) {
    }

    /** Notification that a file has been marked unimportant.
    *
    *
    * @param fo file object that change its actual filesystem
    */
    protected void markUnimportant(FileObject fo) {
    }

    /** Lets any sub filesystems prepare the environment.
     * If they do not support it, it does not care.
     * @deprecated Useless.
     */
    @Deprecated
    public @Override void prepareEnvironment(FileSystem.Environment env)
    throws EnvironmentNotSupportedException {
        FileSystem[] layers = getDelegates();

        for (int i = 0; i < layers.length; i++) {
            if (layers[i] != null) {
                try {
                    layers[i].prepareEnvironment(env);
                } catch (EnvironmentNotSupportedException ense) {
                    // Fine.
                }
            }
        }
    }

    /** Notifies all encapsulated filesystems in advance
    * to superclass behaviour. */
    public @Override void addNotify() {
        super.addNotify();

        for (int i = 0; i < systems.length; i++) {
            if (systems[i] != null) {
                systems[i].addNotify();
            }
        }
    }

    /** Notifies all encapsulated filesystems in advance
    * to superclass behaviour. */
    public @Override void removeNotify() {
        super.removeNotify();

        for (int i = 0; i < systems.length; i++) {
            if (systems[i] != null) {
                systems[i].removeNotify();
            }
        }
    }

    //
    // Private methods
    //

    /** Computes a list of FileObjects in the right order
    * that can represent this instance.
    *
    * @param name of resource to find
    * @return enumeration of FileObject
    */
    Enumeration<FileObject> delegates(final String name) {
        Enumeration<FileSystem> en = Enumerations.array(systems);

        class Resources implements Enumerations.Processor<FileSystem, FileObject> {
            public FileObject process(FileSystem fs, Collection<FileSystem> ignore) {
                if (fs == null) {
                    return null;
                } else {
                    return findResourceOn(fs, name);
                }
            }
        }

        return Enumerations.filter(en, new Resources());
    }

    /** Creates a file object that will mask the given file.
    * @param fs filesystem to work on
    * @param res resource name of the file
    * @exception IOException if it fails
    */
    void maskFile(FileSystem fs, String res) throws IOException {
        FileObject where = findResourceOn(fs, fs.getRoot().getPath());
        FileUtil.createData(where, res + MASK);
    }

    /** Deletes a file object that will mask the given file.
    * @param fs filesystem to work on
    * @param res resource name of the file
    * @exception IOException if it fails
    */
    void unmaskFile(FileSystem fs, String res) throws IOException {
        FileObject fo = findResourceOn(fs, res + MASK);

        if (fo != null) {
            FileLock lock = fo.lock();

            try {
                fo.delete(lock);
            } finally {
                lock.releaseLock();
            }
        }
    }

    /** Deletes a all mask files  that  mask the given file. All
    * higher levels then fs are checked and mask is deleted if necessary
    * @param fs filesystem where res is placed
    * @param res resource name of the file that should be unmasked
    * @exception IOException if it fails
    */
    void unmaskFileOnAll(FileSystem fs, String res) throws IOException {
        FileSystem[] fss = this.getDelegates();

        for (int i = 0; i < fss.length; i++) {
            if ((fss[i] == null) || fss[i].isReadOnly()) {
                continue;
            }

            unmaskFile(fss[i], res);

            /** unamsk on all higher levels, which mask files on fs-layer */
            if (fss[i] == fs) {
                return;
            }
        }
    }

    static boolean isMaskFile(FileObject fo) {
        return fo.getExt().endsWith(MASK);
    }
}
