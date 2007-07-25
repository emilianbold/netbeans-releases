/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.form;

import java.awt.*;
import java.beans.*;

import org.openide.explorer.propertysheet.*;
import org.openide.explorer.propertysheet.editors.*;

/**
 * RADConnectionPropertyEditor is a special property editor that can set
 * properties (of any type) indirectly - e.g. as a property of some bean
 * or as the result of a method call or as a code entered by the user, etc.
 * Only the source code is generated for such property (usually) so it
 * doesn't take effect until runtime.
 *
 * @author Ian Formanek
 */

public class RADConnectionPropertyEditor
    implements PropertyEditor, 
               FormAwareEditor,
               XMLPropertyEditor,
               NamedPropertyEditor
{
    public enum Type { FormConnection, CustomCode }
    private Type editorType;

    protected PropertyChangeSupport support = new PropertyChangeSupport(this);
    private Class propertyType;
    private FormModel formModel = null;
    private FormProperty property;
    private RADConnectionDesignValue designValue = null;
    private Object realValue = null;

    /** Creates a new RADConnectionPropertyEditor */
    public RADConnectionPropertyEditor(Class propertyType) {
        this.propertyType = propertyType;
        this.editorType = Type.FormConnection;
    }

    public RADConnectionPropertyEditor(Class propertyType, Type editorType) {
        this.propertyType = propertyType;
        this.editorType = editorType;
    }

    public Type getEditorType() {
        return editorType;
    }

    /** If a property editor or customizer implements the FormAwareEditor
     * interface, this method is called immediately after the PropertyEditor
     * instance is created or the Customizer is obtained from getCustomizer().
     * @param model  The FormModel representing data of opened form.
     */
    public void setContext(FormModel model, FormProperty prop) {
        formModel = model;
        property = prop;
    }

    // -----------------------------------------------------------------------------
    // PropertyEditor implementation

    public Object getValue() {
        
        return designValue != null ? designValue : realValue;
    }

    public void setValue(Object value) {
        if (value instanceof RADConnectionDesignValue) {
            designValue =(RADConnectionDesignValue)value;
            editorType = designValue.getType() == RADConnectionDesignValue.TYPE_CODE ?
                Type.CustomCode : Type.FormConnection;
            if (editorType == Type.CustomCode) {
                String code = designValue.getCode();
                if ((code == null) || (code.trim().length() == 0)) { 
                    // Issue 101617
                    setValue(property.getDefaultValue());
                }
            }
        } else {
            designValue = null;
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
        if (editorType == Type.FormConnection) {
            ConnectionCustomEditor cust = new ConnectionCustomEditor(this, formModel, propertyType);
            cust.setValue(designValue);
            return cust;
        }
        else {
            CodeCustomEditor cust = new CodeCustomEditor(this, formModel, property);
            cust.setValue(designValue);
            return cust;
        }
    }

    public String getJavaInitializationString() {
        if (designValue != null) {
            if (designValue.needsInit)
                designValue.initialize();

            if (designValue.type == RADConnectionDesignValue.TYPE_VALUE) {
                if ("java.lang.String".equals(designValue.requiredTypeName)) // NOI18N
                    return "\""+designValue.value+"\""; // NOI18N
                else if ("long".equals(designValue.requiredTypeName)) // NOI18N
                    return designValue.value+"L"; // NOI18N
                else if ("float".equals(designValue.requiredTypeName)) // NOI18N
                    return designValue.value+"F"; // NOI18N
                else if ("double".equals(designValue.requiredTypeName)) // NOI18N
                    return designValue.value+"D"; // NOI18N
                else if ("char".equals(designValue.requiredTypeName)) // NOI18N
                    return "\'"+designValue.value+"\'"; // NOI18N
                else return designValue.value;
            }
            else if (designValue.type == RADConnectionDesignValue.TYPE_CODE)
                return designValue.userCode;
            else {
                if (designValue.radComponent == null
                        || designValue.radComponent.getCodeExpression() == null)
                    return null; // invalid component (probably deleted)

                if (designValue.type ==  RADConnectionDesignValue.TYPE_PROPERTY) {
                    PropertyDescriptor pd = designValue.getProperty();
                    if (pd == null) return null; // failed to initialize => do not generate code
                    else {
                        if (designValue.radComponent == formModel.getTopRADComponent()) {
                            return pd.getReadMethod().getName() + "()"; // [FUTURE: Handle indexed properties] // NOI18N
                        } else {
                            return designValue.radComponent.getName() + "." + pd.getReadMethod().getName() + "()"; // [FUTURE: Handle indexed properties] // NOI18N
                        }
                    }
                }
                else if (designValue.type == RADConnectionDesignValue.TYPE_METHOD) {
                    if (designValue.radComponent == formModel.getTopRADComponent()) {
                        return designValue.methodName + "()"; // NOI18N
                    } else {
                        return designValue.radComponent.getName() + "." + designValue.methodName + "()"; // NOI18N
                    }
                }
                else if (designValue.type ==  RADConnectionDesignValue.TYPE_BEAN) {
                    if (designValue.radComponent == formModel.getTopRADComponent()) {
                        return "this"; // NOI18N
                    } else {
                        return designValue.radComponent.getName();
                    }
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
        return FormUtils.getBundleString(editorType == Type.FormConnection ?
                "CTL_FormConnection_DisplayName" : "CTL_CustomCode_DisplayName"); // NOI18N
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
                        "[" + FormUtils.getBundleString("CTL_ArrayOf") + " "  // NOI18N
                            + realValue.getClass().getComponentType().getName() + "]" // NOI18N
                        :
                        "["+org.openide.util.Utilities.getShortClassName(realValue.getClass())+"]"; // NOI18N
        }
        else str = "null"; // NOI18N

        return str;
    }


    // ------------------------------------------
    // implementation class for FormDesignValue

    public static class RADConnectionDesignValue implements FormDesignValue { //, java.io.Serializable {
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
        transient private FormModel formModel;  // used for deserialization init if type = TYPE_PROPERTY or TYPE_METHOD or TYPE_BEAN

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

        private RADConnectionDesignValue(String compName, int valueType, String name, FormModel manager) {
            radComponentName = compName;
            formModel = manager;
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

        public FormDesignValue copy(FormProperty formProperty) {
            switch(type) {
                case TYPE_CODE:
                    return new RADConnectionDesignValue(userCode);
                case TYPE_VALUE:    
                    return new RADConnectionDesignValue(requiredTypeName, value);            
            }
            return null;
        }
        
        String getName() {
            if (needsInit)
                initialize();

            if (type == TYPE_VALUE)
                return FormUtils.getFormattedBundleString("FMT_VALUE_CONN", // NOI18N
                                                          new Object[] { value });
            else if (type == TYPE_CODE)
                return FormUtils.getBundleString("CTL_CODE_CONN"); // NOI18N
            else {
                if (radComponent == null || radComponent.getCodeExpression() == null)
                    return FormUtils.getBundleString("CTL_CONNECTION_INVALID"); // NOI18N

                if (radComponent == null)
                    return null;

                if (type == TYPE_PROPERTY)
                    return FormUtils.getFormattedBundleString(
                        "FMT_PROPERTY_CONN", // NOI18N
                        new Object[] { radComponent.getName(), propertyName });
                else if (type == TYPE_METHOD)
                    return FormUtils.getFormattedBundleString(
                        "FMT_METHOD_CONN", // NOI18N
                        new Object[] { radComponent.getName(), methodName });
                else if (type == TYPE_BEAN)
                     return FormUtils.getFormattedBundleString(
                         "FMT_BEAN_CONN", // NOI18N
                         new Object[] { radComponent.getName() });
            }

            throw new IllegalStateException();
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
            radComponent = formModel.findRADComponent(radComponentName);
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
        public Object getDesignValue() { //RADComponent radComponent) {
            /*      if (needsInit) {
                    if (!initialize()) {
                    return IGNORED_VALUE; // failed to initialize
                    }
                    } */
            switch (type) {
                case TYPE_PROPERTY:
                    try {
                        Object value = getProperty().getReadMethod().invoke(getRADComponent().getBeanInstance(), new Object[0]);
                        return value;
                    } catch (Exception e) {
                        // in case of failure do not provide the value during design time
                        return FormDesignValue.IGNORED_VALUE;
                    }
                case TYPE_METHOD:
                    try {
                        Object value = getMethod().getMethod().invoke(getRADComponent().getBeanInstance(), new Object[0]);
                        return value;
                    } catch (Exception e) {
                        // in case of failure do not provide the value during design time
                        return FormDesignValue.IGNORED_VALUE;
                    }
                case TYPE_VALUE:
                    return parseValue(requiredTypeName, value);
                case TYPE_BEAN:
                    RADComponent comp = getRADComponent();
                    return (comp == null) ? FormDesignValue.IGNORED_VALUE : comp.getBeanInstance();
                case TYPE_CODE:
                    return FormDesignValue.IGNORED_VALUE;
                default:
                    return FormDesignValue.IGNORED_VALUE;
            }
        }

        public Object getDesignValue(Object target) {
            return null;
        }

        public String getDescription() {
            return getName();
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
            if (VALUE_VALUE.equals(typeString)) {
                String value = attributes.getNamedItem(ATTR_VALUE).getNodeValue();
                String valueType = attributes.getNamedItem(ATTR_REQUIRED_TYPE).getNodeValue();
                setValue(new RADConnectionDesignValue(valueType, value));

            } else if (VALUE_PROPERTY.equals(typeString)) {
                String component = attributes.getNamedItem(ATTR_COMPONENT).getNodeValue();
                String name = attributes.getNamedItem(ATTR_NAME).getNodeValue();
                setValue(new RADConnectionDesignValue(component, RADConnectionDesignValue.TYPE_PROPERTY, name, formModel)); //rcomponent.getFormModel()));

            } else if (VALUE_METHOD.equals(typeString)) {
                String component = attributes.getNamedItem(ATTR_COMPONENT).getNodeValue();
                String name = attributes.getNamedItem(ATTR_NAME).getNodeValue();
                setValue(new RADConnectionDesignValue(component, RADConnectionDesignValue.TYPE_METHOD, name, formModel)); //rcomponent.getFormModel()));

            } else if (VALUE_BEAN.equals(typeString)) {
                String component = attributes.getNamedItem(ATTR_COMPONENT).getNodeValue();
                setValue(new RADConnectionDesignValue(component, RADConnectionDesignValue.TYPE_BEAN, null, formModel)); //rcomponent.getFormModel()));

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
        if (designValue == null)
            return null;

        String componentName = designValue.radComponent != null ?
                                   designValue.radComponent.getName() :
                                   designValue.radComponentName;

        if (componentName == null && designValue.radComponent != null)
            return null; // invalid component (probably deleted)

        org.w3c.dom.Element el = doc.createElement(XML_CONNECTION);
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
                el.setAttribute(ATTR_COMPONENT, componentName);
                el.setAttribute(ATTR_NAME, designValue.propertyName);
                break;
            case RADConnectionDesignValue.TYPE_METHOD:
                el.setAttribute(ATTR_COMPONENT, componentName);
                el.setAttribute(ATTR_NAME, designValue.methodName);
                break;
            case RADConnectionDesignValue.TYPE_BEAN:
                el.setAttribute(ATTR_COMPONENT, componentName);
                break;
            case RADConnectionDesignValue.TYPE_CODE:
                el.setAttribute(ATTR_CODE, designValue.userCode);
                break;
        }

        return el;
    }
}
