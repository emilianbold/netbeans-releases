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
