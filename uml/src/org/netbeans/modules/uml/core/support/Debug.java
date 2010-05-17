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


package org.netbeans.modules.uml.core.support;

import javax.swing.JOptionPane;

/**
 * A set of debug utilities.
 *
 * @author Trey Spiva
 */
public class Debug extends DebugSupport
{
   /**
    * Asserts that a condition is true. If the assertion fails the error
    * will be reported in a dialog box.
    *
    * @param condition The test condition.
    * @return <code>true</code> if the condition was successiful.
    */
   public static boolean assertTrue(boolean condition)
   {
      return assertTrue(condition, "Not Equal To True Error");
   }

   /**
    * Asserts that a condition is true. If the assertion fails the error
    * will be reported in a dialog box.
    * 
    * @param condition The test condition.
    * @param msg The error message to display to the user.
    * @return <code>true</code> if the condition was successiful.
    */
   public static boolean assertTrue(boolean condition, String msg)
   {
      boolean retVal = true;

      try
      {
         assert(condition == true);
      }
      catch (AssertionError e)
      {
         fail(e, msg);
         retVal = false;
      }

      return retVal;
   }

   /**
    * Asserts that a condition is false. If the assertion fails the error
    * will be reported in a dialog box.
    * 
    * @param condition The test condition.
    * @return <code>true</code> if the condition was successiful.
    */
   public static boolean assertFalse(boolean condition)
   {
      return assertFalse(condition, "Not Equal To False Error");
   }

   /**
    * Asserts that a condition is false. If the assertion fails the error
    * will be reported in a dialog box.
    * 
    * @param condition The test condition.
    * @param msg The error message to display to the user.
    * @return <code>true</code> if the condition was successiful.
    */
   public static boolean assertFalse(boolean condition, String msg)
   {
      boolean retVal = true;

      try
      {
         assert(condition == false);
      }
      catch (AssertionError e)
      {
         fail(e, msg);
         retVal = false;
      }

      return retVal;
   }

   /**
    * Asserts that an object isn't null. If the assertion fails the error
    * will be reported in a dialog box.
    * 
    * @param condition The test condition.
    * @return <code>true</code> if the condition was successiful.
    */
   public static boolean assertNotNull(Object object)
   {
      return assertNotNull(object, "Not Equal To Null Error");
   }

   /**
    * Asserts that an object isn't null. If the assertion fails the error
    * will be reported in a dialog box.
    * 
    * @param object The object to test.
    * @param msg The error message to display to the user.
    * @return <code>true</code> if the condition was successiful.
    */
   public static boolean assertNotNull(Object object, String msg)
   {
      boolean retVal = true;

      try
      {
         assert(object != null);
      }
      catch (AssertionError e)
      {
         fail(e, msg);
         retVal = false;
      }

      return retVal;
   }

   /**
    * Asserts that an object is null. If the assertion fails the error
    * will be reported in a dialog box.
    * 
    * @param condition The test condition.
    * @return <code>true</code> if the condition was successiful.
    */
   public static boolean assertNull(Object object)
   {
      return assertNull(object, "Equal To Null Error");
   }

   /**
    * Asserts that an object is null. If the assertion fails the error
    * will be reported in a dialog box.
    * 
    * @param condition The test condition.
    * @param msg The error message to display to the user.
    * @return <code>true</code> if the condition was successiful.
    */
   public static boolean assertNull(Object object, String msg)
   {
      boolean retVal = true;

      try
      {
         assert(object == null);
      }
      catch (AssertionError e)
      {
         fail(e, msg);
         retVal = false;
      }

      return retVal;
   }

   /** 
    * Reports an exception to the user.  The error is sent to stderr as well
    * as reported to a dialog box.
    * 
    * @param e
    * @param msg
    */
   protected static void fail(AssertionError e, String msg)
   {
	   //return if debug not enabled
	   if(!isEnabled())
		   return;
	   
      StringBuffer errMsg = new StringBuffer(msg);

      StackTraceElement[] elements = e.getStackTrace();
      // Since the top Element will be the assert I will not add it to the 
      // error message.
      for (int i = 2; i < elements.length; i++)
      {
         errMsg.append(System.getProperty("line.separator"));

         StackTraceElement element = elements[i];
         
         // the "\t at " at the beginning of the line helps us to add a hyperlink to the code
         errMsg.append("\t at " + element.toString() );
      }
      
      out.println(errMsg);
      //JOptionPane.showMessageDialog(null, errMsg, "Assertion Failure", JOptionPane.ERROR_MESSAGE);
   }
}
