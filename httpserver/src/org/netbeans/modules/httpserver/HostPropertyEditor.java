/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.developer.modules.httpserver;

import java.beans.*;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openide.util.NbBundle;

/** Property editor for host property of HttpServerSettings class
*
* @author Ales Novak, Petr Jiricka
* @version 0.11 May 5, 1999
*/
public class HostPropertyEditor extends PropertyEditorSupport {

  private static final java.util.ResourceBundle bundle = NbBundle.getBundle(HostPropertyEditor.class);

  /** localized local host string*/
  private final static String LOCALHOST = bundle.getString("CTL_Local_host");

  /** localized local host string*/
  private final static String ANYHOST = bundle.getString("CTL_Any_host");

  /** array of hosts */
  private static final String[] hostNames = {LOCALHOST, ANYHOST};

  /** @return names of the supported LookAndFeels */
  public String[] getTags() {
    return hostNames;
  }

  /** @return text for the current value */
  public String getAsText () {
    String host = (String) getValue();
    if (host.equals(HttpServerSettings.LOCALHOST)) {
      return LOCALHOST;
    }
    else {
      return ANYHOST;
    }
  }

  /** @param text A text for the current value. */
  public void setAsText (String text) {
//System.out.println("set as text "   + text);
    if (text.equals(LOCALHOST)) {
      //HttpServerSettings.OPTIONS.setHost(HttpServerSettings.LOCALHOST);
      setValue(HttpServerSettings.LOCALHOST);
      return;
    }
    if (text.equals(ANYHOST)) {
      //HttpServerSettings.OPTIONS.setHost(HttpServerSettings.ANYHOST);
      setValue(HttpServerSettings.ANYHOST);
      return;
    }

    throw new IllegalArgumentException ();
  }   
  
  public void setValue(Object value) {
//System.out.println("setValue " + value.toString());
//util.Util.printStackTrace();
    super.setValue(value);
  }
}

/*
 * Log
 *  4    Gandalf   1.3         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         6/8/99   Petr Jiricka    
 *  2    Gandalf   1.1         5/11/99  Petr Jiricka    
 *  1    Gandalf   1.0         5/7/99   Petr Jiricka    
 * $
 */
