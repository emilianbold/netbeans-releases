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
 *
 * Created on Jul 1, 2003
 * @author Trey Spiva
 */
package org.netbeans.modules.uml.ui.support;

import org.netbeans.modules.uml.ui.support.commondialogs.MessageResultKindEnum;

/**
 * The users response from a question dialog.  The details specify which button
 * was pressed as well as the value of the checkbox (if any).
 *
 * @author Trey Spiva
 */
public class QuestionResponse implements MessageResultKindEnum
{
   private boolean m_IsChecked = false;
   private int     m_Result    = SQDRK_RESULT_UNKNOWN;

   public QuestionResponse()
   {
      this(false, SQDRK_RESULT_UNKNOWN);
   }
   
   public QuestionResponse(boolean isChecked, int response)
   {
      setChecked(isChecked);
      setResult(response);
   }
   
   /**
    * Specifies if the check box was selected when the user responded to the
    * question.
    * 
    * @return <b>true</b> if the checkbox is selected, <b>false</b> if the 
    *         checkbox is not selected.
    */
   public boolean isChecked()
   {
      return m_IsChecked;
   }

   /**
    * Set whether or not the check box was selected when the user responded to 
    * the question.
    * 
    * @param value <b>true</b> if the checkbox is selected, <b>false</b> if the 
    *               checkbox is not selected.
    */
   public void setChecked(boolean value)
   {
      m_IsChecked = value;
   }
   
   /**
    * Specifies the answer to the question that was asked the user.  
    * 
    * @return One of the MessageResultKindEnum values.
    * @see MessageResultKindEnum
    */
   public int getResult()
   {
      return m_Result;
   }

   

   /**
    * Sets the answer to the question that was asked the user.  
    * 
    * @param value One of the MessageResultKindEnum values.
    * @see MessageResultKindEnum
    */
   public void setResult(int value)
   {
      m_Result = value;
   }

}
