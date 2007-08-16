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


package org.netbeans.modules.websvc.manager.ui;

import com.sun.tools.ws.processor.model.java.JavaType;

/**
 * This class represents the data for each node in the TreeTable.
 *
 * @author  David Botterill
 */
public class TypeNodeData implements NodeData {
    
    private String parameterName;
    private Object parameterValue;
    private JavaType parameterType;
    
    public TypeNodeData() {
        
    }
    
    
    /** Creates a new instance of TypeNodeData */
    public TypeNodeData(JavaType inType, String inParameterName, Object inValue) {
        parameterType=inType;
        parameterName = inParameterName;
        parameterValue=inValue;
    }
    
    public void setParameterType(JavaType inType) {
        parameterType=inType;
    }
    
    public JavaType getParameterType() {
        return parameterType;
    }
    
    public void setParameterName(String inParameterName) {
        parameterName=inParameterName;
    }
    
    public String getParameterName() {
        return parameterName;
    }
    public void setParameterValue(Object inValue) {
        parameterValue=inValue;
    }
    
    public Object getParameterValue() {
        return parameterValue;
    }
    
    public JavaType getNodeType() {
        return getParameterType();
    }    
    
    public Object getNodeValue() {
        return getParameterValue();
    }    
    
}
