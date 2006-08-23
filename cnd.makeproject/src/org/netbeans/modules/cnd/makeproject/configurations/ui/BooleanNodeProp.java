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
import org.netbeans.modules.cnd.makeproject.api.configurations.BooleanConfiguration;

public class BooleanNodeProp extends PropertySupport {
    private BooleanConfiguration booleanConfiguration;

    public BooleanNodeProp(BooleanConfiguration booleanConfiguration, boolean canWrite, String name1, String name2, String name3) {
        super(name1, Boolean.class, name2, name3, true, canWrite);
        this.booleanConfiguration = booleanConfiguration;
    }
    
    public String getHtmlDisplayName() {
        if (booleanConfiguration.getModified())
            return "<b>" + getDisplayName(); // NOI18N
        else
            return null;
    }
    
    public Object getValue() {
        return new Boolean(booleanConfiguration.getValue());
    }
    
    public void setValue(Object v) {
        booleanConfiguration.setValue(((Boolean)v).booleanValue());
    }
    
    public void restoreDefaultValue() {
        booleanConfiguration.reset();
    }
    
    public boolean supportsDefaultValue() {
        return true;
    }
    
    public boolean isDefaultValue() {
        return !booleanConfiguration.getModified();
    }
}
