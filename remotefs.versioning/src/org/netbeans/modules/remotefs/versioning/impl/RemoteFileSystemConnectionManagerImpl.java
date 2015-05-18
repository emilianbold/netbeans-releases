/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.remotefs.versioning.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionListener;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.modules.remotefs.versioning.api.RemoteFileSystemConnectionListener;
import org.netbeans.modules.remotefs.versioning.api.RemoteFileSystemConnectionManager;
import org.openide.filesystems.FileSystem;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author alsimon
 */
@ServiceProvider(service=RemoteFileSystemConnectionManager.class)
public class RemoteFileSystemConnectionManagerImpl extends RemoteFileSystemConnectionManager implements ConnectionListener {
    private final Set<RemoteFileSystemConnectionListener> listeners = new HashSet<>();
    
    public RemoteFileSystemConnectionManagerImpl() {
        ConnectionManager.getInstance().addConnectionListener(this);
    }

    @Override
    public void addRemoteFileSystemConnectionListener(RemoteFileSystemConnectionListener listener) {
        synchronized(listeners) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeRemoteFileSystemConnectionListener(RemoteFileSystemConnectionListener listener) {
        synchronized(listeners) {
            listeners.remove(listener);
        }
    }

    @Override
    public boolean isConnectedRemoteFileSystem(FileSystem fs) {
        return true;
    }

    @Override
    public void connected(ExecutionEnvironment env) {
        final FileSystem fileSystem = FileSystemProvider.getFileSystem(env);
        if (fileSystem != null) {
            List<RemoteFileSystemConnectionListener> list = new ArrayList<>();
            synchronized(listeners) {
                list.addAll(listeners);
            }
            //TODO: post in request processor
            for(RemoteFileSystemConnectionListener listener : list) {
                listener.connected(fileSystem);
            }
        }
    }

    @Override
    public void disconnected(ExecutionEnvironment env) {
        final FileSystem fileSystem = FileSystemProvider.getFileSystem(env);
        if (fileSystem != null) {
            List<RemoteFileSystemConnectionListener> list = new ArrayList<>();
            synchronized(listeners) {
                list.addAll(listeners);
            }
            //TODO: post in request processor
            for(RemoteFileSystemConnectionListener listener : list) {
                listener.disconnected(fileSystem);
            }
        }
    }
}
