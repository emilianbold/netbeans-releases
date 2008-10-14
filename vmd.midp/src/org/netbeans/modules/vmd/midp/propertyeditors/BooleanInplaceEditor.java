/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.vmd.midp.propertyeditors;


import java.awt.Component;

import java.awt.event.ActionListener;
import java.beans.PropertyEditor;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.properties.DesignPropertyEditor;
import org.openide.explorer.propertysheet.InplaceEditor;
import org.openide.explorer.propertysheet.PropertyEnv;
import org.openide.explorer.propertysheet.PropertyModel;

/**
 *
 * @author Karol Harezlak
 */
public class BooleanInplaceEditor implements InplaceEditor {
    
    private JCheckBox checkBox;
    private DesignPropertyEditor propertyEditor;
    private PropertyModel model;
    
    // Do not create this InplaceEditor in PropertyEditor constructor!!!!!! 
    public BooleanInplaceEditor(DesignPropertyEditor propertyEditor) {    
        this.propertyEditor = propertyEditor;
        checkBox = new JCheckBox();
        PropertyValue value = (PropertyValue) propertyEditor.getValue();
        if (value != null && value.getKind() == PropertyValue.Kind.VALUE) {
            if (!(value.getPrimitiveValue() instanceof Boolean)) {
                Boolean selected = (Boolean) value.getPrimitiveValue();
                checkBox.setSelected(selected);
            }
        } else if (value == PropertyValue.createNull())
            checkBox.setSelected(false);
    }
    
    public void  cleanUp() {
        checkBox = null;
        model = null;
        propertyEditor = null;
    }

    public void connect(PropertyEditor propertyEditor, PropertyEnv env) {
    }
    
    public JComponent getComponent() {
        if (checkBox == null)
            return new JCheckBox();
        checkBox.setBorder(BorderFactory.createEmptyBorder(0,3,0,0));
        if (!propertyEditor.canWrite())
            checkBox.setEnabled(false);
        else
            checkBox.setEnabled(true);
        return checkBox;
    }
    
    public void clear() {
    }
    
    public Object getValue() {
        return propertyEditor.getValue();
    }
    
    public void setValue(Object value) {
    }
    
    public boolean supportsTextEntry() {
        return true;
    }
    
    public void reset() {
    }
    
    public void addActionListener(ActionListener al) {
    }
    
    public void removeActionListener(ActionListener al) {
    }
    
    public KeyStroke[] getKeyStrokes() {
        return new KeyStroke[0];
    }
    
    public PropertyEditor getPropertyEditor() {
        return propertyEditor;
    }
    
    public PropertyModel getPropertyModel() {
        return model;
    }
    
    public void setPropertyModel(PropertyModel model) {
        this.model = model;
    }
    
    public boolean isKnownComponent(Component c) {
        return true;
    }
    
}
