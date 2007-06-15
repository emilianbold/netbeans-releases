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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.Highlighting;
import org.netbeans.api.languages.Highlighting.Highlight;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.ParserManager;
import org.netbeans.api.languages.ParserManager.State;
import org.netbeans.editor.BaseAction;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.languages.features.DatabaseItem;


/**
 *
 * @author Jan Jancura
 */
public class InstantRenameAction extends BaseAction implements KeyListener, DocumentListener {
    
    private List<Element>               elements;
    private List<Highlight>             highlights;
    private JTextComponent              editor;

    
    /** Creates a new instance of InstantRenameAction */
    public InstantRenameAction() {
        super ("in-place-refactoring", ABBREV_RESET | MAGIC_POSITION_RESET | UNDO_MERGE_RESET);
    }
    
    public void actionPerformed (ActionEvent evt, final JTextComponent editor) {
        int offset = editor.getCaretPosition ();
        ParserManager parserManager = ParserManager.get (editor.getDocument ());
        if (parserManager.getState () == State.PARSING) {
            return;
        }
        try {
            removeHighlights (highlights, editor);
            highlights = null;
            ASTNode node = parserManager.getAST ();
            DatabaseContext root = DatabaseManager.getRoot (node);
            if (root == null) return;
            DatabaseItem item = root.getDatabaseItem (offset);
            if (item == null)
                item = root.getDatabaseItem (offset - 1);
            if (item == null) {
                return;
            }
            NbEditorDocument doc = (NbEditorDocument) editor.getDocument ();
            this.editor = editor;
            elements = getUssages (item, node, doc);
            addHighlights (elements);
        } catch (BadLocationException ex) {
            ex.printStackTrace ();
        } catch (ParseException ex) {
            ex.printStackTrace ();
        }
    }
    
    protected Class getShortDescriptionBundleClass () {
        return InstantRenameAction.class;
    }
    
    private static AttributeSet highlightAS = null;
    
    private static AttributeSet getHighlightAS () {
        if (highlightAS == null) {
            SimpleAttributeSet as = new SimpleAttributeSet ();
            as.addAttribute (StyleConstants.Background, new Color (138, 191, 236));
            highlightAS = as;
        }
        return highlightAS;
    }

    private static void removeHighlights (
        final List<Highlight> highlights, 
        final JTextComponent editor
    ) {
        if (highlights == null) return;
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                Iterator<Highlight> it = highlights.iterator ();
                while (it.hasNext ())
                    it.next ().remove ();;
                editor.repaint ();
            }
        });
    }

    private void addHighlights (
        final List<Element> elements
    ) {
        if (elements.isEmpty ()) return;
        MarkOccurrencesSupport.removeHighlights (editor);
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                highlights = new ArrayList<Highlight> ();
                NbEditorDocument doc = (NbEditorDocument) editor.getDocument ();
                Highlighting highlighting = Highlighting.getHighlighting (doc);
                Iterator<Element> it = elements.iterator ();
                while (it.hasNext ()) {
                    Element element = it.next ();
                    highlights.add (highlighting.highlight (element.getItem (), getHighlightAS ()));
                }
                editor.repaint ();
                editor.getDocument().addDocumentListener (InstantRenameAction.this);
                editor.addKeyListener (InstantRenameAction.this);
            }
        });
    }
    
    static List<Element> getUssages (DatabaseItem item, ASTNode root, Document doc) throws BadLocationException {
        List<Element> result = new ArrayList<Element> ();
        DatabaseDefinition definition = null;
        if (item instanceof DatabaseDefinition) {
            definition = (DatabaseDefinition) item;
        } else {
            definition = ((DatabaseUsage) item).getDefinition ();
            ASTItem i = root.findPath (item.getOffset ()).getLeaf ();
            result.add (new Element (
                i, 
                doc.createPosition (i.getOffset ()),
                doc.createPosition (i.getEndOffset ()),
                doc
            ));
        }
        ASTItem i = root.findPath (definition.getOffset ()).getLeaf ();
        result.add (new Element (
            i, 
            doc.createPosition (i.getOffset ()),
            doc.createPosition (i.getEndOffset ()),
            doc
        ));
        Iterator<DatabaseUsage> it = definition.getUsages ().iterator ();
        while (it.hasNext ()) {
            DatabaseUsage databaseUsage = it.next ();
            i = root.findPath (databaseUsage.getOffset ()).getLeaf ();
            if (i == result.get (0).getItem ()) continue;
            result.add (new Element (
                i, 
                doc.createPosition (i.getOffset ()),
                doc.createPosition (i.getEndOffset ()),
                doc
            ));
        }
        return result;
    }
    
    private void update () {
        int offset = editor.getCaretPosition ();
        if (!elements.get (0).contains (offset)) return;
        editor.getDocument ().removeDocumentListener (this);
        ((NbEditorDocument) editor.getDocument ()).readLock ();
        try {
            Iterator<Element> it = elements.iterator ();
            try {
                String text = it.next ().getText ();
                while (it.hasNext ())
                    it.next ().setText (text);
            } catch (BadLocationException ex) {
                ex.printStackTrace ();
            }
            editor.getDocument ().addDocumentListener (this);
        } finally {
            ((NbEditorDocument) editor.getDocument ()).readUnlock ();
        }
    }
    
    
    // KeyListener .............................................................

    public void keyTyped (KeyEvent e) {
    }

    public void keyPressed (KeyEvent e) {
	if ((e.getKeyCode () == KeyEvent.VK_ESCAPE && e.getModifiers () == 0) || 
            (e.getKeyCode () == KeyEvent.VK_ENTER  && e.getModifiers() == 0)
        ) {
            removeHighlights (highlights, editor);
            editor.removeKeyListener (this);
            editor.getDocument ().removeDocumentListener (this);
            highlights = null;
            elements = null;
            editor = null;
	    e.consume ();
	}    
    }

    public void keyReleased (KeyEvent e) {
    }

    
    // DocumentListener ........................................................
    
    public void insertUpdate(DocumentEvent e) {
        update ();
    }

    public void removeUpdate(DocumentEvent e) {
        update ();
    }

    public void changedUpdate(DocumentEvent e) {
        update ();
    }
    
    
    // innerclasses ............................................................
    
    private static class Element {
        private Position start, end;
        private ASTItem item;
        private Document doc;
        
        Element (ASTItem item, Position start, Position end, Document doc) {
            this.item = item;
            this.start = start;
            this.end = end;
            this.doc = doc;
        }
        
        boolean contains (int offset) {
            return start.getOffset () <= offset && offset <= end.getOffset ();
        }
        
        void setText (String text) {
            try {
                doc.insertString (end.getOffset (), text, null);
                doc.remove (start.getOffset (), end.getOffset () - start.getOffset () - text.length ());
            } catch (BadLocationException ex) {
                ex.printStackTrace ();
            }
        }
        
        ASTItem getItem () {
            return item;
        }
        
        String getText () throws BadLocationException {
            return doc.getText (start.getOffset (), end.getOffset () - start.getOffset ());
        }
    }
}
