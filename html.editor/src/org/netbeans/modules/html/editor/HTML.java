/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.html.editor;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import javax.swing.text.AbstractDocument;
import org.netbeans.api.html.lexer.HTMLTokenId;
//import org.netbeans.api.languages.ASTItem;
//import org.netbeans.api.languages.Language;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
//import org.netbeans.api.languages.Context;
//import org.netbeans.api.languages.SyntaxContext;
//import org.netbeans.api.languages.ASTNode;
//import org.netbeans.api.languages.ASTToken;
//import org.netbeans.api.languages.LibrarySupport;
import org.netbeans.api.lexer.TokenHierarchy;

/**
 *
 * @author Jan Jancura
 * @author Marek Fukala
 */

public class HTML {
    
//    private static final String HTML401 = "org/netbeans/modules/html/editor/resources/HTML401.xml";
//    
//    
//    private static int dt = 0; 
//    private static int da = 0;
//    
//    public static boolean isDeprecatedAttribute (Context context) {
//        if(!(context instanceof SyntaxContext)) {
//            return false; //no AST
//        }
//        SyntaxContext scontext = (SyntaxContext)context;
//        int item_offset = scontext.getASTPath().getLeaf().getOffset();
//        AbstractDocument document = (AbstractDocument) context.getDocument ();
//        document.readLock ();
//        try {
//            TokenSequence tokenSequence = getTokenSequence (context);
//            if(tokenSequence == null) return false;
//            Token t = tokenSequence.token ();
//            if (t == null) return false;
//            if (t.id() != HTMLTokenId.ARGUMENT) {
//                return false;
//            }
//            String attribName = t.text ().toString ().toLowerCase ();
//            String tagName = tagName (tokenSequence);
//            if (tagName == null) return false;
//            return "true".equals (getLibrary ().getProperty (tagName, attribName, "deprecated"));
//        } finally {
//            document.readUnlock ();
//        }
//    }
//
//    public static boolean isDeprecatedTag (Context context) {
//        if(!(context instanceof SyntaxContext)) {
//            return false; //no AST
//        }
//        SyntaxContext scontext = (SyntaxContext)context;
//        int item_offset = scontext.getASTPath().getLeaf().getOffset();
//        AbstractDocument document = (AbstractDocument) context.getDocument ();
//        document.readLock ();
//        try {
//            TokenSequence tokenSequence = getTokenSequence (context);
//            if(tokenSequence == null) return false;
//            Token t = tokenSequence.token ();
//            if (t == null) return false;
//            if (t.id() != HTMLTokenId.TAG_OPEN) {
//                return false;
//            }
//            String tagName = t.text ().toString ().toLowerCase ();
//            return "true".equals (getLibrary ().getProperty ("TAG", tagName, "deprecated"));
//        } finally {
//            document.readUnlock ();
//        }
//    }
//
//    public static boolean isEndTagRequired (Context context) {
//        AbstractDocument document = (AbstractDocument) context.getDocument ();
//        document.readLock ();
//        try {
//            TokenSequence tokenSequence = getTokenSequence (context);
//            if(!tokenSequence.language().mimeType().equals("text/html")) {
//                return false;
//            }
//            Token t = tokenSequence.token ();
//            if (t == null) return false;
//            return isEndTagRequired (t.id ().name ().toLowerCase ());
//        } finally {
//            document.readUnlock ();
//        }
//    }
//
//    static boolean isEndTagRequired (String tagName) {
//        String v = getLibrary ().getProperty ("TAG", tagName, "endTag");
//        return !"O".equals (v) && !"F".equals (v);
//    }
//    
//    static boolean isSupportedTag(String tagName) {
//        return getLibrary().getProperty("TAG", tagName, "key") != null;
//    }
//    
//    public static ASTNode process (SyntaxContext context) {
//        ASTNode n = (ASTNode) context.getASTPath ().getRoot ();
//        List l = new ArrayList ();
//        resolve (n, new Stack (), l, true);
//        return ASTNode.create (n.getLanguage (), n.getNT (), l, n.getOffset ());
//    }
//    
//    
//    // private methods .........................................................
//
//    private static String tagName (TokenSequence ts) {
//        while (!ts.token ().id ().primaryCategory().equals ("tag")) //NOI18N
//            if (!ts.movePrevious ()) break;
//        if (!ts.token ().id ().primaryCategory().equals ("tag")) //NOI18N
//            return null;
//        return ts.token ().text ().toString ().toLowerCase ();
//    }
//    
//    private static LibrarySupport library;
//    
//    private static LibrarySupport getLibrary () {
//        if (library == null)
//            library = LibrarySupport.create (HTML401);
//        return library;
//    }
//    
//    private static ASTNode clone (Language language, String nt, int offset, List children) {
//        return clone(language, nt, offset, children, true);
//    }
//    
//    private static ASTNode clone (Language language, String nt, int offset, List children, boolean cloneChildren) {
//        if(cloneChildren) {
//            Iterator it = children.iterator ();
//            List l = new ArrayList ();
//            while (it.hasNext ()) {
//                Object o = it.next ();
//                if (o instanceof ASTToken)
//                    l.add (clone ((ASTToken) o));
//                else
//                    l.add (clone ((ASTNode) o));
//            }
//            return ASTNode.create (language, nt, l, offset);
//        } else {
//            return ASTNode.create(language, nt, children, offset);
//        }
//    }
//    
//    private static ASTNode clone (ASTNode n) {
//        return clone (n.getLanguage (), n.getNT (), n.getOffset (), n.getChildren ());
//    }
//    
//    private static ASTToken clone (ASTToken token) {
//        List<ASTItem> children = new ArrayList ();
//        Iterator<ASTItem> it = token.getChildren ().iterator ();
//        while (it.hasNext ()) {
//            ASTItem item = it.next ();
//            if (item instanceof ASTNode)
//                children.add (clone ((ASTNode) item));
//            else
//                children.add (clone ((ASTToken) item));
//        }
//        return ASTToken.create (
//            token.getLanguage (),
//            token.getTypeID (),
//            token.getIdentifier (),
//            token.getOffset (),
//            token.getLength (),
//            children
//        );
//    }
//    
//    private static ASTNode clone (ASTNode n, String nt) {
//        return clone (n.getLanguage (), nt, n.getOffset (), n.getChildren (), false);
//    }
//    
//    public static void resolve (ASTNode n, Stack s, List l, boolean findUnpairedTags) {
//        Iterator<ASTItem> it = n.getChildren ().iterator ();
//        while (it.hasNext ()) {
//            ASTItem item = it.next ();
//            if (item instanceof ASTToken) {
//                //l.add (clone ((ASTToken) item));
//                l.add ((ASTToken) item);
//                continue;
//            }
//            ASTNode node = (ASTNode) item;
//            if (node.getNT ().equals ("startTag")) {
//                ASTToken tagCloseSymbolToken = node.getTokenType ("TAG_CLOSE_SYMBOL");
//                if (tagCloseSymbolToken != null && "/>".equals(tagCloseSymbolToken.getIdentifier())) {
//                    l.add (clone (node, "simpleTag"));
//                } else {
//                    String name = node.getTokenTypeIdentifier ("TAG_OPEN");
//                    if (name == null) 
//                        name = "";
//                    else
//                        name = name.toLowerCase ();
//                    s.add (name);
//                    s.add (new Integer (l.size () - 1));
//                    
//                    /*if(!isSupportedTag(name)) {
//                        l.add (clone (node, "unsupportedTag"));
//                    } else */if (findUnpairedTags && isEndTagRequired (name))
//                        l.add (clone (node, "unpairedStartTag"));
//                    else
//                        l.add (clone (node, "simpleTag"));
//                }
//                continue;
//            } else
//            if (node.getNT ().equals ("endTag")) {
//                String name = node.getTokenTypeIdentifier ("TAG_CLOSE");
//                if (name == null) 
//                    name = "";
//                else
//                    name = name.toLowerCase ();
//                int indexS = s.lastIndexOf (name);
//                if (indexS >= 0) {
//                    int indexL = ((Integer) s.get (indexS + 1)).intValue ();
//                    List ll = l.subList (indexL, l.size ());
//                    ll.set (1, clone ((ASTNode) ll.get (1), "startTag"));
//                    List ll1 = new ArrayList (ll);
//                    ll1.add (node);
//                    ASTNode tag = clone (
//                        node.getLanguage (),
//                        "tag",
//                        ((ASTItem) ll1.get (0)).getOffset (),
//                        ll1
//                    );
//                    ll.clear ();
//                    s.subList (indexS, s.size ()).clear ();
//                    l.add (tag);
//                } else
//                    l.add (clone (node, "unpairedEndTag"));
//                continue;
//            } else
//            if (node.getNT ().equals ("tags")) {
//                resolve (node, s, l, findUnpairedTags);
//                continue;
//            }
//            if (node.getNT ().equals ("tag")) {
//                resolve (node, s, l, findUnpairedTags);
//                continue;
//            }
////            l.add (clone (node));
//            l.add(node);
//        }
//    }
//    
//    private static TokenSequence getTokenSequence (Context context) {
//        TokenHierarchy tokenHierarchy = TokenHierarchy.get (context.getDocument ());
//        TokenSequence ts = tokenHierarchy.tokenSequence ();
//        while (true) {
//            ts.move (context.getOffset ());
//            if (!ts.moveNext ()) return ts;
//            TokenSequence ts2 = ts.embedded ();
//            if (ts2 == null) return ts;
//            ts = ts2;
//        }
//    }
}

