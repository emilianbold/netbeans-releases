/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
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
