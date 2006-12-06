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
import javax.lang.model.util.ElementFilter;
import javax.tools.JavaFileObject.Kind;

import com.sun.source.tree.*;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Kinds;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Name;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.JavaDataLoader;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.usages.Index;
import org.netbeans.modules.java.source.usages.RepositoryUpdater;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Dusan Balek
 */
public class SourceUtils {    
     
    private static final String PACKAGE_SUMMARY = "package-summary";   //NOI18N
    
    private SourceUtils() {}
    
    public static Tree treeFor(CompilationInfo info, Element element) {
        Context ctx = getSourceContextFor(info.getClasspathInfo(), Phase.ELEMENTS_RESOLVED, element);
        if (ctx != null) {
            Element e = getSourceElementFor(element, ctx);
            if (e != null)
                return JavacElements.instance(ctx).getTree((Symbol)e);
        }
        return null;
    }

    public static TreePath pathFor(CompilationInfo info, Element element) {
        Context ctx = getSourceContextFor(info.getClasspathInfo(), Phase.ELEMENTS_RESOLVED, element);
        if (ctx != null) {
            Element e = getSourceElementFor(element, ctx);
            if (e != null)
                return JavacTrees.instance(ctx).getPath(e);
        }
        return null;
    }
    
    public static Element getImplementationOf(CompilationInfo info, ExecutableElement method, TypeElement origin) {
        Context c = ((JavacTaskImpl) info.getJavacTask()).getContext();
        return ((MethodSymbol)method).implementation((TypeSymbol)origin, com.sun.tools.javac.code.Types.instance(c), true);
    }

    /**
     * Returns the type element within which this member or constructor
     * is declared. Does not accept pakages
     * If this is the declaration of a top-level type (a non-nested class
     * or interface), returns null.
     *
     * @return the type declaration within which this member or constructor
     * is declared, or null if there is none
     * @throws IllegalArgumentException if the provided element is a package element
     */
    public static TypeElement getEnclosingTypeElement( Element element ) throws IllegalArgumentException {
	
	if( element.getKind() == ElementKind.PACKAGE ) {
	    throw new IllegalArgumentException();
	}
	
        if (element.getEnclosingElement().getKind() == ElementKind.PACKAGE) {
            //element is a top level class, returning null according to the contract:
            return null;
        }
        
	while( !(element.getEnclosingElement().getKind().isClass() || 
	       element.getEnclosingElement().getKind().isInterface()) ) {
	    element = element.getEnclosingElement();
	}
	
	return (TypeElement)element.getEnclosingElement(); // Wrong
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
    
    private static EnumSet JAVA_JFO_KIND = EnumSet.of(Kind.CLASS, Kind.SOURCE);
        
    
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
        Element el = info.getTrees().getElement(new TreePath(cut));
        PackageElement pkg = el != null && el.getKind() == ElementKind.PACKAGE ? (PackageElement)el : null;
        Scope scope = info.getTrees().getScope(context);
        String qName = fqn;
        StringBuilder sqName = new StringBuilder();
        String sName = null;
        boolean clashing = false;
        while(qName != null && qName.length() > 0) {
            int lastDot = qName.lastIndexOf('.');
            String simple = qName.substring(lastDot < 0 ? 0 : lastDot + 1);
            if (sName == null)
                sName = simple;
            else
                sqName.insert(0, '.');
            sqName.insert(0, simple);
            TypeElement te = info.getElements().getTypeElement(qName);
            if (te != null) {
                Scope s = scope;
                while (s != null) {
                    for (TypeElement e : ElementFilter.typesIn(s.getLocalElements())) {
                        if (simple.contentEquals(e.getSimpleName())) {
                            //either a clash or already imported:
                            if (qName.contentEquals(e.getQualifiedName())) {
                                return sqName.toString();
                            } else if (fqn == qName) {
                                clashing = true;
                            }
                        }
                    }
                    s = s.getEnclosingScope();
                }
                if (pkg != null) {
                    for (TypeElement e : ElementFilter.typesIn(pkg.getEnclosedElements())) {
                        if (simple.contentEquals(e.getSimpleName())) {
                            //either a clash or already imported:
                            if (qName.contentEquals(e.getQualifiedName())) {
                                return sqName.toString();
                            } else if (fqn == qName) {
                                clashing = true;
                            }
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
            CompilationUnitTree nue = (CompilationUnitTree) ((WorkingCopy)info).getChangeSet().getChange(cut);
            cut = nue != null ? nue : cut;
            ((WorkingCopy)info).rewrite(info.getCompilationUnit(), addImports(cut, Collections.singletonList(fqn), ((WorkingCopy)info).getTreeMaker()));
        } else {
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    try {
                        info.getJavaSource().runModificationTask(new CancellableTask<WorkingCopy>() {
                            public void cancel() {
                            }
                            public void run(WorkingCopy copy) throws Exception {
                                copy.toPhase(Phase.ELEMENTS_RESOLVED);
                                copy.rewrite(copy.getCompilationUnit(), addImports(copy.getCompilationUnit(), Collections.singletonList(fqn), copy.getTreeMaker()));                                
                            }
                        }).commit();
                    } catch (IOException ioe) {
                        Exceptions.printStackTrace(ioe);
                    }
                }
            });
        }
        TypeElement te = info.getElements().getTypeElement(fqn);
        if (te != null) {
            ((JCCompilationUnit) info.getCompilationUnit()).namedImportScope.enterIfAbsent((Symbol) te);
        }
        
        return sName;
    }
    
    /**
     *
     *
     */
    public static CompilationUnitTree addImports(CompilationUnitTree cut, List<String> toImport, TreeMaker make)
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
     * Element must be defined in source file
     * @param element for which the {@link FileObject} should be located
     * @return the defining {@link FileObject} or null if it cannot be
     * found in any source file.
     */
    public static FileObject getFile(Element element) {
        if (element == null) {
            throw new IllegalArgumentException ("Cannot pass null as an argument of the SourceUtils.getFile");  //NOI18N
        }
        Element prev = null;
        while (element.getKind() != ElementKind.PACKAGE) {
            prev = element;
            element = element.getEnclosingElement();
        }
        if (prev == null || (!prev.getKind().isClass() && !prev.getKind().isInterface()))
            return null;
        ClassSymbol clsSym = (ClassSymbol)prev;
        URI uri;
        if (clsSym.completer != null)
            clsSym.complete();
        if (clsSym.sourcefile != null && (uri=clsSym.sourcefile.toUri())!= null && uri.isAbsolute()) {
            try {
                return URLMapper.findFileObject(uri.toURL());
            } catch (MalformedURLException ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }
    
    /**
     * Returns a {@link FileObject} in which the Element is defined.
     * @param element for which the {@link FileObject} should be located
     * @param cpInfo the classpaths context
     * @return the defining {@link FileObject} or null if it cannot be
     * found
     */
    public static FileObject getFile (Element element, ClasspathInfo cpInfo) {
        try {
        if (element == null || cpInfo == null) {
            throw new IllegalArgumentException ("Cannot pass null as an argument of the SourceUtils.getFile");  //NOI18N
        }
        Element prev = null;
        while (element.getKind() != ElementKind.PACKAGE) {
            prev = element;
            element = element.getEnclosingElement();
        }
        if (prev == null || (!prev.getKind().isClass() && !prev.getKind().isInterface()))
            return null;
        ClassSymbol clsSym = (ClassSymbol)prev;
        URI uri;
        if (clsSym.completer != null)
            clsSym.complete();
        if (clsSym.sourcefile != null && (uri=clsSym.sourcefile.toUri())!= null && uri.isAbsolute()) {
            return URLMapper.findFileObject(uri.toURL());
        }
        else {
            if (clsSym.classfile == null)
                return null;
            uri = clsSym.classfile.toUri();
            if (uri == null || !uri.isAbsolute()) {
                return null;
            }
            FileObject classFo = URLMapper.findFileObject(uri.toURL());
            if (classFo == null) {
                return null;
            }
            ClassPath cp = ClassPathSupport.createProxyClassPath(
                new ClassPath[] {
                    createClassPath(cpInfo,ClasspathInfo.PathKind.BOOT),
                    createClassPath(cpInfo,ClasspathInfo.PathKind.OUTPUT),
                    createClassPath(cpInfo,ClasspathInfo.PathKind.COMPILE),
            });
            FileObject root = cp.findOwnerRoot(classFo);
            if (root == null) {
                return null;
            }
            String parentResName = cp.getResourceName(classFo.getParent(),'/',false);       //NOI18N
            SourceForBinaryQuery.Result result = SourceForBinaryQuery.findSourceRoots(root.getURL());
            FileObject[] sourceRoots = result.getRoots();
            ClassPath sourcePath = ClassPathSupport.createClassPath(sourceRoots);
            List<FileObject> folders = (List<FileObject>) sourcePath.findAllResources(parentResName);
            boolean caseSensitive = isCaseSensitive ();
            final String sourceFileName = getSourceFileName (classFo.getName());
            for (FileObject folder : folders) {
                FileObject[] children = folder.getChildren();
                for (FileObject child : children) {
                    if (((caseSensitive && child.getName().equals (sourceFileName)) ||
                        (!caseSensitive && child.getName().equalsIgnoreCase (sourceFileName)))
                        &&
                    JavaDataLoader.JAVA_EXTENSION.equalsIgnoreCase(child.getExt())) {
                        return child;
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
    
    /**
     * Returns a {@link FileObject} of the source file in which the handle is declared.
     * @param handle to find the {@link FileObject} for
     * @param cpInfo classpaths for resolving handle
     * @return {@link FileObject} or null when the source file cannot be found
     */
    public static FileObject getFile (final ElementHandle<? extends Element> handle, final ClasspathInfo cpInfo) {
        if (handle == null || cpInfo == null) {
            throw new IllegalArgumentException ("Cannot pass null as an argument of the SourceUtils.getFile");  //NOI18N
        }
        try {
            boolean pkg = handle.getKind() == ElementKind.PACKAGE;
            String[] signature = handle.getSignature();
            assert signature.length >= 1;
            ClassPath cp = ClassPathSupport.createProxyClassPath(
                new ClassPath[] {
                    createClassPath(cpInfo,ClasspathInfo.PathKind.BOOT),
                    createClassPath(cpInfo,ClasspathInfo.PathKind.OUTPUT),
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
                List<FileObject> folders = (List<FileObject>) sourcePath.findAllResources(pkgName);
                if (pkg) {
                    return folders.isEmpty() ? null : folders.get(0);
                }
                else {               
                    boolean caseSensitive = isCaseSensitive ();
                    String sourceFileName = getSourceFileName (className);
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
    
    /**
     * Finds {@link URL} of a javadoc page for given element when available. This method 
     * uses {@link JavadocForBinaryQuery} to find the javadoc page for the give element.
     * For {@link TypeElement} or {@link Element}s enclosed by the {@link TypeElement}
     * it returns the {@link URL} of the javadoc for top level {@link TypeElement}.
     * For {@link PackageElement} it returns the package-summary.html for given package.
     * @param element to find the Javadoc for
     * @param cpInfo classpaths used to resolve
     * @return the URL of the javadoc page or null when the javadoc is not available.
     */
    public static URL getJavadoc (Element element, final ClasspathInfo cpInfo) {      
        if (element == null || cpInfo == null) {
            throw new IllegalArgumentException ("Cannot pass null as an argument of the SourceUtils.getJavadoc");  //NOI18N
        }
        
        ClassSymbol clsSym = null;
        String pkgName;
        String pageName;
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
            Element prev = null;
            while (element.getKind() != ElementKind.PACKAGE) {
                prev = element;
                element = element.getEnclosingElement();
            }
            if (prev == null || (!prev.getKind().isClass() && !prev.getKind().isInterface())) {
                return null;
            }
            clsSym = (ClassSymbol)prev;
            pkgName = FileObjects.convertPackage2Folder(clsSym.getEnclosingElement().getQualifiedName().toString());
            pageName = clsSym.getSimpleName().toString();
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
                    sourceRoot = Index.getSourceRootForClassFolder(url);
                    if (sourceRoot == null) {
                        binaries.add(url);
                    }
                }
            }
            if (sourceRoot == null && binaries.isEmpty() && clsSym.sourcefile != null) {
                sourceRoot = clsSym.sourcefile.toUri().toURL();
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
                    if (exec == null || source == null) {
                        return null;
                    }
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
out:                for (URL e : roots) {
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
            for (URL binary : binaries) {
                URL[] result = JavadocForBinaryQuery.findJavadoc(binary).getRoots();
                ClassPath cp = ClassPathSupport.createClassPath(result);
                FileObject fo = cp.findResource(pkgName);
                if (fo != null) {
                    for (FileObject child : fo.getChildren()) {
                        if (pageName.equals(child.getName()) && FileObjects.HTML.equalsIgnoreCase(child.getExt())) {
                            return child.getURL();
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
    
    
    /**
     * Waits for the end of the initial scan, this helper method 
     * is designed for tests which require to wait for end of initial scan.
     * @throws InterruptedException is thrown when the waiting thread is interrupted.
     */
    public static void waitScanFinished () throws InterruptedException {
        RepositoryUpdater.getDefault().waitScanFinished();
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
            js.runUserActionTask(new CancellableTask<CompilationController>() {            
                public void run(final CompilationController control) throws Exception {
                    if (control.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED).compareTo (JavaSource.Phase.ELEMENTS_RESOLVED)>=0) {
                        new TreePathScanner<Void,Void> () {
                           public Void visitMethod(MethodTree node, Void p) {
                               ExecutableElement method = (ExecutableElement) control.getTrees().getElement(getCurrentPath());
                               if (SourceUtils.isMainMethod(method) && isAccessible(method.getEnclosingElement())) {
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
                        if (!modifiers.contains(Modifier.PUBLIC)) {
                            return false;
                        }
                        Element parent = element.getEnclosingElement();
                        if (parent.getKind() != ElementKind.PACKAGE && !modifiers.contains(Modifier.STATIC)) {
                            return false;
                        }
                        element = parent;
                        kind = element.getKind();
                    }
                    return true;
                }

                public void cancel() {}

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
            js.runUserActionTask(new CancellableTask<CompilationController>() {

                public void run(CompilationController control) throws Exception {
                    TypeElement type = control.getElements().getTypeElement(qualifiedName);
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

                public void cancel() {}

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
        for (FileObject root : sourceRoots) {
            try {
                ClasspathInfo cpInfo = ClasspathInfo.create(root);
                final Set<ElementHandle<TypeElement>> classes = cpInfo.getClassIndex().getDeclaredTypes("", ClassIndex.NameKind.PREFIX, EnumSet.of(ClassIndex.SearchScope.SOURCE));
                JavaSource js = JavaSource.create(cpInfo);
                js.runUserActionTask(new CancellableTask<CompilationController>() {
                    public void run(CompilationController control) throws Exception {
                        for (ElementHandle<TypeElement> cls : classes) {
                            TypeElement te = cls.resolve(control);
                            if (te != null) {
                                Iterable<? extends ExecutableElement> methods = ElementFilter.methodsIn(te.getEnclosedElements());
                                for (ExecutableElement method : methods) {
                                    if (isMainMethod(method)) {
                                        result.add (cls);
                                    }
                                }
                            }
                        }
                    }

                    public void cancel() {}                
                }, false);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
                return Collections.<ElementHandle<TypeElement>>emptySet();
            }
        }
        return result;
    }
    
    private static boolean isCaseSensitive () {
        return ! new File ("a").equals (new File ("A"));    //NOI18N
    }
    
    private static String getSourceFileName (String classFileName) {
        int index = classFileName.indexOf('$'); //NOI18N
        return index == -1 ? classFileName : classFileName.substring(0,index);
    }
    
    // --------------- Helper methods of getFile () -----------------------------
    private static ClassPath createClassPath (ClasspathInfo cpInfo, PathKind kind) throws MalformedURLException {
	return cpInfo.getClassPath (kind);	
    }    
    
    // --------------- End of getFile () helper methods ------------------------------

    private static Context getSourceContextFor(ClasspathInfo cpInfo, final JavaSource.Phase phase, Element element) {
        try {
            FileObject fo = getFile(element, cpInfo);
            if (fo != null) {
                JavaSource js = JavaSource.forFileObject(fo);
                if (js != null) {
                    final Context[] ret = new Context[1];
                    js.runUserActionTask(new CancellableTask<CompilationController>() {
                        public void cancel() {
                        }
                        public void run(CompilationController controller) throws Exception {
                            controller.toPhase(phase);
                            ret[0] = controller.getJavacTask().getContext();
                        }
                    },true);
                    return ret[0];
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
    private static Element getSourceElementFor(Element element, Context ctx) {
        Symbol sym = (Symbol)element;
        Symtab symbolTable = Symtab.instance(ctx);
        Name.Table nameTable = Name.Table.instance(ctx);
        Symbol owner = sym.owner;
        ClassSymbol enclCls = sym.enclClass();
        Name name = nameTable.fromString(enclCls.flatname.toString());
        ClassSymbol cls = symbolTable.classes.get(name);
        if (enclCls == sym)
            return cls;
        if (cls != null && owner == enclCls) {
            com.sun.tools.javac.code.Scope.Entry e = cls.members().lookup(nameTable.fromString(sym.name.toString()));
            while (e.scope != null) {
                if (e.sym.kind == sym.kind && (e.sym.flags_field & Flags.SYNTHETIC) == 0 &&
                        e.sym.type.toString().equals(sym.type.toString()))
                    return e.sym;
                e = e.next();
            }
        } else if (cls != null && owner.kind == Kinds.MTH && sym.kind == Kinds.VAR) {
            com.sun.tools.javac.code.Scope.Entry e = cls.members().lookup(nameTable.fromString(owner.name.toString()));
            Symbol newOwner = null;
            while (e.scope != null) {
                if (e.sym.kind == owner.kind && (e.sym.flags_field & Flags.SYNTHETIC) == 0 &&
                        e.sym.type.toString().equals(owner.type.toString())) {
                    newOwner = e.sym;
                    break;
                }
                e = e.next();
            }
            if (newOwner != null && newOwner.kind == Kinds.MTH) {
                int i = 0;
                for (com.sun.tools.javac.util.List<VarSymbol> l = ((MethodSymbol)owner).params; l.nonEmpty(); l = l.tail) {
                    i++;
                    if (sym == l.head)
                        break;
                }
                for (com.sun.tools.javac.util.List<VarSymbol> l = ((MethodSymbol)newOwner).params; l.nonEmpty(); l = l.tail) {
                    if (--i == 0)
                        return l.head;
                }
            }
        }
        return null;
    }
}
