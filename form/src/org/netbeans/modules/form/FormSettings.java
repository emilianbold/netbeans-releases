/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form;

import java.util.*;

/**
 * Settings for one form.
 *
 * @author Jan Stola
 */
public class FormSettings {
    private Map settings = new TreeMap();
    
    FormSettings() {
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
    
    void set(String name, Object value) {
        settings.put(name, value);
    }
    
    Map allSettings() {
        return Collections.unmodifiableMap(settings);
    }

}
