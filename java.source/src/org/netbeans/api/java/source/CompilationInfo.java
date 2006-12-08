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

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.Trees;
import com.sun.tools.javac.api.JavacTaskImpl;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.swing.text.Document;
import javax.tools.DiagnosticListener;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.modules.java.source.parsing.SourceFileObject;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
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
    private FileObject fo;
    final JavaFileObject jfo;    
    final JavaSource javaSource;        
    boolean needsRestart;
    
    private ElementUtilities elementUtilities;
    private TreeUtilities treeUtilities;
    private CommentUtilities commentUtilities;
    
    CompilationInfo () {
        this.javaSource = null;
        this.jfo = null;
        this.javacTask = null;
        this.errors = null;
    }
    
    CompilationInfo ( final JavaSource javaSource, final FileObject fo, final JavaFileFilterImplementation filter, final JavacTaskImpl javacTask) throws IOException {
        assert javaSource != null;        
        this.javaSource = javaSource;
        this.fo = fo;
        this.jfo = fo != null ? javaSource.jfoProvider.createJavaFileObject(fo, filter) : null;
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
     * @return {@link CompilationUnitTree} the compilation unit cantaining the top level classes contained in the
     * java source file
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
    
    public TokenHierarchy getTokenHiearchy() {
        if (this.jfo == null) {
            throw new IllegalStateException ();
        }
        try {
            return ((SourceFileObject) this.jfo).getTokenHiearchy();
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
        ArrayList<Diagnostic> localErrors = new ArrayList<Diagnostic>(errors.size());
        for(Diagnostic m : errors) {
            if (this.jfo == m.getSource())
                localErrors.add(m);
        }
        return localErrors;
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
        return fo;
    }
    
    public Document getDocument() throws IOException {
        if (this.fo == null) {
            return null;
        }
        DataObject od = DataObject.find(fo);            
        EditorCookie ec = (EditorCookie) od.getCookie(EditorCookie.class);        
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
    
    public synchronized CommentUtilities getCommentUtilities() {
        if (commentUtilities == null) {
            commentUtilities = new CommentUtilities(this);
        }
        return commentUtilities;
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
