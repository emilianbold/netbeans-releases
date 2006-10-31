/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.java.source;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.java.source.classpath.GlobalSourcePath;
import org.netbeans.modules.java.source.usages.ClassIndexFactory;
import org.netbeans.modules.java.source.usages.ClassIndexImpl;
import org.netbeans.modules.java.source.usages.ClassIndexManager;
import org.netbeans.modules.java.source.usages.Index;
import org.netbeans.modules.java.source.usages.ResultConvertor;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * The ClassIndex provides access to information stored in the 
 * persistent index. It can be used to obtain list of packages
 * or declared types. It can be also used to obtain a list of
 * source files referencing given type (usages of given type).
 *
 * @author Petr Hrebejk, Tomas Zezula
 */
public final class ClassIndex {
    
    private static final Logger LOGGER = Logger.getLogger(ClassIndex.class.getName());
    
    private final ClassPath bootPath;
    private final ClassPath classPath;
    private final ClassPath sourcePath;
    
    private Set<ClassIndexImpl> sourceIndeces;
    private Set<ClassIndexImpl> depsIndeces;
    
    
    
    /**
     * Encodes a type of the name kind used by 
     * {@link ClassIndex#getDeclaredTypes} method.
     *
     */
    public enum NameKind {
        /**
         * The name parameter of the {@link ClassIndex#getDeclaredTypes}
         * is an exact simple name of the package or declared type.
         */
        SIMPLE_NAME,
        
        /**
         * The name parameter of the {@link ClassIndex#getDeclaredTypes} 
         * is an case sensitive prefix of the package or declared type name.
         */
        PREFIX,
        
        /**
         * The name parameter of the {@link ClassIndex#getDeclaredTypes} is 
         * an case insensitive prefix of the declared type name.
         */
        CASE_INSENSITIVE_PREFIX,
        
        /**
         * The name parameter of the {@link ClassIndex#getDeclaredTypes} is 
         * an camel case of the declared type name.
         */
        CAMEL_CASE,
        
        
        /**
         * The name parameter of the {@link ClassIndex#getDeclaredTypes} is 
         * an regular expression of the declared type name.
         */
        REGEXP
    };
    
    
    /**
     * Encodes a reference type,
     * used by {@link ClassIndex#getElements} and {@link ClassIndex#getResources}
     * to restrict the search.
     */
    public enum SearchKind {
        
        /**
         * The returned class has to extend or implement given element
         */
        IMPLEMENTORS,
        
        /**
         * The returned class has to call method on given element
         */
        METHOD_REFERENCES,
        
        /**
         * The returned class has to access a field on given element
         */
        FIELD_REFERENCES,
        
        /**
         * The returned class contains references to the element type
         */
        TYPE_REFERENCES,        
    };
    
    /**
     * Scope used by {@link ClassIndex} to search in
     */
    public enum SearchScope {
        /**
         * Search is done in source path
         */
        SOURCE,
        /**
         * Search is done in compile and boot path
         */
        DEPENDENCIES
    };
    
    static {
	ClassIndexImpl.FACTORY = new ClassIndexFactoryImpl();
    }
    
    ClassIndex(final ClassPath bootPath, final ClassPath classPath, final ClassPath sourcePath) {
        assert bootPath != null;
        assert classPath != null;
        assert sourcePath != null;
        this.bootPath = bootPath;
        this.classPath = classPath;
        this.sourcePath = sourcePath;
    }
    
    
    /**
     * Returns a set of {@link ElementHandle}s containing reference(s) to given element.
     * @param element for which usages should be found
     * @param searchKind type of reference, {@see SearchKind}
     * @param scope to search in {@see SearchScope}
     * @return set of {@link ElementHandle}s containing the reference(s)
     *
     */
    public Set<ElementHandle<TypeElement>> getElements (final ElementHandle<TypeElement> element, final Set<SearchKind> searchKind, final Set<SearchScope> scope) {
        assert element != null;
        assert element.getSignature()[0] != null;
        assert searchKind != null;
        final Set<ElementHandle<TypeElement>> result = new HashSet<ElementHandle<TypeElement>> ();
        final Iterable<? extends ClassIndexImpl> queries = this.getQueries (scope);
        final Set<ClassIndexImpl.UsageType> ut =  encodeSearchKind(element.getKind(),searchKind);
        final String binaryName = element.getSignature()[0];
        final ResultConvertor<ElementHandle<TypeElement>> thConvertor = ResultConvertor.elementHandleConvertor();
        if (!ut.isEmpty()) {
            for (ClassIndexImpl query : queries) {
                query.search(binaryName, ut, thConvertor, result);
            }
        }
        return Collections.unmodifiableSet(result);
    }
    
    /**
     * Returns a set of source files containing reference(s) to given element.
     * @param element for which usages should be found
     * @param searchKind type of reference, {@see SearchKind}
     * @param scope to search in {@see SearchScope}
     * @return set of {@link FileObject}s containing the reference(s)
     *
     */
    public Set<FileObject> getResources (final ElementHandle<TypeElement> element, final Set<SearchKind> searchKind, final Set<SearchScope> scope) {
        assert element != null;
        assert element.getSignature()[0] != null;
        assert searchKind != null;
        final Set<FileObject> result = new HashSet<FileObject> ();
        final Iterable<? extends ClassIndexImpl> queries = this.getQueries (scope);
        final Set<ClassIndexImpl.UsageType> ut =  encodeSearchKind(element.getKind(),searchKind);
        final String binaryName = element.getSignature()[0];        
        if (!ut.isEmpty()) {
            for (ClassIndexImpl query : queries) {
                final ResultConvertor<FileObject> foConvertor = ResultConvertor.fileObjectConvertor (query.getSourceRoots());
                query.search (binaryName, ut, foConvertor, result);
            }
        }
        return Collections.unmodifiableSet(result);
    }        
    
    
    /**
     * Returns {@link ElementHandle}s for all declared types in given classpath corresponding to the name.
     * @param case sensitive prefix, case insensitive prefix, exact simple name,
     * camel case or regular expression depending on the kind parameter.
     * @param kind of the name {@see NameKind}
     * @param scope to search in {@see SearchScope}
     * @return set of all matched declared types
     */
    public Set<ElementHandle<TypeElement>> getDeclaredTypes (final String name, final NameKind kind, final Set<SearchScope> scope) {
        assert name != null;
        assert kind != null;
        final Set<ElementHandle<TypeElement>> result = new HashSet<ElementHandle<TypeElement>>();        
        final Iterable<? extends ClassIndexImpl> queries = this.getQueries (scope);        
        final ResultConvertor<ElementHandle<TypeElement>> thConvertor = ResultConvertor.elementHandleConvertor();
        for (ClassIndexImpl query : queries) {
            query.getDeclaredTypes (name, kind, thConvertor, result);
        }
        LOGGER.fine(String.format("ClassIndex.getDeclaredTypes returned %d elements\n", result.size()));
        return Collections.unmodifiableSet(result);
    }
    
    /**
     * Returns names af all packages in given classpath starting with prefix.
     * @param prefix of the package name
     * @param directOnly if true treats the packages as folders and returns only
     * the nearest component of the package.
     * @param scope to search in {@see SearchScope}
     * @return set of all matched package names
     */
    public Set<String> getPackageNames (final String prefix, boolean directOnly, final Set<SearchScope> scope) {
        assert prefix != null;
        final Set<String> result = new HashSet<String> ();        
        final Iterable<? extends ClassIndexImpl> queries = this.getQueries (scope);
        for (ClassIndexImpl query : queries) {
            query.getPackageNames (prefix, directOnly, result);
        }
        return Collections.unmodifiableSet(result);
    }
    
    // Private innerclasses ----------------------------------------------------
        
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
                createQueriesForRoots (this.bootPath, false, indeces);                
                createQueriesForRoots (this.classPath, false, indeces);	    
                this.depsIndeces = indeces;
            }
            result.addAll(this.depsIndeces);
        }
        LOGGER.fine(String.format("ClassIndex.queries[Scope=%s, sourcePath=%s, bootPath=%s, classPath=%s] => %s\n",scope,sourcePath,bootPath,classPath,result));
        return result;
    }
    
    
    private void createQueriesForRoots (final ClassPath cp, final boolean sources, final Set<? super ClassIndexImpl> queries) {
        final GlobalSourcePath gsp = GlobalSourcePath.getDefault();
        List<ClassPath.Entry> entries = cp.entries();
	for (ClassPath.Entry entry : entries) {
	    try {
                boolean indexNow = false;
                URL[] srcRoots;
                if (!sources) {
                    URL srcRoot = Index.getSourceRootForClassFolder (entry.getURL());
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
    
    
    private static Set<ClassIndexImpl.UsageType> encodeSearchKind (final ElementKind elementKind, final Set<ClassIndex.SearchKind> kind) {
        assert kind != null;
        final Set<ClassIndexImpl.UsageType> result = EnumSet.noneOf(ClassIndexImpl.UsageType.class);
        for (ClassIndex.SearchKind sk : kind) {
            switch (sk) {
                case METHOD_REFERENCES:                    
                    result.add(ClassIndexImpl.UsageType.METHOD_REFERENCE);                    
                    break;
                case FIELD_REFERENCES:
                    result.add(ClassIndexImpl.UsageType.FIELD_REFERENCE);
                    break;
                case TYPE_REFERENCES:
                    result.add(ClassIndexImpl.UsageType.TYPE_REFERENCE);
                    break;
                case IMPLEMENTORS:
                    switch( elementKind) {
                        case INTERFACE:
                        case ANNOTATION_TYPE:
                            result.add(ClassIndexImpl.UsageType.SUPER_INTERFACE);
                            break;
                        case CLASS:
                            result.add(ClassIndexImpl.UsageType.SUPER_CLASS);
                            break;
                        case ENUM:	//enum is final
                            break;
                        case OTHER:
                            result.add(ClassIndexImpl.UsageType.SUPER_INTERFACE);
                            result.add(ClassIndexImpl.UsageType.SUPER_CLASS);
                            break;
                        default:
                            throw new IllegalArgumentException ();                                        
                    }
                    break;
                default:
                    throw new IllegalArgumentException ();                    
            }
        }
        return result;
    }           
    
}
