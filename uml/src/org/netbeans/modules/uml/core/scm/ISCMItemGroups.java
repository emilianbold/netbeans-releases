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

/*
 * ISCMItemGroups.java
 *
 * Created on July 19, 2004, 8:23 AM
 */

package org.netbeans.modules.uml.core.scm;

import org.netbeans.modules.uml.core.support.umlutils.ETList;

/**
 *
 * @author  Trey Spiva
 */
public interface ISCMItemGroups extends ETList < ISCMItemGroup >
{
   /** Adds the Item to a group that matches the passed in Project. */
   public void addItem(ISCMItem pItem );

   /**
    * Retrieves the total count of all the ISCMItem objects encapsulated by
    * all the groups this collection holds.
    */
   public long getNumItems();

   /** Retrieves all the ISCMItems in this groups collection.*/
   public ETList < ISCMItem > getSCMItems();

   /** Calls Prepare() on all internal ISCMItemGroup.*/
   public boolean prepare(ISCMIntegrator integrator);

   /** Makes sure that all ISCMItems are in the appropriate groups.*/
   public void validate();
}
