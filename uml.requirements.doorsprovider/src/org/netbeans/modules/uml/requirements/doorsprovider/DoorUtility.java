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
 * DoorUtility.java
 *
 * Created on July 2, 2004, 11:10 AM
 */

package org.netbeans.modules.uml.requirements.doorsprovider;

import org.netbeans.modules.uml.core.requirementsframework.RequirementsException;
import org.netbeans.modules.uml.ui.support.helpers.ETSmartWaitCursor;

/**
 *
 * @author  Trey Spiva
 */
public class DoorUtility
{
   private static boolean m_IsInitialized = false;

   static
   {
      System.loadLibrary("DOORIntegration");
   }

   /** Creates a new instance of DoorUtility */
   public DoorUtility()
   {
   }
   
   public static String sendRequestToDoors(String str) throws RequirementsException
   {
      ETSmartWaitCursor cursor = new ETSmartWaitCursor();
      
      if(m_IsInitialized == false)
      {
         initialize();
         m_IsInitialized = true;
      }
      
      String retVal = sendRequestToDoorsViaOS(str);
      
      cursor.stop();
      return retVal;
   }
   
   public native static void initialize();
   
   public native static String sendRequestToDoorsViaOS(String str) throws RequirementsException;
}
