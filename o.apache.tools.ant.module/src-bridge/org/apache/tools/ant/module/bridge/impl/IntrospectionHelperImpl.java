/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
