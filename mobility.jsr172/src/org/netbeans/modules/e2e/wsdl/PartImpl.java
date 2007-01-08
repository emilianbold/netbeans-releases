/*
 * PartImpl.java
 *
 * Created on September 24, 2006, 5:38 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.e2e.wsdl;

import javax.xml.namespace.QName;
import org.netbeans.modules.e2e.api.wsdl.Part;

/**
 *
 * @author Michal Skvor
 */
public class PartImpl implements Part {
    
    private String name;
    private QName typeName;
    private QName elementName;
    
    /** Creates a new instance of PartImpl */
    public PartImpl( String name, QName typeName, QName elementName ) {
        this.name = name;
        this.typeName = typeName;
        this.elementName = elementName;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setTypeName( QName typeName ) {
        this.typeName = typeName;
    }

    public QName getTypeName() {
        return typeName;
    }

    public void setElementName( QName elementName ) {
        this.elementName = elementName;
    }

    public QName getElementName() {
        return elementName;
    }
    
}
