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
import java.util.List;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.openide.explorer.propertysheet.editors.XMLPropertyEditor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
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
  public static final String ATTR_PROPERTY_VALUE = "value";
  public static final String ATTR_EVENT_NAME = "event";
  public static final String ATTR_EVENT_HANDLER = "handler";
  public static final String ATTR_AUX_NAME = "name";
  public static final String ATTR_AUX_VALUE = "value";
  public static final String ATTR_LAYOUT_CLASS = "class";
  public static final String ATTR_CONSTRAINT_LAYOUT = "layoutClass";
  public static final String ATTR_CONSTRAINT_VALUE = "value";

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
        e.printStackTrace ();
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

    loadNonVisuals (mainElement, formManager2);
    loadContainer (mainElement, formManager2, topComp, null);

    return formManager2;
  }

  private boolean loadNonVisuals (org.w3c.dom.Node node, FormManager2 formManager2) {
    org.w3c.dom.Node nonVisualsNode = findSubNode (node, XML_NON_VISUAL_COMPONENTS);
    org.w3c.dom.NodeList childNodes = (nonVisualsNode == null) ? null : nonVisualsNode.getChildNodes ();
    ArrayList list = new ArrayList ();
    if (childNodes != null) {
      for (int i = 0; i < childNodes.getLength (); i++) {
        if (childNodes.item (i).getNodeType () == org.w3c.dom.Node.TEXT_NODE) return true; // ignore text nodes
        if (XML_COMPONENT.equals (childNodes.item (i).getNodeName ())) {
          RADComponent comp = new RADComponent ();
          if (loadComponent (childNodes.item (i), formManager2, comp, null)) {
            list.add (comp);
          }
        } else if (XML_CONTAINER.equals (childNodes.item (i).getNodeName ())) {
          RADContainer cont = new RADContainer ();
          if (loadContainer (childNodes.item (i), formManager2, cont, null)) {
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

  private boolean loadComponent (org.w3c.dom.Node node, FormManager2 formManager2, RADComponent comp, ComponentContainer parentContainer) {
    try {
//      System.out.println ("loadComponent: "+node.getNodeName ());
      if (!(comp instanceof FormContainer)) {
        comp.initialize (formManager2);
        String className = findAttribute (node, ATTR_COMPONENT_CLASS);
        Class compClass = null;
        try {
          compClass = TopManager.getDefault ().systemClassLoader ().loadClass (className);
        } catch (Exception e) {
          e.printStackTrace ();
        }
        String compName = findAttribute (node, ATTR_COMPONENT_NAME);
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
    } catch (Exception e) {
        e.printStackTrace ();
      return false; // [PENDING - undo already processed init?]
    }
  }

  private boolean loadVisualComponent (org.w3c.dom.Node node, FormManager2 formManager2, RADVisualComponent comp, ComponentContainer parentContainer) {
    loadComponent (node, formManager2, comp, parentContainer);

    if (!(comp instanceof FormContainer)) {
      org.w3c.dom.Node constraintsNode = findSubNode (node, XML_CONSTRAINTS);
      if (constraintsNode != null) {
        org.w3c.dom.Node[] constrNodes = findSubNodes (constraintsNode, XML_CONSTRAINT);
        for (int i = 0; i < constrNodes.length; i++) {
          String layoutName = findAttribute (constrNodes[i], ATTR_CONSTRAINT_LAYOUT);
          String cdName = findAttribute (constrNodes[i], ATTR_CONSTRAINT_VALUE);
          if ((layoutName != null) && (cdName != null)) {
            try {
              Class layoutClass = TopManager.getDefault ().systemClassLoader ().loadClass (layoutName);
              DesignLayout.ConstraintsDescription cd = (DesignLayout.ConstraintsDescription) TopManager.getDefault ().systemClassLoader ().loadClass (cdName).newInstance ();
              org.w3c.dom.NodeList children = constrNodes[i].getChildNodes ();
              if (children != null) {
                for (int j = 0; j < children.getLength (); j++) {
                  if (children.item (j).getNodeType () == org.w3c.dom.Node.ELEMENT_NODE) {
                    cd.readFromXML (children.item (j));
                    break;
                  }
                }
              }
              comp.setConstraints (layoutClass, cd);
            } catch (Exception e) {
              e.printStackTrace ();
              // ignore and try another constraints // [PENDING - add to errors list]
            }
          }
        }
      }
    }

    return true;
  }

  private boolean loadContainer (org.w3c.dom.Node node, FormManager2 formManager2, RADComponent comp, ComponentContainer parentContainer) {
    if (comp instanceof RADVisualComponent) {
      loadVisualComponent (node, formManager2, (RADVisualComponent)comp, parentContainer);
    } else {
      loadComponent (node, formManager2, comp, parentContainer);
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
            loadVisualComponent (componentNode, formManager2, newComp, (ComponentContainer)comp);
            list.add (newComp);
          } else {
            RADVisualContainer newComp = new RADVisualContainer ();
            loadContainer (componentNode, formManager2, newComp, (ComponentContainer)comp);
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
      String className = findAttribute (layoutNode, ATTR_LAYOUT_CLASS);
      try {
        DesignLayout dl = (DesignLayout)TopManager.getDefault ().systemClassLoader ().loadClass (className).newInstance ();
        org.w3c.dom.Node[] propNodes = findSubNodes (layoutNode, XML_PROPERTY);
        if (propNodes.length > 0) {
          HashMap propsMap = new HashMap (propNodes.length * 2);
          for (int i = 0; i < propNodes.length; i++) {
            Object propValue = getEncodedPropertyValue (propNodes[i], null);
            String propName = findAttribute (propNodes[i], ATTR_PROPERTY_NAME);
            if ((propName != null) && (propValue != null)) {
              propsMap.put (propName, propValue);
            }
          }
          dl.initChangedProperties (propsMap);
        }

        ((RADVisualContainer)comp).setDesignLayout (dl);
      } catch (Exception e) {
        e.printStackTrace ();
        return false; // [PENDING - notify]
      }
    }
    return true;
  }

  private void loadProperties (org.w3c.dom.Node node, RADComponent comp) {
    org.w3c.dom.Node[] propNodes = findSubNodes (node, XML_PROPERTY);
    if (propNodes.length > 0) {
      for (int i = 0; i < propNodes.length; i++) {
        Object propValue = getEncodedPropertyValue (propNodes[i], comp);
        String propName = findAttribute (propNodes[i], ATTR_PROPERTY_NAME);

        org.openide.nodes.Node.Property prop = comp.getPropertyByName (propName);

        if (prop instanceof RADComponent.RADProperty) {
          String propertyEditor = findAttribute (propNodes[i], ATTR_PROPERTY_EDITOR);
          if (propertyEditor != null) {
            try {
              PropertyEditor ed = (PropertyEditor)TopManager.getDefault ().systemClassLoader ().loadClass (propertyEditor).newInstance ();
              ((RADComponent.RADProperty)prop).setCurrentEditor (ed);
            } catch (Exception e) {
              // ignore
            }
          }
        }
        if (propValue != null) {
          try {
            prop.setValue (propValue);
          } catch (java.lang.reflect.InvocationTargetException e) {
            // ignore this property // [PENDING]
          } catch (IllegalAccessException e) {
            // ignore this property // [PENDING]
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
          String eventName = findAttribute (children.item (i), ATTR_EVENT_NAME);
          String handlerName = findAttribute (children.item (i), ATTR_EVENT_HANDLER);
          if ((eventName != null) && (handlerName != null)) { // [PENDING - error check]
            eventsTable.put (eventName, handlerName);
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

          String auxName = findAttribute (children.item (i), ATTR_AUX_NAME);
          String auxValue = findAttribute (children.item (i), ATTR_AUX_VALUE);
          if ((auxName != null) && (auxValue != null)) { // [PENDING - error check]
            try {
              Object auxValueDecoded = decodeValue (auxValue);
              auxTable.put (auxName, auxValueDecoded);
            } catch (IOException e) {
              e.printStackTrace ();
              // [PENDING - handle error]
            }
          }
        }
      }
    }
    return auxTable;
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
      saveLayout (((RADVisualContainer)container).getDesignLayout (), buf, indent);
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

  private void saveLayout (DesignLayout layout, StringBuffer buf, String indent) {
    buf.append ("\n");
    buf.append (indent);
    List changedProperties = layout.getChangedProperties ();
    if (changedProperties.size () == 0) {
      addLeafElementOpenAttr (
          buf, 
          XML_LAYOUT, 
          new String[] { ATTR_LAYOUT_CLASS }, 
          new String[] { layout.getClass ().getName () }
      );
    } else {
      addElementOpenAttr (
          buf, 
          XML_LAYOUT, 
          new String[] { ATTR_LAYOUT_CLASS }, 
          new String[] { layout.getClass ().getName () }
      );
      for (Iterator it = changedProperties.iterator (); it.hasNext (); ) {
        Node.Property prop = (Node.Property)it.next ();
        String propertyName = prop.getName ();
        Object value = null;
        try {
          value = prop.getValue ();
        } catch (java.lang.reflect.InvocationTargetException e) {
          continue; // ignore this property
        } catch (IllegalAccessException e) {
          continue; // ignore this property
        }
        PropertyEditor ed = prop.getPropertyEditor ();

        String encodedValue = null; 
        String encodedSerializeValue = null; 
        org.w3c.dom.Node valueNode = null;
        if (ed instanceof XMLPropertyEditor) {
          ed.setValue (value);
          valueNode = ((XMLPropertyEditor)ed).storeToXML (topDocument);
        } else {
          encodedValue = encodePrimitiveValue (value);
          if (encodedValue == null) encodedSerializeValue = encodeValue (value);
          if ((encodedValue == null) && (encodedSerializeValue == null)) {
            // [PENDING - notify problem?]
            continue;
          }
        }

        buf.append (indent + ONE_INDENT);

        if (encodedValue != null) {
          addLeafElementOpenAttr (
              buf, 
              XML_PROPERTY, 
              new String[] { ATTR_PROPERTY_NAME, ATTR_PROPERTY_TYPE, ATTR_PROPERTY_VALUE }, 
              new String[] { propertyName, prop.getValueType ().getName (), encodedValue }
          );
        } else {
          addElementOpenAttr (
              buf, 
              XML_PROPERTY, 
              new String[] { 
                ATTR_PROPERTY_NAME, 
                ATTR_PROPERTY_TYPE, 
                ATTR_PROPERTY_EDITOR, 
                },
              new String[] { 
                prop.getName (), 
                prop.getValueType ().getName (), 
                ed.getClass ().getName (), 
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
                  encodedSerializeValue,
                }
            );
          }
          buf.append (indent); 
          addElementClose (buf, XML_PROPERTY);
        }
      }
      buf.append (indent); addElementClose (buf, XML_LAYOUT);
    }
  }

  private void saveVisualComponent (RADVisualComponent component, StringBuffer buf, String indent) {
    saveComponent (component, buf, indent);
    if (!(component instanceof FormContainer)) {
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
      String encodedValue = null; 
      String encodedSerializeValue = null; 
      org.w3c.dom.Node valueNode = null;
      if (prop.getCurrentEditor () instanceof XMLPropertyEditor) {
        prop.getCurrentEditor ().setValue (value);
        valueNode = ((XMLPropertyEditor)prop.getCurrentEditor ()).storeToXML (topDocument);
      } else {
        encodedValue = encodePrimitiveValue (value);
        if (encodedValue == null) encodedSerializeValue = encodeValue (value);
        if ((encodedValue == null) && (encodedSerializeValue == null)) {
          // [PENDING - notify problem?]
          continue;
        }
      }
      buf.append (indent); 
      if (encodedValue != null) {
        addLeafElementOpenAttr (
            buf, 
            XML_PROPERTY, 
            new String[] { 
              ATTR_PROPERTY_NAME, 
              ATTR_PROPERTY_TYPE, 
              ATTR_PROPERTY_EDITOR, 
              ATTR_PROPERTY_VALUE, 
              },
            new String[] { 
              desc.getName (), 
              desc.getPropertyType ().getName (), 
              prop.getCurrentEditor ().getClass ().getName (), 
              encodedValue,
            }
        );
      } else {
        addElementOpenAttr (
            buf, 
            XML_PROPERTY, 
            new String[] { 
              ATTR_PROPERTY_NAME, 
              ATTR_PROPERTY_TYPE, 
              ATTR_PROPERTY_EDITOR, 
              },
            new String[] { 
              desc.getName (), 
              desc.getPropertyType ().getName (), 
              prop.getCurrentEditor ().getClass ().getName (), 
            }
        );
        if (valueNode != null) {
          saveNodeIntoText (buf, valueNode, indent + ONE_INDENT);
        } else {
          buf.append (indent + ONE_INDENT); 
          addLeafElementOpenAttr (
              buf, 
              XML_SERIALIZED_PROPERTY_VALUE, 
              new String[] { 
                ATTR_PROPERTY_VALUE, 
                },
              new String[] {
                encodedSerializeValue,
              }
          );
        }
        buf.append (indent); 
        addElementClose (buf, XML_PROPERTY);
      }
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
      String encodedValue = encodeValue (value);
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
      
      org.w3c.dom.Node constrNode = cd.storeToXML (topDocument);
      if (constrNode != null) {
        buf.append (indent); 
        addElementOpenAttr (
            buf, 
            XML_CONSTRAINT, 
            new String[] { 
              ATTR_CONSTRAINT_LAYOUT, 
              ATTR_CONSTRAINT_VALUE 
            },
            new String[] { 
              layoutName, 
              cd.getClass ().getName (),
            }
        );

        saveNodeIntoText (buf, constrNode, indent + ONE_INDENT);

        buf.append (indent); 
        addElementClose (
            buf, 
            XML_CONSTRAINT
        );
      } else {
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
              cd.getClass ().getName (),
            }
        );
      }
    }
  }
  
// --------------------------------------------------------------------------------------
// Value encoding methods

  private Object getEncodedPropertyValue (org.w3c.dom.Node propertyNode, RADComponent radComponent) {
    org.w3c.dom.NamedNodeMap attrs = propertyNode.getAttributes ();
    if (attrs == null) {
      return null;
    }
    org.w3c.dom.Node nameNode = attrs.getNamedItem (ATTR_PROPERTY_NAME);
    org.w3c.dom.Node typeNode = attrs.getNamedItem (ATTR_PROPERTY_TYPE);
    org.w3c.dom.Node editorNode = attrs.getNamedItem (ATTR_PROPERTY_EDITOR);
    org.w3c.dom.Node valueNode = attrs.getNamedItem (ATTR_PROPERTY_VALUE);

/*    System.out.println ("getEncodedPropertyValue::Property: "+nameNode.getNodeValue ());
    System.out.println ("getEncodedPropertyValue::Property Type: "+typeNode.getNodeValue ());
    System.out.println ("getEncodedPropertyValue::Editor Node: "+editorNode);
    System.out.println ("getEncodedPropertyValue::Value Node: "+valueNode);
    if (valueNode != null) {
      System.out.println ("getEncodedPropertyValue::Value in Node: "+valueNode.getNodeValue ());
    }
*/
    if ((nameNode == null) || (typeNode == null)) {
      return null;
    }

    try {
      PropertyEditor ed = null;
      if (editorNode != null) {
        ed = (PropertyEditor)TopManager.getDefault ().systemClassLoader ().loadClass (editorNode.getNodeValue ()).newInstance ();
        if (ed instanceof FormAwareEditor) {
          ((FormAwareEditor)ed).setRADComponent (radComponent);
        }
      }
      Class propertyType = findPropertyType (typeNode.getNodeValue ());
      Object value = null;

      if (valueNode != null) {
        value = decodePrimitiveValue (valueNode.getNodeValue (), propertyType);
      } else {
        if ((ed != null) && (ed instanceof XMLPropertyEditor)) {
          org.w3c.dom.NodeList propChildren = propertyNode.getChildNodes ();
          if ((propChildren != null) && (propChildren.getLength () > 0)) {
            for (int i = 0; i < propChildren.getLength (); i++) {
              if (propChildren.item (i).getNodeType () == org.w3c.dom.Node.ELEMENT_NODE) {
                ((XMLPropertyEditor)ed).readFromXML (propChildren.item (i));
                value = ed.getValue ();
                break;
              }
            }
          }
        } else {
          org.w3c.dom.NodeList propChildren = propertyNode.getChildNodes ();
          if ((propChildren != null) && (propChildren.getLength () > 0)) {
            for (int i = 0; i < propChildren.getLength (); i++) {
              if (XML_SERIALIZED_PROPERTY_VALUE.equals (propChildren.item (i).getNodeName ())) {
                String serValue = findAttribute (propChildren.item (i), ATTR_PROPERTY_VALUE);
                if (serValue != null) {
                  value = decodeValue (serValue);
                }
                break;
              }
            }
          }
        }
      }
/*      if (value == null) {
        System.out.println ("getEncodedPropertyValue::returning null");
      } else {
        System.out.println ("getEncodedPropertyValue::returning: "+value.getClass ().getName ()+", value: "+value);
      } */
      return value;

    } catch (Exception e) {
        e.printStackTrace ();
      return null; 
    }
  }

  private Class  findPropertyType (String type) throws ClassNotFoundException {
    if ("int".equals (type)) return Integer.TYPE;
    else if ("short".equals (type)) return Short.TYPE;
    else if ("byte".equals (type)) return Byte.TYPE;
    else if ("long".equals (type)) return Long.TYPE;
    else if ("float".equals (type)) return Float.TYPE;
    else if ("double".equals (type)) return Double.TYPE;
    else if ("boolean".equals (type)) return Boolean.TYPE;
    else if ("char".equals (type)) return Character.TYPE;
    else {
      return TopManager.getDefault ().systemClassLoader ().loadClass (type);
    }
  }

  /** Decodes a value of given type from the specified String. Supported types are: <UL>
  * <LI> RADConnectionPropertyEditor.RADConnectionDesignValue
  * <LI> Class
  * <LI> String
  * <LI> Integer, Short, Byte, Long, Float, Double, Boolean, Character </UL>
  * @return decoded value or null if specified object is not of supported type
  */
  private Object decodePrimitiveValue (String encoded, Class type) {
    System.out.println ("Decode primitive value: "+encoded+", of type: "+type.getName ());
    if (Integer.class.isAssignableFrom (type) || Integer.TYPE.equals (type)) {
      return Integer.valueOf (encoded);
    } else if (Short.class.isAssignableFrom (type) || Short.TYPE.equals (type)) {
      return Short.valueOf (encoded);
    } else if (Byte.class.isAssignableFrom (type) || Byte.TYPE.equals (type)) {
      return Byte.valueOf (encoded);
    } else if (Long.class.isAssignableFrom (type) || Long.TYPE.equals (type)) {
      return Long.valueOf (encoded);
    } else if (Float.class.isAssignableFrom (type) || Float.TYPE.equals (type)) {
      return Float.valueOf (encoded);
    } else if (Double.class.isAssignableFrom (type) || Double.TYPE.equals (type)) {
      return Double.valueOf (encoded);
    } else if (Boolean.class.isAssignableFrom (type) || Boolean.TYPE.equals (type)) {
      return Boolean.valueOf (encoded);
    } else if (Character.class.isAssignableFrom (type) || Character.TYPE.equals (type)) {
      return new Character (encoded.charAt(0));
    } else if (String.class.isAssignableFrom (type)) {
      return encoded;
    } else if (Class.class.isAssignableFrom (type)) {
      try {
        return TopManager.getDefault ().systemClassLoader ().loadClass (encoded);
      } catch (ClassNotFoundException e) {
        e.printStackTrace ();
        // will return null as the notification of failure
      }
    }
    return null;
  }

  /** Encodes specified value into a String. Supported types are: <UL>
  * <LI> Class
  * <LI> String
  * <LI> Integer, Short, Byte, Long, Float, Double, Boolean, Character </UL>
  * @return String containing encoded value or null if specified object is not of supported type
  */
  private String encodePrimitiveValue (Object value) {
    System.out.println ("Encode primitive value: "+value);
   
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
     
    if (value instanceof Class) {
      return ((Class)value).getName ();
    }
    return null; // is not a primitive type
  }

  /** Decodes a value of from the specified String containing textual representation of serialized stream.
  * @return decoded object
  * @exception IOException thrown if an error occures during deserializing the object
  */
  private Object decodeValue (String value) throws IOException {
     if ((value == null) || (value.length () == 0)) return null;
    System.out.println ("Decode value: "+value);
     char[] bisChars = value.toCharArray ();
     byte[] bytes = new byte[bisChars.length];
     String singleNum = "";
     int count = 0;
     for (int i = 0; i < bisChars.length; i++) {
       if (',' == bisChars[i]) {
         try {
//           System.out.println ("Parsing int: "+singleNum);
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

  /** Encodes specified value to a String containing textual representation of serialized stream.
  * @return String containing textual representation of the serialized object
  */
  private String encodeValue (Object value) {
    System.out.println ("Encode value: "+value);

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
//       sb.append (Integer.toHexString (bosBytes[i])+","); //(int)bosBytes[i] & 0xFF)+",");
         sb.append (bosBytes[i]+",");
      } else {
//        sb.append (Integer.toHexString (bosBytes[i])); //(int)bosBytes[i] & 0xFF));
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

// --------------------------------------------------------------------------------------
// Utility DOM access methods

/*  private void walkTree (org.w3c.dom.Node node, String indent) {
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
*/
  /** Finds first subnode of given node with specified name.
  * @param node the node whose subnode we are looking for
  * @param name requested name of the subnode
  * @return the found subnode or null if no such subnode exists
  */
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

  /** Finds all subnodes of given node with specified name.
  * @param node the node whose subnode we are looking for
  * @param name requested name of the subnode
  * @return array of the found subnodes
  */
  private org.w3c.dom.Node[] findSubNodes (org.w3c.dom.Node node, String name) {
    ArrayList list = new ArrayList ();
    org.w3c.dom.NodeList children = node.getChildNodes ();
    if (children != null) {
      for (int i = 0; i < children.getLength (); i++) {
        if (children.item (i).getNodeType () == org.w3c.dom.Node.TEXT_NODE) continue; // ignore text nodes
        if (name.equals (children.item (i).getNodeName ())) {
          list.add (children.item (i));
        }
      }
    }
    return (org.w3c.dom.Node[]) list.toArray (new org.w3c.dom.Node[list.size ()]);
  }

  /** Utility method to obtain given attribute value from specified Node.
  * @return attribute name or null if the attribute is not present
  */
  private String findAttribute (org.w3c.dom.Node node, String attributeName) {
    org.w3c.dom.NamedNodeMap attributes = node.getAttributes ();
    org.w3c.dom.Node valueNode = attributes.getNamedItem (attributeName);
    if (valueNode == null) return null;
    else return valueNode.getNodeValue ();
  }
}

/*
 * Log
 *  15   Gandalf   1.14        7/13/99  Ian Formanek    Constraints persistence 
 *       added
 *  14   Gandalf   1.13        7/13/99  Ian Formanek    Third draft
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
