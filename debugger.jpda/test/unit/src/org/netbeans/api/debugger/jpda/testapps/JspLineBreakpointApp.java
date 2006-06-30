/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
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

package org.netbeans.api.debugger.jpda.testapps;

import java.util.Vector;
import java.io.PrintWriter;
import java.io.IOException;

public final class JspLineBreakpointApp {

/**
 * Sample JSP line breakpoints application. DO NOT MODIFY - line numbers must not change in this source file.
 *
 * @author Libor Kotouc
 */
  public static void main(String[] args)
            throws IOException {




    StringBuffer sb = new StringBuffer(1024);


    try {









        
        
      sb.append("\r\n");
      sb.append("\r\n");
      sb.append("\r\n");
      sb.append("<html>\r\n");
      sb.append("    <head>\r\n");
      sb.append("        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">\r\n");
      sb.append("        <title>JSP Page</title>\r\n");
      sb.append("    </head>\r\n");
      sb.append("    <body>\r\n");
      sb.append("\r\n");
      sb.append("        <h1>Main Page</h1>\r\n");
      sb.append("        ");
      sb.append("<font color=\"red\">\r\n");
      sb.append("    INCLUDED from d directory\r\n");
      sb.append("</font>");
      sb.append("\r\n");
      sb.append("        <br/>\r\n");
      sb.append("        ");
      sb.append("<font color=\"blue\">\n");
      sb.append("    INCLUDED from &lt;web-root&gt; directory\n");
      sb.append("</font>");
      sb.append("\r\n");
      sb.append("        <br/>\r\n");
      sb.append("        ");
      sb.append("<font color=\"blue\">\n");
      sb.append("    INCLUDED from &lt;web-root&gt; directory\n");
      sb.append("</font>");
      sb.append("\r\n");
      sb.append("    \r\n");
      sb.append("    </body>\r\n");
      sb.append("</html>\r\n");
    } catch (Throwable t) {

        
        
        
        
        
        
        
    }
//    out.flush();
  }
}
