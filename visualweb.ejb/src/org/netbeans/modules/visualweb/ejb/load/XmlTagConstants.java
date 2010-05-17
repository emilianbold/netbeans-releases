/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
/*
 * XmlTagConstants.java
 *
 * Created on May 2, 2004, 12:41 AM
 */

package org.netbeans.modules.visualweb.ejb.load;

/**
 * Contains the XML tags constants for the EJB Data source
 *
 * @author cao
 */
public interface XmlTagConstants 
{
    // NOI18N
    public static final String EJB_GROUP_TAG = "ejb-group";
    public static final String GROUP_NAME_TAG = "group-name";
    public static final String CONTAINER_VENDOR_TAG = "container";
    public static final String SERVER_HOST_TAG = "server-host";
    public static final String IIOP_PORT_TAG = "iiop-port";
    public static final String CLIENT_JAR_TAG = "client-jar";
    public static final String BEAN_WRAPPER_JAR_TAG = "bean-wrapper-jar";
    public static final String DESIGN_TIME_TAG = "design_time_jar";
    public static final String ADDITIONAL_DD_JAR_TAG = "additional-deployment-descriptors";
    public static final String ENTERPRISE_BEANS_TAG = "enterprise-beans";
    public static final String STATELESS_SESSION_TAG = "stateless-session";
    public static final String STATEFUL_SESSION_TAG = "stateful-session";
    public static final String EJB_NAME_TAG = "ejb-name";
    public static final String JNDI_NAME_TAG = "jndi-name";
    public static final String HOME_TAG_TAG = "home";
    public static final String WRAPPER_BEAN_TAG = "wrapper-bean";
    public static final String WRAPPER_BEAN_INFO_TAG = "wrapper-bean-info";
    public static final String REMOTE_TAG = "remote";
    public static final String METHOD_TAG = "method";
    public static final String CREATE_METHOD_TAG = "create-method";
    public static final String METHOD_NAME_TAG = "method-name";
    public static final String RETURN_TYPE_TAG = "return-type";
    public static final String RETURN_ELEM_TYPE_ATTR = "element-type";
    public static final String RETURN_IS_COLLECTION_ATTR = "is-collection";
    public static final String PARAMETER_TAG = "parameter";
    public static final String PARAM_NAME_ATTR = "name";
    public static final String EXCEPTION_TAG = "exception";
    public static final String WEB_EJB_REF_TAG = "web-ejb-ref";
    public static final String BUSINESS_METHOD_ATTR = "business";
    public static final String DATAPROVIDER_TAG = "dataprovider";
}
