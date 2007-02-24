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


package org.netbeans.modules.uml.project.ui.nodes;

import org.openide.util.Lookup;

import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeWorkspace;

/**
 *
 * @author Trey Spiva
 */
public class UMLWorkspaceNode extends UMLElementNode implements ITreeWorkspace
{
   private IWorkspace m_Workspace = null;

   /**
    *
    */
   public UMLWorkspaceNode()
   {
      super();
   }

   /**
    * 
    */
   public UMLWorkspaceNode(Lookup l)
   {
      super(l);
   }

   /**
    * @param item
    * @throws NullPointerException
    */
   public UMLWorkspaceNode(IProjectTreeItem item) throws NullPointerException
   {
      super(item);
   }

   /**
    * @param item
    * @throws NullPointerException
    */
   public UMLWorkspaceNode(Lookup l, IProjectTreeItem item) throws NullPointerException
   {
      super(l, item);
   }

   
   public IWorkspace getWorkspace()
   {
      return m_Workspace;
   }
   
   public void setWorkspace(IWorkspace pVal)
   {
      m_Workspace = pVal;
   }
   
   public String getType()
   {
      return "Workspace";
   }

}
