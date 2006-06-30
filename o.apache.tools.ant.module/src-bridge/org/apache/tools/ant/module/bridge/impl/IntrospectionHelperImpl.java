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

package org.apache.tools.ant.module.bridge.impl;

import java.util.Enumeration;
import org.apache.tools.ant.IntrospectionHelper;
import org.apache.tools.ant.module.bridge.IntrospectionHelperProxy;

/**
 * @author Jesse Glick
 */
final class IntrospectionHelperImpl implements IntrospectionHelperProxy {

    private final IntrospectionHelper helper;

    public IntrospectionHelperImpl(Class c) {
        helper = IntrospectionHelper.getHelper(c);
    }

    public Class getAttributeType(String name) {
        return helper.getAttributeType(name);
    }
    
    @SuppressWarnings("unchecked") // XXX better to make Generics.checkedEnumeration?
    public Enumeration<String> getAttributes() {
        return helper.getAttributes();
    }
    
    public Class getElementType(String name) {
        return helper.getElementType(name);
    }
    
    @SuppressWarnings("unchecked")
    public Enumeration<String> getNestedElements() {
        return helper.getNestedElements();
    }
    
    public boolean supportsCharacters() {
        return helper.supportsCharacters();
    }
    
}
