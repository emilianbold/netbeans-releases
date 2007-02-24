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
 * TextCellEditor.java
 *
 * Created on April 5, 2005, 11:03 AM
 */

package org.netbeans.modules.uml.propertysupport.customizers;

import org.netbeans.modules.uml.core.support.umlutils.IPropertyElement;
import java.util.EventObject;
import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;

/**
 *
 * @author Administrator
 */
public class TextCellEditor extends DefaultCellEditor
{
   private IPropertyElement mElement = null;

   /** Creates a new instance of TextCellEditor */
   public TextCellEditor()
   {
      this(new JTextField());
   }
   
   public TextCellEditor(JTextField field)
   {
      super(field);
      
      field.setBorder(null);
      field.setEditable(true);
      delegate = new PropertyElementTextDelegate(delegate);
   }
   
   class PropertyElementTextDelegate extends DefaultCellEditor.EditorDelegate
   {
      private DefaultCellEditor.EditorDelegate mEditorDelegate = null;
      private boolean mInitializing = false;
      
      public PropertyElementTextDelegate(DefaultCellEditor.EditorDelegate delegate)
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
         }
         mInitializing = true;
         mEditorDelegate.setValue(transValue);
      }
   }
}
