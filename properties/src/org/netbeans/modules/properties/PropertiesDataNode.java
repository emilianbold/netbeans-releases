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

package org.netbeans.modules.properties;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.beans.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.Iterator;
import javax.swing.text.Document;

import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileSystem;
import org.openide.util.datatransfer.*;
import org.openide.actions.InstantiateAction;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.actions.OpenAction;
import org.openide.nodes.*;
import org.openide.loaders.*;
import org.openide.*;
import org.openide.text.EditorSupport;

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
    initialize();
  }

  private void initialize () {
    setIconBase(PropertiesDataObject.PROPERTIES_ICON_BASE);
    setDefaultAction (SystemAction.get(OpenAction.class));
  }


  private void readObject(ObjectInputStream is) throws IOException, ClassNotFoundException {
    is.defaultReadObject();
    initialize();
  }

  /* List new types that can be created in this node.
  * @return new types
  */
  public NewType[] getNewTypes () {
    return new NewType[] {
      new NewType() {      
      
        FileObject folder;
        String newName;
        PropertiesFileEntry  fe;
        PropertiesStructure str;
        MultiDataObject prop;

        public String getName() {
          return NbBundle.getBundle(PropertiesDataNode.class).getString("LAB_NewLocaleAction");
        }
        
        public HelpCtx getHelpCtx() {
          return new HelpCtx (PropertiesDataNode.class.getName () + ".new_locale");
        }                             
         
        public void create() throws IOException {

          NotifyDescriptor.InputLine dlg = new NotifyDescriptor.InputLine(
              NbBundle.getBundle(PropertiesDataNode.class).getString("CTL_NewLocaleLabel"),
              NbBundle.getBundle(PropertiesDataNode.class).getString("CTL_NewLocaleTitle"));
              
          if (NotifyDescriptor.OK_OPTION.equals(TopManager.getDefault().notify(dlg))) {
            newName = dlg.getInputText();
            try {
              if (newName.length() == 0)
                throw new IllegalArgumentException(NbBundle.getBundle(PropertiesDataNode.class).getString("MSG_LangExists"));
              if (newName.charAt(0) != PropertiesDataLoader.PRB_SEPARATOR_CHAR)
                newName = "" + PropertiesDataLoader.PRB_SEPARATOR_CHAR + newName;
                
              // copy the default file to a new file
              prop = (MultiDataObject)getCookie(DataObject.class);
              if (prop != null) {
                fe = (PropertiesFileEntry)prop.getPrimaryEntry();
                str = fe.getHandler().getStructure();
                folder = prop.getPrimaryFile().getParent();
                
                folder.getFileSystem().runAtomicAction(
                new FileSystem.AtomicAction() {
                  public void run() throws IOException {
                    FileObject newFile = FileUtil.createData(folder, prop.getPrimaryFile().getName() + newName + 
                      "." + PropertiesDataLoader.PROPERTIES_EXTENSION);
                    BufferedWriter bw = null;  
                    FileLock lock = newFile.lock();
                    try {
                      bw = new BufferedWriter(new OutputStreamWriter(
                        new PropertiesEditorSupport.NewLineOutputStream(
                        newFile.getOutputStream(lock), fe.getPropertiesEditor().newLineType), "8859_1"));
                      for (Iterator it = str.allItems(); it.hasNext(); ) {
                        Element.ItemElem item1 = (Element.ItemElem)it.next();
                        Element.ItemElem item2 = new Element.ItemElem(null, 
                          new Element.KeyElem(null, item1.getKey()),
                          new Element.ValueElem(null, item1.getValue()),
                          new Element.CommentElem(null, item1.getComment()));
                        String ps = item2.printString();
                        bw.write(ps, 0, ps.length());
                      }
                    } 
                    finally {
                      if (bw != null) {
                        bw.flush();
                        bw.close();
                      }  
                      lock.releaseLock();
                    }  
                  }                                                                 
                }); // end of inner class which is run as atomicaction
                  
                /*FileObject folder = ((DataObject)prop).getPrimaryFile().getParent();
                FileObject newFo = FileUtil.copyFile(((DataObject)prop).getPrimaryFile(), folder, 
                  ((DataObject)prop).getPrimaryFile().getName() + newName);*/
              }
              
            }
            catch (IllegalArgumentException e) {
              // catch & report badly formatted names
              NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                java.text.MessageFormat.format(
                  NbBundle.getBundle(PropertiesDataNode.class).getString("MSG_LangExists"),
                  new Object[] {newName}),
                NotifyDescriptor.ERROR_MESSAGE);
              TopManager.getDefault().notify(msg);
            }
            catch (IOException e) {
              // catch & report IO error
              NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                java.text.MessageFormat.format(
                  NbBundle.getBundle(PropertiesDataNode.class).getString("MSG_LangExists"),
                  new Object[] {newName}),
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
