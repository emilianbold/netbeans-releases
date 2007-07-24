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

import org.netbeans.api.languages.ASTEvaluator;
import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.ParserManager.State;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.lexer.Token;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManager;
import org.openide.text.Annotation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.languages.LanguageDefinitionNotFoundException;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.languages.EditorParser;


/**
 *
 * @author Jan Jancura
 */
public class AnnotationManager extends ASTEvaluator {
    
    private NbEditorDocument            doc;
    private EditorParser                parser;
    private List<ASTItem>               items;
    private List<Feature>               marks;
    private List<LanguagesAnnotation>   annotations = new ArrayList<LanguagesAnnotation> ();

    
    /** Creates a new instance of AnnotationManager */
    public AnnotationManager (Document doc) {
        
        this.doc = (NbEditorDocument) doc;
        parser = EditorParser.get (doc);
        parser.addASTEvaluator (this);
    }

    public void beforeEvaluation (State state, ASTNode root) {
        items = new ArrayList<ASTItem> ();
        marks = new ArrayList<Feature> ();
    }

    public void afterEvaluation (State state, ASTNode root) {
        refresh (items, marks);
    }

    public void evaluate (State state, ASTPath path) {
        try {
            ASTItem item = path.getLeaf ();
            Language language = LanguagesManager.getDefault ().
                getLanguage (item.getMimeType ());
            Feature mark = language.getFeature ("MARK", path);
            if (mark != null) {
                if (mark.getBoolean ("condition", SyntaxContext.create (doc, path), true)) {
                    items.add (item);
                    marks.add (mark);
                }
            }
        } catch (ParseException ex) {
        }
    }
    
    public void remove () {
        removeAnnotations ();
        parser.removeASTEvaluator (this);
    }
    
    private void removeAnnotations () {
        Iterator<LanguagesAnnotation> it = annotations.iterator ();
        while (it.hasNext ())
            doc.removeAnnotation (it.next ());
    }
    
    private void refresh (final List<ASTItem> items, final List<Feature> marks) {
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                try {
                    removeAnnotations ();
                    annotations = new ArrayList<LanguagesAnnotation> ();
                    Iterator<ASTItem> it2 = items.iterator ();
                    Iterator<Feature> it3 = marks.iterator ();
                    while (it2.hasNext ()) {
                        ASTItem item = it2.next ();
                        Feature mark = it3.next ();
                        String message = (String) mark.getValue ("message");
                        try {
                            Language language = LanguagesManager.getDefault ().getLanguage (item.getMimeType ());
                            message = language.localize(message);
                        } catch (LanguageDefinitionNotFoundException e) {
                        }
                        LanguagesAnnotation la = new LanguagesAnnotation (
                            (String) mark.getValue ("type"),
                            message
                        );
   
                        if (item.getLength() == 0) {
                            //when the ASTItem length is zero we need to find an appropriate token to signal the error 
                            TokenHierarchy hi = TokenHierarchy.get(doc);
                            TokenSequence ts = hi.tokenSequence();
                            ts.move(item.getOffset());
                            //test if next token contains the ASTItem's language embedding
                            if(!(ts.moveNext() && testCreateAnnotation(hi, ts, item, la)))
                                //if not, do the same with previous token
                              if(!(ts.movePrevious() && testCreateAnnotation(hi, ts, item, la))) {
                                  //give up - use default annotation location
                                  doc.addAnnotation(doc.createPosition(item.getOffset()), item.getLength(), la);
                              }
                        } else {
                            doc.addAnnotation(doc.createPosition(item.getOffset()), item.getLength(), la);
                        }
                        annotations.add (la);
                    }
                } catch (BadLocationException ex) {
                    //ErrorManager.getDefault ().notify (ex);
                    System.out.println ("AnnotationManager " + ex);
                }
            }
        });
    }

    private boolean testCreateAnnotation(TokenHierarchy hi, TokenSequence ts, ASTItem item, LanguagesAnnotation la) throws BadLocationException {
        if (ts.language().mimeType().equals(item.getMimeType())) {
                Token t = ts.token();
                doc.addAnnotation(doc.createPosition(t.offset(hi)), t.length(), la);
                return true;
            } else {
                ts = ts.embedded();
                if(ts == null) {
                    return false;
                } else {
                    ts.moveNext();
                    return testCreateAnnotation(hi, ts, item, la);
                }
            }
    }
    
    
    // innerclasses ............................................................
    
    static class LanguagesAnnotation extends Annotation {

        private String type;
        private String description;

        /** Creates a new instance of ToolsAnotation */
        LanguagesAnnotation (
            String type,
            String description
        ) {
            this.type = type;
            this.description = description;
        }

        /** Returns name of the file which describes the annotation type.
         * The file must be defined in module installation layer in the
         * directory "Editors/AnnotationTypes"
         * @return  name of the anotation type
         */
        public String getAnnotationType () {
            return type;
        }

        /** Returns the tooltip text for this annotation.
         * @return  tooltip for this annotation
         */
        public String getShortDescription () {
            return description;
        }
    }
}

