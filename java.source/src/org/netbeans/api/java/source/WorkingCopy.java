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
import com.sun.source.tree.Tree;
import com.sun.source.util.Trees;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.code.Source;
import com.sun.tools.javac.util.Context;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position.Bias;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import org.netbeans.api.java.source.ModificationResult.Difference;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.jackpot.model.ChangeSet;
import org.netbeans.jackpot.engine.*;
import org.netbeans.jackpot.query.QueryException;
import org.netbeans.jackpot.transform.Transformer;
import org.netbeans.jackpot.tree.RootTree;
import org.netbeans.modules.java.source.builder.UndoListService;
import org.netbeans.modules.java.source.builder.ASTService;
import org.netbeans.modules.java.source.builder.DefaultEnvironment;
import org.netbeans.modules.java.source.builder.TreeFactory;
import org.netbeans.modules.java.source.save.Commit;

import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;

/**XXX: extends CompilationController now, finish method delegation
 *
 * @author Dusan Balek, Petr Hrebejk
 */
public class WorkingCopy extends CompilationController {
    
    private CompilationInfo delegate;
    private CommandEnvironment ce;
    private ChangeSet changes;
    private boolean afterCommit = false;
    private WorkingCopyContext wcc;
    private TreeMaker treeMaker;
    
    WorkingCopy(final CompilationInfo delegate) throws IOException {        
        super(delegate);
        assert delegate != null;
        this.delegate = delegate;
        wcc = new WorkingCopyContext();
    }

    private synchronized void init() throws ReattributionException {
        final CompilationUnitTree tree = getCompilationUnit();
        if (tree != null) {
            Context context = getContext();
            ASTService model = ASTService.instance(context);
            model.setRoot(TreeFactory.instance(context).Root(new ArrayList<CompilationUnitTree>()));
            UndoListService.instance(context).reset();
            model.addSourceTree(tree);
        }
        
        JavacTaskImpl task = this.delegate.getJavacTask();
        ce = new DefaultEnvironment(
                task, getCompilationUnit(), Source.instance(task.getContext()).name, wcc);
        this.treeMaker = new TreeMaker(this, ce.getTreeMaker());
        this.changes = new ChangeSet(ce, "<no-description>");
    }
    
    private Context getContext() {
        return getJavacTask().getContext();
    }
    
    // API of the class --------------------------------------------------------

    CommandEnvironment getCommandEnvironment() {
        return ce;
    }

    @Override
    public JavaSource.Phase toPhase(JavaSource.Phase phase) throws IOException {
        JavaSource.Phase result = super.toPhase(phase);
        
        if (result.compareTo(JavaSource.Phase.PARSED) >= 0) {
            try {
                init();
            } catch (ReattributionException ex) {
                IOException ioe = new IOException();
                ioe.initCause(ex);
                throw ioe;
            }
        }
        
        return result;
    }
    
    @Override
    public TreeUtilities getTreeUtilities() {
        return this.delegate.getTreeUtilities();
    }
    
    @Override
    public ElementUtilities getElementUtilities() {
        return this.delegate.getElementUtilities();
    }
    
    public synchronized TreeMaker getTreeMaker() {
        if (treeMaker == null)
            throw new IllegalStateException("Cannot call getTreeMaker before toPhase.");
        return treeMaker;
    }
    
    void run(Transformer t) {
        if (afterCommit)
            throw new IllegalStateException ("The run method can't be called on a WorkingCopy instance after the commit");   //NOI18N
        t.attachTo(ce);
        t.apply();
    }
    
    void run(Transformer t, Tree tree) {
        if (afterCommit)
            throw new IllegalStateException ("The run method can't be called on a WorkingCopy instance after the commit");   //NOI18N
        t.attachTo(ce);
        t.apply(tree);
    }
    
    ChangeSet getChangeSet() {
        return changes;
    }
    
    /**
     * Replaces the original tree <code>oldTree</code> with the new one -
     * <code>newTree</code>. <code>null</code> values are not allowed.
     * Use methods in {@link TreeMaker} for tree element removal.
     * 
     * @param oldTree  tree to be replaced, use tree already represented in
     *                 source code.
     * @param newTree  new tree, either created by <code>TreeMaker</code>
     *                 or obtained from different place.
     * @throws IllegalStateException if <code>toPhase()</code> method was not
     *         called before.
     * @throws IllegalArgumentException when <code>null</code> was passed to the 
     *         method.
     * @see TreeMaker
     */
    public synchronized void rewrite(Tree oldTree, Tree newTree) {
        if (changes == null)
            throw new IllegalStateException("Cannot call rewrite before toPhase.");
        if (oldTree == null || newTree == null)
            throw new IllegalArgumentException("Null values are not allowed.");
        
        changes.rewrite(oldTree, newTree);
    }
    
    /**
     * Returns the current phase of the {@link JavaSource}.
     * @return {@link JavaSource.Phase} the state which was reached by the {@link JavaSource}.
     */
    @Override
    public JavaSource.Phase getPhase() {
        return this.delegate.getPhase();
    }
    
    /**
     * Returns the javac tree representing the source file.
     * @return {@link CompilationUnitTree} the compilation unit cantaining the top level classes contained in the
     * java source file
     */
    @Override
    public CompilationUnitTree getCompilationUnit() {
        return this.delegate.getCompilationUnit();
    }
    
    /**
     * Returns the content of the file represented by the {@link JavaSource}.
     * @return String the java source
     */
    @Override
    public String getText() {
        return this.delegate.getText();
    }

    /**@inheritDoc*/
    @Override
    public TokenHierarchy getTokenHierarchy() {
        return this.delegate.getTokenHierarchy();
    }
    
    /**
     * Returns the errors in the file represented by the {@link JavaSource}.
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
    
    // Package private methods -------------------------------------------------
    
    @Override 
    void setPhase(final JavaSource.Phase phase) {
        throw new UnsupportedOperationException ("WorkingCopy supports only read interface");          //NOI18N
    }

    @Override 
    void setCompilationUnit(final CompilationUnitTree compilationUnit) {
        throw new UnsupportedOperationException ("WorkingCopy supports only read interface");          //NOI18N
    }

    @Override
    JavacTaskImpl getJavacTask() {
        return this.delegate.getJavacTask();
    }
    
    List<Difference> getChanges() throws IOException {
        if (afterCommit)
            throw new IllegalStateException("The commit method can be called only once on a WorkingCopy instance");   //NOI18N
        afterCommit = true;
        try {
            RootTree newRoot = changes.commit(ce.getRootNode());
            
            if (changes.hasChanges()) {
                getCommandEnvironment().getModel().setRoot(newRoot);
            }
            
            Commit save = new Commit(this);
            save.attachTo(ce);
            save.commit();
            return wcc.diffs;
        } catch (QueryException qe) {
            Logger.getLogger("global").log(Level.WARNING, qe.getMessage(), qe);
            return null;
        } catch (ReattributionException qe) {
            Logger.getLogger("global").log(Level.WARNING, qe.getMessage(), qe);
            return null;
        }
    }
    
    // Innerclasses ------------------------------------------------------------

    private class WorkingCopyContext extends DefaultApplicationContext {
        
        private ArrayList<Difference> diffs = new ArrayList<Difference>();

        public PrintWriter getOutputWriter(String title) {
            // Sink any log output so it isn't displayed.
            return new PrintWriter(new Writer() {
                public void write(char[] cbuf, int off, int len) throws IOException {}
                public void flush() throws IOException {}
                public void close() throws IOException {}
            }, true);
        }
        
        public SourceRewriter getSourceRewriter(JavaFileObject sourcefile) throws IOException {
            return new Rewriter();
        }
        
        private class Rewriter implements SourceRewriter {
            
            private int offset = 0;
            private CloneableEditorSupport ces;
            
            private Rewriter() throws IOException {
                FileObject fo = getFileObject();
                if (fo != null) {
                    DataObject dObj = DataObject.find(fo);
                    ces = dObj != null ? (CloneableEditorSupport)dObj.getCookie(EditorCookie.class) : null;
                }
                if (ces == null)
                    throw new IOException("Could not find CloneableEditorSupport for " + FileUtil.getFileDisplayName (fo)); //NOI18N
            }
            
            public void writeTo(String s) throws IOException, BadLocationException {                
                Difference diff = diffs.size() > 0 ? diffs.get(diffs.size() - 1) : null;
                if (diff != null && diff.getKind() == Difference.Kind.REMOVE && diff.getEndPosition().getOffset() == offset) {
                    diff.kind = Difference.Kind.CHANGE;
                    diff.newText = s;
                } else {
                    diffs.add(new Difference(Difference.Kind.INSERT, ces.createPositionRef(offset, Bias.Forward), ces.createPositionRef(offset, Bias.Forward), null, s));
                }
            }
            
            public void skipThrough(SourceReader in, int pos) throws IOException, BadLocationException {
                char[] buf = in.getCharsTo(pos);
                Difference diff = diffs.size() > 0 ? diffs.get(diffs.size() - 1) : null;
                if (diff != null && diff.getKind() == Difference.Kind.INSERT && diff.getStartPosition().getOffset() == offset) {
                    diff.kind = Difference.Kind.CHANGE;
                    diff.oldText = new String(buf);
                } else {
                    diffs.add(new Difference(Difference.Kind.REMOVE, ces.createPositionRef(offset, Bias.Forward), ces.createPositionRef(offset + buf.length, Bias.Forward), new String(buf), null));
                }
                offset += buf.length;
            }
            
            public void copyTo(SourceReader in, int pos) throws IOException {
                char[] buf = in.getCharsTo(pos);
                offset += buf.length;
            }
            
            public void copyRest(SourceReader in) throws IOException {
            }
            
            public void close(boolean save) {
            }
        }
    }
}
