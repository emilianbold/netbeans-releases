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
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Language;
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
    
    private static final String COMPLETION = "COMPLETION";
    
    
    public CompletionTask createTask (int queryType, JTextComponent component) {
        return new CompletionTaskImpl (component);
    }

    public int getAutoQueryTypes (JTextComponent component, String typedText) {
        return 0;
    }
    
    private static class CompletionTaskImpl implements CompletionTask {
        
        private JTextComponent          component;
        private NbEditorDocument        doc;
        private boolean                 ignoreCase;
        private List<CompletionItem>    items = new ArrayList<CompletionItem> ();
        
        
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

            Iterator<CompletionItem> it = items.iterator ();
            while (it.hasNext ()) {
                CompletionItem item = it.next ();
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
            Feature feature = null;
            String start = null;
            try {
                Language language = ((LanguagesManagerImpl) LanguagesManager.getDefault ()).
                    getLanguage (mimeType);
                tokenSequence.move (offset - 1);
                if (!tokenSequence.moveNext ()) return;
                Token token = tokenSequence.token ();
                start = token.text ().toString ();
                if (tokenSequence.offset () > offset) {
                    // border of embedded language
                    // [HACK] borders should be represented by some tokens!!!
                    return;
                }
                start = start.substring (0, offset - tokenSequence.offset ()).trim ();
                if (start.equals ("."))
                    start = "";// [HACK]
                String tokenType = token.id ().name ();
                ignoreCase = false;
                Feature f = language.getFeature ("PROPERTIES");
                if (f != null)
                    ignoreCase = f.getBoolean ("ignoreCase", false);
                if (ignoreCase) start = start.toLowerCase ();
                feature = language.getFeature (
                    Language.COMPLETION, 
                    tokenType
                );
            } catch (ParseException ex) {
            }
            if (feature == null)
                compute (tokenSequence.embedded (), offset, resultSet, doc);
            else
                addTags (feature, start, Context.create (doc, tokenSequence), resultSet);
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
            if (token.getLength () != token.getIdentifier ().length ()) {
                // [HACK]
                // something like token.getRealIndex () + 
                // add tokens for language borders...
                return;
            }
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
                    Feature feature = language.getFeature (COMPLETION, path.subPath (i));
                    if (feature != null) {
                        boolean recursive = feature.getBoolean ("recursive", false);
                        if ((i != path.size () - 1) && !recursive) break;
                        addTags (feature, start, SyntaxContext.create (doc, path.subPath (i)), resultSet);
                    }
                } catch (ParseException ex) {
                }
            }
        }

        private void addTags (Feature feature, String start, Context context, CompletionResultSet resultSet) {
            int j = 1;
            while (true) {
                Object o = feature.getValue ("text" + j, context);
                if (o == null) break;
                if (o instanceof String)
                    addTags ((String) o, feature, j, start, resultSet);
                else {
                    addMethodCallTags (
                        (List) o,
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
            List                keys, 
            Context             context, 
            CompletionResultSet resultSet, 
            String              start
        ) {
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
            Feature             feature, 
            int                 j, 
            String              start, 
            CompletionResultSet resultSet
        ) {
            if (ignoreCase)
                text = text.toLowerCase ();
            String description = (String) feature.getValue ("description" + j);
            if (description == null)
                description = text;
            String icon = (String) feature.getValue ("icon" + j);
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