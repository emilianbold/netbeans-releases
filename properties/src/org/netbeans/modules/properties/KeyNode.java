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

import org.openide.util.datatransfer.*;
import org.openide.actions.*;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.nodes.*;
import org.openide.TopManager;
import org.openide.NotifyDescriptor;

/** Standard node representing a key-value-comment item in the properties file.
*
* @author Petr Jiricka
*/
public class KeyNode extends AbstractNode {

  /** generated Serialized Version UID */
//  static final long serialVersionUID = -7882925922830244768L;

  /** Icon base for the KeyNode node */
  static final String ITEMS_ICON_BASE =
    "com/netbeans/developer/modules/loaders/properties/propertiesKey";
                              
  /** Structure on top of which this element lives */
  private PropertiesStructure struct;                
  /** Key for the element */
  private String itemKey;

  /** Create a data node for a given key.
  * The provided children object will be used to hold all child nodes.
  * @param entry entry to work with
  * @param ch children container for the node
  */
  public KeyNode (PropertiesStructure struct, String itemKey) {
    super (Children.LEAF);
    this.struct = struct;
    this.itemKey = itemKey;
    super.setName (itemKey);
    setDefaultAction(SystemAction.get(OpenAction.class));
    setActions(
      new SystemAction[] {
        SystemAction.get(OpenAction.class),
        SystemAction.get(ViewAction.class),
        SystemAction.get(FileSystemAction.class),
        null,
        SystemAction.get(CutAction.class),
        SystemAction.get(CopyAction.class),
        null,
        SystemAction.get(DeleteAction.class),
        SystemAction.get(RenameAction.class),
        null,
        SystemAction.get(ToolsAction.class),
        SystemAction.get(PropertiesAction.class)
      }
    );
    setIconBase (ITEMS_ICON_BASE);
  }

  /** Get the represented item.
   * @return the item
  */
  public Element.ItemElem getItem() {
    Element.ItemElem item = struct.getItem(itemKey);
    if (item == null)
      System.out.println("Warning - item not found for KeyNode : " + itemKey);
    return item;
  }


  /** Indicate whether the node may be destroyed.
   * @return true.
   */
  public boolean canDestroy () {
    return true;
  }

  /* Destroyes the node
  */
  public void destroy () throws IOException {
    struct.deleteItem(itemKey);
    super.destroy ();
  }

  /* Returns true if this node allows copying.
  * @returns true.
  */
  public final boolean canCopy () {
    return true;
  }

  /* Returns true if this node allows cutting.
  * @returns true.
  */
  public final boolean canCut () {
    return true;
  }

  /* Returns true if this node can be renamed.
  * @returns true.
  */
  public final boolean canRename () {
    return true;
  }

  /* Rename the node.
  * @param name new name for the object
  * @exception IllegalArgumentException if the rename failed
  */
  public void setName (String name) {
    String oldKey = itemKey;
    itemKey = name;
//System.out.println("Setting name = " + name);
    if (!struct.renameItem(oldKey, name)) {
      itemKey = oldKey;
      NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
        NbBundle.getBundle(KeyNode.class).getString("MSG_CannotRenameKey"),
        NotifyDescriptor.ERROR_MESSAGE);
      TopManager.getDefault().notify(msg);
      return;
    }
    // regenerate all children
/*    Node par = getParentNode();
    PropertiesFileEntry.PropKeysChildren ch = (PropertiesFileEntry.PropKeysChildren)par.getChildren();
    ch.mySetKeys();*/
  }

  /** Set all actions for this node.
  * @param actions new list of actions
  */
  public void setActions(SystemAction[] actions) {
    systemActions = actions;
  }

  /* Initializes sheet of properties. Allow subclasses to
  * overwrite it.
  * @return the default sheet to use
  */
  protected Sheet createSheet () {
    Sheet s = Sheet.createDefault ();
    Sheet.Set ss = s.get (Sheet.PROPERTIES);

    Node.Property p;

    // Key property
    p = new PropertySupport.ReadWrite (
      PROP_NAME,
      String.class,
      NbBundle.getBundle(KeyNode.class).getString("PROP_item_key"),
      NbBundle.getBundle(KeyNode.class).getString("HINT_item_key")
    ) {
      public Object getValue () {
        return itemKey;
      }

      public void setValue (Object val) throws IllegalAccessException,
      IllegalArgumentException, InvocationTargetException {
      try {
        if (!(val instanceof String))
          throw new IllegalArgumentException();

        KeyNode.this.setName ((String)val);
      } catch (Exception e) {e.printStackTrace();}
      }
    };
    p.setName (Element.ItemElem.PROP_ITEM_KEY);
    ss.put (p);

    // Value property
    p = new PropertySupport.ReadWrite (
      Element.ItemElem.PROP_ITEM_VALUE,
      String.class,
      NbBundle.getBundle(KeyNode.class).getString("PROP_item_value"),
      NbBundle.getBundle(KeyNode.class).getString("HINT_item_value")
    ) {
      public Object getValue () {
        return getItem().getValue();
      }

      public void setValue (Object val) throws IllegalAccessException,
      IllegalArgumentException, InvocationTargetException {
        if (!(val instanceof String))
          throw new IllegalArgumentException();

        getItem().setValue((String)val);
      }
    };
    p.setName (Element.ItemElem.PROP_ITEM_VALUE);
    ss.put (p);

    // Comment property
    p = new PropertySupport.ReadWrite (
      Element.ItemElem.PROP_ITEM_COMMENT,
      String.class,
      NbBundle.getBundle(KeyNode.class).getString("PROP_item_comment"),
      NbBundle.getBundle(KeyNode.class).getString("HINT_item_comment")
    ) {
      public Object getValue () {
        return getItem().getComment();
      }

      public void setValue (Object val) throws IllegalAccessException,
      IllegalArgumentException, InvocationTargetException {
        if (!(val instanceof String))
          throw new IllegalArgumentException();

        getItem().setComment((String)val);
      }
    };
    p.setName (Element.ItemElem.PROP_ITEM_COMMENT);
    ss.put (p);

/*
    try {
      p = new PropertySupport.Reflection (
        getItem(), String.class, "getValue", "setValue"
      );
      p.setName (Element.ItemElem.PROP_ITEM_VALUE);
      p.setDisplayName (NbBundle.getBundle(KeyNode.class).getString("PROP_item_value"));
      p.setShortDescription (NbBundle.getBundle(KeyNode.class).getString("HINT_item_value"));
      ss.put (p);

      p = new PropertySupport.Reflection (
        item, String.class, "getComment", "setComment"
      );
      p.setName (Element.ItemElem.PROP_ITEM_COMMENT);
      p.setDisplayName (NbBundle.getBundle(KeyNode.class).getString("PROP_item_comment"));
      p.setShortDescription (NbBundle.getBundle(KeyNode.class).getString("HINT_item_comment"));
      ss.put (p);

    } catch (Exception ex) {
      throw new InternalError ();
    }
   */ 
    return s;
  }


  /** Support for firing property change.
  * @param ev event describing the change
  */
  void fireChange (PropertyChangeEvent ev) {
    firePropertyChange (ev.getPropertyName (), ev.getOldValue (), ev.getNewValue ());
    if (ev.getPropertyName ().equals (PresentableFileEntry.PROP_NAME)) {
      super.setName (itemKey);
      return;
    }
  }
  
}

/*
 * <<Log>>
 *  6    Gandalf   1.5         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  5    Gandalf   1.4         6/8/99   Petr Jiricka    
 *  4    Gandalf   1.3         6/6/99   Petr Jiricka    
 *  3    Gandalf   1.2         5/16/99  Petr Jiricka    
 *  2    Gandalf   1.1         5/14/99  Petr Jiricka    
 *  1    Gandalf   1.0         5/12/99  Petr Jiricka    
 * $
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 Jaroslav Tulach actions are lookuped in the loaders pool
 *  0    Tuborg    0.12        --/--/98 Ales Novak      Serializable
 *  0    Tuborg    0.13        --/--/98 Jaroslav Tulach default action for templates
 */
