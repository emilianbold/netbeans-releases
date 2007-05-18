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
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Basic;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Column;
import org.netbeans.modules.j2ee.persistence.api.metadata.orm.Id;

/**
 *
 * @author tomslot
 */
public class AttributeWrapper {
    private Object modelElement;
    private Element javaElement;

    public AttributeWrapper(Object modelElement) {
        this.modelElement = modelElement;
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
        
        return null;
    }
    
    public Column getColumn(){
        if (modelElement instanceof Basic){
            return ((Basic)modelElement).getColumn();
        }
        
        if (modelElement instanceof Id){
            return ((Id)modelElement).getColumn();
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
        
        return null;
    }
    
    public Element getJavaElement(){
        return javaElement;
    }
    
    public void setJavaElement(Element javaElement){
        this.javaElement = javaElement;
    }
}
