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
package org.netbeans.modules.bpel.core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModelFactory;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;

public final class ValidationUtil {
    public static final String SCHEMA_COMPONENT_ATTRIBUTE_BASE = "base"; // NOI18N

    public static Collection<GlobalSimpleType> BUILT_IN_SIMPLE_TYPES = 
        SchemaModelFactory.getDefault().getPrimitiveTypesModel().getSchema().getSimpleTypes();
    
    
    private ValidationUtil() {}
    
    public static List<ResultItem> filterBpelResultItems(List<ResultItem> validationResults) {
        List<ResultItem> bpelResultItems = new ArrayList<ResultItem>();
        
        for(ResultItem resultItem: validationResults) {
            Component component = resultItem.getComponents();

            if(component instanceof BpelEntity) {
                ResultItem bpelResultItem = 
                    new ResultItem(resultItem.getValidator(),
                        resultItem.getType(), component, 
                        resultItem.getDescription());
                bpelResultItems.add(bpelResultItem);
            }
        }
        return bpelResultItems;
    }
    
    public static boolean equals(ResultItem item1, ResultItem item2){
        if (item1 == item2){
            return true;
        }
        if(!item1.getDescription().equals(item2.getDescription())) {
            return false;
        }
        
        if(!item1.getType().equals(item2.getType())) {
            return false;
        }
        
        Component components1 = item1.getComponents();
        Component components2 = item2.getComponents();
        
        if(components1 != components2) {
            return false;
        }
        return true;
    }

    private static boolean contains(List<ResultItem> list, ResultItem resultItem) {
        assert list!=null;
        for (ResultItem item: list) {
            if (equals(item, resultItem)){
                return true;
            }
        }
        return false;
    }
    
    public static GlobalSimpleType getBuiltInSimpleType(SchemaComponent schemaComponent) {
        if (schemaComponent == null) return null;
        Collection<GlobalSimpleType> 
            schemaSimpleTypes = schemaComponent.getModel().getSchema().getSimpleTypes();
        
        String baseTypeName = schemaComponent.getAnyAttribute(new QName(
            SCHEMA_COMPONENT_ATTRIBUTE_BASE));
        if (baseTypeName != null) {
            baseTypeName = ignoreNamespace(baseTypeName);
            GlobalSimpleType globalSimpleType = findGlobalSimpleType(baseTypeName, 
                BUILT_IN_SIMPLE_TYPES);
            if (globalSimpleType != null) return globalSimpleType;
            globalSimpleType = findGlobalSimpleType(baseTypeName, schemaSimpleTypes);
            if (globalSimpleType != null) {
                for (SchemaComponent childComponent : schemaComponent.getChildren()) {
                    globalSimpleType = getBuiltInSimpleType(childComponent);
                    if (globalSimpleType != null) return globalSimpleType;
                }
                return null;
            } else {
                return null;
            }
        }
        return null;
    }
    
    public static GlobalSimpleType findGlobalSimpleType(String typeName,
        Collection<GlobalSimpleType> globalSimpleTypes) {
        for (GlobalSimpleType globalSimpleType : globalSimpleTypes) {
            if (globalSimpleType.toString().equals(typeName)) {
                return globalSimpleType;
            }
        }
        return null;
    }

    public static String ignoreNamespace(String dataWithNamespace) {
        int index = dataWithNamespace.indexOf(":");
        if ((index > -1) && (index < dataWithNamespace.length() - 1)) {
            return dataWithNamespace.substring(index + 1);
        }
        return dataWithNamespace;
    }
}