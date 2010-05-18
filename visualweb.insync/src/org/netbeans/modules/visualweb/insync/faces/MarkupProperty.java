/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
        String attribName = null;
        if (attributeDescriptor != null) {
            attribName = attributeDescriptor.getName();
        } else {
            //Use property name from descriptor if ATTRIBUTE_DESCRIPTOR is not available
            attribName = pd.getName();
        }
        element.setAttribute(attribName, "");
        this.attr = element.getAttributeNode(attribName);
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
