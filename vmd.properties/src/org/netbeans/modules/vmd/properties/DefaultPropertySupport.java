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

import java.beans.PropertyEditor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.properties.DesignPropertyEditor;
import org.netbeans.modules.vmd.api.properties.DesignPropertyDescriptor;
import org.openide.nodes.PropertySupport;

/**
 *
 * @author Karol Harezlak
 */
public abstract class DefaultPropertySupport extends PropertySupport {

    public static final String PROPERTY_VALUE_NULL = "PROPERTY_NULL_VALUE_FOR_FEATURE_DESCRIPTOR"; //NOI18N //work around for hashmap which cant accept null
    public static final String PROPERYT_INPLACE_EDITOR = "inplaceEditor"; //NOI18N
    public static final String PROPERTY_CUSTOM_EDITOR_TITLE = "title"; //NOI18N
    private PropertyValue propertyValue;
    private PropertyEditor propertyEditor;
    private List<String> propertyNames;
    private DesignPropertyDescriptor designPropertyDescriptor;

    @SuppressWarnings(value = "unchecked")
    DefaultPropertySupport(DesignPropertyDescriptor designerPropertyDescriptor, Class type) {
        super(designerPropertyDescriptor.getPropertyNames().iterator().next(), type, designerPropertyDescriptor.getPropertyDisplayName(), designerPropertyDescriptor.getPropertyToolTip(), true, true);
        this.designPropertyDescriptor = designerPropertyDescriptor;
        propertyEditor = designerPropertyDescriptor.getPropertyEditor();
        propertyNames = designerPropertyDescriptor.getPropertyNames();
        update();
        if (getPropertyEditor() instanceof DesignPropertyEditor && ((DesignPropertyEditor) getPropertyEditor()).getInplaceEditor() != null) {
            setValue(PROPERYT_INPLACE_EDITOR, ((DesignPropertyEditor) getPropertyEditor()).getInplaceEditor());
        }
    }

    @Override
    public PropertyEditor getPropertyEditor() {
        if (propertyEditor != null) {
            return propertyEditor;
        }
        return super.getPropertyEditor();
    }

    protected PropertyValue readPropertyValue(final DesignComponent component, final String propertyName) {
        assert component != null;
        component.getDocument().getTransactionManager().readAccess(new Runnable() {

            public void run() {
                propertyValue = component.readProperty(propertyName);
            }
        });
        return propertyValue;
    }

    public boolean canWrite() {
        if (propertyEditor instanceof DesignPropertyEditor) {
            return ((DesignPropertyEditor) propertyEditor).canWrite();
        }
        return super.canWrite();
    }

    @SuppressWarnings(value = "unchecked")
    public void restoreDefaultValue() throws IllegalAccessException, InvocationTargetException {
        if (propertyEditor instanceof DesignPropertyEditor && propertyNames != null && (!propertyNames.isEmpty())) {
            final DesignPropertyEditor dpe = (DesignPropertyEditor) propertyEditor;
            if (dpe.isResetToDefaultAutomatically()) {
                setValue(dpe.getDefaultValue());
            } else {
                designPropertyDescriptor.getComponent().getDocument().getTransactionManager().writeAccess(new Runnable() {
                    public void run() {
                        dpe.customEditorResetToDefaultButtonPressed();
                    }
                });
            }
        } else {
            super.restoreDefaultValue();
        }
    }

    public boolean isDefaultValue() {
        if (propertyEditor instanceof DesignPropertyEditor) {
            return ((DesignPropertyEditor) propertyEditor).isDefaultValue();
        }
        return super.isDefaultValue();
    }

    public boolean supportsDefaultValue() {
        if (propertyEditor instanceof DesignPropertyEditor) {
            return ((DesignPropertyEditor) propertyEditor).supportsDefaultValue();
        }
        return super.supportsDefaultValue();
    }

    protected DesignPropertyDescriptor getDesignPropertyDescriptor() {
        return designPropertyDescriptor;
    }

    protected abstract void update();
}
