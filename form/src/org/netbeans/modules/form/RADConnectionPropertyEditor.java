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

/**
* RADConnectionPropertyEditor is a property editor for ListModel, which encapsulates a connection to existing ListModel beans on the form
*
* @author  Ian Formanek
*/
public class RADConnectionPropertyEditor extends Object implements PropertyEditor, FormAwareEditor {

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
}

/*
 * Log
 */
