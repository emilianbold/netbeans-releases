/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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




