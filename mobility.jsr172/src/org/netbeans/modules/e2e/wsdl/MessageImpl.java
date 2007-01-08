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
import org.netbeans.modules.e2e.api.wsdl.Message;
import org.netbeans.modules.e2e.api.wsdl.Part;

/**
 *
 * @author Michal Skvor
 */
public class MessageImpl implements Message {
    
    private String name;
    private Map<String, Part> parts;
    
    /** Creates a new instance of MessageImpl */
    public MessageImpl( String name ) {
        parts = new HashMap();
        this.name = name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public String getName() {
        return name;
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
}
