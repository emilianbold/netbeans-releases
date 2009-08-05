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

package org.netbeans.api.java.source;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;

import javax.lang.model.element.*;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.ElementFilter;

import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.comp.Check;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Context;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.api.java.queries.JavadocForBinaryQuery.Result;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.java.JavaDataLoader;
import org.netbeans.modules.java.source.indexing.JavaCustomIndexer;
import org.netbeans.modules.java.source.indexing.JavaIndex;
import org.netbeans.modules.java.source.parsing.ClasspathInfoProvider;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.java.source.usages.ClassIndexImpl;
import org.netbeans.modules.java.source.usages.ClassIndexManager;
import org.netbeans.modules.java.source.usages.ClasspathInfoAccessor;
import org.netbeans.modules.java.source.usages.ExecutableFilesIndex;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.impl.indexing.friendapi.IndexingController;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;
import org.openide.util.Utilities;

/**
 *
 * @author Dusan Balek
 */
public class SourceUtils {    
     
    private static final String PACKAGE_SUMMARY = "package-summary";   //NOI18N
    
    private SourceUtils() {}
    
    /**
     * @since 0.21
     */
    public static TokenSequence<JavaTokenId> getJavaTokenSequence(final TokenHierarchy hierarchy, final int offset) {
        if (hierarchy != null) {
            TokenSequence<?> ts = hierarchy.tokenSequence();
            while(ts != null && (offset == 0 || ts.moveNext())) {
                ts.move(offset);
                if (ts.language() == JavaTokenId.language())
                    return (TokenSequence<JavaTokenId>)ts;
                if (!ts.moveNext() && !ts.movePrevious())
                    return null;
                ts = ts.embedded();
            }
        }
        return null;
    }
    
    public static boolean checkTypesAssignable(CompilationInfo info, TypeMirror from, TypeMirror to) {
        Context c = ((JavacTaskImpl) info.impl.getJavacTask()).getContext();
        if (from.getKind() == TypeKind.DECLARED) {
            com.sun.tools.javac.util.List<Type> typeVars = com.sun.tools.javac.util.List.nil();
            for (TypeMirror tm : ((DeclaredType)from).getTypeArguments()) {
                if (tm.getKind() == TypeKind.TYPEVAR)
                    typeVars = typeVars.append((Type)tm);
            }
            if (!typeVars.isEmpty())
                from = new Type.ForAll(typeVars, (Type)from);
        } else if (from.getKind() == TypeKind.WILDCARD) {
            from = Types.instance(c).upperBound((Type)from);
        }
        return Check.instance(c).checkType(null, (Type)from, (Type)to).getKind() != TypeKind.ERROR;
    }
    
    public static TypeMirror getBound(WildcardType wildcardType) {
        Type.TypeVar bound = ((Type.WildcardType)wildcardType).bound;
        return bound != null ? bound.bound : null;
    }
    
    /**
     * Returns the type element within which this member or constructor
     * is declared. Does not accept packages
     * If this is the declaration of a top-level type (a non-nested class
     * or interface), returns null.
     *
     * @return the type declaration within which this member or constructor
     * is declared, or null if there is none
     * @throws IllegalArgumentException if the provided element is a package element
     * @deprecated use {@link ElementUtilities#enclosingTypeElement(javax.lang.model.element.Element)}
     */
    public static @Deprecated TypeElement getEnclosingTypeElement( Element element ) throws IllegalArgumentException {
        return ElementUtilities.enclosingTypeElementImpl(element);
    }
    
    public static TypeElement getOutermostEnclosingTypeElement( Element element ) {
	
	Element ec =  getEnclosingTypeElement( element );
	if (ec == null) {
	    ec = element;
	}
	
	while( ec.getEnclosingElement().getKind().isClass() || 
	       ec.getEnclosingElement().getKind().isInterface() ) {
	
	    ec = ec.getEnclosingElement();
	}
		
	return (TypeElement)ec;
    }
    
    /**Resolve full qualified name in the given context. Adds import statement as necessary.
     * Returns name that resolved to a given FQN in given context (either simple name
     * or full qualified name). Handles import conflicts.
     * 
     * <br><b>Note:</b> if the <code>info</code> passed to this method is not an instance of {@link WorkingCopy},
     * missing import statement is added from a separate modification task executed asynchronously.
     * <br><b>Note:</b> after calling this method, it is not permitted to rewrite copy.getCompilationUnit().
     * 
     * @param info CompilationInfo over which the method should work
     * @param context in which the fully qualified should be resolved
     * @param fqn the fully qualified name to resolve
     * @return either a simple name or a FQN that will resolve to given fqn in given context
     */
    public static String resolveImport(final CompilationInfo info, final TreePath context, final String fqn) throws NullPointerException, IOException {
        if (info == null)
            throw new NullPointerException();
        if (context == null)
            throw new NullPointerException();
        if (fqn == null)
            throw new NullPointerException();
        
        CompilationUnitTree cut = info.getCompilationUnit();
        final Trees trees = info.getTrees();
        final Scope scope = trees.getScope(context);
        String qName = fqn;
        StringBuilder sqName = new StringBuilder();
        String sName = null;
        boolean clashing = false;
        ElementUtilities eu = info.getElementUtilities();
        ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
            public boolean accept(Element e, TypeMirror type) {
                return (e.getKind().isClass() || e.getKind().isInterface()) && trees.isAccessible(scope, (TypeElement)e);
            }
        };
        while(qName != null && qName.length() > 0) {
            int lastDot = qName.lastIndexOf('.');
            String simple = qName.substring(lastDot < 0 ? 0 : lastDot + 1);
            if (sName == null)
                sName = simple;
            else
                sqName.insert(0, '.');
            sqName.insert(0, simple);
            if (info.getElements().getTypeElement(qName) != null) {
                boolean matchFound = false;
                for(Element e : eu.getLocalMembersAndVars(scope, acceptor)) {
                    if (simple.contentEquals(e.getSimpleName())) {
                        //either a clash or already imported:
                        if (qName.contentEquals(((TypeElement)e).getQualifiedName())) {
                            return sqName.toString();
                        } else if (fqn == qName) {
                            clashing = true;
                        }
                        matchFound = true;
                        break;
                    }
                }
                if (!matchFound) {
                    for(TypeElement e : eu.getGlobalTypes(acceptor)) {
                        if (simple.contentEquals(e.getSimpleName())) {
                            //either a clash or already imported:
                            if (qName.contentEquals(e.getQualifiedName())) {
                                return sqName.toString();
                            } else if (fqn == qName) {
                                clashing = true;
                            }
                            break;
                        }
                    }
                }
            }
            qName = lastDot < 0 ? null : qName.substring(0, lastDot);
        }
        if (clashing)
            return fqn;
        
        //not imported/visible so far by any means:
        if (info instanceof WorkingCopy) {
            CompilationUnitTree nue = (CompilationUnitTree) ((WorkingCopy)info).getChangeSet().get(cut);
            cut = nue != null ? nue : cut;
            ((WorkingCopy)info).rewrite(info.getCompilationUnit(), addImports(cut, Collections.singletonList(fqn), ((WorkingCopy)info).getTreeMaker()));
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        ModificationResult.runModificationTask(Collections.singletonList(info.getSnapshot().getSource()), new UserTask() {
                            @Override
                            public void run(ResultIterator resultIterator) throws Exception {
                                WorkingCopy copy = WorkingCopy.get(resultIterator.getParserResult());
                                copy.toPhase(Phase.ELEMENTS_RESOLVED);
                                copy.rewrite(copy.getCompilationUnit(), addImports(copy.getCompilationUnit(), Collections.singletonList(fqn), copy.getTreeMaker()));                                
                            }
                        }).commit();
                    } catch (Exception e) {
                        Exceptions.printStackTrace(e);
                    }
                }
            });
        }
        TypeElement te = info.getElements().getTypeElement(fqn);
        if (te != null) {
            JCCompilationUnit unit = (JCCompilationUnit) info.getCompilationUnit();
            unit.namedImportScope = unit.namedImportScope.dupUnshared();
            unit.namedImportScope.enterIfAbsent((Symbol) te);
        }        
        return sName;
    }
    
    /**
     *
     *
     */
    private static CompilationUnitTree addImports(CompilationUnitTree cut, List<String> toImport, TreeMaker make)
        throws IOException {
        // do not modify the list given by the caller (may be reused or immutable).
        toImport = new ArrayList<String>(toImport); 
        Collections.sort(toImport);

        List<ImportTree> imports = new ArrayList<ImportTree>(cut.getImports());
        int currentToImport = toImport.size() - 1;
        int currentExisting = imports.size() - 1;
        
        while (currentToImport >= 0 && currentExisting >= 0) {
            String currentToImportText = toImport.get(currentToImport);
            
            while (currentExisting >= 0 && (imports.get(currentExisting).isStatic() || imports.get(currentExisting).getQualifiedIdentifier().toString().compareTo(currentToImportText) > 0))
                currentExisting--;
            
            if (currentExisting >= 0) {
                imports.add(currentExisting+1, make.Import(make.Identifier(currentToImportText), false));
                currentToImport--;
            }
        }
        // we are at the head of import section and we still have some imports
        // to add, put them to the very beginning
        while (currentToImport >= 0) {
            String importText = toImport.get(currentToImport);
            imports.add(0, make.Import(make.Identifier(importText), false));
            currentToImport--;
        }
        // return a copy of the unit with changed imports section
        return make.CompilationUnit(cut.getPackageName(), imports, cut.getTypeDecls(), cut.getSourceFile());
    }

    /**
     * Returns a {@link FileObject} in which the Element is defined.
     * @param element for which the {@link FileObject} should be located
     * @param cpInfo the classpaths context
     * @return the defining {@link FileObject} or null if it cannot be
     * found
     * 
     * @deprecated use {@link getFile(ElementHandle, ClasspathInfo)}
     */
    public static FileObject getFile (Element element, final ClasspathInfo cpInfo) {
        Parameters.notNull("element", element); //NOI18N
        Parameters.notNull("cpInfo", cpInfo);   //NOI18N
        
        Element prev = element.getKind() == ElementKind.PACKAGE ? element : null;
        while (element.getKind() != ElementKind.PACKAGE) {
            prev = element;
            element = element.getEnclosingElement();
        }
        final ElementKind kind = prev.getKind();
        if (prev == null || !(kind.isClass() || kind.isInterface() || kind == ElementKind.PACKAGE)) {
            return null;
        }        
        final ElementHandle<? extends Element> handle = ElementHandle.create(prev);
        return getFile (handle, cpInfo);
    }
    
    /**
     * Returns a {@link FileObject} of the source file in which the handle is declared.
     * @param handle to find the {@link FileObject} for
     * @param cpInfo classpaths for resolving handle
     * @return {@link FileObject} or null when the source file cannot be found
     */
    public static FileObject getFile (final ElementHandle<? extends Element> handle, final ClasspathInfo cpInfo) {
        Parameters.notNull("handle", handle);
        Parameters.notNull("cpInfo", cpInfo);        
        try {
            boolean pkg = handle.getKind() == ElementKind.PACKAGE;
            String[] signature = handle.getSignature();
            assert signature.length >= 1;
            ClassPath cp = ClassPathSupport.createProxyClassPath(
                new ClassPath[] {
                    cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE),
                    createClassPath(cpInfo,ClasspathInfo.PathKind.BOOT),                    
                    createClassPath(cpInfo,ClasspathInfo.PathKind.COMPILE),
                });
           String pkgName, className = null;
            if (pkg) {
                pkgName = FileObjects.convertPackage2Folder(signature[0]);
            }
            else {
                int index = signature[0].lastIndexOf('.');                          //NOI18N
                if (index<0) {
                    pkgName = "";                                             //NOI18N
                    className = signature[0];
                }
                else {
                    pkgName = FileObjects.convertPackage2Folder(signature[0].substring(0,index));
                    className = signature[0].substring(index+1);
                }
            }
            List<FileObject> fos = cp.findAllResources(pkgName);
            for (FileObject fo : fos) {
                FileObject root = cp.findOwnerRoot(fo);
                assert root != null;
                FileObject[] sourceRoots = SourceForBinaryQuery.findSourceRoots(root.getURL()).getRoots();                        
                ClassPath sourcePath = ClassPathSupport.createClassPath(sourceRoots);
                LinkedList<FileObject> folders = new LinkedList<FileObject>(sourcePath.findAllResources(pkgName));
                if (pkg) {
                    return folders.isEmpty() ? fo : folders.get(0);
                }
                else {               
                    boolean caseSensitive = isCaseSensitive ();
                    String sourceFileName = getSourceFileName (className);
                    folders.addFirst(fo);
                    for (FileObject folder : folders) {
                        FileObject[] children = folder.getChildren();
                        for (FileObject child : children) {
                            if (((caseSensitive && child.getName().equals (sourceFileName)) ||
                                (!caseSensitive && child.getName().equalsIgnoreCase (sourceFileName))) &&
                                (child.isData() && JavaDataLoader.JAVA_EXTENSION.equalsIgnoreCase(child.getExt()))) {
                                return child;
                            }
                        }
                    }
                    FileObject foundFo;
                    if (sourceRoots.length == 0) {
                        foundFo = findSource (signature[0],root);
                    }
                    else {
                        foundFo = findSource (signature[0],sourceRoots);
                    }
                    if (foundFo != null) {
                        return foundFo;
                    }
                }
            }
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
        return null;        
    }
    
    private static FileObject findSource (final String binaryName, final FileObject... fos) throws IOException {
        final ClassIndexManager cim = ClassIndexManager.getDefault();
        try {
            return cim.readLock(new ClassIndexManager.ExceptionAction<FileObject>() {

                public FileObject run() throws IOException, InterruptedException {
                    for (FileObject fo : fos) {
                        ClassIndexImpl ci = cim.getUsagesQuery(fo.getURL());
                        if (ci != null) {
                            String sourceName = ci.getSourceName(binaryName);
                            if (sourceName != null) {
                                FileObject result = fo.getFileObject(sourceName);
                                if (result != null) {
                                    return result;
                                }
                            }
                        }
                    }
                    return null;
                }
            });
        } catch (InterruptedException e) {
            //Never thrown
            Exceptions.printStackTrace(e);
            return null;
        }
    }
    
    /**
     * Finds {@link URL} of a javadoc page for given element when available. This method 
     * uses {@link JavadocForBinaryQuery} to find the javadoc page for the give element.
     * For {@link PackageElement} it returns the package-summary.html for given package.
     * @param element to find the Javadoc for
     * @param cpInfo classpaths used to resolve
     * @return the URL of the javadoc page or null when the javadoc is not available.
     */
    @org.netbeans.api.annotations.common.SuppressWarnings(value={"DMI_COLLECTION_OF_URLS"}/*,justification="URLs have never host part"*/)    //NOI18N
    public static URL getJavadoc (final Element element, final ClasspathInfo cpInfo) {      
        if (element == null || cpInfo == null) {
            throw new IllegalArgumentException ("Cannot pass null as an argument of the SourceUtils.getJavadoc");  //NOI18N
        }
        
        ClassSymbol clsSym = null;
        String pkgName;
        String pageName;
        boolean buildFragment = false;
        if (element.getKind() == ElementKind.PACKAGE) {
            List<? extends Element> els = element.getEnclosedElements();            
            for (Element e :els) {
                if (e.getKind().isClass() || e.getKind().isInterface()) {
                    clsSym = (ClassSymbol) e;
                    break;
                }
            }
            if (clsSym == null) {
                return null;
            }
            pkgName = FileObjects.convertPackage2Folder(((PackageElement)element).getQualifiedName().toString());
            pageName = PACKAGE_SUMMARY;
        }
        else {
            Element e = element;
            StringBuilder sb = new StringBuilder();
            while(e.getKind() != ElementKind.PACKAGE) {
                if (e.getKind().isClass() || e.getKind().isInterface()) {
                    if (sb.length() > 0)
                        sb.insert(0, '.');
                    sb.insert(0, e.getSimpleName());
                    if (clsSym == null)
                        clsSym = (ClassSymbol)e;
                }
                e = e.getEnclosingElement();
            }
            if (clsSym == null)
                return null;
            pkgName = FileObjects.convertPackage2Folder(((PackageElement)e).getQualifiedName().toString());
            pageName = sb.toString();
            buildFragment = element != clsSym;
        }
        
        if (clsSym.completer != null) {
            clsSym.complete();
        }
        
        URL sourceRoot = null;
        Set<URL> binaries = new HashSet<URL>();        
        try {
            if (clsSym.classfile != null) {
                FileObject  fo = URLMapper.findFileObject(clsSym.classfile.toUri().toURL());
                StringTokenizer tk = new StringTokenizer(pkgName,"/");             //NOI18N
                for (int i=0 ;fo != null && i<=tk.countTokens(); i++) {
                    fo = fo.getParent();
                }
                if (fo != null) {
                    URL url = fo.getURL();
                    sourceRoot = JavaIndex.getSourceRootForClassFolder(url);
                    if (sourceRoot == null) {
                        binaries.add(url);
                    } else {
                        // sourceRoot may be a class root in reality
                        binaries.add(sourceRoot);
                    }
                }
            }
            if (sourceRoot != null) {
                FileObject sourceFo = URLMapper.findFileObject(sourceRoot);
                if (sourceFo != null) {
                    ClassPath exec = ClassPath.getClassPath(sourceFo, ClassPath.EXECUTE);
                    ClassPath compile = ClassPath.getClassPath(sourceFo, ClassPath.COMPILE);
                    ClassPath source = ClassPath.getClassPath(sourceFo, ClassPath.SOURCE);
                    if (exec == null) {
                        exec = compile;
                        compile = null;
                    }
                    if (exec != null && source != null) {
                        Set<URL> roots = new HashSet<URL>();
                        for (ClassPath.Entry e : exec.entries()) {
                            roots.add(e.getURL());
                        }
                        if (compile != null) {
                            for (ClassPath.Entry e : compile.entries()) {
                                roots.remove(e.getURL());
                            }
                        }
                        List<FileObject> sourceRoots = Arrays.asList(source.getRoots());
out:                    for (URL e : roots) {
                            FileObject[] res = SourceForBinaryQuery.findSourceRoots(e).getRoots();
                            for (FileObject fo : res) {
                                if (sourceRoots.contains(fo)) {
                                    binaries.add(e);
                                    continue out;
                                }
                            }
                        }
                    }
                }
            }
            for (URL binary : binaries) {
                Result javadocResult = JavadocForBinaryQuery.findJavadoc(binary);
                URL[] result = javadocResult.getRoots();
                for (int cntr = 0; cntr < result.length; cntr++) {
                    if (!result[cntr].toExternalForm().endsWith("/")) { // NOI18N
                        Logger.getLogger(SourceUtils.class.getName()).log(Level.WARNING, "JavadocForBinaryQuery.Result: {0} returned non-folder URL: {1}, ignoring", new Object[] {javadocResult.getClass(), result[cntr].toExternalForm()});
                        result[cntr] = null;
                    }
                }
                ClassPath cp = ClassPathSupport.createClassPath(result);
                FileObject fo = cp.findResource(pkgName);
                if (fo != null) {
                    for (FileObject child : fo.getChildren()) {
                        if (pageName.equals(child.getName()) && FileObjects.HTML.equalsIgnoreCase(child.getExt())) {
                            URL url = child.getURL();
                            CharSequence fragment = null;
                            if (url != null && buildFragment) {
                                fragment = getFragment(element);
                            }
                            if (fragment != null && fragment.length() > 0) {
                                try {
                                    // Javadoc fragments may contain chars that must be escaped to comply with RFC 2396.
                                    // Unfortunately URLEncoder escapes almost everything but
                                    // spaces replaces with '+' char which is wrong so it is
                                    // replaced with "%20"escape sequence here.
                                    String encodedfragment = URLEncoder.encode(fragment.toString(), "UTF-8"); // NOI18N
                                    encodedfragment = encodedfragment.replace("+", "%20"); // NOI18N
                                    return new URI(url.toExternalForm() + '#' + encodedfragment).toURL();
                                } catch (URISyntaxException ex) {
                                    Exceptions.printStackTrace(ex);
                                } catch (UnsupportedEncodingException ex) {
                                    Exceptions.printStackTrace(ex);
                                } catch (MalformedURLException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                            return url;
                        }
                    }
                }
            }
            
        } catch (MalformedURLException e) {
            Exceptions.printStackTrace(e);
        }
        catch (FileStateInvalidException e) {
            Exceptions.printStackTrace(e);
        }
        return null;
    }
    
    private static CharSequence getFragment(Element e) {
        StringBuilder sb = new StringBuilder();
        if (!e.getKind().isClass() && !e.getKind().isInterface()) {
            if (e.getKind() == ElementKind.CONSTRUCTOR) {
                sb.append(e.getEnclosingElement().getSimpleName());
            } else {
                sb.append(e.getSimpleName());
            }
            if (e.getKind() == ElementKind.METHOD || e.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement ee = (ExecutableElement)e;
                sb.append('('); //NOI18N
                for (Iterator<? extends VariableElement> it = ee.getParameters().iterator(); it.hasNext();) {
                    VariableElement param = it.next();
                    appendType(sb, param.asType(), ee.isVarArgs() && !it.hasNext());
                    if (it.hasNext())
                        sb.append(", ");
                }
                sb.append(')'); //NOI18N
            }
        }
        return sb;
    }
    
    private static void appendType(StringBuilder sb, TypeMirror type, boolean varArg) {
        switch (type.getKind()) {
            case ARRAY:
                appendType(sb, ((ArrayType)type).getComponentType(), false);
                sb.append(varArg ? "..." : "[]"); //NOI18N
                break;
            case DECLARED:
                sb.append(((TypeElement)((DeclaredType)type).asElement()).getQualifiedName());
                break;
            default:
                sb.append(type);
        }
    }
    
    /**
     * Tests whether the initial scan is in progress.
     */
    public static boolean isScanInProgress () {
        return IndexingManager.getDefault().isIndexing();
    }

    /**
     * Waits for the end of the initial scan, this helper method 
     * is designed for tests which require to wait for end of initial scan.
     * @throws InterruptedException is thrown when the waiting thread is interrupted.
     * @deprecated use {@link JavaSource#runWhenScanFinished}
     */
    public static void waitScanFinished () throws InterruptedException {
        try {
            class T extends UserTask implements ClasspathInfoProvider {
                private final ClassPath EMPTY_PATH = ClassPathSupport.createClassPath(new URL[0]);
                private final ClasspathInfo cpinfo = ClasspathInfo.create(EMPTY_PATH, EMPTY_PATH, EMPTY_PATH);
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    // no-op
                }

                public ClasspathInfo getClasspathInfo() {
                    return cpinfo;
                }
            }
            Future<Void> f = ParserManager.parseWhenScanFinished(JavacParser.MIME_TYPE, new T());
            if (!f.isDone()) {
                f.get();
            }
        } catch (Exception ex) {
        }
    }
    
    
    /**
     * Returns the dependent source path roots for given source root.
     * It returns all the open project source roots which have either
     * direct or transitive dependency on the given source root.
     * @param root to find the dependent roots for
     * @return {@link Set} of {@link URL}s containing at least the
     * incoming root, never returns null.
     * @since 0.10
     */
    @org.netbeans.api.annotations.common.SuppressWarnings(value={"DMI_COLLECTION_OF_URLS"}/*,justification="URLs have never host part"*/)    //NOI18N
    public static Set<URL> getDependentRoots (final URL root) {
        final Map<URL, List<URL>> deps = IndexingController.getDefault().getRootDependencies();
        return getDependentRootsImpl (root, deps);
    }
    

    @org.netbeans.api.annotations.common.SuppressWarnings(value={"DMI_COLLECTION_OF_URLS"}/*,justification="URLs have never host part"*/)    //NOI18N
    static Set<URL> getDependentRootsImpl (final URL root, final Map<URL, List<URL>> deps) {
        //Create inverse dependencies        
        final Map<URL, List<URL>> inverseDeps = new HashMap<URL, List<URL>> ();
        for (Map.Entry<URL,List<URL>> entry : deps.entrySet()) {
            final URL u1 = entry.getKey();
            final List<URL> l1 = entry.getValue();
            for (URL u2 : l1) {
                List<URL> l2 = inverseDeps.get(u2);
                if (l2 == null) {
                    l2 = new ArrayList<URL>();
                    inverseDeps.put (u2,l2);
                }
                l2.add (u1);
            }
        }
        //Collect dependencies
        final Set<URL> result = new HashSet<URL>();
        final LinkedList<URL> todo = new LinkedList<URL> ();
        todo.add (root);
        while (!todo.isEmpty()) {
            final URL u = todo.removeFirst();
            if (!result.contains(u)) {
                result.add (u);
                final List<URL> ideps = inverseDeps.get(u);
                if (ideps != null) {
                    todo.addAll (ideps);
                }
            }
        }
        //Filter non opened projects
        Set<ClassPath> cps = GlobalPathRegistry.getDefault().getPaths(ClassPath.SOURCE);
        Set<URL> toRetain = new HashSet<URL>();
        for (ClassPath cp : cps) {
            for (ClassPath.Entry e : cp.entries()) {
                toRetain.add(e.getURL());
            }
        }
        result.retainAll(toRetain);
        return result;
    }    
    
    //Helper methods    
    
    /**
     * Returns classes declared in the given source file which have the main method.
     * @param fo source file
     * @return the classes containing main method
     * @throws IllegalArgumentException when file does not exist or is not a java source file.
     */
    public static Collection<ElementHandle<TypeElement>> getMainClasses (final FileObject fo) {
        if (fo == null || !fo.isValid() || fo.isVirtual()) {
            throw new IllegalArgumentException ();
        }
        final JavaSource js = JavaSource.forFileObject(fo);        
        if (js == null) {
            throw new IllegalArgumentException ();
        }
        try {
            final List<ElementHandle<TypeElement>> result = new LinkedList<ElementHandle<TypeElement>>();
            js.runUserActionTask(new Task<CompilationController>() {            
                public void run(final CompilationController control) throws Exception {
                    if (control.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED).compareTo (JavaSource.Phase.ELEMENTS_RESOLVED)>=0) {
                        new TreePathScanner<Void,Void> () {
                           public @Override Void visitMethod(MethodTree node, Void p) {
                               ExecutableElement method = (ExecutableElement) control.getTrees().getElement(getCurrentPath());
                               if (method != null && SourceUtils.isMainMethod(method) && isAccessible(method.getEnclosingElement())) {
                                   result.add (ElementHandle.create((TypeElement)method.getEnclosingElement()));
                               }
                               return null;
                           }
                        }.scan(control.getCompilationUnit(), null);
                    }                   
                }

                private boolean isAccessible (Element element) {
                    ElementKind kind = element.getKind();
                    while (kind != ElementKind.PACKAGE) {
                        if (!kind.isClass() && !kind.isInterface()) {
                            return false;
                        }                    
                        Set<Modifier> modifiers = ((TypeElement)element).getModifiers();
                        Element parent = element.getEnclosingElement();
                        if (parent.getKind() != ElementKind.PACKAGE && !modifiers.contains(Modifier.STATIC)) {
                            return false;
                        }
                        element = parent;
                        kind = element.getKind();
                    }
                    return true;
                }

            }, true);
            return result;
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
            return Collections.<ElementHandle<TypeElement>>emptySet();
        }		
    }
    
    /**
     * Returns true when the class contains main method.
     * @param qualifiedName the fully qualified name of class
     * @param cpInfo the classpath used to resolve the class
     * @return true when the class contains a main method
     */
    public static boolean isMainClass (final String qualifiedName, ClasspathInfo cpInfo) {
        if (qualifiedName == null || cpInfo == null) {
            throw new IllegalArgumentException ();
        }
        final boolean[] result = new boolean[]{false};
        JavaSource js = JavaSource.create(cpInfo);
        try {
            js.runUserActionTask(new Task<CompilationController>() {

                public void run(CompilationController control) throws Exception {
                    TypeElement type = ((JavacElements)control.getElements()).getTypeElementByBinaryName(qualifiedName);
                    if (type == null) {
                        return;
                    }
                    List<? extends ExecutableElement> methods = ElementFilter.methodsIn(type.getEnclosedElements());
                    for (ExecutableElement method : methods) {
                        if (SourceUtils.isMainMethod(method)) {
                            result[0] = true;
                            break;
                        }
                    }
                }

            }, true);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        return result[0];
    }
    
    /**
     * Returns true if the method is a main method
     * @param method to be checked
     * @return true when the method is a main method
     */
    public static boolean isMainMethod (final ExecutableElement method) {
        if (!"main".contentEquals(method.getSimpleName())) {                //NOI18N
            return false;
        }
        long flags = ((Symbol.MethodSymbol)method).flags();                 //faster
        if (((flags & Flags.PUBLIC) == 0) || ((flags & Flags.STATIC) == 0)) {
            return false;
        }
        if (method.getReturnType().getKind() != TypeKind.VOID) {
            return false;
        }
        List<? extends VariableElement> params = method.getParameters();
        if (params.size() != 1) {
            return false;
        }
        TypeMirror param = params.get(0).asType();
        if (param.getKind() != TypeKind.ARRAY) {
            return false;
        }
        ArrayType array = (ArrayType) param;
        TypeMirror compound = array.getComponentType();
        if (compound.getKind() != TypeKind.DECLARED) {
            return false;
        }
        if (!"java.lang.String".contentEquals(((TypeElement)((DeclaredType)compound).asElement()).getQualifiedName())) {   //NOI18N
            return false;
        }
        return true;
    }
    
    /**
     * Returns classes declared under the given source roots which have the main method.
     * @param sourceRoots the source roots
     * @return the classes containing the main methods
     * Currently this method is not optimized and may be slow
     */
    public static Collection<ElementHandle<TypeElement>> getMainClasses (final FileObject[] sourceRoots) {
        final List<ElementHandle<TypeElement>> result = new LinkedList<ElementHandle<TypeElement>> ();
        for (final FileObject root : sourceRoots) {
            try {               
                final File rootFile = FileUtil.toFile(root);
                ClassPath bootPath = ClassPath.getClassPath(root, ClassPath.BOOT);
                ClassPath compilePath = ClassPath.getClassPath(root, ClassPath.COMPILE);
                ClassPath srcPath = ClassPathSupport.createClassPath(new FileObject[] {root});
                ClasspathInfo cpInfo = ClasspathInfo.create(bootPath, compilePath, srcPath);
                JavaSource js = JavaSource.create(cpInfo);
                js.runUserActionTask(new Task<CompilationController>() {
                    public void run(CompilationController control) throws Exception {
                        final URL rootURL = root.getURL();
                        Iterable<? extends URL> mainClasses = ExecutableFilesIndex.DEFAULT.getMainClasses(rootURL);                        
                        List<ElementHandle<TypeElement>> classes = new LinkedList<ElementHandle<TypeElement>>();
                        for (URL mainClass : mainClasses) {
                            File mainFo = new File (URI.create(mainClass.toExternalForm()));
                            if (mainFo.exists()) {
                                classes.addAll(JavaCustomIndexer.getRelatedTypes(mainFo, rootFile));
                            }
                        }
                        for (ElementHandle<TypeElement> cls : classes) {
                            TypeElement te = cls.resolve(control);
                            if (te != null) {
                                Iterable<? extends ExecutableElement> methods = ElementFilter.methodsIn(te.getEnclosedElements());
                                for (ExecutableElement method : methods) {
                                    if (isMainMethod(method)) {
                                        if (isIncluded(cls, control.getClasspathInfo())) {
                                            result.add (cls);
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }, false);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
                return Collections.<ElementHandle<TypeElement>>emptySet();
            }
        }
        return result;
    }

    private static boolean isIncluded (final ElementHandle<TypeElement> element, final ClasspathInfo cpInfo) {
        FileObject fobj = getFile (element,cpInfo);
        if (fobj == null) {
            //Not source
            return true;
        }
        ClassPath sourcePath = cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE);
        for (ClassPath.Entry e : sourcePath.entries()) {
            FileObject root = e.getRoot ();
            if (root != null && FileUtil.isParentOf(root,fobj)) {
                return e.includes(fobj);
            }
        }
        return true;
    }
    
    private static boolean isCaseSensitive () {
        return ! new File ("a").equals (new File ("A"));    //NOI18N
    }
    
    private static String getSourceFileName (String classFileName) {
        int index = classFileName.indexOf('$'); //NOI18N
        return index == -1 ? classFileName : classFileName.substring(0,index);
        }
        
    /**
     * @since 0.24
     */
    public static WildcardType resolveCapturedType(TypeMirror type) {
        if (type instanceof Type.CapturedType) {
            return ((Type.CapturedType) type).wildcard;
        } else {
            return null;
        }
    }
    
    // --------------- Helper methods of getFile () -----------------------------
    private static ClassPath createClassPath (ClasspathInfo cpInfo, PathKind kind) throws MalformedURLException {
	return ClasspathInfoAccessor.getINSTANCE().getCachedClassPath(cpInfo, kind);	
    }    
    
    // --------------- End of getFile () helper methods ------------------------------

    private static final int MAX_LEN = 6;
    /**
     * Utility method for generating method parameter names based on incoming
     * class name when source is unavailable.
     * <p/>
     * This method uses both subjective heuristics to follow common patterns
     * for common JDK classes, acronym creation for bicapitalized names, and
     * vowel and repeated character elision if that fails, to generate
     * readable, programmer-friendly method names.
     *
     * @param typeName The fqn of the parameter class
     * @param used A set of names that have already been used for parameters
     * and should not be reused, to avoid creating uncompilable code
     * @return A programmer-friendly parameter name (i.e. not arg0, arg1...)
     */
    static @NonNull String generateReadableParameterName (@NonNull String typeName, @NonNull Set<String> used) {
        boolean arr = typeName.indexOf ("[") > 0 || typeName.endsWith("..."); //NOI18N
        typeName = trimToSimpleName (typeName);
        String result = typeName.toLowerCase();
        //First, do some common, sane substitutions that are common java parlance
        if ( typeName.endsWith ( "Listener" ) ) { //NOI18N
            result = Character.toLowerCase(typeName.charAt(0)) + "l"; //NOI18N
        } else if ( "Object".equals (typeName)) { //NOI18N
            result = "o"; //NOI18N
        } else if ("Class".equals(typeName)) { //NOI18N
            result = "type"; //NOI18N
        } else if ( "InputStream".equals(typeName)) { //NOI18N
            result = "in"; //NOI18N
        } else if ( "OutputStream".equals(typeName)) {
            result = "out"; //NOI18N
        } else if ( "Runnable".equals(typeName)) {
            result = "r"; //NOI18N
        } else if ( "Lookup".equals(typeName)) {
            result = "lkp"; //NOI18N
        } else if ( typeName.endsWith ( "Stream" )) { //NOI18N
            result = "stream"; //NOI18N
        } else if ( typeName.endsWith ("Writer")) { //NOI18N
            result = "writer"; //NOI18N
        } else if ( typeName.endsWith ("Reader")) { //NOI18N
            result = "reader"; //NOI18N
        } else if ( typeName.endsWith ( "Panel" )) { //NOI18N
            result = "pnl"; //NOI18N
        } else if ( typeName.endsWith ( "Action" )) { //NOI18N
            result = "action"; //NOI18N
        }
        //Now see if we've made a large and unwieldy variable - people
        //usually prefer reasonably short but legible arguments
        if ( result.length () > MAX_LEN ) {
            //See if we can turn, say, NoClassDefFoundError into "ncdfe"
            result = tryToMakeAcronym ( typeName );
            //No luck?  We've probably got one long word like Component or Runnable
            if (result.length() > MAX_LEN) {
                //First, strip out vowels - people easily figure out words
                //missing vowels - common in abbreviations and spam mails
                result = elideVowelsAndRepetitions(result);
                if (result.length() > MAX_LEN) {
                    //Still too long?  Give up and give them a 1 character var name
                    result = new StringBuilder().append(
                            result.charAt(0)).toString().toLowerCase();
                }
            }
        }
        //Make sure we haven't killed everything - if so, use a generic version
        if ( result.trim ().length () == 0 ) {
            result = "value"; //NOI18N
        }
        //If it's an array, pluralize it (english language style - but better than nothing)
        if (arr) {
            result += "s"; //NOI18N
        }
        //Now make sure it's legal;  if not, make it a single letter
        if ( isPrimitiveTypeName ( result ) || !Utilities.isJavaIdentifier ( result ) ) {
            StringBuilder sb = new StringBuilder();
            sb.append (result.charAt(0));
            result = sb.toString();
        }
        //Now make sure we're not duplicating a variable name we already used
        String test = result;
        int revs = 0;
        while ( used.contains ( test ) ) {
            revs++;
            test = result + revs;
        }
        result = test;
        used.add ( result );
        return result;
    }

    /**
     * Trims to the simple class name and removes and generics
     *
     * @param typeName The class name
     * @return A simplified class name
     */
    private static String trimToSimpleName (String typeName) {
        String result = typeName;
        int ix = result.indexOf ("<"); //NOI18N
        if (ix > 0 && ix != typeName.length() - 1) {
            result = typeName.substring(0, ix);
        }
        if (result.endsWith ("...")) { //NOI18N
            result = result.substring (0, result.length() - 3);
        }
        ix = result.lastIndexOf ("$"); //NOI18N
        if (ix > 0 && ix != result.length() - 1) {
            result = result.substring(ix + 1);
        } else {
            ix = result.lastIndexOf("."); //NOI18N
            if (ix > 0 && ix != result.length() - 1) {
                result = result.substring(ix + 1);
            }
        }
        ix = result.indexOf ( "[" ); //NOI18N
        if ( ix > 0 ) {
            result = result.substring ( 0, ix );
        }
        return result;
    }

    /**
     * Removes vowels and repeated letters.  This is used to generate names
     * where the class name a single long word - e.g. abbreviate
     * Runnable to rnbl
     * @param name The name
     * @return A shortened version of it
     */
    private static String elideVowelsAndRepetitions (String name) {
        char[] chars = name.toCharArray();
        StringBuilder sb = new StringBuilder();
        char last = 0;
        char lastUsed = 0;
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (Character.isDigit(c)) {
                continue;
            }
            if (i == 0 || Character.isUpperCase(c)) {
                if (lastUsed != c) {
                    sb.append (c);
                    lastUsed = c;
                }
            } else if (c != last && !isVowel(c)) {
                if (lastUsed != c) {
                    sb.append (c);
                    lastUsed = c;
                }
            }
            last = c;
        }
        return sb.toString();
    }

    private static boolean isVowel(char c) {
        return Arrays.binarySearch(VOWELS, c) >= 0;
    }

    /**
     * Vowels in various indo-european-based languages
     */
    private static char[] VOWELS = new char[] {
    //IMPORTANT:  This array is sorted.  If you add to it,
    //add in the correct place or Arrays.binarySearch will break on it
    '\u0061', '\u0065', '\u0069', '\u006f', '\u0075', '\u0079', '\u00e9', '\u00ea',  //NOI18N
    '\u00e8', '\u00e1', '\u00e2', '\u00e6', '\u00e0', '\u03b1', '\u00e3',  //NOI18N
    '\u00e5', '\u00e4', '\u00eb', '\u00f3', '\u00f4', '\u0153', '\u00f2',  //NOI18N
    '\u03bf', '\u00f5', '\u00f6', '\u00ed', '\u00ee', '\u00ec', '\u03b9',  //NOI18N
    '\u00ef', '\u00fa', '\u00fb', '\u00f9', '\u03d2', '\u03c5', '\u00fc',  //NOI18N
    '\u0430', '\u043e', '\u044f', '\u0438', '\u0439', '\u0435', '\u044b',  //NOI18N
    '\u044d', '\u0443', '\u044e', };

    //PENDING:  The below would be much prettier;  whether it survives
    //cross-platform encoding issues in hg is another question;  the hg diff generated
    //was incorrect
/*
    'a', 'e', 'i', 'o', 'u', 'y', 'à', 'á', //NOI18N
    'â', 'ã', 'ä', 'å', 'æ', 'è', 'é', //NOI18N
    'ê', 'ë', 'ì', 'í', 'î', 'ï', 'ò', //NOI18N
    'ó', 'ô', 'õ', 'ö', 'ù', 'ú', 'û', //NOI18N
    'ü', 'œ', 'α', 'ι', 'ο', 'υ', 'ϒ', //NOI18N
    'а', 'е', 'и', 'й', 'о', 'у', 'ы', //NOI18N
    'э', 'ю', 'я'}; //NOI18N
*/
    /**
     * Determine if a string matches a java primitive type.  Used in generating reasonable variable names.
     */
    private static boolean isPrimitiveTypeName (String typeName) {
        return (
                //Whoa, ascii art!
                "void".equals ( typeName ) || //NOI18N
                "int".equals ( typeName ) || //NOI18N
                "long".equals ( typeName ) || //NOI18N
                "float".equals ( typeName ) || //NOI18N
                "double".equals ( typeName ) || //NOI18N
                "short".equals ( typeName ) || //NOI18N
                "char".equals ( typeName ) || //NOI18N
                "boolean".equals ( typeName ) ); //NOI18N
    }

    /**
     * Try to create an acronym-style variable name from a string - i.e.,
     * "JavaDataObject" becomes "jdo".
     */
    private static String tryToMakeAcronym (String s) {
        char[] c = s.toCharArray ();
        StringBuilder sb = new StringBuilder ();
        for ( int i = 0; i < c.length; i++ ) {
            if ( Character.isUpperCase (c[i])) {
                sb.append ( c[ i ] );
            }
        }
        if ( sb.length () > 1 ) {
            return sb.toString ().toLowerCase ();
        } else {
            return s.toLowerCase();
        }
    }
}
