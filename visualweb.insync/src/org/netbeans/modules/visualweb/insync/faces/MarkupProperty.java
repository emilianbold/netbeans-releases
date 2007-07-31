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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.visualweb.insync.faces;

import java.beans.PropertyDescriptor;
import javax.el.MethodExpression;

import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;

import com.sun.rave.designtime.Constants;
import com.sun.rave.designtime.markup.AttributeDescriptor;
import org.netbeans.modules.visualweb.extension.openide.util.Trace;
import org.netbeans.modules.visualweb.insync.java.Statement;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import com.sun.faces.util.ConstantMethodBinding;
import org.netbeans.modules.visualweb.insync.beans.Property;

/**
 * A source property setting that is persisted as an XML element attribute
 */
public class MarkupProperty extends Property {

    public static final MarkupProperty[] EMPTY_ARRAY = {};

    protected final Element element;  // the element in which this attr-based property will reside

    protected final AttributeDescriptor attributeDescriptor;

    protected Attr attr;              // the attr holding this property when set, null when not

    //--------------------------------------------------------------------------------- Construction

    /**
     * Construct a property bound to existing element attribute
     *
     * @param bean
     * @param pd
     * @param element
     * @param attr
     */
    MarkupProperty(MarkupBean bean, PropertyDescriptor pd, Element element, Attr attr) {
        super(bean, pd, false);
        attributeDescriptor = (AttributeDescriptor) pd.getValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR);
        this.element = element;
        this.attr = attr;
        assert Trace.trace("insync.faces", "new bound MarkupProperty: " + this);
    }

    /**
     * Create a property setting bound to a specific statement. Always defers to super since
     * statement based properties are never markup based
     *
     * @param unit
     * @param s
     * @return the new bound property if bindable, else null
     */
    static Property newBoundInstance(FacesPageUnit unit, Statement s) {
        return Property.newBoundInstance(unit, s);
    }

    /**
     * Construct a new property, creating the underlying tag attrs if needed
     *
     * @param beansUnit
     */
    MarkupProperty(MarkupBean bean, PropertyDescriptor pd, Element element) {
        super(bean, pd, false);   // partial construction--no java statements created
        attributeDescriptor = (AttributeDescriptor) pd.getValue(Constants.PropertyDescriptor.ATTRIBUTE_DESCRIPTOR);
        this.element = element;
        element.setAttribute(attributeDescriptor.getName(), "");
        this.attr = element.getAttributeNode(attributeDescriptor.getName());
        assert Trace.trace("insync.faces", "new MarkupProperty: " + this);
    }


    /*
     * @see org.netbeans.modules.visualweb.insync.beans.Property#getValue(java.lang.Class)
     */
    public Object getValue(Class type) {
        if (attr == null)
            return null;

        String value = attr.getValue();
        if (value == null || type == String.class || type == Object.class)
            return value;
        if (type == Boolean.class || type == Boolean.TYPE)
            return Boolean.valueOf(value);
        if (type == Character.class || type == Character.TYPE)
            return new Character(value.length() > 0 ? value.charAt(0) : ' ');
        if (type == Byte.class || type == Byte.TYPE)
            return Byte.valueOf(value);
        if (type == Short.class || type == Short.TYPE)
            return Short.valueOf(value);
        if (type == Integer.class || type == Integer.TYPE)
            return Integer.valueOf(value);
        if (type == Long.class || type == Long.TYPE)
            return Long.valueOf(value);
        if (type == Float.class || type == Float.TYPE)
            return Float.valueOf(value);
        if (type == Double.class || type == Double.TYPE)
            return Double.valueOf(value);
        // EAT: Not sure how safe this is, will have to see
        if (type == MethodBinding.class) {
            if (value.startsWith("#{")) { //NOI18N
                // This is not the right thing to do here, but its working
                // original code stolen from MethodBindingPropertyEditor
                if(getUnit() instanceof FacesPageUnit) {
                    FacesContext facesContext = ((FacesPageUnit)getUnit()).getFacesContext();
                    Application app = facesContext.getApplication();
                    return app.createMethodBinding(value, new Class[] {});
                }
            } else if (value.length() > 0) {
                return new ConstantMethodBinding(value);
            } 
        }
        if(type == MethodExpression.class) {
            if (value.startsWith("#{")) { //NOI18N
                if(getUnit() instanceof FacesPageUnit) {
                    FacesContext facesContext = ((FacesPageUnit)getUnit()).getFacesContext();
                    Application app = facesContext.getApplication();
                    return app.getExpressionFactory().createMethodExpression(
                            FacesContext.getCurrentInstance().getELContext(), value, String.class, new Class[] {});
                }
            } 
        }
        assert Trace.trace("insync.faces", "Unconvertable markup type:" + type + 
                           " attr:" + attr.getName() + " value:" + value);
        return null;
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.beans.Property#isMarkupProperty()
     */
    public boolean isMarkupProperty() {
        return true;
    }

    /**
     * Get the markup source text value of this property.
     * 
     * @see org.netbeans.modules.visualweb.insync.beans.Property#getValueSource()
     */
    public String getValueSource() {
        return attr != null ? attr.getValue() : "";
    }

    /**
     * Set the value of this property as markup source text (touching DOM only if needed)
     * 
     * @see org.netbeans.modules.visualweb.insync.beans.Property#setValue(java.lang.Object, java.lang.String)
     */
    public void setValue(Object value, String valueSource) {
        //System.err.println("FP.setValue: valueSource:" + valueSource + " to:" + attr);
        if (!attr.getValue().equals(valueSource))
            attr.setValue(valueSource);
    }

    /**
     * Remove this property's attr from the element
     * 
     * @return true iff the source entry for this property was actually removed.
     * @see org.netbeans.modules.visualweb.insync.beans.Property#removeEntry()
     */
    protected boolean removeEntry() {
        if (super.removeEntry())
            return true;
        if (element != null && attr != null) {
            element.removeAttributeNode(attr);
            attr = null;
            return true;
        }
        return false;
    }

    /*
     * @see org.netbeans.modules.visualweb.insync.beans.BeansNode#toString(java.lang.StringBuffer)
     */
    public void toString(StringBuffer sb) {
        super.toString(sb);
    }
}
