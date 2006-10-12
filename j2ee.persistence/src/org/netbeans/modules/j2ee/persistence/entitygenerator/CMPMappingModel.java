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

package org.netbeans.modules.j2ee.persistence.entitygenerator;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class provides the mapping for entity cmp beans to the database table.
 * This class is used by the application server plug-in to facilitate mapping.
 * @author Chris Webster
 */
public class CMPMappingModel {
    private Map cmpFieldMapping;
    private Map cmrFieldMapping;
    private String tableName;
    private Map cmrJoinMapping;
    private Map <String, JoinTableColumnMapping> joinTableColumnMappings;
    
    public CMPMappingModel() {
        cmpFieldMapping = new HashMap();
        cmrFieldMapping = new HashMap();
        cmrJoinMapping = new HashMap();
        joinTableColumnMappings = new HashMap();
    }
    
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public Map getCMPFieldMapping() {
        return cmpFieldMapping;
    }
    
    public void setCMPFieldMapping(Map m) {
        cmpFieldMapping = m;
    }
    
    public Map getCmrFieldMapping() {
        return cmrFieldMapping;
    }
    
    public void setCmrFieldMapping(Map m) {
        cmrFieldMapping = m;
    }
    
    public Map getJoinTableMapping() {
        return cmrJoinMapping;
    }
    
    public void setJoinTableMapping(Map m) {
        cmrJoinMapping = m;
    }
    
    public static class JoinTableColumnMapping {
        private String[] columns;
        private String[] referencedColumns;
        private String[] inverseColumns;
        private String[] referencedInverseColumns;

        public String[] getColumns() {
            return columns;
        }

        public void setColumns(String[] columns) {
            this.columns = columns;
        }

        public String[] getReferencedColumns() {
            return referencedColumns;
        }

        public void setReferencedColumns(String[] referencedColumns) {
            this.referencedColumns = referencedColumns;
        }

        public String[] getInverseColumns() {
            return inverseColumns;
        }

        public void setInverseColumns(String[] inverseColumns) {
            this.inverseColumns = inverseColumns;
        }

        public String[] getReferencedInverseColumns() {
            return referencedInverseColumns;
        }

        public void setReferencedInverseColumns(String[] referencedInverseColumns) {
            this.referencedInverseColumns = referencedInverseColumns;
        }
    }
    
    public Map<String, JoinTableColumnMapping> getJoinTableColumnMppings() {
        return joinTableColumnMappings;
    }
    
    public void setJoiTableColumnMppings(Map<String, JoinTableColumnMapping> joinTableColumnMppings) {
        this.joinTableColumnMappings = joinTableColumnMppings;
    }
    
    public int hashCode() {
        return tableName.hashCode();
    }
    
    public boolean equals(Object o) {
        if (! (o instanceof CMPMappingModel)) {
            return false;
        }
        
        CMPMappingModel other = (CMPMappingModel)o;
        
        if (cmrFieldMapping.size() != other.cmrFieldMapping.size()) {
            return false;
        }
        
        Iterator keyIt = cmrFieldMapping.keySet().iterator();
        while (keyIt.hasNext()) {
            String key = (String) keyIt.next();
            String[] value = (String[]) cmrFieldMapping.get(key);
            List l = Arrays.asList(value);
            Object otherValue = other.cmrFieldMapping.get(key);
            if (otherValue == null || 
                !l.equals(Arrays.asList((String[])other.cmrFieldMapping.get(key)))) {
                return false;
            }
        }
        
        return tableName.equals(other.tableName) &&
            cmrJoinMapping.equals(other.cmrJoinMapping) &&
            cmpFieldMapping.equals(other.cmpFieldMapping);
    }

}
