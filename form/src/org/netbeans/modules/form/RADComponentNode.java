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

package com.netbeans.developer.modules.loaders.form;

import com.netbeans.ide.actions.*;
import com.netbeans.ide.cookies.SaveCookie;
import com.netbeans.ide.nodes.*;
import com.netbeans.ide.util.NbBundle;
import com.netbeans.ide.util.Utilities;
import com.netbeans.ide.util.actions.SystemAction;
import com.netbeans.ide.util.datatransfer.PasteType;
import com.netbeans.developer.modules.loaders.form.actions.*;

import java.awt.Image;
import java.awt.datatransfer.*;
import java.text.MessageFormat;
import java.util.ArrayList;

/** 
*
* @author Ian Formanek
*/
public class RADComponentNode extends AbstractNode implements RADComponentCookie {

  static DataFlavor RAD_COMPONENT_COPY_FLAVOR = new RADDataFlavor (
    RADComponentNode.class,
    "RAD_COMPONENT_COPY_FLAVOR"//FormEditor.getFormBundle ().getString ("radVisualNodeFlavorName")
  );
  static DataFlavor RAD_COMPONENT_CUT_FLAVOR = new RADDataFlavor (
    RADComponentNode.class,
    "RAD_COMPONENT_CUT_FLAVOR"//FormEditor.getFormBundle ().getString ("radVisualNodeFlavorName")
  );


  private final static MessageFormat nameFormat = new MessageFormat (NbBundle.getBundle (RADComponentNode.class).getString ("FMT_ComponentName"));
  
  private RADComponent component;
  
  // FINALIZE DEBUG METHOD
  public void finalize () throws Throwable {
    super.finalize ();
    if (System.getProperty ("netbeans.debug.form.finalize") != null) {
      System.out.println("finalized: "+this.getClass ().getName ()+", instance: "+this);
    }
  } // FINALIZE DEBUG METHOD
  
  public RADComponentNode (RADComponent component) {
    super ((component instanceof ComponentContainer) ? new RADChildren ((ComponentContainer)component) : Children.LEAF);
    this.component = component;
    component.setNodeReference (this);
    getCookieSet ().add (this);
    if (component instanceof ComponentContainer) {
      getCookieSet ().add (new ComponentsIndex ());
    }
    updateName ();
  }

  void updateName () {
    Class compClass = component.getComponentClass ();
    if (component instanceof FormContainer) {
      // [PENDING - handle this better]
      setDisplayName (getName () + " [form]");
    } else {
      setDisplayName (nameFormat.format (new Object[] { getName (), compClass.getName (), Utilities.getShortClassName (compClass) } ));
    }
  }
  
  public Image getIcon (int iconType) {
    Image ic = BeanSupport.getBeanIcon (component.getComponentClass (), iconType);
    if (ic != null) return ic;
    else return super.getIcon (iconType);
  }
  
  public Image getOpenedIcon (int iconType) {
    return getIcon (iconType);
  }
  
  public Node.PropertySet[] getPropertySets () {
    return component.getProperties ();
  }

  /** Lazily initialize set of node's actions (overridable).
  * The default implementation returns <code>null</code>.
  * <p><em>Warning:</em> do not call {@link #getActions} within this method.
  * If necessary, call {@link NodeOp#getDefaultActions} to merge in.
  * @return array of actions for this node, or <code>null</code> to use the default node actions
  */
  protected SystemAction [] createActions () {
    ArrayList actions = new ArrayList (15);

    if (component instanceof RADVisualContainer) {
      actions.add (SystemAction.get(SelectLayoutAction.class));
      actions.add (null);
    }
    
    if (component instanceof ComponentContainer) {
      actions.add (SystemAction.get(ReorderAction.class));
      if (!(component instanceof FormContainer)) {
        actions.add (SystemAction.get(MoveUpAction.class));
        actions.add (SystemAction.get(MoveDownAction.class));
      }
      actions.add (null);
      actions.add (SystemAction.get(PasteAction.class));
    } else {
      actions.add (SystemAction.get(MoveUpAction.class));
      actions.add (SystemAction.get(MoveDownAction.class));
      actions.add (null);
    }
     
    actions.add (SystemAction.get(CopyAction.class));
    actions.add (SystemAction.get(CutAction.class));
    actions.add (null);
    if (!(component instanceof FormContainer)) {
      actions.add (SystemAction.get(RenameAction.class));
      actions.add (SystemAction.get(DeleteAction.class));
      actions.add (null);
    }
    actions.add (SystemAction.get(ToolsAction.class));
    actions.add (SystemAction.get(PropertiesAction.class));

    SystemAction[] array = new SystemAction [actions.size ()];
    actions.toArray (array);
    return array;
  }

  /** Set the system name. Fires a property change event.
  * Also may change the display name according to {@link #displayFormat}.
  *
  * @param s the new name
  */
  public String getName () {
    return component.getName ();
  }

    /** Set the system name. Fires a property change event.
  * Also may change the display name according to {@link #displayFormat}.
  *
  * @param s the new name
  */
  public void setName (String s) {
    component.setName (s);
  }

  /** Can this node be renamed?
  * @return <code>false</code>
  */
  public boolean canRename () {
    return !(component instanceof FormContainer);
  }

  /** Can this node be destroyed?
  * @return <CODE>false</CODE>
  */
  public boolean canDestroy () {
    return !(component instanceof FormContainer);
  }

  /** Remove the node from its parent and deletes it.
  * The default
  * implementation obtains write access to
  * the {@link Children#MUTEX children's lock}, and removes
  * the node from its parent (if any). Also fires a property change.
  * <P>
  * This may be overridden by subclasses to do any additional
  * cleanup.
  *
  * @exception IOException if something fails
  */
  public void destroy () throws java.io.IOException {
    component.getFormManager ().deleteComponent (component);
    super.destroy ();
  }

  /** Get a cookie from the node.
  * Uses the cookie set as determined by {@link #getCookieSet}.
  *
  * @param type the representation class
  * @return the cookie or <code>null</code>
  */
  public Node.Cookie getCookie (Class type) {
    if (SaveCookie.class.equals (type)) {
      return component.getFormManager ().getFormObject ().getCookie (SaveCookie.class);
    }
    return super.getCookie (type);
  }
  
// -----------------------------------------------------------------------------------------
// Clipboard operations

  /** Test whether this node can be copied.
  * The default implementation returns <code>true</code>.
  * @return <code>true</code> if it can
  */
  public boolean canCopy () {
    return !(component instanceof FormContainer);
  }

  /** Test whether this node can be cut.
  * The default implementation assumes it can if this node is {@link #writeable}.
  * @return <code>true</code> if it can
  */
  public boolean canCut () {
    return !(component instanceof FormContainer);
  }

  /** Copy this node to the clipboard.
  *
  * @return The transferable for RACComponentNode
  * @throws IOException if it could not copy
  */
  public Transferable clipboardCopy () throws java.io.IOException {
    System.out.println("Copying...");
    return new RADTransferable (RAD_COMPONENT_COPY_FLAVOR, component);
  }

  /** Cut this node to the clipboard.
  *
  * @return {@link ExTransferable.Single} with one flavor, {@link NodeTransfer#nodeCopyFlavor}
  * @throws IOException if it could not cut
  */
  public Transferable clipboardCut () throws java.io.IOException {
    System.out.println("Cutting...");
    final RADComponent comp = component;
    destroy (); // delete node and component from form
    return new RADTransferable (RAD_COMPONENT_CUT_FLAVOR, component);
  }
  /** Accumulate the paste types that this node can handle
  * for a given transferable.
  * <P>
  * The default implementation simply tests whether the transferable supports
  * {@link NodeTransfer#nodePasteFlavor}, and if so, it obtains the paste types
  * from the {@link NodeTransfer.Paste transfer data} and inserts them into the set.
  *
  * @param t a transferable containing clipboard data
  * @param s a list of {@link PasteType}s that will have added to it all types
  *    valid for this node
  */
  protected void createPasteTypes (final Transferable t, java.util.List s) {
    System.out.println("Create paste types: "+t);
    if ((component instanceof RADVisualContainer) && 
        (t.isDataFlavorSupported (RAD_COMPONENT_COPY_FLAVOR) || t.isDataFlavorSupported (RAD_COMPONENT_CUT_FLAVOR))) {
    System.out.println("Create paste types2: "+t);
      PasteType pasteType = new PasteType () {
      
        /** Perform the paste action.
        * @return transferable which should be inserted into the clipboard after the
        *         paste action. It can be <code>null</code>, meaning that the clipboard content
        *         is not affected. Use e.g. {@link ExTransferable#EMPTY} to clear it.
        * @throws IOException if something fails
        */
        public Transferable paste() throws java.io.IOException {
          System.out.println("Paste...");
          try {
            DataFlavor[] flavors = t.getTransferDataFlavors ();
            System.out.println("COPY FLAVOR: "+RAD_COMPONENT_COPY_FLAVOR);
            System.out.println("CUT FLAVOR: "+RAD_COMPONENT_CUT_FLAVOR);
            for (int i = 0; i < flavors.length; i++) {
              System.out.println("Flavor["+i+"] = "+flavors[i]);
            }
            if (t.isDataFlavorSupported (RAD_COMPONENT_CUT_FLAVOR)) {
              System.out.println("CUT SUPPORTED !!!");
              final RADComponent originalComp = (RADComponent)t.getTransferData (RAD_COMPONENT_CUT_FLAVOR);
              originalComp.initialize (component.getFormManager ()); // if pasting into another form
              component.getFormManager ().addVisualComponent ((RADVisualComponent)originalComp, (RADVisualContainer)component, null);
              System.out.println("Original Component: "+originalComp);
              // [PENDING!!! - visual component flavors]
              
              // put copy flavor as the new one, as the first instance was used already
              return new RADTransferable (RAD_COMPONENT_COPY_FLAVOR, originalComp);
            }
            
            if (t.isDataFlavorSupported (RAD_COMPONENT_COPY_FLAVOR)) {
              System.out.println("COPY SUPPORTED !!!");
              RADComponent copyComp = (RADComponent)t.getTransferData (RAD_COMPONENT_COPY_FLAVOR);
              System.out.println("Copy!!!");
              return null;
            }
          } catch (java.awt.datatransfer.UnsupportedFlavorException e) {
            // ignored
          }
          return null;
        }
              
      };
      s.add (pasteType);
    }
  } 

// -----------------------------------------------------------------------------
// RADComponentCookie implementation
  
  public RADComponent getRADComponent () {
    return component;
  }

// -----------------------------------------------------------------------------
// Innerclasses
  
  /** Index support for reordering of file system pool.
  */
  private final class ComponentsIndex extends com.netbeans.ide.nodes.Index.Support {
    
    /** Get the nodes; should be overridden if needed.
    * @return the nodes
    * @throws NotImplementedException always
    */
    public Node[] getNodes () {
      RADComponent[] comps = ((ComponentContainer)getRADComponent ()).getSubBeans ();
      Node[] nodes = new Node[comps.length];
      for (int i = 0; i < comps.length; i++) {
        nodes[i] = comps[i].getNodeReference ();
      }
      return nodes;
    }

    /** Get the node count. Subclasses must provide this.
    * @return the count
    */
    public int getNodesCount () {
      return getNodes ().length;
    }

    /** Reorder by permutation. Subclasses must provide this.
    * @param perm the permutation
    */
    public void reorder (int[] perm) {
      ((ComponentContainer)getRADComponent ()).reorderSubComponents (perm);
      ((RADChildren)getChildren ()).updateKeys ();
    }
  }
  
  static class RADDataFlavor extends DataFlavor {
    RADDataFlavor (Class representationClass, String name) {
      super (representationClass, name);
    }
  }
  
  public static class RADTransferable implements Transferable {
    private RADComponent radComponent;
    private DataFlavor[] flavors;
    
    RADTransferable (DataFlavor flavor, RADComponent radComponent) {
      this (new DataFlavor[] { flavor }, radComponent);
    }
    
    RADTransferable (DataFlavor[] flavors, RADComponent radComponent) {
      this.flavors = flavors;
      this.radComponent = radComponent;
    }
    
    /** Returns an array of DataFlavor objects indicating the flavors the data 
    * can be provided in.  The array should be ordered according to preference
    * for providing the data (from most richly descriptive to least descriptive).
    * @return an array of data flavors in which this data can be transferred
    */
    public DataFlavor[] getTransferDataFlavors() {
      return flavors;
    }
    
    /** Returns whether or not the specified data flavor is supported for
    * this object.
    * @param flavor the requested flavor for the data
    * @return boolean indicating wjether or not the data flavor is supported
    */
    public boolean isDataFlavorSupported(DataFlavor flavor) {
      for (int i = 0; i < flavors.length; i++) {
        if (flavors[i] == flavor) { // comparison based on exact instances, as these are static in this node
          return true;
        }
      }
      return false;
    }
    
    /** Returns an object which represents the data to be transferred.  The class 
    * of the object returned is defined by the representation class of the flavor.
    *
    * @param flavor the requested flavor for the data
    * @see DataFlavor#getRepresentationClass
    * @exception IOException                if the data is no longer available
    *              in the requested flavor.
    * @exception UnsupportedFlavorException if the requested data flavor is
    *              not supported.
    */
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, java.io.IOException {
      if (flavor instanceof RADDataFlavor) {
        return radComponent;
      }
      throw new UnsupportedFlavorException (flavor);
    }
  }
}

/*
 * Log
 *  13   Gandalf   1.12        6/2/99   Ian Formanek    ToolsAction, Reorder
 *  12   Gandalf   1.11        6/1/99   Ian Formanek    Fixed last change
 *  11   Gandalf   1.10        6/1/99   Ian Formanek    Rename implemented 
 *       correctly, actions provided according to component type (Rename, 
 *       Delete, Paste)
 *  10   Gandalf   1.9         5/24/99  Ian Formanek    Non-Visual components
 *  9    Gandalf   1.8         5/20/99  Ian Formanek    
 *  8    Gandalf   1.7         5/16/99  Ian Formanek    No canRename
 *  7    Gandalf   1.6         5/16/99  Ian Formanek    
 *  6    Gandalf   1.5         5/15/99  Ian Formanek    
 *  5    Gandalf   1.4         5/14/99  Ian Formanek    
 *  4    Gandalf   1.3         5/12/99  Ian Formanek    
 *  3    Gandalf   1.2         5/4/99   Ian Formanek    Package change
 *  2    Gandalf   1.1         4/29/99  Ian Formanek    
 *  1    Gandalf   1.0         4/29/99  Ian Formanek    
 * $
 */
