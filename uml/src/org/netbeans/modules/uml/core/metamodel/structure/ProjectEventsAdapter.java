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
