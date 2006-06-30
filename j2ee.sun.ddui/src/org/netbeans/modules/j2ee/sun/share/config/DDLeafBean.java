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


package org.netbeans.modules.j2ee.sun.share.config;

import org.netbeans.modules.j2ee.deployment.plugins.api.*;
import org.netbeans.modules.schema2beans.*;

import java.util.Collection;
import java.util.LinkedList;

public class DDLeafBean extends DDCommon {

    BaseProperty prop;
    int index;
    boolean indexed;

    DDLeafBean(BaseProperty prop, int index, ModuleDDSupport support) {
        this(prop.getParent(),support,prop.getDtdName());
        this.prop = prop;
        indexed = index != -1;
        this.index = index;
    }

    DDLeafBean(BaseProperty prop, ModuleDDSupport support) {
        this(prop.getParent(),support,prop.getDtdName());
        this.prop = prop;
        indexed = false;
        index = -1;
    }
    
    private DDLeafBean(BaseBean bean, ModuleDDSupport support, String dtdName) {
        super(support.getBean(bean).proxy,bean, support, dtdName);
    }
    
    Collection search(String xpath, boolean addCurrent) {
        Collection ret = new LinkedList();
        if(addCurrent && (xpath.equals("") || xpath.equals("."))) // NOI18N
            ret.add(container);
        return ret;
    }
    
    public String getText() {
        Object ret;
        //        System.out.println("Getting text for " + prop.getName() + " with index " + index);
        if (indexed)
            ret = prop.getParent().getValue(prop.getName(),index);
        else
            ret = prop.getParent().getValue(prop.getName());
        if (ret == null)
            return null;
        return ret.toString();
    }
    
    public boolean equals(Object o) {
        if(o instanceof DDLeafBean) {
            DDLeafBean d = (DDLeafBean) o;
            return d.prop == prop;
        }
        return false;
    }
    
}

