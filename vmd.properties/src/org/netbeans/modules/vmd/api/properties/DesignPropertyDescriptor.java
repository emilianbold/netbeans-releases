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

package org.netbeans.modules.vmd.api.properties;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.vmd.api.model.DesignComponent;

/**
 *
 * @author Karol Harezlak
 */

/**
 * This class is a property descriptor. It contains all information about given DesignComponent's PropertyDescriptor 
 * to create property in the Properties Window. It is possible to bind more that one DesignComponent's PropertyDescriptor \
 * in one DesignPropertyDescryptor. In this case single property in the Properties Window will represent more
 * that one DesignComponent's PropertyDescriptor.   
 * 
 */
public abstract class DesignPropertyDescriptor {

    /**
     * Default implementation of DesignPropertyDescriptor. To map one DesignPropertyDescriptor with
     * more that one DesignComponent's PropertyDescriptors assign more that one PropertyDescriptor name to the 
     * propertyNames parameter. Creates DesignPropertyDescriptor based on given parameters.
     * 
     * 
     * @param displayName  display name of this property created based on this DesignPropertyDescriptor.
     * This String represent display name of the property shown in the Properties Window.
     * @param toolTip tool tip shown for this property in the Properties Window.
     * @param category property's category
     * @param propertyEditor custom property editor
     * @param propertyEditorType type of property ediotr (for example Boolena.class, String.class) 
     * @param propertyNames names of the PropertyDescriptors connected with this DesignPropertyDescriptor
     * @return instance of DesignPropertyDescriptor
     */
    public static final DesignPropertyDescriptor create(String displayName, String toolTip, String category, DesignPropertyEditor propertyEditor, Class propertyEditorType, String... propertyNames) {
        return new DefaultPropertyDescriptor(displayName, toolTip, category, propertyEditor, propertyEditorType, propertyNames);
    }

    /**
     * Returns list of the names of the PropertyDescriptors connected with this DesignPropertyDescriptor.
     * NOTE: First name of this list is a primary name of the property created based on this DesignPropertyDescriptor.
     * 
     * @return list of properties names
     */
    public abstract List<String> getPropertyNames();
    
    /**
     * Returns display name of this property created based on this DesignPropertyDescriptor.
     * This String represent display name of the property shown in the Properties Window.
     * @return display name
     */
    public abstract String getPropertyDisplayName();
    
    /**
     * Returns tool tip shown for property created based on this DesignPropertyDescriptor in the Properties Window.
     * @return tool tip
     */
    public abstract String getPropertyToolTip();
    
    /**
     * Returns property's category as a String.
     * @return category
     */
    public abstract String getPropertyCategory();
    
     /**
     * Returns custom property editor for the property .
     * @return category
     */
    public abstract DesignPropertyEditor getPropertyEditor();
    
     /**
     * Returns DesignComponent connected with this DesignPropertyDEscriptor (property). 
     * @return DesignComponent
     */
    public abstract DesignComponent getComponent();

    @Deprecated
    public abstract Class getPropertyEditorType();
    /**
     * This method is executed when PropertiesPresenter which contains this DesignPropertyDesciptor is
     * attached to the DesignComponet.
     * @param component DesignComponent connected with this DesignPropertyDEscriptor
     */
    public abstract void init(DesignComponent component);

    private static class DefaultPropertyDescriptor extends DesignPropertyDescriptor {

        private List<String> propertyNames;
        private String displayName;
        private String toolTip;
        private String category;
        private DesignPropertyEditor propertyEditor;
        private WeakReference<DesignComponent> component;
        private Class propertyEditorType;

        private DefaultPropertyDescriptor(String displayName, String toolTip, String category, DesignPropertyEditor propertyEditor, Class propertyEditorType, String... propertyNames) {
            if (category == null) {
                throw new IllegalArgumentException("Empty category"); // NOI18N
            }
            this.propertyNames = Arrays.asList(propertyNames);
            this.displayName = displayName;
            this.toolTip = toolTip;
            this.category = category;
            this.propertyEditor = propertyEditor;
            this.propertyEditorType = propertyEditorType;
        }

        public List<String> getPropertyNames() {
            return propertyNames;
        }

        public String getPropertyDisplayName() {
            return displayName;
        }

        public String getPropertyToolTip() {
            return toolTip;
        }

        public String getPropertyCategory() {
            return category;
        }

        public DesignPropertyEditor getPropertyEditor() {
            return propertyEditor;
        }

        public DesignComponent getComponent() {
            if (component == null) {
                return null;
            }
            return component.get();
        }

        public Class getPropertyEditorType() {
            if (propertyEditor != null) {
                return propertyEditor.getClass();
            } else {
                return propertyEditorType;
            }
        }

        public void init(DesignComponent component) {
            this.component = new WeakReference<DesignComponent>(component);
        }
    }
}
