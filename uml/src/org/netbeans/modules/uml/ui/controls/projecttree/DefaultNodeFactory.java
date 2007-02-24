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



package org.netbeans.modules.uml.ui.controls.projecttree;

import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeDiagram;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeElement;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeFolder;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeRelElement;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeWorkspace;

/**
 * 
 * @author Trey Spiva
 */
public class DefaultNodeFactory implements ProjectTreeNodeFactory
{

   /**
    * 
    */
   public DefaultNodeFactory()
   {
      super();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.ProjectTreeNodeFactory#createDiagramNode(org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram)
    */
   public ITreeDiagram createDiagramNode(IProxyDiagram proxy)
   {      
      return new ProjectTreeDiagramNode(proxy);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.ProjectTreeNodeFactory#createFolderNode()
    */
   public ITreeFolder createFolderNode()
   {
      return new ProjectTreeFolderNode();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.ProjectTreeNodeFactory#createProjectNode()
    */
   public ITreeItem createProjectNode()
   {
      return new ProjectTreeNode();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.ProjectTreeNodeFactory#createElementNode()
    */
   public ITreeElement createElementNode()
   {
      return new TreeElementNode();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.ProjectTreeNodeFactory#createRelationshipNode()
    */
   public ITreeRelElement createRelationshipNode()
   {
      return new TreeRelationshipNode();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.controls.projecttree.ProjectTreeNodeFactory#createWorkspaceNode()
    */
   public ITreeWorkspace createWorkspaceNode()
   {
      return new TreeWorkspaceNode();
   }

}
