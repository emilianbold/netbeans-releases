/*
 * EjbRefBindingsType.java
 *
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
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
public class EjbRefBindingsType extends CommonRef implements DDXmiConstants {
    
    public EjbRefBindingsType(String hrefType) {
        this();
        this.hrefType=hrefType;
    }
    public EjbRefBindingsType() {
        this(Common.USE_DEFAULT_VALUES);
    }
    public EjbRefBindingsType(int options) {
        //super(comparators, runtimeVersion);
        super(BINDING_EJB_REF_ID,
                BINDING_EJB_REF,
                EJB_REF_BINDINGS_XMI_ID,
                EJB_REF_BINDINGS_JNDI_NAME,
                BINDING_EJB_REF_HREF);
        this.initialize(options);
    }
    public void initialize(int options) {
        
    }
    public void setDefaults() {
        setBindingReference("");
        setHref("EjbRef_");
        setXmiId("EjbReferenceBinding_");
        setJndiName("services/cache/instance_");
    }
    public String getType(){
        return BINDING_REFERENCE_TYPE_EJB;
    }
}
