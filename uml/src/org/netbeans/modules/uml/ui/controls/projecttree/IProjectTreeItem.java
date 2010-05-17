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

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IElementImport;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IPackageImport;
import org.netbeans.modules.uml.core.metamodel.core.foundation.IProxyElement;
import org.netbeans.modules.uml.core.metamodel.diagrams.IProxyDiagram;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.ui.support.projecttreesupport.ITreeItem;
import javax.swing.Action;
import org.openide.nodes.Node;

/**
 * Represents a node in the tree.  It has a model element, data element, description and a
 * path that specifies the location of the item in the tree.
 * 
 * @author Trey Spiva
 */
public interface IProjectTreeItem extends Node.Cookie
{
   /** The model element associated with this item in the tree, as a proxy */ 
   public IProxyElement getProxyElement();
   /**
    * Retrieve the path to the project tree item.  The is specified
    * by an array of item in the tree.  The element in the array are
    * ordered such that the first element is the root element and the
    * last element is the IProjectTreeItem.
    * 
    * @return The path.
    */
   ITreeItem[] getPath();
   
   /**
    * Set the path to the to the project tree item.  The is specified
    * by an array of item in the tree.  The element in the array are
    * ordered such that the first element is the root element and the
    * last element is the IProjectTreeItem.
    * 
    * @param path The location of the item.
    */
   public void setPath(ITreeItem[] path);
   
   
      
   /**
    * The model element associated with this item in the tree
    */
   public IElement getModelElement();

   /**
    * The XMI ID represented of the model element.
    * 
    * @return The id of the model element.
    */
   public String getModelElementXMIID();
   
   /**
    * The XMI ID of the top element of the model element.
    * 
    * @return The id of the top model element.
    */   
   public String getTopLevelXMIID();

   /**
    * Set the model element assocated with the project tree element.
    * Once a model element is assocated with the project tree element
    * then isModelElement will return true.
    * 
    * @param element
    */
   public void setModelElement(IElement element);
   
   /**
    * Does this tree item represent a model element?
    */
   public boolean isModelElement();

   /**
    * Is this item the same as the passed in one.
    */
   public boolean isSameModelElement( IElement pQueryItem );

   /**
    * Does this tree item represent an imported element?
    */
   public boolean isImportedModelElement();

   /**
    * Retrieves the imported model element.
    * 
    * @return The imported element.  <code>null</code> will be
    *         returned if the project tree item is not an imported
    *         element. 
    * @see isImportedModelElement
    */
   public IElementImport getImportedModelElement();

   /**
    * Does this tree item represent an imported package?
   */
   public boolean isImportedPackage();

   /**
    * Retrieves the imported package.   
    * 
    * @return The imported package.  <code>null</code> will be
    *         returned if the project tree item is not an imported
    *         package. 
    * @see isImportedPackage
    */
   public IPackageImport getImportedPackage();

   /**
    * The model element metatype associated with this item in the tree
    */
   public String getModelElementMetaType();

   /**
    * Sets the model element metatype associated with this item in the tree
    */
   public void setModelElementMetaType(String value);
   
   /**
    * The Object that is the data of the tree item.
    */
   public Object getData();

   /**
    * The model element tree data can have an Object associated with it.  This
    * routine allows you to set the additional data Object.
    * 
    * @param data The additional data.
    */
   public void setData( Object value );

   /**
    * A description of the node
    */
   public String getDescription();

   /**
    * A description of the node
    */
   public void setDescription( String value );

   /**
    * A secondary description of the node
    */
   public String getSecondaryDescription();

   /**
    * A secondary description of the node
    */
   public void setSecondaryDescription( String value );

   /**
    * Is this item the same as the passed in one.
    */
   public boolean isSame( IProjectTreeItem pQueryItem );

   /**
    * How this item should be sorted in the tree.  1 = top of the list..
    */
   public long getSortPriority();

   /**
    * How this item should be sorted in the tree.  1 = top of the list..
    */
   public void setSortPriority( long value );

   /**
    * Does this tree item represent a diagram?  This is based on
    * if the description field is a .etl file.
    */
   public boolean isDiagram();

   /**
    * Retrieve the diagram proxy for that is represented by the 
    * project tree item.  
    * 
    * @return The diagram proxy.  <code>null</code> will be
    *         returned if the project tree item is not a diagram.
    * @see isDiagram
    */
   public IProxyDiagram getDiagram();

   /**
    * Is this item a project.
    */
   public boolean isProject();

   /**
    * Retrieves the project that is represented by the project 
    * tree item. 
    * 
    * @return The IProject object.  <code>null</code> will be
    *         returned if the project tree item is not a project.
    * @see isProject
    */
   public IProject getProject(  );

   /**
    * Is this item a workspace.
   */
   public boolean isWorkspace();

   /**
    * The current item text of the node
    */
   public String getItemText();

   /**
    * The current item text of the node
    */
   public void setItemText( String value );

   /**
    * The project tree support ITreeItem associated with this item in the tree
   */
   public ITreeItem getProjectTreeSupportTreeItem();

   /**
    * The project tree support ITreeItem associated with this item in the tree
   */
   public void setProjectTreeSupportTreeItem( ITreeItem value );
   
   public void setAsAddinNode(boolean val);
   
   public boolean isAddinNode();
   
   public void setActions(Action[] actions);
   
   public Action[] getActions();

}
