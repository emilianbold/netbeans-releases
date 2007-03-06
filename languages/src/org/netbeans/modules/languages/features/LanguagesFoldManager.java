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

package org.netbeans.modules.languages.features;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.languages.ASTEvaluator;
import org.netbeans.api.languages.LanguagesManager;
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
import org.netbeans.modules.languages.Feature;
import org.netbeans.spi.editor.fold.FoldHierarchyTransaction;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldManagerFactory;
import org.netbeans.spi.editor.fold.FoldOperation;
import org.openide.text.NbDocument;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManagerImpl;
import org.netbeans.modules.languages.ParserManagerImpl;
import java.util.ArrayList;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import java.util.Iterator;
import java.util.List;


/**
 *
 * @author Jan Jancura
 */
public class LanguagesFoldManager extends ASTEvaluator implements FoldManager {
    
    private FoldOperation   operation;
    private NbEditorDocument doc;
    
    
    /** Creates a new instance of JavaFoldManager */
    public LanguagesFoldManager () {
    }
    
    /**
     * Initialize this manager.
     *
     * @param operation fold hierarchy operation dedicated to the fold manager.
     */
    public void init (FoldOperation operation) {
        this.operation = operation;
        //S ystem.out.println("init " + mimeType + " : " + operation + " : " + this);
        Document d = operation.getHierarchy ().getComponent ().getDocument ();
        //S ystem.out.println("LanguagesFoldManager.init " + d);
        if (d instanceof NbEditorDocument) {
            doc = (NbEditorDocument) d;
            ParserManagerImpl.get (doc).addASTEvaluator (this);
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
        if (doc != null)
            ParserManagerImpl.get (doc).removeASTEvaluator (this);
    }

    
    // ASTEvaluator methods ....................................................


    private static FoldType defaultFoldType = new FoldType ("default");
    private List<F> folds;
    
    public void beforeEvaluation (State state, ASTNode root) {
        folds = new ArrayList<F> ();
    }

    public void afterEvaluation (State state, ASTNode root) {
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                FoldHierarchy hierarchy = operation.getHierarchy ();
                FoldHierarchyTransaction transaction = operation.openTransaction ();
                try {
                    Fold fold = operation.getHierarchy ().getRootFold ();
                    List<Fold> l = new ArrayList<Fold> (fold.getFoldCount ());
                    int i, k = fold.getFoldCount ();
                    for (i = 0; i < k; i++)
                        l.add (fold.getFold (i));
                    for (i = 0; i < k; i++)
                        operation.removeFromHierarchy (l.get (i), transaction);
                    Iterator<F> it = folds.iterator ();
                    while (it.hasNext ()) {
                        F f = it.next ();
                        operation.addToHierarchy (
                            f.type, f.foldName, false, f.start, f.end, 0, 0, 
                            hierarchy, transaction
                        );
                    }
                } catch (BadLocationException ex) {
                    ex.printStackTrace ();
                } finally {
                    transaction.commit ();
                }
            }
        });
    }

    public void evaluate (State state, ASTPath path) {
        try {
            ASTItem item = path.getLeaf ();
            int s = item.getOffset (),
                e = item.getEndOffset ();
            int sln = NbDocument.findLineNumber (doc, s),
                eln = NbDocument.findLineNumber (doc, e);
            if (sln == eln) return;
            Language language = ((LanguagesManagerImpl) LanguagesManager.getDefault ()).
                getLanguage (item.getMimeType ());
            Feature fold = language.getFeature (Language.FOLD, path);
            if (fold == null) return;
            if (fold.hasSingleValue ()) {
                String foldName = (String) fold.getValue (SyntaxContext.create (doc, path));
                if (foldName == null) return;            
                folds.add (new F(foldName, s, e, defaultFoldType));
                return;
            }
            String foldName = (String) fold.getValue ("fold_display_name", SyntaxContext.create (doc, path));
            if (foldName == null) foldName = "...";
            String foldType = (String) fold.getValue ("collapse_type_action_name");
            folds.add (new F (foldName, s, e, Folds.getFoldType (foldType)));
        } catch (ParseException ex) {
        }
    }
    
    
    // innerclasses ............................................................
    
    private static final class F {
        String foldName;
        int start;
        int end;
        FoldType type;
        
        F (String foldName, int start, int end, FoldType type) {
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
