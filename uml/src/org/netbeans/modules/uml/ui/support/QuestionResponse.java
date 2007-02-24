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
