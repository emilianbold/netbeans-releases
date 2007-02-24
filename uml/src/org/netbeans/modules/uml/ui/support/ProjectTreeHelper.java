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
 * ProjectTreeHelper.java
 *
 */

package org.netbeans.modules.uml.ui.support;

import java.beans.PropertyVetoException;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import java.io.File;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.uml.common.RelationshipCookie;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.openide.awt.StatusDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Administrator
 */
public class ProjectTreeHelper
{
   private static TopComponent projectTabComp = WindowManager.getDefault().findTopComponent( "projectTabLogical_tc" );
   private static final RequestProcessor RP = new RequestProcessor();
      
   public static boolean findElementInProjectTree(IElement element)
   {
      Project project = findReferencingProject(element);
      if ( project == null)
      {
         return false;
      }
      
      if (projectTabComp != null)
      {
         ExplorerManager explorerManager =
                 ((ExplorerManager.Provider)projectTabComp).getExplorerManager();
         Node root = explorerManager.getRootContext();
         Children c = root.getChildren();
         Node[] projectNodes = c.getNodes(true);
         for (int i=0; i<projectNodes.length; i++)
         {
            Project p = (Project) projectNodes[i].getLookup().lookup(Project.class);
            if (p == project)
            {
               Node selectedNode = findNode(projectNodes[i],  element);
               selectProjectNodeAsync(selectedNode);
               return true;
            }
         }
      }
      return false;
   }
   
   public static Project findElementOwner(IElement element)
   {
      Project retVal = null;
      if(element != null)
      {
         IElement owningElement = element.getOwner();
         if (owningElement != null)
         {
            IProject project = owningElement.getProject();
            retVal = findNetBeansProjectForModel(project);
         }
         else
         {
            retVal = findNetBeansProjectForModel(element.getProject());
         }
      }
      
      return retVal;
   }
   
   public static Project findReferencingProject(IElement element)
   {
      Project retVal = null;
      if(element != null)
      {
         IProject project = element.getProject();
         retVal = findNetBeansProjectForModel(project);
      }
      
      return retVal;
   }
   
   public static Project findNetBeansProjectForModel(IProject project)
   {
      Project retVal = null;
      
      if(project != null)
      {
         String filename = project.getFileName();
         if((filename != null) && (filename.length() > 0))
         {
            FileObject fo = FileUtil.toFileObject(new File(filename));
            retVal = FileOwnerQuery.getOwner(fo);
         }
      }
      
      return retVal;
   }
   
   public static Node findNode(Node root, IElement element)
   {
      if (root.isLeaf())
         return null;
      
      Children children = root.getChildren();
      Node[] nodes = children.getNodes(true);
      for (int j=0; j<nodes.length; j++)
      {
         IProjectTreeItem item = (IProjectTreeItem)nodes[j].
                 getCookie(IProjectTreeItem.class);
         if (item != null)
         {
            IElement modelElement = item.getModelElement();
            if (modelElement == null) // could be a diagram node
            {
               if (element instanceof IDiagram)
               {
                  // the unique diagram file name is used to determine
                  // if two diagram objects are same
                  if (item.getDiagram()!=null && item.getDescription().
                          equals(((IDiagram)element).getFilename()))
                     return nodes[j];
               }
            }
            if (modelElement!=null &&
                    element.getXMIID().equals(modelElement.getXMIID()))
            {
               if (nodes[j].getCookie(RelationshipCookie.class) != null)
                  continue;
               return nodes[j];
            }
         }
         if (nodes[j].isLeaf())
            continue;
         Node val = findNode(nodes[j],  element);
         if (val!=null)
            return val;
      }
      return null;
   }
   
   public static void selectProjectNodeAsync(final Node selectedNode)
   {
      if (projectTabComp == null)
         return ;
      
      final ExplorerManager manager =
              ((ExplorerManager.Provider)projectTabComp).getExplorerManager();
      projectTabComp.setCursor( Utilities.createProgressCursor( projectTabComp ) );
      projectTabComp.open();
      projectTabComp.requestActive();
      
      // Do it in different thread than AWT
      RP.post( new Runnable()
      {
         public void run()
         {
            // Back to AWT
            SwingUtilities.invokeLater( new Runnable()
            {
               public void run()
               {
                  if ( selectedNode != null )
                  {
                     try
                     {
                        manager.setSelectedNodes( new Node[] { selectedNode } );
                        StatusDisplayer.getDefault().setStatusText( "" ); // NOI18N
                     }
                     catch ( PropertyVetoException e )
                     {
                        // Bad day node found but can't be selected
                     }
                  }
                  else
                  {
                     StatusDisplayer.getDefault().setStatusText(
                             NbBundle.getMessage( ProjectTreeHelper.class,  "MSG_NodeNotFound" ));
                  }
                  projectTabComp.setCursor( null );
               }
            } );
         }
      } );
      
   }
}
