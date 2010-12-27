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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.parsing.impl.indexing.lucene;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.parsing.lucene.support.DocumentIndex;
import org.netbeans.modules.parsing.lucene.support.IndexManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author Tomas Zezula
 */
public final class DocumentBasedIndexManager {

    private static DocumentBasedIndexManager instance;

    private final Map<URL, DocumentIndex> indexes = new HashMap<URL, DocumentIndex> ();

    private DocumentBasedIndexManager() {}


    public static enum Mode {
        OPENED,
        CREATE,
        IF_EXIST;
    }


    public static synchronized DocumentBasedIndexManager getDefault () {
        if (instance == null) {
            instance = new DocumentBasedIndexManager();
        }
        return instance;
    }

    public synchronized DocumentIndex getIndex (final URL root, final Mode mode) throws IOException {
        assert root != null;
        DocumentIndex li = indexes.get(root);
        if (li == null) {
            try {
                switch (mode) {
                    case CREATE:                    
                        li = IndexManager.createDocumentIndex(new File(root.toURI()));
                        indexes.put(root,li);
                        break;
                    case IF_EXIST:
                        final FileObject fo = URLMapper.findFileObject(root);
                        if (fo != null && fo.isFolder() && fo.getChildren(false).hasMoreElements()) {
                            li = IndexManager.createDocumentIndex(new File(root.toURI()));
                            indexes.put(root,li);
                        }
                        break;
                }
            } catch (URISyntaxException e) {
                throw new IOException(e);
            }
        }
        return li;
    }

}
