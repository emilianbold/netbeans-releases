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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.remote.fs;

import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Vladimir Kvashin
 */
@ServiceProvider(service=org.netbeans.modules.remote.spi.FileSystemProvider.class, position=50)
public class FileSystemProviderImpl extends org.netbeans.modules.remote.spi.FileSystemProvider {

    @Override
    protected FileSystem getFileSystemImpl(ExecutionEnvironment env, String root) {
        return RemoteFileSystemManager.getInstance().get(env);
    }

    @Override
    protected String normalizeAbsolutePathImpl(String absPath, ExecutionEnvironment env) {
        return RemoteFileSystemManager.getInstance().get(env).normalizeAbsolutePath(absPath);
    }

    @Override
    protected  FileObject normalizeFileObjectImpl(FileObject fileObject) {
        if (fileObject instanceof RemoteFileObjectBase) {
            return fileObject;
        }
        return null;
    }

    @Override
    protected FileObject getFileObjectImpl(FileObject baseFileObject, String relativeOrAbsolutePath) {
        if (baseFileObject instanceof RemoteFileObjectBase) {
            ExecutionEnvironment execEnv = ((RemoteFileObjectBase) baseFileObject).getExecutionEnvironment();
            if (CndPathUtilitities.isPathAbsolute(relativeOrAbsolutePath)) {
                relativeOrAbsolutePath = RemoteFileSystemManager.getInstance().get(execEnv).normalizeAbsolutePath(relativeOrAbsolutePath);
                try {
                    baseFileObject.getFileSystem().findResource(relativeOrAbsolutePath);
                } catch (FileStateInvalidException ex) {
                    Exceptions.printStackTrace(ex);
                }
            } else {
                // it's RemoteDirectory responsibility to normalize in this case
                baseFileObject.getFileObject(relativeOrAbsolutePath);
            }
        }
        return null;
    }



}
