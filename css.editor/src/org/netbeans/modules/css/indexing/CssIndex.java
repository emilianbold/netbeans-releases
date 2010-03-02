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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.css.refactoring.api.RefactoringElementType;
import org.netbeans.modules.web.common.api.DependenciesGraph;
import org.netbeans.modules.web.common.api.DependenciesGraph.Node;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.web.common.api.FileReference;
import org.netbeans.modules.web.common.api.WebUtils;
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

    public static CssIndex create(Project project) throws IOException {
        return new CssIndex(project);
    }
    private final QuerySupport querySupport;

    /** Creates a new instance of JsfIndex */
    private CssIndex(Project project) throws IOException {
        //QuerySupport now refreshes the roots indexes so it can held until the source roots are valid
        Collection<FileObject> sourceRoots = QuerySupport.findRoots(project,
                null /* all source roots */,
                Collections.<String>emptyList(),
                Collections.<String>emptyList());
        this.querySupport = QuerySupport.forRoots(CssIndexer.Factory.NAME, CssIndexer.Factory.VERSION, sourceRoots.toArray(new FileObject[]{}));
    }

    /**
     *
     * @param id
     * @return collection of files defining exactly the given element
     */
    public Collection<FileObject> findIds(String id) {
        return find(RefactoringElementType.ID, id);
    }

    /**
     *
     * @param id
     * @return collection of files defining exactly the given element
     */
    public Collection<FileObject> findClasses(String clazz) {
        return find(RefactoringElementType.CLASS, clazz);
    }

    /**
     *
     * @param id
     * @return collection of files defining exactly the given element
     */
    public Collection<FileObject> findHtmlElement(String htmlElement) {
        return find(RefactoringElementType.ELEMENT, htmlElement);
    }

    /**
     *
     * @param id
     * @return collection of files defining exactly the given element
     */
    public Collection<FileObject> findColor(String colorCode) {
        return find(RefactoringElementType.COLOR, colorCode);
    }

    /**
     *
     * @param prefix
     * @return map of fileobject to collection of classes defined in the file starting with prefix
     */
    public Map<FileObject, Collection<String>> findClassesByPrefix(String prefix) {
        return findByPrefix(RefactoringElementType.CLASS, prefix);
    }

    /**
     *
     * @param prefix
     * @return map of fileobject to collection of ids defined in the file starting with prefix
     */
    public Map<FileObject, Collection<String>> findIdsByPrefix(String prefix) {
        return findByPrefix(RefactoringElementType.ID, prefix);
    }

    /**
     *
     * @param prefix
     * @return map of fileobject to collection of colors defined in the file starting with prefix
     */
    public Map<FileObject, Collection<String>> findColorsByPrefix(String prefix) {
        return findByPrefix(RefactoringElementType.COLOR, prefix);
    }
    
    public Map<FileObject, Collection<String>> findByPrefix(RefactoringElementType type, String prefix) {
        String keyName = type.getIndexKey();
        Map<FileObject, Collection<String>> map = new HashMap<FileObject, Collection<String>>();
        try {
            Collection<? extends IndexResult> results;
            if(prefix.length() == 0) {
                results = querySupport.query(keyName, "", QuerySupport.Kind.PREFIX, keyName);
            } else {
                String searchExpression = ".*("+prefix+").*"; //NOI18N
                results = querySupport.query(keyName, searchExpression, QuerySupport.Kind.REGEXP, keyName);
            }
            for (IndexResult result : results) {
                Collection<String> elements = decodeListValue(result.getValue(keyName));
                for(String e : elements) {
                    if(e.startsWith(prefix)) {
                        Collection<String> col = map.get(result.getFile());
                        if(col == null) {
                            col = new LinkedList<String>();
                            map.put(result.getFile(), col);
                        }
                        col.add(e);
                    }
                }

            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return map;
    }

    public Map<FileObject, Collection<String>> findAll(RefactoringElementType type) {
        String keyName = type.getIndexKey();
        Map<FileObject, Collection<String>> map = new HashMap<FileObject, Collection<String>>();
        try {
            Collection<? extends IndexResult> results =
                    querySupport.query(keyName, "", QuerySupport.Kind.PREFIX, keyName);

            for (IndexResult result : results) {
                Collection<String> elements = decodeListValue(result.getValue(keyName));
                for (String e : elements) {
                    Collection<String> col = map.get(result.getFile());
                    if (col == null) {
                        col = new LinkedList<String>();
                        map.put(result.getFile(), col);
                    }
                    col.add(e);
                }

            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return map;
    }

    /**
     *
     * @param keyName
     * @param value
     * @return returns a collection of files which contains the keyName key and the
     * value matches the value regular expression
     */
    public Collection<FileObject> find(RefactoringElementType type, String value) {
        String keyName = type.getIndexKey();
        try {
            String searchExpression = ".*(" + value + ")[,;].*"; //NOI18N
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
            AllDependenciesMaps alldeps = getAllDependencies();
            resolveDependencies(deps.getSourceNode(), alldeps.getSource2dest(), alldeps.getDest2source());
            return deps;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return null;
    }

    /**
     * Gets two maps wrapped in the AllDependenciesMaps class which contains
     * all dependencies defined by imports in the current project.
     *
     * @return instance of AllDependenciesMaps
     * @throws IOException
     */
    public AllDependenciesMaps getAllDependencies() throws IOException {
        Collection<? extends IndexResult> results = querySupport.query(CssIndexer.IMPORTS_KEY, "", QuerySupport.Kind.PREFIX, CssIndexer.IMPORTS_KEY);
        Map<FileObject, Collection<FileReference>> source2dests = new HashMap<FileObject, Collection<FileReference>>();
        Map<FileObject, Collection<FileReference>> dest2sources = new HashMap<FileObject, Collection<FileReference>>();
        for (IndexResult result : results) {
            String importsValue = result.getValue(CssIndexer.IMPORTS_KEY);
            FileObject file = result.getFile();
            Collection<String> imports = decodeListValue(importsValue);
            Collection<FileReference> imported = new HashSet<FileReference>();
            for (String importedFileName : imports) {
                //resolve the file
                FileReference resolvedReference = WebUtils.resolveToReference(file, importedFileName);
//                FileObject resolvedFileObject = ref.target();
                if (resolvedReference != null) {
                    imported.add(resolvedReference);
                    //add reverse dependency
                    Collection<FileReference> sources = dest2sources.get(resolvedReference.target());
                    if (sources == null) {
                        sources = new HashSet<FileReference>();
                        dest2sources.put(resolvedReference.target(), sources);
                    }
                    sources.add(resolvedReference);
                }
            }
            source2dests.put(file, imported);
        }

        return new AllDependenciesMaps(source2dests, dest2sources);

    }
    

    private void resolveDependencies(Node base, Map<FileObject, Collection<FileReference>> source2dests, Map<FileObject, Collection<FileReference>> dest2sources) {
        FileObject baseFile = base.getFile();
        Collection<FileReference> destinations = source2dests.get(baseFile);
        if (destinations != null) {
            //process destinations (file this one refers to)
            for(FileReference destinationReference : destinations) {
                FileObject destination = destinationReference.target();
                Node node = base.getDependencyGraph().getNode(destination);
                if(base.addReferedNode(node)) {
                    //recurse only if we haven't been there yet
                    resolveDependencies(node, source2dests, dest2sources);
                }
            }
        }
        Collection<FileReference> sources = dest2sources.get(baseFile);
        if(sources != null) {
            //process sources (file this one is refered by)
            for(FileReference sourceReference : sources) {
                FileObject source = sourceReference.target();
                Node node = base.getDependencyGraph().getNode(source);
                if(base.addReferingNode(node)) {
                    //recurse only if we haven't been there yet
                    resolveDependencies(node, source2dests, dest2sources);
                }
            }
        }

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

    public static class AllDependenciesMaps {

        Map<FileObject, Collection<FileReference>> source2dest, dest2source;

        public AllDependenciesMaps(Map<FileObject, Collection<FileReference>> source2dest, Map<FileObject, Collection<FileReference>> dest2source) {
            this.source2dest = source2dest;
            this.dest2source = dest2source;
        }

        /**
         *
         * @return reversed map of getSource2dest() (imported file -> collection of
         * importing files)
         */
        public Map<FileObject, Collection<FileReference>> getDest2source() {
            return dest2source;
        }

        /**
         *
         * @return map of fileobject -> collection of fileobject(s) describing
         * relations between css file defined by import directive. The key represents
         * a fileobject which imports the files from the value's collection.
         */
        public Map<FileObject, Collection<FileReference>> getSource2dest() {
            return source2dest;
        }

    }

    
}
