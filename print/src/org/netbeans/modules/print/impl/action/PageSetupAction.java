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
package org.netbeans.modules.print.impl.action;

import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;
import org.netbeans.modules.print.impl.ui.Option;

import static org.netbeans.modules.print.ui.PrintUI.*;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2007.03.09
 */
public final class PageSetupAction extends CallableSystemAction {

  /**{@inheritDoc}*/
  @Override
  public synchronized void performAction()
  {
    Option.showPageSetup();
  }

  @Override
  protected boolean asynchronous()
  {
    return false;
  }

  /**{@inheritDoc}*/
  @Override
  public String getName()
  {
    return i18n(PageSetupAction.class, "CTL_PageSetup_Action"); // NOI18N
  }
  
  /**{@inheritDoc}*/
  @Override
  public HelpCtx getHelpCtx()
  {
    return HelpCtx.DEFAULT_HELP;
  }
}
