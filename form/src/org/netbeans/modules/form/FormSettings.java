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

import java.util.*;
import org.netbeans.modules.form.project.ClassPathUtils;

/**
 * Settings for one form.
 *
 * @author Jan Stola
 */
public class FormSettings {
    /** Prefix for session settings. */
    private static final String SESSION_PREFIX = "Session_"; // NOI18N
    private FormModel formModel;
    private Map settings = new TreeMap();

    FormSettings(FormModel formModel) {
        this.formModel = formModel;

        // Variables Modifier
        int variablesModifier = FormLoaderSettings.getInstance().getVariablesModifier();
        settings.put(FormLoaderSettings.PROP_VARIABLES_MODIFIER, new Integer(variablesModifier));
        
        // Local Variables
        boolean localVariables = FormLoaderSettings.getInstance().getVariablesLocal();
        settings.put(FormLoaderSettings.PROP_VARIABLES_LOCAL, Boolean.valueOf(localVariables));
        
        // Generate Mnemonics Code
        boolean generateMnemonicsCode = FormLoaderSettings.getInstance().getGenerateMnemonicsCode();
        settings.put(FormLoaderSettings.PROP_GENERATE_MNEMONICS, Boolean.valueOf(generateMnemonicsCode));
        
        // Listener Generation Style
        int listenerGenerationStyle = FormLoaderSettings.getInstance().getListenerGenerationStyle();
        settings.put(FormLoaderSettings.PROP_LISTENER_GENERATION_STYLE, new Integer(listenerGenerationStyle));
    }

    // -----
    // code generation

    public int getVariablesModifier() {
        Integer variablesModifier = (Integer)settings.get(FormLoaderSettings.PROP_VARIABLES_MODIFIER);
        return variablesModifier.intValue();
    }
    
    public void setVariablesModifier(int value) {
        settings.put(FormLoaderSettings.PROP_VARIABLES_MODIFIER, new Integer(value));
    }
    
    public boolean getVariablesLocal() {
        Boolean variablesLocal = (Boolean)settings.get(FormLoaderSettings.PROP_VARIABLES_LOCAL);
        return variablesLocal.booleanValue();
    }
    
    public void setVariablesLocal(boolean value) {
        settings.put(FormLoaderSettings.PROP_VARIABLES_LOCAL, Boolean.valueOf(value));
    }
    
    public boolean getAutoSetComponentName() {
        Boolean setting = (Boolean) settings.get(FormLoaderSettings.PROP_AUTO_SET_COMPONENT_NAME);
        boolean autoName;
        if (setting != null) {
            autoName = setting.booleanValue();
        } else {
            autoName = getDefaultAutoSetComponentName();
            setAutoSetComponentName(autoName);
        }
        return autoName;
    }

    public void setAutoSetComponentName(boolean setName) {
        settings.put(FormLoaderSettings.PROP_AUTO_SET_COMPONENT_NAME, Boolean.valueOf(setName));
    }

    boolean getDefaultAutoSetComponentName() {
        int globalNaming = FormLoaderSettings.getInstance().getAutoSetComponentName();
        boolean autoName = globalNaming == FormLoaderSettings.AUTO_NAMING_ON;
        if (globalNaming == FormLoaderSettings.AUTO_NAMING_DEFAULT) {
            ResourceSupport resourceSupport = FormEditor.getResourceSupport(formModel);
            if (resourceSupport.projectUsesResources()) {
                autoName = true;
            }
        }
        return autoName;
    }

    public boolean getGenerateMnemonicsCode() {
        Boolean generateMnemonicsCode = (Boolean)settings.get(FormLoaderSettings.PROP_GENERATE_MNEMONICS);
        return generateMnemonicsCode.booleanValue();
    }
    
    public void setGenerateMnemonicsCode(boolean value) {
        settings.put(FormLoaderSettings.PROP_GENERATE_MNEMONICS, Boolean.valueOf(value));
    }
    
    public int getListenerGenerationStyle() {
        Integer listenerGenerationStyle = (Integer)settings.get(FormLoaderSettings.PROP_LISTENER_GENERATION_STYLE);
        return listenerGenerationStyle.intValue();
    }
    
    public void setListenerGenerationStyle(int value) {
        settings.put(FormLoaderSettings.PROP_LISTENER_GENERATION_STYLE, new Integer(value));
    }

    public int getLayoutCodeTarget() {
        return checkLayoutCodeTarget();
    }

    public void setLayoutCodeTarget(int value) {
        settings.put(FormLoaderSettings.PROP_LAYOUT_CODE_TARGET, new Integer(value));
    }

    private int checkLayoutCodeTarget() {
        Integer lctSetting = (Integer)settings.get(FormLoaderSettings.PROP_LAYOUT_CODE_TARGET);
        int layoutCodeTarget;
        if (lctSetting != null) {
            layoutCodeTarget = lctSetting.intValue();
        }
        else { // no setting
            layoutCodeTarget = FormEditor.getFormEditor(formModel).needPostCreationUpdate() ?
                JavaCodeGenerator.LAYOUT_CODE_AUTO : // newly created form - detect
                JavaCodeGenerator.LAYOUT_CODE_LIBRARY; // old form - use library
        }
        if (layoutCodeTarget == JavaCodeGenerator.LAYOUT_CODE_AUTO) {
            int globalLCT = FormLoaderSettings.getInstance().getLayoutCodeTarget();
            if (globalLCT == JavaCodeGenerator.LAYOUT_CODE_AUTO) {
                layoutCodeTarget = ClassPathUtils.isJava6ProjectPlatform(
                        FormEditor.getFormDataObject(formModel).getPrimaryFile()) ?
                    JavaCodeGenerator.LAYOUT_CODE_JDK6 : JavaCodeGenerator.LAYOUT_CODE_LIBRARY;
            }
            else layoutCodeTarget = globalLCT;
            setLayoutCodeTarget(layoutCodeTarget);
        }
        else if (lctSetting == null) {
            setLayoutCodeTarget(layoutCodeTarget);
        }

        return layoutCodeTarget;
    }

    // -----
    // resource management / internationalization

    // for compatibility
    private static final String PROP_AUTO_I18N = "i18nAutoMode"; // NOI18N

    void setResourceAutoMode(int value) {
        settings.put(ResourceSupport.PROP_AUTO_RESOURCING, value);
        settings.put(PROP_AUTO_I18N, value == ResourceSupport.AUTO_I18N); // for compatibility
    }

    int getResourceAutoMode() {
        Integer resSetting = (Integer) settings.get(ResourceSupport.PROP_AUTO_RESOURCING);
        int resAutoMode = ResourceSupport.AUTO_OFF;
        if (resSetting != null) {
            resAutoMode = resSetting.intValue();
        }
        else {
            Boolean i18nSetting = (Boolean) settings.get(PROP_AUTO_I18N);
            if (i18nSetting != null) {
                if (Boolean.TRUE.equals(i18nSetting))
                    resAutoMode = ResourceSupport.AUTO_I18N;
            }
            else { // no setting available
                if (FormEditor.getFormEditor(formModel).needPostCreationUpdate()) {
                    int globalResAutoMode = FormLoaderSettings.getInstance().getI18nAutoMode();
                    if (globalResAutoMode == FormLoaderSettings.AUTO_RESOURCE_ON) {
                        ResourceSupport resourceSupport = FormEditor.getResourceSupport(formModel);
                        if (resourceSupport.projectUsesResources())
                            resAutoMode = ResourceSupport.AUTO_RESOURCING; // only if app framework already on cp
                        else
                            resAutoMode = ResourceSupport.AUTO_I18N;
                    }
                    else if (globalResAutoMode == FormLoaderSettings.AUTO_RESOURCE_DEFAULT) { // detect
                        ResourceSupport resourceSupport = FormEditor.getResourceSupport(formModel);
                        if (resourceSupport.projectUsesResources())
                            resAutoMode = ResourceSupport.AUTO_RESOURCING; // only if app framework already on cp
                        else if (resourceSupport.isDefaultInternationalizableProject())
                            resAutoMode = ResourceSupport.AUTO_I18N; // NBM project
                    }
                }
                setResourceAutoMode(resAutoMode);
            }
        }
        return resAutoMode;
    }

    public boolean isI18nAutoMode() {
        return getResourceAutoMode() == ResourceSupport.AUTO_I18N;
    }

    public void setFormBundle(String bundleName) {
        settings.put(ResourceSupport.PROP_FORM_BUNDLE, bundleName);
    }

    public String getFormBundle() {
        return (String) settings.get(ResourceSupport.PROP_FORM_BUNDLE);
    }

    // design locale is not persisted in settings

    // -----


    public void set(String name, Object value) {
        set(name, value, false);
    }

    public void set(String name, Object value, boolean session) {
        if (session) {
            name = SESSION_PREFIX + name;
        }
        settings.put(name, value);
    }
    
    public Object get(String name) {
        Object value;
        if (settings.containsKey(name)) {
            value = settings.get(name);
        } else {
            value = settings.get(SESSION_PREFIX + name);
        }
        return value;
    }

    boolean isSessionSetting(String name) {
        return name.startsWith(SESSION_PREFIX);
    }
    
    Map allSettings() {
        return Collections.unmodifiableMap(settings);
    }

}
