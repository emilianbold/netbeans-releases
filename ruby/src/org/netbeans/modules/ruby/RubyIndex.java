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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.ruby.elements.IndexedField;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.api.ruby.platform.RubyPlatformManager;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport.Kind;
import org.netbeans.modules.ruby.elements.IndexedClass;
import org.netbeans.modules.ruby.elements.IndexedConstant;
import org.netbeans.modules.ruby.elements.IndexedElement;
import org.netbeans.modules.ruby.elements.IndexedMethod;
import org.netbeans.modules.ruby.elements.IndexedVariable;
import org.netbeans.modules.ruby.platform.gems.GemManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;

import static org.netbeans.modules.ruby.RubyIndexer.*;

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

    private static final Logger LOGGGER = Logger.getLogger(RubyIndex.class.getName());

    public static final String UNKNOWN_CLASS = "<Unknown>"; // NOI18N
    public static final String OBJECT        = "Object"; // NOI18N
    private static final String CLASS        = "Class"; // NOI18N
    private static final String MODULE       = "Module"; // NOI18N
    private static final String CLUSTER_URL  = "cluster:"; // NOI18N
    private static final String RUBYHOME_URL = "ruby:"; // NOI18N
    private static final String GEM_URL      = "gem:"; // NOI18N

    private static String clusterUrl = null;
    
    private static final RubyIndex EMPTY = new RubyIndex(null);
    private FileObject context;

    private final QuerySupport querySupport;

    /**
     * Caches the index to avoid querying roots (can be time consuming). Holds the index just for
     * one FileObject at time.
     */
    private static final Map<FileObject, RubyIndex> CACHE = new WeakHashMap<FileObject, RubyIndex>(1);

    /**
     * The base class for AR model classes, needs special handling in various
     * places.
     */
    static final String ACTIVE_RECORD_BASE = "ActiveRecord::Base"; //NOI18N

    /** AR Relation. Provides dynamic query methods. */
    static final String ACTIVE_RECORD_RELATION = "ActiveRecord::Relation"; //NOI18N
    
    private RubyIndex(QuerySupport querySupport) {
        this.querySupport = querySupport;
    }

    public static RubyIndex get(Collection<FileObject> roots) {
        try {
            return new RubyIndex(QuerySupport.forRoots(
                    RubyIndexer.Factory.NAME,
                    RubyIndexer.Factory.VERSION,
                    roots.toArray(new FileObject[roots.size()])));
        } catch (IOException ioe) {
            LOGGGER.log(Level.WARNING, null, ioe);
            return EMPTY;
        }
    }

    public static RubyIndex get(final Parser.Result result) {
        return get(RubyUtils.getFileObject(result));
    }

    public static RubyIndex get(final FileObject fo) {
        RubyIndex result = CACHE.get(fo);
        if (result != null) {
            return result;
        }
        result = fo == null ? null : get(QuerySupport.findRoots(fo,
                Collections.singleton(RubyLanguage.SOURCE),
                Collections.singleton(RubyLanguage.BOOT),
                Collections.<String>emptySet()));
        // cache the index just for one fo
        CACHE.clear();
        CACHE.put(fo, result);
        return result;
    }

    public static void resetCache() {
        CACHE.clear();
    }

    public Collection<? extends IndexResult> query(
            final String fieldName, final String fieldValue,
            final QuerySupport.Kind kind, final String... fieldsToLoad) {
        if (querySupport != null) {
            try {
                return querySupport.query(fieldName, fieldValue, kind, fieldsToLoad);
            } catch (IOException ioe) {
                LOGGGER.log(Level.WARNING, null, ioe);
            }
        }

        return Collections.<IndexResult>emptySet();
    }

    private boolean search(String key, String name, QuerySupport.Kind kind, Collection<IndexResult> result, String... fieldsToLoad) {
        try {
            result.addAll(querySupport.query(key, name, kind,  fieldsToLoad));
            return true;
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
            return false;
        }
    }

    FileObject getContext() {
        return context;
    }

    Set<IndexedClass> getClasses(String name, final QuerySupport.Kind kind, boolean includeAll,
        boolean skipClasses, boolean skipModules) {
        return getClasses(name, kind, includeAll, skipClasses, skipModules, null);
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
    public Set<IndexedClass> getClasses(String name, final QuerySupport.Kind kind, boolean includeAll,
        boolean skipClasses, boolean skipModules, Set<String> uniqueClasses) {
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

        final Set<IndexResult> result = new HashSet<IndexResult>();

        String field;

        switch (kind) {
            case EXACT:
            case PREFIX:
            case CAMEL_CASE:
            case REGEXP:
                field = FIELD_CLASS_NAME;
                break;

            case CASE_INSENSITIVE_PREFIX:
            case CASE_INSENSITIVE_REGEXP:
                field = FIELD_CASE_INSENSITIVE_CLASS_NAME;
                break;

            default:
                throw new UnsupportedOperationException(kind.toString());
        }

        search(field, name, kind, result);

        // TODO Prune methods to fit my scheme - later make lucene index smarter about how to prune its index search
        if (includeAll) {
            uniqueClasses = null;
        } else if (uniqueClasses == null) {
            uniqueClasses = new HashSet<String>();
        }

        final Set<IndexedClass> classes = new HashSet<IndexedClass>();

        for (IndexResult map : result) {
            String clz = map.getValue(FIELD_CLASS_NAME);

            if (clz == null) {
                // It's probably a module
                // XXX I need to handle this... for now punt
                continue;
            }

            // Lucene returns some inexact matches, TODO investigate why this is necessary
            if ((kind == QuerySupport.Kind.PREFIX) && !clz.startsWith(name)) {
                continue;
            } else if (kind == QuerySupport.Kind.CASE_INSENSITIVE_PREFIX && !clz.regionMatches(true, 0, name, 0, name.length())) {
                continue;
            }

            if (classFqn != null) {
                if (kind == QuerySupport.Kind.CASE_INSENSITIVE_PREFIX ||
                        kind == QuerySupport.Kind.CASE_INSENSITIVE_REGEXP) {
                    if (!classFqn.equalsIgnoreCase(map.getValue(FIELD_IN))) {
                        continue;
                    }
                } else if (kind == QuerySupport.Kind.CAMEL_CASE) {
                    String in = map.getValue(FIELD_IN);
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
                    if (!classFqn.equals(map.getValue(FIELD_IN))) {
                        continue;
                    }
                }
            }

            String attrs = map.getValue(FIELD_CLASS_ATTRS);
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

            String fqn = map.getValue(FIELD_FQN_NAME);

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
    public Set<IndexedClass> getSubClasses(String name, String fqn, final QuerySupport.Kind kind) {

        Collection<? extends IndexResult> result =
                query(FIELD_EXTENDS_NAME, fqn, QuerySupport.Kind.EXACT, FIELD_EXTENDS_NAME);

        final Set<IndexedClass> classes = new HashSet<IndexedClass>();

        for (IndexResult ir : result) {
            String clz = ir.getValue(FIELD_CLASS_NAME);

            if (clz == null) {
                // It's probably a module
                // XXX I need to handle this... for now punt
                continue;
            }

            // Lucene returns some inexact matches, TODO investigate why this is necessary
            if ((kind == QuerySupport.Kind.PREFIX) && !clz.startsWith(name)) {
                continue;
            } else if (kind == QuerySupport.Kind.CASE_INSENSITIVE_PREFIX && !clz.regionMatches(true, 0, name, 0, name.length())) {
                continue;
            }

            String cfqn = ir.getValue(FIELD_FQN_NAME);

            // Only return a single instance for this signature
            classes.add(createClass(cfqn, clz, ir));
        }

        return classes;
    }
    
    Set<? extends IndexedMethod> getMethods(final String name, final RubyType clz, QuerySupport.Kind kind) {
        Set<IndexedMethod> methods = new HashSet<IndexedMethod>();
        for (String realType : clz.getRealTypes()) {
            methods.addAll(getMethods(name, realType, kind));
        }
        return methods;
    }

    Set<IndexedMethod> getMethods(String name, QuerySupport.Kind kind) {
        return getMethods(name, (String) null, kind);
    }

    /**
     * Return a set of methods that match the given name prefix, and are in the given
     * class and module. If no class is specified, match methods across all classes.
     * Note that inherited methods are not checked. If you want to match inherited methods
     * you must call this method on each superclass as well as the mixin modules.
     */
    @SuppressWarnings("fallthrough")
    public Set<IndexedMethod> getMethods(final String name, final String clz, QuerySupport.Kind kind) {
        boolean inherited = clz == null;
        
        //    public void searchByCriteria(final String name, final ClassIndex.QuerySupport.Kind kind, /*final ResultConvertor<T> convertor,*/ final Set<String> file) throws IOException {
        final Set<IndexResult> result = new HashSet<IndexResult>();

        QuerySupport.Kind originalKind = kind;
        if (kind == QuerySupport.Kind.EXACT) {
            // I can't do exact searches on methods because the method
            // entries include signatures etc. So turn this into a prefix
            // search and then compare chopped off signatures with the name
            kind = QuerySupport.Kind.PREFIX;
        }

        // No point in doing case insensitive searches on method names because
        // method names in Ruby are always case insensitive anyway
        //            case CASE_INSENSITIVE_PREFIX:
        //            case CASE_INSENSITIVE_REGEXP:
        //                field = FIELD_CASE_INSENSITIVE_METHOD_NAME;
        //                break;

        search(FIELD_METHOD_NAME, name, kind, result);
        // include also for attr methods that create accessors (e.g attr, attr_accessor)
        search(FIELD_ATTRIBUTE_NAME, name, kind, result);

        //return Collections.unmodifiableSet(file);

        // TODO Prune methods to fit my scheme - later make lucene index smarter about how to prune its index search
        final Set<IndexedMethod> methods = new HashSet<IndexedMethod>();

        for (IndexResult map : result) {
            if (clz != null) {
                String fqn = map.getValue(FIELD_FQN_NAME);

                if (!(clz.equals(fqn))) {
                    continue;
                }
            }

            String[] signatures = map.getValues(FIELD_METHOD_NAME);

            if (signatures != null) {
                for (String signature : signatures) {
                    // Skip weird methods... Think harder about this
                    if (((name == null) || (name.length() == 0)) &&
                            !Character.isLowerCase(signature.charAt(0))) {
                        continue;
                    }

                    // Lucene returns some inexact matches, TODO investigate why this is necessary
                    if ((kind == QuerySupport.Kind.PREFIX) && !signature.startsWith(name)) {
                        continue;
                    } else if (kind == QuerySupport.Kind.CASE_INSENSITIVE_PREFIX && !signature.regionMatches(true, 0, name, 0, name.length())) {
                        continue;
                    } else if (kind == QuerySupport.Kind.CASE_INSENSITIVE_REGEXP) {
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
                    } else if (originalKind == QuerySupport.Kind.EXACT) {
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

            String[] attributes = map.getValues(FIELD_ATTRIBUTE_NAME);

            if (attributes != null) {
                for (String signature : attributes) {
                    // Skip weird methods... Think harder about this
                    if (((name == null) || (name.length() == 0)) &&
                            !Character.isLowerCase(signature.charAt(0))) {
                        continue;
                    }

                    // Lucene returns some inexact matches, TODO investigate why this is necessary
                    if (kind == QuerySupport.Kind.PREFIX && !signature.startsWith(name)) {
                        continue;
                    } else if (kind == QuerySupport.Kind.CASE_INSENSITIVE_PREFIX && !signature.regionMatches(true, 0, name, 0, name.length())) {
                        continue;
                    } else if (kind == QuerySupport.Kind.CASE_INSENSITIVE_REGEXP && !signature.matches(name)) {
                        continue;
                    } else if (originalKind == QuerySupport.Kind.EXACT) {
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

    public IndexedMethod createMethod(String signature, IndexResult ir, boolean inherited) {
        String clz = ir.getValue(FIELD_CLASS_NAME);
        String module = ir.getValue(FIELD_IN);

        if (clz == null) {
            // Module method?
            clz = module;
        } else if ((module != null) && (module.length() > 0)) {
            clz = module + "::" + clz;
        }
        
        String fqn = ir.getValue(FIELD_FQN_NAME);
        String require = ir.getValue(FIELD_REQUIRE);

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

        IndexedMethod m = IndexedMethod.create(this, signature, fqn, clz,
                ir.getFile(), require, attributes, flags, context);

        m.setInherited(inherited);
        return m;
    }

    public IndexedField createField(String signature, IndexResult ir, boolean isInstance, boolean inherited) {
        String clz = ir.getValue(FIELD_CLASS_NAME);
        String module = ir.getValue(FIELD_IN);

        if (clz == null) {
            // Module method?
            clz = module;
        } else if ((module != null) && (module.length() > 0)) {
            clz = module + "::" + clz;
        }

        String fqn = ir.getValue(FIELD_FQN_NAME);
        String require = ir.getValue(FIELD_REQUIRE);

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

        IndexedField m = IndexedField.create(
                this, signature, fqn, clz, ir, require, attributes, flags, context);
        m.setInherited(inherited);

        return m;
    }

    private static boolean isEmptyOrNull(String str) {
        return str == null || "".equals(str.trim());
    }

    public IndexedConstant createConstant(String signature, IndexResult ir) {
        String classFQN = ir.getValue(FIELD_FQN_NAME);
        String require = ir.getValue(FIELD_REQUIRE);

        int typeIndex = signature.indexOf(';');
        String name = typeIndex == -1 ? signature : signature.substring(0, typeIndex);
        int flags = 0;

        // TODO parse possibly multiple types
        String type = typeIndex == -1 ? null : signature.substring(typeIndex + 1);

        RubyType rubyType = isEmptyOrNull(type) ? RubyType.unknown() : RubyType.create(type);
        IndexedConstant m = IndexedConstant.create(
                this, name, classFQN, ir, require, flags, context, rubyType);

        return m;
    }

    public IndexedClass createClass(String fqn, String clz, IndexResult ir) {
        String require = ir.getValue(FIELD_REQUIRE);

        if (clz == null) {
            clz = ir.getValue(FIELD_CLASS_NAME);
        }

        String attrs = ir.getValue(FIELD_CLASS_ATTRS);
        
        int flags = 0;
        if (attrs != null) {
            flags = IndexedElement.stringToFlag(attrs, 0);
        }

        IndexedClass c =
            IndexedClass.create(this, clz, fqn, ir, require, attrs, flags, context);

        return c;
    }

    // List of String[2]: 0: requirename, 1: fqn
    public Set<String[]> getRequires(final String name, final QuerySupport.Kind kind) {
        final Set<IndexResult> result = new HashSet<IndexResult>();

        String field = FIELD_REQUIRE;

        search(field, name, kind, result);

        // TODO Prune methods to fit my scheme - later make lucene index smarter about how to prune its index search
        final Map<String, String> fqns = new HashMap<String, String>();

        for (IndexResult map : result) {
            String[] r = map.getValues(field);

            if (r != null) {
                for (String require : r) {
                    // Lucene returns some inexact matches, TODO investigate why this is necessary
                    if (kind == QuerySupport.Kind.PREFIX && !require.startsWith(name)) {
                        continue;
                    } else if (kind == QuerySupport.Kind.CASE_INSENSITIVE_PREFIX && !require.regionMatches(true, 0, name, 0, name.length())) {
                        continue;
                    }
                    assert map != null;

                    // TODO - check if there's a rubygem which captures this
                    // require and if so, remove it
                    String fqn = map.getValue(FIELD_FQN_NAME);

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
        final Set<IndexResult> result = new HashSet<IndexResult>();

        String field = FIELD_REQUIRE;

        search(field, require, QuerySupport.Kind.EXACT, result);

        final Set<String> fqns = new HashSet<String>();
        
        for (IndexResult map : result) {
            String fqn = map.getValue(FIELD_FQN_NAME);
            
            if (fqn != null) {
                fqns.add(fqn);
            }
        }

        return fqns;
    }

    /**
     * Gets the super clases of the given class; the class itself
     * is not included.
     * @param fqn
     * @return an ordered list of the super classes; closest first.
     */
    public List<IndexedClass> getSuperClasses(String fqn) {
        // todo: performance?
        List<IndexedClass> superClasses = new ArrayList<IndexedClass>();
        IndexedClass superClass = getSuperclass(fqn);
        while (superClass != null) {
            superClasses.add(superClass);
            superClass = getSuperclass(superClass.getName());
        }
        return superClasses;
    }

    public IndexedClass getSuperclass(String fqn) {
        final Set<IndexResult> result = new HashSet<IndexResult>();

        QuerySupport.Kind kind = QuerySupport.Kind.EXACT;
        String field = FIELD_FQN_NAME;

        search(field, fqn, kind, result, FIELD_FQN_NAME, FIELD_EXTENDS_NAME);

        // XXX Uhm... there could be multiple... Shouldn't I return a set here?
        // (e.g. you can have your own class named File which has nothing to
        // do with the builtin, and has a separate super class...
        
        for (IndexResult map : result) {
            assert fqn.equals(map.getValue(FIELD_FQN_NAME));

            String extendsClass = map.getValue(FIELD_EXTENDS_NAME);

            if (extendsClass != null) {
                // Found the class name, now look it up in the index
                result.clear();

                if (!search(field, extendsClass, kind, result)) {
                    return null;
                }

                // There should be exactly one match
                if (result.size() > 0) {
                    IndexResult superMap = result.iterator().next();
                    String superFqn = superMap.getValue(FIELD_FQN_NAME);

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

        String searchField = FIELD_EXTENDS_NAME;

        Set<IndexResult> result = new HashSet<IndexResult>();

        search(searchField, classFqn, QuerySupport.Kind.EXACT, result, FIELD_EXTENDS_NAME, FIELD_FQN_NAME);

        boolean foundIt = result.size() > 0;

        // If this is a bogus class entry (no search rsults) don't continue
        if (!foundIt) {
            return foundIt;
        }

        for (IndexResult map : result) {
            String fqn = map.getValue(FIELD_FQN_NAME);
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
        //String field = FIELD_FQN_NAME;
        Set<IndexedClass> classes = new LinkedHashSet<IndexedClass>();
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
    
    /** 
     * Gets either the most distant or the closest method in the hierarchy that the given method overrides or
     * the method itself if it doesn't override any super methods.
     *
     * @param className the name of class where the given <code>methodName</code> is.
     * @param methodName the name of the method.
     * @param closest if true, gets the closest super method, otherwise the most distant.
     * 
     * @return method or <code>null</code> if the was no such method.
     */
    public IndexedMethod getSuperMethod(String className, String methodName, boolean closest) {
        return getSuperMethod(className, methodName, closest, true);
    }

    /**
     * Gets either the most distant or the closest method in the hierarchy that the given method overrides or
     * the method itself if it doesn't override any super methods.
     *
     * @param className the name of class where the given <code>methodName</code> is.
     * @param methodName the name of the method.
     * @param closest if true, gets the closest super method, otherwise the most distant.
     * @param includeSelf if true, returns the method itself if it had no super methods.
     *
     * @return method or <code>null</code> if the was no such method.
     */
    IndexedMethod getSuperMethod(String className, String methodName, boolean closest, boolean includeSelf) {
        Set<IndexedMethod> methods = getInheritedMethods(className, methodName, QuerySupport.Kind.EXACT);

        // todo: performance?
        List<IndexedClass> superClasses = getSuperClasses(className);
        if (!closest) {
            Collections.reverse(superClasses);
        }

        for (IndexedClass superClass : superClasses) {
            for (IndexedMethod method : methods) {
                // getInheritedMethods may return methods ON fqn itself
                String clz = method.getIn();
                if (superClass.getName().equals(clz) && !clz.equals(className)) {
                    return method;
                }
            }
        }
        if (!includeSelf) {
            return null;
        }
        return !methods.isEmpty() ? methods.iterator().next() : null;
    }

    /**
     * Gets all the methods that override the given method.
     *
     * @param methodName the name of the method.
     * @param className the (fqn) class name where the method is defined.
     * @return a set containing the overriding methods.
     */
    public Set<IndexedMethod> getOverridingMethods(String methodName, String className) {
        return getOverridingMethods(methodName, className, false);
    }

    /**
     * Gets all the methods that override the given method.
     *
     * @param methodName the name of the method.
     * @param className the (fqn) class name where the method is defined.
     * @param excludeSelf if true, excludes the overridden method itself from the results.
     * 
     * @return a set containing the overriding methods.
     */
    Set<IndexedMethod> getOverridingMethods(String methodName, String className, boolean excludeSelf) {
        Set<IndexedMethod> result = new HashSet<IndexedMethod>();
        Set<IndexedMethod> methods = getMethods(methodName, className, QuerySupport.Kind.EXACT);
        for (IndexedMethod method : methods) {
            Set<IndexedClass> subClasses = getSubClasses(method.getIn(), null, null, false);
            for (IndexedClass subClass : subClasses) {
                if (excludeSelf && className.equals(subClass.getIn())) {
                    continue;
                }
                result.addAll(getMethods(method.getName(), subClass.getName(), QuerySupport.Kind.EXACT));
            }
        }
        return result;
    }
    
    /**
     * Gets all the methods from the given class' hiearchy that override the 
     * given method.
     * 
     * @param methodName
     * @param className
     * @return
     */
    public Set<IndexedMethod> getAllOverridingMethodsInHierachy(String methodName, String className) {

        IndexedMethod superMethod = getSuperMethod(className, methodName, false);
        if (superMethod == null) {
            return Collections.emptySet();
        }
        Set<IndexedMethod> result = new HashSet<IndexedMethod>();
        result.add(superMethod);
        result.addAll(getOverridingMethods(superMethod.getName(), superMethod.getIn()));
        return result;
    }

    Set<IndexedMethod> getInheritedMethods(RubyType receiverType, String prefix, QuerySupport.Kind kind) {
        Set<IndexedMethod> methods = new HashSet<IndexedMethod>();
        for (String realType : receiverType.getRealTypes()) {
            methods.addAll(getInheritedMethods(realType, prefix, kind));
        }
        return methods;
    }

    /**
     * Get the set of inherited (through super classes and mixins) for the given fully qualified class name.
     * @param classFqn FQN: module1::module2::moduleN::class
     * @param prefix If kind is QuerySupport.Kind.PREFIX/CASE_INSENSITIVE_PREFIX, a prefix to filter methods by. Else,
     *    if kind is QuerySupport.Kind.EXACT filter methods by the exact name.
     * @param kind Whether the prefix field should be taken as a prefix or a whole name
     */
    public Set<IndexedMethod> getInheritedMethods(String classFqn, String prefix, QuerySupport.Kind kind) {
        boolean haveRedirected = false;

        if ((classFqn == null) || classFqn.equals(OBJECT)) {
            // Redirect inheritance tree to Class to pick up methods in Class and Module
            classFqn = CLASS;
            haveRedirected = true;
        } else if (MODULE.equals(classFqn) || CLASS.equals(classFqn)) {
            haveRedirected = true;
        }

        //String field = FIELD_FQN_NAME;
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

    /**
     * Like {@link #getInheritedMethods(java.lang.String, java.lang.String, org.netbeans.modules.parsing.spi.indexing.support.QuerySupport.Kind) }, 
     * ut if {@code includeSelf} is false, excludes results from the class itself.
     * @param classFqn
     * @param prefix
     * @param kind
     * @param includeSelf specifies whether methods from {@code classFqn} should be included.
     * @return
     */
    Set<IndexedMethod> getInheritedMethods(String classFqn, String prefix, QuerySupport.Kind kind, boolean includeSelf) {
        Set<IndexedMethod> inherited = getInheritedMethods(classFqn, prefix, kind);
        if (includeSelf) {
            return inherited;
        }
        Set<IndexedMethod> result = new HashSet<IndexedMethod>(inherited.size());
        for (IndexedMethod each : inherited) {
            if (!classFqn.equals(each.getClz())) {
                result.add(each);
            }
        }
        return result;
    }

    /** Return whether the specific class referenced (classFqn) was found or not. This is
     * not the same as returning whether any classes were added since it may add
     * additional methods from parents (Object/Class).
     */
    private boolean addMethodsFromClass(String prefix, QuerySupport.Kind kind, String classFqn,
        Set<IndexedMethod> methods, Set<String> seenSignatures, Set<String> scannedClasses,
        boolean haveRedirected, boolean inheriting) {

        // Prevent problems with circular includes or redundant includes
        if (scannedClasses.contains(classFqn)) {
            return false;
        }

        scannedClasses.add(classFqn);


        Set<IndexResult> result = new LinkedHashSet<IndexResult>();

        search(FIELD_FQN_NAME, classFqn, QuerySupport.Kind.EXACT, result);

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

        for (IndexResult map : result) {
            assert map != null;

            if (extendsClass == null) {
                extendsClass = map.getValue(FIELD_EXTENDS_NAME);
            }

            String includes = map.getValue(FIELD_INCLUDES);

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

            String extendWith = map.getValue(FIELD_EXTEND_WITH);

            if (extendWith != null) {
                // Try both with and without a package qualifier
                boolean isQualified = false;

                Set<IndexedMethod> extendWithMethods = new HashSet<IndexedMethod>();
                if (classIn != null) {
                    isQualified = addMethodsFromClass(prefix, kind, classIn + "::" + extendWith,
                            extendWithMethods, seenSignatures, scannedClasses, haveRedirected, true);
                }
                if (!isQualified) {
                    addMethodsFromClass(prefix, kind, extendWith, extendWithMethods, seenSignatures,
                        scannedClasses, haveRedirected, true);
                }
                // we need to explicitly set methods added via "extends with" as static
                // (we don't track methods added via extend to instances)
                for (IndexedMethod each : extendWithMethods) {
                    each.setStatic(true);
                }
                methods.addAll(extendWithMethods);
            }
            
            String[] signatures = map.getValues(FIELD_METHOD_NAME);
            String className = map.getValue(FIELD_CLASS_NAME);
            if (className == null) {
                className = "";
            }

            if (signatures != null) {
                for (String signature : signatures) {
                    // Skip weird methods like "[]" etc. in completion lists... TODO Think harder about this
                    if ((prefix.length() == 0) && !Character.isLowerCase(signature.charAt(0))) {
                        continue;
                    }

                    String seenSignature = signature + ";" + className;
                    // Prevent duplicates when method is redefined
                    if (!seenSignatures.contains(seenSignature)) {
                        if (signature.startsWith(prefix)) {
                            if (kind == QuerySupport.Kind.EXACT) {
                                // Ensure that the method is not longer than the prefix
                                if ((signature.length() > prefix.length()) &&
                                        (signature.charAt(prefix.length()) != '(') &&
                                        (signature.charAt(prefix.length()) != ';')) {
                                    continue;
                                }
                            } else {
                                // REGEXP, CAMELCASE filtering etc. not supported here
                                assert (kind == QuerySupport.Kind.PREFIX) ||
                                (kind == QuerySupport.Kind.CASE_INSENSITIVE_PREFIX);
                            }

                            seenSignatures.add(seenSignature);

                            IndexedMethod method = createMethod(signature, map, inheriting);
                            method.setSmart(!haveRedirected);
                            methods.add(method);
                        }
                    }
                }
            }

            String[] attributes = map.getValues(FIELD_ATTRIBUTE_NAME);

            if (attributes != null) {
                for (String attribute : attributes) {
                    // Skip weird methods like "[]" etc. in completion lists... TODO Think harder about this
                    if ((prefix.length() == 0) && !Character.isLowerCase(attribute.charAt(0))) {
                        continue;
                    }

                    // Prevent duplicates when method is redefined
                    if (!seenSignatures.contains(attribute)) {
                        if (attribute.startsWith(prefix)) {
                            if (kind == QuerySupport.Kind.EXACT) {
                                // Ensure that the method is not longer than the prefix
                                if ((attribute.length() > prefix.length()) &&
                                        (attribute.charAt(prefix.length()) != '(') &&
                                        (attribute.charAt(prefix.length()) != ';')) {
                                    continue;
                                }
                            } else {
                                // REGEXP, CAMELCASE filtering etc. not supported here
                                assert (kind == QuerySupport.Kind.PREFIX) ||
                                (kind == QuerySupport.Kind.CASE_INSENSITIVE_PREFIX);
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

        // need to add query methods so that they can be chained
        if (ACTIVE_RECORD_RELATION.equals(classFqn)) {
            addQueryMethods(prefix, kind, classFqn, methods);
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
            if (ACTIVE_RECORD_BASE.equals(extendsClass)) { // NOI18N
                // Add in database fields as well
                addDatabaseProperties(prefix, kind, classFqn, methods);
                // add in query methods if this appears to be in a rails 3 project (if AR::Relation is
                // indexed we make the assumption that this is a rails 3 project)
                if (scannedClasses.contains(ACTIVE_RECORD_RELATION)
                        || !getClasses(ACTIVE_RECORD_RELATION, Kind.EXACT, false, false, true).isEmpty()) {
                    addQueryMethods(prefix, kind, classFqn, methods);
                }
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

    private void addDatabaseProperties(String prefix, QuerySupport.Kind kind, String classFqn,
        Set<IndexedMethod> methods) {
        DatabasePropertiesIndexer.indexDatabaseProperties(this, prefix, kind, classFqn, methods);
    }

    private void addQueryMethods(String prefix, QuerySupport.Kind kind, String classFqn, Set<IndexedMethod> methods) {
        ActiveRecordQueryIndexer.indexQueryMehods(this, prefix, kind, classFqn, methods);
    }

    public Set<String> getDatabaseTables(String prefix, QuerySupport.Kind kind) {
        // Query index for database related properties
        
        String searchField = FIELD_DB_TABLE;
        Set<IndexResult> result = new HashSet<IndexResult>();
        search(searchField, prefix, kind, result);

        Set<String> tables = new HashSet<String>();
        for (IndexResult map : result) {
            assert map != null;

            String tableName = map.getValue(FIELD_DB_TABLE);
            if (tableName != null) {
                tables.add(tableName);
            }
        }
        
        return tables;
    }

    public Set<IndexedVariable> getGlobals(String prefix, QuerySupport.Kind kind) {
        // Query index for database related properties

        String searchField = FIELD_GLOBAL_NAME;
        Set<IndexResult> result = new HashSet<IndexResult>();
        // Only include globals from the user's sources, not in the libraries!
        search(searchField, prefix, kind, result);

        Set<IndexedVariable> globals = new HashSet<IndexedVariable>();
        for (IndexResult ir : result) {
            assert ir != null;

            String[] names = ir.getValues(FIELD_GLOBAL_NAME);
            if (names != null) {
                for (String name : names) {
                    int flags = 0;
                    IndexedVariable var = IndexedVariable.create(this, name, name, null, ir, null, name, flags, ElementKind.GLOBAL, context);
                    globals.add(var);
                }
            }
        }

        return globals;
    }

    public Set<? extends IndexedConstant> getConstants(final String constantFqn) {
        String[] parts = RubyUtils.parseConstantName(constantFqn);
        return getConstants(parts[0], parts[1]);
    }

    public Set<? extends IndexedConstant> getConstants(RubyType classFqn, String prefix) {
        Set<IndexedConstant> constants = new HashSet<IndexedConstant>();
        for (String realType : classFqn.getRealTypes()) {
            constants.addAll(getConstants(realType, prefix));
            for (String parentModule : RubyUtils.getParentModules(realType)) {
                constants.addAll(getConstants(parentModule, prefix));
            }
        }
        return constants;
    }

    public Set<? extends IndexedConstant> getConstants(String classFqn, String prefix) {
        boolean haveRedirected = false;

        if ((classFqn == null) || classFqn.equals(OBJECT)) {
            // Redirect inheritance tree to Class to pick up methods in Class and Module
            classFqn = CLASS;
            haveRedirected = true;
        } else if (MODULE.equals(classFqn) || CLASS.equals(classFqn)) {
            haveRedirected = true;
        }

        //String field = FIELD_FQN_NAME;
        Set<IndexedConstant> constants = new HashSet<IndexedConstant>();

        if (prefix == null) {
            prefix = "";
        }

        addConstantsFromClass(prefix, classFqn, constants, haveRedirected);

        return constants;
    }

    private boolean addConstantsFromClass(
            final String prefix,
            final String classFqn,
            final Set<? super IndexedConstant> constants,
            final boolean haveRedirected) {

        String searchField = FIELD_FQN_NAME;
        Set<IndexResult> result = new HashSet<IndexResult>();
        search(searchField, classFqn, QuerySupport.Kind.EXACT, result);

        // If this is a bogus class entry (no search rsults) don't continue
        if (result.size() <= 0) {
            return false;
        }

        for (IndexResult map : result) {
            assert map != null;

            String[] indexedConstants = map.getValues(FIELD_CONSTANT_NAME);

            if (indexedConstants != null) {
                for (String constant : indexedConstants) {
                    if (prefix.length() == 0 || constant.startsWith(prefix)) {
                        IndexedConstant c = createConstant(constant, map);
                        c.setSmart(!haveRedirected);
                        constants.add(c);
                    }
                }
            }
        }

        return true;
    }

    public Set<IndexedField> getInheritedFields(String classFqn, String prefix, QuerySupport.Kind kind, boolean inherited) {
        boolean haveRedirected = false;

        if ((classFqn == null) || classFqn.equals(OBJECT)) {
            // Redirect inheritance tree to Class to pick up methods in Class and Module
            classFqn = CLASS;
            haveRedirected = true;
        } else if (MODULE.equals(classFqn) || CLASS.equals(classFqn)) {
            haveRedirected = true;
        }

        //String field = FIELD_FQN_NAME;
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
    private boolean addFieldsFromClass(String prefix, QuerySupport.Kind kind, String classFqn,
        Set<IndexedField> methods, Set<String> seenSignatures, Set<String> scannedClasses,
        boolean haveRedirected, boolean instanceVars, boolean inheriting) {
        // Prevent problems with circular includes or redundant includes
        if (scannedClasses.contains(classFqn)) {
            return false;
        }

        scannedClasses.add(classFqn);

        String searchField = FIELD_FQN_NAME;

        Set<IndexResult> result = new HashSet<IndexResult>();

        search(searchField, classFqn, QuerySupport.Kind.EXACT, result);

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

        for (IndexResult map : result) {
            assert map != null;

            if (extendsClass == null) {
                extendsClass = map.getValue(FIELD_EXTENDS_NAME);
            }

            String includes = map.getValue(FIELD_INCLUDES);

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

            String extendWith = map.getValue(FIELD_EXTEND_WITH);

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
            

            String[] fields = map.getValues(FIELD_FIELD_NAME);

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
                        if (signatureIndex != -1) {
                            int flags = IndexedElement.stringToFlag(field, signatureIndex + 1);
                            isInstance = (flags & IndexedElement.STATIC) == 0;
                        }
                        if (isInstance != instanceVars) {
                            continue;
                        }
                        
                        if (field.startsWith(prefix)) {
                            if (kind == QuerySupport.Kind.EXACT) {
                                // Ensure that the method is not longer than the prefix
                                if ((field.length() > prefix.length()) &&
                                        (field.charAt(prefix.length()) != '(') &&
                                        (field.charAt(prefix.length()) != ';')) {
                                    continue;
                                }
                            } else {
                                // REGEXP, CAMELCASE filtering etc. not supported here
                                assert (kind == QuerySupport.Kind.PREFIX) ||
                                (kind == QuerySupport.Kind.CASE_INSENSITIVE_PREFIX);
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
        final Set<IndexResult> result = new HashSet<IndexResult>();
        String field = FIELD_FQN_NAME;

        search(field, fqn, QuerySupport.Kind.EXACT, result);

        Set<IndexedClass> matches = new HashSet<IndexedClass>();

        for (IndexResult map : result) {
            assert map != null;

            String attributes = map.getValue(FIELD_CLASS_ATTRS);

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
        final Set<IndexResult> result = new HashSet<IndexResult>();
        String field = FIELD_FQN_NAME;

        search(field, fqn, QuerySupport.Kind.EXACT, result);

        Set<IndexedMethod> matches = new HashSet<IndexedMethod>();

        for (IndexResult map : result) {
            String[] signatures = map.getValues(FIELD_METHOD_NAME);

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

            String[] attribs = map.getValues(FIELD_ATTRIBUTE_NAME);

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

    /** Return the {@link FileObject} corresponding to the given require statement */
    public FileObject getRequiredFile(final String require) {
        final Set<IndexResult> result = new HashSet<IndexResult>();

        String field = FIELD_REQUIRE;

        search(field, require, QuerySupport.Kind.EXACT, result);

        // TODO Prune methods to fit my scheme - later make lucene index smarter about how to prune its index search
        for (IndexResult ir : result) {
            FileObject file = ir.getFile();

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
    public static void setClusterUrl(String url) {
        clusterUrl = url;
    }

    static String getPreindexUrl(String url) {
        return getPreindexUrl(url, null);
    }
    static String getPreindexUrl(String url, FileObject context) {
        // no preindexing in parsing api
//        if (RubyIndexer.isPreindexing()) {
//            Iterator<RubyPlatform> it = null;
//            if (context != null && context.isValid()) {
//                Project project = FileOwnerQuery.getOwner(context);
//                if (project != null) {
//                    RubyPlatform platform = RubyPlatform.platformFor(project);
//                    if (platform != null) {
//                        it = Collections.singleton(platform).iterator();
//                    }
//                }
//            }
//            if (it == null) {
//                it = RubyPlatformManager.platformIterator();
//            }
//            while (it.hasNext()) {
//                RubyPlatform platform = it.next();
//                String s = getGemHomeURL(platform);
//
//                if (s != null && url.startsWith(s)) {
//                    return GEM_URL + url.substring(s.length());
//                }
//
//                s = platform.getHomeUrl();
//
//                if (url.startsWith(s)) {
//                    url = RUBYHOME_URL + url.substring(s.length());
//
//                    return url;
//                }
//            }
//        } else {
            // FIXME: use right platform
        RubyPlatform platform = RubyPlatformManager.getDefaultPlatform();
        if (platform != null) {
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
//        }

        String s = getClusterUrl();

        if (url.startsWith(s)) {
            return CLUSTER_URL + url.substring(s.length());
        }

        return url;
    }

    /** Get the FileObject corresponding to a URL returned from the index */
    public static FileObject getFileObject(String url) {
        return getFileObject(url, null);
    }
    
    public static FileObject getFileObject(String url, FileObject context) {
        try {
            if (url.startsWith(RUBYHOME_URL)) {
                Iterator<RubyPlatform> it = null;
                if (context != null) {
                    Project project = FileOwnerQuery.getOwner(context);
                    if (project != null) {
                        RubyPlatform platform = RubyPlatform.platformFor(project);
                        if (platform != null) {
                            it = Collections.singleton(platform).iterator();
                        }
                    }
                }
                if (it == null) {
                    it = RubyPlatformManager.platformIterator();
                }
                while (it.hasNext()) {
                    RubyPlatform platform = it.next();
                    String u = platform.getHomeUrl() + url.substring(RUBYHOME_URL.length());
                    FileObject fo = URLMapper.findFileObject(new URL(u));
                    if (fo != null) {
                        return fo;
                    }
                }
                
                return null;
            } else if (url.startsWith(GEM_URL)) {
                Iterator<RubyPlatform> it = null;
                if (context != null) {
                    Project project = FileOwnerQuery.getOwner(context);
                    if (project != null) {
                        RubyPlatform platform = RubyPlatform.platformFor(project);
                        if (platform != null) {
                            it = Collections.singleton(platform).iterator();
                        }
                    }
                }
                if (it == null) {
                    it = RubyPlatformManager.platformIterator();
                }
                while (it.hasNext()) {
                    RubyPlatform platform = it.next();
                    if (!platform.hasRubyGemsInstalled()) {
                        continue;
                    }
                    GemManager gemManager = platform.getGemManager();
                    if (gemManager != null) {
                        String u = gemManager.getGemHomeUrl() + url.substring(GEM_URL.length());
                        FileObject fo = URLMapper.findFileObject(new URL(u));
                        if (fo != null) {
                            return fo;
                        }
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
