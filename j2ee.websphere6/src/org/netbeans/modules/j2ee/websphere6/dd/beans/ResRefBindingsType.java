/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.j2ee.websphere6.dd.beans;

import org.w3c.dom.*;
import org.netbeans.modules.schema2beans.*;
import java.beans.*;
import java.util.*;
/**
 *
 * @author dlm198383
 */
public class ResRefBindingsType extends CommonRef implements DDXmiConstants{
    
    
    public ResRefBindingsType(String hrefType) {
        this();
        this.hrefType=hrefType;
    }
    public ResRefBindingsType() {
        this(Common.USE_DEFAULT_VALUES);
    }
    public ResRefBindingsType(int options) {
        super(BINDING_RESOURCE_REF_ID,
                BINDING_RESOURCE_REF,
                RES_REF_BINDINGS_XMI_ID,
                RES_REF_BINDINGS_JNDI_NAME,
                BINDING_RESOURCE_REF_HREF);
        
        this.initialize(options);
    }
    public void initialize(int options) {
        
    }
    
    public void setDefaults() {
        setBindingReference("");
        setHref("ResourceRef_");
        setXmiId("ResourceReferenceBinding_");
        setJndiName("services/cache/instance_");
    }
    
   public String getType(){
        return BINDING_REFERENCE_TYPE_RESOURCE;
    }
    
}
