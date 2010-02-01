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
package org.netbeans.modules.css.indexing;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.css.indexing.DependenciesGraph.Node;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * An instance of the indexer which can be held until the source roots are valid.
 * 
 * @author marekfukala
 */
public class CssIndex {

    private static final Logger LOGGER = Logger.getLogger(CssIndex.class.getSimpleName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);

    public static CssIndex create(FileObject[] sourceRoots) throws IOException {
        return new CssIndex(sourceRoots);
    }
    private final QuerySupport querySupport;

    /** Creates a new instance of JsfIndex */
    private CssIndex(FileObject[] sourceRoots) throws IOException {
        //QuerySupport now refreshes the roots indexes so it can held until
        //the source roots are valid
        this.querySupport = QuerySupport.forRoots(CssIndexer.Factory.NAME, CssIndexer.Factory.VERSION, sourceRoots);
    }

    public Collection<FileObject> findIds(String id) {
        return find(CssIndexer.IDS_KEY, id);
    }

    public Collection<FileObject> findClasses(String clazz) {
        return find(CssIndexer.CLASSES_KEY, clazz);
    }

    public Collection<FileObject> findHtmlElement(String htmlElement) {
        return find(CssIndexer.HTML_ELEMENTS_KEY, htmlElement);
    }

    /**
     *
     * @param keyName
     * @param value
     * @return returns a collection of files which contains the keyName key and the
     * value matches the value regular expression
     */
    public Collection<FileObject> find(String keyName, String value) {
        try {
            String searchExpression = ".*(" + value + ")[,;].*";
            Collection<FileObject> matchedFiles = new LinkedList<FileObject>();
            Collection<? extends IndexResult> results = querySupport.query(keyName, searchExpression, QuerySupport.Kind.REGEXP, keyName);
            for (IndexResult result : results) {
                matchedFiles.add(result.getFile());
            }
            return matchedFiles;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return Collections.emptyList();
    }

    /**
     * Gets all 'related' files to the given css file object.
     * 
     * @param cssFile
     * @return a collection of all files which either imports or are imported
     * by the given cssFile both directly and indirectly (transitive relation)
     */
    public DependenciesGraph getDependencies(FileObject cssFile) {
        try {
            DependenciesGraph deps = new DependenciesGraph(cssFile);
            Collection<? extends IndexResult> results = querySupport.query(CssIndexer.IMPORTS_KEY, "", QuerySupport.Kind.PREFIX, CssIndexer.IMPORTS_KEY);

            //create the refering part of the graph (imported files)
            //map of FileObject to list of imported files
            Map<FileObject, Collection<String>> files2imports = new HashMap<FileObject, Collection<String>>();
            for (IndexResult result : results) {
                String importsValue = result.getValue(CssIndexer.IMPORTS_KEY);
                FileObject file = result.getFile();
                files2imports.put(file, decodeListValue(importsValue));
            }
            resolveImports(deps.getSourceNode(), files2imports);

            //resolve importing files
            //TODO the recursive algrithm uses linear search - this deserves
            //fixing even if the number of css files is typically quite small

            //reversed map of imports to files
            Map<String, FileObject> imports2files = new HashMap<String, FileObject>();
            for (FileObject file : files2imports.keySet()) {
                for (String imp : files2imports.get(file)) {
                    imports2files.put(imp, file);
                }
            }
            resolveImporting(deps.getSourceNode(), imports2files);

            return deps;

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return null;
    }

    private void resolveImporting(Node sourceNode, Map<String, FileObject> imports2files) {
        FileObject source = sourceNode.getFile();
        //a. find all entries which may possibly be references to our base file
        String baseFileName = source.getNameExt();
        //hmm, linear search :-(, wouldn't it be faster to uset the index instead?
        Collection<String> possiblyValidImports = new LinkedList<String>();
        for (String imp : imports2files.keySet()) {
            if (imp.indexOf(baseFileName) != -1) {
                //might possibly import the base file
                possiblyValidImports.add(imp);
            }
        }

        //b.now check if the possible imports do really import our base file
        for (String possibleImport : possiblyValidImports) {
            FileObject base = imports2files.get(possibleImport);
            FileObject resolved = resolve(base, possibleImport);
            if (resolved != null && resolved.equals(source)) {
                //gotcha!
                Node node = sourceNode.getDependencyGraph().getNode(base);
                sourceNode.addReferingNode(node);
                resolveImporting(node, imports2files);
            }
        }
    }

    private void resolveImports(Node base, Map<FileObject, Collection<String>> file2imports) {
        FileObject source = base.getFile();
        Collection<String> imports = file2imports.get(source);
        if (imports == null) {
            return;
        }

        for (String importedFileName : imports) {
            //resolve the file
            FileObject resolvedFileObject = resolve(source, importedFileName);
            if (resolvedFileObject != null) {
                Node node = base.getDependencyGraph().getNode(resolvedFileObject);
                base.addReferedNode(node);
                resolveImports(node, file2imports);
            }
        }

    }

    private FileObject resolve(FileObject source, String importedFileName) {
        URI u = URI.create(importedFileName);
        File file = null;

        if (u.isAbsolute()) {
            //do refactor only file resources
            if ("file".equals(u.getScheme())) { //NOI18N
                try {
                    //the IAE is thrown for invalid URIs quite frequently
                    file = new File(u);
                } catch (IllegalArgumentException iae) {
                    //no-op
                }
            }
        } else {
            //no schema specified
            file = new File(importedFileName);
        }

        if (file != null && !file.isAbsolute()) {
            //relative to the current file's folder - let's resolve
            FileObject resolvedFileObject = source.getParent().getFileObject(importedFileName);
            if (resolvedFileObject != null && resolvedFileObject.isValid()) {
                return resolvedFileObject;
            }
        } else {
            //absolute - TO THE DEPLOYMENT ROOT!!!
            //todo implement!!!
            if(LOG) {
                LOGGER.fine("Cannot resolve import '" + importedFileName + "' from file " + source.getPath()); //NOI18N
            }
        }
        return null;
    }

    //each list value is terminated by semicolon
    private Collection<String> decodeListValue(String value) {
        assert value.charAt(value.length() - 1) == ';';
        Collection<String> list = new ArrayList<String>();
        StringTokenizer st = new StringTokenizer(value.substring(0, value.length() - 1), ",");
        while (st.hasMoreTokens()) {
            list.add(st.nextToken());
        }
        return list;
    }
}
