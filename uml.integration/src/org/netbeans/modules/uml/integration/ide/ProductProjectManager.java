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
 * File         : ProductProjectManager.java
 * Version      : 1.2
 * Description  : Tracks and manages open Describe projects
 * Author       : Ashish
 */
package org.netbeans.modules.uml.integration.ide;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.netbeans.modules.uml.core.IApplication;
import org.netbeans.modules.uml.core.coreapplication.IPreferenceManager2;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagram;
import org.netbeans.modules.uml.core.metamodel.diagrams.IDiagramKind;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlsupport.FileSysManip;
import org.netbeans.modules.uml.core.workspacemanagement.IWSProject;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;
import org.netbeans.modules.uml.ui.controls.newdialog.INewDialog;
import org.netbeans.modules.uml.ui.controls.newdialog.INewDialogDiagramDetails;
import org.netbeans.modules.uml.ui.controls.newdialog.INewDialogProjectDetails;
import org.netbeans.modules.uml.ui.controls.newdialog.INewDialogTabDetails;
import org.netbeans.modules.uml.ui.controls.newdialog.NewDialog;
import org.netbeans.modules.uml.ui.controls.newdialog.NewDialogDiagramDetails;
import org.netbeans.modules.uml.ui.controls.newdialog.NewDialogProjectDetails;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeControl;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductDiagramManager;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductProjectManager;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem;
import org.netbeans.modules.uml.ui.swing.projecttree.DesignCenterSwingModel;
import org.netbeans.modules.uml.ui.swing.projecttree.ISwingProjectTreeModel;
import org.netbeans.modules.uml.ui.swing.projecttree.JProjectTree;

/**
 *  Tracks and manages open Describe projects.
 *
 * Revision History
 * No.  Date        Who         What
 * ---  ----        ---         ----
 *   1  2002-05-03  Sumitabh    Added new methods displayInsertProjectDialog()
 *                              and displayNewProjectDialog() introduced in
 *                              Wolverine build 64.
 * @author  Ashish
 * @version 1.0
 */
public class ProductProjectManager implements IProductProjectManager {
    public void setCurrentProject( IProject proj ) {
        mCurrentProject = proj;
    }

    public IProject getCurrentProject() {
        return mCurrentProject;
    }

    /**
     * This method asks the user for new project details and create a new project.
     */
    public void displayNewProjectDialog() 
    {
       INewDialogProjectDetails details = new NewDialogProjectDetails();
       details.setAllowFromRESelection(false);
       details.setMode("Analysis");
       displayNewProjectDialog(details);
    }

    public void displayInsertProjectDialog(IWorkspace wks) 
    {
       JFileChooser chooser = new JFileChooser();

       chooser.setAcceptAllFileFilterUsed(false);
       chooser.setMultiSelectionEnabled(false);
       chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
       chooser.addChoosableFileFilter(new FileFilter() 
       {
          public boolean accept(File file) {
              return file.isDirectory() ||
                  file.toString().toLowerCase().endsWith(UMLSupport.getString("Dialog.InsertProject.ProjectExtension"));
          }

          public String getDescription() {
              return UMLSupport.getString("Dialog.InsertProject.FileFilter.Description");
          }
       });
       
       if (chooser.showOpenDialog(chooser) == JFileChooser.APPROVE_OPTION)
       {
          File file = chooser.getSelectedFile();
          if (file != null)
          {
             IApplication pApp = UMLSupport.getUMLSupport().getApplication();
             if (pApp != null)
             {
                IProject proj = pApp.openProject(file.getAbsolutePath());
                if (proj != null)
                {
                   insertProjectIntoWorkspace(wks, proj);
                }
             }
          }
       }
    }

   /**
    * Inserts the argument project into the passed in IWorkspace
    */
   private boolean insertProjectIntoWorkspace(IWorkspace pWorkspace, IProject pProject)
   {
      boolean inserted = false;
      IApplication pApp = UMLSupport.getUMLSupport().getApplication();
      if (pApp != null)
      {
         IWSProject appProj = pApp.importProject(pWorkspace, pProject);
         inserted = true;
         
         IProjectTreeControl control = ProductHelper.getDesignCenterTree();
         if (control != null && control instanceof JProjectTree)
         {
            JProjectTree tree = (JProjectTree)control;
            ISwingProjectTreeModel model = tree.getProjectModel();
            if (model != null)
            {
               IWorkspace space = model.getWorkspace();
               if (space != null && space.equals(pWorkspace))
               {
                  //we need to refresh the design center project tree for the inserted project
                  //as the tree never gets notified for the added project
                  if (model instanceof DesignCenterSwingModel)
                  {
                     ITreeItem parent = ((DesignCenterSwingModel)model).getWorkspaceNode(pWorkspace, null);
                     ((DesignCenterSwingModel)model).addProject(pProject.getName(), parent, pProject);
                  }
               }
            }
         }
         
         pWorkspace.setIsDirty(true);
      }
      return inserted;
   }

    private IProject mCurrentProject;

   /**
    * Brings up the new project dialog
    */
    public void displayNewProjectDialog(INewDialogProjectDetails pDetails) 
    {
        IProject retProj = null;
        if (pDetails == null)
        {
           pDetails = new NewDialogProjectDetails();
           pDetails.setAllowFromRESelection(false);
        }
        
        INewDialog pDialog = new NewDialog();
        if (pDialog != null)
        {
           // Allow only the project tab
           pDialog.addTab(NewDialogTabKind.NWIK_NEW_PROJECT);
           if (pDetails != null)
           {
              pDialog.specifyDefaults(pDetails);
           }
           
           // Now display the dialog
           INewDialogTabDetails pResults = pDialog.display(null);
           
           if (pResults != null && pResults instanceof INewDialogProjectDetails)
           {
              // User hit ok, should be project results
              INewDialogProjectDetails projDetails = (INewDialogProjectDetails)pResults;
              
              retProj = newProject(projDetails);
              if (retProj != null)
              {
                 // This call will ask the user to create a diagram if a preference is set.
                 doPostNewProject(retProj, pDetails);
              }
           }
        }
    }
    
   /**
    * Tells the IADApplication to create a new project.
    *
    * @param pFilename [in] The details about the new project
    * @param pOpenedProject [out] The new project that was created.
    */
   public IProject newProject(INewDialogProjectDetails pDetails)
   {
      IProject proj = null;
      if (pDetails != null)
      {
         String location = pDetails.getLocation();
         String name = pDetails.getName();
         
         String fullFilename = FileSysManip.createFullPath(location, name, ".etd");
         
         //we do not need to do it here - as the new dialog display would have already created a project.
         //INewDialogResultProcessor processor = new NewDialogResultProcessor();
         //processor.handleResult(pDetails);
         
         IApplication pApp = UMLSupport.getUMLSupport().getApplication();
         
         if (pApp != null)
         {
            proj = pApp.getProjectByFileName(fullFilename);
         }
      }
      return proj;
   }

   /**
    * After a project we need to question the user about creating a new diagram.  The 
    * INewDialogProjectDetails can specify that whether or not to question the user.
    *
    * @param pProject [in] The project that was just created.
    * @param pDetails [in] The new project details.
    */
   public void doPostNewProject(IProject pProject, INewDialogProjectDetails pDetails) {
       if (pProject != null) {
           boolean promptUser = true;
           if (pDetails != null) {
               promptUser = pDetails.getPromptToCreateDiagram();
           }
           
           if (promptUser) {
               
               // Query the user for a new diagram
               INamespace space = (INamespace)pProject;
               queryUserForNewDiagram(space, IDiagramKind.DK_UNKNOWN, IDiagramKind.DK_ALL);
               
           }
       }
   }
    
   public IDiagram queryUserForNewDiagram(INamespace pNamespace, int diaKind, int availableKinds)
   {
      IDiagram retDia = null;
      INewDialog diag = new NewDialog();
      diag.addTab(NewDialogTabKind.NWIK_NEW_DIAGRAM);
      INewDialogDiagramDetails details = new NewDialogDiagramDetails();
      details.setNamespace(pNamespace);
      details.setDiagramKind(diaKind);
      details.setAvailableDiagramKinds(availableKinds);
      
      diag.specifyDefaults( details );
      
      INewDialogTabDetails results = diag.display( null );
      
      if( results != null && results instanceof INewDialogDiagramDetails)
      {
         INewDialogDiagramDetails elementResults = (INewDialogDiagramDetails)results;
         if( elementResults != null )
         {
            INamespace space = elementResults.getNamespace();
            String name = elementResults.getName();
            int kind = elementResults.getDiagramKind();
            if (name != null && name.length() > 0)
            {
               retDia = newDiagram(space, kind, name);
            }
         }
      }
      return retDia;
   }
   
   public IDiagram newDiagram(INamespace space, int diaKind, String name)
   {
      IDiagram retDia = null;
      IProductDiagramManager diaMan = ProductHelper.getProductDiagramManager();
      if (diaMan != null)
      {
         if (retDia == null)
         {
            if (space != null)
            {
               retDia = diaMan.createDiagram(diaKind, space, name, null);
            }
            else
            {
               IProductProjectManager mgr = ProductHelper.getProductProjectManager();
               if (mgr != null)
               {
                  IProject proj = mgr.getCurrentProject();
                  if (proj != null)
                  {
                     retDia = diaMan.createDiagram(diaKind, proj, name, null);
                  }
               }
            }
         }
      }
      return retDia;
   }

    public java.util.ArrayList<IProject> getOpenProjects()
    {
        return null;
    }
   
}
