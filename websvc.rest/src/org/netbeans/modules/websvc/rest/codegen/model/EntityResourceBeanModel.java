/*
 * ResourceBeanModel.java
 *
 * Created on March 28, 2007, 1:46 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.rest.codegen.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author PeterLiu
 */
public class EntityResourceBeanModel {
    private EntityResourceModelBuilder builder;
    private Map<String, EntityResourceBean> containerResourceBeanMap;
    private Map<String, EntityResourceBean> itemResourceBeanMap;
    private boolean valid;
    
    
    /** Creates a new instance of ResourceBeanModel */
    public EntityResourceBeanModel(EntityResourceModelBuilder builder) {
        this.builder = builder;
        containerResourceBeanMap = new HashMap<String, EntityResourceBean>();
        itemResourceBeanMap = new HashMap<String, EntityResourceBean>();
    }
    
    public void addContainerResourceBean(EntityResourceBean bean) {
        containerResourceBeanMap.put(bean.getEntityClassInfo().getType(), bean);
    }
    
    public void addItemResourceBean(EntityResourceBean bean) {
        itemResourceBeanMap.put(bean.getEntityClassInfo().getType(), bean);
    }
    
    public EntityResourceBean getContainerResourceBean(EntityClassInfo info) {
        return containerResourceBeanMap.get(info);
    }
   
    public EntityResourceBean getContainerResourceBean(String entityType) {
        return containerResourceBeanMap.get(entityType);
    }
    
    public EntityResourceBean getItemResourceBean(EntityClassInfo info) {
        return getItemResourceBean(info.getEntity().getClass2());
    }
    
    public EntityResourceBean getItemResourceBean(String entityType) {
        return itemResourceBeanMap.get(entityType);
    }
   
    public Collection<EntityResourceBean> getResourceBeans() {
        ArrayList<EntityResourceBean> resourceBeans = new ArrayList<EntityResourceBean>();
        
        resourceBeans.addAll(containerResourceBeanMap.values());
        resourceBeans.addAll(itemResourceBeanMap.values());
        
        return resourceBeans;
    }
    
    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean v) {
        valid = v;
    }
    
    public EntityResourceModelBuilder getBuilder() {
        return builder;
    }
}
