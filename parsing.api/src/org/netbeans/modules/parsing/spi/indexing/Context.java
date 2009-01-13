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

package org.netbeans.modules.parsing.spi.indexing;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import org.netbeans.modules.parsing.impl.indexing.IndexFactoryImpl;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 * Represents a context of indexing given root.
 * @author Tomas Zezula
 */
//@NotThreadSafe
public final class Context {

    private final URL rootURL;
    private final FileObject indexBaseFolder;
    private final FileObject indexFolder;
    private final String indexerName;
    private final int indexerVersion;
    private FileObject root;

    //unit test
    final IndexFactoryImpl factory;

    Context (final FileObject indexBaseFolder,
             final URL rootURL, final String indexerName, final int indexerVersion,
             final IndexFactoryImpl factory) throws IOException {
        assert indexBaseFolder != null;
        assert rootURL != null;
        assert indexerName != null;
        this.indexBaseFolder = indexBaseFolder;
        this.rootURL = rootURL;
        this.indexerName = indexerName;
        this.indexerVersion = indexerVersion;
        this.factory = factory;
        final String path = getIndexerPath(indexerName, indexerVersion); //NOI18N
        this.indexFolder = FileUtil.createFolder(this.indexBaseFolder,path);
    }

    /**
     * Returns the cache folder where the indexer may store language metadata.
     * For each root and indexer there exist a separate cache folder.
     * @return The cache folder
     */
    public FileObject getIndexFolder () {        
        return this.indexFolder;
    }

    /**
     * Return the {@link URI} of the processed root
     * @return the absolute URI
     */
    public URL getRootURI () {
        return this.rootURL;
    }

    /**
     * Return the processed root, may return null
     * when the processed root was deleted.
     * The {@link Context#getRootURI()} can be used in
     * this case.
     * @return the root or null when the root doesn't exist
     */
    public FileObject getRoot () {
        if (root == null) {            
            root = URLMapper.findFileObject(this.rootURL);
        }
        return root;
    }

    String getIndexerName () {
        return this.indexerName;
    }

    int getIndexerVersion () {
        return this.indexerVersion;
    }

    static String getIndexerPath (final String indexerName, final int indexerVersion) {
        return indexerName+"/"+indexerVersion;
    }
}
