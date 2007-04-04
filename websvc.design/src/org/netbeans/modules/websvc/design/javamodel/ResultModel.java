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
public class ResultModel {
    
    private String name;
    private String partName;
    private String targetNamespace;
    private String resultType;
    
    /** Creates a new instance of MethodModel */
    ResultModel() {
    }
    
    /** Creates a new instance of MethodModel */
    ResultModel(String resultType) {
        this.resultType=resultType;
    }
    
    public String getName() {
        return name;
    }
    
    void setName(String name) {
        this.name=name;
    }
    
    public String getPartame() {
        return partName;
    }

    void setPartName(String partName) {
        this.partName = partName;
    }

    public String getTargetNamespace() {
        return targetNamespace;
    }
    
    void setTargetNamespace(String targetNamespace) {
        this.targetNamespace = targetNamespace;
    }
    
    public String getResultType() {
        return resultType;
    }

    void setResultType(String faultType) {
        this.resultType = faultType;
    }
        
    public boolean isEqualTo(ResultModel model) {
        if (!Utils.isEqualTo(resultType,model.resultType)) return false;
        if (!Utils.isEqualTo(name,model.name)) return false;
        if (!Utils.isEqualTo(partName,model.partName)) return false;
        if (!Utils.isEqualTo(targetNamespace, model.targetNamespace)) return false;
        return true;
    }
}
