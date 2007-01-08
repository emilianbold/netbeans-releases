/*
 * SOAPAddress.java
 *
 * Created on September 22, 2006, 7:06 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.e2e.api.wsdl.extensions.soap;

import org.netbeans.modules.e2e.api.wsdl.extensions.ExtensibilityElement;

/**
 *
 * @author Michal Skvor
 */
public interface SOAPAddress extends ExtensibilityElement {
    
    public void setLocationURI( String locationURI );
    
    public String getLocationURI();
}
