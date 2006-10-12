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

import java.io.IOException;
import org.openide.ErrorManager;

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
    public static void enableOrdered(Binding b, WSDLModel model) {
        Policy p = Utilities.getPolicyForElement(b, model);
        Ordered ord = getOrdered(p);
        if (ord == null) {
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

                Ordered ordered = (Ordered)wcf.create(all, RMSunQName.ORDERED.getQName());
                all.addExtensibilityElement(ordered);
            } finally {
                if (!isTransaction) {
                        model.endTransaction();
                }
            }
        }
    }

    // disables Ordered delivery in the config wsdl on specified binding
    public static void disableOrdered(Binding b, WSDLModel model) {
        Policy p = Utilities.getPolicyForElement(b, model);
        Ordered ord = getOrdered(p);
        if (ord != null) {
            boolean isTransaction = model.isIntransaction();
            if (!isTransaction) {
                model.startTransaction();
            }
            try {
                ord.getParent().removeExtensibilityElement(ord);
            } finally {
                if (!isTransaction) {
                        model.endTransaction();
                }
            }
        }
    }

    // checks if Flow Control is enabled in the config wsdl  on specified binding
    public static boolean isOrderedEnabled(Binding b, WSDLModel model) {
        Policy p = Utilities.getPolicyForElement(b, model);
        if (p != null) {
            Ordered ord = getOrdered(p);
            return (ord != null);
        }
        return false;
    }
    
    public static Ordered getOrdered(Binding b, WSDLModel model) {
        Policy p = Utilities.getPolicyForElement(b, model);
        return getOrdered(p);
    }

    public static Ordered getOrdered(Policy p) {
        Ordered ordered = null;
        if (p != null) {
            ExactlyOne eo = p.getExactlyOne();
            if (eo != null) {
                All all = eo.getAll();
                if (all != null) {
                    ordered = all.getOrdered();
                }
            }
        }
        return ordered;
    }    
    
}
