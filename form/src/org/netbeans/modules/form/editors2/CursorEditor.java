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

package com.netbeans.developer.explorer.propertysheet.editors;

import org.openide.explorer.propertysheet.editors.*;

import java.beans.*;
import java.awt.*;
import java.util.HashMap;
import javax.swing.*;

/** 
 *
 * @author  Pavel Buzek
 * @version 
 */

public class CursorEditor extends PropertyEditorSupport  implements EnhancedPropertyEditor, org.openide.explorer.propertysheet.editors.XMLPropertyEditor {

  private static HashMap CURSOR_TYPES = new HashMap ();
  private static HashMap CURSOR_CONSTANTS = new HashMap ();
  static {
    CURSOR_TYPES.put (new Cursor (Cursor.CROSSHAIR_CURSOR).getName(), new Integer (Cursor.CROSSHAIR_CURSOR));
    CURSOR_TYPES.put (new Cursor (Cursor.DEFAULT_CURSOR).getName(), new Integer (Cursor.DEFAULT_CURSOR));
    CURSOR_TYPES.put (new Cursor (Cursor.E_RESIZE_CURSOR).getName(), new Integer (Cursor.E_RESIZE_CURSOR));
    CURSOR_TYPES.put (new Cursor (Cursor.HAND_CURSOR).getName(), new Integer (Cursor.HAND_CURSOR));
    CURSOR_TYPES.put (new Cursor (Cursor.MOVE_CURSOR).getName(), new Integer (Cursor.MOVE_CURSOR));
    CURSOR_TYPES.put (new Cursor (Cursor.N_RESIZE_CURSOR).getName(), new Integer (Cursor.N_RESIZE_CURSOR));
    CURSOR_TYPES.put (new Cursor (Cursor.NE_RESIZE_CURSOR).getName(), new Integer (Cursor.NE_RESIZE_CURSOR));
    CURSOR_TYPES.put (new Cursor (Cursor.NW_RESIZE_CURSOR).getName(), new Integer (Cursor.NW_RESIZE_CURSOR));
    CURSOR_TYPES.put (new Cursor (Cursor.S_RESIZE_CURSOR).getName(), new Integer (Cursor.S_RESIZE_CURSOR));
    CURSOR_TYPES.put (new Cursor (Cursor.SE_RESIZE_CURSOR).getName(), new Integer (Cursor.SE_RESIZE_CURSOR));
    CURSOR_TYPES.put (new Cursor (Cursor.SW_RESIZE_CURSOR).getName(), new Integer (Cursor.SW_RESIZE_CURSOR));
    CURSOR_TYPES.put (new Cursor (Cursor.TEXT_CURSOR).getName(), new Integer (Cursor.TEXT_CURSOR));
    CURSOR_TYPES.put (new Cursor (Cursor.W_RESIZE_CURSOR).getName(), new Integer (Cursor.W_RESIZE_CURSOR));
    CURSOR_TYPES.put (new Cursor (Cursor.WAIT_CURSOR).getName(), new Integer (Cursor.WAIT_CURSOR));

    CURSOR_CONSTANTS.put (new Integer (Cursor.CROSSHAIR_CURSOR), "java.awt.Cursor.CROSSHAIR_CURSOR");
    CURSOR_CONSTANTS.put (new Integer (Cursor.DEFAULT_CURSOR), "java.awt.Cursor.DEFAULT_CURSOR");
    CURSOR_CONSTANTS.put (new Integer (Cursor.E_RESIZE_CURSOR), "java.awt.Cursor.E_RESIZE_CURSOR");
    CURSOR_CONSTANTS.put (new Integer (Cursor.HAND_CURSOR), "java.awt.Cursor.HAND_CURSOR");
    CURSOR_CONSTANTS.put (new Integer (Cursor.MOVE_CURSOR), "java.awt.Cursor.MOVE_CURSOR");
    CURSOR_CONSTANTS.put (new Integer (Cursor.N_RESIZE_CURSOR), "java.awt.Cursor.N_RESIZE_CURSOR");
    CURSOR_CONSTANTS.put (new Integer (Cursor.NE_RESIZE_CURSOR), "java.awt.Cursor.NE_RESIZE_CURSOR");
    CURSOR_CONSTANTS.put (new Integer (Cursor.NW_RESIZE_CURSOR), "java.awt.Cursor.NW_RESIZE_CURSOR");
    CURSOR_CONSTANTS.put (new Integer (Cursor.S_RESIZE_CURSOR), "java.awt.Cursor.S_RESIZE_CURSOR");
    CURSOR_CONSTANTS.put (new Integer (Cursor.SE_RESIZE_CURSOR), "java.awt.Cursor.SE_RESIZE_CURSOR");
    CURSOR_CONSTANTS.put (new Integer (Cursor.SW_RESIZE_CURSOR), "java.awt.Cursor.SW_RESIZE_CURSOR");
    CURSOR_CONSTANTS.put (new Integer (Cursor.TEXT_CURSOR), "java.awt.Cursor.TEXT_CURSOR");
    CURSOR_CONSTANTS.put (new Integer (Cursor.W_RESIZE_CURSOR), "java.awt.Cursor.W_RESIZE_CURSOR");
    CURSOR_CONSTANTS.put (new Integer (Cursor.WAIT_CURSOR), "java.awt.Cursor.WAIT_CURSOR");
  }
  
  Cursor current;
  
  /** Creates new CursorEditor */
  public CursorEditor() {
    current = new Cursor (Cursor.DEFAULT_CURSOR);
  }
  
  public Object getValue () {
    return current;
  }

  public void setValue (Object value) {
    if (value == null) return;
    if ( value instanceof Cursor) {
      current = (Cursor) value;
      firePropertyChange();
    } else {
      throw new IllegalArgumentException();
    }
  }

  public String getAsText () {
    if (current == null)
      return "null";
    else
      return current.getName();
  }

  public void setAsText (String string) {
    Object o = CURSOR_TYPES.get(string);
    if (o != null) {
      int type = ((Integer) o).intValue ();
      setValue (new Cursor (type));
    }
  }

  public boolean supportsEditingTaggedValues () {
    return true;
  }

  public String[] getTags () {
    String [] tags = new String[CURSOR_TYPES.size()];
    int i=0;
    for (java.util.Iterator iter = CURSOR_TYPES.keySet().iterator(); iter.hasNext(); i++)
      tags [i] = (String) iter.next();
    return tags;
  }
  
  public boolean hasInPlaceCustomEditor () {
    return false;
  }
  
  public Component getInPlaceCustomEditor () {
    return null;
  }
  
  public boolean supportsCustomEditor () {
    return true;
  }

  public Component getCustomEditor () {
    return new CursorPanel (current);
  } 

  public String getJavaInitializationString () {
    if (current == null) return null; // no code to generate
    String cursorName = (String) CURSOR_CONSTANTS.get (new Integer (current.getType()));
    if (cursorName != null)
      return "new java.awt.Cursor ("+cursorName+")";
    return "new java.awt.Cursor ("+current.getType()+")";
  }

  class CursorPanel extends JPanel implements EnhancedCustomPropertyEditor {
    CursorPanel (Cursor value) {
      setLayout (new java.awt.GridBagLayout ());
      java.awt.GridBagConstraints gridBagConstraints1;
      list = new JList (new java.util.Vector (CURSOR_TYPES.keySet()));
      list.setSelectionMode (javax.swing.ListSelectionModel.SINGLE_SELECTION);
      if (value != null) 
        list.setSelectedValue(value.getName(), true);
      
      gridBagConstraints1 = new java.awt.GridBagConstraints ();
      gridBagConstraints1.gridx = 0;
      gridBagConstraints1.gridy = 1;
      gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
      gridBagConstraints1.insets = new java.awt.Insets (8, 8, 8, 8);
      gridBagConstraints1.weightx = 1.0;
      gridBagConstraints1.weighty = 1.0;
      add (list, gridBagConstraints1);
    
      gridBagConstraints1 = new java.awt.GridBagConstraints ();
      gridBagConstraints1.gridx = 0;
      gridBagConstraints1.gridy = 0;
      gridBagConstraints1.insets = new java.awt.Insets (8, 8, 0, 8);
      gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
      add (new JLabel ("Select Cursor Name"), gridBagConstraints1);
    }
    
    public Object getPropertyValue () throws IllegalStateException {
      if (list.getSelectedValue()==null) return null;
      int type = ((Integer) CURSOR_TYPES.get(list.getSelectedValue())).intValue ();
      return new Cursor (type);
    }

    private JList list;
  }
  
//--------------------------------------------------------------------------
// XMLPropertyEditor implementation

  public static final String XML_CURSOR = "Color";

  public static final String ATTR_ID = "id";

  /** Called to load property value from specified XML subtree. If succesfully loaded, 
  * the value should be available via the getValue method.
  * An IOException should be thrown when the value cannot be restored from the specified XML element
  * @param element the XML DOM element representing a subtree of XML from which the value should be loaded
  * @exception IOException thrown when the value cannot be restored from the specified XML element
  */
  public void readFromXML (org.w3c.dom.Node element) throws java.io.IOException {
    if (!XML_CURSOR.equals (element.getNodeName ())) {
      throw new java.io.IOException ();
    }
    org.w3c.dom.NamedNodeMap attributes = element.getAttributes ();
    try {
      String id = attributes.getNamedItem (ATTR_ID).getNodeValue ();
      setAsText (id);
    } catch (NullPointerException e) {
      throw new java.io.IOException ();
    }
  }
  
  /** Called to store current property value into XML subtree. The property value should be set using the
  * setValue method prior to calling this method.
  * @param doc The XML document to store the XML in - should be used for creating nodes only
  * @return the XML DOM element representing a subtree of XML from which the value should be loaded
  */
  public org.w3c.dom.Node storeToXML(org.w3c.dom.Document doc) {
    org.w3c.dom.Element el = doc.createElement (XML_CURSOR);
    el.setAttribute (ATTR_ID, getAsText ());
    return el;
  }
  
}

/*
 * Log
 *  3    Gandalf   1.2         12/17/99 Pavel Buzek     support for saving into 
 *       XML
 *  2    Gandalf   1.1         12/17/99 Pavel Buzek     
 *  1    Gandalf   1.0         12/17/99 Pavel Buzek     
 * $
 */
