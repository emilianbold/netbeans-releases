/*
 * RelatedResource.java
 *
 * Created on March 28, 2007, 1:19 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.rest.codegen.model;

import org.netbeans.modules.websvc.rest.codegen.model.EntityClassInfo.FieldInfo;

/**
 *
 * @author PeterLiu
 */
public class RelatedEntityResource {
    
    private EntityResourceBean bean;
    
    private FieldInfo fieldInfo;
    
    /** Creates a new instance of RelatedResource */
    public RelatedEntityResource(EntityResourceBean bean, FieldInfo fieldInfo) {
        this.bean = bean;
        this.fieldInfo = fieldInfo;
    }
    
    public EntityResourceBean getResourceBean() {
        return bean;
    }
    
    public FieldInfo getFieldInfo() {
        return fieldInfo;
    }
}
