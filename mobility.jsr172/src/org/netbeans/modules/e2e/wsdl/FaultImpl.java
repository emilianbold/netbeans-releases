/*
 * FaultImpl.java
 *
 * Created on September 24, 2006, 5:32 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.e2e.wsdl;

import org.netbeans.modules.e2e.api.wsdl.Fault;
import org.netbeans.modules.e2e.api.wsdl.Message;

/**
 *
 * @author Michal Skvor
 */
public class FaultImpl implements Fault {
    
    private String name;
    private Message message;
    
    /** Creates a new instance of FaultImpl */
    public FaultImpl( String name, Message message ) {
        this.name = name;
        this.message = message;
    }

    public void setName( String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setMessage( Message message ) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }
    
}
