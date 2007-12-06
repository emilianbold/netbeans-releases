/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package forwsclient;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

/**
 *
 * @author Lukas
 */
@WebService()
public class EchoWS_forWSClient {
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
          
//    @WebMethod(operationName = "getQName")
//    public javax.xml.namespace.QName getQName(@WebParam(name = "parameter")  javax.xml.namespace.QName parameter) {
//        return parameter;
//    }
    
    @WebMethod(operationName = "getByte")
    public byte getByte(@WebParam(name = "parameter")  byte parameter) {
        return parameter;
    }
            
    @WebMethod(operationName = "getByteWrapper")
    public Byte getByteWrapper(@WebParam(name = "parameter")  Byte parameter) {
        return parameter;
    }
}
