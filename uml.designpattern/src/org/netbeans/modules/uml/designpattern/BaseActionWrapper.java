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
 * BaseActionWrapper.java
 *
 * Created on April 16, 2005, 7:01 AM
 */

package org.netbeans.modules.uml.designpattern;

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
//      putValue(Action.SMALL_ICON, action.getValue(Action.SMALL_ICON));
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
   
//   public boolean isChecked()
//   {
//      
//      boolean retValue;
//      
//      retValue = super.isChecked();
//      return retValue;
//   }
   
//   public String getToolTipText()
//   {
//      
//      String retValue;
//      
//      retValue = super.getToolTipText();
//      return (String)mWrappedAction.getValue(Action.SHORT_DESCRIPTION);
//   }
   
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
   
//   public javax.swing.JComponent getCustomComponent()
//   {
//      
//      javax.swing.JComponent retValue;
//      
//      retValue = super.getCustomComponent();
//      return retValue;
//   }
//   
//   public int getStyle()
//   {
//      
//      int retValue;
//      
//      retValue = super.getStyle();
//      return retValue;
//   }
//   
//   public String getText()
//   {
//      return (String)getValue(Action.NAME);
//   }
//   
//   public String getLabel()
//   {
//      String retValue;
//      
//      retValue = super.getLabel();
//      return retValue;
//   }
   
   public String getId()
   {
      String retValue = "BaseActionWrapper";
      
      Class clazz = mWrappedAction.getClass();
      retValue = clazz.getName();
      
      return retValue;
   }
   
}
