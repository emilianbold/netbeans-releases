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

package org.netbeans.modules.wsdlextensions.snmp;


public interface SNMPOperation extends SNMPComponent {

    public static final String IN_ONLY = "http://www.w3.org/2004/08/wsdl/in-only";
    public static final String IN_OUT = "http://www.w3.org/2004/08/wsdl/in-out";
    public static final String IN_OPTIONAL_OUT = "http://www.w3.org/2004/08/wsdl/in-opt-out";
    public static final String ROBUST_IN_ONLY = "http://www.w3.org/2004/08/wsdl/robust-in-only";
    public static final String OUT_ONLY = "http://www.w3.org/2004/08/wsdl/out-only";
    public static final String OUT_IN = "http://www.w3.org/2004/08/wsdl/out-in";
    public static final String OUT_OPTIONAL_IN = "http://www.w3.org/2004/08/wsdl/out-opt-in";
    public static final String ROBUST_OUT_ONLY = "http://www.w3.org/2004/08/wsdl/robust-out-only";    

    // common
    public static final String ATTR_TYPE = "type";
    public static final String ATTR_MOF_ID = "mofId";
    public static final String ATTR_ADAPTATION_ID = "adaptationId";
    public static final String ATTR_MOF_ID_REF = "mofIdRef";

    public String getType();
    public void setType(String val);
    
    public String getMofId();
    public void setMofId(String val);

    public String getAdaptationId();
    public void setAdaptationId(String val);
    
    public String getMofIdRef();
    public void setMofIdRef(String val);
    
}
