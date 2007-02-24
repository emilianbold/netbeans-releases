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
