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

package com.sun.rave.faces.taglib;


import javax.faces.component.ActionSource;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.component.StateHolder;
import javax.faces.component.ValueHolder;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.el.MethodBinding;
import javax.faces.el.ReferenceSyntaxException;
import javax.faces.el.ValueBinding;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.webapp.UIComponentTag;


/**
 * <p>Abstract base class for concrete implementations of
 * <code>javax.faces.webapp.UIComponentTag</code> for the
 * <em>Widgets Library</em>.</p>
 */

public abstract class AbstractTag extends UIComponentTag {


    // ------------------------------------------------------ Instance Variables


    private String action = null;
    private String actionListener = null;
    private String converter = null;
    private String dir = null;
    private String immediate = null;
    private String lang = null;
    private String onclick = null;
    private String ondblclick = null;
    private String onkeydown = null;
    private String onkeypress = null;
    private String onkeyup = null;
    private String onmousedown = null;
    private String onmousemove = null;
    private String onmouseout = null;
    private String onmouseover = null;
    private String onmouseup = null;
    private String required = null;
    private String style = null;
    private String styleClass = null;
    private String title = null;
    private String validator = null;
    private Object value = null;
    private String valueChangeListener = null;


    // Parameter signatures for Validator and ValueChangeListener
    // method bindings
    private static Class actionArgs[] = new Class[0];
    private static Class actionListenerArgs[] =
      { ActionEvent.class };
    private static Class validatorArgs[] =
      { FacesContext.class, UIComponent.class, Object.class };
    private static Class valueChangeListenerArgs[] =
      { ValueChangeEvent.class };


    // ------------------------------------------------------- Common Attributes


    // IMPLEMENTATION NOTE:  The tag library descriptor should expose
    // only the subset of these attributes that are relevant for any
    // particular component tag


    public void setAction(String action) { this.action = action; }
    public void setActionListener(String actionListener)
    { this.actionListener = actionListener; }
    public void setConverter(String converter) { this.converter = converter; }
    public void setDir(String dir) { this.dir = dir; }
    public void setImmediate(String immediate) { this.immediate = immediate; }
    public void setLang(String lang) { this.lang = lang; }
    public void setOnclick(String onclick) { this.onclick = onclick; }
    public void setOndblclick(String ondblclick)
    { this.ondblclick = ondblclick; }
    public void setOnkeydown(String onkeydown) { this.onkeydown = onkeydown; }
    public void setOnkeypress(String onkeypress)
    { this.onkeypress = onkeypress; }
    public void setOnkeyup(String onkeyup) { this.onkeyup = onkeyup; }
    public void setOnmousedown(String onmousedown)
    { this.onmousedown = onmousedown; }
    public void setOnmousemove(String onmousemove)
    { this.onmousemove = onmousemove; }
    public void setOnmouseout(String onmouseout)
    { this.onmouseout = onmouseout; }
    public void setOnmouseover(String onmouseover)
    { this.onmouseover = onmouseover; }
    public void setRequired(String required) { this.required = required; }
    public void setStyle(String style) { this.style = style; }
    public void setStyleClass(String styleClass)
    { this.styleClass = styleClass; }
    public void setTitle(String title) { this.title = title; }
    public void setValidator(String validator) { this.validator = validator; }
    public void setValue(Object value) { this.value = value; }
    public void setValueChangeListener(String valueChangeListener)
    { this.valueChangeListener = valueChangeListener; }


    // -------------------------------------------------- UIComponentTag Methods


    /**
     * <p>Release any resources allocated during the execution
     * of this tag handler.</p>
     */
    public void release() {

        super.release();
        this.action = null;
        this.actionListener = null;
        this.converter = null;
        this.dir = null;
        this.immediate = null;
        this.lang = null;
        this.onclick = null;
        this.ondblclick = null;
        this.onkeydown = null;
        this.onkeypress = null;
        this.onkeyup = null;
        this.onmousedown = null;
        this.onmousemove = null;
        this.onmouseout = null;
        this.onmouseover = null;
        this.onmouseup = null;
        this.required = null;
        this.style = null;
        this.styleClass = null;
        this.title = null;
        this.validator = null;
        this.value = null;
        this.valueChangeListener = null;

    }


    /**
     * <p>Override properties and attributes of the specified component,
     * if the corresponding attributes of this tag handler instance were
     * explicitly set.</p>
     *
     * @param component <code>UIComponent</code> instance being created
     *  and configured by this tag handler instance
     */
    protected void setProperties(UIComponent component) {

        // Pass through superclass-defined properties
        super.setProperties(component);

        // Pass through simple properties
        setStringAttribute(component, "dir", this.dir);               //NOI18N
        setBooleanAttribute(component, "immediate", this.immediate);  //NOI18N
        setStringAttribute(component, "lang", this.lang);             //NOI18N
        setStringAttribute(component, "onclick", this.onclick);       //NOI18N
        setStringAttribute(component, "ondblclick", this.ondblclick); //NOI18N
        setStringAttribute(component, "onkeydown", this.onkeydown);   //NOI18N
        setStringAttribute(component, "onkeypress", this.onkeypress); //NOI18N
        setStringAttribute(component, "onkeyup", this.onkeyup);       //NOI18N
        setStringAttribute(component, "onmousedown", this.onmousedown); //NOI18N
        setStringAttribute(component, "onmousemove", this.onmousemove); //NOI18N
        setStringAttribute(component, "onmouseout", this.onmouseout); //NOI18N
        setStringAttribute(component, "onmouseover", this.onmouseover); //NOI18N
        setStringAttribute(component, "onmouseup", this.onmouseup);   //NOI18N
        setBooleanAttribute(component, "required", this.required);    //NOI18N
        setStringAttribute(component, "style", this.style);           //NOI18N
        setStringAttribute(component, "styleClass", this.styleClass); //NOI18N
        setStringAttribute(component, "title", this.title);           //NOI18N
        setObjectAttribute(component, "value", this.value);           //NOI18N

        // Pass through special case properties
        if (action != null) {
            if (isValueReference(action)) {
                MethodBinding mb =
                  getFacesContext().getApplication().createMethodBinding
                    (action, actionArgs);
                ((ActionSource) component).setAction(mb);
            } else {
                ((ActionSource) component).setAction
                  (new ConstantMethodBinding(action));
            }
        }
        if (actionListener != null) {
            if (isValueReference(actionListener)) {
                MethodBinding mb =
                  getFacesContext().getApplication().createMethodBinding
                    (actionListener, actionListenerArgs);
                ((ActionSource) component).setActionListener(mb);
            } else {
                throw new IllegalArgumentException(actionListener);
            }
        }
        if (converter != null) {
            if (isValueReference(converter)) {
                ValueBinding vb =
                  getFacesContext().getApplication().createValueBinding(converter);
                component.setValueBinding("converter", vb);           //NOI18N
            } else {
                Converter instance =
                  getFacesContext().getApplication().createConverter(converter);
                ((ValueHolder) component).setConverter(instance);
            }
        }
        if (validator != null) {
            if (isValueReference(validator)) {
                MethodBinding mb =
                  getFacesContext().getApplication().createMethodBinding
                    (validator, validatorArgs);
                ((EditableValueHolder) component).setValidator(mb);
            } else {
                throw new IllegalArgumentException(validator);
            }
        }
        if (valueChangeListener != null) {
            if (isValueReference(valueChangeListener)) {
                MethodBinding mb =
                  getFacesContext().getApplication().createMethodBinding
                    (valueChangeListener, valueChangeListenerArgs);
                ((EditableValueHolder) component).setValueChangeListener(mb);
            } else {
                throw new IllegalArgumentException(valueChangeListener);
            }
        }

    }


    // ------------------------------------------------------- Protected Methods


    /**
     * <p>If the specified attribute value is not <code>null</code>
     * use it to either store a value binding expression for the
     * specified attribute name, or store it as the literal value
     * of the attribute.</p>
     *
     * @param component <code>UIComponent</code> whose attribute
     *  is to be set
     * @param name Attribute name
     * @param value Attribute value (or <code>null</code>)
     *
     * @exception NumberFormatException if the value does not
     *  contain a parsable integer
     * @exception ReferenceSyntaxException if the expression has
     *  invalid syntax
     */
    protected void setBooleanAttribute(UIComponent component,
                                       String name, String value) {

        if (value == null) {
            return;
        }
        if (isValueReference(value)) {
            ValueBinding vb =
                getFacesContext().getApplication().createValueBinding(value);
            component.setValueBinding(name, vb);
        } else {
            component.getAttributes().put(name, Boolean.valueOf(value));
        }

    }


    /**
     * <p>If the specified attribute value is not <code>null</code>
     * use it to either store a value binding expression for the
     * specified attribute name, or store it as the literal value
     * of the attribute.</p>
     *
     * @param component <code>UIComponent</code> whose attribute
     *  is to be set
     * @param name Attribute name
     * @param value Attribute value (or <code>null</code>)
     *
     * @exception NumberFormatException if the value does not
     *  contain a parsable integer
     * @exception ReferenceSyntaxException if the expression has
     *  invalid syntax
     */
    protected void setIntegerAttribute(UIComponent component,
                                       String name, String value) {

        if (value == null) {
            return;
        }
        if (isValueReference(value)) {
            ValueBinding vb =
                getFacesContext().getApplication().createValueBinding(value);
            component.setValueBinding(name, vb);
        } else {
            component.getAttributes().put(name, Integer.valueOf(value));
        }

    }


    /**
     * <p>If the specified attribute value is not <code>null</code>
     * use it to either store a value binding expression for the
     * specified attribute name, or store it as the literal value
     * of the attribute.</p>
     *
     * @param component <code>UIComponent</code> whose attribute
     *  is to be set
     * @param name Attribute name
     * @param value Attribute value (or <code>null</code>)
     *
     * @exception ReferenceSyntaxException if the expression has
     *  invalid syntax
     */
    protected void setObjectAttribute(UIComponent component,
                                      String name, Object value) {

        if (value == null) {
            return;
        }
        if ((value instanceof String) && isValueReference((String) value)) {
            ValueBinding vb =
                getFacesContext().getApplication().createValueBinding((String) value);
            component.setValueBinding(name, vb);
        } else {
            component.getAttributes().put(name, value);
        }

    }


    /**
     * <p>If the specified attribute value is not <code>null</code>
     * use it to either store a value binding expression for the
     * specified attribute name, or store it as the literal value
     * of the attribute.</p>
     *
     * @param component <code>UIComponent</code> whose attribute
     *  is to be set
     * @param name Attribute name
     * @param value Attribute value (or <code>null</code>)
     *
     * @exception ReferenceSyntaxException if the expression has
     *  invalid syntax
     */
    protected void setStringAttribute(UIComponent component,
                                      String name, String value) {

        if (value == null) {
            return;
        }
        if (isValueReference(value)) {
            ValueBinding vb =
                getFacesContext().getApplication().createValueBinding(value);
            component.setValueBinding(name, vb);
        } else {
            component.getAttributes().put(name, value);
        }

    }


}


// Private class to implement MethodBinding returning a constant

class ConstantMethodBinding extends MethodBinding implements StateHolder {

    public ConstantMethodBinding(String outcome) {
        this.outcome = outcome;
    }

    private String outcome;

    public Object invoke(FacesContext context, Object params[]) {
        return outcome;
    }
    
    public Class getType(FacesContext context) {
        return String.class;
    }

    public String getExpressionString() {
        return outcome;
    }

    public Object saveState(FacesContext context) {
        return outcome;
    }

    public void restoreState(FacesContext context, Object state) {
        outcome = (String) state;
    }

    private boolean transientFlag = false;

    public boolean isTransient() { return transientFlag; }

    public void setTransient(boolean transientFlag)
    { this.transientFlag = transientFlag; }

}
