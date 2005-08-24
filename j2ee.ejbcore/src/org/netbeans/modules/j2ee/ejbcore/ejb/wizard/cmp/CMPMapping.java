/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ejbcore.ejb.wizard.cmp;

import org.netbeans.modules.j2ee.deployment.common.api.OriginalCMPMapping;
import org.openide.filesystems.FileObject;

/**
 * This class provides the mapping for entity cmp beans to the database table.
 * This class is used by the application server plug-in to facilitate mapping.
 * @author Chris Webster
 */
public class CMPMapping implements OriginalCMPMapping {
    private String ejbName;
    private CMPMappingModel model;
    private FileObject schema;
    
    CMPMapping(String ejbName, CMPMappingModel m, FileObject schema) {
        this.ejbName = ejbName;
        model = m;
        this.schema = schema;
    }

    public String getEjbName() {
        return ejbName;
    }
    
    public String getFieldColumn(String cmpFieldName) {
        return (String) model.getCMPFieldMapping().get(cmpFieldName);
    }
    
    public String[] getRelationshipColumn(String cmrFieldName) {
        return (String[]) model.getCmrFieldMapping().get(cmrFieldName);
    }
    
    public FileObject getSchema() {
        return schema;
    }
    
    public void setTableName(String tableName) {
        model.setTableName(tableName);
    }
    
    public String getTableName() {
        return model.getTableName();
    }
    
    public String getRelationshipJoinTable(String cmrFieldName) {
        return (String) model.getJoinTableMapping().get(cmrFieldName);
    }
    
    public CMPMappingModel getMappingModel() {
        return model;
    }
}
