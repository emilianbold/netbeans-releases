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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 *
 * @author Jan Jancura
 */
public abstract class ASTNode {
    
    public static ASTNode create (
        String      mimeType,
        String      nt,
        int         rule,
        List        children,
        int         offset
    ) {
        return new ASTNodeImpl (mimeType, nt, rule, offset, children);
    }
    
    public static ASTNode create (
        String      mimeType,
        String      nt,
        int         rule,
        int         offset
    ) {
        return new ASTNodeImpl (mimeType, nt, rule, offset);
    }
        
    public abstract String getMimeType ();

    public abstract String getNT ();
    
    public abstract int getRule ();
    
    public abstract ASTNode getParent ();
    
    public abstract List getChildren ();

    public abstract int getOffset ();
    
    public abstract void addToken (SToken t);
    
    public abstract void addNode (ASTNode n);

    private PTPath path;
    
    public PTPath getPath () {
        if (path == null) {
            path = PTPath.create (getParent (), this);
        }
        return path;
    }
    
    private int endOffset = -1;
    public int getEndOffset () {
        if (endOffset < 0) {
            List l = getChildren ();
            if (l.isEmpty ())
                endOffset = getOffset ();
            else {
                Object last = l.get (l.size () - 1);
                if (last instanceof SToken)
                    endOffset = ((SToken) last).getOffset () + 
                        ((SToken) last).getIdentifier ().length ();
                else
                    endOffset = ((ASTNode) last).getEndOffset ();
            }
        }
        return endOffset;
    }
    
    public int getLength () {
        int length = getEndOffset () - getOffset ();
        if (length < 0) {
            System.out.println("ASTNode invalid length offset=" + getOffset () + " endOffset=" + getEndOffset ());
            return 0;
        }
        return length;
    }
    
    public int findIndex (ASTNode n) {
        return getChildren ().indexOf (n);
    }
    
    public int findIndex (SToken t) {
        List children = getChildren ();
        int i, k = children.size ();
        for (i = 0; i < k; i++) {
            Object o = children.get (i);
            if (o instanceof SToken) {
                if (t.isCompatible ((SToken) o)) return i;
            }
        }
        return -1;
    }
    
    
    /**
     * Finds path to the first token defined by type and identifier.
     *
     * @param type          a type of token or null.
     * @param identifier    a value of token or null.
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
    
    public PTPath findPath (int offset) {
        if (offset < getOffset ()) return null;
        if (offset > getEndOffset ()) return null;
        Iterator it = getChildren ().iterator ();
        while (it.hasNext ()) {
            Object e = (Object) it.next ();
            if (e instanceof SToken) {
                SToken token = (SToken) e;
                if (offset < token.getOffset () + token.getIdentifier ().length () &&
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
    
    public SToken getTokenName (String name) {
        ASTNode node = this;
        int i = name.lastIndexOf ('.');
        if (i >= 0)
            node = getNode (name.substring (0, i));
        if (node == null) return null;
        Object o = node.getChild ("token-name-" + name.substring (i + 1));
        if (o == null) return null;
        if (!(o instanceof SToken)) return null;
        return (SToken) o;
    }

    public String getTokenTypeIdentifier (String name) {
        SToken token = getTokenType (name);
        if (token == null) return null;
        return token.getIdentifier ();
    }
    
    public SToken getTokenType (String name) {
        ASTNode node = this;
        int i = name.lastIndexOf ('.');
        if (i >= 0)
            node = getNode (name.substring (0, i));
        if (node == null) return null;
        Object o = node.getChild ("token-type-" + name.substring (i + 1));
        if (o == null) return null;
        if (!(o instanceof SToken)) return null;
        return (SToken) o;
    }
    
    public ASTNode getNode (String name) {
        ASTNode node = this;
        int s = 0, e = name.indexOf ('.');
        while (e >= 0) {
            node = (ASTNode) node.getChild ("node-" + name.substring (s, e));
            if (node == null) return null;
            s = e + 1;
            e = name.indexOf ('.', s);
        }
        return (ASTNode) node.getChild ("node-" + name.substring (s));
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

    public ASTNode getParent (String nt) {
        ASTNode p = getParent ();
        while (p != null && !p.getNT ().equals (nt))
            p = p.getParent ();
        return p;
    }
    
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
    
    static class ASTNodeImpl extends ASTNode {

        private String      mimeType;
        private String      nt;
        private int         rule;
        private List        children;
        private ASTNode     parent;
        private int         offset;

        ASTNodeImpl (
            String      mimeType, 
            String      nt, 
            int         rule, 
            int         offset
        ) {
            this.mimeType = mimeType;
            this.nt =       nt;
            this.rule =     rule;
            this.offset =   offset;
            children =      new ArrayList ();
        }

        ASTNodeImpl (
            String      mimeType, 
            String      nt, 
            int         rule, 
            int         offset,
            List        children
        ) {
            this (mimeType, nt, rule, offset);
            Iterator it = children.iterator ();
            while (it.hasNext ()) {
                Object o = it.next ();
                if (o instanceof SToken)
                    addToken ((SToken) o);
                else
                    addNode ((ASTNodeImpl) o);
            }
                
            
        }

        public String getMimeType () {
            return mimeType;
        }

        public String getNT () {
            return nt;
        }

        public int getRule () {
            return rule;
        }

        public ASTNode getParent () {
            return parent;
        }
        
        public int getOffset () {
            return offset;
        }
        
        public void addNode (ASTNode n) {
            if (n == null)
                throw new NullPointerException ();
            if (((ASTNodeImpl) n).parent != null)
                throw new IllegalArgumentException ();
            ((ASTNodeImpl) n).parent = this;
            children.add (n);
        }
        
        public void addToken (SToken t) {
            if (t == null)
                throw new NullPointerException ();
            children.add (t);
        }

        public List getChildren () {
            return children;
        }
    }
}
