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
