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
 *
 * Created on Jun 30, 2003
 * @author Trey Spiva
 */
package org.netbeans.modules.uml.ui.support;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;

import org.netbeans.modules.uml.ui.controls.filter.IFilterDialog;
import org.netbeans.modules.uml.ui.controls.projecttree.IProjectTreeModel;
import org.netbeans.modules.uml.ui.support.commondialogs.IErrorDialog;
import org.netbeans.modules.uml.ui.support.commondialogs.INavigationDialog;
import org.netbeans.modules.uml.ui.support.commondialogs.IPreferenceQuestionDialog;
import org.netbeans.modules.uml.ui.support.commondialogs.IQuestionDialog;
import org.netbeans.modules.uml.ui.swing.commondialogs.SwingErrorDialog;
import org.netbeans.modules.uml.ui.swing.commondialogs.SwingNavigationDialog;
import org.netbeans.modules.uml.ui.swing.commondialogs.SwingQuestionDialogImpl;
import org.netbeans.modules.uml.ui.swing.projecttree.JFilterDialog;


/**
 * The UIFactory is used to retrieve the implemenation of common dialogs.  Since
 * we must support both SWT and Swing the UIFactory is used to retrieve the
 * correct implementation for the platform that is running.
 * 
 * @author Trey Spiva
 */
public class UIFactory
{

   /**
    * Retrieves the question dialog implementation.
    * 
    * @return The platforms implementation of the IQuestionDialog interface. 
    */
   public static IQuestionDialog createQuestionDialog()
   {
      return new SwingQuestionDialogImpl();
   }

   /**
    * Retrieves the navigation dialog.
    * 
    * @return The platforms implementation of the INavigationDialog interface.
    */
   public static INavigationDialog createNavigationDialog()
   {
      return new SwingNavigationDialog();
   }   
   
   /**
    * Retrieves the question dialog implementation.
    * 
    * @return The platforms implementation of the IQuestionDialog interface. 
    */
   //public static IPreferenceQuestionDialog createPreferenceQuestionDialog(Component parent)
   public static IPreferenceQuestionDialog createPreferenceQuestionDialog()
   {
      return  new SwingPreferenceQuestionDialog();
   }
   
   /**
    * Retrieves the question dialog implementation.
    * 
    * @return The platforms implementation of the IQuestionDialog interface. 
    */
   //public static IPreferenceQuestionDialog createPreferenceQuestionDialog(Component parent)
   public static IErrorDialog createErrorDialog()
   {
      return  new SwingErrorDialog();
   }
   
   /**
    * Creates a new ProjectTreeFilterDialog.
    *   
    * @param parent The owner window.
    * @return The project trees IFilterDialog implementation.
    */
   public static IFilterDialog createProjectTreeFilterDialog(Component parent,
                                                             IProjectTreeModel model)
   {
      IFilterDialog retVal = null;
      
      Object frame = getParentFrame(parent);
      
      if (frame instanceof Frame)
      {
         Frame parentFrame = (Frame)frame;
         retVal = new JFilterDialog(parentFrame, model);
      }
      else if (frame instanceof Dialog)
      {
         Dialog parentDialog = (Dialog)frame;
         retVal = new JFilterDialog(parentDialog, model);
      }
      
      return  retVal;
   }
   
   public static Object getParentFrame(Component comp)
   {
      Object retVal = null;
      
      if (comp instanceof Frame)
      {
         retVal = comp;
      }
      else if (comp instanceof Dialog)
      {
         retVal = comp;
      }
      else
      {
         retVal = getParentFrame(comp.getParent());
      }
      
      return retVal;
   }
}
