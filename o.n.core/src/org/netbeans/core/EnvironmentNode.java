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

package com.netbeans.developer.impl;

import java.awt.Image;
import java.awt.Toolkit;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ResourceBundle;
import java.util.Vector;

import com.netbeans.ide.*;
import com.netbeans.ide.loaders.*;
import com.netbeans.ide.options.*;
import com.netbeans.ide.actions.PropertiesAction;
import com.netbeans.ide.actions.RenameAction;
import com.netbeans.ide.util.HelpCtx;
import com.netbeans.ide.util.actions.*;
import com.netbeans.ide.nodes.*;
import com.netbeans.ide.util.NbBundle;
import com.netbeans.developer.impl.actions.*;

/** This object represents environment settings in the Corona system.
* This class is final only for performance purposes.
* Can be unfinaled if desired.
*
* @author Petr Hamernik, Dafe Simonek
*/
final class EnvironmentNode extends AbstractNode {
  /** generated Serialized Version UID */
  static final long serialVersionUID = 4782447107972624693L;
  /** icon base for icons of this node */
  private static final String EN_ICON_BASE = "/com/netbeans/developer/impl/resources/environment";

  /** base instance of the node */
  private static EnvironmentNode node;

  /** children to use */
  private static Children children;
  

  /** Constructor */
  private EnvironmentNode () {
    super (children = new Children.Array());
    initialize();
  }

  /** Method to add an node to the environment.
  */
  public static void addNode (Node n) {
    getDefault ();
    
    children.add (new Node[] { n });
  }
  
  /** Method to add an node to the environment.
  */
  public static void removeNode (Node n) {
    children.remove (new Node[] { n });
  }

  /** Default instance */
  public static synchronized Node getDefault () {
    if (node == null) {
      node = new EnvironmentNode ();
    }
    return node;
  }

  public boolean canRename () {
    return true;
  }

  /** Does all initialization */
  private void initialize () {
    initializeChildren();
    setName(NbBundle.getBundle(EnvironmentNode.class).
                   getString("CTL_Environment_name"));
    setIconBase(EN_ICON_BASE);
    createProperties();
  }

  /** Initialize children of this node - adds 6 currently defined subnodes. */
  private void initializeChildren () {
  /*
    ret = new Node[8];
    ret[0] = new FSPoolNode(this);
    ret[1] = new MainWindowNode(this);
    ret[2] = CoronaTopManager.getWorkspacePoolContextNode(this);
    ret[3] = CoronaTopManager.getShortcutNode (this);
    if (paletteContextNode != null) // deserialized
      ret[4] = paletteContextNode;
    else {
      ret[4] = new SerializableFilterNode(PaletteContext.getPaletteContext(), this);
      paletteContextNode = ret[4];
    }
    ret[5] = com.netbeans.developer.modules.debugger.JavaDebuggerNode.getDebuggerNode(this);
    ret[6] = com.netbeans.developer.impl.execution.ExecutionNode.getExecutionNode(this);
    ret[7] = com.netbeans.developer.defaults.Default.getDefaultActions(this);

    getChildren().add(ret);
    */
  }

  /** Method that prepares properties. Called from initialize.
  */
  protected void createProperties () {
    final ResourceBundle bundle = NbBundle.getBundle(EnvironmentNode.class);
    // default sheet with "properties" property set
    Sheet sheet = Sheet.createDefault();
    sheet.get(Sheet.PROPERTIES).put(
      new PropertySupport.ReadWrite (
        EnvironmentNode.this.PROP_DISPLAY_NAME,
        String.class,
        bundle.getString("PROP_Environment_name"),
        bundle.getString("HINT_Environment_name")
      ) {
        public Object getValue() {
          return EnvironmentNode.this.getName();
        }
        public void setValue(Object val) {
          if (! (val instanceof String)) return;
          super.setName((String) val);
        }
      }
    );
    // and set new sheet
    setSheet(sheet);
  }

  /** renames this node */
  /*public void rename(String name) {
    String old = getDisplayName();
    setDisplayName(name);
    firePropertyChange(EnvironmentNode.this.PROP_DISPLAY_NAME, old, name);
  }*/

  /** Getter for set of actions that should be present in the
  * popup menu of this node. This set is used in construction of
  * menu returned from getContextMenu and specially when a menu for
  * more nodes is constructed.
  *
  * @return array of system actions that should be in popup menu
  */
  public SystemAction[] createActions () {
    return new SystemAction[] {
      SystemAction.get(RenameAction.class),
      null,
      SystemAction.get(PropertiesAction.class)
    };
  }

  /** serializes the class */
  /*private void writeObject(ObjectOutputStream os)
  throws IOException {
    os.defaultWriteObject(); // outer ref
    os.writeObject(getDisplayName());
  }*/

  /** deserializes the class */
  /*private void readObject(ObjectInputStream is)
  throws IOException, ClassNotFoundException {
    is.defaultReadObject(); // outer ref
    setDisplayName((String)is.readObject());
    is.registerValidation(new java.io.ObjectInputValidation() {
      public void validateObject() {
        initialize();
        thisNodeChange();
      }
    }, 0);
  }*/
}

/*
 * Log
 *  12   Gandalf   1.11        3/26/99  Jaroslav Tulach 
 *  11   Gandalf   1.10        3/26/99  Ian Formanek    Fixed use of obsoleted 
 *       NbBundle.getBundle (this)
 *  10   Gandalf   1.9         3/18/99  Jaroslav Tulach 
 *  9    Gandalf   1.8         2/25/99  Jaroslav Tulach Change of clipboard 
 *       management  
 *  8    Gandalf   1.7         2/12/99  Ian Formanek    Reflected renaming 
 *       Desktop -> Workspace
 *  7    Gandalf   1.6         1/25/99  Jaroslav Tulach Added default project, 
 *       its desktop and changed default explorer in Main.
 *  6    Gandalf   1.5         1/20/99  Jaroslav Tulach 
 *  5    Gandalf   1.4         1/7/99   Ian Formanek    
 *  4    Gandalf   1.3         1/7/99   Ian Formanek    fixed resource names
 *  3    Gandalf   1.2         1/6/99   Ian Formanek    Fixed outerclass 
 *       specifiers uncompilable under JDK 1.2
 *  2    Gandalf   1.1         1/6/99   Jaroslav Tulach ide.* extended to 
 *       ide.loaders.*
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.12        --/--/98 Jan Formanek    Shortcuts and Workspaces moved here from the MainWindowNode
 */
