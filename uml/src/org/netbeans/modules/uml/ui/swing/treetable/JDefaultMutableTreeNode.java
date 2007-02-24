/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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


/*
 * Created on Jun 27, 2003
 *
 */
package org.netbeans.modules.uml.ui.swing.treetable;

import java.util.Enumeration;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

/**
 * @author sumitabhk
 *
 */
public class JDefaultMutableTreeNode extends DefaultMutableTreeNode
{
	private String m_Key = null;
	private int m_Row = 0;
	private boolean m_ExpandedState = false;
	
	//this variable will be used by the node to decide if it is one of the roots.
	private boolean m_IsRoot = false;

	/**
	 *
	 */
	public JDefaultMutableTreeNode()
	{
		super();
	}
	
	public JDefaultMutableTreeNode(Object userObj)
	{
		super(userObj);
	}
	
	public JDefaultMutableTreeNode(Object userObj, boolean allowChild)
	{
		super(userObj,allowChild);
	}
	
	public String getKey()
	{
		return m_Key;
	}
	
	public void setKey(String key)
	{
		m_Key = key;
	}

	public int getRow()
	{
		if (m_Row != 0) {
                    return m_Row;
                }
                int row = 0;
                TreeNode nodeWhoseIndexIsNeeded = this;
                TreeNode treeNode = this.getParent();
                TreeNode root = this.getRoot();
                while (!root.equals(treeNode))
                {
                        int index = treeNode.getIndex(nodeWhoseIndexIsNeeded);
                        for (int x = 0; x < index; x++) {
                            row += countAllNodes(treeNode.getChildAt(x));
                        }
                        row++;
                        nodeWhoseIndexIsNeeded = treeNode;
                        treeNode = treeNode.getParent();
                }

                int index = treeNode.getIndex(nodeWhoseIndexIsNeeded);
                for (int x = 0; x < index; x++) {
                    row += countAllNodes(treeNode.getChildAt(x));
                }
                row++;
		return row;
	}
	
	public void setRow(int row)
	{
		m_Row = row;
	}
	
	public void setExpanded(boolean expand)
	{
		m_ExpandedState = expand;
	}
	
	public boolean isExpanded()
	{
		return m_ExpandedState;
	}
	
	public boolean isRoot()
	{
		return m_IsRoot;
	}
	
	public void setIsRoot(boolean val)
	{
		m_IsRoot = val;
	}
        
        private int countAllNodes(TreeNode node) {
            if (node.isLeaf()) {
                return 1;
            }
            int res = 1;
            for (Enumeration e = node.children(); e.hasMoreElements(); ) {
                TreeNode ch = (TreeNode) e.nextElement();
                res += countAllNodes(ch);
            }
            return res;
        }
}




