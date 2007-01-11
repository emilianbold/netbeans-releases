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
import java.util.Iterator;
import java.util.Map;


class ContentProcessor  {
    private static Map<String, ContentProcessor> clsname2Delegate = new HashMap<String, ContentProcessor>();
    protected String systemOptionInstanceName;
    
    static {
        registerContentProcessor(new JUnitContentProcessor("org.netbeans.modules.junit.JUnitSettings"));//NOI18N
    }
    
    private static void registerContentProcessor(ContentProcessor instance) {
        if (clsname2Delegate.put(instance.systemOptionInstanceName, instance) != null) {
            throw new IllegalArgumentException();
        }
    }
        
            
    protected ContentProcessor(String systemOptionInstanceName) {
        this.systemOptionInstanceName = systemOptionInstanceName;
    }
            
    protected Result parseContent(final Iterator<Object> it, boolean types) {
        Map<String, String> m;
        Result result = null;
        try {
            Map<String, Object> props = parseProperties(it);
            assert props != null;
            //debugInfo("before: ", m);                        
            m = processProperties(props, types);
            //assert debugInfo("after: ", m);
            result = new DefaultResult(systemOptionInstanceName, m);
        } catch (IllegalStateException isx) {
            System.out.println(systemOptionInstanceName+" not parsed: " + isx.getMessage());//NOI18N
        }
        return result;        
    }
    
    static Result parseContent(String systemOptionInstanceName, boolean types, final Iterator<Object> it) {
        ContentProcessor cp = clsname2Delegate.get(systemOptionInstanceName);
        if (cp == null) {
            cp = new ContentProcessor(systemOptionInstanceName);
        }
        return cp.parseContent(it, types);
    }
    
    private final Map<String, String> processProperties(final Map<String, Object> properties, boolean types) {
        Map<String, String> allProps = new HashMap<String, String>();
        for (Iterator<Map.Entry<String, Object>> it = properties.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, Object> entry = it.next();
            String name = entry.getKey();
            Object value = entry.getValue();
            allProps.putAll(PropertyProcessor.processProperty(name, value, types));
        }
        return allProps;
    }
    
    private final  Map<String, Object> parseProperties(final Iterator<Object> it) { // sequences String, Object, SerParser.ObjectWrapper
        Map<String, Object> properties = new HashMap<String, Object>();
        for (; it.hasNext();) {
            Object name = it.next();
            if ("null".equals(name) || name == null) {
                //finito
                return properties;
            } else if (!(name instanceof String)) {
                throw new IllegalStateException(name.getClass().getName());
            } else {
                if (!it.hasNext()) {
                    throw new IllegalStateException(name.toString());
                }
                Object value = it.next();
                properties.put((String)name, value);
                Object propertyRead = it.next();
                if (!(propertyRead instanceof SerParser.ObjectWrapper )) {
                    throw new IllegalStateException(propertyRead.getClass().getName());
                } else {
                    SerParser.ObjectWrapper ow = (SerParser.ObjectWrapper)propertyRead;
                    if (!ow.classdesc.name.endsWith("java.lang.Boolean;")) {//NOI18N
                        throw new IllegalStateException(ow.classdesc.name);
                    }
                }
            }
        }
        throw new IllegalStateException("Unexpected end");//NOI18N
    }        
}