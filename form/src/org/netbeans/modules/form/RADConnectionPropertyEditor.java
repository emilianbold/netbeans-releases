/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form;

import java.awt.*;
import java.beans.*;
import java.text.MessageFormat;

import org.openide.explorer.propertysheet.editors.XMLPropertyEditor;

/**
 * RADConnectionPropertyEditor is a property editor for ListModel, which
 * encapsulates a connection to existing ListModel beans on the form
 *
 * @author Ian Formanek
 */

public class RADConnectionPropertyEditor
    implements PropertyEditor,
               FormAwareEditor,
               XMLPropertyEditor,
               NamedPropertyEditor
{
    protected PropertyChangeSupport support;
    private Class propertyType;
    private RADComponent rcomponent;
    private RADConnectionDesignValue emptyValue = null;
    private RADConnectionDesignValue designValue = emptyValue;
    private Object realValue = null;

    /** Creates a new RADConnectionPropertyEditor */
    public RADConnectionPropertyEditor(Class propertyType) {
        support = new PropertyChangeSupport(this);
        this.propertyType = propertyType;
    }

    /** If a property editor or customizer implements the FormAwareEditor
     * interface, this method is called immediately after the PropertyEditor
     * instance is created or the Customizer is obtained from getCustomizer().
     * @param component The RADComponent representing the JavaBean being edited by this 
     *                  property editor or customizer
     * @param property  The RADProperty being edited by this property editor or null 
     *                  if this interface is implemented by a customizer
     */

    public void setRADComponent(RADComponent rcomp, RADComponent.RADProperty rprop) {
        rcomponent = rcomp;
    }

    // -----------------------------------------------------------------------------
    // PropertyEditor implementation

    public Object getValue() {
        return designValue != null ? designValue : realValue;
    }

    public void setValue(Object value) {
        if (value instanceof RADConnectionDesignValue) {
            designValue =(RADConnectionDesignValue)value;
        } else {
            designValue = emptyValue;
            realValue = value;
        }
        support.firePropertyChange("", null, null); // NOI18N
    }

    public void setAsText(String string) {
    }

    public String getAsText() {
        return null;
    }

    public String[] getTags() {
        return null;
    }

    public boolean isPaintable() {
        return true;
    }

    public void paintValue(Graphics g, Rectangle rectangle) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(getValueString(), rectangle.x,
                            rectangle.y + (rectangle.height - fm.getHeight()) / 2 + fm.getAscent());
    }

    public boolean supportsCustomEditor() {
        return true;
    }

    public java.awt.Component getCustomEditor() {
        ParametersPicker pp = new ParametersPicker(rcomponent.getFormManager(), rcomponent, propertyType);
        pp.setPropertyValue(designValue);
        return pp;
    }

    public String getJavaInitializationString() {
        if (designValue != null) {
            if (designValue.needsInit) {
                designValue.initialize();
            }

            switch (designValue.type) {
                case RADConnectionDesignValue.TYPE_VALUE:
                    if ("java.lang.String".equals(designValue.requiredTypeName)) return "\""+designValue.value+"\""; // NOI18N
                    else if ("long".equals(designValue.requiredTypeName)) return designValue.value+"L"; // NOI18N
                    else if ("float".equals(designValue.requiredTypeName)) return designValue.value+"F"; // NOI18N
                    else if ("double".equals(designValue.requiredTypeName)) return designValue.value+"D"; // NOI18N
                    else return designValue.value;
                case RADConnectionDesignValue.TYPE_CODE: return designValue.userCode;
                case RADConnectionDesignValue.TYPE_PROPERTY:
                    PropertyDescriptor pd = designValue.getProperty();
                    if (pd == null) return null; // failed to initialize => do not generate code
                    else {
                        if (designValue.radComponent instanceof FormContainer) {
                            return pd.getReadMethod().getName() + "()"; // [FUTURE: Handle indexed properties] // NOI18N
                        } else {
                            return designValue.radComponentName + "." + pd.getReadMethod().getName() + "()"; // [FUTURE: Handle indexed properties] // NOI18N
                        }
                    }
                case RADConnectionDesignValue.TYPE_METHOD:
                    if (designValue.radComponent instanceof FormContainer) {
                        return designValue.methodName + "()"; // NOI18N
                    } else {
                        return designValue.radComponentName + "." + designValue.methodName + "()"; // NOI18N
                    }
                case RADConnectionDesignValue.TYPE_BEAN:
                    if (designValue.radComponent instanceof FormContainer) {
                        return "this"; // NOI18N
                    } else {
                        return designValue.radComponentName;
                    }
            }
        }
        return null;
    }

    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        support.addPropertyChangeListener(propertyChangeListener);
    }

    public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        support.removePropertyChangeListener(propertyChangeListener);
    }

    // ------------------------------------------
    // NamedPropertyEditor implementation

    /** @return display name of the property editor */
    public String getDisplayName() {
        return FormEditor.getFormBundle().getString("CTL_RADConn_DisplayName");
    }

    // ------------------------------------------
    private String getValueString() {
        String str;
        if (designValue != null) {
            str = designValue.getName();
        }
        else if (realValue != null) {
            if (realValue instanceof Number
                  || realValue instanceof Boolean
                  || realValue instanceof String
                  || realValue instanceof Character)
                str = realValue.toString();
            else
                str = realValue.getClass().isArray() ?
                        "[" + FormEditor.getFormBundle().getString("CTL_ArrayOf") + " " 
                            + realValue.getClass().getComponentType().getName() + "]"
                        :
                        "["+org.openide.util.Utilities.getShortClassName(realValue.getClass())+"]";
        }
        else
            str = "null"; //FormEditor.getFormBundle().getString("CTL_CONNECTION_NOT_SET"); // NOI18N

        return str;
    }


    // ------------------------------------------
    // implementation class for FormDesignValue

    public static class RADConnectionDesignValue implements FormDesignValue, java.io.Serializable {
        public final static int TYPE_PROPERTY = 0;
        public final static int TYPE_METHOD = 1;
        public final static int TYPE_CODE = 2;
        public final static int TYPE_VALUE = 3;
        public final static int TYPE_BEAN = 4;

        /** Determines the type of connection design value */
        int type;

        private transient RADComponent radComponent = null; // used if type = TYPE_PROPERTY or TYPE_METHOD or TYPE_BEAN
        String radComponentName = null;             // used if type = TYPE_PROPERTY or TYPE_METHOD

        private transient MethodDescriptor method = null;             // used if type = TYPE_METHOD
        String methodName = null;                   // used if type = TYPE_METHOD
        private transient PropertyDescriptor property = null;         // used if type = TYPE_PROPERTY
        String propertyName = null;                 // used if type = TYPE_PROPERTY
        String userCode = null;                     // used if type = TYPE_CODE
        String value = null;                        // used if type = TYPE_VALUE
        String requiredTypeName = null;             // used if type = TYPE_VALUE

        transient private boolean needsInit = false; // used for deserialization init if type = TYPE_PROPERTY or TYPE_METHOD or TYPE_BEAN
        transient private FormManager2 formManager;  // used for deserialization init if type = TYPE_PROPERTY or TYPE_METHOD or TYPE_BEAN

        static final long serialVersionUID =147134837271021412L;
        RADConnectionDesignValue(RADComponent comp) {
            radComponent = comp;
            radComponentName = radComponent.getName();
            type = TYPE_BEAN;
        }

        RADConnectionDesignValue(RADComponent comp, MethodDescriptor md) {
            radComponent = comp;
            radComponentName = radComponent.getName();
            method = md;
            methodName = md.getName();
            type = TYPE_METHOD;
        }

        RADConnectionDesignValue(RADComponent comp, PropertyDescriptor pd) {
            radComponent = comp;
            radComponentName = radComponent.getName();
            property = pd;
            propertyName = pd.getName();
            type = TYPE_PROPERTY;
        }

        RADConnectionDesignValue(String reqTypeName, String valueText) {
            this.requiredTypeName = reqTypeName;
            this.value = valueText;
            type = TYPE_VALUE;
        }

        private RADConnectionDesignValue(String compName, int valueType, String name, FormManager2 manager) {
            radComponentName = compName;
            formManager = manager;
            if (valueType == TYPE_PROPERTY) {
                needsInit = true;
                type = TYPE_PROPERTY;
                propertyName = name;
            } else if (valueType == TYPE_METHOD) {
                needsInit = true;
                type = TYPE_METHOD;
                methodName = name;
            } else if (valueType == TYPE_BEAN) {
                needsInit = true;
                type = TYPE_BEAN;
            } else throw new IllegalArgumentException();
        }

        public RADConnectionDesignValue(Class requiredType, String valueText) {
            this.requiredTypeName = requiredType.getName();
            this.value = valueText;
            type = TYPE_VALUE;
        }

        public RADConnectionDesignValue(String userCode) {
            this.userCode = userCode;
            type = TYPE_CODE;
        }

        String getName() {
            switch (type) {
                case TYPE_PROPERTY: return MessageFormat.format(FormEditor.getFormBundle().getString("FMT_PROPERTY_CONN"), new Object[] { radComponentName, propertyName });
                case TYPE_METHOD: return MessageFormat.format(FormEditor.getFormBundle().getString("FMT_METHOD_CONN"), new Object[] { radComponentName, methodName });
                case TYPE_VALUE: return MessageFormat.format(FormEditor.getFormBundle().getString("FMT_VALUE_CONN"), new Object[] { value });
                case TYPE_CODE: return FormEditor.getFormBundle().getString("CTL_CODE_CONN");
                case TYPE_BEAN: return MessageFormat.format(FormEditor.getFormBundle().getString("FMT_BEAN_CONN"), new Object[] { radComponentName });
            }
            throw new InternalError();
        }

        public PropertyDescriptor getProperty() {
            if (needsInit) {
                if (!initialize()) return null;
            }
            return property;
        }

        public MethodDescriptor getMethod() {
            if (needsInit) {
                if (!initialize()) return null;
            }
            return method;
        }

        public String getCode() {
            if (needsInit) {
                if (!initialize()) return null;
            }
            return userCode;
        }

        public String getValue() {
            if (needsInit) {
                if (!initialize()) return null;
            }
            return value;
        }

        public RADComponent getRADComponent() {
            if (needsInit) {
                if (!initialize()) return null;
            }
            return radComponent;
        }

        private boolean initialize() {
            boolean retVal = false;
            radComponent = formManager.findRADComponent(radComponentName);
            if (radComponent != null) {
                if (type == TYPE_BEAN) { // bean
                    retVal = true;
                } else if (type == TYPE_PROPERTY) { // property
                    PropertyDescriptor[] componentsProps = radComponent.getBeanInfo().getPropertyDescriptors();
                    for (int i = 0; i < componentsProps.length; i++) {
                        if (componentsProps[i].getName().equals(propertyName)) {
                            property = componentsProps[i];
                            retVal = true;
                            break;
                        }
                    } // if the property of given name cannot be found => ignore
                } else { // method
                    MethodDescriptor[] componentMethods = radComponent.getBeanInfo().getMethodDescriptors();
                    for (int i = 0; i < componentMethods.length; i++) {
                        if (componentMethods[i].getName().equals(methodName)) {
                            method = componentMethods[i];
                            retVal = true;
                            break;
                        }
                    } // if the property of given name cannot be found => ignore
                }
            } // if the component cannot be found, simply ignore it
            if (retVal) needsInit = false;
            return retVal;
        }

        /** Provides a value which should be used during design-time
         * as the real property value on the bean instance.
         * E.g. the ResourceBundle String would provide the real value
         * of the String from the resource bundle, so that the design-time
         * representation reflects the real code being generated.
         * @param radComponent the radComponent in which this property is used
         * @return the real property value to be used during design-time
         */
        public Object getDesignValue(RADComponent radComponent) {
            /*      if (needsInit) {
                    if (!initialize()) {
                    return IGNORED_VALUE; // failed to initialize
                    }
                    } */
            switch (type) {
                case TYPE_PROPERTY:
                    return FormDesignValue.IGNORED_VALUE; // [PENDING: use the value during design time]
                    /*            try {
                                  Object value = property.getReadMethod().invoke(radComponent.getBeanInstance(), new Object[0]);
                                  return value;
                                  } catch (Exception e) {
                                  // in case of failure do not provide the value during design time
                                  return FormDesignValue.IGNORED_VALUE;
                                  }*/ // [PENDING]
                case TYPE_METHOD:
                    return FormDesignValue.IGNORED_VALUE; // [PENDING: use the value during design time]
                    /*            try {
                                  Object value = method.getMethod().invoke(radComponent.getBeanInstance(), new Object[0]);
                                  return value;
                                  } catch (Exception e) {
                                  // in case of failure do not provide the value during design time
                                  return FormDesignValue.IGNORED_VALUE;
                                  } */ // [PENDING]
                case TYPE_VALUE:
                    return parseValue(requiredTypeName, value);
                case TYPE_BEAN:
                    return FormDesignValue.IGNORED_VALUE; // [PENDING: use the value during design time]
                case TYPE_CODE:
                    return FormDesignValue.IGNORED_VALUE; // [T.P.] code is not a real value
                default:
                    return FormDesignValue.IGNORED_VALUE;
            }
        }

        /** Returns type of this connection design value.
        */
        public int getType() {
            return type;
        }
    } // end of inner class

    private static Object parseValue(String typeName, String value) {
        try {
            if ("java.lang.String".equals(typeName)) { // NOI18N
                return value;
            } else if ("int".equals(typeName)) { // NOI18N
                return Integer.valueOf(value);
            } else if ("short".equals(typeName)) { // NOI18N
                return Short.valueOf(value);
            } else if ("long".equals(typeName)) { // NOI18N
                return Long.valueOf(value);
            } else if ("byte".equals(typeName)) { // NOI18N
                return Byte.valueOf(value);
            } else if ("float".equals(typeName)) { // NOI18N
                return Float.valueOf(value);
            } else if ("double".equals(typeName)) { // NOI18N
                return Double.valueOf(value);
            } else if ("boolean".equals(typeName)) { // NOI18N
                return Boolean.valueOf(value);
            } else if ("char".equals(typeName)) { // NOI18N
                if (value.length() > 0) return new Character(value.charAt(0));
            }
            return FormDesignValue.IGNORED_VALUE;
        } catch (Exception e) {
            // some problem => use ignored value
            return FormDesignValue.IGNORED_VALUE;
        }
    }

    //--------------------------------------------------------------------------
    // XMLPropertyEditor implementation

    public static final String XML_CONNECTION = "Connection"; // NOI18N

    public static final String ATTR_TYPE = "type"; // NOI18N
    public static final String ATTR_COMPONENT = "component"; // NOI18N
    public static final String ATTR_NAME = "name"; // NOI18N
    public static final String ATTR_CODE = "code"; // NOI18N
    public static final String ATTR_VALUE = "value"; // NOI18N
    public static final String ATTR_REQUIRED_TYPE = "valueType"; // NOI18N

    public static final String VALUE_VALUE = "value"; // NOI18N
    public static final String VALUE_PROPERTY = "property"; // NOI18N
    public static final String VALUE_METHOD = "method"; // NOI18N
    public static final String VALUE_BEAN = "bean"; // NOI18N
    public static final String VALUE_CODE = "code"; // NOI18N

    /** Called to load property value from specified XML subtree. If succesfully loaded,
     * the value should be available via the getValue method.
     * An IOException should be thrown when the value cannot be restored from the specified XML element
     * @param element the XML DOM element representing a subtree of XML from which the value should be loaded
     * @exception IOException thrown when the value cannot be restored from the specified XML element
     */
    public void readFromXML(org.w3c.dom.Node element) throws java.io.IOException {
        if (!XML_CONNECTION.equals(element.getNodeName())) {
            throw new java.io.IOException();
        }
        org.w3c.dom.NamedNodeMap attributes = element.getAttributes();
        try {
            String typeString = attributes.getNamedItem(ATTR_TYPE).getNodeValue();
            int type = RADConnectionDesignValue.TYPE_CODE;
            if (VALUE_VALUE.equals(typeString)) {
                String value = attributes.getNamedItem(ATTR_VALUE).getNodeValue();
                String valueType = attributes.getNamedItem(ATTR_REQUIRED_TYPE).getNodeValue();
                setValue(new RADConnectionDesignValue(valueType, value));
/*                try {
                    Class reqType = TopManager.getDefault().currentClassLoader().loadClass(valueType);
                    setValue(new RADConnectionDesignValue(reqType, value));
                } catch (Exception e) {
                    // ignore failures... and use no conn instead
                } */

            } else if (VALUE_PROPERTY.equals(typeString)) {
                String component = attributes.getNamedItem(ATTR_COMPONENT).getNodeValue();
                String name = attributes.getNamedItem(ATTR_NAME).getNodeValue();
                setValue(new RADConnectionDesignValue(component, RADConnectionDesignValue.TYPE_PROPERTY, name, rcomponent.getFormManager()));

            } else if (VALUE_METHOD.equals(typeString)) {
                String component = attributes.getNamedItem(ATTR_COMPONENT).getNodeValue();
                String name = attributes.getNamedItem(ATTR_NAME).getNodeValue();
                setValue(new RADConnectionDesignValue(component, RADConnectionDesignValue.TYPE_METHOD, name, rcomponent.getFormManager()));

            } else if (VALUE_BEAN.equals(typeString)) {
                String component = attributes.getNamedItem(ATTR_COMPONENT).getNodeValue();
                setValue(new RADConnectionDesignValue(component, RADConnectionDesignValue.TYPE_BEAN, null, rcomponent.getFormManager()));

            } else {
                String code = attributes.getNamedItem(ATTR_CODE).getNodeValue();
                setValue(new RADConnectionDesignValue(code));
            }
        } catch (NullPointerException e) {
            if (System.getProperty("netbeans.debug.exceptions") != null) {
                e.printStackTrace();
            }
            throw new java.io.IOException();
        }
    }

    /** Called to store current property value into XML subtree. The property
     * value should be set using the setValue method prior to calling this method.
     * @param doc The XML document to store the XML in - should be used for creating nodes only
     * @return the XML DOM element representing a subtree of XML from which the
     * value should be loaded
     */

    public org.w3c.dom.Node storeToXML(org.w3c.dom.Document doc) {
        org.w3c.dom.Element el = doc.createElement(XML_CONNECTION);
        if (designValue == null) return null;
        String typeString;
        switch (designValue.type) {
            case RADConnectionDesignValue.TYPE_VALUE: typeString = VALUE_VALUE; break;
            case RADConnectionDesignValue.TYPE_PROPERTY: typeString = VALUE_PROPERTY; break;
            case RADConnectionDesignValue.TYPE_METHOD: typeString = VALUE_METHOD; break;
            case RADConnectionDesignValue.TYPE_BEAN: typeString = VALUE_BEAN; break;
            case RADConnectionDesignValue.TYPE_CODE:
            default:
                typeString = VALUE_CODE; break;
        }
        el.setAttribute(ATTR_TYPE, typeString);
        switch (designValue.type) {
            case RADConnectionDesignValue.TYPE_VALUE:
                el.setAttribute(ATTR_VALUE, designValue.value);
                el.setAttribute(ATTR_REQUIRED_TYPE, designValue.requiredTypeName);
                break;
            case RADConnectionDesignValue.TYPE_PROPERTY:
                el.setAttribute(ATTR_COMPONENT, designValue.radComponentName);
                el.setAttribute(ATTR_NAME, designValue.propertyName);
                break;
            case RADConnectionDesignValue.TYPE_METHOD:
                el.setAttribute(ATTR_COMPONENT, designValue.radComponentName);
                el.setAttribute(ATTR_NAME, designValue.methodName);
                break;
            case RADConnectionDesignValue.TYPE_BEAN:
                el.setAttribute(ATTR_COMPONENT, designValue.radComponentName);
                break;
            case RADConnectionDesignValue.TYPE_CODE:
                el.setAttribute(ATTR_CODE, designValue.userCode);
                break;
        }

        return el;
    }
}
