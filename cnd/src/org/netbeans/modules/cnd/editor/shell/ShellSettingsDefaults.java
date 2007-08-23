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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.editor.shell;

import org.netbeans.editor.*;
import org.netbeans.editor.ext.ExtSettingsDefaults;

/**
* Default settings values for Fortran.
*
*/

public class ShellSettingsDefaults extends ExtSettingsDefaults {

  public static final Boolean defaultShellWordMatchMatchCase = Boolean.TRUE;


  public static final Acceptor defaultIndentHotCharsAcceptor
    = new Acceptor() {
        public boolean accept(char ch) {
          switch (ch) {
            case '}':
            return true;
          }

          return false;
        }
      };

  // DO WE NEED THIS?
  public static final String defaultWordMatchStaticWords
  = "Exception IntrospectionException FileNotFoundException IOException" //NOI18N
    + " ArrayIndexOutOfBoundsException ClassCastException ClassNotFoundException" //NOI18N
    + " CloneNotSupportedException NullPointerException NumberFormatException" //NOI18N
    + " SQLException IllegalAccessException IllegalArgumentException"; //NOI18N

}//ShellSettingsDefaults
