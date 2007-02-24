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



package org.netbeans.modules.uml.ui.support.messaging;

import java.awt.Frame;
import java.util.Vector;

import org.netbeans.modules.uml.common.ETSystem;
import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProxyUserInterface;

/**
 * Implements a progress dialog showing messages in a tree format and a progress indicator.  It
 * wraps an dialog called ProgressDialogImp.
 *
 *  \image html ProgressDialog.jpg
 */

public class ProgressDialog extends ProgressDialogImp implements IProgressDialog
{

   private ProgressDialogImp m_Dialog = null;
   private IProgressDialog m_Proxy = null;
   private int m_RevokeNum = 0;

   private Vector listeners = new Vector();

   public ProgressDialog(Frame frame, String title, boolean modal)
   {
      super(frame, title, modal);
   }

   public ProgressDialog()
   {
      super();
   }

   /**
    * Tells any connection points that the dialog has been canceled.
    */
   public void onCancel()
   {

      ETSystem.out.println("I am here in Cancelled of Progress dialog");

      for (int i = 0; i < listeners.size(); i++)
      {
         IProgressDialogListener listener = (IProgressDialogListener) listeners.get(i);
         listener.onCancelled();
      }

      super.onCancel();
     // Make sure the app isn't locked up.
      IProxyUserInterface ui = ProductHelper.getProxyUserInterface();
      if (ui != null && ui.getWindowHandle() != null)
      {
         ui.getWindowHandle().setEnabled(true);
         ui.getWindowHandle().setVisible(true);
      }
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.ProgressDialogImp#clearFields()
    */
   public long clearFields()
   {
      return super.clearFields();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.ProgressDialogImp#close()
    */
   public long close()
   {
      return super.close();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.ProgressDialogImp#display(int)
    */
   public boolean display(int mode)
   {
      return super.display(mode);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.ProgressDialogImp#getCloseWhenDone()
    */
   public boolean getCloseWhenDone()
   {
      return super.getCloseWhenDone();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.ProgressDialogImp#getCollapse()
    */
   public boolean getCollapse()
   {
      return super.getCollapse();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.ProgressDialogImp#getDefaultExtension()
    */
   public String getDefaultExtension()
   {
      return super.getDefaultExtension();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.ProgressDialogImp#getFieldOne()
    */
   public String getFieldOne()
   {
      return super.getFieldOne();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.ProgressDialogImp#getFieldThree()
    */
   public String getFieldThree()
   {
      return super.getFieldThree();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.ProgressDialogImp#getFieldTwo()
    */
   public String getFieldTwo()
   {
      return super.getFieldTwo();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.ProgressDialogImp#getGroupingTitle()
    */
   public String getGroupingTitle()
   {
      return super.getGroupingTitle();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.ProgressDialogImp#getIncrementAmount()
    */
   public int getIncrementAmount()
   {
      return super.getIncrementAmount();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.ProgressDialogImp#getIsCancelled()
    */
   public boolean getIsCancelled()
   {
      return super.getIsCancelled();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.ProgressDialogImp#getLimits(int, int)
    */
   public ETPairT < Integer, Integer > getLimits()
   {
      return super.getLimits();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.ProgressDialogImp#getLogFileName()
    */
   public String getLogFileName()
   {
      return super.getLogFileName();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.ProgressDialogImp#getPosition()
    */
   public int getPosition()
   {
      return super.getPosition();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.ProgressDialogImp#getProgressExecutor()
    */
   public IProgressExecutor getProgressExecutor()
   {
      return super.getProgressExecutor();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.ProgressDialogImp#increment()
    */
   public int increment(int value)
   {
      return super.increment(value);
   }

   public int increment()
   {
      return super.increment();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.ProgressDialogImp#lockMessageCenterUpdate()
    */
   public void lockMessageCenterUpdate()
   {
      super.lockMessageCenterUpdate();
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.ProgressDialogImp#log(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
    */
   public void log(int type, String group, String first, String second, String third)
   {
      super.log(type, group, first, second, third);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.ProgressDialogImp#promptForClosure(java.lang.String, boolean)
    */
   public void promptForClosure(String buttonTitle, boolean beep)
   {
      super.promptForClosure(buttonTitle, beep);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.ProgressDialogImp#setCloseWhenDone(boolean)
    */
   public void setCloseWhenDone(boolean value)
   {
      super.setCloseWhenDone(value);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.ProgressDialogImp#setCollapse(boolean)
    */
   public void setCollapse(boolean value)
   {
      super.setCollapse(value);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.ProgressDialogImp#setDefaultExtension(java.lang.String)
    */
   public void setDefaultExtension(String value)
   {
      super.setDefaultExtension(value);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.ProgressDialogImp#setFieldOne(java.lang.String, int)
    */
   public void setFieldOne(String newVal, int type)
   {
      super.setFieldOne(newVal, type);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.ProgressDialogImp#setFieldOne(java.lang.String)
    */
   public void setFieldOne(String value)
   {
      super.setFieldOne(value);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.ProgressDialogImp#setFieldThree(java.lang.String, int)
    */
   public void setFieldThree(String newVal, int type)
   {
      super.setFieldThree(newVal, type);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.ProgressDialogImp#setFieldThree(java.lang.String)
    */
   public void setFieldThree(String value)
   {
      super.setFieldThree(value);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.ProgressDialogImp#setFieldTwo(java.lang.String, int)
    */
   public void setFieldTwo(String newVal, int type)
   {
      super.setFieldTwo(newVal, type);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.ProgressDialogImp#setFieldTwo(java.lang.String)
    */
   public void setFieldTwo(String value)
   {
      super.setFieldTwo(value);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.ProgressDialogImp#setGroupingTitle(java.lang.String, int)
    */
   public void setGroupingTitle(String newVal, int type)
   {
      super.setGroupingTitle(newVal, type);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.ProgressDialogImp#setGroupingTitle(java.lang.String)
    */
   public void setGroupingTitle(String value)
   {
      super.setGroupingTitle(value);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.ProgressDialogImp#setIncrementAmount(int)
    */
   public void setIncrementAmount(int value)
   {
      super.setIncrementAmount(value);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.ProgressDialogImp#setLimits(int, int)
    */
   public void setLimits(ETPairT < Integer, Integer > pLimits)
   {
      super.setLimits(pLimits);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.ProgressDialogImp#setLogFileName(java.lang.String)
    */
   public void setLogFileName(String value)
   {
      super.setLogFileName(value);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.ProgressDialogImp#setPosition(int)
    */
   public void setPosition(int value)
   {
      super.setPosition(value);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.ProgressDialogImp#setProgressExecutor(org.netbeans.modules.uml.ui.support.messaging.IProgressExecutor)
    */
   public void setProgressExecutor(IProgressExecutor value)
   {
      super.setProgressExecutor(value);
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.ProgressDialogImp#unlockMessageCenterUpdate()
    */
   public void unlockMessageCenterUpdate()
   {
      super.unlockMessageCenterUpdate();
   }

   public void setIndeterminate(boolean newVal)
   {
      super.setIndeterminate(newVal);
   }

   public IProgressController getProgressController()
   {
      return super.getProgressController();
   }

   public void setProgressController(IProgressController pController)
   {
      super.setProgressController(pController);
   }

   public void addListener(IProgressDialogListener listener)
   {
      listeners.add(listener);
   }

   public void removeListener(IProgressDialogListener listener)
   {
      listeners.remove(listener);
   }
}
