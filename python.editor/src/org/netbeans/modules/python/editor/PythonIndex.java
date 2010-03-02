/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.python.editor;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.python.editor.elements.IndexedElement;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.Index;
import org.netbeans.modules.gsf.api.Index.SearchResult;
import org.netbeans.modules.gsf.api.Index.SearchScope;
import org.netbeans.modules.gsf.api.NameKind;
import org.netbeans.modules.python.api.PythonPlatform;
import org.netbeans.modules.python.api.PythonPlatformManager;
import org.netbeans.modules.python.editor.elements.IndexedPackage;
import org.netbeans.modules.python.editor.imports.ImportManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.URLMapper;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.python.antlr.ast.Import;
import org.python.antlr.ast.ImportFrom;
import org.python.antlr.ast.alias;

/**
 *
 * @author alley
 * @author Tor Norbye
 */
public class PythonIndex {
    public static final Set<SearchScope> ALL_SCOPE = EnumSet.allOf(SearchScope.class);
    public static final Set<SearchScope> SOURCE_SCOPE = EnumSet.of(SearchScope.SOURCE);
    static final String CLUSTER_URL = "cluster:"; // NOI18N
    static final String PYTHONHOME_URL = "python:"; // NOI18N
    private static final String STUB_MISSING = "stub_missing"; // NOI18N
    private final Index index;
    private final FileObject context;

    // The "functions" module is always imported by the interpreter, and ditto
    // for exceptions, constants, etc.
    public static Set<String> BUILTIN_MODULES = new HashSet<String>();


    static {
        //BUILTIN_MODULES.add("objects"); // NOI18N -- just links to the others
        BUILTIN_MODULES.add("stdtypes"); // NOI18N
        //BUILTIN_MODULES.add("types"); // NOI18N
        BUILTIN_MODULES.add("exceptions"); // NOI18N
        BUILTIN_MODULES.add("functions"); // NOI18N
        BUILTIN_MODULES.add("constants"); // NOI18N
    }

    /** Creates a new instance of PythonIndex */
    private PythonIndex(Index index, FileObject context) {
        this.index = index;
        this.context = context;
    }

    public static PythonIndex get(Index index) {
        return new PythonIndex(index, null);
    }

    public static PythonIndex get(Index index, FileObject context) {
        return new PythonIndex(index, context);
    }

    private boolean search(String key, String name, NameKind kind, Set<SearchResult> result,
            Set<SearchScope> scope, Set<String> terms) {
        try {
            index.search(key, name, kind, scope, result, terms);

            return true;
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);

            return false;
        }
    }

    public Set<IndexedElement> getModules(String name, final NameKind kind) {
        final Set<SearchResult> result = new HashSet<SearchResult>();

        //        if (!isValid()) {
        //            LOGGER.fine(String.format("LuceneIndex[%s] is invalid!\n", this.toString()));
        //            return;
        //        }

        // TODO - handle case insensitive searches etc?
        String field = PythonIndexer.FIELD_MODULE_NAME;
        Set<String> terms = new HashSet<String>(5);
        terms.add(PythonIndexer.FIELD_MODULE_ATTR_NAME);
        terms.add(PythonIndexer.FIELD_MODULE_NAME);

        search(field, name, kind, result, ALL_SCOPE, terms);

        final Set<IndexedElement> modules = new HashSet<IndexedElement>();

        for (SearchResult map : result) {
            String url = map.getPersistentUrl();
            if (url == null) {
                continue;
            }
            String module = map.getValue(PythonIndexer.FIELD_MODULE_NAME);
            if (STUB_MISSING.equals(module)) {
                continue;
            }

            IndexedElement element = new IndexedElement(module, ElementKind.MODULE, url, null, null, null);

            String attrs = map.getValue(PythonIndexer.FIELD_MODULE_ATTR_NAME);
            if (attrs != null && attrs.indexOf('D') != -1) {
                element.setFlags(IndexedElement.DEPRECATED);
            }

            String rhs = url.substring(url.lastIndexOf('/') + 1);
            element.setRhs(rhs);
            modules.add(element);
        }

        return modules;
    }

    public Set<IndexedPackage> getPackages(String name, final NameKind kind) {
        final Set<SearchResult> result = new HashSet<SearchResult>();

        String field = PythonIndexer.FIELD_MODULE_NAME;
        search(field, name, kind, result, ALL_SCOPE, Collections.singleton(PythonIndexer.FIELD_MODULE_NAME));

        final Set<IndexedPackage> packages = new HashSet<IndexedPackage>();

        for (SearchResult map : result) {
            String module = map.getValue(PythonIndexer.FIELD_MODULE_NAME);

            String pkgName = null;
            String pkg = null;

            int nextNextDot = -1;
            int lastDot = module.lastIndexOf('.');
            int nameLength = name.length();
            if (nameLength < lastDot) {
                int nextDot = module.indexOf('.', nameLength);
                if (nextDot != -1) {
                    pkg = module.substring(0, nextDot);
                    nextNextDot = module.indexOf('.', nextDot + 1);
                    int start = module.lastIndexOf('.', name.length());
                    if (start == -1) {
                        start = 0;
                    } else {
                        start++;
                    }
                    pkgName = module.substring(start, nextDot);
                }
            } else if (lastDot != -1) {
                pkgName = module.substring(lastDot + 1);
                pkg = module;
            }

            if (pkgName != null) {
                String url = map.getPersistentUrl();
                IndexedPackage element = new IndexedPackage(pkgName, pkg, url, nextNextDot != -1);
                element.setRhs("");
                packages.add(element);
            }
        }

        return packages;
    }

    public Set<IndexedElement> getClasses(String name, final NameKind kind, Set<SearchScope> scope,
            PythonParserResult context, boolean includeDuplicates) {
        final Set<SearchResult> result = new HashSet<SearchResult>();

        //        if (!isValid()) {
        //            LOGGER.fine(String.format("LuceneIndex[%s] is invalid!\n", this.toString()));
        //            return;
        //        }
        String field;

        switch (kind) {
        case EXACT_NAME:
        case PREFIX:
        case CAMEL_CASE:
        case REGEXP:
            field = PythonIndexer.FIELD_CLASS_NAME;

            break;

        case CASE_INSENSITIVE_PREFIX:
        case CASE_INSENSITIVE_REGEXP:
            field = PythonIndexer.FIELD_CASE_INSENSITIVE_CLASS_NAME;

            break;

        default:
            throw new UnsupportedOperationException(kind.toString());
        }

        Set<String> terms = new HashSet<String>(5);
        terms.add(PythonIndexer.FIELD_IN);
        terms.add(PythonIndexer.FIELD_CLASS_ATTR_NAME);
        terms.add(PythonIndexer.FIELD_CLASS_NAME);

        search(field, name, kind, result, scope, terms);

        Set<String> uniqueClasses = includeDuplicates ? null : new HashSet<String>();

        final Set<IndexedElement> classes = new HashSet<IndexedElement>();

        for (SearchResult map : result) {
            String clz = map.getValue(PythonIndexer.FIELD_CLASS_NAME);
            if (clz == null) {
                // A module without classes
                continue;
            }
            String url = map.getPersistentUrl();
            String module = map.getValue(PythonIndexer.FIELD_IN);
            boolean isBuiltin = isBuiltinModule(module);

            String fqn = clz; // No further namespaces in Python, right?
            if (!includeDuplicates) {
                if (!uniqueClasses.contains(fqn)) { // use a map to point right to the class
                    uniqueClasses.add(fqn);
                    IndexedElement element = new IndexedElement(clz, ElementKind.CLASS, url, module, null, null);
                    if (isBuiltin) {
                        element.setRhs("<i>builtin</i>");
                    }
                    String attrs = map.getValue(PythonIndexer.FIELD_CLASS_ATTR_NAME);
                    if (attrs != null) {
                        int flags = IndexedElement.decode(attrs, 0, 0);
                        element.setFlags(flags);
                    }
                    element.setInherited(true);

                    classes.add(element);
                } // else: Possibly pick the best version... based on which items have documentation attributes etc.
            } else {
                IndexedElement element = new IndexedElement(clz, ElementKind.CLASS, url, module, null, null);
                classes.add(element);
            }
        }

        return classes;
    }

//    /** Return the most distant method in the hierarchy that is overriding the given method, or null
//     * @todo Make this method actually compute most distant ancestor
//     * @todo Use arglist arity comparison to reject methods that are not overrides...
//     */
//    public IndexedMethod getOverridingMethod(String className, String methodName) {
//        Set<IndexedElement> methods = getInheritedElements(className, methodName, NameKind.EXACT_NAME);
//
//        // TODO - this is only returning ONE match, not the most distant one. I really need to
//        // produce a PythonIndex method for this which can walk in there and do a decent job!
//
//        for (IndexedElement method : methods) {
//            if (method.getKind() == ElementKind.METHOD || method.getKind() == ElementKind.CONSTRUCTOR) {
//                // getInheritedMethods may return methods ON fqn itself
//                if (!method.getIn().equals(className)) {
//                    return (IndexedMethod)method;
//                }
//            }
//        }
//
//        return null;
//    }
    /** Get the super implementation of the given method */
    public Set<IndexedElement> getOverridingMethods(String className, String function) {
        Set<IndexedElement> methods = getInheritedElements(className, function, NameKind.EXACT_NAME, true);

        // TODO - remove all methods that are in the same file
        if (methods.size() > 0) {
            Set<IndexedElement> result = new HashSet<IndexedElement>(methods.size());
            for (IndexedElement element : methods) {
                if (!className.equals(element.getClz())) {
                    result.add(element);
                }
            }
            methods = result;
        }

        return methods;
//        // TODO - this is only returning ONE match, not the most distant one. I really need to
//        // produce a PythonIndex method for this which can walk in there and do a decent job!
//
//        for (IndexedElement method : methods) {
//            if (method.getKind() == ElementKind.METHOD || method.getKind() == ElementKind.CONSTRUCTOR) {
//                // getInheritedMethods may return methods ON fqn itself
//                if (!method.getIn().equals(className)) {
//                    return (IndexedMethod)method;
//                }
//            }
//        }
//
//        return null;
    }

    /** Get the super class of the given class */
    public Set<IndexedElement> getSuperClasses(String className) {
        final Set<SearchResult> result = new HashSet<SearchResult>();
        Set<String> terms = new HashSet<String>(5);
//        terms.add(PythonIndexer.FIELD_IN);
        terms.add(PythonIndexer.FIELD_EXTENDS_NAME);
        terms.add(PythonIndexer.FIELD_CLASS_NAME);

        search(PythonIndexer.FIELD_CLASS_NAME, className, NameKind.EXACT_NAME, result, ALL_SCOPE, terms);

        Set<String> classNames = new HashSet<String>();
        for (SearchResult map : result) {
            String[] extendsClasses = map.getValues(PythonIndexer.FIELD_EXTENDS_NAME);
            if (extendsClasses != null && extendsClasses.length > 0) {
                for (String clzName : extendsClasses) {
                    classNames.add(clzName);
                }
            }
        }

        terms = new HashSet<String>(5);
        terms.add(PythonIndexer.FIELD_IN);
        terms.add(PythonIndexer.FIELD_CLASS_NAME);

        Set<IndexedElement> superClasses = new HashSet<IndexedElement>();

        for (String superClz : classNames) {
            result.clear();
            search(PythonIndexer.FIELD_CLASS_NAME, superClz, NameKind.EXACT_NAME, result, ALL_SCOPE, terms);
            for (SearchResult map : result) {
                assert superClz.equals(map.getValue(PythonIndexer.FIELD_CLASS_NAME));
                String url = map.getPersistentUrl();
                String module = map.getValue(PythonIndexer.FIELD_IN);
                IndexedElement clz = new IndexedElement(superClz, ElementKind.CLASS, url, module, null, null);
                superClasses.add(clz);
            }
        }

        return superClasses;
    }

    /**
     * Get the set of inherited (through super classes and mixins) for the given fully qualified class name.
     * @param classFqn FQN: module1::module2::moduleN::class
     * @param prefix If kind is NameKind.PREFIX/CASE_INSENSITIVE_PREFIX, a prefix to filter methods by. Else,
     *    if kind is NameKind.EXACT_NAME filter methods by the exact name.
     * @param kind Whether the prefix field should be taken as a prefix or a whole name
     */
    public Set<IndexedElement> getInheritedElements(String classFqn, String prefix, NameKind kind) {
        return getInheritedElements(classFqn, prefix, kind, false);
    }

    public Set<IndexedElement> getInheritedElements(String classFqn, String prefix, NameKind kind, boolean includeOverrides) {
        boolean haveRedirected = false;

        if (classFqn == null) {
            classFqn = OBJECT;
            haveRedirected = true;
        }

        //String field = PythonIndexer.FIELD_FQN_NAME;
        Set<IndexedElement> elements = new HashSet<IndexedElement>();
        Set<String> scannedClasses = new HashSet<String>();
        Set<String> seenSignatures = new HashSet<String>();

        if (prefix == null) {
            prefix = "";
        }

//        String searchUrl = null;
//        if (context != null) {
//            try {
//                searchUrl = context.getFile().getFileObject().getURL().toExternalForm();
//            } catch (FileStateInvalidException ex) {
//                Exceptions.printStackTrace(ex);
//            }
//        }

        addMethodsFromClass(prefix, kind, classFqn, elements, seenSignatures, scannedClasses,
                haveRedirected, false, includeOverrides, 0);

        return elements;
    }

    public static final String OBJECT = "object"; // NOI18N

    /** Return whether the specific class referenced (classFqn) was found or not. This is
     * not the same as returning whether any classes were added since it may add
     * additional methods from parents (Object/Class).
     */
    private boolean addMethodsFromClass(String prefix, NameKind kind, String classFqn,
            Set<IndexedElement> elements, Set<String> seenSignatures, Set<String> scannedClasses,
            boolean haveRedirected, boolean inheriting, boolean includeOverrides, int depth) {
        // Prevent problems with circular includes or redundant includes
        if (scannedClasses.contains(classFqn)) {
            return false;
        }

        scannedClasses.add(classFqn);

        String searchField = PythonIndexer.FIELD_CLASS_NAME;

        Set<SearchResult> result = new HashSet<SearchResult>();

        Set<String> terms = new HashSet<String>(5);
        terms.add(PythonIndexer.FIELD_IN);
        terms.add(PythonIndexer.FIELD_EXTENDS_NAME);
        terms.add(PythonIndexer.FIELD_MEMBER);
        terms.add(PythonIndexer.FIELD_CLASS_NAME);


        search(searchField, classFqn, NameKind.EXACT_NAME, result, ALL_SCOPE, terms);

        boolean foundIt = result.size() > 0;

        // If this is a bogus class entry (no search rsults) don't continue
        if (!foundIt) {
            return foundIt;
        }

        List<String> extendsClasses = null;

        String classIn = null;
        int fqnIndex = classFqn.lastIndexOf("::"); // NOI18N

        if (fqnIndex != -1) {
            classIn = classFqn.substring(0, fqnIndex);
        }
        int prefixLength = prefix.length();

        for (SearchResult map : result) {
            assert map != null;

            String url = map.getPersistentUrl();
            String clz = map.getValue(PythonIndexer.FIELD_CLASS_NAME);
            String module = map.getValue(PythonIndexer.FIELD_IN);

            if (extendsClasses == null) {
                String[] ext = map.getValues(PythonIndexer.FIELD_EXTENDS_NAME);
                if (ext != null && ext.length > 0) {
                    if (extendsClasses == null) {
                        extendsClasses = Arrays.asList(ext);
                    } else {
                        extendsClasses = new ArrayList<String>(extendsClasses);
                        extendsClasses.addAll(Arrays.asList(ext));
                    }
                }
            }

            String[] members = map.getValues(PythonIndexer.FIELD_MEMBER);

            if (members != null) {
                for (String signature : members) {
                    // Prevent duplicates when method is redefined
                    if (includeOverrides || !seenSignatures.contains(signature)) {
                        if (signature.startsWith(prefix)) {
                            if (kind == NameKind.EXACT_NAME) {
                                if (signature.charAt(prefixLength) != ';') {
                                    continue;
                                }
                            } else if (kind == NameKind.CASE_INSENSITIVE_PREFIX && !signature.regionMatches(true, 0, prefix, 0, prefix.length())) {
                                continue;
                            } else {
                                // REGEXP, CAMELCASE filtering etc. not supported here
                                assert (kind == NameKind.PREFIX) ||
                                        (kind == NameKind.CASE_INSENSITIVE_PREFIX);
                            }

                            if (!includeOverrides) {
                                seenSignatures.add(signature);
                            }
                            IndexedElement element = IndexedElement.create(signature, module, url, clz);
                            // TODO - filter out private? Or let code completer do that? Probably should, in case
                            // we have more rights when inheriting
                            element.setSmart(!haveRedirected);
                            element.setInherited(inheriting);
                            if (includeOverrides) {
                                element.setOrder(depth);
                            }
                            elements.add(element);
                        }
                    }
                }
            }
        }

        if (classFqn.equals(OBJECT)) {
            return foundIt;
        }

        if (extendsClasses == null || extendsClasses.size() == 0) {
            addMethodsFromClass(prefix, kind, OBJECT, elements, seenSignatures, scannedClasses,
                    true, true, includeOverrides, depth + 1);
        } else {
            // We're not sure we have a fully qualified path, so try some different candidates
            for (String extendsClass : extendsClasses) {
                if (!addMethodsFromClass(prefix, kind, extendsClass, elements, seenSignatures,
                        scannedClasses, haveRedirected, true, includeOverrides, depth + 1)) {
                    // Search by classIn
                    String fqn = classIn;

                    while (fqn != null) {
                        if (addMethodsFromClass(prefix, kind, fqn + "::" + extendsClass, elements,
                                seenSignatures, scannedClasses, haveRedirected, true, includeOverrides, depth + 1)) {
                            break;
                        }

                        int f = fqn.lastIndexOf("::"); // NOI18N

                        if (f == -1) {
                            break;
                        } else {
                            fqn = fqn.substring(0, f);
                        }
                    }
                }
            }
        }

        return foundIt;
    }

    public Set<IndexedElement> getAllMembers(String name, NameKind kind, Set<SearchScope> scope,
            PythonParserResult context, boolean includeDuplicates) {
        final Set<SearchResult> result = new HashSet<SearchResult>();
        // TODO - handle case sensitivity better...
        String field = PythonIndexer.FIELD_MEMBER;
        NameKind originalKind = kind;
        if (kind == NameKind.EXACT_NAME) {
            // I can't do exact searches on methods because the method
            // entries include signatures etc. So turn this into a prefix
            // search and then compare chopped off signatures with the name
            kind = NameKind.PREFIX;
        }

        String searchUrl = null;
        if (context != null) {
            try {
                searchUrl = context.getFile().getFileObject().getURL().toExternalForm();
            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        Set<String> terms = new HashSet<String>(5);
        terms.add(PythonIndexer.FIELD_IN);
        terms.add(PythonIndexer.FIELD_EXTENDS_NAME);
        terms.add(PythonIndexer.FIELD_MEMBER);
        terms.add(PythonIndexer.FIELD_CLASS_NAME);

        search(field, name, kind, result, scope, terms);

//        Set<String> uniqueClasses = null;
//        if (includeDuplicates) {
//            uniqueClasses = null;
//        } else if (uniqueClasses == null) {
//            uniqueClasses = new HashSet<String>();
//        }

        final Set<IndexedElement> members = new HashSet<IndexedElement>();
        int nameLength = name.length();

        for (SearchResult map : result) {
            String[] signatures = map.getValues(PythonIndexer.FIELD_MEMBER);
            if (signatures != null && signatures.length > 0) {
                String url = map.getPersistentUrl();
                String clz = map.getValue(PythonIndexer.FIELD_CLASS_NAME);
                String module = map.getValue(PythonIndexer.FIELD_IN);
                boolean inherited = searchUrl == null || !searchUrl.equals(url);

                for (String signature : signatures) {
                    if (originalKind == NameKind.EXACT_NAME) {
                        if (signature.charAt(nameLength) != ';') {
                            continue;
                        }
                    } else if (originalKind == NameKind.CASE_INSENSITIVE_PREFIX && !signature.regionMatches(true, 0, name, 0, name.length())) {
                        continue;
                    } else {
                        // REGEXP, CAMELCASE filtering etc. not supported here
                        assert (originalKind == NameKind.PREFIX) ||
                                (originalKind == NameKind.CASE_INSENSITIVE_PREFIX);
                    }

                    IndexedElement element = IndexedElement.create(signature, module, url, clz);
                    element.setInherited(inherited);
                    members.add(element);
                }
            }
        }

        return members;
    }

    public Set<IndexedElement> getAllElements(String name, NameKind kind, Set<SearchScope> scope,
            PythonParserResult context, boolean includeDuplicates) {
        final Set<SearchResult> result = new HashSet<SearchResult>();
        // TODO - handle case sensitivity better...
        String field = PythonIndexer.FIELD_ITEM;
        NameKind originalKind = kind;
        if (kind == NameKind.EXACT_NAME) {
            // I can't do exact searches on methods because the method
            // entries include signatures etc. So turn this into a prefix
            // search and then compare chopped off signatures with the name
            kind = NameKind.PREFIX;
        }

        String searchUrl = null;
        if (context != null) {
            try {
                searchUrl = context.getFile().getFileObject().getURL().toExternalForm();
            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        Set<String> terms = new HashSet<String>(5);
        terms.add(PythonIndexer.FIELD_ITEM);
        terms.add(PythonIndexer.FIELD_MODULE_NAME);

        search(field, name, kind, result, scope, terms);

        final Set<IndexedElement> elements = new HashSet<IndexedElement>();
        int nameLength = name.length();

        for (SearchResult map : result) {
            String[] signatures = map.getValues(PythonIndexer.FIELD_ITEM);
            if (signatures != null && signatures.length > 0) {
                String url = map.getPersistentUrl();
                String module = map.getValue(PythonIndexer.FIELD_MODULE_NAME);
                boolean inherited = searchUrl == null || !searchUrl.equals(url);

                for (String signature : signatures) {
                    if (originalKind == NameKind.EXACT_NAME) {
                        if (signature.charAt(nameLength) != ';') {
                            continue;
                        }
                    } else if (originalKind == NameKind.CASE_INSENSITIVE_PREFIX && !signature.regionMatches(true, 0, name, 0, name.length())) {
                        continue;
                    } else {
                        // REGEXP, CAMELCASE filtering etc. not supported here
                        assert (originalKind == NameKind.PREFIX) ||
                                (originalKind == NameKind.CASE_INSENSITIVE_PREFIX);
                    }

                    IndexedElement element = IndexedElement.create(signature, module, url, null);
                    if (element.isPrivate() && !url.equals(searchUrl)) {
                        continue;
                    }
                    element.setInherited(inherited);
                    elements.add(element);
                }
            }
        }

        return elements;
    }

    public Set<String> getBuiltinSymbols() {
        Set<String> modules = new HashSet<String>();

        // The "functions" module is always imported by the interpreter, and ditto
        // for exceptions, constants, etc.
        //modules.add("objects"); // NOI18N -- just links to the others
        modules.addAll(BUILTIN_MODULES);

        Set<String> symbols = new HashSet<String>(250);

        Set<String> terms = new HashSet<String>(5);
        terms.add(PythonIndexer.FIELD_MODULE_NAME);
        terms.add(PythonIndexer.FIELD_ITEM);

        // Look up all symbols
        for (String module : modules) {
            final Set<SearchResult> result = new HashSet<SearchResult>();
            // TODO - handle case sensitivity better...
            String field = PythonIndexer.FIELD_MODULE_NAME;
            NameKind kind = NameKind.EXACT_NAME;

            search(field, module, kind, result, ALL_SCOPE, terms);

            for (SearchResult map : result) {
                String[] signatures = map.getValues(PythonIndexer.FIELD_ITEM);
                if (signatures != null) {
                    for (String signature : signatures) {
                        int semi = signature.indexOf(';');
                        assert semi != -1;
                        int flags = IndexedElement.decode(signature, semi + 3, 0);
                        if ((flags & IndexedElement.PRIVATE) != 0) {
                            // Skip private symbols - can't import those
                            continue;
                        }
                        String name = signature.substring(0, semi);
                        symbols.add(name);
                    }
                }
            }
        }

        // Computed as described below
        String[] MISSING = {
            "Ellipsis", "False", "IndentationError", "None", "NotImplemented", "TabError", // NOI18N
            "True", "__debug__", "__doc__", "__name__", "copyright", "credits", "exit", "license", // NOI18N
            "quit" // NOI18N
        };
        for (String s : MISSING) {
            symbols.add(s);
        }
        symbols.add("__builtins__"); // NOI18N
        symbols.add("__file__"); // NOI18N

        //// COMPUTING MISSING SYMBOLS:
        //// My builtin .rst files don't seem to define all the builtin symbols that the Python
        //// interpreter is configured with.  I generated these pretty trivially; run
        //// python and type "dir(__builtins__)" and you end up a list like the below:
        ////String[] EXTRA_BUILTINS = {"ArithmeticError", "AssertionError", "AttributeError", "BaseException",
        //    "DeprecationWarning", "EOFError", "Ellipsis", "EnvironmentError", "Exception", "False",
        //    "FloatingPointError", "FutureWarning", "GeneratorExit", "IOError", "ImportError",
        //    "ImportWarning", "IndentationError", "IndexError", "KeyError", "KeyboardInterrupt",
        //    "LookupError", "MemoryError", "NameError", "None", "NotImplemented", "NotImplementedError",
        //    "OSError", "OverflowError", "PendingDeprecationWarning", "ReferenceError", "RuntimeError",
        //    "RuntimeWarning", "StandardError", "StopIteration", "SyntaxError", "SyntaxWarning", "SystemError",
        //    "SystemExit", "TabError", "True", "TypeError", "UnboundLocalError", "UnicodeDecodeError",
        //    "UnicodeEncodeError", "UnicodeError", "UnicodeTranslateError", "UnicodeWarning", "UserWarning",
        //    "ValueError", "Warning", "ZeroDivisionError", "__debug__", "__doc__", "__import__", "__name__",
        //    "abs", "all", "any", "apply", "basestring", "bool", "buffer", "callable", "chr", "classmethod",
        //    "cmp", "coerce", "compile", "complex", "copyright", "credits", "delattr", "dict", "dir", "divmod",
        //    "enumerate", "eval", "execfile", "exit", "file", "filter", "float", "frozenset", "getattr",
        //    "globals", "hasattr", "hash", "help", "hex", "id", "input", "int", "intern", "isinstance",
        //    "issubclass", "iter", "len", "license", "list", "locals", "long", "map", "max", "min", "object",
        //    "oct", "open", "ord", "pow", "property", "quit", "range", "raw_input", "reduce", "reload",
        //    "repr", "reversed", "round", "set", "setattr", "slice", "sorted", "staticmethod", "str", "sum",
        //    "super", "tuple", "type", "unichr", "unicode", "vars", "xrange", "zip"};
        //// Most of these will be defined by my index search. However, for the missing ones, let's add them
        //// in. The following code computes the delta and produces a source-like string for it.
        //// It also counts the total symbol map size so we can pick a reasonable default:
        //List<String> asList = Arrays.asList(EXTRA_BUILTINS);
        //Set<String> asSet = new HashSet<String>(asList);
        //asSet.removeAll(symbols);
        //List<String> missing = new ArrayList<String>(asSet);
        //Collections.sort(missing);
        //int width = 0;
        //StringBuilder sb = new StringBuilder();
        //for (String s : missing) {
        //    sb.append('"');
        //    sb.append(s);
        //    sb.append('"');
        //    sb.append(',');
        //    sb.append(' ');
        //    width += s.length()+4;
        //    if (width > 70) {
        //        sb.append("\n");
        //        width = 0;
        //    }
        //}
        //String missingCode = "String[] MISSING = {\n" + sb.toString() + "\n};\n";
        //symbols.addAll(asList);
        //int requiredSetSize = symbols.size();

        return symbols;
    }

    public static boolean isBuiltinModule(String module) {
        return BUILTIN_MODULES.contains(module) || STUB_MISSING.equals(module);
    }

    @SuppressWarnings("unchecked")
    public Set<String> getImportsFor(String ident, boolean includeSymbol) {
        Set<String> modules = new HashSet<String>(10);

        final Set<SearchResult> result = new HashSet<SearchResult>();
        search(PythonIndexer.FIELD_MODULE_NAME, ident, NameKind.EXACT_NAME, result, ALL_SCOPE, Collections.singleton(PythonIndexer.FIELD_MODULE_NAME));
        for (SearchResult map : result) {
            String module = map.getValue(PythonIndexer.FIELD_MODULE_NAME);
            if (module != null) {
                // TODO - record more information about this, such as the FQN
                // so it's easier for the user to disambiguate
                modules.add(module);
            }
        }

        // TODO - handle case sensitivity better...
        String field = PythonIndexer.FIELD_ITEM;
        NameKind kind = NameKind.PREFIX; // We're storing encoded signatures so not exact matches

        Set<String> terms = new HashSet<String>(5);
        terms.add(PythonIndexer.FIELD_ITEM);
        terms.add(PythonIndexer.FIELD_MODULE_NAME);

        result.clear();
        search(field, ident, kind, result, ALL_SCOPE, terms);
        String match = ident + ";";

        MapSearch:
        for (SearchResult map : result) {
            String module = map.getValue(PythonIndexer.FIELD_MODULE_NAME);
            if (module == null) {
                continue;
            }

            if (module.indexOf('-') != -1) {
                // Don't include modules with -; these aren't real module
                // names (usually python scripts in directories containing a dash
                // that I incorrectly compute a module name for
                continue;
            }

            String[] members = map.getValues(PythonIndexer.FIELD_ITEM);
            if (members == null || members.length == 0) {
                continue;
            }

            int semi = match.length() - 1;

            for (String signature : members) {
                if (signature.startsWith(match)) {
                    if (includeSymbol) {
                        int flags = IndexedElement.decode(signature, semi + 3, 0);
                        if ((flags & IndexedElement.PRIVATE) != 0) {
                            // Skip private symbols - can't import those
                            continue;
                        }
                        String sig = ident;
                        char type = signature.charAt(semi + 1);
                        if (type == 'F') {
                            int sigStart = signature.indexOf(';', semi + 3) + 1;
                            int sigEnd = signature.indexOf(';', sigStart);
                            sig = ident + "(" + signature.substring(sigStart, sigEnd) + ")"; // NOI18N
                        } else if (type == 'I') {
                            // Don't provide modules that just -import- the symbol
                            continue;
                        }
                        if (!sig.equals(module)) {
                            modules.add(module + ": " + sig); // NOI18N
                        } else {
                            modules.add(module);
                        }
                    } else {
                        modules.add(module);
                    }
                    continue MapSearch;
                }
            }
        }

        return modules;
    }

    public Set<IndexedElement> getImportedElements(String prefix, NameKind kind, Set<SearchScope> scope,
            List<Import> imports, List<ImportFrom> importsFrom) {
        // TODO - separate methods from variables?? E.g. if you have method Foo() and class Foo
        // coming from different places


//        Set<String> imported = new HashSet<String>();
//
        Set<IndexedElement> elements = new HashSet<IndexedElement>();

        // Look up the imports and compute all the symbols we get from the import
        Set<String> modules = new HashSet<String>();

        // ImportsFrom require no index lookup
        for (ImportFrom from : importsFrom) {
            if (ImportManager.isFutureImport(from)) {
                continue;
            }
            List<alias> names = from.getInternalNames();
            if (names != null) {
                for (alias at : names) {
                    if ("*".equals(at.getInternalName())) { // NOI18N
                        modules.add(from.getInternalModule());
//                    } else {
//                        String name = at.getInternalAsname() != null ? at.getInternalAsname() : at.getInternalName();
//                        assert name.length() > 0;
//                        imported.add(name);
                    }
                }
            }
        }

//        for (Import imp : imports) {
//            if (imp.names != null) {
//                for (alias at : imp.getInternalNames()) {
//                    if (at.getInternalAsname() != null) {
//                        String name = at.getInternalAsname();
//                        assert name.length() > 0;
//                        imported.add(name);
//                    } else {
//                        imported.add(at.getInternalName());
//                    }
//                }
//            }
//        }
//
//
//        // Create variable items for the locally imported symbols
//        for (String name : imported) {
//            if (name.startsWith(prefix)) {
//                if (kind == NameKind.EXACT_NAME) {
//                    // Ensure that the method is not longer than the prefix
//                    if ((name.length() > prefix.length()) &&
//                            (name.charAt(prefix.length()) != '(') &&
//                            (name.charAt(prefix.length()) != ';')) {
//                        continue;
//                    }
//                } else {
//                    // REGEXP, CAMELCASE filtering etc. not supported here
//                    assert (kind == NameKind.PREFIX) ||
//                    (kind == NameKind.CASE_INSENSITIVE_PREFIX);
//                }
//    String url = null;
//                ElementKind elementKind = ElementKind.VARIABLE;
//                if (Character.isUpperCase(name.charAt(0))) {
//                    // Class?
//                    elementKind = ElementKind.CLASS;
//                }
//                IndexedElement element = new IndexedElement(name, elementKind, url, null);
//                element.setSmart(true);
//                elements.add(element);
//                // TODO - imported class symbls should be shown as classes!
//            }
//        }

        // Always include the current file as imported
        String moduleName = null;
        if (context != null) {
            moduleName = context.getName();
            modules.add(moduleName);
        }

        modules.addAll(BUILTIN_MODULES);

        addImportedElements(prefix, kind, scope, modules, elements, null);

        return elements;
    }
    static Map<String, Set<String>> wildcardImports = new HashMap<String, Set<String>>();

    public Set<String> getImportedFromWildcards(List<ImportFrom> importsFrom) {
        Set<String> symbols = new HashSet<String>(100);

        // Look up the imports and compute all the symbols we get from the import
        Set<String> modules = new HashSet<String>();

        // ImportsFrom require no index lookup
        for (ImportFrom from : importsFrom) {
            List<alias> names = from.getInternalNames();
            if (names != null) {
                for (alias at : names) {
                    if ("*".equals(at.getInternalName())) { // NOI18N
                        modules.add(from.getInternalModule());
                    }
                }
            }
        }

        Set<String> terms = new HashSet<String>(5);
        terms.add(PythonIndexer.FIELD_ITEM);
        terms.add(PythonIndexer.FIELD_MODULE_NAME);

        // Look up all symbols
        for (String module : modules) {
            // TODO - cache builtins?
            Set<String> moduleSymbols = symbols;
            boolean isSystem = isSystemModule(module);
            if (isSystem) {
                Set<String> s = wildcardImports.get(module);
                if (s != null) {
                    symbols.addAll(s);
                    continue;
                } else {
                    moduleSymbols = new HashSet<String>(100);
                }
            }


            final Set<SearchResult> result = new HashSet<SearchResult>();
            // TODO - handle case sensitivity better...

            search(PythonIndexer.FIELD_MODULE_NAME, module, NameKind.EXACT_NAME, result, ALL_SCOPE, terms);

            for (SearchResult map : result) {
                String[] items = map.getValues(PythonIndexer.FIELD_ITEM);
                if (items != null) {
                    for (String signature : items) {
                        int semi = signature.indexOf(';');
                        assert semi != -1;
                        int flags = IndexedElement.decode(signature, semi + 3, 0);
                        if ((flags & IndexedElement.PRIVATE) != 0) {
                            // Skip private symbols - can't import those
                            continue;
                        }

                        String name = signature.substring(0, semi);
                        moduleSymbols.add(name);
                    }
                }
            }

            if (isSystem) {
                assert moduleSymbols != symbols;
                symbols.addAll(moduleSymbols);
                wildcardImports.put(module, moduleSymbols);
            }
        }

        return symbols;
    }

    public Set<IndexedElement> getImportedElements(String prefix, NameKind kind, Set<SearchScope> scope,
            Set<String> modules, Set<String> systemModuleHolder) {
        Set<IndexedElement> elements = new HashSet<IndexedElement>();

        addImportedElements(prefix, kind, scope, modules, elements, systemModuleHolder);

        return elements;
    }
    static Set<String> systemModules;

    public boolean isSystemModule(String module) {
        if (systemModules == null) {
            systemModules = new HashSet<String>(800); // measured: 623
            Set<String> terms = new HashSet<String>(5);
            terms.add(PythonIndexer.FIELD_MODULE_ATTR_NAME);
            terms.add(PythonIndexer.FIELD_MODULE_NAME);
            final Set<SearchResult> result = new HashSet<SearchResult>();

            // This doesn't work because the attrs field isn't searchable:
            //search(PythonIndexer.FIELD_MODULE_ATTR_NAME, "S", NameKind.PREFIX, result, ALL_SCOPE, terms);
            //for (SearchResult map : result) {
            //    assert map.getValue(PythonIndexer.FIELD_MODULE_ATTR_NAME).indexOf("S") != -1;
            //    systemModules.add(map.getValue(PythonIndexer.FIELD_MODULE_NAME));
            //}

            search(PythonIndexer.FIELD_MODULE_NAME, "", NameKind.PREFIX, result, ALL_SCOPE, terms);

            for (SearchResult map : result) {
                String attrs = map.getValue(PythonIndexer.FIELD_MODULE_ATTR_NAME);
                if (attrs != null && attrs.indexOf('S') != -1) {
                    String mod = map.getValue(PythonIndexer.FIELD_MODULE_NAME);
                    systemModules.add(mod);
                }
            }
        }

        return systemModules.contains(module);
    }

    // TODO - make weak?
    static Set<String> availableClasses;

    public boolean isLowercaseClassName(String clz) {
        if (availableClasses == null) {
            availableClasses = new HashSet<String>(300); // measured: 193
            Set<String> terms = new HashSet<String>(5);
            terms.add(PythonIndexer.FIELD_CLASS_NAME);
            final Set<SearchResult> result = new HashSet<SearchResult>();

            search(PythonIndexer.FIELD_CLASS_NAME, "", NameKind.PREFIX, result, ALL_SCOPE, terms);

            for (SearchResult map : result) {
                String c = map.getValue(PythonIndexer.FIELD_CLASS_NAME);
                if (c != null && !Character.isUpperCase(c.charAt(0))) {
                    availableClasses.add(c);
                }
            }
        }

        return availableClasses.contains(clz);
    }

    public void addImportedElements(String prefix, NameKind kind, Set<SearchScope> scope,
            Set<String> modules, Set<IndexedElement> elements, Set<String> systemModuleHolder) {

        Set<String> terms = new HashSet<String>(5);
        terms.add(PythonIndexer.FIELD_ITEM);
        terms.add(PythonIndexer.FIELD_MODULE_ATTR_NAME);
        terms.add(PythonIndexer.FIELD_MODULE_NAME);

        // Look up all symbols
        for (String module : modules) {
            boolean isBuiltin = isBuiltinModule(module);
            boolean isSystem = isBuiltin;

            final Set<SearchResult> result = new HashSet<SearchResult>();
            // TODO - handle case sensitivity better...

            search(PythonIndexer.FIELD_MODULE_NAME, module, NameKind.EXACT_NAME, result, scope, terms);
            int prefixLength = prefix.length();

            for (SearchResult map : result) {
                String url = map.getPersistentUrl();
                String[] items = map.getValues(PythonIndexer.FIELD_ITEM);
                if (items != null) {
                    String attrs = map.getValue(PythonIndexer.FIELD_MODULE_ATTR_NAME);
                    if (attrs != null && attrs.indexOf('S') != -1) {
                        isSystem = true;
                    }
                    for (String signature : items) {
                        if (signature.startsWith(prefix)) {
                            if (kind == NameKind.EXACT_NAME) {
                                if (signature.charAt(prefixLength) != ';') {
                                    continue;
                                }
                            } else if (kind == NameKind.CASE_INSENSITIVE_PREFIX && !signature.regionMatches(true, 0, prefix, 0, prefix.length())) {
                                continue;
                            } else {
                                // REGEXP, CAMELCASE filtering etc. not supported here
                                assert (kind == NameKind.PREFIX) ||
                                        (kind == NameKind.CASE_INSENSITIVE_PREFIX);
                            }

                            IndexedElement element = IndexedElement.create(signature, module, url, null);
                            if (element.isPrivate()) {
                                continue;
                            }
                            if (isBuiltin) {
                                element.setRhs("<i>builtin</i>");
                            } else {
                                element.setSmart(true);
                            }
                            element.setInherited(true);
                            elements.add(element);
                        }
                    }
                }
            }

            if (systemModuleHolder != null && isSystem) {
                systemModuleHolder.add(module);
            }
        }
    }

    public Set<IndexedElement> getExceptions(String prefix, NameKind kind, Set<SearchScope> scope) {
        final Set<SearchResult> result = new HashSet<SearchResult>();
        Set<String> terms = new HashSet<String>();
        terms.add(PythonIndexer.FIELD_EXTENDS_NAME);
        terms.add(PythonIndexer.FIELD_CLASS_NAME);
        terms.add(PythonIndexer.FIELD_CLASS_ATTR_NAME);
        terms.add(PythonIndexer.FIELD_IN);
        search(PythonIndexer.FIELD_EXTENDS_NAME, "", NameKind.PREFIX, result, scope, terms); // NOI18N
        Map<String, String> extendsMap = new HashMap<String, String>(100);
        // First iteration: Compute inheritance hierarchy
        for (SearchResult map : result) {

            String superClass = map.getValue(PythonIndexer.FIELD_EXTENDS_NAME);
            if (superClass != null) {
                String clz = map.getValue(PythonIndexer.FIELD_CLASS_NAME);
                if (clz != null) {
                    extendsMap.put(clz, superClass);
                }
            }
        }

        // Compute set of classes that extend Exception

        Set<String> exceptionClasses = new HashSet<String>();
        Set<String> notExceptionClasses = new HashSet<String>();
        exceptionClasses.add("Exception"); // NOI18N
        Outer:
        for (String cls : extendsMap.keySet()) {
            if (notExceptionClasses.contains(cls)) {
                continue;
            } else if (!exceptionClasses.contains(cls)) {
                // See if this extends exception:
                String c = cls;
                int depth = 0;
                while (c != null) {
                    c = extendsMap.get(c);
                    String prev = null;
                    if (c != null) {
                        if (exceptionClasses.contains(c)) {
                            exceptionClasses.add(cls);
                            continue Outer;
                        }
                        depth++;
                        if (depth == 15) {
                            // we're probably going in circles, perhaps a extends b extends a.
                            // This doesn't really happen in Python, but can happen when there
                            // are unrelated classes with the same name getting treated as one here -
                            // class a in library X, and class a in library Y,
                            break;
                        }
                    } else if (prev != null) {
                        notExceptionClasses.add(prev);
                        break;
                    }
                }
                notExceptionClasses.add(cls);
            }
        }

        // Next add elements for all the exceptions
        final Set<IndexedElement> classes = new HashSet<IndexedElement>();
        for (SearchResult map : result) {
            String clz = map.getValue(PythonIndexer.FIELD_CLASS_NAME);
            if (clz == null || !exceptionClasses.contains(clz)) {
                continue;
            }

            if ((kind == NameKind.PREFIX) && !clz.startsWith(prefix)) {
                continue;
            } else if (kind == NameKind.CASE_INSENSITIVE_PREFIX && !clz.regionMatches(true, 0, prefix, 0, prefix.length())) {
                continue;
            } else if (kind == NameKind.EXACT_NAME && !clz.equals(prefix)) {
                continue;
            }

            String url = map.getPersistentUrl();
            String module = map.getValue(PythonIndexer.FIELD_IN);
            IndexedElement element = new IndexedElement(clz, ElementKind.CLASS, url, module, null, null);
            String attrs = map.getValue(PythonIndexer.FIELD_CLASS_ATTR_NAME);
            if (attrs != null) {
                int flags = IndexedElement.decode(attrs, 0, 0);
                element.setFlags(flags);
            }
            classes.add(element);
        }

        return classes;
    }

    /** Find the subclasses of the given class name, with the POSSIBLE fqn from the
     * context of the usage. */
    public Set<IndexedElement> getSubClasses(String fqn, String possibleFqn, String name, boolean directOnly) {
        //String field = PythonIndexer.FIELD_FQN_NAME;
        Set<IndexedElement> classes = new HashSet<IndexedElement>();
        Set<String> scannedClasses = new HashSet<String>();
        Set<String> seenClasses = new HashSet<String>();

        if (fqn != null) {
            addSubclasses(fqn, classes, seenClasses, scannedClasses, directOnly);
        } else {
            fqn = possibleFqn;
            if (name.equals(possibleFqn)) {
                fqn = null;
            }

            // Try looking at the libraries too
            while ((classes.size() == 0) && (fqn != null && fqn.length() > 0)) {
                // TODO - use the boolvalue from addclasses instead!
                boolean found = addSubclasses(fqn + "::" + name, classes, seenClasses, scannedClasses, directOnly);
                if (found) {
                    return classes;
                }

                int f = fqn.lastIndexOf("::");

                if (f == -1) {
                    break;
                } else {
                    fqn = fqn.substring(0, f);
                }
            }

            if (classes.size() == 0) {
                addSubclasses(name, classes, seenClasses, scannedClasses, directOnly);
            }
        }

        return classes;
    }

    private boolean addSubclasses(String classFqn,
            Set<IndexedElement> classes, Set<String> seenClasses, Set<String> scannedClasses, boolean directOnly) {
        // Prevent problems with circular includes or redundant includes
        if (scannedClasses.contains(classFqn)) {
            return false;
        }

        scannedClasses.add(classFqn);

        String searchField = PythonIndexer.FIELD_EXTENDS_NAME;

        Set<SearchResult> result = new HashSet<SearchResult>();

        Set<String> terms = new HashSet<String>(5);
        terms.add(PythonIndexer.FIELD_IN);
        terms.add(PythonIndexer.FIELD_EXTENDS_NAME);
        terms.add(PythonIndexer.FIELD_CLASS_ATTR_NAME);
        terms.add(PythonIndexer.FIELD_CLASS_NAME);

        search(searchField, classFqn, NameKind.EXACT_NAME, result, ALL_SCOPE, terms);

        boolean foundIt = result.size() > 0;

        // If this is a bogus class entry (no search rsults) don't continue
        if (!foundIt) {
            return foundIt;
        }

        for (SearchResult map : result) {
            String className = map.getValue(PythonIndexer.FIELD_CLASS_NAME);
            if (className != null && !seenClasses.contains(className)) {
                String url = map.getPersistentUrl();
                String module = map.getValue(PythonIndexer.FIELD_IN);
                IndexedElement clz = new IndexedElement(className, ElementKind.CLASS, url, module, null, null);
                String attrs = map.getValue(PythonIndexer.FIELD_CLASS_ATTR_NAME);
                if (attrs != null) {
                    int flags = IndexedElement.decode(attrs, 0, 0);
                    clz.setFlags(flags);
                }
                classes.add(clz);

                seenClasses.add(className);

                if (!directOnly) {
                    addSubclasses(className, classes, seenClasses, scannedClasses, directOnly);
                }
            }
        }

        return foundIt;
    }
    private static String clusterUrl = null;

    // For testing only
    public static void setClusterUrl(String url) {
        clusterUrl = url;
    }

    static String getPreindexUrl(String url) {
        // TODO - look up the correct platform to use!
        final PythonPlatformManager manager = PythonPlatformManager.getInstance();
        final String platformName = manager.getDefaultPlatform();
        PythonPlatform platform = manager.getPlatform(platformName);
        if (platform != null) {
            String s = platform.getHomeUrl();
            if (s != null) {
                if (url.startsWith(s)) {
                    url = PYTHONHOME_URL + url.substring(s.length());
                    return url;
                }
            }
        }

        String s = getClusterUrl();

        if (url.startsWith(s)) {
            return CLUSTER_URL + url.substring(s.length());
        }

        if (url.startsWith("jar:file:")) { // NOI18N
           String sub = url.substring(4);
            if (sub.startsWith(s)) {
                return CLUSTER_URL + sub.substring(s.length());
            }
        }

        return url;
    }

    /** Get the FileObject corresponding to a URL returned from the index */
    public static FileObject getFileObject(String url) {
        return getFileObject(url, null);
    }

    public static FileObject getFileObject(String url, FileObject context) {
        try {
            if (url.startsWith(PYTHONHOME_URL)) {
                Iterator<String> it = null;

                // TODO - look up the right platform for the given project
                //if (context != null) {
                //    Project project = FileOwnerQuery.getOwner(context);
                //    if (project != null) {
                //        PythonPlatform platform = PythonPlatform.platformFor(project);
                //        if (platform != null) {
                //            it = Collections.singleton(platform).iterator();
                //        }
                //    }
                //}

                PythonPlatformManager manager = PythonPlatformManager.getInstance();
                if (it == null) {
                    it = manager.getPlatformList().iterator();
                }
                while (it.hasNext()) {
                    String name = it.next();
                    PythonPlatform platform = manager.getPlatform(name);
                    if (platform != null) {
                        String u = platform.getHomeUrl();
                        if (u != null) {
                            try {
                                u = u + url.substring(PYTHONHOME_URL.length());
                                FileObject fo = URLMapper.findFileObject(new URL(u));
                                if (fo != null) {
                                    return fo;
                                }
                            } catch (MalformedURLException mue) {
                                Exceptions.printStackTrace(mue);
                            }
                        }
                    }
                }

                return null;
            } else if (url.startsWith(CLUSTER_URL)) {
                url = getClusterUrl() + url.substring(CLUSTER_URL.length()); // NOI18N
                if (url.indexOf(".egg!/") != -1) { // NOI18N
                    url = "jar:" + url; // NOI18N
                }
            }

            return URLMapper.findFileObject(new URL(url));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return null;
    }

    static String getClusterUrl() {
        if (clusterUrl == null) {
            File f =
                    InstalledFileLocator.getDefault().locate("modules/org-netbeans-modules-python-editor.jar", null, false); // NOI18N

            if (f == null) {
                throw new RuntimeException("Can't find cluster");
            }

            f = new File(f.getParentFile().getParentFile().getAbsolutePath());

            try {
                f = f.getCanonicalFile();
                clusterUrl = f.toURI().toURL().toExternalForm();
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }

        return clusterUrl;
    }
}
