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



package org.netbeans.modules.uml.ui.controls.projecttree;

import java.util.EventObject;

import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeElement;

/**
 *
 * @author Trey Spiva
 */
public class ProjectTreeModelEvent extends EventObject
{
   private boolean      m_IsClosed = false;
   private ITreeElement m_TreeElement = null;
   private IProject     m_AffectProject = null;

   /**
    * Used to create an event when a project has either been closed or opened.
    * @param source
    */
   public ProjectTreeModelEvent(Object       source, 
                                ITreeElement node, 
                                IProject     project,
                                boolean      isClosed)
   {
      super(source);
      setAffectProject(project);
      setTreeElement(node);
      setClosed(isClosed);
   }

   /**
    * Retrieves the project that is affected by the action.
    * 
    * @return The project.
    */
   public IProject getAffectProject()
   {
      return m_AffectProject;
   }

   /**
    * Sets the project that is affected by the action.
    * 
    * @param project The project.
    */
   public void setAffectProject(IProject project)
   {
      m_AffectProject = project;
   }
   
   /**
    * Test if the project is closed.
    * 
    * @return <code>true</code> if the project is closed. 
    */
   public boolean isClosed()
   {
      return m_IsClosed;
   }
   
   /**
    * Specifies that the project is closed or not.
    * @param b <code>true</code> if the project is closed. 
    */
   public void setClosed(boolean b)
   {
      m_IsClosed = b;
   }

   /**
    * Retrieves the tree node that represents the project.
    * 
    * @return The ITreeElement that represent the project.
    */
   public ITreeElement getTreeElement()
   {
      return m_TreeElement;
   }

   /**
    * Sets the tree node that represents the project.
    * 
    * @param element The ITreeElement that represent the project.
    */
   public void setTreeElement(ITreeElement element)
   {
      m_TreeElement = element;
   }

}
