/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.ruby;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.netbeans.modules.gsf.api.Index;
import org.netbeans.modules.ruby.elements.IndexedField;
import static org.netbeans.modules.gsf.api.Index.*;
import org.netbeans.modules.gsf.api.NameKind;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.api.ruby.platform.RubyPlatformManager;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.ruby.elements.IndexedClass;
import org.netbeans.modules.ruby.elements.IndexedElement;
import org.netbeans.modules.ruby.elements.IndexedMethod;
import org.netbeans.modules.ruby.elements.IndexedVariable;
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
 * @todo I do case-sensitive startsWith filtering here which is probably not good
 * @todo Abort when search list .size() > N
 * 
 * @author Tor Norbye
 */
public final class RubyIndex {

    private static final Logger LOGGER = Logger.getLogger(RubyIndex.class.getName());
    
    public static final String UNKNOWN_CLASS = "<Unknown>"; // NOI18N
    public static final String OBJECT = "Object"; // NOI18N
    private static final String CLASS = "Class"; // NOI18N
    private static final String MODULE = "Module"; // NOI18N
    static final Set<SearchScope> ALL_SCOPE = EnumSet.allOf(SearchScope.class);
    static final Set<SearchScope> SOURCE_SCOPE = EnumSet.of(SearchScope.SOURCE);
    private static String clusterUrl = null;
    private static final String CLUSTER_URL = "cluster:"; // NOI18N
    private static final String RUBYHOME_URL = "ruby:"; // NOI18N
    private static final String GEM_URL = "gem:"; // NOI18N

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
            index.search(key, name, kind, ALL_SCOPE, result, null);

            return true;
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);

            return false;
        }
    }

    private boolean search(String key, String name, NameKind kind, Set<SearchResult> result,
        Set<SearchScope> scope) {
        try {
            index.search(key, name, kind, scope, result, null);

            return true;
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);

            return false;
        }
    }

    Set<IndexedClass> getClasses(String name, final NameKind kind, boolean includeAll,
        boolean skipClasses, boolean skipModules) {
        return getClasses(name, kind, includeAll, skipClasses, skipModules, ALL_SCOPE, null);
    }

    /**
     * Return the full set of classes that match the given name.
     *
     * @param name The name of the class - possibly a fqn like File::Stat, or just a class
     *   name like Stat, or just a prefix like St.
     * @param kind Whether we want the exact name, or whether we're searching by a prefix.
     * @param includeAll If true, return multiple IndexedClasses for the same logical
     *   class, one for each declaration point. For example, File is defined both in the
     *   builtin stubs as well as in ftools.
     */
    public Set<IndexedClass> getClasses(String name, final NameKind kind, boolean includeAll,
        boolean skipClasses, boolean skipModules, Set<Index.SearchScope> scope,
        Set<String> uniqueClasses) {
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
        if (includeAll) {
            uniqueClasses = null;
        } else if (uniqueClasses == null) {
            uniqueClasses = new HashSet<String>();
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
            } else if (kind == NameKind.CASE_INSENSITIVE_PREFIX && !clz.regionMatches(true, 0, name, 0, name.length())) {
                continue;
            }

            if (classFqn != null) {
                if (kind == NameKind.CASE_INSENSITIVE_PREFIX ||
                        kind == NameKind.CASE_INSENSITIVE_REGEXP) {
                    if (!classFqn.equalsIgnoreCase(map.getValue(RubyIndexer.FIELD_IN))) {
                        continue;
                    }
                } else if (kind == NameKind.CAMEL_CASE) {
                    String in = map.getValue(RubyIndexer.FIELD_IN);
                    if (in != null) {
                        // Superslow, make faster 
                        StringBuilder sb = new StringBuilder();
//                        String prefix = null;
                        int lastIndex = 0;
                        int idx;
                        do {

                            int nextUpper = -1;
                            for( int i = lastIndex+1; i < classFqn.length(); i++ ) {
                                if ( Character.isUpperCase(classFqn.charAt(i)) ) {
                                    nextUpper = i;
                                    break;
                                }
                            }
                            idx = nextUpper;
                            String token = classFqn.substring(lastIndex, idx == -1 ? classFqn.length(): idx);
//                            if ( lastIndex == 0 ) {
//                                prefix = token;
//                            }
                            sb.append(token); 
                            // TODO - add in Ruby chars here?
                            sb.append( idx != -1 ?  "[\\p{javaLowerCase}\\p{Digit}_\\$]*" : ".*"); // NOI18N         
                            lastIndex = idx;
                        }
                        while(idx != -1);

                        final Pattern pattern = Pattern.compile(sb.toString());
                        if (!pattern.matcher(in).matches()) {
                            continue;
                        }
                    } else {
                        continue;
                    }
                } else {
                    if (!classFqn.equals(map.getValue(RubyIndexer.FIELD_IN))) {
                        continue;
                    }
                }
            }

            String attrs = map.getValue(RubyIndexer.FIELD_CLASS_ATTRS);
            boolean isClass = true;
            if (attrs != null) {
                int flags = IndexedElement.stringToFlag(attrs, 0);
                isClass = (flags & IndexedClass.MODULE) == 0;
                
            }

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

                    int flags = 0;
                    if (attrs != null) {
                        flags = IndexedElement.stringToFlag(attrs, 0);
                    }

                    boolean isDocumented = (flags & IndexedElement.DOCUMENTED) != 0;

                    if (isDocumented) {
                        // Check the actual size of the documentation, and prefer the largest
                        // method
                        int length = 0;
                        int documentedAt = attrs.indexOf(';');

                        if (documentedAt != -1) {
                            int end = attrs.indexOf(';', documentedAt+1);
                            if (end == -1) {
                                end = attrs.length();
                            }                        
                            length = Integer.parseInt(attrs.substring(documentedAt + 1, end));
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
     * Return the set of classes that directly subclass the given class
     *
     * @param name The name of the class - possibly a fqn like File::Stat, or just a class
     *   name like Stat, or just a prefix like St.
     * @param kind Whether we want the exact name, or whether we're searching by a prefix.
     * @param includeAll If true, return multiple IndexedClasses for the same logical
     *   class, one for each declaration point. For example, File is defined both in the
     *   builtin stubs as well as in ftools.
     */
    public Set<IndexedClass> getSubClasses(String name, String fqn, final NameKind kind, Set<Index.SearchScope> scope) {
        final Set<SearchResult> result = new HashSet<SearchResult>();

        //        if (!isValid()) {
        //            LOGGER.fine(String.format("LuceneIndex[%s] is invalid!\n", this.toString()));
        //            return;
        //        }
        String field = RubyIndexer.FIELD_EXTENDS_NAME;
        search(field, fqn, NameKind.EXACT_NAME, result, scope);

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
            } else if (kind == NameKind.CASE_INSENSITIVE_PREFIX && !clz.regionMatches(true, 0, name, 0, name.length())) {
                continue;
            }

            String cfqn = map.getValue(RubyIndexer.FIELD_FQN_NAME);

            // Only return a single instance for this signature
            classes.add(createClass(cfqn, clz, map));
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
        boolean inherited = clz == null;
        
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
                    } else if (kind == NameKind.CASE_INSENSITIVE_PREFIX && !signature.regionMatches(true, 0, name, 0, name.length())) {
                        continue;
                    } else if (kind == NameKind.CASE_INSENSITIVE_REGEXP) {
                        int len = signature.length();
                        int end = signature.indexOf('(');
                        if (end == -1) {
                            end = signature.indexOf(';');
                            if (end == -1) {
                                end = len;
                            }
                        }
                        String n = end != len ? signature.substring(0, end) : signature;
                        try {
                            if (!n.matches(name)) {
                                continue;
                            }
                        } catch (Exception e) {
                            // Silently ignore regexp failures in the search expression
                        }
                    } else if (originalKind == NameKind.EXACT_NAME) {
                        // Make sure the name matches exactly
                        // We know that the prefix is correct from the first part of
                        // this if clause, by the signature may have more
                        if (((signature.length() > name.length()) &&
                                (signature.charAt(name.length()) != '(')) &&
                                (signature.charAt(name.length()) != ';')) {
                            continue;
                        }
                    }

                    // XXX THIS DOES NOT WORK WHEN THERE ARE IDENTICAL SIGNATURES!!!
                    assert map != null;
                    methods.add(createMethod(signature, map, inherited));
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
                    if (kind == NameKind.PREFIX && !signature.startsWith(name)) {
                        continue;
                    } else if (kind == NameKind.CASE_INSENSITIVE_PREFIX && !signature.regionMatches(true, 0, name, 0, name.length())) {
                        continue;
                    } else if (kind == NameKind.CASE_INSENSITIVE_REGEXP && !signature.matches(name)) {
                        continue;
                    } else if (originalKind == NameKind.EXACT_NAME) {
                        // Make sure the name matches exactly
                        // We know that the prefix is correct from the first part of
                        // this if clause, by the signature may have more
                        if (((signature.length() > name.length()) &&
                                //(signature.charAt(name.length()) != '(')) &&
                                (signature.charAt(name.length()) != ';'))) {
                            continue;
                        }
                    }

                    // XXX THIS DOES NOT WORK WHEN THERE ARE IDENTICAL SIGNATURES!!!
                    assert map != null;
                    // Create method for the attribute
                    methods.add(createMethod(signature, map, inherited));
                }
            }
            
            // TODO - fields
        }

        return methods;
    }

    private IndexedMethod createMethod(String signature, SearchResult map, boolean inherited) {
        String clz = map.getValue(RubyIndexer.FIELD_CLASS_NAME);
        String module = map.getValue(RubyIndexer.FIELD_IN);

        if (clz == null) {
            // Module method?
            clz = module;
        } else if ((module != null) && (module.length() > 0)) {
            clz = module + "::" + clz;
        }
        
        String fileUrl = map.getPersistentUrl();
        
        String fqn = map.getValue(RubyIndexer.FIELD_FQN_NAME);
        String require = map.getValue(RubyIndexer.FIELD_REQUIRE);

        // Extract attributes
        int attributeIndex = signature.indexOf(';');
        String attributes = null;
        int flags = 0;

        if (attributeIndex != -1) {
            flags = IndexedElement.stringToFlag(signature, attributeIndex+1);

            if (signature.length() > attributeIndex+1) {
                attributes = signature.substring(attributeIndex+1, signature.length());
            }

            signature = signature.substring(0, attributeIndex);
        }

        IndexedMethod m =
            IndexedMethod.create(this, signature, fqn, clz, fileUrl, require, attributes, flags);

        m.setInherited(inherited);
        return m;
    }

    private IndexedField createField(String signature, SearchResult map, boolean isInstance, boolean inherited) {
        String clz = map.getValue(RubyIndexer.FIELD_CLASS_NAME);
        String module = map.getValue(RubyIndexer.FIELD_IN);

        if (clz == null) {
            // Module method?
            clz = module;
        } else if ((module != null) && (module.length() > 0)) {
            clz = module + "::" + clz;
        }

        String fileUrl = map.getPersistentUrl();

        String fqn = map.getValue(RubyIndexer.FIELD_FQN_NAME);
        String require = map.getValue(RubyIndexer.FIELD_REQUIRE);

        int attributeIndex = signature.indexOf(';');
        String attributes = null;
        int flags = 0;

        if (attributeIndex != -1) {
            flags = IndexedElement.stringToFlag(signature, attributeIndex+1);

            if (signature.length() > attributeIndex+1) {
                attributes = signature.substring(attributeIndex+1, signature.length());
            }

            signature = signature.substring(0, attributeIndex);
        }

        IndexedField m =
            IndexedField.create(this, signature, fqn, clz, fileUrl, require, attributes, flags);        
        m.setInherited(inherited);

        return m;
    }
    
    private IndexedClass createClass(String fqn, String clz, SearchResult map) {
        String require = map.getValue(RubyIndexer.FIELD_REQUIRE);

        // TODO - how do I determine -which- file to associate with the file?
        // Perhaps the one that defines initialize() ?
        String fileUrl = map.getPersistentUrl();

        if (clz == null) {
            clz = map.getValue(RubyIndexer.FIELD_CLASS_NAME);
        }

        String attrs = map.getValue(RubyIndexer.FIELD_CLASS_ATTRS);
        
        int flags = 0;
        if (attrs != null) {
            flags = IndexedElement.stringToFlag(attrs, 0);
        }

        IndexedClass c =
            IndexedClass.create(this, clz, fqn, fileUrl, require, attrs, flags);

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
                    if (kind == NameKind.PREFIX && !require.startsWith(name)) {
                        continue;
                    } else if (kind == NameKind.CASE_INSENSITIVE_PREFIX && !require.regionMatches(true, 0, name, 0, name.length())) {
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

        // XXX Uhm... there could be multiple... Shouldn't I return a set here?
        // (e.g. you can have your own class named File which has nothing to
        // do with the builtin, and has a separate super class...
        
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
            haveRedirected, false);

        return methods;
    }

    /** Return whether the specific class referenced (classFqn) was found or not. This is
     * not the same as returning whether any classes were added since it may add
     * additional methods from parents (Object/Class).
     */
    private boolean addMethodsFromClass(String prefix, NameKind kind, String classFqn,
        Set<IndexedMethod> methods, Set<String> seenSignatures, Set<String> scannedClasses,
        boolean haveRedirected, boolean inheriting) {
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
                                methods, seenSignatures, scannedClasses, haveRedirected, true);
                    }

                    if (!isQualified) {
                        addMethodsFromClass(prefix, kind, include, methods, seenSignatures,
                            scannedClasses, haveRedirected, true);
                    }
                }
            }

            String extendWith = map.getValue(RubyIndexer.FIELD_EXTEND_WITH);

            if (extendWith != null) {
                // Try both with and without a package qualifier
                boolean isQualified = false;

                if (classIn != null) {
                    isQualified = addMethodsFromClass(prefix, kind, classIn + "::" + extendWith,
                            methods, seenSignatures, scannedClasses, haveRedirected, true);
                }

                if (!isQualified) {
                    addMethodsFromClass(prefix, kind, extendWith, methods, seenSignatures,
                        scannedClasses, haveRedirected, true);
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
                                        (signature.charAt(prefix.length()) != ';')) {
                                    continue;
                                }
                            } else {
                                // REGEXP, CAMELCASE filtering etc. not supported here
                                assert (kind == NameKind.PREFIX) ||
                                (kind == NameKind.CASE_INSENSITIVE_PREFIX);
                            }

                            seenSignatures.add(signature);

                            IndexedMethod method = createMethod(signature, map, inheriting);
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
                                        (attribute.charAt(prefix.length()) != ';')) {
                                    continue;
                                }
                            } else {
                                // REGEXP, CAMELCASE filtering etc. not supported here
                                assert (kind == NameKind.PREFIX) ||
                                (kind == NameKind.CASE_INSENSITIVE_PREFIX);
                            }

                            seenSignatures.add(attribute);

                            // TODO - create both getter and setter methods
                            IndexedMethod method = createMethod(attribute, map, inheriting);
                            method.setSmart(!haveRedirected);
                            method.setMethodType(IndexedMethod.MethodType.ATTRIBUTE);
                            methods.add(method);
                        }
                    }
                }
            }
        }
        
        if (classFqn.equals(OBJECT)) {
            return foundIt;
        }

        if (extendsClass == null) {
            if (haveRedirected) {
                addMethodsFromClass(prefix, kind, OBJECT, methods, seenSignatures, scannedClasses,
                    true, true);
            } else {
                // Rather than inheriting directly from object,
                // let's go via Class (and Module) up to Object
                addMethodsFromClass(prefix, kind, CLASS, methods, seenSignatures, scannedClasses,
                    true, true);
            }
        } else {
            if ("ActiveRecord::Base".equals(extendsClass)) { // NOI18N
                // Add in database fields as well
                addDatabaseProperties(prefix, kind, classFqn, methods);
            }

            // We're not sure we have a fully qualified path, so try some different candidates
            if (!addMethodsFromClass(prefix, kind, extendsClass, methods, seenSignatures,
                        scannedClasses, haveRedirected, true)) {
                // Search by classIn 
                String fqn = classIn;

                while (fqn != null) {
                    if (addMethodsFromClass(prefix, kind, fqn + "::" + extendsClass, methods,
                                seenSignatures, scannedClasses, haveRedirected, true)) {
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

    private void addDatabaseProperties(String prefix, NameKind kind, String classFqn,
        Set<IndexedMethod> methods) {
        // Query index for database related properties
        if (classFqn.indexOf("::") != -1) {
            // Don't know how to handle this scenario
            return;
        }
        
        String tableName = RubyUtils.tableize(classFqn);
        
        String searchField = RubyIndexer.FIELD_DB_TABLE;
        Set<SearchResult> result = new HashSet<SearchResult>();
        search(searchField, tableName, NameKind.EXACT_NAME, result);

        List<TableDefinition> tableDefs = new ArrayList<TableDefinition>();
        TableDefinition schema = null;
        
        for (SearchResult map : result) {
            assert map != null;

            String version = map.getValue(RubyIndexer.FIELD_DB_VERSION);
            assert tableName.equals(map.getValue(RubyIndexer.FIELD_DB_TABLE));
            String fileUrl = map.getPersistentUrl();
            
            TableDefinition def = new TableDefinition(tableName, version, fileUrl);
            tableDefs.add(def);
            String[] columns = map.getValues(RubyIndexer.FIELD_DB_COLUMN);

            if (columns != null) {
                for (String column : columns) {
                    // TODO - do this filtering AFTER applying diffs when
                    // I'm doing renaming of columns etc.
                    def.addColumn(column);
                }
            }

            if (RubyIndexer.SCHEMA_INDEX_VERSION.equals(version)) {
                schema = def;
                // With a schema I don't need to look at anything else
                break;
            }
        }
        
        if (tableDefs.size() > 0) {
            Map<String,String> columnDefs = new HashMap<String,String>();
            Map<String,String> fileUrls = new HashMap<String,String>();
            Set<String> currentCols = new HashSet<String>();
            if (schema != null) {
                List<String> cols = schema.getColumns();
                if (cols != null) {
                    for (String col : cols) {
                        int typeIndex = col.indexOf(';');
                        if (typeIndex != -1) {
                            String name = col.substring(0, typeIndex);
                            if (typeIndex < col.length()-1 && col.charAt(typeIndex+1) == '-') {
                                // Removing column - this is unlikely in a
                                // schema.rb file!
                                currentCols.remove(col);
                            } else {
                                currentCols.add(name);
                                fileUrls.put(col, schema.getFileUrl());
                                columnDefs.put(name, col);
                            }
                        } else {
                            currentCols.add(col);
                            columnDefs.put(col, col);
                            fileUrls.put(col, schema.getFileUrl());
                        }
                    }
                }
            } else {
                // Apply migration files
                Collections.sort(tableDefs);
                for (TableDefinition def : tableDefs) {
                    List<String> cols = def.getColumns();
                    if (cols == null) {
                        continue;
                    }

                    for (String col : cols) {
                        int typeIndex = col.indexOf(';');
                        if (typeIndex != -1) {
                            String name = col.substring(0, typeIndex);
                            if (typeIndex < col.length()-1 && col.charAt(typeIndex+1) == '-') {
                                // Removing column
                                currentCols.remove(name);
                            } else {
                                currentCols.add(name);
                                fileUrls.put(col, def.getFileUrl());
                                columnDefs.put(name, col);
                            }
                        } else {
                            currentCols.add(col);
                            columnDefs.put(col, col);
                            fileUrls.put(col, def.getFileUrl());
                        }
                    }
                }
            }
            
            // Finally, we've "applied" the migrations - just walk
            // through the datastructure and create completion matches
            // as appropriate
            for (String column : currentCols) {
                if (column.startsWith(prefix)) {
                    if (kind == NameKind.EXACT_NAME) {
                        // Ensure that the method is not longer than the prefix
                        if ((column.length() > prefix.length())) {
                            continue;
                        }
                    } else {
                        // REGEXP, CAMELCASE filtering etc. not supported here
                        assert (kind == NameKind.PREFIX) ||
                        (kind == NameKind.CASE_INSENSITIVE_PREFIX);
                    }
                    
                    String c = columnDefs.get(column);
                    String type = tableName;
                    int semicolonIndex = c.indexOf(';');
                    if (semicolonIndex != -1) {
                        type = c.substring(semicolonIndex + 1);
                    }
                    String fileUrl = fileUrls.get(column);

                    String signature = column;
                    String fqn = tableName+"#"+column;
                    String clz = type;
                    String require = null;
                    String attributes = "";
                    int flags = 0;
                    
                    IndexedMethod method =
                        IndexedMethod.create(this, signature, fqn, clz, fileUrl, require, attributes, flags);
                    method.setMethodType(IndexedMethod.MethodType.DBCOLUMN);
                    method.setSmart(true);
                    methods.add(method);
                }
            }
            
            if ("find_by_".startsWith(prefix) ||
                    "find_all_by".startsWith(prefix)) {
                // Generate dynamic finders
                for (String column : currentCols) {
                    String methodOneName = "find_by_" + column;
                    String methodAllName = "find_all_by_" + column;
                    if (methodOneName.startsWith(prefix) || methodAllName.startsWith(prefix)) {
                        if (kind == NameKind.EXACT_NAME) {
// XXX methodOneName || methodAllName?                            
                            // Ensure that the method is not longer than the prefix
                            if ((column.length() > prefix.length())) {
                                continue;
                            }
                        } else {
                            // REGEXP, CAMELCASE filtering etc. not supported here
                            assert (kind == NameKind.PREFIX) ||
                            (kind == NameKind.CASE_INSENSITIVE_PREFIX);
                        }

                        String type = columnDefs.get(column);
                        type = type.substring(type.indexOf(';') + 1);
                        String fileUrl = fileUrls.get(column);

                        String clz = classFqn;
                        String require = null;
                        int flags = IndexedElement.STATIC;
                        String attributes = IndexedElement.flagToString(flags) + ";;;" + "options(:first|:all),args(=>conditions|order|group|limit|offset|joins|readonly:bool|include|select|from|readonly:bool|lock:bool)";

                        if (methodOneName.startsWith(prefix)) {
                            String signature = methodOneName+"(" + column + ",*options)";
                            String fqn = tableName+"#"+signature;
                            IndexedMethod method =
                                IndexedMethod.create(this, signature, fqn, clz, fileUrl, require, attributes, flags);
                            method.setInherited(false);
                            method.setSmart(true);
                            methods.add(method);
                        }
                        if (methodAllName.startsWith(prefix)) {
                            String signature = methodAllName+"(" + column + ",*options)";
                            String fqn = tableName+"#"+signature;
                            IndexedMethod method =
                                IndexedMethod.create(this, signature, fqn, clz, fileUrl, require, attributes, flags);
                            method.setInherited(false);
                            method.setSmart(true);
                            methods.add(method);
                        }
                    }
                }
                
            }
        }
    }
    
    private class TableDefinition implements Comparable<TableDefinition> {
        private String version;
        /** table is redundant, I only search by exact tablenames anyway */
        private String table;
        private String fileUrl;
        private List<String> cols;

        TableDefinition(String table, String version, String fileUrl) {
            this.table = table;
            this.version = version;
            this.fileUrl = fileUrl;
        }

        public int compareTo(RubyIndex.TableDefinition o) {
            // See if we're comparing an old style (3-digit) version number with a new Rails 2.1 UTC version
            if (version.length() != o.version.length()) {
                return version.length() - o.version.length();
            }
            // I can do string comparisons here because the strings
            // are all padded with zeroes on the left (so 100 is going
            // to be greater than 099, which wouldn't be true for "99".)
            return version.compareTo(o.version);
        }
        
        String getFileUrl() {
            return fileUrl;
        }
        
        void addColumn(String column) {
            if (cols == null) {
                cols = new ArrayList<String>();
            }

            cols.add(column);
        }
        
        List<String> getColumns() {
            return cols;
        }
    }
    
    public Set<String> getDatabaseTables(String prefix, NameKind kind) {
        // Query index for database related properties
        
        String searchField = RubyIndexer.FIELD_DB_TABLE;
        Set<SearchResult> result = new HashSet<SearchResult>();
        search(searchField, prefix, kind, result);

        Set<String> tables = new HashSet<String>();
        for (SearchResult map : result) {
            assert map != null;

            String tableName = map.getValue(RubyIndexer.FIELD_DB_TABLE);
            if (tableName != null) {
                tables.add(tableName);
            }
        }
        
        return tables;
    }

    public Set<IndexedVariable> getGlobals(String prefix, NameKind kind) {
        // Query index for database related properties
        
        String searchField = RubyIndexer.FIELD_GLOBAL_NAME;
        Set<SearchResult> result = new HashSet<SearchResult>();
        // Only include globals from the user's sources, not in the libraries!
        search(searchField, prefix, kind, result, SOURCE_SCOPE);

        Set<IndexedVariable> globals = new HashSet<IndexedVariable>();
        for (SearchResult map : result) {
            assert map != null;

            String[] names = map.getValues(RubyIndexer.FIELD_GLOBAL_NAME);
            if (names != null) {
                String fileUrl = map.getPersistentUrl();
                for (String name : names) {
                    int flags = 0;
                    IndexedVariable var = IndexedVariable.create(this, name, name, null, fileUrl, null, name, flags, ElementKind.GLOBAL);
                    globals.add(var);
                }
            }
        }
        
        return globals;
    }
    
    public Set<IndexedField> getInheritedFields(String classFqn, String prefix, NameKind kind, boolean inherited) {
        boolean haveRedirected = false;

        if ((classFqn == null) || classFqn.equals(OBJECT)) {
            // Redirect inheritance tree to Class to pick up methods in Class and Module
            classFqn = CLASS;
            haveRedirected = true;
        } else if (MODULE.equals(classFqn) || CLASS.equals(classFqn)) {
            haveRedirected = true;
        }

        //String field = RubyIndexer.FIELD_FQN_NAME;
        Set<IndexedField> members = new HashSet<IndexedField>();
        Set<String> scannedClasses = new HashSet<String>();
        Set<String> seenSignatures = new HashSet<String>();

        boolean instanceVars = true;
        if (prefix == null) {
            prefix = "";
        } else if (prefix.startsWith("@@")) {
            instanceVars = false;
            prefix = prefix.substring(2);
        } else if (prefix.startsWith("@")) {
            prefix = prefix.substring(1);
        }
        
        addFieldsFromClass(prefix, kind, classFqn, members, seenSignatures, scannedClasses,
            haveRedirected, instanceVars, inherited);

        return members;
    }

    /** Return whether the specific class referenced (classFqn) was found or not. This is
     * not the same as returning whether any classes were added since it may add
     * additional methods from parents (Object/Class).
     */
    private boolean addFieldsFromClass(String prefix, NameKind kind, String classFqn,
        Set<IndexedField> methods, Set<String> seenSignatures, Set<String> scannedClasses,
        boolean haveRedirected, boolean instanceVars, boolean inheriting) {
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
                        isQualified = addFieldsFromClass(prefix, kind, classIn + "::" + include,
                                methods, seenSignatures, scannedClasses, haveRedirected, instanceVars, true);
                    }

                    if (!isQualified) {
                        addFieldsFromClass(prefix, kind, include, methods, seenSignatures,
                            scannedClasses, haveRedirected, instanceVars, true);
                    }
                }
            }

            String extendWith = map.getValue(RubyIndexer.FIELD_EXTEND_WITH);

            if (extendWith != null) {
                // Try both with and without a package qualifier
                boolean isQualified = false;

                if (classIn != null) {
                    isQualified = addFieldsFromClass(prefix, kind, classIn + "::" + extendWith,
                            methods, seenSignatures, scannedClasses, haveRedirected, instanceVars, true);
                }

                if (!isQualified) {
                    addFieldsFromClass(prefix, kind, extendWith, methods, seenSignatures,
                        scannedClasses, haveRedirected, instanceVars, true);
                }
            }
            

            String[] fields = map.getValues(RubyIndexer.FIELD_FIELD_NAME);

            if (fields != null) {
                for (String field : fields) {
                    // Skip weird methods like "[]" etc. in completion lists... TODO Think harder about this
                    if ((prefix.length() == 0) && !Character.isLowerCase(field.charAt(0))) {
                        continue;
                    }

                    // Prevent duplicates when method is redefined
                    if (!seenSignatures.contains(field)) {
                        // See if we need instancevars or classvars
                        boolean isInstance = true;
                        int signatureIndex = field.indexOf(';');
                        if (signatureIndex != -1 && field.indexOf('s', signatureIndex+1) != -1) {
                            isInstance = false;
                        }
                        if (isInstance != instanceVars) {
                            continue;
                        }
                        
                        if (field.startsWith(prefix)) {
                            if (kind == NameKind.EXACT_NAME) {
                                // Ensure that the method is not longer than the prefix
                                if ((field.length() > prefix.length()) &&
                                        (field.charAt(prefix.length()) != '(') &&
                                        (field.charAt(prefix.length()) != ';')) {
                                    continue;
                                }
                            } else {
                                // REGEXP, CAMELCASE filtering etc. not supported here
                                assert (kind == NameKind.PREFIX) ||
                                (kind == NameKind.CASE_INSENSITIVE_PREFIX);
                            }

                            seenSignatures.add(field);

                            IndexedField f = createField(field, map, isInstance, inheriting);
                            f.setSmart(!haveRedirected);
                            methods.add(f);
                        }
                    }
                }
            }
        }
        
        if (classFqn.equals(OBJECT)) {
            return foundIt;
        }

        if (extendsClass == null) {
            if (haveRedirected) {
                addFieldsFromClass(prefix, kind, OBJECT, methods, seenSignatures, scannedClasses,
                    true, instanceVars, true);
            } else {
                // Rather than inheriting directly from object,
                // let's go via Class (and Module) up to Object
                addFieldsFromClass(prefix, kind, CLASS, methods, seenSignatures, scannedClasses,
                    true, instanceVars, true);
            }
        } else {
            // We're not sure we have a fully qualified path, so try some different candidates
            if (!addFieldsFromClass(prefix, kind, extendsClass, methods, seenSignatures,
                        scannedClasses, haveRedirected, instanceVars, true)) {
                // Search by classIn 
                String fqn = classIn;

                while (fqn != null) {
                    if (addFieldsFromClass(prefix, kind, fqn + "::" + extendsClass, methods,
                                seenSignatures, scannedClasses, haveRedirected, instanceVars, true)) {
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

        int hashIndex = fqn.indexOf('#');

        if (hashIndex == -1) {
            // Looking for a class or a module
            return getDocumentedClasses(fqn);
        } else {
            // Looking for a method
            String clz = fqn.substring(0, hashIndex);
            String method = fqn.substring(hashIndex + 1);

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

            if (attributes != null) {
                int flags = IndexedElement.stringToFlag(attributes, 0);
                if ((flags & IndexedElement.DOCUMENTED) != 0) {
                    matches.add(createClass(fqn, null, map));
                }
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
                            (signature.charAt(method.length()) != ';')) {
                        continue;
                    }
                    
                    int attributes = signature.indexOf(';', method.length());
                    if (attributes == -1) {
                        continue;
                    }
                    int flags = IndexedElement.stringToFlag(signature, attributes+1);
                    if ((flags & IndexedElement.DOCUMENTED) != 0) {
                        // Method is documented
                        assert map != null;
                        matches.add(createMethod(signature, map, false));
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
                            (signature.charAt(method.length()) != ';'))) {
                        continue;
                    }

                    // TODO - index whether attributes are documented!
                    //int attributes = signature.indexOf(';', method.length());
                    //
                    //if (attributes == -1) {
                    //    continue;
                    //}
                    //
                    //if (signature.indexOf('d', attributes + 1) != -1) {
                    //    // Method is documented
                        assert map != null;
                        matches.add(createMethod(signature, map, false));
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
            String file = map.getPersistentUrl();

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
        if (RubyIndexer.PREINDEXING) {
            Iterator<RubyPlatform> it = RubyPlatformManager.platformIterator();
            while (it.hasNext()) {
                RubyPlatform platform = it.next();
                String s = getGemHomeURL(platform);
                
                if (s != null && url.startsWith(s)) {
                    return GEM_URL + url.substring(s.length());
                }

                s = platform.getHomeUrl();

                if (url.startsWith(s)) {
                    url = RUBYHOME_URL + url.substring(s.length());

                    return url;
                }
            }
        } else {
            // FIXME: use right platform
            RubyPlatform platform = RubyPlatformManager.getDefaultPlatform();
            String s = getGemHomeURL(platform);

            if (s != null && url.startsWith(s)) {
                return GEM_URL + url.substring(s.length());
            }

            s = platform.getHomeUrl();

            if (url.startsWith(s)) {
                url = RUBYHOME_URL + url.substring(s.length());

                return url;
            }
        }

        String s = getClusterUrl();

        if (url.startsWith(s)) {
            return CLUSTER_URL + url.substring(s.length());
        }

        return url;
    }

    /** Get the FileObject corresponding to a URL returned from the index */
    public static FileObject getFileObject(String url) {
        try {
            if (url.startsWith(RUBYHOME_URL)) {
                // TODO - resolve to correct platform
                // FIXME: per-platform now
                Iterator<RubyPlatform> it = RubyPlatformManager.platformIterator();
                while (it.hasNext()) {
                    RubyPlatform platform = it.next();
                    url = platform.getHomeUrl() + url.substring(RUBYHOME_URL.length());
                    FileObject fo = URLMapper.findFileObject(new URL(url));
                    if (fo != null) {
                        return fo;
                    }
                }
                
                return null;
            } else if (url.startsWith(GEM_URL)) {
                // FIXME: per-platform now
                Iterator<RubyPlatform> it = RubyPlatformManager.platformIterator();
                while (it.hasNext()) {
                    RubyPlatform platform = it.next();
                    if (!platform.hasRubyGemsInstalled()) {
                        continue;
                    }
                    url = platform.getGemManager().getGemHomeUrl() + url.substring(GEM_URL.length());
                    FileObject fo = URLMapper.findFileObject(new URL(url));
                    if (fo != null) {
                        return fo;
                    }
                }
                
                return null;
            } else if (url.startsWith(CLUSTER_URL)) {
                url = getClusterUrl() + url.substring(CLUSTER_URL.length()); // NOI18N
            }

            return URLMapper.findFileObject(new URL(url));
        } catch (MalformedURLException mue) {
            Exceptions.printStackTrace(mue);
        }

        return null;
    }

    private static String getGemHomeURL(RubyPlatform platform) {
        return platform.hasRubyGemsInstalled() ? platform.getGemManager().getGemHomeUrl() : null;
    }
}
