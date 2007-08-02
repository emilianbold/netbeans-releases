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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.java.source;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.model.JavacElements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.swing.text.Document;
import javax.tools.DiagnosticListener;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.parsing.SourceFileObject;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/** Asorted information about the JavaSource.
 *
 * @author Petr Hrebejk, Tomas Zezula
 */
public class CompilationInfo {
    
    private JavaSource.Phase phase = JavaSource.Phase.MODIFIED;
    private CompilationUnitTree compilationUnit;
    private List<Diagnostic> errors;
    
    private JavacTaskImpl javacTask;
    private PositionConverter binding;
    final JavaFileObject jfo;    
    final JavaSource javaSource;        
    boolean needsRestart;
    boolean parserCrashed;      //When javac throws an error, the moveToPhase sets this flag to true to prevent the same exception to be rethrown
    
    private ElementUtilities elementUtilities;
    private TreeUtilities treeUtilities;
    private TypeUtilities typeUtilities;
    
    CompilationInfo () {
        this.javaSource = null;
        this.jfo = null;
        this.javacTask = null;
        this.errors = null;
    }
    
    CompilationInfo (  final JavaSource javaSource,final PositionConverter binding, final JavacTaskImpl javacTask) throws IOException {
        assert javaSource != null;        
        this.javaSource = javaSource;
        this.binding = binding;
        this.jfo = this.binding != null ? javaSource.jfoProvider.createJavaFileObject(binding.getFileObject(), this.binding.getFilter()) : null;
        this.javacTask = javacTask;        
        this.errors = new ArrayList<Diagnostic>();
    }
             
    // API of the class --------------------------------------------------------
    
    /**
     * Returns the current phase of the {@link JavaSource}.
     * @return {@link JavaSource.Phase} the state which was reached by the {@link JavaSource}.
     */
    public JavaSource.Phase getPhase() {
        return this.phase;
    }
       
    /**
     * Returns the javac tree representing the source file.
     * @return {@link CompilationUnitTree} the compilation unit cantaining the top level classes contained in the,
     * java source file. It may return null when the phase is less than {@link JavaSource.Phase#PARSED}
     */
    public CompilationUnitTree getCompilationUnit() {
        if (this.jfo == null) {
            throw new IllegalStateException ();
        }
        return this.compilationUnit;
    }
    
    /**
     * Returns the content of the file represented by the {@link JavaSource}.
     * @return String the java source
     */
    public String getText() {
        if (this.jfo == null) {
            throw new IllegalStateException ();
        }
        try {
            return this.jfo.getCharContent(false).toString();
        } catch (IOException ioe) {
            //Should never happen
            ErrorManager.getDefault().notify(ioe);
            return null;
        }
    }
    
    public TokenHierarchy<Void> getTokenHierarchy() {
        if (this.jfo == null) {
            throw new IllegalStateException ();
        }
        try {
            return ((SourceFileObject) this.jfo).getTokenHierarchy();
        } catch (IOException ioe) {
            //Should never happen
            ErrorManager.getDefault().notify(ioe);
            return null;
        }
    }
    
    /**
     * Returns the errors in the file represented by the {@link JavaSource}.
     * @return an list of {@link Diagnostic} 
     */
    public List<Diagnostic> getDiagnostics() {
        if (this.jfo == null) {
            throw new IllegalStateException ();
        }
        List<Diagnostic> errors = ((DiagnosticListenerImpl) javacTask.getContext().get(DiagnosticListener.class)).errors;
        List<Diagnostic> localErrors = new ArrayList<Diagnostic>(errors.size());
        
        for(Diagnostic m : errors) {
            if (this.jfo == m.getSource())
                localErrors.add(m);
        }
        return localErrors;
    }
    
    /**
     * Returns all top level elements defined in file for which the {@link CompilationInfo}
     * was created. The {@link CompilationInfo} has to be in phase {@link JavaSource#Phase#ELEMENTS_RESOLVED}.
     * @return list of top level elements, it may return null when this {@link CompilationInfo} is not
     * in phase {@link JavaSource#Phase#ELEMENTS_RESOLVED} or higher.
     * @throws IllegalStateException is thrown when the {@link JavaSource} was created with no files
     * @since 0.14
     */
    public List<? extends TypeElement> getTopLevelElements () throws IllegalStateException {
        if (this.jfo == null) {
            throw new IllegalStateException ();
        }
        List<TypeElement> result = new ArrayList<TypeElement>();
        if (this.javaSource.isClassFile()) {
            Elements elements = getElements();
            assert elements != null;
            assert this.javaSource.rootFo != null;
            String name = FileObjects.convertFolder2Package(FileObjects.stripExtension(FileUtil.getRelativePath(javaSource.rootFo, getFileObject())));
            TypeElement e = ((JavacElements)elements).getTypeElementByBinaryName(name);
            if (e != null) {                
                result.add (e);
            }
        }
        else {
            CompilationUnitTree cu = getCompilationUnit();
            if (cu == null) {
                return null;
            }
            else {
                final Trees trees = getTrees();
                assert trees != null;
                List<? extends Tree> typeDecls = cu.getTypeDecls();
                TreePath cuPath = new TreePath(cu);
                for( Tree t : typeDecls ) {
                    TreePath p = new TreePath(cuPath,t);
                    Element e = trees.getElement(p);
                    if ( e != null && ( e.getKind().isClass() || e.getKind().isInterface() ) ) {
                        result.add((TypeElement)e);
                    }
                }
            }
        }
        return Collections.unmodifiableList(result);
    }
    
    //todo: remove when Abort from javac is fixed
    private static boolean isLocal (TypeElement sym) {
        if  (sym.getQualifiedName().contentEquals("")) {    //NOI18N
            return true;
        }        
        Element enclosing = sym.getEnclosingElement();
        while (enclosing != null && enclosing.getKind() != ElementKind.PACKAGE) {
            if (!enclosing.getKind().isClass() && !enclosing.getKind().isInterface()) {
                return true;
            }
            enclosing = enclosing.getEnclosingElement();
        }
        return false;
    }
    
    public Trees getTrees() {
        return Trees.instance(getJavacTask());
    }
    
    public Types getTypes() {
        return getJavacTask().getTypes();
    }
    
    public Elements getElements() {
	return getJavacTask().getElements();
    }
        
    public JavaSource getJavaSource() {
        return javaSource;
    }
    
    public ClasspathInfo getClasspathInfo() {
	return javaSource.getClasspathInfo();
    }
    
    public FileObject getFileObject() {
        return this.binding != null ? this.binding.getFileObject() : null;
    }
    
    /**Return {@link PositionConverter} binding virtual Java source and the real source.
     * Please note that this method is needed only for clients that need to work
     * in non-Java files (eg. JSP files) or in dialogs, like code completion.
     * Most clients do not need to use {@link PositionConverter}.
     * 
     * @return PositionConverter binding the virtual Java source and the real source.
     * @since 0.21
     */
    public PositionConverter getPositionConverter() {
        return binding;
    }
    
    public Document getDocument() throws IOException {
        if (this.binding == null || this.binding.getFileObject() == null) {
            return null;
        }
        DataObject od = DataObject.find(this.binding.getFileObject());            
        EditorCookie ec = od.getCookie(EditorCookie.class);
        if (ec != null) {
            return  ec.getDocument();
        } else {
            return null;
        }
    }
    
    public synchronized TreeUtilities getTreeUtilities() {
        if (treeUtilities == null) {
            treeUtilities = new TreeUtilities(this);
        }
        return treeUtilities;
    }
    
    public synchronized ElementUtilities getElementUtilities() {
        if (elementUtilities == null) {
            elementUtilities = new ElementUtilities(this);

        }
        return elementUtilities;
    }
    
    /**Get the TypeUtilities.
     * 
     * @return an instance of TypeUtilities
     * 
     * @since 0.6
     */
    public synchronized TypeUtilities getTypeUtilities() {
        if (typeUtilities == null) {
            typeUtilities = new TypeUtilities(this);
        }
        return typeUtilities;
    }
    
    // Package private methods -------------------------------------------------
    
    void setPhase(final JavaSource.Phase phase) {
        assert phase != null;
        this.phase = phase;
    }
    
    void setCompilationUnit(final CompilationUnitTree compilationUnit) {
        assert compilationUnit != null;
        this.compilationUnit = compilationUnit;
    }        
    
    synchronized JavacTaskImpl getJavacTask() {	
        if (javacTask == null) {
            javacTask = javaSource.createJavacTask(new DiagnosticListenerImpl(errors));
        }
	return javacTask;
    }
    
    // Innerclasses ------------------------------------------------------------
    
    private static class DiagnosticListenerImpl implements DiagnosticListener {
        
        private final List<Diagnostic> errors;
        
        public DiagnosticListenerImpl(final List<Diagnostic> errors) {
            this.errors = errors;
        }
        
        public void report(Diagnostic message) {
            errors.add(message);
        }
    }
}
