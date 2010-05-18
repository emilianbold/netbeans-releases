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
