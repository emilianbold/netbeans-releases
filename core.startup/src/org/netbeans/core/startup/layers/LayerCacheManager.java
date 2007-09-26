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

package org.netbeans.core.startup.layers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;
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
    static final Logger err = Logger.getLogger("org.netbeans.core.projects.cache"); // NOI18N
    
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
    public void store(FileSystem fs, List<URL> urls) throws IOException {
        throw new NotImplementedException();
    }
    
    /**
     * Save a new cache to disk, load it, and return that filesystem.
     * @param urls list of type URL; earlier layers can override later layers
     * @return a new filesystem with the specified contents
     * Not called if the manager supports loading;
     * otherwise must be overridden.
     */
    public FileSystem store(List<URL> urls) throws IOException {
        throw new NotImplementedException();
    }
    
}
