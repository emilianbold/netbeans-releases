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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.soa.jca.base.palette;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

/**
 * @author echou
 *
 */
public class GlobalRarPaletteMenu extends CallableSystemAction {

  /**
   * Creates a new palette menu.
   */
  public GlobalRarPaletteMenu () {
      myName = NbBundle.getMessage(GlobalRarPaletteMenu.class, "CTL_WSDLPalette"); // NOI18N
  }

  /**
   * If true, this action should be performed asynchronously in a private thread.
   * If false, it will be performed synchronously as called in the event thread.
   * @return true if this action should automatically be performed asynchronously
   */
  protected boolean asynchronous() {
      return false;
  }

  /**
   * Returns name of menu action.
   * @return name of menu action
   */
  public String getName() {
      return myName;
  }

  /**
   * Returns help context of menu action.
   * @return help context of menu action
   */
  public HelpCtx getHelpCtx() {
      return null;
  }

  /**
   * Does action.
   */
  public void performAction() {
      GlobalRarPaletteFactory.getPalette().showCustomizer();
  }

  private final String myName;
  private static final long serialVersionUID = 1L;
}
