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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.xml.wsdl.ui.api.property;

import java.lang.reflect.InvocationTargetException;

import org.netbeans.modules.xml.xam.ui.XAMUtils;
import org.openide.ErrorManager;
import org.openide.nodes.PropertySupport;

/**
 * Provides a default implementation of PropertySupport.Reflection.
 * Can be used to show attributes with default values.
 *
 * @author skini
 *
 */
public class StringAttributeProperty extends PropertySupport.Reflection {

    ExtensibilityElementPropertyAdapter adapter;

    public StringAttributeProperty(ExtensibilityElementPropertyAdapter adapter, Class type, String getter, String setter) throws NoSuchMethodException {
		super(adapter, type, getter, setter);
        this.adapter = adapter;
	}
    

    @Override
    public boolean isDefaultValue() {
        try {
            return getValue().equals(adapter.getDefaultValue());
        } catch (IllegalArgumentException e) {
            ErrorManager.getDefault().notify(e);
        } catch (IllegalAccessException e) {
            ErrorManager.getDefault().notify(e);
        } catch (InvocationTargetException e) {
            ErrorManager.getDefault().notify(e);
        }
        return false;
    }

    @Override
    public void restoreDefaultValue() throws IllegalAccessException, InvocationTargetException {
        setValue(adapter.getDefaultValue());
    }

    @Override
    public boolean supportsDefaultValue() {
        return adapter.supportsDefaultValue();
    }
    
    @Override
    public boolean canWrite() {
        return XAMUtils.isWritable(adapter.getExtensibilityElement().getModel());
    }
    
    
    
    
}