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

package com.netbeans.developer.modules.beans;

import java.awt.datatransfer.Transferable;
import java.beans.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ResourceBundle;

import org.openide.src.*;
import org.openide.src.nodes.*;
import org.openide.nodes.*;
import org.openide.actions.*;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.WeakListener;
import org.openide.util.actions.SystemAction;
import org.openide.TopManager;
import org.openide.NotifyDescriptor;

/** Superclass of nodes representing elements in the source hierarchy.
* <p>Element nodes generally:
* <ul>
* <li>Have an associated icon, according to {@link #resolveIconBase}.
* <li>Have a display name based on the element's properties, using {@link #elementFormat};
* changes to {@link ElementFormat#dependsOnProperty relevant} element properties
* automatically affect the display name.
* <li>Have some node properties (displayable on the property sheet), according to
* the element's properties, and with suitable editors.
* <li>Permit renames and deletes, if a member element and writeable.
* <li>As permitted by the element, and a writable flag in the node,
* permit cut/copy/paste operations, as well as creation of new members.
* </ul>
*
* @author Petr Hrebejk
*/


public abstract class PatternNode extends AbstractNode implements IconBases, PatternProperties, PropertyChangeListener {

  /** Source of the localized human presentable strings. */
  static ResourceBundle bundle = NbBundle.getBundle(PatternNode.class);

  /** Options for the display name format. */
  protected static final SourceOptions sourceOptions = new SourceOptions();

  /** Default return value of getIconAffectingProperties method. */
  private static final String[] ICON_AFFECTING_PROPERTIES = new String[] {
    PROP_MODE
  };

  /** Array of the actions of the java methods, constructors and fields. */
  private static final SystemAction[] DEFAULT_ACTIONS = new SystemAction[] {
    SystemAction.get(OpenAction.class),
    null,
    /*
    SystemAction.get(CutAction.class),
    SystemAction.get(CopyAction.class),
    null,
    */
    SystemAction.get(DeleteAction.class),
    SystemAction.get(RenameAction.class),
    null,
    SystemAction.get(ToolsAction.class),
    SystemAction.get(PropertiesAction.class),
  };


  /** Associated pattern. */
  protected Pattern pattern;

  /** Is this node read-only or are modifications permitted? */
  protected boolean writeable;

  /** Listener to forbid its garbage collection */
  private transient PropertyChangeListener listener;
  
  /** Create a new pattern node.
  *
  * @param element element to represent
  * @param children child nodes
  * @param writeable <code>true</code> if this node should allow modifications.
  *        These include writable properties, clipboard operations, deletions, etc.
  */
  public PatternNode(Pattern pattern, Children children, boolean writeable) {
    super(children);
    this.pattern = pattern;
    this.writeable = writeable;
    setIconBase(resolveIconBase());
    setDefaultAction(SystemAction.get(OpenAction.class));
    setActions(DEFAULT_ACTIONS);

    this.pattern.addPropertyChangeListener(new WeakListener.PropertyChange (this));
    displayFormat = null;
  }

  /* Gets the short description of this node.
  * @return A localized short description associated with this node.
  */
  public String getShortDescription() {
      return super.getShortDescription(); // If not ovewloaded in ancestors 
  }
  
  /** Get the currently appropriate icon base.
  * Subclasses should make this sensitive to the state of the element--for example,
  * a private variable may have a different icon than a public one.
  * The icon will be automatically changed whenever a
  * {@link #getIconAffectingProperties relevant} change is made to the element.
  * @return icon base
  * @see AbstractNode#setIconBase
  */
  abstract protected String resolveIconBase();

  /** Get the names of all element properties which might affect the choice of icon.
  * The default implementation just returns {@link #PROP_MODIFIERS}.
  * @return the property names, from {@link ElementProperties}
  */
  protected String[] getIconAffectingProperties() {
    return ICON_AFFECTING_PROPERTIES;
  }

  public HelpCtx getHelpCtx () {
    return new HelpCtx (PatternNode.class);
  }

  /** Test whether this node can be renamed.
  * The default implementation assumes it can if this node is {@link #writeable}.
  *
  * @return <code>true</code> if this node can be renamed
  */
  public boolean canRename() {
    return writeable;
  }

  /** Test whether this node can be deleted.
  * The default implementation assumes it can if this node is {@link #writeable}.
  *
  * @return <code>true</code> if this node can be renamed
  */
  public boolean canDestroy () {
    return writeable;
  }

  /* Copy this node to the clipboard.
  *
  * @return {@link ExTransferable.Single} with one flavor, {@link NodeTransfer#nodeCopyFlavor}
  * @throws IOException if it could not copy
  */
  public Transferable clipboardCopy () throws IOException {
    //PENDING
    return super.clipboardCopy();
  }

  /* Cut this node to the clipboard.
  *
  * @return {@link ExTransferable.Single} with one flavor, {@link NodeTransfer#nodeCopyFlavor}
  * @throws IOException if it could not cut
  */
  public Transferable clipboardCut () throws IOException {
    if (!writeable)
      throw new IOException();
    
    //PENDING
    return super.clipboardCopy();
  }

  /** Test whether this node can be copied.
  * The default implementation returns <code>true</code>.
  * @return <code>true</code> if it can
  */
  public boolean canCopy () {
    return true;
  }

  /** Test whether this node can be cut.
  * The default implementation assumes it can if this node is {@link #writeable}.
  * @return <code>true</code> if it can
  */
  public boolean canCut () {
    return writeable;
  }

  /** Set all actions for this node.
  * @param actions new list of actions
  */
  public void setActions(SystemAction[] actions) {
    systemActions = actions;
  }

  /** Calls super.fireCookieChange. The reason why is redefined
  * is only to allow the access from this package.
  */
  void superFireCookieChange() {
    fireCookieChange();
  }
  
  /** Get a cookie from this node.
  * First tries the node itself, then {@link Element#getCookie}.
  * Since {@link Element} implements <code>Node.Cookie</code>, it is
  * possible to find the element from a node using code such as:
  * <p><code><pre>
  * Node someNode = ...;
  * MethodElement element = (MethodElement) someNode.getCookie (MethodElement.class);
  * if (element != null) { ... }
  * </pre></code>
  * @param type the cookie class
  * @return the cookie or <code>null</code>
  */
  public Node.Cookie getCookie (Class type) {
    Node.Cookie c = super.getCookie(type);
    if (c == null)
      c = pattern.getCookie(type);

    return c;
  }
  
  /** Test for equality.
  * @return <code>true</code> if the represented {@link Element}s are equal
  */
  public boolean equals (Object o) {
    return (o instanceof PatternNode) && (pattern.equals (((PatternNode)o).pattern));
  }
  
  /** Get a hash code.
  * @return the hash code from the represented {@link Element}
  */
  public int hashCode () {
    return pattern.hashCode ();
  }
  

  /** Called when node name is changed */
  public void superSetName(String name) {
    super.setName( name );
  }

  /** Set's the name of pattern. Must be defined in descendants
  */
  abstract protected void setPatternName(String name) throws SourceException;

  /** Create a node property representing the pattern's name.
  * @param canW if <code>false</code>, property will be read-only
  * @return the property.
  */
  protected Node.Property createNameProperty(boolean canW) {
    return new PatternPropertySupport(PatternProperties.PROP_NAME, String.class, canW) {
      /** Gets the value */
      public Object getValue () {
        return ((Pattern)pattern).getName();
      }

      /** Sets the value */
      public void setValue(Object val) throws IllegalArgumentException,
      IllegalAccessException, InvocationTargetException {
        super.setValue(val);       
        try {
          String str = (String) val;
          pattern.patternAnalyser.setIgnore( true );
          setPatternName( str );
          pattern.patternAnalyser.setIgnore( false );
        }
        catch (SourceException e) {
          throw new InvocationTargetException(e);
        }
        catch (ClassCastException e) {
          throw new IllegalArgumentException();

        }
      }
    };
  }

  /** Called when the node has to be destroyed */
  public void destroy() throws IOException {
    try {
      pattern.destroy();
    }
    catch (SourceException e) {
      throw new IOException(e.getMessage());
    }
    super.destroy();
  }


  // ================== Pattern listener =================================

  public void propertyChange(PropertyChangeEvent evt) {
    setIconBase( resolveIconBase() );
    setName( pattern.getName() );
    firePropertyChange( null, null, null );  
  }

  // ================== Property support for element nodes =================

  /** Property support for element nodes properties.
  */
  static abstract class PatternPropertySupport extends PropertySupport {
    /** Constructs a new ElementProp - support for properties of
    * element hierarchy nodes.
    *
    * @param name The name of the property
    * @param type The class type of the property
    * @param canW The canWrite flag of the property
    */
    public PatternPropertySupport(String name, java.lang.Class type, boolean canW) {
      super(name, type,
            bundle.getString("PROP_" + name),
            bundle.getString("HINT_" + name),
            true, canW);
    }
    
    /** Setter for the value. This implementation only tests
    * if the setting is possible.
    *
    * @param val the value of the property
    * @exception IllegalAccessException when this ElementProp was constructed
    *            like read-only.
    */
   
    public void setValue (Object val) throws IllegalArgumentException,
    IllegalAccessException, InvocationTargetException {
      if (!canWrite())
        throw new IllegalAccessException(bundle.getString("MSG_Cannot_Write"));
    }
   
  }
}
