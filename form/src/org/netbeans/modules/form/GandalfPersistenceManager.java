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
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Hashtable;
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
  public static final String XML_SERIALIZED_PROPERTY_VALUE = "SerializedValue";
  
  public static final String ATTR_FORM_VERSION = "version";
  public static final String ATTR_FORM_TYPE = "type";
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
  public static final String ATTR_LAYOUT_CLASS = "class";
  public static final String ATTR_CONSTRAINT_LAYOUT = "layoutClass";
  public static final String ATTR_CONSTRAINT_VALUE = "value";

  public static final String VALUE_RAD_CONNECTION = "RADConnection";
  
  private static final String ONE_INDENT =  "  ";

  private org.w3c.dom.Document topDocument = new com.ibm.xml.dom.DocumentImpl ();
  
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
    FileObject formFile = formObject.getFormEntry ().getFile ();
    org.w3c.dom.Document doc = org.openide.loaders.XMLDataObject.parse (formFile.getURL ());
    org.w3c.dom.Element mainElement = doc.getDocumentElement ();
//    walkTree (mainElement, "");
// A. Do various checks

  // 1. check the top-level element name
    if (!XML_FORM.equals (mainElement.getTagName ())) {
      throw new IOException (FormEditor.getFormBundle ().getString ("ERR_BadXMLFormat"));
    }

  // 2. check the form version
    if (!CURRENT_VERSION.equals (mainElement.getAttribute (ATTR_FORM_VERSION))) {
      throw new IOException (FormEditor.getFormBundle ().getString ("ERR_BadXMLVersion"));
    }
    String infoClass = mainElement.getAttribute (ATTR_FORM_TYPE);
    FormInfo formInfo = null;
    if (infoClass == null) {
      return null;
    }
    try {
      formInfo = (FormInfo)TopManager.getDefault ().systemClassLoader ().loadClass (infoClass).newInstance ();
    } catch (Exception e) {
      throw new IOException (java.text.MessageFormat.format (
        FormEditor.getFormBundle ().getString ("FMT_ERR_FormInfoNotFound"),
        new String[] { infoClass }
        )
      );
    }

    RADForm radForm = new RADForm (formInfo);
    FormManager2 formManager2 = new FormManager2 (formObject, radForm);
    RADVisualContainer topComp = (RADVisualContainer)radForm.getTopLevelComponent (); // [PENDING - illegal cast]
    
// B. process top-element's subnodes (all required)

    org.w3c.dom.NodeList childNodes = mainElement.getChildNodes ();   
    if (childNodes == null) {
      throw new IOException (FormEditor.getFormBundle ().getString ("ERR_BadXMLFormat"));
    }

    processNonVisuals (mainElement, formManager2);
    processContainer (mainElement, formManager2, topComp, null);

    return formManager2;
  }

  private boolean processNonVisuals (org.w3c.dom.Node node, FormManager2 formManager2) {
    org.w3c.dom.Node nonVisualsNode = findSubNode (node, XML_NON_VISUAL_COMPONENTS);
    org.w3c.dom.NodeList childNodes = (nonVisualsNode == null) ? null : nonVisualsNode.getChildNodes ();
    ArrayList list = new ArrayList ();
    if (childNodes != null) {
      for (int i = 0; i < childNodes.getLength (); i++) {
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

    RADComponent[] nonVisualComps = new RADComponent[list.size ()];
    list.toArray (nonVisualComps);
    formManager2.initNonVisualComponents (nonVisualComps);
    return true;
  }

  private boolean processComponent (org.w3c.dom.Node node, FormManager2 formManager2, RADComponent comp, ComponentContainer parentContainer) {
    System.out.println ("ProcessComponent: "+node.getNodeName ());
    if (!(comp instanceof FormContainer)) {
      comp.initialize (formManager2);
      org.w3c.dom.NamedNodeMap attributes = node.getAttributes ();
      String className = attributes.getNamedItem (ATTR_COMPONENT_CLASS).getNodeValue (); // [PENDING - survive non-existent attr]
      Class compClass = null;
      try {
        compClass = TopManager.getDefault ().systemClassLoader ().loadClass (className);
      } catch (Exception e) {
        e.printStackTrace ();
      }
      String compName = attributes.getNamedItem (ATTR_COMPONENT_NAME).getNodeValue (); // [PENDING - survive non-existent attr]
      comp.setComponent (compClass);
      comp.setName (compName);
      formManager2.getVariablesPool ().createVariable (compName, compClass);
    }


    //convertComponent (node, nonVisualsComps[i]);
    org.w3c.dom.NodeList childNodes = node.getChildNodes ();

    if (childNodes != null) {
      for (int i = 0; i < childNodes.getLength (); i++) {
        org.w3c.dom.Node componentNode = childNodes.item (i);
        if (componentNode.getNodeType () == org.w3c.dom.Node.TEXT_NODE) continue; // ignore text nodes

        if (XML_PROPERTIES.equals (componentNode.getNodeName ())) {
          loadProperties (componentNode, comp);
        } else if (XML_EVENTS.equals (componentNode.getNodeName ())) {
          Hashtable events = loadEvents (componentNode);
          if (events != null) {
            comp.initDeserializedEvents (events);
          }
          
        } else if (XML_AUX_VALUES.equals (componentNode.getNodeName ())) {
          HashMap auxValues = loadAuxValues (componentNode);
          if (auxValues != null) {
            for (Iterator it = auxValues.keySet ().iterator (); it.hasNext (); ) {
              String auxName = (String)it.next ();
              comp.setAuxValue (auxName, auxValues.get (auxName));
            }
          }
        }
      }
    }


    return true;
  }

  private boolean processVisualComponent (org.w3c.dom.Node node, FormManager2 formManager2, RADComponent comp, ComponentContainer parentContainer) {
    processComponent (node, formManager2, comp, parentContainer);

    if (!(comp instanceof FormContainer)) {
      // [PENDING - constraints]
    }

    return true;
  }

  private boolean processContainer (org.w3c.dom.Node node, FormManager2 formManager2, RADComponent comp, ComponentContainer parentContainer) {
    if (comp instanceof RADVisualComponent) {
      processVisualComponent (node, formManager2, comp, parentContainer);
    } else {
      processComponent (node, formManager2, comp, parentContainer);
    }

    if (comp instanceof ComponentContainer) {
      org.w3c.dom.Node subCompsNode = findSubNode (node, XML_SUB_COMPONENTS);
      org.w3c.dom.NodeList children = subCompsNode.getChildNodes ();
      if (children != null) {
        ArrayList list = new ArrayList ();
        for (int i = 0; i < children.getLength (); i++) {
          org.w3c.dom.Node componentNode = children.item (i);
          if (componentNode.getNodeType () == org.w3c.dom.Node.TEXT_NODE) continue; // ignore text nodes

          if (XML_COMPONENT.equals (componentNode.getNodeName ())) {  // [PENDING - visual x non-visual]
            RADVisualComponent newComp = new RADVisualComponent ();
            processVisualComponent (componentNode, formManager2, newComp, (ComponentContainer)comp);
            list.add (newComp);
          } else {
            RADVisualContainer newComp = new RADVisualContainer ();
            processContainer (componentNode, formManager2, newComp, (ComponentContainer)comp);
            list.add (newComp);
          }
        }
        RADComponent[] childComps = new RADComponent[list.size ()];
        list.toArray (childComps);
        ((ComponentContainer)comp).initSubComponents (childComps);
      } else {
        ((ComponentContainer)comp).initSubComponents (new RADComponent[0]);
      }
    }

    if (comp instanceof RADVisualContainer) {
      org.w3c.dom.Node layoutNode = findSubNode (node, XML_LAYOUT);
      org.w3c.dom.NamedNodeMap attributes = layoutNode.getAttributes ();
      String className = attributes.getNamedItem (ATTR_LAYOUT_CLASS).getNodeValue (); // [PENDING - survive non-existent attr]
      try {
        DesignLayout dl = (DesignLayout)TopManager.getDefault ().systemClassLoader ().loadClass (className).newInstance ();
        ((RADVisualContainer)comp).setDesignLayout (dl); // [PENDING]
      } catch (Exception e) {
        return false; // [PENDING - notify]
      }
    }
    return true;
  }

  private org.w3c.dom.Node findSubNode (org.w3c.dom.Node node, String name) {
    org.w3c.dom.NodeList children = node.getChildNodes ();
    if (children != null) {
      for (int i = 0; i < children.getLength (); i++) {
        if (children.item (i).getNodeType () == org.w3c.dom.Node.TEXT_NODE) continue; // ignore text nodes
        if (name.equals (children.item (i).getNodeName ())) {
          return children.item (i);
        }
      }
    }
    return null;
  }

  private void loadProperties (org.w3c.dom.Node node, RADComponent comp) {

    org.w3c.dom.NodeList children = node.getChildNodes ();
    if (children != null) {
      for (int i = 0; i < children.getLength (); i++) {
        if (children.item (i).getNodeType () == org.w3c.dom.Node.TEXT_NODE) continue; // ignore text nodes

        if (XML_PROPERTY.equals (children.item (i).getNodeName ())) {

          org.w3c.dom.NamedNodeMap attrs = children.item (i).getAttributes ();
          if (attrs != null) {
            org.w3c.dom.Node nameNode = attrs.getNamedItem (ATTR_PROPERTY_NAME);
            org.w3c.dom.Node typeNode = attrs.getNamedItem (ATTR_PROPERTY_TYPE);
            org.w3c.dom.Node editorNode = attrs.getNamedItem (ATTR_PROPERTY_EDITOR);
            org.w3c.dom.Node valueTypeNode = attrs.getNamedItem (ATTR_PROPERTY_VALUE_TYPE);
            org.w3c.dom.Node valueNode = attrs.getNamedItem (ATTR_PROPERTY_VALUE);
            System.out.println ("Property: "+nameNode.getNodeValue ());
            System.out.println ("Property Type: "+typeNode.getNodeValue ());
            System.out.println ("Editor: "+editorNode.getNodeValue ());
            System.out.println ("Value Type: "+valueTypeNode.getNodeValue ());
            System.out.println ("Value: "+valueNode.getNodeValue ());
            if ((nameNode != null) && (typeNode != null) && (editorNode != null) && (valueTypeNode != null) && (valueNode != null)) { // [PENDING - error check]
              org.openide.nodes.Node.Property prop = comp.getPropertyByName (nameNode.getNodeValue ());
              try {
                PropertyEditor ed = (PropertyEditor)TopManager.getDefault ().systemClassLoader ().loadClass (editorNode.getNodeValue ()).newInstance ();
                Object value = decodeValue (valueNode.getNodeValue (), ed);
                if (prop instanceof RADComponent.RADProperty) {
                  ((RADComponent.RADProperty)prop).setCurrentEditor (ed);
                }
                prop.setValue (value);
              } catch (Exception e) {
        e.printStackTrace ();
                // [PENDING - handle error]
              }
            }
          }
            
        }
      }
    }
  }

  private Hashtable loadEvents (org.w3c.dom.Node node) {
    Hashtable eventsTable = new Hashtable (20);

    org.w3c.dom.NodeList children = node.getChildNodes ();
    if (children != null) {
      for (int i = 0; i < children.getLength (); i++) {
        if (children.item (i).getNodeType () == org.w3c.dom.Node.TEXT_NODE) continue; // ignore text nodes

        if (XML_EVENT.equals (children.item (i).getNodeName ())) {

          org.w3c.dom.NamedNodeMap attrs = children.item (i).getAttributes ();
          if (attrs != null) {
            org.w3c.dom.Node nameNode = attrs.getNamedItem (ATTR_EVENT_NAME);
            org.w3c.dom.Node handlerNode = attrs.getNamedItem (ATTR_EVENT_HANDLER);
            if ((nameNode != null) && (handlerNode != null)) { // [PENDING - error check]
              eventsTable.put (nameNode.getNodeValue (), handlerNode.getNodeValue ());
            }
          }
            
        }
      }
    }
    return eventsTable;
  }

  private HashMap loadAuxValues (org.w3c.dom.Node node) {
    HashMap auxTable = new HashMap (20);

    org.w3c.dom.NodeList children = node.getChildNodes ();
    if (children != null) {
      for (int i = 0; i < children.getLength (); i++) {
        if (children.item (i).getNodeType () == org.w3c.dom.Node.TEXT_NODE) continue; // ignore text nodes

        if (XML_AUX_VALUE.equals (children.item (i).getNodeName ())) {

          org.w3c.dom.NamedNodeMap attrs = children.item (i).getAttributes ();
          if (attrs != null) {
            org.w3c.dom.Node nameNode = attrs.getNamedItem (ATTR_AUX_NAME);
            org.w3c.dom.Node valueNode = attrs.getNamedItem (ATTR_AUX_VALUE);
            if ((nameNode != null) && (valueNode != null)) { // [PENDING - error check]
              try {
                Object auxValue = decodeValue (valueNode.getNodeValue (), null);
                auxTable.put (nameNode.getNodeValue (), auxValue);
              } catch (IOException e) {
        e.printStackTrace ();
                // [PENDING - handle error]
              }
            }
          }
            
        }
      }
    }
    return auxTable;
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
      buf.append (indent);
      addElementOpenAttr (
          buf, 
          XML_LAYOUT, 
          new String[] { ATTR_LAYOUT_CLASS }, 
          new String[] { ((RADVisualContainer)container).getDesignLayout ().getClass ().getName () }
      );
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
      String encodedValue = null; 
      org.w3c.dom.Node valueNode = null;
      if (prop.getCurrentEditor () instanceof XMLPropertyEditor) {
        prop.getCurrentEditor ().setValue (value);
        valueNode = ((XMLPropertyEditor)prop.getCurrentEditor ()).storeToXML (topDocument);
      } else {
        encodedValue = encodeValue (value, prop.getCurrentEditor ());
        if (encodedValue == null) {
          // [PENDING - notify problem?]
          continue;
        }
      }
      buf.append (indent); 
      addElementOpenAttr (
          buf, 
          XML_PROPERTY, 
          new String[] { 
            ATTR_PROPERTY_NAME, 
            ATTR_PROPERTY_TYPE, 
            ATTR_PROPERTY_EDITOR, 
            ATTR_PROPERTY_VALUE_TYPE, 
            },
          new String[] { 
            desc.getName (), 
            desc.getPropertyType ().getName (), 
            prop.getCurrentEditor ().getClass ().getName (), 
            valueType, 
          }
      );
      if (valueNode != null) {
        saveNodeIntoText (buf, valueNode, indent + ONE_INDENT);
      } else {
        addLeafElementOpenAttr (
            buf, 
            XML_SERIALIZED_PROPERTY_VALUE, 
            new String[] { 
              ATTR_PROPERTY_VALUE, 
              },
            new String[] {
              encodedValue,
            }
        );
      }
      buf.append (indent); 
      addElementClose (buf, XML_PROPERTY);
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
      if (value == null) continue; // such values are not saved
      String encodedValue = encodeValue (value, null);
      if (encodedValue == null) {
        // [PENDING - solve problem?]
        continue;
      }
      buf.append (indent); 
      addLeafElementOpenAttr (
          buf, 
          XML_AUX_VALUE, 
          new String[] { 
            ATTR_AUX_NAME, 
            ATTR_AUX_VALUE },
          new String[] { 
            valueName, 
            encodedValue 
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
            "", // [PENDING]
          }
      );
    }
  }
  
// --------------------------------------------------------------------------------------
// Value encoding methods
  
  private Object decodeValue (String value, PropertyEditor editor) throws IOException {
     if ((value == null) || (value.length () == 0)) return null;
    System.out.println ("Decode value: "+value);
     char[] bisChars = value.toCharArray ();
     byte[] bytes = new byte[bisChars.length];
     String singleNum = "";
     int count = 0;
     for (int i = 0; i < bisChars.length; i++) {
       if (',' == bisChars[i]) {
         try {
           System.out.println ("Parsing int: "+singleNum);
//           bytes[count++] = (byte)Integer.parseInt (singleNum, 16);
           bytes[count++] = Byte.parseByte (singleNum);
         } catch (NumberFormatException e) {
           e.printStackTrace ();
           throw new IOException ();
         }
         singleNum = "";
       } else {
         singleNum += bisChars[i];
       }
     }
     // add the last byte
     bytes[count++] = Byte.parseByte (singleNum);

     ByteArrayInputStream bis = new ByteArrayInputStream (bytes, 0, count);
     try {
       ObjectInputStream ois = new ObjectInputStream (bis);
       Object ret = ois.readObject ();
    System.out.println ("Decoded value: "+ret);
       return ret;
     } catch (Exception e) {
       e.printStackTrace ();
       throw new IOException ();
     }
  }

  private String encodeValue (Object value, PropertyEditor ed) {
    System.out.println ("Encode value: "+value);
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
       e.printStackTrace ();
       return null; // problem during serialization
     }
     byte[] bosBytes = bos.toByteArray ();
     StringBuffer sb = new StringBuffer (bosBytes.length);
     for (int i = 0; i < bosBytes.length; i++) {
       if (i != bosBytes.length - 1) {
//         sb.append (Integer.toHexString (bosBytes[i])+","); //(int)bosBytes[i] & 0xFF)+",");
         sb.append (bosBytes[i]+",");
       } else {
//         sb.append (Integer.toHexString (bosBytes[i])); //(int)bosBytes[i] & 0xFF));
         sb.append (""+bosBytes[i]);
       }
     }
     System.out.println ("Encoded value: "+sb.toString ());
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

  private void saveNodeIntoText (StringBuffer buf, org.w3c.dom.Node valueNode, String indent) {
    buf.append (indent); 
    buf.append ("<");
    buf.append (valueNode.getNodeName ());

    org.w3c.dom.NamedNodeMap attributes = valueNode.getAttributes ();

    for (int i = 0; i < attributes.getLength (); i++) {
      org.w3c.dom.Node attrNode = attributes.item (i);
      String attrName = attrNode.getNodeName (); 
      String attrValue = attrNode.getNodeValue (); 
      
      buf.append (" ");
      buf.append (attrName);
      buf.append ("=\"");
      buf.append (attrValue);
      buf.append ("\"");
    }

    // [PENDING - CNODES, TEXT NODES, ...]

    org.w3c.dom.NodeList children = valueNode.getChildNodes ();
    if ((children == null) || (children.getLength () == 0)) {
      buf.append ("/>\n");
    } else {
      buf.append (">\n");
      for (int i = 0; i < children.getLength (); i++) {
        if (children.item (i).getNodeType () == org.w3c.dom.Node.TEXT_NODE) continue; // ignore text nodes
        saveNodeIntoText (buf, children.item (i), indent + ONE_INDENT);
      }
      buf.append (indent);
      buf.append ("</");
      buf.append (valueNode.getNodeName ());
      buf.append (">\n");
    }
  }


}

/*
 * Log
 *  13   Gandalf   1.12        7/12/99  Ian Formanek    Uses XMLPropertyEditor 
 *       or sub element for serialized property values
 *  12   Gandalf   1.11        7/12/99  Ian Formanek    Second cut - loads/saves
 *       events, properties and aux values
 *  11   Gandalf   1.10        7/12/99  Ian Formanek    First cut of saving
 *  10   Gandalf   1.9         7/11/99  Ian Formanek    hex encoding is 2-char 
 *       for all bytes
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
