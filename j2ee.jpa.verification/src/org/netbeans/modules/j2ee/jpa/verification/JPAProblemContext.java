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

package org.netbeans.modules.j2ee.jpa.verification;

import org.netbeans.modules.j2ee.jpa.model.AccessType;
import org.netbeans.modules.j2ee.jpa.verification.common.ProblemContext;

/**
 * @see ProblemContext
 *
 * @author Tomasz.Slota@Sun.COM
 */
public class JPAProblemContext extends ProblemContext {
    private boolean entity;
    private boolean embeddable;
    private boolean idClass;
    private boolean mappedSuperClass;
    private AccessType accessType;
    
    public boolean isEntity(){
        return entity;
    }
    
    public void setEntity(boolean entity){
        this.entity = entity;
    }
    
    public boolean isEmbeddable(){
        return embeddable;
    }
    
    public void setEmbeddable(boolean embeddable){
        this.embeddable = embeddable;
    }
    
    public boolean isIdClass(){
        return idClass;
    }
    
    public void setIdClass(boolean idClass){
        this.idClass = idClass;
    }
    
    public boolean isMappedSuperClass(){
        return mappedSuperClass;
    }
    
    public void setMappedSuperClass(boolean mappedSuperClass){
        this.mappedSuperClass = mappedSuperClass;
    }
    
    public AccessType getAccessType(){
        return accessType;
    }
    
    public void setAccessType(AccessType accessType){
        this.accessType = accessType;
    }
    
    public boolean isJPAClass(){
        return entity || embeddable || idClass || mappedSuperClass;
    }
}
