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

/**
 * Super class for all the various modifiers.
 *
 * @author ptliu
 */
abstract class Modifier {
    private Enum configurable;
    private Object source;
    private Configurator configurator;

    /** Creates a new instance of Modifier */
    public Modifier(Enum configurable, Object source,
            Configurator configurator) {

        assert configurable != null;
        assert source != null;
        assert configurator != null;

        this.configurable = configurable;
        this.source = source;
        this.configurator = configurator;
    }
    
    public Enum getConfigurable() {
        return configurable;
    }
    
    public Configurator getConfigurator() {
        return configurator;
    }
    
    public Object getSource() {
        return source;
    }
    
    public abstract Object getValue();
    
    public abstract void setValue(Object value);
    
    public void copyData() {
        configurator.setValue(configurable, getValue());
    }
}
