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

package org.netbeans.modules.uml.core.metamodel.structure;

import org.netbeans.modules.uml.core.support.umlsupport.IResultCell;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;


/**
 *
 * @author Trey Spiva
 */
public class ProjectEventsAdapter implements IProjectEventsSink
{

   public void onPreModeModified(IProject pProject,
                                 String newValue,
                                 IResultCell cell)
   {

   }

   public void onModeModified(IProject pProject, IResultCell cell)
   {

   }

   public void onPreDefaultLanguageModified(IProject pProject,
                                             String newValue,
                                             IResultCell cell)
   {
      
   }

   public void onDefaultLanguageModified(IProject pProject, IResultCell cell)
   {
      
   }

   public void onProjectPreCreate(IWorkspace space, IResultCell cell)
   {
      
   }

   public void onProjectCreated(IProject Project, IResultCell cell)
   {
      
   }

   public void onProjectPreOpen(IWorkspace space,
                                String projName,
                                IResultCell cell)
   {
      
   }

   public void onProjectOpened(IProject Project, IResultCell cell)
   {
         
   }

   public void onProjectPreRename(IProject Project,
                                  String newName,
                                  IResultCell cell)
   {
      
   }

   public void onProjectRenamed(IProject Project,
                                String oldName,
                                IResultCell cell)
   {
      
   }

   public void onProjectPreClose(IProject Project, IResultCell cell)
   {
      
   }

   public void onProjectClosed(IProject Project, IResultCell cell)
   {
      
   }

   public void onProjectPreSave(IProject Project, IResultCell cell)
   {
      
   }

   public void onProjectSaved(IProject Project, IResultCell cell)
   {
      
   }

   public void onPreReferencedLibraryAdded(IProject Project,
                                           String refLibLoc,
                                           IResultCell cell)
   {
      
   }

   public void onReferencedLibraryAdded(IProject Project,
                                        String refLibLoc,
                                        IResultCell cell)
   {
      
   }

   public void onPreReferencedLibraryRemoved(IProject Project,
                                             String refLibLoc,
                                             IResultCell cell)
   {
      
   }

   public void onReferencedLibraryRemoved(IProject Project,
                                          String refLibLoc,
                                          IResultCell cell)
   {
      
   }

}
