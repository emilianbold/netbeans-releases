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

package org.netbeans.modules.languages.javascript;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.CharInput;
import org.netbeans.api.languages.LanguageDefinitionNotFoundException;
import org.netbeans.api.languages.LibrarySupport;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.api.languages.Context;
import org.netbeans.api.languages.LibrarySupport;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.modules.languages.javascript.api.JSFunction;
import org.netbeans.modules.languages.javascript.api.JSItem;
import org.netbeans.modules.languages.javascript.api.JSRoot;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.cookies.SaveCookie;
import org.openide.loaders.DataObject;
import org.openide.text.Line;
import org.openide.text.NbDocument;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledDocument;
import org.netbeans.api.languages.CompletionItem;
import org.netbeans.modules.languages.javascript.api.JSParser;
import org.netbeans.modules.languages.javascript.api.JSVariable;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;


/**
 *
 * @author Jan Jancura, Dan Prusa
 */
public class JavaScript {

    private static final String DOC = "org/netbeans/modules/languages/javascript/Documentation.xml";
    private static final String DOM1 = "org/netbeans/modules/languages/javascript/DOM1.xml";
    private static final String DOM2 = "org/netbeans/modules/languages/javascript/DOM2.xml";
    private static final String MIME_TYPE = "text/javascript";
    
    private static Set regExp = new HashSet ();
    static {
        regExp.add (new Integer (','));
        regExp.add (new Integer (')'));
        regExp.add (new Integer (';'));
    }
    
    public static Object[] parseRegularExpression (CharInput input) {
        if (input.read () != '/')
            throw new InternalError ();
        int start = input.getIndex ();
        while (!input.eof () &&
                input.next () != '/'
        ) {
            if (input.next () == '\r' ||
                input.next () == '\n'
            ) {
                input.setIndex (start);
                return new Object[] {
                    ASTToken.create (MIME_TYPE, "js_operator", "", 0),
                    null
                };
            }
            if (input.next () == '\\')
                input.read ();
            input.read ();
        }
        while (input.next () == '/') input.read ();
        while (!input.eof ()) {
            int ch = input.next ();
            if (ch != 'g' && ch != 'i' && ch != 'm')
                break;
            input.read ();
        }
        int end = input.getIndex ();
        char car = input.eof() ? 0 : input.next();
        boolean newLineDetected = false;
        while (
            !input.eof () && (
                car == ' ' ||
                car == '\t' ||
                car == '\n' ||
                car == '\r'
            )
        ) {
            newLineDetected = newLineDetected || car == '\n';
            input.read ();
            if (!input.eof()) {
                car = input.next();
            }
        }
        if (
            !input.eof () && 
            input.next () == '.'
        ) {
            int h = input.getIndex ();
            input.read ();
            if (input.next () >= '0' &&
                input.next () <= '9'
            ) {
                input.setIndex (start);
                return new Object[] {
                    ASTToken.create (MIME_TYPE, "js_operator", "", 0),
                    null
                };
            } else {
                input.setIndex (end);
                return new Object[] {
                    ASTToken.create (MIME_TYPE, "js_regularExpression", "", 0),
                    null
                };
            }
        }
        if (
            newLineDetected || (!input.eof () && regExp.contains (new Integer (input.next ())))
        ) {
            input.setIndex (end);
            return new Object[] {
                ASTToken.create (MIME_TYPE, "js_regularExpression", "", 0),
                null
            };
        }
        input.setIndex (start);
        return new Object[] {
            ASTToken.create (MIME_TYPE, "js_operator", "", 0),
            null
        };
    }

    public static Runnable hyperlink (Context context) {
        if (!(context instanceof SyntaxContext)) return null;
        NbEditorDocument doc = (NbEditorDocument) context.getDocument ();
        SyntaxContext scontext = (SyntaxContext) context;
        ASTPath path = scontext.getASTPath ();
        JSItem item = getDeclaration (path);
        if (item == null) return null;
        int offset = item.getOffset();
        DataObject dobj = NbEditorUtilities.getDataObject (doc);
        LineCookie lc = (LineCookie)dobj.getCookie(LineCookie.class);
        Line.Set lineSet = lc.getLineSet();
        final Line line = lineSet.getCurrent (NbDocument.findLineNumber (doc, offset));
        final int column = NbDocument.findLineColumn (doc, offset);
        return new Runnable () {
            public void run () {
                line.show (Line.SHOW_GOTO, column);
            }
        };
    }
    
    public static String functionName (SyntaxContext context) {
        ASTPath path = context.getASTPath ();
        ASTNode n = (ASTNode) path.getLeaf ();
        String name = null;
        ASTNode nameNode = n.getNode ("FunctionName");
        if (nameNode != null)
            name = nameNode.getAsText ();
        String parameters = "";
        ASTNode parametersNode = n.getNode ("FormalParameterList");
        if (parametersNode != null)
            parameters = parametersNode.getAsText ();
        if (name != null) return name + " (" + parameters + ")";
        
        ListIterator<ASTItem> it = path.listIterator (path.size () - 1);
        while (it.hasPrevious ()) {
            ASTItem item = it.previous ();
            if (item instanceof ASTToken) break;
            ASTNode p = (ASTNode) item;
            if (p.getNT ().equals ("AssignmentExpressionInitial") &&
                p.getNode ("AssignmentOperator") != null
            ) {
                return ((ASTNode) p.getChildren ().get (0)).getAsText () + 
                    " (" + getAsText (n.getNode ("FormalParameterList")) + ")";
            }
            if (p.getNT ().equals ("PropertyNameAndValue")) {
                return p.getNode ("PropertyName").getAsText () + 
                    " (" + getAsText (n.getNode ("FormalParameterList")) + ")";
            }
        }
        return "?";
    }

    public static String objectName (SyntaxContext context) {
        ASTPath path = context.getASTPath ();
        ASTNode n = (ASTNode) path.getLeaf ();
        ListIterator<ASTItem> it = path.listIterator (path.size ());
        while (it.hasPrevious ()) {
            ASTItem item = it.previous ();
            if (item instanceof ASTToken) break;
            ASTNode p = (ASTNode) item;
            if (p.getNT ().equals ("AssignmentExpressionInitial") &&
                p.getNode ("AssignmentOperator") != null
            ) {
                return ((ASTNode) p.getChildren ().get (0)).getAsText ();
            }
            if (p.getNT ().equals ("PropertyNameAndValue")) {
                return p.getNode ("PropertyName").getAsText ();
            }
        }
        return "?";
    }
    
    
    // code completion .........................................................
    
    public static List completionItems (Context context) {
        if (context instanceof SyntaxContext) {
            SyntaxContext syntaxContext = (SyntaxContext) context;
            ASTPath path = ((SyntaxContext) context).getASTPath ();
            Document doc = syntaxContext.getDocument ();
            Map<String,CompletionItem> members = getMembers (
                path,
                getDocumentName (doc)
            );
            List<CompletionItem> result = new ArrayList<CompletionItem> ();
            result.addAll (members.values ());
            FileObject fo = NbEditorUtilities.getFileObject (doc);
            //Collection<CompletionItem> globals = Index.getGlobalItems (fo, members.keySet ());
            //result.addAll (globals);
            return result;
        }
        
        List result = new ArrayList ();
        TokenSequence ts = context.getTokenSequence ();
        Token token = ts.token ();
        String tokenText = token.text ().toString ();
        String libraryContext = null;
        if (token.id ().name ().endsWith ("whitespace") ) {
            token = previousToken (ts);
            if (token.text ().toString ().equals ("new")) {
                result.addAll (getLibrary ().getCompletionItems ("constructor"));
                return result;
            }
        }
        if (tokenText.equals (".")) {
            token = previousToken (ts);
            if (token.id ().name ().endsWith ("identifier"))
                libraryContext = token.text ().toString ();
        } else
        if (token.id ().name ().endsWith ("identifier") ) {
            token = previousToken (ts);
            if (token.text ().toString ().equals (".")) {
                token = previousToken (ts);
                if (token.id ().name ().endsWith ("identifier"))
                    libraryContext = token.text ().toString ();
            } else
            if (token.text ().toString ().equals ("new")) {
                result.addAll (getLibrary ().getCompletionItems ("constructor"));
                return result;
            }
        }
        
        if (libraryContext != null) {
            result.addAll (getLibrary ().getCompletionItems (libraryContext));
            result.addAll (getLibrary ().getCompletionItems ("member"));
        } else
            result.addAll (getLibrary ().getCompletionItems ("root"));
        return result;
    }
    
    private static Token previousToken (TokenSequence ts) {
        do {
            if (!ts.movePrevious ()) return ts.token ();
        } while (ts.token ().id ().name ().endsWith ("whitespace"));
        return ts.token ();
    }
    
    
    // actions .................................................................
    
    public static void performDeleteCurrentMethod (ASTNode node, JTextComponent comp) {
        NbEditorDocument doc = (NbEditorDocument)comp.getDocument();
        int position = comp.getCaretPosition();
        ASTPath path = node.findPath(position);
        ASTNode methodNode = null;
        for (Iterator iter = path.listIterator(); iter.hasNext(); ) {
            Object obj = iter.next();
            if (!(obj instanceof ASTNode))
                break;
            ASTNode n = (ASTNode) obj;
            if ("FunctionDeclaration".equals(n.getNT())) { // NOI18N
                methodNode = n;
            } // if
        } // for
        if (methodNode != null) {
            try {
                doc.remove(methodNode.getOffset(), methodNode.getLength());
            } catch (BadLocationException e) {
                ErrorManager.getDefault().notify(e);
            }
        }
    }
     
    public static boolean enabledDeleteCurrentMethod (ASTNode node, JTextComponent comp) {
        NbEditorDocument doc = (NbEditorDocument)comp.getDocument();
        int position = comp.getCaretPosition();
        ASTPath path = node.findPath(position);
        if (path == null) return false;
        for (Iterator iter = path.listIterator(); iter.hasNext(); ) {
            Object obj = iter.next();
            if (!(obj instanceof ASTNode))
                return false;
            ASTNode n = (ASTNode) obj;
            if ("FunctionDeclaration".equals(n.getNT())) { // NOI18N
                return true;
            } // if
        } // for
        return false;
    }
    
    public static void performRun (ASTNode node, JTextComponent comp) {
        ClassLoader cl = JavaScript.class.getClassLoader ();
        try {
//        ScriptEngineManager manager = new ScriptEngineManager ();
//        ScriptEngine engine = manager.getEngineByMimeType ("text/javascript");
            Class managerClass = cl.loadClass ("javax.script.ScriptEngineManager");
            Object manager = managerClass.newInstance();
            Method getEngineByMimeType = managerClass.getMethod ("getEngineByMimeType", new Class[] {String.class});
            Object engine = getEngineByMimeType.invoke (manager, new Object[] {"text/javascript"});
            
            Document doc = comp.getDocument ();
            DataObject dob = NbEditorUtilities.getDataObject (doc);
            String name = dob.getPrimaryFile ().getNameExt ();
            SaveCookie saveCookie = (SaveCookie) dob.getLookup ().lookup (SaveCookie.class);
            if (saveCookie != null)
                try {
                    saveCookie.save ();
                } catch (IOException ex) {
                    ErrorManager.getDefault ().notify (ex);
                }
            
//            ScriptContext context = engine.getContext ();
            Class engineClass = cl.loadClass ("javax.script.ScriptEngine");
            Method getContext = engineClass.getMethod ("getContext", new Class[] {});
            Object context = getContext.invoke (engine, new Object[] {});
            
            InputOutput io = IOProvider.getDefault ().getIO ("Run " + name, false);
            
//            context.setWriter (io.getOut ());
//            context.setErrorWriter (io.getErr ());
//            context.setReader (io.getIn ());
            Class contextClass = cl.loadClass("javax.script.ScriptContext");
            Method setWriter = contextClass.getMethod ("setWriter", new Class[] {Writer.class});
            Method setErrorWriter = contextClass.getMethod ("setErrorWriter", new Class[] {Writer.class});
            Method setReader = contextClass.getMethod ("setReader", new Class[] {Reader.class});
            setWriter.invoke (context, new Object[] {io.getOut ()});
            setErrorWriter.invoke (context, new Object[] {io.getErr ()});
            setReader.invoke (context, new Object[] {io.getIn ()});
            
            io.getOut().reset ();
            io.getErr ().reset ();
            io.select ();
            
//            Object o = engine.eval (doc.getText (0, doc.getLength ()));
            Method eval = engineClass.getMethod ("eval", new Class[] {String.class});
            Object o = eval.invoke (engine, new Object[] {doc.getText (0, doc.getLength ())});
            
            if (o != null)
                DialogDisplayer.getDefault ().notify (new NotifyDescriptor.Message ("Result: " + o));
            
        } catch (InvocationTargetException ex) {
            try {
                Class scriptExceptionClass = cl.loadClass("javax.script.ScriptException");
                if (ex.getCause () != null && 
                    scriptExceptionClass.isAssignableFrom (ex.getCause ().getClass ())
                )
                    DialogDisplayer.getDefault ().notify (new NotifyDescriptor.Message (ex.getCause ().getMessage ()));
                else
                    ErrorManager.getDefault ().notify (ex);
            } catch (Exception ex2) {
                ErrorManager.getDefault ().notify (ex2);
            }
        } catch (Exception ex) {
            ErrorManager.getDefault ().notify (ex);
        }
//        ScriptEngineManager manager = new ScriptEngineManager ();
//        ScriptEngine engine = manager.getEngineByMimeType ("text/javascript");
//        Document doc = comp.getDocument ();
//        DataObject dob = NbEditorUtilities.getDataObject (doc);
//        String name = dob.getPrimaryFile ().getNameExt ();
//        SaveCookie saveCookie = (SaveCookie) dob.getLookup ().lookup (SaveCookie.class);
//        if (saveCookie != null)
//            try {
//                saveCookie.save ();
//            } catch (IOException ex) {
//                ErrorManager.getDefault ().notify (ex);
//            }
//        try {
//            ScriptContext context = engine.getContext ();
//            InputOutput io = IOProvider.getDefault ().getIO ("Run " + name, false);
//            context.setWriter (io.getOut ());
//            context.setErrorWriter (io.getErr ());
//            context.setReader (io.getIn ());
//            io.select ();
//            Object o = engine.eval (doc.getText (0, doc.getLength ()));
//            if (o != null)
//                DialogDisplayer.getDefault ().notify (new NotifyDescriptor.Message ("Result: " + o));
//        } catch (BadLocationException ex) {
//            ErrorManager.getDefault ().notify (ex);
//        } catch (ScriptException ex) {
//            DialogDisplayer.getDefault ().notify (new NotifyDescriptor.Message (ex.getMessage ()));
//        }
    }

    public static boolean enabledRun (ASTNode node, JTextComponent comp) {
        try {
            ClassLoader cl = JavaScript.class.getClassLoader ();
            Class managerClass = cl.loadClass ("javax.script.ScriptEngineManager");

            return managerClass != null;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }
    
    public static void performGoToDeclaration (ASTNode node, JTextComponent comp) {
        NbEditorDocument doc = (NbEditorDocument)comp.getDocument();
        int position = comp.getCaretPosition();
        ASTPath path = node.findPath(position);
        JSItem item = getDeclaration (path);
        if (item == null) return;
        int offset = item.getOffset();
        DataObject dobj = NbEditorUtilities.getDataObject (doc);
        LineCookie lc = (LineCookie)dobj.getCookie(LineCookie.class);
        Line.Set lineSet = lc.getLineSet();
        Line line = lineSet.getCurrent(NbDocument.findLineNumber(doc, offset));
        int column = NbDocument.findLineColumn (doc, offset);
        line.show (Line.SHOW_GOTO, column);
    }
     
    public static boolean enabledGoToDeclaration (ASTNode node, JTextComponent comp) {
        NbEditorDocument doc = (NbEditorDocument)comp.getDocument();
        int position = comp.getCaretPosition();
        ASTPath path = node.findPath(position);
        JSItem item = getDeclaration (path);
        return item != null;
    }
    
    public static boolean isFunctionParameter (Context context) {
        return isVariable (context, JSVariable.Type.PARAMETER);
    }
    
    public static boolean isLocalVariable(Context context) {
        return isVariable (context, JSVariable.Type.LOCAL);
    }

    
    // helper methods ..........................................................
    
    private static JSParser jsParser;
    private static Map<ASTNode,JSRoot> jsParsers = new WeakHashMap<ASTNode,JSRoot> ();
    
    public static JSRoot getRoot (ASTNode ast) {
        JSRoot result = jsParsers.get (ast);
        if (result != null)
            return result;
        if (jsParser == null)
            try {
                jsParser = JSParser.create ();
            } catch (LanguageDefinitionNotFoundException ex) {
            }
        result = jsParser.process (ast);
        jsParsers.put (ast, result);
        return result;
    }
    
    public static JSItem getDeclaration (ASTPath path) {
        ASTItem leaf = path.getLeaf();
        if (!(leaf instanceof ASTToken))
            return null;
        ASTToken token = (ASTToken) leaf;
        ASTNode ast = (ASTNode) path.getRoot ();
        JSRoot root = JavaScript.getRoot (ast);
        return root.getItem (token.getIdentifier (), token.getOffset ());
    }
    
    private static Map<String,CompletionItem> getMembers (ASTPath path, String title) {
        Map<String,CompletionItem> result = new HashMap<String,CompletionItem> ();
        JSRoot root = getRoot ((ASTNode) path.get (0));
        Collection<JSItem> c = root.getItems (path.getLeaf ().getOffset ());
        Iterator<JSItem> it = c.iterator ();
        while (it.hasNext ()) {
            JSItem item =  it.next ();
            if (item instanceof JSVariable) {
                JSVariable v = (JSVariable) item;
                result.put (
                    v.getName (),
                    toCompletionItem (v, title)
                );
            } else
            if (item instanceof JSFunction) {
                JSFunction f = (JSFunction) item;
                result.put (
                    f.getName (),
                    toCompletionItem (f, title)
                );
            }
        }
        return result;
    }
    
    static CompletionItem toCompletionItem (JSItem item, String title) {
        if (item instanceof JSVariable) {
            JSVariable v = (JSVariable) item;
            CompletionItem.Type type = null;
            switch (v.getType ()) {
            case LOCAL:
                type = CompletionItem.Type.LOCAL;
                break;
            case PARAMETER:
                type = CompletionItem.Type.PARAMETER;
                break;
            case GLOBAL:
                type = CompletionItem.Type.FIELD;
                break;
            }
            return CompletionItem.create (
                v.getName (),
                null,
                title,
                type,
                1
            );
        } else
        if (item instanceof JSFunction) {
            JSFunction f = (JSFunction) item;
            return CompletionItem.create (
                f.getName (),
                null,
                title,
                CompletionItem.Type.METHOD,
                1
            );
        }
        return null;
    }
    
    private static String getDocumentName (Document doc) {
        String name = (String) doc.getProperty ("title");
        if (name == null) return null;
        int i = name.lastIndexOf (File.separatorChar);
        if (i > 0)
            return name.substring (i + 1);
        return name;
    }
    
    private static boolean isVariable(Context context, JSVariable.Type type) {
        if (!(context instanceof SyntaxContext)) {
            return false;
        }
        SyntaxContext scontext = (SyntaxContext)context;
        ASTPath path = scontext.getASTPath ();
        JSItem item = getDeclaration (path);
        return (item instanceof JSVariable) &&
               ((JSVariable) item).getType () == type;
    }
    
    private static LibrarySupport library;
    
    private static LibrarySupport getLibrary () {
        if (library == null)
            library = LibrarySupport.create (
                Arrays.asList (new String[] {DOC, DOM1, DOM2})
            );
        return library;
    }

    private static TokenSequence getTokenSequence (Document doc, Caret caret) {
        int ln = NbDocument.findLineNumber ((StyledDocument) doc, caret.getDot ()) - 1;
        int start = NbDocument.findLineOffset ((StyledDocument) doc, ln);
        TokenHierarchy th = TokenHierarchy.get (doc);
        TokenSequence ts = th.tokenSequence ();
        ts.move (start);
        return ts;
    }
    
    private static void indent (Document doc, Caret caret, int i) {
        StringBuilder sb = new StringBuilder ();
        while (i > 0) {
            sb.append (' ');i--;
        }
        try {
            doc.insertString (caret.getDot (), sb.toString (), null);
        } catch (BadLocationException ex) {
            ErrorManager.getDefault ().notify (ex);
        }
    }
    
    private static int getIndent (TokenSequence ts) {
        if (ts.token ().id ().name ().equals ("js_whitespace")) {
            String w = ts.token ().text ().toString ();
            int i = w.lastIndexOf ('\n');
            if (i >= 0)
                w = w.substring (i + 1);
            i = w.lastIndexOf ('\r');
            if (i >= 0)
                w = w.substring (i + 1);
            return w.length ();
        }
        return 0;
    }
    
    private static String getAsText (ASTNode n) {
        if (n == null) return "";
        return n.getAsText ();
    }
}
