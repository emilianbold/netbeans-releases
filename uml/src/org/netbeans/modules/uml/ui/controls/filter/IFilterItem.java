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

/**
 *
 * @author Trey Spiva
 */
public interface IFilterItem
{
   /** The item will be not be filtered out. */
   public static final int FILTER_STATE_ON  = 1;

   /** The item will be filtered out. */
   public static final int FILTER_STATE_OFF = 2;

   /**
    * The name of the filter item.  The name of the filter item will be
    * the name displayed in the filter dialog.
    *
    * @return The name of the item.
    */
   public String getName();

   /**
    * The state of the filter item.  The state will be either 
    * <code>FILTER_STATE_ON</code> or <code>FILTER_STATE_OFF</code>.
    * 
    * @return The state of the item.
    */
   public int getState();
   
   /**
    * Sets the filter state of the item.  
    * 
    * @param value Either<code>FILTER_STATE_ON</code> or 
    *              <code>FILTER_STATE_OFF</code>.
    */
   public void setState(int value);  
   
   /**
    * Sets the filter state of the item.  If the user modified the filter item
    * via the filter dialog the dialog parameter will give access to the tree 
    * settings.
    * 
    * @param value Either<code>FILTER_STATE_ON</code> or 
    *              <code>FILTER_STATE_OFF</code>.
    * @param dialog The dialog that changed the item.  This can be 
    *               <code>null</code> if the a filter dialog was not used to
    *               change the item.  
    */
   public void setState(int value, IFilterDialog dialog);   
   
   /**
    * Retrieve the icon associated with the item.  There no icon has been 
    * associated the icon the name of the item and CommonResources is used to 
    * try to retrieve the icon for the item.   
    * 
    * @return The icon associated with the item or <code>null</code> if an icon
    *         is not associated with the item.
    */
   public Icon getIcon();
}
