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

import java.awt.*;
import java.beans.*;

import org.openide.explorer.propertysheet.editors.XMLPropertyEditor;

/**
* RADConnectionPropertyEditor is a property editor for ListModel, which encapsulates a connection to existing ListModel beans on the form
*
* @author  Ian Formanek
*/
public class RADConnectionPropertyEditor extends Object implements PropertyEditor, FormAwareEditor, XMLPropertyEditor {

  protected PropertyChangeSupport support;
  private Class propertyType;
  private RADComponent rcomponent;
  private RADConnectionDesignValue emptyValue = null;
  private RADConnectionDesignValue currentValue = emptyValue;

  /** Creates a new RADConnectionPropertyEditor */
  public RADConnectionPropertyEditor (Class propertyType) {
    support = new PropertyChangeSupport (this);
    this.propertyType = propertyType;
  }

  /** Called to set the RADComponent for which this property editor was created.
  * @param node the RADComponent for which this property editor was created
  */
  public void setRADComponent (RADComponent rcomp) {
    rcomponent = rcomp;
  }

// -----------------------------------------------------------------------------
// PropertyEditor implementation

  public Object getValue () {
    return currentValue;
  }

  public void setValue (Object value) {
    if (value instanceof RADConnectionDesignValue) {
      currentValue = (RADConnectionDesignValue)value;
    } else {
      currentValue = emptyValue;
    }
    support.firePropertyChange ("", null, null);
  }

	public void setAsText (String string) {
	}

  public String getAsText () {
    if (currentValue != null)
      return currentValue.getName ();
    else return "<Not Set>"; // [PENDING]
  }

	public String[] getTags () 
	{
		return null;
	}

  public boolean isPaintable () {
    return false;
  }

  public void paintValue (Graphics g, Rectangle rectangle) {
  }

  public boolean supportsCustomEditor () {
    return true;
  }

  public java.awt.Component getCustomEditor () {
    ParametersPicker pp = new ParametersPicker (rcomponent.getFormManager (), rcomponent, propertyType);
    pp.setPropertyValue (currentValue);
    return pp;
  }

  public String getJavaInitializationString () {
    if (currentValue != null) {
      switch (currentValue.type) {
        case RADConnectionDesignValue.TYPE_VALUE: return currentValue.value;     
        case RADConnectionDesignValue.TYPE_CODE: return currentValue.userCode;
        case RADConnectionDesignValue.TYPE_PROPERTY: return currentValue.radComponentName + "." + currentValue.property.getReadMethod ().getName () + " ()"; // [PENDING]
        case RADConnectionDesignValue.TYPE_METHOD: return currentValue.radComponentName + "." + currentValue.methodName + " ()";
      }
    }
    return null;
  }

  public void addPropertyChangeListener (PropertyChangeListener propertyChangeListener) {
    support.addPropertyChangeListener (propertyChangeListener);
  }

  public void removePropertyChangeListener (PropertyChangeListener propertyChangeListener) {
    support.removePropertyChangeListener (propertyChangeListener);
  }


  public static class RADConnectionDesignValue implements FormDesignValue, java.io.Serializable {
    public final static int TYPE_PROPERTY = 0;
    public final static int TYPE_METHOD = 1;
    public final static int TYPE_CODE = 2;
    public final static int TYPE_VALUE = 3;

    /** Determines the type of connection design value */
    int type;
    
    transient RADComponent radComponent = null; // used if type = TYPE_PROPERTY or TYPE_METHOD
    String radComponentName = null;             // used if type = TYPE_PROPERTY or TYPE_METHOD

    transient MethodDescriptor method = null;             // used if type = TYPE_METHOD
    String methodName = null;                   // used if type = TYPE_METHOD
    transient PropertyDescriptor property = null;         // used if type = TYPE_PROPERTY
    String propertyName = null;                 // used if type = TYPE_PROPERTY
    String userCode = null;                     // used if type = TYPE_CODE
    String value = null;                        // used if type = TYPE_VALUE
    String requiredTypeName = null;             // used if type = TYPE_VALUE
    
    RADConnectionDesignValue (RADComponent comp, MethodDescriptor md) {
      radComponent = comp;
      radComponentName = radComponent.getName ();
      method = md;
      methodName = md.getName ();
      type = TYPE_METHOD;
    }

    RADConnectionDesignValue (RADComponent comp, PropertyDescriptor pd) {
      radComponent = comp;
      radComponentName = radComponent.getName ();
      property = pd;
      propertyName = pd.getName ();
      // [PENDING - restore on deserialization]
      type = TYPE_PROPERTY;
    }

    RADConnectionDesignValue (Class requiredType, String valueText) {
      this.requiredTypeName = requiredType.getName ();
      this.value = value;
      type = TYPE_VALUE;
    }

    RADConnectionDesignValue (String userCode) {
      this.userCode = userCode;
      type = TYPE_CODE;
    }

    String getName () {
      switch (type) {
        case TYPE_PROPERTY: return "<"+radComponentName+"."+propertyName+">"; 
        case TYPE_METHOD: return "<"+radComponentName+"."+methodName+">"; 
        case TYPE_VALUE: return "<Value: " + value + ">"; // [PENDING localize]
        case TYPE_CODE: return "<User Code>"; // [PENDING localize]
      }
      throw new InternalError ();
    }

    /** Provides a value which should be used during design-time
    * as the real property value on the bean instance.
    * E.g. the ResourceBundle String would provide the real value
    * of the String from the resource bundle, so that the design-time
    * representation reflects the real code being generated.
    * @param radComponent the radComponent in which this property is used
    * @return the real property value to be used during design-time
    */
    public Object getDesignValue (RADComponent radComponent) {
      switch (type) {
        case TYPE_PROPERTY: return FormDesignValue.IGNORED_VALUE;  // [PENDING]
        case TYPE_METHOD: return FormDesignValue.IGNORED_VALUE; // [PENDING]
        case TYPE_VALUE: return FormDesignValue.IGNORED_VALUE; // [PENDING]
        case TYPE_CODE: return FormDesignValue.IGNORED_VALUE;
      }
      throw new InternalError ();
    }
  }

//--------------------------------------------------------------------------
// XMLPropertyEditor implementation

  public static final String XML_CONNECTION = "Connection";

  public static final String ATTR_TYPE = "type";
  public static final String ATTR_COMPONENT = "component";
  public static final String ATTR_NAME = "name";
  public static final String ATTR_CODE = "code";
  public static final String ATTR_VALUE = "value";

  public static final String VALUE_VALUE = "value";
  public static final String VALUE_PROPERTY = "property";
  public static final String VALUE_METHOD = "method";
  public static final String VALUE_CODE = "code";

  /** Called to load property value from specified XML subtree. If succesfully loaded, 
  * the value should be available via the getValue method.
  * An IOException should be thrown when the value cannot be restored from the specified XML element
  * @param element the XML DOM element representing a subtree of XML from which the value should be loaded
  * @exception IOException thrown when the value cannot be restored from the specified XML element
  */
  public void readFromXML (org.w3c.dom.Node element) throws java.io.IOException {
    if (!XML_CONNECTION.equals (element.getNodeName ())) {
      throw new java.io.IOException ();
    }
    org.w3c.dom.NamedNodeMap attributes = element.getAttributes ();
    try {
      String typeString = attributes.getNamedItem (ATTR_TYPE).getNodeValue ();
      int type = RADConnectionDesignValue.TYPE_CODE;
      if (VALUE_VALUE.equals (typeString)) {
        String value = attributes.getNamedItem (ATTR_VALUE).getNodeValue ();
//        setValue (new RADConnectionDesignValue ()); // [PENDING]
      } else if (VALUE_PROPERTY.equals (typeString)) {
        String component = attributes.getNamedItem (ATTR_COMPONENT).getNodeValue ();
        String name = attributes.getNamedItem (ATTR_NAME).getNodeValue ();
//        setValue (new RADConnectionDesignValue (rcomponent, )); // [PENDING]
      } else if (VALUE_METHOD.equals (typeString)) {
        String component = attributes.getNamedItem (ATTR_COMPONENT).getNodeValue ();
        String name = attributes.getNamedItem (ATTR_NAME).getNodeValue ();
//        setValue (new RADConnectionDesignValue (rcomponent, )); // [PENDING]
      } else {
        String code = attributes.getNamedItem (ATTR_CODE).getNodeValue ();
        setValue (new RADConnectionDesignValue (code));
      }


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
    org.w3c.dom.Element el = doc.createElement (XML_CONNECTION);
    String typeString;
    switch (currentValue.type) {
      case RADConnectionDesignValue.TYPE_VALUE: typeString = VALUE_VALUE; break;
      case RADConnectionDesignValue.TYPE_PROPERTY: typeString = VALUE_PROPERTY; break;
      case RADConnectionDesignValue.TYPE_METHOD: typeString = VALUE_METHOD; break;
      case RADConnectionDesignValue.TYPE_CODE: 
      default:
           typeString = VALUE_CODE; break;
    }
    el.setAttribute (ATTR_TYPE, typeString);
    switch (currentValue.type) {
      case RADConnectionDesignValue.TYPE_VALUE: 
           el.setAttribute (ATTR_VALUE, currentValue.value);
           break;
      case RADConnectionDesignValue.TYPE_PROPERTY:
           el.setAttribute (ATTR_COMPONENT, currentValue.radComponentName);
           el.setAttribute (ATTR_NAME, currentValue.propertyName);
           break;
      case RADConnectionDesignValue.TYPE_METHOD: 
           el.setAttribute (ATTR_COMPONENT, currentValue.radComponentName);
           el.setAttribute (ATTR_NAME, currentValue.propertyName);
           break;
      case RADConnectionDesignValue.TYPE_CODE: 
           el.setAttribute (ATTR_CODE, org.openide.util.Utilities.replaceString (currentValue.userCode, "\n", "\\n"));
           break;
    }

    return el;
  }

}

/*
 * Log
 */
