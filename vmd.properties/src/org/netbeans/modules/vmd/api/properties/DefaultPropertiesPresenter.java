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
/**
 * This class is a default implmentation of PropertiesPresenter. It helps in easy way to compose
 * and provide infomrmations neccessary to create DesignComponent properties visiable in
 * the Properties Window.
 */
public class DefaultPropertiesPresenter extends PropertiesPresenter {

    private static final String NULL_DEFAULT = "Null DefaultPropertyEditorSupport"; //NOI18N
    private List<DesignPropertyDescriptor> descriptors;
    private List<String> categories;
    private String category;
    private DesignEventFilterResolver designEventFilterResolver;

    /**
     * Creates instances of the PropertiesPresenter.
     */
    public DefaultPropertiesPresenter() {
        descriptors = new ArrayList<DesignPropertyDescriptor>();
        categories = new ArrayList<String>();
    }

    /**
     * Creates instances of the PropertiesPresenter. Use this constructor if you'd like to 
     * provide DesignEventFilterResolver to controll execution of designChange method of this
     * presenter.
     * @param designEventFilterResolver
     */
    public DefaultPropertiesPresenter(DesignEventFilterResolver designEventFilterResolver) {
        this();
        this.designEventFilterResolver = designEventFilterResolver;
    }

    /**
     * Based on this methods DesignPropertyEditor is created and automatically added to the list of DesignPropertyDesicriptors
     * available for this presenter.
     * @param displayName  display name of this property created based on this DesignPropertyDescriptor.
     * This String represent display name of the property shown in the Properties Window.
     * @param toolTip tool tip shown for this property in the Properties Window.
     * @param propertyEditor custom property editor
     * @param propertyNames names of the PropertyDescriptors connected with this DesignPropertyDescriptor
     * @return instance of DefaultPropertiesPresenter
     */
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

    /**
     * Based on this methods DesignPropertyEditor is created and automatically added to the list of DesignPropertyDesicriptors
     * available for this presenter.
     * @param displayName  display name of this property created based on this DesignPropertyDescriptor.
     * This String represent display name of the property shown in the Properties Window.
     * @param propertyEditor custom property editor
     * @param propertyNames names of the PropertyDescriptors connected with this DesignPropertyDescriptor
     * @return instance of DefaultPropertiesPresenter
     */
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

    /** Based on this methods DesignPropertyEditor is created and automatically added to the list of DesignPropertyDesicriptors
     * available for this presenter.
     * @param propertyCategory property's category as a String 
     * @return instance of DefaultPropertiesPresenter
     */
    public DefaultPropertiesPresenter addPropertiesCategory(String propertyCategory) {
        assert propertyCategory != null : " Group category cant be null"; // NOI18N
        this.category = propertyCategory;
        if (!categories.contains(propertyCategory)) {
            categories.add(propertyCategory);
        }
        return this;
    }

    /**
     * Returns list of DesignPropertyEditors.
     * @return list od DesignPropertyDescriptors
     */
    public List<DesignPropertyDescriptor> getDesignPropertyDescriptors() {
        return descriptors;
    }

    /**
     * Returns custom property editor for the property .
     * @return category
     */
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