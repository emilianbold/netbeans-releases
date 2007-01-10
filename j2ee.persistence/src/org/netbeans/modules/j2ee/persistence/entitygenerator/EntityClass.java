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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.openide.filesystems.FileObject;

/**
 * Abstract description of an entity class
 * (either CMP or Java Persistence API).
 *
 * @author Chris Webster, Martin Adamek, Andrei Badea
 */
public class EntityClass {
    
    private final String tableName;
    private final FileObject rootFolder;
    private final String className;
    private final String packageName;
    
    private List roles;
    private List fields;
    private boolean usePkField;
    private String pkFieldName;
    private CMPMappingModel mappingModel;
    
    public EntityClass(String tableName, FileObject rootFolder, String packageName, String className) {
        this.tableName = tableName;
        this.rootFolder = rootFolder;
        this.packageName = packageName;
        this.className = className;
        
        roles = Collections.EMPTY_LIST;
        fields = new ArrayList();
        mappingModel = new CMPMappingModel();
    }
    
    public void addRole(RelationshipRole role) {
        if (roles == Collections.EMPTY_LIST) {
            roles = new ArrayList();
        }
        roles.add(role);
    }
    
    public List getRoles() {
        return roles;
    }
    
    public List getFields() {
        return fields;
    }
    
    public void setFields(List fields) {
        this.fields = fields;
    }
    
    public String toString() {
        String cmpFields = ""; // NOI18N
        for (Iterator it = getFields().iterator(); it.hasNext();) {
            EntityMember m = (EntityMember) it.next();
            cmpFields += " " + m.getMemberName() + (m.isPrimaryKey()?" (PK) ":" "); // NOI18N
        }
        return "bean name " + getClassName() + // NOI18N
                "\ncmp-fields "+ cmpFields;  // NOI18N
    }
    
    public FileObject getRootFolder() {
        return rootFolder;
    }
    
    public String getPackage() {
        return packageName;
    }
    
    public String getTableName() {
        return tableName;
    }
    
    public String getClassName() {
        return className;
    }
    
    public FileObject getPackageFileObject() {
        String relative = packageName.replace('.', '/');
        return rootFolder.getFileObject(relative);
    }
    
    public CMPMappingModel getCMPMapping() {
        mappingModel.getCMPFieldMapping().clear();
        Iterator fields = getFields().iterator();
        while (fields.hasNext()) {
            EntityMember member = (EntityMember) fields.next();
            mappingModel.setTableName(member.getTableName());
            mappingModel.getCMPFieldMapping().put(
                    member.getMemberName(), member.getColumnName());
        }
        return mappingModel;
    }
    
    public void usePkField(boolean usePkField) {
        this.usePkField = usePkField;
    }
    
    public boolean isUsePkField() {
        return usePkField;
    }
    
    public String getPkFieldName() {
        return pkFieldName;
    }
    
    public void setPkFieldName(String pkFieldName) {
        this.pkFieldName = pkFieldName;
    }
}
