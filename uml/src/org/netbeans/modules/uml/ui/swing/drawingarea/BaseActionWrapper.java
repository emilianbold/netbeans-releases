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
 * BaseActionWrapper.java
 *
 * Created on April 16, 2005, 7:01 AM
 */

package org.netbeans.modules.uml.ui.swing.drawingarea;

import org.netbeans.modules.uml.ui.products.ad.application.action.BaseAction;
import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.Icon;

/**
 *
 * @author Administrator
 */
public class BaseActionWrapper extends BaseAction
{
   Action mWrappedAction = null;

   /** Creates a new instance of BaseActionWrapper */
   public BaseActionWrapper(Action action)
   {
      mWrappedAction = action;
      putValue(Action.NAME, removeAMPS((String)action.getValue(Action.NAME)));
      putValue(Action.ACCELERATOR_KEY , action.getValue(Action.ACCELERATOR_KEY ));
      putValue(Action.ACTION_COMMAND_KEY , action.getValue(Action.ACTION_COMMAND_KEY ));
      putValue(Action.LONG_DESCRIPTION , action.getValue(Action.LONG_DESCRIPTION ));
      putValue(Action.MNEMONIC_KEY , action.getValue(Action.MNEMONIC_KEY ));
      putValue(Action.SHORT_DESCRIPTION , action.getValue(Action.SHORT_DESCRIPTION ));
      //putValue(Action.SMALL_ICON, action.getValue(Action.SMALL_ICON));
   }

   protected String removeAMPS(String value)
   {
      String retVal = value;
      int index = value.indexOf('&');
      if(index > -1)
      {
         retVal = value.substring(0, index); 
         retVal += value.substring(index+1);
      }
      
      return retVal;
   }
   
   public void actionPerformed(ActionEvent e)
   {
      mWrappedAction.actionPerformed(e);
   }
   
   public boolean isChecked()
   {
      
      boolean retValue;
      
      retValue = super.isChecked();
      return retValue;
   }
   
   public String getToolTipText()
   {
      
      String retValue;
      
      retValue = super.getToolTipText();
      return (String)mWrappedAction.getValue(Action.SHORT_DESCRIPTION);
   }
   
   public javax.swing.Icon getSmallImage()
   {
      
      Icon retVal = null;
      
//      Object obj = getValue(Action.SMALL_ICON);
//      if (obj instanceof Icon)
//      {
//         retVal = (Icon)obj;
//         
//      }
      return retVal;
   }
   
   public javax.swing.JComponent getCustomComponent()
   {
      
      javax.swing.JComponent retValue;
      
      retValue = super.getCustomComponent();
      return retValue;
   }
   
   public int getStyle()
   {
      
      int retValue;
      
      retValue = super.getStyle();
      return retValue;
   }
   
   public String getText()
   {
      return (String)getValue(Action.NAME);
   }
   
   public String getLabel()
   {
      String retValue;
      
      retValue = super.getLabel();
      return retValue;
   }
   
   public String getId()
   {
      String retValue = "BaseActionWrapper";
      
      Class clazz = mWrappedAction.getClass();
      retValue = clazz.getName();
      
      return retValue;
   }
   
}
