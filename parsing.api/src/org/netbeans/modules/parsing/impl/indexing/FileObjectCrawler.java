/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.impl.indexing;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.queries.VisibilityQuery;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tomas Zezula
 */
public final class FileObjectCrawler extends Crawler {
    
    private final FileObject root;
    private final FileObject[] files;

    public FileObjectCrawler(FileObject root, boolean checkTimeStamps, Set<String> mimeTypesToCheck) throws IOException {
        super (root.getURL(), checkTimeStamps, mimeTypesToCheck);
        this.root = root;
        this.files = null;
    }

    public FileObjectCrawler(FileObject root, FileObject[] files, Set<String> mimeTypesToCheck) throws IOException {
        super (root.getURL(), false, mimeTypesToCheck);
        this.root = root;
        this.files = files;
    }

    @Override
    protected Map<String, Collection<Indexable>> collectResources(final Set<? extends String> supportedMimeTypes) {
        Map<String, Collection<Indexable>> result = new HashMap<String, Collection<Indexable>>();
        if (files != null) {
            collect(files, root, result, supportedMimeTypes);
        } else {
            collect(root.getChildren(), root, result, supportedMimeTypes);
        }
        return result;
    }

    private void collect (FileObject[] fos, FileObject root,
            final Map<String, Collection<Indexable>> cache,
            final Set<? extends String> supportedMimeTypes) {
        for (FileObject fo : fos) {
            //keep the same logic like in RepositoryUpdater
            if (!fo.isValid() || !VisibilityQuery.getDefault().isVisible(fo)) {
                continue;
            }
            if (fo.isFolder()) {
                collect(fo.getChildren(), root, cache, supportedMimeTypes);
            } else {
                final String mime = fo.getMIMEType();
//                boolean ignore = "content/unknown".equals(mime); //NOI18N
//
//                if (!ignore && supportedMimeTypes != null) {
//                    ignore = !supportedMimeTypes.contains(mime);
//                }

                if (supportedMimeTypes == null || supportedMimeTypes.contains(mime)) {
                    Collection<Indexable> indexable = cache.get(mime);
                    if (indexable == null) {
                        indexable = new HashSet<Indexable>();
                        cache.put(mime, indexable);
                    }

                    if (!isUpToDate(fo)) {
                        indexable.add(SPIAccessor.getInstance().create(new FileObjectIndexable(root, fo)));
                    }
                }
            }
        }
    }
}
