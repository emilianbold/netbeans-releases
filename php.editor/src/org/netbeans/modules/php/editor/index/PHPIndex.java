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
package org.netbeans.modules.php.editor.index;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.api.Index;
import org.netbeans.modules.gsf.api.Index.SearchResult;
import org.netbeans.modules.gsf.api.Index.SearchScope;
import org.netbeans.modules.gsf.api.NameKind;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class PHPIndex {

    /** Set property to true to find ALL functions regardless of file includes */
    //private static final boolean ALL_REACHABLE = Boolean.getBoolean("javascript.findall");
    private static String clusterUrl = null;
    private static final String CLUSTER_URL = "cluster:"; // NOI18N

    static final Set<SearchScope> ALL_SCOPE = EnumSet.allOf(SearchScope.class);
    static final Set<SearchScope> SOURCE_SCOPE = EnumSet.of(SearchScope.SOURCE);
    private static final Set<String> TERMS_BASE = Collections.singleton(PHPIndexer.FIELD_BASE);
    private static final Set<String> TERMS_CONST = Collections.singleton(PHPIndexer.FIELD_CONST);
    private final Index index;

    /** Creates a new instance of JsIndex */
    public PHPIndex(Index index) {
        this.index = index;
    }

    public static PHPIndex get(Index index) {
        return new PHPIndex(index);
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

    //public needed for tests (see org.netbeans.modules.php.editor.nav.TestBase):
    public static void setClusterUrl(String url) {
        clusterUrl = url;
    }

    static String getPreindexUrl(String url) {
        String s = getClusterUrl();

        if (url.startsWith(s)) {
            return CLUSTER_URL + url.substring(s.length());
        }

        return url;
    }

    /** Get the FileObject corresponding to a URL returned from the index */
    public static FileObject getFileObject(String url) {
        try {
            if (url.startsWith(CLUSTER_URL)) {
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
                    InstalledFileLocator.getDefault().locate("modules/org-netbeans-modules-javascript-editing.jar", null, false); // NOI18N

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
    
    public Collection<IndexedFunction> getMethods(PHPParseResult context, String className, String name, NameKind kind) {
        final Set<SearchResult> classSearchResult = new HashSet<SearchResult>();
        Collection<IndexedFunction> functions = new ArrayList<IndexedFunction>();
        search(PHPIndexer.FIELD_CLASS, className, NameKind.PREFIX, classSearchResult, ALL_SCOPE, TERMS_BASE);

        for (SearchResult classMap : classSearchResult) {
            String[] signatures = classMap.getValues(PHPIndexer.FIELD_METHOD);

            if (signatures == null) {
                continue;
            }

            for (String signature : signatures) {
                int firstSemicolon = signature.indexOf(";");
                String funcName = signature.substring(0, firstSemicolon);
                
                if (funcName.toLowerCase().startsWith(name.toLowerCase())) {
                    IndexedFunction func = (IndexedFunction) IndexedElement.create(signature,
                            classMap.getPersistentUrl(), funcName, className, 0, this, false);

                    functions.add(func);
                }
            }
        }
        return functions;
    }
    
    public Collection<IndexedConstant> getProperties(PHPParseResult context, String className, String name, NameKind kind) {
        final Set<SearchResult> classSearchResult = new HashSet<SearchResult>();
        Collection<IndexedConstant> properties = new ArrayList<IndexedConstant>();
        search(PHPIndexer.FIELD_CLASS, className, NameKind.PREFIX, classSearchResult, ALL_SCOPE, TERMS_BASE);

        for (SearchResult classMap : classSearchResult) {
            String[] signatures = classMap.getValues(PHPIndexer.FIELD_FIELD);

            if (signatures == null) {
                continue;
            }

            for (String signature : signatures) {
                int firstSemicolon = signature.indexOf(";");
                String propName = signature.substring(0, firstSemicolon);
                
                if (propName.toLowerCase().startsWith(name.toLowerCase())) {
                    int offset = extractOffsetFromIndexSignature(signature, 1);
                    
                    IndexedConstant prop = new IndexedConstant(propName, className,
                            this, classMap.getPersistentUrl(), null, 0, offset);

                    properties.add(prop);
                }
            }
        }
        return properties;
    }
    
    static int extractOffsetFromIndexSignature(String signature, int offsetSection) {
        assert offsetSection != 0; // Obtain directly, and logic below (+1) is wrong
        int startIndex = 0;
        
        for (int i = 0; i < offsetSection; i++) {
            startIndex = signature.indexOf(';', startIndex + 1);
        }

        assert startIndex != -1;
        startIndex ++;
        int endIndex = signature.indexOf(';', startIndex);
        
        if (endIndex > startIndex){
            String offsetStr = signature.substring(startIndex, endIndex);
            return Integer.parseInt(offsetStr);
        }
        
        return -1;
    }
    
    public Collection<IndexedFunction> getFunctions(PHPParseResult context, String name, NameKind kind) {
        final Set<SearchResult> result = new HashSet<SearchResult>();
        Collection<IndexedFunction> functions = new ArrayList<IndexedFunction>();
        search(PHPIndexer.FIELD_BASE, name, kind, result, ALL_SCOPE, TERMS_BASE);

        for (SearchResult map : result) {
            if (map.getPersistentUrl() != null && isReachable(context, map.getPersistentUrl())) {
                String[] signatures = map.getValues(PHPIndexer.FIELD_BASE);

                if (signatures == null) {
                    continue;
                }

                for (String signature : signatures) {
                    int firstSemicolon = signature.indexOf(";");
                    String funcName = signature.substring(0, firstSemicolon);

                    IndexedFunction func = (IndexedFunction) IndexedElement.create(signature,
                            map.getPersistentUrl(), funcName, null, 0, this, false);

                    functions.add(func);
                }
            }
        }
        return functions;
    }
    
    public Collection<IndexedConstant> getConstants(PHPParseResult context, String name, NameKind kind) {
        final Set<SearchResult> result = new HashSet<SearchResult>();
        Collection<IndexedConstant> constants = new ArrayList<IndexedConstant>();
        search(PHPIndexer.FIELD_CONST, name, kind, result, ALL_SCOPE, TERMS_CONST);

        for (SearchResult map : result) {
            if (map.getPersistentUrl() != null && isReachable(context, map.getPersistentUrl())) {
                String[] signatures = map.getValues(PHPIndexer.FIELD_CONST);

                if (signatures == null) {
                    continue;
                }

                for (String signature : signatures) {
                    String constName = signature.substring(0, signature.indexOf(';'));
                    int offset = extractOffsetFromIndexSignature(signature, 1);

                    IndexedConstant constant = new IndexedConstant(constName, null,
                            this, map.getPersistentUrl(), null, 0, offset);

                    constants.add(constant);
                }
            }
        }
        
        return constants;
    }
    
    public Collection<IndexedConstant> getClasses(PHPParseResult context, String name, NameKind kind) {
        final Set<SearchResult> result = new HashSet<SearchResult>();
        Collection<IndexedConstant> constants = new ArrayList<IndexedConstant>();
        search(PHPIndexer.FIELD_CLASS, name, kind, result, ALL_SCOPE, TERMS_BASE);

        for (SearchResult map : result) {
            if (map.getPersistentUrl() != null && isReachable(context, map.getPersistentUrl())) {
                String[] signatures = map.getValues(PHPIndexer.FIELD_CLASS);

                if (signatures == null) {
                    continue;
                }

                for (String signature : signatures) {
                    int firstSemicolon = signature.indexOf(";");
                    String className = signature.substring(0, firstSemicolon);
                    int offset = extractOffsetFromIndexSignature(signature, 1);

                    IndexedConstant constant = new IndexedConstant(className, null,
                            this, map.getPersistentUrl(), null, 0, offset);

                    constants.add(constant);
                }
            }
        }
        
        return constants;
    }
    
    public Collection<String>getDirectIncludes(String fileURL){
        ArrayList includes = new ArrayList();
        final Set<SearchResult> result = new HashSet<SearchResult>();
        search("filename", fileURL, NameKind.EXACT_NAME, result, ALL_SCOPE, TERMS_BASE); //NOI18N
        
        for (SearchResult map : result) {
            if (map.getPersistentUrl() != null) {
                String[] signatures = map.getValues(PHPIndexer.FIELD_INCLUDE);

                if (signatures == null) {
                    continue;
                }

                for (String signature : signatures) {
                    
                    for (String incl : signature.split(";")){
                        if (incl.length() > 0) {
                            includes.add(incl);
                        }
                    }
                }
            }
        }
        
        return includes;
    }
    
    public Collection<String>getAllIncludes(String fileURL){
        return getAllIncludes(fileURL, (Collection<String>)Collections.EMPTY_LIST);
    }

    private Collection<String>getAllIncludes(String fileURL, Collection<String> alreadyProcessed){
        Collection<String> includes = new TreeSet<String>();
        includes.add(fileURL.substring("file:".length())); //NOI18N
        includes.addAll(alreadyProcessed);
        Collection<String> directIncludes = getDirectIncludes(fileURL);
        
        for (String directInclude : directIncludes){
            if (!includes.contains(directInclude)){
                includes.addAll(getAllIncludes(directInclude, includes));
            }
        }
        
        return includes;
    }
    
    /** 
     * Decide whether the given url is included from the current compilation
     * context.
     * This will typically return true for all library files, and false for
     * all source level files unless that file is reachable through include-mechanisms
     * from the current file.
     */
    public boolean isReachable(PHPParseResult result, String url) {
        Project project = FileOwnerQuery.getOwner(result.getFile().getFileObject());
        
        if (project != null){
            try {
                // return true for platform files
                // TODO temporary implementation
                PhpSourcePath phpSourcePath = project.getLookup().lookup(PhpSourcePath.class);
                if (phpSourcePath != null) {
                    File file = new File(new URI(url));
                    assert file.exists() : "PHP Index is refering to a non-existing file " + url;
                    
                    FileObject fileObject = FileUtil.toFileObject(file);
                    PhpSourcePath.FileType fileType = phpSourcePath.getFileType(fileObject);
                    if (fileType == PhpSourcePath.FileType.INTERNAL
                            || fileType == PhpSourcePath.FileType.INCLUDE) {
                        return true;
                    }
                }
            } catch (URISyntaxException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        String processedFileURL = null;
        
        try {
            processedFileURL = result.getFile().getFileObject().getURL().toExternalForm();
            
            if (url.equals(processedFileURL)){
                return true;
            }
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        Collection<String> includeList = getAllIncludes(url);
        
        for (String includeURL : includeList){
            if (url.equals("file:" + includeURL)){ //NOI18N
                return true;
            }
        }

        return false;
    }
    
    static String dequote(String string){
        assert string.length() >= 2;
        assert string.startsWith("\"") || string.startsWith("'");
        assert string.endsWith("\"") || string.endsWith("'");
        return string.substring(1, string.length() - 1);
    }
    
    // copied from JspUtils
    /** Returns an absolute context URL (starting with '/') for a relative URL and base URL.
    *  @param relativeTo url to which the relative URL is related. Treated as directory iff
    *    ends with '/'
    *  @param url the relative URL by RFC 2396
    *  @exception IllegalArgumentException if url is not absolute and relativeTo 
    * can not be related to, or if url is intended to be a directory
    */
    static String resolveRelativeURL(String relativeTo, String url) {
        //System.out.println("- resolving " + url + " relative to " + relativeTo);
        String result;
        if (url.startsWith("/")) { // NOI18N
            result = "/"; // NOI18N
            url = url.substring(1);
        }
        else {
            // canonize relativeTo
            if ((relativeTo == null) || (!relativeTo.startsWith("/"))) // NOI18N
                throw new IllegalArgumentException();
            relativeTo = resolveRelativeURL(null, relativeTo);
            int lastSlash = relativeTo.lastIndexOf('/');
            if (lastSlash == -1)
                throw new IllegalArgumentException();
            result = relativeTo.substring(0, lastSlash + 1);
        }

        // now url does not start with '/' and result starts with '/' and ends with '/'
        StringTokenizer st = new StringTokenizer(url, "/", true); // NOI18N
        while(st.hasMoreTokens()) {
            String tok = st.nextToken();
            //System.out.println("token : \"" + tok + "\""); // NOI18N
            if (tok.equals("/")) { // NOI18N
                if (!result.endsWith("/")) // NOI18N
                    result = result + "/"; // NOI18N
            }
            else
                if (tok.equals("")) // NOI18N
                    ;  // do nohing
                else
                    if (tok.equals(".")) // NOI18N
                        ;  // do nohing
                    else
                        if (tok.equals("..")) { // NOI18N
                            String withoutSlash = result.substring(0, result.length() - 1);
                            int ls = withoutSlash.lastIndexOf("/"); // NOI18N
                            if (ls != -1)
                                result = withoutSlash.substring(0, ls + 1);
                        }
                        else {
                            // some file
                            result = result + tok;
                        }
            //System.out.println("result : " + result); // NOI18N
        }
        //System.out.println("- resolved to " + result);
        return result;
    }
}
