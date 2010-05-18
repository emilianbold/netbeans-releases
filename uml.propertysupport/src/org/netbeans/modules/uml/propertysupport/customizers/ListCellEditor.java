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

/*
 * ListCellEditor.java
 *
 * Created on April 4, 2005, 10:31 AM
 */

package org.netbeans.modules.uml.propertysupport.customizers;

import org.netbeans.modules.uml.core.support.umlutils.IPropertyDefinition;
import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
import org.netbeans.modules.uml.propertysupport.DefinitionPropertyBuilder;
import org.netbeans.modules.uml.propertysupport.DefinitionPropertyBuilder.ValidValues;
import java.util.EventObject;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import java.util.Vector;

/**
 * This class provides a little more stability when ueing a JComboBox in a 
 * JTable.  I do not quite understand the problem.  I just know that is class
 * fixed the problem.
 *
 * @author Administrator
 */
public class ListCellEditor extends DefaultCellEditor
{
   private IPropertyElement mElement = null;
   private boolean mIsInitialized = false;
   
   /** Creates a new instance of ListCellEditor */
   public ListCellEditor()
   {
      this(new JComboBox());
   }
   
   public ListCellEditor(JComboBox box)
   {
      super(box);
      
      box.setBorder(null);
      box.setEditable(true);
      delegate = new PropertyElementListDelegate(delegate);
   }

   class PropertyElementListDelegate extends DefaultCellEditor.EditorDelegate
   {
      private DefaultCellEditor.EditorDelegate mEditorDelegate = null;
      private boolean mInitializing = false;
      
      public PropertyElementListDelegate(DefaultCellEditor.EditorDelegate delegate)
      {
         mEditorDelegate = delegate;
      }
      
      ///////////////////////////////////////////////////////////////
      // Acting as a delegate only to the methods that the
      // DefaultCellEditor overrides for JComboBoxes.
      
       public Object getCellEditorValue()
       {
          Object retVal = mEditorDelegate.getCellEditorValue();
          return retVal;
       }
       
      public boolean stopCellEditing()
      {
         if((mElement != null) && (mInitializing == false))
         {
            String value = (String)getCellEditorValue();
            
            
            mElement.setValue(PropertyDataFormatter.translateToFullyQualifiedName(value));
            mElement = null;
         }
         
         mInitializing = false;
         return mEditorDelegate.stopCellEditing();
      }
      
      public boolean shouldSelectCell(EventObject e)
      {
         return true;
      }
      
      public void setValue(Object value)
      {
         Object transValue = value;
         if(value instanceof IPropertyElement)
         {
            mElement = (IPropertyElement)value;
            transValue = mElement.getValue();
            initializeList();
//            mEditorDelegate.setValue(translateFullyQualifiedName((String)transValue));
         }
         mInitializing = true;
         
         if(transValue instanceof String)
         {
             transValue = PropertyDataFormatter.translateFullyQualifiedName((String)transValue);
             mEditorDelegate.setValue(transValue);
         }
         else
         {
             mEditorDelegate.setValue(transValue);
         }
      }
      
      protected void initializeList()
      {
         if((mIsInitialized == false) && (mElement != null))
         {
            mInitializing = true;
            JComboBox box = (JComboBox)getComponent();
            
            // The valid values can not be retrieved until we have a property
            // element that represents the column.  To make rendering easier
            // the TableModel returns the property elements data instead of a
            // property element.  Therefore, we need to first retrieve the
            // property element from the model.
            if(mElement != null)
            {
               IPropertyDefinition def = mElement.getPropertyDefinition();
               
               DefinitionPropertyBuilder builder = DefinitionPropertyBuilder.instance();
               ValidValues values = builder.retrieveValidValues(def, mElement);
               if(values != null)
               {
                  String value = mElement.getValue();
                  Vector < String > strVec = new Vector < String > ();
                  int size = box.getItemCount ();
                  for (int i = 0; i < size; i++) {
                      strVec.add ((String)box.getItemAt(i));
                  }
                  String[] transValues = values.getValidValues();
                  for(String curStr : transValues)
                  {
                      
                     if (!strVec.contains(curStr)){
                         String translated = PropertyDataFormatter.translateFullyQualifiedName(curStr);
                         strVec.add (translated);
                         box.addItem(translated);
                     }
                  }
                  
                  int index = strVec.indexOf(PropertyDataFormatter.translateFullyQualifiedName(value));
                  if(index > 0)
                  {
                      box.setSelectedIndex(index);
                      delegate.setValue(PropertyDataFormatter.translateFullyQualifiedName(value));
                  }
               }
            }
            mInitializing = false;
            mIsInitialized = true;
         }
      }
   }
   
}
