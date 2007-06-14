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
package org.netbeans.modules.j2ee.jpa.model;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Basic;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Column;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Id;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Version;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class AttributeWrapper {
    private Object modelElement;
    private Element javaElement;
    private ExecutableElement accesor;
    private VariableElement instanceVariable;
    private ExecutableElement mutator;
    
    public AttributeWrapper(Object modelElement) {
        this.modelElement = modelElement;
    }
    
    public ExecutableElement getMutator() {
        return mutator;
    }

    public void setMutator(ExecutableElement mutator) {
        this.mutator = mutator;
    }
    
    public Object getModelElement(){
        return modelElement;
    }
    
    public String getName(){
        if (modelElement instanceof Basic){
            return ((Basic)modelElement).getName();
        }
        
        if (modelElement instanceof Id){
            return ((Id)modelElement).getName();
        }
        
        if (modelElement instanceof Version){
            return ((Version)modelElement).getName();
        }
        
        return null;
    }
    
    public Column getColumn(){
        if (modelElement instanceof Basic){
            return ((Basic)modelElement).getColumn();
        }
        
        if (modelElement instanceof Id){
            return ((Id)modelElement).getColumn();
        }
        
        if (modelElement instanceof Version){
            return ((Version)modelElement).getColumn();
        }
        
        return null;
    }
    
    public String getTemporal(){
        if (modelElement instanceof Basic){
            return ((Basic)modelElement).getTemporal();
        }
        
        if (modelElement instanceof Id){
            return ((Id)modelElement).getTemporal();
        }
        
        if (modelElement instanceof Version){
            return ((Version)modelElement).getTemporal();
        }
        
        return null;
    }
    
    public Element getJavaElement(){
        return javaElement;
    }
    
    public void setJavaElement(Element javaElement){
        this.javaElement = javaElement;
    }
    
    public ExecutableElement getAccesor(){
        return accesor;
    }
    
    public void setAccesor(ExecutableElement accesor){
        this.accesor = accesor;
    }
    
    public VariableElement getInstanceVariable(){
        return instanceVariable;
    }
    
    public void setInstanceVariable(VariableElement instanceVariable){
        this.instanceVariable = instanceVariable;
    }
    
    public boolean isFullyResolved(){
        return javaElement != null && instanceVariable != null && accesor != null;
    }
    
    public TypeMirror getType(){
        if (instanceVariable != null){
            return instanceVariable.asType();
        } else if (accesor != null){
            return accesor.getReturnType();
        }
        
        assert false : "getType() must not be called before AttrWrapper is resolved";
        
        return null;
    }
}
