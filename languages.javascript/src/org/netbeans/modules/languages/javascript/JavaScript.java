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

import org.netbeans.api.languages.TokenInput;
import org.netbeans.api.languages.DatabaseManager;
import org.netbeans.api.languages.LibrarySupport;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.PTPath;
import org.netbeans.api.languages.SyntaxCookie;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.api.languages.Cookie;
import org.netbeans.api.languages.LibrarySupport;
import org.netbeans.api.languages.SyntaxCookie;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.SToken;
import org.netbeans.api.languages.TokenInput;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
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
import java.util.Stack;
import javax.swing.text.BadLocationException;
import javax.swing.text.Caret;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledDocument;


/**
 *
 * @author Jan Jancura
 */
public class JavaScript {

    private static final String DOC = "org/netbeans/modules/languages/javascript/Documentation.xml";
    
    
    public static ASTNode parseRegularExpression (
        TokenInput input, 
        Stack stack
    ) {
        List children = new ArrayList ();
        children.add (input.read ());
        while (!input.eof () && !input.next (1).getIdentifier ().equals ("/")) {
            if (input.next (1).getIdentifier ().equals ("\\"))
                children.add (input.read ());
            String s = input.next (1).getIdentifier ();
            if (s.equals ("\n") || s.equals ("\r")) 
                return ASTNode.create (
                    input.next (1).getMimeType (), 
                    "RegularExpression", 
                    100, 
                    input.next (1).getOffset ()
                );
            children.add (input.read ());
        }
        if (!input.eof () && input.next (1).getIdentifier ().equals ("/"))
            children.add (input.read ());
        if (input.next (1).getType ().equals ("js_identifier"))
            children.add (input.read ());
        return ASTNode.create (
            input.next (1).getMimeType (), 
            "RegularExpression", 
            100, 
            input.next (1).getOffset ()
        );
    }

    public static Runnable hyperlink (SyntaxCookie cookie) {
        PTPath path = cookie.getPTPath ();
        SToken t = (SToken) path.getLeaf ();
        ASTNode n = path.size () > 1 ? 
            (ASTNode) path.get (path.size () - 2) :
            null;
        String name = t.getIdentifier ();
        DatabaseManager databaseManager = DatabaseManager.getDefault ();
        List list = databaseManager.get (n, name, false);
        if (list.isEmpty ()) 
            list = databaseManager.get (DatabaseManager.FOLDER, name);
        if (list.isEmpty ()) return null;
        final Line.Part l = (Line.Part) list.get (0);
        if (l == null) return null;
        DataObject dataObject = (DataObject) l.getLine ().getLookup ().
            lookup (DataObject.class);
        EditorCookie ec = (EditorCookie) dataObject.getCookie (EditCookie.class);
        StyledDocument document = ec.getDocument ();
        int offset = NbDocument.findLineOffset (document, l.getLine ().getLineNumber ()) + l.getColumn ();
        if (offset == t.getOffset ()) return null;
        return new Runnable () {
            public void run () {
                l.getLine ().show (Line.SHOW_GOTO, l.getColumn ());
            }
        };
    }
    
    public static String functionName (SyntaxCookie cookie) {
        PTPath path = cookie.getPTPath ();
        ASTNode n = (ASTNode) path.getLeaf ();
        String name = n.getTokenTypeIdentifier ("js_identifier");
        String parameters = "";
        ASTNode parametersNode = n.getNode ("FormalParameterList");
        if (parametersNode != null)
            parameters = parametersNode.getAsText ();
        if (name != null) return name + " (" + parameters + ")";
        ASTNode p = n.getParent ();
        while (p != null) {
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
            p = p.getParent ();
        }
        return "?";
    }

    public static String objectName (SyntaxCookie cookie) {
        PTPath path = cookie.getPTPath ();
        ASTNode n = (ASTNode) path.getLeaf ();
        ASTNode p = n.getParent ();
        while (p != null) {
            if (p.getNT ().equals ("AssignmentExpressionInitial") &&
                p.getNode ("AssignmentOperator") != null
            ) {
                return ((ASTNode) p.getChildren ().get (0)).getAsText ();
            }
            if (p.getNT ().equals ("PropertyNameAndValue")) {
                return p.getNode ("PropertyName").getAsText ();
            }
            p = p.getParent ();
        }
        return "?";
    }
    
    private static List completionItems;
    
    public static List completionItems (Cookie cookie) {
        if (completionItems == null) {
            completionItems = new ArrayList ();
            completionItems.addAll (getLibrary ().getItems ("keyword"));
            completionItems.addAll (getLibrary ().getItems ("root"));
        }
        if (!(cookie instanceof SyntaxCookie)) return completionItems;
        PTPath path = ((SyntaxCookie) cookie).getPTPath ();
        ArrayList l = new ArrayList ();
        DatabaseManager databaseManager = DatabaseManager.getDefault ();
        Collection c = databaseManager.getIds ((ASTNode) path.get (path.size () - 2), true);
        l.addAll (c);
        c = databaseManager.getIds (DatabaseManager.FOLDER);
        l.addAll (c);
        return l;
    }

    private static List completionDescriptions;

    public static List completionDescriptions (Cookie cookie) {
        if (completionDescriptions == null) {
            List tags = completionItems (cookie);
            tags = completionItems;
            completionDescriptions = new ArrayList (tags.size ());
            Iterator it = tags.iterator ();
            while (it.hasNext ()) {
                String tag = (String) it.next ();
                String description = getLibrary ().getProperty 
                    ("keyword", tag, "description");
                if (description != null) {
                    completionDescriptions.add (
                        "<html><b><font color=blue>" + tag + 
                        ": </font></b><font color=black> " + 
                        description + "</font></html>"
                    );
                } else {
                    description = getLibrary ().getProperty 
                        ("root", tag, "description");
                    if (description == null) 
                        completionDescriptions.add (
                            "<html><b><font color=black>" + tag + 
                            "</font></b></html>"
                        );
                    else
                        completionDescriptions.add (
                            "<html><b><font color=black>" + tag + 
                            ": </font></b><font color=black> " + 
                            description + "</font></html>"
                        );
                }
            }
        }
        if (!(cookie instanceof SyntaxCookie)) return completionDescriptions;
        PTPath path = ((SyntaxCookie) cookie).getPTPath ();
        ArrayList l = new ArrayList ();
        DatabaseManager databaseManager = DatabaseManager.getDefault ();
        Collection c = databaseManager.getIds ((ASTNode) path.get (path.size () - 2), true);
        l.addAll (c);
        c = databaseManager.getIds (DatabaseManager.FOLDER);
        l.addAll (c);
        l.addAll (completionDescriptions);
        return l;
    }
    
    
    public static void performDeleteCurrentMethod (ASTNode node, JTextComponent comp) {
        NbEditorDocument doc = (NbEditorDocument)comp.getDocument();
        int position = comp.getCaretPosition();
        PTPath path = node.findPath(position);
        ASTNode methodNode = null;
        for (Iterator iter = path.iterator(); iter.hasNext(); ) {
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
        PTPath path = node.findPath(position);
        if (path == null) return false;
        for (Iterator iter = path.iterator(); iter.hasNext(); ) {
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
    
    // helper methods ..........................................................
    
    private static LibrarySupport library;
    
    private static LibrarySupport getLibrary () {
        if (library == null)
            library = LibrarySupport.create (DOC);
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
