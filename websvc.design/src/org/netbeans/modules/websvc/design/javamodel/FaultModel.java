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

package org.netbeans.modules.websvc.design.javamodel;

/**
 *
 * @author mkuchtiak
 */
public class FaultModel {
    
    private String name;
    private String targetNamespace;
    private String faultType;
    
    /** Creates a new instance of MethodModel */
    FaultModel() {
    }
    
    /** Creates a new instance of MethodModel */
    FaultModel(String faultType) {
        this.faultType=faultType;
    }
    
    public String getName() {
        return name;
    }
    
    void setName(String name) {
        this.name=name;
    }
    
    public String getTargetNamespace() {
        return targetNamespace;
    }
    
    void setTargetNamespace(String targetNamespace) {
        this.targetNamespace = targetNamespace;
    }
    
    public String getFaultType() {
        return faultType;
    }

    void setFaultType(String faultType) {
        this.faultType = faultType;
    }
        
    public boolean isEqualTo(FaultModel model) {
        if (!Utils.isEqualTo(name,model.name)) return false;
        if (!faultType.equals(model.faultType)) return false;
        if (!Utils.isEqualTo(targetNamespace, model.targetNamespace)) return false;
        return true;
    }
}
