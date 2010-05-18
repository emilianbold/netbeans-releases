/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 1997-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.wlm.model.impl;

import org.netbeans.modules.wlm.model.api.ExpressionLanguageSpec;
import org.netbeans.modules.wlm.model.api.TAction;
import org.netbeans.modules.wlm.model.api.TImport;
import org.netbeans.modules.wlm.model.api.TTask;
import org.netbeans.modules.wlm.model.api.EndpointReference;
import org.netbeans.modules.wlm.model.api.TLocalNotification;
import org.netbeans.modules.xml.xam.Named;
import org.netbeans.modules.xml.xam.dom.Attribute;

/**
 * 
 * @author nn136682
 */
public enum WLMAttribute implements Attribute {

    EXPRESSION_LANGUAGE(ExpressionLanguageSpec.EXPRESSION_LANGUAGE),
    TYPE(TAction.TYPE_PROPERTY),
    LOCATION(TImport.LOCATION_PROPERTY),
    NAMESPACE(TImport.NAMESPACE_PROPERTY),
    PORT_TYPE(EndpointReference.PORT_TYPE_PROPERTY),
    OPERATION(EndpointReference.OPERATION_PROPERTY),
    NAME(Named.NAME_PROPERTY),
    REFERENCE(TLocalNotification.REFERENCE_PROPERTY),
    TARGET_NAME_SPACE(TTask.TARGET_NAMESPACE_PROPERTY),
    TITLE(TTask.TITLE_PROPERTY),
    PRIORITY(TTask.PRIORITY_PROPERTY);
    
    private String name;
    private Class type;
    private Class subtype;

    /** Creates a new instance of WSDLAttribute */
    WLMAttribute(String name) {
        this(name, String.class);
    }

    WLMAttribute(String name, Class type) {
        this(name, type, null);
    }

    WLMAttribute(String name, Class type, Class subtype) {
        this.name = name;
        this.type = type;
        this.subtype = subtype;
    }

    @Override
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
}
