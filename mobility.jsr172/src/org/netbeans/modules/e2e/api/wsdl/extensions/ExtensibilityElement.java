/*
 * ExtensibilityElement.java
 *
 * Created on September 22, 2006, 7:04 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.e2e.api.wsdl.extensions;

import javax.xml.namespace.QName;

/**
 *
 * @author Michal Skvor
 */
public interface ExtensibilityElement {
 
    public void setElementType( QName type );
    
    public QName getElementType();
}
