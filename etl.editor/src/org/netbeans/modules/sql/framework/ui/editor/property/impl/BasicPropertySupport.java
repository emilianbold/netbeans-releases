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

package org.netbeans.modules.sql.framework.ui.editor.property.impl;

import java.beans.PropertyEditor;
import java.beans.PropertyVetoException;
import org.netbeans.modules.sql.framework.ui.editor.property.IElement;
import org.netbeans.modules.sql.framework.ui.editor.property.INode;
import org.netbeans.modules.sql.framework.ui.editor.property.IProperty;
import org.netbeans.modules.sql.framework.ui.editor.property.IPropertyCustomizer;
import org.netbeans.modules.sql.framework.ui.editor.property.IPropertyEditor;
import org.openide.nodes.Node;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class BasicPropertySupport extends Node.Property implements IProperty, Comparable {

    private boolean customEditor;
    private IPropertyCustomizer customizer;
    private PropertyEditor editor;
    private String group;
    private boolean isDefault;
    private INode parent;
    private int position;
    private boolean readOnly = false;
    private boolean required;
    private String type;
    private Object val = null;
    private Class valueType;

    public BasicPropertySupport() {
        this(String.class);
    }

    /** Creates a new instance of GUIPropertySupport */
    @SuppressWarnings(value = "unchecked")
    public BasicPropertySupport(Class valueType) {
        super(valueType);
        this.valueType = valueType;
    }

    public BasicPropertySupport(String valueType) {
        this(valueType.getClass());
    }

    /**
     * add a element in the node
     *
     * @param element element to add
     */
    public void add(IElement element) {
    }

    public boolean canRead() {
        return true;
    }

    public boolean canWrite() {
        return !this.readOnly;
    }

    /**
     * Compares this object with the specified object for order. Returns a negative
     * integer, zero, or a positive integer as this object is less than, equal to, or
     * greater than the specified object.
     * <p>
     * In the foregoing description, the notation <tt>sgn(</tt> <i>expression </i>
     * <tt>)</tt> designates the mathematical <i>signum </i> function, which is defined
     * to return one of <tt>-1</tt>,<tt>0</tt>, or <tt>1</tt> according to
     * whether the value of <i>expression </i> is negative, zero or positive. The
     * implementor must ensure <tt>sgn(x.compareTo(y)) ==
     * -sgn(y.compareTo(x))</tt> for
     * all <tt>x</tt> and <tt>y</tt>. (This implies that <tt>x.compareTo(y)</tt>
     * must throw an exception iff <tt>y.compareTo(x)</tt> throws an exception.)
     * <p>
     * The implementor must also ensure that the relation is transitive:
     * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
     * <tt>x.compareTo(z)&gt;0</tt>.
     * <p>
     * Finally, the implementer must ensure that <tt>x.compareTo(y)==0</tt> implies that
     * <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for all <tt>z</tt>.
     * <p>
     * It is strongly recommended, but <i>not </i> strictly required that
     * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>. Generally speaking, any class
     * that implements the <tt>Comparable</tt> interface and violates this condition
     * should clearly indicate this fact. The recommended language is "Note: this class
     * has a natural ordering that is inconsistent with equals."
     *
     * @param o the Object to be compared.
     * @return a negative integer, zero, or a positive integer as this object is less
     *         than, equal to, or greater than the specified object.
     * @throws ClassCastException if the specified object's type prevents it from being
     *         compared to this Object.
     */
    public int compareTo(Object o) {
        if (!(o instanceof IProperty)) {
            throw new ClassCastException("Object " + o + " being compared is not same as this object");
        }

        IProperty property = (IProperty) o;
        if (this.getPosition() < property.getPosition()) {
            return -1;
        } else if (this.getPosition() > property.getPosition()) {
            return 1;
        }
        return 0;
    }

    /**
     * get the group to which this property belongs
     *
     * @return group to which property belongs
     */
    public String getGroup() {
        return this.group;
    }

    /**
     * get the parent element
     *
     * @return parent
     */
    public INode getParent() {
        return parent;
    }

    /**
     * get the position where this property should appear in the property sheet gui
     *
     * @return position
     */
    public int getPosition() {
        return this.position;
    }

    /**
     * get the property customizer
     *
     * @return property customizer
     */
    public IPropertyCustomizer getPropertyCustomizer() {
        return this.customizer;
    }

    @Override
    public PropertyEditor getPropertyEditor() {
        if (this.editor != null) {
            return this.editor;
        }
        return java.beans.PropertyEditorManager.findEditor(getValueType());
    }

    /**
     * get the tooltip of of element
     *
     * @return tooltip
     */
    public String getToolTip() {
        return super.getShortDescription();
    }

    /**
     * get the gui type of this property
     *
     * @return gui type
     */
    public String getType() {
        return this.type;
    }

    public Object getValue() throws java.lang.IllegalAccessException, java.lang.reflect.InvocationTargetException {
        return val;
    }

    /**
     * get the type of value of this property
     *
     * @return property value type
     */
    @Override
    public Class getValueType() {
        return valueType;
    }

    /**
     * does this property has a custom editor
     *
     * @return whether property has a custom editor
     */
    public boolean isCustomEditor() {
        return customEditor;
    }

    /**
     * Does this property has a default value
     *
     * @returrn whether property has a default value
     */
    public boolean isDefault() {
        return this.isDefault;
    }

    /**
     * Is this property read only, if readonly then gui can not edit this property
     *
     * @return whether this property is read only
     */
    public boolean isReadOnly() {
        return this.readOnly;
    }

    /**
     * Is this property require if yes then this property should always have a vaule
     *
     * @return whether this property is required
     */
    public boolean isRequired() {
        return this.required;
    }

    /**
     * is this value valid
     *
     * @return value if valid
     */
    public boolean isValid() {
        try {
            if (this.getValue() != null) {
                return true;
            }
        } catch (Exception ex) {
        }
        return false;
    }

    /**
     * deos this property has a custom editor
     *
     * @param editor whether property has a custom editor
     */
    public void setCustomEditor(String editor) {
        customEditor = Boolean.valueOf(editor).booleanValue();
    }

    /**
     * Does this property has a default value
     *
     * @returrn whether property has a default value
     */
    public void setDefault(boolean def) {
        this.isDefault = def;
    }

    /**
     * Does this property has a default value
     *
     * @returrn whether property has a default value
     */
    public void setDefault(String def) {
        this.isDefault = Boolean.valueOf(def).booleanValue();
    }

    /**
     * set the optional property editor which can be used to edit this property
     *
     * @return property editor
     */
    public void setEditorClass(String editorClass) {
        if (editorClass == null || editorClass.trim().equals("")) {
            return;
        }

        try {
            Class eClass = Class.forName(editorClass);
            editor = (PropertyEditor) eClass.newInstance();
            if (editor instanceof IPropertyEditor) {
                ((IPropertyEditor) editor).setProperty(this);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * set the group to which this property belongs
     *
     * @return group to which property belongs
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * set the owner object which has this property
     */
    public void setOwnerObject(Object obj) {
        // this.ownerObject = obj;
    }

    /**
     * set parent parent element
     */
    public void setParent(INode parent) {
        this.parent = parent;
    }

    /**
     * set the position where this property should appear in the property sheet gui
     *
     * @return position
     */
    public void setPosition(String position) {
        this.position = Integer.parseInt(position);
    }

    /**
     * set the property customizer
     *
     * @param customizer customizer
     */
    public void setPropertyCustomizer(IPropertyCustomizer customizer) {
        this.customizer = customizer;

        if (customizer != null) {
            PropertyEditor pEditor = customizer.getPropertyEditor();
            if (pEditor != null) {
                this.setPropertyEditor(pEditor);
            }
        }
    }

    /**
     * set the optional property editor which can be used to edit this property
     *
     * @return property editor
     */
    public void setPropertyEditor(PropertyEditor editor) {
        this.editor = editor;
        if (editor instanceof IPropertyEditor) {
            ((IPropertyEditor) editor).setProperty(this);
        }
    }

    /**
     * Is this property read only, if readonly then gui can not edit this property
     *
     * @return whether this property is read only
     */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * Is this property read only, if readonly then gui can not edit this property
     *
     * @return whether this property is read only
     */
    public void setReadOnly(String readOnly) {
        this.readOnly = Boolean.valueOf(readOnly).booleanValue();
    }

    /**
     * Is this property require if yes then this property should always have a vaule
     *
     * @return whether this property is required
     */
    public void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * Is this property require if yes then this property should always have a vaule
     *
     * @return whether this property is required
     */
    public void setRequired(String required) {
        this.required = Boolean.valueOf(required).booleanValue();
    }

    /**
     * set the tooltip of the element
     *
     * @param tTip tool tip
     */
    public void setToolTip(String tTip) {
        super.setShortDescription(tTip);
    }

    /**
     * get the gui type of this property
     *
     * @return gui type
     */
    public void setType(String type) {
        this.type = type;
    }

    public void setValue(Object obj) throws java.lang.IllegalAccessException, java.lang.IllegalArgumentException, java.lang.reflect.InvocationTargetException {
        Object oldVal = this.getValue();
        try {
            // first fire vetoable change event
            ((PropertyGroup) this.getParent()).fireVetoableChangeEvent(this.getName(), oldVal, obj);
            // then fire VALID_STATE event
            ((PropertyGroup) this.getParent()).firePropertyChangeEvent();
        } catch (PropertyVetoException ex) {
            return;
        }
        val = obj;

        // then fire value change
        if (!isReadOnly()) {
            ((PropertyGroup) this.getParent()).firePropertyChangeEvent(this.getName(), oldVal, obj);
        }
    }

    /**
     * set the type of value of this property
     *
     * @return property value type
     */
    public void setValueType(String vType) {
        try {
            valueType = Class.forName(vType);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }
}