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

import java.util.Arrays;

/**
 *
 * @author Karol Harezlak
 */
public class GroupPropertyEditor extends DesignPropertyEditor {

    private GroupValue value;
    
    @Override
    public String getAsText() {
        return super.getAsText();
    }
    
    @Override
    public void setAsText(String text) throws IllegalArgumentException {}
   
    public final void setValue(Object value) {
        if (! (value instanceof GroupValue))
            throw new IllegalArgumentException();
        
        GroupValue currentValue = (GroupValue) value;
        GroupValue newValue = new GroupValue(Arrays.asList(currentValue.getPropertyNames()));
        
        for (String propertyName : currentValue.getPropertyNames()) {
            newValue.putValue(propertyName, currentValue.getValue(propertyName));
        }
        this.value = newValue;
        firePropertyChange();
    }
    //TODO Its better to check if value is not null because it means that editor is in invalid state;  
    public GroupValue getValue() {
        return value;
    }

}