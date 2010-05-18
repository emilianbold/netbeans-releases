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

package org.netbeans.modules.wlm.model.xpath;

import java.util.Collection;
import java.util.HashMap;
import javax.xml.namespace.QName;
import org.netbeans.modules.xml.xpath.ext.XPathExtensionFunction;
import org.netbeans.modules.xml.xpath.ext.XPathModel;
import org.netbeans.modules.xml.xpath.ext.metadata.ExtFunctionMetadata;
import org.netbeans.modules.xml.xpath.ext.spi.ExtensionFunctionResolver;
import org.netbeans.modules.xml.xpath.ext.spi.validation.XPathValidationContext;

/**
 * Provides information about BPEL extension functions for XPath. 
 * 
 * TODO: It's necessary to figure out which functions are still required.
 * Only 2 functions are described in the BPEL specification
 * 
 * @author nk160297
 */
public class WlmXpathExtFunctionResolver implements ExtensionFunctionResolver, 
        WlmXPathExtFunctionMetadata {

    private static HashMap<QName, ExtFunctionMetadata> mValidFunctions = 
            new HashMap<QName, ExtFunctionMetadata>();

    static {
        //
        // Runtime specific extensions.
        mValidFunctions.put(CURRENT_TIME_METADATA.getName(), 
                CURRENT_TIME_METADATA);
        mValidFunctions.put(CURRENT_DATE_METADATA.getName(), 
                CURRENT_DATE_METADATA);
        mValidFunctions.put(CURRENT_DATE_TIME_METADATA.getName(), 
                CURRENT_DATE_TIME_METADATA);
//        mValidFunctions.put(DO_MARSHAL_METADATA.getName(),
//                DO_MARSHAL_METADATA);
//        mValidFunctions.put(DO_UNMARSHAL_METADATA.getName(),
//                DO_UNMARSHAL_METADATA);
//        mValidFunctions.put(GET_BPID_METADATA.getName(),
//                GET_BPID_METADATA);
//        mValidFunctions.put(GET_GUID_METADATA.getName(),
//                GET_GUID_METADATA);
        //
        // WLM Specific functions
        mValidFunctions.put(GET_TASK_OWNER_METADATA.getName(),
                GET_TASK_OWNER_METADATA);
        mValidFunctions.put(GET_TASK_ID_METADATA.getName(),
                GET_TASK_ID_METADATA);
        mValidFunctions.put(GET_EMAIL_METADATA.getName(),
                GET_EMAIL_METADATA);
        mValidFunctions.put(GET_MANAGER_EMAIL_METADATA.getName(),
                GET_MANAGER_EMAIL_METADATA);
        mValidFunctions.put(GET_MANAGER_UID_METADATA.getName(),
                GET_MANAGER_UID_METADATA);
        //
        // Another runtime specific extensions
        // These functions are not going to be supported by the runtime
        // mValidFunctions.put(GET_GUID_METADATA.getName(), GET_GUID_METADATA);
        // mValidFunctions.put(GET_BPID_METADATA.getName(), GET_BPID_METADATA);
        // mValidFunctions.put(EXIST_METADATA.getName(), EXIST_METADATA);
        //
//        // XPath 2.0 functions
//        mValidFunctions.put(DATE_TIME_LT_METADATA.getName(),
//                DATE_TIME_LT_METADATA);
//        mValidFunctions.put(TIME_LT_METADATA.getName(), TIME_LT_METADATA);
//        mValidFunctions.put(DATE_LT_METADATA.getName(), DATE_LT_METADATA);
    }
    
    public WlmXpathExtFunctionResolver() {
    }

    public ExtFunctionMetadata getFunctionMetadata(QName name) {
        return mValidFunctions.get(name);
    }

    public boolean isImplicit(QName name) {
        String nsUri = name.getNamespaceURI();
        return nsUri.startsWith(JAVA_PROTOCOL);
    }

    public Collection<QName> getSupportedExtFunctions() {
        return mValidFunctions.keySet();
    }

    public XPathExtensionFunction newInstance(XPathModel model, QName name) {
        return null;
    }

    public void validateFunction(XPathExtensionFunction function, XPathValidationContext context) {
        assert context != null;
//        QName funcQName = function.getMetadata().getName();
//        if (GET_VARIABLE_PROPERTY_METADATA.getName().equals(funcQName)) {
//            String funcName = funcQName.getLocalPart();
//            context.addResultItem(ResultType.WARNING, 
//                    NbBundle.getMessage(BpelVariableResolver.class,
//                                    "RUNTIME_NOT_SUPPORT_EXT_FUNC"), funcName); // NOI18N
//        }
    }
}
