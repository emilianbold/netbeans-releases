/*
 * Copyright (c) 2004 Sun Microsystems, Inc.  All rights reserved. 
 * 
 * Use is subject to the terms of the Sun Industry Standards Source License, 
 * a copy of which must accompany the software distribution.  License terms are
 * available at http://www.opensource.org/licenses/sisslpl.php .
 *
 */


package org.netbeans.lib.collab.xmpp;

import java.util.Iterator;
import java.util.Set;

import org.jabberstudio.jso.Extension;
import org.jabberstudio.jso.NSI;
import org.jabberstudio.jso.io.StreamBuilder;
import org.jabberstudio.jso.StreamElement;
import org.jabberstudio.jso.StreamObject;
import org.jabberstudio.jso.util.Utilities;
import org.jabberstudio.jso.x.core.AuthQuery;
import org.jabberstudio.jso.StreamDataFactory;

import net.outer_planes.jso.ExtensionBuilder;
import net.outer_planes.jso.ExtensionNode;

/**
 *
 */
public class PrivateQueryNode extends ExtensionNode implements PrivateQuery {
    StreamDataFactory _sdf;

    //Constructors
    public PrivateQueryNode(StreamDataFactory sdf) {
        this(sdf, new NSI("query", NAMESPACE));
	_sdf = sdf;
    }
    public PrivateQueryNode(StreamDataFactory sdf, NSI name) {
        super(sdf, name);
	_sdf = sdf;
    }
    protected PrivateQueryNode(StreamElement parent, PrivateQueryNode aqn) {
        super(parent, aqn);
    }
    
    //Methods
    public StreamBuilder createBuilder() {
        return new ExtensionBuilder(this);
    }
        
    public Set getFieldNames() {
        Set         fields = new java.util.TreeSet();
        Iterator    itr = listElements(new NSI(null, getNamespaceURI())).iterator();
        
        while (itr.hasNext()) {
            StreamElement   field = (StreamElement)itr.next();
            String          ln = field.getLocalName();
            
            fields.add(field.getLocalName());
        }
        
        return fields;
    }
    public String getField(String name) throws IllegalArgumentException {
        Iterator        itr;
        String          value = null;
        
        //Validate name
        if (!Utilities.isValidString(name))
            throw new IllegalArgumentException("Name cannot be null or \"\"");
        
        //Retrieve and "normalize"
        itr = listElements(name).iterator();
        if (itr.hasNext())
            value = ((StreamElement)itr.next()).normalizeTrimText();
        
        return value;
    }


     public void setField(String name, String value) throws IllegalArgumentException {
         //Removed existing field(s) -- Validates name as a side-effect
         unsetField(name);
        
         //Add the field
	 addElement(name).addText(value);
     }


    public void unsetField(String name) throws IllegalArgumentException {
        Iterator        itr;

        //Validate name
        if (!Utilities.isValidString(name))
            throw new IllegalArgumentException("Name cannot be null or \"\"");
        
        //Removed existing field(s)
        itr = listElements(name).iterator();
        while (itr.hasNext())
            remove((StreamElement)itr.next());
    }
    
    public StreamObject copy(StreamElement parent) {
        return new PrivateQueryNode(parent, this);
    }
}
