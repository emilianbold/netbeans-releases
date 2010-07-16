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

package org.netbeans.modules.cnd.spi.utils;

import java.io.File;
import java.util.Collection;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileSystem;
import org.openide.util.Lookup;

/**
 *
 * @author Vladimir Kvashin
 */
public abstract class FileSystemsProvider {

    public static final class Data {
        public final FileSystem fileSystem;
        public final String path;

        public Data(FileSystem fileSystem, String path) {
            this.fileSystem = fileSystem;
            this.path = path;
        }

        @Override
        public String toString() {
            return this.fileSystem.getDisplayName() + ":" + path; // NOI18N
        }
    }

    private static final DefaultProvider DEFAULT = new DefaultProvider();

    private static DefaultProvider getDefault() {
        return DEFAULT;
    }

    public static String getCaseInsensitivePath(CharSequence path) {
        return getDefault().getCaseInsensitivePathImpl(path);
    }

    public static CharSequence lowerPathCaseIfNeeded(CharSequence path) {
        return getDefault().lowerPathCaseIfNeededImpl(path);
    }

    /** It can return null */
    public static Data get(File file) {
        return getDefault().getImpl(file);
    }

    /** It can return null */
    public static Data get(CharSequence path) {
        return getDefault().getImpl(path);
    }

    protected abstract Data getImpl(File file);
    protected abstract Data getImpl(CharSequence path);
    protected abstract String getCaseInsensitivePathImpl(CharSequence path);
    protected abstract boolean isMine(CharSequence path);

    private static class DefaultProvider extends FileSystemsProvider {

        private FileSystemsProvider[] cache;

        DefaultProvider() {
            Collection<? extends FileSystemsProvider> instances =
                    Lookup.getDefault().lookupAll(FileSystemsProvider.class);
            cache = instances.toArray(new FileSystemsProvider[instances.size()]);
        }

        public Data getImpl(File file) {
            for (FileSystemsProvider provider : cache) {
                Data data = provider.getImpl(file);
                if (data != null) {
                    return data;
                }
            }
            return null;
        }

        public Data getImpl(CharSequence path) {
            for (FileSystemsProvider provider : cache) {
                Data data = provider.getImpl(path);
                if (data != null) {
                    return data;
                }
            }
            return null;
        }

        public String getCaseInsensitivePathImpl(CharSequence path) {
            for (FileSystemsProvider provider : cache) {
                String data = provider.getCaseInsensitivePathImpl(path);
                if (data != null) {
                    return data;
                }
            }
            return path.toString();
        }

        protected CharSequence lowerPathCaseIfNeededImpl(CharSequence path) {
            if (CndFileUtils.isSystemCaseSensitive()) {
                return path;
            } else {
                for (FileSystemsProvider provider : cache) {
                    if (provider.isMine(path)) {
                        return path;
                    }
                }
                return path.toString().toLowerCase();
            }
        }

        @Override
        protected boolean isMine(CharSequence path) {
            for (FileSystemsProvider provider : cache) {
                if (provider.isMine(path)) {
                    return true;
                }
            }
            return false;
        }
    }
}
