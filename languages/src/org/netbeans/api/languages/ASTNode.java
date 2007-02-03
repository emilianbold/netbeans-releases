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
public class ASTNode {
   
    /**
     * Creates new ASTNode.
     * 
     * @param mimeType   MIME type
     * @param nt         right side of grammar rule
     * @param rule       rule id
     * @param children   list of tokens (SToken) and subnodes (ASTNode)
     * @param offset     start offset of this AST node
     * 
     * @return           returns new instance of AST node
     */
    public static ASTNode create (
        String      mimeType,
        String      nt,
        int         rule,
        List        children,
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
        return new ASTNode (mimeType, nt, rule, offset, Collections.EMPTY_LIST);
    }

    
    private String      mimeType;
    private String      nt;
    private int         rule;
    private List        children;
    private ASTNode     parent;
    private int         offset;

    private ASTNode (
        String      mimeType, 
        String      nt, 
        int         rule, 
        int         offset,
        List        children
    ) {
        this.mimeType = mimeType;
        this.nt =       nt;
        this.rule =     rule;
        this.offset =   offset;
        List l = new ArrayList ();
        if (children != null) {
            Iterator it = children.iterator ();
            while (it.hasNext ()) {
                Object o = it.next ();
                if (o == null)
                    throw new NullPointerException ();
                if (o instanceof SToken)
                    l.add (o);
                else {
                    if (((ASTNode) o).parent != null)
                        throw new IllegalArgumentException ();
                    ((ASTNode) o).parent = this;
                    l.add (o);
                }
            }
        }
        this.children = Collections.unmodifiableList (l);
    }

    /**
     * Returns MIME type of this node.
     * 
     * @return MIME type of this node
     */
    public String getMimeType () {
        return mimeType;
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
     * Returns parent node of this node.
     * 
     * @return parent node of this node
     */
    public ASTNode getParent () {
        return parent;
    }

    /**
     * Returns offset of this node.
     * 
     * @return offset of this node
     */
    public int getOffset () {
        return offset;
    }

    /**
     * Returns list of all subnodes (ASTNode) and tokens (SToken).
     * 
     * @return list of all subnodes (ASTNode) and tokens (SToken)
     */
    public List getChildren () {
        return children;
    }

    private PTPath path;
    
    /**
     * Returns path to this node from root node.
     * 
     * @return path to this node from root node
     */
    public PTPath getPath () {
        if (path == null) {
            path = PTPath.create (getParent (), this);
        }
        return path;
    }
    
    private int endOffset = -1;
    
    /**
     * Returns end offset of this node. Tt is the offset that is not part 
     * of this node.
     * 
     * @return end offset of this node
     */
    public int getEndOffset () {
        if (endOffset < 0) {
            List l = getChildren ();
            if (l.isEmpty ())
                endOffset = getOffset ();
            else {
                Object last = l.get (l.size () - 1);
                if (last instanceof SToken)
                    endOffset = ((SToken) last).getOffset () + 
                        ((SToken) last).getLength ();
                else
                    endOffset = ((ASTNode) last).getEndOffset ();
            }
        }
        return endOffset;
    }
    
    /**
     * Returns length of this node (end offset - start offset).
     * 
     * @return length of this node (end offset - start offset)
     */
    public int getLength () {
        return getEndOffset () - getOffset ();
    }
    
    /**
     * Returns index of given node inside this node or -1.
     * 
     * @param node node
     * @return index of given token inside this node or -1
     */
    public int findIndex (ASTNode node) {
        return getChildren ().indexOf (node);
    }
    
    /**
     * Returns index of given token inside this node or -1.
     * 
     * @param token token
     * @return index of given token inside this node or -1
     */
    public int findIndex (SToken token) {
        List children = getChildren ();
        int i, k = children.size ();
        for (i = 0; i < k; i++) {
            Object o = children.get (i);
            if (o instanceof SToken) {
                if (token.isCompatible ((SToken) o)) return i;
            }
        }
        return -1;
    }
    
    
    /**
     * Finds path to the first token defined by type and identifier.
     *
     * @param type          a type of token or null
     * @param identifier    a value of token or null
     * 
     * @return path to the first token defined by type and identifier
     */
    public List findToken (String type, String identifier) {
        Iterator it = getChildren ().iterator ();
        while (it.hasNext ()) {
            Object e = it.next ();
            if (e instanceof SToken) {
                SToken t = (SToken) e;
                if (type != null && !type.equals (t.getType ())) continue;
                if (identifier != null && !identifier.equals (t.getIdentifier ())) continue;
                List l = new ArrayList ();
                l.add (t);
                return l;
            } else {
                List l = ((ASTNode) e).findToken (type, identifier);
                if (l == null) continue;
                l.add (this);
                return l;
            }
        }
        return null;
    }
    
    /**
     * Returns path from this node to the token on given offset.
     * 
     * @param offset offset
     * 
     * @return path from this node to the token on given offset
     */
    public PTPath findPath (int offset) {
        if (offset < getOffset ()) return null;
        if (offset > getEndOffset ()) return null;
        Iterator it = getChildren ().iterator ();
        while (it.hasNext ()) {
            Object e = (Object) it.next ();
            if (e instanceof SToken) {
                SToken token = (SToken) e;
                if (offset < token.getOffset () + token.getLength () &&
                    token.getOffset () <= offset
                ) {
                    return PTPath.create (this, token);
                }
            } else {
                ASTNode node = (ASTNode) e;
                if (offset < node.getEndOffset ())
                    return node.findPath (offset);
            }
        }
        return null;
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
        SToken token = getTokenType (type);
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
    public SToken getTokenType (String type) {
        ASTNode node = this;
        int i = type.lastIndexOf ('.');
        if (i >= 0)
            node = getNode (type.substring (0, i));
        if (node == null) return null;
        Object o = node.getChild ("token-type-" + type.substring (i + 1));
        if (o == null) return null;
        if (!(o instanceof SToken)) return null;
        return (SToken) o;
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
    
    private Map nameToChild = null;
    
    private Object getChild (String name) {
        if (nameToChild == null) {
            nameToChild = new HashMap ();
            Iterator it = getChildren ().iterator ();
            while (it.hasNext ()) {
                Object o = it.next ();
                if (o instanceof SToken) {
                    SToken t = (SToken) o;
                    nameToChild.put ("token-type-" + t.getType (), t);
                } else {
                    nameToChild.put (
                        "node-" + ((ASTNode) o).getNT (), 
                        o
                    );
                }
            }
        }
        return nameToChild.get (name);
    }

    /**
     * Returns parent node of this node with given non terminal.
     * 
     * @return parent node of this node with given non terminal
     */
    public ASTNode getParent (String nt) {
        ASTNode p = getParent ();
        while (p != null && !p.getNT ().equals (nt))
            p = p.getParent ();
        return p;
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
                sb.append (((SToken) elem).getIdentifier ());
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
