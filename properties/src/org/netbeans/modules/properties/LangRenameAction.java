/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package com.netbeans.developer.modules.loaders.properties;

import java.net.URL;
import java.util.ResourceBundle;

import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.actions.*;
import org.openide.nodes.Node;
import org.openide.actions.RenameAction;


/** Rename a node.
* @see Node#setName
*
* @author   Jiricka
* @version  0.13, Apr 26, 1999
*/
public class LangRenameAction extends RenameAction {
  /** generated Serialized Version UID */
//  static final long serialVersionUID = 1261145028106838566L;

/*  protected boolean enable (Node[] activatedNodes) {
    boolean en = super.enable(activatedNodes);
    if (activatedNodes[0] instanceof PropertiesLocaleNode) {
      if ...
    }  
    return en;
  }*/


  protected void performAction (Node[] activatedNodes) {
    Node n = activatedNodes[0]; // we supposed that one node is activated
    if (!(n instanceof PropertiesLocaleNode))
      throw new InternalError("Node is not PropertiesLocaleNode (renaming language)");
    PropertiesLocaleNode pln = (PropertiesLocaleNode)n;  
      
    //RenameCookie ren = (RenameCookie) Cookies.getInstanceOf (n.getCookie(), RenameCookie.class);
                                           
    String lang = Util.getLocalePartOfFileName (pln.getFileEntry());
    if (lang.length() > 0)
      if (lang.charAt(0) == PropertiesDataLoader.PRB_SEPARATOR_CHAR)
        lang = lang.substring(1);
        
    NotifyDescriptor.InputLine dlg = new NotifyDescriptor.InputLine(
       NbBundle.getBundle("org.openide.actions.Bundle").getString("CTL_RenameLabel"),
       NbBundle.getBundle("org.openide.actions.Bundle").getString("CTL_RenameTitle"));
    dlg.setInputText(lang);
    if (NotifyDescriptor.OK_OPTION.equals(TopManager.getDefault().notify(dlg))) {
      try {
        pln.setName(Util.assembleName (((PropertiesFileEntry)pln.getFileEntry()).basicName, dlg.getInputText()));
      }
      catch (IllegalArgumentException e) {
        // catch & report badly formatted names
        NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
          java.text.MessageFormat.format(
            NbBundle.getBundle("org.openide.actions.Bundle").getString("MSG_BadFormat"),
            new Object[] {n.getName()}),
          NotifyDescriptor.ERROR_MESSAGE);
        TopManager.getDefault().notify(msg);
      }
    }
  }
}


/*
 * <<Log>>
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         6/10/99  Petr Jiricka    
 *  2    Gandalf   1.1         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         5/12/99  Petr Jiricka    
 * $
 */
