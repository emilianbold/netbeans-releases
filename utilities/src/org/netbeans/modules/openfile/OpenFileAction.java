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

package com.netbeans.developer.modules.openfile;

import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;

import org.openide.TopManager;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;

/** Opens a file by file chooser. */
public class OpenFileAction extends CallableSystemAction {
  
static final long serialVersionUID =-3424129228987962529L;
  public String getName () {
    return SettingsBeanInfo.getString ("LBL_openFile");
  }
  
  public HelpCtx getHelpCtx () {
    return new HelpCtx (OpenFileAction.class);
  }
  
  protected String iconResource () {
    return "/com/netbeans/developer/modules/openfile/openFile.gif";
  }
  
  /** Last-used directory. */
  private static File currDir = null;
  public void performAction () {
    JFileChooser chooser = new JFileChooser ();
    chooser.setFileSelectionMode (JFileChooser.FILES_ONLY);
    if (currDir != null) chooser.setCurrentDirectory (currDir);
    if (chooser.showOpenDialog (null) == JFileChooser.APPROVE_OPTION) {
      OpenFile.open (chooser.getSelectedFile (), false, null, 0);
    }
    currDir = chooser.getCurrentDirectory ();
  }
  
  public static void main (String[] ignore) {
    new OpenFileAction ().performAction ();
  }
  
}
