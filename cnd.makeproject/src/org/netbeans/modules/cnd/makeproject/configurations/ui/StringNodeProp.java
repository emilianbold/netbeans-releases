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

package org.netbeans.modules.cnd.makeproject.configurations.ui;

import org.openide.nodes.PropertySupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.StringConfiguration;

public class StringNodeProp extends PropertySupport {
    private StringConfiguration stringConfiguration;
    private String def = null;

    public StringNodeProp(StringConfiguration stringConfiguration, String txt1, String txt2, String txt3) {
        super(txt1, String.class, txt2, txt3, true, true);
        this.stringConfiguration = stringConfiguration;
    }
    
    public StringNodeProp(StringConfiguration stringConfiguration, String def, String txt1, String txt2, String txt3) {
        super(txt1, String.class, txt2, txt3, true, true);
        this.stringConfiguration = stringConfiguration;
        this.def = def;
    }
    
    public String getHtmlDisplayName() {
        if (stringConfiguration.getModified())
            return "<b>" + getDisplayName(); // NOI18N
        else
            return null;
    }
    
    public Object getValue() {
        return stringConfiguration.getValueDef(def);
    }
    
    public void setValue(Object v) {
        stringConfiguration.setValue((String)v);
    }
    
    public void restoreDefaultValue() {
        stringConfiguration.reset();
    }
    
    public boolean supportsDefaultValue() {
        return true;
    }
    
    public boolean isDefaultValue() {
        return !stringConfiguration.getModified();
    }
}
