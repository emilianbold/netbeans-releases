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

import org.netbeans.modules.websvc.wsitmodelext.policy.All;
import org.netbeans.modules.websvc.wsitmodelext.policy.Policy;
import org.netbeans.modules.websvc.wsitmodelext.rm.AckRequestInterval;
import org.netbeans.modules.websvc.wsitmodelext.rm.CloseTimeout;
import org.netbeans.modules.websvc.wsitmodelext.rm.Ordered;
import org.netbeans.modules.websvc.wsitmodelext.rm.RMSunClientQName;
import org.netbeans.modules.websvc.wsitmodelext.rm.RMSunQName;
import org.netbeans.modules.websvc.wsitmodelext.rm.ResendInterval;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;

/**
 *
 * @author Martin Grebac
 */
public class RMSunModelHelper {
    
    /**
     * Creates a new instance of RMSunModelHelper
     */
    public RMSunModelHelper() {
    }
    
    // enables Ordered delivery in the config wsdl on specified binding
    public static void enableOrdered(Binding b) {
        All a = PolicyModelHelper.createPolicy(b, true);
        PolicyModelHelper.createElement(a, RMSunQName.ORDERED.getQName(), Ordered.class, false);
    }

    // disables Ordered delivery in the config wsdl on specified binding
    public static void disableOrdered(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        Ordered ord = getOrdered(p);
        if (ord != null) {
            PolicyModelHelper.removeElement(ord.getParent(), Ordered.class, false);
        }
        PolicyModelHelper.cleanPolicies(b);        
    }

    // checks if Flow Control is enabled in the config wsdl  on specified binding
    public static boolean isOrderedEnabled(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        if (p != null) {
            Ordered ord = getOrdered(p);
            return (ord != null);
        }
        return false;
    }
    
    public static Ordered getOrdered(Policy p) {
        return (Ordered) PolicyModelHelper.getTopLevelElement(p, Ordered.class);
    }    

    public static String getResendInterval(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        ResendInterval ri = (ResendInterval) PolicyModelHelper.getTopLevelElement(p, ResendInterval.class);
        if (ri != null) {
            return ri.getResendInterval();
        }
        return null;
    }
    
    public static void setResendInterval(Binding b, String value) {
        WSDLModel model = b.getModel();
        All all = PolicyModelHelper.createPolicy(b, false);
        ResendInterval ri = PolicyModelHelper.createElement(all, 
                RMSunClientQName.RESENDINTERVAL.getQName(), ResendInterval.class, false);
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            if (ri != null) {
                if (value == null) {
                    PolicyModelHelper.removeElement(ri);
                } else {
                    ri.setResendInterval(value);
                }
            }
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }

    public static String getCloseTimeout(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        CloseTimeout ct = (CloseTimeout) PolicyModelHelper.getTopLevelElement(p, CloseTimeout.class);
        if (ct != null) {
            return ct.getCloseTimeout();
        }
        return null;
    }
    
    public static void setCloseTimeout(Binding b, String value) {
        WSDLModel model = b.getModel();
        All all = PolicyModelHelper.createPolicy(b, false);
        CloseTimeout ct = PolicyModelHelper.createElement(all, 
                RMSunClientQName.CLOSETIMEOUT.getQName(), CloseTimeout.class, false);
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            if (ct != null) {
                if (value == null) {
                    PolicyModelHelper.removeElement(ct);
                } else {
                    ct.setCloseTimeout(value);
                }
            }
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }
    
    public static String getAckRequestInterval(Binding b) {
        Policy p = PolicyModelHelper.getPolicyForElement(b);
        AckRequestInterval ri = PolicyModelHelper.getTopLevelElement(p, AckRequestInterval.class);
        if (ri != null) {
            return ri.getAckRequestInterval();
        }
        return null;
    }
    
    public static void setAckRequestInterval(Binding b, String value) {
        WSDLModel model = b.getModel();
        All all = PolicyModelHelper.createPolicy(b, false);
        AckRequestInterval ri = PolicyModelHelper.createElement(all, 
                RMSunClientQName.ACKREQUESTINTERVAL.getQName(), AckRequestInterval.class, false);
        boolean isTransaction = model.isIntransaction();
        if (!isTransaction) {
            model.startTransaction();
        }
        try {
            if (ri != null) {
                if (value == null) {
                    PolicyModelHelper.removeElement(ri);
                } else {
                    ri.setAckRequestInterval(value);
                }
            }
        } finally {
            if (!isTransaction) {
                model.endTransaction();
            }
        }
    }
    
}
