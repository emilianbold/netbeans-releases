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
import org.netbeans.api.languages.LanguagesManager;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.ParserManager;
import org.netbeans.api.languages.ParserManager.State;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.Evaluator;
import org.netbeans.modules.languages.LanguagesManagerImpl;
import org.openide.text.Annotation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;


/**
 *
 * @author Jan Jancura
 */
public class AnnotationManager extends ASTEvaluator {
    
    private NbEditorDocument    doc;
    private ParserManager       parser;
    private List                items;
    private List<LanguagesAnnotation> annotations = new ArrayList<LanguagesAnnotation> ();

    
    /** Creates a new instance of AnnotationManager */
    public AnnotationManager (Document doc) {
        //doc.addDocumentListener (this);
        this.doc = (NbEditorDocument) doc;
        parser = ParserManager.get ((NbEditorDocument) doc);
        parser.addASTEvaluator (this);
    }

    public void beforeEvaluation (State state, ASTNode root) {
        items = new ArrayList ();
    }

    public void afterEvaluation (State state, ASTNode root) {
        refresh (items);
    }

    public void evaluate (State state, ASTPath path) {
        try {
            ASTItem item = path.getLeaf ();
            Language language = ((LanguagesManagerImpl) LanguagesManager.getDefault ()).
                getLanguage (item.getMimeType ());
            Map properties = (Map) language.getFeature (Language.MARK, path);
            if (properties != null) {
                if (evaluateCondition (doc, path, properties)) {
                    items.add (item);
                    items.add (properties);
                }
            }
        } catch (ParseException ex) {
        }
    }
    
    private void refresh (final List items) {
        SwingUtilities.invokeLater (new Runnable () {
            public void run () {
                try {
                    Iterator it = annotations.iterator ();
                    while (it.hasNext ())
                        doc.removeAnnotation ((Annotation) it.next ());
                    annotations = new ArrayList ();
                    it = items.iterator ();
                    while (it.hasNext ()) {
                        Object o = it.next ();
                        Map m = (Map) it.next ();
                        LanguagesAnnotation la = new LanguagesAnnotation (
                            (String) ((Evaluator) m.get ("type")).evaluate (),
                            (String) ((Evaluator) m.get ("message")).evaluate ()
                        );
                        if (o instanceof ASTNode) {
                            doc.addAnnotation (
                                doc.createPosition (((ASTNode) o).getOffset ()),
                                ((ASTNode) o).getLength (),
                                la
                            );
                        } else {
                            doc.addAnnotation (
                                doc.createPosition (((ASTToken) o).getOffset ()),
                                ((ASTToken) o).getLength (),
                                la
                            );
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
    
    private boolean evaluateCondition (Document doc, ASTPath path, Map m) {
        Evaluator e = (Evaluator) m.get ("condition");
        if (e == null) return true;
        return ((Boolean) e.evaluate (
            SyntaxContext.create (doc, path)
        )).booleanValue ();
    }

    
    // innerclasses ............................................................
    
    private static class LanguagesAnnotation extends Annotation {

        private String type;
        private String description;

        /** Creates a new instance of ToolsAnotation */
        private LanguagesAnnotation (
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

