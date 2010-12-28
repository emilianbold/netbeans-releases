/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
package org.netbeans.modules.remote.impl.fs;

import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.io.File;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.SwingUtilities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.remote.spi.FileSystemCacheProvider;
import org.netbeans.modules.remote.support.RemoteLogger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.windows.WindowManager;

/**
 * Remote file system:
 * gets files on demand from a remote host.
 * It is read-only
 * 
 * @author Vladimir Kvashin
 */
@org.netbeans.api.annotations.common.SuppressWarnings("Se") // is it ever serialized?
public class RemoteFileSystem extends FileSystem {

    private static final SystemAction[] NO_SYSTEM_ACTIONS = new SystemAction[]{};
    private final ExecutionEnvironment execEnv;
    private final String filePrefix;
    private final RootFileObject root;
    private final RemoteFileSupport remoteFileSupport;
    private final File cache;
    private final RemoteFileObjectFactory factory;
    private long dirtyTimestamp;
    /** File transfer statistics */
    private static int fileCopyCount;
    /** Directory synchronization statistics */
    private static int dirSyncCount;
    private static final Object mainLock = new Object();
    private static final Map<File, WeakReference<ReadWriteLock>> locks = new HashMap<File, WeakReference<ReadWriteLock>>();
    private static Reference<Map<String, String>> normalizedRef = new SoftReference<Map<String, String>>(new ConcurrentHashMap<String, String>());

    private static Map<String, String> getNormalizedFilesMap() {
        Map<String, String> map = normalizedRef.get();
        synchronized (RemoteFileSystem.class) {
            map = normalizedRef.get();
            if (map == null) {
                map = new ConcurrentHashMap<String, String>();
                normalizedRef = new SoftReference<Map<String, String>>(map);
            }
        }
        return map;
    }

    public RemoteFileSystem(ExecutionEnvironment execEnv) throws IOException {
        RemoteLogger.assertTrue(execEnv.isRemote());
        this.execEnv = execEnv;
        this.remoteFileSupport = new RemoteFileSupport(execEnv);
        factory = new RemoteFileObjectFactory(this);
        // FIXUP: it's better than asking a compiler instance... but still a fixup.
        // Should be moved to a proper place
        this.filePrefix = FileSystemCacheProvider.getCacheRoot(execEnv);
        cache = new File(filePrefix);
        if (!cache.exists() && !cache.mkdirs()) {
            throw new IOException(NbBundle.getMessage(getClass(), "ERR_CreateDir", cache.getAbsolutePath()));
        }
        this.root = new RootFileObject(this, execEnv, cache); // NOI18N

        final WindowFocusListener windowFocusListener = new WindowFocusListener() {

            public void windowGainedFocus(WindowEvent e) {
                resetDirtyTimestamp();
            }

            public void windowLostFocus(WindowEvent e) {
            }
        };
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                //WindowManager.getDefault().getMainWindow().addWindowFocusListener(focusListener);
                WindowManager.getDefault().getMainWindow().addWindowFocusListener(windowFocusListener);
            }
        });
        resetDirtyTimestamp();
    }

    /*package for test needs*/ void testResetDirtyTimestamp() {
        resetDirtyTimestamp();
    }

    private void resetDirtyTimestamp() {
        cache.setLastModified(System.currentTimeMillis());
        dirtyTimestamp = cache.lastModified(); // otherwise we can't compare it with files - we can easily get a tiny difference...
        RemoteLogger.getInstance().log(Level.FINEST, "Sync: resetting dirty timestamp for {0} to {1}", new Object[]{execEnv, dirtyTimestamp});
    }

    /*package*/
    public long getDirtyTimestamp() {
        return dirtyTimestamp;
    }

    /*package*/ ExecutionEnvironment getExecutionEnvironment() {
        return execEnv;
    }

    public RemoteFileObjectFactory getFactory() {
        return factory;
    }

    public String normalizeAbsolutePath(String absPath) {
        //BZ#192265 as vkvashin stated the URI i sused to normilize the path
        //but URI is really very restrictive so let's use another way
        //will use the face that path is absolute and we have Unix like system
        //no special code for Windows
        return normalize(absPath);

    }

    private static String normalize(String absPath){
        //BZ#192265 as vkvashin stated the URI i sused to normilize the path
        //but URI is really very restrictive so let's use another way
        //will use the face that path is absolute and we have Unix like system
        //no special code for Windows
        //also as absolute path is passed to the method we will use it as an absolute
        String result = absPath;
// # Remove all /./ sequences.
//    local   path=${1//\/.\//\/}
        result = result.replaceAll("[/][.][/]", "[/]"); // NOI18N

//
//    # Remove first dir/.. sequence.
//    local   npath=$(echo $path | sed -e 's;[^/][^/]*/\.\./;;')
        if (result.startsWith("..")){ // NOI18N
            result = result.replaceFirst("..", ""); // NOI18N
        }
//    # Remove remaining dir/.. sequence.
//    while [[ $npath != $path ]]
//    do
//        path=$npath
//        npath=$(echo $path | sed -e 's;[^/][^/]*/\.\./;;')
//    done
//    echo $path
        Pattern p = Pattern.compile(".*[/]([^/]+)[/][.][.].*"); // NOI18N
        Matcher m = p.matcher(result);
        if (m.matches()){
            result = result.replaceAll("[/][^/]+[/][.][.]", ""); // NOI18N
        }
        return result;

    }

    /*package-local, for testing*/
    File getCache() {
        return cache;
    }

    public static ReadWriteLock getLock(File file) {
        synchronized (mainLock) {
            WeakReference<ReadWriteLock> ref = locks.get(file);
            ReadWriteLock result = (ref == null) ? null : ref.get();
            if (result == null) {
                result = new ReentrantReadWriteLock();
                locks.put(file, new WeakReference<ReadWriteLock>(result));
            }
            return result;
        }
    }

    /*package-local test method*/ final void resetStatistic() {
        dirSyncCount = 0;
        fileCopyCount = 0;
    }

    /*package-local test method*/ final int getDirSyncCount() {
        return dirSyncCount;
    }

    /*package-local test method*/ final int getFileCopyCount() {
        return fileCopyCount;
    }

    /*package-local test method*/ final void incrementDirSyncCount() {
        dirSyncCount++;
    }

    /*package-local test method*/ final void incrementFileCopyCount() {
        fileCopyCount++;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(getClass(), "RFS_DISPLAY_NAME", execEnv.getDisplayName());
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public RemoteDirectory getRoot() {
        return root;
    }

    @Override
    public RemoteFileObjectBase findResource(String name) {
        if (name.isEmpty() || name.equals("/")) {  // NOI18N
            return getRoot();
        } else {
            return getRoot().getFileObject(name);
        }
    }

    @Override
    public SystemAction[] getActions() {
        return NO_SYSTEM_ACTIONS;
    }

    public RemoteFileSupport getRemoteFileSupport() {
        return remoteFileSupport;
    }

    @Override
    public String toString() {
        return getDisplayName();
    }

    private static class RootFileObject extends RemoteDirectory {

        public RootFileObject(RemoteFileSystem fileSystem, ExecutionEnvironment execEnv, File cache) {
            super(fileSystem, execEnv, null, "", cache);
        }

        @Override
        public boolean isRoot() {
            return true;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public RemoteDirectory getParent() {
            return null;
        }

        @Override
        protected void refreshImpl(boolean recursive) {
            getFileSystem().resetDirtyTimestamp();
        }
    }
}
