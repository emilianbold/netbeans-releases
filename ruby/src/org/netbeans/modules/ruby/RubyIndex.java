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
package org.netbeans.modules.ruby;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.netbeans.api.gsf.Index;
import static org.netbeans.api.gsf.Index.*;
import org.netbeans.api.gsf.Modifier;
import org.netbeans.api.gsf.NameKind;
import org.netbeans.api.ruby.platform.RubyInstallation;
import org.netbeans.modules.ruby.elements.IndexedClass;
import org.netbeans.modules.ruby.elements.IndexedElement;
import org.netbeans.modules.ruby.elements.IndexedMethod;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;


/**
 * Access to the index of known Ruby classes - core, libraries, gems, user projects, etc.
 *
 * @todo Pull out attributes, fields and constants from the index as well
 * @todo Store signature attributes for methods: private/protected?, documented?, returntype?
 * @todo When there are multiple method/field definitions, pick access level from one which sets it
 * @author Tor Norbye
 */
public final class RubyIndex {
    public static final String UNKNOWN_CLASS = "<Unknown>"; // NOI18N
    public static final String OBJECT = "Object"; // NOI18N
    private static final String CLASS = "Class"; // NOI18N
    private static final String MODULE = "Module"; // NOI18N
    static final Set<SearchScope> ALL_SCOPE = EnumSet.allOf(SearchScope.class);
    static final Set<SearchScope> SOURCE_SCOPE = EnumSet.of(SearchScope.SOURCE);
    private static String clusterUrl = null;
    private static final String CLUSTER_URL = "cluster:"; // NOI18N
    private static final String RUBYHOME_URL = "ruby:"; // NOI18N
    private final Index index;

    /** Creates a new instance of RubyIndex */
    public RubyIndex(Index index) {
        this.index = index;
    }

    public static RubyIndex get(Index index) {
        return new RubyIndex(index);
    }

    private boolean search(String key, String name, NameKind kind, Set<SearchResult> result) {
        try {
            index.gsfSearch(key, name, kind, ALL_SCOPE, result);

            return true;
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);

            return false;
        }
    }

    private boolean search(String key, String name, NameKind kind, Set<SearchResult> result,
        Set<SearchScope> scope) {
        try {
            index.gsfSearch(key, name, kind, scope, result);

            return true;
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);

            return false;
        }
    }

    Set<IndexedClass> getClasses(String name, final NameKind kind, boolean includeAll,
        boolean skipClasses, boolean skipModules) {
        return getClasses(name, kind, includeAll, skipClasses, skipModules, ALL_SCOPE);
    }

    /**
     * Return the full set of classes that match the given name.
     *
     * @param name The name of the class - possibly a fqn like File::Stat, or just a class
     *   name like Stat, or just a prefix like St.
     * @param kind Whether we want the exact name, or whether we're searching by a prefix.
     * @param includeAll If true, return multiple RuIndexedClassnstances for the same logical
     *   class, one for each declaration point. For example, File is defined both in the
     *   builtin stubs as well as in ftools.
     */
    public Set<IndexedClass> getClasses(String name, final NameKind kind, boolean includeAll,
        boolean skipClasses, boolean skipModules, Set<Index.SearchScope> scope) {
        String classFqn = null;

        if (name != null) {
            if (name.indexOf("::") != -1) { // NOI18N

                int p = name.lastIndexOf("::"); // NOI18N
                classFqn = name.substring(0, p);
                name = name.substring(p + 2);
            } else if (name.endsWith(":")) {
                // User has typed something like "Test:" and wants completion on
                // for something like Test::Unit
                classFqn = name.substring(0, name.length() - 1);
                name = "";
            }
        }

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
            field = RubyIndexer.FIELD_CLASS_NAME;

            break;

        case CASE_INSENSITIVE_PREFIX:
        case CASE_INSENSITIVE_REGEXP:
            field = RubyIndexer.FIELD_CASE_INSENSITIVE_CLASS_NAME;

            break;

        default:
            throw new UnsupportedOperationException(kind.toString());
        }

        search(field, name, kind, result, scope);

        // TODO Prune methods to fit my scheme - later make lucene index smarter about how to prune its index search
        final Set<String> uniqueClasses;

        if (includeAll) {
            uniqueClasses = null;
        } else {
            uniqueClasses = new HashSet<String>(); // TODO : init inside includeAll check
        }

        final Set<IndexedClass> classes = new HashSet<IndexedClass>();

        for (SearchResult map : result) {
            String clz = map.getValue(RubyIndexer.FIELD_CLASS_NAME);

            if (clz == null) {
                // It's probably a module
                // XXX I need to handle this... for now punt
                continue;
            }

            // Lucene returns some inexact matches, TODO investigate why this is necessary
            if ((kind == NameKind.PREFIX) && !clz.startsWith(name)) {
                continue;
            }

            if ((classFqn != null) && !classFqn.equals(map.getValue(RubyIndexer.FIELD_IN))) {
                continue;
            }

            String attrs = map.getValue(RubyIndexer.FIELD_CLASS_ATTRS);
            boolean isClass = attrs.charAt(0) == 'C';

            if (skipClasses && isClass) {
                continue;
            }

            if (skipModules && !isClass) {
                continue;
            }

            String fqn = map.getValue(RubyIndexer.FIELD_FQN_NAME);

            // Only return a single instance for this signature
            if (!includeAll) {
                if (uniqueClasses.contains(fqn)) { // use a map to point right to the class
                                                   // Prefer the instance that provides documentation

                    boolean replaced = false;
                    int documentedAt = attrs.indexOf('d');
                    boolean isDocumented = documentedAt != -1;

                    if (isDocumented) {
                        // Check the actual size of the documentation, and prefer the largest
                        // method
                        int length = 0;
                        documentedAt = attrs.indexOf('(', documentedAt + 1);

                        if (documentedAt != -1) {
                            length = Integer.parseInt(attrs.substring(documentedAt + 1,
                                        attrs.indexOf(')', documentedAt + 1)));
                        }

                        // This instance is documented. Replace the other instance...
                        for (IndexedClass c : classes) {
                            if (c.getSignature().equals(fqn) &&
                                    (length > c.getDocumentationLength())) {
                                classes.remove(c);
                                replaced = true;

                                break;
                            }
                        }
                    }

                    if (!replaced) {
                        continue;
                    }
                } else {
                    uniqueClasses.add(fqn);
                }
            }

            classes.add(createClass(fqn, clz, map));
        }

        return classes;
    }

    /**
     * Return a set of methods that match the given name prefix, and are in the given
     * class and module. If no class is specified, match methods across all classes.
     * Note that inherited methods are not checked. If you want to match inherited methods
     * you must call this method on each superclass as well as the mixin modules.
     */
    @SuppressWarnings("unchecked") // unchecked - lucene has source 1.4

    Set<IndexedMethod> getMethods(final String name, final String clz, NameKind kind) {
        return getMethods(name, clz, kind, ALL_SCOPE);
    }

    @SuppressWarnings("fallthrough")
    public Set<IndexedMethod> getMethods(final String name, final String clz, NameKind kind,
        Set<Index.SearchScope> scope) {
        //    public void searchByCriteria(final String name, final ClassIndex.NameKind kind, /*final ResultConvertor<T> convertor,*/ final Set<String> result) throws IOException {
        final Set<SearchResult> result = new HashSet<SearchResult>();

        //        if (!isValid()) {
        //            LOGGER.fine(String.format("LuceneIndex[%s] is invalid!\n", this.toString()));
        //            return;
        //        }
        String field = RubyIndexer.FIELD_METHOD_NAME;
        NameKind originalKind = kind;
        if (kind == NameKind.EXACT_NAME) {
            // I can't do exact searches on methods because the method
            // entries include signatures etc. So turn this into a prefix
            // search and then compare chopped off signatures with the name
            kind = NameKind.PREFIX;
        }

        // No point in doing case insensitive searches on method names because
        // method names in Ruby are always case insensitive anyway
        //            case CASE_INSENSITIVE_PREFIX:
        //            case CASE_INSENSITIVE_REGEXP:
        //                field = RubyIndexer.FIELD_CASE_INSENSITIVE_METHOD_NAME;
        //                break;

        search(field, name, kind, result, scope);

        //return Collections.unmodifiableSet(result);

        // TODO Prune methods to fit my scheme - later make lucene index smarter about how to prune its index search
        final Set<IndexedMethod> methods = new HashSet<IndexedMethod>();

        for (SearchResult map : result) {
            if (clz != null) {
                String fqn = map.getValue(RubyIndexer.FIELD_FQN_NAME);

                if (!(clz.equals(fqn))) {
                    continue;
                }
            }

            String[] signatures = map.getValues(RubyIndexer.FIELD_METHOD_NAME);

            if (signatures != null) {
                for (String signature : signatures) {
                    // Skip weird methods... Think harder about this
                    if (((name == null) || (name.length() == 0)) &&
                            !Character.isLowerCase(signature.charAt(0))) {
                        continue;
                    }

                    // Lucene returns some inexact matches, TODO investigate why this is necessary
                    if ((kind == NameKind.PREFIX) && !signature.startsWith(name)) {
                        continue;
                    } else if (originalKind == NameKind.EXACT_NAME) {
                        // Make sure the name matches exactly
                        // We know that the prefix is correct from the first part of
                        // this if clause, by the signature may have more
                        if (((signature.length() > name.length()) &&
                                (signature.charAt(name.length()) != '(')) &&
                                (signature.charAt(name.length()) != ':')) {
                            continue;
                        }
                    }

                    // XXX THIS DOES NOT WORK WHEN THERE ARE IDENTICAL SIGNATURES!!!
                    assert map != null;
                    methods.add(createMethod(signature, map));
                }
            }

            String[] attributes = map.getValues(RubyIndexer.FIELD_ATTRIBUTE_NAME);

            if (attributes != null) {
                for (String signature : attributes) {
                    // Skip weird methods... Think harder about this
                    if (((name == null) || (name.length() == 0)) &&
                            !Character.isLowerCase(signature.charAt(0))) {
                        continue;
                    }

                    // Lucene returns some inexact matches, TODO investigate why this is necessary
                    if ((kind == NameKind.PREFIX) && !signature.startsWith(name)) {
                        continue;
                    } else if (originalKind == NameKind.EXACT_NAME) {
                        // Make sure the name matches exactly
                        // We know that the prefix is correct from the first part of
                        // this if clause, by the signature may have more
                        if (((signature.length() > name.length()) &&
                                //(signature.charAt(name.length()) != '(')) &&
                                (signature.charAt(name.length()) != ':'))) {
                            continue;
                        }
                    }

                    // XXX THIS DOES NOT WORK WHEN THERE ARE IDENTICAL SIGNATURES!!!
                    assert map != null;
                    // Create method for the attribute
                    methods.add(createMethod(signature, map));
                }
            }
            
            // TODO - fields
        }

        return methods;
    }

    private IndexedMethod createMethod(String signature, SearchResult map) {
        String clz = map.getValue(RubyIndexer.FIELD_CLASS_NAME);
        String module = map.getValue(RubyIndexer.FIELD_IN);

        if (clz == null) {
            // Module method?
            clz = module;
        } else if ((module != null) && (module.length() > 0)) {
            clz = module + "::" + clz;
        }

        String fileUrl = map.getValue(RubyIndexer.FIELD_FILENAME);
        String fqn = map.getValue(RubyIndexer.FIELD_FQN_NAME);
        String require = map.getValue(RubyIndexer.FIELD_REQUIRE);

        // Extract attributes
        int attributeIndex = signature.indexOf(':');
        Set<Modifier> modifiers;
        String attributes = null;

        if (attributeIndex != -1) {
            modifiers = RubyIndexer.getModifiersFromString(signature, attributeIndex);

            if (signature.length() > attributeIndex+1) {
                attributes = signature.substring(attributeIndex+1, signature.length());
            }

            signature = signature.substring(0, attributeIndex);
        } else {
            modifiers = Collections.emptySet();
        }

        IndexedMethod m =
            IndexedMethod.create(this, signature, fqn, clz, fileUrl, require, modifiers, attributes);

        return m;
    }

    private IndexedClass createClass(String fqn, String clz, SearchResult map) {
        String require = map.getValue(RubyIndexer.FIELD_REQUIRE);

        // TODO - how do I determine -which- file to associate with the file?
        // Perhaps the one that defines initialize() ?
        String fileUrl = map.getValue(RubyIndexer.FIELD_FILENAME);

        if (clz == null) {
            clz = map.getValue(RubyIndexer.FIELD_CLASS_NAME);
        }

        String attrs = map.getValue(RubyIndexer.FIELD_CLASS_ATTRS);
        boolean isModule = attrs.charAt(0) == 'm';
        Set<Modifier> modifiers = Collections.emptySet();
        IndexedClass c =
            IndexedClass.create(this, clz, fqn, fileUrl, require, isModule, modifiers, attrs);

        return c;
    }

    // List of String[2]: 0: requirename, 1: fqn
    public Set<String[]> getRequires(final String name, final NameKind kind) {
        final Set<SearchResult> result = new HashSet<SearchResult>();

        String field = RubyIndexer.FIELD_REQUIRE;

        search(field, name, kind, result);

        // TODO Prune methods to fit my scheme - later make lucene index smarter about how to prune its index search
        final Map<String, String> fqns = new HashMap<String, String>();

        for (SearchResult map : result) {
            String[] r = map.getValues(field);

            if (r != null) {
                for (String require : r) {
                    // Lucene returns some inexact matches, TODO investigate why this is necessary
                    if ((kind == NameKind.PREFIX) && !require.startsWith(name)) {
                        continue;
                    }
                    assert map != null;

                    // TODO - check if there's a rubygem which captures this
                    // require and if so, remove it
                    String fqn = map.getValue(RubyIndexer.FIELD_FQN_NAME);

                    String there = fqns.get(require);

                    if ((fqn != null) &&
                            ((there == null) ||
                            ((there != null) && (there.length() < fqn.length())))) {
                        fqns.put(require, fqn);
                    }
                }
            }
        }

        final Set<String[]> requires = new HashSet<String[]>();

        for (String require : fqns.keySet()) {
            String fqn = fqns.get(require);
            String[] item = new String[2];
            item[0] = require;
            item[1] = fqn;
            requires.add(item);
        }

        return requires;
    }

    public Set<String> getRequiresTransitively(Set<String> requires) {
        // Not yet implemented - this requires me to index the require-statements in the files
        return requires;
    }
    
    // List of String[2]: 0: requirename, 1: fqn
    public Set<String> getClassesIn(final String require) {
        final Set<SearchResult> result = new HashSet<SearchResult>();

        String field = RubyIndexer.FIELD_REQUIRE;

        search(field, require, NameKind.EXACT_NAME, result);

        final Set<String> fqns = new HashSet<String>();
        
        for (SearchResult map : result) {
            String fqn = map.getValue(RubyIndexer.FIELD_FQN_NAME);
            
            if (fqn != null) {
                fqns.add(fqn);
            }
        }

        return fqns;
    }

    public IndexedClass getSuperclass(String fqn) {
        final Set<SearchResult> result = new HashSet<SearchResult>();

        NameKind kind = NameKind.EXACT_NAME;
        String field = RubyIndexer.FIELD_FQN_NAME;

        search(field, fqn, kind, result);

        for (SearchResult map : result) {
            assert fqn.equals(map.getValue(RubyIndexer.FIELD_FQN_NAME));

            String extendsClass = map.getValue(RubyIndexer.FIELD_EXTENDS_NAME);

            if (extendsClass != null) {
                // Found the class name, now look it up in the index
                result.clear();

                if (!search(field, extendsClass, kind, result)) {
                    return null;
                }

                // There should be exactly one match
                if (result.size() > 0) {
                    SearchResult superMap = result.iterator().next();
                    String superFqn = superMap.getValue(RubyIndexer.FIELD_FQN_NAME);

                    return createClass(superFqn, extendsClass, superMap);
                } else {
                    return null;
                }
            }
        }

        return null;
    }

    private boolean addSubclasses(String classFqn,
        Set<IndexedClass> classes, Set<String> seenClasses, Set<String> scannedClasses, boolean directOnly) {
        // Prevent problems with circular includes or redundant includes
        if (scannedClasses.contains(classFqn)) {
            return false;
        }

        scannedClasses.add(classFqn);

        String searchField = RubyIndexer.FIELD_EXTENDS_NAME;

        Set<SearchResult> result = new HashSet<SearchResult>();

        search(searchField, classFqn, NameKind.EXACT_NAME, result);

        boolean foundIt = result.size() > 0;

        // If this is a bogus class entry (no search rsults) don't continue
        if (!foundIt) {
            return foundIt;
        }

        for (SearchResult map : result) {
            String fqn = map.getValue(RubyIndexer.FIELD_FQN_NAME);
            if (!seenClasses.contains(fqn)) {
                IndexedClass clz = createClass(fqn, null, map);
                classes.add(clz);
                seenClasses.add(fqn);

                if (!directOnly) {
                    addSubclasses(fqn, classes, seenClasses, scannedClasses, directOnly);
                }
            }
        }
        
        return foundIt;
    }
    
    
    /** Find the subclasses of the given class name, with the POSSIBLE fqn from the
     * context of the usage. */
    public Set<IndexedClass> getSubClasses(String fqn, String possibleFqn, String name, boolean directOnly) {
        //String field = RubyIndexer.FIELD_FQN_NAME;
        Set<IndexedClass> classes = new HashSet<IndexedClass>();
        Set<String> scannedClasses = new HashSet<String>();
        Set<String> seenClasses = new HashSet<String>();
        
        if (fqn != null) {
            addSubclasses(fqn, classes, seenClasses, scannedClasses, directOnly);
        } else {
            fqn = possibleFqn;

            // Try looking at the libraries too
            while ((classes.size() == 0) && (fqn.length() > 0)) {
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
    
    /** Return the most distant method in the hierarchy that is overriding the given method, or null
     * @todo Make this method actually compute most distant ancestor
     * @todo Use arglist arity comparison to reject methods that are not overrides...
     */
    public IndexedMethod getOverridingMethod(String className, String methodName) {
        Set<IndexedMethod> methods = getInheritedMethods(className, methodName, NameKind.EXACT_NAME);

        // TODO - this is only returning ONE match, not the most distant one. I really need to
        // produce a RubyIndex method for this which can walk in there and do a decent job!
        
        for (IndexedMethod method : methods) {
            // getInheritedMethods may return methods ON fqn itself
            if (!method.getIn().equals(className)) {
                return method;
            }
        }
        
        return null;
    }

    /**
     * Get the set of inherited (through super classes and mixins) for the given fully qualified class name.
     * @param classFqn FQN: module1::module2::moduleN::class
     * @param prefix If kind is NameKind.PREFIX/CASE_INSENSITIVE_PREFIX, a prefix to filter methods by. Else,
     *    if kind is NameKind.EXACT_NAME filter methods by the exact name.
     * @param kind Whether the prefix field should be taken as a prefix or a whole name
     */
    public Set<IndexedMethod> getInheritedMethods(String classFqn, String prefix, NameKind kind) {
        boolean haveRedirected = false;

        if ((classFqn == null) || classFqn.equals(OBJECT)) {
            // Redirect inheritance tree to Class to pick up methods in Class and Module
            classFqn = CLASS;
            haveRedirected = true;
        } else if (MODULE.equals(classFqn) || CLASS.equals(classFqn)) {
            haveRedirected = true;
        }

        //String field = RubyIndexer.FIELD_FQN_NAME;
        Set<IndexedMethod> methods = new HashSet<IndexedMethod>();
        Set<String> scannedClasses = new HashSet<String>();
        Set<String> seenSignatures = new HashSet<String>();

        if (prefix == null) {
            prefix = "";
        }

        addMethodsFromClass(prefix, kind, classFqn, methods, seenSignatures, scannedClasses,
            haveRedirected);

        return methods;
    }

    /** Return whether the specific class referenced (classFqn) was found or not. This is
     * not the same as returning whether any classes were added since it may add
     * additional methods from parents (Object/Class).
     */
    private boolean addMethodsFromClass(String prefix, NameKind kind, String classFqn,
        Set<IndexedMethod> methods, Set<String> seenSignatures, Set<String> scannedClasses,
        boolean haveRedirected) {
        // Prevent problems with circular includes or redundant includes
        if (scannedClasses.contains(classFqn)) {
            return false;
        }

        scannedClasses.add(classFqn);

        String searchField = RubyIndexer.FIELD_FQN_NAME;

        Set<SearchResult> result = new HashSet<SearchResult>();

        search(searchField, classFqn, NameKind.EXACT_NAME, result);

        boolean foundIt = result.size() > 0;

        // If this is a bogus class entry (no search rsults) don't continue
        if (!foundIt) {
            return foundIt;
        }

        String extendsClass = null;

        String classIn = null;
        int fqnIndex = classFqn.lastIndexOf("::"); // NOI18N

        if (fqnIndex != -1) {
            classIn = classFqn.substring(0, fqnIndex);
        }

        for (SearchResult map : result) {
            assert map != null;

            if (extendsClass == null) {
                extendsClass = map.getValue(RubyIndexer.FIELD_EXTENDS_NAME);
            }

            String includes = map.getValue(RubyIndexer.FIELD_INCLUDES);

            if (includes != null) {
                String[] in = includes.split(",");

                // I have Util::BacktraceFilter and Assertions, which are both
                // relative to ::,Test,Test::Unit
                for (String include : in) {
                    // Try both with and without a package qualifier
                    boolean isQualified = false;

                    if (classIn != null) {
                        isQualified = addMethodsFromClass(prefix, kind, classIn + "::" + include,
                                methods, seenSignatures, scannedClasses, haveRedirected);
                    }

                    if (!isQualified) {
                        addMethodsFromClass(prefix, kind, include, methods, seenSignatures,
                            scannedClasses, haveRedirected);
                    }
                }
            }

            String extendWith = map.getValue(RubyIndexer.FIELD_EXTEND_WITH);

            if (extendWith != null) {
                // Try both with and without a package qualifier
                boolean isQualified = false;

                if (classIn != null) {
                    isQualified = addMethodsFromClass(prefix, kind, classIn + "::" + extendWith,
                            methods, seenSignatures, scannedClasses, haveRedirected);
                }

                if (!isQualified) {
                    addMethodsFromClass(prefix, kind, extendWith, methods, seenSignatures,
                        scannedClasses, haveRedirected);
                }
            }
            
            String[] signatures = map.getValues(RubyIndexer.FIELD_METHOD_NAME);

            if (signatures != null) {
                for (String signature : signatures) {
                    // Skip weird methods like "[]" etc. in completion lists... TODO Think harder about this
                    if ((prefix.length() == 0) && !Character.isLowerCase(signature.charAt(0))) {
                        continue;
                    }

                    // Prevent duplicates when method is redefined
                    if (!seenSignatures.contains(signature)) {
                        if (signature.startsWith(prefix)) {
                            if (kind == NameKind.EXACT_NAME) {
                                // Ensure that the method is not longer than the prefix
                                if ((signature.length() > prefix.length()) &&
                                        (signature.charAt(prefix.length()) != '(') &&
                                        (signature.charAt(prefix.length()) != ':')) {
                                    continue;
                                }
                            } else {
                                // REGEXP, CAMELCASE filtering etc. not supported here
                                assert (kind == NameKind.PREFIX) ||
                                (kind == NameKind.CASE_INSENSITIVE_PREFIX);
                            }

                            seenSignatures.add(signature);

                            IndexedMethod method = createMethod(signature, map);
                            method.setSmart(!haveRedirected);
                            methods.add(method);
                        }
                    }
                }
            }

            String[] attributes = map.getValues(RubyIndexer.FIELD_ATTRIBUTE_NAME);

            if (attributes != null) {
                for (String attribute : attributes) {
                    // Skip weird methods like "[]" etc. in completion lists... TODO Think harder about this
                    if ((prefix.length() == 0) && !Character.isLowerCase(attribute.charAt(0))) {
                        continue;
                    }

                    // Prevent duplicates when method is redefined
                    if (!seenSignatures.contains(attribute)) {
                        if (attribute.startsWith(prefix)) {
                            if (kind == NameKind.EXACT_NAME) {
                                // Ensure that the method is not longer than the prefix
                                if ((attribute.length() > prefix.length()) &&
                                        (attribute.charAt(prefix.length()) != '(') &&
                                        (attribute.charAt(prefix.length()) != ':')) {
                                    continue;
                                }
                            } else {
                                // REGEXP, CAMELCASE filtering etc. not supported here
                                assert (kind == NameKind.PREFIX) ||
                                (kind == NameKind.CASE_INSENSITIVE_PREFIX);
                            }

                            seenSignatures.add(attribute);

                            // TODO - create both getter and setter methods
                            IndexedMethod method = createMethod(attribute, map);
                            method.setSmart(!haveRedirected);
                            method.setAttribute(true);
                            methods.add(method);
                        }
                    }
                }
            }

//            String[] fields = map.getValues(RubyIndexer.FIELD_FIELD_NAME);
//
//            if (fields != null) {
//                for (String field : fields) {
//                    // Skip weird methods like "[]" etc. in completion lists... TODO Think harder about this
//                    if ((prefix.length() == 0) && !Character.isLowerCase(field.charAt(0))) {
//                        continue;
//                    }
//
//                    // Prevent duplicates when method is redefined
//                    if (!seenSignatures.contains(field)) {
//                        if (field.startsWith(prefix)) {
//                            if (kind == NameKind.EXACT_NAME) {
//                                // Ensure that the method is not longer than the prefix
//                                if ((field.length() > prefix.length()) &&
//                                        (field.charAt(prefix.length()) != '(') &&
//                                        (field.charAt(prefix.length()) != ':')) {
//                                    continue;
//                                }
//                            } else {
//                                // REGEXP, CAMELCASE filtering etc. not supported here
//                                assert (kind == NameKind.PREFIX) ||
//                                (kind == NameKind.CASE_INSENSITIVE_PREFIX);
//                            }
//
//                            seenSignatures.add(field);
//
//                            IndexedField f = createField(field, map);
//                            f.setSmart(!haveRedirected);
//                            methods.add(f);
//                        }
//                    }
//                }
//            }
        }
        
        if (classFqn.equals(OBJECT)) {
            return foundIt;
        }

        if (extendsClass == null) {
            if (haveRedirected) {
                addMethodsFromClass(prefix, kind, OBJECT, methods, seenSignatures, scannedClasses,
                    true);
            } else {
                // Rather than inheriting directly from object,
                // let's go via Class (and Module) up to Object
                addMethodsFromClass(prefix, kind, CLASS, methods, seenSignatures, scannedClasses,
                    true);
            }
        } else {
            // We're not sure we have a fully qualified path, so try some different candidates
            if (!addMethodsFromClass(prefix, kind, extendsClass, methods, seenSignatures,
                        scannedClasses, haveRedirected)) {
                // Search by classIn 
                String fqn = classIn;

                while (fqn != null) {
                    if (addMethodsFromClass(prefix, kind, fqn + "::" + extendsClass, methods,
                                seenSignatures, scannedClasses, haveRedirected)) {
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

        return foundIt;
    }

    /** Return all the method or class definitions for the given FQN that are documented. */
    public Set<?extends IndexedElement> getDocumented(final String fqn) {
        assert (fqn != null) && (fqn.length() > 0);

        int index = fqn.indexOf('#');

        if (index == -1) {
            // Looking for a class or a module
            return getDocumentedClasses(fqn);
        } else {
            // Looking for a method
            String clz = fqn.substring(0, index);
            String method = fqn.substring(index + 1);

            return getDocumentedMethods(clz, method);
        }
    }

    private Set<IndexedClass> getDocumentedClasses(final String fqn) {
        final Set<SearchResult> result = new HashSet<SearchResult>();
        String field = RubyIndexer.FIELD_FQN_NAME;

        search(field, fqn, NameKind.EXACT_NAME, result);

        Set<IndexedClass> matches = new HashSet<IndexedClass>();

        for (SearchResult map : result) {
            assert map != null;

            String attributes = map.getValue(RubyIndexer.FIELD_CLASS_ATTRS);

            if (attributes.indexOf('d') != -1) {
                matches.add(createClass(fqn, null, map));
            }
        }

        return matches;
    }

    private Set<IndexedMethod> getDocumentedMethods(final String fqn, String method) {
        final Set<SearchResult> result = new HashSet<SearchResult>();
        String field = RubyIndexer.FIELD_FQN_NAME;

        search(field, fqn, NameKind.EXACT_NAME, result);

        Set<IndexedMethod> matches = new HashSet<IndexedMethod>();

        for (SearchResult map : result) {
            String[] signatures = map.getValues(RubyIndexer.FIELD_METHOD_NAME);

            if (signatures != null) {
                for (String signature : signatures) {
                    // Skip weird methods... Think harder about this
                    if (((method == null) || (method.length() == 0)) &&
                            !Character.isLowerCase(signature.charAt(0))) {
                        continue;
                    }

                    if (!signature.startsWith(method)) {
                        continue;
                    }

                    // Make sure the name matches exactly
                    // We know that the prefix is correct from the first part of
                    // this if clause, by the signature may have more
                    if (((signature.length() > method.length()) &&
                            (signature.charAt(method.length()) != '(')) &&
                            (signature.charAt(method.length()) != ':')) {
                        continue;
                    }

                    int attributes = signature.indexOf(':', method.length());

                    if (attributes == -1) {
                        continue;
                    }

                    if (signature.indexOf('d', attributes + 1) != -1) {
                        // Method is documented
                        assert map != null;
                        matches.add(createMethod(signature, map));
                    }
                }
            }

            String[] attribs = map.getValues(RubyIndexer.FIELD_ATTRIBUTE_NAME);

            if (attribs != null) {
                for (String signature : attribs) {
                    // Skip weird methods... Think harder about this
                    if (((method == null) || (method.length() == 0)) &&
                            !Character.isLowerCase(signature.charAt(0))) {
                        continue;
                    }

                    if (!signature.startsWith(method)) {
                        continue;
                    }

                    // Make sure the name matches exactly
                    // We know that the prefix is correct from the first part of
                    // this if clause, by the signature may have more
                    if (((signature.length() > method.length()) &&
                            //(signature.charAt(method.length()) != '(')) &&
                            (signature.charAt(method.length()) != ':'))) {
                        continue;
                    }

                    // TODO - index whether attributes are documented!
                    //int attributes = signature.indexOf(':', method.length());
                    //
                    //if (attributes == -1) {
                    //    continue;
                    //}
                    //
                    //if (signature.indexOf('d', attributes + 1) != -1) {
                    //    // Method is documented
                        assert map != null;
                        matches.add(createMethod(signature, map));
                    //}
                }
            }
        }

        return matches;
    }

    /** Return the file url corresponding to the given require statement */
    public String getRequiredFileUrl(final String require) {
        final Set<SearchResult> result = new HashSet<SearchResult>();

        String field = RubyIndexer.FIELD_REQUIRE;

        search(field, require, NameKind.EXACT_NAME, result);

        // TODO Prune methods to fit my scheme - later make lucene index smarter about how to prune its index search
        for (SearchResult map : result) {
            String file = map.getValue(RubyIndexer.FIELD_FILENAME);

            if (file != null) {
                return file;
            }
        }

        return null;
    }

    static String getClusterUrl() {
        if (clusterUrl == null) {
            File f =
                InstalledFileLocator.getDefault()
                                    .locate("modules/org-netbeans-modules-ruby.jar", null, false); // NOI18N

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
    
    // For testing only
    static void setClusterUrl(String url) {
        clusterUrl = url;
    }

    static String getPreindexUrl(String url) {
        String s = RubyInstallation.getInstance().getRubyHomeUrl();

        if (url.startsWith(s)) {
            url = RUBYHOME_URL + url.substring(s.length());

            return url;
        }

        s = getClusterUrl();

        if (url.startsWith(s)) {
            return CLUSTER_URL + url.substring(s.length());
        }

        return url;
    }

    /** Get the FileObject corresponding to a URL returned from the index */
    public static FileObject getFileObject(String url) {
        try {
            if (url.startsWith(RUBYHOME_URL)) {
                url = RubyInstallation.getInstance().getRubyHomeUrl() +
                    url.substring(RUBYHOME_URL.length()); // NOI18N
            } else if (url.startsWith(CLUSTER_URL)) {
                url = getClusterUrl() + url.substring(CLUSTER_URL.length()); // NOI18N
            }

            return URLMapper.findFileObject(new URL(url));
        } catch (MalformedURLException mue) {
            Exceptions.printStackTrace(mue);
        }

        return null;
    }
}
