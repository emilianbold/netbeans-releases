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
          //System.out.println("getCellEditorValue() = " + retVal);
          return retVal;
       }
       
      public boolean stopCellEditing()
      {
         if((mElement != null) && (mInitializing == false))
         {
            String value = (String)getCellEditorValue();
            mElement.setValue(value);
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
         }
         mInitializing = true;
         mEditorDelegate.setValue(transValue);
      }
      
      protected void initializeList()
      {
         if((mIsInitialized == false) && (mElement != null))
         {
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
                  Vector < String > strVec = new Vector < String > ();
                  int size = box.getItemCount ();
                  for (int i = 0; i < size; i++) {
                      strVec.add ((String)box.getItemAt(i));
                  }
                  String[] transValues = values.getValidValues();
                  for(String curStr : transValues)
                  {
                      
                     if (!strVec.contains(curStr)){
                         strVec.add (curStr);
                         box.addItem(curStr);
                     }
                  }
               }
            }
            mIsInitialized = true;
         }
      }
   }
   
}
