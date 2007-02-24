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
 * EnumerationChangeFacility.java
 *
 * Created on April 8, 2005, 3:00 PM
 */

package org.netbeans.modules.uml.core.roundtripframework;

import org.netbeans.modules.uml.core.metamodel.core.constructs.IEnumeration;

/**
 *
 * @author Administrator
 */
public class EnumerationChangeFacility extends RequestFacility
                                       implements IEnumerationChangeFacility
{

   /** Creates a new instance of EnumerationChangeFacility */
   public EnumerationChangeFacility()
   {
   }

   public void changeName(IEnumeration pClassifier, String pName)
   {
      if(pClassifier != null)
      {
         pClassifier.setName(pName);
      }
   }
   
   public void nameChanged(IEnumeration pClassifier)
   {
      // Nothing to do.
   }

}
