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

package org.apache.jmeter.module;

import org.apache.jmeter.module.cookies.JMeterEditable;
import org.openide.cookies.EditCookie;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

public final class ExternalEditAction extends CookieAction {
  
  protected void performAction(final Node[] activatedNodes) {
    final JMeterEditable c = (JMeterEditable) activatedNodes[0].getCookie(JMeterEditable.class);
    final DataObject dobj = (DataObject)activatedNodes[0].getCookie(DataObject.class);
    
    c.edit(dobj.getPrimaryFile());
  }
  
  protected int mode() {
    return CookieAction.MODE_EXACTLY_ONE;
  }
  
  public String getName() {
    return NbBundle.getMessage(ExternalEditAction.class, "CTL_ExternalEditAction");
  }
  
  protected Class[] cookieClasses() {
    return new Class[] {
      DataObject.class,
      JMeterEditable.class
    };
  }
  
  protected void initialize() {
    super.initialize();
    // see org.openide.util.actions.SystemAction.iconResource() javadoc for more details
    putValue("noIconInMenu", Boolean.TRUE);
  }
  
  public HelpCtx getHelpCtx() {
    return HelpCtx.DEFAULT_HELP;
  }
  
  protected boolean asynchronous() {
    return false;
  }
  
  
}

