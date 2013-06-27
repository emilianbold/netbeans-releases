/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
import com.sun.tools.javac.api.ClientCodeWrapper.Trusted;
import com.sun.tools.javac.api.DiagnosticFormatter;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.util.JCDiagnostic;
import com.sun.tools.javac.util.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.Document;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.CompilationInfo.CacheClearPolicy;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.modules.java.source.JavaFileFilterQuery;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.Pair;

/**
 *
 * @author Tomas Zezula
 */
public final class CompilationInfoImpl {

    private JavaSource.Phase phase = JavaSource.Phase.MODIFIED;
    private CompilationUnitTree compilationUnit;

    private JavacTaskImpl javacTask;
    private DiagnosticListener<JavaFileObject> diagnosticListener;
    private final ClasspathInfo cpInfo;
    private Pair<DocPositionRegion,MethodTree> changedMethod;
    private final FileObject file;
    private final FileObject root;
    final JavaFileObject jfo;
    //@NotThreadSafe    //accessed under parser lock
    private Snapshot snapshot;
    private final JavacParser parser;
    private final boolean isClassFile;
    private final boolean isDetached;
    JavaSource.Phase parserCrashed = JavaSource.Phase.UP_TO_DATE;      //When javac throws an error, the moveToPhase sets this to the last safe phase
    private final Map<CacheClearPolicy, Map<Object, Object>> userCache = new EnumMap<CacheClearPolicy, Map<Object, Object>>(CacheClearPolicy.class);

    /**
     * Creates a new CompilationInfoImpl for given source file
     * @param parser used to parse the file
     * @param file to be parsed
     * @param root the owner of the parsed file
     * @param javacTask used javac or null if new one should be created
     * @param snapshot rendered content of the file
     * @param detached true if the CompilationInfoImpl is detached from parsing infrastructure.
     * @throws java.io.IOException
     */
    CompilationInfoImpl (final JavacParser parser,
                         final FileObject file,
                         final FileObject root,
                         final JavacTaskImpl javacTask,
                         final DiagnosticListener<JavaFileObject> diagnosticListener,
                         final Snapshot snapshot,
                         final boolean detached) throws IOException {
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
        this.diagnosticListener = diagnosticListener;
        this.isClassFile = false;
        this.isDetached = detached;
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
        this.isDetached = false;
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
        this.isDetached = false;
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
            throw new IllegalStateException("Cannot call getCompilationUnit() if current phase < JavaSource.Phase.PARSED. You must call toPhase(Phase.PARSED) first.");//NOI18N
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
        Collection<Collection<Diagnostic<? extends JavaFileObject>>> errors = ((DiagnosticListenerImpl)diagnosticListener).getErrors(jfo).values();
        List<Diagnostic<? extends JavaFileObject>> partialReparseErrors = ((DiagnosticListenerImpl)diagnosticListener).partialReparseErrors;
        List<Diagnostic<? extends JavaFileObject>> affectedErrors = ((DiagnosticListenerImpl)diagnosticListener).affectedErrors;
        int errorsSize = 0;

        for (Collection<Diagnostic<? extends JavaFileObject>> err : errors) {
            errorsSize += err.size();
        }

        List<Diagnostic> localErrors = new ArrayList<Diagnostic>(errorsSize +
                (partialReparseErrors == null ? 0 : partialReparseErrors.size()) + 
                (affectedErrors == null ? 0 : affectedErrors.size()));
        DiagnosticFormatter<JCDiagnostic> formatter = Log.instance(javacTask.getContext()).getDiagnosticFormatter();
        
        for (Collection<Diagnostic<? extends JavaFileObject>> err : errors) {
            for (Diagnostic<? extends JavaFileObject> d : err) {
                localErrors.add(RichDiagnostic.wrap(d, formatter));
            }
        }
        if (partialReparseErrors != null) {
            for (Diagnostic<? extends JavaFileObject> d : partialReparseErrors) {
                localErrors.add(RichDiagnostic.wrap(d, formatter));
            }
        }
        if (affectedErrors != null) {
            for (Diagnostic<? extends JavaFileObject> d : affectedErrors) {
                localErrors.add(RichDiagnostic.wrap(d, formatter));
            }
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
            JavaSource.Phase currentPhase = parser.moveToPhase(phase, this, false);
            return currentPhase.compareTo (phase) < 0 ? currentPhase : phase;
        }
    }

    /**
     * Returns {@link JavacTaskImpl}, when it doesn't exist
     * it's created.
     * @return JavacTaskImpl
     */
    public synchronized JavacTaskImpl getJavacTask() {
        if (javacTask == null) {
            diagnosticListener = new DiagnosticListenerImpl(this.jfo);
            javacTask = JavacParser.createJavacTask(this.file, this.root, this.cpInfo,
                    this.parser, diagnosticListener, null, isDetached);
        }
	return javacTask;
    }

    public Object getCachedValue(Object key) {
        for (Map<Object, Object> c : userCache.values()) {
            Object res = c.get(key);

            if (res != null) return res;
        }

        return null;
    }

    public void putCachedValue(Object key, Object value, CacheClearPolicy clearPolicy) {
        for (Map<Object, Object> c : userCache.values()) {
            c.remove(key);
        }

        Map<Object, Object> c = userCache.get(clearPolicy);

        if (c == null) {
            userCache.put(clearPolicy, c = new HashMap<Object, Object>());
        }

        c.put(key, value);
    }

    public void taskFinished() {
        userCache.remove(CacheClearPolicy.ON_TASK_END);
    }

    public void dispose() {
        userCache.clear();
    }
    
    /**
     * Returns current {@link DiagnosticListener}
     * @return listener
     */
    DiagnosticListener<JavaFileObject> getDiagnosticListener() {
        return diagnosticListener;
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
        userCache.remove(CacheClearPolicy.ON_TASK_END);
        userCache.remove(CacheClearPolicy.ON_CHANGE);
    }
    
    /**
     * Sets the {@link CompilationUnitTree}
     * @param compilationUnit
     */
    void setCompilationUnit(final CompilationUnitTree compilationUnit) {
        assert compilationUnit != null;
        this.compilationUnit = compilationUnit;
    }
                
    private boolean hasSource () {
        return this.jfo != null && !isClassFile;
    }
    
    
    // Innerclasses ------------------------------------------------------------
    @Trusted
    static class DiagnosticListenerImpl implements DiagnosticListener<JavaFileObject> {
        
        private final Map<JavaFileObject, TreeMap<Integer, Collection<Diagnostic<? extends JavaFileObject>>>> source2Errors;
        private final JavaFileObject jfo;
        private volatile List<Diagnostic<? extends JavaFileObject>> partialReparseErrors;
        private volatile List<Diagnostic<? extends JavaFileObject>> affectedErrors;
        private volatile int currentDelta;
        
        public DiagnosticListenerImpl(final JavaFileObject jfo) {
            this.jfo = jfo;
            this.source2Errors = new HashMap<JavaFileObject, TreeMap<Integer, Collection<Diagnostic<? extends JavaFileObject>>>>();
        }
        
        @Override
        public void report(Diagnostic<? extends JavaFileObject> message) {
            if (partialReparseErrors != null) {
                if (this.jfo != null && this.jfo == message.getSource()) {
                    partialReparseErrors.add(message);
                }
            } else {
                TreeMap<Integer, Collection<Diagnostic<? extends JavaFileObject>>> errors = getErrors(message.getSource());
                Collection<Diagnostic<? extends JavaFileObject>> diags = errors.get((int) message.getPosition());

                if (diags == null) {
                    errors.put((int) message.getPosition(), diags = new ArrayList<Diagnostic<? extends JavaFileObject>>());
                }
                
                diags.add(message);
            }
        }

        private TreeMap<Integer, Collection<Diagnostic<? extends JavaFileObject>>> getErrors(JavaFileObject file) {
                TreeMap<Integer, Collection<Diagnostic<? extends JavaFileObject>>> errors = source2Errors.get(file);

                if (errors == null) {
                    source2Errors.put(file, errors = new TreeMap<Integer, Collection<Diagnostic<? extends JavaFileObject>>>());
                }

                return errors;
        }
        
        final boolean hasPartialReparseErrors () {
            return this.partialReparseErrors != null && !this.partialReparseErrors.isEmpty();
        }
        
        final void startPartialReparse (int from, int to) {
            if (partialReparseErrors == null) {
                partialReparseErrors = new ArrayList<Diagnostic<? extends JavaFileObject>>();
                TreeMap<Integer, Collection<Diagnostic<? extends JavaFileObject>>> errors = getErrors(jfo);
                errors.subMap(from, to).clear();       //Remove errors in changed method durring the partial reparse
                Map<Integer, Collection<Diagnostic<? extends JavaFileObject>>> tail = errors.tailMap(to);
                this.affectedErrors = new ArrayList<Diagnostic<? extends JavaFileObject>>(tail.size());
                for (Iterator<Entry<Integer,Collection<Diagnostic<? extends JavaFileObject>>>> it = tail.entrySet().iterator(); it.hasNext();) {
                    Entry<Integer, Collection<Diagnostic<? extends JavaFileObject>>> e = it.next();
                    for (Diagnostic<? extends JavaFileObject> d : e.getValue()) {
                        final JCDiagnostic diagnostic = (JCDiagnostic) d;
                        if (diagnostic == null) {
                            throw new IllegalStateException("#184910: diagnostic == null " + mapArraysToLists(Thread.getAllStackTraces())); //NOI18N
                        }
                        this.affectedErrors.add(new D (diagnostic));
                    }
                    it.remove();
                }
            }
            else {
                this.partialReparseErrors.clear();
            }
        }
        
        final void endPartialReparse (final int delta) {
            this.currentDelta+=delta;
        }
        
        private static <A,B> Map<A,List<B>> mapArraysToLists (final Map<? extends A, B[]> map) {
            final Map<A,List<B>> result = new HashMap<A, List<B>>();
            for (Map.Entry<? extends A,B[]> entry : map.entrySet()) {
                result.put(entry.getKey(), Arrays.asList(entry.getValue()));
            }
            return result;
        } 
        
        private final class D implements Diagnostic {
            
            private final JCDiagnostic delegate;
            
            public D (final JCDiagnostic delegate) {
                assert delegate != null;
                this.delegate = delegate;
            }

            @Override
            public Kind getKind() {
                return this.delegate.getKind();
            }

            @Override
            public Object getSource() {
                return this.delegate.getSource();
            }

            @Override
            public long getPosition() {
                long ret = this.delegate.getPosition();
                if (delegate.hasFixedPositions()) {
                    ret+=currentDelta;
                }
                return ret;
            }

            @Override
            public long getStartPosition() {
                long ret = this.delegate.getStartPosition();
                if (delegate.hasFixedPositions()) {
                    ret+=currentDelta;
                }
                return ret;
            }

            @Override
            public long getEndPosition() {
                long ret = this.delegate.getEndPosition();
                if (delegate.hasFixedPositions()) {
                    ret+=currentDelta;
                }
                return ret;
            }

            @Override
            public long getLineNumber() {
                return -1;
            }

            @Override
            public long getColumnNumber() {
                return -1;
            }

            @Override
            public String getCode() {
                return this.delegate.getCode();
            }

            @Override
            public String getMessage(Locale locale) {
                return this.delegate.getMessage(locale);
            }
            
        }
    }

    private static final class RichDiagnostic implements Diagnostic {

        private final JCDiagnostic delegate;
        private final DiagnosticFormatter<JCDiagnostic> formatter;

        public RichDiagnostic(JCDiagnostic delegate, DiagnosticFormatter<JCDiagnostic> formatter) {
            this.delegate = delegate;
            this.formatter = formatter;
        }

        @Override
        public Kind getKind() {
            return delegate.getKind();
        }

        @Override
        public Object getSource() {
            return delegate.getSource();
        }

        @Override
        public long getPosition() {
            return delegate.getPosition();
        }

        @Override
        public long getStartPosition() {
            return delegate.getStartPosition();
        }

        @Override
        public long getEndPosition() {
            return delegate.getEndPosition();
        }

        @Override
        public long getLineNumber() {
            return delegate.getLineNumber();
        }

        @Override
        public long getColumnNumber() {
            return delegate.getColumnNumber();
        }

        @Override
        public String getCode() {
            return delegate.getCode();
        }

        @Override
        public String getMessage(Locale locale) {
            return formatter.format(delegate, locale);
        }

        @Override
        public String toString() {
            return delegate.toString();
        }

        public static Diagnostic wrap(Diagnostic d, DiagnosticFormatter<JCDiagnostic> df) {
            if (d instanceof JCDiagnostic) {
                return new RichDiagnostic((JCDiagnostic) d, df);
            } else {
                return d;
            }
        }
    }
}
