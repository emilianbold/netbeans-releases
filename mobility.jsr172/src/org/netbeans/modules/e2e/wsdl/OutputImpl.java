/*
 * OuputImpl.java
 *
 * Created on September 24, 2006, 5:38 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.e2e.wsdl;

import org.netbeans.modules.e2e.api.wsdl.Message;
import org.netbeans.modules.e2e.api.wsdl.Output;

/**
 *
 * @author Michal Skvor
 */
public class OutputImpl implements Output {
    
    private String name;
    private Message message;
    
    /** Creates a new instance of OuputImpl */
    public OutputImpl( String  name, Message message ) {
        this.name = name;
        this.message = message;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }
    
}
