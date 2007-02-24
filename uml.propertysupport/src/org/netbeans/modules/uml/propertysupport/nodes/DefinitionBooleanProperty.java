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

package org.netbeans.modules.uml.propertysupport.nodes;


import java.lang.reflect.InvocationTargetException;

import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;

/**
 *
 * @author Trey Spiva
 */
public class DefinitionBooleanProperty extends DefinitionPropertySupport
{

      /**
    * @param def
    * @param element
    * @param writable
    */
   public DefinitionBooleanProperty(IPropertyDefinition def,
                                 IPropertyElement element)
   {
      this(def, element, true, true);
   }
   

   /**
    * @param def
    * @param element
    * @param writable
    */
   public DefinitionBooleanProperty(IPropertyDefinition def, 
                                 IPropertyElement element, 
                                 boolean writable, 
                                 boolean autoCommit)
   {
      super(def, element, Boolean.class, writable, autoCommit);
   }
   
   public Object getValue() throws IllegalAccessException, InvocationTargetException
   {
      Object retVal = Boolean.FALSE;
      
      IPropertyElement element = getElement();
      if((element != null))
      {
         String value = element.getValue();
         if(value != null)
         {
            if((value.equals("1") == true) || 
               (value.equals("PSK_TRUE") == true))
            {
               retVal = Boolean.TRUE; 
            }
         }
      }
      
      return retVal;
   }
   //**************************************************
   // Helper Methods
   //**************************************************
   
   /**
    * Translates the value displayed to the user back into the value that
    * needs to be stored to the database.
    * 
    * @param value The object being store to the propetery.
    * @return The string represenation of the value.
    */
   protected String translateValueBack(Object value)
   {
      String retVal = "PSK_FALSE";
      
      if (value instanceof Boolean)
      {         
         Boolean boolValue = (Boolean)value;
         if(boolValue.booleanValue() == true)
         {
            retVal = "PSK_TRUE";                     
         }
      }
      
      return retVal;
   }
}
