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

import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.io.*;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.openide.explorer.propertysheet.editors.XMLPropertyEditor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.util.Utilities;
import org.openide.TopManager;

import com.netbeans.developerx.loaders.form.formeditor.layouts.*;
import com.netbeans.developer.modules.loaders.form.forminfo.*;

/** 
*
* @author Ian Formanek
*/
public class GandalfPersistenceManager extends PersistenceManager {
  public static final String CURRENT_VERSION = "1.0";
  
  public static final String XML_FORM = "Form";
  public static final String XML_NON_VISUAL_COMPONENTS = "NonVisualComponents";
  public static final String XML_CONTAINER = "Container";
  public static final String XML_COMPONENT = "Component";
  public static final String XML_LAYOUT = "Layout";
  public static final String XML_CONSTRAINTS = "Constraints";
  public static final String XML_CONSTRAINT = "Constraint";
  public static final String XML_SUB_COMPONENTS = "SubComponents";
  public static final String XML_EVENTS = "Events";
  public static final String XML_EVENT = "EventHandler";
  public static final String XML_PROPERTIES = "Properties";
  public static final String XML_PROPERTY = "Property";
  public static final String XML_AUX_VALUES = "AuxValues";
  public static final String XML_AUX_VALUE = "AuxValue";
  
  public static final String ATTR_FORM_VERSION = "version";
  public static final String ATTR_FORM_TYPE = "version";
  public static final String ATTR_COMPONENT_NAME = "name";
  public static final String ATTR_COMPONENT_CLASS = "class";
  public static final String ATTR_PROPERTY_NAME = "name";
  public static final String ATTR_PROPERTY_TYPE = "type";
  public static final String ATTR_PROPERTY_EDITOR = "editor";
  public static final String ATTR_PROPERTY_VALUE_TYPE = "valuetype";
  public static final String ATTR_PROPERTY_VALUE = "value";
  public static final String ATTR_EVENT_NAME = "event";
  public static final String ATTR_EVENT_HANDLER = "handler";
  public static final String ATTR_AUX_NAME = "name";
  public static final String ATTR_AUX_VALUE = "value";
  public static final String ATTR_CONSTRAINT_LAYOUT = "layoutClass";
  public static final String ATTR_CONSTRAINT_VALUE = "value";

  public static final String VALUE_RAD_CONNECTION = "RADConnection";
  
  private static final String ONE_INDENT =  "  ";
  
  /** A method which allows the persistence manager to provide infotrmation on whether
  * is is capable to store info about advanced features provided from Developer 3.0 
  * - all persistence managers except the one providing backward compatibility with 
  * Developer 2.X should return true from this method.
  * @return true if this PersistenceManager is capable to store advanced form features, false otherwise
  */
  public boolean supportsAdvancedFeatures () {
    return true;
  }

  /** A method which allows the persistence manager to check whether it can read
  * given form format.
  * @return true if this PersistenceManager can load form stored in the specified form, false otherwise
  * @exception IOException if any problem occured when accessing the form
  */
  public boolean canLoadForm (FormDataObject formObject) throws IOException {
    FileObject formFile = formObject.getFormEntry ().getFile ();
    try {
      org.w3c.dom.Document doc = org.openide.loaders.XMLDataObject.parse (formFile.getURL ());
    } catch (IOException e) { // [PENDING - just test whether it is an XML file and in this case return false
      return false;
    }
    return true;
  }

  /** Called to actually load the form stored in specified formObject.
  * @param formObject the FormDataObject which represents the form files
  * @return the FormManager2 representing the loaded form or null if some problem occured
  * @exception IOException if any problem occured when loading the form
  */
  public FormManager2 loadForm (FormDataObject formObject) throws IOException {
/*    FileObject formFile = formObject.getFormEntry ().getFile ();
    org.w3c.dom.Document doc = org.openide.loaders.XMLDataObject.parse (formFile.getURL ());
    org.w3c.dom.Element mainElement = doc.getDocumentElement ();
    walkTree (mainElement, "");
// A. Do various checks

  // 1. check the top-level element name
    if (!XML_FORM.equals (mainElement.getTagName ())) {
      throw new IOException (); // [PENDING]
    }

  // 2. check the form version
    if (!CURRENT_VERSION.equals (mainElement.getAttribute (ATTR_FORM_VERSION))) {
      throw new IOException (); // [PENDING - better version checking]
    }
    String infoClass = mainElement.getAttribute (ATTR_FORM_TYPE);
    FormInfo formInfo = null;
    if (infoClass == null) {
      return null;
    }
    try {
      formInfo = (FormInfo)TopManager.getDefault ().systemClassLoader ().loadClass (infoClass).newInstance ();
    } catch (Exception e) {
      // [PENDING - notify problem]
      return null;
    }

    RADForm radForm = new RADForm (formInfo);
    FormManager2 formManager2 = new FormManager2 (formObject, radForm);
    RADVisualContainer topComp = (RADVisualContainer)radForm.getTopLevelComponent (); // [PENDING - illegal cast]
    
// B. process top-element's subnodes (all required)

    org.w3c.dom.NodeList childNodes = mainElement.getChildNodes ();   
    if (childNodes == null) {
      // [PENDING - notify problem]
      return null;
    }

    processNonVisuals (mainElement, formManager2);
    processContainer (mainElement, formManager2, topComp, null);

    return formManager2;*/
return null;
  }

/*  private boolean processNonVisuals (org.w3c.dom.Node node, FormManager2 formManager2) {
    org.w3c.dom.Node nonVisualsNode = findNode (node, XML_NON_VISUAL_COMPONENTS);
    org.w3c.dom.NodeList childNodes = (nonVisualsNode == null) ? null : nonVisualsNode.getChildNodes ();
    ArrayList list = new ArrayList ();
    if (childNodes != null) {
      for (int i = 0; i < childNodes.length (); i++) {
        if (childNodes.item (i).getNodeType () == org.w3c.dom.Node.TEXT_NODE) return true; // ignore text nodes
        if (XML_COMPONENT.equals (childNodes.item (i).getNodeName ())) {
          RADComponent comp = new RADComponent ();
          if (processComponent (childNodes.item (i), formManager2, comp, null)) {
            list.add (comp);
          }
        } else if (XML_CONTAINER.equals (childNodes.item (i).getNodeName ())) {
          RADContainer cont = new RADContainer ();
          if (processContainer (childNodes.item (i), formManager2, cont, null)) {
            list.add (cont);
          }
        }
      }
    }

    RADComponent[] nonVisualsComps = new RADComponent[list.size ()];
    list.copyInto (nonVisualComps);
    formManager2.initNonVisualComponents (nonVisualsComps);
  }

  private boolean processComponent (org.w3c.dom.Node node, FormManager2 formManager2, RADComponent comp, ComponentContainer parentContainer) {
    comp.initialize (formManager2);
    NamedNodeMap attributes = node.getAttributes ();
    String className = attributes.getNamedItem (ATTR_COMPONENT_CLASS);
    Class compClass = null;
    try {
      compClass = TopManager.getDefault ().systemClassLoader ().loadClass (className);
    } catch (Exception e) {
    }
    String compName = attributes.getNamedItem (ATTR_COMPONENT_NAME);
    comp.setComponent (compClass);
    comp.setName (compName);
    formManager2.getVariablesPool ().createVariable (compName, compClass);
    //convertComponent (node, nonVisualsComps[i]);
    return true;
  }

  private boolean processContainer (org.w3c.dom.Node node, FormManager2 formManager2, RADComponent comp, ComponentContainer parentContainer) {
    processComponent (comp);

    if (comp instanceof RADVisualComponent) {
      processVisualComponent (comp);
    }

    if (comp instanceof ComponentsContainer) {
      org.w3c.dom.Node subCompsNode = findSubNode (node, XML_SUB_COMPONENTS);
      
    }

    if (comp instanceof RADVisualContainer) {
      org.w3c.dom.Node layoutNode = findSubNode (node, XML_LAYOUT);
    }
  }

  private boolean processNode (org.w3c.dom.Node node, RADComponent component) {
    if (node.getNodeType () == org.w3c.dom.Node.TEXT_NODE) return true; // ignore text nodes
    if (XML_NON_VISUAL_COMPONENTS.equals (node.getNodeName ())) {
      nonVisualsPresent = true;
      org.w3c.dom.NodeList childNodes = node.getChildNodes ();
      if (childNodes == null) {
        return true; // no nonvisual components
      }
      loadNonVisual (node);
    } else if (false) { //[PENDING]
    }
  }

  private void walkTree (org.w3c.dom.Node node, String indent) {
    if (node.getNodeType () == org.w3c.dom.Node.TEXT_NODE) return; // ignore text nodes
    System.out.println (indent + node.getNodeName ());
    org.w3c.dom.NamedNodeMap attrs = node.getAttributes ();
    if (attrs != null) {
      for (int i = 0; i < attrs.getLength (); i++) {
        org.w3c.dom.Node attr = attrs.item(i);
        System.out.println (indent + "  Attribute: "+ attr.getNodeName ()+", value: "+attr.getNodeValue ());
      }
    }

    org.w3c.dom.NodeList children = node.getChildNodes ();
    if (children != null) {
      for (int i = 0; i < children.getLength (); i++) {
        walkTree (children.item (i), indent + "  ");
      }
    }
  }

  /** Called to actually save the form represented by specified FormManager2 into specified formObject.
  * @param formObject the FormDataObject which represents the form files
  * @param manager the FormManager2 representing the form to be saved
  * @exception IOException if any problem occured when saving the form
  */
  public void saveForm (FormDataObject formObject, FormManager2 manager) throws IOException {
    FileObject formFile = formObject.getFormEntry ().getFile ();
    FileLock lock = null;
    java.io.OutputStream os = null;
    try {
      lock = formFile.lock ();
      StringBuffer buf = new StringBuffer ();
      
      // 1.store XML file header
      buf.append ("<?xml version=\"1.0\"?>\n");
      buf.append ("\n");
      
      // 2.store Form element
      addElementOpenAttr (
         buf, 
         XML_FORM, 
         new String[] { ATTR_FORM_VERSION, ATTR_FORM_TYPE }, 
         new String[] { CURRENT_VERSION, manager.getRADForm ().getFormInfo ().getClass ().getName () }
      );

      // 3.store Non-Visual Components
      buf.append (ONE_INDENT); addElementOpen (buf, XML_NON_VISUAL_COMPONENTS);
      RADComponent[] nonVisuals = manager.getNonVisualComponents ();
      for (int i = 0; i < nonVisuals.length; i++) {
        if (nonVisuals[i] instanceof ComponentContainer) {
          buf.append (ONE_INDENT + ONE_INDENT); 
          addElementOpenAttr (
              buf, 
              XML_CONTAINER, 
              new String[] { ATTR_COMPONENT_CLASS, ATTR_COMPONENT_NAME }, 
              new String[] { nonVisuals[i].getBeanClass ().getName (), nonVisuals[i].getName () }
          );
          saveContainer ((ComponentContainer)nonVisuals[i], buf, ONE_INDENT + ONE_INDENT + ONE_INDENT);
          buf.append (ONE_INDENT + ONE_INDENT); addElementClose (buf, XML_CONTAINER);
        } else {
          buf.append (ONE_INDENT + ONE_INDENT); 
          addElementOpenAttr (
              buf, 
              XML_COMPONENT, 
              new String[] { ATTR_COMPONENT_CLASS, ATTR_COMPONENT_NAME }, 
              new String[] { nonVisuals[i].getBeanClass ().getName (), nonVisuals[i].getName () }
          );
          saveComponent (nonVisuals[i], buf, ONE_INDENT + ONE_INDENT + ONE_INDENT);
          buf.append (ONE_INDENT + ONE_INDENT); addElementClose (buf, XML_COMPONENT);
        }
      }  
      buf.append (ONE_INDENT); addElementClose (buf, XML_NON_VISUAL_COMPONENTS);

      // 4.store form and its visual components hierarchy
      buf.append ("\n");
      saveContainer ((ComponentContainer)manager.getRADForm ().getTopLevelComponent (), buf, ONE_INDENT);
      addElementClose (buf, XML_FORM);
      
      os = formFile.getOutputStream (lock); // [PENDING - first save to ByteArray for safety]
      os.write (buf.toString ().getBytes ());
    } finally {
      if (os != null) os.close ();
      if (lock != null) lock.releaseLock ();
    }
  }

  
  private void saveContainer (ComponentContainer container, StringBuffer buf, String indent) {
    if (container instanceof RADVisualContainer) {
      saveVisualComponent ((RADVisualComponent)container, buf, indent);
      buf.append ("\n");
      buf.append (indent); addElementOpen (buf, XML_LAYOUT);
      // [PENDING]
      buf.append (indent); addElementClose (buf, XML_LAYOUT);
    } else {
      saveComponent ((RADComponent)container, buf, indent);
    }
    buf.append ("\n");
    buf.append (indent); addElementOpen (buf, XML_SUB_COMPONENTS);
    RADComponent[] children = container.getSubBeans ();
    for (int i = 0; i < children.length; i++) {
      if (children[i] instanceof ComponentContainer) {
        buf.append (indent + ONE_INDENT); 
        addElementOpenAttr (
            buf, 
            XML_CONTAINER, 
            new String[] { ATTR_COMPONENT_CLASS, ATTR_COMPONENT_NAME }, 
            new String[] { children[i].getBeanClass ().getName (), children[i].getName () }
        );
        saveContainer ((ComponentContainer)children[i], buf, indent + ONE_INDENT + ONE_INDENT);
        buf.append (indent + ONE_INDENT); addElementClose (buf, XML_CONTAINER);
      } else {
        buf.append (indent + ONE_INDENT); 
        addElementOpenAttr (
            buf, 
            XML_COMPONENT, 
            new String[] { ATTR_COMPONENT_CLASS, ATTR_COMPONENT_NAME }, 
            new String[] { children[i].getBeanClass ().getName (), children[i].getName () }
        );
        if (children[i] instanceof RADVisualComponent) {
          saveVisualComponent ((RADVisualComponent)children[i], buf, indent + ONE_INDENT + ONE_INDENT);
        } else {
          saveComponent (children[i], buf, indent + ONE_INDENT + ONE_INDENT);
        }
        buf.append (indent + ONE_INDENT); addElementClose (buf, XML_COMPONENT);
      }
    }
    buf.append (indent); addElementClose (buf, XML_SUB_COMPONENTS);
  }

  private void saveVisualComponent (RADVisualComponent component, StringBuffer buf, String indent) {
    saveComponent (component, buf, indent);
    if (!(component instanceof FormContainer)) {
      buf.append ("\n");
      buf.append (indent); addElementOpen (buf, XML_CONSTRAINTS);
      saveConstraints (component, buf, indent + ONE_INDENT);
      buf.append (indent); addElementClose (buf, XML_CONSTRAINTS);
    }
  }
  
  private void saveComponent (RADComponent component, StringBuffer buf, String indent) {
    // 1. Properties
    if (component.getChangedProperties ().size () > 0) {
      buf.append (indent); addElementOpen (buf, XML_PROPERTIES);
      saveProperties (component.getChangedProperties (), buf, indent + ONE_INDENT);
      buf.append (indent); addElementClose (buf, XML_PROPERTIES);
      buf.append ("\n");
    }

    // 2. Events
    if (component.getEventsList ().getEventNames ().size () > 0) {
      buf.append (indent); addElementOpen (buf, XML_EVENTS);
      saveEvents (component.getEventsList ().getEventNames (), buf, indent + ONE_INDENT);
      buf.append (indent); addElementClose (buf, XML_EVENTS);
      buf.append ("\n");
    }

    // 3. Aux Values
    if (component.getAuxValues ().size () > 0) {
      buf.append (indent); addElementOpen (buf, XML_AUX_VALUES);
      saveAuxValues (component.getAuxValues (), buf, indent + ONE_INDENT);
      buf.append (indent); addElementClose (buf, XML_AUX_VALUES);
    }
  }

  private void saveProperties (Map changedProperties, StringBuffer buf, String indent) {
    for (Iterator it = changedProperties.keySet ().iterator (); it.hasNext (); ) {
      RADComponent.RADProperty prop = (RADComponent.RADProperty) it.next ();
      PropertyDescriptor desc = prop.getPropertyDescriptor ();
      Object value = changedProperties.get (prop);
      String valueType = value.getClass ().getName ();
      if (value instanceof RADConnectionPropertyEditor.RADConnectionDesignValue) {
        valueType = VALUE_RAD_CONNECTION;
      }
      buf.append (indent); 
      addLeafElementOpenAttr (
          buf, 
          XML_PROPERTY, 
          new String[] { 
            ATTR_PROPERTY_NAME, 
            ATTR_PROPERTY_TYPE, 
            ATTR_PROPERTY_EDITOR, 
            ATTR_PROPERTY_VALUE_TYPE, 
            ATTR_PROPERTY_VALUE },
          new String[] { 
            desc.getName (), 
            desc.getPropertyType ().getName (), 
            prop.getCurrentEditor ().getClass ().getName (), 
            valueType, 
            encodeValue (value, prop.getCurrentEditor ()) 
          }
      );
    }
  }

  private void saveEvents (Hashtable events, StringBuffer buf, String indent) {
    for (Iterator it = events.keySet ().iterator (); it.hasNext (); ) {
      String eventName = (String)it.next ();
      String handlerName = (String)events.get (eventName);
      
      buf.append (indent); 
      addLeafElementOpenAttr (
          buf, 
          XML_EVENT, 
          new String[] { 
            ATTR_EVENT_NAME, 
            ATTR_EVENT_HANDLER 
          },
          new String[] { 
            eventName, 
            handlerName, 
          }
      );
    }
  }
    
  private void saveAuxValues (Map auxValues, StringBuffer buf, String indent) {
    for (Iterator it = auxValues.keySet ().iterator (); it.hasNext (); ) {
      String valueName = (String) it.next ();
      Object value = auxValues.get (valueName);
      buf.append (indent); 
      addLeafElementOpenAttr (
          buf, 
          XML_AUX_VALUE, 
          new String[] { 
            ATTR_AUX_NAME, 
            ATTR_AUX_VALUE },
          new String[] { 
            valueName, 
            encodeValue (value, null) 
          }
      );
    }
  }

  private void saveConstraints (RADVisualComponent component, StringBuffer buf, String indent) {
    Map constraintsMap = component.getConstraintsMap ();
    for (Iterator it = constraintsMap.keySet ().iterator (); it.hasNext (); ) {
      String layoutName = (String)it.next ();
      DesignLayout.ConstraintsDescription cd = (DesignLayout.ConstraintsDescription)constraintsMap.get (layoutName);
      
      buf.append (indent); 
      addLeafElementOpenAttr (
          buf, 
          XML_CONSTRAINT, 
          new String[] { 
            ATTR_CONSTRAINT_LAYOUT, 
            ATTR_CONSTRAINT_VALUE 
          },
          new String[] { 
            layoutName, 
            "", 
          }
      );
    }
  }
  
// --------------------------------------------------------------------------------------
// Value encoding methods
  
  private Object decodeValue (String value) throws IOException {
     if ((value == null) || (value.length () == 0)) return null;
     char[] bisChars = value.toCharArray ();
     byte[] bytes = new byte[bisChars.length];
     String singleNum = "";
     int count = 0;
     for (int i = 0; i < bisChars.length; i++) {
       if (',' == bisChars[i]) {
         try {
           bytes[count++] = (byte)Integer.parseInt (singleNum, 16);
         } catch (NumberFormatException e) {
           throw new IOException ();
         }
         singleNum = "";
       } else {
         singleNum += bisChars[i];
       }
     }

     ByteArrayInputStream bis = new ByteArrayInputStream (bytes, 0, count);
     try {
       ObjectInputStream ois = new ObjectInputStream (bis);
       return ois.readObject ();
     } catch (Exception e) {
       throw new IOException ();
     }
  }

  private String encodeValue (Object value, PropertyEditor ed) {
/*    if (value == null) return "null";
   
    if (value instanceof RADConnectionPropertyEditor.RADConnectionDesignValue) {
      RADConnectionPropertyEditor.RADConnectionDesignValue radConn = (RADConnectionPropertyEditor.RADConnectionDesignValue) value;
      StringBuffer sb = new StringBuffer ();
      switch (radConn.type) {
        case RADConnectionPropertyEditor.RADConnectionDesignValue.TYPE_PROPERTY:  
          sb.append ("type=property"); 
          sb.append (";component=");
          sb.append (radConn.radComponentName);
          sb.append (";name=");
          sb.append (radConn.propertyName);
          break;
        case RADConnectionPropertyEditor.RADConnectionDesignValue.TYPE_METHOD:  
          sb.append ("type=method"); 
          sb.append (";component=");
          sb.append (radConn.radComponentName);
          sb.append (";name=");
          sb.append (radConn.propertyName);
          break;
        case RADConnectionPropertyEditor.RADConnectionDesignValue.TYPE_CODE:  
          sb.append ("type=code"); 
          sb.append (";code=");
          sb.append (Utilities.replaceString (radConn.userCode, "\n", "\\n"));
          break;
        case RADConnectionPropertyEditor.RADConnectionDesignValue.TYPE_VALUE:  
          sb.append ("type=value"); 
          sb.append (";value=");
          sb.append (radConn.value);
          break;
      }
      return sb.toString ();
    }
    
    if ((value instanceof Integer) || 
        (value instanceof Short) ||
        (value instanceof Byte) ||
        (value instanceof Long) ||
        (value instanceof Float) ||
        (value instanceof Double) ||
        (value instanceof Boolean) ||
        (value instanceof Character) ||
        (value instanceof String)) {
       return value.toString ();
     } 
     
     if (ed instanceof XMLPropertyEditor) {
       ed.setValue (value);
       org.w3c.dom.Node node = ((XMLPropertyEditor)ed).storeToXML ();
       
       return "XMLized"; // [PENDING]
     }
     
     if (value instanceof Class) {
       return ((Class)value).getName ();
     }
*/     
     ByteArrayOutputStream bos = new ByteArrayOutputStream ();
     try {
       ObjectOutputStream oos = new ObjectOutputStream (bos);
       oos.writeObject (value);
       oos.close ();
     } catch (Exception e) {
       return "null"; // problem during serialization
     }
     byte[] bosBytes = bos.toByteArray ();
     StringBuffer sb = new StringBuffer (bosBytes.length);
     for (int i = 0; i < bosBytes.length; i++) {
       if (i != bosBytes.length - 1) {
         sb.append (Integer.toHexString (bosBytes[i])+",");
       } else {
         sb.append (Integer.toHexString (bosBytes[i]));
       }
     }
     return sb.toString ();
  }
  
// --------------------------------------------------------------------------------------
// Utility formatting methods
  
  private void addElementOpen (StringBuffer buf, String elementName) {
    buf.append ("<");
    buf.append (elementName);
    buf.append (">\n");
  }

  private void addElementOpenAttr (StringBuffer buf, String elementName, String[] attrNames, String[] attrValues) {
    buf.append ("<");
    buf.append (elementName);
    for (int i = 0; i < attrNames.length; i++) {
      buf.append (" ");
      buf.append (attrNames[i]);
      buf.append ("=\"");
      buf.append (attrValues[i]);
      buf.append ("\"");
    }
    buf.append (">\n");
  }
  
  private void addLeafElementOpenAttr (StringBuffer buf, String elementName, String[] attrNames, String[] attrValues) {
    buf.append ("<");
    buf.append (elementName);
    for (int i = 0; i < attrNames.length; i++) {
      buf.append (" ");
      buf.append (attrNames[i]);
      buf.append ("=\"");
      buf.append (attrValues[i]);
      buf.append ("\"");
    }
    buf.append ("/>\n");
  }

  private void addElementClose (StringBuffer buf, String elementName) {
    buf.append ("</");
    buf.append (elementName);
    buf.append (">\n");
  }
}

/*
 * Log
 *  9    Gandalf   1.8         7/11/99  Ian Formanek    
 *  8    Gandalf   1.7         7/8/99   Ian Formanek    
 *  7    Gandalf   1.6         7/5/99   Ian Formanek    getComponentInstance->getBeanInstance,
 *        getComponentClass->getBeanClass
 *  6    Gandalf   1.5         6/30/99  Ian Formanek    Second draft of XML 
 *       Serialization
 *  5    Gandalf   1.4         6/28/99  Ian Formanek    First cut of XML 
 *       persistence
 *  4    Gandalf   1.3         6/24/99  Ian Formanek    
 *  3    Gandalf   1.2         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         6/7/99   Ian Formanek    
 *  1    Gandalf   1.0         5/30/99  Ian Formanek    
 * $
 */
