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

package org.netbeans.modules.web.core.syntax;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import org.netbeans.api.languages.ASTItem;
import org.netbeans.api.languages.ASTPath;
import org.netbeans.api.languages.Language;
import org.netbeans.api.languages.SyntaxContext;
import org.netbeans.api.languages.ASTNode;
import org.netbeans.api.languages.ASTToken;


/**
 * Various Schliemanns callbacks from JSP.nbs file
 *
 * @author Marek Fukala
 */
public class JSP {
    
    public static ASTNode process (SyntaxContext context) {
        ASTPath astPath = context.getASTPath ();
        ASTNode n = (ASTNode) astPath.getRoot ();
        return resolveRoot (n, new Stack (), new ArrayList(), true);
    }
    
    // private methods .........................................................
    
    private static ASTNode clone (Language language, String nt, int offset, List children) {
        return ASTNode.create (language, nt, children, offset);
    }
    
    private static ASTNode clone (ASTNode n) {
        return clone (n.getLanguage (), n.getNT (), n.getOffset (), n.getChildren ());
    }
    
    private static ASTNode clone (ASTNode n, String nt) {
        return clone (n.getLanguage (), nt, n.getOffset (), n.getChildren ());
    }
    
    private static ASTNode resolveRoot(ASTNode n, Stack s, List l, boolean findUnpairedTags) {
        //find java code blocks in jsp document
        ArrayList<Pair> javaBlocks = new ArrayList<Pair>(5);
        findJavaEmbeddings(n, new Stack(), javaBlocks);
        java_code_blocks = javaBlocks;
        
        resolve(n, s, l, findUnpairedTags);
        return ASTNode.create (n.getLanguage (), n.getNT (), l, n.getOffset ());
    }
    
    private static void resolve (ASTNode n, Stack s, List l, boolean findUnpairedTags) {
//        System.out.println("JSP:resolving node " + n.toString());
        Iterator<ASTItem> it = n.getChildren ().iterator ();
        while (it.hasNext ()) {
            ASTItem item = it.next ();
            if (item instanceof ASTToken) {
                l.add (((ASTToken) item));
                continue;
            }
            ASTNode node = (ASTNode) item;
            if (node.getNT ().equals ("startTag")) {
                ASTToken tagCloseSymbolToken = node.getTokenType ("SYMBOL");
                if (tagCloseSymbolToken != null && "/>".equals(tagCloseSymbolToken.getIdentifier())) {
                    l.add (clone (node, "simpleTag"));
                } else {
                    String name = node.getTokenTypeIdentifier ("TAG");
                    if (name == null) {
                        name = "";
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
                if (name == null) {
                    name = "";
                }
                int indexS = s.lastIndexOf (name); //find last 'name' element in the stack
                if (indexS >= 0) {
                    int indexL = ((Integer) s.get (indexS + 1)).intValue (); //gets an index to ll for the indexS
                    List ll = l.subList (indexL, l.size ()); //get everyting from the position of the coresponding starttag
                    ll.set (0, clone ((ASTNode) ll.get (0), "startTag")); //reset unfinished to normal tag
                    List ll1 = new ArrayList (ll);
                    ll1.add (node);
                    ASTNode tag = clone (
                        node.getLanguage (),
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
            //Hack - the process is called for the very root node, so the node 
            //which contains the sub-roots of JSP and HTML ASTs.
            //We need to jump into the JSP subroot and process it.
            if (node.getNT().equals("S") && node.getMimeType().equals("text/x-jsp")) {
                List nodeChildren = new ArrayList();
                resolve (node, new Stack(), nodeChildren, findUnpairedTags);
                l.add(ASTNode.create (node.getLanguage (), node.getNT (), nodeChildren, node.getOffset ()));
                continue;
            }
            
            l.add (clone (node));
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
    static List<Pair> java_code_blocks = Collections.emptyList();
    
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
                    if (name == null) {
                        name = "";
                    }
                    s.add (new NodeInfo(name, node));
                }
            } else
            if (node.getNT ().equals ("endTag")) {
                String name = node.getTokenTypeIdentifier ("ENDTAG");
                if (name == null) {
                    name = "";
                }
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
            } else {
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

