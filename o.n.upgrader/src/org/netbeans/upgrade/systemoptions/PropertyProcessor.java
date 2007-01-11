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

package org.netbeans.upgrade.systemoptions;

import java.util.HashMap;
import java.util.Map;


abstract class PropertyProcessor  {
    private String className;
    private static Map<String, String> results;
    private static Map<String, PropertyProcessor> clsname2Delegate = new HashMap<String, PropertyProcessor>();
    
    static {
        //To extend behaviour of this class then regisetr your own implementation
        registerPropertyProcessor(new TaskTagsProcessor());
        registerPropertyProcessor(new HostPropertyProcessor());
        registerPropertyProcessor(new FileProcessor());//AntSettings
        registerPropertyProcessor(new NbClassPathProcessor());//AntSettings
        registerPropertyProcessor(new HashMapProcessor());//AntSettings
        registerPropertyProcessor(new IntrospectedInfoProcessor());//AntSettings                
        registerPropertyProcessor(new ListProcessor());//ProjectUISettings             
        registerPropertyProcessor(new URLProcessor());//ProjectUISettings             
        registerPropertyProcessor(new ColorProcessor());//FormLoaderSettings
        registerPropertyProcessor(new StringPropertyProcessor());//ProxySettings
    }
    

    private static void registerPropertyProcessor(PropertyProcessor instance) {
        if (clsname2Delegate.put(instance.className, instance) != null) {
            throw new IllegalArgumentException();
        }
    }
    
    private static PropertyProcessor DEFAULT = new PropertyProcessor(false) {
        void processPropertyImpl(final String propertyName, final Object value) {
            String stringvalue = null;
            stringvalue = Utils.valueFromObjectWrapper(value);
            addProperty(propertyName, stringvalue);
        }
    };
    
    private static PropertyProcessor TYPES = new PropertyProcessor(true) {
        void processPropertyImpl(final String propertyName, final Object value) {
            addProperty(propertyName, Utils.getClassNameFromObject(value));
        }        
    };
    
    private boolean types;
    
    
    private PropertyProcessor(boolean types) {
        this.types = types;
    }
    
    protected PropertyProcessor(String className) {
        this(false);        
        this.className = className;
    }
    
    static Map<String, String> processProperty(String propertyName, Object value, boolean types) {
        results = new HashMap<String, String>();
        PropertyProcessor p = (types) ? TYPES : findDelegate(value);
        if (p == null) {
            p = DEFAULT;
        }
        assert p != null;
        p.processPropertyImpl(propertyName, value);
        return results;
    }
    
    abstract void processPropertyImpl(String propertyName, Object value);
    
    protected final void addProperty(String propertyName, String value) {
        if (results.put(propertyName, value) != null) {
            throw new IllegalArgumentException(propertyName);
        }
    }
    
    private static PropertyProcessor findDelegate(final Object value) {
        String clsName = Utils.getClassNameFromObject(value);
        return (PropertyProcessor)clsname2Delegate.get(clsName);
    }       
}