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

package org.netbeans.modules.xml.axi.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.netbeans.modules.xml.axi.AXIComponent;
import org.netbeans.modules.xml.axi.impl.SchemaUpdate.UpdateUnit.Type;

/**
 *
 * @author Ayub Khan
 */
public class SchemaUpdate {
    
    /** Creates a new instance of SchemaUpdate */
    public SchemaUpdate() {
    }
    
    public Collection<UpdateUnit> getUpdateUnits() {
        return Collections.unmodifiableList(units);
    }
    
    public void addUpdateUnit(UpdateUnit uu) {
        units.add(uu);
    }
    
    public UpdateUnit createUpdateUnit(Type type,
            AXIComponent source, Object oldValue, Object newValue, String propertyName) {
        AXIComponent key = null;
        if(type == UpdateUnit.Type.CHILD_MODIFIED)
            key = source;
        else if(type == UpdateUnit.Type.CHILD_ADDED)
            key = (AXIComponent) newValue;
        else if(type == UpdateUnit.Type.CHILD_DELETED)
            key = (AXIComponent) oldValue;
        
        if(key instanceof AXIComponentProxy) {
            key = key.getOriginal();
        }
        if(key != null) {
            List<AXIComponent> items = uniqueMap.get(key);
            if(items == null) {
                items = new ArrayList();
                uniqueMap.put(key, items);
            }
            items.add(key);
            return new UpdateUnit(String.valueOf(count++), type, source, oldValue, newValue,
                    propertyName);
        }
        return null;
    }
    
    public static class UpdateUnit {
        
        public static enum Type {CHILD_ADDED, CHILD_DELETED, CHILD_MODIFIED};
        
        private String id;
        
        private Type type;
        
        private AXIComponent source;
        
        private Object oldValue;
        
        private Object newValue;
        
        private String propertyName;
        
        public UpdateUnit(String id, Type type,
                AXIComponent source, Object oldValue, Object newValue,
                String propertyName) {
            this.id = id;
            this.type = type;
            this.source = source;
            this.oldValue = oldValue;
            this.newValue = newValue;
            this.propertyName = propertyName;
        }
        
        public String getId() {
            return id;
        }
        
        public AXIComponent getSource() {
            return source;
        }
        
        public Type getType() {
            return type;
        }
        
        public Object getOldValue() {
            return oldValue;
        }
        
        public Object getNewValue() {
            return newValue;
        }
        
        public String getPropertyName() {
            return propertyName;
        }
    }
    
    private List<UpdateUnit> units = new ArrayList<UpdateUnit>();
    
    private HashMap<AXIComponent, List<AXIComponent>> uniqueMap =
            new HashMap<AXIComponent, List<AXIComponent>>();
    
    private int count = 0;
}
