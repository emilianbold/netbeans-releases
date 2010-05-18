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
 * Created on Jun 12, 2003
 * @author Trey Spiva
 */
package org.netbeans.modules.uml.ui.support.projecttreesupport;

import org.netbeans.modules.uml.ui.controls.projecttree.ProjectTreeNodeFactory;
import java.util.ArrayList;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.core.workspacemanagement.IWorkspace;

/**
 *
 * @author Trey Spiva
 */
public interface IProjectTreeBuilder
{
    /**
     * Based on the passed in IDispatch, return a list of child elements 
     * that should appear as child nodes
     */ 
   public ITreeItem[] retrieveChildItems(Object pDisp);
   
   /** 
    * Based on the passed in IElement, return a list of child elements
    * that should appear as child nodes"
    */ 
   public ITreeItem[] retrieveChildItemsForElement(Object pDisp, IElement pEle);
   
   /** 
    * Based on the passed in ITreeFolder, return a list of child elements
    * that should appear as child nodes
    */  
   public ITreeItem[] retrieveChildItemsForFolder(ITreeFolder pFolder);
   
   
   /**
    * Get the a parents child items that are to be displayed.
    * 
    * @param parent The parent tree item.
    * @return The tree items that are to be displayed.
    */ 
   public ITreeItem[] getItems(Object parent);
   
   /**
    * Get the names of a parents child folders that are to be displayed.
    * 
    * @param parent The parent tree item.
    * @return The folder names to be displayed.
    */
   public String[] getFolders(Object parent);
   
   /**
    * Based on the passed in IWorkspace, return a list of child elements
    * that should appear as child nodes
    */ 
   public ITreeItem[] retrieveChildItemsForWorkspace(IWorkspace pWork);
   
   /**
    * Based on the passed in Object, create a child ITreeItem and 
    * place it under the passed in parent tree item.
    * 
    * @param pParent The owner of the new tree item.
    * @param pDisp The tree item data.
    * @return The new tree item.
    */ 
   public ITreeItem createChild(ITreeItem pParent, Object pDisp);
   
   /**
    * Based on a sort defined in the xml file, sort the passed in tree items.
    * 
    * @param pParent The parent that owns children are to be sorted.
    * @param pIn The tree items before the sort occurs.
    * @return The tree items after the sort occurs.
    */ 
   public ITreeItem[] sort(Object pParent, ITreeItem[] pIn);
   
   /**
    * Based on the passed in Object, return a list of child elements that
    * should appear as child nodes, sorted per our rules
    * 
    * @param pParent The parent that owns sorted children.
    * @return The sorted children.
    */ 
   public ITreeItem[] retrieveChildItemsSorted(Object pParent);
   
   /**
    * Returns the sort priority for the argument string 
    * (ie Actor or ActivityDiagram).
    * 
    * @param sType
    * @return
    */ 
   public long getSortPriority(String sType);
   
   /**
    * Manages what elements are filtered so we don't do extra processing.  
    * The Object is a IProjectTreeBuilderFilter.
    */ 
   public IProjectTreeBuilderFilter getProjectTreeBuilderFilter();
   
   /**
    * Manages what elements are filtered so we don't do extra processing.
    * The IDispatch is a IProjectTreeBuilderFilter.
    * 
    * @param pFilter
    */ 
   public void setProjectTreeBuilderFilter(IProjectTreeBuilderFilter pFilter);

   /**
    * Retrieves the model elements that are affected by the changed element.
    * 
    * @param changedItem The element that changed.
    * @param strs This is a an array of "|" delimited strings that state
    *             where in the xml definition file the element can live
    * @param items The model elements that are affected.
    */
   public void getInfoForRefresh(IElement             changedItem, 
                                 ETList < String >    strs, 
                                 ETList < ITreeItem > items);
   
   /**
    * Sets the node factory to use when creating project tree items
    * 
    * @param factory The new factory.
    */
   public void setNodeFactory(ProjectTreeNodeFactory factory);
   
   /**
    * Method that eliminates certain items from the list of owned elements 
    * of a project.  The "Excludes" section in the ProjectTreeDefinitions.etc 
    * configuration file determines which elements are excluded.
    * 
    * @param testElement The element in question
    * @return         Whether or not it should be included
    */
   public boolean isExcluded(IElement testElement);
}
