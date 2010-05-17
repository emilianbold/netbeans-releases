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
