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
package org.netbeans.modules.cnd.apt.impl.support.clank;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import org.clang.basic.vfs.FileSystem;
import org.clang.tools.services.ClankCompilationDataBase;
import org.clang.tools.services.support.ClangFileSystemProvider;
import org.llvm.adt.IntrusiveRefCntPtr;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.spi.utils.CndFileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

/**
 *
 * @author vkvashin
 */
public class ClankFileSystemProviderImpl extends ClangFileSystemProvider {
    

    ConcurrentHashMap<org.openide.filesystems.FileSystem, ClankFileObjectBasedFileSystem> fileSystems 
            = new ConcurrentHashMap<org.openide.filesystems.FileSystem, ClankFileObjectBasedFileSystem>();
    
    @Override
    public IntrusiveRefCntPtr<FileSystem> getFileSystemImpl(ClankCompilationDataBase.Entry entry) {
        Collection<URI> compiledFiles = entry.getCompiledFiles();
        if (compiledFiles != null && !compiledFiles.isEmpty()) {
            URI uri = compiledFiles.iterator().next();
            URL url;
            try {
                url = uri.toURL();
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
            FileObject fo = URLMapper.findFileObject(url);
            org.openide.filesystems.FileSystem nbFS;
            try {
                nbFS = fo.getFileSystem();
            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
            if (CndFileSystemProvider.isRemote(nbFS) || APTTraceFlags.ALWAYS_USE_NB_FS) {
                ClankFileObjectBasedFileSystem clankFS = fileSystems.get(nbFS);
                if (clankFS == null) {
                    clankFS = new ClankFileObjectBasedFileSystem(nbFS);
                    ClankFileObjectBasedFileSystem prevClankFS = fileSystems.putIfAbsent(nbFS, clankFS);
                    if (prevClankFS != null) {
                        clankFS.destroy();
                        clankFS = prevClankFS;
                    }
                }
                return new IntrusiveRefCntPtr<FileSystem>(clankFS);
            }
        }
        return null;
    }    
}
