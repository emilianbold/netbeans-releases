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
package org.netbeans.modules.web.jsf.editor.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 *
 * @author marekfukala
 */
public class JsfBinariesIndex {

    private final QuerySupport index;

    /** Creates a new instance of JsIndex */
    private JsfBinariesIndex(QuerySupport index) {
        this.index = index;
    }

    private static JsfBinariesIndex get(FileObject[] roots) {
        try {
            return new JsfBinariesIndex(QuerySupport.forRoots(JsfBinaryIndexer.INDEXER_NAME,
                    JsfBinaryIndexer.INDEX_VERSION,
                    roots));
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return new JsfBinariesIndex(null);
        }

    }

    public static JsfBinariesIndex get(WebModule wm) {
        return get(wm.getDocumentBase());
    }

    public static JsfBinariesIndex get(HtmlParserResult result) {
        FileObject file = result.getSnapshot().getSource().getFileObject();
        return get(file);
    }

    public static JsfBinariesIndex get(FileObject file) {
        return get(ClassPath.getClassPath(file, ClassPath.EXECUTE).getRoots());
    }

    public Map<String, FileObject> getAllTldLibraries() {
        Map<String, FileObject> map = new HashMap<String, FileObject>();
        try {
            Collection<? extends IndexResult> results = index.query(JsfBinaryIndexer.LIB_NAMESPACE_KEY, "", QuerySupport.Kind.PREFIX, JsfBinaryIndexer.LIB_NAMESPACE_KEY);

            for (IndexResult result : results) {
                FileObject file = result.getFile(); //expensive? use result.getRelativePath?
                map.put(result.getValue(JsfBinaryIndexer.LIB_NAMESPACE_KEY), file);

            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return map;
    }

    public FileObject getTldFile(String namespace) {
        try {
            Collection<? extends IndexResult> results =
                    index.query(JsfBinaryIndexer.LIB_NAMESPACE_KEY, namespace, QuerySupport.Kind.EXACT, JsfBinaryIndexer.LIB_NAMESPACE_KEY);
            if (results.size() == 0) {
                return null;
            }
            IndexResult result = results.iterator().next(); //get first occurance
            return result.getFile();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    public Collection<FileObject> getAllFaceletsLibraryDescriptors() {
        Collection<FileObject> files = new ArrayList<FileObject>();
        try {
            Collection<? extends IndexResult> results = index.query(JsfBinaryIndexer.LIB_FACELETS_KEY, "true", QuerySupport.Kind.EXACT, JsfBinaryIndexer.LIB_FACELETS_KEY);

            for (IndexResult result : results) {
                FileObject file = result.getFile();
                files.add(file);

            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return files;
    }
}
