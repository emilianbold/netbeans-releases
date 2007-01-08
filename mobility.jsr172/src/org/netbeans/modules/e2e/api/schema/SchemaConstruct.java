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
}
