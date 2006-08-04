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
package org.netbeans.api.mobility.project;

import org.netbeans.spi.mobility.project.PropertyParser;

/**
 *
 * @author Adam Sotona
 */
public final class PropertyDescriptor {
    
    private final String name;
    private final boolean shared;
    private final PropertyParser parser;
    private final String defaultValue;
    
    public PropertyDescriptor(String name, boolean shared, PropertyParser parser ) {
        this(name, shared, parser, null);
    }
    
    public PropertyDescriptor(String name, boolean shared, PropertyParser parser, String defaultValue ) {
        this.name = name;
        this.shared = shared;
        this.parser = parser;
        this.defaultValue = defaultValue;
    }
    
    public String getName() {
        return name;
    }
    
    public boolean isShared() {
        return shared;
    }
    
    public PropertyParser getPropertyParser() {
        return parser;
    }
    
    public String getDefaultValue() {
        return defaultValue;
    }
    
    public PropertyDescriptor clone(String newName) {
        return new PropertyDescriptor(newName, shared, parser, defaultValue);
    }
}
