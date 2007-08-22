/*
 * ModelBuilder.java
 *
 * Created on March 28, 2007, 1:24 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.rest.codegen.model;

import org.netbeans.modules.websvc.rest.codegen.model.EntityClassInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Entity;
import org.netbeans.modules.websvc.rest.codegen.model.EntityResourceBean.Type;
import org.netbeans.modules.websvc.rest.codegen.model.EntityClassInfo;
import org.netbeans.modules.websvc.rest.codegen.model.EntityClassInfo.FieldInfo;
import org.netbeans.modules.websvc.rest.wizard.Util;

/**
 *
 * @author PeterLiu
 */
public class EntityResourceModelBuilder {
    private Map<String, EntityClassInfo>  entityClassInfoMap;
    EntityResourceBeanModel model;
    
    /** Creates a new instance of ModelBuilder */
    public EntityResourceModelBuilder(Project project, Collection<Entity> entities) {
        entityClassInfoMap = new HashMap<String, EntityClassInfo>();
        for (Entity entity : entities) {
            EntityClassInfo info = new EntityClassInfo(entity, project, this);
            entityClassInfoMap.put(entity.getClass2(), info);
        }
    }
    
    public List<Entity> getEntities() {
        List<Entity> ret = new ArrayList<Entity>();
        for (EntityClassInfo info : entityClassInfoMap.values()) {
            ret.add(info.getEntity());
        }
        return ret;
    }
    
    public Set<EntityClassInfo> getEntityInfos() {
        return new HashSet<EntityClassInfo>(entityClassInfoMap.values());
    }
    
    public Set<String> getAllEntityNames() {
        return entityClassInfoMap.keySet();
    }
    
    public EntityClassInfo getEntityClassInfo(String type) {
        return entityClassInfoMap.get(type);
    }
    
    public EntityResourceBeanModel build(Collection<Entity> selected) {
        model = new EntityResourceBeanModel(this);
        try {
            for (Entity entity : selected) {
                EntityClassInfo info = entityClassInfoMap.get(entity.getClass2());
                getContainerResourceBean(info);
            }
            
            model.setValid(true);
            
        } catch (Exception ex) {
            Logger.getLogger(getClass().getName()).log(Level.ALL, "build", ex);
            model.setValid(false);
        }
        
        return model;
    }
    
    private EntityResourceBean getContainerResourceBean(EntityClassInfo info) {
        EntityResourceBean bean = model.getContainerResourceBean(info);
        
        if (bean == null) {
            bean = createContainerResourceBean(info);
        }
        
        return bean;
    }
    
    private EntityResourceBean createContainerResourceBean(EntityClassInfo info) { 
        EntityResourceBean containerBean = new EntityResourceBean(Type.CONTAINER);
        
        String pluralName = Util.pluralize(info.getName());
        containerBean.setName(pluralName);
        containerBean.setEntityClassInfo(info);
        containerBean.setUriTemplate("/" + Util.lowerFirstChar(pluralName) + "/"); //NOI18N
        
        model.addContainerResourceBean(containerBean);
       
        EntityResourceBean itemBean = getItemResourceBean(info);
        
        containerBean.addSubResource(new RelatedEntityResource(itemBean, info.getIdFieldInfo()));
        
        return containerBean;
    }
    
    
    private EntityResourceBean getItemResourceBean(EntityClassInfo info) {
        EntityResourceBean bean = model.getItemResourceBean(info);
        
        if (bean == null) {
            bean = createItemResourceBean(info);
        }
        
        return bean;
    }
    
    private EntityResourceBean createItemResourceBean(EntityClassInfo info) {
        EntityResourceBean itemBean = new EntityResourceBean(Type.ITEM);
        
        itemBean.setName(Util.singularize(info.getName()));
        itemBean.setEntityClassInfo(info);
        itemBean.setUriTemplate("{" + info.getIdFieldInfo().getName() + "}/");   //NOI18N
        
        model.addItemResourceBean(itemBean);
        
        computeRelationship(itemBean, info);
        
        return itemBean;
    }
    
    private void computeRelationship(EntityResourceBean bean, EntityClassInfo info) { 
        for (FieldInfo fieldInfo : info.getFieldInfos()) {
            if (fieldInfo.isRelationship()) {
                EntityResourceBean foreignBean = null;
                EntityResourceBean foreignItemBean = null;
                
                if (fieldInfo.isOneToMany() || fieldInfo.isManyToMany()) {
                    foreignBean = getContainerResourceBean(entityClassInfoMap.get(fieldInfo.getTypeArg()));
                    foreignItemBean = getItemResourceBean(entityClassInfoMap.get((fieldInfo.getTypeArg())));
                } else {
                    foreignBean = getItemResourceBean(entityClassInfoMap.get(fieldInfo.getType()));
                }
     
                RelatedEntityResource subResource = new RelatedEntityResource(foreignBean, fieldInfo);
                bean.addSubResource(subResource);
                
                RelatedEntityResource superResource = new RelatedEntityResource(bean, fieldInfo);
                foreignBean.addSuperResource(superResource);
                
                if (foreignItemBean != null) {
                    foreignItemBean.addSuperResource(superResource);
                }
            }
        }
    }
}
