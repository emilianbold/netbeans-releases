/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.registry.ui;

import com.sun.xml.rpc.processor.model.java.JavaType;


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
