/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.registry.model;

import java.beans.DefaultPersistenceDelegate;
import java.beans.Encoder;

/**
 *
 * @author  david
 */
public class WebServiceDataPersistenceDelegate extends DefaultPersistenceDelegate {
    
    /** Creates a new instance of WebServiceDataPersistenceDelegate */
    public WebServiceDataPersistenceDelegate() {
    }
    
    /**
     * Overriding PersistenceDelegate to clean up any unwanted classes trying to be written.
     */
    public void writeObject(Object oldInstance, Encoder out) {
        if(oldInstance.getClass().getName().startsWith("javax.xml.namespace.QName") ) {
            return;
        }
        else if(oldInstance.getClass().getName().startsWith("com.sun.xml.rpc.wsdl.document.soap.SOAPStyle")){
            return;
        }
        else if(oldInstance.getClass().getName().startsWith("com.sun.xml.rpc.wsdl.document.soap.SOAPUse")){
            return;
        }else {
            super.writeObject(oldInstance,out);
        }
        
    }
    
    
}
