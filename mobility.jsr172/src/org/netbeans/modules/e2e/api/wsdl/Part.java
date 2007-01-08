/*
 * Part.java
 *
 * Created on September 22, 2006, 1:30 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.e2e.api.wsdl;

import javax.xml.namespace.QName;

/**
 *
 * @author Michal Skvor
 */
public interface Part {
    
    public void setName( String name );
    
    public String getName();
    
    public void setTypeName( QName typeName );
    
    public QName getTypeName();
    
    public void setElementName( QName elementName );
    
    public QName getElementName();
}
