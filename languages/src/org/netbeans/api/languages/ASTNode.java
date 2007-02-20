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

package org.netbeans.api.languages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * Represents one AST node.
 * 
 * @author Jan Jancura
 */
public final class ASTNode extends ASTItem {
   
    /**
     * Creates new ASTNode.
     * 
     * @param mimeType   MIME type
     * @param nt         right side of grammar rule
     * @param rule       rule id
     * @param children   list of tokens (ASTToken) and subnodes (ASTNode)
     * @param offset     start offset of this AST node
     * 
     * @return           returns new instance of AST node
     */
    public static ASTNode create (
        String      mimeType,
        String      nt,
        int         rule,
        List<ASTItem> children,
        int         offset
    ) {
        return new ASTNode (mimeType, nt, rule, offset, children);
    }
    
    /**
     * Creates new ASTNode.
     * 
     * @param mimeType   MIME type
     * @param nt         right side of grammar rule
     * @param rule       rule id
     * @param offset     start offset of this AST node
     * 
     * @return           returns new instance of AST node
     */
    public static ASTNode create (
        String      mimeType,
        String      nt,
        int         rule,
        int         offset
    ) {
        return new ASTNode (mimeType, nt, rule, offset, Collections.<ASTItem>emptyList ());
    }

    
    private String      nt;
    private int         rule;

    private ASTNode (
        String      mimeType, 
        String      nt, 
        int         rule, 
        int         offset,
        List<ASTItem> children
    ) {
        super (mimeType, offset, -1, children);
        this.nt =       nt;
        this.rule =     rule;
    }

    /**
     * Returns the name of non terminal.
     * 
     * @return name of non terminal
     */
    public String getNT () {
        return nt;
    }

    /**
     * Returns id of rule that has created this node.
     * 
     * @return id of rule that has created this node
     */
    public int getRule () {
        return rule;
    }

    /**
     * Finds path to the first token defined by type and identifier or null.
     *
     * @param type          a type of token or null
     * @param identifier    a value of token or null
     * 
     * @return path to the first token defined by type and identifier or null
     */
    public ASTPath findToken (String type, String identifier) {
        List<ASTItem> path = new ArrayList<ASTItem> ();
        findToken (type, identifier, path);
        if (path.isEmpty ()) return null;
        return ASTPath.create (path);
    }
    
    private boolean findToken (String type, String identifier, List<ASTItem> path) {
        path.add (this);
        Iterator it = getChildren ().iterator ();
        while (it.hasNext ()) {
            Object e = it.next ();
            if (e instanceof ASTToken) {
                ASTToken t = (ASTToken) e;
                if (type != null && !type.equals (t.getType ())) continue;
                if (identifier != null && !identifier.equals (t.getIdentifier ())) continue;
                return true;
            } else
                if (((ASTNode) e).findToken (type, identifier, path))
                    return true;
        }
        path.remove (path.size () - 1);
        return false;
    }
    
    /**
     * Returns top-most subnode of this node on given offset with given 
     * non terminal name.
     * 
     * @param nt        name of non terminal
     * @param offset    offset of node
     * 
     * @return MIME     top-most subnode of this node on given offset 
     *                  with given non terminal name
     */
    public ASTNode findNode (String nt, int offset) {
        if (nt.equals (getNT ())) return this;
        Iterator it = getChildren ().iterator ();
        while (it.hasNext ()) {
            Object e = (Object) it.next ();
            if (e instanceof ASTNode) {
                ASTNode node = (ASTNode) e;
                if (node.getOffset () <= offset &&
                    offset < node.getEndOffset ()
                )
                    return node.findNode (nt, offset);
            }
        }
        return null;
    }

    /**
     * Returns identifier of some subtoken with given type.
     * 
     * @param type type of subtoken to be returned
     * 
     * @return identifier of some subtoken with given type
     */
    public String getTokenTypeIdentifier (String type) {
        ASTToken token = getTokenType (type);
        if (token == null) return null;
        return token.getIdentifier ();
    }
    
    /**
     * Returns some subtoken with given type.
     * 
     * @param type type of subtoken to be returned
     * 
     * @return some subtoken with given type
     */
    public ASTToken getTokenType (String type) {
        ASTNode node = this;
        int i = type.lastIndexOf ('.');
        if (i >= 0)
            node = getNode (type.substring (0, i));
        if (node == null) return null;
        Object o = node.getChild ("token-type-" + type.substring (i + 1));
        if (o == null) return null;
        if (!(o instanceof ASTToken)) return null;
        return (ASTToken) o;
    }
    
    /**
     * Returns child node of this node with given path ("foo.goo.boo").
     * 
     * @param path "foo.goo.boo" like path to some subnode
     * 
     * @return child node of this node with given path
     */
    public ASTNode getNode (String path) {
        ASTNode node = this;
        int s = 0, e = path.indexOf ('.');
        while (e >= 0) {
            node = (ASTNode) node.getChild ("node-" + path.substring (s, e));
            if (node == null) return null;
            s = e + 1;
            e = path.indexOf ('.', s);
        }
        return (ASTNode) node.getChild ("node-" + path.substring (s));
    }
    
    private Map<String,ASTItem> nameToChild = null;
    
    private Object getChild (String name) {
        if (nameToChild == null) {
            nameToChild = new HashMap<String,ASTItem> ();
            Iterator<ASTItem> it = getChildren ().iterator ();
            while (it.hasNext ()) {
                ASTItem item = it.next ();
                if (item instanceof ASTToken) {
                    ASTToken t = (ASTToken) item;
                    nameToChild.put ("token-type-" + t.getType (), t);
                } else {
                    nameToChild.put (
                        "node-" + ((ASTNode) item).getNT (), 
                        item
                    );
                }
            }
        }
        return nameToChild.get (name);
    }
    
    /**
     * Returns text representation of this node.
     * 
     * @return text representation of this node
     */
    public String print () {
        return print ("");
    }
    
    private String print (String indent) {
        StringBuilder sb = new StringBuilder ();
        sb.append (indent).append ("ASTNode ").append (getNT ()).append (' ').
            append (getOffset ()).append ('-').append (getEndOffset ());
        indent = "  " + indent;
        Iterator it = getChildren ().iterator ();
        while (it.hasNext ()) {
            Object elem = it.next ();
            if (elem instanceof ASTNode) {
                sb.append ('\n').append (((ASTNode) elem).print (indent));
            } else
                sb.append ('\n').append (indent).append (elem);
        }
        return sb.toString ();
    }
    
    /**
     * Returns text content of this node.
     * 
     * @return text content of this node
     */
    public String getAsText () {
        StringBuilder sb = new StringBuilder ();
        Iterator it = getChildren ().iterator ();
        while (it.hasNext ()) {
            Object elem = it.next ();
            if (elem instanceof ASTNode)
                sb.append (((ASTNode) elem).getAsText ());
            else
                sb.append (((ASTToken) elem).getIdentifier ());
        }
        return sb.toString ();
    }
    
    /**
     * Returns string representation of this object.
     * 
     * @return string representation of this object
     */
    public String toString () {
        StringBuilder sb = new StringBuilder ();
        sb.append ("ASTNode ").append (getNT ()).append (' ').
            append (getOffset ()).append ('-').append (getEndOffset ());
        Iterator it = getChildren ().iterator ();
        while (it.hasNext ()) {
            Object elem = it.next ();
            if (elem instanceof ASTNode)
                sb.append ("\n    ").append (((ASTNode) elem).getNT () + "...");
            else
                sb.append ("\n    ").append (elem);
        }
        return sb.toString ();
    }
}
