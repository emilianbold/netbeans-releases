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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.websvc.axis2.wizards;

/**
 *
 * @author mkuchtiak
 */
public interface WizardProperties {
    public static final String PROP_FROM_JAVA_TYPE="prop_from_java_type"; //NOI18N
    public static final String PROP_JAVA_CLASS="prop_java_class"; //NOI18N
    public static final String PROP_GENERATE_WSDL="prop_generate_wsdl"; //NOI18N
    public static final String PROP_GENERATE_SAMPLE_METHOD="prop_generate_sample_method"; //NOI18N
    public static final String PROP_WSDL_URL="prop_wsdl_URL"; //NOI18N
    public static final String PROP_SERVICE_NAME="prop_service_name"; //NOI18N
    public static final String PROP_PORT_NAME="prop_port_name"; //NOI18N
    public static final String PROP_PACKAGE_NAME="prop_package_name"; //NOI18N
    public static final String PROP_DATABINDING_NAME="prop_databinding_name"; //NOI18N
    public static final String PROP_SEI="prop_sei"; //NOI18N
    public static final String PROP_WS_TO_JAVA_OPTIONS="pws_to_java_optionsi"; //NOI18N
    public static final Boolean JAVA_TYPE_EMPTY=Boolean.TRUE;
    public static final Boolean JAVA_TYPE_EXISTING=Boolean.FALSE;
    public static final String[] DATA_BINDING = {"ADB", "XML Beans", "JiBX", "none(Axiom)"}; //NOI18N
    public static final String BINDING_ADB="ADB"; //NOI18N
    public static final String BINDING_XML_BEANS="XML Beans"; //NOI18N
    public static final String BINDING_JIBX="JiBX"; //NOI18N
    public static final String BINDING_AXIOM="none(Axiom)"; //NOI18N
}
