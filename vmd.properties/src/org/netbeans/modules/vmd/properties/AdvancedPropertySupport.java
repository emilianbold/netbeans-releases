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

package org.netbeans.modules.vmd.properties;

import java.lang.reflect.InvocationTargetException;
import org.netbeans.modules.vmd.api.properties.DesignPropertyEditor;
import org.netbeans.modules.vmd.api.properties.DesignPropertyDescriptor;
import org.netbeans.modules.vmd.api.properties.GroupValue;

/**
 *
 * @author Karol Harezlak
 */
public final class AdvancedPropertySupport extends DefaultPropertySupport {
    
    private String displayName;
    private GroupValue value;
    
    public AdvancedPropertySupport(final DesignPropertyDescriptor designerPropertyDescriptor, Class type) {
        super(designerPropertyDescriptor, type);
    }
    
    public Object getValue() throws IllegalAccessException, InvocationTargetException {
        return value;
    }
    
    public void setValue(final Object value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (getPropertyEditor() instanceof DesignPropertyEditor) {
            DesignPropertyEditor propertyEditor = (DesignPropertyEditor) getPropertyEditor();
            if (propertyEditor.canEditAsText() != null)
                setValue("canEditAsText", propertyEditor.canEditAsText()); //NOI18N
        }
        if (value instanceof GroupValue) {
            this.value = (GroupValue) value;
            if (getPropertyEditor() instanceof DesignPropertyEditor)
                SaveToModelSupport.saveToModel(getDesignPropertyDescriptor().getComponent(), this.value, (DesignPropertyEditor) getPropertyEditor());
            else
                SaveToModelSupport.saveToModel(getDesignPropertyDescriptor().getComponent(), this.value, null);
        } else
            throw new IllegalArgumentException("Wrong type"); //NOI18N
    }
    
    public String getHtmlDisplayName() {
        if (getDesignPropertyDescriptor().getPropertyNames().isEmpty())
            return getDesignPropertyDescriptor().getPropertyDisplayName();
        
        getDesignPropertyDescriptor().getComponent().getDocument().getTransactionManager().readAccess(new Runnable() {
            public void run() {
                boolean isDefault = false;
                
                for (String propertyName : getDesignPropertyDescriptor().getPropertyNames()) {
                    if (getDesignPropertyDescriptor().getComponent().isDefaultValue(propertyName)) {
                        isDefault = false;
                        return;
                    }
                }
                if (isDefault)
                    displayName = getDesignPropertyDescriptor().getPropertyDisplayName();
                else
                    displayName = "<b>" + getDesignPropertyDescriptor().getPropertyDisplayName()+"</b>";  // NOI18N
            }
        });
        
        return displayName;
    }

    protected void update() {
        this.value = new GroupValue(getDesignPropertyDescriptor().getPropertyNames());
        if (getDesignPropertyDescriptor().getPropertyNames() !=null &&! getDesignPropertyDescriptor().getPropertyNames().isEmpty()) {
            for (String propertyName : getDesignPropertyDescriptor().getPropertyNames()) {
                value.putValue(propertyName, readPropertyValue(getDesignPropertyDescriptor().getComponent(), propertyName));
            }
        }
        if (getPropertyEditor() instanceof DesignPropertyEditor) {
            DesignPropertyEditor propertyEditor = (DesignPropertyEditor)getPropertyEditor();
            propertyEditor.resolve(
                    getDesignPropertyDescriptor().getComponent(),
                    getDesignPropertyDescriptor().getPropertyNames(),
                    this.value,
                    this,
                    getDesignPropertyDescriptor().getPropertyDisplayName()
            );
            propertyEditor.resolveInplaceEditor(propertyEditor.getInplaceEditor());
            String title = propertyEditor.getCustomEditorTitle();
            if ( title != null)
                setValue(PROPERTY_CUSTOM_EDITOR_TITLE, title);
            if (propertyEditor != null && propertyEditor.canEditAsText() != null) {
                this.setValue("canEditAsText", propertyEditor.canEditAsText()); //NOI18N
            }
        }
    }
    
}
