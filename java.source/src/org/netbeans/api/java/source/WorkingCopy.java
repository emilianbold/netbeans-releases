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
import com.sun.source.tree.Tree.Kind;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position.Bias;
import javax.tools.JavaFileObject;
import org.netbeans.api.java.source.ModificationResult.Difference;
import org.netbeans.modules.java.source.transform.ChangeSet;
import org.netbeans.modules.java.source.query.QueryException;
import org.netbeans.modules.java.source.transform.Transformer;
import org.netbeans.modules.java.source.engine.RootTree;
import org.netbeans.modules.java.source.builder.ASTService;
import org.netbeans.modules.java.source.builder.TreeFactory;
import org.netbeans.modules.java.source.engine.ApplicationContext;
import org.netbeans.modules.java.source.engine.ReattributionException;
import org.netbeans.modules.java.source.engine.SourceReader;
import org.netbeans.modules.java.source.engine.SourceRewriter;
import org.netbeans.modules.java.source.pretty.VeryPretty;
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
    
    private ChangeSet changes;
    private boolean afterCommit = false;
    private WorkingCopyContext wcc;
    private TreeMaker treeMaker;
    
    WorkingCopy(final CompilationInfo delegate) throws IOException {        
        super(delegate);
        wcc = new WorkingCopyContext();
    }

    private synchronized void init() throws ReattributionException {
        if (changes != null) //already initialized
            return;
        final CompilationUnitTree tree = getCompilationUnit();
        if (tree != null) {
            Context context = getContext();
            ASTService model = ASTService.instance(context);
            List<CompilationUnitTree> units = new ArrayList<CompilationUnitTree>();
            units.add(tree);
            model.setRoot(TreeFactory.instance(context).Root(units));
        }
        
        JavacTaskImpl task = this.delegate.getJavacTask();
        treeMaker = new TreeMaker(this, TreeFactory.instance(getContext()));
        changes = new ChangeSet("<no-description>");
        changes.attach(getContext());
    }
    
    private Context getContext() {
        return getJavacTask().getContext();
    }
    
    // API of the class --------------------------------------------------------

    ApplicationContext getCommandEnvironment() {
        return wcc;
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
    
    public synchronized TreeMaker getTreeMaker() {
        if (treeMaker == null)
            throw new IllegalStateException("Cannot call getTreeMaker before toPhase.");
        return treeMaker;
    }
    
    void run(Transformer t) {
        if (afterCommit)
            throw new IllegalStateException ("The run method can't be called on a WorkingCopy instance after the commit");   //NOI18N
        t.init();
        t.attach(getContext());
        t.apply();
        t.release();
        t.destroy();
    }
    
    void run(Transformer t, Tree tree) {
        if (afterCommit)
            throw new IllegalStateException ("The run method can't be called on a WorkingCopy instance after the commit");   //NOI18N
        t.init();
        t.attach(getContext());
        t.apply(tree);
        t.release();
        t.destroy();
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
        
        if (oldTree == null && Kind.COMPILATION_UNIT == newTree.getKind()) {
            // todo (#pf): hacky stuff - has to be moved to commit() call
            createCompilationUnit((JCTree.JCCompilationUnit) newTree);
            return;
        }
        if (oldTree == null || newTree == null)
            throw new IllegalArgumentException("Null values are not allowed.");
        
        changes.rewrite(oldTree, newTree);
    }
              
    // Package private methods -------------------------------------------------        
    
    List<Difference> getChanges() throws IOException {
        if (afterCommit)
            throw new IllegalStateException("The commit method can be called only once on a WorkingCopy instance");   //NOI18N
        afterCommit = true;
        
        if (changes == null) {
            //may happen when the modification task does not call toPhase at all.
            return null;
        }
        
        try {
            RootTree newRoot = changes.commit((RootTree) ASTService.instance(getContext()).getRoot());
            
            if (changes.hasChanges()) {
                ASTService.instance(getContext()).setRoot(newRoot);
            }
            
            Commit save = new Commit(this, wcc.getSourceRewriter(null));
            save.init();
            save.attach(getContext());
            save.commit();
            save.release();
            save.destroy();
            return wcc.diffs;
        } catch (QueryException qe) {
            Logger.getLogger("global").log(Level.WARNING, qe.getMessage(), qe);
            return null;
        } catch (ReattributionException qe) {
            Logger.getLogger("global").log(Level.WARNING, qe.getMessage(), qe);
            return null;
        }
    }
    
    private void createCompilationUnit(JCTree.JCCompilationUnit unitTree) {
            VeryPretty printer = new VeryPretty(getContext());
            CompilationUnitTree cut = unitTree;
            printer.print(unitTree);
            Writer w = null;
            try {
                cut.getSourceFile().openOutputStream();
                 w = cut.getSourceFile().openWriter();
                w.append(printer.toString());
            } catch (IOException e) {
                Logger.getLogger(WorkingCopy.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            } finally {
                if (w != null) {
                    try {
                        w.close();
                    } catch (IOException e) {
                        Logger.getLogger(WorkingCopy.class.getName()).log(Level.SEVERE, e.getMessage(), e);
                    }
                }
            }
            return;
    }
            
    // Innerclasses ------------------------------------------------------------

    private class WorkingCopyContext implements ApplicationContext {
        
        private ArrayList<Difference> diffs = new ArrayList<Difference>();

        public PrintWriter getOutputWriter(String title) {
            // Sink any log output so it isn't displayed.
            return new PrintWriter(new Writer() {
                public void write(char[] cbuf, int off, int len) throws IOException {}
                public void flush() throws IOException {}
                public void close() throws IOException {}
            }, true);
        }

        private Map<Integer, String> userInfo = Collections.<Integer, String>emptyMap();
       
        @SuppressWarnings("unchecked")
        public void setResult(Object result, String title) {
            if ("user-info".equals(title)) {
                userInfo = Map.class.cast(result);
            }
        }

        public SourceRewriter getSourceRewriter(JavaFileObject sourcefile) throws IOException {
            return new Rewriter();
        }
    
        public void setStatusMessage(String message) {
            System.out.println(message);
        }

        public void setErrorMessage(String message, String title) {
            setStatusMessage(title + ": " + message);
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
                    diffs.add(new Difference(Difference.Kind.INSERT, ces.createPositionRef(offset, Bias.Forward), ces.createPositionRef(offset, Bias.Forward), null, s, userInfo.get(offset)));
                }
            }
            
            public void skipThrough(SourceReader in, int pos) throws IOException, BadLocationException {
                char[] buf = in.getCharsTo(pos);
                Difference diff = diffs.size() > 0 ? diffs.get(diffs.size() - 1) : null;
                if (diff != null && diff.getKind() == Difference.Kind.INSERT && diff.getStartPosition().getOffset() == offset) {
                    diff.kind = Difference.Kind.CHANGE;
                    diff.oldText = new String(buf);
                } else {
                    diffs.add(new Difference(Difference.Kind.REMOVE, ces.createPositionRef(offset, Bias.Forward), ces.createPositionRef(offset + buf.length, Bias.Forward), new String(buf), null, userInfo.get(offset)));
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
