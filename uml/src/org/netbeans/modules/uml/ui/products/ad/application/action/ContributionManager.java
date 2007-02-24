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


package org.netbeans.modules.uml.ui.products.ad.application.action;

import org.netbeans.modules.uml.common.ETSystem;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public abstract class ContributionManager implements IContributionManager
{
   /**
    * The list of contribution items.
    */
   private List m_Contributions = new ArrayList();

   /**
    * Indicates whether the widgets are in sync with the contributions.
    */
   private boolean isDirty = true;

   /**
    * Number of dynamic contribution items.
    */
   private int dynamicItems = 0;

   /**
    * Creates a new contribution manager.
    */
   protected ContributionManager()
   {
   }

   /**
    * Returns whether this contribution manager contains dynamic items. 
    * A dynamic contribution item contributes items conditionally, 
    * dependent on some internal state.
    *
    * @return <code>true</code> if this manager contains dynamic items, and
    *  <code>false</code> otherwise
    */
   protected boolean hasDynamicItems()
   {
      return (dynamicItems > 0);
   }

   /* (non-Javadoc)
    * Method declared on IContributionManager.
    */
   public boolean isEmpty()
   {
      return m_Contributions.isEmpty();
   }

   /**
    * Sets whether this manager is dirty. When dirty, the list of contributions 
    * is not accurately reflected in the corresponding widgets.
    *
    * @param <code>true</code> if this manager is dirty, and <code>false</code>
    *   if it is up-to-date
    */
   protected void setDirty(boolean d)
   {
      isDirty = d;
   }
}