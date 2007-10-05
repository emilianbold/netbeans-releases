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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
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

package org.netbeans.modules.websvc.wsitconf.wsdlmodelext;

import org.netbeans.modules.websvc.wsitmodelext.addressing.Addressing10WsdlQName;
import org.netbeans.modules.websvc.wsitmodelext.addressing.Addressing10WsdlUsingAddressing;
import org.netbeans.modules.websvc.wsitmodelext.policy.All;
import org.netbeans.modules.websvc.wsitmodelext.policy.Policy;
import org.netbeans.modules.xml.wsdl.model.Binding;

/**
 *
 * @author Martin Grebac
 */
public class AddressingModelHelper {
    
    /**
     * Creates a new instance of AddressingModelHelper
     */
    public AddressingModelHelper() {
    }
    
    public static Addressing10WsdlUsingAddressing getUsingAddressing(Policy p) {
        return (Addressing10WsdlUsingAddressing) PolicyModelHelper.getTopLevelElement(p, Addressing10WsdlUsingAddressing.class);        
    }
    
    // checks if Addressing is enabled in the config wsdl on specified binding
    public static boolean isAddressingEnabled(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        if (p != null) {
            Addressing10WsdlUsingAddressing addrAssertion = getUsingAddressing(p);
            return (addrAssertion != null);
        }
        return false;
    }
    
    // enables Addressing in the config wsdl on specified binding
    public static void enableAddressing(Binding b) {
        All a = PolicyModelHelper.createPolicy(b, false);
        PolicyModelHelper.createElement(a, Addressing10WsdlQName.USINGADDRESSING.getQName(), Addressing10WsdlUsingAddressing.class, false);
    }

    // disables Addressing in the config wsdl on specified binding
    public static void disableAddressing(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        Addressing10WsdlUsingAddressing a = getUsingAddressing(p);
        if (a != null) {
            PolicyModelHelper.removeElement(a.getParent(), Addressing10WsdlUsingAddressing.class, false);
        }
        PolicyModelHelper.cleanPolicies(b);
    }
}
