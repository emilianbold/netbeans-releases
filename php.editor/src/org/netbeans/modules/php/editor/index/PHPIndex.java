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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.WeakHashMap;
import org.netbeans.modules.gsf.api.ElementKind;
import org.netbeans.modules.gsf.api.Index;
import org.netbeans.modules.gsf.api.Index.SearchResult;
import org.netbeans.modules.gsf.api.Index.SearchScope;
import org.netbeans.modules.gsf.api.NameKind;
import org.netbeans.modules.gsf.api.annotations.NonNull;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration.Modifier;
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
    public static final int ANY_ATTR = 0xFFFFFFFF;
    private static String clusterUrl = null;
    private static final String CLUSTER_URL = "cluster:"; // NOI18N

    static final Set<SearchScope> ALL_SCOPE = EnumSet.allOf(SearchScope.class);
    static final Set<SearchScope> SOURCE_SCOPE = EnumSet.of(SearchScope.SOURCE);
    private static final Set<String> TERMS_BASE = Collections.singleton(PHPIndexer.FIELD_BASE);
    private static final Set<String> TERMS_CONST = Collections.singleton(PHPIndexer.FIELD_CONST);
    private static final Set<String> TERMS_CLASS = Collections.singleton(PHPIndexer.FIELD_CLASS);
    private static final Set<String> TERMS_VAR = Collections.singleton(PHPIndexer.FIELD_VAR);
    private static final Set<String> TERMS_ALL = new HashSet();

    {
        TERMS_ALL.add(PHPIndexer.FIELD_BASE);
        TERMS_ALL.add(PHPIndexer.FIELD_CONST);
        TERMS_ALL.add(PHPIndexer.FIELD_CLASS);
        TERMS_ALL.add(PHPIndexer.FIELD_VAR);
    }

    private final Index index;

    /** Creates a new instance of JsIndex */
    public PHPIndex(Index index) {
        this.index = index;
    }

    public static PHPIndex get(Index index) {
        return new PHPIndex(index);
    }

    public Collection<IndexedElement> getAll(PHPParseResult context, String prefix, NameKind nameKind) {
        final Set<SearchResult> result = new HashSet<SearchResult>();
        final Collection<IndexedElement> elements = new ArrayList<IndexedElement>();
        Collection<IndexedFunction> functions = new ArrayList<IndexedFunction>();
        Collection<IndexedConstant> constants = new ArrayList<IndexedConstant>();
        Collection<IndexedClass> classes = new ArrayList<IndexedClass>();
        Collection<IndexedVariable> vars = new ArrayList<IndexedVariable>();

        search(PHPIndexer.FIELD_BASE, prefix.toLowerCase(), NameKind.PREFIX, result, ALL_SCOPE, TERMS_ALL);

        findFunctions(result, nameKind, prefix, functions);
        findConstants(result, nameKind, prefix, constants);
        findClasses(result, nameKind, prefix, classes);
        findTopVariables(result, nameKind, prefix, vars);
        elements.addAll(functions);
        elements.addAll(constants);
        elements.addAll(classes);
        elements.addAll(vars);
        return elements;
    }

    protected void findClasses(final Set<SearchResult> result, NameKind kind, String name, Collection<IndexedClass> classes) {
        for (SearchResult map : result) {
            if (map.getPersistentUrl() != null) {
                String[] signatures = map.getValues(PHPIndexer.FIELD_CLASS);
                if (signatures == null) {
                    continue;
                }
                for (String signature : signatures) {
                    Signature sig = Signature.get(signature);
                    String className = sig.string(1);
                    if (kind == NameKind.PREFIX) {
                        //case sensitive
                        if (!className.toLowerCase().startsWith(name.toLowerCase())) {
                            continue;
                        }
                    } else if (kind == NameKind.EXACT_NAME) {
                        if (!className.toLowerCase().equals(name.toLowerCase())) {
                            continue;
                        }
                    }
                    //TODO: handle search kind
                    int offset = sig.integer(2);
                    String superClass = sig.string(3);
                    superClass = superClass.length() == 0 ? null : superClass;
                    IndexedClass clazz = new IndexedClass(className, null, this, map.getPersistentUrl(), superClass, offset, 0);
                    //clazz.setResolved(context != null && isReachable(context, map.getPersistentUrl()));
                    classes.add(clazz);
                }
            }
        }
    }

    protected void findConstants(final Set<SearchResult> result, NameKind kind, String name, Collection<IndexedConstant> constants) {
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
                    IndexedConstant constant = new IndexedConstant(constName, null, this, map.getPersistentUrl(), offset, 0, null);
                    //constant.setResolved(context != null && isReachable(context, map.getPersistentUrl()));
                    constants.add(constant);
                }
            }
        }
    }

    protected void findFunctions(final Set<SearchResult> result, NameKind kind, String name, Collection<IndexedFunction> functions) {
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
                    if (kind == NameKind.PREFIX) {
                        //case sensitive - TODO does it make sense?
                        if (!funcName.startsWith(name)) {
                            continue;
                        }
                    } else if (kind == NameKind.EXACT_NAME) {
                        if (!funcName.equalsIgnoreCase(name)) {
                            // PHP func names r case-insensitive
                            continue;
                        }
                    }
                    int offset = sig.integer(3);
                    String arguments = sig.string(2);
                    IndexedFunction func = new IndexedFunction(funcName, null, this, map.getPersistentUrl(), arguments, offset, 0, ElementKind.METHOD);
                    int[] optionalArgs = extractOptionalArgs(sig.string(4));
                    func.setOptionalArgs(optionalArgs);
                    //func.setResolved(context != null && isReachable(context, map.getPersistentUrl()));
                    functions.add(func);
                    String retType = sig.string(5);
                    retType = retType.length() == 0 ? null : retType;
                    func.setReturnType(retType);
                }
            }
        }
    }

    protected void findTopVariables(final Set<SearchResult> result, NameKind kind, String name, Collection<IndexedVariable> vars) {
        for (SearchResult map : result) {
            if (map.getPersistentUrl() != null) {
                String[] signatures = map.getValues(PHPIndexer.FIELD_VAR);
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
                    } else if (kind == NameKind.EXACT_NAME) {
                        if (!constName.equals(name)) {
                            continue;
                        }
                    }
                    String typeName = sig.string(2);
                    typeName = typeName.length() == 0 ? null : typeName;
                    int offset = sig.integer(3);
                    IndexedVariable var = new IndexedVariable(constName, null, this,
                            map.getPersistentUrl(), offset, 0, typeName);
                    //var.setResolved(context != null && isReachable(context, map.getPersistentUrl()));
                    vars.add(var);
                }
            }
        }
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
    public static FileObject getFileObject(String urlStr) {
        try {
            if (urlStr.startsWith(CLUSTER_URL)) {
                urlStr = getClusterUrl() + urlStr.substring(CLUSTER_URL.length()); // NOI18N

            }

            URL url = new URL(urlStr);
            return URLMapper.findFileObject(url);
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

    /** returns constnats of a class. */
    public Collection<IndexedConstant> getAllClassConstants(PHPParseResult context, String typeName, String name, NameKind kind) {
        Map<String, IndexedConstant> constants = new TreeMap<String, IndexedConstant>();
       
        // #147730 - prefer the current file
        File currentFile = getCurrentFile(context);
        Set<String> currentFileClasses = new HashSet<String>();

        for (String className : getClassAncestors(context, typeName)) {
            //int mask = inheritanceLine.get(0) == clazz ? attrMask : (attrMask & (~Modifier.PRIVATE));
            for (IndexedConstant const0 : getClassConstants(context, className, name, kind)) {
                String constantName = const0.getName();
                if (!constants.containsKey(constantName) || className.equals(typeName)){
                    constants.put(constantName, const0);
                    
                    if (currentFile != null && currentFile.equals(const0.getFile().getFile())) {
                        currentFileClasses.add(className);
                    }
                }
            }
        }

        Collection<IndexedInterface> interfaceTree = getInterfaceTree(context, typeName);

        if (interfaceTree != null){
            for (IndexedInterface iface : interfaceTree){
                for (IndexedConstant constant : getClassConstants(context, iface.getName(), name, kind)){
                    String constantName = constant.getName();
                    
                    if (!constants.containsKey(constantName) || iface.getName().equals(typeName)){
                        constants.put( constantName,constant);
                    }
                }
            }
        }

        Collection<IndexedConstant> result = constants.values();
        filterClassMembers(result, currentFileClasses, currentFile);
        return result;
    }

    /** returns all methods of a class or an interface. */
    public Collection<IndexedFunction> getAllMethods(PHPParseResult context, String typeName, String name, NameKind kind, int attrMask) {
        Map<String, IndexedFunction> methods = new TreeMap<String, IndexedFunction>();
        
        // #147730 - prefer the current file
        File currentFile = getCurrentFile(context);
        Set<String> currentFileClasses = new HashSet<String>();

        for (String className : getClassAncestors(context, typeName)) {
            int mask = className.equals(typeName) ? attrMask : (attrMask & (~Modifier.PRIVATE));
            
            for (IndexedFunction method : getMethods(context, className, name, kind, mask)){
                String methodName = method.getName();
                
                if (!methods.containsKey(methodName) || className.equals(typeName)){
                    methods.put(methodName, method);
                    if (currentFile != null && currentFile.equals(method.getFile().getFile())) {
                        currentFileClasses.add(className);
                    }
                }
            }
        }
        
        Collection<IndexedInterface> interfaceTree = getInterfaceTree(context, typeName);

        if (interfaceTree != null){
            for (IndexedInterface iface : interfaceTree){
                String ifaceName = iface.getName();

                for (IndexedFunction method : getMethods(context, ifaceName, name, kind, attrMask)) {
                    String methodName = method.getName();

                    if (!methods.containsKey(methodName) || ifaceName.equals(typeName)) {
                        methods.put(methodName, method);
                    }
                }
            }
        }

        Collection<IndexedFunction> result = methods.values();
        filterClassMembers(result, currentFileClasses, currentFile);
        return result;
    }

    /** returns all fields of a class or an interface. */
    public Collection<IndexedConstant> getAllFields(PHPParseResult context, String typeName, String name, NameKind kind, int attrMask) {
        Map<String, IndexedConstant> fields = new TreeMap<String, IndexedConstant>();
        
        // #147730 - prefer the current file
        File currentFile = getCurrentFile(context);
        Set<String> currentFileClasses = new HashSet<String>();

        for (String className : getClassAncestors(context, typeName)) {
            int mask = className.equals(typeName) ? attrMask : (attrMask & (~Modifier.PRIVATE));
            for (IndexedConstant field : getFields(context, className, name, kind, mask)) {
                String fieldName = field.getName();
                
                if (!fields.containsKey(fieldName) || className.equals(typeName)){
                    fields.put(fieldName, field);
                }
                
                if (currentFile != null && field != null && currentFile.equals(field.getFile().getFile())) {
                    currentFileClasses.add(className);
                }
            }
        }

        Collection<IndexedConstant> result = fields.values();
        filterClassMembers(result, currentFileClasses, currentFile);
        return result;
    }

    /** Current file for the context or <code>null</code> */
    private File getCurrentFile(PHPParseResult context) {
        if (context != null && context.getFile() != null) {
            return context.getFile().getFile();
        }
        return null;
    }

    // #147730 - prefer the current file
    private void filterClassMembers(Collection<? extends IndexedElement> elements, Set<String> currentFileClasses, File currentFile) {
        if (elements.size() > 0 && currentFileClasses.size() > 0) {
            for (Iterator<? extends IndexedElement> it = elements.iterator(); it.hasNext();) {
                IndexedElement method = it.next();
                if (currentFileClasses.contains(method.getIn())
                        && !currentFile.equals(method.getFile().getFile())) {
                    it.remove();
                }
            }
        }
    }

    /** return a list of all superclasses of the given class.
     *  The head item will be the queried class, otherwise it not safe to rely on the element order
     */
    @NonNull
    public Collection<String>getClassAncestors(PHPParseResult context, String className){
        return getClassAncestors(context, className, new TreeSet<String>());
    }

    @NonNull
    private Collection<String>getClassAncestors(PHPParseResult context, String className, Collection<String> processedClasses){
        Collection<String> ancestors = new TreeSet<String>();

        if (processedClasses.contains(className)) {
            return Collections.<String>emptyList(); //TODO: circular reference, warn the user
        }

        processedClasses.add(className);
        List<String> assumedParents = new LinkedList<String>();
        Collection<IndexedClass>classes = getClasses(context, className, NameKind.EXACT_NAME);
        
        if (classes != null) {
            for (IndexedClass clazz : classes) {
                ancestors.add(clazz.getName());
                String parent = clazz.getSuperClass();

                if (parent != null) {
                    assumedParents.add(parent);
                }
            }
        }

        for (String parent : assumedParents){
            ancestors.addAll(getClassAncestors(context, parent, processedClasses));
        }

        return ancestors;
    }

    /** returns local constnats of a class. */
    public Collection<IndexedConstant> getClassConstants(PHPParseResult context, String typeName, String name, NameKind kind) {
        Collection<IndexedConstant> constants = new ArrayList<IndexedConstant>();
        Map<String, String> signaturesMap = getTypeSpecificSignatures(typeName, PHPIndexer.FIELD_CLASS_CONST, name, kind, ALL_SCOPE);

        for (String signature : signaturesMap.keySet()) {
            //items are not indexed, no case insensitive search key user
            Signature sig = Signature.get(signature);
            String propName = sig.string(0);
            int offset = sig.integer(1);

            IndexedConstant prop = new IndexedConstant(propName, typeName,
                    this, signaturesMap.get(signature), offset, 0, null);

            constants.add(prop);

        }

        return constants;
    }

    /** returns methods of a class. */
    public Collection<IndexedFunction> getMethods(PHPParseResult context, String typeName, String name, NameKind kind, int attrMask) {
        Collection<IndexedFunction> methods = new ArrayList<IndexedFunction>();
        Map<String, String> signaturesMap = getTypeSpecificSignatures(typeName, PHPIndexer.FIELD_METHOD, name, kind, ALL_SCOPE);

        for (String signature : signaturesMap.keySet()) {
            //items are not indexed, no case insensitive search key user
            Signature sig = Signature.get(signature);
            int flags = sig.integer(5);

            if ((flags & (Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE)) == 0){
                flags |= Modifier.PUBLIC; // default modifier
            }

            if ((flags & attrMask) != 0) {
                String funcName = sig.string(0);
                String args = sig.string(1);
                int offset = sig.integer(2);

                IndexedFunction func = new IndexedFunction(funcName, typeName,
                        this, signaturesMap.get(signature), args, offset, flags, ElementKind.METHOD);

                int optionalArgs[] = extractOptionalArgs(sig.string(3));
                func.setOptionalArgs(optionalArgs);
                String retType = sig.string(4);
                retType = retType.length() == 0 ? null : retType;
                func.setReturnType(retType);
                methods.add(func);
            }

        }

        return methods;
    }

    /** returns fields of a class. */
    public Collection<IndexedConstant> getFields(PHPParseResult context, String typeName, String name, NameKind kind, int attrMask) {
        Collection<IndexedConstant> fields = new ArrayList<IndexedConstant>();
        Map<String, String> signaturesMap = getTypeSpecificSignatures(typeName, PHPIndexer.FIELD_FIELD, name, kind, ALL_SCOPE);

        for (String signature : signaturesMap.keySet()) {
            Signature sig = Signature.get(signature);
            int flags = sig.integer(2);

            if ((flags & (Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE)) == 0){
                flags |= Modifier.PUBLIC; // default modifier
            }

            if ((flags & attrMask) != 0) {
                String propName = "$" + sig.string(0);
                int offset = sig.integer(1);
                String type = sig.string(3);

                if (type.length() == 0){
                    type = null;
                }

                IndexedConstant prop = new IndexedConstant(propName, typeName,
                        this, signaturesMap.get(signature), offset, flags, type,ElementKind.FIELD);

                fields.add(prop);
            }
        }

        return fields;
    }

    private Map<String, String> getTypeSpecificSignatures(String typeName, String fieldName, String name, NameKind kind, Set<SearchScope> scope) {
        final Set<SearchResult> searchResult = new HashSet<SearchResult>();
        Map<String, String> signatures = new HashMap<String, String>();
        for (String indexField : new String[]{PHPIndexer.FIELD_CLASS, PHPIndexer.FIELD_IFACE}) {
            search(indexField, typeName.toLowerCase(), NameKind.PREFIX, searchResult, scope, TERMS_BASE);

            for (SearchResult typeMap : searchResult) {
                String[] typeSignatures = typeMap.getValues(indexField);
                String[] rawSignatures = typeMap.getValues(fieldName);

                if (typeSignatures == null || rawSignatures == null) {
                    continue;
                }

                assert typeSignatures.length == 1;
                String foundTypeName = getSignatureItem(typeSignatures[0], 1);
                foundTypeName = (foundTypeName != null) ? foundTypeName.toLowerCase() : null;
                String persistentURL = typeMap.getPersistentUrl();

                if (!typeName.toLowerCase().equals(foundTypeName)) {
                    continue;
                }

                for (String signature : rawSignatures) {
                    String elemName = getSignatureItem(signature, 0);

                    // TODO: now doing IC prefix search only, handle other search types
                    // according to 'kind'
                    if ((kind == NameKind.CASE_INSENSITIVE_PREFIX && elemName.toLowerCase().startsWith(name.toLowerCase())) || (kind == NameKind.PREFIX && elemName.startsWith(name)) || (kind == NameKind.EXACT_NAME && elemName.equals(name))) {
                        signatures.put(signature, persistentURL);
                    }

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

    public Collection<IndexedFunction> getFunctions(PHPParseResult context, String name, NameKind kind) {
        return getFunctions(context, name, kind, ALL_SCOPE);
    }
    /** returns GLOBAL functions. */
    public Collection<IndexedFunction> getFunctions(PHPParseResult context, String name, NameKind kind, Set<SearchScope> scope) {
        final Set<SearchResult> result = new HashSet<SearchResult>();
        Collection<IndexedFunction> functions = new ArrayList<IndexedFunction>();
        search(PHPIndexer.FIELD_BASE, name.toLowerCase(), NameKind.PREFIX, result, scope, TERMS_BASE);
        findFunctions(result, kind, name, functions);
        return functions;
    }
    public Collection<IndexedVariable> getTopLevelVariables(PHPParseResult context, String name, NameKind kind) {
        return getTopLevelVariables(context, name, kind, ALL_SCOPE);
    }
    public Collection<IndexedVariable> getTopLevelVariables(PHPParseResult context, String name, NameKind kind, Set<SearchScope> scope) {
        final Set<SearchResult> result = new HashSet<SearchResult>();
        Collection<IndexedVariable> vars = new ArrayList<IndexedVariable>();
        search(PHPIndexer.FIELD_VAR, name.toLowerCase(), NameKind.PREFIX, result, scope, TERMS_VAR);
        findTopVariables(result, kind, name, vars);
        return vars;
    }


    public Collection<IndexedConstant> getConstants(PHPParseResult context, String name, NameKind kind) {
        return getConstants(context, name, kind, ALL_SCOPE);
    }
    /** returns GLOBAL constants. */
    public Collection<IndexedConstant> getConstants(PHPParseResult context, String name, NameKind kind, Set<SearchScope> scope) {
        final Set<SearchResult> result = new HashSet<SearchResult>();
        Collection<IndexedConstant> constants = new ArrayList<IndexedConstant>();
        search(PHPIndexer.FIELD_CONST, name.toLowerCase(), NameKind.PREFIX, result, scope, TERMS_CONST);
        findConstants(result, kind, name, constants);
        return constants;
    }

    public Set<FileObject> filesWithIdentifiers(String identifierName) {
        final Set<FileObject> result = new HashSet<FileObject>();
        final Set<SearchResult> idSearchResult = new HashSet<SearchResult>();
        search(PHPIndexer.FIELD_IDENTIFIER, identifierName.toLowerCase(), NameKind.PREFIX, idSearchResult, ALL_SCOPE, TERMS_BASE);
        for (SearchResult searchResult : idSearchResult) {
            result.add(FileUtil.toFileObject(new File(URI.create(searchResult.getPersistentUrl()))));
        }
        return result;
    }

    public Set<String> typeNamesForIdentifier(String identifierName, ElementKind kind,NameKind nameKind) {
        return typeNamesForIdentifier(identifierName, kind, nameKind, ALL_SCOPE);
    }

    public Set<String> typeNamesForIdentifier(String identifierName, ElementKind kind,NameKind nameKind, Set<SearchScope> scope) {
        final Set<String> result = new HashSet<String>();
        final Set<SearchResult> idSearchResult = new HashSet<SearchResult>();
        search(PHPIndexer.FIELD_IDENTIFIER_DECLARATION, identifierName.toLowerCase(), NameKind.PREFIX, idSearchResult, scope, TERMS_BASE);
        for (SearchResult searchResult : idSearchResult) {
            if (searchResult.getPersistentUrl() != null) {
                String[] signatures = searchResult.getValues(PHPIndexer.FIELD_IDENTIFIER_DECLARATION);
                if (signatures == null) {
                    continue;
                }
                for (String sign : signatures) {
                    IdentifierSignature idSign = IdentifierSignature.createDeclaration(Signature.get(sign));
                    if ((!idSign.isClassMember() && !idSign.isIfaceMember()) ||
                            idSign.getTypeName() == null) {
                        continue;
                    }
                    switch(nameKind) {
                        case CASE_INSENSITIVE_PREFIX:
                            if (!idSign.getName().startsWith(identifierName.toLowerCase())) {
                                continue;
                            }
                            break;
                        case PREFIX:
                            if (!idSign.getName().startsWith(identifierName)) {
                                continue;
                            }
                            break;
                        default:
                            assert false : nameKind.toString();
                            continue;
                    }
                    if (kind == null) {
                        result.add(idSign.getTypeName());
                    } else if (kind.equals(ElementKind.FIELD) && idSign.isField()) {
                        result.add(idSign.getTypeName());
                    } else if (kind.equals(ElementKind.METHOD) && idSign.isMethod()) {
                        result.add(idSign.getTypeName());
                    } else if (kind.equals(ElementKind.CONSTANT) && idSign.isClassConstant()) {
                        result.add(idSign.getTypeName());
                    }
                }
            }
        }
        return result;
    }


    public Collection<IndexedClass> getClasses(PHPParseResult context, String name, NameKind kind) {
        return getClasses(context, name, kind, ALL_SCOPE);
    }

    public Collection<IndexedClass> getClasses(PHPParseResult context, String name, NameKind kind, Set<SearchScope> scope) {
        final Set<SearchResult> result = new HashSet<SearchResult>();
        Collection<IndexedClass> classes = new ArrayList<IndexedClass>();
        search(PHPIndexer.FIELD_CLASS, name.toLowerCase(), NameKind.PREFIX, result, scope, TERMS_CLASS);
        findClasses(result, kind, name, classes);

        return classes;
    }

    public Collection<IndexedInterface> getInterfaces(PHPParseResult context, String name, NameKind kind) {
        return getInterfaces(context, name, kind, ALL_SCOPE);
    }

    public Collection<IndexedInterface> getInterfaces(PHPParseResult context, String name, NameKind kind, Set<SearchScope> scope) {
        final Set<SearchResult> result = new HashSet<SearchResult>();
        Collection<IndexedInterface> ifaces = new ArrayList<IndexedInterface>();
        if (name != null && name.trim().length() > 0) {
            search(PHPIndexer.FIELD_IFACE, name.toLowerCase(), NameKind.PREFIX, result, scope, TERMS_BASE);
        } else {
            search(PHPIndexer.FIELD_IFACE, name.toLowerCase(), NameKind.PREFIX, result, scope, null);
        }

        for (SearchResult map : result) {
            if (map.getPersistentUrl() != null) {
                String[] signatures = map.getValues(PHPIndexer.FIELD_IFACE);

                if (signatures == null) {
                    continue;
                }

                for (String signature : signatures) {
                    Signature sig = Signature.get(signature);
                    String ifaceName = sig.string(1);

                    if(kind == NameKind.PREFIX) {
                        //case sensitive
                        if(!ifaceName.toLowerCase().startsWith(name.toLowerCase())) {
                            continue;
                        }
                    } else if(kind == NameKind.EXACT_NAME) {
                        if(!ifaceName.toLowerCase().equals(name.toLowerCase())) {
                            continue;
                        }
                    }

                    //TODO: handle search kind

                    int offset = sig.integer(2);
                    String interfaces[] = sig.string(3).split(","); //NOI18N

                    IndexedInterface iface = new IndexedInterface(ifaceName, null,
                            this, map.getPersistentUrl(), interfaces, offset, 0);

                    //iface.setResolved(context != null && isReachable(context, map.getPersistentUrl()));
                    ifaces.add(iface);
                }
            }
        }

        return ifaces;
    }

    private Collection<IndexedInterface> getInterfaceTree(PHPParseResult context, String ifaceName) {
        Collection<IndexedInterface> ifacesByName = new ArrayList<IndexedInterface>();
        Collection<String> alreadyProcessed = new ArrayList<String>();
        Collection<String> unprocessedIfaces = new TreeSet<String>(Collections.singleton(ifaceName));

        while (!unprocessedIfaces.isEmpty()){
            Collection<String> newInterfaces = new TreeSet<String>();

            for (String rawIface: unprocessedIfaces) {
                for (IndexedInterface iface : getInterfaces(context, rawIface, NameKind.EXACT_NAME)){
                    ifacesByName.add(iface);
                    alreadyProcessed.add(iface.getName());

                    for (String inheritedIfaceName : iface.getInterfaces()){
                        if (!alreadyProcessed.contains(inheritedIfaceName)){
                            newInterfaces.add(inheritedIfaceName);
                        }
                    }
                }
            }

            unprocessedIfaces.clear();
            unprocessedIfaces.addAll(newInterfaces);
        }

        return ifacesByName;
    }

    public Collection<String>getDirectIncludes(PHPParseResult context, String filePath){
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

    private WeakHashMap<PHPParseResult, HashMap<String, Collection<String>>> includesCache =
            new WeakHashMap<PHPParseResult, HashMap<String, Collection<String>>>();

    public Collection<String> getAllIncludes(PHPParseResult context, String filePath){
        return getAllIncludes(context, filePath, new TreeSet<String>());
    }

    private Collection<String> getAllIncludes(PHPParseResult context, String filePath, Collection<String> alreadyProcessed){
       // try to fetch cached result first
        HashMap<String, Collection<String>> resultTable = includesCache.get(context);

        if (resultTable != null) {
            Collection<String> cachedResult = resultTable.get(filePath);

            if (cachedResult != null) {
                return cachedResult;
            }
        } else {
            resultTable = new HashMap<String, Collection<String>>();
            includesCache.put(context, resultTable);
        }

        Collection<String> includes = getAllIncludesImpl(context, filePath, alreadyProcessed);
        resultTable.put(filePath, includes);
        return includes;
    }


    private Collection<String>getAllIncludesImpl(PHPParseResult context, String filePath, Collection<String> alreadyProcessed){
        Collection<String> includes = new TreeSet<String>();
        Collection<String> directIncludes = getDirectIncludes(context, filePath);

        for (String directInclude : directIncludes){
            if (!alreadyProcessed.contains(directInclude)) {
                alreadyProcessed.add(directInclude);
                includes.add(directInclude);
                includes.addAll(getAllIncludes(context, directInclude, alreadyProcessed));
            }
        }

        return Collections.unmodifiableCollection(includes);
    }

    private WeakHashMap<PHPParseResult, HashMap<String, Boolean>> isReachableCache =
            new WeakHashMap<PHPParseResult, HashMap<String, Boolean>>();

    /**
     * Decide whether the given url is included from the current compilation
     * context.
     * This will typically return true for all library files, and false for
     * all source level files unless that file is reachable through include-mechanisms
     * from the current file.
     */
    public boolean isReachable(PHPParseResult result, String url) {
        // try to fetch cached result first
        HashMap<String, Boolean> resultTable = isReachableCache.get(result);

        if (resultTable != null) {
            Boolean cachedResult = resultTable.get(url);

            if (cachedResult != null) {
                return cachedResult.booleanValue();
            }
        } else {
            resultTable = new HashMap<String, Boolean>();
            isReachableCache.put(result, resultTable);
        }

        boolean reachable = isReachableImpl(result, url);
        resultTable.put(url, new Boolean(reachable));
        return reachable;
    }

    private  boolean isReachableImpl(PHPParseResult result, String url) {
        if (isSystemFile(result, url)){
            return true;
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

        Collection<String> includeList = getAllIncludes(result, fileURLToAbsPath(processedFileURL));

        if (includeList.contains(fileURLToAbsPath(url))){
            return true;
        }

        return false;
    }

    private WeakHashMap<PHPParseResult, HashMap<String, Boolean>> isSystemFileCache =
            new WeakHashMap<PHPParseResult, HashMap<String, Boolean>>();

    private boolean isSystemFile(PHPParseResult result, String url){
                // try to fetch cached result first
        HashMap<String, Boolean> resultTable = isSystemFileCache.get(result);

        if (resultTable != null) {
            Boolean cachedResult = resultTable.get(url);

            if (cachedResult != null) {
                return cachedResult.booleanValue();
            }
        } else {
            resultTable = new HashMap<String, Boolean>();
            isSystemFileCache.put(result, resultTable);
        }

        boolean systemFile = isSystemFileImpl(result, url);
        resultTable.put(url, new Boolean(systemFile));
        return systemFile;
    }

    private boolean isSystemFileImpl(PHPParseResult result, String url){
        try {
            // return true for platform files
            // TODO temporary implementation
            File file = new File(new URI(url));

            if (!file.exists()){
                return false; // a workaround for #131906
            }

            FileObject fileObject = FileUtil.toFileObject(file);
            PhpSourcePath.FileType fileType = PhpSourcePath.getFileType(fileObject);
            if (fileType == PhpSourcePath.FileType.INTERNAL
                    || fileType == PhpSourcePath.FileType.INCLUDE) {
                return true;
            }
        } catch (URISyntaxException ex) {
            Exceptions.printStackTrace(ex);
        }

        return false;
    }

    private static String fileURLToAbsPath(String url){
        assert url.startsWith("file:") : url + " doesn't start with 'file:'"; //NOI18N
        return url.substring("file:".length()); //NOI18N
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

    private int[] extractOptionalArgs(String optionalParamsStr) {
        if (optionalParamsStr.length() == 0){
            return new int[0];
        }

        String optionalParamsStrParts[] = optionalParamsStr.split(",");
        int optionalArgs[] = new int[optionalParamsStrParts.length];

        for (int i = 0; i < optionalParamsStrParts.length; i++) {
            try{
            optionalArgs[i] = Integer.parseInt(optionalParamsStrParts[i]);
            } catch (NumberFormatException e){
                System.err.println(String.format("*** couldnt parse '%s', part %d", optionalParamsStr, i));
            }
        }

        return optionalArgs;
    }
}
