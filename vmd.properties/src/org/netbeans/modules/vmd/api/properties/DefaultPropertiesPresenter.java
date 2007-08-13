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

package org.netbeans.modules.vmd.api.properties;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignEvent;
import org.netbeans.modules.vmd.api.model.DesignEventFilter;
import org.netbeans.modules.vmd.api.model.PresenterEvent;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Karol Harezlak
 */
public class DefaultPropertiesPresenter extends PropertiesPresenter {

    private static final String NULL_DEFAULT = "Null DefaultPropertyEditorSupport not allowed"; //NOI18N
    
    private List<DesignPropertyDescriptor> descriptors;
    private List<String> categories;
    private String category;
    private DesignEventFilterResolver designEventFilterResolver;

    public DefaultPropertiesPresenter() {
        descriptors = new ArrayList<DesignPropertyDescriptor>();
        categories = new ArrayList<String>();
    }

    public DefaultPropertiesPresenter(DesignEventFilterResolver designEventFilterResolver) {
        this();
        this.designEventFilterResolver = designEventFilterResolver;
    }

    public DefaultPropertiesPresenter addProperty(String displayName, String toolTip, DesignPropertyEditor propertyEditor, String... propertyNames) {
        if (propertyNames.length < 1) {
            throw new IllegalArgumentException(); //NOI18N
        }
        if (propertyEditor == null) {
            throw new IllegalArgumentException(NULL_DEFAULT);
        }
        descriptors.add(DesignPropertyDescriptor.create(displayName, toolTip, category, propertyEditor, propertyEditor.getClass(), propertyNames));
        return this;
    }

    public DefaultPropertiesPresenter addProperty(String displayName, DesignPropertyEditor propertyEditor, String... propertyNames) {
        if (propertyNames.length < 1) {
            throw new IllegalArgumentException(); //NOI18N
        }
        if (propertyEditor == null) {
            throw new IllegalArgumentException(NULL_DEFAULT);
        }
        descriptors.add(DesignPropertyDescriptor.create(displayName, displayName, category, propertyEditor, propertyEditor.getClass(), propertyNames));
        return this;
    }

    public DefaultPropertiesPresenter addPropertiesCategory(String propertyCategory) {
        assert propertyCategory != null : " Group category cant be null"; // NOI18N
        this.category = propertyCategory;
        if (!categories.contains(propertyCategory)) {
            categories.add(propertyCategory);
        }
        return this;
    }

    public List<DesignPropertyDescriptor> getDesignPropertyDescriptors() {
        return descriptors;
    }

    public List<String> getPropertiesCategories() {
        return categories;
    }

    protected void notifyAttached(DesignComponent component) {
        for (DesignPropertyDescriptor designerPropertyDescriptor : getDesignPropertyDescriptors()) {
            if (designerPropertyDescriptor.getPropertyEditor() != null) {
                designerPropertyDescriptor.getPropertyEditor().init(component);
            }
            designerPropertyDescriptor.init(component);
        }
    }

    protected void notifyDetached(DesignComponent component) {
        descriptors = null;
        categories = null;
        category = null;
        designEventFilterResolver = null;
    }

    protected DesignEventFilter getEventFilter() {
        if (designEventFilterResolver != null) {
            return designEventFilterResolver.getEventFilter(getComponent());
        }
        return null;
    }

    protected void designChanged(DesignEvent event) {
        for (DesignPropertyDescriptor designerPropertyDescriptor : getDesignPropertyDescriptors()) {
            DesignPropertyEditor propertyEditor = designerPropertyDescriptor.getPropertyEditor();
            if (designerPropertyDescriptor.getPropertyEditor() != null) {
                propertyEditor.notifyDesignChanged(event);
            }
        }
    }

    protected void presenterChanged(PresenterEvent event) {
    }
}
