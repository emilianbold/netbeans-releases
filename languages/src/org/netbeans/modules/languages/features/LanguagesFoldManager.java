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

package org.netbeans.modules.languages.features;

import java.util.Set;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.SwingUtilities;
import javax.swing.text.StyledDocument;
import javax.swing.text.Document;
import javax.swing.text.BadLocationException;
import org.netbeans.api.languages.ParserManager;
import org.openide.text.NbDocument;
import javax.swing.event.DocumentEvent;

import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.languages.ASTEvaluator;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.ParserManager.State;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldHierarchy;
import org.netbeans.api.editor.fold.FoldType;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.spi.editor.fold.FoldHierarchyTransaction;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldManagerFactory;
import org.netbeans.spi.editor.fold.FoldOperation;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.LanguagesManager;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.ParserManagerImpl;


/**
 *
 * @author Jan Jancura
 */
public class LanguagesFoldManager extends ASTEvaluator implements FoldManager {
    
    static final String         FOLD = "FOLD";
    private static final int    EVALUATING = 0;
    private static final int    STOPPED = 1;
    
    private FoldOperation       operation;
    private Document            doc;
    private ParserManager       editorParser;
    private int                 evalState = STOPPED;
    
    
    /** Creates a new instance of JavaFoldManager */
    public LanguagesFoldManager () {
    }
    
    /**
     * Initialize this manager.
     *
     * @param operation fold hierarchy operation dedicated to the fold manager.
     */
    public void init (FoldOperation operation) {
        Document d = operation.getHierarchy ().getComponent ().getDocument ();
        if (d instanceof NbEditorDocument) {
            this.doc = d;
            this.operation = operation;
            editorParser = ParserManager.get (doc);
            editorParser.addASTEvaluator (this);
            try {
                ((ParserManagerImpl) editorParser).fire (
                    editorParser.getState (), 
                    null, 
                    Collections.<String,Set<ASTEvaluator>>singletonMap (FOLD, Collections.<ASTEvaluator>singleton (this)),
                    editorParser.getAST ()
                );
            } catch (ParseException ex) {
            }
        }
    }
    
    /**
     * Initialize the folds provided by this manager.
     * <br>
     * The fold manager should create initial set of folds here
     * if it does not require too much resource consumption.
     * <br>
     * As this method is by default called at the file opening time
     * then it may be better to schedule the initial fold computations
     * for later time and do nothing here. 
     *
     * <p>
     * Any listeners necessary for the maintenance of the folds
     * can be attached here.
     * <br>
     * Generally there should be just weak listeners used
     * to not prevent the GC of the text component.
     *
     * @param transaction transaction in terms of which the intial
     *  fold changes can be performed.
     */
    public void initFolds (FoldHierarchyTransaction transaction) {
    }
    
    /**
     * Called by hierarchy upon the insertion to the underlying document.
     * <br>
     * If there would be any fold modifications required they may be added
     * to the given transaction.
     *
     * @param evt document event describing the document modification.
     * @param transaction open transaction to which the manager can add
     *  the fold changes.
     */
    public void insertUpdate (DocumentEvent evt, FoldHierarchyTransaction transaction) {
    }
    
    /**
     * Called by hierarchy upon the removal in the underlying document.
     * <br>
     * If there would be any fold modifications required they may be added
     * to the given transaction.
     *
     * @param evt document event describing the document modification.
     * @param transaction open transaction to which the manager can add
     *  the fold changes.
     */
    public void removeUpdate (DocumentEvent evt, FoldHierarchyTransaction transaction) {
    }
    
    /**
     * Called by hierarchy upon the change in the underlying document.
     * <br>
     * If there would be any fold modifications required they may be added
     * to the given transaction.
     *
     * @param evt document event describing the document change.
     * @param transaction open transaction to which the manager can add
     *  the fold changes.
     */
    public void changedUpdate (DocumentEvent evt, FoldHierarchyTransaction transaction) {
    }
    
    /**
     * Notify that the fold was removed from hierarchy automatically
     * by fold hierarchy infrastructure processing
     * because it became empty (by a document modification).
     */
    public void removeEmptyNotify (Fold epmtyFold) {
    }
    
    /**
     * Notify that the fold was removed from hierarchy automatically
     * by fold hierarchy infrastructure processing
     * because it was damaged by a document modification.
     */
    public void removeDamagedNotify (Fold damagedFold) {
    }
    
    /**
     * Notify that the fold was expanded automatically
     * by fold hierarchy infrastructure processing
     * because its <code>isExpandNecessary()</code>
     * return true.
     */
    public void expandNotify (Fold expandedFold) {
    }

    /**
     * Notification that this manager will no longer be used by the hierarchy.
     * <br>
     * The folds that it maintains are still valid but after this method
     * finishes they will be removed from the hierarchy.
     *
     * <p>
     * This method is not guaranteed to be called. Therefore the manager
     * must only listen weekly on the related information providers
     * so that it does not block the hierarchy from being garbage collected.
     */
    public void release () {
        //S ystem.out.println("release " + mimeType + " : " + operation + " : " + this);
        if (doc != null) {
            editorParser.removeASTEvaluator (this);
        }
        editorParser = null;
    }

    
    // ASTEvaluator methods ....................................................


    private static FoldType defaultFoldType = new FoldType ("default");
    private List<FoldItem> folds;
    
    public void beforeEvaluation (State state, ASTNode root) {
        evalState = EVALUATING;
        folds = null;
    }

    public void afterEvaluation (State state, ASTNode root) {
        SwingUtilities.invokeLater (new Runnable () {
            public void run () { 
                if (operation == null) {
                    evalState = STOPPED;
                    return;
                }
                FoldHierarchy hierarchy = operation.getHierarchy ();
                FoldHierarchyTransaction transaction = operation.openTransaction ();
                try {
                    Fold fold = operation.getHierarchy ().getRootFold ();
                    List<Fold> l = new ArrayList<Fold> (fold.getFoldCount ());
                    int i, k = fold.getFoldCount ();
                    for (i = 0; i < k; i++) {
                        Fold f = fold.getFold (i);
                        //hacky fix - we need to find a better solution
                        //how to check if the fold was created by me
                        try {
                            operation.getExtraInfo(f);
                            //no ISE thrown - my fold
                            l.add (f);
                        } catch (IllegalStateException e) {
                            //not my fold
                        }
                    }
                    for (i = 0; i < l.size(); i++)
                        operation.removeFromHierarchy (l.get (i), transaction);
                    if (folds == null) return;
                    Iterator<FoldItem> it = folds.iterator ();
                    while (it.hasNext ()) {
                        FoldItem f = it.next ();
                        operation.addToHierarchy (
                            f.type, f.foldName, false, f.start, f.end, 0, 0, 
                            hierarchy, transaction
                        );
                    }
                } catch (BadLocationException ex) {
                    ex.printStackTrace ();
                } finally {
                    transaction.commit ();
                    evalState = STOPPED;
                }
            }
        });
    }
    
    public String getFeatureName () {
        return "FOLD";
    }

    public void evaluate (State state, List<ASTItem> path, Feature fold) {
        ASTItem item = path.get (path.size () - 1);
        int s = item.getOffset (),
            e = item.getEndOffset ();
        int sln = NbDocument.findLineNumber ((StyledDocument)doc, s),
            eln = NbDocument.findLineNumber ((StyledDocument)doc, e);
        if (sln == eln) return;
        String mimeType = item.getMimeType ();
        Language language = (Language) item.getLanguage ();
        boolean isTokenFold = ((item instanceof ASTToken) && 
                    fold == language.getFeature (FOLD, ((ASTToken) item).getTypeID ()));
        if (!isTokenFold) {
            TokenHierarchy th = TokenHierarchy.get (doc);
            if (doc instanceof NbEditorDocument)
                ((NbEditorDocument) doc).readLock ();
            try {
                TokenSequence ts = th.tokenSequence ();
                ts.move (e - 1);
                if (!ts.moveNext ()) return;
                while (!ts.language ().mimeType ().equals (mimeType)) {
                    ts = ts.embedded ();
                    if (ts == null) return;
                    ts.move (e - 1);
                    if (!ts.moveNext ()) return;
                }
                Token t = ts.token ();
                Set<Integer> skip = language.getAnalyser ().getSkipTokenTypes ();
                while (skip.contains (t.id ().ordinal ())) {
                    if (!ts.movePrevious ()) break;
                    t = ts.token ();
                }
                e = ts.offset () + t.length ();
                sln = NbDocument.findLineNumber ((StyledDocument)doc, s);
                eln = NbDocument.findLineNumber ((StyledDocument)doc, e);
                if (eln - sln < 1) return;
            } finally {
                if (doc instanceof NbEditorDocument)
                    ((NbEditorDocument) doc).readUnlock ();
            }
        }

        if (fold.hasSingleValue ()) {
            String foldName = language.localize((String) fold.getValue (SyntaxContext.create (doc, ASTPath.create (path))));
            if (foldName == null) return;            
            addFold (new FoldItem(foldName, s, e, defaultFoldType));
            return;
        }
        String foldName = language.localize((String) fold.getValue ("fold_display_name", SyntaxContext.create (doc, ASTPath.create (path))));
        if (foldName == null) {
            foldName = "..."; // NOI18N
        }
        String foldType = language.localize((String) fold.getValue ("collapse_type_action_name"));
        addFold (new FoldItem (foldName, s, e, Folds.getFoldType (foldType)));
    }
    
    private void addFold (FoldItem foldItem) {
        if (folds == null)
            folds = new CopyOnWriteArrayList<FoldItem> ();
        folds.add (foldItem);
    }
    
    // package private methods for unit tests...................................
    
    void init (Document doc) {
        this.doc = doc;
        this.operation = null;
        editorParser = ParserManager.get(doc);
        editorParser.addASTEvaluator(this);
    }
    
    List<FoldItem> getFolds() {
        return folds;
    }
    
    boolean isEvaluating() {
        return evalState == EVALUATING;
    }
    
    // innerclasses ............................................................
    
    static final class FoldItem {
        String foldName;
        int start;
        int end;
        FoldType type;
        
        FoldItem (String foldName, int start, int end, FoldType type) {
            this.foldName = foldName;
            this.start = start;
            this.end = end;
            this.type = type;
        }
    } 

    public static final class Factory implements FoldManagerFactory {
        
        public FoldManager createFoldManager () {
            return new LanguagesFoldManager ();
        }

    }
}
