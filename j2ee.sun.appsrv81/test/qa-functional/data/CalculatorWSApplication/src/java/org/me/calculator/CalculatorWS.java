/*
 * CalculatorWS.java
 *
 * Created on May 30, 2006, 3:19 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.me.calculator;

import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;

/**
 *
 * @author ak199487
 */

@Stateless()
@WebService()
public class CalculatorWS {
    /**
     * Web service operation
     */
    @WebMethod
    public int add(@WebParam(name = "i") int i, @WebParam(name = "j") int j) {
        // TODO implement operation 
        int k = i + j;
        return k;
    }
    
   
}
