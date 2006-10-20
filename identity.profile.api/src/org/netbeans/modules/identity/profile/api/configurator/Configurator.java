/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.identity.profile.api.configurator;

import java.util.HashMap;
import java.util.Map;

/** 
 * Super class for the various configurators.
 *
 * @author ptliu
 */
public abstract class Configurator {
       
    public enum AccessMethod {
        DYNAMIC, FILE
    }
   
    private Map<Modifier, Enum> configurableMap;
    private Map<Enum, Modifier> modifierMap;
    private Object configuration;

    /** Creates a new instance of Configurator */
    public Configurator() {
        configurableMap = new HashMap<Modifier, Enum>();
        modifierMap = new HashMap<Enum, Modifier>();
    }

    /**
     * Register an external modifier that is used to modify
     * a Configurable.  The modifier can be a Swing component
     * such as a JTextField, etc.
     *
     */
    protected void addModifierInternal(Enum configurable, Object source) {
        Modifier modifier = ConfiguratorSupport.createModifier(configurable,
                source, this);
  
        configurableMap.put(modifier, configurable);
        modifierMap.put(configurable, modifier);
    }
    
    protected void copyDataFromModifiers() {
        for (Modifier modifier : modifierMap.values()) {
            modifier.copyData();
        }
    }
    
    public abstract void setValue(Enum configurable, Object value);
    
    public abstract Object getValue(Enum configurable);
    
    public abstract void save();
    
    public abstract String validate();
    
    public abstract void enable();
    
    public abstract void disable();
    
    Object getConfiguration() {
        return configuration;
    }
    
    void setConfiguration(Object configuration) {
        this.configuration = configuration;
    }
    
}
