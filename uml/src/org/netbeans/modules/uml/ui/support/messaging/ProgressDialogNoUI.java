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

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.support.Debug;

/**
 * @author brettb
 *
 * The progress dialog is used to allow code to call the progress dialog
 * without actually seeing a dialog box UI.
 */
public class ProgressDialogNoUI implements IProgressDialog
{
   public ProgressDialogNoUI()
   {
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.IProgressDialog#setLimits(int, int)
    */
   public void setLimits( ETPairT<Integer, Integer> limits )
   {
      m_iLower = limits.getParamOne().intValue();
      m_iUpper = limits.getParamTwo().intValue();
      Debug.assertTrue ( m_iLower <= m_iUpper );
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.IProgressDialog#getLimits(int, int)
    */
   public ETPairT<Integer, Integer> getLimits()
   {
      return new ETPairT<Integer, Integer>( new Integer( m_iLower ), new Integer( m_iUpper ));
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.IProgressDialog#increment(int)
    */
   public int increment(int iPrevPos)
   {
      iPrevPos = m_iPosition;
      return m_iPosition++;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.IProgressDialog#setPosition(int)
    */
   public void setPosition(int iPos)
   {
      m_iPosition = iPos;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.IProgressDialog#getPosition()
    */
   public int getPosition()
   {
      return m_iPosition;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.IProgressDialog#clearFields()
    */
   public long clearFields()
   {
      // do nothing
      return 0;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.IProgressDialog#setFieldOne(java.lang.String)
    */
   public void setFieldOne(String strValue)
   {
      // do nothing
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.IProgressDialog#setFieldTwo(java.lang.String)
    */
   public void setFieldTwo(String strValue)
   {
      // do nothing
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.IProgressDialog#setFieldThree(java.lang.String)
    */
   public void setFieldThree(String strValue)
   {
      // do nothing
   }

   private int m_iLower = 0;
   private int m_iUpper = 100;
   private int m_iPosition = 0;
   
   
   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.IProgressDialog#getTitle()
    */
   public String getTitle()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.IProgressDialog#setTitle(java.lang.String)
    */
   public void setTitle(String value)
   {
      // TODO Auto-generated method stub
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.IProgressDialog#setLimits()
    */
   public void setLimits()
   {
      // TODO Auto-generated method stub
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.IProgressDialog#getIncrementAmount()
    */
   public int getIncrementAmount()
   {
      // TODO Auto-generated method stub
      return 0;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.IProgressDialog#setIncrementAmount(int)
    */
   public void setIncrementAmount(int value)
   {
      // TODO Auto-generated method stub
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.IProgressDialog#getGroupingTitle()
    */
   public String getGroupingTitle()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.IProgressDialog#setGroupingTitle(java.lang.String)
    */
   public void setGroupingTitle(String value)
   {
      // TODO Auto-generated method stub
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.IProgressDialog#getFieldOne()
    */
   public String getFieldOne()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.IProgressDialog#getFieldTwo()
    */
   public String getFieldTwo()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.IProgressDialog#getFieldThree()
    */
   public String getFieldThree()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.IProgressDialog#increment()
    */
   public int increment()
   {
      // TODO Auto-generated method stub
      return 0;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.IProgressDialog#display(int)
    */
   public boolean display(int mode)
   {
      // TODO Auto-generated method stub
      return false;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.IProgressDialog#close()
    */
   public long close()
   {
      // TODO Auto-generated method stub
      return 0;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.IProgressDialog#lockMessageCenterUpdate()
    */
   public void lockMessageCenterUpdate()
   {
      // TODO Auto-generated method stub
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.IProgressDialog#unlockMessageCenterUpdate()
    */
   public void unlockMessageCenterUpdate()
   {
      // TODO Auto-generated method stub
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.IProgressDialog#log(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
    */
   public void log(int type, String group, String first, String second, String third)
   {
      // TODO Auto-generated method stub
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.IProgressDialog#setGroupingTitle(java.lang.String, int)
    */
   public void setGroupingTitle(String newVal, int type)
   {
      // TODO Auto-generated method stub
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.IProgressDialog#setFieldOne(java.lang.String, int)
    */
   public void setFieldOne(String newVal, int type)
   {
      // TODO Auto-generated method stub
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.IProgressDialog#setFieldTwo(java.lang.String, int)
    */
   public void setFieldTwo(String newVal, int type)
   {
      // TODO Auto-generated method stub
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.IProgressDialog#setFieldThree(java.lang.String, int)
    */
   public void setFieldThree(String newVal, int type)
   {
      // TODO Auto-generated method stub
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.IProgressDialog#promptForClosure(java.lang.String, boolean)
    */
   public void promptForClosure(String buttonTitle, boolean beep)
   {
      // TODO Auto-generated method stub
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.IProgressDialog#getLogFileName()
    */
   public String getLogFileName()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.IProgressDialog#setLogFileName(java.lang.String)
    */
   public void setLogFileName(String value)
   {
      // TODO Auto-generated method stub
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.IProgressDialog#getDefaultExtension()
    */
   public String getDefaultExtension()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.IProgressDialog#setDefaultExtension(java.lang.String)
    */
   public void setDefaultExtension(String value)
   {
      // TODO Auto-generated method stub
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.IProgressDialog#getCollapse()
    */
   public boolean getCollapse()
   {
      // TODO Auto-generated method stub
      return false;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.IProgressDialog#setCollapse(boolean)
    */
   public void setCollapse(boolean value)
   {
      // TODO Auto-generated method stub
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.IProgressDialog#getCloseWhenDone()
    */
   public boolean getCloseWhenDone()
   {
      // TODO Auto-generated method stub
      return false;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.IProgressDialog#setCloseWhenDone(boolean)
    */
   public void setCloseWhenDone(boolean value)
   {
      // TODO Auto-generated method stub
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.IProgressDialog#getProgressExecutor()
    */
   public IProgressExecutor getProgressExecutor()
   {
      // TODO Auto-generated method stub
      return null;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.IProgressDialog#setProgressExecutor(org.netbeans.modules.uml.ui.support.messaging.IProgressExecutor)
    */
   public void setProgressExecutor(IProgressExecutor value)
   {
      // TODO Auto-generated method stub
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.IProgressDialog#getIsCancelled()
    */
   public boolean getIsCancelled()
   {
      // TODO Auto-generated method stub
      return false;
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.IProgressDialog#setIndeterminate(boolean)
    */
   public void setIndeterminate(boolean newVal)
   {
      // TODO Auto-generated method stub
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.IProgressDialog#setProgressController(org.netbeans.modules.uml.ui.support.messaging.IProgressController)
    */
   public void setProgressController(IProgressController value)
   {
      // TODO Auto-generated method stub
      
   }

   /* (non-Javadoc)
    * @see org.netbeans.modules.uml.ui.support.messaging.IProgressDialog#getProgressController()
    */
   public IProgressController getProgressController()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public void addListener(IProgressDialogListener listener) {

   		// TODO Auto-generated method stub
   	}

   	public void removeListener(IProgressDialogListener listener) {

   		// TODO Auto-generated method stub
	}
}
