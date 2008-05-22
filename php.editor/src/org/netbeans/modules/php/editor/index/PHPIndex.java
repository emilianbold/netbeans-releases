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
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.gsf.api.ElementKind;
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
    
    /** returns all methods of a class. */
    public Collection<IndexedFunction> getAllMethods(PHPParseResult context, String className, String name, NameKind kind) {
        Collection<IndexedFunction> methods = new ArrayList<IndexedFunction>();
        List<IndexedClass> inheritanceLine = getClassInheritanceLine(context, className);
        
        if (inheritanceLine != null){
            for (IndexedClass clazz : inheritanceLine){
                methods.addAll(getMethods(context, clazz.getName(), "", kind)); //NOI18N
            }
        }
        
        return methods;
    }
    
    /** returns all fields of a class. */
    public Collection<IndexedConstant> getAllProperties(PHPParseResult context, String className, String name, NameKind kind) { 
        Collection<IndexedConstant> properties = new ArrayList<IndexedConstant>();
        List<IndexedClass> inheritanceLine = getClassInheritanceLine(context, className);
        
        if (inheritanceLine != null){
            for (IndexedClass clazz : inheritanceLine){
                properties.addAll(getProperties(context, clazz.getName(), "", NameKind.PREFIX)); //NOI18N
            }
        }
        
        return properties;
    }
    
    /** return a list of all superclasses of the given class. */
    public List<IndexedClass>getClassInheritanceLine(PHPParseResult context, String className){
        List<IndexedClass>classLine = new LinkedList<IndexedClass>();
        Collection<String> processedClasses = new TreeSet<String>();
        
        while (className != null){
            if (processedClasses.contains(className)){
                break; //TODO: circular reference, warn the user
            }
            
            processedClasses.add(className);
            
            Collection<IndexedClass>classes = getClasses(context, className, NameKind.EXACT_NAME);
            
            if (classes == null || classes.size() == 0){
                break;
            }
            
            //TODO: handle name conflicts
            IndexedClass clazz = classes.toArray(new IndexedClass[classes.size()])[0];
            classLine.add(clazz);
            className = clazz.getSuperClass();
        }
        
        return classLine;
    }
    
    /** returns local constnats of a class. */
    public Collection<IndexedConstant> getClassConstants(PHPParseResult context, String className, String name, NameKind kind) { 
        Collection<IndexedConstant> properties = new ArrayList<IndexedConstant>();
        Map<String, String> signaturesMap = getClassSpecificSignatures(context, className, PHPIndexer.FIELD_CLASS_CONST, name, kind);
        
        for (String signature : signaturesMap.keySet()) {
            //items are not indexed, no case insensitive search key user
            Signature sig = Signature.get(signature);
            String propName = sig.string(0);
            int offset = sig.integer(1);
            
            IndexedConstant prop = new IndexedConstant(propName, className,
                    this, signaturesMap.get(signature), offset, offset, null);

            properties.add(prop);

        }

        return properties;
    }
    
    /** returns methods of a class. */
    public Collection<IndexedFunction> getMethods(PHPParseResult context, String className, String name, NameKind kind) {
        Collection<IndexedFunction> methods = new ArrayList<IndexedFunction>();
        Map<String, String> signaturesMap = getClassSpecificSignatures(context, className, PHPIndexer.FIELD_METHOD, name, kind);
        
        for (String signature : signaturesMap.keySet()) {
            //items are not indexed, no case insensitive search key user
            Signature sig = Signature.get(signature);
            String funcName = sig.string(0);
            String args = sig.string(1);
            int offset = sig.integer(2);
            int flags = sig.integer(3);

            IndexedFunction func = new IndexedFunction(funcName, className,
                    this, signaturesMap.get(signature), args, offset, flags, ElementKind.METHOD);

            methods.add(func);

        }
    
        return methods;
    }
    
    /** returns fields of a class. */
    public Collection<IndexedConstant> getProperties(PHPParseResult context, String className, String name, NameKind kind) { 
        Collection<IndexedConstant> properties = new ArrayList<IndexedConstant>();
        Map<String, String> signaturesMap = getClassSpecificSignatures(context, className, PHPIndexer.FIELD_FIELD, name, kind);
        
        for (String signature : signaturesMap.keySet()) {
            Signature sig = Signature.get(signature);
            
            String propName = "$" + sig.string(0);
            int offset = sig.integer(1);
            int modifiers = sig.integer(2);

            IndexedConstant prop = new IndexedConstant(propName, className,
                    this, signaturesMap.get(signature), offset, modifiers, null);

            properties.add(prop);

        }

        return properties;
    }
    
    private Map<String, String> getClassSpecificSignatures(PHPParseResult context, String className, String fieldName, String name, NameKind kind) {
        final Set<SearchResult> classSearchResult = new HashSet<SearchResult>();
        Map<String, String> signatures = new HashMap<String, String>();
        search(PHPIndexer.FIELD_CLASS, className.toLowerCase(), NameKind.PREFIX, classSearchResult, ALL_SCOPE, TERMS_BASE);

        for (SearchResult classMap : classSearchResult) {
            String[] classSignatures = classMap.getValues(PHPIndexer.FIELD_CLASS);
            String[] rawSignatures = classMap.getValues(fieldName);
            
            if (classSignatures == null  || rawSignatures == null) {
                continue;
            }
            
            assert classSignatures.length == 1; 
            String foundClassName = getSignatureItem(classSignatures[0], 1);
            String persistentURL = classMap.getPersistentUrl();
            
            if (!className.equals(foundClassName) || (context != null && !isReachable(context, persistentURL))) {
                continue;
            }
            
            if(kind == NameKind.PREFIX) {
                //case sensitive
                if(!foundClassName.startsWith(className)) {
                    continue;
                }
            }

            for (String signature : rawSignatures) {
                String elemName = getSignatureItem(signature, 0);
                
                // TODO: now doing IC prefix search only, handle other search types 
                // according to 'kind'
                if((kind == NameKind.CASE_INSENSITIVE_PREFIX 
                        && elemName.toLowerCase().startsWith(name.toLowerCase()))
                        || (kind == NameKind.PREFIX 
                        && elemName.startsWith(name))) {
                        signatures.put(signature, persistentURL);
                }
                
            }
        }
        
        return signatures;
    }
    
    //faster parsing of signatures.
    //use Signature class if you need to search in the same signature
    //multiple times
    static String getSignatureItem(String signature, int index) {
        int searchIndex = 0;
        for(int i = 0; i < signature.length(); i++) {
            char c = signature.charAt(i);
            
            if(searchIndex == index) {
                for(int j = i ; j < signature.length(); j++) {
                    c = signature.charAt(j);
                    if(c == ';') {
                        return signature.substring(i, j);
                    }
                }
            }
            
            if(c == ';') {
                searchIndex++;
            }
        }
        return null;
    }

    /** returns GLOBAL functions. */
    public Collection<IndexedFunction> getFunctions(PHPParseResult context, String name, NameKind kind) {
        final Set<SearchResult> result = new HashSet<SearchResult>();
        Collection<IndexedFunction> functions = new ArrayList<IndexedFunction>();
        search(PHPIndexer.FIELD_BASE, name.toLowerCase(), NameKind.PREFIX, result, ALL_SCOPE, TERMS_BASE);

        for (SearchResult map : result) {
            if (map.getPersistentUrl() != null) {
                String[] signatures = map.getValues(PHPIndexer.FIELD_BASE);

                if (signatures == null) {
                    continue;
                }

                for (String signature : signatures) {
                    Signature sig = Signature.get(signature);
                    //sig.string(0) is the case insensitive search key
                    String funcName = sig.string(1);
                    
                    if(kind == NameKind.PREFIX) {
                        //case sensitive
                        if(!funcName.startsWith(name)) {
                            continue;
                        }
                    }
                    
                    int offset = sig.integer(3);
                    String arguments = sig.string(2);

                    IndexedFunction func = new IndexedFunction(funcName, null,
                            this, map.getPersistentUrl(), arguments, offset, 0, ElementKind.METHOD);
                    
                    func.setResolved(context != null && isReachable(context, map.getPersistentUrl()));
                    functions.add(func);
                    
                }
            }
        }
        return functions;
    }
    
    /** returns GLOBAL constants. */
    public Collection<IndexedConstant> getConstants(PHPParseResult context, String name, NameKind kind) {
        final Set<SearchResult> result = new HashSet<SearchResult>();
        Collection<IndexedConstant> constants = new ArrayList<IndexedConstant>();
        search(PHPIndexer.FIELD_CONST, name.toLowerCase(), NameKind.PREFIX, result, ALL_SCOPE, TERMS_CONST);

        for (SearchResult map : result) {
            if (map.getPersistentUrl() != null) {
                String[] signatures = map.getValues(PHPIndexer.FIELD_CONST);

                if (signatures == null) {
                    continue;
                }

                for (String signature : signatures) {
                    Signature sig = Signature.get(signature);
                    //sig.string(0) is the case insensitive search key
                    String constName = sig.string(1);

                    if (kind == NameKind.PREFIX) {
                        //case sensitive
                        if (!constName.startsWith(name)) {
                            continue;
                        }
                    }

                    int offset = sig.integer(2);

                    IndexedConstant constant = new IndexedConstant(constName, null,
                            this, map.getPersistentUrl(), offset, 0, null);

                    constant.setResolved(context != null && isReachable(context, map.getPersistentUrl()));
                    constants.add(constant);
                }
            }
        }
        
        return constants;
    }
    
    public Collection<IndexedClass> getClasses(PHPParseResult context, String name, NameKind kind) {
        final Set<SearchResult> result = new HashSet<SearchResult>();
        Collection<IndexedClass> classes = new ArrayList<IndexedClass>();
        search(PHPIndexer.FIELD_CLASS, name.toLowerCase(), NameKind.PREFIX, result, ALL_SCOPE, TERMS_BASE);
       
        for (SearchResult map : result) {
            if (map.getPersistentUrl() != null) {
                String[] signatures = map.getValues(PHPIndexer.FIELD_CLASS);

                if (signatures == null) {
                    continue;
                }

                for (String signature : signatures) {
                    Signature sig = Signature.get(signature);
                    String className = sig.string(1);
                    
                    if(kind == NameKind.PREFIX) {
                        //case sensitive
                        if(!className.startsWith(name)) {
                            continue;
                        }
                    } else if(kind == NameKind.EXACT_NAME) {
                        if(!className.equals(name)) {
                            continue;
                        }
                    }
                    
                    //TODO: handle search kind
                    
                    int offset = sig.integer(2);
                    String superClass = sig.string(3);

                    IndexedClass clazz = new IndexedClass(className, null,
                            this, map.getPersistentUrl(), superClass, offset, 0);
                    
                    clazz.setResolved(context != null && isReachable(context, map.getPersistentUrl()));
                    classes.add(clazz);
                }
            }
        }
        
        return classes;
    }
    
    public Collection<String>getDirectIncludes(String filePath){
        assert !filePath.startsWith("file:");
        ArrayList includes = new ArrayList();
        final Set<SearchResult> result = new HashSet<SearchResult>();
        search("filename", "file:" + filePath, NameKind.EXACT_NAME, result, ALL_SCOPE, TERMS_BASE); //NOI18N
        
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
    
    public Collection<String>getAllIncludes(String filePath){
        TreeSet<String> allIncludes = getAllIncludes(filePath, (Collection<String>)Collections.EMPTY_LIST);
        allIncludes.remove(filePath);
        return allIncludes;
    }

    private TreeSet<String>getAllIncludes(String filePath, Collection<String> alreadyProcessed){
        TreeSet<String> includes = new TreeSet<String>();
        includes.add(filePath);
        includes.addAll(alreadyProcessed);
        Collection<String> directIncludes = getDirectIncludes(filePath);
        
        for (String directInclude : directIncludes){
            if (!includes.contains(directInclude)){
                includes.addAll(getAllIncludes(directInclude, includes));
            }
        }
        
        return includes;
    }
    
    private String lastIsReachableURL = null;
    private WeakReference<PHPParseResult> lastIsReachableResultArg;
    private boolean lastIsReachableReturnValue;
    
    /** 
     * Decide whether the given url is included from the current compilation
     * context.
     * This will typically return true for all library files, and false for
     * all source level files unless that file is reachable through include-mechanisms
     * from the current file.
     */
    public boolean isReachable(PHPParseResult result, String url) {        
        // performance optimization: 
        // this function may be called thousands of times in a row with the same url
        // there is a loss of result accuracy but it is negligible
        if (lastIsReachableResultArg != null 
                && lastIsReachableResultArg.get() == result 
                && url.equals(lastIsReachableURL)){
            
            return lastIsReachableReturnValue;
        }
        
        lastIsReachableResultArg = new WeakReference<PHPParseResult>(result);
        lastIsReachableURL = url;
        lastIsReachableReturnValue = true;
        
        Project project = FileOwnerQuery.getOwner(result.getFile().getFileObject());
        
        if (project != null){
            try {
                // return true for platform files
                // TODO temporary implementation
                PhpSourcePath phpSourcePath = project.getLookup().lookup(PhpSourcePath.class);
                if (phpSourcePath != null) {
                    File file = new File(new URI(url));
                    
                    if (!file.exists()){
                        lastIsReachableReturnValue = false;
                        return false; // a workaround for #131906
                    }
                    
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
        
        Collection<String> includeList = getAllIncludes(fileURLToAbsPath(processedFileURL));
        
        if (includeList.contains(fileURLToAbsPath(url))){
            return true;
        }

        lastIsReachableReturnValue = false;
        return false;
    }
    
    private static String fileURLToAbsPath(String url){
        assert url.startsWith("file:") : url + " doesn't start with 'file:'"; //NOI18N
        return url.substring("file:".length()); //NOI18N
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
