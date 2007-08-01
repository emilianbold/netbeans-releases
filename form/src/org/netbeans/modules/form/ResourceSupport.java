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

package org.netbeans.modules.form;

import java.awt.Component;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.form.editors2.BorderDesignSupport;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.nodes.PropertySupport;
import org.openide.util.Lookup;

/**
 * This class manages resources of a form (i.e. property values stored
 * externally as resources). It communicates with the app framework support
 * via the ResourceService interface. It also takes care of the automatic
 * internationalization backed by the i18n/form module.
 * 
 * @author Tomas Pavek
 */
public class ResourceSupport {

    private FormModel formModel;

    private ResourceService resourceService;
    private I18nService i18nService;

    private String designLocale = ""; // locale suffix (including initial _)
    private static Map<DataObject, String> rememberedLocales = new WeakHashMap();

    private Map droppedValues;

    /** Marks property that should be excluded from resourcing (explicitly set
     * to plain value). */
    private static final String EXCLUDE_FROM_RESOURCING = "does not want to be a resource"; // NOI18N
    private static final String EXCLUSION_DETERMINED = "already consulted with exclusion filters"; // NOI18N
    private static final String[] PROPERTY_ATTRS = { EXCLUSION_DETERMINED, EXCLUDE_FROM_RESOURCING };

    static final String PROP_AUTO_SET_COMPONENT_NAME = "autoSetComponentName"; // NOI18N

    static final String PROP_AUTO_RESOURCING = "autoResourcing"; // NOI18N
    static final int AUTO_OFF = 0;
    static final int AUTO_I18N = 1;
    static final int AUTO_RESOURCING = 2;
    static final int AUTO_INJECTION = 3;

    static final String PROP_FORM_BUNDLE = "formBundle"; // NOI18N
    private static final String PROP_DESIGN_LOCALE = "designLocale"; // NOI18N
    private static final String DEFAULT_BUNDLE_NAME = "Bundle"; // NOI18N
    private String defaultI18nBundle;

    private static final int PLAIN_VALUE = 1;
    private static final int UNDEFINED_RESOURCE = 2; // key == COMPUTE_AUTO_KEY
    private static final int VALID_RESOURCE_VALUE = 4;

    ResourceSupport(FormModel formModel) {
        this.formModel = formModel;
        formModel.addFormModelListener(new ModelListener());
    }

    void init() {
        String locale = rememberedLocales.get(getSrcDataObject());
        if (locale != null) {
            designLocale = locale;
            if (!locale.equals("") && formModel.isFormLoaded()) // NOI18N
                updateDesignLocale();
        }
    }

    private ResourceService getResourceService() {
        if (resourceService == null) {
            resourceService = (ResourceService)Lookup.getDefault().lookup(ResourceService.class);
        }
        return resourceService;
    }

    private I18nService getI18nService() {
        if (i18nService == null) {
            i18nService = (I18nService)Lookup.getDefault().lookup(I18nService.class);
        }
        return i18nService;
    }

    private static ResourceSupport getResourceSupport(FormProperty property) {
        return FormEditor.getResourceSupport(property.getPropertyContext().getFormModel());
    }

    private static ResourceSupport getResourceSupport(RADComponent metacomp) {
        return FormEditor.getResourceSupport(metacomp.getFormModel());
    }

    // -----

    /**
     * Prepares a newly created form.
     */
    void prepareNewForm() {
        if (isResourceAutoMode()) {
            resourceService.prepareNew(getSourceFile());
        } else if (!isI18nAutoMode()) {
            return;
        }
        switchFormToResources(); // templates don't contain internationalized texts or resources
        if (isAutoName()) {
            setupNameProperty(true);
        }
    }

    /**
     * Converts given value to a resource. Called always when a component
     * property is being set.
     * This method is not called during undo/redo.
     * @param value new value being set to the property (can be FormProperty.ValueWithEditor)
     * @param property the property to which the value is going to be set (still
     *        contains the previous value)
     */
    public static Object makeResource(Object value, FormProperty property) {
        if (!isResourceableProperty(property))
            return value;

        ResourceSupport support = getResourceSupport(property);
        if (isResourceType(property.getValueType())) {
            value = support.makeResource0(value, property);
        }
        else { // check nested values (borders are the only meaningful example)
               // the same value is returned, but might have been changed inside
            for (FormProperty prop : support.getNestedResourceProperties(
                    FormProperty.getEnclosedValue(value), property, PLAIN_VALUE)) {
                boolean fire = prop.isChangeFiring();
                prop.setChangeFiring(false);
                try {
                    Object val = prop.getValue();
                    Object resValue = support.makeResource0(val, prop);
                    if (resValue != val)
                        prop.setValue(resValue);
                }
                catch (Exception ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
                prop.setChangeFiring(fire);
            }
        }
        return value;
        // the value is set to the property which leads to call of updateStoredValue
    }

    private Object makeResource0(Object newValue, FormProperty property) {
        Object value = FormProperty.getEnclosedValue(newValue);

        if (value instanceof ExternalValue) {
            // when a copy of resource or i18n value is created, it may have the
            // key set to COMPUTE_AUTO_KEY asking form editor to provide it
            ExternalValue eValue = (ExternalValue) value;
            if (eValue.getKey() == ExternalValue.COMPUTE_AUTO_KEY) {
                if (value instanceof I18nValue && getI18nService() != null) {
                    String key = getDefaultKey0(property, AUTO_I18N);
                    eValue = i18nService.changeKey((I18nValue)value, key);
                }
                else if (value instanceof ResourceValue && getResourceService() != null) {
                    String key = getDefaultKey0(property, AUTO_RESOURCING);
                    eValue = resourceService.changeKey((ResourceValue)value, key);
                }
                return eValue;
            } else {
                return newValue;
            }
        }

        if (value == null || !isConvertibleToResource(value) || isExcludedProperty0(property))
            return newValue;

        Object prevValue;
        try {
            prevValue = property.getValue();
        }
        catch (Exception ex) { // should not happen
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return newValue;
        }

        if (prevValue instanceof I18nValue) {
            if (getI18nService() != null) {
                newValue = i18nService.changeValue((I18nValue)prevValue, value.toString());
            }
        }
        else if (prevValue instanceof ResourceValue) {
            if (getResourceService() != null) {
                newValue = resourceService.changeValue((ResourceValue) prevValue,
                                                       value,
                                                       getStringValue(property, value));
            }
        }
        else if (isI18nAutoMode()) {
            if (value instanceof String) {
                I18nValue i18nValue = searchDroppedI18nValue(property, value.toString());
                if (i18nValue == null) {
                    i18nValue = i18nService.create(getDefaultKey0(property, AUTO_I18N),
                                                   value.toString(),
                                                   getSrcDataObject());
                }
                // also need to switch to the i18n property editor
                newValue = new FormProperty.ValueWithEditor(i18nValue,
                    i18nService.getPropertyEditor(property.getValueType(), property.getCurrentEditor()));
            }
        }
        else if (isResourceAutoMode()) {
            // resource value does not have a particular property editor - respect
            // the one comming from outside
            PropertyEditor newPrEd = newValue instanceof FormProperty.ValueWithEditor ?
                ((FormProperty.ValueWithEditor)newValue).getPropertyEditor() : null;

            ResourceValue resValue = searchDroppedResourceValue(property, value);
            if (resValue == null) {
                resValue = resourceService.create(getDefaultKey0(property, AUTO_RESOURCING),
                                                  property.getValueType(),
                                                  value,
                                                  getStringValue(property, value),
                                                  getSourceFile());
            }
            newValue = newPrEd != null ? new FormProperty.ValueWithEditor(resValue, newPrEd) : resValue;
        }

        return newValue;
    }

    private I18nValue searchDroppedI18nValue(FormProperty property, String expectedValue) {
        if (droppedValues != null) {
            String dropKey = getPropertyPath(property, null);
            Object value = droppedValues.get(dropKey);
            if (value instanceof I18nValue) {
                I18nValue i18nValue = (I18nValue) value;
                if (i18nValue.getValue().equals(expectedValue))
                    return i18nValue;
            }
        }
        return null;
    }

    private ResourceValue searchDroppedResourceValue(FormProperty property, Object expectedValue) {
        if (droppedValues != null) {
            String dropKey = getPropertyPath(property, null);
            Object value = droppedValues.get(dropKey);
            if (value instanceof ResourceValue) {
                ResourceValue resValue = (ResourceValue) value;
                if (resValue.getValue().equals(expectedValue))
                    return resValue;
            }
        }
        return null;
    }

    /**
     * Changes all component's modified properties to resources or i18n. Called
     * when a copy of a component is created.
     * This method is not called during undo/redo.
     */
    public static void switchComponentToResources(RADComponent metacomp) {
        ResourceSupport support = getResourceSupport(metacomp);
        if (support.isAutoMode())
            support.switchComponentToResources(metacomp, false, true);
    }

    private void switchComponentToResources(RADComponent metacomp, boolean update, boolean recursive) {
        int valueType = PLAIN_VALUE | UNDEFINED_RESOURCE;
        for (FormProperty prop : getComponentResourceProperties(metacomp, valueType, recursive)) {
            boolean fire = prop.isChangeFiring(); // suppress firing - avoid usual update when resource is set
            prop.setChangeFiring(false);
            try {
                Object resValue = makeResource0(prop.getValue(), prop);
                prop.setValue(resValue);
                if (update) {
                    resValue = prop.getValue(); // makeResource might return ValueWithEditor...
                    if (resValue instanceof I18nValue)
                        getI18nService().update(null, (I18nValue) resValue,
                                getSrcDataObject(), getI18nBundleName(), designLocale,
                                true);
                    else if (resValue instanceof ResourceValue)
                        getResourceService().update(null, (ResourceValue) resValue,
                                                    getSourceFile(), designLocale);
                }
                // otherwise update will be triggered by component addition
                // (called when copying a component)
            }
            catch (Exception ex) { // getValue, setValue should not fail
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
            prop.setChangeFiring(fire);
        }
    }

    private static void setupNameProperty(RADComponent metacomp, boolean set, boolean recursive) {
        FormProperty nameProp = getNameProperty(metacomp);
        if (nameProp != null) {
            try {
                if (set && !nameProp.isChanged()) {
                    nameProp.setValue(metacomp.getName());
                }
                else if (!set && nameProp.isChanged()
                         && metacomp.getName().equals(nameProp.getValue()))
                {   // property value corresponds to the component name
                    nameProp.restoreDefaultValue();
                }
            }
            catch (Exception ex) { // getValue, setValue, restoreValue - should not fail here
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
        if (recursive && metacomp instanceof ComponentContainer) {
            for (RADComponent subcomp : ((ComponentContainer)metacomp).getSubBeans()) {
                setupNameProperty(subcomp, set, recursive);
            }
        }
    }

    private void setupNameProperty(boolean set) {
        for (RADComponent metacomp : formModel.getAllComponents()) {
            setupNameProperty(metacomp, set, false);
        }
    }

    private static FormProperty getNameProperty(RADComponent metacomp) {
        if (metacomp.getBeanInstance() instanceof Component) {
            return (FormProperty) metacomp.getPropertyByName("name"); // NOI18N
        }
        return null;
    }

    private void switchFormToResources() {
        for (RADComponent metacomp : formModel.getAllComponents()) {
            switchComponentToResources(metacomp, true, false);
        }

        if (droppedValues != null)
            droppedValues.clear();
    }

    private void switchFormToPlainValues(String originalBundleName) {
        for (RADComponent metacomp : formModel.getAllComponents()) {
            for (FormProperty prop : getComponentResourceProperties(metacomp, VALID_RESOURCE_VALUE, false)) {
                Object value = getAutoValue(prop);
                if (value != null) {
                    boolean fire = prop.isChangeFiring();
                    prop.setChangeFiring(false); // suppress firing and converting
                    try {
                        if (value instanceof I18nValue) {
                            I18nValue i18nValue = (I18nValue) value;
                            prop.setValue(new FormProperty.ValueWithEditor(
                                    i18nValue.getValue(), prop.findDefaultEditor()));
                            i18nService.update(i18nValue, null,
                                               getSrcDataObject(),
                                               originalBundleName != null ? originalBundleName : getI18nBundleName(),
                                               null, true);
                        }
                        else if (value instanceof ResourceValue) {
                            ResourceValue resValue = (ResourceValue) value;
                            Object plainValue;
                            PropertyEditor prEd = prop.getCurrentEditor();
                            if (prEd instanceof ResourceWrapperEditor) {
                                prEd.setValue(value);
                                plainValue = ((ResourceWrapperEditor)prEd).getUnwrappedValue();
                            }
                            else {
                                plainValue = resValue.getValue();
                            }
                            prop.setValue(plainValue);
                            resourceService.update(resValue, null, getSourceFile(), designLocale);
                        }
                        addDroppedValue(prop, value); // it will keep data from all locales
                        // (there is no other way to remember this data for undo)
                    }
                    catch (Exception ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    }
                    prop.setChangeFiring(fire);
                }
            }
        }
    }

    private void addDroppedValue(FormProperty property, Object value) {
        if (droppedValues == null) {
            droppedValues = new HashMap();
        }
        droppedValues.put(getPropertyPath(property, null), value);
    }

    /**
     * Reacts on component's variable renaming. All automatically created keys
     * that use the name of the component will be changed to the new name.
     * Also the 'name' property of visual components is updated.
     */
    public static void componentRenamed(RADComponent metacomp, String oldName, String newName) {
        ResourceSupport support = getResourceSupport(metacomp);
        if (support.isAutoMode()) {
            support.renameDefaultKeysForComponent(metacomp, null, null, oldName, newName);
        }

        // hack: 'name' property needs special treatment
        FormProperty nameProp = getNameProperty(metacomp);
        if (nameProp != null && nameProp.isChanged()) {
//                boolean fire = nameProp.isChangeFiring();
//                nameProp.setChangeFiring(false); // don't want to record this change for undo/redo
            try {
                Object name = nameProp.getValue();
                if (oldName.equals(name) && !name.equals(newName)) {
                    nameProp.setValue(newName);
                }
            }
            catch (Exception ex) { // should not happen
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
//                nameProp.setChangeFiring(fire);
        }
    }

    public static void formRenamed(FormModel formModel, String oldName) {
        ResourceSupport support = FormEditor.getResourceSupport(formModel);
        if (support.getAutoMode() == AUTO_I18N) { // [in other auto modes the keys don't include class name]
            support.renameDefaultKeys(oldName);
        }
    }

    private void renameDefaultKeys(String oldFormName) {
        for (RADComponent metacomp : formModel.getAllComponents()) {
            renameDefaultKeysForComponent(metacomp, oldFormName, getSrcDataObject().getName(), null, null);
        }
    }

    private void renameDefaultKeysForComponent(RADComponent metacomp,
            String oldFormName, String newFormName,
            String oldCompName, String newCompName)
    {
        if (oldFormName == null) {
            oldFormName = getSrcDataObject().getName();
            newFormName = oldFormName;
        } else {
            assert newFormName != null;
        }
        if (oldCompName == null) {
            oldCompName = getComponentName(metacomp);
            newCompName = oldCompName;
        } else {
            assert newCompName != null;
        }

        for (FormProperty prop : getComponentResourceProperties(metacomp, VALID_RESOURCE_VALUE, false)) {
            int type = -1;
            I18nValue i18nValue = null;
            ResourceValue resValue = null;
            // check if the value uses a default key
            String oldKey = null;
            try {
                ExternalValue eValue = (ExternalValue) prop.getValue();
                oldKey = eValue.getKey();
                if (eValue instanceof I18nValue) {
                    i18nValue = (I18nValue) eValue;
                    type = AUTO_I18N;
                } else if (eValue instanceof ResourceValue) {
                    resValue = (ResourceValue) eValue;
                    type = AUTO_RESOURCING;
                }
            }
            catch (Exception ex) { // should not happen
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
            String oldDefaultKey = getDefaultKey(oldFormName, getPropertyPath(prop, oldCompName), type);
            if (!isAutoKey(oldKey, oldDefaultKey)) {
                continue;
            }

            // derive the new key
            String suffix = oldKey.length() > oldDefaultKey.length() ?
                            oldKey.substring(oldDefaultKey.length()) : ""; // NOI18N
            String newKey = getDefaultKey(newFormName, getPropertyPath(prop, newCompName), type) + suffix;
            if (newKey.equals(oldKey)) {
                continue;
            }

            // set the new key
            boolean fire = prop.isChangeFiring();
            prop.setChangeFiring(false); // suppress firing - update is not done the usual way
            try {
                if (i18nValue != null) {
                    I18nValue oldI18nValue = i18nValue;
                    I18nValue newI18nValue = i18nService.changeKey(oldI18nValue, newKey);
                    prop.setValue(newI18nValue);
                    i18nService.update(oldI18nValue, newI18nValue,
                                       getSrcDataObject(), getI18nBundleName(), designLocale,
                                       true);
                }
                else if (resValue != null) {
                    ResourceValue oldResValue = resValue;
                    ResourceValue newResValue = resourceService.changeKey(oldResValue, newKey);
                    prop.setValue(newResValue);
                    resourceService.update(oldResValue, newResValue, getSourceFile(), designLocale);
                }
            }
            catch (Exception ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
            prop.setChangeFiring(fire);
        }
    }

    public static void formMoved(FormModel formModel, FileObject oldFolder) {
        ResourceSupport support = FormEditor.getResourceSupport(formModel);
        if (support.getAutoMode() == AUTO_I18N) { // [in other auto modes the entire properties file is moved]
            support.moveFormI18n(oldFolder);
        }
    }

    private void moveFormI18n(FileObject oldFolder) {
        String oldPkg = getPkgResourceName(oldFolder);
        String newPkg = getPkgResourceName(getSourceFile());
        String newBundle = getI18nBundleName();
        String oldBundle = oldPkg + newBundle.substring(newPkg.length());
        switchFormToPlainValues(oldBundle);
        switchFormToResources();
        // TODO: probably should also copy the manually set values (not just
        // move as in case of auto-i18n) - definitely if moving between projects
    }

    public static void loadInjectedResources(RADComponent metacomp) {
        ResourceSupport support = getResourceSupport(metacomp);
        if (support != null && support.getAutoMode() == AUTO_INJECTION)
            support.loadInjectedResources0(metacomp);
    }

    private void loadInjectedResources0(RADComponent metacomp) {
        if (getResourceService() == null)
            return;

        String compName = getComponentName(metacomp);
        String keyEx = compName + "\\.\\w+"; // NOI18N
        Collection<String> compResources = resourceService.findKeys(keyEx, getSourceFile());
        if (compResources.size() > 0) {
            String[] propNames = new String[compResources.size()];
            int compPrefixLength = compName.length() + 1;
            int i = 0;
            for (String key : compResources) {
                propNames[i++] = key.substring(compPrefixLength);
            }
            FormProperty[] properties = metacomp.getBeanProperties(propNames);
            i = 0;
            for (String key : compResources) {
                FormProperty prop = properties[i++];
                if (prop != null && isResourceType(prop.getValueType())) {
                    try {
                        prop.setValue(resourceService.get(
                                key, prop.getValueType(), designLocale, getSourceFile()));
                    }
                    catch (Exception ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    }
                }
            }
        }
    }

    /**
     * Reacts on a change in a resourced/internationalized property value.
     * Makes sure that added/changed/removed value is updated in the properties
     * file. (This method is called only for component properties,
     * so it must scan the value for nested properties.)
     */
    public static void updateStoredValue(Object oldValue, Object newValue, FormProperty property) {
        if (isResourceableProperty(property)) {
            ResourceSupport support = getResourceSupport(property);
            if (support != null) {
                support.updateStoredValue0(oldValue, newValue, property);
            }
        }
    }

    private void updateStoredValue0(Object oldValue, Object newValue, FormProperty property) {
        // hack: This method is called whenever a property value changes, so besides
        // updating resource values we can also react on changing the "name" property
        // which we use to determine the name of automatic resource key.
        if (property.getName().equals("name") && property instanceof RADProperty
                && property.getValueType() == String.class) {
            RADComponent metacomp = ((RADProperty)property).getRADComponent();
            if (!(oldValue instanceof String)) {
                oldValue = metacomp.getName();
            }
            if (!property.isChanged() || !(newValue instanceof String)) {
                newValue = metacomp.getName();
            }
            if (!newValue.equals(oldValue)) {
                componentRenamed(metacomp, (String)oldValue, (String)newValue);
            }
            return;
        }

        if (isResourceType(property.getValueType())) {
            updateStoredValue1(oldValue, newValue, property);
        }
        else {
            Collection<FormProperty> colOld = getNestedResourceProperties(
                    oldValue, property, VALID_RESOURCE_VALUE);
            Collection<FormProperty> colNew = getNestedResourceProperties(
                    newValue, property, VALID_RESOURCE_VALUE);

            for (FormProperty oProp : colOld) {
                boolean foundInNew = false;
                for (Iterator<FormProperty> it=colNew.iterator(); it.hasNext(); ) {
                    FormProperty nProp = it.next();
                    if (getPropertyPath(oProp, null).equals(getPropertyPath(nProp, null))) { // same property
                        try {
                            updateStoredValue1(oProp.getValue(), nProp.getValue(), nProp);
                        }
                        catch (Exception ex) { // getValue should not fail
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                        }
                        it.remove(); // remove so only newly added remain
                        foundInNew = true;
                        break;
                    }
                }
                if (!foundInNew) { // removed resource/i18n value
                    try {
                        updateStoredValue1(oProp.getValue(), null, oProp);
                    }
                    catch (Exception ex) { // getValue() should not fail
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    }
                }
            }
            // colNew now contains newly added resources/i18n values
            for (FormProperty nProp : colNew) {
                try {
                    updateStoredValue1(null, nProp.getValue(), nProp);
                }
                catch (Exception ex) { // getValue should not fail
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
            }
        }
    }

    private void updateStoredValue1(Object oldValue, Object newValue, FormProperty property) {
        // updateI18nValue and updateResourceValue can each be called once or
        // not at all, but not twice
        if (oldValue instanceof I18nValue) {
            updateI18nValue(oldValue, newValue, property);
            if (newValue instanceof ResourceValue)
                updateResourceValue(oldValue, newValue, property);
        }
        else if (oldValue instanceof ResourceValue) {
            updateResourceValue(oldValue, newValue, property);
            if (newValue instanceof I18nValue)
                updateI18nValue(oldValue, newValue, property);
        }
        // updating from plain value
        else if (newValue instanceof I18nValue)
            updateI18nValue(oldValue, newValue, property);
        else if (newValue instanceof ResourceValue)
            updateResourceValue(oldValue, newValue, property);
    }

    private void updateI18nValue(Object oldValue, Object newValue, FormProperty property) {
        if (getI18nService() != null) {
            I18nValue oldVal = oldValue instanceof I18nValue ? (I18nValue) oldValue : null;
            I18nValue newVal = newValue instanceof I18nValue ? (I18nValue) newValue : null;
            try {
                i18nService.update(oldVal, newVal,
                                   getSrcDataObject(), getI18nBundleName(), designLocale,
                                   isAutoValue(oldVal, getDefaultKey0(property, AUTO_I18N)));
            }
            catch (IOException ex) {
                // [can't store to properties bundle - should do something?]
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
    }

    private void updateResourceValue(Object oldValue, Object newValue, FormProperty property) {
        if (getResourceService() != null) {
            ResourceValue oldVal = oldValue instanceof ResourceValue ? (ResourceValue) oldValue : null;
            ResourceValue newVal = newValue instanceof ResourceValue ? (ResourceValue) newValue : null;
            try {
                resourceService.update(oldVal, newVal, getSourceFile(), designLocale);
            }
            catch (IOException ex) {
                // [can't store to properties file - should report something?]
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
    }

    public static ResourceValue findResource(String key, FormProperty property) {
        if (!isResourceableProperty(property))
            return null;

        return getResourceSupport(property).findResource0(key, property);
    }

    private ResourceValue findResource0(String key, FormProperty prop) {
        return getResourceService() != null ?
            resourceService.get(key, prop.getValueType(), designLocale, getSourceFile()) : null;
    }

    /**
     * @return true if the property is capable of holding resource values (i.e. FormDesignValue)
     */
    public static boolean isResourceableProperty(FormProperty prop) {
        return prop.getPropertyContext().useMultipleEditors();
        // some layout properties can't accommodate FormDesignValue
    }

    /**
     * @return true if the property should be resourced if there is a chance to
     */
    public static boolean isPropertyForResourcing(FormProperty prop) {
        if (!isResourceableProperty(prop)) {
            return false;
        }
        ResourceSupport support = getResourceSupport(prop);
        return !support.isExcludedProperty0(prop) && support.isResourceAutoMode();
    }

    /**
     * Returns whether the given property is excluded from automatic resourcing,
     * i.e. marked as it should intentionally hold a plain value. This can
     * happen when the user explicitly chooses not to use resource for given
     * property value, or there can be properties excluded by default
     * (e.g. 'name' property of components).
     * NOTE: This method is usaful only for properties that can be resourced,
     * it should not be called for properties that can't hold resource values.
     * @see isResourceableProperty
     * @return true if the property is marked as excluded from automatic
     *         resourcing (i.e. is expected to hold a plain value)
     */
    public static boolean isExcludedProperty(FormProperty prop) {
        assert isResourceableProperty(prop);
        return Boolean.TRUE.equals(prop.getValue(EXCLUDE_FROM_RESOURCING)) ?
                true : getResourceSupport(prop).isExcludedProperty1(prop);
    }

    private boolean isExcludedProperty0(FormProperty prop) {
        return Boolean.TRUE.equals(prop.getValue(EXCLUDE_FROM_RESOURCING)) ?
                true : isExcludedProperty1(prop);
    }

    private boolean isExcludedProperty1(FormProperty prop) {
        if (!Boolean.TRUE.equals(prop.getValue(EXCLUSION_DETERMINED))) {
            if (getResourceService() == null)
                return false;

            prop.setValue(EXCLUSION_DETERMINED, true);
            Object propOwner = prop.getPropertyContext().getOwner();
            Class type = null;
            if (propOwner instanceof RADComponent) {
                type = ((RADComponent)propOwner).getBeanClass();
            } else if (propOwner instanceof FormProperty) {
                type = ((FormProperty)propOwner).getValueType();
            }
            boolean excl;
            if (type != null) {
                excl = resourceService.isExcludedProperty(type, prop.getName());
            } else {
                excl = false;
            }
            prop.setValue(EXCLUDE_FROM_RESOURCING, excl);
            return excl;
        }
        return false;
    }

    public static void setExcludedProperty(FormProperty prop, boolean excl) {
        if (isResourceableProperty(prop) && excl != isExcludedProperty(prop)) {
            prop.setValue(EXCLUDE_FROM_RESOURCING, excl);
        }
    }

    static String[] getPropertyAttrNames() {
        return PROPERTY_ATTRS;
    }

    public static String getInjectionCode(RADComponent metacomp, String compGenName) {
        return getResourceSupport(metacomp).getInjectionCode0(metacomp, compGenName);
    }

    private String getInjectionCode0(RADComponent metacomp, String compGenName) {
        return getAutoMode() == AUTO_INJECTION && getResourceService() != null ?
            resourceService.getInjectionCode(
                    metacomp.getBeanInstance(), compGenName, getSourceFile()) :
            null;
    }

    public static boolean isInjectedProperty(FormProperty prop) {
        if (isResourceableProperty(prop)) {
            ResourceSupport support = getResourceSupport(prop);
            if (support.getAutoMode() == AUTO_INJECTION) {
                Object value;
                try {
                    value = prop.getValue();
                    if (value instanceof ResourceValue) {
                        String key = ((ResourceValue)value).getKey();
                        if (key != null && support.getDefaultKey0(prop, AUTO_INJECTION).equals(key)) {
                            return true;
                        }
                    }
                }
                catch (Exception ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
            }
        }
        return false;
    }

    public boolean isDefaultInternationalizableProject() {
        if (getI18nService() != null)
            return i18nService.isDefaultInternationalizableProject(getSourceFile());
        else
            return false;
    }

    public boolean projectUsesResources() {
        if (getResourceService() != null)
            return resourceService.projectUsesResources(getSourceFile());
        else
            return false;
    }

    public boolean projectCanUseResources() {
        if (getResourceService() != null)
            return resourceService.projectCanUseResources(getSourceFile());
        else
            return false;
    }

    private void bundleChanged(String oldBundle) {
        switchFormToPlainValues(oldBundle);
        String oldLocale = designLocale;
        setDesignLocale(""); // NOI18N
        FormEditor.getFormEditor(formModel).getFormRootNode()
                        .firePropertyChangeHelper(PROP_DESIGN_LOCALE, oldLocale, designLocale);
        switchFormToResources();
    }

    String getDesignLocale() {
        return designLocale;
    }

    private void changeDesignLocale(String designLocale) {
        setDesignLocale(designLocale);
        updateDesignLocale();
        // hack to update designer...
        formModel.fireEvents(null);
    }

    private void setDesignLocale(String locale) {
        designLocale = locale;
        rememberedLocales.put(getSrcDataObject(), locale); // keep to survive form reload
    }

    private void updateDesignLocale() {
        Collection<FormProperty> props = getAllResourceProperties(VALID_RESOURCE_VALUE);
        // read all values in advance - setting certain properties might reset others (e.g. action and text)
        List values = new ArrayList(props.size());
        for (FormProperty prop : props) {
            try {
                values.add(prop.getValue());
            }
            catch (Exception ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
        // change locale of the resource values and set them back
        Iterator it = values.iterator();
        for (FormProperty prop : props) {
            Object value = it.next();
            boolean fire = prop.isChangeFiring();
            prop.setChangeFiring(false);
            try {
                if (value instanceof I18nValue) {
                    prop.setValue(i18nService.switchLocale((I18nValue)value, designLocale));
                }
                else if (value instanceof ResourceValue) {
                    prop.setValue(resourceService.switchLocale((ResourceValue)value, designLocale));
                }
            }
            catch (Exception ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
            prop.setChangeFiring(fire);
        }
    }

    public static ResourcePanel createResourcePanel(FormProperty property, boolean force) {
        if (!isResourceableProperty(property))
            return null;

        return getResourceSupport(property).createResourcePanel0(property, force);
    }

    private ResourcePanel createResourcePanel0(FormProperty prop, boolean force) {
        if (getResourceService() != null
            && (force
                || (!isI18nAutoMode()
                    && (isResourceAutoMode()
                        || resourceService.projectUsesResources(getSourceFile())))))
        {
            return resourceService.createResourcePanel(prop.getValueType(), getSourceFile());
        }
        else return null;
    }

    public static List<FileObject> getAutomatedResourceFiles(FormModel formModel) {
        return FormEditor.getResourceSupport(formModel).getAutomatedResourceFiles();
    }

    private List<FileObject> getAutomatedResourceFiles() {
        if (isI18nAutoMode()) {
            return getI18nService().getResourceFiles(getSourceFile(), getI18nBundleName());
        } else if (isResourceAutoMode()) {
            return getResourceService().getResourceFiles(getSourceFile());
        } else {
            return Collections.EMPTY_LIST;
        }
    }

    // -----
    // listener on FormModel - reacts on component additions/removals

    private class ModelListener implements FormModelListener {
        public void formChanged(FormModelEvent[] events) {
            if (events == null) {
                return;
            }
            getI18nService();
            getResourceService();
            if (i18nService == null && resourceService == null) {
                return;
            }

            for (int i=0; i < events.length; i++) {
                FormModelEvent ev = events[i];
                switch (ev.getChangeType()) {

                case FormModelEvent.COMPONENT_REMOVED:
                    if (ev.getCreatedDeleted()) {
                        for (FormProperty prop : getComponentResourceProperties(
                                ev.getComponent(), VALID_RESOURCE_VALUE, true)) {
                            Object value = getAutoValue(prop);
                            if (value != null) {
                                // let's remove this key from properties file
                                try {
                                    if (value instanceof I18nValue && i18nService != null) {
                                        i18nService.update((I18nValue) value, null,
                                                           getSrcDataObject(), getI18nBundleName(), null,
                                                           true);
                                    }
                                    else if (value instanceof ResourceValue && resourceService != null) {
                                        resourceService.update((ResourceValue)value, null, getSourceFile(), null);
                                    }
                                }
                                catch (IOException ex) {
                                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                                }
                            }
                        }
                    }
                    break;

                case FormModelEvent.COMPONENT_ADDED:
                    if (ev.getCreatedDeleted()) {
                        RADComponent addedComp = ev.getComponent();
                        for (FormProperty prop : getComponentResourceProperties(
                                    addedComp, VALID_RESOURCE_VALUE, true)) {
                            try { // add resource/i18n value to properties file (restore)
                                Object value = prop.getValue();
                                if (value instanceof I18nValue && i18nService != null) {
                                    i18nService.update(null, (I18nValue) value,
                                                       getSrcDataObject(), getI18nBundleName(), designLocale,
                                                       false);
                                }
                                else if (value instanceof ResourceValue && resourceService != null) {
                                    resourceService.update(null, (ResourceValue) value, getSourceFile(), designLocale);
                                }
                            }
                            catch (Exception ex) {
                                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                            }
                        }
                        if (isAutoName() && addedComp != formModel.getTopRADComponent()
                                && formModel.isUndoRedoRecording()) { // (don't set name to components during undo/redo)
                            setupNameProperty(addedComp, true, true);
                        }
                    }
                    break;

                case FormModelEvent.FORM_TO_BE_SAVED:
                    if (i18nService != null)
                        i18nService.autoSave(getSrcDataObject());
                    if (resourceService != null)
                        resourceService.autoSave(getSourceFile());
                    break;

                case FormModelEvent.FORM_TO_BE_CLOSED:
                    if (i18nService != null)
                        i18nService.close(getSrcDataObject());
                    if (resourceService != null)
                        resourceService.close(getSourceFile());
                    break;
                }
            }
        }
    }

    // -----

    private Collection<FormProperty> getAllResourceProperties(int valueType) {
        Collection<RADComponent> components = formModel.getAllComponents();
        List<FormProperty> propList = new ArrayList<FormProperty>(components.size());
        for (RADComponent metacomp : components) {
            collectResourceProperties(metacomp, valueType, false, propList);
        }
        return propList;
    }

    private Collection<FormProperty> getComponentResourceProperties(
            RADComponent metacomp, int valueType, boolean recursive)
    {
        Collection<FormProperty> col = collectResourceProperties(
                metacomp, valueType, recursive, null);
        if (col == null)
            col = Collections.emptyList();
        return col;
    }

    private Collection<FormProperty> getNestedResourceProperties(
            Object value, FormProperty prop, int valueType)
    {
        Collection<FormProperty> col = collectNestedResourceProperties(
                value, prop, valueType, null);
        if (col == null)
            col = Collections.emptyList();
        return col;
    }

    private Collection<FormProperty> collectResourceProperties(
            RADComponent metacomp, int valueType, boolean recursive, Collection<FormProperty> col)
    {
        // check bean properties
        for (FormProperty prop : metacomp.getKnownBeanProperties()) {
            if (prop.isChanged() && isResourceableProperty(prop) && !isExcludedProperty0(prop))
                col = collectNestedResourceProperties(prop, valueType, col);
        }

        // check layout constraints
        if (metacomp instanceof RADVisualComponent) {
            Node.Property[] constrProps = ((RADVisualComponent)metacomp).getConstraintsProperties();
            if (constrProps != null) {
                for (Node.Property p : constrProps) {
                    if (p instanceof FormProperty) {
                        FormProperty prop = (FormProperty) p;
                        if (prop.isChanged() && isResourceableProperty(prop) && !isExcludedProperty0(prop))
                            col = collectNestedResourceProperties(prop, valueType, col);
                    }
                }
            }
        }

        // check subcomponents
        if (recursive && metacomp instanceof ComponentContainer) {
            for (RADComponent subcomp : ((ComponentContainer)metacomp).getSubBeans()) {
                col = collectResourceProperties(subcomp, valueType, recursive, col);
            }
        }

        return col;
    }

    private Collection<FormProperty> collectNestedResourceProperties(
            Object value, FormProperty property, int valueType, Collection<FormProperty> col)
    {
        Node.Property[] nestedProps = getNestedProperties(value);

        if (nestedProps == null) { // leaf property, no more nesting
            if ((valueType != PLAIN_VALUE || isResourceType(property.getValueType()))
                    && isWanted(valueType, value))
            {   // for plain value the property type must match one of the supported types;
                // for resource value only the key is checked
                if (col == null)
                    col = new LinkedList<FormProperty>();
                col.add(property);
            }
            return col;
        }

        for (Node.Property p : nestedProps) {
            if (p instanceof FormProperty) {
                FormProperty prop = (FormProperty) p;
                if (prop.isChanged() && isResourceableProperty(prop) && !isExcludedProperty0(prop))
                    col = collectNestedResourceProperties(prop, valueType, col);
            }
        }
        return col;
    }

    private Collection<FormProperty> collectNestedResourceProperties(
            FormProperty property, int valueType, Collection<FormProperty> col)
    {
        try {
            return collectNestedResourceProperties(
                    property.getValue(), property, valueType, col);
        }
        catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        return col;
    }

    private static Node.Property[] getNestedProperties(Object value) {
        if (value instanceof BorderDesignSupport) {
            return ((BorderDesignSupport)value).getProperties();
        }
        // [An alternative would be to use BeanPropertyEditor, but calling
        //  getCurrentEditor() forces searching for the PropertyEditor.
        //  Maybe not a problem because "known" properties should have it
        //  already, or will have to find it sooner or later.]
        return null;
    }

    private static boolean isWanted(int valueType, Object value) {
        String key;
        if (value instanceof ExternalValue) {
            key = ((ExternalValue)value).getKey();
            if (key == null) { // it is a complex resource value without a single key
                key = ""; // NOI18N
            }
        }
        else { // it is a plain value
            key = null;
        }

        if (key == null) {
            return (valueType & PLAIN_VALUE) != 0 && !(value instanceof FormDesignValue);
        }

        if ((valueType & UNDEFINED_RESOURCE) != 0 && key.equals(ExternalValue.COMPUTE_AUTO_KEY)) {
            return true;
        }

        if ((valueType & VALID_RESOURCE_VALUE) != 0 && !key.startsWith("#")) { // NOI18N
            return true;
        }

        return false;
    }

    // -----

    private String getComponentName(RADComponent metacomp) {
        String name = null;
        boolean rootName = false;
        if (isResourceAutoMode()) {
            FormProperty nameProp = getNameProperty(metacomp);
            // if 'name' property is set, we use its value as the name of the component
            if (nameProp != null && nameProp.isChanged()) {
                try {
                    name = (String) nameProp.getValue();
                } catch (Exception ex) { // should not fail, ignore non-strings
                }
            }
            rootName = true;
        }
        if ((metacomp != formModel.getTopRADComponent() || rootName)
            && (name == null || name.trim().length() == 0)) { // TBD some check for usable name
            // by default use the name of the variable
            name = metacomp.getName();
        }
        return name;
    }

    private String getPropertyPath(FormProperty property, String compName) {
        String propertyName = property.getName();
        List parents = new ArrayList();
        do {
            FormPropertyContext propContext = property.getPropertyContext();
            Object parent = propContext.getOwner();
            if (parent instanceof FormProperty) {
                parents.add(parent);
                property = (FormProperty) parent;
            } else {
                if (parent instanceof RADComponent) {
                    parents.add(parent);
                }
                property = null;
            }
        } while (property != null);

        StringBuilder buf = new StringBuilder();
        for (Object obj : parents) {
            String append;
            if (obj instanceof RADComponent) {
                append = compName != null ? compName : getComponentName((RADComponent)obj);
            } else {
                append = ((FormProperty)obj).getName();
            }
            if (append != null) {
                if (buf.length() > 0) {
                    buf.append("."); // NOI18N
                }
                buf.append(append);
            }
        }
        if (buf.length() > 0) {
            buf.append("."); // NOI18N
        }
        buf.append(propertyName);
        return buf.toString();
    }

    private String getDefaultKey0(FormProperty prop, int type) {
        return getDefaultKey(getSrcDataObject().getName(), getPropertyPath(prop, null)/*prop.getPropertyContext().getContextPath(), prop.getName()*/, type);
    }

    static String getDefaultKey(FormProperty prop, int type) {
        return getResourceSupport(prop).getDefaultKey0(prop, type);
    }

    private String getDefaultKey(String formName, String propPath/*, String propName*/, int type) {
        if (type != AUTO_I18N) {
            // TBD consult the I18nService for the default key...
            // TBD consult the ResourceService for the default key...
            formName = null;
        }
        StringBuilder buf = new StringBuilder();
        if (formName != null) {
            buf.append(formName).append("."); // NOI18N
        }
        buf.append(propPath);
        return buf.toString();
    }

    private String getDefaultKey(FormProperty prop, ExternalValue eValue) {
        if (eValue instanceof I18nValue) {
            return getDefaultKey0(prop, AUTO_I18N);
        } else if (eValue instanceof ResourceValue) {
            return getDefaultKey0(prop, AUTO_RESOURCING);
        } else {
            return null;
        }
    }

    private FileObject getSourceFile() {
        DataObject dobj = FormEditor.getFormDataObject(formModel);
        return dobj != null ? dobj.getPrimaryFile() : null;
    }

    private DataObject getSrcDataObject() {
        return FormEditor.getFormDataObject(formModel);
    }

    private int getAutoMode() {
        return formModel.getSettings().getResourceAutoMode();
    }

    private boolean isAutoMode() {
        return getAutoMode() != AUTO_OFF;
    }

    private boolean isResourceAutoMode() {
        int mode = formModel.getSettings().getResourceAutoMode();
        return getResourceService() != null
               && (mode == AUTO_RESOURCING || mode == AUTO_INJECTION);
    }

    private boolean isI18nAutoMode() {
        return getI18nService() != null
               && formModel.getSettings().getResourceAutoMode() == AUTO_I18N;
    }

    private boolean isAutoName() {
        return formModel.getSettings().getAutoSetComponentName(); // || getAutoMode() == AUTO_INJECTION;
    }

    private String getI18nBundleName() {
        String bundleName = formModel.getSettings().getFormBundle();
        if (bundleName == null) {
            if (defaultI18nBundle == null) {
                FileObject file = getSourceFile();
                defaultI18nBundle = composeBundleName(getPkgResourceName(getSourceFile()),
                                                      DEFAULT_BUNDLE_NAME);
                // [we could also search for another properties file in the package
                // (what if there is properties file not for i18n?),
                // or try to remember last specified properties file for another form in this package]
            }
            bundleName = defaultI18nBundle;
        }
        return bundleName;
    }

    private static String getPkgResourceName(FileObject fo) {
        ClassPath cp = ClassPath.getClassPath(fo, ClassPath.SOURCE);
        if (cp != null) {
            return cp.getResourceName(fo.isFolder() ? fo : fo.getParent());
        } else {
            return null;
        }
    }

    private static String composeBundleName(String pkgResName, String bundleSimpleName) {
        return pkgResName != null && pkgResName.length() > 0
                ? pkgResName + "/" + bundleSimpleName : bundleSimpleName; // NOI18N
    }

    private static boolean isResourceType(Class type) {
        return type == String.class
               || java.awt.Font.class.isAssignableFrom(type)
               || javax.swing.Icon.class.isAssignableFrom(type)
               || java.awt.Color.class.isAssignableFrom(type);
    }

    private static boolean isConvertibleToResource(Object value) {
        return value instanceof String
               || value instanceof java.awt.Font
               || value instanceof org.netbeans.modules.form.editors.IconEditor.NbImageIcon
               || value instanceof java.awt.Color;
    }

    /**
     * Returns value of the property if it is an ExternalValue with a default
     * key (i.e. used exclusively only in this property).
     */
    private ExternalValue getAutoValue(FormProperty prop) {
        Object value;
        try {
            value = prop.getValue();
        }
        catch (Exception ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            return null;
        }
        ExternalValue eVal;
        if (value instanceof ExternalValue) {
            eVal = (ExternalValue) value;
            if (isAutoValue(eVal, getDefaultKey(prop, eVal))) {
                return eVal;
            }
        }
        return null;
    }

    private static boolean isAutoValue(ExternalValue value, String defaultKey) {
        String key = (value != null) ? value.getKey() : null;
        return key != null ? isAutoKey(key, defaultKey) : false;
    }

    private static boolean isAutoKey(String key, String defaultKey) {
        return key != null && key.startsWith(defaultKey);
    }

    private static String getStringValue(FormProperty prop, Object value) {
        if (value instanceof String)
            return (String) value;

        PropertyEditor prEd = prop.getCurrentEditor();
        prEd.setValue(value);
        return prEd.getAsText(); // [this does not work correctly with IconEditor...]
    }

    // -----

    Node.Property[] createFormProperties() {
        Node.Property autoNamingProp = new PropertySupport.ReadWrite(
            FormLoaderSettings.PROP_AUTO_SET_COMPONENT_NAME,
            Boolean.TYPE,
            FormUtils.getBundleString("PROP_AUTO_SET_COMPONENT_NAME"), // NOI18N
            FormUtils.getBundleString("HINT_AUTO_SET_COMPONENT_NAME")) // NOI18N
        {
            public void setValue(Object value) {
                Object oldValue = getValue();
                if (!oldValue.equals(value)) {
                    boolean autoName = ((Boolean)value).booleanValue();
                    formModel.getSettings().setAutoSetComponentName(autoName);

                    setupNameProperty(autoName);

                    formModel.fireSyntheticPropertyChanged(
                            null, PROP_AUTO_SET_COMPONENT_NAME, oldValue, value);
                    FormEditor.getFormEditor(formModel).getFormRootNode()
                        .firePropertyChangeHelper(PROP_AUTO_SET_COMPONENT_NAME, oldValue, value);
                }
            }

            public Object getValue() {
                return Boolean.valueOf(formModel.getSettings().getAutoSetComponentName());
            }
        };

        Node.Property autoModeProp;
        int mode = getAutoMode();
        if (projectUsesResources() || mode == AUTO_RESOURCING || mode == AUTO_INJECTION) {
            autoModeProp = new PropertySupport.ReadWrite(
                PROP_AUTO_RESOURCING,
                Integer.TYPE,
                FormUtils.getBundleString("PROP_AUTO_RESOURCE"), // NOI18N
                FormUtils.getBundleString("HINT_AUTO_RESOURCE_LOCAL")) // NOI18N
            {
                public void setValue(Object value) {
                    int oldMode = getAutoMode();
                    Integer oldValue = new Integer(oldMode);
                    if (!oldValue.equals(value)) {
                        int newMode = ((Integer)value).intValue();
                        FormSettings settings = formModel.getSettings();
                        // set the setting itself so it is "on" during processing the
                        // resource values (to correctly determine names of components
                        // for default keys)
                        if (newMode == AUTO_OFF) {
                            switchFormToPlainValues(null);
                            settings.setResourceAutoMode(newMode);
                        } else {
                            if (oldMode == AUTO_I18N || newMode == AUTO_I18N) {
                                switchFormToPlainValues(null);
                                settings.setResourceAutoMode(newMode);
                                switchFormToResources();
                            } else { // only chaning between resources and resources with injection
                                settings.setResourceAutoMode(newMode);
                            }
                            if (newMode == AUTO_INJECTION && !isAutoName()) {
                                formModel.getSettings().setAutoSetComponentName(true);
                                setupNameProperty(true);
                                FormEditor.getFormEditor(formModel).getFormRootNode()
                                    .firePropertyChangeHelper(PROP_AUTO_SET_COMPONENT_NAME, oldValue, value);
                            }
                        }

                        formModel.fireSyntheticPropertyChanged(
                                null, PROP_AUTO_RESOURCING, oldValue, value);
                        FormEditor.getFormEditor(formModel).getFormRootNode()
                            .firePropertyChangeHelper(PROP_AUTO_RESOURCING, oldValue, value);
                    }
                }

                public Object getValue() {
                    return getAutoMode();
                }

                public PropertyEditor getPropertyEditor() {
                    return new org.netbeans.modules.form.editors.EnumEditor(new Object[] {
                        FormUtils.getBundleString("CTL_AUTO_OFF"), AUTO_OFF, "", // NOI18N
                        FormUtils.getBundleString("CTL_AUTO_I18N"), AUTO_I18N, "", // NOI18N
                        FormUtils.getBundleString("CTL_AUTO_RESOURCING"), AUTO_RESOURCING, "", // NOI18N
                        FormUtils.getBundleString("CTL_AUTO_INJECTION"), AUTO_INJECTION, "" // NOI18N
                    });
                }
            };
        } else { // only offer automatic internationalization
            autoModeProp = new PropertySupport.ReadWrite(
                PROP_AUTO_RESOURCING,
                Boolean.TYPE,
                FormUtils.getBundleString("PROP_AUTO_I18N"), // NOI18N
                FormUtils.getBundleString("HINT_AUTO_I18N")) // NOI18N
            {
                public void setValue(Object value) {
                    boolean oldAutoI18n = getAutoMode() == AUTO_I18N;
                    Boolean oldValue = Boolean.valueOf(oldAutoI18n);
                    if (!oldValue.equals(value)) {
                        boolean newAutoI18n = ((Boolean)value).booleanValue();
                        FormSettings settings = formModel.getSettings();
                        // set the setting itself so it is "on" during processing the
                        // resource values (to correctly determine names of components
                        // for default keys)
                        if (newAutoI18n) {
                            settings.setResourceAutoMode(AUTO_I18N);
                            switchFormToResources();
                        } else {
                            switchFormToPlainValues(null);
                            settings.setResourceAutoMode(AUTO_OFF);
                        }

                        formModel.fireSyntheticPropertyChanged(
                                null, PROP_AUTO_RESOURCING, oldValue, value);
                        FormEditor.getFormEditor(formModel).getFormRootNode()
                            .firePropertyChangeHelper(PROP_AUTO_RESOURCING, oldValue, value);
                    }
                }

                public Object getValue() {
                    return getAutoMode() == AUTO_I18N;
                }
            };
        }

        Node.Property formBundleProp = new PropertySupport.ReadWrite(
            PROP_FORM_BUNDLE,
            String.class,
            FormUtils.getBundleString("PROP_FORM_BUNDLE"), // NOI18N
            FormUtils.getBundleString("HINT_FORM_BUNDLE")) // NOI18N
        {
            public void setValue(Object value) {
                String oldValue = getI18nBundleName();
                if ((oldValue == null && value != null) || !oldValue.equals(value)) {
                    String resourceName = (String) value;
                    if (resourceName != null && resourceName.toLowerCase().endsWith(".properties")) { // NOI18N
                        resourceName = resourceName.substring(
                                0, resourceName.length()-".properties".length()); // NOI18N
                    }
                    formModel.getSettings().setFormBundle(resourceName);
                    bundleChanged(oldValue);
                    formModel.fireSyntheticPropertyChanged(null, PROP_FORM_BUNDLE, oldValue, value);
                    FormEditor.getFormEditor(formModel).getFormRootNode()
                            .firePropertyChangeHelper(PROP_FORM_BUNDLE, oldValue, value);
                }
            }
            public Object getValue() {
                return getI18nBundleName();
            }

            public PropertyEditor getPropertyEditor() {
                return new BundleFilePropertyEditor();
            }
        };

        Node.Property localeProp = new PropertySupport.ReadWrite(
            PROP_DESIGN_LOCALE,
            String.class,
            FormUtils.getBundleString("PROP_DESIGN_LOCALE"), // NOI18N
            FormUtils.getBundleString("HINT_DESIGN_LOCALE")) // NOI18N
        {
            public void setValue(Object value) {
                // this property is not persistent (not stored in .form file)
                String oldValue = designLocale;
                changeDesignLocale((String)value);
                formModel.fireSyntheticPropertyChanged(null, PROP_DESIGN_LOCALE, oldValue, value);
                FormEditor.getFormEditor(formModel).getFormRootNode()
                        .firePropertyChangeHelper(PROP_DESIGN_LOCALE, oldValue, value);
            }

            public Object getValue() {
                return designLocale;
            }

            public PropertyEditor getPropertyEditor() {
                return new LocalePropertyEditor();
            }
        };

        int autoMode = getAutoMode();
        return autoMode == AUTO_OFF || autoMode == AUTO_I18N ?
            new Node.Property[] { autoNamingProp, autoModeProp, formBundleProp, localeProp } :
            new Node.Property[] { autoNamingProp, autoModeProp, localeProp };
    }

    private class BundleFilePropertyEditor extends PropertyEditorSupport {
        public boolean supportsCustomEditor() {
            return getI18nService() != null;
        }

        public Component getCustomEditor() {
            return getI18nService() != null ?
                i18nService.getBundleSelectionComponent(this, getSourceFile()) :
                null;
        }
    }

    private class LocalePropertyEditor extends PropertyEditorSupport {
        private String[][] tags;

        public String[] getTags() {
            if (tags == null) {
                FileObject srcFile = getSourceFile();
                if (srcFile != null) { // might be called even if the form is already closed
                    if (isI18nAutoMode())
                        tags = i18nService.getAvailableLocales(srcFile, getI18nBundleName());
                    else if (isResourceAutoMode())
                        tags = resourceService.getAvailableLocales(srcFile);
                }
            }
            return tags != null ? tags[1] : null;
        }

        public void setAsText(String text) {
            getTags();
            if (tags != null) {
                for (int i=0,n=tags[0].length; i < n; i++) {
                    if (tags[1][i].equals(text)) {
                        setValue(tags[0][i]);
                        return;
                    }
                }
            }
            setValue(text);
        }

        public String getAsText() {
            Object value = getValue();
            getTags();
            if (tags != null) {
                for (int i=0,n=tags[0].length; i < n; i++) {
                    if (tags[0][i].equals(value))
                        return tags[1][i];
                }
            }
            return value != null ? value.toString() : null;
        }

        public boolean supportsCustomEditor() {
            return getTags() != null;
        }

        public Component getCustomEditor() {
            if (isI18nAutoMode())
                return i18nService.getCreateLocaleComponent(this, getSourceFile(), getI18nBundleName());
            else if (isResourceAutoMode())
                return resourceService.getCreateLocaleComponent(this, getSourceFile());

            return null;
        }
    }
}
