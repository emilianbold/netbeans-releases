/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

/**
 *
 * @author Lukas
 */
@WebService()
public class EchoArrays_DOCUMENT {
    @WebMethod(operationName = "getStringArray")
    public String getString(@WebParam(name = "parameter")
    String parameter) {
        return parameter;
    }


    @WebMethod(operationName = "getIntArray")
    public int[] getIntArray(@WebParam(name = "parameter")
    int[] parameter) {
        return parameter;
    }

    @WebMethod(operationName = "getLongArray")
    public long[] getLongArray(@WebParam(name = "parameter")
    long[] parameter) {
        return parameter;
    }

    @WebMethod(operationName = "getShortArray")
    public Short[] getShortArray(@WebParam(name = "parameter")
    Short[] parameter) {
        return parameter;
    }

    @WebMethod(operationName = "getBooleanArray")
    public boolean[] getBooleanArray(@WebParam(name = "parameter")
    boolean[] parameter) {
        return parameter;
    }
    
    @WebMethod(operationName = "getFloatArray")
    public float[] getFloatArray(@WebParam(name = "parameter")
    float[] parameter) {
        return parameter;
    }
    
    @WebMethod(operationName = "getDoubleArray")
    public Double[] getDoubleArray(@WebParam(name = "parameter")
    Double[] parameter) {
        return parameter;
    }
   /* 
    @WebMethod(operationName = "getQNameArray")
    public javax.xml.namespace.QName[] getQNameArray(@WebParam(name = "parameter")  javax.xml.namespace.QName[] parameter) {
        return parameter;
    }
     */     
    @WebMethod(operationName = "getByteArray")
    public byte getByteArray(@WebParam(name = "parameter")  byte parameter) {
        return parameter;
    }
            
    @WebMethod(operationName = "getByteWrapperArray")
    public Byte[] getByteWrapperArray(@WebParam(name = "parameter")  Byte[] parameter) {
        return parameter;
    }
}
