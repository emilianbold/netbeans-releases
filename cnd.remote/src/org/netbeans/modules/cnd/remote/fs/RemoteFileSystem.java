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

package org.netbeans.modules.cnd.remote.fs;

import java.io.File;
import org.netbeans.modules.cnd.makeproject.api.compilers.BasicCompiler;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 * Remote file system:
 * gets files on demand from a remote host.
 * It is read-only
 * 
 * @author Vladimir Kvashin
 */
public class RemoteFileSystem extends FileSystem {

    private static final SystemAction[] NO_SYSTEM_ACTIONS = new SystemAction[] {};

    private final ExecutionEnvironment execEnv;
    private final String filePrefix;
    private final RemoteFileObjectBase root;
    private final RemoteFileSupport remoteFileSupport;
    private final File cache;

    public RemoteFileSystem(ExecutionEnvironment execEnv) {
        assert execEnv.isRemote();
        this.execEnv = execEnv;
        this.remoteFileSupport = new RemoteFileSupport(execEnv);
        // FIXUP: it's better than asking a compiler instance... but still a fixup.
        // Should be moved to a proper place
        this.filePrefix = BasicCompiler.getIncludeFilePrefix(execEnv);
        cache = new File(filePrefix);
        cache.mkdirs(); // TODO: error processing
        this.root = new RootFileObject(this, execEnv, cache); // NOI18N
    }


    /*package-local, for testing*/
    File getCache() {
        return cache;
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
    public FileObject getRoot() {
        return root;
    }

    @Override
    public FileObject findResource(String name) {
        return getRoot().getFileObject(name);
    }

    @Override
    public SystemAction[] getActions() {
        return NO_SYSTEM_ACTIONS;
    }

    public RemoteFileSupport getRemoteFileSupport() {
        return remoteFileSupport;
    }

    private static class RootFileObject extends RemoteDirectory {

        public RootFileObject(RemoteFileSystem fileSystem, ExecutionEnvironment execEnv, File cache) {
            super(fileSystem, execEnv, "", cache);
        }

        @Override
        public boolean isRoot() {
            return false;
        }
    }
}
