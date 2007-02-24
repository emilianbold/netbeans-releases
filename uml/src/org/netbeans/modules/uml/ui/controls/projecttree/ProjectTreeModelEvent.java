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
