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

import org.apache.jmeter.module.exceptions.InitializationException;
import org.apache.jmeter.module.integration.JMeterIntegrationEngine;
import org.apache.jmeter.module.nodes.RuntimeNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Jaroslav Bachorik
 */
public class ProcessesCleanupAction extends NodeAction {
  protected void performAction(Node[] node) {
    try {
      JMeterIntegrationEngine.getDefault().cleanup();
    } catch (InitializationException e) {
      e.printStackTrace();
    }
  }
  
  public String getName() {
    return "Cleanup all finished processes";
  }
  
  public HelpCtx getHelpCtx() {
    return HelpCtx.DEFAULT_HELP;
  }
  
  protected boolean enable(Node[] node) {
    try {
      return node.length == 1 && JMeterIntegrationEngine.getDefault().getProcesses().size() > 0;
    } catch (InitializationException e) {
      e.printStackTrace();
    }
    return false;
  }
}
