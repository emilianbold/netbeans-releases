/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (this.prop != null ? this.prop.hashCode() : 0);
        hash = 17 * hash + this.index;
        hash = 17 * hash + (this.indexed ? 1 : 0);
        return hash;
    }
    
}

