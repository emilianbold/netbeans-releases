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

import java.util.ArrayList;
import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.LanguagesManager;
import org.netbeans.api.languages.LanguagesManager;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.ParserManager;
import org.netbeans.api.languages.ParserManager.State;
import org.netbeans.api.languages.ParserManagerListener;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.support.CompletionSupport;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.languages.Context;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.Evaluator;
import org.netbeans.modules.languages.LanguagesManagerImpl;
import org.netbeans.modules.languages.LanguagesManagerImpl;
import org.netbeans.modules.languages.ParserManagerImpl;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.openide.ErrorManager;
import org.openide.ErrorManager;
import java.util.Iterator;
import javax.swing.text.JTextComponent;
import java.util.List;
import java.util.Map;


/**
 *
 * @author Jan Jancura
 */
public class CompletionProviderImpl implements CompletionProvider {
    
    public CompletionTask createTask (int queryType, JTextComponent component) {
        return new CompletionTaskImpl (component);
    }

    public int getAutoQueryTypes (JTextComponent component, String typedText) {
        return 0;
    }
    
    private static class CompletionTaskImpl implements CompletionTask {
        
        private JTextComponent      component;
        private NbEditorDocument    doc;
        private boolean             ignoreCase;
        private List                items = new ArrayList ();
        
        
        CompletionTaskImpl (JTextComponent component) {
            this.component = component;
        }
        
        public void query (CompletionResultSet resultSet) {
            //S ystem.out.println("CodeCompletion: query " + resultSet);
            compute (resultSet);
        }

        public void refresh (CompletionResultSet resultSet) {
            //S ystem.out.println("CodeCompletion: refresh " + resultSet);
            if (resultSet == null) return;

            TokenHierarchy tokenHierarchy = TokenHierarchy.get (doc);
            TokenSequence tokenSequence = tokenHierarchy.tokenSequence ();
            int offset = component.getCaret ().getDot ();
            tokenSequence.move (offset - 1);
            if (!tokenSequence.moveNext() && !tokenSequence.movePrevious()) {
                resultSet.finish ();
                return;
            }
            Token token = tokenSequence.token ();
            String start = token.text ().toString ();
            start = start.substring (0, offset - tokenSequence.offset ()).trim ();
            if (start.equals ("."))
                start = "";// [HACK]
            if (ignoreCase) start = start.toLowerCase ();

            Iterator it = items.iterator ();
            while (it.hasNext ()) {
                CompletionItem item = (CompletionItem) it.next ();
                CharSequence chs = item.getInsertPrefix ();
                String s = chs instanceof String ? (String) chs : chs.toString ();
                if (s.startsWith (start))
                    resultSet.addItem (item);
            }
            resultSet.finish ();
            //compute (resultSet);
        }

        public void cancel () {
        }
        
        private void compute (CompletionResultSet resultSet) {
            doc = (NbEditorDocument) component.getDocument ();
            TokenHierarchy tokenHierarchy = TokenHierarchy.get (doc);
            TokenSequence tokenSequence = tokenHierarchy.tokenSequence ();
            int offset = component.getCaret ().getDot ();
            compute (tokenSequence, offset, resultSet, doc);
            addParserTags (resultSet);
        }
        
        private void compute (
            TokenSequence       tokenSequence, 
            int                 offset, 
            CompletionResultSet resultSet,
            NbEditorDocument    doc
        ) {
            if (tokenSequence == null) return;
            String mimeType = tokenSequence.language ().mimeType ();
            Map tags = null;
            String start = null;
            try {
                Language language = ((LanguagesManagerImpl) LanguagesManager.getDefault ()).
                    getLanguage (mimeType);
                tokenSequence.move (offset - 1);
                if (!tokenSequence.moveNext ()) return;
                Token token = tokenSequence.token ();
                start = token.text ().toString ();
                start = start.substring (0, offset - tokenSequence.offset ()).trim ();
                if (start.equals ("."))
                    start = "";// [HACK]
                String tokenType = token.id ().name ();
                ignoreCase = false;
                Evaluator e = (Evaluator) language.getProperty ("ignoreCase");
                if (e != null)
                    ignoreCase = "true".equals (e.evaluate ());
                if (ignoreCase) start = start.toLowerCase ();
                tags = (Map) language.getFeature (
                    Language.COMPLETION, 
                    tokenType
                );
            } catch (ParseException ex) {
            }
            if (tags == null)
                compute (tokenSequence.embedded (), offset, resultSet, doc);
            else
                addTags (tags, start, Context.create (doc, tokenSequence), resultSet);
        }

        private void addParserTags (final CompletionResultSet resultSet) {
            final ParserManager parserManager = ParserManager.get (doc);
            if (parserManager.getState () == State.PARSING) {
                //S ystem.out.println("CodeCompletion: parsing...");
                parserManager.addListener (new ParserManagerListener () {
                    public void parsed (State state, ASTNode ast) {
                        //S ystem.out.println("CodeCompletion: parsed " + state);
                        addParserTags (ast, resultSet);
                        resultSet.finish ();
                        parserManager.removeListener (this);
                    }
                });
            } else {
                try {
                    addParserTags (ParserManagerImpl.get (doc).getAST (), resultSet);
                } catch (ParseException ex) {
                    ErrorManager.getDefault ().notify (ex);
                }
                resultSet.finish ();
            }
        }
        
        private void addParserTags (ASTNode node, CompletionResultSet resultSet) {
            if (node == null) {
                //S ystem.out.println("CodeCompletion: No AST");
                return;
            }
            int offset = component.getCaret ().getDot ();
            ASTPath path = node.findPath (offset - 1);
            if (path == null) return;
            ASTToken token = (ASTToken) path.getLeaf ();
            String start = token.getIdentifier ().substring (
                0, offset - token.getOffset ()
            ).trim ();
            if (start.equals ("."))
                start = "";// [HACK]
            
            //S ystem.out.println("CodeCompletion: (syntax) start=" + start + ": stoken=" + token);
            
            for (int i = path.size () - 1; i >= 0; i--) {
                ASTItem item = path.get (i);
                try {
                    Language language = ((LanguagesManagerImpl) LanguagesManager.getDefault ()).
                        getLanguage (item.getMimeType ());
                    Map tags = (Map) language.getFeature (Language.COMPLETION, path.subPath (i));
                    if (tags != null) {
                        boolean recursive = false;
                        if (tags.containsKey ("recursive")) {
                            Evaluator e = (Evaluator) tags.get ("recursive");
                            recursive = ((String) e.evaluate ()).equals ("true");
                        }
                        if ((i != path.size () - 1) && !recursive) break;
                        addTags (tags, start, SyntaxContext.create (doc, path.subPath (i)), resultSet);
                    }
                } catch (ParseException ex) {
                }
            }
        }

        private void addTags (Map tags, String start, Context context, CompletionResultSet resultSet) {
            int j = 1;
            while (true) {
                Evaluator e = (Evaluator) tags.get ("text" + j);
                if (e == null) break;
                if (e instanceof Evaluator.Expression)
                    addTags ((String) e.evaluate (), tags, j, start, resultSet);
                else {
                    addMethodCallTags (
                        e,
                        context,
                        resultSet,
                        start
                    );
                }
                j++;
            } // while
        }
        
        /**
         * Adds completion items obtained by method call to result.
         */
        private void addMethodCallTags (
            Evaluator           keysEvaluator, 
            Context              context, 
            CompletionResultSet resultSet, 
            String              start
        ) {
            List keys = (List) keysEvaluator.evaluate (context);
            Iterator it = keys.iterator ();
            while (it.hasNext ()) {
                Object o = it.next ();
                if (o instanceof CompletionItem) {
                    CompletionItem item = (CompletionItem) o;
                    items.add (item);
                    CharSequence chs = item.getInsertPrefix ();
                    String s = chs instanceof String ? (String) chs : chs.toString ();
                    if (ignoreCase)
                        s = s.toLowerCase ();
                    if (s.startsWith (start))
                        resultSet.addItem (item);
                } else {
                    String t = (String) o;
                    if (ignoreCase) t = t.toLowerCase ();
                    CompletionItem item = CompletionSupport.createCompletionItem (t);
                    items.add (item);
                    if (t.startsWith (start))
                        resultSet.addItem (item);
                }
            }
        }
        
        private void addTags (
            String              text, 
            Map                 tags, 
            int                 j, 
            String              start, 
            CompletionResultSet resultSet
        ) {
            if (ignoreCase)
                text = text.toLowerCase ();
            String description = (String) tags.get ("description" + j);
            if (description == null)
                description = text;
            String icon = (String) tags.get ("icon" + j);
            CompletionItem item = CompletionSupport.createCompletionItem (
                text, description, icon
            );
            items.add (item);
            if (!text.startsWith (start))
                return;
            resultSet.addItem (item);
        }
    }
}