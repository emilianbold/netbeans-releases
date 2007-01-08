/*
 * WSDL2JavaFactory.java
 *
 * Created on October 30, 2006, 10:43 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.e2e.api.wsdl.wsdl2java;

import org.netbeans.modules.e2e.wsdl.wsdl2java.WSDL2JavaImpl;

/**
 *
 * @author Michal Skvor
 */
public class WSDL2JavaFactory {

    public static WSDL2Java getWSDL2Java( WSDL2Java.Configuration configuration ) {
        return new WSDL2JavaImpl( configuration );
    }   
}
