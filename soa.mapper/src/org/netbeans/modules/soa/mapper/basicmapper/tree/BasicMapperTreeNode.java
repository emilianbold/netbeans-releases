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

package org.netbeans.modules.soa.mapper.basicmapper.tree;


import javax.swing.tree.TreePath;

import org.netbeans.modules.soa.mapper.basicmapper.MapperNode;
import org.netbeans.modules.soa.mapper.common.basicmapper.tree.IMapperTreeNode;

/**
 * <p>
 *
 * Title: BasicMapperTreeNode </p> <p>
 *
 * Description: An basic Tree Mapper Node. This class should not be instanitate
 * directly. To obtain an Tree Mapper Node, use the
 * IMapperTreeView.getMapperTreeNode() method. </p> <p>
 *
 * @author    Un Seng Leong
 * @created   December 23, 2002
 */
public class BasicMapperTreeNode extends MapperNode implements IMapperTreeNode {

    private AbstractMapperTree mTree;
    private TreePath mTreePath;

    private boolean mIsSourceTreeNode;
    private boolean mIsDestTreeNode;
    private boolean mIsHighlightLink;
    private boolean mIsSelectedLink;
    

    /**
     * Creates a new BasicMapperTreeNode object with specified tree path, x and
     * y coordinations.
     *
     * @param address   the tree path of this mapper node.
     * @param x         the x coordination of this node
     * @param y         the y coordination of this node
     * @param isSource  Description of the Parameter
     * @param isDest    Description of the Parameter
     */
    protected BasicMapperTreeNode(AbstractMapperTree tree,
        TreePath address, boolean isSource, boolean isDest,
        int x, int y) {
        super(x, y);
        this.mTree = tree;
        this.mTreePath = address;
        mIsSourceTreeNode = isSource;
        mIsDestTreeNode = isDest;
    }

    /**
     * Creates a new BasicMapperTreeNode object with specified tree path and 0,0
     * coordination.
     *
     * @param address   tree path of this mapper node
     * @param isSource  Description of the Parameter
     * @param isDest    Description of the Parameter
     */
    public BasicMapperTreeNode(AbstractMapperTree tree, TreePath address, boolean isSource, boolean isDest) {
        this(tree, address, isSource, isDest, 0, 0);
    }

    /**
     * Return the tree path of this mapper node repersenting.
     *
     * @return   the tree path of this mapper node repersenting
     */
    public TreePath getPath() {
        return mTreePath;
    }

    public int getRow() {
        return mTree.getTree().getRowForPath(mTreePath);
    }

    public void expand() {
        mTree.getTree().expandPath(mTreePath);
    }

    /**
     * Return true if this tree node repersents source tree path, false
     * otherwise.
     *
     * @return   true if this tree node repersents source tree path, false
     *      otherwise.
     */
    public boolean isSourceTreeNode() {
        return mIsSourceTreeNode;
    }

    /**
     * Return true if this tree node repersents destination tree path, false
     * otherwise.
     *
     * @return   true if this tree node repersents destination tree path, false
     *      otherwise.
     */
    public boolean isDestTreeNode() {
        return mIsDestTreeNode;
    }

    /**
     * Return true if the specified is logically equal to this object, false
     * otherwise.
     *
     * @param obj  the object to compare.
     * @return     true if the specified is logically equal to this object,
     *      false otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof BasicMapperTreeNode)) {
            return false;
        }

        BasicMapperTreeNode treeAddr = (BasicMapperTreeNode) obj;

        return  treeAddr.mIsDestTreeNode == mIsDestTreeNode
            && treeAddr.mIsSourceTreeNode == mIsSourceTreeNode
            && mTreePath.equals(treeAddr.mTreePath);
    }

    public int hashCode() {
        int hashCode = 0;
        if (mTreePath != null) {
            hashCode = hashCode ^ mTreePath.hashCode();
        }

        hashCode = hashCode
                   ^ Boolean.valueOf(this.mIsDestTreeNode).hashCode()
                   ^ Boolean.valueOf(this.mIsSourceTreeNode).hashCode();
        return hashCode;
    }

    /**
     * Return a cloned BasicMapperTreeNode instance. The tree path will not
     * be cloned, both orginal and cloned instance are referred to the same tree path.
     *
     * @return   a cloned BasicMapperTreeNode instance.
     */
    public Object clone() {
        BasicMapperTreeNode newNode = (BasicMapperTreeNode) super.clone();
        newNode.mIsDestTreeNode = mIsDestTreeNode;
        newNode.mIsSourceTreeNode = mIsSourceTreeNode;
        newNode.mTreePath = mTreePath;
        newNode.mTree = mTree;
        return newNode;
    }
    
    public void addToSelection() {
        mTree.getTree().addSelectionPath(getPath());
    }
    
    public void removeFromSelection() {
        mTree.getTree().removeSelectionPath(getPath());
    }
    
    /**
     * set true if link from this node needs to be highlighted.
     *@param highlight flag
     */
    public void setHighlightLink(boolean highlight) {
        mIsHighlightLink = highlight;
    }
    
    /**
     * check if link from this node needs to be highlighted.
     * @return true if links from this node needs to be highlighted.
     */
    public boolean isHighlightLink() {
        return mIsHighlightLink;
    }
    
    /**
     * set true if link from this node needs to be shown as selected.
     *@param selected flag
     */
    public void setSelectedLink(boolean selected) {
        mIsSelectedLink = selected;
    }
    
    /**
     * check if link from this node needs to be shown as selected.
     * @return true if links from this node needs to be shown as selected.
     */
    public boolean isSelectedLink() {
        return mIsSelectedLink;
    }
}
