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
 *
 * Created on Jun 11, 2003
 * @author Trey Spiva
 */
package org.netbeans.modules.uml.ui.support.projecttreesupport;

import java.util.Comparator;
import java.util.Enumeration;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeItem;
import org.openide.nodes.Node;


/**
 *
 * @author Trey Spiva
 */
public interface ITreeItem extends Node.Cookie
{
   /**
    * Sets the name of the tree item. The name is not the displayed name.
    * The name is a program name that will not be translated into
    * localized values, and will not have a aliased value.
    * 
    * @return The name of the tree item.
    */
   public void setName(String name);
   
   /**
    * Gets the name of the tree item. The name is not the displayed name.  
    * The name is a program name that will not be translated into 
    * localized values, and will not have a aliased value.
    * 
    * @return The name of the tree item.
    */
   public String getName();
   
   /**
    * Sets the displayed name or the alias of the tree item depending on
    * the preferences.
    * 
    * @return The displayed name of the tree item.
    */
   public void setDisplayedName(String name);

   /**
    * Sets display Name of the property definition.
    * 
    * @param value the display name.
    * @param buildProperties specifing that we are actually initalizing the node
    */
   public void setDisplayedName(String value, boolean buildProperties);
   
   /**
    * Gets the displayed name or the alias of the tree item depending on
    * the preferences.
    * 
    * @return The displayed name of the tree item.
    */
   public String getDisplayedName();
      
   /**
    * The path that was used when this item was created.  The items in the
    * path are ITreeItem elements.
    * 
    * @return The path to the tree item.
    */
   public Object[] getPath();
   
   public String getPathAsString();
   public void setPathAsString(String str);

   
   /**
    * Is this item the same as the passed in one.
    * 
    * @param queryItem The item to test against.
    * @return true if the two tree items are the same,
    *         false otherwise.
    */
   public boolean isSame(ITreeItem queryItem);
   
   /**
    * Retrieves the parent of this tree item,
    * 
    * @return The parent tree item.
    */
   public ITreeItem getParentItem();
   
   /**
    * Sets the parent of this tree item,
    * 
    * @return The parent tree item.
    */
   public void setParentItem(ITreeItem parent);
   
   /** 
    * Retrieves the top most parent (not including the project).
    * 
    * @return The parent tree item.
    */
   public ITreeItem getTopParentItem();
   
   
   /**
    * Retrieves the top tree element.
    * 
    * @return The Tree Element.
    */
   public ITreeElement getOwningTreeElement();
   
   /**
    * The type of the tree element.
    * 
    * @return The type.
    */
   public String getType();
   
   /**
    * Adds a new child to the node.
    * 
    * @param item The child to add.
    */
   public void addChild(ITreeItem item);
   
   /**
    * Inserts a child into the parent child list.  The new child will be 
    * inserted into the specified location.  If the specified index is 
    * greater than the number of children in the child list the child 
    * will be appended to the end of the child list.
    * 
    * @param item The child to add.
    * @param index The index of the child.
    */
   public void insertAt(ITreeItem item, int index);
   
   /**
    * Remove a child from the node.
    * 
    * @param item The node to remove.
    */
   public void removeChild(ITreeItem item);
   
   /**
    * Removes all children from the node.
    */
   public void removeAllChildren();
   
   /**
    * Retrieve a child from the node.  The node to retrieve is specified by its
    * index.
    * 
    * @param index The child to retrieve. 
    * @return The child.
    */
   public ITreeItem getChild(int index);
   
   /**
    * Sorts the children of the tree item.  The children will be sorted occuring
    * to the default sort order.
    */
   public void sortChildren();
   
   /**
    * Sorts the children of the tree item.  The children will be sorted occuring
    * to the Comparable interface.
    * 
    * @param parent The parent who children are to be sorted.
    * @param compare The comparable interface used to sort the children.
    * @see Comparable
    */
   public void sortChildren(Comparator compare);
   
   /**
    * Retrieve a collection that contains all of the children contained by the
    * node.  
    * <br>
    * <i>Note:</i> This collection should be treated as <b>read-only</b>.  If a
    * node is added to the returned collection any associated view will not be
    * updated until a refresh is performed.
    * 
    * @return The collection of children.
    */
   public Enumeration < ITreeItem > getNodeChildren();
   
   /**
    * The nodes data.
    * @return The data.
    */
   public IProjectTreeItem getData();

   /**
    * Retrieve the number of children that the node contains.
    * 
    * @return The number of children.
    */
   public int getChildCount();
   
   //public Node getXMLNode();
   
   /**
    * Specifies whether or not the node has ever been expanded.  If the
    * node has been expanded before then its children has already been
    * set.  Otherwise, the node has not been completely initialized yet.
    * 
    * @return <b>true</b> if the node has been initalized, <b>false</b>
    *         if the node has not been initialized.
    * @hidden
    */
   public boolean isInitalized();
   
   /**
    * Specifies whether or not the node has ever been expanded.  If the
    * node has been expanded before then its children has already been
    * set.  Otherwise, the node has not been completely initialized yet.
    * 
    * @param value <b>true</b> if the node has been initalized, <b>false</b>
    *              if the node has not been initialized.
    * @hidden
    */
   public void setIsInitalized(boolean value);
   
   public long getSortPriority();
   
   public void setSortPriority(long value);
   
   public void setExpanded(boolean value);

   public void setSelected(boolean value);
   
   public void vcsFeatureExecuted(/* SCMFeatureKind */ int kind);
}
