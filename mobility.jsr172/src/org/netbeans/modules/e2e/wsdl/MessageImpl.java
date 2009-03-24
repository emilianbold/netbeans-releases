/*
 * MessageImpl.java
 *
 * Created on September 24, 2006, 5:34 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.e2e.wsdl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.netbeans.modules.e2e.api.wsdl.Message;
import org.netbeans.modules.e2e.api.wsdl.Part;

/**
 *
 * @author Michal Skvor
 */
public class MessageImpl implements Message {
    
    private QName myName;
    private Map<String, Part> parts;
    
    /** Creates a new instance of MessageImpl */
    public MessageImpl( QName name ) {
        parts = new HashMap<String, Part>();
        myName = name;
    }

    public String getName() {
        return getQName().getLocalPart();
    }
    
    public QName getQName(){
        return myName;
    }
    
    public void addPart( Part part ) {
        parts.put( part.getName(), part );
    }

    public Part getPart( String name ) {
        return parts.get( name );
    }

    public List<Part> getParts() {
        return Collections.unmodifiableList( new ArrayList( parts.values()));
    }    
    
    public static class MessageReferenceImpl extends MessageImpl 
        implements MessageReference 
    {

        public MessageReferenceImpl( QName name  ) {
            super(name);
            
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.e2e.api.wsdl.Message.MessageReference#isValid()
         */
        public boolean isValid() {
            return false;
        }
       
    }
}
