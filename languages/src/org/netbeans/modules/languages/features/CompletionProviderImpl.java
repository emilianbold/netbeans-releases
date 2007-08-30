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

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.List;
import javax.swing.text.JTextComponent;
import javax.swing.text.Document;

import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.CompletionItem.Type;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.ParserManager;
import org.netbeans.api.languages.ParserManager.State;
import org.netbeans.api.languages.ParserManagerListener;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.LanguageDefinitionNotFoundException;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.languages.Context;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManager;
import org.netbeans.modules.languages.LanguagesManager;
import org.netbeans.modules.languages.ParserManagerImpl;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionProvider;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.project.ActionProvider;

import org.openide.ErrorManager;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;


/**
 *
 * @author Jan Jancura
 */
public class CompletionProviderImpl implements CompletionProvider {
    
    static final String COMPLETION = "COMPLETION";
    
    /**
     * Append text after current end of token. This type of cc is used
     * for comments.
     */
    static final String COMPLETION_APPEND = "append";
    
    /**
     * Inserts text into current token, no prefix used. Used for whitespaces, 
     * operators.
     */
    static final String COMPLETION_INSERT = "insert";
    
    /**
     * Inserts text into current token, with current prefix. Used for keywords
     * and identifiers.
     */
    static final String COMPLETION_COMPLETE = "complete";
    
    
    public CompletionTask createTask (int queryType, JTextComponent component) {
        return new CompletionTaskImpl (component);
    }

    public int getAutoQueryTypes (JTextComponent component, String typedText) {
        if (".".equals(typedText)) { // NOI18N
            Document doc = component.getDocument ();
            TokenHierarchy tokenHierarchy = TokenHierarchy.get (doc);
            if (doc instanceof NbEditorDocument)
                ((NbEditorDocument) doc).readLock ();
            try {
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
            } finally {
                if (doc instanceof NbEditorDocument)
                    ((NbEditorDocument) doc).readUnlock ();
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

    private static TokenSequence getInnerTokenSequence (
        TokenHierarchy  tokenHierarchy,
        int             offset
    ) {
        TokenSequence tokenSequence = tokenHierarchy.tokenSequence ();
        tokenSequence.move (offset - 1);
        while (tokenSequence.moveNext ()) {
            TokenSequence ts = tokenSequence.embedded ();
            if (ts == null) break;
            tokenSequence = ts;
        }
        return tokenSequence;
    }
    
    
    // innerclasses ............................................................
    
    private static class CompletionTaskImpl implements CompletionTask {
        
        private JTextComponent          component;
        private Document                doc;
        private FileObject              fileObject;
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
            if (resultSet == null) return;
            doc = component.getDocument ();
            fileObject = NbEditorUtilities.getFileObject (doc);
            TokenHierarchy tokenHierarchy = TokenHierarchy.get (doc);
            if (doc instanceof NbEditorDocument)
                ((NbEditorDocument) doc).readLock ();
            try {
                TokenSequence tokenSequence = getInnerTokenSequence (
                    tokenHierarchy,
                    component.getCaret ().getDot ()
                );
                Token token = tokenSequence.token ();
                if (token != null) {
                    String start = token.text ().toString ();

                    Iterator<CompletionItem> it = items.iterator ();
                    while (it.hasNext ()) {
                        CompletionItem completionItem = it.next ();
                        String text = completionItem.getInsertPrefix ().toString ();
                        if (text.startsWith (start))
                            resultSet.addItem (completionItem);
                    }
                }
            } finally {
                if (doc instanceof NbEditorDocument)
                    ((NbEditorDocument) doc).readUnlock ();
            }
            resultSet.finish ();
        }

        public void cancel () {
        }
        
        private void compute (Result resultSet) {
            doc = component.getDocument ();
            fileObject = NbEditorUtilities.getFileObject (doc);
            TokenHierarchy tokenHierarchy = TokenHierarchy.get (doc);
            boolean finishSet = true;
            if (doc instanceof NbEditorDocument) {
                ((NbEditorDocument) doc).readLock ();
            }
            try {
                int offset = component.getCaret ().getDot ();
                TokenSequence tokenSequence = getInnerTokenSequence (
                    tokenHierarchy,
                    component.getCaret ().getDot ()
                );
                if (!tokenSequence.isEmpty() && tokenSequence.offset () > offset) {
                    // border of embedded language
                    // [HACK] borders should be represented by some tokens!!!
                    return;
                }
                String mimeType = tokenSequence.language ().mimeType ();
                Language language = LanguagesManager.getDefault ().
                    getLanguage (mimeType);
                if (!tokenSequence.isEmpty()) {
                    compute (tokenSequence, offset, resultSet, doc, language);
                }
                finishSet = addParserTags (resultSet, language);
            } catch (LanguageDefinitionNotFoundException ex) {
                // do nothing
            } finally {
                if (doc instanceof NbEditorDocument) {
                    ((NbEditorDocument) doc).readUnlock ();
                }
            }
            if (finishSet) {
                resultSet.finish ();
            }
        }
    
        private String getCompletionType (Feature feature, String tokenType) {
            String projectType = (String) feature.getValue("project_type");
            if (projectType != null) {
                if (fileObject == null) return null;
                Project p = FileOwnerQuery.getOwner (fileObject);
                if (p == null) return null;
                Object o = p.getLookup ().lookup (ActionProvider.class);
                if (o == null) return null;
                if (o.getClass ().getName ().indexOf (projectType) < 0)
                    return null;
            }
            String completionType = (String) feature.getValue ("type");
            if (completionType != null) return completionType;
            if (tokenType.indexOf ("whitespace") >= 0 ||
                tokenType.indexOf ("operator") >= 0 || 
                tokenType.indexOf ("separator") >= 0
            )
                return COMPLETION_INSERT;
            else
            if (tokenType.indexOf ("comment") >= 0)
                return COMPLETION_APPEND;
            return COMPLETION_COMPLETE;
        }
                
        private void compute (
            TokenSequence       tokenSequence, 
            int                 offset, 
            Result              resultSet,
            Document            doc,
            Language            language
        ) {
            String start = null;
            Token token = tokenSequence.token ();
            start = token.text ().toString ();
            String tokenType = token.id ().name ();
            List<Feature> features = language.getFeatures (COMPLETION, tokenType);
            Iterator<Feature> it = features.iterator ();
            while (it.hasNext ()) {
                Feature feature =  it.next ();
                String completionType = getCompletionType (feature, tokenType);
                int tokenOffset = tokenSequence.offset();
                if (completionType == null) continue;
                if (COMPLETION_APPEND.equals (completionType) && 
                    offset < tokenOffset + token.length ()
                ) 
                    continue;
                start = COMPLETION_COMPLETE.equals (completionType) ? 
                    start.substring (0, offset - tokenOffset).trim () :
                    "";
                ignoreCase = false;
                Feature f = language.getFeature ("PROPERTIES");
                if (f != null)
                    ignoreCase = f.getBoolean ("ignoreCase", false);
                if (ignoreCase) start = start.toLowerCase ();
                addTags (feature, start, Context.create (doc, tokenSequence), resultSet);
            }
        }

        private boolean addParserTags (final Result resultSet, final Language language) {
            final ParserManager parserManager = ParserManager.get (doc);
            if (parserManager.getState () == State.PARSING) {
                //S ystem.out.println("CodeCompletion: parsing...");
                parserManager.addListener (new ParserManagerListener () {
                    public void parsed (State state, ASTNode ast) {
                        //S ystem.out.println("CodeCompletion: parsed " + state);
                        if (resultSet.isFinished ()) return;
                        parserManager.removeListener (this);
                        addParserTags (ast, resultSet, language);
                        resultSet.finish ();
                    }
                });
                return false;
            } else {
                try {
                    addParserTags (ParserManagerImpl.get (doc).getAST (), resultSet, language);
                } catch (ParseException ex) {
                    ErrorManager.getDefault ().notify (ex);
                }
                return true;
            }
        }
        
        private void addParserTags (ASTNode node, Result resultSet, Language language) {
            if (node == null) {
                //S ystem.out.println("CodeCompletion: No AST");
                return;
            }
            int offset = component.getCaret ().getDot ();
            ASTPath path = node.findPath (offset - 1);
            if (path == null) return;
            ASTItem item = path.getLeaf ();
            if (item instanceof ASTNode) return;
            ASTToken token = (ASTToken) item;
            if (token.getLength () != token.getIdentifier ().length ()) {
                // [HACK]
                // something like token.getRealIndex () + 
                // add tokens for language borders...
                return;
            }
            String tokenType = token.getType ();
            int tokenOffset = token.getOffset ();

            for (int i = path.size () - 1; i >= 0; i--) {
                item = path.get (i);
                if (!item.getMimeType ().equals(language.getMimeType ())) break;
                List<Feature> features = language.getFeatures (COMPLETION, path.subPath (i));
                Iterator<Feature> it2 = features.iterator ();
                while (it2.hasNext ()) {
                    Feature feature =  it2.next ();
                    String completionType = getCompletionType (feature, tokenType);
                    if (completionType == null) continue;
                    if (COMPLETION_APPEND.equals (completionType) && 
                        offset < tokenOffset + token.getLength ()
                    ) continue;
                    String start = COMPLETION_COMPLETE.equals (completionType) ? 
                        token.getIdentifier ().substring (0, offset - tokenOffset).trim () :
                        "";
                    addTags (feature, start, SyntaxContext.create (doc, path.subPath (i)), resultSet);
                }
            }
            
            DatabaseContext context = DatabaseManager.getRoot (node);
            if (context == null) return;
            List<DatabaseDefinition> definitions = context.getAllVisibleDefinitions (offset);
            String start = token.getIdentifier ().substring (0, offset - tokenOffset).trim ();
            Set<String> names = new HashSet<String> ();
            Iterator<DatabaseDefinition> it = definitions.iterator ();
            while (it.hasNext ()) {
                DatabaseDefinition definition =  it.next ();
                names.add (definition.getName ());
                CompletionSupport cs = createCompletionItem (definition, getFileName (), start);
                items.add (cs);
                if (definition.getName ().startsWith (start))
                    resultSet.addItem (cs);
            }
            try {
                if (fileObject != null) {
                    Map<FileObject,List<DatabaseDefinition>> globals = Index.getGlobalItems (fileObject, true);
                    Iterator<FileObject> it1 = globals.keySet ().iterator ();
                    while (it1.hasNext()) {
                        FileObject fileObject =  it1.next();
                        List<DatabaseDefinition> l = globals.get (fileObject);
                        Iterator<DatabaseDefinition> it2 = l.iterator ();
                        while (it2.hasNext()) {
                            DatabaseDefinition definition =  it2.next();
                            if (names.contains (definition.getName ())) continue;
                            CompletionSupport cs = createCompletionItem (definition, fileObject.getNameExt (), start);
                            items.add (cs);
                            if (definition.getName ().startsWith (start))
                                resultSet.addItem (cs);
                        }

                    }
                }
            } catch (FileNotParsedException ex) {
                ex.printStackTrace ();
            }
        }
        
        private String fileName;
        
        private String getFileName () {
            if (fileName == null) {
                fileName = (String) doc.getProperty ("title");
                if (fileName == null) return null;
                int i = fileName.lastIndexOf (File.separatorChar);
                if (i > 0)
                    fileName = fileName.substring (i + 1);
            }
            return fileName;
        }

        private void addTags (Feature feature, String start, Context context, Result resultSet) {
            int j = 1;
            while (true) {
                if (context instanceof SyntaxContext &&
                    feature.getType ("text" + j) == Feature.Type.STRING &&
                    ((SyntaxContext) context).getASTPath ().getLeaf () instanceof ASTToken
                ) {
                    j++;
                    continue;
                }
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
                        (org.netbeans.api.languages.CompletionItem) o,
                        start
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
                text, start, description, null, icon, 2
            );
            items.add (item);
            if (!text.startsWith (start))
                return;
            resultSet.addItem (item);
        }

        private static CompletionSupport createCompletionItem (DatabaseDefinition definition, String fileName,
                String start) {
            Type type = null;
            if ("local".equals (definition.getType ()))
                type = Type.LOCAL;
            else
            if ("parameter".equals (definition.getType ()))
                type = Type.PARAMETER;
            else
            if ("field".equals (definition.getType ()))
                type = Type.FIELD;
            else
            if ("method".equals (definition.getType ()))
                type = Type.METHOD;
            return new CompletionSupport (new org.netbeans.api.languages.CompletionItem (
                definition.getName (),
                null,
                fileName,
                type,
                100
            ), start);
        }
    }
    
    private static interface Result {
        void addItem (CompletionItem item);
        void finish ();
        boolean isFinished ();
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
        
        public boolean isFinished () {
            return resultSet.isFinished ();
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
        
        public boolean isFinished () {
            return finished;
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


