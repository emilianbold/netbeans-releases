/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.remote.impl.fs;

import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import org.netbeans.modules.nativeexecution.api.NativeProcessBuilder;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ProcessUtils;
import org.openide.util.Exceptions;

/**
 * Manages paths that are marked as "delete on exit".
 */
public class DeleteOnExitSupport {

    private final ExecutionEnvironment execEnv;
    private final File cache;

    /** If the ALLOW_ALTERNATIVE_DELETE_ON_EXIT is ON and transport does not support delete-on-exit, 
     *  then alternative delete-on-exit will work */
    private static final boolean ALLOW_ALTERNATIVE_DELETE_ON_EXIT = 
            RemoteFileSystemUtils.getBoolean("remote.alternative.delete.on.exit", true);

    private static final String DELETE_ON_EXIT_FILE_NAME = ".rfs_delete_on_exit"; // NOI18N

    private static final Object lock = new Object();
    
    // The idea about filesToDelete and filesToRemember is as follows:
    // When a file is marked as "delete on exit", it is added to filesToRemember
    // When a disconnect occurs, we move all files from filesToRemember into filesToDelete
    // (and also store them on disk)
    // When connnect occurs, filesToDelete are deleted.
    // This prevents sync issues 
    
    /** guarded by lock */
    private final LinkedHashSet<String> filesToDelete = new LinkedHashSet<>();

    /** guarded by lock */
    private final LinkedHashSet<String> filesToRemember = new LinkedHashSet<>();

    
    public DeleteOnExitSupport(ExecutionEnvironment execEnv, File cacheRoot) {
        this.execEnv = execEnv;
        this.cache = new File(cacheRoot, DELETE_ON_EXIT_FILE_NAME);
        if (ALLOW_ALTERNATIVE_DELETE_ON_EXIT) {
            synchronized (lock) {
                loadDeleteOnExit(cache, filesToDelete);
            }
        }
    }
    
    /** Called directly from ConnectionListener.connected */
    public void notifyConnected() {        
    }

    /** Called directly from ConnectionListener.disconnected */
    public void notifyDisconnected() {        
        if (ALLOW_ALTERNATIVE_DELETE_ON_EXIT) {
            List<String> paths;
            synchronized (lock) {
                filesToDelete.addAll(filesToRemember);
                filesToRemember.clear();
                paths = new ArrayList<>(filesToDelete);
            }
            storeDeleteOnExit(cache, paths);
        }
    }
    /**
     * Is called from the request processor 
     * in reaction on connect OR disconnect
     */
    public void processConnectionChange() {
        if (ALLOW_ALTERNATIVE_DELETE_ON_EXIT) {
            if (ConnectionManager.getInstance().isConnectedTo(execEnv)) {
                List<String> paths;
                synchronized (lock) {
                    paths = new ArrayList<>(filesToDelete);
                    filesToDelete.clear();
                }
                if (!paths.isEmpty()) {
                    deleteImpl(execEnv, paths);
                }
            }
        }
    }
    
    public void deleteOnExit(String... paths) {
        if (ALLOW_ALTERNATIVE_DELETE_ON_EXIT) {
            synchronized (lock) {
                for (String p : paths) {
                    filesToRemember.add(p);
                }
            }
        }
    }

    private static void deleteImpl(ExecutionEnvironment execEnv, Collection<String> paths) {
        assert ALLOW_ALTERNATIVE_DELETE_ON_EXIT;
        if (paths.isEmpty()) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (String p : paths) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(p);
        }
        if (!ConnectionManager.getInstance().isConnectedTo(execEnv)) {
            return;
        }
        ProcessUtils.execute(NativeProcessBuilder.newProcessBuilder(execEnv).setExecutable("xargs").setArguments("rm"), sb.toString().getBytes()); // NOI18N

    }

    private static void storeDeleteOnExit(File file, Collection<String> paths) {
        assert ALLOW_ALTERNATIVE_DELETE_ON_EXIT;
        // the existence of cache root ensured in ctor
        try (PrintWriter pw = new PrintWriter(file, "UTF8")) { // NOI18N
            if (!paths.isEmpty()) {
                for (String path : paths) {
                    pw.append(path).append('\n');
                }
                pw.close();
            }
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            Exceptions.printStackTrace(ex); // should never occur
        }
    }

    private static void loadDeleteOnExit(File file, Collection<String> pathsToAdd) {
        assert ALLOW_ALTERNATIVE_DELETE_ON_EXIT;
        // the existence of cache root ensured in ctor
        // this is called from ctor only, so it's OK to do file ops in sync block
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            for (String path; (path = br.readLine()) != null;) {
                if (!path.isEmpty()) {
                    pathsToAdd.add(path);
                }
            }
            // line is not visible here.
        } catch (FileNotFoundException ex) {
            // nothing to do: no file is quite normal
        } catch (IOException ex) {
            ex.printStackTrace(System.err);
        }
    }
}
