/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.versioning.util;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 * This class provides means for tight integration between VCS modules and the indexing
 * infrastructure.
 *
 * @author Vita Stejskal
 * @since 1.6
 */
public final class IndexingBridge {

    // -----------------------------------------------------------------------
    // Public implementation
    // -----------------------------------------------------------------------

    /**
     * Gets the signleton instance of <code>IndexingBridge</code>.
     *
     * @return The <code>IndexingBridge</code> instance.
     */
    public static synchronized IndexingBridge getInstance() {
        if (instance == null) {
            instance = new IndexingBridge();
        }
        return instance;
    }

    /**
     * Runs the <code>operation</code> without interfering with indexing. The indexing
     * is blocked while the operation is runing and all events that would normally trigger
     * reindexing will be processed after the operation is finished.
     *
     * @param operation The operation to run.
     * @return Whatever value is returned from the <code>operation</code>.
     *
     * @throws Exception Any exception thrown by the <code>operation</code> is going to be rethrown from
     *   this method.
     */
    public <T> T runWithoutIndexing(Callable<T> operation) throws Exception {
        IndexingBridgeProvider ibp = Lookup.getDefault().lookup(IndexingBridgeProvider.class);
        if (ibp != null) {
            return ibp.runWithoutIndexing(operation);
        } else {
            return operation.call();
        }
    }

    public void refreshFiles(File... files) {
        final Set<File> parents = new HashSet<File>();
        for (File f : files) {
            File parent = f.getParentFile();
            if (parent != null) {
                parents.add(parent);
                LOG.fine("scheduling for fs refresh: [" + parent + "]"); // NOI18N
            }
        }

        if (parents.size() > 0) {
            IndexingBridgeProvider ibp = Lookup.getDefault().lookup(IndexingBridgeProvider.class);
            if (ibp != null) {
                ibp.refreshFiles(parents.toArray(new File[parents.size()]));
            } else {
                // let's give the filesystem some time to wake up and to realize that the file has really changed
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        FileUtil.refreshFor(parents.toArray(new File[parents.size()]));
                    }
                }, 100); // XXX: getDelay()
            }
        }
    }
    
    /**
     * This interface is supposed to be implemented by the actual bridge module
     * that connect the versioning and indexing infrastructure. Ordinary VCS support
     * modules do not need to implement this interface.
     * <p>Implementations of this interface ought to be registered via {@link ServiceProvider} annotation
     * in the default <code>Lookup</code>.
     */
    public static interface IndexingBridgeProvider {
        /**
         * This method is te actual integration point between versioning and indexing.
         * A typical implementation should do something like this:
         * <ul>
         * <li>call the indexing infrastructure and turn off the immediate processing of events</li>
         * <li>call <code>operation.call()</code></li>
         * <li>call the indexing infrastructure to process all the events gatherd while</li>
         * the operation was running and turn on the immediate processing of events
         * </ul>
         *
         * @param operation The operation to run.
         * @return Whatever value is returned from the <code>operation</code>.
         *
         * @throws Exception Any exception thrown by the <code>operation</code> is going to rethrown from
         *   this method.
         */
        <T> T runWithoutIndexing(Callable<T> operation) throws Exception;

        void refreshFiles(File... files);
    }

    // -----------------------------------------------------------------------
    // private implementation
    // -----------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(IndexingBridge.class.getName());
    private static IndexingBridge instance = null;
// XXX: is this neccessary?
//    private static final int DEFAULT_DELAY = 100;
//    private int delayBeforeRefresh = -1;
    
    private IndexingBridge() {
        // no-op
    }

// XXX: is this neccessary?
//    private int getDelay() {
//        if (delayBeforeRefresh == -1) {
//            String delayProp = System.getProperty("subversion.SvnClientRefreshHandler.delay", Integer.toString(DEFAULT_DELAY)); //NOI18N
//            int delay = DEFAULT_DELAY;
//            try {
//                delay = Integer.parseInt(delayProp);
//            } catch (NumberFormatException e) {
//                LOG.log(Level.FINE, null, e);
//            }
//            delayBeforeRefresh = delay;
//        }
//        return delayBeforeRefresh;
//    }
}
