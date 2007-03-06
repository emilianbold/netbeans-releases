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

import java.util.Iterator;
import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.LanguagesManager;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.ASTToken;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseAction;
import org.netbeans.modules.editor.NbEditorDocument;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ParseException;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.modules.languages.Feature;
import org.netbeans.modules.languages.Language;
import org.netbeans.modules.languages.LanguagesManagerImpl;
import org.netbeans.modules.languages.ParserManagerImpl;
import org.openide.ErrorManager;

/**
 *
 * @author Jan Jancura
 */
public class FormatAction extends BaseAction {
    
    public FormatAction () {
        super("Format");
    }
    
    public void actionPerformed (ActionEvent e, JTextComponent component) {
        try {
            NbEditorDocument doc = (NbEditorDocument) component.getDocument ();
            String mimeType = (String) doc.getProperty ("mimeType"); // NOI18N
            
            TokenHierarchy th = TokenHierarchy.get (doc);
            System.out.println(th);
            
            Language language = ((LanguagesManagerImpl) LanguagesManager.getDefault ()).
                getLanguage (mimeType);
            ASTNode root = ParserManagerImpl.get (doc).getAST ();
            if (root == null) return;
            StringBuilder sb = new StringBuilder ();
            Map<String,String> indents = new HashMap<String,String> ();
            indents.put ("i", ""); // NOI18N
            indents.put ("ii", "    "); // NOI18N
            indent (
                root,
                new ArrayList<ASTItem> (),
                language,
                sb,
                indents,
                null,
                false,
                doc
            );
            doc.remove (0, doc.getLength ());
            doc.insertString (0, sb.toString (), null);
        } catch (ParseException ex) {
            ErrorManager.getDefault ().notify (ex);
        } catch (BadLocationException ex) {
            ErrorManager.getDefault ().notify (ex);
        }
    }
    
    // uncomment to disable the action for languages without grammar definition
    /*
    public boolean isEnabled() {
        JTextComponent component = getTextComponent(null);
        if (component == null)
            return false;
        NbEditorDocument doc = (NbEditorDocument) component.getDocument ();
        String mimeType = (String) doc.getProperty ("mimeType"); // NOI18N
        try {
            Language language = LanguagesManager.getDefault().getLanguage(mimeType);
            return !language.getAnalyser().getRules().isEmpty();
        } catch (ParseException e) {
            ErrorManager.getDefault().notify(e);
            return false;
        }
    }
     */
    
    private static void indent (
        ASTItem          item,
        List<ASTItem>    path,
        Language         language,
        StringBuilder    sb,
        Map<String,String> indents,
        ASTToken         whitespace,
        boolean          firstIndented,
        NbEditorDocument document
    ) {
        path.add (item);
        ASTPath path2 = ASTPath.create (path);
        Iterator<ASTItem> it = item.getChildren ().iterator ();
        while (it.hasNext ()) {
            ASTItem e = it.next ();
            
            // compute indent
            String indent;
            if (item instanceof ASTToken) {
                ASTToken token = (ASTToken) e;
                if (token.getType ().equals ("whitespace")) { // NOI18N
                    whitespace = (ASTToken) e;
                    firstIndented = false;
                    continue;
                }
            }
            Feature indentProperties = language.getFeature (Language.INDENT, path2);
            indent = (String) indentProperties.getValue ();
            
            // indent
            if (e instanceof ASTNode)
                System.out.println("indent " + indent + " " + firstIndented + " : " + ((ASTNode) e).getNT () + " wh:" + whitespace);
            else
                System.out.println("indent " + indent + " " + firstIndented + " : " + e + " wh:" + whitespace);
            if (!firstIndented) {
                if (indent != null) {
                    whitespace = null;
                    String indentI = indents.get ("i");
                    String indentII = indents.get ("ii");
                    if (indent.startsWith (">b")) {
                        int b = sb.length () - Math.max (
                            sb.lastIndexOf ("\n"),
                            sb.lastIndexOf ("\r")
                        );
                        indents.put ("b", chars (b));
                        if (!indent.equals (">b"))
                            indent = indent.substring (2);
                    }
                    if (indent != null) {
                        if (indent.equals ("i")) 
                            sb.append ('\n').append (indentI);
                        else
                        if (indent.equals ("b")) 
                            sb.append ('\n').append (indents.get ("b"));
                        else
                        if (indent.equals ("++i")) {
                            indentI += indentII;
                            sb.append ('\n').append (indentI);
                        } else
                        if (indent.equals ("--i")) {
                            indentI = indentI.substring (
                                0, 
                                Math.max (0, indentI.length () - indentII.length ())
                            );
                            sb.append ('\n').append (indentI);
                        } else
                        if (indent.equals ("i++")) {
                            sb.append ('\n').append (indentI);
                            indentI += indentII;
                        } else
                        if (indent.equals ("i--")) {
                            sb.append ('\n').append (indentI);
                            indentI = indentI.substring (
                                0, 
                                Math.max (0, indentI.length () - indentII.length ())
                            );
                        } else
                            sb.append (indent);
                    }
                    indents.put ("i", indentI);
                    indents.put ("ii", indentII);
                } else // indent == null
                if (whitespace != null && e instanceof ASTToken) {
                    sb.append (whitespace.getIdentifier ());
                    whitespace = null;
                }
            } // firstIndented
            firstIndented = false;
            
            // add child
            if (e instanceof ASTToken)
                sb.append (((ASTToken) e).getIdentifier ());
            else
                indent (
                    (ASTNode) e,
                    path,
                    language, 
                    sb, 
                    indents,
                    whitespace,
                    firstIndented || indent != null,
                    null
                );
        }// for children
        path.remove (path.size () - 1);
    }
    
    private static String chars (int length) {
        StringBuilder sb = new StringBuilder ();
        for (int i = 0; i < length; i++) sb.append (' ');
        return sb.toString ();
    }
}
