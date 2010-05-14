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

package org.netbeans.modules.encoder.ui.basic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.xml.schema.model.Element;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.NameableSchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaComponent;

/**
 * Context information that is passed in to concrete validaiton methods.
 * 
 * @author Jun Xu
 */
public class ValidationContext {
    
    public static final String PROP_TOP_ELEMENT_NCNAME = "topelemncname"; //NOI18N
    public static final String PROP_TOP_ELEMENT_PREFIX = "topelemprefix"; //NOI18N
    
    private List<SchemaComponent> _path;
    private Set<NameableSchemaComponent> _nameables;
    private Map<String, Object> _properties = new HashMap<String, Object>();
    
    /**
     * Creates a new instance of ValidationContext.
     */
    private ValidationContext() {
    }
    
    /**
     * Creates a new instance of ValidationContext.
     * 
     * @param path the list if all parent schema component before
     *          reaches the current schema component.
     * @param nameables all parent schema components that are nameable
     */
    private ValidationContext(List<SchemaComponent> path,
            Set<NameableSchemaComponent> nameables) {
        _path = path;
        _nameables = nameables;
    }
    
    public static ValidationContext createContext(List<SchemaComponent> path,
            Set<NameableSchemaComponent> nameables) {
        return new ValidationContext(path, nameables);
    }
    
    public List<SchemaComponent> getPath() {
        return _path;
    }
    
    public Set<NameableSchemaComponent> getNameables() {
        return _nameables;
    }
    
    public String getTopElementNCName() {
        return (String) _properties.get(PROP_TOP_ELEMENT_NCNAME);
    }
    
    public String getTopElementPrefix() {
        return (String) _properties.get(PROP_TOP_ELEMENT_PREFIX);
    }
    
    public void setTopElementNCName(String ncName) {
        _properties.put(PROP_TOP_ELEMENT_NCNAME, ncName);
        _properties.put(PROP_TOP_ELEMENT_PREFIX, ncName + ".");
    }
    
    public Object getProperty(String key) {
        return _properties.get(key);
    }
    
    public void setProperty(String key, Object value) {
        _properties.put(key, value);
    }
    
    public void removeProperty(String key) {
        _properties.remove(key);
    }
    
    /**
     * Gets a path ('/' separated) of all NC names of all elements
     */
    public String getElementNCNamePath() {
        StringBuffer sb = new StringBuffer();
        for (SchemaComponent sc : _path) {
            if ((sc instanceof Element) && !(sc instanceof ElementReference)
                    && (sc instanceof NameableSchemaComponent)) {
                sb.append('/').append(((NameableSchemaComponent) sc).getName());
            }
        }
        return sb.toString();
    }
    
    /**
     * Gets a path ('/' separated) of all NC names of all elements this
     * context holds plus the one passed in.
     * 
     * @param sc the last schema component in the path
     */
    public String getElementNCNamePath(SchemaComponent sc) {
        StringBuffer sb = new StringBuffer();
        for (SchemaComponent pe : _path) {
            if ((pe instanceof Element) && !(pe instanceof ElementReference)
                    && (pe instanceof NameableSchemaComponent)) {
                sb.append('/').append(((NameableSchemaComponent) pe).getName());
            }
        }
        if ((sc instanceof Element) && !(sc instanceof ElementReference)
                && (sc instanceof NameableSchemaComponent)) {
            sb.append('/').append(((NameableSchemaComponent) sc).getName());
        }
        return sb.toString();
    }
    
    public ValidationContext clone() {
        ValidationContext obj = new ValidationContext();
        obj._path = new ArrayList<SchemaComponent>(_path);
        obj._nameables = new HashSet<NameableSchemaComponent>(_nameables);
        obj._properties = new HashMap<String, Object>(_properties);
        return obj;
    }
}
