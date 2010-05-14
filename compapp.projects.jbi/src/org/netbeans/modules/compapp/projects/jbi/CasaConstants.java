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
public interface CasaConstants {
    public static final String CASA_NAMESPACE_URI = "http://java.sun.com/xml/ns/casa";  // NO18N
    
    public static final String CASA_ELEM_NAME = "casa";  // NO18N
    public static final String CASA_SERVICE_UNITS_ELEM_NAME = "service-units";  // NO18N
    public static final String CASA_SERVICE_ENGINE_SERVICE_UNIT_ELEM_NAME = "service-engine-service-unit";  // NO18N
    public static final String CASA_BINDING_COMPONENT_SERVICE_UNIT_ELEM_NAME = "binding-component-service-unit";  // NO18N
    public static final String CASA_LINK_ELEM_NAME = "link";  // NO18N
    public static final String CASA_ENDPOINTS_ELEM_NAME = "endpoints";  // NO18N
    public static final String CASA_ENDPOINT_ELEM_NAME = "endpoint";  // NO18N
    public static final String CASA_CONSUMES_ELEM_NAME = "consumes";  // NO18N
    public static final String CASA_PROVIDES_ELEM_NAME = "provides";  // NO18N
    public static final String CASA_PORTS_ELEM_NAME = "ports";  // NO18N
    public static final String CASA_PORT_ELEM_NAME = "port";  // NO18N
    public static final String CASA_CONNECTIONS_ELEM_NAME = "connections";  // NO18N
    public static final String CASA_CONNECTION_ELEM_NAME = "connection";  // NO18N
    public static final String CASA_PORTTYPES_ELEM_NAME = "porttypes";  // NO18N
    public static final String CASA_BINDINGS_ELEM_NAME = "bindings";  // NO18N
    public static final String CASA_SERVICES_ELEM_NAME = "services";  // NO18N
    public static final String CASA_REGIONS_ELEM_NAME = "regions";  // NO18N
    public static final String CASA_REGION_ELEM_NAME = "region";  // NO18N
    
    public static final String CASA_ENDPOINT_ATTR_NAME = "endpoint";  // NO18N
    public static final String CASA_CONSUMER_ATTR_NAME = "consumer";  // NO18N
    public static final String CASA_PROVIDER_ATTR_NAME = "provider";  // NO18N
    public static final String CASA_INTERNAL_ATTR_NAME = "internal";  // NO18N
    public static final String CASA_DEFINED_ATTR_NAME = "defined";  // NO18N
    public static final String CASA_UNKNOWN_ATTR_NAME = "unknown";  // NO18N
    public static final String CASA_STATE_ATTR_NAME = "state";  // NO18N
    public static final String CASA_X_ATTR_NAME = "x";  // NO18N
    public static final String CASA_Y_ATTR_NAME = "y";  // NO18N
    public static final String CASA_NAME_ATTR_NAME = "name";  // NO18N
    public static final String CASA_WIDTH_ATTR_NAME = "width";  // NO18N
    public static final String CASA_COMPONENT_NAME_ATTR_NAME = "component-name";  // NO18N
    public static final String CASA_UNIT_NAME_ATTR_NAME = "unit-name";  // NO18N
    public static final String CASA_DESCRIPTION_ATTR_NAME = "description";  // NO18N
    public static final String CASA_ARTIFACTS_ZIP_ATTR_NAME = "artifacts-zip";  // NO18N
    public static final String CASA_ENDPOINT_NAME_ATTR_NAME = "endpoint-name";  // NO18N
    public static final String CASA_SERVICE_NAME_ATTR_NAME = "service-name";  // NO18N
    public static final String CASA_INTERFACE_NAME_ATTR_NAME = "interface-name";  // NO18N
    public static final String CASA_BINDING_TYPE_ATTR_NAME = "bindingType";  // NO18N
    
    public static final String CASA_NEW_ATTR_VALUE = "new";  // NO18N
    public static final String CASA_DELETED_ATTR_VALUE = "deleted";  // NO18N
    public static final String CASA_UNCHANGED_ATTR_VALUE = "unchanged";  // NO18N
    
    public static final String CASA_DUMMY_PORTTYPE="dummyCasaPortType"; // NOI18N
}
