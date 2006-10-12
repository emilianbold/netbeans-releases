/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.wsitconf.wsdlmodelext;

import org.netbeans.modules.websvc.wsitconf.Utilities;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.policy.All;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.policy.ExactlyOne;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.policy.Policy;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.rm.*;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.WSDLComponentFactory;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.xam.Model;

import java.io.IOException;
import org.openide.ErrorManager;

/**
 *
 * @author Martin Grebac
 */
public class RMModelHelper {
    
    /**
     * Creates a new instance of RMModelHelper
     */
    public RMModelHelper() {
    }
    
    public static RMAssertion getRMAssertion(Policy p) {
        RMAssertion rmAssertion = null;
        if (p != null) {
            ExactlyOne eo = p.getExactlyOne();
            if (eo != null) {
                All all = eo.getAll();
                if (all != null) {
                    rmAssertion = all.getRMAssertion();
                }
            }
        }
        return rmAssertion;
    }
    
    // checks if RM is enabled in the config wsdl on specified binding
    public static boolean isRMEnabled(Binding b, WSDLModel model) {
        Policy p = Utilities.getPolicyForElement(b, model);
        if (p != null) {
            RMAssertion rm = getRMAssertion(p);
            return (rm != null);
        }
        return false;
    }
    
    // enables RM in the config wsdl on specified binding
    public static void enableRM(Binding b, WSDLModel model) {
        Policy p = Utilities.getPolicyForElement(b, model);
        RMAssertion rm = getRMAssertion(p);
        if (rm == null) {
            boolean isTransaction = model.isIntransaction();
            if (!isTransaction) {
                model.startTransaction();
            }
            try {
                WSDLComponentFactory wcf = model.getFactory();

                All all = null;
                if (p == null) {
                    all = PolicyModelHelper.createTopLevelPolicy(b, model, wcf);
                } else {
                    all = PolicyModelHelper.createTopExactlyOne(p, model, wcf);
                }

                RMAssertion rmAssertion = (RMAssertion)wcf.create(all, RMQName.RMASSERTION.getQName());
                all.addExtensibilityElement(rmAssertion);
            } finally {
                if (!isTransaction) {
                        model.endTransaction();
                }
            }
        }
    }

    // disables RM in the config wsdl on specified binding
    public static void disableRM(Binding b, WSDLModel model) {
        Policy p = Utilities.getPolicyForElement(b, model);
        RMAssertion rm = getRMAssertion(p);
        if (rm != null) {
            boolean isTransaction = model.isIntransaction();
            if (!isTransaction) {
                model.startTransaction();
            }
            try {
                rm.getParent().removeExtensibilityElement(rm);
                FlowControl fc = RMMSModelHelper.getFlowControl(p);
                if (fc != null) {
                    fc.getParent().removeExtensibilityElement(fc);
                }
            } finally {
                if (!isTransaction) {
                        model.endTransaction();
                }
            }
        }
    }
    
    public static String getInactivityTimeout(Binding b, WSDLModel model) {
        Policy p = Utilities.getPolicyForElement(b, model);
        RMAssertion rm = getRMAssertion(p);
        return getInactivityTimeout(rm);
    }    
    
    public static String getInactivityTimeout(RMAssertion rm) {
        String timeout = null;
        if (rm != null) {
            InactivityTimeout time = rm.getInactivityTimeout();
            if (time != null) {
                timeout = time.getMilliseconds();
            }
        }
        return timeout;
    }

    public static void setInactivityTimeout(Binding b, WSDLModel model, String value) {
        Policy p = Utilities.getPolicyForElement(b, model);
        RMAssertion rm = getRMAssertion(p);
        setInactivityTimeout(rm, value);
    }
    
    public static void setInactivityTimeout(RMAssertion rm, String value) {
        if (rm != null) {
            Model model = rm.getModel();
            boolean isTransaction = model.isIntransaction();
            if (!isTransaction) {
                model.startTransaction();
            }
            try {
                InactivityTimeout inTimeout = rm.getInactivityTimeout();
                if (inTimeout == null) {
                    if (value != null) {    // if is null, then there's no element and we want to remove it -> do nothing
                        WSDLComponentFactory wcf = rm.getModel().getFactory();
                        InactivityTimeout inT = (InactivityTimeout)wcf.create(rm, 
                                RMQName.INACTIVITYTIMEOUT.getQName()
                               );
                        inT.setMilliseconds(value);
                        rm.addExtensibilityElement(inT);
                    }
                } else {
                    if (value == null) {
                        rm.removeInactivityTimeout(inTimeout);
                    } else {
                        inTimeout.setMilliseconds(value);
                    }
                }
            } finally {
                if (!isTransaction) {
                        model.endTransaction();
                }
            }
        }
    }

}
