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

import org.openide.TopManager;
import org.openide.actions.*;
import org.openide.cookies.*;
import org.openide.loaders.InstanceSupport;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.datatransfer.NewType;
import com.netbeans.developer.modules.loaders.form.actions.*;

import java.awt.Image;
import java.awt.datatransfer.*;
import java.text.MessageFormat;
import java.util.ArrayList;

/** 
*
* @author Ian Formanek
*/
public class RADComponentNode extends AbstractNode implements RADComponentCookie, FormCookie {

  public static DataFlavor RAD_COMPONENT_COPY_FLAVOR = new RADDataFlavor (
    RADComponentNode.class,
    "RAD_COMPONENT_COPY_FLAVOR"
  );
  public static DataFlavor RAD_COMPONENT_CUT_FLAVOR = new RADDataFlavor (
    RADComponentNode.class,
    "RAD_COMPONENT_CUT_FLAVOR"
  );


  private final static MessageFormat nameFormat = new MessageFormat (NbBundle.getBundle (RADComponentNode.class).getString ("FMT_ComponentName"));
  private final static MessageFormat formNameFormat = new MessageFormat (NbBundle.getBundle (RADComponentNode.class).getString ("FMT_FormName"));
  
  private RADComponent component;
  private RADComponentInstance radComponentInstance;
  
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
    radComponentInstance = new RADComponentInstance (component);
    component.setNodeReference (this);
    getCookieSet ().add (this);
    if (component instanceof ComponentContainer) {
      getCookieSet ().add (new ComponentsIndex ());
    }
    updateName ();
  }

  void updateName () {
    Class compClass = component.getBeanClass ();
    if (component instanceof FormContainer) {
      Class formClass = ((FormContainer)component).getFormInfo ().getFormInstance ().getClass ();
      setDisplayName (formNameFormat.format (new Object[] {component.getFormManager ().getFormObject ().getName () , formClass.getName (), Utilities.getShortClassName (formClass) } ));
    } else {
      setDisplayName (nameFormat.format (new Object[] { getName (), compClass.getName (), Utilities.getShortClassName (compClass) } ));
    }
  }
  
  void notifyPropertiesChange () {
    firePropertyChange (null, null, null);
  }

  public Image getIcon (int iconType) {
    Image ic = BeanSupport.getBeanIcon (component.getBeanClass (), iconType);
    if (ic != null) return ic;
    else return super.getIcon (iconType);
  }
  
  public Image getOpenedIcon (int iconType) {
    return getIcon (iconType);
  }

  public HelpCtx getHelpCtx () {
    HelpCtx help = InstanceSupport.findHelp (new InstanceCookie () {
      public Object instanceCreate () {
        return component.getBeanInstance ();
      }
      public String instanceName () {
        return component.getName ();
      }
      public Class instanceClass () {
        return component.getBeanClass ();
      }
    });
    if (help != null)
      return help;
    else
      return new HelpCtx (RADComponentNode.class);
  }
  
  public Node.PropertySet[] getPropertySets () {
    return component.getProperties ();
  }

  /* List new types that can be created in this node.
  * @return new types
  */
  public NewType[] getNewTypes () {
    return component.getNewTypes ();
  }

  /** Get the default action for this node.
  * This action can but need not be one from the list returned
  * from {@link #getActions}. If so, the popup menu returned from {@link #getContextMenu}
  * is encouraged to highlight the action.
  *
  * @return default action, or <code>null</code> if there should be none
  */
  public SystemAction getDefaultAction () {
    if (component.getEventsList ().getDefaultEvent () != null)
      return SystemAction.get (DefaultRADAction.class);
    else
      return null;
  }

  /** Lazily initialize set of node's actions (overridable).
  * The default implementation returns <code>null</code>.
  * <p><em>Warning:</em> do not call {@link #getActions} within this method.
  * If necessary, call {@link NodeOp#getDefaultActions} to merge in.
  * @return array of actions for this node, or <code>null</code> to use the default node actions
  */
  protected SystemAction [] createActions () {
    ArrayList actions = new ArrayList (15);

    actions.add (SystemAction.get(EventsAction.class));
    actions.add (null);
    if (component instanceof RADVisualContainer) {
      actions.add (SystemAction.get(SelectLayoutAction.class));
      actions.add (SystemAction.get(CustomizeLayoutAction.class));
      actions.add (null);
    }
    
    if (component instanceof ComponentContainer) {
      actions.add (SystemAction.get(ReorderAction.class));
      if (!(component instanceof FormContainer)) {
        actions.add (SystemAction.get(MoveUpAction.class));
        actions.add (SystemAction.get(MoveDownAction.class));
      }
      actions.add (null);
      actions.add (SystemAction.get(GotoFormAction.class));
      actions.add (SystemAction.get(GotoEditorAction.class));
      actions.add (SystemAction.get(GotoInspectorAction.class));
      actions.add (null);
      actions.add (SystemAction.get(CutAction.class));
      actions.add (SystemAction.get(CopyAction.class));
      actions.add (SystemAction.get(PasteAction.class));
    } else {
      actions.add (SystemAction.get(GotoFormAction.class));
      actions.add (SystemAction.get(GotoEditorAction.class));
      actions.add (SystemAction.get(GotoInspectorAction.class));
      actions.add (null);
      actions.add (SystemAction.get(MoveUpAction.class));
      actions.add (SystemAction.get(MoveDownAction.class));
      actions.add (null);
      actions.add (SystemAction.get(CutAction.class));
      actions.add (SystemAction.get(CopyAction.class));
    }
     
    actions.add (null);
    if (!(component instanceof FormContainer)) {
      actions.add (SystemAction.get(RenameAction.class));
      actions.add (SystemAction.get(DeleteAction.class));
      actions.add (null);
    }
    if (getNewTypes ().length != 0) {
      actions.add (SystemAction.get(NewAction.class));
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
    if (InstanceCookie.class.equals (type)) {
      return radComponentInstance;
    }
    Node.Cookie inh = super.getCookie (type);
    if (inh == null) {
      if (CompilerCookie.class.isAssignableFrom (type) || 
          SaveCookie.class.isAssignableFrom (type) || 
          ExecCookie.class.isAssignableFrom (type) || 
          DebuggerCookie.class.isAssignableFrom (type) || 
          CloseCookie.class.isAssignableFrom (type) || 
          ArgumentsCookie.class.isAssignableFrom (type) || 
          PrintCookie.class.isAssignableFrom (type)) {
        return component.getFormManager ().getFormObject ().getCookie (type);
       }
    } 
    return inh;
  }
  
  /** Test whether there is a customizer for this node. If true,
  * the customizer can be obtained via {@link #getCustomizer}.
  *
  * @return <CODE>true</CODE> if there is a customizer
  */
  public boolean hasCustomizer () {
    return component.getBeanInfo ().getBeanDescriptor ().getCustomizerClass () != null;
  }

  /** Get the customizer component.
  * @return the component, or <CODE>null</CODE> if there is no customizer
  */
  public java.awt.Component getCustomizer () {
    Class customizerClass = component.getBeanInfo ().getBeanDescriptor ().getCustomizerClass ();
    if (customizerClass == null) return null;
    Object customizer;
    try {
      customizer = customizerClass.newInstance ();
    } catch (InstantiationException e) {
      return null;
    } catch (IllegalAccessException e) {
      return null;
    }
    if (!(customizer instanceof java.awt.Component) ||
        !(customizer instanceof java.beans.Customizer)) return null;

    if (customizer instanceof FormAwareEditor) {
      ((FormAwareEditor)customizer).setRADComponent (component);
    }
    if (customizer instanceof org.openide.explorer.propertysheet.editors.NodeCustomizer) {
      ((org.openide.explorer.propertysheet.editors.NodeCustomizer)customizer).attach (component.getNodeReference ());
    }

    ((java.beans.Customizer)customizer).setObject (component.getBeanInstance ());
    // [PENDING - in X2 there is some strange addPropertyChangeListener code here...]
    return (java.awt.Component)customizer;
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
    return new RADTransferable (RAD_COMPONENT_COPY_FLAVOR, component);
  }

  /** Cut this node to the clipboard.
  *
  * @return {@link ExTransferable.Single} with one flavor, {@link NodeTransfer#nodeCopyFlavor}
  * @throws IOException if it could not cut
  */
  public Transferable clipboardCut () throws java.io.IOException {
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
  protected void createPasteTypes (Transferable t, java.util.List s) {
    boolean isRADComponentFlavor = false;

    if ((component instanceof RADVisualContainer) && 
      (t.isDataFlavorSupported (RAD_COMPONENT_COPY_FLAVOR) || t.isDataFlavorSupported (RAD_COMPONENT_CUT_FLAVOR))) {
      s.add (new RADPaste (t));
      isRADComponentFlavor = true;
    }

    // if there is not a RADComponent in the clipboard, try if it is not InstanceCookie
    if (!isRADComponentFlavor) {
      InstanceCookie ic = (InstanceCookie)NodeTransfer.cookie (t, NodeTransfer.COPY, InstanceCookie.class);
      if (ic != null) {
        s.add (new InstancePaste (ic));        
      }
    }
  } 

// -----------------------------------------------------------------------------
// RADComponentCookie implementation
  
  public RADComponent getRADComponent () {
    return component;
  }

// -------------------------------------------------------------------------------
// FormCookie implementation

  /** Focuses the source editor */
  public void gotoEditor() {
    component.getFormManager ().getFormEditorSupport ().gotoEditor ();
  }

  /** Focuses the form */
  public void gotoForm() {
    component.getFormManager ().getFormEditorSupport ().gotoForm ();
  }

// -----------------------------------------------------------------------------
// Innerclasses
  
  /** Index support for reordering of file system pool.
  */
  private final class ComponentsIndex extends org.openide.nodes.Index.Support {
    
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

// -----------------------------------------------------------------------------
// Paste types

  /** Paste types for data objects.
  */
  private final class RADPaste extends PasteType {
    private Transferable transferable;

    public RADPaste (Transferable t) {
      this.transferable = t;
    }

    public Transferable paste() throws java.io.IOException {

      boolean fromCut = ! (transferable.isDataFlavorSupported (RAD_COMPONENT_COPY_FLAVOR));
      RADComponent radComponent;
      try {
        if (fromCut) {
          radComponent = (RADComponent)transferable.getTransferData (RAD_COMPONENT_CUT_FLAVOR);
        } else {
          radComponent = (RADComponent)transferable.getTransferData (RAD_COMPONENT_COPY_FLAVOR);
        }
      } catch (java.io.IOException e) {
        return null; // ignore
      } catch (UnsupportedFlavorException e) {
        return null; // ignore
      }
      System.out.println ("RADPaste.paste() : fromCut: "+fromCut+", : "+radComponent);


      // 1. pasting copy of RADComponent
      if (!fromCut) {
        FormManager2 pasteManager = component.getFormManager ();
        RADComponent copyComponent;
        if (radComponent instanceof RADVisualContainer) {
          copyComponent = new RADVisualContainer ();
        } else if (radComponent instanceof RADVisualComponent) {
          copyComponent = new RADVisualComponent ();
        } else {
          copyComponent = new RADComponent ();
        }
        copyComponent.initialize (pasteManager);
        copyComponent.setComponent (radComponent.getBeanClass ());
//        copyComponent.setName
//          formManager2.getVariablesPool ().createVariable (nodes[i].componentName, nodes[i].beanClass);
        if (copyComponent instanceof RADVisualContainer) {
// [PENDING]  ((RADVisualContainer)copyComponent).setDesignLayout (((RADContainerNode)nodes[i]).designLayout);
        }
        if (copyComponent instanceof RADVisualComponent) {
          // [PENDING - how about adding visual container to non-visuals?]
          pasteManager.addVisualComponent ((RADVisualComponent)copyComponent, (RADVisualContainer)component, null);
          pasteManager.getFormTopComponent ().validate();
          pasteManager.fireFormChange ();
        } else {
          pasteManager.addNonVisualComponent (copyComponent, null);
          pasteManager.fireFormChange ();
        }
        return null;
      } else {        
      // 2. pasting cut RADComponent (same instance)
        FormManager2 pasteManager = component.getFormManager ();
        radComponent.initialize (pasteManager); // if pasting into another form
        if (radComponent instanceof RADVisualComponent) {
          pasteManager.addVisualComponent ((RADVisualComponent)radComponent, (RADVisualContainer)component, null);
          pasteManager.getFormTopComponent ().validate();
          pasteManager.fireFormChange ();
        } else {
          pasteManager.addNonVisualComponent (radComponent, null);
          pasteManager.fireFormChange ();
        }
        
        // put copy flavor as the new one, as the first instance was used already
        return new RADTransferable (RAD_COMPONENT_COPY_FLAVOR, radComponent);
      }
    }
  }

  /** Paste type for InstanceCookie
  */
  private final class InstancePaste extends PasteType {
    private InstanceCookie cookie;

    /** 
    * @param obj object to work with
    */
    public InstancePaste (InstanceCookie cookie) {
      this.cookie = cookie;
    }

    /** Paste.
    */
    public final Transferable paste () throws java.io.IOException {
      String name = cookie.instanceName ();
      // [PENDING]
      Class instanceClass = null;
      try {
        instanceClass = cookie.instanceClass ();
      } catch (ClassNotFoundException e) {
        TopManager.getDefault ().notifyException (e); // [PENDING - better notification]
      }
      if (instanceClass == null) {
        return null;
      }

      if (java.awt.Component.class.isAssignableFrom (instanceClass)) {
        RADVisualComponent newRADVisualComponent;

//        DesignLayout dl = FormEditor.findDesignLayout (instanceClass);
/*        if (addItem.isContainer() && (dl != null)) {
          newRADVisualComponent = new RADVisualContainer();
          newRADVisualComponent.initialize (FormManager2.this);
          newRADVisualComponent.setComponent (instanceClass); // [PENDING - how about serialized prototypes and using createInstance on the PaletteItem]
          ((RADVisualContainer)newRADVisualComponent).initSubComponents (new RADComponent[0]);
          ((RADVisualContainer)newRADVisualComponent).setDesignLayout (dl);
        }
        else { */
          newRADVisualComponent = new RADVisualComponent();
          newRADVisualComponent.initialize (component.getFormManager ());
          newRADVisualComponent.setComponent (instanceClass); // [PENDING - how about serialized prototypes and using createInstance on the PaletteItem]
//        }
          
        component.getFormManager ().addVisualComponent (newRADVisualComponent, (RADVisualContainer)component, null);

        // for some components, we initialize their properties with some non-default values
        // e.g. a label on buttons, checkboxes
        FormEditor.defaultComponentInit (newRADVisualComponent);
        component.getFormManager ().selectComponent (newRADVisualComponent, false);
        component.getFormManager ().getFormTopComponent ().validate();
        component.getFormManager ().fireFormChange ();
      }
      // preserve clipboard
      return null;
    }
    
  }
/*
        private static void convertComponent (RADNode node, RADComponent comp) {
          Map origChanged = node.changedValues;
          BeanInfo bi = comp.getBeanInfo ();
          PropertyDescriptor[] pds = bi.getPropertyDescriptors ();
          for (Iterator it = origChanged.keySet ().iterator (); it.hasNext (); ) {
            Object key = it.next ();
            for (int i = 0; i < pds.length; i++) {
              if (key.equals (pds[i].getName ())) {
                try {
                  comp.restorePropertyValue (pds[i], origChanged.get (key));
                } catch (IllegalArgumentException e) {
                  // [PENDING]
                } catch (IllegalAccessException e) {
                  // [PENDING]
                } catch (java.lang.reflect.InvocationTargetException e) {
                  // [PENDING]
                }
                break;
              }
            }
          }

          Hashtable eventHandlers = node.eventHandlers;
          comp.initDeserializedEvents (eventHandlers);

          // process constraints on visual components
          if (node instanceof RADVisualNode) {
            HashMap map = ((RADVisualNode)node).constraints;
            ((RADVisualComponent)comp).initConstraints (map);
          }
        }
*/
}

/*
 * Log
 *  32   Gandalf   1.31        8/1/99   Ian Formanek    NodeCustomizer and 
 *       FormAwareEditor employed in getCustomizer
 *  31   Gandalf   1.30        7/30/99  Ian Formanek    Flavors are public
 *  30   Gandalf   1.29        7/28/99  Ian Formanek    Fixed bug 2890 - Go to 
 *       Source and Go to Form actions are not enabled in a context menu of a 
 *       component
 *  29   Gandalf   1.28        7/28/99  Ian Formanek    Provides InstanceCookie
 *  28   Gandalf   1.27        7/28/99  Ian Formanek    Formatting of top-level 
 *       form node name
 *  27   Gandalf   1.26        7/25/99  Ian Formanek    Fixed bug with too many 
 *       tools actions (namely those on DataObject.class) being enabled on the 
 *       node
 *  26   Gandalf   1.25        7/20/99  Jesse Glick     Context help.
 *  25   Gandalf   1.24        7/19/99  Ian Formanek    paste copy, paste 
 *       instance
 *  24   Gandalf   1.23        7/16/99  Ian Formanek    default action
 *  23   Gandalf   1.22        7/14/99  Ian Formanek    Fixed bug 1830 - Layout 
 *       panel is not synchronized with Form Window
 *  22   Gandalf   1.21        7/5/99   Ian Formanek    provides NewTypes and 
 *       New action
 *  21   Gandalf   1.20        7/5/99   Ian Formanek    getComponentInstance->getBeanInstance,
 *        getComponentClass->getBeanClass
 *  20   Gandalf   1.19        7/5/99   Ian Formanek    CustomizeLayout action
 *  19   Gandalf   1.18        7/2/99   Ian Formanek    GotoInspectorAction 
 *       employed in popup menu
 *  18   Gandalf   1.17        6/27/99  Ian Formanek    Many form actions 
 *       (compile, save, ...) are now enabled on form and component inspector, 
 *       EventsAction, Goto actions employed
 *  17   Gandalf   1.16        6/10/99  Ian Formanek    Removed debug prints, 
 *       copy disabled
 *  16   Gandalf   1.15        6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  15   Gandalf   1.14        6/6/99   Ian Formanek    Cusomizer support added
 *  14   Gandalf   1.13        6/3/99   Ian Formanek    
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
