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
 * RemoveSatisfier.java
 *
 * Created on May 20, 2005, 3:01 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.netbeans.modules.uml.requirements;

import org.netbeans.modules.uml.core.IApplication;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.metamodel.structure.IRequirementArtifact;
import org.netbeans.modules.uml.core.requirementsframework.IRequirement;
import org.netbeans.modules.uml.core.requirementsframework.ISatisfier;
import org.netbeans.modules.uml.core.requirementsframework.RequirementsException;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.netbeans.modules.uml.ui.support.ElementReloader;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.UIFactory;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProxyUserInterface;
import org.netbeans.modules.uml.ui.support.commondialogs.IQuestionDialog;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageDialogKindEnum;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageIconKindEnum;
import org.netbeans.modules.uml.ui.support.commondialogs.MessageResultKindEnum;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.util.NbBundle;

/**
 *
 * @author Administrator
 */
public class RemoveSatisfierAction extends AbstractAction
{
    private IProjectTreeItem mTreeItem = null;
    private IProjectTreeControl mTreeControl = null;
    
    /** Creates a new instance of RemoveSatisfier */
    public RemoveSatisfierAction(IProjectTreeItem item, IProjectTreeControl ctrl)
    {
        mTreeItem = item;
        mTreeControl = ctrl;
        
        putValue(AbstractAction.NAME, NbBundle.getMessage(RemoveSatisfierAction.class, "IDS_REMOVE_SATISFIER"));
    }

    public void actionPerformed(ActionEvent actionEvent)
    {
        try
         {
            String strNodeName = null;
            String strParentNodeName = null;

            ETList < IProjectTreeItem > projectTreeItems2 = new ETArrayList < IProjectTreeItem >();
            //for( int lIdx = 0; lIdx < projectTreeItems.length; lIdx++ )
            {
               // Get the Next Selected item.
               IProjectTreeItem projectTreeItem = mTreeItem;
               ITreeItem[] path = mTreeItem.getPath();

               // Also get its Parent.
               ITreeItem parentItem = path[path.length - 1].getParentItem();
               IProjectTreeItem parentProjectTreeItem = parentItem.getData();

               // Retrieve Names of Selected Node and its Parent.
               strParentNodeName = parentProjectTreeItem.getItemText();
               strNodeName = projectTreeItem.getItemText();

               // Filter out only 'Satisfiers' (happens when a multi-range selection is made from tree).
               if( strNodeName.equals(" Satisfiers") || strParentNodeName.equals(" Satisfiers") )
               {
                  // Add elements to a second collection to be deleted later.
                  //	 projectTreeItems2.add( projectTreeItem )

                  if( strParentNodeName.equals(" Satisfiers") )
                  {
                     removeItemFromProxy( mTreeControl, projectTreeItem, parentProjectTreeItem );

                     // Add elements to a second collection to be deleted later.
                     projectTreeItems2.add( projectTreeItem );
                  }
                  else if( strNodeName.equals(" Satisfiers") )
                  {
                     // Remove all of the Children.
                     ETList < IProjectTreeItem > childProjectTreeItems = mTreeControl.getChildren( projectTreeItem );
                     long lChildCount = childProjectTreeItems.size();

                     // Iterate through all the children.
                     for( int childIndex = 0; childIndex < lChildCount; childIndex++ )
                     {
                        IProjectTreeItem childProjectTreeItem = childProjectTreeItems.get( childIndex );

                        // Delete the child from the Proxy file.
                        removeItemFromProxy( mTreeControl, childProjectTreeItem, projectTreeItem );
                     }  // End For..Loop
                     
                     // Add elements to a second collection to be deleted later.
                     projectTreeItems2.add( projectTreeItem );
                     
                  }  // EndElse - Node is a Satisfiers folder
               }  // EndIf - Node is a satisfier(s} folder

            }  // EndIf - TreeItem selectedItems > 0

            // Delete the items from the second 'filtered' collection.

            if(projectTreeItems2.size() > 0)
            {
               IProjectTreeItem[] removeItems = new IProjectTreeItem[projectTreeItems2.size()];
               projectTreeItems2.toArray(removeItems);
               mTreeControl.removeFromTree( removeItems );
            }
         }
         catch(RequirementsException e)
         {

         }
    }

    /**
    * Removes the Satisfer element from the Requirements Provider and also from
    * the Proxy file.
    *
    * @param pTreeControl [in] The DesignCenter's TreeControl
    * @param pProjectTreeItem [in] The Satisfier node
    * @param pParentProjectTreeItem [in] The 'Satisfiers Folder' node.
    */
   public void removeItemFromProxy( IProjectTreeControl pTreeControl,
                                    IProjectTreeItem pProjectTreeItem,
                                    IProjectTreeItem pParentProjectTreeItem )
      throws RequirementsException
   {
      if( null == pTreeControl ||
          pProjectTreeItem == null ||
          pParentProjectTreeItem == null)
      {
         throw new IllegalArgumentException();
      }

      try
      {
         // Satisfiers Folder node's parent is the Requirement.
         IProjectTreeItem  cpRequirementParentProjectTreeItem = pTreeControl.getParent( pParentProjectTreeItem );

         IRequirement cpRequirement = getRequirementFromTreeItem( cpRequirementParentProjectTreeItem );

         String strSatisfierXMIID = pProjectTreeItem.getSecondaryDescription( );

         ETList < ISatisfier >  cpSatisfiers = cpRequirement.getSatisfiers();

         long lSatisfierCount = cpSatisfiers.size();

         for( int lIdx = 0; lIdx < lSatisfierCount; lIdx++ )
         {
            ISatisfier cpSatisfier = cpSatisfiers.get(lIdx);

            String strSatisfierID = cpSatisfier.getXMIID();
            if( strSatisfierID.equals(strSatisfierXMIID) == true )
            {
               // Delete from IRequirement's collection.
               cpRequirement.removeSatisfier( cpSatisfier );

               // Delete from the Proxy File
               ADRequirementsManager.instance().deleteProxy( cpRequirement, cpSatisfier );

               // Now remove the Requirement Artifact from the owning element.

               // Get the Element's ProjectID.
               String strProjectID = cpSatisfier.getProjectID();
               String strProjectName = cpSatisfier.getProjectName();

               // Get the current Application so we can ask it things.
               IApplication cpApplication = ProductHelper.getApplication();

               // Ask the App for the ProjectInstance using the element's toplevelid.
               IProject cpProject = cpApplication.getProjectByID( strProjectID );

               ElementReloader reloader = new ElementReloader();
               IElement pElement = reloader.getElement(strProjectID, strSatisfierID);
               if (pElement instanceof INamespace)
               {
                  INamespace  cpNamespace = (INamespace)pElement;
                  if( cpNamespace != null )
                  {
                     ETList < INamedElement > cpNamedElements = cpNamespace.getOwnedElements();

                     if( (cpNamedElements != null) && (cpNamedElements.size() > 0) )
                     {
                        long lCnt = cpNamedElements.size();

                        String strRequirementID = cpRequirement.getID();
                        String strRequirementSourceID = cpRequirement.getSourceID();

                        for( int lIndx=0; lIndx<lCnt; lIndx++ )
                        {
                           INamedElement cpNamedElement = cpNamedElements.get(lIndx);
                           if( cpNamedElement instanceof IRequirementArtifact )
                           {
                              IRequirementArtifact cpArtifact = (IRequirementArtifact)cpNamedElement;
                              String strArtifactRequirementID = cpArtifact.getRequirementID();

                              String strArtifactRequirementSourceID = cpArtifact.getRequirementSourceID();

                              if( (strRequirementID.equals(strArtifactRequirementID) == true) &&
                                  (strRequirementSourceID.equals(strArtifactRequirementSourceID) == true))
                              {
                                 cpArtifact.delete();
                                 break;
                              }
                           }
                        }
                     }
                  }
               }
               break;
            }
         }
      }
      catch( RequirementsException err )
      {
         if( err.getExceptionCode() == RequirementsException.RP_E_SHAREDEDITNOTSUPPORTED )
         {
            Frame handle = null;

            IProxyUserInterface cpProxyUserInterface = ProductHelper.getProxyUserInterface();
            if( cpProxyUserInterface != null)
            {
               handle = cpProxyUserInterface.getWindowHandle();
            }

            String msgText = err.getLocalizedMessage();
            String msgTitle = NbBundle.getMessage(ADRequirementsManager.class,"IDS_REQUIRENTERRORTITLE" );

            IQuestionDialog question = UIFactory.createQuestionDialog();
            question.displaySimpleQuestionDialog(MessageDialogKindEnum.SQDK_OK,
                                                 MessageIconKindEnum.EDIK_ICONWARNING,
                                                 msgText,
                                                 MessageResultKindEnum.SQDRK_RESULT_OK,
                                                 handle,
                                                 msgTitle);
         }
      }
   }
   
   public IRequirement getRequirementFromTreeItem( IProjectTreeItem pTreeItem)
   {
      IRequirement retVal = null;

      if( null == pTreeItem)
      {
         throw new IllegalArgumentException();
      }


      Object cpDispatch = pTreeItem.getData();

      if( cpDispatch instanceof IRequirement )
      {
         retVal = (IRequirement)cpDispatch;
      }

      return retVal;
   }
}
