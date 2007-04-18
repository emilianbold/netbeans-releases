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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.core.syntax;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTToken;


/**
 * Various Schliemanns callbacks from JSP.nbs file
 *
 * @author Marek Fukala
 */
public class JSP {
    
    public static String navigatorDisplayName(SyntaxContext context) {
        ASTItem item = context.getASTPath().getLeaf();
        if(item instanceof ASTNode) {
            ASTNode node = (ASTNode)item;
            String type = node.getNT();
            String name = null;
            if("simpleTag".equals(type) || "unpairedStartTag".equals(type)) {
                name = node.getTokenTypeIdentifier("TAG");
            } else if("unpairedEndTag".equals(type)) {
                name = node.getTokenTypeIdentifier("ENDTAG");
            } else if("tag".equals(type)) {
                name = node.getNode("startTag").getTokenTypeIdentifier("TAG");
            }
            if(name != null) {
                if(name.charAt(name.length() - 1) != '>') {
                    name += ">"; //add ending '>' if necessary
                }
                return name;
            }
        }
        return item.toString();
    }
    
    public static ASTNode process (SyntaxContext context) {
        ASTNode n = (ASTNode) context.getASTPath ().getRoot ();
        List l = new ArrayList ();
        resolve (n, new Stack (), l, true);
        return ASTNode.create (n.getMimeType (), n.getNT (), l, n.getOffset ());
    }
    
    // private methods .........................................................
    
    private static ASTNode clone (String mimeType, String nt, int offset, List children) {
        Iterator it = children.iterator ();
        List l = new ArrayList ();
        while (it.hasNext ()) {
            Object o = it.next ();
            if (o instanceof ASTToken)
                l.add (clone ((ASTToken) o));
            else
                l.add (clone ((ASTNode) o));
        }
        return ASTNode.create (mimeType, nt, l, offset);
    }
    
    private static ASTNode clone (ASTNode n) {
        return clone (n.getMimeType (), n.getNT (), n.getOffset (), n.getChildren ());
    }
    
    private static ASTToken clone (ASTToken token) {
        List<ASTItem> children = new ArrayList ();
        Iterator<ASTItem> it = token.getChildren ().iterator ();
        while (it.hasNext ()) {
            ASTItem item = it.next ();
            if (item instanceof ASTNode)
                children.add (clone ((ASTNode) item));
            else
                children.add (clone ((ASTToken) item));
        }
        return ASTToken.create (
            token.getMimeType (),
            token.getType (),
            token.getIdentifier (),
            token.getOffset (),
            token.getLength (),
            children
        );
    }
    
    private static ASTNode clone (ASTNode n, String nt) {
        return clone (n.getMimeType (), nt, n.getOffset (), n.getChildren ());
    }
    
    private static void resolve (ASTNode n, Stack s, List l, boolean findUnpairedTags) {
//        System.out.println("JSP:resolving node " + n.toString());
        Iterator<ASTItem> it = n.getChildren ().iterator ();
        while (it.hasNext ()) {
            ASTItem item = it.next ();
            if (item instanceof ASTToken) {
                l.add (clone ((ASTToken) item));
                continue;
            }
            ASTNode node = (ASTNode) item;
            if (node.getNT ().equals ("startTag")) {
                ASTToken tagCloseSymbolToken = node.getTokenType ("SYMBOL");
                if (tagCloseSymbolToken != null && "/>".equals(tagCloseSymbolToken.getIdentifier())) {
                    l.add (clone (node, "simpleTag"));
                } else {
                    String name = node.getTokenTypeIdentifier ("TAG");
                    if (name == null) 
                        name = "";
                    else {
                        //cut off the leading '<' char
                        name = name.toLowerCase ().substring(1); 
                        //the open tag can have the closing > symbol as a part of the TAG token :-|
                        if(name.endsWith(">")) {
                            name = name.substring(0, name.length() -1 ); 
                        }
                    }
                    s.add (name);
                    s.add (new Integer (l.size ()));
                    if (findUnpairedTags)
                        l.add (clone (node, "unpairedStartTag"));
                    else
                        l.add (clone (node, "startTag"));
                }
                continue;
            } else
            if (node.getNT ().equals ("endTag")) {
                String name = node.getTokenTypeIdentifier ("ENDTAG");
                if (name == null) 
                    name = "";
                else
                    name = name.toLowerCase ().substring(2); //cut off the leading '</' chars;
                int indexS = s.lastIndexOf (name); //find last 'name' element in the stack
                if (indexS >= 0) {
                    int indexL = ((Integer) s.get (indexS + 1)).intValue (); //gets an index to ll for the indexS
                    List ll = l.subList (indexL, l.size ()); //get everyting from the position of the coresponding starttag
                    ll.set (0, clone ((ASTNode) ll.get (0), "startTag")); //reset unfinished to normal tag
                    List ll1 = new ArrayList (ll);
                    ll1.add (node);
                    ASTNode tag = clone (
                        node.getMimeType (),
                        "tag",
                        ((ASTNode) ll1.get (0)).getOffset (),
                        ll1
                    ); //create an AST node with ll as children
                    ll.clear ();//clear the unassigned elements
                    s.subList (indexS, s.size ()).clear (); //clear the stack above indexS
                    l.add (tag);
                } else
                    l.add (clone (node, "unpairedEndTag"));
                continue;
            } else
            if (node.getNT ().equals ("tags")) {
                resolve (node, s, l, findUnpairedTags);
                continue;
            }
            if (node.getNT ().equals ("tag")) {
                resolve (node, s, l, findUnpairedTags);
                continue;
            }
            l.add (clone (node));
        }
    }
}

