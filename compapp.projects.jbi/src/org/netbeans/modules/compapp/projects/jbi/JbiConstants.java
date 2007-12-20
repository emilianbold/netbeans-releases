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

package org.netbeans.modules.compapp.projects.jbi;

/**
 *
 * @author jqian
 */
public interface JbiConstants {
    public static final String JBI_NAMESPACE_URI = "http://java.sun.com/xml/ns/jbi";  // NO18N
    
    public static final String JBI_ELEM_NAME = "jbi";  // NO18N
    public static final String JBI_SERVICE_ASSEMBLY_ELEM_NAME = "service-assembly";  // NO18N
    public static final String JBI_SERVICE_UNIT_ELEM_NAME = "service-unit";  // NO18N
    public static final String JBI_TARGET_ELEM_NAME = "target";  // NO18N
    public static final String JBI_COMPONENT_NAME_ELEM_NAME = "component-name";  // NO18N
    public static final String JBI_ARTIFACTS_ZIP_ELEM_NAME = "artifacts-zip";  // NO18N
    public static final String JBI_IDENTIFICATION_ELEM_NAME = "identification";  // NO18N
    public static final String JBI_DESCRIPTION_ELEM_NAME = "description";  // NO18N
    public static final String JBI_NAME_ELEM_NAME = "name";  // NO18N
    public static final String JBI_CONNECTIONS_ELEM_NAME = "connections";  // NO18N
    public static final String JBI_CONNECTION_ELEM_NAME = "connection";  // NO18N  
    public static final String JBI_PROVIDER_ELEM_NAME = "provider";  // NO18N
    public static final String JBI_CONSUMER_ELEM_NAME = "consumer";  // NO18N
    public static final String JBI_PROVIDES_ELEM_NAME = "provides";  // NO18N
    public static final String JBI_CONSUMES_ELEM_NAME = "consumes";  // NO18N
    public static final String JBI_SERVICES_ELEM_NAME = "services";  // NO18N
    
    public static final String JBI_BINDING_COMPONENT_ATTR_NAME = "binding-component"; // NOI18N
    public static final String JBI_ENDPOINT_NAME_ATTR_NAME = "endpoint-name";  // NO18N
    public static final String JBI_SERVICE_NAME_ATTR_NAME = "service-name";  // NO18N
    public static final String JBI_INTERFACE_NAME_ATTR_NAME = "interface-name";  // NO18N    
}
