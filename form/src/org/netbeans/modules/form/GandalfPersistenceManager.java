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
import java.util.*;

import org.openide.explorer.propertysheet.editors.XMLPropertyEditor;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Utilities;
import org.openide.TopManager;
import org.openide.loaders.XMLDataObject;

import com.netbeans.developerx.loaders.form.formeditor.layouts.*;
import com.netbeans.developer.modules.loaders.form.forminfo.*;

/** 
*
* @author Ian Formanek
*/
public class GandalfPersistenceManager extends PersistenceManager {
  public static final String CURRENT_VERSION = "1.0"; // NOI18N
  
  public static final String XML_FORM = "Form"; // NOI18N
  public static final String XML_NON_VISUAL_COMPONENTS = "NonVisualComponents"; // NOI18N
  public static final String XML_CONTAINER = "Container"; // NOI18N
  public static final String XML_COMPONENT = "Component"; // NOI18N
  public static final String XML_MENU_COMPONENT = "MenuItem"; // NOI18N
  public static final String XML_MENU_CONTAINER = "Menu"; // NOI18N
  public static final String XML_LAYOUT = "Layout"; // NOI18N
  public static final String XML_CONSTRAINTS = "Constraints"; // NOI18N
  public static final String XML_CONSTRAINT = "Constraint"; // NOI18N
  public static final String XML_SUB_COMPONENTS = "SubComponents"; // NOI18N
  public static final String XML_EVENTS = "Events"; // NOI18N
  public static final String XML_EVENT = "EventHandler"; // NOI18N
  public static final String XML_PROPERTIES = "Properties"; // NOI18N
  public static final String XML_PROPERTY = "Property"; // NOI18N
  public static final String XML_SYNTHETIC_PROPERTY = "SyntheticProperty"; // NOI18N
  public static final String XML_SYNTHETIC_PROPERTIES = "SyntheticProperties"; // NOI18N
  public static final String XML_AUX_VALUES = "AuxValues"; // NOI18N
  public static final String XML_AUX_VALUE = "AuxValue"; // NOI18N
  public static final String XML_SERIALIZED_PROPERTY_VALUE = "SerializedValue"; // NOI18N
  
  public static final String ATTR_FORM_VERSION = "version"; // NOI18N
  public static final String ATTR_FORM_TYPE = "type"; // NOI18N
  public static final String ATTR_COMPONENT_NAME = "name"; // NOI18N
  public static final String ATTR_COMPONENT_CLASS = "class"; // NOI18N
  public static final String ATTR_PROPERTY_NAME = "name"; // NOI18N
  public static final String ATTR_PROPERTY_TYPE = "type"; // NOI18N
  public static final String ATTR_PROPERTY_EDITOR = "editor"; // NOI18N
  public static final String ATTR_PROPERTY_VALUE = "value"; // NOI18N
  public static final String ATTR_PROPERTY_PRE_CODE = "preCode"; // NOI18N
  public static final String ATTR_PROPERTY_POST_CODE = "postCode"; // NOI18N
  public static final String ATTR_EVENT_NAME = "event"; // NOI18N
  public static final String ATTR_EVENT_HANDLER = "handler"; // NOI18N
  public static final String ATTR_AUX_NAME = "name"; // NOI18N
  public static final String ATTR_AUX_VALUE = "value"; // NOI18N
  public static final String ATTR_AUX_VALUE_TYPE = "type"; // NOI18N
  public static final String ATTR_LAYOUT_CLASS = "class"; // NOI18N
  public static final String ATTR_CONSTRAINT_LAYOUT = "layoutClass"; // NOI18N
  public static final String ATTR_CONSTRAINT_VALUE = "value"; // NOI18N

  private static final String ONE_INDENT =  "  "; // NOI18N
  private static final Object NO_VALUE = new Object ();
  
  private org.w3c.dom.Document topDocument = XMLDataObject.createDocument();
  
  
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
    } catch (org.xml.sax.SAXException e) { // [PENDING - just test whether it is an XML file and in this case return false
      return false;
    }
    return true;
  }

  private String readEncoding (InputStream is) {
    // If all else fails, assume XML without a declaration, and
    // using UTF-8 encoding.
    String useEncoding = "UTF-8"; // NOI18N
    byte buf [];
    int	len;
    buf = new byte [4];
    // See if we can figure out the character encoding used
    // in this file by peeking at the first few bytes.
    try {
      len = is.read (buf);
      if (len == 4) switch (buf [0] & 0x0ff) {
        case 0:
          // 00 3c 00 3f == illegal UTF-16 big-endian
          if (buf [1] == 0x3c && buf [2] == 0x00 && buf [3] == 0x3f) {
            useEncoding = "UnicodeBig"; // NOI18N
          }
          // else it's probably UCS-4
          break;

        case '<':      // 0x3c: the most common cases!
          switch (buf [1] & 0x0ff) {
            // First character is '<'; could be XML without
            // an XML directive such as "<hello>", "<!-- ...", // NOI18N
            // and so on.
            default:
              break;
            // 3c 00 3f 00 == illegal UTF-16 little endian
            case 0x00:
              if (buf [2] == 0x3f && buf [3] == 0x00) {
                useEncoding = "UnicodeLittle"; // NOI18N
              }
              // else probably UCS-4
              break;

            // 3c 3f 78 6d == ASCII and supersets '<?xm'
            case '?':
              if (buf [2] != 'x' || buf [3] != 'm')
              break;
              //
              // One of several encodings could be used:
              // Shift-JIS, ASCII, UTF-8, ISO-8859-*, etc
              //
              useEncoding = "UTF8"; // NOI18N
          }
          break;

          // 4c 6f a7 94 ... some EBCDIC code page
          case 0x4c:
            if (buf [1] == 0x6f
              && (0x0ff & buf [2]) == 0x0a7
              && (0x0ff & buf [3]) == 0x094) {
              useEncoding = "CP037"; // NOI18N
            }
            // whoops, treat as UTF-8
            break;

          // UTF-16 big-endian
          case 0xfe:
            if ((buf [1] & 0x0ff) != 0xff) break;
            useEncoding = "UTF-16"; // NOI18N

          // UTF-16 little-endian
          case 0xff:
            if ((buf [1] & 0x0ff) != 0xfe) break;
            useEncoding = "UTF-16"; // NOI18N

          // default ... no XML declaration
          default:
            break;
      }

      byte buffer[] = new byte [1024];
      is.read(buffer);
      String s = new String (buffer, useEncoding);
      int pos = s.indexOf("encoding"); // NOI18N
      String result=null;
      int startPos, endPos;
      if ((pos > 0) && (pos < s.indexOf (">"))) { // NOI18N
        if ( (startPos = s.indexOf('"', pos)) > 0 && 
             (endPos = s.indexOf('"', startPos+1)) > startPos ) {
          result = s.substring(startPos+1, endPos);
        }
      }
      if (result == null) {
        // encoding not specified in xml
        //result = System.getProperty ("file.encoding");
        result = null;
      }
      return result;
    } catch (java.io.IOException e) {
      e.printStackTrace();
      return null;
    }
  }
  
  /** Called to actually load the form stored in specified formObject.
  * @param formObject the FormDataObject which represents the form files
  * @return the FormManager2 representing the loaded form or null if some problem occured
  * @exception IOException if any problem occured when loading the form
  */
  public FormManager2 loadForm (FormDataObject formObject) throws IOException {
    FileObject formFile = formObject.getFormEntry ().getFile ();
    org.w3c.dom.Document doc;
    org.w3c.dom.Element mainElement;
    String encoding;
    try {
      encoding = readEncoding (formFile.getURL ().openStream ());
      doc = org.openide.loaders.XMLDataObject.parse (formFile.getURL ());
      mainElement = doc.getDocumentElement ();
    } catch (org.xml.sax.SAXException e) {
      throw new IOException (e.getMessage());
    }
//    walkTree (mainElement, ""); // NOI18N
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
      formInfo = (FormInfo)TopManager.getDefault ().currentClassLoader ().loadClass (infoClass).newInstance ();
    } catch (Exception e) {
      if (Boolean.getBoolean ("netbeans.debug.exceptions")) e.printStackTrace (); // NOI18N
      throw new IOException (java.text.MessageFormat.format (
        FormEditor.getFormBundle ().getString ("FMT_ERR_FormInfoNotFound"),
        new String[] { infoClass }
        )
      );
    }

    RADForm radForm = new RADForm (formInfo);
    FormManager2 formManager2 = new FormManager2 (formObject, radForm);
    formManager2.setEncoding (encoding);
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
//    System.out.println ("NonVisual children: "+childNodes); // NOI18N
    ArrayList list = new ArrayList ();
    if (childNodes != null) {
//    System.out.println ("NonVisual children len: "+childNodes.getLength ()); // NOI18N
      for (int i = 0; i < childNodes.getLength (); i++) {
        if (childNodes.item (i).getNodeType () == org.w3c.dom.Node.TEXT_NODE) continue; // ignore text nodes
//        System.out.println ("Processing node: "+childNodes.item (i).getNodeName ()); // NOI18N
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
        } else if (XML_MENU_COMPONENT.equals (childNodes.item (i).getNodeName ())) {
//          System.out.println ("$$$MenuItem"); // NOI18N
          RADMenuItemComponent comp = new RADMenuItemComponent ();
          if (loadComponent (childNodes.item (i), formManager2, comp, null)) {
            list.add (comp);
          }
        } else if (XML_MENU_CONTAINER.equals (childNodes.item (i).getNodeName ())) {
//          System.out.println ("$$$Menu"); // NOI18N
          RADMenuComponent cont = new RADMenuComponent ();
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
      if (!(comp instanceof FormContainer)) {
        comp.initialize (formManager2);
        String className = findAttribute (node, ATTR_COMPONENT_CLASS);
        String compName = findAttribute (node, ATTR_COMPONENT_NAME);
        Class compClass = null;
        try {
          compClass = TopManager.getDefault ().currentClassLoader ().loadClass (className);
        } catch (Exception e) {
          if (Boolean.getBoolean ("netbeans.debug.exceptions")) e.printStackTrace (); // NOI18N
          FormEditor.fileError (java.text.MessageFormat.format (
            FormEditor.getFormBundle ().getString ("FMT_ERR_ClassNotFound"),
            new Object [] {
              e.getMessage (),
              e.getClass ().getName (),
            }
          ), e); 
          return false; // failed to load the component!!!
        }
        comp.setComponent (compClass);
        comp.setName (compName);
      }
  
  
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
          } else if (XML_SYNTHETIC_PROPERTIES.equals (componentNode.getNodeName ())) {
            loadSyntheticProperties (componentNode, comp);
          }
        }
      }
  
      return true;
    } catch (Exception e) {
      if (Boolean.getBoolean ("netbeans.debug.exceptions")) e.printStackTrace (); // NOI18N
      return false; // [PENDING - undo already processed init?]
    }
  }

  private boolean loadVisualComponent (org.w3c.dom.Node node, FormManager2 formManager2, RADVisualComponent comp, ComponentContainer parentContainer) {
    if (!loadComponent (node, formManager2, comp, parentContainer)) {
      return false;
    }

    if (!(comp instanceof FormContainer)) {
      org.w3c.dom.Node constraintsNode = findSubNode (node, XML_CONSTRAINTS);
      if (constraintsNode != null) {
        org.w3c.dom.Node[] constrNodes = findSubNodes (constraintsNode, XML_CONSTRAINT);
        for (int i = 0; i < constrNodes.length; i++) {
          String layoutName = findAttribute (constrNodes[i], ATTR_CONSTRAINT_LAYOUT);
          String cdName = findAttribute (constrNodes[i], ATTR_CONSTRAINT_VALUE);
          if ((layoutName != null) && (cdName != null)) {
            try {
              Class layoutClass = TopManager.getDefault ().currentClassLoader ().loadClass (layoutName);
              DesignLayout.ConstraintsDescription cd = (DesignLayout.ConstraintsDescription) TopManager.getDefault ().currentClassLoader ().loadClass (cdName).newInstance ();
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
              if (Boolean.getBoolean ("netbeans.debug.exceptions")) e.printStackTrace (); // NOI18N
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
      if (!loadVisualComponent (node, formManager2, (RADVisualComponent)comp, parentContainer)) return false;
    } else {
      if (!loadComponent (node, formManager2, comp, parentContainer)) return false;
    }

//        System.out.println ("1..."); // NOI18N
    if (comp instanceof ComponentContainer) {
//        System.out.println ("2..."); // NOI18N
      org.w3c.dom.Node subCompsNode = findSubNode (node, XML_SUB_COMPONENTS);
      org.w3c.dom.NodeList children = null;
      if (subCompsNode != null) children = subCompsNode.getChildNodes ();
//        System.out.println ("3..."); // NOI18N
      if (children != null) {
//        System.out.println ("4..."); // NOI18N
        ArrayList list = new ArrayList ();
        for (int i = 0; i < children.getLength (); i++) {
          org.w3c.dom.Node componentNode = children.item (i);
//        System.out.println ("Processing node: "+componentNode.getNodeName ()); // NOI18N
          if (componentNode.getNodeType () == org.w3c.dom.Node.TEXT_NODE) continue; // ignore text nodes

          if (XML_COMPONENT.equals (componentNode.getNodeName ())) {  // [PENDING - visual x non-visual]
            RADVisualComponent newComp = new RADVisualComponent ();
            if (loadVisualComponent (componentNode, formManager2, newComp, (ComponentContainer)comp)) {
              list.add (newComp);
            }
          } else if (XML_MENU_COMPONENT.equals (componentNode.getNodeName ())) {  // [PENDING - visual x non-visual]
            RADMenuItemComponent newComp = new RADMenuItemComponent ();
//        System.out.println ("Processing menu node: "+componentNode.getNodeName ()); // NOI18N
            if (loadContainer (componentNode, formManager2, newComp, (ComponentContainer)comp)) {
              list.add (newComp);
            }
          } else if (XML_MENU_CONTAINER.equals (componentNode.getNodeName ())) {  // [PENDING - visual x non-visual]
            RADMenuComponent newComp = new RADMenuComponent ();
//        System.out.println ("Processing menu container node: "+componentNode.getNodeName ()); // NOI18N
            if (loadContainer (componentNode, formManager2, newComp, (ComponentContainer)comp)) {
              list.add (newComp);
            }
          } else {
            RADVisualContainer newComp = new RADVisualContainer ();
            if (loadContainer (componentNode, formManager2, newComp, (ComponentContainer)comp)) {
              list.add (newComp);
            }
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
        DesignLayout dl = (DesignLayout)TopManager.getDefault ().currentClassLoader ().loadClass (className).newInstance ();
        org.w3c.dom.Node[] propNodes = findSubNodes (layoutNode, XML_PROPERTY);
        if (propNodes.length > 0) {
          HashMap propsMap = new HashMap (propNodes.length * 2);
          for (int i = 0; i < propNodes.length; i++) {
            try {
              Object propValue = getEncodedPropertyValue (propNodes[i], null);
              String propName = findAttribute (propNodes[i], ATTR_PROPERTY_NAME);
              if ((propName != null) && (propValue != null) && (propValue != NO_VALUE)) {
                propsMap.put (propName, propValue);
              }
            } catch (Exception e) {
              if (Boolean.getBoolean ("netbeans.debug.exceptions")) e.printStackTrace (); // NOI18N
              // ignore property with problem
              // [PENDING - notify problem]
            }
          }
          dl.initChangedProperties (propsMap);
        }

        ((RADVisualContainer)comp).setDesignLayout (dl);
      } catch (Exception e) {
        // if (System.getProperty ("netbeans.debug.exceptions") != null) // [PENDING]
        if (Boolean.getBoolean ("netbeans.debug.exceptions")) e.printStackTrace (); // NOI18N
        return false; // [PENDING - notify]
      }
    }
    return true;
  }

  private void loadProperties (org.w3c.dom.Node node, RADComponent comp) {
    org.w3c.dom.Node[] propNodes = findSubNodes (node, XML_PROPERTY);
    if (propNodes.length > 0) {
      for (int i = 0; i < propNodes.length; i++) {
        Object propValue;
        try {
          propValue = getEncodedPropertyValue (propNodes[i], comp);
          if (propValue == NO_VALUE) {
            // the value was not saved, just the pre/post code, which was already set inside the getEncodedPropertyValue method
            continue; 
          }
        } catch (Exception e) {
          if (Boolean.getBoolean ("netbeans.debug.exceptions")) e.printStackTrace (); // NOI18N
          // [PENDING - notify error]
          continue; // ignore this property
        }

        String propName = findAttribute (propNodes[i], ATTR_PROPERTY_NAME);
        String propType = findAttribute (propNodes[i], ATTR_PROPERTY_TYPE);

        RADComponent.RADProperty prop = comp.getPropertyByName (propName);

        String propertyEditor = findAttribute (propNodes[i], ATTR_PROPERTY_EDITOR);
        if (propertyEditor != null) {
          try {
            Class editorClass = TopManager.getDefault ().currentClassLoader ().loadClass (propertyEditor);
            Class propertyClass = findPropertyType (propType);
            PropertyEditor ed = FormEditor.createPropertyEditor (editorClass, propertyClass, comp, prop);
            ((RADComponent.RADProperty)prop).setCurrentEditor (ed);
          } catch (Exception e) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) e.printStackTrace (); // NOI18N
            // ignore
          }
        }
        try {
          prop.setValue (propValue);
        } catch (java.lang.reflect.InvocationTargetException e) {
          if (Boolean.getBoolean ("netbeans.debug.exceptions")) e.printStackTrace (); // NOI18N
          // ignore this property // [PENDING]
        } catch (IllegalAccessException e) {
          if (Boolean.getBoolean ("netbeans.debug.exceptions")) e.printStackTrace (); // NOI18N
          // ignore this property // [PENDING]
        } catch (Exception e) {
          // unexpected exception - always printed
          e.printStackTrace ();
          // ignore this property
        }
      }
    }
  }

  private void loadSyntheticProperties (org.w3c.dom.Node node, RADComponent comp) {
    org.w3c.dom.Node[] propNodes = findSubNodes (node, XML_SYNTHETIC_PROPERTY);
    if (propNodes.length > 0) {
      for (int i = 0; i < propNodes.length; i++) {
        String propName = findAttribute (propNodes[i], ATTR_PROPERTY_NAME);
        String encodedValue = findAttribute (propNodes[i], ATTR_PROPERTY_VALUE);
        String propType = findAttribute (propNodes[i], ATTR_PROPERTY_TYPE);

        Class propClass = null;
        try {
          if (propType != null) propClass = findPropertyType (propType);
        } catch (Exception e2) {
          // OK, try to use decodeValue in this case
          if (Boolean.getBoolean ("netbeans.debug.exceptions")) e2.printStackTrace (); // NOI18N
        }
        Object propValue=null;
        //System.out.println("loading name="+propName+", encodedValue="+encodedValue); // NOI18N
        try {
          if (propClass != null) {
            try {
              propValue = decodePrimitiveValue (encodedValue, propClass);
            } catch (IllegalArgumentException e) {
              // not a primitive type
              propValue = decodeValue (encodedValue);
            }
          } else { // info about the property type was not saved
            propValue = decodeValue (encodedValue);
          }
        } catch (IOException e) {
          if (Boolean.getBoolean ("netbeans.debug.exceptions")) e.printStackTrace (); // NOI18N
          // [PENDING - handle error]
        }
        //System.out.println("......encoded to:"+propValue); // NOI18N

        Node.Property [] props = comp.getSyntheticProperties ();
        Node.Property prop=null;
        for (int j=0, n=props.length; j<n; j++) {
          if (props[j].getName ().equals (propName)) {
            prop = props [j];
            break;
          }
        }

        if (prop == null)       // unknown property, ignore
          continue;
        
        try {
          prop.setValue (propValue);
        } catch (java.lang.reflect.InvocationTargetException e) {
          if (Boolean.getBoolean ("netbeans.debug.exceptions")) e.printStackTrace (); // NOI18N
          // ignore this property // [PENDING]
        } catch (IllegalAccessException e) {
          if (Boolean.getBoolean ("netbeans.debug.exceptions")) e.printStackTrace (); // NOI18N
          // ignore this property // [PENDING]
        } catch (Exception e) {
          // unexpected exception - always printed
          e.printStackTrace ();
          // ignore this property
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
          String auxValueClass = findAttribute (children.item (i), ATTR_AUX_VALUE_TYPE);
          if ((auxName != null) && (auxValue != null)) { // [PENDING - error check]
            try {
              Object auxValueDecoded = null;
              Class auxValueType = null;
              if (auxValueClass != null) {
                try {
                  auxValueType = findPropertyType (auxValueClass);
                } catch (Exception e2) {
                  // OK, try to use decodeValue in this case
                  if (Boolean.getBoolean ("netbeans.debug.exceptions")) e2.printStackTrace (); // NOI18N
                }
              }
              if (auxValueType != null) {
                try {
                  auxValueDecoded = decodePrimitiveValue (auxValue, auxValueType);
                } catch (IllegalArgumentException e3) {
                  // not decoded as primitive value
                  auxValueDecoded = decodeValue (auxValue);
                }
              } else {
                // info about property class not stored
                auxValueDecoded = decodeValue (auxValue);
              }
              auxTable.put (auxName, auxValueDecoded);
            } catch (IOException e) {
              if (Boolean.getBoolean ("netbeans.debug.exceptions")) e.printStackTrace (); // NOI18N
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
    String encoding = manager.getEncoding();
    
    if (encoding == null) {
      encoding = "UTF-8";
    }
    else {
      // XXX(-tdt) test if the encoding is supported by the JDK
      try {
        String x = new String(new byte[0], 0, 0, encoding);
      }
      catch (java.io.UnsupportedEncodingException ex) {
        encoding = "UTF-8";
      }
    }

    try {
      lock = formFile.lock ();
      StringBuffer buf = new StringBuffer ();
      
      // 1.store XML file header
      buf.append ("<?xml version=\"1.0\""); // NOI18N
      buf.append (" encoding=\"" + encoding + "\""); // NOI18N
      buf.append (" ?>\n"); // NOI18N
      buf.append ("\n"); // NOI18N
      
      // 2.store Form element
      addElementOpenAttr (
         buf, 
         XML_FORM, 
         new String[] { ATTR_FORM_VERSION, ATTR_FORM_TYPE }, 
         new String[] { CURRENT_VERSION, manager.getRADForm ().getFormInfo ().getClass ().getName () }
      );

      // 3.store Non-Visual Components
      RADComponent[] nonVisuals = manager.getNonVisualComponents ();
      if (nonVisuals.length > 0) {
        buf.append (ONE_INDENT); addElementOpen (buf, XML_NON_VISUAL_COMPONENTS);
        for (int i = 0; i < nonVisuals.length; i++) {
          String elementType;
  
          if (nonVisuals[i] instanceof RADMenuComponent) elementType = XML_MENU_CONTAINER;
          else if (nonVisuals[i] instanceof RADMenuItemComponent) elementType = XML_MENU_COMPONENT;
          else if (nonVisuals[i] instanceof ComponentContainer) elementType = XML_CONTAINER;
          else elementType = XML_COMPONENT;
  
          buf.append (ONE_INDENT + ONE_INDENT); 
          addElementOpenAttr (
              buf, 
              elementType, 
              new String[] { ATTR_COMPONENT_CLASS, ATTR_COMPONENT_NAME }, 
              new String[] { nonVisuals[i].getBeanClass ().getName (), nonVisuals[i].getName () }
          );
  
          if (nonVisuals[i] instanceof RADMenuItemComponent) {
            saveMenuComponent ((RADMenuItemComponent)nonVisuals[i], buf, ONE_INDENT + ONE_INDENT + ONE_INDENT);
          } else if (nonVisuals[i] instanceof ComponentContainer) {
            saveContainer ((ComponentContainer)nonVisuals[i], buf, ONE_INDENT + ONE_INDENT + ONE_INDENT);
          } else {
            saveComponent (nonVisuals[i], buf, ONE_INDENT + ONE_INDENT + ONE_INDENT);
          }
  
          buf.append (ONE_INDENT + ONE_INDENT); addElementClose (buf, elementType);
        }  
        buf.append (ONE_INDENT); addElementClose (buf, XML_NON_VISUAL_COMPONENTS);
      }
      // 4.store form and its visual components hierarchy
      saveContainer ((ComponentContainer)manager.getRADForm ().getTopLevelComponent (), buf, ONE_INDENT);
      addElementClose (buf, XML_FORM);
      
      os = formFile.getOutputStream (lock); // [PENDING - first save to ByteArray for safety]
      os.write (buf.toString ().getBytes(encoding));
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
    RADComponent[] children = container.getSubBeans ();
    if (children.length > 0) {
      buf.append (indent); addElementOpen (buf, XML_SUB_COMPONENTS);
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
  }

  private void saveLayout (DesignLayout layout, StringBuffer buf, String indent) {
    buf.append ("\n"); // NOI18N
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
          if (valueNode == null) continue; // editor refused to save the value
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
  
  private void saveMenuComponent (RADMenuItemComponent component, StringBuffer buf, String indent) {
    saveComponent (component, buf, indent);

    if (component instanceof RADMenuComponent) {
      RADComponent[] children = ((RADMenuComponent)component).getSubBeans ();
      if (children.length > 0) {
        buf.append (indent); addElementOpen (buf, XML_SUB_COMPONENTS);
        for (int i = 0; i < children.length; i++) {
          String elementType;
          if (children[i] instanceof RADMenuComponent) elementType = XML_MENU_CONTAINER;
          else if (children[i] instanceof RADMenuItemComponent) elementType = XML_MENU_COMPONENT;
          else elementType = XML_COMPONENT;
    
          buf.append (indent + ONE_INDENT); 
          addElementOpenAttr (
              buf, 
              elementType, 
              new String[] { ATTR_COMPONENT_CLASS, ATTR_COMPONENT_NAME }, 
              new String[] { children[i].getBeanClass ().getName (), children[i].getName () }
          );
          // [PENDING - RADComponents which are not menu???]
          saveMenuComponent ((RADMenuItemComponent)children[i], buf, indent + ONE_INDENT + ONE_INDENT);
          buf.append (indent + ONE_INDENT); addElementClose (buf, elementType);
        }
        buf.append (indent); addElementClose (buf, XML_SUB_COMPONENTS);
      }
    } 
  }

  private void saveComponent (RADComponent component, StringBuffer buf, String indent) {
    // 1. Properties
    boolean doSaveProps = false;
    RADComponent.RADProperty[] props = component.getAllProperties ();
    for (int i = 0; i < props.length; i++) {
      if (props[i].isChanged () || (props[i].getPreCode () != null) || (props[i].getPostCode () != null)) {
        doSaveProps = true;
        break;
      }
    }

    if (doSaveProps) {
      buf.append (indent); addElementOpen (buf, XML_PROPERTIES);
      saveProperties (component, buf, indent + ONE_INDENT);
      buf.append (indent); addElementClose (buf, XML_PROPERTIES);
    }

    // 1.a synthetic properties - only for RADVisualFormContainer
    if (component instanceof RADVisualFormContainer) {
      buf.append (indent); addElementOpen (buf, XML_SYNTHETIC_PROPERTIES);
      saveSyntheticProperties (component, buf, indent + ONE_INDENT);
      buf.append (indent); addElementClose (buf, XML_SYNTHETIC_PROPERTIES);
    }
    
    // 2. Events
    if (component.getEventsList ().getEventNames ().size () > 0) {
      buf.append ("\n"); // NOI18N
      buf.append (indent); addElementOpen (buf, XML_EVENTS);
      saveEvents (component.getEventsList ().getEventNames (), buf, indent + ONE_INDENT);
      buf.append (indent); addElementClose (buf, XML_EVENTS);
    }

    // 3. Aux Values
    if (component.getAuxValues ().size () > 0) {
      buf.append ("\n"); // NOI18N
      buf.append (indent); addElementOpen (buf, XML_AUX_VALUES);
      saveAuxValues (component.getAuxValues (), buf, indent + ONE_INDENT);
      buf.append (indent); addElementClose (buf, XML_AUX_VALUES);
    }
  }

  private void saveProperties (RADComponent component, StringBuffer buf, String indent) {
    RADComponent.RADProperty[] props = component.getAllProperties ();
    for (int i = 0; i < props.length; i++) {
      RADComponent.RADProperty prop = (RADComponent.RADProperty) props[i];
      PropertyDescriptor desc = prop.getPropertyDescriptor ();

      if (!props[i].isChanged ()) {
        if ((props[i].getPreCode () != null) || (props[i].getPreCode () != null)) {
          buf.append (indent); 
          // in this case save only the pre/post code
          addLeafElementOpenAttr (
              buf, 
              XML_PROPERTY, 
              new String[] { 
                ATTR_PROPERTY_NAME, 
                ATTR_PROPERTY_PRE_CODE, 
                ATTR_PROPERTY_POST_CODE, 
                },
              new String[] { 
                desc.getName (), 
                prop.getPreCode (),
                prop.getPostCode (),
              }
          );
        }
        continue; // not changed, so do not save value
      }

      Object value = null;
      try {
        value = prop.getValue ();
      } catch (Exception e) {
        if (Boolean.getBoolean ("netbeans.debug.exceptions")) e.printStackTrace (); // NOI18N
        // problem getting value => ignore this property
        continue;
      }
      String encodedValue = null; 
      String encodedSerializeValue = null; 
      org.w3c.dom.Node valueNode = null;
      if (prop.getCurrentEditor () instanceof XMLPropertyEditor) {
        prop.getCurrentEditor ().setValue (value);
        valueNode = ((XMLPropertyEditor)prop.getCurrentEditor ()).storeToXML (topDocument);
        if (valueNode == null) continue; // property editor refused to save the value
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
              ATTR_PROPERTY_PRE_CODE, 
              ATTR_PROPERTY_POST_CODE, 
              },
            new String[] { 
              desc.getName (), 
              desc.getPropertyType ().getName (), 
              prop.getCurrentEditor ().getClass ().getName (), 
              encodedValue,
              prop.getPreCode (),
              prop.getPostCode (),
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
              ATTR_PROPERTY_PRE_CODE, 
              ATTR_PROPERTY_POST_CODE, 
              },
            new String[] { 
              desc.getName (), 
              desc.getPropertyType ().getName (), 
              prop.getCurrentEditor ().getClass ().getName (), 
              prop.getPreCode (),
              prop.getPostCode (),
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
  
  private void saveSyntheticProperties (RADComponent component, StringBuffer buf, String indent) {
    Node.Property[] props = component.getSyntheticProperties ();
    for (int i = 0; i < props.length; i++) {
      Node.Property prop = props[i];

      Object value = null;
      try {
        value = prop.getValue ();
      } catch (Exception e) {
        if (Boolean.getBoolean ("netbeans.debug.exceptions")) e.printStackTrace (); // NOI18N
        // problem getting value => ignore this property
        continue;
      }
      String valueType = prop.getValueType ().getName ();
      String encodedValue = encodePrimitiveValue (value);
      if (encodedValue == null) {
        encodedValue = encodeValue (value);
      }
      if (encodedValue == null) {
        // [PENDING - notify problem?]
        continue;
      }
      //System.out.println("saving name="+prop.getName ()+", value="+value); // NOI18N
      buf.append (indent); 

      addLeafElementOpenAttr (
        buf, 
        XML_SYNTHETIC_PROPERTY, 
        new String[] { 
          ATTR_PROPERTY_NAME, 
          ATTR_PROPERTY_TYPE, 
          ATTR_PROPERTY_VALUE, 
          },
        new String[] { 
          prop.getName (), 
          valueType,
          encodedValue,
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
      if (value == null) continue; // such values are not saved
      String valueType = value.getClass ().getName ();
      String encodedValue = encodePrimitiveValue (value);
      if (encodedValue == null) {
        encodedValue = encodeValue (value);
      }
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
            ATTR_AUX_VALUE_TYPE,
            ATTR_AUX_VALUE },
          new String[] { 
            valueName, 
            valueType,
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

  /** Obtains value from given propertyNode for specified RADComponent.
  * @param propertyNode XML node where the property is stored
  * @param radComponent the RADComponent of which the property is to be loaded
  * @return the property value decoded from the node
  */
  private Object getEncodedPropertyValue (org.w3c.dom.Node propertyNode, RADComponent radComponent) 
  throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException 
  {
    org.w3c.dom.NamedNodeMap attrs = propertyNode.getAttributes ();
    if (attrs == null) {
      throw new IOException (); // [PENDING - explanation of problem]
    }
    org.w3c.dom.Node nameNode = attrs.getNamedItem (ATTR_PROPERTY_NAME);
    org.w3c.dom.Node typeNode = attrs.getNamedItem (ATTR_PROPERTY_TYPE);
    org.w3c.dom.Node editorNode = attrs.getNamedItem (ATTR_PROPERTY_EDITOR);
    org.w3c.dom.Node valueNode = attrs.getNamedItem (ATTR_PROPERTY_VALUE);
    org.w3c.dom.Node preCodeNode = attrs.getNamedItem (ATTR_PROPERTY_PRE_CODE);
    org.w3c.dom.Node postCodeNode = attrs.getNamedItem (ATTR_PROPERTY_POST_CODE);

    if (nameNode == null) {
      throw new IOException (); // [PENDING - explanation of problem]
    }

    RADComponent.RADProperty prop = null;
    if (radComponent != null) prop = radComponent.getPropertyByName (nameNode.getNodeValue ());

    if (typeNode == null) {
      if ((preCodeNode == null) || (postCodeNode == null)) {
        throw new IOException (); // [PENDING - explanation of problem]
      } else {
        if (preCodeNode != null) {
          prop.setPreCode (preCodeNode.getNodeValue ());
        }
        if (postCodeNode != null) {
          prop.setPostCode (postCodeNode.getNodeValue ());
        }
      }
      return NO_VALUE; // value is not stored for this property, just the pre/post code
    }

    Class propertyType = findPropertyType (typeNode.getNodeValue ());
    PropertyEditor ed = null;
    if (editorNode != null) {
      Class editorClass = TopManager.getDefault ().currentClassLoader ().loadClass (editorNode.getNodeValue ());
      if (prop != null) {
        ed = FormEditor.createPropertyEditor (editorClass, propertyType, radComponent, prop);
      } else {
        if (Boolean.getBoolean ("netbeans.debug.form")) { // NOI18N
          System.out.println ("Property: "+nameNode.getNodeValue ()+", of component: "+radComponent.getName ()+"["+radComponent.getBeanClass ().getName ()+"] not found."); // NOI18N
        } // [PENDING better notification, localize]
      }
    }
    Object value = null;

    if (prop != null) {
      if (preCodeNode != null) {
        prop.setPreCode (preCodeNode.getNodeValue ());
      }
      if (postCodeNode != null) {
        prop.setPostCode (postCodeNode.getNodeValue ());
      }
    }

    if (valueNode != null) {
      try {
        value = decodePrimitiveValue (valueNode.getNodeValue (), propertyType);
      } catch (IllegalArgumentException e) {
        value = null; // should not happen
      }
    } else {
      if ((ed != null) && (ed instanceof XMLPropertyEditor)) {
        org.w3c.dom.NodeList propChildren = propertyNode.getChildNodes ();
        if ((propChildren != null) && (propChildren.getLength () > 0)) {
          // for forward compatibility - to be able to read props that support XML now
          // but were saved in past when class did not support XML
          boolean isXMLSerialized = false;
          for (int i = 0; i < propChildren.getLength (); i++) {
            if (XML_SERIALIZED_PROPERTY_VALUE.equals (propChildren.item (i).getNodeName ())) {
              isXMLSerialized = true;
              String serValue = findAttribute (propChildren.item (i), ATTR_PROPERTY_VALUE);
              if (serValue != null) {
                value = decodeValue (serValue);
              }
              break;
            }
          }
          if (!isXMLSerialized) {
            for (int i = 0; i < propChildren.getLength (); i++) {
              if (propChildren.item (i).getNodeType () == org.w3c.dom.Node.ELEMENT_NODE) {
                ((XMLPropertyEditor)ed).readFromXML (propChildren.item (i));
                value = ed.getValue ();
                break;
              }
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

    return value;
  }

  private Class  findPropertyType (String type) throws ClassNotFoundException {
    if ("int".equals (type)) return Integer.TYPE; // NOI18N
    else if ("short".equals (type)) return Short.TYPE; // NOI18N
    else if ("byte".equals (type)) return Byte.TYPE; // NOI18N
    else if ("long".equals (type)) return Long.TYPE; // NOI18N
    else if ("float".equals (type)) return Float.TYPE; // NOI18N
    else if ("double".equals (type)) return Double.TYPE; // NOI18N
    else if ("boolean".equals (type)) return Boolean.TYPE; // NOI18N
    else if ("char".equals (type)) return Character.TYPE; // NOI18N
    else {
      return TopManager.getDefault ().currentClassLoader ().loadClass (type);
    }
  }

  /** Decodes a value of given type from the specified String. Supported types are: <UL>
  * <LI> RADConnectionPropertyEditor.RADConnectionDesignValue
  * <LI> Class
  * <LI> String
  * <LI> Integer, Short, Byte, Long, Float, Double, Boolean, Character </UL>
  * @return decoded value
  * @exception IllegalArgumentException thrown if specified object is not of supported type
  */
  private Object decodePrimitiveValue (String encoded, Class type) throws IllegalArgumentException{
    if ("null".equals (encoded)) return null; // NOI18N
    
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
        return TopManager.getDefault ().currentClassLoader ().loadClass (encoded);
      } catch (ClassNotFoundException e) {
        if (Boolean.getBoolean ("netbeans.debug.exceptions")) e.printStackTrace (); // NOI18N
        // will return null as the notification of failure
      }
    }
    throw new IllegalArgumentException ();
  }

  /** Encodes specified value into a String. Supported types are: <UL>
  * <LI> Class
  * <LI> String
  * <LI> Integer, Short, Byte, Long, Float, Double, Boolean, Character </UL>
  * @return String containing encoded value or null if specified object is not of supported type
  */
  private String encodePrimitiveValue (Object value) {
    if ((value instanceof Integer) || 
        (value instanceof Short) ||
        (value instanceof Byte) ||
        (value instanceof Long) ||
        (value instanceof Float) ||
        (value instanceof Double) ||
        (value instanceof Boolean) ||
        (value instanceof Character)) {
      return value.toString ();
    } 

    if (value instanceof String) {
      return (String)value;
    }
     
    if (value instanceof Class) {
      return ((Class)value).getName ();
    }
    
    if (value == null) {
      return "null"; // NOI18N
    }
    
    return null; // is not a primitive type
  }

  /** Decodes a value of from the specified String containing textual representation of serialized stream.
  * @return decoded object
  * @exception IOException thrown if an error occures during deserializing the object
  */
  public static Object decodeValue (String value) throws IOException {
    if ((value == null) || (value.length () == 0)) return null;
    
    char[] bisChars = value.toCharArray ();
    byte[] bytes = new byte[bisChars.length];
    String singleNum = ""; // NOI18N
    int count = 0;
    for (int i = 0; i < bisChars.length; i++) {
      if (',' == bisChars[i]) {
        try {
          bytes[count++] = Byte.parseByte (singleNum);
        } catch (NumberFormatException e) {
          if (Boolean.getBoolean ("netbeans.debug.exceptions")) e.printStackTrace (); // NOI18N
          throw new IOException ();
        }
        singleNum = ""; // NOI18N
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
      return ret;
    } catch (Exception e) {
      if (Boolean.getBoolean ("netbeans.debug.exceptions")) e.printStackTrace (); // NOI18N
      throw new IOException ();
    }
  }

  /** Encodes specified value to a String containing textual representation of serialized stream.
  * @return String containing textual representation of the serialized object
  */
  public static String encodeValue (Object value) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream ();
    try {
      ObjectOutputStream oos = new ObjectOutputStream (bos);
      oos.writeObject (value);
      oos.close ();
    } catch (Exception e) {
      if (Boolean.getBoolean ("netbeans.debug.exceptions")) e.printStackTrace (); // NOI18N
      return null; // problem during serialization
    }
    byte[] bosBytes = bos.toByteArray ();
    StringBuffer sb = new StringBuffer (bosBytes.length);
    for (int i = 0; i < bosBytes.length; i++) {
      if (i != bosBytes.length - 1) {
         sb.append (bosBytes[i]+","); // NOI18N
      } else {
        sb.append (""+bosBytes[i]); // NOI18N
      }
    }
    return sb.toString ();
  }
  
// --------------------------------------------------------------------------------------
// Utility formatting methods
  
  private void addElementOpen (StringBuffer buf, String elementName) {
    buf.append ("<"); // NOI18N
    buf.append (elementName);
    buf.append (">\n"); // NOI18N
  }

  private void addElementOpenAttr (StringBuffer buf, String elementName, String[] attrNames, String[] attrValues) {
    buf.append ("<"); // NOI18N
    buf.append (elementName);
    for (int i = 0; i < attrNames.length; i++) {
      if (attrValues[i] == null) continue;
      buf.append (" "); // NOI18N
      buf.append (attrNames[i]);
      buf.append ("=\""); // NOI18N
      buf.append (encodeToProperXML(attrValues[i]));
      buf.append ("\""); // NOI18N
    }
    buf.append (">\n"); // NOI18N
  }
  
  private void addLeafElementOpenAttr (StringBuffer buf, String elementName, String[] attrNames, String[] attrValues) {
    buf.append ("<"); // NOI18N
    buf.append (elementName);
    for (int i = 0; i < attrNames.length; i++) {
      if (attrValues[i] == null) continue;
      buf.append (" "); // NOI18N
      buf.append (attrNames[i]);
      buf.append ("=\""); // NOI18N
      buf.append (encodeToProperXML(attrValues[i]));
      buf.append ("\""); // NOI18N
    }
    buf.append ("/>\n"); // NOI18N
  }

  private void addElementClose (StringBuffer buf, String elementName) {
    buf.append ("</"); // NOI18N
    buf.append (elementName);
    buf.append (">\n"); // NOI18N
  }

  private void saveNodeIntoText (StringBuffer buf, org.w3c.dom.Node valueNode, String indent) {
    buf.append (indent); 
    buf.append ("<"); // NOI18N
    buf.append (valueNode.getNodeName ());

    org.w3c.dom.NamedNodeMap attributes = valueNode.getAttributes ();

    if (attributes != null) {
      ArrayList attribList = new ArrayList (attributes.getLength ());
      for (int i = 0; i < attributes.getLength (); i++) {
        attribList.add (attributes.item (i));
      }

      // sort the attributes by attribute name
      // probably not necessary, but there is no guarantee that 
      // the order of attributes will remain the same in DOM
      Collections.sort (attribList, new Comparator () {
          public int compare(Object o1, Object o2) {
            org.w3c.dom.Node n1 = (org.w3c.dom.Node)o1;
            org.w3c.dom.Node n2 = (org.w3c.dom.Node)o2;
            return n1.getNodeName ().compareTo (n2.getNodeName ());
          }
        }
      );

      for (Iterator it = attribList.iterator (); it.hasNext ();) {
        org.w3c.dom.Node attrNode = (org.w3c.dom.Node)it.next ();
        String attrName = attrNode.getNodeName (); 
        String attrValue = attrNode.getNodeValue (); 
        
        buf.append (" "); // NOI18N
        buf.append (encodeToProperXML (attrName));
        buf.append ("=\""); // NOI18N
        buf.append (encodeToProperXML (attrValue));
        buf.append ("\""); // NOI18N
      }
    }
    // [PENDING - CNODES, TEXT NODES, ...]

    org.w3c.dom.NodeList children = valueNode.getChildNodes ();
    if ((children == null) || (children.getLength () == 0)) {
      buf.append ("/>\n"); // NOI18N
    } else {
      buf.append (">\n"); // NOI18N
      for (int i = 0; i < children.getLength (); i++) {
        if (children.item (i).getNodeType () == org.w3c.dom.Node.TEXT_NODE) continue; // ignore text nodes
        saveNodeIntoText (buf, children.item (i), indent + ONE_INDENT);
      }
      buf.append (indent);
      buf.append ("</"); // NOI18N
      buf.append (encodeToProperXML (valueNode.getNodeName ()));
      buf.append (">\n"); // NOI18N
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

  private String encodeToProperXML (String text) {
    if (text.indexOf ('&') != -1) text = Utilities.replaceString (text, "&", "&amp;"); // must be the first to prevent changes in the &XX; codes // NOI18N

    if (text.indexOf ('<') != -1) text = Utilities.replaceString (text, "<", "&lt;"); // NOI18N
    if (text.indexOf ('>') != -1) text = Utilities.replaceString (text, ">", "&gt;"); // NOI18N
    if (text.indexOf ('\'') != -1) text = Utilities.replaceString (text, "\'", "&apos;"); // NOI18N
    if (text.indexOf ('\"') != -1) text = Utilities.replaceString (text, "\"", "&quot;"); // NOI18N
    if (text.indexOf ('\n') != -1) text = Utilities.replaceString (text, "\n", "&#xa;"); // NOI18N
    if (text.indexOf ('\t') != -1) text = Utilities.replaceString (text, "\t", "&#x9;"); // NOI18N
    return text;
  }

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
 *  49   Gandalf-post-FCS1.47.1.0    3/20/00  Tran Duc Trung  FIX: wrong form 
 *       character encoding can lead to form data loss
 *  48   Gandalf   1.47        1/13/00  Ian Formanek    NOI18N #2
 *  47   Gandalf   1.46        1/5/00   Ian Formanek    NOI18N
 *  46   Gandalf   1.45        1/2/00   Ian Formanek    Improved serialization 
 *       of primitive types in AUX values and Synthetic properties
 *  45   Gandalf   1.44        12/14/99 Pavel Buzek     #1991
 *  44   Gandalf   1.43        12/9/99  Pavel Buzek     reading propertied that 
 *       support XML but were serialized in older beta version
 *  43   Gandalf   1.42        11/24/99 Pavel Buzek     decodeValue and 
 *       encodeValue made public and static to be used as utility methods
 *  42   Gandalf   1.41        11/16/99 Pavel Buzek     recognition of XML file 
 *       encoding improved
 *  41   Gandalf   1.40        11/15/99 Pavel Buzek     
 *  40   Gandalf   1.39        11/15/99 Pavel Buzek     property for encoding
 *  39   Gandalf   1.38        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  38   Gandalf   1.37        10/6/99  Ian Formanek    Fixed bug 4199 - 
 *       Pre/Post initializer code not added to source code unless property 
 *       editor that spawns Pre/Post code editor has something changed.
 *  37   Gandalf   1.36        9/30/99  Ian Formanek    reflecting XML changes
 *  36   Gandalf   1.35        9/24/99  Ian Formanek    New system of changed 
 *       properties in RADComponent - Fixes bug 3584 - Form Editor should try to
 *       enforce more order in the XML elements in .form.
 *  35   Gandalf   1.34        9/15/99  Ian Formanek    Fixes bug introduced in 
 *       Build 388, which caused layouts not to save their properties, improved 
 *       errors notification
 *  34   Gandalf   1.33        9/14/99  Ian Formanek    RADProperty pre/postCode
 *       is persistent
 *  33   Gandalf   1.32        9/14/99  Ian Formanek    Fixed bug 3287 - Form 
 *       Editor does not correctly reload forms with multi-line String property 
 *       values.
 *  32   Gandalf   1.31        9/14/99  Ian Formanek    Fixed bug 3564 - Form 
 *       editor cannot load forms with non-US characters.
 *  31   Gandalf   1.30        9/12/99  Ian Formanek    FormAwareEditor.setRADComponent
 *        changes
 *  30   Gandalf   1.29        9/7/99   Ian Formanek    Errors notification 
 *       during form load changed
 *  29   Gandalf   1.28        9/2/99   Ian Formanek    Fixed bug 3696 - When 
 *       connection is copied and pasted into form, the initialization code of 
 *       the ConnectionSource component is not correctly generated. and 3695 - 
 *       Modified properties with null value are not restored correctly when a 
 *       form is reloaded.
 *  28   Gandalf   1.27        8/19/99  Ian Formanek    No semantic change
 *  27   Gandalf   1.26        8/9/99   Ian Formanek    Used currentClassLoader 
 *       to fix problems with loading beans only present in repository
 *  26   Gandalf   1.25        8/6/99   Ian Formanek    Survives when storeToXML
 *       returns null
 *  25   Gandalf   1.24        8/6/99   Ian Formanek    displaying error log
 *  24   Gandalf   1.23        8/2/99   Ian Formanek    NonVisuals element is 
 *       not saved if empty
 *  23   Gandalf   1.22        8/2/99   Ian Formanek    Proper encoding of 
 *       special characters into XML (<, >, ", ', &)
 *  22   Gandalf   1.21        8/1/99   Ian Formanek    Really does what last 
 *       checkin should have done
 *  21   Gandalf   1.20        8/1/99   Ian Formanek    Improved creation of 
 *       property editors, removed debug messages
 *  20   Gandalf   1.19        7/25/99  Ian Formanek    Variables management 
 *       moved to RADComponent
 *  19   Gandalf   1.18        7/23/99  Ian Formanek    Works with 
 *       RADConnectionPropertyEditor
 *  18   Gandalf   1.17        7/20/99  Ian Formanek    Persistence of menus
 *  17   Gandalf   1.16        7/18/99  Ian Formanek    More correct handling of
 *       errors during loading form
 *  16   Gandalf   1.15        7/14/99  Ian Formanek    Fixed saveNodeIntoText 
 *       method
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
