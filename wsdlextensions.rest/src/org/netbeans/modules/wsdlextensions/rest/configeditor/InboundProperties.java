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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.wsdlextensions.rest.configeditor;

import org.openide.util.NbBundle;

/**
 *
 * @author jqian
 */
public class InboundProperties extends ValidatableProperties  {

    public static final String HTTP_LISTENER_NAME_PROPERTY = "http-listener-name"; // NOI18N
    public static final String PATH_PROPERTY = "path"; // NOI18N
    public static final String METHOD_PROPERTY = "method"; // NOI18N
    public static final String CONSUME_TYPES_PROPERTY = "consume-types"; // NOI18N
    public static final String PRODUCE_TYPES_PROPERTY = "produce-types"; // NOI18N
    public static final String FORWARD_AS_ATTACHMENT_PROPERTY = "forward-as-attachment"; // NOI18N
    public static final String USER_DEFINED_PROPERTY = "user-defined"; // NOI18N

    public static final String[] PRE_DEFINED_PROPERTIES = new String[] {
        HTTP_LISTENER_NAME_PROPERTY,
        PATH_PROPERTY,
        METHOD_PROPERTY,
        CONSUME_TYPES_PROPERTY,
        PRODUCE_TYPES_PROPERTY,
        FORWARD_AS_ATTACHMENT_PROPERTY,
    };

    public InboundProperties() {
        super("/org/netbeans/modules/wsdlextensions/rest/template/properties/RESTInboundProperties.txt"); // NOI18N
    }

    public String getValidationError() {
        String ret = null;

        String path = get(PATH_PROPERTY);
        if (path == null || path.trim().length() == 0) {
            ret = NbBundle.getMessage(InboundProperties.class, "InboundProperties.PathMissing"); // NOI18N
        }

        return ret;
    }
}
