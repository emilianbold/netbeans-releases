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
    // internationalization

    public void setI18nAutoMode(boolean value) {
        settings.put(FormLoaderSettings.PROP_AUTO_I18N, value ? Boolean.TRUE : Boolean.FALSE);
    }

    public boolean getI18nAutoMode() {
        Boolean i18nSetting = (Boolean) settings.get(FormLoaderSettings.PROP_AUTO_I18N);
        boolean i18nAutoMode;
        if (i18nSetting != null) {
            i18nAutoMode = i18nSetting.booleanValue();
        }
        else { // no setting available
            if (FormEditor.getFormEditor(formModel).needPostCreationUpdate()) {
                int globalI18nAutoMode = FormLoaderSettings.getInstance().getI18nAutoMode();
                if (globalI18nAutoMode == FormLoaderSettings.AUTO_I18N_DEFAULT) { // detect
                    i18nAutoMode = FormEditor.getI18nSupport(formModel).isDefaultInternationalizableProject();
                }
                else i18nAutoMode = (globalI18nAutoMode == FormLoaderSettings.AUTO_I18N_ON);
            }
            else i18nAutoMode = false;
            setI18nAutoMode(i18nAutoMode);
        }
        return i18nAutoMode;
    }

    public void setFormBundle(String bundleName) {
        settings.put(I18nSupport.PROP_FORM_BUNDLE, bundleName);
    }

    public String getFormBundle() {
        return (String) settings.get(I18nSupport.PROP_FORM_BUNDLE);
    }

    // -----

    void set(String name, Object value) {
        settings.put(name, value);
    }
    
    Map allSettings() {
        return Collections.unmodifiableMap(settings);
    }

}
