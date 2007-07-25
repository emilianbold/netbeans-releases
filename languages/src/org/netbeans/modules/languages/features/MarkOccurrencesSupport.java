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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.languages.features;

import java.awt.Color;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.Highlighting;
import org.netbeans.api.languages.Highlighting.Highlight;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.ParserManager;
import org.netbeans.api.languages.ParserManager.State;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.languages.features.AnnotationManager.LanguagesAnnotation;
import org.openide.util.RequestProcessor;


/**
 *
 * @author Jan Jancura
 */
public class MarkOccurrencesSupport implements CaretListener {

    private static Map<JTextComponent,WeakReference<MarkOccurrencesSupport>> 
                                        editorToMOS = new WeakHashMap<JTextComponent,WeakReference<MarkOccurrencesSupport>> ();
    private JTextComponent              editor;
    private RequestProcessor.Task       parsingTask;
    private List<Highlight>             highlights;
    private List<LanguagesAnnotation>   annotations;
    
    
    public MarkOccurrencesSupport (JTextComponent editor) {
        this.editor = editor;
        editorToMOS.put (editor, new WeakReference<MarkOccurrencesSupport> (this));
    }

    public void caretUpdate (final CaretEvent e) {
        if (parsingTask != null) {
            parsingTask.cancel ();
        }
        parsingTask = RequestProcessor.getDefault ().post (new Runnable () {
            public void run () {
                refresh (e.getDot ());
            }
        }, 1000);
    }
    
    private void refresh (int offset) {
        ParserManager parserManager = ParserManager.get (editor.getDocument ());
        if (parserManager.getState () == State.PARSING) {
            return;
        }
        try {
            removeHighlights ();
            ASTNode node = parserManager.getAST ();
            DatabaseContext root = DatabaseManager.getRoot (node);
            if (root == null) {
                // I keep getting NPEs on the next line while editing RHTML
                // files - please check
                return;
            }
            DatabaseItem item = root.getDatabaseItem (offset);
            if (item == null)
                item = root.getDatabaseItem (offset - 1);
            if (item == null) return;
            addHighlights (getUssages (item, node));
        } catch (ParseException ex) {
            ex.printStackTrace ();
        }
    }
    
    private void addHighlights (final List<ASTItem> ussages) {
        if (ussages.isEmpty ()) return;
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                try {
                    NbEditorDocument doc = (NbEditorDocument) editor.getDocument ();
                    Highlighting highlighting = Highlighting.getHighlighting (doc);
                    annotations = new ArrayList<LanguagesAnnotation> ();
                    highlights = new ArrayList<Highlight> ();
                    Iterator<ASTItem> it = ussages.iterator ();
                    while (it.hasNext ()) {
                        ASTItem i = it.next ();
                        highlights.add (highlighting.highlight (i, getHighlightAS ()));
                        LanguagesAnnotation la = new LanguagesAnnotation (
                            "Usage",
                            "..."
                        );
                        doc.addAnnotation (
                            doc.createPosition (i.getOffset ()),
                            i.getLength (),
                            la
                        );
                        annotations.add (la);
                    }
                } catch (BadLocationException ex) {
                    ex.printStackTrace ();
                }
            }
        });
    }
    
    static List<ASTItem> getUssages (DatabaseItem item, ASTNode root) {
        List<ASTItem> result = new ArrayList<ASTItem> ();
        DatabaseDefinition definition = null;
        if (item instanceof DatabaseDefinition)
            definition = (DatabaseDefinition) item;
        else
            definition = ((DatabaseUsage) item).getDefinition ();
        result.add (root.findPath (definition.getOffset ()).getLeaf ());
        Iterator<DatabaseUsage> it = definition.getUsages ().iterator ();
        while (it.hasNext ()) {
            DatabaseUsage databaseUsage =  it.next();
            result.add (root.findPath (databaseUsage.getOffset ()).getLeaf ());
        }
        return result;
    }
    
    static void removeHighlights (JTextComponent editor) {
        WeakReference<MarkOccurrencesSupport> wr = editorToMOS.get (editor);
        if (wr == null) return;
        MarkOccurrencesSupport mos = wr.get ();
        if (mos == null) return;
        mos.removeHighlights ();
    }

    private void removeHighlights (
    ) {
        if (highlights == null) return;
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                NbEditorDocument doc = (NbEditorDocument) editor.getDocument ();
                Iterator<Highlight> it = highlights.iterator ();
                while (it.hasNext ())
                    it.next ().remove ();
                Iterator<LanguagesAnnotation> it2 = annotations.iterator ();
                while (it2.hasNext ())
                    doc.removeAnnotation (it2.next ());
                highlights = null;
                annotations = null;
            }
        });
    }
            
    private static AttributeSet highlightAS = null;
    
    private static AttributeSet getHighlightAS () {
        if (highlightAS == null) {
            SimpleAttributeSet as = new SimpleAttributeSet ();
            as.addAttribute (StyleConstants.Background, new Color (236, 235, 163));
            highlightAS = as;
        }
        return highlightAS;
    }
}
