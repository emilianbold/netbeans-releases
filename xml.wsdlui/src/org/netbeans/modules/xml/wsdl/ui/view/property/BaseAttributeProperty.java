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

package org.netbeans.modules.xml.wsdl.ui.view.property;

import org.netbeans.modules.xml.wsdl.ui.api.property.Writable;
import org.openide.nodes.PropertySupport;

/**
 * Provides a default implementation of PropertySupport.Reflection.
 * This class knows whether the model is writable or not and changes as per the model's read/write capability.
 *
 * @author skini
 *
 */
public class BaseAttributeProperty extends PropertySupport.Reflection {

    Writable writable;

    public BaseAttributeProperty(Writable writable, Class type, String getter, String setter) throws NoSuchMethodException {
		super(writable, type, getter, setter);
        this.writable = writable;
	}

    public BaseAttributeProperty(Writable writable, Class valueType, String argName) throws NoSuchMethodException {
        super(writable, valueType, argName);
        this.writable = writable;
    }

    @Override
    public boolean canWrite() {
        return writable.isWritable();
    }
    
    
    
}