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
 *
 * Created on Jun 10, 2003
 * @author Trey Spiva
 */
package org.netbeans.modules.uml.ui.swing.projecttree;

import javax.swing.JTree;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModel;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem;

/**
 * 
 * @author Trey Spiva
 */
public interface ISwingProjectTreeModel extends TreeModel, IProjectTreeModel
{

   /**
    * Retrieves the project item based on a TreePath.
    * 
    * @param path The path to the item.
    * @return The project tree item.
    */
   public ITreeItem getTreeItem(TreePath path);

   /**
    * Clears the content of the model.  This basically a refresh.
    */
   public void clear();
   
   /**
    * Returns the name of the project tree type - could be designcenter or projecttree.
    */
   public String getProjectTreeName();
   
   public void setProjectTree(JTree tree);
   
   public IProjectTreeItem addWorkspace(ITreeItem pParent, IWorkspace space);
   
   public void closeProject(IProject proj);
}
