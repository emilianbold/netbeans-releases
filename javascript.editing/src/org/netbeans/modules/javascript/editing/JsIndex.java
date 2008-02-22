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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.editing;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.fpi.gsf.ElementKind;
import org.netbeans.fpi.gsf.Index;
import org.netbeans.fpi.gsf.Index.SearchResult;
import org.netbeans.fpi.gsf.Index.SearchScope;
import org.netbeans.fpi.gsf.NameKind;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.URLMapper;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;

/**
 *
 * @author Tor Norbye
 */
public class JsIndex {
    private static String clusterUrl = null;
    private static final String CLUSTER_URL = "cluster:"; // NOI18N
    //private static final String RUBYHOME_URL = "ruby:"; // NOI18N
    //private static final String GEM_URL = "gem:"; // NOI18N

    static final Set<SearchScope> ALL_SCOPE = EnumSet.allOf(SearchScope.class);
    static final Set<SearchScope> SOURCE_SCOPE = EnumSet.of(SearchScope.SOURCE);
    private final Index index;

    /** Creates a new instance of JsIndex */
    public JsIndex(Index index) {
        this.index = index;
    }

    public static JsIndex get(Index index) {
        return new JsIndex(index);
    }

//    private boolean search(String key, String name, NameKind kind, Set<SearchResult> result) {
//        try {
//            index.gsfSearch(key, name, kind, ALL_SCOPE, result);
//
//            return true;
//        } catch (IOException ioe) {
//            Exceptions.printStackTrace(ioe);
//
//            return false;
//        }
//    }

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

    
    static void setClusterUrl(String url) {
        clusterUrl = url;
    }

    static String getPreindexUrl(String url) {
//        String s = JsInstallation.getInstance().getGemHomeUrl();
//
//        if (s != null && url.startsWith(s)) {
//            return GEM_URL + url.substring(s.length());
//        }
//
//        s = JsInstallation.getInstance().getJsHomeUrl();
//
//        if (url.startsWith(s)) {
//            url = RUBYHOME_URL + url.substring(s.length());
//
//            return url;
//        }
//
        String s = getClusterUrl();

        if (url.startsWith(s)) {
            return CLUSTER_URL + url.substring(s.length());
        }

        return url;
    }

    /** Get the FileObject corresponding to a URL returned from the index */
    public static FileObject getFileObject(String url) {
        try {
            /*if (url.startsWith(RUBYHOME_URL)) {
                url = JsInstallation.getInstance().getJsHomeUrl() +
                    url.substring(RUBYHOME_URL.length()); // NOI18N
            } else if (url.startsWith(GEM_URL)) {
                url = JsInstallation.getInstance().getGemHomeUrl() +
                    url.substring(GEM_URL.length()); // NOI18N
            } else*/ if (url.startsWith(CLUSTER_URL)) {
                url = getClusterUrl() + url.substring(CLUSTER_URL.length()); // NOI18N
            }

            return URLMapper.findFileObject(new URL(url));
        } catch (MalformedURLException mue) {
            Exceptions.printStackTrace(mue);
        }

        return null;
    }

    static String getClusterUrl() {
        if (clusterUrl == null) {
            File f =
                InstalledFileLocator.getDefault()
                                    .locate("modules/org-netbeans-modules-javascript-editing.jar", null, false); // NOI18N

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
    
    public Set<IndexedFunction> getConstructors(final String name, NameKind kind,
        Set<Index.SearchScope> scope) {
        return getFunctions(name, null, kind, scope, true, null, true);
    }

    public Set<IndexedFunction> getFunctions(String name, String in, NameKind kind,
        Set<Index.SearchScope> scope, JsParseResult context, boolean includeMethods) {
        return getFunctions(name, in, kind, scope, false, context, includeMethods);
    }
    
//    @SuppressWarnings("fallthrough")
    private Set<IndexedFunction> getFunctions(String name, String in, NameKind kind,
        Set<Index.SearchScope> scope, boolean onlyConstructors, JsParseResult context, boolean includeMethods) {
        
        //    public void searchByCriteria(final String name, final ClassIndex.NameKind kind, /*final ResultConvertor<T> convertor,*/ final Set<String> result) throws IOException {
        final Set<SearchResult> result = new HashSet<SearchResult>();

        //        if (!isValid()) {
        //            LOGGER.fine(String.format("LuceneIndex[%s] is invalid!\n", this.toString()));
        //            return;
        //        }
        String field = JsIndexer.FIELD_JS_FUNCTION;
        NameKind originalKind = kind;
        if (kind == NameKind.EXACT_NAME) {
            // I can't do exact searches on methods because the method
            // entries include signatures etc. So turn this into a prefix
            // search and then compare chopped off signatures with the name
            kind = NameKind.PREFIX;
        }
        
        if (kind == NameKind.CASE_INSENSITIVE_PREFIX) {
            // TODO - handle
            kind = NameKind.PREFIX;
        }

        // No point in doing case insensitive searches on method names because
        // method names in Js are always case insensitive anyway
        //            case CASE_INSENSITIVE_PREFIX:
        //            case CASE_INSENSITIVE_REGEXP:
        //                field = JsIndexer.FIELD_CASE_INSENSITIVE_METHOD_NAME;
        //                break;

        search(field, name, kind, result, scope);

        //return Collections.unmodifiableSet(result);  result.size()

        // TODO Prune methods to fit my scheme - later make lucene index smarter about how to prune its index search
        final Set<IndexedFunction> functions = new HashSet<IndexedFunction>();
        
        String searchUrl = null;
        if (context != null) {
            try {
                searchUrl = context.getFile().getFileObject().getURL().toExternalForm();
            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        for (SearchResult map : result) {
            String[] signatures = map.getValues(field);
            
            if (signatures != null) {
                // Check if this file even applies
                if (context != null) {
                    String fileUrl = map.getPersistentUrl();
                    if (searchUrl == null || !searchUrl.equals(fileUrl)) {
                        boolean isLibrary = fileUrl.indexOf("jsstubs") != -1; // TODO - better algorithm
                        if (!isReachable(context, fileUrl, isLibrary)) {
                            continue;
                        }
                    }
                }
                
                for (String signature : signatures) {
                    // Lucene returns some inexact matches, TODO investigate why this is necessary
                    if ((kind == NameKind.PREFIX) && !signature.startsWith(name)) {
                        continue;
                    } else if (kind == NameKind.CASE_INSENSITIVE_PREFIX && !signature.regionMatches(true, 0, name, 0, name.length())) {
                        continue;
                    } else if (kind == NameKind.CASE_INSENSITIVE_REGEXP) {
                        int end = signature.indexOf(';');
                        assert end != -1;
                        String n = signature.substring(0, end);
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
                                (signature.charAt(name.length()) != ';'))) {
                            continue;
                        }
                    }

                    // XXX THIS DOES NOT WORK WHEN THERE ARE IDENTICAL SIGNATURES!!!
                    assert map != null;
                    
                    int nameEndIdx = signature.indexOf(';');
                    assert nameEndIdx != -1;
                    String funcName = signature.substring(0, nameEndIdx);
                    nameEndIdx++;

                    String funcIn = null;
                    int inEndIdx = signature.indexOf(';', nameEndIdx);
                    assert inEndIdx != -1;
                    if (inEndIdx > nameEndIdx+1) {
                        funcIn = signature.substring(nameEndIdx, inEndIdx);
                    }
                    inEndIdx++;
                    
                    // Filter out methods on other classes
                    if (!includeMethods && (funcIn != null)) {
                        continue;
                    } else if (in != null && (funcIn == null || !funcIn.equals(in))) {
                        continue;
                    }
                    
                    IndexedFunction func = createFunction(signature, map, funcName, funcIn);
                    if (onlyConstructors && func.getKind() != ElementKind.CONSTRUCTOR) {
                        continue;
                    }
                    functions.add(func);
                }
            }
            // TODO - fields
        }

        return functions;
    }

    private IndexedFunction createFunction(String attributes, SearchResult map, String name, String in) {
//        String clz = map.getValue(JsIndexer.FIELD_CLASS_NAME);
//        String module = map.getValue(JsIndexer.FIELD_IN);
//
//        if (clz == null) {
//            // Module method?
//            clz = module;
//        } else if ((module != null) && (module.length() > 0)) {
//            clz = module + "::" + clz;
//        }
//
        String fileUrl = map.getPersistentUrl();
//        String fqn = map.getValue(JsIndexer.FIELD_FQN_NAME);
//        String require = map.getValue(JsIndexer.FIELD_REQUIRE);
//
//        // Extract attributes
//        int attributeIndex = signature.indexOf(';');
//        String attributes = null;
        int flags = 0;
//
//        if (attributeIndex != -1) {
//            flags = IndexedElement.stringToFlag(signature, attributeIndex+1);
//
//            if (signature.length() > attributeIndex+1) {
//                attributes = signature.substring(attributeIndex+1, signature.length());
//            }
//
//            signature = signature.substring(0, attributeIndex);
//        }

        ElementKind kind = Character.isUpperCase(name.charAt(0)) ?
            ElementKind.CONSTRUCTOR : ElementKind.METHOD;
        
//        String attributes = "";
//        int attributeIndex = signature.indexOf(':');
//        if (attributeIndex != -1) {
//            attributes = signature.substring(attributeIndex+1);
//            signature = signature.substring(0, attributeIndex);
//        }
       
        IndexedFunction m = new IndexedFunction(name, in, this, fileUrl, attributes, flags, kind);

//        m.setInherited(inherited);
        return m;
    }
        
    /** 
     * Decide whether the given url is included from the current compilation
     * context.
     * This will typically return true for all library files, and false for
     * all source level files unless that file is reachable through include-mechanisms
     * from the current file.
     * 
     * @todo Add some smarts here to correlate remote URLs (http:// pointers to dojo etc)
     *   with local copies of these.
     * @todo Do some kind of transitive check? Probably not - there isn't a way to do
     *    includes of files that contain other files (you can import a .js file, but that
     *    js file can't include other files)
     */
    public boolean isReachable(JsParseResult result, String url, boolean isLibrary) {
        List<String> imports = result.getStructure().getImports();
        if (imports.size() > 0) {
            // TODO - do some heuristics to deal with relative paths here,
            // e.g.   <script src="../../foo.js"></script>

            for (int i = 0, n = imports.size(); i < n; i++) {
                String imp = imports.get(i);
                if (imp.indexOf("../") != -1) {
                    int lastIndex = imp.lastIndexOf("../");
                    imp = imp.substring(lastIndex+3);
                    if (imp.length() == 0) {
                        continue;
                    }
                }
                if (url.endsWith(imp)) {
                    return true;
                }
            }
        }

        return isLibrary;
    }
}
