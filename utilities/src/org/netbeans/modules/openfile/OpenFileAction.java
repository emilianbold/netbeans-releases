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

package com.netbeans.examples.modules.openfile;

import java.io.IOException;
import javax.swing.JFileChooser;

import org.openide.TopManager;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;

/** Opens a file by file chooser. */
public class OpenFileAction extends CallableSystemAction {
  public String getName () {
    return SettingsBeanInfo.getString ("LBL_openFile");
  }
  public HelpCtx getHelpCtx () {
    return HelpCtx.DEFAULT_HELP;
  }
  public void performAction () {
    JFileChooser chooser = new JFileChooser ();
    chooser.setFileSelectionMode (JFileChooser.FILES_ONLY);
    if (chooser.showOpenDialog (null) == JFileChooser.APPROVE_OPTION)
      try {
        OpenFile.open (chooser.getSelectedFile (), false, null, 0);
      } catch (IOException e) {
        TopManager.getDefault ().notifyException (e);
      }
  }
  protected String iconResource () {
    return "/com/netbeans/examples/modules/openfile/openFile.gif";
  }
  public static void main (String[] ignore) {
    new OpenFileAction ().performAction ();
  }
}
