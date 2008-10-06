/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.napi.gsfret.source;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.gsf.api.Index;
import org.netbeans.modules.gsf.api.Indexer;
import org.netbeans.modules.gsf.api.NameKind;
import org.netbeans.modules.gsfpath.api.classpath.ClassPath;
import org.netbeans.modules.gsf.Language;
import org.netbeans.modules.gsf.LanguageRegistry;
import org.netbeans.modules.gsfret.source.GlobalSourcePath;
import org.netbeans.modules.gsfret.source.usages.ClassIndexFactory;
import org.netbeans.modules.gsfret.source.usages.ClassIndexImpl;
import org.netbeans.modules.gsfret.source.usages.ClassIndexManager;
import org.openide.util.Exceptions;

/**
 * This file is originally from Retouche, the Java Support 
 * infrastructure in NetBeans. I have modified the file as little
 * as possible to make merging Retouche fixes back as simple as
 * possible.
 *
 * The ClassIndex provides access to information stored in the 
 * persistent index. It can be used to obtain list of packages
 * or declared types. It can be also used to obtain a list of
 * source files referencing given type (usages of given type).
 *
 * @author Petr Hrebejk, Tomas Zezula
 * @author Tor Norbye
 */
public final class ClassIndex extends Index {
    
    private static final Logger LOGGER = Logger.getLogger(ClassIndex.class.getName());
    
    private final Language language;
    private final ClassPath bootPath;
    private final ClassPath classPath;
    private final ClassPath sourcePath;
    
    private Set<ClassIndexImpl> sourceIndeces; // XXX Indices
    private Set<ClassIndexImpl> depsIndeces;
    
    // Constants moved up to Index    
    
    static {
	ClassIndexImpl.FACTORY = new ClassIndexFactoryImpl();
    }
    
    ClassIndex(final Language language, final ClassPath bootPath, final ClassPath classPath, final ClassPath sourcePath) {
        this.language = language;
        //assert bootPath != null;
        //assert classPath != null;
        //assert sourcePath != null;
        this.bootPath = bootPath;
        this.classPath = classPath;
        this.sourcePath = sourcePath;
    }
    
    public Language getLanguage() {
        return language;
    }
    
    public void search(final String primaryField, final String name, final NameKind kind, 
            final Set<SearchScope> scope, /*final ResultConvertor<T> convertor,*/ 
            final Set<SearchResult> result, Set<String> terms) throws IOException {
        assert primaryField != null;
        assert name != null;
        assert kind != null;
        final Iterable<? extends ClassIndexImpl> queries = this.getQueries(scope);        
        for (ClassIndexImpl query : queries) {
            try {
                query.search(primaryField, name, kind, scope, result, terms);
            } catch (ClassIndexImpl.IndexAlreadyClosedException e) {
                logClosedIndex (query);
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }

        }
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(String.format("ClassIndex.search returned %d elements\n", result.size()));
        }
    }
    
    /** For test infrastructure only */
    public void initForTest(Set<ClassIndexImpl> sources) {
        this.sourceIndeces = sources;
    }
    
    private static class ClassIndexFactoryImpl implements ClassIndexFactory {
        
	public ClassIndex create(final Language language, final ClassPath bootPath, final ClassPath classPath, final ClassPath sourcePath) {            
	    return new ClassIndex(language, bootPath, classPath, sourcePath);
        }
	
    }

    private static void logClosedIndex (final ClassIndexImpl query) {
        assert query != null;
        LOGGER.info("Ignoring closed index: " + query.toString());  //NOI18N
    }
    
    private synchronized Iterable<? extends ClassIndexImpl> getQueries (final Set<SearchScope> scope) {        
        Set<ClassIndexImpl> result = new HashSet<ClassIndexImpl> ();

        if (scope.contains(SearchScope.SOURCE)) {            
            if (this.sourceIndeces == null) {
                Set<ClassIndexImpl> indeces = new HashSet<ClassIndexImpl>();
                createQueriesForRoots (language, this.sourcePath, true, indeces);
                this.sourceIndeces = indeces;
            }
            result.addAll(this.sourceIndeces);
        }        

        if (scope.contains(SearchScope.DEPENDENCIES)) {
            if (this.depsIndeces == null) {
                Set<ClassIndexImpl> indeces = new HashSet<ClassIndexImpl>();
                // BEGIN TOR MODIFICATIONS
                List<ClassPath.Entry> entries = this.classPath.entries();
                if (entries.size() > 0) {
                    createQueriesForRoots(language, this.bootPath, false, indeces);
                }
                
                // Add in core libraries unconditionally such that they are supported
                // even outside of projects
                LanguageRegistry.getInstance().getLibraryUrls(); // Lazy init

                // For files outside of my projects (such as the libraries in the ruby
                // installation) I don't get a classpath, so I end up with a fallback
                // boot ClassPath containing Java .jar files. I don't want that - I want
                // the boot indices from the Ruby libraries instead. I need to look
                // into the ClassPath platform to support to fix it there, but that's
                // too late/risky for now; so work around this instead. When we have an
                // empty classpath, use the boot indices instead.
                Set<ClassIndexImpl> bootIndices = ClassIndexManager.get(language).getBootIndices();
                Indexer indexer = language.getIndexer();
                if (indexer != null) {
                    for (ClassIndexImpl ci : bootIndices) {
                        if (ci != null) {
                            URL root = ci.getRoot();
                            if (root != null) {
                                if (indexer.acceptQueryPath(root.toExternalForm())) {
                                    indeces.add(ci);
                                }
                            }
                        }
                    }
                }
                
                // END TOR MODIFICATIONS
                createQueriesForRoots(language, this.classPath, false, indeces);	    
                this.depsIndeces = indeces;
            }
            result.addAll(this.depsIndeces);
        }
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(String.format("ClassIndex.queries[Scope=%s, sourcePath=%s, bootPath=%s, classPath=%s] => %s\n",scope,sourcePath,bootPath,classPath,result));
        }
        return result;
    }
    
    
    // Public for the lucene browser
    public static
    /*private*/ void createQueriesForRoots (final Language language, final ClassPath cp, final boolean sources, final Set<? super ClassIndexImpl> queries) {
        final GlobalSourcePath gsp = GlobalSourcePath.getDefault();
        List<ClassPath.Entry> entries = cp.entries();
        Indexer indexer = language.getIndexer();
        if (indexer == null) {
            return;
        }
        
	for (ClassPath.Entry entry : entries) {
	    try {
                if (!indexer.acceptQueryPath(entry.getURL().toExternalForm())) {
                    continue;
                }
                URL[] srcRoots;
                if (!sources) {
                    URL srcRoot = org.netbeans.modules.gsfret.source.usages.Index.getSourceRootForClassFolder (language, entry.getURL());
                    if (srcRoot != null) {
                        srcRoots = new URL[] {srcRoot};
                    }
                    else {                        
                        srcRoots = gsp.getSourceRootForBinaryRoot (entry.getURL(), cp, true);                        
                        if (srcRoots == null) {
                            srcRoots = new URL[] {entry.getURL()};
                        }
                    }
                    //End to be removed
                }
                else {
                    srcRoots = new URL[] {entry.getURL()};
                }                
                for (URL srcRoot : srcRoots) {
                    ClassIndexImpl ci = ClassIndexManager.get(language).getUsagesQuery(srcRoot);
                    if (ci != null) {
                        queries.add (ci);
                    }
                }
	    } catch (IOException ioe) {
		Exceptions.printStackTrace(ioe);
	    }
	}
    }
}
