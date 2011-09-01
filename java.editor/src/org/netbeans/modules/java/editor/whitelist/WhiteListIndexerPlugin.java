/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.editor.whitelist;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.LineMap;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.Trees;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.whitelist.WhiteListQuery;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaIndexerPlugin;
import org.netbeans.modules.parsing.lucene.support.DocumentIndex;
import org.netbeans.modules.parsing.lucene.support.IndexDocument;
import org.netbeans.modules.parsing.lucene.support.IndexManager;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Tomas Zezula
 */
public class WhiteListIndexerPlugin implements JavaIndexerPlugin {

    private static final String WHITE_LIST_INDEX = "whitelist"; //NOI18N
    static final String MSG = "msg";    //NOI18N
    static final String LINE = "line";  //NOI18N
    private static volatile String whiteListPath;

    private final WhiteListQuery.WhiteList whiteList;
    private final DocumentIndex index;

    private WhiteListIndexerPlugin(
            @NonNull final WhiteListQuery.WhiteList whiteList,
            @NonNull final DocumentIndex index) {
        assert whiteList != null;
        assert index != null;
        this.whiteList = whiteList;
        this.index = index;
    }

    @Override
    public void process(
            @NonNull final CompilationUnitTree toProcess,
            @NonNull final Indexable indexable,
            @NonNull final Lookup services) {
        final Trees trees = services.lookup(Trees.class);
        assert trees != null;
        final WhiteListScanner scanner = new WhiteListScanner(
                trees,
                whiteList,
                new AtomicBoolean());
        final List<WhiteListScanner.Problem> problems = new LinkedList<WhiteListScanner.Problem>();
        scanner.scan(toProcess,problems);
        final LineMap lm = toProcess.getLineMap();
        final SourcePositions sp = trees.getSourcePositions();
        for (WhiteListScanner.Problem p : problems) {
            final int start = (int) sp.getStartPosition(toProcess, p.tree);
            int ln;
            if (start>=0 && (ln=(int)lm.getLineNumber(start))>=0) {
                final IndexDocument doc = IndexManager.createDocument(indexable.getRelativePath());
                doc.addPair(MSG, p.description, false, true);
                doc.addPair(LINE, Integer.toString(ln), false, true);
                index.addDocument(doc);
            }
        }
    }

    @Override
    public void delete(@NonNull final Indexable indexable) {
        index.removeDocument(indexable.getRelativePath());
    }

    @Override
    public void finish() {
        try {
            index.store(true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            try {
                index.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    static String getWhiteListPath() {
        return whiteListPath;
    }

    @MimeRegistration(mimeType="text/x-java",service=JavaIndexerPlugin.Factory.class)
    public static class Factory implements JavaIndexerPlugin.Factory {
        @Override
        public JavaIndexerPlugin create(final URL root, final FileObject cacheFolder) {
            final FileObject rootFo = URLMapper.findFileObject(root);
            if (rootFo == null) {
                //TODO: clean up cache
                return null;
            }
            final WhiteListQuery.WhiteList wl = WhiteListQuery.getWhiteList(rootFo);
            if (wl == null) {
                return null;
            }
            try {
                final FileObject whiteListFolder = FileUtil.createFolder(cacheFolder, WHITE_LIST_INDEX);
                final File whiteListDir = FileUtil.toFile(whiteListFolder);
                if (whiteListDir == null) {
                    return null;
                }
                final FileObject indexFolder = cacheFolder.getParent().getParent();
                whiteListPath = FileUtil.getRelativePath(indexFolder, whiteListFolder);
                return new WhiteListIndexerPlugin(
                    wl,
                    IndexManager.createDocumentIndex(whiteListDir));
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
                return null;
            }
        }
    }

}
