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
package org.netbeans.modules.docker.remote;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.compress.archivers.ArchiveException;
import org.apache.commons.compress.archivers.ArchiveOutputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Petr Hejl
 */
public class FolderUploader {

    private static final Logger LOGGER = Logger.getLogger(FolderUploader.class.getName());

    // XXX only one upload at a time ?
    private static final RequestProcessor RP = new RequestProcessor(FolderUploader.class);

    private final OutputStream os;

    public FolderUploader(OutputStream os) {
        this.os = os;
    }

    public Future<Void> upload(final File folder, final IgnoreFileFilter filter) {
        return RP.submit(new Callable<Void>() {
            @Override
            public Void call() throws IOException, ArchiveException {
                ChunkedOutputStream cos = new ChunkedOutputStream(new BufferedOutputStream(os));
                try {
                    GZIPOutputStream gzos = new GZIPOutputStream(cos);
                    try {
                        ArchiveOutputStream aos = new ArchiveStreamFactory().createArchiveOutputStream(
                                ArchiveStreamFactory.TAR, gzos);
                        try {
                            // FIXME exclude dockerignored files
                            FileObject context = FileUtil.toFileObject(FileUtil.normalizeFile(folder));
                            for (Enumeration<? extends FileObject> e = context.getChildren(true); e.hasMoreElements();) {
                                FileObject child = e.nextElement();
                                if (child.isFolder()) {
                                    continue;
                                }
                                String path = FileUtil.getRelativePath(context, child);
                                if (filter.accept(path)) {
                                    LOGGER.log(Level.FINE, "Uploading {0}", path);
                                    TarArchiveEntry entry = new TarArchiveEntry(FileUtil.toFile(child),
                                            path);
                                    aos.putArchiveEntry(entry);
                                    try (InputStream is = new BufferedInputStream(child.getInputStream())) {
                                        FileUtil.copy(is, aos);
                                    }
                                    aos.closeArchiveEntry();
                                }
                            }
                        } finally {
                            aos.finish();
                            aos.flush();
                        }
                    } finally {
                        gzos.finish();
                        gzos.flush();
                    }
                } finally {
                    cos.finish();
                    cos.flush();
                }
                return null;
            }
        });
    }
}
