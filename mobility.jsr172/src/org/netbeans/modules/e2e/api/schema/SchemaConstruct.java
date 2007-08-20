/*
 * SchemaConstruct.java
 *
 * Created on September 26, 2006, 10:59 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.e2e.api.schema;

import javax.xml.namespace.QName;

/**
 *
 * @author Michal Skvor
 */
public class SchemaConstruct {
        
    public enum ConstructType { ELEMENT, TYPE }; 
    
    private String targetNamespace;
    private QName name;
    private ConstructType constructType;
    private String javaName;
    
    private SchemaConstruct parent = null;
    
    public SchemaConstruct( SchemaConstruct.ConstructType constructType ) {
        this.constructType = constructType;
    }
    
    public SchemaConstruct( SchemaConstruct.ConstructType constructType, QName name ) {
        this.constructType = constructType;
        this.name = name;
    }
    
    public void setConstructType( SchemaConstruct.ConstructType constructType ) {
        this.constructType = constructType;
    }
    
    public SchemaConstruct.ConstructType getConstructType() {
        return constructType;
    }
    
    public void setTargetNamespace( String targetNamespace ) {
        this.targetNamespace = targetNamespace;
    }

    public String getTargetNamespace() {
        return targetNamespace;
    }

    public void setName( QName name ) {
        this.name = name;
    }

    public QName getName() {
        return name;
    }
    
    public void setJavaName( String name ) {
        javaName = name;
    }
    
    public String getJavaName() {
        return javaName;
    }
    
    public void setParent( SchemaConstruct parent ) {
        this.parent = parent;
    }
    
    public SchemaConstruct getParent() {
        return parent;
    }
}
