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

import java.util.Set;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Comparator;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dialog;
import javax.swing.JPanel;

import com.netbeans.ide.*;
import com.netbeans.ide.filesystems.*;
import com.netbeans.ide.loaders.*;
import com.netbeans.ide.windows.*;
import com.netbeans.ide.actions.*;
import com.netbeans.ide.text.*;
import com.netbeans.ide.util.*;
import com.netbeans.ide.util.actions.*;
import com.netbeans.ide.nodes.Node;
import com.netbeans.ide.nodes.Children;
import com.netbeans.ide.nodes.AbstractNode;
import com.netbeans.ide.nodes.NodeListener;
import com.netbeans.ide.util.datatransfer.NewType;

/** Object that provides main functionality for properties data loader.
* This class is final only for performance reasons,
* can be unfinaled if desired.
*
* @author Ian Formanek
*/

public class PropertiesLocaleNode extends FileEntryNode {
      
  static final String PROPERTIES_ICON_BASE2 = PropertiesDataObject.PROPERTIES_ICON_BASE2;
        
  /** Creates a new PropertiesLocaleNode for the given locale-specific file */
  public PropertiesLocaleNode (PropertiesFileEntry fe) {
    super(fe, fe.getChildren());
    setDisplayName(Util.getPropertiesLabel(fe));
    setIconBase(PROPERTIES_ICON_BASE2);
    setDefaultAction (SystemAction.get(OpenAction.class));

    // edit as a viewcookie
    getCookieSet().add(((PropertiesDataObject)fe.getDataObject()).getOpenSupport());
    getCookieSet().add (((PropertiesFileEntry)getFileEntry()).getPropertiesEditor());
  }
  
  /** Lazily initialize set of node's actions (overridable).
  * The default implementation returns <code>null</code>.
  * <p><em>Warning:</em> do not call {@link #getActions} within this method.
  * If necessary, call {@link NodeOp#getDefaultActions} to merge in.
  * @return array of actions for this node, or <code>null</code> to use the default node actions
  */
  protected SystemAction[] createActions () {
    return new SystemAction[] {
      SystemAction.get(OpenAction.class),
      SystemAction.get(ViewAction.class),
      SystemAction.get(FileSystemAction.class),
      null,
      SystemAction.get(CutAction.class),
      SystemAction.get(CopyAction.class),
      SystemAction.get(PasteAction.class),
      null,
      SystemAction.get(DeleteAction.class),
      SystemAction.get(LangRenameAction.class),
      null,
      SystemAction.get(NewAction.class),
      SystemAction.get(SaveAsTemplateAction.class),
      null,
      SystemAction.get(ToolsAction.class),
      SystemAction.get(PropertiesAction.class)
    };
  }

  /** Set the system name. Fires a property change event.
  * Also may change the display name according to {@link #displayFormat}.
  *
  * @param s the new name
  */
  public void setName (String s) {
    super.setName (s);
    setDisplayName (Util.getPropertiesLabel(getFileEntry()));
  }

  /** Clones this node */
  public Node cloneNode() {
    return new PropertiesLocaleNode((PropertiesFileEntry)getFileEntry());
  }                                                               
                                      
  /** Returns a string from my bundle. */                                    
  private String getString(String what) {
    return NbBundle.getBundle(PropertiesLocaleNode.class).getString(what);
  }
  
  /* List new types that can be created in this node.
  * @return new types
  */
  public NewType[] getNewTypes () {
    return new NewType[] {
      new NewType() {
      
        public String getName() {
          return NbBundle.getBundle(PropertiesDataNode.class).getString("LAB_NewPropertyAction");
        }
        
        public HelpCtx getHelpCtx() {
          return HelpCtx.DEFAULT_HELP;
        }                             
         
        public void create() throws IOException {
          NewPropertyDialog dia = new NewPropertyDialog();
          Dialog d = dia.getDialog();
          dia.focusKey();
          d.setVisible(true);
          dia.focusKey();
          if (dia.getOKPressed ()) {                            
            if (((PropertiesFileEntry)getFileEntry()).getHandler().getStructure().addItem(
                  dia.getKeyText(), dia.getValueText(), dia.getCommentText()))
              ;
            else {
              NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                java.text.MessageFormat.format(
                  NbBundle.getBundle(PropertiesLocaleNode.class).getString("MSG_KeyExists"),
                  new Object[] {dia.getKeyText()}),
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
 *  3    Gandalf   1.2         6/6/99   Petr Jiricka    
 *  2    Gandalf   1.1         5/13/99  Petr Jiricka    
 *  1    Gandalf   1.0         5/12/99  Petr Jiricka    
 * $
 */
