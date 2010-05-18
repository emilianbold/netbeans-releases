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

package org.netbeans.modules.uml.common;

import java.io.*;

/**
 * General Exception class for errors thrown in the application.
 */
public class ETException extends Exception
{

 /** Constructor. Wraps a regular exception. */
  public ETException (Throwable exc)
  {
    this( ETStrings.E_CMN_UNEXPECTED_EXC, exc.getMessage() );
    iException=exc;
  }

 /** Constructor. Wraps a ETException. */
  public ETException (ETException exc)
  {
    setMessage(exc.getMessage());
    iErrorID = ((ETException)exc).iErrorID;
    iException=exc;
  }

 /** Constructor.  Creates an exception with the specified message ID. */
  public ETException (MsgID ID)
  {
    setMessage(ID.get());
    iErrorID = ID.getID();
  }

 /** Constructor.  Creates an exception with the specified message ID.
     The occurences of %x are replaced by the given parameters.*/
  public ETException (MsgID ID, Object p1)
  {
    setMessage( ID.get(p1) );
    iErrorID = ID.getID();
  }

 /** Constructor.  Creates an exception with the specified message ID.
     The occurences of %x are replaced by the given parameters.*/
  public ETException (MsgID ID, Object p1, Object p2)
  {
    setMessage(ID.get(p1, p2));
    iErrorID = ID.getID();
  }

 /** Constructor.  Creates an exception with the specified message ID.
     The occurences of %x are replaced by the given parameters.*/
  public ETException (MsgID ID, Object p1, Object p2, Object p3)
  {
    setMessage(ID.get(p1, p2, p3));
    iErrorID = ID.getID();
  }

 /** Constructor.  Creates an exception with the specified message ID.
     The occurences of %x are replaced by the given parameters.*/
  public ETException (MsgID ID, Object p1, Object p2, Object p3, Object p4)
  {
    setMessage(ID.get(p1, p2, p3, p4));
    iErrorID = ID.getID();
  }

 /** Constructor.  Creates an exception with the specified message ID.
     The occurences of %x are replaced by the given parameters.*/
  public ETException (MsgID ID, Object[] params)
  {
    setMessage(ID.get(params));
    iErrorID = ID.getID();
  }

 /** This method gets the message ID for the current ETException object.
     @return the ID representing the message that corresponds to the ETException object. */
  public String getErrorID() { return iErrorID; }

 /** Overrides the error ID for this exception. */
  protected void setErrorID(String ID) { iErrorID = ID; }

 /** Local independent error ID.
     @serial */
  private String iErrorID;

 /** Overrides getMessage to return the locale dependant message. */
  public String getMessage() { return iMessage; }

 /** Sets the message to be displayed to the user. */
  protected void setMessage(String msg) { iMessage = msg; }

  public void printStackTrace(){

	  if(iException!=null){
		  iException.printStackTrace();
		  ETSystem.out.println("\nException wrapped in\n\n");
	  }
  }

  public void printStackTrace(PrintStream s){

	  if(iException!=null){
		  iException.printStackTrace(s);
		  s.println("\nException wrapped in\n\n");
	  }
  }

  public void printStackTrace(PrintWriter s){

	  if(iException!=null){
		  iException.printStackTrace(s);
		  s.println("\nException wrapped in\n\n");
	  }
  }

 /** Local dependent message.
     @serial */
  private String iMessage = "";
  private Throwable iException;

}
