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
 * RequirementsException.java
 *
 * Created on June 24, 2004, 1:50 PM
 */

package org.netbeans.modules.uml.core.requirementsframework;

/**
 * A requirements exception that also keeps track of the specific cause of the
 * exception.
 * @author  Trey Spiva
 */
public class RequirementsException extends Exception
{
   /** An unknown error has occured. */
   public static final int RP_E_UNKNOWN = -1;

   /** User Canceled out of Provider's Source Selection Dialog. */
   public static final int RP_E_NO_SELECTION = 0;

   /** Requirement Source has Already Been Added. */
   public static final int RP_E_DUPLICATESOURCE = 1;
   
   /** DOOORS Shared Edit Mode is Not Supported. */
   public static final int RP_E_SHAREDEDITNOTSUPPORTED = 2;
   
   /** Requirement Source Could Not Be Found. */
   public static final int RP_E_REQUIREMENTSOURCENOTFOUND = 3;
   
   /** Requirement Source Could Not Be Found. */
   public static final int RP_E_REQUIREMENTLIBRARYNOTFOUND = 4;
   
   private int m_ExceptionCode = 0;
   
   public RequirementsException(String msg)
   {
      this(RP_E_UNKNOWN, msg);
   }
   
   /**
    * Creates a new instance of <code>RequirementsException</code> without 
    * detail message.
    * 
    * @param error The error code that caused the exception.
    *
    * @see #RP_E_NO_SELECTION
    * @see #RP_E_DUPLICATESOURCE
    * @see #RP_E_SHAREDEDITNOTSUPPORTED              
    * @see #RP_E_REQUIREMENTSOURCENOTFOUND
    */
   public RequirementsException(int error)
   {
      this(error, "");
   }
   
   
   /**
    * Constructs an instance of <code>RequirementsException</code> with the specified detail message.
    * @param msg the detail message.
    * 
    * @param msg The exception message.
    * @param error The error code that caused the exception.
    *
    * @see #RP_E_NO_SELECTION
    * @see #RP_E_DUPLICATESOURCE
    * @see #RP_E_SHAREDEDITNOTSUPPORTED              
    * @see #RP_E_REQUIREMENTSOURCENOTFOUND
    */
   public RequirementsException(int error, String msg)
   {
      super(msg);
      setExceptionCode(error);
   }
   
   /**
    * Retrieves the exception error code.  The valid exception error code are:
    *
    * RP_E_NO_SELECTION
    * RP_E_DUPLICATESOURCE
    * RP_E_SHAREDEDITNOTSUPPORTED
    * RP_E_REQUIREMENTSOURCENOTFOUND
    *
    * @return The error code that caused the exception.
    *
    * @see #RP_E_NO_SELECTION
    * @see #RP_E_DUPLICATESOURCE
    * @see #RP_E_SHAREDEDITNOTSUPPORTED              
    * @see #RP_E_REQUIREMENTSOURCENOTFOUND
    */
   public int getExceptionCode()
   {
      return m_ExceptionCode;
   }
   
   /**
    * Retrieves the exception error code.  The valid exception error code are:
    *
    * RP_E_NO_SELECTION
    * RP_E_DUPLICATESOURCE
    * RP_E_SHAREDEDITNOTSUPPORTED
    * RP_E_REQUIREMENTSOURCENOTFOUND
    *
    * @param code The error code that caused the exception.
    *
    * @see #RP_E_NO_SELECTION
    * @see #RP_E_DUPLICATESOURCE
    * @see #RP_E_SHAREDEDITNOTSUPPORTED              
    * @see #RP_E_REQUIREMENTSOURCENOTFOUND
    */
   public void setExceptionCode(int code)
   {
      m_ExceptionCode = code;
   }
}
