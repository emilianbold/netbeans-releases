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

package ws;

import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

/**
 *
 * @author Lukas Hasik
 */
@WebService(serviceName = "EchoNoArrays_DOCUMENT")
public class EchoNoArrays_DOCUMENT {

/**
     * Web service operation
     */
    @WebMethod(operationName = "getString")
    public String getString(@WebParam(name = "parameter")
    String parameter) {
        return parameter;
    }


    @WebMethod(operationName = "getInt")
    public int getInt(@WebParam(name = "parameter")
    int parameter) {
        return parameter;
    }

//    @WebMethod(operationName = "getIntegerWrapper")
//    public Integer getInteger(@WebParam(name = "parameter")
//    Integer parameter) {
//        return parameter;
//    }

    @WebMethod(operationName = "getLong")
    public long getLong(@WebParam(name = "parameter")
    long parameter) {
        return parameter;
    }

    @WebMethod(operationName = "getLongWrapper")
    public Long getLongWrapper(@WebParam(name = "parameter")
    Long parameter) {
        return parameter;
    }

    @WebMethod(operationName = "getShortWrapper")
    public Short getShortWrapper(@WebParam(name = "parameter")
    Short parameter) {
        return parameter;
    }

    @WebMethod(operationName = "getShort")
    public short getShort(@WebParam(name = "parameter")
    short parameter) {
        return parameter;
    }

    @WebMethod(operationName = "getBoolean")
    public boolean getBoolean(@WebParam(name = "parameter")
    boolean parameter) {
        return parameter;
    }

    @WebMethod(operationName = "getBooleanWrapper")
    public Boolean getBooleanWrapper(@WebParam(name = "parameter")
    Boolean parameter) {
        return parameter;
    }
    
    @WebMethod(operationName = "getFloat")
    public float getFloat(@WebParam(name = "parameter")
    float parameter) {
        return parameter;
    }

    @WebMethod(operationName = "getFloatWrapper")
    public Float getFloatWrapper(@WebParam(name = "parameter")
    Float parameter) {
        return parameter;
    }
    
    @WebMethod(operationName = "getDouble")
    public double getDouble(@WebParam(name = "parameter")
    double parameter) {
        return parameter;
    }

    @WebMethod(operationName = "getDoubleWrapper")
    public Double getDoubleWrapper(@WebParam(name = "parameter")
    Double parameter) {
        return parameter;
    }
          
    @WebMethod(operationName = "getQName")
    public javax.xml.namespace.QName getQName(@WebParam(name = "parameter")  javax.xml.namespace.QName parameter) {
        return parameter;
    }
    
    @WebMethod(operationName = "getByte")
    public byte getByte(@WebParam(name = "parameter")  byte parameter) {
        return parameter;
    }
            
    @WebMethod(operationName = "getByteWrapper")
    public Byte getByteWrapper(@WebParam(name = "parameter")  Byte parameter) {
        return parameter;
    }
    
}