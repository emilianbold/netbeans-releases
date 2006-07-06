/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
                BINDING_RESOURCE_ENV_REF_HREF,
                RES_ENV_REF_BINDINGS_XMI_TYPE);
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
