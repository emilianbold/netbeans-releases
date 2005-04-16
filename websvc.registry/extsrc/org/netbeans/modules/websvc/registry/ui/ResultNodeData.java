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
 * This class represents the data for each node in the Results TreeTable.
 * @author  David Botterill
 */
public class ResultNodeData implements NodeData {
    
    
    
    private Object resultValue;
    private JavaType resultType;
    
    public ResultNodeData() {
        
    }
    
    
    /** Creates a new instance of TypeNodeData */
    public ResultNodeData(JavaType inType, Object inValue) {
        resultType=inType;
        resultValue=inValue;
    }
    
    public void setResultType(JavaType inType) {
        resultType=inType;
    }
    
    public JavaType getResultType() {
        return resultType;
    }
    
    public void setResultValue(Object inValue) {
        resultValue=inValue;
    }
    
    public Object getResultValue() {
        return resultValue;
    }
    
    public JavaType getNodeType() {
        return getResultType();
    }    
    
    public Object getNodeValue() {
        return getResultValue();
    }    
    
}
