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

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.beans.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.datatransfer.*;
import org.openide.actions.InstantiateAction;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.nodes.*;
import org.openide.loaders.*;
import org.openide.*;

/** Standard node representing a data object.
*
* @author Petr Jiricka
*/
public class PropertiesDataNode extends DataNode {

  /** generated Serialized Version UID */
//  static final long serialVersionUID = -7882925922830244768L;


  /** Create a data node for a given data object.
  * The provided children object will be used to hold all child nodes.
  * @param obj object to work with
  * @param ch children container for the node
  */
  public PropertiesDataNode (DataObject obj, Children ch) {
    super (obj, ch);
  }

  /* List new types that can be created in this node.
  * @return new types
  */
  public NewType[] getNewTypes () {
    return new NewType[] {
      new NewType() {

        public String getName() {
          return NbBundle.getBundle(PropertiesDataNode.class).getString("LAB_NewLocaleAction");
        }
        
        public HelpCtx getHelpCtx() {
          return HelpCtx.DEFAULT_HELP;
        }                             
         
        public void create() throws IOException {

          NotifyDescriptor.InputLine dlg = new NotifyDescriptor.InputLine(
              NbBundle.getBundle(PropertiesDataNode.class).getString("CTL_NewLocaleLabel"),
              NbBundle.getBundle(PropertiesDataNode.class).getString("CTL_NewLocaleTitle"));
              
          if (NotifyDescriptor.OK_OPTION.equals(TopManager.getDefault().notify(dlg))) {
            String newname = dlg.getInputText();
            try {
              if (newname.length() == 0)
                throw new IllegalArgumentException(NbBundle.getBundle(PropertiesDataNode.class).getString("MSG_LangExists"));
              if (newname.charAt(0) != PropertiesDataLoader.PRB_SEPARATOR_CHAR)
                newname = "" + PropertiesDataLoader.PRB_SEPARATOR_CHAR + newname;
                
              // copy the default file to a new file
              Node.Cookie prop = getCookie (DataObject.class);
              if (prop != null) {                               
                FileObject folder = ((DataObject)prop).getPrimaryFile().getParent();
                FileObject newFo = FileUtil.copyFile(((DataObject)prop).getPrimaryFile(), folder, 
                    ((DataObject)prop).getPrimaryFile().getName() + newname);
              }
              
            }
            catch (IllegalArgumentException e) {
              // catch & report badly formatted names
              NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                java.text.MessageFormat.format(
                  NbBundle.getBundle(PropertiesDataNode.class).getString("MSG_LangExists"),
                  new Object[] {newname}),
                NotifyDescriptor.ERROR_MESSAGE);
              TopManager.getDefault().notify(msg);
            }
            catch (IOException e) {
              // catch & report IO error
              NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                java.text.MessageFormat.format(
                  NbBundle.getBundle(PropertiesDataNode.class).getString("MSG_LangExists"),
                  new Object[] {newname}),
                NotifyDescriptor.ERROR_MESSAGE);
              TopManager.getDefault().notify(msg);
            }
          }

        }
         
      } // end of inner class
    };
  }

}

/*
 * <<Log>>
 */
