/*
 * ResourceBean.java
 *
 * Created on March 19, 2007, 9:42 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.websvc.rest.codegen.model;

import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.websvc.rest.codegen.model.EntityClassInfo;

/**
 *
 * @author PeterLiu
 */
public class EntityResourceBean {
    
    /**
     * 
     */
    public enum Type {
        CONTAINER, ITEM
    }
 
    private Type type;
    private EntityClassInfo info;
    private String name;
    private Collection<RelatedEntityResource> superResources;
    private Collection<RelatedEntityResource> subResources;
    private String uriTemplate;
    
    
    /** Creates a new instance of ResourceBean 
     * @param javaSource 
     * @param type 
     */
    public EntityResourceBean(Type type) {
        this.type = type;
        this.superResources = new ArrayList<RelatedEntityResource>();
        this.subResources = new ArrayList<RelatedEntityResource>();
    }
    
    /**
     * 
     * @return 
     */
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
  
    public void setUriTemplate(String uriTemplate) {
        this.uriTemplate = uriTemplate;
    }
    
    public String getUriTemplate() {
        return uriTemplate;
    }
    
    /**
     * 
     * @param resourceBean 
     */
    public void addSubResource(RelatedEntityResource relatedResource) {
        subResources.add(relatedResource);
        
    }
    
    public Collection<RelatedEntityResource> getSubResources() {
        return subResources;
    }
    
    public void addSuperResource(RelatedEntityResource relatedResource) {
        superResources.add(relatedResource);
        
    }
    
    public Collection<RelatedEntityResource> getSuperResources() {
        return superResources;
    }
    
   
    public void setEntityClassInfo(EntityClassInfo info) {
        this.info = info;
    }
    
    public EntityClassInfo getEntityClassInfo() {
        return info;
    }
    
    public boolean isItem() {
        return type == Type.ITEM;
    }
    
    public boolean isContainer() {
        return type == Type.CONTAINER;
    }
}
