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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTToken;
import org.netbeans.modules.html.editor.HTML;


/**
 * Various Schliemanns callbacks from JSP.nbs file
 *
 * @author Marek Fukala
 */
public class JSP {
    
    private static final int MAX_PRINT_CHARS = 30; //max. length of scriptlet code shown in navigator
     
    public static String navigatorDisplayName(SyntaxContext context) {
        ASTItem item = context.getASTPath().getLeaf();
        if(item instanceof ASTNode) {
            ASTNode node = (ASTNode)item;
            String type = node.getNT();
            String name = null;
            if("scriptlet".equals(type)) {
                name = node.getNode("S").getAsText();
                if(name != null && name.length() > MAX_PRINT_CHARS) {
                    return name.substring(0, MAX_PRINT_CHARS) + "..."; //NOI18N
                }
            } else if("simpleTag".equals(type) || "unpairedStartTag".equals(type)) {
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
        ASTPath astPath = context.getASTPath ();
        ASTNode n = (ASTNode) astPath.getRoot ();
        return resolveRoot (n, new Stack (), new ArrayList(), true);
    }
    
    // private methods .........................................................
    
    private static ASTNode clone (String mimeType, String nt, int offset, List children) {
//        Iterator it = children.iterator ();
//        List l = new ArrayList ();
//        while (it.hasNext ()) {
//            Object o = it.next ();
//            if (o instanceof ASTToken)
//                l.add (clone ((ASTToken) o));
//            else
//                l.add (clone ((ASTNode) o));
//        }
        return ASTNode.create (mimeType, nt, children, offset);
    }
    
    private static ASTNode clone (ASTNode n) {
        return clone (n.getMimeType (), n.getNT (), n.getOffset (), n.getChildren ());
    }
    
//    private static ASTToken clone (ASTToken token) {
//        List<ASTItem> children = new ArrayList ();
//        Iterator<ASTItem> it = token.getChildren ().iterator ();
//        while (it.hasNext ()) {
//            ASTItem item = it.next ();
//            if (item instanceof ASTNode) {
//                ASTNode n = (ASTNode)item;
//                if(!n.getNT().equals("S") && n.getMimeType().equals("text/html")) {
//                    children.add (clone ((ASTNode) item)); //do not clone html nodes
//                }
//            } else {
//                children.add (clone ((ASTToken) item));
//            }
//        }
//        return ASTToken.create (
//            token.getMimeType (),
//            token.getType (),
//            token.getIdentifier (),
//            token.getOffset (),
//            token.getLength (),
//            children
//        );
//    }
    
    private static ASTNode clone (ASTNode n, String nt) {
        return clone (n.getMimeType (), nt, n.getOffset (), n.getChildren ());
    }
    
    private static void collectHtmlNodes(List<ASTItem> l, ASTNode node) {
        //check if the node may contain an html nodes - just etext and ERROR nodes may contain it
        for(ASTItem itm : node.getChildren()) {
            if(itm instanceof ASTNode && ("text/x-jsp".equals(itm.getMimeType()) || "text/x-tag".equals(itm.getMimeType()))) {
                ASTNode n = (ASTNode)itm;
                if("etext".equals(n.getNT()) || "ERROR".equals(n.getNT())) {
                    ASTToken t = n.getTokenType("TEXT");
                    if(t != null) {
                        ASTNode htmlS = (ASTNode)t.getChildren().get(0); //find S nonterminal node
                        for(ASTItem i : htmlS.getChildren()) {
                            if(i instanceof ASTNode) {
                                ASTNode nn = (ASTNode)i;
                                //fix ERROR nonterminal to etext - see issue #102285
                                Iterator<ASTItem> itr = nn.getChildren().iterator();
                                while(itr.hasNext()) {
                                    ASTItem it = itr.next();
                                    if(it instanceof ASTNode) {
                                        ASTNode nod = (ASTNode)it;
                                        //if the last element in the children list is ERROR, switch it to etext
                                        //note: may 'fix' real errors :-|
                                        if(nod.getNT().equals("ERROR") && !itr.hasNext()) {
                                            l.add(ASTNode.create(nod.getMimeType(), "etext", nod.getChildren(), nod.getOffset()));
                                        } else {
                                            l.add(nod);
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    collectHtmlNodes(l, n);
                }
            }
        }
    }
    
    private static ASTNode resolveRoot(ASTNode n, Stack s, List l, boolean findUnpairedTags) {
        //find java code blocks in jsp document
        ArrayList<Pair> javaBlocks = new ArrayList<Pair>(5);
        findJavaEmbeddings(n, new Stack(), javaBlocks);
        java_code_blocks = javaBlocks;
        
        //find all html root nodes (S nonterminal node) and join their content into one html root node
        List<ASTItem> nodes = new ArrayList();
        collectHtmlNodes(nodes, n);
        
        if(nodes.isEmpty()) {
            //no html code in the page - return node with no children
            return ASTNode.create(n.getMimeType(), n.getNT(), Collections.EMPTY_LIST, n.getOffset());
        }
        
        int firstChildOffset = nodes.get(0).getOffset();
        ASTNode tagsnode = ASTNode.create("text/html", "tags", nodes, firstChildOffset);
        List<ASTItem> schildren = new ArrayList();
        schildren.add(tagsnode);
        ASTNode html_S_node = ASTNode.create("text/html", "S", schildren , firstChildOffset);

        //resolve the joined HTML AST
        List<ASTItem> ll = new ArrayList();
        HTML.resolve(html_S_node, new Stack(), ll, findUnpairedTags);
        
        //OK, now we have the joined resolved html AST, lets merge it with the JSP one...
        html_S_node = ASTNode.create (html_S_node.getMimeType (), html_S_node.getNT (), ll, html_S_node.getOffset ());
        
//        //resolve the JSP AST
//        resolve(n, s, l, findUnpairedTags);
//        n = ASTNode.create (n.getMimeType (), n.getNT (), l, n.getOffset ());
//        
//        //now merge the trees
//        simpleMerge(html_S_node, n);
//        //return the merged AST
        
        return html_S_node; 
    }
    
    private static void simpleMerge(ASTNode html_root_node, ASTNode jsp_root_node) {
        //generate a list of important nodes:
        // startTag, simpleTag, ...
        List<ASTNode> importantTagsList = new ArrayList();
        findImportantTags(jsp_root_node, importantTagsList);
        
        //put the nodes into the html AST
        for(ASTNode in : importantTagsList) {
            int offset = in.getOffset();
            ASTNode found = findNode(html_root_node, offset);
            //find a place where to put the node
            int insert_index = 0;
            for(ASTItem i : found.getChildren()) {
                if(i.getOffset() > offset) {
                    break;
                }
                insert_index++;
            }
            found.getChildren().add(insert_index, in);
        }
    }
    
     private static ASTNode findNode (ASTNode node, int offset) {
        for(ASTItem it : node.getChildren()) {
            if (it instanceof ASTNode && "text/html".equals(it.getMimeType())) {
                ASTNode n = (ASTNode) it;
                if (n.getOffset () <= offset &&
                    offset < n.getEndOffset ()) {
                     return findNode (n, offset);
                }
            }
        }
        return node;
    }
    
    private static void findImportantTags(ASTNode node, List<ASTNode> list) {
        for(ASTItem i : node.getChildren()) {
            if(i instanceof ASTNode) {
                String nn = ((ASTNode)i).getNT();
                if("startTag".equals(nn) || "simpleTag".equals(nn)) {
                    list.add((ASTNode)i);
                }
                findImportantTags((ASTNode)i, list);
            }
        }
    }
    
    //workaround - if there wasn't the hack for Schlieman inability to reasonably merge
    //the html and jsp together the produced AST tree could be accessed via Schalieman
    //API and searched for the java declaring tags.
    
    //synchronization not necessary since the client always listens to ParserManager
    //and is called after the AST processing finishes
    static List<Pair> java_code_blocks;
    
    private static void findJavaEmbeddings (ASTNode n, Stack<NodeInfo> s, ArrayList<Pair> javaBlocks) {
        Iterator<ASTItem> it = n.getChildren ().iterator ();
        while (it.hasNext ()) {
            ASTItem item = it.next ();
            if (item instanceof ASTToken) {
                continue;
            }
            ASTNode node = (ASTNode) item;
            if (node.getNT ().equals ("startTag")) {
                ASTToken tagCloseSymbolToken = node.getTokenType ("SYMBOL");
                if (tagCloseSymbolToken != null && "/>".equals(tagCloseSymbolToken.getIdentifier())) {
                    //ignore simple (empty) tags
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
                    s.add (new NodeInfo(name, node));
                }
            } else
            if (node.getNT ().equals ("endTag")) {
                String name = node.getTokenTypeIdentifier ("ENDTAG");
                if (name == null) 
                    name = "";
                else
                    name = name.toLowerCase ().substring(2); //cut off the leading '</' chars;
                int indexS = s.lastIndexOf (new NodeInfo(name, null)); //find last 'name' element in the stack
                if (indexS >= 0) {
                    //found matching tag
                    NodeInfo ni = s.get(indexS);
                    s.subList (indexS, s.size ()).clear (); //clear the stack above indexS
                    if("jsp:scriptlet".equalsIgnoreCase(name) 
                            || "jsp:declaration".equalsIgnoreCase(name)
                            || "jsp:expression".equalsIgnoreCase(name)) { //NOI18N
                        int scriptletStart = ni.node.getOffset() + ni.name.length() + 1;
                        int scriptletEnd = node.getOffset();
                        javaBlocks.add(new Pair(scriptletStart, scriptletEnd));
                    }
                }
            } else
            if (node.getNT ().equals ("tags")) {
                findJavaEmbeddings (node, s, javaBlocks);
            }
            if (node.getNT ().equals ("tag")) {
                findJavaEmbeddings (node, s, javaBlocks);
            } 
        }
    }
    
    static class Pair {
        int a,b;
        private Pair(int a, int b) {
            this.a = a;
            this.b = b;
        }
    }
    
    private static class NodeInfo  {
        String name;
        ASTNode node;
        private NodeInfo(String nodeName, ASTNode node) {
            this.name = nodeName;
            this.node = node;
        }
        public boolean equals(Object o) {
            if(!(o instanceof NodeInfo)) {
                throw new ClassCastException();
            } else {
                return ((NodeInfo)o).name.equals(name);
            }
        }
    }
}

