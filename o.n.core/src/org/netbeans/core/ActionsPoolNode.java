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
import java.util.ResourceBundle;

import org.openide.nodes.*;
import org.openide.actions.*;
import org.openide.loaders.DataFolder;
import org.openide.util.datatransfer.NewType;
import org.openide.util.actions.SystemAction;
import org.openide.util.NbBundle;

/** The node for the actions pool folder representation.
* Delegates most of its functionality to the original data folder node.
* Final only for better performance, can be unfinaled.
*
* @author Ian Formanek
*/
public final class ActionsPoolNode extends DataFolder.FolderNode {

  /** Actions which this node supports */
  static SystemAction[] staticActions;
  /** Actions of this node when it is top level actions node */
  static SystemAction[] topStaticActions;

  private static final Node.PropertySet[] NO_PROPERTIES = new Node.PropertySet[0];

  public ActionsPoolNode () {
    this (NbTopManager.getDefault ().getPlaces ().folders ().actions ());
  }
  
  /** Constructs this node with given node to filter.
  */
  ActionsPoolNode (DataFolder folder) {
    folder.super(new ActionsPoolChildren(folder));
    //JST: it displays only Menu as name!    super.setDisplayName(NbBundle.getBundle (ActionsPoolNode.class).getString("CTL_Actions_name"));
    super.setShortDescription(NbBundle.getBundle (ActionsPoolNode.class).getString("CTL_Actions_hint"));

    super.setIconBase ("/com/netbeans/developer/impl/resources/actions");
  }

  /** Support for new types that can be created in this node.
  * @return array of new type operations that are allowed
  */
  public NewType[] getNewTypes () {
    return new NewType[0];
  }

  /** Actions.
  * @return array of actions for this node
  */
  public SystemAction[] getActions () {
    if (staticActions == null)
      topStaticActions = new SystemAction [] {
        SystemAction.get (FileSystemAction.class),
        null,
        SystemAction.get(ToolsAction.class),
        SystemAction.get(PropertiesAction.class),
      };
    return topStaticActions;
  }

  /** Creates properties for this node */
  public Node.PropertySet[] getPropertySets () {
    return NO_PROPERTIES;
  }

  /** Children for the ActionsPoolNode. Creates ActionsPoolNodes or
  * ItemNodes as filter subnodes...
  */
  static final class ActionsPoolChildren extends FilterNode.Children {

    /** @param or original node to take children from */
    public ActionsPoolChildren (DataFolder folder) {
      super(folder.getNodeDelegate ());
    }

    /** Overriden, returns ActionsPoolNode filters of original nodes.
    *
    * @param node node to create copy of
    * @return ActionsPoolNode filter of the original node
    */
    protected Node copyNode (Node node) {
      DataFolder df = (DataFolder)node.getCookie(DataFolder.class);
      if (df != null) {
        return new ActionsPoolNode(df);
      }
      return new ActionItemNode(node);
    }

  }

  static final class ActionItemNode extends FilterNode {
    /** Icons for this node */
    static Image itemIcon;
    static Image itemIcon32;

    /** Actions which this node supports */
    static SystemAction[] staticActions;

    /** Constructs new filter node for Action item */
    ActionItemNode (Node filter) {
      super(filter, Children.LEAF);
    }

    /** Finds an icon for this node.
    * @see java.bean.BeanInfo
    * @param type constants from <CODE>java.bean.BeanInfo</CODE>
    * @return icon to use to represent the bean
    */
    public Image getIcon (int type) {
      if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) ||
          (type == java.beans.BeanInfo.ICON_MONO_16x16)) {
        if (itemIcon == null)
          itemIcon = Toolkit.getDefaultToolkit ().getImage (
            getClass ().getResource ("/com/netbeans/developer/impl/resources/action.gif"));
        return itemIcon;
      } else {
        if (itemIcon32 == null)
          itemIcon32 = Toolkit.getDefaultToolkit ().getImage (
            getClass ().getResource ("/com/netbeans/developer/impl/resources/action32.gif"));
        return itemIcon32;
      }
    }

    /** Finds an open icon for this node.
    *
    * @param type constants from <CODE>java.bean.BeanInfo</CODE>
    * @return icon to use to represent the bean when opened
    */
    public Image getOpenedIcon (int type) {
      return getIcon (type);
    }

    /** Actions.
    * @return array of actions for this node
    */
    public SystemAction[] getActions () {
      if (staticActions == null) {
        staticActions = new SystemAction [] {
          SystemAction.get(CopyAction.class),
          null,
          SystemAction.get(ToolsAction.class),
          SystemAction.get(PropertiesAction.class),
        };
      }
      return staticActions;
    }

    /** Disallows renaming.
    */
    public boolean canRename () {
      return false;
    }

    /** Creates properties for this node */
    public Node.PropertySet[] getPropertySets () {
      ResourceBundle bundle = NbBundle.getBundle(ActionsPoolNode.class);
      // default sheet with "properties" property set
      Sheet sheet = Sheet.createDefault();
      sheet.get(Sheet.PROPERTIES).put(
        new PropertySupport.Name(
          this,
          bundle.getString("PROP_ActionItemName"),
          bundle.getString("HINT_ActionItemName")
        )
      );
      // PENDING - enable and help properties missing
      return sheet.toArray();
    }

  } // end of ActionItemNode

}

/*
* Log
*  2    Gandalf   1.1         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  1    Gandalf   1.0         6/8/99   Ian Formanek    
* $
*/
