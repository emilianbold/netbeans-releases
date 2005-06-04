/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.startup.layers;

import java.io.File;
import java.io.IOException;
import java.util.List;

// WARNING: this package should not contain any non-platform openide/core imports!

import org.openide.ErrorManager;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.XMLFileSystem;
import org.openide.util.NotImplementedException;

/** Interface for a manager which can handle XML layer caching.
 * @see "#20168"
 * @author Jesse Glick
 */
public abstract class LayerCacheManager {

    /** Local error manager for in-package use.
     */
    static final ErrorManager err = ErrorManager.getDefault().getInstance("org.netbeans.core.projects.cache"); // NOI18N
    
    private final File cacheDir;
    
    private static LayerCacheManager emptyManager = null;
    /**
     * Get a cache manager which does nothing.
     */
    public static LayerCacheManager emptyManager() {
        if (emptyManager == null) {
            emptyManager = new NonCacheManager();
        }
        return emptyManager;
    }
    
    /** Create a cache manager (for subclass use).
     */
    protected LayerCacheManager(File cacheDir) {
        this.cacheDir = cacheDir;
    }
    
    /** The directory to use a working area for the cache.
     * For informational purposes only.
     * May be null, if the manager is not really caching.
     */
    public final File getCacheDirectory() {
        return cacheDir;
    }
    
    /** True if the cache already seems to exist in the cache dir, false if fresh.
     */
    public abstract boolean cacheExists();
    
    /** Clean up any cache files in the cache directory.
     * @see "#20997"
     */
    public abstract void cleanupCache() throws IOException;
    
    /**
     * If true, this manager supports in-place loading of a cache.
     * If false, it can only load a cache or store it.
     * The result affects which methods are considered effectively present and
     * abstract on this manager.
     * @return true by default
     */
    public boolean supportsLoad() {
        return true;
    }
    
    /** Create an empty cache filesystem, i.e. with no initial layers.
     * Should only be called when the cache directory is clean.
     * Should not be overridden if the manager does not support loading;
     * otherwise must be overridden.
     */
    public FileSystem createEmptyFileSystem() throws IOException {
        if (supportsLoad()) {
            throw new NotImplementedException();
        } else {
            return new XMLFileSystem();
        }
    }
    
    /** Create a preloaded cache filesystem with some existing set of layers.
     * Should only be called when the cache directory is prepared.
     * The default implementation just creates an empty cache and then
     * loads it, but subclasses may override to do this more efficiently.
     * (Subclasses which do not support loading <b>must</b> override it.)
     */
    public FileSystem createLoadedFileSystem() throws IOException {
        if (!supportsLoad()) throw new IOException("Does not support loading!"); // NOI18N
        FileSystem fs = createEmptyFileSystem();
        load(fs);
        return fs;
    }
    
    /** Load the cache from disk.
     * Should only be called when the cache directory is prepared.
     * The filesystem's contents should be modified.
     * The filesystem must have been originally produced by
     * {@link #createEmptyFileSystem} or {@link #createLoadedFileSystem}.
     * Not called if the manager does not support loading;
     * otherwise must be overridden.
     */
    public void load(FileSystem fs) throws IOException {
        throw new NotImplementedException();
    }
    
    /** Save a new cache to disk.
     * Besides modifying the disk cache files, this should also
     * change the contents of the existing filesystem.
     * The filesystem must have been originally produced by
     * {@link #createEmptyFileSystem} or {@link #createLoadedFileSystem}.
     * This might be done simply by calling {@link #load}, or there
     * might be a more efficient way.
     * @param urls list of type URL; earlier layers can override later layers
     * Not called if the manager does not support loading;
     * otherwise must be overridden.
     */
    public void store(FileSystem fs, List urls) throws IOException {
        throw new NotImplementedException();
    }
    
    /**
     * Save a new cache to disk, load it, and return that filesystem.
     * @param urls list of type URL; earlier layers can override later layers
     * @return a new filesystem with the specified contents
     * Not called if the manager supports loading;
     * otherwise must be overridden.
     */
    public FileSystem store(List urls) throws IOException {
        throw new NotImplementedException();
    }
    
}
