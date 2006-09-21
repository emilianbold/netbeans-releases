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

package org.apache.jmeter.module.actions;

import org.apache.jmeter.module.cookies.JMeterProcessCookie;
import org.apache.jmeter.module.integration.JMeterIntegrationEngine;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;

/**
 *
 * @author Jaroslav Bachorik
 */
public class ManageJMeterProcessAction extends CookieAction {  
  protected void performAction(Node[] node) {
    try {
      JMeterProcessCookie cookie = (JMeterProcessCookie)node[0].getCookie(JMeterProcessCookie.class);
      if (cookie.getProcessDescriptor().isRunning()) {
        JMeterIntegrationEngine.getDefault().stopTestPlan(cookie.getProcessDescriptor());
      } else {
        JMeterIntegrationEngine.getDefault().runTestPlan(cookie.getProcessDescriptor());
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
//    System.out.println("Performed action");
  }
  
  protected int mode() {
    return CookieAction.MODE_EXACTLY_ONE;
  }
  
  public String getName() {
    JMeterProcessCookie cookie = (JMeterProcessCookie)getActivatedNodes()[0].getCookie(JMeterProcessCookie.class);
    return cookie.getProcessDescriptor().isRunning() ? "Stop" : "Rerun";
  }
  
  public HelpCtx getHelpCtx() {
    return HelpCtx.DEFAULT_HELP;
  }
  
  protected Class[] cookieClasses() {
    return new Class[]{JMeterProcessCookie.class};
  }
  
}
