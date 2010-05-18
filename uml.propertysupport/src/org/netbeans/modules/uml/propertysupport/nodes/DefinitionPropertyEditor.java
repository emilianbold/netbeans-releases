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

import java.beans.PropertyEditorSupport;

import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElementManager;
import org.netbeans.modules.uml.propertysupport.DefinitionPropertyBuilder;
import java.util.StringTokenizer;
import java.awt.EventQueue;

/**
 *
 * @author Trey Spiva
 */
public class DefinitionPropertyEditor extends PropertyEditorSupport // implements PropertyEditor
{
   private IPropertyDefinition mDefinition = null;
   private IPropertyElement mElement = null;
   private DefinitionPropertyBuilder.ValidValues mValidValues = null;
   private String mInProcessValue = null;
   
//   ConfigStringTranslator mTranslator = new ConfigStringTranslator();

   /**
    * 
    */
   public DefinitionPropertyEditor(IPropertyDefinition def, IPropertyElement element)
   {
      super();
      setDefinition(def);
      setElement(element);
   }

   /* (non-Javadoc)
    * @see java.beans.PropertyEditor#setValue(java.lang.Object)
    */
   public void setValue(Object value)
   {
      if (value instanceof String && !value.equals(getElement().getTranslatedValue()))
      {
         getElement().setValue((String)value);
      }

   }

   /* (non-Javadoc)
    * @see java.beans.PropertyEditor#getValue()
    */
   public Object getValue()
   {
      return getElement().getValue();
   }

   /* (non-Javadoc)
    * @see java.beans.PropertyEditor#getAsText()
    */
   public String getAsText()
   {
      String retVal = "";

      IPropertyElement element = getElement();
      if ((element != null))
      {
         
         String value = mInProcessValue;
         if(value == null)
         {
             value = element.getValue();
         }
         
         //String enumValue = translateEnumerationValue(value);
//         if(enumValue == null)
         {
             if(mValidValues != null)
             {
                retVal = mValidValues.translateValue(value);            
             }
             else
             {
                retVal = element.getTranslatedValue();
             }
         }
//         else
//         {
//             retVal = enumValue;
//         }
      }

      return translateFullyQualifiedName(retVal);
   }

   /* (non-Javadoc)
    * @see java.beans.PropertyEditor#setAsText(java.lang.String)
    */
   public void setAsText(String text) throws IllegalArgumentException
   {
      if(text.length() > 0)
      {
         String transText = translateToFullyQualifiedName(text);
         final IPropertyElement element = getElement();
         if ((element != null) && (transText != null))
         {
            String value = transText;
            if(mValidValues != null)
            {
                value = mValidValues.translateValueBackToPSK(transText);
//               value = mValidValues.translateValueBack(transText);               
//               value = translateEnumBack(value);
                if (value == null) value = transText;
            }            
           
            if(value.equals(element.getValue()) == false)
            {
                element.setValue(value);
                mInProcessValue = transText;

                // fix of CR 6300911; it is necessary to postpone the save task since it can display
                // some other UI and revert the changed value back.
                Runnable runnable = new Runnable() {
                    public void run() {
                        element.setModified(true);            
                        PreferenceHelper.saveModifiedPreferences(element);

                        IPropertyElementManager manager = element.getPropertyElementManager();
                        manager.interpretElementValue(element);
                        mInProcessValue = null;
                        firePropertyChange();
                    }
                };
                EventQueue.invokeLater(runnable);
            }
         }
      }
   }

   protected String translateEnumerationValue(String value)
   {
       String retVal = null;
       
       if((value != null) && (value.length() > 0))
       {
           char firstChar = value.charAt(0);
           if(Character.isDigit(firstChar) == true)
           {
               IPropertyDefinition def = getDefinition();
               String enumValues = def.getEnumValues();
               if(enumValues != null)
               {
                   StringTokenizer tokenizer = new StringTokenizer(enumValues, "|");
                   int tokens = tokenizer.countTokens();
                   for(int index = 0; index < tokens; index++)
                   {
                       String curToken = tokenizer.nextToken();
                       if(curToken.equals(value) == true)
                       {
                           retVal = Integer.toString(index);
                           break;
                       }
                   }

                   // If we have an enumeration this should never be null;
                   if((mValidValues != null) && (retVal != null))
                   {
                       retVal = mValidValues.translateValue(retVal);
                   }
               }
           }
       }
       return retVal;
   }
//   
//   protected String translateEnumBack(String value)
//   {
//       String retVal = value;
//       
//       if(mValidValues != null)
//       {
//           retVal = mValidValues.translateValueBack(value);
//           
//           IPropertyDefinition def = getDefinition();
//           String[] enumValues = def.getEnumValueList();
//           if(enumValues != null)
//           {     
//               try
//               {
//                   int index = Integer.parseInt(retVal);
//                   if(index < enumValues.length)
//                   {
//                       retVal = enumValues[index];
//                   }
//               }
//               catch(NumberFormatException e)
//               {
//                    ErrorManager.getDefault().notify(e);
//               }
////               StringTokenizer tokenizer = new StringTokenizer(enumValues, "|");
////               int tokens = tokenizer.countTokens();
////               for(int index = 0; index < tokens; index++)
////               {
////                   String curToken = tokenizer.nextToken();
////                   if(curToken.equals(retVal) == true)
////                   {
////                       retVal = Integer.toString(index);
////                       break;
////                   }
////               }
//           }
//       }
//       
//       return retVal;
//   }
   
   /* (non-Javadoc)
    * @see java.beans.PropertyEditor#getTags()
    */
   public String[] getTags()
   {
      String[] retVal = null;
      
      if(mDefinition.getControlType().equals("custom") == false)
      {
          mValidValues = DefinitionPropertyBuilder.instance().retrieveValidValues(mDefinition, mElement);    

          if(mValidValues != null)
          {
             retVal = mValidValues.getValidValues();

             for(int index = 0; index < retVal.length; index++)
             {
                 retVal[index] = translateFullyQualifiedName(retVal[index]);
             }
          }
      }
      
      return retVal;
   }

   //**************************************************
   // Data Access Methods
   //**************************************************

   /**
    * @return
    */
   public IPropertyElement getElement()
   {
      return mElement;
   }

   /**
    * @param element
    */
   public void setElement(IPropertyElement element)
   {
      mElement = element;
   }

   /**
    * @return
    */
   public IPropertyDefinition getDefinition()
   {
      return mDefinition;
   }

   /**
    * @param definition
    */
   public void setDefinition(IPropertyDefinition definition)
   {
      mDefinition = definition;
   }

   //**************************************************
   // Helper Methods
   //**************************************************

   protected String translateFullyQualifiedName(String fullQName)
   {
       String retVal = fullQName;
       
       if((fullQName != null) && (fullQName.indexOf("::") > 0))
       {
           int index = fullQName.lastIndexOf("::");
           StringBuffer name = new StringBuffer(fullQName.substring(index + 2));
           name.append(" : ");
           name.append(fullQName.substring(0, index));
           retVal = name.toString();
       }
       
       return retVal;
   }
   
   protected String translateToFullyQualifiedName(String value)
   {
       String retVal = value;
       
       if((value != null) && (value.indexOf(" : ") > 0))
       {
           int index = value.indexOf(" : ");
           String shortName = value.substring(0, index);
           
           StringBuffer fullQName = new StringBuffer();
           fullQName.append(value.substring(index + 3));
           fullQName.append("::");
           fullQName.append(shortName);
           retVal = fullQName.toString();
       }
       
       return retVal;
   }
}
