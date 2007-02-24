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



package org.netbeans.modules.uml.ui.support.applicationmanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 *
 * @author Trey Spiva
 */
public class DiagramToolBarDefs
{
   HashMap m_ToolbarDetails = new HashMap();

   /**
    * @param details
    */
   public void addToolBarDetails(String name, DiagramToolDetails details)
   {
      m_ToolbarDetails.put(name, details);
   }

   public DiagramToolDetails getToolBarDetails(String name)
   {
      return (DiagramToolDetails)m_ToolbarDetails.get(name);
   }
   
   public Set getToolbarNames()
   {
      return m_ToolbarDetails.keySet();
   }

   public ArrayList getRequiredToolbars()
   {
      ArrayList retVal = new ArrayList();
      
      Collection values = m_ToolbarDetails.values();
      
      for (Iterator iter = values.iterator(); iter.hasNext();)
      {
         DiagramToolDetails curDetails = (DiagramToolDetails)iter.next();
         if(curDetails.isRequired() == true)
         {
            retVal.add(curDetails);
         }
      }
      
      return retVal;
   }
   
   public ArrayList getOptionalToolbars()
   {
      ArrayList retVal = new ArrayList();
      
      Collection values = m_ToolbarDetails.values();
      
      for (Iterator iter = values.iterator(); iter.hasNext();)
      {
         DiagramToolDetails curDetails = (DiagramToolDetails)iter.next();
         if(curDetails.isRequired() == false)
         {
            retVal.add(curDetails);
         }
      }
      
      return retVal;
   }
}
