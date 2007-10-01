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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position.Bias;
import javax.tools.JavaFileObject;
import static org.netbeans.api.java.source.ModificationResult.*;
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
    private Map<JavaFileObject, CompilationUnitTree> externalChanges;
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
        
        treeMaker = new TreeMaker(this, TreeFactory.instance(getContext()));
        changes = new ChangeSet();
        externalChanges = null;
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
            RootTree root = (RootTree) ASTService.instance(getContext()).getRoot();
            List<CompilationUnitTree> cuts = null;
            if (externalChanges != null) {
                cuts = new ArrayList<CompilationUnitTree>(root.getCompilationUnits());
                cuts.addAll(externalChanges.values());
                root = new RootTree(cuts);
            }
            root = changes.commit(root);
            if (changes.hasChanges()) {
                ASTService.instance(getContext()).setRoot(root);
            }
            cuts = root.getCompilationUnits();
            Commit save = new Commit(this, wcc.getSourceRewriter());
            save.attach(getContext());
            save.commit();
            save.release();
            if (externalChanges == null) {
                return wcc.diffs;
            } else {
                List<Difference> diffs = wcc.diffs;
                for (CompilationUnitTree unitTree : cuts) {
                    if (externalChanges.containsKey(unitTree.getSourceFile())) {
                        VeryPretty printer = new VeryPretty(getContext());
                        printer.print((JCTree.JCCompilationUnit) unitTree);
                        diffs.add(new CreateChange(unitTree.getSourceFile(), printer.toString()));
                    }
                }
                return diffs;
            }
        } catch (QueryException qe) {
            Logger.getLogger(WorkingCopy.class.getName()).log(Level.WARNING, qe.getMessage(), qe);
            return null;
        } catch (ReattributionException qe) {
            Logger.getLogger(WorkingCopy.class.getName()).log(Level.WARNING, qe.getMessage(), qe);
            return null;
        }
    }
    
    private void createCompilationUnit(JCTree.JCCompilationUnit unitTree) {
        if (externalChanges == null) externalChanges = new HashMap<JavaFileObject, CompilationUnitTree>();
        externalChanges.put(unitTree.getSourceFile(), unitTree);
        return;
    }
    
    // Innerclasses ------------------------------------------------------------
    private class WorkingCopyContext implements ApplicationContext {
        
        private ArrayList<Difference> diffs = new ArrayList<Difference>();

        private Map<Integer, String> userInfo = Collections.<Integer, String>emptyMap();
       
        @SuppressWarnings("unchecked")
        public void setResult(Object result, String title) {
            if ("user-info".equals(title)) {
                userInfo = Map.class.cast(result);
            }
        }

        public SourceRewriter getSourceRewriter() throws IOException {
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
                    PositionConverter converter = getPositionConverter();
                    int off = converter != null ? converter.getOriginalPosition(offset) : offset;
                    if (off >= 0)
                        diffs.add(new Difference(Difference.Kind.INSERT, ces.createPositionRef(off, Bias.Forward), ces.createPositionRef(off, Bias.Backward), null, s, userInfo.get(offset)));
                }
            }
            
            public void skipThrough(SourceReader in, int pos) throws IOException, BadLocationException {
                char[] buf = in.getCharsTo(pos);
                Difference diff = diffs.size() > 0 ? diffs.get(diffs.size() - 1) : null;
                if (diff != null && diff.getKind() == Difference.Kind.INSERT && diff.getStartPosition().getOffset() == offset) {
                    diff.kind = Difference.Kind.CHANGE;
                    diff.oldText = new String(buf);
                } else {
                    PositionConverter converter = getPositionConverter();
                    int off = converter != null ? converter.getOriginalPosition(offset) : offset;
                    if (off >= 0)
                        diffs.add(new Difference(Difference.Kind.REMOVE, ces.createPositionRef(off, Bias.Forward), ces.createPositionRef(off + buf.length, Bias.Backward), new String(buf), null, userInfo.get(offset)));
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
