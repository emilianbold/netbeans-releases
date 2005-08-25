/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.modules.j2ee.sun.share.config;

import org.netbeans.modules.j2ee.deployment.plugins.api.*;
import org.netbeans.modules.schema2beans.*;
import java.util.*;

public class DDNodeBean extends DDCommon {
    
    DDNodeBean(BaseBean bean, ModuleDDSupport support) {
        this(support.getBean(bean.parent()).proxy,bean,support);
    }
    
    DDNodeBean(DDCommon parent, BaseBean bean, ModuleDDSupport support) {
        super(parent, bean, support, bean.dtdName());
    }
    
    DDNodeBean(DDProxy proxy) {
        super(proxy);
        for(Iterator i = proxy.childBeans.iterator(); i.hasNext(); ) {
            DDCommon dd = (DDCommon) i.next();
            dd.parent = this;
        }
    }
    
}

