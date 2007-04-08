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
import javax.swing.text.Document;
import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.ParserManager;
import org.netbeans.api.languages.ParserManager.State;
import org.netbeans.api.languages.ParserManagerListener;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.languages.Context;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManager;
import org.netbeans.modules.languages.LanguagesManager;
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
        if (".".equals(typedText)) { // NOI18N
            Document doc = component.getDocument ();
            TokenHierarchy tokenHierarchy = TokenHierarchy.get (doc);
            TokenSequence tokenSequence = tokenHierarchy.tokenSequence ();
            int offset = component.getCaret().getDot();
            if (offset <= 1) {
                return 0;
            }
            tokenSequence.move(offset - 2);
            if (!tokenSequence.moveNext() && !tokenSequence.movePrevious()) {
                return 0;
            }
            Token token = tokenSequence.token ();
            if (token.id().name().indexOf("identifier") > -1) { // NOI18N [PENDING]
                return COMPLETION_QUERY_TYPE;
            }
        }
        return 0;
    }
    
    List<CompletionItem> query (JTextComponent component) {
        ListResult r = new ListResult ();
        CompletionTaskImpl task = new CompletionTaskImpl (component);
        task.compute (r);
        r.waitFinished ();
        return r.getList ();
    }
    
    
    private static class CompletionTaskImpl implements CompletionTask {
        
        private JTextComponent          component;
        private Document                doc;
        private boolean                 ignoreCase;
        private List<CompletionItem>    items = new ArrayList<CompletionItem> ();
        
        
        CompletionTaskImpl (JTextComponent component) {
            this.component = component; 
        }
        
        public void query (CompletionResultSet resultSet) {
            //S ystem.out.println("CodeCompletion: query " + resultSet);
            compute (new CompletionResult (resultSet));
        }

        public void refresh (CompletionResultSet resultSet) {
            //S ystem.out.println("CodeCompletion: refresh " + resultSet);
            if (resultSet == null) return;
            
            try {
                TokenHierarchy tokenHierarchy = TokenHierarchy.get (doc);
                TokenSequence tokenSequence = tokenHierarchy.tokenSequence ();
                int offset = component.getCaret ().getDot ();
                tokenSequence.move (offset - 1);
                if (!tokenSequence.moveNext() && !tokenSequence.movePrevious()) {
                    resultSet.finish ();
                    return;
                }
                String mimeType = tokenSequence.language ().mimeType ();
                Language language = LanguagesManager.getDefault ().getLanguage (mimeType);
                Token token = tokenSequence.token ();
                int tokenOffset = tokenSequence.offset();
                String tokenType = token.id ().name ();
                Feature feature = language.getFeature (Language.COMPLETION, tokenType);
                boolean doNotUsePrefix = feature != null && "true".equals(feature.getValue("doNotUsePrefix"));
                if (doNotUsePrefix && tokenOffset + token.length() > offset && 
                        token.text().toString().trim().length() > 0) {
                    resultSet.finish ();
                    return;
                }
                String start = doNotUsePrefix ? "" :
                    token.text().toString ().substring (0, offset - tokenOffset).trim ();
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
            } catch (ParseException e) {
            }
        }

        public void cancel () {
        }
        
        private void compute (Result resultSet) {
            doc = component.getDocument ();
            TokenHierarchy tokenHierarchy = TokenHierarchy.get (doc);
            TokenSequence tokenSequence = tokenHierarchy.tokenSequence ();
            int offset = component.getCaret ().getDot ();
            compute (tokenSequence, offset, resultSet, doc);
            addParserTags (resultSet);
        }
        
        private void compute (
            TokenSequence       tokenSequence, 
            int                 offset, 
            Result              resultSet,
            Document            doc
        ) {
            if (tokenSequence == null) return;
            String mimeType = tokenSequence.language ().mimeType ();
            Feature feature = null;
            String start = null;
            try {
                Language language = LanguagesManager.getDefault ().
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
                String tokenType = token.id ().name ();
                feature = language.getFeature (Language.COMPLETION, tokenType);
                boolean doNotUsePrefix = feature != null && "true".equals(feature.getValue("doNotUsePrefix"));
                int tokenOffset = tokenSequence.offset();
                if (doNotUsePrefix && tokenOffset + token.length() > offset &&
                        token.text().toString().trim().length() > 0) {
                    return;
                }
                start = doNotUsePrefix ? "" : start.substring (0, offset - tokenOffset).trim ();
                ignoreCase = false;
                Feature f = language.getFeature ("PROPERTIES");
                if (f != null)
                    ignoreCase = f.getBoolean ("ignoreCase", false);
                if (ignoreCase) start = start.toLowerCase ();
            } catch (ParseException ex) {
            }
            if (feature == null)
                compute (tokenSequence.embedded (), offset, resultSet, doc);
            else
                addTags (feature, start, Context.create (doc, tokenSequence), resultSet);
        }

        private void addParserTags (final Result resultSet) {
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
        
        private void addParserTags (ASTNode node, Result resultSet) {
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
            Language lang = null;
            try {
                lang = LanguagesManager.getDefault ().getLanguage (token.getMimeType());
            } catch (ParseException e) {
            }
            String tokenType = token.getType();
            Feature f = lang.getFeature (Language.COMPLETION, tokenType);
            int tokenOffset = token.getOffset();
            boolean doNotUsePrefix = f != null && "true".equals(f.getValue("doNotUsePrefix"));
            if (doNotUsePrefix && tokenOffset + token.getLength() > offset &&
                    token.getIdentifier().length() > 0) {
                return;
            }
            String start = doNotUsePrefix ? "" : 
                token.getIdentifier ().substring (0, offset - tokenOffset).trim ();
            
            //S ystem.out.println("CodeCompletion: (syntax) start=" + start + ": stoken=" + token);
            
            for (int i = path.size () - 1; i >= 0; i--) {
                ASTItem item = path.get (i);
                try {
                    Language language = LanguagesManager.getDefault ().
                        getLanguage (item.getMimeType ());
                    Feature feature = language.getFeature (COMPLETION, path.subPath (i));
                    if (feature != null)
                        addTags (feature, start, SyntaxContext.create (doc, path.subPath (i)), resultSet);
                } catch (ParseException ex) {
                }
            }
        }

        private void addTags (Feature feature, String start, Context context, Result resultSet) {
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
            Result              resultSet, 
            String              start
        ) {
            Iterator it = keys.iterator ();
            while (it.hasNext ()) {
                Object o = it.next ();
                if (o instanceof org.netbeans.api.languages.CompletionItem)
                    o = new CompletionSupport (
                        (org.netbeans.api.languages.CompletionItem) o
                    );
                CompletionItem item = (CompletionItem) o;
                items.add (item);
                CharSequence chs = item.getInsertPrefix ();
                String s = chs instanceof String ? (String) chs : chs.toString ();
                if (ignoreCase)
                    s = s.toLowerCase ();
                if (s.startsWith (start))
                    resultSet.addItem (item);
            }
        }
        
        private void addTags (
            String              text, 
            Feature             feature, 
            int                 j, 
            String              start, 
            Result              resultSet
        ) {
            if (ignoreCase)
                text = text.toLowerCase ();
            String description = (String) feature.getValue ("description" + j);
            if (description == null)
                description = text;
            String icon = (String) feature.getValue ("icon" + j);
            CompletionItem item = new CompletionSupport (
                text, description, null, icon, 2
            );
            items.add (item);
            if (!text.startsWith (start))
                return;
            resultSet.addItem (item);
        }
    }
    
    private static interface Result {
        void addItem (CompletionItem item);
        void finish ();
    }
    
    private static class CompletionResult implements Result {
        private CompletionResultSet resultSet;
        
        CompletionResult (CompletionResultSet resultSet) {
            this.resultSet = resultSet;
        }
        
        public void addItem (CompletionItem item) {
            resultSet.addItem (item);
        }
        
        public void finish () {
            resultSet.finish ();
        }
    }
    
    private static class ListResult implements Result {
        private List<CompletionItem> result = new ArrayList<CompletionItem> ();
        private boolean finished = false;
        private Object LOCK = new Object ();
        
        public void addItem (CompletionItem item) {
            result.add (item);
        }
        
        public void finish () {
            finished = true;
            synchronized (LOCK) {
                LOCK.notify ();
            }
        }
        
        void waitFinished () {
            if (finished) return;
            synchronized (LOCK) {
                try {
                    LOCK.wait ();
                } catch (InterruptedException ex) {
                }
            }
        }
        public List<CompletionItem> getList () {
            return result;
        }
    }
}


