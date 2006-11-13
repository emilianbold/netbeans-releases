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
import java.util.*;

import javax.lang.model.element.*;
import javax.tools.JavaFileObject.Kind;

import com.sun.source.tree.*;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Kinds;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Name;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.modules.java.JavaDataLoader;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.usages.RepositoryUpdater;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

/**
 *
 * @author Dusan Balek
 */public class SourceUtils {    
    
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
     * Waits for the end of the initial scan, this helper method 
     * is designed for tests which require to wait for end of initial scan.
     * @throws InterruptedException is thrown when the waiting thread is interrupted.
     */
    public static void waitScanFinished () throws InterruptedException {
        RepositoryUpdater.getDefault().waitScanFinished();
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
