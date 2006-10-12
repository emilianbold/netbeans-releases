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

package org.netbeans.modules.websvc.api.jaxws.wsdlmodel;

import com.sun.tools.ws.processor.model.java.JavaParameter;

/**
 *
 * @author mkuchtiak
 */
public class WsdlParameter {
    private JavaParameter parameter;
    /** Creates a new instance of WsdlParameter */
    public WsdlParameter(JavaParameter parameter) {
        this.parameter=parameter;
    }
    
    public Object /*com.sun.tools.ws.processor.model.Operation*/ getInternalJAXWSParameter() {
        return parameter;
    }
    
    public String getName() {
        return parameter.getName();
    }
    
    public String getTypeName() {
        String type = parameter.getType().getName();
        return isHolder()?"javax.xml.ws.Holder<"+wrapperType(type)+">":type;//NOI18N
    }
    
    public boolean isHolder() {
        return parameter.isHolder();
    }
    
    public String getHolderName() {
        return parameter.getHolderName();
    }
    
    private String wrapperType(String type) {
        if ("int".equals(type)) return "Integer"; //NOI18N
        else if ("float".equals(type)) return "Float"; //NOI18N
        else if ("double".equals(type)) return "Double"; //NOI18N
        else if ("byte".equals(type)) return "Byte"; //NOI18N
        else if ("long".equals(type)) return "Long"; //NOI18N
        else if ("boolean".equals(type)) return "Boolean"; //NOI18N
        else if ("char".equals(type)) return "Character"; //NOI18N
        else return type;
    }
}
