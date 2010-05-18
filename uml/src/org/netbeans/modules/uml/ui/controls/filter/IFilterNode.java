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



package org.netbeans.modules.uml.ui.controls.filter;

import javax.swing.Icon;

import org.netbeans.modules.uml.ui.swing.projecttree.JFilterDialog;

/**
 *
 * @author Trey Spiva
 */
public interface IFilterNode
{
   /**
    * Removes newChild from its parent and makes it a child of this node by
    * adding it to the end of this node's child array.
    *
    * @param newChild node to add as a child of this node
    * @throws IllegalArgumentException if newChild is null.
    */
   public void add(IFilterNode newChild) throws IllegalArgumentException;

   /**
    * @return
    */
   public String getDispalyName();

   /**
   * Specifies if the checkbox for the filter item is on or off.
   *   
   * @return <b>true</b> if the item is checked, <b>false</b> if the item is
   *         off.
   */
   public boolean isOn();

   /**
    * Retrieves the icon that represent the filter item.
    * 
    * @return The icon to display.
    */
   public Icon getIcon();

   /**
    * Saves the nodes contents to the associated IFilterItem.  The children
    * of the node is also saved.
    * 
    * @param dialog The dialog that set the state.
    */
   public void save(IFilterDialog dialog);

   /**
    * Sets the state of the filter item.
    *   
    * @param value <b>true</b> if the item is checked, <b>false</b> if the 
    *              item is off.
    */
   public void setState(int state);

   /**
    * Retrieve the number of children that the node has.
    * 
    * @return The number of children.
    */
   public int getChildCount();
}
