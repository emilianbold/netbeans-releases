/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.compapp.casaeditor.model.casa.impl;

import java.util.Collection;
import javax.xml.namespace.QName;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaQName;
import org.netbeans.modules.xml.xam.dom.Attribute;

/**
 *
 * @author jqian
 */
public enum CasaAttribute implements Attribute {
        NS("xmlns"),            // NOI18N // TMP
        TYPE("type"),           // NOI18N
        NAME("name"),           // NOI18N
        UNIT_NAME("unit-name"), // NOI18N
        DESCRIPTION("description"),         // NOI18N
        COMPONENT_NAME("component-name"),   // NOI18N
        ARTIFACTS_ZIP("artifacts-zip"),     // NOI18N
        WIDTH("width"),                     // NOI18N
        X("x"),                             // NOI18N
        Y("y"),                             // NOI18N
        CONSUMER("consumer"),               // NOI18N
        PROVIDER("provider"),               // NOI18N
        IS_CONSUME("is-consume"),           // NOI18N
        ENDPOINT_NAME("endpoint-name"),     // NOI18N
        SERVICE_NAME("service-name"),       // NOI18N
        INTERFACE_NAME("interface-name"),   // NOI18N    
        ENDPOINT("endpoint"),               // NOI18N
        STATE("state"),                     // NOI18N
        INTERNAL("internal"),               // NOI18N
        DEFINED("defined"),                 // NOI18N
        UNKNOWN("unknown"),                 // NOI18N
        BINDINGSTATE("bindingState"),       // NOI18N
        BINDINGTYPE("bindingType"),         // NOI18N
        PORTTYPE("portType"),               // NOI18N
        TARGET_NAMESPACE("targetNamespace"),// NOI18N
        DISPLAY_NAME("display-name"),       // NOI18N
        PROCESS_NAME("process-name"),       // NOI18N
        FILE_PATH("file-path");             // NOI18N
    
    private String name;
    private Class type;
    private Class subtype;
    private String state;
    
    /**
     * Creates a new instance of CasaAttribute
     */
    CasaAttribute(String name) {
        this(name, String.class);
    }
    
    CasaAttribute(String name, Class type) {
        this(name, type, null);
    }
    
    CasaAttribute(String name, Class type, Class subtype) {
        this.name = name;
        this.type = type;
        this.subtype = subtype;
    }
    
    public String toString() { 
        return name; 
    }

    public Class getType() {
        return type;
    }

    public String getName() { 
        return name; 
    }

    public Class getMemberType() { 
        return subtype; 
    }
    
    public QName getQName() {
        return new QName(CasaQName.CASA_NS_URI, name);
    }
}
