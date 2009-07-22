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

package org.netbeans.modules.java.source.parsing;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.util.JCDiagnostic;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.modules.java.source.JavaFileFilterQuery;
import org.netbeans.modules.java.source.usages.Pair;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Zezula
 */
public final class CompilationInfoImpl {
    
    private JavaSource.Phase phase = JavaSource.Phase.MODIFIED;
    private CompilationUnitTree compilationUnit;    
    
    private JavacTaskImpl javacTask;
    private final ClasspathInfo cpInfo;
    Pair<DocPositionRegion,MethodTree> changedMethod;
    private final FileObject file;
    private final FileObject root;
    final JavaFileObject jfo;    
    //@NotThreadSafe    //accessed under parser lock
    private Snapshot snapshot;
    private final JavacParser parser;
    private final boolean isClassFile;
    boolean needsRestart;
    JavaSource.Phase parserCrashed = JavaSource.Phase.UP_TO_DATE;      //When javac throws an error, the moveToPhase sets this to the last safe phase
    private final boolean clone;
        
    /**
     * Creates a new CompilationInfoImpl for given source file
     * @param parser used to parse the file
     * @param file to be parsed
     * @param root the owner of the parsed file
     * @param javacTask used javac or null if new one should be created
     * @param snapshot rendered content of the file
     * @throws java.io.IOException
     */
    CompilationInfoImpl (final JavacParser parser,
                         final FileObject file,
                         final FileObject root,
                         final JavacTaskImpl javacTask,
                         final Snapshot snapshot, 
                         final boolean clone) throws IOException {
        assert parser != null;
        this.parser = parser;
        this.cpInfo = parser.getClasspathInfo();
        assert cpInfo != null;
        this.file = file;
        this.root = root;
        this.snapshot = snapshot;
        assert file == null || snapshot != null;
        this.jfo = file != null ? JavacParser.jfoProvider.createJavaFileObject(file, root, JavaFileFilterQuery.getFilter(file), snapshot.getText()) : null;
        this.javacTask = javacTask;
        this.isClassFile = false;
        this.clone = clone;
    }
    
    /**
     * Creates a new CompilationInfoImpl for classpaths
     * @param cpInfo classpaths
     */
    CompilationInfoImpl (final ClasspathInfo cpInfo) {
        assert cpInfo != null;
        this.parser = null;
        this.file = null;
        this.root = null;
        this.jfo = null;
        this.snapshot = null;
        this.cpInfo = cpInfo;
        this.isClassFile = false;
        this.clone = false;
    }
    
    /**
     * Creates a new CompilationInfoImpl for a class file
     * @param cpInfo classpaths
     * @param file to be analyzed
     * @param root the owner of analyzed file
     */
    CompilationInfoImpl (final ClasspathInfo cpInfo,
                         final FileObject file,
                         final FileObject root) throws IOException {
        assert cpInfo != null;
        assert file != null;
        assert root != null;
        this.parser = null;
        this.file = file;
        this.root = root;
        this.jfo = FileObjects.nbFileObject(file, root);
        this.snapshot = null;
        this.cpInfo = cpInfo;
        this.isClassFile = true;
        this.clone = false;
    }
    
    void update (final Snapshot snapshot) throws IOException {
        assert snapshot != null;
        JavacParser.jfoProvider.update(this.jfo, snapshot.getText());
        this.snapshot = snapshot;
    }
    
    public Snapshot getSnapshot () {
        return this.snapshot;
    }
    
    /**
     * Returns the current phase of the {@link JavaSource}.
     * @return {@link JavaSource.Phase} the state which was reached by the {@link JavaSource}.
     */
    public JavaSource.Phase getPhase() {
        return this.phase;
    }
    
    public Pair<DocPositionRegion,MethodTree> getChangedTree () {
        return this.changedMethod;
    }
    
    /**
     * Returns the javac tree representing the source file.
     * @return {@link CompilationUnitTree} the compilation unit cantaining the top level classes contained in the,
     * java source file. 
     * @throws java.lang.IllegalStateException  when the phase is less than {@link JavaSource.Phase#PARSED}
     */
    public CompilationUnitTree getCompilationUnit() {
        if (this.jfo == null) {
            throw new IllegalStateException ();
        }
        if (this.phase.compareTo (JavaSource.Phase.PARSED) < 0)
            throw new IllegalStateException("Cannot call getCompilationInfo() if current phase < JavaSource.Phase.PARSED. You must call toPhase(Phase.PARSED) first.");//NOI18N
        return this.compilationUnit;
    }
    
    /**
     * Returns the content of the file represented by the {@link JavaSource}.
     * @return String the java source
     */
    public String getText() {
        if (!hasSource()) {
            throw new IllegalStateException ();
        }
        try {
            return this.jfo.getCharContent(false).toString();
        } catch (IOException ioe) {
            //Should never happen
            Exceptions.printStackTrace(ioe);
            return null;
        }
    }
    
    /**
     * Returns the {@link TokenHierarchy} for the file represented by the {@link JavaSource}.
     * @return lexer TokenHierarchy
     */
    public TokenHierarchy<?> getTokenHierarchy() {
        if (!hasSource()) {
            throw new IllegalStateException ();
        }
        try {
            return ((SourceFileObject) this.jfo).getTokenHierarchy();
        } catch (IOException ioe) {
            //Should never happen
            Exceptions.printStackTrace(ioe);
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
        Collection<Diagnostic> errors = ((DiagnosticListenerImpl) javacTask.getContext().get(DiagnosticListener.class)).errors.values();
        List<Diagnostic> partialReparseErrors = ((DiagnosticListenerImpl) javacTask.getContext().get(DiagnosticListener.class)).partialReparseErrors;
        List<Diagnostic> affectedErrors = ((DiagnosticListenerImpl) javacTask.getContext().get(DiagnosticListener.class)).affectedErrors;        
        List<Diagnostic> localErrors = new ArrayList<Diagnostic>(errors.size() + 
                (partialReparseErrors == null ? 0 : partialReparseErrors.size()) + 
                (affectedErrors == null ? 0 : affectedErrors.size()));
        localErrors.addAll(errors);
        if (partialReparseErrors != null) {
            localErrors.addAll (partialReparseErrors);
        }
        if (affectedErrors != null) {
            localErrors.addAll (affectedErrors);
        }
        return localErrors;
    }
    
                   
        
    /**
     * Returns {@link ClasspathInfo} for which this {@link CompilationInfoImpl} was created.
     * @return ClasspathInfo
     */
    public ClasspathInfo getClasspathInfo() {
	return this.cpInfo;
    }
    
    /**
     * Returns {@link JavacParser} which created this {@link CompilationInfoImpl}
     * or null when the {@link CompilationInfoImpl} was created for no files.
     * @return {@link JavacParser} or null
     */
    public JavacParser getParser () {
        return this.parser;
    }
    
    
    /**
     * Returns the {@link FileObject} represented by this {@link CompilationInfo}.
     * @return FileObject
     */
    public FileObject getFileObject () {
        return this.file;
    }
    
    public FileObject getRoot () {
        return this.root;
    }
    
    public boolean isClassFile () {
        return this.isClassFile;
    }
    
    /**
     * Returns {@link Document} of this {@link CompilationInfoImpl}
     * @return Document or null when the {@link DataObject} doesn't
     * exist or has no {@link EditorCookie}.
     * @throws java.io.IOException
     */
    public Document getDocument() {        
        if (this.file == null) {
            return null;
        }
        if (!this.file.isValid()) {
            return null;
        }
        try {
            DataObject od = DataObject.find(file);            
            EditorCookie ec = od.getCookie(EditorCookie.class);
            if (ec != null) {
                return  ec.getDocument();
            } else {
                return null;
            }
        } catch (DataObjectNotFoundException e) {
            //may happen when the underlying FileObject has just been deleted
            //should be safe to ignore
            Logger.getLogger(CompilationInfoImpl.class.getName()).log(Level.FINE, null, e);
            return null;
        }
    }
        
                                
    /**
     * Moves the state to required phase. If given state was already reached 
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
        if (phase == JavaSource.Phase.MODIFIED) {
            throw new IllegalArgumentException( "Invalid phase: " + phase );    //NOI18N
        }
        if (!hasSource()) {
            JavaSource.Phase currentPhase = getPhase();
            if (currentPhase.compareTo(phase)<0) {
                setPhase(phase);
                if (currentPhase == JavaSource.Phase.MODIFIED)
                    getJavacTask().parse(); // Ensure proper javac initialization
                currentPhase = phase;
            }
            return currentPhase;
        }
        else {
            JavaSource.Phase currentPhase = parser.moveToPhase(phase, this, false, false, clone);
            return currentPhase.compareTo (phase) < 0 ? currentPhase : phase;
        }
    }
    
    /**
     * Sets the current {@link JavaSource.Phase}
     * @param phase
     */
    void setPhase(final JavaSource.Phase phase) {
        assert phase != null;
        this.phase = phase;
    }
    
    /**
     * Sets changed method
     * @param changedMethod
     */
    void setChangedMethod (final Pair<DocPositionRegion,MethodTree> changedMethod) {
        this.changedMethod = changedMethod;
    }
    
    /**
     * Sets the {@link CompilationUnitTree}
     * @param compilationUnit
     */
    void setCompilationUnit(final CompilationUnitTree compilationUnit) {
        assert compilationUnit != null;
        this.compilationUnit = compilationUnit;
    }        
    
    /**
     * Returns {@link JavacTaskImpl}, when it doesn't exist
     * it's created.
     * @return JavacTaskImpl
     */
    public synchronized JavacTaskImpl getJavacTask() {	
        if (javacTask == null) {
            javacTask = JavacParser.createJavacTask(this.file, this.root, this.cpInfo,
                    clone ? null : this.parser, new DiagnosticListenerImpl(this.jfo), null);
        }
	return javacTask;
    }
    
    private boolean hasSource () {
        return this.jfo != null && !isClassFile;
    }
    
    
    // Innerclasses ------------------------------------------------------------    
    static class DiagnosticListenerImpl implements DiagnosticListener {
        
        private final TreeMap<Integer,Diagnostic> errors;
        private final JavaFileObject jfo;
        private volatile List<Diagnostic> partialReparseErrors;
        private volatile List<Diagnostic> affectedErrors;
        private volatile int currentDelta;
        
        public DiagnosticListenerImpl(final JavaFileObject jfo) {
            this.jfo = jfo;
            this.errors = new TreeMap<Integer,Diagnostic>();
        }
        
        public void report(Diagnostic message) {            
            if (this.jfo != null && this.jfo == message.getSource()) {
                if (partialReparseErrors != null) {
                    partialReparseErrors.add(message);
                }
                else {
                    errors.put((int)message.getPosition(),message);
                }
            }
        }
        
        final boolean hasPartialReparseErrors () {
            return this.partialReparseErrors != null && !this.partialReparseErrors.isEmpty();
        }
        
        final void startPartialReparse (int from, int to) {
            if (partialReparseErrors == null) {
                partialReparseErrors = new ArrayList<Diagnostic>();
                this.errors.subMap(from, to).clear();       //Remove errors in changed method durring the partial reparse                
                Map<Integer,Diagnostic> tail = this.errors.tailMap(to);
                this.affectedErrors = new ArrayList<Diagnostic>(tail.size());
                for (Iterator<Map.Entry<Integer,Diagnostic>> it = tail.entrySet().iterator(); it.hasNext();) {
                    Map.Entry<Integer,Diagnostic> e = it.next();
                    it.remove();
                    this.affectedErrors.add(new D ((JCDiagnostic)e.getValue()));
                }
            }
            else {
                this.partialReparseErrors.clear();
            }
        }
        
        final void endPartialReparse (final int delta) {
            this.currentDelta+=delta;
        }        
        
        private final class D implements Diagnostic {
            
            private final JCDiagnostic delegate;
            
            public D (final JCDiagnostic delegate) {
                assert delegate != null;
                this.delegate = delegate;
            }

            public Kind getKind() {
                return this.delegate.getKind();
            }

            public Object getSource() {
                return this.delegate.getSource();
            }

            public long getPosition() {
                long ret = this.delegate.getPosition();
                if (delegate.hasFixedPositions()) {
                    ret+=currentDelta;
                }
                return ret;
            }

            public long getStartPosition() {
                long ret = this.delegate.getStartPosition();
                if (delegate.hasFixedPositions()) {
                    ret+=currentDelta;
                }
                return ret;
            }

            public long getEndPosition() {
                long ret = this.delegate.getEndPosition();
                if (delegate.hasFixedPositions()) {
                    ret+=currentDelta;
                }
                return ret;
            }

            public long getLineNumber() {
                return -1;
            }

            public long getColumnNumber() {
                return -1;
            }

            public String getCode() {
                return this.delegate.getCode();
            }

            public String getMessage(Locale locale) {
                return this.delegate.getMessage(locale);
            }
            
        }
    }
}
