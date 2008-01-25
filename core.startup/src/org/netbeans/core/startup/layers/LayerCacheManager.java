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

import java.beans.PropertyVetoException;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
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
    
    private static LayerCacheManager mgr = new BinaryCacheManager();
    private static LayerCacheManager non = new NonCacheManager();
    /**
     * Get a cache manager which does nothing.
     */
    public static LayerCacheManager manager(boolean real) {
        return real ? mgr : non;
    }
    
    /** Create a cache manager (for subclass use).
     */
    protected LayerCacheManager() {
    }
    
    /** Create an empty cache filesystem, i.e. with no initial layers.
     * Should only be called when the cache directory is clean.
     * Should not be overridden if the manager does not support loading;
     * otherwise must be overridden.
     */
    public abstract FileSystem createEmptyFileSystem() throws IOException;
    
    /** Load the cache from disk.
     * Should only be called when the cache directory is prepared.
     * The filesystem's contents should be modified.
     * The filesystem must have been originally produced by
     * {@link #createEmptyFileSystem} or {@link #createLoadedFileSystem}.
     * Not called if the manager does not support loading;
     * otherwise must be overridden.
     */
    public abstract FileSystem load(FileSystem previous, ByteBuffer bb) throws IOException;
    
    /**
     * Save a new cache to disk, load it, and return that filesystem.
     * @param urls list of type URL; earlier layers can override later layers
     * @return a new filesystem with the specified contents
     * Not called if the manager supports loading;
     * otherwise must be overridden.
     */
    public abstract void store(FileSystem fs, List<URL> urls, OutputStream os) throws IOException;
    
    /** Location of cache.
     * 
     * @return path to cache
     */
    public abstract String cacheLocation();
    
    private static final class NonCacheManager extends LayerCacheManager {
        @Override
        public FileSystem createEmptyFileSystem() throws IOException {
            return new XMLFileSystem();
        }

        @Override
        public FileSystem load(FileSystem previous, ByteBuffer bb) throws IOException {
            byte[] arr = new byte[bb.limit()];
            bb.get(arr);
            DataInputStream is = new DataInputStream(new ByteArrayInputStream(arr));
            List<URL> urls = new ArrayList<URL>();
            while (is.available() > 0) {
                String u = is.readUTF();
                urls.add(new URL(u));
            }
            try {
                XMLFileSystem fs = (XMLFileSystem)previous;
                fs.setXmlUrls(urls.toArray(new URL[urls.size()]));
                return fs;
            } catch (PropertyVetoException pve) {
                throw (IOException) new IOException(pve.toString()).initCause(pve);
            }
        }

        @Override
        public void store(FileSystem fs, List<URL> urls, OutputStream os) throws IOException {
            DataOutputStream data = new DataOutputStream(os);
            for (URL u : urls) {
                data.writeUTF(u.toExternalForm());
            }
            data.close();
        }

        @Override
        public String cacheLocation() {
            return "all-local-layers.dat"; // NOI18N
        }
    } // end of NonCacheManager
}
