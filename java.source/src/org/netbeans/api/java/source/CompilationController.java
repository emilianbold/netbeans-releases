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
import java.util.List;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.swing.text.Document;
import javax.tools.Diagnostic;
import org.netbeans.api.lexer.TokenHierarchy;
import static org.netbeans.api.java.source.JavaSource.Phase.*;
import org.openide.filesystems.FileObject;

/** Class for explicit invocation of compilation phases on a java source.
 *  The implementation delegates to the {@link CompilationInfo} to get the data,
 *  the access to {@link CompilationInfo} is not synchronized, so the class isn't
 *  reentrant.
 *  
 *  XXX: make toPhase automatic in getTrees(), Trees.getElement, etc....
 * @author Petr Hrebejk, Tomas Zezula
 */
public class CompilationController extends CompilationInfo {
    
    //Not private for unit tests
    /*private*/final CompilationInfo delegate;
    
    CompilationController(final CompilationInfo delegate) throws IOException {        
        super();
        assert delegate != null;
        this.delegate = delegate;
    }
        
    // API of the class --------------------------------------------------------
    
    /** Moves the state to required phase. If given state was already reached 
     * the state is not changed. The method will throw exception if a state is 
     * illegal required. Acceptable parameters for thid method are <BR>
     * <LI>{@link org.netbeans.api.java.source.JavaSource.Phase.PARSED}
     * <LI>{@link org.netbeans.api.java.source.JavaSource.Phase.ELEMENTS_RESOLVED}
     * <LI>{@link org.netbeans.api.java.source.JavaSource.Phase.RESOLVED}
     * <LI>{@link org.netbeans.api.java.source.JavaSource.Phase.UP_TO_DATE}   
     * @param phase The required phase
     * @return the reached state
     * @throws IllegalArgumentException in case that given state can not be 
     *         reached using this method
     * @throws IOException when the file cannot be red
     */    
    public JavaSource.Phase toPhase(JavaSource.Phase phase ) throws IOException {
        if (phase == MODIFIED) {
            throw new IllegalArgumentException( "Wrong phase" + phase );
        }
        if (delegate.jfo == null) {
            JavaSource.Phase currentPhase = delegate.getPhase();
            if (currentPhase.compareTo(phase)<0) {
                delegate.setPhase(phase);
            }
            return delegate.getPhase();
        }
        else {
            JavaSource.Phase currentPhase = JavaSource.moveToPhase(phase, this.delegate,false);
            return currentPhase.compareTo (phase) < 0 ? currentPhase : phase;
        }
    }            

    /**
     * Returns the current phase of the {@link JavaSource}.
     * 
     * @return {@link JavaSource.Phase} the state which was reached by the {@link JavaSource}.
     */
    @Override
    public JavaSource.Phase getPhase() {        
        return this.delegate.getPhase();
    }
        
    /**
     * Returns the javac tree representing the source file.
     *
     * @return {@link CompilationUnitTree} the compilation unit cantaining the top level classes contained in the
     * java source file
     */
    @Override
    public CompilationUnitTree getCompilationUnit() {
        return this.delegate.getCompilationUnit();
    }

    /**
     * Returns the content of the file represented by the {@link JavaSource}.
     * 
     * @return String the java source
     */
    @Override
    public String getText() {
        return this.delegate.getText();
    }

    /**@inheritDoc*/
    @Override
    public TokenHierarchy getTokenHiearchy() {
        return this.delegate.getTokenHiearchy();
    }
    
    /**
     * Returns the errors in the file represented by the {@link JavaSource}.
     * 
     * @return an list of {@link Diagnostic}
     */
    @Override
    public List<Diagnostic> getDiagnostics() {
        return this.delegate.getDiagnostics();
    }

    @Override
    public Trees getTrees() {
        return this.delegate.getTrees();
    }

    @Override
    public Types getTypes() {
        return this.delegate.getTypes();
    }
    
    @Override
    public Elements getElements() {
        return this.delegate.getElements();
    }
    
    @Override 
    public JavaSource getJavaSource() {
        return this.delegate.getJavaSource();
    }

    @Override 
    public ClasspathInfo getClasspathInfo() {
        return this.delegate.getClasspathInfo();
    }

    @Override
    public FileObject getFileObject() {
        return this.delegate.getFileObject();
    }

    @Override
    public Document getDocument() throws IOException {
        return this.delegate.getDocument();
    }
    
    @Override
    public synchronized TreeUtilities getTreeUtilities() {
        return this.delegate.getTreeUtilities();
    }
    
    @Override
    public synchronized ElementUtilities getElementUtilities() {
        return this.delegate.getElementUtilities();
    }
    
    @Override
    public synchronized CommentUtilities getCommentUtilities() {
        return this.delegate.getCommentUtilities();
    }
    
    // Package private methods -------------------------------------------------
    
    @Override 
    void setPhase(final JavaSource.Phase phase) {
        throw new UnsupportedOperationException ("CompilationController supports only read interface");          //NOI18N
    }   

    @Override 
    void setCompilationUnit(final CompilationUnitTree compilationUnit) {
        throw new UnsupportedOperationException ("CompilationController supports only read interface");          //NOI18N
    }

    @Override 
    JavacTaskImpl getJavacTask() {        
        return this.delegate.getJavacTask();
    }
}
