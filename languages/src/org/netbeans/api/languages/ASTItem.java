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
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;


/**
 * Represents one AST node.
 * 
 * @auASASTItemn Jancura
 */
public class ASTItem {
   
    private String          mimeType;
    private int             offset;
    private int             length = -1;

    private ASTItem         parent;
    private List<ASTItem>   children;
    private ASTPath         path;

    ASTItem (
        String              mimeType,
        int                 offset,
        int                 length,
        List<? extends ASTItem> children
    ) {
        this.mimeType =     mimeType;
        this.offset =       offset;
        this.length =       length;

        // [PENDING]
//        int lastOffset = offset;
        this.children = new ArrayList<ASTItem> ();
        if (children != null) {
            Iterator<? extends ASTItem> it = children.iterator ();
            while (it.hasNext ()) {
                ASTItem item = it.next ();
                if (item == null)
                    throw new NullPointerException ();
//                if (item.getOffset () != lastOffset)
//                    throw new IllegalArgumentException ();
                if (item.getParent () != null)
                    throw new IllegalArgumentException ();
                item.parent = this;
                this.children.add (item);
//                lastOffset += item.getLength ();
            }
        }
        this.children = Collections.unmodifiableList (this.children);
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
     * Returns parent node of this node.
     * 
     * @return parent node of this node
     */
    public ASTItem getParent () {
        return parent;
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
     * Returns list of all subnodes (ASTNode) and tokens (ASTToken).
     * 
     * @return list of all subnodes (ASTNode) and tokens (ASTToken)
     */
    public List<ASTItem> getChildren () {
        return children;
    }
    
    /**
     * Returns path to this node from root node.
     * 
     * @return path to this node from root node
     */
    public ASTPath getPath () {
        if (path == null) {
            path = ASTPath.create (getParent (), this);
        }
        return path;
    }
    
    /**
     * Returns end offset of this node. Tt is the offset that is not part 
     * of this node.
     * 
     * @return end offset of this node
     */
    public int getEndOffset () {
        return getOffset () + getLength ();
    }
    
    /**
     * Returns length of this node (end offset - start offset).
     * 
     * @return length of this node (end offset - start offset)
     */
    public int getLength () {
        if (length < 0) {
            List<ASTItem> l = getChildren ();
            if (l.isEmpty ())
                length = 0;
            else {
                ASTItem last = l.get (l.size () - 1);
                length = last.getEndOffset () - getOffset ();
            }
        }
        return length;
    }
    
    /**
     * Returns index of given node inside this node or -1.
     * 
     * @param node node
     * @return index of given token inside this node or -1
     */
    public int findIndex (ASTItem node) {
        return getChildren ().indexOf (node);
    }
    
    /**
     * Returns path from this node to the token on given offset.
     * 
     * @param offset offset
     * 
     * @return path from this node to the token on given offset
     */
    public ASTPath findPath (int offset) {
        if (offset < getOffset ()) return null;
        if (offset > getEndOffset ()) return null;
        if (getChildren ().isEmpty ())
            return getPath ();
        if (getChildren ().size () > 10)
            return findPath2 (offset);
        Iterator<ASTItem> it = getChildren ().iterator ();
        while (it.hasNext ()) {
            ASTItem item = it.next ();
            if (offset < item.getEndOffset () &&
                item.getOffset () <= offset
            )
                return item.findPath (offset);
        }
        return getPath ();
    }

    private ASTPath findPath2 (int offset) {
        TreeMap<Integer,ASTItem> childrenMap = getChildrenMap ();
        SortedMap<Integer,ASTItem> headMap = childrenMap.headMap (new Integer (offset + 1));
        if (headMap.isEmpty ())
            return getPath ();
        Integer key = headMap.lastKey ();
        ASTItem item = childrenMap.get (key);
        ASTPath path =  item.findPath (offset);
        if (path == null)
            return getPath ();
        return path;
    }
    
    private TreeMap<Integer,ASTItem> childrenMap = null;
    
    private TreeMap<Integer,ASTItem> getChildrenMap () {
        if (childrenMap == null) {
            childrenMap = new TreeMap<Integer,ASTItem> ();
            Iterator<ASTItem> it = getChildren ().iterator ();
            while (it.hasNext ()) {
                ASTItem item = it.next ();
                childrenMap.put (new Integer (item.getOffset ()), item);
            }
        }
        return childrenMap;
    }
}