/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.netbeans.modules.wsdlextensions.sap.model;

/**
 * A BAPI Method.
 * 
 * Example 1:
 *      object type: BUS6035,
 *      method: CHECK,
 *      method name: Check,
 *      function: BAPI_ACC_DOCUMENT_CHECK,
 *      short text: Accounting: Check Document
 *
 * Example 2:
 *      object type: BUS6035,
 *      method: CHECKREVERSAL,
 *      method name: CheckReversal,
 *      function: BAPI_ACC_DOCUMENT_REV_CHECK,
 *      short text:Accounting: Check Reversal
 *
 * @author jqian
 */
public class BAPIMethod {
    private String objectType;
    private String method;
    private String methodName;
    private String function;
    private String shortText;

    public BAPIMethod(String objectType, String method, String methodName,
            String function, String shortText) {
        this.objectType = objectType;
        this.method = method;
        this.methodName = methodName;
        this.function = function;
        this.shortText = shortText;
    }

    public String getObjectType() {
        return objectType;
    }

    public String getMethod() {
        return method;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getFunction() {
        return function;
    }

    public String getShortText() {
        return shortText;
    }
    
    @Override
    public String toString() {
        return getMethodName();
    }

}
