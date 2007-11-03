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
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.gsf.Index;
import org.netbeans.api.gsf.NameKind;
import org.netbeans.api.gsfpath.classpath.ClassPath;
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
 */
public final class ClassIndex extends Index {
    
    private static final Logger LOGGER = Logger.getLogger(ClassIndex.class.getName());
    
    private final ClassPath bootPath;
    private final ClassPath classPath;
    private final ClassPath sourcePath;
    
    private Set<ClassIndexImpl> sourceIndeces; // XXX Indices
    private Set<ClassIndexImpl> depsIndeces;
    
    // Constants moved up to Index    
    
    static {
	ClassIndexImpl.FACTORY = new ClassIndexFactoryImpl();
    }
    
    ClassIndex(final ClassPath bootPath, final ClassPath classPath, final ClassPath sourcePath) {
        //assert bootPath != null;
        //assert classPath != null;
        //assert sourcePath != null;
        this.bootPath = bootPath;
        this.classPath = classPath;
        this.sourcePath = sourcePath;
    }
    
    public void gsfSearch(final String primaryField, final String name, final NameKind kind, 
            final Set<SearchScope> scope, /*final ResultConvertor<T> convertor,*/ 
            final Set<SearchResult> result) throws IOException {
        assert primaryField != null;
        assert name != null;
        assert kind != null;
        final Iterable<? extends ClassIndexImpl> queries = this.getQueries(scope);        
        //final ResultConvertor<ElementHandle<TypeElement>> thConvertor = ResultConvertor.elementHandleConvertor();
        for (ClassIndexImpl query : queries) {
            query.gsfSearch(primaryField, name, kind, scope, result);
        }
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(String.format("ClassIndex.gsfSearch returned %d elements\n", result.size()));
        }
    }

    public void gsfStore(Set<Map<String,String>> fieldToData, Set<Map<String,String>> noIndexFields, Map<String,String> toDelete) throws IOException {
        // XXX Ugh
        throw new RuntimeException("Not yet implemented");
    }
    
//    /**
//     * Returns a set of {@link ElementHandle}s containing reference(s) to given element.
//     * @param element for which usages should be found
//     * @param searchKind type of reference, {@see SearchKind}
//     * @param scope to search in {@see SearchScope}
//     * @return set of {@link ElementHandle}s containing the reference(s)
//     *
//     */
//    public Set<ElementHandle<TypeElement>> getElements (final ElementHandle<TypeElement> element, final Set<SearchKind> searchKind, final Set<SearchScope> scope) {
//        assert element != null;
//        assert element.getSignature()[0] != null;
//        assert searchKind != null;
//        final Set<ElementHandle<TypeElement>> result = new HashSet<ElementHandle<TypeElement>> ();
//        final Iterable<? extends ClassIndexImpl> queries = this.getQueries (scope);
//        final Set<ClassIndexImpl.UsageType> ut =  encodeSearchKind(element.getKind(),searchKind);
//        final String binaryName = element.getSignature()[0];
//        final ResultConvertor<ElementHandle<TypeElement>> thConvertor = ResultConvertor.elementHandleConvertor();
//        if (!ut.isEmpty()) {
//            for (ClassIndexImpl query : queries) {
//                query.search(binaryName, ut, thConvertor, result);
//            }
//        }
//        return Collections.unmodifiableSet(result);
//    }
//    
//    /**
//     * Returns a set of source files containing reference(s) to given element.
//     * @param element for which usages should be found
//     * @param searchKind type of reference, {@see SearchKind}
//     * @param scope to search in {@see SearchScope}
//     * @return set of {@link FileObject}s containing the reference(s)
//     *
//     */
//    public Set<FileObject> getResources (final ElementHandle<TypeElement> element, final Set<SearchKind> searchKind, final Set<SearchScope> scope) {
//        assert element != null;
//        assert element.getSignature()[0] != null;
//        assert searchKind != null;
//        final Set<FileObject> result = new HashSet<FileObject> ();
//        final Iterable<? extends ClassIndexImpl> queries = this.getQueries (scope);
//        final Set<ClassIndexImpl.UsageType> ut =  encodeSearchKind(element.getKind(),searchKind);
//        final String binaryName = element.getSignature()[0];        
//        if (!ut.isEmpty()) {
//            for (ClassIndexImpl query : queries) {
//                final ResultConvertor<FileObject> foConvertor = ResultConvertor.fileObjectConvertor (query.getSourceRoots());
//                query.search (binaryName, ut, foConvertor, result);
//            }
//        }
//        return Collections.unmodifiableSet(result);
//    }        
//    
//    
//    /**
//     * Returns {@link ElementHandle}s for all declared types in given classpath corresponding to the name.
//     * @param case sensitive prefix, case insensitive prefix, exact simple name,
//     * camel case or regular expression depending on the kind parameter.
//     * @param kind of the name {@see NameKind}
//     * @param scope to search in {@see SearchScope}
//     * @return set of all matched declared types
//     */
//    public Set<ElementHandle<TypeElement>> getDeclaredTypes (final String name, final NameKind kind, final Set<SearchScope> scope) {
//        assert name != null;
//        assert kind != null;
//        final Set<ElementHandle<TypeElement>> result = new HashSet<ElementHandle<TypeElement>>();        
//        final Iterable<? extends ClassIndexImpl> queries = this.getQueries (scope);        
//        final ResultConvertor<ElementHandle<TypeElement>> thConvertor = ResultConvertor.elementHandleConvertor();
//        for (ClassIndexImpl query : queries) {
//            query.getDeclaredTypes (name, kind, thConvertor, result);
//        }
//        LOGGER.fine(String.format("ClassIndex.getDeclaredTypes returned %d elements\n", result.size()));
//        return Collections.unmodifiableSet(result);
//    }
//    
//    /**
//     * Returns names af all packages in given classpath starting with prefix.
//     * @param prefix of the package name
//     * @param directOnly if true treats the packages as folders and returns only
//     * the nearest component of the package.
//     * @param scope to search in {@see SearchScope}
//     * @return set of all matched package names
//     */
//    public Set<String> getPackageNames (final String prefix, boolean directOnly, final Set<SearchScope> scope) {
//        assert prefix != null;
//        final Set<String> result = new HashSet<String> ();        
//        final Iterable<? extends ClassIndexImpl> queries = this.getQueries (scope);
//        for (ClassIndexImpl query : queries) {
//            query.getPackageNames (prefix, directOnly, result);
//        }
//        return Collections.unmodifiableSet(result);
//    }
//    
//    // Private innerclasses ----------------------------------------------------
//        
    private static class ClassIndexFactoryImpl implements ClassIndexFactory {
        
	public ClassIndex create(final ClassPath bootPath, final ClassPath classPath, final ClassPath sourcePath) {            
	    return new ClassIndex(bootPath, classPath, sourcePath);
        }
	
    }
    
    //Private methods
    
    
    private synchronized Iterable<? extends ClassIndexImpl> getQueries (final Set<SearchScope> scope) {        
        Set<ClassIndexImpl> result = new HashSet<ClassIndexImpl> ();

        if (scope.contains(SearchScope.SOURCE)) {            
            if (this.sourceIndeces == null) {
                Set<ClassIndexImpl> indeces = new HashSet<ClassIndexImpl>();
                createQueriesForRoots (this.sourcePath, true, indeces);
                this.sourceIndeces = indeces;
            }
            result.addAll(this.sourceIndeces);
        }        

        if (scope.contains(SearchScope.DEPENDENCIES)) {
            if (this.depsIndeces == null) {
                Set<ClassIndexImpl> indeces = new HashSet<ClassIndexImpl>();
                // BEGIN TOR MODIFICATIONS
                List<ClassPath.Entry> entries = this.classPath.entries();
                if (entries.size() == 0) {
                    // For files outside of my projects (such as the libraries in the ruby
                    // installation) I don't get a classpath, so I end up with a fallback
                    // boot ClassPath containing Java .jar files. I don't want that - I want
                    // the boot indices from the Ruby libraries instead. I need to look
                    // into the ClassPath platform to support to fix it there, but that's
                    // too late/risky for now; so work around this instead. When we have an
                    // empty classpath, use the boot indices instead.
                    Set<ClassIndexImpl> bootIndices = ClassIndexManager.getDefault().getBootIndices();
                    indeces.addAll(bootIndices);
                } else {
                    createQueriesForRoots (this.bootPath, false, indeces);     
                }
                // END TOR MODIFICATIONS
                createQueriesForRoots (this.classPath, false, indeces);	    
                this.depsIndeces = indeces;
            }
            result.addAll(this.depsIndeces);
        }
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine(String.format("ClassIndex.queries[Scope=%s, sourcePath=%s, bootPath=%s, classPath=%s] => %s\n",scope,sourcePath,bootPath,classPath,result));
        }
        return result;
    }
    
    
    // For lucene browser
    public static
    /*private*/ void createQueriesForRoots (final ClassPath cp, final boolean sources, final Set<? super ClassIndexImpl> queries) {
        final GlobalSourcePath gsp = GlobalSourcePath.getDefault();
        List<ClassPath.Entry> entries = cp.entries();
	for (ClassPath.Entry entry : entries) {
	    try {
                boolean indexNow = false;
                URL[] srcRoots;
                if (!sources) {
                    URL srcRoot = org.netbeans.modules.gsfret.source.usages.Index.getSourceRootForClassFolder (entry.getURL());
                    if (srcRoot != null) {
                        srcRoots = new URL[] {srcRoot};
                    }
                    else {                        
                        srcRoots = gsp.getSourceRootForBinaryRoot (entry.getURL(), cp, true);                        
                        if (srcRoots == null) {
                            indexNow = true;
                            srcRoots = new URL[] {entry.getURL()};
                        }
                    }
                    //End to be removed
                }
                else {
                    srcRoots = new URL[] {entry.getURL()};
                }                
                for (URL srcRoot : srcRoots) {
                    ClassIndexImpl ci = ClassIndexManager.getDefault().getUsagesQuery(srcRoot);
                    if (ci != null) {
                        queries.add (ci);
                    }
                }
	    } catch (IOException ioe) {
		Exceptions.printStackTrace(ioe);
	    }
	}
    }
    
//    
//    private static Set<ClassIndexImpl.UsageType> encodeSearchKind (final ElementKind elementKind, final Set<ClassIndex.SearchKind> kind) {
//        assert kind != null;
//        final Set<ClassIndexImpl.UsageType> result = EnumSet.noneOf(ClassIndexImpl.UsageType.class);
//        for (ClassIndex.SearchKind sk : kind) {
//            switch (sk) {
//                case METHOD_REFERENCES:                    
//                    result.add(ClassIndexImpl.UsageType.METHOD_REFERENCE);                    
//                    break;
//                case FIELD_REFERENCES:
//                    result.add(ClassIndexImpl.UsageType.FIELD_REFERENCE);
//                    break;
//                case TYPE_REFERENCES:
//                    result.add(ClassIndexImpl.UsageType.TYPE_REFERENCE);
//                    break;
//                case IMPLEMENTORS:
//                    switch( elementKind) {
//                        case INTERFACE:
//                        case ANNOTATION_TYPE:
//                            result.add(ClassIndexImpl.UsageType.SUPER_INTERFACE);
//                            break;
//                        case CLASS:
//                            result.add(ClassIndexImpl.UsageType.SUPER_CLASS);
//                            break;
//                        case ENUM:	//enum is final
//                            break;
//                        case OTHER:
//                            result.add(ClassIndexImpl.UsageType.SUPER_INTERFACE);
//                            result.add(ClassIndexImpl.UsageType.SUPER_CLASS);
//                            break;
//                        default:
//                            throw new IllegalArgumentException ();                                        
//                    }
//                    break;
//                default:
//                    throw new IllegalArgumentException ();                    
//            }
//        }
//        return result;
//    }           
//    
}
