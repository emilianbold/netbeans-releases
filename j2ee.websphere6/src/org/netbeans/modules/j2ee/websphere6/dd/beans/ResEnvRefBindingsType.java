/*
 * ResEnvRefBindingsType.java
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
public class ResEnvRefBindingsType extends CommonRef implements DDXmiConstants {
    
    public ResEnvRefBindingsType(String hrefType) {
        this();
        this.hrefType=hrefType;
    }
    public ResEnvRefBindingsType() {
        this(Common.USE_DEFAULT_VALUES);
    }
    public ResEnvRefBindingsType(int options) {
        super(BINDING_RESOURCE_ENV_REF_ID,
                BINDING_RESOURCE_ENV_REF,
                RES_ENV_REF_BINDINGS_XMI_ID,
                RES_ENV_REF_BINDINGS_JNDI_NAME,
                BINDING_RESOURCE_ENV_REF_HREF);
        this.initialize(options);
    }
    public void initialize(int options) {
        
    }
    public void setDefaults() {
        setBindingReference("");
        setHref("ResourceEnvRef_");
        setXmiId("ResourceEnvReferenceBinding_");
        setJndiName("services/cache/instance_");
    }
    public String getType(){
        return BINDING_REFERENCE_TYPE_RESOURCE_ENV;
    }
}
