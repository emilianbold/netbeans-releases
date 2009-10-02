/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.xslt.core.text.completion;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Alex Petrov (16.06.2008)
 */
public interface XSLTCompletionConstants {
    String
        STYLESHEET_ELEMENT_NAME = "stylesheet", // NOI18N
        RESOURCES_DIR = "resources",
        XSLT_VERSION_1_0 = "1.0", // NOI18N
        XSLT_VERSION_1_1 = "1.1", // NOI18N
        XSLT_VERSION_2_0 = "2.0", // NOI18N
        FILE_XSLT_1_0_SCHEMA = "xslt_1_0.xsd", // NOI18N
        FILE_XSLT_1_1_SCHEMA = "xslt_1_1.xsd", // NOI18N
        FILE_XSLT_2_0_SCHEMA = "xslt_2_0.xsd", // NOI18N
        
        FILE_XSLT_1_0_FUNCTIONS = "xslt_1_0_xpath-functions.txt", // NOI18N
        FILE_XSLT_1_1_FUNCTIONS = "xslt_1_0_xpath-functions.txt", // NOI18N
        FILE_XSLT_2_0_FUNCTIONS = "xslt_2_0_xpath-functions.txt", // NOI18N

        ATTRIB_NAME = "name",
        ATTRIB_TYPE = "type",
        
        XSLT_TAG_NAME_ATTRIBUTE_SET = "attribute-set",
        ATTRIBUTE_NAME_USE_ATTRIBUTE_SETS = "use-attribute-sets",
        
        XSLT_TAG_NAME_APPLY_TEMPLATES = "apply-templates",
        XSLT_TAG_NAME_WITH_PARAM = "with-param",
        TEXT_TAG_TEMPLATE = "template",
        TEXT_ATTRIBUTE_MATCH = "match",

        ATTRIBUTE_TYPE_XSL_EXPRESSION = "xsl:expression",
        
        ATTRIBUTE_MATCH_UNDEFINED_VALUE = "undefined value"; // NOI18N
    
    Set<String> setSupportedXsltVersions = new HashSet<String>(Arrays.asList(
        new String[] {XSLT_VERSION_1_0, XSLT_VERSION_1_1, XSLT_VERSION_2_0}));
}
