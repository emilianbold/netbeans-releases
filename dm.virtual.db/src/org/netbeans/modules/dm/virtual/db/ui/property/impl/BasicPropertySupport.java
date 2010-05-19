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

package org.netbeans.modules.dm.virtual.db.ui.property.impl;

import java.beans.PropertyEditor;
import java.beans.PropertyVetoException;
import org.netbeans.modules.dm.virtual.db.ui.property.IElement;
import org.netbeans.modules.dm.virtual.db.ui.property.INode;
import org.netbeans.modules.dm.virtual.db.ui.property.IProperty;
import org.netbeans.modules.dm.virtual.db.ui.property.IPropertyCustomizer;
import org.netbeans.modules.dm.virtual.db.ui.property.IPropertyEditor;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 * @author Ritesh Adval
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

    @SuppressWarnings(value = "unchecked")
    public BasicPropertySupport(Class valueType) {
        super(valueType);
        this.valueType = valueType;
    }

    public BasicPropertySupport(String valueType) {
        this(valueType.getClass());
    }

    public void add(IElement element) {
    }

    public boolean canRead() {
        return true;
    }

    public boolean canWrite() {
        return !this.readOnly;
    }

    public int compareTo(Object o) {
        if (!(o instanceof IProperty)) {
            throw new ClassCastException(NbBundle.getMessage(BasicPropertySupport.class, "MSG_Object") + o + NbBundle.getMessage(BasicPropertySupport.class, "MSG_notSame"));
        }

        IProperty property = (IProperty) o;
        if (this.getPosition() < property.getPosition()) {
            return -1;
        } else if (this.getPosition() > property.getPosition()) {
            return 1;
        }
        return 0;
    }

    public String getGroup() {
        return this.group;
    }

    public INode getParent() {
        return parent;
    }

    public int getPosition() {
        return this.position;
    }

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

    public String getToolTip() {
        return super.getShortDescription();
    }

    public String getType() {
        return this.type;
    }

    public Object getValue() throws java.lang.IllegalAccessException, java.lang.reflect.InvocationTargetException {
        return val;
    }

    @Override
    public Class getValueType() {
        return valueType;
    }

    public boolean isCustomEditor() {
        return customEditor;
    }

    public boolean isDefault() {
        return this.isDefault;
    }

    public boolean isReadOnly() {
        return this.readOnly;
    }

    public boolean isRequired() {
        return this.required;
    }

    public boolean isValid() {
        try {
            if (this.getValue() != null) {
                return true;
            }
        } catch (Exception ex) {
        }
        return false;
    }

    public void setCustomEditor(String editor) {
        customEditor = Boolean.valueOf(editor).booleanValue();
    }

    public void setDefault(boolean def) {
        this.isDefault = def;
    }

    public void setDefault(String def) {
        this.isDefault = Boolean.valueOf(def).booleanValue();
    }

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

    public void setGroup(String group) {
        this.group = group;
    }

    public void setOwnerObject(Object obj) {
        // this.ownerObject = obj;
    }

    public void setParent(INode parent) {
        this.parent = parent;
    }

    public void setPosition(String position) {
        this.position = Integer.parseInt(position);
    }

    public void setPropertyCustomizer(IPropertyCustomizer customizer) {
        this.customizer = customizer;

        if (customizer != null) {
            PropertyEditor pEditor = customizer.getPropertyEditor();
            if (pEditor != null) {
                this.setPropertyEditor(pEditor);
            }
        }
    }

    public void setPropertyEditor(PropertyEditor editor) {
        this.editor = editor;
        if (editor instanceof IPropertyEditor) {
            ((IPropertyEditor) editor).setProperty(this);
        }
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public void setReadOnly(String readOnly) {
        this.readOnly = Boolean.valueOf(readOnly).booleanValue();
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public void setRequired(String required) {
        this.required = Boolean.valueOf(required).booleanValue();
    }

    public void setToolTip(String tTip) {
        super.setShortDescription(tTip);
    }

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

    public void setValueType(String vType) {
        try {
            valueType = Class.forName(vType);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }
}