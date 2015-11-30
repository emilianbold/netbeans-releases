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
package org.netbeans.modules.java.source.parsing;

import java.io.IOException;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.tools.JavaFileObject;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;

/**
 *
 * @author Tomas Zezula
 */
public final class PathArchive implements Archive {

    private final Path root;
    private final String rootURI;
    private final char separator;

    PathArchive(
            @NonNull final Path root,
            @NullAllowed final URI rootURI) {
        assert root != null;
        this.root = root;
        this.rootURI = rootURI == null ? null : rootURI.toString();
        final String separator = root.getFileSystem().getSeparator();
        if (separator.length() != 1) {
            throw new IllegalArgumentException("Multi character separators are unsupported");
        }
        this.separator = separator.charAt(0);
    }

    @Override
    @NonNull
    public Iterable<JavaFileObject> getFiles(
            @NonNull String folderName,
            @NullAllowed final ClassPath.Entry entry,
            @NullAllowed final Set<JavaFileObject.Kind> kinds,
            @NullAllowed final JavaFileFilterImplementation filter) throws IOException {
        if (separator != FileObjects.NBFS_SEPARATOR_CHAR) {
            folderName = folderName.replace(FileObjects.NBFS_SEPARATOR_CHAR, separator);
        }
        final Path target = root.resolve(folderName);
        try (DirectoryStream<Path> dir = Files.newDirectoryStream(target, new KindFilter(kinds))) {
            final List<JavaFileObject> res = new ArrayList<>();
            for (Path dirEnt : dir) {
                res.add(FileObjects.pathFileObject(dirEnt, root, rootURI, null));
            }
            return res;
        } catch (NotDirectoryException | NoSuchFileException e) {
            return Collections.emptyList();
        }
    }

    @Override
    @CheckForNull
    public JavaFileObject getFile(@NonNull String name) throws IOException {
        if (separator != FileObjects.NBFS_SEPARATOR_CHAR) {
            name = name.replace(FileObjects.NBFS_SEPARATOR_CHAR, separator);
        }
        final Path target = root.resolve(name);
        return Files.exists(target) ?
                FileObjects.pathFileObject(target, root, rootURI, null) :
                null;
    }

    @Override
    public JavaFileObject create(String relativeName, JavaFileFilterImplementation filter) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Write not supported");   //NOI18N
    }

    @Override
    public void clear() {
    }


    private static final class KindFilter implements DirectoryStream.Filter<Path> {
        private final Set<JavaFileObject.Kind> kinds;

        KindFilter(@NullAllowed final Set<JavaFileObject.Kind> kinds) {
            this.kinds = kinds;
        }

        @Override
        public boolean accept(Path entry) throws IOException {
            if (Files.isDirectory(entry)) {
                return false;
            }
            if (kinds == null) {
                return true;
            }
            return kinds.contains(FileObjects.getKind(
                    FileObjects.getExtension(entry.getFileName().toString())));
        }
    }
}
